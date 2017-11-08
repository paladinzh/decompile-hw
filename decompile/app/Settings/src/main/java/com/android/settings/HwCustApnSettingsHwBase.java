package com.android.settings;

import android.content.Context;
import android.net.Uri;
import android.support.v7.preference.PreferenceCategory;
import java.util.ArrayList;

public class HwCustApnSettingsHwBase {
    public ApnSettingsHwBase mApnSettingsHwBase;

    public HwCustApnSettingsHwBase(ApnSettingsHwBase apnSettingsHwBase) {
        this.mApnSettingsHwBase = apnSettingsHwBase;
    }

    public String getApnDisplayTitle(Context context, String name) {
        return name;
    }

    public String getCustOperatorNumericSelection(int subscription) {
        return "";
    }

    public boolean isShowWapApn(String apn, String type, int subscription) {
        return true;
    }

    public boolean isSortbyId() {
        return false;
    }

    public Uri getPreferredApnUri(Uri prefer_apn_uri, int sub) {
        return prefer_apn_uri;
    }

    public Uri getApnUri(Uri apn_uri, int sub) {
        return apn_uri;
    }

    public Uri getRestoreAPnUri(Uri restore_apn_uri, int sub) {
        return restore_apn_uri;
    }

    public ArrayList<String> getOperatorNumeric(Context context, String numeric, ArrayList<String> result) {
        return result;
    }

    public boolean checkShouldHideApn(String apn) {
        return false;
    }

    public void addOrangeSpecialPreference(String apn, String name, PreferenceCategory mCategory_apn_general, ApnPreference pref, String apnName) {
        mCategory_apn_general.addPreference(pref);
    }

    public boolean isHideSpecialAPN(String mccmnc, String apn) {
        return false;
    }

    public boolean hideApnCustbyPreferred(String mccmnc, String preferredApn, String apn) {
        return false;
    }
}
