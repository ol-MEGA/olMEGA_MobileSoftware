package com.fragtest.android.pa.AppStates;

/**
 * Created by ul1021 on 15.04.2018.
 */

public interface AppState {

    void setInterface();

    void countdownStart();

    void countdownFinish();

    void noQuest();

    void chargeOn();

    void chargeOff();

    void bluetoothPresent();

    void bluetoothNotPresent();

    void batteryLow();

    void batteryCritical();

    void batteryNormal();

    void startQuest();

    void finishQuest();

    void openHelp();

    void closeHelp();

    void timeCorrect();

    void timeIncorrect();

    void usbPresent();

    void usbNotPresent();

    void startRecording();

    void stopRecording();

}
