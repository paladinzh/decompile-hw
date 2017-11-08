package com.android.gallery3d.util;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View.OnClickListener;
import com.android.gallery3d.util.Wrapper.ReflectCaller;
import java.lang.reflect.InvocationTargetException;

public class ActivityExWrapper extends Wrapper {
    public static final boolean IS_PRESS_SUPPORT;
    private Activity mActivity;
    private ReflectCaller mAddPeekActionCallerSDL = new ReflectCaller() {
        public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
            return ActivityExWrapper.this.mActivity.getClass().getMethod("addPeekAction", new Class[]{String.class, Drawable.class, OnClickListener.class}).invoke(ActivityExWrapper.this.mActivity, para);
        }
    };
    private ReflectCaller mAddPeekActionCallerSDLC = new ReflectCaller() {
        public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
            return ActivityExWrapper.this.mActivity.getClass().getMethod("addPeekAction", new Class[]{String.class, Drawable.class, OnClickListener.class, Integer.TYPE}).invoke(ActivityExWrapper.this.mActivity, para);
        }
    };
    private ReflectCaller mReplacePeekActionCallerPSDL = new ReflectCaller() {
        public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
            return ActivityExWrapper.this.mActivity.getClass().getMethod("replacePeekAction", new Class[]{Integer.TYPE, String.class, Drawable.class, OnClickListener.class}).invoke(ActivityExWrapper.this.mActivity, para);
        }
    };
    private ReflectCaller mReplacePeekActionCallerPSDLC = new ReflectCaller() {
        public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
            return ActivityExWrapper.this.mActivity.getClass().getMethod("replacePeekAction", new Class[]{Integer.TYPE, String.class, Drawable.class, OnClickListener.class, Integer.TYPE}).invoke(ActivityExWrapper.this.mActivity, para);
        }
    };
    private ReflectCaller mRunMethodCaller = new ReflectCaller() {
        public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
            return ActivityExWrapper.this.mActivity.getClass().getMethod(para[0], new Class[0]).invoke(ActivityExWrapper.this.mActivity, new Object[0]);
        }
    };

    static {
        boolean z = true;
        z = false;
        try {
            new Activity().getClass().getMethod("addPeekAction", new Class[]{String.class, Drawable.class, OnClickListener.class});
        } catch (NoSuchMethodException e) {
            GalleryLog.e("TouchForceManager", "NoSuchMethodException com.huawei.android.app.Activity failed");
        } finally {
            IS_PRESS_SUPPORT = z;
        }
    }

    public ActivityExWrapper(Activity activity) {
        this.mActivity = activity;
    }

    public Object run(String method) {
        return Wrapper.runCaller(this.mRunMethodCaller, method);
    }
}
