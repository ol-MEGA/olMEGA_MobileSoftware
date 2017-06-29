package com.fragtest.android.pa.Questionnaire;

import android.view.View;

import java.util.List;

/**
 * Created by ulrikkowalk on 09.03.17.
 */

public class QuestionViewActive implements Comparable<QuestionViewActive> {

    private View mView;
    private Integer mId;
    private Integer mPositionInRaw;
    private boolean mMandatory;
    private List<Answer> mListOfAnswerIds;

    @Override
    public int compareTo(QuestionViewActive questionViewActive) {
        return this.mPositionInRaw.compareTo(questionViewActive.getPositionInRaw());
    }

    public QuestionViewActive (View view, int id, int positionInRaw, boolean mandatory,
                               List<Answer> listOfAnswerIds) {
        mView = view;
        mId = id;
        mPositionInRaw = positionInRaw;
        mMandatory = mandatory;
        mListOfAnswerIds = listOfAnswerIds;
    }

    public View getView() { return mView; }

    public int getId() { return mId; }

    public List<Answer> getListOfAnswerIds() { return mListOfAnswerIds; }

    public int getPositionInRaw() { return mPositionInRaw; }

    public boolean isMandatory() { return mMandatory; }

}
