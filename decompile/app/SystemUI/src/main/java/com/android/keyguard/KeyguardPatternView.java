package com.android.keyguard;

import android.content.Context;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternUtils.RequestThrottledException;
import com.android.internal.widget.LockPatternView;
import com.android.internal.widget.LockPatternView.Cell;
import com.android.internal.widget.LockPatternView.CellState;
import com.android.internal.widget.LockPatternView.DisplayMode;
import com.android.internal.widget.LockPatternView.OnPatternListener;
import com.android.keyguard.EmergencyButton.EmergencyButtonCallback;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.android.settingslib.animation.AppearAnimationCreator;
import com.android.settingslib.animation.AppearAnimationUtils;
import com.android.settingslib.animation.DisappearAnimationUtils;
import com.huawei.keyguard.HwKeyguardAbsPatternView;
import com.huawei.keyguard.policy.ErrorMessage;
import com.huawei.keyguard.policy.RetryPolicy;
import com.huawei.keyguard.policy.RetryPolicy.IRetryPolicy;
import com.huawei.keyguard.policy.VerifyPolicy;
import com.huawei.keyguard.support.RemoteLockUtils;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.view.widget.KeyguardButtonView;
import com.huawei.timekeeper.TimeTickInfo;
import java.util.List;

public class KeyguardPatternView extends HwKeyguardAbsPatternView implements KeyguardSecurityView, AppearAnimationCreator<CellState>, EmergencyButtonCallback {
    private final AppearAnimationUtils mAppearAnimationUtils;
    private KeyguardButtonView mBackButton;
    private Runnable mCancelPatternRunnable;
    private ViewGroup mContainer;
    private CountDownTimer mCountdownTimer;
    private final DisappearAnimationUtils mDisappearAnimationUtils;
    private int mDisappearYTranslation;
    private View mEcaView;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private long mLastPokeTime;
    protected LockPatternUtils mLockPatternUtils;
    private LockPatternView mLockPatternView;
    private AsyncTask<?, ?, ?> mPendingLockCheck;
    private Rect mTempRect;

    private class UnlockPatternListener implements OnPatternListener {
        private UnlockPatternListener() {
        }

        public void onPatternStart() {
            KeyguardPatternView.this.mBackButton.setClickable(false);
            KeyguardPatternView.this.mBackButton.setEnabled(false);
            KeyguardPatternView.this.mLockPatternView.removeCallbacks(KeyguardPatternView.this.mCancelPatternRunnable);
        }

        public void onPatternCleared() {
        }

        public void onPatternCellAdded(List<Cell> list) {
            KeyguardPatternView.this.mCallback.userActivity();
            if (KeyguardPatternView.this.mStartUnlockInputTime < 0) {
                KeyguardPatternView.this.mStartUnlockInputTime = System.currentTimeMillis();
            }
        }

        public void onPatternDetected(List<Cell> pattern) {
            KeyguardPatternView.this.mLockPatternView.disableInput();
            KeyguardPatternView.this.mLockPatternView.disableInput();
            int userId = KeyguardUpdateMonitor.getCurrentUser();
            if (pattern.size() < 4) {
                KeyguardPatternView.this.mLockPatternView.enableInput();
                KeyguardPatternView.this.onPatternChecked(userId, false, 0, false);
                return;
            }
            VerifyPolicy.getInstance(KeyguardPatternView.this.mContext).verifyPatten(pattern, userId, KeyguardPatternView.this);
            if (pattern.size() > 2) {
                KeyguardPatternView.this.mCallback.userActivity();
            }
        }
    }

    public KeyguardPatternView(Context context) {
        this(context, null);
    }

