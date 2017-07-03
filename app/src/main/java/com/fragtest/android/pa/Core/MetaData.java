package com.fragtest.android.pa.Core;

import android.content.Context;
import android.provider.Settings.Secure;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.fragtest.android.pa.BuildConfig;
import com.fragtest.android.pa.Questionnaire.Question;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ulrikkowalk on 14.03.17.
 */

public class MetaData extends AppCompatActivity {

    private static String LOG_STRING = "MetaData";

    private String DEVICE_Id, START_DATE, START_DATE_UTC, END_DATE,
            END_DATE_UTC, KEY_HEAD, KEY_FOOT, KEY_TAG_CLOSE, KEY_VALUE_OPEN, KEY_VALUE_CLOSE,
            KEY_SURVEY_URI, KEY_RECORD_OPEN, KEY_RECORD_CLOSE, KEY_DATA,
            KEY_QUESTID, mRawInput, FILE_NAME;

    private SimpleDateFormat DATE_FORMAT;

    private int mTimeQuery = 0;
    private int mTimeQueryUTC = 0;

    private ArrayList<Question> mQuestionList;

    private Context mContext;

    private EvaluationList mEvaluationList;

    public MetaData(Context context, String rawInput) {
        mContext = context;
        mRawInput = rawInput;
        mQuestionList = new ArrayList<>();
        DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);

        KEY_RECORD_OPEN = "<record";
        KEY_RECORD_CLOSE = "</record>";
        KEY_TAG_CLOSE = ">";
        KEY_VALUE_OPEN = "<value ";
        KEY_VALUE_CLOSE = "</value>";
    }

    public boolean initialise() {
        // Obtain Device Id
        DEVICE_Id = generateDeviceId();
        // Obtain current Time Stamp at the Beginning of Questionnaire
        START_DATE = generateTimeNow();
        // Obtain current UTC Time Stamp at the Beginning of Questionnaire
        START_DATE_UTC = generateTimeNowUTC();

        String[] mRawInputLines = mRawInput.split("\n");

        KEY_HEAD = mRawInputLines[0] + mRawInputLines[1];

        KEY_SURVEY_URI = mRawInput.split("<survey uri=\"")[1].split("\">")[0];
        KEY_FOOT = mRawInputLines[mRawInputLines.length - 1];
        KEY_QUESTID = generateQuestId();
        FILE_NAME = generateFileName();

        if (BuildConfig.DEBUG) {
            Log.i(LOG_STRING, "Object initialised");
        }
        return true;
    }

    public boolean finalise(EvaluationList evaluationList) {

        mEvaluationList = evaluationList;
        // Obtain current Time Stamp at the End of Questionnaire
        END_DATE = generateTimeNow();
        // Obtain current UTC Time Stamp at the End of Questionnaire
        END_DATE_UTC = generateTimeNowUTC();
        collectData();

        if (BuildConfig.DEBUG) {
            Log.i(LOG_STRING, "Object finalised");
        }
        return true;
    }

    // List of questions according to questionnaire - needed to account for unanswered questions
    public void addQuestion(Question question) {
        mQuestionList.add(question);
    }

    private void collectData() {

        int questionId = -255;

        KEY_DATA = KEY_HEAD;
        KEY_DATA += KEY_RECORD_OPEN;
        KEY_DATA += " uri=\"";
        KEY_DATA += KEY_SURVEY_URI.substring(0, KEY_SURVEY_URI.length() - 4);        // loose ".xml"
        KEY_DATA += "/";
        KEY_DATA += KEY_QUESTID;
        KEY_DATA += ".xml\"";
        KEY_DATA += " survey uri=\"";
        KEY_DATA += KEY_SURVEY_URI;
        KEY_DATA += "\"";
        KEY_DATA += KEY_TAG_CLOSE;

        for (int iQuestion = 0; iQuestion < mQuestionList.size(); iQuestion++) {

            questionId = mQuestionList.get(iQuestion).getQuestionId();
            if (questionId != 99999) {

                KEY_DATA += KEY_VALUE_OPEN;
                KEY_DATA += "question_id=\"";
                KEY_DATA += questionId;
                KEY_DATA += "\"";

                String ANSWER_DATA = "";

                //Log.i(LOG_STRING,mEvaluationList.getAnswerTypeFromQuestionId(questionId));
                switch (mEvaluationList.getAnswerTypeFromQuestionId(questionId)) {
                    case "none":
                        //Log.i(LOG_STRING,"none");
                        ANSWER_DATA += "/>";
                        break;
                    case "text":
                        ANSWER_DATA += ">";
                        ANSWER_DATA += mEvaluationList.getTextFromQuestionId(questionId);
                        ANSWER_DATA += KEY_VALUE_CLOSE;
                        break;
                    case "id":
                        ArrayList<String> listOfIds =
                                mEvaluationList.getCheckedAnswerIdsFromQuestionId(questionId);
                        ANSWER_DATA += "option_ids=\"";
                        ANSWER_DATA += listOfIds.get(0);
                        for (int iId = 0; iId < listOfIds.size(); iId++) {
                            ANSWER_DATA += ";";
                            ANSWER_DATA += listOfIds.get(iId);
                        }
                        ANSWER_DATA += "\" />";
                        break;
                    case "value":
                        //Log.i(LOG_STRING,"value");
                        break;
                    default:
                        Log.e(LOG_STRING, "Unknown element found during evaluation: " +
                                mEvaluationList.getAnswerTypeFromQuestionId(questionId));
                        break;
                }

                KEY_DATA += ANSWER_DATA;
            }
        }
        KEY_DATA += KEY_RECORD_CLOSE;
        KEY_DATA += KEY_FOOT;
    }

    private String generateFileName() {
        return getQuestId() + ".xml";
    }

    private String generateQuestId() {
        return getDeviceId() + "_" + getStartDateUTC();
    }

    private String generateDeviceId() {
        return Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID);
    }

    private String generateTimeNow() {
        Calendar dateTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
        return DATE_FORMAT.format(dateTime.getTime());
    }

    private String generateTimeNowUTC() {
        Calendar dateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return DATE_FORMAT.format(dateTime.getTime());
    }

    private String getQuestId() {
        return KEY_QUESTID;
    }

    private String getDeviceId() {
        return DEVICE_Id;
    }

    private String getStartDate() {
        return START_DATE;
    }

    private String getStartDateUTC() {
        return START_DATE_UTC;
    }

    private String getEndDate() {
        return END_DATE;
    }

    private String getEndDateUTC() {
        return END_DATE_UTC;
    }

    private String getTimeNow() {
        if (mTimeQuery == 0) {
            mTimeQuery++;
            return getStartDate();
        } else {
            return getEndDate();
        }
    }

    private String getTimeNowUTC() {
        if (mTimeQueryUTC == 0) {
            mTimeQueryUTC++;
            return getStartDateUTC();
        } else {
            return getEndDateUTC();
        }
    }

    private String getTextFromId(int id) {
        try {
            return mEvaluationList.getTextFromQuestionId(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getData() {
        return KEY_DATA;
    }

    public String getFileName() {
        return FILE_NAME;
    }
}