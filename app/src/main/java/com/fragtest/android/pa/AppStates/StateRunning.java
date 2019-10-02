package com.fragtest.android.pa.AppStates;

import android.util.Log;
import android.view.View;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.LogIHAB;
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
        LogIHAB.log(LOG + ":" + "setInterface()");
        qpa.getMenuPage().setText(mainActivity.getResources().getString(R.string.menuText));
        qpa.getMenuPage().makeTextSizeNormal();
        qpa.getMenuPage().makeFontWeightNormal();
        qpa.getMenuPage().showErrorList();
        qpa.getMenuPage().resetQuestionnaireCallback();
        mainActivity.mCharging.setVisibility(View.INVISIBLE);
        mainActivity.messageService(ControlService.MSG_START_COUNTDOWN);
        //mainActivity.setBTLogoConnected();

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
        qpa.getMenuPage().showCountdownText();
        qpa.startCountDown();
    }

    @Override
    public void countdownFinish() {
        LogIHAB.log(LOG + ":" + "countdownFinish()");
        mainActivity.mTaskHandler.post(qpa.mSetProgressBarFullRunnable);
        mainActivity.mTaskHandler.removeCallbacks(qpa.mSetProgressBarFullRunnable);
        mainActivity.setState(mainActivity.getStateProposing());
        mainActivity.mAppState.setInterface();
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
        // Already not charging
    }

    @Override
    public void bluetoothConnected() {
        LogIHAB.log(LOG + ":" + "bluetoothConnected()");
        mainActivity.removeError(MainActivity.AppErrors.ERROR_NO_BT);
    }

    @Override
    public void bluetoothDisconnected() {
        LogIHAB.log(LOG + ":" + "bluetoothDisconnected()");
        //mainActivity.setBTLogoDisconnected();
        mainActivity.addError(MainActivity.AppErrors.ERROR_NO_BT);
        mainActivity.setState(mainActivity.getStateConnecting());
        mainActivity.mAppState.setInterface();
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
        mainActivity.setState(mainActivity.getStateError());
        mainActivity.mAppState.setInterface();
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
        qpa.hideQuestionnaireProgressBar();
        //qpa.createQuestionnaire();
        mainActivity.setState(mainActivity.getStateQuest());
        mainActivity.mAppState.setInterface();
    }

    @Override
    public void finishQuest() {
        LogIHAB.log(LOG + ":" + "finishQuest()");
        // Cannot happen
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
    public void startRecording() {
        LogIHAB.log(LOG + ":" + "startRecording()");
    }

    @Override
    public void stopRecording() {
        LogIHAB.log(LOG + ":" + "stopRecording()");
        mainActivity.setState(mainActivity.getStateConnecting());
        mainActivity.mAppState.setInterface();
    }
}
