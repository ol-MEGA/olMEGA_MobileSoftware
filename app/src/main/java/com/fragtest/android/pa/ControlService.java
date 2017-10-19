package com.fragtest.android.pa;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
import com.fragtest.android.pa.Core.EventTimer;
import com.fragtest.android.pa.Core.FileIO;
import com.fragtest.android.pa.Core.SingleMediaScanner;
import com.fragtest.android.pa.Core.Vibration;
import com.fragtest.android.pa.Core.XMLReader;
import com.fragtest.android.pa.Processing.MainProcessingThread;
import com.fragtest.android.pa.Questionnaire.PermissionActivity;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.FileWriter;

import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

/**
 * The brains of the operation.
 *
 * Based on https://developer.android.com/reference/android/app/Service.html
 */

public class ControlService extends Service {

    static final String LOG = "ControlService";

    /**
     * Constants for messaging. Should(!) be self-explanatory.
     */

    // 1* - general
    public static final int MSG_REGISTER_CLIENT = 11;
    public static final int MSG_UNREGISTER_CLIENT = 12;
    public static final int MSG_GET_STATUS = 13;
    public static final int MSG_SET_VISIBILITY = 14;
    public static final int MSG_NO_QUESTIONNAIRE_FOUND = 15;

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

    // Shows whether questionnaire is active - tackles lifecycle jazz
    private boolean isActiveQuestionnaire = false;
    private boolean isTimerRunning = false;
    private boolean isQuestionnairePending = false;
    private boolean isQuestionnairePresent = false;
    private boolean isMenu = true;
    private XMLReader mXmlReader;
    private Vibration mVibration;
    private String mSelectQuestionnaire, mTempQuestionnaire;

    // preferences
    private boolean isTimer, isWave, keepAudioCache, isLocked, filterHp, downsample,
            showConfigButton, showRecordingButton;

    private int samplerate, chunklengthInS, filterHpFrequency, mFinalCountDown, mTimerInterval;

    private boolean restartActivity = false; // TODO: implement in settings
    private NotificationManager mNotificationManager;

    private SharedPreferences sharedPreferences;
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

    Context context = this;

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

                    if (isQuestionnairePresent) {
                        setAlarmAndCountdown();
                    } else{
                        messageClient(MSG_NO_QUESTIONNAIRE_FOUND);
                    }
                    Bundle show = new Bundle();
                    show.putBoolean("showConfigButton", showConfigButton);
                    show.putBoolean("showRecordingButton", showRecordingButton);
                    show.putBoolean("isQuestionnairePresent", isQuestionnairePresent);

                    Log.e(LOG, "Visibility Config: "+showConfigButton);
                    Log.e(LOG, "Visibility Recording: "+showRecordingButton);
                    Log.e(LOG, "isQuestionnairePresent: "+isQuestionnairePresent);
                    messageClient(MSG_SET_VISIBILITY, show);
                    break;

                case MSG_UNREGISTER_CLIENT:
                    mClientMessenger = null; //TODO: Evaluate whether this is good
                    Logger.info("Client unregistered from service");
                    if (restartActivity) {
                        startActivity();
                    }
                    break;

                case MSG_GET_STATUS:
                    Bundle status = new Bundle();
                    status.putBoolean("isRecording", isRecording);
                    status.putBoolean("isQuestionnairePending", isQuestionnairePending);
                    status.putBoolean("showConfigButton", showConfigButton);
                    status.putBoolean("showRecordingButton", showRecordingButton);
                    status.putBoolean("isQuestionnairePresent", isQuestionnairePresent);
                    status.putBoolean("isTimer", isTimer);
                    messageClient(MSG_GET_STATUS, status);
                    break;

                case MSG_ALARM_RECEIVED:
                    messageClient(MSG_ALARM_RECEIVED);
                    // perform checks whether running a questionnaire is valid
                    if (isMenu) { //!isActiveQuestionnaire
                        messageClient(MSG_PROPOSE_QUESTIONNAIRE);
                        mVibration.repeatingBurstOn();
                    } else {
                        // React to when questionnaire is active but another one is due
                        Log.i(LOG,"Waiting for new questionnaire.");
                    }
                    isTimerRunning = false;
                    isQuestionnairePending = true;
                    break;

