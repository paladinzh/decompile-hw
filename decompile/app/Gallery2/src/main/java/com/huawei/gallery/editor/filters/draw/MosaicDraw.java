package com.huawei.gallery.editor.filters.draw;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.cache.CustDrawBrushCache;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation.StrokeData;

public class MosaicDraw extends DrawStyle {
    private int mMode;

    public MosaicDraw(int mode) {
        this.mMode = mode;
    }

    public void paint(Bitmap appliedMosaicBitmap, Canvas canvas, Matrix toScrMatrix, StrokeData sd, CustDrawBrushCache brushCache) {
        if (sd != null && sd.mPath != null) {
            if (appliedMosaicBitmap == null && this.mMode == 0) {
                GalleryLog.w("MosaicDraw", "we have no applied mosaic bitmap");
                return;
            }
            Paint paint = new Paint();
            paint.setStyle(Style.STROKE);
            paint.setStrokeCap(Cap.ROUND);
            paint.setStrokeJoin(Join.ROUND);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(toScrMatrix.mapRadius(sd.mRadius) * 2.0f);
            if (this.mMode == 1) {
                paint.setColor(0);
                paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            } else {
                paint.setShader(new BitmapShader(appliedMosaicBitmap, TileMode.REPEAT, TileMode.REPEAT));
            }
            Path path = new Path();
            path.addPath(sd.mPath, toScrMatrix);
            canvas.drawPath(path, paint);
        }
    }
}
