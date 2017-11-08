package com.huawei.openalliance.ad.utils;

import android.text.TextUtils;
import fyusion.vislib.BuildConfig;

/* compiled from: Unknown */
public class e {
    public static String a(String str) {
        return !TextUtils.isEmpty(str) ? a(str, true) : BuildConfig.FLAVOR;
    }

    public static String a(String str, boolean z) {
        return d.a(str, z);
    }
}
