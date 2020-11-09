package com.fragtest.android.pa.InputProfile;

import android.os.Handler;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.LogIHAB;

public class InputProfile_Blank implements InputProfile {

    private String LOG = "InputProfile_Blank";
    private ControlService mContext;
    private Handler mTaskHandler = new Handler();
    private int mWaitInterval = 100;
    private boolean mIsBound = false;

    public InputProfile_Blank(ControlService context) {
        this.mContext = context;
    }

    // This Runnable has the purpose of delaying/waiting until the application is ready again
    private Runnable mSetInterfaceRunnable = new Runnable() {
        @Override
        public void run() {
            if (!ControlService.getIsCharging() && mIsBound) {
                mContext.messageClient(ControlService.MSG_START_RECORDING);
                mTaskHandler.removeCallbacks(mSetInterfaceRunnable);
            } else {
                mTaskHandler.postDelayed(mSetInterfaceRunnable, mWaitInterval);
            }
        }
    };

    @Override
    public void setInterface() {

        LogIHAB.log(LOG);
        mTaskHandler.removeCallbacks(mSetInterfaceRunnable);
        mTaskHandler.postDelayed(mSetInterfaceRunnable, mWaitInterval);
    }

    @Override
    public void setState(String inputProfile) {

    }

    @Override
    public String getInputProfile() {
        return "";
    }

    @Override
    public void cleanUp() {
        mTaskHandler.removeCallbacks(mSetInterfaceRunnable);
        mContext.messageClient(ControlService.MSG_STOP_RECORDING);
        System.gc();
    }

    @Override
    public void setDevice(String sDeviceName) {}

    @Override
    public boolean getIsAudioRecorderClosed() {
        return true;
    }

    @Override
    public void registerClient() {
        mIsBound = true;

    }

    @Override
    public void unregisterClient() {
        mIsBound = false;
        mContext.messageClient(ControlService.MSG_STOP_RECORDING);
        cleanUp();
    }

    @Override
    public void applicationShutdown() {

    }

    @Override
    public void batteryCritical() {

    }

    @Override
    public void chargingOff() {

    }

    @Override
    public void chargingOn() {
        mContext.setChargingProfile();
    }

    @Override
    public void chargingOnPre() {

    }

    @Override
    public void onDestroy() {

    }
}
