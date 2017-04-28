package com.fragtest.android.pa;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ulrikkowalk on 04.04.17.
 */

public class AnswerTypeSliderFix_OLD extends AppCompatActivity {

    public AnswerLayout parent;
    private Context mContext;
    private List<StringAndInteger> mListOfAnswers;
    public VerticalSeekBar mSeekBar;
    private LinearLayout mLayout, mCategoryLayout;
    private final List<Integer> mListOfIds = new ArrayList<>();
    private AnswerIds mAnswerIds;
    private int mDefaultAnswer = 0;
    public static String CLASS_NAME = "AnswerTypeSliderFix";

    public AnswerTypeSliderFix_OLD(Context context, AnswerLayout qParent) {

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

        int usableHeight = (new Units(mContext)).getUsableSliderHeight();
        parent.scrollContent.setMinimumHeight(usableHeight);

        mLayout = new LinearLayout(mContext);
        mLayout.setOrientation(LinearLayout.HORIZONTAL);
        mLayout.setMinimumHeight(usableHeight);


        LinearLayout mSeekBarMainLayout = new LinearLayout(mContext);
        LinearLayout.LayoutParams seekBarMainLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.3f
        );


        RelativeLayout mSeekBarContainer = new RelativeLayout(mContext);
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
        mSeekBar.setThumb(ContextCompat.getDrawable(mContext,
                android.R.drawable.radiobutton_on_background));

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

    public AnswerIds buildSlider(AnswerIds answerIds) {

        mAnswerIds = answerIds;

        for (int iAnswer = mListOfAnswers.size()-1; iAnswer >= 0; iAnswer--) {
            TextView textMark = new TextView(mContext);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0,
                    1.0f);

            if (iAnswer == 0) {
                textParams.setMargins(
                        (int) mContext.getResources().getDimension(R.dimen.SliderFixTextMargin_Left),
                        (int) mContext.getResources().getDimension(R.dimen.SliderFixTextMargin_Top),
                        (int) mContext.getResources().getDimension(R.dimen.SliderFixTextMargin_Right),
                        (int) mContext.getResources().getDimension(R.dimen.SliderFixTextMargin_Bottom));
            } else if (iAnswer == mListOfAnswers.size()-1) {
                textParams.setMargins(
                        (int) mContext.getResources().getDimension(R.dimen.SliderFixTextTopMargin_Left),
                        (int) mContext.getResources().getDimension(R.dimen.SliderFixTextTopMargin_Top),
                        (int) mContext.getResources().getDimension(R.dimen.SliderFixTextTopMargin_Right),
                        (int) mContext.getResources().getDimension(R.dimen.SliderFixTextTopMargin_Bottom));
            } else {
                textParams.setMargins(
                        (int) mContext.getResources().getDimension(R.dimen.SliderFixTextBottomMargin_Left),
                        (int) mContext.getResources().getDimension(R.dimen.SliderFixTextBottomMargin_Top),
                        (int) mContext.getResources().getDimension(R.dimen.SliderFixTextBottomMargin_Right),
                        (int) mContext.getResources().getDimension(R.dimen.SliderFixTextBottomMargin_Bottom));
            }
            textMark.setPadding(
                    (int) mContext.getResources().getDimension(R.dimen.SliderFixTextPadding_Left),
                    (int) mContext.getResources().getDimension(R.dimen.SliderFixTextPadding_Top),
                    (int) mContext.getResources().getDimension(R.dimen.SliderFixTextPadding_Right),
                    (int) mContext.getResources().getDimension(R.dimen.SliderFixTextPadding_Bottom));
            textMark.setText(mListOfAnswers.get(iAnswer).getText());
            textMark.setId(mListOfAnswers.get(iAnswer).getId());
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
        }

        if (mDefaultAnswer == 0) {
            mAnswerIds.add(mListOfAnswers.get(mListOfAnswers.size() / 2).getId());
            mSeekBar.setProgress(mListOfAnswers.size() / 2);
        } else {
            mAnswerIds.add(mListOfAnswers.get(mDefaultAnswer).getId());
            mSeekBar.setProgress(mDefaultAnswer);
        }

        mSeekBar.setMax(mListOfAnswers.size()-1);
        parent.layoutAnswer.addView(mLayout);

        Log.i(CLASS_NAME,"Slider successfully built.");

        return mAnswerIds;
    }

    public boolean addAnswer(int nAnswerId, String sAnswer, boolean isDefault) {
        mListOfAnswers.add(new StringAndInteger(sAnswer,nAnswerId));
        // index of default answer if present
        if (isDefault) {
            // If default present, this element is the one
            mDefaultAnswer = mListOfAnswers.size() - 1;
        }
        mListOfIds.add(nAnswerId);
        return true;
    }

    public AnswerIds addIdFromProgress(int progress) {
        mAnswerIds.removeAll(mListOfIds);
        mAnswerIds.add(mListOfAnswers.get(progress).getId());
        return mAnswerIds;
    }

    public void synchronizeThumb() {
        mSeekBar.setProgress(mSeekBar.getProgress());
        mSeekBar.setThumbOffset(0);
    }
}



