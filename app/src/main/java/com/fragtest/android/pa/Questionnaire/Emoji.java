package com.fragtest.android.pa.Questionnaire;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by ul1021 on 12.09.2017.
 */

public class Emoji {

    private int mId;
    private boolean isChecked = false;
    private boolean isDefault = false;
    private int mResourceUnchecked, mResourceChecked;
    private Button mAnswerButton;
    private Context mContext;
    private int mSize, mTag;

    public Emoji(Context ctx, int resUnchecked, int resChecked, int size, boolean def) {
        mContext = ctx;
        mResourceUnchecked = resUnchecked;
        mResourceChecked = resChecked;
        mAnswerButton = new Button(mContext);
        mSize = size;
        isDefault = def;
    }

    public Button createButton(){
        mAnswerButton.setLayoutParams(new LinearLayout.LayoutParams(
                mSize,
                mSize,
                1.0f));

        if (isDefault) {
            mAnswerButton.setBackground(ContextCompat.getDrawable(mContext, mResourceChecked));
        } else {
            mAnswerButton.setBackground(ContextCompat.getDrawable(mContext, mResourceUnchecked));
        }

        return mAnswerButton;
    }

    public void setTag(int tag) {
        mTag = tag;
        mAnswerButton.setTag(mTag);
    }

    public void setDefault(boolean def) {
        isDefault = def;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
        if (isChecked) {
            mAnswerButton.setBackground(ContextCompat.getDrawable(mContext, mResourceChecked));
        } else {
            mAnswerButton.setBackground(ContextCompat.getDrawable(mContext, mResourceUnchecked));
        }

    }
}
