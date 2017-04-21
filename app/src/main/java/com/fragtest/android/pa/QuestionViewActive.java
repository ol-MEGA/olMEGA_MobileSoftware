package com.fragtest.android.pa;

import android.view.View;

import java.util.List;

/**
 * Created by ulrikkowalk on 09.03.17.
 */

public class QuestionViewActive implements Comparable<QuestionViewActive> {

    View mView;
    Integer mId;
    Integer mPositionInRaw;
    List<Answer> mListOfAnswerIds;

    @Override
    public int compareTo(QuestionViewActive questionViewActive) {
        return this.mPositionInRaw.compareTo(questionViewActive.getPositionInRaw());
    }

    public QuestionViewActive (View view, int id, int positionInRaw, List<Answer> listOfAnswerIds) {
        mView = view;
        mId = id;
        mPositionInRaw = positionInRaw;
        mListOfAnswerIds = listOfAnswerIds;
    }

    public View getView() { return mView; }
    public int getId() { return mId; }
    public List<Answer> getListOfAnswerIds() { return mListOfAnswerIds; }
    public int getPositionInRaw() { return mPositionInRaw; }
}
