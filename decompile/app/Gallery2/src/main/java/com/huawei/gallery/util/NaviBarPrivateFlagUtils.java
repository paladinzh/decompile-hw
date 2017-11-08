package com.huawei.gallery.util;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import com.android.gallery3d.util.Wrapper;
import com.android.gallery3d.util.Wrapper.ReflectCaller;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NaviBarPrivateFlagUtils extends Wrapper {
    private static ReflectCaller sAddPrivateFlagsMethod;
    private static boolean sCanSetNaviBarPrivateFlag = false;
    private static int sPrivateFlagHideNaviBar;
    private static ReflectCaller sSetHwFlagsMethod;

    private static class InitCaller implements ReflectCaller {
        private Method addPrivateFlags;
        private Method setHwFlags;

        private InitCaller() {
        }

        public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
            Class<?> window = Window.class;
            this.addPrivateFlags = window.getDeclaredMethod("addPrivateFlags", new Class[]{Integer.TYPE});
            this.setHwFlags = window.getDeclaredMethod("setHwFlags", new Class[]{Integer.TYPE, Integer.TYPE});
            NaviBarPrivateFlagUtils.sPrivateFlagHideNaviBar = LayoutParams.class.getDeclaredField("PRIVATE_FLAG_HIDE_NAVI_BAR").getInt(null);
            NaviBarPrivateFlagUtils.sCanSetNaviBarPrivateFlag = true;
            return null;
        }
    }

    static {
        final InitCaller initor = new InitCaller();
        Wrapper.runCaller(initor, new Object[0]);
        sSetHwFlagsMethod = new ReflectCaller() {
            Method method = initor.setHwFlags;

            public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
                return this.method.invoke(para[0], new Object[]{para[1], para[2]});
            }
        };
        sAddPrivateFlagsMethod = new ReflectCaller() {
            Method method = initor.addPrivateFlags;

            public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
                return this.method.invoke(para[0], new Object[]{para[1]});
            }
        };
    }

    public static void addPrivateHideNaviBarFlag(Activity activity) {
        if (sCanSetNaviBarPrivateFlag && activity != null) {
            Wrapper.runCaller(sAddPrivateFlagsMethod, activity.getWindow(), Integer.valueOf(sPrivateFlagHideNaviBar));
        }
    }

    public static void clearPrivateHideNaviBarFlag(Activity activity) {
        if (sCanSetNaviBarPrivateFlag && activity != null) {
            Wrapper.runCaller(sSetHwFlagsMethod, activity.getWindow(), Integer.valueOf(0), Integer.valueOf(sPrivateFlagHideNaviBar));
        }
    }

    public static boolean isSupportNaviBarPrivateFlagSet() {
        return sCanSetNaviBarPrivateFlag;
    }
}
