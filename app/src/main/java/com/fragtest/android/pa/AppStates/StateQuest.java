package com.fragtest.android.pa.AppStates;

import android.util.Log;
import android.view.View;

import com.fragtest.android.pa.ControlService;
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
        qpa.stopCountDown();
        qpa.getMenuPage().hideCountdownText();
        qpa.getMenuPage().makeTextSizeNormal();
        qpa.getMenuPage().makeFontWeightNormal();
        qpa.getMenuPage().clearQuestionnaireCallback();
        mainActivity.mCharging.setVisibility(View.INVISIBLE);
        mainActivity.messageService(ControlService.MSG_STOP_COUNTDOWN);

        qpa.createQuestionnaire();

        Log.e(LOG, LOG);
    }

    @Override
    public void noQuest() {
        mainActivity.addError(MainActivity.AppErrors.ERROR_NO_QUEST);
        mainActivity.setState(mainActivity.getStateError());
        mainActivity.mAppState.setInterface();
    }

    @Override
    public void countdownStart() {
        // No countdown in quest state
    }

    @Override
    public void countdownFinish() {
        // No countdown in quest state
    }

    @Override
    public void chargeOn() {
        mainActivity.setState(mainActivity.getStateCharging());
        qpa.backToMenu();
        mainActivity.mAppState.setInterface();
    }

    @Override
    public void chargeOff() {
        // Already not charging
    }

    @Override
    public void bluetoothPresent() {
        mainActivity.removeError(MainActivity.AppErrors.ERROR_NO_BT);
    }

    @Override
    public void bluetoothNotPresent() {
        mainActivity.addError(MainActivity.AppErrors.ERROR_NO_BT);
    }

    @Override
    public void batteryLow() {
        mainActivity.removeError(MainActivity.AppErrors.ERROR_BATT_CRITICAL);
        mainActivity.addError(MainActivity.AppErrors.ERROR_BATT_LOW);
    }

    @Override
    public void batteryCritical() {
        mainActivity.removeError(MainActivity.AppErrors.ERROR_BATT_LOW);
        mainActivity.addError(MainActivity.AppErrors.ERROR_BATT_CRITICAL);
    }

    @Override
    public void batteryNormal() {
        mainActivity.removeError(MainActivity.AppErrors.ERROR_BATT_LOW);
        mainActivity.removeError(MainActivity.AppErrors.ERROR_BATT_CRITICAL);
    }

    @Override
    public void startQuest() {
        // Already in quest state
    }

    @Override
    public void finishQuest() {

        qpa.backToMenu();

        if (mainActivity.mErrorList.contains(MainActivity.AppErrors.ERROR_BATT_CRITICAL) ||
                mainActivity.mErrorList.contains(MainActivity.AppErrors.ERROR_NO_QUEST)) {
            mainActivity.setState(mainActivity.getStateError());
            mainActivity.mAppState.setInterface();
        } else if (mainActivity.mErrorList.contains(MainActivity.AppErrors.ERROR_NO_BT)) {
                mainActivity.setState(mainActivity.getStateConnecting());
                mainActivity.mAppState.setInterface();
        } else {
            mainActivity.setState(mainActivity.getStateRunning());
            mainActivity.mAppState.setInterface();
        }
    }

    @Override
    public void openHelp() {
        qpa.createHelpScreen();
    }

    @Override
    public void closeHelp() {
        // Don't care for now -> later might make Help a state
    }

    @Override
    public void timeCorrect() {
        qpa.getMenuPage().showTime();
    }

    @Override
    public void timeIncorrect() {
        qpa.getMenuPage().hideTime();
    }
}
