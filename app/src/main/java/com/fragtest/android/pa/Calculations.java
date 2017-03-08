package com.fragtest.android.pa;

import android.bluetooth.BluetoothHealthAppConfiguration;
import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

/**
 * Created by ulrikkowalk on 01.03.17.
 */

public class Calculations extends AppCompatActivity {

    Resources mResources;
    DisplayMetrics mMetrics;

    public Calculations(Context context){
        mResources = context.getResources();
        mMetrics = mResources.getDisplayMetrics();
    }

    public int convertDpToPixels(float dp){
        int px = (int) (dp * ((float) mMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public int convertPixelsToDp(float px){
        int dp = (int) (px / ((float) mMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    public int convertPixelsToSp(int px) {
        int sp = (int) (px / mMetrics.scaledDensity);
        return sp;
    }

    public int convertSpToPixels(int sp) {
        int px = (int) (sp * mMetrics.scaledDensity);
        return px;
    }

}
