package com.android.systemui.statusbar.notification;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.service.notification.StatusBarNotification;
import android.support.v4.graphics.ColorUtils;
import android.view.View;
import com.android.systemui.R;
import com.android.systemui.ViewInvertHelper;
import com.android.systemui.statusbar.ExpandableNotificationRow;

public class NotificationCustomViewWrapper extends NotificationViewWrapper {
    private int mBackgroundColor = 0;
    private final Paint mGreyPaint = new Paint();
    private final ViewInvertHelper mInvertHelper;
    private boolean mShouldInvertDark;
    private boolean mShowingLegacyBackground;

    protected NotificationCustomViewWrapper(View view, ExpandableNotificationRow row) {
        super(view, row);
        this.mInvertHelper = new ViewInvertHelper(view, 700);
    }

    public void setDark(boolean dark, boolean fade, long delay) {
        if (dark != this.mDark || !this.mDarkInitialized) {
            super.setDark(dark, fade, delay);
            if (this.mShowingLegacyBackground || !this.mShouldInvertDark) {
                this.mView.setLayerType(dark ? 2 : 0, null);
                if (fade) {
                    fadeGrayscale(dark, delay);
                } else {
                    updateGrayscale(dark);
                }
            } else if (fade) {
                this.mInvertHelper.fade(dark, delay);
            } else {
                this.mInvertHelper.update(dark);
            }
        }
    }

    protected void fadeGrayscale(final boolean dark, long delay) {
        startIntensityAnimation(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                NotificationCustomViewWrapper.this.updateGrayscaleMatrix(((Float) animation.getAnimatedValue()).floatValue());
                NotificationCustomViewWrapper.this.mGreyPaint.setColorFilter(new ColorMatrixColorFilter(NotificationCustomViewWrapper.this.mGrayscaleColorMatrix));
                NotificationCustomViewWrapper.this.mView.setLayerPaint(NotificationCustomViewWrapper.this.mGreyPaint);
            }
        }, dark, delay, new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (!dark) {
                    NotificationCustomViewWrapper.this.mView.setLayerType(0, null);
                }
            }
        });
    }

    protected void updateGrayscale(boolean dark) {
        if (dark) {
            updateGrayscaleMatrix(1.0f);
            this.mGreyPaint.setColorFilter(new ColorMatrixColorFilter(this.mGrayscaleColorMatrix));
            this.mView.setLayerPaint(this.mGreyPaint);
        }
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.mView.setAlpha(visible ? 1.0f : 0.0f);
    }

    public void notifyContentUpdated(StatusBarNotification notification) {
        boolean isColorLight;
        super.notifyContentUpdated(notification);
        Drawable background = this.mView.getBackground();
        this.mBackgroundColor = 0;
        if (background instanceof ColorDrawable) {
            this.mBackgroundColor = ((ColorDrawable) background).getColor();
            this.mView.setBackground(null);
            this.mView.setTag(R.id.custom_background_color, Integer.valueOf(this.mBackgroundColor));
        } else if (this.mView.getTag(R.id.custom_background_color) != null) {
            this.mBackgroundColor = ((Integer) this.mView.getTag(R.id.custom_background_color)).intValue();
        }
        if (this.mBackgroundColor != 0) {
            isColorLight = isColorLight(this.mBackgroundColor);
        } else {
            isColorLight = true;
        }
        this.mShouldInvertDark = isColorLight;
    }

    private boolean isColorLight(int backgroundColor) {
        return Color.alpha(backgroundColor) == 0 || ColorUtils.calculateLuminance(backgroundColor) > 0.5d;
    }

    public int getCustomBackgroundColor() {
        return this.mRow.isSummaryWithChildren() ? 0 : this.mBackgroundColor;
    }

    public void setShowingLegacyBackground(boolean showing) {
        super.setShowingLegacyBackground(showing);
        this.mShowingLegacyBackground = showing;
    }
}
