package com.fragtest.android.pa.ServiceStates;

import android.content.SharedPreferences;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.LogIHAB;

public class StateSTANDALONE implements ServiceState {

    ControlService mService;
    String LOG = "StateSTANDALONE";

    public StateSTANDALONE(ControlService service) {
        this.mService = service;

    }

    @Override
    public void setInterface() {
        LogIHAB.log(LOG + ":setInterface()");

        if (mService.getIsRecording()) {
            LogIHAB.log(LOG + ": stopping Recording");
            mService.stopRecording();
        }

    }

    @Override
    public void registerClient() {

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
        mService.getMTaskHandler().postDelayed(mService.mDisableBT, mService.mDisableBTTime);
    }

    @Override
    public void chargingOnPre() {

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

    }

    @Override
    public void onDestroy() {
       mService.stopAlarmAndCountdown();
    }
}
