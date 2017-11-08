package com.huawei.keyguard.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.renderscript.Allocation;
import android.renderscript.Allocation.MipmapControl;
import android.renderscript.Element;
import android.renderscript.RSInvalidStateException;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

public class ImageUtils {
    private static int[] mGradientArray = new int[200];
    private static int mGradientArrayCount;
    private static RenderScript rs;
    private static ScriptIntrinsicBlur theIntrinsic;

    public static synchronized Bitmap blurImage(Context ctx, Bitmap input, Bitmap output, float radius) {
        synchronized (ImageUtils.class) {
            if (ctx == null || input == null || output == null || radius <= 0.0f || radius > 35.0f) {
                HwLog.w("ImageUtils", "blurImage() parameter is incorrect:" + ctx + "," + input + "," + output + "," + radius);
                return null;
            }
            Context c = ctx.getApplicationContext();
            if (c != null) {
                ctx = c;
            }
            if (rs == null) {
                rs = RenderScript.create(ctx);
            }
            Allocation tmpIn = Allocation.createFromBitmap(rs, input, MipmapControl.MIPMAP_NONE, 1);
            Allocation tmpOut = Allocation.createTyped(rs, tmpIn.getType());
            if (theIntrinsic == null) {
                theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            }
            theIntrinsic.setRadius(radius);
            theIntrinsic.setInput(tmpIn);
            theIntrinsic.forEach(tmpOut);
            tmpOut.copyTo(output);
            try {
                tmpIn.destroy();
            } catch (RSInvalidStateException e) {
                HwLog.w("ImageUtils", "destroy tmpIn failed");
            }
            try {
                tmpOut.destroy();
            } catch (RSInvalidStateException e2) {
                HwLog.w("ImageUtils", "destroy tmpOut failed");
            }
        }
        return output;
    }

    public static boolean canUseBitmapCache(Bitmap bitmap, Options targetOptions, Context context) {
        boolean z = true;
        if (targetOptions == null || bitmap == null) {
            return false;
        }
        if (bitmap.getConfig() != targetOptions.inPreferredConfig) {
            HwLog.w("ImageUtils", "canUseBitmapCache config not match " + bitmap.getConfig());
            return false;
        } else if (VERSION.SDK_INT >= 19) {
            int width = targetOptions.outWidth / targetOptions.inSampleSize;
            int height = targetOptions.outHeight / targetOptions.inSampleSize;
            int byteCount = (width * height) * getBytesPerPixel(targetOptions.inPreferredConfig);
            int allocCount = bitmap.getAllocationByteCount();
            if (byteCount <= allocCount) {
                use = true;
            } else {
                use = false;
            }
            if (!use) {
                HwLog.w("ImageUtils", "cannot use bitmap in newer than KITKAT byteCount = " + byteCount + ", " + width + "x" + height + ", allocCount = " + allocCount + " - " + bitmap.getWidth() + "x" + bitmap.getHeight() + ", " + targetOptions.inSampleSize);
            }
            return use;
        } else {
            if (bitmap.getWidth() == targetOptions.outWidth && bitmap.getHeight() == targetOptions.outHeight) {
                if (targetOptions.inSampleSize != 1) {
                    z = false;
                }
                use = z;
            } else {
                use = false;
            }
            if (!use) {
                HwLog.w("ImageUtils", "cannot use bitmap in lower than KITKAT : sampleSize = " + targetOptions.inSampleSize + ", inbitmapsize = " + bitmap.getWidth() + "x" + bitmap.getHeight() + " - target size  = " + targetOptions.outWidth + "x" + targetOptions.outHeight);
            }
            return use;
        }
    }

    public static int getBytesPerPixel(Config config) {
        if (config == Config.ARGB_8888) {
            return 4;
        }
        if (config == Config.RGB_565 || config == Config.ARGB_4444) {
            return 2;
        }
        return config == Config.ALPHA_8 ? 1 : 1;
    }

