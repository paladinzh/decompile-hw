package com.huawei.watermark.wmutil;

import android.util.Log;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectClass {
    private static final String TAG = ReflectClass.class.getSimpleName();
    private Class clazz;
    private Constructor ctor;
    private Method[] methods;

    public ReflectClass(String className, Class<?>... ctorParamTypes) {
        this.clazz = getClass(className);
        if (this.clazz != null) {
            this.ctor = getConstructor(ctorParamTypes);
            this.methods = this.clazz.getMethods();
        }
    }

    public Object invokeS(String methodName, Object... params) {
        Method method = findMethod(methodName);
        if (method != null) {
            try {
                return method.invoke(this.clazz, params);
            } catch (IllegalAccessException e) {
                Log.e(TAG, String.format("reflectInvoke(%s) IllegalAccessException", new Object[]{methodName}));
            } catch (InvocationTargetException e2) {
                Log.e(TAG, String.format("reflectInvoke(%s) InvocationTargetException", new Object[]{methodName}));
            }
        }
        return null;
    }

    public Object getStaticField(String fieldName) {
        if (this.clazz == null) {
            Log.e(TAG, "invoke getStaticField with null clazz");
            return null;
        }
        try {
            return this.clazz.getDeclaredField(fieldName).get(null);
        } catch (NoSuchFieldException e) {
            Log.d(TAG, "NoSuchFieldException: " + this.clazz.getSimpleName() + " fieldName, " + e.getMessage());
            return null;
        } catch (IllegalAccessException e2) {
            Log.d(TAG, "IllegalAccessException: " + this.clazz.getSimpleName() + " fieldName, " + e2.getMessage());
            return null;
        } catch (Exception e3) {
            Log.d(TAG, "Exception: " + this.clazz.getSimpleName() + " fieldName, " + e3.getMessage());
            return null;
        }
    }

    private Constructor getConstructor(Class<?>[] paramTypes) {
        if (this.clazz == null) {
            Log.e(TAG, "invoke getConstructor with null clazz");
            return null;
        }
        try {
            return this.clazz.getConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            Log.d(TAG, "NoSuchMethodException: " + this.clazz.getSimpleName() + " getConstructor");
            return null;
        }
    }

    private Class getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "ClassNotFoundException: " + className);
            return null;
        }
    }

    private Method findMethod(String methodName) {
        if (this.methods == null) {
            return null;
        }
        for (Method method : this.methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        Log.e(TAG, "Can't findMethod method: " + methodName);
        return null;
    }
}
