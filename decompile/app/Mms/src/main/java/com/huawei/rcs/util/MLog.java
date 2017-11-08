package com.huawei.rcs.util;

import android.os.SystemProperties;
import android.util.Log;

public class MLog {
    private static final String LINE_END = System.lineSeparator();
    private static volatile int sCallCounter = 1;
    private static boolean sDebugMode = false;
    private static boolean sEnableShowRcsLog = RcsXmlParser.getBoolean("enableShowRcsLog", true);
    private static boolean sModLog = false;
    private static boolean sPerfLog = false;
    private static boolean sSysLog = false;

    static {
        initLog();
    }

    public static void initLog() {
        boolean z = true;
        if (sEnableShowRcsLog) {
            setDebugMode(true);
            return;
        }
        boolean z2;
        sDebugMode = SystemProperties.get("config.hw.enable_debug_rcs", "false").equals("true");
        if (sDebugMode) {
            z2 = true;
        } else {
            z2 = SystemProperties.get("config.hw.debug_rcs_performance", "false").equals("true");
        }
        sPerfLog = z2;
        if (sDebugMode) {
            z2 = true;
        } else {
            z2 = SystemProperties.get("ro.config.hw_log", "false").equals("true");
        }
        sSysLog = z2;
        if (!sDebugMode) {
            z = SystemProperties.get("ro.config.hw_module_log", "false").equals("true");
        }
        sModLog = z;
    }

    public static void setDebugMode(boolean debug) {
        if (debug) {
            sModLog = true;
            sSysLog = true;
            sPerfLog = true;
            sDebugMode = true;
            return;
        }
        sDebugMode = false;
        initLog();
    }

    public static final int v(String tag, String msg) {
        return sSysLog ? Log.v("RcsService", delimit(tag) + msg) : 0;
    }

    public static final int d(String tag, String msg) {
        return sSysLog ? Log.d("RcsService", delimit(tag) + msg) : 0;
    }

    public static final int w(String tag, String msg) {
        return Log.w("RcsService", delimit(tag) + msg);
    }

    public static final int e(String tag, String msg) {
        return Log.e("RcsService", delimit(tag) + msg);
    }

    private static String delimit(String tag) {
        return tag + " - ";
    }
}
