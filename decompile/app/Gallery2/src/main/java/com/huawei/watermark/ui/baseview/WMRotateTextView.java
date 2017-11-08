package com.huawei.watermark.ui.baseview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class WMRotateTextView extends TextView implements WMRotatable {
    private long mAnimationEndTime = 0;
    private long mAnimationStartTime = 0;
    private boolean mClockwise = false;
    private int mCurrentDegree = 0;
    private int mStartDegree = 0;
    private int mTargetDegree = 0;

    public WMRotateTextView(Context paramContext) {
        super(paramContext);
    }

    public WMRotateTextView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
    }

    public WMRotateTextView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
    }

    protected void onDraw(Canvas canvas) {
        if (this.mCurrentDegree != this.mTargetDegree) {
            long time = AnimationUtils.currentAnimationTimeMillis();
            if (time < this.mAnimationEndTime) {
                int deltaTime = (int) (time - this.mAnimationStartTime);
                int i = this.mStartDegree;
                if (!this.mClockwise) {
                    deltaTime = -deltaTime;
                }
                int degree = i + ((deltaTime * 180) / 1000);
                if (degree >= 0) {
                    this.mCurrentDegree = degree % 360;
                } else {
                    this.mCurrentDegree = (degree % 360) + 360;
                }
                invalidate();
            } else {
                this.mCurrentDegree = this.mTargetDegree;
            }
        }
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int width = (getWidth() - left) - right;
        int height = (getHeight() - top) - getPaddingBottom();
        canvas.translate((float) ((width / 2) + left), (float) ((height / 2) + top));
        canvas.rotate((float) (-this.mCurrentDegree));
        canvas.translate(((float) (-width)) / 2.0f, ((float) (-height)) / 2.0f);
        super.onDraw(canvas);
    }

    public void setOrientation(int orientation, boolean animation) {
        boolean z = false;
        if (orientation >= 0) {
            orientation %= 360;
        } else {
            orientation = (orientation % 360) + 360;
        }
        if (orientation != this.mTargetDegree) {
            this.mTargetDegree = orientation;
            this.mStartDegree = this.mCurrentDegree;
            this.mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();
            int diff = this.mTargetDegree - this.mCurrentDegree;
            if (diff <= 0) {
                diff += 360;
            }
            if (diff > 180) {
                diff -= 360;
            }
            if (diff >= 0) {
                z = true;
            }
            this.mClockwise = z;
            this.mAnimationEndTime = this.mAnimationStartTime + ((long) ((Math.abs(diff) * 1000) / 180));
            invalidate();
        }
    }
}
