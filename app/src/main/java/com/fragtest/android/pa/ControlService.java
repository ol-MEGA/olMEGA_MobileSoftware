package com.fragtest.android.pa;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.fragtest.android.pa.Core.AudioFileIO;
import com.fragtest.android.pa.Core.EventReceiver;
import com.fragtest.android.pa.Core.EventTimer;
import com.fragtest.android.pa.Core.FileIO;
import com.fragtest.android.pa.Core.LogIHAB;
import com.fragtest.android.pa.Core.SingleMediaScanner;
import com.fragtest.android.pa.Core.Vibration;
import com.fragtest.android.pa.Core.XMLReader;
import com.fragtest.android.pa.Processing.MainProcessingThread;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.FileWriter;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * The brains of the operation.
 *
 * Based on https://developer.android.com/reference/android/app/Service.html
 */

public class ControlService extends Service {

    static final String LOG = "ControlService";
    static final boolean needsBluetooth = true;
    static final int CURRENT_YEAR = 2018;

    /**
     * Constants for messaging. Should(!) be self-explanatory.
     */

    // 1* - general
    public static final int MSG_REGISTER_CLIENT = 11;
    public static final int MSG_UNREGISTER_CLIENT = 12;
    public static final int MSG_GET_STATUS = 13;
    public static final int MSG_RESET_BT = 14;
    public static final int MSG_NO_QUESTIONNAIRE_FOUND = 15;
    public static final int MSG_NO_TIMER = 16;
    public static final int MSG_CHANGE_PREFERENCE = 17;
    public static final int MSG_BT_CONNECTED = 18;
    public static final int MSG_BT_DISCONNECTED = 19;

    // 2* - alarm
    public static final int MSG_ALARM_RECEIVED = 21;
    public static final int MSG_START_COUNTDOWN = 22;
    public static final int MSG_STOP_COUNTDOWN = 23;
    public static final int MSG_SET_COUNTDOWN_TIME = 24;

    // 3* - questionnaire
    public static final int MSG_QUESTIONNAIRE_FINISHED = 30;
    public static final int MSG_ISMENU = 31;
    public static final int MSG_QUESTIONNAIRE_INACTIVE = 32;
    public static final int MSG_START_QUESTIONNAIRE = 33;
    public static final int MSG_PROPOSE_QUESTIONNAIRE = 34;
    public static final int MSG_PROPOSITION_ACCEPTED = 35;
    public static final int MSG_MANUAL_QUESTIONNAIRE = 36;
    public static final int MSG_QUESTIONNAIRE_ACTIVE = 37;
    public static final int MSG_CHECK_FOR_PREFERENCES = 38;
    public static final int MSG_PREFS_IN_FOREGROUND = 39;

    // 4* - recording
    public static final int MSG_START_RECORDING = 41;
    public static final int MSG_STOP_RECORDING = 42;
    public static final int MSG_RECORDING_STOPPED = 43;
    public static final int MSG_CHUNK_RECORDED = 44;

    // 5* - processing
    public static final int MSG_CHUNK_PROCESSED = 51;

    // 6* - application
    public static final int MSG_APPLICATION_SHUTDOWN = 61;
    public static final int MSG_BATTERY_CRITICAL = 62;
    public static final int MSG_BATTERY_LEVEL_INFO = 63;
    public static final int MSG_CHARGING_OFF = 64;
    public static final int MSG_CHARGING_ON = 65;
    public static final int MSG_CHARGING_ON_PRE = 66;
    public static final int MSG_TIME_CORRECT = 67;
    public static final int MSG_TIME_INCORRECT = 68;

    // Shows whether questionnaire is active - tackles lifecycle jazz
    private boolean isActiveQuestionnaire = false;
    private boolean isTimerRunning = false;
    private boolean isQuestionnairePending = false;
    private boolean isQuestionnairePresent = false;
    private boolean isBluetoothPresent = false;
    private boolean isMenu = true;
    private XMLReader mXmlReader;
    private Vibration mVibration;
    private String mSelectQuestionnaire, mTempQuestionnaire;
    public static boolean isCharging = false;
    private static boolean isActivityRunning = true;

