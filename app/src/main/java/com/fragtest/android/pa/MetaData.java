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

    private static String META_DATA = "MetaData";

    private String DEVICE_Id, START_DATE, START_DATE_UTC, END_DATE,
            END_DATE_UTC, KEY_HEAD, KEY_FOOT, KEY_TAG_CLOSE, KEY_VALUE_OPEN, KEY_VALUE_CLOSE,
            KEY_TAG_CLOSE_SOFT, KEY_SURVEY_URI, KEY_RECORD_OPEN, KEY_RECORD_CLOSE, KEY_DATA,
            KEY_QUESTId, mRawInput, FILE_NAME;

    private SimpleDateFormat DATE_FORMAT;

    private int mTimeQuery = 0;
    private int mTimeQueryUTC = 0;

    private ArrayList<Question> mQuestionList;

    private Context mContext;

    private AnswerIds mAnswerIds;
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

        Log.i(META_DATA, "Object initialised");
        return true;
    }

    public boolean finalise(AnswerIds answerIds, AnswerTexts answerTexts) {

        mAnswerIds = answerIds;
        mAnswerTexts = answerTexts;
        // Obtain current Time Stamp at the End of Questionnaire
        END_DATE = generateTimeNow();
        // Obtain current UTC Time Stamp at the End of Questionnaire
        END_DATE_UTC = generateTimeNowUTC();
        collectData();

        Log.i(META_DATA, "Object finalised");
        return true;
    }

    public void addQuestion(Question question) {
        mQuestionList.add(question);
    }

    private void collectData() {

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

                KEY_DATA += KEY_VALUE_OPEN;
                KEY_DATA += "question_id=\"";
                KEY_DATA += mQuestionList.get(iQuestion).getQuestionId();
                KEY_DATA += "\"";

                if (((int) Math.floor(mQuestionList.get(iQuestion).getAnswers().get(0).Id/100000))
                        == 333) {
                    /** Id 333* means editable text input and as such the answer was saved
                     * in mAnswerTexts array along with 333 + associated question id (unfortunately
                     * no specific answer id is featured in original implementation) **/

                    KEY_DATA += KEY_TAG_CLOSE;
                    KEY_DATA += getTextFromId(mQuestionList.get(iQuestion).getAnswers().get(0).Id);
                    KEY_DATA += KEY_VALUE_CLOSE;

                } else {

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
                            for (int iAnswer = 0; iAnswer < mQuestionList.get(iQuestion).
                                    getNumAnswers();
                                 iAnswer++) {
                                /** All possible answer Ids are found, but have to be checked for
                                 * whether they appear in mAnswerIds or not. If yes, they are
                                 * printed to output String. Pay attention to forced blank spaces -
                                 * Id 66666 **/

                                // Collect all checked ids and bundle them
                                if ((mQuestionList.get(iQuestion).getAnswerIds().
                                        get(iAnswer) != 66666) &&
                                        (mAnswerIds.contains(mQuestionList.get(iQuestion).
                                                getAnswerIds().get(iAnswer)))) {

                                    // Option ids are separated by semicolon
                                    if (!ANSWER_DATA.isEmpty()) {
                                        ANSWER_DATA += ";";
                                    }
                                    ANSWER_DATA += mQuestionList.get(iQuestion).
                                            getAnswerIds().get(iAnswer).toString();
                                }
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
                }
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
        for (int iText = 0; iText <mAnswerTexts.size(); iText++){
            if (mAnswerTexts.get(iText).getId() == id) {
                return mAnswerTexts.get(iText).getText();
            }
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