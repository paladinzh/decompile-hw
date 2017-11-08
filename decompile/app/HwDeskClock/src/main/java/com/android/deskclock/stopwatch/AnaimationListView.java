package com.android.deskclock.stopwatch;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ListView;

public class AnaimationListView extends ListView {

    public static class SpeedDownAnimation extends Animation {
        private View mTarget;

        public void setTarget(View view) {
            this.mTarget = view;
        }

        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (this.mTarget != null) {
                if (this.mTarget.getLayoutParams() == null || this.mTarget.getLayoutParams().layoutAnimationParameters == null || this.mTarget.getLayoutParams().layoutAnimationParameters.index != 0) {
                    float mFromYDelta = (float) (-this.mTarget.getHeight());
                    float dy = mFromYDelta;
                    if (mFromYDelta != 0.0f) {
                        dy = mFromYDelta + ((0.0f - mFromYDelta) * interpolatedTime);
                    }
                    t.getMatrix().setTranslate(0.0f, dy);
                } else {
                    t.setAlpha(0.0f);
                }
            }
        }

        protected SpeedDownAnimation clone() throws CloneNotSupportedException {
            SpeedDownAnimation result = (SpeedDownAnimation) super.clone();
            result.setTarget(null);
            return result;
        }
    }

    public AnaimationListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AnaimationListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnaimationListView(Context context) {
        super(context);
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    protected void attachLayoutAnimationParameters(View child, LayoutParams params, int index, int count) {
        super.attachLayoutAnimationParameters(child, params, index, count);
    }
}
