package com.fragtest.android.pa;

import android.content.Intent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.id;

/**
 * Created by ulrikkowalk on 09.03.17.
 */

public class QuestionViewActive implements Comparable<QuestionViewActive> {

    View mView;
    Integer mId;
    Integer mPositionInRaw;
    List<Answer> mListOfAnswerIDs;

    @Override
    public int compareTo(QuestionViewActive questionViewActive) {
        return this.mPositionInRaw.compareTo(questionViewActive.getPositionInRaw());
    }

    public QuestionViewActive (View view, int id, int positionInRaw, List<Answer> listOfAnswerIDs) {
        mView = view;
        mId = id;
        mPositionInRaw = positionInRaw;
        mListOfAnswerIDs = listOfAnswerIDs;
    }

    public View getView() { return mView; }
    public int getId() { return mId; }
    public List<Answer> getListOfAnswerIDs() { return mListOfAnswerIDs; }
    public int getPositionInRaw() { return mPositionInRaw; }
}
