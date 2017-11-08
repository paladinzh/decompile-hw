package com.android.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Log {
    public static void v(String tag, String msg) {
        HwLog.v(tag, msg);
    }

    public static void i(String tag, String msg) {
        HwLog.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        HwLog.d(tag, msg);
    }

    public static void w(String tag, String msg) {
        HwLog.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        HwLog.e(tag, msg);
    }

    public static void dRelease(String tag, String msg) {
        HwLog.d(tag, msg);
    }

    public static void iRelease(String tag, String msg) {
        HwLog.i(tag, msg);
    }

    public static void printf(String message, Object... args) {
        String msg;
        if (args == null || args.length == 0) {
            msg = message;
        } else {
            msg = String.format(message, args);
        }
        HwLog.d("TimeZone", msg);
    }

    public static void printfe(String message, Object... args) {
        String msg;
        if (args == null || args.length == 0) {
            msg = message;
        } else {
            msg = String.format(message, args);
        }
        HwLog.e("TimeZone", msg);
    }

    public static String formatTime(long millis) {
        return new SimpleDateFormat("HH:mm:ss.SSS aaa", Locale.ENGLISH).format(new Date(millis));
    }
}
