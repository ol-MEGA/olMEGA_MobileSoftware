package com.fragtest.android.pa;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ulrikkowalk on 17.02.17.
 */

public class AnswerTypeFinish extends AppCompatActivity {

    public Button mAnswerButton;
    public LinearLayout.LayoutParams answerParams;
    public AnswerLayout parent;
    private int nAnswerID;
    private Context mContext;

    public AnswerTypeFinish(Context context, int ID, AnswerLayout qParent) {

        mContext = context;
        nAnswerID = ID;
        parent = qParent;
        mAnswerButton = new Button(context);
        mAnswerButton.setId(nAnswerID);
        mAnswerButton.setText("Abschließen");
        mAnswerButton.setTextSize(20);                           //<<<<< TO DO: DP
        mAnswerButton.setGravity(Gravity.CENTER_HORIZONTAL);
        mAnswerButton.setTextColor(Color.BLACK);
        mAnswerButton.setBackgroundColor(Color.WHITE);
        mAnswerButton.setAllCaps(false);

        // Parameters of Answer Button
        answerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        answerParams.setMargins(48,32,24,32);                           //<<<<< TO DO: DP
    }

    public boolean addAnswer() {
        parent.layoutAnswer.addView(mAnswerButton,answerParams);
        return true;
    }


    public void addClickListener() {
        mAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,"Information was recorded.",Toast.LENGTH_SHORT).show();
            }
        });

    }

}
