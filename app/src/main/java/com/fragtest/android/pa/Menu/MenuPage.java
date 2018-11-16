package com.fragtest.android.pa.Menu;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.MainActivity;
import com.fragtest.android.pa.Questionnaire.QuestionnairePagerAdapter;
import com.fragtest.android.pa.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ul1021 on 30.06.2017.
 */

public class MenuPage extends AppCompatActivity {

    private final static String LOG = "MenuPage";
    private String mCountDownString;
    private MainActivity mMainActivity;
    private QuestionnairePagerAdapter mContextQPA;
    private String StartText;
    private TextView mCountDownRemaining, mStartQuestionnaire, mDate, mConnecting;
    public TextView mDots;
    private String[] mTempTextCountDownRemaining;
    private SimpleDateFormat mDateFormat;
    private ArrayList<String> mErrorList;
    private ArrayAdapter<String> mErrorAdapter;
    private ListView mErrorView;
    public Handler mTaskHandler = new Handler();
    private boolean isTimePlausible = true;
    private boolean showErrors = true;

    private String[] mStringDots = {"   ", "•  ", "•• ", "•••"};
    private int iDot = 0;
    private int mDelayDots = 500;

    private Runnable mDotRunnable = new Runnable() {
        @Override
        public void run() {
            mDots.setText(mStringDots[iDot%4]);
            iDot++;
            mTaskHandler.postDelayed(mDotRunnable, mDelayDots);
        }
    };

    private Runnable mErrorViewRunnable = new Runnable() {
        @Override
        public void run() {
            if (showErrors) {
                mErrorView.setVisibility(View.VISIBLE);
            } else {
                mErrorView.setVisibility(View.INVISIBLE);
            }
            mTaskHandler.removeCallbacks(mErrorViewRunnable);
        }
    };

    private Runnable mDateViewRunnable = new Runnable() {
        @Override
        public void run() {
            if (isTimePlausible) {
                mDate.setVisibility(View.VISIBLE);
            } else {
                mDate.setVisibility(View.INVISIBLE);
            }
            mTaskHandler.removeCallbacks(mDateViewRunnable);
        }
    };

