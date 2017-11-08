package com.android.systemui.utils;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import fyusion.vislib.BuildConfig;
import java.util.HashSet;
import java.util.Set;

public class Proguard {
    private static String[] keyArray = new String[]{"incoming_number", "plmn", "deviceid", "emmcid", "BSSID", "bssid", "MAC", "linkProperties", "networkInfo", "wifiInfo"};
    private static final HashSet<String> keyList = new HashSet();

    static {
        initKeyList();
    }

    private static void initKeyList() {
        if (keyArray != null) {
            for (String element : keyArray) {
                keyList.add(element);
            }
        }
        Log.d("Proguard", "keyList size is " + keyList.size());
    }

    public static String get(Object o) {
        return String.valueOf(o);
    }

    private static String getSimpleStr(String info, boolean isDebug) {
        if (isDebug) {
            return info;
        }
        return "*";
    }

    public static String get(String info) {
        return getSimpleStr(info, true);
    }

    public static String get(Bundle bundle) {
        if (bundle == null) {
            return BuildConfig.FLAVOR;
        }
        Set<String> keys = bundle.keySet();
        StringBuffer sb = new StringBuffer();
        for (String k : keys) {
            Object obj = bundle.get(k);
            String proguardVal = BuildConfig.FLAVOR;
            if (obj instanceof Bundle) {
                proguardVal = get((Bundle) obj);
            } else {
                proguardVal = get(obj);
            }
            proguardSecretInfo(k, proguardVal, sb);
            sb.append(' ');
        }
        return sb.toString();
    }

    public static void proguardSecretInfo(String key, String val, StringBuffer sb) {
        if (key == null || !keyList.contains(key)) {
            sb.append(key).append("=").append(val);
        } else {
            sb.append(getSimpleStr(key, false)).append("=").append(getSimpleStr(val, false));
        }
    }

    public static String get(Intent intent) {
        if (intent == null) {
            return BuildConfig.FLAVOR;
        }
        StringBuffer sb = new StringBuffer();
        if (!TextUtils.isEmpty(intent.getAction())) {
            sb.append("act:" + intent.getAction()).append(' ');
        }
        sb.append(" flag:" + intent.getFlags()).append(' ');
        if (intent.getExtras() != null) {
            sb.append(get(intent.getExtras()));
        }
        return sb.toString();
    }

    public static String get(String content, boolean isLongStr) {
        return content;
    }
}
