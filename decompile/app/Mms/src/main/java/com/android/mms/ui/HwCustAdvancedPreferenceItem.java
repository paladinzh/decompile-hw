package com.android.mms.ui;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceScreen;

public class HwCustAdvancedPreferenceItem {
    public void onCustomPreferenceItemClick(Context context, Preference preference) {
    }

    public void updateCustPreference(Context context, PreferenceScreen prefRoot) {
    }

    public boolean getEnableShowSmscNotEdit() {
        return false;
    }

    public void setSummaryAndDisableSmsCenterAddrPref(Preference preference, int subID) {
    }

    public boolean getCustMccmncEnableShowSmscNotEdit(int subID) {
        return false;
    }

    public boolean getEnableCotaFeature() {
        return false;
    }

    public void setAlwaysTxRxMmsClick(Context context) {
    }

    public void setUserModifiedAutoRetreiveFlag(Context context) {
    }
}
