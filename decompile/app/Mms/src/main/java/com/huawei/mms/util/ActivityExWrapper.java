package com.huawei.mms.util;

import android.app.Activity;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;

public class ActivityExWrapper {
    public static final boolean IS_PRESS_SUPPORT = false;
    private static Class<?> sClass;
    private Activity mActivity;

    static {
        try {
            sClass = Class.forName("android.app.Activity");
        } catch (ClassNotFoundException e) {
            Log.e("ActivityExWrapper", "load class com.huawei.android.app.ActivityEx failed");
        } finally {
        }
    }

    public ActivityExWrapper(Activity activity) {
        this.mActivity = activity;
    }

    public Object run(String method) {
        Object ret = null;
        try {
            ret = sClass.getMethod(method, new Class[0]).invoke(this.mActivity, new Object[0]);
        } catch (IllegalArgumentException e) {
            Log.e("ActivityExWrapper", "IllegalArgumentException " + e.getMessage());
        } catch (InvocationTargetException e2) {
            Log.e("ActivityExWrapper", "InvocationTargetException " + e2.getMessage());
        } catch (IllegalAccessException e3) {
            Log.e("ActivityExWrapper", "IllegalAccessException " + e3.getMessage());
        } catch (NoSuchMethodException e4) {
            Log.e("ActivityExWrapper", "NoSuchMethodException " + e4.getMessage());
        } catch (NullPointerException e5) {
            Log.e("ActivityExWrapper", "NullPointerException " + e5.getMessage());
        } catch (Exception e6) {
            Log.e("ActivityExWrapper", "unknow exception " + e6.getMessage());
        }
        return ret;
    }
}
