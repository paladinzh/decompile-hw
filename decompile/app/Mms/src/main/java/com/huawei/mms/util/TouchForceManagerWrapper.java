package com.huawei.mms.util;

import android.content.Context;
import com.huawei.cspcommon.MLog;
import java.lang.reflect.InvocationTargetException;

public class TouchForceManagerWrapper {
    private static Class<?> sClass;
    private Context mContext;

    static {
        try {
            sClass = Class.forName("com.huawei.android.hardware.toucforce.TouchForceManager");
        } catch (ClassNotFoundException e) {
            MLog.e("TouchForceManager", e.getMessage());
        }
    }

    public TouchForceManagerWrapper(Context context) {
        this.mContext = context;
    }

    public boolean isSupportForce() throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        boolean z = false;
        if (sClass == null) {
            return false;
        }
        Object instance = sClass.getConstructor(new Class[]{Context.class}).newInstance(new Object[]{this.mContext});
        MLog.d("TouchForceManager", "isSupportForce of com.huawei.android.hardware.toucforce.TouchForceManager is called.");
        Object ret = sClass.getMethod("isSupportForce", new Class[0]).invoke(instance, new Object[0]);
        if (ret != null) {
            z = ((Boolean) ret).booleanValue();
        }
        return z;
    }
}
