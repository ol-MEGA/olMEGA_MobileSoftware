package com.fragtest.android.pa;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

/**
 * Created by ulrikkowalk on 01.03.17.
 */

public class Units extends AppCompatActivity {

    private Resources mResources;
    private DisplayMetrics mMetrics;

    private static int TEXT_SIZE_QUESTION = 20;
    private static int TEXT_SIZE_ANSWER = 20;
    private static int RADIO_MIN_HEIGHT = 96;
    private static int CHECKBOX_MIN_HEIGHT = 96;
    private static int QUESTION_TEXT_HEIGHT = 165;
    private static int[] ANSWER_TEXT_MARGIN = {48, 32, 24, 32};
    private static int[] ANSWER_LAYOUT_PADDING = {48, 48, 48, 48};
    private static int[] ANSWER_FINISH_MARGIN = {0,48,0,0};
    private static int SCREEN_SIZE_HEIGHT;
    private static int SCREEN_SIZE_WIDTH;

    public Units(Context context){
        mResources = context.getResources();
        mMetrics = mResources.getDisplayMetrics();
        SCREEN_SIZE_WIDTH = mMetrics.widthPixels;
        SCREEN_SIZE_HEIGHT = mMetrics.heightPixels;
    }

    public int getStatusBarHeight() {
        return (int) (24 * mMetrics.density);
    }

    public static int getScreenHeight() { return SCREEN_SIZE_HEIGHT; }

    public static int getScreenWidth() { return SCREEN_SIZE_WIDTH; }

    public static int[] getAnswerFinishMargin() { return ANSWER_FINISH_MARGIN; }

    public static int[] getAnswerLayoutPadding() {return ANSWER_LAYOUT_PADDING; }

    public static int[] getAnswerTextMargin() { return ANSWER_TEXT_MARGIN; }

    public static int getQuestionTextHeight() { return QUESTION_TEXT_HEIGHT; }

    public static int getCheckBoxMinHeight() { return CHECKBOX_MIN_HEIGHT; }

    public static int getRadioMinHeight() { return RADIO_MIN_HEIGHT; }

    public static int getTextSizeQuestion() { return TEXT_SIZE_QUESTION; }

    public static int getTextSizeAnswer() { return TEXT_SIZE_ANSWER; }

    public int convertDpToPixels(float dp){
        return (int) (dp * ((float) mMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public int convertPixelsToDp(float px){
        return (int) (px / ((float) mMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public int convertPixelsToSp(int px) {
        return (int) (px / mMetrics.scaledDensity);
    }

    public int convertSpToPixels(int sp) {
        return (int) (sp * mMetrics.scaledDensity);
    }

}
