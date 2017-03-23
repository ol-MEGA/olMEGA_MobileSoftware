package com.fragtest.android.pa;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {

    public ViewPager mViewPager = null;
    private QuestionnairePagerAdapter pagerAdapter = null;

    private View mArrowBack;
    private View mArrowForward;
    private View mRevert;
    private int mPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Intro Video -> might be better Option to include additional Activity
        //setContentView(R.layout.intro);
        //VideoView videoView = (VideoView)findViewById(R.id.introVideo);
        //videoView.setMediaController(mc);
        //Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.logo);
        //videoView.setVideoURI(uri);
        //videoView.start();

        //videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
        //   @Override
        //   public void onCompletion(MediaPlayer mp) {


        setContentView(R.layout.activity_main);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new QuestionnairePagerAdapter(getApplicationContext(), mViewPager));
        mViewPager.setCurrentItem(0);

        mViewPager.addOnPageChangeListener(myOnPageChangeListener);

        mArrowBack = findViewById(R.id.Action_Back);
        mArrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPosition != 0) {
                    mViewPager.setCurrentItem(mPosition - 1);
                }
            }
        });

        mArrowForward = findViewById(R.id.Action_Forward);
        mArrowForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPosition < mViewPager.getAdapter().getCount() - 1) {
                    mViewPager.setCurrentItem(mPosition + 1);
                }
            }
        });

        mRevert = findViewById(R.id.Action_Revert);
        mRevert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Data was reverted.", Toast.LENGTH_SHORT).show();
                mViewPager.setCurrentItem(0);
            }
        });

        setQuestionnaireProgBar(0);
        setArrows(0);

        //}
        //});


    }

    private ViewPager.OnPageChangeListener myOnPageChangeListener =
            new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageScrollStateChanged(int state) {
                }

                @Override
                public void onPageScrolled(int position,
                                           float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    setQuestionnaireProgBar(position);
                    setArrows(position);
                }
            };

    // Set the horizontal Indicator at the Top to follow Page Position
    public void setQuestionnaireProgBar(int position) {

        mPosition = position;
        int nAccuracy = 100;

        View progress = findViewById(R.id.progress);
        View regress = findViewById(R.id.regress);

        float nProgress = (float) (position + 1) / mViewPager.getAdapter().getCount() * nAccuracy;
        float nRegress = (nAccuracy - nProgress);

        LinearLayout.LayoutParams progParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                nRegress
        );
        LinearLayout.LayoutParams regParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                nProgress
        );

        progress.setLayoutParams(progParams);
        regress.setLayoutParams(regParams);
    }

    public void setArrows(int position) {
        View arrowBack = findViewById(R.id.Action_Back);
        if (position == 0) {
            arrowBack.setVisibility(View.INVISIBLE);
        } else if (arrowBack.getVisibility() == View.INVISIBLE) {
            arrowBack.setVisibility(View.VISIBLE);
        }

        View arrowForward = findViewById(R.id.Action_Forward);
        if (position == mViewPager.getAdapter().getCount() - 1) {
            arrowForward.setVisibility(View.INVISIBLE);
        } else if (arrowForward.getVisibility() == View.INVISIBLE) {
            arrowForward.setVisibility(View.VISIBLE);
        }
    }
}