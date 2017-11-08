package com.android.server.wm;

import android.animation.ValueAnimator;
import android.annotation.IntDef;
import android.app.ActivityManagerInternal;
import android.app.ActivityManagerNative;
import android.app.AppOpsManager;
import android.app.AppOpsManager.OnOpChangedInternalListener;
import android.app.IActivityManager;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManagerInternal;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.PowerManagerInternal;
import android.os.PowerManagerInternal.LowPowerModeListener;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemService;
import android.os.Trace;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.rms.HwSysResource;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Flog;
import android.util.Jlog;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import android.util.TypedValue;
import android.util.Xml;
import android.view.AppTransitionAnimationSpec;
import android.view.Choreographer;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.IApplicationToken;
import android.view.IDockedStackListener;
import android.view.IInputFilter;
import android.view.IOnKeyguardExitResult;
import android.view.IRotationWatcher;
import android.view.IWindow;
import android.view.IWindowId;
import android.view.IWindowSession;
import android.view.IWindowSessionCallback;
import android.view.InputChannel;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.InputEventReceiver.Factory;
import android.view.MagnificationSpec;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.View;
import android.view.WindowContentFrameStats;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerInternal;
import android.view.WindowManagerInternal.AppTransitionListener;
import android.view.WindowManagerInternal.MagnificationCallbacks;
import android.view.WindowManagerInternal.OnHardKeyboardStatusChangeListener;
import android.view.WindowManagerInternal.WindowsForAccessibilityCallback;
import android.view.WindowManagerPolicy;
import android.view.WindowManagerPolicy.InputConsumer;
import android.view.WindowManagerPolicy.OnKeyguardExitResult;
import android.view.WindowManagerPolicy.PointerEventListener;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManagerInternal;
import com.android.internal.R;
import com.android.internal.app.IAssistScreenshotReceiver;
import com.android.internal.os.IResultReceiver;
import com.android.internal.policy.IShortcutService;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.XmlUtils;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodManager;
import com.android.internal.view.WindowManagerPolicyThread;
import com.android.server.AbsLocationManagerService;
import com.android.server.AttributeCache;
import com.android.server.AttributeCache.Entry;
import com.android.server.DisplayThread;
import com.android.server.EventLogTags;
import com.android.server.FgThread;
import com.android.server.HwServiceFactory;
import com.android.server.HwServiceFactory.IHwWindowManagerService;
import com.android.server.LocalServices;
import com.android.server.UiThread;
import com.android.server.Watchdog;
import com.android.server.Watchdog.Monitor;
import com.android.server.input.InputManagerService;
import com.android.server.job.controllers.JobStatus;
import com.android.server.policy.HwPolicyFactory;
import com.android.server.power.IHwShutdownThread;
import com.android.server.power.ShutdownThread;
import com.hisi.perfhub.PerfHub;
import com.huawei.cust.HwCustUtils;
import com.huawei.pgmng.log.LogPower;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.Socket;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class WindowManagerService extends AbsWindowManagerService implements Monitor, WindowManagerFuncs {
    private static final boolean ALWAYS_KEEP_CURRENT = true;
    private static final int ANIMATION_DURATION_SCALE = 2;
    private static final int BOOT_ANIMATION_POLL_INTERVAL = 200;
    private static final String BOOT_ANIMATION_SERVICE = "bootanim";
    public static final int COMPAT_MODE_DISABLED = 0;
    public static final int COMPAT_MODE_ENABLED = 1;
    public static final int COMPAT_MODE_MATCH_PARENT = -3;
    static final boolean CUSTOM_SCREEN_ROTATION = true;
    static final long DEFAULT_INPUT_DISPATCHING_TIMEOUT_NANOS = 5000000000L;
    private static final String DENSITY_OVERRIDE = "ro.config.density_override";
    private static final float DRAG_SHADOW_ALPHA_TRANSPARENT = 0.7071f;
    static final boolean HISI_PERF_OPT = SystemProperties.getBoolean("build.hisi_perf_opt", false);
    static final boolean HWFLOW = true;
    private static final int INPUT_DEVICES_READY_FOR_SAFE_MODE_DETECTION_TIMEOUT_MILLIS = 1000;
    static final int LAST_ANR_LIFETIME_DURATION_MSECS = 7200000;
    static final int LAYER_OFFSET_DIM = 1;
    static final int LAYER_OFFSET_THUMBNAIL = 4;
    static final int LAYOUT_REPEAT_THRESHOLD = 4;
    static final int MAX_ANIMATION_DURATION = 10000;
    private static final int MAX_SCREENSHOT_RETRIES = 3;
    static final boolean PROFILE_ORIENTATION = false;
    private static final String PROPERTY_BUILD_DATE_UTC = "ro.build.date.utc";
    private static final String PROPERTY_EMULATOR_CIRCULAR = "ro.emulator.circular";
    static final boolean SCREENSHOT_FORCE_565 = true;
    private static final String SIZE_OVERRIDE = "ro.config.size_override";
    private static final String SYSTEM_DEBUGGABLE = "ro.debuggable";
    private static final String SYSTEM_SECURE = "ro.secure";
    private static final String TAG = "WindowManager";
    private static final int TRANSITION_ANIMATION_SCALE = 1;
    static final int TYPE_LAYER_MULTIPLIER = 10000;
    static final int TYPE_LAYER_OFFSET = 1000;
    static final int UPDATE_FOCUS_NORMAL = 0;
    static final int UPDATE_FOCUS_PLACING_SURFACES = 2;
    static final int UPDATE_FOCUS_WILL_ASSIGN_LAYERS = 1;
    static final int UPDATE_FOCUS_WILL_PLACE_SURFACES = 3;
    static final int WINDOWS_FREEZING_SCREENS_ACTIVE = 1;
    static final int WINDOWS_FREEZING_SCREENS_NONE = 0;
    static final int WINDOWS_FREEZING_SCREENS_TIMEOUT = 2;
    private static final int WINDOW_ANIMATION_SCALE = 0;
    static final int WINDOW_FREEZE_TIMEOUT_DURATION = 2000;
    static final int WINDOW_LAYER_MULTIPLIER = 5;
    static final int WINDOW_REPLACEMENT_TIMEOUT_DURATION = 2000;
    static final boolean localLOGV = false;
    AccessibilityController mAccessibilityController;
    final IActivityManager mActivityManager;
    private final AppTransitionListener mActivityManagerAppTransitionNotifier = new AppTransitionListener() {
        public void onAppTransitionCancelledLocked() {
            WindowManagerService.this.mH.sendEmptyMessage(48);
        }

        public void onAppTransitionFinishedLocked(IBinder token) {
            WindowManagerService.this.mH.sendEmptyMessage(49);
            AppWindowToken atoken = WindowManagerService.this.findAppWindowToken(token);
            if (atoken != null) {
                if (atoken.mLaunchTaskBehind) {
                    try {
                        WindowManagerService.this.mActivityManager.notifyLaunchTaskBehindComplete(atoken.token);
                    } catch (RemoteException e) {
                    }
                    atoken.mLaunchTaskBehind = false;
                } else {
                    atoken.updateReportedVisibilityLocked();
                    if (atoken.mEnteringAnimation) {
                        atoken.mEnteringAnimation = false;
                        try {
                            WindowManagerService.this.mActivityManager.notifyEnterAnimationComplete(atoken.token);
                        } catch (RemoteException e2) {
                        }
                    }
                }
            }
        }
    };
    private HwSysResource mActivityResource;
    final boolean mAllowAnimationsInLowPowerMode;
    final boolean mAllowBootMessages;
    boolean mAllowTheaterModeWakeFromLayout;
    boolean mAltOrientation = false;
    final ActivityManagerInternal mAmInternal;
    boolean mAnimateWallpaperWithTarget;
    boolean mAnimationScheduled;
    boolean mAnimationsDisabled = false;
    final WindowAnimator mAnimator;
    float mAnimatorDurationScaleSetting = 1.0f;
    final AppOpsManager mAppOps;
    final AppTransition mAppTransition;
    int mAppsFreezingScreen = 0;
    boolean mBootAnimationStopped = false;
    private final BoundsAnimationController mBoundsAnimationController;
    final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(intent.getAction())) {
                WindowManagerService.this.mKeyguardDisableHandler.sendEmptyMessage(3);
            }
        }
    };
    private final ArrayList<Integer> mChangedStackList = new ArrayList();
    final Choreographer mChoreographer = Choreographer.getInstance();
    CircularDisplayMask mCircularDisplayMask;
    boolean mClientFreezingScreen = false;
    final ArraySet<AppWindowToken> mClosingApps = new ArraySet();
    final DisplayMetrics mCompatDisplayMetrics = new DisplayMetrics();
    float mCompatibleScreenScale;
    protected final Context mContext;
    Configuration mCurConfiguration = new Configuration();
    WindowState mCurrentFocus = null;
    int[] mCurrentProfileIds = new int[0];
    int mCurrentUserId;
    private HwCustWindowManagerService mCust;
    int mDeferredRotationPauseCount;
    final ArrayList<WindowState> mDestroyPreservedSurface = new ArrayList();
    final ArrayList<WindowState> mDestroySurface = new ArrayList();
    SparseArray<DisplayContent> mDisplayContents = new SparseArray(2);
    boolean mDisplayEnabled = false;
    long mDisplayFreezeTime = 0;
    boolean mDisplayFrozen = false;
    final DisplayManager mDisplayManager;
    final DisplayManagerInternal mDisplayManagerInternal;
    final DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    boolean mDisplayReady;
    final DisplaySettings mDisplaySettings;
    final Display[] mDisplays;
    Rect mDockedStackCreateBounds;
    int mDockedStackCreateMode = 0;
    DragState mDragState = null;
    final long mDrawLockTimeoutMillis;
    EmulatorDisplayOverlay mEmulatorDisplayOverlay;
    int mEnterAnimId;
    private boolean mEventDispatchingEnabled;
    int mExitAnimId;
    final ArrayList<AppWindowToken> mFinishedEarlyAnim = new ArrayList();
    final ArrayList<AppWindowToken> mFinishedStarting = new ArrayList();
    boolean mFocusMayChange;
    AppWindowToken mFocusedApp = null;
    float mForceCompatibleScreenScale;
    boolean mForceDisplayEnabled = false;
    final ArrayList<WindowState> mForceRemoves = new ArrayList();
    boolean mForceResizableTasks = false;
    int mForcedAppOrientation = -1;
    final SurfaceSession mFxSession;
    final H mH = new H();
    boolean mHardKeyboardAvailable;
    OnHardKeyboardStatusChangeListener mHardKeyboardStatusChangeListener;
    final boolean mHasPermanentDpad;
    final boolean mHaveInputMethods;
    Session mHoldingScreenOn;
    WakeLock mHoldingScreenWakeLock;
    boolean mInTouchMode;
    InputConsumerImpl mInputConsumer;
    final InputManagerService mInputManager;
    final ArrayList<WindowState> mInputMethodDialogs = new ArrayList();
    IInputMethodManager mInputMethodManager;
    WindowState mInputMethodTarget = null;
    boolean mInputMethodTargetWaitingAnim;
    WindowState mInputMethodWindow = null;
    final InputMonitor mInputMonitor = new InputMonitor(this);
    boolean mIsPerfBoost = false;
    boolean mIsTouchDevice;
    boolean mKeyguardAttachWallpaper;
    private final KeyguardDisableHandler mKeyguardDisableHandler;
    Runnable mKeyguardDismissDoneCallback;
    private boolean mKeyguardWaitingForActivityDrawn;
    WindowState mKeyguardWin;
    String mLastANRState;
    int mLastDispatchedSystemUiVisibility = 0;
    int mLastDisplayFreezeDuration = 0;
    Object mLastFinishedFreezeSource = null;
    WindowState mLastFocus = null;
    int mLastKeyguardForcedOrientation = -1;
    int mLastStatusBarVisibility = 0;
    WindowState mLastWakeLockHoldingWindow = null;
    WindowState mLastWakeLockObscuringWindow = null;
    int mLastWindowForcedOrientation = -1;
    final WindowLayersController mLayersController;
    int mLayoutSeq = 0;
    public int mLazyModeOn = 0;
    final boolean mLimitedAlphaCompositing;
    ArrayList<WindowState> mLosingFocus = new ArrayList();
    private MousePositionTracker mMousePositionTracker = new MousePositionTracker();
    final List<IBinder> mNoAnimationNotifyOnTransitionFinished = new ArrayList();
    final boolean mOnlyCore;
    final ArraySet<AppWindowToken> mOpeningApps = new ArraySet();
    private final HashMap<String, Boolean> mPackages = new HashMap();
    final ArrayList<WindowState> mPendingRemove = new ArrayList();
    WindowState[] mPendingRemoveTmp = new WindowState[20];
    private PerfHub mPerfHub;
    private final PointerEventDispatcher mPointerEventDispatcher;
    final WindowManagerPolicy mPolicy = HwPolicyFactory.getHwPhoneWindowManager();
    PowerManager mPowerManager;
    PowerManagerInternal mPowerManagerInternal;
    final DisplayMetrics mRealDisplayMetrics = new DisplayMetrics();
    WindowState[] mRebuildTmp = new WindowState[20];
    private final DisplayContentList mReconfigureOnConfigurationChanged = new DisplayContentList();
    final ArrayList<AppWindowToken> mReplacingWindowTimeouts = new ArrayList();
    final ArrayList<WindowState> mResizingWindows = new ArrayList();
    int mRotation = (SystemProperties.getInt("ro.panel.hw_orientation", 0) / 90);
    ArrayList<RotationWatcher> mRotationWatchers = new ArrayList();
    boolean mSafeMode;
    SparseArray<Boolean> mScreenCaptureDisabled = new SparseArray();
    private final WakeLock mScreenFrozenLock;
    final Rect mScreenRect = new Rect();
    final ArraySet<Session> mSessions = new ArraySet();
    SettingsObserver mSettingsObserver;
    boolean mShowingBootMessages = false;
    boolean mSkipAppTransitionAnimation = false;
    SparseArray<TaskStack> mStackIdToStack = new SparseArray();
    StrictModeFlash mStrictModeFlash;
    boolean mSystemBooted = false;
    int mSystemDecorLayer = 0;
    SparseArray<Task> mTaskIdToTask = new SparseArray();
    TaskPositioner mTaskPositioner;
    final Configuration mTempConfiguration = new Configuration();
    private WindowContentFrameStats mTempWindowRenderStats;
    final DisplayMetrics mTmpDisplayMetrics = new DisplayMetrics();
    final float[] mTmpFloats = new float[9];
    final Rect mTmpRect = new Rect();
    final Rect mTmpRect2 = new Rect();
    final Rect mTmpRect3 = new Rect();
    private final SparseIntArray mTmpTaskIds = new SparseIntArray();
    final ArrayList<WindowState> mTmpWindows = new ArrayList();
    final HashMap<IBinder, WindowToken> mTokenMap = new HashMap();
    int mTopWallpaperAnimLayer;
    WindowState mTopWallpaperWin;
    int mTransactionSequence;
    float mTransitionAnimationScaleSetting = 1.0f;
    boolean mTurnOnScreen;
    private ViewServer mViewServer;
    boolean mWaitingForConfig = false;
    ArrayList<WindowState> mWaitingForDrawn = new ArrayList();
    Runnable mWaitingForDrawnCallback;
    WallpaperController mWallpaperControllerLocked;
    InputConsumerImpl mWallpaperInputConsumer;
    Watermark mWatermark;
    float mWindowAnimationScaleSetting = 1.0f;
    final ArrayList<WindowChangeListener> mWindowChangeListeners = new ArrayList();
    final HashMap<IBinder, WindowState> mWindowMap = new HashMap();
    final WindowSurfacePlacer mWindowPlacerLocked;
    boolean mWindowsChanged = false;
    int mWindowsFreezingScreen = 0;

    public interface WindowChangeListener {
        void focusChanged();

        void windowsChanged();
    }

    final class DragInputEventReceiver extends InputEventReceiver {
        private boolean mIsStartEvent = true;
        private boolean mStylusButtonDownAtStart;

        public DragInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onInputEvent(InputEvent event) {
            boolean handled = false;
            try {
                if (!(!(event instanceof MotionEvent) || (event.getSource() & 2) == 0 || WindowManagerService.this.mDragState == null)) {
                    MotionEvent motionEvent = (MotionEvent) event;
                    boolean endDrag = false;
                    float newX = motionEvent.getRawX();
                    float newY = motionEvent.getRawY();
                    boolean isStylusButtonDown = (motionEvent.getButtonState() & 32) != 0;
                    if (this.mIsStartEvent) {
                        if (isStylusButtonDown) {
                            this.mStylusButtonDownAtStart = true;
                        }
                        this.mIsStartEvent = false;
                    }
                    switch (motionEvent.getAction()) {
                        case 1:
                            synchronized (WindowManagerService.this.mWindowMap) {
                                endDrag = WindowManagerService.this.mDragState.notifyDropLw(newX, newY);
                            }
                        case 2:
                            if (!this.mStylusButtonDownAtStart || isStylusButtonDown) {
                                synchronized (WindowManagerService.this.mWindowMap) {
                                    WindowManagerService.this.mDragState.notifyMoveLw(newX, newY);
                                }
                            } else {
                                synchronized (WindowManagerService.this.mWindowMap) {
                                    endDrag = WindowManagerService.this.mDragState.notifyDropLw(newX, newY);
                                }
                            }
                            break;
                        case 3:
                            endDrag = true;
                            if (endDrag) {
                                synchronized (WindowManagerService.this.mWindowMap) {
                                    WindowManagerService.this.mDragState.endDragLw();
                                }
                                this.mStylusButtonDownAtStart = false;
                                this.mIsStartEvent = true;
                            }
                            handled = true;
                            break;
                    }
                    if (endDrag) {
                        synchronized (WindowManagerService.this.mWindowMap) {
                            WindowManagerService.this.mDragState.endDragLw();
                        }
                        this.mStylusButtonDownAtStart = false;
                        this.mIsStartEvent = true;
                    }
                    handled = true;
                }
                finishInputEvent(event, handled);
            } catch (Exception e) {
                Slog.e("WindowManager", "Exception caught by drag handleMotion", e);
                finishInputEvent(event, false);
            } catch (Throwable th) {
                finishInputEvent(event, false);
            }
        }
    }

    public final class H extends Handler {
        public static final int ADD_STARTING = 5;
        public static final int ALL_WINDOWS_DRAWN = 33;
        public static final int APP_FREEZE_TIMEOUT = 17;
        public static final int APP_TRANSITION_TIMEOUT = 13;
        public static final int BOOT_TIMEOUT = 23;
        public static final int CHECK_IF_BOOT_ANIMATION_FINISHED = 37;
        public static final int CLIENT_FREEZE_TIMEOUT = 30;
        public static final int DO_ANIMATION_CALLBACK = 26;
        public static final int DO_DISPLAY_ADDED = 27;
        public static final int DO_DISPLAY_CHANGED = 29;
        public static final int DO_DISPLAY_REMOVED = 28;
        public static final int DO_TRAVERSAL = 4;
        public static final int DRAG_END_TIMEOUT = 21;
        public static final int DRAG_START_TIMEOUT = 20;
        public static final int ENABLE_SCREEN = 16;
        public static final int FINISHED_STARTING = 7;
        public static final int FINISH_TASK_POSITIONING = 40;
        public static final int FORCE_GC = 15;
        public static final int KEYGUARD_DISMISS_DONE = 101;
        public static final int NEW_ANIMATOR_SCALE = 34;
        public static final int NOTIFY_ACTIVITY_DRAWN = 32;
        public static final int NOTIFY_APP_TRANSITION_CANCELLED = 48;
        public static final int NOTIFY_APP_TRANSITION_FINISHED = 49;
        public static final int NOTIFY_APP_TRANSITION_STARTING = 47;
        public static final int NOTIFY_DOCKED_STACK_MINIMIZED_CHANGED = 53;
        public static final int NOTIFY_STARTING_WINDOW_DRAWN = 50;
        public static final int PERSIST_ANIMATION_SCALE = 14;
        public static final int REMOVE_STARTING = 6;
        public static final int REPORT_APPLICATION_TOKEN_DRAWN = 9;
        public static final int REPORT_APPLICATION_TOKEN_WINDOWS = 8;
        public static final int REPORT_FOCUS_CHANGE = 2;
        public static final int REPORT_HARD_KEYBOARD_STATUS_CHANGE = 22;
        public static final int REPORT_LOSING_FOCUS = 3;
        public static final int REPORT_WINDOWS_CHANGE = 19;
        public static final int RESET_ANR_MESSAGE = 38;
        public static final int RESIZE_STACK = 42;
        public static final int RESIZE_TASK = 43;
        public static final int SEND_NEW_CONFIGURATION = 18;
        public static final int SHOW_CIRCULAR_DISPLAY_MASK = 35;
        public static final int SHOW_EMULATOR_DISPLAY_OVERLAY = 36;
        public static final int SHOW_STRICT_MODE_VIOLATION = 25;
        public static final int TAP_OUTSIDE_TASK = 31;
        public static final int TWO_FINGER_SCROLL_START = 44;
        public static final int UNUSED = 0;
        public static final int UPDATE_ANIMATION_SCALE = 51;
        public static final int UPDATE_DOCKED_STACK_DIVIDER = 41;
        public static final int WAITING_FOR_DRAWN_TIMEOUT = 24;
        public static final int WAIT_KEYGUARD_DISMISS_DONE_TIMEOUT = 100;
        public static final int WALLPAPER_DRAW_PENDING_TIMEOUT = 39;
        public static final int WINDOW_FREEZE_TIMEOUT = 11;
        public static final int WINDOW_REMOVE_TIMEOUT = 52;
        public static final int WINDOW_REPLACEMENT_TIMEOUT = 46;

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            int i;
            int N;
            HashMap hashMap;
            AppWindowToken wtoken;
            View view;
            IBinder iBinder;
            IBinder win;
            Runnable callback;
            switch (msg.what) {
                case 2:
                    AccessibilityController accessibilityController = null;
                    synchronized (WindowManagerService.this.mWindowMap) {
                        if (WindowManagerService.this.mAccessibilityController != null && WindowManagerService.this.getDefaultDisplayContentLocked().getDisplayId() == 0) {
                            accessibilityController = WindowManagerService.this.mAccessibilityController;
                        }
                        WindowState lastFocus = WindowManagerService.this.mLastFocus;
                        WindowState newFocus = WindowManagerService.this.mCurrentFocus;
                        if (lastFocus != newFocus) {
                            WindowManagerService.this.mLastFocus = newFocus;
                            if (!(newFocus == null || lastFocus == null || newFocus.isDisplayedLw())) {
                                WindowManagerService.this.mLosingFocus.add(lastFocus);
                                lastFocus = null;
                                break;
                            }
                        }
                        return;
                    }
                case 3:
                    ArrayList<WindowState> losers;
                    synchronized (WindowManagerService.this.mWindowMap) {
                        losers = WindowManagerService.this.mLosingFocus;
                        WindowManagerService.this.mLosingFocus = new ArrayList();
                    }
                    N = losers.size();
                    for (i = 0; i < N; i++) {
                        ((WindowState) losers.get(i)).reportFocusChangedSerialized(false, WindowManagerService.this.mInTouchMode);
                    }
                    break;
                case 4:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                        break;
                    }
                case 5:
                    wtoken = msg.obj;
                    StartingData sd = wtoken.startingData;
                    Flog.i(301, "Add starting wtoken: " + wtoken + " sd= " + sd);
                    if (sd != null) {
                        Configuration configuration;
                        boolean abort;
                        view = null;
                        if (wtoken != null) {
                            try {
                                if (wtoken.mTask != null) {
                                    configuration = wtoken.mTask.mOverrideConfig;
                                    view = WindowManagerService.this.mPolicy.addStartingWindow(wtoken.token, sd.pkg, sd.theme, sd.compatInfo, sd.nonLocalizedLabel, sd.labelRes, sd.icon, sd.logo, sd.windowFlags, configuration);
                                    if (view != null) {
                                        abort = false;
                                        synchronized (WindowManagerService.this.mWindowMap) {
                                            if (wtoken.removed && wtoken.startingData != null) {
                                                wtoken.startingView = view;
                                            } else if (wtoken.startingWindow != null) {
                                                wtoken.startingWindow = null;
                                                wtoken.startingData = null;
                                                abort = true;
                                            }
                                        }
                                        if (abort) {
                                            try {
                                                WindowManagerService.this.mPolicy.removeStartingWindow(wtoken.token, view);
                                                break;
                                            } catch (Throwable e) {
                                                Slog.w("WindowManager", "Exception when removing starting window", e);
                                                break;
                                            }
                                        }
                                    }
                                }
                            } catch (Throwable e2) {
                                Slog.w("WindowManager", "Exception when adding starting window", e2);
                            }
                        }
                        configuration = null;
                        view = WindowManagerService.this.mPolicy.addStartingWindow(wtoken.token, sd.pkg, sd.theme, sd.compatInfo, sd.nonLocalizedLabel, sd.labelRes, sd.icon, sd.logo, sd.windowFlags, configuration);
                        if (view != null) {
                            abort = false;
                            synchronized (WindowManagerService.this.mWindowMap) {
                                if (wtoken.removed) {
                                    break;
                                }
                                if (wtoken.startingWindow != null) {
                                    wtoken.startingWindow = null;
                                    wtoken.startingData = null;
                                    abort = true;
                                }
                            }
                            if (abort) {
                                WindowManagerService.this.mPolicy.removeStartingWindow(wtoken.token, view);
                            }
                        }
                    } else {
                        return;
                    }
                    break;
                case 6:
                    wtoken = (AppWindowToken) msg.obj;
                    iBinder = null;
                    view = null;
                    synchronized (WindowManagerService.this.mWindowMap) {
                        Flog.i(301, "Remove starting " + wtoken + ": startingWindow=" + wtoken.startingWindow + " startingView=" + wtoken.startingView);
                        if (wtoken.startingWindow != null) {
                            view = wtoken.startingView;
                            iBinder = wtoken.token;
                            wtoken.startingData = null;
                            wtoken.startingView = null;
                            wtoken.startingWindow = null;
                            wtoken.startingDisplayed = false;
                        }
                    }
                    if (view != null) {
                        try {
                            WindowManagerService.this.mPolicy.removeStartingWindow(iBinder, view);
                            break;
                        } catch (Throwable e22) {
                            Slog.w("WindowManager", "Exception when removing starting window", e22);
                            break;
                        }
                    }
                    break;
                case 7:
                    while (true) {
                        hashMap = WindowManagerService.this.mWindowMap;
                        synchronized (hashMap) {
                            N = WindowManagerService.this.mFinishedStarting.size();
                            if (N <= 0) {
                                break;
                            }
                            wtoken = (AppWindowToken) WindowManagerService.this.mFinishedStarting.remove(N - 1);
                            Flog.i(301, "Finished starting " + wtoken + ": startingWindow=" + wtoken.startingWindow + " startingView=" + wtoken.startingView);
                            if (wtoken.startingWindow == null) {
                            } else {
                                view = wtoken.startingView;
                                iBinder = wtoken.token;
                                wtoken.startingData = null;
                                wtoken.startingView = null;
                                wtoken.startingWindow = null;
                                wtoken.startingDisplayed = false;
                                try {
                                    WindowManagerService.this.mPolicy.removeStartingWindow(iBinder, view);
                                } catch (Throwable e222) {
                                    Slog.w("WindowManager", "Exception when removing starting window", e222);
                                }
                            }
                        }
                    }
                case 8:
                    wtoken = (AppWindowToken) msg.obj;
                    boolean nowVisible = msg.arg1 != 0;
                    if (msg.arg2 != 0) {
                    }
                    if (!nowVisible) {
                        wtoken.appToken.windowsGone();
                        break;
                    }
                    try {
                        wtoken.appToken.windowsVisible();
                        break;
                    } catch (RemoteException e3) {
                        break;
                    }
                case 9:
                    try {
                        ((AppWindowToken) msg.obj).appToken.windowsDrawn();
                        break;
                    } catch (RemoteException e4) {
                        break;
                    }
                case 11:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        Slog.w("WindowManager", "Window freeze timeout expired.");
                        WindowManagerService.this.mWindowsFreezingScreen = 2;
                        WindowList windows = WindowManagerService.this.getDefaultWindowListLocked();
                        i = windows.size();
                        while (i > 0) {
                            i--;
                            WindowState w = (WindowState) windows.get(i);
                            if (w.mOrientationChanging) {
                                w.mOrientationChanging = false;
                                w.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - WindowManagerService.this.mDisplayFreezeTime);
                                Slog.w("WindowManager", "Force clearing orientation change: " + w);
                            }
                        }
                        WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                        break;
                    }
                case 13:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        if (!(!WindowManagerService.this.mAppTransition.isTransitionSet() && WindowManagerService.this.mOpeningApps.isEmpty() && WindowManagerService.this.mClosingApps.isEmpty())) {
                            Slog.w("WindowManager", "*** APP TRANSITION TIMEOUT. isTransitionSet()=" + WindowManagerService.this.mAppTransition.isTransitionSet() + " mOpeningApps.size()=" + WindowManagerService.this.mOpeningApps.size() + " mClosingApps.size()=" + WindowManagerService.this.mClosingApps.size());
                            WindowManagerService.this.mAppTransition.setTimeout();
                            N = WindowManagerService.this.mOpeningApps.size();
                            for (i = 0; i < N; i++) {
                                AppWindowToken appToken = (AppWindowToken) WindowManagerService.this.mOpeningApps.valueAt(i);
                                appToken.mPendingRelaunchCount = 0;
                                appToken.mFrozenBounds.clear();
                                appToken.mFrozenMergedConfig.clear();
                            }
                            WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                            break;
                        }
                    }
                case 14:
                    Global.putFloat(WindowManagerService.this.mContext.getContentResolver(), "window_animation_scale", WindowManagerService.this.mWindowAnimationScaleSetting);
                    Global.putFloat(WindowManagerService.this.mContext.getContentResolver(), "transition_animation_scale", WindowManagerService.this.mTransitionAnimationScaleSetting);
                    Global.putFloat(WindowManagerService.this.mContext.getContentResolver(), "animator_duration_scale", WindowManagerService.this.mAnimatorDurationScaleSetting);
                    break;
                case 15:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        if (WindowManagerService.this.mAnimator.isAnimating() || WindowManagerService.this.mAnimationScheduled) {
                            sendEmptyMessageDelayed(15, 2000);
                            return;
                        } else if (WindowManagerService.this.mDisplayFrozen) {
                            return;
                        }
                    }
                    break;
                case 16:
                    WindowManagerService.this.performEnableScreen();
                    break;
                case 17:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        Slog.w("WindowManager", "App freeze timeout expired.");
                        WindowManagerService.this.mWindowsFreezingScreen = 2;
                        int numStacks = WindowManagerService.this.mStackIdToStack.size();
                        for (int stackNdx = 0; stackNdx < numStacks; stackNdx++) {
                            ArrayList<Task> tasks = ((TaskStack) WindowManagerService.this.mStackIdToStack.valueAt(stackNdx)).getTasks();
                            for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                                AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
                                for (int tokenNdx = tokens.size() - 1; tokenNdx >= 0; tokenNdx--) {
                                    AppWindowToken tok = (AppWindowToken) tokens.get(tokenNdx);
                                    if (tok.mAppAnimator.freezingScreen) {
                                        Slog.w("WindowManager", "Force clearing freeze: " + tok);
                                        WindowManagerService.this.unsetAppFreezingScreenLocked(tok, true, true);
                                    }
                                }
                            }
                        }
                        break;
                    }
                case 18:
                    removeMessages(18);
                    WindowManagerService.this.sendNewConfiguration();
                    break;
                case 19:
                    if (WindowManagerService.this.mWindowsChanged) {
                        synchronized (WindowManagerService.this.mWindowMap) {
                            WindowManagerService.this.mWindowsChanged = false;
                        }
                        WindowManagerService.this.notifyWindowsChanged();
                        break;
                    }
                    break;
                case 20:
                    win = msg.obj;
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        if (WindowManagerService.this.mDragState != null) {
                            WindowManagerService.this.mDragState.unregister();
                            WindowManagerService.this.mInputMonitor.updateInputWindowsLw(true);
                            WindowManagerService.this.mDragState.reset();
                            WindowManagerService.this.mDragState = null;
                            break;
                        }
                    }
                    break;
                case DRAG_END_TIMEOUT /*21*/:
                    win = (IBinder) msg.obj;
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        if (WindowManagerService.this.mDragState != null) {
                            WindowManagerService.this.mDragState.mDragResult = false;
                            WindowManagerService.this.mDragState.endDragLw();
                            break;
                        }
                    }
                    break;
                case REPORT_HARD_KEYBOARD_STATUS_CHANGE /*22*/:
                    WindowManagerService.this.notifyHardKeyboardStatusChange();
                    break;
                case BOOT_TIMEOUT /*23*/:
                    WindowManagerService.this.performBootTimeout();
                    break;
                case WAITING_FOR_DRAWN_TIMEOUT /*24*/:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        Flog.i(NativeResponseCode.SERVICE_FOUND, "Timeout waiting for drawn: undrawn=" + WindowManagerService.this.mWaitingForDrawn);
                        WindowManagerService.this.mWaitingForDrawn.clear();
                        callback = WindowManagerService.this.mWaitingForDrawnCallback;
                        WindowManagerService.this.mWaitingForDrawnCallback = null;
                    }
                    if (callback != null) {
                        callback.run();
                        break;
                    }
                    break;
                case 25:
                    WindowManagerService.this.showStrictModeViolation(msg.arg1, msg.arg2);
                    break;
                case DO_ANIMATION_CALLBACK /*26*/:
                    try {
                        ((IRemoteCallback) msg.obj).sendResult(null);
                        break;
                    } catch (RemoteException e5) {
                        break;
                    }
                case DO_DISPLAY_ADDED /*27*/:
                    WindowManagerService.this.handleDisplayAdded(msg.arg1);
                    break;
                case DO_DISPLAY_REMOVED /*28*/:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        WindowManagerService.this.handleDisplayRemovedLocked(msg.arg1);
                        break;
                    }
                case 29:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        WindowManagerService.this.handleDisplayChangedLocked(msg.arg1);
                        break;
                    }
                case 30:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        if (WindowManagerService.this.mClientFreezingScreen) {
                            WindowManagerService.this.mClientFreezingScreen = false;
                            WindowManagerService.this.mLastFinishedFreezeSource = "client-timeout";
                            WindowManagerService.this.stopFreezingDisplayLocked();
                            break;
                        }
                    }
                    break;
                case 31:
                    WindowManagerService.this.handleTapOutsideTask((DisplayContent) msg.obj, msg.arg1, msg.arg2);
                    break;
                case 32:
                    try {
                        WindowManagerService.this.mActivityManager.notifyActivityDrawn((IBinder) msg.obj);
                        break;
                    } catch (RemoteException e6) {
                        break;
                    }
                case 33:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        callback = WindowManagerService.this.mWaitingForDrawnCallback;
                        WindowManagerService.this.mWaitingForDrawnCallback = null;
                    }
                    if (callback != null) {
                        callback.run();
                        break;
                    }
                    break;
                case 34:
                    break;
                case 35:
                    WindowManagerService.this.showCircularMask(msg.arg1 == 1);
                    break;
                case 36:
                    WindowManagerService.this.showEmulatorDisplayOverlay();
                    break;
                case 37:
                    boolean bootAnimationComplete;
                    synchronized (WindowManagerService.this.mWindowMap) {
                        bootAnimationComplete = WindowManagerService.this.checkBootAnimationCompleteLocked();
                    }
                    if (bootAnimationComplete) {
                        WindowManagerService.this.performEnableScreen();
                        break;
                    }
                    break;
                case 38:
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        WindowManagerService.this.mLastANRState = null;
                        break;
                    }
                case 39:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        if (WindowManagerService.this.mWallpaperControllerLocked.processWallpaperDrawPendingTimeout()) {
                            WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                        }
                    }
                case 40:
                    WindowManagerService.this.finishPositioning();
                    break;
                case 41:
                    break;
                case 42:
                    try {
                        WindowManagerService.this.mActivityManager.resizeStack(msg.arg1, (Rect) msg.obj, msg.arg2 == 1, false, false, -1);
                        break;
                    } catch (RemoteException e7) {
                        break;
                    }
                case 43:
                    try {
                        WindowManagerService.this.mActivityManager.resizeTask(msg.arg1, (Rect) msg.obj, msg.arg2);
                        break;
                    } catch (RemoteException e8) {
                        break;
                    }
                case 44:
                    WindowManagerService.this.startScrollingTask((DisplayContent) msg.obj, msg.arg1, msg.arg2);
                    break;
                case WINDOW_REPLACEMENT_TIMEOUT /*46*/:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        for (i = WindowManagerService.this.mReplacingWindowTimeouts.size() - 1; i >= 0; i--) {
                            ((AppWindowToken) WindowManagerService.this.mReplacingWindowTimeouts.get(i)).clearTimedoutReplacesLocked();
                        }
                        WindowManagerService.this.mReplacingWindowTimeouts.clear();
                    }
                case 47:
                    break;
                case 48:
                    WindowManagerService.this.mAmInternal.notifyAppTransitionCancelled();
                    break;
                case 49:
                    WindowManagerService.this.mAmInternal.notifyAppTransitionFinished();
                    break;
                case 50:
                    WindowManagerService.this.mAmInternal.notifyStartingWindowDrawn();
                    break;
                case 51:
                    switch (msg.arg1) {
                        case 0:
                            WindowManagerService.this.mWindowAnimationScaleSetting = Global.getFloat(WindowManagerService.this.mContext.getContentResolver(), "window_animation_scale", WindowManagerService.this.mWindowAnimationScaleSetting);
                            break;
                        case 1:
                            WindowManagerService.this.mTransitionAnimationScaleSetting = Global.getFloat(WindowManagerService.this.mContext.getContentResolver(), "transition_animation_scale", WindowManagerService.this.mTransitionAnimationScaleSetting);
                            break;
                        case 2:
                            WindowManagerService.this.mAnimatorDurationScaleSetting = Global.getFloat(WindowManagerService.this.mContext.getContentResolver(), "animator_duration_scale", WindowManagerService.this.mAnimatorDurationScaleSetting);
                            WindowManagerService.this.dispatchNewAnimatorScaleLocked(null);
                            break;
                        default:
                            break;
                    }
                case 52:
                    WindowState window = msg.obj;
                    hashMap = WindowManagerService.this.mWindowMap;
                    synchronized (hashMap) {
                        LayoutParams layoutParams = window.mAttrs;
                        layoutParams.flags &= -129;
                        window.setDisplayLayoutNeeded();
                        WindowManagerService.this.mWindowPlacerLocked.performSurfacePlacement();
                        break;
                    }
                case 53:
                    WindowManagerService.this.mAmInternal.notifyDockedStackMinimizedChanged(msg.arg1 == 1);
                    break;
                case 100:
                case 101:
                    synchronized (WindowManagerService.this.mWindowMap) {
                        callback = WindowManagerService.this.mKeyguardDismissDoneCallback;
                        WindowManagerService.this.mKeyguardDismissDoneCallback = null;
                        WindowManagerService.this.mKeyguardWin = null;
                        WindowManagerService.this.mTopWallpaperWin = null;
                    }
                    if (callback != null) {
                        callback.run();
                        break;
                    }
                    break;
            }
            i++;
        }
    }

    private static final class HideNavInputConsumer extends InputConsumerImpl implements InputConsumer {
        private final InputEventReceiver mInputEventReceiver;

        HideNavInputConsumer(WindowManagerService service, Looper looper, Factory inputEventReceiverFactory) {
            super(service, "input consumer", null);
            this.mInputEventReceiver = inputEventReceiverFactory.createInputEventReceiver(this.mClientChannel, looper);
        }

        public void dismiss() {
            if (this.mService.removeInputConsumer()) {
                synchronized (this.mService.mWindowMap) {
                    this.mInputEventReceiver.dispose();
                    disposeChannelsLw();
                }
            }
        }
    }

    private final class LocalService extends WindowManagerInternal {
        private LocalService() {
        }

        public void requestTraversalFromDisplayManager() {
            WindowManagerService.this.requestTraversal();
        }

        public void setMagnificationSpec(MagnificationSpec spec) {
            synchronized (WindowManagerService.this.mWindowMap) {
                if (WindowManagerService.this.mAccessibilityController != null) {
                    WindowManagerService.this.mAccessibilityController.setMagnificationSpecLocked(spec);
                } else {
                    throw new IllegalStateException("Magnification callbacks not set!");
                }
            }
            if (Binder.getCallingPid() != Process.myPid()) {
                spec.recycle();
            }
        }

        public void getMagnificationRegion(Region magnificationRegion) {
            synchronized (WindowManagerService.this.mWindowMap) {
                if (WindowManagerService.this.mAccessibilityController != null) {
                    WindowManagerService.this.mAccessibilityController.getMagnificationRegionLocked(magnificationRegion);
                } else {
                    throw new IllegalStateException("Magnification callbacks not set!");
                }
            }
        }

        public MagnificationSpec getCompatibleMagnificationSpecForWindow(IBinder windowToken) {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowState windowState = (WindowState) WindowManagerService.this.mWindowMap.get(windowToken);
                if (windowState == null) {
                    return null;
                }
                MagnificationSpec spec = null;
                if (WindowManagerService.this.mAccessibilityController != null) {
                    spec = WindowManagerService.this.mAccessibilityController.getMagnificationSpecForWindowLocked(windowState);
                }
                if ((spec == null || spec.isNop()) && windowState.mGlobalScale == 1.0f) {
                    return null;
                }
                spec = spec == null ? MagnificationSpec.obtain() : MagnificationSpec.obtain(spec);
                spec.scale *= windowState.mGlobalScale;
                return spec;
            }
        }

        public void setMagnificationCallbacks(MagnificationCallbacks callbacks) {
            synchronized (WindowManagerService.this.mWindowMap) {
                if (WindowManagerService.this.mAccessibilityController == null) {
                    WindowManagerService.this.mAccessibilityController = new AccessibilityController(WindowManagerService.this);
                }
                WindowManagerService.this.mAccessibilityController.setMagnificationCallbacksLocked(callbacks);
                if (!WindowManagerService.this.mAccessibilityController.hasCallbacksLocked()) {
                    WindowManagerService.this.mAccessibilityController = null;
                }
            }
        }

        public void setWindowsForAccessibilityCallback(WindowsForAccessibilityCallback callback) {
            synchronized (WindowManagerService.this.mWindowMap) {
                if (WindowManagerService.this.mAccessibilityController == null) {
                    WindowManagerService.this.mAccessibilityController = new AccessibilityController(WindowManagerService.this);
                }
                WindowManagerService.this.mAccessibilityController.setWindowsForAccessibilityCallback(callback);
                if (!WindowManagerService.this.mAccessibilityController.hasCallbacksLocked()) {
                    WindowManagerService.this.mAccessibilityController = null;
                }
            }
        }

        public void setInputFilter(IInputFilter filter) {
            WindowManagerService.this.mInputManager.setInputFilter(filter);
        }

        public IBinder getFocusedWindowToken() {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowState windowState = WindowManagerService.this.getFocusedWindowLocked();
                if (windowState != null) {
                    IBinder asBinder = windowState.mClient.asBinder();
                    return asBinder;
                }
                return null;
            }
        }

        public boolean isKeyguardLocked() {
            return WindowManagerService.this.isKeyguardLocked();
        }

        public void showGlobalActions() {
            WindowManagerService.this.showGlobalActions();
        }

        public void getWindowFrame(IBinder token, Rect outBounds) {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowState windowState = (WindowState) WindowManagerService.this.mWindowMap.get(token);
                if (windowState != null) {
                    outBounds.set(windowState.mFrame);
                } else {
                    outBounds.setEmpty();
                }
            }
        }

        public void waitForAllWindowsDrawn(Runnable callback, long timeout) {
            boolean allWindowsDrawn = false;
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowManagerService.this.mWaitingForDrawnCallback = callback;
                WindowList windows = WindowManagerService.this.getDefaultWindowListLocked();
                int winNdx;
                WindowState win;
                boolean isForceHiding;
                if (WindowManagerService.this.isCoverOpen()) {
                    Flog.i(NativeResponseCode.SERVICE_FOUND, "waitForAllWindowsDrawn  cover is open or null");
                    for (winNdx = windows.size() - 1; winNdx >= 0; winNdx--) {
                        win = (WindowState) windows.get(winNdx);
                        isForceHiding = WindowManagerService.this.mPolicy.isForceHiding(win.mAttrs);
                        if (win.isVisibleLw() && (win.mAppToken != null || isForceHiding)) {
                            win.mWinAnimator.mDrawState = 1;
                            win.mLastContentInsets.set(-1, -1, -1, -1);
                            WindowManagerService.this.mWaitingForDrawn.add(win);
                            if (isForceHiding) {
                                break;
                            }
                        }
                    }
                } else {
                    Flog.i(NativeResponseCode.SERVICE_FOUND, "waitForAllWindowsDrawn  cover is close");
                    for (winNdx = windows.size() - 1; winNdx >= 0; winNdx--) {
                        win = (WindowState) windows.get(winNdx);
                        isForceHiding = WindowManagerService.this.mPolicy.isForceHiding(win.mAttrs);
                        if ((win.isVisibleLw() && (win.mAppToken != null || isForceHiding)) || win.mAttrs.type == 2100 || win.mAttrs.type == 2101) {
                            win.mWinAnimator.mDrawState = 1;
                            win.mLastContentInsets.set(-1, -1, -1, -1);
                            WindowManagerService.this.mWaitingForDrawn.add(win);
                            if (isForceHiding) {
                                break;
                            }
                        }
                    }
                }
                WindowManagerService.this.mWindowPlacerLocked.requestTraversal();
                WindowManagerService.this.mH.removeMessages(24);
                if (WindowManagerService.this.mWaitingForDrawn.isEmpty()) {
                    allWindowsDrawn = true;
                } else {
                    WindowManagerService.this.mH.sendEmptyMessageDelayed(24, timeout);
                    WindowManagerService.this.checkDrawnWindowsLocked();
                }
            }
            if (allWindowsDrawn) {
                callback.run();
                return;
            }
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowManagerService.this.mH.sendEmptyMessageDelayed(24, timeout);
                WindowManagerService.this.checkDrawnWindowsLocked();
            }
        }

        public void addWindowToken(IBinder token, int type) {
            WindowManagerService.this.addWindowToken(token, type);
        }

        public void removeWindowToken(IBinder token, boolean removeWindows) {
            synchronized (WindowManagerService.this.mWindowMap) {
                if (removeWindows) {
                    WindowToken wtoken = (WindowToken) WindowManagerService.this.mTokenMap.remove(token);
                    if (wtoken != null) {
                        wtoken.removeAllWindows();
                    }
                }
                WindowManagerService.this.removeWindowToken(token);
            }
        }

        public void registerAppTransitionListener(AppTransitionListener listener) {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowManagerService.this.mAppTransition.registerListenerLocked(listener);
            }
        }

        public int getInputMethodWindowVisibleHeight() {
            int inputMethodWindowVisibleHeightLw;
            synchronized (WindowManagerService.this.mWindowMap) {
                inputMethodWindowVisibleHeightLw = WindowManagerService.this.mPolicy.getInputMethodWindowVisibleHeightLw();
            }
            return inputMethodWindowVisibleHeightLw;
        }

        public void saveLastInputMethodWindowForTransition() {
            synchronized (WindowManagerService.this.mWindowMap) {
                if (WindowManagerService.this.mInputMethodWindow != null) {
                    WindowManagerService.this.mPolicy.setLastInputMethodWindowLw(WindowManagerService.this.mInputMethodWindow, WindowManagerService.this.mInputMethodTarget);
                }
            }
        }

        public void clearLastInputMethodWindowForTransition() {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowManagerService.this.mPolicy.setLastInputMethodWindowLw(null, null);
            }
        }

        public boolean isHardKeyboardAvailable() {
            boolean z;
            synchronized (WindowManagerService.this.mWindowMap) {
                z = WindowManagerService.this.mHardKeyboardAvailable;
            }
            return z;
        }

        public void setOnHardKeyboardStatusChangeListener(OnHardKeyboardStatusChangeListener listener) {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowManagerService.this.mHardKeyboardStatusChangeListener = listener;
            }
        }

        public boolean isStackVisible(int stackId) {
            boolean isStackVisibleLocked;
            synchronized (WindowManagerService.this.mWindowMap) {
                isStackVisibleLocked = WindowManagerService.this.isStackVisibleLocked(stackId);
            }
            return isStackVisibleLocked;
        }

        public boolean isDockedDividerResizing() {
            boolean isResizing;
            synchronized (WindowManagerService.this.mWindowMap) {
                isResizing = WindowManagerService.this.getDefaultDisplayContentLocked().getDockedDividerController().isResizing();
            }
            return isResizing;
        }

        public void waitForKeyguardDismissDone(Runnable callback, long timeout) {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowManagerService.this.mKeyguardDismissDoneCallback = callback;
                WindowList windows = WindowManagerService.this.getDefaultWindowListLocked();
                for (int winNdx = windows.size() - 1; winNdx >= 0; winNdx--) {
                    WindowState win = (WindowState) windows.get(winNdx);
                    if (win.mAttrs.type == 2004) {
                        boolean z;
                        WindowManagerService.this.mKeyguardWin = win;
                        WindowManagerService windowManagerService = WindowManagerService.this;
                        if ((WindowManagerService.this.mKeyguardWin.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                            z = true;
                        } else {
                            z = false;
                        }
                        windowManagerService.mKeyguardAttachWallpaper = z;
                    }
                    if (win.mAttrs.type == 2013 && WindowManagerService.this.mTopWallpaperWin == null) {
                        WindowManagerService.this.mTopWallpaperWin = win;
                        WindowManagerService.this.mTopWallpaperAnimLayer = WindowManagerService.this.mTopWallpaperWin.mWinAnimator.mAnimLayer;
                    }
                }
                WindowManagerService.this.mWindowPlacerLocked.requestTraversal();
                boolean iskeyguardWindNull = WindowManagerService.this.mKeyguardWin == null;
            }
            WindowManagerService.this.mH.removeMessages(100);
            if (WindowManagerService.this.mPolicy.isStatusBarKeyguardShowing() || !iskeyguardWindNull) {
                WindowManagerService.this.mH.sendEmptyMessageDelayed(100, timeout);
                return;
            }
            Slog.i(WindowManagerService.TAG, "waitForKeyguardDismissDone there is no keyguard.");
            callback.run();
        }

        public void setDockedStackDividerRotation(int rotation) {
            synchronized (WindowManagerService.this.mWindowMap) {
                WindowManagerService.this.getDefaultDisplayContentLocked().getDockedDividerController().setDockedStackDividerRotation(rotation);
            }
        }
    }

    private static class MousePositionTracker implements PointerEventListener {
        private boolean mLatestEventWasMouse;
        private float mLatestMouseX;
        private float mLatestMouseY;

        private MousePositionTracker() {
        }

        void updatePosition(float x, float y) {
            synchronized (this) {
                this.mLatestEventWasMouse = true;
                this.mLatestMouseX = x;
                this.mLatestMouseY = y;
            }
        }

        public void onPointerEvent(MotionEvent motionEvent) {
            if (motionEvent.isFromSource(8194)) {
                updatePosition(motionEvent.getRawX(), motionEvent.getRawY());
                return;
            }
            synchronized (this) {
                this.mLatestEventWasMouse = false;
            }
        }
    }

    class RotationWatcher {
        DeathRecipient deathRecipient;
        IRotationWatcher watcher;

        RotationWatcher(IRotationWatcher w, DeathRecipient d) {
            this.watcher = w;
            this.deathRecipient = d;
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri mAnimationDurationScaleUri = Global.getUriFor("animator_duration_scale");
        private final Uri mDisplayInversionEnabledUri = Secure.getUriFor("accessibility_display_inversion_enabled");
        private final Uri mTransitionAnimationScaleUri = Global.getUriFor("transition_animation_scale");
        private final Uri mWindowAnimationScaleUri = Global.getUriFor("window_animation_scale");

        public SettingsObserver() {
            super(new Handler());
            ContentResolver resolver = WindowManagerService.this.mContext.getContentResolver();
            resolver.registerContentObserver(this.mDisplayInversionEnabledUri, false, this, -1);
            resolver.registerContentObserver(this.mWindowAnimationScaleUri, false, this, -1);
            resolver.registerContentObserver(this.mTransitionAnimationScaleUri, false, this, -1);
            resolver.registerContentObserver(this.mAnimationDurationScaleUri, false, this, -1);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (uri != null) {
                if (this.mDisplayInversionEnabledUri.equals(uri)) {
                    WindowManagerService.this.updateCircularDisplayMaskIfNeeded();
                } else {
                    int mode;
                    if (this.mWindowAnimationScaleUri.equals(uri)) {
                        mode = 0;
                    } else if (this.mTransitionAnimationScaleUri.equals(uri)) {
                        mode = 1;
                    } else if (this.mAnimationDurationScaleUri.equals(uri)) {
                        mode = 2;
                    } else {
                        return;
                    }
                    WindowManagerService.this.mH.sendMessage(WindowManagerService.this.mH.obtainMessage(51, mode, 0));
                }
            }
        }
    }

    @IntDef({0, 1, 2})
    @Retention(RetentionPolicy.SOURCE)
    private @interface UpdateAnimationScaleMode {
    }

    boolean updateStatusBarVisibilityLocked(int r1) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.WindowManagerService.updateStatusBarVisibilityLocked(int):boolean
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.WindowManagerService.updateStatusBarVisibilityLocked(int):boolean");
    }

    int getDragLayerLocked() {
        return (this.mPolicy.windowTypeToLayerLw(2016) * 10000) + 1000;
    }

    public static WindowManagerService main(Context context, InputManagerService im, boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore) {
        final WindowManagerService[] holder = new WindowManagerService[1];
        final Context context2 = context;
        final InputManagerService inputManagerService = im;
        final boolean z = haveInputMethods;
        final boolean z2 = showBootMsgs;
        final boolean z3 = onlyCore;
        DisplayThread.getHandler().runWithScissors(new Runnable() {
            public void run() {
                IHwWindowManagerService iwms = HwServiceFactory.getHuaweiWindowManagerService();
                if (iwms != null) {
                    holder[0] = iwms.getInstance(context2, inputManagerService, z, z2, z3);
                } else {
                    holder[0] = new WindowManagerService(context2, inputManagerService, z, z2, z3);
                }
            }
        }, 0);
        return holder[0];
    }

    private void initPolicy() {
        UiThread.getHandler().runWithScissors(new Runnable() {
            public void run() {
                WindowManagerPolicyThread.set(Thread.currentThread(), Looper.myLooper());
                WindowManagerService.this.mPolicy.init(WindowManagerService.this.mContext, WindowManagerService.this, WindowManagerService.this);
            }
        }, 0);
    }

    public boolean getAccelPackages(String pkg) {
        Boolean pkgIn = (Boolean) this.mPackages.get(pkg);
        return Boolean.valueOf(pkgIn != null ? pkgIn.booleanValue() : false).booleanValue();
    }

    private void initAccelPackages() {
        File systemDir = new File(Environment.getRootDirectory(), "etc");
        if (systemDir.mkdirs()) {
            Slog.w(TAG, "system/etc dir is not exist! Creat");
        }
        FileInputStream fileInputStream = null;
        XmlPullParser xmlPullParser = null;
        try {
            fileInputStream = new AtomicFile(new File(systemDir, "accelpackages.xml")).openRead();
            xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(fileInputStream, null);
            while (true) {
                XmlUtils.nextElement(xmlPullParser);
                String element = xmlPullParser.getName();
                if (element == null) {
                    break;
                } else if (element.equals(AbsLocationManagerService.DEL_PKG)) {
                    String pkg = xmlPullParser.getAttributeValue(null, "name");
                    if (pkg != null) {
                        this.mPackages.put(pkg, Boolean.valueOf(true));
                    }
                }
            }
            if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                ((XmlResourceParser) xmlPullParser).close();
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                }
            }
        } catch (XmlPullParserException e2) {
            Slog.w(TAG, "Error parse accelpackages.xml", e2);
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e3) {
                }
            }
            if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                ((XmlResourceParser) xmlPullParser).close();
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e4) {
                }
            }
        } catch (IOException e5) {
            if (fileInputStream == null) {
                Slog.w(TAG, "Error reading accelpackages.xml", e5);
            }
            if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                ((XmlResourceParser) xmlPullParser).close();
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e6) {
                }
            }
        } catch (Throwable th) {
            if (xmlPullParser != null && (xmlPullParser instanceof XmlResourceParser)) {
                ((XmlResourceParser) xmlPullParser).close();
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e7) {
                }
            }
        }
    }

    protected WindowManagerService(Context context, InputManagerService inputManager, boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore) {
        this.mContext = context;
        this.mHaveInputMethods = haveInputMethods;
        this.mAllowBootMessages = showBootMsgs;
        this.mOnlyCore = onlyCore;
        this.mLimitedAlphaCompositing = context.getResources().getBoolean(17956875);
        this.mHasPermanentDpad = context.getResources().getBoolean(17956998);
        this.mInTouchMode = context.getResources().getBoolean(17957025);
        this.mDrawLockTimeoutMillis = (long) context.getResources().getInteger(17694870);
        this.mAllowAnimationsInLowPowerMode = context.getResources().getBoolean(17957027);
        this.mInputManager = inputManager;
        this.mDisplayManagerInternal = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
        this.mDisplaySettings = new DisplaySettings();
        this.mDisplaySettings.readSettingsLocked();
        this.mWallpaperControllerLocked = new WallpaperController(this);
        this.mWindowPlacerLocked = new WindowSurfacePlacer(this);
        this.mLayersController = new WindowLayersController(this);
        LocalServices.addService(WindowManagerPolicy.class, this.mPolicy);
        this.mPointerEventDispatcher = new PointerEventDispatcher(this.mInputManager.monitorInput("WindowManager"));
        this.mFxSession = new SurfaceSession();
        this.mDisplayManager = (DisplayManager) context.getSystemService("display");
        this.mCust = (HwCustWindowManagerService) HwCustUtils.createObj(HwCustWindowManagerService.class, new Object[0]);
        this.mDisplays = this.mDisplayManager.getDisplays();
        for (Display display : this.mDisplays) {
            createDisplayContentLocked(display);
        }
        this.mKeyguardDisableHandler = new KeyguardDisableHandler(this.mContext, this.mPolicy);
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mPowerManagerInternal.registerLowPowerModeObserver(new LowPowerModeListener() {
            public void onLowPowerModeChanged(boolean enabled) {
                synchronized (WindowManagerService.this.mWindowMap) {
                    if (!(WindowManagerService.this.mAnimationsDisabled == enabled || WindowManagerService.this.mAllowAnimationsInLowPowerMode)) {
                        WindowManagerService.this.mAnimationsDisabled = enabled;
                        WindowManagerService.this.dispatchNewAnimatorScaleLocked(null);
                    }
                }
            }
        });
        this.mAnimationsDisabled = this.mPowerManagerInternal.getLowPowerModeEnabled();
        this.mScreenFrozenLock = this.mPowerManager.newWakeLock(1, "SCREEN_FROZEN");
        this.mScreenFrozenLock.setReferenceCounted(false);
        this.mAppTransition = HwServiceFactory.createHwAppTransition(context, this);
        this.mAppTransition.registerListenerLocked(this.mActivityManagerAppTransitionNotifier);
        this.mBoundsAnimationController = new BoundsAnimationController(this.mAppTransition, UiThread.getHandler());
        this.mActivityManager = ActivityManagerNative.getDefault();
        this.mAmInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        OnOpChangedInternalListener opListener = new OnOpChangedInternalListener() {
            public void onOpChanged(int op, String packageName) {
                WindowManagerService.this.sendUpdateAppOpsState();
                WindowManagerService.this.updateAppOpsStateReport(op, packageName);
            }
        };
        this.mAppOps.startWatchingMode(24, null, opListener);
        this.mAppOps.startWatchingMode(45, null, opListener);
        this.mWindowAnimationScaleSetting = Global.getFloat(context.getContentResolver(), "window_animation_scale", this.mWindowAnimationScaleSetting);
        this.mTransitionAnimationScaleSetting = Global.getFloat(context.getContentResolver(), "transition_animation_scale", this.mTransitionAnimationScaleSetting);
        setAnimatorDurationScale(Global.getFloat(context.getContentResolver(), "animator_duration_scale", this.mAnimatorDurationScaleSetting));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        this.mSettingsObserver = new SettingsObserver();
        this.mHoldingScreenWakeLock = this.mPowerManager.newWakeLock(536870922, "WindowManager");
        this.mHoldingScreenWakeLock.setReferenceCounted(false);
        this.mAnimator = new WindowAnimator(this);
        this.mAllowTheaterModeWakeFromLayout = context.getResources().getBoolean(17956916);
        LocalServices.addService(WindowManagerInternal.class, new LocalService());
        initPolicy();
        Watchdog.getInstance().addMonitor(this);
        SurfaceControl.openTransaction();
        try {
            createWatermarkInTransaction();
            showEmulatorDisplayOverlayIfNeeded();
            if (HISI_PERF_OPT) {
                initAccelPackages();
            }
        } finally {
            SurfaceControl.closeTransaction();
        }
    }

    public InputMonitor getInputMonitor() {
        return this.mInputMonitor;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.wtf("WindowManager", "Window Manager Crash", e);
            }
            throw e;
        }
    }

    private void placeWindowAfter(WindowState pos, WindowState window) {
        WindowList windows = pos.getWindowList();
        windows.add(windows.indexOf(pos) + 1, window);
        this.mWindowsChanged = true;
    }

    private void placeWindowBefore(WindowState pos, WindowState window) {
        WindowList windows = pos.getWindowList();
        int i = windows.indexOf(pos);
        if (i < 0) {
            Slog.w("WindowManager", "placeWindowBefore: Unable to find " + pos + " in " + windows);
            i = 0;
        }
        windows.add(i, window);
        this.mWindowsChanged = true;
    }

    protected int findIdxBasedOnAppTokens(WindowState win) {
        WindowList windows = win.getWindowList();
        for (int j = windows.size() - 1; j >= 0; j--) {
            if (((WindowState) windows.get(j)).mAppToken == win.mAppToken) {
                return j;
            }
        }
        return -1;
    }

    private WindowList getTokenWindowsOnDisplay(WindowToken token, DisplayContent displayContent) {
        WindowList windowList = new WindowList();
        int count = token.windows.size();
        for (int i = 0; i < count; i++) {
            WindowState win = (WindowState) token.windows.get(i);
            if (win.getDisplayContent() == displayContent) {
                windowList.add(win);
            }
        }
        return windowList;
    }

    private int indexOfWinInWindowList(WindowState targetWin, WindowList windows) {
        for (int i = windows.size() - 1; i >= 0; i--) {
            WindowState w = (WindowState) windows.get(i);
            if (w == targetWin) {
                return i;
            }
            if (!w.mChildWindows.isEmpty() && indexOfWinInWindowList(targetWin, w.mChildWindows) >= 0) {
                return i;
            }
        }
        return -1;
    }

    private int addAppWindowToListLocked(WindowState win) {
        DisplayContent displayContent = win.getDisplayContent();
        if (displayContent == null) {
            return 0;
        }
        IWindow client = win.mClient;
        WindowToken token = win.mToken;
        WindowList windows = displayContent.getWindowList();
        WindowList tokenWindowList = getTokenWindowsOnDisplay(token, displayContent);
        if (!tokenWindowList.isEmpty()) {
            return addAppWindowToTokenListLocked(win, token, windows, tokenWindowList);
        }
        WindowState pos = null;
        ArrayList<Task> tasks = displayContent.getTasks();
        int tokenNdx = -1;
        int taskNdx = tasks.size() - 1;
        while (taskNdx >= 0) {
            AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
            tokenNdx = tokens.size() - 1;
            while (tokenNdx >= 0) {
                WindowToken t = (AppWindowToken) tokens.get(tokenNdx);
                if (t == token) {
                    tokenNdx--;
                    if (tokenNdx < 0) {
                        taskNdx--;
                        if (taskNdx >= 0) {
                            tokenNdx = ((Task) tasks.get(taskNdx)).mAppTokens.size() - 1;
                        }
                    }
                    if (tokenNdx >= 0) {
                        break;
                    }
                    taskNdx--;
                } else {
                    tokenWindowList = getTokenWindowsOnDisplay(t, displayContent);
                    if (!t.sendingToBottom && tokenWindowList.size() > 0) {
                        pos = (WindowState) tokenWindowList.get(0);
                    }
                    tokenNdx--;
                }
            }
            if (tokenNdx >= 0) {
                break;
            }
            taskNdx--;
        }
        WindowToken atoken;
        if (pos != null) {
            atoken = (WindowToken) this.mTokenMap.get(pos.mClient.asBinder());
            if (atoken != null) {
                tokenWindowList = getTokenWindowsOnDisplay(atoken, displayContent);
                if (tokenWindowList.size() > 0) {
                    WindowState bottom = (WindowState) tokenWindowList.get(0);
                    if (bottom.mSubLayer < 0) {
                        pos = bottom;
                    }
                }
            }
            placeWindowBefore(pos, win);
            return 0;
        }
        for (taskNdx = 
/*
Method generation error in method: com.android.server.wm.WindowManagerService.addAppWindowToListLocked(com.android.server.wm.WindowState):int
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r15_5 'taskNdx' int) = (r15_3 'taskNdx' int), (r15_1 'taskNdx' int) binds: {(r15_1 'taskNdx' int)=B:65:0x0087, (r15_3 'taskNdx' int)=B:66:0x0087} in method: com.android.server.wm.WindowManagerService.addAppWindowToListLocked(com.android.server.wm.WindowState):int
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:226)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:184)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:187)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:328)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:265)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:228)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:118)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:83)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:19)
	at jadx.core.ProcessClass.process(ProcessClass.java:43)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.CodegenException: PHI can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:530)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:514)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
	... 27 more

*/

        private int addAppWindowToTokenListLocked(WindowState win, WindowToken token, WindowList windows, WindowList tokenWindowList) {
            if (win.mAttrs.type == 1) {
                WindowState lowestWindow = (WindowState) tokenWindowList.get(0);
                placeWindowBefore(lowestWindow, win);
                return indexOfWinInWindowList(lowestWindow, token.windows);
            }
            AppWindowToken atoken = win.mAppToken;
            WindowState lastWindow = (WindowState) tokenWindowList.get(tokenWindowList.size() - 1);
            if (atoken == null || lastWindow != atoken.startingWindow) {
                int tokenWindowsPos;
                int newIdx = findIdxBasedOnAppTokens(win);
                windows.add(newIdx + 1, win);
                if (newIdx < 0) {
                    tokenWindowsPos = 0;
                } else {
                    tokenWindowsPos = indexOfWinInWindowList((WindowState) windows.get(newIdx), token.windows) + 1;
                }
                this.mWindowsChanged = true;
                return tokenWindowsPos;
            }
            placeWindowBefore(lastWindow, win);
            return indexOfWinInWindowList(lastWindow, token.windows);
        }

        private void addFreeWindowToListLocked(WindowState win) {
            WindowList windows = win.getWindowList();
            int myLayer = win.mBaseLayer;
            int i = windows.size() - 1;
            while (i >= 0) {
                WindowState otherWin = (WindowState) windows.get(i);
                if (otherWin.getBaseType() != 2013 && otherWin.mBaseLayer <= myLayer) {
                    break;
                }
                i--;
            }
            windows.add(i + 1, win);
            this.mWindowsChanged = true;
        }

        private void addAttachedWindowToListLocked(WindowState win, boolean addToToken) {
            WindowToken token = win.mToken;
            DisplayContent displayContent = win.getDisplayContent();
            if (displayContent != null) {
                int i;
                WindowState attached = win.mAttachedWindow;
                WindowList tokenWindowList = getTokenWindowsOnDisplay(token, displayContent);
                int NA = tokenWindowList.size();
                int sublayer = win.mSubLayer;
                int largestSublayer = Integer.MIN_VALUE;
                WindowState windowWithLargestSublayer = null;
                for (i = 0; i < NA; i++) {
                    WindowState w = (WindowState) tokenWindowList.get(i);
                    int wSublayer = w.mSubLayer;
                    if (wSublayer >= largestSublayer) {
                        largestSublayer = wSublayer;
                        windowWithLargestSublayer = w;
                    }
                    if (sublayer < 0) {
                        if (wSublayer >= sublayer) {
                            if (addToToken) {
                                token.windows.add(i, win);
                            }
                            if (wSublayer >= 0) {
                                w = attached;
                            }
                            placeWindowBefore(w, win);
                            if (i >= NA) {
                                if (addToToken) {
                                    token.windows.add(win);
                                }
                                if (sublayer < 0) {
                                    placeWindowBefore(attached, win);
                                } else {
                                    if (largestSublayer < 0) {
                                        windowWithLargestSublayer = attached;
                                    }
                                    placeWindowAfter(windowWithLargestSublayer, win);
                                }
                            }
                        }
                    } else if (wSublayer > sublayer) {
                        if (addToToken) {
                            token.windows.add(i, win);
                        }
                        placeWindowBefore(w, win);
                        if (i >= NA) {
                            if (addToToken) {
                                token.windows.add(win);
                            }
                            if (sublayer < 0) {
                                if (largestSublayer < 0) {
                                    windowWithLargestSublayer = attached;
                                }
                                placeWindowAfter(windowWithLargestSublayer, win);
                            } else {
                                placeWindowBefore(attached, win);
                            }
                        }
                    }
                }
                if (i >= NA) {
                    if (addToToken) {
                        token.windows.add(win);
                    }
                    if (sublayer < 0) {
                        placeWindowBefore(attached, win);
                    } else {
                        if (largestSublayer < 0) {
                            windowWithLargestSublayer = attached;
                        }
                        placeWindowAfter(windowWithLargestSublayer, win);
                    }
                }
            }
        }

        private void addWindowToListInOrderLocked(WindowState win, boolean addToToken) {
            if (this.mActivityResource == null) {
                this.mActivityResource = HwFrameworkFactory.getHwResource(36);
            }
            if (!(this.mActivityResource == null || win.mAttrs.packageName == null)) {
                if (Log.HWINFO) {
                    Slog.d(TAG, "ACTIVITY check resid: " + win.mAttrs.packageName + ", size=" + win.mToken.windows.size());
                }
                this.mActivityResource.acquire(win.mOwnerUid, win.mAttrs.packageName, -1, win.mToken.windows.size());
            }
            if (win.mAttachedWindow == null) {
                WindowToken token = win.mToken;
                int tokenWindowsPos = 0;
                if (token.appWindowToken != null) {
                    tokenWindowsPos = addAppWindowToListLocked(win);
                } else {
                    addFreeWindowToListLocked(win);
                }
                if (addToToken) {
                    token.windows.add(tokenWindowsPos, win);
                }
            } else {
                addAttachedWindowToListLocked(win, addToToken);
            }
            AppWindowToken appToken = win.mAppToken;
            if (appToken != null && addToToken) {
                appToken.addWindow(win);
            }
        }

        static boolean canBeImeTarget(WindowState w) {
            int fl = w.mAttrs.flags & 131080;
            int type = w.mAttrs.type;
            if (fl == 0 || fl == 131080 || type == 3) {
                return w.isVisibleOrAdding();
            }
            return false;
        }

        int findDesiredInputMethodWindowIndexLocked(boolean willMove) {
            WindowState curTarget;
            AppWindowToken token;
            int highestPos;
            WindowState dockedDivider;
            int dividerIndex;
            WindowList windows = getDefaultWindowListLocked();
            WindowState windowState = null;
            int i = windows.size() - 1;
            while (i >= 0) {
                WindowState win = (WindowState) windows.get(i);
                WindowState windowState2;
                WindowList curWindows;
                int pos;
                if (canBeImeTarget(win)) {
                    windowState = win;
                    if (!willMove && win.mAttrs.type == 3 && i > 0) {
                        WindowState wb = (WindowState) windows.get(i - 1);
                        if (wb.mAppToken == win.mAppToken && canBeImeTarget(wb)) {
                            i--;
                            windowState = wb;
                        }
                    }
                    curTarget = this.mInputMethodTarget;
                    if (curTarget == null && curTarget.isDisplayedLw() && curTarget.isClosing() && (windowState == null || curTarget.mWinAnimator.mAnimLayer > windowState.mWinAnimator.mAnimLayer)) {
                        return windows.indexOf(curTarget) + 1;
                    }
                    if (willMove && windowState != null) {
                        token = curTarget != null ? null : curTarget.mAppToken;
                        if (token != null) {
                            windowState2 = null;
                            highestPos = 0;
                            if (token.mAppAnimator.animating || token.mAppAnimator.animation != null) {
                                curWindows = curTarget.getWindowList();
                                for (pos = curWindows.indexOf(curTarget); pos >= 0; pos--) {
                                    win = (WindowState) curWindows.get(pos);
                                    if (win.mAppToken != token) {
                                        break;
                                    }
                                    if (!win.mRemoved && (r5 == null || win.mWinAnimator.mAnimLayer > r5.mWinAnimator.mAnimLayer)) {
                                        windowState2 = win;
                                        highestPos = pos;
                                    }
                                }
                            }
                            if (windowState2 != null) {
                                if (this.mAppTransition.isTransitionSet()) {
                                    this.mInputMethodTargetWaitingAnim = true;
                                    this.mInputMethodTarget = windowState2;
                                    return highestPos + 1;
                                } else if (windowState2.mWinAnimator.isAnimationSet() && windowState2.mWinAnimator.mAnimLayer > windowState.mWinAnimator.mAnimLayer) {
                                    this.mInputMethodTargetWaitingAnim = true;
                                    this.mInputMethodTarget = windowState2;
                                    return highestPos + 1;
                                }
                            }
                        }
                    }
                    if (windowState == null) {
                        if (willMove) {
                            this.mInputMethodTarget = windowState;
                            this.mInputMethodTargetWaitingAnim = false;
                            if (windowState.mAppToken == null) {
                                this.mLayersController.setInputMethodAnimLayerAdjustment(windowState.mAppToken.mAppAnimator.animLayerAdjustment);
                            } else {
                                this.mLayersController.setInputMethodAnimLayerAdjustment(0);
                            }
                        }
                        dockedDivider = windowState.mDisplayContent.mDividerControllerLocked.getWindow();
                        if (dockedDivider != null && dockedDivider.isVisibleLw()) {
                            dividerIndex = windows.indexOf(dockedDivider);
                            if (dividerIndex > 0 && dividerIndex > i) {
                                return dividerIndex + 1;
                            }
                        }
                        return i + 1;
                    }
                    if (willMove) {
                        this.mInputMethodTarget = null;
                        this.mLayersController.setInputMethodAnimLayerAdjustment(0);
                    }
                    return -1;
                }
                i--;
            }
            curTarget = this.mInputMethodTarget;
            if (curTarget == null) {
            }
            if (curTarget != null) {
            }
            if (token != null) {
                windowState2 = null;
                highestPos = 0;
                curWindows = curTarget.getWindowList();
                for (pos = curWindows.indexOf(curTarget); pos >= 0; pos--) {
                    win = (WindowState) curWindows.get(pos);
                    if (win.mAppToken != token) {
                        break;
                    }
                    windowState2 = win;
                    highestPos = pos;
                }
                if (windowState2 != null) {
                    if (this.mAppTransition.isTransitionSet()) {
                        this.mInputMethodTargetWaitingAnim = true;
                        this.mInputMethodTarget = windowState2;
                        return highestPos + 1;
                    }
                    this.mInputMethodTargetWaitingAnim = true;
                    this.mInputMethodTarget = windowState2;
                    return highestPos + 1;
                }
            }
            if (windowState == null) {
                if (willMove) {
                    this.mInputMethodTarget = null;
                    this.mLayersController.setInputMethodAnimLayerAdjustment(0);
                }
                return -1;
            }
            if (willMove) {
                this.mInputMethodTarget = windowState;
                this.mInputMethodTargetWaitingAnim = false;
                if (windowState.mAppToken == null) {
                    this.mLayersController.setInputMethodAnimLayerAdjustment(0);
                } else {
                    this.mLayersController.setInputMethodAnimLayerAdjustment(windowState.mAppToken.mAppAnimator.animLayerAdjustment);
                }
            }
            dockedDivider = windowState.mDisplayContent.mDividerControllerLocked.getWindow();
            dividerIndex = windows.indexOf(dockedDivider);
            return dividerIndex + 1;
        }

        void addInputMethodWindowToListLocked(WindowState win) {
            int pos = findDesiredInputMethodWindowIndexLocked(true);
            if (pos >= 0) {
                win.mTargetAppToken = this.mInputMethodTarget.mAppToken;
                getDefaultWindowListLocked().add(pos, win);
                this.mWindowsChanged = true;
                moveInputMethodDialogsLocked(pos + 1);
                return;
            }
            win.mTargetAppToken = null;
            addWindowToListInOrderLocked(win, true);
            moveInputMethodDialogsLocked(pos);
        }

        private int tmpRemoveWindowLocked(int interestingPos, WindowState win) {
            WindowList windows = win.getWindowList();
            int wpos = windows.indexOf(win);
            if (wpos >= 0) {
                if (wpos < interestingPos) {
                    interestingPos--;
                }
                windows.remove(wpos);
                this.mWindowsChanged = true;
                int NC = win.mChildWindows.size();
                while (NC > 0) {
                    NC--;
                    int cpos = windows.indexOf((WindowState) win.mChildWindows.get(NC));
                    if (cpos >= 0) {
                        if (cpos < interestingPos) {
                            interestingPos--;
                        }
                        windows.remove(cpos);
                    }
                }
            }
            return interestingPos;
        }

        private void reAddWindowToListInOrderLocked(WindowState win) {
            addWindowToListInOrderLocked(win, false);
            WindowList windows = win.getWindowList();
            int wpos = windows.indexOf(win);
            if (wpos >= 0) {
                windows.remove(wpos);
                this.mWindowsChanged = true;
                reAddWindowLocked(wpos, win);
            }
        }

        void logWindowList(WindowList windows, String prefix) {
            int N = windows.size();
            while (N > 0) {
                N--;
                Slog.v("WindowManager", prefix + "#" + N + ": " + windows.get(N));
            }
        }

        void moveInputMethodDialogsLocked(int pos) {
            int i;
            ArrayList<WindowState> dialogs = this.mInputMethodDialogs;
            WindowList windows = getDefaultWindowListLocked();
            int N = dialogs.size();
            for (i = 0; i < N; i++) {
                pos = tmpRemoveWindowLocked(pos, (WindowState) dialogs.get(i));
            }
            if (pos >= 0) {
                AppWindowToken targetAppToken = this.mInputMethodTarget.mAppToken;
                if (this.mInputMethodWindow != null) {
                    while (pos < windows.size()) {
                        WindowState wp = (WindowState) windows.get(pos);
                        if (wp != this.mInputMethodWindow && wp.mAttachedWindow != this.mInputMethodWindow) {
                            break;
                        }
                        pos++;
                    }
                }
                for (i = 0; i < N; i++) {
                    WindowState win = (WindowState) dialogs.get(i);
                    win.mTargetAppToken = targetAppToken;
                    pos = reAddWindowLocked(pos, win);
                }
                return;
            }
            for (i = 0; i < N; i++) {
                win = (WindowState) dialogs.get(i);
                win.mTargetAppToken = null;
                reAddWindowToListInOrderLocked(win);
            }
        }

        boolean moveInputMethodWindowsIfNeededLocked(boolean needAssignLayers) {
            WindowState imWin = this.mInputMethodWindow;
            int DN = this.mInputMethodDialogs.size();
            if (imWin == null && DN == 0) {
                return false;
            }
            WindowList windows = getDefaultWindowListLocked();
            int imPos = findDesiredInputMethodWindowIndexLocked(true);
            if (imPos >= 0) {
                WindowState baseImWin;
                int N = windows.size();
                WindowState windowState = imPos < N ? (WindowState) windows.get(imPos) : null;
                if (imWin != null) {
                    baseImWin = imWin;
                } else {
                    baseImWin = (WindowState) this.mInputMethodDialogs.get(0);
                }
                if (baseImWin.mChildWindows.size() > 0) {
                    WindowState cw = (WindowState) baseImWin.mChildWindows.get(0);
                    if (cw.mSubLayer < 0) {
                        baseImWin = cw;
                    }
                }
                if (windowState == baseImWin) {
                    int pos = imPos + 1;
                    while (pos < N && ((WindowState) windows.get(pos)).mIsImWindow) {
                        pos++;
                    }
                    pos++;
                    while (pos < N && !((WindowState) windows.get(pos)).mIsImWindow) {
                        pos++;
                    }
                    if (pos >= N) {
                        if (imWin != null) {
                            imWin.mTargetAppToken = this.mInputMethodTarget.mAppToken;
                        }
                        return false;
                    }
                }
                if (imWin != null) {
                    imPos = tmpRemoveWindowLocked(imPos, imWin);
                    imWin.mTargetAppToken = this.mInputMethodTarget.mAppToken;
                    reAddWindowLocked(imPos, imWin);
                    if (DN > 0) {
                        moveInputMethodDialogsLocked(imPos + 1);
                    }
                } else {
                    moveInputMethodDialogsLocked(imPos);
                }
            } else if (imWin != null) {
                tmpRemoveWindowLocked(0, imWin);
                imWin.mTargetAppToken = null;
                reAddWindowToListInOrderLocked(imWin);
                if (DN > 0) {
                    moveInputMethodDialogsLocked(-1);
                }
            } else {
                moveInputMethodDialogsLocked(-1);
            }
            if (needAssignLayers) {
                this.mLayersController.assignLayersLocked(windows);
            }
            return true;
        }

        private static boolean excludeWindowTypeFromTapOutTask(int windowType) {
            switch (windowType) {
                case IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME /*2000*/:
                case 2012:
                case 2019:
                    return true;
                default:
                    return false;
            }
        }

        private static boolean excludeWindowsFromTapOutTask(WindowState win) {
            boolean z = false;
            LayoutParams attrs = null;
            if (win != null) {
                attrs = win.getAttrs();
            }
            if (attrs == null) {
                return false;
            }
            if (attrs.type == 1000) {
                z = "com.baidu.input_huawei".equals(attrs.packageName);
            }
            return z;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int addWindow(Session session, IWindow client, int seq, LayoutParams attrs, int viewVisibility, int displayId, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, InputChannel outInputChannel) {
            int[] appOp = new int[1];
            int res = this.mPolicy.checkAddPermission(attrs, appOp);
            if (res != 0) {
                return res;
            }
            int forceCompatMode;
            boolean reportNewConfig = false;
            WindowState windowState = null;
            int type = attrs.type;
            if (attrs.type >= 1000 && attrs.type <= 1999) {
                forceCompatMode = -3;
            } else if (attrs.packageName == null) {
                forceCompatMode = -3;
            } else if ((attrs.privateFlags & 128) != 0) {
                forceCompatMode = 1;
            } else {
                forceCompatMode = 0;
            }
            synchronized (this.mWindowMap) {
                if (this.mDisplayReady) {
                    DisplayContent displayContent = getDisplayContentLocked(displayId);
                    if (displayContent == null) {
                        Slog.w("WindowManager", "Attempted to add window to a display that does not exist: " + displayId + ".  Aborting.");
                        return -9;
                    } else if (!displayContent.hasAccess(session.mUid)) {
                        Slog.w("WindowManager", "Attempted to add window to a display for which the application does not have access: " + displayId + ".  Aborting.");
                        return -9;
                    } else if (this.mWindowMap.containsKey(client.asBinder())) {
                        Slog.w("WindowManager", "Window " + client + " is already added");
                        return -5;
                    } else {
                        if (type >= 1000 && type <= 1999) {
                            windowState = windowForClientLocked(null, attrs.token, false);
                            if (windowState == null) {
                                Slog.w("WindowManager", "Attempted to add window with token that is not a window: " + attrs.token + ".  Aborting.");
                                return -2;
                            } else if (windowState.mAttrs.type >= 1000 && windowState.mAttrs.type <= 1999) {
                                Slog.w("WindowManager", "Attempted to add window with token that is a sub-window: " + attrs.token + ".  Aborting.");
                                return -2;
                            }
                        }
                        if (type == 2030) {
                            if (!displayContent.isPrivate()) {
                                Slog.w("WindowManager", "Attempted to add private presentation window to a non-private display.  Aborting.");
                                return -8;
                            }
                        }
                        boolean addToken = false;
                        WindowToken token = (WindowToken) this.mTokenMap.get(attrs.token);
                        AppWindowToken atoken = null;
                        if (token == null) {
                            if (type >= 1 && type <= 99) {
                                Slog.w("WindowManager", "Attempted to add application window with unknown token " + attrs.token + ".  Aborting.");
                                return -1;
                            } else if (type == 2011) {
                                Slog.w("WindowManager", "Attempted to add input method window with unknown token " + attrs.token + ".  Aborting.");
                                return -1;
                            } else if (type == 2031) {
                                Slog.w("WindowManager", "Attempted to add voice interaction window with unknown token " + attrs.token + ".  Aborting.");
                                return -1;
                            } else if (type == 2013) {
                                Slog.w("WindowManager", "Attempted to add wallpaper window with unknown token " + attrs.token + ".  Aborting.");
                                return -1;
                            } else if (type == 2023) {
                                Slog.w("WindowManager", "Attempted to add Dream window with unknown token " + attrs.token + ".  Aborting.");
                                return -1;
                            } else if (type == 2035) {
                                Slog.w("WindowManager", "Attempted to add QS dialog window with unknown token " + attrs.token + ".  Aborting.");
                                return -1;
                            } else if (type == 2032) {
                                Slog.w("WindowManager", "Attempted to add Accessibility overlay window with unknown token " + attrs.token + ".  Aborting.");
                                return -1;
                            } else {
                                token = new WindowToken(this, attrs.token, -1, false);
                                addToken = true;
                            }
                        } else if (type >= 1 && type <= 99) {
                            atoken = token.appWindowToken;
                            if (atoken == null) {
                                Slog.w("WindowManager", "Attempted to add window with non-application token " + token + ".  Aborting.");
                                return -3;
                            } else if (atoken.removed) {
                                Slog.w("WindowManager", "Attempted to add window with exiting application token " + token + ".  Aborting.");
                                return -4;
                            } else if (type == 3) {
                                if (atoken.firstWindowDrawn) {
                                    return -6;
                                }
                            }
                        } else if (type == 2011) {
                            if (token.windowType != 2011) {
                                Slog.w("WindowManager", "Attempted to add input method window with bad token " + attrs.token + ".  Aborting.");
                                return -1;
                            }
                        } else if (type == 2031) {
                            if (token.windowType != 2031) {
                                Slog.w("WindowManager", "Attempted to add voice interaction window with bad token " + attrs.token + ".  Aborting.");
                                return -1;
                            }
                        } else if (type == 2013) {
                            if (token.windowType != 2013) {
                                Slog.w("WindowManager", "Attempted to add wallpaper window with bad token " + attrs.token + ".  Aborting.");
                                return -1;
                            }
                        } else if (type == 2023) {
                            if (token.windowType != 2023) {
                                Slog.w("WindowManager", "Attempted to add Dream window with bad token " + attrs.token + ".  Aborting.");
                                return -1;
                            }
                        } else if (type == 2032) {
                            if (token.windowType != 2032) {
                                Slog.w("WindowManager", "Attempted to add Accessibility overlay window with bad token " + attrs.token + ".  Aborting.");
                                return -1;
                            }
                        } else if (type == 2035) {
                            if (token.windowType != 2035) {
                                Slog.w("WindowManager", "Attempted to add QS dialog window with bad token " + attrs.token + ".  Aborting.");
                                return -1;
                            }
                        } else if (token.appWindowToken != null) {
                            Slog.w("WindowManager", "Non-null appWindowToken for system window of type=" + type);
                            attrs.token = null;
                            token = new WindowToken(this, null, -1, false);
                            addToken = true;
                        } else if (!(this.mCust == null || !this.mCust.isChargingAlbumType(type) || this.mCust.isChargingAlbumType(token.windowType))) {
                            Slog.w(TAG, "Attempted to add Dream window with bad token " + attrs.token + ".  Aborting.");
                            return -1;
                        }
                        WindowState win = new WindowState(this, session, client, token, windowState, appOp[0], seq, attrs, viewVisibility, displayContent, forceCompatMode);
                        if (win.mDeathRecipient == null) {
                            Slog.w("WindowManager", "Adding window client " + client.asBinder() + " that is dead, aborting.");
                            return -4;
                        } else if (win.getDisplayContent() == null) {
                            Slog.w("WindowManager", "Adding window to Display that has been removed.");
                            return -9;
                        } else {
                            this.mPolicy.adjustWindowParamsLw(win.mAttrs);
                            win.setShowToOwnerOnlyLocked(this.mPolicy.checkShowToOwnerOnly(attrs));
                            res = this.mPolicy.prepareAddWindowLw(win, attrs);
                            if (res != 0) {
                                return res;
                            }
                            boolean openInputChannels = outInputChannel != null ? (attrs.inputFeatures & 2) == 0 : false;
                            if (openInputChannels) {
                                win.openInputChannel(outInputChannel);
                            }
                            res = 0;
                            if (excludeWindowTypeFromTapOutTask(type) || excludeWindowsFromTapOutTask(win)) {
                                displayContent.mTapExcludedWindows.add(win);
                            }
                            long origId = Binder.clearCallingIdentity();
                            if (addToken) {
                                this.mTokenMap.put(attrs.token, token);
                            }
                            win.attach();
                            this.mWindowMap.put(client.asBinder(), win);
                            if (win.mAppOp != -1) {
                                int startOpResult = this.mAppOps.startOpNoThrow(win.mAppOp, win.getOwningUid(), win.getOwningPackage());
                                if (!(startOpResult == 0 || startOpResult == 3)) {
                                    setAppOpHideHook(win, false);
                                }
                                addWindowReport(win, startOpResult);
                            }
                            setVisibleFromParent(win);
                            if (type == 3 && token.appWindowToken != null) {
                                token.appWindowToken.startingWindow = win;
                            }
                            boolean imMayMove = true;
                            if (type == 2011) {
                                win.mGivenInsetsPending = true;
                                this.mInputMethodWindow = win;
                                addInputMethodWindowToListLocked(win);
                                imMayMove = false;
                            } else if (type == 2012) {
                                this.mInputMethodDialogs.add(win);
                                addWindowToListInOrderLocked(win, true);
                                moveInputMethodDialogsLocked(findDesiredInputMethodWindowIndexLocked(true));
                                imMayMove = false;
                            } else {
                                addWindowToListInOrderLocked(win, true);
                                if (type == 2013) {
                                    this.mWallpaperControllerLocked.clearLastWallpaperTimeoutTime();
                                    displayContent.pendingLayoutChanges |= 4;
                                } else if ((attrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                                    displayContent.pendingLayoutChanges |= 4;
                                } else if (this.mWallpaperControllerLocked.isBelowWallpaperTarget(win)) {
                                    displayContent.pendingLayoutChanges |= 4;
                                }
                            }
                            win.applyScrollIfNeeded();
                            win.applyAdjustForImeIfNeeded();
                            if (type == 2034) {
                                getDefaultDisplayContentLocked().getDockedDividerController().setWindow(win);
                            }
                            WindowStateAnimator winAnimator = win.mWinAnimator;
                            winAnimator.mEnterAnimationPending = true;
                            winAnimator.mEnteringAnimation = true;
                            if (!(atoken == null || prepareWindowReplacementTransition(atoken))) {
                                prepareNoneTransitionForRelaunching(atoken);
                            }
                            if (displayContent.isDefaultDisplay) {
                                Rect taskBounds;
                                DisplayInfo displayInfo = displayContent.getDisplayInfo();
                                if (atoken == null || atoken.mTask == null) {
                                    taskBounds = null;
                                } else {
                                    taskBounds = this.mTmpRect;
                                    atoken.mTask.getBounds(this.mTmpRect);
                                }
                                if (this.mPolicy.getInsetHintLw(win.mAttrs, taskBounds, this.mRotation, displayInfo.logicalWidth, displayInfo.logicalHeight, outContentInsets, outStableInsets, outOutsets)) {
                                    res = 4;
                                }
                            } else {
                                outContentInsets.setEmpty();
                                outStableInsets.setEmpty();
                            }
                            if (this.mInTouchMode) {
                                res |= 1;
                            }
                            if (win.mAppToken == null || !win.mAppToken.clientHidden) {
                                res |= 2;
                            }
                            this.mInputMonitor.setUpdateInputWindowsNeededLw();
                            boolean focusChanged = false;
                            if (win.canReceiveKeys()) {
                                focusChanged = updateFocusedWindowLocked(1, false);
                                if (focusChanged) {
                                    imMayMove = false;
                                }
                            } else if (win.getAttrs().type == 1) {
                                this.mPolicy.updateSystemUiColorLw(win);
                            }
                            if (imMayMove) {
                                moveInputMethodWindowsIfNeededLocked(false);
                            }
                            this.mLayersController.assignLayersLocked(displayContent.getWindowList());
                            if (focusChanged) {
                                this.mInputMonitor.setInputFocusLw(this.mCurrentFocus, false);
                            }
                            this.mInputMonitor.updateInputWindowsLw(false);
                            if (win.isVisibleOrAdding() && updateOrientationFromAppTokensLocked(false)) {
                                reportNewConfig = true;
                            }
                            if (attrs.removeTimeoutMilliseconds > 0) {
                                this.mH.sendMessageDelayed(this.mH.obtainMessage(52, win), attrs.removeTimeoutMilliseconds);
                            }
                        }
                    }
                } else {
                    throw new IllegalStateException("Display has not been initialialized");
                }
            }
        }

        private boolean prepareWindowReplacementTransition(AppWindowToken atoken) {
            atoken.clearAllDrawn();
            WindowState replacedWindow = null;
            for (int i = atoken.windows.size() - 1; i >= 0 && replacedWindow == null; i--) {
                WindowState candidate = (WindowState) atoken.windows.get(i);
                if (candidate.mAnimatingExit && candidate.mWillReplaceWindow && candidate.mAnimateReplacingWindow) {
                    replacedWindow = candidate;
                }
            }
            if (replacedWindow == null) {
                return false;
            }
            Rect frame = replacedWindow.mVisibleFrame;
            this.mOpeningApps.add(atoken);
            prepareAppTransition(18, true);
            this.mAppTransition.overridePendingAppTransitionClipReveal(frame.left, frame.top, frame.width(), frame.height());
            executeAppTransition();
            return true;
        }

        private void prepareNoneTransitionForRelaunching(AppWindowToken atoken) {
            if (this.mDisplayFrozen && !this.mOpeningApps.contains(atoken) && atoken.isRelaunching()) {
                this.mOpeningApps.add(atoken);
                prepareAppTransition(0, false);
                executeAppTransition();
            }
        }

        boolean isScreenCaptureDisabledLocked(int userId) {
            Boolean disabled = (Boolean) this.mScreenCaptureDisabled.get(userId);
            if (disabled == null) {
                return false;
            }
            return disabled.booleanValue();
        }

        boolean isSecureLocked(WindowState w) {
            return (w.mAttrs.flags & DumpState.DUMP_PREFERRED_XML) != 0 || isScreenCaptureDisabledLocked(UserHandle.getUserId(w.mOwnerUid));
        }

        public void setScreenCaptureDisabled(int userId, boolean disabled) {
            if (Binder.getCallingUid() != 1000) {
                throw new SecurityException("Only system can call setScreenCaptureDisabled.");
            }
            synchronized (this.mWindowMap) {
                this.mScreenCaptureDisabled.put(userId, Boolean.valueOf(disabled));
                for (int displayNdx = this.mDisplayContents.size() - 1; displayNdx >= 0; displayNdx--) {
                    WindowList windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                    for (int winNdx = windows.size() - 1; winNdx >= 0; winNdx--) {
                        WindowState win = (WindowState) windows.get(winNdx);
                        if (win.mHasSurface && userId == UserHandle.getUserId(win.mOwnerUid)) {
                            win.mWinAnimator.setSecureLocked(disabled);
                        }
                    }
                }
            }
        }

        private void setupWindowForRemoveOnExit(WindowState win) {
            win.mRemoveOnExit = true;
            win.setDisplayLayoutNeeded();
            boolean focusChanged = updateFocusedWindowLocked(3, false);
            this.mWindowPlacerLocked.performSurfacePlacement();
            if (focusChanged) {
                this.mInputMonitor.updateInputWindowsLw(false);
            }
        }

        public void removeWindow(Session session, IWindow client) {
            synchronized (this.mWindowMap) {
                WindowState win = windowForClientLocked(session, client, false);
                if (win == null) {
                    return;
                }
                removeWindowLocked(win);
            }
        }

        void removeWindowLocked(WindowState win) {
            removeWindowLocked(win, false);
        }

        void removeWindowLocked(WindowState win, boolean keepVisibleDeadWindow) {
            long origId;
            boolean z;
            win.mWindowRemovalAllowed = true;
            boolean startingWindow = win.mAttrs.type == 3;
            if (startingWindow) {
                origId = Binder.clearCallingIdentity();
                win.disposeInputChannel();
                z = false;
            } else {
                origId = Binder.clearCallingIdentity();
                win.disposeInputChannel();
                z = false;
            }
            if (win.mHasSurface && okToDisplay()) {
                AppWindowToken appToken = win.mAppToken;
                if (win.mWillReplaceWindow) {
                    win.mAnimatingExit = true;
                    win.mReplacingRemoveRequested = true;
                    Binder.restoreCallingIdentity(origId);
                    return;
                } else if (!win.isAnimatingWithSavedSurface() || appToken.allDrawnExcludingSaved) {
                    z = win.isWinVisibleLw();
                    if (keepVisibleDeadWindow) {
                        win.mAppDied = true;
                        win.setDisplayLayoutNeeded();
                        this.mWindowPlacerLocked.performSurfacePlacement();
                        win.openInputChannel(null);
                        this.mInputMonitor.updateInputWindowsLw(true);
                        Binder.restoreCallingIdentity(origId);
                        return;
                    }
                    WindowStateAnimator winAnimator = win.mWinAnimator;
                    if (z) {
                        int transit = !startingWindow ? 2 : 5;
                        if (winAnimator.applyAnimationLocked(transit, false)) {
                            win.mAnimatingExit = true;
                        }
                        if (this.mAccessibilityController != null && win.getDisplayId() == 0) {
                            this.mAccessibilityController.onWindowTransitionLocked(win, transit);
                        }
                    }
                    boolean isAnimating = winAnimator.isAnimationSet() && !winAnimator.isDummyAnimation();
                    boolean lastWindowIsStartingWindow = (!startingWindow || appToken == null) ? false : appToken.allAppWindows.size() == 1;
                    if (winAnimator.getShown() && win.mAnimatingExit && (!lastWindowIsStartingWindow || isAnimating)) {
                        setupWindowForRemoveOnExit(win);
                        if (appToken != null) {
                            appToken.updateReportedVisibilityLocked();
                        }
                        Binder.restoreCallingIdentity(origId);
                        return;
                    }
                } else {
                    setupWindowForRemoveOnExit(win);
                    Binder.restoreCallingIdentity(origId);
                    return;
                }
            }
            removeWindowInnerLocked(win);
            if (z && updateOrientationFromAppTokensLocked(false)) {
                this.mH.sendEmptyMessage(18);
            }
            updateFocusedWindowLocked(0, true);
            Binder.restoreCallingIdentity(origId);
        }

        void removeWindowInnerLocked(WindowState win) {
            removeWindowInnerLocked(win, true);
        }

        void removeWindowInnerLocked(WindowState win, boolean performLayout) {
            if (!win.mRemoved) {
                for (int i = win.mChildWindows.size() - 1; i >= 0; i--) {
                    WindowState cwin = (WindowState) win.mChildWindows.get(i);
                    Slog.w("WindowManager", "Force-removing child win " + cwin + " from container " + win);
                    removeWindowInnerLocked(cwin);
                }
                win.mRemoved = true;
                if (this.mInputMethodTarget == win) {
                    moveInputMethodWindowsIfNeededLocked(false);
                }
                int type = win.mAttrs.type;
                if (excludeWindowTypeFromTapOutTask(type) || excludeWindowsFromTapOutTask(win)) {
                    win.getDisplayContent().mTapExcludedWindows.remove(win);
                }
                this.mPolicy.removeWindowLw(win);
                win.removeLocked();
                this.mWindowMap.remove(win.mClient.asBinder());
                if (win.mAppOp != -1) {
                    this.mAppOps.finishOp(win.mAppOp, win.getOwningUid(), win.getOwningPackage());
                    removeWindowReport(win);
                }
                this.mPendingRemove.remove(win);
                this.mResizingWindows.remove(win);
                this.mWindowsChanged = true;
                if (this.mInputMethodWindow == win) {
                    this.mInputMethodWindow = null;
                } else if (win.mAttrs.type == 2012) {
                    this.mInputMethodDialogs.remove(win);
                }
                WindowToken token = win.mToken;
                AppWindowToken atoken = win.mAppToken;
                token.windows.remove(win);
                if (atoken != null) {
                    atoken.allAppWindows.remove(win);
                }
                if (token.windows.size() == 0) {
                    if (!token.explicit) {
                        this.mTokenMap.remove(token.token);
                    } else if (atoken != null) {
                        atoken.firstWindowDrawn = false;
                        atoken.clearAllDrawn();
                    }
                }
                if (atoken != null) {
                    if (atoken.startingWindow == win) {
                        scheduleRemoveStartingWindowLocked(atoken);
                    } else if (atoken.allAppWindows.size() == 0 && atoken.startingData != null) {
                        atoken.startingData = null;
                    } else if (atoken.allAppWindows.size() == 1 && atoken.startingView != null) {
                        scheduleRemoveStartingWindowLocked(atoken);
                    }
                }
                DisplayContent defaultDisplayContentLocked;
                if (type == 2013) {
                    this.mWallpaperControllerLocked.clearLastWallpaperTimeoutTime();
                    defaultDisplayContentLocked = getDefaultDisplayContentLocked();
                    defaultDisplayContentLocked.pendingLayoutChanges |= 4;
                } else if ((win.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                    defaultDisplayContentLocked = getDefaultDisplayContentLocked();
                    defaultDisplayContentLocked.pendingLayoutChanges |= 4;
                }
                WindowList windows = win.getWindowList();
                if (windows != null) {
                    windows.remove(win);
                    if (!this.mWindowPlacerLocked.isInLayout()) {
                        this.mLayersController.assignLayersLocked(windows);
                        win.setDisplayLayoutNeeded();
                        this.mWindowPlacerLocked.performSurfacePlacement();
                        if (win.mAppToken != null) {
                            win.mAppToken.updateReportedVisibilityLocked();
                        }
                    }
                }
                this.mInputMonitor.updateInputWindowsLw(true);
            }
        }

        public void updateAppOpsState() {
            synchronized (this.mWindowMap) {
                int numDisplays = this.mDisplayContents.size();
                for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                    WindowList windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                    int numWindows = windows.size();
                    for (int winNdx = 0; winNdx < numWindows; winNdx++) {
                        WindowState win = (WindowState) windows.get(winNdx);
                        if (win.mAppOp != -1) {
                            setAppOpVisibilityLwHook(win, this.mAppOps.checkOpNoThrow(win.mAppOp, win.getOwningUid(), win.getOwningPackage()));
                        }
                    }
                }
            }
        }

        static void logSurface(WindowState w, String msg, boolean withStackTrace) {
            String str = "  SURFACE " + msg + ": " + w;
            if (withStackTrace) {
                logWithStack(TAG, str);
            } else {
                Slog.i("WindowManager", str);
            }
        }

        static void logSurface(SurfaceControl s, String title, String msg) {
            Slog.i("WindowManager", "  SURFACE " + s + ": " + msg + " / " + title);
        }

        static void logWithStack(String tag, String s) {
            Slog.i(tag, s, null);
        }

        void setTransparentRegionWindow(Session session, IWindow client, Region region) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    WindowState w = windowForClientLocked(session, client, false);
                    if (w != null && w.mHasSurface) {
                        w.mWinAnimator.setTransparentRegionHintLocked(region);
                    }
                }
                Binder.restoreCallingIdentity(origId);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
        }

        void setInsetsWindow(Session session, IWindow client, int touchableInsets, Rect contentInsets, Rect visibleInsets, Region touchableRegion) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    WindowState w = windowForClientLocked(session, client, false);
                    if (w != null) {
                        w.mGivenInsetsPending = false;
                        w.mGivenContentInsets.set(contentInsets);
                        w.mGivenVisibleInsets.set(visibleInsets);
                        w.mGivenTouchableRegion.set(touchableRegion);
                        w.mTouchableInsets = touchableInsets;
                        if (w.mGlobalScale != 1.0f) {
                            w.mGivenContentInsets.scale(w.mGlobalScale);
                            w.mGivenVisibleInsets.scale(w.mGlobalScale);
                            w.mGivenTouchableRegion.scale(w.mGlobalScale);
                        }
                        w.setDisplayLayoutNeeded();
                        this.mWindowPlacerLocked.performSurfacePlacement();
                    }
                }
                Binder.restoreCallingIdentity(origId);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
        }

        public void getWindowDisplayFrame(Session session, IWindow client, Rect outDisplayFrame) {
            synchronized (this.mWindowMap) {
                WindowState win = windowForClientLocked(session, client, false);
                if (win == null) {
                    outDisplayFrame.setEmpty();
                    return;
                }
                outDisplayFrame.set(win.mDisplayFrame);
            }
        }

        public void onRectangleOnScreenRequested(IBinder token, Rect rectangle) {
            synchronized (this.mWindowMap) {
                if (this.mAccessibilityController != null) {
                    WindowState window = (WindowState) this.mWindowMap.get(token);
                    if (window != null && window.getDisplayId() == 0) {
                        this.mAccessibilityController.onRectangleOnScreenRequestedLocked(rectangle);
                    }
                }
            }
        }

        public IWindowId getWindowId(IBinder token) {
            IWindowId iWindowId = null;
            synchronized (this.mWindowMap) {
                WindowState window = (WindowState) this.mWindowMap.get(token);
                if (window != null) {
                    iWindowId = window.mWindowId;
                }
            }
            return iWindowId;
        }

        public void pokeDrawLock(Session session, IBinder token) {
            synchronized (this.mWindowMap) {
                WindowState window = windowForClientLocked(session, token, false);
                if (window != null) {
                    window.pokeDrawLockLw(this.mDrawLockTimeoutMillis);
                }
            }
        }

        void repositionChild(Session session, IWindow client, int left, int top, int right, int bottom, long frameNumber, Rect outFrame) {
            Trace.traceBegin(32, "repositionChild");
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    WindowState win = windowForClientLocked(session, client, false);
                    if (win == null) {
                        Binder.restoreCallingIdentity(origId);
                        Trace.traceEnd(32);
                    } else if (win.mAttachedWindow == null) {
                        throw new IllegalArgumentException("repositionChild called but window is notattached to a parent win=" + win);
                    } else {
                        win.mAttrs.x = left;
                        win.mAttrs.y = top;
                        win.mAttrs.width = right - left;
                        win.mAttrs.height = bottom - top;
                        win.setWindowScale(win.mRequestedWidth, win.mRequestedHeight);
                        if (win.mHasSurface) {
                            SurfaceControl.openTransaction();
                            win.applyGravityAndUpdateFrame(win.mContainingFrame, win.mDisplayFrame);
                            win.mWinAnimator.computeShownFrameLocked();
                            win.mWinAnimator.setSurfaceBoundariesLocked(false);
                            if (frameNumber > 0) {
                                win.mWinAnimator.deferTransactionUntilParentFrame(frameNumber);
                            }
                            SurfaceControl.closeTransaction();
                        }
                        outFrame = win.mCompatFrame;
                        Binder.restoreCallingIdentity(origId);
                        Trace.traceEnd(32);
                    }
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
                Trace.traceEnd(32);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int relayoutWindow(Session session, IWindow client, int seq, LayoutParams attrs, int requestedWidth, int requestedHeight, int viewVisibility, int flags, Rect outFrame, Rect outOverscanInsets, Rect outContentInsets, Rect outVisibleInsets, Rect outStableInsets, Rect outOutsets, Rect outBackdropFrame, Configuration outConfig, Surface outSurface) {
            int result = 0;
            boolean hasStatusBarPermission = this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR") == 0;
            long origId = Binder.clearCallingIdentity();
            synchronized (this.mWindowMap) {
                WindowState win = windowForClientLocked(session, client, false);
                if (win == null) {
                    return 0;
                }
                boolean isRogEnable;
                boolean z;
                boolean isDefaultDisplay;
                boolean focusMayChange;
                boolean wallpaperMayMove;
                int oldVisibility;
                boolean configChanged;
                DisplayInfo displayInfo;
                WindowStateAnimator winAnimator = win.mWinAnimator;
                if (viewVisibility != 8) {
                    win.setRequestedSize(requestedWidth, requestedHeight);
                }
                int attrChanges = 0;
                int flagChanges = 0;
                if (attrs != null) {
                    this.mPolicy.adjustWindowParamsLw(attrs);
                    if (seq == win.mSeq) {
                        int systemUiVisibility = attrs.systemUiVisibility | attrs.subtreeSystemUiVisibility;
                        if (!((67043328 & systemUiVisibility) == 0 || hasStatusBarPermission)) {
                            systemUiVisibility &= -67043329;
                        }
                        win.mSystemUiVisibility = systemUiVisibility;
                    }
                    if (win.mAttrs.type != attrs.type) {
                        throw new IllegalArgumentException("Window type can not be changed after the window is added, type changed from " + win.mAttrs.type + " to " + attrs.type);
                    }
                    if ((attrs.privateFlags & DumpState.DUMP_PREFERRED_XML) != 0) {
                        attrs.x = win.mAttrs.x;
                        attrs.y = win.mAttrs.y;
                        attrs.width = win.mAttrs.width;
                        attrs.height = win.mAttrs.height;
                    }
                    LayoutParams layoutParams = win.mAttrs;
                    flagChanges = layoutParams.flags ^ attrs.flags;
                    layoutParams.flags = flagChanges;
                    attrChanges = win.mAttrs.copyFrom(attrs);
                    if ((attrChanges & 16385) != 0) {
                        win.mLayoutNeeded = true;
                    }
                }
                if (win.mViewVisibility != viewVisibility) {
                    Flog.i(307, "Relayout " + win + ": viewVisibility=" + viewVisibility + " req=" + requestedWidth + "x" + requestedHeight + " " + win.mAttrs);
                }
                winAnimator.mSurfaceDestroyDeferred = (flags & 2) != 0;
                if (this.mCurrentFocus == win && (Integer.MIN_VALUE & attrChanges) != 0) {
                    this.mPolicy.updateSystemUiColorLw(win);
                }
                if ((win.mAttrs.privateFlags & 128) == 0) {
                    isRogEnable = win.isRogEnable();
                } else {
                    isRogEnable = true;
                }
                win.mEnforceSizeCompat = isRogEnable;
                if ((attrChanges & 128) != 0) {
                    winAnimator.mAlpha = attrs.alpha;
                }
                win.setWindowScale(win.mRequestedWidth, win.mRequestedHeight);
                if (win.mAttrs.surfaceInsets.left == 0 && win.mAttrs.surfaceInsets.top == 0) {
                    if (win.mAttrs.surfaceInsets.right == 0) {
                        if (win.mAttrs.surfaceInsets.bottom != 0) {
                        }
                        z = (131080 & flagChanges) == 0;
                        isDefaultDisplay = win.isDefaultDisplay();
                        focusMayChange = isDefaultDisplay ? (win.mViewVisibility == viewVisibility || (flagChanges & 8) != 0) ? true : !win.mRelayoutCalled : false;
                        wallpaperMayMove = win.mViewVisibility == viewVisibility ? (win.mAttrs.flags & DumpState.DUMP_DEXOPT) == 0 : false;
                        wallpaperMayMove |= (DumpState.DUMP_DEXOPT & flagChanges) == 0 ? 1 : 0;
                        if (!((flagChanges & DumpState.DUMP_PREFERRED_XML) == 0 || winAnimator.mSurfaceController == null)) {
                            winAnimator.mSurfaceController.setSecure(isSecureLocked(win));
                        }
                        win.mRelayoutCalled = true;
                        win.mInRelayout = true;
                        oldVisibility = win.mViewVisibility;
                        win.mViewVisibility = viewVisibility;
                        if (viewVisibility == 0 || (win.mAppToken != null && win.mAppToken.clientHidden)) {
                            winAnimator.mEnterAnimationPending = false;
                            winAnimator.mEnteringAnimation = false;
                            boolean usingSavedSurfaceBeforeVisible = oldVisibility == 0 ? win.isAnimatingWithSavedSurface() : false;
                            if (!(!winAnimator.hasSurface() || win.mAnimatingExit || usingSavedSurfaceBeforeVisible)) {
                                if (!win.mWillReplaceWindow) {
                                    focusMayChange = tryStartExitingAnimation(win, winAnimator, isDefaultDisplay, focusMayChange);
                                }
                                result = 4;
                            }
                            outSurface.release();
                        } else {
                            try {
                                result = createSurfaceControl(outSurface, relayoutVisibleWindow(outConfig, 0, win, winAnimator, attrChanges, oldVisibility), win, winAnimator);
                                if ((result & 2) != 0) {
                                    focusMayChange = isDefaultDisplay;
                                }
                                if (win.mAttrs.type == 2011 && (this.mInputMethodWindow == null || this.mInputMethodWindow != win)) {
                                    this.mInputMethodWindow = win;
                                    z = true;
                                }
                                win.adjustStartingWindowFlags();
                            } catch (Exception e) {
                                this.mInputMonitor.updateInputWindowsLw(true);
                                Slog.w("WindowManager", "Exception thrown when creating surface for client " + client + " (" + win.mAttrs.getTitle() + ")", e);
                                Binder.restoreCallingIdentity(origId);
                                return 0;
                            }
                        }
                        if (focusMayChange && updateFocusedWindowLocked(3, false)) {
                            z = false;
                        }
                        boolean toBeDisplayed = (result & 2) == 0;
                        if (z && (moveInputMethodWindowsIfNeededLocked(false) || toBeDisplayed)) {
                            this.mLayersController.assignLayersLocked(win.getWindowList());
                        }
                        if (wallpaperMayMove) {
                            DisplayContent defaultDisplayContentLocked = getDefaultDisplayContentLocked();
                            defaultDisplayContentLocked.pendingLayoutChanges |= 4;
                        }
                        win.setDisplayLayoutNeeded();
                        win.mGivenInsetsPending = (flags & 1) == 0;
                        configChanged = updateOrientationFromAppTokensLocked(false);
                        this.mWindowPlacerLocked.performSurfacePlacement();
                        if (toBeDisplayed && win.mIsWallpaper) {
                            displayInfo = getDefaultDisplayInfoLocked();
                            this.mWallpaperControllerLocked.updateWallpaperOffset(win, displayInfo.logicalWidth, displayInfo.logicalHeight, false);
                        }
                        if (win.mAppToken != null) {
                            win.mAppToken.updateReportedVisibilityLocked();
                        }
                        if (winAnimator.mReportSurfaceResized) {
                            winAnimator.mReportSurfaceResized = false;
                            result |= 32;
                        }
                        if (this.mPolicy.isNavBarForcedShownLw(win)) {
                            result |= 64;
                        }
                        if (!win.isGoneForLayoutLw()) {
                            win.mResizedWhileGone = false;
                        }
                        outFrame.set(win.mCompatFrame);
                        outOverscanInsets.set(win.mOverscanInsets);
                        outContentInsets.set(win.mContentInsets);
                        outVisibleInsets.set(win.mVisibleInsets);
                        outStableInsets.set(win.mStableInsets);
                        outOutsets.set(win.mOutsets);
                        outBackdropFrame.set(win.getBackdropFrame(win.mFrame));
                        result |= this.mInTouchMode ? 1 : 0;
                        this.mInputMonitor.updateInputWindowsLw(true);
                        win.mInRelayout = false;
                    }
                }
                winAnimator.setOpaqueLocked(false);
                if ((131080 & flagChanges) == 0) {
                }
                isDefaultDisplay = win.isDefaultDisplay();
                if (isDefaultDisplay) {
                    if (win.mViewVisibility == viewVisibility) {
                    }
                }
                if (win.mViewVisibility == viewVisibility) {
                }
                if ((DumpState.DUMP_DEXOPT & flagChanges) == 0) {
                }
                wallpaperMayMove |= (DumpState.DUMP_DEXOPT & flagChanges) == 0 ? 1 : 0;
                winAnimator.mSurfaceController.setSecure(isSecureLocked(win));
                win.mRelayoutCalled = true;
                win.mInRelayout = true;
                oldVisibility = win.mViewVisibility;
                win.mViewVisibility = viewVisibility;
                if (viewVisibility == 0) {
                }
                winAnimator.mEnterAnimationPending = false;
                winAnimator.mEnteringAnimation = false;
                if (oldVisibility == 0) {
                }
                if (win.mWillReplaceWindow) {
                    focusMayChange = tryStartExitingAnimation(win, winAnimator, isDefaultDisplay, focusMayChange);
                }
                result = 4;
                outSurface.release();
                z = false;
                if ((result & 2) == 0) {
                }
                this.mLayersController.assignLayersLocked(win.getWindowList());
                if (wallpaperMayMove) {
                    DisplayContent defaultDisplayContentLocked2 = getDefaultDisplayContentLocked();
                    defaultDisplayContentLocked2.pendingLayoutChanges |= 4;
                }
                win.setDisplayLayoutNeeded();
                if ((flags & 1) == 0) {
                }
                win.mGivenInsetsPending = (flags & 1) == 0;
                configChanged = updateOrientationFromAppTokensLocked(false);
                this.mWindowPlacerLocked.performSurfacePlacement();
                displayInfo = getDefaultDisplayInfoLocked();
                this.mWallpaperControllerLocked.updateWallpaperOffset(win, displayInfo.logicalWidth, displayInfo.logicalHeight, false);
                if (win.mAppToken != null) {
                    win.mAppToken.updateReportedVisibilityLocked();
                }
                if (winAnimator.mReportSurfaceResized) {
                    winAnimator.mReportSurfaceResized = false;
                    result |= 32;
                }
                if (this.mPolicy.isNavBarForcedShownLw(win)) {
                    result |= 64;
                }
                if (win.isGoneForLayoutLw()) {
                    win.mResizedWhileGone = false;
                }
                outFrame.set(win.mCompatFrame);
                outOverscanInsets.set(win.mOverscanInsets);
                outContentInsets.set(win.mContentInsets);
                outVisibleInsets.set(win.mVisibleInsets);
                outStableInsets.set(win.mStableInsets);
                outOutsets.set(win.mOutsets);
                outBackdropFrame.set(win.getBackdropFrame(win.mFrame));
                if (this.mInTouchMode) {
                }
                result |= this.mInTouchMode ? 1 : 0;
                this.mInputMonitor.updateInputWindowsLw(true);
                win.mInRelayout = false;
            }
        }

        private boolean tryStartExitingAnimation(WindowState win, WindowStateAnimator winAnimator, boolean isDefaultDisplay, boolean focusMayChange) {
            int transit = 2;
            if (win.mAttrs.type == 3) {
                transit = 5;
            }
            if (win.isWinVisibleLw() && winAnimator.applyAnimationLocked(transit, false)) {
                focusMayChange = isDefaultDisplay;
                win.mAnimatingExit = true;
                win.mWinAnimator.mAnimating = true;
            } else if (win.mWinAnimator.isAnimationSet()) {
                win.mAnimatingExit = true;
                win.mWinAnimator.mAnimating = true;
            } else if (this.mWallpaperControllerLocked.isWallpaperTarget(win)) {
                win.mAnimatingExit = true;
                win.mWinAnimator.mAnimating = true;
            } else {
                if (this.mInputMethodWindow == win) {
                    this.mInputMethodWindow = null;
                }
                win.destroyOrSaveSurface();
            }
            if (this.mAccessibilityController != null && win.getDisplayId() == 0) {
                this.mAccessibilityController.onWindowTransitionLocked(win, transit);
            }
            return focusMayChange;
        }

        private int createSurfaceControl(Surface outSurface, int result, WindowState win, WindowStateAnimator winAnimator) {
            if (!win.mHasSurface) {
                result |= 4;
            }
            WindowSurfaceController surfaceController = winAnimator.createSurfaceLocked();
            if (surfaceController != null) {
                surfaceController.getSurface(outSurface);
            } else {
                outSurface.release();
            }
            return result;
        }

        private int relayoutVisibleWindow(Configuration outConfig, int result, WindowState win, WindowStateAnimator winAnimator, int attrChanges, int oldVisibility) {
            int i;
            int i2 = 0;
            if (win.isVisibleLw()) {
                i = 0;
            } else {
                i = 2;
            }
            result |= i;
            if (win.mAnimatingExit) {
                Slog.d(TAG, "relayoutVisibleWindow: " + win + " mAnimatingExit=true, mRemoveOnExit=" + win.mRemoveOnExit + ", mDestroying=" + win.mDestroying);
                winAnimator.cancelExitAnimationForNextAnimationLocked();
                win.mAnimatingExit = false;
            }
            if (win.mDestroying) {
                win.mDestroying = false;
                this.mDestroySurface.remove(win);
            }
            if (oldVisibility == 8) {
                winAnimator.mEnterAnimationPending = true;
            }
            winAnimator.mEnteringAnimation = true;
            if ((result & 2) != 0) {
                win.prepareWindowToDisplayDuringRelayout(outConfig);
            } else if (!(this.mPowerManager.isScreenOn() || (win.mAttrs.flags & 2097152) == 0 || win.mOwnerUid != 1001)) {
                this.mPowerManager.wakeUp(SystemClock.uptimeMillis());
            }
            if (!((attrChanges & 8) == 0 || winAnimator.tryChangeFormatInPlaceLocked())) {
                winAnimator.preserveSurfaceLocked();
                result |= 6;
            }
            if (win.isDragResizeChanged() || win.isResizedWhileNotDragResizing()) {
                win.setDragResizing();
                win.setResizedWhileNotDragResizing(false);
                if (win.mHasSurface && win.mAttachedWindow == null) {
                    winAnimator.preserveSurfaceLocked();
                    result |= 2;
                }
            }
            boolean freeformResizing = win.isDragResizing() ? win.getResizeMode() == 0 : false;
            boolean dockedResizing = win.isDragResizing() ? win.getResizeMode() == 1 : false;
            if (freeformResizing) {
                i = 16;
            } else {
                i = 0;
            }
            result |= i;
            if (dockedResizing) {
                i2 = 8;
            }
            result |= i2;
            if (win.isAnimatingWithSavedSurface()) {
                return result | 2;
            }
            return result;
        }

        public void performDeferredDestroyWindow(Session session, IWindow client) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    WindowState win = windowForClientLocked(session, client, false);
                    if (win == null || win.mWillReplaceWindow) {
                        Binder.restoreCallingIdentity(origId);
                        return;
                    }
                    win.mWinAnimator.destroyDeferredSurfaceLocked();
                    Binder.restoreCallingIdentity(origId);
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
        }

        public boolean outOfMemoryWindow(Session session, IWindow client) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    WindowState win = windowForClientLocked(session, client, false);
                    if (win == null) {
                        Binder.restoreCallingIdentity(origId);
                        return false;
                    }
                    boolean reclaimSomeSurfaceMemoryLocked = reclaimSomeSurfaceMemoryLocked(win.mWinAnimator, "from-client", false);
                    Binder.restoreCallingIdentity(origId);
                    return reclaimSomeSurfaceMemoryLocked;
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
        }

        public void finishDrawingWindow(Session session, IWindow client) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    WindowState win = windowForClientLocked(session, client, false);
                    if (win != null && win.mWinAnimator.finishDrawingLocked()) {
                        if ((win.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                            DisplayContent defaultDisplayContentLocked = getDefaultDisplayContentLocked();
                            defaultDisplayContentLocked.pendingLayoutChanges |= 4;
                        }
                        win.setDisplayLayoutNeeded();
                        this.mWindowPlacerLocked.requestTraversal();
                    }
                }
                Binder.restoreCallingIdentity(origId);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
        }

        protected boolean isDisplayOkForAnimation(int width, int height, int transit, AppWindowToken atoken) {
            return okToDisplay();
        }

        private boolean applyAnimationLocked(AppWindowToken atoken, LayoutParams lp, int transit, boolean enter, boolean isVoiceInteraction) {
            DisplayInfo displayInfo = getDefaultDisplayInfoLocked();
            int width = displayInfo.appWidth;
            int height = displayInfo.appHeight;
            if (isDisplayOkForAnimation(width, height, transit, atoken)) {
                WindowState win = atoken.findMainWindow();
                Rect frame = new Rect(0, 0, width, height);
                Rect displayFrame = new Rect(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
                Rect insets = new Rect();
                Rect surfaceInsets = null;
                boolean inFreeformWorkspace = win != null ? win.inFreeformWorkspace() : false;
                if (win != null) {
                    if (inFreeformWorkspace) {
                        frame.set(win.mFrame);
                    } else {
                        frame.set(win.mContainingFrame);
                    }
                    surfaceInsets = win.getAttrs().surfaceInsets;
                    insets.set(win.mContentInsets);
                }
                if (atoken.mLaunchTaskBehind) {
                    enter = false;
                }
                Animation a = this.mAppTransition.loadAnimation(lp, transit, enter, this.mCurConfiguration.uiMode, this.mCurConfiguration.orientation, frame, displayFrame, insets, surfaceInsets, isVoiceInteraction, inFreeformWorkspace, atoken.mTask.mTaskId);
                if (a != null) {
                    atoken.mAppAnimator.setAnimation(a, frame.width(), frame.height(), this.mAppTransition.canSkipFirstFrame(), this.mAppTransition.getAppStackClipMode());
                }
            } else {
                atoken.mAppAnimator.clearAnimation();
            }
            if (atoken.mAppAnimator.animation != null) {
                return true;
            }
            return false;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void validateAppTokens(int stackId, List<TaskGroup> tasks) {
            synchronized (this.mWindowMap) {
                int t = tasks.size() - 1;
                if (t < 0) {
                    Slog.w("WindowManager", "validateAppTokens: empty task list");
                    return;
                }
                int taskId = ((TaskGroup) tasks.get(0)).taskId;
                DisplayContent displayContent = ((Task) this.mTaskIdToTask.get(taskId)).getDisplayContent();
                if (displayContent == null) {
                    Slog.w("WindowManager", "validateAppTokens: no Display for taskId=" + taskId);
                    return;
                }
                ArrayList<Task> localTasks = ((TaskStack) this.mStackIdToStack.get(stackId)).getTasks();
                int taskNdx = localTasks.size() - 1;
                while (taskNdx >= 0 && t >= 0) {
                    AppTokenList localTokens = ((Task) localTasks.get(taskNdx)).mAppTokens;
                    TaskGroup task = (TaskGroup) tasks.get(t);
                    List<IApplicationToken> tokens = task.tokens;
                    DisplayContent lastDisplayContent = displayContent;
                    displayContent = ((Task) this.mTaskIdToTask.get(taskId)).getDisplayContent();
                    if (displayContent == lastDisplayContent) {
                        int tokenNdx = localTokens.size() - 1;
                        int v = task.tokens.size() - 1;
                        while (tokenNdx >= 0 && v >= 0) {
                            AppWindowToken atoken = (AppWindowToken) localTokens.get(tokenNdx);
                            if (atoken.removed) {
                                tokenNdx--;
                            } else if (tokens.get(v) != atoken.token) {
                                break;
                            } else {
                                tokenNdx--;
                                v--;
                            }
                        }
                        if (tokenNdx >= 0 || v >= 0) {
                            break;
                        }
                        taskNdx--;
                        t--;
                    } else {
                        Slog.w("WindowManager", "validateAppTokens: displayContent changed in TaskGroup list!");
                        return;
                    }
                }
                if (taskNdx >= 0 || t >= 0) {
                    Slog.w("WindowManager", "validateAppTokens: Mismatch! ActivityManager=" + tasks);
                    Slog.w("WindowManager", "validateAppTokens: Mismatch! WindowManager=" + localTasks);
                    Slog.w("WindowManager", "validateAppTokens: Mismatch! Callers=" + Debug.getCallers(4));
                }
            }
        }

        public void validateStackOrder(Integer[] remoteStackIds) {
        }

        boolean checkCallingPermission(String permission, String func) {
            if (Binder.getCallingPid() == Process.myPid() || this.mContext.checkCallingPermission(permission) == 0) {
                return true;
            }
            Slog.w("WindowManager", "Permission Denial: " + func + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + permission);
            return false;
        }

        boolean okToDisplay() {
            return (this.mDisplayFrozen || !this.mDisplayEnabled) ? false : this.mPolicy.isScreenOn();
        }

        AppWindowToken findAppWindowToken(IBinder token) {
            WindowToken wtoken = (WindowToken) this.mTokenMap.get(token);
            if (wtoken == null) {
                return null;
            }
            return wtoken.appWindowToken;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void addWindowToken(IBinder token, int type) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "addWindowToken()")) {
                synchronized (this.mWindowMap) {
                    if (((WindowToken) this.mTokenMap.get(token)) != null) {
                        Slog.w("WindowManager", "Attempted to add existing input method token: " + token);
                        return;
                    }
                    WindowToken wtoken = new WindowToken(this, token, type, true);
                    this.mTokenMap.put(token, wtoken);
                    if (type == 2013) {
                        this.mWallpaperControllerLocked.addWallpaperToken(wtoken);
                    }
                }
            } else {
                throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
            }
        }

        public void removeWindowToken(IBinder token) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "removeWindowToken()")) {
                long origId = Binder.clearCallingIdentity();
                synchronized (this.mWindowMap) {
                    DisplayContent displayContent = null;
                    WindowToken wtoken = (WindowToken) this.mTokenMap.remove(token);
                    if (wtoken != null) {
                        boolean delayed = false;
                        if (!wtoken.hidden) {
                            int N = wtoken.windows.size();
                            boolean changed = false;
                            for (int i = 0; i < N; i++) {
                                WindowState win = (WindowState) wtoken.windows.get(i);
                                displayContent = win.getDisplayContent();
                                if (win.mWinAnimator.isAnimationSet()) {
                                    delayed = true;
                                }
                                if (win.isVisibleNow()) {
                                    win.mWinAnimator.applyAnimationLocked(2, false);
                                    if (this.mAccessibilityController != null && win.isDefaultDisplay()) {
                                        this.mAccessibilityController.onWindowTransitionLocked(win, 2);
                                    }
                                    changed = true;
                                    if (displayContent != null) {
                                        displayContent.layoutNeeded = true;
                                    }
                                }
                            }
                            wtoken.hidden = true;
                            if (changed) {
                                this.mWindowPlacerLocked.performSurfacePlacement();
                                updateFocusedWindowLocked(0, false);
                            }
                            if (delayed && displayContent != null) {
                                displayContent.mExitingTokens.add(wtoken);
                            } else if (wtoken.windowType == 2013) {
                                this.mWallpaperControllerLocked.removeWallpaperToken(wtoken);
                            }
                        } else if (wtoken.windowType == 2013) {
                            this.mWallpaperControllerLocked.removeWallpaperToken(wtoken);
                        }
                        this.mInputMonitor.updateInputWindowsLw(true);
                    } else {
                        Slog.w("WindowManager", "Attempted to remove non-existing token: " + token);
                    }
                }
                Binder.restoreCallingIdentity(origId);
                return;
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        private Task createTaskLocked(int taskId, int stackId, int userId, AppWindowToken atoken, Rect bounds, Configuration config) {
            TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
            if (stack == null) {
                throw new IllegalArgumentException("addAppToken: invalid stackId=" + stackId);
            }
            boolean z;
            EventLog.writeEvent(EventLogTags.WM_TASK_CREATED, new Object[]{Integer.valueOf(taskId), Integer.valueOf(stackId)});
            Task task = new Task(taskId, stack, userId, this, bounds, config);
            this.mTaskIdToTask.put(taskId, task);
            if (atoken.mLaunchTaskBehind) {
                z = false;
            } else {
                z = true;
            }
            stack.addTask(task, z, atoken.showForAllUsers);
            return task;
        }

        public void addAppToken(int addPos, IApplicationToken token, int taskId, int stackId, int requestedOrientation, boolean fullscreen, boolean showForAllUsers, int userId, int configChanges, boolean voiceInteraction, boolean launchTaskBehind, Rect taskBounds, Configuration config, int taskResizeMode, boolean alwaysFocusable, boolean homeTask, int targetSdkVersion) {
            addAppToken(addPos, token, taskId, stackId, requestedOrientation, fullscreen, showForAllUsers, userId, configChanges, voiceInteraction, launchTaskBehind, taskBounds, config, taskResizeMode, alwaysFocusable, homeTask, targetSdkVersion, false);
        }

        public void addAppToken(int addPos, IApplicationToken token, int taskId, int stackId, int requestedOrientation, boolean fullscreen, boolean showForAllUsers, int userId, int configChanges, boolean voiceInteraction, boolean launchTaskBehind, Rect taskBounds, Configuration config, int taskResizeMode, boolean alwaysFocusable, boolean homeTask, int targetSdkVersion, boolean naviBarHide) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "addAppToken()")) {
                long inputDispatchingTimeoutNanos;
                try {
                    inputDispatchingTimeoutNanos = token.getKeyDispatchingTimeout() * 1000000;
                } catch (RemoteException ex) {
                    Slog.w("WindowManager", "Could not get dispatching timeout.", ex);
                    inputDispatchingTimeoutNanos = DEFAULT_INPUT_DISPATCHING_TIMEOUT_NANOS;
                }
                synchronized (this.mWindowMap) {
                    if (findAppWindowToken(token.asBinder()) != null) {
                        Slog.w("WindowManager", "Attempted to add existing app token: " + token);
                        return;
                    }
                    AppWindowToken atoken = new AppWindowToken(this, token, voiceInteraction);
                    atoken.inputDispatchingTimeoutNanos = inputDispatchingTimeoutNanos;
                    atoken.appFullscreen = fullscreen;
                    atoken.showForAllUsers = showForAllUsers;
                    atoken.targetSdk = targetSdkVersion;
                    atoken.requestedOrientation = requestedOrientation;
                    atoken.navigationBarHide = naviBarHide;
                    atoken.layoutConfigChanges = (configChanges & 1152) != 0;
                    atoken.mLaunchTaskBehind = launchTaskBehind;
                    atoken.mAlwaysFocusable = alwaysFocusable;
                    Task task = (Task) this.mTaskIdToTask.get(taskId);
                    if (task == null) {
                        task = createTaskLocked(taskId, stackId, userId, atoken, taskBounds, config);
                    }
                    task.addAppToken(addPos, atoken, taskResizeMode, homeTask);
                    this.mTokenMap.put(token.asBinder(), atoken);
                    atoken.hidden = true;
                    atoken.hiddenRequested = true;
                    return;
                }
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public void setAppTask(IBinder token, int taskId, int stackId, Rect taskBounds, Configuration config, int taskResizeMode, boolean homeTask) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setAppTask()")) {
                synchronized (this.mWindowMap) {
                    AppWindowToken atoken = findAppWindowToken(token);
                    if (atoken == null) {
                        Slog.w("WindowManager", "Attempted to set task id of non-existing app token: " + token);
                        return;
                    }
                    Task oldTask = atoken.mTask;
                    oldTask.removeAppToken(atoken);
                    Task newTask = (Task) this.mTaskIdToTask.get(taskId);
                    if (newTask == null) {
                        newTask = createTaskLocked(taskId, stackId, oldTask.mUserId, atoken, taskBounds, config);
                    }
                    newTask.addAppToken(Integer.MAX_VALUE, atoken, taskResizeMode, homeTask);
                    return;
                }
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public int getOrientationLocked() {
            AppWindowToken appShowWhenLocked = null;
            if (!this.mDisplayFrozen) {
                int req;
                WindowList windows = getDefaultWindowListLocked();
                for (int pos = windows.size() - 1; pos >= 0; pos--) {
                    WindowState win = (WindowState) windows.get(pos);
                    if (win.mAppToken != null) {
                        break;
                    }
                    if (win.isVisibleLw() && win.mPolicyVisibilityAfterAnim) {
                        req = win.mAttrs.screenOrientation;
                        if (!(req == -1 || req == 3)) {
                            Flog.i(308, win + " forcing orientation to " + req);
                            if (this.mPolicy.isKeyguardHostWindow(win.mAttrs)) {
                                this.mLastKeyguardForcedOrientation = req;
                            }
                            this.mLastWindowForcedOrientation = req;
                            return req;
                        }
                    }
                }
                this.mLastWindowForcedOrientation = -1;
                if (this.mPolicy.isKeyguardLocked()) {
                    WindowState winShowWhenLocked = (WindowState) this.mPolicy.getWinShowWhenLockedLw();
                    if (winShowWhenLocked != null) {
                        appShowWhenLocked = winShowWhenLocked.mAppToken;
                    }
                    if (appShowWhenLocked == null) {
                        return this.mLastKeyguardForcedOrientation;
                    }
                    req = appShowWhenLocked.requestedOrientation;
                    if (req == 3) {
                        req = this.mLastKeyguardForcedOrientation;
                    }
                    return req;
                }
            } else if (this.mLastWindowForcedOrientation != -1) {
                Flog.i(308, "Display is frozen, return " + this.mLastWindowForcedOrientation);
                return this.mLastWindowForcedOrientation;
            }
            return getAppSpecifiedOrientation();
        }

        protected boolean checkAppOrientationForForceRotation(AppWindowToken aToken) {
            return false;
        }

        private int getAppSpecifiedOrientation() {
            boolean inMultiWindow;
            int lastOrientation = -1;
            boolean findingBehind = false;
            boolean lastFullscreen = false;
            ArrayList<Task> tasks = getDefaultDisplayContentLocked().getTasks();
            if (isStackVisibleLocked(3)) {
                inMultiWindow = true;
            } else {
                inMultiWindow = isStackVisibleLocked(2);
            }
            boolean dockMinimized = getDefaultDisplayContentLocked().mDividerControllerLocked.isMinimizedDock();
            for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
                int firstToken = tokens.size() - 1;
                for (int tokenNdx = firstToken; tokenNdx >= 0; tokenNdx--) {
                    AppWindowToken atoken = (AppWindowToken) tokens.get(tokenNdx);
                    if (!findingBehind && !atoken.hidden && atoken.hiddenRequested) {
                        Slog.v(TAG, "Skipping " + atoken + " -- going to hide");
                    } else if (tokenNdx == firstToken && lastOrientation != 3 && r7) {
                        return lastOrientation;
                    } else {
                        if (!atoken.hiddenRequested && (!inMultiWindow || (atoken.mTask.isHomeTask() && dockMinimized))) {
                            if (tokenNdx == 0) {
                                lastOrientation = atoken.requestedOrientation;
                            }
                            int or = atoken.requestedOrientation;
                            if (checkAppOrientationForForceRotation(atoken)) {
                                return -1;
                            }
                            lastFullscreen = atoken.appFullscreen;
                            if (lastFullscreen && or != 3) {
                                return or;
                            }
                            if (or != -1 && or != 3) {
                                return or;
                            }
                            findingBehind |= or == 3 ? 1 : 0;
                        }
                    }
                }
            }
            return inMultiWindow ? -1 : this.mForcedAppOrientation;
        }

        public Configuration updateOrientationFromAppTokens(Configuration currentConfig, IBinder freezeThisOneIfNeeded) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "updateOrientationFromAppTokens()")) {
                Configuration config;
                long ident = Binder.clearCallingIdentity();
                synchronized (this.mWindowMap) {
                    config = updateOrientationFromAppTokensLocked(currentConfig, freezeThisOneIfNeeded);
                }
                Binder.restoreCallingIdentity(ident);
                return config;
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        private Configuration updateOrientationFromAppTokensLocked(Configuration currentConfig, IBinder freezeThisOneIfNeeded) {
            if (!this.mDisplayReady) {
                return null;
            }
            Configuration config = null;
            if (updateOrientationFromAppTokensLocked(false)) {
                if (freezeThisOneIfNeeded != null) {
                    AppWindowToken atoken = findAppWindowToken(freezeThisOneIfNeeded);
                    if (atoken != null) {
                        startAppFreezingScreenLocked(atoken);
                    }
                }
                config = computeNewConfigurationLocked();
            } else if (currentConfig != null) {
                this.mTempConfiguration.setToDefaults();
                this.mTempConfiguration.updateFrom(currentConfig);
                computeScreenConfigurationLocked(this.mTempConfiguration);
                if (currentConfig.diff(this.mTempConfiguration) != 0) {
                    this.mWaitingForConfig = true;
                    DisplayContent displayContent = getDefaultDisplayContentLocked();
                    displayContent.layoutNeeded = true;
                    int[] anim = new int[2];
                    if (displayContent.isDimming()) {
                        anim[1] = 0;
                        anim[0] = 0;
                    } else {
                        this.mPolicy.selectRotationAnimationLw(anim);
                    }
                    if (!this.mIgnoreFrozen) {
                        startFreezingDisplayLocked(false, anim[0], anim[1]);
                    }
                    config = new Configuration(this.mTempConfiguration);
                }
            }
            if (this.mIgnoreFrozen) {
                this.mIgnoreFrozen = false;
            }
            return config;
        }

        boolean updateOrientationFromAppTokensLocked(boolean inTransaction) {
            long ident = Binder.clearCallingIdentity();
            try {
                int req = getOrientationLocked();
                if (req != this.mForcedAppOrientation) {
                    this.mForcedAppOrientation = req;
                    this.mPolicy.setCurrentOrientationLw(req);
                    if (updateRotationUncheckedLocked(inTransaction)) {
                        return true;
                    }
                }
                Binder.restoreCallingIdentity(ident);
                return false;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int[] setNewConfiguration(Configuration config) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setNewConfiguration()")) {
                int[] onConfigurationChanged;
                long callingId = Binder.clearCallingIdentity();
                try {
                    if ((this.mCurConfiguration.diff(config) & DumpState.DUMP_VERSION) == 0 && (this.mCurConfiguration.diff(config) & 4) == 0) {
                        if ((this.mCurConfiguration.diff(config) & DumpState.DUMP_INSTALLS) != 0) {
                        }
                        Binder.restoreCallingIdentity(callingId);
                        synchronized (this.mWindowMap) {
                            if (this.mWaitingForConfig) {
                                this.mWaitingForConfig = false;
                                this.mLastFinishedFreezeSource = "new-config";
                            }
                            if (this.mCurConfiguration.diff(config) == 0) {
                                return null;
                            }
                            prepareFreezingAllTaskBounds();
                            this.mCurConfiguration = new Configuration(config);
                            onConfigurationChanged = onConfigurationChanged();
                            return onConfigurationChanged;
                        }
                    }
                    ApplicationInfo appInfo = ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).getApplicationInfo("com.android.browser", this.mCurrentUserId);
                    if (appInfo != null) {
                        Slog.d(TAG, "update configuration and killUid + " + appInfo.uid + " for com.android.browser temporarily");
                        this.mActivityManager.killUid(UserHandle.getAppId(appInfo.uid), this.mCurrentUserId, null);
                    }
                    Binder.restoreCallingIdentity(callingId);
                } catch (RemoteException e) {
                    Slog.d(TAG, "fail to force stop pkg com.android.browser for temp");
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingId);
                }
                synchronized (this.mWindowMap) {
                    if (this.mWaitingForConfig) {
                        this.mWaitingForConfig = false;
                        this.mLastFinishedFreezeSource = "new-config";
                    }
                    if (this.mCurConfiguration.diff(config) == 0) {
                    }
                    if (this.mCurConfiguration.diff(config) == 0) {
                        prepareFreezingAllTaskBounds();
                        this.mCurConfiguration = new Configuration(config);
                        onConfigurationChanged = onConfigurationChanged();
                        return onConfigurationChanged;
                    }
                    return null;
                }
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public Rect getBoundsForNewConfiguration(int stackId) {
            Rect outBounds;
            synchronized (this.mWindowMap) {
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                outBounds = new Rect();
                stack.getBoundsForNewConfiguration(outBounds);
            }
            return outBounds;
        }

        private void prepareFreezingAllTaskBounds() {
            for (int i = this.mDisplayContents.size() - 1; i >= 0; i--) {
                ArrayList<TaskStack> stacks = ((DisplayContent) this.mDisplayContents.valueAt(i)).getStacks();
                for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                    ((TaskStack) stacks.get(stackNdx)).prepareFreezingTaskBounds();
                }
            }
        }

        private int[] onConfigurationChanged() {
            this.mPolicy.onConfigurationChanged();
            DisplayContent defaultDisplayContent = getDefaultDisplayContentLocked();
            if (!this.mReconfigureOnConfigurationChanged.contains(defaultDisplayContent)) {
                this.mReconfigureOnConfigurationChanged.add(defaultDisplayContent);
            }
            for (int i = this.mReconfigureOnConfigurationChanged.size() - 1; i >= 0; i--) {
                reconfigureDisplayLocked((DisplayContent) this.mReconfigureOnConfigurationChanged.remove(i));
            }
            defaultDisplayContent.getDockedDividerController().onConfigurationChanged();
            this.mChangedStackList.clear();
            for (int stackNdx = this.mStackIdToStack.size() - 1; stackNdx >= 0; stackNdx--) {
                TaskStack stack = (TaskStack) this.mStackIdToStack.valueAt(stackNdx);
                if (stack.onConfigurationChanged()) {
                    this.mChangedStackList.add(Integer.valueOf(stack.mStackId));
                }
            }
            return this.mChangedStackList.isEmpty() ? null : ArrayUtils.convertToIntArray(this.mChangedStackList);
        }

        public void setAppOrientation(IApplicationToken token, int requestedOrientation) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setAppOrientation()")) {
                synchronized (this.mWindowMap) {
                    AppWindowToken atoken = findAppWindowToken(token.asBinder());
                    if (atoken == null) {
                        Slog.w("WindowManager", "Attempted to set orientation of non-existing app token: " + token);
                        return;
                    }
                    atoken.requestedOrientation = requestedOrientation;
                    return;
                }
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public int getAppOrientation(IApplicationToken token) {
            synchronized (this.mWindowMap) {
                AppWindowToken wtoken = findAppWindowToken(token.asBinder());
                if (wtoken == null) {
                    return -1;
                }
                int i = wtoken.requestedOrientation;
                return i;
            }
        }

        void setFocusTaskRegionLocked() {
            if (this.mFocusedApp != null) {
                Task task = this.mFocusedApp.mTask;
                DisplayContent displayContent = task.getDisplayContent();
                if (displayContent != null) {
                    displayContent.setTouchExcludeRegion(task);
                }
            }
        }

        public void setFocusedApp(IBinder token, boolean moveFocusNow) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setFocusedApp()")) {
                synchronized (this.mWindowMap) {
                    AppWindowToken appWindowToken;
                    if (token == null) {
                        appWindowToken = null;
                    } else {
                        appWindowToken = findAppWindowToken(token);
                        if (appWindowToken == null) {
                            Slog.w("WindowManager", "Attempted to set focus to non-existing app token: " + token);
                        }
                    }
                    boolean changed = this.mFocusedApp != appWindowToken;
                    if (changed) {
                        this.mFocusedApp = appWindowToken;
                        this.mInputMonitor.setFocusedAppLw(appWindowToken);
                        setFocusTaskRegionLocked();
                    }
                    if (moveFocusNow && changed) {
                        long origId = Binder.clearCallingIdentity();
                        updateFocusedWindowLocked(0, true);
                        Binder.restoreCallingIdentity(origId);
                    }
                }
                return;
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public void prepareAppTransition(int transit, boolean alwaysKeepCurrent) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "prepareAppTransition()")) {
                synchronized (this.mWindowMap) {
                    if (this.mAppTransition.prepareAppTransitionLocked(transit, alwaysKeepCurrent) && okToDisplay()) {
                        this.mSkipAppTransitionAnimation = false;
                    }
                }
                return;
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public int getPendingAppTransition() {
            return this.mAppTransition.getAppTransition();
        }

        public void overridePendingAppTransition(String packageName, int enterAnim, int exitAnim, IRemoteCallback startedCallback) {
            synchronized (this.mWindowMap) {
                this.mAppTransition.overridePendingAppTransition(packageName, enterAnim, exitAnim, startedCallback);
            }
        }

        public void setExitPosition(int startX, int startY, int width, int height) {
            synchronized (this.mWindowMap) {
                this.mAppTransition.setExitPosition(startX, startY, width, height);
            }
        }

        public void overridePendingAppTransitionScaleUp(int startX, int startY, int startWidth, int startHeight) {
            synchronized (this.mWindowMap) {
                this.mAppTransition.overridePendingAppTransitionScaleUp(startX, startY, startWidth, startHeight);
            }
        }

        public void overridePendingAppTransitionClipReveal(int startX, int startY, int startWidth, int startHeight) {
            synchronized (this.mWindowMap) {
                this.mAppTransition.overridePendingAppTransitionClipReveal(startX, startY, startWidth, startHeight);
            }
        }

        public void overridePendingAppTransitionThumb(Bitmap srcThumb, int startX, int startY, IRemoteCallback startedCallback, boolean scaleUp) {
            synchronized (this.mWindowMap) {
                this.mAppTransition.overridePendingAppTransitionThumb(srcThumb, startX, startY, startedCallback, scaleUp);
            }
        }

        public void overridePendingAppTransitionAspectScaledThumb(Bitmap srcThumb, int startX, int startY, int targetWidth, int targetHeight, IRemoteCallback startedCallback, boolean scaleUp) {
            synchronized (this.mWindowMap) {
                this.mAppTransition.overridePendingAppTransitionAspectScaledThumb(srcThumb, startX, startY, targetWidth, targetHeight, startedCallback, scaleUp);
            }
        }

        public void overridePendingAppTransitionMultiThumb(AppTransitionAnimationSpec[] specs, IRemoteCallback onAnimationStartedCallback, IRemoteCallback onAnimationFinishedCallback, boolean scaleUp) {
            synchronized (this.mWindowMap) {
                this.mAppTransition.overridePendingAppTransitionMultiThumb(specs, onAnimationStartedCallback, onAnimationFinishedCallback, scaleUp);
                prolongAnimationsFromSpecs(specs, scaleUp);
            }
        }

        void prolongAnimationsFromSpecs(AppTransitionAnimationSpec[] specs, boolean scaleUp) {
            this.mTmpTaskIds.clear();
            for (int i = specs.length - 1; i >= 0; i--) {
                this.mTmpTaskIds.put(specs[i].taskId, 0);
            }
            for (WindowState win : this.mWindowMap.values()) {
                Task task = win.getTask();
                if (!(task == null || this.mTmpTaskIds.get(task.mTaskId, -1) == -1 || !task.inFreeformWorkspace())) {
                    AppWindowToken appToken = win.mAppToken;
                    if (!(appToken == null || appToken.mAppAnimator == null)) {
                        appToken.mAppAnimator.startProlongAnimation(scaleUp ? 2 : 1);
                    }
                }
            }
        }

        public void overridePendingAppTransitionInPlace(String packageName, int anim) {
            synchronized (this.mWindowMap) {
                this.mAppTransition.overrideInPlaceAppTransition(packageName, anim);
            }
        }

        public void overridePendingAppTransitionMultiThumbFuture(IAppTransitionAnimationSpecsFuture specsFuture, IRemoteCallback callback, boolean scaleUp) {
            synchronized (this.mWindowMap) {
                this.mAppTransition.overridePendingAppTransitionMultiThumbFuture(specsFuture, callback, scaleUp);
            }
        }

        public void endProlongedAnimations() {
            synchronized (this.mWindowMap) {
                for (WindowState win : this.mWindowMap.values()) {
                    AppWindowToken appToken = win.mAppToken;
                    if (!(appToken == null || appToken.mAppAnimator == null)) {
                        appToken.mAppAnimator.endProlongedAnimation();
                    }
                }
                this.mAppTransition.notifyProlongedAnimationsEnded();
            }
        }

        public void executeAppTransition() {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "executeAppTransition()")) {
                synchronized (this.mWindowMap) {
                    Flog.i(307, "Execute app transition: " + this.mAppTransition + " Callers=" + Debug.getCallers(5));
                    if (this.mAppTransition.isTransitionSet()) {
                        this.mAppTransition.setReady();
                        long origId = Binder.clearCallingIdentity();
                        try {
                            this.mWindowPlacerLocked.performSurfacePlacement();
                        } finally {
                            Binder.restoreCallingIdentity(origId);
                        }
                    }
                }
                return;
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public boolean setAppStartingWindow(IBinder token, String pkg, int theme, CompatibilityInfo compatInfo, CharSequence nonLocalizedLabel, int labelRes, int icon, int logo, int windowFlags, IBinder transferFrom, boolean createIfNeeded) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setAppStartingWindow()")) {
                synchronized (this.mWindowMap) {
                    AppWindowToken wtoken = findAppWindowToken(token);
                    if (wtoken == null) {
                        Slog.w("WindowManager", "Attempted to set icon of non-existing app token: " + token);
                        return false;
                    }
                    Flog.i(301, "setAppStartingWindow: token=" + token + " pkg=" + pkg + " transferFrom=" + transferFrom + " windowFlags=" + windowFlags + " createIfNeeded=" + createIfNeeded + " okToDisplay=" + okToDisplay());
                    if (!okToDisplay()) {
                        return false;
                    } else if (wtoken.startingData != null) {
                        return false;
                    } else {
                        if (theme != 0) {
                            Entry ent = AttributeCache.instance().get(pkg, theme, R.styleable.Window, this.mCurrentUserId);
                            if (ent == null) {
                                return false;
                            }
                            boolean windowIsTranslucent = ent.array.getBoolean(5, false);
                            boolean windowIsFloating = ent.array.getBoolean(4, false);
                            boolean windowShowWallpaper = ent.array.getBoolean(14, false);
                            boolean windowDisableStarting = ent.array.getBoolean(12, false);
                            if ("com.huawei.android.launcher".equals(pkg) || isSplitMode()) {
                                return false;
                            }
                            if (windowIsTranslucent) {
                                if (HISI_PERF_OPT) {
                                    Boolean pkgIn = (Boolean) this.mPackages.get(pkg);
                                    Boolean accel = Boolean.valueOf(pkgIn != null ? pkgIn.booleanValue() : false);
                                    if (accel.booleanValue()) {
                                        Slog.i(TAG, "setAppStartingWindow pkgname " + pkg + ".accel " + accel);
                                        if (ent.array.getResourceId(1, 0) == 0) {
                                            return false;
                                        }
                                    }
                                    return false;
                                }
                                return false;
                            }
                            if (windowIsFloating || windowDisableStarting) {
                                return false;
                            } else if (windowShowWallpaper) {
                                if (this.mWallpaperControllerLocked.getWallpaperTarget() == null) {
                                    windowFlags |= DumpState.DUMP_DEXOPT;
                                } else {
                                    return false;
                                }
                            }
                        }
                        if (transferStartingWindow(transferFrom, wtoken)) {
                            return true;
                        } else if (createIfNeeded) {
                            wtoken.startingData = new StartingData(pkg, theme, compatInfo, nonLocalizedLabel, labelRes, icon, logo, windowFlags);
                            this.mH.sendMessageAtFrontOfQueue(this.mH.obtainMessage(5, wtoken));
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        private boolean transferStartingWindow(IBinder transferFrom, AppWindowToken wtoken) {
            if (transferFrom == null) {
                return false;
            }
            AppWindowToken ttoken = findAppWindowToken(transferFrom);
            if (ttoken == null) {
                return false;
            }
            WindowState startingWindow = ttoken.startingWindow;
            if (startingWindow != null && ttoken.startingView != null) {
                this.mSkipAppTransitionAnimation = true;
                Flog.i(301, "Moving existing starting " + startingWindow + " from " + ttoken + " to " + wtoken);
                long origId = Binder.clearCallingIdentity();
                wtoken.startingData = ttoken.startingData;
                wtoken.startingView = ttoken.startingView;
                wtoken.startingDisplayed = ttoken.startingDisplayed;
                ttoken.startingDisplayed = false;
                wtoken.startingWindow = startingWindow;
                wtoken.reportedVisible = ttoken.reportedVisible;
                ttoken.startingData = null;
                ttoken.startingView = null;
                ttoken.startingWindow = null;
                ttoken.startingMoved = true;
                startingWindow.mToken = wtoken;
                startingWindow.mRootToken = wtoken;
                startingWindow.mAppToken = wtoken;
                startingWindow.getWindowList().remove(startingWindow);
                this.mWindowsChanged = true;
                ttoken.windows.remove(startingWindow);
                ttoken.allAppWindows.remove(startingWindow);
                if (wtoken.startingView == null) {
                    Slog.d(TAG, "***transferFrom view error for starting window: " + startingWindow);
                    this.mH.sendMessageAtFrontOfQueue(this.mH.obtainMessage(5, wtoken));
                    Binder.restoreCallingIdentity(origId);
                    return true;
                }
                addWindowToListInOrderLocked(startingWindow, true);
                if (ttoken.allDrawn) {
                    wtoken.allDrawn = true;
                    wtoken.deferClearAllDrawn = ttoken.deferClearAllDrawn;
                }
                if (ttoken.firstWindowDrawn) {
                    wtoken.firstWindowDrawn = true;
                }
                if (!ttoken.hidden) {
                    wtoken.hidden = false;
                    wtoken.hiddenRequested = false;
                }
                if (wtoken.clientHidden != ttoken.clientHidden) {
                    wtoken.clientHidden = ttoken.clientHidden;
                    wtoken.sendAppVisibilityToClients();
                }
                ttoken.mAppAnimator.transferCurrentAnimation(wtoken.mAppAnimator, startingWindow.mWinAnimator);
                updateFocusedWindowLocked(3, true);
                getDefaultDisplayContentLocked().layoutNeeded = true;
                this.mWindowPlacerLocked.performSurfacePlacement();
                Binder.restoreCallingIdentity(origId);
                return true;
            } else if (ttoken.startingData != null) {
                wtoken.startingData = ttoken.startingData;
                ttoken.startingData = null;
                ttoken.startingMoved = true;
                this.mH.sendMessageAtFrontOfQueue(this.mH.obtainMessage(5, wtoken));
                return true;
            } else {
                AppWindowAnimator tAppAnimator = ttoken.mAppAnimator;
                AppWindowAnimator wAppAnimator = wtoken.mAppAnimator;
                if (tAppAnimator.thumbnail != null) {
                    if (wAppAnimator.thumbnail != null) {
                        wAppAnimator.thumbnail.destroy();
                    }
                    wAppAnimator.thumbnail = tAppAnimator.thumbnail;
                    wAppAnimator.thumbnailLayer = tAppAnimator.thumbnailLayer;
                    wAppAnimator.thumbnailAnimation = tAppAnimator.thumbnailAnimation;
                    tAppAnimator.thumbnail = null;
                }
                return false;
            }
        }

        public void removeAppStartingWindow(IBinder token) {
            synchronized (this.mWindowMap) {
                scheduleRemoveStartingWindowLocked(((WindowToken) this.mTokenMap.get(token)).appWindowToken);
            }
        }

        public void setAppFullscreen(IBinder token, boolean toOpaque) {
            synchronized (this.mWindowMap) {
                AppWindowToken atoken = findAppWindowToken(token);
                if (atoken != null) {
                    atoken.appFullscreen = toOpaque;
                    setWindowOpaqueLocked(token, toOpaque);
                    this.mWindowPlacerLocked.requestTraversal();
                }
            }
        }

        public void setWindowOpaque(IBinder token, boolean isOpaque) {
            synchronized (this.mWindowMap) {
                setWindowOpaqueLocked(token, isOpaque);
            }
        }

        public void setWindowOpaqueLocked(IBinder token, boolean isOpaque) {
            AppWindowToken wtoken = findAppWindowToken(token);
            if (wtoken != null) {
                WindowState win = wtoken.findMainWindow();
                if (win != null) {
                    win.mWinAnimator.setOpaqueLocked(isOpaque);
                }
            }
        }

        boolean setTokenVisibilityLocked(AppWindowToken wtoken, LayoutParams lp, boolean visible, int transit, boolean performLayout, boolean isVoiceInteraction) {
            int i;
            boolean delayed = false;
            if (wtoken.clientHidden == visible) {
                wtoken.clientHidden = !visible;
                wtoken.sendAppVisibilityToClients();
            }
            boolean visibilityChanged = false;
            if (wtoken.hidden == visible || ((wtoken.hidden && wtoken.mIsExiting) || (visible && wtoken.waitingForReplacement()))) {
                boolean changed = false;
                boolean runningAppAnimation = false;
                if (transit != -1) {
                    if (wtoken.mAppAnimator.animation == AppWindowAnimator.sDummyAnimation) {
                        wtoken.mAppAnimator.setNullAnimation();
                    }
                    if (applyAnimationLocked(wtoken, lp, transit, visible, isVoiceInteraction)) {
                        runningAppAnimation = true;
                        delayed = true;
                    }
                    WindowState window = wtoken.findMainWindow();
                    if (!(window == null || this.mAccessibilityController == null || window.getDisplayId() != 0)) {
                        this.mAccessibilityController.onAppWindowTransitionLocked(window, transit);
                    }
                    changed = true;
                }
                int windowsCount = wtoken.allAppWindows.size();
                for (i = 0; i < windowsCount; i++) {
                    WindowState win = (WindowState) wtoken.allAppWindows.get(i);
                    if (win == wtoken.startingWindow) {
                        if (!visible && win.isVisibleNow() && wtoken.mAppAnimator.isAnimating()) {
                            win.mAnimatingExit = true;
                            win.mRemoveOnExit = true;
                            win.mWindowRemovalAllowed = true;
                        }
                    } else if (visible) {
                        if (!win.isVisibleNow()) {
                            if (!runningAppAnimation) {
                                win.mWinAnimator.applyAnimationLocked(1, true);
                                if (this.mAccessibilityController != null && win.getDisplayId() == 0) {
                                    this.mAccessibilityController.onWindowTransitionLocked(win, 1);
                                }
                            }
                            changed = true;
                            win.setDisplayLayoutNeeded();
                        }
                    } else if (win.isVisibleNow()) {
                        if (!runningAppAnimation) {
                            win.mWinAnimator.applyAnimationLocked(2, false);
                            if (this.mAccessibilityController != null && win.getDisplayId() == 0) {
                                this.mAccessibilityController.onWindowTransitionLocked(win, 2);
                            }
                        }
                        changed = true;
                        win.setDisplayLayoutNeeded();
                    }
                }
                boolean z = !visible;
                wtoken.hiddenRequested = z;
                wtoken.hidden = z;
                visibilityChanged = true;
                if (visible) {
                    WindowState swin = wtoken.startingWindow;
                    if (!(swin == null || swin.isDrawnLw())) {
                        swin.mPolicyVisibility = false;
                        swin.mPolicyVisibilityAfterAnim = false;
                    }
                } else {
                    unsetAppFreezingScreenLocked(wtoken, true, true);
                }
                if (changed) {
                    this.mInputMonitor.setUpdateInputWindowsNeededLw();
                    if (performLayout) {
                        updateFocusedWindowLocked(3, false);
                        this.mWindowPlacerLocked.performSurfacePlacement();
                    }
                    this.mInputMonitor.updateInputWindowsLw(false);
                }
            }
            if (wtoken.mAppAnimator.animation != null) {
                delayed = true;
            }
            for (i = wtoken.allAppWindows.size() - 1; i >= 0 && !delayed; i--) {
                if (((WindowState) wtoken.allAppWindows.get(i)).mWinAnimator.isWindowAnimationSet()) {
                    delayed = true;
                }
            }
            if (visibilityChanged) {
                if (visible && !delayed) {
                    wtoken.mEnteringAnimation = true;
                    this.mActivityManagerAppTransitionNotifier.onAppTransitionFinishedLocked(wtoken.token);
                }
                if (!(this.mClosingApps.contains(wtoken) || this.mOpeningApps.contains(wtoken))) {
                    getDefaultDisplayContentLocked().getDockedDividerController().notifyAppVisibilityChanged();
                }
            }
            return delayed;
        }

        void updateTokenInPlaceLocked(AppWindowToken wtoken, int transit) {
            if (transit != -1) {
                if (wtoken.mAppAnimator.animation == AppWindowAnimator.sDummyAnimation) {
                    wtoken.mAppAnimator.setNullAnimation();
                }
                applyAnimationLocked(wtoken, null, transit, false, false);
            }
        }

        public void notifyAppStopped(IBinder token, boolean stopped) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "notifyAppStopped()")) {
                synchronized (this.mWindowMap) {
                    AppWindowToken wtoken = findAppWindowToken(token);
                    if (wtoken == null) {
                        Slog.w("WindowManager", "Attempted to set visibility of non-existing app token: " + token);
                        return;
                    }
                    wtoken.notifyAppStopped(stopped);
                    return;
                }
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void setAppVisibility(IBinder token, boolean visible) {
            boolean z = false;
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setAppVisibility()")) {
                synchronized (this.mWindowMap) {
                    AppWindowToken wtoken = findAppWindowToken(token);
                    if (wtoken == null) {
                        Slog.w("WindowManager", "Attempted to set visibility of non-existing app token: " + token);
                        return;
                    }
                    this.mOpeningApps.remove(wtoken);
                    this.mClosingApps.remove(wtoken);
                    wtoken.waitingToShow = false;
                    if (!visible) {
                        z = true;
                    }
                    wtoken.hiddenRequested = z;
                    if (!visible) {
                        wtoken.removeAllDeadWindows();
                        wtoken.setVisibleBeforeClientHidden();
                    } else if (visible) {
                        if (!this.mAppTransition.isTransitionSet() && this.mAppTransition.isReady()) {
                            this.mOpeningApps.add(wtoken);
                        }
                        wtoken.startingMoved = false;
                        if (wtoken.hidden || wtoken.mAppStopped) {
                            wtoken.clearAllDrawn();
                            if (wtoken.hidden) {
                                wtoken.waitingToShow = true;
                            }
                            if (wtoken.clientHidden) {
                                wtoken.clientHidden = false;
                                wtoken.sendAppVisibilityToClients();
                            }
                        }
                        wtoken.requestUpdateWallpaperIfNeeded();
                        wtoken.mAppStopped = false;
                    }
                    if (okToDisplay() && this.mAppTransition.isTransitionSet()) {
                        if (wtoken.mAppAnimator.usingTransferredAnimation && wtoken.mAppAnimator.animation == null) {
                            Slog.wtf("WindowManager", "Will NOT set dummy animation on: " + wtoken + ", using null transfered animation!");
                        }
                        if (!wtoken.mAppAnimator.usingTransferredAnimation && (!wtoken.startingDisplayed || this.mSkipAppTransitionAnimation)) {
                            wtoken.mAppAnimator.setDummyAnimation();
                        }
                        wtoken.inPendingTransaction = true;
                        if (visible) {
                            this.mOpeningApps.add(wtoken);
                            wtoken.mEnteringAnimation = true;
                        } else {
                            this.mClosingApps.add(wtoken);
                            wtoken.mEnteringAnimation = false;
                        }
                        if (this.mAppTransition.getAppTransition() == 16) {
                            WindowState win = findFocusedWindowLocked(getDefaultDisplayContentLocked());
                            if (win != null) {
                                AppWindowToken focusedToken = win.mAppToken;
                                if (focusedToken != null) {
                                    focusedToken.hidden = true;
                                    this.mOpeningApps.add(focusedToken);
                                }
                            }
                        }
                    } else {
                        long origId = Binder.clearCallingIdentity();
                        wtoken.inPendingTransaction = false;
                        setTokenVisibilityLocked(wtoken, null, visible, -1, true, wtoken.voiceInteraction);
                        wtoken.updateReportedVisibilityLocked();
                        Binder.restoreCallingIdentity(origId);
                        return;
                    }
                }
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        void unsetAppFreezingScreenLocked(AppWindowToken wtoken, boolean unfreezeSurfaceNow, boolean force) {
            if (!(wtoken == null || wtoken.mAppAnimator == null || !wtoken.mAppAnimator.freezingScreen)) {
                Slog.i("WindowManager", "Clear freezing of " + wtoken + " force=" + force + " unfreezeSurfaceNow " + unfreezeSurfaceNow);
                int N = wtoken.allAppWindows.size();
                boolean unfrozeWindows = false;
                for (int i = 0; i < N; i++) {
                    WindowState w = (WindowState) wtoken.allAppWindows.get(i);
                    if (w.mAppFreezing) {
                        w.mAppFreezing = false;
                        if (!(!w.mHasSurface || w.mOrientationChanging || this.mWindowsFreezingScreen == 2)) {
                            w.mOrientationChanging = true;
                            this.mWindowPlacerLocked.mOrientationChangeComplete = false;
                        }
                        w.mLastFreezeDuration = 0;
                        unfrozeWindows = true;
                        w.setDisplayLayoutNeeded();
                    }
                }
                if (force || unfrozeWindows) {
                    wtoken.mAppAnimator.freezingScreen = false;
                    wtoken.mAppAnimator.lastFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mDisplayFreezeTime);
                    this.mAppsFreezingScreen--;
                    this.mLastFinishedFreezeSource = wtoken;
                }
                if (unfreezeSurfaceNow) {
                    if (unfrozeWindows) {
                        this.mWindowPlacerLocked.performSurfacePlacement();
                    }
                    stopFreezingDisplayLocked();
                }
            }
        }

        private void startAppFreezingScreenLocked(AppWindowToken wtoken) {
            logWithStack(TAG, "Set freezing of " + wtoken.appToken + ": hidden=" + wtoken.hidden + " freezing=" + wtoken.mAppAnimator.freezingScreen);
            if (!wtoken.hiddenRequested) {
                if (!wtoken.mAppAnimator.freezingScreen) {
                    wtoken.mAppAnimator.freezingScreen = true;
                    wtoken.mAppAnimator.lastFreezeDuration = 0;
                    this.mAppsFreezingScreen++;
                    if (this.mAppsFreezingScreen == 1) {
                        startFreezingDisplayLocked(false, 0, 0);
                        this.mH.removeMessages(17);
                        this.mH.sendEmptyMessageDelayed(17, 2000);
                    }
                }
                int N = wtoken.allAppWindows.size();
                for (int i = 0; i < N; i++) {
                    ((WindowState) wtoken.allAppWindows.get(i)).mAppFreezing = true;
                }
            }
        }

        public void startAppFreezingScreen(IBinder token, int configChanges) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setAppFreezingScreen()")) {
                synchronized (this.mWindowMap) {
                    if (configChanges == 0) {
                        if (okToDisplay()) {
                            return;
                        }
                    }
                    AppWindowToken wtoken = findAppWindowToken(token);
                    if (wtoken == null || wtoken.appToken == null) {
                        Slog.w("WindowManager", "Attempted to freeze screen with non-existing app token: " + wtoken);
                        return;
                    }
                    long origId = Binder.clearCallingIdentity();
                    startAppFreezingScreenLocked(wtoken);
                    Binder.restoreCallingIdentity(origId);
                    return;
                }
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void stopAppFreezingScreen(IBinder token, boolean force) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setAppFreezingScreen()")) {
                synchronized (this.mWindowMap) {
                    AppWindowToken wtoken = findAppWindowToken(token);
                    if (wtoken == null || wtoken.appToken == null) {
                    } else {
                        long origId = Binder.clearCallingIdentity();
                        unsetAppFreezingScreenLocked(wtoken, true, force);
                        Binder.restoreCallingIdentity(origId);
                        return;
                    }
                }
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public void removeAppToken(IBinder token) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "removeAppToken()")) {
                AppWindowToken appWindowToken = null;
                AppWindowToken appWindowToken2 = null;
                boolean z = false;
                long origId = Binder.clearCallingIdentity();
                synchronized (this.mWindowMap) {
                    WindowToken basewtoken = (WindowToken) this.mTokenMap.remove(token);
                    if (basewtoken != null) {
                        appWindowToken = basewtoken.appWindowToken;
                        if (appWindowToken != null) {
                            z = setTokenVisibilityLocked(appWindowToken, null, false, -1, true, appWindowToken.voiceInteraction);
                            appWindowToken.inPendingTransaction = false;
                            this.mOpeningApps.remove(appWindowToken);
                            appWindowToken.waitingToShow = false;
                            if (this.mClosingApps.contains(appWindowToken)) {
                                z = true;
                            } else if (this.mAppTransition.isTransitionSet()) {
                                this.mClosingApps.add(appWindowToken);
                                z = true;
                            }
                            TaskStack stack = appWindowToken.mTask.mStack;
                            if (!z || appWindowToken.allAppWindows.isEmpty()) {
                                appWindowToken.mAppAnimator.clearAnimation();
                                appWindowToken.mAppAnimator.animating = false;
                                appWindowToken.removeAppFromTaskLocked();
                            } else {
                                stack.mExitingAppTokens.add(appWindowToken);
                                appWindowToken.mIsExiting = true;
                            }
                            appWindowToken.removed = true;
                            if (appWindowToken.startingData != null) {
                                appWindowToken2 = appWindowToken;
                            }
                            unsetAppFreezingScreenLocked(appWindowToken, true, true);
                            if (this.mFocusedApp == appWindowToken) {
                                this.mFocusedApp = null;
                                updateFocusedWindowLocked(0, true);
                                this.mInputMonitor.setFocusedAppLw(null);
                            }
                            if (!(z || appWindowToken == null)) {
                                appWindowToken.updateReportedVisibilityLocked();
                            }
                            scheduleRemoveStartingWindowLocked(appWindowToken2);
                        }
                    }
                    Slog.w("WindowManager", "Attempted to remove non-existing app token: " + token);
                    appWindowToken.updateReportedVisibilityLocked();
                    scheduleRemoveStartingWindowLocked(appWindowToken2);
                }
                Binder.restoreCallingIdentity(origId);
                return;
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        void scheduleRemoveStartingWindowLocked(AppWindowToken wtoken) {
            if (wtoken != null && !this.mH.hasMessages(6, wtoken)) {
                if (wtoken == null || wtoken.startingWindow != null) {
                    Flog.i(301, "Schedule remove starting " + wtoken + " startingWindow= " + wtoken.startingWindow);
                    this.mH.sendMessage(this.mH.obtainMessage(6, wtoken));
                    return;
                }
                if (wtoken.startingData != null) {
                    wtoken.startingData = null;
                }
            }
        }

        void dumpAppTokensLocked() {
            int numStacks = this.mStackIdToStack.size();
            for (int stackNdx = 0; stackNdx < numStacks; stackNdx++) {
                TaskStack stack = (TaskStack) this.mStackIdToStack.valueAt(stackNdx);
                Slog.v("WindowManager", "  Stack #" + stack.mStackId + " tasks from bottom to top:");
                ArrayList<Task> tasks = stack.getTasks();
                int numTasks = tasks.size();
                for (int taskNdx = 0; taskNdx < numTasks; taskNdx++) {
                    Task task = (Task) tasks.get(taskNdx);
                    Slog.v("WindowManager", "    Task #" + task.mTaskId + " activities from bottom to top:");
                    AppTokenList tokens = task.mAppTokens;
                    int numTokens = tokens.size();
                    for (int tokenNdx = 0; tokenNdx < numTokens; tokenNdx++) {
                        Slog.v("WindowManager", "      activity #" + tokenNdx + ": " + ((AppWindowToken) tokens.get(tokenNdx)).token);
                    }
                }
            }
        }

        void dumpWindowsLocked() {
            int numDisplays = this.mDisplayContents.size();
            for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                DisplayContent displayContent = (DisplayContent) this.mDisplayContents.valueAt(displayNdx);
                Slog.v("WindowManager", " Display #" + displayContent.getDisplayId());
                WindowList windows = displayContent.getWindowList();
                for (int winNdx = windows.size() - 1; winNdx >= 0; winNdx--) {
                    Slog.v("WindowManager", "  #" + winNdx + ": " + windows.get(winNdx));
                }
            }
        }

        private final int reAddWindowLocked(int index, WindowState win) {
            WindowList windows = win.getWindowList();
            int NCW = win.mChildWindows.size();
            boolean winAdded = false;
            for (int j = 0; j < NCW; j++) {
                WindowState cwin = (WindowState) win.mChildWindows.get(j);
                if (!winAdded && cwin.mSubLayer >= 0) {
                    win.mRebuilding = false;
                    windows.add(index, win);
                    index++;
                    winAdded = true;
                }
                cwin.mRebuilding = false;
                windows.add(index, cwin);
                index++;
            }
            if (!winAdded) {
                win.mRebuilding = false;
                windows.add(index, win);
                index++;
            }
            this.mWindowsChanged = true;
            return index;
        }

        private final int reAddAppWindowsLocked(DisplayContent displayContent, int index, WindowToken token, boolean needNotifyColor) {
            int NW = token.windows.size();
            for (int i = 0; i < NW; i++) {
                WindowState win = (WindowState) token.windows.get(i);
                DisplayContent winDisplayContent = win.getDisplayContent();
                if (winDisplayContent == displayContent || winDisplayContent == null) {
                    win.mDisplayContent = displayContent;
                    index = reAddWindowLocked(index, win);
                    if (needNotifyColor && win.canCarryColors() && win.isWinVisibleLw()) {
                        this.mPolicy.updateSystemUiColorLw(win);
                    }
                }
            }
            return index;
        }

        void moveStackWindowsLocked(DisplayContent displayContent) {
            WindowList windows = displayContent.getWindowList();
            this.mTmpWindows.addAll(windows);
            rebuildAppWindowListLocked(displayContent);
            int tmpSize = this.mTmpWindows.size();
            int winSize = windows.size();
            int tmpNdx = 0;
            int winNdx = 0;
            while (tmpNdx < tmpSize && winNdx < winSize) {
                while (true) {
                    int tmpNdx2 = tmpNdx + 1;
                    WindowState tmp = (WindowState) this.mTmpWindows.get(tmpNdx);
                    if (tmpNdx2 >= tmpSize || tmp.mAppToken == null || !tmp.mAppToken.mIsExiting) {
                        while (true) {
                            int winNdx2 = winNdx + 1;
                            WindowState win = (WindowState) windows.get(winNdx);
                            winNdx = winNdx2;
                        }
                    } else {
                        tmpNdx = tmpNdx2;
                    }
                }
                while (true) {
                    int winNdx22 = winNdx + 1;
                    WindowState win2 = (WindowState) windows.get(winNdx);
                    if (winNdx22 < winSize && win2.mAppToken != null && win2.mAppToken.mIsExiting) {
                        winNdx = winNdx22;
                    } else if (tmp != win2) {
                        displayContent.layoutNeeded = true;
                        winNdx = winNdx22;
                        tmpNdx = tmpNdx2;
                        break;
                    } else {
                        winNdx = winNdx22;
                        tmpNdx = tmpNdx2;
                    }
                }
                if (tmp != win2) {
                    displayContent.layoutNeeded = true;
                    winNdx = winNdx22;
                    tmpNdx = tmpNdx2;
                    break;
                }
                winNdx = winNdx22;
                tmpNdx = tmpNdx2;
            }
            if (tmpNdx != winNdx) {
                displayContent.layoutNeeded = true;
            }
            this.mTmpWindows.clear();
            if (!updateFocusedWindowLocked(3, false)) {
                this.mLayersController.assignLayersLocked(displayContent.getWindowList());
            }
            this.mInputMonitor.setUpdateInputWindowsNeededLw();
            this.mWindowPlacerLocked.performSurfacePlacement();
            this.mInputMonitor.updateInputWindowsLw(false);
        }

        public void moveTaskToTop(int taskId) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    Task task = (Task) this.mTaskIdToTask.get(taskId);
                    if (task == null) {
                        Binder.restoreCallingIdentity(origId);
                        return;
                    }
                    TaskStack stack = task.mStack;
                    DisplayContent displayContent = task.getDisplayContent();
                    if (displayContent == null) {
                        Binder.restoreCallingIdentity(origId);
                        return;
                    }
                    displayContent.moveStack(stack, true);
                    if (displayContent.isDefaultDisplay) {
                        TaskStack homeStack = displayContent.getHomeStack();
                        if (homeStack != stack) {
                            displayContent.moveStack(homeStack, false);
                        }
                    }
                    stack.moveTaskToTop(task);
                    if (this.mAppTransition.isTransitionSet()) {
                        task.setSendingToBottom(false);
                    }
                    moveStackWindowsLocked(displayContent);
                    Binder.restoreCallingIdentity(origId);
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
        }

        public void moveTaskToBottom(int taskId) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    Task task = (Task) this.mTaskIdToTask.get(taskId);
                    if (task == null) {
                        Slog.e("WindowManager", "moveTaskToBottom: taskId=" + taskId + " not found in mTaskIdToTask");
                        Binder.restoreCallingIdentity(origId);
                        return;
                    }
                    TaskStack stack = task.mStack;
                    stack.moveTaskToBottom(task);
                    if (this.mAppTransition.isTransitionSet()) {
                        task.setSendingToBottom(true);
                    }
                    moveStackWindowsLocked(stack.getDisplayContent());
                    Binder.restoreCallingIdentity(origId);
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
        }

        boolean isStackVisibleLocked(int stackId) {
            TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
            return stack != null ? stack.isVisibleLocked() : false;
        }

        public void setDockedStackCreateState(int mode, Rect bounds) {
            synchronized (this.mWindowMap) {
                setDockedStackCreateStateLocked(mode, bounds);
            }
        }

        void setDockedStackCreateStateLocked(int mode, Rect bounds) {
            this.mDockedStackCreateMode = mode;
            this.mDockedStackCreateBounds = bounds;
        }

        public Rect attachStack(int stackId, int displayId, boolean onTop) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    DisplayContent displayContent = (DisplayContent) this.mDisplayContents.get(displayId);
                    if (displayContent != null) {
                        TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                        if (stack == null) {
                            stack = new TaskStack(this, stackId);
                            this.mStackIdToStack.put(stackId, stack);
                            if (stackId == 3) {
                                getDefaultDisplayContentLocked().mDividerControllerLocked.notifyDockedStackExistsChanged(true);
                            }
                        }
                        stack.attachDisplayContent(displayContent);
                        displayContent.attachStack(stack, onTop);
                        if (stack.getRawFullscreen()) {
                            Binder.restoreCallingIdentity(origId);
                            return null;
                        }
                        Rect bounds = new Rect();
                        stack.getRawBounds(bounds);
                        Binder.restoreCallingIdentity(origId);
                        return bounds;
                    }
                    Binder.restoreCallingIdentity(origId);
                    return null;
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
        }

        void detachStackLocked(DisplayContent displayContent, TaskStack stack) {
            displayContent.detachStack(stack);
            stack.detachDisplay();
            if (stack.mStackId == 3) {
                getDefaultDisplayContentLocked().mDividerControllerLocked.notifyDockedStackExistsChanged(false);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void detachStack(int stackId) {
            synchronized (this.mWindowMap) {
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                if (stack != null) {
                    DisplayContent displayContent = stack.getDisplayContent();
                    if (displayContent != null) {
                        if (stack.isAnimating()) {
                            stack.mDeferDetach = true;
                            return;
                        }
                        detachStackLocked(displayContent, stack);
                    }
                }
            }
        }

        public void removeStack(int stackId) {
            synchronized (this.mWindowMap) {
                this.mStackIdToStack.remove(stackId);
            }
        }

        public void removeTask(int taskId) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task == null) {
                    return;
                }
                task.removeLocked();
            }
        }

        public void cancelTaskWindowTransition(int taskId) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task != null) {
                    task.cancelTaskWindowTransition();
                }
            }
        }

        public void cancelTaskThumbnailTransition(int taskId) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task != null) {
                    task.cancelTaskThumbnailTransition();
                }
            }
        }

        public void addTask(int taskId, int stackId, boolean toTop) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task == null) {
                    return;
                }
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                stack.addTask(task, toTop);
                stack.getDisplayContent().layoutNeeded = true;
                this.mWindowPlacerLocked.performSurfacePlacement();
            }
        }

        public void moveTaskToStack(int taskId, int stackId, boolean toTop) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task == null) {
                    return;
                }
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                if (stack == null) {
                    return;
                }
                task.moveTaskToStack(stack, toTop);
                stack.getDisplayContent().layoutNeeded = true;
                this.mWindowPlacerLocked.performSurfacePlacement();
            }
        }

        public void getStackDockedModeBounds(int stackId, Rect bounds, boolean ignoreVisibility) {
            synchronized (this.mWindowMap) {
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                if (stack != null) {
                    stack.getStackDockedModeBoundsLocked(bounds, ignoreVisibility);
                    return;
                }
                bounds.setEmpty();
            }
        }

        public void getStackBounds(int stackId, Rect bounds) {
            synchronized (this.mWindowMap) {
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                if (stack != null) {
                    stack.getBounds(bounds);
                    return;
                }
                bounds.setEmpty();
            }
        }

        public boolean resizeStack(int stackId, Rect bounds, SparseArray<Configuration> configs, SparseArray<Rect> taskBounds, SparseArray<Rect> taskTempInsetBounds) {
            boolean rawFullscreen;
            synchronized (this.mWindowMap) {
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                if (stack == null) {
                    throw new IllegalArgumentException("resizeStack: stackId " + stackId + " not found.");
                }
                if (stack.setBounds(bounds, configs, taskBounds, taskTempInsetBounds) && stack.isVisibleLocked()) {
                    stack.getDisplayContent().layoutNeeded = true;
                    this.mWindowPlacerLocked.performSurfacePlacement();
                }
                rawFullscreen = stack.getRawFullscreen();
            }
            return rawFullscreen;
        }

        public void prepareFreezingTaskBounds(int stackId) {
            synchronized (this.mWindowMap) {
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                if (stack == null) {
                    throw new IllegalArgumentException("prepareFreezingTaskBounds: stackId " + stackId + " not found.");
                }
                stack.prepareFreezingTaskBounds();
            }
        }

        public void positionTaskInStack(int taskId, int stackId, int position, Rect bounds, Configuration config) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task == null) {
                    return;
                }
                TaskStack stack = (TaskStack) this.mStackIdToStack.get(stackId);
                if (stack == null) {
                    return;
                }
                task.positionTaskInStack(stack, position, bounds, config);
                stack.getDisplayContent().layoutNeeded = true;
                this.mWindowPlacerLocked.performSurfacePlacement();
            }
        }

        public void resizeTask(int taskId, Rect bounds, Configuration configuration, boolean relayout, boolean forced) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task == null) {
                    throw new IllegalArgumentException("resizeTask: taskId " + taskId + " not found.");
                }
                if (task.resizeLocked(bounds, configuration, forced) && relayout) {
                    task.getDisplayContent().layoutNeeded = true;
                    this.mWindowPlacerLocked.performSurfacePlacement();
                }
            }
        }

        public void setTaskDockedResizing(int taskId, boolean resizing) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task == null) {
                    Slog.w(TAG, "setTaskDockedResizing: taskId " + taskId + " not found.");
                    return;
                }
                task.setDragResizing(resizing, 1);
            }
        }

        public void scrollTask(int taskId, Rect bounds) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task == null) {
                    throw new IllegalArgumentException("scrollTask: taskId " + taskId + " not found.");
                }
                if (task.scrollLocked(bounds)) {
                    task.getDisplayContent().layoutNeeded = true;
                    this.mInputMonitor.setUpdateInputWindowsNeededLw();
                    this.mWindowPlacerLocked.performSurfacePlacement();
                }
            }
        }

        public void deferSurfaceLayout() {
            synchronized (this.mWindowMap) {
                this.mWindowPlacerLocked.deferLayout();
            }
        }

        public void continueSurfaceLayout() {
            synchronized (this.mWindowMap) {
                this.mWindowPlacerLocked.continueLayout();
            }
        }

        public void getTaskBounds(int taskId, Rect bounds) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task != null) {
                    task.getBounds(bounds);
                    return;
                }
                bounds.setEmpty();
            }
        }

        public boolean isValidTaskId(int taskId) {
            boolean z;
            synchronized (this.mWindowMap) {
                z = this.mTaskIdToTask.get(taskId) != null;
            }
            return z;
        }

        public void startFreezingScreen(int exitAnim, int enterAnim) {
            if (checkCallingPermission("android.permission.FREEZE_SCREEN", "startFreezingScreen()")) {
                synchronized (this.mWindowMap) {
                    if (!this.mClientFreezingScreen) {
                        this.mClientFreezingScreen = true;
                        long origId = Binder.clearCallingIdentity();
                        try {
                            startFreezingDisplayLocked(false, exitAnim, enterAnim);
                            this.mH.removeMessages(30);
                            this.mH.sendEmptyMessageDelayed(30, 5000);
                        } finally {
                            Binder.restoreCallingIdentity(origId);
                        }
                    }
                }
                return;
            }
            throw new SecurityException("Requires FREEZE_SCREEN permission");
        }

        public void stopFreezingScreen() {
            if (checkCallingPermission("android.permission.FREEZE_SCREEN", "stopFreezingScreen()")) {
                synchronized (this.mWindowMap) {
                    if (this.mClientFreezingScreen) {
                        this.mClientFreezingScreen = false;
                        this.mLastFinishedFreezeSource = "client";
                        long origId = Binder.clearCallingIdentity();
                        try {
                            stopFreezingDisplayLocked();
                        } finally {
                            Binder.restoreCallingIdentity(origId);
                        }
                    }
                }
                return;
            }
            throw new SecurityException("Requires FREEZE_SCREEN permission");
        }

        public void disableKeyguard(IBinder token, String tag) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
                throw new SecurityException("Requires DISABLE_KEYGUARD permission");
            } else if (Binder.getCallingUid() != 1000 && isKeyguardSecure()) {
                Log.d("WindowManager", "current mode is SecurityMode, ignore disableKeyguard");
            } else if (Binder.getCallingUserHandle().getIdentifier() != this.mCurrentUserId) {
                Log.d("WindowManager", "non-current user, ignore disableKeyguard");
            } else if (token == null) {
                throw new IllegalArgumentException("token == null");
            } else {
                this.mKeyguardDisableHandler.sendMessage(this.mKeyguardDisableHandler.obtainMessage(1, new Pair(token, tag)));
            }
        }

        public void reenableKeyguard(IBinder token) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
                throw new SecurityException("Requires DISABLE_KEYGUARD permission");
            } else if (token == null) {
                throw new IllegalArgumentException("token == null");
            } else {
                this.mKeyguardDisableHandler.sendMessage(this.mKeyguardDisableHandler.obtainMessage(2, token));
            }
        }

        public void exitKeyguardSecurely(final IOnKeyguardExitResult callback) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
                throw new SecurityException("Requires DISABLE_KEYGUARD permission");
            } else if (callback == null) {
                throw new IllegalArgumentException("callback == null");
            } else {
                this.mPolicy.exitKeyguardSecurely(new OnKeyguardExitResult() {
                    public void onKeyguardExitResult(boolean success) {
                        try {
                            callback.onKeyguardExitResult(success);
                        } catch (RemoteException e) {
                        }
                    }
                });
            }
        }

        public boolean inKeyguardRestrictedInputMode() {
            return this.mPolicy.inKeyguardRestrictedKeyInputMode();
        }

        public boolean isKeyguardLocked() {
            return this.mPolicy.isKeyguardLocked();
        }

        public boolean isKeyguardSecure() {
            int userId = UserHandle.getCallingUserId();
            long origId = Binder.clearCallingIdentity();
            try {
                boolean isKeyguardSecure = this.mPolicy.isKeyguardSecure(userId);
                return isKeyguardSecure;
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }

        public void dismissKeyguard() {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
                throw new SecurityException("Requires DISABLE_KEYGUARD permission");
            }
            synchronized (this.mWindowMap) {
                this.mPolicy.dismissKeyguardLw();
            }
        }

        public void keyguardGoingAway(int flags) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.DISABLE_KEYGUARD") != 0) {
                throw new SecurityException("Requires DISABLE_KEYGUARD permission");
            }
            synchronized (this.mWindowMap) {
                this.mAnimator.mKeyguardGoingAway = true;
                this.mAnimator.mKeyguardGoingAwayFlags = flags;
                this.mWindowPlacerLocked.requestTraversal();
            }
        }

        public void keyguardWaitingForActivityDrawn() {
            synchronized (this.mWindowMap) {
                this.mKeyguardWaitingForActivityDrawn = true;
            }
        }

        public void notifyActivityDrawnForKeyguard() {
            synchronized (this.mWindowMap) {
                if (this.mKeyguardWaitingForActivityDrawn) {
                    this.mPolicy.notifyActivityDrawnForKeyguardLw();
                    this.mKeyguardWaitingForActivityDrawn = false;
                }
            }
        }

        void showGlobalActions() {
            this.mPolicy.showGlobalActions();
        }

        public void closeSystemDialogs(String reason) {
            synchronized (this.mWindowMap) {
                int numDisplays = this.mDisplayContents.size();
                for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                    WindowList windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                    int numWindows = windows.size();
                    for (int winNdx = 0; winNdx < numWindows; winNdx++) {
                        WindowState w = (WindowState) windows.get(winNdx);
                        if (w.mHasSurface) {
                            try {
                                w.mClient.closeSystemDialogs(reason);
                            } catch (RemoteException e) {
                            }
                        }
                    }
                }
            }
        }

        static float fixScale(float scale) {
            if (scale < 0.0f) {
                scale = 0.0f;
            } else if (scale > 20.0f) {
                scale = 20.0f;
            }
            return Math.abs(scale);
        }

        public void setAnimationScale(int which, float scale) {
            if (checkCallingPermission("android.permission.SET_ANIMATION_SCALE", "setAnimationScale()")) {
                scale = fixScale(scale);
                switch (which) {
                    case 0:
                        this.mWindowAnimationScaleSetting = scale;
                        break;
                    case 1:
                        this.mTransitionAnimationScaleSetting = scale;
                        break;
                    case 2:
                        this.mAnimatorDurationScaleSetting = scale;
                        break;
                }
                this.mH.sendEmptyMessage(14);
                return;
            }
            throw new SecurityException("Requires SET_ANIMATION_SCALE permission");
        }

        public void setAnimationScales(float[] scales) {
            if (checkCallingPermission("android.permission.SET_ANIMATION_SCALE", "setAnimationScale()")) {
                if (scales != null) {
                    if (scales.length >= 1) {
                        this.mWindowAnimationScaleSetting = fixScale(scales[0]);
                    }
                    if (scales.length >= 2) {
                        this.mTransitionAnimationScaleSetting = fixScale(scales[1]);
                    }
                    if (scales.length >= 3) {
                        this.mAnimatorDurationScaleSetting = fixScale(scales[2]);
                        dispatchNewAnimatorScaleLocked(null);
                    }
                }
                this.mH.sendEmptyMessage(14);
                return;
            }
            throw new SecurityException("Requires SET_ANIMATION_SCALE permission");
        }

        private void setAnimatorDurationScale(float scale) {
            this.mAnimatorDurationScaleSetting = scale;
            ValueAnimator.setDurationScale(scale);
        }

        public float getWindowAnimationScaleLocked() {
            return this.mAnimationsDisabled ? 0.0f : this.mWindowAnimationScaleSetting;
        }

        public float getTransitionAnimationScaleLocked() {
            return this.mAnimationsDisabled ? 0.0f : this.mTransitionAnimationScaleSetting;
        }

        public float getAnimationScale(int which) {
            switch (which) {
                case 0:
                    return this.mWindowAnimationScaleSetting;
                case 1:
                    return this.mTransitionAnimationScaleSetting;
                case 2:
                    return this.mAnimatorDurationScaleSetting;
                default:
                    return 0.0f;
            }
        }

        public float[] getAnimationScales() {
            return new float[]{this.mWindowAnimationScaleSetting, this.mTransitionAnimationScaleSetting, this.mAnimatorDurationScaleSetting};
        }

        public float getCurrentAnimatorScale() {
            float f;
            synchronized (this.mWindowMap) {
                f = this.mAnimationsDisabled ? 0.0f : this.mAnimatorDurationScaleSetting;
            }
            return f;
        }

        void dispatchNewAnimatorScaleLocked(Session session) {
            this.mH.obtainMessage(34, session).sendToTarget();
        }

        public void registerPointerEventListener(PointerEventListener listener) {
            this.mPointerEventDispatcher.registerInputEventListener(listener);
        }

        public void unregisterPointerEventListener(PointerEventListener listener) {
            this.mPointerEventDispatcher.unregisterInputEventListener(listener);
        }

        public int getLidState() {
            int sw = this.mInputManager.getSwitchState(-1, -256, 0);
            if (sw > 0) {
                return 0;
            }
            if (sw == 0) {
                return 1;
            }
            return -1;
        }

        public void lockDeviceNow() {
            lockNow(null);
        }

        public int getCameraLensCoverState() {
            int sw = this.mInputManager.getSwitchState(-1, -256, 9);
            if (sw > 0) {
                return 1;
            }
            return sw == 0 ? 0 : -1;
        }

        public void switchInputMethod(boolean forwardDirection) {
            InputMethodManagerInternal inputMethodManagerInternal = (InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class);
            if (inputMethodManagerInternal != null) {
                inputMethodManagerInternal.switchInputMethod(forwardDirection);
            }
        }

        public void shutdown(boolean confirm) {
            ShutdownThread.shutdown(this.mContext, "userrequested", confirm);
        }

        public void rebootSafeMode(boolean confirm) {
            ShutdownThread.rebootSafeMode(this.mContext, confirm);
        }

        public void setCurrentProfileIds(int[] currentProfileIds) {
            synchronized (this.mWindowMap) {
                this.mCurrentProfileIds = currentProfileIds;
            }
        }

        public void setCurrentUser(int newUserId, int[] currentProfileIds) {
            synchronized (this.mWindowMap) {
                DisplayContent displayContent;
                this.mCurrentUserId = newUserId;
                this.mCurrentProfileIds = currentProfileIds;
                this.mAppTransition.setCurrentUser(newUserId);
                this.mPolicy.setCurrentUserLw(newUserId);
                this.mPolicy.enableKeyguard(true);
                int numDisplays = this.mDisplayContents.size();
                for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                    displayContent = (DisplayContent) this.mDisplayContents.valueAt(displayNdx);
                    displayContent.switchUserStacks();
                    rebuildAppWindowListLocked(displayContent);
                }
                this.mWindowPlacerLocked.performSurfacePlacement();
                displayContent = getDefaultDisplayContentLocked();
                displayContent.mDividerControllerLocked.notifyDockedStackExistsChanged(hasDockedTasksForUser(newUserId));
                if (this.mDisplayReady) {
                    int targetDensity;
                    int forcedDensity = getForcedDisplayDensityForUserLocked(newUserId);
                    if (forcedDensity != 0) {
                        targetDensity = forcedDensity;
                    } else {
                        targetDensity = displayContent.mInitialDisplayDensity;
                    }
                    setForcedDisplayDensityLocked(displayContent, targetDensity);
                }
            }
        }

        boolean hasDockedTasksForUser(int userId) {
            TaskStack stack = (TaskStack) this.mStackIdToStack.get(3);
            if (stack == null) {
                return false;
            }
            ArrayList<Task> tasks = stack.getTasks();
            boolean hasUserTask = false;
            for (int i = tasks.size() - 1; i >= 0 && !hasUserTask; i--) {
                hasUserTask = ((Task) tasks.get(i)).mUserId == userId;
            }
            return hasUserTask;
        }

        boolean isCurrentProfileLocked(int userId) {
            if (userId == this.mCurrentUserId) {
                return true;
            }
            for (int i : this.mCurrentProfileIds) {
                if (i == userId) {
                    return true;
                }
            }
            return false;
        }

        public void enableScreenAfterBoot() {
            synchronized (this.mWindowMap) {
                if (this.mSystemBooted) {
                    return;
                }
                this.mSystemBooted = true;
                hideBootMessagesLocked();
                this.mH.sendEmptyMessageDelayed(23, 30000);
                this.mPolicy.systemBooted();
                performEnableScreen();
            }
        }

        public void enableScreenIfNeeded() {
            synchronized (this.mWindowMap) {
                enableScreenIfNeededLocked();
            }
        }

        void enableScreenIfNeededLocked() {
            if (!this.mDisplayEnabled) {
                if (this.mSystemBooted || this.mShowingBootMessages) {
                    this.mH.sendEmptyMessage(16);
                }
            }
        }

        public void performBootTimeout() {
            synchronized (this.mWindowMap) {
                if (this.mDisplayEnabled) {
                    return;
                }
                Slog.w("WindowManager", "***** BOOT TIMEOUT: forcing display enabled");
                this.mForceDisplayEnabled = true;
                performEnableScreen();
            }
        }

        private boolean checkWaitingForWindowsLocked() {
            boolean haveBootMsg = false;
            boolean haveApp = false;
            boolean haveWallpaper = false;
            boolean wallpaperEnabled = this.mContext.getResources().getBoolean(17956944) ? !this.mOnlyCore : false;
            boolean haveKeyguard = this.mBootAnimationStopped;
            WindowList windows = getDefaultWindowListLocked();
            int N = windows.size();
            for (int i = 0; i < N; i++) {
                WindowState w = (WindowState) windows.get(i);
                if (w.isVisibleLw() && !w.mObscured && !w.isDrawnLw()) {
                    return true;
                }
                if (w.isDrawnLw()) {
                    if (w.mAttrs.type == 2021) {
                        haveBootMsg = true;
                    } else if (w.mAttrs.type == 2) {
                        haveApp = true;
                    } else if (w.mAttrs.type == 2013) {
                        haveWallpaper = true;
                    } else if (w.mAttrs.type == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME) {
                        haveKeyguard = this.mPolicy.isKeyguardDrawnLw();
                    }
                }
            }
            if (!this.mSystemBooted && !haveBootMsg) {
                return true;
            }
            if (!this.mSystemBooted || ((haveApp || haveKeyguard) && (!wallpaperEnabled || haveWallpaper))) {
                return false;
            }
            return true;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void performEnableScreen() {
            synchronized (this.mWindowMap) {
                if (this.mDisplayEnabled) {
                    return;
                } else if (!this.mSystemBooted && !this.mShowingBootMessages) {
                    return;
                } else if (this.mForceDisplayEnabled || !checkWaitingForWindowsLocked()) {
                    if (!this.mBootAnimationStopped) {
                        Trace.asyncTraceBegin(32, "Stop bootanim", 0);
                        try {
                            IBinder surfaceFlinger = ServiceManager.getService("SurfaceFlinger");
                            if (surfaceFlinger != null) {
                                Flog.i(304, "******* TELLING SURFACE FLINGER WE ARE BOOTED!");
                                Parcel data = Parcel.obtain();
                                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                                surfaceFlinger.transact(1, data, null, 0);
                                data.recycle();
                            }
                        } catch (RemoteException e) {
                            Slog.e("WindowManager", "Boot completed: SurfaceFlinger is dead!");
                        }
                        this.mBootAnimationStopped = true;
                    }
                    if (this.mForceDisplayEnabled || checkBootAnimationCompleteLocked()) {
                        EventLog.writeEvent(EventLogTags.WM_BOOT_ANIMATION_DONE, SystemClock.uptimeMillis());
                        Trace.asyncTraceEnd(32, "Stop bootanim", 0);
                        this.mDisplayEnabled = true;
                        this.mInputMonitor.setEventDispatchingLw(this.mEventDispatchingEnabled);
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
            this.mPolicy.enableScreenAfterBoot();
            updateRotationUnchecked(false, false);
        }

        private boolean checkBootAnimationCompleteLocked() {
            if (!SystemService.isRunning(BOOT_ANIMATION_SERVICE)) {
                return true;
            }
            this.mH.removeMessages(37);
            this.mH.sendEmptyMessageDelayed(37, 200);
            return false;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void showBootMessage(CharSequence msg, boolean always) {
            boolean first = false;
            synchronized (this.mWindowMap) {
                if (this.mAllowBootMessages) {
                    if (!this.mShowingBootMessages) {
                        if (always) {
                            first = true;
                        } else {
                            return;
                        }
                    }
                    if (this.mSystemBooted) {
                    } else {
                        this.mShowingBootMessages = true;
                        this.mPolicy.showBootMessage(msg, always);
                    }
                }
            }
        }

        public void hideBootMessagesLocked() {
            if (this.mShowingBootMessages) {
                this.mShowingBootMessages = false;
                this.mPolicy.hideBootMessages();
            }
        }

        public void setInTouchMode(boolean mode) {
            synchronized (this.mWindowMap) {
                this.mInTouchMode = mode;
            }
        }

        private void updateCircularDisplayMaskIfNeeded() {
            if (this.mContext.getResources().getConfiguration().isScreenRound() && this.mContext.getResources().getBoolean(17957001)) {
                int currentUserId;
                synchronized (this.mWindowMap) {
                    currentUserId = this.mCurrentUserId;
                }
                int showMask = Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_display_inversion_enabled", 0, currentUserId) == 1 ? 0 : 1;
                Message m = this.mH.obtainMessage(35);
                m.arg1 = showMask;
                this.mH.sendMessage(m);
            }
        }

        public void showEmulatorDisplayOverlayIfNeeded() {
            if (this.mContext.getResources().getBoolean(17957002) && SystemProperties.getBoolean(PROPERTY_EMULATOR_CIRCULAR, false) && Build.IS_EMULATOR) {
                this.mH.sendMessage(this.mH.obtainMessage(36));
            }
        }

        public void showCircularMask(boolean visible) {
            synchronized (this.mWindowMap) {
                SurfaceControl.openTransaction();
                if (visible) {
                    try {
                        if (this.mCircularDisplayMask == null) {
                            this.mCircularDisplayMask = new CircularDisplayMask(getDefaultDisplayContentLocked().getDisplay(), this.mFxSession, (this.mPolicy.windowTypeToLayerLw(2018) * 10000) + 10, this.mContext.getResources().getInteger(17694869), this.mContext.getResources().getDimensionPixelSize(17105050));
                        }
                        this.mCircularDisplayMask.setVisibility(true);
                    } catch (Throwable th) {
                        SurfaceControl.closeTransaction();
                    }
                } else if (this.mCircularDisplayMask != null) {
                    this.mCircularDisplayMask.setVisibility(false);
                    this.mCircularDisplayMask = null;
                }
                SurfaceControl.closeTransaction();
            }
        }

        public void showEmulatorDisplayOverlay() {
            synchronized (this.mWindowMap) {
                SurfaceControl.openTransaction();
                try {
                    if (this.mEmulatorDisplayOverlay == null) {
                        this.mEmulatorDisplayOverlay = new EmulatorDisplayOverlay(this.mContext, getDefaultDisplayContentLocked().getDisplay(), this.mFxSession, (this.mPolicy.windowTypeToLayerLw(2018) * 10000) + 10);
                    }
                    this.mEmulatorDisplayOverlay.setVisibility(true);
                } finally {
                    SurfaceControl.closeTransaction();
                }
            }
        }

        public void showStrictModeViolation(boolean on) {
            this.mH.sendMessage(this.mH.obtainMessage(25, on ? 1 : 0, Binder.getCallingPid()));
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void showStrictModeViolation(int arg, int pid) {
            boolean on = arg != 0;
            synchronized (this.mWindowMap) {
                if (on) {
                    boolean isVisible = false;
                    int numDisplays = this.mDisplayContents.size();
                    for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                        WindowList windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                        int numWindows = windows.size();
                        for (int winNdx = 0; winNdx < numWindows; winNdx++) {
                            WindowState ws = (WindowState) windows.get(winNdx);
                            if (ws.mSession.mPid == pid && ws.isVisibleLw()) {
                                isVisible = true;
                                break;
                            }
                        }
                    }
                    if (!isVisible) {
                        return;
                    }
                }
                SurfaceControl.openTransaction();
                try {
                    if (this.mStrictModeFlash == null) {
                        this.mStrictModeFlash = new StrictModeFlash(getDefaultDisplayContentLocked().getDisplay(), this.mFxSession);
                    }
                    this.mStrictModeFlash.setVisibility(on);
                } finally {
                    SurfaceControl.closeTransaction();
                }
            }
        }

        public void setStrictModeVisualIndicatorPreference(String value) {
            SystemProperties.set("persist.sys.strictmode.visual", value);
        }

        private static void convertCropForSurfaceFlinger(Rect crop, int rot, int dw, int dh) {
            int tmp;
            if (rot == 1) {
                tmp = crop.top;
                crop.top = dw - crop.right;
                crop.right = crop.bottom;
                crop.bottom = dw - crop.left;
                crop.left = tmp;
            } else if (rot == 2) {
                tmp = crop.top;
                crop.top = dh - crop.bottom;
                crop.bottom = dh - tmp;
                tmp = crop.right;
                crop.right = dw - crop.left;
                crop.left = dw - tmp;
            } else if (rot == 3) {
                tmp = crop.top;
                crop.top = crop.left;
                crop.left = dh - crop.bottom;
                crop.bottom = crop.right;
                crop.right = dh - tmp;
            }
        }

        public boolean requestAssistScreenshot(final IAssistScreenshotReceiver receiver) {
            if (checkCallingPermission("android.permission.READ_FRAME_BUFFER", "requestAssistScreenshot()")) {
                FgThread.getHandler().post(new Runnable() {
                    public void run() {
                        try {
                            receiver.send(WindowManagerService.this.screenshotApplicationsInner(null, 0, -1, -1, true, 1.0f, Config.ARGB_8888));
                        } catch (RemoteException e) {
                        }
                    }
                });
                return true;
            }
            throw new SecurityException("Requires READ_FRAME_BUFFER permission");
        }

        public Bitmap screenshotApplications(IBinder appToken, int displayId, int width, int height, float frameScale) {
            if (checkCallingPermission("android.permission.READ_FRAME_BUFFER", "screenshotApplications()")) {
                try {
                    Trace.traceBegin(32, "screenshotApplications");
                    Bitmap screenshotApplicationsInner = screenshotApplicationsInner(appToken, displayId, width, height, false, frameScale, Config.RGB_565);
                    return screenshotApplicationsInner;
                } finally {
                    Trace.traceEnd(32);
                }
            } else {
                throw new SecurityException("Requires READ_FRAME_BUFFER permission");
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        Bitmap screenshotApplicationsInner(IBinder appToken, int displayId, int width, int height, boolean includeFullDisplay, float frameScale, Config config) {
            synchronized (this.mWindowMap) {
                DisplayContent displayContent = getDisplayContentLocked(displayId);
                if (displayContent == null) {
                    return null;
                }
            }
        }

        public void freezeRotation(int rotation) {
            if (!checkCallingPermission("android.permission.SET_ORIENTATION", "freezeRotation()")) {
                throw new SecurityException("Requires SET_ORIENTATION permission");
            } else if (rotation < -1 || rotation > 3) {
                throw new IllegalArgumentException("Rotation argument must be -1 or a valid rotation constant.");
            } else {
                Flog.i(308, "freezeRotation: mRotation=" + this.mRotation + ",rotation=" + rotation + ",by pid=" + Binder.getCallingPid());
                long origId = Binder.clearCallingIdentity();
                try {
                    WindowManagerPolicy windowManagerPolicy = this.mPolicy;
                    if (rotation == -1) {
                        rotation = this.mRotation;
                    }
                    windowManagerPolicy.setUserRotationMode(1, rotation);
                    updateRotationUnchecked(false, false);
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            }
        }

        public void thawRotation() {
            if (checkCallingPermission("android.permission.SET_ORIENTATION", "thawRotation()")) {
                Flog.i(308, "thawRotation: mRotation=" + this.mRotation + ", by pid=" + Binder.getCallingPid());
                long origId = Binder.clearCallingIdentity();
                try {
                    this.mPolicy.setUserRotationMode(0, 777);
                    updateRotationUnchecked(false, false);
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } else {
                throw new SecurityException("Requires SET_ORIENTATION permission");
            }
        }

        public void updateRotation(boolean alwaysSendConfiguration, boolean forceRelayout) {
            updateRotationUnchecked(alwaysSendConfiguration, forceRelayout);
        }

        void pauseRotationLocked() {
            this.mDeferredRotationPauseCount++;
        }

        void resumeRotationLocked() {
            if (this.mDeferredRotationPauseCount > 0) {
                this.mDeferredRotationPauseCount--;
                if (this.mDeferredRotationPauseCount == 0 && updateRotationUncheckedLocked(false)) {
                    this.mH.sendEmptyMessage(18);
                }
            }
        }

        public void updateRotationUnchecked(boolean alwaysSendConfiguration, boolean forceRelayout) {
            Slog.i("WindowManager", "updateRotationUnchecked(alwaysSendConfiguration=" + alwaysSendConfiguration + ")");
            long origId = Binder.clearCallingIdentity();
            synchronized (this.mWindowMap) {
                boolean changed = updateRotationUncheckedLocked(false);
                if (changed) {
                    LogPower.push(128);
                }
                if (changed) {
                    if (this.mPerfHub == null) {
                        this.mPerfHub = new PerfHub();
                    }
                    if (this.mPerfHub != null) {
                        this.mIsPerfBoost = true;
                        this.mPerfHub.perfEvent(6, "", new int[]{1});
                    }
                }
                if (!changed || forceRelayout) {
                    getDefaultDisplayContentLocked().layoutNeeded = true;
                    this.mWindowPlacerLocked.performSurfacePlacement();
                }
                if (changed) {
                    if (this.mLastFinishedFreezeSource != null) {
                        Jlog.d(58, "" + this.mLastFinishedFreezeSource);
                    } else {
                        Jlog.d(58, "");
                    }
                }
            }
            if (changed || alwaysSendConfiguration) {
                sendNewConfiguration();
            }
            Binder.restoreCallingIdentity(origId);
        }

        public boolean updateRotationUncheckedLocked(boolean inTransaction) {
            if (this.mDeferredRotationPauseCount > 0) {
                Slog.i("WindowManager", "Deferring rotation, rotation is paused.");
                return false;
            }
            ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(0);
            if (screenRotationAnimation != null && screenRotationAnimation.isAnimating()) {
                return false;
            }
            if (!this.mDisplayEnabled) {
                return false;
            }
            int rotation = this.mPolicy.rotationForOrientationLw(this.mForcedAppOrientation, this.mRotation);
            boolean altOrientation = !this.mPolicy.rotationHasCompatibleMetricsLw(this.mForcedAppOrientation, rotation);
            Slog.i("WindowManager", "Application requested orientation " + this.mForcedAppOrientation + ", got rotation " + rotation + " which has " + (altOrientation ? "incompatible" : "compatible") + " metrics");
            if (this.mRotation == rotation && this.mAltOrientation == altOrientation) {
                return false;
            }
            int i;
            Slog.i("WindowManager", "Rotation changed to " + rotation + (altOrientation ? " (alt)" : "") + " from " + this.mRotation + (this.mAltOrientation ? " (alt)" : "") + ", forceApp=" + this.mForcedAppOrientation);
            this.mRotation = rotation;
            this.mAltOrientation = altOrientation;
            this.mPolicy.setRotationLw(this.mRotation);
            this.mWindowsFreezingScreen = 1;
            this.mH.removeMessages(11);
            this.mH.sendEmptyMessageDelayed(11, 2000);
            this.mWaitingForConfig = true;
            DisplayContent displayContent = getDefaultDisplayContentLocked();
            displayContent.layoutNeeded = true;
            int[] anim = new int[2];
            if (displayContent.isDimming()) {
                anim[1] = 0;
                anim[0] = 0;
            } else {
                this.mPolicy.selectRotationAnimationLw(anim);
            }
            startFreezingDisplayLocked(inTransaction, anim[0], anim[1]);
            screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(0);
            updateDisplayAndOrientationLocked(this.mCurConfiguration.uiMode);
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            if (!inTransaction) {
                SurfaceControl.openTransaction();
            }
            if (screenRotationAnimation != null) {
                try {
                    if (screenRotationAnimation.hasScreenshot() && screenRotationAnimation.setRotationInTransaction(rotation, this.mFxSession, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY, getTransitionAnimationScaleLocked(), displayInfo.logicalWidth, displayInfo.logicalHeight)) {
                        scheduleAnimationLocked();
                    }
                } catch (RuntimeException e) {
                    Slog.wtf(TAG, "Display exception in Window Manager", e);
                    if (!inTransaction) {
                        SurfaceControl.closeTransaction();
                    }
                } catch (Throwable th) {
                    if (!inTransaction) {
                        SurfaceControl.closeTransaction();
                    }
                }
            }
            this.mDisplayManagerInternal.performTraversalInTransactionFromWindowManager();
            if (!inTransaction) {
                SurfaceControl.closeTransaction();
            }
            WindowList windows = displayContent.getWindowList();
            for (i = windows.size() - 1; i >= 0; i--) {
                WindowState w = (WindowState) windows.get(i);
                if (w.mAppToken != null) {
                    w.mAppToken.destroySavedSurfaces();
                }
                if (w.mHasSurface) {
                    w.mOrientationChanging = true;
                    this.mWindowPlacerLocked.mOrientationChangeComplete = false;
                }
                w.mLastFreezeDuration = 0;
            }
            for (i = this.mRotationWatchers.size() - 1; i >= 0; i--) {
                try {
                    ((RotationWatcher) this.mRotationWatchers.get(i)).watcher.onRotationChanged(rotation);
                } catch (RemoteException e2) {
                }
            }
            if (screenRotationAnimation == null && this.mAccessibilityController != null && displayContent.getDisplayId() == 0) {
                this.mAccessibilityController.onRotationChangedLocked(getDefaultDisplayContentLocked(), rotation);
            }
            return true;
        }

        public int getRotation() {
            return this.mRotation;
        }

        public boolean isRotationFrozen() {
            return this.mPolicy.getUserRotationMode() == 1;
        }

        public int watchRotation(IRotationWatcher watcher) {
            int i;
            final IBinder watcherBinder = watcher.asBinder();
            DeathRecipient dr = new DeathRecipient() {
                public void binderDied() {
                    synchronized (WindowManagerService.this.mWindowMap) {
                        int i = 0;
                        while (i < WindowManagerService.this.mRotationWatchers.size()) {
                            if (watcherBinder == ((RotationWatcher) WindowManagerService.this.mRotationWatchers.get(i)).watcher.asBinder()) {
                                IBinder binder = ((RotationWatcher) WindowManagerService.this.mRotationWatchers.remove(i)).watcher.asBinder();
                                if (binder != null) {
                                    binder.unlinkToDeath(this, 0);
                                }
                                i--;
                            }
                            i++;
                        }
                    }
                }
            };
            synchronized (this.mWindowMap) {
                try {
                    watcher.asBinder().linkToDeath(dr, 0);
                    this.mRotationWatchers.add(new RotationWatcher(watcher, dr));
                } catch (RemoteException e) {
                }
                i = this.mRotation;
            }
            return i;
        }

        public void removeRotationWatcher(IRotationWatcher watcher) {
            IBinder watcherBinder = watcher.asBinder();
            synchronized (this.mWindowMap) {
                int i = 0;
                while (i < this.mRotationWatchers.size()) {
                    if (watcherBinder == ((RotationWatcher) this.mRotationWatchers.get(i)).watcher.asBinder()) {
                        RotationWatcher removed = (RotationWatcher) this.mRotationWatchers.remove(i);
                        IBinder binder = removed.watcher.asBinder();
                        if (binder != null) {
                            binder.unlinkToDeath(removed.deathRecipient, 0);
                        }
                        i--;
                    }
                    i++;
                }
            }
        }

        public int getPreferredOptionsPanelGravity() {
            synchronized (this.mWindowMap) {
                int rotation = getRotation();
                DisplayContent displayContent = getDefaultDisplayContentLocked();
                if (displayContent.mInitialDisplayWidth < displayContent.mInitialDisplayHeight) {
                    switch (rotation) {
                        case 1:
                            return 85;
                        case 2:
                            return 81;
                        case 3:
                            return 8388691;
                        default:
                            return 81;
                    }
                }
                switch (rotation) {
                    case 1:
                        return 81;
                    case 2:
                        return 8388691;
                    case 3:
                        return 81;
                    default:
                        return 85;
                }
            }
        }

        public boolean startViewServer(int port) {
            if (isSystemSecure() || !checkCallingPermission("android.permission.DUMP", "startViewServer") || port < 1024) {
                return false;
            }
            if (this.mViewServer != null) {
                if (!this.mViewServer.isRunning()) {
                    try {
                        return this.mViewServer.start();
                    } catch (IOException e) {
                        Slog.w("WindowManager", "View server did not start");
                    }
                }
                return false;
            }
            try {
                this.mViewServer = new ViewServer(this, port);
                return this.mViewServer.start();
            } catch (IOException e2) {
                Slog.w("WindowManager", "View server did not start");
                return false;
            }
        }

        private boolean isSystemSecure() {
            if ("1".equals(SystemProperties.get(SYSTEM_SECURE, "1"))) {
                return "0".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0"));
            }
            return false;
        }

        public boolean stopViewServer() {
            if (isSystemSecure() || !checkCallingPermission("android.permission.DUMP", "stopViewServer") || this.mViewServer == null) {
                return false;
            }
            return this.mViewServer.stop();
        }

        public boolean isViewServerRunning() {
            boolean z = false;
            if (isSystemSecure() || !checkCallingPermission("android.permission.DUMP", "isViewServerRunning")) {
                return false;
            }
            if (this.mViewServer != null) {
                z = this.mViewServer.isRunning();
            }
            return z;
        }

        boolean viewServerListWindows(Socket client) {
            Throwable th;
            if (isSystemSecure()) {
                return false;
            }
            boolean result = true;
            WindowList windows = new WindowList();
            synchronized (this.mWindowMap) {
                int numDisplays = this.mDisplayContents.size();
                for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                    windows.addAll(((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList());
                }
            }
            BufferedWriter bufferedWriter = null;
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()), DumpState.DUMP_PREFERRED_XML);
                try {
                    int count = windows.size();
                    for (int i = 0; i < count; i++) {
                        WindowState w = (WindowState) windows.get(i);
                        out.write(Integer.toHexString(System.identityHashCode(w)));
                        out.write(32);
                        out.append(w.mAttrs.getTitle());
                        out.write(10);
                    }
                    out.write("DONE.\n");
                    out.flush();
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            result = false;
                        }
                    }
                    bufferedWriter = out;
                } catch (Exception e2) {
                    bufferedWriter = out;
                    result = false;
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e3) {
                            result = false;
                        }
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedWriter = out;
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e4) {
                        }
                    }
                    throw th;
                }
            } catch (Exception e5) {
                result = false;
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                return result;
            } catch (Throwable th3) {
                th = th3;
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                throw th;
            }
            return result;
        }

        boolean viewServerGetFocusedWindow(Socket client) {
            Throwable th;
            if (isSystemSecure()) {
                return false;
            }
            boolean result = true;
            WindowState focusedWindow = getFocusedWindow();
            BufferedWriter bufferedWriter = null;
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()), DumpState.DUMP_PREFERRED_XML);
                if (focusedWindow != null) {
                    try {
                        out.write(Integer.toHexString(System.identityHashCode(focusedWindow)));
                        out.write(32);
                        out.append(focusedWindow.mAttrs.getTitle());
                    } catch (Exception e) {
                        bufferedWriter = out;
                        result = false;
                        if (bufferedWriter != null) {
                            try {
                                bufferedWriter.close();
                            } catch (IOException e2) {
                                result = false;
                            }
                        }
                        return result;
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedWriter = out;
                        if (bufferedWriter != null) {
                            try {
                                bufferedWriter.close();
                            } catch (IOException e3) {
                            }
                        }
                        throw th;
                    }
                }
                out.write(10);
                out.flush();
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e4) {
                        result = false;
                    }
                }
                bufferedWriter = out;
            } catch (Exception e5) {
                result = false;
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                return result;
            } catch (Throwable th3) {
                th = th3;
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                throw th;
            }
            return result;
        }

        boolean viewServerWindowCommand(Socket client, String command, String parameters) {
            Exception e;
            Throwable th;
            if (isSystemSecure()) {
                return false;
            }
            boolean success = true;
            Parcel parcel = null;
            Parcel parcel2 = null;
            BufferedWriter bufferedWriter = null;
            try {
                int index = parameters.indexOf(32);
                if (index == -1) {
                    index = parameters.length();
                }
                int hashCode = (int) Long.parseLong(parameters.substring(0, index), 16);
                if (index < parameters.length()) {
                    parameters = parameters.substring(index + 1);
                } else {
                    parameters = "";
                }
                WindowState window = findWindow(hashCode);
                if (window == null) {
                    return false;
                }
                parcel = Parcel.obtain();
                parcel.writeInterfaceToken("android.view.IWindow");
                parcel.writeString(command);
                parcel.writeString(parameters);
                parcel.writeInt(1);
                ParcelFileDescriptor.fromSocket(client).writeToParcel(parcel, 0);
                parcel2 = Parcel.obtain();
                window.mClient.asBinder().transact(1, parcel, parcel2, 0);
                parcel2.readException();
                if (!client.isOutputShutdown()) {
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                    try {
                        out.write("DONE\n");
                        out.flush();
                        bufferedWriter = out;
                    } catch (Exception e2) {
                        e = e2;
                        bufferedWriter = out;
                        try {
                            Slog.w("WindowManager", "Could not send command " + command + " with parameters " + parameters, e);
                            success = false;
                            if (parcel != null) {
                                parcel.recycle();
                            }
                            if (parcel2 != null) {
                                parcel2.recycle();
                            }
                            if (bufferedWriter != null) {
                                try {
                                    bufferedWriter.close();
                                } catch (IOException e3) {
                                }
                            }
                            return success;
                        } catch (Throwable th2) {
                            th = th2;
                            if (parcel != null) {
                                parcel.recycle();
                            }
                            if (parcel2 != null) {
                                parcel2.recycle();
                            }
                            if (bufferedWriter != null) {
                                try {
                                    bufferedWriter.close();
                                } catch (IOException e4) {
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        bufferedWriter = out;
                        if (parcel != null) {
                            parcel.recycle();
                        }
                        if (parcel2 != null) {
                            parcel2.recycle();
                        }
                        if (bufferedWriter != null) {
                            bufferedWriter.close();
                        }
                        throw th;
                    }
                }
                if (parcel != null) {
                    parcel.recycle();
                }
                if (parcel2 != null) {
                    parcel2.recycle();
                }
                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.close();
                    } catch (IOException e5) {
                    }
                }
                return success;
            } catch (Exception e6) {
                e = e6;
                Slog.w("WindowManager", "Could not send command " + command + " with parameters " + parameters, e);
                success = false;
                if (parcel != null) {
                    parcel.recycle();
                }
                if (parcel2 != null) {
                    parcel2.recycle();
                }
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                return success;
            }
        }

        public void addWindowChangeListener(WindowChangeListener listener) {
            synchronized (this.mWindowMap) {
                this.mWindowChangeListeners.add(listener);
            }
        }

        public void removeWindowChangeListener(WindowChangeListener listener) {
            synchronized (this.mWindowMap) {
                this.mWindowChangeListeners.remove(listener);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void notifyWindowsChanged() {
            synchronized (this.mWindowMap) {
                if (this.mWindowChangeListeners.isEmpty()) {
                } else {
                    WindowChangeListener[] windowChangeListeners = (WindowChangeListener[]) this.mWindowChangeListeners.toArray(new WindowChangeListener[this.mWindowChangeListeners.size()]);
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void notifyFocusChanged() {
            synchronized (this.mWindowMap) {
                if (this.mWindowChangeListeners.isEmpty()) {
                } else {
                    WindowChangeListener[] windowChangeListeners = (WindowChangeListener[]) this.mWindowChangeListeners.toArray(new WindowChangeListener[this.mWindowChangeListeners.size()]);
                }
            }
        }

        private WindowState findWindow(int hashCode) {
            if (hashCode == -1) {
                return getFocusedWindow();
            }
            synchronized (this.mWindowMap) {
                int numDisplays = this.mDisplayContents.size();
                for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                    WindowList windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                    int numWindows = windows.size();
                    for (int winNdx = 0; winNdx < numWindows; winNdx++) {
                        WindowState w = (WindowState) windows.get(winNdx);
                        if (System.identityHashCode(w) == hashCode) {
                            return w;
                        }
                    }
                }
                return null;
            }
        }

        void sendNewConfiguration() {
            try {
                this.mActivityManager.updateConfiguration(null);
            } catch (RemoteException e) {
            }
        }

        public Configuration computeNewConfiguration() {
            Configuration computeNewConfigurationLocked;
            synchronized (this.mWindowMap) {
                computeNewConfigurationLocked = computeNewConfigurationLocked();
            }
            return computeNewConfigurationLocked;
        }

        Configuration computeNewConfigurationLocked() {
            if (!this.mDisplayReady) {
                return null;
            }
            Configuration config = new Configuration();
            config.fontScale = 0.0f;
            computeScreenConfigurationLocked(config);
            return config;
        }

        private void adjustDisplaySizeRanges(DisplayInfo displayInfo, int rotation, int uiMode, int dw, int dh) {
            int width = this.mPolicy.getConfigDisplayWidth(dw, dh, rotation, uiMode);
            if (width < displayInfo.smallestNominalAppWidth) {
                displayInfo.smallestNominalAppWidth = width;
            }
            if (width > displayInfo.largestNominalAppWidth) {
                displayInfo.largestNominalAppWidth = width;
            }
            int height = this.mPolicy.getConfigDisplayHeight(dw, dh, rotation, uiMode);
            if (height < displayInfo.smallestNominalAppHeight) {
                displayInfo.smallestNominalAppHeight = height;
            }
            if (height > displayInfo.largestNominalAppHeight) {
                displayInfo.largestNominalAppHeight = height;
            }
        }

        private int reduceConfigLayout(int curLayout, int rotation, float density, int dw, int dh, int uiMode) {
            int w = this.mPolicy.getNonDecorDisplayWidth(dw, dh, rotation, uiMode);
            int h = this.mPolicy.getNonDecorDisplayHeight(dw, dh, rotation, uiMode);
            int longSize = w;
            int shortSize = h;
            if (w < h) {
                int tmp = w;
                longSize = h;
                shortSize = w;
            }
            return Configuration.reduceScreenLayout(curLayout, (int) (((float) longSize) / density), (int) (((float) shortSize) / density));
        }

        private void computeSizeRangesAndScreenLayout(DisplayInfo displayInfo, boolean rotated, int uiMode, int dw, int dh, float density, Configuration outConfig) {
            int unrotDw;
            int unrotDh;
            if (rotated) {
                unrotDw = dh;
                unrotDh = dw;
            } else {
                unrotDw = dw;
                unrotDh = dh;
            }
            displayInfo.smallestNominalAppWidth = 1073741824;
            displayInfo.smallestNominalAppHeight = 1073741824;
            displayInfo.largestNominalAppWidth = 0;
            displayInfo.largestNominalAppHeight = 0;
            adjustDisplaySizeRanges(displayInfo, 0, uiMode, unrotDw, unrotDh);
            adjustDisplaySizeRanges(displayInfo, 1, uiMode, unrotDh, unrotDw);
            adjustDisplaySizeRanges(displayInfo, 2, uiMode, unrotDw, unrotDh);
            adjustDisplaySizeRanges(displayInfo, 3, uiMode, unrotDh, unrotDw);
            int sl = reduceConfigLayout(reduceConfigLayout(reduceConfigLayout(reduceConfigLayout(Configuration.resetScreenLayout(outConfig.screenLayout), 0, density, unrotDw, unrotDh, uiMode), 1, density, unrotDh, unrotDw, uiMode), 2, density, unrotDw, unrotDh, uiMode), 3, density, unrotDh, unrotDw, uiMode);
            outConfig.smallestScreenWidthDp = (int) (((float) displayInfo.smallestNominalAppWidth) / density);
            outConfig.screenLayout = sl;
        }

        private int reduceCompatConfigWidthSize(int curSize, int rotation, int uiMode, DisplayMetrics dm, int dw, int dh) {
            dm.noncompatWidthPixels = this.mPolicy.getNonDecorDisplayWidth(dw, dh, rotation, uiMode);
            dm.noncompatHeightPixels = this.mPolicy.getNonDecorDisplayHeight(dw, dh, rotation, uiMode);
            int size = (int) (((((float) dm.noncompatWidthPixels) / CompatibilityInfo.computeCompatibleScaling(dm, null)) / dm.density) + TaskPositioner.RESIZING_HINT_ALPHA);
            if (curSize == 0 || size < curSize) {
                return size;
            }
            return curSize;
        }

        private int computeCompatSmallestWidth(boolean rotated, int uiMode, DisplayMetrics dm, int dw, int dh) {
            int unrotDw;
            int unrotDh;
            this.mTmpDisplayMetrics.setTo(dm);
            DisplayMetrics tmpDm = this.mTmpDisplayMetrics;
            if (rotated) {
                unrotDw = dh;
                unrotDh = dw;
            } else {
                unrotDw = dw;
                unrotDh = dh;
            }
            return reduceCompatConfigWidthSize(reduceCompatConfigWidthSize(reduceCompatConfigWidthSize(reduceCompatConfigWidthSize(0, 0, uiMode, tmpDm, unrotDw, unrotDh), 1, uiMode, tmpDm, unrotDh, unrotDw), 2, uiMode, tmpDm, unrotDw, unrotDh), 3, uiMode, tmpDm, unrotDh, unrotDw);
        }

        DisplayInfo updateDisplayAndOrientationLocked(int uiMode) {
            DisplayContent displayContent = getDefaultDisplayContentLocked();
            boolean rotated = this.mRotation != 1 ? this.mRotation == 3 : true;
            int realdw = rotated ? displayContent.mBaseDisplayHeight : displayContent.mBaseDisplayWidth;
            int realdh = rotated ? displayContent.mBaseDisplayWidth : displayContent.mBaseDisplayHeight;
            int dw = realdw;
            int dh = realdh;
            if (this.mAltOrientation) {
                if (realdw > realdh) {
                    int maxw = (int) (((float) realdh) / 1.3f);
                    if (maxw < realdw) {
                        dw = maxw;
                    }
                } else {
                    int maxh = (int) (((float) realdw) / 1.3f);
                    if (maxh < realdh) {
                        dh = maxh;
                    }
                }
            }
            int appWidth = this.mPolicy.getNonDecorDisplayWidth(dw, dh, this.mRotation, uiMode);
            int appHeight = this.mPolicy.getNonDecorDisplayHeight(dw, dh, this.mRotation, uiMode);
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            displayInfo.rotation = this.mRotation;
            displayInfo.logicalWidth = dw;
            displayInfo.logicalHeight = dh;
            displayInfo.logicalDensityDpi = displayContent.mBaseDisplayDensity;
            displayInfo.appWidth = appWidth;
            displayInfo.appHeight = appHeight;
            displayInfo.getLogicalMetrics(this.mRealDisplayMetrics, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO, null);
            displayInfo.getAppMetrics(this.mDisplayMetrics);
            if (displayContent.mDisplayScalingDisabled) {
                displayInfo.flags |= 1073741824;
            } else {
                displayInfo.flags &= -1073741825;
            }
            this.mDisplayManagerInternal.setDisplayInfoOverrideFromWindowManager(displayContent.getDisplayId(), displayInfo);
            displayContent.mBaseDisplayRect.set(0, 0, dw, dh);
            this.mForceCompatibleScreenScale = CompatibilityInfo.computeForceCompatibleScaling(this.mDisplayMetrics, this.mCompatDisplayMetrics);
            this.mCompatibleScreenScale = CompatibilityInfo.computeCompatibleScaling(this.mDisplayMetrics, this.mCompatDisplayMetrics);
            return displayInfo;
        }

        void computeScreenConfigurationLocked(Configuration config) {
            int i;
            DisplayInfo displayInfo = updateDisplayAndOrientationLocked(config.uiMode);
            int dw = displayInfo.logicalWidth;
            int dh = displayInfo.logicalHeight;
            if (dw <= dh) {
                i = 1;
            } else {
                i = 2;
            }
            config.orientation = i;
            config.screenWidthDp = (int) (((float) this.mPolicy.getConfigDisplayWidth(dw, dh, this.mRotation, config.uiMode)) / this.mDisplayMetrics.density);
            config.screenHeightDp = (int) (((float) this.mPolicy.getConfigDisplayHeight(dw, dh, this.mRotation, config.uiMode)) / this.mDisplayMetrics.density);
            boolean rotated = this.mRotation != 1 ? this.mRotation == 3 : true;
            computeSizeRangesAndScreenLayout(displayInfo, rotated, config.uiMode, dw, dh, this.mDisplayMetrics.density, config);
            int i2 = config.screenLayout & -769;
            if ((displayInfo.flags & 16) != 0) {
                i = 512;
            } else {
                i = 256;
            }
            config.screenLayout = i | i2;
            config.compatScreenWidthDp = (int) (((float) config.screenWidthDp) / this.mCompatibleScreenScale);
            config.compatScreenHeightDp = (int) (((float) config.screenHeightDp) / this.mCompatibleScreenScale);
            config.compatSmallestScreenWidthDp = computeCompatSmallestWidth(rotated, config.uiMode, this.mDisplayMetrics, dw, dh);
            config.densityDpi = displayInfo.logicalDensityDpi;
            config.touchscreen = 1;
            config.keyboard = 1;
            config.navigation = 1;
            int keyboardPresence = 0;
            int navigationPresence = 0;
            for (InputDevice device : this.mInputManager.getInputDevices()) {
                if (!device.isVirtual()) {
                    int presenceFlag;
                    int sources = device.getSources();
                    if (device.isExternal()) {
                        presenceFlag = 2;
                    } else {
                        presenceFlag = 1;
                    }
                    if (!this.mIsTouchDevice) {
                        config.touchscreen = 1;
                    } else if ((sources & 4098) == 4098) {
                        config.touchscreen = 3;
                    }
                    if ((65540 & sources) == 65540) {
                        config.navigation = 3;
                        navigationPresence |= presenceFlag;
                    } else if ((sources & 513) == 513 && config.navigation == 1) {
                        config.navigation = 2;
                        navigationPresence |= presenceFlag;
                    }
                    if (device.getKeyboardType() == 2) {
                        config.keyboard = 2;
                        keyboardPresence |= presenceFlag;
                    }
                }
            }
            if (config.navigation == 1 && this.mHasPermanentDpad) {
                config.navigation = 2;
                navigationPresence |= 1;
            }
            boolean hardKeyboardAvailable = config.keyboard != 1;
            if (hardKeyboardAvailable != this.mHardKeyboardAvailable) {
                this.mHardKeyboardAvailable = hardKeyboardAvailable;
                this.mH.removeMessages(22);
                this.mH.sendEmptyMessage(22);
            }
            config.keyboardHidden = 1;
            config.hardKeyboardHidden = 1;
            config.navigationHidden = 1;
            this.mPolicy.adjustConfigurationLw(config, keyboardPresence, navigationPresence);
        }

        void notifyHardKeyboardStatusChange() {
            synchronized (this.mWindowMap) {
                OnHardKeyboardStatusChangeListener listener = this.mHardKeyboardStatusChangeListener;
                boolean available = this.mHardKeyboardAvailable;
            }
            if (listener != null) {
                listener.onHardKeyboardStatusChange(available);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        boolean startMovingTask(IWindow window, float startX, float startY) {
            synchronized (this.mWindowMap) {
                WindowState win = windowForClientLocked(null, window, false);
                if (!startPositioningLocked(win, false, startX, startY)) {
                    return false;
                }
            }
            return true;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void startScrollingTask(DisplayContent displayContent, int startX, int startY) {
            Task task = null;
            synchronized (this.mWindowMap) {
                int taskId = displayContent.taskIdFromPoint(startX, startY);
                if (taskId >= 0) {
                    task = (Task) this.mTaskIdToTask.get(taskId);
                }
                if (!(task != null && task.isDockedInEffect() && startPositioningLocked(task.getTopVisibleAppMainWindow(), false, (float) startX, (float) startY))) {
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void handleTapOutsideTask(DisplayContent displayContent, int x, int y) {
            synchronized (this.mWindowMap) {
                Task task = displayContent.findTaskForControlPoint(x, y);
                int taskId;
                if (task == null) {
                    taskId = displayContent.taskIdFromPoint(x, y);
                } else if (startPositioningLocked(task.getTopVisibleAppMainWindow(), true, (float) x, (float) y)) {
                    taskId = task.mTaskId;
                }
            }
        }

        private boolean startPositioningLocked(WindowState win, boolean resize, float startX, float startY) {
            if (win == null || win.getAppToken() == null) {
                Slog.w("WindowManager", "startPositioningLocked: Bad window " + win);
                return false;
            } else if (win.mInputChannel == null) {
                Slog.wtf("WindowManager", "startPositioningLocked: " + win + " has no input channel, " + " probably being removed");
                return false;
            } else {
                DisplayContent displayContent = win.getDisplayContent();
                if (displayContent == null) {
                    Slog.w("WindowManager", "startPositioningLocked: Invalid display content " + win);
                    return false;
                }
                Display display = displayContent.getDisplay();
                this.mTaskPositioner = new TaskPositioner(this);
                this.mTaskPositioner.register(display);
                this.mInputMonitor.updateInputWindowsLw(true);
                WindowState transferFocusFromWin = win;
                if (!(this.mCurrentFocus == null || this.mCurrentFocus == win || this.mCurrentFocus.mAppToken != win.mAppToken)) {
                    transferFocusFromWin = this.mCurrentFocus;
                }
                if (this.mInputManager.transferTouchFocus(transferFocusFromWin.mInputChannel, this.mTaskPositioner.mServerChannel)) {
                    this.mTaskPositioner.startDragLocked(win, resize, startX, startY);
                    return true;
                }
                Slog.e("WindowManager", "startPositioningLocked: Unable to transfer touch focus");
                this.mTaskPositioner.unregister();
                this.mTaskPositioner = null;
                this.mInputMonitor.updateInputWindowsLw(true);
                return false;
            }
        }

        private void finishPositioning() {
            synchronized (this.mWindowMap) {
                if (this.mTaskPositioner != null) {
                    this.mTaskPositioner.unregister();
                    this.mTaskPositioner = null;
                    this.mInputMonitor.updateInputWindowsLw(true);
                }
            }
        }

        void adjustForImeIfNeeded(DisplayContent displayContent) {
            WindowState imeWin = this.mInputMethodWindow;
            boolean imeVisible = (imeWin != null && imeWin.isVisibleLw() && imeWin.isDisplayedLw()) ? !displayContent.mDividerControllerLocked.isImeHideRequested() : false;
            boolean dockVisible = isStackVisibleLocked(3);
            TaskStack imeTargetStack = getImeFocusStackLocked();
            int imeDockSide = (!dockVisible || imeTargetStack == null) ? -1 : imeTargetStack.getDockSide();
            boolean imeOnTop = imeDockSide == 2;
            boolean imeOnBottom = imeDockSide == 4;
            boolean dockMinimized = displayContent.mDividerControllerLocked.isMinimizedDock();
            int imeHeight = this.mPolicy.getInputMethodWindowVisibleHeightLw();
            boolean imeHeightChanged = imeVisible ? imeHeight != displayContent.mDividerControllerLocked.getImeHeightAdjustedFor() : false;
            ArrayList<TaskStack> stacks;
            int i;
            if (imeVisible && dockVisible && ((imeOnTop || imeOnBottom) && !dockMinimized)) {
                stacks = displayContent.getStacks();
                for (i = stacks.size() - 1; i >= 0; i--) {
                    TaskStack stack = (TaskStack) stacks.get(i);
                    boolean isDockedOnBottom = stack.getDockSide() == 4;
                    if (stack.isVisibleLocked() && (imeOnBottom || isDockedOnBottom)) {
                        stack.setAdjustedForIme(imeWin, imeOnBottom ? imeHeightChanged : false);
                    } else {
                        stack.resetAdjustedForIme(false);
                    }
                }
                displayContent.mDividerControllerLocked.setAdjustedForIme(imeOnBottom, true, true, imeWin, imeHeight);
                return;
            }
            stacks = displayContent.getStacks();
            for (i = stacks.size() - 1; i >= 0; i--) {
                ((TaskStack) stacks.get(i)).resetAdjustedForIme(!dockVisible);
            }
            displayContent.mDividerControllerLocked.setAdjustedForIme(false, false, dockVisible, imeWin, imeHeight);
        }

        IBinder prepareDragSurface(IWindow window, SurfaceSession session, int flags, int width, int height, Surface outSurface) {
            IBinder token;
            OutOfResourcesException e;
            Throwable th;
            if (getLazyMode() != 0) {
                width = (int) (((float) width) * 0.75f);
                height = (int) (((float) height) * 0.75f);
            }
            int callerPid = Binder.getCallingPid();
            int callerUid = Binder.getCallingUid();
            long origId = Binder.clearCallingIdentity();
            IBinder token2 = null;
            try {
                synchronized (this.mWindowMap) {
                    try {
                        if (this.mDragState == null) {
                            Display display = getDefaultDisplayContentLocked().getDisplay();
                            SurfaceControl surface = new SurfaceControl(session, "drag surface", width, height, -3, 4);
                            surface.setLayerStack(display.getLayerStack());
                            float alpha = 1.0f;
                            if ((flags & 512) == 0) {
                                alpha = DRAG_SHADOW_ALPHA_TRANSPARENT;
                            }
                            surface.setAlpha(alpha);
                            outSurface.copyFrom(surface);
                            IBinder winBinder = window.asBinder();
                            token = new Binder();
                            try {
                                this.mDragState = new DragState(this, token, surface, flags, winBinder);
                                this.mDragState.mPid = callerPid;
                                this.mDragState.mUid = callerUid;
                                this.mDragState.mOriginalAlpha = alpha;
                                token2 = new Binder();
                                this.mDragState.mToken = token2;
                                this.mH.removeMessages(20, winBinder);
                                this.mH.sendMessageDelayed(this.mH.obtainMessage(20, winBinder), 5000);
                                token = token2;
                            } catch (OutOfResourcesException e2) {
                                e = e2;
                                try {
                                    Slog.e("WindowManager", "Can't allocate drag surface w=" + width + " h=" + height, e);
                                    if (this.mDragState != null) {
                                        this.mDragState.reset();
                                        this.mDragState = null;
                                    }
                                    Binder.restoreCallingIdentity(origId);
                                    return token;
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            }
                            try {
                                Binder.restoreCallingIdentity(origId);
                                return token;
                            } catch (Throwable th3) {
                                th = th3;
                                Binder.restoreCallingIdentity(origId);
                                throw th;
                            }
                        }
                        Slog.w("WindowManager", "Drag already in progress");
                        token = null;
                        Binder.restoreCallingIdentity(origId);
                        return token;
                    } catch (OutOfResourcesException e3) {
                        e = e3;
                        token = token2;
                        Slog.e("WindowManager", "Can't allocate drag surface w=" + width + " h=" + height, e);
                        if (this.mDragState != null) {
                            this.mDragState.reset();
                            this.mDragState = null;
                        }
                        Binder.restoreCallingIdentity(origId);
                        return token;
                    } catch (Throwable th4) {
                        th = th4;
                        token = token2;
                        throw th;
                    }
                }
            } catch (Throwable th5) {
                th = th5;
                token = null;
                Binder.restoreCallingIdentity(origId);
                throw th;
            }
        }

        public void pauseKeyDispatching(IBinder _token) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "pauseKeyDispatching()")) {
                synchronized (this.mWindowMap) {
                    WindowToken token = (WindowToken) this.mTokenMap.get(_token);
                    if (token != null) {
                        this.mInputMonitor.pauseDispatchingLw(token);
                    }
                }
                return;
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public void resumeKeyDispatching(IBinder _token) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "resumeKeyDispatching()")) {
                synchronized (this.mWindowMap) {
                    WindowToken token = (WindowToken) this.mTokenMap.get(_token);
                    if (token != null) {
                        this.mInputMonitor.resumeDispatchingLw(token);
                    }
                }
                return;
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        public void setEventDispatching(boolean enabled) {
            if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setEventDispatching()")) {
                synchronized (this.mWindowMap) {
                    this.mEventDispatchingEnabled = enabled;
                    if (this.mDisplayEnabled) {
                        this.mInputMonitor.setEventDispatchingLw(enabled);
                    }
                }
                return;
            }
            throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
        }

        private WindowState getFocusedWindow() {
            WindowState focusedWindowLocked;
            synchronized (this.mWindowMap) {
                focusedWindowLocked = getFocusedWindowLocked();
            }
            return focusedWindowLocked;
        }

        private WindowState getFocusedWindowLocked() {
            return this.mCurrentFocus;
        }

        TaskStack getImeFocusStackLocked() {
            if (this.mFocusedApp == null || this.mFocusedApp.mTask == null) {
                return null;
            }
            return this.mFocusedApp.mTask.mStack;
        }

        private void showAuditSafeModeNotification() {
            PendingIntent pendingIntent = PendingIntent.getActivity(this.mContext, 0, new Intent("android.intent.action.VIEW", Uri.parse("https://support.google.com/nexus/answer/2852139")), 0);
            String title = this.mContext.getString(17040859);
            ((NotificationManager) this.mContext.getSystemService("notification")).notifyAsUser(null, 17040859, new Builder(this.mContext).setSmallIcon(17301642).setWhen(0).setOngoing(true).setTicker(title).setLocalOnly(true).setPriority(1).setVisibility(1).setColor(this.mContext.getColor(17170519)).setContentTitle(title).setContentText(this.mContext.getString(17040860)).setContentIntent(pendingIntent).build(), UserHandle.ALL);
        }

        public boolean detectSafeMode() {
            if (!this.mInputMonitor.waitForInputDevicesReady(1000)) {
                Slog.w("WindowManager", "Devices still not ready after waiting 1000 milliseconds before attempting to detect safe mode.");
            }
            if (Global.getInt(this.mContext.getContentResolver(), "safe_boot_disallowed", 0) != 0) {
                return false;
            }
            int menuState = this.mInputManager.getKeyCodeState(-1, -256, 82);
            int sState = this.mInputManager.getKeyCodeState(-1, -256, 47);
            int dpadState = this.mInputManager.getKeyCodeState(-1, 513, 23);
            int trackballState = this.mInputManager.getScanCodeState(-1, 65540, InputManagerService.BTN_MOUSE);
            boolean z = (menuState > 0 || sState > 0 || dpadState > 0 || trackballState > 0) ? true : this.mInputManager.getKeyCodeState(-1, -256, 25) > 0;
            this.mSafeMode = z;
            try {
                if (!(SystemProperties.getInt(ShutdownThread.REBOOT_SAFEMODE_PROPERTY, 0) == 0 && SystemProperties.getInt(ShutdownThread.RO_SAFEMODE_PROPERTY, 0) == 0)) {
                    int auditSafeMode = SystemProperties.getInt(ShutdownThread.AUDIT_SAFEMODE_PROPERTY, 0);
                    if (auditSafeMode == 0) {
                        this.mSafeMode = true;
                        SystemProperties.set(ShutdownThread.REBOOT_SAFEMODE_PROPERTY, "");
                    } else if (auditSafeMode >= SystemProperties.getInt(PROPERTY_BUILD_DATE_UTC, 0)) {
                        this.mSafeMode = true;
                        showAuditSafeModeNotification();
                    } else {
                        SystemProperties.set(ShutdownThread.REBOOT_SAFEMODE_PROPERTY, "");
                        SystemProperties.set(ShutdownThread.AUDIT_SAFEMODE_PROPERTY, "");
                    }
                }
            } catch (IllegalArgumentException e) {
            }
            if ("factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
                this.mSafeMode = false;
            }
            if (this.mSafeMode) {
                Log.i("WindowManager", "SAFE MODE ENABLED (menu=" + menuState + " s=" + sState + " dpad=" + dpadState + " trackball=" + trackballState + ")");
                SystemProperties.set(ShutdownThread.RO_SAFEMODE_PROPERTY, "1");
            } else {
                Log.i("WindowManager", "SAFE MODE not enabled");
            }
            this.mPolicy.setSafeMode(this.mSafeMode);
            return this.mSafeMode;
        }

        public void displayReady() {
            for (Display display : this.mDisplays) {
                displayReady(display.getDisplayId());
            }
            synchronized (this.mWindowMap) {
                readForcedDisplayPropertiesLocked(getDefaultDisplayContentLocked());
                this.mDisplayReady = true;
            }
            try {
                this.mActivityManager.updateConfiguration(null);
            } catch (RemoteException e) {
            }
            synchronized (this.mWindowMap) {
                this.mIsTouchDevice = this.mContext.getPackageManager().hasSystemFeature("android.hardware.touchscreen");
                configureDisplayPolicyLocked(getDefaultDisplayContentLocked());
            }
            try {
                this.mActivityManager.updateConfiguration(null);
            } catch (RemoteException e2) {
            }
            updateCircularDisplayMaskIfNeeded();
        }

        private void displayReady(int displayId) {
            synchronized (this.mWindowMap) {
                DisplayContent displayContent = getDisplayContentLocked(displayId);
                if (displayContent != null) {
                    this.mAnimator.addDisplayLocked(displayId);
                    displayContent.initializeDisplayBaseInfo();
                    if (displayContent.mTapDetector != null) {
                        displayContent.mTapDetector.init();
                    }
                }
            }
        }

        public void systemReady() {
            this.mPolicy.systemReady();
        }

        void destroyPreservedSurfaceLocked() {
            for (int i = this.mDestroyPreservedSurface.size() - 1; i >= 0; i--) {
                ((WindowState) this.mDestroyPreservedSurface.get(i)).mWinAnimator.destroyPreservedSurfaceLocked();
            }
            this.mDestroyPreservedSurface.clear();
        }

        void stopUsingSavedSurfaceLocked() {
            for (int i = this.mFinishedEarlyAnim.size() - 1; i >= 0; i--) {
                ((AppWindowToken) this.mFinishedEarlyAnim.get(i)).stopUsingSavedSurfaceLocked();
            }
            this.mFinishedEarlyAnim.clear();
        }

        public IWindowSession openSession(IWindowSessionCallback callback, IInputMethodClient client, IInputContext inputContext) {
            if (client == null) {
                throw new IllegalArgumentException("null client");
            } else if (inputContext != null) {
                return new Session(this, callback, client, inputContext);
            } else {
                throw new IllegalArgumentException("null inputContext");
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean inputMethodClientHasFocus(IInputMethodClient client) {
            synchronized (this.mWindowMap) {
                int idx = findDesiredInputMethodWindowIndexLocked(false);
                if (idx > 0) {
                    WindowState imFocus = (WindowState) getDefaultWindowListLocked().get(idx - 1);
                    if (imFocus != null) {
                        if (imFocus.mAttrs.type == 3 && imFocus.mAppToken != null) {
                            for (int i = 0; i < imFocus.mAppToken.windows.size(); i++) {
                                WindowState w = (WindowState) imFocus.mAppToken.windows.get(i);
                                if (w != imFocus) {
                                    Log.i("WindowManager", "Switching to real app window: " + w);
                                    imFocus = w;
                                    break;
                                }
                            }
                        }
                        if (imFocus.mSession.mClient != null && imFocus.mSession.mClient.asBinder() == client.asBinder()) {
                            return true;
                        }
                    }
                }
                if (this.mCurrentFocus == null || this.mCurrentFocus.mSession.mClient == null || this.mCurrentFocus.mSession.mClient.asBinder() != client.asBinder()) {
                } else {
                    return true;
                }
            }
        }

        public void getInitialDisplaySize(int displayId, Point size) {
            synchronized (this.mWindowMap) {
                DisplayContent displayContent = getDisplayContentLocked(displayId);
                if (displayContent != null && displayContent.hasAccess(Binder.getCallingUid())) {
                    size.x = displayContent.mInitialDisplayWidth;
                    size.y = displayContent.mInitialDisplayHeight;
                }
            }
        }

        public void getBaseDisplaySize(int displayId, Point size) {
            synchronized (this.mWindowMap) {
                DisplayContent displayContent = getDisplayContentLocked(displayId);
                if (displayContent != null && displayContent.hasAccess(Binder.getCallingUid())) {
                    size.x = displayContent.mBaseDisplayWidth;
                    size.y = displayContent.mBaseDisplayHeight;
                }
            }
        }

        public void setForcedDisplaySize(int displayId, int width, int height) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
                throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
            } else if (displayId != 0) {
                throw new IllegalArgumentException("Can only set the default display");
            } else {
                long ident = Binder.clearCallingIdentity();
                try {
                    synchronized (this.mWindowMap) {
                        DisplayContent displayContent = getDisplayContentLocked(displayId);
                        if (displayContent != null) {
                            width = Math.min(Math.max(width, BOOT_ANIMATION_POLL_INTERVAL), displayContent.mInitialDisplayWidth * 2);
                            height = Math.min(Math.max(height, BOOT_ANIMATION_POLL_INTERVAL), displayContent.mInitialDisplayHeight * 2);
                            updateResourceConfiguration(displayId, displayContent.mBaseDisplayDensity, width, height);
                            setForcedDisplaySizeLocked(displayContent, width, height);
                            Global.putString(this.mContext.getContentResolver(), "display_size_forced", width + "," + height);
                        }
                    }
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public void setForcedDisplayScalingMode(int displayId, int mode) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
                throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
            } else if (displayId != 0) {
                throw new IllegalArgumentException("Can only set the default display");
            } else {
                long ident = Binder.clearCallingIdentity();
                try {
                    synchronized (this.mWindowMap) {
                        DisplayContent displayContent = getDisplayContentLocked(displayId);
                        if (displayContent != null) {
                            if (mode < 0 || mode > 1) {
                                mode = 0;
                            }
                            setForcedDisplayScalingModeLocked(displayContent, mode);
                            Global.putInt(this.mContext.getContentResolver(), "display_scaling_force", mode);
                        }
                    }
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        private void setForcedDisplayScalingModeLocked(DisplayContent displayContent, int mode) {
            boolean z;
            Slog.i("WindowManager", "Using display scaling mode: " + (mode == 0 ? "auto" : "off"));
            if (mode != 0) {
                z = true;
            } else {
                z = false;
            }
            displayContent.mDisplayScalingDisabled = z;
            reconfigureDisplayLocked(displayContent);
        }

        private void readForcedDisplayPropertiesLocked(DisplayContent displayContent) {
            String sizeStr = Global.getString(this.mContext.getContentResolver(), "display_size_forced");
            if (sizeStr == null || sizeStr.length() == 0) {
                sizeStr = SystemProperties.get(SIZE_OVERRIDE, null);
            }
            if (sizeStr != null && sizeStr.length() > 0) {
                int pos = sizeStr.indexOf(44);
                if (pos > 0 && sizeStr.lastIndexOf(44) == pos) {
                    try {
                        int width = Integer.parseInt(sizeStr.substring(0, pos));
                        int height = Integer.parseInt(sizeStr.substring(pos + 1));
                        if (!(displayContent.mBaseDisplayWidth == width && displayContent.mBaseDisplayHeight == height)) {
                            Slog.i("WindowManager", "FORCED DISPLAY SIZE: " + width + "x" + height);
                            displayContent.mBaseDisplayWidth = width;
                            displayContent.mBaseDisplayHeight = height;
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
            int density = getForcedDisplayDensityForUserLocked(this.mCurrentUserId);
            if (density != 0) {
                displayContent.mBaseDisplayDensity = density;
            }
            if (Global.getInt(this.mContext.getContentResolver(), "display_scaling_force", 0) != 0) {
                Slog.i("WindowManager", "FORCED DISPLAY SCALING DISABLED");
                displayContent.mDisplayScalingDisabled = true;
            }
        }

        private void setForcedDisplaySizeLocked(DisplayContent displayContent, int width, int height) {
            Slog.i("WindowManager", "Using new display size: " + width + "x" + height);
            displayContent.mBaseDisplayWidth = width;
            displayContent.mBaseDisplayHeight = height;
            reconfigureDisplayLocked(displayContent);
        }

        public void clearForcedDisplaySize(int displayId) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
                throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
            } else if (displayId != 0) {
                throw new IllegalArgumentException("Can only set the default display");
            } else {
                long ident = Binder.clearCallingIdentity();
                try {
                    synchronized (this.mWindowMap) {
                        DisplayContent displayContent = getDisplayContentLocked(displayId);
                        if (displayContent != null) {
                            setForcedDisplaySizeLocked(displayContent, displayContent.mInitialDisplayWidth, displayContent.mInitialDisplayHeight);
                            Global.putString(this.mContext.getContentResolver(), "display_size_forced", "");
                        }
                    }
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int getInitialDisplayDensity(int displayId) {
            synchronized (this.mWindowMap) {
                DisplayContent displayContent = getDisplayContentLocked(displayId);
                if (displayContent == null || !displayContent.hasAccess(Binder.getCallingUid())) {
                } else {
                    int i = displayContent.mInitialDisplayDensity;
                    return i;
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int getBaseDisplayDensity(int displayId) {
            synchronized (this.mWindowMap) {
                DisplayContent displayContent = getDisplayContentLocked(displayId);
                if (displayContent == null || !displayContent.hasAccess(Binder.getCallingUid())) {
                } else {
                    int i = displayContent.mBaseDisplayDensity;
                    return i;
                }
            }
        }

        public void setForcedDisplayDensity(int displayId, int density) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
                throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
            } else if (displayId != 0) {
                throw new IllegalArgumentException("Can only set the default display");
            } else {
                long ident = Binder.clearCallingIdentity();
                try {
                    synchronized (this.mWindowMap) {
                        DisplayContent displayContent = getDisplayContentLocked(displayId);
                        if (displayContent != null) {
                            updateResourceConfiguration(displayId, density, displayContent.mBaseDisplayWidth, displayContent.mBaseDisplayHeight);
                            setForcedDisplayDensityLocked(displayContent, density);
                            Secure.putStringForUser(this.mContext.getContentResolver(), "display_density_forced", Integer.toString(density), this.mCurrentUserId);
                        }
                    }
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public void clearForcedDisplayDensity(int displayId) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
                throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
            } else if (displayId != 0) {
                throw new IllegalArgumentException("Can only set the default display");
            } else {
                long ident = Binder.clearCallingIdentity();
                try {
                    synchronized (this.mWindowMap) {
                        DisplayContent displayContent = getDisplayContentLocked(displayId);
                        if (displayContent != null) {
                            setForcedDisplayDensityLocked(displayContent, displayContent.mInitialDisplayDensity);
                            Secure.putStringForUser(this.mContext.getContentResolver(), "display_density_forced", "", this.mCurrentUserId);
                        }
                    }
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        private int getForcedDisplayDensityForUserLocked(int userId) {
            String densityStr = Secure.getStringForUser(this.mContext.getContentResolver(), "display_density_forced", userId);
            if (densityStr == null || densityStr.length() == 0) {
                densityStr = SystemProperties.get(DENSITY_OVERRIDE, null);
            }
            if (densityStr != null && densityStr.length() > 0) {
                try {
                    return Integer.parseInt(densityStr);
                } catch (NumberFormatException e) {
                }
            }
            return 0;
        }

        private void setForcedDisplayDensityLocked(DisplayContent displayContent, int density) {
            displayContent.mBaseDisplayDensity = density;
            reconfigureDisplayLocked(displayContent);
        }

        protected void reconfigureDisplayLocked(DisplayContent displayContent) {
            if (this.mDisplayReady) {
                configureDisplayPolicyLocked(displayContent);
                displayContent.layoutNeeded = true;
                boolean configChanged = updateOrientationFromAppTokensLocked(false);
                this.mTempConfiguration.setToDefaults();
                this.mTempConfiguration.updateFrom(this.mCurConfiguration);
                computeScreenConfigurationLocked(this.mTempConfiguration);
                if (configChanged | (this.mCurConfiguration.diff(this.mTempConfiguration) != 0 ? 1 : 0)) {
                    this.mWaitingForConfig = true;
                    startFreezingDisplayLocked(false, 0, 0);
                    this.mH.sendEmptyMessage(18);
                    if (!this.mReconfigureOnConfigurationChanged.contains(displayContent)) {
                        this.mReconfigureOnConfigurationChanged.add(displayContent);
                    }
                }
                this.mWindowPlacerLocked.performSurfacePlacement();
            }
        }

        private void configureDisplayPolicyLocked(DisplayContent displayContent) {
            this.mPolicy.setInitialDisplaySize(displayContent.getDisplay(), displayContent.mBaseDisplayWidth, displayContent.mBaseDisplayHeight, displayContent.mBaseDisplayDensity);
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            this.mPolicy.setDisplayOverscan(displayContent.getDisplay(), displayInfo.overscanLeft, displayInfo.overscanTop, displayInfo.overscanRight, displayInfo.overscanBottom);
        }

        public void setOverscan(int displayId, int left, int top, int right, int bottom) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
                throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
            }
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    DisplayContent displayContent = getDisplayContentLocked(displayId);
                    if (displayContent != null) {
                        setOverscanLocked(displayContent, left, top, right, bottom);
                    }
                }
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }

        private void setOverscanLocked(DisplayContent displayContent, int left, int top, int right, int bottom) {
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            displayInfo.overscanLeft = left;
            displayInfo.overscanTop = top;
            displayInfo.overscanRight = right;
            displayInfo.overscanBottom = bottom;
            this.mDisplaySettings.setOverscanLocked(displayInfo.uniqueId, displayInfo.name, left, top, right, bottom);
            this.mDisplaySettings.writeSettingsLocked();
            reconfigureDisplayLocked(displayContent);
        }

        final WindowState windowForClientLocked(Session session, IWindow client, boolean throwOnError) {
            return windowForClientLocked(session, client.asBinder(), throwOnError);
        }

        final WindowState windowForClientLocked(Session session, IBinder client, boolean throwOnError) {
            WindowState win = (WindowState) this.mWindowMap.get(client);
            RuntimeException ex;
            if (win == null) {
                ex = new IllegalArgumentException("Requested window " + client + " does not exist");
                if (throwOnError) {
                    throw ex;
                }
                Slog.w("WindowManager", "Failed looking up window", ex);
                return null;
            } else if (session == null || win.mSession == session) {
                return win;
            } else {
                ex = new IllegalArgumentException("Requested window " + client + " is in session " + win.mSession + ", not " + session);
                if (throwOnError) {
                    throw ex;
                }
                Slog.w("WindowManager", "Failed looking up window", ex);
                return null;
            }
        }

        final void rebuildAppWindowListLocked() {
            rebuildAppWindowListLocked(getDefaultDisplayContentLocked());
        }

        private void rebuildAppWindowListLocked(DisplayContent displayContent) {
            int stackNdx;
            WindowList windows = displayContent.getWindowList();
            int NW = windows.size();
            int lastBelow = -1;
            int numRemoved = 0;
            if (this.mRebuildTmp.length < NW) {
                this.mRebuildTmp = new WindowState[(NW + 10)];
            }
            int i = 0;
            while (i < NW) {
                WindowState w = (WindowState) windows.get(i);
                if (w.mAppToken != null) {
                    WindowState win = (WindowState) windows.remove(i);
                    win.mRebuilding = true;
                    this.mRebuildTmp[numRemoved] = win;
                    this.mWindowsChanged = true;
                    NW--;
                    numRemoved++;
                } else {
                    if (lastBelow == i - 1 && w.mAttrs.type == 2013) {
                        lastBelow = i;
                    }
                    i++;
                }
            }
            lastBelow++;
            i = lastBelow;
            ArrayList<TaskStack> stacks = displayContent.getStacks();
            int numStacks = stacks.size();
            for (stackNdx = 0; stackNdx < numStacks; stackNdx++) {
                AppTokenList exitingAppTokens = ((TaskStack) stacks.get(stackNdx)).mExitingAppTokens;
                int NT = exitingAppTokens.size();
                for (int j = 0; j < NT; j++) {
                    i = reAddAppWindowsLocked(displayContent, i, (WindowToken) exitingAppTokens.get(j), false);
                }
            }
            for (stackNdx = 0; stackNdx < numStacks; stackNdx++) {
                ArrayList<Task> tasks = ((TaskStack) stacks.get(stackNdx)).getTasks();
                int numTasks = tasks.size();
                for (int taskNdx = 0; taskNdx < numTasks; taskNdx++) {
                    AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
                    int numTokens = tokens.size();
                    for (int tokenNdx = 0; tokenNdx < numTokens; tokenNdx++) {
                        WindowToken wtoken = (AppWindowToken) tokens.get(tokenNdx);
                        if (!wtoken.mIsExiting || wtoken.waitingForReplacement()) {
                            i = reAddAppWindowsLocked(displayContent, i, wtoken, true);
                        }
                    }
                }
            }
            i -= lastBelow;
            if (i != numRemoved) {
                displayContent.layoutNeeded = true;
                Slog.w("WindowManager", "On display=" + displayContent.getDisplayId() + " Rebuild removed " + numRemoved + " windows but added " + i + " rebuildAppWindowListLocked() " + " callers=" + Debug.getCallers(10));
                for (i = 0; i < numRemoved; i++) {
                    WindowState ws = this.mRebuildTmp[i];
                    if (ws.mRebuilding) {
                        Writer sw = new StringWriter();
                        PrintWriter pw = new FastPrintWriter(sw, false, 1024);
                        ws.dump(pw, "", true);
                        pw.flush();
                        Slog.w("WindowManager", "This window was lost: " + ws);
                        Slog.w("WindowManager", sw.toString());
                        ws.mWinAnimator.destroySurfaceLocked();
                    }
                }
                Slog.w("WindowManager", "Current app token list:");
                dumpAppTokensLocked();
                Slog.w("WindowManager", "Final window list:");
                dumpWindowsLocked();
            }
            Arrays.fill(this.mRebuildTmp, null);
        }

        void makeWindowFreezingScreenIfNeededLocked(WindowState w) {
            if (!okToDisplay() && this.mWindowsFreezingScreen != 2) {
                w.mOrientationChanging = true;
                w.mLastFreezeDuration = 0;
                this.mWindowPlacerLocked.mOrientationChangeComplete = false;
                if (this.mWindowsFreezingScreen == 0) {
                    this.mWindowsFreezingScreen = 1;
                    this.mH.removeMessages(11);
                    this.mH.sendEmptyMessageDelayed(11, 2000);
                }
            }
        }

        int handleAnimatingStoppedAndTransitionLocked() {
            this.mAppTransition.setIdle();
            for (int i = this.mNoAnimationNotifyOnTransitionFinished.size() - 1; i >= 0; i--) {
                this.mAppTransition.notifyAppTransitionFinishedLocked((IBinder) this.mNoAnimationNotifyOnTransitionFinished.get(i));
            }
            this.mNoAnimationNotifyOnTransitionFinished.clear();
            this.mWallpaperControllerLocked.hideDeferredWallpapersIfNeeded();
            ArrayList<TaskStack> stacks = getDefaultDisplayContentLocked().getStacks();
            for (int stackNdx = stacks.size() - 1; stackNdx >= 0; stackNdx--) {
                ArrayList<Task> tasks = ((TaskStack) stacks.get(stackNdx)).getTasks();
                for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                    AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
                    for (int tokenNdx = tokens.size() - 1; tokenNdx >= 0; tokenNdx--) {
                        ((AppWindowToken) tokens.get(tokenNdx)).sendingToBottom = false;
                    }
                }
            }
            rebuildAppWindowListLocked();
            moveInputMethodWindowsIfNeededLocked(true);
            this.mWindowPlacerLocked.mWallpaperMayChange = true;
            this.mFocusMayChange = true;
            return 1;
        }

        void updateResizingWindows(WindowState w) {
            WindowStateAnimator winAnimator = w.mWinAnimator;
            if (w.mHasSurface && w.mLayoutSeq == this.mLayoutSeq && !w.isGoneForLayoutLw()) {
                Task task = w.getTask();
                if (task == null || !task.mStack.getBoundsAnimating()) {
                    w.setInsetsChanged();
                    boolean configChanged = w.isConfigChanged();
                    boolean dragResizingChanged = w.isDragResizeChanged() ? !w.isDragResizingChangeReported() : false;
                    w.mLastFrame.set(w.mFrame);
                    if (w.mContentInsetsChanged || w.mVisibleInsetsChanged || winAnimator.mSurfaceResized || w.mOutsetsChanged || configChanged || dragResizingChanged || !w.isResizedWhileNotDragResizingReported()) {
                        if (w.mAppToken == null || !w.mAppDied) {
                            w.mLastOverscanInsets.set(w.mOverscanInsets);
                            w.mLastContentInsets.set(w.mContentInsets);
                            w.mLastVisibleInsets.set(w.mVisibleInsets);
                            w.mLastStableInsets.set(w.mStableInsets);
                            w.mLastOutsets.set(w.mOutsets);
                            makeWindowFreezingScreenIfNeededLocked(w);
                            if (w.mOrientationChanging || dragResizingChanged || w.isResizedWhileNotDragResizing()) {
                                Flog.i(307, "Orientation or resize start waiting for draw, mDrawState=DRAW_PENDING in " + w + ", surfaceController " + winAnimator.mSurfaceController);
                                winAnimator.mDrawState = 1;
                                if (w.mAppToken != null) {
                                    w.mAppToken.clearAllDrawn();
                                }
                            }
                            if (!this.mResizingWindows.contains(w)) {
                                this.mResizingWindows.add(w);
                            }
                        } else {
                            w.mAppToken.removeAllDeadWindows();
                        }
                    } else if (w.mOrientationChanging && w.isDrawnLw()) {
                        w.mOrientationChanging = false;
                        w.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mDisplayFreezeTime);
                    }
                }
            }
        }

        void checkDrawnWindowsLocked() {
            if (!this.mWaitingForDrawn.isEmpty() && this.mWaitingForDrawnCallback != null) {
                for (int j = this.mWaitingForDrawn.size() - 1; j >= 0; j--) {
                    WindowState win = (WindowState) this.mWaitingForDrawn.get(j);
                    if (win.mRemoved || !win.mHasSurface || !win.mPolicyVisibility) {
                        this.mWaitingForDrawn.remove(win);
                    } else if (win.hasDrawnLw()) {
                        this.mWaitingForDrawn.remove(win);
                    }
                }
                if (this.mWaitingForDrawn.isEmpty()) {
                    this.mH.removeMessages(24);
                    this.mH.sendEmptyMessage(33);
                }
            }
        }

        void setHoldScreenLocked(Session newHoldScreen) {
            boolean hold = newHoldScreen != null;
            if (hold && this.mHoldingScreenOn != newHoldScreen) {
                this.mHoldingScreenWakeLock.setWorkSource(new WorkSource(newHoldScreen.mUid));
            }
            this.mHoldingScreenOn = newHoldScreen;
            if (hold == this.mHoldingScreenWakeLock.isHeld()) {
                return;
            }
            if (hold) {
                this.mLastWakeLockHoldingWindow = this.mWindowPlacerLocked.mHoldScreenWindow;
                this.mLastWakeLockObscuringWindow = null;
                this.mHoldingScreenWakeLock.acquire();
                this.mPolicy.keepScreenOnStartedLw();
                return;
            }
            this.mLastWakeLockHoldingWindow = null;
            this.mLastWakeLockObscuringWindow = this.mWindowPlacerLocked.mObsuringWindow;
            this.mPolicy.keepScreenOnStoppedLw();
            this.mHoldingScreenWakeLock.release();
        }

        void requestTraversal() {
            synchronized (this.mWindowMap) {
                this.mWindowPlacerLocked.requestTraversal();
            }
        }

        void scheduleAnimationLocked() {
            if (!this.mAnimationScheduled) {
                this.mAnimationScheduled = true;
                this.mChoreographer.postFrameCallback(this.mAnimator.mAnimationFrameCallback);
            }
        }

        boolean needsLayout() {
            int numDisplays = this.mDisplayContents.size();
            for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                if (((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).layoutNeeded) {
                    return true;
                }
            }
            return false;
        }

        int adjustAnimationBackground(WindowStateAnimator winAnimator) {
            WindowList windows = winAnimator.mWin.getWindowList();
            for (int i = windows.size() - 1; i >= 0; i--) {
                WindowState testWin = (WindowState) windows.get(i);
                if (testWin.mIsWallpaper && testWin.isVisibleNow()) {
                    return testWin.mWinAnimator.mAnimLayer;
                }
            }
            return winAnimator.mAnimLayer;
        }

        boolean reclaimSomeSurfaceMemoryLocked(WindowStateAnimator winAnimator, String operation, boolean secure) {
            int displayNdx;
            WindowSurfaceController surfaceController = winAnimator.mSurfaceController;
            boolean leakedSurface = false;
            boolean killedApps = false;
            EventLog.writeEvent(EventLogTags.WM_NO_SURFACE_MEMORY, new Object[]{winAnimator.mWin.toString(), Integer.valueOf(winAnimator.mSession.mPid), operation});
            long callingIdentity = Binder.clearCallingIdentity();
            Slog.i("WindowManager", "Out of memory for surface!  Looking for leaks...");
            int numDisplays = this.mDisplayContents.size();
            for (displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                int winNdx;
                WindowList windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                int numWindows = windows.size();
                for (winNdx = 0; winNdx < numWindows; winNdx++) {
                    WindowState ws = (WindowState) windows.get(winNdx);
                    WindowStateAnimator wsa = ws.mWinAnimator;
                    if (wsa.mSurfaceController != null) {
                        if (!this.mSessions.contains(wsa.mSession)) {
                            Slog.w("WindowManager", "LEAKED SURFACE (session doesn't exist): " + ws + " surface=" + wsa.mSurfaceController + " token=" + ws.mToken + " pid=" + ws.mSession.mPid + " uid=" + ws.mSession.mUid);
                            wsa.destroySurface();
                            this.mForceRemoves.add(ws);
                            leakedSurface = true;
                        } else if (ws.mAppToken != null && ws.mAppToken.clientHidden) {
                            Slog.w("WindowManager", "LEAKED SURFACE (app token hidden): " + ws + " surface=" + wsa.mSurfaceController + " token=" + ws.mAppToken + " saved=" + ws.hasSavedSurface());
                            wsa.destroySurface();
                            leakedSurface = true;
                        }
                    }
                }
            }
            if (!leakedSurface) {
                Slog.w("WindowManager", "No leaked surfaces; killing applicatons!");
                SparseIntArray pidCandidates = new SparseIntArray();
                displayNdx = 0;
                while (displayNdx < numDisplays) {
                    windows = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                    numWindows = windows.size();
                    for (winNdx = 0; winNdx < numWindows; winNdx++) {
                        ws = (WindowState) windows.get(winNdx);
                        if (!this.mForceRemoves.contains(ws)) {
                            wsa = ws.mWinAnimator;
                            if (wsa.mSurfaceController != null) {
                                pidCandidates.append(wsa.mSession.mPid, wsa.mSession.mPid);
                            }
                        }
                    }
                    try {
                        if (pidCandidates.size() > 0) {
                            int[] pids = new int[pidCandidates.size()];
                            for (int i = 0; i < pids.length; i++) {
                                pids[i] = pidCandidates.keyAt(i);
                            }
                            try {
                                if (this.mActivityManager.killPids(pids, "Free memory", secure)) {
                                    killedApps = true;
                                }
                            } catch (RemoteException e) {
                            }
                        }
                        displayNdx++;
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(callingIdentity);
                    }
                }
            }
            if (leakedSurface || killedApps) {
                Slog.w("WindowManager", "Looks like we have reclaimed some memory, clearing surface for retry.");
                if (surfaceController != null) {
                    winAnimator.destroySurface();
                    scheduleRemoveStartingWindowLocked(winAnimator.mWin.mAppToken);
                }
                try {
                    winAnimator.mWin.mClient.dispatchGetNewSurface();
                } catch (RemoteException e2) {
                }
            }
            Binder.restoreCallingIdentity(callingIdentity);
            return !leakedSurface ? killedApps : true;
        }

        boolean updateFocusedWindowLocked(int mode, boolean updateInputWindows) {
            WindowState newFocus = computeFocusedWindowLocked();
            if (this.mCurrentFocus == newFocus) {
                return false;
            }
            Trace.traceBegin(32, "wmUpdateFocus");
            this.mH.removeMessages(2);
            this.mH.sendEmptyMessage(2);
            DisplayContent displayContent = getDefaultDisplayContentLocked();
            boolean z = mode != 1 ? mode != 3 : false;
            boolean imWindowChanged = moveInputMethodWindowsIfNeededLocked(z);
            if (imWindowChanged) {
                displayContent.layoutNeeded = true;
                newFocus = computeFocusedWindowLocked();
            }
            WindowState oldFocus = this.mCurrentFocus;
            this.mCurrentFocus = newFocus;
            this.mInputManager.setCurFocusWindow(this.mCurrentFocus);
            this.mLosingFocus.remove(newFocus);
            int focusChanged = this.mPolicy.focusChangedLw(oldFocus, newFocus);
            if (imWindowChanged && oldFocus != this.mInputMethodWindow) {
                Slog.i("WindowManager", "Focus of the input method window changed. Perform layout begin");
                if (mode == 2) {
                    this.mWindowPlacerLocked.performLayoutLockedInner(displayContent, true, updateInputWindows);
                    focusChanged &= -2;
                } else if (mode == 3) {
                    this.mLayersController.assignLayersLocked(displayContent.getWindowList());
                }
                Slog.i("WindowManager", "Focus of the input method window changed. Perform layout end");
            }
            if ((focusChanged & 1) != 0) {
                Slog.i("WindowManager", "The change in focus caused us to need to do a layout begin");
                displayContent.layoutNeeded = true;
                if (mode == 2) {
                    this.mWindowPlacerLocked.performLayoutLockedInner(displayContent, true, updateInputWindows);
                }
                Slog.i("WindowManager", "The change in focus caused us to need to do a layout end");
            }
            if (mode != 1) {
                this.mInputMonitor.setInputFocusLw(this.mCurrentFocus, updateInputWindows);
            }
            String NUM_REGEX = "[0-9]++";
            Pattern pattern = Pattern.compile("[0-9]++");
            Matcher matcherForOldFocusWin = pattern.matcher(oldFocus != null ? oldFocus.getAttrs().getTitle() : "");
            Matcher matcherForNewFocusWin = pattern.matcher(newFocus != null ? newFocus.getAttrs().getTitle() : "");
            if (!(matcherForOldFocusWin.find() || matcherForNewFocusWin.find())) {
                Flog.i(304, "oldFocusWindow: " + oldFocus + ", currentFocusWindow: " + this.mCurrentFocus + ", currentFocusApp: " + this.mFocusedApp);
            }
            adjustForImeIfNeeded(displayContent);
            Trace.traceEnd(32);
            return true;
        }

        private WindowState computeFocusedWindowLocked() {
            int displayCount = this.mDisplayContents.size();
            for (int i = 0; i < displayCount; i++) {
                WindowState win = findFocusedWindowLocked((DisplayContent) this.mDisplayContents.valueAt(i));
                if (win != null) {
                    return win;
                }
            }
            return null;
        }

        WindowState findFocusedWindowLocked(DisplayContent displayContent) {
            WindowList windows = displayContent.getWindowList();
            for (int i = windows.size() - 1; i >= 0; i--) {
                WindowState win = (WindowState) windows.get(i);
                if (win.canReceiveKeys()) {
                    AppWindowToken wtoken = win.mAppToken;
                    if (wtoken == null || !(wtoken.removed || wtoken.sendingToBottom)) {
                        if (wtoken != null && win.mAttrs.type != 3 && this.mFocusedApp != null) {
                            ArrayList<Task> tasks = displayContent.getTasks();
                            for (int taskNdx = tasks.size() - 1; taskNdx >= 0; taskNdx--) {
                                AppTokenList tokens = ((Task) tasks.get(taskNdx)).mAppTokens;
                                int tokenNdx = tokens.size() - 1;
                                while (tokenNdx >= 0) {
                                    AppWindowToken token = (AppWindowToken) tokens.get(tokenNdx);
                                    if (wtoken == token) {
                                        break;
                                    } else if (this.mFocusedApp == token && token.windowsAreFocusable()) {
                                        return null;
                                    } else {
                                        tokenNdx--;
                                    }
                                }
                                if (tokenNdx >= 0) {
                                    break;
                                }
                            }
                        }
                        return win;
                    }
                }
            }
            return null;
        }

        private void startFreezingDisplayLocked(boolean inTransaction, int exitAnim, int enterAnim) {
            if (!this.mDisplayFrozen && this.mDisplayReady && this.mPolicy.isScreenOn()) {
                this.mScreenFrozenLock.acquire();
                this.mDisplayFrozen = true;
                this.mDisplayFreezeTime = SystemClock.elapsedRealtime();
                this.mLastFinishedFreezeSource = null;
                this.mInputMonitor.freezeInputDispatchingLw();
                this.mPolicy.setLastInputMethodWindowLw(null, null);
                if (this.mAppTransition.isTransitionSet()) {
                    this.mAppTransition.freeze();
                }
                this.mExitAnimId = exitAnim;
                this.mEnterAnimId = enterAnim;
                DisplayContent displayContent = getDefaultDisplayContentLocked();
                int displayId = displayContent.getDisplayId();
                ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(displayId);
                if (screenRotationAnimation != null) {
                    screenRotationAnimation.kill();
                }
                boolean isSecure = false;
                WindowList windows = getDefaultWindowListLocked();
                int N = windows.size();
                for (int i = 0; i < N; i++) {
                    WindowState ws = (WindowState) windows.get(i);
                    if (ws.isOnScreen() && (ws.mAttrs.flags & DumpState.DUMP_PREFERRED_XML) != 0) {
                        isSecure = true;
                        break;
                    }
                }
                displayContent.updateDisplayInfo();
                this.mAnimator.setScreenRotationAnimationLocked(displayId, new ScreenRotationAnimation(this.mContext, displayContent, this.mFxSession, inTransaction, this.mPolicy.isDefaultOrientationForced(), isSecure));
            }
        }

        void stopFreezingDisplayLocked() {
            if (this.mDisplayFrozen) {
                boolean z;
                if (this.mWaitingForConfig || this.mAppsFreezingScreen > 0 || this.mWindowsFreezingScreen == 1) {
                    z = true;
                } else {
                    z = this.mClientFreezingScreen;
                }
                int size = this.mOpeningApps.size();
                if (z || size > 0) {
                    Slog.d("WindowManager", "stopFreezingDisplayLocked: Returning mWaitingForConfig=" + this.mWaitingForConfig + ", mAppsFreezingScreen=" + this.mAppsFreezingScreen + ", mWindowsFreezingScreen=" + this.mWindowsFreezingScreen + ", mClientFreezingScreen=" + this.mClientFreezingScreen + ", mOpeningApps.size()=" + this.mOpeningApps.size());
                    if (!z && size > 0) {
                        printFreezingDisplayLogs();
                    }
                    return;
                }
                this.mDisplayFrozen = false;
                this.mLastDisplayFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mDisplayFreezeTime);
                StringBuilder stringBuilder = new StringBuilder(128);
                stringBuilder.append("Screen frozen for ");
                TimeUtils.formatDuration((long) this.mLastDisplayFreezeDuration, stringBuilder);
                if (this.mLastFinishedFreezeSource != null) {
                    stringBuilder.append(" due to ");
                    stringBuilder.append(this.mLastFinishedFreezeSource);
                }
                Slog.i("WindowManager", stringBuilder.toString());
                this.mH.removeMessages(17);
                this.mH.removeMessages(30);
                boolean updateRotation = false;
                DisplayContent displayContent = getDefaultDisplayContentLocked();
                int displayId = displayContent.getDisplayId();
                ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(displayId);
                if (screenRotationAnimation == null || !screenRotationAnimation.hasScreenshot()) {
                    if (screenRotationAnimation != null) {
                        screenRotationAnimation.kill();
                        this.mAnimator.setScreenRotationAnimationLocked(displayId, null);
                    }
                    updateRotation = true;
                } else {
                    DisplayInfo displayInfo = displayContent.getDisplayInfo();
                    if (!this.mPolicy.validateRotationAnimationLw(this.mExitAnimId, this.mEnterAnimId, displayContent.isDimming())) {
                        this.mEnterAnimId = 0;
                        this.mExitAnimId = 0;
                    }
                    if (screenRotationAnimation.dismiss(this.mFxSession, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY, getTransitionAnimationScaleLocked(), displayInfo.logicalWidth, displayInfo.logicalHeight, this.mExitAnimId, this.mEnterAnimId)) {
                        scheduleAnimationLocked();
                    } else {
                        screenRotationAnimation.kill();
                        this.mAnimator.setScreenRotationAnimationLocked(displayId, null);
                        updateRotation = true;
                    }
                }
                this.mInputMonitor.thawInputDispatchingLw();
                boolean configChanged = updateOrientationFromAppTokensLocked(false);
                this.mH.removeMessages(15);
                this.mH.sendEmptyMessageDelayed(15, 2000);
                this.mScreenFrozenLock.release();
                if (updateRotation) {
                    configChanged |= updateRotationUncheckedLocked(false);
                }
                if (configChanged) {
                    this.mH.sendEmptyMessage(18);
                }
            }
        }

        static int getPropertyInt(String[] tokens, int index, int defUnits, int defDps, DisplayMetrics dm) {
            if (index < tokens.length) {
                String str = tokens[index];
                if (str != null && str.length() > 0) {
                    try {
                        return Integer.parseInt(str);
                    } catch (Exception e) {
                    }
                }
            }
            if (defUnits == 0) {
                return defDps;
            }
            return (int) TypedValue.applyDimension(defUnits, (float) defDps, dm);
        }

        void createWatermarkInTransaction() {
            Throwable th;
            if (this.mWatermark == null) {
                FileInputStream fileInputStream = null;
                DataInputStream dataInputStream = null;
                try {
                    FileInputStream in = new FileInputStream(new File("/system/etc/setup.conf"));
                    try {
                        DataInputStream ind = new DataInputStream(in);
                        try {
                            String line = ind.readLine();
                            if (line != null) {
                                String[] toks = line.split("%");
                                if (toks != null && toks.length > 0) {
                                    this.mWatermark = new Watermark(getDefaultDisplayContentLocked().getDisplay(), this.mRealDisplayMetrics, this.mFxSession, toks);
                                }
                            }
                            if (ind != null) {
                                try {
                                    ind.close();
                                } catch (IOException e) {
                                }
                            } else if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e2) {
                                }
                            }
                            fileInputStream = in;
                        } catch (FileNotFoundException e3) {
                            dataInputStream = ind;
                            fileInputStream = in;
                            if (dataInputStream == null) {
                                try {
                                    dataInputStream.close();
                                } catch (IOException e4) {
                                }
                            } else if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e5) {
                                }
                            }
                        } catch (IOException e6) {
                            dataInputStream = ind;
                            fileInputStream = in;
                            if (dataInputStream == null) {
                                try {
                                    dataInputStream.close();
                                } catch (IOException e7) {
                                }
                            } else if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e8) {
                                }
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            dataInputStream = ind;
                            fileInputStream = in;
                            if (dataInputStream == null) {
                                try {
                                    dataInputStream.close();
                                } catch (IOException e9) {
                                }
                            } else if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e10) {
                                }
                            }
                            throw th;
                        }
                    } catch (FileNotFoundException e11) {
                        fileInputStream = in;
                        if (dataInputStream == null) {
                            dataInputStream.close();
                        } else if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                    } catch (IOException e12) {
                        fileInputStream = in;
                        if (dataInputStream == null) {
                            dataInputStream.close();
                        } else if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fileInputStream = in;
                        if (dataInputStream == null) {
                            dataInputStream.close();
                        } else if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e13) {
                    if (dataInputStream == null) {
                        dataInputStream.close();
                    } else if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                } catch (IOException e14) {
                    if (dataInputStream == null) {
                        dataInputStream.close();
                    } else if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                } catch (Throwable th4) {
                    th = th4;
                    if (dataInputStream == null) {
                        dataInputStream.close();
                    } else if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            }
        }

        public void statusBarVisibilityChanged(int visibility) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.STATUS_BAR") != 0) {
                throw new SecurityException("Caller does not hold permission android.permission.STATUS_BAR");
            }
            synchronized (this.mWindowMap) {
                int diff = visibility ^ this.mLastStatusBarVisibility;
                if (diff != 0) {
                    Flog.i(303, "statusBarVisibilityChanged,vis=" + Integer.toHexString(visibility) + ",diff=" + Integer.toHexString(diff));
                }
                this.mLastStatusBarVisibility = visibility;
                visibility = this.mPolicy.adjustSystemUiVisibilityLw(visibility);
                if ((diff & 201326592) == 201326592) {
                    DisplayContent displayContent = getDefaultDisplayContentLocked();
                    displayContent.pendingLayoutChanges |= 1;
                    this.mWindowPlacerLocked.performSurfacePlacementInner(false);
                }
                updateStatusBarVisibilityLocked(visibility);
            }
        }

        public void reevaluateStatusBarVisibility() {
            synchronized (this.mWindowMap) {
                if (updateStatusBarVisibilityLocked(this.mPolicy.adjustSystemUiVisibilityLw(this.mLastStatusBarVisibility))) {
                    this.mWindowPlacerLocked.requestTraversal();
                }
            }
        }

        public InputConsumer addInputConsumer(Looper looper, Factory inputEventReceiverFactory) {
            HideNavInputConsumer inputConsumerImpl;
            synchronized (this.mWindowMap) {
                inputConsumerImpl = new HideNavInputConsumer(this, looper, inputEventReceiverFactory);
                this.mInputConsumer = inputConsumerImpl;
                this.mInputMonitor.updateInputWindowsLw(true);
            }
            return inputConsumerImpl;
        }

        boolean removeInputConsumer() {
            synchronized (this.mWindowMap) {
                if (this.mInputConsumer != null) {
                    this.mInputConsumer = null;
                    this.mInputMonitor.updateInputWindowsLw(true);
                    return true;
                }
                return false;
            }
        }

        public void createWallpaperInputConsumer(InputChannel inputChannel) {
            synchronized (this.mWindowMap) {
                this.mWallpaperInputConsumer = new InputConsumerImpl(this, "wallpaper input", inputChannel);
                this.mWallpaperInputConsumer.mWindowHandle.hasWallpaper = true;
                this.mInputMonitor.updateInputWindowsLw(true);
            }
        }

        public void removeWallpaperInputConsumer() {
            synchronized (this.mWindowMap) {
                if (this.mWallpaperInputConsumer != null) {
                    this.mWallpaperInputConsumer.disposeChannelsLw();
                    this.mWallpaperInputConsumer = null;
                    this.mInputMonitor.updateInputWindowsLw(true);
                }
            }
        }

        public boolean hasNavigationBar() {
            return this.mPolicy.hasNavigationBar();
        }

        public void lockNow(Bundle options) {
            this.mPolicy.lockNow(options);
        }

        public void showRecentApps(boolean fromHome) {
            this.mPolicy.showRecentApps(fromHome);
        }

        public boolean isSafeModeEnabled() {
            return this.mSafeMode;
        }

        public boolean clearWindowContentFrameStats(IBinder token) {
            if (checkCallingPermission("android.permission.FRAME_STATS", "clearWindowContentFrameStats()")) {
                synchronized (this.mWindowMap) {
                    WindowState windowState = (WindowState) this.mWindowMap.get(token);
                    if (windowState == null) {
                        return false;
                    }
                    WindowSurfaceController surfaceController = windowState.mWinAnimator.mSurfaceController;
                    if (surfaceController == null) {
                        return false;
                    }
                    boolean clearWindowContentFrameStats = surfaceController.clearWindowContentFrameStats();
                    return clearWindowContentFrameStats;
                }
            }
            throw new SecurityException("Requires FRAME_STATS permission");
        }

        public WindowContentFrameStats getWindowContentFrameStats(IBinder token) {
            if (checkCallingPermission("android.permission.FRAME_STATS", "getWindowContentFrameStats()")) {
                synchronized (this.mWindowMap) {
                    WindowState windowState = (WindowState) this.mWindowMap.get(token);
                    if (windowState == null) {
                        return null;
                    }
                    WindowSurfaceController surfaceController = windowState.mWinAnimator.mSurfaceController;
                    if (surfaceController == null) {
                        return null;
                    }
                    if (this.mTempWindowRenderStats == null) {
                        this.mTempWindowRenderStats = new WindowContentFrameStats();
                    }
                    WindowContentFrameStats stats = this.mTempWindowRenderStats;
                    if (surfaceController.getWindowContentFrameStats(stats)) {
                        return stats;
                    }
                    return null;
                }
            }
            throw new SecurityException("Requires FRAME_STATS permission");
        }

        public void notifyAppRelaunching(IBinder token) {
            synchronized (this.mWindowMap) {
                AppWindowToken appWindow = findAppWindowToken(token);
                if (appWindow != null) {
                    appWindow.startRelaunching();
                }
            }
        }

        public void notifyAppRelaunchingFinished(IBinder token) {
            synchronized (this.mWindowMap) {
                AppWindowToken appWindow = findAppWindowToken(token);
                if (appWindow != null) {
                    appWindow.finishRelaunching();
                }
            }
        }

        public int getDockedDividerInsetsLw() {
            return getDefaultDisplayContentLocked().getDockedDividerController().getContentInsets();
        }

        void dumpPolicyLocked(PrintWriter pw, String[] args, boolean dumpAll) {
            pw.println("WINDOW MANAGER POLICY STATE (dumpsys window policy)");
            this.mPolicy.dump("    ", pw, args);
        }

        void dumpAnimatorLocked(PrintWriter pw, String[] args, boolean dumpAll) {
            pw.println("WINDOW MANAGER ANIMATOR STATE (dumpsys window animator)");
            this.mAnimator.dumpLocked(pw, "    ", dumpAll);
        }

        void dumpTokensLocked(PrintWriter pw, boolean dumpAll) {
            WindowToken token;
            pw.println("WINDOW MANAGER TOKENS (dumpsys window tokens)");
            if (!this.mTokenMap.isEmpty()) {
                pw.println("  All tokens:");
                for (WindowToken token2 : this.mTokenMap.values()) {
                    pw.print("  ");
                    pw.print(token2);
                    if (dumpAll) {
                        pw.println(':');
                        token2.dump(pw, "    ");
                    } else {
                        pw.println();
                    }
                }
            }
            this.mWallpaperControllerLocked.dumpTokens(pw, "  ", dumpAll);
            if (!this.mFinishedStarting.isEmpty()) {
                pw.println();
                pw.println("  Finishing start of application tokens:");
                for (int i = this.mFinishedStarting.size() - 1; i >= 0; i--) {
                    token2 = (WindowToken) this.mFinishedStarting.get(i);
                    pw.print("  Finished Starting #");
                    pw.print(i);
                    pw.print(' ');
                    pw.print(token2);
                    if (dumpAll) {
                        pw.println(':');
                        token2.dump(pw, "    ");
                    } else {
                        pw.println();
                    }
                }
            }
            if (!this.mOpeningApps.isEmpty() || !this.mClosingApps.isEmpty()) {
                pw.println();
                if (this.mOpeningApps.size() > 0) {
                    pw.print("  mOpeningApps=");
                    pw.println(this.mOpeningApps);
                }
                if (this.mClosingApps.size() > 0) {
                    pw.print("  mClosingApps=");
                    pw.println(this.mClosingApps);
                }
            }
        }

        void dumpSessionsLocked(PrintWriter pw, boolean dumpAll) {
            pw.println("WINDOW MANAGER SESSIONS (dumpsys window sessions)");
            for (int i = 0; i < this.mSessions.size(); i++) {
                Session s = (Session) this.mSessions.valueAt(i);
                pw.print("  Session ");
                pw.print(s);
                pw.println(':');
                s.dump(pw, "    ");
            }
        }

        void dumpDisplayContentsLocked(PrintWriter pw, boolean dumpAll) {
            pw.println("WINDOW MANAGER DISPLAY CONTENTS (dumpsys window displays)");
            if (this.mDisplayReady) {
                int numDisplays = this.mDisplayContents.size();
                for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                    ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).dump("  ", pw);
                }
                return;
            }
            pw.println("  NO DISPLAY");
        }

        void dumpWindowsLocked(PrintWriter pw, boolean dumpAll, ArrayList<WindowState> windows) {
            pw.println("WINDOW MANAGER WINDOWS (dumpsys window windows)");
            dumpWindowsNoHeaderLocked(pw, dumpAll, windows);
        }

        void dumpWindowsNoHeaderLocked(PrintWriter pw, boolean dumpAll, ArrayList<WindowState> windows) {
            int displayNdx;
            int i;
            int numDisplays = this.mDisplayContents.size();
            for (displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                WindowList windowList = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                for (int winNdx = windowList.size() - 1; winNdx >= 0; winNdx--) {
                    WindowState w = (WindowState) windowList.get(winNdx);
                    if (!w.toString().contains("hwSingleMode_window") && (windows == null || windows.contains(w))) {
                        pw.print("  Window #");
                        pw.print(winNdx);
                        pw.print(' ');
                        pw.print(w);
                        pw.println(":");
                        String str = "    ";
                        boolean z = dumpAll || windows != null;
                        w.dump(pw, str, z);
                    }
                }
            }
            if (this.mInputMethodDialogs.size() > 0) {
                pw.println();
                pw.println("  Input method dialogs:");
                for (i = this.mInputMethodDialogs.size() - 1; i >= 0; i--) {
                    w = (WindowState) this.mInputMethodDialogs.get(i);
                    if (windows == null || windows.contains(w)) {
                        pw.print("  IM Dialog #");
                        pw.print(i);
                        pw.print(": ");
                        pw.println(w);
                    }
                }
            }
            if (this.mPendingRemove.size() > 0) {
                pw.println();
                pw.println("  Remove pending for:");
                for (i = this.mPendingRemove.size() - 1; i >= 0; i--) {
                    w = (WindowState) this.mPendingRemove.get(i);
                    if (windows == null || windows.contains(w)) {
                        pw.print("  Remove #");
                        pw.print(i);
                        pw.print(' ');
                        pw.print(w);
                        if (dumpAll) {
                            pw.println(":");
                            w.dump(pw, "    ", true);
                        } else {
                            pw.println();
                        }
                    }
                }
            }
            if (this.mForceRemoves != null && this.mForceRemoves.size() > 0) {
                pw.println();
                pw.println("  Windows force removing:");
                for (i = this.mForceRemoves.size() - 1; i >= 0; i--) {
                    w = (WindowState) this.mForceRemoves.get(i);
                    pw.print("  Removing #");
                    pw.print(i);
                    pw.print(' ');
                    pw.print(w);
                    if (dumpAll) {
                        pw.println(":");
                        w.dump(pw, "    ", true);
                    } else {
                        pw.println();
                    }
                }
            }
            if (this.mDestroySurface.size() > 0) {
                pw.println();
                pw.println("  Windows waiting to destroy their surface:");
                for (i = this.mDestroySurface.size() - 1; i >= 0; i--) {
                    w = (WindowState) this.mDestroySurface.get(i);
                    if (windows == null || windows.contains(w)) {
                        pw.print("  Destroy #");
                        pw.print(i);
                        pw.print(' ');
                        pw.print(w);
                        if (dumpAll) {
                            pw.println(":");
                            w.dump(pw, "    ", true);
                        } else {
                            pw.println();
                        }
                    }
                }
            }
            if (this.mLosingFocus.size() > 0) {
                pw.println();
                pw.println("  Windows losing focus:");
                for (i = this.mLosingFocus.size() - 1; i >= 0; i--) {
                    w = (WindowState) this.mLosingFocus.get(i);
                    if (windows == null || windows.contains(w)) {
                        pw.print("  Losing #");
                        pw.print(i);
                        pw.print(' ');
                        pw.print(w);
                        if (dumpAll) {
                            pw.println(":");
                            w.dump(pw, "    ", true);
                        } else {
                            pw.println();
                        }
                    }
                }
            }
            if (this.mResizingWindows.size() > 0) {
                pw.println();
                pw.println("  Windows waiting to resize:");
                for (i = this.mResizingWindows.size() - 1; i >= 0; i--) {
                    w = (WindowState) this.mResizingWindows.get(i);
                    if (windows == null || windows.contains(w)) {
                        pw.print("  Resizing #");
                        pw.print(i);
                        pw.print(' ');
                        pw.print(w);
                        if (dumpAll) {
                            pw.println(":");
                            w.dump(pw, "    ", true);
                        } else {
                            pw.println();
                        }
                    }
                }
            }
            if (this.mWaitingForDrawn.size() > 0) {
                pw.println();
                pw.println("  Clients waiting for these windows to be drawn:");
                for (i = this.mWaitingForDrawn.size() - 1; i >= 0; i--) {
                    WindowState win = (WindowState) this.mWaitingForDrawn.get(i);
                    pw.print("  Waiting #");
                    pw.print(i);
                    pw.print(' ');
                    pw.print(win);
                }
            }
            pw.println();
            pw.print("  mCurConfiguration=");
            pw.println(this.mCurConfiguration);
            pw.print("  mHasPermanentDpad=");
            pw.println(this.mHasPermanentDpad);
            pw.print("  mCurrentFocus=");
            pw.println(this.mCurrentFocus);
            if (this.mLastFocus != this.mCurrentFocus) {
                pw.print("  mLastFocus=");
                pw.println(this.mLastFocus);
            }
            pw.print("  mFocusedApp=");
            pw.println(this.mFocusedApp);
            if (this.mInputMethodTarget != null) {
                pw.print("  mInputMethodTarget=");
                pw.println(this.mInputMethodTarget);
            }
            pw.print("  mInTouchMode=");
            pw.print(this.mInTouchMode);
            pw.print(" mLayoutSeq=");
            pw.println(this.mLayoutSeq);
            pw.print("  mLastDisplayFreezeDuration=");
            TimeUtils.formatDuration((long) this.mLastDisplayFreezeDuration, pw);
            if (this.mLastFinishedFreezeSource != null) {
                pw.print(" due to ");
                pw.print(this.mLastFinishedFreezeSource);
            }
            pw.println();
            pw.print("  mLastWakeLockHoldingWindow=");
            pw.print(this.mLastWakeLockHoldingWindow);
            pw.print(" mLastWakeLockObscuringWindow=");
            pw.print(this.mLastWakeLockObscuringWindow);
            pw.println();
            this.mInputMonitor.dump(pw, "  ");
            if (dumpAll) {
                pw.print("  mSystemDecorLayer=");
                pw.print(this.mSystemDecorLayer);
                pw.print(" mScreenRect=");
                pw.println(this.mScreenRect.toShortString());
                if (this.mLastStatusBarVisibility != 0) {
                    pw.print("  mLastStatusBarVisibility=0x");
                    pw.println(Integer.toHexString(this.mLastStatusBarVisibility));
                }
                if (this.mInputMethodWindow != null) {
                    pw.print("  mInputMethodWindow=");
                    pw.println(this.mInputMethodWindow);
                }
                this.mWindowPlacerLocked.dump(pw, "  ");
                this.mWallpaperControllerLocked.dump(pw, "  ");
                this.mLayersController.dump(pw, "  ");
                pw.print("  mSystemBooted=");
                pw.print(this.mSystemBooted);
                pw.print(" mDisplayEnabled=");
                pw.println(this.mDisplayEnabled);
                if (needsLayout()) {
                    pw.print("  layoutNeeded on displays=");
                    for (displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                        DisplayContent displayContent = (DisplayContent) this.mDisplayContents.valueAt(displayNdx);
                        if (displayContent.layoutNeeded) {
                            pw.print(displayContent.getDisplayId());
                        }
                    }
                    pw.println();
                }
                pw.print("  mTransactionSequence=");
                pw.println(this.mTransactionSequence);
                pw.print("  mDisplayFrozen=");
                pw.print(this.mDisplayFrozen);
                pw.print(" windows=");
                pw.print(this.mWindowsFreezingScreen);
                pw.print(" client=");
                pw.print(this.mClientFreezingScreen);
                pw.print(" apps=");
                pw.print(this.mAppsFreezingScreen);
                pw.print(" waitingForConfig=");
                pw.println(this.mWaitingForConfig);
                pw.print("  mRotation=");
                pw.print(this.mRotation);
                pw.print(" mAltOrientation=");
                pw.println(this.mAltOrientation);
                pw.print("  mLastWindowForcedOrientation=");
                pw.print(this.mLastWindowForcedOrientation);
                pw.print(" mForcedAppOrientation=");
                pw.println(this.mForcedAppOrientation);
                pw.print("  mDeferredRotationPauseCount=");
                pw.println(this.mDeferredRotationPauseCount);
                pw.print("  Animation settings: disabled=");
                pw.print(this.mAnimationsDisabled);
                pw.print(" window=");
                pw.print(this.mWindowAnimationScaleSetting);
                pw.print(" transition=");
                pw.print(this.mTransitionAnimationScaleSetting);
                pw.print(" animator=");
                pw.println(this.mAnimatorDurationScaleSetting);
                pw.print(" mSkipAppTransitionAnimation=");
                pw.println(this.mSkipAppTransitionAnimation);
                pw.println("  mLayoutToAnim:");
                this.mAppTransition.dump(pw, "    ");
            }
        }

        boolean dumpWindows(PrintWriter pw, String name, String[] args, int opti, boolean dumpAll) {
            WindowList windows = new WindowList();
            HashMap hashMap;
            int numDisplays;
            int displayNdx;
            WindowList windowList;
            int winNdx;
            WindowState w;
            if ("apps".equals(name) || "visible".equals(name) || "visible-apps".equals(name)) {
                boolean appsOnly = name.contains("apps");
                boolean visibleOnly = name.contains("visible");
                hashMap = this.mWindowMap;
                synchronized (hashMap) {
                    if (appsOnly) {
                        dumpDisplayContentsLocked(pw, true);
                    }
                    numDisplays = this.mDisplayContents.size();
                    for (displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                        windowList = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                        for (winNdx = windowList.size() - 1; winNdx >= 0; winNdx--) {
                            w = (WindowState) windowList.get(winNdx);
                            if ((!visibleOnly || w.mWinAnimator.getShown()) && !(appsOnly && w.mAppToken == null)) {
                                windows.add(w);
                            }
                        }
                    }
                }
            } else {
                CharSequence name2;
                int objectId = 0;
                try {
                    objectId = Integer.parseInt(name, 16);
                    name2 = null;
                } catch (RuntimeException e) {
                }
                hashMap = this.mWindowMap;
                synchronized (hashMap) {
                    numDisplays = this.mDisplayContents.size();
                    for (displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                        windowList = ((DisplayContent) this.mDisplayContents.valueAt(displayNdx)).getWindowList();
                        for (winNdx = windowList.size() - 1; winNdx >= 0; winNdx--) {
                            w = (WindowState) windowList.get(winNdx);
                            if (name2 != null) {
                                if (w.mAttrs.getTitle().toString().contains(name2)) {
                                    windows.add(w);
                                }
                            } else if (System.identityHashCode(w) == objectId) {
                                windows.add(w);
                            }
                        }
                    }
                }
            }
            if (windows.size() <= 0) {
                return false;
            }
            synchronized (this.mWindowMap) {
                dumpWindowsLocked(pw, dumpAll, windows);
            }
            return true;
        }

        void dumpLastANRLocked(PrintWriter pw) {
            pw.println("WINDOW MANAGER LAST ANR (dumpsys window lastanr)");
            if (this.mLastANRState == null) {
                pw.println("  <no ANR has occurred since boot>");
            } else {
                pw.println(this.mLastANRState);
            }
        }

        public void saveANRStateLocked(AppWindowToken appWindowToken, WindowState windowState, String reason) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new FastPrintWriter(sw, false, 1024);
            pw.println("  ANR time: " + DateFormat.getInstance().format(new Date()));
            if (appWindowToken != null) {
                pw.println("  Application at fault: " + appWindowToken.stringName);
            }
            if (windowState != null) {
                pw.println("  Window at fault: " + windowState.mAttrs.getTitle());
            }
            if (reason != null) {
                pw.println("  Reason: " + reason);
            }
            pw.println();
            dumpWindowsNoHeaderLocked(pw, true, null);
            pw.println();
            pw.println("Last ANR continued");
            dumpDisplayContentsLocked(pw, true);
            pw.close();
            this.mLastANRState = sw.toString();
            this.mH.removeMessages(38);
            this.mH.sendEmptyMessageDelayed(38, 7200000);
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            String str = null;
            if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump WindowManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            boolean dumpAll = false;
            int opti = 0;
            while (opti < args.length) {
                String opt = args[opti];
                if (opt == null || opt.length() <= 0 || opt.charAt(0) != '-') {
                    break;
                }
                opti++;
                if ("-a".equals(opt)) {
                    dumpAll = true;
                } else if ("-h".equals(opt)) {
                    pw.println("Window manager dump options:");
                    pw.println("  [-a] [-h] [cmd] ...");
                    pw.println("  cmd may be one of:");
                    pw.println("    l[astanr]: last ANR information");
                    pw.println("    p[policy]: policy state");
                    pw.println("    a[animator]: animator state");
                    pw.println("    s[essions]: active sessions");
                    pw.println("    surfaces: active surfaces (debugging enabled only)");
                    pw.println("    d[isplays]: active display contents");
                    pw.println("    t[okens]: token list");
                    pw.println("    w[indows]: window list");
                    pw.println("  cmd may also be a NAME to dump windows.  NAME may");
                    pw.println("    be a partial substring in a window name, a");
                    pw.println("    Window hex object identifier, or");
                    pw.println("    \"all\" for all windows, or");
                    pw.println("    \"visible\" for the visible windows.");
                    pw.println("    \"visible-apps\" for the visible app windows.");
                    pw.println("  -a: include all available server state.");
                    return;
                } else {
                    pw.println("Unknown argument: " + opt + "; use -h for help");
                }
            }
            if (opti < args.length) {
                String cmd = args[opti];
                opti++;
                if ("lastanr".equals(cmd) || "l".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        dumpLastANRLocked(pw);
                    }
                    return;
                } else if ("policy".equals(cmd) || "p".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        dumpPolicyLocked(pw, args, true);
                    }
                    return;
                } else if ("animator".equals(cmd) || "a".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        dumpAnimatorLocked(pw, args, true);
                    }
                    return;
                } else if ("sessions".equals(cmd) || "s".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        dumpSessionsLocked(pw, true);
                    }
                    return;
                } else if ("surfaces".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        SurfaceTrace.dumpAllSurfaces(pw, null);
                    }
                    return;
                } else if ("displays".equals(cmd) || "d".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        dumpDisplayContentsLocked(pw, true);
                    }
                    return;
                } else if ("tokens".equals(cmd) || "t".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        dumpTokensLocked(pw, true);
                    }
                    return;
                } else if ("windows".equals(cmd) || "w".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        dumpWindowsLocked(pw, true, null);
                    }
                    return;
                } else if ("all".equals(cmd) || "a".equals(cmd)) {
                    synchronized (this.mWindowMap) {
                        dumpWindowsLocked(pw, true, null);
                    }
                    return;
                } else {
                    if (!dumpWindows(pw, cmd, args, opti, dumpAll)) {
                        pw.println("Bad window command, or no windows match: " + cmd);
                        pw.println("Use -h for help.");
                    }
                    return;
                }
            }
            synchronized (this.mWindowMap) {
                pw.println();
                if (dumpAll) {
                    pw.println("-------------------------------------------------------------------------------");
                }
                dumpLastANRLocked(pw);
                pw.println();
                if (dumpAll) {
                    pw.println("-------------------------------------------------------------------------------");
                }
                dumpPolicyLocked(pw, args, dumpAll);
                pw.println();
                if (dumpAll) {
                    pw.println("-------------------------------------------------------------------------------");
                }
                dumpAnimatorLocked(pw, args, dumpAll);
                pw.println();
                if (dumpAll) {
                    pw.println("-------------------------------------------------------------------------------");
                }
                dumpSessionsLocked(pw, dumpAll);
                pw.println();
                if (dumpAll) {
                    pw.println("-------------------------------------------------------------------------------");
                }
                if (dumpAll) {
                    str = "-------------------------------------------------------------------------------";
                }
                SurfaceTrace.dumpAllSurfaces(pw, str);
                pw.println();
                if (dumpAll) {
                    pw.println("-------------------------------------------------------------------------------");
                }
                dumpDisplayContentsLocked(pw, dumpAll);
                pw.println();
                if (dumpAll) {
                    pw.println("-------------------------------------------------------------------------------");
                }
                dumpTokensLocked(pw, dumpAll);
                pw.println();
                if (dumpAll) {
                    pw.println("-------------------------------------------------------------------------------");
                }
                dumpWindowsLocked(pw, dumpAll, null);
            }
        }

        public void monitor() {
            synchronized (this.mWindowMap) {
            }
        }

        private DisplayContent newDisplayContentLocked(Display display) {
            DisplayContent displayContent = new DisplayContent(display, this);
            int displayId = display.getDisplayId();
            this.mDisplayContents.put(displayId, displayContent);
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            Rect rect = new Rect();
            this.mDisplaySettings.getOverscanLocked(displayInfo.name, displayInfo.uniqueId, rect);
            displayInfo.overscanLeft = rect.left;
            displayInfo.overscanTop = rect.top;
            displayInfo.overscanRight = rect.right;
            displayInfo.overscanBottom = rect.bottom;
            this.mDisplayManagerInternal.setDisplayInfoOverrideFromWindowManager(displayId, displayInfo);
            configureDisplayPolicyLocked(displayContent);
            if (displayId == 0) {
                displayContent.mTapDetector = new TaskTapPointerEventListener(this, displayContent);
                registerPointerEventListener(displayContent.mTapDetector);
                registerPointerEventListener(this.mMousePositionTracker);
            }
            return displayContent;
        }

        public void createDisplayContentLocked(Display display) {
            if (display == null) {
                throw new IllegalArgumentException("getDisplayContent: display must not be null");
            }
            getDisplayContentLocked(display.getDisplayId());
        }

        public DisplayContent getDisplayContentLocked(int displayId) {
            DisplayContent displayContent = (DisplayContent) this.mDisplayContents.get(displayId);
            if (displayContent != null) {
                return displayContent;
            }
            Display display = this.mDisplayManager.getDisplay(displayId);
            if (display != null) {
                return newDisplayContentLocked(display);
            }
            return displayContent;
        }

        public DisplayContent getDefaultDisplayContentLocked() {
            return getDisplayContentLocked(0);
        }

        public WindowList getDefaultWindowListLocked() {
            return getDefaultDisplayContentLocked().getWindowList();
        }

        public DisplayInfo getDefaultDisplayInfoLocked() {
            return getDefaultDisplayContentLocked().getDisplayInfo();
        }

        public WindowList getWindowListLocked(Display display) {
            return getWindowListLocked(display.getDisplayId());
        }

        public WindowList getWindowListLocked(int displayId) {
            DisplayContent displayContent = getDisplayContentLocked(displayId);
            if (displayContent != null) {
                return displayContent.getWindowList();
            }
            return null;
        }

        public void onDisplayAdded(int displayId) {
            this.mH.sendMessage(this.mH.obtainMessage(27, displayId, 0));
        }

        public void handleDisplayAdded(int displayId) {
            synchronized (this.mWindowMap) {
                Display display = this.mDisplayManager.getDisplay(displayId);
                if (display != null) {
                    createDisplayContentLocked(display);
                    displayReady(displayId);
                }
                this.mWindowPlacerLocked.requestTraversal();
            }
        }

        public void onDisplayRemoved(int displayId) {
            this.mH.sendMessage(this.mH.obtainMessage(28, displayId, 0));
        }

        private void handleDisplayRemovedLocked(int displayId) {
            DisplayContent displayContent = getDisplayContentLocked(displayId);
            if (displayContent != null) {
                if (displayContent.isAnimating()) {
                    displayContent.mDeferredRemoval = true;
                    return;
                }
                this.mDisplayContents.delete(displayId);
                displayContent.close();
                if (displayId == 0) {
                    unregisterPointerEventListener(displayContent.mTapDetector);
                    unregisterPointerEventListener(this.mMousePositionTracker);
                }
            }
            this.mAnimator.removeDisplayLocked(displayId);
            this.mWindowPlacerLocked.requestTraversal();
        }

        public void onDisplayChanged(int displayId) {
            this.mH.sendMessage(this.mH.obtainMessage(29, displayId, 0));
        }

        private void handleDisplayChangedLocked(int displayId) {
            DisplayContent displayContent = getDisplayContentLocked(displayId);
            if (displayContent != null) {
                displayContent.updateDisplayInfo();
            }
            this.mWindowPlacerLocked.requestTraversal();
        }

        public Object getWindowManagerLock() {
            return this.mWindowMap;
        }

        public void setReplacingWindow(IBinder token, boolean animate) {
            synchronized (this.mWindowMap) {
                AppWindowToken appWindowToken = findAppWindowToken(token);
                if (appWindowToken == null || !appWindowToken.isVisible()) {
                    Slog.w("WindowManager", "Attempted to set replacing window on non-existing app token " + token);
                    return;
                }
                appWindowToken.setReplacingWindows(animate);
            }
        }

        public void setReplacingWindows(IBinder token, boolean childrenOnly) {
            synchronized (this.mWindowMap) {
                AppWindowToken appWindowToken = findAppWindowToken(token);
                if (appWindowToken == null || !appWindowToken.isVisible()) {
                    Slog.w("WindowManager", "Attempted to set replacing window on non-existing app token " + token);
                    return;
                }
                if (childrenOnly) {
                    appWindowToken.setReplacingChildren();
                } else {
                    appWindowToken.setReplacingWindows(false);
                }
                scheduleClearReplacingWindowIfNeeded(token, true);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void scheduleClearReplacingWindowIfNeeded(IBinder token, boolean replacing) {
            synchronized (this.mWindowMap) {
                AppWindowToken appWindowToken = findAppWindowToken(token);
                if (appWindowToken == null) {
                    Slog.w("WindowManager", "Attempted to reset replacing window on non-existing app token " + token);
                } else if (replacing) {
                    scheduleReplacingWindowTimeouts(appWindowToken);
                } else {
                    appWindowToken.resetReplacingWindows();
                }
            }
        }

        void scheduleReplacingWindowTimeouts(AppWindowToken appWindowToken) {
            if (!this.mReplacingWindowTimeouts.contains(appWindowToken)) {
                this.mReplacingWindowTimeouts.add(appWindowToken);
            }
            this.mH.removeMessages(46);
            this.mH.sendEmptyMessageDelayed(46, 2000);
        }

        public int getDockedStackSide() {
            int dockSide;
            synchronized (this.mWindowMap) {
                TaskStack dockedStack = getDefaultDisplayContentLocked().getDockedStackVisibleForUserLocked();
                dockSide = dockedStack == null ? -1 : dockedStack.getDockSide();
            }
            return dockSide;
        }

        public void setDockedStackResizing(boolean resizing) {
            synchronized (this.mWindowMap) {
                getDefaultDisplayContentLocked().getDockedDividerController().setResizing(resizing);
                requestTraversal();
            }
        }

        public void setDockedStackDividerTouchRegion(Rect touchRegion) {
            synchronized (this.mWindowMap) {
                getDefaultDisplayContentLocked().getDockedDividerController().setTouchRegion(touchRegion);
                setFocusTaskRegionLocked();
            }
        }

        public void setResizeDimLayer(boolean visible, int targetStackId, float alpha) {
            synchronized (this.mWindowMap) {
                getDefaultDisplayContentLocked().getDockedDividerController().setResizeDimLayer(visible, targetStackId, alpha);
            }
        }

        public void animateResizePinnedStack(Rect bounds, int animationDuration) {
            synchronized (this.mWindowMap) {
                final TaskStack stack = (TaskStack) this.mStackIdToStack.get(4);
                if (stack == null) {
                    Slog.w(TAG, "animateResizePinnedStack: stackId 4 not found.");
                    return;
                }
                final Rect originalBounds = new Rect();
                stack.getBounds(originalBounds);
                final Rect rect = bounds;
                final int i = animationDuration;
                UiThread.getHandler().post(new Runnable() {
                    public void run() {
                        WindowManagerService.this.mBoundsAnimationController.animateBounds(stack, originalBounds, rect, i);
                    }
                });
            }
        }

        public void setTaskResizeable(int taskId, int resizeMode) {
            synchronized (this.mWindowMap) {
                Task task = (Task) this.mTaskIdToTask.get(taskId);
                if (task != null) {
                    task.setResizeable(resizeMode);
                }
            }
        }

        public void setForceResizableTasks(boolean forceResizableTasks) {
            synchronized (this.mWindowMap) {
                this.mForceResizableTasks = forceResizableTasks;
            }
        }

        static int dipToPixel(int dip, DisplayMetrics displayMetrics) {
            return (int) TypedValue.applyDimension(1, (float) dip, displayMetrics);
        }

        public void registerDockedStackListener(IDockedStackListener listener) {
            if (checkCallingPermission("android.permission.REGISTER_WINDOW_MANAGER_LISTENERS", "registerDockedStackListener()") || checkCallingPermission("huawei.android.permission.MULTIWINDOW_SDK", "registerDockedStackListener()")) {
                getDefaultDisplayContentLocked().mDividerControllerLocked.registerDockedStackListener(listener);
            }
        }

        public void requestAppKeyboardShortcuts(IResultReceiver receiver, int deviceId) {
            try {
                WindowState focusedWindow = getFocusedWindow();
                if (focusedWindow != null && focusedWindow.mClient != null) {
                    getFocusedWindow().mClient.requestAppKeyboardShortcuts(receiver, deviceId);
                }
            } catch (RemoteException e) {
            }
        }

        public void getStableInsets(Rect outInsets) throws RemoteException {
            synchronized (this.mWindowMap) {
                getStableInsetsLocked(outInsets);
            }
        }

        void getStableInsetsLocked(Rect outInsets) {
            DisplayInfo di = getDefaultDisplayInfoLocked();
            this.mPolicy.getStableInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, outInsets);
        }

        private void getNonDecorInsetsLocked(Rect outInsets) {
            DisplayInfo di = getDefaultDisplayInfoLocked();
            this.mPolicy.getNonDecorInsetsLw(di.rotation, di.logicalWidth, di.logicalHeight, outInsets);
        }

        public void subtractStableInsets(Rect inOutBounds) {
            synchronized (this.mWindowMap) {
                getStableInsetsLocked(this.mTmpRect2);
                DisplayInfo di = getDefaultDisplayInfoLocked();
                this.mTmpRect.set(0, 0, di.logicalWidth, di.logicalHeight);
                subtractInsets(this.mTmpRect, this.mTmpRect2, inOutBounds);
            }
        }

        public void subtractNonDecorInsets(Rect inOutBounds) {
            synchronized (this.mWindowMap) {
                getNonDecorInsetsLocked(this.mTmpRect2);
                DisplayInfo di = getDefaultDisplayInfoLocked();
                this.mTmpRect.set(0, 0, di.logicalWidth, di.logicalHeight);
                subtractInsets(this.mTmpRect, this.mTmpRect2, inOutBounds);
            }
        }

        void subtractInsets(Rect display, Rect insets, Rect inOutBounds) {
            this.mTmpRect3.set(display);
            this.mTmpRect3.inset(insets);
            inOutBounds.intersect(this.mTmpRect3);
        }

        public int getSmallestWidthForTaskBounds(Rect bounds) {
            int smallestWidthDpForBounds;
            synchronized (this.mWindowMap) {
                smallestWidthDpForBounds = getDefaultDisplayContentLocked().getDockedDividerController().getSmallestWidthDpForBounds(bounds);
            }
            return smallestWidthDpForBounds;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        void updatePointerIcon(IWindow client) {
            synchronized (this.mMousePositionTracker) {
                if (this.mMousePositionTracker.mLatestEventWasMouse) {
                    float mouseX = this.mMousePositionTracker.mLatestMouseX;
                    float mouseY = this.mMousePositionTracker.mLatestMouseY;
                } else {
                    return;
                }
            }
        }

        void restorePointerIconLocked(DisplayContent displayContent, float latestX, float latestY) {
            this.mMousePositionTracker.updatePosition(latestX, latestY);
            WindowState windowUnderPointer = displayContent.getTouchableWinAtPointLocked(latestX, latestY);
            if (windowUnderPointer != null) {
                try {
                    windowUnderPointer.mClient.updatePointerIcon(windowUnderPointer.translateToWindowX(latestX), windowUnderPointer.translateToWindowY(latestY));
                    return;
                } catch (RemoteException e) {
                    Slog.w("WindowManager", "unable to restore pointer icon");
                    return;
                }
            }
            InputManager.getInstance().setPointerIconType(1000);
        }

        public void registerShortcutKey(long shortcutCode, IShortcutService shortcutKeyReceiver) throws RemoteException {
            if (checkCallingPermission("android.permission.REGISTER_WINDOW_MANAGER_LISTENERS", "registerShortcutKey")) {
                this.mPolicy.registerShortcutKey(shortcutCode, shortcutKeyReceiver);
                return;
            }
            throw new SecurityException("Requires REGISTER_WINDOW_MANAGER_LISTENERS permission");
        }

        public final void performhwLayoutAndPlaceSurfacesLocked() {
            this.mWindowPlacerLocked.performSurfacePlacement();
        }

        protected boolean canBeFloatImeTarget(WindowState w) {
            int fl = w.mAttrs.flags & 131080;
            if (fl == 0 || fl == 131080 || w.mAttrs.type == 3) {
                return w.isVisibleOrAdding();
            }
            return false;
        }

        private void printFreezingDisplayLogs() {
            int appsCount = this.mOpeningApps.size();
            for (int i = 0; i < appsCount; i++) {
                AppWindowToken wtoken = (AppWindowToken) this.mOpeningApps.valueAt(i);
                StringBuilder builder = new StringBuilder();
                builder.append("opening app wtoken = ");
                builder.append(wtoken.toString());
                builder.append(", allDrawn= ");
                builder.append(wtoken.allDrawn);
                builder.append(", startingDisplayed =  ");
                builder.append(wtoken.startingDisplayed);
                builder.append(", startingMoved =  ");
                builder.append(wtoken.startingMoved);
                builder.append(", isRelaunching =  ");
                builder.append(wtoken.isRelaunching());
                Slog.d(TAG, "printFreezingDisplayLogs" + builder.toString());
            }
        }
    }
