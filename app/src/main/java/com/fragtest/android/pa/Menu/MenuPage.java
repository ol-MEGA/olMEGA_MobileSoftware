package com.fragtest.android.pa.Menu;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
    private TextView mCountDownRemaining, mStartQuestionnaire, mDate;
    private String[] mTempTextCountDownRemaining;
    private SimpleDateFormat mDateFormat;
    private ArrayList<String> mErrorList = new ArrayList<>();
    private ArrayAdapter<String> mErrorAdapter;
    private ListView mErrorView;
    private boolean isCharging = false;

    public final int ERROR_NOBT = 0;
    public final int ERROR_NOQUEST = 1;
    public final int ERROR_BATT = 2;
    public final int ERROR_BATT_CRIT = 3;
    public final int ERROR_BATT_CHARGING = 4;

    public MenuPage(Context context, QuestionnairePagerAdapter contextQPA) {

        mContext = context;
        mContextQPA = contextQPA;
        StartText = "";
        mCountDownString = mContext.getResources().getString(R.string.timeRemaining);
        mTempTextCountDownRemaining = mCountDownString.split("%");
        mDateFormat = new SimpleDateFormat("dd.MM.yy, HH:mm", Locale.ROOT);

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

        for (int iString = 0; iString < mErrorList.size(); iString++) {
            Log.e(LOG, "error "+ iString + ": " + mErrorList.get(iString));
        }

        if (mErrorList.contains(getMessage(ERROR_NOBT)) ||
                mErrorList.contains(getMessage(ERROR_NOQUEST)) ||
                mErrorList.contains(getMessage(ERROR_BATT_CRIT))) {
            disableQuestionnaire();
            Log.e(LOG, "errorlist Disabling Quest");
        } else {
            enableQuestionnaire();
            Log.e(LOG, "errorlist Enabling Quest");
        }
    }

    public void addError(int error) {
        if (!mErrorList.contains(getMessage(error))) {
            mErrorList.add(getMessage(error));
            mErrorAdapter.notifyDataSetChanged();
            Log.e(LOG, "errorlist: " + mErrorList.size() + " - add");
        }
        reactToErrors();
    }

    public void removeError(int error) {
        if (mErrorList.contains(getMessage(error))) {
            mErrorList.remove(getMessage(error));
            mErrorAdapter.notifyDataSetChanged();
            Log.e(LOG, "errorlist: " + mErrorList.size() + " - remove");
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
    }

    public void setCharging(boolean charging) {
        isCharging = charging;
        if (isCharging) {
            setText(mContext.getResources().getString(R.string.infoCharging));
            mStartQuestionnaire.setEnabled(false);
            resetStartTextSize();
            mCountDownRemaining.setText("");
            mErrorView.setVisibility(View.INVISIBLE);
        } else if (mErrorList.isEmpty()) {
            mErrorView.setVisibility(View.VISIBLE);
            setText(mContext.getResources().getString(R.string.menuText));
        } else {
            mErrorView.setVisibility(View.VISIBLE);
            setText(mContext.getResources().getString(R.string.infoError));
        }
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
        //mStartQuestionnaire.setMaxWidth(1000);
        mStartQuestionnaire.setTextColor(ContextCompat.getColor(mContext, R.color.JadeRed));
        mStartQuestionnaire.setBackgroundColor(Color.WHITE);
        mStartQuestionnaire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetStartTextSize();
                mContextQPA.sendMessage(ControlService.MSG_MANUAL_QUESTIONNAIRE);
            }
        });

        // Error View
        mErrorView = new ListView(mContext);
        mErrorAdapter = new ArrayAdapter<String>(
                mContext,
                android.R.layout.simple_list_item_1,
                mErrorList );
        LinearLayout.LayoutParams tempErrorParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 0.3f);
        mErrorView.setAdapter(mErrorAdapter);
        mErrorView.setOnItemClickListener(null);
        mErrorView.setDividerHeight(0);

        mDate = new TextView(mContext);
        mDate.setText("DD.MM.YY, HH:MM");
        mDate.setTextColor(ContextCompat.getColor(mContext, R.color.JadeGray));
        mDate.setTextSize(mContext.getResources().getDimension(R.dimen.textSizeAnswer));
        mDate.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        centerLayout.addView(mStartQuestionnaire);
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
        mStartQuestionnaire.setText(cleanUpString(StartText));
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