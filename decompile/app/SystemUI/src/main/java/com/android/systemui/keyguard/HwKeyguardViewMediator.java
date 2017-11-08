package com.android.systemui.keyguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.cover.ICoverViewDelegate.Stub;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.ViewGroup;
import com.android.internal.policy.IKeyguardDrawnCallback;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.settings.HwBrightnessController;
import com.android.systemui.statusbar.phone.FingerprintUnlockController;
import com.android.systemui.statusbar.phone.HwPhoneStatusBar;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.phone.StatusBarWindowManager;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.cover.CoverViewManager;
import com.huawei.keyguard.data.MusicInfo;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.monitor.RadarUtil;
import com.huawei.keyguard.policy.VerifyPolicy;
import com.huawei.keyguard.support.FingerprintNavigator;
import com.huawei.keyguard.support.LauncherInteractiveUtil;
import com.huawei.keyguard.theme.KeyguardTheme;
import com.huawei.keyguard.util.FpUtils;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.KeyguardUtils;
import fyusion.vislib.BuildConfig;

public class HwKeyguardViewMediator extends KeyguardViewMediator {
    private HwBrightnessController mBrightnessController = null;
    private BroadcastReceiver mCoverAddedReceiver = null;
    private CoverViewDelegateStub mCoverStub = null;
    private long mDurationOfTurn = 0;
    private FingerprintUnlockController mFingerprintUnlockController;
    BroadcastReceiver mHwBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent == null ? BuildConfig.FLAVOR : intent.getAction();
            if ("com.android.internal.policy.impl.PhoneWindowManager.LOCKED_KEYGUARD".equals(action)) {
                Log.i("KeyguardViewMediator", "com.android.internal.policy.impl.PhoneWindowManager.LOCKED_KEYGUARD");
                synchronized (HwKeyguardViewMediator.this) {
                    HwKeyguardViewMediator.this.doKeyguardLocked(null);
                }
            } else if ("com.android.internal.policy.impl.PhoneWindowManager.UNLOCKED_KEYGUARD".equals(action)) {
                if (HwKeyguardViewMediator.this.mStatusBarKeyguardViewManager.isShowing() && HwKeyguardViewMediator.this.mUpdateMonitor.isDeviceInteractive()) {
                    Log.w("KeyguardViewMediator", "received UNLOCKED_KEYGUARD_ACTION !!!");
                    HwKeyguardViewMediator.this.setOccluded(false);
                    HwKeyguardViewMediator.this.dismiss();
                    FingerprintNavigator.getInst().blockNavigation(true);
                } else {
                    Log.e("KeyguardViewMediator", "receive invalid UNLOCKED_KEYGUARD_ACTION and skipped.");
                }
            }
        }
    };
    private long mLastingCoverOpenTime = 0;
    private HwPhoneStatusBar mPhoneStatusBar;
    private Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        private boolean mAddCoverWhenKeyguardShowing = false;

        private void clearOldMessages() {
            removeMessages(2013);
            removeMessages(2000);
            removeMessages(2024);
            removeMessages(2023);
        }

        public void handleMessage(Message msg) {
            CoverViewManager coverViewManager = CoverViewManager.getInstance(HwKeyguardViewMediator.this.mContext);
            HwLog.v("KeyguardViewMediator", "Handle HwMessage: " + msg.what + (this.mAddCoverWhenKeyguardShowing ? " withKG: " : " withoutKG") + (HwKeyguardViewMediator.this.mHiding ? "; Hiding " : BuildConfig.FLAVOR) + (HwKeyguardViewMediator.this.mStatusBarKeyguardViewManager.isOccluded() ? "; occluded" : BuildConfig.FLAVOR));
            boolean z;
            switch (msg.what) {
                case 2000:
                    HwKeyguardViewMediator.this.preCoverChanged(true);
                    if (!HwKeyguardViewMediator.this.mStatusBarKeyguardViewManager.isShowing() || HwKeyguardViewMediator.this.mStatusBarKeyguardViewManager.isOccluded()) {
                        z = false;
                    } else {
                        z = true;
                    }
                    this.mAddCoverWhenKeyguardShowing = z;
                    HwKeyguardViewMediator.this.afterCoverChanged(true);
                    break;
                case 2001:
                    HwKeyguardViewMediator.this.handleExitAnimate();
                    break;
                case 2011:
                    clearOldMessages();
                    HwKeyguardViewMediator.this.preCoverChanged(true);
                    z = HwKeyguardViewMediator.this.mStatusBarKeyguardViewManager.isShowing() && !HwKeyguardViewMediator.this.mStatusBarKeyguardViewManager.isOccluded();
                    this.mAddCoverWhenKeyguardShowing = z;
                    coverViewManager.addCoverScreenWindow();
                    KeyguardTheme.getInst().checkStyle(HwKeyguardViewMediator.this.mContext, false, false);
                    AppHandler.sendMessage(30);
                    if (!(HwKeyguardViewMediator.this.mStatusBarKeyguardViewManager.isShowing() || !KeyguardUtils.isNeedKeyguard(HwKeyguardViewMediator.this.mContext) || HwKeyguardViewMediator.this.isInCall())) {
                        HwLog.i("KeyguardViewMediator", "DoKeyguardLocked as add cover.");
                        HwKeyguardViewMediator.this.doKeyguardLocked(null);
                    }
                    HwKeyguardViewMediator.this.afterCoverChanged(true);
                    HwLockScreenReporter.report(HwKeyguardViewMediator.this.mContext, 170, BuildConfig.FLAVOR);
                    break;
                case 2013:
                    if (HwKeyguardViewMediator.this.getCoverViewManager().isCoverAdded()) {
                        HwLog.w("KeyguardViewMediator", "check style for keyguard");
                        HwKeyguardViewMediator.this.doKeyguardLocked(null);
                        break;
                    }
                    return;
                case 2021:
                    HwKeyguardViewMediator.this.mLastingCoverOpenTime = SystemClock.uptimeMillis();
                    clearOldMessages();
                    HwKeyguardViewMediator.this.preCoverChanged(false);
                    coverViewManager.removeBarView();
                    coverViewManager.removeCoverScreenWindow();
                    this.mAddCoverWhenKeyguardShowing = false;
                    AppHandler.sendSingleMessage(31);
                    sendEmptyMessageDelayed(2024, 500);
                    HwLockScreenReporter.report(HwKeyguardViewMediator.this.mContext, 171, BuildConfig.FLAVOR);
                    noticeCoverRemoved();
                    HwKeyguardViewMediator.this.afterCoverChanged(false);
                    break;
                case 2024:
                    gotoInCallUiIfNotOccluded();
                    break;
                default:
                    HwLog.w("KeyguardViewMediator", "remove cover with state" + HwKeyguardViewMediator.this.mShowing + " " + HwKeyguardViewMediator.this.mStatusBarKeyguardViewManager.isShowing() + " " + HwKeyguardViewMediator.this.mStatusBarKeyguardViewManager.isInDismiss());
                    if (!HwKeyguardViewMediator.this.mStatusBarKeyguardViewManager.isShowing()) {
                        HwKeyguardViewMediator.this.mStatusBarKeyguardViewManager.updateStates();
                        break;
                    } else {
                        HwKeyguardViewMediator.this.mStatusBarKeyguardViewManager.reset();
                        break;
                    }
            }
        }

        private void noticeCoverRemoved() {
            Intent intent = new Intent("com.huawei.android.cover.STATE");
            intent.putExtra("coverOpen", true);
            intent.addFlags(268435456);
            try {
                HwKeyguardViewMediator.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                HwLog.w("KeyguardViewMediator", "sendBroadcastAsUser COVER_STATE_CHANGED_ACTION");
            } catch (SecurityException e) {
                HwLog.e("KeyguardViewMediator", "sendBroadcastAsUser with permission deny!");
            }
        }

        private void gotoInCallUiIfNotOccluded() {
            if (HwKeyguardViewMediator.this.isShowingAndNotOccluded() && HwKeyguardViewMediator.this.isInCall()) {
                HwLog.w("KeyguardViewMediator", "gotoInCallUiIfNotOccluded. " + HwKeyguardViewMediator.this.isInCall());
                HwKeyguardViewMediator.this.resumeCall();
            }
        }
    };
    private long mWakingupTime = 0;

    private class CoverViewDelegateStub extends Stub {
        private CoverViewDelegateStub() {
        }

        public void addCoverScreenWindow() throws RemoteException {
            HwKeyguardViewMediator.this.mUIHandler.removeMessages(2021);
            HwKeyguardViewMediator.this.mUIHandler.removeMessages(2011);
            HwKeyguardViewMediator.this.mUIHandler.sendEmptyMessage(2011);
            HwLog.w("KeyguardViewMediator", "KGSvcCall cover addCoverScreenWindow.");
        }

        public void removeCoverScreenWindow() throws RemoteException {
            HwKeyguardViewMediator.this.mUIHandler.removeMessages(2011);
            HwKeyguardViewMediator.this.mUIHandler.removeMessages(2021);
            HwKeyguardViewMediator.this.mUIHandler.sendEmptyMessage(2021);
            HwLog.w("KeyguardViewMediator", "KGSvcCall cover removeCoverScreenWindow.");
        }
    }

    private CoverViewManager getCoverViewManager() {
        return CoverViewManager.getInstance(this.mContext);
    }

    protected boolean isCoverAdded() {
        return CoverViewManager.getInstance(this.mContext).isCoverAdded();
    }

    public StatusBarKeyguardViewManager registerStatusBar(PhoneStatusBar phoneStatusBar, ViewGroup container, StatusBarWindowManager statusBarWindowManager, ScrimController scrimController, FingerprintUnlockController fingerprintUnlockController) {
        this.mPhoneStatusBar = (HwPhoneStatusBar) phoneStatusBar;
        this.mFingerprintUnlockController = fingerprintUnlockController;
        listenCoverItemAdded();
        return super.registerStatusBar(phoneStatusBar, container, statusBarWindowManager, scrimController, fingerprintUnlockController);
    }

    protected void sendUserPresentBroadcast() {
        try {
            super.sendUserPresentBroadcast();
        } catch (SecurityException e) {
            RadarUtil.uploadsendPresentBroadcastException(this.mContext, "sendUserPresentBroadcast throw exception: " + e.toString());
        }
        if (this.mBootCompleted) {
            LauncherInteractiveUtil.sendUnockedEventByCallProvider(this.mContext);
        }
    }

    protected void handleSetOccluded(boolean isOccluded) {
        boolean z = false;
        Log.d("KeyguardViewMediator", "handleSetOccluded " + isOccluded);
        super.handleSetOccluded(isOccluded);
        synchronized (this) {
            KeyguardUpdateMonitor keyguardUpdateMonitor = this.mUpdateMonitor;
            if (this.mShowing && !this.mHiding) {
                z = true;
            }
            keyguardUpdateMonitor.setKeyguardViewState(z, isOccluded);
        }
        if (!isOccluded) {
            this.mUIHandler.removeMessages(2023);
        }
    }

    public void onDreamingStarted() {
        super.onDreamingStarted();
        this.mUpdateMonitor.setDreamingState(true);
    }

    public void onDreamingStopped() {
        super.onDreamingStopped();
        this.mUpdateMonitor.setDreamingState(false);
    }

    protected void handleReset() {
        super.handleReset();
    }

    protected void showLocked(final Bundle options) {
        if (this.mStatusBarKeyguardViewManager.hasIninted()) {
            super.showLocked(options);
            maybeCreateKeyguard();
            return;
        }
        Log.w("KeyguardViewMediator", "statusBarKeyguardViewManager is not inited.");
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                HwKeyguardViewMediator.this.showLocked(options);
            }
        }, 100);
    }

    public void onBootCompleted() {
        super.onBootCompleted();
        if (this.mCoverStub == null) {
            this.mCoverStub = new CoverViewDelegateStub();
            if (!CoverViewManager.getInstance(this.mContext).isCoverOpen() ? CoverViewManager.isWindowedCover(this.mContext) : false) {
                this.mUIHandler.sendEmptyMessage(2000);
            }
            CoverViewManager.getInstance(this.mContext).registCoverBinder(this.mCoverStub);
        }
        LauncherInteractiveUtil.setBootCompleteTime();
    }

    private void maybeCreateKeyguard() {
        if (!this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser())) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    Log.d("KeyguardViewMediator", "send keyguard locked intent");
                    HwKeyguardViewMediator.this.mContext.sendBroadcastAsUser(new Intent("com.android.keyguard.keyguardlocked"), new UserHandle(KeyguardUpdateMonitor.getCurrentUser()), "com.android.keyguard.permission.KEYGUARD_LOCKED");
                }
            });
            LauncherInteractiveUtil.sendLockedEventByCallProvider(this.mContext);
        }
        MusicInfo.getInst().resetShowingState();
    }

    public void dismiss() {
        if (isCoverAdded()) {
            Log.w("KeyguardViewMediator", "Block dismiss as cover is closed");
        } else {
            super.dismiss();
        }
    }

    protected void setupLocked() {
        super.setupLocked();
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        IntentFilter filter = new IntentFilter("com.android.internal.policy.impl.PhoneWindowManager.LOCKED_KEYGUARD");
        filter.addAction("com.android.internal.policy.impl.PhoneWindowManager.UNLOCKED_KEYGUARD");
        this.mContext.registerReceiver(this.mHwBroadcastReceiver, filter);
    }

    public void start() {
        super.start();
        CoverViewManager coverViewManager = CoverViewManager.getInstance(this.mContext);
        if ("1".equals(SystemProperties.get("sys.boot_completed", "0"))) {
            HwLog.w("KeyguardViewMediator", "Keyguard start before BOOT_COMPLETED, reset cover binder later");
            if (this.mCoverStub == null) {
                this.mCoverStub = new CoverViewDelegateStub();
                coverViewManager.registCoverBinder(this.mCoverStub);
            }
        } else {
            FpUtils.setResetTypeHasRead(this.mContext, false);
        }
        HwKeyguardUpdateMonitor.getInstance(this.mContext).setChangeTipsForAbnormalReboot(FpUtils.isChangeTipsForAbnormalReboot(this.mContext));
        HwLog.w("KeyguardViewMediator", "KeyguardService finish create");
    }

    private void preCoverChanged(boolean added) {
        getCoverViewManager().setCoverAdded(added);
        if (!added) {
            if (this.mBrightnessController != null) {
                this.mBrightnessController.onCoverModeChanged(added);
            }
            if (this.mPhoneStatusBar != null) {
                this.mPhoneStatusBar.setCoverMode(added);
            }
        }
    }

    private void afterCoverChanged(boolean added) {
        if (added) {
            if (this.mPhoneStatusBar != null) {
                this.mPhoneStatusBar.setCoverMode(added);
            }
            if (this.mBrightnessController != null) {
                this.mBrightnessController.onCoverModeChanged(added);
            }
        }
        if (!added) {
            this.mStatusBarKeyguardViewManager.afterCoverRemoved();
        }
    }

    private void onCoverChanged(String pkg, boolean added) {
        getCoverViewManager().onCoverChanged(pkg, added);
    }

    private void listenCoverItemAdded() {
        if (this.mCoverAddedReceiver == null && getCoverViewManager().isLargeCover(this.mContext)) {
            this.mCoverAddedReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (intent == null || !"com.huawei.action.coverview_window_changed".equals(intent.getAction())) {
                        HwLog.w("KeyguardViewMediator", "Cover skip message listener get message. " + intent);
                        return;
                    }
                    CoverViewManager coverViewManager = HwKeyguardViewMediator.this.getCoverViewManager();
                    if (intent.getIntExtra("change_type", -1) == 1) {
                        if (!coverViewManager.isCoverAdded()) {
                            HwLog.w("KeyguardViewMediator", "Add cover with. " + intent.getStringExtra("package"));
                        }
                        HwKeyguardViewMediator.this.onCoverChanged(intent.getStringExtra("package"), true);
                    } else {
                        HwKeyguardViewMediator.this.onCoverChanged(intent.getStringExtra("package"), false);
                        if (!HwKeyguardViewMediator.this.mStatusBarKeyguardViewManager.isShowing()) {
                            HwKeyguardViewMediator.this.mUIHandler.sendEmptyMessageDelayed(2013, 500);
                        }
                    }
                }
            };
            this.mContext.registerReceiver(this.mCoverAddedReceiver, new IntentFilter("com.huawei.action.coverview_window_changed"));
        }
    }

    protected boolean isInCall() {
        return getTelecommManager().isInCall();
    }

    private void resumeCall() {
        getTelecommManager().showInCallScreen(false);
    }

    private TelecomManager getTelecommManager() {
        return (TelecomManager) this.mContext.getSystemService("telecom");
    }

    public void onServiceDestroyed() {
        HwLog.w("KeyguardViewMediator", "Service destroyed.");
        CoverViewManager.getInstance(this.mContext).removeCoverScreenWindow();
    }

    protected void handleShow(Bundle options) {
        super.handleShow(options);
        synchronized (this) {
            this.mUpdateMonitor.setKeyguardViewState(this.mShowing, this.mOccluded);
        }
        AppHandler.sendImmediateMessage(12);
        FingerprintNavigator.getInst().blockNavigation(false);
    }

    protected void handleHide() {
        boolean z = false;
        super.handleHide();
        synchronized (this) {
            KeyguardUpdateMonitor keyguardUpdateMonitor = this.mUpdateMonitor;
            if (this.mShowing && !this.mHiding) {
                z = true;
            }
            keyguardUpdateMonitor.setKeyguardViewState(z, this.mOccluded);
        }
        if (this.mHiding) {
            AppHandler.sendMessage(11);
        }
        if (VerifyPolicy.getInstance(this.mContext).isLongTimeDecrypt()) {
            this.mStatusBarKeyguardViewManager.forceShowWallpaper(true);
            HwLog.w("KeyguardViewMediator", "set show wallpaper when first time startup in Encryp version");
        }
    }

    protected void handleStartKeyguardExitAnimation(long startTime, long fadeoutDuration) {
        super.handleStartKeyguardExitAnimation(startTime, fadeoutDuration);
        this.mStatusBarKeyguardViewManager.forceShowWallpaper(false);
    }

    public boolean isInfastScreenMode() {
        if (this.mFingerprintUnlockController == null) {
            return false;
        }
        return this.mFingerprintUnlockController.isInfastScreenMode();
    }

    public void notifyWakeupDevice() {
        if (this.mFingerprintUnlockController != null) {
            this.mFingerprintUnlockController.notifyWakeupDevice();
        }
    }

    private void showDropbackView() {
        if (this.mPhoneStatusBar != null) {
            this.mPhoneStatusBar.showDropbackView();
        }
    }

    public void handleDelayScreenLockStatusCheck() {
        if (this.mUpdateMonitor.isShowing()) {
            Log.d("KeyguardViewMediator", "handleDelayScreenLockStatusCheck showDropbackView ");
            showDropbackView();
        }
    }

    public void dealWithkeyguardView() {
        Log.d("KeyguardViewMediator", "handleNotifyScreenTurnedOn End with : " + this.mDurationOfTurn);
        if (KeyguardCfg.isFpPerformanceOpen() && this.mDurationOfTurn > 500) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1000), 400);
        }
        this.mDurationOfTurn = 0;
    }

    public void onStartedWakingUp() {
        this.mWakingupTime = System.currentTimeMillis();
        HwLockScreenReporter.report(this.mContext, 122, BuildConfig.FLAVOR);
        super.onStartedWakingUp();
    }

    public void onScreenTurningOn(IKeyguardDrawnCallback callback) {
        this.mDurationOfTurn = System.currentTimeMillis() - this.mWakingupTime;
        super.onScreenTurningOn(callback);
    }

    public void setBrightnessController(HwBrightnessController controller) {
        this.mBrightnessController = controller;
    }

    public void dealWithUseFingerAfterCoverOpen() {
        if (3000 > SystemClock.uptimeMillis() - this.mLastingCoverOpenTime) {
            synchronized (this) {
                this.mKeyguardExitAnimateStart = false;
            }
            this.mUIHandler.sendMessageDelayed(this.mUIHandler.obtainMessage(2001), 200);
            return;
        }
        Log.d("KeyguardViewMediator", "dealWithUseFingerAfterCoverOpen do nothing");
    }

    public boolean isShowingAndOccluded() {
        return this.mShowing ? this.mOccluded : false;
    }

    protected void handleExitAfterHide() {
        super.handleExitAfterHide();
        this.mUpdateMonitor.setFpAuthenticated(false);
    }

    public void onStartedGoingToSleep(int why) {
        super.onStartedGoingToSleep(why);
        synchronized (this) {
            if (why == 2) {
                if (this.mPendingReset && !this.mPendingLock) {
                    if (!this.mLockLater && VerifyPolicy.getInstance(this.mContext).isLongTimeDecryptForPowerOff()) {
                        this.mPendingLock = true;
                        Log.i("KeyguardViewMediator", "onStartedGoingToSleep should doKeyguardLock");
                    }
                }
            }
        }
    }
}
