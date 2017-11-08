package com.android.systemui.wallpaper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.provider.Settings.System;
import android.util.SparseIntArray;
import com.android.systemui.R;
import com.android.systemui.tint.TintManager;
import com.android.systemui.utils.HwLog;

public class HwWallpaperMask {
    public static Bitmap tryUpdateWallpaperWithMask(Context context, Bitmap wallpaper) {
        if (!(context == null || wallpaper == null || wallpaper.isRecycled())) {
            SparseIntArray resultMap = getColorInfo(context, wallpaper);
            int avgColorType = getLauncherAreaColorType(resultMap);
            int useMask = getMaskUsingType(resultMap);
            HwLog.i("HwWallpaperMask", "tryUpdateWallpaperWithMask::useMask=" + useMask + ", avgColorType=" + avgColorType);
            if (3 == useMask) {
                System.putInt(context.getContentResolver(), "launcher_background_color", 3);
                TintManager.getInstance().setWallpaperAVGColor(3, true);
                return getLayerBitmap(context, wallpaper, 2);
            } else if (1 == useMask) {
                System.putInt(context.getContentResolver(), "launcher_background_color", 3);
                TintManager.getInstance().setWallpaperAVGColor(3, false);
            } else if (2 == useMask) {
                System.putInt(context.getContentResolver(), "launcher_background_color", avgColorType);
                TintManager.getInstance().setWallpaperAVGColor(3, true);
                return getLayerBitmap(context, wallpaper, 2);
            } else {
                TintManager.getInstance().setWallpaperAVGColor(avgColorType, false);
                System.putInt(context.getContentResolver(), "launcher_background_color", avgColorType);
            }
        }
        return wallpaper;
    }

    public static void tryUpdateKeyguardWallpaperWithMask(SparseIntArray resultMap) {
        int avgColorType = getLauncherAreaColorType(resultMap);
        int useMask = getMaskUsingType(resultMap);
        HwLog.i("HwWallpaperMask", "tryUpdateKeyguardWallpaperWithMask::useMask=" + useMask + ", avgColorType=" + avgColorType);
        if (3 == useMask) {
            TintManager.getInstance().setKeyguardWallpaperAVGColor(3, true);
        } else if (1 == useMask) {
            TintManager.getInstance().setKeyguardWallpaperAVGColor(3, false);
        } else if (2 == useMask) {
            TintManager.getInstance().setKeyguardWallpaperAVGColor(3, true);
        } else {
            TintManager.getInstance().setKeyguardWallpaperAVGColor(avgColorType, false);
        }
    }

    private static Bitmap getLayerBitmap(Context context, Bitmap wallpaper, int useMask) {
        HwLog.d("HwWallpaperMask", "getLayerBitmap");
        Drawable d1 = getDrawableByBitmap(context, wallpaper);
        Drawable d2 = null;
        if (2 == useMask || 3 == useMask) {
            d2 = context.getResources().getDrawable(R.drawable.dark_mask_statusbar_new);
        }
        if (d2 == null) {
            return getBitmapByDrawable(d1);
        }
        return getBitmapByDrawable(new LayerDrawable(new Drawable[]{d1, d2}));
    }

