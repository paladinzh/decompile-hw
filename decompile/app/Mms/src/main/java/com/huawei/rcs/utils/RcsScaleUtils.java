package com.huawei.rcs.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import java.io.InputStream;
import java.math.BigDecimal;

public class RcsScaleUtils {
    public static Bitmap decodeResource(InputStream in, int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
        Options options = new Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateSampleSize(srcWidth, srcHeight, dstWidth, dstHeight);
        return BitmapFactory.decodeStream(in, null, options);
    }

    public static Bitmap createScaledBitmap(Bitmap unscaledBitmap, int dstWidth, int dstHeight) {
        Rect srcRect = calculateSrcRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(), dstWidth, dstHeight);
        Rect dstRect = calculateDstRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(), dstWidth, dstHeight);
        Bitmap scaledBitmap = Bitmap.createBitmap(dstRect.width(), dstRect.height(), Config.ARGB_8888);
        new Canvas(scaledBitmap).drawBitmap(unscaledBitmap, srcRect, dstRect, new Paint(2));
        return scaledBitmap;
    }

    public static int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
        if (((float) srcWidth) / ((float) srcHeight) > ((float) dstWidth) / ((float) dstHeight)) {
            return srcWidth / dstWidth;
        }
        return srcHeight / dstHeight;
    }

    public static Rect calculateSrcRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
        return new Rect(0, 0, srcWidth, srcHeight);
    }

    public static Rect calculateDstRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
        float srcAspect = ((float) srcWidth) / ((float) srcHeight);
        if (srcAspect > ((float) dstWidth) / ((float) dstHeight)) {
            return new Rect(0, 0, dstWidth, (int) (((float) dstWidth) / srcAspect));
        }
        return new Rect(0, 0, (int) (((float) dstHeight) * srcAspect), dstHeight);
    }

    public static int[] calculateResolution(int width, int height) {
        int i = 480;
        int[] des = new int[2];
        if (Math.max(width, height) <= 960) {
            des[0] = width > height ? 640 : 480;
            if (width <= height) {
                i = 640;
            }
            des[1] = i;
        } else {
            int longSide = Math.max(width, height);
            float scale = new BigDecimal(longSide <= 1280 ? 960 : 1280).divide(new BigDecimal(longSide), 4, 1).floatValue();
            des[0] = Math.round(((float) width) * scale);
            des[1] = Math.round(((float) height) * scale);
        }
        return des;
    }

    public static int calculateQuality(int sampleSize, int[] des) {
        if (Math.max(des[0], des[1]) > 960) {
            return 80;
        }
        return 85;
    }
}
