package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.UserManager;
import android.util.Slog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardHostView;
import com.android.keyguard.KeyguardHostView.OnDismissAction;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.DejankUtils;
import com.android.systemui.R;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.utils.UserSwitchUtils;
import com.huawei.keyguard.inf.HwKeyguardPolicy;

public class KeyguardBouncer {
    private int mBouncerPromptReason;
    protected ViewMediatorCallback mCallback;
    protected ViewGroup mContainer;
    protected Context mContext;
    private FalsingManager mFalsingManager;
    private boolean mInDismiss = false;
    protected KeyguardHostView mKeyguardView;
    protected LockPatternUtils mLockPatternUtils;
    protected ViewGroup mRoot;
    private final Runnable mShowRunnable = new Runnable() {
        public void run() {
            KeyguardBouncer.this.mRoot.setVisibility(0);
            KeyguardBouncer.this.mKeyguardView.onResume();
            KeyguardBouncer.this.showPromptReason(KeyguardBouncer.this.mBouncerPromptReason);
            if (KeyguardBouncer.this.mKeyguardView.getHeight() != 0) {
                KeyguardBouncer.this.mKeyguardView.startAppearAnimation();
            } else {
                KeyguardBouncer.this.mKeyguardView.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                    public boolean onPreDraw() {
                        KeyguardBouncer.this.mKeyguardView.getViewTreeObserver().removeOnPreDrawListener(this);
                        KeyguardBouncer.this.mKeyguardView.startAppearAnimation();
                        return true;
                    }
                });
                KeyguardBouncer.this.mKeyguardView.requestLayout();
            }
            KeyguardBouncer.this.mShowingSoon = false;
            KeyguardBouncer.this.mInDismiss = false;
            KeyguardBouncer.this.mKeyguardView.sendAccessibilityEvent(32);
        }
    };
    private boolean mShowingSoon;
    private KeyguardUpdateMonitorCallback mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        public void onStrongAuthStateChanged(int userId) {
            KeyguardBouncer.this.mBouncerPromptReason = KeyguardBouncer.this.mCallback.getBouncerPromptReason();
        }
    };
    private StatusBarWindowManager mWindowManager;

    public KeyguardBouncer(Context context, ViewMediatorCallback callback, LockPatternUtils lockPatternUtils, StatusBarWindowManager windowManager, ViewGroup container) {
        this.mContext = context;
        this.mCallback = callback;
        this.mLockPatternUtils = lockPatternUtils;
        this.mContainer = container;
        this.mWindowManager = windowManager;
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mUpdateMonitorCallback);
        this.mFalsingManager = FalsingManager.getInstance(this.mContext);
    }

    public boolean isInDismiss() {
        return this.mInDismiss;
    }

    public boolean isShowingSoon() {
        return this.mShowingSoon;
    }

    public void show(boolean resetSecuritySelection) {
        int keyguardUserId = KeyguardUpdateMonitor.getCurrentUser();
        if (keyguardUserId != 0 || !UserManager.isSplitSystemUser()) {
            this.mFalsingManager.onBouncerShown();
            ensureView();
            if (resetSecuritySelection) {
                this.mKeyguardView.showPrimarySecurityScreen();
            }
            if (this.mRoot.getVisibility() != 0 && !this.mShowingSoon) {
                boolean z;
                int activeUserId = UserSwitchUtils.getCurrentUser();
                if (UserManager.isSplitSystemUser() && activeUserId == 0) {
                    z = true;
                } else {
                    z = false;
                }
                boolean allowDismissKeyguard = !z ? activeUserId == keyguardUserId : false;
                if (allowDismissKeyguard && this.mKeyguardView.dismiss()) {
                    this.mInDismiss = true;
                    return;
                }
                this.mInDismiss = false;
                if (!allowDismissKeyguard) {
                    Slog.w("KeyguardBouncer", "User can't dismiss keyguard: " + activeUserId + " != " + keyguardUserId);
                }
                this.mShowingSoon = true;
                DejankUtils.postAfterTraversal(this.mShowRunnable);
            }
        }
    }

    public void revertToKeyguard() {
        final Runnable finishRunner = new Runnable() {
            public void run() {
                if (KeyguardBouncer.this.mRoot != null) {
                    KeyguardBouncer.this.mRoot.setVisibility(8);
                }
            }
        };
        if (this.mKeyguardView.getHeight() != 0) {
            this.mKeyguardView.startRevertAnimation(finishRunner);
        } else {
            this.mKeyguardView.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                public boolean onPreDraw() {
                    KeyguardBouncer.this.mKeyguardView.getViewTreeObserver().removeOnPreDrawListener(this);
                    KeyguardBouncer.this.mKeyguardView.startRevertAnimation(finishRunner);
                    return true;
                }
            });
            this.mKeyguardView.requestLayout();
        }
        this.mKeyguardView.sendAccessibilityEvent(32);
    }

    public void showPromptReason(int reason) {
        this.mKeyguardView.showPromptReason(reason);
    }

    public void showMessage(String message, int color) {
        this.mKeyguardView.showMessage(message, color);
    }

    private void cancelShowRunnable() {
        DejankUtils.removeCallbacks(this.mShowRunnable);
        this.mShowingSoon = false;
        this.mInDismiss = false;
    }

    public void showWithDismissAction(OnDismissAction r, Runnable cancelAction) {
        ensureView();
        this.mKeyguardView.setOnDismissAction(r, cancelAction);
        show(false);
    }

    public void hide(boolean destroyView) {
        this.mFalsingManager.onBouncerHidden();
        cancelShowRunnable();
        if (this.mKeyguardView != null) {
            this.mKeyguardView.cancelDismissAction();
            this.mKeyguardView.cleanUp();
        }
        if (destroyView) {
            removeView();
        } else if (this.mRoot != null) {
            this.mRoot.setVisibility(4);
        }
        this.mInDismiss = false;
    }

    public void startPreHideAnimation(Runnable runnable) {
        if (this.mKeyguardView != null) {
            this.mKeyguardView.startDisappearAnimation(runnable);
        } else if (runnable != null) {
            runnable.run();
        }
    }

    public void onScreenTurnedOff() {
        if (this.mKeyguardView != null && this.mRoot != null && this.mRoot.getVisibility() == 0) {
            this.mKeyguardView.onPause();
        }
    }

    public boolean isShowing() {
        if (this.mShowingSoon) {
            return true;
        }
        return this.mRoot != null && this.mRoot.getVisibility() == 0;
    }

    public boolean isShowingWithSecure() {
        boolean z = true;
        if (this.mKeyguardView == null || !isShowing()) {
            return false;
        }
        SecurityMode mode = this.mKeyguardView.getSecurityMode();
        if (!(mode == SecurityMode.Pattern || mode == SecurityMode.PIN || mode == SecurityMode.Password)) {
            z = false;
        }
        return z;
    }

    public void prepare() {
        boolean wasInitialized = this.mRoot != null;
        ensureView();
        if (wasInitialized) {
            this.mKeyguardView.showPrimarySecurityScreen();
        }
        this.mBouncerPromptReason = this.mCallback.getBouncerPromptReason();
    }

    protected void ensureView() {
        if (this.mRoot == null) {
            inflateView();
        }
    }

    protected void inflateView() {
        removeView();
        this.mRoot = (ViewGroup) LayoutInflater.from(this.mContext).inflate(R.layout.keyguard_bouncer, null);
        this.mKeyguardView = (KeyguardHostView) this.mRoot.findViewById(R.id.keyguard_host_view);
        this.mKeyguardView.setLockPatternUtils(this.mLockPatternUtils);
        this.mKeyguardView.setViewMediatorCallback(this.mCallback);
        this.mContainer.addView(this.mRoot, this.mContainer.getChildCount());
        this.mRoot.setVisibility(4);
        this.mRoot.setSystemUiVisibility(2097152);
    }

    protected void removeView() {
        if (this.mRoot != null && this.mRoot.getParent() == this.mContainer) {
            this.mContainer.removeView(this.mRoot);
            this.mRoot = null;
        }
    }

    public boolean needsFullscreenBouncer() {
        boolean z = true;
        ensureView();
        if (HwKeyguardPolicy.getInst().isSkipKeyguardView(this.mContext)) {
            return true;
        }
        if (this.mKeyguardView == null) {
            return false;
        }
        SecurityMode mode = this.mKeyguardView.getSecurityMode();
        if (!(mode == SecurityMode.SimPin || mode == SecurityMode.SimPuk)) {
            z = false;
        }
        return z;
    }

    public boolean isFullscreenBouncer() {
        boolean z = true;
        if (HwKeyguardPolicy.getInst().isSkipKeyguardView(this.mContext)) {
            return true;
        }
        if (this.mKeyguardView == null) {
            return false;
        }
        SecurityMode mode = this.mKeyguardView.getCurrentSecurityMode();
        if (!(mode == SecurityMode.SimPin || mode == SecurityMode.SimPuk)) {
            z = false;
        }
        return z;
    }

    public boolean isSecure() {
        return this.mKeyguardView == null || this.mKeyguardView.getSecurityMode() != SecurityMode.None;
    }

    public boolean shouldDismissOnMenuPressed() {
        return this.mKeyguardView.shouldEnableMenuKey();
    }

    public boolean interceptMediaKey(KeyEvent event) {
        ensureView();
        return this.mKeyguardView.interceptMediaKey(event);
    }

    public void notifyKeyguardAuthenticated(boolean strongAuth) {
        ensureView();
        this.mKeyguardView.finish(strongAuth);
    }
}
