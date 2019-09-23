package com.fragtest.android.pa.ServiceStates;

import android.content.SharedPreferences;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.util.Log;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.LogIHAB;

public class StateUSB implements ServiceState {

    private ControlService mService;
    private String LOG = "StateUSB";
    private AudioManager mAudioManager;
    private int mInterval = 200;
    private boolean isBound = false;
    private AudioDeviceInfo mDevice;

    private Runnable mFindDeviceRunnable = new Runnable() {
        @Override
        public void run() {
            if (isBound) {
                Log.e(LOG, "Client is bound.");

                try {
                    AudioDeviceInfo[] devices = mAudioManager.getDevices(android.media.AudioManager.GET_DEVICES_ALL);

                    mDevice = null;
                    for (AudioDeviceInfo device : devices) {
                        Log.e(LOG, "Device found: " + device.getType() + " Source: " + device.isSource() + " Sink: " + device.isSink());
                        // Device needs to be USB and provide audio input
                        if (device.getType() == AudioDeviceInfo.TYPE_USB_DEVICE && device.isSource()) {
                            Log.e(LOG, "This looks like a good device");
                            mDevice = device;
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mService.getMTaskHandler().removeCallbacks(mFindDeviceRunnable);
                mService.startRecording();
            } else {
                Log.e(LOG, "Client is not bound yet - short wait before retry.");
                mService.getMTaskHandler().postDelayed(mFindDeviceRunnable, mInterval);
            }
        }
    };

    public StateUSB(ControlService service) {
        this.mService = service;

    }

    @Override
    public void setInterface() {

        LogIHAB.log(LOG + ":setInterface()");
        Log.e(LOG, "SET INTERFACE");
        Log.e(LOG, "USB connected: " + mService.checkUSB());

        if (mService.checkUSB()) {
            mAudioManager = (AudioManager) mService.getSystemService(mService.AUDIO_SERVICE);
            AudioDeviceInfo[] devices = mAudioManager.getDevices(android.media.AudioManager.GET_DEVICES_ALL);

            boolean found = false;
            mDevice = null;
            for (AudioDeviceInfo device : devices) {
                Log.e(LOG, "Device found: " + device.getType() + " Source: " + device.isSource() + " Sink: " + device.isSink());
                // Device needs to be A2DP Profile and only provide audio output
                if (device.getType() == AudioDeviceInfo.TYPE_USB_DEVICE && device.isSource()) {
                    Log.e(LOG, "THis looks like a good device.");
                    found = true;
                    mDevice = device;
                }
            }
            if (!found) {
                Log.e(LOG, "NO USB DEVICE WAS FOUND!");
            }

            if (found) {
                //usbAttached();
                mService.getMTaskHandler().removeCallbacks(mFindDeviceRunnable);
                mService.startRecording();
            } else {
                mService.getMTaskHandler().postDelayed(mFindDeviceRunnable, mInterval);
            }
        }
    }

    @Override
    public void cleanUp() {
        Log.e(LOG, "CHANGE STATE");
        /** Cleanup **/
        mService.stopRecording();
        mService.shutdownAudioRecorder();
        mService.getMTaskHandler().removeCallbacks(mFindDeviceRunnable);
        mService.stopAlarmAndCountdown();
    }

    @Override
    public AudioDeviceInfo getPreferredDevice() {
        return mDevice;
    }

    @Override
    public void registerClient() {
        isBound = true;
        if (mService.checkUSB()) {
            usbAttached();
        } else {
            usbDetached();
        }
    }

    @Override
    public void unregisterClient() {
        isBound = false;
        mService.stopRecording();
        mService.getMTaskHandler().removeCallbacks(mFindDeviceRunnable);
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

    }

    @Override
    public void chunkRecorded() {

    }

    @Override
    public void chunkProcessed() {

    }

    @Override
    public void applicationShutdown() {

    }

    @Override
    public void batteryLevelInfo() {

    }

    @Override
    public void batteryCritical() {

    }

    @Override
    public void chargingOff() {
        setInterface();
    }

    @Override
    public void chargingOn() {
        cleanUp();
    }

    @Override
    public void chargingOnPre() {
        cleanUp();
    }

    @Override
    public void usbAttached() {
        LogIHAB.log(LOG + ":" + "usbAttached()");
        if (mService.getVibration() != null) {
            mService.getVibration().singleBurst();
        }
        mService.messageClient(ControlService.MSG_USB_CONNECT);
        setInterface();
    }

    @Override
    public void usbDetached() {
        LogIHAB.log(LOG + ":" + "usbDetached()");
        if (mService.getVibration() != null) {
            mService.getVibration().singleBurst();
        }
        cleanUp();
    }

    @Override
    public void bluetoothReceived(String action, SharedPreferences sharedPreferences) {

    }

    @Override
    public void onDestroy() {
        mService.stopAlarmAndCountdown();
    }

    @Override
    public void bluetoothConnected() {

    }

    @Override
    public void bluetoothDisconnected() {

    }

    @Override
    public void bluetoothSwitchedOff() {

    }

    @Override
    public void bluetoothSwitchedOn() {

    }
}
