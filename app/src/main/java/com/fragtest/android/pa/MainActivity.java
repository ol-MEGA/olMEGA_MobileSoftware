package com.fragtest.android.pa;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.fragtest.android.pa.AppStates.AppState;
import com.fragtest.android.pa.AppStates.StateCharging;
import com.fragtest.android.pa.AppStates.StateConnecting;
import com.fragtest.android.pa.AppStates.StateError;
import com.fragtest.android.pa.AppStates.StateProposing;
import com.fragtest.android.pa.AppStates.StateQuest;
import com.fragtest.android.pa.AppStates.StateRunning;
import com.fragtest.android.pa.Core.FileIO;
import com.fragtest.android.pa.Core.LogIHAB;
import com.fragtest.android.pa.Core.MessageList;
import com.fragtest.android.pa.InputProfile.INPUT_CONFIG;
import com.fragtest.android.pa.Questionnaire.QuestionnairePagerAdapter;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.fragtest.android.pa.ControlService.MSG_APPLICATION_SHUTDOWN;
import static com.fragtest.android.pa.ControlService.MSG_CHANGE_PREFERENCE;
import static com.fragtest.android.pa.ControlService.MSG_CHARGING_OFF;
import static com.fragtest.android.pa.ControlService.MSG_CHARGING_ON;
import static com.fragtest.android.pa.ControlService.MSG_NO_QUESTIONNAIRE_FOUND;
import static com.fragtest.android.pa.ControlService.MSG_SET_COUNTDOWN_TIME;


public class MainActivity extends AppCompatActivity {

    private boolean ALLOW_KIOSK_MODE_DISABLED = false;
    private boolean USE_KIOSK_MODE = false;
    //public static boolean USE_DEVELOPER_MODE = false;
    private Locale LANGUAGE_CODE = Locale.GERMANY;
    //private Locale LANGUAGE_CODE = Locale.ENGLISH;

    static final String LOG = "MainActivity";
    private static final String KEY_PREFS_IN_FOREGROUND = "prefsInForeGround";

    // RELEVANT FOR PERMISSIONS (Android 6+, just in case)
    private int requestIterator = 0;
    private boolean permissionGranted = false;
    private int nPermissions = 8;
    private int iPermission;
    private String[] requestString;
    private final static int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 0;
    private final static int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 1;
    private final static int MY_PERMISSIONS_RECEIVE_BOOT_COMPLETED = 2;
    private final static int MY_PERMISSIONS_RECORD_AUDIO = 3;
    private final static int MY_PERMISSIONS_VIBRATE = 4;
    private final static int MY_PERMISSIONS_WAKE_LOCK = 5;
    private final static int MY_PERMISSIONS_DISABLE_KEYGUARD = 6;
    private final static int MY_PERMISSIONS_CAMERA = 7;

    public INPUT_CONFIG mServiceState;

    final Messenger mMessageHandler = new Messenger(new MessageHandler());
    private Messenger mServiceMessenger;
    public Handler mTaskHandler = new Handler();
    private MessageList mMessageList;

    public ViewPager mViewPager;
    private QuestionnairePagerAdapter mAdapter;
    public TextView mLogo;
    public View mRecord, mArrowBack, mArrowForward, mRevert, mProgress, mRegress, mConfig,
            mBatteryReg, mBatteryProg, mCharging;

    private boolean mServiceIsBound;
    private boolean isCharging = false;
    private boolean mServiceIsRecording;
    private boolean isPrefsInForeGround = false;
    private boolean isActivityRunning = false;
    private boolean isQuestionnairePresent = true;
    //private boolean isCharging = false;
    private boolean isTimer = false;
    //private boolean showConfigButton = false;
    //private boolean showRecordingButton = true;
    private boolean isBluetoothPresent = false;

    //private long durationTemp = 0;
    private long durationLongClick = 5*1000;

