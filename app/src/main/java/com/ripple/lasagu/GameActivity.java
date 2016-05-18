package com.ripple.lasagu;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.widget.GridView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.internal.AppEventsLoggerUtility;
import com.facebook.internal.GraphUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class GameActivity extends AppCompatActivity implements ResultPass {

    Toolbar toolbar;
    TextView balls, time;
    GridView gridView;
    GridAdapter gridAdapter;
    int ballsLeft = 25;
    long t, pausedTime = 0, pausedT = 0;
    private Handler mHandler;
    boolean paused = false;
    private boolean mStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t = System.currentTimeMillis();
        mHandler = new Handler();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        balls = (TextView) findViewById(R.id.balls_amount);
        time = (TextView) findViewById(R.id.time);
        balls.setText(String.valueOf(ballsLeft));

        gridView = (GridView) findViewById(R.id.grid);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int height = metrics.heightPixels;
        gridAdapter = new GridAdapter(this, height, this);
        gridView.setAdapter(gridAdapter);

//        GraphRequest.newPostRequest(AccessToken.getCurrentAccessToken(),GraphRequest.)
    }

    @Override
    public void passResult(boolean isRight) {
        if (isRight) {
            ballsLeft--;
            balls.setText(String.valueOf(ballsLeft));
        } else {

        }
    }

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mStarted) {
                long seconds = ((System.currentTimeMillis() - t) / 1000) - pausedTime;
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        mStarted = false;
        mHandler.removeCallbacks(mRunnable);
        pausedT = System.currentTimeMillis();
        paused = true;
    }
}
