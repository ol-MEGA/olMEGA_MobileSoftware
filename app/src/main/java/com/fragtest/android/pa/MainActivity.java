package com.fragtest.android.pa;

import android.Manifest;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fragtest.android.pa.Core.FileIO;
import com.fragtest.android.pa.Questionnaire.QuestionnairePagerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static android.R.color.darker_gray;
import static android.R.color.holo_green_dark;
import static com.fragtest.android.pa.ControlService.MSG_APPLICATION_SHUTDOWN;
import static com.fragtest.android.pa.ControlService.MSG_CHANGE_PREFERENCE;
import static com.fragtest.android.pa.ControlService.MSG_CHARGING_OFF;
import static com.fragtest.android.pa.ControlService.MSG_CHARGING_ON;
import static com.fragtest.android.pa.ControlService.MSG_CHARGING_ON_PRE;
import static com.fragtest.android.pa.ControlService.MSG_NO_QUESTIONNAIRE_FOUND;


public class MainActivity extends AppCompatActivity {

    private boolean USE_KIOSK_MODE = true;
    private boolean USE_MASTER_MODE = false;

    static final String LOG = "MainActivity";
    private static final String KEY_QUEST = "whichQuest";
    private static final String KEY_PREFS_IN_FOREGROUND = "prefsInForeGround";
    private int nPermissions = 8;
    private int iPermission;
    private final static int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 0;
    private final static int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 1;
    private final static int MY_PERMISSIONS_RECEIVE_BOOT_COMPLETED = 2;
    private final static int MY_PERMISSIONS_RECORD_AUDIO = 3;
    private final static int MY_PERMISSIONS_VIBRATE = 4;
    private final static int MY_PERMISSIONS_WAKE_LOCK = 5;
    private final static int MY_PERMISSIONS_DISABLE_KEYGUARD = 6;
    private final static int MY_PERMISSIONS_CAMERA = 7;
    final Messenger mMessageHandler = new Messenger(new MessageHandler());
    public ViewPager mViewPager = null;
    public TextView mLogo;
    public View mRecord, mArrowBack, mArrowForward, mRevert, mProgress, mRegress, mConfig,
            mBatteryReg, mBatteryProg, mCharging;
    private QuestionnairePagerAdapter mAdapter;
    private boolean mServiceIsBound;
    private boolean isPrefsInForeGround = false;
    private boolean isActivityRunning = false;
    private boolean mServiceIsRecording;
    private Messenger mServiceMessenger;
    private boolean isQuestionnairePresent = true;
    private String[] requestString;

    private boolean isCharging = false;

