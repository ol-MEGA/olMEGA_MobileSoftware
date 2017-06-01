package com.fragtest.android.pa;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ul1021 on 29.05.2017.
 */

public class TimedEvent {

    private String LOG_STRING = "TimedEvent";
    private String mRawInput;
    private Context mContext;
    private ArrayList<Integer> meanMinute, meanHour, meanDay;
    private ArrayList<Integer> randMinute, randHour, randDay;
    private int[] rangeScheduleMinutes, rangeScheduleHours, rangeScheduleDays,
            rangeRandMinutes, rangeRandHours, rangeRandDays;
    private boolean isDebug = true;

    public TimedEvent(Context context, String string) {
        mContext = context;
        mRawInput = string;

        setRanges();
    }

    public boolean extractVariables() {

        if (mRawInput.contains("<timed>")) {

            horizontalRule();

            if (mRawInput.split("<schedule>|</schedule>").length > 0) {
                String meanString = mRawInput.split("<schedule>|</schedule>")[1];

                // Extract values for scheduling
                meanMinute  = extractTime(meanString, "minute" ,rangeScheduleMinutes);
                meanHour    = extractTime(meanString, "hour", rangeScheduleHours);
                meanDay     = extractTime(meanString, "day", rangeScheduleDays);
            }

            horizontalRule();

            if (mRawInput.split("<random>|</random>").length > 0) {
                String randString = mRawInput.split("<random>|</random>")[1];

                // Extract values for scheduling randomness
                randMinute  = extractTime(randString, "minute", rangeRandMinutes);
                randHour    = extractTime(randString, "hour", rangeRandHours);
                randDay     = extractTime(randString, "day", rangeRandDays);
            }

            horizontalRule();
        }
        return true;
    }

    private ArrayList<Integer> extractTime(String string, String type, int[] range) {

        ArrayList<Integer> time = new ArrayList<>();
        String temp_meanTime;




/**
        NEXT: EXTRACT SCHEDULE vs EXTRACTRAND -> different specification smin/max - plus
                single value for rand!
*/

        switch (type) {
            case "minute":
                temp_meanTime = string.split(" ")[0];
                break;
            case "hour":
                temp_meanTime = string.split(" ")[1];
                break;
            case "day":
                temp_meanTime = string.split(" ")[2];
                break;
            default:
                Log.e(LOG_STRING, "Unknown entry: " + type);
                return null;
        }

        if (temp_meanTime.contains(",")) {
            // Several comma-separated explicit or consecutive entries
            String[] temp_meanTime_split = temp_meanTime.split(",");
            for (int iSubString = 0; iSubString < temp_meanTime_split.length; iSubString++) {

                if (temp_meanTime_split[iSubString].contains("-")) {
                    // Multiple consecutive entries e.g. 1-3
                    String[] temp_StartToEnd = temp_meanTime_split[iSubString].split("-");
                    int nStart = Integer.parseInt(temp_StartToEnd[0]);
                    int nEnd = Integer.parseInt(temp_StartToEnd[1]);

                    for (int iItem = nStart; iItem <= nEnd; iItem++) {
                        time.add(iItem);
                    }
                } else {
                    time.add(Integer.parseInt(temp_meanTime_split[iSubString]));
                }
            }
        }
        else if (temp_meanTime.equals("*")) {
            // Every possible time
            for (int iEntry = 0; iEntry < range[1]; iEntry++) {
                time.add(iEntry);
            }
        }
        else if(temp_meanTime.startsWith("*/")) {
            // A series of points in time based on explicitly given interval
            int everyNthSecond = Integer.parseInt(temp_meanTime.split("\\*/")[1]);
            for (int iMeanSecond = 0; iMeanSecond < range[1]/everyNthSecond; iMeanSecond++) {
                time.add(iMeanSecond*everyNthSecond);
            }
        }
        else {
            // Single explicit entry
            time.add(Integer.parseInt(temp_meanTime));
        }
        time = cleanUpTimes(time, range);
        showDebugInfo(time, type);
        return time;
    }

    private ArrayList<Integer> cleanUpTimes(ArrayList<Integer> times, int[] range) {
        // Omits all out of range values, duplicates and sorting

        // Removal of duplicates
        Set<Integer> temp_set = new HashSet<Integer>(times);
        times.clear();
        times.addAll(temp_set);

        // Removal of outliers
        for (int iEntry = times.size()-1; iEntry >= 0; iEntry--) {
            if ((times.get(iEntry) < range[0]) || (times.get(iEntry) > range[1])) {
                times.remove(iEntry);
            }
        }
        // Sorting of elements
        Collections.sort(times);

        return times;
    }

    private boolean setRanges() {

        rangeScheduleMinutes    = new int[] {0,59};
        rangeScheduleHours      = new int[] {0,23};
        rangeScheduleDays       = new int[] {0,30};
        rangeRandMinutes        = new int[] {0,29};
        rangeRandHours          = new int[] {0,11};
        rangeRandDays           = new int[] {0,15};

        return true;
    }

    private void horizontalRule() {
        if (isDebug) {
            Log.d(LOG_STRING, "==================================================");
        }
    }

    private void showDebugInfo(ArrayList<Integer> time, String type) {
        if (isDebug) {
            if (time.size() > 1) {
                String resultString = "";
                for (int iEntry = 0; iEntry < time.size(); iEntry++) {
                    resultString += time.get(iEntry);
                    resultString += " ";
                }
                Log.d(LOG_STRING, "Multiple entries at " + type + ": " + resultString);
            } else {
                Log.d(LOG_STRING, "Single entry at " + type + ": " + time.get(0));
            }
        }
    }

    public void setDebug(boolean bDebug) {
        isDebug = bDebug;
    }
}
