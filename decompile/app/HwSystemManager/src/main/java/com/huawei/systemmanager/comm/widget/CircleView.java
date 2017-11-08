package com.huawei.systemmanager.comm.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;

public abstract class CircleView extends View {
    public static final int MAX_ALPHA = 255;
    public static final float MAX_ANGLE = 360.0f;
    public static final float START_ANGLE = 90.0f;
    private static final String TAG = "CircleView";
    private float mAngleOffset;
    private Paint mBackgroundPaint;
    private int mCenterX;
    private int mCenterY;
    private int mLineLength;
    private float[] mLinePoints;
    private Paint mPointPaint;
    private int mPointRadius;
    private int mPonitPositionOffset;
    private Paint mProgressPaint;
    public float mStartAngle;

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mStartAngle = START_ANGLE;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleViewAttrs);
        initBackgroundCirclePaint(a);
        initProgressPaint(a);
        intitPointPaint(a);
        initOther(a);
        a.recycle();
    }

    private void initBackgroundCirclePaint(TypedArray a) {
        this.mBackgroundPaint = new Paint();
        this.mBackgroundPaint.setStyle(Style.STROKE);
        this.mBackgroundPaint.setAntiAlias(true);
        this.mBackgroundPaint.setStrokeWidth((float) a.getDimensionPixelSize(0, 2));
        this.mBackgroundPaint.setColor(a.getColor(1, Color.argb(50, 255, 255, 255)));
    }

    private void initProgressPaint(TypedArray a) {
        this.mProgressPaint = new Paint();
        this.mProgressPaint.setStyle(Style.STROKE);
        this.mProgressPaint.setAntiAlias(true);
        this.mProgressPaint.setStrokeWidth((float) a.getDimensionPixelSize(2, 4));
        this.mProgressPaint.setColor(a.getColor(3, -1));
    }

    private void intitPointPaint(TypedArray a) {
        this.mPointPaint = new Paint();
        this.mPointPaint.setStrokeWidth(Utility.ALPHA_MAX);
        this.mPointPaint.setAntiAlias(true);
        this.mPointPaint.setStyle(Style.FILL);
        this.mPointPaint.setColor(a.getColor(6, -1));
        this.mPointRadius = a.getDimensionPixelSize(7, 5);
        this.mPonitPositionOffset = a.getDimensionPixelSize(8, 0);
    }

    private void initOther(TypedArray a) {
        this.mLineLength = a.getDimensionPixelSize(4, 10);
        this.mAngleOffset = a.getFloat(5, 2.0f);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int preCenterX = this.mCenterX;
        int preCenterY = this.mCenterY;
        this.mCenterX = getWidth() / 2;
        this.mCenterY = getHeight() / 2;
        if (preCenterX != this.mCenterX || preCenterY != this.mCenterY) {
            initPoints();
        }
    }

    private void initPoints() {
        float angleOffset = this.mAngleOffset;
        float xOffset = (float) this.mCenterX;
        float yOffset = (float) this.mCenterY;
        float xStart = (float) (this.mCenterX - this.mLineLength);
        float yStart = (float) (this.mCenterY - this.mLineLength);
        float xEnd = (float) this.mCenterX;
        float yEnd = (float) this.mCenterY;
        float[] points = new float[((((int) (360.0f / angleOffset)) + 1) * 4)];
        int i = 0;
        float endAngle = 360.0f + this.mStartAngle;
        for (float angle = this.mStartAngle; Float.compare(endAngle, angle) > 0; angle += angleOffset) {
            double angleSin = Math.sin(Math.toRadians((double) angle));
            double angleCos = Math.cos(Math.toRadians((double) angle));
            int i2 = i + 1;
            points[i] = ((float) (((double) xStart) * angleCos)) + xOffset;
            i = i2 + 1;
            points[i2] = ((float) (((double) yStart) * angleSin)) + yOffset;
            i2 = i + 1;
            points[i] = ((float) (((double) xEnd) * angleCos)) + xOffset;
            i = i2 + 1;
            points[i2] = ((float) (((double) yEnd) * angleSin)) + yOffset;
        }
        this.mLinePoints = points;
    }

    public void drawCircle(Canvas canvas, float startAngle, float endAngle, Paint paint) {
        if (this.mLinePoints == null) {
            HwLog.d(TAG, "drawCircle mLinePoints is null");
            return;
        }
        startAngle = Math.max(0.0f, startAngle);
        endAngle = Math.min(endAngle, 360.0f);
        int pointCount = this.mLinePoints.length;
        int offset = ((int) (startAngle / this.mAngleOffset)) * 4;
        int count = ((int) ((endAngle - startAngle) / this.mAngleOffset)) * 4;
        if (offset >= pointCount) {
            HwLog.e(TAG, "offset beyond pointCount!");
            return;
        }
        if (offset + count > pointCount) {
            HwLog.w(TAG, "offset + count > pointCount, adjust it");
            count = pointCount - offset;
        }
        canvas.drawLines(this.mLinePoints, offset, count, paint);
    }

    protected void drawBackground(Canvas canvas) {
        drawCircle(canvas, 0.0f, 360.0f, this.mBackgroundPaint);
    }

    protected void drawProgress(Canvas canvas, float progress) {
        drawCircle(canvas, 0.0f, (progress / 100.0f) * 360.0f, this.mProgressPaint);
    }

    protected void drawProgress(Canvas canvas) {
        drawCircle(canvas, 0.0f, 360.0f, this.mProgressPaint);
    }

    protected void drawPoint(Canvas canvas) {
        canvas.drawCircle((float) this.mCenterX, (float) ((this.mLineLength + this.mPointRadius) + this.mPonitPositionOffset), (float) this.mPointRadius, this.mPointPaint);
    }

    protected float getAngleOffset() {
        return this.mAngleOffset;
    }

    protected void setProgressColor(int color) {
        this.mProgressPaint.setColor(color);
    }

    public Paint getBackgroundPaint() {
        return this.mBackgroundPaint;
    }

    protected Paint getProgressPaint() {
        return this.mProgressPaint;
    }

    protected Paint getPonitPaint() {
        return this.mPointPaint;
    }

    protected int getCenterX() {
        return this.mCenterX;
    }

    protected int getCenterY() {
        return this.mCenterY;
    }
}
