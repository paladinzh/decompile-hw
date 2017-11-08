package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Trace;
import android.provider.Settings.Global;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.WindowManagerGlobal;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardHostView.OnDismissAction;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.statusbar.RemoteInputController.Callback;
import com.android.systemui.utils.HwLog;
import com.huawei.keyguard.inf.HwKeyguardPolicy;

public abstract class StatusBarKeyguardViewManager implements Callback {
    protected static String TAG = "StatusBarKeyguardViewManager";
    private OnDismissAction mAfterKeyguardGoneAction;
    protected KeyguardBouncer mBouncer;
    protected ViewGroup mContainer;
    protected final Context mContext;
    private boolean mDeferScrimFadeOut;
    private boolean mDeviceInteractive = false;
    private boolean mDeviceWillWakeUp;
    private FingerprintUnlockController mFingerprintUnlockController;
    protected boolean mFirstUpdate = true;
    private boolean mLastBouncerDismissible;
    protected boolean mLastBouncerShowing;
    protected boolean mLastOccluded;
    protected boolean mLastRemoteInputActive;
    protected boolean mLastShowing;
    protected LockPatternUtils mLockPatternUtils;
    private Runnable mMakeNavigationBarVisibleRunnable = new Runnable() {
        public void run() {
            StatusBarKeyguardViewManager.this.mPhoneStatusBar.getNavigationBarView().setVisibility(0);
        }
    };
    protected boolean mOccluded;
    protected PhoneStatusBar mPhoneStatusBar;
    protected boolean mRemoteInputActive;
    private boolean mScreenTurnedOn;
    protected ScrimController mScrimController;
    protected boolean mShowing;
    protected StatusBarWindowManager mStatusBarWindowManager;
    protected ViewMediatorCallback mViewMediatorCallback;
    protected boolean mWillShowBouncer = false;

    protected abstract void animateScrimControllerKeyguardFadingOutRaw(long j, long j2, Runnable runnable, boolean z);

    public abstract void exitBouncer();

    public abstract void setFobiddenCollpaseNotification(boolean z);

    public StatusBarKeyguardViewManager(Context context, ViewMediatorCallback callback, LockPatternUtils lockPatternUtils) {
        this.mContext = context;
        this.mViewMediatorCallback = callback;
        this.mLockPatternUtils = lockPatternUtils;
    }

    public void registerStatusBar(PhoneStatusBar phoneStatusBar, ViewGroup container, StatusBarWindowManager statusBarWindowManager, ScrimController scrimController, FingerprintUnlockController fingerprintUnlockController) {
        this.mPhoneStatusBar = phoneStatusBar;
        this.mContainer = container;
        this.mStatusBarWindowManager = statusBarWindowManager;
        this.mScrimController = scrimController;
        this.mFingerprintUnlockController = fingerprintUnlockController;
        this.mBouncer = SystemUIFactory.getInstance().createKeyguardBouncer(this.mContext, this.mViewMediatorCallback, this.mLockPatternUtils, this.mStatusBarWindowManager, container);
    }

    public void show(Bundle options) {
        HwLog.i(TAG, "show");
        this.mShowing = true;
        if (this.mStatusBarWindowManager != null) {
            this.mStatusBarWindowManager.setKeyguardShowing(true);
        }
        if (this.mScrimController != null) {
            this.mScrimController.abortKeyguardFadingOut();
        }
        reset();
    }

    protected void showBouncerOrKeyguard() {
        if (this.mBouncer == null) {
            HwLog.e(TAG, "showBouncerOrKeyguard::mBouncer is null!");
            return;
        }
        if (this.mBouncer.needsFullscreenBouncer()) {
            this.mWillShowBouncer = true;
            this.mPhoneStatusBar.hideKeyguard();
            this.mBouncer.show(true);
            this.mWillShowBouncer = false;
        } else {
            this.mWillShowBouncer = false;
            this.mPhoneStatusBar.showKeyguard();
            this.mBouncer.hide(false);
            this.mBouncer.prepare();
        }
    }

