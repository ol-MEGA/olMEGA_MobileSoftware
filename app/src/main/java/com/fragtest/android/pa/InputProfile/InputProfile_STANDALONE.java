package com.fragtest.android.pa.InputProfile;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Messenger;
import android.util.Log;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.LogIHAB;

public class InputProfile_STANDALONE implements InputProfile {

    private final String INPUT_PROFILE = "STANDALONE";
    private ControlService mContext;
    private Messenger mServiceMessenger;
    private String LOG = "InputProfile_STANDALONE";
    private Handler mTaskHandler = new Handler();
    private int mWaitInterval = 100;
    private boolean mIsBound = false;

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

    public InputProfile_STANDALONE(ControlService context, Messenger serviceMessenger) {
        this.mContext = context;
        this.mServiceMessenger = serviceMessenger;
    }

    @Override
    public String getInputProfile() {
        return this.INPUT_PROFILE;
    }

    @Override
    public void setState(String inputProfile) {

    }

    @Override
    public void setInterface() {

        LogIHAB.log(LOG);

        mTaskHandler.removeCallbacks(mSetInterfaceRunnable);
        mTaskHandler.postDelayed(mSetInterfaceRunnable, mWaitInterval);
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
        Log.e(LOG, "Client Registered");
        mIsBound = true;
    }

    @Override
    public void unregisterClient() {
        Log.e(LOG, "Client Unregistered");
        mIsBound = false;
        mContext.messageClient(ControlService.MSG_STOP_RECORDING);
        cleanUp();
    }

    @Override
    public void batteryCritical() {
        cleanUp();
    }

    @Override
    public void chargingOff() {
        Log.e(LOG, "CharginOff");
        //mTaskHandler.postDelayed(mSetInterfaceRunnable, mWaitInterval);
    }

    @Override
    public void chargingOn() {
        Log.e(LOG, "CharginOn");
        //mTaskHandler.removeCallbacks(mSetInterfaceRunnable);
        //mContext.messageClient(ControlService.MSG_STOP_RECORDING);
        mContext.setChargingProfile();
    }

    @Override
    public void chargingOnPre() {
        Log.e(LOG, "CharginOnPre");
    }

    @Override
    public void onDestroy() {
        cleanUp();
    }


    @Override
    public void applicationShutdown() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.disable();
    }
}
