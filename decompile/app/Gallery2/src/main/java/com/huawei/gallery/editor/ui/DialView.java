package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.autonavi.amap.mapcore.MapConfig;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.gallery.util.ColorfulUtils;

public class DialView extends View {
    private static final float DIAL_CHORD = ((((float) GalleryUtils.getWidthPixels()) * MapConfig.MIN_ZOOM) / 4.0f);
    private static final float DIAL_RADIUS = ((float) (((double) DIAL_CHORD) / (Math.sin(0.5235987901687622d) * 2.0d)));
    private static final float DIAL_VER = ((float) (((double) DIAL_RADIUS) * Math.cos(0.5235987901687622d)));
    private static final float TRIANGLE_HALF_WIDTH = ((float) (((double) TRIANGLE_HEIGHT) * Math.atan(1.0471975803375244d)));
    protected static final int TRIANGLE_HEIGHT = GalleryUtils.dpToPixel(6);
    protected int mCenterX;
    protected int mCenterY;
    protected float mCurrentRotatedAngle;
    protected RectF mDisplayBounds;
    protected final Paint mFillColorPaint;
    protected float mLastRotatedAngle;
    protected float mLastTriangleBottom;
    protected float mMaxRotatedAngle;
    protected float mMinRotatedAngle;
    protected OnResetChangeListener mResetListener;
    protected OnRotateChangeListener mRotateChangeListener;
    protected Rect mTextBounds = new Rect();
    protected final Paint mTextPaint;
    protected float mTouchStartAngle;
    protected final Paint mTrianglePaint;
    protected final Path mTrianglePath = new Path();

    public interface OnResetChangeListener {
        void onResetLayoutChanged(float f, float f2);

        void onVisibilityChanged(boolean z);
    }

    public interface OnRotateChangeListener {
        void onAngleChanged(float f, boolean z);

        void onStartTrackingTouch();

        void onStopTrackingTouch();
    }

    public DialView(Context context, AttributeSet attrs) {
        super(context, attrs);
        int clockLabelColor = context.getResources().getColor(R.color.editor_label_color);
        this.mFillColorPaint = new Paint();
        this.mFillColorPaint.setStyle(Style.FILL);
        this.mFillColorPaint.setColor(clockLabelColor);
        this.mFillColorPaint.setAntiAlias(true);
        this.mTextPaint = new Paint();
        this.mTextPaint.setTextSize(36.0f);
        this.mTextPaint.setAntiAlias(true);
        this.mTextPaint.setColor(clockLabelColor);
        this.mTextPaint.setShadowLayer(2.0f, 0.0f, 0.0f, -16777216);
        this.mTrianglePaint = new Paint();
        this.mTrianglePaint.setStyle(Style.FILL);
        this.mTrianglePaint.setColor(ColorfulUtils.mappingColorfulColor(getContext(), context.getResources().getColor(R.color.editor_reset_color)));
        this.mTrianglePaint.setAntiAlias(true);
        setRotateSpan(45.0f, -45.0f);
    }

    public void setRotatedAngle(float degrees) {
        refreshAngle(degrees, false);
    }

    public void hide() {
        this.mCurrentRotatedAngle = 0.0f;
    }

    public void setRotateSpan(float maxDegrees, float minDegrees) {
        this.mMaxRotatedAngle = maxDegrees / 57.295776f;
        this.mMinRotatedAngle = minDegrees / 57.295776f;
    }

    public void setPhotoDisplayBounds(RectF bounds) {
        this.mDisplayBounds = bounds;
        computeArgs();
        invalidate();
    }

    public void setOnRotateChangeListener(OnRotateChangeListener listener) {
        this.mRotateChangeListener = listener;
    }

    public void setOnResetChangeListener(OnResetChangeListener listener) {
        this.mResetListener = listener;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        computeArgs();
    }