    private void showBouncer() {
        if (this.mShowing) {
            this.mBouncer.show(false);
        }
        updateStates();
    }

    public void dismissWithAction(OnDismissAction r, Runnable cancelAction, boolean afterKeyguardGone) {
        if (this.mShowing) {
            if (afterKeyguardGone) {
                this.mBouncer.show(false);
                this.mAfterKeyguardGoneAction = r;
                HwLog.w(TAG, "set AfterKeyguardGoneAction. " + r);
            } else {
                this.mBouncer.showWithDismissAction(r, cancelAction);
            }
        }
        updateStates();
    }

    public boolean isInDismiss() {
        return this.mBouncer.isInDismiss();
    }

    public void reset() {
        if (this.mShowing) {
            if (this.mOccluded) {
                this.mPhoneStatusBar.hideKeyguard();
                this.mPhoneStatusBar.stopWaitingForKeyguardExit();
                this.mBouncer.hide(false);
            } else {
                showBouncerOrKeyguard();
            }
            KeyguardUpdateMonitor.getInstance(this.mContext).sendKeyguardReset();
            updateStates();
        }
    }

    public void onStartedGoingToSleep() {
        this.mPhoneStatusBar.onStartedGoingToSleep();
    }

    public void onFinishedGoingToSleep() {
        this.mDeviceInteractive = false;
        this.mPhoneStatusBar.onFinishedGoingToSleep();
        this.mBouncer.onScreenTurnedOff();
    }

    public void onStartedWakingUp() {
        this.mDeviceInteractive = true;
        this.mDeviceWillWakeUp = false;
        if (this.mPhoneStatusBar != null) {
            this.mPhoneStatusBar.onStartedWakingUp();
        }
    }

    public void onScreenTurningOn() {
        if (this.mPhoneStatusBar != null) {
            this.mPhoneStatusBar.onScreenTurningOn();
        }
    }

    public boolean isScreenTurnedOn() {
        return this.mScreenTurnedOn;
    }

    public void onScreenTurnedOn() {
        this.mScreenTurnedOn = true;
        if (this.mDeferScrimFadeOut) {
            this.mDeferScrimFadeOut = false;
            animateScrimControllerKeyguardFadingOut(0, 0, true);
            updateStates();
        }
        if (this.mPhoneStatusBar != null) {
            this.mPhoneStatusBar.onScreenTurnedOn();
        }
    }

    public void onRemoteInputActive(boolean active) {
        this.mRemoteInputActive = active;
        updateStates();
    }

    public void onScreenTurnedOff() {
        this.mScreenTurnedOn = false;
        this.mPhoneStatusBar.onScreenTurnedOff();
    }

    public void notifyDeviceWakeUpRequested() {
        this.mDeviceWillWakeUp = !this.mDeviceInteractive;
    }

    public void verifyUnlock() {
        dismiss();
    }

    public void setNeedsInput(boolean needsInput) {
        this.mStatusBarWindowManager.setKeyguardNeedsInput(needsInput);
    }

    public boolean isUnlockWithWallpaper() {
        return this.mStatusBarWindowManager.isShowingWallpaper();
    }

    public void setOccluded(boolean occluded) {
        if (occluded && !this.mOccluded && this.mShowing && this.mPhoneStatusBar.isInLaunchTransition()) {
            this.mOccluded = true;
            this.mPhoneStatusBar.fadeKeyguardAfterLaunchTransition(null, new Runnable() {
                public void run() {
                    StatusBarKeyguardViewManager.this.mStatusBarWindowManager.setKeyguardOccluded(StatusBarKeyguardViewManager.this.mOccluded);
                    StatusBarKeyguardViewManager.this.reset();
                }
            });
            return;
        }
        this.mOccluded = occluded;
        this.mPhoneStatusBar.updateMediaMetaData(false, false);
        this.mStatusBarWindowManager.setKeyguardOccluded(occluded);
        reset();
    }

    public boolean isOccluded() {
        return this.mOccluded;
    }

