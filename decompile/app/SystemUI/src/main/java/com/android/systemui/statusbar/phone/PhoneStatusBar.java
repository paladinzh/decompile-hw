package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaController.Callback;
import android.media.session.MediaSession.Token;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.service.notification.NotificationListenerService.RankingMap;
import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ThreadedRenderer;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.view.ViewPropertyAnimator;
import android.view.ViewStub;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.keyguard.KeyguardHostView.OnDismissAction;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.AutoReinflateContainer;
import com.android.systemui.AutoReinflateContainer.InflateListener;
import com.android.systemui.DemoMode;
import com.android.systemui.EventLogTags;
import com.android.systemui.Interpolators;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.assist.HwAssistManager;
import com.android.systemui.classifier.FalsingLog;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.doze.DozeHost;
import com.android.systemui.doze.DozeHost.PulseCallback;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.keyguard.HwKeyguardViewMediator;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.qs.HwSuperpowerModeManager;
import com.android.systemui.qs.QSContainer;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.QSTile.State;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.recents.ScreenPinningRequest;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.AppTransitionFinishedEvent;
import com.android.systemui.recents.events.activity.UndockingTaskEvent;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.stackdivider.WindowManagerProxy;
import com.android.systemui.statusbar.ActivatableNotificationView;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.DismissView;
import com.android.systemui.statusbar.DragDownHelper.DragDownCallback;
import com.android.systemui.statusbar.EmptyShadeView;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.GestureRecorder;
import com.android.systemui.statusbar.HwExpandableNotificationRowHelper;
import com.android.systemui.statusbar.KeyboardShortcuts;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.statusbar.NotificationOverflowContainer;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.SignalClusterView;
import com.android.systemui.statusbar.phone.LockscreenWallpaper.WallpaperDrawable;
import com.android.systemui.statusbar.phone.NavigationBarView.OnVerticalChangedListener;
import com.android.systemui.statusbar.phone.UnlockMethodCache.OnUnlockMethodChangedListener;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback;
import com.android.systemui.statusbar.policy.BatteryControllerImpl;
import com.android.systemui.statusbar.policy.BluetoothControllerImpl;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
import com.android.systemui.statusbar.policy.CastControllerImpl;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.HeadsUpManager.HeadsUpEntry;
import com.android.systemui.statusbar.policy.HeadsUpManager.OnHeadsUpChangedListener;
import com.android.systemui.statusbar.policy.HotspotControllerImpl;
import com.android.systemui.statusbar.policy.HwBluetoothControllerImpl;
import com.android.systemui.statusbar.policy.HwNetworkControllerImpl;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.LocationControllerImpl;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.PreviewInflater;
import com.android.systemui.statusbar.policy.RotationLockControllerImpl;
import com.android.systemui.statusbar.policy.SecurityControllerImpl;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout.OnChildLocationsChangedListener;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.analyze.BDReporter;
import com.android.systemui.utils.analyze.JanklogUtils;
import com.android.systemui.utils.analyze.PerfDebugUtils;
import com.android.systemui.volume.VolumeComponent;
import com.android.systemui.wallpaper.HwWallpaperMask;
import com.huawei.cust.HwCustUtils;
import com.huawei.keyguard.inf.IFlashlightController;
import com.huawei.keyguard.view.HwBackDropView;
import com.huawei.systemui.IPhoneStatusBar;
import fyusion.vislib.BuildConfig;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PhoneStatusBar extends BaseStatusBar implements IPhoneStatusBar, DemoMode, DragDownCallback, ActivityStarter, OnUnlockMethodChangedListener, OnHeadsUpChangedListener, HwPhoneStatusBarItf {
    public static final Interpolator ALPHA_IN = Interpolators.ALPHA_IN;
    public static final Interpolator ALPHA_OUT = Interpolators.ALPHA_OUT;
    private static final boolean FREEFORM_WINDOW_MANAGEMENT;
    private static final boolean ONLY_CORE_APPS;
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new Builder().setContentType(4).setUsage(13).build();
    int[] mAbsPos = new int[2];
    AccessibilityController mAccessibilityController;
    private final Runnable mAnimateCollapsePanels = new Runnable() {
        public void run() {
            PhoneStatusBar.this.animateCollapsePanels();
        }
    };
    protected AutoReinflateContainer mAutoContainer;
    protected final Runnable mAutohide = new Runnable() {
        public void run() {
            HwLog.i("PhoneStatusBar", "auto hide");
            int requested = PhoneStatusBar.this.mSystemUiVisibility & -201326593;
            if (PhoneStatusBar.this.mSystemUiVisibility != requested) {
                PhoneStatusBar.this.notifyUiVisibilityChanged(requested);
            }
        }
    };
    private boolean mAutohideSuspended;
    protected HwBackDropView mBackdrop;
    private ImageView mBackdropBack;
    private ImageView mBackdropFront;
    BatteryController mBatteryController;
    BluetoothControllerImpl mBluetoothController;
    protected BrightnessMirrorController mBrightnessMirrorController;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action)) {
                KeyboardShortcuts.dismiss();
                if (PhoneStatusBar.this.mRemoteInputController != null) {
                    PhoneStatusBar.this.mRemoteInputController.closeRemoteInputs();
                }
                if (PhoneStatusBar.this.isCurrentProfile(getSendingUserId())) {
                    int flags = 0;
                    String reason = intent.getStringExtra("reason");
                    if (reason != null && reason.equals("recentapps")) {
                        flags = 2;
                    }
                    PhoneStatusBar.this.animateCollapsePanels(flags);
                }
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                PhoneStatusBar.this.mIsScreenOn = false;
                PhoneStatusBar.this.notifyNavigationBarScreenOn(false);
                PhoneStatusBar.this.notifyHeadsUpScreenOff();
                PhoneStatusBar.this.finishBarAnimations();
                PhoneStatusBar.this.resetUserExpandedStates();
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                PhoneStatusBar.this.mIsScreenOn = true;
                PhoneStatusBar.this.notifyNavigationBarScreenOn(true);
            }
        }
    };
    CastControllerImpl mCastController;
    private final Runnable mCheckBarModes = new Runnable() {
        public void run() {
            PhoneStatusBar.this.checkBarModes();
        }
    };
    Point mCurrentDisplaySize = new Point();
    private final ArraySet<NotificationVisibility> mCurrentlyVisibleNotifications = new ArraySet();
    HwCustPhoneStatusBar mCust;
    private boolean mDemoMode;
    private boolean mDemoModeAllowed;
    private BroadcastReceiver mDemoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.android.systemui.demo".equals(action)) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    String command = bundle.getString("command", BuildConfig.FLAVOR).trim().toLowerCase();
                    if (command.length() > 0) {
                        try {
                            PhoneStatusBar.this.dispatchDemoCommand(command, bundle);
                        } catch (Throwable t) {
                            Log.w("PhoneStatusBar", "Error running demo command, intent=" + intent, t);
                        }
                    }
                }
            } else if (!"fake_artwork".equals(action)) {
            }
        }
    };
    int mDisabled1 = 0;
    int mDisabled2 = 0;
    private int mDisabledUnmodified1;
    private int mDisabledUnmodified2;
    Display mDisplay;
    DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    protected DozeScrimController mDozeScrimController;
    private DozeServiceHost mDozeServiceHost;
    private boolean mDozing;
    private boolean mDozingRequested;
    private ExpandableNotificationRow mDraggedDownRow;
    int mEmuiStyle = -1;
    View mExpandedContents;
    boolean mExpandedVisible;
    private FalsingManager mFalsingManager;
    FingerprintUnlockController mFingerprintUnlockController;
    FlashlightController mFlashlightController;
    private final GestureRecorder mGestureRec = null;
    private WakeLock mGestureWakeLock;
    private HandlerThread mHandlerThread;
    BaseStatusBarHeader mHeader;
    private final ContentObserver mHeadsUpObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean selfChange) {
            boolean z = false;
            boolean wasUsing = PhoneStatusBar.this.mUseHeadsUp;
            PhoneStatusBar phoneStatusBar = PhoneStatusBar.this;
            boolean z2 = PhoneStatusBar.this.mDisableNotificationAlerts ? false : Global.getInt(PhoneStatusBar.this.mContext.getContentResolver(), "heads_up_notifications_enabled", 0) != 0;
            phoneStatusBar.mUseHeadsUp = z2;
            PhoneStatusBar phoneStatusBar2 = PhoneStatusBar.this;
            if (PhoneStatusBar.this.mUseHeadsUp && Global.getInt(PhoneStatusBar.this.mContext.getContentResolver(), "ticker_gets_heads_up", 0) != 0) {
                z = true;
            }
            phoneStatusBar2.mHeadsUpTicker = z;
            Log.d("PhoneStatusBar", "heads up is " + (PhoneStatusBar.this.mUseHeadsUp ? "enabled" : "disabled"));
            if (wasUsing != PhoneStatusBar.this.mUseHeadsUp && !PhoneStatusBar.this.mUseHeadsUp) {
                Log.d("PhoneStatusBar", "dismissing any existing heads up notification on disable event");
                PhoneStatusBar.this.mHeadsUpManager.releaseAllImmediately();
            }
        }
    };
    private Runnable mHideBackdropFront = new Runnable() {
        public void run() {
            if (PhoneStatusBar.this.mBackdropFront != null) {
                PhoneStatusBar.this.mBackdropFront.setVisibility(4);
                PhoneStatusBar.this.mBackdropFront.animate().cancel();
                PhoneStatusBar.this.mBackdropFront.setImageDrawable(null);
            }
        }
    };
    public final OnTouchListener mHomeActionListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case 1:
                case 3:
                    BDReporter.c(PhoneStatusBar.this.mContext, 5);
                    PhoneStatusBar.this.awakenDreams();
                    break;
            }
            return false;
        }
    };
    HotspotControllerImpl mHotspotController;
    protected StatusBarIconController mIconController;
    PhoneStatusBarPolicy mIconPolicy;
    private int mInteractingWindows;
    protected boolean mIsScreenOn = false;
    KeyguardBottomAreaView mKeyguardBottomArea;
    private boolean mKeyguardFadingAway;
    private long mKeyguardFadingAwayDelay;
    private long mKeyguardFadingAwayDuration;
    private boolean mKeyguardGoingAway;
    KeyguardIndicationController mKeyguardIndicationController;
    protected KeyguardMonitor mKeyguardMonitor;
    KeyguardStatusBarView mKeyguardStatusBar;
    View mKeyguardStatusView;
    KeyguardUserSwitcher mKeyguardUserSwitcher;
    private ViewMediatorCallback mKeyguardViewMediatorCallback;
    private int mLastCameraLaunchSource;
    private int mLastDispatchedSystemUiVisibility = -1;
    private final Rect mLastDockedStackBounds = new Rect();
    private final Rect mLastFullscreenStackBounds = new Rect();
    private int mLastLoggedStateFingerprint;
    private long mLastVisibilityReportUptimeMs;
    private RankingMap mLatestRankingMap;
    private boolean mLaunchCameraOnFinishedGoingToSleep;
    private boolean mLaunchCameraOnScreenTurningOn;
    private Runnable mLaunchTransitionEndRunnable;
    private boolean mLaunchTransitionFadingAway;
    boolean mLeaveOpenOnKeyguardHide;
    LightStatusBarController mLightStatusBarController;
    LocationControllerImpl mLocationController;
    protected LockscreenWallpaper mLockscreenWallpaper;
    public OnLongClickListener mLongPressBackListener = new OnLongClickListener() {
        public boolean onLongClick(View v) {
            return PhoneStatusBar.this.handleLongPressBack();
        }
    };
    public final OnLongClickListener mLongPressHomeListener = new OnLongClickListener() {
        public boolean onLongClick(View v) {
            if (SystemProperties.getBoolean("sys.super_power_save", false)) {
                HwLog.i("PhoneStatusBar", "go in super power, return");
                return false;
            } else if (PhoneStatusBar.this.mCust != null && PhoneStatusBar.this.mCust.disableNavigationKey()) {
                HwLog.i("PhoneStatusBar", "mLongPressHomeListener::cust disableNavigationKey, return");
                return false;
            } else if (PhoneStatusBar.mDeviceRestrictionManager.isHomeButtonDisabled(null)) {
                HwLog.i("PhoneStatusBar", "mLongPressHomeListener::device restriction manager disable home button, return");
                return false;
            } else if (PhoneStatusBar.this.shouldDisableNavbarGestures()) {
                HwLog.i("PhoneStatusBar", "mLongPressHomeListener::disable navigation bar gesture, " + PhoneStatusBar.this.mDisabled1 + ", return");
                return false;
            } else {
                int delay = 0;
                if (PhoneStatusBar.this.mNotificationPanel != null && PhoneStatusBar.this.mNotificationPanel.isFullyExpanded()) {
                    PhoneStatusBar.this.animateCollapsePanels();
                    delay = 400;
                }
                PhoneStatusBar.this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        PhoneStatusBar.this.mAssistManager.startAssist(new Bundle(), true);
                    }
                }, (long) delay);
                MetricsLogger.action(PhoneStatusBar.this.mContext, 239);
                PhoneStatusBar.this.awakenDreams();
                if (PhoneStatusBar.this.mNavigationBarView != null) {
                    PhoneStatusBar.this.mNavigationBarView.abortCurrentGesture();
                }
                return true;
            }
        }
    };
    int mMaxAllowedKeyguardNotifications;
    private int mMaxKeyguardNotifications;
    private MediaController mMediaController;
    private Callback mMediaListener = new Callback() {
        public void onPlaybackStateChanged(PlaybackState state) {
            super.onPlaybackStateChanged(state);
            if (state != null && !PhoneStatusBar.this.isPlaybackActive(state.getState())) {
                PhoneStatusBar.this.clearCurrentMediaNotification();
                PhoneStatusBar.this.updateMediaMetaData(true, true);
            }
        }

        public void onMetadataChanged(MediaMetadata metadata) {
            super.onMetadataChanged(metadata);
            PhoneStatusBar.this.mMediaMetadata = metadata;
            PhoneStatusBar.this.updateMediaMetaData(true, true);
        }
    };
    private MediaMetadata mMediaMetadata;
    private String mMediaNotificationKey;
    private MediaSessionManager mMediaSessionManager;
    private HwSuperpowerModeManager mModeManager;
    int mNaturalBarHeight = -1;
    private int mNavigationBarMode;
    private int mNavigationBarWindowState = 0;
    private int mNavigationIconHints = 0;
    NetworkControllerImpl mNetworkController;
    NextAlarmController mNextAlarmController;
    private boolean mNoAnimationOnNextBarModeChange;
    private final OnChildLocationsChangedListener mNotificationLocationsChangedListener = new OnChildLocationsChangedListener() {
        public void onChildLocationsChanged(NotificationStackScrollLayout stackScrollLayout) {
            if (!PhoneStatusBar.this.mHandler.hasCallbacks(PhoneStatusBar.this.mVisibilityReporter)) {
                PhoneStatusBar.this.mHandler.postAtTime(PhoneStatusBar.this.mVisibilityReporter, PhoneStatusBar.this.mLastVisibilityReportUptimeMs + 500);
            }
        }
    };
    protected NotificationPanelView mNotificationPanel;
    private final OnChildLocationsChangedListener mOnChildLocationsChangedListener = new OnChildLocationsChangedListener() {
        public void onChildLocationsChanged(NotificationStackScrollLayout stackScrollLayout) {
            PhoneStatusBar.this.userActivity();
        }
    };
    private final OnClickListener mOverflowClickListener = new OnClickListener() {
        public void onClick(View v) {
            PhoneStatusBar.this.goToLockedShade(null);
        }
    };
    protected View mPendingRemoteInputView;
    private View mPendingWorkRemoteInputView;
    int mPixelFormat;
    ArrayList<Runnable> mPostCollapseRunnables = new ArrayList();
    protected QSCustomizer mQSCustomizer;
    protected QSPanel mQSPanel;
    Object mQueueLock = new Object();
    public OnClickListener mRecentsClickListener = new OnClickListener() {
        public void onClick(View v) {
            BDReporter.c(PhoneStatusBar.this.mContext, 7);
            if (PhoneStatusBar.this.mCust != null && PhoneStatusBar.this.mCust.disableNavigationKey()) {
                Log.d("PhoneStatusBar", "disable recent click, return");
            } else if (PhoneStatusBar.mDeviceRestrictionManager.isTaskButtonDisabled(null)) {
                Log.d("PhoneStatusBar", "the task key is disabled! OnClickListener return;");
            } else {
                try {
                    PerfDebugUtils.beginSystraceSection("PhoneStatusBar.RecentsClick");
                    if (!SystemProperties.getBoolean("sys.super_power_save", false)) {
                        JanklogUtils.eventBegin(135);
                        PerfDebugUtils.perfRecentsLaunchElapsedTimeBegin(1);
                        PhoneStatusBar.this.awakenDreams();
                        PhoneStatusBar.this.toggleRecentApps();
                        PerfDebugUtils.endSystraceSection();
                    }
                } finally {
                    PerfDebugUtils.endSystraceSection();
                }
            }
        }
    };
    public OnLongClickListener mRecentsLongClickListener = new OnLongClickListener() {
        public boolean onLongClick(View v) {
            if (PhoneStatusBar.this.mCust != null && PhoneStatusBar.this.mCust.disableNavigationKey()) {
                Log.d("PhoneStatusBar", "disable recent long click, return");
                return false;
            } else if (PhoneStatusBar.mDeviceRestrictionManager.isTaskButtonDisabled(null)) {
                Log.d("PhoneStatusBar", "the task key is disabled! OnLongClickListener return false;");
                return false;
            } else {
                boolean lIsUltraPower = SystemProperties.getBoolean("sys.super_power_save", false);
                if (PhoneStatusBar.this.mRecents == null || !ActivityManager.supportsMultiWindow() || !((Divider) PhoneStatusBar.this.getComponent(Divider.class)).getView().getSnapAlgorithm().isSplitScreenFeasible() || lIsUltraPower) {
                    return false;
                }
                PhoneStatusBar.this.toggleSplitScreenMode(271, 286);
                return true;
            }
        }
    };
    RotationLockControllerImpl mRotationLockController;
    private ScreenPinningRequest mScreenPinningRequest;
    private boolean mScreenTurningOn;
    protected ScrimController mScrimController;
    protected boolean mScrimSrcModeEnabled;
    SecurityControllerImpl mSecurityController;
    private final ShadeUpdates mShadeUpdates = new ShadeUpdates();
    private PorterDuffXfermode mSrcOverXferMode = new PorterDuffXfermode(Mode.SRC_OVER);
    private PorterDuffXfermode mSrcXferMode = new PorterDuffXfermode(Mode.SRC);
    Runnable mStartTracing = new Runnable() {
        public void run() {
            PhoneStatusBar.this.vibrate();
            SystemClock.sleep(250);
            Log.d("PhoneStatusBar", "startTracing");
            Debug.startMethodTracing("/data/statusbar-traces/trace");
            PhoneStatusBar.this.mHandler.postDelayed(PhoneStatusBar.this.mStopTracing, 10000);
        }
    };
    protected boolean mStartedGoingToSleep;
    private int mStatusBarMode;
    protected PhoneStatusBarView mStatusBarView;
    protected StatusBarWindowView mStatusBarWindow;
    protected StatusBarWindowManager mStatusBarWindowManager;
    protected int mStatusBarWindowState = 0;
    Runnable mStopTracing = new Runnable() {
        public void run() {
            Debug.stopMethodTracing();
            Log.d("PhoneStatusBar", "stopTracing");
            PhoneStatusBar.this.vibrate();
        }
    };
    int mSystemUiVisibility = 0;
    private HashMap<ExpandableNotificationRow, List<ExpandableNotificationRow>> mTmpChildOrderMap = new HashMap();
    boolean mTracking;
    int mTrackingPosition;
    private UnlockMethodCache mUnlockMethodCache;
    UserInfoController mUserInfoController;
    private boolean mUserSetup = false;
    private ContentObserver mUserSetupObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            if (PhoneStatusBar.this.mCurrentUserId != -1) {
                boolean userSetup = Secure.getIntForUser(PhoneStatusBar.this.mContext.getContentResolver(), "user_setup_complete", 0, PhoneStatusBar.this.mCurrentUserId) != 0;
                if (userSetup != PhoneStatusBar.this.mUserSetup) {
                    PhoneStatusBar.this.mUserSetup = userSetup;
                    if (!(PhoneStatusBar.this.mUserSetup || PhoneStatusBar.this.mStatusBarView == null)) {
                        PhoneStatusBar.this.animateCollapseQuickSettings();
                    }
                    if (PhoneStatusBar.this.mKeyguardBottomArea != null) {
                        PhoneStatusBar.this.mKeyguardBottomArea.setUserSetupComplete(PhoneStatusBar.this.mUserSetup);
                    }
                    if (PhoneStatusBar.this.mNetworkController != null) {
                        PhoneStatusBar.this.mNetworkController.setUserSetupComplete(PhoneStatusBar.this.mUserSetup);
                    }
                }
                if (PhoneStatusBar.this.mIconPolicy != null) {
                    PhoneStatusBar.this.mIconPolicy.setCurrentUserSetup(PhoneStatusBar.this.mUserSetup);
                }
            }
        }
    };
    protected UserSwitcherController mUserSwitcherController;
    private Vibrator mVibrator;
    private final Runnable mVisibilityReporter = new Runnable() {
        private final ArraySet<NotificationVisibility> mTmpCurrentlyVisibleNotifications = new ArraySet();
        private final ArraySet<NotificationVisibility> mTmpNewlyVisibleNotifications = new ArraySet();
        private final ArraySet<NotificationVisibility> mTmpNoLongerVisibleNotifications = new ArraySet();

        public void run() {
            PhoneStatusBar.this.mLastVisibilityReportUptimeMs = SystemClock.uptimeMillis();
            String mediaKey = PhoneStatusBar.this.getCurrentMediaNotificationKey();
            ArrayList<Entry> activeNotifications = PhoneStatusBar.this.mNotificationData.getActiveNotifications();
            int N = activeNotifications.size();
            for (int i = 0; i < N; i++) {
                boolean isVisible;
                Entry entry = (Entry) activeNotifications.get(i);
                String key = entry.notification.getKey();
                if ((PhoneStatusBar.this.mStackScroller.getChildLocation(entry.row) & 5) != 0) {
                    isVisible = true;
                } else {
                    isVisible = false;
                }
                NotificationVisibility visObj = NotificationVisibility.obtain(key, i, isVisible);
                boolean previouslyVisible = PhoneStatusBar.this.mCurrentlyVisibleNotifications.contains(visObj);
                if (isVisible) {
                    this.mTmpCurrentlyVisibleNotifications.add(visObj);
                    if (!previouslyVisible) {
                        this.mTmpNewlyVisibleNotifications.add(visObj);
                    }
                } else {
                    visObj.recycle();
                }
            }
            this.mTmpNoLongerVisibleNotifications.addAll(PhoneStatusBar.this.mCurrentlyVisibleNotifications);
            this.mTmpNoLongerVisibleNotifications.removeAll(this.mTmpCurrentlyVisibleNotifications);
            PhoneStatusBar.this.logNotificationVisibilityChanges(this.mTmpNewlyVisibleNotifications, this.mTmpNoLongerVisibleNotifications);
            PhoneStatusBar.this.recycleAllVisibilityObjects(PhoneStatusBar.this.mCurrentlyVisibleNotifications);
            PhoneStatusBar.this.mCurrentlyVisibleNotifications.addAll(this.mTmpCurrentlyVisibleNotifications);
            PhoneStatusBar.this.recycleAllVisibilityObjects(this.mTmpNoLongerVisibleNotifications);
            this.mTmpCurrentlyVisibleNotifications.clear();
            this.mTmpNewlyVisibleNotifications.clear();
            this.mTmpNoLongerVisibleNotifications.clear();
        }
    };
    VolumeComponent mVolumeComponent;
    private boolean mWaitingForKeyguardExit;
    private boolean mWakeUpComingFromTouch;
    private PointF mWakeUpTouchLocation;
    protected ZenModeController mZenModeController;

    final /* synthetic */ class -void_onStartedWakingUp__LambdaImpl0 implements Runnable {
        private /* synthetic */ PhoneStatusBar val$this;

        public /* synthetic */ -void_onStartedWakingUp__LambdaImpl0(PhoneStatusBar phoneStatusBar) {
            this.val$this = phoneStatusBar;
        }

        public void run() {
            this.val$this.-com_android_systemui_statusbar_phone_PhoneStatusBar_lambda$2();
        }
    }

    private final class DozeServiceHost extends KeyguardUpdateMonitorCallback implements DozeHost {
        private final ArrayList<DozeHost.Callback> mCallbacks;
        private final H mHandler;
        private boolean mNotificationLightOn;

        private final class H extends Handler {
            private H() {
            }

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        DozeServiceHost.this.handleStartDozing((Runnable) msg.obj);
                        return;
                    case 2:
                        DozeServiceHost.this.handlePulseWhileDozing((PulseCallback) msg.obj, msg.arg1);
                        return;
                    case 3:
                        DozeServiceHost.this.handleStopDozing();
                        return;
                    default:
                        return;
                }
            }
        }

        private DozeServiceHost() {
            this.mCallbacks = new ArrayList();
            this.mHandler = new H();
        }

        public String toString() {
            return "PSB.DozeServiceHost[mCallbacks=" + this.mCallbacks.size() + "]";
        }

        public void firePowerSaveChanged(boolean active) {
            for (DozeHost.Callback callback : this.mCallbacks) {
                callback.onPowerSaveChanged(active);
            }
        }

        public void fireBuzzBeepBlinked() {
            for (DozeHost.Callback callback : this.mCallbacks) {
                callback.onBuzzBeepBlinked();
            }
        }

        public void fireNotificationLight(boolean on) {
            this.mNotificationLightOn = on;
            for (DozeHost.Callback callback : this.mCallbacks) {
                callback.onNotificationLight(on);
            }
        }

        public void fireNewNotifications() {
            for (DozeHost.Callback callback : this.mCallbacks) {
                callback.onNewNotifications();
            }
        }

        public void addCallback(DozeHost.Callback callback) {
            this.mCallbacks.add(callback);
        }

        public void removeCallback(DozeHost.Callback callback) {
            this.mCallbacks.remove(callback);
        }

        public void startDozing(Runnable ready) {
            this.mHandler.obtainMessage(1, ready).sendToTarget();
        }

        public void pulseWhileDozing(PulseCallback callback, int reason) {
            this.mHandler.obtainMessage(2, reason, 0, callback).sendToTarget();
        }

        public void stopDozing() {
            this.mHandler.obtainMessage(3).sendToTarget();
        }

        public boolean isPowerSaveActive() {
            return PhoneStatusBar.this.mBatteryController != null ? PhoneStatusBar.this.mBatteryController.isPowerSave() : false;
        }

        public boolean isPulsingBlocked() {
            return PhoneStatusBar.this.mFingerprintUnlockController.getMode() == 1;
        }

        public boolean isNotificationLightOn() {
            return this.mNotificationLightOn;
        }

        private void handleStartDozing(Runnable ready) {
            if (!PhoneStatusBar.this.mDozingRequested) {
                PhoneStatusBar.this.mDozingRequested = true;
                DozeLog.traceDozing(PhoneStatusBar.this.mContext, PhoneStatusBar.this.mDozing);
                PhoneStatusBar.this.updateDozing();
            }
            ready.run();
        }

        private void handlePulseWhileDozing(final PulseCallback callback, int reason) {
            PhoneStatusBar.this.mDozeScrimController.pulse(new PulseCallback() {
                public void onPulseStarted() {
                    callback.onPulseStarted();
                    PhoneStatusBar.this.mStackScroller.setPulsing(true);
                }

                public void onPulseFinished() {
                    callback.onPulseFinished();
                    PhoneStatusBar.this.mStackScroller.setPulsing(false);
                }
            }, reason);
        }

        private void handleStopDozing() {
            if (PhoneStatusBar.this.mDozingRequested) {
                PhoneStatusBar.this.mDozingRequested = false;
                DozeLog.traceDozing(PhoneStatusBar.this.mContext, PhoneStatusBar.this.mDozing);
                PhoneStatusBar.this.updateDozing();
            }
        }
    }

    private static class FastColorDrawable extends Drawable {
        private final int mColor;

        public FastColorDrawable(int color) {
            this.mColor = -16777216 | color;
        }

        public void draw(Canvas canvas) {
            canvas.drawColor(this.mColor, Mode.SRC);
        }

        public void setAlpha(int alpha) {
        }

        public void setColorFilter(ColorFilter colorFilter) {
        }

        public int getOpacity() {
            return -1;
        }

        public void setBounds(int left, int top, int right, int bottom) {
        }

        public void setBounds(Rect bounds) {
        }
    }

    private class H extends H {
        private H() {
            super();
        }

        public void handleMessage(Message m) {
            super.handleMessage(m);
            switch (m.what) {
                case 1000:
                    PhoneStatusBar.this.animateExpandNotificationsPanel();
                    return;
                case 1001:
                    PhoneStatusBar.this.animateCollapsePanels();
                    return;
                case 1002:
                    PhoneStatusBar.this.animateExpandSettingsPanel((String) m.obj);
                    return;
                case 1003:
                    PhoneStatusBar.this.onLaunchTransitionTimeout();
                    return;
                default:
                    return;
            }
        }
    }

    private final class ShadeUpdates {
        private final ArraySet<String> mNewVisibleNotifications;
        private final ArraySet<String> mVisibleNotifications;

        private ShadeUpdates() {
            this.mVisibleNotifications = new ArraySet();
            this.mNewVisibleNotifications = new ArraySet();
        }

        public void check() {
            this.mNewVisibleNotifications.clear();
            ArrayList<Entry> activeNotifications = PhoneStatusBar.this.mNotificationData.getActiveNotifications();
            for (int i = 0; i < activeNotifications.size(); i++) {
                Entry entry = (Entry) activeNotifications.get(i);
                boolean visible = entry.row != null ? entry.row.getVisibility() == 0 : false;
                if (visible) {
                    this.mNewVisibleNotifications.add(entry.key + entry.notification.getPostTime());
                }
            }
            boolean updates = !this.mVisibleNotifications.containsAll(this.mNewVisibleNotifications);
            this.mVisibleNotifications.clear();
            this.mVisibleNotifications.addAll(this.mNewVisibleNotifications);
            if (updates && PhoneStatusBar.this.mDozeServiceHost != null) {
                PhoneStatusBar.this.mDozeServiceHost.fireNewNotifications();
            }
        }
    }

    public abstract boolean getDropbackViewHideStatus();

    public abstract boolean getFastUnlockMode();

    public abstract boolean getFpUnlockingStatus();

    public abstract KeyguardStatusBarView getKeyguardStatusBarView();

    public abstract void hideDropbackView();

    public abstract void hideNotificationToast();

    public abstract boolean isFullscreenBouncer();

    public abstract void setBokehChangeStatus(boolean z);

    public void setInteracting(int r1, boolean r2) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.systemui.statusbar.phone.PhoneStatusBar.setInteracting(int, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.PhoneStatusBar.setInteracting(int, boolean):void");
    }

    public void setSystemUiVisibility(int r1, int r2, int r3, int r4, android.graphics.Rect r5, android.graphics.Rect r6) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.systemui.statusbar.phone.PhoneStatusBar.setSystemUiVisibility(int, int, int, int, android.graphics.Rect, android.graphics.Rect):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.PhoneStatusBar.setSystemUiVisibility(int, int, int, int, android.graphics.Rect, android.graphics.Rect):void");
    }

    public abstract void showDropbackView();

    public abstract void showNotificationToast(boolean z);

    static {
        boolean onlyCoreApps;
        boolean hasSystemFeature;
        try {
            IPackageManager packageManager = Stub.asInterface(ServiceManager.getService("package"));
            onlyCoreApps = packageManager.isOnlyCoreApps();
            hasSystemFeature = packageManager.hasSystemFeature("android.software.freeform_window_management", 0);
        } catch (RemoteException e) {
            onlyCoreApps = false;
            hasSystemFeature = false;
        }
        ONLY_CORE_APPS = onlyCoreApps;
        FREEFORM_WINDOW_MANAGEMENT = hasSystemFeature;
    }

    private void recycleAllVisibilityObjects(ArraySet<NotificationVisibility> array) {
        int N = array.size();
        for (int i = 0; i < N; i++) {
            ((NotificationVisibility) array.valueAt(i)).recycle();
        }
        array.clear();
    }

    public void start() {
        this.mCust = (HwCustPhoneStatusBar) HwCustUtils.createObj(HwCustPhoneStatusBar.class, new Object[]{this.mContext});
        this.mDisplay = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
        updateDisplaySize();
        this.mScrimSrcModeEnabled = this.mContext.getResources().getBoolean(R.bool.config_status_bar_scrim_behind_use_src);
        super.start();
        this.mMediaSessionManager = (MediaSessionManager) this.mContext.getSystemService("media_session");
        addNavigationBar();
        this.mIconPolicy = new HwPhoneStatusBarPolicy(this.mContext, this.mIconController, this.mCastController, this.mHotspotController, this.mUserInfoController, this.mBluetoothController, this.mRotationLockController, this.mNetworkController.getDataSaverController());
        this.mIconPolicy.setCurrentUserSetup(this.mUserSetup);
        this.mSettingsObserver.onChange(false);
        this.mHeadsUpObserver.onChange(true);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("heads_up_notifications_enabled"), true, this.mHeadsUpObserver);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("ticker_gets_heads_up"), true, this.mHeadsUpObserver);
        this.mUnlockMethodCache = UnlockMethodCache.getInstance(this.mContext);
        this.mUnlockMethodCache.addListener(this);
        startKeyguard();
        this.mDozeServiceHost = new DozeServiceHost();
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mDozeServiceHost);
        putComponent(DozeHost.class, this.mDozeServiceHost);
        putComponent(PhoneStatusBar.class, this);
        setControllerUsers();
        notifyUserAboutHiddenNotifications();
        this.mScreenPinningRequest = new ScreenPinningRequest(this.mContext);
        this.mFalsingManager = FalsingManager.getInstance(this.mContext);
    }

    protected void createIconController() {
        this.mIconController = new StatusBarIconController(this.mContext, this.mStatusBarView, this.mKeyguardStatusBar, this);
    }

    protected PhoneStatusBarView makeStatusBarView() {
        Context context = this.mContext;
        updateDisplaySize();
        updateResources();
        inflateStatusBarWindow(context);
        this.mStatusBarWindow.setService(this);
        this.mStatusBarWindow.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                PhoneStatusBar.this.checkUserAutohide(v, event);
                if (event.getAction() == 0 && PhoneStatusBar.this.mExpandedVisible) {
                    PhoneStatusBar.this.animateCollapsePanels();
                }
                return PhoneStatusBar.this.mStatusBarWindow.onTouchEvent(event);
            }
        });
        this.mNotificationPanel = (NotificationPanelView) this.mStatusBarWindow.findViewById(R.id.notification_panel);
        this.mNotificationPanel.setStatusBar(this);
        this.mNotificationPanel.setGroupManager(this.mGroupManager);
        this.mQSCustomizer = (QSCustomizer) this.mNotificationPanel.findViewById(R.id.qs_customize);
        this.mStatusBarView = (PhoneStatusBarView) this.mStatusBarWindow.findViewById(R.id.status_bar);
        this.mStatusBarView.setBar(this);
        this.mStatusBarView.setPanel(this.mNotificationPanel);
        if (!ActivityManager.isHighEndGfx()) {
            this.mStatusBarWindow.setBackground(null);
            this.mNotificationPanel.setBackground(new FastColorDrawable(context.getColor(R.color.notification_panel_solid_background)));
        }
        this.mHeadsUpManager = new HeadsUpManager(context, this.mStatusBarWindow, this.mGroupManager);
        this.mHeadsUpManager.setBar(this);
        this.mHeadsUpManager.addListener(this);
        this.mHeadsUpManager.addListener(this.mNotificationPanel);
        this.mHeadsUpManager.addListener(this.mGroupManager);
        this.mNotificationPanel.setHeadsUpManager(this.mHeadsUpManager);
        this.mNotificationData.setHeadsUpManager(this.mHeadsUpManager);
        this.mGroupManager.setHeadsUpManager(this.mHeadsUpManager);
        try {
            if (this.mWindowManagerService.hasNavigationBar()) {
                createNavigationBarView(context);
            }
        } catch (RemoteException e) {
        }
        this.mAssistManager = new HwAssistManager(this, context);
        this.mPixelFormat = -1;
        this.mStackScroller = (NotificationStackScrollLayout) this.mStatusBarWindow.findViewById(R.id.notification_stack_scroller);
        this.mStackScroller.setLongPressListener(this.mNotificationSetting, getNotificationLongClicker());
        this.mStackScroller.setPhoneStatusBar(this);
        this.mStackScroller.setGroupManager(this.mGroupManager);
        this.mStackScroller.setHeadsUpManager(this.mHeadsUpManager);
        this.mGroupManager.setOnGroupChangeListener(this.mStackScroller);
        inflateOverflowContainer();
        inflateEmptyShadeView();
        inflateDismissView();
        this.mExpandedContents = this.mStackScroller;
        this.mBackdrop = (HwBackDropView) this.mStatusBarWindow.findViewById(R.id.backdrop);
        this.mBackdropFront = (ImageView) this.mBackdrop.findViewById(R.id.backdrop_front);
        this.mBackdropBack = this.mBackdrop.getBackdropBack();
        this.mScrimController = SystemUIFactory.getInstance().createScrimController((ScrimView) this.mStatusBarWindow.findViewById(R.id.scrim_behind), (ScrimView) this.mStatusBarWindow.findViewById(R.id.scrim_in_front), this.mStatusBarWindow.findViewById(R.id.heads_up_scrim));
        if (this.mScrimSrcModeEnabled) {
            Runnable anonymousClass22 = new Runnable() {
                public void run() {
                    boolean asSrc = PhoneStatusBar.this.mBackdrop.getVisibility() != 0;
                    PhoneStatusBar.this.mScrimController.setDrawBehindAsSrc(asSrc);
                    PhoneStatusBar.this.mStackScroller.setDrawBackgroundAsSrc(asSrc);
                }
            };
            this.mBackdrop.setOnVisibilityChangedRunnable(anonymousClass22);
            anonymousClass22.run();
        }
        this.mHeadsUpManager.addListener(this.mScrimController);
        this.mStackScroller.setScrimController(this.mScrimController);
        this.mStatusBarView.setScrimController(this.mScrimController);
        this.mDozeScrimController = new DozeScrimController(this.mScrimController, context);
        this.mKeyguardStatusBar = (KeyguardStatusBarView) this.mStatusBarWindow.findViewById(R.id.keyguard_header);
        this.mKeyguardStatusView = this.mStatusBarWindow.findViewById(R.id.keyguard_status_view);
        this.mKeyguardBottomArea = (KeyguardBottomAreaView) this.mStatusBarWindow.findViewById(R.id.keyguard_bottom_area);
        if (this.mKeyguardBottomArea != null) {
            this.mKeyguardBottomArea.setActivityStarter(this);
            this.mKeyguardBottomArea.setAssistManager(this.mAssistManager);
            this.mKeyguardIndicationController = new KeyguardIndicationController(this.mContext, (KeyguardIndicationTextView) this.mStatusBarWindow.findViewById(R.id.keyguard_indication_text), this.mKeyguardBottomArea.getLockIcon());
            this.mKeyguardBottomArea.setKeyguardIndicationController(this.mKeyguardIndicationController);
        }
        this.mLockscreenWallpaper = new LockscreenWallpaper(this.mContext, this, this.mHandler);
        setAreThereNotifications();
        createIconController();
        this.mHandlerThread = new HandlerThread("PhoneStatusBar", 10);
        this.mHandlerThread.start();
        this.mLocationController = new LocationControllerImpl(this.mContext, this.mHandlerThread.getLooper());
        this.mBatteryController = createBatteryController();
        this.mBatteryController.addStateChangedCallback(new BatteryStateChangeCallback() {
            public void onPowerSaveChanged(boolean isPowerSave) {
                PhoneStatusBar.this.mHandler.post(PhoneStatusBar.this.mCheckBarModes);
                if (PhoneStatusBar.this.mDozeServiceHost != null) {
                    PhoneStatusBar.this.mDozeServiceHost.firePowerSaveChanged(isPowerSave);
                }
            }

            public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
            }
        });
        this.mNetworkController = new HwNetworkControllerImpl(this.mContext, this.mHandlerThread.getLooper());
        this.mNetworkController.setUserSetupComplete(this.mUserSetup);
        this.mHotspotController = new HotspotControllerImpl(this.mContext);
        this.mBluetoothController = new HwBluetoothControllerImpl(this.mContext, this.mHandlerThread.getLooper());
        this.mSecurityController = new SecurityControllerImpl(this.mContext);
        if (this.mContext.getResources().getBoolean(R.bool.config_showRotationLock)) {
            this.mRotationLockController = new RotationLockControllerImpl(this.mContext);
        }
        this.mUserInfoController = new UserInfoController(this.mContext);
        this.mVolumeComponent = (VolumeComponent) getComponent(VolumeComponent.class);
        if (this.mVolumeComponent != null) {
            this.mZenModeController = this.mVolumeComponent.getZenController();
        }
        this.mCastController = new CastControllerImpl(this.mContext);
        initSignalCluster(this.mStatusBarView);
        initSignalCluster(this.mKeyguardStatusBar);
        this.mFlashlightController = new FlashlightController(this.mContext);
        this.mAccessibilityController = new AccessibilityController(this.mContext);
        if (this.mKeyguardBottomArea != null) {
            this.mKeyguardBottomArea.setFlashlightController(this.mFlashlightController);
            this.mKeyguardBottomArea.setPhoneStatusBar(this);
            this.mKeyguardBottomArea.setUserSetupComplete(this.mUserSetup);
            this.mKeyguardBottomArea.setAccessibilityController(this.mAccessibilityController);
        }
        this.mNextAlarmController = new NextAlarmController(this.mContext);
        this.mLightStatusBarController = new LightStatusBarController(this.mIconController, this.mBatteryController);
        this.mKeyguardMonitor = new KeyguardMonitor(this.mContext);
        if (UserManager.get(this.mContext).isUserSwitcherEnabled()) {
            this.mUserSwitcherController = new UserSwitcherController(this.mContext, this.mKeyguardMonitor, this.mHandler, this);
            createUserSwitcher();
        }
        this.mModeManager = new HwSuperpowerModeManager();
        this.mModeManager.init(this.mContext);
        this.mAutoContainer = (AutoReinflateContainer) this.mStatusBarWindow.findViewById(R.id.qs_auto_reinflate_container);
        if (this.mAutoContainer != null) {
            QSTileHost qsh = SystemUIFactory.getInstance().createQSTileHost(this.mContext, this, this.mBluetoothController, this.mLocationController, this.mRotationLockController, this.mNetworkController, this.mZenModeController, this.mHotspotController, this.mCastController, this.mFlashlightController, this.mUserSwitcherController, this.mUserInfoController, this.mKeyguardMonitor, this.mSecurityController, this.mBatteryController, this.mIconController, this.mNextAlarmController);
            this.mBrightnessMirrorController = new BrightnessMirrorController(this.mStatusBarWindow);
            final QSTileHost qSTileHost = qsh;
            this.mAutoContainer.addInflateListener(new InflateListener() {
                public void onInflated(View v) {
                    QSContainer qsContainer = (QSContainer) v.findViewById(R.id.quick_settings_container);
                    qsContainer.setHost(qSTileHost);
                    PhoneStatusBar.this.mModeManager.removeAllCallbacks();
                    PhoneStatusBar.this.mModeManager.addCallback(qsContainer);
                    PhoneStatusBar.this.mQSPanel = qsContainer.getQsPanel();
                    PhoneStatusBar.this.mQSPanel.registerBrightnessControllerCallback(true);
                    PhoneStatusBar.this.mQSPanel.setBrightnessMirror(PhoneStatusBar.this.mBrightnessMirrorController);
                    PhoneStatusBar.this.mQSPanel.addCallback(PhoneStatusBar.this.mModeManager);
                    PhoneStatusBar.this.mKeyguardStatusBar.setQSPanel(PhoneStatusBar.this.mQSPanel);
                    PhoneStatusBar.this.mHeader = qsContainer.getHeader();
                    PhoneStatusBar.this.initSignalCluster(PhoneStatusBar.this.mHeader);
                    PhoneStatusBar.this.mHeader.setActivityStarter(PhoneStatusBar.this);
                    PhoneStatusBar.this.mQSCustomizer = qsContainer.getCustomizer();
                    KeyguardViewMediator keyguardViewMediator = (KeyguardViewMediator) PhoneStatusBar.this.getComponent(HwKeyguardViewMediator.class);
                    if (keyguardViewMediator != null) {
                        ((HwKeyguardViewMediator) keyguardViewMediator).setBrightnessController(PhoneStatusBar.this.mQSPanel.getBrightnessController());
                    }
                }

                public void onAllViewsRemoved() {
                    if (PhoneStatusBar.this.mQSPanel != null) {
                        PhoneStatusBar.this.mQSPanel.registerBrightnessControllerCallback(false);
                    }
                }
            });
        }
        this.mKeyguardStatusBar.setUserInfoController(this.mUserInfoController);
        this.mKeyguardStatusBar.setUserSwitcherController(this.mUserSwitcherController);
        this.mUserInfoController.reloadUserInfo();
        PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
        this.mBroadcastReceiver.onReceive(this.mContext, new Intent(pm.isScreenOn() ? "android.intent.action.SCREEN_ON" : "android.intent.action.SCREEN_OFF"));
        this.mGestureWakeLock = pm.newWakeLock(10, "GestureWakeLock");
        this.mVibrator = (Vibrator) this.mContext.getSystemService(Vibrator.class);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.SCREEN_ON");
        context.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, filter, null, null);
        IntentFilter demoFilter = new IntentFilter();
        demoFilter.addAction("com.android.systemui.demo");
        context.registerReceiverAsUser(this.mDemoReceiver, UserHandle.ALL, demoFilter, "android.permission.DUMP", null);
        resetUserSetupObserver();
        ThreadedRenderer.overrideProperty("disableProfileBars", "true");
        ThreadedRenderer.overrideProperty("ambientRatio", String.valueOf(1.5f));
        return this.mStatusBarView;
    }

    protected BatteryController createBatteryController() {
        return new BatteryControllerImpl(this.mContext);
    }

    private void inflateOverflowContainer() {
        this.mKeyguardIconOverflowContainer = (NotificationOverflowContainer) LayoutInflater.from(this.mContext).inflate(R.layout.status_bar_notification_keyguard_overflow, this.mStackScroller, false);
        if (this.mKeyguardIconOverflowContainer != null) {
            this.mKeyguardIconOverflowContainer.setOnActivatedListener(this);
            this.mKeyguardIconOverflowContainer.setOnClickListener(this.mOverflowClickListener);
            this.mStackScroller.setOverflowContainer(this.mKeyguardIconOverflowContainer);
        }
    }

    protected void onDensityOrFontScaleChanged() {
        super.onDensityOrFontScaleChanged();
        this.mScrimController.onDensityOrFontScaleChanged();
        this.mStatusBarView.onDensityOrFontScaleChanged();
        if (this.mBrightnessMirrorController != null) {
            this.mBrightnessMirrorController.onDensityOrFontScaleChanged();
        }
        inflateSignalClusters();
        this.mIconController.onDensityOrFontScaleChanged();
        inflateDismissView();
        updateClearAll();
        inflateEmptyShadeView();
        updateEmptyShadeView();
        inflateOverflowContainer();
        this.mStatusBarKeyguardViewManager.onDensityOrFontScaleChanged();
        this.mUserInfoController.onDensityOrFontScaleChanged();
        if (this.mUserSwitcherController != null) {
            this.mUserSwitcherController.onDensityOrFontScaleChanged();
        }
        if (this.mKeyguardUserSwitcher != null) {
            this.mKeyguardUserSwitcher.onDensityOrFontScaleChanged();
        }
        updateRowStates();
        this.mIconController.updateNotificationIcons(this.mNotificationData);
    }

    private void inflateSignalClusters() {
        this.mIconController.setSignalCluster(reinflateSignalCluster(this.mStatusBarView));
        reinflateSignalCluster(this.mKeyguardStatusBar);
    }

    private SignalClusterView reinflateSignalCluster(View view) {
        SignalClusterView signalCluster = (SignalClusterView) view.findViewById(R.id.signal_cluster);
        if (signalCluster == null) {
            return null;
        }
        ViewParent parent = signalCluster.getParent();
        if (!(parent instanceof ViewGroup)) {
            return signalCluster;
        }
        ViewGroup viewParent = (ViewGroup) parent;
        int index = viewParent.indexOfChild(signalCluster);
        viewParent.removeView(signalCluster);
        SignalClusterView newCluster = (SignalClusterView) LayoutInflater.from(this.mContext).inflate(R.layout.signal_cluster_view, viewParent, false);
        MarginLayoutParams layoutParams = (MarginLayoutParams) viewParent.getLayoutParams();
        layoutParams.setMarginsRelative(this.mContext.getResources().getDimensionPixelSize(R.dimen.signal_cluster_margin_start), 0, 0, 0);
        newCluster.setLayoutParams(layoutParams);
        newCluster.setSecurityController(this.mSecurityController);
        newCluster.setNetworkController(this.mNetworkController);
        viewParent.addView(newCluster, index);
        return newCluster;
    }

    private void inflateEmptyShadeView() {
        this.mEmptyShadeView = (EmptyShadeView) LayoutInflater.from(this.mContext).inflate(R.layout.status_bar_no_notifications, this.mStackScroller, false);
        this.mStackScroller.setEmptyShadeView(this.mEmptyShadeView);
    }

    private void inflateDismissView() {
        this.mDismissView = (DismissView) LayoutInflater.from(this.mContext).inflate(R.layout.status_bar_notification_dismiss_all, this.mStackScroller, false);
        this.mDismissView.setOnButtonClickListener(new OnClickListener() {
            public void onClick(View v) {
                MetricsLogger.action(PhoneStatusBar.this.mContext, 148);
                PhoneStatusBar.this.clearAllNotifications();
            }
        });
        this.mStackScroller.setDismissView(this.mDismissView);
    }

    protected void createUserSwitcher() {
        this.mKeyguardUserSwitcher = new KeyguardUserSwitcher(this.mContext, (ViewStub) this.mStatusBarWindow.findViewById(R.id.keyguard_user_switcher), this.mKeyguardStatusBar, this.mNotificationPanel, this.mUserSwitcherController);
    }

    protected void inflateStatusBarWindow(Context context) {
        this.mStatusBarWindow = (StatusBarWindowView) View.inflate(context, R.layout.super_status_bar, null);
    }

    protected void createNavigationBarView(Context context) {
        inflateNavigationBarView(context);
        this.mNavigationBarView.setDisabledFlags(this.mDisabled1);
        this.mNavigationBarView.setComponents(this.mRecents, (Divider) getComponent(Divider.class));
        this.mNavigationBarView.setOnVerticalChangedListener(new OnVerticalChangedListener() {
            public void onVerticalChanged(boolean isVertical) {
                if (PhoneStatusBar.this.mAssistManager != null) {
                    PhoneStatusBar.this.mAssistManager.onConfigurationChanged();
                }
                PhoneStatusBar.this.mNotificationPanel.setQsScrimEnabled(!isVertical);
            }
        });
        this.mNavigationBarView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                PhoneStatusBar.this.checkUserAutohide(v, event);
                return false;
            }
        });
    }

    protected void inflateNavigationBarView(Context context) {
        this.mNavigationBarView = (NavigationBarView) View.inflate(context, R.layout.navigation_bar, null);
    }

    protected void initSignalCluster(View containerView) {
        SignalClusterView signalCluster = (SignalClusterView) containerView.findViewById(R.id.signal_cluster);
        if (signalCluster != null) {
            signalCluster.setSecurityController(this.mSecurityController);
            signalCluster.setNetworkController(this.mNetworkController);
        }
    }

    public void clearAllNotifications() {
        int numChildren = this.mStackScroller.getChildCount();
        ArrayList<View> viewsToHide = new ArrayList(numChildren);
        for (int i = 0; i < numChildren; i++) {
            View child = this.mStackScroller.getChildAt(i);
            if (child instanceof ExpandableNotificationRow) {
                if (HwExpandableNotificationRowHelper.isClearableWhenDeleteAllNotification((ExpandableNotificationRow) child) && child.getVisibility() == 0) {
                    viewsToHide.add(child);
                }
                ExpandableNotificationRow row = (ExpandableNotificationRow) child;
                List<ExpandableNotificationRow> children = row.getNotificationChildren();
                if (row.areChildrenExpanded() && children != null) {
                    for (ExpandableNotificationRow childRow : children) {
                        if (HwExpandableNotificationRowHelper.isClearableWhenDeleteAllNotification(childRow) && childRow.getVisibility() == 0) {
                            viewsToHide.add(childRow);
                        }
                    }
                }
            }
        }
        int allSize = (this.mNotificationData == null || this.mNotificationData.getActiveNotifications() == null) ? 0 : this.mNotificationData.getActiveNotifications().size();
        HwLog.i("PhoneStatusBar", "clearAllNotifications::all notification size=" + allSize + ", can clear notification size=" + viewsToHide.size());
        if (viewsToHide.isEmpty()) {
            animateCollapsePanels(0);
            return;
        }
        addPostCollapseAction(new Runnable() {
            public void run() {
                PhoneStatusBar.this.mStackScroller.setDismissAllInProgress(false);
                try {
                    PhoneStatusBar.this.mBarService.onClearAllNotifications(PhoneStatusBar.this.mCurrentUserId);
                } catch (Exception e) {
                }
            }
        });
        performDismissAllAnimations(viewsToHide);
    }

    private void performDismissAllAnimations(ArrayList<View> hideAnimatedList) {
        Runnable animationFinishAction = new Runnable() {
            public void run() {
                PhoneStatusBar.this.animateCollapsePanels(0);
            }
        };
        this.mStackScroller.setDismissAllInProgress(true);
        int currentDelay = 140;
        int totalDelay = 180;
        for (int i = hideAnimatedList.size() - 1; i >= 0; i--) {
            View view = (View) hideAnimatedList.get(i);
            Runnable endRunnable = null;
            if (i == 0) {
                endRunnable = animationFinishAction;
            }
            this.mStackScroller.dismissViewAnimated(view, endRunnable, totalDelay, 260);
            currentDelay = Math.max(50, currentDelay - 10);
            totalDelay += currentDelay;
        }
    }

    protected void setZenMode(int mode) {
        super.setZenMode(mode);
        if (this.mIconPolicy != null) {
            this.mIconPolicy.setZenMode(mode);
        }
    }

    protected void startKeyguard() {
        Log.i("PhoneStatusBar", "startKeyguard called");
        KeyguardViewMediator keyguardViewMediator = (KeyguardViewMediator) getComponent(HwKeyguardViewMediator.class);
        this.mFingerprintUnlockController = new HwFingerprintUnlockController(this.mContext, this.mStatusBarWindowManager, this.mDozeScrimController, keyguardViewMediator, this.mScrimController, this);
        this.mStatusBarKeyguardViewManager = keyguardViewMediator.registerStatusBar(this, getBouncerContainer(), this.mStatusBarWindowManager, this.mScrimController, this.mFingerprintUnlockController);
        if (this.mKeyguardIndicationController != null) {
            this.mKeyguardIndicationController.setStatusBarKeyguardViewManager(this.mStatusBarKeyguardViewManager);
        }
        this.mFingerprintUnlockController.setStatusBarKeyguardViewManager(this.mStatusBarKeyguardViewManager);
        this.mIconPolicy.setStatusBarKeyguardViewManager(this.mStatusBarKeyguardViewManager);
        this.mRemoteInputController.addCallback(this.mStatusBarKeyguardViewManager);
        this.mRemoteInputController.addCallback(new RemoteInputController.Callback() {
            public void onRemoteInputSent(Entry entry) {
                if (PhoneStatusBar.FORCE_REMOTE_INPUT_HISTORY && PhoneStatusBar.this.mKeysKeptForRemoteInput.contains(entry.key)) {
                    PhoneStatusBar.this.removeNotification(entry.key, null);
                } else if (PhoneStatusBar.this.mRemoteInputEntriesToRemoveOnCollapse.contains(entry)) {
                    PhoneStatusBar.this.mHandler.postDelayed(new PhoneStatusBar$30$-void_onRemoteInputSent_com_android_systemui_statusbar_NotificationData$Entry_entry_LambdaImpl0(this, entry), 200);
                }
            }

            /* synthetic */ void -com_android_systemui_statusbar_phone_PhoneStatusBar$30_lambda$1(Entry entry) {
                if (PhoneStatusBar.this.mRemoteInputEntriesToRemoveOnCollapse.remove(entry)) {
                    PhoneStatusBar.this.removeNotification(entry.key, null);
                }
            }
        });
        this.mKeyguardViewMediatorCallback = keyguardViewMediator.getViewMediatorCallback();
        this.mLightStatusBarController.setFingerprintUnlockController(this.mFingerprintUnlockController);
    }

    protected View getStatusBarView() {
        return this.mStatusBarView;
    }

    public StatusBarWindowView getStatusBarWindow() {
        return this.mStatusBarWindow;
    }

    protected ViewGroup getBouncerContainer() {
        return this.mStatusBarWindow;
    }

    public int getStatusBarHeight() {
        if (this.mNaturalBarHeight < 0) {
            this.mNaturalBarHeight = this.mContext.getResources().getDimensionPixelSize(17104919);
        }
        return this.mNaturalBarHeight;
    }

    public void toggleSplitScreenMode(int metricsDockAction, int metricsUndockAction) {
        if (this.mRecents != null && !"factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            if (WindowManagerProxy.getInstance().getDockSide() == -1) {
                this.mRecents.dockTopTask(-1, 0, null, metricsDockAction);
            } else {
                EventBus.getDefault().send(new UndockingTaskEvent());
                if (metricsUndockAction != -1) {
                    MetricsLogger.action(this.mContext, metricsUndockAction);
                }
            }
        }
    }

    public void awakenDreams() {
        if (this.mDreamManager != null) {
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                public boolean runInThread() {
                    try {
                        PhoneStatusBar.this.mDreamManager.awaken();
                    } catch (RemoteException e) {
                    }
                    return false;
                }
            });
        }
    }

    protected void prepareNavigationBarView() {
        this.mNavigationBarView.reorient();
        ButtonDispatcher recentsButton = this.mNavigationBarView.getRecentsButton();
        recentsButton.setOnClickListener(this.mRecentsClickListener);
        recentsButton.setOnTouchListener(this.mRecentsPreloadOnTouchListener);
        recentsButton.setLongClickable(true);
        recentsButton.setOnLongClickListener(this.mRecentsLongClickListener);
        ButtonDispatcher backButton = this.mNavigationBarView.getBackButton();
        backButton.setLongClickable(true);
        backButton.setOnLongClickListener(this.mLongPressBackListener);
        ButtonDispatcher homeButton = this.mNavigationBarView.getHomeButton();
        homeButton.setOnTouchListener(this.mHomeActionListener);
        homeButton.setOnLongClickListener(this.mLongPressHomeListener);
        this.mAssistManager.onConfigurationChanged();
    }

    protected void addNavigationBar() {
        if (this.mNavigationBarView != null) {
            prepareNavigationBarView();
            this.mWindowManager.addView(this.mNavigationBarView, getNavigationBarLayoutParams());
        }
    }

    protected void repositionNavigationBar() {
        if (this.mNavigationBarView != null && this.mNavigationBarView.isAttachedToWindow()) {
            prepareNavigationBarView();
            this.mWindowManager.updateViewLayout(this.mNavigationBarView, getNavigationBarLayoutParams());
        }
    }

    private void notifyNavigationBarScreenOn(boolean screenOn) {
        if (this.mNavigationBarView != null) {
            this.mNavigationBarView.notifyScreenOn(screenOn);
        }
    }

    private LayoutParams getNavigationBarLayoutParams() {
        LayoutParams lp = new LayoutParams(-1, -1, 2019, 8650856, -3);
        if (ActivityManager.isHighEndGfx()) {
            lp.flags |= 16777216;
        }
        lp.setTitle("NavigationBar");
        lp.windowAnimations = 0;
        return lp;
    }

    public void setIcon(String slot, StatusBarIcon icon) {
        this.mIconController.setIcon(slot, icon);
    }

    public void removeIcon(String slot) {
        this.mIconController.removeIcon(slot);
    }

    public void addNotification(StatusBarNotification notification, RankingMap ranking, Entry oldEntry) {
        HwLog.i("PhoneStatusBar", "addNotification key=" + notification.getKey());
        this.mNotificationData.updateRanking(ranking);
        Entry shadeEntry = createNotificationViews(notification);
        if (shadeEntry == null) {
            HwLog.i("PhoneStatusBar", "addNotification ,hadeEntry == null");
            return;
        }
        boolean isHeadsUped = shouldPeek(shadeEntry);
        if (isHeadsUped) {
            this.mHeadsUpManager.showNotification(shadeEntry);
            setNotificationShown(notification);
        }
        if (!(isHeadsUped || notification.getNotification().fullScreenIntent == null)) {
            if (shouldSuppressFullScreenIntent(notification.getKey())) {
                Log.d("PhoneStatusBar", "No Fullscreen intent: suppressed by DND: " + notification.getKey());
            } else if (this.mNotificationData.getImportance(notification.getKey()) < 5) {
                Log.d("PhoneStatusBar", "No Fullscreen intent: not important enough: " + notification.getKey());
            } else {
                awakenDreams();
                Log.d("PhoneStatusBar", "Notification has fullScreenIntent; sending fullScreenIntent " + notification.getKey());
                try {
                    EventLog.writeEvent(36002, notification.getKey());
                    notification.getNotification().fullScreenIntent.send();
                    shadeEntry.notifyFullScreenIntentLaunched();
                    MetricsLogger.count(this.mContext, "note_fullscreen", 1);
                } catch (CanceledException e) {
                    HwLog.i("PhoneStatusBar", "send " + notification.getKey() + " failed, " + e.getMessage());
                }
            }
        }
        addNotificationViews(shadeEntry, ranking);
        setAreThereNotifications();
    }

    private boolean shouldSuppressFullScreenIntent(String key) {
        if (isDeviceInVrMode()) {
            return true;
        }
        if (this.mPowerManager.isInteractive()) {
            return this.mNotificationData.shouldSuppressScreenOn(key);
        }
        return this.mNotificationData.shouldSuppressScreenOff(key);
    }

    protected void updateNotificationRanking(RankingMap ranking) {
        this.mNotificationData.updateRanking(ranking);
        updateNotifications();
    }

    public void removeNotification(String key, RankingMap ranking) {
        HwLog.i("PhoneStatusBar", "removeNotification:" + key);
        boolean deferRemoval = false;
        if (this.mHeadsUpManager.isHeadsUp(key)) {
            boolean ignoreEarliestRemovalTime = this.mRemoteInputController.isSpinning(key) ? !FORCE_REMOTE_INPUT_HISTORY : false;
            deferRemoval = !this.mHeadsUpManager.removeNotification(key, ignoreEarliestRemovalTime);
        }
        if (key.equals(this.mMediaNotificationKey)) {
            clearCurrentMediaNotification();
            updateMediaMetaData(true, true);
        }
        Entry entry;
        if (FORCE_REMOTE_INPUT_HISTORY && this.mRemoteInputController.isSpinning(key)) {
            CharSequence[] newHistory;
            entry = this.mNotificationData.get(key);
            StatusBarNotification sbn = entry.notification;
            Notification.Builder b = Notification.Builder.recoverBuilder(this.mContext, sbn.getNotification().clone());
            CharSequence[] oldHistory = sbn.getNotification().extras.getCharSequenceArray("android.remoteInputHistory");
            if (oldHistory == null) {
                newHistory = new CharSequence[1];
            } else {
                newHistory = new CharSequence[(oldHistory.length + 1)];
                for (int i = 0; i < oldHistory.length; i++) {
                    newHistory[i + 1] = oldHistory[i];
                }
            }
            newHistory[0] = String.valueOf(entry.remoteInputText);
            b.setRemoteInputHistory(newHistory);
            Notification newNotification = b.build();
            newNotification.contentView = sbn.getNotification().contentView;
            newNotification.bigContentView = sbn.getNotification().bigContentView;
            newNotification.headsUpContentView = sbn.getNotification().headsUpContentView;
            updateNotification(new StatusBarNotification(sbn.getPackageName(), sbn.getOpPkg(), sbn.getId(), sbn.getTag(), sbn.getUid(), sbn.getInitialPid(), 0, newNotification, sbn.getUser(), sbn.getPostTime()), null);
            this.mKeysKeptForRemoteInput.add(entry.key);
            HwLog.i("PhoneStatusBar", "add to mKeysKeptForRemoteInput: " + key);
        } else if (deferRemoval) {
            this.mLatestRankingMap = ranking;
            this.mHeadsUpEntriesToRemoveOnSwitch.add(this.mHeadsUpManager.getEntry(key));
            HwLog.i("PhoneStatusBar", "add to mHeadsUpEntriesToRemoveOnSwitch: " + key);
        } else {
            entry = this.mNotificationData.get(key);
            if (entry == null || !this.mRemoteInputController.isRemoteInputActive(entry)) {
                if (!(entry == null || entry.row == null)) {
                    entry.row.setRemoved();
                }
                handleGroupSummaryRemoved(key, ranking);
                if (!(removeNotificationViews(key, ranking) == null || hasActiveNotifications() || this.mNotificationPanel.isTracking() || this.mNotificationPanel.isQsExpanded())) {
                    if (this.mState == 0) {
                        animateCollapsePanels();
                    } else if (this.mState == 2) {
                        goToKeyguard();
                    }
                }
                setAreThereNotifications();
                return;
            }
            this.mLatestRankingMap = ranking;
            this.mRemoteInputEntriesToRemoveOnCollapse.add(entry);
            HwLog.i("PhoneStatusBar", "add to mRemoteInputEntriesToRemoveOnCollapse: " + key);
        }
    }

    private void handleGroupSummaryRemoved(String key, RankingMap ranking) {
        Entry entry = this.mNotificationData.get(key);
        if (entry != null && entry.row != null && entry.row.isSummaryWithChildren() && (entry.notification.getOverrideGroupKey() == null || entry.row.isDismissed())) {
            List<ExpandableNotificationRow> notificationChildren = entry.row.getNotificationChildren();
            if (notificationChildren != null) {
                int i;
                ArrayList<ExpandableNotificationRow> toRemove = new ArrayList(notificationChildren);
                for (i = 0; i < toRemove.size(); i++) {
                    ((ExpandableNotificationRow) toRemove.get(i)).setKeepInParent(true);
                    ((ExpandableNotificationRow) toRemove.get(i)).setRemoved();
                }
                for (i = 0; i < toRemove.size(); i++) {
                    removeNotification(((ExpandableNotificationRow) toRemove.get(i)).getStatusBarNotification().getKey(), ranking);
                    this.mStackScroller.removeViewStateForView((View) toRemove.get(i));
                }
            }
        }
    }

    protected void performRemoveNotification(StatusBarNotification n, boolean removeView) {
        Entry entry = this.mNotificationData.get(n.getKey());
        if (this.mRemoteInputController.isRemoteInputActive(entry)) {
            this.mRemoteInputController.removeRemoteInput(entry);
        }
        super.performRemoveNotification(n, removeView);
    }

    protected void refreshLayout(int layoutDirection) {
        if (this.mNavigationBarView != null) {
            this.mNavigationBarView.setLayoutDirection(layoutDirection);
        }
    }

    protected void updateNotificationShade() {
        Log.i("PhoneStatusBar", "updateNotificationShade: total=" + this.mNotificationData.getTotalCount() + ", active=" + this.mNotificationData.getActiveCount());
        if (this.mStackScroller != null) {
            if (isCollapsing()) {
                addPostCollapseAction(new Runnable() {
                    public void run() {
                        PhoneStatusBar.this.updateNotificationShade();
                    }
                });
                return;
            }
            int i;
            View child;
            ArrayList<Entry> activeNotifications = this.mNotificationData.getActiveNotifications();
            ArrayList<ExpandableNotificationRow> arrayList = new ArrayList(activeNotifications.size());
            int N = activeNotifications.size();
            for (i = 0; i < N; i++) {
                Entry ent = (Entry) activeNotifications.get(i);
                int vis = ent.notification.getNotification().visibility;
                boolean hideSensitive = !userAllowsPrivateNotificationsInPublic(ent.notification.getUserId());
                boolean sensitive = ((vis == 0) && hideSensitive) ? true : packageHasVisibilityOverride(ent.notification.getKey());
                boolean showingPublic = sensitive ? isLockscreenPublicMode() : false;
                if (showingPublic) {
                    updatePublicContentView(ent, ent.notification);
                }
                ent.row.setSensitive(sensitive, hideSensitive);
                if (ent.autoRedacted && ent.legacy) {
                    if (showingPublic) {
                        ent.row.setShowingLegacyBackground(false);
                    } else {
                        ent.row.setShowingLegacyBackground(true);
                    }
                }
                if (this.mGroupManager.isChildInGroupWithSummary(ent.row.getStatusBarNotification())) {
                    ExpandableNotificationRow summary = this.mGroupManager.getGroupSummary(ent.row.getStatusBarNotification());
                    List<ExpandableNotificationRow> orderedChildren = (List) this.mTmpChildOrderMap.get(summary);
                    if (orderedChildren == null) {
                        orderedChildren = new ArrayList();
                        this.mTmpChildOrderMap.put(summary, orderedChildren);
                    }
                    orderedChildren.add(ent.row);
                } else {
                    arrayList.add(ent.row);
                }
            }
            ArrayList<ExpandableNotificationRow> toRemove = new ArrayList();
            for (i = 0; i < this.mStackScroller.getChildCount(); i++) {
                child = this.mStackScroller.getChildAt(i);
                if (!arrayList.contains(child) && (child instanceof ExpandableNotificationRow)) {
                    toRemove.add((ExpandableNotificationRow) child);
                }
            }
            for (ExpandableNotificationRow remove : toRemove) {
                if (this.mGroupManager.isChildInGroupWithSummary(remove.getStatusBarNotification())) {
                    this.mStackScroller.setChildTransferInProgress(true);
                }
                if (remove.isSummaryWithChildren()) {
                    remove.removeAllChildren();
                }
                this.mStackScroller.removeView(remove);
                this.mStackScroller.setChildTransferInProgress(false);
            }
            removeNotificationChildren();
            for (i = 0; i < arrayList.size(); i++) {
                View v = (View) arrayList.get(i);
                if (v.getParent() == null) {
                    this.mStackScroller.addView(v);
                }
            }
            addNotificationChildrenAndSort();
            int j = 0;
            for (i = 0; i < this.mStackScroller.getChildCount(); i++) {
                child = this.mStackScroller.getChildAt(i);
                if (child instanceof ExpandableNotificationRow) {
                    if (j < arrayList.size()) {
                        View targetChild = (ExpandableNotificationRow) arrayList.get(j);
                        if (child != targetChild) {
                            this.mStackScroller.changeViewPosition(targetChild, i);
                        }
                    }
                    j++;
                }
            }
            this.mTmpChildOrderMap.clear();
            updateRowStates();
            updateSpeedbump();
            updateClearAll();
            updateEmptyShadeView();
            updateQsExpansionEnabled();
            this.mShadeUpdates.check();
        }
    }

    private void updateQsExpansionEnabled() {
        boolean z = false;
        NotificationPanelView notificationPanelView = this.mNotificationPanel;
        if (isDeviceProvisioned()) {
            if (this.mUserSetup || this.mUserSwitcherController == null || !this.mUserSwitcherController.isSimpleUserSwitcher()) {
                if ((this.mDisabled2 & 1) == 0 && !ONLY_CORE_APPS) {
                    z = true;
                }
            }
        }
        notificationPanelView.setQsExpansionEnabled(z);
    }

    private void addNotificationChildrenAndSort() {
        boolean orderChanged = false;
        for (int i = 0; i < this.mStackScroller.getChildCount(); i++) {
            View view = this.mStackScroller.getChildAt(i);
            if (view instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow parent = (ExpandableNotificationRow) view;
                List<ExpandableNotificationRow> children = parent.getNotificationChildren();
                List<ExpandableNotificationRow> orderedChildren = (List) this.mTmpChildOrderMap.get(parent);
                int childIndex = 0;
                while (orderedChildren != null && childIndex < orderedChildren.size()) {
                    ExpandableNotificationRow childView = (ExpandableNotificationRow) orderedChildren.get(childIndex);
                    if (children == null || !children.contains(childView)) {
                        parent.addChildNotification(childView, childIndex);
                        this.mStackScroller.notifyGroupChildAdded(childView);
                    }
                    childIndex++;
                }
                parent.afterAddNotification();
                orderChanged |= parent.applyChildOrder(orderedChildren);
            }
        }
        if (orderChanged) {
            this.mStackScroller.generateChildOrderChangedEvent();
        }
    }

    private void removeNotificationChildren() {
        HwLog.i("PhoneStatusBar", "removeNotificationChildren");
        ArrayList<ExpandableNotificationRow> toRemove = new ArrayList();
        for (int i = 0; i < this.mStackScroller.getChildCount(); i++) {
            View view = this.mStackScroller.getChildAt(i);
            if (view instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow parent = (ExpandableNotificationRow) view;
                List<ExpandableNotificationRow> children = parent.getNotificationChildren();
                List<ExpandableNotificationRow> orderedChildren = (List) this.mTmpChildOrderMap.get(parent);
                if (children != null) {
                    toRemove.clear();
                    for (ExpandableNotificationRow childRow : children) {
                        if ((orderedChildren == null || !orderedChildren.contains(childRow)) && !childRow.keepInParent()) {
                            toRemove.add(childRow);
                        }
                    }
                    for (ExpandableNotificationRow remove : toRemove) {
                        parent.removeChildNotification(remove);
                        if (this.mNotificationData.get(remove.getStatusBarNotification().getKey()) == null) {
                            this.mStackScroller.notifyGroupChildRemoved(remove, parent.getChildrenContainer());
                        }
                    }
                }
            }
        }
    }

    public void addQsTile(ComponentName tile) {
        this.mQSPanel.getHost().addTile(tile);
    }

    public void remQsTile(ComponentName tile) {
        this.mQSPanel.getHost().removeTile(tile);
    }

    public void clickTile(ComponentName tile) {
        this.mQSPanel.clickTile(tile);
    }

    private boolean packageHasVisibilityOverride(String key) {
        return this.mNotificationData.getVisibilityOverride(key) == 0;
    }

    protected void updateClearAll() {
        boolean hasActiveClearableNotifications;
        if (this.mState != 1) {
            hasActiveClearableNotifications = this.mNotificationData.hasActiveClearableNotifications();
        } else {
            hasActiveClearableNotifications = false;
        }
        this.mStackScroller.updateDismissView(hasActiveClearableNotifications);
    }

    private void updateEmptyShadeView() {
        boolean showEmptyShade = this.mState != 1 ? this.mNotificationData.getActiveNotifications().size() == 0 : false;
        this.mNotificationPanel.setShadeEmpty(showEmptyShade);
    }

    private void updateSpeedbump() {
        int speedbumpIndex = -1;
        int currentIndex = 0;
        int N = this.mStackScroller.getChildCount();
        for (int i = 0; i < N; i++) {
            View view = this.mStackScroller.getChildAt(i);
            if (view.getVisibility() != 8 && (view instanceof ExpandableNotificationRow)) {
                if (this.mNotificationData.isAmbient(((ExpandableNotificationRow) view).getStatusBarNotification().getKey())) {
                    speedbumpIndex = currentIndex;
                    break;
                }
                currentIndex++;
            }
        }
        this.mStackScroller.updateSpeedBumpIndex(speedbumpIndex);
    }

    public static boolean isTopLevelChild(Entry entry) {
        return entry.row.getParent() instanceof NotificationStackScrollLayout;
    }

    protected void updateNotifications() {
        this.mNotificationData.filterAndSort();
        updateNotificationShade();
        this.mIconController.updateNotificationIcons(this.mNotificationData);
    }

    public void requestNotificationUpdate() {
        updateNotifications();
    }

    protected void setAreThereNotifications() {
        boolean z;
        int i = 1;
        final View nlo = this.mStatusBarView.findViewById(R.id.notification_lights_out);
        boolean showDot = hasActiveNotifications() && !areLightsOn();
        if (nlo.getAlpha() == 1.0f) {
            z = true;
        } else {
            z = false;
        }
        if (showDot != z) {
            if (showDot) {
                nlo.setAlpha(0.0f);
                nlo.setVisibility(0);
            }
            ViewPropertyAnimator animate = nlo.animate();
            if (!showDot) {
                i = 0;
            }
            animate.alpha((float) i).setDuration((long) (showDot ? 750 : 250)).setInterpolator(new AccelerateInterpolator(2.0f)).setListener(showDot ? null : new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator _a) {
                    nlo.setVisibility(8);
                }
            }).start();
        }
        findAndUpdateMediaNotifications();
    }

    public void findAndUpdateMediaNotifications() {
        boolean metaDataChanged = false;
        synchronized (this.mNotificationData) {
            int i;
            MediaController aController;
            ArrayList<Entry> activeNotifications = this.mNotificationData.getActiveNotifications();
            int N = activeNotifications.size();
            Entry entry = null;
            MediaController controller = null;
            for (i = 0; i < N; i++) {
                Entry entry2 = (Entry) activeNotifications.get(i);
                if (isMediaNotification(entry2)) {
                    Token token = (Token) entry2.notification.getNotification().extras.getParcelable("android.mediaSession");
                    if (token != null) {
                        aController = new MediaController(this.mContext, token);
                        if (3 == getMediaControllerPlaybackState(aController)) {
                            entry = entry2;
                            controller = aController;
                            break;
                        }
                    } else {
                        continue;
                    }
                }
            }
            if (entry == null && this.mMediaSessionManager != null) {
                for (MediaController aController2 : this.mMediaSessionManager.getActiveSessionsForUser(null, -1)) {
                    if (3 == getMediaControllerPlaybackState(aController2)) {
                        String pkg = aController2.getPackageName();
                        for (i = 0; i < N; i++) {
                            entry2 = (Entry) activeNotifications.get(i);
                            if (entry2.notification.getPackageName().equals(pkg)) {
                                controller = aController2;
                                entry = entry2;
                                break;
                            }
                        }
                    }
                }
            }
            if (controller != null) {
                if (!sameSessions(this.mMediaController, controller)) {
                    clearCurrentMediaNotification();
                    this.mMediaController = controller;
                    this.mMediaController.registerCallback(this.mMediaListener);
                    this.mMediaMetadata = this.mMediaController.getMetadata();
                    if (entry != null) {
                        this.mMediaNotificationKey = entry.notification.getKey();
                    }
                    metaDataChanged = true;
                }
            }
        }
        if (metaDataChanged) {
            updateNotifications();
        }
        updateMediaMetaData(metaDataChanged, true);
    }

    private int getMediaControllerPlaybackState(MediaController controller) {
        if (controller != null) {
            PlaybackState playbackState = controller.getPlaybackState();
            if (playbackState != null) {
                return playbackState.getState();
            }
        }
        return 0;
    }

    private boolean isPlaybackActive(int state) {
        return (state == 1 || state == 7 || state == 0) ? false : true;
    }

    private void clearCurrentMediaNotification() {
        this.mMediaNotificationKey = null;
        this.mMediaMetadata = null;
        if (this.mMediaController != null) {
            this.mMediaController.unregisterCallback(this.mMediaListener);
        }
        this.mMediaController = null;
    }

    private boolean sameSessions(MediaController a, MediaController b) {
        if (a == b) {
            return true;
        }
        if (a == null) {
            return false;
        }
        return a.controlsSameSession(b);
    }

    public void updateMediaMetaData(boolean metaDataChanged, boolean allowEnterAnimation) {
        if (this.mBackdrop != null) {
            if (this.mLaunchTransitionFadingAway) {
                this.mBackdrop.setVisibility(4);
                return;
            }
            Drawable drawable = null;
            if (this.mMediaMetadata != null) {
                Bitmap artworkBitmap = this.mMediaMetadata.getBitmap("android.media.metadata.ART");
                if (artworkBitmap == null) {
                    artworkBitmap = this.mMediaMetadata.getBitmap("android.media.metadata.ALBUM_ART");
                }
                if (artworkBitmap != null) {
                    drawable = new BitmapDrawable(this.mBackdropBack.getResources(), artworkBitmap);
                }
            }
            boolean allowWhenShade = false;
            if (drawable == null) {
                Bitmap lockWallpaper = this.mLockscreenWallpaper.getBitmap();
                if (lockWallpaper != null) {
                    drawable = new WallpaperDrawable(this.mBackdropBack.getResources(), lockWallpaper);
                    allowWhenShade = this.mStatusBarKeyguardViewManager != null ? this.mStatusBarKeyguardViewManager.isShowing() : false;
                }
            }
            boolean isOccluded;
            if (this.mStatusBarKeyguardViewManager != null) {
                isOccluded = this.mStatusBarKeyguardViewManager.isOccluded();
            } else {
                isOccluded = false;
            }
            if ((drawable != null) && !((this.mState == 0 && !r0) || this.mFingerprintUnlockController.getMode() == 2 || r5)) {
                if (this.mBackdrop.getVisibility() != 0) {
                    this.mBackdrop.setVisibility(0);
                    if (allowEnterAnimation) {
                        this.mBackdrop.animate().alpha(1.0f).withEndAction(new Runnable() {
                            public void run() {
                                PhoneStatusBar.this.mStatusBarWindowManager.setBackdropShowing(true);
                            }
                        });
                    } else {
                        this.mBackdrop.animate().cancel();
                        this.mBackdrop.setAlpha(1.0f);
                        this.mStatusBarWindowManager.setBackdropShowing(true);
                    }
                    metaDataChanged = true;
                }
                if (metaDataChanged) {
                    if (this.mBackdropBack.getDrawable() != null) {
                        this.mBackdropFront.setImageDrawable(this.mBackdropBack.getDrawable().getConstantState().newDrawable(this.mBackdropFront.getResources()).mutate());
                        if (this.mScrimSrcModeEnabled) {
                            this.mBackdropFront.getDrawable().mutate().setXfermode(this.mSrcOverXferMode);
                        }
                        this.mBackdropFront.setAlpha(1.0f);
                        this.mBackdropFront.setVisibility(0);
                    } else {
                        this.mBackdropFront.setVisibility(4);
                    }
                    this.mBackdropBack.setImageDrawable(drawable);
                    if (this.mScrimSrcModeEnabled) {
                        this.mBackdropBack.getDrawable().mutate().setXfermode(this.mSrcXferMode);
                    }
                    if (this.mBackdropFront.getVisibility() == 0) {
                        this.mBackdropFront.animate().setDuration(250).alpha(0.0f).withEndAction(this.mHideBackdropFront);
                    }
                }
            } else if (this.mBackdrop.getVisibility() != 8) {
                if (this.mFingerprintUnlockController.getMode() == 2 || r5) {
                    this.mBackdrop.setVisibility(8);
                    this.mBackdropBack.setImageDrawable(null);
                    this.mStatusBarWindowManager.setBackdropShowing(false);
                } else {
                    this.mStatusBarWindowManager.setBackdropShowing(false);
                    this.mBackdrop.animate().alpha(0.002f).setInterpolator(Interpolators.ACCELERATE_DECELERATE).setDuration(300).setStartDelay(0).withEndAction(new Runnable() {
                        public void run() {
                            PhoneStatusBar.this.mBackdrop.setVisibility(8);
                            PhoneStatusBar.this.mBackdropFront.animate().cancel();
                            PhoneStatusBar.this.mBackdropBack.setImageDrawable(null);
                            PhoneStatusBar.this.mHandler.post(PhoneStatusBar.this.mHideBackdropFront);
                        }
                    });
                    if (this.mKeyguardFadingAway) {
                        this.mBackdrop.animate().setDuration(this.mKeyguardFadingAwayDuration / 2).setStartDelay(this.mKeyguardFadingAwayDelay).setInterpolator(Interpolators.LINEAR).start();
                    }
                }
            }
        }
    }

    protected int adjustDisableFlags(int state) {
        if (this.mLaunchTransitionFadingAway || this.mKeyguardFadingAway) {
            return state;
        }
        if (this.mExpandedVisible || this.mBouncerShowing || this.mWaitingForKeyguardExit) {
            return (state | 131072) | 1048576;
        }
        return state;
    }

    public void disable(int state1, int state2, boolean animate) {
        String str;
        animate &= this.mStatusBarWindowState != 2 ? 1 : 0;
        this.mDisabledUnmodified1 = state1;
        this.mDisabledUnmodified2 = state2;
        state1 = adjustDisableFlags(state1);
        int diff1 = state1 ^ this.mDisabled1;
        this.mDisabled1 = state1;
        int diff2 = state2 ^ this.mDisabled2;
        this.mDisabled2 = state2;
        StringBuilder flagdbg = new StringBuilder();
        flagdbg.append("disable: < ");
        flagdbg.append((65536 & state1) != 0 ? "EXPAND" : "expand");
        flagdbg.append((65536 & diff1) != 0 ? "* " : " ");
        flagdbg.append((131072 & state1) != 0 ? "ICONS" : "icons");
        flagdbg.append((131072 & diff1) != 0 ? "* " : " ");
        flagdbg.append((262144 & state1) != 0 ? "ALERTS" : "alerts");
        flagdbg.append((262144 & diff1) != 0 ? "* " : " ");
        flagdbg.append((1048576 & state1) != 0 ? "SYSTEM_INFO" : "system_info");
        flagdbg.append((1048576 & diff1) != 0 ? "* " : " ");
        flagdbg.append((4194304 & state1) != 0 ? "BACK" : "back");
        flagdbg.append((4194304 & diff1) != 0 ? "* " : " ");
        flagdbg.append((2097152 & state1) != 0 ? "HOME" : "home");
        flagdbg.append((2097152 & diff1) != 0 ? "* " : " ");
        flagdbg.append((16777216 & state1) != 0 ? "RECENT" : "recent");
        flagdbg.append((16777216 & diff1) != 0 ? "* " : " ");
        flagdbg.append((8388608 & state1) != 0 ? "CLOCK" : "clock");
        flagdbg.append((8388608 & diff1) != 0 ? "* " : " ");
        flagdbg.append((Integer.MIN_VALUE & state1) != 0 ? "NAVIGATION_BAR_TRANSLUCENT" : "navigationbar");
        flagdbg.append((Integer.MIN_VALUE & diff1) != 0 ? "* " : " ");
        flagdbg.append((33554432 & state1) != 0 ? "SEARCH" : "search");
        flagdbg.append((33554432 & diff1) != 0 ? "* " : " ");
        if ((state2 & 1) != 0) {
            str = "QUICK_SETTINGS";
        } else {
            str = "quick_settings";
        }
        flagdbg.append(str);
        flagdbg.append((diff2 & 1) != 0 ? "* " : " ");
        flagdbg.append(">");
        Log.d("PhoneStatusBar", flagdbg.toString());
        if ((1048576 & diff1) != 0) {
            if ((1048576 & state1) != 0) {
                this.mIconController.hideSystemIconArea(animate);
            } else {
                this.mIconController.showSystemIconArea(animate);
            }
        }
        if ((8388608 & diff1) != 0) {
            this.mIconController.setClockVisibility((8388608 & state1) == 0);
        }
        if (!((65536 & diff1) == 0 || (65536 & state1) == 0)) {
            animateCollapsePanels();
        }
        if ((56623104 & diff1) != 0) {
            if (this.mNavigationBarView != null) {
                this.mNavigationBarView.setDisabledFlags(state1);
            }
            if ((16777216 & state1) != 0) {
                this.mHandler.removeMessages(1020);
                this.mHandler.sendEmptyMessage(1020);
            }
        }
        if ((Integer.MIN_VALUE & diff1) != 0) {
            if ((Integer.MIN_VALUE & state1) != 0) {
                requestUpdateNavigationBar(true);
            } else {
                requestUpdateNavigationBar(false);
            }
        }
        if ((131072 & diff1) != 0) {
            if ((131072 & state1) != 0) {
                this.mIconController.hideNotificationIconArea(animate);
            } else {
                this.mIconController.showNotificationIconArea(animate);
            }
        }
        if ((262144 & diff1) != 0) {
            this.mDisableNotificationAlerts = (262144 & state1) != 0;
            this.mHeadsUpObserver.onChange(true);
        }
        if ((diff2 & 1) != 0) {
            updateQsExpansionEnabled();
        }
    }

    protected H createHandler() {
        return new H();
    }

    public void startActivity(Intent intent, boolean dismissShade) {
        startActivityDismissingKeyguard(intent, false, dismissShade);
    }

    public void startActivity(Intent intent, boolean dismissShade, ActivityStarter.Callback callback) {
        startActivityDismissingKeyguard(intent, false, dismissShade, callback);
    }

    public void preventNextAnimation() {
        overrideActivityPendingAppTransition(true);
    }

    public void setQsExpanded(boolean expanded) {
        int i;
        this.mStatusBarWindowManager.setQsExpanded(expanded);
        View view = this.mKeyguardStatusView;
        if (expanded) {
            i = 4;
        } else {
            i = 0;
        }
        view.setImportantForAccessibility(i);
    }

    public boolean isGoingToNotificationShade() {
        return this.mLeaveOpenOnKeyguardHide;
    }

    public boolean isQsExpanded() {
        return this.mNotificationPanel.isQsExpanded();
    }

    public boolean isWakeUpComingFromTouch() {
        return this.mWakeUpComingFromTouch;
    }

    public boolean isFalsingThresholdNeeded() {
        return getBarState() == 1;
    }

    public boolean isDozing() {
        return this.mDozing;
    }

    public String getCurrentMediaNotificationKey() {
        return this.mMediaNotificationKey;
    }

    public boolean isScrimSrcModeEnabled() {
        return this.mScrimSrcModeEnabled;
    }

    public void onKeyguardViewManagerStatesUpdated() {
        logStateToEventlog();
    }

    public void onUnlockMethodStateChanged() {
        logStateToEventlog();
    }

    public void onHeadsUpPinnedModeChanged(boolean inPinnedMode) {
        if (inPinnedMode) {
            this.mStatusBarWindowManager.setHeadsUpShowing(true);
            this.mStatusBarWindowManager.setForceStatusBarVisible(true);
            if (this.mNotificationPanel.isFullyCollapsed()) {
                this.mNotificationPanel.requestLayout();
                this.mStatusBarWindowManager.setForceWindowCollapsed(true);
                this.mNotificationPanel.post(new Runnable() {
                    public void run() {
                        PhoneStatusBar.this.mStatusBarWindowManager.setForceWindowCollapsed(false);
                    }
                });
            }
        } else if (!this.mNotificationPanel.isFullyCollapsed() || this.mNotificationPanel.isTracking()) {
            this.mStatusBarWindowManager.setHeadsUpShowing(false);
        } else {
            this.mHeadsUpManager.setHeadsUpGoingAway(true);
            this.mStackScroller.runAfterAnimationFinished(new Runnable() {
                public void run() {
                    if (!PhoneStatusBar.this.mHeadsUpManager.hasPinnedHeadsUp()) {
                        PhoneStatusBar.this.mStatusBarWindowManager.setHeadsUpShowing(false);
                        PhoneStatusBar.this.mHeadsUpManager.setHeadsUpGoingAway(false);
                    }
                }
            });
        }
    }

    public void onHeadsUpPinned(ExpandableNotificationRow headsUp) {
        dismissVolumeDialog();
    }

    public void onHeadsUpUnPinned(ExpandableNotificationRow headsUp) {
    }

    public void onHeadsUpStateChanged(Entry entry, boolean isHeadsUp) {
        if (isHeadsUp || !this.mHeadsUpEntriesToRemoveOnSwitch.contains(entry)) {
            updateNotificationRanking(null);
            return;
        }
        removeNotification(entry.key, this.mLatestRankingMap);
        this.mHeadsUpEntriesToRemoveOnSwitch.remove(entry);
        if (this.mHeadsUpEntriesToRemoveOnSwitch.isEmpty()) {
            this.mLatestRankingMap = null;
        }
    }

    protected void updateHeadsUp(String key, Entry entry, boolean shouldPeek, boolean alertAgain) {
        if (isHeadsUp(key)) {
            if (shouldPeek) {
                this.mHeadsUpManager.updateNotification(entry, alertAgain);
            } else {
                this.mHeadsUpManager.removeNotification(key, false);
            }
        } else if (shouldPeek && alertAgain) {
            this.mHeadsUpManager.showNotification(entry);
        }
    }

    protected void setHeadsUpUser(int newUserId) {
        if (this.mHeadsUpManager != null) {
            this.mHeadsUpManager.setUser(newUserId);
        }
    }

    public boolean isHeadsUp(String key) {
        return this.mHeadsUpManager.isHeadsUp(key);
    }

    protected boolean isSnoozedPackage(StatusBarNotification sbn) {
        return this.mHeadsUpManager.isSnoozed(sbn.getPackageName());
    }

    public boolean isKeyguardCurrentlySecure() {
        return !this.mUnlockMethodCache.canSkipBouncer();
    }

    public void setPanelExpanded(boolean isExpanded) {
        this.mStatusBarWindowManager.setPanelExpanded(isExpanded);
        if (isExpanded && getBarState() != 1) {
            clearNotificationEffects();
        }
        if (!isExpanded) {
            removeRemoteInputEntriesKeptUntilCollapsed();
        }
    }

    private void removeRemoteInputEntriesKeptUntilCollapsed() {
        for (int i = 0; i < this.mRemoteInputEntriesToRemoveOnCollapse.size(); i++) {
            Entry entry = (Entry) this.mRemoteInputEntriesToRemoveOnCollapse.valueAt(i);
            this.mRemoteInputController.removeRemoteInput(entry);
            removeNotification(entry.key, this.mLatestRankingMap);
        }
        this.mRemoteInputEntriesToRemoveOnCollapse.clear();
    }

    public void onScreenTurnedOff() {
        this.mFalsingManager.onScreenOff();
    }

    public void maybeEscalateHeadsUp() {
        for (HeadsUpEntry entry : this.mHeadsUpManager.getAllEntries()) {
            StatusBarNotification sbn = entry.entry.notification;
            Notification notification = sbn.getNotification();
            if (notification.fullScreenIntent != null) {
                try {
                    EventLog.writeEvent(36003, sbn.getKey());
                    notification.fullScreenIntent.send();
                    entry.entry.notifyFullScreenIntentLaunched();
                } catch (CanceledException e) {
                }
            }
        }
        this.mHeadsUpManager.releaseAllImmediately();
    }

    boolean panelsEnabled() {
        return (this.mDisabled1 & 65536) == 0 && !ONLY_CORE_APPS;
    }

    void makeExpandedVisible(boolean force) {
        boolean z = false;
        if (force || (!this.mExpandedVisible && panelsEnabled())) {
            PerfDebugUtils.beginSystraceSection("PhoneStatusBar_makeExpandedVisible");
            this.mExpandedVisible = true;
            if (this.mNavigationBarView != null) {
                this.mNavigationBarView.setSlippery(true);
            }
            this.mStatusBarWindowManager.setPanelVisible(true);
            updateStatusBar();
            visibilityChanged(true);
            this.mWaitingForKeyguardExit = false;
            PerfDebugUtils.beginSystraceSection("PhoneStatusBar_makeExpandedVisible_call_disable");
            int i = this.mDisabledUnmodified1;
            int i2 = this.mDisabledUnmodified2;
            if (!force) {
                z = true;
            }
            disable(i, i2, z);
            PerfDebugUtils.endSystraceSection();
            setInteracting(1, true);
            PerfDebugUtils.endSystraceSection();
            this.mIconController.updateNotificationIcons(this.mNotificationData);
        }
    }

    public void animateCollapsePanels() {
        animateCollapsePanels(0);
    }

    public void postAnimateCollapsePanels() {
        this.mHandler.post(this.mAnimateCollapsePanels);
    }

    public void postAnimateOpenPanels() {
        this.mHandler.sendEmptyMessage(1002);
    }

    public void animateCollapsePanels(int flags) {
        animateCollapsePanels(flags, false, false, 1.0f);
    }

    public void animateCollapsePanels(int flags, boolean force) {
        animateCollapsePanels(flags, force, false, 1.0f);
    }

    public void animateCollapsePanels(int flags, boolean force, boolean delayed) {
        animateCollapsePanels(flags, force, delayed, 1.0f);
    }

    public void animateCollapsePanels(int flags, boolean force, boolean delayed, float speedUpFactor) {
        HwLog.i("PhoneStatusBar", "animateCollapsePanels:flags=" + flags + ", force=" + force + ", delayed=" + delayed + ", mExpandedVisible=" + this.mExpandedVisible);
        if (force || this.mState == 0) {
            if ((flags & 2) == 0 && !this.mHandler.hasMessages(1020)) {
                this.mHandler.removeMessages(1020);
                this.mHandler.sendEmptyMessage(1020);
            }
            if (this.mStatusBarWindow != null) {
                this.mStatusBarWindowManager.setStatusBarFocusable(false);
                this.mStatusBarWindow.cancelExpandHelper();
                this.mStatusBarView.collapsePanel(true, delayed, speedUpFactor);
            }
            return;
        }
        runPostCollapseRunnables();
    }

    private void runPostCollapseRunnables() {
        ArrayList<Runnable> clonedList = new ArrayList(this.mPostCollapseRunnables);
        this.mPostCollapseRunnables.clear();
        int size = clonedList.size();
        for (int i = 0; i < size; i++) {
            ((Runnable) clonedList.get(i)).run();
        }
    }

    public void animateExpandNotificationsPanel() {
        Log.d("PhoneStatusBar", "animateExpand: mExpandedVisible=" + this.mExpandedVisible);
        if (!panelsEnabled()) {
            HwLog.i("PhoneStatusBar", "panel is disabled");
        } else if (this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
            HwLog.i("PhoneStatusBar", "animateExpandNotificationsPanel DISABLE_EXPAND because bouncerShowing!");
        } else {
            this.mNotificationPanel.expand(true);
        }
    }

    public void animateExpandSettingsPanel(String subPanel) {
        Log.d("PhoneStatusBar", "animateExpand: mExpandedVisible=" + this.mExpandedVisible);
        if (!panelsEnabled()) {
            HwLog.i("PhoneStatusBar", "panel is disabled");
        } else if (this.mUserSetup) {
            if (subPanel != null) {
                this.mQSPanel.openDetails(subPanel);
            }
            this.mNotificationPanel.expandWithQs();
        }
    }

    public void animateCollapseQuickSettings() {
        if (this.mState == 0) {
            this.mStatusBarView.collapsePanel(true, false, 1.0f);
        }
    }

    void makeExpandedInvisible() {
        Log.d("PhoneStatusBar", "makeExpandedInvisible: mExpandedVisible=" + this.mExpandedVisible);
        if (this.mExpandedVisible && this.mStatusBarWindow != null) {
            this.mStatusBarView.collapsePanel(false, false, 1.0f);
            this.mNotificationPanel.closeQs();
            this.mExpandedVisible = false;
            if (this.mNavigationBarView != null) {
                this.mNavigationBarView.setSlippery(false);
            }
            visibilityChanged(false);
            this.mStatusBarWindowManager.setPanelVisible(false);
            this.mStatusBarWindowManager.setForceStatusBarVisible(false);
            updateStatusBar();
            dismissPopups();
            runPostCollapseRunnables();
            setInteracting(1, false);
            showBouncer();
            disable(this.mDisabledUnmodified1, this.mDisabledUnmodified2, true);
            if (!this.mStatusBarKeyguardViewManager.isShowing()) {
                WindowManagerGlobal.getInstance().trimMemory(20);
            }
        }
    }

    public boolean interceptTouchEvent(MotionEvent event) {
        if (this.mStatusBarWindowState == 0) {
            boolean upOrCancel = event.getAction() != 1 ? event.getAction() == 3 : true;
            if (!upOrCancel || this.mExpandedVisible) {
                setInteracting(1, true);
            } else {
                setInteracting(1, false);
            }
        }
        return false;
    }

    public GestureRecorder getGestureRecorder() {
        return this.mGestureRec;
    }

    private void setNavigationIconHints(int hints) {
        if (hints != this.mNavigationIconHints) {
            this.mNavigationIconHints = hints;
            if (this.mNavigationBarView != null) {
                this.mNavigationBarView.setNavigationIconHints(hints);
            }
            checkBarModes();
        }
    }

    public void setWindowState(int window, int state) {
        boolean showing = state == 0;
        if (!(this.mStatusBarWindow == null || window != 1 || this.mStatusBarWindowState == state)) {
            this.mStatusBarWindowState = state;
            if (!showing && this.mState == 0) {
                this.mStatusBarView.collapsePanel(false, false, 1.0f);
            }
        }
        if (this.mNavigationBarView != null && window == 2 && this.mNavigationBarWindowState != state) {
            this.mNavigationBarWindowState = state;
        }
    }

    public void buzzBeepBlinked() {
        if (this.mDozeServiceHost != null) {
            this.mDozeServiceHost.fireBuzzBeepBlinked();
        }
    }

    public void notificationLightOff() {
        if (this.mDozeServiceHost != null) {
            this.mDozeServiceHost.fireNotificationLight(false);
        }
    }

    public void notificationLightPulse(int argb, int onMillis, int offMillis) {
        if (this.mDozeServiceHost != null) {
            this.mDozeServiceHost.fireNotificationLight(true);
        }
    }

    private int computeBarMode(int oldVis, int newVis, BarTransitions transitions, int transientFlag, int translucentFlag, int transparentFlag) {
        int oldMode = barMode(oldVis, transientFlag, translucentFlag, transparentFlag);
        int newMode = barMode(newVis, transientFlag, translucentFlag, transparentFlag);
        if (oldMode == newMode) {
            return -1;
        }
        return newMode;
    }

    private int barMode(int vis, int transientFlag, int translucentFlag, int transparentFlag) {
        int lightsOutTransparent = transparentFlag | 1;
        if ((vis & transientFlag) != 0) {
            return 1;
        }
        if ((vis & translucentFlag) != 0) {
            return 2;
        }
        if ((vis & lightsOutTransparent) == lightsOutTransparent) {
            return 6;
        }
        if ((vis & transparentFlag) != 0) {
            return 4;
        }
        return (vis & 1) != 0 ? 3 : 0;
    }

    private void checkBarModes() {
        if (!this.mDemoMode) {
            checkBarMode(this.mStatusBarMode, this.mStatusBarWindowState, this.mStatusBarView.getBarTransitions(), this.mNoAnimationOnNextBarModeChange);
            if (this.mNavigationBarView != null) {
                checkBarMode(this.mNavigationBarMode, this.mNavigationBarWindowState, this.mNavigationBarView.getBarTransitions(), this.mNoAnimationOnNextBarModeChange);
            }
            this.mNoAnimationOnNextBarModeChange = false;
        }
    }

    private void checkBarMode(int mode, int windowState, BarTransitions transitions, boolean noAnimation) {
        boolean powerSave = this.mBatteryController.isPowerSave();
        if (!noAnimation && this.mDeviceInteractive && windowState != 2) {
            if (powerSave) {
            }
        }
        if (powerSave && getBarState() == 0) {
            mode = 5;
        }
        transitions.transitionTo(mode, false);
    }

    private void finishBarAnimations() {
        this.mStatusBarView.getBarTransitions().finishAnimations();
        if (this.mNavigationBarView != null) {
            this.mNavigationBarView.getBarTransitions().finishAnimations();
        }
    }

    private void dismissVolumeDialog() {
        if (this.mVolumeComponent != null) {
            this.mVolumeComponent.dismissNow();
        }
    }

    private void resumeSuspendedAutohide() {
        HwLog.i("PhoneStatusBar", "resumeSuspendedAutohide");
        if (this.mAutohideSuspended) {
            scheduleAutohide();
            this.mHandler.postDelayed(this.mCheckBarModes, 500);
        }
    }

    private void suspendAutohide() {
        boolean z = false;
        HwLog.i("PhoneStatusBar", "suspendAutohide");
        this.mHandler.removeCallbacks(this.mAutohide);
        this.mHandler.removeCallbacks(this.mCheckBarModes);
        if ((this.mSystemUiVisibility & 201326592) != 0) {
            z = true;
        }
        this.mAutohideSuspended = z;
    }

    protected void cancelAutohide() {
        HwLog.i("PhoneStatusBar", "cancelAutohide");
        this.mAutohideSuspended = false;
        this.mHandler.removeCallbacks(this.mAutohide);
    }

    protected void scheduleAutohide() {
        HwLog.i("PhoneStatusBar", "scheduleAutohide");
        cancelAutohide();
        this.mHandler.postDelayed(this.mAutohide, 3000);
    }

    private void checkUserAutohide(View v, MotionEvent event) {
        if ((this.mSystemUiVisibility & 201326592) != 0 && event.getAction() == 4 && event.getX() == 0.0f && event.getY() == 0.0f && !this.mRemoteInputController.isRemoteInputActive()) {
            userAutohide();
        }
    }

    private void userAutohide() {
        HwLog.i("PhoneStatusBar", "userAutohide");
        cancelAutohide();
        this.mHandler.postDelayed(this.mAutohide, 350);
    }

    private boolean areLightsOn() {
        return (this.mSystemUiVisibility & 1) == 0;
    }

    public void setLightsOn(boolean on) {
        Log.v("PhoneStatusBar", "setLightsOn(" + on + ")");
        if (on) {
            setSystemUiVisibility(0, 0, 0, 1, this.mLastFullscreenStackBounds, this.mLastDockedStackBounds);
            return;
        }
        setSystemUiVisibility(1, 0, 0, 1, this.mLastFullscreenStackBounds, this.mLastDockedStackBounds);
    }

    private void notifyUiVisibilityChanged(int vis) {
        try {
            HwLog.i("PhoneStatusBar", "notifyUiVisibilityChanged:vis=0x" + Integer.toHexString(vis) + ", SystemUiVisibility=0x" + Integer.toHexString(this.mSystemUiVisibility));
            this.mWindowManagerService.statusBarVisibilityChanged(vis);
            this.mLastDispatchedSystemUiVisibility = vis;
        } catch (RemoteException e) {
        }
    }

    public void topAppWindowChanged(boolean showMenu) {
        if (this.mNavigationBarView != null) {
            this.mNavigationBarView.setMenuVisibility(showMenu);
        }
        if (showMenu) {
            setLightsOn(true);
        }
    }

    public void setImeWindowStatus(IBinder token, int vis, int backDisposition, boolean showImeSwitcher) {
        boolean imeShown = (vis & 2) != 0;
        int flags = this.mNavigationIconHints;
        if (backDisposition == 2 || imeShown) {
            flags |= 1;
        } else {
            flags &= -2;
        }
        if (showImeSwitcher) {
            flags |= 2;
        } else {
            flags &= -3;
        }
        setNavigationIconHints(flags);
    }

    public static String viewInfo(View v) {
        return "[(" + v.getLeft() + "," + v.getTop() + ")(" + v.getRight() + "," + v.getBottom() + ") " + v.getWidth() + "x" + v.getHeight() + "]";
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (this.mQueueLock) {
            pw.println("Current Status Bar state:");
            pw.println("  mExpandedVisible=" + this.mExpandedVisible + ", mTrackingPosition=" + this.mTrackingPosition);
            pw.println("  mTracking=" + this.mTracking);
            pw.println("  mDisplayMetrics=" + this.mDisplayMetrics);
            pw.println("  mStackScroller: " + viewInfo(this.mStackScroller));
            pw.println("  mStackScroller: " + viewInfo(this.mStackScroller) + " scroll " + this.mStackScroller.getScrollX() + "," + this.mStackScroller.getScrollY());
        }
        pw.print("  mInteractingWindows=");
        pw.println(this.mInteractingWindows);
        pw.print("  mStatusBarWindowState=");
        pw.println(StatusBarManager.windowStateToString(this.mStatusBarWindowState));
        pw.print("  mStatusBarMode=");
        pw.println(BarTransitions.modeToString(this.mStatusBarMode));
        pw.print("  mDozing=");
        pw.println(this.mDozing);
        pw.print("  mZenMode=");
        pw.println(Global.zenModeToString(this.mZenMode));
        pw.print("  mUseHeadsUp=");
        pw.println(this.mUseHeadsUp);
        dumpBarTransitions(pw, "mStatusBarView", this.mStatusBarView.getBarTransitions());
        if (this.mNavigationBarView != null) {
            pw.print("  mNavigationBarWindowState=");
            pw.println(StatusBarManager.windowStateToString(this.mNavigationBarWindowState));
            pw.print("  mNavigationBarMode=");
            pw.println(BarTransitions.modeToString(this.mNavigationBarMode));
            dumpBarTransitions(pw, "mNavigationBarView", this.mNavigationBarView.getBarTransitions());
        }
        pw.print("  mNavigationBarView=");
        if (this.mNavigationBarView == null) {
            pw.println("null");
        } else {
            this.mNavigationBarView.dump(fd, pw, args);
        }
        pw.print("  mShowLockscreenNotifications=" + this.mShowLockscreenNotifications);
        pw.print("  mMediaSessionManager=");
        pw.println(this.mMediaSessionManager);
        pw.print("  mMediaNotificationKey=");
        pw.println(this.mMediaNotificationKey);
        pw.print("  mMediaController=");
        pw.print(this.mMediaController);
        if (this.mMediaController != null) {
            pw.print(" state=" + this.mMediaController.getPlaybackState());
        }
        pw.println();
        pw.print("  mMediaMetadata=");
        pw.print(this.mMediaMetadata);
        if (this.mMediaMetadata != null) {
            pw.print(" title=" + this.mMediaMetadata.getText("android.media.metadata.TITLE"));
        }
        pw.println();
        pw.println("  Panels: ");
        if (this.mNotificationPanel != null) {
            pw.println("    mNotificationPanel=" + this.mNotificationPanel + " params=" + this.mNotificationPanel.getLayoutParams().debug(BuildConfig.FLAVOR));
            pw.print("      ");
            this.mNotificationPanel.dump(fd, pw, args);
        }
        DozeLog.dump(pw);
        synchronized (this.mNotificationData) {
            this.mNotificationData.dump(pw, "  ");
        }
        this.mIconController.dump(pw);
        if (this.mStatusBarWindowManager != null) {
            this.mStatusBarWindowManager.dump(fd, pw, args);
        }
        if (this.mNetworkController != null) {
            this.mNetworkController.dump(fd, pw, args);
        }
        if (this.mBluetoothController != null) {
            this.mBluetoothController.dump(fd, pw, args);
        }
        if (this.mHotspotController != null) {
            this.mHotspotController.dump(fd, pw, args);
        }
        if (this.mCastController != null) {
            this.mCastController.dump(fd, pw, args);
        }
        if (this.mUserSwitcherController != null) {
            this.mUserSwitcherController.dump(fd, pw, args);
        }
        if (this.mBatteryController != null) {
            this.mBatteryController.dump(fd, pw, args);
        }
        if (this.mNextAlarmController != null) {
            this.mNextAlarmController.dump(fd, pw, args);
        }
        if (this.mSecurityController != null) {
            this.mSecurityController.dump(fd, pw, args);
        }
        if (this.mHeadsUpManager != null) {
            this.mHeadsUpManager.dump(fd, pw, args);
        } else {
            pw.println("  mHeadsUpManager: null");
        }
        if (this.mGroupManager != null) {
            this.mGroupManager.dump(fd, pw, args);
        } else {
            pw.println("  mGroupManager: null");
        }
        if (KeyguardUpdateMonitor.getInstance(this.mContext) != null) {
            KeyguardUpdateMonitor.getInstance(this.mContext).dump(fd, pw, args);
        }
        FalsingManager.getInstance(this.mContext).dump(pw);
        FalsingLog.dump(pw);
        pw.println("SharedPreferences:");
        for (Map.Entry<String, ?> entry : Prefs.getAll(this.mContext).entrySet()) {
            pw.print("  ");
            pw.print((String) entry.getKey());
            pw.print("=");
            pw.println(entry.getValue());
        }
        if (this.mNotificationPanel != null) {
            this.mNotificationPanel.dump(fd, pw, args);
        }
    }

    private static void dumpBarTransitions(PrintWriter pw, String var, BarTransitions transitions) {
        pw.print("  ");
        pw.print(var);
        pw.print(".BarTransitions.mMode=");
        pw.println(BarTransitions.modeToString(transitions.getMode()));
    }

    public void createAndAddWindows() {
        addStatusBarWindow();
    }

    private void addStatusBarWindow() {
        makeStatusBarView();
        this.mStatusBarWindowManager = new StatusBarWindowManager(this.mContext);
        this.mRemoteInputController = new RemoteInputController(this.mStatusBarWindowManager, this.mHeadsUpManager);
        this.mStatusBarWindowManager.add(this.mStatusBarWindow, getStatusBarHeight());
    }

    void updateDisplaySize() {
        this.mDisplay.getMetrics(this.mDisplayMetrics);
        this.mDisplay.getSize(this.mCurrentDisplaySize);
    }

    float getDisplayDensity() {
        return this.mDisplayMetrics.density;
    }

    public void startActivityDismissingKeyguard(Intent intent, boolean onlyProvisioned, boolean dismissShade) {
        startActivityDismissingKeyguard(intent, onlyProvisioned, dismissShade, null);
    }

    public void startActivityDismissingKeyguard(Intent intent, boolean onlyProvisioned, boolean dismissShade, final ActivityStarter.Callback callback) {
        if (!onlyProvisioned || isDeviceProvisioned()) {
            final boolean afterKeyguardGone = PreviewInflater.wouldLaunchResolverActivity(this.mContext, intent, this.mCurrentUserId);
            final boolean keyguardShowing = this.mStatusBarKeyguardViewManager.isShowing();
            final Intent intent2 = intent;
            final ActivityStarter.Callback callback2 = callback;
            executeRunnableDismissingKeyguard(new Runnable() {
                public void run() {
                    PhoneStatusBar.this.mAssistManager.hideAssist();
                    intent2.setFlags(805306368);
                    int result = -6;
                    try {
                        result = ActivityManagerNative.getDefault().startActivityAsUser(null, PhoneStatusBar.this.mContext.getBasePackageName(), intent2, intent2.resolveTypeIfNeeded(PhoneStatusBar.this.mContext.getContentResolver()), null, null, 0, 268435456, null, PhoneStatusBar.this.getActivityOptions(), UserHandle.CURRENT.getIdentifier());
                    } catch (RemoteException e) {
                        Log.w("PhoneStatusBar", "Unable to start activity", e);
                    }
                    PhoneStatusBar phoneStatusBar = PhoneStatusBar.this;
                    boolean z = keyguardShowing && !afterKeyguardGone;
                    phoneStatusBar.overrideActivityPendingAppTransition(z);
                    if (callback2 != null) {
                        callback2.onActivityStarted(result);
                    }
                }
            }, new Runnable() {
                public void run() {
                    if (callback != null) {
                        callback.onActivityStarted(-6);
                    }
                }
            }, dismissShade, afterKeyguardGone, true);
        }
    }

    public void executeRunnableDismissingKeyguard(Runnable runnable, Runnable cancelAction, boolean dismissShade, boolean afterKeyguardGone, boolean deferred) {
        final boolean keyguardShowing = this.mStatusBarKeyguardViewManager.isShowing();
        HwLog.i("PhoneStatusBar", "executeRunnableDismissingKeyguard::keyguardShowing=" + keyguardShowing + ", dismissShade=" + dismissShade + ", deferred=" + deferred + ", afterKeyguardGone=" + afterKeyguardGone);
        final boolean z = dismissShade;
        final boolean z2 = deferred;
        final boolean z3 = afterKeyguardGone;
        final Runnable runnable2 = runnable;
        dismissKeyguardThenExecute(new OnDismissAction() {
            public boolean onDismiss() {
                final boolean z = keyguardShowing;
                final boolean z2 = z3;
                final Runnable runnable = runnable2;
                AsyncTask.execute(new Runnable() {
                    public void run() {
                        try {
                            if (z && !z2) {
                                ActivityManagerNative.getDefault().keyguardWaitingForActivityDrawn();
                            }
                            if (runnable != null) {
                                runnable.run();
                            }
                        } catch (RemoteException e) {
                            HwLog.e("PhoneStatusBar", "executeRunnableDismissingKeyguard:: dismiss exception=" + e.getMessage());
                        }
                    }
                });
                if (z) {
                    PhoneStatusBar.this.animateCollapsePanels(2, true, true);
                }
                return z2;
            }
        }, cancelAction, afterKeyguardGone);
    }

    public void resetUserExpandedStates() {
        ArrayList<Entry> activeNotifications = this.mNotificationData.getActiveNotifications();
        int notificationCount = activeNotifications.size();
        for (int i = 0; i < notificationCount; i++) {
            Entry entry = (Entry) activeNotifications.get(i);
            if (entry.row != null) {
                entry.row.resetUserExpansion();
            }
        }
    }

    protected void dismissKeyguardThenExecute(OnDismissAction action, boolean afterKeyguardGone) {
        dismissKeyguardThenExecute(action, null, afterKeyguardGone);
    }

    public void dismissKeyguard() {
        this.mStatusBarKeyguardViewManager.dismiss();
    }

    private void dismissKeyguardThenExecute(OnDismissAction action, Runnable cancelAction, boolean afterKeyguardGone) {
        if (this.mStatusBarKeyguardViewManager.isShowing()) {
            this.mStatusBarKeyguardViewManager.dismissWithAction(action, cancelAction, afterKeyguardGone);
        } else {
            action.onDismiss();
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        updateResources();
        updateDisplaySize();
        super.onConfigurationChanged(newConfig);
        repositionNavigationBar();
        updateRowStates();
        this.mScreenPinningRequest.onConfigurationChanged();
        this.mNetworkController.onConfigurationChanged();
        this.mIconController.updateNotificationIcons(this.mNotificationData);
    }

    public void userSwitched(int newUserId) {
        super.userSwitched(newUserId);
        animateCollapsePanels();
        updatePublicMode();
        updateNotifications();
        resetUserSetupObserver();
        setControllerUsers();
        clearCurrentMediaNotification();
        this.mLockscreenWallpaper.setCurrentUser(newUserId);
        updateMediaMetaData(true, false);
    }

    private void setControllerUsers() {
        if (this.mZenModeController != null) {
            this.mZenModeController.setUserId(this.mCurrentUserId);
        }
        if (this.mSecurityController != null) {
            this.mSecurityController.onUserSwitched(this.mCurrentUserId);
        }
    }

    private void resetUserSetupObserver() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mUserSetupObserver);
        this.mUserSetupObserver.onChange(false);
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("user_setup_complete"), true, this.mUserSetupObserver, this.mCurrentUserId);
    }

    void updateResources() {
        if (this.mQSPanel != null) {
            this.mQSPanel.updateResources();
        }
        loadDimens();
        if (this.mNotificationPanel != null) {
            this.mNotificationPanel.updateResources();
        }
        if (this.mBrightnessMirrorController != null) {
            this.mBrightnessMirrorController.updateResources();
        }
        if (this.mNavigationBarView != null) {
            this.mNavigationBarView.updateResources();
        }
    }

    protected void loadDimens() {
        Resources res = this.mContext.getResources();
        int oldBarHeight = this.mNaturalBarHeight;
        this.mNaturalBarHeight = res.getDimensionPixelSize(17104919);
        if (!(this.mStatusBarWindowManager == null || this.mNaturalBarHeight == oldBarHeight)) {
            this.mStatusBarWindowManager.setBarHeight(this.mNaturalBarHeight);
        }
        this.mMaxAllowedKeyguardNotifications = res.getInteger(R.integer.keyguard_max_notification_count);
    }

    protected void handleVisibleToUserChanged(boolean visibleToUser) {
        if (visibleToUser) {
            super.handleVisibleToUserChanged(visibleToUser);
            startNotificationLogging();
            return;
        }
        stopNotificationLogging();
        super.handleVisibleToUserChanged(visibleToUser);
    }

    private void stopNotificationLogging() {
        if (!this.mCurrentlyVisibleNotifications.isEmpty()) {
            logNotificationVisibilityChanges(Collections.emptyList(), this.mCurrentlyVisibleNotifications);
            recycleAllVisibilityObjects(this.mCurrentlyVisibleNotifications);
        }
        this.mHandler.removeCallbacks(this.mVisibilityReporter);
        this.mStackScroller.setChildLocationsChangedListener(null);
    }

    private void startNotificationLogging() {
        this.mStackScroller.setChildLocationsChangedListener(this.mNotificationLocationsChangedListener);
        this.mNotificationLocationsChangedListener.onChildLocationsChanged(this.mStackScroller);
    }

    private void logNotificationVisibilityChanges(Collection<NotificationVisibility> newlyVisible, Collection<NotificationVisibility> noLongerVisible) {
        if (!newlyVisible.isEmpty() || !noLongerVisible.isEmpty()) {
            final NotificationVisibility[] newlyVisibleAr = (NotificationVisibility[]) newlyVisible.toArray(new NotificationVisibility[newlyVisible.size()]);
            final NotificationVisibility[] noLongerVisibleAr = (NotificationVisibility[]) noLongerVisible.toArray(new NotificationVisibility[noLongerVisible.size()]);
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                public boolean runInThread() {
                    HwLog.i("PhoneStatusBar", "logNotificationVisibilityChanges runInThread start");
                    try {
                        PhoneStatusBar.this.mBarService.onNotificationVisibilityChanged(newlyVisibleAr, noLongerVisibleAr);
                    } catch (RemoteException e) {
                    }
                    HwLog.i("PhoneStatusBar", "logNotificationVisibilityChanges runInThread over");
                    return false;
                }
            });
            int N = newlyVisible.size();
            if (N > 0) {
                String[] newlyVisibleKeyAr = new String[N];
                for (int i = 0; i < N; i++) {
                    newlyVisibleKeyAr[i] = newlyVisibleAr[i].key;
                }
                setNotificationsShown(newlyVisibleKeyAr);
            }
        }
    }

    private void logStateToEventlog() {
        int i = 1;
        boolean isShowing = this.mStatusBarKeyguardViewManager.isShowing();
        boolean isOccluded = this.mStatusBarKeyguardViewManager.isOccluded();
        boolean isBouncerShowing = this.mStatusBarKeyguardViewManager.isBouncerShowing();
        boolean isSecure = this.mUnlockMethodCache.isMethodSecure();
        boolean canSkipBouncer = this.mUnlockMethodCache.canSkipBouncer();
        int stateFingerprint = getLoggingFingerprint(this.mState, isShowing, isOccluded, isBouncerShowing, isSecure, canSkipBouncer);
        if (stateFingerprint != this.mLastLoggedStateFingerprint) {
            int i2;
            int i3;
            int i4;
            int i5 = this.mState;
            int i6 = isShowing ? 1 : 0;
            if (isOccluded) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            if (isBouncerShowing) {
                i3 = 1;
            } else {
                i3 = 0;
            }
            if (isSecure) {
                i4 = 1;
            } else {
                i4 = 0;
            }
            if (!canSkipBouncer) {
                i = 0;
            }
            EventLogTags.writeSysuiStatusBarState(i5, i6, i2, i3, i4, i);
            this.mLastLoggedStateFingerprint = stateFingerprint;
        }
    }

    private static int getLoggingFingerprint(int statusBarState, boolean keyguardShowing, boolean keyguardOccluded, boolean bouncerShowing, boolean secure, boolean currentlyInsecure) {
        int i;
        int i2 = 1;
        int i3 = statusBarState & 255;
        if (keyguardShowing) {
            i = 1;
        } else {
            i = 0;
        }
        i3 |= i << 8;
        if (keyguardOccluded) {
            i = 1;
        } else {
            i = 0;
        }
        i3 |= i << 9;
        if (bouncerShowing) {
            i = 1;
        } else {
            i = 0;
        }
        i3 |= i << 10;
        if (secure) {
            i = 1;
        } else {
            i = 0;
        }
        i = (i << 11) | i3;
        if (!currentlyInsecure) {
            i2 = 0;
        }
        return (i2 << 12) | i;
    }

    void vibrate() {
        ((Vibrator) this.mContext.getSystemService("vibrator")).vibrate(250, VIBRATION_ATTRIBUTES);
    }

    public boolean shouldDisableNavbarGestures() {
        return (isDeviceProvisioned() && (this.mDisabled1 & 33554432) == 0) ? false : true;
    }

    public void postQSRunnableDismissingKeyguard(final Runnable runnable) {
        this.mHandler.post(new Runnable() {
            public void run() {
                PhoneStatusBar.this.mLeaveOpenOnKeyguardHide = true;
                PhoneStatusBar.this.executeRunnableDismissingKeyguard(runnable, null, false, false, false);
            }
        });
    }

    public void postStartActivityDismissingKeyguard(final PendingIntent intent) {
        this.mHandler.post(new Runnable() {
            public void run() {
                PhoneStatusBar.this.startPendingIntentDismissingKeyguard(intent);
            }
        });
    }

    public void postStartActivityDismissingKeyguard(final Intent intent, int delay) {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                PhoneStatusBar.this.handleStartActivityDismissingKeyguard(intent, true);
            }
        }, (long) delay);
    }

    private void handleStartActivityDismissingKeyguard(Intent intent, boolean onlyProvisioned) {
        startActivityDismissingKeyguard(intent, onlyProvisioned, true);
    }

    public void destroy() {
        super.destroy();
        if (this.mStatusBarWindow != null) {
            this.mWindowManager.removeViewImmediate(this.mStatusBarWindow);
            this.mStatusBarWindow = null;
        }
        if (this.mNavigationBarView != null) {
            this.mWindowManager.removeViewImmediate(this.mNavigationBarView);
            this.mNavigationBarView = null;
        }
        if (this.mHandlerThread != null) {
            this.mHandlerThread.quitSafely();
            this.mHandlerThread = null;
        }
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        this.mContext.unregisterReceiver(this.mDemoReceiver);
        this.mAssistManager.destroy();
        SignalClusterView signalClusterKeyguard = (SignalClusterView) this.mKeyguardStatusBar.findViewById(R.id.signal_cluster);
        SignalClusterView signalClusterQs = (SignalClusterView) this.mHeader.findViewById(R.id.signal_cluster);
        this.mNetworkController.removeSignalCallback((SignalClusterView) this.mStatusBarView.findViewById(R.id.signal_cluster));
        this.mNetworkController.removeSignalCallback(signalClusterKeyguard);
        this.mNetworkController.removeSignalCallback(signalClusterQs);
        if (this.mQSPanel != null && this.mQSPanel.getHost() != null) {
            this.mQSPanel.getHost().destroy();
        }
    }

    public void dispatchDemoCommand(String command, Bundle args) {
        View notifications = null;
        if (!this.mDemoModeAllowed) {
            boolean z;
            if (Global.getInt(this.mContext.getContentResolver(), "sysui_demo_allowed", 0) != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mDemoModeAllowed = z;
        }
        if (this.mDemoModeAllowed) {
            boolean modeChange;
            if (command.equals("enter")) {
                this.mDemoMode = true;
            } else if (command.equals("exit")) {
                this.mDemoMode = false;
                checkBarModes();
            } else if (!this.mDemoMode) {
                dispatchDemoCommand("enter", new Bundle());
            }
            if (command.equals("enter")) {
                modeChange = true;
            } else {
                modeChange = command.equals("exit");
            }
            if ((modeChange || command.equals("volume")) && this.mVolumeComponent != null) {
                this.mVolumeComponent.dispatchDemoCommand(command, args);
            }
            if (modeChange || command.equals("clock")) {
                dispatchDemoCommandToView(command, args, R.id.clock);
            }
            if (modeChange || command.equals("battery")) {
                this.mBatteryController.dispatchDemoCommand(command, args);
            }
            if (modeChange || command.equals("status")) {
                this.mIconController.dispatchDemoCommand(command, args);
            }
            if (this.mNetworkController != null && (modeChange || command.equals("network"))) {
                this.mNetworkController.dispatchDemoCommand(command, args);
            }
            if (modeChange || command.equals("notifications")) {
                if (this.mStatusBarView != null) {
                    notifications = this.mStatusBarView.findViewById(R.id.notification_icon_area);
                }
                if (notifications != null) {
                    int vis;
                    String visible = args.getString("visible");
                    if (this.mDemoMode && "false".equals(visible)) {
                        vis = 4;
                    } else {
                        vis = 0;
                    }
                    notifications.setVisibility(vis);
                }
            }
            if (command.equals("bars")) {
                String mode = args.getString("mode");
                int barMode = "opaque".equals(mode) ? 0 : "translucent".equals(mode) ? 2 : "semi-transparent".equals(mode) ? 1 : "transparent".equals(mode) ? 4 : "warning".equals(mode) ? 5 : -1;
                if (barMode != -1) {
                    if (this.mStatusBarView != null) {
                        this.mStatusBarView.getBarTransitions().transitionTo(barMode, true);
                    }
                    if (this.mNavigationBarView != null) {
                        this.mNavigationBarView.getBarTransitions().transitionTo(barMode, true);
                    }
                }
            }
        }
    }

    private void dispatchDemoCommandToView(String command, Bundle args, int id) {
        if (this.mStatusBarView != null) {
            View v = this.mStatusBarView.findViewById(id);
            if (v instanceof DemoMode) {
                ((DemoMode) v).dispatchDemoCommand(command, args);
            }
        }
    }

    public int getBarState() {
        return this.mState;
    }

    public boolean isPanelFullyCollapsed() {
        return this.mNotificationPanel.isFullyCollapsed();
    }

    public void showKeyguard() {
        if (this.mLaunchTransitionFadingAway) {
            this.mNotificationPanel.animate().cancel();
            onLaunchTransitionFadingEnded();
        }
        this.mHandler.removeMessages(1003);
        if (this.mUserSwitcherController == null || !this.mUserSwitcherController.useFullscreenUserSwitcher()) {
            setBarState(1);
        } else {
            setBarState(3);
        }
        updateKeyguardState(false, false);
        if (!this.mDeviceInteractive) {
            this.mNotificationPanel.setTouchDisabled(true);
        }
        if (this.mState == 1) {
            instantExpandNotificationsPanel();
        } else if (this.mState == 3) {
            instantCollapseNotificationPanel();
        }
        this.mLeaveOpenOnKeyguardHide = false;
        if (this.mDraggedDownRow != null) {
            this.mDraggedDownRow.setUserLocked(false);
            this.mDraggedDownRow.notifyHeightChanged(false);
            this.mDraggedDownRow = null;
        }
        this.mPendingRemoteInputView = null;
        this.mAssistManager.onLockscreenShown();
    }

    private void onLaunchTransitionFadingEnded() {
        this.mNotificationPanel.setAlpha(1.0f);
        this.mNotificationPanel.onAffordanceLaunchEnded();
        releaseGestureWakeLock();
        runLaunchTransitionEndRunnable();
        this.mLaunchTransitionFadingAway = false;
        this.mScrimController.forceHideScrims(false);
        updateMediaMetaData(true, true);
    }

    public boolean isCollapsing() {
        return this.mNotificationPanel.isCollapsing();
    }

    public void addPostCollapseAction(Runnable r) {
        this.mPostCollapseRunnables.add(r);
    }

    public boolean isInLaunchTransition() {
        if (this.mNotificationPanel.isLaunchTransitionRunning()) {
            return true;
        }
        return this.mNotificationPanel.isLaunchTransitionFinished();
    }

    public void fadeKeyguardAfterLaunchTransition(final Runnable beforeFading, Runnable endRunnable) {
        this.mHandler.removeMessages(1003);
        this.mLaunchTransitionEndRunnable = endRunnable;
        Runnable hideRunnable = new Runnable() {
            public void run() {
                PhoneStatusBar.this.mLaunchTransitionFadingAway = true;
                if (beforeFading != null) {
                    beforeFading.run();
                }
                PhoneStatusBar.this.mScrimController.forceHideScrims(true);
                PhoneStatusBar.this.updateMediaMetaData(false, true);
                PhoneStatusBar.this.mNotificationPanel.setAlpha(1.0f);
                PhoneStatusBar.this.mStackScroller.setParentFadingOut(true);
                PhoneStatusBar.this.mNotificationPanel.animate().alpha(0.0f).setStartDelay(100).setDuration(300).withLayer().withEndAction(new Runnable() {
                    public void run() {
                        PhoneStatusBar.this.onLaunchTransitionFadingEnded();
                    }
                });
                PhoneStatusBar.this.mIconController.appTransitionStarting(SystemClock.uptimeMillis(), 120);
            }
        };
        if (this.mNotificationPanel.isLaunchTransitionRunning()) {
            this.mNotificationPanel.setLaunchTransitionEndRunnable(hideRunnable);
        } else {
            hideRunnable.run();
        }
    }

    public void fadeKeyguardWhilePulsing() {
        this.mNotificationPanel.animate().alpha(0.0f).setStartDelay(0).setDuration(96).setInterpolator(ScrimController.KEYGUARD_FADE_OUT_INTERPOLATOR).start();
    }

    private void onLaunchTransitionTimeout() {
        Log.w("PhoneStatusBar", "Launch transition: Timeout!");
        this.mNotificationPanel.onAffordanceLaunchEnded();
        releaseGestureWakeLock();
        this.mNotificationPanel.resetViews();
    }

    private void runLaunchTransitionEndRunnable() {
        if (this.mLaunchTransitionEndRunnable != null) {
            Runnable r = this.mLaunchTransitionEndRunnable;
            this.mLaunchTransitionEndRunnable = null;
            r.run();
        }
    }

    public boolean hideKeyguard() {
        boolean staying = this.mLeaveOpenOnKeyguardHide;
        setBarState(0);
        View viewToClick = null;
        if (this.mLeaveOpenOnKeyguardHide) {
            this.mLeaveOpenOnKeyguardHide = false;
            long delay = calculateGoingToFullShadeDelay();
            this.mNotificationPanel.animateToFullShade(delay);
            if (this.mDraggedDownRow != null) {
                this.mDraggedDownRow.setUserLocked(false);
                this.mDraggedDownRow = null;
            }
            viewToClick = this.mPendingRemoteInputView;
            this.mPendingRemoteInputView = null;
            if (this.mNavigationBarView != null) {
                this.mNavigationBarView.setLayoutTransitionsEnabled(false);
                this.mNavigationBarView.postDelayed(new Runnable() {
                    public void run() {
                        PhoneStatusBar.this.mNavigationBarView.setLayoutTransitionsEnabled(true);
                    }
                }, 448 + delay);
            }
        } else {
            instantCollapseNotificationPanel();
        }
        updateKeyguardState(staying, false);
        if (viewToClick != null) {
            viewToClick.callOnClick();
        }
        if (this.mQSPanel != null) {
            this.mQSPanel.refreshAllTiles();
        }
        this.mHandler.removeMessages(1003);
        releaseGestureWakeLock();
        this.mNotificationPanel.onAffordanceLaunchEnded();
        this.mNotificationPanel.animate().cancel();
        this.mNotificationPanel.setAlpha(1.0f);
        return staying;
    }

    private void releaseGestureWakeLock() {
        if (this.mGestureWakeLock.isHeld()) {
            this.mGestureWakeLock.release();
        }
    }

    public long calculateGoingToFullShadeDelay() {
        return this.mKeyguardFadingAwayDelay + this.mKeyguardFadingAwayDuration;
    }

    public void keyguardGoingAway() {
        this.mKeyguardGoingAway = true;
        this.mIconController.appTransitionPending();
    }

    public void setKeyguardFadingAway(long startTime, long delay, long fadeoutDuration) {
        boolean z = true;
        this.mKeyguardFadingAway = true;
        this.mKeyguardFadingAwayDelay = delay;
        this.mKeyguardFadingAwayDuration = fadeoutDuration;
        this.mWaitingForKeyguardExit = false;
        this.mIconController.appTransitionStarting((startTime + fadeoutDuration) - 120, 120);
        int i = this.mDisabledUnmodified1;
        int i2 = this.mDisabledUnmodified2;
        if (fadeoutDuration <= 0) {
            z = false;
        }
        disable(i, i2, z);
    }

    public boolean isKeyguardFadingAway() {
        return this.mKeyguardFadingAway;
    }

    public void finishKeyguardFadingAway() {
        this.mKeyguardFadingAway = false;
        this.mKeyguardGoingAway = false;
        disable(this.mDisabledUnmodified1, this.mDisabledUnmodified2, true);
    }

    public void stopWaitingForKeyguardExit() {
        this.mWaitingForKeyguardExit = false;
    }

    private void updatePublicMode() {
        boolean isPublic = false;
        if (this.mStatusBarKeyguardViewManager.isShowing()) {
            for (int i = this.mCurrentProfiles.size() - 1; i >= 0; i--) {
                if (this.mStatusBarKeyguardViewManager.isSecure(((UserInfo) this.mCurrentProfiles.valueAt(i)).id)) {
                    isPublic = true;
                    break;
                }
            }
        }
        setLockscreenPublicMode(isPublic);
    }

    protected void updateKeyguardState(boolean goingToFullShade, boolean fromShadeLocked) {
        boolean z = true;
        if (this.mState == 1) {
            if (this.mKeyguardIndicationController != null) {
                this.mKeyguardIndicationController.setVisible(true);
            }
            this.mNotificationPanel.resetViews();
            if (this.mKeyguardUserSwitcher != null) {
                this.mKeyguardUserSwitcher.setKeyguard(true, fromShadeLocked);
            }
            this.mStatusBarView.removePendingHideExpandedRunnables();
        } else {
            if (this.mKeyguardIndicationController != null) {
                this.mKeyguardIndicationController.setVisible(false);
            }
            if (this.mKeyguardUserSwitcher != null) {
                boolean z2;
                KeyguardUserSwitcher keyguardUserSwitcher = this.mKeyguardUserSwitcher;
                if (goingToFullShade || this.mState == 2) {
                    z2 = true;
                } else {
                    z2 = fromShadeLocked;
                }
                keyguardUserSwitcher.setKeyguard(false, z2);
            }
        }
        if (this.mState == 1 || this.mState == 2) {
            this.mScrimController.setKeyguardShowing(true);
        } else {
            this.mScrimController.setKeyguardShowing(false);
        }
        this.mIconPolicy.notifyKeyguardShowingChanged();
        this.mNotificationPanel.setBarState(this.mState, this.mKeyguardFadingAway, goingToFullShade);
        if (this.mFingerprintUnlockController.isInfastScreenMode()) {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    boolean z = true;
                    PhoneStatusBar.this.updateDozingState();
                    PhoneStatusBar.this.updateNotifications();
                    PhoneStatusBar.this.checkBarModes();
                    PhoneStatusBar phoneStatusBar = PhoneStatusBar.this;
                    if (PhoneStatusBar.this.mState == 1) {
                        z = false;
                    }
                    phoneStatusBar.updateMediaMetaData(false, z);
                }
            }, 150);
            updatePublicMode();
            updateStackScrollerState(goingToFullShade, fromShadeLocked);
            this.mKeyguardMonitor.notifyKeyguardState(false, true);
            return;
        }
        updateDozingState();
        updatePublicMode();
        updateStackScrollerState(goingToFullShade, fromShadeLocked);
        updateNotifications();
        checkBarModes();
        if (this.mState == 1) {
            z = false;
        }
        updateMediaMetaData(false, z);
        this.mKeyguardMonitor.notifyKeyguardState(this.mStatusBarKeyguardViewManager.isShowing(), this.mStatusBarKeyguardViewManager.isSecure());
    }

    private void updateDozingState() {
        boolean z = false;
        boolean isPulsing = !this.mDozing ? this.mDozeScrimController.isPulsing() : false;
        this.mNotificationPanel.setDozing(this.mDozing, isPulsing);
        this.mStackScroller.setDark(this.mDozing, isPulsing, this.mWakeUpTouchLocation);
        this.mScrimController.setDozing(this.mDozing);
        DozeScrimController dozeScrimController = this.mDozeScrimController;
        if (this.mDozing && this.mFingerprintUnlockController.getMode() != 2) {
            z = true;
        }
        dozeScrimController.setDozing(z, isPulsing);
    }

    public void updateStackScrollerState(boolean goingToFullShade, boolean fromShadeLocked) {
        boolean z = true;
        if (this.mStackScroller != null) {
            boolean onKeyguard = this.mState == 1;
            this.mStackScroller.setHideSensitive(isLockscreenPublicMode(), goingToFullShade);
            this.mStackScroller.setDimmed(onKeyguard, fromShadeLocked);
            NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
            if (onKeyguard) {
                z = false;
            }
            notificationStackScrollLayout.setExpandingEnabled(z);
            ActivatableNotificationView activatedChild = this.mStackScroller.getActivatedChild();
            this.mStackScroller.setActivatedChild(null);
            if (activatedChild != null) {
                activatedChild.makeInactive(false);
            }
        }
    }

    public void userActivity() {
        if (this.mState == 1) {
            this.mKeyguardViewMediatorCallback.userActivity();
        }
    }

    public boolean interceptMediaKey(KeyEvent event) {
        if (this.mState == 1) {
            return this.mStatusBarKeyguardViewManager.interceptMediaKey(event);
        }
        return false;
    }

    public boolean onMenuPressed() {
        if (!this.mDeviceInteractive || this.mState == 0 || !this.mStatusBarKeyguardViewManager.shouldDismissOnMenuPressed()) {
            return false;
        }
        animateCollapsePanels(2, true);
        return true;
    }

    public void endAffordanceLaunch() {
        releaseGestureWakeLock();
        this.mNotificationPanel.onAffordanceLaunchEnded();
    }

    public boolean onBackPressed() {
        if (this.mStatusBarKeyguardViewManager.onBackPressed()) {
            return true;
        }
        if (this.mNotificationPanel.isQsExpanded()) {
            if (this.mNotificationPanel.isQsDetailShowing()) {
                this.mNotificationPanel.closeQsDetail();
            } else {
                this.mNotificationPanel.animateCloseQs();
            }
            return true;
        } else if (this.mState == 1 || this.mState == 2) {
            return false;
        } else {
            animateCollapsePanels();
            return true;
        }
    }

    public boolean onSpacePressed() {
        if (!this.mDeviceInteractive || this.mState == 0) {
            return false;
        }
        animateCollapsePanels(2, true);
        return true;
    }

    private void showBouncer() {
        if (this.mState == 1 || this.mState == 2) {
            this.mWaitingForKeyguardExit = this.mStatusBarKeyguardViewManager.isShowing();
            this.mStatusBarKeyguardViewManager.dismiss();
        }
    }

    private void instantExpandNotificationsPanel() {
        makeExpandedVisible(true);
        this.mNotificationPanel.expand(false);
    }

    private void instantCollapseNotificationPanel() {
        this.mNotificationPanel.instantCollapse();
    }

    public void onActivated(ActivatableNotificationView view) {
        EventLogTags.writeSysuiLockscreenGesture(7, 0, 0);
        HwLog.i("PhoneStatusBar", "onActivated: " + this.mKeyguardIndicationController);
        if (this.mKeyguardIndicationController != null) {
            this.mKeyguardIndicationController.showTransientIndication((int) R.string.notification_tap_again);
        }
        showNotificationToast(view.shouldShowNodetails());
        ActivatableNotificationView previousView = this.mStackScroller.getActivatedChild();
        if (previousView != null) {
            previousView.makeInactive(true);
        }
        this.mStackScroller.setActivatedChild(view);
    }

    public void setBarState(int state) {
        if (state != this.mState && this.mVisible && (state == 2 || (state == 0 && isGoingToNotificationShade()))) {
            clearNotificationEffects();
        }
        if (state == 1) {
            removeRemoteInputEntriesKeptUntilCollapsed();
        }
        this.mState = state;
        this.mGroupManager.setStatusBarState(state);
        this.mFalsingManager.setStatusBarState(state);
        this.mStatusBarWindowManager.setStatusBarState(state);
        updateDozing();
    }

    public void onActivationReset(ActivatableNotificationView view) {
        if (view == this.mStackScroller.getActivatedChild()) {
            if (this.mKeyguardIndicationController != null) {
                this.mKeyguardIndicationController.hideTransientIndication();
            }
            this.mStackScroller.setActivatedChild(null);
        }
    }

    public void onTrackingStarted() {
        runPostCollapseRunnables();
    }

    public void onClosingFinished() {
        runPostCollapseRunnables();
    }

    public void onUnlockHintStarted() {
        this.mFalsingManager.onUnlockHintStarted();
        if (this.mKeyguardIndicationController != null) {
            this.mKeyguardIndicationController.showTransientIndication((int) R.string.keyguard_unlock);
        }
    }

    public void onHintFinished() {
        if (this.mKeyguardIndicationController != null) {
            this.mKeyguardIndicationController.hideTransientIndicationDelayed(1200);
        }
    }

    public void onTrackingStopped(boolean expand) {
        if ((this.mState == 1 || this.mState == 2) && !expand && !this.mUnlockMethodCache.canSkipBouncer()) {
            showBouncer();
        }
    }

    protected int getMaxKeyguardNotifications(boolean recompute) {
        if (!recompute) {
            return this.mMaxKeyguardNotifications;
        }
        this.mMaxKeyguardNotifications = Math.max(1, this.mNotificationPanel.computeMaxKeyguardNotifications(this.mMaxAllowedKeyguardNotifications));
        return this.mMaxKeyguardNotifications;
    }

    public int getMaxKeyguardNotifications() {
        return getMaxKeyguardNotifications(false);
    }

    public NavigationBarView getNavigationBarView() {
        return this.mNavigationBarView;
    }

    public boolean onDraggedDown(View startingChild, int dragLengthY) {
        HwLog.i("PhoneStatusBar", "onDraggedDown");
        if (!hasActiveNotifications()) {
            return false;
        }
        EventLogTags.writeSysuiLockscreenGesture(2, (int) (((float) dragLengthY) / this.mDisplayMetrics.density), 0);
        goToLockedShade(startingChild);
        if (startingChild instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) startingChild).onExpandedByGesture(true);
        }
        return true;
    }

    public void onDragDownReset() {
        this.mStackScroller.setDimmed(true, true);
        this.mStackScroller.resetScrollPosition();
    }

    public void onCrossedThreshold(boolean above) {
        boolean z;
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        if (above) {
            z = false;
        } else {
            z = true;
        }
        notificationStackScrollLayout.setDimmed(z, true);
    }

    public void onTouchSlopExceeded() {
        this.mStackScroller.removeLongPressCallback();
    }

    public void setEmptyDragAmount(float amount) {
        this.mNotificationPanel.setEmptyDragAmount(amount);
    }

    public void goToLockedShade(View expandView) {
        HwLog.i("PhoneStatusBar", "goToLockedShade:");
        ExpandableNotificationRow expandableNotificationRow = null;
        if (expandView instanceof ExpandableNotificationRow) {
            expandableNotificationRow = (ExpandableNotificationRow) expandView;
            expandableNotificationRow.setUserExpanded(true, true);
            expandableNotificationRow.setGroupExpansionChanging(true);
        }
        boolean shouldEnforceBouncer;
        if (userAllowsPrivateNotificationsInPublic(this.mCurrentUserId) && this.mShowLockscreenNotifications) {
            shouldEnforceBouncer = this.mFalsingManager.shouldEnforceBouncer();
        } else {
            shouldEnforceBouncer = true;
        }
        if (isLockscreenPublicMode() && r0) {
            this.mLeaveOpenOnKeyguardHide = true;
            showBouncer();
            this.mDraggedDownRow = expandableNotificationRow;
            this.mPendingRemoteInputView = null;
        } else {
            this.mNotificationPanel.animateToFullShade(0);
            setBarState(2);
            updateKeyguardState(false, false);
        }
        hideNotificationToast();
    }

    public void onLockedNotificationImportanceChange(OnDismissAction dismissAction) {
        this.mLeaveOpenOnKeyguardHide = true;
        dismissKeyguardThenExecute(dismissAction, true);
    }

    protected void onLockedRemoteInput(ExpandableNotificationRow row, View clicked) {
        this.mLeaveOpenOnKeyguardHide = true;
        showBouncer();
        this.mPendingRemoteInputView = clicked;
    }

    protected boolean startWorkChallengeIfNecessary(int userId, IntentSender intendSender, String notificationKey) {
        this.mPendingWorkRemoteInputView = null;
        return super.startWorkChallengeIfNecessary(userId, intendSender, notificationKey);
    }

    protected void onLockedWorkRemoteInput(int userId, ExpandableNotificationRow row, View clicked) {
        animateCollapsePanels();
        startWorkChallengeIfNecessary(userId, null, null);
        this.mPendingWorkRemoteInputView = clicked;
    }

    protected void onWorkChallengeUnlocked() {
        if (this.mPendingWorkRemoteInputView != null) {
            View pendingWorkRemoteInputView = this.mPendingWorkRemoteInputView;
            final Runnable clickPendingViewRunnable = new Runnable() {
                public void run() {
                    if (PhoneStatusBar.this.mPendingWorkRemoteInputView != null) {
                        for (ViewParent p = PhoneStatusBar.this.mPendingWorkRemoteInputView.getParent(); p != null; p = p.getParent()) {
                            if (p instanceof ExpandableNotificationRow) {
                                final ExpandableNotificationRow row = (ExpandableNotificationRow) p;
                                ViewParent viewParent = row.getParent();
                                if (viewParent instanceof NotificationStackScrollLayout) {
                                    final NotificationStackScrollLayout scrollLayout = (NotificationStackScrollLayout) viewParent;
                                    row.makeActionsVisibile();
                                    row.post(new Runnable() {
                                        public void run() {
                                            final NotificationStackScrollLayout notificationStackScrollLayout = scrollLayout;
                                            Runnable finishScrollingCallback = new Runnable() {
                                                public void run() {
                                                    PhoneStatusBar.this.mPendingWorkRemoteInputView.callOnClick();
                                                    PhoneStatusBar.this.mPendingWorkRemoteInputView = null;
                                                    notificationStackScrollLayout.setFinishScrollingCallback(null);
                                                }
                                            };
                                            if (scrollLayout.scrollTo(row)) {
                                                scrollLayout.setFinishScrollingCallback(finishScrollingCallback);
                                            } else {
                                                finishScrollingCallback.run();
                                            }
                                        }
                                    });
                                    return;
                                }
                                return;
                            }
                        }
                    }
                }
            };
            this.mNotificationPanel.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (PhoneStatusBar.this.mNotificationPanel.mStatusBar.getStatusBarWindow().getHeight() != PhoneStatusBar.this.mNotificationPanel.mStatusBar.getStatusBarHeight()) {
                        PhoneStatusBar.this.mNotificationPanel.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        PhoneStatusBar.this.mNotificationPanel.post(clickPendingViewRunnable);
                    }
                }
            });
            instantExpandNotificationsPanel();
        }
    }

    public void onExpandClicked(Entry clickedEntry, boolean nowExpanded) {
        this.mHeadsUpManager.setExpanded(clickedEntry, nowExpanded);
        if (this.mState == 1 && nowExpanded) {
            goToLockedShade(clickedEntry.row);
        }
    }

    public void goToKeyguard() {
        if (this.mState == 2) {
            this.mStackScroller.onGoToKeyguard();
            setBarState(1);
            updateKeyguardState(false, true);
        }
    }

    public long getKeyguardFadingAwayDelay() {
        return this.mKeyguardFadingAwayDelay;
    }

    public long getKeyguardFadingAwayDuration() {
        return this.mKeyguardFadingAwayDuration;
    }

    public void setBouncerShowing(boolean bouncerShowing) {
        super.setBouncerShowing(bouncerShowing);
        this.mStatusBarView.setBouncerShowing(bouncerShowing);
        disable(this.mDisabledUnmodified1, this.mDisabledUnmodified2, true);
    }

    public void onStartedGoingToSleep() {
        HwLog.i("PhoneStatusBar", "onStartedGoingToSleep");
        this.mStartedGoingToSleep = true;
    }

    public void onFinishedGoingToSleep() {
        HwLog.i("PhoneStatusBar", "onFinishedGoingToSleep");
        this.mNotificationPanel.onAffordanceLaunchEnded();
        releaseGestureWakeLock();
        this.mLaunchCameraOnScreenTurningOn = false;
        this.mStartedGoingToSleep = false;
        this.mDeviceInteractive = false;
        this.mWakeUpComingFromTouch = false;
        this.mWakeUpTouchLocation = null;
        this.mStackScroller.setAnimationsEnabled(false);
        updateVisibleToUser();
        if (this.mLaunchCameraOnFinishedGoingToSleep) {
            this.mLaunchCameraOnFinishedGoingToSleep = false;
            this.mHandler.post(new Runnable() {
                public void run() {
                    PhoneStatusBar.this.onCameraLaunchGestureDetected(PhoneStatusBar.this.mLastCameraLaunchSource);
                }
            });
        }
    }

    public void onStartedWakingUp() {
        HwLog.i("PhoneStatusBar", "onStartedWakingUp");
        this.mDeviceInteractive = true;
        this.mHandler.postDelayed(new -void_onStartedWakingUp__LambdaImpl0(), 1000);
        this.mNotificationPanel.setTouchDisabled(false);
        updateVisibleToUser();
    }

    /* synthetic */ void -com_android_systemui_statusbar_phone_PhoneStatusBar_lambda$2() {
        this.mStackScroller.setAnimationsEnabled(true);
    }

    public void onScreenTurningOn() {
        HwLog.i("PhoneStatusBar", "onScreenTurningOn");
        this.mScreenTurningOn = true;
        this.mFalsingManager.onScreenTurningOn();
        this.mNotificationPanel.onScreenTurningOn();
        if (this.mLaunchCameraOnScreenTurningOn) {
            this.mNotificationPanel.launchCamera(false, this.mLastCameraLaunchSource);
            this.mLaunchCameraOnScreenTurningOn = false;
        }
    }

    private void vibrateForCameraGesture() {
        this.mVibrator.vibrate(new long[]{0, 750}, -1);
    }

    public void onScreenTurnedOn() {
        HwLog.i("PhoneStatusBar", "onScreenTurnedOn");
        this.mScreenTurningOn = false;
        this.mDozeScrimController.onScreenTurnedOn();
    }

    public boolean handleLongPressBack() {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                Log.i("PhoneStatusBar", "handleLongPressBack runInThread");
                boolean z = false;
                try {
                    IActivityManager activityManager = ActivityManagerNative.getDefault();
                    z = activityManager.isInLockTaskMode();
                    if (z) {
                        activityManager.stopSystemLockTaskMode();
                    }
                } catch (RemoteException e) {
                    Log.d("PhoneStatusBar", "Unable to reach activity manager", e);
                }
                Log.i("PhoneStatusBar", "handleLongPressBack runInThread isInLockTaskMode " + z);
                return z;
            }

            public void runInUI() {
                Log.i("PhoneStatusBar", "handleLongPressBack runInUI isInLockTaskMode ");
                PhoneStatusBar.this.mNavigationBarView.setDisabledFlags(PhoneStatusBar.this.mDisabled1, true);
                Intent intent = new Intent("com.huawei.android.systemui.screenpinning");
                intent.setPackage(PhoneStatusBar.this.mContext.getPackageName());
                intent.putExtra("screenpinning_state", false);
                PhoneStatusBar.this.mContext.sendBroadcast(intent);
            }
        });
        return true;
    }

    public void updateRecentsVisibility(boolean visible) {
        if (visible) {
            this.mSystemUiVisibility |= 16384;
        } else {
            this.mSystemUiVisibility &= -16385;
        }
        notifyUiVisibilityChanged(this.mSystemUiVisibility);
    }

    public void showScreenPinningRequest(int taskId) {
        if (!this.mKeyguardMonitor.isShowing()) {
            showScreenPinningRequest(taskId, true);
        }
    }

    public void showScreenPinningRequest(int taskId, boolean allowCancel) {
        showScreenPinningDialog(taskId, allowCancel);
    }

    public boolean hasActiveNotifications() {
        return !this.mNotificationData.getActiveNotifications().isEmpty();
    }

    public void wakeUpIfDozing(long time, MotionEvent event) {
        if (this.mDozing && this.mDozeScrimController.isPulsing()) {
            ((PowerManager) this.mContext.getSystemService("power")).wakeUp(time, "com.android.systemui:NODOZE");
            this.mWakeUpComingFromTouch = true;
            this.mWakeUpTouchLocation = new PointF(event.getX(), event.getY());
            this.mNotificationPanel.setTouchDisabled(false);
            this.mStatusBarKeyguardViewManager.notifyDeviceWakeUpRequested();
            this.mFalsingManager.onScreenOnFromTouch();
        }
    }

    public void appTransitionPending() {
        if (!this.mKeyguardFadingAway) {
            this.mIconController.appTransitionPending();
        }
    }

    public void appTransitionCancelled() {
        this.mIconController.appTransitionCancelled();
        EventBus.getDefault().send(new AppTransitionFinishedEvent());
    }

    public void appTransitionStarting(long startTime, long duration) {
        if (!this.mKeyguardGoingAway) {
            this.mIconController.appTransitionStarting(startTime, duration);
        }
        if (this.mIconPolicy != null) {
            this.mIconPolicy.appTransitionStarting(startTime, duration);
        }
    }

    public void appTransitionFinished() {
        EventBus.getDefault().send(new AppTransitionFinishedEvent());
    }

    public void onCameraLaunchGestureDetected(int source) {
        this.mLastCameraLaunchSource = source;
        if (this.mStartedGoingToSleep) {
            this.mLaunchCameraOnFinishedGoingToSleep = true;
            return;
        }
        if (this.mNotificationPanel.canCameraGestureBeLaunched(this.mStatusBarKeyguardViewManager.isShowing() ? this.mExpandedVisible : false)) {
            if (!this.mDeviceInteractive) {
                ((PowerManager) this.mContext.getSystemService(PowerManager.class)).wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:CAMERA_GESTURE");
                this.mStatusBarKeyguardViewManager.notifyDeviceWakeUpRequested();
            }
            vibrateForCameraGesture();
            if (this.mStatusBarKeyguardViewManager.isShowing()) {
                if (!this.mDeviceInteractive) {
                    this.mScrimController.dontAnimateBouncerChangesUntilNextFrame();
                    this.mGestureWakeLock.acquire(6000);
                }
                if (this.mScreenTurningOn || this.mStatusBarKeyguardViewManager.isScreenTurnedOn()) {
                    this.mNotificationPanel.launchCamera(this.mDeviceInteractive, source);
                } else {
                    this.mLaunchCameraOnScreenTurningOn = true;
                }
            } else {
                startActivity(KeyguardBottomAreaView.INSECURE_CAMERA_INTENT, true);
            }
        }
    }

    public void showTvPictureInPictureMenu() {
    }

    public void notifyFpAuthModeChanged() {
        updateDozing();
    }

    private void updateDozing() {
        boolean z = true;
        if (!((this.mDozingRequested && this.mState == 1) || this.mFingerprintUnlockController.getMode() == 2)) {
            z = false;
        }
        this.mDozing = z;
        updateDozingState();
    }

    public IFlashlightController getFlashlightController() {
        return this.mFlashlightController;
    }

    public View getNotificationPanelView() {
        return this.mNotificationPanel;
    }

    public View getNotificationStackScrollerView() {
        if (this.mStatusBarWindow != null) {
            return this.mStatusBarWindow.findViewById(R.id.notification_stack_scroller);
        }
        return null;
    }

    public Bitmap getLockScreenWallpaper() {
        if (this.mLockscreenWallpaper == null) {
            return null;
        }
        return this.mLockscreenWallpaper.getBitmap();
    }

    public boolean isNotificationPanelExpanded() {
        return this.mStatusBarView.getState() == 2;
    }

    public boolean updateKeyguardStatusbarColor(SparseIntArray resultMap) {
        HwWallpaperMask.tryUpdateKeyguardWallpaperWithMask(resultMap);
        return true;
    }

    public void removeFingerprintMsg() {
        this.mFingerprintUnlockController.removeFingerprintMsg();
    }

    public void hideNotificationToastIfShowing() {
        hideNotificationToast();
    }

    public void updateTileState(State state, String spec) {
        if (this.mQSCustomizer != null) {
            this.mQSCustomizer.updateCustomPanel(state, spec);
        }
    }
}
