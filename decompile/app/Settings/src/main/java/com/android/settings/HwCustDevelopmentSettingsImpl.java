package com.android.settings;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.SettingsEx.Systemex;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;

public class HwCustDevelopmentSettingsImpl extends HwCustDevelopmentSettings {
    private static final String ADB_INSTALL_NEED_CONFIRM_KEY = "adb_install_need_confirm";
    private static final String DEBUG_DEBUGGING_CATEGORY_KEY = "debug_debugging_category";
    private static final String DEBUG_DRAWING_CATEGORY_KEY = "debug_drawing_category";
    private static final String DEBUG_HW_HARDWARE_CATEGORY_KEY = "debug_hw_drawing_category";
    private static final String DEBUG_MONITORING_CATEGORY_KEY = "debug_monitoring_category";
    private static final String DEFAULT_DISABLE_DEVELOPMENT_SETTINGS = "default_developmentsettings_disable";
    private static final String FORCE_HARDWARE_UI_KEY = "force_hw_ui";
    private static final String HW_DEVELOPMENT_ADDF_SHOW = "hw_development_draw_disable";
    private static final String LOCAL_BACKUP_PASSWORD_KEY = "local_backup_password";
    private static final String SHOW_SCREEN_UPDATES_KEY = "show_screen_updates";
    private static final String STRICT_MODE_KEY = "strict_mode";
    private static final String WINDOW_ANIMATION_SCALE_KEY = "window_animation_scale";
    private DevelopmentSettings mDevelopmentSettings = null;

    public HwCustDevelopmentSettingsImpl(DevelopmentSettings developmentSettings) {
        super(developmentSettings);
        this.mDevelopmentSettings = developmentSettings;
    }

    public void disableAllDeveloperSettings() {
        if (this.mDevelopmentSettings != null && Systemex.getInt(this.mDevelopmentSettings.getContentResolver(), DEFAULT_DISABLE_DEVELOPMENT_SETTINGS, 0) == 1) {
            this.mDevelopmentSettings.resetDangerousOptions();
            this.mDevelopmentSettings.uncheckEnabledSwitch();
            this.mDevelopmentSettings.setLastEnabledState(false);
            this.mDevelopmentSettings.setPrefsEnabledState(false);
            Global.putInt(this.mDevelopmentSettings.getActivity().getContentResolver(), "development_settings_enabled", 0);
            Systemex.putInt(this.mDevelopmentSettings.getContentResolver(), DEFAULT_DISABLE_DEVELOPMENT_SETTINGS, 0);
        }
    }

    public boolean isShowADDF() {
        if (this.mDevelopmentSettings == null || Systemex.getInt(this.mDevelopmentSettings.getContentResolver(), HW_DEVELOPMENT_ADDF_SHOW, 0) != 1) {
            return true;
        }
        return false;
    }

    public void updateCustPreference(Context context) {
        if (this.mDevelopmentSettings != null) {
            if (SystemProperties.getBoolean("ro.config.hwRemoveADBMonitor", false)) {
                PreferenceGroup debugDebuggingCategory = (PreferenceGroup) this.mDevelopmentSettings.findPreference(DEBUG_DEBUGGING_CATEGORY_KEY);
                Preference adbInstallNeedConfirm = this.mDevelopmentSettings.findPreference(ADB_INSTALL_NEED_CONFIRM_KEY);
                if (!(debugDebuggingCategory == null || adbInstallNeedConfirm == null)) {
                    debugDebuggingCategory.removePreference(adbInstallNeedConfirm);
                }
            }
            if (1 == Global.getInt(context.getContentResolver(), "hw_development_items_hide", 0)) {
                PreferenceScreen mPassword = (PreferenceScreen) this.mDevelopmentSettings.findPreference(LOCAL_BACKUP_PASSWORD_KEY);
                PreferenceGroup monitoringCategory = (PreferenceGroup) this.mDevelopmentSettings.findPreference(DEBUG_MONITORING_CATEGORY_KEY);
                PreferenceGroup drawingCategory = (PreferenceGroup) this.mDevelopmentSettings.findPreference(DEBUG_DRAWING_CATEGORY_KEY);
                PreferenceGroup hardwareCategory = (PreferenceGroup) this.mDevelopmentSettings.findPreference(DEBUG_HW_HARDWARE_CATEGORY_KEY);
                TwoStatePreference mShowScreenUpdates = (TwoStatePreference) this.mDevelopmentSettings.findPreference(SHOW_SCREEN_UPDATES_KEY);
                TwoStatePreference mStrictMode = (TwoStatePreference) this.mDevelopmentSettings.findPreference(STRICT_MODE_KEY);
                ListPreference mWindowAnimationScale = (ListPreference) this.mDevelopmentSettings.findPreference(WINDOW_ANIMATION_SCALE_KEY);
                TwoStatePreference mForceHardwareUi = (TwoStatePreference) this.mDevelopmentSettings.findPreference(FORCE_HARDWARE_UI_KEY);
                if (mPassword != null) {
                    this.mDevelopmentSettings.getPreferenceScreen().removePreference(mPassword);
                }
                if (!(drawingCategory == null || mShowScreenUpdates == null)) {
                    drawingCategory.removePreference(mShowScreenUpdates);
                }
                if (!(drawingCategory == null || mWindowAnimationScale == null)) {
                    drawingCategory.removePreference(mWindowAnimationScale);
                }
                if (!(monitoringCategory == null || mStrictMode == null)) {
                    monitoringCategory.removePreference(mStrictMode);
                }
                if (!(hardwareCategory == null || mForceHardwareUi == null)) {
                    hardwareCategory.removePreference(mForceHardwareUi);
                }
            }
        }
    }
}