    public void startPreHideAnimation(Runnable finishRunnable) {
        if (this.mBouncer.isShowing()) {
            this.mBouncer.startPreHideAnimation(finishRunnable);
        } else if (finishRunnable != null) {
            finishRunnable.run();
        }
    }

    public void hide(long startTime, long fadeoutDuration) {
        this.mShowing = false;
        long delay = Math.max(0, (-48 + startTime) - SystemClock.uptimeMillis());
        if (this.mPhoneStatusBar.isInLaunchTransition()) {
            HwLog.v(TAG, "hide: isInLaunchTransition " + startTime);
            this.mPhoneStatusBar.fadeKeyguardAfterLaunchTransition(new Runnable() {
                public void run() {
                    StatusBarKeyguardViewManager.this.mStatusBarWindowManager.setKeyguardShowing(false);
                    StatusBarKeyguardViewManager.this.mStatusBarWindowManager.setKeyguardFadingAway(true);
                    StatusBarKeyguardViewManager.this.mBouncer.hide(true);
                    StatusBarKeyguardViewManager.this.updateStates();
                    StatusBarKeyguardViewManager.this.animateScrimControllerKeyguardFadingOutRaw(100, 300, null, false);
                }
            }, new Runnable() {
                public void run() {
                    StatusBarKeyguardViewManager.this.mPhoneStatusBar.hideKeyguard();
                    StatusBarKeyguardViewManager.this.mStatusBarWindowManager.setKeyguardFadingAway(false);
                    StatusBarKeyguardViewManager.this.mViewMediatorCallback.keyguardGone();
                    StatusBarKeyguardViewManager.this.executeAfterKeyguardGoneAction();
                }
            });
            return;
        }
        long j;
        if (this.mFingerprintUnlockController.getMode() == 2) {
            HwLog.v(TAG, "hide: MODE_WAKE_AND_UNLOCK_PULSING " + startTime);
            this.mFingerprintUnlockController.startKeyguardFadingAway();
            this.mPhoneStatusBar.setKeyguardFadingAway(startTime, 0, 240);
            this.mStatusBarWindowManager.setKeyguardFadingAway(true);
            this.mPhoneStatusBar.fadeKeyguardWhilePulsing();
            animateScrimControllerKeyguardFadingOut(0, 240, new Runnable() {
                public void run() {
                    StatusBarKeyguardViewManager.this.mPhoneStatusBar.hideKeyguard();
                }
            }, false);
            j = delay;
        } else {
            boolean staying;
            this.mFingerprintUnlockController.startKeyguardFadingAway();
            if (this.mFingerprintUnlockController.isInfastScreenMode()) {
                j = 0;
                fadeoutDuration = 0;
                this.mPhoneStatusBar.setKeyguardFadingAway(startTime, 0, 0);
                staying = this.mPhoneStatusBar.hideKeyguard();
            } else {
                this.mPhoneStatusBar.setKeyguardFadingAway(startTime, delay, fadeoutDuration);
                staying = this.mPhoneStatusBar.hideKeyguard();
                j = delay;
            }
            HwLog.v(TAG, "hide: staying " + staying + "  " + startTime);
            if (staying) {
                this.mScrimController.animateGoingToFullShade(j, fadeoutDuration);
                this.mPhoneStatusBar.finishKeyguardFadingAway();
            } else {
                this.mStatusBarWindowManager.setKeyguardFadingAway(true);
                if (this.mFingerprintUnlockController.getMode() == 1) {
                    animateScrimControllerKeyguardFadingOut(0, 0, true);
                } else {
                    animateScrimControllerKeyguardFadingOut(j, fadeoutDuration, false);
                }
            }
        }
        this.mStatusBarWindowManager.setKeyguardShowing(false);
        this.mBouncer.hide(true);
        this.mViewMediatorCallback.keyguardGone();
        executeAfterKeyguardGoneAction();
        updateStates();
    }

    public void onDensityOrFontScaleChanged() {
        this.mBouncer.hide(true);
    }

    private void animateScrimControllerKeyguardFadingOut(long delay, long duration, boolean skipFirstFrame) {
        animateScrimControllerKeyguardFadingOut(delay, duration, null, skipFirstFrame);
    }

