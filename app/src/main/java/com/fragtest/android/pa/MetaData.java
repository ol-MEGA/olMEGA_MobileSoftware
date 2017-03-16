package com.fragtest.android.pa;

import static android.R.attr.value;

/**
 * Created by ulrikkowalk on 14.03.17.
 */

public class MetaData {

    String _DeviceID;
    int _StartDate;
    int _StartDateUTC;

    public MetaData() {}

    public void setDeviceID(String DeviceID) {
        _DeviceID = DeviceID;
    }

    public void setStartDate(int StartDate) {
        _StartDate = StartDate;
    }

    public void setStartDateUTC(int StartDateUTC) {
        _StartDateUTC = StartDateUTC;
    }

    public String get_DeviceID() { return _DeviceID; }

    public int get_StartDate() { return _StartDate; }

    public int getStartDateUTC() { return _StartDateUTC; }

}
