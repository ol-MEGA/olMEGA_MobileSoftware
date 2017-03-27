package com.fragtest.android.pa;

import android.app.Activity;
import android.content.Context;
import android.renderscript.ScriptGroup;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by ulrikkowalk on 24.03.17.
 */

public class Utils {

    InputMethodManager mIMM;
    Context mContext;

    public Utils(Context context){
        mContext = context;
        mIMM = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public InputMethodManager getIMM() { return mIMM; }

    public static void hide(Activity activity) {
        if (null == activity) {
            return;
        }

        View view = activity.getCurrentFocus();
        if (null == view) {
            return;
        }

        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
