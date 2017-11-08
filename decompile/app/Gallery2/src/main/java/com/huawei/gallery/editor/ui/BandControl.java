package com.huawei.gallery.editor.ui;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.editor.filters.FilterIllusionRepresentation;
import com.huawei.gallery.editor.tools.EditorUtils;

public class BandControl extends FullScreenControl {
    private float mInitAngle;
    private float mInitLength;
    private float mPoint1X = Float.NaN;
    private float mPoint1Y;
    private float mPoint2X;
    private float mPoint2Y;
    private RectF mRectF = new RectF();

    protected void initPoints(FilterIllusionRepresentation representation) {
        this.mPoint1X = (float) this.mImageBounds.centerX();
        this.mPoint1Y = (float) (this.mImageBounds.centerY() - (this.mImageBounds.height() / 8));
        this.mPoint2X = (float) this.mImageBounds.centerX();
        this.mPoint2Y = (float) (this.mImageBounds.centerY() + (this.mImageBounds.height() / 8));
        this.mRectF.set(-4096.0f, this.mPoint1Y, 4096.0f, this.mPoint2Y);
        this.mInitLength = this.mRectF.height();
        this.mInitAngle = 1.5707964f;
        if (representation != null) {
            representation.setPoint1(this.mPoint1X, this.mPoint1Y);
            representation.setPoint2(this.mPoint2X, this.mPoint2Y);
        }
    }

    protected void updatePoints(FilterIllusionRepresentation representation, float scale) {
        this.mPoint1X *= scale;
        this.mPoint2X *= scale;
        this.mPoint1Y *= scale;
        this.mPoint2Y *= scale;
        this.mRectF.set(-4096.0f, (float) (this.mImageBounds.centerY() - (this.mImageBounds.height() / 8)), 4096.0f, (float) (this.mImageBounds.centerY() + (this.mImageBounds.height() / 8)));
        this.mInitLength = this.mRectF.height();
        this.mInitAngle = 1.5707964f;
        if (representation != null) {
            representation.setPoint1(this.mPoint1X, this.mPoint1Y);
            representation.setPoint2(this.mPoint2X, this.mPoint2Y);
        }
    }

    public void actionDown(float x, float y) {
        super.actionDown(x, y);
    }

    public void actionUp(FilterIllusionRepresentation representation, boolean force) {
        super.actionUp(representation, force);
        float scale = this.mListener.getScaleScreenToImage(force);
        representation.setPoint1(this.mPoint1X * scale, this.mPoint1Y * scale);
        representation.setPoint2(this.mPoint2X * scale, this.mPoint2Y * scale);
    }

    public void actionUp(FilterIllusionRepresentation representation, float x, float y) {
        float dx = x - ((this.mPoint1X + this.mPoint2X) / 2.0f);
        float dy = y - ((this.mPoint1Y + this.mPoint2Y) / 2.0f);
        this.mPoint1X += dx;
        this.mPoint2X += dx;
        this.mPoint1Y += dy;
        this.mPoint2Y += dy;
        float scale = this.mListener.getScaleScreenToImage(false);
        representation.setPoint1(this.mPoint1X * scale, this.mPoint1Y * scale);
        representation.setPoint2(this.mPoint2X * scale, this.mPoint2Y * scale);
    }

    public void actionMove(float x, float y) {
        if (this.mTouchStyle != 1) {
            actionDown(x, y);
            return;
        }
        float dx = x - this.mDownX;
        float dy = y - this.mDownY;
        if (centerIsOutside((((this.mPoint1X + dx) + this.mPoint2X) + dx) / 2.0f, (((this.mPoint1Y + dy) + this.mPoint2Y) + dy) / 2.0f)) {
            this.mDownX = x;
            this.mDownY = y;
            return;
        }
        this.mPoint1X += dx;
        this.mPoint1Y += dy;
        this.mPoint2X += dx;
        this.mPoint2Y += dy;
        this.mDownX = x;
        this.mDownY = y;
    }

    public void actionMove(float x1, float y1, float x2, float y2) {
        if (this.mTouchStyle != 2) {
            actionDown(x1, y1, x2, y2);
            return;
        }
        Matrix matrix = calculateMatrix(x1, y1, x2, y2);
        temp1 = new float[2];
        float[] temp2 = new float[]{this.mPoint1X, this.mPoint1Y};
        matrix.mapPoints(temp1);
        temp2[0] = this.mPoint2X;
        temp2[1] = this.mPoint2Y;
        matrix.mapPoints(temp2);
        float len = getLength(temp1[0], temp1[1], temp2[0], temp2[1]);
        if (len < ((float) Math.min(this.mImageBounds.width(), this.mImageBounds.height())) / 4.0f || len > ((float) Math.max(this.mImageBounds.width(), this.mImageBounds.height())) * 0.8f) {
            matrix = calculateMatrixWidthOutScale(x1, y1, x2, y2);
            temp1[0] = this.mPoint1X;
            temp1[1] = this.mPoint1Y;
            matrix.mapPoints(temp1);
            temp2[0] = this.mPoint2X;
            temp2[1] = this.mPoint2Y;
            matrix.mapPoints(temp2);
        }
        this.mPoint1X = temp1[0];
        this.mPoint1Y = temp1[1];
        this.mPoint2X = temp2[0];
        this.mPoint2Y = temp2[1];
        this.mDown1X = x1;
        this.mDown1Y = y1;
        this.mDown2X = x2;
        this.mDown2Y = y2;
    }

