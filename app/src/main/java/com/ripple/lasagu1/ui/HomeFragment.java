package com.ripple.lasagu1.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;
import com.ripple.lasagu1.R;
import com.ripple.lasagu1.util.Constants;

/**
 * Created by royce on 23-05-2016.
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    public interface Listener {
        void onStartGameRequested(int mode);

        void onShowAchievementsRequested();

        void onShowLeaderboardsRequested(int mode);

        void onSignInButtonClicked();

        void onSignOutButtonClicked();
    }

    Listener mListener = null;
    TextView mSignOut;
    SignInButton mSignInButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        final int[] clickable = new int[]{
                R.id.sign_in, R.id.sign_out,
                R.id.ach, R.id.timed, R.id.timeless,
                R.id.play_timed, R.id.play_timeless
        };
        for (int i : clickable) {
            v.findViewById(i).setOnClickListener(this);
        }
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSignInButton = (SignInButton) view.findViewById(R.id.sign_in);
        mSignOut = (TextView) view.findViewById(R.id.sign_out);
    }

    @Override
    public void onStart() {
        super.onStart();
        // updateUi();
    }

    void updateUi(boolean showSignIn) {

        if (showSignIn) {
            mSignInButton.setVisibility(View.VISIBLE);
            mSignOut.setVisibility(View.GONE);
        } else {
            mSignInButton.setVisibility(View.GONE);
            mSignOut.setVisibility(View.VISIBLE);
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

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.sign_in:
                mListener.onSignInButtonClicked();
                break;
            case R.id.sign_out:
                mListener.onSignOutButtonClicked();
                break;
            case R.id.ach:
                mListener.onShowAchievementsRequested();
                break;
            case R.id.timed:
                mListener.onShowLeaderboardsRequested(Constants.MODE_TIMED);
                break;
            case R.id.timeless:
                mListener.onShowLeaderboardsRequested(Constants.MODE_TIMELESS);
                break;
            case R.id.play_timed:
                mListener.onStartGameRequested(Constants.MODE_TIMED);
                break;
            case R.id.play_timeless:
                mListener.onStartGameRequested(Constants.MODE_TIMELESS);
                break;

        }
    }
}
