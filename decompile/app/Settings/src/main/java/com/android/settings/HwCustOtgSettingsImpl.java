package com.android.settings;

import android.content.Context;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

public class HwCustOtgSettingsImpl extends HwCustOtgSettings {
    private static final String KEY_OTG_FEATURE_SETTING = "otg_feature_settings";
    private Boolean isOtgFeatureEnabled = Boolean.valueOf(SystemProperties.getBoolean("ro.config.hw_otgFeature", false));
    private Context mContext;
    public MoreAssistanceSettings mMoreAssistanceSettings;
    private OtgEnabler mOtgEnabler;

    public HwCustOtgSettingsImpl(MoreAssistanceSettings moreAssistanceSettings) {
        super(moreAssistanceSettings);
        this.mMoreAssistanceSettings = moreAssistanceSettings;
    }

    public void updateCustPreference(Context context) {
        this.mContext = context;
        PreferenceScreen root = this.mMoreAssistanceSettings.getPreferenceScreen();
        this.mMoreAssistanceSettings.getPreferenceManager().inflateFromResource(context, 2131230827, root);
        Preference otgFeaturePreference = (PreferenceScreen) root.findPreference(KEY_OTG_FEATURE_SETTING);
        if (this.isOtgFeatureEnabled.booleanValue()) {
            this.mOtgEnabler = new OtgEnabler(this.mContext, otgFeaturePreference);
        } else {
            root.removePreference(otgFeaturePreference);
        }
    }

    public void onResume() {
        if (this.mOtgEnabler != null) {
            this.mOtgEnabler.resume();
        }
    }

    public void onPause() {
        if (this.mOtgEnabler != null) {
            this.mOtgEnabler.pause();
        }
    }
}