                case MSG_MANUAL_QUESTIONNAIRE:
                    // Check if necessary states are set for questionnaire
                    //TODO: perform checks
                    if (!isActiveQuestionnaire) {
                        startQuestionnaire("manual");
                    }
                    break;

                case MSG_PROPOSITION_ACCEPTED:
                    // User has accepted proposition to start a new questionnaire by selecting
                    // "Start Questionnaire" item in User Menu
                    //TODO: perform checks
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
                        isMenu = false;
                        Log.i(LOG, "Questionnaire active");
                    break;

                case MSG_QUESTIONNAIRE_INACTIVE:

                    isMenu = true;
                    if (isMenu) {
                        isActiveQuestionnaire = false;
                        if (isTimer) {
                            Log.i(LOG, "setAlarmAndCountdown()");
                            setAlarmAndCountdown();
                        }
                        Log.i(LOG, "Questionnaire inactive");
                    }
                    break;

                case MSG_CHECK_FOR_PREFERENCES:
                    Bundle prefs = msg.getData();
                    updatePreferences(prefs);
                    checkForPreferences();
                    break;

                case MSG_START_RECORDING:
                    Log.d(LOG, "Start caching audio");
                    Logger.info("Start caching audio");
                    audioRecorder = new AudioRecorder(
                            serviceMessenger,
                            chunklengthInS,
                            samplerate,
                            isWave);
                    audioRecorder.start();
                    isRecording = true;
                    messageClient(MSG_START_RECORDING);
                    break;

                case MSG_STOP_RECORDING:
                    Log.d(LOG, "Requesting stop caching audio");
                    Logger.info("Requesting stop caching audio");
                    audioRecorder.stop();
                    break;

                case MSG_RECORDING_STOPPED:
                    Log.d(LOG, "Stop caching audio");
                    Logger.info("Stop caching audio");
                    audioRecorder.close();
                    isRecording = false;
                    messageClient(MSG_GET_STATUS);
                    break;

                case MSG_CHUNK_RECORDED:
                    String filename = msg.getData().getString("filename");

                    addProccessingBuffer(idxRecording, filename);
                    idxRecording = (idxRecording + 1) % processingBufferSize;

