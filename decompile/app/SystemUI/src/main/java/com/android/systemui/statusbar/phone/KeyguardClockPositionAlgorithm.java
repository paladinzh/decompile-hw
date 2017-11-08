package com.android.systemui.statusbar.phone;

import android.content.res.Resources;
import android.graphics.Path;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.PathInterpolator;
import com.android.systemui.R;

public class KeyguardClockPositionAlgorithm {
    private static final PathInterpolator sSlowDownInterpolator;
    private AccelerateInterpolator mAccelerateInterpolator = new AccelerateInterpolator();
    private int mClockNotificationsMarginMax;
    private int mClockNotificationsMarginMin;
    private float mClockYFractionMax;
    private float mClockYFractionMin;
    private float mDensity;
    private float mEmptyDragAmount;
    private float mExpandedHeight;
    private int mHeight;
    private int mKeyguardStatusHeight;
    private int mMaxKeyguardNotifications;
    private int mMaxPanelHeight;
    private float mMoreCardNotificationAmount;
    private int mNotificationCount;

    public static class Result {
        public float clockAlpha;
        public float clockScale;
        public int clockY;
        public int stackScrollerPadding;
        public int stackScrollerPaddingAdjustment;
    }

    static {
        Path path = new Path();
        path.moveTo(0.0f, 0.0f);
        path.cubicTo(0.3f, 0.875f, 0.6f, 1.0f, 1.0f, 1.0f);
        sSlowDownInterpolator = new PathInterpolator(path);
    }

    public void loadDimens(Resources res) {
        this.mClockNotificationsMarginMin = res.getDimensionPixelSize(R.dimen.keyguard_clock_notifications_margin_min);
        this.mClockNotificationsMarginMax = res.getDimensionPixelSize(R.dimen.keyguard_clock_notifications_margin_max);
        this.mClockYFractionMin = res.getFraction(R.fraction.keyguard_clock_y_fraction_min, 1, 1);
        this.mClockYFractionMax = res.getFraction(R.fraction.keyguard_clock_y_fraction_max, 1, 1);
        this.mMoreCardNotificationAmount = ((float) res.getDimensionPixelSize(R.dimen.notification_summary_height)) / ((float) res.getDimensionPixelSize(R.dimen.notification_min_height));
        this.mDensity = res.getDisplayMetrics().density;
    }

    public void setup(int maxKeyguardNotifications, int maxPanelHeight, float expandedHeight, int notificationCount, int height, int keyguardStatusHeight, float emptyDragAmount) {
        this.mMaxKeyguardNotifications = maxKeyguardNotifications;
        this.mMaxPanelHeight = maxPanelHeight;
        this.mExpandedHeight = expandedHeight;
        this.mNotificationCount = notificationCount;
        this.mHeight = height;
        this.mKeyguardStatusHeight = keyguardStatusHeight;
        this.mEmptyDragAmount = emptyDragAmount;
    }

    public float getMinStackScrollerPadding(int height, int keyguardStatusHeight) {
        return ((this.mClockYFractionMin * ((float) height)) + ((float) (keyguardStatusHeight / 2))) + ((float) this.mClockNotificationsMarginMin);
    }

    public void run(Result result) {
        int y = getClockY() - (this.mKeyguardStatusHeight / 2);
        result.stackScrollerPaddingAdjustment = (int) (getClockYExpansionAdjustment() * getTopPaddingAdjMultiplier());
        int padding = y + (getClockNotificationsPadding() + result.stackScrollerPaddingAdjustment);
        result.clockY = y;
        result.stackScrollerPadding = this.mKeyguardStatusHeight + padding;
        result.clockScale = getClockScale(result.stackScrollerPadding, result.clockY, (getClockNotificationsPadding() + y) + this.mKeyguardStatusHeight);
        result.clockAlpha = getClockAlpha(result.clockScale);
    }

    private float getClockScale(int notificationPadding, int clockY, int startPadding) {
        float scaleEnd = ((float) clockY) - (((float) this.mKeyguardStatusHeight) * (getNotificationAmountT() == 0.0f ? 6.0f : 5.0f));
        return (float) (((double) this.mAccelerateInterpolator.getInterpolation(Math.max(0.0f, Math.min((((float) notificationPadding) - scaleEnd) / (((float) startPadding) - scaleEnd), 1.0f)))) * Math.pow((double) (((this.mEmptyDragAmount / this.mDensity) / 300.0f) + 1.0f), 0.30000001192092896d));
    }

    private int getClockNotificationsPadding() {
        float t = Math.min(getNotificationAmountT(), 1.0f);
        return (int) ((((float) this.mClockNotificationsMarginMin) * t) + ((1.0f - t) * ((float) this.mClockNotificationsMarginMax)));
    }

    private float getClockYFraction() {
        float t = Math.min(getNotificationAmountT(), 1.0f);
        return ((1.0f - t) * this.mClockYFractionMax) + (this.mClockYFractionMin * t);
    }

    private int getClockY() {
        return (int) (getClockYFraction() * ((float) this.mHeight));
    }

    private float getClockYExpansionAdjustment() {
        float value = getClockYExpansionRubberbandFactor() * (((float) this.mMaxPanelHeight) - this.mExpandedHeight);
        float slowedDownValue = ((-sSlowDownInterpolator.getInterpolation(value / ((float) this.mMaxPanelHeight))) * 0.4f) * ((float) this.mMaxPanelHeight);
        if (this.mNotificationCount == 0) {
            return ((-2.0f * value) + slowedDownValue) / 3.0f;
        }
        return slowedDownValue;
    }

    private float getClockYExpansionRubberbandFactor() {
        float t = (float) Math.pow((double) Math.min(getNotificationAmountT(), 1.0f), 0.30000001192092896d);
        return ((1.0f - t) * 0.8f) + (0.08f * t);
    }

    private float getTopPaddingAdjMultiplier() {
        float t = Math.min(getNotificationAmountT(), 1.0f);
        return ((1.0f - t) * 1.4f) + (3.2f * t);
    }

    private float getClockAlpha(float scale) {
        float fadeEnd;
        if (getNotificationAmountT() == 0.0f) {
            fadeEnd = 0.5f;
        } else {
            fadeEnd = 0.75f;
        }
        return Math.max(0.0f, Math.min(1.0f, (scale - fadeEnd) / (0.95f - fadeEnd)));
    }

    private float getNotificationAmountT() {
        return ((float) this.mNotificationCount) / (((float) this.mMaxKeyguardNotifications) + this.mMoreCardNotificationAmount);
    }
}
