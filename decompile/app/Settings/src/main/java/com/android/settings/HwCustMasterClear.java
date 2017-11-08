package com.android.settings;

import android.content.Context;

public class HwCustMasterClear {
    protected Context mContext;

    public HwCustMasterClear(Context context) {
        this.mContext = context;
    }

    public boolean isShowExternalStorage() {
        return false;
    }
}
