package com.fragtest.android.pa.Menu;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.Units;
import com.fragtest.android.pa.Questionnaire.QuestionnairePagerAdapter;
import com.fragtest.android.pa.R;

/**
 * Created by ul1021 on 30.06.2017.
 */

public class MenuPage extends AppCompatActivity {

    private String LOG_STRING = "MenuPage";
    private String mCountDownString;
    private Context mContext;
    private QuestionnairePagerAdapter mContextQPA;
    private String questFileName;
    private TextView mCountDownRemaining;
    private String[] mTempTextCountDownRemaining;

    public MenuPage(Context context, QuestionnairePagerAdapter contextQPA) {

        mContext = context;
        mContextQPA = contextQPA;
        questFileName = "Start Questionnaire";
        mCountDownString = mContext.getResources().getString(R.string.timeRemaining);
        mTempTextCountDownRemaining = mCountDownString.split("%");
        mCountDownRemaining = new TextView(mContext);

    }

    public LinearLayout generateView() {

        LinearLayout menuLayout = new LinearLayout(mContext);

        mCountDownRemaining = new TextView(mContext);
        LinearLayout.LayoutParams tempTopParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f);
        mCountDownRemaining.setText(mCountDownString);
        mCountDownRemaining.setGravity(View.TEXT_ALIGNMENT_CENTER);
        mCountDownRemaining.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        View tempViewBottom = new View(mContext);

        TextView textViewTitle = new TextView(mContext);
        textViewTitle.setText(questFileName);
        textViewTitle.setTextSize(mContext.getResources().getDimension(R.dimen.textSizeAnswer));
        textViewTitle.setTextColor(ContextCompat.getColor(mContext, R.color.JadeRed));
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f
        );
        textViewTitle.setGravity(View.TEXT_ALIGNMENT_CENTER);
        textViewTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        textViewTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContextQPA.sendMessage(ControlService.MSG_ARE_WE_RUNNING);
            }
        });

        menuLayout.addView(mCountDownRemaining, tempTopParams);
        menuLayout.addView(textViewTitle, textParams);
        menuLayout.addView(tempViewBottom, tempTopParams);

        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menuLayout.setHorizontalGravity(View.TEXT_ALIGNMENT_CENTER);
        menuLayout.setGravity(View.TEXT_ALIGNMENT_CENTER);

        int height = (new Units(mContext)).getUsableSliderHeight();
        // Roughly okay, refine later
        mCountDownRemaining.setPadding(0, height / 5, 0, height / 6);
        textViewTitle.setPadding(0, height / 6, 0, height / 6);

        return menuLayout;
    }

    public void updateCountdownText(int seconds) {
        // Handles update of visible text countdown
        int minutesRemaining = seconds / 60;
        int secondsRemaining = seconds - minutesRemaining * 60;
        mCountDownRemaining.setText("" + mTempTextCountDownRemaining[0] + minutesRemaining +
                mTempTextCountDownRemaining[1] + secondsRemaining + mTempTextCountDownRemaining[2]);
    }
}