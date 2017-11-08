package com.android.settings;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.TwoStatePreference;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedSwitchPreference;
import java.util.HashSet;
import org.json.JSONException;
import org.json.JSONObject;

public class DateTimeSettingsHwBase extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    protected RestrictedSwitchPreference mAutoTimePref;
    protected TwoStatePreference mAutoTimeZonePref;
    protected Preference mDatePref;
    protected TwoStatePreference mDualClocksSwitchPref;
    protected TwoSummaryPreference mHomeCityPref;
    protected LockPatternUtils mLockPatternUtils;
    protected TwoStatePreference mTime24Pref;
    protected Preference mTimePref;
    protected Preference mTimeZone;

    class HomeCityTask extends AsyncTask<Void, Void, String> {
        HomeCityTask() {
        }

        protected String doInBackground(Void... params) {
            Bundle bundle = null;
            String homeCity = "";
            try {
                Bundle extras = new Bundle();
                extras.putInt("city_type", 15);
                bundle = DateTimeSettingsHwBase.this.getContentResolver().call(Uri.parse("content://com.huawei.android.weather"), "query_home_city_weather", null, extras);
            } catch (Exception e) {
                Log.e("DateTimeSettingsHwBase", "query_home_city_weather call failed.");
            }
            if (bundle != null) {
                try {
                    homeCity = new JSONObject(new JSONObject(bundle.getString("key_home_city_weather")).getString("cityinfo")).getString("city_native_name");
                } catch (JSONException e2) {
                    Log.e("DateTimeSettingsHwBase", "parse jsonObject failed. No value for city_native_name.");
                }
            }
            return homeCity;
        }

        protected void onPostExecute(String homeCity) {
            if (DateTimeSettingsHwBase.this.mHomeCityPref != null) {
                DateTimeSettingsHwBase.this.mHomeCityPref.setSummary((CharSequence) homeCity);
            }
        }
    }

    protected int getMetricsCategory() {
        return 38;
    }

    private boolean getAutoState(String name) {
        boolean z = false;
        try {
            if (Global.getInt(getContentResolver(), name) > 0) {
                z = true;
            }
            return z;
        } catch (SettingNotFoundException snfe) {
            MLog.e("DateTimeSettingsHwBase", "SettingNotFoundException e: " + snfe);
            return false;
        }
    }

    private boolean isCardNotPresent(Context context) {
        boolean isCardpresent0;
        boolean isCardpresent1 = false;
        if (Utils.isMultiSimEnabled()) {
            isCardpresent0 = TelephonyManager.getDefault().getSimState(0) == 5;
            isCardpresent1 = TelephonyManager.getDefault().getSimState(1) == 5;
        } else {
            isCardpresent0 = TelephonyManager.getDefault().getSimState() != 1;
        }
        if (isCardpresent0 || isCardpresent1) {
            return false;
        }
        return true;
    }

    protected void setDataTimeByNetworkType() {
        boolean z = false;
        try {
            int parentCtrlStatus = ParentControl.getParentControlStatus(getActivity());
            int iNetworkType;
            if (MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
                MSimTelephonyManager.getDefault();
                iNetworkType = MSimTelephonyManager.getNetworkType(Utils.getMainCardSlotId());
            } else {
                iNetworkType = TelephonyManager.getDefault().getNetworkType();
            }
            if (isCardNotPresent(getActivity()) || !(4 == iNetworkType || 5 == iNetworkType || 6 == iNetworkType || 12 == iNetworkType || 14 == iNetworkType || 7 == iNetworkType || isCdmaLteNetwork(iNetworkType))) {
                boolean z2;
                boolean autoTimeEnabled = getAutoState("auto_time");
                this.mAutoTimePref.setChecked(autoTimeEnabled);
                ParentControl.enablePref(this.mAutoTimePref, true, parentCtrlStatus);
                if (!ParentControl.isChildModeOn(getActivity())) {
                    this.mAutoTimePref.setDisabledByAdmin(RestrictedLockUtils.checkIfAutoTimeRequired(getActivity()));
                }
                Preference preference = this.mTimePref;
                if (autoTimeEnabled) {
                    z2 = false;
                } else {
                    z2 = true;
                }
                ParentControl.enablePref(preference, z2, parentCtrlStatus);
                preference = this.mDatePref;
                if (autoTimeEnabled) {
                    z2 = false;
                } else {
                    z2 = true;
                }
                ParentControl.enablePref(preference, z2, parentCtrlStatus);
                if (!Utils.isWifiOnly(getActivity())) {
                    boolean autoTimeZoneEnabled = getAutoState("auto_time_zone");
                    this.mAutoTimeZonePref.setChecked(autoTimeZoneEnabled);
                    ParentControl.enablePref(this.mAutoTimeZonePref, true, parentCtrlStatus);
                    Preference preference2 = this.mTimeZone;
                    if (!autoTimeZoneEnabled) {
                        z = true;
                    }
                    ParentControl.enablePref(preference2, z, parentCtrlStatus);
                    return;
                }
                return;
            }
            System.putInt(getContentResolver(), "auto_time", 1);
            System.putInt(getContentResolver(), "auto_time_zone", 1);
            this.mAutoTimeZonePref.setChecked(true);
            this.mAutoTimeZonePref.setEnabled(false);
            this.mAutoTimePref.setChecked(true);
            this.mAutoTimePref.setEnabled(false);
            this.mTimePref.setEnabled(false);
            this.mDatePref.setEnabled(false);
        } catch (RuntimeException e) {
            MLog.e("DateTimeSettingsHwBase", "Exception e: " + e);
            e.printStackTrace();
        }
    }

    private boolean isCdmaLteNetwork(int type) {
        int currentMode = System.getInt(getContentResolver(), "ct_lte_mode", -1);
        HashSet<String> telecomPlmnSet = new HashSet();
        telecomPlmnSet.add("46003");
        telecomPlmnSet.add("46005");
        telecomPlmnSet.add("46011");
        if ((Utils.isChinaTelecomArea() || telecomPlmnSet.contains(TelephonyManager.getDefault().getSimOperator())) && type == 13 && currentMode != 2 && currentMode != 3) {
            return true;
        }
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, objValue);
        String key = preference.getKey();
        if ("24 hour".equals(key)) {
            Boolean value = (Boolean) objValue;
            set24Hour(value.booleanValue());
            updateTimeAndDateDisplay(getActivity());
            timeUpdated(value.booleanValue());
        } else if ("dual_clocks".equals(key)) {
            Boolean dualClocksEnabled = (Boolean) objValue;
            if (this.mHomeCityPref != null) {
                this.mHomeCityPref.setEnabled(dualClocksEnabled.booleanValue());
            }
            System.putInt(getContentResolver(), "dual_clocks", dualClocksEnabled.booleanValue() ? 1 : 0);
        }
        return true;
    }

    protected void set24Hour(boolean is24Hour) {
    }

    public void updateTimeAndDateDisplay(Context context) {
    }

    protected void timeUpdated(boolean is24Hour) {
    }

    protected void initUI() {
        if (Utils.isWifiOnly(getActivity())) {
            removePreference("dual_clocks_category");
        }
        this.mLockPatternUtils = new LockPatternUtils(getActivity());
        if (!this.mLockPatternUtils.isSecure(UserHandle.myUserId()) && this.mLockPatternUtils.isLockScreenDisabled(UserHandle.myUserId())) {
            removePreference("dual_clocks_category");
        } else if (!Utils.hasIntentActivity(getPackageManager(), "com.huawei.android.action.SET_HOME_CITY")) {
            Log.d("DateTimeSettingsHwBase", "ActivityNotFoundException: com.huawei.android.action.SET_HOME_CITY");
            removePreference("dual_clocks_category", "set_home_city");
        }
        this.mDualClocksSwitchPref = (TwoStatePreference) findPreference("dual_clocks");
        if (this.mDualClocksSwitchPref != null) {
            this.mDualClocksSwitchPref.setOnPreferenceChangeListener(this);
            this.mDualClocksSwitchPref.setChecked(System.getInt(getContentResolver(), "dual_clocks", 1) == 1);
            this.mHomeCityPref = (TwoSummaryPreference) findPreference("set_home_city");
            if (this.mHomeCityPref != null) {
                this.mHomeCityPref.setNetherSummary(getResources().getString(2131628669));
                this.mHomeCityPref.setEnabled(this.mDualClocksSwitchPref.isChecked());
            }
        }
    }

    public void onResume() {
        super.onResume();
        updateHomeCity();
    }

    protected void updateHomeCity() {
        new HomeCityTask().execute(new Void[0]);
    }

    public void onPause() {
        super.onPause();
        ItemUseStat.getInstance().cacheData(getActivity());
    }

    protected void enableDateTimeSettings(boolean enabled) {
        boolean z;
        boolean autoTimeEnabled = getAutoState("auto_time");
        this.mAutoTimePref.setEnabled(enabled);
        if (!ParentControl.isChildModeOn(getActivity())) {
            this.mAutoTimePref.setDisabledByAdmin(RestrictedLockUtils.checkIfAutoTimeRequired(getActivity()));
        }
        Preference preference = this.mTimePref;
        if (autoTimeEnabled) {
            z = false;
        } else {
            z = enabled;
        }
        preference.setEnabled(z);
        preference = this.mDatePref;
        if (autoTimeEnabled) {
            z = false;
        } else {
            z = enabled;
        }
        preference.setEnabled(z);
        if (Utils.isWifiOnly(getActivity())) {
            this.mTimeZone.setEnabled(enabled);
            return;
        }
        this.mAutoTimeZonePref.setEnabled(enabled);
        Preference preference2 = this.mTimeZone;
        if (autoTimeEnabled) {
            enabled = false;
        }
        preference2.setEnabled(enabled);
    }

    protected void updateParentControlItems() {
        enableDateTimeSettings(!ParentControl.isChildModeOn(getActivity()));
    }

    protected void updateParentCtrlAutoOptions() {
        if (ParentControl.isChildModeOn(getActivity())) {
            if (!Utils.isWifiOnly(getActivity())) {
                Global.putInt(getContentResolver(), "auto_time_zone", 1);
            }
            Global.putInt(getContentResolver(), "auto_time", 1);
        }
    }
}
