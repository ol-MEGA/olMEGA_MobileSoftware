package com.fragtest.android.pa.InputProfile;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Messenger;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fragtest.android.pa.AudioRecorder;
import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.AudioFileIO;
import com.fragtest.android.pa.Core.ConnectedThread;
import com.fragtest.android.pa.Core.LogIHAB;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class InputProfile_RFCOMM implements InputProfile {

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final String INPUT_PROFILE = "RFCOMM";
    ConnectedThread mConnectedThread = null;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mSocket;
    private ControlService mContext;
    private AudioRecorder mAudioRecorder;
    private Messenger mServiceMessenger;
    private String LOG = "InputProfile_RFCOMM";

    //private SharedPreferences mSharedPreferences;
    private Handler mTaskHandler = new Handler();
    private int mWaitInterval = 200;
    private int mReleaseInterval = 0;
    private int mSocketInterval = 1000;
    private boolean mIsBound = false;
    private int mChunklengthInS;
    private boolean mIsWave;
    private int mSamplerate = 16000;


    private final BroadcastReceiver mUUIDReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice deviceExtra =
                    intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            Parcelable[] uuidExtra =
                    intent.getParcelableArrayExtra("android.bluetooth.device.extra.UUID");

            Log.e(LOG, "GOT SOMETHING: " + deviceExtra);

            //Parse the UUIDs and get the one you are interested in
        }
    };

    private final BroadcastReceiver mBTReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(LOG, "ACTION: " + action);
        }
    };

    // This Runnable has the purpose of delaying a release of mConnectedThread to avoid null pointer
    private Runnable mAudioReleaseRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mConnectedThread.getIsReleased()) {
                mConnectedThread.release();
                mConnectedThread = null;
                mSocket = null;
                mTaskHandler.removeCallbacks(mAudioReleaseRunnable);
            } else {
                mTaskHandler.postDelayed(mAudioReleaseRunnable, mWaitInterval);
            }
        }
    };

    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                    //setInterface();

                    // Save list of paired devices
                    /*Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                    for (BluetoothDevice bt : pairedDevices) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                        sharedPreferences.edit().putString("BTDevice", bt.getAddress()).apply();
                        Log.i(LOG, "CONNECTED TO: " + bt.getAddress());
                    }*/
                    /*try {
                        mSocket.connect();
                    } catch (Exception e) {
                        Log.e(LOG, "I HATE THIS.");
                    }*/
                    startRecording();
                    Log.e(LOG, "BT connected.");
                    LogIHAB.log("Bluetooth: connected");
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    stopRecording();
                    Log.e(LOG, "BT disconnected.");
                    LogIHAB.log("Bluetooth: disconnected");
                }
            }
        }
    };

    private Runnable mSocketConnectRunnable = new Runnable() {
        @Override
        public void run() {

            Log.e(LOG, "Connection Runnable.");

            if (!mSocket.isConnected()) {
                try {
                    mSocket.connect();
                } catch (IOException e) {
                    Log.e(LOG, "Problem connecting socket: " + e.toString());
                }
                mTaskHandler.postDelayed(mSocketConnectRunnable, mSocketInterval);
            } else {
                Log.e(LOG, "ALREADY CONNECTED.");
            }
            mTaskHandler.removeCallbacks(mSocketConnectRunnable);
        }
    };

    // This Runnable scans for available audio devices and acts if an A2DP device is present
    private Runnable mFindDeviceRunnable = new Runnable() {
        @Override
        public void run() {

            Log.e(LOG, "Looking for device.");
            if (mIsBound && !ControlService.getIsCharging()) {
                Log.e(LOG, "Now.");

                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                for (BluetoothDevice bt : pairedDevices) {

                    Log.e(LOG, "Device: " + bt.getBluetoothClass().getDeviceClass());

                    if (bt.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET) {

                        Log.e(LOG, "This looks promising:" + bt.getAddress() + ", Type: " + bt.getType());

                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(bt.getAddress());
                        try {
                            mSocket.close();
                        }catch(Exception e){

                        }
                        mSocket = null;

                        //mBluetoothAdapter.cancelDiscovery();

                        //device.createBond();
/*
                        Class<?> clazz = mBluetoothAdapter.getRemoteDevice(bt.getAddress()).getClass();
                        Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};

                        try {
                            Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                            Object[] params = new Object[]{Integer.valueOf(2)};

                            mSocket = (BluetoothSocket) m.invoke(mBluetoothAdapter.getRemoteDevice(bt.getAddress()), params);
                            mSocket.close();
                            mSocket.connect();
                        } catch (Exception e) {
                            Log.e(LOG, "NOPE!");
                        }*/


                        try {
                            Class<?> clazz = mBluetoothAdapter.getRemoteDevice(bt.getAddress()).getClass();
                            Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};

                            Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                            Object[] params = new Object[]{Integer.valueOf(1)};

                            mSocket = (BluetoothSocket) m.invoke(mBluetoothAdapter.getRemoteDevice(bt.getAddress()), params);
                            mSocket.connect();
                        } catch (Exception e) {
                            Log.e(LOG, "SOMETHING SUCKS: " + e.toString());
                        }



/*
                        try {

                            mSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,2);
                            //mSocket = device.createRfcommSocketToServiceRecord(MY_UUID);


                        /*} catch (IOException e) {
                            Log.e(LOG, "Socket connection failed\n" + e.toString() + "\n");
                            mSocket = null;*/ /*
                        } catch (NoSuchMethodException e) {
                            Log.e(LOG, "No Such Method: " + e.toString());
                        } catch (java.lang.IllegalAccessException e) {
                            Log.e(LOG, "Illegal Access: " + e.toString());
                        } catch (java.lang.reflect.InvocationTargetException e) {
                            Log.e(LOG, "Some other weird shit.");
                        }*/

                        if (mConnectedThread != null) {
                            mConnectedThread.cancel();
                            mConnectedThread = null;
                        }

                        Log.e(LOG, "SOCKET: " + mSocket);
                    }


                  // mTaskHandler.postDelayed(mSocketConnectRunnable, mSocketInterval);

                    mConnectedThread = new ConnectedThread(mSocket, mServiceMessenger, mChunklengthInS, mIsWave);
                    mConnectedThread.setPriority(Thread.MAX_PRIORITY);
                    //startRecording();
                }


            } else {
                Log.e(LOG, "Client not yet bound. Retrying soon.");
                //mTaskHandler.postDelayed(mFindDeviceRunnable, mWaitInterval);
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

    public InputProfile_RFCOMM(ControlService context, Messenger serviceMessenger) {
        this.mContext = context;
        this.mServiceMessenger = serviceMessenger;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public String getInputProfile() {
        return this.INPUT_PROFILE;
    }

    @Override
    public void setInterface() {

        Log.e(LOG, "Requested setInterface()");

        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        // Register broadcasts receiver for bluetooth state change
        IntentFilter filterBT = new IntentFilter();
        filterBT.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filterBT.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filterBT.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        mContext.registerReceiver(mBluetoothStateReceiver, filterBT);


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mContext.registerReceiver(mBTReceiver, intentFilter);

        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        //mBluetoothAdapter.startDiscovery();

        mBluetoothAdapter.cancelDiscovery();

        // Yes - it's spelled like that.
        String action = "android.bleutooth.device.action.UUID";
        IntentFilter filterUUID = new IntentFilter(action);
        mContext.registerReceiver(mUUIDReceiver, filterUUID);

        if (!ControlService.getIsCharging()) {
            Log.e(LOG, "Setting interface");
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.mContext);
            mChunklengthInS = Integer.parseInt(sharedPreferences.getString("chunklengthInS", "60"));
            mIsWave = sharedPreferences.getBoolean("isWave", true);

            mTaskHandler.postDelayed(mFindDeviceRunnable, mWaitInterval);
        }
    }

    @Override
    public void cleanUp() {

        Log.e(LOG, "Cleaning up.");

        mTaskHandler.removeCallbacks(mFindDeviceRunnable);
        mTaskHandler.removeCallbacks(mSetInterfaceRunnable);
        try {
            mContext.unregisterReceiver(mBluetoothStateReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(LOG, "Receiver not registered: mBluetoothStateReceiver");
        }

        try {
            mContext.unregisterReceiver(mUUIDReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(LOG, "Receiver not registered: mUUIDReceiver");
        }

        if (ControlService.getIsRecording()) {
            stopRecording();
        }
        mBluetoothAdapter = null;
        System.gc();
    }

    @Override
    public boolean getIsAudioRecorderClosed() {
        if (mConnectedThread != null) {
            return mConnectedThread.getIsReleased();
        }
        return true;
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
        cleanUp();
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

        Log.e(LOG, "Requested start recording");

        AudioFileIO.setChunkId(mContext.getChunkId());
        if (!ControlService.getIsRecording() && mConnectedThread != null) {
            Log.d(LOG, "Start caching audio");
            LogIHAB.log("Start caching audio");
            mConnectedThread.start();
            mContext.getVibration().singleBurst();
        }
    }

    private void stopRecording() {

        Log.e(LOG, "Requested stop recording");

        if (ControlService.getIsRecording()) {
            Log.e(LOG, "Requesting stop caching audio");
            LogIHAB.log("Requesting stop caching audio");

            if (mConnectedThread != null) {
                mConnectedThread.stopRecording();
                mConnectedThread.cancel();
                mContext.getVibration().singleBurst();
                mTaskHandler.postDelayed(mAudioReleaseRunnable, mReleaseInterval);
            }
        }
    }


}
