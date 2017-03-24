package com.fragtest.android.pa;

import java.util.Calendar;
import java.util.TimeZone;
import android.content.Context;
import android.provider.Settings.Secure;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Chronometer;

/**
 * Created by ulrikkowalk on 14.03.17.
 */

public class MetaData extends AppCompatActivity{

    public boolean timerActive = false;

    private String DEVICE_ID;
    private String START_DATE;
    private String START_DATE_UTC;
    private String END_DATE;
    private String END_DATE_UTC;

    private long TIME_TO_COMPLETE_SECONDS;

    private Chronometer mTimer;
    private Context mContext;


    public MetaData(Context context) {
        mContext = context;
        TIME_TO_COMPLETE_SECONDS = -1;
        mTimer = new Chronometer(mContext);
    }

    public boolean initialise() {
        // Obtain Device ID
        setDeviceID(Secure.getString(mContext.getContentResolver(),Secure.ANDROID_ID));
        // Obtain current Time Stamp at the Beginning of Questionnaire
        Calendar dateTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
        setStartDate(dateTime.getTime().toString());
        // Obtain current UTC Time Stamp at the Beginning of Questionnaire
        Calendar dateTimeUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        setStartDateUTC(dateTimeUTC.getTime().toString());
        // Set a Point in Time at beginning of Questionnaire
        startTimer();
        Log.e("deviceID",getDeviceID());
        Log.e("initialised at",getStartDate());
        Log.e("initialised at (UTC)",getStartDateUTC());
        return true;
    }

    public boolean finalise() {
        // Obtain current Time Stamp at the End of Questionnaire
        Calendar dateTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
        setEndDate(dateTime.getTime().toString());
        // Obtain current UTC Time Stamp at the End of Questionnaire
        Calendar dateTimeUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        setEndDateUTC(dateTimeUTC.getTime().toString());
        // Stop the Clock and calculate consumed Time
        stopTimer();
        Log.e("finalised at",getEndDate());
        Log.e("finalised at (UTC)",getEndDateUTC());
        Log.e("it took this much time",""+ TIME_TO_COMPLETE_SECONDS);
        return true;
    }

    private void setDeviceID(String DeviceID) {
        DEVICE_ID = DeviceID;
    }

    private void setStartDate(String StartDate) {
        START_DATE = StartDate;
    }

    private void setStartDateUTC(String StartDateUTC) {
        START_DATE_UTC = StartDateUTC;
    }

    private void setEndDate(String EndDate) {
        END_DATE = EndDate;
    }

    private void setEndDateUTC(String EndDateUTC) {
        END_DATE_UTC = EndDateUTC;
    }

    private void startTimer() {
        if (!timerActive) {
            timerActive = true;
            mTimer.start();
        }
    }

    private void stopTimer() {
        if (timerActive) {
            timerActive = false;
            mTimer.stop();
            TIME_TO_COMPLETE_SECONDS = mTimer.getBase();
        }
    }

    public String getDeviceID() { return DEVICE_ID; }

    public String getStartDate() { return START_DATE; }

    public String getStartDateUTC() { return START_DATE_UTC; }

    public String getEndDate() { return END_DATE; }

    public String getEndDateUTC() { return END_DATE_UTC; }

    public long getTimeToCompleteSeconds() { return TIME_TO_COMPLETE_SECONDS; }
}
