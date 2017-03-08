package com.fragtest.android.pa;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by ulrikkowalk on 17.02.17.
 */

public class QuestionText extends AppCompatActivity {

    TextView questionTextView;
    LinearLayout.LayoutParams questionLayoutParams;
    LinearLayout parent;
    Calculations mCalculations;

    public QuestionText(Context context, int nQuestionID, String sQuestion, LinearLayout qParent) {

        mCalculations = new Calculations(context);
        parent = qParent;
        questionTextView = new TextView(context);
        questionTextView.setId(nQuestionID);
        questionTextView.setTextColor(Color.BLACK);
        questionTextView.setBackgroundColor(Color.WHITE);
        questionTextView.setTextSize(mCalculations.convertSpToPixels(9));        //<<<<< TO DO: SP
        questionTextView.setText(sQuestion);
        questionTextView.setPadding(mCalculations.convertDpToPixels(16),
                mCalculations.convertDpToPixels(8),
                mCalculations.convertDpToPixels(16),
                mCalculations.convertDpToPixels(16));
        questionLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

    }

    public boolean addQuestion() {
        parent.addView(
                questionTextView,questionLayoutParams);
        return true;
    }
}
