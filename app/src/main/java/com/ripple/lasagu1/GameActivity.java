package com.ripple.lasagu1;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class GameActivity extends AppCompatActivity implements ResultPass {

    Toolbar toolbar;
    TextView mBalls, mTime, mBallsPlace;
    GridView gridView;
    GridAdapter gridAdapter;
    int ballsLeft = 0, initialBalls = 0;
    long t, pausedTime = 0, pausedT = 0;
    private Handler mHandler;
    boolean paused = false;
    private boolean mStarted = false;
    long seconds, millis;
    boolean timeLessMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mBalls = (TextView) findViewById(R.id.balls_amount);
        mBallsPlace = (TextView) findViewById(R.id.balls_place);
        mTime = (TextView) findViewById(R.id.time);

        ballsLeft = getIntent().getIntExtra(Constants.NO_OF_BALLS, 10);
        if (ballsLeft != 100) {
            initialBalls = ballsLeft;
            t = System.currentTimeMillis();
            mHandler = new Handler();
        } else {
            ballsLeft = 0;
            timeLessMode = true;
            mTime.setVisibility(View.GONE);
            mBallsPlace.setText("BALLS BROKEN : ");
        }
        mBalls.setText(String.valueOf(ballsLeft));

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
            if (!timeLessMode) {
                ballsLeft--;
                if (ballsLeft == 0) {
                    showWinDialog();
                }
            } else {
                ballsLeft++;
            }
            mBalls.setText(String.valueOf(ballsLeft));

        } else {
            showLostDialog();
        }
    }

    private void showLostDialog() {
        if (mStarted) {
            mHandler.removeCallbacks(mRunnable);
            Toast.makeText(this, "Sorry bro , you lost the game by " + ballsLeft + " ball", Toast.LENGTH_SHORT).show();
            onBackPressed();
        } else
            handleScore();
    }

    private void showWinDialog() {
        mHandler.removeCallbacks(mRunnable);
        handleScore();
    }

    private void handleScore() {
        if (!timeLessMode) {
            long current = PreferenceManager.getInstance(this).getLong(Constants.TIMED);
            if (millis < current) {
                PreferenceManager.getInstance(this).putLong(Constants.TIMED, millis);
                Toast.makeText(this, "New record updated with time " + millis + " ms", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, "Missed record score by " + (millis - current) + " ms", Toast.LENGTH_SHORT).show();
        } else {
            int current = PreferenceManager.getInstance(this).getInt(Constants.TIMELESS);
            Log.i("SCORE", current + " " + ballsLeft);
            if (ballsLeft > current) {
                PreferenceManager.getInstance(this).put(Constants.TIMELESS, ballsLeft);
                Toast.makeText(this, "New record updated with balls broken " + ballsLeft + "", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, "Missed record score by " + (current - ballsLeft) + " balls", Toast.LENGTH_SHORT).show();
        }
        onBackPressed();
    }

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mStarted) {
                millis = (System.currentTimeMillis() - t) - (pausedTime * 1000);
                seconds = ((System.currentTimeMillis() - t) / 1000) - pausedTime;
                mTime.setText(String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60));
                mHandler.postDelayed(mRunnable, 1000L);
            }
        }

    };

    @Override
    protected void onStart() {
        super.onStart();
        if (!timeLessMode) {
            mStarted = true;
            mHandler.postDelayed(mRunnable, 1000L);
            if (paused)
                pausedTime += ((System.currentTimeMillis() - pausedT) / 1000);
            paused = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!timeLessMode) {
            mStarted = false;
            mHandler.removeCallbacks(mRunnable);
            pausedT = System.currentTimeMillis();
            paused = true;
        }
    }

}
