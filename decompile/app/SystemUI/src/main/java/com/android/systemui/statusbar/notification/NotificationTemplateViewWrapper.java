package com.android.systemui.statusbar.notification;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Color;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation;

public class NotificationTemplateViewWrapper extends NotificationHeaderViewWrapper {
    private View mActionsContainer;
    private int mContentHeight;
    private int mMinHeightHint;
    protected ImageView mPicture;
    private ProgressBar mProgressBar;
    private TextView mText;
    private TextView mTitle;

    protected NotificationTemplateViewWrapper(Context ctx, View view, ExpandableNotificationRow row) {
        super(ctx, view, row);
        this.mTransformationHelper.setCustomTransformation(new CustomTransformation() {
            public boolean transformTo(TransformState ownState, TransformableView notification, float transformationAmount) {
                if (!(notification instanceof HybridNotificationView)) {
                    return false;
                }
                TransformState otherState = notification.getCurrentState(1);
                CrossFadeHelper.fadeOut(ownState.getTransformedView(), transformationAmount);
                if (otherState != null) {
                    ownState.transformViewVerticalTo(otherState, this, transformationAmount);
                    otherState.recycle();
                }
                return true;
            }

            public boolean customTransformTarget(TransformState ownState, TransformState otherState) {
                ownState.setTransformationEndY(getTransformationY(ownState, otherState));
                return true;
            }

            public boolean transformFrom(TransformState ownState, TransformableView notification, float transformationAmount) {
                if (!(notification instanceof HybridNotificationView)) {
                    return false;
                }
                TransformState otherState = notification.getCurrentState(1);
                CrossFadeHelper.fadeIn(ownState.getTransformedView(), transformationAmount);
                if (otherState != null) {
                    ownState.transformViewVerticalFrom(otherState, this, transformationAmount);
                    otherState.recycle();
                }
                return true;
            }

            public boolean initTransformation(TransformState ownState, TransformState otherState) {
                ownState.setTransformationStartY(getTransformationY(ownState, otherState));
                return true;
            }

            private float getTransformationY(TransformState ownState, TransformState otherState) {
                return ((float) ((otherState.getLaidOutLocationOnScreen()[1] + otherState.getTransformedView().getHeight()) - ownState.getLaidOutLocationOnScreen()[1])) * 0.33f;
            }
        }, 2);
    }

    private void resolveTemplateViews(StatusBarNotification notification) {
        this.mPicture = (ImageView) this.mView.findViewById(16908356);
        this.mPicture.setTag(R.id.image_icon_tag, notification.getNotification().getLargeIcon());
        this.mTitle = (TextView) this.mView.findViewById(16908310);
        this.mText = (TextView) this.mView.findViewById(16908413);
        View progress = this.mView.findViewById(16908301);
        if (progress instanceof ProgressBar) {
            this.mProgressBar = (ProgressBar) progress;
        } else {
            this.mProgressBar = null;
        }
        this.mActionsContainer = this.mView.findViewById(16909214);
    }

    public void notifyContentUpdated(StatusBarNotification notification) {
        resolveTemplateViews(notification);
        super.notifyContentUpdated(notification);
    }

    protected void updateInvertHelper() {
        super.updateInvertHelper();
        View mainColumn = this.mView.findViewById(16909231);
        if (mainColumn != null) {
            this.mInvertHelper.addTarget(mainColumn);
        }
    }

    protected void updateTransformedTypes() {
        super.updateTransformedTypes();
        if (this.mTitle != null) {
            this.mTransformationHelper.addTransformedView(1, this.mTitle);
        }
        if (this.mText != null) {
            this.mTransformationHelper.addTransformedView(2, this.mText);
        }
        if (this.mPicture != null) {
            this.mTransformationHelper.addTransformedView(3, this.mPicture);
        }
        if (this.mProgressBar != null) {
            this.mTransformationHelper.addTransformedView(4, this.mProgressBar);
        }
    }

    public void setDark(boolean dark, boolean fade, long delay) {
        if (dark != this.mDark || !this.mDarkInitialized) {
            super.setDark(dark, fade, delay);
            setPictureGrayscale(dark, fade, delay);
            setProgressBarDark(dark, fade, delay);
        }
    }

    private void setProgressBarDark(boolean dark, boolean fade, long delay) {
        if (this.mProgressBar == null) {
            return;
        }
        if (fade) {
            fadeProgressDark(this.mProgressBar, dark, delay);
        } else {
            updateProgressDark(this.mProgressBar, dark);
        }
    }

    private void fadeProgressDark(final ProgressBar target, boolean dark, long delay) {
        startIntensityAnimation(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                NotificationTemplateViewWrapper.this.updateProgressDark(target, ((Float) animation.getAnimatedValue()).floatValue());
            }
        }, dark, delay, null);
    }

    private void updateProgressDark(ProgressBar target, float intensity) {
        int color = interpolateColor(this.mColor, -1, intensity);
        target.getIndeterminateDrawable().mutate().setTint(color);
        target.getProgressDrawable().mutate().setTint(color);
    }

    private void updateProgressDark(ProgressBar target, boolean dark) {
        updateProgressDark(target, dark ? 1.0f : 0.0f);
    }

    protected void setPictureGrayscale(boolean grayscale, boolean fade, long delay) {
        if (this.mPicture == null) {
            return;
        }
        if (fade) {
            fadeGrayscale(this.mPicture, grayscale, delay);
        } else {
            updateGrayscale(this.mPicture, grayscale);
        }
    }

    private static int interpolateColor(int source, int target, float t) {
        int aSource = Color.alpha(source);
        int rSource = Color.red(source);
        int gSource = Color.green(source);
        int bSource = Color.blue(source);
        return Color.argb((int) ((((float) aSource) * (1.0f - t)) + (((float) Color.alpha(target)) * t)), (int) ((((float) rSource) * (1.0f - t)) + (((float) Color.red(target)) * t)), (int) ((((float) gSource) * (1.0f - t)) + (((float) Color.green(target)) * t)), (int) ((((float) bSource) * (1.0f - t)) + (((float) Color.blue(target)) * t)));
    }

    public void setContentHeight(int contentHeight, int minHeightHint) {
        super.setContentHeight(contentHeight, minHeightHint);
        this.mContentHeight = contentHeight;
        this.mMinHeightHint = minHeightHint;
        updateActionOffset();
    }

    private void updateActionOffset() {
        if (this.mActionsContainer != null) {
            this.mActionsContainer.setTranslationY((float) (Math.max(this.mContentHeight, this.mMinHeightHint) - this.mView.getHeight()));
        }
    }
}
