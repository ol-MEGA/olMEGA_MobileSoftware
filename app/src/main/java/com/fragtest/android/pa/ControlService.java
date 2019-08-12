package com.fragtest.android.pa;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fragtest.android.pa.Core.AudioFileIO;
import com.fragtest.android.pa.Core.ConnectedThread;
import com.fragtest.android.pa.Core.EventReceiver;
import com.fragtest.android.pa.Core.EventTimer;
import com.fragtest.android.pa.Core.FileIO;
import com.fragtest.android.pa.Core.LogIHAB;
import com.fragtest.android.pa.Core.SingleMediaScanner;
import com.fragtest.android.pa.Core.Vibration;
import com.fragtest.android.pa.Core.XMLReader;
import com.fragtest.android.pa.DataTypes.INPUT_CONFIG;
import com.fragtest.android.pa.Processing.MainProcessingThread;
import com.fragtest.android.pa.ServiceStates.ServiceState;
import com.fragtest.android.pa.ServiceStates.StateA2DP;
import com.fragtest.android.pa.ServiceStates.StateRFCOMM;
import com.fragtest.android.pa.ServiceStates.StateSTANDALONE;
import com.fragtest.android.pa.ServiceStates.StateUSB;

import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * The brains of the operation.
 *
 * Based on https://developer.android.com/reference/android/app/Service.html
 */


public class ControlService extends Service {


    static final String LOG = "ControlService";
    static final int CURRENT_YEAR = 2019;
    public static INPUT_CONFIG INPUT;// = INPUT_CONFIG.USB;


    /**
     * Constants for messaging. Should(!) be self-explanatory.
     */

    // 1* - general
    public static final int MSG_REGISTER_CLIENT         = 11;
    public static final int MSG_UNREGISTER_CLIENT       = 12;
    public static final int MSG_GET_STATUS              = 13;
    public static final int MSG_RESET_BT                = 14;
    public static final int MSG_NO_QUESTIONNAIRE_FOUND  = 15;
    public static final int MSG_SHUTDOWN_RECEIVED       = 16;
    public static final int MSG_CHANGE_PREFERENCE       = 17;
    public static final int MSG_BT_CONNECTED            = 18;
    public static final int MSG_BT_DISCONNECTED         = 19;

    // 2* - alarm
    public static final int MSG_ALARM_RECEIVED          = 21;
    public static final int MSG_START_COUNTDOWN         = 22;
    public static final int MSG_STOP_COUNTDOWN          = 23;
    public static final int MSG_SET_COUNTDOWN_TIME      = 24;

    // 3* - questionnaire
    public static final int MSG_QUESTIONNAIRE_FINISHED  = 30;
    public static final int MSG_ISMENU                  = 31;
    public static final int MSG_QUESTIONNAIRE_INACTIVE  = 32;
    public static final int MSG_START_QUESTIONNAIRE     = 33;
    public static final int MSG_PROPOSE_QUESTIONNAIRE   = 34;
    public static final int MSG_PROPOSITION_ACCEPTED    = 35;
    public static final int MSG_MANUAL_QUESTIONNAIRE    = 36;
    public static final int MSG_QUESTIONNAIRE_ACTIVE    = 37;
    public static final int MSG_CHECK_FOR_PREFERENCES   = 38;
    public static final int MSG_PREFS_IN_FOREGROUND     = 39;

    // 4* - recording
    public static final int MSG_START_RECORDING         = 41;
    public static final int MSG_STOP_RECORDING          = 42;
    public static final int MSG_RECORDING_STOPPED       = 43;
    public static final int MSG_CHUNK_RECORDED          = 44;

    // 5* - processing
    public static final int MSG_CHUNK_PROCESSED         = 51;

    // 6* - application
    public static final int MSG_APPLICATION_SHUTDOWN    = 61;
    public static final int MSG_BATTERY_CRITICAL        = 62;
    public static final int MSG_BATTERY_LEVEL_INFO      = 63;
    public static final int MSG_CHARGING_OFF            = 64;
    public static final int MSG_CHARGING_ON             = 65;
    public static final int MSG_CHARGING_ON_PRE         = 66;
    public static final int MSG_TIME_CORRECT            = 67;
    public static final int MSG_TIME_INCORRECT          = 68;
    public static final int MSG_STATE_CHANGE            = 69;

    // 7* - USB
    public static final int MSG_USB_DEVICE              = 70;
    public static final int MSG_USB_NO_DEVICE           = 71;
    public static final int MSG_USB_CONNECT 		    = 72;
    public static final int MSG_USB_DISCONNECT 		    = 73;
    public static final int MSG_USB_PERMISSION 		    = 74;
    public static final int MSG_USB_NO_PERMISSION 	    = 75;

    // 8* - Preferences
    public static final int MSG_PREFS_CLOSED            = 80;

    // 9* Permissions
    public static final int MSG_REQUEST_PERMISSION = 90;
    public static final int MSG_PERMISSION_RESULT = 91;


    // Shows whether questionnaire is active - tackles lifecycle jazz
    private boolean isActiveQuestionnaire = false;
    private boolean isTimerRunning = false;
    private boolean isQuestionnairePending = false;
    private boolean isQuestionnairePresent = false;
    private boolean isBTPresent = false;
    //public boolean isUSBPresent = false;
    private boolean isRegistered = false;
    private boolean isMenu = true;
    private XMLReader mXmlReader;
    private Vibration mVibration;
    private String mSelectQuestionnaire, mTempQuestionnaire;
    public static boolean isCharging = false;
    private static boolean isActivityRunning = true;

    private String operationModeStatus = "";

    // States
    public ServiceState mServiceState;
    private StateA2DP mStateA2DP;
    private StateRFCOMM mStateRFCOMM;
    private StateUSB mStateUSB;
    private StateSTANDALONE mStateSTANDALONE;


    public static final String FILENAME_LOG = "log2.txt";

    private int mChunkId = 1;

    // preferences
    private boolean isTimer, isWave, keepAudioCache, filterHp, downsample,
            showConfigButton, showRecordingButton, questionnaireHasTimer;

