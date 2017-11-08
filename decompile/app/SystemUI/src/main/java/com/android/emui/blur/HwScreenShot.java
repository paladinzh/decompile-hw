package com.android.emui.blur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceControl;
import android.view.WindowManager;

public class HwScreenShot {
    public static Bitmap screenShotBitmap(Context ctx, int minLayer, int maxLayer, float scale, Rect rect) {
        Bitmap bitmap;
        if (rect == null) {
            rect = new Rect();
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display display = ((WindowManager) ctx.getSystemService("window")).getDefaultDisplay();
        display.getRealMetrics(displayMetrics);
        int[] dims = new int[]{(((int) (((float) displayMetrics.widthPixels) * scale)) / 2) * 2, (((int) (((float) displayMetrics.heightPixels) * scale)) / 2) * 2};
        int rotation = display.getRotation();
        if (rotation == 0 || 2 == rotation) {
            bitmap = SurfaceControl.screenshot(rect, dims[0], dims[1], minLayer, maxLayer, false, converseRotation(rotation));
        } else {
            bitmap = rotationScreenBitmap(rect, rotation, dims, minLayer, maxLayer);
        }
        if (bitmap == null) {
            Log.e("ScreenShotHelper", "screenShotBitmap error bitmap is null");
            return null;
        }
        bitmap.prepareToDraw();
        return bitmap;
    }

    public static Bitmap rotationScreenBitmap(Rect rect, int rotation, int[] srcDims, int minLayer, int maxLayer) {
        float degrees = convertRotationToDegrees(rotation);
        float[] dims = new float[]{(float) srcDims[0], (float) srcDims[1]};
        Matrix metrics = new Matrix();
        metrics.reset();
        metrics.preRotate(-degrees);
        metrics.mapPoints(dims);
        dims[0] = Math.abs(dims[0]);
        dims[1] = Math.abs(dims[1]);
        Bitmap bitmap = SurfaceControl.screenshot(rect, (int) dims[0], (int) dims[1], minLayer, maxLayer, false, 0);
        Bitmap ss = Bitmap.createBitmap(srcDims[0], srcDims[1], Config.ARGB_8888);
        Canvas c = new Canvas(ss);
        c.translate(((float) srcDims[0]) / 2.0f, ((float) srcDims[1]) / 2.0f);
        c.rotate(degrees);
        c.translate((-dims[0]) / 2.0f, (-dims[1]) / 2.0f);
        c.drawBitmap(bitmap, 0.0f, 0.0f, null);
        bitmap.recycle();
        bitmap = ss;
        return ss;
    }

    private static float convertRotationToDegrees(int rotation) {
        switch (rotation) {
            case 1:
                return 270.0f;
            case 2:
                return 180.0f;
            case 3:
                return 90.0f;
            default:
                return 0.0f;
        }
    }

    public static int converseRotation(int rotation) {
        switch (rotation) {
            case 1:
                return 3;
            case 2:
                return 2;
            case 3:
                return 1;
            default:
                return 0;
        }
    }
}
