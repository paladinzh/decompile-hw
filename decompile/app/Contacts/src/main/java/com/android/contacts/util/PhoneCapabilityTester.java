package com.android.contacts.util;

import android.content.Context;
import android.net.sip.SipManager;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import com.android.contacts.hap.EmuiFeatureManager;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.CommonConstants;
import com.huawei.cust.HwCustUtils;

public final class PhoneCapabilityTester {
    private static HwCustPhoneCapabilityTester mHwCustPhoneCapabilityTester;
    private static boolean sIsEmobileCustomer;
    private static int sIsGeoCodeFeatureEnabled = -1;
    private static boolean sIsInitialized;
    private static boolean sIsPhone;
    private static boolean sIsSipPhone;

    public static boolean isPhone(Context context) {
        if (!sIsInitialized) {
            initialize(context);
        }
        return sIsPhone;
    }

    private static void initialize(Context context) {
        sIsPhone = ((TelephonyManager) context.getSystemService("phone")).isVoiceCapable();
        sIsSipPhone = sIsPhone ? SipManager.isVoipSupported(context) : false;
        sIsInitialized = true;
        sIsEmobileCustomer = context.getResources().getBoolean(R.bool.config_customer_emobile);
    }

    public static boolean isSipPhone(Context context) {
        if (!sIsInitialized) {
            initialize(context);
        }
        return sIsSipPhone;
    }

    public static boolean isCMCCCustomer(Context context) {
        return context.getResources().getBoolean(R.bool.config_customer_cmcc);
    }

    public static boolean isChinaTelecomCustomer(Context context) {
        return context.getResources().getBoolean(R.bool.config_customer_china_telecom);
    }

    public static boolean isEmobileCustomer() {
        return sIsEmobileCustomer;
    }

    public static boolean isTwoButtonsEmergencyDialerActive(Context context) {
        boolean isActive = System.getInt(context.getContentResolver(), "emergency_call_two_button", 0) == 1;
        if (HwLog.HWDBG) {
            HwLog.d("PhoneCapabilityTester", "isTwoButtonsEmergencyDialerActive" + isActive);
        }
        return isActive;
    }

    public static boolean isSpeedDialForPlatformEnabled(Context context) {
        return context.getResources().getBoolean(R.bool.config_enable_speed_dail_For_platfrom);
    }

    public static boolean isGeoCodeFeatureEnabled(Context context) {
        return EmuiFeatureManager.isChinaArea();
    }

    public static void reset() {
        sIsGeoCodeFeatureEnabled = -1;
    }

    public static boolean isSipEnabled(Context context) {
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            mHwCustPhoneCapabilityTester = (HwCustPhoneCapabilityTester) HwCustUtils.createObj(HwCustPhoneCapabilityTester.class, new Object[0]);
        }
        if (mHwCustPhoneCapabilityTester != null) {
            return mHwCustPhoneCapabilityTester.includeSipFeature(context);
        }
        return context.getResources().getBoolean(R.bool.config_sip_enabled);
    }

    public static boolean isOnlySyncMyContactsEnabled(Context context) {
        return CommonConstants.isOnlySyncMyContactsEnabled(context);
    }

    public static boolean isHarassmentEnabled(Context context) {
        return context.getResources().getBoolean(R.bool.config_harassment);
    }

    public static boolean isCallDurationHid() {
        return SystemProperties.getBoolean("ro.config.hide_call_duration", false);
    }
}
