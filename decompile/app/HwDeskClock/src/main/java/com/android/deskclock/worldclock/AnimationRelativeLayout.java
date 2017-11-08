package com.android.deskclock.worldclock;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;
import com.android.deskclock.DigitalClock;
import com.android.deskclock.R;
import com.android.deskclock.stopwatch.AnaimationListView;
import com.android.deskclock.worldclock.WorldAnaimationListView.CustomTranslateAnimation;
import com.android.deskclock.worldclock.WorldAnaimationListView.SpeedDownAnimation;

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
        } else if (animation instanceof CustomTranslateAnimation) {
            ((CustomTranslateAnimation) animation).setView(this);
            animation.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    final float offset = ((CustomTranslateAnimation) animation).getmTranslateOffset();
                    AnimationRelativeLayout.this.post(new Runnable() {
                        public void run() {
                            if (((DigitalClock) AnimationRelativeLayout.this.findViewById(R.id.digital_clock)).getVisibility() == 0) {
                                AnimationRelativeLayout.this.setTranslationY(-offset);
                            }
                        }
                    });
                }
            });
        }
        super.setAnimation(animation);
    }

    protected void onDetachedFromWindow() {
        if (getAnimation() instanceof AnaimationListView.SpeedDownAnimation) {
            ((AnaimationListView.SpeedDownAnimation) getAnimation()).setTarget(null);
        }
        super.onDetachedFromWindow();
    }
}
