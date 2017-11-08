package com.huawei.cspcommon.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

public class BitmapUtil {
    protected BitmapUtil() {
    }

    public static Bitmap createRoundRectBitmap(Bitmap aSource, float leftTop, float rightTop, float leftBottom, float rightBottom) {
        if (aSource == null) {
            return null;
        }
        Bitmap lBitmap = Bitmap.createBitmap(aSource.getWidth(), aSource.getHeight(), Config.ARGB_8888);
        Canvas lCanvas = new Canvas(lBitmap);
        Paint lPaint = new Paint();
        Path path = new Path();
        Rect lRect = new Rect(0, 0, aSource.getWidth(), aSource.getHeight());
        setCornerRadii(path, new RectF(lRect), leftTop, rightTop, leftBottom, rightBottom);
        lPaint.setAntiAlias(true);
        lCanvas.drawARGB(0, 0, 0, 0);
        lPaint.setColor(-12434878);
        lCanvas.drawPath(path, lPaint);
        lPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        lCanvas.drawBitmap(aSource, lRect, lRect, lPaint);
        return lBitmap;
    }

    static void setCornerRadii(Path drawable, RectF rect, float r0, float r1, float r2, float r3) {
        drawable.addRoundRect(rect, new float[]{r0, r0, r1, r1, r2, r2, r3, r3}, Direction.CW);
    }
}
