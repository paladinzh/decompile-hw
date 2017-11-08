package com.android.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.TwoStatePreference;
import android.util.Log;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.utils.PowerModeReceiver;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.List;

public class NotificationAndStatusSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            ArrayList<SearchIndexableResource> result = new ArrayList();
            Log.e("terigele", "try loading the notification and status setting.");
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = 2131230824;
            result.add(sir);
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            ArrayList<String> result = new ArrayList();
            if (Utils.isWifiOnly(context)) {
                result.add("show_operator_name");
            }
            result.add("notification_manager");
            if (!context.getResources().getBoolean(17956932)) {
                result.add("notification_pulse");
            }
            if (!Utils.isChinaArea() || Utils.isWifiOnly(context)) {
                result.add("usage_display");
            }
            if (NotificationAndStatusSettings.mHwCustNotificationAndStatusSettings != null && NotificationAndStatusSettings.mHwCustNotificationAndStatusSettings.isHideNetworkSpeed()) {
                result.add("show_network_speed");
            }
            if (!LockScreenUtils.isMagazineLock()) {
                result.add("wakeup_when_receive_notification");
            }
            return result;
        }
    };
    private static HwCustNotificationAndStatusSettings mHwCustNotificationAndStatusSettings = ((HwCustNotificationAndStatusSettings) HwCustUtils.createObj(HwCustNotificationAndStatusSettings.class, new Object[0]));
    private Context mContext;
    private CustomSwitchPreference mNotificationPulse;
    private ListPreference mNotificationWaysDisplay;
    private PowerModeReceiver mPowerModeReceiver;
    protected CustomSwitchPreference mWakeupWhenReceiveNotification;
    private ListPreference mWaysDisplay;

    public void onCreate(Bundle savedInstanceState) {
        boolean z;
        boolean z2 = false;
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230824);
        Preference notificationManager = findPreference("notification_manager");
        if (notificationManager != null) {
            if (!Utils.hasIntentActivity(getPackageManager(), notificationManager.getIntent()) || System.getInt(getContentResolver(), "hsm_notification", 0) == 0) {
                removePreference("notification_manager");
            } else {
                notificationManager.getIntent().putExtra("package_name", getActivity().getPackageName());
            }
        }
        this.mNotificationPulse = (CustomSwitchPreference) findPreference("notification_pulse");
        if (this.mNotificationPulse != null) {
            this.mNotificationPulse.setOnPreferenceChangeListener(this);
            if (getResources().getBoolean(17956932)) {
                try {
                    CustomSwitchPreference customSwitchPreference = this.mNotificationPulse;
                    if (System.getInt(getContentResolver(), "notification_light_pulse") == 1) {
                        z = true;
                    } else {
                        z = false;
                    }
                    customSwitchPreference.setChecked(z);
                    this.mNotificationPulse.setOnPreferenceChangeListener(this);
                } catch (SettingNotFoundException e) {
                    Log.e("NotificationAndStatusSettings", "notification_light_pulse not found");
                }
            } else {
                removePreference("notification_pulse");
            }
        }
        if (Utils.isWifiOnly(getActivity())) {
            removePreference("status_bar_category", "show_operator_name");
            removePreference("notification_panel_category");
        }
        TwoStatePreference showOperatorPrefs = (TwoStatePreference) findPreference("show_operator_name");
        if (showOperatorPrefs != null) {
            if (System.getInt(getContentResolver(), "hw_status_bar_operators", 1) == 1) {
                z = true;
            } else {
                z = false;
            }
            showOperatorPrefs.setChecked(z);
            showOperatorPrefs.setOnPreferenceChangeListener(this);
        }
        TwoStatePreference showNetworkSpeedPrefs = (TwoStatePreference) findPreference("show_network_speed");
        if (showNetworkSpeedPrefs != null) {
            if (System.getInt(getContentResolver(), "show_network_speed_enabled", 0) == 1) {
                z = true;
            } else {
                z = false;
            }
            showNetworkSpeedPrefs.setChecked(z);
            showNetworkSpeedPrefs.setOnPreferenceChangeListener(this);
        }
        if (mHwCustNotificationAndStatusSettings != null && mHwCustNotificationAndStatusSettings.isHideNetworkSpeed()) {
            removePreference("status_bar_category", "show_network_speed");
        }
        if (mHwCustNotificationAndStatusSettings != null && mHwCustNotificationAndStatusSettings.isHideNetworkName()) {
            removePreference("status_bar_category", "show_operator_name");
        }
        if (Utils.isChinaArea()) {
            TwoStatePreference display_usage = (TwoStatePreference) findPreference("usage_display");
            if (display_usage != null) {
                if (System.getInt(getContentResolver(), "traffic_switch", 0) == 1) {
                    z = true;
                } else {
                    z = false;
                }
                display_usage.setChecked(z);
                display_usage.setOnPreferenceChangeListener(this);
            }
        } else {
            removePreference("notification_panel_category");
        }
        this.mWaysDisplay = (ListPreference) findPreference("ways_display_battery_percentage");
        this.mWaysDisplay.setOnPreferenceChangeListener(this);
        this.mNotificationWaysDisplay = (ListPreference) findPreference("notification_way");
        this.mNotificationWaysDisplay.setOnPreferenceChangeListener(this);
        int nvalue = System.getInt(getActivity().getContentResolver(), "notification_way_switch", 0);
        this.mNotificationWaysDisplay.setValueIndex(nvalue);
        this.mNotificationWaysDisplay.setSummary(this.mNotificationWaysDisplay.getEntries()[nvalue]);
        if (LockScreenUtils.isMagazineLock()) {
            this.mWakeupWhenReceiveNotification = (CustomSwitchPreference) findPreference("wakeup_when_receive_notification");
            if (this.mWakeupWhenReceiveNotification != null) {
                CustomSwitchPreference customSwitchPreference2 = this.mWakeupWhenReceiveNotification;
                if (Secure.getInt(getContentResolver(), "wakeup_when_receive_notification", 0) != 0) {
                    z2 = true;
                }
                customSwitchPreference2.setChecked(z2);
                this.mWakeupWhenReceiveNotification.setOnPreferenceChangeListener(this);
            }
        } else {
            removePreference("wakeup_when_receive_notification");
        }
        setHasOptionsMenu(true);
        this.mContext = getContext();
    }

    public void initBatteryPercent() {
        boolean showBatteryPercent;
        int value = -1;
        if (System.getInt(getActivity().getContentResolver(), "battery_percent_switch", 0) != 0) {
            showBatteryPercent = true;
        } else {
            showBatteryPercent = false;
        }
        if (showBatteryPercent) {
            value = System.getInt(getActivity().getContentResolver(), "battery_percent_switch_in", 0);
        }
        this.mWaysDisplay.setValueIndex(value + 1);
        this.mWaysDisplay.setSummary(this.mWaysDisplay.getEntries()[value + 1]);
    }

    public void applyLowPowerMode(boolean isLowPowerMode) {
        super.applyLowPowerMode(isLowPowerMode);
        this.mWaysDisplay.setEnabled(!isLowPowerMode);
    }

    public void onPause() {
        super.onPause();
        ItemUseStat.getInstance().cacheData(getActivity());
        if (this.mPowerModeReceiver != null) {
            this.mPowerModeReceiver.unregisterReceiver(this.mContext);
            this.mPowerModeReceiver = null;
        }
    }

    public void onResume() {
        super.onResume();
        initBatteryPercent();
        if (this.mPowerModeReceiver == null) {
            this.mPowerModeReceiver = new PowerModeReceiver(this);
            this.mPowerModeReceiver.registerReceiver(this.mContext);
        }
        if (this.mContext != null) {
            applyLowPowerMode(Utils.isLowPowerMode(this.mContext));
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int i = 0;
        String key = preference.getKey();
        Boolean value;
        if ("show_operator_name".equals(key)) {
            int i2;
            value = (Boolean) newValue;
            ContentResolver contentResolver = getContentResolver();
            String str = "hw_status_bar_operators";
            if (value.booleanValue()) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            System.putInt(contentResolver, str, i2);
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, value);
        } else if ("show_network_speed".equals(key)) {
            value = (Boolean) newValue;
            r3 = getContentResolver();
            r6 = "show_network_speed_enabled";
            if (value.booleanValue()) {
                i = 1;
            }
            System.putInt(r3, r6, i);
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, value);
        } else if ("usage_display".equals(key)) {
            value = (Boolean) newValue;
            r3 = getContentResolver();
            r6 = "traffic_switch";
            if (value.booleanValue()) {
                i = 1;
            }
            System.putInt(r3, r6, i);
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, value);
        } else if ("ways_display_battery_percentage".equals(key)) {
            value = Integer.parseInt((String) newValue);
            Log.d("NotificationAndStatusSettings", "Bettery percentage, current selected value is: " + value);
            if (-1 == value) {
                System.putInt(getActivity().getContentResolver(), "battery_percent_switch", 0);
            } else {
                System.putInt(getActivity().getContentResolver(), "battery_percent_switch", 1);
                System.putInt(getActivity().getContentResolver(), "battery_percent_switch_in", value);
            }
            this.mWaysDisplay.setSummary(this.mWaysDisplay.getEntries()[value + 1]);
            ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mWaysDisplay, ItemUseStat.KEY_BATTERY_PERCENTAGE, (String) newValue);
        } else if ("wakeup_when_receive_notification".equals(key)) {
            value = (Boolean) newValue;
            r3 = getContentResolver();
            r6 = "wakeup_when_receive_notification";
            if (value.booleanValue()) {
                i = 1;
            }
            Secure.putInt(r3, r6, i);
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, value);
        } else if ("notification_pulse".equals(key)) {
            value = (Boolean) newValue;
            r3 = getContentResolver();
            r6 = "notification_light_pulse";
            if (value.booleanValue()) {
                i = 1;
            }
            System.putInt(r3, r6, i);
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, value);
        } else if (!"notification_way".equals(key)) {
            return false;
        } else {
            value = Integer.parseInt((String) newValue);
            System.putInt(getActivity().getContentResolver(), "notification_way_switch", value);
            this.mNotificationWaysDisplay.setSummary(this.mNotificationWaysDisplay.getEntries()[value]);
            ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mNotificationWaysDisplay, ItemUseStat.KEY_NOTIFICATION_WAY, (String) newValue);
        }
        return true;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        return super.onPreferenceTreeClick(preference);
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
