package com.fragtest.android.pa;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
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
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

/**
 * The brains of the operation.
 *
 * Based on https://developer.android.com/reference/android/app/Service.html
 */

public class ControlService extends Service {

    static final String LOG = "ControlService";
    static final boolean needsBluetooth = true;

    /**
     * Constants for messaging. Should(!) be self-explanatory.
     */

    // 1* - general
    public static final int MSG_REGISTER_CLIENT = 11;
    public static final int MSG_UNREGISTER_CLIENT = 12;
    public static final int MSG_GET_STATUS = 13;
    public static final int MSG_SET_VISIBILITY = 14;
    public static final int MSG_NO_QUESTIONNAIRE_FOUND = 15;
    public static final int MSG_NO_TIMER = 16;
    public static final int MSG_CHANGE_PREFERENCE = 17;
    public static final int MSG_BT_CONNECTED = 18;
    public static final int MSG_BT_DISCONNECTED = 19;

    // 2* - alarm
    public static final int MSG_ALARM_RECEIVED = 21;
    public static final int MSG_START_COUNTDOWN = 22;

    // 3* - questionnaire
    public static final int MSG_RESET_MENU = 30;
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
    public static final int MSG_TIME_PLAUSIBLE = 67;

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
    private static boolean isCharging = false;

    public static final String FILENAME_LOG = "log.txt";

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
    private int mDelayDateTime = 10*1000;
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

    public static final boolean useLogMode = true;

    Context context = this;


