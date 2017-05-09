package com.fragtest.android.pa;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by ulrikkowalk on 03.05.17.
 */

public class AnswerValues extends ArrayList<IntAndFloat>{

    private ArrayList<IntAndFloat> mAnswerValues;

    public AnswerValues() {mAnswerValues = new ArrayList<>();}

    public boolean add(int id, float value) {
        mAnswerValues.add(new IntAndFloat(id, value));
        return true;
    }

    public boolean add(IntAndFloat intAndFloat) {
        mAnswerValues.add(intAndFloat);
        return true;
    }

    public float getValueFromId(int id) {
        for (int iEntry = 0; iEntry < mAnswerValues.size(); iEntry++) {
            if (mAnswerValues.get(iEntry).getId() == id) {
                return mAnswerValues.get(iEntry).getValue();
            }
        }
        return -255.f;
    }

    public void removeValueWithId(int id) {
        for (int iEntry = 0; iEntry < mAnswerValues.size(); iEntry++) {
            if (mAnswerValues.get(iEntry).getId() == id) {
                mAnswerValues.remove(iEntry);
                break;
            }
        }
    }
}
