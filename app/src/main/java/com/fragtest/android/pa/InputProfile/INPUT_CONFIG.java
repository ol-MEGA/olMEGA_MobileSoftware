package com.fragtest.android.pa.InputProfile;

public enum INPUT_CONFIG {
    A2DP,
    RFCOMM,
    PHANTOM,
    USB,
    STANDALONE,
    INTERNAL_MIC,
    CHARGING;

    public static INPUT_CONFIG toState(String myEnumString) {
        try {
            return valueOf(myEnumString);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String toString(INPUT_CONFIG input_config) {
        try {
            return input_config.toString();
        } catch (Exception e) {
            return null;
        }
    }
}