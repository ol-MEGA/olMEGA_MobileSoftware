package com.fragtest.android.pa;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ulrikkowalk on 06.03.17.
 */

public class QuestionInfo {

    private int mId;
    private ArrayList<Integer> mFilterId;
    private boolean mCondition;
    private boolean mActive;
    private boolean mHidden;
    private boolean mMandatory;
    private int mPositionInPager;
    private Question mQuestion;
    private List<Integer> mListOfAnswerIds;

    public QuestionInfo(Question question, int id, ArrayList<Integer> filterId,
                        int position, boolean hidden, boolean mandatory,
                        List<Integer> answerIds) {
        mQuestion = question;
        mId = id;
        mFilterId = filterId;
        //mCondition = condition;
        mActive = true;
        mPositionInPager = position;
        mHidden = hidden;
        mMandatory = mandatory;
        mListOfAnswerIds = answerIds;
    }

    public boolean isActive() { return mActive; }

    public int getId() { return mId; }

    public ArrayList<Integer> getFilterId() { return mFilterId; }

    public ArrayList<Integer> getFilterIdPositive() {
        ArrayList<Integer> listOfPositiveIds = new ArrayList<>();
        for (int iElement =0; iElement < mFilterId.size(); iElement++) {
            if (mFilterId.get(iElement) >= 0) {
                listOfPositiveIds.add(mFilterId.get(iElement));
            }
        }
        return listOfPositiveIds;
    }

    public ArrayList<Integer> getFilterIdNegative() {
        ArrayList<Integer> listOfNegativeIds = new ArrayList<>();
        for (int iElement = 0; iElement < mFilterId.size(); iElement++) {
            if (mFilterId.get(iElement) < 0) {
                listOfNegativeIds.add((-1)*mFilterId.get(iElement));
            }
        }
        return listOfNegativeIds;
    }

    public boolean existsFilterId() {
        if (mFilterId.size()>0) {
            return true;
        } else {
            return false;
        }
    }

    //public boolean getCondition() { return mCondition; }

    public boolean isMandatory() { return mMandatory; }

    public void setInactive() { mActive = false; }

    public void setActive() { mActive = true; }

    public void setPositionInPager(int position) { mPositionInPager = position; }

    public int getPositionInPager() { return mPositionInPager; }

    public Question getQuestion() { return mQuestion; }

    public boolean isHidden() { return mHidden; }

    public List<Integer> getAnswerIds() { return mListOfAnswerIds; }

}
