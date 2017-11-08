package com.android.settings;

import android.app.ActivityManager;
import android.app.UiModeManager;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.view.RotationPolicy;
import com.android.settings.accessibility.ToggleFontSizePreferenceFragment;
import com.android.settings.colortemper.ColorTemperatureSettingsPreference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.HwCustSearchIndexProvider;
import com.android.settings.search.Index;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.utils.PowerModeReceiver;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreference;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.List;

public class DisplaySettings extends DisplaySettingsHwBase implements OnPreferenceChangeListener, Indexable {
    private static final int[] SCREEN_TIMEOUT = new int[]{15, 30, 1, 2, 5, 10};
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            ArrayList<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = 2131230781;
            result.add(sir);
            DisplaySettings.mHwCustSearchIndexProvider.addDisplayXmlResourcesToIndex(context, result);
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            HwCustDisplaySettingsHwBase custDisplaySettingsHwBase = (HwCustDisplaySettingsHwBase) HwCustUtils.createObj(HwCustDisplaySettingsHwBase.class, new Object[]{context});
            ArrayList<String> result = new ArrayList();
            if (!context.getResources().getBoolean(17956973)) {
                result.add("screensaver");
            }
            if (!DisplaySettings.isAutomaticBrightnessAvailable(context.getResources())) {
                result.add("brightness");
            }
            if (!DisplaySettings.isLiftToWakeAvailable(context)) {
                result.add("lift_to_wake");
            }
            if (!DisplaySettings.isDozeAvailable(context)) {
                result.add("doze");
            }
            if (!RotationPolicy.isRotationLockToggleVisible(context)) {
                result.add("accelerometer");
            }
            Intent intent = new Intent("com.huawei.launcher.wallpaper_setting");
            if (!(Utils.onIsMultiPane(context) || Utils.hasIntentActivity(context.getPackageManager(), intent))) {
                result.add("wallpaper");
            }
            if (!Utils.hasIntentActivity(context.getPackageManager(), new Intent("huawei.intent.action.FONT_STYLE")) || (custDisplaySettingsHwBase != null && custDisplaySettingsHwBase.hideSettingsFontStyle())) {
                result.add("font_style");
            }
            if (!context.getResources().getBoolean(17956985) || Utils.atLestOneSharingAppExist(context)) {
                result.add("wifi_display");
            }
            if (!Utils.isDisplayModeFeatureSupportedEx(context)) {
                result.add("display_mode");
            }
            if (!SystemProperties.getBoolean("config.auto_display_mode", false)) {
                result.add("screen_zoom");
            }
            if (!DisplaySettings.isTapToWakeAvailable(context.getResources())) {
                result.add("tap_to_wake");
            }
            if (!DisplaySettings.isCameraGestureAvailable(context.getResources())) {
                result.add("camera_gesture");
            }
            if (!DisplaySettings.isVrDisplayModeAvailable(context)) {
                result.add("vr_display_pref");
            }
            if (!DisplaySettings.SUPPORT_COLOR_SETTINGS) {
                result.add("color_temperature");
                result.add("color_temperature_rgb");
            } else if (DisplaySettings.SUPPORT_COLOR_WHEEL) {
                result.add("color_temperature");
            } else {
                result.add("color_temperature_rgb");
            }
            result.add("eyes_protection");
            if (!DisplaySettings.isSupportColorMode()) {
                result.add("color_mode");
            }
            DisplaySettings.mHwCustSearchIndexProvider.addDisplayNonIndexableKeys(context, result);
            return result;
        }
    };
    private static final int SMART_DISPLAY_ENABLE = SystemProperties.getInt("ro.config.hw_eyes_protection", 1);
    private static HwCustSearchIndexProvider mHwCustSearchIndexProvider = ((HwCustSearchIndexProvider) HwCustUtils.createObj(HwCustSearchIndexProvider.class, new Object[0]));
    private SwitchPreference mCameraGesturePreference;
    private ColorTemperatureSettingsPreference mColorTempPref;
    private Context mContext;
    private final Configuration mCurConfig = new Configuration();
    private CustomSwitchPreference mDozePreference;
    private ContentObserver mEyeProtectionModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            DisplaySettings.this.updateEyesProtectionPreference();
        }
    };
    private TwoSummaryPreference mEyesProtectionPreference;
    private Preference mFontSizePref;
    private HwCustDisplaySettings mHwCustDisplaySettings;
    private CustomSwitchPreference mLiftToWakePreference;
    private ListPreference mNightModePreference;
    private PowerModeReceiver mPowerModeReceiver;
    private Preference mScreenSaverPreference;
    private TimeoutListPreference mScreenTimeoutPreference;
    private CustomSwitchPreference mTapToWakePreference;
    private Preference mWallPaper;
    private String[] screen_timeout_values = new String[]{"screen_timeout_15_seconds", "screen_timeout_30_seconds", "screen_timeout_1_minute", "screen_timeout_2_minutes", "screen_timeout_5_minutes", "screen_timeout_10_minutes", "screen_timeout_30_minutes"};

    protected int getMetricsCategory() {
        return 46;
    }

    private CharSequence[] buildTimeoutEntries(Context context) {
        CharSequence[] timeoutEntries = new CharSequence[6];
        timeoutEntries[0] = String.format(getResources().getString(2131628200, new Object[]{Integer.valueOf(SCREEN_TIMEOUT[0])}), new Object[0]);
        timeoutEntries[1] = String.format(getResources().getString(2131628201, new Object[]{Integer.valueOf(SCREEN_TIMEOUT[1])}), new Object[0]);
        timeoutEntries[2] = String.format(getResources().getString(2131628202, new Object[]{Integer.valueOf(SCREEN_TIMEOUT[2])}), new Object[0]);
        timeoutEntries[3] = String.format(getResources().getString(2131628203, new Object[]{Integer.valueOf(SCREEN_TIMEOUT[3])}), new Object[0]);
        timeoutEntries[4] = String.format(getResources().getString(2131628204, new Object[]{Integer.valueOf(SCREEN_TIMEOUT[4])}), new Object[0]);
        timeoutEntries[5] = String.format(getResources().getString(2131628205, new Object[]{Integer.valueOf(SCREEN_TIMEOUT[5])}), new Object[0]);
        return timeoutEntries;
    }

    public void onCreate(Bundle savedInstanceState) {
        long currentTimeout;
        super.onCreate(savedInstanceState);
        final Context activity = getActivity();
        ContentResolver resolver = activity.getContentResolver();
        this.mHwCustDisplaySettings = (HwCustDisplaySettings) HwCustUtils.createObj(HwCustDisplaySettings.class, new Object[]{this});
        this.mScreenSaverPreference = findPreference("screensaver");
        if (this.mScreenSaverPreference == null || getResources().getBoolean(17956973)) {
            if ("factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            }
            this.mWallPaper = findPreference("wallpaper");
            this.mScreenTimeoutPreference = (TimeoutListPreference) findPreference("screen_timeout");
            this.mScreenTimeoutPreference.setNetherSummary(getResources().getString(2131628156));
            this.mScreenTimeoutPreference.setEntries(buildTimeoutEntries(activity));
            currentTimeout = System.getLong(resolver, "screen_off_timeout", 30000);
            if (this.mHwCustDisplaySettings != null) {
                this.mHwCustDisplaySettings.updateScreenTimeoutPreference(this.mScreenTimeoutPreference);
            }
            this.mScreenTimeoutPreference.setValue(String.valueOf(currentTimeout));
            this.mScreenTimeoutPreference.setOnPreferenceChangeListener(this);
            disableUnusableTimeouts(this.mScreenTimeoutPreference);
            updateTimeoutPreferenceDescription(currentTimeout);
            this.mFontSizePref = findPreference("font_size");
            if (isLiftToWakeAvailable(activity)) {
                removePreference("lift_to_wake");
            } else {
                this.mLiftToWakePreference = (CustomSwitchPreference) findPreference("lift_to_wake");
                this.mLiftToWakePreference.setOnPreferenceChangeListener(this);
            }
            if (isDozeAvailable(activity)) {
                removePreference("doze");
            } else {
                this.mDozePreference = (CustomSwitchPreference) findPreference("doze");
                this.mDozePreference.setOnPreferenceChangeListener(this);
            }
            if (isTapToWakeAvailable(getResources())) {
                removePreference("tap_to_wake");
            } else {
                this.mTapToWakePreference = (CustomSwitchPreference) findPreference("tap_to_wake");
                this.mTapToWakePreference.setOnPreferenceChangeListener(this);
            }
            if (isCameraGestureAvailable(getResources())) {
                removePreference("camera_gesture");
            } else {
                this.mCameraGesturePreference = (SwitchPreference) findPreference("camera_gesture");
                this.mCameraGesturePreference.setOnPreferenceChangeListener(this);
            }
            if (isVrDisplayModeAvailable(activity)) {
                removePreference("vr_display_pref");
            } else {
                DropDownPreference vrDisplayPref = (DropDownPreference) findPreference("vr_display_pref");
                vrDisplayPref.setEntries(new CharSequence[]{activity.getString(2131626764), activity.getString(2131626765)});
                vrDisplayPref.setEntryValues(new CharSequence[]{"0", "1"});
                Context c = activity;
                vrDisplayPref.setValueIndex(Secure.getIntForUser(activity.getContentResolver(), "vr_display_mode", 0, ActivityManager.getCurrentUser()));
                vrDisplayPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (!Secure.putIntForUser(activity.getContentResolver(), "vr_display_mode", Integer.parseInt((String) newValue), ActivityManager.getCurrentUser())) {
                            Log.e("DisplaySettings", "Could not change setting for vr_display_mode");
                        }
                        return true;
                    }
                });
            }
            this.mNightModePreference = (ListPreference) findPreference("night_mode");
            if (this.mNightModePreference != null) {
                this.mNightModePreference.setValue(String.valueOf(((UiModeManager) getSystemService("uimode")).getNightMode()));
                this.mNightModePreference.setOnPreferenceChangeListener(this);
            }
            if (this.mHwCustDisplaySettings != null) {
                this.mHwCustDisplaySettings.updateCustPreference(getActivity());
            }
            if (!SUPPORT_COLOR_SETTINGS) {
                removePreference("color_temperature");
                removePreference("category_screen", "color_temperature_rgb");
            } else if (SUPPORT_COLOR_WHEEL) {
                removePreference("category_screen", "color_temperature_rgb");
            } else {
                removePreference("color_temperature");
            }
            this.mColorTempPref = (ColorTemperatureSettingsPreference) findPreference("color_temperature_rgb");
            if (SMART_DISPLAY_ENABLE == 0) {
                this.mEyesProtectionPreference = (TwoSummaryPreference) findPreference("eyes_protection");
                this.mEyesProtectionPreference.setNetherSummary(getResources().getString(2131628538));
            } else {
                removePreference("category_screen", "eyes_protection");
            }
            if (!isSupportColorMode()) {
                removePreference("category_screen", "color_mode");
            }
            Index.getInstance(getActivity()).updateFromClassNameResource(DisplaySettings.class.getName(), true, true);
            setHasOptionsMenu(true);
            this.mContext = getContext();
        }
        getPreferenceScreen().removePreference(this.mScreenSaverPreference);
        this.mWallPaper = findPreference("wallpaper");
        this.mScreenTimeoutPreference = (TimeoutListPreference) findPreference("screen_timeout");
        this.mScreenTimeoutPreference.setNetherSummary(getResources().getString(2131628156));
        this.mScreenTimeoutPreference.setEntries(buildTimeoutEntries(activity));
        currentTimeout = System.getLong(resolver, "screen_off_timeout", 30000);
        if (this.mHwCustDisplaySettings != null) {
            this.mHwCustDisplaySettings.updateScreenTimeoutPreference(this.mScreenTimeoutPreference);
        }
        this.mScreenTimeoutPreference.setValue(String.valueOf(currentTimeout));
        this.mScreenTimeoutPreference.setOnPreferenceChangeListener(this);
        disableUnusableTimeouts(this.mScreenTimeoutPreference);
        updateTimeoutPreferenceDescription(currentTimeout);
        this.mFontSizePref = findPreference("font_size");
        if (isLiftToWakeAvailable(activity)) {
            removePreference("lift_to_wake");
        } else {
            this.mLiftToWakePreference = (CustomSwitchPreference) findPreference("lift_to_wake");
            this.mLiftToWakePreference.setOnPreferenceChangeListener(this);
        }
        if (isDozeAvailable(activity)) {
            removePreference("doze");
        } else {
            this.mDozePreference = (CustomSwitchPreference) findPreference("doze");
            this.mDozePreference.setOnPreferenceChangeListener(this);
        }
        if (isTapToWakeAvailable(getResources())) {
            removePreference("tap_to_wake");
        } else {
            this.mTapToWakePreference = (CustomSwitchPreference) findPreference("tap_to_wake");
            this.mTapToWakePreference.setOnPreferenceChangeListener(this);
        }
        if (isCameraGestureAvailable(getResources())) {
            removePreference("camera_gesture");
        } else {
            this.mCameraGesturePreference = (SwitchPreference) findPreference("camera_gesture");
            this.mCameraGesturePreference.setOnPreferenceChangeListener(this);
        }
        if (isVrDisplayModeAvailable(activity)) {
            removePreference("vr_display_pref");
        } else {
            DropDownPreference vrDisplayPref2 = (DropDownPreference) findPreference("vr_display_pref");
            vrDisplayPref2.setEntries(new CharSequence[]{activity.getString(2131626764), activity.getString(2131626765)});
            vrDisplayPref2.setEntryValues(new CharSequence[]{"0", "1"});
            Context c2 = activity;
            vrDisplayPref2.setValueIndex(Secure.getIntForUser(activity.getContentResolver(), "vr_display_mode", 0, ActivityManager.getCurrentUser()));
            vrDisplayPref2.setOnPreferenceChangeListener(/* anonymous class already generated */);
        }
        this.mNightModePreference = (ListPreference) findPreference("night_mode");
        if (this.mNightModePreference != null) {
            this.mNightModePreference.setValue(String.valueOf(((UiModeManager) getSystemService("uimode")).getNightMode()));
            this.mNightModePreference.setOnPreferenceChangeListener(this);
        }
        if (this.mHwCustDisplaySettings != null) {
            this.mHwCustDisplaySettings.updateCustPreference(getActivity());
        }
        if (!SUPPORT_COLOR_SETTINGS) {
            removePreference("color_temperature");
            removePreference("category_screen", "color_temperature_rgb");
        } else if (SUPPORT_COLOR_WHEEL) {
            removePreference("category_screen", "color_temperature_rgb");
        } else {
            removePreference("color_temperature");
        }
        this.mColorTempPref = (ColorTemperatureSettingsPreference) findPreference("color_temperature_rgb");
        if (SMART_DISPLAY_ENABLE == 0) {
            removePreference("category_screen", "eyes_protection");
        } else {
            this.mEyesProtectionPreference = (TwoSummaryPreference) findPreference("eyes_protection");
            this.mEyesProtectionPreference.setNetherSummary(getResources().getString(2131628538));
        }
        if (isSupportColorMode()) {
            removePreference("category_screen", "color_mode");
        }
        Index.getInstance(getActivity()).updateFromClassNameResource(DisplaySettings.class.getName(), true, true);
        setHasOptionsMenu(true);
        this.mContext = getContext();
    }

    public void applyLowPowerMode(boolean isLowPowerMode) {
        super.applyLowPowerMode(isLowPowerMode);
        if (isLowPowerMode) {
            this.mScreenTimeoutPreference.setEnabled(false);
        } else if (!this.mScreenTimeoutPreference.isDisabledByAdmin()) {
            this.mScreenTimeoutPreference.setEnabled(true);
        }
    }

    private static boolean isSupportColorMode() {
        boolean isSupportColorMode = false;
        try {
            Class classz = Class.forName("com.huawei.android.hwsmartdisplay.HwSmartDisplay");
            Object instance = classz.newInstance();
            isSupportColorMode = ((Boolean) classz.getDeclaredMethod("isFeatureSupported", new Class[]{Integer.TYPE}).invoke(instance, new Object[]{Integer.valueOf(2)})).booleanValue();
            Log.d("DisplaySettings", " isSupportColorMode: " + isSupportColorMode);
            return isSupportColorMode;
        } catch (RuntimeException ex) {
            Log.e("DisplaySettings", ": reflection exception is " + ex.getMessage());
            return isSupportColorMode;
        } catch (Exception ex2) {
            Log.e("DisplaySettings", ": Exception happend when check if color mode feature supported. Message is: " + ex2.getMessage());
            return isSupportColorMode;
        }
    }

    private static boolean isLiftToWakeAvailable(Context context) {
        SensorManager sensors = (SensorManager) context.getSystemService("sensor");
        if (sensors == null || sensors.getDefaultSensor(23) == null) {
            return false;
        }
        return true;
    }

    private static boolean isDozeAvailable(Context context) {
        String name = Build.IS_DEBUGGABLE ? SystemProperties.get("debug.doze.component") : null;
        if (TextUtils.isEmpty(name)) {
            name = context.getResources().getString(17039450);
        }
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        return true;
    }

    private static boolean isTapToWakeAvailable(Resources res) {
        return res.getBoolean(17957028);
    }

    private static boolean isAutomaticBrightnessAvailable(Resources res) {
        return res.getBoolean(17956900);
    }

    private static boolean isCameraGestureAvailable(Resources res) {
        boolean configSet;
        if (res.getInteger(17694883) != -1) {
            configSet = true;
        } else {
            configSet = false;
        }
        if (!configSet || SystemProperties.getBoolean("gesture.disable_camera_launch", false)) {
            return false;
        }
        return true;
    }

    private static boolean isVrDisplayModeAvailable(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.vr.high_performance");
    }

    protected void updateTimeoutPreferenceDescription(long currentTimeout) {
        if (this.mScreenTimeoutPreference != null) {
            String summary;
            TimeoutListPreference preference = this.mScreenTimeoutPreference;
            if (preference.isDisabledByAdmin()) {
                summary = getString(2131627106);
                preference.setEnabled(false);
            } else if (currentTimeout < 0) {
                summary = "";
            } else if (currentTimeout >= 2147483647L) {
                summary = "";
            } else {
                CharSequence[] entries = preference.getEntries();
                CharSequence[] values = preference.getEntryValues();
                if (entries == null || entries.length == 0) {
                    summary = "";
                } else {
                    int best = 0;
                    for (int i = 0; i < values.length; i++) {
                        if (currentTimeout >= Long.parseLong(values[i].toString())) {
                            best = i;
                        }
                    }
                    summary = getString(2131625160, new Object[]{entries[best]});
                }
            }
            preference.setSummary(summary);
        }
    }

    private void disableUnusableTimeouts(ListPreference screenTimeoutPreference) {
        DevicePolicyManager dpm = (DevicePolicyManager) getActivity().getSystemService("device_policy");
        long maxTimeout = dpm != null ? dpm.getMaximumTimeToLock(null) : 0;
        if (maxTimeout != 0) {
            boolean z;
            CharSequence[] entries = screenTimeoutPreference.getEntries();
            CharSequence[] values = screenTimeoutPreference.getEntryValues();
            ArrayList<CharSequence> revisedEntries = new ArrayList();
            ArrayList<CharSequence> revisedValues = new ArrayList();
            long nearMaxTimeout = 60000;
            for (int i = 0; i < values.length; i++) {
                long timeout = Long.parseLong(values[i].toString());
                if (timeout <= maxTimeout) {
                    revisedEntries.add(entries[i]);
                    revisedValues.add(values[i]);
                    nearMaxTimeout = timeout;
                }
            }
            if (this.mHwCustDisplaySettings != null) {
                this.mHwCustDisplaySettings.changeScreenOffTimeoutArrays(revisedEntries, revisedValues);
                this.mHwCustDisplaySettings.setCurrentScreenOffTimeoutValue();
            }
            if (((long) System.getInt(getContentResolver(), "screen_off_timeout", 30000)) >= maxTimeout) {
                System.putInt(getContentResolver(), "screen_off_timeout", (int) nearMaxTimeout);
            }
            if (revisedEntries.size() > 0) {
                z = true;
            } else {
                z = false;
            }
            screenTimeoutPreference.setEnabled(z);
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mHwCustDisplaySettings != null) {
            this.mHwCustDisplaySettings.onResume();
        }
        getContentResolver().registerContentObserver(System.getUriFor("eyes_protection_mode"), true, this.mEyeProtectionModeObserver);
        updateEyesProtectionPreference();
        updateState();
        long currentTimeout = System.getLong(getActivity().getContentResolver(), "screen_off_timeout", 30000);
        this.mScreenTimeoutPreference.setValue(String.valueOf(currentTimeout));
        this.mScreenTimeoutPreference.setOnPreferenceChangeListener(this);
        DevicePolicyManager dpm = (DevicePolicyManager) getActivity().getSystemService("device_policy");
        if (dpm != null) {
            EnforcedAdmin admin = RestrictedLockUtils.checkIfMaximumTimeToLockIsSet(getActivity());
            this.mScreenTimeoutPreference.removeUnusableTimeouts(dpm.getMaximumTimeToLockForUserAndProfiles(UserHandle.myUserId()), admin);
        }
        updateTimeoutPreferenceDescription(currentTimeout);
        disablePreferenceIfManaged("wallpaper", "no_set_wallpaper");
        disablePreferenceIfManaged("google_wallpaper", "no_set_wallpaper");
        if (this.mContext != null) {
            applyLowPowerMode(Utils.isLowPowerMode(this.mContext));
        }
        if (this.mPowerModeReceiver == null && this.mContext != null) {
            this.mPowerModeReceiver = new PowerModeReceiver(this);
            this.mPowerModeReceiver.registerReceiver(this.mContext);
        }
    }

    private void updateEyesProtectionPreference() {
        if (this.mEyesProtectionPreference != null) {
            boolean isModeOn;
            if (System.getIntForUser(getContentResolver(), "eyes_protection_mode", 0, UserHandle.myUserId()) != 0) {
                isModeOn = true;
            } else {
                isModeOn = false;
            }
            if (isModeOn) {
                this.mEyesProtectionPreference.setSummary(2131627698);
            } else {
                this.mEyesProtectionPreference.setSummary(2131627699);
            }
            if (this.mColorTempPref != null) {
                if (isModeOn) {
                    this.mColorTempPref.setEnabled(false);
                    this.mColorTempPref.setNetherSummary(getResources().getString(2131628547));
                } else {
                    this.mColorTempPref.setEnabled(true);
                    this.mColorTempPref.setNetherSummary("");
                }
            }
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mHwCustDisplaySettings != null) {
            this.mHwCustDisplaySettings.onPause();
        }
        if (this.mPowerModeReceiver != null) {
            this.mPowerModeReceiver.unregisterReceiver(this.mContext);
            this.mPowerModeReceiver = null;
        }
        if (this.mEyeProtectionModeObserver != null) {
            getContentResolver().unregisterContentObserver(this.mEyeProtectionModeObserver);
        }
    }

    protected void updateState() {
        int value;
        boolean z;
        boolean z2 = true;
        updateAccelerometerRotationCheckbox();
        updateFontSizeSummary();
        updateScreenSaverSummary();
        updateCurrentTimeout();
        updateFontStyleSummary();
        if (this.mLiftToWakePreference != null) {
            value = Secure.getInt(getContentResolver(), "wake_gesture_enabled", 0);
            CustomSwitchPreference customSwitchPreference = this.mLiftToWakePreference;
            if (value != 0) {
                z = true;
            } else {
                z = false;
            }
            customSwitchPreference.setChecked(z);
        }
        if (this.mDozePreference != null) {
            value = Secure.getInt(getContentResolver(), "doze_enabled", 1);
            customSwitchPreference = this.mDozePreference;
            if (value != 0) {
                z = true;
            } else {
                z = false;
            }
            customSwitchPreference.setChecked(z);
        }
        if (this.mTapToWakePreference != null) {
            value = Secure.getInt(getContentResolver(), "double_tap_to_wake", 0);
            customSwitchPreference = this.mTapToWakePreference;
            if (value != 0) {
                z = true;
            } else {
                z = false;
            }
            customSwitchPreference.setChecked(z);
        }
        if (this.mCameraGesturePreference != null) {
            value = Secure.getInt(getContentResolver(), "camera_gesture_disabled", 0);
            SwitchPreference switchPreference = this.mCameraGesturePreference;
            if (value != 0) {
                z2 = false;
            }
            switchPreference.setChecked(z2);
        }
    }

    private void updateScreenSaverSummary() {
        if (this.mScreenSaverPreference != null) {
            this.mScreenSaverPreference.setSummary(getDreamSettingsSummaryResId());
        }
    }

    private void updateFontSizeSummary() {
        Context context = this.mFontSizePref.getContext();
        float currentScale = System.getFloat(context.getContentResolver(), "font_scale", 1.0f);
        Resources res = context.getResources();
        this.mFontSizePref.setSummary(res.getStringArray(2131361838)[ToggleFontSizePreferenceFragment.fontSizeValueToIndex(currentScale, res.getStringArray(2131361839))]);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        return super.onPreferenceTreeClick(preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        boolean value;
        int i;
        int i2 = 0;
        if ("screen_timeout".equals(preference.getKey())) {
            try {
                int value2 = Integer.parseInt((String) objValue);
                if (this.mHwCustDisplaySettings != null) {
                    this.screen_timeout_values = this.mHwCustDisplaySettings.getScreenTimeOutValues(this.screen_timeout_values);
                }
                ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mScreenTimeoutPreference, this.screen_timeout_values, (String) objValue);
                if (this.mHwCustDisplaySettings != null) {
                    value2 = this.mHwCustDisplaySettings.getCustomTimeout(getActivity(), value2);
                }
                System.putInt(getContentResolver(), "screen_off_timeout", value2);
                updateTimeoutPreferenceDescription((long) value2);
            } catch (NumberFormatException e) {
                Log.e("DisplaySettings", "could not persist screen timeout setting", e);
            }
        }
        if (preference == this.mLiftToWakePreference) {
            value = ((Boolean) objValue).booleanValue();
            ContentResolver contentResolver = getContentResolver();
            String str = "wake_gesture_enabled";
            if (value) {
                i = 1;
            } else {
                i = 0;
            }
            Secure.putInt(contentResolver, str, i);
        }
        if (preference == this.mDozePreference) {
            value = ((Boolean) objValue).booleanValue();
            contentResolver = getContentResolver();
            str = "doze_enabled";
            if (value) {
                i = 1;
            } else {
                i = 0;
            }
            Secure.putInt(contentResolver, str, i);
        }
        if (preference == this.mTapToWakePreference) {
            value = ((Boolean) objValue).booleanValue();
            contentResolver = getContentResolver();
            str = "double_tap_to_wake";
            if (value) {
                i = 1;
            } else {
                i = 0;
            }
            Secure.putInt(contentResolver, str, i);
        }
        if (preference == this.mCameraGesturePreference) {
            value = ((Boolean) objValue).booleanValue();
            ContentResolver contentResolver2 = getContentResolver();
            String str2 = "camera_gesture_disabled";
            if (!value) {
                i2 = 1;
            }
            Secure.putInt(contentResolver2, str2, i2);
        }
        if (preference == this.mNightModePreference) {
            try {
                ((UiModeManager) getSystemService("uimode")).setNightMode(Integer.parseInt((String) objValue));
            } catch (NumberFormatException e2) {
                Log.e("DisplaySettings", "could not persist night mode setting", e2);
            }
        }
        return super.onPreferenceChange(preference, objValue);
    }

    protected int getHelpResource() {
        return 2131626525;
    }

    private void disablePreferenceIfManaged(String key, String restriction) {
        RestrictedPreference pref = (RestrictedPreference) findPreference(key);
        if (pref != null) {
            pref.setDisabledByAdmin(null);
            if (RestrictedLockUtils.hasBaseUserRestriction(getActivity(), restriction, UserHandle.myUserId())) {
                pref.setEnabled(false);
            } else {
                pref.checkRestrictionAndSetDisabled(restriction);
            }
        }
    }
}
