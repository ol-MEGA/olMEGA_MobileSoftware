package com.fragtest.android.pa.ServiceStates;

import android.content.SharedPreferences;
import android.media.AudioDeviceInfo;

public interface ServiceState {

    void setInterface();

    void changeState();

    void registerClient();

    void unregisterClient();

    void getStatus();

    void resetBT();

    void startCountdown();

    void stopCountdown();

    void manualQuestionnaire();

    void propositionAccepted();

    void questionnaireFinished();

    void isMenu();

    void questionnaireActive();

    void checkForPreferences();

    void recordingStopped();

    void chunkRecorded();

    void chunkProcessed();

    void applicationShutdown();

    void batteryLevelInfo();

    void batteryCritical();

    void chargingOff();

    void chargingOn();

    void chargingOnPre();

    void usbAttached();

    void usbDetached();

    void bluetoothReceived(String action, SharedPreferences sharedPreferences);

    void onDestroy();

    void bluetoothConnected();

    void bluetoothDisconnected();

    void bluetoothSwitchedOff();

    void bluetoothSwitchedOn();

    AudioDeviceInfo getPreferredDevice();

}
