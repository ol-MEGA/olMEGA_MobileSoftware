package com.fragtest.android.pa.Questionnaire;

import android.view.View;

import java.util.List;

/**
 * Created by ulrikkowalk on 09.03.17.
 */

public class QuestionViewActive implements Comparable<QuestionViewActive> {

    private final View mView;
    private final Integer mId;
    private final Integer mPositionInRaw;
    private final boolean mMandatory;
    private final List<Answer> mListOfAnswerIds;

    public QuestionViewActive(View view, int id, int positionInRaw, boolean mandatory,
                              List<Answer> listOfAnswerIds) {
        mView = view;
        mId = id;
        mPositionInRaw = positionInRaw;
        mMandatory = mandatory;
        mListOfAnswerIds = listOfAnswerIds;
    }

    @Override
    public int compareTo(QuestionViewActive questionViewActive) {
        return this.mPositionInRaw.compareTo(questionViewActive.getPositionInRaw());
    }

    public View getView() {
        return mView;
    }

    public int getId() {
        return mId;
    }

    List<Answer> getListOfAnswerIds() {
        return mListOfAnswerIds;
    }

    int getPositionInRaw() {
        return mPositionInRaw;
    }

    boolean isMandatory() {
        return mMandatory;
    }

}
