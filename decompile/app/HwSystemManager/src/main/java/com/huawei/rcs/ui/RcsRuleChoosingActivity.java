package com.huawei.rcs.ui;

import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.strategy.StrategyConfigs.StrategyId;
import com.huawei.rcs.util.HwRcsFeatureEnabler;
import com.huawei.systemmanager.R;

public class RcsRuleChoosingActivity {
    private static final int ORI_ORDER_IN_PREFERENCE = -1;
    private static final String TAG = "RcsRuleChoosingActivity";
    private PreferenceActivity mContext;
    public SwitchPreference mPrefBlacklistRcs = null;
    public PreferenceCategory mPrefRuleCallMsg = null;

    public RcsRuleChoosingActivity(PreferenceActivity context) {
        this.mContext = context;
    }

    public boolean isRCSEnable() {
        return HwRcsFeatureEnabler.isRcsEnabled();
    }

    public void initPreferencesFromResource(PreferenceScreen screen) {
        if (HwRcsFeatureEnabler.isRcsEnabled()) {
            this.mContext.getPreferenceManager().inflateFromResource(this.mContext, R.xml.rcs_interception_rule_choosing_preference, screen);
            this.mContext.findPreference(ConstValues.UIKEY_RULE_FOR_CALL_MSG).setOrder(-1);
            this.mPrefRuleCallMsg = (PreferenceCategory) this.mContext.findPreference(ConstValues.UIKEY_RULE_FOR_CALL_MSG);
            this.mPrefBlacklistRcs = (SwitchPreference) this.mContext.findPreference(ConstValues.UIKEY_RULE_BLACKLIST_RCS);
        }
    }

    public void setRcsOnPreferenceChangeListener(OnPreferenceChangeListener listener) {
        if (HwRcsFeatureEnabler.isRcsEnabled()) {
            this.mPrefBlacklistRcs.setOnPreferenceChangeListener(listener);
        }
    }

    public boolean removeRcsPreference(Preference preference) {
        if (HwRcsFeatureEnabler.isRcsEnabled()) {
            return this.mPrefRuleCallMsg.removePreference(preference);
        }
        return false;
    }

    public void setRcsChecked(boolean checked) {
        if (HwRcsFeatureEnabler.isRcsEnabled()) {
            this.mPrefBlacklistRcs.setChecked(checked);
        }
    }

    public void setRcsChecked(String key, boolean checked, SwitchPreference preference) {
        if (HwRcsFeatureEnabler.isRcsEnabled()) {
            if (key.equals(ConstValues.UIKEY_RULE_BLACKLIST)) {
                this.mPrefBlacklistRcs.setChecked(checked);
            }
            if (key.equals(ConstValues.UIKEY_RULE_BLACKLIST_RCS)) {
                preference.setChecked(checked);
            }
        }
    }

    public int getRcsStrategy(int oldStrategy) {
        if (!HwRcsFeatureEnabler.isRcsEnabled()) {
            return oldStrategy;
        }
        if (this.mPrefBlacklistRcs.isChecked()) {
            oldStrategy |= StrategyId.BLOCK_BLACKLIST.getValue();
        }
        return oldStrategy;
    }
}
