package com.fragtest.android.pa.Questionnaire;

import android.util.Log;

/**
 * Created by ulrikkowalk on 28.02.17.
 */

public class Answer {

    public String Text;
    public int Id;
    public boolean Default = false;
    private String LOG_STRING = "Answer";
    private boolean isDebug = false;

    public Answer(String sAnswer, int nAnswerId) {
        Text = sAnswer;
        Text = Text.replaceAll("&lt;","<");
        Text = Text.replaceAll("&gt;",">");
        Id = nAnswerId;

        if (isDebug) {
            Log.i(LOG_STRING, "Answer added - Text: " + Text + ", Id: " + Id);
        }
    }

    public Answer(String sAnswer, int nAnswerId, boolean isDefault) {
        Text = sAnswer;
        Text = Text.replaceAll("&lt;","<");
        Text = Text.replaceAll("&gt;",">");
        Id = nAnswerId;
        Default = isDefault;

        if (isDebug) {
            Log.i(LOG_STRING, "Answer added - Text: " + Text + ", Id: " +
                    Id + ", Default: " + Default);
        }
    }

    public boolean isDefault() { return Default; }
}
