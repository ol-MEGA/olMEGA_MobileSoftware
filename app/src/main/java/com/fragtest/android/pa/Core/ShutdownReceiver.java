package com.fragtest.android.pa.Core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receiver events set in EventTimer
 */



public class ShutdownReceiver extends BroadcastReceiver {

    private static final String LOG = "ShutdownReceiver";
    private static Context mContext;


    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context.getApplicationContext();
        sendMessageToService();

    }

    private static void sendMessageToService() {
        Intent intent = new Intent("ShutdownReceived");
        mContext.sendBroadcast(intent);
    }
}