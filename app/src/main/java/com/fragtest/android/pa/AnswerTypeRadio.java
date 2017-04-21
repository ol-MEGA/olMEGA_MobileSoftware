package com.fragtest.android.pa;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
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
    private boolean mChecked = false;
    private int nAnswerId;

    public AnswerTypeRadio(Context context, int Id, String sAnswer, RadioGroup qParent) {

        mContext = context;
        nAnswerId = Id;
        mParent = qParent;
        mAnswerButton = new RadioButton(context);
        mAnswerButton.setId(nAnswerId);
        mAnswerButton.setText(sAnswer);
        mAnswerButton.setTextSize(Units.getTextSizeAnswer());
        mAnswerButton.setChecked(false);
        mAnswerButton.setGravity(Gravity.START);
        mAnswerButton.setTextColor(ContextCompat.getColor(context, R.color.TextColor));
        mAnswerButton.setBackgroundColor(ContextCompat.getColor(context, R.color.BackgroundColor));
        int states[][] = {{android.R.attr.state_checked}, {}};
        int colors[] = {ContextCompat.getColor(context, R.color.JadeRed),
                ContextCompat.getColor(context, R.color.JadeRed)};
        CompoundButtonCompat.setButtonTintList(mAnswerButton, new ColorStateList(states, colors));
        mAnswerButton.setMinHeight(Units.getRadioMinHeight());
        mAnswerButton.setChecked(mChecked);

        // Parameters of Answer Button
        answerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    public boolean addAnswer() {
        mParent.addView(mAnswerButton, answerParams);
        return true;
    }

    public void setChecked() {
        mAnswerButton.setChecked(true);
    }

}
