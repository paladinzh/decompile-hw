package com.huawei.powergenie.integration.adapter;

import android.os.IBinder;
import android.os.ServiceManager;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MultiWinServiceAdapter {
    private static Class mMultiWinClass = null;
    private static Method sGetMWMaintainedMethod = null;
    private static Method sGetMultiWinCountMethod = null;
    private static Method sIsMultiWinMethod = null;
    private static Method sIsPartOfMultiWindowMethod = null;
    private static Object sMultiWinService;
    private static Method sMultiWindowMethod = null;

    static {
        initDeclaredMethods();
    }

    private static void initDeclaredMethods() {
        Class<?>[] isPartOfMultiWindowArgs = new Class[]{Integer.TYPE};
        Class<?>[] isMultiWinArgs = new Class[]{IBinder.class};
        try {
            sMultiWinService = Class.forName("android.os.IMultiWinService$Stub").getMethod("asInterface", new Class[]{IBinder.class}).invoke(null, new Object[]{ServiceManager.getService("multiwin")});
            Class<?> clazz = sMultiWinService.getClass();
            sGetMWMaintainedMethod = clazz.getDeclaredMethod("getMWMaintained", (Class[]) null);
            sIsPartOfMultiWindowMethod = clazz.getDeclaredMethod("isPartOfMultiWindow", isPartOfMultiWindowArgs);
            sIsMultiWinMethod = clazz.getDeclaredMethod("isMultiWin", isMultiWinArgs);
            sGetMultiWinCountMethod = clazz.getDeclaredMethod("getMultiWinCount", (Class[]) null);
            Log.i("MultiWinServiceAdapter", "support multi window");
        } catch (ClassNotFoundException e) {
            Log.e("MultiWinServiceAdapter", "initDeclaredMethods failed:" + e.toString());
        } catch (NoSuchMethodException e2) {
            Log.e("MultiWinServiceAdapter", "initDeclaredMethods failed:" + e2.toString());
        } catch (IllegalAccessException e3) {
            Log.e("MultiWinServiceAdapter", "initDeclaredMethods failed:" + e3.toString());
        } catch (IllegalArgumentException e4) {
            Log.e("MultiWinServiceAdapter", "initDeclaredMethods failed:" + e4.toString());
        } catch (InvocationTargetException e5) {
            Log.e("MultiWinServiceAdapter", "initDeclaredMethods failed:" + e5.toString());
        } catch (NullPointerException e6) {
            Log.e("MultiWinServiceAdapter", "initDeclaredMethods failed:" + e6.toString());
        }
    }

    public static int getMWMaintained() {
        int i = 1;
        try {
            if (sMultiWindowMethod == null) {
                mMultiWinClass = Class.forName("com.huawei.android.app.HwMultiWindowEx");
                sMultiWindowMethod = mMultiWinClass.getMethod("isInMultiWindowMode", new Class[0]);
            }
            return ((Boolean) sMultiWindowMethod.invoke(mMultiWinClass, new Object[0])).booleanValue() ? 1 : 0;
        } catch (Exception e) {
            Log.e("MultiWinServiceAdapter", "call method isInMultiWindowMode failed !!!");
            Method method = sGetMWMaintainedMethod;
            if (method == null) {
                return -1;
            }
            try {
                if (!((Boolean) method.invoke(sMultiWinService, (Object[]) null)).booleanValue()) {
                    i = 0;
                }
                return i;
            } catch (Exception e2) {
                Log.d("MultiWinServiceAdapter", "call method " + method.getName() + " failed !!!", e2);
                return -1;
            }
        }
    }
}
