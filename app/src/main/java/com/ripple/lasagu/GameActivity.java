package com.ripple.lasagu;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.util.Locale;

public class GameActivity extends AppCompatActivity implements ResultPass, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    Toolbar toolbar;
    TextView balls, time;
    GridView gridView;
    GridAdapter gridAdapter;
    int ballsLeft = 25, initialBalls = 0;
    long t, pausedTime = 0, pausedT = 0;
    private Handler mHandler;
    boolean paused = false;
    private boolean mStarted;
    long seconds, millis;
    GoogleApiClient mGoogleApiClient;
    private boolean mResolvingConnectionFailure = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ballsLeft = getIntent().getIntExtra(Constants.NO_OF_BALLS, 10);
        initialBalls = ballsLeft;
        t = System.currentTimeMillis();
        mHandler = new Handler();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        balls = (TextView) findViewById(R.id.balls_amount);
        time = (TextView) findViewById(R.id.time);
        balls.setText(String.valueOf(ballsLeft));

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        gridView = (GridView) findViewById(R.id.grid);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int height = metrics.heightPixels;
        gridAdapter = new GridAdapter(this, height, this);
        gridView.setAdapter(gridAdapter);

    }

    @Override
    public void passResult(boolean isRight) {
        if (isRight) {
            ballsLeft--;
            balls.setText(String.valueOf(ballsLeft));
            if (ballsLeft == 0) {
                showWinDialog();
            }
        } else {
            showLostDialog();
        }
    }

    private void showLostDialog() {
        mHandler.removeCallbacks(mRunnable);
        Toast.makeText(this, "Sorry bro , you lost the game by " + ballsLeft + " balls", Toast.LENGTH_SHORT).show();
        onBackPressed();
    }

    private void showWinDialog() {
        mHandler.removeCallbacks(mRunnable);
        handleScore();
    }

    private void handleScore() {
        switch (initialBalls) {
            case 10:
                long current = PreferenceManager.getInstance(this).getLong(Constants.BALLS_10);
                Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_break_10), millis);
                Log.i("SCORE", current + " " + millis);
                if (millis < current) {
                    PreferenceManager.getInstance(this).putLong(Constants.BALLS_10, millis);
                    Toast.makeText(this, "New record updated with time " + millis + " ms", Toast.LENGTH_SHORT).show();
                }
                break;
            case 25:
                long c25 = PreferenceManager.getInstance(this).getLong(Constants.BALLS_25);
                if (millis < c25) {
                    PreferenceManager.getInstance(this).putLong(Constants.BALLS_25, millis);
                    Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_break_25), millis);
                    Toast.makeText(this, "New record updated with time " + millis + " ms", Toast.LENGTH_SHORT).show();
                }
                break;
            case 50:
                long c50 = PreferenceManager.getInstance(this).getLong(Constants.BALLS_50);
                if (millis < c50) {
                    PreferenceManager.getInstance(this).putLong(Constants.BALLS_50, millis);
                    Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_break_50), millis);
                    Toast.makeText(this, "New record updated with time " + millis + " ms", Toast.LENGTH_SHORT).show();
                }

                break;
            case 100:
                long c100 = PreferenceManager.getInstance(this).getLong(Constants.BALLS_100);
                if (millis < c100) {
                    PreferenceManager.getInstance(this).putLong(Constants.BALLS_100, millis);
                    Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_break_100), millis);
                    Toast.makeText(this, "New record updated with time " + millis + " ms", Toast.LENGTH_SHORT).show();
                }

                break;


        }
        onBackPressed();
    }

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mStarted) {
                millis = (System.currentTimeMillis() - t) - (pausedTime * 1000);
                seconds = ((System.currentTimeMillis() - t) / 1000) - pausedTime;
                time.setText(String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60));
                mHandler.postDelayed(mRunnable, 1000L);
            }
        }

    };

    @Override
    protected void onStart() {
        super.onStart();
        mStarted = true;
        mHandler.postDelayed(mRunnable, 1000L);
        if (paused)
            pausedTime += ((System.currentTimeMillis() - pausedT) / 1000);
        paused = false;
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mStarted = false;
        mHandler.removeCallbacks(mRunnable);
        pausedT = System.currentTimeMillis();
        paused = true;
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            return;
        }
        mResolvingConnectionFailure = true;
        if (!BaseGameUtils.resolveConnectionFailure(GameActivity.this,
                mGoogleApiClient, connectionResult,
                9001, "Error")) {
            mResolvingConnectionFailure = false;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }
}
