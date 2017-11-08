package com.huawei.powergenie.integration.adapter;

import java.lang.reflect.Method;

public class ReflectUtils {
    public static Object invokeMethod(String methodName, Object instance, Object[] args) throws UnsupportedOperationException {
        try {
            return getMethod(methodName, instance, args).invoke(instance, args);
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnsupportedOperationException(e);
        }
    }

    public static Object invokeMethod(String methodName, String claszzName, Object[] args) throws UnsupportedOperationException {
        try {
            return getMethod(methodName, Class.forName(claszzName), args).invoke(null, args);
        } catch (Exception exp) {
            throw new UnsupportedOperationException(exp);
        }
    }

    public static Method getMethod(String methodName, Object instance, Object[] args) throws UnsupportedOperationException {
        return getMethod(methodName, instance.getClass(), args);
    }

    public static Method getMethod(String methodName, Class<?> clazz, Object[] args) throws UnsupportedOperationException {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)) {
                Class<?>[] ptypes = method.getParameterTypes();
                if (args == null) {
                    if (ptypes.length == 0) {
                        return method;
                    }
                } else if (args.length == ptypes.length) {
                    return method;
                }
            }
        }
        throw new UnsupportedOperationException();
    }
}
