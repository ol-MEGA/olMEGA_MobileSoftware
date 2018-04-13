package com.fragtest.android.pa.Menu;

import android.content.Context;
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
    private Context mContext;
    private QuestionnairePagerAdapter mContextQPA;
    private String StartText;
    private TextView mCountDownRemaining, mStartQuestionnaire, mDate, mConnecting, mDots;
    private String[] mTempTextCountDownRemaining;
    private SimpleDateFormat mDateFormat;
    private ArrayList<String> mErrorList = new ArrayList<>();
    private ArrayAdapter<String> mErrorAdapter;
    private ListView mErrorView;
    private boolean isCharging = true;
    private String mOriginalText = "";
    private Handler mTaskHandler = new Handler();
    private int mConnectingDelay = 90*1000;
    private boolean isTimePlausible = true;

    private String[] mStringDots = {"   ", "•  ", "•• ", "•••"};
    private int iDot = 0;
    private int mDelayDots = 500;

    public final int ERROR_NOBT = 0;
    public final int ERROR_NOQUEST = 1;
    public final int ERROR_BATT = 2;
    public final int ERROR_BATT_CRIT = 3;
    public final int ERROR_BATT_CHARGING = 4;

    private Runnable mDotRunnable = new Runnable() {
        @Override
        public void run() {
            mDots.setText(mStringDots[iDot%4]);
            iDot++;
            mTaskHandler.postDelayed(mDotRunnable, mDelayDots);
        }
    };

    private Runnable mConnectingRunnable = new Runnable() {
        @Override
        public void run() {
            stopConnecting();
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

    public MenuPage(Context context, QuestionnairePagerAdapter contextQPA) {
        mContext = context;
        mContextQPA = contextQPA;
        StartText = "";
        mCountDownString = mContext.getResources().getString(R.string.timeRemaining);
        mTempTextCountDownRemaining = mCountDownString.split("%");
        mDateFormat = new SimpleDateFormat("dd.MM.yy, HH:mm", Locale.ROOT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopConnecting();
        mTaskHandler.removeCallbacks(mConnectingRunnable);
    }

    private String getMessage(int error) {
        switch(error) {
            case ERROR_BATT:
                return mContext.getResources().getString(R.string.batteryWarning);
            case ERROR_NOBT:
                return mContext.getResources().getString(R.string.noBluetooth);
            case ERROR_NOQUEST:
                return mContext.getResources().getString(R.string.noQuestionnaires);
            case ERROR_BATT_CRIT:
                return mContext.getResources().getString(R.string.batteryCritical);
            case ERROR_BATT_CHARGING:
                return mContext.getResources().getString(R.string.infoCharging);
            default:
                return null;
        }
    }

    private void reactToErrors() {
        if (mErrorList.contains(getMessage(ERROR_NOBT)) ||
                mErrorList.contains(getMessage(ERROR_NOQUEST)) ||
                mErrorList.contains(getMessage(ERROR_BATT_CRIT))) {
            disableQuestionnaire();
        } else {
            enableQuestionnaire();
            stopConnecting();
        }
    }

    public void addError(int error) {
        if (!mErrorList.contains(getMessage(error))) {
            mErrorList.add(getMessage(error));
            mErrorAdapter.notifyDataSetChanged();
        }
        reactToErrors();
    }

    public void removeError(int error) {
        if (mErrorList.contains(getMessage(error))) {
            mErrorList.remove(getMessage(error));
            mErrorAdapter.notifyDataSetChanged();
        }
        reactToErrors();
    }

    public void rehashErrors() {
        mErrorAdapter.notifyDataSetChanged();
    }

    private void enableQuestionnaire() {
        setText(mContext.getResources().getString(R.string.menuText));
        mStartQuestionnaire.setEnabled(true);
    }

    private void disableQuestionnaire() {
        if (!isCharging) {
            setText(mContext.getResources().getString(R.string.infoError));
            mStartQuestionnaire.setEnabled(false);
            mCountDownRemaining.setText("");
        }
        resetStartTextSize();
    }

    public void startConnecting() {
        if (mErrorList.contains(getMessage(ERROR_NOBT)) && !mErrorList.contains(getMessage(ERROR_BATT_CRIT))) {
            mStartQuestionnaire.setVisibility(View.GONE);
            mConnecting.setVisibility(View.VISIBLE);
            mDots.setVisibility(View.VISIBLE);
            mErrorView.setVisibility(View.INVISIBLE);
            mTaskHandler.postDelayed(mConnectingRunnable, mConnectingDelay);
            mTaskHandler.post(mDotRunnable);
        }
    }

    public void stopConnecting() {
        mConnecting.setVisibility(View.GONE);
        mDots.setVisibility(View.GONE);
        mStartQuestionnaire.setVisibility(View.VISIBLE);
        mTaskHandler.removeCallbacks(mConnectingRunnable);
        mTaskHandler.removeCallbacks(mDotRunnable);
        mErrorView.setVisibility(View.VISIBLE);
    }

    public void setNotCharging() {
        isCharging = false;
        resetStartTextSize();
        reactToErrors();
        startConnecting();
    }

    public void setCharging() {
        isCharging = true;
        stopConnecting();
        setText(mContext.getResources().getString(R.string.infoCharging));
        mStartQuestionnaire.setEnabled(false);
        resetStartTextSize();
        mCountDownRemaining.setText("");
        mErrorView.setVisibility(View.INVISIBLE);
    }

    public void setTimePlausible(boolean isPlausible) {
        isTimePlausible = isPlausible;
        mTaskHandler.post(mDateViewRunnable);
    }

    public LinearLayout generateView() {

        // Parent Layout of the menu
        LinearLayout menuLayout = new LinearLayout(mContext);
        menuLayout.setBackgroundColor(ContextCompat.getColor(mContext,R.color.BackgroundColor));
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        //menuLayout.setBackgroundColor(Color.GREEN);

        // Top View carrying countdown
        mCountDownRemaining = new TextView(mContext);
        LinearLayout.LayoutParams tempTopParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 0.1f);
        mCountDownRemaining.setText(mCountDownString);
        mCountDownRemaining.setGravity(View.TEXT_ALIGNMENT_CENTER);
        mCountDownRemaining.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        mCountDownRemaining.setTextColor(ContextCompat.getColor(mContext,R.color.TextColor_Light));
        mCountDownRemaining.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        // Layout patch carrying "Take Survey" Text/Button
        LinearLayout centerLayout = new LinearLayout(mContext);
        centerLayout.setBackgroundColor(ContextCompat.getColor(mContext,R.color.BackgroundColor));
        centerLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams centerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 0.7f
        );
        centerLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        // The actual Text/Button "Take Survey"
        mStartQuestionnaire = new TextView(mContext);
        mStartQuestionnaire.setText("");
        mStartQuestionnaire.setTextSize(mContext.getResources().getDimension(R.dimen.textSizeAnswer));
        mStartQuestionnaire.setTypeface(Typeface.DEFAULT);
        mStartQuestionnaire.setTextColor(ContextCompat.getColor(mContext, R.color.JadeRed));
        mStartQuestionnaire.setBackgroundColor(Color.WHITE);
        mStartQuestionnaire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetStartTextSize();
                mContextQPA.sendMessage(ControlService.MSG_MANUAL_QUESTIONNAIRE);
            }
        });

        // Is displayed during "Connecting" Stage INSTEAD OF mStartQuestionnaire
        mConnecting = new TextView(mContext);
        mConnecting.setVisibility(View.GONE);
        mConnecting.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        mConnecting.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        mConnecting.setText(mContext.getResources().getString(R.string.infoConnecting));
        mConnecting.setTextSize(mContext.getResources().getDimension(R.dimen.textSizeAnswer));
        mConnecting.setTypeface(Typeface.DEFAULT);
        mConnecting.setTextColor(ContextCompat.getColor(mContext, R.color.JadeRed));
        mConnecting.setBackgroundColor(Color.WHITE);

        mDots = new TextView(mContext);
        mDots.setVisibility(View.GONE);
        mDots.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        mDots.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        mDots.setText(mContext.getResources().getString(R.string.infoConnecting));
        mDots.setTextSize(mContext.getResources().getDimension(R.dimen.textSizeAnswer));
        mDots.setTypeface(Typeface.DEFAULT_BOLD);
        mDots.setTextColor(ContextCompat.getColor(mContext, R.color.JadeRed));
        mDots.setBackgroundColor(Color.WHITE);

        // Error View
        mErrorView = new ListView(mContext);
        mErrorAdapter = new ArrayAdapter<>(
                mContext,
                android.R.layout.simple_list_item_1,
                mErrorList );
        LinearLayout.LayoutParams tempErrorParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 0.3f);
        mErrorView.setAdapter(mErrorAdapter);
        mErrorView.setOnItemClickListener(null);
        mErrorView.setDividerHeight(0);
        mErrorView.setVisibility(View.VISIBLE);

        mDate = new TextView(mContext);
        mDate.setVisibility(View.VISIBLE);
        mDate.setText("DD.MM.YY, HH:MM");
        mDate.setTextColor(ContextCompat.getColor(mContext, R.color.JadeGray));
        mDate.setTextSize(mContext.getResources().getDimension(R.dimen.textSizeAnswer));
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

    // Simply increases text size of "Start Questionnaire" item in user menu
    public void increaseStartTextSize() {

        mStartQuestionnaire.setText(formatString(StartText));
        mStartQuestionnaire.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        mStartQuestionnaire.setTextSize(mContext.getResources().
                getDimension(R.dimen.textSizeProposed));
        mStartQuestionnaire.setTypeface(null, Typeface.BOLD);
        mStartQuestionnaire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContextQPA.sendMessage(ControlService.MSG_PROPOSITION_ACCEPTED);
            }
        });
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
        //mStartQuestionnaire.setText(cleanUpString(StartText));
        mStartQuestionnaire.setTextSize(mContext.getResources().
                getDimension(R.dimen.textSizeAnswer));
        mStartQuestionnaire.setTypeface(Typeface.DEFAULT);
    }

    // Handles update of visible text countdown
    public void updateCountdownText(int seconds) {
        int minutesRemaining = seconds / 60;
        int secondsRemaining = seconds - minutesRemaining * 60;
        mCountDownRemaining.setText("" + mTempTextCountDownRemaining[0] + minutesRemaining +
                mTempTextCountDownRemaining[1] + secondsRemaining + mTempTextCountDownRemaining[2]);
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