package com.fragtest.android.pa;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;

import com.fragtest.android.pa.Core.FileIO;

import static com.fragtest.android.pa.R.xml.preferences;

/**
 * Preferences
 */

public class PreferencesActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG = "PreferencesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as main content
        /*getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new Preferences())
                .commit();*/
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(this, R.xml.preferences,
                false);
        initSummary(getPreferenceScreen());
        includeQuestList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        updatePrefSummary(findPreference(key));
    }

    private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }
    }

    private void updatePrefSummary(Preference p) {
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            p.setSummary(listPref.getEntry());
        }
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            if (p.getTitle().toString().toLowerCase().contains("password")) {
                p.setSummary("******");
            } else {
                p.setSummary(editTextPref.getText());
            }
        }
        /*if (p instanceof MultiSelectListPreference) {
            MultiSelectListPreference multiSelectListPreference = (MultiSelectListPreference) p;


            CharSequence cs = multiSelectListPreference.getSummary();
            String summary = cs.toString();

            if (summary.contains("%s")) {
                String text = "";
                StringBuilder builder = new StringBuilder();
                CharSequence[] entries = multiSelectListPreference.getEntries();
                if(entries.length > 0) {
                    CharSequence[] entryValues = multiSelectListPreference.getEntryValues();
                    Set<String> values = multiSelectListPreference.getValues();
                    int pos = 0;

                    for (String value : values) {
                        pos++;
                        int index = -1;
                        for (int i = 0; i < entryValues.length; i++) {
                            if (entryValues[i].equals(value)) {
                                index = i;
                                break;
                            }
                        }
                        builder.append(entries[index]);
                        if (pos < values.size())
                            builder.append(", ");
                    }
                    text = builder.toString();
                }
                summary = String.format(summary, text);
            }

            p.setSummary(summary);
        }*/
    }

    private void includeQuestList() {
        // Scan file system for available questionnaires
        FileIO fileIO = new FileIO();
        String[] fileList = fileIO.scanQuestOptions();

        ListPreference listPreferenceQuest = (ListPreference) findPreference("whichQuest");
        // TODO: Isn't the second constraint enough?
        if ((fileList != null) && (fileList.length > 0)) {
            // Fill in menu contents
            listPreferenceQuest.setEntries(fileList);
            listPreferenceQuest.setEntryValues(fileList);
            listPreferenceQuest.setDefaultValue(fileList[0]);
            listPreferenceQuest.setValue(fileList[0]);
            listPreferenceQuest.setSummary(fileList[0]);
        } else {
            listPreferenceQuest.setSummary(R.string.noQuestionnaires);
            listPreferenceQuest.setSelectable(false);
        }
    }

    public static class Preferences extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);

            // Load preference from XMl resource
            addPreferencesFromResource(preferences);
            includeQuestList();
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
    }
}