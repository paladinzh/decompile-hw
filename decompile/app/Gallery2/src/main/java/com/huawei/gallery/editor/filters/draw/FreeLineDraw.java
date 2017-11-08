package com.huawei.gallery.editor.filters.draw;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import com.huawei.gallery.editor.cache.CustDrawBrushCache;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation.StrokeData;

public class FreeLineDraw extends DrawStyle {
    private Paint mPaint = new Paint();

    public FreeLineDraw() {
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setStrokeCap(Cap.ROUND);
        this.mPaint.setStrokeJoin(Join.ROUND);
        this.mPaint.setAntiAlias(true);
    }

    public void paint(Bitmap appliedMosaicBitmap, Canvas canvas, Matrix toScrMatrix, StrokeData sd, CustDrawBrushCache brushCache) {
        if (sd != null && sd.mPath != null) {
            this.mPaint.setColor(sd.mColor);
            this.mPaint.setStrokeWidth(toScrMatrix.mapRadius(sd.mRadius));
            Path cacheTransPath = new Path();
            cacheTransPath.addPath(sd.mPath, toScrMatrix);
            canvas.drawPath(cacheTransPath, this.mPaint);
        }
    }
}
