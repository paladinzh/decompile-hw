package com.android.settings.deviceinfo;

import android.content.Context;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SettingsEx.Systemex;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.settings.Utils;

public class HwCustMSimStatusImpl extends HwCustMSimStatus {
    private static final boolean IMEI_SV_ENABLE = SystemProperties.getBoolean("ro.config.hw_imei_sv_enable", false);
    private static final int IMEI_SV_LENGTH = 16;
    private static final boolean IMEI_SV_SHOW_TWO = SystemProperties.getBoolean("ro.config.hw_imei_sv_show_two", false);
    private static final int IMEI_SV_SUB_STRING = 14;
    private static final String KEY_HARDWARE_VERSION = "hardwareversion";
    private static final String KEY_IMEI_SV = "imei_sv";
    private static final String KEY_IMEI_SV1 = "imei_sv1";
    private static final String KEY_NETWORK = "network";
    private Preference mHardwareVersion;

    public HwCustMSimStatusImpl(MSimStatus mSimStatus) {
        super(mSimStatus);
    }

    public void showHardwarePreference(Context context) {
        if (Systemex.getInt(context.getContentResolver(), "is_show_hardwareversion", 0) == 1) {
            PreferenceScreen root = this.mMSimStatus.getPreferenceScreen();
            this.mHardwareVersion = new Preference(context);
            this.mHardwareVersion.setKey(KEY_HARDWARE_VERSION);
            this.mHardwareVersion.setPersistent(false);
            this.mHardwareVersion.setLayoutResource(2130968980);
            this.mHardwareVersion.setTitle(2131627415);
            this.mHardwareVersion.setSummary(2131625223);
            root.addPreference(this.mHardwareVersion);
            Preference networkPref = root.findPreference(KEY_NETWORK);
            if (networkPref != null) {
                this.mHardwareVersion.setOrder(networkPref.getOrder() + 1);
            }
            StatusHwBase.initExtralPreferences(this.mMSimStatus);
        }
    }

    public void getAndShowMultiSimStatus() {
        int i = 0;
        if (Utils.isMultiSimEnabled() && UserHandle.myUserId() == 0) {
            int mainCardSlotId = Utils.getMainCardSlotId();
            Phone phone1 = PhoneFactory.getPhone(mainCardSlotId);
            if (mainCardSlotId == 0) {
                i = 1;
            }
            setAndShowMultiSimStatus(phone1, PhoneFactory.getPhone(i));
        }
    }

    private void setAndShowMultiSimStatus(Phone phone1, Phone phone2) {
        if (IMEI_SV_ENABLE) {
            String imeiSv = null;
            String imeiSv1 = null;
            PreferenceScreen root = this.mMSimStatus.getPreferenceScreen();
            new PreferenceManager(this.mMSimStatus.getContext().getApplicationContext()).inflateFromResource(this.mMSimStatus.getContext(), 2131230818, root);
            Preference imeiSvPref = root.findPreference(KEY_IMEI_SV);
            Preference imeiSv1Pref1 = root.findPreference(KEY_IMEI_SV1);
            Preference network = root.findPreference(KEY_NETWORK);
            if (imeiSvPref == null || network == null || phone1 == null) {
                root.removePreference(imeiSvPref);
            } else {
                imeiSv = getIMEISVSummaryText(phone1);
                imeiSvPref.setOrder(network.getOrder() + 1);
                imeiSvPref.setSummary((CharSequence) imeiSv);
            }
            if (imeiSv1Pref1 == null || network == null || phone2 == null) {
                root.removePreference(imeiSv1Pref1);
            } else {
                imeiSv1 = getIMEISVSummaryText(phone2);
                imeiSv1Pref1.setOrder(network.getOrder() + 2);
                imeiSv1Pref1.setSummary((CharSequence) imeiSv1);
            }
            if (imeiSv != null && (imeiSv.equals(imeiSv1) || TextUtils.isEmpty(imeiSv1))) {
                root.removePreference(imeiSv1Pref1);
            }
        }
    }

    private String getIMEISVSummaryText(Phone phone) {
        String imeiSv = phone.getDeviceSvn();
        if (isIMEISVShowTwoChar(imeiSv)) {
            return imeiSv.substring(14);
        }
        return imeiSv;
    }

    private boolean isIMEISVShowTwoChar(String sv) {
        if (IMEI_SV_SHOW_TWO && sv != null && 16 == sv.length()) {
            return true;
        }
        return false;
    }
}
