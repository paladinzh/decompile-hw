package com.android.deskclock.stopwatch;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import com.android.deskclock.stopwatch.AnaimationListView.SpeedDownAnimation;

public class AnimationRelativeLayout extends RelativeLayout {
    public AnimationRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AnimationRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimationRelativeLayout(Context context) {
        super(context);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void setAnimation(Animation animation) {
        if (animation instanceof SpeedDownAnimation) {
            ((SpeedDownAnimation) animation).setTarget(this);
        }
        super.setAnimation(animation);
    }

    protected void onDetachedFromWindow() {
        if (getAnimation() instanceof SpeedDownAnimation) {
            ((SpeedDownAnimation) getAnimation()).setTarget(null);
        }
        super.onDetachedFromWindow();
    }
}
