package com.huawei.permissionmanager.utils;

import android.text.TextUtils;

public class SmsParseUtil {
    private static final String DIVIDER_CHAR = ":";

    public static String getSmsPhoneNumber(String desAddr) {
        String phoneNum = "";
        if (!TextUtils.isEmpty(desAddr) && desAddr.contains(DIVIDER_CHAR)) {
            return desAddr.substring(0, desAddr.indexOf(DIVIDER_CHAR));
        }
        return phoneNum;
    }

    public static String getSmsContent(String desAddr) {
        String smsContent = "";
        if (TextUtils.isEmpty(desAddr)) {
            return smsContent;
        }
        if (desAddr.contains(DIVIDER_CHAR)) {
            return desAddr.substring(desAddr.indexOf(DIVIDER_CHAR) + 1);
        }
        return desAddr;
    }
}
