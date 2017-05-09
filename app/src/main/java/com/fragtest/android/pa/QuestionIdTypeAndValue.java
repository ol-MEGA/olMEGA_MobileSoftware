package com.fragtest.android.pa;

import android.util.Log;

/**
 * Created by ulrikkowalk on 09.05.17.
 */

public class QuestionIdTypeAndValue {

    private int QuestionId;
    private String AnswerType;
    private String Value;
    private String LOG_STRING = "QuestionIdTypeAndValue";

    public QuestionIdTypeAndValue(int nQuestionId, String sAnswerType, String sValue) {
        QuestionId = nQuestionId;
        AnswerType = sAnswerType;
        Value = sValue;

        Log.i(LOG_STRING, "QId: " + QuestionId + ", Type: " + sAnswerType + ", Value: " + Value);
    }

    public int getQuestionId() {
        return QuestionId;
    }

    public String getAnswerType() {
        return AnswerType;
    }

    public String getValue() {
        return Value;
    }
}
