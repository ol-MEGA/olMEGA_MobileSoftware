package com.fragtest.android.pa;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Preferences
 */

public class SwipingActivity extends AppCompatActivity {

    private static final String LOG = "SwipingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as main content
        /*getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new Preferences())
                .commit();*/
        setContentView(R.layout.layout_swipe_message);
        this.setFinishOnTouchOutside(false);


        Button mOkay = (Button) findViewById(R.id.swipe_button);
        mOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                MainActivity.startRecordingFalseSwipes();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}