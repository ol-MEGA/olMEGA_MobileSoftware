package com.fragtest.android.pa.Core;

import com.fragtest.android.pa.Questionnaire.QuestionView;

import java.util.ArrayList;

/**
 * ListOfViews carries all QuestionView objects that are part of the current active Questionnaire
 */

public class ListOfViews extends ArrayList<QuestionView> {

    //private ArrayList<QuestionView> mList;

    public ListOfViews() {

        //mList = new ArrayList<>();

    }

    //public void add(QuestionView questionView) {
    //    this.add(questionView);
    //}

    //public QuestionView get(int pos) {
    //    return this.get(pos);
    //}

    public QuestionView getFromId(int id) {
        for (int iItem = 0; iItem < this.size(); iItem++) {
            if (this.get(iItem).getId() == id) {
                return this.get(iItem);
            }
        }
        return null;
    }

    public void removeFromId(int id) {
        for (int iItem = this.size() - 1; iItem >= 0; iItem--) {
            if (this.get(iItem).getId() == id) {
                this.remove(iItem);
            }
        }
    }

    public int getPosFromId(int id) {
        for (int iItem = 0; iItem < this.size(); iItem++) {
            if (this.get(iItem).getId() == id) {
                return iItem;
            }
        }
        return -1;
    }

    //public int size() {
    //    return this.size();
    //}



    public int indexOf(Object object) {
        for (int iItem = 0; iItem < this.size(); iItem++) {
            if (this.get(iItem) == object) {
                return iItem;
            }
        }
        return -1;
    }

}
