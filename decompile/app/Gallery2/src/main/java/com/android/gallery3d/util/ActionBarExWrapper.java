package com.android.gallery3d.util;

import android.app.ActionBar.Tab;
import com.android.gallery3d.util.Wrapper.ReflectCaller;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ActionBarExWrapper extends Wrapper {
    private static ReflectCaller mSetTabViewId;

    private static class InstanceCreator implements ReflectCaller {
        private Method mSetTabViewId;
        private Class<?> sActionBarEx;

        private InstanceCreator() {
        }

        public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
            this.sActionBarEx = Class.forName("com.huawei.android.app.ActionBarEx");
            this.mSetTabViewId = this.sActionBarEx.getDeclaredMethod("setTabViewId", new Class[]{Tab.class, Integer.TYPE});
            return null;
        }
    }

    static {
        final InstanceCreator creator = new InstanceCreator();
        Wrapper.runCaller(creator, new Object[0]);
        mSetTabViewId = new ReflectCaller() {
            Method method = creator.mSetTabViewId;

            public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
                return this.method.invoke(null, new Object[]{para[0], para[1]});
            }
        };
    }

    public static void setTabViewId(Object tab, int id) {
        Wrapper.runCaller(mSetTabViewId, tab, Integer.valueOf(id));
    }
}
