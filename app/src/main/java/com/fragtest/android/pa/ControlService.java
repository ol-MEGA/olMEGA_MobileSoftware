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
import com.fragtest.android.pa.Core.Vibration;
import com.fragtest.android.pa.Core.XMLReader;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

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

    static boolean isRecording = false;
    private boolean isCountDownRunning = false;
    private XMLReader mXmlReader;
    private Vibration mVibration;

    private int mFinalCountDown = -255;
    private int mTimerInterval = -255;

    // Shows whether questionnaire is active - tackles lifecycle jazz
    private boolean isActiveQuestionnaire = false;
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

            switch (msg.what) {

                case MSG_REGISTER_CLIENT:
                    mClientMessenger = msg.replyTo;
                    setAlarmAndCountdown();
                    break;

                case MSG_UNREGISTER_CLIENT:
                    mClientMessenger = null;

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
                    isCountDownRunning = false;

                    if (true) {
                        // perform checks whether running a questionnaire is valid
                        messageClient(MSG_PROPOSE_QUESTIONNAIRE);
                        mVibration.repeatingBurstOn();
                    }

                    break;

                case MSG_NEW_ALARM:
                    isCountDownRunning = false;
                    setAlarmAndCountdown();
                    break;


                case MSG_MANUAL_QUESTIONNAIRE:
                    // Check if necessary states are set for questionnaire [TO DO]
                    // ... perform checks
                    if (true) {
                        Bundle data = new Bundle();
                        ArrayList<String> questionList = mXmlReader.getQuestionList();
                        data.putStringArrayList("questionList",questionList);
                        messageClient(MSG_START_QUESTIONNAIRE, data);
                    }
                    break;

                case MSG_PROPOSITION_ACCEPTED:
                    // User has accepted proposition to start a new questionnaire by selecting
                    // "Start Questionnaire" item in User Menu
                    mVibration.repeatingBurstOff();
                    // Send questionnaire data to questionnaire class to create a new ... tadaa ...
                    // questionnaire
                    Bundle data = new Bundle();
                    ArrayList<String> questionList = mXmlReader.getQuestionList();
                    data.putStringArrayList("questionList",questionList);
                    messageClient(MSG_START_QUESTIONNAIRE, data);
                    break;

                case MSG_QUESTIONNAIRE_ACTIVE:
                    isActiveQuestionnaire = true;
                    mVibration.repeatingBurstOff();
                    Log.i(LOG,"Questionnaire active - alarm shut off.");
                    break;

                case MSG_QUESTIONNAIRE_INACTIVE:
                    isActiveQuestionnaire = false;
                    Log.i(LOG,"Questionnaire inactive");
                    break;

                case MSG_GET_FINAL_COUNTDOWN:
                    Bundle dataCountDown = new Bundle();
                    dataCountDown.putInt("finalCountDown",mEventTimer.getFinalCountDown());
                    dataCountDown.putInt("countDownInterval",mTimerInterval);
                    messageClient(MSG_SET_FINAL_COUNTDOWN, dataCountDown);
                    Log.e(LOG,"Sending FCD");

                case MSG_START_RECORDING:
                    Log.d(LOG, "Start Recording.");
                    audioRecorder = new AudioRecorder(serviceMessenger, 16000);
                    audioRecorder.start();
                    isRecording = true;
                    messageClient(MSG_START_RECORDING);
                    break;

                case MSG_STOP_RECORDING:
                    Log.d(LOG, "Stop Recording.");
                    audioRecorder.stop();
                    audioRecorder.close();
                    isRecording = false;
                    messageClient(MSG_STOP_RECORDING);
                    break;

                case MSG_BLOCK_RECORDED:
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
        if (!isCountDownRunning || !isActiveQuestionnaire) {
            mTimerInterval = mXmlReader.getNewTimerInterval();
            mEventTimer.setTimer(mTimerInterval);
            mFinalCountDown = mEventTimer.getFinalCountDown();

            Bundle data = new Bundle();
            data.putInt("timerInterval", mFinalCountDown);
            // Send message to initialise new functional timer
            messageClient(ControlService.MSG_START_COUNTDOWN, data);
            Log.e(LOG, "Timer set to " + mTimerInterval + "s");

            isCountDownRunning = true;
        } else {
            Bundle data = new Bundle();
            data.putInt("timerInterval", mFinalCountDown);
            // Send message to initialise new functional timer
            messageClient(ControlService.MSG_START_COUNTDOWN, data);
        }
    }
}
