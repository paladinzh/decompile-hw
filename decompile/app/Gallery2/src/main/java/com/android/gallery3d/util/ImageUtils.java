package com.android.gallery3d.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;

public class ImageUtils {
    private static final Paint sResizePaint = new Paint(2);

    private ImageUtils() {
    }

    public static Bitmap getCircleImage(Bitmap srcBitmap) {
        if (srcBitmap == null) {
            return null;
        }
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();
        Bitmap dstBitmap = null;
        try {
            dstBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        } catch (IllegalArgumentException e) {
            GalleryLog.i("ImageUtils", "width and height must > 0");
        }
        Canvas canvas = new Canvas(dstBitmap);
        Paint paint = new Paint();
        paint.setColor(-16777216);
        paint.setAntiAlias(true);
        paint.setStyle(Style.FILL);
        canvas.drawCircle(((float) width) / 2.0f, ((float) height) / 2.0f, ((float) Math.min(width, height)) / 2.0f, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.saveLayer(null, paint, 3);
        canvas.drawBitmap(srcBitmap, 0.0f, 0.0f, null);
        canvas.setBitmap(null);
        return dstBitmap;
    }
}
