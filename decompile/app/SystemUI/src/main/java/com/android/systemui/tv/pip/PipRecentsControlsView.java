package com.android.systemui.tv.pip;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.android.systemui.R;

public class PipRecentsControlsView extends FrameLayout {
    private Animator mFocusGainAnimator;
    private AnimatorSet mFocusLossAnimatorSet;
    private PipControlsView mPipControlsView;
    private final PipManager mPipManager;
    private View mScrim;

    public interface Listener extends com.android.systemui.tv.pip.PipControlsView.Listener {
        void onBackPressed();
    }

    public PipRecentsControlsView(Context context) {
        this(context, null, 0, 0);
    }

    public PipRecentsControlsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public PipRecentsControlsView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PipRecentsControlsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mPipManager = PipManager.getInstance();
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        this.mPipControlsView = (PipControlsView) findViewById(R.id.pip_control_contents);
        this.mScrim = findViewById(R.id.scrim);
        this.mFocusGainAnimator = loadAnimator(this.mPipControlsView, R.anim.tv_pip_controls_in_recents_focus_gain_animation);
        this.mFocusLossAnimatorSet = new AnimatorSet();
        this.mFocusLossAnimatorSet.playSequentially(new Animator[]{loadAnimator(this.mPipControlsView, R.anim.tv_pip_controls_in_recents_focus_loss_animation), loadAnimator(this.mScrim, R.anim.tv_pip_controls_in_recents_scrim_fade_in_animation)});
        setPadding(0, this.mPipManager.getRecentsFocusedPipBounds().bottom, 0, 0);
    }

    private Animator loadAnimator(View view, int animatorResId) {
        Animator animator = AnimatorInflater.loadAnimator(getContext(), animatorResId);
        animator.setTarget(view);
        return animator;
    }

    public void startFocusGainAnimation() {
        this.mScrim.setAlpha(0.0f);
        PipControlButtonView focus = this.mPipControlsView.getFocusedButton();
        if (focus != null) {
            focus.startFocusGainAnimation();
        }
        startAnimator(this.mFocusGainAnimator, this.mFocusLossAnimatorSet);
    }

    public void startFocusLossAnimation() {
        PipControlButtonView focus = this.mPipControlsView.getFocusedButton();
        if (focus != null) {
            focus.startFocusLossAnimation();
        }
        startAnimator(this.mFocusLossAnimatorSet, this.mFocusGainAnimator);
    }

    public void reset() {
        cancelAnimator(this.mFocusGainAnimator);
        cancelAnimator(this.mFocusLossAnimatorSet);
        this.mScrim.setAlpha(0.0f);
        this.mPipControlsView.setTranslationY(0.0f);
        this.mPipControlsView.setScaleX(1.0f);
        this.mPipControlsView.setScaleY(1.0f);
        this.mPipControlsView.reset();
    }

    private static void startAnimator(Animator animator, Animator previousAnimator) {
        cancelAnimator(previousAnimator);
        if (!animator.isStarted()) {
            animator.start();
        }
    }

    private static void cancelAnimator(Animator animator) {
        if (animator.isStarted()) {
            animator.cancel();
        }
    }

    public void setListener(Listener listener) {
        this.mPipControlsView.setListener(listener);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (!event.isCanceled()) {
            if (event.getKeyCode() == 4 && event.getAction() == 1) {
                if (this.mPipControlsView.mListener != null) {
                    ((Listener) this.mPipControlsView.mListener).onBackPressed();
                }
                return true;
            } else if (event.getKeyCode() == 20) {
                if (event.getAction() == 0) {
                    this.mPipManager.getPipRecentsOverlayManager().clearFocus();
                }
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
