package com.android.gallery3d.gadget;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import com.android.gallery3d.util.GalleryLog;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class RoundBitmapUtils {
    public static final int MASK_RADIUS_10 = 10;
    public static final int MASK_RADIUS_12 = 12;
    public static final int MASK_RADIUS_14 = 14;
    public static final int MASK_RADIUS_16 = 16;
    public static final int MASK_RADIUS_4 = 4;
    public static final int MASK_RADIUS_6 = 6;
    public static final int MASK_RADIUS_8 = 8;
    public static final String TAG = "RoundBitmapUtils";

    public static Bitmap getRoundBitmap(Bitmap src, int radius) {
        if (radius > 16) {
            return toRoundCorner(src, radius);
        }
        try {
            clearRoundPixels(src, src.getWidth(), src.getHeight(), getMaskAlpha(radius), radius);
            return src;
        } catch (Throwable ex) {
            GalleryLog.i(TAG, "Catch throw exception in getRoundBitmap() method." + ex.getMessage());
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
                return new int[]{0, 64, 153, 230, 64, SmsCheckResult.ESCT_204, 255, 255, 153, 255, 255, 255, 255, 255, 255, 255};
            case 6:
                return new int[]{0, 0, 0, 64, 153, 230, 0, 0, 102, 179, 255, 255, 0, 102, 255, 255, 255, 255, 64, 179, 255, 255, 255, 255, 153, 255, 255, 255, 255, 255, 230, 255, 255, 255, 255, 255};
            case 8:
                return new int[]{0, 0, 0, 0, 72, SmsCheckResult.ESCT_143, SmsCheckResult.ESCT_204, 247, 0, 0, 31, SmsCheckResult.ESCT_143, 250, 255, 255, 255, 0, 31, SmsCheckResult.ESCT_ADS, 255, 255, 255, 255, 255, 0, SmsCheckResult.ESCT_143, 255, 255, 255, 255, 255, 255, 72, 250, 255, 255, 255, 255, 255, 255, SmsCheckResult.ESCT_143, 255, 255, 255, 255, 255, 255, 255, SmsCheckResult.ESCT_204, 255, 255, 255, 255, 255, 255, 255, 247, 255, 255, 255, 255, 255, 255, 255};
            case 10:
                return new int[]{0, 0, 0, 0, 0, 26, 102, SmsCheckResult.ESCT_161, SmsCheckResult.ESCT_209, 255, 0, 0, 0, 0, 102, 179, 255, 255, 255, 255, 0, 0, 20, 140, 255, 255, 255, 255, 255, 255, 0, 0, 140, 255, 255, 255, 255, 255, 255, 255, 0, 102, 255, 255, 255, 255, 255, 255, 255, 255, 26, 179, 255, 255, 255, 255, 255, 255, 255, 255, 102, 255, 255, 255, 255, 255, 255, 255, 255, 255, SmsCheckResult.ESCT_161, 255, 255, 255, 255, 255, 255, 255, 255, 255, SmsCheckResult.ESCT_209, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255};
            case 12:
                return new int[]{0, 0, 0, 0, 0, 0, 0, 56, 119, SmsCheckResult.ESCT_174, SmsCheckResult.ESCT_214, 255, 0, 0, 0, 0, 0, 59, 153, 233, 255, 255, 255, 255, 0, 0, 0, 0, 107, SmsCheckResult.ESCT_214, 255, 255, 255, 255, 255, 255, 0, 0, 0, 120, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0, 107, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 59, SmsCheckResult.ESCT_214, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 153, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 56, 233, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 119, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, SmsCheckResult.ESCT_174, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, SmsCheckResult.ESCT_214, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255};
            case 14:
                return new int[]{0, 0, 0, 0, 0, 0, 0, 0, 18, 77, 135, SmsCheckResult.ESCT_184, SmsCheckResult.ESCT_222, SmsCheckResult.ESCT_249, 0, 0, 0, 0, 0, 0, 21, 110, SmsCheckResult.ESCT_191, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 71, SmsCheckResult.ESCT_174, 255, 255, 255, 255, 255, 255, 255, 0, 0, 0, 0, 92, SmsCheckResult.ESCT_217, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0, 0, 92, 227, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0, 71, SmsCheckResult.ESCT_217, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 21, SmsCheckResult.ESCT_174, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 110, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 18, SmsCheckResult.ESCT_191, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 77, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 135, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, SmsCheckResult.ESCT_184, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, SmsCheckResult.ESCT_222, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, SmsCheckResult.ESCT_249, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255};
            case 16:
                return new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 38, 102, SmsCheckResult.ESCT_158, SmsCheckResult.ESCT_204, 237, 255, 0, 0, 0, 0, 0, 0, 0, 0, 102, SmsCheckResult.ESCT_158, 237, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 0, 31, SmsCheckResult.ESCT_143, 245, 255, 255, 255, 255, 255, 255, 255, 0, 0, 0, 0, 0, 102, SmsCheckResult.ESCT_191, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0, 0, 0, 77, 215, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0, 0, 102, 215, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0, 31, SmsCheckResult.ESCT_191, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 0, SmsCheckResult.ESCT_143, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, 102, 245, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0, SmsCheckResult.ESCT_158, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 38, 237, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 102, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, SmsCheckResult.ESCT_158, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, SmsCheckResult.ESCT_204, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 137, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255};
            default:
                return null;
        }
    }

    public static Bitmap toRoundCorner(Bitmap bitmap, int radius) {
        if (bitmap == null) {
            return null;
        }
        Bitmap output = null;
        try {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            Paint paint = new Paint();
            Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            RectF rectF = new RectF(rect);
            float roundPx = (float) radius;
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(-12434878);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            if (!(bitmap.equals(output) || bitmap.isRecycled())) {
                bitmap.recycle();
            }
        } catch (OutOfMemoryError e) {
            GalleryLog.e(TAG, "Error: out of memory.");
        }
        return output;
    }
}
