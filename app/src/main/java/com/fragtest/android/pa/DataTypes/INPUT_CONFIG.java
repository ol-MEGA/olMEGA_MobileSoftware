package com.fragtest.android.pa.DataTypes;

public enum INPUT_CONFIG {
    A2DP,
    RFCOMM,
    USB,
    STANDALONE,
    INTERNAL_MIC;

    public static INPUT_CONFIG toState(String myEnumString) {
        try {
            return valueOf(myEnumString);
        } catch (Exception ex) {
            return null;
        }
    }
}