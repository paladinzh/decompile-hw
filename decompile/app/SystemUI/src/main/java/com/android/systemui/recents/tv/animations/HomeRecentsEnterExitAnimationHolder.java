package com.android.systemui.recents.tv.animations;

import android.content.Context;
import android.view.View;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.tv.views.TaskCardView;
import com.android.systemui.recents.tv.views.TaskStackHorizontalGridView;

public class HomeRecentsEnterExitAnimationHolder {
    private Context mContext;
    private long mDelay = ((long) this.mContext.getResources().getInteger(R.integer.recents_home_delay));
    private float mDimAlpha = this.mContext.getResources().getFloat(R.dimen.recents_recents_row_dim_alpha);
    private int mDuration = this.mContext.getResources().getInteger(R.integer.recents_home_duration);
    private TaskStackHorizontalGridView mGridView;
    private int mTranslationX = this.mContext.getResources().getDimensionPixelSize(R.dimen.recents_tv_home_recents_shift);

    public HomeRecentsEnterExitAnimationHolder(Context context, TaskStackHorizontalGridView gridView) {
        this.mContext = context;
        this.mGridView = gridView;
    }

    public void startEnterAnimation(boolean isPipShown) {
        for (int i = 0; i < this.mGridView.getChildCount(); i++) {
            TaskCardView view = (TaskCardView) this.mGridView.getChildAt(i);
            view.setTranslationX((float) (-this.mTranslationX));
            view.animate().alpha(isPipShown ? this.mDimAlpha : 1.0f).translationX(0.0f).setDuration((long) this.mDuration).setStartDelay(this.mDelay * ((long) i)).setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        }
    }

    public void startExitAnimation(DismissRecentsToHomeAnimationStarted dismissEvent) {
        for (int i = this.mGridView.getChildCount() - 1; i >= 0; i--) {
            TaskCardView view = (TaskCardView) this.mGridView.getChildAt(i);
            view.animate().alpha(0.0f).translationXBy((float) (-this.mTranslationX)).setDuration((long) this.mDuration).setStartDelay(this.mDelay * ((long) ((this.mGridView.getChildCount() - 1) - i))).setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            if (i == 0) {
                view.animate().setListener(dismissEvent.getAnimationTrigger().decrementOnAnimationEnd());
                dismissEvent.getAnimationTrigger().increment();
            }
        }
    }

    public void setEnterFromHomeStartingAnimationValues(boolean isPipShown) {
        for (int i = 0; i < this.mGridView.getChildCount(); i++) {
            TaskCardView view = (TaskCardView) this.mGridView.getChildAt(i);
            view.setTranslationX(0.0f);
            view.setAlpha(0.0f);
            view.getInfoFieldView().setAlpha(isPipShown ? 0.0f : 1.0f);
            if (isPipShown && view.hasFocus()) {
                view.getViewFocusAnimator().changeSize(false);
            }
        }
    }

    public void setEnterFromAppStartingAnimationValues(boolean isPipShown) {
        for (int i = 0; i < this.mGridView.getChildCount(); i++) {
            float f;
            TaskCardView view = (TaskCardView) this.mGridView.getChildAt(i);
            view.setTranslationX(0.0f);
            if (isPipShown) {
                f = this.mDimAlpha;
            } else {
                f = 1.0f;
            }
            view.setAlpha(f);
            View infoFieldView = view.getInfoFieldView();
            if (isPipShown) {
                f = 0.0f;
            } else {
                f = 1.0f;
            }
            infoFieldView.setAlpha(f);
            if (isPipShown && view.hasFocus()) {
                view.getViewFocusAnimator().changeSize(false);
            }
        }
    }
}
