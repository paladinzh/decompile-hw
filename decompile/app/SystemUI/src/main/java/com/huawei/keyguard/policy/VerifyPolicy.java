package com.huawei.keyguard.policy;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.os.SystemProperties;
import com.android.internal.widget.LockPatternUtils.RequestThrottledException;
import com.android.internal.widget.LockPatternView.Cell;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.huawei.android.widget.LockPatternUtilsEx;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.monitor.StateMonitor;
import com.huawei.keyguard.policy.RetryPolicy.IRetryPolicy;
import com.huawei.keyguard.support.HiddenSpace;
import com.huawei.keyguard.support.PrivacyMode;
import com.huawei.keyguard.support.RemoteLockUtils;
import com.huawei.keyguard.theme.KeyguardTheme;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.OsUtils;
import com.huawei.keyguard.util.PasswordCheckExceptionUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class VerifyPolicy {
    private static int sMaxAttempForFirstLock = 5;
    private static VerifyPolicy sVerifyPolicy = null;
    private static long sVerifyTid = -1;
    private Context mContext;
    private Executor mDefualtExecutor = null;
    private EncryptEnvChecker mEncryptEnvChecker = new EncryptEnvChecker();
    private boolean mHasCheckedOnce = false;
    private boolean mIsInHDSpaceSwitching = false;
    private boolean mIsPrivacyModeOn = false;
    private AtomicLong mLastCanceledTime = new AtomicLong(0);
    private long mLastRunningTime = 0;
    private long mLastRunningTimeForPowerOff = 0;
    private long mLastVerifyStartTime = 0;
    private LockPatternUtilsEx mLockPatternUtils;
    private AsyncTask<?, ?, ?> mPendingLockCheck;
    private Runnable mRecheckPassword = null;
    private ResetLockoutDeadlineRunner mResetLockoutDeadlineRunner = new ResetLockoutDeadlineRunner();
    private int mTargetUid = -100;
    private KeyguardUpdateMonitorCallback mUpdaterCallback = new KeyguardUpdateMonitorCallback() {
        public void onUserSwitchComplete(int userId) {
            if (VerifyPolicy.this.mRecheckPassword != null) {
                HiddenSpace.getInstance().setmIsSwitchUserByPassword(true);
                VerifyPolicy.this.mIsInHDSpaceSwitching = false;
                GlobalContext.getBackgroundHandler().post(VerifyPolicy.this.mRecheckPassword);
                VerifyPolicy.this.mRecheckPassword = null;
            }
        }
    };
    private long max_wait_time = 9000;

    public interface IVerifyCallBack {
        void blockInput();

        void onAuthenticationPending();

        void onInputChecked(int i, int i2, int i3, int i4, boolean z, boolean z2);
    }

    private static class EncryptEnvChecker implements Runnable {
        private Runnable mExitAnimStarter;
        private IVerifyCallBack mVerifyCallBack;

        private EncryptEnvChecker() {
            this.mExitAnimStarter = new Runnable() {
                public void run() {
                    if (EncryptEnvChecker.this.mVerifyCallBack != null) {
                        EncryptEnvChecker.this.mVerifyCallBack.onAuthenticationPending();
                    }
                }
            };
        }

        private void setVerifyCallBack(IVerifyCallBack callback) {
            this.mVerifyCallBack = callback;
        }

        public void run() {
            if (this.mVerifyCallBack != null) {
                this.mVerifyCallBack.blockInput();
                GlobalContext.getUIHandler().postDelayed(this.mExitAnimStarter, 200);
            }
        }

        public void finish() {
            HwLog.w("KG_Policy", "EncryptEnvChecker finish.");
            GlobalContext.getUIHandler().removeCallbacks(this);
            GlobalContext.getUIHandler().removeCallbacks(this.mExitAnimStarter);
            this.mVerifyCallBack = null;
        }
    }

    private class ResetLockoutDeadlineRunner implements Runnable {
        private boolean mIsRunning;

        private ResetLockoutDeadlineRunner() {
            this.mIsRunning = false;
        }

        public void run() {
            VerifyPolicy.this.mLockPatternUtils.resetLockoutDeadline();
            synchronized (this) {
                this.mIsRunning = false;
            }
        }

        private void resetLockoutDeadline() {
            synchronized (this) {
                if (this.mIsRunning) {
                    HwLog.e("KG_Policy", "resetLockoutDeadline is Running!!!");
                    return;
                }
                this.mIsRunning = true;
                GlobalContext.getBackgroundHandler().post(this);
            }
        }
    }

    private void checkIfTaskThreadHealth() {
        long nowTime = SystemClock.uptimeMillis();
        if (this.mLastVerifyStartTime != 0 && nowTime > this.mLastVerifyStartTime + this.max_wait_time) {
            HwLog.w("KG_Policy", "VerifyExecutor maybe died TID = " + sVerifyTid + "; last call-" + this.mLastVerifyStartTime);
            if (this.mDefualtExecutor instanceof ExecutorService) {
                HwLog.w("KG_Policy", "Unruning Tasks " + ((ExecutorService) this.mDefualtExecutor).shutdownNow().toString());
            }
            HwLog.dumpThreadStack("Verify-check", sVerifyTid);
            this.mDefualtExecutor = createVerifyExecutor();
        }
        long lastCanceledTime = this.mLastCanceledTime.get();
        if (lastCanceledTime != 0 && nowTime > this.max_wait_time + lastCanceledTime) {
            HwLog.w("KG_Policy", "Last canceled time : " + lastCanceledTime + "; now time : " + nowTime);
            PasswordCheckExceptionUtil.sendPwdCheckException(this.mContext, 3);
        }
        this.mLastCanceledTime.set(0);
    }

    private static Executor createVerifyExecutor() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            public void run() {
                VerifyPolicy.sVerifyTid = Thread.currentThread().getId();
            }
        });
        return executor;
    }

    public VerifyPolicy(Context context) {
        this.mContext = context;
        this.mLockPatternUtils = new LockPatternUtilsEx(context);
        this.mPendingLockCheck = null;
        this.mDefualtExecutor = createVerifyExecutor();
        KeyguardUpdateMonitor.getInstance(context).registerCallback(this.mUpdaterCallback);
    }

    private IRetryPolicy getDefaultRetryPolicy() {
        return RetryPolicy.getDefaultPolicy(this.mContext);
    }

    private IRetryPolicy getUsersDefaultRetryPolicy(int userId) {
        return RetryPolicy.getRetryPolicy(this.mContext, 1, userId);
    }

    public static VerifyPolicy getInstance(Context context) {
        VerifyPolicy verifyPolicy;
        synchronized (VerifyPolicy.class) {
            if (sVerifyPolicy == null) {
                sVerifyPolicy = new VerifyPolicy(context.getApplicationContext());
            }
            verifyPolicy = sVerifyPolicy;
        }
        return verifyPolicy;
    }

    public int verifyPassword(String entry, int userId, boolean forceCheck, IVerifyCallBack callback) {
        if (this.mPendingLockCheck != null) {
            this.mPendingLockCheck.cancel(false);
            HwLog.v("KG_Policy", "Have a unfinished checker in runing");
        }
        checkIfTaskThreadHealth();
        this.mPendingLockCheck = checkPassword(entry, userId, forceCheck, callback);
        return 0;
    }

    private boolean showProgressWhileDecrypt() {
        if (this.mHasCheckedOnce) {
            return false;
        }
        return HwKeyguardUpdateMonitor.getInstance(this.mContext).isFirstTimeStartupAndEncrypted();
    }

    public LockPatternUtilsEx getLockPatternUtils() {
        return this.mLockPatternUtils;
    }

    private boolean checkIsNeedToSwitch(int userId) {
        if (!SystemProperties.getBoolean("ro.config.hw_privacySpace", true)) {
            return false;
        }
        if (userId < 0) {
            HwLog.e("KG_Policy", "user id is illegal");
            return false;
        }
        HiddenSpace hiddenSp = HiddenSpace.getInstance();
        boolean hasAuthenticate = hiddenSp.ismHasAuthenticate();
        int privateUserId = hiddenSp.getmPrivateUserId();
        if (KeyguardTheme.getInst().getLockStyle() != 5 && (hasAuthenticate || userId == privateUserId)) {
            SecurityMode privateUserSecurityMode = hiddenSp.getmPrivateSecurityMode();
            SecurityMode OwnerSecurityMode = hiddenSp.getmOwnerSecurityMode();
            if (!(OwnerSecurityMode == SecurityMode.None || privateUserSecurityMode == SecurityMode.None || privateUserSecurityMode != OwnerSecurityMode)) {
                HwLog.e("KG_Policy", "mPrivateUserId=" + privateUserId + " mPrivateSecurityMode=" + privateUserSecurityMode + " mOwnerSecurityMode=" + OwnerSecurityMode + "mHasAuthenticate =" + hasAuthenticate);
                if (userId == 0 && privateUserId != -100) {
                    this.mTargetUid = privateUserId;
                    return true;
                } else if (userId == privateUserId) {
                    this.mTargetUid = 0;
                    return true;
                }
            }
        }
        return false;
    }

    private AsyncTask<?, ?, ?> checkPassword(String password, int userId, boolean force, IVerifyCallBack callback) {
        final boolean showProgress = showProgressWhileDecrypt();
        if (showProgress) {
            KeyguardCfg.setVerifyingStatus(true);
            this.mEncryptEnvChecker.finish();
        }
        final IVerifyCallBack iVerifyCallBack = callback;
        final String str = password;
        final int i = userId;
        final boolean z = force;
        AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
            boolean mGateKeeperMaybeTimeout = false;
            IRetryPolicy mRetryPolicy = RetryPolicy.getDefaultPolicy(VerifyPolicy.this.mContext);
            private int mThrottleTimeout;

            protected Integer doInBackground(Void... args) {
                try {
                    VerifyPolicy.this.mLastVerifyStartTime = SystemClock.uptimeMillis();
                    if (showProgress) {
                        VerifyPolicy.this.mEncryptEnvChecker.setVerifyCallBack(iVerifyCallBack);
                        GlobalContext.getUIHandler().postDelayed(VerifyPolicy.this.mEncryptEnvChecker, 250);
                    }
                    return doInSecurityBackground();
                } catch (SecurityException e) {
                    HwLog.e("KG_Policy", "checkPassword got SecurityException.", e);
                    return Integer.valueOf(-10);
                } catch (Exception e2) {
                    HwLog.e("KG_Policy", "checkPassword got exception.", e2);
                    return Integer.valueOf(-10);
                }
            }

            private Integer doInSecurityBackground() {
                boolean z = false;
                VerifyPolicy.this.mTargetUid = -100;
                try {
                    this.mRetryPolicy.checkLockDeadline();
                    if (!OsUtils.isOwner() && RemoteLockUtils.isDeviceRemoteLocked(VerifyPolicy.this.mContext)) {
                        PasswordCheckExceptionUtil.sendPwdCheckException(VerifyPolicy.this.mContext, 1);
                        return Integer.valueOf(-12);
                    } else if (str.length() < 4) {
                        return Integer.valueOf(-11);
                    } else {
                        if (this.mRetryPolicy.getRemainingChance() <= 0) {
                            HwLog.w("KG_Policy", "Try verify with no chance remaining");
                            PasswordCheckExceptionUtil.sendPwdCheckException(VerifyPolicy.this.mContext, 2);
                            return Integer.valueOf(-13);
                        } else if (isCancelled()) {
                            VerifyPolicy.this.mLastCanceledTime.set(SystemClock.uptimeMillis());
                            return Integer.valueOf(-15);
                        } else {
                            this.mGateKeeperMaybeTimeout = true;
                            if (VerifyPolicy.this.mLockPatternUtils.checkPassword(str, i)) {
                                StateMonitor.getInst().triggerEvent(101);
                                VerifyPolicy.this.mHasCheckedOnce = true;
                                return Integer.valueOf(10);
                            }
                            if (!VerifyPolicy.this.mIsInHDSpaceSwitching && VerifyPolicy.this.checkIsNeedToSwitch(i)) {
                                HwLog.e("KG_Policy", "check password need to switch user, mTargetUid" + VerifyPolicy.this.mTargetUid);
                                if (VerifyPolicy.this.mLockPatternUtils.checkPassword(str, VerifyPolicy.this.mTargetUid)) {
                                    VerifyPolicy.this.mIsInHDSpaceSwitching = true;
                                    return Integer.valueOf(12);
                                }
                            }
                            VerifyPolicy verifyPolicy = VerifyPolicy.this;
                            if (i == 0) {
                                z = PrivacyMode.isPrivacyModeOn(VerifyPolicy.this.mContext);
                            }
                            verifyPolicy.mIsPrivacyModeOn = z;
                            if (!VerifyPolicy.this.mIsPrivacyModeOn || !VerifyPolicy.this.mLockPatternUtils.checkPassword(str, PrivacyMode.getPrivateUserId(VerifyPolicy.this.mContext))) {
                                return Integer.valueOf(-10);
                            }
                            HwLog.d("KG_PrivacyMode", "checkPassword for guest succ");
                            return Integer.valueOf(11);
                        }
                    }
                } catch (RequestThrottledException ex) {
                    this.mThrottleTimeout = ex.getTimeoutMs();
                    return Integer.valueOf(-13);
                }
            }

            private int handleGatekeeperLockout(int timeout) {
                if (timeout <= 0) {
                    return -10;
                }
                HwLog.w("KG_Policy", "GK timeout " + timeout);
                this.mRetryPolicy.trigerLockout((long) timeout);
                HwLockScreenReporter.reportVerifyTimeOut(VerifyPolicy.this.mContext, (long) timeout, 0);
                return -13;
            }

            protected void onPostExecute(Integer result) {
                int i = 0;
                if (KeyguardCfg.isVerifying()) {
                    KeyguardCfg.setVerifyingStatus(false);
                }
                VerifyPolicy.this.mPendingLockCheck = null;
                VerifyPolicy.this.mLastRunningTime = SystemClock.uptimeMillis() - VerifyPolicy.this.mLastVerifyStartTime;
                VerifyPolicy.this.mLastVerifyStartTime = 0;
                HwLog.i("KG_Policy", "Verify: " + i + " ret: " + result + "; using: " + VerifyPolicy.this.mLastRunningTime);
                if (showProgress) {
                    VerifyPolicy.this.mEncryptEnvChecker.finish();
                }
                int delay = 0;
                boolean isValidPassword = true;
                if (result.intValue() > 0) {
                    if (!isCancelled()) {
                        if (result.intValue() == 12) {
                            HiddenSpace.switchUserForHiddenSpace(VerifyPolicy.this.mContext, VerifyPolicy.this.mTargetUid);
                            if (VerifyPolicy.this.mRecheckPassword == null) {
                                VerifyPolicy verifyPolicy = VerifyPolicy.this;
                                final String str = str;
                                final boolean z = z;
                                final IVerifyCallBack iVerifyCallBack = iVerifyCallBack;
                                verifyPolicy.mRecheckPassword = new Runnable() {
                                    public void run() {
                                        VerifyPolicy.this.mPendingLockCheck = VerifyPolicy.this.checkPassword(str, OsUtils.getCurrentUser(), z, iVerifyCallBack);
                                    }
                                };
                            }
                            VerifyPolicy.this.mTargetUid = -100;
                            return;
                        }
                        boolean z2;
                        VerifyPolicy.this.updatePermanetLock();
                        VerifyPolicy.this.resetLockoutForRestorePattern();
                        VerifyPolicy.this.reportSuccedUnlockAttempt();
                        VerifyPolicy verifyPolicy2 = VerifyPolicy.this;
                        if (result.intValue() == 11) {
                            z2 = true;
                        }
                        delay = verifyPolicy2.sendPrivacyBroadcast(z2);
                    }
                } else if (result.intValue() == -12 || result.intValue() == -15) {
                    isValidPassword = false;
                } else if (result.intValue() == -13 && this.mGateKeeperMaybeTimeout) {
                    handleGatekeeperLockout(this.mThrottleTimeout);
                } else {
                    IRetryPolicy iRetryPolicy = this.mRetryPolicy;
                    int length = str.length();
                    if (!z) {
                        i = str.hashCode();
                    }
                    if (iRetryPolicy.isThinkAsFail(length, i)) {
                        try {
                            VerifyPolicy.this.reportFailedUnlockAttempt(i);
                            result = Integer.valueOf(result.intValue() == -11 ? -14 : -16);
                        } catch (IllegalStateException e) {
                            HwLog.w("KG_Policy", "Add Failcount fail IllegalStateException.");
                            result = Integer.valueOf(-13);
                        }
                    } else {
                        isValidPassword = false;
                    }
                }
                iVerifyCallBack.onInputChecked(i, result.intValue(), this.mThrottleTimeout, delay, isValidPassword, z);
            }
        };
        task.executeOnExecutor(this.mDefualtExecutor, new Void[0]);
        return task;
    }

    public void checkUnexecuteError() {
        int uID = OsUtils.getCurrentUser();
        if (getUsersDefaultRetryPolicy(uID).hasUnexecuteError()) {
            reportFailedUnlockAttempt(uID);
        }
    }

    public int verifyPatten(List<Cell> pattern, int userId, IVerifyCallBack callback) {
        if (this.mPendingLockCheck != null) {
            this.mPendingLockCheck.cancel(false);
            HwLog.w("KG_Policy", "verifyPatten maybe blocked");
        }
        checkIfTaskThreadHealth();
        this.mPendingLockCheck = checkPatten(pattern, userId, callback);
        return 0;
    }

    private AsyncTask<?, ?, ?> checkPatten(List<Cell> pattern, int userId, IVerifyCallBack callback) {
        final boolean showProgress = showProgressWhileDecrypt();
        if (showProgress) {
            KeyguardCfg.setVerifyingStatus(true);
            this.mEncryptEnvChecker.finish();
        }
        final IVerifyCallBack iVerifyCallBack = callback;
        final List<Cell> list = pattern;
        final int i = userId;
        AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
            boolean mGateKeeperMaybeTimeout = false;
            IRetryPolicy mRetryPolicy = RetryPolicy.getDefaultPolicy(VerifyPolicy.this.mContext);
            private int mThrottleTimeout;

            protected Integer doInBackground(Void... args) {
                try {
                    VerifyPolicy.this.mLastVerifyStartTime = SystemClock.uptimeMillis();
                    if (showProgress) {
                        VerifyPolicy.this.mEncryptEnvChecker.setVerifyCallBack(iVerifyCallBack);
                        GlobalContext.getUIHandler().postDelayed(VerifyPolicy.this.mEncryptEnvChecker, 250);
                    }
                    return doInSecurityBackground(new Void[0]);
                } catch (SecurityException e) {
                    HwLog.e("KG_Policy", "checkPassword got SecurityException.", e);
                    return Integer.valueOf(-10);
                } catch (Exception e2) {
                    HwLog.e("KG_Policy", "checkPassword got exception.", e2);
                    return Integer.valueOf(-10);
                }
            }

            private Integer doInSecurityBackground(Void... args) {
                VerifyPolicy.this.mTargetUid = -100;
                try {
                    if (OsUtils.isOwner() || !RemoteLockUtils.isDeviceRemoteLocked(VerifyPolicy.this.mContext)) {
                        this.mRetryPolicy.checkLockDeadline();
                        if (list.size() < 4) {
                            return Integer.valueOf(-11);
                        }
                        this.mGateKeeperMaybeTimeout = true;
                        try {
                            if (VerifyPolicy.this.mLockPatternUtils.checkPattern(list, OsUtils.getCurrentUser())) {
                                StateMonitor.getInst().triggerEvent(102);
                                return Integer.valueOf(10);
                            }
                            if (!VerifyPolicy.this.mIsInHDSpaceSwitching && VerifyPolicy.this.checkIsNeedToSwitch(i)) {
                                HwLog.e("KG_Policy", "check pattern need to switch user, mTargetUid=" + VerifyPolicy.this.mTargetUid);
                                if (VerifyPolicy.this.mLockPatternUtils.checkPattern(list, VerifyPolicy.this.mTargetUid)) {
                                    VerifyPolicy.this.mIsInHDSpaceSwitching = true;
                                    return Integer.valueOf(12);
                                }
                            }
                            return Integer.valueOf(-10);
                        } catch (RequestThrottledException ex) {
                            HwLog.e("KG_Policy", "onPatternDetected RequestThrottledException " + ex.getTimeoutMs());
                        }
                    } else {
                        PasswordCheckExceptionUtil.sendPwdCheckException(VerifyPolicy.this.mContext, 1);
                        return Integer.valueOf(-12);
                    }
                } catch (RequestThrottledException ex2) {
                    this.mThrottleTimeout = ex2.getTimeoutMs();
                    return Integer.valueOf(-13);
                }
            }

            private int handleGatekeeperLockout(int timeout) {
                if (timeout <= 0) {
                    return -10;
                }
                HwLog.w("KG_Policy", "GK timeout " + timeout);
                this.mRetryPolicy.trigerLockout((long) timeout);
                HwLockScreenReporter.reportVerifyTimeOut(VerifyPolicy.this.mContext, (long) timeout, 0);
                return -13;
            }

            protected void onPostExecute(Integer result) {
                if (KeyguardCfg.isVerifying()) {
                    KeyguardCfg.setVerifyingStatus(false);
                }
                VerifyPolicy.this.mPendingLockCheck = null;
                VerifyPolicy.this.mLastRunningTime = SystemClock.uptimeMillis() - VerifyPolicy.this.mLastVerifyStartTime;
                VerifyPolicy.this.mLastVerifyStartTime = 0;
                HwLog.i("KG_Policy", "Verify: " + (result.intValue() < 0 ? "FAIL" : "SUCC") + "; using: " + VerifyPolicy.this.mLastRunningTime);
                if (showProgress) {
                    VerifyPolicy.this.mEncryptEnvChecker.finish();
                }
                int delay = 0;
                boolean validInput = list.size() >= 4;
                if (result.intValue() > 0) {
                    if (result.intValue() == 12) {
                        HiddenSpace.switchUserForHiddenSpace(VerifyPolicy.this.mContext, VerifyPolicy.this.mTargetUid);
                        if (VerifyPolicy.this.mRecheckPassword == null) {
                            VerifyPolicy verifyPolicy = VerifyPolicy.this;
                            final List list = list;
                            final IVerifyCallBack iVerifyCallBack = iVerifyCallBack;
                            verifyPolicy.mRecheckPassword = new Runnable() {
                                public void run() {
                                    VerifyPolicy.this.mPendingLockCheck = VerifyPolicy.this.checkPatten(list, OsUtils.getCurrentUser(), iVerifyCallBack);
                                }
                            };
                        }
                        VerifyPolicy.this.mTargetUid = -100;
                        return;
                    }
                    boolean z;
                    VerifyPolicy.this.updatePermanetLock();
                    VerifyPolicy.this.reportSuccedUnlockAttempt();
                    VerifyPolicy verifyPolicy2 = VerifyPolicy.this;
                    if (result.intValue() == 11) {
                        z = true;
                    } else {
                        z = false;
                    }
                    delay = verifyPolicy2.sendPrivacyBroadcast(z);
                } else if (result.intValue() == -12) {
                    HwLog.d("KG_Policy", "Patten disabled when remote locked");
                } else {
                    if (result.intValue() == -13 && this.mGateKeeperMaybeTimeout) {
                        handleGatekeeperLockout(this.mThrottleTimeout);
                    }
                    if (validInput) {
                        VerifyPolicy.this.reportFailedUnlockAttempt(i);
                    }
                }
                iVerifyCallBack.onInputChecked(i, result.intValue(), this.mThrottleTimeout, delay, validInput, true);
            }
        };
        task.executeOnExecutor(this.mDefualtExecutor, new Void[0]);
        return task;
    }

    private int sendPrivacyBroadcast(boolean isGuest) {
        if (this.mIsPrivacyModeOn) {
            return PrivacyMode.sendPrivacyBroadcast(this.mContext, isGuest);
        }
        return 0;
    }

    private void updatePermanetLock() {
        this.mContext.getSharedPreferences("lock_preferences", 0).edit().putBoolean("permanentlylocked", false).apply();
    }

    private void clearFailedAttempts() {
        HwLog.i("KG_Policy", "clearFailedAttempts");
        OsUtils.putSecureInt(this.mContext, "keyguard_verify_failed_attempts", 0);
    }

    private void addFailedAttempts() {
        int failedAttempts = OsUtils.getSecureInt(this.mContext, "keyguard_verify_failed_attempts", 0);
        HwLog.i("KG_Policy", "addFailedAttempts ++" + failedAttempts);
        OsUtils.putSecureInt(this.mContext, "keyguard_verify_failed_attempts", failedAttempts + 1);
    }

    public int getFailedAttempts() {
        return OsUtils.getSecureInt(this.mContext, "keyguard_verify_failed_attempts", 0);
    }

    public void clearFailedUnlockAttempts() {
        ArrayList<IRetryPolicy> ret = RetryPolicy.getUserPolicies(OsUtils.getCurrentUser());
        for (int i = 0; i < ret.size(); i++) {
            ((IRetryPolicy) ret.get(i)).resetErrorCount(this.mContext);
        }
        clearFailedAttempts();
    }

    public int getFailedUnlockAttempts() {
        return getDefaultRetryPolicy().getErrorCount();
    }

    public void reportFailedUnlockAttempt(int userId) {
        try {
            int failedAttemptsBeforeWipe = getMaximumFailedForWipe(userId);
            if (failedAttemptsBeforeWipe > 0) {
                addFailedAttempts();
            }
            IRetryPolicy policy = getUsersDefaultRetryPolicy(userId);
            if (policy.getErrorCount() == 0 && failedAttemptsBeforeWipe > 0) {
                policy.setDpmMaxFailed(failedAttemptsBeforeWipe);
                for (int failsBefore = getCurrentFailedPasswordAttempts(userId); failsBefore > 0; failsBefore--) {
                    policy.addErrorCount();
                }
            }
            policy.addErrorCount();
            if (policy.getRemainingChance() <= 0) {
                long toWait = policy.getRemainingTime();
                if (toWait > 0) {
                    HwLockScreenReporter.reportVerifyTimeOut(this.mContext, toWait, policy.getErrorCount());
                }
            }
        } catch (IllegalStateException e) {
            HwLog.e("KG_Policy", "reportFailedUnlockAttempt fail.", e);
        }
    }

    public int getCurrentFailedPasswordAttempts(int userId) {
        try {
            return this.mLockPatternUtils.getCurrentFailedPasswordAttempts(userId);
        } catch (SecurityException e) {
            HwLog.w("KG_Policy", "getCurrentFailedPasswordAttempts fail. ", e);
            return 0;
        }
    }

    public void reportSuccedUnlockAttempt() {
        clearFailedUnlockAttempts();
        this.mLockPatternUtils.reportSuccessfulPasswordAttempt(OsUtils.getCurrentUser());
        FingerPrintPolicy fpp = (FingerPrintPolicy) RetryPolicy.getFingerPolicy(this.mContext);
        fpp.resetErrorCount(this.mContext);
        fpp.setFingerOpened(OsUtils.getCurrentUser());
    }

    public int getMaximumFailedForWipe(int userId) {
        return this.mLockPatternUtils.getDevicePolicyManager().getMaximumFailedPasswordsForWipe(null, userId);
    }

    public int getMaxPasswordLen() {
        return 16;
    }

    private void resetLockoutForRestorePattern() {
        if (RetryPolicy.getDefaultPolicy(this.mContext).getErrorCount() > 0 && this.mLockPatternUtils.isLockPatternEnabled(OsUtils.getCurrentUser())) {
            resetLockoutDeadline();
        }
    }

    public void resetLockoutDeadline() {
        this.mResetLockoutDeadlineRunner.resetLockoutDeadline();
    }

    public static int getMaxPattenAttempBeforeRestore() {
        return sMaxAttempForFirstLock;
    }

    public boolean isLongTimeDecrypt() {
        boolean isLongDecrypt = this.mLastRunningTime > 500;
        this.mLastRunningTimeForPowerOff = this.mLastRunningTime;
        this.mLastRunningTime = 0;
        return isLongDecrypt;
    }

    public boolean isLongTimeDecryptForPowerOff() {
        boolean isLongDecrypt = this.mLastRunningTimeForPowerOff > 500;
        this.mLastRunningTimeForPowerOff = 0;
        return isLongDecrypt;
    }
}
