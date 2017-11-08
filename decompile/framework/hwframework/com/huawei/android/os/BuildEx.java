package com.huawei.android.os;

import android.os.SystemProperties;
import android.telephony.MSimTelephonyConstants;

public class BuildEx {
    public static final String EMUI_VERSION = SystemProperties.get("ro.build.version.emui", MSimTelephonyConstants.MY_RADIO_PLATFORM);

    public static class VERSION {
        public static final int EMUI_SDK_INT = SystemProperties.getInt("ro.build.hw_emui_api_level", 0);
    }

    public static class VERSION_CODES {
        public static final int CUR_DEVELOPMENT = 10000;
        public static final int EMUI_1_0 = 1;
        public static final int EMUI_1_5 = 2;
        public static final int EMUI_1_6 = 3;
        public static final int EMUI_2_0_JB = 4;
        public static final int EMUI_2_0_KK = 5;
        public static final int EMUI_2_3 = 6;
        public static final int EMUI_3_0 = 7;
        public static final int EMUI_3_0_5 = 8;
        public static final int EMUI_3_1 = 8;
        public static final int EMUI_4_0 = 9;
        public static final int EMUI_4_1 = 10;
        public static final int EMUI_5_0 = 11;
        public static final int EMUI_5_1 = 12;
        public static final int UNKNOWN_EMUI = 0;
    }
}
