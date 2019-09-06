package com.fragtest.android.pa.ServiceStates;

import android.content.SharedPreferences;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.util.Log;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.LogIHAB;

public class StateUSB implements ServiceState {

    ControlService mService;
    String LOG = "StateUSB";
    AudioManager mAudioManager;
    private boolean isBound = false;
    private AudioDeviceInfo mDevice;

    public StateUSB(ControlService service) {
        this.mService = service;

    }

    @Override
    public void setInterface() {

        LogIHAB.log(LOG + ":setInterface()");

        mAudioManager = (AudioManager) mService.getSystemService(mService.AUDIO_SERVICE);
        AudioDeviceInfo[] devices = mAudioManager.getDevices(android.media.AudioManager.GET_DEVICES_ALL);

        boolean found = false;
        for (AudioDeviceInfo device : devices) {
            Log.e(LOG, "Device found: " + device.getType() + " Source: " + device.isSource() + " Sink: " + device.isSink());
            // Device needs to be A2DP Profile and only provide audio output
            if (device.getType() == AudioDeviceInfo.TYPE_USB_DEVICE && device.isSource()) {
                mService.setPreferredAudioDevice(device);
                found = true;
            }
        }
        if (!found) {
            Log.e(LOG, "NO USB DEVICE WAS FOUND!");
        }

        if (found) {
            usbAttached();
        } else {
            usbDetached();
        }
    }

    @Override
    public void cleanUp() {
        Log.e(LOG, "CHANGE STATE");
        /** Cleanup **/
    }

    @Override
    public AudioDeviceInfo getPreferredDevice() {
        return mDevice;
    }

    @Override
    public void registerClient() {

        if (mService.checkUSB()) {
            usbAttached();
        } else {
            usbDetached();
        }
    }

    @Override
    public void unregisterClient() {

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

    }

    @Override
    public void chargingOn() {
        mService.stopRecording();
    }

    @Override
    public void chargingOnPre() {

    }

    @Override
    public void usbAttached() {
        LogIHAB.log(LOG + ":" + "usbAttached()");
        //mService.isUSBPresent = true;
        if (mService.getVibration() != null) {
            mService.getVibration().singleBurst();
        }
        //mService.announceUSBConnected();
        mService.messageClient(ControlService.MSG_USB_CONNECT);
        mService.startRecording();
    }

    @Override
    public void usbDetached() {
        LogIHAB.log(LOG + ":" + "usbDetached()");
        //mService.isUSBPresent = false;
        if (mService.getVibration() != null) {
            mService.getVibration().singleBurst();
        }
        //mService.announceUSBDisconnected();
        mService.messageClient(ControlService.MSG_USB_DISCONNECT);
        mService.stopRecording();
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