    private int mFinalCountDown, mTimerInterval;
    private ArrayList<String> mTimerList;
    private int mTimerNumber = 0;

    private String samplerate, chunklengthInS, filterHpFrequency;
    /**
     * Runnables
     **/


    public Runnable mStartRecordingRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRegistered && audioRecorder == null) {

                audioRecorder = new AudioRecorder(
                        serviceMessenger,
                        Integer.parseInt(chunklengthInS),
                        Integer.parseInt(samplerate),
                        isWave);

                if (!isCharging && audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
                    Log.e(LOG, "STARTING AUDIORECORDER!");
                    audioRecorder.start();
                    setIsRecording(true);
                    messageClient(MSG_START_RECORDING);
                } else {
                    mTaskHandler.postDelayed(mStartRecordingRunnable, 1000);
                }
            } else {
                mTaskHandler.postDelayed(mStartRecordingRunnable, 1000);
            }
        }
    };

    private boolean restartActivity = false; // TODO: implement in settings
    private NotificationManager mNotificationManager;

    private SharedPreferences sharedPreferences;
    private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);
    private Calendar dateTime;
    private Handler mTaskHandler = new Handler();
    private int mDateCheckTime = 5*60*1000;
    private int mLogCheckTime = 5*60*1000;
    private int mActivityCheckTime = 10*1000;
    public int mDisableBTTime = 10*1000;
    // Questionnaire-Timer
    EventTimer mEventTimer;

    private FileIO mFileIO;

    // Messenger to clients
    private Messenger mClientMessenger;
    // Messenger to pass to threads
    final Messenger serviceMessenger = new Messenger(new MessageHandler());

    // Audio recording
    public AudioRecorder audioRecorder;

    // ID to access our notification
    private int NOTIFICATION_ID = 1;

    // recording/processing buffer
    int idxRecording = 0;
    int idxProcessing = 0;
    int processingBufferSize = 100;
    String[] processingBuffer = new String[processingBufferSize];

    // thread-safety
    static boolean isRecording = false;
    static boolean isProcessing = false;
    static final Object recordingLock = new Object();
    static final Object processingLock = new Object();

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private int mDelayResetBT = 500;

    public static final boolean useLogMode = true;

    Context context = this;

    // RFCOMM-specific
    public ConnectedThread mConnectedThread = null;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    /**
     * State Affairs
     **/


    public void setState(ServiceState newServiceState) {
        mServiceState = newServiceState;
    }

    public ServiceState getStateA2DP() {
        return mStateA2DP;
    }

    public ServiceState getStateRFCOMM() {
        return mStateRFCOMM;
    }

    public ServiceState getStateUSB() {
        return mStateUSB;
    }

    public ServiceState getStateStandalone() {
        return mStateSTANDALONE;
    }

    private boolean hasPermissionRecordAudio = false;

    // This BroadcastReceiver listens to attachment of a USB device
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.e(LOG, "SOMETHING COOL HAPPENED: " + action);
            Log.e(LOG, "MODE: " + mServiceState);

            switch (action) {

                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    mServiceState.usbAttached();
                    break;

                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    mServiceState.usbDetached();
                    break;

                case BluetoothDevice.ACTION_FOUND:
                    break;

                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    mServiceState.bluetoothReceived(action, sharedPreferences);
                    messageClient(ControlService.MSG_BT_CONNECTED);
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    break;

                case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                    break;

                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    mServiceState.bluetoothReceived(action, sharedPreferences);
                    messageClient(ControlService.MSG_BT_DISCONNECTED);
                    break;

                case "android.intent.action.SCREEN_ON":
                    LogIHAB.log("Display: on");
                    break;

                case "android.intent.action.SCREEN_OFF":
                    LogIHAB.log("Display: off");
                    break;
            }
        }
    };

    public Runnable mDisableBT = new Runnable() {
        @Override
        public void run() {
            if (isCharging && getIsRecording()) {
                if (INPUT == INPUT_CONFIG.RFCOMM) {
                    stopRecordingRFCOMM();
                } else {
                    // Bluetooth receiver handles stopRecording()
                    mBluetoothAdapter.disable();
                }
                mTaskHandler.postDelayed(mDisableBT, mDisableBTTime);
            }
        }
    };

    public Runnable mDateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            checkTime();
            mTaskHandler.postDelayed(mDateTimeRunnable, mDateCheckTime);
        }
    };

    public Runnable mLogCheckRunnable = new Runnable() {
        @Override
        public void run() {
            checkLog();
            mTaskHandler.postDelayed(mLogCheckRunnable, mLogCheckTime);
        }
    };

    // Check if Activity is running
    public Runnable mActivityCheckRunnable = new Runnable() {
        @Override
        public void run() {
            if (isActivityRunning != isActivityRunning(getPackageName())) {
                LogIHAB.log("Activity running: " + isActivityRunning);
            }
            isActivityRunning = isActivityRunning(getPackageName());
            mTaskHandler.postDelayed(mActivityCheckRunnable, mActivityCheckTime);
        }
    };


    /** Receivers **/


    private EventReceiver mAlarmReceiver = new EventReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            messageClient(MSG_ALARM_RECEIVED);
            // perform checks whether running a questionnaire is valid
            if (isMenu) { //!isActiveQuestionnaire
                messageClient(MSG_PROPOSE_QUESTIONNAIRE);
                mVibration.repeatingBurstOn();
            } else {
                // React to when questionnaire is active but another one is due
                // -> probably deprecated because timer is only started after Q was finished
                Log.i(LOG,"Waiting for new questionnaire.");
            }
            isTimerRunning = false;
            isQuestionnairePending = true;
        }
    };

    /*private ShutdownReceiver mShutdownReceiver = new ShutdownReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            messageClient(MSG_SHUTDOWN_RECEIVED);
            LogIHAB.log("Shutting down.");
        }
    };*/
    public Runnable mResetBTAdapterRunnable = new Runnable() {
        @Override
        public void run() {

            if (mBluetoothAdapter == null) {
                mBluetoothAdapter = getBluetoothAdapter();
            }

            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }

            if (INPUT == INPUT_CONFIG.RFCOMM) {
                if (sharedPreferences.contains("BTDevice")) {
                    String btdevice = sharedPreferences.getString("BTDevice", null);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(btdevice);
                    device.createBond();
                    LogIHAB.log("Connecting to device: " + device.getAddress());

                    //TODO: is this needed for RFCOMM as well?
                    mBluetoothAdapter.startDiscovery();
                }
            } else {
                connectBtDevice();
            }

        }
    };

    public static boolean getIsRecording() {
        synchronized (recordingLock) {
            return isRecording;
        }
    }

    final Messenger mMessengerHandler = new Messenger(new MessageHandler());

    // Send message to connected client with additional data
    public void messageClient(int what, Bundle data) {

        if (mClientMessenger != null) {
            try {
                Message msg = Message.obtain(null, what);
                msg.setData(data);
                mClientMessenger.send(msg);
            } catch (RemoteException e) {
            }
        } else {
            Log.d(LOG, "mClientMessenger is null.");
        }
    }

    // Send message to connected client
    public void messageClient(int what) {

        if (mClientMessenger != null) {
            try {
                Message msg = Message.obtain(null, what);
                mClientMessenger.send(msg);
            } catch (RemoteException e) {
            }
        } else {
            Log.d(LOG, "mClientMessenger is null.");
        }
    }

    /** Lifecycle **/


    @Override
    public void onCreate() {
        Log.e(LOG, "onCreate");

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();

        mStateA2DP          = new StateA2DP(this);
        mStateRFCOMM        = new StateRFCOMM(this);
        mStateUSB           = new StateUSB(this);
        mStateSTANDALONE    = new StateSTANDALONE(this);

        // Begin as Standalone
        mServiceState = mStateSTANDALONE;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String operationMode = sharedPreferences.getString("operationMode", "STANDALONE");
        setOperationMode(operationMode);

        // If Chunk ID is not included in Shared Preference, initialize it -- else obtain it.
        if (sharedPreferences.getInt("chunkId", 0) == 0) {
            sharedPreferences.edit().putInt("chunkId", mChunkId).apply();
        } else {
            mChunkId = sharedPreferences.getInt("chunkId", mChunkId);
        }

        // If no Date representation can be found in Shared Preferences, add it.
        Date dateTime = new Date(System.currentTimeMillis());
        if (sharedPreferences.getLong("timeStamp", 0) == 0) {
            sharedPreferences.edit().putLong("timeStamp", dateTime.getTime()).apply();
        }

        // FileIO lets us read questionnaires
        mFileIO = new FileIO();
        isQuestionnairePresent = mFileIO.setupFirstUse(this);

        // Event Timer is used to schedule Alarms
        mEventTimer = new EventTimer(this, serviceMessenger);
        mEventTimer.setTimer(10);
        mEventTimer.stopTimer();

        // Vibration Device
        mVibration = new Vibration(this);
        mVibration.singleBurst();

        // Start Monitoring Processes for 1) Correct Time, 2) Log keeping, 3) Activity Running
        mTaskHandler.post(mDateTimeRunnable);
        mTaskHandler.post(mLogCheckRunnable);
        mTaskHandler.post(mActivityCheckRunnable);

        // Register Event Receiver for Alarms
        this.registerReceiver(mAlarmReceiver, new IntentFilter("AlarmReceived"));

        // Register Broadcast Receiver to keep a log of Display Activity ...
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        // ... for USB Activity
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        // ... and for Bluetooth Activity
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        this.registerReceiver(mBroadcastReceiver, filter);

        //checkPermissions();

        checkForPreferences();

        LogIHAB.log("Service started");
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int StartID) {
        Log.d(LOG, "onStartCommand");
        Logger.info("Service started");
        return START_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(LOG,"onConfigurationChanged");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(LOG,"onLowMemory");
    }

    @Override
    public void onDestroy() {
        Log.d(LOG, "onDestroy");
        super.onDestroy();

        mServiceState.onDestroy();
        mNotificationManager.cancel(NOTIFICATION_ID);

        LogIHAB.log("Service stopped");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG, "onBind");
        LogIHAB.log("Service bound");
        return mMessengerHandler.getBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        Log.e(LOG,"onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(LOG,"onUnbind");
        if (audioRecorder != null) {
            audioRecorder.close();
            audioRecorder = null;
        }
        stopSelf();
        return super.onUnbind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e(LOG,"onTaskRemoved");
        if (audioRecorder != null) {
            audioRecorder.close();
            audioRecorder = null;
        }
        super.onTaskRemoved(rootIntent);
        LogIHAB.log("Service closed");
        stopSelf();
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        Log.e(LOG,"onDump");
        super.dump(fd, writer, args);
    }


    /** Custom Methods **/


    public void startActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void showNotification() {

        // Launch activity when notification is selected
        PendingIntent intent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Configure notification
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.logo)
                .setTicker(getString(R.string.app_name))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.app_name))
                .setContentIntent(intent)
                .build();

        // Post notification to status bar, use startForeground() instead of
        // NotificationManager.notify() to prevent service from being killed
        // by ActivityManager when the Activity gets shut down.
        startForeground(NOTIFICATION_ID, notification);
    }

    public boolean isActivityRunning(String myPackage) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        for (int iActivity = 0; iActivity < runningTaskInfo.size(); iActivity++) {
            ComponentName componentInfo = runningTaskInfo.get(iActivity).topActivity;
            if (componentInfo.getPackageName().equals(myPackage)) {
                return true;
            }
        }
        if (INPUT == INPUT_CONFIG.RFCOMM) {
            stopRecordingRFCOMM();
        } else if (INPUT == INPUT_CONFIG.A2DP) {
            mBluetoothAdapter.disable();
        }
        return false;
    }

    private int getChunkId() {
        // Returns the current chunk ID and increments
        if (mChunkId < 999999) {
            mChunkId += 1;
        } else {
            mChunkId = 1;
        }
        sharedPreferences.edit().putInt("chunkId", mChunkId).apply();
        LogIHAB.log("Returning chunk id: "+ mChunkId);
        return mChunkId;
    }

    public void enableBluetoothAdapter() {
        mBluetoothAdapter.enable();
    }

    public void disableBluetoothAdapter() {
        mBluetoothAdapter.disable();
    }

    public boolean getIsBluetoothAdapterEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public boolean getIsBTPresent() {
        return isBTPresent;
    }

    public void setIsBTPresent(boolean present) {
        isBTPresent = present;
    }

    public boolean getIsCharging() {
        return isCharging;
    }

    public Vibration getVibration() {
        return mVibration;
    }

    public Handler getMTaskHandler() {
        return mTaskHandler;
    }

    private boolean checkLog() {

        File fLog = new File(FileIO.getFolderPath() + File.separator + FILENAME_LOG);

        if (!fLog.exists()) {
            try{
                fLog.createNewFile();
                Log.d(LOG, "Log file created");
            } catch (IOException e) {
                Log.d(LOG, "Error creating Log file");
            }
        }

        new SingleMediaScanner(context, fLog);
        Log.d(LOG, "Log file checked.");

        return true;
    }

    public boolean checkUSB() {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        if (deviceList.size() > 0) {
            //mServiceState.usbAttached();
            return true;
        } else {
            return false;
        }
    }

    private boolean checkTime() {
        // Check whether system time has correct year (devices tend to fall back to 1970 on startup)

        long prefTime = sharedPreferences.getLong("timeStamp",0);
        long systemTime = new Date(System.currentTimeMillis()).getTime();

        if (systemTime < prefTime) {
            messageClient(MSG_TIME_INCORRECT);
            Logger.info("Device Time false: " + Calendar.getInstance().getTime());
            LogIHAB.log("Device Time false: " + Calendar.getInstance().getTime());
            Log.e(LOG, "Device Time false: " + Calendar.getInstance().getTime());
            return false;
        /*}
        if (Calendar.getInstance().get(Calendar.YEAR) < CURRENT_YEAR) {
            messageClient(MSG_TIME_INCORRECT);
            Logger.info("Device Time false: " + Calendar.getInstance().getTime());
            LogIHAB.log("Device Time false: " + Calendar.getInstance().getTime());
            Log.e(LOG, "Device Time false: " + Calendar.getInstance().getTime());
            return false;*/
        } else {
            messageClient(MSG_TIME_CORRECT);
            Logger.info("Device Time: " + Calendar.getInstance().getTime());
            LogIHAB.log("Device Time: " + Calendar.getInstance().getTime());
            Log.e(LOG, "Device Time: " + Calendar.getInstance().getTime());
            return true;
        }
    }

    /** Functional Stuff **/


    /*public void announceBTDisconnected() {
        if (INPUT == INPUT_CONFIG.A2DP || INPUT == INPUT_CONFIG.RFCOMM) {
            Log.e(LOG, "BTDEVICES not connected.");

            if (INPUT == INPUT_CONFIG.RFCOMM) {
                stopRecordingRFCOMM();
            } else {
                stopRecording();
            }
            setIsBTPresent(false);
            mVibration.singleBurst();
        }
    }*/

    /*public void announceBTConnected() {
        if (INPUT == INPUT_CONFIG.A2DP || INPUT == INPUT_CONFIG.RFCOMM) {
            Log.e(LOG, "BTDEVICES connected.");

            if (INPUT == INPUT_CONFIG.RFCOMM) {
                startRecordingRFCOMM();
            } else if (INPUT == INPUT_CONFIG.A2DP) {
                startRecording();
            }
            isBTPresent = true;
            mTaskHandler.removeCallbacks(mResetBTAdapterRunnable);
        }
    }*/

    /*public void announceUSBConnected() {
            messageClient(MSG_USB_CONNECT);
            startRecording();
    }

    public void announceUSBDisconnected() {
            messageClient(MSG_USB_DISCONNECT);
            stopRecording();
    }*/

    /*private void setOperationMode(INPUT_CONFIG operationMode) {
        if (operationMode != operationModeStatus) {
            INPUT = operationMode;
            operationModeStatus = operationMode;
            LogIHAB.log("Operation Mode changed to: " + operationMode);
        } else {
            LogIHAB.log("Operation Mode: " + operationMode);
        }
    }*/

    private void setOperationMode(String operationMode) {

        if (!operationMode.equals(operationModeStatus) || operationModeStatus.equals("")) {

            stopRecording();
            audioRecorder = null;

            switch (operationMode) {
                case "A2DP":
                    operationModeStatus = INPUT_CONFIG.A2DP.toString();
                    mServiceState = getStateA2DP();
                    INPUT = INPUT_CONFIG.A2DP;
                    break;
                case "RFCOMM":
                    operationModeStatus = INPUT_CONFIG.RFCOMM.toString();
                    mServiceState = getStateRFCOMM();
                    INPUT = INPUT_CONFIG.RFCOMM;
                    break;
                case "USB":
                    operationModeStatus = INPUT_CONFIG.USB.toString();
                    mServiceState = getStateUSB();
                    INPUT = INPUT_CONFIG.USB;
                    break;
                case "STANDALONE":
                    operationModeStatus = INPUT_CONFIG.STANDALONE.toString();
                    mServiceState = getStateStandalone();
                    INPUT = INPUT_CONFIG.STANDALONE;
                    break;
            }

            mServiceState.setInterface();

            LogIHAB.log("Operation Mode changed to: " + INPUT.toString());
            Log.e(LOG, "Operation Mode changed to: " + INPUT.toString());

            Bundle bundle = new Bundle();
            bundle.putString("operationMode", INPUT.toString());

            messageClient(MSG_STATE_CHANGE, bundle);
        } else {
            mServiceState.setInterface();
            LogIHAB.log("OpMode: " + INPUT.toString());
            Log.e(LOG, "OpMode: " + INPUT.toString());
        }

    }


    public void setAlarmAndCountdown() {

        if (mFileIO.scanForQuestionnaire(mSelectQuestionnaire)) {

            mXmlReader = new XMLReader(this, mSelectQuestionnaire);
            questionnaireHasTimer = mXmlReader.getQuestionnaireHasTimer();

            // Needed for the first run
            if (questionnaireHasTimer) {

                mTimerInterval = mXmlReader.getNewTimerInterval();

                if (!isTimerRunning) {

                    mEventTimer.stopTimer();
                    mVibration.repeatingBurstOff();
                    mEventTimer.setTimer(mTimerInterval);
                    mFinalCountDown = mEventTimer.getFinalCountDown();
                    isTimerRunning = true;

                    Bundle dataCountdown = new Bundle();
                    dataCountdown.putInt("finalCountDown", mFinalCountDown);
                    dataCountdown.putInt("countDownInterval", mTimerInterval);
                    messageClient(MSG_SET_COUNTDOWN_TIME, dataCountdown);

                } else {
                    // Usually when app is restarted
                    if (BuildConfig.DEBUG) {
                        Log.i(LOG, "Final Timer already set. Reinstating countdown");
                    }
                }
            }
        }
    }

    public void stopAlarmAndCountdown() {

        if (isTimerRunning) {
            Log.e(LOG, "Cancelling Alarm.");
            isTimerRunning = false;
            if (mEventTimer != null) {
                mEventTimer.stopTimer();
            }
            mVibration.repeatingBurstOff();
        }
    }

    // Load new questionnaire (initiated after quest change in preferences)
    public void renewQuestionnaire() {
        if (isQuestionnairePresent) {
            String[] questList = mFileIO.scanQuestOptions();

            if (mSelectQuestionnaire.isEmpty() && questList.length > 0) {
                mSelectQuestionnaire = questList[0];
            }
            mXmlReader = new XMLReader(this, mSelectQuestionnaire);
        }
    }

    // Starts a new questionnaire, motivation can be {"auto", "manual"}
    public void startQuestionnaire(String motivation) {

        if (isQuestionnairePresent) {
            Bundle data = new Bundle();
            ArrayList<String> questionList = mXmlReader.getQuestionList();
            String head = mXmlReader.getHead();
            String foot = mXmlReader.getFoot();
            String surveyUri = mXmlReader.getSurveyURI();

            data.putStringArrayList("questionList", questionList);
            data.putString("head", head);
            data.putString("foot", foot);
            data.putString("surveyUri", surveyUri);
            data.putString("motivation", "<motivation motivation =\"" + motivation + "\"/>");

            messageClient(MSG_START_QUESTIONNAIRE, data);

            Log.i(LOG, "Questionnaire initiated: " + motivation);

            isQuestionnairePending = false;
        }
    }

    /*
    private void checkPermissions() {
        setHasPermissionRecordAudio(checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
        Log.e(LOG, "RECORD AUDIO PERMISSION: " + getHasPermissionRecordAudio());
    }*/

    /*
    private boolean getHasPermissionRecordAudio() {
        return hasPermissionRecordAudio;
    }

    private void setHasPermissionRecordAudio(boolean value) {
        hasPermissionRecordAudio = value;
    }*/

    private void requestPermission(int permission) {
        Bundle bundle = new Bundle();
        bundle.putInt("permissionName", permission);
        messageClient(MSG_REQUEST_PERMISSION, bundle);
    }

    private void updatePreferences(Bundle dataPreferences) {

        sharedPreferences = getSharedPreferences();

        // Extract preferences from data Bundle
        mSelectQuestionnaire = dataPreferences.getString("whichQuest", mSelectQuestionnaire);

        isWave = dataPreferences.getBoolean("isWave", isWave);
        //isTimer = dataPreferences.getBoolean("isTimer", isTimer);
        filterHp = dataPreferences.getBoolean("filterHp", filterHp);
        filterHpFrequency = dataPreferences.getString("filterHpFrequency", "" + filterHpFrequency);
        downsample = dataPreferences.getBoolean("downsample", downsample);
        keepAudioCache = dataPreferences.getBoolean("keepAudioCache", keepAudioCache);

        samplerate = dataPreferences.getString("samplerate", "" + samplerate);
        chunklengthInS = dataPreferences.getString("chunklengthInS", "" + chunklengthInS);

        String operationMode = dataPreferences.getString("operationMode", "" + InitValues.operationMode);

        Log.e(LOG, "FOUND MODE: " + operationMode);

        ArrayList<String> listActiveFeatures = dataPreferences.getStringArrayList("features");
        Set<String> activeFeatures = new HashSet<>();
        activeFeatures.addAll(listActiveFeatures);

        // Editor accumulates new preferences and writes them to shared preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Boolean
        editor.putBoolean("keepAudioCache", keepAudioCache);
        editor.putBoolean("isWave", isWave);
        //editor.putBoolean("isTimer", isTimer);
        editor.putBoolean("filterHp", filterHp);
        editor.putBoolean("downsample", downsample);
        editor.putBoolean("showCofigButton", showConfigButton);
        editor.putBoolean("showRecordingButton", showRecordingButton);
        // String Set
        editor.putStringSet("features", activeFeatures);
        // String
        editor.putString("whichQuest", mSelectQuestionnaire);
        editor.putString("filterHpFrequency", "" + filterHpFrequency);
        editor.putString("samplerate", "" + samplerate);
        editor.putString("chunklengthInS", "" + chunklengthInS);
        editor.putString("operationMode", operationMode);
        // Make changes permanent
        editor.apply();
    }

    private void checkForPreferences() {

        getPreferences();
        //isTimer = bundle.getBoolean("isTimer", isTimer);

        if (!Objects.equals(mSelectQuestionnaire, mTempQuestionnaire) && !mSelectQuestionnaire.isEmpty()) {

            Log.i(LOG, "New Questionnaire selected.");

            mTempQuestionnaire = mSelectQuestionnaire;
            // Reads new XML file
            renewQuestionnaire();
            isTimerRunning = false;
            questionnaireHasTimer = mXmlReader.getQuestionnaireHasTimer();
        }
    }

    private Bundle getPreferences() {

        isQuestionnairePresent = mFileIO.setupFirstUse(this);

        // recording
        samplerate = sharedPreferences.getString("samplerate","16000");
        chunklengthInS = sharedPreferences.getString("chunklengthInS", "60");
        keepAudioCache = sharedPreferences.getBoolean("keepAudioCache", keepAudioCache);
        isWave = sharedPreferences.getBoolean("isWave", isWave);

        // Use automatic timer
        //isTimer = sharedPreferences.getBoolean("isTimer", isTimer);

        // Scan file system for questionnaires
        if (isQuestionnairePresent) {
            String[] fileList = mFileIO.scanQuestOptions();

            if (fileList.length == 0) {
                Log.e(LOG, "No Questionnaires available.");
                messageClient(MSG_NO_QUESTIONNAIRE_FOUND);
                isQuestionnairePresent = false;
            } else {
                // Load questionnaire if selected, otherwise load default
                mSelectQuestionnaire = sharedPreferences.getString("whichQuest", fileList[0]);

                if (mTempQuestionnaire == null || mTempQuestionnaire.isEmpty() ) {
                    mTempQuestionnaire = "";
                }

                if (!mFileIO.scanForQuestionnaire(mSelectQuestionnaire)) {
                    mSelectQuestionnaire = null;
                }

                if (mSelectQuestionnaire == null || mSelectQuestionnaire.isEmpty()) {
                    mSelectQuestionnaire = fileList[0];
                    if (BuildConfig.DEBUG) {
                        Log.i(LOG, "Using default questionnaire: " + mSelectQuestionnaire);
                    }
                }
            }
        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("whichQuest", "").apply();
            if (BuildConfig.DEBUG) {
                Log.e(LOG, "No Questionnaires available.");
            }
            messageClient(MSG_NO_QUESTIONNAIRE_FOUND);
        }

        // processing
        HashSet<String> tempFeatures = new HashSet<>();
        tempFeatures.add("PSD");
        tempFeatures.add("RMS");
        tempFeatures.add("ZCR");

        HashSet<String> activeFeatures =
                (HashSet<String>) sharedPreferences.getStringSet("features", tempFeatures);

        filterHp = sharedPreferences.getBoolean("filterHp", true);
        filterHpFrequency = sharedPreferences.getString("filterHPFrequency", "100");

        Bundle processingSettings = new Bundle();
        //processingSettings.putBoolean("isTimer", isTimer);
        processingSettings.putInt("samplerate", Integer.parseInt(samplerate));
        processingSettings.putInt("chunklengthInS", Integer.parseInt(chunklengthInS));
        processingSettings.putBoolean("isWave", isWave);
        processingSettings.putSerializable("features", activeFeatures);
        processingSettings.putBoolean("filterHp", filterHp);
        processingSettings.putInt("filterHpFrequency", Integer.parseInt(filterHpFrequency));
        processingSettings.putBoolean("downsample", downsample);
        processingSettings.putString("whichQuest", mSelectQuestionnaire);

        return processingSettings;
    }



    /**
     * Thread-safe status variables
     */

    static void setIsRecording(boolean status) {
        synchronized (recordingLock) {
            isRecording = status;
        }
    }

    /**
     * Recording
     */


    public void startRecording() {

        //checkPermissions();

        //if (getHasPermissionRecordAudio()) {
        if (!getIsRecording() && audioRecorder == null) {
            Log.d(LOG, "Start caching audio");
            LogIHAB.log("Start caching audio");
            AudioFileIO.setChunkId(mChunkId);
            // A delay before starting a new recording prevents initialisation bug
            if (!isCharging) {
                mTaskHandler.postDelayed(mStartRecordingRunnable, 1000);
            }
        }
        //} else {
        //Log.e(LOG, "Permission not granted. Requesting.");
        //requestPermission(MainActivity.MY_PERMISSIONS_RECORD_AUDIO);
        //mTaskHandler.postDelayed(mStartRecordingRunnable, 1000);
        //}

    }

    static void setIsProcessing(boolean status) {
        synchronized (processingLock) {
            isProcessing = status;
        }
    }

    static boolean getIsProcessing() {
        synchronized (processingLock) {
            return isProcessing;
        }
    }

    void addProccessingBuffer(int idx, String filename) {
        synchronized (processingLock) {
            processingBuffer[idx] = filename;
        }
    }

    void deleteProccessingBuffer(int idx) {
        synchronized (processingLock) {
            processingBuffer[idx] = null;
        }
    }


    /**
     * RFCOMM
     */


    public void connectBtDevice() {

        if (mBluetoothAdapter.isEnabled()) {

            String deviceAddress = sharedPreferences.getString("BTDevice", "");
            if (!deviceAddress.equals("")) {

                if (mConnectedThread != null) {
                    mConnectedThread.cancel();
                    mConnectedThread = null;
                }

                Log.e(LOG, "Device Address: " + deviceAddress);

                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
                BluetoothSocket socket = null;
                try {
                    socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                    try {
                        // This is a blocking call and will only return on a successful connection or an exception
                        socket.connect();
                        mConnectedThread = new ConnectedThread(socket, serviceMessenger,
                                Integer.parseInt(chunklengthInS), isWave);
                        mConnectedThread.setPriority(Thread.MAX_PRIORITY);
                        //mConnectedThread.start();
                        Log.e(LOG, "Number 1");
                    } catch (IOException e) {
                        Log.e(LOG, "ConnectedThread creation failed " + e.toString());
                        mConnectedThread = null;
                    }
                } catch (IOException e) {
                    Log.e(LOG, "Socket create() failed " + e.toString());
                    mConnectedThread = null;
                }
            } else
                Log.e(LOG, "no Device selected. Please open Settings!");
        } else {
            Log.e(LOG, "Bluetooth not activated.");
            mBluetoothAdapter.enable();
        }
    }

    public void stopRecording() {

        if (getIsRecording() && audioRecorder != null) {
            Log.d(LOG, "Requesting stop caching audio");
            Logger.info("Requesting stop caching audio");
            LogIHAB.log("Requesting stop caching audio");
            audioRecorder.stop();
            setIsRecording(false);
            mTaskHandler.removeCallbacks(mStartRecordingRunnable);
            audioRecorder.close();
            audioRecorder = null;
            messageClient(MSG_STOP_RECORDING);
        }
    }

    /**
     * Communication between Service and Activity
     **/


    class MessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            Log.d(LOG, "Received Message: " + msg.what);

            switch (msg.what) {

                case MSG_REGISTER_CLIENT:

                    mClientMessenger = msg.replyTo;

                    Bundle data = new Bundle();
                    data.putString("operationMode", INPUT.toString());
                    messageClient(MSG_STATE_CHANGE, data);

                    mServiceState.registerClient();

                    isRegistered = true;

                    LogIHAB.log("Client registered to service");
                    break;

                case MSG_UNREGISTER_CLIENT:

                    mClientMessenger = null;
                    mServiceState.unregisterClient();

                    switch (INPUT) {
                        case A2DP:
                            mBluetoothAdapter.disable();
                            mTaskHandler.removeCallbacks(mResetBTAdapterRunnable);
                            break;

                        case RFCOMM:
                            stopRecordingRFCOMM();
                            mTaskHandler.removeCallbacks(mResetBTAdapterRunnable);
                            break;

                        case USB:

                            break;

                        case STANDALONE:

                            break;
                    }

                    // Remove Runnables
                    mTaskHandler.removeCallbacks(mDateTimeRunnable);
                    mTaskHandler.removeCallbacks(mLogCheckRunnable);

                    // Unregister Receivers
                    unregisterReceiver(mAlarmReceiver);
                    //unregisterReceiver(mDisplayReceiver);
                    unregisterReceiver(mBroadcastReceiver);
                    //unregisterReceiver(mBluetoothStateReceiver);
                    //unregisterReceiver(mUsbAttachReceiver);
                    //unregisterReceiver(mUsbDetachReceiver);

                    LogIHAB.log("Client unregistered from service");
                    break;


                case MSG_GET_STATUS:
                    Bundle status = new Bundle();
                    status.putBoolean("isRecording", getIsRecording());
                    messageClient(MSG_GET_STATUS, status);
                    break;

                case MSG_CHECK_FOR_PREFERENCES:
                    updatePreferences(msg.getData());
                    String operationMode = sharedPreferences.getString("operationMode", "STANDALONE");
                    setOperationMode(operationMode);
                    break;

                case MSG_RESET_BT:
                    mBluetoothAdapter.cancelDiscovery();
                    if (INPUT == INPUT_CONFIG.RFCOMM) {
                        stopRecordingRFCOMM();

                    } else {
                        mBluetoothAdapter.disable();
                    }
                    if (INPUT == INPUT_CONFIG.A2DP || INPUT == INPUT_CONFIG.RFCOMM) {
                        mTaskHandler.postDelayed(mResetBTAdapterRunnable, mDelayResetBT);
                    }
                    break;

                case MSG_START_COUNTDOWN:
                    checkForPreferences();
                    setAlarmAndCountdown();
                    break;

                case MSG_STOP_COUNTDOWN:
                    stopAlarmAndCountdown();
                    break;

                case MSG_MANUAL_QUESTIONNAIRE:
                    // User has initiated questionnaire manually without/before timer
                    startQuestionnaire("manual");
                    LogIHAB.log("Taking Questionnaire: manual");
                    break;

                case MSG_PROPOSITION_ACCEPTED:
                    // User has accepted proposition to start a new questionnaire by selecting
                    // "Start Questionnaire" item in User Menu
                    startQuestionnaire("auto");
                    LogIHAB.log("Taking Questionnaire: auto");
                    break;

                case MSG_QUESTIONNAIRE_FINISHED:
                    LogIHAB.log("Questionnaire finished");
                    break;

                case MSG_ISMENU:
                    isMenu = true;
                    break;

                case MSG_QUESTIONNAIRE_ACTIVE:
                    isMenu = false;
                    isActiveQuestionnaire = true;
                    mEventTimer.stopTimer();
                    isTimerRunning = false;
                    Log.i(LOG, "Questionnaire active");
                    break;

                case MSG_RECORDING_STOPPED:
                    Log.d(LOG, "Stop caching audio");
                    LogIHAB.log("Stop caching audio");

                    mServiceState.recordingStopped();

                    setIsRecording(false);
                    messageClient(MSG_GET_STATUS);
                    break;

                case MSG_CHUNK_RECORDED:

                    LogIHAB.log("CHUNK RECORDED");

                    AudioFileIO.setChunkId(getChunkId());

                    String filename = msg.getData().getString("filename");
                    addProccessingBuffer(idxRecording, filename);
                    idxRecording = (idxRecording + 1) % processingBufferSize;

                    if (!getIsProcessing()) {

                        LogIHAB.log("Start Processing");

                        Bundle settings = getPreferences();

                        settings.putString("filename", processingBuffer[idxProcessing]);
                        MainProcessingThread processingThread =
                                new MainProcessingThread(serviceMessenger, settings);
                        setIsProcessing(true);
                        processingThread.start();
                    }

                    if (keepAudioCache) {
                        new SingleMediaScanner(context, new File(filename));
                    }

                    Log.d(LOG, "New cache: " + filename);
                    LogIHAB.log("New cache:\t" + filename);

                    break;

                case MSG_CHUNK_PROCESSED:

                    ArrayList<String> featureFiles = msg.getData().
                            getStringArrayList("featureFiles");

                    if (!keepAudioCache) {
                        AudioFileIO.deleteFile(processingBuffer[idxProcessing]);
                    }

                    for (String file : featureFiles) {
                        if (file != null) {
                            new SingleMediaScanner(context, new File(file));
                        }
                    }

                    deleteProccessingBuffer(idxProcessing);
                    idxProcessing = (idxProcessing + 1) % processingBufferSize;

                    if (processingBuffer[idxProcessing] != null) {
                        Bundle settings = getPreferences();

                        settings.putString("filename", processingBuffer[idxProcessing]);
                        MainProcessingThread processingThread =
                                new MainProcessingThread(serviceMessenger, settings);
                        setIsProcessing(true);
                        processingThread.start();
                    } else {
                        setIsProcessing(false);
                    }

                    break;

                case MSG_APPLICATION_SHUTDOWN:
                    mServiceState.applicationShutdown();
                    LogIHAB.log("Shutdown");
                    break;

                case MSG_BATTERY_LEVEL_INFO:
                    float batteryLevel = msg.getData().getFloat("batteryLevel");
                    LogIHAB.log("battery level: " + batteryLevel);
                    Log.e(LOG, "Battery Level Info: " + batteryLevel);
                    break;

                case MSG_BATTERY_CRITICAL:
                    LogIHAB.log("CRITICAL battery level: active");
                    mServiceState.batteryCritical();
                    break;

                case MSG_CHARGING_OFF:
                    LogIHAB.log("Charging: inactive");
                    isCharging = false;
                    mServiceState.chargingOff();
                    mVibration.singleBurst();
                    break;

                case MSG_CHARGING_ON:
                    isCharging = true;
                    LogIHAB.log("Charging: active");
                    mVibration.singleBurst();
                    break;

                case MSG_CHARGING_ON_PRE:
                    isCharging = true;
                    LogIHAB.log("Charging: active");
                    break;

                case MSG_PERMISSION_RESULT:

                    Bundle result = msg.getData();
                    boolean value = result.getBoolean("permissionValue");

                    switch (result.getInt("permissionName")) {

                        case MainActivity.MY_PERMISSIONS_RECORD_AUDIO:
                            /*if (value) {
                                setHasPermissionRecordAudio(true);
                            } else {
                                setHasPermissionRecordAudio(false);
                            }*/
                            break;

                        default:
                            Log.e(LOG, "Permission result for " +
                                    result.getInt("permissionName") + ": " + value);
                    }

                    break;

                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    public void startRecordingRFCOMM() {

        Log.e(LOG, "mConnectedThread - start: " + mConnectedThread);
        Log.e(LOG, "Number 4");
        AudioFileIO.setChunkId(mChunkId);

        if (!isCharging) {

            setIsRecording(true);
            mConnectedThread.start();
            messageClient(MSG_START_RECORDING);
            mVibration.singleBurst();
        }

    }

    public void stopRecordingRFCOMM() {

        Log.e(LOG, "mConnectedThread - stop: " + mConnectedThread);
        if (mConnectedThread != null) {
            mConnectedThread.stopRecording();
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }




    /** Shit we need to think about **/


    /*private void setupApplication() {

        // If no chunk Id in present shared preferences, initialise with 1, else fetch
        // Important for the first initialisation
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getInt("chunkId", 0) == 0) {
            sharedPreferences.edit().putInt("chunkId", mChunkId).apply();
        } else {
            mChunkId = sharedPreferences.getInt("chunkId", mChunkId);
        }

        // If no Date representation is
        Date dateTime = new Date(System.currentTimeMillis());
        if (sharedPreferences.getLong("timeStamp", 0) == 0) {
            sharedPreferences.edit().putLong("timeStamp", dateTime.getTime()).apply();
        }

        //String operationMode = sharedPreferences.getString("operationMode", "Standalone");
        //setOperationMode(operationMode);

        initialiseValues();

        mFileIO = new FileIO();
        isQuestionnairePresent = mFileIO.setupFirstUse(this);

        Log.e(LOG, "Messenger Control S: "+mMessengerHandler);
        mEventTimer = new EventTimer(this, serviceMessenger); // mMessengerHandler

        mEventTimer.setTimer(10);
        mEventTimer.stopTimer();

        mVibration = new Vibration(this);
        mVibration.singleBurst();

        if (INPUT == INPUT_CONFIG.USB) {
            IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            registerReceiver(mUsbAttachReceiver , filter);
            filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
            registerReceiver(mUsbDetachReceiver , filter);
        }

        if (useLogMode) {
            // Register receiver for display activity (if used in log mode)
            IntentFilter displayFilter = new IntentFilter();
            displayFilter.addAction(Intent.ACTION_SCREEN_ON);
            displayFilter.addAction(Intent.ACTION_SCREEN_OFF);
            this.registerReceiver(mDisplayReceiver, displayFilter);
        }

        // Register receiver for alarm
        this.registerReceiver(mAlarmReceiver, new IntentFilter("AlarmReceived"));

        if (INPUT == INPUT_CONFIG.A2DP || INPUT == INPUT_CONFIG.RFCOMM) {
            // Register broadcasts receiver for bluetooth state change
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            this.registerReceiver(mBluetoothStateReceiver, filter);
        }

        // It is safe to say, that the display is illuminated on system/application startup
        if (useLogMode) {
            Logger.info("Display: on");
            LogIHAB.log("Display: on");
        }

        checkForPreferences();
    }*/


    // Load preset values from shared preferences, default values from external class InitValues
    /*private void initialiseValues() {

        //sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // preferences
        isTimer = sharedPreferences.getBoolean("isTimer", true);
        isWave = sharedPreferences.getBoolean("isWave", false);
        keepAudioCache = sharedPreferences.getBoolean("keepAudioCache", false);
        filterHp = sharedPreferences.getBoolean("filterHp", false);
        downsample = sharedPreferences.getBoolean("downsample", true);
        showRecordingButton = sharedPreferences.getBoolean("showRecordingButton", true);

        // Cave: These are Strings
        filterHpFrequency = sharedPreferences.getString("filterHpFrequency", "100");
        samplerate = sharedPreferences.getString("samplerate", "16000");
        chunklengthInS = sharedPreferences.getString("chunklengthInS", "60");

        mFinalCountDown = InitValues.finalCountDown;
        mTimerInterval = InitValues.timerInterval;
    }*/

}