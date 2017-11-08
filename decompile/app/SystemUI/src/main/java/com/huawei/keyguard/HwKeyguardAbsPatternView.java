package com.huawei.keyguard;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.android.keyguard.KeyguardSecurityCallback;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.R$id;
import com.android.keyguard.R$string;
import com.android.keyguard.SecurityMessageDisplay;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.policy.ErrorMessage;
import com.huawei.keyguard.policy.RetryPolicy;
import com.huawei.keyguard.policy.RetryPolicy.IRetryPolicy;
import com.huawei.keyguard.policy.VerifyPolicy;
import com.huawei.keyguard.policy.VerifyPolicy.IVerifyCallBack;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.LockPatternUtilsEx;
import com.huawei.keyguard.view.effect.AnimUtils;
import com.huawei.timekeeper.TimeObserver;
import com.huawei.timekeeper.TimeTickInfo;
import fyusion.vislib.BuildConfig;

public abstract class HwKeyguardAbsPatternView extends LinearLayout implements OnClickListener, IVerifyCallBack, TimeObserver {
    protected static final boolean mBkPinEnabled = SystemProperties.getBoolean("ro.keyguard.hwbkpin", true);
    protected KeyguardSecurityCallback mCallback;
    protected Button mEmergencyCallButton;
    protected Button mForgotPatternButton;
    protected Button mGoBackButton;
    private LockPatternUtilsEx mLockPatternUtilsEx;
    protected IRetryPolicy mRetryPolicy;
    protected SecurityMessageDisplay mSecurityMessageDisplay;
    protected long mStartUnlockInputTime;
    private int mStyle;

    protected abstract void onPatternChecked(int i, boolean z, int i2, boolean z2);

    protected abstract void setPattenEnabled(boolean z);

    public HwKeyguardAbsPatternView(Context context) {
        this(context, null);
    }

