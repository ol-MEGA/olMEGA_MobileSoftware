package com.fragtest.android.pa;

import java.util.List;

/**
 * Created by ulrikkowalk on 06.03.17.
 */

public class QuestionInfo {

    private int mId;
    private int mFilterId;
    private boolean mCondition;
    private boolean mActive;
    private boolean mHidden;
    private boolean mMandatory;
    private int mPositionInPager;
    private Question mQuestion;
    private List<Integer> mListOfAnswerIds;

    public QuestionInfo(Question question, int id, int filterId,
                        boolean condition, int position, boolean hidden, boolean mandatory,
                        List<Integer> answerIds) {
        mQuestion = question;
        mId = id;
        mFilterId = filterId;
        mCondition = condition;
        mActive = true;
        mPositionInPager = position;
        mHidden = hidden;
        mMandatory = mandatory;
        mListOfAnswerIds = answerIds;
    }

    public boolean isActive() { return mActive; }

    public int getId() { return mId; }

    public int getFilterId() { return mFilterId; }

    public boolean getCondition() { return mCondition; }

    public boolean isMandatory() { return mMandatory; }

    public void setInactive() { mActive = false; }

    public void setActive() { mActive = true; }

    public void setPositionInPager(int position) { mPositionInPager = position; }

    public int getPositionInPager() { return mPositionInPager; }

    public Question getQuestion() { return mQuestion; }

    public boolean isHidden() { return mHidden; }

    public List<Integer> getAnswerIds() { return mListOfAnswerIds; }

}
