package com.fragtest.android.pa.ServiceStates;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
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
    private AudioManager mAudioManager;
    private int mIntervalRecordingCheck = 200;
    private boolean isBound = false;
    private AudioDeviceInfo mDevice;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public ConnectedThread mConnectedThread = null;
    private String mDeviceAdress;
    private String chunklengthInS;
    private boolean isWave;
    private BluetoothSocket mSocket;

    private Runnable mRecordingRunnable = new Runnable() {
        @Override
        public void run() {
            if (isBound) {
                Log.e(LOG, "Client is bound.");

                mService.getBluetoothAdapter().startDiscovery();

                Set<BluetoothDevice> btdevices = mService.getBluetoothAdapter().getBondedDevices();
                for (BluetoothDevice device : btdevices) {
                    Log.e(LOG, "BT Device: " + device.getType() + " Bonded: " + device.getBondState() + " Class: " + device.getBluetoothClass().getDeviceClass());

                    if (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET
                            && device.getBondState() == BluetoothDevice.BOND_BONDED
                            && device.getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC) {

                        mService.getBluetoothAdapter().cancelDiscovery();

                        Log.e(LOG, "Connecting to device: " + device);
                        try {
                            mSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                            Log.e(LOG, "Socket created: " + mSocket);
                        } catch (IOException e) {
                            Log.e(LOG, "Socket NOT created.");
                        }

                        try {
                            Log.e(LOG, "Will it connect?");
                            // This is a blocking call and will only return on a successful connection or an exception
                            if (!mSocket.isConnected()) {
                                mSocket.connect();
                                Log.e(LOG, "Connecting");
                            } else {
                                Log.e(LOG, "Already connected.");
                            }
                        } catch (IOException e) {
                            Log.e(LOG, "Connection failed.");
                        }

                        Log.e(LOG, "Connection routine finished.");
                        mService.getMTaskHandler().removeCallbacks(mRecordingRunnable);

                    }
                    break;
                }

            } else {
                Log.e(LOG, "Client is not bound yet - 1 second wait.");
                //mService.getMTaskHandler().postDelayed(mRecordingRunnable, mIntervalRecordingCheck);
            }
        }
    };

    public StateRFCOMM(ControlService service) {
        this.mService = service;
    }

    @Override
    public void setInterface() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.mService);
        chunklengthInS = sharedPreferences.getString("chunklengthInS", "60");
        isWave = sharedPreferences.getBoolean("isWave", true);

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
    public void changeState() {
        Log.e(LOG, "CHANGE STATE");
        /** Cleanup **/
        mService.getMTaskHandler().removeCallbacks(mRecordingRunnable);
        mConnectedThread.stopRecording();
        mConnectedThread = null;
        mAudioManager = null;
        try {
            mSocket.close();
        } catch (Exception e) {
        }
        mSocket = null;
        mDevice = null;
        mDeviceAdress = null;
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
        //mService.stopRecording();
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
        mConnectedThread.stopRecording();
    }

    @Override
    public void batteryLevelInfo() {
    }

    @Override
    public void batteryCritical() {
        if (ControlService.getIsRecording()) {
            mConnectedThread.stopRecording();
        }
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
        if (ControlService.getIsRecording()) {
            mConnectedThread.stopRecording();
        }
        mService.getMTaskHandler().removeCallbacks(mRecordingRunnable);
        mService.getVibration().singleBurst();
        isBound = false;
    }

    @Override
    public void chargingOnPre() {
        isBound = false;
        //mService.stopRecording();
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

                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:

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

        if (!ControlService.getIsRecording()) {
            Log.e(LOG, "New thread.");
            mConnectedThread = new ConnectedThread(mSocket, mService.getServiceMessenger(),
                    Integer.parseInt(chunklengthInS), isWave);

            Log.e(LOG, "Max Priority");
            mConnectedThread.setPriority(Thread.MAX_PRIORITY);
            Log.e(LOG, "Now starting.");
            mConnectedThread.start();
        }


    }

    @Override
    public void bluetoothDisconnected() {

        Log.e(LOG, "BT DISCONNECTED");

        if (mConnectedThread != null) {
            mConnectedThread.stopRecording();
        }

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
        if (mConnectedThread != null) {
            mConnectedThread.stopRecording();
        }
        mService.getBluetoothAdapter().enable();
    }

    @Override
    public void bluetoothSwitchedOn() {

        Log.e(LOG, "BT Switched on.");

        mService.getVibration().singleBurst();
    }
}
