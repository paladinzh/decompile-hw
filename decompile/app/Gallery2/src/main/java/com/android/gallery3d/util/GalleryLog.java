package com.android.gallery3d.util;

import android.util.Log;
import java.lang.reflect.Field;

public class GalleryLog {
    private static boolean sIsAllLogPrintSwitch = true;
    private static boolean sIsDFXLogCanPrint = false;
    private static boolean sIsDLogCanPrint;
    private static boolean sIsILogCanPrint;
    private static boolean sIsPhotoShareLogCanPrint = false;
    private static boolean sIsVLogCanPrint;

    static {
        sIsVLogCanPrint = true;
        sIsDLogCanPrint = true;
        sIsILogCanPrint = true;
        try {
            Class<?> classType = Log.class;
            Field hwLog = classType.getDeclaredField("HWLog");
            Field hwModuleLog = classType.getDeclaredField("HWModuleLog");
            Field hwINFO = classType.getDeclaredField("HWINFO");
            sIsVLogCanPrint = isNormalLogCanPrint("HwGallery2", hwLog, hwModuleLog, 2);
            sIsDLogCanPrint = isNormalLogCanPrint("HwGallery2", hwLog, hwModuleLog, 3);
            sIsILogCanPrint = isInfoLogCanPrint("HwGallery2", hwINFO, hwModuleLog, 4);
        } catch (NoSuchFieldException e) {
            Log.e("Gallery3d_GalleryLog", "Initialize huawei gallery log Exception:", e);
        }
    }

    private static boolean isNormalLogCanPrint(String tag, Field hwLog, Field hwModuleLog, int level) {
        boolean z = true;
        if (hwLog == null || hwModuleLog == null || sIsAllLogPrintSwitch) {
            return true;
        }
        try {
            if (!hwLog.getBoolean(null)) {
                z = hwModuleLog.getBoolean(null) ? Log.isLoggable(tag, level) : false;
            }
            return z;
        } catch (IllegalAccessException e) {
            d(tag, "Method isNormalLogCanPrint() getBoolean failed because of IllegalAccessException.");
            return true;
        } catch (IllegalArgumentException e2) {
            d(tag, "Method isNormalLogCanPrint() getBoolean failed because of IllegalArgumentException.");
            return true;
        }
    }

    private static boolean isInfoLogCanPrint(String tag, Field hwINFO, Field hwModuleLog, int level) {
        boolean z = true;
        if (hwINFO == null || hwModuleLog == null || sIsAllLogPrintSwitch) {
            return true;
        }
        try {
            if (!hwINFO.getBoolean(null)) {
                z = hwModuleLog.getBoolean(null) ? Log.isLoggable(tag, level) : false;
            }
            return z;
        } catch (IllegalAccessException e) {
            d(tag, "Method isInfoLogCanPrint() getBoolean failed because of IllegalAccessException.");
            return true;
        } catch (IllegalArgumentException e2) {
            d(tag, "Method isInfoLogCanPrint() getBoolean failed because of IllegalArgumentException.");
            return true;
        }
    }

    public static int printPhotoShareLog(String tag, String msg) {
        if (sIsPhotoShareLogCanPrint) {
            return v(tag, msg);
        }
        return -1;
    }

    public static int printDFXLog(String msg) {
        if (sIsDFXLogCanPrint) {
            return v("DFX", msg);
        }
        return -1;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int v(String tag, String msg) {
        if (tag == null || msg == null || !sIsVLogCanPrint) {
            return -1;
        }
        return Log.v(tag, msg);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int d(String tag, String msg) {
        if (tag == null || msg == null || !sIsDLogCanPrint) {
            return -1;
        }
        return Log.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        if (sIsDLogCanPrint) {
            return Log.d(tag, msg, tr);
        }
        return -1;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int i(String tag, String msg) {
        if (tag == null || msg == null || !sIsILogCanPrint) {
            return -1;
        }
        return Log.i(tag, msg);
    }

    public static int w(String tag, String msg) {
        if (tag == null || msg == null) {
            return -1;
        }
        return Log.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return Log.w(tag, msg, tr);
    }

    public static int w(String tag, Throwable tr) {
        return Log.w(tag, tr);
    }

    public static int e(String tag, String msg) {
        if (tag == null || msg == null) {
            return -1;
        }
        return Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return Log.e(tag, msg, tr);
    }

    public static void noPermissionForMediaProviderLog(String tag) {
        Log.w(tag, "No permission to process MediaProvider!");
    }
}
