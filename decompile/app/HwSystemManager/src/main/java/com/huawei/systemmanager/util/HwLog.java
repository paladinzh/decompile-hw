package com.huawei.systemmanager.util;

import android.util.Log;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.UnknownHostException;

public class HwLog {
    private static String TAG = "HwSystemManager";
    private static final String TAG_INSTALLED = "HwSystemManager_Install";
    private static final String TAG_PUSHED = "HwSystemManager_Pushed";
    private static final String TAG_RELEASE = "HwSystemManager";
    private static boolean sHwDebug;
    private static boolean sHwInfo;
    private static boolean sHwModuleDebug;

    private static class CheckDevVersionThread extends Thread {
        public CheckDevVersionThread() {
            super("util_check_log_tag");
        }

        public void run() {
            if (HwLogTagHelper.scanHsmInDataApp()) {
                HwLog.TAG = HwLog.TAG_INSTALLED;
                return;
            }
            if (HwLogTagHelper.checkHsmIsPushed()) {
                HwLog.TAG = HwLog.TAG_PUSHED;
            }
        }
    }

    static {
        boolean z = true;
        sHwDebug = false;
        sHwInfo = true;
        sHwModuleDebug = true;
        try {
            Class<Log> logClass = Log.class;
            Field field_Hwlog = logClass.getField("HWLog");
            Field field_HwModuleLog = logClass.getField("HWModuleLog");
            Field field_HwInfoeLog = logClass.getField("HWINFO");
            sHwDebug = field_Hwlog.getBoolean(null);
            sHwInfo = field_HwInfoeLog.getBoolean(null);
            sHwModuleDebug = field_HwModuleLog.getBoolean(null);
            boolean isLoggable = !sHwDebug ? sHwModuleDebug ? Log.isLoggable(TAG, 3) : false : true;
            sHwDebug = isLoggable;
            if (!sHwInfo) {
                if (sHwModuleDebug) {
                    z = Log.isLoggable(TAG, 4);
                } else {
                    z = false;
                }
            }
            sHwInfo = z;
            i(TAG, "HwDebug:" + sHwDebug + " HwModuleDebug:" + sHwModuleDebug);
        } catch (IllegalArgumentException e) {
            e(TAG, "error:getLogField--IllegalArgumentException" + e.getMessage());
        } catch (IllegalAccessException e2) {
            e(TAG, "error:getLogField--IllegalAccessException" + e2.getMessage());
        } catch (NoSuchFieldException e3) {
            e(TAG, "error:getLogField--NoSuchFieldException" + e3.getMessage());
        }
        new CheckDevVersionThread().start();
    }

    public static void v(String tag, String msg) {
        if (sHwDebug) {
            Log.v(TAG, tag + ":" + msg);
        }
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (sHwDebug) {
            Log.v(TAG, tag + ":" + msg + '\n' + getStackTraceString(tr));
        }
    }

    public static void d(String tag, String msg) {
        if (sHwDebug) {
            Log.d(TAG, tag + ":" + msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (sHwDebug) {
            Log.d(TAG, tag + ":" + msg + '\n' + getStackTraceString(tr));
        }
    }

    public static void i(String tag, String msg) {
        if (sHwInfo) {
            Log.i(TAG, tag + ":" + msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (sHwInfo) {
            Log.i(TAG, tag + ":" + msg + '\n' + getStackTraceString(tr));
        }
    }

    public static void w(String tag, String msg) {
        Log.w(TAG, tag + ":" + msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        Log.w(TAG, tag + ":" + msg + '\n' + getStackTraceString(tr));
    }

    public static void w(String tag, Throwable tr) {
        Log.w(TAG, tag + ":" + '\n' + getStackTraceString(tr));
    }

    public static void e(String tag, String msg) {
        Log.e(TAG, tag + ":" + msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(TAG, tag + ":" + msg + '\n' + getStackTraceString(tr));
    }

    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }
        for (Throwable t = tr; t != null; t = t.getCause()) {
            if (t instanceof UnknownHostException) {
                return "";
            }
        }
        StringWriter sw = new StringWriter();
        tr.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
