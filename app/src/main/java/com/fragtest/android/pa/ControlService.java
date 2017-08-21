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

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.FileWriter;

import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;

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

    // 2* - alarm
    public static final int MSG_ALARM_RECEIVED = 21;
    public static final int MSG_START_COUNTDOWN = 22;

    // 3* - questionnaire
    public static final int MSG_ISMENU = 31;
    public static final int MSG_QUESTIONNAIRE_INACTIVE = 32;
    public static final int MSG_START_QUESTIONNAIRE = 33;
    public static final int MSG_PROPOSE_QUESTIONNAIRE = 34;
    public static final int MSG_PROPOSITION_ACCEPTED = 35;
    public static final int MSG_MANUAL_QUESTIONNAIRE = 36;
    public static final int MSG_QUESTIONNAIRE_ACTIVE = 37;

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
    private boolean isMenu = true;
    private XMLReader mXmlReader;
    private Vibration mVibration;

    // preferences
    private boolean isTimer, isWave, keepAudioCache;
    private int samplerate, chunklengthInS;


    private int mFinalCountDown = -255;
    private int mTimerInterval = -255;

    private boolean restartActivity = false; // TODO: implement in settings
    private NotificationManager mNotificationManager;

    // Questionnaire-Timer
    EventTimer mEventTimer;

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
                    Logger.info("Client registered to service");
                    mClientMessenger = msg.replyTo;
                    if (isTimer && !isQuestionnairePending) {
                        setAlarmAndCountdown();
                    }
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
                    messageClient(MSG_GET_STATUS, status);
                    break;





                /** TIMER DISPLAY IS NOT SYNCHRONISED WHEN Quest -> Square -> Choose -> IHA **/




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
                    //if (isMenu) { //!isActiveQuestionnaire
                        isActiveQuestionnaire = true;
                        mEventTimer.stopTimer();
                        isTimerRunning = false;
                        isMenu = false;
                        Log.i(LOG, "Questionnaire active");
                    //}
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

        Log.d(LOG, "onCreate");

        // log-file
        Configurator.currentConfig()
                .writer(new FileWriter(FileIO.getFolderPath() + "/log.txt", false, true))
                .level(Level.INFO)
                .formatPattern("{date:yyyy-MM-dd_HH:mm:ss.SSS}\t{message}")
                .activate();

        getPreferences();

        Logger.info("Service onCreate");

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();
        Toast.makeText(this, "ControlService started", Toast.LENGTH_SHORT).show();

        mXmlReader = new XMLReader(this);
        mEventTimer = new EventTimer(this, mMessengerHandler);
        mVibration = new Vibration(this);

        Toast.makeText(this, "ControlService started", Toast.LENGTH_SHORT).show();
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
        mEventTimer.stopTimer();
        mVibration.repeatingBurstOff();
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
        mEventTimer.stopTimer();
        mVibration.repeatingBurstOff();
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

    public void startActivity() {
        Intent intent = new Intent(this, MainActivity.class);
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

    private void setAlarmAndCountdown() {

        if (!isTimerRunning) {
            mTimerInterval = mXmlReader.getNewTimerInterval();
            mEventTimer.setTimer(mTimerInterval);
            mFinalCountDown = mEventTimer.getFinalCountDown();
            isTimerRunning = true;
        } else {
            // Usually when app is restarted
            Log.i(LOG, "Timer already set. Reinstating countdown");
        }

        // Send message to initialise / update timer
        Bundle data = new Bundle();
        data.putInt("finalCountDown", mFinalCountDown);
        data.putInt("countDownInterval", mTimerInterval);
        messageClient(MSG_START_COUNTDOWN, data);
        Log.e(LOG, "Timer set to " + mTimerInterval + "s");
    }

    private void startQuestionnaire(String motivation) {

        Bundle data = new Bundle();
        ArrayList<String> questionList = mXmlReader.getQuestionList();
        data.putStringArrayList("questionList",questionList);
        String head = mXmlReader.getHead();
        data.putString("head", head);
        data.putString("motivation", "<motivation='"+ motivation +"'>");
        messageClient(MSG_START_QUESTIONNAIRE, data);
        Log.i(LOG,"Questionnaire initiated: " + motivation);

        isQuestionnairePending = false;

    }

    private Bundle getPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // timer and questionnaire
        isTimer = sharedPreferences.getBoolean("isTimer", true);

        // recording
        samplerate = Integer.parseInt(sharedPreferences.getString("samplerate", "16000"));
        chunklengthInS = Integer.parseInt(sharedPreferences.getString("chunklengthInS", "60"));
        keepAudioCache = sharedPreferences.getBoolean("keepAudioCache", false);
        isWave = sharedPreferences.getBoolean("isWave", false);

        // processing
        HashSet<String> activeFeatures =
                (HashSet<String>) sharedPreferences.getStringSet("features", null);
        Boolean filterHp = sharedPreferences.getBoolean("filterHp", true);
        int filterHpFrequency = Integer.parseInt(sharedPreferences.getString("filterHpFrequency", "100"));
        Boolean downsample = sharedPreferences.getBoolean("downsample", false);

        // TODO: bundle up everything for initial adaptation of processing stack.
        // TODO: how to clean this up? read SharedPreferences from processing stack (synchronise?!)?
        Bundle processingSettings = new Bundle();
        // processingSettings.putBoolean("isTimer", isTimer);
        processingSettings.putInt("samplerate", samplerate);
        processingSettings.putInt("chunklengthInS", chunklengthInS);
        // processingSettings.putBoolean("isWave", isWave);
        processingSettings.putSerializable("activeFeatures", activeFeatures);
        processingSettings.putBoolean("filterHp", filterHp);
        processingSettings.putInt("filterHpFrequency", filterHpFrequency);
        processingSettings.putBoolean("downsample", downsample);

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
