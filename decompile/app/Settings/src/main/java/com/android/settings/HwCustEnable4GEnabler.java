package com.android.settings;

import android.content.Context;

public class HwCustEnable4GEnabler {
    protected Context mContext;

    public HwCustEnable4GEnabler(Context context) {
        this.mContext = context;
    }

    boolean isVSimEnabled() {
        return false;
    }
}
