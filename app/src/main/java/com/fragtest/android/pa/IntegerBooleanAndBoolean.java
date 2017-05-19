package com.fragtest.android.pa;

/**
 * Created by ulrikkowalk on 31.03.17.
 */

public class IntegerBooleanAndBoolean {

    private int mId;
    private boolean mMandatory;
    private boolean mHidden;


    public IntegerBooleanAndBoolean(int id, boolean mandatory, boolean hidden) {
        mId = id;
        mMandatory = mandatory;
        mHidden = hidden;
    }

    public int getId() { return mId; }

    public boolean isMandatory() { return mMandatory; }

    public boolean isHidden() { return mHidden; }
}