    protected void computeArgs() {
        this.mCenterX = Math.round(this.mDisplayBounds.centerX());
        this.mCenterY = Math.round(this.mDisplayBounds.centerY());
        this.mTrianglePath.reset();
        float trianglePoint;
        if (isPort()) {
            trianglePoint = (((((float) this.mCenterY) + (this.mDisplayBounds.height() / 2.0f)) + (getRadius() - getVer())) + 10.0f) + 8.0f;
            this.mTrianglePath.moveTo((float) this.mCenterX, trianglePoint);
            this.mTrianglePath.lineTo(((float) this.mCenterX) - TRIANGLE_HALF_WIDTH, ((float) TRIANGLE_HEIGHT) + trianglePoint);
            this.mTrianglePath.lineTo(((float) this.mCenterX) + TRIANGLE_HALF_WIDTH, ((float) TRIANGLE_HEIGHT) + trianglePoint);
            this.mTrianglePath.close();
            if (!isAlmostEqual(this.mLastTriangleBottom, ((float) TRIANGLE_HEIGHT) + trianglePoint)) {
                this.mLastTriangleBottom = ((float) TRIANGLE_HEIGHT) + trianglePoint;
                if (this.mResetListener != null) {
                    this.mResetListener.onResetLayoutChanged((float) this.mCenterX, this.mLastTriangleBottom);
                    return;
                }
                return;
            }
            return;
        }
        trianglePoint = (((((float) this.mCenterX) + (this.mDisplayBounds.width() / 2.0f)) + (getRadius() - getVer())) + 10.0f) + 8.0f;
        this.mTrianglePath.moveTo(trianglePoint, (float) this.mCenterY);
        this.mTrianglePath.lineTo(((float) TRIANGLE_HEIGHT) + trianglePoint, ((float) this.mCenterY) - TRIANGLE_HALF_WIDTH);
        this.mTrianglePath.lineTo(((float) TRIANGLE_HEIGHT) + trianglePoint, ((float) this.mCenterY) + TRIANGLE_HALF_WIDTH);
        this.mTrianglePath.close();
        if (!isAlmostEqual(this.mLastTriangleBottom, ((float) TRIANGLE_HEIGHT) + trianglePoint)) {
            this.mLastTriangleBottom = ((float) TRIANGLE_HEIGHT) + trianglePoint;
            if (this.mResetListener != null) {
                this.mResetListener.onResetLayoutChanged(this.mLastTriangleBottom, (float) (this.mCenterY - GalleryUtils.dpToPixel(16)));
            }
        }
    }

    private float getRadius() {
        return DIAL_RADIUS;
    }

    private float getVer() {
        return DIAL_VER;
    }

