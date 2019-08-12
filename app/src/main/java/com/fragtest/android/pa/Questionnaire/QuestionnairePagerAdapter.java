package com.fragtest.android.pa.Questionnaire;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.Units;
import com.fragtest.android.pa.Core.Vibration;
import com.fragtest.android.pa.MainActivity;
import com.fragtest.android.pa.Menu.Help;
import com.fragtest.android.pa.Menu.MenuPage;
import com.fragtest.android.pa.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ulrikkowalk on 28.02.17.
 */

public class QuestionnairePagerAdapter extends PagerAdapter {

    private static String LOG = "Quest..PagerAdapter";
    final ViewPager mViewPager;
    public  MainActivity mMainActivity;
    //private final Context mContext;
    private final Handler mCountDownHandler = new Handler();
    private final int mUpdateIntervalStatusBar = 1000;
    private final int mUpdateIntervalBattery = 1000*60;
    private final int mUpdateIntervalTime = 1000*10;
    // Stores all active Views
    ArrayList<QuestionViewActive> mListOfActiveViews;
    // Stores all Views
    ArrayList<QuestionViewActive> mListOfViewsStorage;
    private boolean isCountDownRunning = false;
    private boolean isInForeGround = false;
    private boolean isMenu = false;
    //private boolean isQuestionnaireActive = false;
    private boolean needsIncreasing = false;
    private boolean isPrefsInForeGround = false;
    private int mCountDownInterval = 30;
    private int mNUM_PAGES;
    private int mMAX_ALLOWED = 1;
    private int mFinalCountdown = -255;
    private int mSecondsRemaining = 120;
    private String mHead, mFoot, mSurveyURI, mVersion;
    private Questionnaire mQuestionnaire;
    private MenuPage mMenuPage;
    private Help mHelpScreen;
    private boolean isImmersive = false;
    private Units mUnits;
    private Vibration mVibration;
    private float batteryPlaceholderWeight;
    private int[] batteryStates;
    private boolean bBatteryCritical = false;
    private boolean isCharging = false;
    private int mBatteryState = -1;
    private float batteryLevel = 1.0f;
    private String mMotivation = "";
    private ArrayList<String> mQuestionList;

    private IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    private Intent batteryStatus;

    private final Runnable mCountDownRunnable = new Runnable() {
        @Override
        public void run() {
            if (isCountDownRunning) {
                mSecondsRemaining = mFinalCountdown - (int) (System.currentTimeMillis() / 1000);
                updateCountDown();
                mCountDownHandler.postDelayed(this, mUpdateIntervalStatusBar);
            }
        }
    };

    private final Runnable mBatteryRunnable = new Runnable() {
        @Override
        public void run() {
            getBatteryInfo();
            setBatteryLogo();
            checkBatteryCritical();
            mCountDownHandler.postDelayed(this, mUpdateIntervalBattery);
        }
    };