    protected void animateScrimControllerKeyguardFadingOut(long delay, long duration, final Runnable endRunnable, boolean skipFirstFrame) {
        Trace.asyncTraceBegin(8, "Fading out", 0);
        this.mScrimController.animateKeyguardFadingOut(delay, duration, new Runnable() {
            public void run() {
                if (endRunnable != null) {
                    endRunnable.run();
                }
                StatusBarKeyguardViewManager.this.mStatusBarWindowManager.setKeyguardFadingAway(false);
                StatusBarKeyguardViewManager.this.mPhoneStatusBar.finishKeyguardFadingAway();
                StatusBarKeyguardViewManager.this.mFingerprintUnlockController.finishKeyguardFadingAway();
                WindowManagerGlobal.getInstance().trimMemory(20);
                Trace.asyncTraceEnd(8, "Fading out", 0);
            }
        }, skipFirstFrame);
    }

    private void executeAfterKeyguardGoneAction() {
        if (this.mAfterKeyguardGoneAction != null) {
            this.mAfterKeyguardGoneAction.onDismiss();
            this.mAfterKeyguardGoneAction = null;
            HwLog.w(TAG, "AfterKeyguardGoneAction executed.");
        }
    }

    public void dismiss() {
        Log.i(TAG, "StatusBarKeyguardViewManager dismiss: " + this.mDeviceInteractive + " " + this.mDeviceWillWakeUp);
        if (this.mDeviceInteractive || this.mDeviceWillWakeUp) {
            showBouncer();
        }
    }

    public boolean isSecure() {
        return this.mBouncer.isSecure();
    }

    public boolean isShowing() {
        return this.mShowing;
    }

    public boolean onBackPressed() {
        if (!this.mBouncer.isShowing()) {
            return false;
        }
        this.mPhoneStatusBar.endAffordanceLaunch();
        exitBouncer();
        return true;
    }

    public boolean willShowBouncer() {
        return this.mWillShowBouncer;
    }

    public boolean isBouncerShowing() {
        return this.mBouncer.isShowing();
    }

    private long getNavBarShowDelay() {
        if (this.mPhoneStatusBar.isKeyguardFadingAway()) {
            return this.mPhoneStatusBar.getKeyguardFadingAwayDelay();
        }
        return 0;
    }

    public void updateStates() {
        int vis = this.mContainer.getSystemUiVisibility();
        boolean showing = this.mShowing;
        boolean occluded = this.mOccluded;
        boolean bouncerShowing = this.mBouncer.isShowing();
        boolean bouncerDismissible = !this.mBouncer.isFullscreenBouncer();
        boolean remoteInputActive = this.mRemoteInputActive;
        boolean z = (bouncerDismissible || !showing) ? true : remoteInputActive;
        boolean z2 = (this.mLastBouncerDismissible || !this.mLastShowing) ? true : this.mLastRemoteInputActive;
        if (z != z2 || this.mFirstUpdate) {
            if (bouncerDismissible || !showing || remoteInputActive) {
                this.mContainer.setSystemUiVisibility(-4194305 & vis);
            } else {
                this.mContainer.setSystemUiVisibility(4194304 | vis);
            }
        }
        boolean navBarVisible = isNavBarVisible();
        if ((navBarVisible != getLastNavBarVisible() || this.mFirstUpdate) && this.mPhoneStatusBar.getNavigationBarView() != null) {
            Log.w(TAG, "set navBarVisible ");
            if (navBarVisible) {
                long delay = getNavBarShowDelay();
                if (delay == 0) {
                    this.mMakeNavigationBarVisibleRunnable.run();
                } else {
                    this.mContainer.postOnAnimationDelayed(this.mMakeNavigationBarVisibleRunnable, delay);
                }
            } else {
                this.mContainer.removeCallbacks(this.mMakeNavigationBarVisibleRunnable);
                this.mPhoneStatusBar.getNavigationBarView().setVisibility(8);
            }
        }
        if (bouncerShowing != this.mLastBouncerShowing || this.mFirstUpdate) {
            this.mStatusBarWindowManager.setBouncerShowing(bouncerShowing);
            this.mPhoneStatusBar.setBouncerShowing(bouncerShowing);
            this.mScrimController.setBouncerShowing(bouncerShowing);
        }
        KeyguardUpdateMonitor updateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        Object obj = (!showing || occluded) ? null : 1;
        Object obj2 = (!this.mLastShowing || this.mLastOccluded) ? null : 1;
        if (obj != obj2 || this.mFirstUpdate) {
            z = showing && !occluded;
            updateMonitor.onKeyguardVisibilityChanged(z);
        }
        if (bouncerShowing != this.mLastBouncerShowing || this.mFirstUpdate) {
            updateMonitor.sendKeyguardBouncerChanged(bouncerShowing);
        }
        this.mFirstUpdate = false;
        this.mLastShowing = showing;
        this.mLastOccluded = occluded;
        this.mLastBouncerShowing = bouncerShowing;
        this.mLastBouncerDismissible = bouncerDismissible;
        this.mLastRemoteInputActive = remoteInputActive;
        this.mPhoneStatusBar.onKeyguardViewManagerStatesUpdated();
    }

