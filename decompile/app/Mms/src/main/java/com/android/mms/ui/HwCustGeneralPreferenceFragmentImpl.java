package com.android.mms.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.text.SpannableStringBuilder;
import com.android.mms.HwCustMmsConfigImpl;
import com.huawei.android.provider.SettingsEx.Systemex;

public class HwCustGeneralPreferenceFragmentImpl extends HwCustGeneralPreferenceFragment {
    public static final String CUST_DELIVERY_REPORT_KEY = "cust_delivery_report_key";
    private static final int DELIVERY_REPORT_NOT_CUST_VAL = -1;
    private static final String TAG = "HwCustGeneralPreferenceFragmentImpl";

    public String getKeyCustMessageRing(SharedPreferences sp) {
        return sp.getString("key_cust_message_ring", "no_cust_message_ring");
    }

    public void restoreKeyCustMessageRing(SharedPreferences sp, String custMessageRing) {
        sp.edit().putString("key_cust_message_ring", custMessageRing).commit();
    }

    public String getGeneralDefaultsBtlDigest(SharedPreferences sp) {
        return sp.getString("general_defaults_digest", "still_no_digest");
    }

    public void setGeneralDefaultsBtlDigest(SharedPreferences sp, String generalOldBtlDigest) {
        sp.edit().putString("general_defaults_digest", generalOldBtlDigest).commit();
    }

    public boolean getEnableCotaFeature() {
        return HwCustMmsConfigImpl.getEnableCotaFeature();
    }

    public int getCustDeliveryReportState(Context context, int deliveryReportState) {
        int state = deliveryReportState;
        int custStateVal = Systemex.getInt(context.getContentResolver(), CUST_DELIVERY_REPORT_KEY, -1);
        if (custStateVal == -1 || deliveryReportState >= custStateVal) {
            return state;
        }
        return custStateVal;
    }

    public boolean isHideDeliveryReportsItem() {
        return HwCustMmsConfigImpl.isHideDeliveryReportsItem();
    }

    public void hideDeliveryReportsItem(PreferenceCategory category, Preference deliverPref) {
        if (deliverPref != null && category != null) {
            category.removePreference(deliverPref);
        }
    }

    public CharSequence[] getCustDeliveryReportItem(CharSequence[] reportChoices, SpannableStringBuilder textMessage) {
        return new CharSequence[]{textMessage};
    }
}
