package com.android.contacts.hap.copy;

import android.content.ContentProviderOperation.Builder;
import android.content.Context;

public class HwCustCopyContactsProcessor {
    protected static final String TAG = "HwCustCopyContactsProcessor";
    Context mContext;

    public HwCustCopyContactsProcessor(Context context) {
        this.mContext = context;
    }

    public void setAggregationMode(Builder builder) {
    }
}
