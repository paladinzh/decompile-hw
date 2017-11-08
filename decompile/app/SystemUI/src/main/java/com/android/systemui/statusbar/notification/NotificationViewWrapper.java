package com.android.systemui.statusbar.notification;

import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.ColorMatrix;
import android.service.notification.StatusBarNotification;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.TransformableView;

public abstract class NotificationViewWrapper implements TransformableView {
    protected boolean mDark;
    protected boolean mDarkInitialized = false;
    protected final ColorMatrix mGrayscaleColorMatrix = new ColorMatrix();
    protected final ExpandableNotificationRow mRow;
    protected final View mView;

    public static NotificationViewWrapper wrap(Context ctx, View v, ExpandableNotificationRow row) {
        if (v.getId() == 16909230) {
            if ("bigPicture".equals(v.getTag())) {
                return new NotificationBigPictureTemplateViewWrapper(ctx, v, row);
            }
            if ("bigText".equals(v.getTag())) {
                return new NotificationBigTextTemplateViewWrapper(ctx, v, row);
            }
            if ("media".equals(v.getTag()) || "bigMediaNarrow".equals(v.getTag())) {
                return new NotificationMediaTemplateViewWrapper(ctx, v, row);
            }
            if ("messaging".equals(v.getTag())) {
                return new NotificationMessagingTemplateViewWrapper(ctx, v, row);
            }
            return new NotificationTemplateViewWrapper(ctx, v, row);
        } else if (v instanceof NotificationHeaderView) {
            return new NotificationHeaderViewWrapper(ctx, v, row);
        } else {
            return new NotificationCustomViewWrapper(v, row);
        }
    }

    protected NotificationViewWrapper(View view, ExpandableNotificationRow row) {
        this.mView = view;
        this.mRow = row;
    }

    public void setDark(boolean dark, boolean fade, long delay) {
        this.mDark = dark;
        this.mDarkInitialized = true;
    }

    public void notifyContentUpdated(StatusBarNotification notification) {
        this.mDarkInitialized = false;
    }

    protected void startIntensityAnimation(AnimatorUpdateListener updateListener, boolean dark, long delay, AnimatorListener listener) {
        float startIntensity = dark ? 0.0f : 1.0f;
        float endIntensity = dark ? 1.0f : 0.0f;
        ValueAnimator animator = ValueAnimator.ofFloat(new float[]{startIntensity, endIntensity});
        animator.addUpdateListener(updateListener);
        animator.setDuration(700);
        animator.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        animator.setStartDelay(delay);
        if (listener != null) {
            animator.addListener(listener);
        }
        animator.start();
    }

    protected void updateGrayscaleMatrix(float intensity) {
        this.mGrayscaleColorMatrix.setSaturation(1.0f - intensity);
    }

    public void updateExpandability(boolean expandable, OnClickListener onClickListener) {
    }

    public NotificationHeaderView getNotificationHeader() {
        return null;
    }

    public TransformState getCurrentState(int fadingView) {
        return null;
    }

    public void transformTo(TransformableView notification, Runnable endRunnable) {
        CrossFadeHelper.fadeOut(this.mView, endRunnable);
    }

    public void transformTo(TransformableView notification, float transformationAmount) {
        CrossFadeHelper.fadeOut(this.mView, transformationAmount);
    }

    public void transformFrom(TransformableView notification) {
        CrossFadeHelper.fadeIn(this.mView);
    }

    public void transformFrom(TransformableView notification, float transformationAmount) {
        CrossFadeHelper.fadeIn(this.mView, transformationAmount);
    }

    public void setVisible(boolean visible) {
        this.mView.animate().cancel();
        this.mView.setVisibility(visible ? 0 : 4);
    }

    public int getCustomBackgroundColor() {
        return 0;
    }

    public void setShowingLegacyBackground(boolean showing) {
    }

    public void setContentHeight(int contentHeight, int minHeightHint) {
    }
}