    private final Runnable mTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (isMenu) {
                mMenuPage.setTime();
                mCountDownHandler.postDelayed(this, mUpdateIntervalTime);
            }
        }
    };

    public final Runnable mSetProgressBarFullRunnable = new Runnable() {
        @Override
        public void run() {
                setQuestionnaireProgressBar(0.9999f);
        }
    };

    public QuestionnairePagerAdapter(MainActivity mainActivity, ViewPager viewPager, boolean immersive) {
        mMainActivity = mainActivity;
        mViewPager = viewPager;
        mVersion = mMainActivity.getVersion();
        isImmersive = immersive;
        mUnits = new Units(mMainActivity);
        batteryPlaceholderWeight = mMainActivity.getResources().getIntArray(R.array.battery_placeholder_weight)[0]*0.01f;
        batteryStates = mMainActivity.getResources().getIntArray(R.array.batteryStates);
        mVibration =  new Vibration(mMainActivity);
        handleControls();

    }

    public void setIsCharging(boolean val) {
        isCharging = val;
    }

    public MenuPage getMenuPage() {
        return mMenuPage;
    }

    // Calculation of remaining time and visual update
    private void updateCountDown() {

        if (mSecondsRemaining >= 0) {
            mMenuPage.updateCountdownText(mSecondsRemaining);
            setQuestionnaireProgressBar((float) mSecondsRemaining / mCountDownInterval);
        } else {
            stopCountDown();
        }
    }

    // Instate new countdown target
    public void setFinalCountDown(int finalCountDown, int countDownInterval) {
        mFinalCountdown = finalCountDown;
        mCountDownInterval = countDownInterval;
    }

    // Start/restart countdown and determine validity
    public void startCountDown() {
        if ((mFinalCountdown - System.currentTimeMillis() / 1000) >= 0) {
            isCountDownRunning = true;
            mCountDownHandler.post(mCountDownRunnable);
        } else {
            stopCountDown();
            setQuestionnaireProgressBar(0f);
        }
    }

    // End/Stop countdown
    public void stopCountDown() {
        isCountDownRunning = false;
        mCountDownHandler.removeCallbacks(mCountDownRunnable);
    }

    // Initialise menu with visible countdown
    public void createMenu() {
        backToMenu();
        needsIncreasing = false;
        setBatteryLogo();
        sendMessage(ControlService.MSG_QUESTIONNAIRE_INACTIVE);
    }

    public void backToMenu() {

        isMenu = true;
        sendMessage(ControlService.MSG_ISMENU);
        // Instantiates a MenuPage Object based on Contents of raw XML File
        mMenuPage = new MenuPage(mMainActivity, this);
        mNUM_PAGES = 1;
        mViewPager.setOffscreenPageLimit(0);

        mListOfActiveViews = new ArrayList<>();
        mListOfViewsStorage = new ArrayList<>();

        createMenuLayout();
        setControlsMenu();

        onResume();

        mCountDownHandler.post(mTimeRunnable);
    }

    public void createHelpScreen() {

        // Instantiates a MenuPage Object based on Contents of raw XML File
        mHelpScreen = new Help(mMainActivity, this);
        mNUM_PAGES = 1;
        mViewPager.setOffscreenPageLimit(0);

        mListOfActiveViews = new ArrayList<>();
        mListOfViewsStorage = new ArrayList<>();

        createHelpLayout();
    }

    public void createHelpLayout() {

        LinearLayout layout = mHelpScreen.generateView();

        layout.setId(0);
        // Adds the Layout to List carrying all ACTIVE Views
        mListOfActiveViews.add(new QuestionViewActive(layout, layout.getId(),
                0, true, null));
        // Adds the Layout to List storing ALL Views
        mListOfViewsStorage.add(new QuestionViewActive(layout, layout.getId(),
                0, true, null));

        notifyDataSetChanged();
        mViewPager.setCurrentItem(0);
    }

    // Initialise questionnaire based on new input parameters
    public void createQuestionnaire(ArrayList<String> questionList, String head, String foot,
                                    String surveyUri, String motivation) {

        isMenu = false;
        stopCountDown();
        sendMessage(ControlService.MSG_QUESTIONNAIRE_ACTIVE);
        mQuestionList = questionList;
        mHead = head;
        mFoot = foot;
        mSurveyURI = surveyUri;
        mMotivation = motivation;

        // Instantiates a Questionnaire Object based on Contents of raw XML File
        mQuestionnaire = new Questionnaire(mMainActivity, mHead, mFoot, mSurveyURI,
                mMotivation, mVersion, this);
        mQuestionnaire.setUp(questionList);
        mNUM_PAGES = mQuestionnaire.getNumPages();
        mViewPager.setOffscreenPageLimit(1);

        mListOfActiveViews = new ArrayList<>();
        mListOfViewsStorage = new ArrayList<>();

        createQuestionnaireLayout();
        setControlsQuestionnaire();
        // Creates and destroys views based on filter id settings
        mQuestionnaire.checkVisibility();

        notifyDataSetChanged();
        mViewPager.setCurrentItem(0);
        setArrows(0);
        setQuestionnaireProgressBar();
    }

    // Initialise questionnaire based on last input parameters (only used in case of reversion)
    public void createQuestionnaire() {

        // Instantiates a Questionnaire Object based on Contents of raw XML File
        mQuestionnaire = new Questionnaire(mMainActivity, mHead, mFoot, mSurveyURI,
                mMotivation, mVersion, this);

        mQuestionnaire.setUp(mQuestionList);

        mNUM_PAGES = mQuestionnaire.getNumPages();
        mViewPager.setOffscreenPageLimit(1);

        mListOfActiveViews = new ArrayList<>();
        mListOfViewsStorage = new ArrayList<>();

        createQuestionnaireLayout();
        setControlsQuestionnaire();
        // Creates and destroys views based on filter id settings
        mQuestionnaire.checkVisibility();

        notifyDataSetChanged();
        mViewPager.setCurrentItem(0);
        setArrows(0);
        setQuestionnaireProgressBar();
        needsIncreasing = false;
    }

    public void hideQuestionnaireProgressBar() {
        View progress = mMainActivity.mProgress;
        View regress = mMainActivity.mRegress;

        float nProgress = 0;
        float nRegress = 1;

        LinearLayout.LayoutParams progParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                nRegress
        );
        LinearLayout.LayoutParams regParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                nProgress
        );

        progress.setLayoutParams(progParams);
        regress.setLayoutParams(regParams);
    }

    public void fillQuestionnaireProgressBar() {
        View progress = mMainActivity.mProgress;
        View regress = mMainActivity.mRegress;

        float nProgress = 1;
        float nRegress = 0;

        LinearLayout.LayoutParams progParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                nRegress
        );
        LinearLayout.LayoutParams regParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                nProgress
        );

        progress.setLayoutParams(progParams);
        regress.setLayoutParams(regParams);
    }

    // Set the horizontal Indicator at the Top to follow Page Position
    public void setQuestionnaireProgressBar(int position) {

        View progress = mMainActivity.mProgress;
        View regress = mMainActivity.mRegress;

        int nAccuracy = 100;
        float nProgress = (float) (position + 1) / mViewPager.getAdapter().getCount() * nAccuracy;
        float nRegress = (nAccuracy - nProgress);

        LinearLayout.LayoutParams progParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                nRegress
        );
        LinearLayout.LayoutParams regParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                nProgress
        );

        progress.setLayoutParams(progParams);
        regress.setLayoutParams(regParams);
    }

    // Set the horizontal Indicator at the Top to follow Page Position
    public void setQuestionnaireProgressBar() {

        int nAccuracy = 100;

        View progress = mMainActivity.mProgress;
        View regress = mMainActivity.mRegress;

        float nProgress = (float) (mViewPager.getCurrentItem() + 1) /
                mViewPager.getAdapter().getCount() * nAccuracy;
        float nRegress = (nAccuracy - nProgress);

        LinearLayout.LayoutParams progParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                nRegress
        );
        LinearLayout.LayoutParams regParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                nProgress
        );

        progress.setLayoutParams(progParams);
        regress.setLayoutParams(regParams);
    }

    // Set the horizontal Indicator at the Top to follow Page Position
    public void setQuestionnaireProgressBar(float fraction) {

        View progress = mMainActivity.mProgress;
        View regress = mMainActivity.mRegress;

        float nProgress = fraction;
        float nRegress = 1f - nProgress;

        LinearLayout.LayoutParams progParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                nProgress
        );
        LinearLayout.LayoutParams regParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                nRegress
        );

        progress.setLayoutParams(progParams);
        regress.setLayoutParams(regParams);
    }

    // Adjust visibility of navigation symbols to given state
    public void setArrows(int position) {

        if (position == 0) {
            mMainActivity.mArrowBack.setVisibility(View.INVISIBLE);
        } else if (mMainActivity.mArrowBack.getVisibility() == View.INVISIBLE) {
            mMainActivity.mArrowBack.setVisibility(View.VISIBLE);
        }

        if (position == mViewPager.getAdapter().getCount() - 1) {
            mMainActivity.mArrowForward.setVisibility(View.INVISIBLE);
        } else if (mMainActivity.mArrowForward.getVisibility() == View.INVISIBLE) {
            mMainActivity.mArrowForward.setVisibility(View.VISIBLE);
        }
    }

    // Add new page to display
    public int addView(View view, int position, int positionInRaw, boolean mandatory,
                       List<Answer> listOfAnswers) {

        mListOfActiveViews.add(new QuestionViewActive(view, view.getId(), positionInRaw, mandatory,
                listOfAnswers));
        // Sort the Views by their id (implicitly their determined order)
        Collections.sort(mListOfActiveViews);
        return position;
    }

    // Inserts contents in blank menu
    private void createMenuLayout() {

        LinearLayout layout = mMenuPage.generateView();
        mMenuPage.updateCountDownText("");
        mMenuPage.setText(mMainActivity.getResources().getString(R.string.menuText));
        mMenuPage.resetStartTextSize();

        layout.setId(0);
        // Adds the Layout to List carrying all ACTIVE Views
        mListOfActiveViews.add(new QuestionViewActive(layout, layout.getId(),
                0, true, null));
        // Adds the Layout to List storing ALL Views
        mListOfViewsStorage.add(new QuestionViewActive(layout, layout.getId(),
                0, true, null));

        notifyDataSetChanged();
        mViewPager.setCurrentItem(0);
    }

    // Sets up visible control elements for menu i.e. status bar
    private void setControlsMenu() {

        mMainActivity.mLogo.setText(R.string.menuHelp);
        mMainActivity.mArrowForward.setVisibility(View.INVISIBLE);
        mMainActivity.mArrowBack.setVisibility(View.INVISIBLE);
        mMainActivity.mRevert.setVisibility(View.INVISIBLE);
        mMainActivity.mProgress.setBackgroundColor(
                ContextCompat.getColor(mMainActivity, R.color.JadeRed));
        mMainActivity.mRegress.setBackgroundColor(
                ContextCompat.getColor(mMainActivity, R.color.JadeGray));
        mMainActivity.mLogo.setEnabled(true);
    }

    // Sets up visible control elements for questionnaire i.e. navigation symbols
    private void setControlsQuestionnaire() {

        mMainActivity.mLogo.setText("IHAB");
        mMainActivity.mArrowForward.setVisibility(View.VISIBLE);
        mMainActivity.mArrowBack.setVisibility(View.VISIBLE);
        mMainActivity.mRevert.setVisibility(View.VISIBLE);
        mMainActivity.mProgress.setBackgroundColor(
                ContextCompat.getColor(mMainActivity, R.color.JadeRed));
        mMainActivity.mRegress.setBackgroundColor(
                ContextCompat.getColor(mMainActivity, R.color.JadeGray));
        if (MainActivity.USE_DEVELOPER_MODE) {
            mMainActivity.mLogo.setEnabled(true);
        } else {
            mMainActivity.mLogo.setEnabled(false);
        }
    }

    // Inserts contents into questionnaire and appoints recycler
    private void createQuestionnaireLayout() {
        // Generate a view for each page/question and collect them in ArrayList
        for (int iQuestion = 0; iQuestion < mNUM_PAGES; iQuestion++) {
            // Extracts Question Details from Questionnaire and creates Question
            Question question = mQuestionnaire.createQuestion(iQuestion);
            // Inflates Question Layout based on Question Details
            LinearLayout layout = mQuestionnaire.generateView(question, isImmersive);
            // Sets Layout Id to Question Id
            layout.setId(mQuestionnaire.getId(question));
            // Adds the Layout to List carrying all ACTIVE Views
            mListOfActiveViews.add(new QuestionViewActive(layout, layout.getId(),
                    iQuestion, question.isMandatory(), question.getAnswers()));
            // Adds the Layout to List storing ALL Views
            mListOfViewsStorage.add(new QuestionViewActive(layout, layout.getId(),
                    iQuestion, question.isMandatory(), question.getAnswers()));
        }
    }

    // Set up control elements on top menu
    private void handleControls() {

        mMainActivity.mLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isMenu) {
                    createHelpScreen();
                } else {
                    mMainActivity.mAppState.finishQuest();
                }
            }
        });
        mMainActivity.mArrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mViewPager.getCurrentItem() != 0) {
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
                }
            }
        });
        mMainActivity.mArrowForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mViewPager.getCurrentItem() < mViewPager.getAdapter().getCount() - 1)) {
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                }
            }
        });
        mMainActivity.mRevert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mMainActivity, R.string.infoTextRevert, Toast.LENGTH_SHORT).show();
                createQuestionnaire();
            }
        });

        mCountDownHandler.post(mBatteryRunnable);
    }


    /**
     * Battery Related Methods
     */


    private void setBatteryLogo() {
        LinearLayout.LayoutParams regparams = new LinearLayout.LayoutParams(
                mUnits.convertDpToPixels(12),
                0,
                (1.0f-2* batteryPlaceholderWeight)*(1.0f - batteryLevel)
        );
        regparams.leftMargin = mUnits.convertDpToPixels(0);
        regparams.rightMargin = mUnits.convertDpToPixels(10);
        mMainActivity.mBatteryReg.setLayoutParams(regparams);

        LinearLayout.LayoutParams progparams = new LinearLayout.LayoutParams(
                mUnits.convertDpToPixels(12),
                0,
                (1.0f-2* batteryPlaceholderWeight)* batteryLevel
        );
        progparams.leftMargin = mUnits.convertDpToPixels(0);
        progparams.rightMargin = mUnits.convertDpToPixels(0);
        mMainActivity.mBatteryProg.setLayoutParams(progparams);
    }

    private void getBatteryInfo() {

        batteryStatus = mMainActivity.registerReceiver(null, batteryFilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        batteryLevel = level / (float) scale;
        setBatteryColor();

        if (ControlService.useLogMode) {
            // Send battery level info to Control Service for logging etc.
            Bundle batteryLevelBundle = new Bundle();
            batteryLevelBundle.putFloat("batteryLevel", batteryLevel);
            sendMessage(ControlService.MSG_BATTERY_LEVEL_INFO, batteryLevelBundle);
        }
    }
    private void setBatteryColor() {

        if (batteryLevel*100 <= batteryStates[1]) {
            mMainActivity.mBatteryProg.setBackgroundColor(mMainActivity.getResources().getColor(R.color.JadeRed));
        } else if (batteryLevel*100 >= batteryStates[1] && batteryLevel*100 <= batteryStates[0]) {
            mMainActivity.mBatteryProg.setBackgroundColor(mMainActivity.getResources().getColor(R.color.BatteryYellow));
        } else if (batteryLevel*100 > batteryStates[0]) {
            mMainActivity.mBatteryProg.setBackgroundColor(mMainActivity.getResources().getColor(R.color.BatteryGreen));
        }
    }

    public void checkBatteryCritical() {

        if (batteryLevel*100 <= batteryStates[1] && mBatteryState != 2) {
            bBatteryCritical = true;
            mBatteryState = 2;
            announceBatteryCritical();
            mMenuPage.showErrorList();
        } else if (batteryLevel*100 > batteryStates[1] && batteryLevel*100 <= batteryStates[0] && mBatteryState != 1) {
            bBatteryCritical = false;
            mBatteryState = 1;
            announceBatteryWarning();
            mMenuPage.showErrorList();
        } else if (batteryLevel*100 > batteryStates[0] && mBatteryState != 0) {
            bBatteryCritical = false;
            mBatteryState = 0;
            announceBatteryNormal();
        }
    }

    private void announceBatteryWarning() {
        mMainActivity.mAppState.batteryLow();
        mVibration.singleBurst();
    }

    private void announceBatteryCritical() {
        mMainActivity.mAppState.batteryCritical();
        sendMessage(ControlService.MSG_BATTERY_CRITICAL);
    }

    private void announceBatteryNormal() {
        mMainActivity.mAppState.batteryNormal();
        mVibration.singleBurst();
    }



    /**
     * Array Adapter Methods
     * */


    // Removes specific view from list and updates viewpager
    int removeView(int position) {

        int nCurrentItem = mViewPager.getCurrentItem();
        mViewPager.setAdapter(null);
        mListOfActiveViews.remove(position);
        mViewPager.setAdapter(this);
        mViewPager.setCurrentItem(nCurrentItem);
        return position;
    }

    // Returns position of element with given id in viewpager
    int getPositionFromId(int iId) {
        for (int iItem = 0; iItem < mListOfActiveViews.size(); iItem++) {
            if (mListOfActiveViews.get(iItem).getId() == iId) {
                return iItem;
            }
        }
        return -1;
    }

    // Takes view out of viewpager and includes it in displayable collection
    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        View view = mListOfActiveViews.get(position).getView();
        collection.addView(view);
        return view;
    }

    // Removes view from displayable collection
    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    // Returns number of pages in viewpager
    @Override
    public int getCount() {
        if (!(mListOfActiveViews == null) && !(mListOfActiveViews.size() == 0)) {
            mNUM_PAGES = mListOfActiveViews.size();
        } else {
            mNUM_PAGES = 0;
        }
        return mNUM_PAGES;
    }

    public int getMaxAllowed() {
        return mMAX_ALLOWED;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    // Announce change in contents and invoke rehash
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    // Returns position of object in displayed list
    @Override
    public int getItemPosition(Object object) {
        int index = mListOfActiveViews.indexOf(object);

        if (index == -1)
            return POSITION_NONE;
        else
            return index;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    // Information about current state (menu/questionnaire)
    public boolean isMenu() {
        return isMenu;
    }

    // Message service for communication with ControlService
    public void sendMessage(int what) {
        mMainActivity.messageService(what);
    }

    // Message service for communication with ControlService
    public void sendMessage(int what, Bundle data) {
        mMainActivity.messageService(what, data);
    }

    public void setPrefsInForeGround(boolean state) {
        isPrefsInForeGround = state;
    }


    /**
     * Lifecycle methods
     **/


    public void onResume() {
        setBatteryLogo();
        isInForeGround = true;
    }

    public void onPause() {
        isInForeGround = false;
    }

}