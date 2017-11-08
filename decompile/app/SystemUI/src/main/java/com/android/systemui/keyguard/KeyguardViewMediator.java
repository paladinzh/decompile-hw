package com.android.systemui.keyguard;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.app.StatusBarManager;
import android.app.trust.TrustManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.android.internal.policy.IKeyguardDrawnCallback;
import com.android.internal.policy.IKeyguardExitCallback;
import com.android.internal.policy.IKeyguardStateCallback;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardAbsKeyInputView;
import com.android.keyguard.KeyguardDisplayManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitor.StrongAuthTracker;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.SystemUI;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.statusbar.phone.FingerprintUnlockController;
import com.android.systemui.statusbar.phone.HwStatusBarKeyguardViewManager;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.phone.StatusBarWindowManager;
import com.android.systemui.utils.CalibrateUtil;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;
import com.android.systemui.utils.analyze.PerfDebugUtils;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.cover.CoverViewManager;
import com.huawei.keyguard.data.MusicInfo;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.policy.RetryPolicy;
import com.huawei.keyguard.support.HiddenSpace;
import com.huawei.keyguard.theme.KeyguardTheme;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public abstract class KeyguardViewMediator extends SystemUI {
    private static final long FAST_FP_UNLOCK_WAKUP_DELAY = SystemProperties.getLong("ro.config.fp_unlock_delay", 30);
    public static final boolean IS_EMUI_LITE = SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
    private static final Intent USER_PRESENT_INTENT = new Intent("android.intent.action.USER_PRESENT").addFlags(603979776);
    private static boolean mIsFirstLocked = true;
    private AlarmManager mAlarmManager;
    private AudioManager mAudioManager;
    protected boolean mBootCompleted;
    private boolean mBootSendUserPresent;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int sequence;
            KeyguardViewMediator keyguardViewMediator;
            if ("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD".equals(intent.getAction())) {
                sequence = intent.getIntExtra("seq", 0);
                Log.d("KeyguardViewMediator", "received DELAYED_KEYGUARD_ACTION with seq = " + sequence + ", mDelayedShowingSequence = " + KeyguardViewMediator.this.mDelayedShowingSequence);
                keyguardViewMediator = KeyguardViewMediator.this;
                synchronized (keyguardViewMediator) {
                    if (KeyguardViewMediator.this.mDelayedShowingSequence == sequence) {
                        KeyguardViewMediator.this.doKeyguardLocked(null);
                    }
                }
            } else if ("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_LOCK".equals(intent.getAction())) {
                sequence = intent.getIntExtra("seq", 0);
                int userId = intent.getIntExtra("android.intent.extra.USER_ID", 0);
                if (userId != 0) {
                    keyguardViewMediator = KeyguardViewMediator.this;
                    synchronized (keyguardViewMediator) {
                        if (KeyguardViewMediator.this.mDelayedProfileShowingSequence == sequence) {
                            KeyguardViewMediator.this.lockProfile(userId);
                        }
                    }
                } else {
                    return;
                }
            } else {
                return;
            }
        }
    };
    private int mDelayedProfileShowingSequence;
    private int mDelayedShowingSequence;
    private boolean mDeviceInteractive;
    private IKeyguardDrawnCallback mDrawnCallback;
    private IKeyguardExitCallback mExitSecureCallback;
    private boolean mExternallyEnabled = true;
    private boolean mGoingToSleep;
    protected Handler mHandler = new Handler(Looper.myLooper(), null, true) {
        public void handleMessage(Message msg) {
            boolean z = true;
            KeyguardViewMediator keyguardViewMediator;
            switch (msg.what) {
                case 2:
                    KeyguardViewMediator.this.handleShow((Bundle) msg.obj);
                    return;
                case 3:
                    KeyguardViewMediator.this.handleHide();
                    return;
                case 4:
                    KeyguardViewMediator.this.handleReset();
                    return;
                case 5:
                    KeyguardViewMediator.this.handleVerifyUnlock();
                    return;
                case 6:
                    KeyguardViewMediator.this.handleNotifyFinishedGoingToSleep();
                    return;
                case 7:
                    KeyguardViewMediator.this.handleNotifyScreenTurningOn((IKeyguardDrawnCallback) msg.obj);
                    return;
                case 9:
                    keyguardViewMediator = KeyguardViewMediator.this;
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    keyguardViewMediator.handleKeyguardDone(z);
                    return;
                case 10:
                    KeyguardViewMediator.this.handleKeyguardDoneDrawing();
                    return;
                case 12:
                    keyguardViewMediator = KeyguardViewMediator.this;
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    keyguardViewMediator.handleSetOccluded(z);
                    return;
                case 13:
                    synchronized (KeyguardViewMediator.this) {
                        KeyguardViewMediator.this.doKeyguardLocked((Bundle) msg.obj);
                    }
                    return;
                case 17:
                    KeyguardViewMediator.this.handleDismiss();
                    return;
                case 18:
                    StartKeyguardExitAnimParams params = msg.obj;
                    KeyguardViewMediator.this.handleStartKeyguardExitAnimation(params.startTime, params.fadeoutDuration);
                    FalsingManager.getInstance(KeyguardViewMediator.this.mContext).onSucccessfulUnlock();
                    return;
                case 19:
                    break;
                case 20:
                    Log.w("KeyguardViewMediator", "Timeout while waiting for activity drawn!");
                    break;
                case 21:
                    KeyguardViewMediator.this.handleNotifyStartedWakingUp();
                    return;
                case 22:
                    KeyguardViewMediator.this.handleNotifyScreenTurnedOn();
                    return;
                case 23:
                    KeyguardViewMediator.this.handleNotifyScreenTurnedOff();
                    return;
                case 24:
                    KeyguardViewMediator.this.handleNotifyStartedGoingToSleep();
                    return;
                case 100:
                    KeyguardViewMediator.this.handleResetToBouncerStateLocked();
                    return;
                case 1000:
                    KeyguardViewMediator.this.handleDelayScreenLockStatusCheck();
                    return;
                case 1001:
                    KeyguardViewMediator.this.handleExitAfterHide();
                    return;
                default:
                    return;
            }
            KeyguardViewMediator.this.handleOnActivityDrawn();
        }
    };
    protected Animation mHideAnimation;
    private boolean mHideAnimationRun = false;
    protected boolean mHiding;
    private boolean mInputRestricted;
    private boolean mIsPerUserLock;
    private KeyguardDisplayManager mKeyguardDisplayManager;
    private boolean mKeyguardDonePending = false;
    protected boolean mKeyguardExitAnimateStart = true;
    private final Runnable mKeyguardGoingAwayRunnable = new Runnable() {
        public void run() {
            try {
                KeyguardViewMediator.this.mStatusBarKeyguardViewManager.keyguardGoingAway();
                int flags = 0;
                if (KeyguardViewMediator.this.mStatusBarKeyguardViewManager.shouldDisableWindowAnimationsForUnlock() || KeyguardViewMediator.this.mWakeAndUnlocking) {
                    flags = 2;
                }
                if (KeyguardViewMediator.this.mStatusBarKeyguardViewManager.isGoingToNotificationShade()) {
                    flags |= 1;
                }
                if (KeyguardViewMediator.this.mStatusBarKeyguardViewManager.isUnlockWithWallpaper()) {
                    flags |= 4;
                }
                ActivityManagerNative.getDefault().keyguardGoingAway(flags);
            } catch (RemoteException e) {
                Log.e("KeyguardViewMediator", "Error while calling WindowManager", e);
            }
        }
    };
    private final ArrayList<IKeyguardStateCallback> mKeyguardStateCallbacks = new ArrayList();
    protected boolean mLockLater;
    protected LockPatternUtils mLockPatternUtils;
    private int mLockSoundId;
    private int mLockSoundStreamId;
    private float mLockSoundVolume;
    private SoundPool mLockSounds;
    private boolean mNeedToReshowWhenReenabled = false;
    protected boolean mOccluded = false;
    private PowerManager mPM;
    protected boolean mPendingLock;
    protected boolean mPendingReset;
    private String mPhoneState = TelephonyManager.EXTRA_STATE_IDLE;
    private SearchManager mSearchManager;
    private WakeLock mShowKeyguardWakeLock;
    protected boolean mShowing;
    protected HwStatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private StatusBarManager mStatusBarManager;
    private boolean mSwitchingUser;
    private boolean mSystemReady;
    private TrustManager mTrustManager;
    private int mTrustedSoundId;
    private int mUiSoundsStreamType;
    private int mUnlockSoundId;
    KeyguardUpdateMonitorCallback mUpdateCallback = new KeyguardUpdateMonitorCallback() {
        private static final /* synthetic */ int[] -com-android-internal-telephony-IccCardConstants$StateSwitchesValues = null;

        private static /* synthetic */ int[] -getcom-android-internal-telephony-IccCardConstants$StateSwitchesValues() {
            if (-com-android-internal-telephony-IccCardConstants$StateSwitchesValues != null) {
                return -com-android-internal-telephony-IccCardConstants$StateSwitchesValues;
            }
            int[] iArr = new int[State.values().length];
            try {
                iArr[State.ABSENT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[State.CARD_IO_ERROR.ordinal()] = 7;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[State.DEACTIVED.ordinal()] = 8;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[State.NETWORK_LOCKED.ordinal()] = 9;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[State.NOT_READY.ordinal()] = 2;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[State.PERM_DISABLED.ordinal()] = 3;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[State.PIN_REQUIRED.ordinal()] = 4;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[State.PUK_REQUIRED.ordinal()] = 5;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[State.READY.ordinal()] = 6;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[State.UNKNOWN.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            -com-android-internal-telephony-IccCardConstants$StateSwitchesValues = iArr;
            return iArr;
        }

        public void onUserSwitching(int userId) {
            synchronized (KeyguardViewMediator.this) {
                KeyguardViewMediator.this.mSwitchingUser = true;
                KeyguardViewMediator.this.resetKeyguardDonePendingLocked();
                KeyguardViewMediator.this.resetStateLocked();
                KeyguardViewMediator.this.adjustStatusBarLocked();
            }
        }

        public void onUserSwitchComplete(int userId) {
            KeyguardViewMediator.this.mSwitchingUser = false;
        }

        public void onUserInfoChanged(int userId) {
        }

        public void onPhoneStateChanged(int phoneState) {
            synchronized (KeyguardViewMediator.this) {
                if (phoneState == 0) {
                    if (!KeyguardViewMediator.this.mDeviceInteractive) {
                        if (KeyguardViewMediator.this.mExternallyEnabled) {
                            Log.d("KeyguardViewMediator", "screen is off and call ended, let's make sure the keyguard is showing");
                            KeyguardViewMediator.this.doKeyguardLocked(null);
                        }
                    }
                }
            }
        }

        public void onClockVisibilityChanged() {
            KeyguardViewMediator.this.adjustStatusBarLocked();
        }

        public void onDeviceProvisioned() {
            KeyguardViewMediator.this.sendUserPresentBroadcast();
            synchronized (KeyguardViewMediator.this) {
                if (UserManager.isSplitSystemUser() && KeyguardUpdateMonitor.getCurrentUser() == 0) {
                    KeyguardViewMediator.this.doKeyguardLocked(null);
                }
            }
        }

        public void onSimStateChanged(int subId, int slotId, State simState) {
            Log.d("KeyguardViewMediator", "onSimStateChanged(subId=" + subId + ", slotId=" + slotId + ",state=" + simState + ")");
            int size = KeyguardViewMediator.this.mKeyguardStateCallbacks.size();
            boolean simPinSecure = KeyguardViewMediator.this.mUpdateMonitor.isSimPinSecure();
            for (int i = size - 1; i >= 0; i--) {
                try {
                    ((IKeyguardStateCallback) KeyguardViewMediator.this.mKeyguardStateCallbacks.get(i)).onSimSecureStateChanged(simPinSecure);
                } catch (RemoteException e) {
                    Slog.w("KeyguardViewMediator", "Failed to call onSimSecureStateChanged", e);
                    if (e instanceof DeadObjectException) {
                        KeyguardViewMediator.this.mKeyguardStateCallbacks.remove(i);
                    }
                }
            }
            switch (AnonymousClass1.-getcom-android-internal-telephony-IccCardConstants$StateSwitchesValues()[simState.ordinal()]) {
                case 1:
                case 2:
                    synchronized (this) {
                        if (KeyguardViewMediator.this.shouldWaitForProvisioning()) {
                            if (KeyguardViewMediator.this.mShowing) {
                                KeyguardViewMediator.this.resetStateLocked();
                            } else {
                                Log.d("KeyguardViewMediator", "ICC_ABSENT isn't showing, we need to show the keyguard since the device isn't provisioned yet.");
                                KeyguardViewMediator.this.doKeyguardLocked(null);
                            }
                        }
                        KeyguardAbsKeyInputView.setCancelCount(0);
                        KeyguardViewMediator.this.mUpdateMonitor.resetCancelSubscriptId();
                        break;
                    }
                case 3:
                    synchronized (this) {
                        if (!KeyguardViewMediator.this.mShowing) {
                            Log.d("KeyguardViewMediator", "PERM_DISABLED and keygaurd isn't showing.");
                            KeyguardViewMediator.this.doKeyguardLocked(null);
                            break;
                        }
                        Log.d("KeyguardViewMediator", "PERM_DISABLED, resetStateLocked toshow permanently disabled message in lockscreen.");
                        KeyguardViewMediator.this.resetToBouncerStateLocked();
                        break;
                    }
                case 4:
                case 5:
                    synchronized (this) {
                        boolean isHiding = KeyguardViewMediator.this.mHiding;
                        if (!KeyguardViewMediator.this.mHiding) {
                            isHiding = !KeyguardViewMediator.this.mHandler.hasMessages(9) ? KeyguardViewMediator.this.mHandler.hasMessages(18) : true;
                        }
                        Log.i("KeyguardViewMediator", " is keyguard hiding? isHiding = " + isHiding + " mHiding = " + KeyguardViewMediator.this.mHiding);
                        if (isHiding) {
                            KeyguardViewMediator.this.mHiding = false;
                            KeyguardViewMediator.this.mHandler.removeMessages(9);
                            KeyguardViewMediator.this.mHandler.removeMessages(18);
                        }
                        if (KeyguardViewMediator.this.mShowing && !isHiding) {
                            KeyguardViewMediator.this.resetStateLocked();
                            break;
                        }
                        Log.d("KeyguardViewMediator", "INTENT_VALUE_ICC_LOCKED and keygaurd isn't showing; need to show keyguard so user can enter sim pin");
                        KeyguardViewMediator.this.doKeyguardLocked(null);
                        break;
                    }
                case 6:
                    synchronized (this) {
                        if (KeyguardViewMediator.this.mShowing) {
                            KeyguardViewMediator.this.resetToBouncerStateLocked();
                            break;
                        }
                    }
                default:
                    Log.v("KeyguardViewMediator", "Ignoring state: " + simState);
                    return;
            }
        }

        public void onFingerprintAuthFailed() {
            int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            if (KeyguardViewMediator.this.mLockPatternUtils.isSecure(currentUser)) {
                KeyguardViewMediator.this.mLockPatternUtils.getDevicePolicyManager().reportFailedFingerprintAttempt(currentUser);
            }
        }
    };
    private Runnable mUpdateLockScreenStateRunner = new Runnable() {
        public void run() {
            try {
                ActivityManagerNative.getDefault().setLockScreenShown(KeyguardViewMediator.this.mShowing, KeyguardViewMediator.this.mOccluded);
            } catch (RemoteException e) {
            }
        }
    };
    protected KeyguardUpdateMonitor mUpdateMonitor;
    ViewMediatorCallback mViewMediatorCallback = new ViewMediatorCallback() {
        public void userActivity() {
            KeyguardViewMediator.this.userActivity();
        }

        public void keyguardDone(boolean strongAuth) {
            if (!KeyguardViewMediator.this.mKeyguardDonePending) {
                KeyguardViewMediator.this.keyguardDone(true);
            }
            if (strongAuth) {
                KeyguardViewMediator.this.mUpdateMonitor.reportSuccessfulStrongAuthUnlockAttempt();
            }
        }

        public void keyguardDoneDrawing() {
            KeyguardViewMediator.this.mHandler.sendEmptyMessage(10);
        }

        public void setNeedsInput(boolean needsInput) {
            KeyguardViewMediator.this.mStatusBarKeyguardViewManager.setNeedsInput(needsInput);
        }

        public void keyguardDonePending(boolean strongAuth) {
            KeyguardViewMediator.this.mKeyguardDonePending = true;
            KeyguardViewMediator.this.mHideAnimationRun = true;
            KeyguardViewMediator.this.mStatusBarKeyguardViewManager.startPreHideAnimation(null);
            KeyguardViewMediator.this.mHandler.sendEmptyMessageDelayed(20, 3000);
            if (strongAuth) {
                KeyguardViewMediator.this.mUpdateMonitor.reportSuccessfulStrongAuthUnlockAttempt();
            }
        }

        public void keyguardGone() {
            KeyguardViewMediator.this.mKeyguardDisplayManager.hide();
        }

        public void readyForKeyguardDone() {
            if (KeyguardViewMediator.this.mKeyguardDonePending) {
                KeyguardViewMediator.this.keyguardDone(true);
            }
        }

        public void resetKeyguard() {
            KeyguardViewMediator.this.resetStateLocked();
        }

        public void playTrustedSound() {
            KeyguardViewMediator.this.playTrustedSound();
        }

        public boolean isInputRestricted() {
            return KeyguardViewMediator.this.isInputRestricted();
        }

        public boolean isScreenOn() {
            return KeyguardViewMediator.this.mDeviceInteractive;
        }

        public int getBouncerPromptReason() {
            int currentUser = UserSwitchUtils.getCurrentUser();
            boolean trust = KeyguardViewMediator.this.mTrustManager.isTrustUsuallyManaged(currentUser);
            boolean fingerprint = KeyguardViewMediator.this.mUpdateMonitor.isUnlockWithFingerprintPossible(currentUser);
            boolean z = !trust ? fingerprint : true;
            StrongAuthTracker strongAuthTracker = KeyguardViewMediator.this.mUpdateMonitor.getStrongAuthTracker();
            int strongAuth = strongAuthTracker.getStrongAuthForUser(currentUser);
            if (!z || strongAuthTracker.hasUserAuthenticatedSinceBoot()) {
                if (fingerprint && KeyguardViewMediator.this.mUpdateMonitor.hasFingerprintUnlockTimedOut(currentUser)) {
                    return 10;
                }
                if (z && (strongAuth & 2) != 0) {
                    return 3;
                }
                if (trust && (strongAuth & 4) != 0) {
                    return 4;
                }
                if (z && (strongAuth & 8) != 0) {
                    return 5;
                }
                if (!trust || (strongAuth & 16) == 0) {
                    return 0;
                }
                return 6;
            } else if (RetryPolicy.getDefaultPolicy(KeyguardViewMediator.this.mContext).getRemainingTime() > 0) {
                return 11;
            } else {
                if (KeyguardViewMediator.this.mUpdateMonitor.isChangeTipsForAbnormalReboot()) {
                    return 2;
                }
                return 1;
            }
        }
    };
    private boolean mWaitingUntilKeyguardVisible = false;
    private boolean mWakeAndUnlocking;

    private static class StartKeyguardExitAnimParams {
        long fadeoutDuration;
        long startTime;

        private StartKeyguardExitAnimParams(long startTime, long fadeoutDuration) {
            this.startTime = startTime;
            this.fadeoutDuration = fadeoutDuration;
        }
    }

    public abstract void dealWithUseFingerAfterCoverOpen();

    public abstract void dealWithkeyguardView();

    public abstract void handleDelayScreenLockStatusCheck();

    protected abstract boolean isInCall();

    public abstract boolean isInfastScreenMode();

    public abstract boolean isShowingAndOccluded();

    public abstract void notifyWakeupDevice();

    public abstract void onServiceDestroyed();

    public void userActivity() {
        GlobalContext.getBackgroundHandler().post(new Runnable() {
            public void run() {
                KeyguardViewMediator.this.mPM.userActivity(SystemClock.uptimeMillis(), false);
            }
        });
    }

    protected void setupLocked() {
        boolean z;
        this.mPM = (PowerManager) this.mContext.getSystemService("power");
        this.mTrustManager = (TrustManager) this.mContext.getSystemService("trust");
        this.mShowKeyguardWakeLock = this.mPM.newWakeLock(1, "show keyguard");
        this.mShowKeyguardWakeLock.setReferenceCounted(false);
        this.mContext.registerReceiver(this.mBroadcastReceiver, new IntentFilter("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD"));
        this.mContext.registerReceiver(this.mBroadcastReceiver, new IntentFilter("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_LOCK"));
        this.mKeyguardDisplayManager = new KeyguardDisplayManager(this.mContext);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        KeyguardUpdateMonitor.setCurrentUser(UserSwitchUtils.getCurrentUser());
        if (shouldWaitForProvisioning() || this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser())) {
            z = false;
        } else {
            z = true;
        }
        setShowingLocked(z);
        updateInputRestrictedLocked();
        this.mTrustManager.reportKeyguardShowingChanged();
        this.mStatusBarKeyguardViewManager = (HwStatusBarKeyguardViewManager) SystemUIFactory.getInstance().createStatusBarKeyguardViewManager(this.mContext, this.mViewMediatorCallback, this.mLockPatternUtils);
        ContentResolver cr = this.mContext.getContentResolver();
        this.mDeviceInteractive = this.mPM.isInteractive();
        this.mLockSounds = new SoundPool(1, 1, 0);
        String soundPath = Global.getString(cr, "lock_sound");
        if (soundPath != null) {
            this.mLockSoundId = this.mLockSounds.load(soundPath, 1);
        }
        if (soundPath == null || this.mLockSoundId == 0) {
            Log.w("KeyguardViewMediator", "failed to load lock sound from " + soundPath);
        }
        soundPath = Global.getString(cr, "unlock_sound");
        if (soundPath != null) {
            this.mUnlockSoundId = this.mLockSounds.load(soundPath, 1);
        }
        if (soundPath == null || this.mUnlockSoundId == 0) {
            Log.w("KeyguardViewMediator", "failed to load unlock sound from " + soundPath);
        }
        soundPath = Global.getString(cr, "trusted_sound");
        if (soundPath != null) {
            this.mTrustedSoundId = this.mLockSounds.load(soundPath, 1);
        }
        if (soundPath == null || this.mTrustedSoundId == 0) {
            Log.w("KeyguardViewMediator", "failed to load trusted sound from " + soundPath);
        }
        this.mLockSoundVolume = (float) Math.pow(10.0d, (double) (((float) this.mContext.getResources().getInteger(17694725)) / 20.0f));
        this.mHideAnimation = AnimationUtils.loadAnimation(this.mContext, 17432659);
    }

    public void start() {
        synchronized (this) {
            setupLocked();
        }
        putComponent(HwKeyguardViewMediator.class, (HwKeyguardViewMediator) this);
    }

    public void onSystemReady() {
        this.mSearchManager = (SearchManager) this.mContext.getSystemService("search");
        synchronized (this) {
            Log.d("KeyguardViewMediator", "onSystemReady");
            this.mSystemReady = true;
            doKeyguardLocked(null);
            this.mUpdateMonitor.registerCallback(this.mUpdateCallback);
        }
        this.mIsPerUserLock = StorageManager.isFileEncryptedNativeOrEmulated();
        maybeSendUserPresentBroadcast();
    }

    public void onStartedGoingToSleep(int why) {
        synchronized (this) {
            this.mDeviceInteractive = false;
            this.mGoingToSleep = true;
            int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            boolean lockImmediately = !this.mLockPatternUtils.getPowerButtonInstantlyLocks(currentUser) ? !this.mLockPatternUtils.isSecure(currentUser) : true;
            long timeout = getLockTimeout(currentUser);
            this.mLockLater = false;
            if (this.mExitSecureCallback != null) {
                Log.d("KeyguardViewMediator", "pending exit secure callback cancelled");
                try {
                    this.mExitSecureCallback.onKeyguardExitResult(false);
                } catch (RemoteException e) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e);
                }
                this.mExitSecureCallback = null;
                if (!this.mExternallyEnabled) {
                    hideLocked();
                }
            } else if (this.mShowing) {
                this.mPendingReset = true;
            } else if ((why == 3 && timeout > 0) || (why == 2 && !lockImmediately)) {
                doKeyguardLaterLocked(timeout);
                this.mLockLater = true;
            } else if (why == 6 || why == 7) {
                Log.w("KeyguardViewMediator", "onStartedGoingToSleep because of prox sensor." + why);
            } else if (!this.mLockPatternUtils.isLockScreenDisabled(currentUser)) {
                this.mPendingLock = doPendingLock();
            }
            if (this.mPendingLock) {
                playSounds(true);
            }
        }
        Log.i("KeyguardViewMediator", "onStartedGoingToSleep(" + why + ") Lock : " + this.mPendingLock + " reset: " + this.mPendingReset);
        KeyguardUpdateMonitor.getInstance(this.mContext).dispatchStartedGoingToSleep(why);
        notifyStartedGoingToSleep();
    }

    private boolean doPendingLock() {
        if (CoverViewManager.getInstance(this.mContext).isCoverOpen() || CoverViewManager.isWindowedCover(this.mContext) || HwKeyguardUpdateMonitor.getInstance(this.mContext).isSecure()) {
            return true;
        }
        return false;
    }

    public void onFinishedGoingToSleep(int why, boolean cameraGestureTriggered) {
        Log.d("KeyguardViewMediator", "onFinishedGoingToSleep(" + why + ")");
        synchronized (this) {
            this.mDeviceInteractive = false;
            if (this.mGoingToSleep) {
                this.mGoingToSleep = false;
            } else if (this.mStatusBarKeyguardViewManager.isShowing() && this.mStatusBarKeyguardViewManager.isBouncerShowing() && isInCall()) {
                HwLog.w("KeyguardViewMediator", "reset keyguard view when bouncer showing and inCall");
                this.mPendingReset = true;
            }
            resetKeyguardDonePendingLocked();
            this.mHideAnimationRun = false;
            notifyFinishedGoingToSleep();
            if (cameraGestureTriggered) {
                Log.i("KeyguardViewMediator", "Camera gesture was triggered, preventing Keyguard locking.");
                ((PowerManager) this.mContext.getSystemService(PowerManager.class)).wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:CAMERA_GESTURE_PREVENT_LOCK");
                this.mPendingLock = false;
                this.mPendingReset = false;
            }
            if (this.mPendingReset) {
                resetStateLocked();
                this.mPendingReset = false;
            }
            if (this.mPendingLock) {
                doKeyguardLocked(null);
                this.mPendingLock = false;
            }
            if (!(this.mLockLater || cameraGestureTriggered)) {
                doKeyguardForChildProfilesLocked();
            }
        }
        KeyguardUpdateMonitor.getInstance(this.mContext).dispatchFinishedGoingToSleep(why);
    }

    private long getLockTimeout(int userId) {
        ContentResolver cr = this.mContext.getContentResolver();
        long lockAfterTimeout = (long) Secure.getInt(cr, "lock_screen_lock_after_timeout", 5000);
        long policyTimeout = this.mLockPatternUtils.getDevicePolicyManager().getMaximumTimeToLockForUserAndProfiles(userId);
        if (policyTimeout <= 0) {
            return lockAfterTimeout;
        }
        return Math.max(Math.min(policyTimeout - Math.max((long) System.getInt(cr, "screen_off_timeout", 30000), 0), lockAfterTimeout), 0);
    }

    private void doKeyguardLaterLocked() {
        long timeout = getLockTimeout(KeyguardUpdateMonitor.getCurrentUser());
        if (timeout == 0) {
            doKeyguardLocked(null);
        } else {
            doKeyguardLaterLocked(timeout);
        }
    }

    private void doKeyguardLaterLocked(long timeout) {
        long when = SystemClock.elapsedRealtime() + timeout;
        Intent intent = new Intent("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD");
        intent.putExtra("seq", this.mDelayedShowingSequence);
        intent.addFlags(268435456);
        this.mAlarmManager.setExactAndAllowWhileIdle(2, when, PendingIntent.getBroadcast(this.mContext, 0, intent, 268435456));
        Log.d("KeyguardViewMediator", "setting alarm to turn off keyguard, seq = " + this.mDelayedShowingSequence);
        doKeyguardLaterForChildProfilesLocked();
    }

    private void doKeyguardLaterForChildProfilesLocked() {
        for (int profileId : UserManager.get(this.mContext).getEnabledProfileIds(UserHandle.myUserId())) {
            if (this.mLockPatternUtils.isSeparateProfileChallengeEnabled(profileId)) {
                long userTimeout = getLockTimeout(profileId);
                if (userTimeout == 0) {
                    doKeyguardForChildProfilesLocked();
                } else {
                    long userWhen = SystemClock.elapsedRealtime() + userTimeout;
                    Intent lockIntent = new Intent("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_LOCK");
                    lockIntent.putExtra("seq", this.mDelayedProfileShowingSequence);
                    lockIntent.putExtra("android.intent.extra.USER_ID", profileId);
                    lockIntent.addFlags(268435456);
                    this.mAlarmManager.setExactAndAllowWhileIdle(2, userWhen, PendingIntent.getBroadcast(this.mContext, 0, lockIntent, 268435456));
                }
            }
        }
    }

    private void doKeyguardForChildProfilesLocked() {
        for (int profileId : UserManager.get(this.mContext).getEnabledProfileIds(UserHandle.myUserId())) {
            if (this.mLockPatternUtils.isSeparateProfileChallengeEnabled(profileId)) {
                lockProfile(profileId);
            }
        }
    }

    private void cancelDoKeyguardLaterLocked() {
        this.mDelayedShowingSequence++;
    }

    private void cancelDoKeyguardForChildProfilesLocked() {
        this.mDelayedProfileShowingSequence++;
    }

    public void onStartedWakingUp() {
        synchronized (this) {
            this.mDeviceInteractive = true;
            cancelDoKeyguardLaterLocked();
            cancelDoKeyguardForChildProfilesLocked();
            Log.d("KeyguardViewMediator", "onStartedWakingUp, seq = " + this.mDelayedShowingSequence);
            notifyStartedWakingUp();
        }
        KeyguardUpdateMonitor.getInstance(this.mContext).dispatchStartedWakingUp();
        maybeSendUserPresentBroadcast();
    }

    public void onScreenTurningOn(IKeyguardDrawnCallback callback) {
        notifyScreenOn(callback);
    }

    public void onScreenTurnedOn() {
        notifyScreenTurnedOn();
        this.mUpdateMonitor.dispatchScreenTurnedOn();
    }

    public void onScreenTurnedOff() {
        notifyScreenTurnedOff();
        this.mUpdateMonitor.dispatchScreenTurnedOff();
    }

    private void maybeSendUserPresentBroadcast() {
        if (this.mSystemReady && this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser())) {
            sendUserPresentBroadcast();
        } else if (this.mSystemReady && shouldWaitForProvisioning()) {
            getLockPatternUtils().userPresent(KeyguardUpdateMonitor.getCurrentUser());
        }
    }

    public void onDreamingStarted() {
        synchronized (this) {
            if (this.mDeviceInteractive && this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser())) {
                doKeyguardLaterLocked();
            }
        }
    }

    public void onDreamingStopped() {
        synchronized (this) {
            if (this.mDeviceInteractive) {
                cancelDoKeyguardLaterLocked();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setKeyguardEnabled(boolean enabled) {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "setKeyguardEnabled(" + enabled + ")");
            this.mExternallyEnabled = enabled;
            if (enabled || !this.mShowing) {
                if (enabled) {
                    if (this.mNeedToReshowWhenReenabled) {
                        Log.d("KeyguardViewMediator", "previously hidden, reshowing, reenabling status bar expansion");
                        this.mNeedToReshowWhenReenabled = false;
                        updateInputRestrictedLocked();
                        if (this.mExitSecureCallback != null) {
                            Log.d("KeyguardViewMediator", "onKeyguardExitResult(false), resetting");
                            try {
                                this.mExitSecureCallback.onKeyguardExitResult(false);
                            } catch (RemoteException e) {
                                Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e);
                            }
                            this.mExitSecureCallback = null;
                            resetStateLocked();
                        } else {
                            showLocked(null);
                            this.mWaitingUntilKeyguardVisible = true;
                            this.mHandler.sendEmptyMessageDelayed(10, 2000);
                            Log.d("KeyguardViewMediator", "waiting until mWaitingUntilKeyguardVisible is false");
                            while (this.mWaitingUntilKeyguardVisible) {
                                try {
                                    wait();
                                } catch (InterruptedException e2) {
                                    Thread.currentThread().interrupt();
                                }
                            }
                            Log.d("KeyguardViewMediator", "done waiting for mWaitingUntilKeyguardVisible");
                        }
                    }
                }
            } else if (this.mExitSecureCallback != null) {
                Log.d("KeyguardViewMediator", "in process of verifyUnlock request, ignoring");
            } else {
                Log.d("KeyguardViewMediator", "remembering to reshow, hiding keyguard, disabling status bar expansion");
                this.mNeedToReshowWhenReenabled = true;
                updateInputRestrictedLocked();
                hideLocked();
            }
        }
    }

    public void verifyUnlock(IKeyguardExitCallback callback) {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "verifyUnlock");
            if (shouldWaitForProvisioning()) {
                Log.d("KeyguardViewMediator", "ignoring because device isn't provisioned");
                try {
                    callback.onKeyguardExitResult(false);
                } catch (RemoteException e) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e);
                }
            } else if (this.mExternallyEnabled) {
                Log.w("KeyguardViewMediator", "verifyUnlock called when not externally disabled");
                try {
                    callback.onKeyguardExitResult(false);
                } catch (RemoteException e2) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e2);
                }
            } else if (this.mExitSecureCallback != null) {
                try {
                    callback.onKeyguardExitResult(false);
                } catch (RemoteException e22) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e22);
                }
            } else if (isSecure()) {
                try {
                    callback.onKeyguardExitResult(false);
                } catch (RemoteException e222) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e222);
                }
            } else {
                this.mExternallyEnabled = true;
                this.mNeedToReshowWhenReenabled = false;
                updateInputRestricted();
                try {
                    callback.onKeyguardExitResult(true);
                } catch (RemoteException e2222) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e2222);
                }
            }
        }
    }

    public boolean isShowingAndNotOccluded() {
        return this.mShowing && !this.mOccluded;
    }

    public void setOccluded(boolean isOccluded) {
        int i;
        Log.d("KeyguardViewMediator", "setOccluded " + isOccluded);
        this.mHandler.removeMessages(12);
        Handler handler = this.mHandler;
        if (isOccluded) {
            i = 1;
        } else {
            i = 0;
        }
        this.mHandler.sendMessage(handler.obtainMessage(12, i, 0));
    }

    protected void handleSetOccluded(boolean isOccluded) {
        synchronized (this) {
            if (this.mHiding && isOccluded) {
                startKeyguardExitAnimation(0, 0);
            }
            if (this.mOccluded != isOccluded) {
                this.mOccluded = isOccluded;
                this.mStatusBarKeyguardViewManager.setOccluded(isOccluded);
                updateActivityLockScreenState();
                adjustStatusBarLocked();
            }
        }
    }

    public void doKeyguardTimeout(Bundle options) {
        this.mHandler.removeMessages(13);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(13, options));
    }

    public boolean isInputRestricted() {
        return !this.mShowing ? this.mNeedToReshowWhenReenabled : true;
    }

    private void updateInputRestricted() {
        synchronized (this) {
            updateInputRestrictedLocked();
        }
    }

    private void updateInputRestrictedLocked() {
        boolean inputRestricted = isInputRestricted();
        if (this.mInputRestricted != inputRestricted) {
            this.mInputRestricted = inputRestricted;
            for (int i = this.mKeyguardStateCallbacks.size() - 1; i >= 0; i--) {
                try {
                    ((IKeyguardStateCallback) this.mKeyguardStateCallbacks.get(i)).onInputRestrictedStateChanged(inputRestricted);
                } catch (RemoteException e) {
                    Slog.w("KeyguardViewMediator", "Failed to call onDeviceProvisioned", e);
                    if (e instanceof DeadObjectException) {
                        this.mKeyguardStateCallbacks.remove(i);
                    }
                }
            }
        }
    }

    public void doKeyguardLocked(Bundle options) {
        if (!this.mExternallyEnabled) {
            Log.d("KeyguardViewMediator", "doKeyguard: not showing because externally disabled");
        } else if (this.mStatusBarKeyguardViewManager.isShowing()) {
            Log.d("KeyguardViewMediator", "doKeyguard: not showing because it is already showing");
            resetStateLocked();
        } else {
            if (!(UserManager.isSplitSystemUser() && KeyguardUpdateMonitor.getCurrentUser() == 0 && this.mUpdateMonitor.isDeviceProvisioned())) {
                boolean lockedOrMissing = !this.mUpdateMonitor.isSimPinSecure() ? (SubscriptionManager.isValidSubscriptionId(this.mUpdateMonitor.getNextSubIdForState(State.ABSENT)) || SubscriptionManager.isValidSubscriptionId(this.mUpdateMonitor.getNextSubIdForState(State.PERM_DISABLED))) ? !SystemProperties.getBoolean("keyguard.no_require_sim", false) : false : true;
                if (!lockedOrMissing && shouldWaitForProvisioning()) {
                    Log.d("KeyguardViewMediator", "doKeyguard: not showing because device isn't provisioned and the sim is not locked or missing");
                    return;
                } else if (this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser()) && !lockedOrMissing) {
                    Log.d("KeyguardViewMediator", "doKeyguard: not showing because lockscreen is off");
                    return;
                } else if (this.mLockPatternUtils.checkVoldPassword(KeyguardUpdateMonitor.getCurrentUser())) {
                    Log.d("KeyguardViewMediator", "Not showing lock screen since just decrypted");
                    setShowingLocked(false);
                    hideLocked();
                    this.mUpdateMonitor.reportSuccessfulStrongAuthUnlockAttempt();
                    return;
                }
            }
            if (mIsFirstLocked || CoverViewManager.getInstance(this.mContext).isCoverOpen() || isSecure()) {
                if (mIsFirstLocked) {
                    CalibrateUtil.sendCalibrate(this.mContext);
                }
                Log.d("KeyguardViewMediator", "doKeyguard: showing the lock screen");
                showLocked(options);
                setFirstLocked();
            }
        }
    }

    private static void setFirstLocked() {
        mIsFirstLocked = false;
    }

    private void lockProfile(int userId) {
        this.mTrustManager.setDeviceLockedForUser(userId, true);
    }

    private boolean shouldWaitForProvisioning() {
        return (this.mUpdateMonitor.isDeviceProvisioned() || isSecure()) ? false : true;
    }

    public void handleDismiss() {
        if (this.mShowing && !this.mOccluded) {
            this.mStatusBarKeyguardViewManager.dismiss();
        }
    }

    public void dismiss() {
        Log.w("KeyguardViewMediator", "Keyguard do dismiss!!!");
        this.mHandler.sendEmptyMessage(17);
    }

    protected void resetToBouncerStateLocked() {
        Log.w("KeyguardViewMediator", "resetStateLocked");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(100));
    }

    private void resetStateLocked() {
        Log.w("KeyguardViewMediator", "resetStateLocked");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(4));
    }

    private void notifyStartedGoingToSleep() {
        Log.d("KeyguardViewMediator", "notifyStartedGoingToSleep");
        this.mHandler.sendEmptyMessage(24);
    }

    private void notifyFinishedGoingToSleep() {
        Log.d("KeyguardViewMediator", "notifyFinishedGoingToSleep");
        this.mHandler.sendEmptyMessage(6);
    }

    private void notifyStartedWakingUp() {
        Log.d("KeyguardViewMediator", "notifyStartedWakingUp");
        this.mHandler.sendEmptyMessage(21);
    }

    private void notifyScreenOn(IKeyguardDrawnCallback callback) {
        Log.d("KeyguardViewMediator", "notifyScreenOn");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(7, callback));
    }

    private void notifyScreenTurnedOn() {
        Log.d("KeyguardViewMediator", "notifyScreenTurnedOn");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(22));
    }

    private void notifyScreenTurnedOff() {
        Log.d("KeyguardViewMediator", "notifyScreenTurnedOff");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(23));
    }

    protected void showLocked(Bundle options) {
        Log.d("KeyguardViewMediator", "showLocked");
        this.mShowKeyguardWakeLock.acquire();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2, options));
    }

    protected void hideLocked() {
        Log.d("KeyguardViewMediator", "hideLocked");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(3));
    }

    public boolean isSecure() {
        if (this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser())) {
            return true;
        }
        return KeyguardUpdateMonitor.getInstance(this.mContext).isSimPinSecure();
    }

    public void setCurrentUser(int newUserId) {
        KeyguardUpdateMonitor.setCurrentUser(newUserId);
    }

    public void keyguardDone(boolean authenticated) {
        Log.d("KeyguardViewMediator", "keyguardDone(" + authenticated + ")");
        if (this.mHandler.hasMessages(9)) {
            HwLog.w("KeyguardViewMediator", "mHandler.hasMessages(KEYGUARD_DONE)  return");
            return;
        }
        EventLog.writeEvent(70000, 2);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(9, Integer.valueOf(authenticated ? 1 : 0)));
    }

    protected void handleKeyguardDone(boolean authenticated) {
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        if (this.mLockPatternUtils.isSecure(currentUser)) {
            this.mLockPatternUtils.getDevicePolicyManager().reportKeyguardDismissed(currentUser);
        }
        Log.d("KeyguardViewMediator", "handleKeyguardDone");
        synchronized (this) {
            resetKeyguardDonePendingLocked();
        }
        if (authenticated) {
            this.mUpdateMonitor.clearFailedUnlockAttempts();
        }
        this.mUpdateMonitor.clearFingerprintRecognized();
        if (this.mGoingToSleep) {
            Log.i("KeyguardViewMediator", "Device is going to sleep, aborting keyguardDone");
            return;
        }
        if (this.mExitSecureCallback != null) {
            try {
                this.mExitSecureCallback.onKeyguardExitResult(authenticated);
            } catch (RemoteException e) {
                Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(" + authenticated + ")", e);
            }
            this.mExitSecureCallback = null;
            if (authenticated) {
                this.mExternallyEnabled = true;
                this.mNeedToReshowWhenReenabled = false;
                updateInputRestricted();
            }
        }
        handleHide();
    }

    protected void sendUserPresentBroadcast() {
        synchronized (this) {
            if (this.mBootCompleted) {
                int currentUserId = KeyguardUpdateMonitor.getCurrentUser();
                for (int profileId : ((UserManager) this.mContext.getSystemService("user")).getProfileIdsWithDisabled(new UserHandle(currentUserId).getIdentifier())) {
                    this.mContext.sendBroadcastAsUser(USER_PRESENT_INTENT, UserHandle.of(profileId));
                }
                getLockPatternUtils().userPresent(currentUserId);
                AppHandler.sendMessage(16);
            } else {
                this.mBootSendUserPresent = true;
            }
        }
    }

    private void handleKeyguardDoneDrawing() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleKeyguardDoneDrawing");
            if (this.mWaitingUntilKeyguardVisible) {
                Log.d("KeyguardViewMediator", "handleKeyguardDoneDrawing: notifying mWaitingUntilKeyguardVisible");
                this.mWaitingUntilKeyguardVisible = false;
                notifyAll();
                this.mHandler.removeMessages(10);
            }
        }
    }

    private void playSounds(boolean locked) {
        playSound(locked ? this.mLockSoundId : this.mUnlockSoundId);
    }

    private void playSound(final int soundId) {
        if (soundId != 0) {
            GlobalContext.getBackgroundHandler().post(new Runnable() {
                public void run() {
                    if (System.getInt(KeyguardViewMediator.this.mContext.getContentResolver(), "lockscreen_sounds_enabled", 1) == 1) {
                        KeyguardViewMediator.this.mLockSounds.stop(KeyguardViewMediator.this.mLockSoundStreamId);
                        if (KeyguardViewMediator.this.mAudioManager == null) {
                            KeyguardViewMediator.this.mAudioManager = (AudioManager) KeyguardViewMediator.this.mContext.getSystemService("audio");
                            if (KeyguardViewMediator.this.mAudioManager != null) {
                                KeyguardViewMediator.this.mUiSoundsStreamType = KeyguardViewMediator.this.mAudioManager.getUiSoundsStreamType();
                            } else {
                                return;
                            }
                        }
                        if (!KeyguardViewMediator.this.mAudioManager.isStreamMute(KeyguardViewMediator.this.mUiSoundsStreamType)) {
                            KeyguardViewMediator.this.mLockSoundStreamId = KeyguardViewMediator.this.mLockSounds.play(soundId, KeyguardViewMediator.this.mLockSoundVolume, KeyguardViewMediator.this.mLockSoundVolume, 1, 0, 1.0f);
                        }
                    }
                }
            });
        }
    }

    private void playTrustedSound() {
        playSound(this.mTrustedSoundId);
    }

    private void updateActivityLockScreenState() {
        GlobalContext.getBackgroundHandler().post(this.mUpdateLockScreenStateRunner);
    }

    protected void handleShow(Bundle options) {
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        if (this.mLockPatternUtils.isSecure(currentUser)) {
            this.mLockPatternUtils.getDevicePolicyManager().reportKeyguardSecured(currentUser);
        }
        synchronized (this) {
            if (this.mSystemReady) {
                Log.d("KeyguardViewMediator", "handleShow");
                setShowingLocked(true);
                this.mStatusBarKeyguardViewManager.show(options);
                this.mHiding = false;
                this.mWakeAndUnlocking = false;
                resetKeyguardDonePendingLocked();
                this.mHideAnimationRun = false;
                updateActivityLockScreenState();
                adjustStatusBarLocked();
                userActivity();
                this.mShowKeyguardWakeLock.release();
                this.mKeyguardDisplayManager.show();
                HiddenSpace.getInstance().updateHiddenSpaceData(this.mContext);
                return;
            }
            Log.d("KeyguardViewMediator", "ignoring handleShow because system is not ready.");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void handleHide() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleHide");
            if (UserManager.isSplitSystemUser() && KeyguardUpdateMonitor.getCurrentUser() == 0) {
                Log.d("KeyguardViewMediator", "Split system user, quit unlocking.");
                return;
            }
            this.mHiding = true;
            if (KeyguardTheme.getInst().getLockStyle() == 7) {
                MusicInfo.getInst().resetShowingState();
            }
            if (isInfastScreenMode()) {
                this.mStatusBarKeyguardViewManager.keyguardGoingAway();
                handleStartKeyguardExitAnimation(SystemClock.uptimeMillis() + this.mHideAnimation.getStartOffset(), 0);
            } else if (!this.mShowing || this.mOccluded) {
                handleStartKeyguardExitAnimation(SystemClock.uptimeMillis() + this.mHideAnimation.getStartOffset(), this.mHideAnimation.getDuration());
            } else {
                if (this.mHideAnimationRun) {
                    this.mKeyguardGoingAwayRunnable.run();
                } else {
                    this.mStatusBarKeyguardViewManager.startPreHideAnimation(this.mKeyguardGoingAwayRunnable);
                }
                dealWithUseFingerAfterCoverOpen();
            }
        }
    }

    private void handleOnActivityDrawn() {
        Log.d("KeyguardViewMediator", "handleOnActivityDrawn: mKeyguardDonePending=" + this.mKeyguardDonePending);
        if (this.mKeyguardDonePending) {
            this.mStatusBarKeyguardViewManager.onActivityDrawn();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void handleStartKeyguardExitAnimation(long startTime, long fadeoutDuration) {
        synchronized (this) {
            this.mKeyguardExitAnimateStart = true;
            if (this.mHiding) {
                this.mHiding = false;
                PerfDebugUtils.beginSystraceSection("handleStartKeyguardExitAnimation_hide");
                if (this.mWakeAndUnlocking && this.mDrawnCallback != null) {
                    this.mStatusBarKeyguardViewManager.getViewRootImpl().setReportNextDraw();
                    notifyDrawn(this.mDrawnCallback);
                }
                this.mStatusBarKeyguardViewManager.hide(startTime, fadeoutDuration);
                PerfDebugUtils.endSystraceSection();
                setShowingLocked(false);
                resetKeyguardDonePendingLocked();
                this.mHideAnimationRun = false;
                updateActivityLockScreenState();
                if (isInfastScreenMode()) {
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1001), FAST_FP_UNLOCK_WAKUP_DELAY);
                } else {
                    handleExitAfterHide();
                }
            } else {
                HwLog.w("KeyguardViewMediator", "skip KeyguardExitAnimation as Hiding");
            }
        }
    }

    private void adjustStatusBarLocked() {
        if (this.mStatusBarManager == null) {
            this.mStatusBarManager = (StatusBarManager) this.mContext.getSystemService("statusbar");
        }
        if (this.mStatusBarManager == null) {
            Log.w("KeyguardViewMediator", "Could not get status bar manager");
            return;
        }
        int flags = 0;
        if (this.mShowing) {
            flags = 16777216 | 33554432;
        }
        if (isShowingAndNotOccluded()) {
            flags |= 2097152;
            if (IS_EMUI_LITE) {
                flags |= 4194304;
            }
        } else if (isShowingAndOccluded()) {
            flags |= 65536;
        }
        Log.d("KeyguardViewMediator", "adjustStatusBarLocked: mShowing=" + this.mShowing + " mOccluded=" + this.mOccluded + " isSecure=" + isSecure() + " --> flags=0x" + Integer.toHexString(flags));
        if (!(this.mContext instanceof Activity)) {
            this.mStatusBarManager.disable(flags);
        }
    }

    protected void handleResetToBouncerStateLocked() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleResetToBouncerStateLocked");
            this.mStatusBarKeyguardViewManager.resetOnlyToBouncer();
        }
    }

    protected void handleReset() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleReset");
            this.mStatusBarKeyguardViewManager.reset();
        }
    }

    private void handleVerifyUnlock() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleVerifyUnlock");
            setShowingLocked(true);
            this.mStatusBarKeyguardViewManager.verifyUnlock();
            updateActivityLockScreenState();
        }
    }

    private void handleNotifyStartedGoingToSleep() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyStartedGoingToSleep");
            this.mStatusBarKeyguardViewManager.onStartedGoingToSleep();
        }
    }

    private void handleNotifyFinishedGoingToSleep() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyFinishedGoingToSleep");
            this.mStatusBarKeyguardViewManager.onFinishedGoingToSleep();
        }
    }

    private void handleNotifyStartedWakingUp() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyWakingUp");
            this.mStatusBarKeyguardViewManager.onStartedWakingUp();
        }
    }

    private void handleNotifyScreenTurningOn(IKeyguardDrawnCallback callback) {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyScreenTurningOn");
            this.mStatusBarKeyguardViewManager.onScreenTurningOn();
            if (callback != null) {
                if (this.mWakeAndUnlocking) {
                    this.mDrawnCallback = callback;
                } else {
                    notifyDrawn(callback);
                }
            }
            dealWithkeyguardView();
        }
    }

    private void handleNotifyScreenTurnedOn() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyScreenTurnedOn");
            this.mStatusBarKeyguardViewManager.onScreenTurnedOn();
        }
    }

    private void handleNotifyScreenTurnedOff() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyScreenTurnedOff");
            this.mStatusBarKeyguardViewManager.onScreenTurnedOff();
            this.mWakeAndUnlocking = false;
        }
    }

    private void notifyDrawn(IKeyguardDrawnCallback callback) {
        try {
            callback.onDrawn();
        } catch (RemoteException e) {
            Slog.w("KeyguardViewMediator", "Exception calling onDrawn():", e);
        }
    }

    private void resetKeyguardDonePendingLocked() {
        this.mKeyguardDonePending = false;
        this.mHandler.removeMessages(20);
    }

    public void onBootCompleted() {
        this.mUpdateMonitor.dispatchBootCompleted();
        synchronized (this) {
            this.mBootCompleted = true;
            if (this.mBootSendUserPresent) {
                sendUserPresentBroadcast();
            }
        }
    }

    public void onWakeAndUnlocking() {
        this.mWakeAndUnlocking = true;
        keyguardDone(true);
    }

    public StatusBarKeyguardViewManager registerStatusBar(PhoneStatusBar phoneStatusBar, ViewGroup container, StatusBarWindowManager statusBarWindowManager, ScrimController scrimController, FingerprintUnlockController fingerprintUnlockController) {
        Log.i("KeyguardViewMediator", "registerStatusBar called");
        this.mStatusBarKeyguardViewManager.registerStatusBar(phoneStatusBar, container, statusBarWindowManager, scrimController, fingerprintUnlockController);
        return this.mStatusBarKeyguardViewManager;
    }

    public void startKeyguardExitAnimation(long startTime, long fadeoutDuration) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(18, new StartKeyguardExitAnimParams(startTime, fadeoutDuration)));
    }

    public void onActivityDrawn() {
        this.mHandler.sendEmptyMessage(19);
    }

    public ViewMediatorCallback getViewMediatorCallback() {
        return this.mViewMediatorCallback;
    }

    public LockPatternUtils getLockPatternUtils() {
        return this.mLockPatternUtils;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print("  mSystemReady: ");
        pw.println(this.mSystemReady);
        pw.print("  mBootCompleted: ");
        pw.println(this.mBootCompleted);
        pw.print("  mBootSendUserPresent: ");
        pw.println(this.mBootSendUserPresent);
        pw.print("  mExternallyEnabled: ");
        pw.println(this.mExternallyEnabled);
        pw.print("  mNeedToReshowWhenReenabled: ");
        pw.println(this.mNeedToReshowWhenReenabled);
        pw.print("  mShowing: ");
        pw.println(this.mShowing);
        pw.print("  mInputRestricted: ");
        pw.println(this.mInputRestricted);
        pw.print("  mOccluded: ");
        pw.println(this.mOccluded);
        pw.print("  mDelayedShowingSequence: ");
        pw.println(this.mDelayedShowingSequence);
        pw.print("  mExitSecureCallback: ");
        pw.println(this.mExitSecureCallback);
        pw.print("  mDeviceInteractive: ");
        pw.println(this.mDeviceInteractive);
        pw.print("  mGoingToSleep: ");
        pw.println(this.mGoingToSleep);
        pw.print("  mHiding: ");
        pw.println(this.mHiding);
        pw.print("  mWaitingUntilKeyguardVisible: ");
        pw.println(this.mWaitingUntilKeyguardVisible);
        pw.print("  mKeyguardDonePending: ");
        pw.println(this.mKeyguardDonePending);
        pw.print("  mHideAnimationRun: ");
        pw.println(this.mHideAnimationRun);
        pw.print("  mPendingReset: ");
        pw.println(this.mPendingReset);
        pw.print("  mPendingLock: ");
        pw.println(this.mPendingLock);
        pw.print("  mWakeAndUnlocking: ");
        pw.println(this.mWakeAndUnlocking);
        pw.print("  mDrawnCallback: ");
        pw.println(this.mDrawnCallback);
    }

    private void setShowingLocked(boolean showing) {
        if (showing != this.mShowing) {
            if (this.mShowing) {
                playUnlockSounds();
            }
            this.mShowing = showing;
            for (int i = this.mKeyguardStateCallbacks.size() - 1; i >= 0; i--) {
                try {
                    ((IKeyguardStateCallback) this.mKeyguardStateCallbacks.get(i)).onShowingStateChanged(showing);
                } catch (RemoteException e) {
                    Slog.w("KeyguardViewMediator", "Failed to call onShowingStateChanged", e);
                    if (e instanceof DeadObjectException) {
                        this.mKeyguardStateCallbacks.remove(i);
                    }
                }
            }
            updateInputRestrictedLocked();
            this.mTrustManager.reportKeyguardShowingChanged();
        }
    }

    public void addStateMonitorCallback(IKeyguardStateCallback callback) {
        synchronized (this) {
            this.mKeyguardStateCallbacks.add(callback);
            try {
                callback.onSimSecureStateChanged(this.mUpdateMonitor.isSimPinSecure());
                callback.onShowingStateChanged(this.mShowing);
                callback.onInputRestrictedStateChanged(this.mInputRestricted);
            } catch (RemoteException e) {
                Slog.w("KeyguardViewMediator", "Failed to call onShowingStateChanged or onSimSecureStateChanged or onInputRestrictedStateChanged", e);
            }
        }
    }

    protected void handleExitAfterHide() {
        PerfDebugUtils.beginSystraceSection("handleExitAfterHide_notifyWakeupDevice");
        notifyWakeupDevice();
        PerfDebugUtils.endSystraceSection();
        adjustStatusBarLocked();
        sendUserPresentBroadcast();
    }

    private void playUnlockSounds() {
        if (TelephonyManager.EXTRA_STATE_IDLE.equals(this.mPhoneState) && this.mShowing) {
            GlobalContext.getBackgroundHandler().post(new Runnable() {
                public void run() {
                    KeyguardViewMediator.this.playSounds(false);
                }
            });
        }
    }

    protected void handleExitAnimate() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleExitAnimate with mKeyguardExitAnimateStart : " + this.mKeyguardExitAnimateStart);
            if (!(this.mKeyguardExitAnimateStart || this.mHideAnimation == null)) {
                handleStartKeyguardExitAnimation(SystemClock.uptimeMillis() + this.mHideAnimation.getStartOffset(), this.mHideAnimation.getDuration());
            }
        }
    }
}
