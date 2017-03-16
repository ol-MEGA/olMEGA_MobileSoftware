package com.fragtest.android.pa;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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
    public ViewGroup mCollection;
    public ViewPager mViewPager;

    private String _DEVICEID;

    public QuestionnairePagerAdapter(Context context, ViewPager viewPager) {

        _DEVICEID = Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID);

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
            mListOfActiveViews.add(new QuestionViewActive(mLayout, mLayout.getId(), iQuestion));
            // Adds the Layout to List storing ALL Views
            mListOfViewsStorage.add(new QuestionViewActive(mLayout, mLayout.getId(), iQuestion));
        }
        // Creates and Destroys Views based on Filter ID Settings
        mQuestionnaire.checkVisibility();
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
        // Sort the Views by their ID (implicitly their determined order)
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

    public int getPositionFromId(int iId) {
        for (int iItem = 0; iItem < mListOfActiveViews.size(); iItem++) {
            if (mListOfActiveViews.get(iItem).getId() == iId) {
                return iItem;
            }
        }
        return -1;
    }

    public String getDeviceID() { return _DEVICEID; }

    /*
    public View getView(int position) {
        return mListOfActiveViews.get(position).getView();
    }*/

    /*
    public int addView(View v) {
        return addView(v, mListOfActiveViews.size());
        return addView(v, )
    }
    */
    /*
    public int removeView(ViewPager pager, View v) {
        Log.i("index", "" + mListOfActiveViews.indexOf(v));
        return removeView(mListOfActiveViews.indexOf(v));
    }
*/

    /*
    public void setCount(int numPages) {
        mNUM_PAGES = numPages;
    }

    public void setCount() {
        mNUM_PAGES = getCount();
    }
    */

}