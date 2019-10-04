package com.fragtest.android.pa.InputProfile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
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

public class InputProfile_USB implements InputProfile {

    private final String INPUT_PROFILE = "USB";
    private ControlService mContext;
    private AudioRecorder mAudioRecorder;
    private Messenger mServiceMessenger;
    private String LOG = "InputProfile_USB";
    private SharedPreferences mSharedPreferences;
    private Handler mTaskHandler = new Handler();
    private int mWaitInterval = 100;
    private boolean mIsBound = false;
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
                    if (device.getType() == AudioDeviceInfo.TYPE_USB_DEVICE && device.isSource()) {
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
    // This BroadcastReceiver listens to attachment of a USB device
    private final BroadcastReceiver mUSBReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    Log.e(LOG, "USB attached.");
                    setInterface();
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    Log.e(LOG, "USB detached.");
                    Log.e(LOG, "DEVICE DETACHED: " + UsbManager.EXTRA_DEVICE);
                    stopRecording();
                    break;
            }
        }
    };

    public InputProfile_USB(ControlService context, Messenger serviceMessenger) {
        this.mContext = context;
        this.mServiceMessenger = serviceMessenger;
        //this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.mContext);
    }

    @Override
    public String getInputProfile() {
        return this.INPUT_PROFILE;
    }

    @Override
    public void setInterface() {

        // Register Broadcast Receiver to keep a log of USB Activity ...
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mContext.registerReceiver(mUSBReceiver, filter);

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
        mContext.unregisterReceiver(mUSBReceiver);
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
            Log.e(LOG, "Requesting stop caching audio");
            LogIHAB.log("Requesting stop caching audio");
            mAudioRecorder.stop();
            mTaskHandler.post(mAudioReleaseRunnable);
        }
    }
}
