package com.fragtest.android.pa;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ulrikkowalk on 20.02.17.
 */

public class AnswerTexts extends ArrayList<String> {

    private QuestionnairePagerAdapter mContextQPA;

    private List<String> mAnswerTexts;

    public AnswerTexts(QuestionnairePagerAdapter contextQPA) {
        mContextQPA = contextQPA;
        mAnswerTexts = new ArrayList<String>();
    }
}