    public MenuPage(MainActivity context, QuestionnairePagerAdapter contextQPA) {
        mMainActivity = context;
        mContextQPA = contextQPA;
        StartText = "";
        mCountDownString = mMainActivity.getResources().getString(R.string.timeRemaining);
        mTempTextCountDownRemaining = mCountDownString.split("%");
        mDateFormat = new SimpleDateFormat("dd.MM.yy, HH:mm", Locale.ROOT);
        mErrorList = mMainActivity.mErrorList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void hideErrorList() {
        mErrorAdapter.notifyDataSetChanged();
        mErrorView.setVisibility(View.INVISIBLE);
    }

    public void showErrorList() {
        mErrorAdapter.notifyDataSetChanged();
        mErrorView.setVisibility(View.VISIBLE);
    }

    public void makeTextSizeNormal() {
        mStartQuestionnaire.setTextSize(mMainActivity.getResources().getDimension(R.dimen.textSizeMenu));
    }

    public void proposeQuestionnaire() {
        mStartQuestionnaire.setText(formatString(StartText));
        mStartQuestionnaire.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        mStartQuestionnaire.setTextSize(mMainActivity.getResources().
                getDimension(R.dimen.textSizeProposed));
        mStartQuestionnaire.setTypeface(null, Typeface.BOLD);
        mStartQuestionnaire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContextQPA.sendMessage(ControlService.MSG_PROPOSITION_ACCEPTED);
            }
        });
    }

    public void resetQuestionnaireCallback() {
        mStartQuestionnaire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContextQPA.sendMessage(ControlService.MSG_MANUAL_QUESTIONNAIRE);
            }
        });
    }

    public void clearQuestionnaireCallback() {
        mStartQuestionnaire.setOnClickListener(null);
    }

    public void showTime() {
        mDate.setVisibility(View.VISIBLE);
    }

    public void hideTime() {
        mDate.setVisibility(View.INVISIBLE);
    }

    public void showCountdownText() {
        mCountDownRemaining.setVisibility(View.VISIBLE);
    }

    public void hideCountdownText() {
        mCountDownRemaining.setVisibility(View.INVISIBLE);
    }

    public void makeFontWeightNormal() {
        mStartQuestionnaire.setTypeface(Typeface.DEFAULT);
    }

    public LinearLayout generateView() {

        // Parent Layout of the menu
        LinearLayout menuLayout = new LinearLayout(mMainActivity);
        menuLayout.setBackgroundColor(ContextCompat.getColor(mMainActivity,R.color.BackgroundColor));
        menuLayout.setOrientation(LinearLayout.VERTICAL);

        // Top View carrying countdown
        mCountDownRemaining = new TextView(mMainActivity);
        LinearLayout.LayoutParams tempTopParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 0.1f);
        mCountDownRemaining.setText(mCountDownString);
        mCountDownRemaining.setGravity(View.TEXT_ALIGNMENT_CENTER);
        mCountDownRemaining.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        mCountDownRemaining.setTextColor(ContextCompat.getColor(mMainActivity,R.color.TextColor_Light));
        mCountDownRemaining.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        // Layout patch carrying "Take Survey" Text/Button
        LinearLayout centerLayout = new LinearLayout(mMainActivity);
        centerLayout.setBackgroundColor(ContextCompat.getColor(mMainActivity,R.color.BackgroundColor));
        centerLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams centerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 0.7f
        );
        centerLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        // The actual Text/Button "Take Survey"
        mStartQuestionnaire = new TextView(mMainActivity);
        mStartQuestionnaire.setText("");
        mStartQuestionnaire.setTextSize(mMainActivity.getResources().getDimension(R.dimen.textSizeMenu));
        mStartQuestionnaire.setTypeface(Typeface.DEFAULT);
        mStartQuestionnaire.setTextColor(ContextCompat.getColor(mMainActivity, R.color.JadeRed));
        mStartQuestionnaire.setBackgroundColor(Color.WHITE);
        mStartQuestionnaire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContextQPA.sendMessage(ControlService.MSG_MANUAL_QUESTIONNAIRE);
            }
        });

        // Is displayed during "Connecting" Stage INSTEAD OF mStartQuestionnaire
        mConnecting = new TextView(mMainActivity);
        mConnecting.setVisibility(View.GONE);
        mConnecting.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        mConnecting.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        mConnecting.setText(mMainActivity.getResources().getString(R.string.infoConnecting));
        mConnecting.setTextSize(mMainActivity.getResources().getDimension(R.dimen.textSizeAnswer));
        mConnecting.setTypeface(Typeface.DEFAULT);
        mConnecting.setTextColor(ContextCompat.getColor(mMainActivity, R.color.JadeRed));
        mConnecting.setBackgroundColor(Color.WHITE);

        mDots = new TextView(mMainActivity);
        mDots.setVisibility(View.GONE);
        mDots.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        mDots.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        mDots.setText(mMainActivity.getResources().getString(R.string.infoConnecting));
        mDots.setTextSize(mMainActivity.getResources().getDimension(R.dimen.textSizeAnswer));
        mDots.setTypeface(Typeface.DEFAULT_BOLD);
        mDots.setTextColor(ContextCompat.getColor(mMainActivity, R.color.JadeRed));
        mDots.setBackgroundColor(Color.WHITE);

        // Error View
        mErrorView = new ListView(mMainActivity);
        mErrorAdapter = new ArrayAdapter<>(
                mMainActivity,
                android.R.layout.simple_list_item_1,
                mErrorList );
        LinearLayout.LayoutParams tempErrorParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 0.3f);
        mErrorView.setAdapter(mErrorAdapter);
        mErrorView.setOnItemClickListener(null);
        mErrorView.setDividerHeight(0);
        mErrorView.setVisibility(View.VISIBLE);

        mDate = new TextView(mMainActivity);
        mDate.setVisibility(View.VISIBLE);
        mDate.setText("DD.MM.YY, HH:MM");
        mDate.setTextColor(ContextCompat.getColor(mMainActivity, R.color.JadeGray));
        mDate.setTextSize(mMainActivity.getResources().getDimension(R.dimen.textSizeAnswer));
        mDate.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        centerLayout.addView(mStartQuestionnaire);
        centerLayout.addView(mConnecting);
        centerLayout.addView(mDots);
        menuLayout.addView(mCountDownRemaining, tempTopParams);
        menuLayout.addView(centerLayout, centerParams);
        menuLayout.addView(mErrorView, tempErrorParams);
        menuLayout.addView(mDate, tempTopParams);

        return menuLayout;
    }

    public void setTime() {
        Calendar dateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        mDate.setText(mDateFormat.format(dateTime.getTime()));
    }

    public void setText(String text) {
        StartText = text;
        mStartQuestionnaire.setText(cleanUpString(text));
        mStartQuestionnaire.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    }

    public void setText() {
        mStartQuestionnaire.setText(mCountDownString);
    }

    public void resetStartTextSize() {
        mStartQuestionnaire.setTextSize(mMainActivity.getResources().
                getDimension(R.dimen.textSizeAnswer));
        mStartQuestionnaire.setTypeface(Typeface.DEFAULT);
    }

    // Handles update of visible text countdown
    public void updateCountdownText(int seconds) {
        int hoursRemaining = seconds / 60 / 60;
        int minutesRemaining = (seconds  - hoursRemaining * 60 * 60) / 60;
        int secondsRemaining = seconds - hoursRemaining * 60 * 60 - minutesRemaining * 60;

        String tmp_text = String.format("%s%d%s%d%s%d%s",
                mTempTextCountDownRemaining[0], hoursRemaining,
                mTempTextCountDownRemaining[1], minutesRemaining,
                mTempTextCountDownRemaining[2], secondsRemaining,
                mTempTextCountDownRemaining[3]);

        mCountDownRemaining.setText(tmp_text);
    }

    private String cleanUpString(String inString) {
        return inString.replace("-","");
    }

    private String formatString(String inString) {
        return inString.replace(" ","\n").replace("-","-\n");
    }

    public void updateCountDownText(String text) {
        mCountDownRemaining.setText(text);
    }
}