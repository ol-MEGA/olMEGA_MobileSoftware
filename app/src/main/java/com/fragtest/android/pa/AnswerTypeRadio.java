package com.fragtest.android.pa;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;


/**
 * Created by ulrikkowalk on 17.02.17.
 */

public class AnswerTypeRadio extends AppCompatActivity {

    RadioButton answerButton;
    private Context mContext;
    LinearLayout.LayoutParams answerParams;
    RadioGroup mParent;
    private boolean bChecked = false;
    private int nAnswerID;

    public AnswerTypeRadio(Context context, int ID, String sAnswer, RadioGroup qParent) {

        mContext = context;
        nAnswerID = ID;
        mParent = qParent;
        answerButton = new RadioButton(context);
        answerButton.setId(nAnswerID);
        answerButton.setText(sAnswer);
        answerButton.setTextSize(20);                           //<<<<< TO DO: DP
        answerButton.setChecked(false);
        answerButton.setGravity(Gravity.START);
        answerButton.setTextColor(Color.BLACK);
        answerButton.setBackgroundColor(Color.WHITE);

        // Parameters of Answer Button
        answerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        answerParams.setMargins(48,32,24,32);                           //<<<<< TO DO: DP

    }

    public boolean addAnswer() {
        mParent.addView(answerButton, answerParams);
        return true;
    }

    public void setChecked() {
        answerButton.setChecked(true);
    }

}
