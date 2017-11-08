package com.huawei.systemmanager.preventmode.util;

import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.huawei.systemmanager.customize.CustomizeManager;

public class Utility {
    public static String reserveData(String beforeDeleteStr) {
        if (TextUtils.isEmpty(beforeDeleteStr)) {
            return "";
        }
        return beforeDeleteStr.replaceAll("[^0-9]+", "");
    }

    public static String formatPhoneNumber(String phone) {
        return PhoneNumberUtils.stripSeparators(phone);
    }

    public static boolean isGlobalVersion() {
        return !CustomizeManager.getInstance().isFeatureEnabled(9);
    }
}
