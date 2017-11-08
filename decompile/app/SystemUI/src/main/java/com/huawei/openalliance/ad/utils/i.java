package com.huawei.openalliance.ad.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Environment;
import com.huawei.openalliance.ad.utils.b.d;
import fyusion.vislib.BuildConfig;
import java.io.File;

/* compiled from: Unknown */
public class i {
    public static String a(Context context) {
        if (a()) {
            String c = c(context);
            if (c != null) {
                return c;
            }
        }
        return b(context);
    }

    public static boolean a() {
        return "mounted".equals(Environment.getExternalStorageState()) || !b();
    }

    public static String b(Context context) {
        File filesDir = context.getFilesDir();
        return filesDir != null ? filesDir.getAbsolutePath() : BuildConfig.FLAVOR;
    }

    @TargetApi(9)
    protected static boolean b() {
        return !l.a() ? true : Environment.isExternalStorageRemovable();
    }

    public static String c(Context context) {
        try {
            File externalFilesDir = context.getExternalFilesDir(null);
            if (externalFilesDir != null) {
                return externalFilesDir.getAbsolutePath();
            }
        } catch (Exception e) {
            d.c("StorageUtils", "getExternalFilesDir exception, use memory card folder.");
        }
        return null;
    }
}
