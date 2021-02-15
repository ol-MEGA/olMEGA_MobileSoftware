package com.fragtest.android.pa.Questionnaire;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fragtest.android.pa.R;

/**
 * Created by ulrikkowalk on 17.02.17.
 */

public class AnswerTypeInfoScreen extends AppCompatActivity {

    private Button mAnswerButton;
    private LinearLayout.LayoutParams answerParams;
    private AnswerLayout parent;
    private Context mContext;
    private Questionnaire mQuestionnaire;
    private TextView mInfoText;
    private String LOG = "AnswerTypeInfoScreen";

    public AnswerTypeInfoScreen(Context context, Questionnaire questionnaire, AnswerLayout qParent) {

        mContext = context;
        mQuestionnaire = questionnaire;
        parent = qParent;

        mInfoText = new TextView(context);


        // Parameters of Answer Button
        answerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        answerParams.setMargins(
                (int) mContext.getResources().getDimension(R.dimen.answerFinishMargin_Left),
                (int) mContext.getResources().getDimension(R.dimen.answerFinishMargin_Top),
                (int) mContext.getResources().getDimension(R.dimen.answerFinishMargin_Right),
                (int) mContext.getResources().getDimension(R.dimen.answerFinishMargin_Bottom));
    }

    public boolean addAnswer(String sAnswer) {

        String[] tmpArray = sAnswer.split("<br/>");
        String tmp = "";
        for (int iLine = 0; iLine < tmpArray.length-1; iLine++) {
            tmp += tmpArray[iLine];
            tmp += System.getProperty ("line.separator");
        }
        tmp += tmpArray[tmpArray.length-1];

        mInfoText.setText(tmp);
        mInfoText.setTextColor(mContext.getResources().getColor(R.color.TextColor));
        mInfoText.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        mInfoText.setTextSize(mContext.getResources().getDimension(R.dimen.textSizeAnswer));

        parent.layoutAnswer.addView(mInfoText);
        return true;
    }

    public void addClickListener() {
    }
}
