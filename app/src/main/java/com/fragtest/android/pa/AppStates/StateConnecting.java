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

public class StateConnecting implements AppState {

    private final String LOG = "StateConnecting";
    private MainActivity mainActivity;
    private QuestionnairePagerAdapter qpa;
    private int mDelayConnecting = 90*1000;
    private int mDelayDots = 500;
    private int mDelayPollBT = 30*1000;
    private String[] mStringDots = {"   ", "•  ", "•• ", "•••"};
    private int iDot = 0;
    private boolean blockError;

    private Runnable mConnectingRunnable = new Runnable() {
        @Override
        public void run() {
            stopConnecting();
        }
    };

    private Runnable mPollBTRunnable = new Runnable() {
        @Override
        public void run() {
            mainActivity.messageService(ControlService.MSG_RESET_BT);
            mainActivity.mTaskHandler.postDelayed(mPollBTRunnable, mDelayPollBT);
        }
    };

    private Runnable mDotRunnable = new Runnable() {
        @Override
        public void run() {
            qpa.getMenuPage().mDots.setText(mStringDots[iDot%4]);
            iDot++;
            mainActivity.mTaskHandler.postDelayed(mDotRunnable, mDelayDots);
        }
    };

    public StateConnecting(MainActivity context, QuestionnairePagerAdapter qpa) {
        this.mainActivity = context;
        this.qpa = qpa;
        this.blockError = true;
    }

    @Override
    public void setInterface() {
        LogIHAB.log(LOG + ":" + "setInterface()");
        qpa.hideQuestionnaireProgressBar();
        qpa.stopCountDown();
        qpa.getMenuPage().setText(mainActivity.getResources().getString(R.string.infoConnecting));
        qpa.getMenuPage().makeTextSizeNormal();
        qpa.getMenuPage().makeFontWeightNormal();
        qpa.getMenuPage().mDots.setVisibility(View.VISIBLE);
        qpa.getMenuPage().hideErrorList();
        qpa.getMenuPage().hideCountdownText();
        qpa.getMenuPage().clearQuestionnaireCallback();
        mainActivity.mCharging.setVisibility(View.INVISIBLE);
        mainActivity.setBTLogoDisconnected();
        mainActivity.messageService(ControlService.MSG_STOP_COUNTDOWN);
        mainActivity.messageService(ControlService.MSG_CHECK_FOR_PREFERENCES, null);
        mainActivity.mTaskHandler.postDelayed(mConnectingRunnable, mDelayConnecting);
        mainActivity.mTaskHandler.post(mDotRunnable);
        // Have to run tests whether this is necessary
        mainActivity.mTaskHandler.postDelayed(mPollBTRunnable, mDelayPollBT);

        Log.e(LOG, LOG);
    }

    @Override
    public void countdownStart() {
        LogIHAB.log(LOG + ":" + "countdownStart()");
        // No countdown during connecting state
    }

    @Override
    public void countdownFinish() {
        LogIHAB.log(LOG + ":" + "countdownFinish()");
        // No countdown during connecting state
    }

    @Override
    public void noQuest() {
        LogIHAB.log(LOG + ":" + "NoQuest()");
        mainActivity.addError(MainActivity.AppErrors.ERROR_NO_QUEST);
        mainActivity.setBTLogoDisconnected();
        stopConnecting();
    }

    @Override
    public void chargeOn() {
        LogIHAB.log(LOG + ":" + "chargeOn()");
        stopConnecting();
        mainActivity.setState(mainActivity.getStateCharging());
        mainActivity.mAppState.setInterface();
    }

    @Override
    public void chargeOff() {
        LogIHAB.log(LOG + ":" + "chargeOff()");
        // Already not charging
    }

    @Override
    public void bluetoothPresent() {
        LogIHAB.log(LOG + ":" + "bluetoothPresent()");
        stopConnecting();
        mainActivity.removeError(MainActivity.AppErrors.ERROR_NO_BT);
        mainActivity.setBTLogoConnected();
        mainActivity.setState(mainActivity.getStateRunning());
        mainActivity.mAppState.setInterface();
    }

    @Override
    public void bluetoothNotPresent() {
        LogIHAB.log(LOG + ":" + "bluetoothNotPresent()");
        mainActivity.setBTLogoDisconnected();
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
        stopConnecting();
        mainActivity.removeError(MainActivity.AppErrors.ERROR_BATT_LOW);
        mainActivity.addError(MainActivity.AppErrors.ERROR_BATT_CRITICAL);
        mainActivity.setState(mainActivity.getStateError());
        mainActivity.mAppState.setInterface();
    }

    @Override
    public void batteryNormal() {
        LogIHAB.log(LOG + ":" + "batteryNormal()");
        mainActivity.removeError(MainActivity.AppErrors.ERROR_BATT_CRITICAL);
        mainActivity.removeError(MainActivity.AppErrors.ERROR_BATT_LOW);
    }

    @Override
    public void startQuest() {
        LogIHAB.log(LOG + ":" + "startQuest()");
        // No quest during connecting state
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

    private void stopConnecting() {
        LogIHAB.log(LOG + ":" + "stopConnecting()");
        qpa.getMenuPage().mDots.setVisibility(View.INVISIBLE);
        mainActivity.mTaskHandler.removeCallbacks(mConnectingRunnable);
        mainActivity.mTaskHandler.removeCallbacks(mDotRunnable);
        mainActivity.mTaskHandler.removeCallbacks(mPollBTRunnable);
        blockError = false;

        Log.e(LOG, LOG + ":" + "State Connecting going on to Error..");

        mainActivity.setState(mainActivity.getStateError());
        mainActivity.mAppState.setInterface();
    }
}
