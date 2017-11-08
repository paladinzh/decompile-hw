package com.android.keyguard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.android.keyguard.hwlockscreen.SliderView;
import com.android.keyguard.hwlockscreen.SliderView.OnConfirmListener;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.monitor.StateMonitor;
import com.huawei.keyguard.policy.VerifyPolicy;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.util.SingleHandUtils;
import fyusion.vislib.BuildConfig;

public class KeyguardSecurityContainer extends FrameLayout implements KeyguardSecurityView {
    private static final /* synthetic */ int[] -com-android-keyguard-KeyguardSecurityModel$SecurityModeSwitchesValues = null;
    private KeyguardSecurityCallback mCallback;
    private SecurityMode mCurrentSecuritySelection;
    private boolean mIsVerifyUnlockOnly;
    private LockPatternUtils mLockPatternUtils;
    private KeyguardSecurityCallback mNullCallback;
    private SecurityCallback mSecurityCallback;
    private KeyguardSecurityModel mSecurityModel;
    private KeyguardSecurityViewFlipper mSecurityViewFlipper;
    private final KeyguardUpdateMonitor mUpdateMonitor;
    private int orientation;

    public interface SecurityCallback {
        boolean dismiss(boolean z);

        void finish(boolean z);

        void onSecurityModeChanged(SecurityMode securityMode, boolean z);

        void reset();

        void userActivity();
    }

