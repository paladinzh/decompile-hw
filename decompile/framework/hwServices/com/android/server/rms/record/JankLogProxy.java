package com.android.server.rms.record;

import android.util.Log;
import com.huawei.utils.reflect.HwReflectUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class JankLogProxy {
    private static final Class<?> CLASS_Jlog = HwReflectUtils.getClass("android.util.Jlog");
    private static final Class<?> CLASS_JlogConstants = HwReflectUtils.getClass("android.util.JlogConstants");
    private static final Field FIELD_RESOURCE_MANAGER = HwReflectUtils.getField(CLASS_JlogConstants, "JLID_RESOURCE_MANAGER");
    private static final int JlogID = HwReflectUtils.objectToInt(HwReflectUtils.getFieldValue(null, FIELD_RESOURCE_MANAGER));
    private static final Method METHOD_Jlogd_Arg = HwReflectUtils.getMethod(CLASS_Jlog, "d", new Class[]{Integer.TYPE, String.class, Integer.TYPE, String.class});
    private static final String TAG = "RMS.JankLogProxy";
    private static JankLogProxy mJankLogProxy;
    private final Object mJLog = getJlogInstance(CLASS_Jlog);

    public static synchronized JankLogProxy getInstance() {
        JankLogProxy jankLogProxy;
        synchronized (JankLogProxy.class) {
            if (mJankLogProxy == null) {
                mJankLogProxy = new JankLogProxy();
            }
            jankLogProxy = mJankLogProxy;
        }
        return jankLogProxy;
    }

    private static Object getJlogInstance(Class<?> targetClass) {
        if (targetClass == null) {
            return null;
        }
        try {
            Constructor<?> CONSTRUCTOR_Jlog = targetClass.getDeclaredConstructor(new Class[0]);
            if (CONSTRUCTOR_Jlog != null) {
                CONSTRUCTOR_Jlog.setAccessible(true);
            }
            return HwReflectUtils.newInstance(CONSTRUCTOR_Jlog, new Object[0]);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "Jlog method not found in class " + targetClass);
            return null;
        } catch (Exception e2) {
            Log.e(TAG, "Unknown exception while trying to get Jlog Instance");
            return null;
        }
    }

    public int jlog_d(String arg1, int arg2, String msg) {
        int result = -1;
        try {
            if (this.mJLog != null) {
                result = ((Integer) HwReflectUtils.invoke(this.mJLog, METHOD_Jlogd_Arg, new Object[]{Integer.valueOf(JlogID), arg1, Integer.valueOf(arg2), msg})).intValue();
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Unknown exception while trying to invoke [Jlog.d.arg]");
        }
        return result;
    }
}
