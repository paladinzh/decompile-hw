package com.android.settings;

import android.content.Context;
import android.support.v7.preference.PreferenceScreen;

public class HwCustSmartKeySettingsImpl extends HwCustSmartKeySettings {
    private static final String KEY_SMART_KEY_SETTING = "smartkey_settings";
    private static final String SMART_KEY = "com.android.huawei.smartkey";
    private boolean haveSmartKey;
    private Context mContext;
    private PreferenceScreen mSmartKeyPreference;

    public HwCustSmartKeySettingsImpl(MoreAssistanceSettings moreAssistanceSettings) {
        super(moreAssistanceSettings);
    }

    public void updateCustPreference(Context context) {
        this.mContext = context;
        this.haveSmartKey = Utils.hasPackageInfo(this.mContext.getPackageManager(), SMART_KEY);
        PreferenceScreen root = this.mMoreAssistanceSettings.getPreferenceScreen();
        this.mMoreAssistanceSettings.getPreferenceManager().inflateFromResource(context, 2131230900, root);
        this.mSmartKeyPreference = (PreferenceScreen) root.findPreference(KEY_SMART_KEY_SETTING);
        if (this.haveSmartKey) {
            this.mSmartKeyPreference.setOrder(2);
        } else {
            root.removePreference(this.mSmartKeyPreference);
        }
    }
}
