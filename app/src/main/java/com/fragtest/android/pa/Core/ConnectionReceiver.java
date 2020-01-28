package com.fragtest.android.pa.Core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Messenger;
import android.util.Log;

public class ConnectionReceiver extends BroadcastReceiver {


    private static final String LOG = "ConnectionReceiver";
    private static Context mContext;


    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context.getApplicationContext();
        Log.d(LOG, "Context:" + mContext.toString());


        Messenger messenger = intent.getParcelableExtra("Connection");

        Log.e(LOG,"messenger from pete's brother: "+messenger.toString());

        sendMessageToService();

    }

    private static void sendMessageToService() {
        Intent intent = new Intent("EstablishConnection");
        mContext.sendBroadcast(intent);
    }

}
