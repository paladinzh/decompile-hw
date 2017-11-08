package com.android.settings.notification;

import android.app.NotificationManager;
import android.app.NotificationManager.Policy;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

public class ZenModeSettings extends ZenModeSettingsBase {
    private Policy mPolicy;
    private Preference mPrioritySettings;
    private Preference mVisualSettings;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230955);
        PreferenceScreen root = getPreferenceScreen();
        this.mPrioritySettings = root.findPreference("priority_settings");
        this.mVisualSettings = root.findPreference("visual_interruptions_settings");
        this.mPolicy = NotificationManager.from(this.mContext).getNotificationPolicy();
    }

    public void onResume() {
        super.onResume();
        if (!isUiRestricted()) {
        }
    }

    protected int getMetricsCategory() {
        return 76;
    }

    protected void onZenModeChanged() {
        updateControls();
    }

    protected void onZenModeConfigChanged() {
        this.mPolicy = NotificationManager.from(this.mContext).getNotificationPolicy();
        updateControls();
    }

    private void updateControls() {
        updatePrioritySettingsSummary();
        updateVisualSettingsSummary();
    }

    private void updatePrioritySettingsSummary() {
        CharSequence s = appendLowercase(appendLowercase(getResources().getString(2131626822), isCategoryEnabled(this.mPolicy, 1), 2131626823), isCategoryEnabled(this.mPolicy, 2), 2131626824);
        if (isCategoryEnabled(this.mPolicy, 4)) {
            if (this.mPolicy.priorityMessageSenders == 0) {
                s = appendLowercase(s, true, 2131626816);
            } else {
                s = appendLowercase(s, true, 2131626817);
            }
        }
        if (isCategoryEnabled(this.mPolicy, 8)) {
            if (this.mPolicy.priorityCallSenders == 0) {
                s = appendLowercase(s, true, 2131626825);
            } else {
                s = appendLowercase(s, true, 2131626826);
            }
        } else if (isCategoryEnabled(this.mPolicy, 16)) {
            s = appendLowercase(s, true, 2131626827);
        }
        this.mPrioritySettings.setSummary(s);
    }

    private void updateVisualSettingsSummary() {
        CharSequence s = getString(2131626844);
        if (isEffectSuppressed(2) && isEffectSuppressed(1)) {
            s = getString(2131626847);
        } else if (isEffectSuppressed(2)) {
            s = getString(2131626845);
        } else if (isEffectSuppressed(1)) {
            s = getString(2131626846);
        }
        this.mVisualSettings.setSummary(s);
    }

    private boolean isEffectSuppressed(int effect) {
        return (this.mPolicy.suppressedVisualEffects & effect) != 0;
    }

    private boolean isCategoryEnabled(Policy policy, int categoryType) {
        return (policy.priorityCategories & categoryType) != 0;
    }

    private String appendLowercase(String s, boolean condition, int resId) {
        if (!condition) {
            return s;
        }
        return getResources().getString(2131625669, new Object[]{s, getResources().getString(resId).toLowerCase()});
    }

    protected int getHelpResource() {
        return 2131626527;
    }
}
