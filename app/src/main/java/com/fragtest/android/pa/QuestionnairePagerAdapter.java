package com.fragtest.android.pa;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by ulrikkowalk on 28.02.17.
 */

public class QuestionnairePagerAdapter extends PagerAdapter {

    // Stores all active Views
    public ArrayList<QuestionViewActive> mListOfActiveViews;
    // Stores all Views
    public ArrayList<QuestionViewActive> mListOfViewsStorage;
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
        mQuestionnaire = new Questionnaire(mContext, this);
        mNUM_PAGES = mQuestionnaire.getNumPages();
        mViewPager.setOffscreenPageLimit(mNUM_PAGES - 1);

        mListOfActiveViews = new ArrayList<>();
        mListOfViewsStorage = new ArrayList<>();
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
            // Adds the Layout to List carrying all ACTIVE Views
            mListOfActiveViews.add(new QuestionViewActive(mLayout,mLayout.getId()));
            // Adds the Layout to List storing ALL Views
            mListOfViewsStorage.add(new QuestionViewActive(mLayout,mLayout.getId()));
        }
        // Creates and Destroys Views based on Filter ID Settings
        mQuestionnaire.checkVisibility();

        Log.e("size of list", "" + mListOfActiveViews.size());
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        View view = mListOfActiveViews.get(position).getView();
        collection.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        mNUM_PAGES = mListOfActiveViews.size();
        return mNUM_PAGES;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public void setCount(int numPages) {
        mNUM_PAGES = numPages;
    }

    public void setCount() {
        mNUM_PAGES = getCount();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        Log.e("count", "" + getCount());
    }

    public int addView(View v) {
        return addView(v, mListOfActiveViews.size());
    }

    public int addView(View view, int position) {
        mListOfActiveViews.add(new QuestionViewActive(view,view.getId()));

        // Sort the Views by their ID (implicitly their determined order)
        Collections.sort(mListOfActiveViews);

        /** SHOW ALL IDS IN THEIR ORDER **/
        for (int iItem = 0; iItem<mListOfActiveViews.size(); iItem++) {
            Log.i("pos "+iItem,""+mListOfActiveViews.get(iItem).getId());
        }


        return position;
    }

    public int removeView(ViewPager pager, View v) {
        Log.i("index",""+mListOfActiveViews.indexOf(v));
        return removeView(mListOfActiveViews.indexOf(v));
    }

    public int removeView(int position) {
        // ViewPager doesn't have a delete method; the closest is to set the adapter
        // again.  When doing so, it deletes all its views.  Then we can delete the view
        // from from the adapter and finally set the adapter to the pager again.  Note
        // that we set the adapter to null before removing the view from "views" - that's
        // because while ViewPager deletes all its views, it will call destroyItem which
        // will in turn cause a null pointer ref.
        Log.i("position req",""+position);
        mViewPager.setAdapter(null);
        Log.i("length before",""+mListOfActiveViews.size());
        mListOfActiveViews.remove(position);
        Log.i("length afterwards",""+mListOfActiveViews.size());
        mViewPager.setAdapter(this);
        Log.i("adapter","set");
        return position;
    }

    public View getView(int position) {
        return mListOfActiveViews.get(position).getView();
    }

    @Override
    public int getItemPosition(Object object) {
        int index = mListOfActiveViews.indexOf(object);
        if (index == -1)
            return POSITION_NONE;
        else
            return index;
    }

    public int getPositionFromId(int iId){
        for (int iItem = 0; iItem < mListOfActiveViews.size(); iItem++) {
            if (mListOfActiveViews.get(iItem).getId() == iId) {
                return iItem;
            }
        }
        return -1;
    }
}