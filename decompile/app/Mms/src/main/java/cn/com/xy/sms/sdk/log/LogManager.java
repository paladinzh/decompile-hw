package cn.com.xy.sms.sdk.log;

import android.util.Log;
import java.text.SimpleDateFormat;

/* compiled from: Unknown */
public class LogManager {
    private static SimpleDateFormat a = null;
    public static boolean debug = false;
    public static boolean writeFileLog = false;

    private static synchronized SimpleDateFormat a() {
        SimpleDateFormat simpleDateFormat;
        synchronized (LogManager.class) {
            if (a == null) {
                a = new SimpleDateFormat("yyyy.MM.dd");
            }
            simpleDateFormat = a;
        }
        return simpleDateFormat;
    }

    public static String appendStrArr(String... strArr) {
        if (strArr == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String append : strArr) {
            stringBuilder.append(append);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    public static void d(String str, String str2) {
        String appendStrArr = appendStrArr(str2);
        if (debug) {
            Log.d(str, appendStrArr);
        }
    }

    public static void d(String str, String str2, String str3) {
        String appendStrArr = appendStrArr(str2, str3);
        if (debug) {
            Log.d(str, appendStrArr);
        }
    }

    public static void d(String str, Throwable th, String str2) {
        if (debug) {
            Log.d(str, str2, th);
        }
    }

    public static void e(String str, String str2) {
        if (debug) {
            Log.e(str, str2);
        }
    }

    public static void e(String str, String str2, Throwable th) {
        if (debug) {
            Log.e(str, str2, th);
        }
    }

    public static void i(String str, String str2) {
        String appendStrArr = appendStrArr(str2);
        if (debug) {
            Log.i(str, appendStrArr);
        }
    }

    public static void i(String str, Throwable th, String str2) {
        if (debug) {
            Log.i(str, str2, th);
        }
    }

    public static void ll(String str, String... strArr) {
        String appendStrArr = appendStrArr(strArr);
        if (debug) {
            Log.d(str, appendStrArr);
        }
    }

    public static void w(String str, String str2) {
        if (debug) {
            Log.w(str, str2);
        }
    }

    public static void w(String str, String str2, Throwable th) {
        if (debug) {
            Log.w(str, str2, th);
        }
    }
}
