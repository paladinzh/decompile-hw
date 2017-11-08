package com.huawei.keyguard.util;

import android.util.Log;
import java.lang.reflect.Field;
import java.util.Map.Entry;

public final class HwLog {
    private static boolean sHwDebug;
    private static boolean sHwInfo;
    private static boolean sHwModuleDebug;

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
            boolean isLoggable = !sHwDebug ? sHwModuleDebug ? Log.isLoggable("Log", 3) : false : true;
            sHwDebug = isLoggable;
            if (!sHwInfo) {
                if (sHwModuleDebug) {
                    z = Log.isLoggable("Log", 4);
                } else {
                    z = false;
                }
            }
            sHwInfo = z;
            i("Log", "HwDebug:" + sHwDebug + " HwModuleDebug:" + sHwModuleDebug);
        } catch (IllegalArgumentException e) {
            e("Log", "error:getLogField--IllegalArgumentException" + e.getMessage());
        } catch (IllegalAccessException e2) {
            e("Log", "error:getLogField--IllegalAccessException" + e2.getMessage());
        } catch (NoSuchFieldException e3) {
            e("Log", "error:getLogField--NoSuchFieldException" + e3.getMessage());
        }
    }

    public static void v(String tag, String msg) {
        Log.v(tag, msg);
    }

    public static void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void w(String tag, String msg, Throwable error) {
        Log.w(tag, msg, error);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable error) {
        Log.e(tag, msg, error);
    }

    public static void wtf(String tag, String msg, Throwable error) {
        Log.wtf(tag, msg, error);
    }

    public static void dumpThreadStack(String tag, long tid) {
        for (Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            if (tid == -1 || ((Thread) entry.getKey()).getId() == tid) {
                StackTraceElement[] stacks = (StackTraceElement[]) entry.getValue();
                for (StackTraceElement stackTraceElement : stacks) {
                    Log.i(tag, "     -> " + stackTraceElement.toString());
                }
            }
        }
    }
}
