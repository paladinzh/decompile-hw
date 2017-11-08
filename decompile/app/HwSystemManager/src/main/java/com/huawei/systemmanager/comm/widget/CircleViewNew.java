package com.huawei.systemmanager.comm.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HSMConst;
import com.huawei.systemmanager.util.HwLog;

public abstract class CircleViewNew extends View {
    public static final int MAX_ALPHA = 255;
    public static final float MAX_ANGLE = 360.0f;
    public static final int ROLL_LINES_NUM = 30;
    public static final int SIZE_OF_COLOR = 120;
    public static final int SIZE_OF_HALF_COLOR = 60;
    public static final int SIZE_OF_POINTS = 480;
    public static final float START_ANGLE = -90.0f;
    private static final String TAG = "CircleView_5_0";
    private float circle_size_percent;
    private float mAngleOffset;
    private int mCenterX;
    private int mCenterY;
    private int[] mColorSet;
    private Paint mFirstBackgroundPaint;
    private int[] mHalfColorSet;
    private int mLineLength;
    private float[] mLinePoints;
    private Paint mProgressPaint;
    private Resources mRes;
    private int[] mRollAlphaSet;
    private Paint mSecondBackgroundPaint;
    public float mStartAngle;
    private Paint mThirdBackgroundPaint;
    private int secondBackgroundOffset;
    private int secondBackgroundWidth;
    private int thirdBackgroundOffset;
    private int thirdBackgroundStart;

    public CircleViewNew(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleViewNew(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mStartAngle = START_ANGLE;
        this.mColorSet = new int[SIZE_OF_COLOR];
        this.mHalfColorSet = new int[60];
        this.mRollAlphaSet = new int[30];
        this.mRes = context.getResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleViewAttrs);
        initFirstBackgroundCirclePaint(a);
        initSecondBackgroundCirclePaint(a);
        initThirdBackgroundCirclePaint(a);
        initProgressPaint(a);
        initOther(a);
        a.recycle();
    }

    private void initFirstBackgroundCirclePaint(TypedArray a) {
        this.mFirstBackgroundPaint = new Paint();
        Shader mFirstShader = new LinearGradient(150.0f, 0.0f, 150.0f, 500.0f, this.mRes.getColor(R.color.main_scren_circle_first_shader_color0), this.mRes.getColor(R.color.main_scren_circle_first_shader_color1), TileMode.CLAMP);
        this.mFirstBackgroundPaint.setStyle(Style.STROKE);
        this.mFirstBackgroundPaint.setAntiAlias(true);
        this.mFirstBackgroundPaint.setColor(this.mRes.getColor(R.color.main_scren_circle_first_color));
        this.mFirstBackgroundPaint.setStrokeWidth(2.0f);
        this.mFirstBackgroundPaint.setShader(mFirstShader);
    }

    private void initSecondBackgroundCirclePaint(TypedArray a) {
        this.mSecondBackgroundPaint = new Paint();
        Shader mSecondShader = new LinearGradient(150.0f, 0.0f, 150.0f, 400.0f, this.mRes.getColor(R.color.main_scren_circle_second_shader_color0), this.mRes.getColor(R.color.main_scren_circle_second_shader_color1), TileMode.CLAMP);
        this.mSecondBackgroundPaint.setStyle(Style.STROKE);
        this.mSecondBackgroundPaint.setAntiAlias(true);
        this.mSecondBackgroundPaint.setStrokeWidth(0.5f);
        this.mSecondBackgroundPaint.setColor(this.mRes.getColor(R.color.main_scren_circle_second_color));
        this.mSecondBackgroundPaint.setShader(mSecondShader);
    }

    private void initThirdBackgroundCirclePaint(TypedArray a) {
        this.mThirdBackgroundPaint = new Paint();
        this.mThirdBackgroundPaint.setStyle(Style.STROKE);
        this.mThirdBackgroundPaint.setAntiAlias(true);
        this.mThirdBackgroundPaint.setStrokeJoin(Join.ROUND);
        this.mThirdBackgroundPaint.setStrokeCap(Cap.ROUND);
        this.mThirdBackgroundPaint.setStrokeWidth((float) a.getDimensionPixelSize(2, 4));
        this.mThirdBackgroundPaint.setColor(getContext().getResources().getColor(R.color.main_scren_circle_degree_background));
    }

