package com.avast.android.shepherd.obfuscated;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import java.io.File;

/* compiled from: Unknown */
public class z {
    public static boolean a(Context context) {
        return a(context, "com.avast.android.mobilesecurity");
    }

    public static boolean a(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        try {
            context.getPackageManager().getPackageInfo(str, 0);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static String b(Context context) {
        return !a(context, "com.avast.android.antitheft") ? !a(context, "com.avast.android.at_play") ? null : "com.avast.android.at_play" : "com.avast.android.antitheft";
    }

    public static boolean c(Context context) {
        return new File("/system/app/com.avast.android.antitheft.apk").exists() || new File("/system/priv-app/com.avast.android.antitheft.apk").exists() || a(context, "com.avast.android.antitheft");
    }

    public static boolean d(Context context) {
        return a(context, "com.avast.android.backup");
    }

    public static boolean e(Context context) {
        return a(context, "com.avast.android.vpn");
    }

    public static boolean f(Context context) {
        return a(context, "com.avast.android.batterysaver");
    }

    public static boolean g(Context context) {
        return a(context, "com.avast.android.cleaner");
    }
}
