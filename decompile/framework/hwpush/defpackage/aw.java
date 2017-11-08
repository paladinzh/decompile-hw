package defpackage;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

/* renamed from: aw */
public class aw {
    private static String bM = "";
    private static aw bN = null;

    private aw() {
    }

    private synchronized void a(int i, String str, String str2, Throwable th, int i2) {
        try {
            if (aw.isLoggable(i)) {
                String str3 = "[" + Thread.currentThread().getName() + "-" + Thread.currentThread().getId() + "]" + str2;
                StackTraceElement[] stackTrace = new Throwable().getStackTrace();
                str3 = stackTrace.length > i2 ? str3 + "(" + bM + "/" + stackTrace[i2].getFileName() + ":" + stackTrace[i2].getLineNumber() + ")" : str3 + "(" + bM + "/unknown source)";
                if (th != null) {
                    str3 = str3 + '\n' + aw.getStackTraceString(th);
                }
                Log.println(i, str, str3);
            }
        } catch (Throwable e) {
            aw.d("PushLog2841", "call writeLog cause:" + e.toString(), e);
        }
    }

    public static void a(String str, String str2, Throwable th) {
        aw.bM().a(3, str, str2, th, 2);
    }

    public static void b(String str, String str2, Throwable th) {
        aw.bM().a(4, str, str2, th, 2);
    }

    private static synchronized aw bM() {
        aw awVar;
        synchronized (aw.class) {
            if (bN == null) {
                bN = new aw();
            }
            awVar = bN;
        }
        return awVar;
    }

    public static void c(String str, String str2, Throwable th) {
        aw.bM().a(5, str, str2, th, 2);
    }

    public static void d(String str, String str2) {
        aw.bM().a(3, str, str2, null, 2);
    }

    public static void d(String str, String str2, Throwable th) {
        aw.bM().a(6, str, str2, th, 2);
    }

    public static void e(String str, String str2) {
        aw.bM().a(6, str, str2, null, 2);
    }

    public static String getStackTraceString(Throwable th) {
        return Log.getStackTraceString(th);
    }

    public static void i(String str, String str2) {
        aw.bM().a(4, str, str2, null, 2);
    }

    public static void init(Context context) {
        if (bN == null) {
            aw.bM();
        }
        if (TextUtils.isEmpty(bM)) {
            if (bN != null) {
                ag.n(context);
            }
            String packageName = context.getPackageName();
            if (packageName != null) {
                String[] split = packageName.split("\\.");
                if (split != null && split.length > 0) {
                    bM = split[split.length - 1];
                }
            }
        }
    }

    private static boolean isLoggable(int i) {
        try {
            return Log.isLoggable("hwpush", i);
        } catch (Exception e) {
            return false;
        }
    }

    public static void v(String str, String str2) {
        aw.bM().a(2, str, str2, null, 2);
    }

    public static void w(String str, String str2) {
        aw.bM().a(5, str, str2, null, 2);
    }
}
