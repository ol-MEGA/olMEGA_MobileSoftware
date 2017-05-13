package com.fragtest.android.pa;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by ulrikkowalk on 17.02.17.
 */

public class AnswerTypeFinish extends AppCompatActivity {

    public Button mAnswerButton;
    public LinearLayout.LayoutParams answerParams;
    public AnswerLayout parent;
    private int mAnswerId;
    private Context mContext;
    private FileIO fileIO;
    private EvaluationList mEvaluationList;

    public AnswerTypeFinish(Context context, AnswerLayout qParent) {

        mContext = context;
        parent = qParent;

        mAnswerButton = new Button(context);
        mAnswerButton.setText(R.string.buttonTextFinish);
        mAnswerButton.setTextSize(Units.getTextSizeAnswer());
        mAnswerButton.setGravity(Gravity.CENTER_HORIZONTAL);
        mAnswerButton.setTextColor(ContextCompat.getColor(context, R.color.TextColor));
        mAnswerButton.setBackgroundColor(ContextCompat.getColor(context, R.color.BackgroundColor));
        mAnswerButton.setAllCaps(false);
        mAnswerButton.setTypeface(null, Typeface.NORMAL);
        mAnswerButton.setBackgroundResource(R.drawable.button);

        // Parameters of Answer Button
        answerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        answerParams.setMargins(
                (int) mContext.getResources().getDimension(R.dimen.answerFinishMargin_Left),
                (int) mContext.getResources().getDimension(R.dimen.answerFinishMargin_Top),
                (int) mContext.getResources().getDimension(R.dimen.answerFinishMargin_Right),
                (int) mContext.getResources().getDimension(R.dimen.answerFinishMargin_Bottom));
    }

    public boolean addAnswer() {
        parent.layoutAnswer.addView(mAnswerButton, answerParams);
        return true;
    }

    public void addClickListener(final Context context, final MetaData metaData,
                                 EvaluationList evaluationList) {
        mEvaluationList = evaluationList;
        mAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //metaData.finalise(mEvaluationList);
                fileIO = new FileIO();
                fileIO.saveDataToFile(mContext, metaData.getFileName(), metaData.getData());
                //Toast.makeText(mContext,R.string.infoTextSave,Toast.LENGTH_SHORT).show();
                ((Activity)context).finish();

            }
        });
    }
}
