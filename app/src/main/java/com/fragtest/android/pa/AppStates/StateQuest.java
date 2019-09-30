package com.fragtest.android.pa.AppStates;

import android.util.Log;
import android.view.View;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.LogIHAB;
import com.fragtest.android.pa.MainActivity;
import com.fragtest.android.pa.Questionnaire.QuestionnairePagerAdapter;

/**
 * Created by ul1021 on 15.04.2018.
 */

public class StateQuest implements AppState {

    private final String LOG = "StateQuest";
    private MainActivity mainActivity;
    private QuestionnairePagerAdapter qpa;

    public StateQuest(MainActivity context, QuestionnairePagerAdapter qpa) {
        this.mainActivity = context;
        this.qpa = qpa;
    }

    @Override
    public void setInterface() {
        LogIHAB.log(LOG + ":" + "setInterface()");
        qpa.stopCountDown();
        qpa.getMenuPage().hideCountdownText();
        qpa.getMenuPage().makeTextSizeNormal();
        qpa.getMenuPage().makeFontWeightNormal();
        qpa.getMenuPage().clearQuestionnaireCallback();
        mainActivity.mCharging.setVisibility(View.INVISIBLE);
        mainActivity.messageService(ControlService.MSG_STOP_COUNTDOWN);

        qpa.createQuestionnaire();

        Log.e(LOG, LOG);
        LogIHAB.log(LOG);
    }

    @Override
    public void noQuest() {
        LogIHAB.log(LOG + ":" + "NoQuest()");
        mainActivity.addError(MainActivity.AppErrors.ERROR_NO_QUEST);
        mainActivity.setState(mainActivity.getStateError());
        mainActivity.mAppState.setInterface();
    }

    @Override
    public void countdownStart() {
        LogIHAB.log(LOG + ":" + "countdownStart()");
        // No countdown in quest state
    }

    @Override
    public void countdownFinish() {
        LogIHAB.log(LOG + ":" + "countdownFinish()");
        // No countdown in quest state
    }

    @Override
    public void chargeOn() {
        LogIHAB.log(LOG + ":" + "chargeOn()");
        mainActivity.setState(mainActivity.getStateCharging());
        qpa.backToMenu();
        mainActivity.mAppState.setInterface();
    }

    @Override
    public void chargeOff() {
        LogIHAB.log(LOG + ":" + "chargeOff()");
        // Already not charging
    }

    @Override
    public void bluetoothConnected() {
        LogIHAB.log(LOG + ":" + "bluetoothConnected()");
        mainActivity.removeError(MainActivity.AppErrors.ERROR_NO_BT);
        mainActivity.setBTLogoConnected();
    }

    @Override
    public void bluetoothDisconnected() {
        LogIHAB.log(LOG + ":" + "bluetoothDisconnected()");
        mainActivity.addError(MainActivity.AppErrors.ERROR_NO_BT);
        mainActivity.setBTLogoDisconnected();
    }

    @Override
    public void batteryLow() {
        LogIHAB.log(LOG + ":" + "batteryLow()");
        mainActivity.removeError(MainActivity.AppErrors.ERROR_BATT_CRITICAL);
        mainActivity.addError(MainActivity.AppErrors.ERROR_BATT_LOW);
    }

    @Override
    public void batteryCritical() {
        LogIHAB.log(LOG + ":" + "batteryCritical()");
        mainActivity.removeError(MainActivity.AppErrors.ERROR_BATT_LOW);
        mainActivity.addError(MainActivity.AppErrors.ERROR_BATT_CRITICAL);
    }

    @Override
    public void batteryNormal() {
        LogIHAB.log(LOG + ":" + "batteryNormal()");
        mainActivity.removeError(MainActivity.AppErrors.ERROR_BATT_LOW);
        mainActivity.removeError(MainActivity.AppErrors.ERROR_BATT_CRITICAL);
    }

    @Override
    public void startQuest() {
        LogIHAB.log(LOG + ":" + "startQuest()");
        // Already in quest state
    }

    @Override
    public void finishQuest() {
        LogIHAB.log(LOG + ":" + "finishQuest()");

        qpa.backToMenu();

        if (mainActivity.mErrorList.contains(MainActivity.AppErrors.ERROR_BATT_CRITICAL.getErrorMessage()) ||
                mainActivity.mErrorList.contains(MainActivity.AppErrors.ERROR_NO_QUEST.getErrorMessage())) {
            mainActivity.setState(mainActivity.getStateError());
            mainActivity.mAppState.setInterface();
        } else if (mainActivity.mErrorList.contains(MainActivity.AppErrors.ERROR_NO_BT.getErrorMessage())) {
                mainActivity.setState(mainActivity.getStateConnecting());
                mainActivity.mAppState.setInterface();
        } else {
            mainActivity.setState(mainActivity.getStateRunning());
            mainActivity.mAppState.setInterface();
        }
    }

    @Override
    public void openHelp() {
        LogIHAB.log(LOG + ":" + "openHelp()");
        qpa.createHelpScreen();
    }

    @Override
    public void closeHelp() {
        LogIHAB.log(LOG + ":" + "closeHelp()");
        // Don't care for now -> later might make Help a state
    }

    @Override
    public void timeCorrect() {
        LogIHAB.log(LOG + ":" + "timeCorrect()");
        qpa.getMenuPage().showTime();
    }

    @Override
    public void timeIncorrect() {
        LogIHAB.log(LOG + ":" + "timeIncorrect()");
        qpa.getMenuPage().hideTime();
    }

    @Override
    public void startRecording() {
        LogIHAB.log(LOG + ":" + "startRecording()");
    }

    @Override
    public void stopRecording() {
        LogIHAB.log(LOG + ":" + "stopRecording()");
        mainActivity.setState(mainActivity.getStateConnecting());
        qpa.backToMenu();
        mainActivity.mAppState.setInterface();
    }
}
