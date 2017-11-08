package com.android.huawei.coverscreen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;

public abstract class AbstractClockView extends View {
    protected Rect mBackgroundBoundsRect;
    protected int mBackgroundHeight;
    protected int mBackgroundWidth;
    protected int mBgHeight;
    protected int mBgWidth;
    protected Drawable mClockBackgroundDrawable;
    protected Rect mForgroundBoundsRect;
    protected int mForgroundHeight;
    protected int mForgroundWidth;
    protected Handler mHandler;
    protected boolean mIsSizeChange;
    protected int mPivotX;
    protected int mPivotY;
    protected float mScale;

    protected abstract long getMessageDelayMills();

    protected abstract void paintBackground(Canvas canvas);

    protected abstract void paintForground(Canvas canvas);

    protected abstract void setFgDrawable(int[] iArr);

    public AbstractClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbstractClockView(Context context, AttributeSet attrs, int defStyle) {
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
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        AbstractClockView.this.invalidate();
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    public void init(int bgDrawableId, int[] fgDrawableIdArray) {
        setBgDrawable(bgDrawableId);
        setFgDrawable(fgDrawableIdArray);
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

    private void sendRedrawMessage() {
        this.mHandler.sendEmptyMessageDelayed(1, getMessageDelayMills());
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mHandler.removeCallbacksAndMessages(null);
    }

    protected Drawable getDrawableById(int drawableId) {
        if (drawableId != 0) {
            return getContext().getResources().getDrawable(drawableId);
        }
        return null;
    }

    protected void setBgDrawable(int bgDrawableId) {
        if (bgDrawableId != 0) {
            this.mClockBackgroundDrawable = getDrawableById(bgDrawableId);
            this.mBackgroundWidth = this.mClockBackgroundDrawable.getIntrinsicWidth();
            this.mBackgroundHeight = this.mClockBackgroundDrawable.getIntrinsicHeight();
        }
    }
}
