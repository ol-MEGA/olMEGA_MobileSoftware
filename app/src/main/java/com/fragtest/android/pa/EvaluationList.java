package com.fragtest.android.pa;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ulrikkowalk on 09.05.17.
 */

public class EvaluationList extends ArrayList<QuestionIdTypeAndValue> {

    private String LOG_STRING = "EvaluationList";
    private List<QuestionIdTypeAndValue> mEvaluationList;

    public EvaluationList() {
        mEvaluationList = new ArrayList<>();
    }

    // For answer Ids
    public boolean add(int nQuestionId, int nAnswerId) {
        mEvaluationList.add(new QuestionIdTypeAndValue(
                nQuestionId, "Id", Integer.toString(nAnswerId)));
        return true;
    }

    // For answer texts
    public boolean add(int nQuestionId, String sText) {
        mEvaluationList.add(new QuestionIdTypeAndValue(
                nQuestionId, "Text", sText));
        return true;
    }

    // For floating point values
    public boolean add(int nQuestionId, float nValue) {
        mEvaluationList.add(new QuestionIdTypeAndValue(
                nQuestionId, "Value", Float.toString(nValue)));
        return true;
    }

    //Remove all answers with given Ids in input list
    public boolean removeAll(ArrayList<Integer> listOfIds) {
        int nRemoved = 0;
        for (int iId = 0; iId < listOfIds.size(); iId++) {
            int currentId = listOfIds.get(iId);
            for (int iAnswer = mEvaluationList.size() - 1; iAnswer >= 0; iAnswer--) {
                if ((mEvaluationList.get(iAnswer).getAnswerType().equals("Id")) &&
                        (mEvaluationList.get(iAnswer).getValue().equals(
                                Integer.toString(currentId)))) {
                    mEvaluationList.remove(iAnswer);
                    nRemoved++;
                }
            }
        }
        Log.i(LOG_STRING, "Entries removed: " + nRemoved);
        return true;
    }

    //Remove all answers from question with given QuestionId
    public boolean removeQuestionId(int QuestionId) {
        int nRemoved = 0;
        for (int iAnswer = mEvaluationList.size() - 1; iAnswer >= 0; iAnswer--) {
            if (mEvaluationList.get(iAnswer).getQuestionId() == QuestionId) {
                mEvaluationList.remove(iAnswer);
                nRemoved++;
            }
        }
        Log.i(LOG_STRING, "Entries removed: " + nRemoved);
        return true;
    }

    //Remove all answers of given type
    public boolean removeAll(String sType) {
        int nRemoved = 0;
        for (int iAnswer = mEvaluationList.size() - 1; iAnswer >= 0; iAnswer--) {
            if (mEvaluationList.get(iAnswer).getAnswerType().equals(sType)) {
                mEvaluationList.remove(iAnswer);
                nRemoved++;
            }
        }
        Log.i(LOG_STRING, "Entries removed: " + nRemoved);
        return true;
    }

    //Remove given AnswerId
    public boolean removeAnswerId(int Id) {
        int nRemoved = 0;
        for (int iAnswer = mEvaluationList.size() - 1; iAnswer >= 0; iAnswer--) {
            if ((mEvaluationList.get(iAnswer).getAnswerType().equals("Id")) &&
                    (mEvaluationList.get(iAnswer).getValue().equals(Integer.toString(Id)))) {
                mEvaluationList.remove(iAnswer);
                nRemoved++;
            }
        }
        Log.i(LOG_STRING, "Entries removed: " + nRemoved);
        return true;
    }
}
