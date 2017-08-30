package com.fragtest.android.pa.Core;

import android.content.Context;
import android.util.Log;

import com.fragtest.android.pa.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Central class for XML sheet info extraction tasks
 */

public class XMLReader {

    private static String LOG_STRING = "XMLReader";
    private Context mContext;
    private FileIO mFileIO;
    private String mHead;
    private int mTimerMean, mTimerDeviation, mTimerInterval;
    // List containing all questions (including all attached information)
    private ArrayList<String> mQuestionList;

    public XMLReader(Context context) {

        mContext = context;
        mFileIO = new FileIO();
        mQuestionList = new ArrayList<>();

        //rawInput = mFileIO.readRawTextFile();
        // offline version
        String rawInput = mFileIO.readRawTextFile(mContext, R.raw.questionnairecheckboxgroup); //question_short_eng
        String[] timerTemp = rawInput.split("<timer|</timer>");

        if (timerTemp[1].split("mean").length > 1) {
            try {
                mTimerMean = Integer.parseInt(timerTemp[1].split("\"")[1]);
                Log.e(LOG_STRING, "Timer mean set to " + mTimerMean + " seconds.");
            } catch (Exception e) {
                mTimerMean = 30 * 60;
                Log.e(LOG_STRING, "Invalid entry. Timer mean set to " + mTimerMean + " seconds.");
            }
        }

        if (timerTemp[1].split("deviation").length > 1) {
            try {
                mTimerDeviation = Integer.parseInt(timerTemp[1].split("\"")[3]);
                Log.e(LOG_STRING, "Timer deviation set to " + mTimerDeviation + " seconds.");
            } catch (Exception e) {
                mTimerDeviation = 5 * 60;
                Log.e(LOG_STRING, "Invalid entry. Timer mean set to 300 seconds.");
            }
        }

        // Split basis data into question segments
        String[] questionnaire = rawInput.split("<question|</question>|<finish>|</finish>");
        mHead = extractHead(rawInput);
        mQuestionList = stringArrayToListString(questionnaire);
        mQuestionList = thinOutList(mQuestionList);
    }

    public int getNewTimerInterval() {
        mTimerInterval = ThreadLocalRandom.current().nextInt(
                mTimerMean - mTimerDeviation,
                mTimerMean + mTimerDeviation + 1);

        return mTimerInterval;
    }

    public String getHead() {
        return mHead;
    }

    public ArrayList<String> getQuestionList() {
        return mQuestionList;
    }

    private ArrayList<String> thinOutList(ArrayList<String> mQuestionList) {
        // Removes irrelevant data from question sheet

        for (int iItem = mQuestionList.size() - 1; iItem >= 0; iItem = iItem - 2) {
            mQuestionList.remove(iItem);
        }
        return mQuestionList;
    }

    private ArrayList<String> stringArrayToListString(String[] stringArray) {
        // Turns an array of Strings into a List of Strings
        ArrayList<String> listString = new ArrayList<>();
        Collections.addAll(listString, stringArray);
        return listString;
    }

    private String extractHead(String rawInput) {
        String head = "";
        String[] tempHead = rawInput.split("<|>");

        head += "<";
        head += tempHead[1];
        head +="><";
        head += tempHead[3];
        head += ">";

        return head;
    }
}
