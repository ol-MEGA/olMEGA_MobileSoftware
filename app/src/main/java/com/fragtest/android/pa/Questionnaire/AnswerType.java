package com.fragtest.android.pa.Questionnaire;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.fragtest.android.pa.DataTypes.StringAndInteger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ul1021 on 06.02.2018.
 */

public abstract class AnswerType extends AppCompatActivity {

    static final String LOG = "AnswerType";
    final AnswerLayout mParent;
    final Context mContext;
    final int mQuestionId;
    final List<StringAndInteger> mListOfAnswers;
    final Questionnaire mQuestionnaire;

    public AnswerType(Context context, Questionnaire questionnaire, AnswerLayout parent, int Id) {
        mContext = context;
        mQuestionnaire = questionnaire;
        mParent = parent;
        mQuestionId = Id;
        mListOfAnswers = new ArrayList<>();
    }

    public abstract void buildView();

    public abstract void addClickListener();

}
