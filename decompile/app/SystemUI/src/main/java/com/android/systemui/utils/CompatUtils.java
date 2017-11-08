package com.android.systemui.utils;

import android.os.Build.VERSION;
import android.util.Log;
import com.android.internal.policy.IKeyguardService;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CompatUtils {
    private static final String TAG = CompatUtils.class.getSimpleName();
    public static final int mAPIleve = VERSION.SDK_INT;
    static Method rmTaskMethod = null;

    public static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "className not found:" + className);
            return null;
        }
    }

    public static Method getMethod(Class<?> targetClass, String name, Class<?>... parameterTypes) {
        if (targetClass == null || isEmpty(name)) {
            return null;
        }
        try {
            return targetClass.getMethod(name, parameterTypes);
        } catch (SecurityException e) {
            Log.w(TAG, e.getCause());
            return null;
        } catch (NoSuchMethodException e2) {
            Log.w(TAG, name + ", not such method.");
            return null;
        }
    }

    public static Field getField(Class<?> targetClass, String name) {
        if (targetClass == null || isEmpty(name)) {
            return null;
        }
        try {
            return targetClass.getField(name);
        } catch (SecurityException e) {
            Log.w(TAG, e.getCause());
            return null;
        } catch (NoSuchFieldException e2) {
            Log.w(TAG, name + ", no such field.");
            return null;
        }
    }

    public static Object invoke(Object receiver, Method method, Object... args) {
        if (method == null) {
            throw new UnsupportedOperationException();
        }
        try {
            return method.invoke(receiver, args);
        } catch (RuntimeException re) {
            Log.e(TAG, "Exception in invoke: " + re.getClass().getSimpleName());
            if ("com.huawei.android.util.NoExtAPIException".equals(re.getClass().getName())) {
                throw new UnsupportedOperationException();
            }
            throw new UnsupportedOperationException();
        } catch (Exception e) {
            Log.e(TAG, "Exception in invoke: " + e.getCause() + "; method=" + method.getName());
            throw new UnsupportedOperationException();
        }
    }

    public static Object getFieldValue(Object receiver, Field field) {
        if (field == null) {
            return null;
        }
        try {
            return field.get(receiver);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getFieldValue: " + e.getClass().getSimpleName());
            throw new UnsupportedOperationException();
        }
    }

    private static boolean isEmpty(String str) {
        boolean z = true;
        if (str == null || str.length() == 0) {
            return true;
        }
        if (str.trim().length() != 0) {
            z = false;
        }
        return z;
    }

    public static final void useKeyguardTouchDelegateMethod(IKeyguardService mService, String methodname) {
        if (mService == null) {
            Log.d(TAG, "useKeyguardTouchDelegateMethod is not be executed because IKeyguardService is NULL");
            return;
        }
        try {
            if (mAPIleve <= 21) {
                mService.getClass().getMethod(methodname, new Class[0]).invoke(mService, new Object[0]);
            }
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "Android API = " + mAPIleve + " and Exception in useKeyguardTouchDelegateMethod: " + e.getCause());
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "Android API = " + mAPIleve + " and Exception in useKeyguardTouchDelegateMethod: " + e2.getCause());
        } catch (IllegalArgumentException e3) {
            Log.e(TAG, "Android API = " + mAPIleve + " and Exception in useKeyguardTouchDelegateMethod: " + e3.getCause());
        } catch (InvocationTargetException e4) {
            Log.e(TAG, "Android API = " + mAPIleve + " and Exception in useKeyguardTouchDelegateMethod: " + e4.getCause());
        } catch (SecurityException e5) {
            Log.e(TAG, "Android API = " + mAPIleve + " and Exception in useKeyguardTouchDelegateMethod: " + e5.getCause());
        }
    }

    public static int objectToInt(Object o) {
        if (o == null) {
            return -1;
        }
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) {
            return -1;
        }
    }
}
