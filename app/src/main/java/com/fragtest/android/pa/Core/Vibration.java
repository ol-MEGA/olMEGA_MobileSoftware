package com.fragtest.android.pa.Core;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by ul1021 on 13.07.2017.
 */

public class Vibration {

    private final static String LOG = "Vibration";
    private Context mContext;
    // Interval between bursts of vibration when reminder alarm is set off
    private static int mVibrationInterval_s = 1;
    private static int mVibrationDuration_ms = 200;
    private static long mLengthWakeLock_ms = 2000;
    private final Handler mTimerHandler = new Handler();
    private boolean isActive = false;
    private Vibrator mVibrator;

    private final Runnable loop = new Runnable() {
        @Override
        public void run() {
            if (isActive) {
                mVibrator.vibrate(mVibrationDuration_ms);
                Log.e(LOG, "Ring.");
                mTimerHandler.postDelayed(this, mVibrationInterval_s*1000);
            }
        }
    };

    public Vibration(Context context) {
        mContext = context;
        mVibrator = ((Vibrator) mContext.getSystemService(VIBRATOR_SERVICE));
    }

    public void singleBurst() {
        mVibrator.vibrate(mVibrationDuration_ms);
    }

    public void repeatingBurstOn() {
        Log.e(LOG,"rrringgrrrrinngggg!");

        if (!isActive) { // ensure that only one alarm is annoying us at any given time
            mTimerHandler.post(loop);
            PowerManager pm = (PowerManager) mContext.getSystemService(
                    Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                    PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
            wakeLock.acquire(mLengthWakeLock_ms);
        }

        isActive = true;

    }

    public void repeatingBurstOff() {

        mTimerHandler.removeCallbacks(loop);
        KeyguardManager keyguardManager = (KeyguardManager) mContext.getSystemService(
                Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
        keyguardLock.disableKeyguard();

        isActive = false;
    }
}
