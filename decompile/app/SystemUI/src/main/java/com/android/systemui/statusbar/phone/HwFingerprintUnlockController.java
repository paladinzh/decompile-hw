package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemVibrator;
import android.os.Vibrator;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils.RequestThrottledException;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.fingerprint.HwCustFingerprintValidation;
import com.android.systemui.R;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.utils.HwLog;
import com.huawei.cust.HwCustUtils;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.policy.ErrorMessage;
import com.huawei.keyguard.policy.FingerBlackCounter;
import com.huawei.keyguard.policy.FingerPrintPolicy;
import com.huawei.keyguard.policy.RetryPolicy;
import com.huawei.keyguard.policy.RetryPolicy.IRetryPolicy;
import com.huawei.keyguard.support.FingerprintNavigator;
import com.huawei.keyguard.support.HiddenSpace;
import com.huawei.keyguard.support.LauncherInteractiveUtil;
import com.huawei.keyguard.util.FpUtils;
import com.huawei.keyguard.util.KeyguardToast;
import com.huawei.keyguard.util.OsUtils;
import com.huawei.timekeeper.TimeObserver;
import com.huawei.timekeeper.TimeTickInfo;
import fyusion.vislib.BuildConfig;

public class HwFingerprintUnlockController extends FingerprintUnlockController implements TimeObserver {
    private long MAX_DURATION_FROM_DOWN_TO_WAIT = 1000;
    private long MIN_DURATION_FROM_DOWN_TO_DOWN = 500;
    private long MIN_DURATION_FROM_WAIT_TO_DOWN = 300;
    private Context mContext;
    private HwCustFingerprintValidation mCustStub;
    private long mFpDownTimeWhenBlack = 0;
    private long mFpWaitInPutTime = 0;
    private boolean mIsLockout = false;
    private boolean mIsPasswordLockout = false;
    private IRetryPolicy mRetryPolicy = null;
    private Runnable mStateCleaner = new Runnable() {
        public void run() {
            Log.i("FingerprintController", "DO resetFingerprintStatus");
            HwFingerprintUnlockController.this.resetFingerprintStatus();
        }
    };
    private KeyguardToast mToast;
    private Runnable mUserSwitchRun = null;
    private SystemVibrator mVibrator;

    public HwFingerprintUnlockController(Context context, StatusBarWindowManager statusBarWindowManager, DozeScrimController dozeScrimController, KeyguardViewMediator keyguardViewMediator, ScrimController scrimController, PhoneStatusBar phoneStatusBar) {
        super(context, statusBarWindowManager, dozeScrimController, keyguardViewMediator, scrimController, phoneStatusBar);
        this.mContext = context;
        this.mCustStub = (HwCustFingerprintValidation) HwCustUtils.createObj(HwCustFingerprintValidation.class, new Object[]{this.mContext});
        this.mRetryPolicy = RetryPolicy.getFingerPolicy(this.mContext);
        this.mVibrator = (SystemVibrator) ((Vibrator) context.getSystemService("vibrator"));
    }

    private boolean isSecureLockForFinger() {
        return KeyguardSecurityModel.isPINorPasswordorPatternMode(KeyguardSecurityModel.getInst(this.mContext).getSecurityMode());
    }

