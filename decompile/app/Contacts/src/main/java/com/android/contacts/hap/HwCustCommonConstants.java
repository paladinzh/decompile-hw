package com.android.contacts.hap;

import android.os.SystemProperties;

public class HwCustCommonConstants {
    public static final String AAB_ACCOUNT_TYPE = "com.att.aab";
    public static final String AAB_PACKAGE_NAME = "com.huawei.android.ds";
    public static final String AAB_STARTUP_KEY = "startupkey";
    public static final String ATT_ACCOUNT_NAME = "AT&T Address Book";
    public static final boolean DEBUG_AAB = true;
    public static final boolean EAS_ACCOUNT_ICON_DISP_EMABLED = SystemProperties.getBoolean("ro.config.hw_easicon", false);
    public static final String EAS_ACCOUNT_TYPE = "com.android.exchange";
    public static final int GROUP_INVISIBLE_INT = 2;
    public static final String ICE_ACCOUNT_NAME;
    public static final String ICE_ACCOUNT_TYPE;
    public static final String ICE_DIALER_MODE_ACTION = "ICE_DIALER_MODE";
    public static final String ICE_DIAL_911 = "911";
    public static final String ICE_EXTRA_URI_NAME = "mProfileLookupUri";
    public static final String ICE_GROUP_LABEL = "ICE";
    public static final String ICE_MY_INFO_SP_ALLERGIES = "ice_my_info_allergies";
    public static final String ICE_MY_INFO_SP_CURRENT_MEDICATION = "ice_my_info_current_medication";
    public static final String ICE_MY_INFO_SP_HEALTH_RECORD = "ice_my_info_health_record";
    public static final String ICE_MY_INFO_SP_NAME = "ice_my_info_my_name";
    public static final String ICE_MY_INFO_SP_OTHER = "ice_my_info_other";
    public static final boolean IS_AAB_ATT = SystemProperties.getBoolean("ro.config.att.aab", false);
    public static final boolean IS_SHOW_VVM = SystemProperties.getBoolean("ro.config.hw_show_vvm", false);
    public static final int REQ_ID_VIBRATION = 1003;
    public static final String START_AAB_INIT_ACTIVITY_ACTION = "com.huawei.android.startup.init";
    public static final String TAG_AAB = "AAB";
    public static final String VIBRATION_MIMETYPE = "vibration";
    public static final String VIBRATION_PATTERN_KEY = "vibration_pattern_key";

    static {
        String str;
        if (IS_AAB_ATT) {
            str = ATT_ACCOUNT_NAME;
        } else {
            str = "Phone";
        }
        ICE_ACCOUNT_NAME = str;
        if (IS_AAB_ATT) {
            str = AAB_ACCOUNT_TYPE;
        } else {
            str = "com.android.huawei.phone";
        }
        ICE_ACCOUNT_TYPE = str;
    }
}
