package com.fragtest.android.pa;

import android.content.Context;
import android.provider.Settings.Secure;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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
            KEY_TAG_CLOSE_SOFT, KEY_SURVEY_URI, KEY_RECORD_OPEN, KEY_RECORD_CLOSE, KEY_DATA,
            KEY_QUESTId, mRawInput, FILE_NAME;

    private SimpleDateFormat DATE_FORMAT;

    private int mTimeQuery = 0;
    private int mTimeQueryUTC = 0;

    private ArrayList<Question> mQuestionList;

    private Context mContext;

    private EvaluationList mEvaluationList;
    private AnswerTexts mAnswerTexts;

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
        KEY_TAG_CLOSE_SOFT = "/>";
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
        KEY_SURVEY_URI = mRawInputLines[2].split("\"")[1];
        KEY_FOOT = mRawInputLines[mRawInputLines.length - 1];
        KEY_QUESTId = generateQuestId();
        FILE_NAME = generateFileName();

        Log.i(LOG_STRING, "Object initialised");
        return true;
    }

    public boolean finalise(EvaluationList evaluationList) {

        mEvaluationList = evaluationList;
        // Obtain current Time Stamp at the End of Questionnaire
        END_DATE = generateTimeNow();
        // Obtain current UTC Time Stamp at the End of Questionnaire
        END_DATE_UTC = generateTimeNowUTC();
        collectData();

        Log.i(LOG_STRING, "Object finalised");
        return true;
    }


    // List of questions according to questionnaire - neede to account for unanswered questions
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
        KEY_DATA += KEY_QUESTId;
        KEY_DATA += ".xml\"";
        KEY_DATA += " survey uri=\"";
        KEY_DATA += KEY_SURVEY_URI;
        KEY_DATA += "\"";
        KEY_DATA += KEY_TAG_CLOSE;

        for (int iQuestion = 0; iQuestion < mQuestionList.size(); iQuestion++) {

            if (mQuestionList.get(iQuestion).getQuestionId() != 99999) {

                questionId = mQuestionList.get(iQuestion).getQuestionId();

                KEY_DATA += KEY_VALUE_OPEN;
                KEY_DATA += "question_id=\"";
                KEY_DATA += questionId;
                KEY_DATA += "\"";

                /*
                if (((int) Math.floor(mQuestionList.get(iQuestion).getAnswers().get(0).Id/100000))
                        == 333) {
                    /** Id 333* means editable text input and as such the answer was saved
                     * in mAnswerTexts array along with 333 + associated question id (unfortunately
                     * no specific answer id is featured in original implementation) **/

                /*
                    KEY_DATA += KEY_TAG_CLOSE;
                    KEY_DATA += getTextFromId(mQuestionList.get(iQuestion).getAnswers().get(0).Id);
                    KEY_DATA += KEY_VALUE_CLOSE;

                } else {
*/
                switch (mQuestionList.get(iQuestion).getAnswers().get(0).Text) {
                    case "$device.id":

                        KEY_DATA += KEY_TAG_CLOSE;
                        KEY_DATA += getDeviceId();
                        KEY_DATA += KEY_VALUE_CLOSE;
                        break;

                    case "$now":

                        KEY_DATA += KEY_TAG_CLOSE;
                        KEY_DATA += getTimeNow();
                        KEY_DATA += KEY_VALUE_CLOSE;
                        break;

                    case "$utcnow":

                        KEY_DATA += KEY_TAG_CLOSE;
                        KEY_DATA += getTimeNowUTC();
                        KEY_DATA += KEY_VALUE_CLOSE;
                        break;

                    default:

                        String ANSWER_DATA = "";

                        switch (mEvaluationList.getAnswerTypeFromQuestionId(questionId)) {
                            case "none":
                                break;
                            case "text":
                                ANSWER_DATA += mEvaluationList.getTextFromQuestionId(
                                        questionId);
                                break;
                            case "id":
                                ArrayList<String> listOfAnswerIds = mEvaluationList.
                                        getCheckedAnswerIdsFromQuestionId(questionId);

                                if (listOfAnswerIds.size() > 0) {
                                    for (int iId = 0; iId < listOfAnswerIds.size(); iId++) {
                                        // Option ids are separated by semicolon
                                        if (!ANSWER_DATA.isEmpty()) {
                                            ANSWER_DATA += ";";
                                        }
                                        ANSWER_DATA += listOfAnswerIds.get(iId);
                                    }
                                }
/*
                                        for (int iAnswer = 0; iAnswer < mQuestionList.get(iQuestion).
                                                getNumAnswers();
                                             iAnswer++) {
                                            // Collect all checked ids and bundle them
                                            if ((mQuestionList.get(iQuestion).getAnswerIds().
                                                    get(iAnswer) != 66666) &&
                                                    (mEvaluationList.containsAnswerId(mQuestionList.get(iQuestion).
                                                            getAnswerIds().get(iAnswer)))) {

                                                // Option ids are separated by semicolon
                                                if (!ANSWER_DATA.isEmpty()) {
                                                    ANSWER_DATA += ";";
                                                }
                                                ANSWER_DATA += mQuestionList.get(iQuestion).
                                                        getAnswerIds().get(iAnswer).toString();
                                            }
                                        }*/
                                break;
                            case "value":
                                ArrayList<String> listOfAnswerValues = mEvaluationList.
                                        getCheckedAnswerValuesFromQuestionId(questionId);

                                if (listOfAnswerValues.size() > 0) {
                                    for (int iId = 0; iId < listOfAnswerValues.size(); iId++) {
                                        // Option ids are separated by semicolon
                                        if (!ANSWER_DATA.isEmpty()) {
                                            ANSWER_DATA += ";";
                                        }
                                        ANSWER_DATA += listOfAnswerValues.get(iId);
                                    }
                                }
                                break;


                        }
                        // Add bundle of checked ids to record
                        if (!ANSWER_DATA.isEmpty()) {
                            KEY_DATA += " ";
                            KEY_DATA += "option_ids=\"";
                            KEY_DATA += ANSWER_DATA;
                            KEY_DATA += "\" ";
                        }
                        KEY_DATA += KEY_TAG_CLOSE_SOFT;
                        break;
                }
                //}
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
        return KEY_QUESTId;
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