    public KeyguardPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mCountdownTimer = null;
        this.mLastPokeTime = -7000;
        this.mCancelPatternRunnable = new Runnable() {
            public void run() {
                KeyguardPatternView.this.mLockPatternView.clearPattern();
            }
        };
        this.mTempRect = new Rect();
        this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        this.mAppearAnimationUtils = new AppearAnimationUtils(context, 220, 1.5f, 2.0f, AnimationUtils.loadInterpolator(this.mContext, 17563662));
        this.mDisappearAnimationUtils = new DisappearAnimationUtils(context, 125, 1.2f, 0.6f, AnimationUtils.loadInterpolator(this.mContext, 17563663));
        this.mDisappearYTranslation = getResources().getDimensionPixelSize(R$dimen.disappear_y_translation);
    }

    public void setKeyguardCallback(KeyguardSecurityCallback callback) {
        this.mCallback = callback;
    }

    public void setLockPatternUtils(LockPatternUtils utils) {
        this.mLockPatternUtils = utils;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mLockPatternUtils = this.mLockPatternUtils == null ? new LockPatternUtils(this.mContext) : this.mLockPatternUtils;
        this.mLockPatternView = (LockPatternView) findViewById(R$id.lockPatternView);
        this.mLockPatternView.setSaveEnabled(false);
        this.mLockPatternView.setOnPatternListener(new UnlockPatternListener());
        this.mLockPatternView.setTactileFeedbackEnabled(this.mLockPatternUtils.isTactileFeedbackEnabled());
        this.mSecurityMessageDisplay = (KeyguardMessageArea) KeyguardMessageArea.findSecurityMessageDisplay(this);
        this.mSecurityMessageDisplay.setTimeout(0);
        this.mBackButton = (KeyguardButtonView) findViewById(R$id.back_to_keyguard);
        this.mBackButton.setSupportsLongpressBack(false);
        this.mEcaView = findViewById(R$id.keyguard_selector_fade_container);
        this.mContainer = (ViewGroup) findViewById(R$id.container);
        EmergencyButton button = (EmergencyButton) findViewById(R$id.emergency_call_button);
        if (button != null) {
            button.setCallback(this);
        }
    }

    public void onEmergencyButtonClickedWhenInCall() {
        this.mCallback.reset();
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = super.onTouchEvent(ev);
        long elapsed = SystemClock.elapsedRealtime() - this.mLastPokeTime;
        if (result && elapsed > 6900) {
            this.mLastPokeTime = SystemClock.elapsedRealtime();
        }
        this.mTempRect.set(0, 0, 0, 0);
        offsetRectIntoDescendantCoords(this.mLockPatternView, this.mTempRect);
        ev.offsetLocation((float) this.mTempRect.left, (float) this.mTempRect.top);
        if (this.mLockPatternView.dispatchTouchEvent(ev)) {
            result = true;
        }
        ev.offsetLocation((float) (-this.mTempRect.left), (float) (-this.mTempRect.top));
        return result;
    }

    public void reset() {
        boolean z;
        this.mBackButton.setEnabled(true);
        this.mBackButton.setClickable(true);
        LockPatternView lockPatternView = this.mLockPatternView;
        if (this.mLockPatternUtils.isVisiblePatternEnabled(KeyguardUpdateMonitor.getCurrentUser())) {
            z = false;
        } else {
            z = true;
        }
        lockPatternView.setInStealthMode(z);
        setPattenEnabled(true);
        this.mLockPatternView.clearPattern();
        IRetryPolicy retryPolicy = RetryPolicy.getDefaultPolicy(this.mContext);
        try {
            retryPolicy.checkLockDeadline();
            displayDefaultSecurityMessage();
            setPattenEnabled(true);
        } catch (RequestThrottledException e) {
            HwLog.i("SecurityPatternView", "KeyguardPatternView Is lockout");
            setPattenEnabled(false);
            TimeTickInfo info = retryPolicy.getTimeTickInfo();
            if (info != null) {
                if (info.getMinute() <= 0 && info.getHour() <= 0) {
                    if (info.getSecond() > 0) {
                    }
                }
                this.mSecurityMessageDisplay.setMessage(ErrorMessage.getTimeoutMessage(this.mContext, SecurityMode.Pattern, info), true);
            }
            retryPolicy.registerObserver(this);
        }
        updateFooter();
    }

    protected void displayDefaultSecurityMessage() {
        CharSequence remoteInfo = RemoteLockUtils.getDeviceRemoteLockedInfo(this.mContext);
        HwLog.d("SecurityPatternView", "displayDefaultSecurityMessagei remoteInfo = " + remoteInfo);
        if (!RemoteLockUtils.isDeviceRemoteLocked(this.mContext) || TextUtils.isEmpty(remoteInfo)) {
            this.mSecurityMessageDisplay.setMessage(R$string.kg_pattern_instructions, true);
        } else {
            this.mSecurityMessageDisplay.setMessage(remoteInfo, true);
        }
    }

    protected void onPatternChecked(int userId, boolean matched, int timeoutMs, boolean isValidPattern) {
        boolean enableInput = true;
        boolean dismissKeyguard = KeyguardUpdateMonitor.getCurrentUser() == userId;
        HwLog.v("SecurityPatternView", "PatternChecked dismiss: " + dismissKeyguard + " " + matched);
        if (matched) {
            this.mCallback.reportUnlockAttempt(userId, true, 0);
            if (dismissKeyguard) {
                this.mLockPatternView.setDisplayMode(DisplayMode.Correct);
                this.mCallback.dismiss(true);
            }
            return;
        }
        this.mBackButton.setEnabled(true);
        this.mBackButton.setClickable(true);
        this.mLockPatternView.setDisplayMode(DisplayMode.Wrong);
        if (isValidPattern) {
            this.mCallback.reportUnlockAttempt(userId, false, timeoutMs);
            if (timeoutMs > 0) {
                enableInput = false;
                HwLog.w("SecurityPatternView", "AttemptLockout at " + this.mLockPatternUtils.setLockoutAttemptDeadline(userId, timeoutMs));
            }
        }
        if (timeoutMs == 0) {
            this.mSecurityMessageDisplay.setMessage(R$string.kg_verify_fail_hint_patten, true);
            this.mLockPatternView.postDelayed(this.mCancelPatternRunnable, 2000);
        }
        setPattenEnabled(enableInput);
    }

    public void onTimeFinish() {
        super.onTimeFinish();
        displayDefaultSecurityMessage();
    }

    public boolean needsInput() {
        return false;
    }

    public void onPause() {
        if (this.mCountdownTimer != null) {
            this.mCountdownTimer.cancel();
            this.mCountdownTimer = null;
        }
        if (this.mPendingLockCheck != null) {
            this.mPendingLockCheck.cancel(false);
            this.mPendingLockCheck = null;
        }
    }

    public void onResume(int reason) {
        reset();
    }

    public void showPromptReason(int reason) {
        switch (reason) {
            case 0:
            case 10:
                return;
            case 1:
                this.mSecurityMessageDisplay.setMessage(R$string.kg_prompt_reason_restart_pattern, true);
                return;
            case 2:
                this.mSecurityMessageDisplay.setMessage(R$string.kg_prompt_reason_timeout_pattern, true);
                return;
            case 3:
                this.mSecurityMessageDisplay.setMessage(R$string.kg_prompt_reason_device_admin, true);
                return;
            case 4:
                this.mSecurityMessageDisplay.setMessage(R$string.kg_prompt_reason_user_request, true);
                return;
            default:
                this.mSecurityMessageDisplay.setMessage(R$string.kg_prompt_reason_timeout_pattern, true);
                return;
        }
    }

    public void showMessage(String message, int color) {
        this.mSecurityMessageDisplay.setNextMessageColor(color);
        this.mSecurityMessageDisplay.setMessage((CharSequence) message, true);
    }

    public void startAppearAnimation() {
        enableClipping(false);
        Runnable animFinishRunner = new Runnable() {
            public void run() {
                KeyguardPatternView.this.enableClipping(true);
            }
        };
        if (!startAppearAnimationHw(animFinishRunner)) {
            View messageArea;
            setAlpha(1.0f);
            setTranslationY(this.mAppearAnimationUtils.getStartTranslation());
            AppearAnimationUtils.startTranslationYAnimation(this, 0, 500, 0.0f, this.mAppearAnimationUtils.getInterpolator());
            this.mAppearAnimationUtils.startAnimation2d(this.mLockPatternView.getCellStates(), animFinishRunner, this);
            if (this.mSecurityMessageDisplay != null) {
                messageArea = (KeyguardMessageArea) this.mSecurityMessageDisplay;
            } else {
                messageArea = null;
            }
            if (!(messageArea == null || TextUtils.isEmpty(messageArea.getText()))) {
                this.mAppearAnimationUtils.createAnimation(messageArea, 0, 220, this.mAppearAnimationUtils.getStartTranslation(), true, this.mAppearAnimationUtils.getInterpolator(), null);
            }
        }
    }

    public boolean startDisappearAnimation(final Runnable finishRunnable) {
        this.mLockPatternView.clearPattern();
        enableClipping(false);
        setTranslationY(0.0f);
        Runnable animFinishRunner = new Runnable() {
            public void run() {
                KeyguardPatternView.this.enableClipping(true);
                if (finishRunnable != null) {
                    finishRunnable.run();
                }
            }
        };
        if (startDisappearAnimationHw(animFinishRunner)) {
            return true;
        }
        KeyguardMessageArea messageArea;
        AppearAnimationUtils.startTranslationYAnimation(this, 0, 300, -this.mDisappearAnimationUtils.getStartTranslation(), this.mDisappearAnimationUtils.getInterpolator());
        this.mDisappearAnimationUtils.startAnimation2d(this.mLockPatternView.getCellStates(), animFinishRunner, this);
        if (this.mSecurityMessageDisplay != null) {
            messageArea = (KeyguardMessageArea) this.mSecurityMessageDisplay;
        } else {
            messageArea = null;
        }
        if (!(messageArea == null || TextUtils.isEmpty(messageArea.getText()))) {
            this.mDisappearAnimationUtils.createAnimation((View) messageArea, 0, 200, (-this.mDisappearAnimationUtils.getStartTranslation()) * 3.0f, false, this.mDisappearAnimationUtils.getInterpolator(), null);
        }
        return true;
    }

    private void enableClipping(boolean enable) {
        setClipChildren(enable);
        this.mContainer.setClipToPadding(enable);
        this.mContainer.setClipChildren(enable);
    }

    public void createAnimation(CellState animatedCell, long delay, long duration, float translationY, boolean appearing, Interpolator interpolator, Runnable finishListener) {
        this.mLockPatternView.startCellStateAnimation(animatedCell, 1.0f, appearing ? 1.0f : 0.0f, appearing ? translationY : 0.0f, appearing ? 0.0f : translationY, appearing ? 0.0f : 1.0f, 1.0f, delay, duration, interpolator, finishListener);
        if (finishListener != null) {
            this.mAppearAnimationUtils.createAnimation(this.mEcaView, delay, duration, translationY, appearing, interpolator, null);
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    protected void setPattenEnabled(boolean enabled) {
        if (enabled) {
            this.mLockPatternView.enableInput();
            this.mLockPatternView.setEnabled(true);
        } else {
            this.mLockPatternView.disableInput();
            this.mLockPatternView.setEnabled(false);
        }
        this.mLockPatternView.clearPattern();
    }
}
