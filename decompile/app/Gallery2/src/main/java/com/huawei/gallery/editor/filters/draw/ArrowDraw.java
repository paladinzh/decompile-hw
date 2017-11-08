package com.huawei.gallery.editor.filters.draw;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.editor.cache.CustDrawBrushCache;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation.StrokeData;
import com.huawei.gallery.editor.ui.BasePaintBar;

public class ArrowDraw extends DrawStyle {
    private static final double DEFAULT_ARROW_HEIGHT = ((double) GalleryUtils.dpToPixel(10));
    private static final PointF INVALID_POINT = new PointF(GroundOverlayOptions.NO_DIMENSION, GroundOverlayOptions.NO_DIMENSION);
    private Paint mPaint = new Paint();

    public ArrowDraw() {
        this.mPaint.setStrokeCap(Cap.SQUARE);
        this.mPaint.setAntiAlias(true);
    }

    public void paint(Bitmap appliedMosaicBitmap, Canvas canvas, Matrix toScrMatrix, StrokeData sd, CustDrawBrushCache brushCache) {
        if (sd != null && !INVALID_POINT.equals(sd.startPoint) && !INVALID_POINT.equals(sd.lastPoint)) {
            double arrowLeftPointX;
            double arrowLeftPointY;
            double arrowRightPointX;
            double arrowRightPointY;
            double lineLastPointX;
            double lineLastPointY;
            double deltaAngle = Math.atan((double) (Math.abs(sd.startPoint.y - sd.lastPoint.y) / Math.abs(sd.startPoint.x - sd.lastPoint.x)));
            double arrowHeight = DEFAULT_ARROW_HEIGHT * ((double) (sd.mRadius / ((float) BasePaintBar.SUB_STROKE_LEVEL_VALUE[1])));
            double arrowHypot = arrowHeight / Math.cos(0.5235987755982988d);
            if (sd.startPoint.x <= sd.lastPoint.x && sd.startPoint.y >= sd.lastPoint.y) {
                arrowLeftPointX = ((double) sd.lastPoint.x) - (Math.cos(0.5235987755982988d - deltaAngle) * arrowHypot);
                arrowLeftPointY = ((double) sd.lastPoint.y) - (Math.sin(0.5235987755982988d - deltaAngle) * arrowHypot);
                arrowRightPointX = ((double) sd.lastPoint.x) - (Math.sin((1.5707963267948966d - deltaAngle) - 0.5235987755982988d) * arrowHypot);
                arrowRightPointY = ((double) sd.lastPoint.y) + (Math.cos((1.5707963267948966d - deltaAngle) - 0.5235987755982988d) * arrowHypot);
                lineLastPointX = ((double) sd.lastPoint.x) - (Math.cos(deltaAngle) * arrowHeight);
                lineLastPointY = ((double) sd.lastPoint.y) + (Math.sin(deltaAngle) * arrowHeight);
            } else if (sd.startPoint.x >= sd.lastPoint.x && sd.startPoint.y >= sd.lastPoint.y) {
                arrowLeftPointX = ((double) sd.lastPoint.x) + (Math.sin((1.5707963267948966d - deltaAngle) - 0.5235987755982988d) * arrowHypot);
                arrowLeftPointY = ((double) sd.lastPoint.y) + (Math.cos((1.5707963267948966d - deltaAngle) - 0.5235987755982988d) * arrowHypot);
                arrowRightPointX = ((double) sd.lastPoint.x) + (Math.cos(0.5235987755982988d - deltaAngle) * arrowHypot);
                arrowRightPointY = ((double) sd.lastPoint.y) - (Math.sin(0.5235987755982988d - deltaAngle) * arrowHypot);
                lineLastPointX = ((double) sd.lastPoint.x) + (Math.cos(deltaAngle) * arrowHeight);
                lineLastPointY = ((double) sd.lastPoint.y) + (Math.sin(deltaAngle) * arrowHeight);
            } else if (sd.startPoint.x < sd.lastPoint.x || sd.startPoint.y > sd.lastPoint.y) {
                arrowLeftPointX = ((double) sd.lastPoint.x) - (Math.sin((1.5707963267948966d - deltaAngle) - 0.5235987755982988d) * arrowHypot);
                arrowLeftPointY = ((double) sd.lastPoint.y) - (Math.cos((1.5707963267948966d - deltaAngle) - 0.5235987755982988d) * arrowHypot);
                arrowRightPointX = ((double) sd.lastPoint.x) - (Math.cos(0.5235987755982988d - deltaAngle) * arrowHypot);
                arrowRightPointY = ((double) sd.lastPoint.y) + (Math.sin(0.5235987755982988d - deltaAngle) * arrowHypot);
                lineLastPointX = ((double) sd.lastPoint.x) - (Math.cos(deltaAngle) * arrowHeight);
                lineLastPointY = ((double) sd.lastPoint.y) - (Math.sin(deltaAngle) * arrowHeight);
            } else {
                arrowLeftPointX = ((double) sd.lastPoint.x) + (Math.cos(deltaAngle - 0.5235987755982988d) * arrowHypot);
                arrowLeftPointY = ((double) sd.lastPoint.y) - (Math.sin(deltaAngle - 0.5235987755982988d) * arrowHypot);
                arrowRightPointX = ((double) sd.lastPoint.x) + (Math.sin((1.5707963267948966d - deltaAngle) - 0.5235987755982988d) * arrowHypot);
                arrowRightPointY = ((double) sd.lastPoint.y) - (Math.cos((1.5707963267948966d - deltaAngle) - 0.5235987755982988d) * arrowHypot);
                lineLastPointX = ((double) sd.lastPoint.x) + (Math.cos(deltaAngle) * arrowHeight);
                lineLastPointY = ((double) sd.lastPoint.y) - (Math.sin(deltaAngle) * arrowHeight);
            }
            this.mPaint.setColor(sd.mColor);
            this.mPaint.setStrokeWidth(toScrMatrix.mapRadius(sd.mRadius));
            this.mPaint.setStyle(Style.STROKE);
            canvas.drawLine(sd.startPoint.x, sd.startPoint.y, (float) lineLastPointX, (float) lineLastPointY, this.mPaint);
            Path path = new Path();
            path.moveTo(sd.lastPoint.x, sd.lastPoint.y);
            path.lineTo((float) arrowLeftPointX, (float) arrowLeftPointY);
            path.lineTo((float) arrowRightPointX, (float) arrowRightPointY);
            path.close();
            this.mPaint.setStyle(Style.FILL);
            canvas.drawPath(path, this.mPaint);
        }
    }
}
