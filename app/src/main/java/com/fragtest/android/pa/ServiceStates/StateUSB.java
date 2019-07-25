package com.fragtest.android.pa.ServiceStates;

import android.content.SharedPreferences;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.LogIHAB;

public class StateUSB implements ServiceState {

    ControlService mService;
    String LOG = "StateUSB";
    //private boolean mIsUSBPresent = false;

    public StateUSB(ControlService service) {
        this.mService = service;

    }

    @Override
    public void setInterface() {

        LogIHAB.log(LOG + ":setInterface()");

        if (mService.checkUSB()) {
            usbAttached();
        } else {
            usbDetached();
        }
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
        //mService.audioRecorder.close();
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
        mService.getMTaskHandler().postDelayed(mService.mDisableBT, mService.mDisableBTTime);
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
}
