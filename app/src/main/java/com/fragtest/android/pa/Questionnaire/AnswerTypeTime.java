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
    }

    public void buildView() {

        try {
            Date now = Calendar.getInstance(TimeZone.getTimeZone("GMT+1")).getTime();
        } catch (Exception ex) {}

        for (int iTime = 0; iTime < mListOfAnswers.size(); iTime++) {

            try {
                Date first = DATE_FORMAT.parse(mListOfAnswers.get(iTime).getText().subSequence(0, 5).toString());
                Date last = DATE_FORMAT.parse(mListOfAnswers.get(iTime).getText().subSequence(6, 10).toString());
                Date test = DATE_FORMAT.parse(mListOfAnswers.get(iTime).getText());

                Log.e(LOG, "First: " + first.toString() + ", Last: " + last.toString() + ", Now: " + test.toString());

            } catch (Exception ex) {}



            Log.e(LOG_STRING, "Time now: " + generateTimeNow());
            Log.e(LOG_STRING, "Time: " + mListOfAnswers.get(iTime).getText() + ", id: " + mListOfAnswers.get(iTime).getId());

        }

    }

    public void addClickListener() {

    }

    private String generateTimeNow() {
        Calendar dateTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+1"));
        return DATE_FORMAT.format(dateTime.getTime());
    }
}
