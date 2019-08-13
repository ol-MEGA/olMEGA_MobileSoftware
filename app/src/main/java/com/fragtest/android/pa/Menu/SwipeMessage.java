package com.fragtest.android.pa.Menu;


import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.fragtest.android.pa.MainActivity;
import com.fragtest.android.pa.Questionnaire.QuestionnairePagerAdapter;
import com.fragtest.android.pa.R;

/**
 * Created by ul1021 on 17.11.2017.
 */

public class SwipeMessage extends AppCompatActivity {


    private final static String LOG_STRING = "Help";
    private MainActivity mMainActivity;
    private QuestionnairePagerAdapter mContextQPA;
    private LayoutInflater inflater;
    private Button mOkayButton;


    public SwipeMessage(MainActivity context, QuestionnairePagerAdapter contextQPA) {
        mMainActivity = context;
        mContextQPA = contextQPA;
        inflater = LayoutInflater.from(context);
    }

    public LinearLayout generateView() {
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.layout_swipe_message, null);
        mOkayButton = (Button) view.findViewById(R.id.swipe_button);

        mOkayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContextQPA.returnToQuestionnaire();
            }
        });

        return view;
    }

}
