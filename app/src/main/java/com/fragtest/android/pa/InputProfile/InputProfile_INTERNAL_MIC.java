package com.fragtest.android.pa.InputProfile;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.os.Handler;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fragtest.android.pa.AudioRecorder;
import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.AudioFileIO;
import com.fragtest.android.pa.Core.LogIHAB;

public class InputProfile_INTERNAL_MIC implements InputProfile {

    private final String INPUT_PROFILE = "INTERNALMIC";
    private BluetoothAdapter mBluetoothAdapter;
    private ControlService mContext;
    private AudioRecorder mAudioRecorder;
    private Messenger mServiceMessenger;
    private String LOG = "InputProfile_INTERNALMIC";
    private SharedPreferences mSharedPreferences;
    private Handler mTaskHandler = new Handler();
    private int mWaitInterval = 500;
    private boolean mIsBound = false;
    // This Runnable has the purpose of delaying a release of AudioRecorder to avoid null pointer
    private Runnable mAudioReleaseRunnable = new Runnable() {
        @Override
        public void run() {
            if (mAudioRecorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                mAudioRecorder.release();
                mAudioRecorder = null;
                mTaskHandler.removeCallbacks(mAudioReleaseRunnable);
            } else {
                mTaskHandler.postDelayed(mAudioReleaseRunnable, mWaitInterval);
            }
        }
    };
    // This Runnable scans for available audio devices and acts if an A2DP device is present
    private Runnable mFindDeviceRunnable = new Runnable() {
        @Override
        public void run() {

            if (mIsBound && !ControlService.getIsCharging()) {
                Log.e(LOG, "Looking for device.");

                AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

                AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);
                boolean found = false;
                for (AudioDeviceInfo device : devices) {
                    Log.e(LOG, "Device: " + device.getType());
                    if (device.getType() == AudioDeviceInfo.TYPE_BUILTIN_MIC && device.isSource()) {
                        mAudioRecorder.setPreferredDevice(device);
                        found = true;
                        Log.e(LOG, "FOUND! - breaking loop.");
                        break;
                    }
                }
                if (found) {
                    Log.e(LOG, "Device found.");
                    startRecording();
                    mTaskHandler.removeCallbacks(mFindDeviceRunnable);
                } else {
                    Log.e(LOG, "Device not found. Trying again soon.");
                    mTaskHandler.postDelayed(mFindDeviceRunnable, mWaitInterval);
                }
            } else {
                Log.e(LOG, "Client not yet bound. Retrying soon.");
                mTaskHandler.postDelayed(mFindDeviceRunnable, mWaitInterval);
            }
        }
    };
    // This Runnable has the purpose of delaying/waiting until the application is ready again
    private Runnable mSetInterfaceRunnable = new Runnable() {
        @Override
        public void run() {
            if (!ControlService.getIsCharging()) {
                setInterface();
                mTaskHandler.removeCallbacks(mSetInterfaceRunnable);
            } else {
                mTaskHandler.postDelayed(mSetInterfaceRunnable, mWaitInterval);
            }
        }
    };
    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                setInterface();
                Log.e(LOG, "BT connected.");
                LogIHAB.log("Bluetooth: connected");
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                stopRecording();
                Log.e(LOG, "BT disconnected.");
                LogIHAB.log("Bluetooth: disconnected");
            }
        }
    };

    public InputProfile_INTERNAL_MIC(ControlService context, Messenger serviceMessenger) {
        this.mContext = context;
        this.mServiceMessenger = serviceMessenger;
    }

    @Override
    public String getInputProfile() {
        return this.INPUT_PROFILE;
    }

    @Override
    public void setInterface() {


        if (!ControlService.getIsCharging()) {
            Log.e(LOG, "Setting interface");
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.mContext);

            String chunklengthInS = mSharedPreferences.getString("chunklengthInS", "60");
            String samplerate = mSharedPreferences.getString("samplerate", "48000");
            boolean isWave = mSharedPreferences.getBoolean("isWave", true);

            if (mAudioRecorder == null) {
                mAudioRecorder = new AudioRecorder(
                        mContext,
                        mServiceMessenger,
                        Integer.parseInt(chunklengthInS),
                        Integer.parseInt(samplerate),
                        isWave);
            }

            mTaskHandler.postDelayed(mFindDeviceRunnable, mWaitInterval);
        } else {
            mTaskHandler.postDelayed(mSetInterfaceRunnable, mWaitInterval);
        }
    }

    @Override
    public void cleanUp() {
        mTaskHandler.removeCallbacks(mFindDeviceRunnable);
        mTaskHandler.removeCallbacks(mSetInterfaceRunnable);
        Log.e(LOG, "audiorecorder:" + mAudioRecorder);
        if (mAudioRecorder != null) {
            stopRecording();
        }
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
        stopRecording();
    }

    @Override
    public void batteryCritical() {
        cleanUp();
    }

    @Override
    public void chargingOff() {
        Log.e(LOG, "CharginOff");
        mTaskHandler.postDelayed(mSetInterfaceRunnable, mWaitInterval);
    }

    @Override
    public void chargingOn() {
        Log.e(LOG, "CharginOn");
        stopRecording();
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

    }

    private void startRecording() {
        Log.d(LOG, "Start caching audio");
        LogIHAB.log("Start caching audio");
        AudioFileIO.setChunkId(mContext.getChunkId());
        if (!ControlService.getIsRecording()) {
            mAudioRecorder.start();
        }
    }

    private void stopRecording() {
        if (ControlService.getIsRecording()) {
            Log.d(LOG, "Requesting stop caching audio");
            LogIHAB.log("Requesting stop caching audio");
            mAudioRecorder.stop();
            mTaskHandler.post(mAudioReleaseRunnable);
        }
    }
}
