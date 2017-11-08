package com.android.settings.applications;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class LinearColorBar extends LinearLayout {
    final int LEFT_COLOR = getResources().getColor(2131427496);
    final int MIDDLE_COLOR = getResources().getColor(2131427496);
    final Paint mColorGradientPaint = new Paint();
    final Path mColorPath = new Path();
    private int mColoredRegions = 7;
    final Paint mEdgeGradientPaint = new Paint();
    final Path mEdgePath = new Path();
    private float mGreenRatio;
    int mLastInterestingLeft;
    int mLastInterestingRight;
    int mLastLeftDiv;
    int mLastRegion;
    int mLastRightDiv;
    private int mLeftColor = this.LEFT_COLOR;
    int mLineWidth;
    private int mMiddleColor = this.MIDDLE_COLOR;
    private OnRegionTappedListener mOnRegionTappedListener;
    final Paint mPaint = new Paint();
    final Rect mRect = new Rect();
    private float mRedRatio;
    private int mRightColor = -3221541;
    private boolean mShowIndicator = true;
    private boolean mShowingGreen;
    private float mYellowRatio;

    public interface OnRegionTappedListener {
        void onRegionTapped(int i);
    }

    public LinearColorBar(Context context, AttributeSet attrs) {
        int i;
        super(context, attrs);
        setWillNotDraw(false);
        this.mPaint.setStyle(Style.FILL);
        this.mColorGradientPaint.setStyle(Style.FILL);
        this.mColorGradientPaint.setAntiAlias(true);
        this.mEdgeGradientPaint.setStyle(Style.STROKE);
        if (getResources().getDisplayMetrics().densityDpi >= 240) {
            i = 2;
        } else {
            i = 1;
        }
        this.mLineWidth = i;
        this.mEdgeGradientPaint.setStrokeWidth((float) this.mLineWidth);
        this.mEdgeGradientPaint.setAntiAlias(true);
    }

    public void setOnRegionTappedListener(OnRegionTappedListener listener) {
        if (listener != this.mOnRegionTappedListener) {
            this.mOnRegionTappedListener = listener;
            setClickable(listener != null);
        }
    }

    public void setColoredRegions(int regions) {
        this.mColoredRegions = regions;
        invalidate();
    }

    public void setRatios(float red, float yellow, float green) {
        this.mRedRatio = red;
        this.mYellowRatio = yellow;
        this.mGreenRatio = green;
        invalidate();
    }

    public void setColors(int red, int yellow, int green) {
        this.mLeftColor = red;
        this.mMiddleColor = yellow;
        this.mRightColor = green;
        updateIndicator();
        invalidate();
    }

    public void setShowIndicator(boolean showIndicator) {
        this.mShowIndicator = showIndicator;
        updateIndicator();
        invalidate();
    }

    private void updateIndicator() {
        int off = getPaddingTop() - getPaddingBottom();
        if (off < 0) {
            off = 0;
        }
        this.mRect.top = off;
        this.mRect.bottom = getHeight();
        if (this.mShowIndicator) {
            if (this.mShowingGreen) {
                this.mColorGradientPaint.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, (float) (off - 2), this.mRightColor & 16777215, this.mRightColor, TileMode.CLAMP));
            } else {
                this.mColorGradientPaint.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, (float) (off - 2), this.mMiddleColor & 16777215, this.mMiddleColor, TileMode.CLAMP));
            }
            this.mEdgeGradientPaint.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, (float) (off / 2), 10526880, -6250336, TileMode.CLAMP));
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateIndicator();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mOnRegionTappedListener != null) {
            switch (event.getAction()) {
                case 0:
                    int x = (int) event.getX();
                    if (x < this.mLastLeftDiv) {
                        this.mLastRegion = 1;
                    } else if (x < this.mLastRightDiv) {
                        this.mLastRegion = 2;
                    } else {
                        this.mLastRegion = 4;
                    }
                    invalidate();
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    protected void dispatchSetPressed(boolean pressed) {
        invalidate();
    }

    public boolean performClick() {
        if (!(this.mOnRegionTappedListener == null || this.mLastRegion == 0)) {
            this.mOnRegionTappedListener.onRegionTapped(this.mLastRegion);
            this.mLastRegion = 0;
        }
        return super.performClick();
    }

    private int pickColor(int color, int region) {
        if (isPressed() && (this.mLastRegion & region) != 0) {
            return -1;
        }
        if ((this.mColoredRegions & region) == 0) {
            return -11184811;
        }
        return color;
    }

    protected void onDraw(Canvas canvas) {
        int indicatorLeft;
        int indicatorRight;
        super.onDraw(canvas);
        int width = getWidth();
        int left = 0;
        boolean layoutRtl = isLayoutRtl();
        int rectWidth = width;
        int right = ((int) (((float) width) * this.mRedRatio)) + 0;
        int right2 = right + ((int) (((float) width) * this.mYellowRatio));
        int right3 = right2 + ((int) (((float) width) * this.mGreenRatio));
        if (this.mShowingGreen) {
            indicatorLeft = right2;
            indicatorRight = right3;
        } else {
            indicatorLeft = right;
            indicatorRight = right2;
        }
        if (!(this.mLastInterestingLeft == indicatorLeft && this.mLastInterestingRight == indicatorRight)) {
            this.mColorPath.reset();
            this.mEdgePath.reset();
            if (this.mShowIndicator && indicatorLeft < indicatorRight) {
                int midTopY = this.mRect.top;
                this.mColorPath.moveTo((float) indicatorLeft, (float) this.mRect.top);
                this.mColorPath.cubicTo((float) indicatorLeft, 0.0f, -2.0f, (float) midTopY, -2.0f, 0.0f);
                this.mColorPath.lineTo((float) ((width + 2) - 1), 0.0f);
                this.mColorPath.cubicTo((float) ((width + 2) - 1), (float) midTopY, (float) indicatorRight, 0.0f, (float) indicatorRight, (float) this.mRect.top);
                this.mColorPath.close();
                float lineOffset = ((float) this.mLineWidth) + 0.5f;
                this.mEdgePath.moveTo(-2.0f + lineOffset, 0.0f);
                this.mEdgePath.cubicTo(-2.0f + lineOffset, (float) midTopY, ((float) indicatorLeft) + lineOffset, 0.0f, ((float) indicatorLeft) + lineOffset, (float) this.mRect.top);
                this.mEdgePath.moveTo(((float) ((width + 2) - 1)) - lineOffset, 0.0f);
                this.mEdgePath.cubicTo(((float) ((width + 2) - 1)) - lineOffset, (float) midTopY, ((float) indicatorRight) - lineOffset, 0.0f, ((float) indicatorRight) - lineOffset, (float) this.mRect.top);
            }
            this.mLastInterestingLeft = indicatorLeft;
            this.mLastInterestingRight = indicatorRight;
        }
        if (!this.mEdgePath.isEmpty()) {
            canvas.drawPath(this.mEdgePath, this.mEdgeGradientPaint);
            canvas.drawPath(this.mColorPath, this.mColorGradientPaint);
        }
        if (right > 0) {
            this.mRect.left = 0;
            this.mRect.right = right;
            if (layoutRtl) {
                this.mRect.left = width - right;
                this.mRect.right = width + 0;
            }
            this.mPaint.setColor(pickColor(this.mLeftColor, 1));
            canvas.drawRect(this.mRect, this.mPaint);
            width -= right + 0;
            left = right;
        }
        this.mLastLeftDiv = right;
        this.mLastRightDiv = right2;
        right = right2;
        if (left < right2) {
            this.mRect.left = left;
            this.mRect.right = right2;
            if (layoutRtl) {
                this.mRect.left = rectWidth - right2;
                this.mRect.right = rectWidth - left;
            }
            this.mPaint.setColor(pickColor(this.mMiddleColor, 2));
            canvas.drawRect(this.mRect, this.mPaint);
            width -= right2 - left;
            left = right2;
        }
        right = left + width;
        if (left < right) {
            this.mRect.left = left;
            this.mRect.right = right;
            if (layoutRtl) {
                this.mRect.left = rectWidth - right;
                this.mRect.right = rectWidth - left;
            }
            this.mPaint.setColor(pickColor(this.mRightColor, 4));
            canvas.drawRect(this.mRect, this.mPaint);
        }
    }
}
