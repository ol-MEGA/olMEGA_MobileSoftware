package com.fragtest.android.pa.InputProfile;

public interface InputProfile {

    void setInterface();

    String getInputProfile();

    void cleanUp();

    void registerClient();

    void unregisterClient();

    void applicationShutdown();

    void batteryCritical();

    void chargingOff();

    void chargingOn();

    void chargingOnPre();

    void onDestroy();

}
