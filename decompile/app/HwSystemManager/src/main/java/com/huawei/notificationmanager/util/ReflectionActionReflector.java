package com.huawei.notificationmanager.util;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

/* compiled from: ReflectionActionWrapper */
class ReflectionActionReflector {
    private static final String CLASSNAME_REFLECTIONACTION = "ReflectionAction";
    private static final String FIELDNAME_METHODNAME = "methodName";
    private static final String FIELDNAME_TYPE = "type";
    private static final String FIELDNAME_VALUE = "value";
    public static Class<?> clazz;
    public static Field methodName;
    public static Field type;
    public static Field value;

    ReflectionActionReflector() {
    }

    static {
        clazz = null;
        methodName = null;
        type = null;
        value = null;
        for (Class<?> thisClass : RemoteViewsReflector.clazz.getDeclaredClasses()) {
            if (CLASSNAME_REFLECTIONACTION.equals(thisClass.getSimpleName())) {
                clazz = thisClass;
                break;
            }
        }
        if (clazz != null) {
            try {
                methodName = clazz.getDeclaredField(FIELDNAME_METHODNAME);
                type = clazz.getDeclaredField("type");
                value = clazz.getDeclaredField("value");
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    public Void run() {
                        ReflectionActionReflector.methodName.setAccessible(true);
                        ReflectionActionReflector.type.setAccessible(true);
                        ReflectionActionReflector.value.setAccessible(true);
                        return null;
                    }
                });
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (SecurityException e2) {
                e2.printStackTrace();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        }
    }

    public static int getType(Object object) {
        try {
            return type.getInt(object);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return 0;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return 0;
        }
    }

    public static String getMethod(Object object) {
        try {
            return (String) methodName.get(object);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static Object getValue(Object object) {
        try {
            return value.get(object);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return null;
        }
    }
}
