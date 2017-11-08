package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardHostView.OnDismissAction;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.PerfAdjust;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.cover.CoverViewManager;
import com.huawei.keyguard.theme.KeyguardTheme;

public class HwStatusBarKeyguardViewManager extends StatusBarKeyguardViewManager {
    private final boolean isDismissHwlock = SystemProperties.getBoolean("ro.config.dismiss_hwlock", false);
    private IAnimationListener mAnimationListener = null;
    protected boolean mIsFobiddenCollpaseNotification = false;
    private boolean mIsNotificationTranslucent = false;
    private boolean mLastCoverShow = false;
    private boolean mShowBouncerAtFirstStart = true;

    public interface IAnimationListener {
        void onPreHideAnimation();

        void onRevertToKeyguard(Runnable runnable);
    }

    public boolean hasIninted() {
        return (this.mPhoneStatusBar == null || this.mBouncer == null) ? false : true;
    }

    public void setAnimationListener(IAnimationListener animListener) {
        this.mAnimationListener = animListener;
    }

    public HwStatusBarKeyguardViewManager(Context context, ViewMediatorCallback callback, LockPatternUtils lockPatternUtils) {
        super(context, callback, lockPatternUtils);
    }

    public boolean isInDismiss() {
        return this.mBouncer.isInDismiss();
    }

    public void reset() {
        super.reset();
        restoreNotificationPanel();
    }

    public void exitBouncer() {
        if (!this.mShowing || this.mOccluded) {
            reset();
            HwLog.w(TAG, "exitBouncer with invalid state");
            return;
        }
        Runnable onFinishRunner = new Runnable() {
            public void run() {
                HwStatusBarKeyguardViewManager.this.mPhoneStatusBar.showKeyguard();
                HwStatusBarKeyguardViewManager.this.mBouncer.hide(false);
                HwStatusBarKeyguardViewManager.this.mBouncer.prepare();
                KeyguardUpdateMonitor.getInstance(HwStatusBarKeyguardViewManager.this.mContext).sendKeyguardReset();
                HwStatusBarKeyguardViewManager.this.updateStates();
            }
        };
        if (this.mContainer != null) {
            this.mBouncer.revertToKeyguard();
            if (this.mAnimationListener != null) {
                this.mAnimationListener.onRevertToKeyguard(onFinishRunner);
            }
        } else {
            onFinishRunner.run();
        }
        restoreNotificationPanel();
        this.mPhoneStatusBar.userActivity();
    }

