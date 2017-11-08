package com.android.keyguard;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityContainer.SecurityCallback;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.huawei.android.media.AudioManagerEx;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.util.DoubleTapUtils;
import com.huawei.keyguard.util.HwLog;
import fyusion.vislib.BuildConfig;
import java.io.File;

public class KeyguardHostView extends FrameLayout implements SecurityCallback {
    private AudioManager mAudioManager;
    private Runnable mCancelAction;
    private OnDismissAction mDismissAction;
    GestureDetector mGestureDetector;
    protected LockPatternUtils mLockPatternUtils;
    private KeyguardSecurityContainer mSecurityContainer;
    private TelephonyManager mTelephonyManager;
    private final KeyguardUpdateMonitorCallback mUpdateCallback;
    protected ViewMediatorCallback mViewMediatorCallback;

    public interface OnDismissAction {
        boolean onDismiss();
    }

    private class SimpleGestureListener extends SimpleOnGestureListener {
        private SimpleGestureListener() {
        }

        public boolean onDoubleTap(MotionEvent event) {
            if (!DoubleTapUtils.readWakeupCheckValue(KeyguardHostView.this.mContext)) {
                return super.onDoubleTap(event);
            }
            DoubleTapUtils.offScreen(KeyguardHostView.this.mContext);
            HwLockScreenReporter.report(KeyguardHostView.this.mContext, 155, BuildConfig.FLAVOR);
            return super.onDoubleTap(event);
        }
    }

    public KeyguardHostView(Context context) {
        this(context, null);
    }

