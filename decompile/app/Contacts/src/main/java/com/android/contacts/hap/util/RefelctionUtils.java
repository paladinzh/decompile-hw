package com.android.contacts.hap.util;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import com.android.contacts.util.HwLog;
import dalvik.system.DexClassLoader;
import java.lang.reflect.Method;

public abstract class RefelctionUtils {
    private static final String TAG = RefelctionUtils.class.getSimpleName();

    private RefelctionUtils() {
    }

    public static Object invokeMethod(String methodName, Object instance, Object[] args) throws UnsupportedException {
        try {
            return getMethod(methodName, instance, args).invoke(instance, args);
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnsupportedException(e);
        }
    }

    public static Object invokeMethod(String methodName, Class<?> clazz, Object[] args) throws UnsupportedException {
        try {
            return getMethod(methodName, (Class) clazz, args).invoke(null, args);
        } catch (Exception exp) {
            throw new UnsupportedException(exp);
        }
    }

    public static Method getMethod(String methodName, Object instance, Object[] args) throws UnsupportedException {
        return getMethod(methodName, instance.getClass(), args);
    }

    public static Method getMethod(String methodName, Class<?> clazz, Object[] args) throws UnsupportedException {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)) {
                Class<?>[] ptypes = method.getParameterTypes();
                if (args == null) {
                    if (ptypes == null || ptypes.length == 0) {
                        return method;
                    }
                } else if (args.length == ptypes.length) {
                    return method;
                }
            }
        }
        throw new UnsupportedException();
    }

    public static Object getStaticVariableValue(Class<?> clazz, String name) throws UnsupportedException {
        try {
            return clazz.getField(name).get(null);
        } catch (NoSuchFieldException exp) {
            throw new UnsupportedException(exp);
        } catch (IllegalArgumentException exp2) {
            throw new UnsupportedException(exp2);
        } catch (IllegalAccessException exp3) {
            throw new UnsupportedException(exp3);
        }
    }

    public static Object getStaticVariableValue(String clazzName, String variableName) throws UnsupportedException {
        try {
            return getStaticVariableValue(Class.forName(clazzName), variableName);
        } catch (Exception exp) {
            throw new UnsupportedException(exp);
        }
    }

    public static Object invokeInnerClass(String aClassName, String aInnderClass, String method, Object[] args) throws UnsupportedException {
        try {
            Class<?>[] innerClass = Class.forName(aClassName).getClasses();
            if (innerClass != null) {
                for (Class tempClass : innerClass) {
                    if (tempClass.getSimpleName().equals(aInnderClass)) {
                        return invokeMethod(method, tempClass, args);
                    }
                }
            }
            return null;
        } catch (Exception exp) {
            throw new UnsupportedException(exp);
        }
    }

    public static boolean isLibraryInstalled(Context context, String libName) {
        try {
            String[] libs = context.getPackageManager().getSystemSharedLibraryNames();
            if (libs != null) {
                for (String lib : libs) {
                    if (libName.equals(lib)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            HwLog.e(TAG, "isLibraryInstalled libName(" + libName + ") " + e);
        }
        return false;
    }

    private static DexClassLoader loadDex(String dexPath, Context context) {
        if (context == null) {
            return null;
        }
        if (dexPath == null) {
            dexPath = "/system/framework/com.android.contacts.separated.jar";
        }
        return new DexClassLoader(dexPath, context.getDir("dex", 0).getAbsolutePath(), null, context.getClassLoader());
    }

    public static Object invokeMethodFromDex(String dexPath, Context context, String clazzName, String methodName, Class<?>[] parameterTypes, Object instance, Object[] args) {
        DexClassLoader dexLoader = loadDex(dexPath, context);
        if (dexLoader == null) {
            return null;
        }
        try {
            Method method = dexLoader.loadClass(clazzName).getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(instance, args);
        } catch (Exception e) {
            String str = TAG;
            StringBuilder append = new StringBuilder().append("invokeMethodFromDex dexPath(");
            if (dexPath == null) {
                dexPath = "null";
            }
            HwLog.w(str, append.append(dexPath).append(") class(").append(clazzName).append(") ").append(" method(").append(methodName).append(") ").append(e).toString());
            return null;
        }
    }

    public static boolean isAppInstalled(Context context, String appPackageName) {
        if (appPackageName == null || context == null) {
            return false;
        }
        try {
            context.getPackageManager().getPackageInfo(appPackageName, 0);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
}
