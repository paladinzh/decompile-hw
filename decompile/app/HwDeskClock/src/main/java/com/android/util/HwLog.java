package com.android.util;

import android.util.Log;
import java.lang.reflect.Field;

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

    public static void v(String TAG, String msg) {
        if (sHwDebug) {
            Log.v("HwDeskClock", TAG + " verbose:" + msg);
        }
    }

    public static void d(String TAG, String msg) {
        if (sHwDebug) {
            Log.d("HwDeskClock", TAG + " debug:" + msg);
        }
    }

    public static void i(String TAG, String msg) {
        if (sHwInfo) {
            Log.i("HwDeskClock", TAG + " info:" + msg);
        }
    }

    public static void w(String tag, String msg) {
        Log.w("HwDeskClock", tag + " warn: " + msg);
    }

    public static void e(String tag, String msg) {
        Log.e("HwDeskClock", tag + " error:" + msg);
    }

    public static void e(String tag, String msg, Throwable error) {
        Log.e("HwDeskClock", tag + " error:" + msg, error);
    }
}
