package com.fragtest.android.pa;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ulrikkowalk on 04.04.17.
 */

public class AnswerTypeSliderFix extends AppCompatActivity {

    public static String CLASS_NAME = "AnswerTypeSliderFix";
    private final List<Integer> mListOfIds = new ArrayList<>();
    public AnswerLayout parent;
    private Context mContext;
    private List<StringAndInteger> mListOfAnswers;
    private LinearLayout mHorizontalContainer, mAnswerListContainer;
    private View mResizeView, mRemainView;
    private RelativeLayout mSliderContainer;
    private int mDefaultAnswer = -1;
    private int width, mUsableHeight;
    private int nTextViewHeight, mQuestionId;
    private int[] answerLayoutPadding;
    private EvaluationList mEvaluationList;

    public AnswerTypeSliderFix(Context context, AnswerLayout qParent, int nQuestionId) {

        mContext = context;
        parent = qParent;
        mListOfAnswers = new ArrayList<>();
        mQuestionId = nQuestionId;

        // Slider Layout is predefined in XML
        LayoutInflater inflater = LayoutInflater.from(context);
        width = Units.getScreenWidth();
        answerLayoutPadding = Units.getAnswerLayoutPadding();

        parent.scrollContent.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.f
        ));

        mUsableHeight = (new Units(mContext)).getUsableSliderHeight();

        /**
         *
         *  |           mHorizontalContainer          |
         *  | mSliderContainer | mAnswerListContainer |
         *
         * **/

        // mHorizontalContainer is parent to both slider and answer option containers
        mHorizontalContainer = (LinearLayout) inflater.inflate(
                R.layout.answer_type_slider, parent.scrollContent, false);
        mHorizontalContainer.setOrientation(LinearLayout.HORIZONTAL);
        mHorizontalContainer.setLayoutParams(new LinearLayout.LayoutParams(
                width,
                mUsableHeight,
                1.f
        ));
        mHorizontalContainer.setBackgroundColor(
                ContextCompat.getColor(mContext, R.color.BackgroundColor));

        // mSliderContainer is host to slider on the left
        mSliderContainer = (RelativeLayout) mHorizontalContainer.findViewById(
                R.id.SliderContainer);
        mSliderContainer.setBackgroundColor(ContextCompat.getColor(
                mContext, R.color.BackgroundColor));

        // mAnswerListContainer is host to vertical array of answer options
        mAnswerListContainer = (LinearLayout) mHorizontalContainer.
                findViewById(R.id.AnswerTextContainer);

        mAnswerListContainer.setOrientation(LinearLayout.VERTICAL);
        mAnswerListContainer.setBackgroundColor(ContextCompat.getColor(
                mContext, R.color.BackgroundColor));
        mAnswerListContainer.setLayoutParams(new LinearLayout.LayoutParams(
                width-100,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.f
        ));

        mResizeView = mHorizontalContainer.findViewById(R.id.ResizeView);
        mRemainView = mHorizontalContainer.findViewById(R.id.RemainView);

        mResizeView.setBackgroundColor(ContextCompat.getColor(mContext,R.color.JadeRed));
    }

    public void buildView() {

        // Iterate over all options and create a TextView for each one
        for (int iAnswer = 0; iAnswer < mListOfAnswers.size(); iAnswer++) {
            TextView textMark = new TextView(mContext);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1.0f);

            textParams.setMargins(
                    (int) mContext.getResources().getDimension(R.dimen.SliderFixTextBottomMargin_Left),
                    (int) mContext.getResources().getDimension(R.dimen.SliderFixTextBottomMargin_Top),
                    (int) mContext.getResources().getDimension(R.dimen.SliderFixTextBottomMargin_Right),
                    (int) mContext.getResources().getDimension(R.dimen.SliderFixTextBottomMargin_Bottom));
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
            textMark.setBackgroundColor(ContextCompat.getColor(mContext,R.color.BackgroundColor));

            mAnswerListContainer.addView(textMark);
        }
        parent.layoutAnswer.addView(mHorizontalContainer);
    }

    public boolean addAnswer(int nAnswerId, String sAnswer, boolean isDefault) {
        mListOfAnswers.add(new StringAndInteger(sAnswer,nAnswerId));
        // index of default answer if present
        if (isDefault) {
            // If default present, this element is the one
            mDefaultAnswer = mListOfAnswers.size() - 1;
            // Handles default id if existent
            setProgressItem(mDefaultAnswer);

        }
        mListOfIds.add(nAnswerId);
        return true;
    }

    public AnswerIds addIdFromItem(int item, AnswerIds answerIds) {
        answerIds.removeAll(mListOfIds);
        answerIds.add(mListOfAnswers.get(item).getId());
        return answerIds;
    }

    public EvaluationList addClickListener(EvaluationList evaluationList) {
        mEvaluationList = evaluationList;

        final TextView tvTemp = (TextView) mAnswerListContainer.findViewById(mListOfAnswers.get(0).getId());
        tvTemp.post(new Runnable()
        {
            @Override
            public void run() {
                nTextViewHeight = tvTemp.getHeight();
                // Handles default id if existent
                if (mDefaultAnswer == -1) {

                    //                    answerIds.add(mListOfAnswers.get(mListOfAnswers.size() / 2).getId());
                    setProgressItem((int) ((mListOfAnswers.size()-1)/2.0f));
                    mEvaluationList.add(mQuestionId, mListOfAnswers.get(mListOfAnswers.size() / 2).getId());
                } else {
                    setProgressItem(mDefaultAnswer);
                    mEvaluationList.add(mQuestionId, mListOfAnswers.get(mDefaultAnswer).getId());
                }
            }
        });

        // Enables clicking on option directly
        for (int iAnswer = 0; iAnswer < mListOfAnswers.size(); iAnswer++) {
            final int numAnswer = iAnswer;
            final int currentId = mListOfAnswers.get(iAnswer).getId();
            TextView tv = (TextView) mAnswerListContainer.findViewById(currentId);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setProgressItem(numAnswer);
                    mEvaluationList.removeQuestionId(mQuestionId);
                    mEvaluationList.add(mQuestionId, currentId);
                    //answerIds.removeAll(mListOfIds);
                    //answerIds.add(currentId);
                }
            });
        }

        // Enables dragging of slider
        mResizeView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = MotionEventCompat.getActionMasked(event);

                switch(action) {
                    case (MotionEvent.ACTION_DOWN) :
                        return true;
                    case (MotionEvent.ACTION_MOVE) :
                        rescaleSliderOnline(event);
                        return true;
                    case (MotionEvent.ACTION_UP) :
                        rescaleSliderFinal(event, mEvaluationList);
                        return true;
                    case (MotionEvent.ACTION_CANCEL) :
                        return true;
                    case (MotionEvent.ACTION_OUTSIDE) :
                        Log.d("Motion","Movement occurred outside bounds " +
                                "of current screen element");
                        return true;
                    default :
                }
                return true;
            }
        });

        // Enables clicking in area above slider (remainView) to adjust
        mRemainView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = MotionEventCompat.getActionMasked(event);

                switch(action) {
                    case (MotionEvent.ACTION_DOWN) :
                        return true;
                    case (MotionEvent.ACTION_MOVE) :
                        rescaleSliderOnline(event);
                        return true;
                    case (MotionEvent.ACTION_UP) :
                        rescaleSliderFinal(event, mEvaluationList);
                        return true;
                    case (MotionEvent.ACTION_CANCEL) :
                        return true;
                    case (MotionEvent.ACTION_OUTSIDE) :
                        Log.d("Motion","Movement occurred outside bounds " +
                                "of current screen element");
                        return true;
                    default :
                }
                return true;
            }
        });
        return mEvaluationList;
    }


    // Set progress  bar according to user input
    private void rescaleSliderFinal(MotionEvent motionEvent, EvaluationList evaluationList) {
        int nValueSelected = (int) clipValuesToRange(motionEvent.getRawY());
        int nItem = mapValuesToItems(nValueSelected);
        nItem = clipItemsToRange(nItem);
        //int nItem = mapValuesToItems(nValueSelected);
        //nItem = clipItemsToRange(nItem);
        try {
            setProgressItem(nItem);
            //setProgressPixels(nValueSelected);
            evaluationList.removeQuestionId(mQuestionId);
            evaluationList.add(mQuestionId, mListOfAnswers.get(nItem).getId());
            //setProgressItem(nItem);
            //answerIds.removeAll(mListOfIds);
            //addIdFromItem(nItem, answerIds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Set progress  bar according to user input
    private void rescaleSliderOnline(MotionEvent motionEvent) {
        int nValueSelected = (int) clipValuesToRange(motionEvent.getRawY());
        int nItem = mapValuesToItems(nValueSelected);
        nItem = clipItemsToRange(nItem);
        try {
            //setProgressPixels(nValueSelected);
            setProgressItem(nItem);
            //setProgressItem(nItem);
            //answerIds.removeAll(mListOfIds);
            //addIdFromItem(nItem, answerIds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Ensure values inside slider boundaries
    private float clipValuesToRange(float inVal) {
        if (inVal < Units.getScreenHeight()-mUsableHeight-answerLayoutPadding[3]) {
            inVal =  Units.getScreenHeight()-mUsableHeight-answerLayoutPadding[3];
        } else if (inVal > Units.getScreenHeight()-answerLayoutPadding[3]) {
            inVal = Units.getScreenHeight()-answerLayoutPadding[3];
        }
        return inVal;
    }

    // Ensure valid item numbers
    private int clipItemsToRange(int item) {
        if (item > mListOfAnswers.size()-1) {
            item = mListOfAnswers.size()-1;
        } else if (item < 0) {
            item = 0;
        }
        return item;
    }

    // Enables quantisation of values (fixed choice options), counting from 0 (lowest)
    private int mapValuesToItems(float inVal) {
        return (int) ((inVal - (Units.getScreenHeight() -
                mUsableHeight-answerLayoutPadding[3]))/(nTextViewHeight));
    }

    // Set progress/slider according to number of selected item (counting from 0)
    public void setProgressItem(int numItem) {
        mResizeView.getLayoutParams().height =
                (int) ((2*(mListOfAnswers.size()-numItem)-1)/2.0f*nTextViewHeight);
        mResizeView.setLayoutParams(mResizeView.getLayoutParams());
    }
}



