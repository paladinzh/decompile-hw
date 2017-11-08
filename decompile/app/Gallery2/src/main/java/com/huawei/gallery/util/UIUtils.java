package com.huawei.gallery.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.View;
import android.view.Window;
import com.android.gallery3d.R;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.ui.BitmapScreenNail;
import com.android.gallery3d.ui.ScreenNail;
import com.android.gallery3d.ui.TiledScreenNail;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.watermark.manager.parse.WMElement;
import java.lang.reflect.InvocationTargetException;

public abstract class UIUtils {
    public static void showStatusBar(View view) {
        view.setSystemUiVisibility(view.getSystemUiVisibility() & -6);
    }

    @TargetApi(16)
    public static void setNavigationBarIsOverlay(View view, boolean isOverlay) {
        if (view != null) {
            int flags = view.getSystemUiVisibility();
            if (isOverlay) {
                flags |= 512;
            } else {
                flags &= -513;
            }
            view.setSystemUiVisibility(flags);
        }
    }

    @TargetApi(13)
    public static Point getDisplaySizeWithoutStatusBar(Activity activity) {
        boolean isPort = true;
        Point size = new Point();
        int statusBarHeight = getStatusBarHeight(activity);
        Display d = activity.getWindowManager().getDefaultDisplay();
        if (!(d.getRotation() == 0 || d.getRotation() == 2)) {
            isPort = false;
        }
        size.set(isPort ? d.getWidth() : d.getHeight(), (isPort ? d.getHeight() : d.getWidth()) - statusBarHeight);
        return size;
    }

