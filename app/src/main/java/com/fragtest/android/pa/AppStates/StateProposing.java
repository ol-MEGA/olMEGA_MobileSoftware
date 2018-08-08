package com.fragtest.android.pa.AppStates;

import android.util.Log;
import android.view.View;

import com.fragtest.android.pa.MainActivity;
import com.fragtest.android.pa.Questionnaire.QuestionnairePagerAdapter;

/**
 * Created by ul1021 on 15.04.2018.
 */

public class StateProposing implements AppState {

    private final String LOG = "StateProposing";
    private MainActivity mainActivity;
    private QuestionnairePagerAdapter qpa;

    public StateProposing(MainActivity context, QuestionnairePagerAdapter qpa) {
        this.mainActivity = context;
        this.qpa = qpa;
    }

    @Override
    public void setInterface() {
        qpa.stopCountDown();
        qpa.getMenuPage().proposeQuestionnaire();
        qpa.getMenuPage().hideCountdownText();
        qpa.fillQuestionnaireProgressBar();
        mainActivity.mCharging.setVisibility(View.INVISIBLE);

        Log.e(LOG, LOG);
    }

    @Override
    public void noQuest() {
        mainActivity.addError(MainActivity.AppErrors.ERROR_NO_QUEST);
    }

    @Override
    public void countdownStart() {
        // No countdown in proposing state
    }

    @Override
    public void countdownFinish() {
        // No countdown in proposing state
    }

    @Override
    public void chargeOn() {
        mainActivity.setState(mainActivity.getStateCharging());
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
        mainActivity.setState(mainActivity.getStateConnecting());
        mainActivity.mAppState.setInterface();
    }

    @Override
    public void batteryLow() {
        mainActivity.removeError(MainActivity.AppErrors.ERROR_BATT_CRITICAL);
        mainActivity.addError(MainActivity.AppErrors.ERROR_BATT_LOW);
    }

    @Override
    public void batteryCritical() {
        mainActivity.addError(MainActivity.AppErrors.ERROR_BATT_CRITICAL);
        mainActivity.removeError(MainActivity.AppErrors.ERROR_BATT_LOW);
        mainActivity.setState(mainActivity.getStateError());
        mainActivity.mAppState.setInterface();
    }

    @Override
    public void batteryNormal() {
        mainActivity.removeError(MainActivity.AppErrors.ERROR_BATT_CRITICAL);
        mainActivity.removeError(MainActivity.AppErrors.ERROR_BATT_LOW);
    }

    @Override
    public void startQuest() {
        qpa.createQuestionnaire();
        mainActivity.setState(mainActivity.getStateQuest());
        mainActivity.mAppState.setInterface();
    }

    @Override
    public void finishQuest() {
        // Cannot happen in proposing state
    }

    @Override
    public void openHelp() {
        qpa.createHelpScreen();
    }

    @Override
    public void closeHelp() {
        setInterface();
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
