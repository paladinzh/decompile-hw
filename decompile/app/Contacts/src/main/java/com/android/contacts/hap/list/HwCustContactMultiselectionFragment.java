package com.android.contacts.hap.list;

import android.content.Context;

public class HwCustContactMultiselectionFragment {
    protected static final String TAG = "HwCustContactMultiselectionFragment";
    Context mContext;

    public HwCustContactMultiselectionFragment(Context context) {
        this.mContext = context;
    }

    public boolean supportReadOnly() {
        return false;
    }
}
