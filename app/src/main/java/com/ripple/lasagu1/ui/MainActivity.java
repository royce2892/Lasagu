package com.ripple.lasagu1.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.ripple.lasagu1.R;
import com.ripple.lasagu1.game.BaseGameUtils;
import com.ripple.lasagu1.util.Constants;
import com.ripple.lasagu1.util.PreferenceManager;

/**
 * Created by royce on 23-05-2016.
 */
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, HomeFragment.Listener, GameFragment.Listener {

    HomeFragment mHomeFragment;
    GameFragment mGameFragment;
    Bundle gameArgs;

    private static int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingConnectionFailure = false;
    private boolean mSignInClicked = false;
    private boolean mRecordsUpdated = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        mGoogleApiClient.connect();

        mHomeFragment = new HomeFragment();

        getSupportFragmentManager().
                beginTransaction().
                add(R.id.frame_container, mHomeFragment).
                addToBackStack("none").
                commit();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mHomeFragment.updateUi(false);
        if (!mRecordsUpdated) {
            updateTimedRecords();
            updateTimeLessRecords();
            mRecordsUpdated = true;
        }
    }

   /* @Override
    protected void onD() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }*/

    @Override
    public void onConnectionSuspended(int i) {
        mHomeFragment.updateUi(true);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            return;
        }

        if (mSignInClicked) {
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            if (!BaseGameUtils.resolveConnectionFailure(MainActivity.this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, "Error")) {
                mResolvingConnectionFailure = false;
//                mHomeFragment.updateUi(true);
            }
        }
    }

    @Override
    public void onStartGameRequested(int mode) {

        mGameFragment = null;
        mGameFragment = new GameFragment();
        gameArgs = new Bundle();
        if (mode == Constants.MODE_TIMED) {
            gameArgs.putInt(Constants.NO_OF_BALLS, 10);
        } else {
            gameArgs.putInt(Constants.NO_OF_BALLS, 100);
        }
        mGameFragment.setArguments(gameArgs);
        switchToFragment(mGameFragment);
    }

    @Override
    public void onShowAchievementsRequested() {
        if (isSignedIn())
            startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 3);

    }

    @Override
    public void onShowLeaderboardsRequested(int mode) {
        if (isSignedIn()) {
            if (mode == Constants.MODE_TIMED)
                startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, getString(R.string.leaderboard_timed)), 3);
            else
                startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, getString(R.string.leaderboard_timeless)), 4);

        }
    }

    @Override
    public void onSignInButtonClicked() {
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    @Override
    public void onSignOutButtonClicked() {
        Games.signOut(mGoogleApiClient);
        mGoogleApiClient.disconnect();
        mHomeFragment.updateUi(true);
        PreferenceManager.getInstance(this).clear();
        mResolvingConnectionFailure = false;
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == Activity.RESULT_OK) {
                mGoogleApiClient.connect();
            } else {

                BaseGameUtils.showActivityResultError(this,
                        requestCode, resultCode, R.string.signin_other_error);
            }
        } /*else
            super.onActivityResult(requestCode, resultCode, intent);*/

    }

    private boolean isSignedIn() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }

    private void switchToFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().add(R.id.frame_container,
                fragment).addToBackStack("none").commit();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if (getSupportFragmentManager().getBackStackEntryCount() != 1)
            getSupportFragmentManager().popBackStack();
        else
            this.finish();
    }

    private void updateTimeLessRecords() {
        int balls = PreferenceManager.getInstance(this).getInt(Constants.TIMELESS);
        if (balls != 0 && isSignedIn())
            Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_timeless), balls);
    }

    private void updateTimedRecords() {
        long time = PreferenceManager.getInstance(this).getLong(Constants.TIMED);
        if (time != 1000 * 1000 && time != 0) {
            testTime(time);
            if (isSignedIn()) {
                Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_timed), time);
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_first_timed_win));
            }
        }
    }

    private void testTime(long time) {

        time = time / 1000;
        if (time < 60)
            Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_timed_in_a_minute));
        if (time < 45)
            Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_timed_in_45s));
        if (time < 30)
            Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_timed_in_30s));
        if (time < 25)
            Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_timed_in_25s));
        if (time < 20)
            Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_timed_in_20s));
        if (time < 15)
            Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_timed_in_15s));

    }

    private void getScoresIfExist() {
        /*Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient, getString(R.string.leaderboard_break_10), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
            @Override
            public void onResult(final Leaderboards.LoadPlayerScoreResult scoreResult) {
                if (isScoreResultValid(scoreResult)) {
                    long mPoints = scoreResult.getScore().getRawScore();
                    long mScore = PreferenceManager.getInstance(HomeActivity.this).getLong(Constants.TIMED);
                    if (mPoints > mScore)
                        Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_break_10), mScore);
                }
            }
        });*/
    }

    private boolean isScoreResultValid(final Leaderboards.LoadPlayerScoreResult scoreResult) {
        return scoreResult != null && GamesStatusCodes.STATUS_OK == scoreResult.getStatus().getStatusCode() && scoreResult.getScore() != null;
    }

    @Override
    public void onNewHighScore(boolean timeLessMode) {
        if (timeLessMode)
            updateTimeLessRecords();
        else
            updateTimedRecords();
    }
}
