package com.fragtest.android.pa.AppStates;

import android.util.Log;
import android.view.View;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.LogIHAB;
import com.fragtest.android.pa.DataTypes.INPUT_CONFIG;
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
        LogIHAB.log(LOG + ":" + "setInterface()");
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
        LogIHAB.log(LOG);
    }

    @Override
    public void noQuest() {
        LogIHAB.log(LOG + ":" + "NoQuest()");
        mainActivity.addError(MainActivity.AppErrors.ERROR_NO_QUEST);
    }

    @Override
    public void countdownStart() {
        LogIHAB.log(LOG + ":" + "countdownStart()");
        // No countdown during error state
    }

    @Override
    public void countdownFinish() {
        LogIHAB.log(LOG + ":" + "countdownFinish()");
        // No countdown during error state
    }

    @Override
    public void chargeOn() {
        LogIHAB.log(LOG + ":" + "chargeOn()");
        mainActivity.setState(mainActivity.getStateCharging());
        mainActivity.mAppState.setInterface();
    }

    @Override
    public void chargeOff() {
        LogIHAB.log(LOG + ":" + "chargeOff()");
        setInterface();
    }

    @Override
    public void bluetoothPresent() {
        LogIHAB.log(LOG + ":" + "bluetoothPresent()");
        mainActivity.removeError(MainActivity.AppErrors.ERROR_NO_BT);
        mainActivity.setBTLogoConnected();

        if (!mainActivity.mErrorList.contains(MainActivity.AppErrors.ERROR_NO_QUEST.getErrorMessage()) &&
                !mainActivity.mErrorList.contains(MainActivity.AppErrors.ERROR_BATT_CRITICAL.getErrorMessage())) {
            mainActivity.setState(mainActivity.getStateRunning());
            mainActivity.mAppState.setInterface();
        }
    }

    @Override
    public void bluetoothNotPresent() {
        LogIHAB.log(LOG + ":" + "bluetoothNotPresent()");
        // Already in error state
        mainActivity.addError(MainActivity.AppErrors.ERROR_NO_BT);
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
        // No questionnaires in error state
    }

    @Override
    public void finishQuest() {
        LogIHAB.log(LOG + ":" + "finishQuest()");
        qpa.backToMenu();
        setInterface();
    }

    @Override
    public void openHelp() {
        LogIHAB.log(LOG + ":" + "openHelp()");
        qpa.createHelpScreen();
    }

    @Override
    public void closeHelp() {
        LogIHAB.log(LOG + ":" + "closeHelp()");
        setInterface();
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
    public void usbPresent() {
        LogIHAB.log(LOG + ":" + "usbPresent()");
        if (ControlService.INPUT == INPUT_CONFIG.USB) {
            mainActivity.removeError(MainActivity.AppErrors.ERROR_NO_USB);
            mainActivity.setState(mainActivity.getStateRunning());
            mainActivity.mAppState.setInterface();
        }
    }

    @Override
    public void usbNotPresent() {
        LogIHAB.log(LOG + ":" + "usbNotPresent()");
        if (ControlService.INPUT == INPUT_CONFIG.USB) {
            // TODO: See if this is needed
            mainActivity.addError(MainActivity.AppErrors.ERROR_NO_USB);
        }
    }
}