    public static int getStatusBarHeight(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.status_bar_height);
    }

    public static int getActionBarHeight(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.action_bar_height);
    }

    public static int getFootBarHeight(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.foot_bar_height);
    }

    @SuppressLint({"ServiceCast"})
    public static Bitmap getWallpaperBitmap(Context context, Rect rect) {
        WallpaperManager wm = (WallpaperManager) context.getSystemService("wallpaper");
        try {
            return (Bitmap) Class.forName("com.huawei.android.app.WallpaperManagerEx").getDeclaredMethod("getBlurBitmap", new Class[]{WallpaperManager.class, Rect.class}).invoke(null, new Object[]{wm, rect});
        } catch (Exception e) {
            WallpaperInfo info = wm.getWallpaperInfo();
            Drawable drawable = info != null ? info.loadThumbnail(context.getPackageManager()) : wm.getDrawable();
            Rect wallRect = new Rect(0, 0, drawable != null ? drawable.getIntrinsicWidth() : 0, drawable != null ? drawable.getIntrinsicHeight() : 0);
            if (!wallRect.isEmpty() && !rect.isEmpty() && wallRect.intersect(rect)) {
                return getBitmapFromDrawable(drawable, rect);
            }
            GalleryLog.d("UIUtils", "The wall paper is too small for rect " + rect);
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.divider_horizontal_blue_emui);
        }
    }

    public static Drawable getWallpaper(Context context, Rect rect, boolean needThemeAsBackground) {
        Drawable drawable;
        if (needThemeAsBackground && ImmersionUtils.isActionbarBackgroundThemed(context)) {
            Resources res = context.getResources();
            int backgroundID = res.getIdentifier("emui_theme_actionbar_background", "drawable", "androidhwext");
            if (backgroundID != 0) {
                drawable = res.getDrawable(backgroundID);
                if (drawable != null) {
                    return drawable;
                }
            }
        }
        drawable = ImmersionUtils.getColorDrawable(context);
        if (drawable != null) {
            return drawable;
        }
        return new BitmapDrawable(context.getResources(), getWallpaperBitmap(context, rect));
    }

    public static Bitmap getBitmapFromScreenNail(ScreenNail screenNail) {
        if (screenNail instanceof TiledScreenNail) {
            return ((TiledScreenNail) screenNail).getBitmap();
        }
        if (screenNail instanceof BitmapScreenNail) {
            return ((BitmapScreenNail) screenNail).getBitmap();
        }
        return null;
    }

    public static Bitmap getGifBitmapFromScreenNail(ScreenNail screenNail) {
        if (screenNail instanceof TiledScreenNail) {
            return ((TiledScreenNail) screenNail).getGifBitmap();
        }
        if (screenNail instanceof BitmapScreenNail) {
            return ((BitmapScreenNail) screenNail).getGifBitmap();
        }
        return null;
    }

    public static void addStateListAnimation(View view, Context context) {
        View itemWrapper = view.findViewById(R.id.gallery_statelist_view);
        if (itemWrapper != null) {
            itemWrapper.setBackground(null);
        }
    }

    public static Bitmap getBitmapFromDrawable(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap getBitmapFromDrawable(Drawable drawable, Rect rect) {
        Bitmap bitmap = getBitmapFromDrawable(drawable);
        float scale = Math.max(((float) rect.right) / ((float) bitmap.getWidth()), ((float) rect.bottom) / ((float) bitmap.getWidth()));
        if (scale > WMElement.CAMERASIZEVALUE1B1) {
            bitmap = BitmapUtils.resizeBitmapByScale(bitmap, scale, true);
        }
        return Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());
    }

    public static int getStatusBarColor(Window window) {
        if (!ApiHelper.HAS_MODIFY_STATUS_BAR_COLOR) {
            return 0;
        }
        try {
            return ((Integer) window.getClass().getMethod("getStatusBarColor", new Class[0]).invoke(window, new Object[0])).intValue();
        } catch (NoSuchMethodException e) {
            GalleryLog.d("UIUtils", "NoSuchMethodException");
            return 0;
        } catch (InvocationTargetException e2) {
            GalleryLog.d("UIUtils", "InvocationTargetException");
            return 0;
        } catch (IllegalAccessException e3) {
            GalleryLog.d("UIUtils", "IllegalAccessException");
            return 0;
        }
    }

    public static void setStatusBarColor(Window window, int color) {
        if (ApiHelper.HAS_MODIFY_STATUS_BAR_COLOR) {
            try {
                window.getClass().getMethod("setStatusBarColor", new Class[]{Integer.TYPE}).invoke(window, new Object[]{Integer.valueOf(color)});
            } catch (NoSuchMethodException e) {
                GalleryLog.d("UIUtils", "NoSuchMethodException");
            } catch (InvocationTargetException e2) {
                GalleryLog.d("UIUtils", "InvocationTargetException");
            } catch (IllegalAccessException e3) {
                GalleryLog.d("UIUtils", "IllegalAccessException");
            }
        }
    }

    public static int getNavigationBarColor(Window window) {
        if (!ApiHelper.HAS_MODIFY_STATUS_BAR_COLOR) {
            return 0;
        }
        try {
            return ((Integer) window.getClass().getMethod("getNavigationBarColor", new Class[0]).invoke(window, new Object[0])).intValue();
        } catch (NoSuchMethodException e) {
            GalleryLog.d("UIUtils", "NoSuchMethodException");
            return 0;
        } catch (InvocationTargetException e2) {
            GalleryLog.d("UIUtils", "InvocationTargetException");
            return 0;
        } catch (IllegalAccessException e3) {
            GalleryLog.d("UIUtils", "IllegalAccessException");
            return 0;
        }
    }

    public static void setNavigationBarColor(Window window, int color) {
        if (ApiHelper.HAS_MODIFY_STATUS_BAR_COLOR) {
            try {
                window.getClass().getMethod("setNavigationBarColor", new Class[]{Integer.TYPE}).invoke(window, new Object[]{Integer.valueOf(color)});
            } catch (NoSuchMethodException e) {
                GalleryLog.d("UIUtils", "NoSuchMethodException");
            } catch (InvocationTargetException e2) {
                GalleryLog.d("UIUtils", "InvocationTargetException");
            } catch (IllegalAccessException e3) {
                GalleryLog.d("UIUtils", "IllegalAccessException");
            }
        }
    }
}
