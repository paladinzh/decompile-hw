package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.android.systemui.Interpolators;

public class SettingsButton extends ImageView {
    private ObjectAnimator mAnimator;
    private final Runnable mLongPressCallback = new Runnable() {
        public void run() {
            SettingsButton.this.startAccelSpin();
        }
    };
    private float mSlop = ((float) ViewConfiguration.get(getContext()).getScaledTouchSlop());
    private boolean mUpToSpeed;

    public SettingsButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isAnimating() {
        return this.mAnimator != null ? this.mAnimator.isRunning() : false;
    }

    public boolean isTunerClick() {
        return this.mUpToSpeed;
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case 0:
                postDelayed(this.mLongPressCallback, 1000);
                break;
            case 1:
                if (!this.mUpToSpeed) {
                    cancelLongClick();
                    break;
                }
                startExitAnimation();
                break;
            case 2:
                float x = event.getX();
                float y = event.getY();
                if (x >= (-this.mSlop) && y >= (-this.mSlop) && x <= ((float) getWidth()) + this.mSlop) {
                    if (y > ((float) getHeight()) + this.mSlop) {
                    }
                }
                cancelLongClick();
                break;
            case 3:
                cancelLongClick();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void cancelLongClick() {
        cancelAnimation();
        this.mUpToSpeed = false;
        removeCallbacks(this.mLongPressCallback);
    }

    private void cancelAnimation() {
        if (this.mAnimator != null) {
            this.mAnimator.removeAllListeners();
            this.mAnimator.cancel();
            this.mAnimator = null;
        }
    }

    private void startExitAnimation() {
        animate().translationX(((float) ((View) getParent().getParent()).getWidth()) - getX()).alpha(0.0f).setDuration(350).setInterpolator(AnimationUtils.loadInterpolator(getContext(), 17563650)).setListener(new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                SettingsButton.this.setAlpha(1.0f);
                SettingsButton.this.setTranslationX(0.0f);
                SettingsButton.this.cancelLongClick();
            }

            public void onAnimationCancel(Animator animation) {
            }
        }).start();
    }

    protected void startAccelSpin() {
        cancelAnimation();
        this.mAnimator = ObjectAnimator.ofFloat(this, View.ROTATION, new float[]{0.0f, 360.0f});
        this.mAnimator.setInterpolator(AnimationUtils.loadInterpolator(getContext(), 17563648));
        this.mAnimator.setDuration(750);
        this.mAnimator.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                SettingsButton.this.startContinuousSpin();
            }

            public void onAnimationCancel(Animator animation) {
            }
        });
        this.mAnimator.start();
    }

    protected void startContinuousSpin() {
        cancelAnimation();
        performHapticFeedback(0);
        this.mUpToSpeed = true;
        this.mAnimator = ObjectAnimator.ofFloat(this, View.ROTATION, new float[]{0.0f, 360.0f});
        this.mAnimator.setInterpolator(Interpolators.LINEAR);
        this.mAnimator.setDuration(375);
        this.mAnimator.setRepeatCount(-1);
        this.mAnimator.start();
    }
}
