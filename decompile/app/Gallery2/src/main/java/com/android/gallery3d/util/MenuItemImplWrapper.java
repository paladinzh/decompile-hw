package com.android.gallery3d.util;

import com.android.gallery3d.util.Wrapper.InstanceMethodCaller;
import com.android.gallery3d.util.Wrapper.ReflectCaller;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MenuItemImplWrapper extends Wrapper {
    private ReflectCaller mSetProgressStatus;

    private static class InstanceCreator implements ReflectCaller {
        private Class<?> sMenuItemImpl;
        private Method setProgressStatus;

        private InstanceCreator() {
        }

        public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
            this.sMenuItemImpl = Class.forName("com.android.internal.view.menu.MenuItemImpl");
            this.setProgressStatus = this.sMenuItemImpl.getDeclaredMethod("setProgressStatus", new Class[]{Integer.TYPE, Integer.TYPE});
            return null;
        }
    }

    public MenuItemImplWrapper() {
        InstanceCreator creator = new InstanceCreator();
        Wrapper.runCaller(creator, new Object[0]);
        this.mSetProgressStatus = new InstanceMethodCaller(creator.setProgressStatus);
    }

    public void setProgressStatus(Object obj, int status, int progress) {
        Wrapper.runCaller(this.mSetProgressStatus, obj, Integer.valueOf(status), Integer.valueOf(progress));
    }
}
