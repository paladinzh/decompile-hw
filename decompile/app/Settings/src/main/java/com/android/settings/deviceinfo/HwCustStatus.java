package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.Phone;

public class HwCustStatus {
    private Status mStatus;

    public HwCustStatus(Status status) {
        this.mStatus = status;
    }

    public String getCustomNetworkType(String networkType, TelephonyManager telephonyMgr, boolean isCAstate) {
        return networkType;
    }

    public boolean isIMEISVShowTwo(TelephonyManager telephonyManager) {
        return false;
    }

    public String getIMEISVSummaryText() {
        return null;
    }

    public String getCustOperatorName(String operatorName, ServiceState serviceState) {
        return operatorName;
    }

    public boolean isHideRoaming() {
        return false;
    }

    public void updateCustomSatatusPreference(String meid, String meidHexKey, String imeiKey, TelephonyManager telephonyMgr) {
    }

    public String updateCustPhoneNumber(Phone aPhone, String rawNumber) {
        return rawNumber;
    }

    public void addImsPreference(Context context, PreferenceScreen prefRoot) {
    }

    public boolean isDisplayIms() {
        return false;
    }

    public void setImsStatus(Context context, Intent intent) {
    }
}
