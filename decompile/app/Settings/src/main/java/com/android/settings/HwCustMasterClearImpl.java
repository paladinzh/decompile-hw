package com.android.settings;

import android.content.Context;
import android.provider.SettingsEx.Systemex;

public class HwCustMasterClearImpl extends HwCustMasterClear {
    public HwCustMasterClearImpl(Context context) {
        super(context);
    }

    public boolean isShowExternalStorage() {
        if (Systemex.getInt(this.mContext.getContentResolver(), "showInternalExternalStorage", 0) == 1) {
            return true;
        }
        return false;
    }
}
