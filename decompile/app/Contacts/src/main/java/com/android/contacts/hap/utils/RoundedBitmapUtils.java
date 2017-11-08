package com.android.contacts.hap.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class RoundedBitmapUtils {
    private static String TAG = RoundedBitmapUtils.class.getSimpleName();
    private static int mCutHeight;
    private static int mCutWidth;
    private static int mViewHeight;
    private static int mViewWidth;

    public static Bitmap getRoundBitmap(Bitmap src, int radius) {
        try {
            clearRoundPixels(src, src.getWidth(), src.getHeight(), getMaskAlpha(radius), radius);
            return src;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static void clearRoundPixels(Bitmap src, int w, int h, int[] maskPixels, int radius) {
        for (int i = 0; i < radius; i++) {
            int srcBottomPos = (h - i) - 1;
            int maskPos = i * radius;
            for (int j = 0; j < radius; j++) {
                int mask = maskPixels[maskPos + j];
                if (mask != 0) {
                    if (mask >= 255) {
                        break;
                    }
                    int alphaMask = (mask << 24) + 16777215;
                    src.setPixel(j, i, src.getPixel(j, i) & alphaMask);
                    src.setPixel((w - j) - 1, i, src.getPixel((w - j) - 1, i) & alphaMask);
                    src.setPixel(j, srcBottomPos, src.getPixel(j, srcBottomPos) & alphaMask);
                    src.setPixel((w - j) - 1, srcBottomPos, src.getPixel((w - j) - 1, srcBottomPos) & alphaMask);
                } else {
                    src.setPixel(j, i, 0);
                    src.setPixel((w - j) - 1, i, 0);
                    src.setPixel(j, srcBottomPos, 0);
                    src.setPixel((w - j) - 1, srcBottomPos, 0);
                }
            }
        }
    }

    private static int[] getMaskAlpha(int radius) {
        switch (radius) {
            case 4:
                return createMask(4);
            case 5:
            case 6:
                return createMask(6);
            case 7:
            case 8:
                return createMask(8);
            case 9:
            case 10:
                return createMask(10);
            case 11:
            case 12:
                return createMask(12);
            case 13:
            case 14:
                return createMask(14);
            case 15:
            case 16:
                return createMask(16);
            default:
                if (radius < 4) {
                    return createMask(4);
                }
                return createMask(16);
        }
    }

    private static int[] createMask(int radius) {
        switch (radius) {
            case 4:
                return new int[]{0, 64, 153, 230, 64, 204, 255, 255, 153, 255, 255, 255, 255, 255, 255, 255};
            case 6:
                return new int[]{0, 0, 0, 64, 153, 230, 0, 0, 102, 179, 255, 255, 0, 102, 255, 255, 255, 255, 64, 179, 255, 255, 255, 255, 153, 255, 255, 255, 255, 255, 230, 255, 255, 255, 255, 255};
            case 8:
                return new int[]{0, 0, 0, 0, 72, 143, 204, 247, 0, 0, 31, 143, 250, 255, 255, 255, 0, 31, 166, 255, 255, 255, 255, 255, 0, 143, 255, 255, 255, 255, 255, 255, 72, 250, 255, 255, 255, 255, 255, 255, 143, 255, 255, 255, 255, 255, 255, 255, 204, 255, 255, 255, 255, 255, 255, 255, 247, 255, 255, 255, 255, 255, 255, 255};
            case 10:
                return new int[]{0, 0, 0, 0, 0, 26, 102, 161, 209, 255, 0, 0, 0, 0, 102, 179, 255, 255, 255, 255, 0, 0, 20, 140, 255, 255, 255, 255, 255, 255, 0, 0, 140, 255, 255, 255, 255, 255, 255, 255, 0, 102, 255, 255, 255, 255, 255, 255, 255, 255, 26, 179, 255, 255, 255, 255, 255, 255, 255, 255, 102, 255, 255, 255, 255, 255, 255, 255, 255, 255, 161, 255, 255, 255, 255, 255, 255, 255, 255, 255, 209, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255};
            case 12:
                return new int[]{0, 0, 0, 0, 0, 0, 0, 56, 119, 174, 214, 255, 0, 0, 0, 0, 0, 59, 153, 233, 255, 255, 255, 255, 0, 0, 0, 0, 107, 214, 255, 255, 255, 255, 255, 255, 0, 0, 0, 120, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0, 107, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 59, 214, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 153, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 56, 233, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 119, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 174, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 214, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255};
            case 14:
                return new int[]{0, 0, 0, 0, 0, 0, 0, 0, 18, 77, 135, 184, 222, 249, 0, 0, 0, 0, 0, 0, 21, 110, 191, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 71, 174, 255, 255, 255, 255, 255, 255, 255, 0, 0, 0, 0, 92, 217, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0, 0, 92, 227, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0, 71, 217, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 21, 174, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 110, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 18, 191, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 77, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 135, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 184, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 222, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 249, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255};
            case 16:
                return new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 38, 102, 158, 204, 237, 255, 0, 0, 0, 0, 0, 0, 0, 0, 102, 158, 237, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 0, 31, 143, 245, 255, 255, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 102, 191, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0, 0, 0, 77, 215, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0, 0, 102, 215, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0, 31, 191, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0, 143, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 102, 245, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 158, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 38, 237, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 102, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 158, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 204, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 137, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255};
            default:
                return null;
        }
    }

    public static void initailize(Resources aResources) {
        mViewWidth = aResources.getDimensionPixelSize(R.dimen.widget_cell_width);
        mViewHeight = aResources.getDimensionPixelSize(R.dimen.widget_cell_height_4X3);
        mCutWidth = 0;
        mCutHeight = 0;
    }

    public static Bitmap getCompressBitmap(Context aContext, byte[] aPhotoBytes) {
        Bitmap bitmap = null;
        try {
            Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(aPhotoBytes, 0, aPhotoBytes.length, options);
            HwLog.i(TAG, "options.outWidth : " + options.outWidth + " options.outHeight : " + options.outHeight);
            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return null;
            }
            options.inJustDecodeBounds = false;
            if (((float) options.outWidth) / ((float) options.outHeight) < ((float) mViewWidth) / ((float) mViewHeight)) {
                options.inSampleSize = options.outWidth / mViewWidth;
            } else {
                options.inSampleSize = options.outHeight / mViewHeight;
            }
            if (options.inSampleSize > 1) {
                options.inPreferredConfig = Config.RGB_565;
            }
            bitmap = BitmapFactory.decodeByteArray(aPhotoBytes, 0, aPhotoBytes.length, options);
            return bitmap;
        } catch (Throwable thow) {
            HwLog.e(TAG, "compress getCompressBitmap dataBase err!!!");
            thow.printStackTrace();
        }
    }

    public static Bitmap getCutBitmap(Bitmap compressBitmap) {
        if (compressBitmap == null) {
            return null;
        }
        int w = compressBitmap.getWidth();
        int h = compressBitmap.getHeight();
        int viewWidth = mViewWidth - mCutWidth;
        int viewHeight = mViewHeight - mCutHeight;
        float scale = Math.max(((float) viewWidth) / ((float) w), ((float) viewHeight) / ((float) h));
        HwLog.i(TAG, "scale : " + scale + " compress bitmap width : " + w + " compress bitmap height : " + h);
        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.translate(((float) viewWidth) / 2.0f, ((float) viewHeight) / 2.0f);
            canvas.rotate(0.0f);
            canvas.scale(scale, scale);
            canvas.drawBitmap(compressBitmap, ((float) (-w)) / 2.0f, ((float) (-h)) / 2.0f, new Paint(6));
        } catch (Throwable thow) {
            HwLog.w(TAG, "compress getCutBitmap cutBitmap err!!!");
            thow.printStackTrace();
        }
        return bitmap;
    }
}
