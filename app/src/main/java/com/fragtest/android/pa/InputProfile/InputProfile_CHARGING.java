package com.fragtest.android.pa.InputProfile;

import android.os.Messenger;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.LogIHAB;

public class InputProfile_CHARGING implements InputProfile {

    private String LOG = "InputProfile_Blank";
    private final String INPUT_PROFILE = "CHARGING";
    private String mProfile_State;
    private ControlService mContext;
    private Messenger mServiceMessenger;


    public InputProfile_CHARGING(ControlService context, Messenger serviceMessenger) {
        this.mContext = context;
        this.mServiceMessenger = serviceMessenger;
    }

    @Override
    public void setState(String inputProfile) {
        this.mProfile_State = inputProfile;
    }


    @Override
    public void setInterface() {

        LogIHAB.log(LOG);

    }

    @Override
    public String getInputProfile() {
        return this.INPUT_PROFILE;
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
        this.mContext.setInputProfile(this.mProfile_State);
    }

    @Override
    public void chargingOn() {

    }

    @Override
    public void chargingOnPre() {

    }

    @Override
    public void onDestroy() {

    }
}
