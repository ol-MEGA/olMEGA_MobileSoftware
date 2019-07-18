package com.fragtest.android.pa.ServiceStates;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.LogIHAB;
import com.fragtest.android.pa.PreferencesActivity;

import java.util.Set;

public class StateA2DP implements ServiceState {

    ControlService mService;
    String LOG = "StateA2DP";

    public StateA2DP(ControlService service) {
        this.mService = service;

    }

    @Override
    public void setInterface() {

        LogIHAB.log(LOG + ":setInterface()");
        Log.e(LOG, "INTERNATL MODE: " + LOG);
    }

    @Override
    public void registerClient() {
        mService.disableBluetoothAdapter();
        mService.messageClient(ControlService.MSG_BT_DISCONNECTED);

        if (!mService.getIsBluetoothAdapterEnabled()) {
            mService.enableBluetoothAdapter();

            SharedPreferences sharedPreferences = mService.getSharedPreferences();

            if (sharedPreferences.contains("BTDevice")) {
                String btdevice = sharedPreferences.getString("BTDevice", null);
                BluetoothDevice device = mService.getBluetoothAdapter().getRemoteDevice(btdevice);
                device.createBond();
                LogIHAB.log("Connecting to device: " + device.getAddress());
            }
        }
        if (mService.getIsCharging()) {
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
        mService.audioRecorder.close();
    }

    @Override
    public void chunkRecorded() {

    }

    @Override
    public void chunkProcessed() {

    }

    @Override
    public void applicationShutdown() {
        mService.disableBluetoothAdapter();
    }

    @Override
    public void batteryLevelInfo() {

    }

    @Override
    public void batteryCritical() {
        mService.disableBluetoothAdapter();
    }

    @Override
    public void chargingOff() {
        mService.getMTaskHandler().removeCallbacks(mService.mDisableBT);
        if (!mService.getIsBluetoothAdapterEnabled()) {
            mService.enableBluetoothAdapter();
        }
    }

    @Override
    public void chargingOn() {
        if (mService.getIsBluetoothAdapterEnabled()) {
            mService.disableBluetoothAdapter();
        }
        mService.stopRecording();
        mService.getMTaskHandler().postDelayed(mService.mDisableBT, mService.mDisableBTTime);
    }

    @Override
    public void chargingOnPre() {
        if (mService.getIsBluetoothAdapterEnabled()) {
            mService.disableBluetoothAdapter();
        }
        mService.stopRecording();
        mService.getMTaskHandler().postDelayed(mService.mDisableBT, mService.mDisableBTTime);
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
        mService.disableBluetoothAdapter();
    }
}