    private Runnable mDateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            putTime();
            mTaskHandler.postDelayed(mDateTimeRunnable, mDelayDateTime);
        }
    };

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
                    break;
                case "android.intent.action.SCREEN_OFF":
                    Logger.info("Display: off");
                    break;
            }
        }
    };

    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //.makeText(getApplicationContext(), "Device found.", Toast.LENGTH_SHORT).show();
                Log.e(LOG, "BTDEVICES found.");
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Toast.makeText(getApplicationContext(), "Device connected.", Toast.LENGTH_SHORT).show();
                announceBTConnected();
                Logger.info("Bluetooth: connected");
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Toast.makeText(getApplicationContext(), "Discovery finished.", Toast.LENGTH_SHORT).show();
                Log.e(LOG, "BTDEVICES finished.");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //Toast.makeText(getApplicationContext(), "Device about to disconnect.", Toast.LENGTH_SHORT).show();
                Log.e(LOG, "BTDEVICES about to disconnect.");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Toast.makeText(getApplicationContext(), "Device disconected.", Toast.LENGTH_SHORT).show();
                announceBTDisconnected();
                Logger.info("Bluetooth: disconnected");
            }
        }
    };

    class MessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            Log.d(LOG, "Received Message: " + msg.what);

            switch (msg.what) {

                case MSG_REGISTER_CLIENT:

                    Log.e(LOG,"msg: "+msg);
                    Log.i(LOG, "Client registered to service");
                    Logger.info("Client registered to service");
                    mClientMessenger = msg.replyTo;

                    setupApplication();
                    mTaskHandler.post(mDateTimeRunnable);

                    // Bundled information about visible contents
                    Bundle bundleShow = new Bundle();
                    bundleShow.putBoolean("showConfigButton", showConfigButton);
                    bundleShow.putBoolean("showRecordingButton", showRecordingButton);
                    bundleShow.putBoolean("isQuestionnairePresent", isQuestionnairePresent);

                    messageClient(MSG_SET_VISIBILITY, bundleShow);

                    // Set and announce bluetooth disabled - then enable it to force recognition via
                    // broadcast receiver. This way, a connection can be made with an already
                    // running transmitter
                    mBluetoothAdapter.disable();
                    messageClient(MSG_BT_DISCONNECTED);

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
                    break;

                case MSG_UNREGISTER_CLIENT:
                    mClientMessenger = null;
                    Logger.info("Client unregistered from service");
                    if (restartActivity) {
                        startActivity();
                    } else {
                        mBluetoothAdapter.disable();
                    }
                    break;

                case MSG_GET_STATUS:
                    Bundle status = new Bundle();
                    status.putBoolean("isRecording", isRecording);
                    messageClient(MSG_GET_STATUS, status);
                    break;

                case MSG_MANUAL_QUESTIONNAIRE:
                    // User has initiated questionnaire manually without/before timer
                    if (!isActiveQuestionnaire && isBluetoothPresent) {
                        startQuestionnaire("manual");
                    }
                    break;

                case MSG_PROPOSITION_ACCEPTED:
                    // User has accepted proposition to start a new questionnaire by selecting
                    // "Start Questionnaire" item in User Menu
                    mVibration.repeatingBurstOff();
                    if (!isActiveQuestionnaire) {
                        startQuestionnaire("auto");
                    }
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

                case MSG_QUESTIONNAIRE_INACTIVE:
                    // In case questionnaires are no longer present, program crashes..
                    // but shouldn't be a problem since it is destructive user interference (DUI).
                    isMenu = true;
                    isActiveQuestionnaire = false;
                    if (isTimer && isBluetoothPresent) {
                        setAlarmAndCountdown();
                    } else if (isBluetoothPresent) {
                        messageClient(MSG_NO_TIMER);
                    } else {
                        messageClient(MSG_BT_DISCONNECTED);
                    }
                    break;

                case MSG_CHECK_FOR_PREFERENCES:
                    Bundle prefs = msg.getData();
                    updatePreferences(prefs);
                    checkForPreferences();
                    break;

                case MSG_START_RECORDING:
                    startRecording();
                    break;

                case MSG_STOP_RECORDING:
                    stopRecording();
                    break;

                case MSG_RECORDING_STOPPED:
                    Log.d(LOG, "Stop caching audio");
                    Logger.info("Stop caching audio");
                    audioRecorder.close();
                    isRecording = false;
                    messageClient(MSG_GET_STATUS);
                    break;

                case MSG_CHUNK_RECORDED:

                    AudioFileIO.setChunkId(getChunkId());
                    String filename = msg.getData().getString("filename");
                    addProccessingBuffer(idxRecording, filename);
                    idxRecording = (idxRecording + 1) % processingBufferSize;

                    if (!getIsProcessing()) {
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

                    //just to keep sure for now
                    //messageClient(MSG_BT_CONNECTED);

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
                    //stopRecording(); is called by receiver
                    if (mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.disable();
                    }
                    break;

                case MSG_BATTERY_LEVEL_INFO:
                    float batteryLevel = msg.getData().getFloat("batteryLevel");
                    Logger.info("battery level: " + batteryLevel);
                    Log.e(LOG, "Battery Level Info: "+batteryLevel);
                    break;

                case MSG_BATTERY_CRITICAL:
                    //TODO: Test this case
                    Logger.info("CRITICAL battery level: active");
                    if (mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.disable();
                    }
                    break;

                case MSG_CHARGING_OFF:
                    Log.e(LOG, "CHARGINNNN OFF");
                    Logger.info("Charging: inactive");
                    isCharging = false;
                    if (!mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.enable();
                    }
                    mVibration.singleBurst();
                    break;

                case MSG_CHARGING_ON:
                    Log.e(LOG, "CHARGINNNN ON");
                    isCharging = true;
                    Logger.info("Charging: active");
                    stopRecording();
                    if (mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.disable();
                    }
                    mVibration.singleBurst();
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
                .writer(new FileWriter(FileIO.getFolderPath() + File.separator + FILENAME_LOG, false, true))
                .level(Level.INFO)
                .formatPattern("{date:yyyy-MM-dd_HH:mm:ss.SSS}\t{message}")
                .activate();

        Logger.info("Service onCreate");

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();

        Log.e(LOG,"ControlService started");
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
        stopAlarmAndCountdown();

        mBluetoothAdapter.disable();

        mNotificationManager.cancel(NOTIFICATION_ID);

        // Unregister broadcast listeners
        this.unregisterReceiver(mAlarmReceiver);
        this.unregisterReceiver(mBluetoothStateReceiver);

        Toast.makeText(this, "ControlService stopped", Toast.LENGTH_SHORT).show();
        Log.e(LOG,"ControlService stopped");
        Logger.info("Service stopped");

        //mBluetoothAdapter.cancelDiscovery();
        //mBluetoothAdapter.disable();
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
        int chunkId = sharedPreferences.getInt("chunkId", 0);
        if (chunkId < 999999) {
            sharedPreferences.edit().putInt("chunkId", chunkId + 1).apply();
        } else {
            sharedPreferences.edit().putInt("chunkId", 1).apply();
        }
        return chunkId;
    }

    private boolean timePlausible() {


        int year = Calendar.getInstance().get(Calendar.YEAR);

        Log.e(LOG, "YEAR: "+year);
        if (year < 2018) {
            Bundle timeBundle = new Bundle();
            timeBundle.putBoolean("timePlausible", false);
            messageClient(MSG_TIME_PLAUSIBLE, timeBundle);
            return false;
        } else {
            return true;
        }
    }

    private void putTime() {

        timePlausible();

        dateTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
        Date dateNew = dateTime.getTime();
        try {
            Date dateOld = DATE_FORMAT.parse(sharedPreferences.getString("currentDateTime", "0"));
            if (dateNew.after(dateOld)) {
                sharedPreferences.edit().putString("currentDateTime", DATE_FORMAT.format(dateNew)).apply();
                Logger.info("Time put: " + DATE_FORMAT.format(dateNew));
            } else {
                Log.e(LOG, "Date was set back!!!");
                Logger.info("Device time reset: " + DATE_FORMAT.format(dateNew));
                sharedPreferences.edit().putString("currentDateTime", DATE_FORMAT.format(dateNew)).apply();
            }
        } catch (ParseException p) {
            Log.e(LOG, "Parsing Exception: " + p.getMessage());
        }

    }

    private void announceBTDisconnected() {
        Log.e(LOG, "BTDEVICES not connected.");
        stopRecording();
        isBluetoothPresent = false;
        messageClient(MSG_BT_DISCONNECTED);
        stopAlarmAndCountdown();
        mVibration.singleBurst();
    }

    private void announceBTConnected() {
        Log.e(LOG, "BTDEVICES connected.");
        startRecording();
        isBluetoothPresent = true;
        messageClient(MSG_BT_CONNECTED);
        setAlarmAndCountdown();
        mVibration.singleBurst();
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

    public static void setCharging(boolean charging) {
        isCharging = charging;
    }

    public void startActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void startRecording() {
        if (isQuestionnairePresent) {
            Log.d(LOG, "Start caching audio");
            Logger.info("Start caching audio");

            audioRecorder = new AudioRecorder(
                    serviceMessenger,
                    Integer.parseInt(chunklengthInS),
                    Integer.parseInt(samplerate),
                    isWave);

            audioRecorder.start();
            isRecording = true;
            messageClient(MSG_START_RECORDING);
        }
    }

    private void stopRecording() {
        Log.d(LOG, "Requesting stop caching audio");
        Logger.info("Requesting stop caching audio");
        if (isRecording) {
            audioRecorder.stop();
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

        // If no chunk Id in present shared preferences, initialise with 1
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getInt("chunkId", 0) == 0) {
            sharedPreferences.edit().putInt("chunkId", 1).apply();
        }

        if (sharedPreferences.getString("currentDateTime", "") == "") {
            dateTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
            sharedPreferences.edit().putString("currentDateTime", DATE_FORMAT.format(dateTime.getTime())).apply();
        } else {
            putTime();
        }

        initialiseValues();

        mFileIO = new FileIO();
        isQuestionnairePresent = mFileIO.setupFirstUse(this);

        Log.e(LOG, "Messenger Control S: "+mMessengerHandler);
        mEventTimer = new EventTimer(this, serviceMessenger); // mMessengerHandler
        mVibration = new Vibration(this);

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
        }

        //mBluetoothAdapter.disable();

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

    private void setSinglePreference(String key, boolean value) {
        Bundle data = new Bundle();
        data.putString("type", "boolean");
        data.putString("key", key);
        data.putBoolean(key, value);
        sharedPreferences.edit().putBoolean(key, value).apply();
        messageClient(MSG_CHANGE_PREFERENCE, data);
    }

    private void updatePreferences(Bundle dataPreferences) {
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

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

        if (isTimer && questionnaireHasTimer) {
            setAlarmAndCountdown();
        } else {
            stopAlarmAndCountdown();
        }
        messageClient(MSG_RESET_MENU);
    }

    private Bundle getPreferences() {

        isQuestionnairePresent = mFileIO.setupFirstUse(this);

        //sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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
                Log.e(LOG, "No Questionnaires available.");
                messageClient(MSG_NO_QUESTIONNAIRE_FOUND);
                isQuestionnairePresent = false;
            } else {
                // Load questionnaire if selected, otherwise load default
                mSelectQuestionnaire = sharedPreferences.getString("whichQuest", fileList[0]);

                if (mTempQuestionnaire == null || mTempQuestionnaire.isEmpty()) {
                    mTempQuestionnaire = "";
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
       HashSet<String> activeFeatures =
                (HashSet<String>) sharedPreferences.getStringSet("features", null);

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

        if (isQuestionnairePresent && isTimer && isBluetoothPresent) {

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
                    if (BuildConfig.DEBUG) {
                        Log.e(LOG, "Timer set to " + mTimerInterval + "s");
                    }
                } else {
                    // Usually when app is restarted
                    if (BuildConfig.DEBUG) {
                        Log.i(LOG, "Timer already set. Reinstating countdown");
                    }
                }
            } else {
                messageClient(MSG_NO_TIMER);
            }

            // Send message to initialise / update timer
            Bundle data = new Bundle();
            data.putInt("finalCountDown", mFinalCountDown);
            data.putInt("countDownInterval", mTimerInterval);
            messageClient(MSG_START_COUNTDOWN, data);
        }
    }

    private void stopAlarmAndCountdown() {

        Log.e(LOG, "Cancelling Alarm.");

        messageClient(MSG_NO_TIMER);
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
}
