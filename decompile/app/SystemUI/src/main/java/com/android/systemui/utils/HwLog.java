package com.android.systemui.utils;

import android.util.Log;

public final class HwLog {
    private static final boolean HWDBG;
    private static final boolean HWINFO;

    static {
        boolean z = true;
        boolean isLoggable = !Log.HWLog ? Log.HWModuleLog ? Log.isLoggable("SystemUI", 3) : false : true;
        HWDBG = isLoggable;
        if (!Log.HWINFO) {
            z = Log.HWModuleLog ? Log.isLoggable("SystemUI", 4) : false;
        }
        HWINFO = z;
    }

    private HwLog() {
    }

    public static void v(String tag, String msg) {
        if (HWDBG) {
            Log.v(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (HWINFO) {
            Log.i(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (HWDBG) {
            Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable error) {
        Log.e(tag, msg, error);
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        Log.w(tag, msg, tr);
    }
}
