package com.fragtest.android.pa;

import android.database.sqlite.SQLiteBindOrColumnIndexOutOfRangeException;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.fragtest.android.pa.R.id.regress;

public class MainActivity extends AppCompatActivity {

    public ViewPager mViewPager = null;
    private QuestionnairePagerAdapter pagerAdapter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new QuestionnairePagerAdapter(this, mViewPager));
        mViewPager.setCurrentItem(0);

        mViewPager.addOnPageChangeListener(myOnPageChangeListener);

        /*
        // Bind the tabs to the ViewPager
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setOnPageChangeListener(myOnPageChangeListener);
        tabs.setViewPager(mViewPager);
        tabs.setTextColor(Color.WHITE);
        tabs.setDividerPadding(0);
        tabs.setTabPaddingLeftRight(0);
        tabs.setShouldExpand(true);
*/
        setQuestionnaireProgBar(0);



    }

    private ViewPager.OnPageChangeListener myOnPageChangeListener =
            new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageScrollStateChanged(int state) {
                    //Called when the scroll state changes.

                }

                @Override
                public void onPageScrolled(int position,
                                           float positionOffset, int positionOffsetPixels) {
                    //This method will be invoked when the current page is scrolled,
                    //either as part of a programmatically initiated smooth scroll
                    //or a user initiated touch scroll.
                }

                @Override
                public void onPageSelected(int position) {
                    //This method will be invoked when a new page becomes selected.
                    //Log.i("onPageSelected","" + position);
                    //mViewPager.setCurrentItem(position);

                    setQuestionnaireProgBar(position);
                }
            };

            // Set the horizontal Indicator at the Top to follow Page Position
            public void setQuestionnaireProgBar(int position) {

                int nAccuracy = 100;

                View progress = findViewById(R.id.progress);
                View regress = findViewById(R.id.regress);

                float nProgress = (float) (position+1)/mViewPager.getAdapter().getCount()*nAccuracy;
                float nRegress = (nAccuracy-nProgress);

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

/*
    public void addView (View newPage)
    {
        int pageIndex = pagerAdapter.addView (newPage);
        // You might want to make "newPage" the currently displayed page:
        mViewPager.setCurrentItem (pageIndex, true);
    }

    //-----------------------------------------------------------------------------
    // Here's what the app should do to remove a view from the ViewPager.
    public void removeView (View defunctPage)
    {
        int pageIndex = pagerAdapter.removeView (mViewPager, defunctPage);
        // You might want to choose what page to display, if the current page was "defunctPage".
        if (pageIndex == pagerAdapter.getCount())
            pageIndex--;
        mViewPager.setCurrentItem (pageIndex);
    }

    //-----------------------------------------------------------------------------
    // Here's what the app should do to get the currently displayed page.
    public View getCurrentPage ()
    {
        return pagerAdapter.getView (mViewPager.getCurrentItem());
    }

    //-----------------------------------------------------------------------------
    // Here's what the app should do to set the currently displayed page.  "pageToShow" must
    // currently be in the adapter, or this will crash.
    public void setCurrentPage (View pageToShow)
    {
        mViewPager.setCurrentItem (pagerAdapter.getItemPosition (pageToShow), true);
    }
    */


}