package com.fragtest.android.pa;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * Created by ulrikkowalk on 17.02.17.
 */

public class AnswerLayout extends AppCompatActivity {

    LinearLayout layoutAnswer;
    ScrollView scrollContent;
    boolean bNegative = false;
    int nFilterID;


    public AnswerLayout(Context context) {
        // Main Layout has to be incorporated in ScrollView for Overflow Handling
        scrollContent = new ScrollView(context);
        scrollContent.setBackgroundColor(0x00FFFFFF);
        scrollContent.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        scrollContent.setId(0);

        // Main Layout - Right now Framework carrying ONE Question
        layoutAnswer = new LinearLayout(context);

        layoutAnswer.setPadding(0, 48, 48, 48);
        layoutAnswer.setOrientation(LinearLayout.VERTICAL);
        layoutAnswer.setBackgroundColor(Color.LTGRAY);

        // Linear Layout is Child to ScrollView
        scrollContent.addView(layoutAnswer);

    }

    public boolean setTag(int tag) {
        scrollContent.setTag(tag);
        return true;
    }

    public int getTag() {
        int tag = Integer.parseInt(scrollContent.getTag().toString());
        return tag;
    }

    public void setVisibility(int visibility) {
        scrollContent.setVisibility(visibility);
    }
}
