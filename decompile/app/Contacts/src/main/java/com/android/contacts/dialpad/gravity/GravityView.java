package com.android.contacts.dialpad.gravity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class GravityView extends LinearLayout {
    private float mFromH;
    private float mFromW;
    private AnimatorListener mListener;
    private LayoutParams mParams = new LayoutParams(-2, -2);
    private FrameLayout mParentView;
    private float mToH;
    private float mToW;

    protected interface AnimatorListener {
        void onAnimatorEnd(ObjectAnimator objectAnimator);

        void onAnimatorStart(ObjectAnimator objectAnimator);
    }

    public GravityView(Context context) {
        super(context);
    }

    public GravityView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GravityView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAnimatorListener(AnimatorListener listener) {
        this.mListener = listener;
    }

    public ObjectAnimator getViewAnimator(float fromW, float toW, float fromH, float toH, float fromX, float toX, final boolean updateParent) {
        this.mFromW = fromW;
        this.mFromH = fromH;
        this.mToW = toW;
        this.mToH = toH;
        final float deltaW = this.mToW - this.mFromW;
        final float deltaH = this.mToH - this.mFromH;
        if (HwLog.HWDBG) {
            HwLog.d("GravityView", "translationX fromX:" + fromX + " toX:" + toX);
        }
        PropertyValuesHolder pvhTransitionX = PropertyValuesHolder.ofFloat("translationX", new float[]{fromX, toX});
        final ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(this, new PropertyValuesHolder[]{pvhTransitionX});
        Interpolator interpolator = AnimationUtils.loadInterpolator(getContext(), R.interpolator.scale_anim);
        if (interpolator != null) {
            anim.setInterpolator(interpolator);
        }
        anim.setDuration(360);
        anim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                GravityView.this.mParams.width = (int) (GravityView.this.mFromW + (deltaW * fraction));
                GravityView.this.mParams.height = (int) (GravityView.this.mFromH + (deltaH * fraction));
                GravityView.this.mParams.gravity = 80;
                GravityView.this.setLayoutParams(GravityView.this.mParams);
                if (updateParent && GravityView.this.mParentView != null && (GravityView.this.mParentView.getLayoutParams() instanceof LayoutParams)) {
                    LayoutParams param = (LayoutParams) GravityView.this.mParentView.getLayoutParams();
                    param.height = (int) (GravityView.this.mFromH + (deltaH * fraction));
                    param.gravity = 80;
                    GravityView.this.mParentView.setLayoutParams(param);
                }
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                if (GravityView.this.mListener != null) {
                    GravityView.this.mListener.onAnimatorStart(anim);
                }
            }

            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ObjectAnimator lAni = null;
                if (animation instanceof ObjectAnimator) {
                    lAni = (ObjectAnimator) animation;
                }
                GravityView.this.processEndLayout(updateParent, lAni);
            }
        });
        return anim;
    }

    protected void processEndLayout(boolean updateParent, ObjectAnimator animation) {
        if (HwLog.HWDBG) {
            HwLog.d("GravityView", "onAnimationEnd mToW:" + this.mToW);
        }
        this.mParams.width = (int) this.mToW;
        this.mParams.height = (int) this.mToH;
        this.mParams.gravity = 80;
        setLayoutParams(this.mParams);
        if (updateParent && this.mParentView != null && (this.mParentView.getLayoutParams() instanceof LayoutParams)) {
            LayoutParams param = (LayoutParams) this.mParentView.getLayoutParams();
            param.width = -1;
            param.height = (int) this.mToH;
            param.gravity = 80;
            this.mParentView.setLayoutParams(param);
        }
        if (this.mListener != null) {
            this.mListener.onAnimatorEnd(animation);
        }
    }

    public void setParentView(FrameLayout parent) {
        this.mParentView = parent;
    }
}
