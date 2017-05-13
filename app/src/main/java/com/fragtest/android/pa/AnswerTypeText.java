package com.fragtest.android.pa;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * Created by ulrikkowalk on 17.02.17.
 */

public class AnswerTypeText extends AppCompatActivity {

    public EditText mAnswerText;
    public LinearLayout.LayoutParams answerParams;
    public AnswerLayout parent;
    private String LOG_STRING = "AnswerTypeText";
    private Button mButtonOkay;
    private Context mContext;
    private int mQuestionId;
    private Questionnaire mQuestionnaire;


    public AnswerTypeText(Context context, Questionnaire questionnaire, AnswerLayout qParent, int nQuestionId) {
        mContext = context;
        mQuestionId = nQuestionId;
        parent = qParent;
        mQuestionnaire = questionnaire;
    }

    public void buildView() {

        mAnswerText = new EditText(mContext);
        mAnswerText.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        mAnswerText.setTextSize(Units.getTextSizeAnswer());
        mAnswerText.setGravity(Gravity.START);
        mAnswerText.setTextColor(ContextCompat.getColor(mContext, R.color.TextColor));
        mAnswerText.setBackgroundColor(ContextCompat.getColor(mContext, R.color.BackgroundColor));
        mAnswerText.setHint(R.string.hintTextAnswer);
        mAnswerText.setHintTextColor(ContextCompat.getColor(mContext, R.color.JadeGray));

        // Parameters of Answer Button Layout
        answerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        answerParams.setMargins(
                (int) mContext.getResources().getDimension(R.dimen.answerTextMargin_Left),
                (int) mContext.getResources().getDimension(R.dimen.answerTextMargin_Top),
                (int) mContext.getResources().getDimension(R.dimen.answerTextMargin_Right),
                (int) mContext.getResources().getDimension(R.dimen.answerTextMargin_Bottom));

        mButtonOkay = new Button(mContext);
        mButtonOkay.setText(R.string.buttonTextOkay);
        mButtonOkay.setTextColor(ContextCompat.getColor(mContext, R.color.TextColor));
        mButtonOkay.setBackground(ContextCompat.getDrawable(mContext, R.drawable.button));
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        mAnswerText.isFocusableInTouchMode();

        parent.layoutAnswer.addView(mAnswerText, answerParams);
        parent.layoutAnswer.addView(mButtonOkay, buttonParams);
    }

    public boolean addClickListener() {

        mButtonOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check if no view has focus, then hide soft keyboard:
                View view = (View) mAnswerText;
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) mAnswerText.getContext().
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mAnswerText.getWindowToken(), 0);
                    mAnswerText.setCursorVisible(false);
                }

                String text = mAnswerText.getText().toString();
                if (text.length() != 0) {
                    mQuestionnaire.removeQuestionIdFromEvaluationList(mQuestionId);
                    // mEvaluationList.removeQuestionId(mQuestionId);
                    mQuestionnaire.addTextToEvaluationLst(mQuestionId, text);
                    //  mEvaluationList.add(mQuestionId, text);
                } else {
                    Log.e(LOG_STRING, "No text was entered.");
                }
            }
        });
        return true;
    }
}
