package com.huawei.hwid.a;

import android.content.Context;

/* compiled from: HwIDInterfManager */
public class a {
    public static boolean a(Context context) {
        return a("com.huawei.hwid.plugin.social.apk.interf.SocialPluginInterfManager");
    }

    public static boolean a(String str) {
        try {
            Class.forName(str);
            return true;
        } catch (ClassNotFoundException e) {
            com.huawei.hwid.core.c.b.a.d("isExsit", "The class is not existing: " + str);
            return false;
        }
    }
}
