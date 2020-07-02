package com.fragtest.android.pa.Questionnaire;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fragtest.android.pa.Core.Units;
import com.fragtest.android.pa.MainActivity;
import com.fragtest.android.pa.R;

/**
 * Created by ulrikkowalk on 17.02.17.
 */

public class QuestionText extends AppCompatActivity {

    final TextView questionTextView;
    final LinearLayout.LayoutParams questionLayoutParams;
    final LinearLayout parent;
    final Units mUnits;
    final String mText;
    private MainActivity mContext;

    public QuestionText(MainActivity context, int nQuestionId, String sQuestion, LinearLayout qParent) {

        mContext = context;
        parent = qParent;
        mUnits = new Units(context);
        mText = sQuestion;
        questionTextView = new TextView(context);
        questionTextView.setId(nQuestionId);
        questionTextView.setTextColor(ContextCompat.getColor(context, R.color.TextColor));
        questionTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.lighterGray));
        questionTextView.setTextSize(context.getResources().getDimension(R.dimen.textSizeQuestion));
        questionTextView.setText(sQuestion);
        questionTextView.setPadding(mUnits.convertDpToPixels(16),
                mUnits.convertDpToPixels(8),
                mUnits.convertDpToPixels(16),
                mUnits.convertDpToPixels(16));
        questionLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        questionTextView.setMinHeight((int) (context.getResources().getDimension(R.dimen.textSizeQuestion)));
    }

    public boolean addQuestion() {
        parent.addView(
                questionTextView, questionLayoutParams);
        return true;
    }

    public int getQuestionHeight() {
        return spToPx(mContext.getResources().getDimension(R.dimen.textSizeQuestion), mContext) * approximateLineCount(mText);
    }

    public static int spToPx(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public int approximateLineCount(String sText) {
        return (int) Math.ceil(sText.length() / 24.0);
    }

}
