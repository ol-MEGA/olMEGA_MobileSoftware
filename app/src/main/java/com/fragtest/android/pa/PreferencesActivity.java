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

import com.fragtest.android.pa.Core.FileIO;

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

        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);

            // Load preference from XMl resource
            addPreferencesFromResource(preferences);
            includeQuestList();
            SwitchPreference deviceOwnerPref = (SwitchPreference) findPreference("unsetDeviceAdmin");
            deviceOwnerPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if (!((SwitchPreference) preference).isChecked()) {
                        confirmUnsetDeviceOwner();
                    }
                    return true;
                }
            });

            final ListPreference sampleratePref = (ListPreference) findPreference("samplerate");

            ListPreference inputProfilePref = (ListPreference) findPreference("inputProfile");
            inputProfilePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if (o.equals("RFCOMM")) {
                        Log.e(LOG, "Chosen input profile: " + o + ". Setting Samplerate to 16000.");
                        sampleratePref.setValue("16000");
                    } else {
                        Log.e(LOG, "Chosen input profile: " + preference.getSummary() + ". Setting Samplerate to 48000.");
                        sampleratePref.setValue("48000");
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