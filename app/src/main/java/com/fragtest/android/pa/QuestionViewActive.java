package com.fragtest.android.pa;

import android.view.View;

import static android.R.attr.id;

/**
 * Created by ulrikkowalk on 09.03.17.
 */

public class QuestionViewActive implements Comparable<QuestionViewActive> {

    View mView;
    Integer mId;

    @Override
    public int compareTo(QuestionViewActive questionViewActive) {
        return this.mId.compareTo(questionViewActive.getId());
    }

    public QuestionViewActive (View view, int id) {
        mView = view;
        mId = id;
    }

    public View getView() { return mView; }
    public int getId() { return mId; }
}