    public void onFingerprintAcquired(int acquireInfo) {
        boolean z = true;
        long currentTime;
        if (((long) acquireInfo) == 2001) {
            currentTime = System.currentTimeMillis();
            if (KeyguardCfg.isFpPerformanceOpen() && this.MAX_DURATION_FROM_DOWN_TO_WAIT > currentTime - this.mFpDownTimeWhenBlack && this.mPhoneStatusBar.getDropbackViewHideStatus() && HwKeyguardUpdateMonitor.getInstance().shouldShowing()) {
                this.mPhoneStatusBar.showDropbackView();
            }
            this.mFpWaitInPutTime = currentTime;
        } else if (((long) acquireInfo) == 2002) {
            this.mNeedFastOnScreen = isNeedFastScreen();
            if (this.mNeedFastOnScreen) {
                FpUtils.startWakeUpReady(this.mContext);
            }
            if (FingerprintNavigator.isInFingerNavigation(this.mContext)) {
                Log.i("FingerprintController", "onFingerprintAcquired Skip FPAcquire as in navagation");
                KeyguardUpdateMonitor.getInstance(this.mContext).stopListeningForFingerprint();
                FpUtils.doBlackStopWakeUpReady(this.mContext);
                return;
            }
            boolean z2;
            FpUtils.setLastInputFingerTime();
            if (this.mNeedFastOnScreen && this.mStatusBarKeyguardViewManager.isShowing() && KeyguardCfg.isFpPerformanceOpen()) {
                currentTime = System.currentTimeMillis();
                if (this.MIN_DURATION_FROM_DOWN_TO_DOWN < currentTime - this.mFpDownTimeWhenBlack && this.MIN_DURATION_FROM_WAIT_TO_DOWN < currentTime - this.mFpWaitInPutTime) {
                    this.mPhoneStatusBar.hideDropbackView();
                }
                this.mFpDownTimeWhenBlack = currentTime;
            }
            super.onFingerprintAcquired();
            this.mIsPasswordLockout = RetryPolicy.getDefaultPolicy(this.mContext).getRemainingChance() <= 0;
            if (this.mIsPasswordLockout) {
                z2 = true;
            } else {
                z2 = isFingerprintShouldLockout();
            }
            this.mIsLockout = z2;
            if (!this.mIsLockout) {
                if (checkRemaingTimes() > 0) {
                    z = false;
                }
                this.mIsLockout = z;
            }
        }
    }

    public void onFingerprintAuthenticated(final int userId, final int fingerId) {
        this.mNeedWakeupDevice = false;
        final boolean isScreenOff = FpUtils.isScreenOff(this.mContext);
        if (this.mIsLockout) {
            if (this.mIsPasswordLockout && isScreenOff) {
                RetryPolicy.getDefaultPolicy(this.mContext).registerObserver(this);
                Log.d("FingerprintController", "screenoff and passwordlock");
            } else {
                if (isScreenOff) {
                    FpUtils.fingerTurnOnScreen(this.mContext);
                }
                showLockoutMessage(this.mIsPasswordLockout);
            }
            startFingerState();
            Log.i("FingerprintController", "Skip FPAuth as lockout");
            return;
        }
        int currentUid = OsUtils.getCurrentUser();
        if (this.mUpdateMonitor.getStrongAuthTracker().hasUserAuthenticatedSinceBoot()) {
            Log.i("FingerprintController", "currentUid = " + currentUid + " userId=" + userId);
            if (currentUid != userId && (HiddenSpace.isHiddenSpace(this.mContext, userId) || userId == 0)) {
                startSwitchUser(userId, fingerId, isScreenOff);
                return;
            }
        } else if (HiddenSpace.isHiddenSpace(this.mContext, currentUid) && userId == 0) {
            startSwitchUser(userId, fingerId, isScreenOff);
            return;
        }
        Log.i("FingerprintController", "FpPerformance onFingerAuthenticated!");
        this.mMode = 0;
        if (KeyguardUpdateMonitor.getInstance(this.mContext).isInDreamingState()) {
            this.mPhoneStatusBar.dismissKeyguard();
            this.mPhoneStatusBar.awakenDreams();
            this.mHandler.post(new Runnable() {
                public void run() {
                    HwFingerprintUnlockController.this.unlockInDreaming(userId, fingerId);
                    Handler backgroundHandler = GlobalContext.getBackgroundHandler();
                    final boolean z = isScreenOff;
                    backgroundHandler.post(new Runnable() {
                        public void run() {
                            FpUtils.doReporterWhenUnlockFingerprintSucceed(HwFingerprintUnlockController.this.mContext, z);
                        }
                    });
                }
            });
            return;
        }
        super.onFingerprintAuthenticated(userId);
        afterUnlockByFinger(userId, fingerId);
        GlobalContext.getBackgroundHandler().post(new Runnable() {
            public void run() {
                FpUtils.doReporterWhenUnlockFingerprintSucceed(HwFingerprintUnlockController.this.mContext, isScreenOff);
            }
        });
    }

