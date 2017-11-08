package com.android.gallery3d.util;

import android.content.Context;
import com.android.gallery3d.util.Wrapper.ReflectCaller;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ImmersionStyleWrapper extends Wrapper {
    private static ReflectCaller sGetPrimaryColorCaller;
    private static ReflectCaller sGetSuggestionForgroundColorStyle;
    private static boolean sIsImmersionSupported;

    private static class InitCaller implements ReflectCaller {
        private Method getPrimaryColor;
        private Method getSuggestionForgroundColorStyle;
        private boolean success;

        private InitCaller() {
            this.success = false;
        }

        public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException {
            Class<?> clazz = Class.forName("com.huawei.android.immersion.ImmersionStyle");
            this.getPrimaryColor = clazz.getDeclaredMethod("getPrimaryColor", new Class[]{Context.class});
            this.getSuggestionForgroundColorStyle = clazz.getDeclaredMethod("getSuggestionForgroundColorStyle", new Class[]{Integer.TYPE});
            this.success = true;
            return null;
        }
    }

    static {
        final InitCaller initor = new InitCaller();
        Wrapper.runCaller(initor, new Object[0]);
        sIsImmersionSupported = initor.success;
        sGetPrimaryColorCaller = new ReflectCaller() {
            Method mMethod = initor.getPrimaryColor;

            public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException {
                return this.mMethod.invoke(null, new Object[]{para[0]});
            }
        };
        sGetSuggestionForgroundColorStyle = new ReflectCaller() {
            Method mMethod = initor.getSuggestionForgroundColorStyle;

            public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException {
                return this.mMethod.invoke(null, new Object[]{para[0]});
            }
        };
    }

    public static Integer getPrimaryColor(Context context) {
        return (Integer) Wrapper.runCaller(sGetPrimaryColorCaller, context);
    }

    public static Integer getSuggestionForgroundColorStyle(int color) {
        return (Integer) Wrapper.runCaller(sGetSuggestionForgroundColorStyle, Integer.valueOf(color));
    }
}
