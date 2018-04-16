package com.fragtest.android.pa.AppStates;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.fragtest.android.pa.ControlService;
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
    private int mDelayPollBT = 10*1000;
    private String[] mStringDots = {"   ", "•  ", "•• ", "•••"};
    private int iDot = 0;
    private boolean blockError;
    private Handler mTaskHandler = new Handler();

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
            mTaskHandler.postDelayed(mPollBTRunnable, mDelayPollBT);
        }
    };

    private Runnable mDotRunnable = new Runnable() {
        @Override
        public void run() {
            qpa.getMenuPage().mDots.setText(mStringDots[iDot%4]);
            iDot++;
            mTaskHandler.postDelayed(mDotRunnable, mDelayDots);
        }
    };

    public StateConnecting(MainActivity context, QuestionnairePagerAdapter qpa) {
        this.mainActivity = context;
        this.qpa = qpa;
        this.blockError = true;
    }

    @Override
    public void setInterface() {
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
        mTaskHandler.postDelayed(mConnectingRunnable, mDelayConnecting);
        mTaskHandler.post(mDotRunnable);
        // Have to run tests whether this is necessary
        //mTaskHandler.postDelayed(mPollBTRunnable, mDelayPollBT);

        Log.e(LOG, LOG);
    }

    @Override
    public void countdownStart() {
        // No countdown during connecting state
    }

    @Override
    public void countdownFinish() {
        // No countdown during connecting state
    }

    @Override
    public void noQuest() {
        mainActivity.addError(MainActivity.AppErrors.ERROR_NO_QUEST);
        mainActivity.setBTLogoDisconnected();
        stopConnecting();
    }

    @Override
    public void chargeOn() {
        stopConnecting();
        mainActivity.setState(mainActivity.getStateCharging());
        mainActivity.mAppState.setInterface();
    }

    @Override
    public void chargeOff() {
        // Already not charging
    }

    @Override
    public void bluetoothPresent() {
        stopConnecting();
        mainActivity.removeError(MainActivity.AppErrors.ERROR_NO_BT);
        mainActivity.setBTLogoConnected();
        mainActivity.setState(mainActivity.getStateRunning());
        mainActivity.mAppState.setInterface();
    }

    @Override
    public void bluetoothNotPresent() {

        mainActivity.setBTLogoDisconnected();
        mainActivity.addError(MainActivity.AppErrors.ERROR_NO_BT);

        // No error message during startup
        if (!blockError) {
            mainActivity.setState(mainActivity.getStateError());
            mainActivity.mAppState.setInterface();
            stopConnecting();
        }
    }

    @Override
    public void batteryLow() {
        mainActivity.removeError(MainActivity.AppErrors.ERROR_BATT_CRITICAL);
        mainActivity.addError(MainActivity.AppErrors.ERROR_BATT_LOW);
    }

    @Override
    public void batteryCritical() {
        stopConnecting();
        mainActivity.removeError(MainActivity.AppErrors.ERROR_BATT_LOW);
        mainActivity.addError(MainActivity.AppErrors.ERROR_BATT_CRITICAL);
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
        // No quest during connecting state
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

    private void stopConnecting() {
        qpa.getMenuPage().mDots.setVisibility(View.INVISIBLE);
        mTaskHandler.removeCallbacks(mConnectingRunnable);
        mTaskHandler.removeCallbacks(mDotRunnable);
        mTaskHandler.removeCallbacks(mPollBTRunnable);
        blockError = false;

        mainActivity.setState(mainActivity.getStateError());
        mainActivity.mAppState.setInterface();
    }
}
