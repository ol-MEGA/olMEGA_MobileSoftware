package com.fragtest.android.pa;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ulrikkowalk on 20.02.17.
 */

public class AnswerIDs extends ArrayList<Integer> {

    private QuestionnairePagerAdapter mContextQPA;

    private List<Integer> mAnswerIDs;

    public AnswerIDs(QuestionnairePagerAdapter contextQPA) {
        mContextQPA = contextQPA;
        mAnswerIDs = new ArrayList<Integer>();
    }

    public int getNumAnswers() {
        return mAnswerIDs.size();
    }

    public void generateVisibility() {
        /**  DECIDE WHICH IDS TRIGGER WHICH VIEW TO TOGGLE  **/
        //mContextQPA.toggleVisibility();

    }




/*

    public boolean toggleIDRadio(int checkedID, RadioGroup radioGroup) {

        int nChildren = radioGroup.getChildCount();

        for (int iChild = 0;iChild < nChildren; iChild++) {
            View child = radioGroup.getChildAt(iChild);
            int nID = child.getId();
            if (vAnswerIDs.contains(nID) && (nID != checkedID)) {
                removeID(nID);
            } else if ((!vAnswerIDs.contains(nID)) && (nID == checkedID)) {
                addID(nID);
            }
        }

        return true;

    }

    public boolean contains(int nID) {
        for (int iID = 0; iID < vAnswerIDs.size(); iID++) {
            if (vAnswerIDs.get(iID) == nID) {
                return true;
            }
        }
        return false;
    }
    */
}
