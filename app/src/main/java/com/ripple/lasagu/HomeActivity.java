package com.ripple.lasagu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.example.games.basegameutils.BaseGameUtils;

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
        findViewById(R.id.break_10).setOnClickListener(this);
        findViewById(R.id.break_25).setOnClickListener(this);
        findViewById(R.id.break_50).setOnClickListener(this);
        findViewById(R.id.break_100).setOnClickListener(this);
        findViewById(R.id.ach).setOnClickListener(this);
        findViewById(R.id.lead_10).setOnClickListener(this);
        findViewById(R.id.lead_25).setOnClickListener(this);
        findViewById(R.id.lead_50).setOnClickListener(this);
        findViewById(R.id.lead_100).setOnClickListener(this);

        initPrefs();

    }

    private void initPrefs() {

        if (PreferenceManager.getInstance(this).getBoolean(Constants.FIRST)) {
        } else {
            PreferenceManager.getInstance(this).putLong(Constants.BALLS_10, 1000 * 1000);
            PreferenceManager.getInstance(this).putLong(Constants.BALLS_25, 1000 * 1000);
            PreferenceManager.getInstance(this).putLong(Constants.BALLS_50, 1000 * 1000);
            PreferenceManager.getInstance(this).putLong(Constants.BALLS_100, 1000 * 1000);
            PreferenceManager.getInstance(this).put(Constants.FIRST, true);
        }
    }

    private void getScoresIfExist() {
        Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient, getString(R.string.leaderboard_break_10), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
            @Override
            public void onResult(final Leaderboards.LoadPlayerScoreResult scoreResult) {
                if (isScoreResultValid(scoreResult)) {
                    long mPoints = scoreResult.getScore().getRawScore();
                    long mScore = PreferenceManager.getInstance(HomeActivity.this).getLong(Constants.BALLS_10);
                    if (mPoints > mScore)
                        Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_break_10), mScore);
                }
            }
        });
    }

    private boolean isScoreResultValid(final Leaderboards.LoadPlayerScoreResult scoreResult) {
        return scoreResult != null && GamesStatusCodes.STATUS_OK == scoreResult.getStatus().getStatusCode() && scoreResult.getScore() != null;
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        findViewById(R.id.sign_in).setVisibility(View.GONE);
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
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
        findViewById(R.id.sign_in).setVisibility(View.VISIBLE);
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
                        requestCode, resultCode, R.string.accept);
            }
        }
    }

    private void signInClicked() {
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.break_10:
                startActivity(new Intent(this, GameActivity.class).putExtra(Constants.NO_OF_BALLS, 10));
                break;
            case R.id.break_25:
                startActivity(new Intent(this, GameActivity.class).putExtra(Constants.NO_OF_BALLS, 25));
                break;
            case R.id.break_50:
                startActivity(new Intent(this, GameActivity.class).putExtra(Constants.NO_OF_BALLS, 50));
                break;
            case R.id.break_100:
                startActivity(new Intent(this, GameActivity.class).putExtra(Constants.NO_OF_BALLS, 100));
                break;
            case R.id.lead_10:
                startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                        getResources().getString(R.string.leaderboard_break_10)), 2);
                break;
            case R.id.lead_25:
                startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                        getResources().getString(R.string.leaderboard_break_25)), 2);
                break;
            case R.id.lead_50:
                startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                        getResources().getString(R.string.leaderboard_break_50)), 2);
                break;
            case R.id.lead_100:
                startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                        getResources().getString(R.string.leaderboard_break_100)), 2);
                break;
            case R.id.sign_in:
                signInClicked();
                break;
            case R.id.ach:
                startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 3);
                break;
        }
    }


}
