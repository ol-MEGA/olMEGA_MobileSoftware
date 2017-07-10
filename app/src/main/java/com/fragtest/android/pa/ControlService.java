package com.fragtest.android.pa;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.fragtest.android.pa.Core.EventTimer;

/**
 * The brains of the operation.
 *
 * Based on https://developer.android.com/reference/android/app/Service.html
 */

public class ControlService extends Service {

    static final String LOG = "ControlService";

    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_GET_STATUS = 3;
    public static final int MSG_ALARM_RECEIVED = 4;

    private NotificationManager mNotificationManager;

    // Q-Timer
    EventTimer mEventTimer;

    // Messenger to clients
    private Messenger mClientMessenger;

    // ID to access our notification
    private int NOTIFICATION_ID = 1;

    class MessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            Log.d(LOG, "Received Message: " + msg.what);

            switch (msg.what) {

                case MSG_REGISTER_CLIENT:
                    mClientMessenger = msg.replyTo;
                    break;

                case MSG_UNREGISTER_CLIENT:
                    mClientMessenger = null;
                    startActivity();
                    break;

                case MSG_GET_STATUS:
                    messageClient(1);
                    break;

                case MSG_ALARM_RECEIVED:
                    messageClient(MSG_ALARM_RECEIVED);
                    break;

                default:
                    super.handleMessage(msg);

            }
        }
    }

    final Messenger mMessengerHandler = new Messenger(new MessageHandler());


    @Override
    public void onCreate() {
        mEventTimer = new EventTimer(this, mMessengerHandler, 10, 0);
        mEventTimer.setTimer();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();
        Toast.makeText(this, "ControlService started", Toast.LENGTH_SHORT).show();
    }


    @Override
    public int onStartCommand(Intent intent, int flag, int StartID) {
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        mNotificationManager.cancel(NOTIFICATION_ID);
        Toast.makeText(this, "ControlService stopped", Toast.LENGTH_SHORT).show();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mMessengerHandler.getBinder();
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
//        Intent intent = new Intent();
//        intent.setClass(getBaseContext(), MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

        // Post notification to status bar
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }
}
