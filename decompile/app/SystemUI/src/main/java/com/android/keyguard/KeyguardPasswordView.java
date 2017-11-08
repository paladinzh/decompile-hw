package com.android.keyguard;

import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.android.internal.widget.TextViewInputDisabler;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.inf.HwKeyguardPolicy;
import com.huawei.keyguard.policy.RetryPolicy;
import com.huawei.keyguard.support.RemoteLockUtils;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.OsUtils;
import com.huawei.keyguard.view.widget.HintErrorEffectEditText;
import fyusion.vislib.BuildConfig;
import java.util.List;

public class KeyguardPasswordView extends KeyguardAbsKeyInputView implements KeyguardSecurityView, OnEditorActionListener {
    private final int mDisappearYTranslation;
    private Interpolator mFastOutLinearInInterpolator;
    private InputMethodManager mImm;
    private Interpolator mLinearOutSlowInInterpolator;
    private TextView mPasswordEntry;
    private TextViewInputDisabler mPasswordEntryDisabler;
    private HintErrorEffectEditText mPasswordEntry_bg;
    private final boolean mShowImeAtScreenOn;
    private final KeyguardUpdateMonitor mUpdateMonitor;

    public KeyguardPasswordView(Context context) {
        this(context, null);
    }

    public KeyguardPasswordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mShowImeAtScreenOn = context.getResources().getBoolean(R$bool.kg_show_ime_at_screen_on);
        this.mDisappearYTranslation = getResources().getDimensionPixelSize(R$dimen.disappear_y_translation);
        this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563662);
        this.mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(context, 17563663);
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
    }

    protected void resetState() {
        CharSequence remoteInfo = RemoteLockUtils.isDeviceRemoteLocked(this.mContext) ? RemoteLockUtils.getDeviceRemoteLockedInfo(this.mContext) : BuildConfig.FLAVOR;
        if (TextUtils.isEmpty(remoteInfo)) {
            this.mSecurityMessageDisplay.setMessage(HwKeyguardUpdateMonitor.getInstance(this.mContext).isFirstTimeStartup() ? R$string.kg_prompt_reason_restart_password : R$string.kg_password_instructions, true);
        } else {
            this.mSecurityMessageDisplay.setMessage(remoteInfo, true);
        }
        boolean wasEnabled = this.mPasswordEntry.isEnabled();
        setPasswordEntryEnabled(true);
        setPasswordEntryInputEnabled(true);
        if (wasEnabled && !this.mUpdateMonitor.isOccluded()) {
            this.mImm.showSoftInput(this.mPasswordEntry, 1);
        }
    }

    protected int getPasswordTextViewId() {
        return R$id.passwordEntry;
    }

    public boolean needsInput() {
        return true;
    }

    public void onResume(final int reason) {
        super.onResume(reason);
        post(new Runnable() {
            public void run() {
                if (KeyguardPasswordView.this.isShown() && KeyguardPasswordView.this.mPasswordEntry.isEnabled()) {
                    KeyguardPasswordView.this.mPasswordEntry.requestFocus();
                    if (reason != 1 || KeyguardPasswordView.this.mShowImeAtScreenOn) {
                        KeyguardPasswordView.this.mImm.showSoftInput(KeyguardPasswordView.this.mPasswordEntry, 1);
                    }
                }
            }
        });
    }

    protected int getPromtReasonStringRes(int reason) {
        switch (reason) {
            case 0:
                return 0;
            case 1:
                return R$string.kg_prompt_reason_restart_password;
            case 2:
                return R$string.kg_prompt_reason_timeout_password;
            case 3:
                return R$string.kg_prompt_reason_device_admin;
            case 4:
                return R$string.kg_prompt_reason_user_request;
            case 10:
                if (KeyguardCfg.isSupportFpPasswordTimeout() && HwKeyguardUpdateMonitor.getInstance(getContext()).isFingerprintUnlockTimedOut(OsUtils.getCurrentUser())) {
                    return R$plurals.keyguard_reason_fp_timeout_pin;
                }
                return 0;
            default:
                return R$string.kg_prompt_reason_timeout_password;
        }
    }

    public void onPause() {
        super.onPause();
        try {
            this.mImm.hideSoftInputFromWindow(getWindowToken(), 0);
        } catch (IllegalArgumentException e) {
            HwLog.e("KeyguardPasswordView", "onPause hideSoftInput Err:" + e.toString());
        } catch (Exception e2) {
            HwLog.e("KeyguardPasswordView", "onPause Err:" + e2.toString());
        }
    }

    public void reset() {
        super.reset();
        this.mPasswordEntry.requestFocus();
        if (this.mPasswordEntry_bg.isShownErrEffect()) {
            this.mPasswordEntry_bg.hideErrorEffect();
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            post(new Runnable() {
                public void run() {
                    if (KeyguardPasswordView.this.mPasswordEntry.isEnabled() && KeyguardPasswordView.this.getWindowSystemUiVisibility() != 0) {
                        KeyguardPasswordView.this.mPasswordEntry.requestFocus();
                        KeyguardPasswordView.this.mImm.showSoftInput(KeyguardPasswordView.this.mPasswordEntry, 1);
                    }
                }
            });
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSecurityMessageDisplay.setTimeout(0);
        boolean imeOrDeleteButtonVisible = false;
        this.mImm = (InputMethodManager) getContext().getSystemService("input_method");
        this.mPasswordEntry = (TextView) findViewById(getPasswordTextViewId());
        this.mPasswordEntry_bg = (HintErrorEffectEditText) findViewById(R$id.passwordEntry_bg);
        this.mPasswordEntryDisabler = new TextViewInputDisabler(this.mPasswordEntry);
        this.mPasswordEntry.setKeyListener(TextKeyListener.getInstance());
        this.mPasswordEntry.setInputType(16777345);
        this.mPasswordEntry.setOnEditorActionListener(this);
        this.mPasswordEntry.addTextChangedListener(this);
        this.mPasswordEntry.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                KeyguardPasswordView.this.mCallback.userActivity();
            }
        });
        this.mPasswordEntry.setSelected(true);
        this.mPasswordEntry.requestFocus();
        View switchImeButton = findViewById(R$id.switch_ime_button);
        if (switchImeButton != null && hasMultipleEnabledIMEsOrSubtypes(this.mImm, false)) {
            this.mPasswordEntry.setCursorVisible(true);
            switchImeButton.setVisibility(0);
            imeOrDeleteButtonVisible = true;
            switchImeButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    KeyguardPasswordView.this.mCallback.userActivity();
                    KeyguardPasswordView.this.mImm.showInputMethodPicker(false);
                }
            });
        }
        if (!imeOrDeleteButtonVisible) {
            LayoutParams params = this.mPasswordEntry.getLayoutParams();
            if (params instanceof MarginLayoutParams) {
                ((MarginLayoutParams) params).setMarginStart(0);
                this.mPasswordEntry.setLayoutParams(params);
            }
        }
        this.mUpdateMonitor.checkSecurityMode(getSecurityMode());
    }

    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        return this.mPasswordEntry.requestFocus(direction, previouslyFocusedRect);
    }

    protected void resetPasswordText(boolean animate, boolean announce) {
        this.mPasswordEntry.setText(BuildConfig.FLAVOR);
    }

    protected String getPasswordText() {
        return this.mPasswordEntry.getText().toString();
    }

    protected void setPasswordEntryEnabled(boolean enabled) {
        this.mPasswordEntry.setEnabled(enabled);
    }

    protected void setPasswordEntryInputEnabled(boolean enabled) {
        this.mPasswordEntryDisabler.setInputEnabled(enabled);
    }

    private boolean hasMultipleEnabledIMEsOrSubtypes(InputMethodManager imm, boolean shouldIncludeAuxiliarySubtypes) {
        boolean z = true;
        if (imm == null || imm.isSecImmEnabled()) {
            return false;
        }
        int filteredImisCount = 0;
        for (InputMethodInfo imi : imm.getEnabledInputMethodList()) {
            if (filteredImisCount > 1) {
                return true;
            }
            List<InputMethodSubtype> subtypes = imm.getEnabledInputMethodSubtypeList(imi, true);
            if (subtypes.isEmpty()) {
                filteredImisCount++;
            } else {
                int auxCount = 0;
                for (InputMethodSubtype subtype : subtypes) {
                    if (subtype.isAuxiliary()) {
                        auxCount++;
                    }
                }
                if (subtypes.size() - auxCount > 0 || (shouldIncludeAuxiliarySubtypes && auxCount > 1)) {
                    filteredImisCount++;
                }
            }
        }
        if (filteredImisCount <= 1 && imm.getEnabledInputMethodSubtypeList(null, false).size() <= 1) {
            z = false;
        }
        return z;
    }

    public int getWrongPasswordStringId() {
        return R$string.kg_wrong_password;
    }

    public String getWrongPasswordString() {
        int remaining = RetryPolicy.getDefaultPolicy(this.mContext).getRemainingChance();
        return getResources().getQuantityString(R$plurals.kg_verify_fail_hint_password, remaining, new Object[]{Integer.valueOf(remaining)});
    }

    public void startAppearAnimation() {
        setAlpha(0.0f);
        if (!startAppearAnimationHw(null)) {
            setTranslationY(0.0f);
            animate().alpha(1.0f).withLayer().setDuration(300).setInterpolator(this.mLinearOutSlowInInterpolator);
        }
    }

    public boolean startDisappearAnimation(Runnable finishRunnable) {
        if (startDisappearAnimationHw(finishRunnable)) {
            return true;
        }
        animate().alpha(0.0f).translationY((float) this.mDisappearYTranslation).setInterpolator(this.mFastOutLinearInInterpolator).setDuration(100).withEndAction(finishRunnable);
        return true;
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        super.beforeTextChanged(s, start, count, after);
        if (this.mCallback != null) {
            this.mCallback.userActivity();
        }
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        super.onTextChanged(s, start, before, count);
        if (s.length() > 0 && 24 == Integer.valueOf(s.charAt(s.length() - 1)).intValue()) {
            HwKeyguardPolicy.getInst().onBackPressed();
            resetPasswordText(false, true);
        }
    }

    public void afterTextChanged(Editable s) {
        super.afterTextChanged(s);
        if (!TextUtils.isEmpty(s)) {
            onUserInput();
        }
    }

    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean isSoftImeEvent = event == null ? (actionId == 0 || actionId == 6) ? true : actionId == 5 : false;
        boolean isKeyboardEnterKey = (event == null || !KeyEvent.isConfirmKey(event.getKeyCode())) ? false : event.getAction() == 0;
        if (!isSoftImeEvent && !isKeyboardEnterKey) {
            return false;
        }
        verifyPasswordAndUnlock();
        return true;
    }

    protected SecurityMode getSecurityMode() {
        return SecurityMode.Password;
    }

    public void onCharAppend() {
        super.onCharAppend();
        if (this.mPasswordEntry_bg.isShownErrEffect()) {
            this.mPasswordEntry_bg.hideErrorEffect();
        }
    }

    protected void onPasswordChecked(int userId, boolean matched, int timeoutMs, boolean isValidPassword) {
        super.onPasswordChecked(userId, matched, timeoutMs, isValidPassword);
        if (matched) {
            this.mPasswordEntry_bg.hideErrorEffect();
        } else {
            this.mPasswordEntry_bg.showErrorEffect();
        }
    }

    protected int getReportVerifyEventId() {
        return 150;
    }
}
