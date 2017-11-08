package com.huawei.keyguard.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseIntArray;
import com.huawei.keyguard.support.WaterMarkUtils;

public final class BitmapUtils {
    private BitmapUtils() {
    }

    public static Bitmap blurBitmap(Context context, Bitmap bitmap, Bitmap blurBitmap, float degree) {
        if (degree <= 1.0f) {
            return bitmap;
        }
        if (context == null || bitmap == null) {
            HwLog.w("BitmapUtils", "keyguard blur, get blur: context or bitmap is null");
            return null;
        }
        long start = SystemClock.uptimeMillis();
        int dstWidth = bitmap.getWidth();
        int dstHeight = bitmap.getHeight();
        Bitmap rgb888 = bitmap.getConfig() == Config.ARGB_8888 ? bitmap : RGB565toARGB888(bitmap);
        StringBuffer descrption = new StringBuffer("blurBitmap : ").append(bitmap);
        descrption.append(rgb888 == bitmap ? "recreate source" : "use source bitmap");
        descrption.append("; ");
        if (blurBitmap == null || blurBitmap.getConfig() != Config.ARGB_8888) {
            if (rgb888 != bitmap) {
                blurBitmap = rgb888;
                descrption.append("use rgb888 as target");
            } else {
                blurBitmap = createEmptyBitmap(blurBitmap, dstWidth, dstHeight, Config.ARGB_8888);
                descrption.append("create target");
            }
        }
        blurBitmap = ImageUtils.blurImage(context, rgb888, blurBitmap, degree);
        descrption.append("; Consueme: ").append(SystemClock.uptimeMillis() - start).append("ms");
        HwLog.w("BitmapUtils", descrption.toString());
        return blurBitmap;
    }

    private static Bitmap createEmptyBitmap(Bitmap blurBitmap, int dstWidth, int dstHeight, Config config) {
        if (blurBitmap == null) {
            return Bitmap.createBitmap(dstWidth, dstHeight, config);
        }
        int blurW = blurBitmap.getWidth();
        int blurH = blurBitmap.getHeight();
        if (dstWidth == blurW && dstHeight == blurH) {
            return blurBitmap;
        }
        blurBitmap.reconfigure(dstWidth, dstHeight, config);
        return blurBitmap;
    }

    private static Bitmap RGB565toARGB888(Bitmap img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int[] pixels = new int[(w * h)];
        img.getPixels(pixels, 0, w, 0, 0, w, h);
        HwLog.i("BitmapUtils", "RGB565toARGB888 create bmp = " + w + "x" + h);
        Bitmap bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        bmp.setPixels(pixels, 0, w, 0, 0, w, h);
        return bmp;
    }

    public static Bitmap decodeFile(Context context, String path, Bitmap inbmp) {
        return WaterMarkUtils.addWaterMark(decodeFileNoWaterMark(context, path, inbmp));
    }

    public static Bitmap decodeFile(String path) {
        return WaterMarkUtils.addWaterMark(decodeFileNoWaterMark(path, 1080, 1920));
    }

    public static Bitmap decodeResource(Context context, int resId) {
        return decodeResource(context, resId, true);
    }

