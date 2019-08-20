package com.fragtest.android.pa.Core;

import com.fragtest.android.pa.Questionnaire.QuestionView;

import java.util.ArrayList;

/**
 * ListOfViews carries all QuestionView objects that are part of the current active Questionnaire
 */

public class ListOfViews extends ArrayList {

    private ArrayList<QuestionView> mList;

    public ListOfViews() {

        mList = new ArrayList<>();

    }

    public void add(QuestionView questionView) {
        mList.add(questionView);
    }

    public QuestionView get(int pos) {
        return mList.get(pos);
    }

    public QuestionView getFromId(int id) {
        for (int iItem = mList.size(); iItem >= 0; iItem--) {
            if (mList.get(iItem).getId() == id) {
                return mList.get(iItem);
            }
        }
        return null;
    }

    public void removeFromId(int id) {
        for (int iItem = mList.size(); iItem >= 0; iItem--) {
            if (mList.get(iItem).getId() == id) {
                mList.remove(iItem);
            }
        }
    }

    public int size() {
        return mList.size();
    }

    public int indexOf(Object object) {
        for (int iItem = 0; iItem < mList.size(); iItem++) {
            if (mList.get(iItem) == object) {
                return iItem;
            }
        }
        return -1;
    }

}