    public HwKeyguardAbsPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mRetryPolicy = null;
        this.mStyle = 0;
        this.mStartUnlockInputTime = -1;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mLockPatternUtilsEx = this.mLockPatternUtilsEx == null ? new LockPatternUtilsEx(this.mContext) : this.mLockPatternUtilsEx;
        this.mRetryPolicy = RetryPolicy.getDefaultPolicy(this.mContext);
        View buttonView = findViewById(R$id.forgot_password_button);
        if (buttonView instanceof Button) {
            buttonView = (Button) buttonView;
        } else {
            buttonView = null;
        }
        this.mForgotPatternButton = buttonView;
        buttonView = findViewById(R$id.emergency_call_button);
        if (buttonView instanceof Button) {
            buttonView = (Button) buttonView;
        } else {
            buttonView = null;
        }
        this.mEmergencyCallButton = buttonView;
        buttonView = findViewById(R$id.back_to_keyguard);
        if (buttonView instanceof Button) {
            buttonView = (Button) buttonView;
        } else {
            buttonView = null;
        }
        this.mGoBackButton = buttonView;
        if (this.mForgotPatternButton != null) {
            if (mBkPinEnabled) {
                this.mForgotPatternButton.setText(R$string.kg_bkup_pin);
            } else {
                this.mForgotPatternButton.setText(R$string.kg_forgot_pattern_button_text);
            }
            this.mForgotPatternButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            this.mForgotPatternButton.setOnClickListener(this);
        }
    }

    public void onClick(View v) {
        if (v.getId() == R$id.forgot_password_button) {
            HwLog.w("AbsKeyguardPatternView", this.mCallback == null ? "Skip showBackupSecurity." : "showBackupSecurity. ");
            if (this.mCallback != null) {
                this.mCallback.userActivity();
                this.mCallback.showBackupSecurity();
            }
            updateFooter();
        }
    }

    private boolean needForgotPatternButton() {
        String phoneSdkVersion = VERSION.SDK;
        if (this.mLockPatternUtilsEx.isSavedPasswordExists(KeyguardUpdateMonitor.getCurrentUser()) || Integer.parseInt(phoneSdkVersion) <= 23) {
            return true;
        }
        return false;
    }

    protected void updateFooter() {
        if (this.mForgotPatternButton == null) {
            HwLog.w("AbsKeyguardPatternView", "No ForgotPatternButton");
            return;
        }
        boolean backupPinEnabled = mBkPinEnabled && !(this.mCallback != null ? this.mCallback.isVerifyUnlockOnly() : false);
        boolean isSupportKeyguard = KeyguardCfg.isDoubleLockOn(this.mContext);
        if (backupPinEnabled) {
            int errCnt = this.mRetryPolicy.getErrorCount();
            VerifyPolicy.getInstance(this.mContext);
            backupPinEnabled = errCnt >= VerifyPolicy.getMaxPattenAttempBeforeRestore();
        }
        int flagBackup = KeyguardCfg.isCredentialProtected(this.mContext) ? 0 : 4;
        if (!needForgotPatternButton()) {
            flagBackup = 0;
        }
        if (backupPinEnabled && isSupportKeyguard) {
            setButtonStyle((flagBackup | 1) | 2);
        } else if (isSupportKeyguard) {
            setButtonStyle(3);
        } else if (backupPinEnabled) {
            setButtonStyle(flagBackup | 1);
        } else {
            setButtonStyle(1);
        }
    }

    protected void setButtonStyle(int style) {
        if (this.mStyle != style && this.mForgotPatternButton != null && this.mEmergencyCallButton != null && this.mGoBackButton != null) {
            Button button;
            int i;
            LayoutParams params;
            HwLog.i("AbsKeyguardPatternView", "setButtonStyle to: " + style);
            if (!(style == 1 || this.mStyle == 1)) {
                if (this.mStyle == 0) {
                }
                button = this.mForgotPatternButton;
                if ((style & 4) != 0) {
                    i = 4;
                } else {
                    i = 0;
                }
                button.setVisibility(i);
                button = this.mGoBackButton;
                if ((style & 2) != 0) {
                    i = 8;
                } else {
                    i = 0;
                }
                button.setVisibility(i);
                if (this.mForgotPatternButton.getVisibility() == 0) {
                    if (this.mRetryPolicy.getRemainingChance() <= 0) {
                        this.mForgotPatternButton.setEnabled(true);
                        this.mForgotPatternButton.setAlpha(1.0f);
                    } else {
                        this.mForgotPatternButton.setEnabled(false);
                        this.mForgotPatternButton.setAlpha(0.3f);
                    }
                }
                invalidate();
                HwLog.d("AbsKeyguardPatternView", " EmergencyCallButton: " + this.mEmergencyCallButton + "\n ForgotPatternButton:" + this.mForgotPatternButton);
            }
            if (style == 1) {
                params = new LayoutParams(-1, -2);
                params.weight = 0.0f;
            } else {
                params = new LayoutParams(-1, -2);
                params.weight = 1.0f;
            }
            this.mForgotPatternButton.setLayoutParams(params);
            this.mEmergencyCallButton.setLayoutParams(params);
            this.mGoBackButton.setLayoutParams(params);
            button = this.mForgotPatternButton;
            if ((style & 4) != 0) {
                i = 0;
            } else {
                i = 4;
            }
            button.setVisibility(i);
            button = this.mGoBackButton;
            if ((style & 2) != 0) {
                i = 0;
            } else {
                i = 8;
            }
            button.setVisibility(i);
            if (this.mForgotPatternButton.getVisibility() == 0) {
                if (this.mRetryPolicy.getRemainingChance() <= 0) {
                    this.mForgotPatternButton.setEnabled(false);
                    this.mForgotPatternButton.setAlpha(0.3f);
                } else {
                    this.mForgotPatternButton.setEnabled(true);
                    this.mForgotPatternButton.setAlpha(1.0f);
                }
            }
            invalidate();
            HwLog.d("AbsKeyguardPatternView", " EmergencyCallButton: " + this.mEmergencyCallButton + "\n ForgotPatternButton:" + this.mForgotPatternButton);
        }
    }

    public void onInputChecked(int userId, int type, int timeoutMs, int delay, boolean isValidPassword, boolean forceCheck) {
        boolean z;
        HwLog.w("AbsKeyguardPatternView", "onInputChecked " + userId + " result " + type + "; " + timeoutMs);
        IRetryPolicy retryPolicy = RetryPolicy.getRetryPolicy(this.mContext, 1);
        if (type > 0) {
            retryPolicy.unregisterAll();
            HwLockScreenReporter.reportUnlockInfo(getContext(), getReportVerifyEventId(), true, this.mStartUnlockInputTime, getSinglehandStatus());
        } else if (type == -13 || retryPolicy.getRemainingChance() <= 0) {
            retryPolicy.registerObserver(this);
            timeoutMs = (int) retryPolicy.getRemainingTime();
        } else {
            HwLockScreenReporter.reportUnlockInfo(getContext(), getReportVerifyEventId(), false, this.mStartUnlockInputTime, getSinglehandStatus());
        }
        this.mStartUnlockInputTime = -1;
        if (type > 0) {
            z = true;
        } else {
            z = false;
        }
        onPatternChecked(userId, z, timeoutMs, isValidPassword);
    }

    public void onTimeTick(TimeTickInfo info) {
        updateFooter();
        CharSequence msg = ErrorMessage.getTimeoutMessage(this.mContext, SecurityMode.Pattern, info);
        if (TextUtils.isEmpty(msg)) {
            this.mSecurityMessageDisplay.setMessage(BuildConfig.FLAVOR, true);
        } else {
            this.mSecurityMessageDisplay.setMessage(msg, true);
        }
        HwLog.w("AbsKeyguardPatternView", "onTimeTick all back");
    }

    public void onTimeFinish() {
        setPattenEnabled(true);
        updateFooter();
        RetryPolicy.getFingerPolicy(this.mContext).resetErrorCount(this.mContext);
    }

    protected boolean startAppearAnimationHw(Runnable endRunnable) {
        AnimUtils.startEnterSecurityViewAnimation(this, endRunnable);
        return true;
    }

    protected boolean startDisappearAnimationHw(Runnable endRunnable) {
        AnimUtils.startExitSecurityViewAnimation((View) this, endRunnable);
        return true;
    }

    public boolean startRevertAnimation(Runnable endRunnable) {
        return AnimUtils.startRevertSecurityViewAnimation(this, endRunnable);
    }

    public void blockInput() {
        setPattenEnabled(false);
    }

    public void onAuthenticationPending() {
        AnimUtils.startExitSecurityViewAnimation((View) this, 500);
    }

    protected int getReportVerifyEventId() {
        return 151;
    }

    protected int getSinglehandStatus() {
        return 3;
    }
}
