package com.android.deskclock.widgetlayout;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import com.android.util.Log;

public class ImageToneUtils {
    private static int[] mGradientColors = new int[]{Color.argb(25, 0, 0, 0), Color.argb(178, 0, 0, 0)};

    public static float calculateSaturation(int saturation) {
        Log.dRelease("jetta", "calculateSaturation : saturation = " + saturation);
        return (((float) saturation) * 1.0f) / 127.0f;
    }

    public static Bitmap handleImage(Bitmap bm, float saturationValue) {
        if (!bm.isMutable()) {
            return bm;
        }
        Canvas canvas = new Canvas(bm);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        ColorMatrix saturationMatrix = new ColorMatrix();
        saturationMatrix.reset();
        saturationMatrix.setSaturation(saturationValue);
        paint.setColorFilter(new ColorMatrixColorFilter(saturationMatrix));
        canvas.drawBitmap(bm, 0.0f, 0.0f, paint);
        return bm;
    }

    public static Bitmap coverImage(Bitmap bm, boolean isLand) {
        if (!bm.isMutable()) {
            return bm;
        }
        RadialGradient radialGradientClamp;
        int width = bm.getWidth();
        int height = bm.getHeight();
        Canvas canvas = new Canvas(bm);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        if (isLand) {
            radialGradientClamp = new RadialGradient(((float) width) / 4.0f, ((float) height) / 2.0f, ((float) (width * 2)) / 3.0f, mGradientColors, null, TileMode.CLAMP);
        } else {
            radialGradientClamp = new RadialGradient(((float) width) / 2.0f, ((float) (height * 2)) / 5.0f, ((float) (height * 2)) / 3.0f, mGradientColors, null, TileMode.CLAMP);
        }
        canvas.drawBitmap(bm, 0.0f, 0.0f, paint);
        paint.setShader(radialGradientClamp);
        canvas.drawRect(new Rect(0, 0, width, height), paint);
        return bm;
    }
}
