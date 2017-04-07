package com.fragtest.android.pa;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
    private int mSeekBarProgress;
    private final List<Integer> mListOfIds = new ArrayList<>();
    private AnswerIDs mAnswerIDs = new AnswerIDs();

    public AnswerTypeSliderFix(Context context, AnswerLayout qParent) {

        /**
         *  parent
         *      -> scrollContent
         *          -> layoutAnswer
         *              -> mLayout
         *                  -> mSeekBarMainLayout
         *                      -> mSeekBarContainer
         *                          -> mSeekBar [actual SeekBar]
         *                  -> mCategoryLayout ["Ticks"]
         */

        mContext = context;
        parent = qParent;
        mListOfAnswers = new ArrayList<>();
        mUnits = new Units(mContext);
        mSeekBarProgress = 0;

        int usableHeight = mUnits.getUsableSliderHeight();

        //parent.scrollContent.setBackgroundColor(Color.YELLOW);
        //parent.layoutAnswer.setBackgroundColor(Color.GREEN);
        parent.scrollContent.setMinimumHeight(usableHeight);

        Log.i("Usable height",""+usableHeight);

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
        //mSeekBarContainer.setBackgroundColor(Color.MAGENTA);
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
        seekBarParams.setMargins(0,0,0,40);
        mSeekBar.setBackgroundColor(ContextCompat.getColor(mContext,R.color.BackgroundColor));
        mSeekBar.setThumb(ContextCompat.getDrawable(mContext,android.R.drawable.radiobutton_on_background));

        int states[][] = {{android.R.attr.state_checked}, {}};
        int colorsThumb[] = {ContextCompat.getColor(context, R.color.JadeRed),
                ContextCompat.getColor(context, R.color.JadeRed)};
        int colorsProgress[] = {ContextCompat.getColor(context, R.color.JadeRed),
                ContextCompat.getColor(context, R.color.JadeRed)};
        mSeekBar.setThumbTintList(new ColorStateList(states, colorsThumb));
        mSeekBar.setProgressTintList(new ColorStateList(states, colorsProgress));





        mCategoryLayout = new LinearLayout(mContext);
        //mCategoryLayout.setBackgroundColor(Color.CYAN);
        mCategoryLayout.setPadding(0,0,0,0);
        mCategoryLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams categoryLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                usableHeight,
                0.7f
        );

        mSeekBarContainer.addView(mSeekBar,seekBarParams);
        mSeekBarMainLayout.addView(mSeekBarContainer,seekBarContainerParams);
        mLayout.addView(mSeekBarMainLayout,seekBarMainLayoutParams);
        mLayout.addView(mCategoryLayout,categoryLayoutParams);
    }


    public void buildSlider() {

        for (int iAnswer = mListOfAnswers.size()-1; iAnswer >= 0; iAnswer--) {
            TextView textMark = new TextView(mContext);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0,
                    1.0f);

            if (iAnswer == 0) {
                textParams.setMargins(0, 20, 0, 0);
            } else if (iAnswer == mListOfAnswers.size()-1) {
                textParams.setMargins(0, -20, 0, 20);
            } else {
                textParams.setMargins(0, 20, 0, 0);
            }

            textMark.setPadding(0, 0, 0, 0);
            textMark.setText(mListOfAnswers.get(iAnswer).getText());
            textMark.setId(mListOfAnswers.get(iAnswer).getID());
            textMark.setTextColor(ContextCompat.getColor(mContext, R.color.TextColor));

            // Adaptive size and lightness of font of in-between tick marks
            if (mListOfAnswers.size() > 6) {
                int textSize = Units.getTextSizeAnswer() * 7 /mListOfAnswers.size();
                if (iAnswer%2 == 1){
                    textSize -= 2;
                    textMark.setTextColor(ContextCompat.getColor(mContext,R.color.TextColor_Light));
                }
                textMark.setTextSize(textSize);
            } else {
                textMark.setTextSize(Units.getTextSizeAnswer());
            }


            textMark.setGravity(Gravity.CENTER_VERTICAL);
            textMark.setLayoutParams(textParams);
            mCategoryLayout.addView(textMark);

            mSeekBar.setProgress(mListOfAnswers.size()/2);

        }


        mSeekBar.setMax(mListOfAnswers.size()-1);
        parent.layoutAnswer.addView(mLayout);
    }

    public void addClickListener(AnswerIDs answerIDs) {
        mAnswerIDs = answerIDs;

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //if (fromUser) {
                    mSeekBarProgress = progress;
                    mAnswerIDs.removeAll(mListOfIds);
                    mAnswerIDs.add(mListOfAnswers.get(mSeekBarProgress).getID());
                    Log.e("ID added", "" + mListOfAnswers.get(mSeekBarProgress).getID());
                //}
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Log.e("Touch","Started");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Log.e("Touch","Stopped");
            }
        });


    }

    public boolean addAnswer(int nAnswerID, String sAnswer) {
        mListOfAnswers.add(new StringAndInteger(sAnswer,nAnswerID));
        mListOfIds.add(nAnswerID);
        return true;
    }



}



