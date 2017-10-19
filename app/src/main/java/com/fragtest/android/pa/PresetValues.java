package com.fragtest.android.pa;

import android.content.Context;

/**
 * Created by ul1021 on 18.10.2017.
 */

public class PresetValues {

    // preferences
    public static boolean isTimer, isWave, keepAudioCache, isLocked, filterHp, downsample,
            showConfigButton, showRecordingButton;
    public static int samplerate, chunklengthInS, filterHpFrequency, mFinalCountDown,
            mTimerInterval;

    private Context mContext;

    // Load preset values
    public PresetValues(Context context) {

        mContext = context;

        // preferences
        isTimer = mContext.getResources().getBoolean(R.bool.isTimer);
        isWave = mContext.getResources().getBoolean(R.bool.isWave);
        keepAudioCache = mContext.getResources().getBoolean(R.bool.keepAudioCache);
        isLocked = mContext.getResources().getBoolean(R.bool.isLocked);
        filterHp = mContext.getResources().getBoolean(R.bool.filterHp);
        downsample = mContext.getResources().getBoolean(R.bool.downsample);
        showConfigButton = mContext.getResources().getBoolean(R.bool.showConfigButton);
        showRecordingButton = mContext.getResources().getBoolean(R.bool.showRecordingButton);

        samplerate = R.integer.samplerate;
        chunklengthInS = R.integer.chunklengthInS;
        filterHpFrequency = R.integer.filterHpFrequency;

        mFinalCountDown = R.integer.mFinalCountDown;
        mTimerInterval = R.integer.mFinalCountDown;
    }
}
