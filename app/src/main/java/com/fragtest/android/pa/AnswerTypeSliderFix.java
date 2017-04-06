package com.fragtest.android.pa;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.etiennelawlor.discreteslider.library.ui.DiscreteSlider;

import java.util.ArrayList;
import java.util.List;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static android.os.Build.VERSION_CODES.M;

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
    private SeekBar mSeekBar;
    private LinearLayout mLayout, mCategoryLayout, mSeekBarMainLayout;

    //private VerticalSeekBar mSlider;


    public AnswerTypeSliderFix(Context context, AnswerLayout qParent) {

        mContext = context;
        parent = qParent;
        mListOfAnswers = new ArrayList<>();

        parent.scrollContent.setBackgroundColor(Color.YELLOW);
        parent.layoutAnswer.setBackgroundColor(Color.GREEN);
        mLayout = new LinearLayout(mContext);
        mLayout.setOrientation(LinearLayout.HORIZONTAL);
        mLayout.setMinimumHeight(700);

        mSeekBarMainLayout = new LinearLayout(mContext);
        mSeekBarMainLayout.setBackgroundColor(Color.MAGENTA);
        mSeekBarMainLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams seekBarMainLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                6
        );

        mSeekBar = new SeekBar(mContext);
        mSeekBar.setRotation(270);
        LinearLayout.LayoutParams seekBarParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        mCategoryLayout = new LinearLayout(mContext);
        mCategoryLayout.setBackgroundColor(Color.CYAN);
        mCategoryLayout.setPadding(0,0,0,0);
        mCategoryLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams categoryLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                4
        );

        mSeekBarMainLayout.addView(mSeekBar,seekBarParams);
        mLayout.addView(mSeekBarMainLayout,seekBarMainLayoutParams);
        mLayout.addView(mCategoryLayout,categoryLayoutParams);




    }


    public void buildSlider() {

        mSeekBar.setMax(mListOfAnswers.size()-1);
        /*
        mSeekBarLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                7));

        mTableLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                3));
*/
        parent.layoutAnswer.addView(mLayout);

    }

    public boolean addAnswer(int nAnswerID, String sAnswer) {
        mListOfAnswers.add(new StringAndInteger(sAnswer,nAnswerID));

        TextView textMark = new TextView(mContext);
        textMark.setPadding(0,0,0,0);
        textMark.setText(sAnswer);
        textMark.setTextSize(Units.getTextSizeAnswer());
        textMark.setTextColor(ContextCompat.getColor(mContext, R.color.TextColor));
        textMark.setLayoutParams(new LinearLayout.LayoutParams(
               LinearLayout.LayoutParams.WRAP_CONTENT,
               0,
               1));

        mCategoryLayout.addView(textMark);
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



