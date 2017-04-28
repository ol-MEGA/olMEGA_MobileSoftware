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

/**
 * Created by ulrikkowalk on 04.04.17.
 */

public class AnswerTypeSliderFix extends AppCompatActivity {

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
    private int width, mUsableHeight;

    public AnswerTypeSliderFix(Context context, AnswerLayout qParent) {

        mContext = context;
        parent = qParent;
        mListOfAnswers = new ArrayList<>();

        LayoutInflater inflater = LayoutInflater.from(context);

        //Display display = getWindowManager().getDefaultDisplay();
        //Point size = new Point();
        //display.getSize(size);
        width = Units.getScreenWidth();


        //parent.scrollContent.setBackgroundColor(Color.CYAN);
        parent.scrollContent.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.f
        ));

        mUsableHeight = (new Units(mContext)).getUsableSliderHeight();

        //parent.scrollContent.getLayoutParams().height = usableHeight;
        //parent.scrollContent.setLayoutParams(parent.scrollContent.getLayoutParams());

        mHorizontalContainer = (LinearLayout) inflater.inflate(
                R.layout.answer_type_slider, parent.scrollContent, false);
        mHorizontalContainer.setOrientation(LinearLayout.HORIZONTAL);
        mHorizontalContainer.setLayoutParams(new LinearLayout.LayoutParams(
                width,
                mUsableHeight,
                1.f
        ));
        mHorizontalContainer.setBackgroundColor(Color.GREEN);






        mSliderContainer = (RelativeLayout) mHorizontalContainer.findViewById(R.id.SliderContainer);
        mSliderContainer.setBackgroundColor(Color.YELLOW);
        /*
        mSliderContainer.setLayoutParams(new RelativeLayout.LayoutParams(
                100,
                usableHeight
        ));
        */







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

    public AnswerIds buildSlider(AnswerIds answerIds) {

        for (int iAnswer = mListOfAnswers.size()-1; iAnswer >= 0; iAnswer--) {
            TextView textMark = new TextView(mContext);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
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

        Log.i(CLASS_NAME,"Slider successfully built.");



        return answerIds;
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

    public AnswerIds addIdFromProgress(int progress, AnswerIds answerIds) {
        answerIds.removeAll(mListOfIds);
        answerIds.add(mListOfAnswers.get(progress).getId());
        return answerIds;
    }

    public AnswerIds addClickListener(final AnswerIds answerIds) {

        if (mDefaultAnswer == -1) {
            answerIds.add(mListOfAnswers.get(mListOfAnswers.size() / 2).getId());
            setProgressPixels((mHorizontalContainer.getLayoutParams().height)/2);
            Log.e("NO","default not found");
        } else {
            answerIds.add(mListOfAnswers.get(mDefaultAnswer).getId());
            final TextView tvDefault = (TextView) mAnswerListContainer.findViewById(
                    mListOfAnswers.get(mDefaultAnswer).getId());
            tvDefault.post(new Runnable()
            {
                @Override
                public void run() {
                    Log.d("HEIGHT", "Height = "+tvDefault.getY());
                    setProgressPixels((int) (mHorizontalContainer.getLayoutParams().height*(
                            1 - 0.5/mListOfAnswers.size()) - tvDefault.getY()));
                }
            });
            mResizeView.getLayoutParams().height = (int) (tvDefault.getY());
            mResizeView.setLayoutParams(mResizeView.getLayoutParams());
        }

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
                        rescale(event, answerIds);
                        return true;
                    case (MotionEvent.ACTION_UP) :
                        Log.d("Motion","Action was UP");
                        rescale(event, answerIds);
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
                        rescale(event, answerIds);
                        return true;
                    case (MotionEvent.ACTION_UP) :
                        Log.d("Motion","Action was UP");
                        rescale(event, answerIds);
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
        return answerIds;
    }

    private AnswerIds rescale(MotionEvent motionEvent, AnswerIds answerIds) {
        float yPos = motionEvent.getRawY();

        try {
            setProgressPixels((int) (Units.getScreenHeight() - 48 - yPos));
        } catch (Exception e) {
            e.printStackTrace();
        }
    return answerIds;
    }

    public void setProgressPercent(int progress) {
        mResizeView.getLayoutParams().height = (int) (Units.getScreenHeight()/100*progress);
        mResizeView.setLayoutParams(mResizeView.getLayoutParams());
    }

    public void setProgressPixels(int progress) {
        if (progress > mHorizontalContainer.getLayoutParams().height) {
            progress = mHorizontalContainer.getLayoutParams().height;
        } else if (progress < 0) {
            progress = 0;
        }
        Log.e("Progress",""+progress);
        mResizeView.getLayoutParams().height = (int) (progress);
        mResizeView.setLayoutParams(mResizeView.getLayoutParams());
    }

    public void setProgressItem(int item) {

    }
    /*
    public void synchronizeThumb() {
        mSeekBar.setProgress(mSeekBar.getProgress());
        mSeekBar.setThumbOffset(0);
    }
    */
}



