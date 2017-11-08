package com.huawei.keyguard;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.android.internal.widget.LockPatternUtils.RequestThrottledException;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.SecurityMessageDisplay;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.policy.ErrorMessage;
import com.huawei.keyguard.policy.RetryPolicy;
import com.huawei.keyguard.policy.RetryPolicy.IRetryPolicy;
import com.huawei.keyguard.policy.VerifyPolicy;
import com.huawei.keyguard.policy.VerifyPolicy.IVerifyCallBack;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.view.effect.AnimUtils;
import com.huawei.timekeeper.TimeObserver;
import com.huawei.timekeeper.TimeTickInfo;
import fyusion.vislib.BuildConfig;

public abstract class HwKeyguardAbsKeyInputView extends LinearLayout implements TextWatcher, IVerifyCallBack, TimeObserver {
    private Runnable mFastPwdChecker = new Runnable() {
        public void run() {
            HwKeyguardAbsKeyInputView.this.verifyPasswordAndUnlock();
        }
    };
    protected boolean mIsAppendInput = true;
    IRetryPolicy mRetryPolicy;
    protected SecurityMessageDisplay mSecurityMessageDisplay;
    private long mStartUnlockInputTime = -1;

    protected abstract String getPasswordText();

    protected abstract int getPasswordTextViewId();

    protected abstract SecurityMode getSecurityMode();

    protected abstract void onPasswordChecked(int i, boolean z, int i2, boolean z2);

    protected abstract void resetPasswordText(boolean z, boolean z2);

    protected abstract void resetState();

    protected abstract void setLockout(TimeTickInfo timeTickInfo);

    protected abstract void setPasswordEntryEnabled(boolean z);

    protected abstract void setPasswordEntryInputEnabled(boolean z);

    public HwKeyguardAbsKeyInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwKeyguardAbsKeyInputView(Context context) {
        super(context);
    }

    protected IRetryPolicy getDefaultPolicy() {
        IRetryPolicy retryPolicy = RetryPolicy.getDefaultPolicy(this.mContext);
        if (retryPolicy != this.mRetryPolicy && this.mRetryPolicy != null) {
            this.mRetryPolicy.unregisterObserver(this);
            this.mRetryPolicy = retryPolicy;
        } else if (this.mRetryPolicy == null) {
            this.mRetryPolicy = retryPolicy;
        }
        return this.mRetryPolicy;
    }

    public void reset() {
        HwLog.w("HwKeyguardAbsKeyInputView", "Reset");
        if (getSecurityMode() == SecurityMode.SimPuk || getSecurityMode() == SecurityMode.SimPin) {
            resetState();
            return;
        }
        IRetryPolicy retryPolicy = getDefaultPolicy();
        try {
            VerifyPolicy.getInstance(this.mContext).checkUnexecuteError();
            retryPolicy.checkLockDeadline();
            retryPolicy.unregisterObserver(this);
            resetState();
        } catch (RequestThrottledException e) {
            setLockout(retryPolicy.getTimeTickInfo());
            retryPolicy.registerObserver(this);
        }
    }

    protected void verifyPasswordAndUnlock() {
        verifyPasswordAndUnlock(false);
    }

