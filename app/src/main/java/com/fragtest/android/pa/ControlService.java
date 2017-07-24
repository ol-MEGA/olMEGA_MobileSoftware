package com.fragtest.android.pa;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.fragtest.android.pa.Core.EventTimer;
import com.fragtest.android.pa.Core.FileIO;
import com.fragtest.android.pa.Core.Vibration;
import com.fragtest.android.pa.Core.XMLReader;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;


import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.FileWriter;

/**
 * The brains of the operation.
 *
 * Based on https://developer.android.com/reference/android/app/Service.html
 */

public class ControlService extends Service {

    static final String LOG = "ControlService";

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_GET_STATUS = 3;
    public static final int MSG_ALARM_RECEIVED = 4;
    public static final int MSG_START_COUNTDOWN = 5;
    public static final int MSG_NEW_ALARM = 6;
    public static final int MSG_ARE_WE_RUNNING = 7;
    public static final int MSG_START_QUESTIONNAIRE = 8;
    public static final int MSG_START_RECORDING = 9;
    public static final int MSG_STOP_RECORDING = 10;
    public static final int MSG_RECORDING_STOPPED = 20;
    public static final int MSG_STATUS = 11;
    public static final int MSG_BLOCK_RECORDED = 12;
    public static final int MSG_PROPOSE_QUESTIONNAIRE = 13;
    public static final int MSG_PROPOSITION_ACCEPTED = 14;
    public static final int MSG_MANUAL_QUESTIONNAIRE = 15;
    public static final int MSG_QUESTIONNAIRE_ACTIVE = 16;
    public static final int MSG_QUESTIONNAIRE_INACTIVE = 17;
    public static final int MSG_GET_FINAL_COUNTDOWN = 18;
    public static final int MSG_SET_FINAL_COUNTDOWN = 19;
    public static final int MSG_FINAL_COUNTDOWN_SET = 20;

    // Shows whether questionnaire is active - tackles lifecycle jazz
    private boolean isActiveQuestionnaire = false;
    static boolean isRecording = false;
    private boolean isTimerRunning = false;
    private XMLReader mXmlReader;
    private Vibration mVibration;

    private int mFinalCountDown = -255;
    private int mTimerInterval = -255;

    private boolean restartActivity = false; // TODO: implement in settings
    private NotificationManager mNotificationManager;

    protected static final Object lock = new Object();

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

    class MessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            Log.d(LOG, "Received Message: " + msg.what);
            Log.d(LOG, "TID: " + android.os.Process.myTid());
            Log.d(LOG, "PID: " + android.os.Process.myPid());

            Logger.info("Message received:\t{}", msg.what);

