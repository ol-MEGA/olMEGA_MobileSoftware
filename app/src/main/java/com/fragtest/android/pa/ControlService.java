package com.fragtest.android.pa;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

/**
 * The brains of the operation.
 *
 * Based on https://developer.android.com/reference/android/app/Service.html
 */

public class ControlService extends Service {

    private NotificationManager mNotificationManager;

    // ID to access our notification
    private int NOTIFICATION_ID = 1;

    private final IBinder mBinder = new LocalBinder();


    public class LocalBinder extends Binder {
        ControlService getService() {
            return ControlService.this;
        }
    }


    @Override
    public void onCreate() {
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
        return mBinder;
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
