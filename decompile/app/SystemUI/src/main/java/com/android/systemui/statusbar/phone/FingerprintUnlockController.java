package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.keyguard.KeyguardViewMediator;

public abstract class FingerprintUnlockController extends KeyguardUpdateMonitorCallback {
    private DozeScrimController mDozeScrimController;
    protected int mGoToSleepType = -1;
    protected Handler mHandler = new Handler();
    private KeyguardViewMediator mKeyguardViewMediator;
    protected int mMode;
    protected boolean mNeedFastOnScreen = false;
    protected boolean mNeedWakeupDevice = false;
    private int mPendingAuthenticatedUserId = -1;
    protected PhoneStatusBar mPhoneStatusBar;
    protected PowerManager mPowerManager;
    private final Runnable mReleaseFingerprintWakeLockRunnable = new Runnable() {
        public void run() {
            Log.i("FingerprintController", "fp wakelock: TIMEOUT!!");
            FingerprintUnlockController.this.releaseFingerprintWakeLock();
        }
    };
    private ScrimController mScrimController;
    protected StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private StatusBarWindowManager mStatusBarWindowManager;
    protected KeyguardUpdateMonitor mUpdateMonitor;
    private WakeLock mWakeLock;

    public abstract boolean isInfastScreenMode();

    public abstract void notifyWakeupDevice();

    public abstract void removeFingerprintMsg();

    public abstract void setAuthSucceeded();

    public FingerprintUnlockController(Context context, StatusBarWindowManager statusBarWindowManager, DozeScrimController dozeScrimController, KeyguardViewMediator keyguardViewMediator, ScrimController scrimController, PhoneStatusBar phoneStatusBar) {
        this.mPowerManager = (PowerManager) context.getSystemService(PowerManager.class);
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        this.mUpdateMonitor.registerCallback(this);
        this.mStatusBarWindowManager = statusBarWindowManager;
        this.mDozeScrimController = dozeScrimController;
        this.mKeyguardViewMediator = keyguardViewMediator;
        this.mScrimController = scrimController;
        this.mPhoneStatusBar = phoneStatusBar;
    }

    public void setStatusBarKeyguardViewManager(StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
    }

    protected void releaseFingerprintWakeLock() {
        if (this.mWakeLock != null) {
            this.mHandler.removeCallbacks(this.mReleaseFingerprintWakeLockRunnable);
            Log.i("FingerprintController", "releasing fp wakelock");
            this.mWakeLock.release();
            this.mWakeLock = null;
        }
    }

    public void onFingerprintAcquired() {
        releaseFingerprintWakeLock();
        if (!this.mUpdateMonitor.isDeviceInteractive()) {
            this.mWakeLock = this.mPowerManager.newWakeLock(1, "wake-and-unlock wakelock");
            this.mWakeLock.acquire();
            Log.i("FingerprintController", "fingerprint acquired, grabbing fp wakelock");
            this.mHandler.postDelayed(this.mReleaseFingerprintWakeLockRunnable, 15000);
            if (this.mDozeScrimController.isPulsing()) {
                this.mStatusBarWindowManager.setForceDozeBrightness(true);
            }
        }
    }

