package com.fragtest.android.pa.Questionnaire;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.fragtest.android.pa.DataTypes.StringAndInteger;
import com.fragtest.android.pa.R;

import java.text.SimpleDateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ulrikkowalk on 17.02.17.
 */

public class AnswerTypeTime extends AnswerType {

    private static String LOG_STRING = "AnswerTypeRadio";
    private int mDefault = -1;
    private final SimpleDateFormat DATE_FORMAT;


    public AnswerTypeTime(Context context, Questionnaire questionnaire, AnswerLayout qParent, int nQuestionId) {

        super(context, questionnaire, qParent, nQuestionId);

        DATE_FORMAT = new SimpleDateFormat("HH:mm", Locale.ROOT);

    }

    public void addAnswer(int nAnswerId, String sAnswer) {
        mListOfAnswers.add(new StringAndInteger(sAnswer, nAnswerId));
        Log.e(LOG, "Zwischen ADDING: " + mListOfAnswers.size());
    }

    public void buildView() {

        try {
            Date now = Calendar.getInstance(TimeZone.getTimeZone("GMT+1")).getTime();
        } catch (Exception ex) {}

        for (int iTime = 0; iTime < mListOfAnswers.size(); iTime++) {

            try {

                Log.e(LOG, "Zwischen First: " +  DATE_FORMAT.parse(mListOfAnswers.get(iTime).getText().subSequence(0, 5).toString()));
                Log.e(LOG, "Zwischen Last: " +  DATE_FORMAT.parse(mListOfAnswers.get(iTime).getText().subSequence(6, 11).toString()));
                Log.e(LOG, "Zwischen Now: " +  DATE_FORMAT.parse(generateTimeNow()));

                Date first = DATE_FORMAT.parse(mListOfAnswers.get(iTime).getText().subSequence(0, 5).toString());
                Date last = DATE_FORMAT.parse(mListOfAnswers.get(iTime).getText().subSequence(6, 11).toString());
                Date test = DATE_FORMAT.parse(generateTimeNow());

                Log.e(LOG, "Zwischen Comp: " + test.compareTo(first) + ", and " + test.compareTo(last));

                if (test.compareTo(first) > 0 && test.compareTo(last) <= 0) {
                    Log.e(LOG, "Es ist zwischen " + first.toString() + " und " + last.toString());
                    mQuestionnaire.addIdToEvaluationList(mQuestionId, mListOfAnswers.get(iTime).getId());
                    return;
                } else {
                    Log.e(LOG, "zwischen NOT");
                }


            } catch (Exception ex) {}
        }
    }

    public void addClickListener() {

    }

    private String generateTimeNow() {
        Calendar dateTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
        return DATE_FORMAT.format(dateTime.getTime());
    }
}
