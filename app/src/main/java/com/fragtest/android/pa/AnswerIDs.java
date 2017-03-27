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
}
