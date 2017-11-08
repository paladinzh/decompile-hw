package com.android.mms.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.text.SpannableStringBuilder;

public class HwCustGeneralPreferenceFragment {
    public HwCustGeneralPreferenceFragment(PreferenceFragment preferenceFragment) {
    }

    public void onCreate(Context context) {
    }

    public void onPreferenceTreeClick(Context context, Preference preference) {
    }

    public String getKeyCustMessageRing(SharedPreferences sp) {
        return "no_cust_message_ring";
    }

    public void restoreKeyCustMessageRing(SharedPreferences sp, String custMessageRing) {
    }

    public String getGeneralDefaultsBtlDigest(SharedPreferences sp) {
        return "still_no_digest";
    }

    public void setGeneralDefaultsBtlDigest(SharedPreferences sp, String generalOldBtlDigest) {
    }

    public boolean getEnableCotaFeature() {
        return false;
    }

    public void setGeneralPreferenceFragment(Activity activity, PreferenceFragment fragment) {
    }

    public void onCreateRCS() {
    }

    public void onResume() {
    }

    public void onDestroy() {
    }

    public void restoreDefaultPreferences() {
    }

    public boolean isRcsSwitchOn() {
        return false;
    }

    public void hideDisplayModePref(Preference preference) {
    }

    public int getCustDeliveryReportState(Context context, int deliveryReportState) {
        return deliveryReportState;
    }

    public boolean isHideDeliveryReportsItem() {
        return false;
    }

    public void hideDeliveryReportsItem(PreferenceCategory category, Preference deliverPref) {
    }

    public CharSequence[] getCustDeliveryReportItem(CharSequence[] reportChoices, SpannableStringBuilder textMessage) {
        return reportChoices;
    }
}
