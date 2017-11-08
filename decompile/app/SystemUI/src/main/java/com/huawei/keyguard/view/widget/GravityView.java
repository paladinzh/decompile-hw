package com.huawei.keyguard.view.widget;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import com.android.keyguard.R$interpolator;

public class GravityView extends LinearLayout {
    private LayoutParams mParams = new LayoutParams(-2, -2);

    public GravityView(Context context) {
        super(context);
    }

    public GravityView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GravityView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ObjectAnimator getViewAnimator(float fromW, float toW, float fromH, float toH, float fromX, float toX) {
        final float deltaW = toW - fromW;
        final float deltaH = toH - fromH;
        PropertyValuesHolder pvhTransitionX = PropertyValuesHolder.ofFloat("translationX", new float[]{fromX, toX});
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(this, new PropertyValuesHolder[]{pvhTransitionX});
        Interpolator interpolator = AnimationUtils.loadInterpolator(getContext(), R$interpolator.scale_anim);
        if (interpolator != null) {
            anim.setInterpolator(interpolator);
        }
        anim.setDuration(400);
        final float f = fromW;
        final float f2 = fromH;
        anim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                GravityView.this.mParams.width = (int) (f + (deltaW * fraction));
                GravityView.this.mParams.height = (int) (f2 + (deltaH * fraction));
                GravityView.this.setLayoutParams(GravityView.this.mParams);
            }
        });
        return anim;
    }
}
