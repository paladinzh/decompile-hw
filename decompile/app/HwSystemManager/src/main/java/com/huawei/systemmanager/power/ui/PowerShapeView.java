package com.huawei.systemmanager.power.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Region.Op;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;

public class PowerShapeView extends View {
    private static final int ALPHA_MAX = 255;
    private static final float DRUATION = 2000.0f;
    private static final float TRANSLATION_ANGLR = 0.08f;
    private static float frequency = Utility.ALPHA_MAX;
    private int mAlpha;
    private float mAngle = 0.0f;
    private boolean mBegin = false;
    private int mCurrentLevel = 0;
    private boolean mEnd = false;
    private boolean mGettingTop = false;
    private float mHeight = 0.0f;
    private float mHeightOffset = 0.0f;
    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private Paint mPaint;
    private Paint mPaintCircle;
    private Paint mPaintWaterCircle;
    private float[] mPointbg;
    private float[] mPoints;
    private float mRadius = 0.0f;
    private float mRotation;
    private float mScanPointRadius = 9.0f;
    private boolean mScaning = false;
    private Shader mShader;
    private long mStartTime = -1;
    private float mWaterHeight;
    private float mWidth = 0.0f;

    public PowerShapeView(Context context) {
        super(context);
        initView(context);
    }

    public PowerShapeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        this.mContext = context;
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setColor(getResources().getColor(R.color.hwsystemmanager_white_alpha30_color));
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setStrokeWidth(4.0f);
        this.mPaintCircle = new Paint();
        this.mPaintCircle.setAntiAlias(true);
        this.mPaintCircle.setStrokeWidth(4.0f);
        this.mPaintCircle.setStyle(Style.FILL);
        this.mPaintWaterCircle = new Paint();
        this.mPaintWaterCircle.setStrokeWidth((float) dip2px(2.0f));
        this.mPaintWaterCircle.setColor(getResources().getColor(R.color.hwsystemmanager_white_alpha30_color));
        this.mPaintWaterCircle.setStyle(Style.STROKE);
        this.mPaintWaterCircle.setAntiAlias(true);
        this.mScanPointRadius = getResources().getDimension(R.dimen.power_scan_point_raduis);
        float dimension = this.mContext.getResources().getDimension(R.dimen.power_circle_view_width);
        this.mWidth = dimension;
        this.mHeight = dimension;
        this.mRadius = this.mHeight / 2.0f;
        this.mWaterHeight = this.mHeight - this.mScanPointRadius;
        this.mHeightOffset = (this.mHeight - this.mScanPointRadius) / 100.0f;
        this.mPoints = new float[((int) (this.mWidth * 2.0f))];
        this.mPointbg = new float[((int) (this.mWidth * 2.0f))];
    }

    protected void onDraw(Canvas canvas) {
        caculatData();
        drawWaterWave(canvas);
        drawProgressCircle(canvas);
        invalidate();
    }

    public int dip2px(float dipValue) {
        return (int) ((dipValue * this.mContext.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean isOutRange;
        if (Float.compare((float) Math.pow(Math.pow(Math.abs(((double) event.getX()) - (((double) getWidth()) / 2.0d)), 2.0d) + Math.pow(Math.abs(((double) event.getY()) - (((double) getHeight()) / 2.0d)), 2.0d), 0.5d), (float) dip2px(92.0f)) > 0) {
            isOutRange = true;
        } else {
            isOutRange = false;
        }
        if (!isOutRange) {
            return super.onTouchEvent(event);
        }
        setPressed(false);
        return true;
    }

    private void drawWaterWave(Canvas canvas) {
        Path path = new Path();
        for (int i = 0; ((float) i) < this.mWidth; i += 4) {
            this.mPoints[i] = (float) i;
            this.mPointbg[i] = (float) i;
            this.mPoints[i + 1] = (this.mWaterHeight + this.mScanPointRadius) - (((float) Math.sin((((((double) frequency) * 6.283185307179586d) * ((double) i)) / ((double) this.mWidth)) + ((double) this.mAngle))) * 15.0f);
            this.mPointbg[i + 1] = (this.mWaterHeight + this.mScanPointRadius) - (((float) Math.sin(((((((double) frequency) * 6.283185307179586d) * ((double) i)) / ((double) this.mWidth)) + ((double) this.mAngle)) + 60.0d)) * 15.0f);
            this.mPoints[i + 2] = (float) i;
            this.mPointbg[i + 2] = (float) i;
            this.mPoints[i + 3] = this.mHeight - this.mScanPointRadius;
            this.mPointbg[i + 3] = this.mHeight - this.mScanPointRadius;
        }
        canvas.save();
        path.addCircle(this.mWidth / 2.0f, this.mHeight / 2.0f, (this.mRadius - this.mScanPointRadius) - ((float) dip2px(6.0f)), Direction.CCW);
        canvas.clipPath(path, Op.INTERSECT);
        this.mPaint.setShader(this.mShader);
        this.mPaint.setAlpha(255);
        canvas.drawLines(this.mPoints, this.mPaint);
        this.mPaint.setAlpha(100);
        canvas.drawLines(this.mPointbg, this.mPaint);
        canvas.restore();
        canvas.drawCircle(this.mWidth / 2.0f, this.mHeight / 2.0f, this.mRadius - this.mScanPointRadius, this.mPaintWaterCircle);
    }

    private void drawProgressCircle(Canvas canvas) {
        if (this.mScaning || this.mEnd) {
            canvas.rotate(this.mRotation, this.mWidth / 2.0f, this.mHeight / 2.0f);
            this.mPaintCircle.setColor(Color.argb(this.mAlpha, 255, 255, 255));
            canvas.drawCircle(this.mWidth / 2.0f, this.mScanPointRadius, this.mScanPointRadius, this.mPaintCircle);
        }
    }

    private void linearShaderMaker() {
        float f = 0.0f;
        this.mShader = new LinearGradient(((float) this.mCurrentLevel) + this.mScanPointRadius, 0.0f, f, this.mHeight, new int[]{getResources().getColor(R.color.power_powershape_shader1), getResources().getColor(R.color.power_powershape_shader2)}, null, TileMode.CLAMP);
    }

    public void setScaningFlag(boolean scaning) {
        if (scaning) {
            this.mRotation = 0.0f;
            this.mStartTime = -1;
            this.mBegin = true;
        } else {
            this.mEnd = true;
        }
        this.mScaning = scaning;
    }

    public boolean getScaningFlag() {
        return this.mScaning;
    }

    public void updateWaterLevel(int level) {
        if (level != this.mCurrentLevel) {
            linearShaderMaker();
        }
        this.mCurrentLevel = level;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mCurrentLevel = 0;
    }

    private void caculatData() {
        caculateWaterLevel();
        caculateProgress();
    }

    private void caculateWaterLevel() {
        this.mAngle += TRANSLATION_ANGLR;
        float limit = (this.mHeight - this.mScanPointRadius) * (Utility.ALPHA_MAX - (((float) this.mCurrentLevel) / 100.0f));
        float rateWaterLever = this.mWaterHeight / (this.mHeight - this.mScanPointRadius);
        if (Float.compare(this.mWaterHeight, limit) > 0) {
            this.mWaterHeight -= (this.mInterpolator.getInterpolation(2.0f * rateWaterLever) * 25.0f) + this.mHeightOffset;
            if (this.mWaterHeight < 0.0f) {
                this.mWaterHeight = 0.0f;
            }
            this.mGettingTop = false;
            return;
        }
        this.mWaterHeight = limit;
        this.mGettingTop = true;
    }

    public boolean isTopLevel() {
        return this.mGettingTop;
    }

    private void caculateProgress() {
        long time = System.currentTimeMillis();
        if (this.mStartTime == -1) {
            this.mStartTime = time;
        }
        float temp = this.mInterpolator.getInterpolation((((float) (time - this.mStartTime)) % DRUATION) / DRUATION) * 360.0f;
        if (this.mBegin) {
            if (Float.compare(this.mRotation, 30.0f) <= 0) {
                this.mAlpha += 10;
            } else if (Float.compare(this.mRotation, 30.0f) > 0 && Float.compare(this.mRotation, 330.0f) <= 0) {
                this.mAlpha = 255;
                this.mBegin = false;
            }
        }
        if (this.mEnd) {
            if (Float.compare(this.mRotation, temp) >= 0) {
                this.mAlpha = 0;
                this.mEnd = false;
            } else if (Float.compare(this.mRotation, 330.0f) > 0 && Float.compare(this.mRotation, 360.0f) < 0) {
                this.mAlpha -= 10;
            }
        }
        this.mRotation = temp;
        if (this.mRotation >= 360.0f) {
            this.mRotation = 0.0f;
        }
    }
}
