package com.android.systemui.recents.views;

import android.animation.TimeAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.systemui.R;
import com.android.systemui.R$styleable;
import com.android.systemui.utils.SystemUiUtil;

public class HwRoundProgressBar extends View {
    TimeAnimator mAni;
    private boolean mDrawPoint;
    private int mMax;
    RectF mOval;
    private Paint mPaint;
    Interpolator mPointInterpolator;
    private Bitmap mPointerBitmap;
    private int mProgress;
    private int mRoundColor;
    private int mRoundProgressColor;
    private float mRoundWidth;
    long mStartTime;
    boolean mStarted;
    private int mStyle;
    private int mTextColor;
    private boolean mTextIsDisplayable;
    private float mTextSize;

    public HwRoundProgressBar(Context context) {
        this(context, null);
    }

    public HwRoundProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwRoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mPointerBitmap = null;
        this.mStarted = false;
        this.mStartTime = -1;
        this.mAni = new TimeAnimator();
        this.mDrawPoint = true;
        this.mPaint = new Paint();
        this.mOval = new RectF();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R$styleable.RoundProgressBar);
        this.mRoundColor = typedArray.getColor(1, 0);
        this.mRoundProgressColor = typedArray.getColor(2, -1);
        this.mTextColor = typedArray.getColor(0, -1);
        this.mTextSize = typedArray.getDimension(4, 15.0f);
        this.mRoundWidth = typedArray.getDimension(3, 5.0f);
        this.mMax = typedArray.getInteger(5, 100);
        this.mTextIsDisplayable = typedArray.getBoolean(6, true);
        this.mStyle = typedArray.getInt(7, 0);
        typedArray.recycle();
        try {
            this.mPointInterpolator = AnimationUtils.loadInterpolator(context, R.anim.mainscreen_interpolator);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDetachedFromWindow() {
        this.mStarted = false;
        super.onDetachedFromWindow();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mDrawPoint) {
            drawPoint(canvas);
            return;
        }
        int centre = getWidth() / 2;
        int radius = (int) (((float) centre) - (this.mRoundWidth / 2.0f));
        drawCircle(canvas, centre, radius);
        drawProgressPercent(canvas, centre);
        this.mPaint.setStrokeWidth(this.mRoundWidth);
        this.mPaint.setColor(this.mRoundProgressColor);
        SystemUiUtil.generateRectF((float) (centre - radius), (float) (centre - radius), (float) (centre + radius), (float) (centre + radius), this.mOval);
        switch (this.mStyle) {
            case 0:
                this.mPaint.setStyle(Style.STROKE);
                canvas.drawArc(this.mOval, 270.0f, ((float) (this.mProgress * 360)) / ((float) this.mMax), false, this.mPaint);
                if (this.mPointerBitmap != null) {
                    double round = Math.toRadians((((double) this.mProgress) * 360.0d) / ((double) this.mMax));
                    canvas.drawBitmap(this.mPointerBitmap, (float) (((float) this.mProgress) / ((float) this.mMax) >= 0.5f ? Math.sin(round) * ((double) centre) : (Math.sin(round) * ((double) ((float) centre))) + ((double) ((float) centre))), (float) (((float) this.mProgress) / ((float) this.mMax) >= 0.5f ? Math.cos(round) * ((double) centre) : (Math.cos(round) * ((double) ((float) centre))) + ((double) ((float) centre))), null);
                    break;
                }
                break;
            case 1:
                this.mPaint.setStyle(Style.FILL_AND_STROKE);
                if (this.mProgress != 0) {
                    canvas.drawArc(this.mOval, 270.0f, ((float) (this.mProgress * 360)) / ((float) this.mMax), true, this.mPaint);
                    break;
                }
                break;
        }
    }

    private void drawCircle(Canvas canvas, int centre, int radius) {
        this.mPaint.setColor(this.mRoundColor);
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setStrokeWidth(this.mRoundWidth);
        this.mPaint.setAntiAlias(true);
        canvas.drawCircle((float) centre, (float) centre, (float) radius, this.mPaint);
    }

    private void drawProgressPercent(Canvas canvas, int centre) {
        this.mPaint.setStrokeWidth(0.0f);
        this.mPaint.setColor(this.mTextColor);
        this.mPaint.setTextSize(this.mTextSize);
        int percent = (int) ((((float) this.mProgress) / ((float) this.mMax)) * 100.0f);
        float textWidth = this.mPaint.measureText(percent + "%");
        if (this.mTextIsDisplayable && percent != 0 && this.mStyle == 0) {
            canvas.drawText(percent + "%", ((float) centre) - (textWidth / 2.0f), ((float) centre) + (this.mTextSize / 2.0f), this.mPaint);
        }
    }

    private void drawPoint(Canvas canvas) {
        if (this.mStarted) {
            this.mPaint.setColor(-1);
            this.mPaint.setAntiAlias(true);
            canvas.save();
            canvas.rotate(this.mPointInterpolator.getInterpolation((((float) getStartOffset()) % 800.0f) / 800.0f) * 360.0f, (float) getCenterX(), (float) getCenterY());
            canvas.drawCircle((float) getCenterY(), 6.0f, 6.0f, this.mPaint);
            canvas.restore();
        }
    }

    private int getCenterX() {
        return getWidth() / 2;
    }

    private int getCenterY() {
        return getHeight() / 2;
    }

    public long getStartOffset() {
        return System.currentTimeMillis() - this.mStartTime;
    }
}