    private static /* synthetic */ int[] -getcom-android-keyguard-KeyguardSecurityModel$SecurityModeSwitchesValues() {
        if (-com-android-keyguard-KeyguardSecurityModel$SecurityModeSwitchesValues != null) {
            return -com-android-keyguard-KeyguardSecurityModel$SecurityModeSwitchesValues;
        }
        int[] iArr = new int[SecurityMode.values().length];
        try {
            iArr[SecurityMode.Invalid.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[SecurityMode.None.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[SecurityMode.PIN.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[SecurityMode.Password.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[SecurityMode.Pattern.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[SecurityMode.SimPin.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[SecurityMode.SimPuk.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        -com-android-keyguard-KeyguardSecurityModel$SecurityModeSwitchesValues = iArr;
        return iArr;
    }

    public KeyguardSecurityContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyguardSecurityContainer(Context context) {
        this(context, null, 0);
    }

    public KeyguardSecurityContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mCurrentSecuritySelection = SecurityMode.Invalid;
        this.mCallback = new KeyguardSecurityCallback() {
            public void userActivity() {
                if (KeyguardSecurityContainer.this.mSecurityCallback != null) {
                    KeyguardSecurityContainer.this.mSecurityCallback.userActivity();
                }
            }

            public void dismiss(boolean authenticated) {
                KeyguardSecurityContainer.this.mSecurityCallback.dismiss(authenticated);
            }

            public boolean isVerifyUnlockOnly() {
                return KeyguardSecurityContainer.this.mIsVerifyUnlockOnly;
            }

            public void reportUnlockAttempt(int userId, boolean success, int timeoutMs) {
                KeyguardUpdateMonitor monitor = KeyguardUpdateMonitor.getInstance(KeyguardSecurityContainer.this.mContext);
                if (success) {
                    monitor.clearFailedUnlockAttempts();
                    KeyguardSecurityContainer.this.mLockPatternUtils.reportSuccessfulPasswordAttempt(userId);
                    return;
                }
                KeyguardSecurityContainer.this.reportFailedUnlockAttempt(userId, timeoutMs);
            }

            public void reset() {
                KeyguardSecurityContainer.this.mSecurityCallback.reset();
            }

            public void showBackupSecurity() {
                KeyguardSecurityContainer.this.mCurrentSecuritySelection = SecurityMode.Pattern;
                KeyguardSecurityContainer.this.showBackupSecurityScreen();
            }
        };
        this.mNullCallback = new KeyguardSecurityCallback() {
            public void userActivity() {
            }

            public void reportUnlockAttempt(int userId, boolean success, int timeoutMs) {
            }

            public boolean isVerifyUnlockOnly() {
                return false;
            }

            public void dismiss(boolean securityVerified) {
            }

            public void reset() {
            }

            public void showBackupSecurity() {
            }
        };
        this.mSecurityModel = new KeyguardSecurityModel(context);
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
    }

    public void setSecurityCallback(SecurityCallback callback) {
        this.mSecurityCallback = callback;
    }

    public void onResume(int reason) {
        if (this.mCurrentSecuritySelection != SecurityMode.None) {
            getSecurityView(this.mCurrentSecuritySelection).onResume(reason);
        }
    }

    public void onPause() {
        if (this.mCurrentSecuritySelection != SecurityMode.None) {
            getSecurityView(this.mCurrentSecuritySelection).onPause();
        }
    }

    public void startAppearAnimation() {
        if (this.mCurrentSecuritySelection != SecurityMode.None) {
            getSecurityView(this.mCurrentSecuritySelection).startAppearAnimation();
        }
    }

    public boolean startDisappearAnimation(Runnable onFinishRunnable) {
        if (this.mCurrentSecuritySelection == SecurityMode.None) {
            return false;
        }
        HwLog.v("KeyguardSecurityView", "KeyguardSecurityContainer startDisappearAnimation will begin!");
        return getSecurityView(this.mCurrentSecuritySelection).startDisappearAnimation(onFinishRunnable);
    }

    public boolean startRevertAnimation(Runnable onFinishRunnable) {
        if (this.mCurrentSecuritySelection != SecurityMode.None) {
            return getSecurityView(this.mCurrentSecuritySelection).startRevertAnimation(onFinishRunnable);
        }
        return false;
    }

    public CharSequence getCurrentSecurityModeContentDescription() {
        View v = (View) getSecurityView(this.mCurrentSecuritySelection);
        if (v != null) {
            return v.getContentDescription();
        }
        return BuildConfig.FLAVOR;
    }

    private KeyguardSecurityView getSecurityView(SecurityMode securityMode) {
        int securityViewIdForMode = getSecurityViewIdForMode(securityMode);
        KeyguardSecurityView keyguardSecurityView = null;
        int children = this.mSecurityViewFlipper.getChildCount();
        for (int child = 0; child < children; child++) {
            HwLog.v("KeyguardSecurityView", "Container check " + this.mSecurityViewFlipper.getChildAt(child).getId() + " <-> " + securityViewIdForMode);
            if (this.mSecurityViewFlipper.getChildAt(child).getId() == securityViewIdForMode) {
                keyguardSecurityView = (KeyguardSecurityView) this.mSecurityViewFlipper.getChildAt(child);
                break;
            }
        }
        int layoutId = getLayoutIdFor(securityMode);
        if (keyguardSecurityView != null || layoutId == 0) {
            return keyguardSecurityView;
        }
        View v = LayoutInflater.from(this.mContext).inflate(layoutId, this.mSecurityViewFlipper, false);
        this.mSecurityViewFlipper.addView(v);
        HwLog.v("KeyguardSecurityView", "Container inflating id = " + layoutId + " : view Id " + v.getId());
        updateSecurityView(v);
        return (KeyguardSecurityView) v;
    }

    private void updateSecurityView(View view) {
        if (view instanceof KeyguardSecurityView) {
            KeyguardSecurityView ksv = (KeyguardSecurityView) view;
            ksv.setKeyguardCallback(this.mCallback);
            ksv.setLockPatternUtils(this.mLockPatternUtils);
            return;
        }
        HwLog.w("KeyguardSecurityView", "View " + view + " is not a KeyguardSecurityView");
    }

    protected void onFinishInflate() {
        this.orientation = this.mContext.getResources().getConfiguration().orientation;
        this.mSecurityViewFlipper = (KeyguardSecurityViewFlipper) findViewById(R$id.view_flipper);
        this.mSecurityViewFlipper.setLockPatternUtils(this.mLockPatternUtils);
    }

    public void setLockPatternUtils(LockPatternUtils utils) {
        this.mLockPatternUtils = utils;
        this.mSecurityModel.setLockPatternUtils(utils);
        this.mSecurityViewFlipper.setLockPatternUtils(this.mLockPatternUtils);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (!(!HwUnlockUtils.isTablet() || this.orientation == this.mContext.getResources().getConfiguration().orientation || KeyguardCfg.isVerifying())) {
            updateSelectedSecurityView();
        }
        this.orientation = this.mContext.getResources().getConfiguration().orientation;
    }

    private void showDialog(String title, String message) {
        AlertDialog dialog = new Builder(this.mContext).setTitle(title).setMessage(message).setNeutralButton(R$string.ok, null).create();
        if (!(this.mContext instanceof Activity)) {
            dialog.getWindow().setType(2009);
        }
        dialog.show();
    }

    private void showDialog(String title, String message, int remaining) {
        View view;
        final AlertDialog dialog = new Builder(this.mContext).setTitle(title).setMessage(message).create();
        int spacePx = this.mContext.getResources().getDimensionPixelSize(R$dimen.exchange_dialog_space);
        if (remaining == 1) {
            view = View.inflate(this.mContext, R$layout.slide_view, null);
            ((SliderView) view.findViewById(R$id.is_slide_button_bg)).setOnConfirmListener(new OnConfirmListener() {
                public void onConfirm() {
                    dialog.dismiss();
                }
            });
        } else {
            view = View.inflate(this.mContext, R$layout.confirm_view, null);
            ((TextView) view.findViewById(R$id.tv_confirm)).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
        dialog.setView(view, spacePx, spacePx, spacePx, spacePx);
        if (!(this.mContext instanceof Activity)) {
            dialog.getWindow().setType(2009);
        }
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showTimeoutDialog(int timeoutMs) {
        HwLog.d("KeyguardSecurityView", "showTimeoutDialog with hw style");
    }

    private void showAlmostAtWipeDialog(int attempts, int remaining, int userType) {
        String message = null;
        String title = this.mContext.getString(R$string.security_policy_notification);
        switch (userType) {
            case 1:
                message = this.mContext.getString(R$string.kg_failed_attempts_almost_at_wipe, new Object[]{Integer.valueOf(attempts), Integer.valueOf(remaining)});
                break;
            case 2:
                message = this.mContext.getString(R$string.kg_failed_attempts_almost_at_erase_profile, new Object[]{Integer.valueOf(attempts), Integer.valueOf(remaining)});
                break;
            case 3:
                message = this.mContext.getString(R$string.kg_failed_attempts_almost_at_erase_user, new Object[]{Integer.valueOf(attempts), Integer.valueOf(remaining)});
                break;
        }
        showDialog(title, message, remaining);
    }

    private void showWipeDialog(int attempts, int userType) {
        String message = null;
        switch (userType) {
            case 1:
                message = this.mContext.getString(R$string.kg_failed_attempts_now_wiping, new Object[]{Integer.valueOf(attempts)});
                break;
            case 2:
                message = this.mContext.getString(R$string.kg_failed_attempts_now_erasing_profile, new Object[]{Integer.valueOf(attempts)});
                break;
            case 3:
                message = this.mContext.getString(R$string.kg_failed_attempts_now_erasing_user, new Object[]{Integer.valueOf(attempts)});
                break;
        }
        showDialog(null, message);
    }

    private void reportFailedUnlockAttempt(int userId, int timeoutMs) {
        int remainingBeforeWipe;
        KeyguardUpdateMonitor monitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        int failedAttempts = VerifyPolicy.getInstance(this.mContext).getFailedAttempts();
        HwLog.d("KeyguardSecurityView", "reportFailedPatternAttempt: #" + failedAttempts);
        DevicePolicyManager dpm = this.mLockPatternUtils.getDevicePolicyManager();
        int failedAttemptsBeforeWipe = dpm.getMaximumFailedPasswordsForWipe(null, userId);
        if (failedAttemptsBeforeWipe > 0) {
            remainingBeforeWipe = failedAttemptsBeforeWipe - failedAttempts;
        } else {
            remainingBeforeWipe = Integer.MAX_VALUE;
        }
        if (remainingBeforeWipe < 5) {
            int expiringUser = dpm.getProfileWithMinimumFailedPasswordsForWipe(userId);
            int userType = 1;
            if (expiringUser == userId) {
                if (expiringUser != 0) {
                    userType = 3;
                }
            } else if (expiringUser != -10000) {
                userType = 2;
            }
            if (remainingBeforeWipe > 0) {
                showAlmostAtWipeDialog(failedAttempts, remainingBeforeWipe, userType);
            } else {
                Slog.i("KeyguardSecurityView", "Too many unlock attempts; user " + expiringUser + " will be wiped!");
                showWipeDialog(failedAttempts, userType);
            }
        }
        monitor.reportFailedStrongAuthUnlockAttempt(userId);
        this.mLockPatternUtils.reportFailedPasswordAttempt(userId);
        if (timeoutMs > 0) {
            showTimeoutDialog(timeoutMs);
        }
    }

    void showPrimarySecurityScreen(boolean turningOff) {
        SecurityMode securityMode = this.mSecurityModel.getSecurityMode();
        HwLog.v("KeyguardSecurityView", "showPrimarySecurityScreen(turningOff=" + turningOff + ")");
        showSecurityScreen(securityMode);
    }

    boolean showNextSecurityScreenOrFinish(boolean authenticated) {
        boolean finish = false;
        boolean strongAuth = false;
        if (!this.mUpdateMonitor.getUserCanSkipBouncer(KeyguardUpdateMonitor.getCurrentUser())) {
            SecurityMode securityMode;
            if (SecurityMode.None != this.mCurrentSecuritySelection) {
                if (authenticated) {
                    switch (-getcom-android-keyguard-KeyguardSecurityModel$SecurityModeSwitchesValues()[this.mCurrentSecuritySelection.ordinal()]) {
                        case 3:
                        case 4:
                            StateMonitor.getInst().cancelEvent(111);
                            strongAuth = true;
                            finish = true;
                            break;
                        case 5:
                            StateMonitor.getInst().cancelEvent(112);
                            strongAuth = true;
                            finish = true;
                            break;
                        case 6:
                        case 7:
                            securityMode = this.mSecurityModel.getSecurityMode();
                            if (securityMode != SecurityMode.None || !this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser())) {
                                showSecurityScreen(securityMode);
                                break;
                            }
                            finish = true;
                            break;
                            break;
                        default:
                            HwLog.v("KeyguardSecurityView", "Bad security screen " + this.mCurrentSecuritySelection + ", fail safe");
                            showPrimarySecurityScreen(false);
                            break;
                    }
                }
            }
            securityMode = this.mSecurityModel.getSecurityMode();
            if (SecurityMode.None == securityMode) {
                finish = true;
            } else {
                showSecurityScreen(securityMode);
            }
        } else {
            finish = true;
        }
        if (finish) {
            this.mSecurityCallback.finish(strongAuth);
        }
        HwLog.d("KeyguardSecurityView", "showNextSecurityScreenOrFinish(" + authenticated + " - " + strongAuth + " - " + finish + ")");
        return finish;
    }

    private void showSecurityScreen(SecurityMode securityMode) {
        HwLog.d("KeyguardSecurityView", "showSecurityScreen(" + securityMode + ")");
        if (securityMode != this.mCurrentSecuritySelection) {
            KeyguardSecurityView oldView = getSecurityView(this.mCurrentSecuritySelection);
            KeyguardSecurityView newView = getSecurityView(securityMode);
            if (oldView != null) {
                oldView.onPause();
                oldView.setKeyguardCallback(this.mNullCallback);
                this.mSecurityViewFlipper.removeView((View) oldView);
            }
            if (securityMode != SecurityMode.None) {
                newView.onResume(2);
                newView.setKeyguardCallback(this.mCallback);
            }
            int childCount = this.mSecurityViewFlipper.getChildCount();
            int securityViewIdForMode = getSecurityViewIdForMode(securityMode);
            for (int i = 0; i < childCount; i++) {
                if (this.mSecurityViewFlipper.getChildAt(i).getId() == securityViewIdForMode) {
                    this.mSecurityViewFlipper.setDisplayedChild(i);
                    break;
                }
            }
            this.mCurrentSecuritySelection = securityMode;
            this.mSecurityCallback.onSecurityModeChanged(securityMode, securityMode != SecurityMode.None ? newView.needsInput() : false);
        }
    }

    private void updateSelectedSecurityView() {
        if (SecurityMode.None != this.mCurrentSecuritySelection) {
            KeyguardSecurityView oldView = getSecurityView(this.mCurrentSecuritySelection);
            if (oldView != null) {
                oldView.onPause();
                oldView.setKeyguardCallback(this.mNullCallback);
                this.mSecurityViewFlipper.removeView((View) oldView);
            }
            KeyguardSecurityView newView = getSecurityView(this.mCurrentSecuritySelection);
            if (newView != null) {
                newView.onResume(2);
                newView.setKeyguardCallback(this.mCallback);
            }
            int childCount = this.mSecurityViewFlipper.getChildCount();
            int securityViewIdForMode = getSecurityViewIdForMode(this.mCurrentSecuritySelection);
            for (int i = 0; i < childCount; i++) {
                if (this.mSecurityViewFlipper.getChildAt(i).getId() == securityViewIdForMode) {
                    this.mSecurityViewFlipper.setDisplayedChild(i);
                    break;
                }
            }
        }
    }

    protected int getSecurityViewIdForMode(SecurityMode securityMode) {
        boolean isSinglehandOpen = SingleHandUtils.isSingleHandOpen(this.mContext);
        switch (-getcom-android-keyguard-KeyguardSecurityModel$SecurityModeSwitchesValues()[securityMode.ordinal()]) {
            case 3:
                return isSinglehandOpen ? R$id.keyguard_single_hand_pin_view : R$id.keyguard_pin_view;
            case 4:
                return R$id.keyguard_password_view;
            case 5:
                return isSinglehandOpen ? R$id.keyguard_single_hand_pattern_view : R$id.keyguard_pattern_view;
            case 6:
                return R$id.keyguard_sim_pin_view;
            case 7:
                return R$id.keyguard_sim_puk_view;
            default:
                return 0;
        }
    }

    protected int getLayoutIdFor(SecurityMode securityMode) {
        boolean isSinglehandOpen = SingleHandUtils.isSingleHandOpen(this.mContext);
        switch (-getcom-android-keyguard-KeyguardSecurityModel$SecurityModeSwitchesValues()[securityMode.ordinal()]) {
            case 3:
                return isSinglehandOpen ? R$layout.keyguard_single_hand_pin_view : R$layout.keyguard_pin_view;
            case 4:
                return R$layout.keyguard_password_view;
            case 5:
                return isSinglehandOpen ? R$layout.keyguard_single_hand_pattern_view : R$layout.keyguard_pattern_view;
            case 6:
                return R$layout.keyguard_sim_pin_view;
            case 7:
                return R$layout.keyguard_sim_puk_view;
            default:
                return 0;
        }
    }

    public SecurityMode getSecurityMode() {
        return this.mSecurityModel.getSecurityMode();
    }

    public SecurityMode getCurrentSecurityMode() {
        return this.mCurrentSecuritySelection;
    }

    public boolean needsInput() {
        return this.mSecurityViewFlipper.needsInput();
    }

    public void setKeyguardCallback(KeyguardSecurityCallback callback) {
        this.mSecurityViewFlipper.setKeyguardCallback(callback);
    }

    public void showPromptReason(int reason) {
        if (this.mCurrentSecuritySelection != SecurityMode.None) {
            if (reason != 0) {
                HwLog.i("KeyguardSecurityView", "Strong auth required, reason: " + reason);
            }
            getSecurityView(this.mCurrentSecuritySelection).showPromptReason(reason);
        }
    }

    public void showMessage(String message, int color) {
        if (this.mCurrentSecuritySelection != SecurityMode.None) {
            getSecurityView(this.mCurrentSecuritySelection).showMessage(message, color);
        }
    }

    private void showBackupSecurityScreen() {
        SecurityMode securityMode = this.mSecurityModel.getBackupSecurityMode(this.mCurrentSecuritySelection);
        HwLog.v("KeyguardSecurityView", "showBackupSecurityScreen");
        showSecurityScreen(securityMode);
    }
}
