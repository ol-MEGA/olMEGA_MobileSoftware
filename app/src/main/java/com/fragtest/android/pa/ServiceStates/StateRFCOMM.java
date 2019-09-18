package com.fragtest.android.pa.ServiceStates;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;
import android.media.AudioDeviceInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.ConnectedThread;
import com.fragtest.android.pa.Core.LogIHAB;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;


public class StateRFCOMM implements ServiceState {

    private ControlService mService;
    private String LOG = "StateRFCOMM";
    private int mIntervalConnectionCheck = 500;
    private boolean isBound = false;
    private boolean isCharging = false;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public ConnectedThread mConnectedThread = null;
    private String chunklengthInS;
    private boolean isWave;
    private BluetoothSocket mSocket = null;
    private BluetoothDevice mBluetoothDevice;

    private Runnable mConnectionRunnable = new Runnable() {
        @Override
        public void run() {
            if (isBound && !isCharging) {
                Log.e(LOG, "Client is bound.");

                Set<BluetoothDevice> btdevices = mService.getBluetoothAdapter().getBondedDevices();
                for (BluetoothDevice device : btdevices) {
                    Log.e(LOG, "BT Device: " + device.getType() + " Bonded: " + device.getBondState() + " Class: " + device.getBluetoothClass().getDeviceClass());

                    if (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET
                            && device.getBondState() == BluetoothDevice.BOND_BONDED
                            && device.getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC) {

                        mBluetoothDevice = device;
                        mBluetoothDevice.createBond();
                        mService.getBluetoothAdapter().cancelDiscovery();

                        Log.e(LOG, "NULLING SOCKET");

                        mSocket = null;
                        Log.e(LOG, "Connecting to device: " + mBluetoothDevice);
                        try {

                            mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
                            Log.e(LOG, "Socket created: " + mSocket);
                        } catch (IOException e) {
                            Log.e(LOG, "Socket NOT created.");
                        }

                        try {
                            Log.e(LOG, "Will it connect?");
                            // This is a blocking call and will only return on a successful connection or an exception
                            if (!mSocket.isConnected()) {
                                mSocket.connect();


                                Log.e(LOG, "Connected: " + mSocket.isConnected());
                            } else {
                                Log.e(LOG, "Already connected.");
                            }
                        } catch (IOException e) {
                            Log.e(LOG, "Connection failed.");
                        }

                        Log.e(LOG, "Connection routine finished.");

                        if (mSocket.isConnected()) {
                            mService.getMTaskHandler().removeCallbacks(mConnectionRunnable);
                        } else {
                            Log.e(LOG, "Retrying connection");
                            mService.getMTaskHandler().postDelayed(mConnectionRunnable, mIntervalConnectionCheck);
                        }

                        break;
                    }

                }

            } else {
                Log.e(LOG, "Client is not bound yet - 1 second wait.");
                mService.getMTaskHandler().postDelayed(mConnectionRunnable, mIntervalConnectionCheck);
            }
        }
    };

    public StateRFCOMM(ControlService service) {
        this.mService = service;
    }

    @Override
    public void setInterface() {

        mService.getBluetoothAdapter().startDiscovery();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.mService);
        chunklengthInS = sharedPreferences.getString("chunklengthInS", "60");
        isWave = sharedPreferences.getBoolean("isWave", true);

        LogIHAB.log(LOG + ":setInterface()");
        Log.e(LOG, "INTERNAL MODE: " + LOG);

        if (!mService.getBluetoothAdapter().isEnabled()) {
            mService.getBluetoothAdapter().enable();
        }

