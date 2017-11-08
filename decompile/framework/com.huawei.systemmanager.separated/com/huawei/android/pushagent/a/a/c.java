package com.huawei.android.pushagent.a.a;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

/* compiled from: Unknown */
public class c {
    private static String a = "";
    private static String b = "hwpush";
    private static String c = "PushLog";
    private static c d = null;

    private c() {
    }

    public static synchronized c a() {
        c cVar;
        synchronized (c.class) {
            if (d == null) {
                d = new c();
            }
            cVar = d;
        }
        return cVar;
    }

    public static String a(Throwable th) {
        return Log.getStackTraceString(th);
    }

    private synchronized void a(int i, String str, String str2, Throwable th, int i2) {
        try {
            if (a(i)) {
                String str3 = "[" + Thread.currentThread().getName() + "-" + Thread.currentThread().getId() + "]" + str2;
                StackTraceElement[] stackTrace = new Throwable().getStackTrace();
                str3 = stackTrace.length <= i2 ? str3 + "(" + a + "/unknown source)" : str3 + "(" + a + "/" + stackTrace[i2].getFileName() + ":" + stackTrace[i2].getLineNumber() + ")";
                if (th != null) {
                    str3 = str3 + '\n' + a(th);
                }
                Log.println(i, c, str3);
            }
        } catch (Throwable e) {
            Log.e("PushLogSC2606", "call writeLog cause:" + e.toString(), e);
        }
    }

    public static synchronized void a(Context context) {
        synchronized (c.class) {
            if (d == null) {
                a();
            }
            if (TextUtils.isEmpty(a)) {
                String packageName = context.getPackageName();
                if (packageName != null) {
                    String[] split = packageName.split("\\.");
                    if (split != null && split.length > 0) {
                        a = split[split.length - 1];
                    }
                }
                c = b(context);
                return;
            }
        }
    }

    public static void a(String str, String str2) {
        a().a(3, str, str2, null, 2);
    }

    public static void a(String str, String str2, Throwable th) {
        a().a(3, str, str2, th, 2);
    }

    public static void a(String str, String str2, Object... objArr) {
        try {
            a().a(3, str, String.format(str2, objArr), null, 2);
        } catch (Throwable e) {
            Log.e("PushLogSC2606", "call writeLog cause:" + e.toString(), e);
        }
    }

    private static boolean a(int i) {
        return Log.isLoggable(b, i);
    }

    public static String b(Context context) {
        String str = "PushLogSC2606";
        if (context == null) {
            return str;
        }
        if ("com.huawei.android.pushagent".equals(context.getPackageName())) {
            str = str.replace("SC", "AC");
        } else if ("android".equals(context.getPackageName())) {
            str = str.replace("SC", "");
        }
        return str;
    }

    public static void b(String str, String str2) {
        a().a(4, str, str2, null, 2);
    }

    public static void b(String str, String str2, Throwable th) {
        a().a(4, str, str2, th, 2);
    }

    public static void b(String str, String str2, Object... objArr) {
        try {
            a().a(2, str, String.format(str2, objArr), null, 2);
        } catch (Throwable e) {
            Log.e("PushLogSC2606", "call writeLog cause:" + e.toString(), e);
        }
    }

    public static void c(String str, String str2) {
        a().a(5, str, str2, null, 2);
    }

    public static void c(String str, String str2, Throwable th) {
        a().a(6, str, str2, th, 2);
    }

    public static void d(String str, String str2) {
        a().a(6, str, str2, null, 2);
    }

    public static void d(String str, String str2, Throwable th) {
        a().a(2, str, str2, th, 2);
    }

    public static void e(String str, String str2) {
        a().a(2, str, str2, null, 2);
    }
}
