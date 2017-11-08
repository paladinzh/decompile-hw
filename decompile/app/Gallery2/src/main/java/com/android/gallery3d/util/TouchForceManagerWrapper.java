package com.android.gallery3d.util;

import android.content.Context;
import com.android.gallery3d.util.Wrapper.ReflectCaller;
import java.lang.reflect.InvocationTargetException;

public class TouchForceManagerWrapper extends Wrapper {
    private static boolean mIsSupportForce = false;
    private static float mPressureLimit = 0.0f;

    private static class SimpleCaller implements ReflectCaller {
        private static Class<?> sClass;
        private Context mContext;

        static {
            try {
                sClass = Class.forName("com.huawei.android.hardware.toucforce.TouchForceManager");
            } catch (ClassNotFoundException e) {
                GalleryLog.e("TouchForceManager", "load class com.huawei.android.hardware.toucforce.TouchForceManager failed");
            }
        }

        private SimpleCaller(Context context) {
            this.mContext = context;
        }

        public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
            Object instance = sClass.getConstructor(new Class[]{Context.class}).newInstance(new Object[]{this.mContext});
            GalleryLog.d("TouchForceManager", "isSupportForce of com.huawei.android.hardware.toucforce.TouchForceManager is called.");
            Object ret = sClass.getMethod("isSupportForce", new Class[0]).invoke(instance, new Object[0]);
            TouchForceManagerWrapper.mIsSupportForce = ret == null ? false : ((Boolean) ret).booleanValue();
            Object ret1 = sClass.getMethod("getPressureLimit", new Class[0]).invoke(instance, new Object[0]);
            TouchForceManagerWrapper.mPressureLimit = ret1 == null ? 0.0f : ((Float) ret1).floatValue();
            return null;
        }
    }

    public TouchForceManagerWrapper(Context context) {
        Wrapper.runCaller(new SimpleCaller(context), new Object[0]);
    }

    public boolean isSupportForce() {
        return mIsSupportForce;
    }

    public float getPressureLimit() {
        return mPressureLimit;
    }
}
