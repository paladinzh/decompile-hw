package com.huawei.keyguard.util;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import com.android.keyguard.R$bool;
import com.huawei.keyguard.support.WaterMarkUtils;
import com.huawei.keyguard.theme.HwThemeParser;
import com.huawei.keyguard.theme.ThemeCfg;
import java.io.File;

public class WallpaperUtils {
    public static Point getRealScreenPoint(Context context) {
        return getRealScreenPoint(context, HwUnlockUtils.getPoint(context));
    }

    public static Point getRealScreenPoint(Context context, Point outSize) {
        if (outSize == null) {
            return new Point();
        }
        if (context == null) {
            return outSize;
        }
        if (context.getResources().getBoolean(R$bool.config_isPortraitLockScreen)) {
            outSize.set(Math.min(outSize.x, outSize.y), Math.max(outSize.x, outSize.y));
        } else {
            outSize.set(Math.max(outSize.x, outSize.y), Math.min(outSize.x, outSize.y));
        }
        return outSize;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            HwLog.i("KGWallpaperUtils", "drawable is null");
            return null;
        } else if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            Bitmap bitmap = null;
            int width = drawable.getIntrinsicWidth();
            if (width <= 0) {
                width = 1;
            }
            int height = drawable.getIntrinsicHeight();
            if (height <= 0) {
                height = 1;
            }
            try {
                bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            } catch (IllegalArgumentException e) {
                HwLog.w("KGWallpaperUtils", "createBitmap fail");
            }
            drawable.draw(new Canvas(bitmap));
            return bitmap;
        }
    }

    private static boolean isSupportLandAndInLandmode(Context context) {
        boolean z = false;
        if (!HwUnlockUtils.isSupportOrientation() || !HwThemeParser.getInstance().isSupportOrientationByTheme()) {
            return false;
        }
        Point screenSize = HwUnlockUtils.getPoint(context);
        if (screenSize.x > screenSize.y) {
            z = true;
        }
        return z;
    }

    public static Drawable getThemeWallpaper(Context context) {
        if (context == null) {
            HwLog.w("KGWallpaperUtils", "getWallpaper fail because context is null");
            return null;
        }
        String wallpaper = ThemeCfg.getWallpaper();
        if (wallpaper.length() <= 0) {
            HwLog.e("KGWallpaperUtils", "getWallpaper fail.");
            return null;
        }
        String director = "/data/skin/wallpaper/";
        String defaltland = "unlock_wallpaper_0_land.jpg";
        String fileName = director + wallpaper;
        if (isSupportLandAndInLandmode(context)) {
            fileName = director + defaltland;
            if (!isFileExist(fileName)) {
                fileName = director + wallpaper;
            }
        }
        return BitmapUtils.createDrawableFromPath(context, fileName, false);
    }

    private static boolean isFileExist(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }
        try {
            File file = new File(fileName);
            if (file.exists() && !file.isDirectory()) {
                return true;
            }
        } catch (RuntimeException e) {
            HwLog.e("KGWallpaperUtils", "fileName is not exit");
        }
        return false;
    }

    private static Drawable getDesktopWallpaper(Context context) {
        WallpaperManager wpm = WallpaperManager.getInstance(context);
        if (wpm != null && wpm.getWallpaperInfo() == null) {
            return wpm.getDrawable();
        }
        HwLog.e("KGWallpaperUtils", "get DesktopWallpaper skiped as null or dynamic.");
        return null;
    }

    public static Bitmap getKeyguardWallpaper(Context context) {
        Bitmap retBmp = getKeyguardWallpaper(context, false);
        if (retBmp != null) {
            return retBmp;
        }
        return getKeyguardWallpaper(context, true);
    }

    public static Bitmap getKeyguardWallpaper(Context context, boolean desktop) {
        String msg = desktop ? "get desktop Wallpaper" : "get theme Wallpaper";
        Drawable drawable = desktop ? getDesktopWallpaper(context) : getThemeWallpaper(context);
        if (desktop) {
            drawable = getDesktopWallpaper(context);
        }
        if (drawable == null) {
            drawable = desktop ? getThemeWallpaper(context) : getDesktopWallpaper(context);
            msg = msg + " with another type";
        }
        HwLog.w("KGWallpaperUtils", msg + (drawable == null ? " Fail" : " SUCC"));
        if (drawable == null) {
            return null;
        }
        return WaterMarkUtils.addWaterMark(clipWallpaperDrawable(drawable, getRealScreenPoint(context, HwUnlockUtils.getPoint(context))));
    }

    public static Bitmap clipWallpaperDrawable(Drawable drawable, Point size) {
        Bitmap retBitmap = null;
        if (size == null || drawable == null) {
            HwLog.i("KGWallpaperUtils", "The specified wallpaper or size is null");
            return null;
        }
        Rect bounds = drawable.getBounds();
        if (bounds.width() == 0) {
            computeCustomBackgroundBounds(drawable, size);
        }
        int vWidth = size.x;
        int vHeight = size.y;
        HwLog.i("KGWallpaperUtils", "view size=" + vWidth + "x" + vHeight);
        if (vWidth > 0 && vHeight > 0) {
            retBitmap = Bitmap.createBitmap(vWidth, vHeight, Config.ARGB_8888);
            Canvas canvas = new Canvas(retBitmap);
            canvas.translate(((float) (-(bounds.width() - vWidth))) / 2.0f, ((float) (-(bounds.height() - vHeight))) / 2.0f);
            drawable.draw(canvas);
        }
        return retBitmap;
    }

    private static void computeCustomBackgroundBounds(Drawable drawable, Point size) {
        if (size == null) {
            HwLog.i("KGWallpaperUtils", "The specified wallpaper size is null");
            return;
        }
        int bgWidth = drawable.getIntrinsicWidth();
        int bgHeight = drawable.getIntrinsicHeight();
        int vWidth = size.x;
        int vHeight = size.y;
        float bgAspect = ((float) bgWidth) / ((float) bgHeight);
        if (bgAspect > ((float) vWidth) / ((float) vHeight)) {
            drawable.setBounds(0, 0, (int) (((float) vHeight) * bgAspect), vHeight);
        } else {
            drawable.setBounds(0, 0, vWidth, (int) (((float) vWidth) / bgAspect));
        }
    }
}
