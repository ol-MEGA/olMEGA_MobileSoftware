package com.fragtest.android.pa.ServiceStates;

import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.util.Log;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.LogIHAB;


public class StateA2DP implements ServiceState {

    private ControlService mService;
    private String LOG = "StateA2DP";
    private AudioManager mAudioManager;
    private int mIntervalRecordingCheck = 200;
    private boolean isBound = false;
    private AudioDeviceInfo mDevice;

    private Runnable mRecordingRunnable = new Runnable() {
        @Override
        public void run() {
            if (isBound) {
                Log.e(LOG, "Client is bound.");

                try {
                    AudioDeviceInfo[] devices = mAudioManager.getDevices(android.media.AudioManager.GET_DEVICES_ALL);

                    boolean found = false;
                    for (AudioDeviceInfo device : devices) {
                        Log.e(LOG, "Device found: " + device.getType() + " Source: " + device.isSource() + " Sink: " + device.isSink());
                        // Device needs to be A2DP Profile and only provide audio output
                        if (device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP && device.isSource() && !device.isSink()) {
                            mDevice = device;
                            found = true;
                        }
                    }
                    if (!found) {
                        if (mService.getBluetoothAdapter().isDiscovering()) {
                            mService.getBluetoothAdapter().cancelDiscovery();
                        }
                        mService.messageClient(ControlService.MSG_BT_DISCONNECTED);
                        //mService.getBluetoothAdapter().startDiscovery();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mService.getMTaskHandler().removeCallbacks(mRecordingRunnable);
                //mService.setPreferredAudioDevice(mDevice);
                mService.startRecording();
            } else {
                Log.e(LOG, "Client is not bound yet - 1 second wait.");
                mService.getMTaskHandler().postDelayed(mRecordingRunnable, mIntervalRecordingCheck);
            }
        }
    };

    public StateA2DP(ControlService service) {
        this.mService = service;
    }

    @Override
    public void setInterface() {

        LogIHAB.log(LOG + ":setInterface()");
        Log.e(LOG, "INTERNAL MODE: " + LOG);

        if (!mService.getBluetoothAdapter().isEnabled()) {
            mService.getBluetoothAdapter().enable();
        }

        mAudioManager = (AudioManager) mService.getSystemService(ControlService.AUDIO_SERVICE);

        if (!ControlService.getIsCharging()) {
            mService.getMTaskHandler().post(mRecordingRunnable);
        }
    }

    @Override
    public void cleanUp() {
        Log.e(LOG, "CHANGE STATE");
        /** Cleanup **/
        mService.stopRecording();
        mService.shutdownAudioRecorder();
        mService.getMTaskHandler().removeCallbacks(mRecordingRunnable);
    }

    @Override
    public AudioDeviceInfo getPreferredDevice() {
        return mDevice;
    }

    @Override
    public void registerClient() {
        isBound = true;
    }

    @Override
    public void unregisterClient() {
        isBound = false;
        mService.stopRecording();
        mService.getMTaskHandler().removeCallbacks(mRecordingRunnable);
    }

    @Override
    public void getStatus() {
    }

    @Override
    public void resetBT() {
    }

    @Override
    public void startCountdown() {
    }

    @Override
    public void stopCountdown() {
    }

    @Override
    public void manualQuestionnaire() {
    }

    @Override
    public void propositionAccepted() {
    }

    @Override
    public void questionnaireFinished() {
    }

    @Override
    public void isMenu() {
    }

    @Override
    public void questionnaireActive() {
    }

    @Override
    public void checkForPreferences() {
    }

    @Override
    public void recordingStopped() {
        Log.e(LOG, "Recording was stopped.");
    }

    @Override
    public void chunkRecorded() {
    }

    @Override
    public void chunkProcessed() {
    }

    @Override
    public void applicationShutdown() {
        mService.stopRecording();
    }

    @Override
    public void batteryLevelInfo() {
    }

    @Override
    public void batteryCritical() {
        mService.stopRecording();
        mService.getMTaskHandler().removeCallbacks(mRecordingRunnable);
        mService.getVibration().singleBurst();
        isBound = false;
    }

    @Override
    public void chargingOff() {
        isBound = true;
        mService.getMTaskHandler().postDelayed(mRecordingRunnable, mIntervalRecordingCheck);
    }

    @Override
    public void chargingOn() {
        mService.stopRecording();
        mService.getMTaskHandler().removeCallbacks(mRecordingRunnable);
        mService.getVibration().singleBurst();
        isBound = false;
    }

    @Override
    public void chargingOnPre() {
        mService.stopRecording();
        mService.getMTaskHandler().removeCallbacks(mRecordingRunnable);
    }

    @Override
    public void usbAttached() {
        LogIHAB.log(LOG + ":" + "usbAttached()");
    }

    @Override
    public void usbDetached() {
        LogIHAB.log(LOG + ":" + "usbDetached()");
    }

    @Override
    public void bluetoothReceived(String action, SharedPreferences sharedPreferences) {

        Log.e(LOG, "BT Message: " + action);

        switch (action) {
            case BluetoothDevice.ACTION_ACL_CONNECTED:

                //Log.e(LOG, "BT CONNECTED");
                //if (!ControlService.getIsRecording()) {
                //    mService.getMTaskHandler().post(mRecordingRunnable);
                //}


                //mService.startRecording();
                //mService.getVibration().singleBurst();
                //mService.messageClient(ControlService.MSG_BT_CONNECTED);
                // Save list of paired devices
                /*Set<BluetoothDevice> pairedDevices = mService.getBluetoothAdapter().getBondedDevices();
                for (BluetoothDevice bt : pairedDevices) {
                    sharedPreferences.edit().putString("BTDevice", bt.getAddress()).apply();
                    Log.i(LOG, "CONNECTED TO: " + bt.getAddress());
                }
                //mService.announceBTConnected();
                Log.e(LOG, "BTDEVICES CONNECTED!");
                mService.startRecording();
                mService.setIsBTPresent(true);
*/
                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:

                //Log.e(LOG, "BT DISCONNECTED");
                //mService.stopRecording();
                //mService.getMTaskHandler().removeCallbacks(mRecordingRunnable);
                //mService.getVibration().singleBurst();

                //if (!mService.getBluetoothAdapter().isEnabled()) {
                //    mService.getBluetoothAdapter().enable();
                //}

/*
                mService.stopRecording();
                mService.setIsBTPresent(false);
                mService.getVibration().singleBurst();
                //mService.announceBTDisconnected();
                LogIHAB.log("Bluetooth: disconnected");
                */
                break;
        }


    }

    @Override
    public void onDestroy() {
        mService.getMTaskHandler().removeCallbacks(mRecordingRunnable);
        mService.stopAlarmAndCountdown();
    }

    @Override
    public void bluetoothConnected() {

        Log.e(LOG, "BT CONNECTED");
        isBound = true;
        mService.getMTaskHandler().postDelayed(mRecordingRunnable, mIntervalRecordingCheck);

    }

    @Override
    public void bluetoothDisconnected() {

        Log.e(LOG, "BT DISCONNECTED");

        mService.stopRecording();
        mService.getMTaskHandler().removeCallbacks(mRecordingRunnable);
        mService.getVibration().singleBurst();
        isBound = false;

        if (!mService.getBluetoothAdapter().isEnabled()) {
            mService.getBluetoothAdapter().enable();
        }
    }

    @Override
    public void bluetoothSwitchedOff() {

        Log.e(LOG, "BT Switched off.");

        mService.getVibration().singleBurst();
        mService.stopRecording();
        mService.getBluetoothAdapter().enable();
    }

    @Override
    public void bluetoothSwitchedOn() {

        Log.e(LOG, "BT Switched on.");

        mService.getVibration().singleBurst();
        //mService.getMTaskHandler().postDelayed(mRecordingRunnable, mIntervalRecordingCheck);
    }
}