    // RELEVANT FOR KIOSK MODE
    private FileIO mFileIO;
    private ComponentName mAdminComponentName;
    private DevicePolicyManager mDevicePolicyManager;
    private final List blockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP));

    // Preferences
    private SharedPreferences sharedPreferences;

    // States
    public AppState mAppState;
    private StateCharging mStateCharging;
    private StateConnecting mStateConnecting;
    private StateError mStateError;
    private StateProposing mStateProposing;
    private StateQuest mStateQuest;
    private StateRunning mStateRunning;

    private static boolean bRecordSwipes = true;
    // Forced Answers (no answer no swipe)
    private int falseSwipes = 0;
    private boolean isForcedAnswer, isForcedAnswerDialog;

    // Context
    private static Context mStatContext;

    public static Context getContext() {
        return mStatContext;
    }

    // Errors
    public enum AppErrors {
        ERROR_NO_BT, ERROR_BATT_LOW, ERROR_BATT_CRITICAL, ERROR_NO_QUEST;

        public String getErrorMessage() {
            switch (this) {
                case ERROR_NO_BT:
                    return mStatContext.getResources().getString(R.string.noBluetooth);
                case ERROR_BATT_LOW:
                    return mStatContext.getResources().getString(R.string.batteryWarning);
                case ERROR_BATT_CRITICAL:
                    return mStatContext.getResources().getString(R.string.batteryCritical);
                case ERROR_NO_QUEST:
                    return mStatContext.getResources().getString(R.string.noQuestionnaires);
                default:
                    return "generic error message";
            }
        }
    }

    public ArrayList<String> mErrorList = new ArrayList<>();

    public void addError(AppErrors error) {
        if (!mErrorList.contains(error.getErrorMessage())) {
            // In case of Standalone Mode, no BT error is needed
            if (!(ControlService.isStandalone && error == AppErrors.ERROR_NO_BT)) {
                mErrorList.add(error.getErrorMessage());
            }
        }
    }

    public void removeError(AppErrors error) {
        if (mErrorList.contains(error.getErrorMessage())) {
            mErrorList.remove(error.getErrorMessage());
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Log.e(LOG, "ON SERVICE CONNECTED");

            mServiceMessenger = new Messenger(service);

            //TODO: Experimental - Grant permission and suppress "Bluetooth Permission" - problem
            //mDevicePolicyManager.setPermissionPolicy(mAdminComponentName, DevicePolicyManager.PERMISSION_POLICY_AUTO_GRANT);

            messageService(ControlService.MSG_REGISTER_CLIENT);
            messageService(ControlService.MSG_GET_STATUS);

            LogIHAB.log("Processing message list of length: " + mMessageList.getLength());
            mMessageList.work();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceMessenger = null;
        }
    };

    @SuppressWarnings("deprecation")
    private void setSystemLocale() {
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();

        // Automatically set to specific language
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            configuration.setLocale(LANGUAGE_CODE);
        } else{
            configuration.locale = LANGUAGE_CODE;
        }
        */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getApplicationContext().createConfigurationContext(configuration);
        } else {
            resources.updateConfiguration(configuration, displayMetrics);
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :
                manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ControlService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    void doBindService() {
        if (!isServiceRunning() && permissionGranted) {
            startService(new Intent(this, ControlService.class));
        }
        if (permissionGranted) {
            bindService(new Intent(this, ControlService.class),
                    mConnection, Context.BIND_AUTO_CREATE);
            mServiceIsBound = true;
        }
    }

    void doUnbindService() {
        if (mServiceIsBound) {
            messageService(ControlService.MSG_UNREGISTER_CLIENT);
            unbindService(mConnection);
            mServiceIsBound = false;
        }
    }

    public void handleNewPagerAdapter() {
        mViewPager = null;
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mAdapter = new QuestionnairePagerAdapter(this, mViewPager, getKioskMode());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.addOnPageChangeListener(myOnPageChangeListener);
    }

    private void connectToDevice(String sDeviceName) {

    }

    private ViewPager.OnPageChangeListener myOnPageChangeListener =
            new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageScrollStateChanged(int state) {
                }

                @Override
                public void onPageScrolled(int position,
                                           float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    // In case of forced answers, no forward swiping is allowed on unanswered questions

                    if (!mAdapter.getHasQuestionBeenAnswered() && mAdapter.getHasQuestionForcedAnswer() && isForcedAnswer) {
                        mAdapter.setQuestionnaireProgressBar(position - 1);
                        mAdapter.setArrows(position - 1);
                        mViewPager.setCurrentItem(position - 1, true);
                        if (bRecordSwipes) {
                            falseSwipes += 1;
                        }
                        Log.e(LOG, "False Swipes: " + falseSwipes);
                        if (bRecordSwipes && isForcedAnswer && isForcedAnswerDialog && falseSwipes > 2) {
                            messageFalseSwipes();
                        }
                    } else {
                        mAdapter.setQuestionnaireProgressBar(position);
                        mAdapter.setArrows(position);
                        mViewPager.setCurrentItem(position, true);
                    }
                }
            };

    private void shipPreferencesToControlService() {
        // Load information from shared preferences and bundle them
        Bundle dataPreferences = new Bundle();
        // String
        dataPreferences.putString("whichQuest", sharedPreferences.getString("whichQuest", ""));
        dataPreferences.putString("samplerate", sharedPreferences.getString("samplerate", "" + InitValues.samplerate));
        dataPreferences.putString("chunklengthInS", sharedPreferences.getString("chunklengthInS", "" + InitValues.chunklengthInS));
        dataPreferences.putString("filterHpFrequency", sharedPreferences.getString("filterHpFrequency", "" + InitValues.filterHpFrequency));
        dataPreferences.putString("inputProfile", sharedPreferences.getString("inputProfile", "STANDALONE"));
        dataPreferences.putString("listDevices", sharedPreferences.getString("listDevices", ""));
        // Boolean
        dataPreferences.putBoolean("isWave", sharedPreferences.getBoolean("isWave", InitValues.isWave));
        dataPreferences.putBoolean("isTimer", sharedPreferences.getBoolean("isTimer", InitValues.isTimer));
        //dataPreferences.putBoolean("isLocked", sharedPreferences.getBoolean("isLocked", InitValues.isLocked));
        dataPreferences.putBoolean("keepAudioCache", sharedPreferences.getBoolean("keepAudioCache", InitValues.keepAudioCache));
        dataPreferences.putBoolean("downsample", sharedPreferences.getBoolean("downsample", InitValues.downsample));
        dataPreferences.putBoolean("showConfigButton", sharedPreferences.getBoolean("showConfigButton", InitValues.showConfigButton));
        dataPreferences.putBoolean("showRecordingButton", sharedPreferences.getBoolean("showRecordingButton", InitValues.showRecordingButton));
        dataPreferences.putBoolean("filterHp", sharedPreferences.getBoolean("filterHp", InitValues.filterHp));
        // String Set
        Set<String> activeFeatures = sharedPreferences.getStringSet("features", null);
        // String Set cannot be bundled natively, cast to ArrayList
        ArrayList<String> listActiveFeatures = new ArrayList<>();
        listActiveFeatures.addAll(activeFeatures);
        dataPreferences.putStringArrayList("features", listActiveFeatures);

        messageService(ControlService.MSG_CHECK_FOR_PREFERENCES, dataPreferences);
    }

    public String getVersion() {
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void incrementPage() {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
    }


    /**
     * State Affairs
     **/


    public void setState(AppState newAppState) {
        mAppState = newAppState;
    }

    public AppState getStateCharging() {
        return mStateCharging;
    }

    public AppState getStateConnecting() {
        return mStateConnecting;
    }

    public AppState getStateError() {
        return mStateError;
    }

    public AppState getStateProposing() {
        return mStateProposing;
    }

    public AppState getStateQuest() {
        return mStateQuest;
    }

    public AppState getStateRunning() {
        return mStateRunning;
    }

    public boolean getIsCharging() {
        return isCharging;
    }

    /**
     * QPA Modifiers
     **/


    public void setIsCharging(boolean val) {
        isCharging = val;
    }

    public void finishQuestionnaire() {
        mAppState.finishQuest();
        messageService(ControlService.MSG_QUESTIONNAIRE_FINISHED);
    }

    public void setLogoActive() {

        if (mServiceState == null) {
            mServiceState = INPUT_CONFIG.STANDALONE;
        }

        Log.e(LOG, "SERIVCE STATE: " + mServiceState.toString());

        switch (mServiceState) {

            case A2DP:
                mRecord.setBackgroundTintList(
                        ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),
                                R.color.BatteryGreen, null)));
                break;

            case RFCOMM:
                mRecord.setBackgroundTintList(
                        ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),
                                R.color.RfcommViolet, null)));
                break;

            case PHANTOM:
                mRecord.setBackgroundTintList(
                        ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),
                                R.color.PhantomDarkBlue, null)));
                break;

            case USB:
                mRecord.setBackgroundTintList(
                        ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),
                                R.color.JadeRed, null)));
                break;

            case STANDALONE:
                mRecord.setBackgroundTintList(
                        ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),
                                R.color.AirplaneBlue, null)));
                break;
            case INTERNAL_MIC:
                mRecord.setBackgroundTintList(
                        ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),
                                R.color.InternalMicOrange, null)));
                break;
        }
    }

    public void setLogoInactive() {
        mRecord.setBackgroundTintList(
                ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),
                        R.color.darkerGray, null)));
    }

    private void setConfigVisibility() {
        if (!getKioskMode()) {
            mConfig.setVisibility(View.VISIBLE);
        } else {
            mConfig.setVisibility(View.GONE);
        }
    }

    public static void stopRecordingFalseSwipes() {
        bRecordSwipes = false;
    }

    public static void startRecordingFalseSwipes() {
        bRecordSwipes = true;
    }

    private void messageFalseSwipes() {
        stopRecordingFalseSwipes();
        falseSwipes = 0;
        new AlertDialog.Builder(this, R.style.SwipeDialogTheme)
                .setTitle(R.string.app_name)
                .setMessage(R.string.swipeMessage)
                .setPositiveButton(R.string.swipeOkay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startRecordingFalseSwipes();
                    }
                })
                .setCancelable(false)
                .show();
    }

    // Timer enabling long click for user access to preferences menu
    private CountDownTimer timerLongClick = new CountDownTimer(durationLongClick, 200) {
        @Override
        public void onTick(long l) {}
        @Override
        public void onFinish() {
            startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
            isPrefsInForeGround = true;
            mAdapter.setPrefsInForeGround(isPrefsInForeGround);
        }
    };


    /**
     * Lifecycle methods
     */

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mStatContext = this;
        mMessageList = new MessageList(this);

        LogIHAB.log("Standalone Mode: " + ControlService.isStandalone);

        setSystemLocale();

        checkForPermissions();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Obtain last known system state from preferences (otherwise initialized as standalone)
        mServiceState = INPUT_CONFIG.toState(sharedPreferences.getString("serviceState", INPUT_CONFIG.STANDALONE.name()));


        if (!isActivityRunning) {
            super.onCreate(savedInstanceState);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            for (iPermission = 0; iPermission < nPermissions; iPermission++) {
                requestPermissions(iPermission);
            }

            setContentView(R.layout.activity_main);
            mLogo = (TextView) findViewById(R.id.Action_Logo);
            mRecord = findViewById(R.id.Action_Record);
            mArrowBack = findViewById(R.id.Action_Back);
            mArrowForward = findViewById(R.id.Action_Forward);
            mRevert = findViewById(R.id.Action_Revert);
            mProgress = findViewById(R.id.progress);
            mRegress = findViewById(R.id.regress);
            mConfig = findViewById(R.id.Action_Config);
            mBatteryProg = findViewById(R.id.battery_prog);
            mBatteryReg = findViewById(R.id.battery_reg);
            mCharging = findViewById(R.id.charging);

            mFileIO = new FileIO();

            // KIOSK mode can only ever be disabled if allowed to do so by file "config" in IHAB
            // directory and then still needs to be manually disabled via preferences menu
            ALLOW_KIOSK_MODE_DISABLED = mFileIO.checkConfigFile();
            boolean tmpEnableKioskMode = sharedPreferences.getBoolean("enableKioskMode", true);
            if (!tmpEnableKioskMode && ALLOW_KIOSK_MODE_DISABLED) {
                setKioskMode(false);
            } else {
                setKioskMode(true);
            }

            mConfig.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Intent intent = new Intent(getApplicationContext(), PreferencesActivity.class);
                    //startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);

                    startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
                    isPrefsInForeGround = true;
                    mAdapter.setPrefsInForeGround(isPrefsInForeGround);
                }
            });

            mLogo.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            //Start timer
                            timerLongClick.start();
                            break;
                        case MotionEvent.ACTION_UP:
                            //Clear timer
                            timerLongClick.cancel();
                            break;
                    }
                    return false;
                }
            });

            doBindService();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            handleNewPagerAdapter();

            mAdapter.createMenu();

            mStateCharging = new StateCharging(this, mAdapter);
            mStateError = new StateError(this, mAdapter);
            mStateProposing = new StateProposing(this, mAdapter);
            mStateQuest = new StateQuest(this, mAdapter);
            mStateRunning = new StateRunning(this, mAdapter);
            mStateConnecting = new StateConnecting(this, mAdapter);

            mAppState = mStateConnecting;

            mAppState.setInterface();

            mAdapter.checkBatteryCritical();

            isActivityRunning = true;
        }

        /*if (!Settings.System.canWrite(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + mStatContext.getPackageName()));
            startActivity(intent);
        }*/


        // KIOSK MODE
        ComponentName deviceAdmin = new ComponentName(this, AdminReceiver.class);
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDevicePolicyManager.setLockTaskPackages(deviceAdmin, new String[]{getPackageName()});
        mAdminComponentName = deviceAdmin;
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(
                Context.DEVICE_POLICY_SERVICE);


        setDefaultCosuPolicies(getKioskMode());
        setKioskMode(getKioskMode());
    }



    @Override
    protected void onDestroy() {
        if (BuildConfig.DEBUG) {
            Log.i(LOG, "onDestroy");
        }
        super.onDestroy();

        sharedPreferences.edit().putString("serviceState", mServiceState.name()).apply();

        messageService(MSG_APPLICATION_SHUTDOWN);
        doUnbindService();
    }

    @Override
    protected void onStart() {
        if (BuildConfig.DEBUG) {
            Log.i(LOG, "onStart");
        }
        super.onStart();
    }

    @Override
    protected void onRestart() {
        if (BuildConfig.DEBUG) {
            Log.i(LOG, "onRestart");
        }
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (BuildConfig.DEBUG) {
            Log.i(LOG, "onStop");
        }
    }

    @Override
    protected void onPause() {
        if (BuildConfig.DEBUG) {
            Log.i(LOG, "onPause");
        }
        mAdapter.onPause();
        super.onPause();

        // KIOSK MODE related
        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        if (getKioskMode()) {
            activityManager.moveTaskToFront(getTaskId(), 0);
        }
    }

    @Override
    protected void onResume() {
        if (BuildConfig.DEBUG) {
            Log.i(LOG, "onResume");
        }

        if (isPrefsInForeGround) {
            isPrefsInForeGround = false;
            mAdapter.setPrefsInForeGround(isPrefsInForeGround);

            //TODO: CHeck whether this still applies
            shipPreferencesToControlService();
        }
        mAdapter.onResume();
        super.onResume();

        isForcedAnswer = sharedPreferences.getBoolean("forceAnswer", true);
        isForcedAnswerDialog = sharedPreferences.getBoolean("forceAnswerDialog", true);

        // Unset the device admin programmatically so the app can be uninstalled.
        if (sharedPreferences.getBoolean("unsetDeviceAdmin", false)) {
            mDevicePolicyManager.clearDeviceOwnerApp(this.getPackageName());
        }

        setKioskMode(sharedPreferences.getBoolean("enableKioskMode", true));

        //String sDeviceName = sharedPreferences.getString("listDevices", "");
        //connectToDevice(sDeviceName);

        /*if (sharedPreferences.getBoolean("enableKioskMode", true)) {
            setKioskMode(true);
        } else {
            setKioskMode(false);
        }*/

        hideSystemUI(getKioskMode());
        setConfigVisibility();

        //Set the system brightness using the brightness variable value
        /*boolean maxBrightness = sharedPreferences.getBoolean("maxBrightness", false);
        if (maxBrightness) {
            LogIHAB.log(LOG + ": Setting display brightness to maximum.");
            //Settings.System.putInt(mResolver, Settings.System.SCREEN_BRIGHTNESS, 255);
            //Get the current window attributes
            WindowManager.LayoutParams layoutParams = mWindow.getAttributes();
            //Set the brightness of this window
            layoutParams.screenBrightness = 1f;//mBrightness / (float)255;
            //Apply attribute changes to this window
            mWindow.setAttributes(layoutParams);
        }*/

    }


    /**
     * KIOSK MODE RELATED STUFF
     */


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // Little hack since the Power button seems to be inaccessible at this point
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus && getKioskMode()) {
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            setKioskMode(true);
        }
    }

    // This disables the Volume Buttons
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        Log.e(LOG, "EVENT: " + event.getKeyCode());

        if (blockedKeys.contains(event.getKeyCode()) && getKioskMode()) {
            return true;
        } else if ((event.getKeyCode() == KeyEvent.KEYCODE_POWER) && getKioskMode()) {
            Log.e(LOG, "POWER BUTTON WAS PRESSED");
            return super.dispatchKeyEvent(event);
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    // When back button is pressed, questionnaire navigates one page backwards, menu does nothing
    @Override
    public void onBackPressed() {
        if (!mAdapter.isMenu() && getKioskMode()) {
            if (mViewPager.getCurrentItem() != 0) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
            } else {
                // Might be unsafe because this accidentally resets the timer and starts a new cycle
                //mAdapter.createMenu();
            }
        }
    }

    private void setDefaultCosuPolicies(boolean active) {
        // set user restrictions
        setUserRestriction(UserManager.DISALLOW_ADD_USER, active);
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, active);
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, false);
        setUserRestriction(UserManager.DISALLOW_CREATE_WINDOWS, active);
        Log.i(LOG, "KIOSK MODE: " + active);
        // disable keyguard and status bar - needs API 23 (Damnit!)
        //mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, active);
        //mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, active);
    }

    private void setUserRestriction(String restriction, boolean disallow) {
        if (disallow) {
            mDevicePolicyManager.addUserRestriction(mAdminComponentName,
                    restriction);
        } else {
            mDevicePolicyManager.clearUserRestriction(mAdminComponentName,
                    restriction);
        }
    }

    private void setKioskMode(boolean enabled) {
        try {
            if (enabled) {
                if (mDevicePolicyManager.isLockTaskPermitted(this.getPackageName())) {
                    startLockTask();
                    USE_KIOSK_MODE = true;
                } else {
                    stopLockTask();
                    Toast.makeText(this, "Kiosk not permitted", Toast.LENGTH_SHORT).show();
                    USE_KIOSK_MODE = false;
                }
            } else if (ALLOW_KIOSK_MODE_DISABLED) {
                USE_KIOSK_MODE = false;
                stopLockTask();
            }
            setConfigVisibility();
        } catch (Exception e) {
            Logger.info("Unable to start KIOSK mode");
            LogIHAB.log("Unable to start KIOSK mode");
        }
    }

    public boolean getKioskMode() {
        return USE_KIOSK_MODE;
    }

    public void hideSystemUI(boolean isImmersive) {
        if (isImmersive) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_VISIBLE
            );
        }
    }


    /**
     * PERMISSION STUFF (ANDROID 6+)
     */

    public void checkForPermissions() {

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)
                !=PackageManager.PERMISSION_GRANTED)
        {
            LogIHAB.log("Requesting permission to record audio.");
            requestPermissions(MY_PERMISSIONS_RECORD_AUDIO);
        }

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                !=PackageManager.PERMISSION_GRANTED)
        {
            LogIHAB.log("Requesting permission to record audio.");
            requestPermissions(MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
        }

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
                !=PackageManager.PERMISSION_GRANTED)
        {
            LogIHAB.log("Requesting permission to record audio.");
            requestPermissions(MY_PERMISSIONS_READ_EXTERNAL_STORAGE);
        }

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.VIBRATE)
                !=PackageManager.PERMISSION_GRANTED)
        {
            LogIHAB.log("Requesting permission to record audio.");
            requestPermissions(MY_PERMISSIONS_VIBRATE);
        }

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WAKE_LOCK)
                !=PackageManager.PERMISSION_GRANTED)
        {
            LogIHAB.log("Requesting permission to record audio.");
            requestPermissions(MY_PERMISSIONS_WAKE_LOCK);
        }

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECEIVE_BOOT_COMPLETED)
                !=PackageManager.PERMISSION_GRANTED)
        {
            LogIHAB.log("Requesting permission to record audio.");
            requestPermissions(MY_PERMISSIONS_RECEIVE_BOOT_COMPLETED);
        }
    }

    public void requestPermissions(int iPermission) {

        // TODO: Make array
        switch (iPermission) {
            case 0:
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_READ_EXTERNAL_STORAGE);
                break;
            case 1:
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
                break;
            case 2:
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED},
                        MY_PERMISSIONS_RECEIVE_BOOT_COMPLETED);
                break;
            case 3:
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);
                break;
            case 4:
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.VIBRATE},
                        MY_PERMISSIONS_VIBRATE);
                break;
            case 5:
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WAKE_LOCK},
                        MY_PERMISSIONS_WAKE_LOCK);
                break;
            case 6:
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.DISABLE_KEYGUARD},
                        MY_PERMISSIONS_DISABLE_KEYGUARD);
                break;
            case 7:
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_CAMERA);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        requestString = getResources().getStringArray(R.array.permissionMessages);
        switch (requestCode) {

            case MY_PERMISSIONS_RECORD_AUDIO: {
                //Toast.makeText(this, "Thanks for permission to record audio.", Toast.LENGTH_SHORT).show();

                break;
            }
            case MY_PERMISSIONS_RECEIVE_BOOT_COMPLETED: {
                //Toast.makeText(this, "Thanks for permission to receive boot completed.", Toast.LENGTH_SHORT).show();

                break;
            }
            case MY_PERMISSIONS_VIBRATE: {
                //Toast.makeText(this, "Thanks for permission to vibrate.", Toast.LENGTH_SHORT).show();

                break;
            }
            case MY_PERMISSIONS_WAKE_LOCK: {
                //Toast.makeText(this, "Thanks for permission for wake lock.", Toast.LENGTH_SHORT).show();

                break;
            }
            case MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
                //Toast.makeText(this, "Thanks for permission to write external storage.", Toast.LENGTH_SHORT).show();

                break;
            }
            case MY_PERMISSIONS_DISABLE_KEYGUARD: {
                //Toast.makeText(this, "Thanks for permission to disable keyguard.", Toast.LENGTH_SHORT).show();

                break;
            }
            case MY_PERMISSIONS_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = true;
                    //Toast.makeText(this, "Thanks for permission to read external storage.", Toast.LENGTH_SHORT).show();
                    doBindService();
                } else {
                    Toast.makeText(this, requestString[requestIterator % (requestString.length)], Toast.LENGTH_SHORT).show();
                    requestPermissions(iPermission);
                    requestIterator++;
                }
                break;
            }
        }
    }


    /** Message Handling */


    // Send message to connected client
    public void messageService(int what) {

        if (mServiceIsBound) {

            if (mServiceMessenger != null) {
                try {
                    Message msg = Message.obtain(null, what);
                    msg.replyTo = mMessageHandler;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                }
            }
        } else {
            mMessageList.addMessage(what);
            LogIHAB.log("Message was added to MessageList: " + what);
        }
    }

    // Send message to connected client
    public void messageService(int what, Bundle data) {

        if (mServiceIsBound) {

            if (mServiceMessenger != null) {
                try {
                    Message msg = Message.obtain(null, what);
                    msg.setData(data);
                    msg.replyTo = mMessageHandler;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                }
            }
        } else {
            mMessageList.addMessage(what, data);
            LogIHAB.log("Message was added to MessageList with data: " + what);
        }
    }

    class MessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            Log.d(LOG, "Message received: " + msg.what);

            switch (msg.what) {

                case ControlService.MSG_STATE_CHANGE:

                    setLogoInactive();

                    switch (msg.getData().getString("inputProfile", "")) {
                        case "A2DP":
                            mServiceState = INPUT_CONFIG.A2DP;
                            //removeError(AppErrors.ERROR_NO_USB);
                            break;
                        case "RFCOMM":
                            mServiceState = INPUT_CONFIG.RFCOMM;
                            //removeError(AppErrors.ERROR_NO_USB);
                            break;
                        case "PHANTOM":
                            mServiceState = INPUT_CONFIG.PHANTOM;
                            //removeError(AppErrors.ERROR_NO_USB);
                            break;
                        case "USB":
                            mServiceState = INPUT_CONFIG.USB;
                            //removeError(AppErrors.ERROR_NO_BT);
                            //setLogoActive();
                            break;
                        case "STANDALONE":
                            mServiceState = INPUT_CONFIG.STANDALONE;
                            //removeError(AppErrors.ERROR_NO_BT);
                            //removeError(AppErrors.ERROR_NO_USB);
                            //setLogoActive();
                            break;
                        case "INTERNAL_MIC":
                            mServiceState = INPUT_CONFIG.INTERNAL_MIC;
                            break;
                    }

                    Log.e(LOG, "IS CHARGING: " + getIsCharging());

                    if (!getIsCharging()) {
                        mAppState = mStateConnecting;
                    } else {
                        mAppState = mStateCharging;
                    }
                    mAppState.setInterface();
                    break;

                case MSG_CHARGING_ON:
                    mAppState.chargeOn();
                    setIsCharging(true);
                    break;

                case MSG_CHARGING_OFF:
                    mAppState.chargeOff();
                    setIsCharging(false);
                    break;

                case MSG_NO_QUESTIONNAIRE_FOUND:
                    mAppState.noQuest();
                    break;

                case MSG_CHANGE_PREFERENCE:

                    Bundle data = msg.getData();
                    if (data.getString("type").equals("boolean")) {
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).
                                edit().putBoolean(data.getString("key"), data.getBoolean("value")).
                                apply();
                    }
                    shipPreferencesToControlService();

                    break;

                case MSG_SET_COUNTDOWN_TIME:

                    int finalCountDown = msg.getData().getInt("finalCountDown");
                    int countDownInterval = msg.getData().getInt("countDownInterval");
                    mAdapter.setFinalCountDown(finalCountDown, countDownInterval);
                    mAppState.countdownStart();

                    break;

                case ControlService.MSG_START_QUESTIONNAIRE:

                    Bundle dataQuest = msg.getData();
                    ArrayList<String> questionList = dataQuest.getStringArrayList("questionList");
                    String head = dataQuest.getString("head");
                    String foot = dataQuest.getString("foot");
                    String surveyUri = dataQuest.getString("surveyUri");
                    String motivation = dataQuest.getString("motivation");
                    mAdapter.createQuestionnaire(questionList, head, foot, surveyUri, motivation);
                    mAppState.startQuest();

                    break;

                case ControlService.MSG_PROPOSE_QUESTIONNAIRE:

                    mAppState.countdownFinish();

                    break;

                case ControlService.MSG_GET_STATUS:

                    // Set UI to match ControlService's state
                    Bundle status = msg.getData();
                    mServiceIsRecording = status.getBoolean("isRecording");

                    Log.d(LOG, "recording state: " + mServiceIsRecording);

                    if (isBluetoothPresent && !ControlService.isStandalone) {
                        mAppState.bluetoothConnected();
                    } else if (!ControlService.isStandalone) {
                        mAppState.bluetoothDisconnected();
                    }

                    break;

                case ControlService.MSG_PREFS_IN_FOREGROUND:

                    isPrefsInForeGround = msg.getData().getBoolean(KEY_PREFS_IN_FOREGROUND);
                    break;

                case ControlService.MSG_START_RECORDING:

                    /*if (!ControlService.isStandalone) {
                        mAppState.bluetoothConnected();
                        isBluetoothPresent = true;
                    }*/
                    mAppState.startRecording();
                    setLogoActive();

                    break;

                case ControlService.MSG_STOP_RECORDING:

                    /*if (!ControlService.isStandalone) {
                        mAppState.bluetoothDisconnected();
                        isBluetoothPresent = false;
                    }*/
                    mAppState.stopRecording();
                    setLogoInactive();

                    break;

                case ControlService.MSG_TIME_CORRECT:

                    mAppState.timeCorrect();

                    break;
                case ControlService.MSG_TIME_INCORRECT:

                    mAppState.timeIncorrect();

                    break;

                case ControlService.MSG_BT_CONNECTED:

                    mAppState.bluetoothConnected();

                    break;

                case ControlService.MSG_BT_DISCONNECTED:

                    mAppState.bluetoothDisconnected();

                    break;

                default:

                    super.handleMessage(msg);

                    break;
            }
        }
    }
}
