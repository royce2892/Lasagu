package com.ripple.lasagu1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.ripple.lasagu1.game.BaseGameUtils;

/**
 * Created by royce on 18-05-2016.
 */
public class HomeActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInflow = true;
    private boolean mSignInClicked = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_home);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        findViewById(R.id.sign_in).setOnClickListener(this);
        findViewById(R.id.play_timed).setOnClickListener(this);
        findViewById(R.id.play_timeless).setOnClickListener(this);
        findViewById(R.id.practice).setOnClickListener(this);
        findViewById(R.id.ach).setOnClickListener(this);
        findViewById(R.id.timed).setOnClickListener(this);
        findViewById(R.id.timeless).setOnClickListener(this);
        findViewById(R.id.sign_out).setOnClickListener(this);


        initPrefs();

    }

    private void initPrefs() {

        if (PreferenceManager.getInstance(this).getBoolean(Constants.NOT_FIRST)) {
            mGoogleApiClient.connect();
        } else {
            PreferenceManager.getInstance(this).putLong(Constants.TIMED, 1000 * 1000);
            PreferenceManager.getInstance(this).put(Constants.TIMELESS, 0);
            PreferenceManager.getInstance(this).put(Constants.NOT_FIRST, true);
        }
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
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        findViewById(R.id.sign_in).setVisibility(View.GONE);
        findViewById(R.id.sign_out).setVisibility(View.VISIBLE);
        Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_sign_in_with_google_plus));
        if (PreferenceManager.getInstance(this).getBoolean(Constants.NOT_FIRST)) {
            long time = PreferenceManager.getInstance(this).getLong(Constants.TIMED);
            if (time != 1000 * 1000 && time != 0) {
                testTime(time);
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_first_timed_win));
                Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_timed), time);
            }
            int balls = PreferenceManager.getInstance(this).getInt(Constants.TIMELESS);
            if (balls != 0)
                Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_timeless), balls);
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


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            return;
        }

        if (mSignInClicked || mAutoStartSignInflow) {
            mAutoStartSignInflow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            if (!BaseGameUtils.resolveConnectionFailure(HomeActivity.this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, "Error")) {
                mResolvingConnectionFailure = false;
                findViewById(R.id.sign_in).setVisibility(View.VISIBLE);
                findViewById(R.id.sign_out).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
        findViewById(R.id.sign_in).setVisibility(View.VISIBLE);
        findViewById(R.id.sign_out).setVisibility(View.GONE);
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

    private void signInClicked() {
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    private void signOutClicked() {
        Games.signOut(mGoogleApiClient);
        mGoogleApiClient.disconnect();
        PreferenceManager.getInstance(this).clear();
        findViewById(R.id.sign_in).setVisibility(View.VISIBLE);
        findViewById(R.id.sign_out).setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_timed:
                startActivity(new Intent(this, GameActivity.class).putExtra(Constants.NO_OF_BALLS, 10));
                break;
            case R.id.play_timeless:
                startActivity(new Intent(this, GameActivity.class).putExtra(Constants.NO_OF_BALLS, 100));
                break;
            case R.id.practice:
//                startActivity(new Intent(this, GameActivity.class).putExtra(Constants.NO_OF_BALLS, 50));
                break;

            case R.id.timed:
                Games.setViewForPopups(mGoogleApiClient, getWindow().getDecorView().findViewById(android.R.id.content));
                startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                        getResources().getString(R.string.leaderboard_timed)), 5000);
                break;
            case R.id.timeless:
                Games.setViewForPopups(mGoogleApiClient, getWindow().getDecorView().findViewById(android.R.id.content));
                startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                        getResources().getString(R.string.leaderboard_timeless)), 5001);
                break;
            case R.id.sign_in:
                signInClicked();
                break;
            case R.id.sign_out:
                signOutClicked();
                break;
            case R.id.ach:
                startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 3);
                break;
        }
    }


}