                    if (!getIsProcessing()) {
                        Bundle settings = getPreferences();
                        settings.putString("filename", processingBuffer[idxProcessing]);;
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

                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    final Messenger mMessengerHandler = new Messenger(new MessageHandler());

    @Override
    public void onCreate() {

        // Load preset values
        initialiseValues();

        Log.d(LOG, "onCreate");
        // log-file
        Configurator.currentConfig()
                .writer(new FileWriter(FileIO.getFolderPath() + "/log.txt", false, true))
                .level(Level.INFO)
                .formatPattern("{date:yyyy-MM-dd_HH:mm:ss.SSS}\t{message}")
                .activate();

        Logger.info("Service onCreate");

        mFileIO = new FileIO();
        isQuestionnairePresent = mFileIO.setupFirstUse(this);
        showConfigButton = mFileIO.scanConfigMode();
        mEventTimer = new EventTimer(this, mMessengerHandler);
        mVibration = new Vibration(this);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();

        checkForPreferences();

        //Toast.makeText(this, "ControlService started", Toast.LENGTH_SHORT).show();
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
        if (isQuestionnairePresent) {
            mEventTimer.stopTimer();
            mVibration.repeatingBurstOff();
        }
        mNotificationManager.cancel(NOTIFICATION_ID);

        Toast.makeText(this, "ControlService stopped", Toast.LENGTH_SHORT).show();
        Log.e(LOG,"ControlService stopped");
        Logger.info("Service stopped");
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
        if (isQuestionnairePresent) {
            mEventTimer.stopTimer();
            mVibration.repeatingBurstOff();
        }
        super.onTaskRemoved(rootIntent);
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        Log.e(LOG,"onDump");
        super.dump(fd, writer, args);
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

    public void requestPermissions() {
        Intent intent = new Intent(this, PermissionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void startActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        //intent.putExtra("showConfigButton",showConfigButton);
        //Log.e(LOG, "shw Config: "+showConfigButton);
        startActivity(intent);
    }

    private void showNotification() {

        // Launch activiy when notification is selected
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

    private void stopAlarmAndCountdown() {
        if (isTimerRunning) {
            mEventTimer.stopTimer();
            mVibration.repeatingBurstOff();
            Bundle data = new Bundle();
            data.putBoolean("isTimer", isTimer);
            messageClient(MSG_GET_STATUS, data);
        }
    }

    // Load preset values
    private void initialiseValues() {

        PresetValues presetValues = new PresetValues(context);

        // preferences
        isTimer = presetValues.isTimer;
        isWave = presetValues.isWave;
        keepAudioCache = presetValues.keepAudioCache;
        isLocked = presetValues.isLocked;
        filterHp = presetValues.filterHp;
        downsample = presetValues.downsample;
        showConfigButton = presetValues.showConfigButton;
        showRecordingButton = presetValues.showRecordingButton;

        samplerate = presetValues.samplerate;
        chunklengthInS = presetValues.chunklengthInS;
        filterHpFrequency = presetValues.filterHpFrequency;

        mFinalCountDown = presetValues.mFinalCountDown;
        mTimerInterval = presetValues.mTimerInterval;
    }

    private void updatePreferences(Bundle dataPreferences) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Extract preferences from data Bundle
        mSelectQuestionnaire = dataPreferences.getString("whichQuest", mSelectQuestionnaire);
        samplerate = Integer.parseInt(dataPreferences.getString("samplerate", ""+samplerate));
        chunklengthInS = Integer.parseInt(dataPreferences.getString("chunklengthInS", ""+chunklengthInS));
        keepAudioCache = dataPreferences.getBoolean("keepAudioCache", false);
        isWave = dataPreferences.getBoolean("isWave", false);
        isTimer = dataPreferences.getBoolean("isTimer", false);
        //isLocked = prefs.getBoolean("isLocked", false);
        //filterHp = prefs.getBoolean("filterHp", false);
        //filterHpFrequency = prefs.getInt("filterHpFrequency", filterHpFrequency);
        //downsample = prefs.getBoolean("downsample", downsample);
        //activeFeatures = prefs.getStringArray("activeFeatures");
        Log.i(LOG, "KEEP WAVE REALLY?"+isWave);

        editor.putString("whichQuest", mSelectQuestionnaire);
        editor.putString("samplerate", ""+samplerate);
        editor.putString("chunklengthInS", ""+chunklengthInS);
        editor.putBoolean("keepAudioCache", keepAudioCache);
        editor.putBoolean("isWave", isWave);
        editor.putBoolean("isTimer", isTimer);
        //editor.putBoolean("isLocked", isLocked);
        //editor.putBoolean("filterHp", filterHp);
        //editor.putInt("filterHpFrequency", filterHpFrequency);
        //editor.putBoolean("downsample", downsample);
        //editor.putStringSet("activeFeatures", activeFeatures);
        editor.apply();
    }

    private void checkForPreferences() {

        Bundle bundle = getPreferences();
        isLocked = bundle.getBoolean("isLocked", false);
        isTimer = bundle.getBoolean("isTimer", false);

        //TODO: Transform this to a simple shared preference setting
        // deletes rules.ini
        if (isLocked) {
            showConfigButton = !isLocked;
            mFileIO.lockPreferences();
            Bundle dataVisibility = new Bundle();
            dataVisibility.putBoolean("showConfigButton", showConfigButton);
            dataVisibility.putBoolean("showRecordingButton", showRecordingButton);
            //dataVisibility.putBoolean("isQuestionnairePresent", isQuestionnairePresent);
            messageClient(MSG_SET_VISIBILITY, dataVisibility);
        }

        Log.i(LOG, "Questionnaire to be displayed: "+mSelectQuestionnaire);

        if (!Objects.equals(mSelectQuestionnaire, mTempQuestionnaire)) {

            Log.i(LOG, "Questionnaire is not the same.");

            mTempQuestionnaire = mSelectQuestionnaire;
            // Reads new XML file
            renewQuestionnaire();

            //TODO: Needed?
            isTimerRunning = false;

            if (isTimer) {
                setAlarmAndCountdown();
            } else{
                stopAlarmAndCountdown();
            }

        }

        Bundle data = new Bundle();
        data.putBoolean("showConfigButton", showConfigButton);
        data.putBoolean("showRecordingButton", showRecordingButton);
        data.putBoolean("isQuestionnairePresent", isQuestionnairePresent);
        messageClient(MSG_RESET_MENU, data);
    }


    private Bundle getPreferences() {

        isQuestionnairePresent = mFileIO.setupFirstUse(this);
        Log.i(LOG, "Questionnaire present: "+isQuestionnairePresent);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //sharedPreferences = getSharedPreferences(sharedPreferencesName, MODE_MULTI_PROCESS);

        // recording
        samplerate = Integer.parseInt(sharedPreferences.getString("samplerate", ""+samplerate));
        chunklengthInS = Integer.parseInt(sharedPreferences.getString("chunklengthInS", ""+chunklengthInS));
        keepAudioCache = sharedPreferences.getBoolean("keepAudioCache", false);
        isWave = sharedPreferences.getBoolean("isWave", false);

        //TODO: For some reason this is only updated after relaunch -> maybe due to service/activity
        // Use automatic timer
        isTimer = sharedPreferences.getBoolean("isTimer", false);
        // Show preferences button
        isLocked = sharedPreferences.getBoolean("isLocked", false);

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
                    Log.i(LOG, "Using default questionnaire: " + mSelectQuestionnaire);
                }
            }

        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("whichQuest", "").apply();
            Log.e(LOG, "No Questionnaires available.");
            messageClient(MSG_NO_QUESTIONNAIRE_FOUND);
        }

        // processing
        HashSet<String> activeFeatures =
                (HashSet<String>) sharedPreferences.getStringSet("features", null);
        filterHp = sharedPreferences.getBoolean("filterHp", true);
        //int filterHpFrequency = Integer.parseInt(sharedPreferences.getString("filterHpFrequency", "100"));
        //Boolean downsample = sharedPreferences.getBoolean("downsample", false);

        // TODO: bundle up everything for initial adaptation of processing stack.
        // TODO: how to clean this up? read SharedPreferences from processing stack (synchronise?!)?
        Bundle processingSettings = new Bundle();
        processingSettings.putBoolean("isTimer", isTimer);
        processingSettings.putInt("samplerate", samplerate);
        processingSettings.putInt("chunklengthInS", chunklengthInS);
        processingSettings.putBoolean("isWave", isWave);
        processingSettings.putSerializable("activeFeatures", activeFeatures);
        processingSettings.putBoolean("filterHp", filterHp);
        processingSettings.putInt("filterHpFrequency", filterHpFrequency);
        processingSettings.putBoolean("downsample", downsample);
        processingSettings.putString("whichQuest", mSelectQuestionnaire);

        return processingSettings;
    }

