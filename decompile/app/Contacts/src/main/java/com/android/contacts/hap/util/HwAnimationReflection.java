package com.android.contacts.hap.util;

import android.content.Context;
import com.android.contacts.util.HwLog;
import dalvik.system.PathClassLoader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HwAnimationReflection {
    private static final boolean DEBUG = HwLog.HWDBG;
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
                HwLog.e("HwAnimationReflection", "overrideTransition " + e.toString());
            } catch (IllegalAccessException e2) {
                HwLog.e("HwAnimationReflection", "overrideTransition " + e2.toString());
            } catch (InvocationTargetException e3) {
                HwLog.e("HwAnimationReflection", "overrideTransition " + e3.toString());
            }
        }
    }

    private void initAnimUtilObjectAndMethods(Context client) {
        Class animUtilClass = null;
        try {
            animUtilClass = new PathClassLoader("/system/framework/", client.getClassLoader()).loadClass("com.huawei.hwanimation.AnimUtil");
        } catch (ClassNotFoundException e) {
            HwLog.e("HwAnimationReflection", "initAnimUtilObjectAndMethods : " + e.toString());
        }
        if (animUtilClass == null) {
            HwLog.e("HwAnimationReflection", "initAnimUtilObjectAndMethods : cann't construct of AniUtil class object");
            return;
        }
        try {
            this.mOverrideTransitionMethod = animUtilClass.getDeclaredMethod("overrideTransition", new Class[]{Integer.TYPE});
        } catch (NoSuchMethodException e2) {
            HwLog.e("HwAnimationReflection", "initAnimUtilObjectAndMethods : " + e2.toString());
        }
        if (this.mOverrideTransitionMethod == null) {
            HwLog.e("HwAnimationReflection", "initAnimUtilObjectAndMethods : cann't get the method of overrideTransiton defined in AnimUtil");
            return;
        }
        Constructor constructor = null;
        try {
            constructor = animUtilClass.getConstructor(new Class[]{Context.class});
        } catch (NoSuchMethodException e22) {
            HwLog.e("HwAnimationReflection", "initAnimUtilObjectAndMethods : " + e22.toString());
        }
        if (constructor == null) {
            HwLog.e("HwAnimationReflection", "initAnimUtilObjectAndMethods : cann't get constructor method of AnimUtil");
            return;
        }
        try {
            this.mAnimUtilObject = constructor.newInstance(new Object[]{client});
        } catch (IllegalArgumentException e3) {
            HwLog.e("HwAnimationReflection", "initAnimUtilObjectAndMethods : " + e3.toString());
        } catch (InstantiationException e4) {
            HwLog.e("HwAnimationReflection", "initAnimUtilObjectAndMethods : " + e4.toString());
        } catch (IllegalAccessException e5) {
            HwLog.e("HwAnimationReflection", "initAnimUtilObjectAndMethods : " + e5.toString());
        } catch (InvocationTargetException e6) {
            HwLog.e("HwAnimationReflection", "initAnimUtilObjectAndMethods : " + e6.toString());
        }
    }
}
