package com.fragtest.android.pa.InputProfile;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.LogIHAB;

public class InputProfile_Blank implements InputProfile {

    private String LOG = "InputProfile_Blank";
    private ControlService mContext;

    public InputProfile_Blank(ControlService context) {
        this.mContext = context;
    }

    @Override
    public void setInterface() {

        LogIHAB.log(LOG);

    }

    @Override
    public void setState(String inputProfile) {

    }

    @Override
    public String getInputProfile() {
        return "";
    }

    @Override
    public void cleanUp() {

    }

    @Override
    public void setDevice(String sDeviceName) {}

    @Override
    public boolean getIsAudioRecorderClosed() {
        return true;
    }

    @Override
    public void registerClient() {

    }

    @Override
    public void unregisterClient() {

    }

    @Override
    public void applicationShutdown() {

    }

    @Override
    public void batteryCritical() {

    }

    @Override
    public void chargingOff() {

    }

    @Override
    public void chargingOn() {
        mContext.setChargingProfile();
    }

    @Override
    public void chargingOnPre() {

    }

    @Override
    public void onDestroy() {

    }
}
