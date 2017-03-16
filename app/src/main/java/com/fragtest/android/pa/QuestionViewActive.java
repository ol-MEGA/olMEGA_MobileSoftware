package com.fragtest.android.pa;

import android.view.View;

import static android.R.attr.id;

/**
 * Created by ulrikkowalk on 09.03.17.
 */

public class QuestionViewActive implements Comparable<QuestionViewActive> {

    View mView;
    Integer mId;
    Integer mPositionInRaw;

    @Override
    public int compareTo(QuestionViewActive questionViewActive) {
        return this.mPositionInRaw.compareTo(questionViewActive.getPositionInRaw());
    }

    public QuestionViewActive (View view, int id, int positionInRaw) {
        mView = view;
        mId = id;
        mPositionInRaw = positionInRaw;
    }

    public View getView() { return mView; }
    public int getId() { return mId; }
    public int getPositionInRaw() { return mPositionInRaw; }
}
