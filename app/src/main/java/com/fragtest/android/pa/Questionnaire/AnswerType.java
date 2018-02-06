package com.fragtest.android.pa.Questionnaire;

import android.content.Context;

import com.fragtest.android.pa.DataTypes.StringIntegerAndInteger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ul1021 on 06.02.2018.
 */

public class AnswerType {

    private static final String LOG = "AnswerType";
    public final AnswerLayout mParent;
    private final Context mContext;
    private final int mQuestionId;
    private final List<StringIntegerAndInteger> mListOfAnswers;
    private final Questionnaire mQuestionnaire;

    public AnswerType(Context context, Questionnaire questionnaire, AnswerLayout parent, int Id) {

        mContext = context;
        mQuestionnaire = questionnaire;
        mParent = parent;
        mQuestionId = Id;
        mListOfAnswers = new ArrayList<>();
    }

    public void addAnswer(){}

    public void buildView(){}

    public void addClickListener(){}

}