    public KeyguardHostView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mTelephonyManager = null;
        this.mUpdateCallback = new KeyguardUpdateMonitorCallback() {
            public void onUserSwitchComplete(int userId) {
                KeyguardHostView.this.getSecurityContainer().showPrimarySecurityScreen(false);
            }

            public void onTrustGrantedWithFlags(int flags, int userId) {
                if (userId == KeyguardUpdateMonitor.getCurrentUser() && KeyguardHostView.this.isAttachedToWindow()) {
                    boolean bouncerVisible = KeyguardHostView.this.isVisibleToUser();
                    boolean initiatedByUser = (flags & 1) != 0;
                    boolean dismissKeyguard = (flags & 2) != 0;
                    if (initiatedByUser || dismissKeyguard) {
                        if (KeyguardHostView.this.mViewMediatorCallback.isScreenOn() && (bouncerVisible || dismissKeyguard)) {
                            if (!bouncerVisible) {
                                HwLog.i("KeyguardViewBase", "TrustAgent dismissed Keyguard.");
                            }
                            KeyguardHostView.this.dismiss(false);
                        } else {
                            KeyguardHostView.this.mViewMediatorCallback.playTrustedSound();
                        }
                    }
                }
            }
        };
        this.mGestureDetector = new GestureDetector(this.mContext, new SimpleGestureListener());
        KeyguardUpdateMonitor.getInstance(context).registerCallback(this.mUpdateCallback);
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (this.mViewMediatorCallback != null) {
            this.mViewMediatorCallback.keyguardDoneDrawing();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mGestureDetector.onTouchEvent(event)) {
            return event.getAction() == 0;
        } else {
            event.recycle();
            return true;
        }
    }

    public void setOnDismissAction(OnDismissAction action, Runnable cancelAction) {
        if (this.mCancelAction != null) {
            this.mCancelAction.run();
            this.mCancelAction = null;
        }
        this.mDismissAction = action;
        this.mCancelAction = cancelAction;
    }

    public void cancelDismissAction() {
        setOnDismissAction(null, null);
    }

    protected void onFinishInflate() {
        this.mSecurityContainer = (KeyguardSecurityContainer) findViewById(R$id.keyguard_security_container);
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mSecurityContainer.setLockPatternUtils(this.mLockPatternUtils);
        this.mSecurityContainer.setSecurityCallback(this);
        this.mSecurityContainer.showPrimarySecurityScreen(false);
    }

    public void showPrimarySecurityScreen() {
        HwLog.d("KeyguardViewBase", "showPrimarySecurityScreen()");
        this.mSecurityContainer.showPrimarySecurityScreen(false);
    }

    public void showPromptReason(int reason) {
        this.mSecurityContainer.showPromptReason(reason);
    }

    public void showMessage(String message, int color) {
        this.mSecurityContainer.showMessage(message, color);
    }

    public boolean dismiss() {
        return dismiss(false);
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != 32) {
            return super.dispatchPopulateAccessibilityEvent(event);
        }
        event.getText().add(this.mSecurityContainer.getCurrentSecurityModeContentDescription());
        return true;
    }

    protected KeyguardSecurityContainer getSecurityContainer() {
        return this.mSecurityContainer;
    }

    public boolean dismiss(boolean authenticated) {
        return this.mSecurityContainer.showNextSecurityScreenOrFinish(authenticated);
    }

    public void finish(boolean strongAuth) {
        boolean z = false;
        if (this.mDismissAction != null) {
            z = this.mDismissAction.onDismiss();
            this.mDismissAction = null;
            this.mCancelAction = null;
        }
        if (this.mViewMediatorCallback == null) {
            return;
        }
        if (z) {
            this.mViewMediatorCallback.keyguardDonePending(strongAuth);
        } else {
            this.mViewMediatorCallback.keyguardDone(strongAuth);
        }
    }

    public void reset() {
        this.mViewMediatorCallback.resetKeyguard();
    }

    public void onSecurityModeChanged(SecurityMode securityMode, boolean needsInput) {
        if (this.mViewMediatorCallback != null) {
            this.mViewMediatorCallback.setNeedsInput(needsInput);
        }
    }

    public void userActivity() {
        if (this.mViewMediatorCallback != null) {
            this.mViewMediatorCallback.userActivity();
        }
    }

    public void onPause() {
        HwLog.d("KeyguardViewBase", String.format("screen off, instance %s at %s", new Object[]{Integer.toHexString(hashCode()), Long.valueOf(SystemClock.uptimeMillis())}));
        this.mSecurityContainer.showPrimarySecurityScreen(true);
        this.mSecurityContainer.onPause();
        clearFocus();
    }

    public void onResume() {
        HwLog.d("KeyguardViewBase", "screen on, instance " + Integer.toHexString(hashCode()));
        this.mSecurityContainer.onResume(1);
        requestFocus();
    }

    public void startAppearAnimation() {
        this.mSecurityContainer.startAppearAnimation();
    }

    public void startDisappearAnimation(Runnable finishRunnable) {
        if (!this.mSecurityContainer.startDisappearAnimation(finishRunnable) && finishRunnable != null) {
            finishRunnable.run();
        }
    }

    public void startRevertAnimation(Runnable finishRunnable) {
        if (!this.mSecurityContainer.startRevertAnimation(finishRunnable) && finishRunnable != null) {
            finishRunnable.run();
        }
    }

    public void cleanUp() {
        getSecurityContainer().onPause();
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (interceptMediaKey(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean interceptMediaKey(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.getAction() != 0) {
            if (event.getAction() == 1) {
                switch (keyCode) {
                    case 79:
                    case 85:
                    case 86:
                    case 87:
                    case 88:
                    case 89:
                    case 90:
                    case 91:
                    case 126:
                    case 127:
                    case 130:
                    case 222:
                        handleMediaKeyEvent(event);
                        return true;
                    default:
                        break;
                }
            }
        }
        switch (keyCode) {
            case 24:
            case 25:
            case 164:
                return handleVolumStateChanged(keyCode);
            case 79:
            case 86:
            case 87:
            case 88:
            case 89:
            case 90:
            case 91:
            case 130:
            case 222:
                break;
            case 85:
            case 126:
            case 127:
                if (this.mTelephonyManager == null) {
                    this.mTelephonyManager = (TelephonyManager) getContext().getSystemService("phone");
                }
                if (!(this.mTelephonyManager == null || this.mTelephonyManager.getCallState() == 0)) {
                    return true;
                }
        }
        return false;
    }

    private boolean handleVolumStateChanged(int keyCode) {
        int i = -1;
        synchronized (this) {
            if (this.mAudioManager == null) {
                this.mAudioManager = (AudioManager) getContext().getSystemService("audio");
            }
        }
        if (this.mAudioManager.isMusicActive()) {
            int i2;
            AudioManager audioManager = this.mAudioManager;
            if (keyCode == 24) {
                i2 = 1;
            } else {
                i2 = -1;
            }
            audioManager.adjustStreamVolume(3, i2, 0);
            HwLog.d("KeyguardViewBase", "handle volume for music");
        } else if (AudioManagerEx.isFMActive(this.mAudioManager)) {
            AudioManager audioManager2 = this.mAudioManager;
            int i3 = AudioManagerEx.STREAM_FM;
            if (keyCode == 24) {
                i = 1;
            }
            audioManager2.adjustStreamVolume(i3, i, 0);
            HwLog.d("KeyguardViewBase", "handle volume for FM");
        }
        return true;
    }

    private void handleMediaKeyEvent(KeyEvent keyEvent) {
        synchronized (this) {
            if (this.mAudioManager == null) {
                this.mAudioManager = (AudioManager) getContext().getSystemService("audio");
            }
        }
        this.mAudioManager.dispatchMediaKeyEvent(keyEvent);
    }

    public void dispatchSystemUiVisibilityChanged(int visibility) {
        super.dispatchSystemUiVisibilityChanged(visibility);
        if (!(this.mContext instanceof Activity)) {
            HwLog.w("KeyguardViewBase", " SystemUiVisibility changed " + visibility);
            setSystemUiVisibility(4194304);
        }
    }

    public boolean shouldEnableMenuKey() {
        return (!getResources().getBoolean(R$bool.config_disableMenuKeyInLockScreen) || ActivityManager.isRunningInTestHarness()) ? true : new File("/data/local/enable_menu_key").exists();
    }

    public void setViewMediatorCallback(ViewMediatorCallback viewMediatorCallback) {
        this.mViewMediatorCallback = viewMediatorCallback;
        this.mViewMediatorCallback.setNeedsInput(this.mSecurityContainer.needsInput());
    }

    public void setLockPatternUtils(LockPatternUtils utils) {
        this.mLockPatternUtils = utils;
        this.mSecurityContainer.setLockPatternUtils(utils);
    }

    public SecurityMode getSecurityMode() {
        return this.mSecurityContainer.getSecurityMode();
    }

    public SecurityMode getCurrentSecurityMode() {
        return this.mSecurityContainer.getCurrentSecurityMode();
    }
}
