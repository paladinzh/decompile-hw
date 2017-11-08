package com.huawei.systemmanager.netassistant.ui.setting;

import android.app.Activity;
import android.preference.PreferenceGroup;

public class HwCustGlobalTrafficSettingFragment {
    public GlobalTrafficSettingFragment mGlobalTrafficSettingFragment;

    public HwCustGlobalTrafficSettingFragment(GlobalTrafficSettingFragment globalTrafficSettingFragment) {
        this.mGlobalTrafficSettingFragment = globalTrafficSettingFragment;
    }

    public void addResetPreferenceToGroup(Activity activity, PreferenceGroup preferenceGroup, String imsi, int cardPreferIndex) {
    }

    public void removeResetPreferenceFromGroup(PreferenceGroup preferenceGroup, int cardPreferIndex) {
    }
}
