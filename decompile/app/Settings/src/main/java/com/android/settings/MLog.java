package com.android.settings;

import android.os.SystemProperties;
import android.util.Log;

public class MLog {
    private static final String LINE_END = System.lineSeparator();
    private static boolean sDebugMode = false;
    private static boolean sModLog = false;
    private static boolean sSysLog = false;

    static {
        initLog();
    }

    public static void initLog() {
        sSysLog = SystemProperties.get("ro.config.hw_log", "false").equals("true");
        sModLog = SystemProperties.get("ro.config.hw_module_log", "false").equals("true");
    }

    public static final int v(String tag, String msg) {
        return sSysLog ? Log.v(tag, msg) : 0;
    }

    public static final int d(String tag, String msg) {
        return sSysLog ? Log.d(tag, msg) : 0;
    }

    public static final int i(String tag, String msg) {
        return sSysLog ? Log.i(tag, msg) : 0;
    }

    public static final int w(String tag, String msg) {
        return Log.w(tag, msg);
    }

    public static final int w(String tag, String format, Object... args) {
        return Log.w(tag, format(format, args));
    }

    public static final int e(String tag, String msg) {
        return Log.e(tag, msg);
    }

    public static final int e(String tag, String msg, Throwable tr) {
        return Log.e(tag, msg, tr);
    }

    public static final int e(String tag, String format, Object... args) {
        return Log.e(tag, format(format, args));
    }

    private static final String format(String format, Object... args) {
        try {
            return String.format(format, args);
        } catch (Exception e) {
            return "Exception in format Log >> " + format + "<< !!";
        }
    }
}
