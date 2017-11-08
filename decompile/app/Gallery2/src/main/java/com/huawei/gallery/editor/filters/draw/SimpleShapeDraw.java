package com.huawei.gallery.editor.filters.draw;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.huawei.gallery.editor.cache.CustDrawBrushCache;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation.StrokeData;

public class SimpleShapeDraw extends DrawStyle {
    private static final PointF INVALID_POINT = new PointF(GroundOverlayOptions.NO_DIMENSION, GroundOverlayOptions.NO_DIMENSION);
    private Paint mPaint = new Paint();
    private int mShape;

    public SimpleShapeDraw(int shape) {
        this.mShape = shape;
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setStrokeCap(Cap.SQUARE);
        this.mPaint.setStrokeJoin(Join.ROUND);
        this.mPaint.setAntiAlias(true);
    }

    public void paint(Bitmap appliedMosaicBitmap, Canvas canvas, Matrix toScrMatrix, StrokeData sd, CustDrawBrushCache brushCache) {
        if (sd != null && !INVALID_POINT.equals(sd.startPoint) && !INVALID_POINT.equals(sd.lastPoint)) {
            this.mPaint.setColor(sd.mColor);
            this.mPaint.setStrokeWidth(toScrMatrix.mapRadius(sd.mRadius));
            switch (this.mShape) {
                case 0:
                    canvas.drawLine(sd.startPoint.x, sd.startPoint.y, sd.lastPoint.x, sd.lastPoint.y, this.mPaint);
                    break;
                case 1:
                    canvas.drawRect(sd.startPoint.x, sd.startPoint.y, sd.lastPoint.x, sd.lastPoint.y, this.mPaint);
                    break;
                case 2:
                    canvas.drawOval(new RectF(sd.startPoint.x, sd.startPoint.y, sd.lastPoint.x, sd.lastPoint.y), this.mPaint);
                    break;
            }
        }
    }
}
