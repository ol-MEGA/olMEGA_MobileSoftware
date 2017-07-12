package com.fragtest.android.pa;

import android.content.Context;
import android.util.Log;
import com.fragtest.android.pa.Core.FileIO;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Central class for XML sheet info extraction tasks
 */

public class XMLReader {

    private static String LOG_STRING = "XMLReader";
    private Context mContext;
    private FileIO mFileIO;
    private int mTimerMean, mTimerDeviation, mTimerInterval;

    public XMLReader(Context context) {

        mContext = context;
        mFileIO = new FileIO();

        //mRawInput = mFileIO.readRawTextFile();
        // offline version
        String mRawInput = mFileIO.readRawTextFile(mContext, R.raw.question_short_eng);
        String[] timerTemp = mRawInput.split("<timer|</timer>");

        if(timerTemp[1].split("mean").length > 1) {
            try {
                mTimerMean = Integer.parseInt(timerTemp[1].split("\"")[1]);
                Log.e(LOG_STRING, "Timer mean set to "+mTimerMean+" seconds.");
            } catch (Exception e) {
                mTimerMean = 30*60;
                Log.e(LOG_STRING, "Invalid entry. Timer mean set to "+mTimerMean+" seconds.");
            }
        }

        if(timerTemp[1].split("deviation").length > 1) {
            try {
                mTimerDeviation = Integer.parseInt(timerTemp[1].split("\"")[3]);
                Log.e(LOG_STRING, "Timer deviation set to "+mTimerDeviation+" seconds.");
            } catch (Exception e) {
                mTimerDeviation = 5*60;
                Log.e(LOG_STRING, "Invalid entry. Timer mean set to 300 seconds.");
            }
        }
    }

    public int getNewTimerInterval(){
        mTimerInterval = ThreadLocalRandom.current().nextInt(
                mTimerMean - mTimerDeviation,
                mTimerMean + mTimerDeviation + 1);

        return mTimerInterval;
    }
}