    private void initProgressPaint(TypedArray a) {
        this.mProgressPaint = new Paint();
        this.mProgressPaint.setStyle(Style.STROKE);
        this.mProgressPaint.setAntiAlias(true);
        this.mProgressPaint.setStrokeJoin(Join.ROUND);
        this.mProgressPaint.setStrokeCap(Cap.ROUND);
        this.mProgressPaint.setStrokeWidth((float) a.getDimensionPixelSize(2, 4));
        this.mProgressPaint.setColor(a.getColor(3, -1));
        getPaintColor(getResources().getColor(R.color.hsm_widget_circleview_startcolor), getResources().getColor(R.color.emui5_theme), 60);
        getColorFinally();
        getPaintAlpha(255, 0, 30);
    }

    private void initOther(TypedArray a) {
        this.mLineLength = a.getDimensionPixelSize(4, 9);
        this.mAngleOffset = a.getFloat(5, 2.0f);
        this.circle_size_percent = (float) (((double) a.getInteger(9, 70)) / 100.0d);
        this.secondBackgroundOffset = a.getDimensionPixelSize(11, 15);
        this.thirdBackgroundOffset = a.getDimensionPixelSize(12, 18);
        this.secondBackgroundWidth = (this.thirdBackgroundOffset + this.mLineLength) * 2;
        this.thirdBackgroundStart = (this.secondBackgroundOffset + this.thirdBackgroundOffset) + 3;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int border = (int) (((float) HSMConst.getLongOrShortLength(false)) * this.circle_size_percent);
        super.onMeasure(MeasureSpec.makeMeasureSpec(border, 1073741824), MeasureSpec.makeMeasureSpec(border, 1073741824));
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
        float xStart = (float) ((this.mCenterX - this.mLineLength) - this.thirdBackgroundStart);
        float yStart = (float) ((this.mCenterX - this.mLineLength) - this.thirdBackgroundStart);
        float xEnd = (float) (this.mCenterX - this.thirdBackgroundStart);
        float yEnd = (float) (this.mCenterX - this.thirdBackgroundStart);
        float[] points = new float[((((int) (360.0f / angleOffset)) + 1) * 4)];
        int i = 0;
        float endAngle = 360.0f + this.mStartAngle;
        for (float angle = this.mStartAngle; Float.compare(endAngle, angle) > 0; angle += angleOffset) {
            double angleCos = Math.cos(Math.toRadians((double) angle));
            double angleSin = Math.sin(Math.toRadians((double) angle));
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
        if (this.mLinePoints != null) {
            startAngle = Math.max(0.0f, startAngle);
            int offset = ((int) (startAngle / this.mAngleOffset)) * 4;
            int count = ((int) ((Math.min(endAngle, 360.0f) - startAngle) / this.mAngleOffset)) * 4;
            int pointCount = this.mLinePoints.length;
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
    }

    public void drawCircle_color(Canvas canvas, float startAngle, float endAngle, Paint paint) {
        int color_num = 0;
        if (this.mLinePoints != null) {
            int count = ((int) ((Math.min(endAngle, 360.0f) - Math.max(0.0f, startAngle)) / this.mAngleOffset)) * 4;
            int pointCount = this.mLinePoints.length;
            if (count + 0 > pointCount) {
                HwLog.w(TAG, "offset + count > pointCount, adjust it");
                count = pointCount + 0;
            }
            for (int i = 0; i < count; i += 4) {
                float start_x = this.mLinePoints[i];
                float start_y = this.mLinePoints[i + 1];
                float end_x = this.mLinePoints[i + 2];
                float end_y = this.mLinePoints[i + 3];
                paint.setColor(this.mColorSet[color_num]);
                color_num++;
                canvas.drawLine(start_x, start_y, end_x, end_y, paint);
            }
        }
    }

    public void drawCircle_roll(Canvas canvas, float endAngle, Paint paint, int alphaRef) {
        int color_num = 0;
        if (this.mLinePoints != null) {
            endAngle = Math.min(endAngle, 360.0f);
            int offset = ((int) (endAngle / this.mAngleOffset)) * 4;
            int colorOffset = (int) (endAngle / this.mAngleOffset);
            int count = offset + SIZE_OF_COLOR;
            for (int i = offset; i < count; i += 4) {
                int iBuff = i;
                int colorBuff = color_num + colorOffset;
                int alphaBuff = color_num - alphaRef;
                if (i >= SIZE_OF_POINTS) {
                    iBuff -= 480;
                }
                if (colorBuff >= SIZE_OF_COLOR) {
                    colorBuff -= 120;
                }
                if (alphaBuff <= 0) {
                    alphaBuff = 0;
                }
                float start_x = this.mLinePoints[iBuff];
                float start_y = this.mLinePoints[iBuff + 1];
                float end_x = this.mLinePoints[iBuff + 2];
                float end_y = this.mLinePoints[iBuff + 3];
                paint.setColor(this.mColorSet[colorBuff]);
                paint.setAlpha(this.mRollAlphaSet[alphaBuff]);
                color_num++;
                canvas.drawLine(start_x, start_y, end_x, end_y, paint);
            }
        }
    }

    protected void drawBackground(Canvas canvas) {
        canvas.drawCircle((float) this.mCenterX, (float) this.mCenterY, (float) this.mCenterX, this.mFirstBackgroundPaint);
        drawSecondBackground(canvas);
        drawCircle(canvas, 0.0f, 360.0f, this.mThirdBackgroundPaint);
    }

    protected Paint getProgressPaint() {
        return this.mProgressPaint;
    }

    protected void getPaintColor(int startColor, int endColor, int num) {
        int startRed = (16711680 & startColor) >> 16;
        int startGreen = (MotionEventCompat.ACTION_POINTER_INDEX_MASK & startColor) >> 8;
        int startBlue = startColor & 255;
        float red_f = (float) ((((double) (startRed - ((16711680 & endColor) >> 16))) * 1.0d) / ((double) num));
        float green_f = (float) ((((double) (startGreen - ((MotionEventCompat.ACTION_POINTER_INDEX_MASK & endColor) >> 8))) * 1.0d) / ((double) num));
        float blue_f = (float) ((((double) (startBlue - (endColor & 255))) * 1.0d) / ((double) num));
        for (int i = 0; i < num; i++) {
            int mBlue = (int) (((float) startBlue) + (((float) i) * blue_f));
            this.mHalfColorSet[i] = (((((int) (((float) startRed) - (((float) i) * red_f))) << 16) + ViewCompat.MEASURED_STATE_MASK) + (((int) (((float) startGreen) + (((float) i) * green_f))) << 8)) + mBlue;
        }
    }

    protected void getColorFinally() {
        for (int i = 0; i < 60; i++) {
            this.mColorSet[i] = this.mHalfColorSet[59 - i];
            this.mColorSet[i + 60] = this.mHalfColorSet[i];
        }
    }

    protected void getPaintAlpha(int startAlpha, int endAlpha, int num) {
        float alpha_f = (float) ((((double) (startAlpha - endAlpha)) * 1.0d) / ((double) num));
        for (int i = 0; i < num; i++) {
            this.mRollAlphaSet[i] = (int) (((float) endAlpha) + (((float) i) * alpha_f));
        }
    }

    protected void drawSecondBackground(Canvas canvas) {
        float drawRadio = (float) (this.mCenterX - this.secondBackgroundOffset);
        for (int i = 0; i < this.secondBackgroundWidth; i++) {
            canvas.drawCircle((float) this.mCenterX, (float) this.mCenterY, drawRadio - (((float) i) / 2.0f), this.mSecondBackgroundPaint);
            this.mSecondBackgroundPaint.setAlpha(this.secondBackgroundWidth - i);
        }
    }
}
