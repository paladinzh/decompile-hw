package com.android.keyguard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class HwCustManager {
    public static final String CLASS_HWCUSTUTIL = "com.huawei.cust.HwCustUtils";
    public static final String METHOD_CREATEOBJ = "createObj";
    private static HwCustManager sInstance;
    private Method mCreateObjMethod;
    private HashMap<Class<?>, Object> mCustHashMap = new HashMap();

    public HwCustManager() {
        try {
            this.mCreateObjMethod = Class.forName(CLASS_HWCUSTUTIL).getMethod(METHOD_CREATEOBJ, new Class[]{Class.class, Object[].class});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        }
    }

    public static HwCustManager getInstance() {
        if (sInstance == null) {
            sInstance = new HwCustManager();
        }
        return sInstance;
    }

    public Object getHwCustObj(Class<?> classClass, Object... args) {
        return getHwCustObj(classClass, false, args);
    }

    public Object getHwCustObj(Class<?> classClass, boolean isClear, Object... args) {
        if (this.mCreateObjMethod == null) {
            return null;
        }
        Object obj = this.mCustHashMap.get(classClass);
        if (isClear) {
            this.mCustHashMap.remove(classClass);
            obj = null;
        }
        if (obj == null) {
            try {
                obj = this.mCreateObjMethod.invoke(null, new Object[]{classClass, args});
                if (obj != null) {
                    this.mCustHashMap.put(classClass, obj);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                if (obj != null) {
                    this.mCustHashMap.put(classClass, obj);
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                if (obj != null) {
                    this.mCustHashMap.put(classClass, obj);
                }
            } catch (InvocationTargetException e3) {
                e3.printStackTrace();
                if (obj != null) {
                    this.mCustHashMap.put(classClass, obj);
                }
            } catch (Throwable th) {
                if (obj != null) {
                    this.mCustHashMap.put(classClass, obj);
                }
            }
        }
        return obj;
    }

    public static synchronized void clearCustMap() {
        synchronized (HwCustManager.class) {
            if (sInstance != null) {
                sInstance.mCustHashMap.clear();
                sInstance = null;
            }
        }
    }
}