    protected void verifyPasswordAndUnlock(boolean userTrigger) {
        String entry = getPasswordText();
        int pswdLen = entry.length();
        if (pswdLen >= 3) {
            if (pswdLen == 3 && userTrigger) {
                userTrigger = false;
            }
            VerifyPolicy verifier = VerifyPolicy.getInstance(this.mContext);
            boolean forceCheck = userTrigger || entry.length() >= verifier.getMaxPasswordLen();
            if (userTrigger) {
                setPasswordEntryInputEnabled(false);
            }
            verifier.verifyPassword(entry, KeyguardUpdateMonitor.getCurrentUser(), forceCheck, this);
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (isSupportFastVerify()) {
            removeCallbacks(this.mFastPwdChecker);
            if (count == before) {
                HwLog.d("HwKeyguardAbsKeyInputView", "is this is a error input ?");
            } else if (Math.abs(count - before) > 1) {
                HwLog.d("HwKeyguardAbsKeyInputView", "This is a unspect change");
            } else if (count > before) {
                onCharAppend();
            } else if (this.mIsAppendInput) {
                HwLog.v("HwKeyguardAbsKeyInputView", "post mFastPwdChecker onTextChanged");
                post(this.mFastPwdChecker);
                this.mIsAppendInput = false;
            }
        }
    }

    public void afterTextChanged(Editable s) {
    }

    public void onCharAppend() {
        this.mIsAppendInput = true;
        if (this.mStartUnlockInputTime < 0) {
            this.mStartUnlockInputTime = System.currentTimeMillis();
        }
        post(this.mFastPwdChecker);
    }

    public void onCharDel() {
        if (this.mIsAppendInput) {
            HwLog.v("HwKeyguardAbsKeyInputView", "post mFastPwdChecker onCharDel");
            post(this.mFastPwdChecker);
            this.mIsAppendInput = false;
        }
    }

    public void onInputChecked(int userId, int type, int timeoutMs, int delay, boolean isValidPassword, boolean forceCheck) {
        boolean z;
        HwLog.d("HwKeyguardAbsKeyInputView", "!!! onInputChecked" + type + "  " + isValidPassword);
        IRetryPolicy retryPolicy = RetryPolicy.getRetryPolicy(this.mContext, 1);
        if (type > 0) {
            retryPolicy.unregisterAll();
            HwLockScreenReporter.reportUnlockInfo(getContext(), getReportVerifyEventId(), true, this.mStartUnlockInputTime, getSinglehandStatus());
            KeyguardUpdateMonitor.getInstance(this.mContext).reportSuccessfulStrongAuthUnlockAttempt();
            this.mStartUnlockInputTime = -1;
        } else {
            setPasswordEntryInputEnabled(true);
            if (type != -11 && isValidPassword) {
                if (type == -13 || retryPolicy.getRemainingChance() <= 0) {
                    retryPolicy.registerObserver(this);
                    timeoutMs = (int) retryPolicy.getRemainingTime();
                }
                if (timeoutMs > 0 || forceCheck) {
                    resetPasswordText(true, true);
                }
                if (-16 == type) {
                    HwLockScreenReporter.reportUnlockInfo(getContext(), getReportVerifyEventId(), false, this.mStartUnlockInputTime, getSinglehandStatus());
                    this.mStartUnlockInputTime = -1;
                }
            } else {
                return;
            }
        }
        if (type > 0) {
            z = true;
        } else {
            z = false;
        }
        onPasswordChecked(userId, z, timeoutMs, isValidPassword);
    }

    public void onTimeTick(TimeTickInfo info) {
        setPasswordEntryEnabled(false);
        CharSequence msg = ErrorMessage.getTimeoutMessage(this.mContext, SecurityMode.PIN, info);
        if (TextUtils.isEmpty(msg)) {
            this.mSecurityMessageDisplay.setMessage(BuildConfig.FLAVOR, true);
        } else {
            this.mSecurityMessageDisplay.setMessage(msg, true);
        }
    }

    public void onTimeFinish() {
        HwLog.i("HwKeyguardAbsKeyInputView", "Wait finish");
        setPasswordEntryEnabled(true);
        setPasswordEntryInputEnabled(true);
        RetryPolicy.getFingerPolicy(this.mContext).resetErrorCount(this.mContext);
        resetState();
    }

    protected boolean isSupportFastVerify() {
        return true;
    }

    protected boolean startAppearAnimationHw(Runnable endRunnable) {
        return AnimUtils.startEnterSecurityViewAnimation(this, endRunnable);
    }

    protected boolean startDisappearAnimationHw(Runnable endRunnable) {
        return AnimUtils.startExitSecurityViewAnimation((View) this, endRunnable);
    }

    public boolean startRevertAnimation(Runnable endRunnable) {
        return AnimUtils.startRevertSecurityViewAnimation(this, endRunnable);
    }

    public void blockInput() {
        setPasswordEntryInputEnabled(false);
    }

    public void onAuthenticationPending() {
        AnimUtils.startExitSecurityViewAnimation((View) this, 500);
    }

    protected int getReportVerifyEventId() {
        return -1;
    }

    protected int getSinglehandStatus() {
        return 3;
    }
}
