package com.android.contacts;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Point;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import com.android.contacts.hap.utils.ScreenUtils;

public class ContactDpiAdapter {
    private static final boolean FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    private static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
    private static final boolean IS_CHINA_AREA = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    public static final boolean NOT_SRC_DPI;
    public static final int REAL_Dpi = SystemProperties.getInt("persist.sys.dpi", SRC_DPI);
    public static final int SRC_DPI = SystemProperties.getInt("ro.sf.lcd_density", 0);

    static {
        boolean z;
        if (SRC_DPI == REAL_Dpi || REAL_Dpi == 0 || SRC_DPI == 0) {
            z = false;
        } else {
            z = true;
        }
        NOT_SRC_DPI = z;
    }

    public static int getNewPxDpi(int dmienId, Context context) {
        if (context == null) {
            return 0;
        }
        if (NOT_SRC_DPI) {
            return (context.getResources().getDimensionPixelSize(dmienId) * SRC_DPI) / REAL_Dpi;
        }
        return context.getResources().getDimensionPixelSize(dmienId);
    }

    public static int getNewDpiFromDimen(int dimenValue) {
        if (NOT_SRC_DPI) {
            return (SRC_DPI * dimenValue) / REAL_Dpi;
        }
        return dimenValue;
    }

    public static int getStatusBarHeight(Context context) {
        if (context == null) {
            return 0;
        }
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static int getNavigationBarHeight(Context context) {
        if (context == null) {
            return 0;
        }
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static int getActionbarHeight(Context context) {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(16843499, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        return 0;
    }

    public static int getScreenSize(Context context, boolean toGetWidth) {
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Point realSize = new Point();
        display.getRealSize(realSize);
        int w_screen = realSize.x;
        int h_screen = realSize.y;
        if (toGetWidth) {
            return w_screen;
        }
        return h_screen;
    }

    public static int getActivityPaddingHeight(Context context) {
        if (context.getResources().getConfiguration().orientation == 1) {
            return getActionbarHeight(context) + getStatusBarHeight(context);
        }
        return getActionbarHeight(context);
    }

    public static int getActivityContentHeight(Context context) {
        return (getScreenSize(context, false) - getActionbarHeight(context)) - getStatusBarHeight(context);
    }

    public static int getActivityContentHeights(Context context) {
        if (isShowNavigationBar(context) && ScreenUtils.isLandscape(context)) {
            return getActivityContentHeight(context) - getNavigationBarHeight(context);
        }
        return getActivityContentHeight(context);
    }

    public static boolean isShowNavigationBar(Context context) {
        boolean havNavbar = isNaviBarEnabled(context.getContentResolver());
        boolean isNavOnBotoom = SystemProperties.getInt("ro.panel.hw_orientation", 0) == 90;
        if (havNavbar) {
            return isNavOnBotoom;
        }
        return false;
    }

    public static boolean isNaviBarEnabled(ContentResolver resolver) {
        boolean z = true;
        int NAVI_BAR_DEFAULT_STATUS = 1;
        if (!FRONT_FINGERPRINT_NAVIGATION) {
            return true;
        }
        if (FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 0) {
            if (IS_CHINA_AREA) {
                NAVI_BAR_DEFAULT_STATUS = 0;
            } else {
                NAVI_BAR_DEFAULT_STATUS = 1;
            }
        } else if (FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
            return false;
        }
        if (System.getIntForUser(resolver, "enable_navbar", NAVI_BAR_DEFAULT_STATUS, ActivityManager.getCurrentUser()) != 1) {
            z = false;
        }
        return z;
    }
}
