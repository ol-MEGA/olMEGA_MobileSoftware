package com.fragtest.android.pa.Questionnaire;

import android.util.Log;

import com.fragtest.android.pa.BuildConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ulrikkowalk on 06.03.17.
 */

public class QuestionInfo {

    private final String LOG_STRING = "QuestionInfo";
    private final int mId;
    private final ArrayList<Integer> mFilterId;
    private final boolean mHidden;
    //private final boolean mMandatory;
    private final Question mQuestion;
    private final List<Integer> mListOfAnswerIds;
    private boolean mActive;
    private int mPositionInPager;

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
        //mMandatory = mandatory;
        mListOfAnswerIds = answerIds;
    }

    public boolean isActive() {
        return mActive;
    }

    public int getId() {
        return mId;
    }

    public ArrayList<Integer> getFilterIdPositive() {
        // Function returns all positive Filter IDs which represent the MUST EXIST cases
        ArrayList<Integer> listOfPositiveIds = new ArrayList<>();
        for (int iElement = 0; iElement < mFilterId.size(); iElement++) {
            if (mFilterId.get(iElement) >= 0) {
                listOfPositiveIds.add(mFilterId.get(iElement));
            }
        }
        return listOfPositiveIds;
    }

    public ArrayList<Integer> getFilterIdNegative() {
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

    public void setInactive() {
        mActive = false;
        if (BuildConfig.DEBUG) {
            Log.i(LOG_STRING, "View set inactive");
        }
    }

    public void setActive() {
        mActive = true;
        if (BuildConfig.DEBUG) {
            Log.i(LOG_STRING, "View set active");
        }
    }

    public int getPositionInPager() {
        return mPositionInPager;
    }

    public void setPositionInPager(int position) {
        mPositionInPager = position;
    }

    public Question getQuestion() {
        return mQuestion;
    }

    public boolean isHidden() {
        return mHidden;
    }

    public List<Integer> getAnswerIds() {
        return mListOfAnswerIds;
    }

}

/*
    public ArrayList<Integer> getFilterId() {
        return mFilterId;
    }

    public boolean existsFilterId() {
        if (mFilterId.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isMandatory() {
        return mMandatory;
    }
 */