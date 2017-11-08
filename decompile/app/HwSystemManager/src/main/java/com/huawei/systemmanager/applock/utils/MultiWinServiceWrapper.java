package com.huawei.systemmanager.applock.utils;

import android.os.IBinder;
import android.os.ServiceManager;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MultiWinServiceWrapper {
    public static final boolean MULTIWINDOW_FLAG = true;
    private static final String TAG = MultiWinServiceWrapper.class.getSimpleName();
    private static Method sGetMWMaintained = null;
    private static Method sIsPartOfMultiWindowMethod = null;
    private static Object sMultiWinService;

    static {
        initDeclaredMethods();
    }

    private static void initDeclaredMethods() {
        Class<?>[] isPartOfMultiWindowArgs = new Class[]{Integer.TYPE};
        try {
            sMultiWinService = Class.forName("android.os.IMultiWinService$Stub").getMethod("asInterface", new Class[]{IBinder.class}).invoke(null, new Object[]{ServiceManager.getService("multiwin")});
            Class<?> clazz = sMultiWinService.getClass();
            sIsPartOfMultiWindowMethod = clazz.getDeclaredMethod("isPartOfMultiWindow", isPartOfMultiWindowArgs);
            sGetMWMaintained = clazz.getDeclaredMethod("getMWMaintained", (Class[]) null);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "initDeclaredMethods failed:" + e.toString());
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "initDeclaredMethods failed:" + e2.toString());
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "initDeclaredMethods failed:" + e3.toString());
        } catch (IllegalArgumentException e4) {
            Log.e(TAG, "initDeclaredMethods failed:" + e4.toString());
        } catch (InvocationTargetException e5) {
            Log.e(TAG, "initDeclaredMethods failed:" + e5.toString());
        } catch (NullPointerException e6) {
            Log.e(TAG, "initDeclaredMethods failed:" + e6.toString());
        }
    }

    public static boolean isPartOfMultiWindow(int aTaskId) {
        Method method = sIsPartOfMultiWindowMethod;
        if (method != null) {
            try {
                return ((Boolean) method.invoke(sMultiWinService, new Object[]{Integer.valueOf(aTaskId)})).booleanValue();
            } catch (Exception e) {
                Log.d(TAG, "call method " + method.getName() + " failed !!!", e);
            }
        }
        return false;
    }

    public static boolean getMWMaintained() {
        Method method = sGetMWMaintained;
        if (method != null) {
            try {
                return ((Boolean) method.invoke(sMultiWinService, (Object[]) null)).booleanValue();
            } catch (Exception e) {
                Log.d(TAG, "call method " + method.getName() + " failed !!!", e);
            }
        }
        return false;
    }
}
