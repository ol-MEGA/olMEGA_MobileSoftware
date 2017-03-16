package com.fragtest.android.pa;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ulrikkowalk on 17.02.17.
 */

public class AnswerTypeText extends AppCompatActivity {

    public EditText mAnswerText;
    public LinearLayout.LayoutParams answerParams;
    public AnswerLayout parent;
    private int nAnswerID;
    private Context mContext;

    public AnswerTypeText(Context context, int ID, AnswerLayout qParent) {

        mContext = context;
        nAnswerID = ID;
        parent = qParent;
        mAnswerText = new EditText(mContext);
        mAnswerText.setId(nAnswerID);
        mAnswerText.setTextSize(20);                           //<<<<< TO DO: DP
        mAnswerText.setGravity(Gravity.START);
        mAnswerText.setTextColor(Color.BLACK);
        mAnswerText.setBackgroundColor(Color.WHITE);
        mAnswerText.setHint("kurze Beschreibung");

        // Parameters of Answer Button
        answerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        answerParams.setMargins(48,32,24,32);                           //<<<<< TO DO: DP
    }

    public boolean addAnswer() {
        parent.layoutAnswer.addView(mAnswerText,answerParams);
        return true;
    }

/*
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
*/
}
