package com.android.systemui.recents.tv.animations;

import android.animation.Animator.AnimatorListener;
import android.content.res.Resources;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.recents.tv.views.TaskCardView;

public class DismissAnimationsHolder {
    private ImageView mCardDismissIcon;
    private TransitionDrawable mDismissDrawable = ((TransitionDrawable) this.mCardDismissIcon.getDrawable());
    private int mDismissEnterYDelta;
    private float mDismissIconNotInDismissStateAlpha;
    private int mDismissStartYDelta;
    private TextView mDismissText;
    private LinearLayout mInfoField;
    private long mLongDuration;
    private long mShortDuration;
    private View mThumbnailView;

    public DismissAnimationsHolder(TaskCardView taskCardView) {
        this.mInfoField = (LinearLayout) taskCardView.findViewById(R.id.card_info_field);
        this.mThumbnailView = taskCardView.findViewById(R.id.card_view_thumbnail);
        this.mCardDismissIcon = (ImageView) taskCardView.findViewById(R.id.dismiss_icon);
        this.mDismissDrawable.setCrossFadeEnabled(true);
        this.mDismissText = (TextView) taskCardView.findViewById(R.id.card_dismiss_text);
        Resources res = taskCardView.getResources();
        this.mDismissEnterYDelta = res.getDimensionPixelOffset(R.dimen.recents_tv_dismiss_shift_down);
        this.mDismissStartYDelta = this.mDismissEnterYDelta * 2;
        this.mShortDuration = (long) res.getInteger(R.integer.dismiss_short_duration);
        this.mLongDuration = (long) res.getInteger(R.integer.dismiss_long_duration);
        this.mDismissIconNotInDismissStateAlpha = res.getFloat(R.integer.dismiss_unselected_alpha);
    }

    public void startEnterAnimation() {
        this.mCardDismissIcon.animate().setDuration(this.mShortDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).alpha(1.0f).withStartAction(new Runnable() {
            public void run() {
                DismissAnimationsHolder.this.mDismissDrawable.startTransition(0);
            }
        });
        this.mDismissText.animate().setDuration(this.mShortDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).alpha(1.0f);
        this.mInfoField.animate().setDuration(this.mShortDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).translationY((float) this.mDismissEnterYDelta).alpha(0.5f);
        this.mThumbnailView.animate().setDuration(this.mShortDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).translationY((float) this.mDismissEnterYDelta).alpha(0.5f);
    }

    public void startExitAnimation() {
        this.mCardDismissIcon.animate().setDuration(this.mShortDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).alpha(this.mDismissIconNotInDismissStateAlpha).withEndAction(new Runnable() {
            public void run() {
                DismissAnimationsHolder.this.mDismissDrawable.reverseTransition(0);
            }
        });
        this.mDismissText.animate().setDuration(this.mShortDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).alpha(0.0f);
        this.mInfoField.animate().setDuration(this.mShortDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).translationY(0.0f).alpha(1.0f);
        this.mThumbnailView.animate().setDuration(this.mShortDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).translationY(0.0f).alpha(1.0f);
    }

    public void startDismissAnimation(AnimatorListener listener) {
        this.mCardDismissIcon.animate().setDuration(this.mShortDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).alpha(0.0f).withEndAction(new Runnable() {
            public void run() {
                DismissAnimationsHolder.this.mDismissDrawable.reverseTransition(0);
            }
        });
        this.mDismissText.animate().setDuration(this.mShortDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).alpha(0.0f);
        this.mInfoField.animate().setDuration(this.mLongDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).translationY((float) this.mDismissStartYDelta).alpha(0.0f).setListener(listener);
        this.mThumbnailView.animate().setDuration(this.mLongDuration).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).translationY((float) this.mDismissStartYDelta).alpha(0.0f);
    }

    public void reset() {
        this.mInfoField.setAlpha(1.0f);
        this.mInfoField.setTranslationY(0.0f);
        this.mInfoField.animate().setListener(null);
        this.mThumbnailView.setAlpha(1.0f);
        this.mThumbnailView.setTranslationY(0.0f);
        this.mCardDismissIcon.setAlpha(0.0f);
        this.mDismissText.setAlpha(0.0f);
    }
}
