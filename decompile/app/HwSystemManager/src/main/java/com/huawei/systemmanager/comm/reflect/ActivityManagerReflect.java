package com.huawei.systemmanager.comm.reflect;

import android.app.ActivityManager;
import android.os.Build.VERSION;
import com.huawei.systemmanager.util.HwLog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ActivityManagerReflect {
    static final int CURRENT_API_LEVEL = VERSION.SDK_INT;
    private static final String TAG = "ActivityManagerReflect";
    static Method rmTaskMethod = null;

    public static final void setRemoveTask(ActivityManager am, int taskId, int flags) {
        if (am == null) {
            HwLog.d(TAG, "removeTask is not be executed because ActivityManager is NULL");
            return;
        }
        try {
            if (CURRENT_API_LEVEL <= 21) {
                if (rmTaskMethod == null) {
                    rmTaskMethod = am.getClass().getMethod("removeTask", new Class[]{Integer.TYPE, Integer.TYPE});
                }
                rmTaskMethod.invoke(am, new Object[]{Integer.valueOf(taskId), Integer.valueOf(flags)});
            } else {
                if (rmTaskMethod == null) {
                    rmTaskMethod = am.getClass().getMethod("removeTask", new Class[]{Integer.TYPE});
                }
                rmTaskMethod.invoke(am, new Object[]{Integer.valueOf(taskId)});
            }
        } catch (NoSuchMethodException e) {
            HwLog.e(TAG, "Android API = " + CURRENT_API_LEVEL + " and Exception in setRemoveTask: " + e.getCause());
        } catch (IllegalAccessException e2) {
            HwLog.e(TAG, "Android API = " + CURRENT_API_LEVEL + " and Exception in setRemoveTask: " + e2.getCause());
        } catch (IllegalArgumentException e3) {
            HwLog.e(TAG, "Android API = " + CURRENT_API_LEVEL + " and Exception in setRemoveTask: " + e3.getCause());
        } catch (InvocationTargetException e4) {
            HwLog.e(TAG, "Android API = " + CURRENT_API_LEVEL + " and Exception in setRemoveTask: " + e4.getCause());
        } catch (SecurityException e5) {
            HwLog.e(TAG, "Android API = " + CURRENT_API_LEVEL + " and Exception in setRemoveTask: " + e5.getCause());
        }
    }
}
