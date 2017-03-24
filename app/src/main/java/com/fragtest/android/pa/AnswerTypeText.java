package com.fragtest.android.pa;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.KeyEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;

import static java.security.AccessController.getContext;

/**
 * Created by ulrikkowalk on 17.02.17.
 */

public class AnswerTypeText extends AppCompatActivity {

    public EditText mAnswerText;
    public LinearLayout.LayoutParams answerParams;
    public AnswerLayout parent;
    private Button mButtonOkay;
    private LinearLayout.LayoutParams buttonParams;
    private int nAnswerID;
    private Context mContext;

    public AnswerTypeText(Context context, int ID, AnswerLayout qParent) {

        mContext = context;
        nAnswerID = ID;
        parent = qParent;

        mAnswerText = new EditText(mContext);
        mAnswerText.setId(nAnswerID);
        mAnswerText.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        mAnswerText.setTextSize(Units.getTextSizeAnswer());
        mAnswerText.setGravity(Gravity.START);
        mAnswerText.setTextColor(ContextCompat.getColor(context, R.color.TextColor));
        mAnswerText.setBackgroundColor(ContextCompat.getColor(context, R.color.BackgroundColor));
        mAnswerText.setHint(R.string.hintTextAnswer);
        mAnswerText.setHintTextColor(ContextCompat.getColor(context, R.color.JadeGray));

        // Parameters of Answer Button Layout
        answerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        int[] AnswerTextMargins = Units.getAnswerTextMargin();
        answerParams.setMargins(AnswerTextMargins[0],AnswerTextMargins[1],
                AnswerTextMargins[2],AnswerTextMargins[3]);

        mButtonOkay = new Button(mContext);
        mButtonOkay.setText(R.string.buttonTextOkay);
        mButtonOkay.setTextColor(ContextCompat.getColor(context, R.color.TextColor));
        mButtonOkay.setBackground(ContextCompat.getDrawable(context, R.drawable.button));
        buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    public boolean addAnswer() {
        parent.layoutAnswer.addView(mAnswerText, answerParams);
        parent.layoutAnswer.addView(mButtonOkay, buttonParams);
        return true;
    }

    public void addClickListener(final AnswerTexts answerTexts) {
        mButtonOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mAnswerText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


                String text = mAnswerText.getText().toString();
                if (text.length() != 0) {
                    answerTexts.add(text);
                    Log.e("Text",text);
                } else {
                    Log.e("No","Text");
                }
            }
        });
    }


}
