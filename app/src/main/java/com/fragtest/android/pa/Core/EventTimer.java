package com.fragtest.android.pa.Core;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Messenger;
import android.util.Log;

import com.fragtest.android.pa.BuildConfig;

/**
 * Timer to trigger questionnaire and display remaining time.
 */

public class EventTimer {

    private static final String LOG = "EventTimer";
    private Context context;
    private Messenger messenger;

    public EventTimer(Context ctx, Messenger msg) {

        context = ctx;
        messenger = msg;

        if (BuildConfig.DEBUG) {
            Log.d(LOG,"EventTimer created.");
        }
    }

    public void setTimer(int interval) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Alarm will call EventReceiver class by sending broadcast along with messenger context
        Intent intent = new Intent(context, EventReceiver.class);
        intent.putExtra("Messenger", messenger);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        // Schedules the initialisation of new questionnaire
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + (interval-1)*1000, alarmIntent);

        if (BuildConfig.DEBUG){
            Log.d(LOG,"New timer interval set to "+interval+"s");
        }
    }
}