    public static Bitmap decodeResource(Context context, int resId, boolean withWMark) {
        Options opt = new Options();
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId, opt);
        return withWMark ? WaterMarkUtils.addWaterMark(bitmap) : bitmap;
    }

    public static Drawable createDrawableFromPath(Context context, String pathName, boolean waterMark) {
        if (pathName == null) {
            return null;
        }
        Bitmap bm = decodeFileNoWaterMark(pathName, 0, 0);
        if (waterMark) {
            WaterMarkUtils.addWaterMark(bm);
        }
        if (bm != null) {
            return new BitmapDrawable(context.getResources(), bm);
        }
        return null;
    }

    private static Bitmap decodeFileNoWaterMark(String path, int st_width, int st_height) {
        if (path == null) {
            HwLog.w("BitmapUtils", "decodeFile failed, path is null");
            return null;
        } else if (st_width <= 0 || st_height <= 0) {
            return BitmapFactory.decodeFile(path);
        } else {
            Options opts = new Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, opts);
            int width = opts.outWidth;
            int height = opts.outHeight;
            if (width == 0 || height == 0) {
                HwLog.d("BitmapUtils", "width = " + width + ",height = " + height);
                return null;
            }
            if (width > st_width || height > st_height) {
                opts.inSampleSize = Math.max(width / 1080, height / 1920);
            }
            opts.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(path, opts);
        }
    }

    private static Bitmap decodeFileNoWaterMark(Context context, String path, Bitmap inbmp) {
        Options opts = new Options();
        opts.inMutable = true;
        opts.inJustDecodeBounds = true;
        if (!ImageUtils.isBitmapTransparent(path)) {
            opts.inPreferredConfig = Config.RGB_565;
        }
        BitmapFactory.decodeFile(path, opts);
        opts.inSampleSize = ImageUtils.calculateInSampleSize(opts, 1080, 1920);
        opts.inJustDecodeBounds = false;
        if (ImageUtils.canUseBitmapCache(inbmp, opts, context)) {
            opts.inBitmap = inbmp;
        }
        Bitmap bmp = BitmapFactory.decodeFile(path, opts);
        if (bmp == null) {
            HwLog.w("BitmapUtils", "BitmapFactory.decodeFile fail " + opts.inBitmap);
            if (opts.inBitmap == null) {
                return null;
            }
            opts.inBitmap = null;
            bmp = BitmapFactory.decodeFile(path, opts);
            if (bmp == null) {
                return null;
            }
        }
        if (bmp.getConfig() == Config.RGB_565) {
            boolean bBanding = ImageUtils.checkColorBanding(bmp);
            if (bBanding) {
                HwLog.d("BitmapUtils", "--------> banding = " + bBanding + ", " + path + ", redecode using dither");
                opts.inDither = true;
                bmp = BitmapFactory.decodeFile(path, opts);
            }
        }
        return bmp;
    }

    public static boolean isSameDrawable(Drawable dr1, Drawable dr2) {
        boolean z = true;
        if (dr1 == dr2) {
            return true;
        }
        if (!(dr1 instanceof BitmapDrawable) || !(dr2 instanceof BitmapDrawable)) {
            return false;
        }
        if (((BitmapDrawable) dr1).getBitmap() != ((BitmapDrawable) dr2).getBitmap()) {
            z = false;
        }
        return z;
    }

    public static int getStatusBarHeight(Context context) {
        TypedArray actionbarSizeTypedArray = context.obtainStyledAttributes(new int[]{16843499});
        int sbhHeight = (int) actionbarSizeTypedArray.getDimension(0, 0.0f);
        actionbarSizeTypedArray.recycle();
        return sbhHeight;
    }

    public static SparseIntArray getColorInfo(Context context, Bitmap bmp) {
        return getColorInfo(context, bmp, getStatusBarHeight(context));
    }

    public static SparseIntArray getColorInfo(Context context, Bitmap bmp, int actbarHeight) {
        SparseIntArray resultMap = new SparseIntArray();
        resultMap.put(1, 2);
        resultMap.put(3, 0);
        resultMap.put(2, 2);
        resultMap.put(4, 0);
        resultMap.put(5, 2);
        if (bmp == null) {
            HwLog.d("BitmapUtils", "getColorType bmp is null and return!");
            return resultMap;
        }
        int scaleWidth = bmp.getWidth() / 10;
        int scaleHeight = bmp.getHeight() / 10;
        int scaleActbarHeight = actbarHeight / 10;
        int[] bufPixel = new int[(scaleWidth * scaleHeight)];
        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createScaledBitmap(bmp, scaleWidth, scaleHeight, true);
            bitmap.getPixels(bufPixel, 0, scaleWidth, 0, 0, scaleWidth, scaleHeight);
            boolean isLand = 2 == context.getResources().getConfiguration().orientation;
            int actualWidth = isLand ? scaleHeight : scaleWidth;
            int actualHeight = isLand ? scaleWidth : scaleHeight;
            if (scaleWidth * scaleHeight < actualWidth * scaleActbarHeight) {
                return resultMap;
            }
            int statHeightEnd = scaleActbarHeight;
            resultMap = getSuggestColorInfo(actualWidth, scaleActbarHeight, actualHeight, bufPixel, resultMap);
            bitmap.recycle();
            return resultMap;
        } catch (Exception e) {
            if (bitmap != null) {
                bitmap.recycle();
            }
            HwLog.e("BitmapUtils", "getColorInfo:: get pixels from scaleBitmap exception:" + e);
            return resultMap;
        }
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
        if (colorCount != 0) {
            return ((int) msdSum) / colorCount;
        }
        HwLog.w("BitmapUtils", "getRGBColorMSD return 0!");
        return 0;
    }

    public static SparseIntArray getSuggestColorInfo(int width, int heightBegin, int heightEnd, int[] bufPixel, SparseIntArray resultMap) {
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
        float graySum = ((0.299f * rSum) + (0.587f * gSum)) + (0.114f * bSum);
        int averageGrayColor = (int) (graySum / ((float) colorCount));
        int launType = averageGrayColor < 157 ? 2 : 1;
        int touchType = averageGrayColor < 204 ? 2 : 1;
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
        resultMap.put(5, touchType);
        Log.i("BitmapUtils", "getSuggestColorInfo::isStatusBarArea=" + isStatusBarArea + ", colorMSD=" + colorMSD + ", average color=" + averageGrayColor + ", color type=" + launType + ", mask type=" + maskType + "; light " + graySum, new Exception());
        return resultMap;
    }
}
