package com.huawei.keyguard.cover.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.cover.AnalogClockResourceUtils;
import com.huawei.keyguard.util.HwLog;

@SuppressLint({"NewApi"})
public abstract class AbstractAnalogClockView extends View {
    protected Rect mBackgroundBoundsRect;
    protected int mBackgroundHeight;
    protected int mBackgroundWidth;
    protected int mBgHeight;
    protected int mBgWidth;
    protected Drawable mClockBackgroundDrawable;
    protected Rect mForgroundBoundsRect;
    protected int mForgroundHeight;
    protected int mForgroundWidth;
    private final Handler mHandler;
    protected boolean mIsSizeChange;
    protected int mPivotX;
    protected int mPivotY;
    protected float mScale;
    private Runnable mUpdater;

    protected abstract long getMessageDelayMills();

    protected abstract void paintBackground(Canvas canvas);

    protected abstract void paintForground(Canvas canvas);

    protected abstract void setAnalogClockDrawable(Drawable[] drawableArr);

    public AbstractAnalogClockView(Context context) {
        this(context, null);
    }

    public AbstractAnalogClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbstractAnalogClockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mForgroundWidth = 0;
        this.mForgroundHeight = 0;
        this.mBackgroundWidth = 0;
        this.mBackgroundHeight = 0;
        this.mBgWidth = 0;
        this.mBgHeight = 0;
        this.mScale = 1.0f;
        this.mIsSizeChange = true;
        this.mPivotX = 0;
        this.mPivotY = 0;
        this.mHandler = GlobalContext.getUIHandler();
        this.mUpdater = new Runnable() {
            public void run() {
                AbstractAnalogClockView.this.invalidate();
            }
        };
        init();
    }

    public void init() {
        Drawable[] drawableArray = AnalogClockResourceUtils.getAnalogClockDrawable();
        Drawable bgDrawable = AnalogClockResourceUtils.getAnalogClockBg();
        if (!(drawableArray[0] == null || drawableArray[1] == null || drawableArray[3] == null || drawableArray[4] == null || drawableArray[2] == null || drawableArray[5] == null)) {
            if (bgDrawable == null) {
            }
            setBgDrawable(bgDrawable);
            setAnalogClockDrawable(drawableArray);
            this.mBackgroundBoundsRect = new Rect();
            this.mForgroundBoundsRect = new Rect();
        }
        HwLog.d("AbstractAnalogClockView", "Load analog clock drawable resource default.");
        bgDrawable = AnalogClockResourceUtils.getAnalogClockBgFromResource(getContext());
        drawableArray = AnalogClockResourceUtils.getAnalogClockDrawableFromResource(getContext());
        setBgDrawable(bgDrawable);
        setAnalogClockDrawable(drawableArray);
        this.mBackgroundBoundsRect = new Rect();
        this.mForgroundBoundsRect = new Rect();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (this.mBgWidth == 0) {
            this.mBgWidth = widthSize;
            this.mBgHeight = heightSize;
            this.mScale = 1.0f;
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        float hScale = 1.0f;
        float vScale = 1.0f;
        if (widthMode != 0) {
            hScale = ((float) widthSize) / ((float) this.mBgWidth);
        }
        if (heightMode != 0) {
            vScale = ((float) heightSize) / ((float) this.mBgHeight);
        }
        this.mScale = Math.min(hScale, vScale);
        setMeasuredDimension(resolveSizeAndState((int) (((float) this.mBgWidth) * this.mScale), widthMeasureSpec, 0), resolveSizeAndState((int) (((float) this.mBgHeight) * this.mScale), heightMeasureSpec, 0));
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int availableWidth = getRight() - getLeft();
        int availableHeight = getBottom() - getTop();
        this.mPivotX = availableWidth / 2;
        this.mPivotY = availableHeight / 2;
        this.mScale = Math.min(((float) availableWidth) / ((float) this.mBgWidth), ((float) availableHeight) / ((float) this.mBgHeight));
        if (!(this.mBackgroundWidth == 0 || this.mBackgroundHeight == 0)) {
            this.mBackgroundBoundsRect.set(this.mPivotX - (this.mBackgroundWidth / 2), this.mPivotY - (this.mBackgroundHeight / 2), this.mPivotX + (this.mBackgroundWidth / 2), this.mPivotY + (this.mBackgroundHeight / 2));
        }
        this.mIsSizeChange = true;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.scale(this.mScale, this.mScale, (float) this.mPivotX, (float) this.mPivotY);
        paintBackground(canvas);
        paintForground(canvas);
        if (this.mIsSizeChange) {
            this.mIsSizeChange = false;
        }
        sendRedrawMessage();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    private void sendRedrawMessage() {
        this.mHandler.postDelayed(this.mUpdater, getMessageDelayMills());
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mHandler.removeCallbacksAndMessages(null);
    }

    protected void setBgDrawable(Drawable bgDrawable) {
        if (bgDrawable != null) {
            this.mClockBackgroundDrawable = bgDrawable;
            this.mBackgroundWidth = this.mClockBackgroundDrawable.getIntrinsicWidth();
            this.mBackgroundHeight = this.mClockBackgroundDrawable.getIntrinsicHeight();
        }
    }
}
