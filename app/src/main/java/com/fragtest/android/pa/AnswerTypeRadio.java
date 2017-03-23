package com.fragtest.android.pa;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;


/**
 * Created by ulrikkowalk on 17.02.17.
 */

public class AnswerTypeRadio extends AppCompatActivity {

    RadioButton mAnswerButton;
    private Context mContext;
    LinearLayout.LayoutParams answerParams;
    RadioGroup mParent;
    private boolean bChecked = false;
    private int nAnswerID;

    public AnswerTypeRadio(Context context, int ID, String sAnswer, RadioGroup qParent) {

        mContext = context;
        nAnswerID = ID;
        mParent = qParent;
        mAnswerButton = new RadioButton(context);
        mAnswerButton.setId(nAnswerID);
        mAnswerButton.setText(sAnswer);
        mAnswerButton.setTextSize(20);                           //<<<<< TO DO: SP
        mAnswerButton.setChecked(false);
        mAnswerButton.setGravity(Gravity.START);
        mAnswerButton.setTextColor(Color.BLACK);
        mAnswerButton.setBackgroundColor(Color.WHITE);
        int states[][] = {{android.R.attr.state_checked}, {}};
        int colors[] = {R.color.JadeRed, R.color.JadeRed};
        CompoundButtonCompat.setButtonTintList(mAnswerButton, new ColorStateList(states, colors));
        mAnswerButton.setMinHeight(96);

        // Parameters of Answer Button
        answerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        //answerParams.setMargins(48,32,24,32);                           //<<<<< TO DO: DP


    }

    public boolean addAnswer() {
        mParent.addView(mAnswerButton, answerParams);
        return true;
    }

    public void setChecked() {
        mAnswerButton.setChecked(true);
    }

}