    private void startFingerState() {
        GlobalContext.getUIHandler().post(new Runnable() {
            public void run() {
                KeyguardUpdateMonitor.getInstance(HwFingerprintUnlockController.this.mContext).startListeningForFingerprint();
            }
        });
    }

    private void startSwitchUser(final int userId, final int fingerId, boolean isScreenOff) {
        if (isScreenOff) {
            FpUtils.fingerTurnOnScreen(this.mContext);
        }
        if (HiddenSpace.isCalling(this.mContext)) {
            doVibrate();
            startFingerState();
            return;
        }
        Log.i("FingerprintController", "Switch User By HiddenSpace!");
        HiddenSpace.switchUserForHiddenSpace(this.mContext, userId);
        this.mUserSwitchRun = new Runnable() {
            public void run() {
                HwFingerprintUnlockController.this.unlockByUserSwitch(userId, fingerId);
            }
        };
    }

    private void unlockByUserSwitch(int userId, int fingerId) {
        KeyguardUpdateMonitor.getInstance(this.mContext).onFingerprintAuthenticated(userId, fingerId);
    }

    public void onUserSwitchComplete(int userId) {
        HwLog.e("FingerprintController", "onUserSwitchComplete By HiddenSpace");
        if (this.mUserSwitchRun != null) {
            this.mHandler.post(this.mUserSwitchRun);
            this.mUserSwitchRun = null;
        }
    }

    private void unlockInDreaming(int userId, int fingerId) {
        super.onFingerprintAuthenticated(userId);
        afterUnlockByFinger(userId, fingerId);
        HwLog.w("FingerprintController", "Finger awake dreams");
    }

    private void afterUnlockByFinger(int userId, int fingerId) {
        boolean unlock = false;
        if (this.mMode == 2 || this.mMode == 5 || this.mMode == 1) {
            unlock = true;
        } else if (this.mMode == 6) {
            unlock = true;
        }
        if (unlock) {
            if (this.mStatusBarKeyguardViewManager.isOccluded()) {
                this.mPhoneStatusBar.dismissKeyguard();
            }
            RetryPolicy.getRetryPolicy(this.mContext, 1, userId).resetErrorCount(this.mContext);
            this.mRetryPolicy.resetErrorCount(this.mContext);
            HwLog.d("FingerprintController", "Finger dismissKeyguard" + this.mMode);
        } else {
            HwLog.d("FingerprintController", "Finger maybe skipped in Keyguard. " + this.mMode);
        }
        if (!(unlock || this.mMode == 4)) {
            if (this.mMode == 0) {
                if (this.mStatusBarKeyguardViewManager.isShowing()) {
                }
            }
            cancelMessage();
        }
        FingerprintNavigator.getInst().launchAppForFinger(this.mContext, fingerId);
        LauncherInteractiveUtil.sendUnockedEventByCallProvider(this.mContext);
        cancelMessage();
    }

    public void onFingerprintAuthFailed() {
        this.mNeedWakeupDevice = false;
        super.onFingerprintAuthFailed();
    }

    private boolean doFakeAuth() {
        if (!this.mUpdateMonitor.isDeviceInteractive()) {
            return false;
        }
        KeyguardUpdateMonitor keyguardUpdateMonitor = this.mUpdateMonitor;
        int userId = KeyguardUpdateMonitor.getCurrentUser();
        if (!this.mUpdateMonitor.getStrongAuthTracker().hasUserAuthenticatedSinceBoot()) {
            HwLog.i("FingerprintController", "Do FakeAuth when first startup");
            onFingerprintAuthenticated(userId, -1);
            return true;
        } else if (!this.mUpdateMonitor.getUserHasTrust(userId) || RetryPolicy.getDefaultPolicy(this.mContext).getRemainingChance() <= 0) {
            return false;
        } else {
            HwLog.i("FingerprintController", "Do FakeAuth as trust connected");
            onFingerprintAuthenticated(userId, -1);
            return true;
        }
    }

    public void onFingerprintHelp(int msgId, String helpString) {
        super.onFingerprintHelp(msgId, helpString);
        Log.d("FingerprintController", "FP onFingerprintHelp with msgId is : " + msgId);
        if (msgId == -1) {
            handleFailFingerprintAttempt();
        }
    }

