package com.fragtest.android.pa.Core;

import com.fragtest.android.pa.DataTypes.IntegerBooleanBooleanAndBoolean;

import java.util.ArrayList;

/**
 * Created by ul1021 on 19.05.2017.
 */

public class MandatoryInfo extends ArrayList<IntegerBooleanBooleanAndBoolean> {

    private String LOG_STRING = "MandatoryInfo";
    private ArrayList<IntegerBooleanBooleanAndBoolean> mMandatoryInfo;

    public MandatoryInfo(){
        mMandatoryInfo = new ArrayList<>();
    }

    public boolean add(int id, boolean mandatory, boolean hidden, boolean isForced) {
        mMandatoryInfo.add(new IntegerBooleanBooleanAndBoolean(id, mandatory, hidden, isForced));
        return true;
    }

    public boolean isMandatoryFromId(int id) {
        for (int iItem = 0; iItem < mMandatoryInfo.size(); iItem++) {
            if (mMandatoryInfo.get(iItem).getId() == id) {
                return mMandatoryInfo.get(iItem).isMandatory();
            }
        }
        return false;
    }

    public boolean isForcedFromId(int id) {
        for (int iItem = 0; iItem < mMandatoryInfo.size(); iItem++) {
            if (mMandatoryInfo.get(iItem).getId() == id) {
                return mMandatoryInfo.get(iItem).isForced();
            }
        }
        return false;
    }

    public boolean isHiddenFromId(int id) {
        for (int iItem = 0; iItem < mMandatoryInfo.size(); iItem++) {
            if (mMandatoryInfo.get(iItem).getId() == id) {
                return mMandatoryInfo.get(iItem).isHidden();
            }
        }
        return false;
    }

    public IntegerBooleanBooleanAndBoolean get(int item) {
        return mMandatoryInfo.get(item);
    }
}
