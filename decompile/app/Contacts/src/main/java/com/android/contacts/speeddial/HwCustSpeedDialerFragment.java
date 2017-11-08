package com.android.contacts.speeddial;

import android.content.Context;

public class HwCustSpeedDialerFragment {
    protected static final String TAG = "SpeedDialerFrament";
    Context mContext;

    public HwCustSpeedDialerFragment(Context context) {
        this.mContext = context;
    }

    public Boolean isDisableCustomService() {
        return Boolean.valueOf(false);
    }

    public String getPredefinedSpeedDialNumbersByMccmnc(String singlePair) {
        return singlePair;
    }

    public boolean isPredefinedSpeedNumberEditable() {
        return true;
    }
}