            switch (msg.what) {

                case MSG_REGISTER_CLIENT:
                    mClientMessenger = msg.replyTo;
                    setAlarmAndCountdown();
                    break;

                case MSG_UNREGISTER_CLIENT:
                    mClientMessenger = null; //TODO: Evaluate whether this is good

                    if (restartActivity) {
                        startActivity();
                    }
                    break;

                case MSG_GET_STATUS:
                    Bundle status = new Bundle();
                    status.putBoolean("isRecording", isRecording);
                    messageClient(MSG_STATUS, status);
                    break;

                case MSG_ALARM_RECEIVED:
                    messageClient(MSG_ALARM_RECEIVED);
                    Log.i(LOG,"isRecording: "+isRecording);
                    // perform checks whether running a questionnaire is valid
                    if (true && !isActiveQuestionnaire) {
                        messageClient(MSG_PROPOSE_QUESTIONNAIRE);
                        mVibration.repeatingBurstOn();
                    } else {
                        // React to when questionnaire is active but another one is due
                        Log.i(LOG,"Waiting for new questionnaire.");
                    }
                    isTimerRunning = false;
                    break;

                case MSG_MANUAL_QUESTIONNAIRE:
                    // Check if necessary states are set for questionnaire
                    //TODO: perform checks
                    if (true && !isActiveQuestionnaire) {
                        Bundle data = new Bundle();
                        ArrayList<String> questionList = mXmlReader.getQuestionList();
                        data.putStringArrayList("questionList",questionList);
                        String head = mXmlReader.getHead();
                        data.putString("head", head);
                        data.putString("motivation", "<motivation='manual'>");
                        messageClient(MSG_START_QUESTIONNAIRE, data);
                        Log.i(LOG,"Manual questionnaire initiated.");
                    }
                    break;

                case MSG_PROPOSITION_ACCEPTED:
                    // User has accepted proposition to start a new questionnaire by selecting
                    // "Start Questionnaire" item in User Menu
                    //TODO: perform checks
                    if (true && !isActiveQuestionnaire) {
                        mVibration.repeatingBurstOff();
                        // Send questionnaire data to questionnaire class to create a new ... tadaa ...
                        // questionnaire
                        Bundle data = new Bundle();
                        ArrayList<String> questionList = mXmlReader.getQuestionList();
                        data.putStringArrayList("questionList", questionList);
                        String head = mXmlReader.getHead();
                        data.putString("head", head);
                        data.putString("motivation","<motivation='auto'>");
                        messageClient(MSG_START_QUESTIONNAIRE, data);
                        Log.i(LOG, "Recurring questionnaire initiated.");
                    }
                    break;

                case MSG_QUESTIONNAIRE_ACTIVE:
                    isActiveQuestionnaire = true;
                    mEventTimer.stopTimer();
                    isTimerRunning = false;
                    //mVibration.repeatingBurstOff();
                    Log.i(LOG,"Questionnaire active");
                    break;

                case MSG_QUESTIONNAIRE_INACTIVE:
                    isActiveQuestionnaire = false;
                    setAlarmAndCountdown();
                    Log.i(LOG,"Questionnaire inactive");
                    break;

                case MSG_START_RECORDING:
                    Log.d(LOG, "Start Recording.");

                    int blocklengthInMs = 5000;
                    int samplerate = 16000;
                    boolean isWave = false;

                    audioRecorder = new AudioRecorder(
                            serviceMessenger,
                            blocklengthInMs,
                            samplerate,
                            isWave);
                    audioRecorder.start();
                    isRecording = true;
                    messageClient(MSG_START_RECORDING);
                    break;

                case MSG_STOP_RECORDING:
                    Log.d(LOG, "Stop Recording.");
                    audioRecorder.stop();
                    break;

                case MSG_RECORDING_STOPPED:
                    audioRecorder.close();
                    isRecording = false;
                    messageClient(MSG_STOP_RECORDING);
                    break;

                case MSG_BLOCK_RECORDED:
                    String filename = msg.getData().getString("filename");
                    Log.d(LOG, "Recorded: " + filename);
                    Logger.info("New cache:\t{}", filename);
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

        Configurator.currentConfig()
                .writer(new FileWriter(FileIO.getFolderPath() + "/log.txt", false, true))
                .level(Level.INFO)
                .formatPattern("{date:yyyy-MM-dd_HH:mm:ss.SSS}\t{message}")
                .activate();

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
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG, "onDestroy");
        mEventTimer.stopTimer();
        mVibration.repeatingBurstOff();
        mNotificationManager.cancel(NOTIFICATION_ID);
        Toast.makeText(this, "ControlService stopped", Toast.LENGTH_SHORT).show();
        Log.e(LOG,"ControlService stopped");
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

            // Send message to initialise new functional timer
            Bundle data = new Bundle();
            data.putInt("finalCountDown", mFinalCountDown);
            data.putInt("countDownInterval", mTimerInterval);
            messageClient(ControlService.MSG_START_COUNTDOWN, data);
            Log.e(LOG, "Timer set to " + mTimerInterval + "s");

            isTimerRunning = true;
        } else {
            // Usually when app is restarted
            Log.i(LOG, "Timer already set. Reinstating countdown");
            // Send message to initialise new functional timer
            Bundle data = new Bundle();
            data.putInt("finalCountDown", mFinalCountDown);
            data.putInt("countDownInterval", mTimerInterval);
            messageClient(ControlService.MSG_START_COUNTDOWN, data);
            Log.e(LOG, "Timer set to " + mTimerInterval + "s");
        }
    }

}
