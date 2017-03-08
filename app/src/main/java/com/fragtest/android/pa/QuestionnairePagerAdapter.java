package com.fragtest.android.pa;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.util.Objects.isNull;

/**
 * Created by ulrikkowalk on 28.02.17.
 */

public class QuestionnairePagerAdapter extends PagerAdapter {

    private ArrayList<View> mListOfViews;
    private Context mContext;
    private int mNUM_PAGES;
    private Questionnaire mQuestionnaire;
    private LinearLayout mLayout;
    public ViewGroup mCollection;
    public ViewPager mViewPager;

    public QuestionnairePagerAdapter(Context context, ViewPager viewPager) {
        mContext = context;
        mViewPager = viewPager;
        // Instantiates a Questionnaire Object based on Contents of raw XML File
        mQuestionnaire = new Questionnaire(mContext,this);
        mNUM_PAGES = mQuestionnaire.getNumPages();
        mViewPager.setOffscreenPageLimit(mNUM_PAGES-1);

        mListOfViews = new ArrayList<>();
        createLayout();
    }

    private void createLayout() {
        for (int iQuestion = 0; iQuestion < mNUM_PAGES; iQuestion++) {
            // Extracts Question Details from Questionnaire and creates Question
            Question question = mQuestionnaire.createQuestion(iQuestion);
            // Inflates Question Layout based on Question Details
            mLayout = mQuestionnaire.generateView(question);
            // Sets Layout ID to Question ID
            mLayout.setId(mQuestionnaire.getId(question));

            mListOfViews.add(mLayout);
        }

        Log.e("size of list",""+mListOfViews.size());
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        View view = mListOfViews.get (position);
        collection.addView (view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return mListOfViews.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public void setNumPages(int numPages) { mNUM_PAGES = numPages; }

    @Override
    public void notifyDataSetChanged() {
        Log.e("count",""+getCount());
        super.notifyDataSetChanged();
    }

    public int addView(View v) {
        return addView(v, mListOfViews.size());
    }

    public int addView(View v, int position) {
        mListOfViews.add(position, v);
        return position;
    }

    public int removeView(ViewPager pager, View v) {
        return removeView(pager, mListOfViews.indexOf (v));
    }

    public int removeView(ViewPager pager, int position) {
        // ViewPager doesn't have a delete method; the closest is to set the adapter
        // again.  When doing so, it deletes all its views.  Then we can delete the view
        // from from the adapter and finally set the adapter to the pager again.  Note
        // that we set the adapter to null before removing the view from "views" - that's
        // because while ViewPager deletes all its views, it will call destroyItem which
        // will in turn cause a null pointer ref.
        mViewPager.setAdapter(null);
        mListOfViews.remove(position);
        mViewPager.setAdapter(this);

        return position;
    }

    public View getView(int position) {
        return mListOfViews.get (position);
    }

    @Override
    public int getItemPosition(Object object) {
        int index = mListOfViews.indexOf(object);
        if (index == -1)
            return POSITION_NONE;
        else
            return index;
    }
}