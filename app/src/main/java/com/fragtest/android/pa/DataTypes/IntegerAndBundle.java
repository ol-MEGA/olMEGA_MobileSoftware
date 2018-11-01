package com.fragtest.android.pa.DataTypes;

import android.os.Bundle;

/**
 * Created by ul1021 on 30.10.2018.
 */

public class IntegerAndBundle {

    private Integer integer;
    private Bundle bundle;

    public IntegerAndBundle(Integer integer, Bundle bundle) {
        this.integer = integer;
        this.bundle = bundle;
    }

    public IntegerAndBundle(Integer integer) {
        this.integer = integer;
        Bundle tmp = new Bundle();
        tmp.putString("tmp", "-1");
        this.bundle = tmp;
    }

    public int getInteger() { return this.integer; }

    public Bundle getBundle() { return this.bundle; }

}
