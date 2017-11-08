package com.huawei.keyguard.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.keyguard.R$interpolator;
import com.huawei.keyguard.support.HwSensorManager;
import com.huawei.keyguard.view.widget.GravityView;

public class SingleHandAdapter {
    private AnimatorSet mAnimatorSet;
    private Context mContext;
    private int mDirection = -1;
    private boolean mIsAnimPlaying;
    private boolean mIsSingleHand = true;
    private GravityView mKeyNumPadView;
    private View mLeftModeButton;
    private View mRightModeButton;
    private HwSensorManager mSensorManager;
    private UpdateSinglehandView mUpdateSinglehandView;
    private boolean mWindHasFocus = true;

    public interface UpdateSinglehandView {
    }

    public SingleHandAdapter(Context context, UpdateSinglehandView view) {
        this.mSensorManager = new HwSensorManager(context);
        this.mContext = context;
        this.mUpdateSinglehandView = view;
        this.mAnimatorSet = new AnimatorSet();
    }

    public void initAnimatorSet(float lAlpha, float rAlpha, float w, float h, float transX) {
        if (this.mAnimatorSet != null && !this.mIsAnimPlaying) {
            if (this.mLeftModeButton == null || this.mRightModeButton == null || this.mKeyNumPadView == null) {
                HwLog.w("SingleHandAdapter", "initAnimatorSet mLeftModeButton = " + this.mLeftModeButton + ", " + this.mRightModeButton + ", " + this.mKeyNumPadView);
                return;
            }
            ObjectAnimator left_anim = getAlphaAnimator(this.mLeftModeButton, this.mLeftModeButton.getAlpha(), lAlpha);
            ObjectAnimator right_anim = getAlphaAnimator(this.mRightModeButton, this.mRightModeButton.getAlpha(), rAlpha);
            ObjectAnimator pin_anim = this.mKeyNumPadView.getViewAnimator((float) this.mKeyNumPadView.getWidth(), w, (float) this.mKeyNumPadView.getHeight(), h, this.mKeyNumPadView.getTranslationX(), transX);
            this.mAnimatorSet.playTogether(new Animator[]{left_anim, right_anim, pin_anim});
            this.mAnimatorSet.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator arg0) {
                    super.onAnimationStart(arg0);
                    SingleHandAdapter.this.mIsAnimPlaying = true;
                }

                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (SingleHandAdapter.this.mIsSingleHand) {
                        SingleHandAdapter.this.mSensorManager.setMoveDirection(-1);
                    }
                    SingleHandAdapter.this.mIsAnimPlaying = false;
                }

                public void onAnimationCancel(Animator anim) {
                    super.onAnimationCancel(anim);
                    SingleHandAdapter.this.mIsAnimPlaying = false;
                }
            });
        }
    }

    public void setAnimatedViews(View lView, View rView, GravityView keyNumView) {
        this.mLeftModeButton = lView;
        this.mRightModeButton = rView;
        this.mKeyNumPadView = keyNumView;
    }

    public boolean startAnimatorUpdateView() {
        if (this.mAnimatorSet == null || this.mIsAnimPlaying) {
            return false;
        }
        this.mAnimatorSet.start();
        return true;
    }

    public void setModeDirection(int mode) {
        switch (mode) {
            case 1:
                this.mDirection = 1;
                break;
            case 2:
                this.mDirection = 2;
                break;
            default:
                this.mDirection = -1;
                break;
        }
        this.mSensorManager.setMoveDirection(this.mDirection);
    }

    private ObjectAnimator getAlphaAnimator(View view, float fromAlpha, float toAlpha) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", new float[]{fromAlpha, toAlpha});
        animator.setDuration(300);
        Interpolator interpolator = AnimationUtils.loadInterpolator(this.mContext, R$interpolator.scale_anim);
        if (interpolator != null) {
            animator.setInterpolator(interpolator);
        }
        return animator;
    }

    public void setWindowFocus(boolean focus) {
        this.mWindHasFocus = focus;
    }
}