    private void restoreNotificationPanel() {
        View nPanel = null;
        if (this.mPhoneStatusBar != null) {
            nPanel = this.mPhoneStatusBar.getNotificationPanelView();
        }
        if (nPanel != null) {
            if (this.mIsFobiddenCollpaseNotification) {
                HwLog.w(TAG, "animate restoreNotificationPanel " + this.mIsFobiddenCollpaseNotification);
                nPanel.animate().alpha(1.0f).setDuration(250).setInterpolator(new AccelerateInterpolator(2.0f)).setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator _a) {
                        HwStatusBarKeyguardViewManager.this.mIsNotificationTranslucent = false;
                    }
                }).start();
                return;
            }
            if (nPanel.getAlpha() < 1.0f) {
                HwLog.w(TAG, "restoreNotificationPanel " + this.mIsFobiddenCollpaseNotification);
                nPanel.setAlpha(1.0f);
            }
            this.mIsNotificationTranslucent = false;
        }
    }

    public void setOccluded(boolean occluded) {
        HwLog.i(TAG, "setOccluded to:" + occluded + " from " + this.mOccluded + " " + this.mShowing);
        super.setOccluded(occluded);
    }

    public void startPreHideAnimation(Runnable finishRunnable) {
        HwLog.w(TAG, "startPreHideAnimation");
        super.startPreHideAnimation(finishRunnable);
        if (this.mAnimationListener != null) {
            this.mAnimationListener.onPreHideAnimation();
        }
    }

    protected void animateScrimControllerKeyguardFadingOutRaw(long delay, long duration, Runnable endRunnable, boolean skipFirstFrame) {
        this.mScrimController.animateKeyguardFadingOut(delay, duration, endRunnable, skipFirstFrame);
    }

    public void updateStates() {
        boolean showClock = true;
        HwLog.i(TAG, "updateStates " + this.mShowing + " " + this.mOccluded);
        if (this.mContainer != null) {
            int lastNavVisibility = 0;
            NavigationBarView navigationBarView = getNavBarView();
            if (navigationBarView != null) {
                lastNavVisibility = navigationBarView.getVisibility();
            }
            super.updateStates();
            checkToRequestFitSystemWindows(lastNavVisibility);
            KeyguardStatusBarView keyguardStatusBarView = this.mPhoneStatusBar.getKeyguardStatusBarView();
            if (keyguardStatusBarView != null) {
                if (!isShowing() || isOccluded()) {
                    keyguardStatusBarView.setVisibility(8);
                    HwLog.w(TAG, "updateStates Hide KeyguardStatusBar ");
                    resetFitSystemWindows();
                } else {
                    keyguardStatusBarView.setVisibility(0);
                    if (!(this.mBouncer.isShowing() || this.mLastBouncerShowing || KeyguardTheme.getInst().getLockStyle() == 7 || KeyguardTheme.getInst().getLockStyle() == 8)) {
                        showClock = false;
                    }
                    keyguardStatusBarView.setClockVisible(showClock);
                    HwLog.w(TAG, "updateStates Show KeyguardStatusBar with clcok: " + showClock);
                }
            }
            this.mLastCoverShow = CoverViewManager.getInstance(this.mContext).isCoverAdded();
        }
    }

    private NavigationBarView getNavBarView() {
        if (this.mPhoneStatusBar != null) {
            return this.mPhoneStatusBar.getNavigationBarView();
        }
        return null;
    }

    private void resetFitSystemWindows() {
        if (isLand() && this.mPhoneStatusBar != null) {
            View view = this.mPhoneStatusBar.getStatusBarWindow();
            if (view != null) {
                view.requestFitSystemWindows();
            }
        }
    }

    private void checkToRequestFitSystemWindows(int navVisibility) {
        NavigationBarView navigationBarView = getNavBarView();
        if (navigationBarView != null) {
            int currentNavVisibility = navigationBarView.getVisibility();
            if (currentNavVisibility != navVisibility && currentNavVisibility == 8 && isLand() && PerfAdjust.supportScreenRotation() && this.mPhoneStatusBar != null) {
                View view = this.mPhoneStatusBar.getStatusBarWindow();
                if (view != null) {
                    view.requestFitSystemWindows();
                }
            }
        }
    }

    private boolean isLand() {
        boolean z = true;
        Resources resources = this.mContext.getResources();
        if (resources == null) {
            return true;
        }
        Configuration config = resources.getConfiguration();
        if (config == null) {
            return true;
        }
        if (config.orientation == 1) {
            z = false;
        }
        return z;
    }

    protected void showBouncerOrKeyguard() {
        if (this.isDismissHwlock) {
            this.mShowBouncerAtFirstStart = true;
            HwLog.w(TAG, "showBouncerOrKeyguard customer need remove hwlock when set bouncer.isDismissHwlock:" + this.isDismissHwlock);
        }
        if (this.mShowBouncerAtFirstStart) {
            HwKeyguardUpdateMonitor monitor = HwKeyguardUpdateMonitor.getInstance(this.mContext);
            boolean z = monitor.isSecure() ? !monitor.isFirstTimeStartup() ? this.isDismissHwlock : true : false;
            this.mShowBouncerAtFirstStart = z;
        }
        if (this.mShowBouncerAtFirstStart) {
            if (this.mPhoneStatusBar != null) {
                this.mPhoneStatusBar.hideKeyguard();
            } else {
                HwLog.e(TAG, "showBouncerOrKeyguard::mPhoneStatusBar is null!");
            }
            if (this.mBouncer != null) {
                this.mBouncer.show(true);
            } else {
                HwLog.e(TAG, "showBouncerOrKeyguard::mBouncer is null!");
            }
            if (this.isDismissHwlock) {
                this.mShowBouncerAtFirstStart = true;
                return;
            } else {
                this.mShowBouncerAtFirstStart = false;
                return;
            }
        }
        super.showBouncerOrKeyguard();
        if (!this.mWillShowBouncer) {
            restoreNotificationPanel();
        }
    }

    public void dismissWithAction(OnDismissAction r, Runnable cancelAction, boolean afterKeyguardGone) {
        boolean isInDismiss = !this.mBouncer.isShowingSoon() ? this.mBouncer.isInDismiss() : true;
        super.dismissWithAction(r, cancelAction, afterKeyguardGone);
        HwKeyguardUpdateMonitor monitor = HwKeyguardUpdateMonitor.getInstance(this.mContext);
        boolean userCanSkipBouncer = monitor != null ? monitor.getUserCanSkipBouncer(KeyguardUpdateMonitor.getCurrentUser()) : false;
        boolean isSecure = monitor != null ? monitor.isSecure() : false;
        HwLog.w(TAG, "dismissWithAction going to bouncer. showing: " + this.mShowing + " " + isInDismiss + " - " + this.mBouncer.isShowingSoon() + ", " + userCanSkipBouncer + ", " + isSecure);
        if (isSecure && !userCanSkipBouncer && !isInDismiss) {
            if (this.mBouncer.isInDismiss() || this.mBouncer.isShowingSoon()) {
                collpaseOrHideNotification();
            }
        }
    }

    public void collpaseOrHideNotification() {
        if (this.mIsNotificationTranslucent) {
            HwLog.w(TAG, "collpaseOrHideNotification is already translucent now");
        } else if (this.mIsFobiddenCollpaseNotification) {
            HwLog.w(TAG, "collpaseOrHideNotification do hide");
            this.mIsFobiddenCollpaseNotification = false;
            this.mPhoneStatusBar.getNotificationPanelView().animate().alpha(0.0f).setDuration(250).setInterpolator(new AccelerateInterpolator(2.0f)).setListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator _a) {
                    HwStatusBarKeyguardViewManager.this.mIsNotificationTranslucent = true;
                }
            }).start();
        } else {
            HwLog.w(TAG, "collpaseOrHideNotification do animateCollapsePanels");
            animateCollapsePanels(1.8f);
        }
    }

    public void dismiss() {
        boolean isInDismiss = !this.mBouncer.isShowingSoon() ? this.mBouncer.isInDismiss() : true;
        super.dismiss();
        HwKeyguardUpdateMonitor monitor = HwKeyguardUpdateMonitor.getInstance(this.mContext);
        boolean userCanSkipBouncer = monitor != null ? monitor.getUserCanSkipBouncer(KeyguardUpdateMonitor.getCurrentUser()) : false;
        boolean isSecure = monitor != null ? monitor.isSecure() : false;
        HwLog.w(TAG, "dismiss. showing: " + this.mShowing + " " + isInDismiss + " - " + this.mBouncer.isShowingSoon() + ", " + userCanSkipBouncer + ", " + isSecure, new Exception());
        if (isSecure && !userCanSkipBouncer && !isInDismiss) {
            if (this.mBouncer.isInDismiss() || this.mBouncer.isShowingSoon()) {
                collpaseOrHideNotification();
            }
        }
    }

    protected boolean isNavBarVisible() {
        return !CoverViewManager.getInstance(this.mContext).isCoverAdded() ? super.isNavBarVisible() : false;
    }

    protected boolean getLastNavBarVisible() {
        return !this.mLastCoverShow ? super.getLastNavBarVisible() : false;
    }

    public void resetOnlyToBouncer() {
        if (this.mShowing) {
            if (this.mOccluded) {
                this.mPhoneStatusBar.hideKeyguard();
                this.mPhoneStatusBar.stopWaitingForKeyguardExit();
                this.mBouncer.hide(false);
            } else {
                showKeyguardIfNotInSecureBouncer();
            }
            KeyguardUpdateMonitor.getInstance(this.mContext).sendKeyguardReset();
            updateStates();
        }
    }

    protected void showKeyguardIfNotInSecureBouncer() {
        if (this.mBouncer.needsFullscreenBouncer() || this.mBouncer.isShowingWithSecure()) {
            HwLog.w(TAG, "showKeyguardIfNotInSecureBouncer do show bouncer");
            this.mPhoneStatusBar.hideKeyguard();
            this.mBouncer.show(true);
            return;
        }
        HwLog.w(TAG, "showKeyguardIfNotInSecureBouncer do show keyguard");
        this.mPhoneStatusBar.showKeyguard();
        this.mBouncer.hide(false);
        this.mBouncer.prepare();
    }

    public void setFobiddenCollpaseNotification(boolean forbidden) {
        this.mIsFobiddenCollpaseNotification = true;
    }

    public void afterCoverRemoved() {
        if ((isOccluded() || !isShowing()) && isNavBarVisible() != getLastNavBarVisible()) {
            HwLog.w(TAG, "afterCoverRemoved updateStates");
            updateStates();
        }
    }
}
