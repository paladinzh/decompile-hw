package com.android.settings;

import android.content.Context;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.telephony.TelephonyManager;

public class HwCustDateTimeSettingsImpl extends HwCustDateTimeSettings {
    private static final String CATEGORY_INTER_ROAMING_TIME_KEY = "pref_catkey_inter_roam_time";
    private static final String CONFIRM_DOUBLE_TIME_TIMESCHENE_KEY = "pref_key_double_time_timescheme";
    private static final String CONFIRM_DOUBLE_TIME_TIMEZONE_KEY = "pref_key_double_time_timezone";
    private static final String DUAL_CLOCKS_KEY = "dual_clocks";
    private Preference mDoubleTimeTimeschemePref = this.mDateTimeSettings.findPreference(CONFIRM_DOUBLE_TIME_TIMESCHENE_KEY);
    private Preference mDoubleTimeTimezonePref = this.mDateTimeSettings.findPreference(CONFIRM_DOUBLE_TIME_TIMEZONE_KEY);
    private int mMainCard = 0;

    public HwCustDateTimeSettingsImpl(DateTimeSettings dateTimeSettings) {
        super(dateTimeSettings);
    }

    public void updateCustPreference(Context context) {
        this.mDateTimeSettings.getPreferenceManager().inflateFromResource(context, 2131230765, this.mDateTimeSettings.getPreferenceScreen());
        setDoubleTimeMenusEnable();
        hideDualClockControlMenu();
    }

    private void setDoubleTimeMenusEnable() {
        boolean z = false;
        this.mDoubleTimeTimezonePref = this.mDateTimeSettings.findPreference(CONFIRM_DOUBLE_TIME_TIMEZONE_KEY);
        this.mDoubleTimeTimeschemePref = this.mDateTimeSettings.findPreference(CONFIRM_DOUBLE_TIME_TIMESCHENE_KEY);
        if (SystemProperties.getBoolean("ro.config_hw_doubletime", false)) {
            TelephonyManager tm = (TelephonyManager) this.mDateTimeSettings.getSystemService("phone");
            Preference preference;
            if (Utils.isMultiSimEnabled()) {
                if (tm != null) {
                    this.mDoubleTimeTimeschemePref.setEnabled(tm.isNetworkRoaming(this.mMainCard));
                    preference = this.mDoubleTimeTimezonePref;
                    if (tm.isNetworkRoaming(this.mMainCard) && !isCdma(this.mMainCard)) {
                        z = true;
                    }
                    preference.setEnabled(z);
                }
            } else if (tm != null && tm.getPhoneType() == 2) {
                this.mDoubleTimeTimeschemePref.setEnabled(tm.isNetworkRoaming());
                preference = this.mDoubleTimeTimezonePref;
                if (tm.isNetworkRoaming() && !isCdma()) {
                    z = true;
                }
                preference.setEnabled(z);
            }
            return;
        }
        this.mDateTimeSettings.getPreferenceScreen().removePreference(this.mDateTimeSettings.findPreference(CATEGORY_INTER_ROAMING_TIME_KEY));
    }

    private boolean isCdma(int subscription) {
        switch (TelephonyManager.getDefault().getNetworkType(subscription)) {
            case 4:
            case 5:
            case 6:
            case 7:
            case 12:
            case 14:
                return true;
            default:
                return false;
        }
    }

    private boolean isCdma() {
        switch (TelephonyManager.getDefault().getNetworkType()) {
            case 4:
            case 5:
            case 6:
            case 7:
            case 12:
            case 14:
                return true;
            default:
                return false;
        }
    }

    private void hideDualClockControlMenu() {
        if (SystemProperties.getBoolean("ro.config.hideDualClock", false)) {
            Preference dualClock = this.mDateTimeSettings.findPreference(DUAL_CLOCKS_KEY);
            if (dualClock != null) {
                this.mDateTimeSettings.getPreferenceScreen().removePreference(dualClock);
            }
        }
    }
}
