package com.fragtest.android.pa;

import android.util.Log;

/**
 * Created by ulrikkowalk on 28.02.17.
 */

public class Answer {

    public String Text;
    public int Id;
    public boolean Default = false;
    private String LOG_STRING = "Answer";

    public Answer(String sAnswer, int nAnswerId) {
        Text = sAnswer;
        Text = Text.replaceAll("&lt;","<");
        Text = Text.replaceAll("&gt;",">");
        Id = nAnswerId;

        Log.i(LOG_STRING, "Answer added - Text: " + Text + ", Id: " + Id);
    }

    public Answer(String sAnswer, int nAnswerId, boolean isDefault) {
        Text = sAnswer;
        Text = Text.replaceAll("&lt;","<");
        Text = Text.replaceAll("&gt;",">");
        Id = nAnswerId;
        Default = isDefault;

        Log.i(LOG_STRING, "Answer added - Text: " + Text + ", Id: " + Id + ", Default: " + Default);
    }

    public boolean isDefault() { return Default; }
}