    private static Drawable getDrawableByBitmap(Context context, Bitmap bitmap) {
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    private static Bitmap getBitmapByDrawable(Drawable drawable) {
        Bitmap bitmap;
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        if (drawableWidth <= 0 || drawableHeight <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawableWidth, drawableHeight, Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private static SparseIntArray getColorInfo(Context context, Bitmap bmp) {
        SparseIntArray resultMap = new SparseIntArray();
        resultMap.put(1, 2);
        resultMap.put(3, 0);
        resultMap.put(2, 2);
        resultMap.put(4, 0);
        if (bmp == null) {
            HwLog.d("HwWallpaperMask", "getColorType bmp is null and return!");
            return resultMap;
        }
        int bitmapWidth = bmp.getWidth();
        int bitmapHeight = bmp.getHeight();
        TypedArray actionbarSizeTypedArray = context.obtainStyledAttributes(new int[]{16843499});
        int actbarHeight = (int) actionbarSizeTypedArray.getDimension(0, 0.0f);
        actionbarSizeTypedArray.recycle();
        int scaleWidth = bitmapWidth / 10;
        int scaleHeight = bitmapHeight / 10;
        int scaleActbarHeight = actbarHeight / 10;
        Bitmap scaleBitmap = Bitmap.createScaledBitmap(bmp, scaleWidth, scaleHeight, true);
        int[] bufPixel = new int[(scaleWidth * scaleHeight)];
        try {
            scaleBitmap.getPixels(bufPixel, 0, scaleWidth, 0, 0, scaleWidth, scaleHeight);
            boolean isLand = 2 == context.getResources().getConfiguration().orientation;
            int actualWidth = isLand ? scaleHeight : scaleWidth;
            int statHeightEnd = scaleActbarHeight;
            resultMap = getSuggestColorInfo(actualWidth, scaleActbarHeight, isLand ? scaleWidth : scaleHeight, bufPixel, getSuggestColorInfo(actualWidth, 0, scaleActbarHeight, bufPixel, resultMap));
            scaleBitmap.recycle();
            return resultMap;
        } catch (Exception e) {
            HwLog.e("HwWallpaperMask", "getColorInfo:: get pixels from scaleBitmap exception:" + e);
            return resultMap;
        }
    }

    private static SparseIntArray getSuggestColorInfo(int width, int heightBegin, int heightEnd, int[] bufPixel, SparseIntArray resultMap) {
        int launType;
        boolean isStatusBarArea = heightBegin == 0;
        int colorCount = 0;
        float rSum = 0.0f;
        float gSum = 0.0f;
        float bSum = 0.0f;
        for (int i = heightBegin; i < heightEnd; i++) {
            for (int j = 0; j < width; j++) {
                int color = bufPixel[(i * width) + j];
                rSum += (float) Color.red(color);
                gSum += (float) Color.green(color);
                bSum += (float) Color.blue(color);
                colorCount++;
            }
        }
        int averageGrayColor = (int) ((((0.299f * rSum) + (0.587f * gSum)) + (0.114f * bSum)) / ((float) colorCount));
        if (averageGrayColor < 157) {
            launType = 2;
        } else {
            launType = 1;
        }
        int colorMSD = getRGBColorMSD(width, heightBegin, heightEnd, rSum / ((float) colorCount), gSum / ((float) colorCount), bSum / ((float) colorCount), bufPixel);
        int maskType = 0;
        if (isStatusBarArea) {
            if (colorMSD > 1000) {
                maskType = 2;
                launType = 1;
            }
            resultMap.put(4, maskType);
            resultMap.put(2, launType);
        } else {
            if (colorMSD > 5000) {
                maskType = 1;
                launType = 1;
            }
            resultMap.put(1, launType);
            resultMap.put(3, maskType);
        }
        HwLog.i("HwWallpaperMask", "getSuggestColorInfo::isStatusBarArea=" + isStatusBarArea + ", colorMSD=" + colorMSD + ", average color=" + averageGrayColor + ", color type=" + launType + ", mask type=" + maskType);
        return resultMap;
    }

    private static int getRGBColorMSD(int width, int heightBegin, int hightEnd, float avgR, float avgG, float avgB, int[] bufPixel) {
        int colorCount = 0;
        float msdSum = 0.0f;
        for (int i = heightBegin; i < hightEnd; i++) {
            for (int j = 0; j < width; j++) {
                int color = bufPixel[(i * width) + j];
                msdSum = (float) (((double) msdSum) + ((Math.pow((double) (((float) Color.red(color)) - avgR), 2.0d) + Math.pow((double) (((float) Color.green(color)) - avgG), 2.0d)) + Math.pow((double) (((float) Color.blue(color)) - avgB), 2.0d)));
                colorCount++;
            }
        }
        return ((int) msdSum) / colorCount;
    }

    private static int getLauncherAreaColorType(SparseIntArray resultMap) {
        return (1 == resultMap.get(1) && 1 == resultMap.get(2)) ? 1 : 2;
    }

    private static int getMaskUsingType(SparseIntArray resultMap) {
        int launcherColor = resultMap.get(1);
        int statusbarColor = resultMap.get(2);
        int lanMaskType = resultMap.get(3);
        int statusbarMaskType = resultMap.get(4);
        if (launcherColor == statusbarColor && lanMaskType == 0 && statusbarMaskType == 0) {
            return 0;
        }
        if ((lanMaskType > 0 && statusbarMaskType > 0) || ((lanMaskType > 0 && 1 == statusbarColor) || (statusbarMaskType > 0 && 1 == launcherColor))) {
            return 3;
        }
        if ((statusbarMaskType <= 0 || 2 != launcherColor) && (1 != statusbarColor || 2 != launcherColor)) {
            return ((lanMaskType <= 0 || 2 != statusbarColor) && !(2 == statusbarColor && 1 == launcherColor)) ? 3 : 1;
        } else {
            return 2;
        }
    }
}