    public void onFingerprintAuthenticated(int userId) {
        if (this.mUpdateMonitor.isGoingToSleep()) {
            this.mPendingAuthenticatedUserId = userId;
            return;
        }
        boolean wasDeviceInteractive = this.mUpdateMonitor.isDeviceInteractive();
        this.mMode = calculateMode();
        Log.i("FingerprintController", "onFingerprintAuthenticated with mMode : " + this.mMode + ", wasDeviceInteractive :" + wasDeviceInteractive);
        if (wasDeviceInteractive) {
            releaseFingerprintWakeLock();
        } else if (1 == this.mMode && this.mNeedFastOnScreen) {
            Log.i("FingerprintController", "fp wakelock: use fast wakeup solution");
            this.mNeedFastOnScreen = false;
            this.mNeedWakeupDevice = true;
        } else {
            Log.i("FingerprintController", "fp wakelock: Authenticated, waking up...");
            this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "android.policy:FINGERPRINT");
            releaseFingerprintWakeLock();
        }
        switch (this.mMode) {
            case 1:
                break;
            case 2:
                this.mPhoneStatusBar.updateMediaMetaData(false, true);
                break;
            case 3:
            case 5:
                if (!wasDeviceInteractive) {
                    this.mStatusBarKeyguardViewManager.notifyDeviceWakeUpRequested();
                }
                this.mStatusBarKeyguardViewManager.animateCollapsePanels(1.3f);
                break;
            case 6:
                this.mStatusBarKeyguardViewManager.notifyKeyguardAuthenticated(false);
                break;
        }
        setAuthSucceeded();
        this.mStatusBarWindowManager.setStatusBarFocusable(false);
        this.mDozeScrimController.abortPulsing();
        this.mKeyguardViewMediator.onWakeAndUnlocking();
        this.mScrimController.setWakeAndUnlocking();
        if (this.mPhoneStatusBar.getNavigationBarView() != null) {
            this.mPhoneStatusBar.getNavigationBarView().setWakeAndUnlocking(true);
        }
        if (this.mMode != 2) {
            this.mStatusBarWindowManager.setForceDozeBrightness(false);
        }
        this.mPhoneStatusBar.notifyFpAuthModeChanged();
    }

    public void onStartedGoingToSleep(int why) {
        this.mPendingAuthenticatedUserId = -1;
        this.mGoToSleepType = why;
    }

    public void onFinishedGoingToSleep(int why) {
        this.mGoToSleepType = -1;
        if (this.mPendingAuthenticatedUserId != -1) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    FingerprintUnlockController.this.onFingerprintAuthenticated(FingerprintUnlockController.this.mPendingAuthenticatedUserId);
                }
            });
        }
        this.mPendingAuthenticatedUserId = -1;
    }

    public int getMode() {
        return this.mMode;
    }

    private int calculateMode() {
        boolean unlockingAllowed = this.mUpdateMonitor.isUnlockingWithFingerprintAllowed();
        if (this.mUpdateMonitor.isDeviceInteractive()) {
            if (this.mStatusBarKeyguardViewManager.isShowing()) {
                if (this.mStatusBarKeyguardViewManager.isBouncerShowing() && unlockingAllowed) {
                    return 6;
                }
                if (unlockingAllowed) {
                    this.mPhoneStatusBar.setBokehChangeStatus(false);
                    return 5;
                } else if (!this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
                    return 3;
                }
            }
            return 0;
        } else if (!this.mStatusBarKeyguardViewManager.isShowing()) {
            return 4;
        } else {
            if (this.mDozeScrimController.isPulsing() && unlockingAllowed) {
                return 2;
            }
            if (unlockingAllowed) {
                return 1;
            }
            return 3;
        }
    }

    public void onFingerprintAuthFailed() {
        cleanup();
    }

    public void onFingerprintError(int msgId, String errString) {
        cleanup();
    }

    private void cleanup() {
        this.mMode = 0;
        releaseFingerprintWakeLock();
        this.mStatusBarWindowManager.setForceDozeBrightness(false);
        this.mPhoneStatusBar.notifyFpAuthModeChanged();
    }

    public void startKeyguardFadingAway() {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                FingerprintUnlockController.this.mStatusBarWindowManager.setForceDozeBrightness(false);
            }
        }, 96);
    }

    public void finishKeyguardFadingAway() {
        this.mPhoneStatusBar.setBokehChangeStatus(true);
        this.mMode = 0;
        if (this.mPhoneStatusBar.getNavigationBarView() != null) {
            this.mPhoneStatusBar.getNavigationBarView().setWakeAndUnlocking(false);
        }
        this.mPhoneStatusBar.notifyFpAuthModeChanged();
    }
}