    public static final String FILENAME_LOG = "log2.txt";
    public static final String FILENAME_LOG_tmp = "log.txt";

    private int mChunkId = 1;

    // preferences
    private boolean isTimer, isWave, keepAudioCache, filterHp, downsample,
            showConfigButton, showRecordingButton, questionnaireHasTimer;

    private int mFinalCountDown, mTimerInterval;

    private String samplerate, chunklengthInS, filterHpFrequency;

    private boolean restartActivity = false; // TODO: implement in settings
    private NotificationManager mNotificationManager;

    private SharedPreferences sharedPreferences;
    private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);
    private Calendar dateTime;
    private Handler mTaskHandler = new Handler();
    private int mDateCheckTime = 5*60*1000;
    private int mLogCheckTime = 5*60*1000;
    private int mActivityCheckTime = 10*1000;
    private int mDisableBTTime = 10*1000;
    // Questionnaire-Timer
    EventTimer mEventTimer;

    private FileIO mFileIO;

    // Messenger to clients
    private Messenger mClientMessenger;
    // Messenger to pass to threads
    final Messenger serviceMessenger = new Messenger(new MessageHandler());

    // Audio recording
    private AudioRecorder audioRecorder;

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

    private Runnable mStartRecordingRunnable = new Runnable() {
        @Override
        public void run() {
            audioRecorder = new AudioRecorder(
                    serviceMessenger,
                    Integer.parseInt(chunklengthInS),
                    Integer.parseInt(samplerate),
                    isWave);
            if (!isCharging) {
                audioRecorder.start();
                setIsRecording(true);
                messageClient(MSG_START_RECORDING);
                mVibration.singleBurst();
            }
        }
    };

    private Runnable mResetBTAdapterRunnable = new Runnable() {
        @Override
        public void run() {
            mBluetoothAdapter.enable();
            mBluetoothAdapter.startDiscovery();
        }
    };

    private Runnable mDisableBT = new Runnable() {
        @Override
        public void run() {
            if (isCharging) {
                mBluetoothAdapter.disable();
                mTaskHandler.postDelayed(mDisableBT, mDisableBTTime);
            }
        }
    };

    private Runnable mDateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            checkTime();
            mTaskHandler.postDelayed(mDateTimeRunnable, mDateCheckTime);
        }
    };

    private Runnable mLogCheckRunnable = new Runnable() {
        @Override
        public void run() {
            checkLog();
            mTaskHandler.postDelayed(mLogCheckRunnable, mLogCheckTime);
        }
    };

    // Check if Activity is running
    private Runnable mActivityCheckRunnable = new Runnable() {
        @Override
        public void run() {

            if (isActivityRunning != isActivityRunning(getPackageName())) {
                LogIHAB.log("Activity running: " + isActivityRunning);
            }

            isActivityRunning = isActivityRunning(getPackageName());
            mTaskHandler.postDelayed(mActivityCheckRunnable, mActivityCheckTime);
        }
    };

    public boolean isActivityRunning(String myPackage) {
    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
    List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        for (int iActivity = 0; iActivity < runningTaskInfo.size(); iActivity++) {
            ComponentName componentInfo = runningTaskInfo.get(iActivity).topActivity;
            if (componentInfo.getPackageName().equals(myPackage)) {
                return true;
            }
        }
        mBluetoothAdapter.disable();
        return false;
    }

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

    private final BroadcastReceiver mDisplayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case "android.intent.action.SCREEN_ON":
                    Logger.info("Display: on");
                    LogIHAB.log("Display: on");
                    break;
                case "android.intent.action.SCREEN_OFF":
                    Logger.info("Display: off");
                    LogIHAB.log("Display: off");
                    break;
            }
        }
    };

    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.e(LOG, "BTDEVICES found.");
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                announceBTConnected();
                Logger.info("Bluetooth: connected");
                LogIHAB.log("Bluetooth: connected");
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.e(LOG, "BTDEVICES finished.");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                Log.e(LOG, "BTDEVICES about to disconnect.");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                announceBTDisconnected();
                Logger.info("Bluetooth: disconnected");
                LogIHAB.log("Bluetooth: disconnected");
            }
        }
    };

    class MessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            Log.d(LOG, "Received Message: " + msg.what);

            switch (msg.what) {

                case MSG_REGISTER_CLIENT:

                    Configurator.defaultConfig()
                            .writer(new FileWriter(FILENAME_LOG_tmp))
                            .level(Level.INFO)
                            .activate();

                    Log.e(LOG,"msg: "+msg);
                    Log.i(LOG, "Client registered to service");
                    Logger.info("Client registered to service");
                    LogIHAB.log("Client registered to service");
                    mClientMessenger = msg.replyTo;

                    setupApplication();
                    mTaskHandler.post(mDateTimeRunnable);
                    mTaskHandler.post(mLogCheckRunnable);
                    mTaskHandler.post(mActivityCheckRunnable);

                    // Set and announce bluetooth disabled - then enable it to force recognition via
                    // broadcast receiver. This way, a connection can be made with an already
                    // active transmitter
                    mBluetoothAdapter.disable();
                    messageClient(MSG_BT_DISCONNECTED);


                    IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    Intent batteryStatus = registerReceiver(null, batteryFilter);

                    if (!isCharging) {
                        if (!mBluetoothAdapter.isEnabled()) {
                            mBluetoothAdapter.enable();
                        }
                    } else {
                        mVibration.singleBurst();
                    }

                    if (!needsBluetooth) {
                        announceBTConnected();
                    }

                    checkTime();
                    checkLog();
                    break;

                case MSG_UNREGISTER_CLIENT:
                    mClientMessenger = null;
                    stopAlarmAndCountdown();
                    Logger.info("Client unregistered from service");
                    LogIHAB.log("Client unregistered from service");
                    if (restartActivity) {
                        startActivity();
                    } else {
                        mBluetoothAdapter.disable();
                    }
                    mTaskHandler.removeCallbacks(mDateTimeRunnable);
                    mTaskHandler.removeCallbacks(mLogCheckRunnable);
                    mTaskHandler.removeCallbacks(mResetBTAdapterRunnable);
                    break;

                case MSG_GET_STATUS:
                    Bundle status = new Bundle();
                    status.putBoolean("isRecording", getIsRecording());
                    messageClient(MSG_GET_STATUS, status);
                    break;

                case MSG_RESET_BT:
                    mBluetoothAdapter.cancelDiscovery();
                    mBluetoothAdapter.disable();
                    mTaskHandler.postDelayed(mResetBTAdapterRunnable, mDelayResetBT);
                    break;

                case MSG_START_COUNTDOWN:
                    setAlarmAndCountdown();
                    break;

                case MSG_STOP_COUNTDOWN:
                    stopAlarmAndCountdown();
                    break;

                case MSG_MANUAL_QUESTIONNAIRE:
                    // User has initiated questionnaire manually without/before timer
                    startQuestionnaire("manual");
                    Logger.info("Taking Questionnaire: manual");
                    LogIHAB.log("Taking Questionnaire: manual");
                    break;

                case MSG_PROPOSITION_ACCEPTED:
                    // User has accepted proposition to start a new questionnaire by selecting
                    // "Start Questionnaire" item in User Menu
                    startQuestionnaire("auto");
                    Logger.info("Taking Questionnaire: auto");
                    LogIHAB.log("Taking Questionnaire: auto");
                    break;

                case MSG_QUESTIONNAIRE_FINISHED:
                    Logger.info("Questionnaire finished");
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

                case MSG_CHECK_FOR_PREFERENCES:
                    if (!msg.getData().isEmpty()) {
                        Bundle prefs = msg.getData();
                        updatePreferences(prefs);
                    }
                    checkForPreferences();
                    break;

                case MSG_RECORDING_STOPPED:
                    Log.d(LOG, "Stop caching audio");
                    Logger.info("Stop caching audio");
                    LogIHAB.log("Stop caching audio");
                    audioRecorder.close();
                    setIsRecording(false);
                    messageClient(MSG_GET_STATUS);
                    break;

                case MSG_CHUNK_RECORDED:

                    LogIHAB.log("CHUNK RECORDED");

                    AudioFileIO.setChunkId(getChunkId());

                    String filename = msg.getData().getString("filename");
                    addProccessingBuffer(idxRecording, filename);
                    idxRecording = (idxRecording + 1) % processingBufferSize;

                    LogIHAB.log("isProcessing: "+getIsProcessing()+", isRecording: "+getIsRecording());

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
                    Logger.info("New cache:\t{}", filename);
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
                    //stopRecording() is called by receiver
                    if (mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.disable();
                    }
                    Logger.info("Shutdown");
                    LogIHAB.log("Shutdown");
                    break;

                case MSG_BATTERY_LEVEL_INFO:
                    float batteryLevel = msg.getData().getFloat("batteryLevel");
                    Logger.info("battery level: " + batteryLevel);
                    LogIHAB.log("battery level: " + batteryLevel);
                    Log.e(LOG, "Battery Level Info: "+batteryLevel);
                    break;

                case MSG_BATTERY_CRITICAL:
                    //TODO: Test this case
                    Logger.info("CRITICAL battery level: active");
                    LogIHAB.log("CRITICAL battery level: active");
                    if (mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.disable();
                    }
                    break;

                case MSG_CHARGING_OFF:
                    Logger.info("Charging: inactive");
                    LogIHAB.log("Charging: inactive");
                    isCharging = false;
                    mTaskHandler.removeCallbacks(mDisableBT);
                    if (!mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.enable();
                    }
                    mVibration.singleBurst();
                    // startRecording() is invoked by receiver upon completion
                    break;

                case MSG_CHARGING_ON:
                    isCharging = true;
                    Logger.info("Charging: active");
                    LogIHAB.log("Charging: active");

                    if (mBluetoothAdapter.isEnabled()) {
                        stopRecording();
                        mBluetoothAdapter.disable();
                    }
                    mVibration.singleBurst();
                    mTaskHandler.postDelayed(mDisableBT, mDisableBTTime);
                    break;

                case MSG_CHARGING_ON_PRE:
                    isCharging = true;
                    break;

                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    final Messenger mMessengerHandler = new Messenger(new MessageHandler());

    @Override
    public void onCreate() {

        Log.e(LOG, "onCreate");

        // log file
        Configurator.currentConfig()
                .writer(new FileWriter(FileIO.getFolderPath() + File.separator + FILENAME_LOG_tmp, false, true))
                .level(Level.INFO)
                .formatPattern("{date:yyyy-MM-dd_HH:mm:ss.SSS}\t{message}")
                .activate();

        Logger.info("Service onCreate");
        LogIHAB.log("Service onCreate");

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();

        Log.e(LOG,"ControlService started");
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int StartID) {
        Log.d(LOG, "onStartCommand");
        Logger.info("Service started");
        LogIHAB.log("Service started");
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
        stopAlarmAndCountdown();

        mBluetoothAdapter.disable();

        mNotificationManager.cancel(NOTIFICATION_ID);

        // Unregister broadcast listeners
        this.unregisterReceiver(mAlarmReceiver);
        this.unregisterReceiver(mBluetoothStateReceiver);

        Toast.makeText(this, "ControlService stopped", Toast.LENGTH_SHORT).show();
        Log.e(LOG,"ControlService stopped");
        Logger.info("Service stopped");
        LogIHAB.log("Service stopped");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG, "onBind");
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
        return super.onUnbind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e(LOG,"onTaskRemoved");
        stopAlarmAndCountdown();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        Log.e(LOG,"onDump");
        super.dump(fd, writer, args);
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

    private boolean checkLog() {

        // TODO: Once LogIHAB has been verified, this part (and all Logger.info()) is obsolete
        File fLog_tmp = new File(FileIO.getFolderPath() + File.separator + FILENAME_LOG_tmp);

        if (!fLog_tmp.exists()) {
            try{
                fLog_tmp.createNewFile();
                Log.d(LOG, "Log file created");
            } catch (IOException e) {
                Log.d(LOG, "Error creating Log file");
            }
        }

        new SingleMediaScanner(context, fLog_tmp);
        Log.d(LOG, "Log file checked.");




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

    private void announceBTDisconnected() {
        Log.e(LOG, "BTDEVICES not connected.");
        stopRecording();
        isBluetoothPresent = false;
        mVibration.singleBurst();
    }

    private void announceBTConnected() {
        Log.e(LOG, "BTDEVICES connected.");
        startRecording();
        isBluetoothPresent = true;
        mTaskHandler.removeCallbacks(mResetBTAdapterRunnable);
    }

    // Send message to connected client with additional data
    private void messageClient(int what, Bundle data) {

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
    private void messageClient(int what) {

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

    public void startActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void startRecording() {
        Log.d(LOG, "Start caching audio");
        Logger.info("Start caching audio");
        LogIHAB.log("Start caching audio");
        AudioFileIO.setChunkId(mChunkId);
        // A delay before starting a new recording prevents initialisation bug
        if (!getIsRecording() && !isCharging) {
            mTaskHandler.postDelayed(mStartRecordingRunnable, 1000);
        }
    }

    private void stopRecording() {

        if (getIsRecording()) {
            Log.d(LOG, "Requesting stop caching audio");
            Logger.info("Requesting stop caching audio");
            LogIHAB.log("Requesting stop caching audio");

            audioRecorder.stop();
            setIsRecording(false);
            // TODO: Experimental
            mTaskHandler.removeCallbacks(mStartRecordingRunnable);

            messageClient(MSG_STOP_RECORDING);
        }
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

    private void setupApplication() {

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

        initialiseValues();

        mFileIO = new FileIO();
        isQuestionnairePresent = mFileIO.setupFirstUse(this);

        Log.e(LOG, "Messenger Control S: "+mMessengerHandler);
        mEventTimer = new EventTimer(this, serviceMessenger); // mMessengerHandler

        mEventTimer.setTimer(10);
        mEventTimer.stopTimer();

        mVibration = new Vibration(this);
        mVibration.singleBurst();

        if (useLogMode) {
            // Register receiver for display activity (if used in log mode)
            IntentFilter displayFilter = new IntentFilter();
            displayFilter.addAction(Intent.ACTION_SCREEN_ON);
            displayFilter.addAction(Intent.ACTION_SCREEN_OFF);
            this.registerReceiver(mDisplayReceiver, displayFilter);
        }

        // Register receiver for alarm
        this.registerReceiver(mAlarmReceiver, new IntentFilter("AlarmReceived"));

        // Register broadcasts receiver for bluetooth state change
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mBluetoothStateReceiver, filter);

        // It is safe to say, that the display is illuminated on system/application startup
        if (useLogMode) {
            Logger.info("Display: on");
            LogIHAB.log("Display: on");
        }

        checkForPreferences();
    }

    // Load preset values from shared preferences, default values from external class InitValues
    private void initialiseValues() {

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
    }

    private void updatePreferences(Bundle dataPreferences) {

        // Extract preferences from data Bundle
        mSelectQuestionnaire = dataPreferences.getString("whichQuest", mSelectQuestionnaire);

        isWave = dataPreferences.getBoolean("isWave", isWave);
        isTimer = dataPreferences.getBoolean("isTimer", isTimer);
        filterHp = dataPreferences.getBoolean("filterHp", filterHp);
        filterHpFrequency = dataPreferences.getString("filterHpFrequency", "" + filterHpFrequency);
        downsample = dataPreferences.getBoolean("downsample", downsample);
        keepAudioCache = dataPreferences.getBoolean("keepAudioCache", keepAudioCache);

        samplerate = dataPreferences.getString("samplerate", "" + samplerate);
        chunklengthInS = dataPreferences.getString("chunklengthInS", "" + chunklengthInS);

        ArrayList<String> listActiveFeatures = dataPreferences.getStringArrayList("features");
        Set<String> activeFeatures = new HashSet<>();
        activeFeatures.addAll(listActiveFeatures);

        // Editor accumulates new preferences and writes them to shared preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Boolean
        editor.putBoolean("keepAudioCache", keepAudioCache);
        editor.putBoolean("isWave", isWave);
        editor.putBoolean("isTimer", isTimer);
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
        // Make changes permanent
        editor.apply();
    }

    private void checkForPreferences() {

        Bundle bundle = getPreferences();
        isTimer = bundle.getBoolean("isTimer", isTimer);

        if (!Objects.equals(mSelectQuestionnaire, mTempQuestionnaire) && !mSelectQuestionnaire.isEmpty()) {

            if (BuildConfig.DEBUG) {
                Log.i(LOG, "New Questionnaire selected.");
            }

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
        isTimer = sharedPreferences.getBoolean("isTimer", isTimer);

        // Scan file system for questionnaires
        if (isQuestionnairePresent) {
            String[] fileList = mFileIO.scanQuestOptions();

            if (fileList.length == 0) {
                Log.e(LOG, "XXX - No Questionnaires available.");
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

                Log.i(LOG, "XXX choosing: "+mSelectQuestionnaire);

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

        Bundle processingSettings = new Bundle();
        processingSettings.putBoolean("isTimer", isTimer);
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

    private void setAlarmAndCountdown() {

        mXmlReader = new XMLReader(this, mSelectQuestionnaire);
        mTimerInterval = mXmlReader.getNewTimerInterval();
        questionnaireHasTimer = mXmlReader.getQuestionnaireHasTimer();

        // Needed for the first run
        if (questionnaireHasTimer) {
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

    private void stopAlarmAndCountdown() {

        Log.e(LOG, "Cancelling Alarm.");

        isTimerRunning = false;
        mEventTimer.stopTimer();
        mVibration.repeatingBurstOff();
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
    private void startQuestionnaire(String motivation) {

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

    /**
     * Thread-safe status variables
     */

    static void setIsRecording(boolean status) {
        synchronized (recordingLock) {
            isRecording = status;
        }
    }

    static boolean getIsRecording() {
        synchronized (recordingLock) {
            return isRecording;
        }
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
     * BT optional
     */

    private String getBTMajorDeviceClass(int major){
        switch(major){
            case BluetoothClass.Device.Major.AUDIO_VIDEO:
                return "AUDIO_VIDEO";
            case BluetoothClass.Device.Major.COMPUTER:
                return "COMPUTER";
            case BluetoothClass.Device.Major.HEALTH:
                return "HEALTH";
            case BluetoothClass.Device.Major.IMAGING:
                return "IMAGING";
            case BluetoothClass.Device.Major.MISC:
                return "MISC";
            case BluetoothClass.Device.Major.NETWORKING:
                return "NETWORKING";
            case BluetoothClass.Device.Major.PERIPHERAL:
                return "PERIPHERAL";
            case BluetoothClass.Device.Major.PHONE:
                return "PHONE";
            case BluetoothClass.Device.Major.TOY:
                return "TOY";
            case BluetoothClass.Device.Major.UNCATEGORIZED:
                return "UNCATEGORIZED";
            case BluetoothClass.Device.Major.WEARABLE:
                return "AUDIO_VIDEO";
            default: return "unknown!";
        }
    }


    /**
     * RFCOMM
     */

    /*
    protected void initBluetooth() {
        BA = BluetoothAdapter.getDefaultAdapter();
        if (BA != null) {
            if (!BA.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                int REQUEST_ENABLE_BT = 1;
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }
*/

}
