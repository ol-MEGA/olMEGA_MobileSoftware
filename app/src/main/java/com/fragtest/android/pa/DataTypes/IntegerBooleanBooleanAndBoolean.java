package com.fragtest.android.pa.DataTypes;

/**
 * Created by ulrikkowalk on 31.03.17.
 */

public class IntegerBooleanBooleanAndBoolean {

    private int mId;
    private boolean mMandatory;
    private boolean mHidden;
    private boolean mIsForced;


    public IntegerBooleanBooleanAndBoolean(int id, boolean mandatory, boolean hidden, boolean isForced) {
        mId = id;
        mMandatory = mandatory;
        mHidden = hidden;
        mIsForced = isForced;
    }

    public int getId() { return mId; }

    public boolean isMandatory() { return mMandatory; }

    public boolean isHidden() { return mHidden; }

    public boolean isForced() {
        return mIsForced;
    }
}
