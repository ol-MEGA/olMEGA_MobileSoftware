package com.fragtest.android.pa;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ulrikkowalk on 04.04.17.
 */

public class AnswerTypeSliderFix extends AppCompatActivity {


    public CheckBox mAnswerButton;
    public LinearLayout.LayoutParams answerParams;
    public AnswerLayout parent;
    private int nAnswerID;
    private Context mContext;
    private List<StringAndInteger> mListOfAnswers;
    private VerticalSeekBar mSeekBar;
    private LinearLayout mLayout, mCategoryLayout, mSeekBarMainLayout;
    private RelativeLayout mSeekBarContainer;
    private Units mUnits;

    public AnswerTypeSliderFix(Context context, AnswerLayout qParent) {

        mContext = context;
        parent = qParent;
        mListOfAnswers = new ArrayList<>();
        mUnits = new Units(mContext);

        Rect rectangle = new Rect();
        Window window = getWindow();
/*
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        int contentViewTop =
                window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight = contentViewTop - statusBarHeight;*/
        int titleBarHeight = mUnits.getStatusBarHeight();

        Log.e("tb",""+titleBarHeight);

        // calculate height of usable display space in pixels
        int usableHeight = Units.getScreenHeight()-Units.getQuestionTextHeight()-
                mUnits.convertDpToPixels(32)-mUnits.convertDpToPixels(6)-
                Units.getAnswerLayoutPadding()[1]-Units.getAnswerLayoutPadding()[3]-
                titleBarHeight;


        parent.scrollContent.setBackgroundColor(Color.YELLOW);
        parent.layoutAnswer.setBackgroundColor(Color.GREEN);
        parent.scrollContent.setMinimumHeight(usableHeight);

        Log.e("height",""+usableHeight);

        mLayout = new LinearLayout(mContext);
        mLayout.setOrientation(LinearLayout.HORIZONTAL);
        mLayout.setMinimumHeight(usableHeight);

        mSeekBarMainLayout = new LinearLayout(mContext);
        LinearLayout.LayoutParams seekBarMainLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.3f
        );


        mSeekBarContainer = new RelativeLayout(mContext);
        mSeekBarContainer.setBackgroundColor(Color.MAGENTA);
        RelativeLayout.LayoutParams seekBarContainerParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        seekBarContainerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        seekBarContainerParams.addRule(RelativeLayout.CENTER_VERTICAL);


        mSeekBar = new VerticalSeekBar(mContext);
        RelativeLayout.LayoutParams seekBarParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        seekBarParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        seekBarParams.addRule(RelativeLayout.CENTER_VERTICAL);


        mCategoryLayout = new LinearLayout(mContext);
        mCategoryLayout.setBackgroundColor(Color.CYAN);
        mCategoryLayout.setPadding(0,0,0,0);
        mCategoryLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams categoryLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.7f
        );

        mSeekBarContainer.addView(mSeekBar,seekBarParams);
        mSeekBarMainLayout.addView(mSeekBarContainer,seekBarContainerParams);
        mLayout.addView(mSeekBarMainLayout,seekBarMainLayoutParams);
        mLayout.addView(mCategoryLayout,categoryLayoutParams);




    }


    public void buildSlider() {


        for (int iAnswer = 0; iAnswer < mListOfAnswers.size(); iAnswer++) {
            TextView textMark = new TextView(mContext);
            textMark.setPadding(0, 0, 0, 0);
            textMark.setText(mListOfAnswers.get(iAnswer).getText());
            textMark.setId(mListOfAnswers.get(iAnswer).getID());
            textMark.setTextSize(Units.getTextSizeAnswer());
            textMark.setTextColor(ContextCompat.getColor(mContext, R.color.TextColor));
            textMark.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0,
                    1));
            if (mListOfAnswers.size() == 0) {
                textMark.setGravity(Gravity.TOP);
            } else if (iAnswer == mListOfAnswers.size() - 1) {
                textMark.setGravity(Gravity.BOTTOM);
            }

            View view = new View(mContext);
            view.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                    0,
                    0,
                    1.0f
            ));


            mCategoryLayout.addView(textMark);
            if (iAnswer < mListOfAnswers.size()-1) {
                mCategoryLayout.addView(view);
            }
        }


        mSeekBar.setMax(mListOfAnswers.size()-1);
        parent.layoutAnswer.addView(mLayout);

    }

    public boolean addAnswer(int nAnswerID, String sAnswer) {
        mListOfAnswers.add(new StringAndInteger(sAnswer,nAnswerID));
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
            }
        });
        return vAnswerIDs;
    }
}



