package com.huawei.systemmanager.netassistant.ui.mainpage;

import android.content.Context;
import android.preference.Preference;
import com.huawei.hwsystemmanager.HwCustSystemManagerUtils;

public class HwCustMainListFragmentImpl extends HwCustMainListFragment {
    public void updatePreferenceTitle(Context context, Preference mPrefeerence, int subId) {
        if (HwCustSystemManagerUtils.isMccChange4G("hw_show_lte", context, subId) && mPrefeerence != null) {
            mPrefeerence.setTitle(mPrefeerence.getTitle().toString().replace("4G", "LTE"));
        }
        if (HwCustSystemManagerUtils.isMccChange4G("hw_show_4_5G_for_mcc", context, subId) && mPrefeerence != null) {
            mPrefeerence.setTitle(mPrefeerence.getTitle().toString().replace("4G", "4.5G"));
        }
    }

    public void updatePreferenceTitle(Context context, Preference mPrefeerence) {
        if (HwCustSystemManagerUtils.isMccChange4G("hw_show_lte", context) && mPrefeerence != null) {
            mPrefeerence.setTitle(mPrefeerence.getTitle().toString().replace("4G", "LTE"));
        }
        if (HwCustSystemManagerUtils.isMccChange4G("hw_show_4_5G_for_mcc", context) && mPrefeerence != null) {
            mPrefeerence.setTitle(mPrefeerence.getTitle().toString().replace("4G", "4.5G"));
        }
    }
}