    public static boolean checkColorBanding(Bitmap bmp) {
        if (bmp == null) {
            HwLog.w("ImageUtils", "checkColorBanding bmp is null");
            return false;
        }
        mGradientArrayCount = 0;
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int gradientCount = 0;
        for (int by = 10; by + 150 < h; by += 150) {
            for (int bx = 10; bx + 150 < w; bx += 150) {
                int count;
                int counta;
                int i;
                boolean rCheck = checkline(bmp, w, bx, 150, 10, by);
                if (rCheck) {
                    rCheck = checkline(bmp, w, bx, 150, 10, by + 150);
                }
                if (rCheck) {
                    count = 0;
                    counta = 0;
                    for (i = by + 10; i < by + 150; i += 10) {
                        if (checkline(bmp, w, bx, 150, 10, i)) {
                            count++;
                        }
                        counta++;
                    }
                    if (((float) count) / ((float) counta) >= 0.8f) {
                        gradientCount++;
                        if (gradientCount >= 5) {
                            return true;
                        }
                    }
                }
                rCheck = checkrow(bmp, w, by, 150, 10, bx);
                if (rCheck) {
                    rCheck = checkrow(bmp, w, by, 150, 10, bx + 150);
                }
                if (rCheck) {
                    count = 0;
                    counta = 0;
                    for (i = bx + 10; i < bx + 150; i += 10) {
                        if (checkrow(bmp, w, by, 150, 10, i)) {
                            count++;
                        }
                        counta++;
                    }
                    if (((float) count) / ((float) counta) >= 0.8f) {
                        gradientCount++;
                        if (gradientCount >= 5) {
                            return true;
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
        return false;
    }

    private static boolean checkline(Bitmap bmp, int w, int bx, int bstepx, int step, int y) {
        int br = 0;
        int bg = 0;
        int bb = 0;
        int er = 0;
        int eg = 0;
        int eb = 0;
        int nr = 0;
        int ng = 0;
        int nb = 0;
        boolean gradient = true;
        int x = bx;
        while (x + step < bx + bstepx) {
            int c0 = bmp.getPixel(x + step, y);
            int c1 = bmp.getPixel(x, y);
            int p0 = Color.red(c0);
            int p1 = Color.red(c1);
            int r = p0 > p1 ? 1 : p0 < p1 ? -1 : 0;
            if ((br <= 0 || r >= 0) && (br >= 0 || r <= 0)) {
                if (r != 0 && (x == bx || x == (bx + bstepx) - (step * 2))) {
                    er += r;
                }
                br += r;
                p0 = Color.green(c0);
                p1 = Color.green(c1);
                r = p0 > p1 ? 1 : p0 < p1 ? -1 : 0;
                if ((bg <= 0 || r >= 0) && (bg >= 0 || r <= 0)) {
                    if (r != 0 && (x == bx || x == (bx + bstepx) - (step * 2))) {
                        eg += r;
                    }
                    bg += r;
                    p0 = Color.blue(c0);
                    p1 = Color.blue(c1);
                    r = p0 > p1 ? 1 : p0 < p1 ? -1 : 0;
                    if ((bb <= 0 || r >= 0) && (bb >= 0 || r <= 0)) {
                        if (r != 0 && (x == bx || x == (bx + bstepx) - (step * 2))) {
                            eb += r;
                        }
                        bb += r;
                    } else {
                        nb++;
                    }
                } else {
                    ng++;
                }
            } else {
                nr++;
            }
            x += step;
        }
        if (nr <= 1 && ng <= 1) {
            if (nb > 1) {
            }
            if (br == 0 && bg == 0 && bb == 0) {
                gradient = false;
            }
            if (br == 0 && br == er && bg != 0 && bg == eg && bb != 0 && bb == eb) {
                return false;
            }
            return gradient;
        }
        gradient = false;
        gradient = false;
        return br == 0 ? gradient : gradient;
    }

    private static boolean checkrow(Bitmap bmp, int w, int by, int bstepy, int step, int x) {
        int br = 0;
        int bg = 0;
        int bb = 0;
        int er = 0;
        int eg = 0;
        int eb = 0;
        int nr = 0;
        int ng = 0;
        int nb = 0;
        boolean gradient = true;
        int y = by;
        while (y + step < by + bstepy) {
            int c0 = bmp.getPixel(x, y + step);
            int c1 = bmp.getPixel(x, y);
            int p0 = Color.red(c0);
            int p1 = Color.red(c1);
            int r = p0 > p1 ? 1 : p0 < p1 ? -1 : 0;
            if ((br <= 0 || r >= 0) && (br >= 0 || r <= 0)) {
                if (r != 0 && (y == by || y == (by + bstepy) - (step * 2))) {
                    er += r;
                }
                br += r;
                p0 = Color.green(c0);
                p1 = Color.green(c1);
                r = p0 > p1 ? 1 : p0 < p1 ? -1 : 0;
                if ((bg <= 0 || r >= 0) && (bg >= 0 || r <= 0)) {
                    if (r != 0 && (y == by || y == (by + bstepy) - (step * 2))) {
                        eg += r;
                    }
                    bg += r;
                    p0 = Color.blue(c0);
                    p1 = Color.blue(c1);
                    r = p0 > p1 ? 1 : p0 < p1 ? -1 : 0;
                    if ((bb <= 0 || r >= 0) && (bb >= 0 || r <= 0)) {
                        if (r != 0 && (y == by || y == (by + bstepy) - (step * 2))) {
                            eb += r;
                        }
                        bb += r;
                    } else {
                        nb++;
                    }
                } else {
                    ng++;
                }
            } else {
                nr++;
            }
            y += step;
        }
        if (nr <= 1 && ng <= 1) {
            if (nb > 1) {
            }
            if (br == 0 && bg == 0 && bb == 0) {
                gradient = false;
            }
            if (br == 0 && br == er && bg != 0 && bg == eg && bb != 0 && bb == eb) {
                return false;
            }
            return gradient;
        }
        gradient = false;
        gradient = false;
        return br == 0 ? gradient : gradient;
    }

    public static boolean isBitmapTransparent(String path) {
        if (path == null) {
            return false;
        }
        return "PNG".equalsIgnoreCase(path.substring(path.lastIndexOf(".") + 1));
    }

    public static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while (true) {
                if (halfHeight / inSampleSize < reqHeight && halfWidth / inSampleSize < reqWidth) {
                    break;
                }
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
