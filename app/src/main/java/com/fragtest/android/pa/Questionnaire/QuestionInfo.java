package com.fragtest.android.pa.Questionnaire;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ulrikkowalk on 06.03.17.
 */

class QuestionInfo {

    private final String LOG_STRING = "QuestionInfo";
    private final int mId;
    private final ArrayList<Integer> mFilterId;
    private final boolean mHidden;
    //private final boolean mMandatory;
    private final Question mQuestion;
    private final List<Integer> mListOfAnswerIds;
    private boolean mActive;
    private int mPositionInPager;

    QuestionInfo(Question question, int id, ArrayList<Integer> filterId,
                 int position, boolean hidden, boolean mandatory,
                 List<Integer> answerIds) {
        mQuestion = question;
        mId = id;
        mFilterId = filterId;
        mActive = true;
        mPositionInPager = position;
        mHidden = hidden;
        mListOfAnswerIds = answerIds;
    }

    boolean isActive() {
        return mActive;
    }

    public int getId() {
        return mId;
    }

    ArrayList<Integer> getFilterIdPositive() {
        // Function returns all positive Filter IDs which represent the MUST EXIST cases
        ArrayList<Integer> listOfPositiveIds = new ArrayList<>();
        for (int iElement = 0; iElement < mFilterId.size(); iElement++) {
            if (mFilterId.get(iElement) >= 0) {
                listOfPositiveIds.add(mFilterId.get(iElement));
            }
        }
        return listOfPositiveIds;
    }

    ArrayList<Integer> getFilterIdNegative() {
        // Function returns all negative IDs (only absolute values), which represent the MUST NOT
        // EXIST case.
        ArrayList<Integer> listOfNegativeIds = new ArrayList<>();
        for (int iElement = 0; iElement < mFilterId.size(); iElement++) {
            if (mFilterId.get(iElement) < 0) {
                listOfNegativeIds.add((-1) * mFilterId.get(iElement));
            }
        }
        return listOfNegativeIds;
    }

    void setInactive() {
        mActive = false;
    }

    void setActive() {
        mActive = true;
    }

    int getPositionInPager() {
        return mPositionInPager;
    }

    void setPositionInPager(int position) {
        mPositionInPager = position;
    }

    public Question getQuestion() {
        return mQuestion;
    }

    boolean isHidden() {
        return mHidden;
    }

    List<Integer> getAnswerIds() {
        return mListOfAnswerIds;
    }

}
