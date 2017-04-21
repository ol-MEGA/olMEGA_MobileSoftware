package com.fragtest.android.pa;

/**
 * Created by ulrikkowalk on 28.02.17.
 */

public class Answer {

    public String Text;
    public int Id;
    public boolean Default = false;

    public Answer(String sAnswer, int nAnswerId) {
        Text = sAnswer;
        Text = Text.replaceAll("&lt;","<");
        Text = Text.replaceAll("&gt;",">");
        Id = nAnswerId;
    }

    public Answer(String sAnswer, int nAnswerId, boolean def) {
        Text = sAnswer;
        Text = Text.replaceAll("&lt;","<");
        Text = Text.replaceAll("&gt;",">");
        Id = nAnswerId;
        Default = def;
    }

    public boolean isDefault() { return Default; }
}
