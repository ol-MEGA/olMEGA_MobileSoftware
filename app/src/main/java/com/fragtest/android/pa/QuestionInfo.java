package com.fragtest.android.pa;

import android.util.Log;

import static android.R.attr.handle;

/**
 * Created by ulrikkowalk on 06.03.17.
 */

public class QuestionInfo {

    private int mId;
    private int mFilterId;
    private boolean mCondition;
    private boolean mActive;
    private int mPositionInPager;
    private Question mQuestion;

    public QuestionInfo(Question question, int id, int filterId, boolean condition, int position) {
        mQuestion = question;
        mId = id;
        mFilterId = filterId;
        mCondition = condition;
        mActive = true;
        mPositionInPager = position;
    }

    public boolean isActive() { return mActive; }

    public int getId() { return mId; }

    public int getFilterId() { return mFilterId; }

    public boolean getCondition() { return mCondition; }

    public void setInactive() { mActive = false; }

    public void setActive() { mActive = true; }

    public void setPositionInPager(int position) { mPositionInPager = position; }

    public int getPositionInPager() { return mPositionInPager; }

    public Question getQuestion() { return mQuestion; }

}
