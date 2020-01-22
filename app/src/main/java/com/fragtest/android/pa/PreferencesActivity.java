package com.fragtest.android.pa;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.fragtest.android.pa.Core.DeviceName;
import com.fragtest.android.pa.Core.FileIO;
import com.fragtest.android.pa.Core.LogIHAB;
import com.fragtest.android.pa.DataTypes.StringAndString;

import java.util.ArrayList;
import java.util.Set;

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

        // Member fields
        private BluetoothAdapter mBtAdapter;
        private ArrayAdapter<String> mPairedDevicesArrayAdapter;
        private Set<BluetoothDevice> pairedDevices;
        private String[] listDevices;


        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);

            doDiscovery();

            // Register for broadcasts when a device is discovered
            //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            //this.registerReceiver(mReceiver, filter);

            // Register for broadcasts when discovery has finished
            //filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            //this.registerReceiver(mReceiver, filter);

            // Load preference from XMl resource
            addPreferencesFromResource(preferences);
            // Switch list dummy for actual present questionnaire files
            includeQuestList();

            includeDevicesList();

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
                    if (o.equals("RFCOMM") || o.equals("PHANTOM")) {
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

            //doDiscovery();
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

        private void includeDevicesList() {
            ListPreference listPreferenceDevices = (ListPreference) findPreference("listDevices");
            if (listDevices.length > 0) {
                listPreferenceDevices.setEntries(listDevices);
                listPreferenceDevices.setEntryValues(listDevices);
                listPreferenceDevices.setDefaultValue(listDevices[0]);
            } else {
                listPreferenceDevices.setSummary("Unable to find devices.");
                listPreferenceDevices.setSelectable(false);
            }
        }

        // Start device discover with the BluetoothAdapter
        private void doDiscovery() {

            // Get the local Bluetooth adapter
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
            // Get a set of currently paired devices
            pairedDevices = mBtAdapter.getBondedDevices();

            // Remove all element from the list
            //mPairedDevicesArrayAdapter.clear();
            ArrayList<StringAndString> tmpList = new ArrayList<>();

            // If there are paired devices, add each one to the ArrayAdapter
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    //mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    tmpList.add(new StringAndString(device.getName(), device.getAddress()));
                    Log.e(LOG, "DEVICE FOUND: " + device.getName() + ", Address: " + device.getAddress());
                }
            } else {
                Log.e(LOG, "NO DEVICES FOUND.");//lse {
            }

            listDevices = new String[pairedDevices.size()];
            for (int iItem = 0; iItem < listDevices.length; iItem++) {
                listDevices[iItem] = tmpList.get(iItem).getName();
            }

            // If we're already discovering, stop it
            //if (mBtAdapter.isDiscovering()) {
            //    mBtAdapter.cancelDiscovery();
            //}

            // Request discover from BluetoothAdapter
            //mBtAdapter.startDiscovery();
        }
    }
}