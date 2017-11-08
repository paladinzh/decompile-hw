package com.android.deskclock.timer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import com.android.deskclock.R;

public class TimerDragRing extends View {
    private Drawable mAnimatePoint;
    private int mAnimatePointHeight;
    private int mAnimatePointWidth;
    private boolean mAttached;
    private boolean mChanged;
    private int mDialHeight;
    private int mDialWidth;
    private PaintFlagsDrawFilter mDrawFilter;
    private boolean mIsPressed;
    private Rect mMildRect;
    private Paint mPaint;
    private RectF mRectF;
    private Drawable mRgulator;
    private int mRgulatorHeight;
    private int mRgulatorWidth;
    private Rect mSmallRect;
    private float mStartAngle;
    private float mStartAngleSmall;
    private float mSweepAngel;
    private float mSweepAngelSmall;
    private float scale;
    private boolean scaled;
    private int x;
    private int y;

    public TimerDragRing(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerDragRing(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.scale = 1.0f;
        this.x = 0;
        this.y = 0;
        this.scaled = false;
        this.mStartAngleSmall = 10.0f;
        this.mSweepAngelSmall = 340.0f;
        this.mStartAngle = 10.0f;
        this.mSweepAngel = 340.0f;
        this.mIsPressed = false;
        Drawable discal = getResources().getDrawable(R.drawable.time_circle);
        this.mRgulator = getResources().getDrawable(R.drawable.ic_regulator);
        this.mAnimatePoint = getResources().getDrawable(R.drawable.ic_animate_point);
        this.mDialWidth = discal.getIntrinsicWidth();
        this.mDialHeight = discal.getIntrinsicHeight();
        this.mRgulatorWidth = this.mRgulator.getIntrinsicWidth();
        this.mRgulatorHeight = this.mRgulator.getIntrinsicHeight();
        this.mAnimatePointWidth = this.mAnimatePoint.getIntrinsicWidth();
        this.mAnimatePointHeight = this.mAnimatePoint.getIntrinsicHeight();
        this.mPaint = new Paint();
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setFilterBitmap(true);
        this.mPaint.setDither(true);
        this.mPaint.setColor(-1);
        this.mPaint.setAlpha(102);
        this.mPaint.setStrokeWidth(getResources().getDisplayMetrics().density * 2.0f);
        this.mDrawFilter = new PaintFlagsDrawFilter(0, 2);
        this.mRectF = new RectF();
        this.mMildRect = new Rect();
        this.mSmallRect = new Rect();
        calculateAngle();
        calculateAngleSmall();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mAttached) {
            this.mAttached = true;
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mAttached) {
            this.mAttached = false;
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        float hScale = 1.0f;
        float vScale = 1.0f;
        if (widthMode != 0 && widthSize < this.mDialWidth + this.mRgulatorWidth) {
            hScale = ((float) widthSize) / ((float) (this.mDialWidth + this.mRgulatorWidth));
        }
        if (heightMode != 0 && heightSize < this.mDialHeight + this.mRgulatorHeight) {
            vScale = ((float) heightSize) / ((float) (this.mDialHeight + this.mRgulatorHeight));
        }
        this.scale = Math.min(hScale, vScale);
        setMeasuredDimension(resolveSizeAndState((int) (((float) (this.mDialWidth + this.mRgulatorWidth)) * this.scale), widthMeasureSpec, 0), resolveSizeAndState((int) (((float) (this.mDialHeight + this.mRgulatorHeight)) * this.scale), heightMeasureSpec, 0));
        calculateAngle();
        calculateAngleSmall();
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int availableWidth = this.mRight - this.mLeft;
        int availableHeight = this.mBottom - this.mTop;
        this.x = availableWidth >> 1;
        this.y = availableHeight >> 1;
        if (availableWidth < this.mDialWidth + this.mRgulatorWidth || availableHeight < this.mDialHeight + this.mRgulatorHeight) {
            this.scaled = true;
            this.scale = Math.min(((float) availableWidth) / ((float) (this.mDialWidth + this.mRgulatorWidth)), ((float) availableHeight) / ((float) (this.mDialHeight + this.mRgulatorHeight)));
        }
        this.mRectF.set((float) (this.x - (this.mDialWidth >> 1)), (float) (this.y - (this.mDialHeight >> 1)), (float) (this.x + (this.mDialWidth >> 1)), (float) (this.y + (this.mDialHeight >> 1)));
        this.mMildRect.set(this.x - (this.mRgulatorWidth >> 1), (this.y - (this.mDialHeight >> 1)) - (this.mRgulatorHeight >> 1), this.x + (this.mRgulatorWidth >> 1), (this.y - (this.mDialHeight >> 1)) + (this.mRgulatorHeight >> 1));
        this.mSmallRect.set(this.x - (this.mAnimatePointWidth >> 1), (this.y - (this.mDialHeight >> 1)) - (this.mAnimatePointHeight >> 1), this.x + (this.mAnimatePointWidth >> 1), (this.y - (this.mDialHeight >> 1)) + (this.mAnimatePointHeight >> 1));
        calculateAngle();
        calculateAngleSmall();
        this.mChanged = true;
    }

    private void calculateAngleSmall() {
        this.mStartAngleSmall = (float) ((Math.atan((double) ((((float) this.mAnimatePointWidth) / 2.0f) / (((float) this.mDialWidth) / 2.0f))) * 180.0d) / 3.141592653589793d);
        this.mSweepAngelSmall = 360.0f - (this.mStartAngleSmall * 2.0f);
        this.mStartAngleSmall += 270.0f;
    }

    private void calculateAngle() {
        this.mStartAngle = (float) ((Math.atan((double) ((((float) this.mRgulatorWidth) / 2.0f) / (((float) this.mDialWidth) / 2.0f))) * 180.0d) / 3.141592653589793d);
        this.mSweepAngel = 360.0f - (this.mStartAngle * 2.0f);
        this.mStartAngle += 270.0f;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(this.mDrawFilter);
        if (this.scaled) {
            canvas.save();
            canvas.scale(this.scale, this.scale, (float) this.x, (float) this.y);
        }
        if (this.mIsPressed) {
            canvas.save();
            canvas.drawArc(this.mRectF, this.mStartAngle, this.mSweepAngel, false, this.mPaint);
            this.mRgulator.setBounds(this.mMildRect);
            this.mRgulator.draw(canvas);
            canvas.restore();
        } else {
            canvas.save();
            canvas.drawArc(this.mRectF, this.mStartAngleSmall, this.mSweepAngelSmall, false, this.mPaint);
            this.mAnimatePoint.setBounds(this.mSmallRect);
            this.mAnimatePoint.draw(canvas);
            canvas.restore();
        }
        if (this.scaled) {
            canvas.restore();
        }
        if (this.mChanged) {
            this.mChanged = false;
        }
    }

    public void setPressed(boolean pressed) {
        this.mIsPressed = pressed;
        invalidate();
    }
}
