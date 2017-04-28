package com.fragtest.android.pa;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

/**
 * Created by ulrikkowalk on 17.02.17.
 */

public class AnswerTypeEmoji extends AppCompatActivity {

    Button mAnswerButton;
    private Context mContext;
    LinearLayout.LayoutParams answerParams;
    RadioGroup mParent;
    private View mPlaceHolder;
    private boolean mChecked = false;
    private Units mUnits;

    public AnswerTypeEmoji(Context context, int Id, String sAnswer, RadioGroup qParent) {

        mContext = context;
        mParent = qParent;
        mAnswerButton = new Button(context);
        mUnits = new Units(mContext);

        mAnswerButton.setBackground(ContextCompat.getDrawable(mContext, R.drawable.em1of5));
        //mAnswerButton.setText(sAnswer);
        mAnswerButton.setId(Id);


        /*
        mAnswerButton.setId(nAnswerId);
        mAnswerButton.setText(sAnswer);
        mAnswerButton.setTextSize(Units.getTextSizeAnswer());
        mAnswerButton.setChecked(false);
        mAnswerButton.setGravity(Gravity.CENTER_VERTICAL);
        mAnswerButton.setTextColor(ContextCompat.getColor(context, R.color.TextColor));
        mAnswerButton.setBackgroundColor(ContextCompat.getColor(context, R.color.BackgroundColor));
        int states[][] = {{android.R.attr.state_checked}, {}};
        int colors[] = {ContextCompat.getColor(context, R.color.JadeRed),
                ContextCompat.getColor(context, R.color.JadeRed)};
        CompoundButtonCompat.setButtonTintList(mAnswerButton, new ColorStateList(states, colors));
        mAnswerButton.setMinHeight(Units.getRadioMinHeight());
        mAnswerButton.setChecked(mChecked);
        mAnswerButton.setPadding(24,24,24,24);
        */

        // Parameters of Answer Button
        answerParams = new LinearLayout.LayoutParams(
                mUnits.convertDpToPixels(64),
                mUnits.convertDpToPixels(64));

        // Placeholder View because padding has no effect
        mPlaceHolder = new View(mContext);
        mPlaceHolder.setBackgroundColor(ContextCompat.getColor(mContext, R.color.BackgroundColor));
        mPlaceHolder.setLayoutParams(new LinearLayout.LayoutParams(
                mUnits.convertDpToPixels(20), mUnits.convertDpToPixels(20), 1.f
        ));
    }

    public boolean addAnswer() {
        mParent.addView(mAnswerButton, answerParams);
        mParent.addView(mPlaceHolder);
        return true;
    }

    public AnswerIds addClickListener(AnswerIds answerIds) {
        mAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleChecked();
            }
        });
        return answerIds;
    }

    public void toggleChecked() {
        if (mChecked) {
           setChecked(false);
        } else {
            setChecked(true);
        }
    }

    public void setChecked(boolean isChecked) {
        if (isChecked) {
            mChecked = true;
            mAnswerButton.setBackground(ContextCompat.getDrawable(mContext, R.drawable.em1of5_active));
        } else {
            mChecked = false;
            mAnswerButton.setBackground(ContextCompat.getDrawable(mContext, R.drawable.em1of5));
        }
    }


}

