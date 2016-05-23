package com.ripple.lasagu1.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.ripple.lasagu1.R;
import com.ripple.lasagu1.util.Constants;
import com.ripple.lasagu1.util.PreferenceManager;

import java.util.Locale;

/**
 * Created by royce on 23-05-2016.
 */
public class GameFragment extends Fragment implements ResultPass {

    TextView mBalls, mTime, mBallsPlace;
    GridView gridView;
    GameGridAdapter gameGridAdapter;
    int ballsLeft = 0, initialBalls = 0;
    long t, pausedTime = 0, pausedT = 0;
    private Handler mHandler;
    boolean paused = false;
    private boolean mStarted = false;
    long seconds, millis;
    boolean timeLessMode = false;
    Listener mListener;

    public interface Listener {
        void onNewHighScore(boolean timeLessMode);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ballsLeft = getArguments().getInt(Constants.NO_OF_BALLS, 10);
        return inflater.inflate(R.layout.activity_game, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBalls = (TextView) view.findViewById(R.id.balls_amount);
        mBallsPlace = (TextView) view.findViewById(R.id.balls_place);
        mTime = (TextView) view.findViewById(R.id.time);

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

        gridView = (GridView) view.findViewById(R.id.grid);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int height = metrics.heightPixels;
        gameGridAdapter = new GameGridAdapter(getContext(), height, this);
        gridView.setAdapter(gameGridAdapter);
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
            Toast.makeText(getActivity(), "Sorry bro , you lost the game by " + ballsLeft + " ball", Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
            //onBackPressed();
        } else
            handleScore();
    }

    private void showWinDialog() {
        mHandler.removeCallbacks(mRunnable);
        handleScore();
    }

    private void handleScore() {
        if (!timeLessMode) {
            long current = PreferenceManager.getInstance(getContext()).getLong(Constants.TIMED);
            mListener.onNewHighScore(timeLessMode);
            if (millis < current) {
                PreferenceManager.getInstance(getContext()).putLong(Constants.TIMED, millis);
                Toast.makeText(getActivity(), "New record updated with time " + millis + " ms", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(getActivity(), "Missed record score by " + (millis - current) + " ms", Toast.LENGTH_SHORT).show();
        } else {
            int current = PreferenceManager.getInstance(getContext()).getInt(Constants.TIMELESS);
            PreferenceManager.getInstance(getContext()).put(Constants.TIMELESS, ballsLeft);
            mListener.onNewHighScore(timeLessMode);
            Log.i("SCORE", current + " " + ballsLeft);
            if (ballsLeft > current) {
                PreferenceManager.getInstance(getContext()).put(Constants.TIMELESS, ballsLeft);
                Toast.makeText(getActivity(), "New record updated with balls broken " + ballsLeft + "", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(getActivity(), "Missed record score by " + (current - ballsLeft) + " balls", Toast.LENGTH_SHORT).show();
        }
        getActivity().onBackPressed();
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
    public void onStart() {
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
    public void onStop() {
        super.onStop();
        if (!timeLessMode) {
            mStarted = false;
            mHandler.removeCallbacks(mRunnable);
            pausedT = System.currentTimeMillis();
            paused = true;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (Listener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
}
