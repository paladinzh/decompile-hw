package com.android.gallery3d.util;

import android.content.Context;
import com.android.gallery3d.R;
import com.android.gallery3d.util.Wrapper.ReflectCaller;
import com.android.gallery3d.util.Wrapper.StaticMethodCaller;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HwWidgetFactoryWrapper extends Wrapper {
    private ReflectCaller mGetPrimaryColor;

    private static class InstanceCreator implements ReflectCaller {
        private static Class<?> sHwWigetFactoryClass;
        private Method getPrimaryColor;

        private InstanceCreator() {
        }

        static {
            try {
                sHwWigetFactoryClass = Class.forName("android.hwcontrol.HwWidgetFactory");
            } catch (ClassNotFoundException e) {
                GalleryLog.e("HwWidgetFactoryWrapper", "load class android.hwcontrol.HwWidgetFactory failed");
            }
        }

        public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
            if (sHwWigetFactoryClass == null) {
                return null;
            }
            Constructor<?> c = sHwWigetFactoryClass.getConstructor(new Class[0]);
            this.getPrimaryColor = sHwWigetFactoryClass.getDeclaredMethod("getPrimaryColor", new Class[]{Context.class});
            return c.newInstance(new Object[0]);
        }
    }

    public HwWidgetFactoryWrapper() {
        InstanceCreator creator = new InstanceCreator();
        Wrapper.runCaller(creator, new Object[0]);
        this.mGetPrimaryColor = new StaticMethodCaller(creator.getPrimaryColor);
    }

    public int getPrimaryColor(Context context) {
        Object obj = Wrapper.runCaller(this.mGetPrimaryColor, context);
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return context.getResources().getColor(R.color.actionbar_background_color);
    }
}
