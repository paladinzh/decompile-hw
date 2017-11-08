package com.android.settings;

import android.content.Context;
import dalvik.system.PathClassLoader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HwAnimationReflection {
    private Object mAnimUtilObject = null;
    private Method mOverrideTransitionMethod = null;

    public HwAnimationReflection(Context context) {
        initAnimUtilObjectAndMethods(context);
    }

    public void overrideTransition(int transit) {
        if (this.mOverrideTransitionMethod != null && this.mAnimUtilObject != null) {
            try {
                this.mOverrideTransitionMethod.invoke(this.mAnimUtilObject, new Object[]{Integer.valueOf(transit)});
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e2) {
            } catch (InvocationTargetException e3) {
            }
        }
    }

    private void initAnimUtilObjectAndMethods(Context client) {
        Class animUtilClass = null;
        try {
            animUtilClass = new PathClassLoader("/system/framework/", client.getClassLoader()).loadClass("com.huawei.hwanimation.AnimUtil");
        } catch (ClassNotFoundException e) {
        }
        if (animUtilClass != null) {
            try {
                this.mOverrideTransitionMethod = animUtilClass.getDeclaredMethod("overrideTransition", new Class[]{Integer.TYPE});
            } catch (NoSuchMethodException e2) {
            }
            if (this.mOverrideTransitionMethod != null) {
                Constructor constructor = null;
                try {
                    constructor = animUtilClass.getConstructor(new Class[]{Context.class});
                } catch (NoSuchMethodException e3) {
                }
                if (constructor != null) {
                    try {
                        this.mAnimUtilObject = constructor.newInstance(new Object[]{client});
                    } catch (IllegalArgumentException e4) {
                    } catch (InstantiationException e5) {
                    } catch (IllegalAccessException e6) {
                    } catch (InvocationTargetException e7) {
                    }
                    if (this.mAnimUtilObject != null) {
                    }
                }
            }
        }
    }
}
