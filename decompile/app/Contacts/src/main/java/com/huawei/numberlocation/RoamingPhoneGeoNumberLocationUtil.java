package com.huawei.numberlocation;

import android.content.Context;

public class RoamingPhoneGeoNumberLocationUtil {
    public static String getRoamingPhoneGeoNumberLocation(Context context, String phoneNumber) {
        return NLUtils.getGeoNumberLocation(context, phoneNumber, Boolean.valueOf(false));
    }
}