    private void handleFailFingerprintAttempt() {
        boolean isLockout = true;
        boolean isPasswordLockout = false;
        boolean screenOff = FpUtils.isScreenOff(this.mContext);
        if (!screenOff && doFakeAuth()) {
            return;
        }
        if (isSecureLockForFinger()) {
            if (RetryPolicy.getDefaultPolicy(this.mContext).getRemainingChance() <= 0) {
                isPasswordLockout = true;
            }
            if (isPasswordLockout && screenOff) {
                RetryPolicy.getDefaultPolicy(this.mContext).registerObserver(this);
                Log.i("FingerprintController", "Skip FPHelp as lockout in screenoff.");
                return;
            }
            if (!isPasswordLockout) {
                isLockout = isFingerprintShouldLockout();
            }
            if (isLockout) {
                showLockoutMessage(isPasswordLockout);
                if (screenOff) {
                    FpUtils.turnOnScreen(this.mContext);
                }
                Log.i("FingerprintController", "Skip FPHelp as lockout.");
                return;
            }
            HwLockScreenReporter.report(this.mContext, 127, BuildConfig.FLAVOR);
            doVibrate();
            if (screenOff) {
                onNoMatchBlack();
            } else {
                onNoMatchWhite();
            }
            return;
        }
        if (screenOff) {
            doVibrate();
            FpUtils.doBlackStopWakeUpReady(this.mContext);
        } else {
            Log.i("FingerprintController", "onNoMatch isScreenOn ButNotLockedStatus!");
            KeyguardUpdateMonitor.getInstance(this.mContext).stopListeningForFingerprint();
        }
        Log.i("FingerprintController", "Skip FPHelp as not secure locked.");
    }

    private void onNoMatchBlack() {
        boolean hasThirdAppTimeOut = false;
        if (FingerBlackCounter.getVerifyFailCount() == 0) {
            hasThirdAppTimeOut = checkRemaingTimes() <= 0;
        }
        boolean addVerifyFailCount = !hasThirdAppTimeOut ? FingerBlackCounter.addVerifyFailCount() : true;
        Log.d("FingerprintController", "FP NoMatchBlack triger counter: " + addVerifyFailCount);
        if (addVerifyFailCount) {
            FpUtils.fingerTurnOnScreen(this.mContext);
            showLockoutMessage(false);
            if (!(this.mStatusBarKeyguardViewManager.isOccluded() || isInCall())) {
                this.mPhoneStatusBar.dismissKeyguard();
            }
            if (!hasThirdAppTimeOut) {
                resetFPStatusLater(30000);
                return;
            }
            return;
        }
        FpUtils.doBlackStopWakeUpReady(this.mContext);
    }

    private boolean isInCall() {
        TelecomManager tm = (TelecomManager) this.mContext.getSystemService("telecom");
        return tm != null ? tm.isInCall() : false;
    }

    private void onNoMatchWhite() {
        int remainingNum = checkRemaingTimes();
        Log.d("FingerprintController", "FP NoMatchWhite remain: " + remainingNum);
        if (remainingNum <= 0) {
            showLockoutMessage(false);
            return;
        }
        if (!(remainingNum > 2 || this.mStatusBarKeyguardViewManager.isOccluded() || isInCall() || FingerprintNavigator.getAlertDspStatus(this.mContext))) {
            this.mPhoneStatusBar.dismissKeyguard();
        }
        showFailMessage(remainingNum);
    }

    public void onFingerprintError(int msgId, String errString) {
        super.onFingerprintError(msgId, errString);
    }

    public void cancelMessage() {
        if (this.mToast != null) {
            this.mToast.cancel();
            this.mToast = null;
        }
    }

    private void showMessage(String msg) {
        showMessage(msg, 0);
    }