    private boolean isPort() {
        return getResources().getConfiguration().orientation == 1;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float currentAngle = this.mCurrentRotatedAngle * 57.295776f;
        int tempAngle = Math.round(currentAngle);
        int delta = 0;
        if (tempAngle % 2 != 0) {
            delta = tempAngle >= 0 ? 1 : -1;
        }
        tempAngle += delta;
        int init;
        int angle;
        float rotateAngle;
        boolean isTenMultiples;
        int pointRadius;
        String text;
        if (isPort()) {
            init = tempAngle;
            float dialCenterY = ((float) this.mCenterY) - (getVer() - (this.mDisplayBounds.height() / 2.0f));
            for (angle = tempAngle - 30; angle <= tempAngle + 30; angle += 2) {
                rotateAngle = ((float) angle) - currentAngle;
                float deltaHeight = (float) (((double) getRadius()) - (((double) getVer()) / Math.cos((double) ((Math.abs(rotateAngle) * 3.1415927f) / BitmapDescriptorFactory.HUE_CYAN))));
                isTenMultiples = angle % 10 == 0;
                pointRadius = isTenMultiples ? 8 : 4;
                if (((float) pointRadius) < deltaHeight) {
                    canvas.save();
                    canvas.rotate(rotateAngle, (float) this.mCenterX, dialCenterY);
                    this.mFillColorPaint.setStrokeWidth(((float) pointRadius) / 2.0f);
                    canvas.drawLine((float) this.mCenterX, (getRadius() + dialCenterY) - ((float) pointRadius), (float) this.mCenterX, ((float) pointRadius) + (getRadius() + dialCenterY), this.mFillColorPaint);
                    if (isTenMultiples) {
                        text = GalleryUtils.getValueFormat((long) (-angle));
                        this.mTextPaint.getTextBounds(text, 0, text.length(), this.mTextBounds);
                        if ((((double) this.mTextBounds.height()) / Math.cos(0.5235987901687622d)) + 20.0d < ((double) deltaHeight)) {
                            canvas.drawText(text, ((float) this.mCenterX) - (((float) this.mTextBounds.width()) / 2.0f), (getRadius() + dialCenterY) - MapConfig.MAX_ZOOM_INDOOR, this.mTextPaint);
                        }
                    }
                    canvas.restore();
                }
            }
        } else {
            init = tempAngle;
            float dialCenterX = ((float) this.mCenterX) - (DIAL_VER - (this.mDisplayBounds.width() / 2.0f));
            for (angle = tempAngle - 30; angle <= tempAngle + 30; angle += 2) {
                rotateAngle = ((float) angle) - currentAngle;
                float deltaWidth = (float) (((double) getRadius()) - (((double) getVer()) / Math.cos((double) ((Math.abs(rotateAngle) * 3.1415927f) / BitmapDescriptorFactory.HUE_CYAN))));
                isTenMultiples = angle % 10 == 0;
                pointRadius = isTenMultiples ? 8 : 4;
                if (((float) pointRadius) < deltaWidth) {
                    canvas.save();
                    canvas.rotate(rotateAngle, dialCenterX, (float) this.mCenterY);
                    this.mFillColorPaint.setStrokeWidth(((float) pointRadius) / 2.0f);
                    canvas.drawLine((getRadius() + dialCenterX) - ((float) pointRadius), (float) this.mCenterY, ((float) pointRadius) + (getRadius() + dialCenterX), (float) this.mCenterY, this.mFillColorPaint);
                    if (isTenMultiples) {
                        text = GalleryUtils.getValueFormat((long) (-angle));
                        this.mTextPaint.getTextBounds(text, 0, text.length(), this.mTextBounds);
                        if (((float) (this.mTextBounds.width() + 20)) < deltaWidth) {
                            canvas.drawText(text, ((getRadius() + dialCenterX) - MapConfig.MAX_ZOOM_INDOOR) - ((float) this.mTextBounds.width()), ((float) this.mCenterY) + (((float) this.mTextBounds.height()) / 2.0f), this.mTextPaint);
                        }
                    }
                    canvas.restore();
                }
            }
        }
        canvas.drawPath(this.mTrianglePath, this.mTrianglePaint);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        if (isEnabled()) {
            switch (ev.getAction()) {
                case 0:
                    this.mLastRotatedAngle = this.mCurrentRotatedAngle;
                    this.mTouchStartAngle = EditorUtils.calculateAngle(ev.getX(), ev.getY(), (float) this.mCenterX, (float) this.mCenterY);
                    if (this.mRotateChangeListener != null) {
                        this.mRotateChangeListener.onStartTrackingTouch();
                        break;
                    }
                    break;
                case 1:
                case 3:
                    if (this.mRotateChangeListener != null) {
                        this.mRotateChangeListener.onStopTrackingTouch();
                        break;
                    }
                    break;
                case 2:
                    float touchAngle = EditorUtils.calculateAngle(ev.getX(), ev.getY(), (float) this.mCenterX, (float) this.mCenterY);
                    float rotatedAngle = (touchAngle - this.mTouchStartAngle) + this.mLastRotatedAngle;
                    if (rotatedAngle <= this.mMaxRotatedAngle && rotatedAngle >= this.mMinRotatedAngle) {
                        refreshAngle((-rotatedAngle) * 57.295776f, true);
                        break;
                    }
                    this.mLastRotatedAngle = this.mCurrentRotatedAngle;
                    this.mTouchStartAngle = touchAngle;
                    break;
                    break;
            }
        }
        return true;
    }

    private void refreshAngle(float degrees, boolean fromUser) {
        this.mCurrentRotatedAngle = (-degrees) / 57.295776f;
        if (this.mRotateChangeListener != null) {
            this.mRotateChangeListener.onAngleChanged(degrees, fromUser);
        }
        if (this.mResetListener != null) {
            this.mResetListener.onVisibilityChanged(Math.abs(degrees) > 0.0f);
        }
        invalidate();
    }

    protected static boolean isAlmostEqual(float a, float b) {
        float diff = a - b;
        if (diff < 0.0f) {
            diff = -diff;
        }
        return diff < 0.02f;
    }
}
