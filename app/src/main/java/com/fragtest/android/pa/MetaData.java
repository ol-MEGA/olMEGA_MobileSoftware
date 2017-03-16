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

    private String _DeviceID;
    private String _StartDate;
    private String _StartDateUTC;
    private String _EndDate;
    private String _EndDateUTC;
    private Chronometer mTimer;
    private Context mContext;
    public boolean timerActive = false;

    long _TimeToCompleteSeconds;

    public MetaData(Context context) {
        mContext = context;
        _TimeToCompleteSeconds = -1;
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
        Log.e("it took this much time",""+_TimeToCompleteSeconds);
        return true;
    }

    private void setDeviceID(String DeviceID) {
        _DeviceID = DeviceID;
    }

    private void setStartDate(String StartDate) {
        _StartDate = StartDate;
    }

    private void setStartDateUTC(String StartDateUTC) {
        _StartDateUTC = StartDateUTC;
    }

    private void setEndDate(String EndDate) {
        _EndDate = EndDate;
    }

    private void setEndDateUTC(String EndDateUTC) {
        _EndDateUTC = EndDateUTC;
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
            _TimeToCompleteSeconds = mTimer.getBase();
        }
    }

    public String getDeviceID() { return _DeviceID; }

    public String getStartDate() { return _StartDate; }

    public String getStartDateUTC() { return _StartDateUTC; }

    public String getEndDate() { return _EndDate; }

    public String getEndDateUTC() { return _EndDateUTC; }

    public long getTimeToCompleteSeconds() { return _TimeToCompleteSeconds; }
}
