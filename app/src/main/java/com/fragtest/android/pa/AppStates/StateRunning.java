package com.fragtest.android.pa.AppStates;

import android.util.Log;
import android.view.View;

import com.fragtest.android.pa.MainActivity;
import com.fragtest.android.pa.Questionnaire.QuestionnairePagerAdapter;
import com.fragtest.android.pa.R;

/**
 * Created by ul1021 on 15.04.2018.
 */

public class StateRunning implements AppState {

    private final String LOG = "StateRunning";
    private MainActivity mainActivity;
    private QuestionnairePagerAdapter qpa;

    public StateRunning(MainActivity context, QuestionnairePagerAdapter qpa) {
        this.mainActivity = context;
        this.qpa = qpa;
    }

    @Override
    public void setInterface() {
        qpa.getMenuPage().setText(mainActivity.getResources().getString(R.string.menuText));
        qpa.getMenuPage().makeTextSizeNormal();
        qpa.getMenuPage().showErrorList();
        qpa.startCountDown();
        mainActivity.mCharging.setVisibility(View.INVISIBLE);

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
        qpa.startCountDown();
    }

    @Override
    public void countdownFinish() {
        mainActivity.setState(mainActivity.getStateProposing());
        mainActivity.mAppState.setInterface();
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
        mainActivity.removeError(MainActivity.AppErrors.ERROR_BATT_LOW);
        mainActivity.addError(MainActivity.AppErrors.ERROR_BATT_CRITICAL);
        mainActivity.setState(mainActivity.getStateError());
        mainActivity.mAppState.setInterface();
    }

    @Override
    public void batteryNormal() {
        mainActivity.removeError(MainActivity.AppErrors.ERROR_BATT_LOW);
        mainActivity.removeError(MainActivity.AppErrors.ERROR_BATT_CRITICAL);
    }

    @Override
    public void startQuest() {
        qpa.hideQuestionnaireProgressBar();
        qpa.createQuestionnaire();
        mainActivity.setState(mainActivity.getStateQuest());
        mainActivity.mAppState.setInterface();
    }

    @Override
    public void finishQuest() {
        // Cannot happen
    }

    @Override
    public void openHelp() {
        qpa.createHelpScreen();
    }

    @Override
    public void closeHelp() {
        // Let's see
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
