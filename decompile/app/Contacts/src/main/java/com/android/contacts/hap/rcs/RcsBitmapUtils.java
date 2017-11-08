package com.android.contacts.hap.rcs;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import com.android.contacts.util.HwLog;
import java.io.ByteArrayOutputStream;

public class RcsBitmapUtils {
    public static Bitmap setSaturation(Bitmap bitmap, float saturation) {
        if (!bitmap.isMutable()) {
            return bitmap;
        }
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        ColorMatrix saturationMatrix = new ColorMatrix();
        saturationMatrix.reset();
        saturationMatrix.setSaturation(saturation);
        paint.setColorFilter(new ColorMatrixColorFilter(saturationMatrix));
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
        return bitmap;
    }

    public static Bitmap setRadialGradient(Bitmap bitmap, RadialGradient radialGradient) {
        if (!bitmap.isMutable()) {
            return bitmap;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
        paint.setShader(radialGradient);
        canvas.drawRect(new Rect(0, 0, width, height), paint);
        return bitmap;
    }

    public static byte[] bitmap2bytes(Bitmap bitmap, ByteArrayOutputStream tempBaos) {
        tempBaos.reset();
        bitmap.compress(CompressFormat.JPEG, 90, tempBaos);
        return tempBaos.toByteArray();
    }

    public static byte[] compressBitmap(Bitmap bitmap, int newSize) {
        int resultSizeMax = newSize << 10;
        int resultSizeMin = resultSizeMax - 10240;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] source = bitmap2bytes(bitmap, out);
        if (source.length < resultSizeMax) {
            return source;
        }
        byte[] ret;
        float scaleMax = 1.0f;
        float scaleMin = 0.0f;
        float scaleCurrent = (((float) resultSizeMax) * 2.0f) / ((float) source.length);
        int sourceWidth = bitmap.getWidth();
        int sourceHeight = bitmap.getHeight();
        int i = 0;
        while (true) {
            Matrix matrix = new Matrix();
            matrix.postScale(scaleCurrent, scaleCurrent);
            Bitmap bitmapResult = Bitmap.createBitmap(bitmap, 0, 0, sourceWidth, sourceHeight, matrix, true);
            ret = bitmap2bytes(bitmapResult, out);
            if (bitmapResult != bitmap) {
                bitmapResult.recycle();
            }
            if (ret.length < resultSizeMax && ret.length >= resultSizeMin) {
                break;
            }
            if (ret.length >= resultSizeMax) {
                scaleMax = scaleCurrent;
                scaleCurrent = (scaleCurrent + scaleMin) / 2.0f;
            } else {
                scaleMin = scaleCurrent;
                scaleCurrent = (scaleCurrent + scaleMax) / 2.0f;
            }
            i++;
        }
        if (HwLog.HWFLOW) {
            HwLog.i("RcsBitmapUtils", "compressBitmap: (" + source.length + '/' + sourceWidth + '/' + sourceHeight + "); Scale: " + scaleCurrent + "; Times: " + i);
        }
        return ret;
    }

    public static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            while (height / inSampleSize > reqHeight && width / inSampleSize > reqWidth) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }
}
