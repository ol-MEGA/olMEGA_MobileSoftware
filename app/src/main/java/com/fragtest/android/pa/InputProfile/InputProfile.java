package com.fragtest.android.pa.InputProfile;

public interface InputProfile {

    void setInterface();

    // Only interesting for CHARGING Profile
    void setState(String inputProfile);

    String getInputProfile();

    void cleanUp();

    void setDevice(String sDeviceName);

    boolean getIsAudioRecorderClosed();

    void registerClient();

    void unregisterClient();

    void applicationShutdown();

    void batteryCritical();

    void chargingOff();

    void chargingOn();

    void chargingOnPre();

    void onDestroy();

}
