package com.fragtest.android.pa;

import static android.R.attr.handle;

/**
 * Created by ulrikkowalk on 06.03.17.
 */

public class QuestionInfo {

    private int mId;
    private int mFilterId;
    private boolean mCondition;
    private boolean mActive;
    private int mPosition;

    public QuestionInfo(int id, int filterId, boolean condition, int position) {
        mId = id;
        mFilterId = filterId;
        mCondition = condition;
        mPosition = position;
        mActive = true;
    }

    public boolean isActive() { return mActive; }

    public int getId() { return mId; }

    public int getFilterId() { return mFilterId; }

    public boolean getCondition() { return mCondition; }

    public int getPosition() { return mPosition; }

    public void setInactive() { mActive = false; }

    public void setActive() { mActive = true; }
}
