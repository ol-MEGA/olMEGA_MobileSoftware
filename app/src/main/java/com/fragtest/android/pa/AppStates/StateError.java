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

public class StateError implements AppState {

    private final String LOG = "StateError";
    private MainActivity mainActivity;
    private QuestionnairePagerAdapter qpa;

    public StateError(MainActivity context, QuestionnairePagerAdapter qpa) {
        this.mainActivity = context;
        this.qpa = qpa;
    }

    @Override
    public void setInterface() {
        qpa.hideQuestionnaireProgressBar();
        qpa.stopCountDown();
        qpa.getMenuPage().setText(mainActivity.getResources().getString(R.string.infoError));
        qpa.getMenuPage().makeTextSizeNormal();
        qpa.getMenuPage().makeFontWeightNormal();
        qpa.getMenuPage().showErrorList();
        qpa.getMenuPage().hideCountdownText();
        qpa.getMenuPage().clearQuestionnaireCallback();
        mainActivity.mCharging.setVisibility(View.INVISIBLE);
        mainActivity.messageService(ControlService.MSG_STOP_COUNTDOWN);
        mainActivity.setBTLogoDisconnected();

        Log.e(LOG, LOG);
    }

    @Override
    public void noQuest() {
        mainActivity.addError(MainActivity.AppErrors.ERROR_NO_QUEST);
    }

    @Override
    public void countdownStart() {
        // No countdown during error state
    }

    @Override
    public void countdownFinish() {
        // No countdown during error state
    }

    @Override
    public void chargeOn() {
        mainActivity.setState(mainActivity.getStateCharging());
        mainActivity.mAppState.setInterface();
    }

    @Override
    public void chargeOff() {
        setInterface();
    }

    @Override
    public void bluetoothPresent() {
        mainActivity.removeError(MainActivity.AppErrors.ERROR_NO_BT);

        if (!mainActivity.mErrorList.contains(
                MainActivity.AppErrors.ERROR_NO_QUEST.getErrorMessage()) &&
                !mainActivity.mErrorList.contains(
                MainActivity.AppErrors.ERROR_BATT_CRITICAL.getErrorMessage())) {
            mainActivity.setState(mainActivity.getStateRunning());
            mainActivity.mAppState.setInterface();
        }
    }

    @Override
    public void bluetoothNotPresent() {
        // Already in error state
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
        // No questionnaires in error state
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
