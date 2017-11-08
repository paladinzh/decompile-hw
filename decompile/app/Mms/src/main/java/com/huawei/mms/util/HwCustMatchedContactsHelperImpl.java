package com.huawei.mms.util;

import android.content.Context;
import android.provider.Settings.System;

public class HwCustMatchedContactsHelperImpl extends HwCustMatchedContactsHelper {
    private static final String TAG = "HwCustMatchedContactsHelperImpl";
    private String hwRussiaNumberRelevance;
    private Context mContext;

    public HwCustMatchedContactsHelperImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    public String getHwRussiaNumberRelevance() {
        if (this.hwRussiaNumberRelevance == null) {
            this.hwRussiaNumberRelevance = System.getString(this.mContext.getContentResolver(), "hw_RussiaNumberRelevance");
        }
        return this.hwRussiaNumberRelevance;
    }

    public String getFlagForRussiaNumberRelevance() {
        String result = System.getString(this.mContext.getContentResolver(), "enable_RussiaNumberRelevance");
        if ("true".equals(result)) {
            return result;
        }
        return "false";
    }
}
