package com.fragtest.android.pa;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by ulrikkowalk on 17.02.17.
 */

public class AnswerTypeCheckBox extends AppCompatActivity {

    public CheckBox mAnswerButton;
    public LinearLayout.LayoutParams answerParams;
    public AnswerLayout parent;
    private int nAnswerId;
    private Context mContext;

    public AnswerTypeCheckBox(Context context, int Id, String sAnswer, AnswerLayout qParent) {

        mContext = context;
        nAnswerId = Id;
        parent = qParent;
        mAnswerButton = new CheckBox(mContext);
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

        // Parameters of Answer Button
        answerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mAnswerButton.setMinHeight(Units.getCheckBoxMinHeight());
    }

    public boolean addAnswer() {
        parent.layoutAnswer.addView(mAnswerButton,answerParams);
        return true;
    }


    public AnswerIds addClickListener(final AnswerIds vAnswerIds) {
        mAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implementing Lists simplifies the task of finding and removing the correct Id
                ArrayList<Integer> AnswerId = new ArrayList<>();
                AnswerId.add(nAnswerId);
                if (mAnswerButton.isChecked()) {
                    vAnswerIds.addAll(AnswerId);
                } else {
                    vAnswerIds.removeAll(AnswerId);
                }
            }
        });
        return vAnswerIds;
    }

    public void setChecked() {
        mAnswerButton.setChecked(true);
    }

}
