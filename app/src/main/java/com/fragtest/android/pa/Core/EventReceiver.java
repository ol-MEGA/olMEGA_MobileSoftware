package com.fragtest.android.pa.Core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.fragtest.android.pa.ControlService;

/**
 * Receiver events set in EventTimer
 */

public class EventReceiver extends BroadcastReceiver {

    private static final String LOG = "EventReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Context ctx = context.getApplicationContext();
        Log.d(LOG, "Context:" + ctx.toString());

        Messenger messenger = intent.getParcelableExtra("Messenger");

        Log.e(LOG,"messenger: "+messenger.toString());

        Log.d(LOG, messenger.toString());
        Message msg = Message.obtain(null, ControlService.MSG_ALARM_RECEIVED);
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}