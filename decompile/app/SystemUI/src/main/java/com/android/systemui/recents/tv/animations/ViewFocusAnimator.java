package com.android.systemui.recents.tv.animations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import com.android.systemui.R;
import com.android.systemui.recents.tv.views.TaskCardView;

public class ViewFocusAnimator implements OnFocusChangeListener {
    private final int mAnimDuration;
    private final float mDismissIconAlpha;
    ObjectAnimator mFocusAnimation;
    private final Interpolator mFocusInterpolator;
    private float mFocusProgress;
    private final float mSelectedScale;
    private final float mSelectedScaleDelta = (this.mSelectedScale - this.mUnselectedScale);
    private final float mSelectedSpacingDelta;
    private final float mSelectedZDelta;
    protected TaskCardView mTargetView;
    private final float mUnselectedScale;
    private final float mUnselectedSpacing;
    private final float mUnselectedZ;

    public ViewFocusAnimator(TaskCardView view) {
        this.mTargetView = view;
        Resources res = view.getResources();
        this.mTargetView.setOnFocusChangeListener(this);
        TypedValue out = new TypedValue();
        res.getValue(R.integer.unselected_scale, out, true);
        this.mUnselectedScale = out.getFloat();
        res.getValue(R.integer.selected_scale, out, true);
        this.mSelectedScale = out.getFloat();
        this.mUnselectedZ = (float) res.getDimensionPixelOffset(R.dimen.recents_tv_unselected_item_z);
        this.mSelectedZDelta = (float) res.getDimensionPixelOffset(R.dimen.recents_tv_selected_item_z_delta);
        this.mUnselectedSpacing = (float) res.getDimensionPixelOffset(R.dimen.recents_tv_gird_card_spacing);
        this.mSelectedSpacingDelta = (float) res.getDimensionPixelOffset(R.dimen.recents_tv_gird_focused_card_delta);
        this.mAnimDuration = res.getInteger(R.integer.item_scale_anim_duration);
        this.mFocusInterpolator = new AccelerateDecelerateInterpolator();
        this.mFocusAnimation = ObjectAnimator.ofFloat(this, "focusProgress", new float[]{0.0f});
        this.mFocusAnimation.setDuration((long) this.mAnimDuration);
        this.mFocusAnimation.setInterpolator(this.mFocusInterpolator);
        this.mDismissIconAlpha = res.getFloat(R.integer.dismiss_unselected_alpha);
        setFocusProgress(0.0f);
        this.mFocusAnimation.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                ViewFocusAnimator.this.mTargetView.setHasTransientState(true);
            }

            public void onAnimationEnd(Animator animation) {
                ViewFocusAnimator.this.mTargetView.setHasTransientState(false);
            }
        });
    }

    private void setFocusProgress(float level) {
        this.mFocusProgress = level;
        float scale = this.mUnselectedScale + (this.mSelectedScaleDelta * level);
        float z = this.mUnselectedZ + (this.mSelectedZDelta * level);
        float spacing = this.mUnselectedSpacing + (this.mSelectedSpacingDelta * level);
        this.mTargetView.setScaleX(scale);
        this.mTargetView.setScaleY(scale);
        this.mTargetView.setPadding((int) spacing, this.mTargetView.getPaddingTop(), (int) spacing, this.mTargetView.getPaddingBottom());
        this.mTargetView.getDismissIconView().setAlpha(this.mDismissIconAlpha * level);
        this.mTargetView.getThumbnailView().setZ(z);
        this.mTargetView.getDismissIconView().setZ(z);
    }

    private void animateFocus(boolean focused) {
        if (this.mFocusAnimation.isStarted()) {
            this.mFocusAnimation.cancel();
        }
        if (this.mFocusProgress != (focused ? 1.0f : 0.0f)) {
            this.mFocusAnimation.setFloatValues(new float[]{this.mFocusProgress, focused ? 1.0f : 0.0f});
            this.mFocusAnimation.start();
        }
    }

    public void onFocusChange(View v, boolean hasFocus) {
        if (v == this.mTargetView) {
            changeSize(hasFocus);
        }
    }

    public void changeSize(boolean hasFocus) {
        LayoutParams lp = this.mTargetView.getLayoutParams();
        int width = lp.width;
        int height = lp.height;
        if (width < 0 && height < 0) {
            this.mTargetView.measure(0, 0);
        }
        if (this.mTargetView.isAttachedToWindow() && this.mTargetView.hasWindowFocus() && this.mTargetView.getVisibility() == 0) {
            animateFocus(hasFocus);
            return;
        }
        if (this.mFocusAnimation.isStarted()) {
            this.mFocusAnimation.cancel();
        }
        setFocusProgress(hasFocus ? 1.0f : 0.0f);
    }
}
