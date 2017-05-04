package com.fragtest.android.pa;

import android.content.Context;
import android.graphics.Color;
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

import static android.R.attr.fraction;
import static android.R.attr.text;

/**
 * Created by ulrikkowalk on 04.04.17.
 */

public class AnswerTypeSliderFree extends AppCompatActivity {

    public AnswerLayout parent;
    private Context mContext;
    private List<StringAndInteger> mListOfAnswers;
    //public VerticalSeekBar mSeekBar;
    private LinearLayout mHorizontalContainer, mAnswerListContainer;
    private View mResizeView, mRemainView;
    private RelativeLayout mSliderContainer;
    private final List<Integer> mListOfIds = new ArrayList<>();
    //private AnswerIds mAnswerIds;
    private int mDefaultAnswer = -1;
    public static String CLASS_NAME = "AnswerTypeSliderFix";
    private int width, mUsableHeight, mSliderMaxHeight, mQuestionId;
    private AnswerValues mAnswerValues;

    public AnswerTypeSliderFree(Context context, AnswerLayout qParent) {

        mContext = context;
        parent = qParent;
        mListOfAnswers = new ArrayList<>();

        LayoutInflater inflater = LayoutInflater.from(context);
        width = Units.getScreenWidth();

        parent.scrollContent.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.f
        ));

        mUsableHeight = (new Units(mContext)).getUsableSliderHeight();

        // mHorizontalContainer is parent to both slider and answer option containers
        mHorizontalContainer = (LinearLayout) inflater.inflate(
                R.layout.answer_type_slider, parent.scrollContent, false);
        mHorizontalContainer.setOrientation(LinearLayout.HORIZONTAL);
        mHorizontalContainer.setLayoutParams(new LinearLayout.LayoutParams(
                width,
                mUsableHeight,
                1.f
        ));
        mHorizontalContainer.setBackgroundColor(Color.GREEN);

        // mSliderContainer is host to slider on the left
        mSliderContainer = (RelativeLayout) mHorizontalContainer.findViewById(R.id.SliderContainer);
        mSliderContainer.setBackgroundColor(Color.YELLOW);

        // mAnswerListContainer is host to vertical array of answer options
        mAnswerListContainer = (LinearLayout) mHorizontalContainer.
                findViewById(R.id.AnswerTextContainer);

        mAnswerListContainer.setOrientation(LinearLayout.VERTICAL);
        mAnswerListContainer.setBackgroundColor(Color.MAGENTA);
        mAnswerListContainer.setLayoutParams(new LinearLayout.LayoutParams(
                width-100,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.f
        ));

        mResizeView = mHorizontalContainer.findViewById(R.id.ResizeView);
        mRemainView = mHorizontalContainer.findViewById(R.id.RemainView);
    }

    public void buildSlider() {

        // Iterate over all options and create a TextView for each one
        for (int iAnswer = mListOfAnswers.size()-1; iAnswer >= 0; iAnswer--) {
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
        }
        mListOfIds.add(nAnswerId);
        return true;
    }

    public AnswerValues addClickListener(AnswerValues answerValues, int questionId) {

        mQuestionId = questionId;
        mAnswerValues = answerValues;

        // Handles default id if existent
        if (mDefaultAnswer == -1) {
            //answerValues.add(mListOfAnswers.get(mListOfAnswers.size() / 2).getId());
            answerValues.add(mQuestionId,(int) mListOfAnswers.size()/2);
            setProgressItem((int) mListOfAnswers.size()/2);
            //setProgressPixels((mHorizontalContainer.getLayoutParams().height)/2);
        } else {
            //answerIds.add(mListOfAnswers.get(mDefaultAnswer).getId());
            answerValues.add(mQuestionId,mDefaultAnswer);
            final TextView tvDefault = (TextView) mAnswerListContainer.findViewById(
                    mListOfAnswers.get(mDefaultAnswer).getId());
            tvDefault.post(new Runnable()
            {
                @Override
                public void run() {
                    setProgressPixels((int) (mHorizontalContainer.getLayoutParams().height*(
                            1 - 0.5/mListOfAnswers.size()) - tvDefault.getY()));
                }
            });
            mResizeView.getLayoutParams().height = (int) (tvDefault.getY());
            mResizeView.setLayoutParams(mResizeView.getLayoutParams());
        }

        // Enables clicking on option directly
        for (int iAnswer = mListOfAnswers.size()-1; iAnswer >= 0; iAnswer--) {
            final int numAnswer = iAnswer;
            final int currentId = mListOfAnswers.get(iAnswer).getId();
            TextView tv = (TextView) mAnswerListContainer.findViewById(currentId);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setProgressItem(numAnswer);
                    mAnswerValues.removeValueWithId(mQuestionId);
                    mAnswerValues.add(mQuestionId,numAnswer);
                    //answerIds.removeAll(mListOfIds);
                    //answerIds.add(currentId);
                }
            });
        }

        // Needed handle for estimation of maximum slider height
        final View remainView = mHorizontalContainer.findViewById(R.id.RemainView);
        remainView.post(new Runnable()
        {
            @Override
            public void run() {
                mSliderMaxHeight = remainView.getHeight();
            }
        });

        // Enables dragging of slider
        mResizeView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = MotionEventCompat.getActionMasked(event);

                switch(action) {
                    case (MotionEvent.ACTION_DOWN) :
                        Log.d("Motion","Action was DOWN");
                        return true;
                    case (MotionEvent.ACTION_MOVE) :
                        Log.d("Motion","Action was MOVE");
                        mAnswerValues = rescale(event, mAnswerValues);
                        return true;
                    case (MotionEvent.ACTION_UP) :
                        Log.d("Motion","Action was UP");
                        mAnswerValues = rescale(event, mAnswerValues);
                        return true;
                    case (MotionEvent.ACTION_CANCEL) :
                        Log.d("Motion","Action was CANCEL");
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
                        Log.d("Motion","Action was DOWN");
                        return true;
                    case (MotionEvent.ACTION_MOVE) :
                        Log.d("Motion","Action was MOVE");
                        mAnswerValues = rescale(event, mAnswerValues);
                        return true;
                    case (MotionEvent.ACTION_UP) :
                        Log.d("Motion","Action was UP");
                        mAnswerValues = rescale(event, mAnswerValues);
                        return true;
                    case (MotionEvent.ACTION_CANCEL) :
                        Log.d("Motion","Action was CANCEL");
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
        return mAnswerValues;
    }

    // Set progress  bar according to user input
    private AnswerValues rescale(MotionEvent motionEvent, AnswerValues answerValues) {
        mAnswerValues = answerValues;
        Log.i("re","scaling");
        // Clip selection to fixed items
        int numItem = clipValuesToElements(motionEvent.getRawY());
        try {
            setProgressItem(numItem);
            mAnswerValues.removeValueWithId(mQuestionId);
            mAnswerValues.add(mQuestionId,numItem);


            for (int iA = 0; iA<mAnswerValues.size(); iA++) {
                Log.i("AV",""+mAnswerValues.get(iA).getId());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return mAnswerValues;
    }

    // Enables rasterization of values (fixed choice options)
    private int clipValuesToElements(float inVal) {
        int numItem = (int) (mListOfAnswers.size()*(1 - inVal/mSliderMaxHeight)+2);
        return numItem;
    }

    // Set progress/slider based on percentage of total height
    public void setProgressPercent(float percent) {
        mResizeView.getLayoutParams().height =
                (int) (mSliderMaxHeight*percent/100);
        mResizeView.setLayoutParams(mResizeView.getLayoutParams());
    }

    // Set progress/slider based on fraction of whole set of options e.g. 1.f/7
    public void setProgressFraction(float fraction) {
        mResizeView.getLayoutParams().height =
                (int) (mSliderMaxHeight*(fraction-1.f/mListOfAnswers.size())+72);
        mResizeView.setLayoutParams(mResizeView.getLayoutParams());
    }

    // Set progress/slider according to exact pixel number
    public void setProgressPixels(float progress) {
        if (progress > getPixelHeightOfItem(mListOfAnswers.size()-1)) {
            progress = getPixelHeightOfItem(mListOfAnswers.size()-1);
        } else if (progress < 72) {
            progress = 72;
        }
        mResizeView.getLayoutParams().height = (int) (progress);
        mResizeView.setLayoutParams(mResizeView.getLayoutParams());
    }

    // Set progress/slider according to number of selected item (counting from 0)
    public void setProgressItem(int numItem) {
        if (numItem > mListOfAnswers.size()-1) {
            numItem = mListOfAnswers.size()-1;
        } else if (numItem < 0) {
            numItem = 0;
        }
        mResizeView.getLayoutParams().height =
                (int) (mSliderMaxHeight*(numItem)/mListOfAnswers.size()+72);
        mResizeView.setLayoutParams(mResizeView.getLayoutParams());
    }

    private int getPixelHeightOfItem(int numItem) {
        return (int) (mSliderMaxHeight*(numItem)/mListOfAnswers.size()+72);
    }
}



