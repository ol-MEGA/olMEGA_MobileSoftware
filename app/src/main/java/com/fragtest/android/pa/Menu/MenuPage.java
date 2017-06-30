package com.fragtest.android.pa.Menu;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fragtest.android.pa.Core.FileIO;
import com.fragtest.android.pa.Core.Units;
import com.fragtest.android.pa.MainActivity;
import com.fragtest.android.pa.Questionnaire.QuestionnairePagerAdapter;
import com.fragtest.android.pa.R;

/**
 * Created by ul1021 on 30.06.2017.
 */

public class MenuPage extends AppCompatActivity {

    private String LOG_STRING = "MenuPage";
    private String timeString;
    private Context mContext;
    private MainActivity mMainActivity;
    private QuestionnairePagerAdapter mContextQPA;
    private FileIO mFileIO;
    private Resources mResources;
    private String questFileName;
    private TextView mTimeRemaining;
    private String[] tempTimeRemaining;

    public MenuPage(Context context, QuestionnairePagerAdapter contextQPA) {
        mContext = context;
        mMainActivity = (MainActivity) context;
        mContextQPA = contextQPA;
        mFileIO = new FileIO();
        //questFileName = mResources.getResourceName(mContext,R.raw.question_short_eng);
        questFileName = "Start Questionnaire";
        timeString = mContext.getResources().getString(R.string.timeRemaining);
        tempTimeRemaining = timeString.split("%");
        mTimeRemaining = new TextView(mContext);
    }

    public void setUp() {
        //mRawInput = mFileIO.readRawTextFile();
        // offline version
        String mRawInput = mFileIO.readRawTextFile(mContext, R.raw.question_short_eng);
        String[] mTimerTemp = mRawInput.split("<timer>|</timer>");

    }

    public LinearLayout generateView() {
        LinearLayout menuLayout = new LinearLayout(mContext);

        mTimeRemaining = new TextView(mContext);
        LinearLayout.LayoutParams tempTopParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,1f);
        mTimeRemaining.setText(timeString);
        mTimeRemaining.setGravity(View.TEXT_ALIGNMENT_CENTER);
        mTimeRemaining.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        View tempViewBottom = new View(mContext);

        TextView textViewTitle = new TextView(mContext);
        textViewTitle.setText(questFileName);
        textViewTitle.setTextSize(mContext.getResources().getDimension(R.dimen.textSizeAnswer));
        textViewTitle.setTextColor(ContextCompat.getColor(mContext,R.color.JadeRed));
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,1f
        );
        //textViewTitle.setLayoutParams(textParams);
        textViewTitle.setGravity(View.TEXT_ALIGNMENT_CENTER);
        textViewTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        textViewTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContextQPA.createQuestionnaire();
            }
        });

        menuLayout.addView(mTimeRemaining, tempTopParams);
        menuLayout.addView(textViewTitle, textParams);
        menuLayout.addView(tempViewBottom, tempTopParams);

        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menuLayout.setHorizontalGravity(View.TEXT_ALIGNMENT_CENTER);
        menuLayout.setGravity(View.TEXT_ALIGNMENT_CENTER);

        int height = (new Units(mContext)).getUsableSliderHeight();
        // Roughly okay, refine later
        mTimeRemaining.setPadding(0,height/5,0,height/6);
        textViewTitle.setPadding(0,height/6,0,height/6);

        return menuLayout;
    }

    public void updateTime(int seconds) {

        int minutesRemaining = seconds/60;
        int secondsRemaining = seconds - minutesRemaining*60;
        mTimeRemaining.setText(""+tempTimeRemaining[0]+minutesRemaining+tempTimeRemaining[1]+
            secondsRemaining+tempTimeRemaining[2]);
    }
}