    // RELEVANT FOR KIOSK MODE
    private FileIO mFileIO;
    private ComponentName mAdminComponentName;
    private DevicePolicyManager mDevicePolicyManager;
    private final List blockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP));


    // RELEVANT FOR PERMISSIONS (Android 6+, just in case)
    private int requestIterator = 0;
    private boolean permissionGranted = false;

    // Battery Status Receiver
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean charging = status == BatteryManager.BATTERY_PLUGGED_USB || status == BatteryManager.BATTERY_PLUGGED_AC;

            if (charging && !isCharging) {
                mCharging.setVisibility(View.VISIBLE);
                messageService(MSG_CHARGING_ON);
            } else if (!charging && isCharging){
                mCharging.setVisibility(View.INVISIBLE);
                messageService(MSG_CHARGING_OFF);
            }

            isCharging = charging;
            // Announce charging and hide error messages
            mAdapter.setCharging(isCharging);
            ControlService.setCharging(isCharging);
        }
    };

    // Preferences
    private SharedPreferences sharedPreferences;
    private boolean isTimer, showConfigButton = false, showRecordingButton = true;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            Log.e(LOG, "ON SERVICE CONNECTED");

            mServiceMessenger = new Messenger(service);

            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(null, ifilter);
            // Are we charging / charged?
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;


            if (isCharging) {
                messageService(MSG_CHARGING_ON_PRE);
            }

            messageService(ControlService.MSG_REGISTER_CLIENT);
            messageService(ControlService.MSG_GET_STATUS);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceMessenger = null;
        }
    };
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
                    mAdapter.setQuestionnaireProgressBar(position);
                    mAdapter.setArrows(position);
                }
            };

    // Is ControlService already running?
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
        mAdapter = new QuestionnairePagerAdapter(this, mViewPager, USE_KIOSK_MODE);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.addOnPageChangeListener(myOnPageChangeListener);
    }

    private void shipPreferencesToControlService() {
        // Load information from shared preferences and bundle them
        Bundle dataPreferences = new Bundle();
        // String
        dataPreferences.putString("whichQuest", sharedPreferences.getString("whichQuest", ""));
        dataPreferences.putString("samplerate", sharedPreferences.getString("samplerate", "" + InitValues.samplerate));
        dataPreferences.putString("chunklengthInS", sharedPreferences.getString("chunklengthInS", "" + InitValues.chunklengthInS));
        dataPreferences.putString("filterHpFrequency", sharedPreferences.getString("filterHpFrequency", "" + InitValues.filterHpFrequency));
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


    /** Lifecycle methods */


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        showConfigButton = sharedPreferences.getBoolean("showConfigButton", showConfigButton);
        showRecordingButton = sharedPreferences.getBoolean("showRecordingButton", showRecordingButton);

        if (BuildConfig.DEBUG) {
            Log.i(LOG, "OnCreate");
        }

        if (!isActivityRunning) {
            super.onCreate(savedInstanceState);

            Log.d(LOG, "Requesting Permissions.");
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
            showConfigButton = mFileIO.checkConfigFile();
            if (showConfigButton) {
                USE_KIOSK_MODE = false;
                USE_MASTER_MODE = true;
            }

            setConfigVisibility();

            mConfig.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
                        isPrefsInForeGround = true;
                        mAdapter.setPrefsInForeGround(isPrefsInForeGround);
                    }
                });


            handleNewPagerAdapter();
            doBindService();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

            mAdapter.createMenu();
            mAdapter.onCreate();
            isActivityRunning = true;
        }


        ComponentName deviceAdmin = new ComponentName(this, AdminReceiver.class);
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (!mDevicePolicyManager.isAdminActive(deviceAdmin)) {
            Toast.makeText(this, "Not device admin.", Toast.LENGTH_SHORT).show();
        }

        if (mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
            mDevicePolicyManager.setLockTaskPackages(deviceAdmin, new String[]{getPackageName()});
        } else {
            Toast.makeText(this, "Not device owner.", Toast.LENGTH_SHORT).show();
        }

        mAdminComponentName = deviceAdmin;
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(
                Context.DEVICE_POLICY_SERVICE);

        Log.e(LOG, "KIOSK is: "+USE_KIOSK_MODE);
        setDefaultCosuPolicies(USE_KIOSK_MODE);
        enableKioskMode(USE_KIOSK_MODE);

    }

    @Override
    protected void onDestroy() {
        if (BuildConfig.DEBUG) {
            Log.i(LOG, "onDestroy");
        }
        super.onDestroy();
        messageService(MSG_APPLICATION_SHUTDOWN);
        doUnbindService();
    }

    @Override
    protected void onStart() {
        if (BuildConfig.DEBUG) {
            Log.i(LOG, "onStart");
        }
        super.onStart();
        mAdapter.onStart();
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
        mAdapter.onStop();
    }

    @Override
    protected void onPause() {
        if (BuildConfig.DEBUG) {
            Log.i(LOG, "onPause");
        }
        mAdapter.onPause();
        super.onPause();

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        if (USE_KIOSK_MODE) {
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

            shipPreferencesToControlService();
        }
        mAdapter.onResume();
        super.onResume();

        hideSystemUI(USE_KIOSK_MODE);
    }

    private void setConfigVisibility() {
        if (showConfigButton) {
            mConfig.setVisibility(View.VISIBLE);
        } else {
            mConfig.setVisibility(View.GONE);
        }
    }

    private void setRecordingVisibility() {
        if (showRecordingButton) {
            mRecord.setVisibility(View.VISIBLE);
        } else {
            mRecord.setVisibility(View.INVISIBLE);
        }
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
        mViewPager.setCurrentItem(mViewPager.getCurrentItem()+1, true);
    }

    private void setBTLogoConnected() {
        //showRecordingButton = true;
        setRecordingVisibility();
        mRecord.setBackgroundTintList(
                ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),
                        R.color.BatteryGreen, null)));
    }

    private void setBTLogoDisconnected() {
        //showRecordingButton = false;
        setRecordingVisibility();
        mRecord.setBackgroundTintList(
                ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),
                        darker_gray, null)));
    }


