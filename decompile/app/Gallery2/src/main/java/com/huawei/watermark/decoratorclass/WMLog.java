package com.huawei.watermark.decoratorclass;

import android.util.Log;
import com.huawei.watermark.wmutil.ReflectClass;
import java.lang.reflect.Field;

public class WMLog {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMLog.class.getSimpleName());
    private static long TRACE_TAG_CAMERA;
    private static Class<?> classType;
    private static Field hwLog;
    private static Field hwModuleLog;
    private static boolean mIsDLogCanPrint;
    private static boolean mIsDebugVersion = true;
    private static boolean mIsILogCanPrint;
    private static boolean mIsTraceEnable = true;
    private static boolean mIsVLogCanPrint;
    private static ReflectClass mTraceClass;

    static {
        hwLog = null;
        hwModuleLog = null;
        classType = null;
        mIsVLogCanPrint = true;
        mIsDLogCanPrint = true;
        mIsILogCanPrint = true;
        mTraceClass = null;
        TRACE_TAG_CAMERA = 1024;
        try {
            classType = Class.forName("android.util.Log");
            mTraceClass = new ReflectClass("android.os.Trace", new Class[0]);
            Object object = mTraceClass.getStaticField("TRACE_TAG_CAMERA");
            if (object == null) {
                TRACE_TAG_CAMERA = 1024;
            } else {
                TRACE_TAG_CAMERA = ((Long) object).longValue();
            }
            hwLog = classType.getDeclaredField("HWLog");
            hwModuleLog = classType.getDeclaredField("HWModuleLog");
            mIsVLogCanPrint = isNormalLogCanPrint("HwCamera", 2);
            mIsDLogCanPrint = isNormalLogCanPrint("HwCamera", 3);
            mIsILogCanPrint = isNormalLogCanPrint("HwCamera", 4);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Initialize huawei camera log ClassNotFoundException.");
        } catch (Exception e2) {
            Log.e(TAG, "Initialize huawei camera log Exception:" + e2.getMessage());
        }
    }

    private WMLog() {
    }

    private static boolean isNormalLogCanPrint(String tag, int level) {
        boolean z = true;
        if (hwLog == null || hwModuleLog == null || mIsDebugVersion) {
            return true;
        }
        try {
            if (!hwLog.getBoolean(null)) {
                z = hwModuleLog.getBoolean(null) ? Log.isLoggable(tag, level) : false;
            }
            return z;
        } catch (IllegalAccessException e) {
            return true;
        } catch (IllegalArgumentException e2) {
            return true;
        }
    }

    public static int v(String tag, String msg) {
        if (mIsVLogCanPrint) {
            return Log.v(tag, msg);
        }
        return -1;
    }

    public static int d(String tag, String msg) {
        if (mIsDLogCanPrint) {
            return Log.d(tag, msg);
        }
        return -1;
    }

    public static int i(String tag, String msg) {
        if (mIsILogCanPrint) {
            return Log.i(tag, msg);
        }
        return -1;
    }

    public static int i(String tag, String msg, Throwable tr) {
        if (mIsILogCanPrint) {
            return Log.i(tag, msg, tr);
        }
        return -1;
    }

    public static int e(String tag, String msg) {
        return Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return Log.e(tag, msg, tr);
    }
}
