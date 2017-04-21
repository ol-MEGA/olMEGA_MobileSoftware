package com.fragtest.android.pa;

/**
 * Created by ulrikkowalk on 31.03.17.
 */

public class StringAndInteger {

    private String Text;
    private int Id;

    public StringAndInteger(String string, int id) {
        Text = string;
        Id = id;
    }

    public void setText(String text) { Text = text; }

    public String getText() { return Text; }

    public int getId() { return Id; }
}
