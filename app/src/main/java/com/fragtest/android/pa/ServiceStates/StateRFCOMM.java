package com.fragtest.android.pa.ServiceStates;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.LogIHAB;

import java.util.Set;

public class StateRFCOMM implements ServiceState {

    ControlService mService;
    String LOG = "StateR2DP";

    public StateRFCOMM(ControlService service) {
        this.mService = service;

    }

    @Override
    public void setInterface() {

        LogIHAB.log(LOG + ":setInterface()");
        Log.e(LOG, "INTERNATL MODE: " + LOG);
    }

    @Override
    public void registerClient() {
        if (!mService.getIsCharging()) {
            if (mService.getBluetoothAdapter() != null) {
                if (!mService.getIsBluetoothAdapterEnabled()) {
                    mService.getBluetoothAdapter().enable();
                }
                mService.connectBtDevice();
            }
        } else {
            mService.getVibration().singleBurst();
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
        mService.mConnectedThread.stopRecording();
    }

    @Override
    public void chunkRecorded() {

    }

    @Override
    public void chunkProcessed() {

    }

    @Override
    public void applicationShutdown() {
        mService.stopRecordingRFCOMM();
    }

    @Override
    public void batteryLevelInfo() {

    }

    @Override
    public void batteryCritical() {
        mService.stopRecordingRFCOMM();
    }

    @Override
    public void chargingOff() {
        mService.getMTaskHandler().removeCallbacks(mService.mDisableBT);
        mService.connectBtDevice();
    }

    @Override
    public void chargingOn() {
        if (mService.mConnectedThread != null) {
            mService.stopRecordingRFCOMM();
        }
    }

    @Override
    public void chargingOnPre() {
        if (mService.mConnectedThread != null) {
            mService.stopRecordingRFCOMM();
        }
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

        switch (action) {
            case BluetoothDevice.ACTION_ACL_CONNECTED:
                // Save list of paired devices
                Set<BluetoothDevice> pairedDevices = mService.getBluetoothAdapter().getBondedDevices();
                for (BluetoothDevice bt : pairedDevices) {
                    sharedPreferences.edit().putString("BTDevice", bt.getAddress()).apply();
                    Log.i(LOG, "CONNECTED TO: " + bt.getAddress());
                }
                mService.announceBTConnected();
                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                mService.announceBTDisconnected();
                LogIHAB.log("Bluetooth: disconnected");
                break;
        }
    }

    @Override
    public void onDestroy() {
        mService.stopAlarmAndCountdown();
        mService.stopRecordingRFCOMM();
    }
}
