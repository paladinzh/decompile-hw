package com.android.keyguard;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.android.keyguard.PasswordTextView.PasswdChangeListener;
import com.android.settingslib.animation.AppearAnimationUtils;
import com.android.settingslib.animation.DisappearAnimationUtils;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.policy.RetryPolicy;
import com.huawei.keyguard.support.RemoteLockUtils;
import com.huawei.keyguard.util.OsUtils;
import com.huawei.keyguard.view.widget.KeyguardButtonView;

public class KeyguardPINView extends KeyguardPinBasedInputView implements PasswdChangeListener {
    private final AppearAnimationUtils mAppearAnimationUtils;
    private KeyguardButtonView mBackButton;
    private ViewGroup mContainer;
    private final DisappearAnimationUtils mDisappearAnimationUtils;
    private int mDisappearYTranslation;
    private ViewGroup mRow0;
    private ViewGroup mRow1;
    private ViewGroup mRow2;
    private ViewGroup mRow3;
    Runnable mSetBackButtonState;
    private final KeyguardUpdateMonitor mUpdateMonitor;
    private View[][] mViews;

    public KeyguardPINView(Context context) {
        this(context, null);
    }

    public KeyguardPINView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSetBackButtonState = new Runnable() {
            public void run() {
                KeyguardPINView.this.enableBackButton(true);
            }
        };
        this.mAppearAnimationUtils = new AppearAnimationUtils(context);
        this.mDisappearAnimationUtils = new DisappearAnimationUtils(context, 125, 0.6f, 0.45f, AnimationUtils.loadInterpolator(this.mContext, 17563663));
        this.mDisappearYTranslation = getResources().getDimensionPixelSize(R$dimen.disappear_y_translation);
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
    }

    protected void resetState() {
        super.resetState();
        CharSequence remoteInfo = RemoteLockUtils.getDeviceRemoteLockedInfo(this.mContext);
        if (!RemoteLockUtils.isDeviceRemoteLocked(this.mContext) || TextUtils.isEmpty(remoteInfo)) {
            this.mSecurityMessageDisplay.setMessage(HwKeyguardUpdateMonitor.getInstance(this.mContext).isFirstTimeStartup() ? R$string.kg_prompt_reason_restart_pin : R$string.kg_pin_instructions, true);
        } else {
            this.mSecurityMessageDisplay.setMessage(remoteInfo, true);
        }
    }

    public void onResume(int reason) {
        super.onResume(reason);
    }

    protected int getPasswordTextViewId() {
        return R$id.pinEntry;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSecurityMessageDisplay.setTimeout(0);
        this.mBackButton = (KeyguardButtonView) findViewById(R$id.back_to_keyguard);
        this.mBackButton.setSupportsLongpressBack(false);
        this.mContainer = (ViewGroup) findViewById(R$id.container);
        this.mRow0 = (ViewGroup) findViewById(R$id.row0);
        this.mRow1 = (ViewGroup) findViewById(R$id.row1);
        this.mRow2 = (ViewGroup) findViewById(R$id.row2);
        this.mRow3 = (ViewGroup) findViewById(R$id.row3);
        View[][] viewArr = new View[6][];
        viewArr[0] = new View[]{this.mRow0, null, null};
        viewArr[1] = new View[]{findViewById(R$id.key1), findViewById(R$id.key2), findViewById(R$id.key3)};
        viewArr[2] = new View[]{findViewById(R$id.key4), findViewById(R$id.key5), findViewById(R$id.key6)};
        viewArr[3] = new View[]{findViewById(R$id.key7), findViewById(R$id.key8), findViewById(R$id.key9)};
        viewArr[4] = new View[]{findViewById(R$id.emergency_call_button_pin_view), findViewById(R$id.key0), findViewById(R$id.delete_back_layout)};
        viewArr[5] = new View[]{null, this.mEcaView, null};
        this.mViews = viewArr;
        this.mDeleteButton.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                if (KeyguardPINView.this.mPasswordEntry.isEnabled()) {
                    if (KeyguardPINView.this.mIsAppendInput) {
                        KeyguardPINView.this.verifyPasswordAndUnlock();
                    }
                    KeyguardPINView.this.resetPasswordText(true, true);
                }
                KeyguardPINView.this.doHapticKeyClick();
                return true;
            }
        });
        if (!this.mLockPatternUtils.isLockPatternEnabled(KeyguardUpdateMonitor.getCurrentUser())) {
            this.mUpdateMonitor.checkSecurityMode(getSecurityMode());
        }
    }

    protected void resetPasswordText(boolean animate, boolean announce) {
        super.resetPasswordText(animate, announce);
        switchToBackButton(true);
    }

    public int getWrongPasswordStringId() {
        return R$string.kg_wrong_pin;
    }

    public String getWrongPasswordString() {
        int remaining = RetryPolicy.getDefaultPolicy(this.mContext).getRemainingChance();
        return getResources().getQuantityString(R$plurals.kg_verify_fail_hint_pin, remaining, new Object[]{Integer.valueOf(remaining)});
    }

    public void startAppearAnimation() {
        enableClipping(false);
        Runnable animFinishRunner = new Runnable() {
            public void run() {
                KeyguardPINView.this.enableClipping(true);
            }
        };
        if (!startAppearAnimationHw(animFinishRunner)) {
            setAlpha(1.0f);
            setTranslationY(this.mAppearAnimationUtils.getStartTranslation());
            AppearAnimationUtils.startTranslationYAnimation(this, 0, 500, 0.0f, this.mAppearAnimationUtils.getInterpolator());
            this.mAppearAnimationUtils.startAnimation2d(this.mViews, animFinishRunner);
        }
    }

    public boolean startDisappearAnimation(final Runnable finishRunnable) {
        enableClipping(false);
        Runnable animFinishRunner = new Runnable() {
            public void run() {
                KeyguardPINView.this.enableClipping(true);
                if (finishRunnable != null) {
                    finishRunnable.run();
                }
            }
        };
        if (startDisappearAnimationHw(animFinishRunner)) {
            return true;
        }
        setTranslationY(0.0f);
        AppearAnimationUtils.startTranslationYAnimation(this, 0, 280, (float) this.mDisappearYTranslation, this.mDisappearAnimationUtils.getInterpolator());
        this.mDisappearAnimationUtils.startAnimation2d(this.mViews, animFinishRunner);
        return true;
    }

    private void enableClipping(boolean enable) {
        this.mContainer.setClipToPadding(enable);
        this.mContainer.setClipChildren(enable);
        this.mRow1.setClipToPadding(enable);
        this.mRow2.setClipToPadding(enable);
        this.mRow3.setClipToPadding(enable);
        setClipChildren(enable);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    protected SecurityMode getSecurityMode() {
        return SecurityMode.PIN;
    }

    protected void onPasswordChecked(int userId, boolean matched, int timeoutMs, boolean isValidPassword) {
        super.onPasswordChecked(userId, matched, timeoutMs, isValidPassword);
        if (matched) {
            resetPattenIfNeeded();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void resetPattenIfNeeded() {
        if (KeyguardCfg.isBackupPinEnabled() && getSecurityMode() == SecurityMode.PIN && this.mLockPatternUtils.isLockPatternEnabled(KeyguardUpdateMonitor.getCurrentUser())) {
            Intent ai = new Intent("com.android.settings.action.HW_RESET_NEW_PASSWORD");
            ai.setFlags(268435456);
            OsUtils.startUserActivity(this.mContext, ai);
        }
    }

    public void onCharAppend() {
        super.onCharAppend();
        if (getPasswordText().length() == 1) {
            switchToBackButton(false);
        }
    }

    public void onCharDel() {
        super.onCharDel();
        if (getPasswordText().length() == 1) {
            enableBackButton(false);
            switchToBackButton(true);
        }
    }

    private void switchToBackButton(boolean doBack) {
        switchToBackButtonNormal(doBack);
    }

    private void switchToBackButtonNormal(boolean doBack) {
        if (doBack) {
            this.mDeleteButton.setVisibility(8);
            this.mBackButton.setVisibility(0);
            this.mBackButton.postDelayed(this.mSetBackButtonState, 1000);
            return;
        }
        this.mDeleteButton.setVisibility(0);
        this.mBackButton.setVisibility(8);
        this.mBackButton.removeCallbacks(this.mSetBackButtonState);
    }

    private void enableBackButton(boolean enable) {
        this.mBackButton.setVisibility(0);
        if (enable) {
            this.mBackButton.setClickable(true);
            this.mBackButton.setAlpha(1.0f);
            return;
        }
        this.mBackButton.setClickable(false);
    }

    protected int getReportVerifyEventId() {
        return 152;
    }
}
