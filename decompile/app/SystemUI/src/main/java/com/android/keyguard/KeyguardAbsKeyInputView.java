package com.android.keyguard;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.EmergencyButton.EmergencyButtonCallback;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.HwKeyguardAbsKeyInputView;
import com.huawei.keyguard.policy.ErrorMessage;
import com.huawei.keyguard.util.HwLog;
import com.huawei.timekeeper.TimeTickInfo;

public abstract class KeyguardAbsKeyInputView extends HwKeyguardAbsKeyInputView implements KeyguardSecurityView, EmergencyButtonCallback {
    private static int sCancelledCount = 0;
    protected KeyguardSecurityCallback mCallback;
    private boolean mDismissing;
    protected View mEcaView;
    protected boolean mEnableHaptics;
    protected LockPatternUtils mLockPatternUtils;
    protected AsyncTask<?, ?, ?> mPendingLockCheck;
    private int mSubscription;

    protected abstract int getPromtReasonStringRes(int i);

    public KeyguardAbsKeyInputView(Context context) {
        this(context, null);
    }

    public KeyguardAbsKeyInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSubscription = -1;
    }

    public void setKeyguardCallback(KeyguardSecurityCallback callback) {
        this.mCallback = callback;
    }

    public void setLockPatternUtils(LockPatternUtils utils) {
        this.mLockPatternUtils = utils;
        this.mEnableHaptics = this.mLockPatternUtils.isTactileFeedbackEnabled();
    }

    public void reset() {
        this.mDismissing = false;
        resetPasswordText(false, false);
        super.reset();
    }

    protected void setLockout(TimeTickInfo info) {
        HwLog.i("KeyguardAbsKeyInputView", "KeyguardInputView Is lockout");
        if (info != null) {
            if (info.getMinute() <= 0 && info.getHour() <= 0) {
                if (info.getSecond() > 0) {
                }
            }
            this.mSecurityMessageDisplay.setMessage(ErrorMessage.getTimeoutMessage(this.mContext, getSecurityMode(), info), true);
        }
        setPasswordEntryEnabled(false);
        setPasswordEntryInputEnabled(false);
    }

    protected void onFinishInflate() {
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mSecurityMessageDisplay = KeyguardMessageArea.findSecurityMessageDisplay(this);
        this.mEcaView = findViewById(R$id.keyguard_selector_fade_container);
        EmergencyButton button = (EmergencyButton) findViewById(R$id.emergency_call_button);
        if (button != null) {
            button.setCallback(this);
        }
    }

    public void onEmergencyButtonClickedWhenInCall() {
        this.mCallback.reset();
    }

    protected int getWrongPasswordStringId() {
        return R$string.kg_wrong_password;
    }

    protected String getWrongPasswordString() {
        return getResources().getString(getWrongPasswordStringId());
    }

    protected void verifyPasswordAndUnlock() {
        if (!this.mDismissing) {
            super.verifyPasswordAndUnlock();
        }
    }

    protected void onPasswordChecked(int userId, boolean matched, int timeoutMs, boolean isValidPassword) {
        boolean dismissKeyguard = KeyguardUpdateMonitor.getCurrentUser() == userId;
        if (matched) {
            this.mCallback.reportUnlockAttempt(userId, true, 0);
            if (dismissKeyguard) {
                this.mDismissing = true;
                this.mCallback.dismiss(true);
                return;
            }
            return;
        }
        if (isValidPassword) {
            this.mCallback.reportUnlockAttempt(userId, false, timeoutMs);
            if (timeoutMs > 0) {
                long lockoutAttemptDeadline = this.mLockPatternUtils.setLockoutAttemptDeadline(userId, timeoutMs);
            }
        }
        if (timeoutMs == 0) {
            this.mSecurityMessageDisplay.setMessage(getWrongPasswordString(), true);
        }
    }

    protected void onUserInput() {
        if (this.mCallback != null) {
            this.mCallback.userActivity();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        onUserInput();
        return false;
    }

    public boolean needsInput() {
        return false;
    }

    public void onPause() {
        if (this.mPendingLockCheck != null) {
            this.mPendingLockCheck.cancel(false);
            this.mPendingLockCheck = null;
        }
    }

    public void onResume(int reason) {
        reset();
    }

    public void showPromptReason(int reason) {
        if (!(reason == 11 || reason == 0)) {
            int promtReasonStringRes = getPromtReasonStringRes(reason);
            if (promtReasonStringRes != 0) {
                this.mSecurityMessageDisplay.setMessage(promtReasonStringRes, true);
            }
        }
    }

    public void showMessage(String message, int color) {
        this.mSecurityMessageDisplay.setNextMessageColor(color);
        this.mSecurityMessageDisplay.setMessage((CharSequence) message, true);
    }

    public void doHapticKeyClick() {
        if (this.mEnableHaptics) {
            performHapticFeedback(1, 3);
        }
    }

    public boolean startDisappearAnimation(Runnable finishRunnable) {
        return false;
    }

    public void closeKeyGuard(boolean bAuthenticated, String type) {
        KeyguardUpdateMonitor updateMonitor = KeyguardUpdateMonitor.getInstance(getContext());
        int numCardsConfigured = 0;
        int numPinLocked = 0;
        int numPhones = GlobalContext.getTelephonyManager(this.mContext).getPhoneCount();
        for (int i = 0; i < numPhones; i++) {
            State simState = updateMonitor.getSimState(i);
            if (simState == State.PIN_REQUIRED || simState == State.PUK_REQUIRED) {
                numPinLocked++;
            }
            if (!(simState == State.READY || simState == State.PIN_REQUIRED)) {
                if (simState != State.PUK_REQUIRED) {
                }
            }
            numCardsConfigured++;
        }
        if (!bAuthenticated) {
            if (sCancelledCount < numCardsConfigured - 1) {
                setCancelCount(sCancelledCount + 1);
            } else {
                return;
            }
        }
        if (numPinLocked <= 1) {
            setCancelCount(0);
        }
        if (!bAuthenticated) {
            if (type.equals("simPin")) {
                this.mSubscription = updateMonitor.getNextSubIdForState(State.PIN_REQUIRED);
            } else {
                this.mSubscription = updateMonitor.getNextSubIdForState(State.PUK_REQUIRED);
            }
        }
        if (this.mSubscription >= 0) {
            updateMonitor.reportSimUnlocked(this.mSubscription);
            updateMonitor.setCancelSubscriptId(this.mSubscription);
        }
        this.mCallback.dismiss(true);
        if (!bAuthenticated && numPinLocked > 1) {
            resetState();
        }
    }

    public static void setCancelCount(int cancelCount) {
        sCancelledCount = cancelCount;
    }
}
