package com.android.deskclock.worldclock;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;

public class WorldAnaimationListView extends ListView {

    public static class CustomTranslateAnimation extends TranslateAnimation {
        private View mTargetView;
        public final float mTranslateOffset;

        public float getmTranslateOffset() {
            return this.mTranslateOffset;
        }

        public CustomTranslateAnimation(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta, int offset) {
            super(fromXDelta, toXDelta, fromYDelta, toYDelta);
            this.mTranslateOffset = (float) offset;
        }

        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (this.mTargetView != null) {
                super.applyTransformation(interpolatedTime, t);
            }
        }

        public void setView(View view) {
            this.mTargetView = view;
        }
    }

    public static class SpeedDownAnimation extends Animation {
        private View mTarget;

        public void setTarget(View view) {
            this.mTarget = view;
        }

        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (this.mTarget != null) {
                float mFromYDelta = ((float) (-getScreenHeight())) / 6.0f;
                float dy = mFromYDelta;
                if (mFromYDelta != 0.0f) {
                    dy = mFromYDelta + ((0.0f - mFromYDelta) * interpolatedTime);
                }
                t.getMatrix().setTranslate(0.0f, dy);
            }
        }

        private int getScreenHeight() {
            if (this.mTarget == null) {
                return 5760;
            }
            return ((WindowManager) this.mTarget.getContext().getSystemService("window")).getDefaultDisplay().getHeight();
        }

        protected SpeedDownAnimation clone() throws CloneNotSupportedException {
            SpeedDownAnimation result = (SpeedDownAnimation) super.clone();
            result.setTarget(null);
            return result;
        }
    }

    public WorldAnaimationListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WorldAnaimationListView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
