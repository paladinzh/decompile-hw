package com.huawei.hwid.core.d.b;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;

public abstract class b {
    static String a = "";

    static class a {
        static String a(Context context) {
            String str = b(context) + "/Log/";
            File file = new File(str);
            if (file.exists() || file.mkdirs()) {
                return str;
            }
            return null;
        }

        private static String b(Context context) {
            if (a()) {
                File externalFilesDir = context.getExternalFilesDir(null);
                if (externalFilesDir != null) {
                    return externalFilesDir.getAbsolutePath();
                }
            }
            return context.getFilesDir().getAbsolutePath();
        }

        private static boolean a() {
            return "mounted".equals(Environment.getExternalStorageState()) || !b();
        }

        @TargetApi(9)
        private static boolean b() {
            if (VERSION.SDK_INT < 9) {
                return true;
            }
            return Environment.isExternalStorageRemovable();
        }
    }

    abstract void a(String str, String str2);

    abstract void a(String str, String str2, Throwable th);

    abstract void b(String str, String str2);

    abstract void b(String str, String str2, Throwable th);

    abstract void c(String str, String str2);

    static String a(Context context) {
        Object obj;
        String str;
        String str2 = "";
        if (context == null) {
            obj = str2;
        } else if ("com.huawei.hwid".equals(context.getPackageName())) {
            try {
                obj = "HwID_APK_log[" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName + "]:";
            } catch (Throwable e) {
                Log.e("hwid", "getVersionTag error", e);
                str = str2;
            }
        } else {
            str = str2;
        }
        if (TextUtils.isEmpty(obj)) {
            return "HwID_SDK_log[2.4.0.300]";
        }
        return obj;
    }
}
