package com.fragtest.android.pa.DataTypes;


/**
 * Created by ulrikkowalk on 03.05.17.
 */

public class IntAndFloat {

    private int mId;
    private float mValue;

    public IntAndFloat(int id, float value){
        mId = id;
        mValue = value;
    }

    public int getId() {return mId;}

    public float getValue() {return mValue;}
}
