package com.fragtest.android.pa.Questionnaire;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import com.fragtest.android.pa.Core.LogIHAB;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ulrikkowalk on 17.02.17.
 */

public class AnswerTypeTime extends AppCompatActivity {

    private final int mQuestionId;
    private final Questionnaire mQuestionnaire;
    private final Context mContext;
    private final SimpleDateFormat DATE_FORMAT;
    private String LOG = "AnswerTypeTime";

    public AnswerTypeTime(Context context, Questionnaire questionnaire, int questionId) {

        mContext = context;
        mQuestionnaire = questionnaire;
        mQuestionId = questionId;
        DATE_FORMAT = new SimpleDateFormat("HH:mm", Locale.ROOT);
    }

    public void addAnswer(int nAnswerId, String sAnswer) {

        try {

            Date first = DATE_FORMAT.parse(sAnswer.subSequence(0, 5).toString());
            Date last = DATE_FORMAT.parse(sAnswer.subSequence(6, 11).toString());
            Date test = DATE_FORMAT.parse(generateTimeNow());

            if (test.compareTo(first) > 0 && test.compareTo(last) <= 0) {
                mQuestionnaire.addIdToEvaluationList(mQuestionId, nAnswerId);
                LogIHAB.log("Time-based decision made in favour of id: " + nAnswerId);
            }

        } catch (Exception ex) {
            LogIHAB.log("Exception caught during AnswerTypeTime");
        }
    }

    private String generateTimeNow() {
        Calendar dateTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
        return DATE_FORMAT.format(dateTime.getTime());
    }

    private String generateTimeNowUTC() {
        Calendar dateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return DATE_FORMAT.format(dateTime.getTime());
    }
}

