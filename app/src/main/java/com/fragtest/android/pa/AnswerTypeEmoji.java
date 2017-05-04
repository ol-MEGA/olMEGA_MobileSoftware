package com.fragtest.android.pa;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.id;

/**
 * Created by ulrikkowalk on 17.02.17.
 */

public class AnswerTypeEmoji extends AppCompatActivity {

    private Context mContext;
    private AnswerLayout mParent;
    private List<StringAndInteger> mListOfAnswers;
    private List<Integer> mListOfIds;
    private int[] drawables = new int[5];
    private int[] drawables_pressed = new int[5];
    private int mDefault = -1;
    private Questionnaire mQuestionnaire;

    public AnswerTypeEmoji(Context context, QuestionnairePagerAdapter contextQPA,
                           AnswerLayout qParent) {

        mContext = context;
        mParent = qParent;
        mListOfAnswers = new ArrayList<>();
        mListOfIds = new ArrayList<>();
        mQuestionnaire = new Questionnaire(mContext, contextQPA);

        drawables[0] = R.drawable.em1of5;
        drawables[1] = R.drawable.em2of5;
        drawables[2] = R.drawable.em3of5;
        drawables[3] = R.drawable.em4of5;
        drawables[4] = R.drawable.em5of5;

        drawables_pressed[0] = R.drawable.em1of5_active;
        drawables_pressed[1] = R.drawable.em2of5_active;
        drawables_pressed[2] = R.drawable.em3of5_active;
        drawables_pressed[3] = R.drawable.em4of5_active;
        drawables_pressed[4] = R.drawable.em5of5_active;
    }

    public boolean addAnswer(int nId, String sAnswer, boolean isDefault) {
        mListOfAnswers.add(new StringAndInteger(sAnswer,nId));
        mListOfIds.add(nId);
        if (isDefault) {
            mDefault = mListOfAnswers.size()-1;
        }
        return true;
    }

    public AnswerIds buildView(final AnswerIds answerIds) {

        int usableHeight = (new Units(mContext)).getUsableSliderHeight();
        int numEmojis = mListOfAnswers.size();
        int emojiSize = (int) (usableHeight / (1.2f*numEmojis));

        for (int iAnswer = 0; iAnswer < mListOfAnswers.size(); iAnswer++) {

            final Button answerButton = new Button(mContext);
            answerButton.setLayoutParams(new LinearLayout.LayoutParams(
                    emojiSize,
                    emojiSize,
                    1.0f));

            String sAnswer = mListOfAnswers.get(iAnswer).getText();
            switch(sAnswer){
                case "emoji1of5":
                    answerButton.setBackground(ContextCompat.getDrawable(mContext, drawables[0]));
                    answerButton.setTag(0);
                    break;
                case "emoji2of5":
                    answerButton.setBackground(ContextCompat.getDrawable(mContext, drawables[1]));
                    answerButton.setTag(1);
                    break;
                case "emoji3of5":
                    answerButton.setBackground(ContextCompat.getDrawable(mContext, drawables[2]));
                    answerButton.setTag(2);
                    break;
                case "emoji4of5":
                    answerButton.setBackground(ContextCompat.getDrawable(mContext, drawables[3]));
                    answerButton.setTag(3);
                    break;
                case "emoji5of5":
                    answerButton.setBackground(ContextCompat.getDrawable(mContext, drawables[4]));
                    answerButton.setTag(4);
                    break;
                default:
                    break;
            }

            if (iAnswer == mDefault) {
                setChecked(true, answerButton);
                answerIds.add(mListOfAnswers.get(iAnswer).getId());
            } else {
                setChecked(false, answerButton);
            }
            answerButton.setId(mListOfAnswers.get(iAnswer).getId());

            mParent.layoutAnswer.addView(answerButton);

            // Placeholder View because padding has no effect
            View placeHolder = new View(mContext);
            placeHolder.setBackgroundColor(ContextCompat.getColor(
                    mContext, R.color.BackgroundColor));
            placeHolder.setLayoutParams(new LinearLayout.LayoutParams(
                    (int) (0.2*emojiSize),
                    (int) (0.2*emojiSize),
                    1.0f
            ));
            mParent.layoutAnswer.addView(placeHolder);
        }
        return answerIds;
    }

    public AnswerIds addClickListener(final AnswerIds answerIds) {

        for (int iAnswer = 0; iAnswer < mListOfAnswers.size(); iAnswer++) {
            final Button button = (Button) mParent.layoutAnswer.findViewById(
                    mListOfAnswers.get(iAnswer).getId());
            final int currentAnswer = iAnswer;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int iButton = 0; iButton < mListOfAnswers.size(); iButton++) {
                        final Button button = (Button) mParent.layoutAnswer.findViewById(
                                mListOfAnswers.get(iButton).getId());
                        if (iButton == currentAnswer) {
                            setChecked(true, button);
                        } else {
                            setChecked(false, button);
                        }
                    }
                    answerIds.removeAll(mListOfIds);
                    answerIds.add(mListOfAnswers.get(currentAnswer).getId());

                    mQuestionnaire.mAnswerIds = answerIds;
                    mQuestionnaire.checkVisibility();
                }
            });
        }
        return answerIds;
    }

    public void setChecked(boolean isChecked, Button answerButton) {
        if (isChecked) {
            answerButton.setBackground(ContextCompat.getDrawable(mContext,
                    drawables_pressed[(int) answerButton.getTag()]));
        } else {
            answerButton.setBackground(ContextCompat.getDrawable(mContext,
                    drawables[(int) answerButton.getTag()]));
        }
    }

    public void toggleChecked(Button answerButton) {
        if (answerButton.isPressed()) {
            answerButton.setBackground(ContextCompat.getDrawable(mContext,
                    drawables_pressed[(int) answerButton.getTag()]));
        } else {
            answerButton.setBackground(ContextCompat.getDrawable(mContext,
                    drawables[(int) answerButton.getTag()]));
        }
    }
}