    protected boolean isNavBarVisible() {
        if ((!this.mShowing || this.mOccluded || this.mRemoteInputActive || (this.mBouncer.isShowing() && HwKeyguardPolicy.getInst().showNavigationBarInbouncer())) && Global.getInt(this.mContext.getContentResolver(), "navigationbar_is_min", 0) != 1) {
            return true;
        }
        return false;
    }

    protected boolean getLastNavBarVisible() {
        if (!this.mLastShowing || this.mLastOccluded || this.mLastRemoteInputActive) {
            return true;
        }
        return this.mLastBouncerShowing ? HwKeyguardPolicy.getInst().showNavigationBarInbouncer() : false;
    }

    public boolean shouldDismissOnMenuPressed() {
        return this.mBouncer.shouldDismissOnMenuPressed();
    }

    public boolean interceptMediaKey(KeyEvent event) {
        return this.mBouncer.interceptMediaKey(event);
    }

    public void onActivityDrawn() {
        if (this.mPhoneStatusBar.isCollapsing()) {
            this.mPhoneStatusBar.addPostCollapseAction(new Runnable() {
                public void run() {
                    StatusBarKeyguardViewManager.this.mViewMediatorCallback.readyForKeyguardDone();
                }
            });
        } else {
            this.mViewMediatorCallback.readyForKeyguardDone();
        }
    }

    public boolean shouldDisableWindowAnimationsForUnlock() {
        return this.mPhoneStatusBar.isInLaunchTransition();
    }

    public boolean isGoingToNotificationShade() {
        return this.mPhoneStatusBar.isGoingToNotificationShade();
    }

    public boolean isSecure(int userId) {
        return !this.mBouncer.isSecure() ? this.mLockPatternUtils.isSecure(userId) : true;
    }

    public boolean isInputRestricted() {
        return this.mViewMediatorCallback.isInputRestricted();
    }

    public void keyguardGoingAway() {
        this.mPhoneStatusBar.keyguardGoingAway();
    }

    public void animateCollapsePanels(float speedUpFactor) {
        this.mPhoneStatusBar.animateCollapsePanels(0, true, false, speedUpFactor);
    }

    public void notifyKeyguardAuthenticated(boolean strongAuth) {
        this.mBouncer.notifyKeyguardAuthenticated(strongAuth);
    }

    public void showBouncerMessage(String message, int color) {
        this.mBouncer.showMessage(message, color);
    }

    public ViewRootImpl getViewRootImpl() {
        return this.mPhoneStatusBar.getStatusBarView().getViewRootImpl();
    }

    public boolean isFullscreenBouncer() {
        return this.mBouncer.isFullscreenBouncer();
    }

    public void forceShowWallpaper(boolean force) {
        this.mStatusBarWindowManager.forceShowWallpaper(force);
    }
}