        if (!isCharging) {
            mService.getMTaskHandler().postDelayed(mConnectionRunnable, mIntervalConnectionCheck);
        }
    }

    @Override
    public void cleanUp() {
        Log.e(LOG, "CHANGE STATE");
        /** Cleanup **/
        mService.getMTaskHandler().removeCallbacks(mConnectionRunnable);
        if (mConnectedThread != null) {
            mConnectedThread.stopRecording();
            mConnectedThread.interrupt();
            //mConnectedThread.cancel();
            mConnectedThread = null;
        } else {
            Log.e(LOG, "MCONNECTEDTHREAD IS NULL!");
        }
        mSocket = null;
    }

    @Override
    public AudioDeviceInfo getPreferredDevice() {
        return null;
    }

    @Override
    public void registerClient() {
        isBound = true;
    }

    @Override
    public void unregisterClient() {
        isBound = false;
        cleanUp();
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
        cleanUp();
    }

    @Override
    public void batteryLevelInfo() {
    }

    @Override
    public void batteryCritical() {
        if (ControlService.getIsRecording()) {
            mConnectedThread.stopRecording();
        }
        mService.getMTaskHandler().removeCallbacks(mConnectionRunnable);
        mService.getVibration().singleBurst();
        //isBound = false;
    }

    @Override
    public void chargingOff() {
        Log.e(LOG, "ChargingOff");
        //isBound = true;
        //mService.getMTaskHandler().postDelayed(mConnectionRunnable, mIntervalConnectionCheck);
        isCharging = false;
        setInterface();
    }

    @Override
    public void chargingOn() {
        Log.e(LOG, "ChargingOn");
        //if (ControlService.getIsRecording()) {
        //    mConnectedThread.stopRecording();
        //}
        //mService.getMTaskHandler().removeCallbacks(mConnectionRunnable);
        //mService.getVibration().singleBurst();
        //isBound = false;
        isCharging = true;
        cleanUp();
    }

    @Override
    public void chargingOnPre() {
        Log.e(LOG, "ChargingOnPre");
        //isBound = false;
        //mService.stopRecording();
        isCharging = true;
        //mService.getMTaskHandler().removeCallbacks(mConnectionRunnable);
        cleanUp();
    }

    @Override
    public void usbAttached() {
        Log.e(LOG, "USB Attached");
        LogIHAB.log(LOG + ":" + "usbAttached()");
        isCharging = true;
        cleanUp();
    }

    @Override
    public void usbDetached() {
        Log.e(LOG, "USB Detached");
        LogIHAB.log(LOG + ":" + "usbDetached()");
        isCharging = false;
        setInterface();
    }

    @Override
    public void bluetoothReceived(String action, SharedPreferences sharedPreferences) {

        Log.e(LOG, "BT Message: " + action);

        switch (action) {
            case BluetoothDevice.ACTION_ACL_CONNECTED:

                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:

                break;
        }
    }

    @Override
    public void onDestroy() {
        mService.getMTaskHandler().removeCallbacks(mConnectionRunnable);
        mService.stopAlarmAndCountdown();
        cleanUp();
    }

    @Override
    public void bluetoothConnected() {

        Log.e(LOG, "BT CONNECTED");
        //isBound = true;

        if (!ControlService.getIsRecording()) {
            try {
                mConnectedThread = null;
                Log.e(LOG, "New thread.");
                mConnectedThread = new ConnectedThread(mSocket, mService.getServiceMessenger(),
                        Integer.parseInt(chunklengthInS), isWave);

                Log.e(LOG, "Max Priority");
                mConnectedThread.setPriority(Thread.MAX_PRIORITY);
                Log.e(LOG, "Now starting.");
                mConnectedThread.start();
                mService.getVibration().singleBurst();

            } catch (Exception e) {
                Log.e(LOG, "ConnectedThread creation failed.");
                mConnectedThread = null;
            }
        }
    }

    @Override
    public void bluetoothDisconnected() {

        Log.e(LOG, "BT DISCONNECTED");

        if (mSocket == null) {
            Log.e(LOG, "MSOCKET IS NULL");
        } else {

        }

        if (mConnectedThread != null) {
            try {
                mConnectedThread.stopRecording();
                mConnectedThread.cancel();
                mConnectedThread = null;
            } catch (Exception e) {
                Log.e(LOG, "ConnectedThread cancellation failed.");
            }
        }

        //mService.getMTaskHandler().removeCallbacks(mConnectionRunnable);
        mService.getVibration().singleBurst();
        //isBound = false;

        //if (!mService.getBluetoothAdapter().isEnabled()) {
        //    mService.getBluetoothAdapter().enable();
        //}
        setInterface();
        //mService.getMTaskHandler().postDelayed(mConnectionRunnable, mIntervalConnectionCheck);
    }

    @Override
    public void bluetoothSwitchedOff() {

        Log.e(LOG, "BT Switched off.");

        /*mService.getVibration().singleBurst();
        if (mConnectedThread != null) {
            mConnectedThread.stopRecording();
            mConnectedThread.cancel();
        }*/
        mService.getBluetoothAdapter().enable();
    }

    @Override
    public void bluetoothSwitchedOn() {

        Log.e(LOG, "BT Switched on.");

        mService.getVibration().singleBurst();
    }
}