    private void showMessage(String msg, int duration) {
        cancelMessage();
        int toastX = this.mContext.getResources().getDimensionPixelSize(R.dimen.fingerprint_toast_pos_x);
        int toastY = this.mContext.getResources().getDimensionPixelSize(R.dimen.fingerprint_toast_pos_y);
        this.mToast = KeyguardToast.makeText(this.mContext, (CharSequence) msg, duration);
        if (this.mToast != null) {
            this.mToast.setGravity(49, toastX, toastY);
            this.mToast.show();
        }
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                HwFingerprintUnlockController.this.mToast = null;
            }
        }, 3000);
    }

    private void showLockoutMessage(boolean isPswdLock) {
        String msg = ErrorMessage.getFingerFobiddenMessage(this.mContext, isPswdLock);
        if (!TextUtils.isEmpty(msg)) {
            showMessage(msg);
        }
    }

    private void doVibrate() {
        if (!KeyguardCfg.isFrontFpNavigationSupport() || this.mVibrator == null) {
            if (this.mCustStub == null || !this.mCustStub.isVibrationSwitch()) {
                FpUtils.vibrate(this.mContext, 80);
            } else {
                this.mCustStub.startVibrate();
            }
            return;
        }
        this.mVibrator.hwVibrate(null, 14);
    }

    private boolean isNeedFastScreen() {
        boolean z = false;
        if (-1 != this.mGoToSleepType) {
            HwLog.w("FingerprintController", "isNeedFastScreen No need fastScreen");
            return false;
        }
        if (getRemainingNum() > 0 && !FingerBlackCounter.shouldLockout()) {
            z = FpUtils.isScreenOff(this.mContext);
        }
        return z;
    }

    public void showFailMessage(int leftCount) {
        if (4 == leftCount) {
            showMessage(this.mContext.getResources().getQuantityString(R.plurals.emui51_fingerprint_dirty_try_again, leftCount, new Object[]{Integer.valueOf(leftCount)}));
            return;
        }
        showMessage(this.mContext.getResources().getQuantityString(R.plurals.emui40_fingerprint_try_again, leftCount, new Object[]{Integer.valueOf(leftCount)}));
    }

    private int checkRemaingTimes() {
        int remainingNum = getRemainingNum();
        if (remainingNum <= 0) {
            Log.i("FingerprintController", "Fingerprint is suspend with no remaining left");
            long remainingTime = getRemainingTime();
            FingerPrintPolicy.setLockoutDeadline(SystemClock.elapsedRealtime() + remainingTime);
            resetFPStatusLater(remainingTime);
        }
        return remainingNum;
    }

    public long getRemainingTime() {
        try {
            this.mRetryPolicy.checkLockDeadline();
            return 0;
        } catch (RequestThrottledException ex) {
            return (long) ex.getTimeoutMs();
        }
    }

    private boolean isFingerprintShouldLockout() {
        if (FingerBlackCounter.shouldLockout() || FingerPrintPolicy.getLockoutDeadline() > SystemClock.elapsedRealtime()) {
            return true;
        }
        this.mHandler.removeCallbacks(this.mStateCleaner);
        return false;
    }

    private int getRemainingNum() {
        return this.mRetryPolicy.getRemainingChance();
    }

    public void resetFingerprintStatus() {
        cancelMessage();
        this.mRetryPolicy.resetErrorCount(this.mContext);
        this.mHandler.removeCallbacks(this.mStateCleaner);
    }

    private void resetFPStatusLater(long timeToWait) {
        this.mHandler.removeCallbacks(this.mStateCleaner);
        this.mHandler.postDelayed(this.mStateCleaner, timeToWait);
    }

    public void removeFingerprintMsg() {
        this.mHandler.removeCallbacks(this.mStateCleaner);
    }

    public void setAuthSucceeded() {
        FpUtils.setAuthSucceeded(this.mContext);
    }

    public void onTimeFinish() {
        resetFingerprintStatus();
        this.mHandler.post(new Runnable() {
            public void run() {
                RetryPolicy.getDefaultPolicy(HwFingerprintUnlockController.this.mContext).unregisterObserver(HwFingerprintUnlockController.this);
            }
        });
    }

    public void onTimeTick(TimeTickInfo arg0) {
    }

    public void notifyWakeupDevice() {
        if (isInfastScreenMode()) {
            Log.i("FingerprintController", "FpPerformance after onFingerAuthenticated, will wakeup");
            this.mNeedWakeupDevice = false;
            FpUtils.fingerTurnOnScreen(this.mContext);
            releaseFingerprintWakeLock();
        }
    }

    public boolean isInfastScreenMode() {
        return this.mNeedWakeupDevice;
    }
}
