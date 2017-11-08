package com.huawei.gallery.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;

@TargetApi(17)
public class LayoutHelper {
    private static int sActionbarHeight;
    private static Context sContext;
    private static int sLongSideLength;
    private static NavigationBarHandler sNaviBarHandler;
    private static float sPixelDensity = GroundOverlayOptions.NO_DIMENSION;
    private static int sScreenHeight;
    private static int sScreenWidth;
    private static int sShortSideLength;
    private static int sStatusBarHeight;

    public static void init(Context context) {
        sContext = context;
        sStatusBarHeight = context.getResources().getDimensionPixelSize(R.dimen.status_bar_height);
        sActionbarHeight = context.getResources().getDimensionPixelSize(R.dimen.action_bar_height);
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRealMetrics(metrics);
        sPixelDensity = metrics.density;
        if (isPort()) {
            sScreenWidth = metrics.widthPixels;
            sScreenHeight = metrics.heightPixels;
        } else {
            sScreenWidth = metrics.heightPixels;
            sScreenHeight = metrics.widthPixels;
        }
        if (sScreenHeight > sScreenWidth) {
            sLongSideLength = sScreenHeight;
            sShortSideLength = sScreenWidth;
        } else {
            sLongSideLength = sScreenWidth;
            sShortSideLength = sScreenHeight;
        }
        sNaviBarHandler = new NavigationBarHandler(context);
    }

    public static NavigationBarHandler getNavigationBarHandler() {
        return sNaviBarHandler;
    }

    public static int getNavigationBarHeight() {
        return sNaviBarHandler.getHeight();
    }

    public static int getStatusBarHeight() {
        return sStatusBarHeight;
    }

    public static int getActionBarHeight() {
        return sActionbarHeight;
    }

    public static int getScreenWidth() {
        return sScreenWidth;
    }

    public static boolean isPort() {
        return sContext.getResources().getConfiguration().orientation == 1;
    }

    public static boolean isPort(GalleryContext context) {
        boolean z = true;
        if (context == null) {
            return true;
        }
        if (context.getResources().getConfiguration().orientation != 1) {
            z = false;
        }
        return z;
    }

    public static boolean isPortFeel() {
        WindowManager wm = (WindowManager) sContext.getSystemService("window");
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
        return metrics.heightPixels > metrics.widthPixels;
    }

    public static int getScreenLongSide() {
        return sLongSideLength;
    }

    public static int getScreenShortSide() {
        return sShortSideLength;
    }

    public static int getNavigationBarHeightForDefaultLand() {
        return sContext.getResources().getDimensionPixelSize(R.dimen.navigation_bar_height);
    }

    public static boolean isDefaultLandOrientationProduct() {
        int defaultOrientation = SystemProperties.getInt("ro.panel.hw_orientation", 0);
        if (defaultOrientation == 90 || defaultOrientation == 270) {
            return true;
        }
        return false;
    }
}
