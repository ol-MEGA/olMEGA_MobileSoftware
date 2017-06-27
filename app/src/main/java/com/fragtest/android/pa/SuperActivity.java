package com.fragtest.android.pa;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by ul1021 on 29.05.2017.
 */

public class SuperActivity extends AppCompatActivity{


    private String LOG_STRING = "TimerTest";
    private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);
    private int delayMinutes = 5;
    private int delayMilliseconds = delayMinutes*60*1000;
    private int durVibrationMilliseconds = 800;

    private Handler timerHandler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            //((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(durVibrationMilliseconds);

            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainIntent);
            //finish();
            //timerHandler.postDelayed(this, delayMilliseconds);
        }
    };
    //private int REQUEST_EXIT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FileIO mFileIO = new FileIO();
        String rawInput = mFileIO.readRawTextFile(this, R.raw.question_single);
        TimedEvent timedEvent = new TimedEvent(this, rawInput);
        timedEvent.extractVariables();

        super.onCreate(savedInstanceState);

        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainIntent);


        //timerHandler.postDelayed(runnable, 0);



    }
}
