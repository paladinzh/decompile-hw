package com.huawei.gallery.editor.ui;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.editor.filters.FilterIllusionRepresentation;

public class CircleControl extends FullScreenControl {
    private static final int DEFAULT_RADIUS = GalleryUtils.dpToPixel(90);
    private float mPointX = Float.NaN;
    private float mPointY = Float.NaN;
    private float mRadius = 0.0f;

    protected void initPoints(FilterIllusionRepresentation representation) {
        this.mPointX = (float) this.mImageBounds.centerX();
        this.mPointY = (float) this.mImageBounds.centerY();
        this.mRadius = (float) Math.min(DEFAULT_RADIUS, Math.min(this.mImageBounds.width(), this.mImageBounds.height()));
        if (representation != null) {
            representation.setPoint(this.mPointX, this.mPointY);
            representation.setRadius(this.mRadius);
        }
    }

    protected void updatePoints(FilterIllusionRepresentation representation, float scale) {
        this.mPointX *= scale;
        this.mPointY *= scale;
        this.mRadius *= scale;
        if (representation != null) {
            representation.setPoint(this.mPointX, this.mPointY);
            representation.setRadius(this.mRadius);
        }
    }

    public void actionDown(float x, float y) {
        super.actionDown(x, y);
    }

    public void actionUp(FilterIllusionRepresentation representation, boolean force) {
        super.actionUp(representation, force);
        float scale = this.mListener.getScaleScreenToImage(force);
        representation.setPoint(this.mPointX * scale, this.mPointY * scale);
        representation.setRadius(this.mRadius * scale);
    }

    public void actionUp(FilterIllusionRepresentation representation, float x, float y) {
        this.mPointX = x;
        this.mPointY = y;
        float scale = this.mListener.getScaleScreenToImage(false);
        representation.setPoint(this.mPointX * scale, this.mPointY * scale);
        representation.setRadius(this.mRadius * scale);
    }

    public void actionMove(float x, float y) {
        if (this.mTouchStyle != 1) {
            actionDown(x, y);
            return;
        }
        float dx = x - this.mDownX;
        float dy = y - this.mDownY;
        if (centerIsOutside(this.mPointX + dx, this.mPointY + dy)) {
            this.mDownX = x;
            this.mDownY = y;
            return;
        }
        this.mPointX += dx;
        this.mPointY += dy;
        this.mDownX = x;
        this.mDownY = y;
    }

    public void actionMove(float x1, float y1, float x2, float y2) {
        if (this.mTouchStyle != 2) {
            actionDown(x1, y1, x2, y2);
            return;
        }
        Matrix matrix = calculateMatrix(x1, y1, x2, y2);
        if (matrix != null) {
            float radius = matrix.mapRadius(this.mRadius);
            if (radius >= ((float) Math.min(this.mImageBounds.width(), this.mImageBounds.height())) / 8.0f && radius <= ((float) Math.max(this.mImageBounds.width(), this.mImageBounds.height())) / 2.0f) {
                this.mRadius = radius;
            }
        }
        this.mDown1X = x1;
        this.mDown1Y = y1;
        this.mDown2X = x2;
        this.mDown2Y = y2;
    }

    protected Matrix calculateMatrix(float x1, float y1, float x2, float y2) {
        float oldLength = getLength(this.mDown1X, this.mDown1Y, this.mDown2X, this.mDown2Y);
        float newLength = getLength(x1, y1, x2, y2);
        if (this.mRadius == 0.0f || this.mRadius + ((newLength - oldLength) / 2.0f) < 0.0f) {
            return null;
        }
        float scale = (this.mRadius + ((newLength - oldLength) / 2.0f)) / this.mRadius;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return matrix;
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        this.mPaint.reset();
        if (this.mNeedCover) {
            canvas.save();
            canvas.clipRect(this.mImageBounds);
            this.mPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            canvas.drawCircle(this.mPointX, this.mPointY, this.mRadius, this.mPaint);
            canvas.restore();
            canvas.clipRect(this.mImageBounds);
            this.mPaint.setColor(-1291845633);
            this.mPaint.setStrokeWidth((float) GalleryUtils.dpToPixel(1));
            this.mPaint.setXfermode(new PorterDuffXfermode(Mode.ADD));
            this.mPaint.setStyle(Style.STROKE);
            this.mPaint.setAntiAlias(true);
            canvas.drawCircle(this.mPointX, this.mPointY, this.mRadius, this.mPaint);
        }
    }
}
