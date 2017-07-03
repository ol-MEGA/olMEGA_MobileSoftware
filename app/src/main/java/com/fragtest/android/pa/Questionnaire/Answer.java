package com.fragtest.android.pa.Questionnaire;

import android.util.Log;

/**
 * Created by ulrikkowalk on 28.02.17.
 */

public class Answer {

    String Text;
    int Id;
    private boolean isDefault = false;
    private String LOG_STRING = "Answer";
    private boolean isDebug = false;

    public Answer(String sAnswer, int nAnswerId) {
        Text = sAnswer;
        Text = Text.replaceAll("&lt;", "<");
        Text = Text.replaceAll("&gt;", ">");
        Id = nAnswerId;

        if (isDebug) {
            Log.i(LOG_STRING, "Answer added - Text: " + Text + ", Id: " + Id);
        }
    }

    public Answer(String sAnswer, int nAnswerId, boolean bDefault) {
        Text = sAnswer;
        Text = Text.replaceAll("&lt;", "<");
        Text = Text.replaceAll("&gt;", ">");
        Id = nAnswerId;
        isDefault = bDefault;

        if (isDebug) {
            Log.i(LOG_STRING, "Answer added - Text: " + Text + ", Id: " +
                    Id + ", Default: " + isDefault);
        }
    }

    public boolean isDefault() {
        return isDefault;
    }
}
