package com.android.deskclock.smartcover;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.widget.TextView;

public class SmartCoverAnimateTextView extends TextView {
    private Matrix mGradientMatrix;
    private LinearGradient mLinearGradient;
    private Paint mPaint;
    private boolean mStart = false;
    private int mTranslate = 0;
    private int mViewWidth = 0;

    public SmartCoverAnimateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (this.mViewWidth == 0) {
            this.mViewWidth = getMeasuredWidth();
            if (this.mViewWidth > 0) {
                this.mPaint = getPaint();
                this.mLinearGradient = new LinearGradient((float) (-this.mViewWidth), 0.0f, 0.0f, 0.0f, new int[]{2013265919, -1, 2013265919}, new float[]{0.2f, 0.5f, 1.0f}, TileMode.CLAMP);
                this.mPaint.setShader(this.mLinearGradient);
                this.mGradientMatrix = new Matrix();
            }
        }
    }

    public void startAnimator() {
        this.mStart = true;
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mStart && this.mGradientMatrix != null) {
            this.mTranslate += this.mViewWidth / 8;
            if (this.mTranslate > this.mViewWidth * 2) {
                this.mTranslate = 0;
            }
            this.mGradientMatrix.setTranslate((float) this.mTranslate, 0.0f);
            if (this.mLinearGradient != null) {
                this.mLinearGradient.setLocalMatrix(this.mGradientMatrix);
            }
        }
        if (this.mTranslate == 0) {
            this.mStart = false;
        } else {
            postInvalidateDelayed(60);
        }
    }
}
