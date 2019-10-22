package com.fragtest.android.pa;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.fragtest.android.pa.Core.DeviceName;
import com.fragtest.android.pa.Core.FileIO;
import com.fragtest.android.pa.Core.LogIHAB;

import static com.fragtest.android.pa.R.xml.preferences;

/**
 * Preferences
 */

public class PreferencesActivity extends PreferenceActivity {

    private static final String LOG = "PreferencesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as main content
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new Preferences())
                .commit();
    }

    public static class Preferences extends PreferenceFragment {

        private final String DEVICE_WITH_A2DP = "LGE Car Hammerhead";

        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);

            // Load preference from XMl resource
            addPreferencesFromResource(preferences);
            includeQuestList();
            /*SwitchPreference deviceOwnerPref = (SwitchPreference) findPreference("unsetDeviceAdmin");
            deviceOwnerPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if (!((SwitchPreference) preference).isChecked()) {
                        confirmUnsetDeviceOwner();
                    }
                    return true;
                }
            });*/

            final ListPreference inputProfilePref = (ListPreference) findPreference("inputProfile");
            final ListPreference sampleratePref = (ListPreference) findPreference("samplerate");
            final SwitchPreference downsamplePref = (SwitchPreference) findPreference("downsample");

            if (DeviceName.getDeviceName().equals(DEVICE_WITH_A2DP)) {
                inputProfilePref.setEntries(R.array.inputProfileWithA2DP);
                inputProfilePref.setEntryValues(R.array.inputProfileWithA2DP);
                String message = "Device: " + DEVICE_WITH_A2DP + " qualifies for use with A2DP protocol.";
                Log.e(LOG, message);
                LogIHAB.log(message);
            } else {
                inputProfilePref.setEntries(R.array.inputProfile);
                inputProfilePref.setEntryValues(R.array.inputProfile);
            }

            inputProfilePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if (o.equals("RFCOMM")) {
                        String messsage = "Chosen input profile: " + o + ". Setting Samplerate to 16000 and disabling downsampling by factor 2.";
                        Log.e(LOG, messsage);
                        LogIHAB.log(messsage);
                        sampleratePref.setValue("16000");
                        downsamplePref.setChecked(false);
                    } else {
                        String message = "Chosen input profile: " + o + ". Setting Samplerate to 48000 and enabling downsampling by factor 2.";
                        Log.e(LOG, message);
                        LogIHAB.log(message);
                        sampleratePref.setValue("48000");
                        downsamplePref.setChecked(true);
                    }
                    return true;
                }
            });

        }

        private void includeQuestList() {
            // Scan file system for available questionnaires
            FileIO fileIO = new FileIO();
            String[] fileList = fileIO.scanQuestOptions();

            ListPreference listPreferenceQuest = (ListPreference) findPreference("whichQuest");
            // TODO: Isn't the second constraint enough?
            if ((fileList != null) && (fileList.length>0)) {
                // Fill in menu contents
                listPreferenceQuest.setEntries(fileList);
                listPreferenceQuest.setEntryValues(fileList);
                listPreferenceQuest.setDefaultValue(fileList[0]);
            } else {
                listPreferenceQuest.setSummary(R.string.noQuestionnaires);
                listPreferenceQuest.setSelectable(false);
            }
        }

        private void confirmUnsetDeviceOwner() {

            new AlertDialog.Builder(getActivity(), R.style.SwipeDialogTheme)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.deviceOwnerMessage)
                    .setPositiveButton(R.string.deviceOwnerYes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setNegativeButton(R.string.deviceOwnerNo, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SwitchPreference deviceOwnerPref = (SwitchPreference) findPreference("unsetDeviceAdmin");
                            deviceOwnerPref.setChecked(false);
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }
}