    protected Matrix calculateMatrix(float x1, float y1, float x2, float y2) {
        float dx = getLength(x1, y1, x2, y2) - getLength(this.mDown1X, this.mDown1Y, this.mDown2X, this.mDown2Y);
        float len = getLength(this.mPoint1X, this.mPoint1Y, this.mPoint2X, this.mPoint2Y);
        float scale = (len + dx) / len;
        float dAngle = EditorUtils.calculateAngle(x1 - ((x1 + x2) / 2.0f), y1 - ((y1 + y2) / 2.0f)) - EditorUtils.calculateAngle(this.mDown1X - ((this.mDown1X + this.mDown2X) / 2.0f), this.mDown1Y - ((this.mDown1Y + this.mDown2Y) / 2.0f));
        Matrix matrix = new Matrix();
        matrix.setTranslate((-(this.mPoint1X + this.mPoint2X)) / 2.0f, (-(this.mPoint1Y + this.mPoint2Y)) / 2.0f);
        matrix.postRotate(57.295776f * dAngle);
        matrix.postScale(scale, scale);
        matrix.postTranslate((this.mPoint1X + this.mPoint2X) / 2.0f, (this.mPoint1Y + this.mPoint2Y) / 2.0f);
        return matrix;
    }

    private Matrix calculateMatrixWidthOutScale(float x1, float y1, float x2, float y2) {
        float dAngle = EditorUtils.calculateAngle(x1 - ((x1 + x2) / 2.0f), y1 - ((y1 + y2) / 2.0f)) - EditorUtils.calculateAngle(this.mDown1X - ((this.mDown1X + this.mDown2X) / 2.0f), this.mDown1Y - ((this.mDown1Y + this.mDown2Y) / 2.0f));
        Matrix matrix = new Matrix();
        matrix.setTranslate((-(this.mPoint1X + this.mPoint2X)) / 2.0f, (-(this.mPoint1Y + this.mPoint2Y)) / 2.0f);
        matrix.postRotate(57.295776f * dAngle);
        matrix.postTranslate((this.mPoint1X + this.mPoint2X) / 2.0f, (this.mPoint1Y + this.mPoint2Y) / 2.0f);
        return matrix;
    }

    private void calculateCanvas(Canvas canvas, float x1, float y1, float x2, float y2) {
        float oldXCenter = (float) this.mImageBounds.centerX();
        float oldYCenter = (float) this.mImageBounds.centerY();
        float newXcenter = (x1 + x2) / 2.0f;
        float newYcenter = (y1 + y2) / 2.0f;
        float scale = getLength(x1, y1, x2, y2) / this.mInitLength;
        float angle = EditorUtils.calculateAngle(x1 - newXcenter, y1 - newYcenter) - this.mInitAngle;
        canvas.translate(newXcenter - oldXCenter, newYcenter - oldYCenter);
        canvas.translate(oldXCenter, oldYCenter);
        canvas.scale(scale, scale);
        canvas.rotate(57.295776f * angle);
        canvas.translate(-oldXCenter, -oldYCenter);
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        this.mPaint.reset();
        if (this.mNeedCover) {
            canvas.save();
            canvas.clipRect(this.mImageBounds);
            calculateCanvas(canvas, this.mPoint1X, this.mPoint1Y, this.mPoint2X, this.mPoint2Y);
            this.mPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            canvas.drawRect(this.mRectF, this.mPaint);
            canvas.restore();
            this.mPaint.setColor(-1291845633);
            canvas.save();
            canvas.clipRect(this.mImageBounds);
            calculateCanvas(canvas, this.mPoint1X, this.mPoint1Y, this.mPoint2X, this.mPoint2Y);
            this.mPaint.setXfermode(new PorterDuffXfermode(Mode.ADD));
            this.mPaint.setStrokeWidth(((float) GalleryUtils.dpToPixel(1)) / (getLength(this.mPoint1X, this.mPoint1Y, this.mPoint2X, this.mPoint2Y) / (((float) this.mImageBounds.height()) * 0.25f)));
            this.mPaint.setStyle(Style.STROKE);
            this.mPaint.setAntiAlias(true);
            canvas.drawRect(this.mRectF, this.mPaint);
            canvas.restore();
        }
    }
}
