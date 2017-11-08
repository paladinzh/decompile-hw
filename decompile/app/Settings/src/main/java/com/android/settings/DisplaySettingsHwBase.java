package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.internal.view.RotationPolicy;
import com.android.internal.view.RotationPolicy.RotationPolicyListener;
import com.huawei.cust.HwCustUtils;
import java.util.List;

public class DisplaySettingsHwBase extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    protected static final boolean SUPPORT_COLOR_SETTINGS = SystemProperties.getBoolean("ro.config.colorTemperature_K3", false);
    protected static final boolean SUPPORT_COLOR_WHEEL = SystemProperties.getBoolean("ro.config.colorTemperature_3d", false);
    private static boolean mUserToolbox;
    private CustomSwitchPreference mAccelerometer;
    private ContentObserver mCurrentTimeoutObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            DisplaySettingsHwBase.this.updateCurrentTimeout();
        }
    };
    private Preference mFontStylePreference;
    private HwCustDisplaySettingsHwBase mHwCustDisplaySettingsHwBase;
    private boolean mRegistered = false;
    private BroadcastReceiver mRotateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent in) {
            if ("huawei.intent.ROTATION_CHANGED".equals(in.getAction())) {
                DisplaySettingsHwBase.this.updateState();
            }
        }
    };
    private final RotationPolicyListener mRotationPolicyListener = new RotationPolicyListener() {
        public void onChange() {
            DisplaySettingsHwBase.this.updateAccelerometerRotationCheckbox();
        }
    };
    protected TimeoutListPreference mScreenTimeoutPreference;

    static {
        boolean z = true;
        if (!SystemProperties.getBoolean("ro.config.hw_toolbox", true)) {
            z = false;
        } else if (SystemProperties.getInt("vold.decrypt", 0) == 1) {
            z = false;
        }
        mUserToolbox = z;
    }

    protected void updateAccelerometerRotationCheckbox() {
        if (!(getActivity() == null || this.mAccelerometer == null)) {
            boolean z;
            CustomSwitchPreference customSwitchPreference = this.mAccelerometer;
            if (RotationPolicy.isRotationLocked(getActivity())) {
                z = false;
            } else {
                z = true;
            }
            customSwitchPreference.setChecked(z);
        }
    }

    protected void updateState() {
    }

    protected void updateTimeoutPreferenceDescription(long currentTimeout) {
    }

    protected void updateCurrentTimeout() {
        if (this.mScreenTimeoutPreference != null) {
            long currentTimeout = System.getLong(getContentResolver(), "screen_off_timeout", 30000);
            updateTimeoutPreferenceDescription(currentTimeout);
            this.mScreenTimeoutPreference.setValue(String.valueOf(currentTimeout));
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230781);
        this.mHwCustDisplaySettingsHwBase = (HwCustDisplaySettingsHwBase) HwCustUtils.createObj(HwCustDisplaySettingsHwBase.class, new Object[]{getActivity()});
        List<ResolveInfo> homes = getPackageManager().queryIntentActivities(new Intent("com.huawei.launcher.wallpaper_setting"), 0);
        if (Utils.onIsMultiPane(getActivity()) || !(homes == null || homes.size() == 0)) {
            Preference mGoogleWallpaper = findPreference("google_wallpaper");
            if (mGoogleWallpaper != null) {
                getPreferenceScreen().removePreference(mGoogleWallpaper);
            }
        } else {
            Preference pref = findPreference("wallpaper");
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
        }
        this.mFontStylePreference = findPreference("font_style");
        if (this.mFontStylePreference != null && (!Utils.hasIntentActivity(getPackageManager(), this.mFontStylePreference.getIntent()) || (this.mHwCustDisplaySettingsHwBase != null && this.mHwCustDisplaySettingsHwBase.hideSettingsFontStyle()))) {
            removePreference("font_style");
        }
        removeWifiDisplayIfDisabled();
        this.mAccelerometer = (CustomSwitchPreference) findPreference("accelerometer");
        this.mAccelerometer.setOnPreferenceChangeListener(this);
        this.mAccelerometer.setPersistent(false);
        Preference displayModePreference = findPreference("display_mode");
        if (displayModePreference != null) {
            if (Utils.isDisplayModeFeatureSupportedEx(getActivity())) {
                updateDisplayModeSummary(displayModePreference);
            } else {
                getPreferenceScreen().removePreference(displayModePreference);
            }
        }
        chooseDisplayMode(displayModePreference);
    }

    private void chooseDisplayMode(Preference displayModePreference) {
        Preference autoDisplayModePreference = findPreference("screen_zoom");
        if (SystemProperties.getBoolean("config.auto_display_mode", false)) {
            if (displayModePreference != null) {
                getPreferenceScreen().removePreference(displayModePreference);
            }
        } else if (autoDisplayModePreference != null) {
            getPreferenceScreen().removePreference(autoDisplayModePreference);
        }
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void onResume() {
        super.onResume();
        RotationPolicy.registerRotationPolicyListener(getActivity(), this.mRotationPolicyListener);
        getContentResolver().registerContentObserver(System.getUriFor("screen_off_timeout"), true, this.mCurrentTimeoutObserver);
        if (mUserToolbox && !this.mRegistered) {
            this.mRegistered = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("huawei.intent.ROTATION_CHANGED");
            getActivity().registerReceiver(this.mRotateReceiver, filter);
        }
    }

    public void onPause() {
        super.onPause();
        RotationPolicy.unregisterRotationPolicyListener(getActivity(), this.mRotationPolicyListener);
        ItemUseStat.getInstance().cacheData(getActivity());
        getContentResolver().unregisterContentObserver(this.mCurrentTimeoutObserver);
        if (mUserToolbox && this.mRegistered) {
            getActivity().unregisterReceiver(this.mRotateReceiver);
            this.mRegistered = false;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        String key = preference.getKey();
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, objValue);
        if ("accelerometer".equals(key)) {
            boolean z;
            Boolean value = (Boolean) objValue;
            Context activity = getActivity();
            if (value.booleanValue()) {
                z = false;
            } else {
                z = true;
            }
            RotationPolicy.setRotationLockForAccessibility(activity, z);
        }
        return true;
    }

    protected void updateFontStyleSummary() {
        new ThemeQueryHandler(getContentResolver(), this.mFontStylePreference).startQuery(0, null, ThemeQueryHandler.URI_MODULE_INFO, new String[]{"display_name_en", "display_name_zh"}, "module_name=?", new String[]{"fonts"}, null);
    }

    protected void removeWifiDisplayIfDisabled() {
        if (!getResources().getBoolean(17956985) || Utils.atLestOneSharingAppExist(getActivity())) {
            MLog.d("DisplaySettingsHwBase", "config_enableWifiDisplay=false, wifi display not supported");
            Preference wifiDisplayPreference = findPreference("wifi_display");
            if (wifiDisplayPreference != null) {
                getPreferenceScreen().removePreference(wifiDisplayPreference);
            }
        }
    }

    protected int getDreamSettingsSummaryResId() {
        if (1 == Secure.getInt(getContentResolver(), "screensaver_enabled", 0)) {
            return 2131627596;
        }
        return 2131627597;
    }

    protected void updateDisplayModeSummary(Preference displayModePreference) {
        if (displayModePreference != null) {
            displayModePreference.setSummary(getResources().getStringArray(2131361943)[Utils.getCurrentDisplayModeIndex(getContentResolver(), getActivity())]);
        }
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
