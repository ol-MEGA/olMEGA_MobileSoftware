package com.fragtest.android.pa.Questionnaire;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.fragtest.android.pa.DataTypes.StringAndInteger;
import com.fragtest.android.pa.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ulrikkowalk on 17.02.17.
 */

public class AnswerTypeCheckBox extends AppCompatActivity {

    public final AnswerLayout mParent;
    private final Context mContext;
    private final int mQuestionId;
    private final List<StringAndInteger> mListOfAnswers;
    private final List<Integer> mListOfDefaults;
    private final Questionnaire mQuestionnaire;
    public LinearLayout.LayoutParams answerParams;

    public AnswerTypeCheckBox(Context context, Questionnaire questionnaire, AnswerLayout qParent, int nQuestionId) {

        mContext = context;
        mParent = qParent;
        mQuestionId = nQuestionId;
        mQuestionnaire = questionnaire;
        mListOfDefaults = new ArrayList<>();
        mListOfAnswers = new ArrayList<>();

    }

    public boolean addAnswer(int nAnswerId, String sAnswer, boolean isDefault) {
        mListOfAnswers.add(new StringAndInteger(sAnswer, nAnswerId));
        if (isDefault) {
            mListOfDefaults.add(mListOfAnswers.size() - 1);
        }
        return true;
    }

    public void buildView() {

        for (int iAnswer = 0; iAnswer < mListOfAnswers.size(); iAnswer++) {

            int currentId = mListOfAnswers.get(iAnswer).getId();
            String currentString = mListOfAnswers.get(iAnswer).getText();

            CheckBox checkBox = new CheckBox(mContext);
            checkBox.setId(currentId);
            checkBox.setText(currentString);
            checkBox.setTextSize(mContext.getResources().getDimension(R.dimen.textSizeAnswer));
            checkBox.setChecked(false);
            checkBox.setGravity(Gravity.START);
            checkBox.setPadding(
                    (int) mContext.getResources().getDimension(R.dimen.answerTypeCheckBoxPadding_Left),
                    (int) mContext.getResources().getDimension(R.dimen.answerTypeCheckBoxPadding_Top),
                    (int) mContext.getResources().getDimension(R.dimen.answerTypeCheckBoxPadding_Right),
                    (int) mContext.getResources().getDimension(R.dimen.answerTypeCheckBoxPadding_Bottom)
            );
            checkBox.setTextColor(ContextCompat.getColor(mContext, R.color.TextColor));
            checkBox.setBackgroundColor(ContextCompat.getColor(mContext, R.color.BackgroundColor));
            int states[][] = {{android.R.attr.state_checked}, {}};
            int colors[] = {ContextCompat.getColor(mContext, R.color.JadeRed),
                    ContextCompat.getColor(mContext, R.color.JadeRed)};
            CompoundButtonCompat.setButtonTintList(checkBox, new ColorStateList(states, colors));

            // Parameters of Answer Button
            answerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            checkBox.setMinHeight((int) mContext.getResources().getDimension(R.dimen.textSizeAnswer));

            mParent.layoutAnswer.addView(checkBox, answerParams);
        }
    }

    public boolean addClickListener() {

        for (int iAnswer = 0; iAnswer < mListOfAnswers.size(); iAnswer++) {

            final int currentId = mListOfAnswers.get(iAnswer).getId();
            final CheckBox checkBox = (CheckBox) mParent.layoutAnswer.findViewById(currentId);

            if (mListOfDefaults.contains(currentId)) {
                checkBox.setChecked(true);
                mQuestionnaire.addIdToEvaluationList(mQuestionId, currentId);
            }

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (checkBox.isChecked()) {
                        mQuestionnaire.addIdToEvaluationList(mQuestionId, currentId);
                    } else {
                        mQuestionnaire.removeIdFromEvaluationList(currentId);
                    }
                    mQuestionnaire.checkVisibility();
                }
            });
        }
        return true;
    }

}
