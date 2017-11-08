package com.huawei.hwid.core.c.b;

import android.content.Context;
import android.util.Log;
import com.huawei.hwid.core.c.p;

/* compiled from: LogX */
public class a {
    private static String a = "hwid";
    private static String b = "";

    public static synchronized void a(Context context) {
        synchronized (a.class) {
            String packageName = context.getPackageName();
            if (packageName != null) {
                String[] split = packageName.split("\\.");
                if (split.length > 0) {
                    a = split[split.length - 1];
                }
            }
            b = b(context);
        }
    }

    public static String b(Context context) {
        String str;
        String str2 = "";
        if (context == null) {
            str = str2;
        } else if ("com.huawei.hwid".equals(context.getPackageName())) {
            try {
                str = "HwID_APK_log[" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName + "]:";
            } catch (Throwable e) {
                Log.e("hwid", "getVersionTag error", e);
                str = str2;
            }
        } else {
            str = str2;
        }
        if (p.e(str)) {
            return "HwID_SDK_log[2.1.1.202]";
        }
        return str;
    }

    public static void a(String str, String str2) {
        a(3, str, str2, null, 2);
    }

    public static void a(String str, String str2, Throwable th) {
        a(3, str, str2, th, 2);
    }

    public static void b(String str, String str2) {
        a(4, str, str2, null, 2);
    }

    public static void b(String str, String str2, Throwable th) {
        a(4, str, str2, th, 2);
    }

    public static void c(String str, String str2) {
        a(5, str, str2, null, 2);
    }

    public static void c(String str, String str2, Throwable th) {
        a(5, str, str2, th, 2);
    }

    public static void d(String str, String str2) {
        a(6, str, str2, null, 2);
    }

    public static void d(String str, String str2, Throwable th) {
        a(6, str, str2, th, 2);
    }

    public static void e(String str, String str2) {
        a(2, str, str2, null, 2);
    }

    public static void e(String str, String str2, Throwable th) {
        a(2, str, str2, th, 2);
    }

    private static synchronized void a(int i, String str, String str2, Throwable th, int i2) {
        synchronized (a.class) {
            if (a(i)) {
                try {
                    Log.println(i, b + str, str2);
                } catch (Throwable e) {
                    Log.e("hwid", "println IllegalArgumentException", e);
                } catch (Throwable e2) {
                    Log.e("hwid", "println Exception", e2);
                }
            } else {
                return;
            }
        }
    }

    private static boolean a(int i) {
        return Log.isLoggable("hwid", i);
    }
}
