package com.huawei.systemmanager.comm.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HwLog;

public class TipClickImage extends ImageView {
    private static final String TAG = "TipClickImage";
    private int alpha_max;
    private int alpha_pressed;
    private int drawableHeight;
    private int drawableWidth;
    private boolean mIsTipState;
    private Drawable mTipDrawable;

    public TipClickImage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TipClickImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mIsTipState = false;
        this.drawableWidth = 0;
        this.drawableHeight = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Tip_Image_View, defStyle, 0);
        this.mTipDrawable = a.getDrawable(0);
        if (this.mTipDrawable != null) {
            this.drawableWidth = this.mTipDrawable.getIntrinsicWidth();
            this.drawableHeight = this.mTipDrawable.getIntrinsicHeight();
        }
        this.alpha_max = a.getInteger(1, 255);
        this.alpha_pressed = a.getInteger(2, (int) (((double) this.alpha_max) * 0.5d));
        a.recycle();
    }

    public void setTipState(boolean enable) {
        this.mIsTipState = enable;
        if (!this.mIsTipState) {
            setPadding(0, 0, 0, 0);
        } else if (getLayoutDirection() == 1) {
            setPadding(this.drawableWidth, 0, 0, 0);
        } else {
            setPadding(0, 0, this.drawableWidth, 0);
        }
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mIsTipState) {
            drawTip(canvas, this.mTipDrawable);
        }
    }

    private void drawTip(Canvas canvas, Drawable drawable) {
        if (drawable != null) {
            int left;
            drawable.setAlpha(255);
            if (getLayoutDirection() == 1) {
                left = 0;
            } else {
                left = getWidth() - getPaddingRight();
            }
            drawable.setBounds(left, 0, this.drawableWidth + left, this.drawableHeight);
            drawable.draw(canvas);
        }
    }

    protected void dispatchSetPressed(boolean pressed) {
        Drawable d = getDrawable();
        if (d == null) {
            HwLog.e(TAG, "no drawable");
            return;
        }
        int i;
        if (pressed) {
            i = this.alpha_pressed;
        } else {
            i = this.alpha_max;
        }
        d.setAlpha(i);
    }
}
