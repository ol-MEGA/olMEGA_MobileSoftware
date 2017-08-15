package com.fragtest.android.pa.Core;

import com.fragtest.android.pa.DataTypes.IntegerBooleanAndBoolean;

import java.util.ArrayList;

/**
 * Created by ul1021 on 19.05.2017.
 */

public class MandatoryInfo extends ArrayList<IntegerBooleanAndBoolean> {

    private String LOG_STRING = "MandatoryInfo";
    private ArrayList<IntegerBooleanAndBoolean> mMandatoryInfo;

    public MandatoryInfo(){
        mMandatoryInfo = new ArrayList<>();
    }

    public boolean add(int id, boolean mandatory, boolean hidden) {
        mMandatoryInfo.add(new IntegerBooleanAndBoolean(id, mandatory, hidden));
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

    public boolean isHiddenFromId(int id) {
        for (int iItem = 0; iItem < mMandatoryInfo.size(); iItem++) {
            if (mMandatoryInfo.get(iItem).getId() == id) {
                return mMandatoryInfo.get(iItem).isHidden();
            }
        }
        return false;
    }

    public IntegerBooleanAndBoolean get(int item) {
        return mMandatoryInfo.get(item);
    }
}
