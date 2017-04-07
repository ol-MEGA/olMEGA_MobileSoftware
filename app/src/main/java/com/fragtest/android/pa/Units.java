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
    private static int QUESTION_TEXT_HEIGHT = 165; //225 is enough space for 2 lines of text
    private static int[] ANSWER_TEXT_MARGIN = {48, 32, 24, 32};
    private static int[] ANSWER_LAYOUT_PADDING = {48, 48, 48, 48};
    private static int[] ANSWER_FINISH_MARGIN = {0,48,0,0};
    private static int SCREEN_SIZE_HEIGHT;
    private static int SCREEN_SIZE_WIDTH;
    private Context mContext;

    public Units(Context context){
        mContext = context;
        mResources = context.getResources();
        mMetrics = mResources.getDisplayMetrics();
        SCREEN_SIZE_WIDTH = mMetrics.widthPixels;
        SCREEN_SIZE_HEIGHT = mMetrics.heightPixels;
    }

    public int getUsableSliderHeight() {
        return getScreenHeight() - getQuestionTextHeight() -
                convertDpToPixels(32) - convertDpToPixels(6) -
                getAnswerLayoutPadding()[1] - getAnswerLayoutPadding()[3] -
                getStatusBarHeight() - getAnswerLayoutPadding()[1];
    }

    /*
    public static Point getNavigationBarSize(Context context) {
        Point appUsableSize = getAppUsableScreenSize(context);
        Point realScreenSize = getRealScreenSize(context);

        // navigation bar on the right
        if (appUsableSize.x < realScreenSize.x) {
            return new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
        }

        // navigation bar at the bottom
        if (appUsableSize.y < realScreenSize.y) {
            return new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
        }

        // navigation bar is not present
        return new Point();
    }

    public static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();

        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(size);
        } else if (Build.VERSION.SDK_INT >= 14) {
            try {
                size.x = (Integer)     Display.class.getMethod("getRawWidth").invoke(display);
                size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (IllegalAccessException e) {} catch     (InvocationTargetException e) {} catch (NoSuchMethodException e) {}
        }

        return size;
    }
*/



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
