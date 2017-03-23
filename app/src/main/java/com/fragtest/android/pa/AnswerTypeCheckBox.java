package com.fragtest.android.pa;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ulrikkowalk on 17.02.17.
 */

public class AnswerTypeCheckBox extends AppCompatActivity {

    public CheckBox mAnswerButton;
    public LinearLayout.LayoutParams answerParams;
    public AnswerLayout parent;
    private int nAnswerID;
    private Context mContext;

    public AnswerTypeCheckBox(Context context, int ID, String sAnswer, AnswerLayout qParent) {

        mContext = context;
        nAnswerID = ID;
        parent = qParent;
        mAnswerButton = new CheckBox(context);
        mAnswerButton.setId(nAnswerID);
        mAnswerButton.setText(sAnswer);
        mAnswerButton.setTextSize(20);                           //<<<<< TO DO: DP
        mAnswerButton.setChecked(false);
        mAnswerButton.setGravity(Gravity.START);
        mAnswerButton.setTextColor(Color.BLACK);
        mAnswerButton.setBackgroundColor(Color.WHITE);
        int states[][] = {{android.R.attr.state_checked}, {}};
        int colors[] = {R.color.JadeRed, R.color.JadeRed};
        CompoundButtonCompat.setButtonTintList(mAnswerButton, new ColorStateList(states, colors));

        // Parameters of Answer Button
        answerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        //answerParams.setMargins(0,8,0,8);                           //<<<<< TO DO: DP
        mAnswerButton.setMinHeight(96);
    }

    public boolean addAnswer() {
        parent.layoutAnswer.addView(mAnswerButton,answerParams);
        return true;
    }


    public AnswerIDs addClickListener(final AnswerIDs vAnswerIDs) {
        mAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implementing Lists simplifies the task of finding and removing the correct ID
                ArrayList<Integer> AnswerID = new ArrayList<>();
                AnswerID.add(nAnswerID);
                if (mAnswerButton.isChecked()) {
                    vAnswerIDs.addAll(AnswerID);
                } else {
                    vAnswerIDs.removeAll(AnswerID);
                }

                Toast.makeText(mContext,""+vAnswerIDs.size(),Toast.LENGTH_SHORT).show();
            }
        });
        return vAnswerIDs;
    }

    public void setChecked() {
        mAnswerButton.setChecked(true);
    }

}