/** KIOSK MODE RELATED STUFF */


    // This disables the Volume Buttons
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (blockedKeys.contains(event.getKeyCode()) && USE_KIOSK_MODE) {
            Toast.makeText(getApplicationContext(), "VOL", Toast.LENGTH_SHORT).show();
            return true;
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_POWER && USE_KIOSK_MODE) {
            Toast.makeText(getApplicationContext(), "POWER TO THE PEOPLE!", Toast.LENGTH_SHORT).show();
            return super.dispatchKeyEvent(event);
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    // When back button is pressed, questionnaire navigates one page backwards, menu does nothing
    @Override
    public void onBackPressed() {
        if (!mAdapter.isMenu() && USE_KIOSK_MODE) {
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
    setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, active);
    setUserRestriction(UserManager.DISALLOW_CREATE_WINDOWS, active);
    Log.i(LOG, "KIOSK MODE: "+active);
    // disable keyguard and status bar
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

    private void enableKioskMode(boolean enabled) {
        try {
            if (enabled && USE_KIOSK_MODE) {
                if (mDevicePolicyManager.isLockTaskPermitted(this.getPackageName())) {
                    startLockTask();
                } else {
                    Toast.makeText(this, "Kiosk not permitted.", Toast.LENGTH_SHORT).show();
                }
            }/*else {
                stopLockTask();
            }*/
        } catch (Exception e) {
            // TODO: Log and handle appropriately
        }
    }

    public void hideSystemUI(boolean isImmersive) {
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        if (isImmersive) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_VISIBLE
            );
        }
    }


    /** Message Handling */


    // Send message to connected client
    public void messageService(int what) {

        if (BuildConfig.DEBUG) {
            Log.e(LOG, "Sending Message: " + what);
        }

        if (mServiceMessenger != null) {
            try {
                Message msg = Message.obtain(null, what);
                msg.replyTo = mMessageHandler;
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
            }
        }
    }

    // Send message to connected client
    public void messageService(int what, Bundle data) {

        if (BuildConfig.DEBUG) {
            Log.e(LOG, "Sending Message: " + what);
        }

        if (mServiceMessenger != null) {
            try {
                Message msg = Message.obtain(null, what);
                msg.setData(data);
                msg.replyTo = mMessageHandler;
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
            }
        }
    }

    class MessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            if (BuildConfig.DEBUG) {
                Log.d(LOG, "Message received: " + msg.what);
            }

            switch (msg.what) {

                case ControlService.MSG_SET_VISIBILITY:

                    Bundle dataVisibility = msg.getData();
                    isQuestionnairePresent = dataVisibility.getBoolean("isQuestionnairePresent", isQuestionnairePresent);

                    if (isQuestionnairePresent) {
                        mAdapter.questionnairePresent();
                    }

                    break;

                case MSG_NO_QUESTIONNAIRE_FOUND:
                    isQuestionnairePresent = false;
                    mAdapter.noQuestionnaires();
                    break;

                case MSG_CHANGE_PREFERENCE:
                    Bundle data = msg.getData();
                    if (data.getString("type").equals("boolean")) {
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).
                                edit().putBoolean(data.getString("key"), data.getBoolean("value")).
                                apply();

                        Log.i(LOG, "Boolean "+data.getString("key")+" changed to "+PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(data.getString("key"), false));
                    }
                    shipPreferencesToControlService();
                    break;

                case ControlService.MSG_BT_CONNECTED:
                    //setBTLogoConnected();
                    mAdapter.setBluetoothPresent();
                    break;

                case ControlService.MSG_BT_DISCONNECTED:
                    //setBTLogoDisconnected();
                    mAdapter.noBluetooth();
                    break;

                case ControlService.MSG_START_COUNTDOWN:

                    isQuestionnairePresent = true;
                    isTimer = true;

                    if (isQuestionnairePresent) {
                        int finalCountDown = msg.getData().getInt("finalCountDown");
                        int countDownInterval = msg.getData().getInt("countDownInterval");
                        if (isTimer) {
                            mAdapter.setFinalCountDown(finalCountDown, countDownInterval);
                            mAdapter.startCountDown();
                        } else {
                            mAdapter.setQuestionnaireProgressBar(100);
                        }
                        mAdapter.questionnairePresent();
                        //mAdapter.displayManualStart();
                    } else {
                        mAdapter.noQuestionnaires();
                    }
                    break;

                case ControlService.MSG_START_QUESTIONNAIRE:
                    Bundle dataQuest = msg.getData();
                    ArrayList<String> questionList = dataQuest.getStringArrayList("questionList");
                    String head = dataQuest.getString("head");
                    String foot = dataQuest.getString("foot");
                    String surveyUri = dataQuest.getString("surveyUri");
                    String motivation = dataQuest.getString("motivation");
                    mAdapter.createQuestionnaire(questionList, head, foot, surveyUri, motivation);
                    break;

                case ControlService.MSG_PROPOSE_QUESTIONNAIRE:
                    mAdapter.proposeQuestionnaire();
                    break;

                case ControlService.MSG_GET_STATUS:
                    // Set UI to match ControlService's state
                    Bundle status = msg.getData();
                    mServiceIsRecording = status.getBoolean("isRecording");

                    Log.d(LOG, "recording state: " + mServiceIsRecording);

                    if (mServiceIsRecording) {
                        mRecord.setBackgroundTintList(
                                ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),
                                        holo_green_dark, null)));
                    } else {
                        mRecord.setBackgroundTintList(
                                ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),
                                        darker_gray, null)));
                    }
                    break;

                case ControlService.MSG_NO_TIMER:
                    isTimer = false;
                    mAdapter.noTimer();
                    break;

                case ControlService.MSG_RESET_MENU:
                    mAdapter.resetMenu();
                    break;

                case ControlService.MSG_PREFS_IN_FOREGROUND:
                    isPrefsInForeGround = msg.getData().getBoolean(KEY_PREFS_IN_FOREGROUND);
                    break;

                case ControlService.MSG_START_RECORDING:
                    setBTLogoConnected();
                    break;

                case ControlService.MSG_STOP_RECORDING:
                    setBTLogoDisconnected();
                    break;

                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }


    /** PERMISSION STUFF (ANDROID 6+)  */


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
            case MY_PERMISSIONS_READ_EXTERNAL_STORAGE : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = true;
                    doBindService();
                } else {
                    Toast.makeText(this, requestString[requestIterator%(requestString.length)], Toast.LENGTH_SHORT).show();
                    requestPermissions(iPermission);
                    requestIterator++;
                }
            }
        }
    }

}