    private void setAlarmAndCountdown() {

        Log.i(LOG, "Timer present: "+isTimer);
        Log.i(LOG, "Questionnaire present: "+isQuestionnairePresent);

        if (isQuestionnairePresent && isTimer) {

            mXmlReader = new XMLReader(this, mSelectQuestionnaire);
            mTimerInterval = mXmlReader.getNewTimerInterval();

            if (!isTimerRunning) {

                mEventTimer.stopTimer();
                mVibration.repeatingBurstOff();
                mEventTimer.setTimer(mTimerInterval);
                mFinalCountDown = mEventTimer.getFinalCountDown();
                isTimerRunning = true;
                Log.e(LOG, "Timer set to " + mTimerInterval + "s");
            } else {
                // Usually when app is restarted
                Log.i(LOG, "Timer already set. Reinstating countdown");
            }

            // Send message to initialise / update timer
            Bundle data = new Bundle();
            data.putInt("finalCountDown", mFinalCountDown);
            data.putInt("countDownInterval", mTimerInterval);
            data.putBoolean("showConfigButton", showConfigButton);
            data.putBoolean("showRecordingButton", showRecordingButton);
            data.putBoolean("isQuestionnairePresent", isQuestionnairePresent);
            messageClient(MSG_START_COUNTDOWN, data);
            Log.i(LOG, "Countdown set.");
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
            data.putString("motivation", "<motivation='" + motivation + "'>");

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


}
