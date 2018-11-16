package com.fragtest.android.pa.Menu;

import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.fragtest.android.pa.Core.Units;
import com.fragtest.android.pa.MainActivity;
import com.fragtest.android.pa.Questionnaire.QuestionnairePagerAdapter;
import com.fragtest.android.pa.R;

/**
 * Created by ul1021 on 17.11.2017.
 */

public class Help extends AppCompatActivity {


    private final static String LOG_STRING = "Help";
    private MainActivity mMainActivity;
    private QuestionnairePagerAdapter mContextQPA;
    private LayoutInflater inflater;
    private RadioButton rb1, rb2;
    private CheckBox cb1, cb2, cb3;
    private TextView tv1, tv2, tv3;
    private Button bt1;
    private Units mUnits;

    public Help(MainActivity context, QuestionnairePagerAdapter contextQPA) {
        mMainActivity = context;
        mContextQPA = contextQPA;
        inflater = LayoutInflater.from(context);
        mUnits = new Units(mMainActivity);
    }

    public LinearLayout generateView() {
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.layout_help, null);

        rb1 = (RadioButton) view.findViewById(R.id.radioButton);
        rb2 = (RadioButton) view.findViewById(R.id.radioButton2);

        cb1 = (CheckBox) view.findViewById(R.id.checkBox);
        cb2 = (CheckBox) view.findViewById(R.id.checkBox2);
        cb3 = (CheckBox) view.findViewById(R.id.checkBox3);

        tv1 = (TextView) view.findViewById(R.id.textView);
        tv2 = (TextView) view.findViewById(R.id.textView2);
        tv3 = (TextView) view.findViewById(R.id.textView3);

        bt1 = (Button) view.findViewById(R.id.button);

        tv2.setText(R.string.help_hint);

        setRadioPrefs(rb1);
        setRadioPrefs(rb2);
        setCheckPrefs(cb1);
        setCheckPrefs(cb2);
        setCheckPrefs(cb3);
        setHeadLinePrefs(tv1);

        setTextPrefs(tv2);
        setTextPrefs(tv3);
        setButtonPrefs(bt1);

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContextQPA.backToMenu();
                mMainActivity.mAppState.closeHelp();
            }
        });

        return view;
    }

    private void setRadioPrefs(RadioButton radiobutton) {
        radiobutton.setTextSize(mMainActivity.getResources().getDimension(R.dimen.textSizeAnswerHelp));
        radiobutton.setGravity(Gravity.CENTER_VERTICAL);
        radiobutton.setTextColor(ContextCompat.getColor(mMainActivity, R.color.TextColor));
        radiobutton.setBackgroundColor(ContextCompat.getColor(mMainActivity, R.color.BackgroundColor));
        int states[][] = {{android.R.attr.state_checked}, {}};
        int colors[] = {ContextCompat.getColor(mMainActivity, R.color.JadeRed),
                ContextCompat.getColor(mMainActivity, R.color.JadeRed)};
        CompoundButtonCompat.setButtonTintList(radiobutton, new ColorStateList(states, colors));
        radiobutton.setMinHeight((int) mMainActivity.getResources().getDimension(R.dimen.radioMinHeight));
        radiobutton.setPadding(24, 24, 24, 24);
    }

    private void setCheckPrefs(CheckBox checkBox) {
        checkBox.setTextSize(mMainActivity.getResources().getDimension(R.dimen.textSizeAnswerHelp));
        checkBox.setChecked(false);
        checkBox.setGravity(Gravity.CENTER_VERTICAL);
        checkBox.setPadding(24, 24, 24, 24);
        checkBox.setTextColor(ContextCompat.getColor(mMainActivity, R.color.TextColor));
        checkBox.setBackgroundColor(ContextCompat.getColor(mMainActivity, R.color.BackgroundColor));
        int states[][] = {{android.R.attr.state_checked}, {}};
        int colors[] = {ContextCompat.getColor(mMainActivity, R.color.JadeRed),
                ContextCompat.getColor(mMainActivity, R.color.JadeRed)};
        CompoundButtonCompat.setButtonTintList(checkBox, new ColorStateList(states, colors));
    }

    private void setHeadLinePrefs(TextView textView) {
        textView.setTextColor(ContextCompat.getColor(mMainActivity, R.color.TextColor));
        textView.setBackgroundColor(ContextCompat.getColor(mMainActivity, R.color.lighterGray));
        textView.setTextSize(mMainActivity.getResources().getDimension(R.dimen.textSizeQuestion));
    }

    private void setTextPrefs(TextView textView) {
        textView.setTextColor(ContextCompat.getColor(mMainActivity, R.color.TextColor));
        textView.setTextSize(mMainActivity.getResources().getDimension(R.dimen.textSizeAnswerHelp));
    }

    private void setTextHTML(TextView textView) {
        String string = textView.getText().toString();
        textView.setText(Html.fromHtml(string));
    }

    private void setButtonPrefs(Button button) {
        button.setScaleX(1.2f);
        button.setScaleY(1.2f);
        button.setTextColor(ContextCompat.getColor(mMainActivity, R.color.TextColor));
        button.setBackground(ContextCompat.getDrawable(mMainActivity, R.drawable.button));
    }
}
