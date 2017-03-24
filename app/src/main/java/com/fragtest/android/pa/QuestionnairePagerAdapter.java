package com.fragtest.android.pa;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;

import android.provider.Settings.Secure;

import static java.security.AccessController.getContext;

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
    public ViewPager mViewPager;

    // Object containing system information e.g. time and date
    private MetaData mMetaData;

    public QuestionnairePagerAdapter(Context context, ViewPager viewPager) {

        mContext = context;
        mViewPager = viewPager;
        mMetaData = new MetaData(mContext);
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
            mListOfActiveViews.add(new QuestionViewActive(mLayout, mLayout.getId(), iQuestion));
            // Adds the Layout to List storing ALL Views
            mListOfViewsStorage.add(new QuestionViewActive(mLayout, mLayout.getId(), iQuestion));
        }

        // Creates and destroys views based on filter id settings
        mQuestionnaire.checkVisibility();
        mMetaData.initialise();
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

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public int addView(View view, int position, int positionInRaw) {
        mListOfActiveViews.add(new QuestionViewActive(view, view.getId(), positionInRaw));
        // Sort the Views by their id (implicitly their determined order)
        Collections.sort(mListOfActiveViews);
        return position;
    }

    public int removeView(int position) {

        int nCurrentItem = mViewPager.getCurrentItem();
        mViewPager.setAdapter(null);
        mListOfActiveViews.remove(position);
        mViewPager.setAdapter(this);
        mViewPager.setCurrentItem(nCurrentItem);
        return position;
    }

    @Override
    public int getItemPosition(Object object) {
        int index = mListOfActiveViews.indexOf(object);

        if (index == -1)
            return POSITION_NONE;
        else
            return index;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    public int getPositionFromId(int iId) {
        for (int iItem = 0; iItem < mListOfActiveViews.size(); iItem++) {
            if (mListOfActiveViews.get(iItem).getId() == iId) {
                return iItem;
            }
        }
        return -1;
    }
}