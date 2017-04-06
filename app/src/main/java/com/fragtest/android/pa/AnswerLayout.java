package com.fragtest.android.pa;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * Created by ulrikkowalk on 17.02.17.
 */

public class AnswerLayout extends AppCompatActivity {

    LinearLayout layoutAnswer;
    ScrollView scrollContent;

    public AnswerLayout(Context context) {
        // Main Layout has to be incorporated in ScrollView for Overflow Handling
        scrollContent = new ScrollView(context);
        scrollContent.setBackgroundColor(ContextCompat.getColor(context, R.color.BackgroundColor));
        scrollContent.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        scrollContent.setId(0);

        // Main Layout - Right now Framework carrying ONE Question
        layoutAnswer = new LinearLayout(context);

        int[] answerLayoutPadding = Units.getAnswerLayoutPadding();
        layoutAnswer.setPadding(answerLayoutPadding[0], answerLayoutPadding[1],
                answerLayoutPadding[2], answerLayoutPadding[3]);
        layoutAnswer.setOrientation(LinearLayout.VERTICAL);
        layoutAnswer.setBackgroundColor(ContextCompat.getColor(context, R.color.BackgroundColor));
        layoutAnswer.setGravity(Gravity.CENTER_HORIZONTAL);
        layoutAnswer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        // Linear Layout is Child to ScrollView (must always be)
        scrollContent.addView(layoutAnswer);

    }
}
