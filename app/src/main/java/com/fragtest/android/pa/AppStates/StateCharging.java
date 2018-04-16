package com.fragtest.android.pa.AppStates;

import android.util.Log;
import android.view.View;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.MainActivity;
import com.fragtest.android.pa.Questionnaire.QuestionnairePagerAdapter;
import com.fragtest.android.pa.R;

/**
 * Created by ul1021 on 15.04.2018.
 */

public class StateCharging implements AppState {

    private final String LOG = "StateCharging";
    private MainActivity mainActivity;
    private QuestionnairePagerAdapter qpa;

    public StateCharging(MainActivity context, QuestionnairePagerAdapter qpa) {
        this.mainActivity = context;
        this.qpa = qpa;
    }

    @Override
    public void setInterface() {
        qpa.getMenuPage().setText(mainActivity.getResources().getString(R.string.infoCharging));
        qpa.getMenuPage().makeTextSizeNormal();
        qpa.getMenuPage().makeFontWeightNormal();
        qpa.hideQuestionnaireProgressBar();
        qpa.stopCountDown();
        qpa.getMenuPage().hideErrorList();
        qpa.getMenuPage().hideCountdownText();
        qpa.getMenuPage().clearQuestionnaireCallback();

        mainActivity.mCharging.setVisibility(View.VISIBLE);
        mainActivity.messageService(ControlService.MSG_STOP_COUNTDOWN);

        Log.e(LOG, LOG);
    }

    @Override
    public void countdownStart() {
        // No countdown during charging state
    }

    @Override
    public void countdownFinish() {
        // No countdown during charging state
    }

    @Override
    public void noQuest() {
        mainActivity.addError(MainActivity.AppErrors.ERROR_NO_QUEST);
    }

    @Override
    public void chargeOn() {
        //mainActivity.mCharging.setVisibility(View.VISIBLE);
    }

    @Override
    public void chargeOff() {
        //mainActivity.mCharging.setVisibility(View.INVISIBLE);
        mainActivity.setState(mainActivity.getStateConnecting());
        mainActivity.mAppState.setInterface();
    }

    @Override
    public void bluetoothPresent() {
        // No bluetooth during charging state
    }

    @Override
    public void bluetoothNotPresent() {
        // No bluetooth during charging state
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
        // No quest during charging sate
    }

    @Override
    public void finishQuest() {
        qpa.backToMenu();
        setInterface();
    }

    @Override
    public void openHelp() {
        qpa.createHelpScreen();
    }

    @Override
    public void closeHelp() {
        // Don't care for now -> later might make Help a state
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
