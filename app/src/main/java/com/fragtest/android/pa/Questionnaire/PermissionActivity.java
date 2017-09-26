package com.fragtest.android.pa.Questionnaire;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by ul1021 on 19.09.2017.
 */

public class PermissionActivity extends AppCompatActivity {

    private final static int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 0;
    private final static int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 1;
    private final static int MY_PERMISSIONS_RECEIVE_BOOT_COMPLETED = 2;
    private final static int MY_PERMISSIONS_RECORD_AUDIO = 3;
    private final static int MY_PERMISSIONS_VIBRATE = 4;
    private final static int MY_PERMISSIONS_WAKE_LOCK = 5;
    private final static int MY_PERMISSIONS_DISABLE_KEYGUARD = 6;
    private final static int MY_PERMISSIONS_CAMERA = 7;

    private final static String LOG = "PermissionsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOG, "Requesting Permissions.");

        //Android 6.0.1+
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                MY_PERMISSIONS_READ_EXTERNAL_STORAGE);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED},
                MY_PERMISSIONS_RECEIVE_BOOT_COMPLETED);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                MY_PERMISSIONS_RECORD_AUDIO);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.VIBRATE},
                MY_PERMISSIONS_VIBRATE);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WAKE_LOCK},
                MY_PERMISSIONS_WAKE_LOCK);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.DISABLE_KEYGUARD},
                MY_PERMISSIONS_DISABLE_KEYGUARD);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                MY_PERMISSIONS_CAMERA);

    }
}
