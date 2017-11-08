package com.android.settings.deviceinfo;

import android.content.Context;
import android.telephony.TelephonyManager;

public class HwCustMSimSubscriptionStatusTabFragment {
    public MSimSubscriptionStatusTabFragment mMSimSubscriptionStatusTabFragment;
    public int mSubscription;

    public HwCustMSimSubscriptionStatusTabFragment(MSimSubscriptionStatusTabFragment mSimSubscriptionStatusTabFragment, int subscription) {
        this.mMSimSubscriptionStatusTabFragment = mSimSubscriptionStatusTabFragment;
        this.mSubscription = subscription;
    }

    public void updateCustPreference(Context context) {
    }

    public void updateSignalStrength() {
    }

    public void updateMccMncPrefSummary() {
    }

    public void updateDataState(TelephonyManager mTelephonyManager, boolean isCAstate) {
    }

    public void updateServiceState() {
    }

    public boolean isHideRoaming() {
        return false;
    }

    public boolean isHideOperatorName(String preference, String text, String unknownOperatorName) {
        return false;
    }
}
