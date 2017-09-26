package com.fragtest.android.pa;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

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
            // For some reason this needs to be set manually - only shown when is not checked anyway
            SwitchPreference switchPreference = (SwitchPreference) findPreference("isLocked");
            switchPreference.setChecked(false);

            includeQuestList();


        }

        private void includeQuestList() {
            // Scan file system for available questionnaires
            FileIO fileIO = new FileIO();
            String[] fileList = fileIO.scanQuestOptions();

            ListPreference listPreferenceQuest = (ListPreference) findPreference("whichQuest");
            if ((fileList != null) && (fileList.length>0)) {
                // Fill in menu contents
                listPreferenceQuest.setEntries(fileList);
                listPreferenceQuest.setEntryValues(fileList);
                listPreferenceQuest.setDefaultValue(fileList[0]);
                //if (listPreferenceQuest.getSummary() == "") {
                //    listPreferenceQuest.setSummary(fileList[0]);
                //}
            } else {
                listPreferenceQuest.setSummary(R.string.noQuestionnaires);
                listPreferenceQuest.setSelectable(false);
            }
        }
    }
}