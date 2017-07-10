package com.fragtest.android.pa.Core;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Messenger;


import java.util.concurrent.ThreadLocalRandom;

/**
 * Timer to trigger questionnaire and display remaining time.
 */

public class EventTimer {

    static final String LOG = "EventTimer";

    int mTimerMean, mTimerDeviation;
    Context context;
    Messenger messenger;

    public EventTimer(Context ctx, Messenger msg, int timerMean, int timerDeviation) {

        context = ctx;
        messenger = msg;
        mTimerMean = timerMean;
        mTimerDeviation = timerDeviation;
    }

    public void setTimer() {

        int msDelay;

        // time to next alarm in ms
        msDelay = ThreadLocalRandom.current().nextInt(
                mTimerMean - mTimerDeviation, mTimerMean + mTimerDeviation + 1) * 1000;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, EventReceiver.class);
        intent.putExtra("Hint", "It's a Trap!");
        intent.putExtra("Messenger", messenger);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + msDelay, alarmIntent);
    }



}
