package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityManager.ProcessErrorStateInfo;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManager.StackId;
import android.app.ActivityManager.StackInfo;
import android.app.ActivityManager.TaskDescription;
import android.app.ActivityManager.TaskThumbnail;
import android.app.ActivityManager.TaskThumbnailInfo;
import android.app.ActivityManagerInternal;
import android.app.ActivityManagerInternal.SleepToken;
import android.app.ActivityOptions;
import android.app.ActivityThread;
import android.app.AlertDialog;
import android.app.AppGlobals;
import android.app.ApplicationErrorReport.CrashInfo;
import android.app.ApplicationThreadNative;
import android.app.BroadcastOptions;
import android.app.Dialog;
import android.app.HwCustNonHardwareAcceleratedPackagesManager;
import android.app.IActivityContainer;
import android.app.IActivityContainerCallback;
import android.app.IActivityController;
import android.app.IActivityManager.ContentProviderHolder;
import android.app.IActivityManager.WaitResult;
import android.app.IAppTask;
import android.app.IAppTask.Stub;
import android.app.IApplicationThread;
import android.app.IInstrumentationWatcher;
import android.app.INotificationManager;
import android.app.IProcessObserver;
import android.app.IServiceConnection;
import android.app.IStopUserCallback;
import android.app.ITaskStackListener;
import android.app.IUiAutomationConnection;
import android.app.IUidObserver;
import android.app.IUserSwitchObserver;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProfilerInfo;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.app.backup.IBackupManager;
import android.app.usage.UsageStatsManagerInternal;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IContentProvider;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.UriPermission;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageManager;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ParceledListSlice;
import android.content.pm.PathPermission;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutServiceInternal;
import android.content.pm.UserInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hdm.HwDeviceManager;
import android.hwtheme.HwThemeManager;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Debug.MemoryInfo;
import android.os.DropBoxManager;
import android.os.Environment;
import android.os.FactoryTest;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.IPermissionController;
import android.os.IProcessInfoService;
import android.os.IProgressListener;
import android.os.LocaleList;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.PowerManagerInternal;
import android.os.Process;
import android.os.Process.ProcessStartResult;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.ViolationInfo;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.TransactionTooLargeException;
import android.os.UpdateLock;
import android.os.UserHandle;
import android.os.WorkSource;
import android.os.storage.IMountService;
import android.os.storage.MountServiceInternal;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.rms.HwSysResource;
import android.rog.AppRogInfo;
import android.rog.IHwRogListener;
import android.service.voice.IVoiceInteractionSession;
import android.service.voice.VoiceInteractionManagerInternal;
import android.telecom.TelecomManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.BoostFramework;
import android.util.DebugUtils;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwSlog;
import android.util.Jlog;
import android.util.Log;
import android.util.Pair;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.AssistUtils;
import com.android.internal.app.DumpHeapActivity;
import com.android.internal.app.IAppOpsCallback;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.app.ProcessMap;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.BatteryStatsImpl.BatteryCallback;
import com.android.internal.os.BatteryStatsImpl.Uid.Pkg.Serv;
import com.android.internal.os.BatteryStatsImpl.Uid.Proc;
import com.android.internal.os.HwBootFail;
import com.android.internal.os.IResultReceiver;
import com.android.internal.os.ProcessCpuTracker;
import com.android.internal.os.ProcessCpuTracker.Stats;
import com.android.internal.os.TransferPipe;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.MemInfoReader;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.server.AbsLocationManagerService;
import com.android.server.AlarmManagerService;
import com.android.server.AppOpsService;
import com.android.server.AttributeCache;
import com.android.server.HwServiceFactory;
import com.android.server.HwServiceFactory.IHwActiveServices;
import com.android.server.HwServiceFactory.IHwActivityManagerService;
import com.android.server.HwServiceFactory.IHwActivityStackSupervisor;
import com.android.server.HwServiceFactory.IHwActivityStarter;
import com.android.server.HwServiceFactory.IHwAppOpsService;
import com.android.server.HwServiceFactory.IHwBinderMonitor;
import com.android.server.IntentResolver;
import com.android.server.LocalServices;
import com.android.server.LockGuard;
import com.android.server.ServiceThread;
import com.android.server.SmartShrinker;
import com.android.server.SystemService;
import com.android.server.SystemServiceManager;
import com.android.server.UiThread;
import com.android.server.Watchdog;
import com.android.server.Watchdog.Monitor;
import com.android.server.am.ActivityStackSupervisor.ActivityContainer;
import com.android.server.am.UriPermission.PersistedTimeComparator;
import com.android.server.am.UriPermission.Snapshot;
import com.android.server.audio.AudioService;
import com.android.server.firewall.IntentFirewall;
import com.android.server.firewall.IntentFirewall.AMSInterface;
import com.android.server.job.JobSchedulerShellCommand;
import com.android.server.job.controllers.JobStatus;
import com.android.server.location.LocationFudger;
import com.android.server.pm.Installer;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.radar.FrameworkRadar;
import com.android.server.radar.RadarHeader;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.vr.VrManagerInternal;
import com.android.server.wm.WindowManagerService;
import com.google.android.collect.Lists;
import com.google.android.collect.Maps;
import com.hisi.perfhub.PerfHub;
import com.huawei.pgmng.common.Utils;
import com.huawei.pgmng.log.LogPower;
import dalvik.system.VMRuntime;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import libcore.io.IoUtils;
import libcore.util.EmptyArray;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ActivityManagerService extends AbsActivityManager implements Monitor, BatteryCallback {
    public static final String ACTION_TRIGGER_IDLE = "com.android.server.ACTION_TRIGGER_IDLE";
    static final int ADD_POWERSAVE_TEMP_WHITELIST_APP_MSG = 97;
    static final int ALLOW_FULL_ONLY = 2;
    static final int ALLOW_NON_FULL = 0;
    static final int ALLOW_NON_FULL_IN_PROFILE = 1;
    static final boolean ANIMATE = true;
    static final boolean APP_AUTO_START = SystemProperties.getBoolean("persist.app_auto_start", false);
    static final int APP_AUTO_START_OK = 0;
    static final int APP_BOOST_DEACTIVATE_MSG = 58;
    static final int APP_BOOST_MESSAGE_DELAY = 3000;
    static final int APP_BOOST_TIMEOUT = 2500;
    static final long APP_SWITCH_DELAY_TIME = 5000;
    private static final String ATTR_CREATED_TIME = "createdTime";
    private static final String ATTR_MODE_FLAGS = "modeFlags";
    private static final String ATTR_PREFIX = "prefix";
    private static final String ATTR_SOURCE_PKG = "sourcePkg";
    private static final String ATTR_SOURCE_USER_ID = "sourceUserId";
    private static final String ATTR_TARGET_PKG = "targetPkg";
    private static final String ATTR_TARGET_USER_ID = "targetUserId";
    private static final String ATTR_URI = "uri";
    private static final String ATTR_USER_HANDLE = "userHandle";
    static final int BACKGROUND_SETTLE_TIME = 60000;
    static final long BATTERY_STATS_TIME = 1800000;
    static final int BROADCAST_BG_TIMEOUT = 60000;
    static final int BROADCAST_FG_TIMEOUT;
    static final String CALLED_PRE_BOOTS_FILENAME = "called_pre_boots.dat";
    static final int CANCEL_HEAVY_NOTIFICATION_MSG = 25;
    static final int CHECK_EXCESSIVE_WAKE_LOCKS_MSG = 27;
    static final int CHECK_SERVICE_TIMEOUT_MSG = 99;
    static final int CLEAR_DNS_CACHE_MSG = 28;
    static final int COLLECT_PSS_BG_MSG = 1;
    static final int CONTENT_PROVIDER_PUBLISH_TIMEOUT = 10000;
    static final int CONTENT_PROVIDER_PUBLISH_TIMEOUT_MSG = 59;
    static final int CONTENT_PROVIDER_RETAIN_TIME = 20000;
    static final int CONTINUE_USER_SWITCH_MSG = 35;
    static final int CPU_MIN_CHECK_DURATION;
    static final int DELETE_DUMPHEAP_MSG = 52;
    static final int DISMISS_DIALOG_UI_MSG = 48;
    static final int DISPATCH_PROCESSES_CHANGED_UI_MSG = 31;
    static final int DISPATCH_PROCESS_DIED_UI_MSG = 32;
    static final int DISPATCH_UIDS_CHANGED_UI_MSG = 54;
    static final int DO_PENDING_ACTIVITY_LAUNCHES_MSG = 21;
    static final int DROPBOX_MAX_SIZE = 196608;
    static final long[] DUMP_MEM_BUCKETS = new long[]{5120, 7168, 10240, 15360, 20480, 30720, 40960, 81920, 122880, 163840, 204800, 256000, 307200, 358400, 409600, 512000, 614400, 819200, 1048576, 2097152, 5242880, 10485760, 20971520};
    static final int[] DUMP_MEM_OOM_ADJ = new int[]{JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE, -900, -800, -700, 0, 100, FIRST_BROADCAST_QUEUE_MSG, FIRST_COMPAT_MODE_MSG, 400, 500, 600, 700, 800, 900};
    static final String[] DUMP_MEM_OOM_COMPACT_LABEL = new String[]{"native", "sys", "pers", "persvc", "fore", "vis", "percept", "heavy", "backup", "servicea", "home", "prev", "serviceb", "cached"};
    static final String[] DUMP_MEM_OOM_LABEL = new String[]{"Native", "System", "Persistent", "Persistent Service", "Foreground", "Visible", "Perceptible", "Heavy Weight", "Backup", "A Services", "Home", "Previous", "B Services", "Cached"};
    static final String[] EMPTY_STRING_ARRAY = new String[0];
    static final int ENTER_ANIMATION_COMPLETE_MSG = 44;
    static final int FINALIZE_PENDING_INTENT_MSG = 23;
    static final int FINISH_BOOTING_MSG = 45;
    static final int FIRST_ACTIVITY_STACK_MSG = 100;
    static final int FIRST_BROADCAST_QUEUE_MSG = 200;
    static final int FIRST_COMPAT_MODE_MSG = 300;
    static final int FIRST_SUPERVISOR_STACK_MSG = 100;
    static final int FOREGROUND_PROFILE_CHANGED_MSG = 53;
    public static final float FULLSCREEN_SCREENSHOT_SCALE = 0.6f;
    static final int FULL_PSS_LOWERED_INTERVAL = 120000;
    static final int FULL_PSS_MIN_INTERVAL = 600000;
    static final int GC_BACKGROUND_PROCESSES_MSG = 5;
    static final int GC_MIN_INTERVAL = 60000;
    static final int GC_TIMEOUT = 5000;
    static final int IDLE_UIDS_MSG = 60;
    static final int IMMERSIVE_MODE_LOCK_MSG = 37;
    static final int INSTRUMENTATION_KEY_DISPATCHING_TIMEOUT = 60000;
    private static final String INTENT_REMOTE_BUGREPORT_FINISHED = "android.intent.action.REMOTE_BUGREPORT_FINISHED";
    static final boolean IS_DEBUG_VERSION;
    static final boolean IS_FPGA = boardname.contains("fpga");
    static final boolean IS_USER_BUILD = "user".equals(Build.TYPE);
    static final int KEY_DISPATCHING_TIMEOUT;
    static final int KILL_APPLICATION_MSG = 22;
    private static final int KSM_SHARED = 0;
    private static final int KSM_SHARING = 1;
    private static final int KSM_UNSHARED = 2;
    private static final int KSM_VOLATILE = 3;
    static final int LOCK_SCREEN_HIDDEN = 0;
    static final int LOCK_SCREEN_LEAVING = 1;
    static final int LOCK_SCREEN_SHOWN = 2;
    static final int LOG_STACK_STATE = 62;
    private static final int MAX_DUP_SUPPRESSED_STACKS = 5000;
    static final int MAX_PERSISTED_URI_GRANTS = 128;
    private static final int MEMINFO_COMPACT_VERSION = 1;
    static final long MONITOR_CPU_MAX_TIME = 268435455;
    static final long MONITOR_CPU_MIN_TIME = 5000;
    static final boolean MONITOR_CPU_USAGE = true;
    static final boolean MONITOR_THREAD_CPU_USAGE = false;
    static final int MY_PID = Process.myPid();
    static final int NOTIFY_ACTIVITY_DISMISSING_DOCKED_STACK_MSG = 68;
    static final int NOTIFY_ACTIVITY_PINNED_LISTENERS_MSG = 64;
    static final int NOTIFY_CLEARTEXT_NETWORK_MSG = 50;
    static final int NOTIFY_FORCED_RESIZABLE_MSG = 67;
    static final int NOTIFY_PINNED_ACTIVITY_RESTART_ATTEMPT_LISTENERS_MSG = 65;
    static final int NOTIFY_PINNED_STACK_ANIMATION_ENDED_LISTENERS_MSG = 66;
    static final int NOTIFY_TASK_STACK_CHANGE_LISTENERS_DELAY = 100;
    static final int NOTIFY_TASK_STACK_CHANGE_LISTENERS_MSG = 49;
    static final int PENDING_ASSIST_EXTRAS_LONG_TIMEOUT = 2000;
    static final int PENDING_ASSIST_EXTRAS_TIMEOUT = 500;
    private static final int PERSISTENT_MASK = 9;
    static final int PERSIST_URI_GRANTS_MSG = 38;
    static final int POST_DUMP_HEAP_NOTIFICATION_MSG = 51;
    static final int POST_HEAVY_NOTIFICATION_MSG = 24;
    static final int POWER_CHECK_DELAY = (((ActivityManagerDebugConfig.DEBUG_POWER_QUICK ? 2 : 15) * 60) * 1000);
    private static final int[] PROCESS_STATE_STATS_FORMAT = new int[]{32, 544, 10272};
    static final int PROC_START_TIMEOUT = 10000;
    static final int PROC_START_TIMEOUT_MSG = 20;
    static final int PROC_START_TIMEOUT_WITH_WRAPPER = 1200000;
    protected static final String REASON_CLONED_APP_DELETED = "delete cloned app";
    private static final String REASON_STOP_BY_APP = "by app";
    private static final String REASON_SYS_REPLACE = "replace sys pkg";
    private static final boolean REMOVE_FROM_RECENTS = true;
    static final int REPORT_MEM_USAGE_MSG = 33;
    static final int REPORT_TIME_TRACKER_MSG = 55;
    static final int REPORT_USER_SWITCH_COMPLETE_MSG = 56;
    static final int REPORT_USER_SWITCH_MSG = 34;
    static final int REQUEST_ALL_PSS_MSG = 39;
    static final int RESERVED_BYTES_PER_LOGCAT_LINE = 100;
    static final int SEND_LOCALE_TO_MOUNT_DAEMON_MSG = 47;
    static final int SERVICE_TIMEOUT_MSG = 12;
    static final int SERVICE_USAGE_INTERACTION_TIME = 1800000;
    static final int SHOW_COMPAT_MODE_DIALOG_UI_MSG = 30;
    static final int SHOW_ERROR_UI_MSG = 1;
    static final int SHOW_FACTORY_ERROR_UI_MSG = 3;
    static final int SHOW_FINGERPRINT_ERROR_UI_MSG = 15;
    static final int SHOW_NOT_RESPONDING_UI_MSG = 2;
    static final int SHOW_STRICT_MODE_VIOLATION_UI_MSG = 26;
    static final int SHOW_UID_ERROR_UI_MSG = 14;
    static final int SHOW_UNSUPPORTED_DISPLAY_SIZE_DIALOG_MSG = 70;
    static final int SHUTDOWN_UI_AUTOMATION_CONNECTION_MSG = 57;
    static final int START_PROFILES_MSG = 40;
    static final int START_USER_SWITCH_UI_MSG = 46;
    static final int STOCK_PM_FLAGS = 1024;
    static final String SYSTEM_DEBUGGABLE = "ro.debuggable";
    static final int SYSTEM_USER_CURRENT_MSG = 43;
    static final int SYSTEM_USER_START_MSG = 42;
    static final int SYSTEM_USER_UNLOCK_MSG = 61;
    static final String TAG = "ActivityManager";
    private static final String TAG_BACKUP = (TAG + ActivityManagerDebugConfig.POSTFIX_BACKUP);
    private static final String TAG_BROADCAST = (TAG + ActivityManagerDebugConfig.POSTFIX_BROADCAST);
    private static final String TAG_CLEANUP = (TAG + ActivityManagerDebugConfig.POSTFIX_CLEANUP);
    private static final String TAG_CONFIGURATION = (TAG + ActivityManagerDebugConfig.POSTFIX_CONFIGURATION);
    private static final String TAG_FOCUS = (TAG + ActivityManagerDebugConfig.POSTFIX_FOCUS);
    private static final String TAG_IMMERSIVE = (TAG + ActivityManagerDebugConfig.POSTFIX_IMMERSIVE);
    private static final String TAG_LOCKSCREEN = (TAG + ActivityManagerDebugConfig.POSTFIX_LOCKSCREEN);
    private static final String TAG_LOCKTASK = (TAG + ActivityManagerDebugConfig.POSTFIX_LOCKTASK);
    private static final String TAG_LRU = (TAG + ActivityManagerDebugConfig.POSTFIX_LRU);
    private static final String TAG_MU = (TAG + "_MU");
    private static final String TAG_OOM_ADJ = (TAG + ActivityManagerDebugConfig.POSTFIX_OOM_ADJ);
    private static final String TAG_POWER = (TAG + ActivityManagerDebugConfig.POSTFIX_POWER);
    private static final String TAG_PROCESSES = (TAG + ActivityManagerDebugConfig.POSTFIX_PROCESSES);
    private static final String TAG_PROCESS_OBSERVERS = (TAG + ActivityManagerDebugConfig.POSTFIX_PROCESS_OBSERVERS);
    private static final String TAG_PROVIDER = (TAG + ActivityManagerDebugConfig.POSTFIX_PROVIDER);
    private static final String TAG_PSS = (TAG + ActivityManagerDebugConfig.POSTFIX_PSS);
    private static final String TAG_RECENTS = (TAG + ActivityManagerDebugConfig.POSTFIX_RECENTS);
    private static final String TAG_SERVICE = (TAG + ActivityManagerDebugConfig.POSTFIX_SERVICE);
    private static final String TAG_STACK = (TAG + ActivityManagerDebugConfig.POSTFIX_STACK);
    private static final String TAG_SWITCH = (TAG + ActivityManagerDebugConfig.POSTFIX_SWITCH);
    private static final String TAG_UID_OBSERVERS = (TAG + ActivityManagerDebugConfig.POSTFIX_UID_OBSERVERS);
    private static final String TAG_URI_GRANT = "uri-grant";
    private static final String TAG_URI_GRANTS = "uri-grants";
    private static final String TAG_URI_PERMISSION = (TAG + ActivityManagerDebugConfig.POSTFIX_URI_PERMISSION);
    private static final String TAG_VISIBILITY = (TAG + ActivityManagerDebugConfig.POSTFIX_VISIBILITY);
    private static final String TAG_VISIBLE_BEHIND = (TAG + ActivityManagerDebugConfig.POSTFIX_VISIBLE_BEHIND);
    static final boolean TAKE_FULLSCREEN_SCREENSHOTS = true;
    static final int TRIM_SERVICE_AFTER_BOOT = 98;
    static final int UPDATE_CONFIGURATION_MSG = 4;
    static final int UPDATE_HTTP_PROXY_MSG = 29;
    static final int UPDATE_TIME = 41;
    static final int UPDATE_TIME_ZONE = 13;
    static final long USAGE_STATS_INTERACTION_INTERVAL = 86400000;
    static final int USER_SWITCH_TIMEOUT_MSG = 36;
    static final boolean VALIDATE_UID_STATES = true;
    static final int VR_MODE_APPLY_IF_NEEDED_MSG = 69;
    static final int VR_MODE_CHANGE_MSG = 63;
    static final int WAIT_FOR_DEBUGGER_UI_MSG = 6;
    static final int WAKE_LOCK_MIN_CHECK_DURATION;
    static String boardname = SystemProperties.get("ro.board.boardname", "0");
    private static final ThreadLocal<Identity> sCallerIdentity = new ThreadLocal();
    static KillHandler sKillHandler = null;
    static ServiceThread sKillThread = null;
    static ThreadLocal<PriorityState> sThreadPriorityState = new ThreadLocal<PriorityState>() {
        protected PriorityState initialValue() {
            return new PriorityState();
        }
    };
    final int GL_ES_VERSION;
    ProcessChangeItem[] mActiveProcessChanges;
    ChangeItem[] mActiveUidChanges;
    final SparseArray<UidRecord> mActiveUids;
    boolean mActivityIdle;
    final ActivityStarter mActivityStarter;
    int mAdjSeq;
    boolean mAllowLowerMemLevel;
    private AlarmManagerService mAlms;
    private final HashSet<Integer> mAlreadyLoggedViolatedStacks;
    boolean mAlwaysFinishActivities;
    HashMap<String, IBinder> mAppBindArgs;
    final AppErrors mAppErrors;
    final AppOpsService mAppOpsService;
    long mAppSwitchesAllowedTime;
    final SparseArray<ArrayMap<ComponentName, SparseArray<ArrayMap<String, Association>>>> mAssociations;
    boolean mAutoStopProfiler;
    final ArrayList<ProcessChangeItem> mAvailProcessChanges;
    final ArrayList<ChangeItem> mAvailUidChanges;
    String mBackupAppName;
    BackupRecord mBackupTarget;
    final ArrayMap<String, Boolean> mBadPkgs;
    final ProcessMap<BadProcessInfo> mBadProcesses;
    final BatteryStatsService mBatteryStatsService;
    BroadcastQueue mBgBroadcastQueue;
    final Handler mBgHandler;
    BroadcastQueue mBgKeyAppBroadcastQueue;
    BroadcastQueue mBgThirdAppBroadcastQueue;
    private boolean mBinderTransactionTrackingEnabled;
    private long mBoostStartTime = 0;
    @GuardedBy("this")
    boolean mBootAnimationComplete;
    boolean mBooted;
    @GuardedBy("this")
    boolean mBooting;
    final BroadcastQueue[] mBroadcastQueues;
    @GuardedBy("this")
    boolean mCallFinishBooting;
    @GuardedBy("this")
    boolean mCheckedForSetup;
    CompatModeDialog mCompatModeDialog;
    final CompatModePackages mCompatModePackages;
    Configuration mConfiguration;
    int mConfigurationSeq;
    Context mContext;
    IActivityController mController;
    boolean mControllerIsAMonkey;
    CoreSettingsObserver mCoreSettingsObserver;
    protected boolean mCpusetSwitch = false;
    private HashSet<String> mCtsActions;
    private HashSet<String> mCtsPackages;
    private AppTimeTracker mCurAppTimeTracker;
    BroadcastStats mCurBroadcastStats;
    private String mCurResumedPackage;
    private int mCurResumedUid;
    private HwCustActivityManagerService mCustAms;
    IActivityController mCustomController;
    String mDebugApp;
    boolean mDebugTransient;
    Rect mDefaultPinnedStackBounds;
    String mDeviceOwnerName;
    boolean mDidAppSwitch;
    boolean mDidDexOpt;
    boolean mDoingSetFocusedActivity;
    volatile int mFactoryTest;
    BroadcastQueue mFgBroadcastQueue;
    BroadcastQueue mFgKeyAppBroadcastQueue;
    BroadcastQueue mFgThirdAppBroadcastQueue;
    ActivityRecord mFocusedActivity;
    FontScaleSettingObserver mFontScaleSettingObserver;
    boolean mForceResizableActivities;
    final ProcessMap<ArrayList<ProcessRecord>> mForegroundPackages;
    final SparseArray<ForegroundToken> mForegroundProcesses;
    boolean mFullPssPending;
    float mFullscreenThumbnailScale;
    private final AtomicFile mGrantFile;
    @GuardedBy("this")
    private final SparseArray<ArrayMap<GrantUri, UriPermission>> mGrantedUriPermissions;
    final MainHandler mHandler;
    final ServiceThread mHandlerThread;
    boolean mHasRecents;
    ProcessRecord mHeavyWeightProcess;
    ProcessRecord mHomeProcess;
    private boolean mInVrMode = false;
    Installer mInstaller;
    final InstrumentationReporter mInstrumentationReporter = new InstrumentationReporter();
    public IntentFirewall mIntentFirewall;
    final HashMap<Key, WeakReference<PendingIntentRecord>> mIntentSenderRecords;
    private int mIsAppAutoStart = -1;
    private boolean mIsBoosted = false;
    boolean mIsHwLowRam;
    public boolean mIsPerfBoostEnabled;
    HashMap<String, IBinder> mIsolatedAppBindArgs;
    final SparseArray<ProcessRecord> mIsolatedProcesses;
    ActivityInfo mLastAddedTaskActivity;
    ComponentName mLastAddedTaskComponent;
    int mLastAddedTaskUid;
    BroadcastStats mLastBroadcastStats;
    final AtomicLong mLastCpuTime;
    private int mLastFocusedUserId;
    long mLastFullPssTime;
    long mLastIdleTime;
    long mLastMemUsageReportTime;
    int mLastMemoryLevel;
    int mLastNumProcesses;
    long mLastPowerCheckRealtime;
    long mLastPowerCheckUptime;
    long mLastWriteTime;
    @GuardedBy("this")
    boolean mLaunchWarningShown;
    final ArrayList<ContentProviderRecord> mLaunchingProviders;
    boolean mLenientBackgroundCheck;
    com.android.server.DeviceIdleController.LocalService mLocalDeviceIdleController;
    PowerManagerInternal mLocalPowerManager;
    int mLockScreenShown;
    SparseArray<String[]> mLockTaskPackages;
    long mLowRamStartTime;
    long mLowRamTimeSinceLastIdle;
    int mLruProcessActivityStart;
    int mLruProcessServiceStart;
    protected final ArrayList<ProcessRecord> mLruProcesses;
    int mLruSeq;
    String mMemWatchDumpFile;
    int mMemWatchDumpPid;
    String mMemWatchDumpProcName;
    int mMemWatchDumpUid;
    final ProcessMap<Pair<Long, String>> mMemWatchProcesses;
    String mNativeDebuggingApp;
    int mNewNumAServiceProcs;
    int mNewNumServiceProcs;
    int mNextIsolatedProcessUid;
    int mNumCachedHiddenProcs;
    int mNumNonCachedProcs;
    int mNumServiceProcs;
    volatile boolean mOnBattery;
    String mOrigDebugApp;
    boolean mOrigWaitForDebugger;
    PackageManagerInternal mPackageManagerInt;
    final ArrayList<PendingAssistExtras> mPendingAssistExtras;
    final ArrayList<ProcessChangeItem> mPendingProcessChanges;
    final ArrayList<ProcessRecord> mPendingPssProcesses;
    final ArrayList<ChangeItem> mPendingUidChanges;
    private PerfHub mPerfHub;
    boolean mPersistentReady;
    final ArrayList<ProcessRecord> mPersistentStartingProcesses;
    final SparseArray<ProcessRecord> mPidsSelfLocked;
    ProcessRecord mPreviousProcess;
    long mPreviousProcessVisibleTime;
    final AtomicBoolean mProcessCpuMutexFree;
    final Thread mProcessCpuThread;
    final ProcessCpuTracker mProcessCpuTracker;
    final ProcessMap<Long> mProcessCrashTimes;
    int mProcessLimit;
    int mProcessLimitOverride;
    protected final ProcessList mProcessList;
    protected final ProcessMap<ProcessRecord> mProcessNames;
    final RemoteCallbackList<IProcessObserver> mProcessObservers;
    private final long[] mProcessStateStatsLongs;
    final ProcessStatsService mProcessStats;
    final ArrayList<ProcessRecord> mProcessesOnHold;
    volatile boolean mProcessesReady;
    final ArrayList<ProcessRecord> mProcessesToGc;
    String mProfileApp;
    ParcelFileDescriptor mProfileFd;
    String mProfileFile;
    ProcessRecord mProfileProc;
    int mProfileType;
    final ProviderMap mProviderMap;
    final ProviderMap mProviderMapForClone;
    final IntentResolver<BroadcastFilter, BroadcastFilter> mReceiverResolver;
    final RecentTasks mRecentTasks;
    final HashMap<IBinder, ReceiverList> mRegisteredReceivers;
    final ArrayList<ProcessRecord> mRemovedProcesses;
    private IVoiceInteractionSession mRunningVoice;
    boolean mSafeMode;
    int mSamplingInterval;
    final ActiveServices mServices;
    private boolean mShowDialogs = true;
    boolean mShuttingDown;
    final ArrayList<SleepToken> mSleepTokens;
    private boolean mSleeping;
    final ActivityStackSupervisor mStackSupervisor;
    final SparseArray<ArrayMap<String, ArrayList<Intent>>> mStickyBroadcasts;
    private final StringBuilder mStrictModeBuffer;
    final StringBuilder mStringBuilder;
    private String[] mSupportedSystemLocales;
    boolean mSupportsFreeformWindowManagement;
    boolean mSupportsLeanbackOnly;
    boolean mSupportsMultiWindow;
    boolean mSupportsPictureInPicture;
    boolean mSuppressResizeConfigChanges;
    private Dialog mSwitchUserDlg;
    volatile boolean mSystemReady;
    SystemServiceManager mSystemServiceManager;
    final ActivityThread mSystemThread;
    private final RemoteCallbackList<ITaskStackListener> mTaskStackListeners = new RemoteCallbackList();
    boolean mTestPssMode;
    int mThumbnailHeight;
    int mThumbnailWidth;
    final long[] mTmpLong;
    String mTopAction;
    ComponentName mTopComponent;
    String mTopData;
    int mTopProcessState;
    String mTrackAllocationApp;
    boolean mTrackingAssociations;
    final UiHandler mUiHandler;
    final RemoteCallbackList<IUidObserver> mUidObservers;
    UnsupportedDisplaySizeDialog mUnsupportedDisplaySizeDialog;
    final UpdateLock mUpdateLock;
    UsageStatsManagerInternal mUsageStatsService;
    private boolean mUseFifoUiScheduling = false;
    final UserController mUserController;
    private boolean mUserIsMonkey;
    private boolean mUserStateInitializing;
    final SparseArray<UidRecord> mValidateUids;
    private int mViSessionId;
    WakeLock mVoiceWakeLock;
    boolean mWaitForDebugger;
    private int mWakefulness;
    WindowManagerService mWindowManager;
    private volatile int mWtfClusterCount;
    private volatile long mWtfClusterStart;

    abstract class ForegroundToken implements DeathRecipient {
        int pid;
        IBinder token;

        ForegroundToken() {
        }
    }

    private final class AppDeathRecipient implements DeathRecipient {
        final ProcessRecord mApp;
        final IApplicationThread mAppThread;
        final int mPid;

        AppDeathRecipient(ProcessRecord app, int pid, IApplicationThread thread) {
            if (ActivityManagerDebugConfig.DEBUG_ALL) {
                Slog.v(ActivityManagerService.TAG, "New death recipient " + this + " for thread " + thread.asBinder());
            }
            this.mApp = app;
            this.mPid = pid;
            this.mAppThread = thread;
        }

        public void binderDied() {
            AppDiedInfo appDiedInfo;
            boolean isKilledByAm = this.mApp.killedByAm;
            boolean isForgroundActivities = this.mApp.foregroundActivities;
            if (ActivityManagerDebugConfig.DEBUG_ALL) {
                Slog.v(ActivityManagerService.TAG, "Death received in " + this + " for thread " + this.mAppThread.asBinder());
            }
            synchronized (ActivityManagerService.this) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityManagerService.this.appDiedLocked(this.mApp, this.mPid, this.mAppThread, true);
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            if (isKilledByAm || isForgroundActivities) {
                appDiedInfo = new AppDiedInfo(this.mApp.userId, this.mApp.processName, -1, "BinderDiedElse");
            } else {
                appDiedInfo = new AppDiedInfo(this.mApp.userId, this.mApp.processName, -1, "BinderDiedLMK");
            }
            ActivityManagerService.this.reportAppDiedMsg(appDiedInfo);
        }
    }

    class AppTaskImpl extends Stub {
        private int mCallingUid;
        private int mTaskId;

        public AppTaskImpl(int taskId, int callingUid) {
            this.mTaskId = taskId;
            this.mCallingUid = callingUid;
        }

        private void checkCaller() {
            if (this.mCallingUid != Binder.getCallingUid()) {
                throw new SecurityException("Caller " + this.mCallingUid + " does not match caller of getAppTasks(): " + Binder.getCallingUid());
            }
        }

        public void finishAndRemoveTask() {
            long origId;
            checkCaller();
            synchronized (ActivityManagerService.this) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    origId = Binder.clearCallingIdentity();
                    if (ActivityManagerService.this.removeTaskByIdLocked(this.mTaskId, false, true)) {
                        Binder.restoreCallingIdentity(origId);
                    } else {
                        throw new IllegalArgumentException("Unable to find task ID " + this.mTaskId);
                    }
                } catch (Throwable th) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }

        public RecentTaskInfo getTaskInfo() {
            RecentTaskInfo -wrap0;
            checkCaller();
            synchronized (ActivityManagerService.this) {
                long origId;
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    origId = Binder.clearCallingIdentity();
                    TaskRecord tr = ActivityManagerService.this.mStackSupervisor.anyTaskForIdLocked(this.mTaskId);
                    if (tr == null) {
                        throw new IllegalArgumentException("Unable to find task ID " + this.mTaskId);
                    }
                    -wrap0 = ActivityManagerService.this.createRecentTaskInfoFromTaskRecord(tr);
                    Binder.restoreCallingIdentity(origId);
                } catch (Throwable th) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
            return -wrap0;
        }

        public void moveToFront() {
            checkCaller();
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this) {
                    ActivityManagerService.this.mStackSupervisor.startActivityFromRecentsInner(this.mTaskId, null);
                }
                Binder.restoreCallingIdentity(origId);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
        }

        public int startActivity(IBinder whoThread, String callingPackage, Intent intent, String resolvedType, Bundle bOptions) {
            TaskRecord tr;
            IApplicationThread appThread;
            checkCaller();
            int callingUser = UserHandle.getCallingUserId();
            synchronized (ActivityManagerService.this) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    tr = ActivityManagerService.this.mStackSupervisor.anyTaskForIdLocked(this.mTaskId);
                    if (tr == null) {
                        throw new IllegalArgumentException("Unable to find task ID " + this.mTaskId);
                    }
                    appThread = ApplicationThreadNative.asInterface(whoThread);
                    if (appThread == null) {
                        throw new IllegalArgumentException("Bad app thread " + appThread);
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            return ActivityManagerService.this.mActivityStarter.startActivityMayWait(appThread, -1, callingPackage, intent, resolvedType, null, null, null, null, 0, 0, null, null, null, bOptions, false, callingUser, null, tr);
        }

        public void setExcludeFromRecents(boolean exclude) {
            checkCaller();
            synchronized (ActivityManagerService.this) {
                long origId;
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    HwSlog.d(ActivityManagerService.TAG, "setExcludeFromRecents:pkgName=" + ActivityManagerService.this.mLastAddedTaskComponent);
                    origId = Binder.clearCallingIdentity();
                    TaskRecord tr = ActivityManagerService.this.mStackSupervisor.anyTaskForIdLocked(this.mTaskId);
                    if (tr == null) {
                        throw new IllegalArgumentException("Unable to find task ID " + this.mTaskId);
                    }
                    Intent intent = tr.getBaseIntent();
                    if (exclude) {
                        intent.addFlags(8388608);
                    } else {
                        intent.setFlags(intent.getFlags() & -8388609);
                    }
                    Binder.restoreCallingIdentity(origId);
                } catch (Throwable th) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
        }
    }

    static final class Association {
        int mCount;
        int mLastState = 17;
        long mLastStateUptime;
        int mNesting;
        final String mSourceProcess;
        final int mSourceUid;
        long mStartTime;
        long[] mStateTimes = new long[18];
        final ComponentName mTargetComponent;
        final String mTargetProcess;
        final int mTargetUid;
        long mTime;

        Association(int sourceUid, String sourceProcess, int targetUid, ComponentName targetComponent, String targetProcess) {
            this.mSourceUid = sourceUid;
            this.mSourceProcess = sourceProcess;
            this.mTargetUid = targetUid;
            this.mTargetComponent = targetComponent;
            this.mTargetProcess = targetProcess;
        }
    }

    static final class BadProcessInfo {
        final String longMsg;
        final String shortMsg;
        final String stack;
        final long time;

        BadProcessInfo(long time, String shortMsg, String longMsg, String stack) {
            this.time = time;
            this.shortMsg = shortMsg;
            this.longMsg = longMsg;
            this.stack = stack;
        }
    }

    static class CpuBinder extends Binder {
        ActivityManagerService mActivityManagerService;

        CpuBinder(ActivityManagerService activityManagerService) {
            this.mActivityManagerService = activityManagerService;
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (this.mActivityManagerService.checkCallingPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump cpuinfo from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
                return;
            }
            synchronized (this.mActivityManagerService.mProcessCpuTracker) {
                pw.print(this.mActivityManagerService.mProcessCpuTracker.printCurrentLoad());
                pw.print(this.mActivityManagerService.mProcessCpuTracker.printCurrentState(SystemClock.uptimeMillis()));
            }
        }
    }

    static class DbBinder extends Binder {
        ActivityManagerService mActivityManagerService;

        DbBinder(ActivityManagerService activityManagerService) {
            this.mActivityManagerService = activityManagerService;
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (this.mActivityManagerService.checkCallingPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump dbinfo from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
            } else {
                this.mActivityManagerService.dumpDbInfo(fd, pw, args);
            }
        }
    }

    private final class FontScaleSettingObserver extends ContentObserver {
        private final Uri mFontScaleUri = System.getUriFor("font_scale");

        public FontScaleSettingObserver() {
            super(ActivityManagerService.this.mHandler);
            ActivityManagerService.this.mContext.getContentResolver().registerContentObserver(this.mFontScaleUri, false, this, -1);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (this.mFontScaleUri.equals(uri)) {
                ActivityManagerService.this.updateFontScaleIfNeeded();
            }
        }
    }

    public static class GrantUri {
        public boolean prefix;
        public final int sourceUserId;
        public final Uri uri;

        public GrantUri(int sourceUserId, Uri uri, boolean prefix) {
            this.sourceUserId = sourceUserId;
            this.uri = uri;
            this.prefix = prefix;
        }

        public int hashCode() {
            return ((((this.sourceUserId + 31) * 31) + this.uri.hashCode()) * 31) + (this.prefix ? 1231 : 1237);
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof GrantUri)) {
                return false;
            }
            GrantUri other = (GrantUri) o;
            if (this.uri.equals(other.uri) && this.sourceUserId == other.sourceUserId && this.prefix == other.prefix) {
                z = true;
            }
            return z;
        }

        public String toString() {
            String result = Integer.toString(this.sourceUserId) + " @ " + this.uri.toString();
            if (this.prefix) {
                return result + " [prefix]";
            }
            return result;
        }

        public String toSafeString() {
            String result = Integer.toString(this.sourceUserId) + " @ " + this.uri.toSafeString();
            if (this.prefix) {
                return result + " [prefix]";
            }
            return result;
        }

        public static GrantUri resolve(int defaultSourceUserHandle, Uri uri) {
            return new GrantUri(ContentProvider.getUserIdFromUri(uri, defaultSourceUserHandle), ContentProvider.getUriWithoutUserId(uri), false);
        }
    }

    static class GraphicsBinder extends Binder {
        ActivityManagerService mActivityManagerService;

        GraphicsBinder(ActivityManagerService activityManagerService) {
            this.mActivityManagerService = activityManagerService;
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (this.mActivityManagerService.checkCallingPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump gfxinfo from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
            } else {
                this.mActivityManagerService.dumpGraphicsHardwareUsage(fd, pw, args);
            }
        }
    }

    private class Identity {
        public final int pid;
        public final IBinder token;
        public final int uid;

        Identity(IBinder _token, int _pid, int _uid) {
            this.token = _token;
            this.pid = _pid;
            this.uid = _uid;
        }
    }

    class IntentFirewallInterface implements AMSInterface {
        IntentFirewallInterface() {
        }

        public int checkComponentPermission(String permission, int pid, int uid, int owningUid, boolean exported) {
            return ActivityManagerService.this.checkComponentPermission(permission, pid, uid, owningUid, exported);
        }

        public Object getAMSLock() {
            return ActivityManagerService.this;
        }
    }

    static class ItemMatcher {
        boolean all = true;
        ArrayList<ComponentName> components;
        ArrayList<Integer> objects;
        ArrayList<String> strings;

        ItemMatcher() {
        }

        void build(String name) {
            ComponentName componentName = ComponentName.unflattenFromString(name);
            if (componentName != null) {
                if (this.components == null) {
                    this.components = new ArrayList();
                }
                this.components.add(componentName);
                this.all = false;
                return;
            }
            try {
                int objectId = Integer.parseInt(name, 16);
                if (this.objects == null) {
                    this.objects = new ArrayList();
                }
                this.objects.add(Integer.valueOf(objectId));
                this.all = false;
            } catch (RuntimeException e) {
                if (this.strings == null) {
                    this.strings = new ArrayList();
                }
                this.strings.add(name);
                this.all = false;
            }
        }

        int build(String[] args, int opti) {
            while (opti < args.length) {
                String name = args[opti];
                if ("--".equals(name)) {
                    return opti + 1;
                }
                build(name);
                opti++;
            }
            return opti;
        }

        boolean match(Object object, ComponentName comp) {
            if (this.all) {
                return true;
            }
            int i;
            if (this.components != null) {
                for (i = 0; i < this.components.size(); i++) {
                    if (((ComponentName) this.components.get(i)).equals(comp)) {
                        return true;
                    }
                }
            }
            if (this.objects != null) {
                for (i = 0; i < this.objects.size(); i++) {
                    if (System.identityHashCode(object) == ((Integer) this.objects.get(i)).intValue()) {
                        return true;
                    }
                }
            }
            if (this.strings != null) {
                String flat = comp.flattenToString();
                for (i = 0; i < this.strings.size(); i++) {
                    if (flat.contains((CharSequence) this.strings.get(i))) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    final class KillHandler extends Handler {
        static final int KILL_PROCESS_GROUP_MSG = 4000;

        public KillHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KILL_PROCESS_GROUP_MSG /*4000*/:
                    Trace.traceBegin(64, "killProcessGroup");
                    Process.killProcessGroup(msg.arg1, msg.arg2);
                    Trace.traceEnd(64);
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        }
    }

    public static final class Lifecycle extends SystemService {
        private final ActivityManagerService mService;

        public Lifecycle(Context context) {
            super(context);
            IHwActivityManagerService iAMS = HwServiceFactory.getHwActivityManagerService();
            if (iAMS != null) {
                this.mService = iAMS.getInstance(context);
            } else {
                this.mService = new ActivityManagerService(context);
            }
        }

        public void onStart() {
            this.mService.start();
        }

        public ActivityManagerService getService() {
            return this.mService;
        }
    }

    private final class LocalService extends ActivityManagerInternal {
        private LocalService() {
        }

        public String checkContentProviderAccess(String authority, int userId) {
            return ActivityManagerService.this.checkContentProviderAccess(authority, userId);
        }

        public void onWakefulnessChanged(int wakefulness) {
            ActivityManagerService.this.onWakefulnessChanged(wakefulness);
        }

        public int startIsolatedProcess(String entryPoint, String[] entryPointArgs, String processName, String abiOverride, int uid, Runnable crashHandler) {
            return ActivityManagerService.this.startIsolatedProcess(entryPoint, entryPointArgs, processName, abiOverride, uid, crashHandler);
        }

        public SleepToken acquireSleepToken(String tag) {
            SleepTokenImpl token;
            Preconditions.checkNotNull(tag);
            ComponentName componentName = null;
            ComponentName componentName2 = null;
            int userId = -1;
            synchronized (ActivityManagerService.this) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if (ActivityManagerService.this.mFocusedActivity != null) {
                        componentName = ActivityManagerService.this.mFocusedActivity.requestedVrComponent;
                        componentName2 = ActivityManagerService.this.mFocusedActivity.info.getComponentName();
                        userId = ActivityManagerService.this.mFocusedActivity.userId;
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            if (componentName != null) {
                ActivityManagerService.this.applyVrMode(false, componentName, userId, componentName2, true);
            }
            synchronized (ActivityManagerService.this) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    token = new SleepTokenImpl(tag);
                    ActivityManagerService.this.mSleepTokens.add(token);
                    ActivityManagerService.this.updateSleepIfNeededLocked();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            return token;
        }

        public ComponentName getHomeActivityForUser(int userId) {
            ComponentName componentName = null;
            synchronized (ActivityManagerService.this) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityRecord homeActivity = ActivityManagerService.this.mStackSupervisor.getHomeActivityForUser(userId);
                    if (homeActivity != null) {
                        componentName = homeActivity.realActivity;
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            return componentName;
        }

        public void onUserRemoved(int userId) {
            synchronized (ActivityManagerService.this) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityManagerService.this.onUserStoppedLocked(userId);
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void onLocalVoiceInteractionStarted(IBinder activity, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor) {
            synchronized (ActivityManagerService.this) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityManagerService.this.onLocalVoiceInteractionStartedLocked(activity, voiceSession, voiceInteractor);
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void notifyStartingWindowDrawn() {
            synchronized (ActivityManagerService.this) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityManagerService.this.mStackSupervisor.mActivityMetricsLogger.notifyStartingWindowDrawn();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void notifyAppTransitionStarting(int reason) {
            synchronized (ActivityManagerService.this) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityManagerService.this.mStackSupervisor.mActivityMetricsLogger.notifyTransitionStarting(reason);
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void notifyAppTransitionFinished() {
            synchronized (ActivityManagerService.this) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityManagerService.this.mStackSupervisor.notifyAppTransitionDone();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void notifyAppTransitionCancelled() {
            synchronized (ActivityManagerService.this) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityManagerService.this.mStackSupervisor.notifyAppTransitionDone();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public List<IBinder> getTopVisibleActivities() {
            List<IBinder> topVisibleActivities;
            synchronized (ActivityManagerService.this) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    topVisibleActivities = ActivityManagerService.this.mStackSupervisor.getTopVisibleActivities();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
            return topVisibleActivities;
        }

        public void notifyDockedStackMinimizedChanged(boolean minimized) {
            synchronized (ActivityManagerService.this) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityManagerService.this.mStackSupervisor.setDockedStackMinimized(minimized);
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void killForegroundAppsForUser(int userHandle) {
            synchronized (ActivityManagerService.this) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ArrayList<ProcessRecord> procs = new ArrayList();
                    int NP = ActivityManagerService.this.mProcessNames.getMap().size();
                    for (int ip = 0; ip < NP; ip++) {
                        SparseArray<ProcessRecord> apps = (SparseArray) ActivityManagerService.this.mProcessNames.getMap().valueAt(ip);
                        int NA = apps.size();
                        for (int ia = 0; ia < NA; ia++) {
                            ProcessRecord app = (ProcessRecord) apps.valueAt(ia);
                            if (!app.persistent) {
                                if (app.removed) {
                                    procs.add(app);
                                } else if (app.userId == userHandle && app.foregroundActivities) {
                                    app.removed = true;
                                    procs.add(app);
                                }
                            }
                        }
                    }
                    int N = procs.size();
                    for (int i = 0; i < N; i++) {
                        ActivityManagerService.this.removeProcessLocked((ProcessRecord) procs.get(i), false, true, "kill all fg");
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void setPendingIntentWhitelistDuration(IIntentSender target, long duration) {
            if (target instanceof PendingIntentRecord) {
                ((PendingIntentRecord) target).setWhitelistDuration(duration);
            } else {
                Slog.w(ActivityManagerService.TAG, "markAsSentFromNotification(): not a PendingIntentRecord: " + target);
            }
        }

        public void notifyRogSwitchStateChanged(IHwRogListener listener, boolean rogEnable, AppRogInfo rogInfo) {
            if (ActivityManagerService.this.shouldResponseForRog()) {
                ActivityManagerService.this.applyRogStateChangedForStack(listener, rogEnable, rogInfo, ActivityManagerService.this.mStackSupervisor.getFocusedStack());
            }
        }

        public void notifyRogInfoUpdated(IHwRogListener listener, AppRogInfo rogInfo) {
            if (ActivityManagerService.this.shouldResponseForRog()) {
                ActivityManagerService.this.applyRogInfoUpdatedForStack(listener, rogInfo, ActivityManagerService.this.mStackSupervisor.getFocusedStack());
            }
        }

        public int getPackageScreenCompatMode(String packageName) {
            return ActivityManagerService.this.mCompatModePackages.getPackageScreenCompatModeLocked(packageName);
        }

        public int handleUserForClone(String name, int userId) {
            return ActivityManagerService.this.handleUserForClone(name, userId);
        }
    }

    final class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            ProcessRecord r;
            Message nmsg;
            int i;
            ProcessRecord app;
            String text;
            INotificationManager inm;
            ActivityRecord r2;
            int uid;
            int userId;
            ProcessRecord proc;
            switch (msg.what) {
                case 4:
                    System.putConfigurationForUser(ActivityManagerService.this.mContext.getContentResolver(), (Configuration) msg.obj, msg.arg1);
                    break;
                case 5:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityManagerService.this.performAppGcsIfAppropriateLocked();
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 12:
                    if (!ActivityManagerService.this.mDidDexOpt) {
                        ActivityManagerService.this.mServices.serviceTimeout((ProcessRecord) msg.obj);
                        break;
                    }
                    ActivityManagerService.this.mDidDexOpt = false;
                    nmsg = ActivityManagerService.this.mHandler.obtainMessage(12);
                    nmsg.obj = msg.obj;
                    ActivityManagerService.this.mHandler.sendMessageDelayed(nmsg, (long) ActiveServices.SERVICE_TIMEOUT);
                    return;
                case 13:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            for (i = ActivityManagerService.this.mLruProcesses.size() - 1; i >= 0; i--) {
                                r = (ProcessRecord) ActivityManagerService.this.mLruProcesses.get(i);
                                if (r.thread != null) {
                                    r.thread.updateTimeZone();
                                }
                            }
                        } catch (RemoteException e) {
                            Slog.w(ActivityManagerService.TAG, "Failed to update time zone for: " + r.info.processName);
                        } catch (Throwable th) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    break;
                case 20:
                    if (!ActivityManagerService.this.mDidDexOpt) {
                        app = msg.obj;
                        synchronized (ActivityManagerService.this) {
                            try {
                                ActivityManagerService.boostPriorityForLockedSection();
                                ActivityManagerService.this.processStartTimedOutLocked(app);
                            } finally {
                                ActivityManagerService.resetPriorityAfterLockedSection();
                            }
                        }
                        break;
                    }
                    ActivityManagerService.this.mDidDexOpt = false;
                    nmsg = ActivityManagerService.this.mHandler.obtainMessage(20);
                    nmsg.obj = msg.obj;
                    ActivityManagerService.this.mHandler.sendMessageDelayed(nmsg, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
                    return;
                case 21:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityManagerService.this.mActivityStarter.doPendingActivityLaunchesLocked(true);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 22:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            Bundle bundle = msg.obj;
                            ActivityManagerService.this.forceStopPackageLocked(bundle.getString(AbsLocationManagerService.DEL_PKG), msg.arg1, false, false, true, false, false, msg.arg2, bundle.getString(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY));
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 23:
                    ((PendingIntentRecord) msg.obj).completeFinalize();
                    break;
                case 24:
                    INotificationManager inm2 = NotificationManager.getService();
                    if (inm2 != null) {
                        ActivityRecord root = msg.obj;
                        ProcessRecord process = root.app;
                        if (process != null) {
                            try {
                                text = ActivityManagerService.this.mContext.getString(17040293, new Object[]{ActivityManagerService.this.mContext.createPackageContext(process.info.packageName, 0).getApplicationInfo().loadLabel(ActivityManagerService.this.mContext.createPackageContext(process.info.packageName, 0).getPackageManager())});
                                try {
                                    inm2.enqueueNotificationWithTag("android", "android", null, 17040293, new Builder(ActivityManagerService.this.mContext.createPackageContext(process.info.packageName, 0)).setSmallIcon(17303219).setWhen(0).setOngoing(true).setTicker(text).setColor(ActivityManagerService.this.mContext.getColor(17170519)).setContentTitle(text).setContentText(ActivityManagerService.this.mContext.getText(17040294)).setContentIntent(PendingIntent.getActivityAsUser(ActivityManagerService.this.mContext, 0, root.intent, 268435456, null, new UserHandle(root.userId))).build(), new int[1], root.userId);
                                    break;
                                } catch (Throwable e2) {
                                    Slog.w(ActivityManagerService.TAG, "Error showing notification for heavy-weight app", e2);
                                    break;
                                } catch (RemoteException e3) {
                                    break;
                                }
                            } catch (Throwable e4) {
                                Slog.w(ActivityManagerService.TAG, "Unable to create context for heavy notification", e4);
                                break;
                            }
                        }
                        return;
                    }
                    return;
                case 25:
                    inm = NotificationManager.getService();
                    if (inm != null) {
                        try {
                            inm.cancelNotificationWithTag("android", null, 17040293, msg.arg1);
                            break;
                        } catch (Throwable e22) {
                            Slog.w(ActivityManagerService.TAG, "Error canceling notification for service", e22);
                            break;
                        } catch (RemoteException e5) {
                            break;
                        }
                    }
                    return;
                case 27:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityManagerService.this.checkExcessivePowerUsageLocked(true);
                            removeMessages(27);
                            sendMessageDelayed(obtainMessage(27), (long) ActivityManagerService.POWER_CHECK_DELAY);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 28:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            for (i = ActivityManagerService.this.mLruProcesses.size() - 1; i >= 0; i--) {
                                r = (ProcessRecord) ActivityManagerService.this.mLruProcesses.get(i);
                                if (r.thread != null) {
                                    r.thread.clearDnsCache();
                                }
                            }
                        } catch (RemoteException e6) {
                            Slog.w(ActivityManagerService.TAG, "Failed to clear dns cache for: " + r.info.processName);
                        } catch (Throwable th2) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    break;
                case 29:
                    ProxyInfo proxy = msg.obj;
                    String host = "";
                    String port = "";
                    String exclList = "";
                    Uri pacFileUrl = Uri.EMPTY;
                    if (proxy != null) {
                        host = proxy.getHost();
                        port = Integer.toString(proxy.getPort());
                        exclList = proxy.getExclusionListAsString();
                        pacFileUrl = proxy.getPacFileUrl();
                    }
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            for (i = ActivityManagerService.this.mLruProcesses.size() - 1; i >= 0; i--) {
                                r = (ProcessRecord) ActivityManagerService.this.mLruProcesses.get(i);
                                if (r.thread != null) {
                                    r.thread.setHttpProxy(host, port, exclList, pacFileUrl);
                                }
                            }
                        } catch (RemoteException e7) {
                            Slog.w(ActivityManagerService.TAG, "Failed to update http proxy for: " + r.info.processName);
                        } catch (Throwable th3) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    break;
                case 33:
                    final ArrayList<ProcessMemInfo> arrayList = msg.obj;
                    new Thread() {
                        public void run() {
                            ActivityManagerService.this.reportMemUsage(arrayList);
                        }
                    }.start();
                    break;
                case 34:
                    ActivityManagerService.this.mUserController.dispatchUserSwitch((UserState) msg.obj, msg.arg1, msg.arg2);
                    break;
                case 35:
                    ActivityManagerService.this.mUserController.continueUserSwitch((UserState) msg.obj, msg.arg1, msg.arg2);
                    break;
                case 36:
                    ActivityManagerService.this.mUserController.timeoutUserSwitch((UserState) msg.obj, msg.arg1, msg.arg2);
                    break;
                case 37:
                    boolean nextState = msg.arg1 != 0;
                    if (ActivityManagerService.this.mUpdateLock.isHeld() != nextState) {
                        if (ActivityManagerDebugConfig.DEBUG_IMMERSIVE) {
                            Slog.d(ActivityManagerService.TAG_IMMERSIVE, "Applying new update lock state '" + nextState + "' for " + ((ActivityRecord) msg.obj));
                        }
                        if (!nextState) {
                            ActivityManagerService.this.mUpdateLock.release();
                            break;
                        } else {
                            ActivityManagerService.this.mUpdateLock.acquire();
                            break;
                        }
                    }
                    break;
                case 38:
                    ActivityManagerService.this.writeGrantedUriPermissions();
                    break;
                case 39:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityManagerService.this.requestPssAllProcsLocked(SystemClock.uptimeMillis(), true, false);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 40:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityManagerService.this.mUserController.startProfilesLocked();
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 41:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            for (i = ActivityManagerService.this.mLruProcesses.size() - 1; i >= 0; i--) {
                                r = (ProcessRecord) ActivityManagerService.this.mLruProcesses.get(i);
                                if (r.thread != null) {
                                    r.thread.updateTimePrefs(msg.arg1 != 0);
                                }
                            }
                        } catch (RemoteException e8) {
                            Slog.w(ActivityManagerService.TAG, "Failed to update preferences for: " + r.info.processName);
                        } catch (Throwable th4) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    break;
                case 42:
                    ActivityManagerService.this.mBatteryStatsService.noteEvent(32775, Integer.toString(msg.arg1), msg.arg1);
                    ActivityManagerService.this.mSystemServiceManager.startUser(msg.arg1);
                    break;
                case 43:
                    ActivityManagerService.this.mBatteryStatsService.noteEvent(16392, Integer.toString(msg.arg2), msg.arg2);
                    ActivityManagerService.this.mBatteryStatsService.noteEvent(32776, Integer.toString(msg.arg1), msg.arg1);
                    ActivityManagerService.this.mSystemServiceManager.switchUser(msg.arg1);
                    break;
                case 44:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            r2 = ActivityRecord.forTokenLocked((IBinder) msg.obj);
                            if (!(r2 == null || r2.app == null || r2.app.thread == null)) {
                                try {
                                    r2.app.thread.scheduleEnterAnimationComplete(r2.appToken);
                                } catch (RemoteException e9) {
                                }
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case ActivityManagerService.FINISH_BOOTING_MSG /*45*/:
                    if (msg.arg1 != 0) {
                        Trace.traceBegin(64, "FinishBooting");
                        ActivityManagerService.this.finishBooting();
                        Trace.traceEnd(64);
                    }
                    if (msg.arg2 != 0) {
                        ActivityManagerService.this.enableScreenAfterBoot();
                        break;
                    }
                    break;
                case 47:
                    try {
                        Locale l = msg.obj;
                        IMountService mountService = IMountService.Stub.asInterface(ServiceManager.getService("mount"));
                        Log.d(ActivityManagerService.TAG, "Storing locale " + l.toLanguageTag() + " for decryption UI");
                        mountService.setField("SystemLocale", l.toLanguageTag());
                        break;
                    } catch (Throwable e10) {
                        Log.e(ActivityManagerService.TAG, "Error storing locale for decryption UI", e10);
                        break;
                    }
                case 49:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            for (i = ActivityManagerService.this.mTaskStackListeners.beginBroadcast() - 1; i >= 0; i--) {
                                try {
                                    ((ITaskStackListener) ActivityManagerService.this.mTaskStackListeners.getBroadcastItem(i)).onTaskStackChanged();
                                } catch (RemoteException e11) {
                                }
                            }
                            ActivityManagerService.this.mTaskStackListeners.finishBroadcast();
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 50:
                    uid = msg.arg1;
                    byte[] firstPacket = (byte[]) msg.obj;
                    synchronized (ActivityManagerService.this.mPidsSelfLocked) {
                        for (i = 0; i < ActivityManagerService.this.mPidsSelfLocked.size(); i++) {
                            ProcessRecord p = (ProcessRecord) ActivityManagerService.this.mPidsSelfLocked.valueAt(i);
                            if (p.uid == uid && p.thread != null) {
                                try {
                                    p.thread.notifyCleartextNetwork(firstPacket);
                                } catch (RemoteException e12) {
                                }
                            }
                        }
                    }
                    break;
                case 51:
                    String procName;
                    long memLimit;
                    String str;
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            procName = ActivityManagerService.this.mMemWatchDumpProcName;
                            uid = ActivityManagerService.this.mMemWatchDumpUid;
                            Pair<Long, String> val = (Pair) ActivityManagerService.this.mMemWatchProcesses.get(procName, uid);
                            if (val == null) {
                                val = (Pair) ActivityManagerService.this.mMemWatchProcesses.get(procName, 0);
                            }
                            if (val != null) {
                                memLimit = ((Long) val.first).longValue();
                                str = (String) val.second;
                            } else {
                                memLimit = 0;
                                str = null;
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    if (procName != null) {
                        if (ActivityManagerDebugConfig.DEBUG_PSS) {
                            Slog.d(ActivityManagerService.TAG_PSS, "Showing dump heap notification from " + procName + "/" + uid);
                        }
                        inm = NotificationManager.getService();
                        if (inm != null) {
                            text = ActivityManagerService.this.mContext.getString(17040301, new Object[]{procName});
                            Intent deleteIntent = new Intent();
                            deleteIntent.setAction("com.android.server.am.DELETE_DUMPHEAP");
                            Intent intent = new Intent();
                            intent.setClassName("android", DumpHeapActivity.class.getName());
                            intent.putExtra("process", procName);
                            intent.putExtra("size", memLimit);
                            if (str != null) {
                                intent.putExtra("direct_launch", str);
                            }
                            userId = UserHandle.getUserId(uid);
                            try {
                                inm.enqueueNotificationWithTag("android", "android", null, 17040301, new Builder(ActivityManagerService.this.mContext).setSmallIcon(17303219).setWhen(0).setOngoing(true).setAutoCancel(true).setTicker(text).setColor(ActivityManagerService.this.mContext.getColor(17170519)).setContentTitle(text).setContentText(ActivityManagerService.this.mContext.getText(17040302)).setContentIntent(PendingIntent.getActivityAsUser(ActivityManagerService.this.mContext, 0, intent, 268435456, null, new UserHandle(userId))).setDeleteIntent(PendingIntent.getBroadcastAsUser(ActivityManagerService.this.mContext, 0, deleteIntent, 0, UserHandle.SYSTEM)).build(), new int[1], userId);
                                break;
                            } catch (Throwable e222) {
                                Slog.w(ActivityManagerService.TAG, "Error showing notification for dump heap", e222);
                                break;
                            } catch (RemoteException e13) {
                                break;
                            }
                        }
                        return;
                    }
                    return;
                case 52:
                    ActivityManagerService.this.revokeUriPermission(ActivityThread.currentActivityThread().getApplicationThread(), DumpHeapActivity.JAVA_URI, 3, UserHandle.myUserId());
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityManagerService.this.mMemWatchDumpFile = null;
                            ActivityManagerService.this.mMemWatchDumpProcName = null;
                            ActivityManagerService.this.mMemWatchDumpPid = -1;
                            ActivityManagerService.this.mMemWatchDumpUid = -1;
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 53:
                    ActivityManagerService.this.mUserController.dispatchForegroundProfileChanged(msg.arg1);
                    break;
                case 55:
                    msg.obj.deliverResult(ActivityManagerService.this.mContext);
                    break;
                case 56:
                    ActivityManagerService.this.mUserController.dispatchUserSwitchComplete(msg.arg1);
                    break;
                case ActivityManagerService.SHUTDOWN_UI_AUTOMATION_CONNECTION_MSG /*57*/:
                    try {
                        msg.obj.shutdown();
                    } catch (RemoteException e14) {
                        Slog.w(ActivityManagerService.TAG, "Error shutting down UiAutomationConnection");
                    }
                    ActivityManagerService.this.mUserIsMonkey = false;
                    break;
                case ActivityManagerService.APP_BOOST_DEACTIVATE_MSG /*58*/:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            if (ActivityManagerService.this.mIsBoosted) {
                                if (ActivityManagerService.this.mBoostStartTime < SystemClock.uptimeMillis() - 2500) {
                                    ActivityManagerService.nativeMigrateFromBoost();
                                    ActivityManagerService.this.mIsBoosted = false;
                                    ActivityManagerService.this.mBoostStartTime = 0;
                                } else {
                                    ActivityManagerService.this.mHandler.sendMessageDelayed(ActivityManagerService.this.mHandler.obtainMessage(ActivityManagerService.APP_BOOST_DEACTIVATE_MSG), 2500);
                                }
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case ActivityManagerService.CONTENT_PROVIDER_PUBLISH_TIMEOUT_MSG /*59*/:
                    app = (ProcessRecord) msg.obj;
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityManagerService.this.processContentProviderPublishTimedOutLocked(app);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 60:
                    ActivityManagerService.this.idleUids();
                    break;
                case ActivityManagerService.SYSTEM_USER_UNLOCK_MSG /*61*/:
                    userId = msg.arg1;
                    ActivityManagerService.this.mSystemServiceManager.unlockUser(userId);
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityManagerService.this.mRecentTasks.loadUserRecentsLocked(userId);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    if (userId == 0) {
                        ActivityManagerService.this.startPersistentApps(DumpState.DUMP_DOMAIN_PREFERRED);
                    }
                    ActivityManagerService.this.installEncryptionUnawareProviders(userId);
                    ActivityManagerService.this.mUserController.finishUserUnlocked((UserState) msg.obj);
                    break;
                case ActivityManagerService.LOG_STACK_STATE /*62*/:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityManagerService.this.mStackSupervisor.logStackState();
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case ActivityManagerService.VR_MODE_CHANGE_MSG /*63*/:
                    boolean vrMode;
                    ComponentName requestedPackage;
                    ComponentName callingPackage;
                    VrManagerInternal vrService = (VrManagerInternal) LocalServices.getService(VrManagerInternal.class);
                    r2 = (ActivityRecord) msg.obj;
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            vrMode = r2.requestedVrComponent != null;
                            requestedPackage = r2.requestedVrComponent;
                            userId = r2.userId;
                            callingPackage = r2.info.getComponentName();
                            if (ActivityManagerService.this.mInVrMode != vrMode) {
                                ActivityManagerService.this.mInVrMode = vrMode;
                                ActivityManagerService.this.mShowDialogs = ActivityManagerService.shouldShowDialogs(ActivityManagerService.this.mConfiguration, ActivityManagerService.this.mInVrMode);
                                if (r2.app != null) {
                                    proc = r2.app;
                                    if (proc.vrThreadTid > 0 && proc.curSchedGroup == 2) {
                                        if (ActivityManagerService.this.mInVrMode) {
                                            Process.setThreadScheduler(proc.vrThreadTid, 1073741825, 1);
                                        } else {
                                            Process.setThreadScheduler(proc.vrThreadTid, 0, 0);
                                        }
                                    }
                                }
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    vrService.setVrMode(vrMode, requestedPackage, userId, callingPackage);
                    break;
                case 64:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            for (i = ActivityManagerService.this.mTaskStackListeners.beginBroadcast() - 1; i >= 0; i--) {
                                try {
                                    ((ITaskStackListener) ActivityManagerService.this.mTaskStackListeners.getBroadcastItem(i)).onActivityPinned();
                                } catch (RemoteException e15) {
                                }
                            }
                            ActivityManagerService.this.mTaskStackListeners.finishBroadcast();
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 65:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            for (i = ActivityManagerService.this.mTaskStackListeners.beginBroadcast() - 1; i >= 0; i--) {
                                try {
                                    ((ITaskStackListener) ActivityManagerService.this.mTaskStackListeners.getBroadcastItem(i)).onPinnedActivityRestartAttempt();
                                } catch (RemoteException e16) {
                                }
                            }
                            ActivityManagerService.this.mTaskStackListeners.finishBroadcast();
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 66:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            for (i = ActivityManagerService.this.mTaskStackListeners.beginBroadcast() - 1; i >= 0; i--) {
                                try {
                                    ((ITaskStackListener) ActivityManagerService.this.mTaskStackListeners.getBroadcastItem(i)).onPinnedStackAnimationEnded();
                                } catch (RemoteException e17) {
                                }
                            }
                            ActivityManagerService.this.mTaskStackListeners.finishBroadcast();
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 67:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            for (i = ActivityManagerService.this.mTaskStackListeners.beginBroadcast() - 1; i >= 0; i--) {
                                try {
                                    ((ITaskStackListener) ActivityManagerService.this.mTaskStackListeners.getBroadcastItem(i)).onActivityForcedResizable((String) msg.obj, msg.arg1);
                                } catch (RemoteException e18) {
                                }
                            }
                            ActivityManagerService.this.mTaskStackListeners.finishBroadcast();
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 68:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            for (i = ActivityManagerService.this.mTaskStackListeners.beginBroadcast() - 1; i >= 0; i--) {
                                try {
                                    ((ITaskStackListener) ActivityManagerService.this.mTaskStackListeners.getBroadcastItem(i)).onActivityDismissingDockedStack();
                                } catch (RemoteException e19) {
                                }
                            }
                            ActivityManagerService.this.mTaskStackListeners.finishBroadcast();
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 69:
                    r2 = (ActivityRecord) msg.obj;
                    boolean needsVrMode = (r2 == null || r2.requestedVrComponent == null) ? false : true;
                    if (needsVrMode) {
                        ActivityManagerService.this.applyVrMode(msg.arg1 == 1, r2.requestedVrComponent, r2.userId, r2.info.getComponentName(), false);
                        break;
                    }
                    break;
                case 97:
                    if (ActivityManagerService.this.mLocalDeviceIdleController != null) {
                        ActivityManagerService.this.mLocalDeviceIdleController.addPowerSaveTempWhitelistAppDirect(msg.arg1, (long) msg.arg2, true, (String) msg.obj);
                        break;
                    }
                    break;
                case 98:
                    ActivityManagerService.this.trimServicesAfterBoot();
                    break;
                case 99:
                    proc = msg.obj;
                    if (proc != null) {
                        Utils.handleTimeOut("startservice", proc.processName, String.valueOf(proc.pid));
                        break;
                    }
                    break;
            }
        }
    }

    static class MemBinder extends Binder {
        ActivityManagerService mActivityManagerService;

        MemBinder(ActivityManagerService activityManagerService) {
            this.mActivityManagerService = activityManagerService;
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (this.mActivityManagerService.checkCallingPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump meminfo from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
                return;
            }
            this.mActivityManagerService.dumpApplicationMemoryUsage(fd, pw, "  ", args, false, null);
        }
    }

    static final class MemItem {
        final boolean hasActivities;
        final int id;
        final boolean isProc = false;
        final String label;
        final long pss;
        final String shortLabel;
        ArrayList<MemItem> subitems;
        final long swapPss;

        public MemItem(String _label, String _shortLabel, long _pss, long _swapPss, int _id, boolean _hasActivities) {
            this.label = _label;
            this.shortLabel = _shortLabel;
            this.pss = _pss;
            this.swapPss = _swapPss;
            this.id = _id;
            this.hasActivities = _hasActivities;
        }

        public MemItem(String _label, String _shortLabel, long _pss, long _swapPss, int _id) {
            this.label = _label;
            this.shortLabel = _shortLabel;
            this.pss = _pss;
            this.swapPss = _swapPss;
            this.id = _id;
            this.hasActivities = false;
        }
    }

    static class NeededUriGrants extends ArrayList<GrantUri> {
        final int flags;
        final String targetPkg;
        final int targetUid;

        NeededUriGrants(String targetPkg, int targetUid, int flags) {
            this.targetPkg = targetPkg;
            this.targetUid = targetUid;
            this.flags = flags;
        }
    }

    public class PendingAssistExtras extends Binder implements Runnable {
        public final ActivityRecord activity;
        public AssistContent content = null;
        public final Bundle extras;
        public boolean haveResult = false;
        public final String hint;
        public final Intent intent;
        public final IResultReceiver receiver;
        public Bundle receiverExtras;
        public Bundle result = null;
        public AssistStructure structure = null;
        public final int userHandle;

        public PendingAssistExtras(ActivityRecord _activity, Bundle _extras, Intent _intent, String _hint, IResultReceiver _receiver, Bundle _receiverExtras, int _userHandle) {
            this.activity = _activity;
            this.extras = _extras;
            this.intent = _intent;
            this.hint = _hint;
            this.receiver = _receiver;
            this.receiverExtras = _receiverExtras;
            this.userHandle = _userHandle;
        }

        public void run() {
            Slog.w(ActivityManagerService.TAG, "getAssistContextExtras failed: timeout retrieving from " + this.activity);
            synchronized (this) {
                this.haveResult = true;
                notifyAll();
            }
            ActivityManagerService.this.pendingAssistExtrasTimedOut(this);
        }
    }

    static class PermissionController extends IPermissionController.Stub {
        ActivityManagerService mActivityManagerService;

        PermissionController(ActivityManagerService activityManagerService) {
            this.mActivityManagerService = activityManagerService;
        }

        public boolean checkPermission(String permission, int pid, int uid) {
            return this.mActivityManagerService.checkPermission(permission, pid, uid) == 0;
        }

        public String[] getPackagesForUid(int uid) {
            return this.mActivityManagerService.mContext.getPackageManager().getPackagesForUid(uid);
        }

        public boolean isRuntimePermission(String permission) {
            boolean z = true;
            try {
                if (this.mActivityManagerService.mContext.getPackageManager().getPermissionInfo(permission, 0).protectionLevel != 1) {
                    z = false;
                }
                return z;
            } catch (NameNotFoundException nnfe) {
                Slog.e(ActivityManagerService.TAG, "No such permission: " + permission, nnfe);
                return false;
            }
        }
    }

    private static final class PriorityState {
        private int prevPriority;
        private int regionCounter;

        private PriorityState() {
            this.regionCounter = 0;
            this.prevPriority = Integer.MIN_VALUE;
        }
    }

    static final class ProcessChangeItem {
        static final int CHANGE_ACTIVITIES = 1;
        static final int CHANGE_PROCESS_STATE = 2;
        int changes;
        boolean foregroundActivities;
        int pid;
        int processState;
        int uid;

        ProcessChangeItem() {
        }
    }

    static class ProcessInfoService extends IProcessInfoService.Stub {
        final ActivityManagerService mActivityManagerService;

        ProcessInfoService(ActivityManagerService activityManagerService) {
            this.mActivityManagerService = activityManagerService;
        }

        public void getProcessStatesFromPids(int[] pids, int[] states) {
            this.mActivityManagerService.getProcessStatesAndOomScoresForPIDs(pids, states, null);
        }

        public void getProcessStatesAndOomScoresFromPids(int[] pids, int[] states, int[] scores) {
            this.mActivityManagerService.getProcessStatesAndOomScoresForPIDs(pids, states, scores);
        }
    }

    private final class SleepTokenImpl extends SleepToken {
        private final long mAcquireTime = SystemClock.uptimeMillis();
        private final String mTag;

        public SleepTokenImpl(String tag) {
            this.mTag = tag;
        }

        public void release() {
            synchronized (ActivityManagerService.this) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    if (ActivityManagerService.this.mSleepTokens.remove(this)) {
                        ActivityManagerService.this.updateSleepIfNeededLocked();
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public String toString() {
            return "{\"" + this.mTag + "\", acquire at " + TimeUtils.formatUptime(this.mAcquireTime) + "}";
        }
    }

    final class UiHandler extends Handler {
        public UiHandler() {
            super(UiThread.get().getLooper(), null, true);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            Dialog d;
            AlertDialog d2;
            ActivityRecord ar;
            switch (msg.what) {
                case 1:
                    ActivityManagerService.this.mAppErrors.handleShowAppErrorUi(msg);
                    ActivityManagerService.this.ensureBootCompleted();
                    break;
                case 2:
                    ActivityManagerService.this.mAppErrors.handleShowAnrUi(msg);
                    ActivityManagerService.this.ensureBootCompleted();
                    break;
                case 3:
                    new FactoryErrorDialog(ActivityManagerService.this.mContext, msg.getData().getCharSequence("msg")).show();
                    ActivityManagerService.this.ensureBootCompleted();
                    break;
                case 6:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ProcessRecord app = msg.obj;
                            if (msg.arg1 != 0) {
                                if (!app.waitedForDebugger) {
                                    d = new AppWaitingForDebuggerDialog(ActivityManagerService.this, ActivityManagerService.this.mContext, app);
                                    app.waitDialog = d;
                                    app.waitedForDebugger = true;
                                    d.show();
                                }
                            } else if (app.waitDialog != null) {
                                app.waitDialog.dismiss();
                                app.waitDialog = null;
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
                case 14:
                    if (ActivityManagerService.this.mShowDialogs) {
                        d2 = new BaseErrorDialog(ActivityManagerService.this.mContext);
                        d2.getWindow().setType(2010);
                        d2.setCancelable(false);
                        d2.setTitle(ActivityManagerService.this.mContext.getText(17039681));
                        d2.setMessage(ActivityManagerService.this.mContext.getText(17040816));
                        d2.setButton(-1, ActivityManagerService.this.mContext.getText(17039370), obtainMessage(48, d2));
                        d2.show();
                        break;
                    }
                    break;
                case 15:
                    if (ActivityManagerService.this.mShowDialogs) {
                        d2 = new BaseErrorDialog(ActivityManagerService.this.mContext);
                        d2.getWindow().setType(2010);
                        d2.setCancelable(false);
                        d2.setTitle(ActivityManagerService.this.mContext.getText(17039681));
                        d2.setMessage(ActivityManagerService.this.mContext.getText(17040817));
                        d2.setButton(-1, ActivityManagerService.this.mContext.getText(17039370), obtainMessage(48, d2));
                        d2.show();
                        break;
                    }
                    break;
                case 26:
                    HashMap<String, Object> data = msg.obj;
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ProcessRecord proc = (ProcessRecord) data.get("app");
                            if (proc != null) {
                                if (proc.crashDialog == null) {
                                    AppErrorResult res = (AppErrorResult) data.get("result");
                                    if (ActivityManagerService.this.mShowDialogs && !ActivityManagerService.this.mSleeping) {
                                        if (!ActivityManagerService.this.mShuttingDown) {
                                            d = new StrictModeViolationDialog(ActivityManagerService.this.mContext, ActivityManagerService.this, res, proc);
                                            d.show();
                                            proc.crashDialog = d;
                                            break;
                                        }
                                    }
                                    res.set(0);
                                    break;
                                }
                                Slog.e(ActivityManagerService.TAG, "App already has strict mode dialog: " + proc);
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                            Slog.e(ActivityManagerService.TAG, "App not found when showing strict mode dialog.");
                            break;
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                case 30:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ar = msg.obj;
                            if (ActivityManagerService.this.mCompatModeDialog != null) {
                                if (ActivityManagerService.this.mCompatModeDialog.mAppInfo.packageName.equals(ar.info.applicationInfo.packageName)) {
                                } else {
                                    ActivityManagerService.this.mCompatModeDialog.dismiss();
                                    ActivityManagerService.this.mCompatModeDialog = null;
                                }
                            }
                            if (ar != null) {
                            }
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            break;
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                case 31:
                    ActivityManagerService.this.dispatchProcessesChanged();
                    break;
                case 32:
                    ActivityManagerService.this.dispatchProcessDied(msg.arg1, msg.arg2);
                    break;
                case 46:
                    ActivityManagerService.this.mUserController.showUserSwitchDialog((Pair) msg.obj);
                    break;
                case 48:
                    ((Dialog) msg.obj).dismiss();
                    break;
                case 54:
                    ActivityManagerService.this.dispatchUidsChanged();
                    break;
                case 70:
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ar = (ActivityRecord) msg.obj;
                            if (ActivityManagerService.this.mUnsupportedDisplaySizeDialog != null) {
                                ActivityManagerService.this.mUnsupportedDisplaySizeDialog.dismiss();
                                ActivityManagerService.this.mUnsupportedDisplaySizeDialog = null;
                            }
                            if (ar != null && ActivityManagerService.this.mCompatModePackages.getPackageNotifyUnsupportedZoomLocked(ar.packageName)) {
                                ActivityManagerService.this.mUnsupportedDisplaySizeDialog = new UnsupportedDisplaySizeDialog(ActivityManagerService.this, ActivityManagerService.this.mContext, ar.info.applicationInfo);
                                ActivityManagerService.this.mUnsupportedDisplaySizeDialog.show();
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    break;
            }
        }
    }

    private static native int nativeMigrateFromBoost();

    private static native int nativeMigrateToBoost();

    public void systemReady(java.lang.Runnable r51) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:25:0x008a
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.modifyBlocksTree(BlockProcessor.java:248)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:52)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r50 = this;
        monitor-enter(r50);
        boostPriorityForLockedSection();	 Catch:{ all -> 0x0089 }
        r0 = r50;	 Catch:{ all -> 0x0089 }
        r2 = r0.mSystemReady;	 Catch:{ all -> 0x0089 }
        if (r2 == 0) goto L_0x0014;	 Catch:{ all -> 0x0089 }
    L_0x000a:
        if (r51 == 0) goto L_0x000f;	 Catch:{ all -> 0x0089 }
    L_0x000c:
        r51.run();	 Catch:{ all -> 0x0089 }
    L_0x000f:
        monitor-exit(r50);
        resetPriorityAfterLockedSection();
        return;
    L_0x0014:
        r50.initCtsDropActions();	 Catch:{ all -> 0x0089 }
        r50.initCtsDropPackages();	 Catch:{ all -> 0x0089 }
        r2 = com.android.server.DeviceIdleController.LocalService.class;	 Catch:{ all -> 0x0089 }
        r2 = com.android.server.LocalServices.getService(r2);	 Catch:{ all -> 0x0089 }
        r2 = (com.android.server.DeviceIdleController.LocalService) r2;	 Catch:{ all -> 0x0089 }
        r0 = r50;	 Catch:{ all -> 0x0089 }
        r0.mLocalDeviceIdleController = r2;	 Catch:{ all -> 0x0089 }
        r0 = r50;	 Catch:{ all -> 0x0089 }
        r2 = r0.mUserController;	 Catch:{ all -> 0x0089 }
        r2.onSystemReady();	 Catch:{ all -> 0x0089 }
        r0 = r50;	 Catch:{ all -> 0x0089 }
        r2 = r0.mRecentTasks;	 Catch:{ all -> 0x0089 }
        r2.onSystemReadyLocked();	 Catch:{ all -> 0x0089 }
        r2 = 1;	 Catch:{ all -> 0x0089 }
        r0 = r50;	 Catch:{ all -> 0x0089 }
        r0.mSystemReady = r2;	 Catch:{ all -> 0x0089 }
        r50.startPushService();	 Catch:{ all -> 0x0089 }
        r2 = "sys.super_power_save";	 Catch:{ all -> 0x0089 }
        r3 = "false";	 Catch:{ all -> 0x0089 }
        android.os.SystemProperties.set(r2, r3);	 Catch:{ all -> 0x0089 }
        monitor-exit(r50);
        resetPriorityAfterLockedSection();
        r46 = 0;
        r0 = r50;
        r3 = r0.mPidsSelfLocked;
        monitor-enter(r3);
        r0 = r50;	 Catch:{ all -> 0x00d2 }
        r2 = r0.mPidsSelfLocked;	 Catch:{ all -> 0x00d2 }
        r2 = r2.size();	 Catch:{ all -> 0x00d2 }
        r41 = r2 + -1;
        r47 = r46;
    L_0x005c:
        if (r41 < 0) goto L_0x008f;
    L_0x005e:
        r0 = r50;	 Catch:{ all -> 0x0316 }
        r2 = r0.mPidsSelfLocked;	 Catch:{ all -> 0x0316 }
        r0 = r41;	 Catch:{ all -> 0x0316 }
        r45 = r2.valueAt(r0);	 Catch:{ all -> 0x0316 }
        r45 = (com.android.server.am.ProcessRecord) r45;	 Catch:{ all -> 0x0316 }
        r0 = r45;	 Catch:{ all -> 0x0316 }
        r2 = r0.info;	 Catch:{ all -> 0x0316 }
        r0 = r50;	 Catch:{ all -> 0x0316 }
        r2 = r0.isAllowedWhileBooting(r2);	 Catch:{ all -> 0x0316 }
        if (r2 != 0) goto L_0x031f;	 Catch:{ all -> 0x0316 }
    L_0x0076:
        if (r47 != 0) goto L_0x031b;	 Catch:{ all -> 0x0316 }
    L_0x0078:
        r46 = new java.util.ArrayList;	 Catch:{ all -> 0x0316 }
        r46.<init>();	 Catch:{ all -> 0x0316 }
    L_0x007d:
        r0 = r46;	 Catch:{ all -> 0x00d2 }
        r1 = r45;	 Catch:{ all -> 0x00d2 }
        r0.add(r1);	 Catch:{ all -> 0x00d2 }
    L_0x0084:
        r41 = r41 + -1;
        r47 = r46;
        goto L_0x005c;
    L_0x0089:
        r2 = move-exception;
        monitor-exit(r50);
        resetPriorityAfterLockedSection();
        throw r2;
    L_0x008f:
        monitor-exit(r3);
        monitor-enter(r50);
        boostPriorityForLockedSection();	 Catch:{ all -> 0x02bf }
        if (r47 == 0) goto L_0x00d5;	 Catch:{ all -> 0x02bf }
    L_0x0096:
        r2 = r47.size();	 Catch:{ all -> 0x02bf }
        r41 = r2 + -1;	 Catch:{ all -> 0x02bf }
    L_0x009c:
        if (r41 < 0) goto L_0x00d5;	 Catch:{ all -> 0x02bf }
    L_0x009e:
        r0 = r47;	 Catch:{ all -> 0x02bf }
        r1 = r41;	 Catch:{ all -> 0x02bf }
        r45 = r0.get(r1);	 Catch:{ all -> 0x02bf }
        r45 = (com.android.server.am.ProcessRecord) r45;	 Catch:{ all -> 0x02bf }
        r2 = TAG;	 Catch:{ all -> 0x02bf }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x02bf }
        r3.<init>();	 Catch:{ all -> 0x02bf }
        r4 = "Removing system update proc: ";	 Catch:{ all -> 0x02bf }
        r3 = r3.append(r4);	 Catch:{ all -> 0x02bf }
        r0 = r45;	 Catch:{ all -> 0x02bf }
        r3 = r3.append(r0);	 Catch:{ all -> 0x02bf }
        r3 = r3.toString();	 Catch:{ all -> 0x02bf }
        android.util.Slog.i(r2, r3);	 Catch:{ all -> 0x02bf }
        r2 = "system update done";	 Catch:{ all -> 0x02bf }
        r3 = 1;	 Catch:{ all -> 0x02bf }
        r4 = 0;	 Catch:{ all -> 0x02bf }
        r0 = r50;	 Catch:{ all -> 0x02bf }
        r1 = r45;	 Catch:{ all -> 0x02bf }
        r0.removeProcessLocked(r1, r3, r4, r2);	 Catch:{ all -> 0x02bf }
        r41 = r41 + -1;
        goto L_0x009c;
    L_0x00d2:
        r2 = move-exception;
    L_0x00d3:
        monitor-exit(r3);
        throw r2;
    L_0x00d5:
        r2 = 1;
        r0 = r50;	 Catch:{ all -> 0x02bf }
        r0.mProcessesReady = r2;	 Catch:{ all -> 0x02bf }
        monitor-exit(r50);
        resetPriorityAfterLockedSection();
        r2 = "JL_BOOT_PROGRESS_AMS_READY";
        r3 = 33;
        android.util.Jlog.d(r3, r2);
        r2 = TAG;
        r3 = "System now ready";
        android.util.Slog.i(r2, r3);
        r2 = android.os.SystemClock.uptimeMillis();
        r4 = 3040; // 0xbe0 float:4.26E-42 double:1.502E-320;
        android.util.EventLog.writeEvent(r4, r2);
        monitor-enter(r50);
        boostPriorityForLockedSection();	 Catch:{ all -> 0x02e7 }
        r0 = r50;	 Catch:{ all -> 0x02e7 }
        r2 = r0.mFactoryTest;	 Catch:{ all -> 0x02e7 }
        r3 = 1;	 Catch:{ all -> 0x02e7 }
        if (r2 != r3) goto L_0x017c;	 Catch:{ all -> 0x02e7 }
    L_0x0102:
        r0 = r50;	 Catch:{ all -> 0x02e7 }
        r2 = r0.mContext;	 Catch:{ all -> 0x02e7 }
        r2 = r2.getPackageManager();	 Catch:{ all -> 0x02e7 }
        r3 = new android.content.Intent;	 Catch:{ all -> 0x02e7 }
        r4 = "android.intent.action.FACTORY_TEST";	 Catch:{ all -> 0x02e7 }
        r3.<init>(r4);	 Catch:{ all -> 0x02e7 }
        r4 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;	 Catch:{ all -> 0x02e7 }
        r48 = r2.resolveActivity(r3, r4);	 Catch:{ all -> 0x02e7 }
        r40 = 0;	 Catch:{ all -> 0x02e7 }
        if (r48 == 0) goto L_0x02d6;	 Catch:{ all -> 0x02e7 }
    L_0x011c:
        r0 = r48;	 Catch:{ all -> 0x02e7 }
        r0 = r0.activityInfo;	 Catch:{ all -> 0x02e7 }
        r36 = r0;	 Catch:{ all -> 0x02e7 }
        r0 = r36;	 Catch:{ all -> 0x02e7 }
        r0 = r0.applicationInfo;	 Catch:{ all -> 0x02e7 }
        r37 = r0;	 Catch:{ all -> 0x02e7 }
        r0 = r37;	 Catch:{ all -> 0x02e7 }
        r2 = r0.flags;	 Catch:{ all -> 0x02e7 }
        r2 = r2 & 1;	 Catch:{ all -> 0x02e7 }
        if (r2 == 0) goto L_0x02c5;	 Catch:{ all -> 0x02e7 }
    L_0x0130:
        r2 = "android.intent.action.FACTORY_TEST";	 Catch:{ all -> 0x02e7 }
        r0 = r50;	 Catch:{ all -> 0x02e7 }
        r0.mTopAction = r2;	 Catch:{ all -> 0x02e7 }
        r2 = 0;	 Catch:{ all -> 0x02e7 }
        r0 = r50;	 Catch:{ all -> 0x02e7 }
        r0.mTopData = r2;	 Catch:{ all -> 0x02e7 }
        r2 = new android.content.ComponentName;	 Catch:{ all -> 0x02e7 }
        r0 = r37;	 Catch:{ all -> 0x02e7 }
        r3 = r0.packageName;	 Catch:{ all -> 0x02e7 }
        r0 = r36;	 Catch:{ all -> 0x02e7 }
        r4 = r0.name;	 Catch:{ all -> 0x02e7 }
        r2.<init>(r3, r4);	 Catch:{ all -> 0x02e7 }
        r0 = r50;	 Catch:{ all -> 0x02e7 }
        r0.mTopComponent = r2;	 Catch:{ all -> 0x02e7 }
    L_0x014d:
        if (r40 == 0) goto L_0x017c;	 Catch:{ all -> 0x02e7 }
    L_0x014f:
        r2 = 0;	 Catch:{ all -> 0x02e7 }
        r0 = r50;	 Catch:{ all -> 0x02e7 }
        r0.mTopAction = r2;	 Catch:{ all -> 0x02e7 }
        r2 = 0;	 Catch:{ all -> 0x02e7 }
        r0 = r50;	 Catch:{ all -> 0x02e7 }
        r0.mTopData = r2;	 Catch:{ all -> 0x02e7 }
        r2 = 0;	 Catch:{ all -> 0x02e7 }
        r0 = r50;	 Catch:{ all -> 0x02e7 }
        r0.mTopComponent = r2;	 Catch:{ all -> 0x02e7 }
        r44 = android.os.Message.obtain();	 Catch:{ all -> 0x02e7 }
        r2 = 3;	 Catch:{ all -> 0x02e7 }
        r0 = r44;	 Catch:{ all -> 0x02e7 }
        r0.what = r2;	 Catch:{ all -> 0x02e7 }
        r2 = r44.getData();	 Catch:{ all -> 0x02e7 }
        r3 = "msg";	 Catch:{ all -> 0x02e7 }
        r0 = r40;	 Catch:{ all -> 0x02e7 }
        r2.putCharSequence(r3, r0);	 Catch:{ all -> 0x02e7 }
        r0 = r50;	 Catch:{ all -> 0x02e7 }
        r2 = r0.mUiHandler;	 Catch:{ all -> 0x02e7 }
        r0 = r44;	 Catch:{ all -> 0x02e7 }
        r2.sendMessage(r0);	 Catch:{ all -> 0x02e7 }
    L_0x017c:
        monitor-exit(r50);
        resetPriorityAfterLockedSection();
        r50.retrieveSettings();
        monitor-enter(r50);
        boostPriorityForLockedSection();	 Catch:{ all -> 0x02ed }
        r0 = r50;	 Catch:{ all -> 0x02ed }
        r2 = r0.mUserController;	 Catch:{ all -> 0x02ed }
        r18 = r2.getCurrentUserIdLocked();	 Catch:{ all -> 0x02ed }
        r50.readGrantedUriPermissionsLocked();	 Catch:{ all -> 0x02ed }
        monitor-exit(r50);
        resetPriorityAfterLockedSection();
        if (r51 == 0) goto L_0x019b;
    L_0x0198:
        r51.run();
    L_0x019b:
        r0 = r50;
        r2 = r0.mBatteryStatsService;
        r3 = java.lang.Integer.toString(r18);
        r4 = 32775; // 0x8007 float:4.5928E-41 double:1.6193E-319;
        r0 = r18;
        r2.noteEvent(r4, r3, r0);
        r0 = r50;
        r2 = r0.mBatteryStatsService;
        r3 = java.lang.Integer.toString(r18);
        r4 = 32776; // 0x8008 float:4.5929E-41 double:1.61935E-319;
        r0 = r18;
        r2.noteEvent(r4, r3, r0);
        r0 = r50;
        r2 = r0.mSystemServiceManager;
        r0 = r18;
        r2.startUser(r0);
        monitor-enter(r50);
        boostPriorityForLockedSection();	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r2 = 524288; // 0x80000 float:7.34684E-40 double:2.590327E-318;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r0 = r50;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r0.startPersistentApps(r2);	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r2 = 1;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r0 = r50;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r0.mPersistentReady = r2;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r2 = 1;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r0 = r50;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r0.mBooting = r2;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r2 = android.os.UserManager.isSplitSystemUser();	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        if (r2 == 0) goto L_0x01f8;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
    L_0x01df:
        r38 = new android.content.ComponentName;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r0 = r50;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r2 = r0.mContext;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r3 = com.android.internal.app.SystemUserHomeActivity.class;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r0 = r38;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r0.<init>(r2, r3);	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r2 = android.app.AppGlobals.getPackageManager();	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r3 = 1;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r4 = 0;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r6 = 0;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r0 = r38;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r2.setComponentEnabledSetting(r0, r3, r4, r6);	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
    L_0x01f8:
        android.hwtheme.HwThemeManager.linkDataSkinDirAsUser(r18);	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r2 = "systemReady";	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r0 = r50;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r1 = r18;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r0.startHomeActivityLocked(r1, r2);	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r2 = android.app.AppGlobals.getPackageManager();	 Catch:{ RemoteException -> 0x0313 }
        r2 = r2.hasSystemUidErrors();	 Catch:{ RemoteException -> 0x0313 }
        if (r2 == 0) goto L_0x0224;	 Catch:{ RemoteException -> 0x0313 }
    L_0x020f:
        r2 = TAG;	 Catch:{ RemoteException -> 0x0313 }
        r3 = "UIDs on the system are inconsistent, you need to wipe your data partition or your device will be unstable.";	 Catch:{ RemoteException -> 0x0313 }
        android.util.Slog.e(r2, r3);	 Catch:{ RemoteException -> 0x0313 }
        r0 = r50;	 Catch:{ RemoteException -> 0x0313 }
        r2 = r0.mUiHandler;	 Catch:{ RemoteException -> 0x0313 }
        r3 = 14;	 Catch:{ RemoteException -> 0x0313 }
        r2 = r2.obtainMessage(r3);	 Catch:{ RemoteException -> 0x0313 }
        r2.sendToTarget();	 Catch:{ RemoteException -> 0x0313 }
    L_0x0224:
        r2 = android.os.Build.isBuildConsistent();	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        if (r2 != 0) goto L_0x023f;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
    L_0x022a:
        r2 = TAG;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r3 = "Build fingerprint is not consistent, warning user";	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        android.util.Slog.e(r2, r3);	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r0 = r50;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r2 = r0.mUiHandler;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r3 = 15;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r2 = r2.obtainMessage(r3);	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r2.sendToTarget();	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
    L_0x023f:
        r42 = android.os.Binder.clearCallingIdentity();	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        r5 = new android.content.Intent;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r2 = "android.intent.action.USER_STARTED";	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r5.<init>(r2);	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r2 = 1342177280; // 0x50000000 float:8.5899346E9 double:6.631236847E-315;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r5.addFlags(r2);	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r2 = "android.intent.extra.user_handle";	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r0 = r18;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r5.putExtra(r2, r0);	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r16 = MY_PID;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r3 = 0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r4 = 0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r6 = 0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r7 = 0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r8 = 0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r9 = 0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r10 = 0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r11 = 0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r12 = -1;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r13 = 0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r14 = 0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r15 = 0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r17 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r2 = r50;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r2.broadcastIntentLocked(r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18);	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r5 = new android.content.Intent;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r2 = "android.intent.action.USER_STARTING";	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r5.<init>(r2);	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r2 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r5.addFlags(r2);	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r2 = "android.intent.extra.user_handle";	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r0 = r18;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r5.putExtra(r2, r0);	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r24 = new com.android.server.am.ActivityManagerService$18;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r0 = r24;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r1 = r50;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r0.<init>();	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r2 = 1;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r0 = new java.lang.String[r2];	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r28 = r0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r2 = "android.permission.INTERACT_ACROSS_USERS";	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r3 = 0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r28[r3] = r2;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r33 = MY_PID;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r20 = 0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r21 = 0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r23 = 0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r25 = 0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r26 = 0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r27 = 0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r29 = -1;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r30 = 0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r31 = 1;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r32 = 0;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r34 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r35 = -1;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r19 = r50;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r22 = r5;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r19.broadcastIntentLocked(r20, r21, r22, r23, r24, r25, r26, r27, r28, r29, r30, r31, r32, r33, r34, r35);	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        android.os.Binder.restoreCallingIdentity(r42);
    L_0x02ba:
        monitor-exit(r50);
        resetPriorityAfterLockedSection();
        return;
    L_0x02bf:
        r2 = move-exception;
        monitor-exit(r50);
        resetPriorityAfterLockedSection();
        throw r2;
    L_0x02c5:
        r0 = r50;	 Catch:{ all -> 0x02e7 }
        r2 = r0.mContext;	 Catch:{ all -> 0x02e7 }
        r2 = r2.getResources();	 Catch:{ all -> 0x02e7 }
        r3 = 17040097; // 0x10402e1 float:2.4246636E-38 double:8.4189265E-317;	 Catch:{ all -> 0x02e7 }
        r40 = r2.getText(r3);	 Catch:{ all -> 0x02e7 }
        goto L_0x014d;	 Catch:{ all -> 0x02e7 }
    L_0x02d6:
        r0 = r50;	 Catch:{ all -> 0x02e7 }
        r2 = r0.mContext;	 Catch:{ all -> 0x02e7 }
        r2 = r2.getResources();	 Catch:{ all -> 0x02e7 }
        r3 = 17040098; // 0x10402e2 float:2.424664E-38 double:8.418927E-317;	 Catch:{ all -> 0x02e7 }
        r40 = r2.getText(r3);	 Catch:{ all -> 0x02e7 }
        goto L_0x014d;
    L_0x02e7:
        r2 = move-exception;
        monitor-exit(r50);
        resetPriorityAfterLockedSection();
        throw r2;
    L_0x02ed:
        r2 = move-exception;
        monitor-exit(r50);
        resetPriorityAfterLockedSection();
        throw r2;
    L_0x02f3:
        r39 = move-exception;
        r2 = r39.rethrowAsRuntimeException();	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        throw r2;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
    L_0x02f9:
        r2 = move-exception;
        monitor-exit(r50);
        resetPriorityAfterLockedSection();
        throw r2;
    L_0x02ff:
        r49 = move-exception;
        r2 = TAG;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r3 = "Failed sending first user broadcasts";	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        r0 = r49;	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        android.util.Slog.wtf(r2, r3, r0);	 Catch:{ Throwable -> 0x02ff, all -> 0x030e }
        android.os.Binder.restoreCallingIdentity(r42);
        goto L_0x02ba;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
    L_0x030e:
        r2 = move-exception;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        android.os.Binder.restoreCallingIdentity(r42);	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
        throw r2;	 Catch:{ RemoteException -> 0x02f3, all -> 0x02f9 }
    L_0x0313:
        r39 = move-exception;
        goto L_0x0224;
    L_0x0316:
        r2 = move-exception;
        r46 = r47;
        goto L_0x00d3;
    L_0x031b:
        r46 = r47;
        goto L_0x007d;
    L_0x031f:
        r46 = r47;
        goto L_0x0084;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ActivityManagerService.systemReady(java.lang.Runnable):void");
    }

    static {
        boolean z;
        int i;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) == 3) {
            z = true;
        } else {
            z = false;
        }
        IS_DEBUG_VERSION = z;
        if (ActivityManagerDebugConfig.DEBUG_POWER_QUICK) {
            i = 1;
        } else {
            i = 5;
        }
        WAKE_LOCK_MIN_CHECK_DURATION = (i * 60) * 1000;
        if (ActivityManagerDebugConfig.DEBUG_POWER_QUICK) {
            i = 1;
        } else {
            i = 5;
        }
        CPU_MIN_CHECK_DURATION = (i * 60) * 1000;
        if (IS_FPGA) {
            i = 30000;
        } else {
            i = CONTENT_PROVIDER_RETAIN_TIME;
        }
        BROADCAST_FG_TIMEOUT = i;
        if (IS_FPGA) {
            i = ProcessList.PSS_MIN_TIME_FROM_STATE_CHANGE;
        } else {
            i = 8000;
        }
        KEY_DISPATCHING_TIMEOUT = i;
    }

    BroadcastQueue broadcastQueueForIntent(Intent intent) {
        boolean isFg = (intent.getFlags() & 268435456) != 0;
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_BACKGROUND) {
            Slog.i(TAG_BROADCAST, "Broadcast intent " + intent + " on " + (isFg ? "foreground" : "background") + " queue");
        }
        return isFg ? this.mFgBroadcastQueue : this.mBgBroadcastQueue;
    }

    public boolean canShowErrorDialogs() {
        return (!this.mShowDialogs || this.mSleeping || this.mShuttingDown) ? false : true;
    }

    static void boostPriorityForLockedSection() {
        int tid = Process.myTid();
        int prevPriority = Process.getThreadPriority(tid);
        PriorityState state = (PriorityState) sThreadPriorityState.get();
        if (state.regionCounter == 0 && prevPriority > -2) {
            state.prevPriority = prevPriority;
            Process.setThreadPriority(tid, -2);
        }
        state.regionCounter = state.regionCounter + 1;
    }

    static void resetPriorityAfterLockedSection() {
        PriorityState state = (PriorityState) sThreadPriorityState.get();
        state.regionCounter = state.regionCounter - 1;
        if (state.regionCounter == 0 && state.prevPriority > -2) {
            Process.setThreadPriority(Process.myTid(), state.prevPriority);
        }
    }

    public void setSystemProcess() {
        try {
            ServiceManager.addService("activity", this, true);
            ServiceManager.addService("procstats", this.mProcessStats);
            ServiceManager.addService("meminfo", new MemBinder(this));
            ServiceManager.addService("gfxinfo", new GraphicsBinder(this));
            ServiceManager.addService("dbinfo", new DbBinder(this));
            ServiceManager.addService("cpuinfo", new CpuBinder(this));
            ServiceManager.addService("permission", new PermissionController(this));
            ServiceManager.addService("processinfo", new ProcessInfoService(this));
            ApplicationInfo info = this.mContext.getPackageManager().getApplicationInfo("android", 1049600);
            this.mSystemThread.installSystemApplicationInfo(info, getClass().getClassLoader());
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    ProcessRecord app = newProcessRecordLocked(info, info.processName, false, 0);
                    app.persistent = true;
                    app.pid = MY_PID;
                    app.maxAdj = -900;
                    app.makeActive(this.mSystemThread.getApplicationThread(), this.mProcessStats);
                    synchronized (this.mPidsSelfLocked) {
                        this.mPidsSelfLocked.put(app.pid, app);
                    }
                    updateLruProcessLocked(app, false, null);
                    updateOomAdjLocked();
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                }
            }
            resetPriorityAfterLockedSection();
        } catch (NameNotFoundException e) {
            throw new RuntimeException("Unable to find android system package", e);
        }
    }

    public void setWindowManager(WindowManagerService wm) {
        this.mWindowManager = wm;
        this.mStackSupervisor.setWindowManager(wm);
        this.mActivityStarter.setWindowManager(wm);
    }

    public void setAlarmManager(AlarmManagerService service) {
        this.mAlms = service;
    }

    public void setUsageStatsManager(UsageStatsManagerInternal usageStatsManager) {
        this.mUsageStatsService = usageStatsManager;
    }

    public void startObservingNativeCrashes() {
        new NativeCrashListener(this).start();
    }

    public IAppOpsService getAppOpsService() {
        return this.mAppOpsService;
    }

    public ActivityManagerService(Context systemContext) {
        boolean z;
        if (SystemProperties.getBoolean("ro.config.hw_low_ram", false)) {
            z = true;
        } else {
            z = SystemProperties.getBoolean("ro.config.hw_smart_shrink", false);
        }
        this.mIsHwLowRam = z;
        this.mBroadcastQueues = initialBroadcastQueue();
        this.mIsPerfBoostEnabled = false;
        this.mSwitchUserDlg = null;
        this.mUserStateInitializing = false;
        this.mCustAms = (HwCustActivityManagerService) HwCustUtils.createObj(HwCustActivityManagerService.class, new Object[0]);
        this.mFocusedActivity = null;
        this.mLockTaskPackages = new SparseArray();
        this.mPendingAssistExtras = new ArrayList();
        this.mProcessList = new ProcessList();
        this.mProcessNames = new ProcessMap();
        this.mIsolatedProcesses = new SparseArray();
        this.mNextIsolatedProcessUid = 0;
        this.mHeavyWeightProcess = null;
        this.mProcessCrashTimes = new ProcessMap();
        this.mBadProcesses = new ProcessMap();
        this.mBadPkgs = new ArrayMap();
        this.mPidsSelfLocked = new SparseArray();
        this.mForegroundProcesses = new SparseArray();
        this.mProcessesOnHold = new ArrayList();
        this.mPersistentStartingProcesses = new ArrayList();
        this.mRemovedProcesses = new ArrayList();
        this.mLruProcesses = new ArrayList();
        this.mLruProcessActivityStart = 0;
        this.mLruProcessServiceStart = 0;
        this.mProcessesToGc = new ArrayList();
        this.mPendingPssProcesses = new ArrayList();
        this.mBinderTransactionTrackingEnabled = false;
        this.mLastFullPssTime = SystemClock.uptimeMillis();
        this.mFullPssPending = false;
        this.mActiveUids = new SparseArray();
        this.mValidateUids = new SparseArray();
        this.mIntentSenderRecords = new HashMap();
        this.mAlreadyLoggedViolatedStacks = new HashSet();
        this.mStrictModeBuffer = new StringBuilder();
        this.mRegisteredReceivers = new HashMap();
        this.mReceiverResolver = new IntentResolver<BroadcastFilter, BroadcastFilter>() {
            protected boolean allowFilterResult(BroadcastFilter filter, List<BroadcastFilter> dest) {
                if (filter.receiverList.receiver == null) {
                    Slog.w(ActivityManagerService.TAG, "  Receiver of filter's receiverList is null; packageName = " + filter.packageName);
                    return false;
                }
                IBinder target = filter.receiverList.receiver.asBinder();
                for (int i = dest.size() - 1; i >= 0; i--) {
                    if (((BroadcastFilter) dest.get(i)).receiverList.receiver.asBinder() == target) {
                        return false;
                    }
                }
                return true;
            }

            protected BroadcastFilter newResult(BroadcastFilter filter, int match, int userId) {
                if (userId == -1 || filter.owningUserId == -1 || userId == filter.owningUserId) {
                    return (BroadcastFilter) super.newResult(filter, match, userId);
                }
                return null;
            }

            protected BroadcastFilter[] newArray(int size) {
                return new BroadcastFilter[size];
            }

            protected boolean isPackageForFilter(String packageName, BroadcastFilter filter) {
                return packageName.equals(filter.packageName);
            }
        };
        this.mStickyBroadcasts = new SparseArray();
        this.mAssociations = new SparseArray();
        this.mBackupAppName = null;
        this.mBackupTarget = null;
        this.mLaunchingProviders = new ArrayList();
        this.mGrantedUriPermissions = new SparseArray();
        this.mConfiguration = new Configuration();
        this.mConfigurationSeq = 0;
        this.mSuppressResizeConfigChanges = false;
        this.mStringBuilder = new StringBuilder(256);
        this.mTopAction = "android.intent.action.MAIN";
        this.mProcessesReady = false;
        this.mSystemReady = false;
        this.mOnBattery = false;
        this.mBooting = false;
        this.mCallFinishBooting = false;
        this.mBootAnimationComplete = false;
        this.mLaunchWarningShown = false;
        this.mCheckedForSetup = false;
        this.mSleeping = false;
        this.mTopProcessState = 2;
        this.mWakefulness = 1;
        this.mSleepTokens = new ArrayList();
        this.mLockScreenShown = 0;
        this.mShuttingDown = false;
        this.mAdjSeq = 0;
        this.mLruSeq = 0;
        this.mNumNonCachedProcs = 0;
        this.mNumCachedHiddenProcs = 0;
        this.mNumServiceProcs = 0;
        this.mNewNumAServiceProcs = 0;
        this.mNewNumServiceProcs = 0;
        this.mAllowLowerMemLevel = false;
        this.mLastMemoryLevel = 0;
        this.mLastIdleTime = SystemClock.uptimeMillis();
        this.mLowRamTimeSinceLastIdle = 0;
        this.mLowRamStartTime = 0;
        this.mCurResumedPackage = null;
        this.mCurResumedUid = -1;
        this.mForegroundPackages = new ProcessMap();
        this.mTestPssMode = false;
        this.mDebugApp = null;
        this.mWaitForDebugger = false;
        this.mDebugTransient = false;
        this.mOrigDebugApp = null;
        this.mOrigWaitForDebugger = false;
        this.mAlwaysFinishActivities = false;
        this.mLenientBackgroundCheck = false;
        this.mController = null;
        this.mControllerIsAMonkey = false;
        this.mCustomController = null;
        this.mProfileApp = null;
        this.mProfileProc = null;
        this.mSamplingInterval = 0;
        this.mAutoStopProfiler = false;
        this.mProfileType = 0;
        this.mMemWatchProcesses = new ProcessMap();
        this.mTrackAllocationApp = null;
        this.mNativeDebuggingApp = null;
        this.mTmpLong = new long[2];
        this.mProcessObservers = new RemoteCallbackList();
        this.mActiveProcessChanges = new ProcessChangeItem[5];
        this.mPendingProcessChanges = new ArrayList();
        this.mAvailProcessChanges = new ArrayList();
        this.mUidObservers = new RemoteCallbackList();
        this.mActiveUidChanges = new ChangeItem[5];
        this.mPendingUidChanges = new ArrayList();
        this.mAvailUidChanges = new ArrayList();
        this.mProcessCpuTracker = new ProcessCpuTracker(false);
        this.mLastCpuTime = new AtomicLong(0);
        this.mProcessCpuMutexFree = new AtomicBoolean(true);
        this.mLastWriteTime = 0;
        this.mUpdateLock = new UpdateLock("immersive");
        this.mBooted = false;
        this.mActivityIdle = false;
        this.mProcessLimit = ProcessList.MAX_CACHED_APPS;
        this.mProcessLimitOverride = -1;
        this.mLastMemUsageReportTime = 0;
        this.mViSessionId = 1000;
        this.mBgHandler = new Handler(BackgroundThread.getHandler().getLooper()) {
            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        long start = SystemClock.uptimeMillis();
                        MemInfoReader memInfoReader = null;
                        synchronized (ActivityManagerService.this) {
                            try {
                                ActivityManagerService.boostPriorityForLockedSection();
                                if (ActivityManagerService.this.mFullPssPending) {
                                    ActivityManagerService.this.mFullPssPending = false;
                                    memInfoReader = new MemInfoReader();
                                }
                            } finally {
                                ActivityManagerService.resetPriorityAfterLockedSection();
                            }
                        }
                        if (memInfoReader != null) {
                            ActivityManagerService.this.updateCpuStatsNow();
                            long nativeTotalPss = 0;
                            synchronized (ActivityManagerService.this.mProcessCpuTracker) {
                                int N = ActivityManagerService.this.mProcessCpuTracker.countStats();
                                for (int j = 0; j < N; j++) {
                                    Stats st = ActivityManagerService.this.mProcessCpuTracker.getStats(j);
                                    if (st.vsize > 0 && st.uid < 10000) {
                                        synchronized (ActivityManagerService.this.mPidsSelfLocked) {
                                            if (ActivityManagerService.this.mPidsSelfLocked.indexOfKey(st.pid) >= 0) {
                                            } else {
                                                nativeTotalPss += Debug.getPss(st.pid, null, null);
                                            }
                                        }
                                    }
                                }
                            }
                            memInfoReader.readMemInfo();
                            synchronized (ActivityManagerService.this) {
                                try {
                                    ActivityManagerService.boostPriorityForLockedSection();
                                    if (ActivityManagerDebugConfig.DEBUG_PSS) {
                                        Slog.d(ActivityManagerService.TAG_PSS, "Collected native and kernel memory in " + (SystemClock.uptimeMillis() - start) + "ms");
                                    }
                                    long cachedKb = memInfoReader.getCachedSizeKb();
                                    long freeKb = memInfoReader.getFreeSizeKb();
                                    long zramKb = memInfoReader.getZramTotalSizeKb();
                                    long kernelKb = memInfoReader.getKernelUsedSizeKb();
                                    EventLogTags.writeAmMeminfo(1024 * cachedKb, 1024 * freeKb, 1024 * zramKb, 1024 * kernelKb, 1024 * nativeTotalPss);
                                    ActivityManagerService.this.mProcessStats.addSysMemUsageLocked(cachedKb, freeKb, zramKb, kernelKb, nativeTotalPss);
                                } finally {
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                }
                            }
                        }
                        int num = 0;
                        long[] tmp = new long[2];
                        while (true) {
                            synchronized (ActivityManagerService.this) {
                                try {
                                    ActivityManagerService.boostPriorityForLockedSection();
                                    if (ActivityManagerService.this.mPendingPssProcesses.size() > 0) {
                                        ProcessRecord proc = (ProcessRecord) ActivityManagerService.this.mPendingPssProcesses.remove(0);
                                        int i = proc.pssProcState;
                                        long j2 = proc.lastPssTime;
                                        int pid;
                                        if (proc.thread != null && i == proc.setProcState && 1000 + j2 < SystemClock.uptimeMillis()) {
                                            pid = proc.pid;
                                            break;
                                        }
                                        proc = null;
                                        pid = 0;
                                        break;
                                    }
                                    if (ActivityManagerService.this.mTestPssMode || ActivityManagerDebugConfig.DEBUG_PSS) {
                                        Slog.d(ActivityManagerService.TAG_PSS, "Collected PSS of " + num + " processes in " + (SystemClock.uptimeMillis() - start) + "ms");
                                    }
                                    ActivityManagerService.this.mPendingPssProcesses.clear();
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    return;
                                } finally {
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                }
                            }
                        }
                        break;
                    default:
                        return;
                }
            }
        };
        this.mProcessStateStatsLongs = new long[1];
        this.mPersistentReady = false;
        this.mSupportedSystemLocales = null;
        this.mCtsActions = new HashSet();
        this.mCtsPackages = new HashSet();
        this.mContext = systemContext;
        this.mFactoryTest = FactoryTest.getMode();
        this.mSystemThread = ActivityThread.currentActivityThread();
        Slog.i(TAG, "Memory class: " + ActivityManager.staticGetMemoryClass());
        this.mHandlerThread = new ServiceThread(TAG, -2, false);
        this.mHandlerThread.start();
        this.mHandler = new MainHandler(this.mHandlerThread.getLooper());
        this.mUiHandler = new UiHandler();
        if (sKillHandler == null) {
            sKillThread = new ServiceThread(TAG + ":kill", 10, true);
            sKillThread.start();
            sKillHandler = new KillHandler(sKillThread.getLooper());
        }
        this.mFgBroadcastQueue = new HwBroadcastQueue(this, this.mHandler, "foreground", (long) BROADCAST_FG_TIMEOUT, false);
        this.mBgBroadcastQueue = new HwBroadcastQueue(this, this.mHandler, "background", 60000, false);
        this.mBroadcastQueues[0] = this.mFgBroadcastQueue;
        this.mBroadcastQueues[1] = this.mBgBroadcastQueue;
        setThirdPartyAppBroadcastQueue(this.mBroadcastQueues);
        setKeyAppBroadcastQueue(this.mBroadcastQueues);
        IHwActiveServices iActiveS = HwServiceFactory.getHwActiveServices();
        if (iActiveS != null) {
            this.mServices = iActiveS.getInstance(this);
        } else {
            this.mServices = new ActiveServices(this);
        }
        this.mProviderMap = new ProviderMap(this);
        this.mProviderMapForClone = new ProviderMap(this);
        this.mAppErrors = new AppErrors(this.mContext, this);
        File systemDir = new File(Environment.getDataDirectory(), "system");
        systemDir.mkdirs();
        this.mBatteryStatsService = new BatteryStatsService(systemDir, this.mHandler);
        this.mBatteryStatsService.getActiveStatistics().readLocked();
        this.mBatteryStatsService.scheduleWriteToDisk();
        if (ActivityManagerDebugConfig.DEBUG_POWER) {
            z = true;
        } else {
            z = this.mBatteryStatsService.getActiveStatistics().getIsOnBattery();
        }
        this.mOnBattery = z;
        this.mBatteryStatsService.getActiveStatistics().setCallback(this);
        this.mProcessStats = new ProcessStatsService(this, new File(systemDir, "procstats"));
        IHwAppOpsService iaos = HwServiceFactory.getHwAppOpsService();
        if (iaos == null) {
            this.mAppOpsService = new AppOpsService(new File(systemDir, "appops.xml"), this.mHandler);
        } else {
            this.mAppOpsService = iaos.getInstance(new File(systemDir, "appops.xml"), this.mHandler);
        }
        this.mAppOpsService.startWatchingMode(VR_MODE_CHANGE_MSG, null, new IAppOpsCallback.Stub() {
            public void opChanged(int op, int uid, String packageName) {
                if (op == ActivityManagerService.VR_MODE_CHANGE_MSG && packageName != null && ActivityManagerService.this.mAppOpsService.checkOperation(op, uid, packageName) != 0) {
                    ActivityManagerService.this.runInBackgroundDisabled(uid);
                }
            }
        });
        this.mGrantFile = new AtomicFile(new File(systemDir, "urigrants.xml"));
        this.mUserController = new UserController(this);
        this.GL_ES_VERSION = SystemProperties.getInt("ro.opengles.version", 0);
        if (SystemProperties.getInt("sys.use_fifo_ui", 0) != 0) {
            this.mUseFifoUiScheduling = true;
        }
        this.mTrackingAssociations = "1".equals(SystemProperties.get("debug.track-associations"));
        this.mConfiguration.setToDefaults();
        this.mConfiguration.setLocales(LocaleList.getDefault());
        this.mConfiguration.seq = 1;
        this.mConfigurationSeq = 1;
        this.mProcessCpuTracker.init();
        this.mCompatModePackages = new CompatModePackages(this, systemDir, this.mHandler);
        HwFrameworkFactory.getHwNsdImpl().setContext(this.mContext);
        this.mIntentFirewall = new IntentFirewall(new IntentFirewallInterface(), this.mHandler);
        IHwActivityStackSupervisor iActivitySS = HwServiceFactory.getHwActivityStackSupervisor();
        if (iActivitySS != null) {
            this.mStackSupervisor = iActivitySS.getInstance(this);
        } else {
            this.mStackSupervisor = new ActivityStackSupervisor(this);
        }
        IHwActivityStarter iActivitySt = HwServiceFactory.getHwActivityStarter();
        if (iActivitySt != null) {
            this.mActivityStarter = iActivitySt.getInstance(this, this.mStackSupervisor);
        } else {
            this.mActivityStarter = new ActivityStarter(this, this.mStackSupervisor);
        }
        this.mRecentTasks = new RecentTasks(this, this.mStackSupervisor);
        this.mProcessCpuThread = new Thread("CpuTracker") {
            public void run() {
                while (true) {
                    try {
                        synchronized (this) {
                            long now = SystemClock.uptimeMillis();
                            long nextCpuDelay = (ActivityManagerService.this.mLastCpuTime.get() + ActivityManagerService.MONITOR_CPU_MAX_TIME) - now;
                            long nextWriteDelay = (ActivityManagerService.this.mLastWriteTime + 1800000) - now;
                            if (nextWriteDelay < nextCpuDelay) {
                                nextCpuDelay = nextWriteDelay;
                            }
                            if (nextCpuDelay > 0) {
                                ActivityManagerService.this.mProcessCpuMutexFree.set(true);
                                wait(nextCpuDelay);
                            }
                        }
                    } catch (InterruptedException e) {
                    }
                    try {
                        ActivityManagerService.this.updateCpuStatsNow();
                    } catch (Exception e2) {
                        Slog.e(ActivityManagerService.TAG, "Unexpected exception collecting process stats", e2);
                    }
                }
            }
        };
        Watchdog.getInstance().addMonitor(this);
        Watchdog.getInstance().addThread(this.mHandler);
        this.mIsPerfBoostEnabled = this.mContext.getResources().getBoolean(17957044);
    }

    public void setSystemServiceManager(SystemServiceManager mgr) {
        this.mSystemServiceManager = mgr;
    }

    public void setInstaller(Installer installer) {
        this.mInstaller = installer;
    }

    private void start() {
        Process.removeAllProcessGroups();
        this.mProcessCpuThread.start();
        this.mBatteryStatsService.publish(this.mContext);
        this.mAppOpsService.publish(this.mContext);
        Slog.d("AppOps", "AppOpsService published");
        LocalServices.addService(ActivityManagerInternal.class, new LocalService());
    }

    void onUserStoppedLocked(int userId) {
        this.mRecentTasks.unloadUserDataFromMemoryLocked(userId);
    }

    public void initPowerManagement() {
        this.mStackSupervisor.initPowerManagement();
        this.mBatteryStatsService.initPowerManagement();
        this.mLocalPowerManager = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mVoiceWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "*voice*");
        this.mVoiceWakeLock.setReferenceCounted(false);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == 1599295570) {
            ArrayList<IBinder> procs = new ArrayList();
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    int NP = this.mProcessNames.getMap().size();
                    for (int ip = 0; ip < NP; ip++) {
                        SparseArray<ProcessRecord> apps = (SparseArray) this.mProcessNames.getMap().valueAt(ip);
                        int NA = apps.size();
                        for (int ia = 0; ia < NA; ia++) {
                            ProcessRecord app = (ProcessRecord) apps.valueAt(ia);
                            if (app.thread != null) {
                                procs.add(app.thread.asBinder());
                            }
                        }
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            int N = procs.size();
            for (int i = 0; i < N; i++) {
                Parcel data2 = Parcel.obtain();
                try {
                    ((IBinder) procs.get(i)).transact(1599295570, data2, null, 0);
                } catch (RemoteException e) {
                }
                data2.recycle();
            }
        }
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e2) {
            if (!(e2 instanceof SecurityException)) {
                Slog.wtf(TAG, "Activity Manager Crash", e2);
            }
            throw e2;
        }
    }

    void updateCpuStats() {
        if (this.mLastCpuTime.get() < SystemClock.uptimeMillis() - 5000 && this.mProcessCpuMutexFree.compareAndSet(true, false)) {
            synchronized (this.mProcessCpuThread) {
                this.mProcessCpuThread.notify();
            }
        }
    }

    void updateCpuStatsNow() {
        synchronized (this.mProcessCpuTracker) {
            this.mProcessCpuMutexFree.set(false);
            long now = SystemClock.uptimeMillis();
            boolean haveNewCpuStats = false;
            if (this.mLastCpuTime.get() < now - 5000) {
                this.mLastCpuTime.set(now);
                this.mProcessCpuTracker.update();
                if (this.mProcessCpuTracker.hasGoodLastStats()) {
                    haveNewCpuStats = true;
                    if ("true".equals(SystemProperties.get("events.cpu"))) {
                        int user = this.mProcessCpuTracker.getLastUserTime();
                        int system = this.mProcessCpuTracker.getLastSystemTime();
                        int iowait = this.mProcessCpuTracker.getLastIoWaitTime();
                        int irq = this.mProcessCpuTracker.getLastIrqTime();
                        int softIrq = this.mProcessCpuTracker.getLastSoftIrqTime();
                        int total = ((((user + system) + iowait) + irq) + softIrq) + this.mProcessCpuTracker.getLastIdleTime();
                        if (total == 0) {
                            total = 1;
                        }
                        EventLog.writeEvent(EventLogTags.CPU, new Object[]{Integer.valueOf((((((user + system) + iowait) + irq) + softIrq) * 100) / total), Integer.valueOf((user * 100) / total), Integer.valueOf((system * 100) / total), Integer.valueOf((iowait * 100) / total), Integer.valueOf((irq * 100) / total), Integer.valueOf((softIrq * 100) / total)});
                    }
                }
            }
            BatteryStatsImpl bstats = this.mBatteryStatsService.getActiveStatistics();
            synchronized (bstats) {
                synchronized (this.mPidsSelfLocked) {
                    if (haveNewCpuStats) {
                        if (bstats.startAddingCpuLocked()) {
                            int totalUTime = 0;
                            int totalSTime = 0;
                            int N = this.mProcessCpuTracker.countStats();
                            for (int i = 0; i < N; i++) {
                                Stats st = this.mProcessCpuTracker.getStats(i);
                                if (st.working) {
                                    ProcessRecord pr = (ProcessRecord) this.mPidsSelfLocked.get(st.pid);
                                    totalUTime += st.rel_utime;
                                    totalSTime += st.rel_stime;
                                    Proc ps;
                                    if (pr != null) {
                                        ps = pr.curProcBatteryStats;
                                        if (ps == null || !ps.isActive()) {
                                            ps = bstats.getProcessStatsLocked(pr.info.uid, pr.processName);
                                            pr.curProcBatteryStats = ps;
                                        }
                                        ps.addCpuTimeLocked(st.rel_utime, st.rel_stime);
                                        pr.curCpuTime += (long) (st.rel_utime + st.rel_stime);
                                    } else {
                                        ps = st.batteryStats;
                                        if (ps == null || !ps.isActive()) {
                                            ps = bstats.getProcessStatsLocked(bstats.mapUid(st.uid), st.name);
                                            st.batteryStats = ps;
                                        }
                                        ps.addCpuTimeLocked(st.rel_utime, st.rel_stime);
                                    }
                                }
                            }
                            bstats.finishAddingCpuLocked(totalUTime, totalSTime, this.mProcessCpuTracker.getLastUserTime(), this.mProcessCpuTracker.getLastSystemTime(), this.mProcessCpuTracker.getLastIoWaitTime(), this.mProcessCpuTracker.getLastIrqTime(), this.mProcessCpuTracker.getLastSoftIrqTime(), this.mProcessCpuTracker.getLastIdleTime());
                        }
                    }
                }
                if (this.mLastWriteTime < now - 1800000) {
                    this.mLastWriteTime = now;
                    this.mBatteryStatsService.scheduleWriteToDisk();
                }
            }
        }
    }

    public void batteryNeedsCpuUpdate() {
        updateCpuStatsNow();
    }

    public void batteryPowerChanged(boolean onBattery) {
        updateCpuStatsNow();
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                synchronized (this.mPidsSelfLocked) {
                    if (ActivityManagerDebugConfig.DEBUG_POWER) {
                        onBattery = true;
                    }
                    this.mOnBattery = onBattery;
                }
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void batterySendBroadcast(Intent intent) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                broadcastIntentLocked(null, null, intent, null, null, 0, null, null, null, -1, null, false, false, -1, 1000, -1);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private HashMap<String, IBinder> getCommonServicesLocked(boolean isolated) {
        if (isolated) {
            if (this.mIsolatedAppBindArgs == null) {
                this.mIsolatedAppBindArgs = new HashMap();
                this.mIsolatedAppBindArgs.put(HwBroadcastRadarUtil.KEY_PACKAGE, ServiceManager.getService(HwBroadcastRadarUtil.KEY_PACKAGE));
            }
            return this.mIsolatedAppBindArgs;
        }
        if (this.mAppBindArgs == null) {
            this.mAppBindArgs = new HashMap();
            this.mAppBindArgs.put(HwBroadcastRadarUtil.KEY_PACKAGE, ServiceManager.getService(HwBroadcastRadarUtil.KEY_PACKAGE));
            this.mAppBindArgs.put("window", ServiceManager.getService("window"));
            this.mAppBindArgs.put("alarm", ServiceManager.getService("alarm"));
        }
        return this.mAppBindArgs;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean setFocusedActivityLocked(ActivityRecord r, String reason) {
        if (r == null || this.mFocusedActivity == r || !r.isFocusable()) {
            return false;
        }
        boolean wasDoingSetFocusedActivity = this.mDoingSetFocusedActivity;
        if (wasDoingSetFocusedActivity) {
            Slog.w(TAG, "setFocusedActivityLocked: called recursively, r=" + r + ", reason=" + reason);
        }
        this.mDoingSetFocusedActivity = true;
        ActivityRecord last = this.mFocusedActivity;
        this.mFocusedActivity = r;
        if (!r.task.isApplicationTask()) {
            r.appTimeTracker = null;
        } else if (this.mCurAppTimeTracker != r.appTimeTracker) {
            if (this.mCurAppTimeTracker != null) {
                this.mCurAppTimeTracker.stop();
                this.mHandler.obtainMessage(55, this.mCurAppTimeTracker).sendToTarget();
                this.mStackSupervisor.clearOtherAppTimeTrackers(r.appTimeTracker);
                this.mCurAppTimeTracker = null;
            }
            if (r.appTimeTracker != null) {
                this.mCurAppTimeTracker = r.appTimeTracker;
                startTimeTrackingFocusedActivityLocked();
            }
        } else {
            startTimeTrackingFocusedActivityLocked();
        }
        if (r.task.voiceInteractor != null) {
            startRunningVoiceLocked(r.task.voiceSession, r.info.applicationInfo.uid);
        } else {
            finishRunningVoiceLocked();
            if (last != null) {
                IVoiceInteractionSession session = last.task.voiceSession;
                if (session == null) {
                    session = last.voiceSession;
                }
                finishVoiceTask(session);
            }
        }
        if (this.mStackSupervisor.moveActivityStackToFront(r, reason + " setFocusedActivity")) {
            this.mWindowManager.setFocusedApp(r.appToken, true);
        }
        applyUpdateLockStateLocked(r);
        applyUpdateVrModeLocked(r);
        if (this.mFocusedActivity.userId != this.mLastFocusedUserId) {
            this.mHandler.removeMessages(53);
            this.mHandler.obtainMessage(53, this.mFocusedActivity.userId, 0).sendToTarget();
            this.mLastFocusedUserId = this.mFocusedActivity.userId;
        }
        if (this.mFocusedActivity != r) {
            Slog.w(TAG, "setFocusedActivityLocked: r=" + r + " but focused to " + this.mFocusedActivity);
        }
        this.mDoingSetFocusedActivity = wasDoingSetFocusedActivity;
        EventLogTags.writeAmFocusedActivity(this.mFocusedActivity == null ? -1 : this.mFocusedActivity.userId, this.mFocusedActivity == null ? "NULL" : this.mFocusedActivity.shortComponentName, reason);
        if (IS_DEBUG_VERSION) {
            ArrayMap<String, Object> params = new ArrayMap();
            params.put("checkType", "FocusWindowNullScene");
            params.put("looper", BackgroundThread.getHandler().getLooper());
            if (this.mFocusedActivity != null) {
                params.put("focusedActivityName", this.mFocusedActivity.toString());
            }
            params.put("windowManager", this.mWindowManager);
            if (HwServiceFactory.getWinFreezeScreenMonitor() != null) {
                HwServiceFactory.getWinFreezeScreenMonitor().checkFreezeScreen(params);
            }
        }
        return true;
    }

    final void resetFocusedActivityIfNeededLocked(ActivityRecord goingAway) {
        if (this.mFocusedActivity == goingAway) {
            ActivityStack focusedStack = this.mStackSupervisor.getFocusedStack();
            if (focusedStack != null) {
                ActivityRecord top = focusedStack.topActivity();
                if (!(top == null || top.userId == this.mLastFocusedUserId)) {
                    this.mHandler.removeMessages(53);
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(53, top.userId, 0));
                    this.mLastFocusedUserId = top.userId;
                }
            }
            if (!setFocusedActivityLocked(focusedStack.topRunningActivityLocked(), "resetFocusedActivityIfNeeded")) {
                this.mFocusedActivity = null;
                EventLogTags.writeAmFocusedActivity(-1, "NULL", "resetFocusedActivityIfNeeded");
            }
        }
    }

    public void setFocusedStack(int stackId) {
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "setFocusedStack()");
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                ActivityStack stack = this.mStackSupervisor.getStack(stackId);
                if (stack == null) {
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(callingId);
                    return;
                }
                if (setFocusedActivityLocked(stack.topRunningActivityLocked(), "setFocusedStack")) {
                    this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                }
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(callingId);
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void setFocusedTask(int taskId) {
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "setFocusedTask()");
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                TaskRecord task = this.mStackSupervisor.anyTaskForIdLocked(taskId);
                if (task == null) {
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(callingId);
                } else if (this.mUserController.shouldConfirmCredentials(task.userId)) {
                    this.mActivityStarter.showConfirmDeviceCredential(task.userId);
                    if (task.stack != null && task.stack.mStackId == 2) {
                        this.mStackSupervisor.moveTaskToStackLocked(task.taskId, 1, false, false, "setFocusedTask", true);
                    }
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(callingId);
                } else {
                    if (setFocusedActivityLocked(task.topRunningActivityLocked(), "setFocusedTask")) {
                        this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                    }
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void registerTaskStackListener(ITaskStackListener listener) throws RemoteException {
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "registerTaskStackListener()");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if (listener != null) {
                    this.mTaskStackListeners.register(listener);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void notifyActivityDrawn(IBinder token) {
        if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.d(TAG_VISIBILITY, "notifyActivityDrawn: token=" + token);
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord r = this.mStackSupervisor.isInAnyStackLocked(token);
                if (r != null) {
                    r.task.stack.notifyActivityDrawnLocked(r);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    final void applyUpdateLockStateLocked(ActivityRecord r) {
        int i;
        boolean z = r != null ? r.immersive : false;
        MainHandler mainHandler = this.mHandler;
        MainHandler mainHandler2 = this.mHandler;
        if (z) {
            i = 1;
        } else {
            i = 0;
        }
        mainHandler.sendMessage(mainHandler2.obtainMessage(37, i, 0, r));
    }

    final void applyUpdateVrModeLocked(ActivityRecord r) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(VR_MODE_CHANGE_MSG, 0, 0, r));
    }

    private void applyVrModeIfNeededLocked(ActivityRecord r, boolean enable) {
        int i;
        MainHandler mainHandler = this.mHandler;
        MainHandler mainHandler2 = this.mHandler;
        if (enable) {
            i = 1;
        } else {
            i = 0;
        }
        mainHandler.sendMessage(mainHandler2.obtainMessage(69, i, 0, r));
    }

    private void applyVrMode(boolean enabled, ComponentName packageName, int userId, ComponentName callingPackage, boolean immediate) {
        VrManagerInternal vrService = (VrManagerInternal) LocalServices.getService(VrManagerInternal.class);
        if (immediate) {
            vrService.setVrModeImmediate(enabled, packageName, userId, callingPackage);
        } else {
            vrService.setVrMode(enabled, packageName, userId, callingPackage);
        }
    }

    final void showAskCompatModeDialogLocked(ActivityRecord r) {
        Message msg = Message.obtain();
        msg.what = 30;
        if (r.task.askedCompatMode) {
            r = null;
        }
        msg.obj = r;
        this.mUiHandler.sendMessage(msg);
    }

    final void showUnsupportedZoomDialogIfNeededLocked(ActivityRecord r) {
        if (this.mConfiguration.densityDpi != DisplayMetrics.DENSITY_DEVICE_STABLE && r.appInfo.requiresSmallestWidthDp > this.mConfiguration.smallestScreenWidthDp) {
            Message msg = Message.obtain();
            msg.what = 70;
            msg.obj = r;
            this.mUiHandler.sendMessage(msg);
        }
    }

    private int updateLruProcessInternalLocked(ProcessRecord app, long now, int index, String what, Object obj, ProcessRecord srcApp) {
        app.lastActivityTime = now;
        if (app.activities.size() > 0) {
            return index;
        }
        int lrui = this.mLruProcesses.lastIndexOf(app);
        if (lrui < 0) {
            Slog.wtf(TAG, "Adding dependent process " + app + " not on LRU list: " + what + " " + obj + " from " + srcApp);
            return index;
        } else if (lrui >= index || lrui >= this.mLruProcessActivityStart) {
            return index;
        } else {
            this.mLruProcesses.remove(lrui);
            if (index > 0) {
                index--;
            }
            if (ActivityManagerDebugConfig.DEBUG_LRU) {
                Slog.d(TAG_LRU, "Moving dep from " + lrui + " to " + index + " in LRU list: " + app);
            }
            this.mLruProcesses.add(index, app);
            return index;
        }
    }

    static void killProcessGroup(int uid, int pid) {
        if (sKillHandler != null) {
            sKillHandler.sendMessage(sKillHandler.obtainMessage(4000, uid, pid));
            return;
        }
        Slog.w(TAG, "Asked to kill process group before system bringup!");
        Process.killProcessGroup(uid, pid);
    }

    final void removeLruProcessLocked(ProcessRecord app) {
        int lrui = this.mLruProcesses.lastIndexOf(app);
        if (lrui >= 0) {
            if (!app.killed) {
                Slog.wtfStack(TAG, "Removing process that hasn't been killed: " + app);
                Process.killProcessQuiet(app.pid);
                killProcessGroup(app.uid, app.pid);
            }
            if (lrui <= this.mLruProcessActivityStart) {
                this.mLruProcessActivityStart--;
            }
            if (lrui <= this.mLruProcessServiceStart) {
                this.mLruProcessServiceStart--;
            }
            this.mLruProcesses.remove(lrui);
        }
    }

    final void updateLruProcessLocked(ProcessRecord app, boolean activityChange, ProcessRecord client) {
        boolean hasActivity;
        if (app.activities.size() > 0 || app.hasClientActivities) {
            hasActivity = true;
        } else {
            hasActivity = app.treatLikeActivity;
        }
        if (activityChange || !hasActivity || (app.persistent && !this.mLruProcesses.contains(app))) {
            int N;
            this.mLruSeq++;
            long now = SystemClock.uptimeMillis();
            app.lastActivityTime = now;
            if (hasActivity) {
                N = this.mLruProcesses.size();
                if (N > 0 && this.mLruProcesses.get(N - 1) == app) {
                    if (ActivityManagerDebugConfig.DEBUG_LRU) {
                        Slog.d(TAG_LRU, "Not moving, already top activity: " + app);
                    }
                    return;
                }
            } else if (this.mLruProcessServiceStart > 0 && this.mLruProcesses.get(this.mLruProcessServiceStart - 1) == app) {
                if (ActivityManagerDebugConfig.DEBUG_LRU) {
                    Slog.d(TAG_LRU, "Not moving, already top other: " + app);
                }
                return;
            }
            int lrui = this.mLruProcesses.lastIndexOf(app);
            if (!app.persistent || lrui < 0) {
                int nextIndex;
                int j;
                if (lrui >= 0) {
                    if (lrui < this.mLruProcessActivityStart) {
                        this.mLruProcessActivityStart--;
                    }
                    if (lrui < this.mLruProcessServiceStart) {
                        this.mLruProcessServiceStart--;
                    }
                    this.mLruProcesses.remove(lrui);
                }
                if (hasActivity) {
                    N = this.mLruProcesses.size();
                    if (app.activities.size() != 0 || this.mLruProcessActivityStart >= N - 1) {
                        if (ActivityManagerDebugConfig.DEBUG_LRU) {
                            Slog.d(TAG_LRU, "Adding to top of LRU activity list: " + app);
                        }
                        this.mLruProcesses.add(app);
                    } else {
                        if (ActivityManagerDebugConfig.DEBUG_LRU) {
                            Slog.d(TAG_LRU, "Adding to second-top of LRU activity list: " + app);
                        }
                        this.mLruProcesses.add(N - 1, app);
                        int uid = app.info.uid;
                        int i = N - 2;
                        while (i > this.mLruProcessActivityStart && ((ProcessRecord) this.mLruProcesses.get(i)).info.uid == uid) {
                            if (((ProcessRecord) this.mLruProcesses.get(i - 1)).info.uid != uid) {
                                if (ActivityManagerDebugConfig.DEBUG_LRU) {
                                    Slog.d(TAG_LRU, "Pushing uid " + uid + " swapping at " + i + ": " + this.mLruProcesses.get(i) + " : " + this.mLruProcesses.get(i - 1));
                                }
                                ProcessRecord tmp = (ProcessRecord) this.mLruProcesses.get(i);
                                this.mLruProcesses.set(i, (ProcessRecord) this.mLruProcesses.get(i - 1));
                                this.mLruProcesses.set(i - 1, tmp);
                                i--;
                            }
                            i--;
                        }
                    }
                    nextIndex = this.mLruProcessServiceStart;
                } else {
                    int index = this.mLruProcessServiceStart;
                    if (client != null) {
                        int clientIndex = this.mLruProcesses.lastIndexOf(client);
                        if (ActivityManagerDebugConfig.DEBUG_LRU && clientIndex < 0) {
                            Slog.d(TAG_LRU, "Unknown client " + client + " when updating " + app);
                        }
                        if (clientIndex <= lrui) {
                            clientIndex = lrui;
                        }
                        if (clientIndex >= 0 && index > clientIndex) {
                            index = clientIndex;
                        }
                    }
                    if (ActivityManagerDebugConfig.DEBUG_LRU) {
                        Slog.d(TAG_LRU, "Adding at " + index + " of LRU list: " + app);
                    }
                    this.mLruProcesses.add(index, app);
                    nextIndex = index - 1;
                    this.mLruProcessActivityStart++;
                    this.mLruProcessServiceStart++;
                }
                for (j = app.connections.size() - 1; j >= 0; j--) {
                    ConnectionRecord cr = (ConnectionRecord) app.connections.valueAt(j);
                    if (!(cr.binding == null || cr.serviceDead || cr.binding.service == null || cr.binding.service.app == null || cr.binding.service.app.lruSeq == this.mLruSeq || cr.binding.service.app.persistent)) {
                        nextIndex = updateLruProcessInternalLocked(cr.binding.service.app, now, nextIndex, "service connection", cr, app);
                    }
                }
                for (j = app.conProviders.size() - 1; j >= 0; j--) {
                    ContentProviderRecord cpr = ((ContentProviderConnection) app.conProviders.get(j)).provider;
                    if (!(cpr.proc == null || cpr.proc.lruSeq == this.mLruSeq || cpr.proc.persistent)) {
                        nextIndex = updateLruProcessInternalLocked(cpr.proc, now, nextIndex, "provider reference", cpr, app);
                    }
                }
                return;
            }
            if (ActivityManagerDebugConfig.DEBUG_LRU) {
                Slog.d(TAG_LRU, "Not moving, persistent: " + app);
            }
        }
    }

    protected ProcessRecord getProcessRecordLocked(String processName, int uid, boolean keepIfLarge) {
        if (uid == 1000) {
            SparseArray<ProcessRecord> procs = (SparseArray) this.mProcessNames.getMap().get(processName);
            if (procs == null) {
                return null;
            }
            int procCount = procs.size();
            for (int i = 0; i < procCount; i++) {
                int procUid = procs.keyAt(i);
                if (!UserHandle.isApp(procUid) && UserHandle.isSameUser(procUid, uid)) {
                    return (ProcessRecord) procs.valueAt(i);
                }
            }
        }
        ProcessRecord proc = (ProcessRecord) this.mProcessNames.get(processName, uid);
        if (proc != null && !keepIfLarge && this.mLastMemoryLevel > 0 && proc.setProcState >= 16) {
            if (ActivityManagerDebugConfig.DEBUG_PSS) {
                Slog.d(TAG_PSS, "May not keep " + proc + ": pss=" + proc.lastCachedPss);
            }
            if (proc.lastCachedPss >= this.mProcessList.getCachedRestoreThresholdKb() && !"com.android.deskclock".equals(processName)) {
                if (proc.baseProcessTracker != null) {
                    proc.baseProcessTracker.reportCachedKill(proc.pkgList, proc.lastCachedPss);
                }
                proc.kill(Long.toString(proc.lastCachedPss) + "k from cached", true);
            }
        }
        return proc;
    }

    void notifyPackageUse(String packageName, int reason) {
        try {
            AppGlobals.getPackageManager().notifyPackageUse(packageName, reason);
        } catch (RemoteException e) {
        }
    }

    boolean isNextTransitionForward() {
        int transit = this.mWindowManager.getPendingAppTransition();
        if (transit == 6 || transit == 8 || transit == 10) {
            return true;
        }
        return false;
    }

    int startIsolatedProcess(String entryPoint, String[] entryPointArgs, String processName, String abiOverride, int uid, Runnable crashHandler) {
        int i;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ApplicationInfo info = new ApplicationInfo();
                info.uid = 1000;
                info.processName = processName;
                info.className = entryPoint;
                info.packageName = "android";
                ProcessRecord proc = startProcessLocked(processName, info, false, 0, "", null, true, true, uid, true, abiOverride, entryPoint, entryPointArgs, crashHandler);
                i = proc != null ? proc.pid : 0;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return i;
    }

    final ProcessRecord startProcessLocked(String processName, ApplicationInfo info, boolean knownToBeDead, int intentFlags, String hostingType, ComponentName hostingName, boolean allowWhileBooting, boolean isolated, boolean keepIfLarge) {
        return startProcessLocked(processName, info, knownToBeDead, intentFlags, hostingType, hostingName, allowWhileBooting, isolated, 0, keepIfLarge, null, null, null, null);
    }

    final ProcessRecord startProcessLocked(String processName, ApplicationInfo info, boolean knownToBeDead, int intentFlags, String hostingType, ComponentName hostingName, boolean allowWhileBooting, boolean isolated, int isolatedUid, boolean keepIfLarge, String abiOverride, String entryPoint, String[] entryPointArgs, Runnable crashHandler) {
        ProcessRecord app;
        long startTime = SystemClock.elapsedRealtime();
        if (isolated) {
            app = null;
        } else {
            app = getProcessRecordLocked(processName, info.uid + info.euid, keepIfLarge);
            checkTime(startTime, "startProcess: after getProcessRecord");
            if ((intentFlags & 4) == 0) {
                if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                    Slog.v(TAG, "Clearing bad process: " + info.uid + "/" + info.processName);
                }
                this.mAppErrors.resetProcessCrashTimeLocked(info);
                if (this.mAppErrors.isBadProcessLocked(info)) {
                    EventLog.writeEvent(EventLogTags.AM_PROC_GOOD, new Object[]{Integer.valueOf(UserHandle.getUserId(info.uid)), Integer.valueOf(info.uid), info.processName});
                    this.mAppErrors.clearBadProcessLocked(info);
                    if (app != null) {
                        app.bad = false;
                    }
                }
                if (this.mBadPkgs.get(info.packageName) != null) {
                    this.mBadPkgs.remove(info.packageName);
                    Slog.d(TAG, "Clearing bad pkg: " + info.packageName);
                }
            } else if (this.mAppErrors.isBadProcessLocked(info)) {
                if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                    Slog.v(TAG, "Bad process: " + info.uid + "/" + info.processName);
                }
                return null;
            }
        }
        nativeMigrateToBoost();
        this.mIsBoosted = true;
        this.mBoostStartTime = SystemClock.uptimeMillis();
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(APP_BOOST_DEACTIVATE_MSG), 3000);
        if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
            Slog.v(TAG_PROCESSES, "startProcess: name=" + processName + " app=" + app + " knownToBeDead=" + knownToBeDead + " thread=" + (app != null ? app.thread : null) + " pid=" + (app != null ? app.pid : -1));
        }
        if (app != null && app.pid > 0) {
            if ((knownToBeDead || app.killed) && app.thread != null) {
                if (ActivityManagerDebugConfig.DEBUG_PROCESSES || ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                    Slog.v(TAG_PROCESSES, "App died: " + app);
                }
                checkTime(startTime, "startProcess: bad proc running, killing");
                killProcessGroup(app.uid, app.pid);
                handleAppDiedLocked(app, true, true);
                checkTime(startTime, "startProcess: done killing old proc");
            } else {
                if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
                    Slog.v(TAG_PROCESSES, "App already running: " + app);
                }
                app.addPackage(info.packageName, info.versionCode, this.mProcessStats);
                checkTime(startTime, "startProcess: done, added package to proc");
                return app;
            }
        }
        String flattenToShortString = hostingName != null ? hostingName.flattenToShortString() : null;
        if (app == null) {
            checkTime(startTime, "startProcess: creating new process record");
            app = newProcessRecordLocked(info, processName, isolated, isolatedUid);
            if (app == null) {
                Slog.w(TAG, "Failed making new process record for " + processName + "/" + info.uid + " isolated=" + isolated);
                return null;
            }
            app.crashHandler = crashHandler;
            checkTime(startTime, "startProcess: done creating new process record");
        } else {
            app.addPackage(info.packageName, info.versionCode, this.mProcessStats);
            checkTime(startTime, "startProcess: added package to existing proc");
        }
        if (this.mProcessesReady || isAllowedWhileBooting(info) || allowWhileBooting) {
            checkTime(startTime, "startProcess: stepping in to startProcess");
            startProcessLocked(app, hostingType, flattenToShortString, abiOverride, entryPoint, entryPointArgs);
            checkTime(startTime, "startProcess: done starting proc!");
            if (app.pid == 0) {
                app = null;
            }
            return app;
        }
        if (!this.mProcessesOnHold.contains(app)) {
            this.mProcessesOnHold.add(app);
        }
        if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
            Slog.v(TAG_PROCESSES, "System not ready, putting on hold: " + app);
        }
        checkTime(startTime, "startProcess: returning with proc on hold");
        return app;
    }

    boolean isAllowedWhileBooting(ApplicationInfo ai) {
        return (ai.flags & 8) != 0;
    }

    protected void startProcessLocked(ProcessRecord app, String hostingType, String hostingNameStr) {
        startProcessLocked(app, hostingType, hostingNameStr, null, null, null);
    }

    protected void startProcessLocked(ProcessRecord app, String hostingType, String hostingNameStr, String abiOverride, String entryPoint, String[] entryPointArgs) {
        ThreadPolicy savedPolicy;
        if ("activity".equals(hostingType)) {
            app.launchfromActivity = true;
        } else {
            app.launchfromActivity = false;
        }
        long startTime = SystemClock.elapsedRealtime();
        if (app.pid > 0 && app.pid != MY_PID) {
            checkTime(startTime, "startProcess: removing from pids map");
            synchronized (this.mPidsSelfLocked) {
                this.mPidsSelfLocked.remove(app.pid);
                this.mHandler.removeMessages(20, app);
            }
            checkTime(startTime, "startProcess: done removing from pids map");
            app.setPid(0);
        }
        if (ActivityManagerDebugConfig.DEBUG_PROCESSES && this.mProcessesOnHold.contains(app)) {
            Slog.v(TAG_PROCESSES, "startProcessLocked removing on hold: " + app);
        }
        this.mProcessesOnHold.remove(app);
        checkTime(startTime, "startProcess: starting to update cpu stats");
        updateCpuStats();
        checkTime(startTime, "startProcess: done updating cpu stats");
        try {
            String requiredAbi;
            ProcessRecord oldApp;
            AppGlobals.getPackageManager().checkPackageStartable(app.info.packageName, UserHandle.getUserId(app.uid));
            int uid = app.uid;
            int[] gids = null;
            int mountExternal = 0;
            if (!app.isolated) {
                checkTime(startTime, "startProcess: getting gids from package manager");
                int[] permGids = AppGlobals.getPackageManager().getPackageGids(app.info.packageName, 268435456, app.userId);
                mountExternal = ((MountServiceInternal) LocalServices.getService(MountServiceInternal.class)).getExternalStorageMountMode(uid, app.info.packageName);
                if (ArrayUtils.isEmpty(permGids)) {
                    gids = new int[2];
                } else {
                    gids = new int[(permGids.length + 2)];
                    System.arraycopy(permGids, 0, gids, 2, permGids.length);
                }
                gids[0] = UserHandle.getSharedAppGid(UserHandle.getAppId(uid));
                gids[1] = UserHandle.getUserGid(UserHandle.getUserId(uid));
                gids = handleGidsForUser(gids, UserHandle.getUserId(uid));
            }
            checkTime(startTime, "startProcess: building args");
            if (this.mFactoryTest != 0) {
                if (this.mFactoryTest == 1 && this.mTopComponent != null && app.processName.equals(this.mTopComponent.getPackageName())) {
                    uid = 0;
                }
                if (this.mFactoryTest == 2 && (app.info.flags & 16) != 0) {
                    uid = 0;
                }
            }
            int debugFlags = 0;
            if ((app.info.flags & 2) != 0) {
                debugFlags = 1 | 2;
            }
            if ((app.info.flags & DumpState.DUMP_KEYSETS) != 0 || this.mSafeMode) {
                debugFlags |= 8;
            }
            if ("1".equals(SystemProperties.get("debug.checkjni"))) {
                debugFlags |= 2;
            }
            if ("true".equals(SystemProperties.get("debug.generate-debug-info"))) {
                debugFlags |= 32;
            }
            if ("1".equals(SystemProperties.get("debug.jni.logging"))) {
                debugFlags |= 16;
            }
            if ("1".equals(SystemProperties.get("debug.assert"))) {
                debugFlags |= 4;
            }
            if (this.mNativeDebuggingApp != null && this.mNativeDebuggingApp.equals(app.processName)) {
                debugFlags = ((debugFlags | 64) | 32) | 128;
                this.mNativeDebuggingApp = null;
            }
            if (abiOverride != null) {
                requiredAbi = abiOverride;
            } else {
                requiredAbi = app.info.primaryCpuAbi;
            }
            if (requiredAbi == null) {
                requiredAbi = Build.SUPPORTED_ABIS[0];
            }
            String instructionSet = null;
            if (app.info.primaryCpuAbi != null) {
                instructionSet = VMRuntime.getInstructionSet(app.info.primaryCpuAbi);
            }
            app.gids = gids;
            app.requiredAbi = requiredAbi;
            app.instructionSet = instructionSet;
            boolean isActivityProcess = entryPoint == null;
            if (entryPoint == null) {
                entryPoint = "android.app.ActivityThread";
            }
            if (Jlog.isPerfTest()) {
                Jlog.i(2029, "processname=" + app.processName + "&pkg=" + (hostingNameStr != null ? hostingNameStr : "unknow"));
            }
            Trace.traceBegin(64, "Start proc: " + app.processName);
            checkTime(startTime, "startProcess: asking zygote to start proc");
            ProcessStartResult startResult = Process.start(entryPoint, app.processName, uid, uid, gids, debugFlags, mountExternal, app.info.targetSdkVersion, app.info.seinfo, requiredAbi, instructionSet, app.info.dataDir, entryPointArgs);
            if (Jlog.isPerfTest()) {
                Jlog.i(2030, "pid=" + (startResult != null ? startResult.pid : 0) + "&processname=" + app.processName);
            }
            checkTime(startTime, "startProcess: returned from zygote!");
            Trace.traceEnd(64);
            if (app.isolated) {
                this.mBatteryStatsService.addIsolatedUid(app.uid, app.info.uid);
            }
            if (!hostingType.equals("activity")) {
                if (!hostingType.equals("content provider")) {
                    this.mBatteryStatsService.noteProcessStart(app.processName, app.info.uid);
                }
            }
            if ("1".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0")) || (app.info.flags & 1) == 0) {
                HwSysResource pids = HwFrameworkFactory.getHwResource(16);
                if (pids != null) {
                    savedPolicy = StrictMode.allowThreadDiskReads();
                    pids.acquire(startResult.pid, app.info.packageName, 0);
                    StrictMode.setThreadPolicy(savedPolicy);
                }
            }
            checkTime(startTime, "startProcess: done updating battery stats");
            Object[] objArr = new Object[6];
            objArr[0] = Integer.valueOf(UserHandle.getUserId(uid));
            objArr[1] = Integer.valueOf(startResult.pid);
            objArr[2] = Integer.valueOf(app.info.euid + uid);
            objArr[3] = app.processName;
            objArr[4] = hostingType;
            objArr[5] = hostingNameStr != null ? hostingNameStr : "";
            EventLog.writeEvent(EventLogTags.AM_PROC_START, objArr);
            try {
                AppGlobals.getPackageManager().logAppProcessStartIfNeeded(app.processName, app.uid, app.info.seinfo, app.info.sourceDir, startResult.pid);
            } catch (RemoteException e) {
            }
            if (app.persistent) {
                Watchdog.getInstance().processStarted(app.processName, startResult.pid);
            }
            checkTime(startTime, "startProcess: building log message");
            StringBuilder buf = this.mStringBuilder;
            buf.setLength(0);
            if (hostingType.equals("activity")) {
                BoostFramework boostFramework = null;
                if (this.mIsPerfBoostEnabled) {
                    boostFramework = new BoostFramework();
                }
                if (boostFramework != null) {
                    boostFramework.perfIOPrefetchStart(startResult.pid, app.processName);
                }
            }
            buf.append("Start proc ");
            buf.append(startResult.pid);
            buf.append(':');
            buf.append(app.processName);
            buf.append('/');
            UserHandle.formatUid(buf, uid);
            if (!isActivityProcess) {
                buf.append(" [");
                buf.append(entryPoint);
                buf.append("]");
            }
            buf.append(" for ");
            buf.append(hostingType);
            if (hostingNameStr != null) {
                buf.append(" ");
                buf.append(hostingNameStr);
            }
            Slog.i(TAG, buf.toString());
            LogPower.push(111, app.processName, hostingType, String.valueOf(startResult.pid), new String[]{String.valueOf(uid)});
            app.setPid(startResult.pid);
            app.usingWrapper = startResult.usingWrapper;
            app.removed = false;
            app.killed = false;
            app.killedByAm = false;
            checkTime(startTime, "startProcess: starting to update pids map");
            registerCtrlSocketForMm(app.processName, app.pid);
            synchronized (this.mPidsSelfLocked) {
                oldApp = (ProcessRecord) this.mPidsSelfLocked.get(startResult.pid);
            }
            if (!(oldApp == null || app.isolated)) {
                Slog.w(TAG, "Reusing pid " + startResult.pid + " while app is still mapped to it");
                cleanUpApplicationRecordLocked(oldApp, false, false, -1, true);
            }
            synchronized (this.mPidsSelfLocked) {
                this.mPidsSelfLocked.put(startResult.pid, app);
                if (isActivityProcess) {
                    Message msg = this.mHandler.obtainMessage(20);
                    msg.obj = app;
                    this.mHandler.sendMessageDelayed(msg, (long) (startResult.usingWrapper ? PROC_START_TIMEOUT_WITH_WRAPPER : 10000));
                }
                HwServiceFactory.getHwNLPManager().setPidGoogleLocation(app.pid, app.processName);
            }
            checkTime(startTime, "startProcess: done updating pids map");
            if (Jlog.isUBMEnable()) {
                StringBuilder append = new StringBuilder().append("AC#").append(app.processName != null ? app.processName : "unknow").append("(").append(app.info.versionCode).append(",").append(app.pid).append(",");
                if (hostingNameStr == null) {
                    hostingNameStr = "none";
                }
                Jlog.d(268, append.append(hostingNameStr).append(")").toString());
            }
        } catch (RemoteException e2) {
            throw e2.rethrowAsRuntimeException();
        } catch (RemoteException e22) {
            throw e22.rethrowAsRuntimeException();
        } catch (Throwable e3) {
            Slog.e(TAG, "Failure starting process " + app.processName, e3);
            forceStopPackageLocked(app.info.packageName, UserHandle.getAppId(app.uid), false, false, true, false, false, UserHandle.getUserId(app.userId), "start failure");
        } catch (Throwable th) {
            StrictMode.setThreadPolicy(savedPolicy);
        }
    }

    protected void updateUsageStats(ActivityRecord component, boolean resumed) {
        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
            Slog.d(TAG_SWITCH, "updateUsageStats: comp=" + component + "res=" + resumed);
        }
        BatteryStatsImpl stats = this.mBatteryStatsService.getActiveStatistics();
        if (resumed) {
            if (this.mUsageStatsService != null) {
                this.mUsageStatsService.reportEvent(component.realActivity, component.userId, 1);
            }
            synchronized (stats) {
                stats.noteActivityResumedLocked(component.app.uid);
            }
        }
        if (this.mUsageStatsService != null) {
            this.mUsageStatsService.reportEvent(component.realActivity, component.userId, 2);
        }
        synchronized (stats) {
            stats.noteActivityPausedLocked(component.app.uid);
        }
    }

    Intent getHomeIntent() {
        Uri uri = null;
        String str = this.mTopAction;
        if (this.mTopData != null) {
            uri = Uri.parse(this.mTopData);
        }
        Intent intent = new Intent(str, uri);
        intent.setComponent(this.mTopComponent);
        intent.addFlags(256);
        if (this.mFactoryTest != 1) {
            intent.addCategory("android.intent.category.HOME");
            intent.addFlags(512);
        }
        return intent;
    }

    boolean startHomeActivityLocked(int userId, String reason) {
        if (this.mFactoryTest == 1 && this.mTopAction == null) {
            return false;
        }
        Intent intent = getHomeIntent();
        ActivityInfo aInfo = resolveActivityInfo(intent, 1024, userId);
        if (aInfo != null) {
            intent.setComponent(new ComponentName(aInfo.applicationInfo.packageName, aInfo.name));
            ActivityInfo aInfo2 = new ActivityInfo(aInfo);
            aInfo2.applicationInfo = getAppInfoForUser(aInfo2.applicationInfo, userId);
            ProcessRecord app = getProcessRecordLocked(aInfo2.processName, aInfo2.applicationInfo.uid + aInfo2.applicationInfo.euid, true);
            if (app == null || app.instrumentationClass == null) {
                intent.setFlags(intent.getFlags() | 268435456);
                this.mActivityStarter.startHomeActivityLocked(intent, aInfo2, reason);
                aInfo = aInfo2;
            }
        } else {
            Slog.wtf(TAG, "No home screen found for " + intent, new Throwable());
        }
        return true;
    }

    private ActivityInfo resolveActivityInfo(Intent intent, int flags, int userId) {
        ComponentName comp = intent.getComponent();
        if (comp != null) {
            try {
                return AppGlobals.getPackageManager().getActivityInfo(comp, flags, userId);
            } catch (RemoteException e) {
                return null;
            }
        }
        ResolveInfo info = AppGlobals.getPackageManager().resolveIntent(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), flags, userId);
        if (info != null) {
            return info.activityInfo;
        }
        return null;
    }

    void startSetupActivityLocked() {
        if (!this.mCheckedForSetup) {
            ContentResolver resolver = this.mContext.getContentResolver();
            if (this.mFactoryTest != 1) {
                if (Global.getInt(resolver, "device_provisioned", 0) != 0) {
                    this.mCheckedForSetup = true;
                    Intent intent = new Intent("android.intent.action.UPGRADE_SETUP");
                    List<ResolveInfo> ris = this.mContext.getPackageManager().queryIntentActivities(intent, 1048704);
                    if (!ris.isEmpty()) {
                        String vers;
                        ResolveInfo ri = (ResolveInfo) ris.get(0);
                        if (ri.activityInfo.metaData != null) {
                            vers = ri.activityInfo.metaData.getString("android.SETUP_VERSION");
                        } else {
                            vers = null;
                        }
                        if (vers == null && ri.activityInfo.applicationInfo.metaData != null) {
                            vers = ri.activityInfo.applicationInfo.metaData.getString("android.SETUP_VERSION");
                        }
                        String lastVers = Secure.getString(resolver, "last_setup_shown");
                        if (!(vers == null || vers.equals(lastVers))) {
                            intent.setFlags(268435456);
                            intent.setComponent(new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name));
                            this.mActivityStarter.startActivityLocked(null, intent, null, null, ri.activityInfo, null, null, null, null, null, 0, 0, 0, null, 0, 0, 0, null, false, false, null, null, null);
                        }
                    }
                }
            }
        }
    }

    CompatibilityInfo compatibilityInfoForPackageLocked(ApplicationInfo ai) {
        return this.mCompatModePackages.compatibilityInfoForPackageLocked(ai);
    }

    void enforceNotIsolatedCaller(String caller) {
        if (UserHandle.isIsolated(Binder.getCallingUid())) {
            throw new SecurityException("Isolated process not allowed to call " + caller);
        }
    }

    void enforceShellRestriction(String restriction, int userHandle) {
        if (Binder.getCallingUid() != 2000) {
            return;
        }
        if (userHandle < 0 || this.mUserController.hasUserRestriction(restriction, userHandle)) {
            throw new SecurityException("Shell does not have permission to access user " + userHandle);
        }
    }

    public int getFrontActivityScreenCompatMode() {
        int frontActivityScreenCompatModeLocked;
        enforceNotIsolatedCaller("getFrontActivityScreenCompatMode");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                frontActivityScreenCompatModeLocked = this.mCompatModePackages.getFrontActivityScreenCompatModeLocked();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return frontActivityScreenCompatModeLocked;
    }

    public void setFrontActivityScreenCompatMode(int mode) {
        enforceCallingPermission("android.permission.SET_SCREEN_COMPATIBILITY", "setFrontActivityScreenCompatMode");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mCompatModePackages.setFrontActivityScreenCompatModeLocked(mode);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public int getPackageScreenCompatMode(String packageName) {
        enforceNotIsolatedCaller("getPackageScreenCompatMode");
        if (packageName == null) {
            this.mCompatModePackages.loadCompatModeAppList();
        }
        return this.mCompatModePackages.getPackageScreenCompatModeLocked(packageName);
    }

    public void setPackageScreenCompatMode(String packageName, int mode) {
        enforceCallingPermission("android.permission.SET_SCREEN_COMPATIBILITY", "setPackageScreenCompatMode");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mCompatModePackages.setPackageScreenCompatModeLocked(packageName, mode);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean getPackageAskScreenCompat(String packageName) {
        boolean packageAskCompatModeLocked;
        enforceNotIsolatedCaller("getPackageAskScreenCompat");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                packageAskCompatModeLocked = this.mCompatModePackages.getPackageAskCompatModeLocked(packageName);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return packageAskCompatModeLocked;
    }

    public void setPackageAskScreenCompat(String packageName, boolean ask) {
        enforceCallingPermission("android.permission.SET_SCREEN_COMPATIBILITY", "setPackageAskScreenCompat");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mCompatModePackages.setPackageAskCompatModeLocked(packageName, ask);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private boolean hasUsageStatsPermission(String callingPackage) {
        boolean z = true;
        int mode = this.mAppOpsService.checkOperation(43, Binder.getCallingUid(), callingPackage);
        if (mode == 3) {
            if (checkCallingPermission("android.permission.PACKAGE_USAGE_STATS") != 0) {
                z = false;
            }
            return z;
        }
        if (mode != 0) {
            z = false;
        }
        return z;
    }

    public int getPackageProcessState(String packageName, String callingPackage) {
        if (!hasUsageStatsPermission(callingPackage)) {
            enforceCallingPermission("android.permission.GET_PACKAGE_IMPORTANCE", "getPackageProcessState");
        }
        int procState = -1;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
                    ProcessRecord proc = (ProcessRecord) this.mLruProcesses.get(i);
                    if (procState == -1 || procState > proc.setProcState) {
                        int j;
                        boolean found = false;
                        for (j = proc.pkgList.size() - 1; j >= 0 && !r0; j--) {
                            if (((String) proc.pkgList.keyAt(j)).equals(packageName)) {
                                procState = proc.setProcState;
                                found = true;
                            }
                        }
                        if (proc.pkgDeps != null && !r0) {
                            for (j = proc.pkgDeps.size() - 1; j >= 0; j--) {
                                if (((String) proc.pkgDeps.valueAt(j)).equals(packageName)) {
                                    procState = proc.setProcState;
                                    break;
                                }
                            }
                        }
                    }
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return procState;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean setProcessMemoryTrimLevel(String process, int userId, int level) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ProcessRecord app = findProcessLocked(process, userId, "setProcessMemoryTrimLevel");
                if (app == null) {
                } else if (app.trimMemoryLevel < level && app.thread != null && (level < 20 || app.curProcState >= 7)) {
                    try {
                        app.thread.scheduleTrimMemory(level);
                        app.trimMemoryLevel = level;
                        resetPriorityAfterLockedSection();
                        return true;
                    } catch (RemoteException e) {
                        resetPriorityAfterLockedSection();
                        return false;
                    }
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private void dispatchProcessesChanged() {
        synchronized (this) {
            try {
                int j;
                boostPriorityForLockedSection();
                int N = this.mPendingProcessChanges.size();
                if (this.mActiveProcessChanges.length < N) {
                    this.mActiveProcessChanges = new ProcessChangeItem[N];
                }
                this.mPendingProcessChanges.toArray(this.mActiveProcessChanges);
                this.mPendingProcessChanges.clear();
                if (ActivityManagerDebugConfig.DEBUG_PROCESS_OBSERVERS) {
                    Slog.i(TAG_PROCESS_OBSERVERS, "*** Delivering " + N + " process changes");
                }
                int i = this.mProcessObservers.beginBroadcast();
                while (i > 0) {
                    i--;
                    IProcessObserver observer = (IProcessObserver) this.mProcessObservers.getBroadcastItem(i);
                    if (observer != null) {
                        j = 0;
                        while (j < N) {
                            try {
                                ProcessChangeItem item = this.mActiveProcessChanges[j];
                                if ((item.changes & 1) != 0) {
                                    if (ActivityManagerDebugConfig.DEBUG_PROCESS_OBSERVERS) {
                                        Slog.i(TAG_PROCESS_OBSERVERS, "ACTIVITIES CHANGED pid=" + item.pid + " uid=" + item.uid + ": " + item.foregroundActivities);
                                    }
                                    observer.onForegroundActivitiesChanged(item.pid, item.uid, item.foregroundActivities);
                                }
                                if ((item.changes & 2) != 0) {
                                    if (ActivityManagerDebugConfig.DEBUG_PROCESS_OBSERVERS) {
                                        Slog.i(TAG_PROCESS_OBSERVERS, "PROCSTATE CHANGED pid=" + item.pid + " uid=" + item.uid + ": " + item.processState);
                                    }
                                    observer.onProcessStateChanged(item.pid, item.uid, item.processState);
                                }
                                j++;
                            } catch (RemoteException e) {
                            }
                        }
                    }
                }
                this.mProcessObservers.finishBroadcast();
                synchronized (this) {
                    try {
                        boostPriorityForLockedSection();
                        for (j = 0; j < N; j++) {
                            this.mAvailProcessChanges.add(this.mActiveProcessChanges[j]);
                        }
                    } finally {
                        resetPriorityAfterLockedSection();
                    }
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private void dispatchProcessDied(int pid, int uid) {
        int i = this.mProcessObservers.beginBroadcast();
        while (i > 0) {
            i--;
            IProcessObserver observer = (IProcessObserver) this.mProcessObservers.getBroadcastItem(i);
            if (observer != null) {
                try {
                    observer.onProcessDied(pid, uid);
                } catch (RemoteException e) {
                }
            }
        }
        this.mProcessObservers.finishBroadcast();
    }

    private void dispatchUidsChanged() {
        synchronized (this) {
            try {
                int i;
                int j;
                ChangeItem item;
                boostPriorityForLockedSection();
                int N = this.mPendingUidChanges.size();
                if (this.mActiveUidChanges.length < N) {
                    this.mActiveUidChanges = new ChangeItem[N];
                }
                for (i = 0; i < N; i++) {
                    ChangeItem change = (ChangeItem) this.mPendingUidChanges.get(i);
                    this.mActiveUidChanges[i] = change;
                    if (change.uidRecord != null) {
                        change.uidRecord.pendingChange = null;
                        change.uidRecord = null;
                    }
                }
                this.mPendingUidChanges.clear();
                if (ActivityManagerDebugConfig.DEBUG_UID_OBSERVERS) {
                    Slog.i(TAG_UID_OBSERVERS, "*** Delivering " + N + " uid changes");
                }
                if (this.mLocalPowerManager != null) {
                    for (j = 0; j < N; j++) {
                        item = this.mActiveUidChanges[j];
                        if (item.change == 1 || item.change == 2) {
                            this.mLocalPowerManager.uidGone(item.uid);
                        } else {
                            this.mLocalPowerManager.updateUidProcState(item.uid, item.processState);
                        }
                    }
                }
                i = this.mUidObservers.beginBroadcast();
                while (i > 0) {
                    i--;
                    IUidObserver observer = (IUidObserver) this.mUidObservers.getBroadcastItem(i);
                    int which = ((Integer) this.mUidObservers.getBroadcastCookie(i)).intValue();
                    if (observer != null) {
                        j = 0;
                        while (j < N) {
                            try {
                                item = this.mActiveUidChanges[j];
                                int change2 = item.change;
                                UidRecord uidRecord = null;
                                if (i == 0) {
                                    uidRecord = (UidRecord) this.mValidateUids.get(item.uid);
                                    if (!(uidRecord != null || change2 == 1 || change2 == 2)) {
                                        uidRecord = new UidRecord(item.uid);
                                        this.mValidateUids.put(item.uid, uidRecord);
                                    }
                                }
                                if (change2 == 3 || change2 == 2) {
                                    if ((which & 4) != 0) {
                                        if (ActivityManagerDebugConfig.DEBUG_UID_OBSERVERS) {
                                            Slog.i(TAG_UID_OBSERVERS, "UID idle uid=" + item.uid);
                                        }
                                        observer.onUidIdle(item.uid);
                                    }
                                    if (i == 0 && uidRecord != null) {
                                        uidRecord.idle = true;
                                    }
                                } else if (change2 == 4) {
                                    if ((which & 8) != 0) {
                                        if (ActivityManagerDebugConfig.DEBUG_UID_OBSERVERS) {
                                            Slog.i(TAG_UID_OBSERVERS, "UID active uid=" + item.uid);
                                        }
                                        observer.onUidActive(item.uid);
                                    }
                                    if (i == 0) {
                                        uidRecord.idle = false;
                                    }
                                }
                                if (change2 == 1 || change2 == 2) {
                                    if ((which & 2) != 0) {
                                        if (ActivityManagerDebugConfig.DEBUG_UID_OBSERVERS) {
                                            Slog.i(TAG_UID_OBSERVERS, "UID gone uid=" + item.uid);
                                        }
                                        observer.onUidGone(item.uid);
                                    }
                                    if (i == 0 && uidRecord != null) {
                                        this.mValidateUids.remove(item.uid);
                                    }
                                } else {
                                    if ((which & 1) != 0) {
                                        if (ActivityManagerDebugConfig.DEBUG_UID_OBSERVERS) {
                                            Slog.i(TAG_UID_OBSERVERS, "UID CHANGED uid=" + item.uid + ": " + item.processState);
                                        }
                                        observer.onUidStateChanged(item.uid, item.processState);
                                    }
                                    if (i == 0) {
                                        int i2 = item.processState;
                                        uidRecord.setProcState = i2;
                                        uidRecord.curProcState = i2;
                                    }
                                }
                                j++;
                            } catch (RemoteException e) {
                            }
                        }
                    }
                }
                this.mUidObservers.finishBroadcast();
                synchronized (this) {
                    try {
                        boostPriorityForLockedSection();
                        for (j = 0; j < N; j++) {
                            this.mAvailUidChanges.add(this.mActiveUidChanges[j]);
                        }
                    } finally {
                        resetPriorityAfterLockedSection();
                    }
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public final int startActivity(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle bOptions) {
        return startActivityAsUser(caller, callingPackage, intent, resolvedType, resultTo, resultWho, requestCode, startFlags, profilerInfo, bOptions, UserHandle.getCallingUserId());
    }

    final int startActivity(Intent intent, ActivityContainer container) {
        enforceNotIsolatedCaller("ActivityContainer.startActivity");
        int userId = this.mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), this.mStackSupervisor.mCurrentUser, false, 2, "ActivityContainer", null);
        String mimeType = intent.getType();
        Uri data = intent.getData();
        if (mimeType == null && data != null && "content".equals(data.getScheme())) {
            mimeType = getProviderMimeType(data, userId);
        }
        container.checkEmbeddedAllowedInner(userId, intent, mimeType);
        intent.addFlags(402718720);
        return this.mActivityStarter.startActivityMayWait(null, -1, null, intent, mimeType, null, null, null, null, 0, 0, null, null, null, null, false, userId, container, null);
    }

    public int startActivityAsUser(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, int userId) {
        enforceNotIsolatedCaller("startActivity");
        return this.mActivityStarter.startActivityMayWait(caller, -1, callingPackage, intent, resolvedType, null, null, resultTo, resultWho, requestCode, startFlags, profilerInfo, null, null, bOptions, false, this.mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, 2, "startActivity", null), null, null);
    }

    public final int startActivityAsCaller(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, boolean ignoreTargetSecurity, int userId) {
        synchronized (this) {
            ActivityRecord sourceRecord;
            try {
                boostPriorityForLockedSection();
                if (resultTo == null) {
                    throw new SecurityException("Must be called from an activity");
                }
                sourceRecord = this.mStackSupervisor.isInAnyStackLocked(resultTo);
                if (sourceRecord == null) {
                    throw new SecurityException("Called with bad activity token: " + resultTo);
                } else if (!sourceRecord.info.packageName.equals("android")) {
                    throw new SecurityException("Must be called from an activity that is declared in the android package");
                } else if (sourceRecord.app == null) {
                    throw new SecurityException("Called without a process attached to activity");
                } else if (UserHandle.getAppId(sourceRecord.app.uid) == 1000 || sourceRecord.app.uid == sourceRecord.launchedFromUid) {
                    if (ignoreTargetSecurity) {
                        if (intent.getComponent() == null) {
                            throw new SecurityException("Component must be specified with ignoreTargetSecurity");
                        } else if (intent.getSelector() != null) {
                            throw new SecurityException("Selector not allowed with ignoreTargetSecurity");
                        }
                    }
                    int targetUid = sourceRecord.launchedFromUid;
                    String targetPackage = sourceRecord.launchedFromPackage;
                } else {
                    throw new SecurityException("Calling activity in uid " + sourceRecord.app.uid + " must be system uid or original calling uid " + sourceRecord.launchedFromUid);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        if (userId == -10000) {
            userId = UserHandle.getUserId(sourceRecord.app.uid);
        }
        try {
            return this.mActivityStarter.startActivityMayWait(null, targetUid, targetPackage, intent, resolvedType, null, null, resultTo, resultWho, requestCode, startFlags, null, null, null, bOptions, ignoreTargetSecurity, userId, null, null);
        } catch (SecurityException e) {
            throw e;
        }
    }

    public final WaitResult startActivityAndWait(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, int userId) {
        enforceNotIsolatedCaller("startActivityAndWait");
        userId = this.mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, 2, "startActivityAndWait", null);
        WaitResult res = new WaitResult();
        this.mActivityStarter.startActivityMayWait(caller, -1, callingPackage, intent, resolvedType, null, null, resultTo, resultWho, requestCode, startFlags, profilerInfo, res, null, bOptions, false, userId, null, null);
        return res;
    }

    public final int startActivityWithConfig(IApplicationThread caller, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, Configuration config, Bundle bOptions, int userId) {
        enforceNotIsolatedCaller("startActivityWithConfig");
        return this.mActivityStarter.startActivityMayWait(caller, -1, callingPackage, intent, resolvedType, null, null, resultTo, resultWho, requestCode, startFlags, null, null, config, bOptions, false, this.mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, 2, "startActivityWithConfig", null), null, null);
    }

    public int startActivityIntentSender(IApplicationThread caller, IntentSender intent, Intent fillInIntent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flagsMask, int flagsValues, Bundle bOptions) throws TransactionTooLargeException {
        enforceNotIsolatedCaller("startActivityIntentSender");
        if (fillInIntent == null || !fillInIntent.hasFileDescriptors()) {
            IIntentSender sender = intent.getTarget();
            if (sender instanceof PendingIntentRecord) {
                PendingIntentRecord pir = (PendingIntentRecord) sender;
                synchronized (this) {
                    try {
                        boostPriorityForLockedSection();
                        ActivityStack stack = getFocusedStack();
                        if (stack.mResumedActivity != null && stack.mResumedActivity.info.applicationInfo.uid == Binder.getCallingUid()) {
                            this.mAppSwitchesAllowedTime = 0;
                        }
                    } finally {
                        resetPriorityAfterLockedSection();
                    }
                }
                return pir.sendInner(0, fillInIntent, resolvedType, null, null, resultTo, resultWho, requestCode, flagsMask, flagsValues, bOptions, null);
            }
            throw new IllegalArgumentException("Bad PendingIntent object");
        }
        throw new IllegalArgumentException("File descriptors passed in Intent");
    }

    public int startVoiceActivity(String callingPackage, int callingPid, int callingUid, Intent intent, String resolvedType, IVoiceInteractionSession session, IVoiceInteractor interactor, int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, int userId) {
        if (checkCallingPermission("android.permission.BIND_VOICE_INTERACTION") != 0) {
            String msg = "Permission Denial: startVoiceActivity() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.BIND_VOICE_INTERACTION";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        } else if (session == null || interactor == null) {
            throw new NullPointerException("null session or interactor");
        } else {
            return this.mActivityStarter.startActivityMayWait(null, callingUid, callingPackage, intent, resolvedType, session, interactor, null, null, 0, startFlags, profilerInfo, null, null, bOptions, false, this.mUserController.handleIncomingUser(callingPid, callingUid, userId, false, 2, "startVoiceActivity", null), null, null);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startLocalVoiceInteraction(IBinder callingActivity, Bundle options) throws RemoteException {
        Slog.i(TAG, "Activity tried to startVoiceInteraction");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord activity = getFocusedStack().topActivity();
                if (ActivityRecord.forTokenLocked(callingActivity) != activity) {
                    throw new SecurityException("Only focused activity can call startVoiceInteraction");
                }
                if (this.mRunningVoice == null && activity.task.voiceSession == null) {
                    if (activity.voiceSession == null) {
                        if (activity.pendingVoiceInteractionStart) {
                            Slog.w(TAG, "Pending start of voice interaction already.");
                            resetPriorityAfterLockedSection();
                            return;
                        }
                        activity.pendingVoiceInteractionStart = true;
                        resetPriorityAfterLockedSection();
                        ((VoiceInteractionManagerInternal) LocalServices.getService(VoiceInteractionManagerInternal.class)).startLocalVoiceInteraction(callingActivity, options);
                        return;
                    }
                }
                Slog.w(TAG, "Already in a voice interaction, cannot start new voice interaction");
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void stopLocalVoiceInteraction(IBinder callingActivity) throws RemoteException {
        ((VoiceInteractionManagerInternal) LocalServices.getService(VoiceInteractionManagerInternal.class)).stopLocalVoiceInteraction(callingActivity);
    }

    public boolean supportsLocalVoiceInteraction() throws RemoteException {
        return ((VoiceInteractionManagerInternal) LocalServices.getService(VoiceInteractionManagerInternal.class)).supportsLocalVoiceInteraction();
    }

    void onLocalVoiceInteractionStartedLocked(IBinder activity, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor) {
        ActivityRecord activityToCallback = ActivityRecord.forTokenLocked(activity);
        if (activityToCallback != null) {
            activityToCallback.setVoiceSessionLocked(voiceSession);
            long token;
            try {
                activityToCallback.app.thread.scheduleLocalVoiceInteractionStarted(activity, voiceInteractor);
                token = Binder.clearCallingIdentity();
                startRunningVoiceLocked(voiceSession, activityToCallback.appInfo.uid);
                Binder.restoreCallingIdentity(token);
            } catch (RemoteException e) {
                activityToCallback.clearVoiceSessionLocked();
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public void setVoiceKeepAwake(IVoiceInteractionSession session, boolean keepAwake) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if (this.mRunningVoice != null && this.mRunningVoice.asBinder() == session.asBinder()) {
                    if (keepAwake) {
                        this.mVoiceWakeLock.acquire();
                    } else {
                        this.mVoiceWakeLock.release();
                    }
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean startNextMatchingActivity(IBinder callingActivity, Intent intent, Bundle bOptions) {
        Throwable th;
        if (intent == null || !intent.hasFileDescriptors()) {
            ActivityOptions options = ActivityOptions.fromBundle(bOptions);
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(callingActivity);
                    if (r == null) {
                        ActivityOptions.abort(options);
                        resetPriorityAfterLockedSection();
                        return false;
                    } else if (r.app == null || r.app.thread == null) {
                        ActivityOptions.abort(options);
                        resetPriorityAfterLockedSection();
                        return false;
                    } else {
                        Intent intent2 = new Intent(intent);
                        try {
                            boolean wasFinishing;
                            ActivityRecord resultTo;
                            String resultWho;
                            int requestCode;
                            long origId;
                            int res;
                            intent2.setDataAndType(r.intent.getData(), r.intent.getType());
                            intent2.setComponent(null);
                            boolean debug = (intent2.getFlags() & 8) != 0;
                            ActivityInfo activityInfo = null;
                            try {
                                List<ResolveInfo> resolves = AppGlobals.getPackageManager().queryIntentActivities(intent2, r.resolvedType, 66560, UserHandle.getCallingUserId()).getList();
                                int N = resolves != null ? resolves.size() : 0;
                                for (int i = 0; i < N; i++) {
                                    ResolveInfo rInfo = (ResolveInfo) resolves.get(i);
                                    if (rInfo.activityInfo.packageName.equals(r.packageName) && rInfo.activityInfo.name.equals(r.info.name)) {
                                        i++;
                                        if (i < N) {
                                            activityInfo = ((ResolveInfo) resolves.get(i)).activityInfo;
                                        }
                                        if (debug) {
                                            String str;
                                            Slog.v(TAG, "Next matching activity: found current " + r.packageName + "/" + r.info.name);
                                            String str2 = TAG;
                                            StringBuilder append = new StringBuilder().append("Next matching activity: next is ");
                                            if (activityInfo == null) {
                                                str = "null";
                                            } else {
                                                str = activityInfo.packageName + "/" + activityInfo.name;
                                            }
                                            Slog.v(str2, append.append(str).toString());
                                        }
                                        if (activityInfo != null) {
                                            ActivityOptions.abort(options);
                                            if (debug) {
                                                Slog.d(TAG, "Next matching activity: nothing found");
                                            }
                                            resetPriorityAfterLockedSection();
                                            return false;
                                        }
                                        intent2.setComponent(new ComponentName(activityInfo.applicationInfo.packageName, activityInfo.name));
                                        intent2.setFlags(intent2.getFlags() & -503316481);
                                        wasFinishing = r.finishing;
                                        r.finishing = true;
                                        resultTo = r.resultTo;
                                        resultWho = r.resultWho;
                                        requestCode = r.requestCode;
                                        r.resultTo = null;
                                        if (resultTo != null) {
                                            resultTo.removeResultsLocked(r, resultWho, requestCode);
                                        }
                                        origId = Binder.clearCallingIdentity();
                                        res = this.mActivityStarter.startActivityLocked(r.app.thread, intent2, null, r.resolvedType, activityInfo, null, null, null, resultTo == null ? resultTo.appToken : null, resultWho, requestCode, -1, r.launchedFromUid, r.launchedFromPackage, -1, r.launchedFromUid, 0, options, false, false, null, null, null);
                                        Binder.restoreCallingIdentity(origId);
                                        r.finishing = wasFinishing;
                                        if (res == 0) {
                                            resetPriorityAfterLockedSection();
                                            return false;
                                        }
                                        resetPriorityAfterLockedSection();
                                        return true;
                                    }
                                }
                            } catch (RemoteException e) {
                            }
                            if (activityInfo != null) {
                                intent2.setComponent(new ComponentName(activityInfo.applicationInfo.packageName, activityInfo.name));
                                intent2.setFlags(intent2.getFlags() & -503316481);
                                wasFinishing = r.finishing;
                                r.finishing = true;
                                resultTo = r.resultTo;
                                resultWho = r.resultWho;
                                requestCode = r.requestCode;
                                r.resultTo = null;
                                if (resultTo != null) {
                                    resultTo.removeResultsLocked(r, resultWho, requestCode);
                                }
                                origId = Binder.clearCallingIdentity();
                                if (resultTo == null) {
                                }
                                res = this.mActivityStarter.startActivityLocked(r.app.thread, intent2, null, r.resolvedType, activityInfo, null, null, null, resultTo == null ? resultTo.appToken : null, resultWho, requestCode, -1, r.launchedFromUid, r.launchedFromPackage, -1, r.launchedFromUid, 0, options, false, false, null, null, null);
                                Binder.restoreCallingIdentity(origId);
                                r.finishing = wasFinishing;
                                if (res == 0) {
                                    resetPriorityAfterLockedSection();
                                    return true;
                                }
                                resetPriorityAfterLockedSection();
                                return false;
                            }
                            ActivityOptions.abort(options);
                            if (debug) {
                                Slog.d(TAG, "Next matching activity: nothing found");
                            }
                            resetPriorityAfterLockedSection();
                            return false;
                        } catch (Throwable th2) {
                            th = th2;
                            intent = intent2;
                            resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        throw new IllegalArgumentException("File descriptors passed in Intent");
    }

    public final int startActivityFromRecents(int taskId, Bundle bOptions) {
        if (checkCallingPermission("android.permission.START_TASKS_FROM_RECENTS") != 0) {
            String msg = "Permission Denial: startActivityFromRecents called without android.permission.START_TASKS_FROM_RECENTS";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
        long origId = Binder.clearCallingIdentity();
        try {
            int startActivityFromRecentsInner;
            synchronized (this) {
                boostPriorityForLockedSection();
                Flog.i(101, "startActivityFromRecents: taskId =" + taskId);
                startActivityFromRecentsInner = this.mStackSupervisor.startActivityFromRecentsInner(taskId, bOptions);
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(origId);
            return startActivityFromRecentsInner;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
        }
    }

    final int startActivityInPackage(int uid, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, Bundle bOptions, int userId, IActivityContainer container, TaskRecord inTask) {
        return this.mActivityStarter.startActivityMayWait(null, uid, callingPackage, intent, resolvedType, null, null, resultTo, resultWho, requestCode, startFlags, null, null, null, bOptions, false, this.mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, 2, "startActivityInPackage", null), container, inTask);
    }

    public int startActivities(IApplicationThread caller, String callingPackage, Intent[] intents, String[] resolvedTypes, IBinder resultTo, Bundle bOptions, int userId) {
        enforceNotIsolatedCaller("startActivities");
        return this.mActivityStarter.startActivities(caller, -1, callingPackage, intents, resolvedTypes, resultTo, bOptions, this.mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, 2, "startActivity", null));
    }

    final int startActivitiesInPackage(int uid, String callingPackage, Intent[] intents, String[] resolvedTypes, IBinder resultTo, Bundle bOptions, int userId) {
        return this.mActivityStarter.startActivities(null, uid, callingPackage, intents, resolvedTypes, resultTo, bOptions, this.mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, 2, "startActivityInPackage", null));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void reportActivityFullyDrawn(IBinder token) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                } else {
                    r.reportFullyDrawnLocked();
                    resetPriorityAfterLockedSection();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setRequestedOrientation(IBinder token, int requestedOrientation) {
        IBinder iBinder = null;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                } else {
                    TaskRecord task = r.task;
                    if (task == null || (task.mFullscreen && task.stack.mFullscreen)) {
                        long origId = Binder.clearCallingIdentity();
                        this.mWindowManager.setAppOrientation(r.appToken, requestedOrientation);
                        this.mWindowManager.prepareForForceRotation(r.appToken.asBinder(), r.packageName, r.shortComponentName);
                        WindowManagerService windowManagerService = this.mWindowManager;
                        Configuration configuration = this.mConfiguration;
                        if (r.mayFreezeScreenLocked(r.app)) {
                            iBinder = r.appToken;
                        }
                        Configuration config = windowManagerService.updateOrientationFromAppTokens(configuration, iBinder);
                        if (config != null) {
                            r.frozenBeforeDestroy = true;
                            if (!updateConfigurationLocked(config, r, false)) {
                                this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                            }
                        }
                        Binder.restoreCallingIdentity(origId);
                        resetPriorityAfterLockedSection();
                    }
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getRequestedOrientation(IBinder token) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    int appOrientation = this.mWindowManager.getAppOrientation(r.appToken);
                    resetPriorityAfterLockedSection();
                    return appOrientation;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public final boolean finishActivity(IBinder token, int resultCode, Intent resultData, int finishTask) {
        if (resultData == null || !resultData.hasFileDescriptors()) {
            synchronized (this) {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                    resetPriorityAfterLockedSection();
                    return true;
                }
                TaskRecord tr = r.task;
                ActivityRecord rootR = tr.getRootActivity();
                if (rootR == null) {
                    Slog.w(TAG, "Finishing task with all activities already finished");
                }
                if (tr.mLockTaskAuth != 4 && rootR == r && this.mStackSupervisor.isLastLockedTask(tr)) {
                    Slog.i(TAG, "Not finishing task in lock task mode");
                    this.mStackSupervisor.showLockTaskToast();
                    resetPriorityAfterLockedSection();
                    return false;
                }
                boolean res;
                try {
                    if (this.mController != null) {
                        ActivityRecord next = r.task.stack.topRunningActivityLocked(token, 0);
                        if (next != null) {
                            boolean resumeOK = true;
                            resumeOK = this.mController.activityResuming(next.packageName);
                            if (!resumeOK) {
                                Slog.i(TAG, "Not finishing activity because controller resumed");
                                resetPriorityAfterLockedSection();
                                return false;
                            }
                        }
                    }
                } catch (RemoteException e) {
                    this.mController = null;
                    Watchdog.getInstance().setActivityController(null);
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                }
                long origId = Binder.clearCallingIdentity();
                boolean finishWithRootActivity = finishTask == 1;
                if (finishTask == 2 || (finishWithRootActivity && r == rootR)) {
                    res = removeTaskByIdLocked(tr.taskId, false, finishWithRootActivity);
                    if (!res) {
                        Slog.i(TAG, "Removing task failed to finish activity");
                    }
                } else {
                    res = tr.stack.requestFinishActivityLocked(token, resultCode, resultData, "app-request", true);
                    if (!res) {
                        Slog.i(TAG, "Failed to finish by app-request");
                    }
                }
                Binder.restoreCallingIdentity(origId);
                resetPriorityAfterLockedSection();
                return res;
            }
        }
        throw new IllegalArgumentException("File descriptors passed in Intent");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void finishHeavyWeightApp() {
        if (checkCallingPermission("android.permission.FORCE_STOP_PACKAGES") != 0) {
            String msg = "Permission Denial: finishHeavyWeightApp() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.FORCE_STOP_PACKAGES";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if (this.mHeavyWeightProcess == null) {
                } else {
                    ArrayList<ActivityRecord> activities = new ArrayList(this.mHeavyWeightProcess.activities);
                    for (int i = 0; i < activities.size(); i++) {
                        ActivityRecord r = (ActivityRecord) activities.get(i);
                        if (!r.finishing && r.isInStackLocked()) {
                            r.task.stack.finishActivityLocked(r, 0, null, "finish-heavy", true);
                        }
                    }
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(25, this.mHeavyWeightProcess.userId, 0));
                    this.mHeavyWeightProcess = null;
                    resetPriorityAfterLockedSection();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void crashApplication(int uid, int initialPid, String packageName, String message) {
        if (checkCallingPermission("android.permission.FORCE_STOP_PACKAGES") != 0) {
            String msg = "Permission Denial: crashApplication() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.FORCE_STOP_PACKAGES";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mAppErrors.scheduleAppCrashLocked(uid, initialPid, packageName, message);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public final void finishSubActivity(IBinder token, String resultWho, int requestCode) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    r.task.stack.finishSubActivityLocked(r, resultWho, requestCode);
                }
                Binder.restoreCallingIdentity(origId);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean finishActivityAffinity(IBinder token) {
        synchronized (this) {
            long origId;
            try {
                boostPriorityForLockedSection();
                origId = Binder.clearCallingIdentity();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                    Binder.restoreCallingIdentity(origId);
                    resetPriorityAfterLockedSection();
                    return false;
                }
                TaskRecord task = r.task;
                if (task.mLockTaskAuth != 4 && this.mStackSupervisor.isLastLockedTask(task) && task.getRootActivity() == r) {
                    this.mStackSupervisor.showLockTaskToast();
                    Binder.restoreCallingIdentity(origId);
                    resetPriorityAfterLockedSection();
                    return false;
                }
                boolean finishActivityAffinityLocked = task.stack.finishActivityAffinityLocked(r);
                Binder.restoreCallingIdentity(origId);
                resetPriorityAfterLockedSection();
                return finishActivityAffinityLocked;
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void finishVoiceTask(IVoiceInteractionSession session) {
        long origId;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                origId = Binder.clearCallingIdentity();
                this.mStackSupervisor.finishVoiceTask(session);
                Binder.restoreCallingIdentity(origId);
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
    }

    public boolean releaseActivityInstance(IBinder token) {
        long origId;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                origId = Binder.clearCallingIdentity();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                    Binder.restoreCallingIdentity(origId);
                    resetPriorityAfterLockedSection();
                    return false;
                }
                boolean safelyDestroyActivityLocked = r.task.stack.safelyDestroyActivityLocked(r, "app-req");
                Binder.restoreCallingIdentity(origId);
                resetPriorityAfterLockedSection();
                return safelyDestroyActivityLocked;
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void releaseSomeActivities(IApplicationThread appInt) {
        synchronized (this) {
            long origId;
            try {
                boostPriorityForLockedSection();
                origId = Binder.clearCallingIdentity();
                this.mStackSupervisor.releaseSomeActivitiesLocked(getRecordForAppLocked(appInt), "low-mem");
                Binder.restoreCallingIdentity(origId);
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean willActivityBeVisible(IBinder token) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityStack stack = ActivityRecord.getStackLocked(token);
                if (stack != null) {
                    boolean willActivityBeVisibleLocked = stack.willActivityBeVisibleLocked(token);
                } else {
                    resetPriorityAfterLockedSection();
                    return false;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void overridePendingTransition(IBinder token, String packageName, int enterAnim, int exitAnim) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord self = ActivityRecord.isInStackLocked(token);
                if (self == null) {
                } else {
                    long origId = Binder.clearCallingIdentity();
                    if (self.state == ActivityState.RESUMED || self.state == ActivityState.PAUSING) {
                        this.mWindowManager.overridePendingAppTransition(packageName, enterAnim, exitAnim, null);
                    }
                    Binder.restoreCallingIdentity(origId);
                    resetPriorityAfterLockedSection();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setExitPosition(int startX, int startY, int width, int height) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mWindowManager.setExitPosition(startX, startY, width, height);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private void releaseFMChannel() {
        Intent intent = new Intent("android.intent.action.FM");
        intent.putExtra(AudioService.CONNECT_INTENT_KEY_STATE, 0);
        this.mContext.sendBroadcast(intent);
    }

    private final void handleAppDiedLocked(ProcessRecord app, boolean restarting, boolean allowRestart) {
        if ("com.huawei.android.FMRadio".equals(app.processName)) {
            releaseFMChannel();
        }
        if ("com.huawei.screenrecorder".equals(app.processName)) {
            UserInfo ui = getCurrentUser();
            System.putIntForUser(this.mContext.getContentResolver(), "show_touches", 0, ui != null ? ui.id : 0);
        }
        if ("com.huawei.vdrive".equals(app.processName)) {
            ((PowerManager) this.mContext.getSystemService("power")).setMirrorLinkPowerStatus(false);
        }
        setSoundEffectState(true, app.processName, false, null);
        int pid = app.pid;
        if (!(cleanUpApplicationRecordLocked(app, restarting, allowRestart, -1, false) || restarting)) {
            removeLruProcessLocked(app);
            if (pid > 0) {
                ProcessList.remove(pid);
            }
        }
        if (this.mProfileProc == app) {
            clearProfilerLocked();
        }
        boolean hasVisibleActivities = this.mStackSupervisor.handleAppDiedLocked(app);
        app.activities.clear();
        app.hasClientActivities = false;
        clearBroadcastResource(app);
        if (app.instrumentationClass != null) {
            Slog.w(TAG, "Crash of app " + app.processName + " running instrumentation " + app.instrumentationClass);
            Bundle info = new Bundle();
            info.putString("shortMsg", "Process crashed.");
            finishInstrumentationLocked(app, 0, info);
        }
        if (!restarting && hasVisibleActivities && !this.mStackSupervisor.resumeFocusedStackTopActivityLocked()) {
            this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
        }
    }

    private final int getLRURecordIndexForAppLocked(IApplicationThread thread) {
        IBinder threadBinder = thread.asBinder();
        for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
            ProcessRecord rec = (ProcessRecord) this.mLruProcesses.get(i);
            if (rec != null && rec.thread != null && rec.thread.asBinder() == threadBinder) {
                return i;
            }
        }
        return -1;
    }

    final ProcessRecord getRecordForAppLocked(IApplicationThread thread) {
        ProcessRecord processRecord = null;
        if (thread == null) {
            return null;
        }
        int appIndex = getLRURecordIndexForAppLocked(thread);
        if (appIndex >= 0) {
            processRecord = (ProcessRecord) this.mLruProcesses.get(appIndex);
        }
        return processRecord;
    }

    final void doLowMemReportIfNeededLocked(ProcessRecord dyingProc) {
        int i;
        boolean haveBg = false;
        for (i = this.mLruProcesses.size() - 1; i >= 0; i--) {
            ProcessRecord rec = (ProcessRecord) this.mLruProcesses.get(i);
            if (rec.thread != null && rec.setProcState >= 14) {
                haveBg = true;
                break;
            }
        }
        if (!haveBg) {
            long now;
            boolean doReport = "1".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0"));
            if (doReport) {
                now = SystemClock.uptimeMillis();
                if (now < this.mLastMemUsageReportTime + 300000) {
                    doReport = false;
                } else {
                    this.mLastMemUsageReportTime = now;
                }
            }
            ArrayList arrayList = doReport ? new ArrayList(this.mLruProcesses.size()) : null;
            EventLog.writeEvent(EventLogTags.AM_LOW_MEMORY, this.mLruProcesses.size());
            now = SystemClock.uptimeMillis();
            for (i = this.mLruProcesses.size() - 1; i >= 0; i--) {
                rec = (ProcessRecord) this.mLruProcesses.get(i);
                if (!(rec == dyingProc || rec.thread == null)) {
                    if (doReport) {
                        arrayList.add(new ProcessMemInfo(rec.processName, rec.pid, rec.setAdj, rec.setProcState, rec.adjType, rec.makeAdjReason()));
                    }
                    if (rec.lastLowMemory + 60000 <= now) {
                        if (rec.setAdj <= 400) {
                            rec.lastRequestedGc = 0;
                        } else {
                            rec.lastRequestedGc = rec.lastLowMemory;
                        }
                        rec.reportLowMemory = true;
                        rec.lastLowMemory = now;
                        this.mProcessesToGc.remove(rec);
                        addProcessToGcListLocked(rec);
                    }
                }
            }
            if (doReport) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(33, arrayList));
            }
            scheduleAppGcsLocked();
        }
    }

    final void appDiedLocked(ProcessRecord app) {
        appDiedLocked(app, app.pid, app.thread, false);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final void appDiedLocked(ProcessRecord app, int pid, IApplicationThread thread, boolean fromBinderDied) {
        synchronized (this.mPidsSelfLocked) {
            ProcessRecord curProc = (ProcessRecord) this.mPidsSelfLocked.get(pid);
            if (curProc != app) {
                Slog.w(TAG, "Spurious death for " + app + ", curProc for " + pid + ": " + curProc);
            }
        }
    }

    public static File dumpStackTraces(ProcessRecord app, boolean clearTraces, ArrayList<Integer> firstPids, ProcessCpuTracker processCpuTracker, SparseArray<Boolean> lastPids, String[] nativeProcs) {
        boolean isSystemApp = false;
        if (app == null || app.info == null) {
            isSystemApp = true;
        } else if ((app.info.flags & 129) != 0) {
            isSystemApp = true;
        }
        if (isSystemApp || Log.HWINFO) {
            return dumpStackTraces(true, (ArrayList) firstPids, processCpuTracker, (SparseArray) lastPids, Watchdog.NATIVE_STACKS_OF_INTEREST);
        }
        firstPids.clear();
        firstPids.add(Integer.valueOf(app.pid));
        return dumpStackTraces(true, (ArrayList) firstPids, processCpuTracker, (SparseArray) lastPids, null);
    }

    public static File dumpStackTraces(boolean clearTraces, ArrayList<Integer> firstPids, ProcessCpuTracker processCpuTracker, SparseArray<Boolean> lastPids, String[] nativeProcs) {
        String tracesPath = SystemProperties.get("dalvik.vm.stack-trace-file", null);
        if (tracesPath == null || tracesPath.length() == 0) {
            return null;
        }
        File tracesFile = new File(tracesPath);
        if (clearTraces) {
            try {
                if (tracesFile.exists()) {
                    tracesFile.delete();
                }
            } catch (IOException e) {
                Slog.w(TAG, "Unable to prepare ANR traces file: " + tracesPath, e);
                return null;
            }
        }
        tracesFile.createNewFile();
        FileUtils.setPermissions(tracesFile.getPath(), 438, -1, -1);
        dumpStackTraces(tracesPath, (ArrayList) firstPids, processCpuTracker, (SparseArray) lastPids, nativeProcs);
        return tracesFile;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void dumpStackTraces(String tracesPath, ArrayList<Integer> firstPids, ProcessCpuTracker processCpuTracker, SparseArray<Boolean> lastPids, String[] nativeProcs) {
        int i;
        FileObserver observer = new FileObserver(tracesPath, 8) {
            public synchronized void onEvent(int event, String path) {
                notify();
            }
        };
        observer.startWatching();
        if (firstPids != null) {
            try {
                int num = firstPids.size();
                i = 0;
                while (i < num) {
                    synchronized (observer) {
                        long sime = SystemClock.elapsedRealtime();
                        Process.sendSignal(((Integer) firstPids.get(i)).intValue(), 3);
                        observer.wait(1000);
                    }
                }
            } catch (InterruptedException e) {
                Slog.wtf(TAG, e);
            }
        }
        if (nativeProcs != null) {
            int[] pids = Process.getPidsForCommands(nativeProcs);
            if (pids != null) {
                for (int pid : pids) {
                    sime = SystemClock.elapsedRealtime();
                    Debug.dumpNativeBacktraceToFile(pid, tracesPath);
                }
            }
        }
        if (processCpuTracker != null) {
            processCpuTracker.init();
            System.gc();
            processCpuTracker.update();
            try {
                synchronized (processCpuTracker) {
                    processCpuTracker.wait(500);
                }
            } catch (InterruptedException e2) {
            }
            try {
                processCpuTracker.update();
                int N = processCpuTracker.countWorkingStats();
                int numProcs = 0;
                for (i = 0; i < N && numProcs < 5; i++) {
                    Stats stats = processCpuTracker.getWorkingStats(i);
                    if (lastPids.indexOfKey(stats.pid) >= 0) {
                        numProcs++;
                        try {
                            synchronized (observer) {
                                long stime = SystemClock.elapsedRealtime();
                                Process.sendSignal(stats.pid, 3);
                                observer.wait(1000);
                            }
                        } catch (InterruptedException e3) {
                            Slog.wtf(TAG, e3);
                        }
                    }
                }
            } catch (Throwable th) {
                observer.stopWatching();
            }
        }
        IHwBinderMonitor iBinderM = HwServiceFactory.getIHwBinderMonitor();
        if (iBinderM != null) {
            iBinderM.writeTransactonToTrace(tracesPath);
        }
        observer.stopWatching();
    }

    final void logAppTooSlow(ProcessRecord app, long startTime, String msg) {
    }

    final void showLaunchWarningLocked(final ActivityRecord cur, final ActivityRecord next) {
        if (!this.mLaunchWarningShown) {
            this.mLaunchWarningShown = true;
            this.mUiHandler.post(new Runnable() {
                public void run() {
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            final Dialog d = new LaunchWarningWindow(ActivityManagerService.this.mContext, cur, next);
                            d.show();
                            ActivityManagerService.this.mUiHandler.postDelayed(new Runnable() {
                                public void run() {
                                    synchronized (ActivityManagerService.this) {
                                        try {
                                            ActivityManagerService.boostPriorityForLockedSection();
                                            d.dismiss();
                                            ActivityManagerService.this.mLaunchWarningShown = false;
                                        } finally {
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                        }
                                    }
                                }
                            }, 4000);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                }
            });
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean clearApplicationUserData(String packageName, IPackageDataObserver observer, int userId) {
        enforceNotIsolatedCaller("clearApplicationUserData");
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        userId = this.mUserController.handleIncomingUser(pid, uid, userId, false, 2, "clearApplicationUserData", null);
        long callingId = Binder.clearCallingIdentity();
        try {
            IPackageManager pm = AppGlobals.getPackageManager();
            int pkgUid = -1;
            synchronized (this) {
                boostPriorityForLockedSection();
                if (getPackageManagerInternalLocked().canPackageBeWiped(userId, packageName)) {
                    throw new SecurityException("Cannot clear data for a device owner or a profile owner");
                }
                try {
                    pkgUid = pm.getPackageUid(packageName, DumpState.DUMP_PREFERRED_XML, userId);
                } catch (RemoteException e) {
                }
                if (pkgUid == -1) {
                    Slog.w(TAG, "Invalid packageName: " + packageName);
                    if (observer != null) {
                        try {
                            observer.onRemoveCompleted(packageName, false);
                        } catch (RemoteException e2) {
                            Slog.i(TAG, "Observer no longer exists.");
                        }
                    }
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(callingId);
                    return false;
                } else if (uid == pkgUid || checkComponentPermission("android.permission.CLEAR_APP_USER_DATA", pid, uid, -1, true) == 0) {
                    forceStopPackageLocked(packageName, pkgUid, "clear data");
                    for (int i = this.mRecentTasks.size() - 1; i >= 0; i--) {
                        TaskRecord tr = (TaskRecord) this.mRecentTasks.get(i);
                        String taskPackageName = tr.getBaseIntent().getComponent().getPackageName();
                        if (tr.userId == userId && taskPackageName.equals(packageName)) {
                            removeTaskByIdLocked(tr.taskId, false, true);
                        }
                    }
                } else {
                    throw new SecurityException("PID " + pid + " does not have permission " + "android.permission.CLEAR_APP_USER_DATA" + " to clear data" + " of package " + packageName);
                }
            }
            Binder.restoreCallingIdentity(callingId);
            return true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void killBackgroundProcesses(String packageName, int userId) {
        if (checkCallingPermission("android.permission.KILL_BACKGROUND_PROCESSES") == 0 || checkCallingPermission("android.permission.RESTART_PACKAGES") == 0) {
            userId = this.mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, 2, "killBackgroundProcesses", null);
            Slog.i(TAG, "killBackgroundProcesses() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            long callingId = Binder.clearCallingIdentity();
            try {
                IPackageManager pm = AppGlobals.getPackageManager();
                synchronized (this) {
                    boostPriorityForLockedSection();
                    int appId = -1;
                    try {
                        appId = UserHandle.getAppId(pm.getPackageUid(packageName, 268435456, userId));
                    } catch (RemoteException e) {
                    }
                    if (appId == -1) {
                        Slog.w(TAG, "Invalid packageName: " + packageName);
                        resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(callingId);
                        return;
                    }
                    killPackageProcessesLocked(packageName, appId, userId, 500, false, true, true, false, "kill background");
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(callingId);
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(callingId);
            }
        } else {
            String msg = "Permission Denial: killBackgroundProcesses() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.KILL_BACKGROUND_PROCESSES";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
    }

    public void killAllBackgroundProcesses() {
        if (checkCallingPermission("android.permission.KILL_BACKGROUND_PROCESSES") != 0) {
            String msg = "Permission Denial: killAllBackgroundProcesses() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.KILL_BACKGROUND_PROCESSES";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                ArrayList<ProcessRecord> procs = new ArrayList();
                int NP = this.mProcessNames.getMap().size();
                for (int ip = 0; ip < NP; ip++) {
                    SparseArray<ProcessRecord> apps = (SparseArray) this.mProcessNames.getMap().valueAt(ip);
                    int NA = apps.size();
                    for (int ia = 0; ia < NA; ia++) {
                        ProcessRecord app = (ProcessRecord) apps.valueAt(ia);
                        if (!app.persistent) {
                            if (app.removed) {
                                procs.add(app);
                            } else if (app.setAdj >= 900) {
                                app.removed = true;
                                procs.add(app);
                            }
                        }
                    }
                }
                int N = procs.size();
                for (int i = 0; i < N; i++) {
                    removeProcessLocked((ProcessRecord) procs.get(i), false, true, "kill all background");
                }
                this.mAllowLowerMemLevel = true;
                updateOomAdjLocked();
                doLowMemReportIfNeededLocked(null);
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    private void killAllBackgroundProcessesExcept(int minTargetSdk, int maxProcState) {
        if (checkCallingPermission("android.permission.KILL_BACKGROUND_PROCESSES") != 0) {
            String msg = "Permission Denial: killAllBackgroundProcessesExcept() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.KILL_BACKGROUND_PROCESSES";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                ArrayList<ProcessRecord> procs = new ArrayList();
                int NP = this.mProcessNames.getMap().size();
                for (int ip = 0; ip < NP; ip++) {
                    SparseArray<ProcessRecord> apps = (SparseArray) this.mProcessNames.getMap().valueAt(ip);
                    int NA = apps.size();
                    for (int ia = 0; ia < NA; ia++) {
                        ProcessRecord app = (ProcessRecord) apps.valueAt(ia);
                        if (app.removed) {
                            procs.add(app);
                        } else if ((minTargetSdk < 0 || app.info.targetSdkVersion < minTargetSdk) && (maxProcState < 0 || app.setProcState > maxProcState)) {
                            app.removed = true;
                            procs.add(app);
                        }
                    }
                }
                int N = procs.size();
                for (int i = 0; i < N; i++) {
                    removeProcessLocked((ProcessRecord) procs.get(i), false, true, "kill all background except");
                }
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void forceStopPackage(String packageName, int userId) {
        if (checkCallingPermission("android.permission.FORCE_STOP_PACKAGES") != 0) {
            String msg = "Permission Denial: forceStopPackage() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.FORCE_STOP_PACKAGES";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
        int callingPid = Binder.getCallingPid();
        userId = this.mUserController.handleIncomingUser(callingPid, Binder.getCallingUid(), userId, true, 2, "forceStopPackage", null);
        long callingId = Binder.clearCallingIdentity();
        try {
            if (HwDeviceManager.disallowOp(3, packageName)) {
                Slog.i(TAG, "[" + packageName + "] is Persistent app,won't be killed");
                Binder.restoreCallingIdentity(callingId);
                return;
            }
            IPackageManager pm = AppGlobals.getPackageManager();
            synchronized (this) {
                boostPriorityForLockedSection();
                for (int user : userId == -1 ? this.mUserController.getUsers() : new int[]{userId}) {
                    int pkgUid = -1;
                    try {
                        pkgUid = pm.getPackageUid(packageName, 268435456, user);
                    } catch (RemoteException e) {
                    }
                    if (pkgUid == -1) {
                        Slog.w(TAG, "Invalid packageName: " + packageName);
                    } else {
                        try {
                            pm.setPackageStoppedState(packageName, true, user);
                        } catch (RemoteException e2) {
                        } catch (IllegalArgumentException e3) {
                            Slog.w(TAG, "Failed trying to unstop package " + packageName + ": " + e3);
                        }
                        if (this.mUserController.isUserRunningLocked(user, 0)) {
                            forceStopPackageLocked(packageName, pkgUid, "from pid " + callingPid + (callingPid == Process.myPid() ? "" : REASON_STOP_BY_APP));
                            finishForceStopPackageLocked(packageName, pkgUid);
                            reportAppDiedMsg(new AppDiedInfo(userId, packageName, callingPid, "forceStop"));
                        }
                    }
                }
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addPackageDependency(String packageName) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if (Binder.getCallingPid() == Process.myPid()) {
                    resetPriorityAfterLockedSection();
                    return;
                }
                ProcessRecord proc;
                synchronized (this.mPidsSelfLocked) {
                    proc = (ProcessRecord) this.mPidsSelfLocked.get(Binder.getCallingPid());
                }
                if (proc != null) {
                    if (proc.pkgDeps == null) {
                        proc.pkgDeps = new ArraySet(1);
                    }
                    proc.pkgDeps.add(packageName);
                }
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void killApplication(String pkg, int appId, int userId, String reason) {
        if (pkg != null) {
            if (appId < 0) {
                Slog.w(TAG, "Invalid appid specified for pkg : " + pkg);
                return;
            }
            int callerUid = Binder.getCallingUid();
            if (UserHandle.getAppId(callerUid) == 1000) {
                Message msg = this.mHandler.obtainMessage(22);
                msg.arg1 = appId;
                msg.arg2 = userId;
                Bundle bundle = new Bundle();
                bundle.putString(AbsLocationManagerService.DEL_PKG, pkg);
                bundle.putString(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, reason);
                msg.obj = bundle;
                this.mHandler.sendMessage(msg);
                return;
            }
            throw new SecurityException(callerUid + " cannot kill pkg: " + pkg);
        }
    }

    public void closeSystemDialogs(String reason) {
        enforceNotIsolatedCaller("closeSystemDialogs");
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    if (uid >= 10000) {
                        ProcessRecord proc;
                        synchronized (this.mPidsSelfLocked) {
                            proc = (ProcessRecord) this.mPidsSelfLocked.get(pid);
                        }
                        if (proc.curRawAdj > FIRST_BROADCAST_QUEUE_MSG) {
                            Slog.w(TAG, "Ignoring closeSystemDialogs " + reason + " from background process " + proc);
                            resetPriorityAfterLockedSection();
                            Binder.restoreCallingIdentity(origId);
                            return;
                        }
                    }
                    resetPriorityAfterLockedSection();
                    this.mWindowManager.closeSystemDialogs(reason);
                    synchronized (this) {
                        boostPriorityForLockedSection();
                        closeSystemDialogsLocked(reason);
                    }
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                }
            }
        } catch (Throwable th2) {
            Binder.restoreCallingIdentity(origId);
        }
    }

    void closeSystemDialogsLocked(String reason) {
        Intent intent = new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        intent.addFlags(1342177280);
        if (reason != null) {
            intent.putExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, reason);
        }
        this.mStackSupervisor.closeSystemDialogsLocked();
        broadcastIntentLocked(null, null, intent, null, null, 0, null, null, null, -1, null, false, false, -1, 1000, -1);
    }

    public MemoryInfo[] getProcessMemoryInfo(int[] pids) {
        enforceNotIsolatedCaller("getProcessMemoryInfo");
        MemoryInfo[] infos = new MemoryInfo[pids.length];
        for (int i = pids.length - 1; i >= 0; i--) {
            synchronized (this) {
                boostPriorityForLockedSection();
                synchronized (this.mPidsSelfLocked) {
                    ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(pids[i]);
                    int oomAdj = proc != null ? proc.setAdj : 0;
                    try {
                    } catch (Throwable th) {
                        resetPriorityAfterLockedSection();
                    }
                }
            }
            resetPriorityAfterLockedSection();
            infos[i] = new MemoryInfo();
            Debug.getMemoryInfo(pids[i], infos[i]);
            if (proc != null) {
                synchronized (this) {
                    try {
                        boostPriorityForLockedSection();
                        if (proc.thread != null && proc.setAdj == oomAdj) {
                            proc.baseProcessTracker.addPss((long) infos[i].getTotalPss(), (long) infos[i].getTotalUss(), false, proc.pkgList);
                        }
                    } finally {
                        resetPriorityAfterLockedSection();
                    }
                }
            }
        }
        return infos;
    }

    public long[] getProcessPss(int[] pids) {
        enforceNotIsolatedCaller("getProcessPss");
        long[] pss = new long[pids.length];
        for (int i = pids.length - 1; i >= 0; i--) {
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    synchronized (this.mPidsSelfLocked) {
                        ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(pids[i]);
                        int oomAdj = proc != null ? proc.setAdj : 0;
                    }
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                }
            }
            resetPriorityAfterLockedSection();
            long[] tmpUss = new long[]{Debug.getPss(pids[i], tmpUss, null)};
            if (proc != null) {
                synchronized (this) {
                    try {
                        boostPriorityForLockedSection();
                        if (proc.thread != null && proc.setAdj == oomAdj) {
                            proc.baseProcessTracker.addPss(pss[i], tmpUss[0], false, proc.pkgList);
                        }
                    } finally {
                        resetPriorityAfterLockedSection();
                    }
                }
            }
        }
        return pss;
    }

    public void killApplicationProcess(String processName, int uid) {
        if (processName != null) {
            int callerUid = Binder.getCallingUid();
            if (callerUid == 1000) {
                synchronized (this) {
                    try {
                        boostPriorityForLockedSection();
                        ProcessRecord app = getProcessRecordLocked(processName, uid, true);
                        if (app == null || app.thread == null) {
                            Slog.w(TAG, "Process/uid not found attempting kill of " + processName + " / " + uid);
                        } else {
                            try {
                                app.thread.scheduleSuicide();
                            } catch (RemoteException e) {
                            }
                        }
                    } finally {
                        resetPriorityAfterLockedSection();
                    }
                }
                return;
            }
            throw new SecurityException(callerUid + " cannot kill app process: " + processName);
        }
    }

    private void forceStopPackageLocked(String packageName, int uid, String reason) {
        forceStopPackageLocked(packageName, UserHandle.getAppId(uid), false, false, true, false, false, UserHandle.getUserId(uid), reason);
    }

    private void finishForceStopPackageLocked(String packageName, int uid) {
        Intent intent = new Intent("android.intent.action.PACKAGE_RESTARTED", Uri.fromParts(HwBroadcastRadarUtil.KEY_PACKAGE, packageName, null));
        if (!this.mProcessesReady) {
            intent.addFlags(1342177280);
        }
        intent.putExtra("android.intent.extra.UID", uid);
        intent.putExtra("android.intent.extra.user_handle", UserHandle.getUserId(uid));
        broadcastIntentLocked(null, null, intent, null, null, 0, null, null, null, -1, null, false, false, MY_PID, 1000, UserHandle.getUserId(uid));
    }

    final boolean killPackageProcessesLocked(String packageName, int appId, int userId, int minOomAdj, boolean callerWillRestart, boolean allowRestart, boolean doit, boolean evenPersistent, String reason) {
        ArrayList<ProcessRecord> procs = new ArrayList();
        int NP = this.mProcessNames.getMap().size();
        for (int ip = 0; ip < NP; ip++) {
            SparseArray<ProcessRecord> apps = (SparseArray) this.mProcessNames.getMap().valueAt(ip);
            int NA = apps.size();
            int ia = 0;
            while (ia < NA) {
                ProcessRecord app = (ProcessRecord) apps.valueAt(ia);
                try {
                    if (reason.endsWith(REASON_CLONED_APP_DELETED) && app.info.euid == 0) {
                        ia++;
                    } else {
                        if (reason.endsWith(REASON_SYS_REPLACE) && app.info.packageName.equals(packageName)) {
                            app.persistent = false;
                        }
                        if (!app.persistent || evenPersistent) {
                            if (app.removed) {
                                if (doit) {
                                    procs.add(app);
                                }
                            } else if (app.setAdj < minOomAdj) {
                                continue;
                            } else {
                                if (packageName == null) {
                                    if (userId != -1) {
                                        if (app.userId != userId) {
                                            continue;
                                        }
                                    }
                                    if (appId >= 0 && UserHandle.getAppId(app.uid) != appId) {
                                    }
                                } else {
                                    boolean isDep;
                                    if (app.pkgDeps != null) {
                                        isDep = app.pkgDeps.contains(packageName);
                                    } else {
                                        isDep = false;
                                    }
                                    if ((isDep || UserHandle.getAppId(app.uid) == appId) && ((userId == -1 || app.userId == userId) && (app.pkgList.containsKey(packageName) || isDep))) {
                                        if (app == this.mHomeProcess && isDep) {
                                            if (reason.endsWith(REASON_STOP_BY_APP)) {
                                                Slog.i(TAG, "Don't kill current launcher!");
                                            }
                                        }
                                    }
                                }
                                if (!doit) {
                                    return true;
                                }
                                app.removed = true;
                                procs.add(app);
                            }
                            ia++;
                        } else {
                            ia++;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        int N = procs.size();
        for (int i = 0; i < N; i++) {
            removeProcessLocked((ProcessRecord) procs.get(i), callerWillRestart, allowRestart, reason);
        }
        updateOomAdjLocked();
        return N > 0;
    }

    private void cleanupDisabledPackageComponentsLocked(String packageName, int userId, boolean killProcess, String[] changedClasses) {
        Set<String> disabledClasses = null;
        boolean packageDisabled = false;
        IPackageManager pm = AppGlobals.getPackageManager();
        if (changedClasses != null) {
            int i;
            Set set;
            for (i = changedClasses.length - 1; i >= 0; i--) {
                String changedClass = changedClasses[i];
                int enabled;
                if (changedClass.equals(packageName)) {
                    try {
                        enabled = pm.getApplicationEnabledSetting(packageName, userId != -1 ? userId : 0);
                        packageDisabled = enabled != 1 ? enabled != 0 : false;
                        if (packageDisabled) {
                            set = null;
                            break;
                        }
                    } catch (Exception e) {
                        return;
                    }
                }
                try {
                    enabled = pm.getComponentEnabledSetting(new ComponentName(packageName, changedClass), userId != -1 ? userId : 0);
                    if (!(enabled == 1 || enabled == 0)) {
                        if (disabledClasses == null) {
                            disabledClasses = new ArraySet(changedClasses.length);
                        }
                        disabledClasses.add(changedClass);
                    }
                } catch (Exception e2) {
                    return;
                }
            }
            if (packageDisabled || set != null) {
                if (this.mStackSupervisor.finishDisabledPackageActivitiesLocked(packageName, set, true, false, userId) && this.mBooted) {
                    this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                    this.mStackSupervisor.scheduleIdleLocked();
                }
                cleanupDisabledPackageTasksLocked(packageName, set, userId);
                this.mServices.bringDownDisabledPackageServicesLocked(packageName, set, userId, false, killProcess, true);
                ArrayList providers = new ArrayList();
                this.mProviderMap.collectPackageProvidersLocked(packageName, set, true, false, userId, providers);
                ArrayList providersForClone = new ArrayList();
                this.mProviderMapForClone.collectPackageProvidersLocked(packageName, set, true, false, userId, providersForClone);
                providers.addAll(providersForClone);
                for (i = providers.size() - 1; i >= 0; i--) {
                    removeDyingProviderLocked(null, (ContentProviderRecord) providers.get(i), true);
                }
                for (i = this.mBroadcastQueues.length - 1; i >= 0; i--) {
                    this.mBroadcastQueues[i].cleanupDisabledPackageReceiversLocked(packageName, set, userId, true);
                }
            }
        }
    }

    final boolean clearBroadcastQueueForUserLocked(int userId) {
        boolean didSomething = false;
        for (int i = this.mBroadcastQueues.length - 1; i >= 0; i--) {
            didSomething |= this.mBroadcastQueues[i].cleanupDisabledPackageReceiversLocked(null, null, userId, true);
        }
        return didSomething;
    }

    final boolean forceStopPackageLocked(String packageName, int appId, boolean callerWillRestart, boolean purgeCache, boolean doit, boolean evenPersistent, boolean uninstalling, int userId, String reason) {
        int i;
        if (userId == -1 && packageName == null) {
            Slog.w(TAG, "Can't force stop all processes of all users, that is insane!");
        }
        if (appId < 0 && packageName != null) {
            try {
                appId = UserHandle.getAppId(AppGlobals.getPackageManager().getPackageUid(packageName, 268435456, 0));
            } catch (RemoteException e) {
            }
        }
        if (doit) {
            if (packageName != null) {
                Slog.i(TAG, "Force stopping " + packageName + " appid=" + appId + " user=" + userId + ": " + reason);
            } else {
                Slog.i(TAG, "Force stopping u" + userId + ": " + reason);
            }
            this.mAppErrors.resetProcessCrashTimeLocked(packageName == null, appId, userId);
        }
        boolean killPackageProcessesLocked = killPackageProcessesLocked(packageName, appId, userId, -10000, callerWillRestart, true, doit, evenPersistent, packageName == null ? "stop user " + userId + reason : "stop " + packageName + reason);
        if (this.mStackSupervisor.finishDisabledPackageActivitiesLocked(packageName, null, doit, evenPersistent, userId)) {
            if (!doit) {
                return true;
            }
            killPackageProcessesLocked = true;
        }
        if (this.mServices.bringDownDisabledPackageServicesLocked(packageName, null, userId, evenPersistent, true, doit)) {
            if (!doit) {
                return true;
            }
            killPackageProcessesLocked = true;
        }
        if (packageName == null) {
            this.mStickyBroadcasts.remove(userId);
        }
        ArrayList providers = new ArrayList();
        if (this.mProviderMap.collectPackageProvidersLocked(packageName, null, doit, evenPersistent, userId, providers)) {
            if (!doit) {
                return true;
            }
            killPackageProcessesLocked = true;
        }
        ArrayList providersForClone = new ArrayList();
        this.mProviderMapForClone.collectPackageProvidersLocked(packageName, null, doit, evenPersistent, userId, providersForClone);
        providers.addAll(providersForClone);
        for (i = providers.size() - 1; i >= 0; i--) {
            removeDyingProviderLocked(null, (ContentProviderRecord) providers.get(i), true);
        }
        removeUriPermissionsForPackageLocked(packageName, userId, false);
        if (doit) {
            for (i = this.mBroadcastQueues.length - 1; i >= 0; i--) {
                killPackageProcessesLocked |= this.mBroadcastQueues[i].cleanupDisabledPackageReceiversLocked(packageName, null, userId, doit);
            }
        }
        if ((packageName == null || uninstalling) && this.mIntentSenderRecords.size() > 0) {
            Iterator<WeakReference<PendingIntentRecord>> it = this.mIntentSenderRecords.values().iterator();
            while (it.hasNext()) {
                WeakReference<PendingIntentRecord> wpir = (WeakReference) it.next();
                if (wpir == null) {
                    it.remove();
                } else {
                    PendingIntentRecord pir = (PendingIntentRecord) wpir.get();
                    if (pir == null) {
                        it.remove();
                    } else {
                        if (packageName == null) {
                            if (pir.key.userId != userId) {
                                continue;
                            }
                        } else if (UserHandle.getAppId(pir.uid) == appId && (userId == -1 || pir.key.userId == userId)) {
                            if (!pir.key.packageName.equals(packageName)) {
                            }
                        }
                        if (!doit) {
                            return true;
                        }
                        int didSomething = 1;
                        it.remove();
                        pir.canceled = true;
                        if (!(pir.key.activity == null || pir.key.activity.pendingResults == null || pir.key.activity.pendingResults.size() <= 0)) {
                            pir.key.activity.pendingResults.remove(pir.ref);
                        }
                    }
                }
            }
        }
        if (doit) {
            if (purgeCache && packageName != null) {
                AttributeCache ac = AttributeCache.instance();
                if (ac != null) {
                    ac.removePackage(packageName);
                }
            }
            if (this.mBooted) {
                this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                this.mStackSupervisor.scheduleIdleLocked();
            }
        }
        return killPackageProcessesLocked;
    }

    private final ProcessRecord removeProcessNameLocked(String name, int uid) {
        ProcessRecord old = (ProcessRecord) this.mProcessNames.remove(name, uid);
        if (old != null) {
            UidRecord uidRecord = old.uidRecord;
            uidRecord.numProcs--;
            if (old.uidRecord.numProcs == 0) {
                if (ActivityManagerDebugConfig.DEBUG_UID_OBSERVERS) {
                    Slog.i(TAG_UID_OBSERVERS, "No more processes in " + old.uidRecord);
                }
                enqueueUidChangeLocked(old.uidRecord, -1, 1);
                this.mActiveUids.remove(uid);
                noteUidProcessState(uid, -1);
            }
            old.uidRecord = null;
        }
        this.mIsolatedProcesses.remove(uid);
        return old;
    }

    private final void addProcessNameLocked(ProcessRecord proc) {
        ProcessRecord old = removeProcessNameLocked(proc.processName, proc.uid + proc.info.euid);
        if (old == proc && proc.persistent) {
            Slog.w(TAG, "Re-adding persistent process " + proc);
        } else if (old != null) {
            Slog.wtf(TAG, "Already have existing proc " + old + " when adding " + proc);
        }
        UidRecord uidRec = (UidRecord) this.mActiveUids.get(proc.uid);
        if (uidRec == null) {
            uidRec = new UidRecord(proc.uid);
            if (ActivityManagerDebugConfig.DEBUG_UID_OBSERVERS) {
                Slog.i(TAG_UID_OBSERVERS, "Creating new process uid: " + uidRec);
            }
            this.mActiveUids.put(proc.uid, uidRec);
            noteUidProcessState(uidRec.uid, uidRec.curProcState);
            enqueueUidChangeLocked(uidRec, -1, 4);
        }
        proc.uidRecord = uidRec;
        uidRec.numProcs++;
        this.mProcessNames.put(proc.processName, proc.uid + proc.info.euid, proc);
        if (proc.isolated) {
            this.mIsolatedProcesses.put(proc.uid, proc);
        }
    }

    protected boolean removeProcessLocked(ProcessRecord app, boolean callerWillRestart, boolean allowRestart, String reason) {
        String name = app.processName;
        int uid = app.uid;
        if (ActivityManagerDebugConfig.DEBUG_PROCESSES) {
            Slog.d(TAG_PROCESSES, "Force removing proc " + app.toShortString() + " (" + name + "/" + uid + ")");
        }
        if (((ProcessRecord) this.mProcessNames.get(name, app.info.euid + uid)) != app) {
            Slog.w(TAG, "Ignoring remove of inactive process: " + app);
            return false;
        }
        removeProcessNameLocked(name, app.info.euid + uid);
        if (this.mHeavyWeightProcess == app) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(25, this.mHeavyWeightProcess.userId, 0));
            this.mHeavyWeightProcess = null;
        }
        boolean needRestart = false;
        if (app.pid <= 0 || app.pid == MY_PID) {
            this.mRemovedProcesses.add(app);
        } else {
            int pid = app.pid;
            synchronized (this.mPidsSelfLocked) {
                this.mPidsSelfLocked.remove(pid);
                this.mHandler.removeMessages(20, app);
            }
            this.mBatteryStatsService.noteProcessFinish(app.processName, app.info.uid);
            if (app.isolated) {
                this.mBatteryStatsService.removeIsolatedUid(app.uid, app.info.uid);
            }
            boolean willRestart = false;
            if (app.persistent && !app.isolated) {
                if (callerWillRestart) {
                    needRestart = true;
                } else {
                    willRestart = true;
                }
            }
            app.kill(reason, true);
            handleAppDiedLocked(app, willRestart, allowRestart);
            if (willRestart) {
                removeLruProcessLocked(app);
                addAppLocked(app.info, false, null);
            }
        }
        return needRestart;
    }

    private final void processContentProviderPublishTimedOutLocked(ProcessRecord app) {
        cleanupAppInLaunchingProvidersLocked(app, true);
        removeProcessLocked(app, false, true, "timeout publishing content providers");
    }

    private final void processStartTimedOutLocked(ProcessRecord app) {
        int pid = app.pid;
        boolean gone = false;
        synchronized (this.mPidsSelfLocked) {
            ProcessRecord knownApp = (ProcessRecord) this.mPidsSelfLocked.get(pid);
            if (knownApp != null && knownApp.thread == null) {
                this.mPidsSelfLocked.remove(pid);
                gone = true;
            }
        }
        if (gone) {
            Slog.w(TAG, "Process " + app + " failed to attach");
            EventLog.writeEvent(EventLogTags.AM_PROCESS_START_TIMEOUT, new Object[]{Integer.valueOf(app.userId), Integer.valueOf(pid), Integer.valueOf(app.uid), app.processName});
            removeProcessNameLocked(app.processName, app.uid + app.info.euid);
            if (this.mHeavyWeightProcess == app) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(25, this.mHeavyWeightProcess.userId, 0));
                this.mHeavyWeightProcess = null;
            }
            this.mBatteryStatsService.noteProcessFinish(app.processName, app.info.uid);
            if (app.isolated) {
                this.mBatteryStatsService.removeIsolatedUid(app.uid, app.info.uid);
            }
            cleanupAppInLaunchingProvidersLocked(app, true);
            this.mServices.processStartTimedOutLocked(app);
            app.kill("start timeout", true);
            removeLruProcessLocked(app);
            if (this.mBackupTarget != null && this.mBackupTarget.app.pid == pid) {
                Slog.w(TAG, "Unattached app died before backup, skipping");
                try {
                    IBackupManager.Stub.asInterface(ServiceManager.getService("backup")).agentDisconnected(app.info.packageName);
                } catch (RemoteException e) {
                }
            }
            if (isPendingBroadcastProcessLocked(pid)) {
                Slog.w(TAG, "Unattached app died before broadcast acknowledged, skipping");
                skipPendingBroadcastLocked(pid);
                return;
            }
            return;
        }
        Slog.w(TAG, "Spurious process start timeout - pid not known for " + app);
    }

    private final boolean attachApplicationLocked(IApplicationThread thread, int pid) {
        ProcessRecord app;
        if (pid == MY_PID || pid < 0) {
            app = null;
        } else {
            synchronized (this.mPidsSelfLocked) {
                app = (ProcessRecord) this.mPidsSelfLocked.get(pid);
            }
        }
        if (app == null) {
            Slog.w(TAG, "No pending application record for pid " + pid + " (IApplicationThread " + thread + "); dropping process");
            EventLog.writeEvent(EventLogTags.AM_DROP_PROCESS, pid);
            if (pid <= 0 || pid == MY_PID) {
                try {
                    thread.scheduleExit();
                } catch (Exception e) {
                }
            } else {
                Process.killProcessQuiet(pid);
            }
            return false;
        }
        if (app.thread != null) {
            handleAppDiedLocked(app, true, true);
        }
        if (ActivityManagerDebugConfig.DEBUG_ALL) {
            Slog.v(TAG, "Binding process pid " + pid + " to record " + app);
        }
        String processName = app.processName;
        try {
            AppDeathRecipient appDeathRecipient = new AppDeathRecipient(app, pid, thread);
            thread.asBinder().linkToDeath(appDeathRecipient, 0);
            app.deathRecipient = appDeathRecipient;
            EventLog.writeEvent(EventLogTags.AM_PROC_BOUND, new Object[]{Integer.valueOf(app.userId), Integer.valueOf(app.pid), app.processName});
            app.makeActive(thread, this.mProcessStats);
            app.verifiedAdj = -10000;
            app.setAdj = -10000;
            app.curAdj = -10000;
            app.setSchedGroup = 1;
            app.curSchedGroup = 1;
            app.forcingToForeground = null;
            updateProcessForegroundLocked(app, false, false);
            app.hasShownUi = false;
            app.debugging = false;
            app.cached = false;
            app.killedByAm = false;
            app.unlocked = StorageManager.isUserKeyUnlocked(app.userId);
            this.mHandler.removeMessages(20, app);
            boolean normalMode = !this.mProcessesReady ? isAllowedWhileBooting(app.info) : true;
            List<ProviderInfo> providers = normalMode ? generateApplicationProvidersLocked(app) : null;
            if (providers != null && checkAppInLaunchingProvidersLocked(app)) {
                Message msg = this.mHandler.obtainMessage(CONTENT_PROVIDER_PUBLISH_TIMEOUT_MSG);
                msg.obj = app;
                this.mHandler.sendMessageDelayed(msg, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
            }
            if (!normalMode) {
                Slog.i(TAG, "Launching preboot mode app: " + app);
            }
            Flog.i(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "New app record " + app + " thread=" + thread.asBinder() + " pid=" + pid + " procname=" + processName);
            int testMode = 0;
            try {
                ProfilerInfo profilerInfo;
                if (this.mDebugApp != null && this.mDebugApp.equals(processName)) {
                    if (this.mWaitForDebugger) {
                        testMode = 2;
                    } else {
                        testMode = 1;
                    }
                    app.debugging = true;
                    if (this.mDebugTransient) {
                        this.mDebugApp = this.mOrigDebugApp;
                        this.mWaitForDebugger = this.mOrigWaitForDebugger;
                    }
                }
                String profileFile = app.instrumentationProfileFile;
                ParcelFileDescriptor parcelFileDescriptor = null;
                int samplingInterval = 0;
                boolean profileAutoStop = false;
                if (this.mProfileApp != null && this.mProfileApp.equals(processName)) {
                    this.mProfileProc = app;
                    profileFile = this.mProfileFile;
                    parcelFileDescriptor = this.mProfileFd;
                    samplingInterval = this.mSamplingInterval;
                    profileAutoStop = this.mAutoStopProfiler;
                }
                boolean enableTrackAllocation = false;
                if (this.mTrackAllocationApp != null && this.mTrackAllocationApp.equals(processName)) {
                    enableTrackAllocation = true;
                    this.mTrackAllocationApp = null;
                }
                boolean isRestrictedBackupMode = false;
                if (this.mBackupTarget != null && this.mBackupAppName.equals(processName)) {
                    isRestrictedBackupMode = this.mBackupTarget.appInfo.uid >= 10000 ? (this.mBackupTarget.backupMode == 2 || this.mBackupTarget.backupMode == 3) ? true : this.mBackupTarget.backupMode == 1 : false;
                }
                if (app.instrumentationClass != null) {
                    notifyPackageUse(app.instrumentationClass.getPackageName(), 7);
                }
                if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                    Slog.v(TAG_CONFIGURATION, "Binding proc " + processName + " with config " + this.mConfiguration);
                }
                ApplicationInfo appInfo = app.instrumentationInfo != null ? app.instrumentationInfo : app.info;
                app.compat = compatibilityInfoForPackageLocked(appInfo);
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor = parcelFileDescriptor.dup();
                }
                if (profileFile == null) {
                    profilerInfo = null;
                } else {
                    profilerInfo = new ProfilerInfo(profileFile, parcelFileDescriptor, samplingInterval, profileAutoStop);
                }
                if (Jlog.isPerfTest()) {
                    Jlog.i(2033, "pid=" + pid + "&processname=" + processName);
                }
                attachRogInfoToApp(app, appInfo);
                ComponentName componentName = app.instrumentationClass;
                Bundle bundle = app.instrumentationArguments;
                IInstrumentationWatcher iInstrumentationWatcher = app.instrumentationWatcher;
                IUiAutomationConnection iUiAutomationConnection = app.instrumentationUiAutomationConnection;
                boolean z = this.mBinderTransactionTrackingEnabled;
                boolean z2 = isRestrictedBackupMode || !normalMode;
                thread.bindApplication(processName, appInfo, providers, componentName, profilerInfo, bundle, iInstrumentationWatcher, iUiAutomationConnection, testMode, z, enableTrackAllocation, z2, app.persistent, new Configuration(this.mConfiguration), app.compat, getCommonServicesLocked(app.isolated), this.mCoreSettingsObserver.getCoreSettingsLocked());
                updateLruProcessLocked(app, false, null);
                long uptimeMillis = SystemClock.uptimeMillis();
                app.lastLowMemory = uptimeMillis;
                app.lastRequestedGc = uptimeMillis;
                this.mPersistentStartingProcesses.remove(app);
                if (ActivityManagerDebugConfig.DEBUG_PROCESSES && this.mProcessesOnHold.contains(app)) {
                    Slog.v(TAG_PROCESSES, "Attach application locked removing on hold: " + app);
                }
                this.mProcessesOnHold.remove(app);
                boolean badApp = false;
                int didSomething = false;
                if (normalMode) {
                    try {
                        if (this.mStackSupervisor.attachApplicationLocked(app)) {
                            didSomething = true;
                        }
                    } catch (Throwable e2) {
                        Slog.wtf(TAG, "Exception thrown launching activities in " + app, e2);
                        badApp = true;
                    }
                }
                if (!badApp) {
                    try {
                        didSomething |= this.mServices.attachApplicationLocked(app, processName);
                    } catch (Throwable e22) {
                        Flog.w(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "Exception thrown starting services in " + app, e22);
                        Slog.wtf(TAG, "Exception thrown starting services in " + app, e22);
                        badApp = true;
                    }
                }
                if (!badApp && isPendingBroadcastProcessLocked(pid)) {
                    try {
                        didSomething |= sendPendingBroadcastsLocked(app);
                    } catch (Throwable e222) {
                        Slog.wtf(TAG, "Exception thrown dispatching broadcasts in " + app, e222);
                        badApp = true;
                    }
                }
                if (!(badApp || this.mBackupTarget == null || this.mBackupTarget.appInfo.uid != app.uid)) {
                    if (ActivityManagerDebugConfig.DEBUG_BACKUP) {
                        Slog.v(TAG_BACKUP, "New app is backup target, launching agent for " + app);
                    }
                    notifyPackageUse(this.mBackupTarget.appInfo.packageName, 5);
                    try {
                        thread.scheduleCreateBackupAgent(this.mBackupTarget.appInfo, compatibilityInfoForPackageLocked(this.mBackupTarget.appInfo), this.mBackupTarget.backupMode);
                    } catch (Throwable e2222) {
                        Slog.wtf(TAG, "Exception thrown creating backup agent in " + app, e2222);
                        badApp = true;
                    }
                }
                if (badApp) {
                    app.kill("error during init", true);
                    handleAppDiedLocked(app, false, true);
                    return false;
                }
                if (didSomething == 0) {
                    updateOomAdjLocked();
                }
                return true;
            } catch (Throwable e22222) {
                Slog.wtf(TAG, "Exception thrown during bind of " + app, e22222);
                app.resetPackageList(this.mProcessStats);
                app.unlinkDeathRecipient();
                startProcessLocked(app, "bind fail", processName);
                return false;
            }
        } catch (RemoteException e3) {
            app.resetPackageList(this.mProcessStats);
            startProcessLocked(app, "link fail", processName);
            return false;
        }
    }

    public final void attachApplication(IApplicationThread thread) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                int callingPid = Binder.getCallingPid();
                long origId = Binder.clearCallingIdentity();
                Slog.d(TAG, "ActivityManagerService,attachApplication,callingPid = " + callingPid);
                attachApplicationLocked(thread, callingPid);
                Binder.restoreCallingIdentity(origId);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public final void activityIdle(IBinder token, Configuration config, boolean stopProfiling) {
        long origId = Binder.clearCallingIdentity();
        if (!this.mActivityIdle) {
            this.mActivityIdle = true;
            HwBootFail.notifyBootSuccess();
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if (ActivityRecord.getStackLocked(token) != null) {
                    ActivityRecord r = this.mStackSupervisor.activityIdleInternalLocked(token, false, config);
                    if (stopProfiling && this.mProfileProc == r.app && this.mProfileFd != null) {
                        try {
                            this.mProfileFd.close();
                        } catch (IOException e) {
                        }
                        clearProfilerLocked();
                    }
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        Binder.restoreCallingIdentity(origId);
    }

    void postFinishBooting(boolean finishBooting, boolean enableScreen) {
        int i;
        int i2 = 1;
        MainHandler mainHandler = this.mHandler;
        MainHandler mainHandler2 = this.mHandler;
        if (finishBooting) {
            i = 1;
        } else {
            i = 0;
        }
        if (!enableScreen) {
            i2 = 0;
        }
        mainHandler.sendMessage(mainHandler2.obtainMessage(FINISH_BOOTING_MSG, i, i2));
    }

    void enableScreenAfterBoot() {
        EventLog.writeEvent(EventLogTags.BOOT_PROGRESS_ENABLE_SCREEN, SystemClock.uptimeMillis());
        Jlog.d(34, "JL_BOOT_PROGRESS_ENABLE_SCREEN");
        this.mWindowManager.enableScreenAfterBoot();
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                updateEventDispatchingLocked();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        if (this.mIsHwLowRam) {
            Message nmsg = this.mHandler.obtainMessage(98);
            Log.i(TAG, "smartshrink send msg to trim service");
            this.mHandler.sendMessageDelayed(nmsg, 60000);
        }
    }

    public void showBootMessage(CharSequence msg, boolean always) {
        if (Binder.getCallingUid() != Process.myUid()) {
            this.mWindowManager.showBootMessage(msg, always);
        } else {
            this.mWindowManager.showBootMessage(msg, always);
        }
    }

    public void keyguardWaitingForActivityDrawn() {
        enforceNotIsolatedCaller("keyguardWaitingForActivityDrawn");
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                logLockScreen("");
                this.mWindowManager.keyguardWaitingForActivityDrawn();
                if (this.mLockScreenShown == 2) {
                    this.mLockScreenShown = 1;
                    updateSleepIfNeededLocked();
                }
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(token);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void keyguardGoingAway(int flags) {
        enforceNotIsolatedCaller("keyguardGoingAway");
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                logLockScreen("");
                this.mWindowManager.keyguardGoingAway(flags);
                if (this.mLockScreenShown == 2) {
                    this.mLockScreenShown = 0;
                    updateSleepIfNeededLocked();
                    this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
                    applyVrModeIfNeededLocked(this.mFocusedActivity, true);
                }
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(token);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final void finishBooting() {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if (this.mBootAnimationComplete) {
                    this.mCallFinishBooting = false;
                } else {
                    this.mCallFinishBooting = true;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        completedIsas.add(instructionSet);
        int i++;
    }

    public void bootAnimationComplete() {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                boolean callFinishBooting = this.mCallFinishBooting;
                this.mBootAnimationComplete = true;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        if (callFinishBooting) {
            Trace.traceBegin(64, "FinishBooting");
            finishBooting();
            Trace.traceEnd(64);
        }
    }

    final void ensureBootCompleted() {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                boolean booting = this.mBooting;
                this.mBooting = false;
                boolean enableScreen = !this.mBooted;
                this.mBooted = true;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        if (booting) {
            Trace.traceBegin(64, "FinishBooting");
            finishBooting();
            Trace.traceEnd(64);
        }
        if (enableScreen) {
            enableScreenAfterBoot();
        }
    }

    public final void activityResumed(IBinder token) {
        long origId = Binder.clearCallingIdentity();
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityStack stack = ActivityRecord.getStackLocked(token);
                if (stack != null) {
                    stack.activityResumedLocked(token);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        Binder.restoreCallingIdentity(origId);
    }

    public final void activityPaused(IBinder token) {
        long origId = Binder.clearCallingIdentity();
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityStack stack = ActivityRecord.getStackLocked(token);
                if (stack != null) {
                    stack.activityPausedLocked(token, false);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        Binder.restoreCallingIdentity(origId);
    }

    public final void activityStopped(IBinder token, Bundle icicle, PersistableBundle persistentState, CharSequence description) {
        if (ActivityManagerDebugConfig.DEBUG_ALL) {
            Slog.v(TAG, "Activity stopped: token=" + token);
        }
        if (icicle == null || !icicle.hasFileDescriptors()) {
            long origId = Binder.clearCallingIdentity();
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(token);
                    if (r != null) {
                        r.task.stack.activityStoppedLocked(r, icicle, persistentState, description);
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            trimApplications();
            Binder.restoreCallingIdentity(origId);
            return;
        }
        throw new IllegalArgumentException("File descriptors passed in Bundle");
    }

    public final void activityDestroyed(IBinder token) {
        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
            Slog.v(TAG_SWITCH, "ACTIVITY DESTROYED: " + token);
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityStack stack = ActivityRecord.getStackLocked(token);
                if (stack != null) {
                    stack.activityDestroyedLocked(token, "activityDestroyed");
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public final void activityRelaunched(IBinder token) {
        long origId = Binder.clearCallingIdentity();
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mStackSupervisor.activityRelaunchedLocked(token);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        Binder.restoreCallingIdentity(origId);
    }

    public void reportSizeConfigurations(IBinder token, int[] horizontalSizeConfiguration, int[] verticalSizeConfigurations, int[] smallestSizeConfigurations) {
        if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
            Slog.v(TAG, "Report configuration: " + token + " " + horizontalSizeConfiguration + " " + verticalSizeConfigurations);
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord record = ActivityRecord.isInStackLocked(token);
                if (record == null) {
                    throw new IllegalArgumentException("reportSizeConfigurations: ActivityRecord not found for: " + token);
                }
                record.setSizeConfigurations(horizontalSizeConfiguration, verticalSizeConfigurations, smallestSizeConfigurations);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public final void backgroundResourcesReleased(IBinder token) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                ActivityStack stack = ActivityRecord.getStackLocked(token);
                if (stack != null) {
                    stack.backgroundResourcesReleased();
                }
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(origId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public final void notifyLaunchTaskBehindComplete(IBinder token) {
        HwSlog.d(TAG, "notifyLaunchTaskBehindComplete, token=" + token);
        this.mStackSupervisor.scheduleLaunchTaskBehindComplete(token);
    }

    public final void notifyEnterAnimationComplete(IBinder token) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(44, token));
    }

    public String getCallingPackage(IBinder token) {
        String str = null;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord r = getCallingRecordLocked(token);
                if (r != null) {
                    str = r.info.packageName;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return str;
    }

    public ComponentName getCallingActivity(IBinder token) {
        ComponentName componentName = null;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord r = getCallingRecordLocked(token);
                if (r != null) {
                    componentName = r.intent.getComponent();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return componentName;
    }

    private ActivityRecord getCallingRecordLocked(IBinder token) {
        ActivityRecord r = ActivityRecord.isInStackLocked(token);
        if (r == null) {
            return null;
        }
        return r.resultTo;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ComponentName getActivityClassForToken(IBinder token) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                } else {
                    ComponentName component = r.intent.getComponent();
                    resetPriorityAfterLockedSection();
                    return component;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String getPackageForToken(IBinder token) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                } else {
                    String str = r.packageName;
                    resetPriorityAfterLockedSection();
                    return str;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isRootVoiceInteraction(IBinder token) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    boolean z = r.rootVoiceInteraction;
                    resetPriorityAfterLockedSection();
                    return z;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public IIntentSender getIntentSender(int type, String packageName, IBinder token, String resultWho, int requestCode, Intent[] intents, String[] resolvedTypes, int flags, Bundle bOptions, int userId) {
        enforceNotIsolatedCaller("getIntentSender");
        if (intents != null) {
            if (intents.length < 1) {
                throw new IllegalArgumentException("Intents array length must be >= 1");
            }
            for (int i = 0; i < intents.length; i++) {
                Intent intent = intents[i];
                if (intent != null) {
                    if (intent.hasFileDescriptors()) {
                        throw new IllegalArgumentException("File descriptors passed in Intent");
                    } else if (type != 1 || (intent.getFlags() & 33554432) == 0) {
                        intents[i] = new Intent(intent);
                    } else {
                        throw new IllegalArgumentException("Can't use FLAG_RECEIVER_BOOT_UPGRADE here");
                    }
                }
            }
            if (!(resolvedTypes == null || resolvedTypes.length == intents.length)) {
                throw new IllegalArgumentException("Intent array length does not match resolvedTypes length");
            }
        }
        if (bOptions == null || !bOptions.hasFileDescriptors()) {
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    int callingUid = Binder.getCallingUid();
                    int origUserId = userId;
                    userId = this.mUserController.handleIncomingUser(Binder.getCallingPid(), callingUid, userId, type == 1, 0, "getIntentSender", null);
                    if (origUserId == -2) {
                        userId = -2;
                    }
                    if (!(callingUid == 0 || callingUid == 1000)) {
                        int uid = AppGlobals.getPackageManager().getPackageUid(packageName, 268435456, UserHandle.getUserId(callingUid));
                        if (uid == -1) {
                            Slog.e(TAG, "getIntentSender() error, user " + UserHandle.getUserId(callingUid) + " is removed, or package " + packageName + " is removed");
                            resetPriorityAfterLockedSection();
                            return null;
                        } else if (!UserHandle.isSameApp(callingUid, uid)) {
                            String msg = "Permission Denial: getIntentSender() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", (need uid=" + uid + ")" + " is not allowed to send as package " + packageName + " callers=" + Debug.getCallers(5);
                            Slog.w(TAG, msg);
                            throw new SecurityException(msg);
                        }
                    }
                    IIntentSender intentSenderLocked = getIntentSenderLocked(type, packageName, callingUid, userId, token, resultWho, requestCode, intents, resolvedTypes, flags, bOptions);
                    resetPriorityAfterLockedSection();
                    return intentSenderLocked;
                } catch (Throwable e) {
                    throw new SecurityException(e);
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new IllegalArgumentException("File descriptors passed in options");
        }
    }

    IIntentSender getIntentSenderLocked(int type, String packageName, int callingUid, int userId, IBinder token, String resultWho, int requestCode, Intent[] intents, String[] resolvedTypes, int flags, Bundle bOptions) {
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.v(TAG_MU, "getIntentSenderLocked(): uid=" + callingUid);
        }
        ActivityRecord activityRecord = null;
        if (type == 3) {
            activityRecord = ActivityRecord.isInStackLocked(token);
            if (activityRecord == null) {
                Slog.w(TAG, "Failed createPendingResult: activity " + token + " not in any stack");
                return null;
            } else if (activityRecord.finishing) {
                Slog.w(TAG, "Failed createPendingResult: activity " + activityRecord + " is finishing");
                return null;
            }
        }
        if (intents != null) {
            for (Intent defusable : intents) {
                defusable.setDefusable(true);
            }
        }
        Bundle.setDefusable(bOptions, true);
        boolean noCreate = (536870912 & flags) != 0;
        boolean cancelCurrent = (268435456 & flags) != 0;
        boolean updateCurrent = (134217728 & flags) != 0;
        Key key = new Key(type, packageName, activityRecord, resultWho, requestCode, intents, resolvedTypes, flags & -939524097, bOptions, userId);
        WeakReference<PendingIntentRecord> ref = (WeakReference) this.mIntentSenderRecords.get(key);
        PendingIntentRecord rec = ref != null ? (PendingIntentRecord) ref.get() : null;
        if (rec != null) {
            if (cancelCurrent) {
                rec.canceled = true;
                this.mIntentSenderRecords.remove(key);
            } else {
                if (updateCurrent) {
                    if (rec.key.requestIntent != null) {
                        rec.key.requestIntent.replaceExtras(intents != null ? intents[intents.length - 1] : null);
                    }
                    if (intents != null) {
                        intents[intents.length - 1] = rec.key.requestIntent;
                        rec.key.allIntents = intents;
                        rec.key.allResolvedTypes = resolvedTypes;
                    } else {
                        rec.key.allIntents = null;
                        rec.key.allResolvedTypes = null;
                    }
                }
                return rec;
            }
        }
        if (noCreate) {
            return rec;
        }
        PendingIntentRecord pendingIntentRecord = new PendingIntentRecord(this, key, callingUid);
        this.mIntentSenderRecords.put(key, pendingIntentRecord.ref);
        if (type == 3) {
            if (activityRecord.pendingResults == null) {
                activityRecord.pendingResults = new HashSet();
            }
            activityRecord.pendingResults.add(pendingIntentRecord.ref);
        }
        return pendingIntentRecord;
    }

    public int sendIntentSender(IIntentSender target, int code, Intent intent, String resolvedType, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
        if (target instanceof PendingIntentRecord) {
            return ((PendingIntentRecord) target).sendWithResult(code, intent, resolvedType, finishedReceiver, requiredPermission, options);
        }
        if (intent == null) {
            Slog.wtf(TAG, "Can't use null intent with direct IIntentSender call");
            intent = new Intent("android.intent.action.MAIN");
        }
        try {
            target.send(code, intent, resolvedType, null, requiredPermission, options);
        } catch (RemoteException e) {
        }
        if (finishedReceiver != null) {
            try {
                finishedReceiver.performReceive(intent, 0, null, null, false, false, UserHandle.getCallingUserId());
            } catch (RemoteException e2) {
            }
        }
        return 0;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void tempWhitelistAppForPowerSave(int callerPid, int callerUid, int targetUid, long duration) {
        if (ActivityManagerDebugConfig.DEBUG_WHITELISTS) {
            Slog.d(TAG, "tempWhitelistAppForPowerSave(" + callerPid + ", " + callerUid + ", " + targetUid + ", " + duration + ")");
        }
        synchronized (this.mPidsSelfLocked) {
            ProcessRecord pr = (ProcessRecord) this.mPidsSelfLocked.get(callerPid);
            if (pr == null) {
                Slog.w(TAG, "tempWhitelistAppForPowerSave() no ProcessRecord for pid " + callerPid);
            } else if (pr.whitelistManager) {
                Message msg = this.mHandler.obtainMessage(97);
                msg.arg1 = targetUid;
                msg.arg2 = (int) duration;
                msg.obj = "pe from uid:" + callerUid;
                this.mHandler.sendMessage(msg);
            } else if (ActivityManagerDebugConfig.DEBUG_WHITELISTS) {
                Slog.d(TAG, "tempWhitelistAppForPowerSave() for target " + targetUid + ": pid " + callerPid + " is not allowed");
            }
        }
    }

    public void cancelIntentSender(IIntentSender sender) {
        if (sender instanceof PendingIntentRecord) {
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    PendingIntentRecord rec = (PendingIntentRecord) sender;
                    if (UserHandle.isSameApp(AppGlobals.getPackageManager().getPackageUid(rec.key.packageName, 268435456, UserHandle.getCallingUserId()), Binder.getCallingUid())) {
                        cancelIntentSenderLocked(rec, true);
                    } else {
                        String msg = "Permission Denial: cancelIntentSender() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " is not allowed to cancel packges " + rec.key.packageName;
                        Slog.w(TAG, msg);
                        throw new SecurityException(msg);
                    }
                } catch (RemoteException e) {
                    throw new SecurityException(e);
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                }
            }
            resetPriorityAfterLockedSection();
        }
    }

    void cancelIntentSenderLocked(PendingIntentRecord rec, boolean cleanActivity) {
        rec.canceled = true;
        this.mIntentSenderRecords.remove(rec.key);
        if (cleanActivity && rec.key.activity != null) {
            rec.key.activity.pendingResults.remove(rec.ref);
        }
    }

    public String getPackageForIntentSender(IIntentSender pendingResult) {
        if (!(pendingResult instanceof PendingIntentRecord)) {
            return null;
        }
        try {
            return ((PendingIntentRecord) pendingResult).key.packageName;
        } catch (ClassCastException e) {
            return null;
        }
    }

    public int getUidForIntentSender(IIntentSender sender) {
        if (sender instanceof PendingIntentRecord) {
            try {
                return ((PendingIntentRecord) sender).uid;
            } catch (ClassCastException e) {
            }
        }
        return -1;
    }

    public boolean isIntentSenderTargetedToPackage(IIntentSender pendingResult) {
        if (!(pendingResult instanceof PendingIntentRecord)) {
            return false;
        }
        try {
            PendingIntentRecord res = (PendingIntentRecord) pendingResult;
            if (res.key.allIntents == null) {
                return false;
            }
            for (Intent intent : res.key.allIntents) {
                if (intent.getPackage() != null && intent.getComponent() != null) {
                    return false;
                }
            }
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public boolean isIntentSenderAnActivity(IIntentSender pendingResult) {
        if (!(pendingResult instanceof PendingIntentRecord)) {
            return false;
        }
        try {
            if (((PendingIntentRecord) pendingResult).key.type == 2) {
                return true;
            }
            return false;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public Intent getIntentForIntentSender(IIntentSender pendingResult) {
        enforceCallingPermission("android.permission.GET_INTENT_SENDER_INTENT", "getIntentForIntentSender()");
        if (!(pendingResult instanceof PendingIntentRecord)) {
            return null;
        }
        try {
            PendingIntentRecord res = (PendingIntentRecord) pendingResult;
            return res.key.requestIntent != null ? new Intent(res.key.requestIntent) : null;
        } catch (ClassCastException e) {
            return null;
        }
    }

    public String getTagForIntentSender(IIntentSender pendingResult, String prefix) {
        if (!(pendingResult instanceof PendingIntentRecord)) {
            return null;
        }
        try {
            String tagForIntentSenderLocked;
            PendingIntentRecord res = (PendingIntentRecord) pendingResult;
            synchronized (this) {
                boostPriorityForLockedSection();
                tagForIntentSenderLocked = getTagForIntentSenderLocked(res, prefix);
            }
            resetPriorityAfterLockedSection();
            return tagForIntentSenderLocked;
        } catch (ClassCastException e) {
            return null;
        } catch (Throwable th) {
            resetPriorityAfterLockedSection();
        }
    }

    String getTagForIntentSenderLocked(PendingIntentRecord res, String prefix) {
        Intent intent = res.key.requestIntent;
        if (intent == null) {
            return null;
        }
        if (res.lastTag != null && res.lastTagPrefix == prefix && (res.lastTagPrefix == null || res.lastTagPrefix.equals(prefix))) {
            return res.lastTag;
        }
        res.lastTagPrefix = prefix;
        StringBuilder sb = new StringBuilder(128);
        if (prefix != null) {
            sb.append(prefix);
        }
        if (intent.getAction() != null) {
            sb.append(intent.getAction());
        } else if (intent.getComponent() != null) {
            intent.getComponent().appendShortString(sb);
        } else {
            sb.append("?");
        }
        String stringBuilder = sb.toString();
        res.lastTag = stringBuilder;
        return stringBuilder;
    }

    public void setProcessLimit(int max) {
        enforceCallingPermission("android.permission.SET_PROCESS_LIMIT", "setProcessLimit()");
        synchronized (this) {
            try {
                int i;
                boostPriorityForLockedSection();
                if (max < 0) {
                    i = ProcessList.MAX_CACHED_APPS;
                } else {
                    i = max;
                }
                this.mProcessLimit = i;
                this.mProcessLimitOverride = max;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        trimApplications();
    }

    public int getProcessLimit() {
        int i;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                i = this.mProcessLimitOverride;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return i;
    }

    void foregroundTokenDied(ForegroundToken token) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                synchronized (this.mPidsSelfLocked) {
                    if (((ForegroundToken) this.mForegroundProcesses.get(token.pid)) != token) {
                        resetPriorityAfterLockedSection();
                        return;
                    }
                    this.mForegroundProcesses.remove(token.pid);
                    ProcessRecord pr = (ProcessRecord) this.mPidsSelfLocked.get(token.pid);
                    if (pr == null) {
                        resetPriorityAfterLockedSection();
                        return;
                    }
                    pr.forcingToForeground = null;
                    updateProcessForegroundLocked(pr, false, false);
                    updateOomAdjLocked();
                    resetPriorityAfterLockedSection();
                }
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setProcessForeground(IBinder token, int pid, boolean isForeground) {
        enforceCallingPermission("android.permission.SET_PROCESS_LIMIT", "setProcessForeground()");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                boolean changed = false;
                synchronized (this.mPidsSelfLocked) {
                    ProcessRecord pr = (ProcessRecord) this.mPidsSelfLocked.get(pid);
                    if (pr == null && isForeground) {
                        Slog.w(TAG, "setProcessForeground called on unknown pid: " + pid);
                        resetPriorityAfterLockedSection();
                        return;
                    }
                    ForegroundToken oldToken = (ForegroundToken) this.mForegroundProcesses.get(pid);
                    if (oldToken != null) {
                        oldToken.token.unlinkToDeath(oldToken, 0);
                        this.mForegroundProcesses.remove(pid);
                        if (pr != null) {
                            pr.forcingToForeground = null;
                        }
                        changed = true;
                    }
                    if (isForeground && token != null) {
                        ForegroundToken newToken = new ForegroundToken(this) {
                            public void binderDied() {
                                this.foregroundTokenDied(this);
                            }
                        };
                        newToken.pid = pid;
                        newToken.token = token;
                        try {
                            token.linkToDeath(newToken, 0);
                            this.mForegroundProcesses.put(pid, newToken);
                            pr.forcingToForeground = token;
                            changed = true;
                        } catch (RemoteException e) {
                        }
                    }
                }
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isAppForeground(int uid) throws RemoteException {
        boolean z = false;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                UidRecord uidRec = (UidRecord) this.mActiveUids.get(uid);
                if (uidRec == null || uidRec.idle) {
                } else if (uidRec.curProcState <= 6) {
                    z = true;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    int getUidState(int uid) {
        int i;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                UidRecord uidRec = (UidRecord) this.mActiveUids.get(uid);
                i = uidRec == null ? -1 : uidRec.curProcState;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return i;
    }

    public boolean isInMultiWindowMode(IBinder token) {
        boolean z = false;
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                    return false;
                }
                if (!r.task.mFullscreen) {
                    z = true;
                }
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(origId);
                return z;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public boolean isInPictureInPictureMode(IBinder token) {
        boolean z = false;
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                ActivityStack stack = ActivityRecord.getStackLocked(token);
                if (stack == null) {
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                    return false;
                }
                if (stack.mStackId == 4) {
                    z = true;
                }
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(origId);
                return z;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void enterPictureInPictureMode(IBinder token) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                if (this.mSupportsPictureInPicture) {
                    ActivityRecord r = ActivityRecord.forTokenLocked(token);
                    if (r == null) {
                        throw new IllegalStateException("enterPictureInPictureMode: Can't find activity for token=" + token);
                    } else if (r.supportsPictureInPicture()) {
                        ActivityStack pinnedStack = this.mStackSupervisor.getStack(4);
                        this.mStackSupervisor.moveActivityToPinnedStackLocked(r, "enterPictureInPictureMode", pinnedStack != null ? pinnedStack.mBounds : this.mDefaultPinnedStackBounds);
                    } else {
                        throw new IllegalArgumentException("enterPictureInPictureMode: Picture-In-Picture not supported for r=" + r);
                    }
                }
                throw new IllegalStateException("enterPictureInPictureMode: Device doesn't support picture-in-picture mode.");
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(origId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void getProcessStatesAndOomScoresForPIDs(int[] pids, int[] states, int[] scores) {
        if (scores != null) {
            enforceCallingPermission("android.permission.GET_PROCESS_STATE_AND_OOM_SCORE", "getProcessStatesAndOomScoresForPIDs()");
        }
        if (pids == null) {
            throw new NullPointerException("pids");
        } else if (states == null) {
            throw new NullPointerException("states");
        } else if (pids.length != states.length) {
            throw new IllegalArgumentException("pids and states arrays have different lengths!");
        } else if (scores == null || pids.length == scores.length) {
            synchronized (this.mPidsSelfLocked) {
                for (int i = 0; i < pids.length; i++) {
                    int i2;
                    ProcessRecord pr = (ProcessRecord) this.mPidsSelfLocked.get(pids[i]);
                    if (pr == null) {
                        i2 = -1;
                    } else {
                        i2 = pr.curProcState;
                    }
                    states[i] = i2;
                    if (scores != null) {
                        if (pr == null) {
                            i2 = -10000;
                        } else {
                            i2 = pr.curAdj;
                        }
                        scores[i] = i2;
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("pids and scores arrays have different lengths!");
        }
    }

    int checkComponentPermission(String permission, int pid, int uid, int owningUid, boolean exported) {
        if (pid == MY_PID) {
            return 0;
        }
        return ActivityManager.checkComponentPermission(permission, uid, owningUid, exported);
    }

    public int checkPermission(String permission, int pid, int uid) {
        if (permission == null) {
            return -1;
        }
        return checkComponentPermission(permission, pid, uid, -1, true);
    }

    public int checkPermissionWithToken(String permission, int pid, int uid, IBinder callerToken) {
        if (permission == null) {
            return -1;
        }
        Identity tlsIdentity = (Identity) sCallerIdentity.get();
        if (tlsIdentity != null && tlsIdentity.token == callerToken) {
            Slog.d(TAG, "checkComponentPermission() adjusting {pid,uid} to {" + tlsIdentity.pid + "," + tlsIdentity.uid + "}");
            uid = tlsIdentity.uid;
            pid = tlsIdentity.pid;
        }
        return checkComponentPermission(permission, pid, uid, -1, true);
    }

    int checkCallingPermission(String permission) {
        return checkPermission(permission, Binder.getCallingPid(), UserHandle.getAppId(Binder.getCallingUid()));
    }

    void enforceCallingPermission(String permission, String func) {
        if (checkCallingPermission(permission) != 0) {
            String msg = "Permission Denial: " + func + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + permission;
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
    }

    private final boolean checkHoldingPermissionsLocked(IPackageManager pm, ProviderInfo pi, GrantUri grantUri, int uid, int modeFlags) {
        if (ActivityManagerDebugConfig.DEBUG_URI_PERMISSION) {
            Slog.v(TAG_URI_PERMISSION, "checkHoldingPermissionsLocked: uri=" + grantUri + " uid=" + uid);
        }
        if (UserHandle.getUserId(uid) == grantUri.sourceUserId || ActivityManager.checkComponentPermission("android.permission.INTERACT_ACROSS_USERS", uid, -1, true) == 0) {
            return checkHoldingPermissionsInternalLocked(pm, pi, grantUri, uid, modeFlags, true);
        }
        return false;
    }

    private final boolean checkHoldingPermissionsInternalLocked(IPackageManager pm, ProviderInfo pi, GrantUri grantUri, int uid, int modeFlags, boolean considerUidPermissions) {
        if (pi.applicationInfo.uid == uid) {
            return true;
        }
        if (!pi.exported) {
            return false;
        }
        boolean readMet = (modeFlags & 1) == 0;
        boolean writeMet = (modeFlags & 2) == 0;
        if (!readMet) {
            try {
                if (pi.readPermission != null && considerUidPermissions) {
                    if (pm.checkUidPermission(pi.readPermission, uid) == 0) {
                        readMet = true;
                    }
                }
            } catch (RemoteException e) {
                return false;
            }
        }
        if (!(writeMet || pi.writePermission == null || !considerUidPermissions)) {
            if (pm.checkUidPermission(pi.writePermission, uid) == 0) {
                writeMet = true;
            }
        }
        boolean allowDefaultRead = pi.readPermission == null;
        boolean allowDefaultWrite = pi.writePermission == null;
        PathPermission[] pps = pi.pathPermissions;
        if (pps != null) {
            String path = grantUri.uri.getPath();
            int i = pps.length;
            while (i > 0 && (!readMet || !writeMet)) {
                i--;
                PathPermission pp = pps[i];
                if (pp.match(path)) {
                    if (!readMet) {
                        String pprperm = pp.getReadPermission();
                        if (ActivityManagerDebugConfig.DEBUG_URI_PERMISSION) {
                            Slog.v(TAG_URI_PERMISSION, "Checking read perm for " + pprperm + " for " + pp.getPath() + ": match=" + pp.match(path) + " check=" + pm.checkUidPermission(pprperm, uid));
                        }
                        if (pprperm != null) {
                            if (considerUidPermissions && pm.checkUidPermission(pprperm, uid) == 0) {
                                readMet = true;
                            } else {
                                allowDefaultRead = false;
                            }
                        }
                    }
                    if (!writeMet) {
                        String ppwperm = pp.getWritePermission();
                        if (ActivityManagerDebugConfig.DEBUG_URI_PERMISSION) {
                            Slog.v(TAG_URI_PERMISSION, "Checking write perm " + ppwperm + " for " + pp.getPath() + ": match=" + pp.match(path) + " check=" + pm.checkUidPermission(ppwperm, uid));
                        }
                        if (ppwperm != null) {
                            if (considerUidPermissions && pm.checkUidPermission(ppwperm, uid) == 0) {
                                writeMet = true;
                            } else {
                                allowDefaultWrite = false;
                            }
                        }
                    }
                }
            }
        }
        if (allowDefaultRead) {
            readMet = true;
        }
        if (allowDefaultWrite) {
            writeMet = true;
        }
        if (!readMet) {
            writeMet = false;
        }
        return writeMet;
    }

    public int getAppStartMode(int uid, String packageName) {
        int checkAllowBackgroundLocked;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                checkAllowBackgroundLocked = checkAllowBackgroundLocked(uid, packageName, -1, true);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return checkAllowBackgroundLocked;
    }

    int checkAllowBackgroundLocked(int uid, String packageName, int callingPid, boolean allowWhenForeground) {
        UidRecord uidRec = (UidRecord) this.mActiveUids.get(uid);
        if (this.mLenientBackgroundCheck) {
            if (uidRec == null || uidRec.idle) {
                if (callingPid >= 0) {
                    ProcessRecord proc;
                    synchronized (this.mPidsSelfLocked) {
                        proc = (ProcessRecord) this.mPidsSelfLocked.get(callingPid);
                    }
                    if (proc != null && proc.curProcState < 11) {
                        return 0;
                    }
                }
                if (this.mAppOpsService.noteOperation(VR_MODE_CHANGE_MSG, uid, packageName) != 0) {
                    return 1;
                }
            }
        } else if ((!allowWhenForeground || uidRec == null || uidRec.curProcState >= 7) && this.mAppOpsService.noteOperation(VR_MODE_CHANGE_MSG, uid, packageName) != 0) {
            return 1;
        }
        return 0;
    }

    private ProviderInfo getProviderInfoLocked(String authority, int userHandle, int pmFlags) {
        ProviderInfo pi = null;
        ContentProviderRecord cpr = (isClonedProcess(Binder.getCallingPid()) ? this.mProviderMapForClone : this.mProviderMap).getProviderByName(authority, userHandle);
        if (cpr != null) {
            return cpr.info;
        }
        try {
            return AppGlobals.getPackageManager().resolveContentProvider(authority, pmFlags | 2048, userHandle);
        } catch (RemoteException e) {
            return pi;
        }
    }

    private UriPermission findUriPermissionLocked(int targetUid, GrantUri grantUri) {
        ArrayMap<GrantUri, UriPermission> targetUris = (ArrayMap) this.mGrantedUriPermissions.get(targetUid);
        if (targetUris != null) {
            return (UriPermission) targetUris.get(grantUri);
        }
        return null;
    }

    private UriPermission findOrCreateUriPermissionLocked(String sourcePkg, String targetPkg, int targetUid, GrantUri grantUri) {
        ArrayMap<GrantUri, UriPermission> targetUris = (ArrayMap) this.mGrantedUriPermissions.get(targetUid);
        if (targetUris == null) {
            targetUris = Maps.newArrayMap();
            this.mGrantedUriPermissions.put(targetUid, targetUris);
        }
        UriPermission perm = (UriPermission) targetUris.get(grantUri);
        if (perm != null) {
            return perm;
        }
        perm = new UriPermission(sourcePkg, targetPkg, targetUid, grantUri);
        targetUris.put(grantUri, perm);
        return perm;
    }

    private final boolean checkUriPermissionLocked(GrantUri grantUri, int uid, int modeFlags) {
        boolean persistable;
        if ((modeFlags & 64) != 0) {
            persistable = true;
        } else {
            persistable = false;
        }
        int minStrength;
        if (persistable) {
            minStrength = 3;
        } else {
            minStrength = 1;
        }
        if (uid == 0) {
            return true;
        }
        ArrayMap<GrantUri, UriPermission> perms = (ArrayMap) this.mGrantedUriPermissions.get(uid);
        if (perms == null) {
            return false;
        }
        UriPermission exactPerm = (UriPermission) perms.get(grantUri);
        if (exactPerm != null && exactPerm.getStrength(modeFlags) >= minStrength) {
            return true;
        }
        int N = perms.size();
        for (int i = 0; i < N; i++) {
            UriPermission perm = (UriPermission) perms.valueAt(i);
            if (perm.uri.prefix && grantUri.uri.isPathPrefixMatch(perm.uri.uri) && perm.getStrength(modeFlags) >= minStrength) {
                return true;
            }
        }
        return false;
    }

    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags, int userId, IBinder callerToken) {
        int i = 0;
        enforceNotIsolatedCaller("checkUriPermission");
        Identity tlsIdentity = (Identity) sCallerIdentity.get();
        if (tlsIdentity != null && tlsIdentity.token == callerToken) {
            uid = tlsIdentity.uid;
            pid = tlsIdentity.pid;
        }
        if (pid == MY_PID) {
            return 0;
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if (!checkUriPermissionLocked(new GrantUri(userId, uri, false), uid, modeFlags)) {
                    i = -1;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return i;
    }

    int checkGrantUriPermissionLocked(int callingUid, String targetPkg, GrantUri grantUri, int modeFlags, int lastTargetUid) {
        if (!Intent.isAccessUriMode(modeFlags)) {
            return -1;
        }
        if (targetPkg != null && ActivityManagerDebugConfig.DEBUG_URI_PERMISSION) {
            Slog.v(TAG_URI_PERMISSION, "Checking grant " + targetPkg + " permission to " + grantUri);
        }
        IPackageManager pm = AppGlobals.getPackageManager();
        if ("content".equals(grantUri.uri.getScheme())) {
            ProviderInfo pi = getProviderInfoLocked(grantUri.uri.getAuthority(), grantUri.sourceUserId, 268435456);
            if (pi == null) {
                Slog.w(TAG, "No content provider found for permission check: " + grantUri.uri.toSafeString());
                return -1;
            }
            boolean allowed;
            boolean specialCrossUserGrant;
            int targetUid = lastTargetUid;
            if (lastTargetUid < 0 && targetPkg != null) {
                try {
                    targetUid = pm.getPackageUid(targetPkg, 268435456, UserHandle.getUserId(callingUid));
                    if (targetUid < 0) {
                        if (ActivityManagerDebugConfig.DEBUG_URI_PERMISSION) {
                            Slog.v(TAG_URI_PERMISSION, "Can't grant URI permission no uid for: " + targetPkg);
                        }
                        return -1;
                    }
                } catch (RemoteException e) {
                    return -1;
                }
            }
            if (targetUid < 0) {
                allowed = pi.exported;
                if (!((modeFlags & 1) == 0 || pi.readPermission == null)) {
                    allowed = false;
                }
                if (!((modeFlags & 2) == 0 || pi.writePermission == null)) {
                    allowed = false;
                }
                if (allowed) {
                    return -1;
                }
            } else if (checkHoldingPermissionsLocked(pm, pi, grantUri, targetUid, modeFlags)) {
                if (ActivityManagerDebugConfig.DEBUG_URI_PERMISSION) {
                    Slog.v(TAG_URI_PERMISSION, "Target " + targetPkg + " already has full permission to " + grantUri);
                }
                return -1;
            }
            if (UserHandle.getUserId(targetUid) != grantUri.sourceUserId) {
                specialCrossUserGrant = checkHoldingPermissionsInternalLocked(pm, pi, grantUri, callingUid, modeFlags, false);
            } else {
                specialCrossUserGrant = false;
            }
            if (!specialCrossUserGrant) {
                if (!pi.grantUriPermissions) {
                    throw new SecurityException("Provider " + pi.packageName + "/" + pi.name + " does not allow granting of Uri permissions (uri " + grantUri + ")");
                } else if (pi.uriPermissionPatterns != null) {
                    int N = pi.uriPermissionPatterns.length;
                    allowed = false;
                    int i = 0;
                    while (i < N) {
                        if (pi.uriPermissionPatterns[i] != null && pi.uriPermissionPatterns[i].match(grantUri.uri.getPath())) {
                            allowed = true;
                            break;
                        }
                        i++;
                    }
                    if (!allowed) {
                        throw new SecurityException("Provider " + pi.packageName + "/" + pi.name + " does not allow granting of permission to path of Uri " + grantUri);
                    }
                }
            }
            if (UserHandle.getAppId(callingUid) == 1000 || checkHoldingPermissionsLocked(pm, pi, grantUri, callingUid, modeFlags) || checkUriPermissionLocked(grantUri, callingUid, modeFlags)) {
                return targetUid;
            }
            throw new SecurityException("Uid " + callingUid + " does not have permission to uri " + grantUri);
        }
        if (ActivityManagerDebugConfig.DEBUG_URI_PERMISSION) {
            Slog.v(TAG_URI_PERMISSION, "Can't grant URI permission for non-content URI: " + grantUri);
        }
        return -1;
    }

    public int checkGrantUriPermission(int callingUid, String targetPkg, Uri uri, int modeFlags, int userId) {
        int checkGrantUriPermissionLocked;
        enforceNotIsolatedCaller("checkGrantUriPermission");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                checkGrantUriPermissionLocked = checkGrantUriPermissionLocked(callingUid, targetPkg, new GrantUri(userId, uri, false), modeFlags, -1);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return checkGrantUriPermissionLocked;
    }

    void grantUriPermissionUncheckedLocked(int targetUid, String targetPkg, GrantUri grantUri, int modeFlags, UriPermissionOwner owner) {
        if (Intent.isAccessUriMode(modeFlags)) {
            if (ActivityManagerDebugConfig.DEBUG_URI_PERMISSION) {
                Slog.v(TAG_URI_PERMISSION, "Granting " + targetPkg + "/" + targetUid + " permission to " + grantUri);
            }
            ProviderInfo pi = getProviderInfoLocked(grantUri.uri.getAuthority(), grantUri.sourceUserId, 268435456);
            if (pi == null) {
                Slog.w(TAG, "No content provider found for grant: " + grantUri.toSafeString());
                return;
            }
            if ((modeFlags & 128) != 0) {
                grantUri.prefix = true;
            }
            findOrCreateUriPermissionLocked(pi.packageName, targetPkg, targetUid, grantUri).grantModes(modeFlags, owner);
        }
    }

    void grantUriPermissionLocked(int callingUid, String targetPkg, GrantUri grantUri, int modeFlags, UriPermissionOwner owner, int targetUserId) {
        if (targetPkg == null) {
            throw new NullPointerException(ATTR_TARGET_PKG);
        }
        try {
            int targetUid = checkGrantUriPermissionLocked(callingUid, targetPkg, grantUri, modeFlags, AppGlobals.getPackageManager().getPackageUid(targetPkg, 268435456, targetUserId));
            if (targetUid >= 0) {
                grantUriPermissionUncheckedLocked(targetUid, targetPkg, grantUri, modeFlags, owner);
            }
        } catch (RemoteException e) {
        }
    }

    NeededUriGrants checkGrantUriPermissionFromIntentLocked(int callingUid, String targetPkg, Intent intent, int mode, NeededUriGrants needed, int targetUserId) {
        if (ActivityManagerDebugConfig.DEBUG_URI_PERMISSION) {
            Slog.v(TAG_URI_PERMISSION, "Checking URI perm to data=" + (intent != null ? intent.getData() : null) + " clip=" + (intent != null ? intent.getClipData() : null) + " from " + intent + "; flags=0x" + Integer.toHexString(intent != null ? intent.getFlags() : 0));
        }
        if (targetPkg == null) {
            throw new NullPointerException(ATTR_TARGET_PKG);
        } else if (intent == null) {
            return null;
        } else {
            Uri data = intent.getData();
            ClipData clip = intent.getClipData();
            if (data == null && clip == null) {
                return null;
            }
            int targetUid;
            GrantUri grantUri;
            NeededUriGrants neededUriGrants;
            int contentUserHint = intent.getContentUserHint();
            if (contentUserHint == -2) {
                contentUserHint = UserHandle.getUserId(callingUid);
            }
            IPackageManager pm = AppGlobals.getPackageManager();
            if (needed != null) {
                targetUid = needed.targetUid;
            } else {
                try {
                    targetUid = pm.getPackageUid(targetPkg, 268435456, targetUserId);
                    if (targetUid < 0) {
                        if (ActivityManagerDebugConfig.DEBUG_URI_PERMISSION) {
                            Slog.v(TAG_URI_PERMISSION, "Can't grant URI permission no uid for: " + targetPkg + " on user " + targetUserId);
                        }
                        return null;
                    }
                } catch (RemoteException e) {
                    return null;
                }
            }
            if (data != null) {
                grantUri = GrantUri.resolve(contentUserHint, data);
                targetUid = checkGrantUriPermissionLocked(callingUid, targetPkg, grantUri, mode, targetUid);
                if (targetUid > 0) {
                    if (needed == null) {
                        neededUriGrants = new NeededUriGrants(targetPkg, targetUid, mode);
                    }
                    needed.add(grantUri);
                }
            }
            if (clip != null) {
                for (int i = 0; i < clip.getItemCount(); i++) {
                    Uri uri = clip.getItemAt(i).getUri();
                    if (uri != null) {
                        grantUri = GrantUri.resolve(contentUserHint, uri);
                        targetUid = checkGrantUriPermissionLocked(callingUid, targetPkg, grantUri, mode, targetUid);
                        if (targetUid > 0) {
                            if (needed == null) {
                                neededUriGrants = new NeededUriGrants(targetPkg, targetUid, mode);
                            }
                            needed.add(grantUri);
                        }
                    } else {
                        Intent clipIntent = clip.getItemAt(i).getIntent();
                        if (clipIntent != null) {
                            NeededUriGrants newNeeded = checkGrantUriPermissionFromIntentLocked(callingUid, targetPkg, clipIntent, mode, needed, targetUserId);
                            if (newNeeded != null) {
                                needed = newNeeded;
                            }
                        }
                    }
                }
            }
            return needed;
        }
    }

    void grantUriPermissionUncheckedFromIntentLocked(NeededUriGrants needed, UriPermissionOwner owner) {
        if (needed != null) {
            for (int i = 0; i < needed.size(); i++) {
                grantUriPermissionUncheckedLocked(needed.targetUid, needed.targetPkg, (GrantUri) needed.get(i), needed.flags, owner);
            }
        }
    }

    void grantUriPermissionFromIntentLocked(int callingUid, String targetPkg, Intent intent, UriPermissionOwner owner, int targetUserId) {
        NeededUriGrants needed = checkGrantUriPermissionFromIntentLocked(callingUid, targetPkg, intent, intent != null ? intent.getFlags() : 0, null, targetUserId);
        if (needed != null) {
            grantUriPermissionUncheckedFromIntentLocked(needed, owner);
        }
    }

    public void grantUriPermission(IApplicationThread caller, String targetPkg, Uri uri, int modeFlags, int userId) {
        enforceNotIsolatedCaller("grantUriPermission");
        GrantUri grantUri = new GrantUri(userId, uri, false);
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ProcessRecord r = getRecordForAppLocked(caller);
                if (r == null) {
                    throw new SecurityException("Unable to find app for caller " + caller + " when granting permission to uri " + grantUri);
                } else if (targetPkg == null) {
                    throw new IllegalArgumentException("null target");
                } else if (grantUri == null) {
                    throw new IllegalArgumentException("null uri");
                } else {
                    Preconditions.checkFlagsArgument(modeFlags, HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_MINUS);
                    grantUriPermissionLocked(r.uid, targetPkg, grantUri, modeFlags, null, UserHandle.getUserId(r.uid));
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    void removeUriPermissionIfNeededLocked(UriPermission perm) {
        if (perm.modeFlags == 0) {
            ArrayMap<GrantUri, UriPermission> perms = (ArrayMap) this.mGrantedUriPermissions.get(perm.targetUid);
            if (perms != null) {
                if (ActivityManagerDebugConfig.DEBUG_URI_PERMISSION) {
                    Slog.v(TAG_URI_PERMISSION, "Removing " + perm.targetUid + " permission to " + perm.uri);
                }
                perms.remove(perm.uri);
                if (perms.isEmpty()) {
                    this.mGrantedUriPermissions.remove(perm.targetUid);
                }
            }
        }
    }

    private void revokeUriPermissionLocked(int callingUid, GrantUri grantUri, int modeFlags) {
        if (ActivityManagerDebugConfig.DEBUG_URI_PERMISSION) {
            Slog.v(TAG_URI_PERMISSION, "Revoking all granted permissions to " + grantUri);
        }
        IPackageManager pm = AppGlobals.getPackageManager();
        ProviderInfo pi = getProviderInfoLocked(grantUri.uri.getAuthority(), grantUri.sourceUserId, 786432);
        if (pi == null) {
            Slog.w(TAG, "No content provider found for permission revoke: " + grantUri.toSafeString());
        } else if (checkHoldingPermissionsLocked(pm, pi, grantUri, callingUid, modeFlags)) {
            boolean persistChanged = false;
            int N = this.mGrantedUriPermissions.size();
            int i = 0;
            while (i < N) {
                int targetUid = this.mGrantedUriPermissions.keyAt(i);
                perms = (ArrayMap) this.mGrantedUriPermissions.valueAt(i);
                it = perms.values().iterator();
                while (it.hasNext()) {
                    perm = (UriPermission) it.next();
                    if (perm.uri.sourceUserId == grantUri.sourceUserId && perm.uri.uri.isPathPrefixMatch(grantUri.uri)) {
                        if (ActivityManagerDebugConfig.DEBUG_URI_PERMISSION) {
                            Slog.v(TAG_URI_PERMISSION, "Revoking " + perm.targetUid + " permission to " + perm.uri);
                        }
                        persistChanged |= perm.revokeModes(modeFlags | 64, true);
                        if (perm.modeFlags == 0) {
                            it.remove();
                        }
                    }
                }
                if (perms.isEmpty()) {
                    this.mGrantedUriPermissions.remove(targetUid);
                    N--;
                    i--;
                }
                i++;
            }
            if (persistChanged) {
                schedulePersistUriGrants();
            }
        } else {
            perms = (ArrayMap) this.mGrantedUriPermissions.get(callingUid);
            if (perms != null) {
                int persistChanged2 = 0;
                it = perms.values().iterator();
                while (it.hasNext()) {
                    perm = (UriPermission) it.next();
                    if (perm.uri.sourceUserId == grantUri.sourceUserId && perm.uri.uri.isPathPrefixMatch(grantUri.uri)) {
                        if (ActivityManagerDebugConfig.DEBUG_URI_PERMISSION) {
                            Slog.v(TAG_URI_PERMISSION, "Revoking non-owned " + perm.targetUid + " permission to " + perm.uri);
                        }
                        persistChanged2 |= perm.revokeModes(modeFlags | 64, false);
                        if (perm.modeFlags == 0) {
                            it.remove();
                        }
                    }
                }
                if (perms.isEmpty()) {
                    this.mGrantedUriPermissions.remove(callingUid);
                }
                if (persistChanged2 != 0) {
                    schedulePersistUriGrants();
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void revokeUriPermission(IApplicationThread caller, Uri uri, int modeFlags, int userId) {
        enforceNotIsolatedCaller("revokeUriPermission");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ProcessRecord r = getRecordForAppLocked(caller);
                if (r == null) {
                    throw new SecurityException("Unable to find app for caller " + caller + " when revoking permission to uri " + uri);
                } else if (uri == null) {
                    Slog.w(TAG, "revokeUriPermission: null uri");
                } else if (!Intent.isAccessUriMode(modeFlags)) {
                    resetPriorityAfterLockedSection();
                } else if (getProviderInfoLocked(uri.getAuthority(), userId, 786432) == null) {
                    Slog.w(TAG, "No content provider found for permission revoke: " + uri.toSafeString());
                    resetPriorityAfterLockedSection();
                } else {
                    revokeUriPermissionLocked(r.uid, new GrantUri(userId, uri, false), modeFlags);
                    resetPriorityAfterLockedSection();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private void removeUriPermissionsForPackageLocked(String packageName, int userHandle, boolean persistable) {
        if (userHandle == -1 && packageName == null) {
            throw new IllegalArgumentException("Must narrow by either package or user");
        }
        int persistChanged = 0;
        int N = this.mGrantedUriPermissions.size();
        int i = 0;
        while (i < N) {
            int targetUid = this.mGrantedUriPermissions.keyAt(i);
            ArrayMap<GrantUri, UriPermission> perms = (ArrayMap) this.mGrantedUriPermissions.valueAt(i);
            if (userHandle == -1 || userHandle == UserHandle.getUserId(targetUid)) {
                Iterator<UriPermission> it = perms.values().iterator();
                while (it.hasNext()) {
                    UriPermission perm = (UriPermission) it.next();
                    if ((packageName == null || perm.sourcePkg.equals(packageName) || perm.targetPkg.equals(packageName)) && (!"downloads".equals(perm.uri.uri.getAuthority()) || persistable)) {
                        int i2;
                        if (persistable) {
                            i2 = -1;
                        } else {
                            i2 = -65;
                        }
                        persistChanged |= perm.revokeModes(i2, true);
                        if (perm.modeFlags == 0) {
                            it.remove();
                        }
                    }
                }
                if (perms.isEmpty()) {
                    this.mGrantedUriPermissions.remove(targetUid);
                    N--;
                    i--;
                }
            }
            i++;
        }
        if (persistChanged != 0) {
            schedulePersistUriGrants();
        }
    }

    public IBinder newUriPermissionOwner(String name) {
        IBinder externalTokenLocked;
        enforceNotIsolatedCaller("newUriPermissionOwner");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                externalTokenLocked = new UriPermissionOwner(this, name).getExternalTokenLocked();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return externalTokenLocked;
    }

    public IBinder getUriPermissionOwnerForActivity(IBinder activityToken) {
        IBinder externalTokenLocked;
        enforceNotIsolatedCaller("getUriPermissionOwnerForActivity");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(activityToken);
                if (r == null) {
                    throw new IllegalArgumentException("Activity does not exist; token=" + activityToken);
                }
                externalTokenLocked = r.getUriPermissionsLocked().getExternalTokenLocked();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return externalTokenLocked;
    }

    public void grantUriPermissionFromOwner(IBinder token, int fromUid, String targetPkg, Uri uri, int modeFlags, int sourceUserId, int targetUserId) {
        targetUserId = this.mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), targetUserId, false, 2, "grantUriPermissionFromOwner", null);
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                UriPermissionOwner owner = UriPermissionOwner.fromExternalToken(token);
                if (owner == null) {
                    throw new IllegalArgumentException("Unknown owner: " + token);
                } else if (fromUid != Binder.getCallingUid() && Binder.getCallingUid() != Process.myUid()) {
                    throw new SecurityException("nice try");
                } else if (targetPkg == null) {
                    throw new IllegalArgumentException("null target");
                } else if (uri == null) {
                    throw new IllegalArgumentException("null uri");
                } else {
                    grantUriPermissionLocked(fromUid, targetPkg, new GrantUri(sourceUserId, uri, false), modeFlags, owner, targetUserId);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void revokeUriPermissionFromOwner(IBinder token, Uri uri, int mode, int userId) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                UriPermissionOwner owner = UriPermissionOwner.fromExternalToken(token);
                if (owner == null) {
                    throw new IllegalArgumentException("Unknown owner: " + token);
                }
                if (uri == null) {
                    owner.removeUriPermissionsLocked(mode);
                } else {
                    owner.removeUriPermissionLocked(new GrantUri(userId, uri, false), mode);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private void schedulePersistUriGrants() {
        if (!this.mHandler.hasMessages(38)) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(38), JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
        }
    }

    private void writeGrantedUriPermissions() {
        if (ActivityManagerDebugConfig.DEBUG_URI_PERMISSION) {
            Slog.v(TAG_URI_PERMISSION, "writeGrantedUriPermissions()");
        }
        ArrayList<Snapshot> persist = Lists.newArrayList();
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                int size = this.mGrantedUriPermissions.size();
                for (int i = 0; i < size; i++) {
                    for (UriPermission perm : ((ArrayMap) this.mGrantedUriPermissions.valueAt(i)).values()) {
                        if (perm.persistedModeFlags != 0) {
                            persist.add(perm.snapshot());
                        }
                    }
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = this.mGrantFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fileOutputStream, StandardCharsets.UTF_8.name());
            out.startDocument(null, Boolean.valueOf(true));
            out.startTag(null, TAG_URI_GRANTS);
            for (Snapshot perm2 : persist) {
                out.startTag(null, TAG_URI_GRANT);
                XmlUtils.writeIntAttribute(out, ATTR_SOURCE_USER_ID, perm2.uri.sourceUserId);
                XmlUtils.writeIntAttribute(out, ATTR_TARGET_USER_ID, perm2.targetUserId);
                out.attribute(null, ATTR_SOURCE_PKG, perm2.sourcePkg);
                out.attribute(null, ATTR_TARGET_PKG, perm2.targetPkg);
                out.attribute(null, ATTR_URI, String.valueOf(perm2.uri.uri));
                XmlUtils.writeBooleanAttribute(out, ATTR_PREFIX, perm2.uri.prefix);
                XmlUtils.writeIntAttribute(out, ATTR_MODE_FLAGS, perm2.persistedModeFlags);
                XmlUtils.writeLongAttribute(out, ATTR_CREATED_TIME, perm2.persistedCreateTime);
                out.endTag(null, TAG_URI_GRANT);
            }
            out.endTag(null, TAG_URI_GRANTS);
            out.endDocument();
            this.mGrantFile.finishWrite(fileOutputStream);
        } catch (IOException e) {
            if (fileOutputStream != null) {
                this.mGrantFile.failWrite(fileOutputStream);
            }
        }
    }

    private void readGrantedUriPermissionsLocked() {
        if (ActivityManagerDebugConfig.DEBUG_URI_PERMISSION) {
            Slog.v(TAG_URI_PERMISSION, "readGrantedUriPermissions()");
        }
        long now = System.currentTimeMillis();
        AutoCloseable autoCloseable = null;
        try {
            autoCloseable = this.mGrantFile.openRead();
            XmlPullParser in = Xml.newPullParser();
            in.setInput(autoCloseable, StandardCharsets.UTF_8.name());
            while (true) {
                int type = in.next();
                if (type == 1) {
                    break;
                }
                String tag = in.getName();
                if (type == 2 && TAG_URI_GRANT.equals(tag)) {
                    int sourceUserId;
                    int targetUserId;
                    int userHandle = XmlUtils.readIntAttribute(in, ATTR_USER_HANDLE, -10000);
                    if (userHandle != -10000) {
                        sourceUserId = userHandle;
                        targetUserId = userHandle;
                    } else {
                        sourceUserId = XmlUtils.readIntAttribute(in, ATTR_SOURCE_USER_ID);
                        targetUserId = XmlUtils.readIntAttribute(in, ATTR_TARGET_USER_ID);
                    }
                    String sourcePkg = in.getAttributeValue(null, ATTR_SOURCE_PKG);
                    String targetPkg = in.getAttributeValue(null, ATTR_TARGET_PKG);
                    Uri uri = Uri.parse(in.getAttributeValue(null, ATTR_URI));
                    boolean prefix = XmlUtils.readBooleanAttribute(in, ATTR_PREFIX);
                    int modeFlags = XmlUtils.readIntAttribute(in, ATTR_MODE_FLAGS);
                    long createdTime = XmlUtils.readLongAttribute(in, ATTR_CREATED_TIME, now);
                    ProviderInfo pi = getProviderInfoLocked(uri.getAuthority(), sourceUserId, 786432);
                    if (pi != null) {
                        if (sourcePkg.equals(pi.packageName)) {
                            int targetUid = -1;
                            try {
                                targetUid = AppGlobals.getPackageManager().getPackageUid(targetPkg, DumpState.DUMP_PREFERRED_XML, targetUserId);
                            } catch (RemoteException e) {
                            }
                            if (targetUid != -1) {
                                findOrCreateUriPermissionLocked(sourcePkg, targetPkg, targetUid, new GrantUri(sourceUserId, uri, prefix)).initPersistedModes(modeFlags, createdTime);
                            } else {
                                continue;
                            }
                        }
                    }
                    Slog.w(TAG, "Persisted grant for " + uri + " had source " + sourcePkg + " but instead found " + pi);
                }
            }
        } catch (FileNotFoundException e2) {
        } catch (IOException e3) {
            Slog.wtf(TAG, "Failed reading Uri grants", e3);
        } catch (XmlPullParserException e4) {
            Slog.wtf(TAG, "Failed reading Uri grants", e4);
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    public void takePersistableUriPermission(Uri uri, int modeFlags, int userId) {
        boolean z = true;
        enforceNotIsolatedCaller("takePersistableUriPermission");
        Preconditions.checkFlagsArgument(modeFlags, 3);
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                int callingUid = Binder.getCallingUid();
                boolean z2 = false;
                GrantUri grantUri = new GrantUri(userId, uri, false);
                UriPermission exactPerm = findUriPermissionLocked(callingUid, new GrantUri(userId, uri, false));
                UriPermission prefixPerm = findUriPermissionLocked(callingUid, new GrantUri(userId, uri, true));
                boolean exactValid = exactPerm != null ? (exactPerm.persistableModeFlags & modeFlags) == modeFlags : false;
                boolean prefixValid = prefixPerm != null ? (prefixPerm.persistableModeFlags & modeFlags) == modeFlags : false;
                if (!exactValid) {
                    z = prefixValid;
                }
                if (z) {
                    if (exactValid) {
                        z2 = exactPerm.takePersistableModes(modeFlags);
                    }
                    if (prefixValid) {
                        z2 |= prefixPerm.takePersistableModes(modeFlags);
                    }
                    if (z2 | maybePrunePersistedUriGrantsLocked(callingUid)) {
                        schedulePersistUriGrants();
                    }
                } else {
                    throw new SecurityException("No persistable permission grants found for UID " + callingUid + " and Uri " + grantUri.toSafeString());
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void releasePersistableUriPermission(Uri uri, int modeFlags, int userId) {
        enforceNotIsolatedCaller("releasePersistableUriPermission");
        Preconditions.checkFlagsArgument(modeFlags, 3);
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                int callingUid = Binder.getCallingUid();
                boolean z = false;
                UriPermission exactPerm = findUriPermissionLocked(callingUid, new GrantUri(userId, uri, false));
                UriPermission prefixPerm = findUriPermissionLocked(callingUid, new GrantUri(userId, uri, true));
                if (exactPerm == null && prefixPerm == null) {
                    throw new SecurityException("No permission grants found for UID " + callingUid + " and Uri " + uri.toSafeString());
                }
                if (exactPerm != null) {
                    z = exactPerm.releasePersistableModes(modeFlags);
                    removeUriPermissionIfNeededLocked(exactPerm);
                }
                if (prefixPerm != null) {
                    z |= prefixPerm.releasePersistableModes(modeFlags);
                    removeUriPermissionIfNeededLocked(prefixPerm);
                }
                if (z) {
                    schedulePersistUriGrants();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private boolean maybePrunePersistedUriGrantsLocked(int uid) {
        ArrayMap<GrantUri, UriPermission> perms = (ArrayMap) this.mGrantedUriPermissions.get(uid);
        if (perms == null || perms.size() < 128) {
            return false;
        }
        UriPermission perm;
        ArrayList<UriPermission> persisted = Lists.newArrayList();
        for (UriPermission perm2 : perms.values()) {
            if (perm2.persistedModeFlags != 0) {
                persisted.add(perm2);
            }
        }
        int trimCount = persisted.size() - 128;
        if (trimCount <= 0) {
            return false;
        }
        Collections.sort(persisted, new PersistedTimeComparator());
        for (int i = 0; i < trimCount; i++) {
            perm2 = (UriPermission) persisted.get(i);
            if (ActivityManagerDebugConfig.DEBUG_URI_PERMISSION) {
                Slog.v(TAG_URI_PERMISSION, "Trimming grant created at " + perm2.persistedCreateTime);
            }
            perm2.releasePersistableModes(-1);
            removeUriPermissionIfNeededLocked(perm2);
        }
        return true;
    }

    public ParceledListSlice<UriPermission> getPersistedUriPermissions(String packageName, boolean incoming) {
        enforceNotIsolatedCaller("getPersistedUriPermissions");
        Preconditions.checkNotNull(packageName, "packageName");
        int callingUid = Binder.getCallingUid();
        try {
            if (AppGlobals.getPackageManager().getPackageUid(packageName, 786432, UserHandle.getUserId(callingUid)) != callingUid) {
                throw new SecurityException("Package " + packageName + " does not belong to calling UID " + callingUid);
            }
            ArrayList<UriPermission> result = Lists.newArrayList();
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    if (incoming) {
                        ArrayMap<GrantUri, UriPermission> perms = (ArrayMap) this.mGrantedUriPermissions.get(callingUid);
                        if (perms == null) {
                            Slog.w(TAG, "No permission grants found for " + packageName);
                        } else {
                            for (UriPermission perm : perms.values()) {
                                if (packageName.equals(perm.targetPkg) && perm.persistedModeFlags != 0) {
                                    result.add(perm.buildPersistedPublicApiObject());
                                }
                            }
                        }
                    } else {
                        int size = this.mGrantedUriPermissions.size();
                        for (int i = 0; i < size; i++) {
                            for (UriPermission perm2 : ((ArrayMap) this.mGrantedUriPermissions.valueAt(i)).values()) {
                                if (packageName.equals(perm2.sourcePkg) && perm2.persistedModeFlags != 0) {
                                    result.add(perm2.buildPersistedPublicApiObject());
                                }
                            }
                        }
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return new ParceledListSlice(result);
        } catch (RemoteException e) {
            throw new SecurityException("Failed to verify package name ownership");
        }
    }

    public ParceledListSlice<UriPermission> getGrantedUriPermissions(String packageName, int userId) {
        enforceCallingPermission("android.permission.GET_APP_GRANTED_URI_PERMISSIONS", "getGrantedUriPermissions");
        ArrayList<UriPermission> result = Lists.newArrayList();
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                int size = this.mGrantedUriPermissions.size();
                for (int i = 0; i < size; i++) {
                    for (UriPermission perm : ((ArrayMap) this.mGrantedUriPermissions.valueAt(i)).values()) {
                        if (packageName.equals(perm.targetPkg) && perm.targetUserId == userId && perm.persistedModeFlags != 0) {
                            result.add(perm.buildPersistedPublicApiObject());
                        }
                    }
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return new ParceledListSlice(result);
    }

    public void clearGrantedUriPermissions(String packageName, int userId) {
        enforceCallingPermission("android.permission.CLEAR_APP_GRANTED_URI_PERMISSIONS", "clearGrantedUriPermissions");
        removeUriPermissionsForPackageLocked(packageName, userId, true);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void showWaitingForDebugger(IApplicationThread who, boolean waiting) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                Object recordForAppLocked = who != null ? getRecordForAppLocked(who) : null;
                if (recordForAppLocked == null) {
                } else {
                    Message msg = Message.obtain();
                    msg.what = 6;
                    msg.obj = recordForAppLocked;
                    msg.arg1 = waiting ? 1 : 0;
                    this.mUiHandler.sendMessage(msg);
                    resetPriorityAfterLockedSection();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void getMemoryInfo(ActivityManager.MemoryInfo outInfo) {
        boolean z;
        long homeAppMem = this.mProcessList.getMemLevel(600);
        long cachedAppMem = this.mProcessList.getMemLevel(900);
        outInfo.availMem = Process.getFreeMemory();
        outInfo.totalMem = Process.getTotalMemory();
        outInfo.threshold = homeAppMem;
        if (outInfo.availMem < ((cachedAppMem - homeAppMem) / 2) + homeAppMem) {
            z = true;
        } else {
            z = false;
        }
        outInfo.lowMemory = z;
        outInfo.hiddenAppThreshold = cachedAppMem;
        outInfo.secondaryServerThreshold = this.mProcessList.getMemLevel(500);
        outInfo.visibleAppThreshold = this.mProcessList.getMemLevel(100);
        outInfo.foregroundAppThreshold = this.mProcessList.getMemLevel(0);
    }

    public List<IAppTask> getAppTasks(String callingPackage) {
        ArrayList<IAppTask> list;
        int callingUid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                list = new ArrayList();
                if (ActivityManagerDebugConfig.DEBUG_ALL) {
                    Slog.v(TAG, "getAppTasks");
                }
                int N = this.mRecentTasks.size();
                for (int i = 0; i < N; i++) {
                    TaskRecord tr = (TaskRecord) this.mRecentTasks.get(i);
                    if (tr.effectiveUid == callingUid) {
                        Intent intent = tr.getBaseIntent();
                        if (intent != null && callingPackage.equals(intent.getComponent().getPackageName())) {
                            list.add(new AppTaskImpl(createRecentTaskInfoFromTaskRecord(tr).persistentId, callingUid));
                        }
                    }
                }
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
        return list;
    }

    public List<RunningTaskInfo> getTasks(int maxNum, int flags) {
        int callingUid = Binder.getCallingUid();
        ArrayList<RunningTaskInfo> list = new ArrayList();
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if (ActivityManagerDebugConfig.DEBUG_ALL) {
                    Slog.v(TAG, "getTasks: max=" + maxNum + ", flags=" + flags);
                }
                this.mStackSupervisor.getTasksLocked(maxNum, list, callingUid, isGetTasksAllowed("getTasks", Binder.getCallingPid(), callingUid));
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return list;
    }

    private RecentTaskInfo createRecentTaskInfoFromTaskRecord(TaskRecord tr) {
        ComponentName component;
        int i = -1;
        ComponentName componentName = null;
        tr.updateTaskDescription();
        RecentTaskInfo rti = new RecentTaskInfo();
        rti.id = tr.getTopActivity() == null ? -1 : tr.taskId;
        rti.persistentId = tr.taskId;
        rti.baseIntent = new Intent(tr.getBaseIntent());
        rti.origActivity = tr.origActivity;
        rti.realActivity = tr.realActivity;
        rti.description = tr.lastDescription;
        if (tr.stack != null) {
            i = tr.stack.mStackId;
        }
        rti.stackId = i;
        rti.userId = tr.userId;
        rti.taskDescription = new TaskDescription(tr.lastTaskDescription);
        rti.firstActiveTime = tr.firstActiveTime;
        rti.lastActiveTime = tr.lastActiveTime;
        rti.affiliatedTaskId = tr.mAffiliatedTaskId;
        rti.affiliatedTaskColor = tr.mAffiliatedTaskColor;
        rti.numActivities = 0;
        if (tr.mBounds != null) {
            rti.bounds = new Rect(tr.mBounds);
        }
        rti.isDockable = tr.canGoInDockedStack();
        rti.resizeMode = tr.mResizeMode;
        ActivityRecord base = null;
        ActivityRecord top = null;
        for (int i2 = tr.mActivities.size() - 1; i2 >= 0; i2--) {
            ActivityRecord tmp = (ActivityRecord) tr.mActivities.get(i2);
            if (!tmp.finishing) {
                base = tmp;
                if (top == null || top.state == ActivityState.INITIALIZING) {
                    top = tmp;
                }
                rti.numActivities++;
            }
        }
        if (base != null) {
            component = base.intent.getComponent();
        } else {
            component = null;
        }
        rti.baseActivity = component;
        if (top != null) {
            componentName = top.intent.getComponent();
        }
        rti.topActivity = componentName;
        return rti;
    }

    private boolean isGetTasksAllowed(String caller, int callingPid, int callingUid) {
        boolean z = false;
        if (checkPermission("android.permission.REAL_GET_TASKS", callingPid, callingUid) == 0) {
            z = true;
        }
        if (!z && checkPermission("android.permission.GET_TASKS", callingPid, callingUid) == 0) {
            try {
                if (AppGlobals.getPackageManager().isUidPrivileged(callingUid)) {
                    z = true;
                    if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        Slog.w(TAG, caller + ": caller " + callingUid + " is using old GET_TASKS but privileged; allowing");
                    }
                }
            } catch (RemoteException e) {
                Slog.w(TAG, caller + ": caller " + callingUid + " check allowed ex:", e);
            }
        }
        if (!z) {
            Slog.w(TAG, caller + ": caller " + callingUid + " does not hold REAL_GET_TASKS; limiting output");
        }
        return z;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ParceledListSlice<RecentTaskInfo> getRecentTasks(int maxNum, int flags, int userId) {
        int callingUid = Binder.getCallingUid();
        userId = this.mUserController.handleIncomingUser(Binder.getCallingPid(), callingUid, userId, false, 2, "getRecentTasks", null);
        boolean includeProfiles = (flags & 4) != 0;
        boolean withExcluded = (flags & 1) != 0;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                boolean allowed = isGetTasksAllowed("getRecentTasks", Binder.getCallingPid(), callingUid);
                boolean detailed = checkCallingPermission("android.permission.GET_DETAILED_TASKS") == 0;
                ParceledListSlice<RecentTaskInfo> parceledListSlice;
                if (isUserRunning(userId, 4)) {
                    int i;
                    Set<Integer> includedUsers;
                    this.mRecentTasks.loadUserRecentsLocked(userId);
                    int recentsCount = this.mRecentTasks.size();
                    if (maxNum < recentsCount) {
                        i = maxNum;
                    } else {
                        i = recentsCount;
                    }
                    ArrayList<RecentTaskInfo> arrayList = new ArrayList(i);
                    if (includeProfiles) {
                        includedUsers = this.mUserController.getProfileIds(userId);
                    } else {
                        includedUsers = new HashSet();
                    }
                    includedUsers.add(Integer.valueOf(userId));
                    for (int i2 = 0; i2 < recentsCount && maxNum > 0; i2++) {
                        TaskRecord tr = (TaskRecord) this.mRecentTasks.get(i2);
                        if (!includedUsers.contains(Integer.valueOf(tr.userId))) {
                            Slog.d(TAG_RECENTS, "Skipping, not user: " + tr);
                        } else if (tr.realActivitySuspended) {
                            Slog.d(TAG_RECENTS, "Skipping, activity suspended: " + tr);
                        } else {
                            if (!withExcluded) {
                                if (!(tr.intent == null || (tr.intent.getFlags() & 8388608) == 0)) {
                                    Slog.d(TAG_RECENTS, "Skipping, withExcluded: " + withExcluded + ", tr.intent:" + tr.intent);
                                }
                            }
                            if (!allowed && !tr.isHomeTask() && tr.effectiveUid != callingUid) {
                                Slog.d(TAG_RECENTS, "Skipping, not allowed: " + tr);
                            } else if ((flags & 8) == 0 || tr.stack == null || !tr.stack.isHomeStack()) {
                                if ((flags & 16) != 0) {
                                    ActivityStack stack = tr.stack;
                                    if (stack != null && stack.isDockedStack() && stack.topTask() == tr) {
                                        Slog.d(TAG_RECENTS, "Skipping, top task in docked stack: " + tr);
                                    }
                                }
                                if ((flags & 32) != 0 && tr.stack != null && tr.stack.isPinnedStack()) {
                                    Slog.d(TAG_RECENTS, "Skipping, pinned stack task: " + tr);
                                } else if (tr.autoRemoveRecents && tr.getTopActivity() == null) {
                                    Slog.d(TAG_RECENTS, "Skipping, auto-remove without activity: " + tr);
                                } else if ((flags & 2) != 0 && !tr.isAvailable) {
                                    Slog.d(TAG_RECENTS, "Skipping, unavail real act: " + tr);
                                } else if (tr.mUserSetupComplete) {
                                    RecentTaskInfo rti = createRecentTaskInfoFromTaskRecord(tr);
                                    rti.baseIntent.addHwFlags(tr.multiLaunchId != 0 ? 1 : 0);
                                    if (!detailed) {
                                        rti.baseIntent.replaceExtras((Bundle) null);
                                    }
                                    arrayList.add(rti);
                                    maxNum--;
                                } else {
                                    Slog.d(TAG_RECENTS, "Skipping, user setup not complete: " + tr);
                                }
                            } else {
                                Slog.d(TAG_RECENTS, "Skipping, home stack task: " + tr);
                            }
                        }
                    }
                    HwSlog.d(TAG, "getRecentTasks: num=" + arrayList.size() + ",flags=" + flags + ",totalTasks=" + recentsCount);
                    if (arrayList.size() > 0) {
                        HwSlog.d(TAG, "getRecentTasks: topActivity=" + ((RecentTaskInfo) arrayList.get(0)).topActivity);
                    }
                    parceledListSlice = new ParceledListSlice(arrayList);
                    resetPriorityAfterLockedSection();
                    return parceledListSlice;
                }
                Slog.i(TAG, "user " + userId + " is still locked. Cannot load recents");
                parceledListSlice = ParceledListSlice.emptyList();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public TaskThumbnail getTaskThumbnail(int id) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                enforceCallingPermission("android.permission.READ_FRAME_BUFFER", "getTaskThumbnail()");
                TaskRecord tr = this.mStackSupervisor.anyTaskForIdLocked(id, false, -1);
                if (tr != null) {
                    TaskThumbnail taskThumbnailLocked = tr.getTaskThumbnailLocked();
                } else {
                    resetPriorityAfterLockedSection();
                    return null;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public int addAppTask(IBinder activityToken, Intent intent, TaskDescription description, Bitmap thumbnail) throws RemoteException {
        int callingUid = Binder.getCallingUid();
        long callingIdent = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(activityToken);
                if (r == null) {
                    throw new IllegalArgumentException("Activity does not exist; token=" + activityToken);
                }
                ComponentName comp = intent.getComponent();
                if (comp == null) {
                    throw new IllegalArgumentException("Intent " + intent + " must specify explicit component");
                } else if (thumbnail.getWidth() == this.mThumbnailWidth && thumbnail.getHeight() == this.mThumbnailHeight) {
                    if (intent.getSelector() != null) {
                        intent.setSelector(null);
                    }
                    if (intent.getSourceBounds() != null) {
                        intent.setSourceBounds(null);
                    }
                    if ((intent.getFlags() & DumpState.DUMP_FROZEN) != 0) {
                        if ((intent.getFlags() & DumpState.DUMP_PREFERRED_XML) == 0) {
                            intent.addFlags(DumpState.DUMP_PREFERRED_XML);
                        }
                    } else if ((intent.getFlags() & 268435456) != 0) {
                        intent.addFlags(268435456);
                    }
                    if (!(comp.equals(this.mLastAddedTaskComponent) && callingUid == this.mLastAddedTaskUid)) {
                        this.mLastAddedTaskActivity = null;
                    }
                    ActivityInfo ainfo = this.mLastAddedTaskActivity;
                    if (ainfo == null) {
                        ainfo = AppGlobals.getPackageManager().getActivityInfo(comp, 0, UserHandle.getUserId(callingUid));
                        this.mLastAddedTaskActivity = ainfo;
                        if (!(ainfo == null || ainfo.applicationInfo.uid == callingUid)) {
                            throw new SecurityException("Can't add task for another application: target uid=" + ainfo.applicationInfo.uid + ", calling uid=" + callingUid);
                        }
                    }
                    Point displaySize = new Point();
                    TaskThumbnailInfo thumbnailInfo = new TaskThumbnailInfo();
                    r.task.stack.getDisplaySize(displaySize);
                    thumbnailInfo.taskWidth = displaySize.x;
                    thumbnailInfo.taskHeight = displaySize.y;
                    thumbnailInfo.screenOrientation = this.mConfiguration.orientation;
                    TaskRecord task = HwServiceFactory.createTaskRecord(this, this.mStackSupervisor.getNextTaskIdForUserLocked(r.userId), ainfo, intent, description, thumbnailInfo);
                    if (this.mRecentTasks.trimForTaskLocked(task, false) >= 0) {
                        resetPriorityAfterLockedSection();
                        Binder.restoreCallingIdentity(callingIdent);
                        return -1;
                    }
                    int N = this.mRecentTasks.size();
                    if (N >= ActivityManager.getMaxRecentTasksStatic() - 1) {
                        ((TaskRecord) this.mRecentTasks.remove(N - 1)).removedFromRecents();
                    }
                    HwSlog.d(TAG, "adding App task: " + task);
                    task.inRecents = true;
                    this.mRecentTasks.add(task);
                    r.task.stack.addTask(task, false, "addAppTask");
                    task.setLastThumbnailLocked(thumbnail);
                    task.freeLastThumbnail();
                    int i = task.taskId;
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(callingIdent);
                    return i;
                } else {
                    throw new IllegalArgumentException("Bad thumbnail size: got " + thumbnail.getWidth() + "x" + thumbnail.getHeight() + ", require " + this.mThumbnailWidth + "x" + this.mThumbnailHeight);
                }
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingIdent);
        }
    }

    public Point getAppTaskThumbnailSize() {
        Point point;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                point = new Point(this.mThumbnailWidth, this.mThumbnailHeight);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return point;
    }

    public void setTaskDescription(IBinder token, TaskDescription td) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    r.setTaskDescription(td);
                    r.task.updateTaskDescription();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setTaskResizeable(int taskId, int resizeableMode) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                TaskRecord task = this.mStackSupervisor.anyTaskForIdLocked(taskId, false, -1);
                if (task == null) {
                    Slog.w(TAG, "setTaskResizeable: taskId=" + taskId + " not found");
                } else if (task.mResizeMode != resizeableMode) {
                    task.mResizeMode = resizeableMode;
                    this.mWindowManager.setTaskResizeable(taskId, resizeableMode);
                    this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
                    this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void resizeTask(int taskId, Rect bounds, int resizeMode) {
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "resizeTask()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                TaskRecord task = this.mStackSupervisor.anyTaskForIdLocked(taskId);
                if (task == null) {
                    Slog.w(TAG, "resizeTask: taskId=" + taskId + " not found");
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(ident);
                    return;
                }
                int stackId = task.stack.mStackId;
                if (bounds != null && task.inCropWindowsResizeMode() && this.mStackSupervisor.isStackDockedInEffect(stackId)) {
                    this.mWindowManager.scrollTask(task.taskId, bounds);
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(ident);
                } else if (StackId.isTaskResizeAllowed(stackId)) {
                    if (bounds == null && stackId == 2) {
                        stackId = 1;
                    } else if (!(bounds == null || stackId == 2)) {
                        stackId = 2;
                    }
                    boolean preserveWindow = (resizeMode & 1) != 0;
                    if (stackId != task.stack.mStackId) {
                        this.mStackSupervisor.moveTaskToStackUncheckedLocked(task, stackId, true, false, "resizeTask");
                        preserveWindow = false;
                    }
                    this.mStackSupervisor.resizeTaskLocked(task, bounds, resizeMode, preserveWindow, false);
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(ident);
                } else {
                    throw new IllegalArgumentException("resizeTask not allowed on task=" + task);
                }
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public Rect getTaskBounds(int taskId) {
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "getTaskBounds()");
        long ident = Binder.clearCallingIdentity();
        Rect rect = new Rect();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                TaskRecord task = this.mStackSupervisor.anyTaskForIdLocked(taskId, false, -1);
                if (task == null) {
                    Slog.w(TAG, "getTaskBounds: taskId=" + taskId + " not found");
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(ident);
                    return rect;
                }
                if (task.stack != null) {
                    this.mWindowManager.getTaskBounds(task.taskId, rect);
                } else if (task.mBounds != null) {
                    rect.set(task.mBounds);
                } else if (task.mLastNonFullscreenBounds != null) {
                    rect.set(task.mLastNonFullscreenBounds);
                }
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(ident);
                return rect;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public Bitmap getTaskDescriptionIcon(String filePath, int userId) {
        if (userId != UserHandle.getCallingUserId()) {
            enforceCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "getTaskDescriptionIcon");
        }
        if (new File(TaskPersister.getUserImagesDir(userId), new File(filePath).getName()).getPath().equals(filePath) && filePath.contains("_activity_icon_")) {
            return this.mRecentTasks.getTaskDescriptionIcon(filePath);
        }
        throw new IllegalArgumentException("Bad file path: " + filePath + " passed for userId " + userId);
    }

    public void startInPlaceAnimationOnFrontMostApplication(ActivityOptions opts) throws RemoteException {
        if (opts.getAnimationType() != 10 || opts.getCustomInPlaceResId() == 0) {
            throw new IllegalArgumentException("Expected in-place ActivityOption with valid animation");
        }
        this.mWindowManager.prepareAppTransition(17, false);
        this.mWindowManager.overridePendingAppTransitionInPlace(opts.getPackageName(), opts.getCustomInPlaceResId());
        this.mWindowManager.executeAppTransition();
    }

    private void cleanUpRemovedTaskLocked(TaskRecord tr, boolean killProcess, boolean removeFromRecents) {
        if (removeFromRecents) {
            this.mRecentTasks.remove(tr);
            tr.removedFromRecents();
        }
        ComponentName component = tr.getBaseIntent().getComponent();
        if (component == null) {
            Slog.w(TAG, "No component for base intent of task: " + tr);
            return;
        }
        this.mServices.cleanUpRemovedTaskLocked(tr, component, new Intent(tr.getBaseIntent()));
        if (killProcess) {
            String pkg = component.getPackageName();
            if (!shouldNotKillProcWhenRemoveTask(pkg)) {
                int i;
                if (getRecordCust() != null) {
                    getRecordCust().appExitRecord(pkg, "rkill");
                }
                ArrayList<ProcessRecord> procsToKill = new ArrayList();
                ArrayMap<String, SparseArray<ProcessRecord>> pmap = this.mProcessNames.getMap();
                for (i = 0; i < pmap.size(); i++) {
                    SparseArray<ProcessRecord> uids = (SparseArray) pmap.valueAt(i);
                    for (int j = 0; j < uids.size(); j++) {
                        ProcessRecord proc = (ProcessRecord) uids.valueAt(j);
                        if (proc.userId == tr.userId && proc != this.mHomeProcess && proc.pkgList.containsKey(pkg)) {
                            int k = 0;
                            while (k < proc.activities.size()) {
                                TaskRecord otherTask = ((ActivityRecord) proc.activities.get(k)).task;
                                if (tr.taskId == otherTask.taskId || !otherTask.inRecents) {
                                    k++;
                                } else {
                                    return;
                                }
                            }
                            if (!proc.foregroundServices) {
                                procsToKill.add(proc);
                            } else {
                                return;
                            }
                        }
                    }
                }
                for (i = 0; i < procsToKill.size(); i++) {
                    ProcessRecord pr = (ProcessRecord) procsToKill.get(i);
                    if (pr != null) {
                        if (pr.curAdj >= 900 && (pr.info.flags & 1) != 0 && (pr.info.hwFlags & 33554432) == 0) {
                            Slog.d(TAG, " the process " + pr.processName + " adj >= " + 900);
                            try {
                                SmartShrinker.reclaim(pr.pid, 4);
                                pr.thread.scheduleTrimMemory(80);
                            } catch (RemoteException e) {
                            }
                            pr.trimMemoryLevel = 80;
                        } else if (pr.setSchedGroup == 0 && pr.curReceiver == null) {
                            pr.kill("remove task", true);
                        } else {
                            pr.waitingToKill = "remove task";
                        }
                    }
                }
            }
        }
    }

    private void removeTasksByPackageNameLocked(String packageName, int userId) {
        for (int i = this.mRecentTasks.size() - 1; i >= 0; i--) {
            TaskRecord tr = (TaskRecord) this.mRecentTasks.get(i);
            if (tr.userId == userId) {
                ComponentName cn = tr.intent.getComponent();
                if (cn != null && cn.getPackageName().equals(packageName)) {
                    removeTaskByIdLocked(tr.taskId, true, true);
                }
            }
        }
    }

    private void cleanupDisabledPackageTasksLocked(String packageName, Set<String> filterByClasses, int userId) {
        for (int i = this.mRecentTasks.size() - 1; i >= 0; i--) {
            TaskRecord tr = (TaskRecord) this.mRecentTasks.get(i);
            if (userId == -1 || tr.userId == userId) {
                ComponentName cn = tr.intent.getComponent();
                boolean sameComponent = (cn == null || !cn.getPackageName().equals(packageName)) ? false : filterByClasses != null ? filterByClasses.contains(cn.getClassName()) : true;
                if (sameComponent) {
                    removeTaskByIdLocked(tr.taskId, false, true);
                }
            }
        }
    }

    boolean removeTaskByIdLocked(int taskId, boolean killProcess, boolean removeFromRecents) {
        TaskRecord tr = this.mStackSupervisor.anyTaskForIdLocked(taskId, false, -1);
        if (tr != null) {
            if (!(tr.getBaseIntent() == null || tr.getBaseIntent().getComponent() == null)) {
                String packageName = tr.getBaseIntent().getComponent().getPackageName();
                if (HwDeviceManager.disallowOp(3, packageName)) {
                    Slog.i(TAG, "[" + packageName + "] is Persistent app,won't be killed");
                    return false;
                }
            }
            tr.removeTaskActivitiesLocked();
            cleanUpRemovedTaskLocked(tr, killProcess, removeFromRecents);
            if (tr.isPersistable) {
                notifyTaskPersisterLocked(null, true);
            }
            return true;
        }
        Slog.w(TAG, "Request to remove task ignored for non-existent task " + taskId);
        return false;
    }

    public void removeStack(int stackId) {
        long ident;
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "removeStack()");
        if (stackId == 0) {
            throw new IllegalArgumentException("Removing home stack is not allowed.");
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ident = Binder.clearCallingIdentity();
                ActivityStack stack = this.mStackSupervisor.getStack(stackId);
                if (stack == null) {
                    Binder.restoreCallingIdentity(ident);
                    resetPriorityAfterLockedSection();
                    return;
                }
                ArrayList<TaskRecord> tasks = stack.getAllTasks();
                for (int i = tasks.size() - 1; i >= 0; i--) {
                    removeTaskByIdLocked(((TaskRecord) tasks.get(i)).taskId, true, true);
                }
                Binder.restoreCallingIdentity(ident);
                resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean removeTask(int taskId) {
        long ident;
        boolean removeTaskByIdLocked;
        enforceCallingPermission("android.permission.REMOVE_TASKS", "removeTask()");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ident = Binder.clearCallingIdentity();
                removeTaskByIdLocked = removeTaskByIdLocked(taskId, true, true);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
        return removeTaskByIdLocked;
    }

    public void moveTaskToFront(int taskId, int flags, Bundle bOptions) {
        enforceCallingPermission("android.permission.REORDER_TASKS", "moveTaskToFront()");
        if (ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.d(TAG_STACK, "moveTaskToFront: moving taskId=" + taskId);
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                moveTaskToFrontLocked(taskId, flags, bOptions);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    void moveTaskToFrontLocked(int taskId, int flags, Bundle bOptions) {
        ActivityOptions options = ActivityOptions.fromBundle(bOptions);
        if (checkAppSwitchAllowedLocked(Binder.getCallingPid(), Binder.getCallingUid(), -1, -1, "Task to front")) {
            long origId = Binder.clearCallingIdentity();
            try {
                TaskRecord task = this.mStackSupervisor.anyTaskForIdLocked(taskId);
                if (task == null) {
                    Slog.d(TAG, "Could not find task for id: " + taskId);
                } else if (this.mStackSupervisor.isLockTaskModeViolation(task)) {
                    this.mStackSupervisor.showLockTaskToast();
                    Slog.e(TAG, "moveTaskToFront: Attempt to violate Lock Task Mode");
                    Binder.restoreCallingIdentity(origId);
                } else {
                    ActivityRecord prev = this.mStackSupervisor.topRunningActivityLocked();
                    if (prev != null && prev.isRecentsActivity()) {
                        task.setTaskToReturnTo(2);
                    }
                    HwSlog.d(TAG, "moveTaskToFront: moving taskId=" + taskId + ",pid=" + Binder.getCallingPid());
                    this.mStackSupervisor.findTaskToMoveToFrontLocked(task, flags, options, "moveTaskToFront", false);
                    Binder.restoreCallingIdentity(origId);
                    ActivityOptions.abort(options);
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        } else {
            ActivityOptions.abort(options);
        }
    }

    public boolean moveActivityTaskToBack(IBinder token, boolean nonRoot) {
        enforceNotIsolatedCaller("moveActivityTaskToBack");
        synchronized (this) {
            long origId;
            try {
                boostPriorityForLockedSection();
                origId = Binder.clearCallingIdentity();
                int taskId = ActivityRecord.getTaskForActivityLocked(token, !nonRoot);
                TaskRecord task = this.mStackSupervisor.anyTaskForIdLocked(taskId);
                if (task == null) {
                    Binder.restoreCallingIdentity(origId);
                    resetPriorityAfterLockedSection();
                    return false;
                } else if (this.mStackSupervisor.isLockedTask(task)) {
                    this.mStackSupervisor.showLockTaskToast();
                    Binder.restoreCallingIdentity(origId);
                    resetPriorityAfterLockedSection();
                    return false;
                } else {
                    boolean moveTaskToBackLocked = ActivityRecord.getStackLocked(token).moveTaskToBackLocked(taskId);
                    Binder.restoreCallingIdentity(origId);
                    resetPriorityAfterLockedSection();
                    return moveTaskToBackLocked;
                }
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void moveTaskBackwards(int task) {
        enforceCallingPermission("android.permission.REORDER_TASKS", "moveTaskBackwards()");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if (checkAppSwitchAllowedLocked(Binder.getCallingPid(), Binder.getCallingUid(), -1, -1, "Task backwards")) {
                    long origId = Binder.clearCallingIdentity();
                    moveTaskBackwardsLocked(task);
                    Binder.restoreCallingIdentity(origId);
                    resetPriorityAfterLockedSection();
                    return;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private final void moveTaskBackwardsLocked(int task) {
        Slog.e(TAG, "moveTaskBackwards not yet implemented!");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public IActivityContainer createVirtualActivityContainer(IBinder parentActivityToken, IActivityContainerCallback callback) throws RemoteException {
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "createActivityContainer()");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if (parentActivityToken == null) {
                    throw new IllegalArgumentException("parent token must not be null");
                }
                ActivityRecord r = ActivityRecord.forTokenLocked(parentActivityToken);
                if (r == null) {
                } else if (callback == null) {
                    throw new IllegalArgumentException("callback must not be null");
                } else {
                    IActivityContainer createVirtualActivityContainer = this.mStackSupervisor.createVirtualActivityContainer(r, callback);
                    resetPriorityAfterLockedSection();
                    return createVirtualActivityContainer;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void deleteActivityContainer(IActivityContainer container) throws RemoteException {
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "deleteActivityContainer()");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mStackSupervisor.deleteActivityContainer(container);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public IActivityContainer createStackOnDisplay(int displayId) throws RemoteException {
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "createStackOnDisplay()");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityStack stack = this.mStackSupervisor.createStackOnDisplay(this.mStackSupervisor.getNextStackId(), displayId, true);
                if (stack == null) {
                } else {
                    IActivityContainer iActivityContainer = stack.mActivityContainer;
                    resetPriorityAfterLockedSection();
                    return iActivityContainer;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getActivityDisplayId(IBinder activityToken) throws RemoteException {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityStack stack = ActivityRecord.getStackLocked(activityToken);
                if (stack == null || !stack.mActivityContainer.isAttachedLocked()) {
                    resetPriorityAfterLockedSection();
                    return 0;
                }
                int displayId = stack.mActivityContainer.getDisplayId();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getActivityStackId(IBinder token) throws RemoteException {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityStack stack = ActivityRecord.getStackLocked(token);
                if (stack != null) {
                    int i = stack.mStackId;
                    resetPriorityAfterLockedSection();
                    return i;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void exitFreeformMode(IBinder token) throws RemoteException {
        long ident;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ident = Binder.clearCallingIdentity();
                ActivityRecord r = ActivityRecord.forTokenLocked(token);
                if (r == null) {
                    throw new IllegalArgumentException("exitFreeformMode: No activity record matching token=" + token);
                }
                ActivityStack stack = ActivityRecord.getStackLocked(token);
                if (stack == null || stack.mStackId != 2) {
                    throw new IllegalStateException("exitFreeformMode: You can only go fullscreen from freeform.");
                }
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.d(TAG_STACK, "exitFreeformMode: " + r);
                }
                this.mStackSupervisor.moveTaskToStackLocked(r.task.taskId, 1, true, false, "exitFreeformMode", true);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void moveTaskToStack(int taskId, int stackId, boolean toTop) {
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "moveTaskToStack()");
        if (stackId == 0) {
            throw new IllegalArgumentException("moveTaskToStack: Attempt to move task " + taskId + " to home stack");
        }
        synchronized (this) {
            long ident;
            try {
                boostPriorityForLockedSection();
                ident = Binder.clearCallingIdentity();
                Flog.i(101, "moveTaskToStack: moving task=" + taskId + " to stackId=" + stackId + " toTop=" + toTop);
                if (stackId == 3) {
                    this.mWindowManager.setDockedStackCreateState(0, null);
                }
                if (this.mStackSupervisor.moveTaskToStackLocked(taskId, stackId, toTop, false, "moveTaskToStack", true) && stackId == 3) {
                    this.mStackSupervisor.moveHomeStackTaskToTop(2, "moveTaskToDockedStack");
                }
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void swapDockedAndFullscreenStack() throws RemoteException {
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "swapDockedAndFullscreenStack()");
        synchronized (this) {
            long ident;
            try {
                TaskRecord topTask;
                ArrayList allTasks;
                boostPriorityForLockedSection();
                ident = Binder.clearCallingIdentity();
                ActivityStack fullscreenStack = this.mStackSupervisor.getStack(1);
                if (fullscreenStack != null) {
                    topTask = fullscreenStack.topTask();
                } else {
                    topTask = null;
                }
                ActivityStack dockedStack = this.mStackSupervisor.getStack(3);
                if (dockedStack != null) {
                    allTasks = dockedStack.getAllTasks();
                } else {
                    allTasks = null;
                }
                if (!(topTask == null || allTasks == null)) {
                    if (allTasks.size() != 0) {
                        this.mWindowManager.prepareAppTransition(18, false);
                        this.mStackSupervisor.moveTaskToStackLocked(topTask.taskId, 3, false, false, "swapDockedAndFullscreenStack", true, true);
                        int size = allTasks.size();
                        for (int i = 0; i < size; i++) {
                            int id = ((TaskRecord) allTasks.get(i)).taskId;
                            if (id != topTask.taskId) {
                                this.mStackSupervisor.moveTaskToStackLocked(id, 1, true, false, "swapDockedAndFullscreenStack", true, true);
                            }
                        }
                        this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
                        this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                        this.mWindowManager.executeAppTransition();
                        Binder.restoreCallingIdentity(ident);
                        resetPriorityAfterLockedSection();
                        return;
                    }
                }
                Slog.w(TAG, "Unable to swap tasks, either docked or fullscreen stack is empty.");
                Binder.restoreCallingIdentity(ident);
                resetPriorityAfterLockedSection();
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean moveTaskToDockedStack(int taskId, int createMode, boolean toTop, boolean animate, Rect initialBounds, boolean moveHomeStackFront) {
        boolean moved;
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "moveTaskToDockedStack()");
        synchronized (this) {
            long ident;
            try {
                boostPriorityForLockedSection();
                ident = Binder.clearCallingIdentity();
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.d(TAG_STACK, "moveTaskToDockedStack: moving task=" + taskId + " to createMode=" + createMode + " toTop=" + toTop);
                }
                this.mWindowManager.setDockedStackCreateState(createMode, initialBounds);
                moved = this.mStackSupervisor.moveTaskToStackLocked(taskId, 3, toTop, false, "moveTaskToDockedStack", animate, true);
                if (moved) {
                    if (moveHomeStackFront) {
                        this.mStackSupervisor.moveHomeStackToFront("moveTaskToDockedStack");
                    }
                    this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
                }
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
        return moved;
    }

    public boolean moveTopActivityToPinnedStack(int stackId, Rect bounds) {
        boolean moveTopStackActivityToPinnedStackLocked;
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "moveTopActivityToPinnedStack()");
        synchronized (this) {
            long ident;
            try {
                boostPriorityForLockedSection();
                if (this.mSupportsPictureInPicture) {
                    ident = Binder.clearCallingIdentity();
                    moveTopStackActivityToPinnedStackLocked = this.mStackSupervisor.moveTopStackActivityToPinnedStackLocked(stackId, bounds);
                    Binder.restoreCallingIdentity(ident);
                } else {
                    throw new IllegalStateException("moveTopActivityToPinnedStack:Device doesn't support picture-in-pciture mode");
                }
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
        return moveTopStackActivityToPinnedStackLocked;
    }

    public void resizeStack(int stackId, Rect bounds, boolean allowResizeInDockedMode, boolean preserveWindows, boolean animate, int animationDuration) {
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "resizeStack()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                if (!animate) {
                    Flog.i(101, "resizeStack, stackId: " + stackId + ", bounds=" + bounds);
                    this.mStackSupervisor.resizeStackLocked(stackId, bounds, null, null, preserveWindows, allowResizeInDockedMode, false);
                } else if (stackId == 4) {
                    this.mWindowManager.animateResizePinnedStack(bounds, animationDuration);
                } else {
                    throw new IllegalArgumentException("Stack: " + stackId + " doesn't support animated resize.");
                }
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(ident);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void resizeDockedStack(Rect dockedBounds, Rect tempDockedTaskBounds, Rect tempDockedTaskInsetBounds, Rect tempOtherTaskBounds, Rect tempOtherTaskInsetBounds) {
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "resizeDockedStack()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                this.mStackSupervisor.resizeDockedStackLocked(dockedBounds, tempDockedTaskBounds, tempDockedTaskInsetBounds, tempOtherTaskBounds, tempOtherTaskInsetBounds, true);
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(ident);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void resizePinnedStack(Rect pinnedBounds, Rect tempPinnedTaskBounds) {
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "resizePinnedStack()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                this.mStackSupervisor.resizePinnedStackLocked(pinnedBounds, tempPinnedTaskBounds);
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(ident);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void positionTaskInStack(int taskId, int stackId, int position) {
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "positionTaskInStack()");
        if (stackId == 0) {
            throw new IllegalArgumentException("positionTaskInStack: Attempt to change the position of task " + taskId + " in/to home stack");
        }
        synchronized (this) {
            long ident;
            try {
                boostPriorityForLockedSection();
                ident = Binder.clearCallingIdentity();
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.d(TAG_STACK, "positionTaskInStack: positioning task=" + taskId + " in stackId=" + stackId + " at position=" + position);
                }
                this.mStackSupervisor.positionTaskInStackLocked(taskId, stackId, position);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
    }

    public List<StackInfo> getAllStackInfos() {
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "getAllStackInfos()");
        long ident = Binder.clearCallingIdentity();
        try {
            List allStackInfosLocked;
            synchronized (this) {
                boostPriorityForLockedSection();
                allStackInfosLocked = this.mStackSupervisor.getAllStackInfosLocked();
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(ident);
            return allStackInfosLocked;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public StackInfo getStackInfo(int stackId) {
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "getStackInfo()");
        long ident = Binder.clearCallingIdentity();
        try {
            StackInfo stackInfoLocked;
            synchronized (this) {
                boostPriorityForLockedSection();
                stackInfoLocked = this.mStackSupervisor.getStackInfoLocked(stackId);
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(ident);
            return stackInfoLocked;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean isInHomeStack(int taskId) {
        boolean z = false;
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "getStackInfo()");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                TaskRecord tr = this.mStackSupervisor.anyTaskForIdLocked(taskId, false, -1);
                if (!(tr == null || tr.stack == null)) {
                    z = tr.stack.isHomeStack();
                }
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(ident);
            return z;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public int getTaskForActivity(IBinder token, boolean onlyRoot) {
        int taskForActivityLocked;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                taskForActivityLocked = ActivityRecord.getTaskForActivityLocked(token, onlyRoot);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return taskForActivityLocked;
    }

    public void updateDeviceOwner(String packageName) {
        int callingUid = Binder.getCallingUid();
        if (callingUid == 0 || callingUid == 1000) {
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    this.mDeviceOwnerName = packageName;
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        throw new SecurityException("updateDeviceOwner called from non-system process");
    }

    public void updateLockTaskPackages(int userId, String[] packages) {
        int callingUid = Binder.getCallingUid();
        if (!(callingUid == 0 || callingUid == 1000)) {
            enforceCallingPermission("android.permission.UPDATE_LOCK_TASK_PACKAGES", "updateLockTaskPackages()");
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                    Slog.w(TAG_LOCKTASK, "Whitelisting " + userId + ":" + Arrays.toString(packages));
                }
                this.mLockTaskPackages.put(userId, packages);
                this.mStackSupervisor.onLockTaskPackagesUpdatedLocked();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    void startLockTaskModeLocked(TaskRecord task) {
        int i = 1;
        if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
            Slog.w(TAG_LOCKTASK, "startLockTaskModeLocked: " + task);
        }
        if (task.mLockTaskAuth != 0) {
            int callingUid = Binder.getCallingUid();
            boolean isSystemInitiated = callingUid == 1000;
            long ident = Binder.clearCallingIdentity();
            if (!isSystemInitiated) {
                try {
                    task.mLockTaskUid = callingUid;
                    if (task.mLockTaskAuth == 1) {
                        if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                            Slog.w(TAG_LOCKTASK, "Mode default, asking user");
                        }
                        StatusBarManagerInternal statusBarManager = (StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class);
                        if (statusBarManager != null) {
                            statusBarManager.showScreenPinningRequest(task.taskId);
                        }
                        Binder.restoreCallingIdentity(ident);
                        return;
                    }
                    ActivityStack stack = this.mStackSupervisor.getFocusedStack();
                    if (stack == null || task != stack.topTask()) {
                        throw new IllegalArgumentException("Invalid task, not in foreground");
                    }
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                }
            }
            if (ActivityManagerDebugConfig.DEBUG_LOCKTASK) {
                String str;
                String str2 = TAG_LOCKTASK;
                if (isSystemInitiated) {
                    str = "Locking pinned";
                } else {
                    str = "Locking fully";
                }
                Slog.w(str2, str);
            }
            ActivityStackSupervisor activityStackSupervisor = this.mStackSupervisor;
            if (isSystemInitiated) {
                i = 2;
            }
            activityStackSupervisor.setLockTaskModeLocked(task, i, "startLockTask", true);
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void startLockTaskMode(int taskId) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                TaskRecord task = this.mStackSupervisor.anyTaskForIdLocked(taskId);
                if (task != null) {
                    startLockTaskModeLocked(task);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startLockTaskMode(IBinder token) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.forTokenLocked(token);
                if (r == null) {
                } else {
                    TaskRecord task = r.task;
                    if (task != null) {
                        startLockTaskModeLocked(task);
                    }
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void startSystemLockTaskMode(int taskId) throws RemoteException {
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "startSystemLockTaskMode");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                startLockTaskMode(taskId);
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(ident);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void stopLockTaskMode() {
        TaskRecord lockTask = this.mStackSupervisor.getLockedTaskLocked();
        if (lockTask != null) {
            int callingUid = Binder.getCallingUid();
            int lockTaskUid = lockTask.mLockTaskUid;
            if (this.mStackSupervisor.getLockTaskModeState() != 0) {
                if (checkCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS") == 0 || callingUid == lockTaskUid || (lockTaskUid == 0 && callingUid == lockTask.effectiveUid)) {
                    long ident = Binder.clearCallingIdentity();
                    try {
                        Log.d(TAG, "stopLockTaskMode");
                        synchronized (this) {
                            boostPriorityForLockedSection();
                            this.mStackSupervisor.setLockTaskModeLocked(null, 0, "stopLockTask", true);
                        }
                        resetPriorityAfterLockedSection();
                        TelecomManager tm = (TelecomManager) this.mContext.getSystemService("telecom");
                        if (tm != null) {
                            tm.showInCallScreen(false);
                        }
                        Binder.restoreCallingIdentity(ident);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(ident);
                    }
                } else {
                    throw new SecurityException("Invalid uid, expected " + lockTaskUid + " callingUid=" + callingUid + " effectiveUid=" + lockTask.effectiveUid);
                }
            }
        }
    }

    public void stopSystemLockTaskMode() throws RemoteException {
        if (this.mStackSupervisor.getLockTaskModeState() == 2) {
            stopLockTaskMode();
        } else {
            this.mStackSupervisor.showLockTaskToast();
        }
    }

    public boolean isInLockTaskMode() {
        return getLockTaskModeState() != 0;
    }

    public int getLockTaskModeState() {
        int lockTaskModeState;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                lockTaskModeState = this.mStackSupervisor.getLockTaskModeState();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return lockTaskModeState;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void showLockTaskEscapeMessage(IBinder token) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.forTokenLocked(token);
                if (r == null) {
                } else {
                    this.mStackSupervisor.showLockTaskEscapeMessageLocked(r.task);
                    resetPriorityAfterLockedSection();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private final List<ProviderInfo> generateApplicationProvidersLocked(ProcessRecord app) {
        boolean isClonedProcess = app.info.euid != 0;
        ProviderMap providerMap = isClonedProcess ? this.mProviderMapForClone : this.mProviderMap;
        List<ProviderInfo> providers = null;
        int flags = 268438528;
        if (isClonedProcess) {
            flags = 272632832;
            try {
                Flog.i(HdmiCecKeycode.CEC_KEYCODE_TUNE_FUNCTION, "generateApplicationProvidersLocked app = " + app);
            } catch (RemoteException e) {
            }
        }
        providers = AppGlobals.getPackageManager().queryContentProviders(app.processName, app.uid, flags).getList();
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.v(TAG_MU, "generateApplicationProvidersLocked, app.info.uid = " + app.uid);
        }
        int userId = app.userId;
        if (providers != null) {
            int N = providers.size();
            app.pubProviders.ensureCapacity(app.pubProviders.size() + N);
            int i = 0;
            while (i < N) {
                ProviderInfo cpi = (ProviderInfo) providers.get(i);
                boolean singleton = isSingleton(cpi.processName, cpi.applicationInfo, cpi.name, cpi.flags);
                if (!singleton || UserHandle.getUserId(app.uid) == 0) {
                    ComponentName comp = new ComponentName(cpi.packageName, cpi.name);
                    ContentProviderRecord cpr = providerMap.getProviderByClass(comp, userId);
                    if (cpr == null) {
                        cpr = new ContentProviderRecord(this, cpi, app.info, comp, singleton);
                        providerMap.putProviderByClass(comp, cpr);
                    }
                    if (ActivityManagerDebugConfig.DEBUG_MU) {
                        Slog.v(TAG_MU, "generateApplicationProvidersLocked, cpi.uid = " + cpr.uid);
                    }
                    app.pubProviders.put(cpi.name, cpr);
                    if (!(cpi.multiprocess && "android".equals(cpi.packageName))) {
                        app.addPackage(cpi.applicationInfo.packageName, cpi.applicationInfo.versionCode, this.mProcessStats);
                    }
                    notifyPackageUse(cpi.applicationInfo.packageName, 4);
                } else {
                    providers.remove(i);
                    N--;
                    i--;
                }
                i++;
            }
        }
        return providers;
    }

    public String checkContentProviderAccess(String authority, int userId) {
        if (userId == -1) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", TAG);
            userId = UserHandle.getCallingUserId();
        }
        ProviderInfo cpi = null;
        try {
            cpi = AppGlobals.getPackageManager().resolveContentProvider(authority, 789504, userId);
        } catch (RemoteException e) {
        }
        if (cpi == null) {
            return null;
        }
        synchronized (this.mPidsSelfLocked) {
            ProcessRecord r = (ProcessRecord) this.mPidsSelfLocked.get(Binder.getCallingPid());
        }
        if (r == null) {
            return "Failed to find PID " + Binder.getCallingPid();
        }
        String checkContentProviderPermissionLocked;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                checkContentProviderPermissionLocked = checkContentProviderPermissionLocked(cpi, r, userId, true);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return checkContentProviderPermissionLocked;
    }

    private final String checkContentProviderPermissionLocked(ProviderInfo cpi, ProcessRecord r, int userId, boolean checkUser) {
        int callingPid = r != null ? r.pid : Binder.getCallingPid();
        int callingUid = r != null ? r.uid : Binder.getCallingUid();
        boolean checkedGrants = false;
        if (checkUser) {
            int tmpTargetUserId = this.mUserController.unsafeConvertIncomingUserLocked(userId);
            if (tmpTargetUserId != UserHandle.getUserId(callingUid)) {
                if (checkAuthorityGrants(callingUid, cpi, tmpTargetUserId, checkUser)) {
                    return null;
                }
                checkedGrants = true;
            }
            userId = this.mUserController.handleIncomingUser(callingPid, callingUid, userId, false, 0, "checkContentProviderPermissionLocked " + cpi.authority, null);
            if (userId != tmpTargetUserId) {
                checkedGrants = false;
            }
        }
        if (checkComponentPermission(cpi.readPermission, callingPid, callingUid, cpi.applicationInfo.uid, cpi.exported) == 0) {
            return null;
        }
        if (checkComponentPermission(cpi.writePermission, callingPid, callingUid, cpi.applicationInfo.uid, cpi.exported) == 0) {
            return null;
        }
        PathPermission[] pps = cpi.pathPermissions;
        if (pps != null) {
            int i = pps.length;
            while (i > 0) {
                i--;
                PathPermission pp = pps[i];
                String pprperm = pp.getReadPermission();
                if (pprperm != null) {
                    if (checkComponentPermission(pprperm, callingPid, callingUid, cpi.applicationInfo.uid, cpi.exported) == 0) {
                        return null;
                    }
                }
                String ppwperm = pp.getWritePermission();
                if (ppwperm != null) {
                    if (checkComponentPermission(ppwperm, callingPid, callingUid, cpi.applicationInfo.uid, cpi.exported) == 0) {
                        return null;
                    }
                }
            }
        }
        if (!checkedGrants && checkAuthorityGrants(callingUid, cpi, userId, checkUser)) {
            return null;
        }
        String msg;
        StringBuilder append;
        if (cpi.exported) {
            append = new StringBuilder().append("Permission Denial: opening provider ").append(cpi.name).append(" from ");
            if (r == null) {
                r = "(null)";
            }
            msg = append.append(r).append(" (pid=").append(callingPid).append(", uid=").append(callingUid).append(") requires ").append(cpi.readPermission).append(" or ").append(cpi.writePermission).toString();
        } else {
            append = new StringBuilder().append("Permission Denial: opening provider ").append(cpi.name).append(" from ");
            if (r == null) {
                r = "(null)";
            }
            msg = append.append(r).append(" (pid=").append(callingPid).append(", uid=").append(callingUid).append(") that is not exported from uid ").append(cpi.applicationInfo.uid).toString();
        }
        Slog.w(TAG, msg);
        return msg;
    }

    boolean checkAuthorityGrants(int callingUid, ProviderInfo cpi, int userId, boolean checkUser) {
        ArrayMap<GrantUri, UriPermission> perms = (ArrayMap) this.mGrantedUriPermissions.get(callingUid);
        if (perms != null) {
            for (int i = perms.size() - 1; i >= 0; i--) {
                GrantUri grantUri = (GrantUri) perms.keyAt(i);
                if ((grantUri.sourceUserId == userId || !checkUser) && matchesProvider(grantUri.uri, cpi)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean matchesProvider(Uri uri, ProviderInfo cpi) {
        String uriAuth = uri.getAuthority();
        String cpiAuth = cpi.authority;
        if (cpiAuth.indexOf(CONTENT_PROVIDER_PUBLISH_TIMEOUT_MSG) == -1) {
            return cpiAuth.equals(uriAuth);
        }
        for (String equals : cpiAuth.split(";")) {
            if (equals.equals(uriAuth)) {
                return true;
            }
        }
        return false;
    }

    ContentProviderConnection incProviderCountLocked(ProcessRecord r, ContentProviderRecord cpr, IBinder externalProcessToken, boolean stable) {
        if (r != null) {
            ContentProviderConnection conn;
            for (int i = 0; i < r.conProviders.size(); i++) {
                conn = (ContentProviderConnection) r.conProviders.get(i);
                if (conn.provider == cpr) {
                    if (ActivityManagerDebugConfig.DEBUG_PROVIDER) {
                        Slog.v(TAG_PROVIDER, "Adding provider requested by " + r.processName + " from process " + cpr.info.processName + ": " + cpr.name.flattenToShortString() + " scnt=" + conn.stableCount + " uscnt=" + conn.unstableCount);
                    }
                    if (stable) {
                        conn.stableCount++;
                        conn.numStableIncs++;
                    } else {
                        conn.unstableCount++;
                        conn.numUnstableIncs++;
                    }
                    return conn;
                }
            }
            conn = new ContentProviderConnection(cpr, r);
            if (stable) {
                conn.stableCount = 1;
                conn.numStableIncs = 1;
            } else {
                conn.unstableCount = 1;
                conn.numUnstableIncs = 1;
            }
            cpr.connections.add(conn);
            r.conProviders.add(conn);
            if (!(conn.provider == null || conn.provider.proc == null || conn.provider.proc.uid < 10000 || r.pid == conn.provider.proc.pid || r.info == null || conn.provider.proc.info == null || r.info.packageName == null || r.info.packageName.equals(conn.provider.proc.info.packageName))) {
                LogPower.push(166, conn.provider.proc.processName, Integer.toString(r.pid), Integer.toString(conn.provider.proc.pid), new String[]{"provider"});
            }
            startAssociationLocked(r.uid, r.processName, r.curProcState, cpr.uid, cpr.name, cpr.info.processName);
            reportServiceRelationIAware(2, cpr, r);
            smartTrimAddProcessRelation_HwSysM(conn);
            return conn;
        }
        cpr.addExternalProcessHandleLocked(externalProcessToken);
        return null;
    }

    boolean decProviderCountLocked(ContentProviderConnection conn, ContentProviderRecord cpr, IBinder externalProcessToken, boolean stable) {
        if (conn != null) {
            cpr = conn.provider;
            if (ActivityManagerDebugConfig.DEBUG_PROVIDER) {
                Slog.v(TAG_PROVIDER, "Removing provider requested by " + conn.client.processName + " from process " + cpr.info.processName + ": " + cpr.name.flattenToShortString() + " scnt=" + conn.stableCount + " uscnt=" + conn.unstableCount);
            }
            if (stable) {
                conn.stableCount--;
            } else {
                conn.unstableCount--;
            }
            if (conn.stableCount != 0 || conn.unstableCount != 0) {
                return false;
            }
            cpr.connections.remove(conn);
            conn.client.conProviders.remove(conn);
            if (conn.client.setProcState < 13 && cpr.proc != null) {
                cpr.proc.lastProviderTime = SystemClock.uptimeMillis();
            }
            if (!(cpr.proc == null || cpr.proc.uid < 10000 || conn.client.pid == cpr.proc.pid || conn.client.info == null || cpr.proc.info == null || conn.client.info.packageName.equals(cpr.proc.info.packageName))) {
                LogPower.push(167, cpr.proc.processName, Integer.toString(conn.client.pid), Integer.toString(cpr.proc.pid), new String[]{"provider"});
            }
            stopAssociationLocked(conn.client.uid, conn.client.processName, cpr.uid, cpr.name);
            return true;
        }
        cpr.removeExternalProcessHandleLocked(externalProcessToken);
        return false;
    }

    private void checkTime(long startTime, String where) {
        long now = SystemClock.uptimeMillis();
        if (now - startTime > 50) {
            Slog.w(TAG, "Slow operation: " + (now - startTime) + "ms so far, now at " + where);
        }
    }

    boolean isProcessAliveLocked(ProcessRecord proc) {
        boolean z = false;
        if (proc.procStatFile == null) {
            proc.procStatFile = "/proc/" + proc.pid + "/stat";
        }
        this.mProcessStateStatsLongs[0] = 0;
        if (Process.readProcFile(proc.procStatFile, PROCESS_STATE_STATS_FORMAT, null, this.mProcessStateStatsLongs, null)) {
            long state = this.mProcessStateStatsLongs[0];
            if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                Slog.d(TAG, "RETRIEVED STATE FOR " + proc.procStatFile + ": " + ((char) ((int) state)));
            }
            if (!(state == 90 || state == 88 || state == 120 || state == 75)) {
                z = true;
            }
            return z;
        }
        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
            Slog.d(TAG, "UNABLE TO RETRIEVE STATE FOR " + proc.procStatFile);
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected ContentProviderHolder getContentProviderImpl(IApplicationThread caller, String name, IBinder token, boolean stable, int userId) {
        long ident;
        ContentProviderConnection contentProviderConnection = null;
        ProviderInfo providerInfo = null;
        synchronized (this) {
            ProviderMap providerMap;
            boolean isProviderForClone;
            ContentProviderRecord cpr;
            ContentProviderRecord cpr2;
            boolean providerRunning;
            String msg;
            boostPriorityForLockedSection();
            long startTime = SystemClock.uptimeMillis();
            ProcessRecord processRecord = null;
            if (caller != null) {
                processRecord = getRecordForAppLocked(caller);
                if (processRecord == null) {
                    throw new SecurityException("Unable to find app for caller " + caller + " (pid=" + Binder.getCallingPid() + ") when getting content provider " + name);
                }
            }
            try {
                providerMap = this.mProviderMap;
                isProviderForClone = false;
                if (!(processRecord == null || processRecord.info.euid == 0)) {
                    ProviderInfo pi = getProviderInfoLocked(name, userId, 786432);
                    if (pi != null) {
                        if (isPackageCloned(pi.packageName, userId)) {
                            Flog.i(HdmiCecKeycode.CEC_KEYCODE_TUNE_FUNCTION, "getContentProviderImpl providerInfo: " + pi + " for cloned process: " + processRecord);
                            providerMap = this.mProviderMapForClone;
                            isProviderForClone = true;
                        }
                    }
                }
                boolean checkCrossUser = true;
                checkTime(startTime, "getContentProviderImpl: getProviderByName");
                cpr = providerMap.getProviderByName(name, userId);
                if (cpr != null || userId == 0) {
                    cpr2 = cpr;
                } else {
                    cpr = providerMap.getProviderByName(name, 0);
                    if (cpr != null) {
                        providerInfo = cpr.info;
                        if (isSingleton(providerInfo.processName, providerInfo.applicationInfo, providerInfo.name, providerInfo.flags) && isValidSingletonCall(processRecord.uid, providerInfo.applicationInfo.uid)) {
                            userId = 0;
                            checkCrossUser = false;
                            cpr2 = cpr;
                        } else {
                            providerInfo = null;
                            cpr2 = null;
                        }
                    } else {
                        cpr2 = cpr;
                    }
                }
                providerRunning = (cpr2 == null || cpr2.proc == null || cpr2.proc.killed) ? false : true;
                if (isProviderForClone) {
                    Flog.i(HdmiCecKeycode.CEC_KEYCODE_TUNE_FUNCTION, "getContentProviderImpl name: " + name + ", providerRunning = " + providerRunning);
                }
                if (providerRunning) {
                    providerInfo = cpr2.info;
                    checkTime(startTime, "getContentProviderImpl: before checkContentProviderPermission");
                    if (providerInfo != null) {
                        msg = checkContentProviderPermissionLocked(providerInfo, processRecord, userId, checkCrossUser);
                        if (msg != null) {
                            throw new SecurityException(msg);
                        }
                    }
                    checkTime(startTime, "getContentProviderImpl: after checkContentProviderPermission");
                    if (!(providerInfo == null || processRecord == null)) {
                        if (shouldPreventStartProvider(providerInfo, processRecord.uid, processRecord.pid, processRecord.info.packageName, userId)) {
                            resetPriorityAfterLockedSection();
                            return null;
                        }
                    }
                    if (processRecord != null) {
                        if (cpr2.canRunHere(processRecord)) {
                            ContentProviderHolder holder = cpr2.newHolder(null);
                            holder.provider = null;
                            resetPriorityAfterLockedSection();
                            return holder;
                        }
                    }
                    long origId = Binder.clearCallingIdentity();
                    checkTime(startTime, "getContentProviderImpl: incProviderCountLocked");
                    contentProviderConnection = incProviderCountLocked(processRecord, cpr2, token, stable);
                    if (contentProviderConnection != null && contentProviderConnection.stableCount + contentProviderConnection.unstableCount == 1 && cpr2.proc != null && processRecord.setAdj <= FIRST_BROADCAST_QUEUE_MSG) {
                        checkTime(startTime, "getContentProviderImpl: before updateLruProcess");
                        updateLruProcessLocked(cpr2.proc, false, null);
                        checkTime(startTime, "getContentProviderImpl: after updateLruProcess");
                    }
                    checkTime(startTime, "getContentProviderImpl: before updateOomAdj");
                    int verifiedAdj = cpr2.proc.verifiedAdj;
                    boolean success = updateOomAdjLocked(cpr2.proc);
                    if (success && verifiedAdj != cpr2.proc.setAdj) {
                        if (!isProcessAliveLocked(cpr2.proc)) {
                            success = false;
                        }
                    }
                    maybeUpdateProviderUsageStatsLocked(processRecord, cpr2.info.packageName, name);
                    checkTime(startTime, "getContentProviderImpl: after updateOomAdj");
                    if (ActivityManagerDebugConfig.DEBUG_PROVIDER) {
                        Slog.i(TAG_PROVIDER, "Adjust success: " + success);
                    }
                    if (success) {
                        cpr2.proc.verifiedAdj = cpr2.proc.setAdj;
                    } else {
                        Slog.i(TAG, "Existing provider " + cpr2.name.flattenToShortString() + " is crashing; detaching " + processRecord);
                        boolean lastRef = decProviderCountLocked(contentProviderConnection, cpr2, token, stable);
                        checkTime(startTime, "getContentProviderImpl: before appDied");
                        appDiedLocked(cpr2.proc);
                        checkTime(startTime, "getContentProviderImpl: after appDied");
                        if (lastRef) {
                            providerRunning = false;
                            contentProviderConnection = null;
                        } else {
                            resetPriorityAfterLockedSection();
                            return null;
                        }
                    }
                    Binder.restoreCallingIdentity(origId);
                } else if (!(cpr2 == null || cpr2.proc == null || !cpr2.proc.killed)) {
                    Slog.e(TAG, "appDied, remove old cpr");
                    long callingId = Binder.clearCallingIdentity();
                    appDiedLocked(cpr2.proc);
                    Binder.restoreCallingIdentity(callingId);
                }
            } catch (RemoteException e) {
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable ex) {
                Slog.e(TAG, "appDied", ex);
                Binder.restoreCallingIdentity(callingId);
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
            if (providerRunning) {
                cpr = cpr2;
            } else {
                try {
                    checkTime(startTime, "getContentProviderImpl: before resolveContentProvider");
                    providerInfo = AppGlobals.getPackageManager().resolveContentProvider(name, (isProviderForClone ? 4194304 : 0) | 3072, userId);
                    checkTime(startTime, "getContentProviderImpl: after resolveContentProvider");
                } catch (RemoteException e2) {
                }
                if (providerInfo == null) {
                    resetPriorityAfterLockedSection();
                    return null;
                }
                boolean isValidSingletonCall;
                if (isSingleton(providerInfo.processName, providerInfo.applicationInfo, providerInfo.name, providerInfo.flags)) {
                    isValidSingletonCall = isValidSingletonCall(processRecord.uid, providerInfo.applicationInfo.uid);
                } else {
                    isValidSingletonCall = false;
                }
                if (isValidSingletonCall) {
                    userId = 0;
                }
                providerInfo.applicationInfo = getAppInfoForUser(providerInfo.applicationInfo, userId);
                checkTime(startTime, "getContentProviderImpl: got app info for user");
                checkTime(startTime, "getContentProviderImpl: before checkContentProviderPermission");
                msg = checkContentProviderPermissionLocked(providerInfo, processRecord, userId, !isValidSingletonCall);
                if (msg != null) {
                    throw new SecurityException(msg);
                }
                checkTime(startTime, "getContentProviderImpl: after checkContentProviderPermission");
                if (providerInfo != null) {
                    if (shouldPreventStartProvider(providerInfo, processRecord != null ? processRecord.pid : Binder.getCallingPid(), processRecord != null ? processRecord.uid : Binder.getCallingUid())) {
                        resetPriorityAfterLockedSection();
                        return null;
                    }
                }
                if (!(providerInfo == null || processRecord == null)) {
                    if (shouldPreventStartProvider(providerInfo, processRecord.uid, processRecord.pid, processRecord.info.packageName, userId)) {
                        resetPriorityAfterLockedSection();
                        return null;
                    }
                }
                if (!this.mProcessesReady && !providerInfo.processName.equals("system")) {
                    throw new IllegalArgumentException("Attempt to launch content provider before system ready");
                } else if (this.mUserController.isUserRunningLocked(userId, 0)) {
                    int N;
                    int i;
                    ProcessRecord proc;
                    ComponentName comp = new ComponentName(providerInfo.packageName, providerInfo.name);
                    checkTime(startTime, "getContentProviderImpl: before getProviderByClass");
                    cpr2 = providerMap.getProviderByClass(comp, userId);
                    checkTime(startTime, "getContentProviderImpl: after getProviderByClass");
                    boolean firstClass = cpr2 == null;
                    if (isProviderForClone) {
                        Flog.i(HdmiCecKeycode.CEC_KEYCODE_TUNE_FUNCTION, "getContentProviderImpl name: " + name + ", firstClass = " + firstClass);
                    }
                    if (firstClass) {
                        ident = Binder.clearCallingIdentity();
                        if (!Build.PERMISSIONS_REVIEW_REQUIRED || requestTargetProviderPermissionsReviewIfNeededLocked(providerInfo, processRecord, userId)) {
                            checkTime(startTime, "getContentProviderImpl: before getApplicationInfo");
                            ApplicationInfo ai = AppGlobals.getPackageManager().getApplicationInfo(providerInfo.applicationInfo.packageName, (isProviderForClone ? 4194304 : 0) | 1024, userId);
                            checkTime(startTime, "getContentProviderImpl: after getApplicationInfo");
                            if (ai == null) {
                                Slog.w(TAG, "No package info for content provider " + providerInfo.name);
                                Binder.restoreCallingIdentity(ident);
                                resetPriorityAfterLockedSection();
                                return null;
                            }
                            cpr = new ContentProviderRecord(this, providerInfo, getAppInfoForUser(ai, userId), comp, isValidSingletonCall);
                            Binder.restoreCallingIdentity(ident);
                            checkTime(startTime, "getContentProviderImpl: now have ContentProviderRecord");
                            if (processRecord == null && cpr.canRunHere(processRecord)) {
                                ContentProviderHolder newHolder = cpr.newHolder(null);
                                resetPriorityAfterLockedSection();
                                return newHolder;
                            }
                            if (ActivityManagerDebugConfig.DEBUG_PROVIDER) {
                                Slog.w(TAG_PROVIDER, "LAUNCHING REMOTE PROVIDER (myuid " + (processRecord == null ? Integer.valueOf(processRecord.uid) : null) + " pruid " + cpr.appInfo.uid + "): " + cpr.info.name + " callers=" + Debug.getCallers(6));
                            }
                            N = this.mLaunchingProviders.size();
                            i = 0;
                            while (i < N && this.mLaunchingProviders.get(i) != cpr) {
                                i++;
                            }
                            if (i >= N) {
                                origId = Binder.clearCallingIdentity();
                                try {
                                    checkTime(startTime, "getContentProviderImpl: before set stopped state");
                                    AppGlobals.getPackageManager().setPackageStoppedState(cpr.appInfo.packageName, false, userId);
                                    checkTime(startTime, "getContentProviderImpl: after set stopped state");
                                } catch (RemoteException e3) {
                                } catch (IllegalArgumentException e4) {
                                    Slog.w(TAG, "Failed trying to unstop package " + cpr.appInfo.packageName + ": " + e4);
                                } catch (Throwable th2) {
                                    Binder.restoreCallingIdentity(origId);
                                }
                                checkTime(startTime, "getContentProviderImpl: looking for process record");
                                proc = getProcessRecordLocked(providerInfo.processName, cpr.appInfo.uid + cpr.appInfo.euid, false);
                                if (isProviderForClone) {
                                    Flog.i(HdmiCecKeycode.CEC_KEYCODE_TUNE_FUNCTION, "getContentProviderImpl name: " + name + ", getProcessRecordLocked: " + proc);
                                }
                                if (proc != null || proc.thread == null || proc.killed) {
                                    checkTime(startTime, "getContentProviderImpl: before start process");
                                    proc = startProcessLocked(providerInfo.processName, cpr.appInfo, false, 0, "content provider", new ComponentName(providerInfo.applicationInfo.packageName, providerInfo.name), false, false, false);
                                    checkTime(startTime, "getContentProviderImpl: after start process");
                                    if (proc == null) {
                                        Slog.w(TAG, "Unable to launch app " + providerInfo.applicationInfo.packageName + "/" + providerInfo.applicationInfo.uid + " for provider " + name + ": process is bad");
                                        Binder.restoreCallingIdentity(origId);
                                        resetPriorityAfterLockedSection();
                                        return null;
                                    }
                                }
                                if (ActivityManagerDebugConfig.DEBUG_PROVIDER) {
                                    Slog.d(TAG_PROVIDER, "Installing in existing process " + proc);
                                }
                                if (!proc.pubProviders.containsKey(providerInfo.name)) {
                                    checkTime(startTime, "getContentProviderImpl: scheduling install");
                                    proc.pubProviders.put(providerInfo.name, cpr);
                                    try {
                                        proc.thread.scheduleInstallProvider(providerInfo);
                                    } catch (RemoteException e5) {
                                    }
                                }
                                cpr.launchingApp = proc;
                                this.mLaunchingProviders.add(cpr);
                                Binder.restoreCallingIdentity(origId);
                            }
                            checkTime(startTime, "getContentProviderImpl: updating data structures");
                            if (firstClass) {
                                providerMap.putProviderByClass(comp, cpr);
                            }
                            providerMap.putProviderByName(name, cpr);
                            contentProviderConnection = incProviderCountLocked(processRecord, cpr, token, stable);
                            if (contentProviderConnection != null) {
                                contentProviderConnection.waiting = true;
                            }
                        } else {
                            resetPriorityAfterLockedSection();
                            return null;
                        }
                    }
                    cpr = cpr2;
                    checkTime(startTime, "getContentProviderImpl: now have ContentProviderRecord");
                    if (processRecord == null) {
                    }
                    if (ActivityManagerDebugConfig.DEBUG_PROVIDER) {
                        if (processRecord == null) {
                        }
                        Slog.w(TAG_PROVIDER, "LAUNCHING REMOTE PROVIDER (myuid " + (processRecord == null ? Integer.valueOf(processRecord.uid) : null) + " pruid " + cpr.appInfo.uid + "): " + cpr.info.name + " callers=" + Debug.getCallers(6));
                    }
                    N = this.mLaunchingProviders.size();
                    i = 0;
                    while (i < N) {
                        i++;
                    }
                    if (i >= N) {
                        origId = Binder.clearCallingIdentity();
                        checkTime(startTime, "getContentProviderImpl: before set stopped state");
                        AppGlobals.getPackageManager().setPackageStoppedState(cpr.appInfo.packageName, false, userId);
                        checkTime(startTime, "getContentProviderImpl: after set stopped state");
                        checkTime(startTime, "getContentProviderImpl: looking for process record");
                        proc = getProcessRecordLocked(providerInfo.processName, cpr.appInfo.uid + cpr.appInfo.euid, false);
                        if (isProviderForClone) {
                            Flog.i(HdmiCecKeycode.CEC_KEYCODE_TUNE_FUNCTION, "getContentProviderImpl name: " + name + ", getProcessRecordLocked: " + proc);
                        }
                        if (proc != null) {
                        }
                        checkTime(startTime, "getContentProviderImpl: before start process");
                        proc = startProcessLocked(providerInfo.processName, cpr.appInfo, false, 0, "content provider", new ComponentName(providerInfo.applicationInfo.packageName, providerInfo.name), false, false, false);
                        checkTime(startTime, "getContentProviderImpl: after start process");
                        if (proc == null) {
                            Slog.w(TAG, "Unable to launch app " + providerInfo.applicationInfo.packageName + "/" + providerInfo.applicationInfo.uid + " for provider " + name + ": process is bad");
                            Binder.restoreCallingIdentity(origId);
                            resetPriorityAfterLockedSection();
                            return null;
                        }
                        cpr.launchingApp = proc;
                        this.mLaunchingProviders.add(cpr);
                        Binder.restoreCallingIdentity(origId);
                    }
                    checkTime(startTime, "getContentProviderImpl: updating data structures");
                    if (firstClass) {
                        providerMap.putProviderByClass(comp, cpr);
                    }
                    providerMap.putProviderByName(name, cpr);
                    contentProviderConnection = incProviderCountLocked(processRecord, cpr, token, stable);
                    if (contentProviderConnection != null) {
                        contentProviderConnection.waiting = true;
                    }
                } else {
                    Slog.w(TAG, "Unable to launch app " + providerInfo.applicationInfo.packageName + "/" + providerInfo.applicationInfo.uid + " for provider " + name + ": user " + userId + " is stopped");
                    resetPriorityAfterLockedSection();
                    return null;
                }
            }
            checkTime(startTime, "getContentProviderImpl: done!");
        }
        while (cpr.provider == null) {
            if (cpr.launchingApp == null) {
                Slog.w(TAG, "Unable to launch app " + providerInfo.applicationInfo.packageName + "/" + providerInfo.applicationInfo.uid + " for provider " + name + ": launching app became null");
                EventLog.writeEvent(EventLogTags.AM_PROVIDER_LOST_PROCESS, new Object[]{Integer.valueOf(UserHandle.getUserId(providerInfo.applicationInfo.uid)), providerInfo.applicationInfo.packageName, Integer.valueOf(providerInfo.applicationInfo.uid), name});
                return null;
            }
            try {
                Slog.v(TAG_MU, "Waiting to start provider " + cpr + " launchingApp=" + cpr.launchingApp + " caller pid= " + Binder.getCallingPid());
                if (contentProviderConnection != null) {
                    contentProviderConnection.waiting = true;
                }
                cpr.wait();
                if (contentProviderConnection != null) {
                    contentProviderConnection.waiting = false;
                }
                Slog.v(TAG, "Successfully start provider " + cpr + " launchingApp=" + cpr.launchingApp + " caller pid= " + Binder.getCallingPid());
            } catch (InterruptedException e6) {
                if (contentProviderConnection != null) {
                    contentProviderConnection.waiting = false;
                }
                Slog.v(TAG, "Successfully start provider " + cpr + " launchingApp=" + cpr.launchingApp + " caller pid= " + Binder.getCallingPid());
            } catch (Throwable th3) {
                if (contentProviderConnection != null) {
                    contentProviderConnection.waiting = false;
                }
                Slog.v(TAG, "Successfully start provider " + cpr + " launchingApp=" + cpr.launchingApp + " caller pid= " + Binder.getCallingPid());
            }
        }
        return cpr != null ? cpr.newHolder(contentProviderConnection) : null;
    }

    private boolean requestTargetProviderPermissionsReviewIfNeededLocked(ProviderInfo cpi, ProcessRecord r, int userId) {
        boolean callerForeground = true;
        if (!getPackageManagerInternalLocked().isPermissionsReviewRequired(cpi.packageName, userId)) {
            return true;
        }
        if (r != null && r.setSchedGroup == 0) {
            callerForeground = false;
        }
        if (callerForeground) {
            final Intent intent = new Intent("android.intent.action.REVIEW_PERMISSIONS");
            intent.addFlags(276824064);
            intent.putExtra("android.intent.extra.PACKAGE_NAME", cpi.packageName);
            if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
                Slog.i(TAG, "u" + userId + " Launching permission review " + "for package " + cpi.packageName);
            }
            final UserHandle userHandle = new UserHandle(userId);
            this.mHandler.post(new Runnable() {
                public void run() {
                    ActivityManagerService.this.mContext.startActivityAsUser(intent, userHandle);
                }
            });
            return false;
        }
        Slog.w(TAG, "u" + userId + " Instantiating a provider in package" + cpi.packageName + " requires a permissions review");
        return false;
    }

    PackageManagerInternal getPackageManagerInternalLocked() {
        if (this.mPackageManagerInt == null) {
            this.mPackageManagerInt = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        }
        return this.mPackageManagerInt;
    }

    public ContentProviderHolder getContentProvider(IApplicationThread caller, String name, int userId, boolean stable) {
        enforceNotIsolatedCaller("getContentProvider");
        if (caller != null) {
            return getContentProviderImpl(caller, name, null, stable, userId);
        }
        String msg = "null IApplicationThread when getting content provider " + name;
        Slog.w(TAG, msg);
        throw new SecurityException(msg);
    }

    public ContentProviderHolder getContentProviderExternal(String name, int userId, IBinder token) {
        enforceCallingPermission("android.permission.ACCESS_CONTENT_PROVIDERS_EXTERNALLY", "Do not have permission in call getContentProviderExternal()");
        return getContentProviderExternalUnchecked(name, token, this.mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, 2, "getContentProvider", null));
    }

    private ContentProviderHolder getContentProviderExternalUnchecked(String name, IBinder token, int userId) {
        return getContentProviderImpl(null, name, token, true, userId);
    }

    public void removeContentProvider(IBinder connection, boolean stable) {
        enforceNotIsolatedCaller("removeContentProvider");
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                ContentProviderConnection conn = (ContentProviderConnection) connection;
                if (conn == null) {
                    throw new NullPointerException("connection is null");
                }
                if (decProviderCountLocked(conn, null, null, stable)) {
                    updateOomAdjLocked();
                }
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(ident);
        } catch (ClassCastException e) {
            String msg = "removeContentProvider: " + connection + " not a ContentProviderConnection";
            Slog.w(TAG, msg);
            throw new IllegalArgumentException(msg);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void removeContentProviderExternal(String name, IBinder token) {
        enforceCallingPermission("android.permission.ACCESS_CONTENT_PROVIDERS_EXTERNALLY", "Do not have permission in call removeContentProviderExternal()");
        int userId = UserHandle.getCallingUserId();
        long ident = Binder.clearCallingIdentity();
        try {
            removeContentProviderExternalUnchecked(name, token, userId);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void removeContentProviderExternalUnchecked(String name, IBinder token, int userId) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ContentProviderRecord cpr = this.mProviderMap.getProviderByName(name, userId);
                if (cpr != null) {
                    ContentProviderRecord localCpr = this.mProviderMap.getProviderByClass(new ComponentName(cpr.info.packageName, cpr.info.name), userId);
                    if (!localCpr.hasExternalProcessHandles()) {
                        Slog.e(TAG, "Attmpt to remove content provider: " + localCpr + " with no external references.");
                    } else if (localCpr.removeExternalProcessHandleLocked(token)) {
                        updateOomAdjLocked();
                    } else {
                        Slog.e(TAG, "Attmpt to remove content provider " + localCpr + " with no external reference for token: " + token + ".");
                    }
                } else if (ActivityManagerDebugConfig.DEBUG_ALL) {
                    Slog.v(TAG, name + " content provider not found in providers list");
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public final void publishContentProviders(IApplicationThread caller, List<ContentProviderHolder> providers) {
        if (providers != null) {
            enforceNotIsolatedCaller("publishContentProviders");
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    ProcessRecord r = getRecordForAppLocked(caller);
                    if (ActivityManagerDebugConfig.DEBUG_MU) {
                        Slog.v(TAG_MU, "ProcessRecord uid = " + r.uid);
                    }
                    if (r == null) {
                        throw new SecurityException("Unable to find app for caller " + caller + " (pid=" + Binder.getCallingPid() + ") when publishing content providers");
                    }
                    long origId = Binder.clearCallingIdentity();
                    int N = providers.size();
                    for (int i = 0; i < N; i++) {
                        ContentProviderHolder src = (ContentProviderHolder) providers.get(i);
                        if (!(src == null || src.info == null || src.provider == null)) {
                            ContentProviderRecord dst = (ContentProviderRecord) r.pubProviders.get(src.info.name);
                            if (dst == null) {
                                continue;
                            } else {
                                if (ActivityManagerDebugConfig.DEBUG_MU) {
                                    Slog.v(TAG_MU, "ContentProviderRecord uid = " + dst.uid);
                                }
                                ComponentName comp = new ComponentName(dst.info.packageName, dst.info.name);
                                ProviderMap providerMap = this.mProviderMap;
                                if (!(dst.info.applicationInfo.euid == 0 && r.info.euid == 0) && isPackageCloned(dst.info.packageName, UserHandle.getUserId(dst.uid))) {
                                    providerMap = this.mProviderMapForClone;
                                    Flog.i(HdmiCecKeycode.CEC_KEYCODE_TUNE_FUNCTION, "publishContentProviders in cloned process providerInfo: " + dst.info + ", dst.info.applicationInfo.euid: " + dst.info.applicationInfo.euid + ", r.info.euid: " + r.info.euid);
                                }
                                providerMap.putProviderByClass(comp, dst);
                                String[] names = dst.info.authority.split(";");
                                for (String putProviderByName : names) {
                                    providerMap.putProviderByName(putProviderByName, dst);
                                }
                                int launchingCount = this.mLaunchingProviders.size();
                                boolean wasInLaunchingProviders = false;
                                int j = 0;
                                while (j < launchingCount) {
                                    if (this.mLaunchingProviders.get(j) == dst) {
                                        this.mLaunchingProviders.remove(j);
                                        wasInLaunchingProviders = true;
                                        j--;
                                        launchingCount--;
                                    }
                                    j++;
                                }
                                if (wasInLaunchingProviders) {
                                    this.mHandler.removeMessages(CONTENT_PROVIDER_PUBLISH_TIMEOUT_MSG, r);
                                }
                                synchronized (dst) {
                                    dst.provider = src.provider;
                                    dst.proc = r;
                                    dst.notifyAll();
                                }
                                updateOomAdjLocked(r);
                                maybeUpdateProviderUsageStatsLocked(r, src.info.packageName, src.info.authority);
                            }
                        }
                    }
                    Binder.restoreCallingIdentity(origId);
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                }
            }
            resetPriorityAfterLockedSection();
        }
    }

    public boolean refContentProvider(IBinder connection, int stable, int unstable) {
        boolean z = false;
        try {
            ContentProviderConnection conn = (ContentProviderConnection) connection;
            if (conn == null) {
                throw new NullPointerException("connection is null");
            }
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    if (stable > 0) {
                        conn.numStableIncs += stable;
                    }
                    stable += conn.stableCount;
                    if (stable < 0) {
                        throw new IllegalStateException("stableCount < 0: " + stable);
                    }
                    if (unstable > 0) {
                        conn.numUnstableIncs += unstable;
                    }
                    unstable += conn.unstableCount;
                    if (unstable < 0) {
                        throw new IllegalStateException("unstableCount < 0: " + unstable);
                    } else if (stable + unstable <= 0) {
                        throw new IllegalStateException("ref counts can't go to zero here: stable=" + stable + " unstable=" + unstable);
                    } else {
                        conn.stableCount = stable;
                        conn.unstableCount = unstable;
                        if (!conn.dead) {
                            z = true;
                        }
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return z;
        } catch (ClassCastException e) {
            String msg = "refContentProvider: " + connection + " not a ContentProviderConnection";
            Slog.w(TAG, msg);
            throw new IllegalArgumentException(msg);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void unstableProviderDied(IBinder connection) {
        try {
            ContentProviderConnection conn = (ContentProviderConnection) connection;
            if (conn == null) {
                throw new NullPointerException("connection is null");
            }
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    IContentProvider provider = conn.provider.provider;
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } catch (ClassCastException e) {
            String msg = "refContentProvider: " + connection + " not a ContentProviderConnection";
            Slog.w(TAG, msg);
            throw new IllegalArgumentException(msg);
        }
    }

    public void appNotRespondingViaProvider(IBinder connection) {
        enforceCallingPermission("android.permission.REMOVE_TASKS", "appNotRespondingViaProvider()");
        ContentProviderConnection conn = (ContentProviderConnection) connection;
        if (conn == null) {
            Slog.w(TAG, "ContentProviderConnection is null");
            return;
        }
        final ProcessRecord host = conn.provider.proc;
        if (host == null) {
            Slog.w(TAG, "Failed to find hosting ProcessRecord");
        } else {
            this.mHandler.post(new Runnable() {
                public void run() {
                    ActivityManagerService.this.mAppErrors.appNotResponding(host, null, null, false, "ContentProvider not responding");
                }
            });
        }
    }

    public final void installSystemProviders() {
        synchronized (this) {
            List<ProviderInfo> providers;
            try {
                boostPriorityForLockedSection();
                providers = generateApplicationProvidersLocked((ProcessRecord) this.mProcessNames.get("system", 1000));
                if (providers != null) {
                    for (int i = providers.size() - 1; i >= 0; i--) {
                        ProviderInfo pi = (ProviderInfo) providers.get(i);
                        if ((pi.applicationInfo.flags & 1) == 0) {
                            Slog.w(TAG, "Not installing system proc provider " + pi.name + ": not system .apk");
                            providers.remove(i);
                        }
                    }
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        if (providers != null) {
            this.mSystemThread.installSystemProviders(providers);
        }
        this.mCoreSettingsObserver = new CoreSettingsObserver(this);
        this.mFontScaleSettingObserver = new FontScaleSettingObserver();
    }

    private void startPersistentApps(int matchFlags) {
        if (this.mFactoryTest != 1) {
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    try {
                        for (ApplicationInfo app : AppGlobals.getPackageManager().getPersistentApplications(matchFlags | 1024).getList()) {
                            if (!"android".equals(app.packageName)) {
                                addAppLocked(app, false, null);
                            }
                        }
                    } catch (RemoteException e) {
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        }
    }

    private void installEncryptionUnawareProviders(int userId) {
        int matchFlags = 262152;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                int NP = this.mProcessNames.getMap().size();
                for (int ip = 0; ip < NP; ip++) {
                    SparseArray<ProcessRecord> apps = (SparseArray) this.mProcessNames.getMap().valueAt(ip);
                    int NA = apps.size();
                    for (int ia = 0; ia < NA; ia++) {
                        ProcessRecord app = (ProcessRecord) apps.valueAt(ia);
                        if (!(app.userId != userId || app.thread == null || app.unlocked)) {
                            int NG = app.pkgList.size();
                            for (int ig = 0; ig < NG; ig++) {
                                try {
                                    String pkgName = (String) app.pkgList.keyAt(ig);
                                    if (app.info.euid != 0 && isPackageCloned(pkgName, userId)) {
                                        matchFlags |= 4194304;
                                        Flog.i(HdmiCecKeycode.CEC_KEYCODE_TUNE_FUNCTION, "installEncryptionUnawareProviders for cloned process: " + app);
                                    }
                                    PackageInfo pkgInfo = AppGlobals.getPackageManager().getPackageInfo(pkgName, matchFlags, userId);
                                    if (!(pkgInfo == null || ArrayUtils.isEmpty(pkgInfo.providers))) {
                                        for (ProviderInfo pi : pkgInfo.providers) {
                                            boolean processMatch;
                                            if (Objects.equals(pi.processName, app.processName)) {
                                                processMatch = true;
                                            } else {
                                                processMatch = pi.multiprocess;
                                            }
                                            boolean userMatch = !isSingleton(pi.processName, pi.applicationInfo, pi.name, pi.flags) || app.userId == 0;
                                            if (processMatch && userMatch) {
                                                Log.v(TAG, "Installing " + pi);
                                                app.thread.scheduleInstallProvider(pi);
                                            } else {
                                                Log.v(TAG, "Skipping " + pi);
                                            }
                                        }
                                    }
                                } catch (RemoteException e) {
                                }
                            }
                        }
                    }
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public String getProviderMimeType(Uri uri, int userId) {
        enforceNotIsolatedCaller("getProviderMimeType");
        String name = uri.getAuthority();
        int callingUid = Binder.getCallingUid();
        int callingPid = Binder.getCallingPid();
        long ident = 0;
        boolean clearedIdentity = false;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                userId = this.mUserController.unsafeConvertIncomingUserLocked(userId);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        if (canClearIdentity(callingPid, callingUid, userId)) {
            clearedIdentity = true;
            ident = Binder.clearCallingIdentity();
        }
        try {
            ContentProviderHolder holder = getContentProviderExternalUnchecked(name, null, userId);
            if (holder != null) {
                String type = holder.provider.getType(uri);
                if (!clearedIdentity) {
                    ident = Binder.clearCallingIdentity();
                }
                if (holder != null) {
                    try {
                        removeContentProviderExternalUnchecked(name, null, userId);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(ident);
                    }
                }
                Binder.restoreCallingIdentity(ident);
                return type;
            }
            if (!clearedIdentity) {
                ident = Binder.clearCallingIdentity();
            }
            if (holder != null) {
                try {
                    removeContentProviderExternalUnchecked(name, null, userId);
                } catch (Throwable th2) {
                    Binder.restoreCallingIdentity(ident);
                }
            }
            Binder.restoreCallingIdentity(ident);
            return null;
        } catch (RemoteException e) {
            Log.w(TAG, "Content provider dead retrieving " + uri, e);
            if (!clearedIdentity) {
                ident = Binder.clearCallingIdentity();
            }
            if (null != null) {
                removeContentProviderExternalUnchecked(name, null, userId);
            }
            Binder.restoreCallingIdentity(ident);
            return null;
        } catch (Exception e2) {
            Log.w(TAG, "Exception while determining type of " + uri, e2);
            if (!clearedIdentity) {
                ident = Binder.clearCallingIdentity();
            }
            if (null != null) {
                removeContentProviderExternalUnchecked(name, null, userId);
            }
            Binder.restoreCallingIdentity(ident);
            return null;
        } catch (Throwable th3) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private boolean canClearIdentity(int callingPid, int callingUid, int userId) {
        return UserHandle.getUserId(callingUid) == userId || checkComponentPermission("android.permission.INTERACT_ACROSS_USERS", callingPid, callingUid, -1, true) == 0 || checkComponentPermission("android.permission.INTERACT_ACROSS_USERS_FULL", callingPid, callingUid, -1, true) == 0;
    }

    final ProcessRecord newProcessRecordLocked(ApplicationInfo info, String customProcess, boolean isolated, int isolatedUid) {
        String proc = customProcess != null ? customProcess : info.processName;
        BatteryStatsImpl stats = this.mBatteryStatsService.getActiveStatistics();
        int userId = UserHandle.getUserId(info.uid);
        int uid = info.uid;
        if (isolated) {
            if (isolatedUid == 0) {
                int stepsLeft = 1000;
                do {
                    if (this.mNextIsolatedProcessUid < 99000 || this.mNextIsolatedProcessUid > 99999) {
                        this.mNextIsolatedProcessUid = 99000;
                    }
                    uid = UserHandle.getUid(userId, this.mNextIsolatedProcessUid);
                    this.mNextIsolatedProcessUid++;
                    if (this.mIsolatedProcesses.indexOfKey(uid) >= 0) {
                        stepsLeft--;
                    }
                } while (stepsLeft > 0);
                return null;
            }
            uid = isolatedUid;
        }
        ProcessRecord r = new ProcessRecord(stats, info, proc, uid);
        Flog.i(100, "new Process app=" + r + ", name: " + r.processName + ", euid: " + info.euid);
        if (!this.mBooted && !this.mBooting && userId == 0 && (info.flags & 9) == 9) {
            r.persistent = true;
        }
        addProcessNameLocked(r);
        return r;
    }

    final ProcessRecord addAppLocked(ApplicationInfo info, boolean isolated, String abiOverride) {
        ProcessRecord app;
        if (isolated) {
            app = null;
        } else {
            app = getProcessRecordLocked(info.processName, info.uid + info.euid, true);
        }
        if (app == null) {
            app = newProcessRecordLocked(info, null, isolated, 0);
            updateLruProcessLocked(app, false, null);
            updateOomAdjLocked();
        }
        try {
            AppGlobals.getPackageManager().setPackageStoppedState(info.packageName, false, UserHandle.getUserId(app.uid));
        } catch (RemoteException e) {
        } catch (IllegalArgumentException e2) {
            Slog.w(TAG, "Failed trying to unstop package " + info.packageName + ": " + e2);
        }
        if ((info.flags & 9) == 9) {
            app.persistent = true;
            app.maxAdj = -800;
        }
        if (app.thread == null && this.mPersistentStartingProcesses.indexOf(app) < 0) {
            this.mPersistentStartingProcesses.add(app);
            startProcessLocked(app, "added application", app.processName, abiOverride, null, null);
        } else if (!(this.mPersistentReady || app.thread == null || !"com.android.phone".equals(app.processName))) {
            Slog.i(TAG, " phone process is running before persist ready");
            Intent intent = new Intent();
            intent.setPackage("com.android.phone");
            intent.setAction("com.android.phone.action.FAKE_BOOT_SERVICE");
            this.mContext.startService(intent);
        }
        return app;
    }

    public void unhandledBack() {
        enforceCallingPermission("android.permission.FORCE_BACK", "unhandledBack()");
        synchronized (this) {
            long origId;
            try {
                boostPriorityForLockedSection();
                origId = Binder.clearCallingIdentity();
                getFocusedStack().unhandledBackLocked();
                Binder.restoreCallingIdentity(origId);
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
    }

    public ParcelFileDescriptor openContentUri(Uri uri) throws RemoteException {
        enforceNotIsolatedCaller("openContentUri");
        int userId = UserHandle.getCallingUserId();
        String name = uri.getAuthority();
        ContentProviderHolder cph = getContentProviderExternalUnchecked(name, null, userId);
        ParcelFileDescriptor pfd = null;
        if (cph != null) {
            Binder token = new Binder();
            sCallerIdentity.set(new Identity(token, Binder.getCallingPid(), Binder.getCallingUid()));
            try {
                pfd = cph.provider.openFile(null, uri, "r", null, token);
            } catch (FileNotFoundException e) {
            } finally {
                sCallerIdentity.remove();
                removeContentProviderExternalUnchecked(name, null, userId);
            }
        } else {
            Slog.d(TAG, "Failed to get provider for authority '" + name + "'");
        }
        return pfd;
    }

    boolean isSleepingOrShuttingDownLocked() {
        return !isSleepingLocked() ? this.mShuttingDown : true;
    }

    boolean isShuttingDownLocked() {
        return this.mShuttingDown;
    }

    boolean isSleepingLocked() {
        return this.mSleeping;
    }

    void onWakefulnessChanged(int wakefulness) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mWakefulness = wakefulness;
                updateSleepIfNeededLocked();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    void finishRunningVoiceLocked() {
        if (this.mRunningVoice != null) {
            this.mRunningVoice = null;
            this.mVoiceWakeLock.release();
            updateSleepIfNeededLocked();
        }
    }

    void startTimeTrackingFocusedActivityLocked() {
        if (!this.mSleeping && this.mCurAppTimeTracker != null && this.mFocusedActivity != null) {
            this.mCurAppTimeTracker.start(this.mFocusedActivity.packageName);
        }
    }

    void updateSleepIfNeededLocked() {
        if (this.mSleeping && !shouldSleepLocked()) {
            this.mSleeping = false;
            startTimeTrackingFocusedActivityLocked();
            this.mTopProcessState = 2;
            this.mStackSupervisor.comeOutOfSleepIfNeededLocked();
            updateOomAdjLocked();
        } else if (!this.mSleeping && shouldSleepLocked()) {
            this.mSleeping = true;
            if (this.mCurAppTimeTracker != null) {
                this.mCurAppTimeTracker.stop();
            }
            this.mTopProcessState = 5;
            this.mStackSupervisor.goingToSleepLocked();
            updateOomAdjLocked();
            checkExcessivePowerUsageLocked(false);
            this.mHandler.removeMessages(27);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(27), (long) POWER_CHECK_DELAY);
        }
    }

    private boolean shouldSleepLocked() {
        boolean z = true;
        if (this.mRunningVoice != null) {
            return false;
        }
        switch (this.mWakefulness) {
            case 1:
            case 2:
            case 3:
                if (this.mLockScreenShown == 0 && this.mSleepTokens.isEmpty()) {
                    z = false;
                }
                return z;
            default:
                return true;
        }
    }

    void notifyTaskPersisterLocked(TaskRecord task, boolean flush) {
        this.mRecentTasks.notifyTaskPersisterLocked(task, flush);
    }

    void notifyTaskStackChangedLocked() {
        this.mHandler.sendEmptyMessage(LOG_STACK_STATE);
        this.mHandler.removeMessages(49);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(49), 100);
    }

    void notifyActivityPinnedLocked() {
        this.mHandler.removeMessages(64);
        this.mHandler.obtainMessage(64).sendToTarget();
    }

    void notifyPinnedActivityRestartAttemptLocked() {
        this.mHandler.removeMessages(65);
        this.mHandler.obtainMessage(65).sendToTarget();
    }

    public void notifyPinnedStackAnimationEnded() {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mHandler.removeMessages(66);
                this.mHandler.obtainMessage(66).sendToTarget();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void notifyCleartextNetwork(int uid, byte[] firstPacket) {
        this.mHandler.obtainMessage(50, uid, 0, firstPacket).sendToTarget();
    }

    public boolean shutdown(int timeout) {
        if (checkCallingPermission("android.permission.SHUTDOWN") != 0) {
            throw new SecurityException("Requires permission android.permission.SHUTDOWN");
        }
        boolean z = false;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mShuttingDown = true;
                updateEventDispatchingLocked();
                z = this.mStackSupervisor.shutdownLocked(timeout);
                this.mAppOpsService.shutdown();
                if (this.mUsageStatsService != null) {
                    this.mUsageStatsService.prepareShutdown();
                }
                this.mBatteryStatsService.shutdown();
                synchronized (this) {
                    try {
                        boostPriorityForLockedSection();
                        this.mProcessStats.shutdownLocked();
                        notifyTaskPersisterLocked(null, true);
                    } finally {
                        resetPriorityAfterLockedSection();
                    }
                }
                return z;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    public final void activitySlept(IBinder token) {
        if (ActivityManagerDebugConfig.DEBUG_ALL) {
            Slog.v(TAG, "Activity slept: token=" + token);
        }
        long origId = Binder.clearCallingIdentity();
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    this.mStackSupervisor.activitySleptLocked(r);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        Binder.restoreCallingIdentity(origId);
    }

    private String lockScreenShownToString() {
        switch (this.mLockScreenShown) {
            case 0:
                return "LOCK_SCREEN_HIDDEN";
            case 1:
                return "LOCK_SCREEN_LEAVING";
            case 2:
                return "LOCK_SCREEN_SHOWN";
            default:
                return "Unknown=" + this.mLockScreenShown;
        }
    }

    void logLockScreen(String msg) {
        Flog.i(HdmiCecKeycode.CEC_KEYCODE_POWER_TOGGLE_FUNCTION, Debug.getCallers(2) + ":" + msg + " mLockScreenShown=" + lockScreenShownToString() + " mWakefulness=" + PowerManagerInternal.wakefulnessToString(this.mWakefulness) + " mSleeping=" + this.mSleeping);
    }

    void startRunningVoiceLocked(IVoiceInteractionSession session, int targetUid) {
        Slog.d(TAG, "<<<  startRunningVoiceLocked()");
        this.mVoiceWakeLock.setWorkSource(new WorkSource(targetUid));
        if (this.mRunningVoice == null || this.mRunningVoice.asBinder() != session.asBinder()) {
            boolean wasRunningVoice = this.mRunningVoice != null;
            this.mRunningVoice = session;
            if (!wasRunningVoice) {
                this.mVoiceWakeLock.acquire();
                updateSleepIfNeededLocked();
            }
        }
    }

    private void updateEventDispatchingLocked() {
        boolean z = false;
        WindowManagerService windowManagerService = this.mWindowManager;
        if (this.mBooted && !this.mShuttingDown) {
            z = true;
        }
        windowManagerService.setEventDispatching(z);
    }

    public void setLockScreenShown(boolean showing, boolean occluded) {
        boolean z = false;
        if (checkCallingPermission("android.permission.DEVICE_POWER") != 0) {
            throw new SecurityException("Requires permission android.permission.DEVICE_POWER");
        }
        synchronized (this) {
            long ident;
            try {
                boostPriorityForLockedSection();
                ident = Binder.clearCallingIdentity();
                logLockScreen(" showing=" + showing + " occluded=" + occluded);
                int i = (!showing || occluded) ? 0 : 2;
                this.mLockScreenShown = i;
                if (showing && occluded) {
                    ActivityStackSupervisor activityStackSupervisor = this.mStackSupervisor;
                    if (this.mStackSupervisor.mFocusedStack.getStackId() == 3) {
                        z = true;
                    }
                    activityStackSupervisor.moveTasksToFullscreenStackLocked(3, z);
                }
                if (showing) {
                    exitSingleHandMode();
                }
                updateSleepIfNeededLocked();
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void notifyLockedProfile(int userId) {
        long ident;
        try {
            if (AppGlobals.getPackageManager().isUidPrivileged(Binder.getCallingUid())) {
                synchronized (this) {
                    try {
                        boostPriorityForLockedSection();
                        if (this.mStackSupervisor.isUserLockedProfile(userId)) {
                            ident = Binder.clearCallingIdentity();
                            int currentUserId = this.mUserController.getCurrentUserIdLocked();
                            this.mStackSupervisor.moveProfileTasksFromFreeformToFullscreenStackLocked(userId);
                            if (this.mUserController.isLockScreenDisabled(currentUserId)) {
                                this.mActivityStarter.showConfirmDeviceCredential(userId);
                            } else {
                                startHomeActivityLocked(currentUserId, "notifyLockedProfile");
                            }
                            Binder.restoreCallingIdentity(ident);
                        }
                    } catch (Throwable th) {
                        resetPriorityAfterLockedSection();
                    }
                }
                resetPriorityAfterLockedSection();
                return;
            }
            throw new SecurityException("Only privileged app can call notifyLockedProfile");
        } catch (RemoteException ex) {
            throw new SecurityException("Fail to check is caller a privileged app", ex);
        }
    }

    public void startConfirmDeviceCredentialIntent(Intent intent) {
        long ident;
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "startConfirmDeviceCredentialIntent");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ident = Binder.clearCallingIdentity();
                this.mActivityStarter.startConfirmCredentialIntent(intent);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void stopAppSwitches() {
        if (checkCallingPermission("android.permission.STOP_APP_SWITCHES") != 0) {
            throw new SecurityException("viewquires permission android.permission.STOP_APP_SWITCHES");
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mAppSwitchesAllowedTime = SystemClock.uptimeMillis() + 5000;
                this.mDidAppSwitch = false;
                this.mHandler.removeMessages(21);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(21), 5000);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void resumeAppSwitches() {
        if (checkCallingPermission("android.permission.STOP_APP_SWITCHES") != 0) {
            throw new SecurityException("Requires permission android.permission.STOP_APP_SWITCHES");
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mAppSwitchesAllowedTime = 0;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    boolean checkAppSwitchAllowedLocked(int sourcePid, int sourceUid, int callingPid, int callingUid, String name) {
        if (this.mAppSwitchesAllowedTime < SystemClock.uptimeMillis() || checkComponentPermission("android.permission.STOP_APP_SWITCHES", sourcePid, sourceUid, -1, true) == 0) {
            return true;
        }
        if (callingUid != -1 && callingUid != sourceUid && checkComponentPermission("android.permission.STOP_APP_SWITCHES", callingPid, callingUid, -1, true) == 0) {
            return true;
        }
        Slog.w(TAG, name + " request from " + sourceUid + " stopped");
        return false;
    }

    public void setDebugApp(String packageName, boolean waitForDebugger, boolean persistent) {
        enforceCallingPermission("android.permission.SET_DEBUG_APP", "setDebugApp()");
        long ident = Binder.clearCallingIdentity();
        if (persistent) {
            try {
                ContentResolver resolver = this.mContext.getContentResolver();
                Global.putString(resolver, "debug_app", packageName);
                Global.putInt(resolver, "wait_for_debugger", waitForDebugger ? 1 : 0);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
        synchronized (this) {
            boostPriorityForLockedSection();
            if (!persistent) {
                this.mOrigDebugApp = this.mDebugApp;
                this.mOrigWaitForDebugger = this.mWaitForDebugger;
            }
            this.mDebugApp = packageName;
            this.mWaitForDebugger = waitForDebugger;
            this.mDebugTransient = !persistent;
            if (packageName != null) {
                forceStopPackageLocked(packageName, -1, false, false, true, true, false, -1, "set debug app");
            }
        }
        resetPriorityAfterLockedSection();
        Binder.restoreCallingIdentity(ident);
    }

    void setTrackAllocationApp(ApplicationInfo app, String processName) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if ("1".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0")) || (app.flags & 2) != 0) {
                    this.mTrackAllocationApp = processName;
                } else {
                    throw new SecurityException("Process not debuggable: " + app.packageName);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    void setProfileApp(ApplicationInfo app, String processName, ProfilerInfo profilerInfo) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if ("1".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0")) || (app.flags & 2) != 0) {
                    this.mProfileApp = processName;
                    this.mProfileFile = profilerInfo.profileFile;
                    if (this.mProfileFd != null) {
                        try {
                            this.mProfileFd.close();
                        } catch (IOException e) {
                        }
                        this.mProfileFd = null;
                    }
                    this.mProfileFd = profilerInfo.profileFd;
                    this.mSamplingInterval = profilerInfo.samplingInterval;
                    this.mAutoStopProfiler = profilerInfo.autoStopProfiler;
                    this.mProfileType = 0;
                } else {
                    throw new SecurityException("Process not debuggable: " + app.packageName);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    void setNativeDebuggingAppLocked(ApplicationInfo app, String processName) {
        if ("1".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0")) || (app.flags & 2) != 0) {
            this.mNativeDebuggingApp = processName;
            return;
        }
        throw new SecurityException("Process not debuggable: " + app.packageName);
    }

    public void setAlwaysFinish(boolean enabled) {
        enforceCallingPermission("android.permission.SET_ALWAYS_FINISH", "setAlwaysFinish()");
        long ident = Binder.clearCallingIdentity();
        try {
            Global.putInt(this.mContext.getContentResolver(), "always_finish_activities", enabled ? 1 : 0);
            synchronized (this) {
                boostPriorityForLockedSection();
                this.mAlwaysFinishActivities = enabled;
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(ident);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void setLenientBackgroundCheck(boolean enabled) {
        enforceCallingPermission("android.permission.SET_PROCESS_LIMIT", "setLenientBackgroundCheck()");
        long ident = Binder.clearCallingIdentity();
        try {
            Global.putInt(this.mContext.getContentResolver(), "lenient_background_check", enabled ? 1 : 0);
            synchronized (this) {
                boostPriorityForLockedSection();
                this.mLenientBackgroundCheck = enabled;
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(ident);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void setActivityController(IActivityController controller, boolean imAMonkey) {
        enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "setActivityController()");
        Watchdog.getInstance().processStarted("ActivityController", controller == null ? 0 : Binder.getCallingPid());
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mController = controller;
                this.mControllerIsAMonkey = imAMonkey;
                Watchdog.getInstance().setActivityController(controller);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void setUserIsMonkey(boolean userIsMonkey) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                synchronized (this.mPidsSelfLocked) {
                    int callingPid = Binder.getCallingPid();
                    ProcessRecord precessRecord = (ProcessRecord) this.mPidsSelfLocked.get(callingPid);
                    if (precessRecord == null) {
                        throw new SecurityException("Unknown process: " + callingPid);
                    } else if (precessRecord.instrumentationUiAutomationConnection == null) {
                        throw new SecurityException("Only an instrumentation process with a UiAutomation can call setUserIsMonkey");
                    }
                }
                this.mUserIsMonkey = userIsMonkey;
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
    }

    public boolean isUserAMonkey() {
        boolean z;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                z = !this.mUserIsMonkey ? this.mController != null ? this.mControllerIsAMonkey : false : true;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    public void requestBugReport(int bugreportType) {
        String service = null;
        switch (bugreportType) {
            case 0:
                service = "bugreport";
                break;
            case 1:
                service = "bugreportplus";
                break;
            case 2:
                service = "bugreportremote";
                break;
        }
        if (service == null) {
            throw new IllegalArgumentException("Provided bugreport type is not correct, value: " + bugreportType);
        }
        enforceCallingPermission("android.permission.DUMP", "requestBugReport");
        SystemProperties.set("ctl.start", service);
    }

    public static long getInputDispatchingTimeoutLocked(ActivityRecord r) {
        return r != null ? getInputDispatchingTimeoutLocked(r.app) : (long) KEY_DISPATCHING_TIMEOUT;
    }

    public static long getInputDispatchingTimeoutLocked(ProcessRecord r) {
        if (r == null || (r.instrumentationClass == null && !r.usingWrapper)) {
            return (long) KEY_DISPATCHING_TIMEOUT;
        }
        return 60000;
    }

    public long inputDispatchingTimedOut(int pid, boolean aboveSystem, String reason) {
        if (checkCallingPermission("android.permission.FILTER_EVENTS") != 0) {
            throw new SecurityException("Requires permission android.permission.FILTER_EVENTS");
        }
        synchronized (this) {
            try {
                ProcessRecord proc;
                boostPriorityForLockedSection();
                synchronized (this.mPidsSelfLocked) {
                    proc = (ProcessRecord) this.mPidsSelfLocked.get(pid);
                }
                long timeout = getInputDispatchingTimeoutLocked(proc);
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
        if (inputDispatchingTimedOut(proc, null, null, aboveSystem, reason)) {
            return timeout;
        }
        return -1;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean inputDispatchingTimedOut(ProcessRecord proc, ActivityRecord activity, ActivityRecord parent, boolean aboveSystem, String reason) {
        if (checkCallingPermission("android.permission.FILTER_EVENTS") != 0) {
            throw new SecurityException("Requires permission android.permission.FILTER_EVENTS");
        }
        String annotation;
        if (reason == null) {
            annotation = "Input dispatching timed out";
        } else {
            annotation = "Input dispatching timed out (" + reason + ")";
        }
        if (proc != null) {
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    if (!proc.debugging) {
                        if (this.mDidDexOpt) {
                            this.mDidDexOpt = false;
                            resetPriorityAfterLockedSection();
                            return false;
                        } else if (proc.instrumentationClass != null) {
                            Bundle info = new Bundle();
                            info.putString("shortMsg", "keyDispatchingTimedOut");
                            info.putString("longMsg", annotation);
                            finishInstrumentationLocked(proc, 0, info);
                            resetPriorityAfterLockedSection();
                            return true;
                        }
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        }
        return true;
    }

    public Bundle getAssistContextExtras(int requestType) {
        PendingAssistExtras pae = enqueueAssistContext(requestType, null, null, null, null, null, true, true, UserHandle.getCallingUserId(), null, 500);
        if (pae == null) {
            return null;
        }
        synchronized (pae) {
            while (!pae.haveResult) {
                try {
                    pae.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                buildAssistBundleLocked(pae, pae.result);
                this.mPendingAssistExtras.remove(pae);
                this.mUiHandler.removeCallbacks(pae);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return pae.extras;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isAssistDataAllowedOnCurrentActivity() {
        boolean z = true;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                int userId = this.mUserController.getCurrentUserIdLocked();
                ActivityRecord activity = getFocusedStack().topActivity();
                if (activity == null) {
                } else {
                    userId = activity.userId;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean showAssistFromActivity(IBinder token, Bundle args) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                ActivityRecord caller = ActivityRecord.forTokenLocked(token);
                ActivityRecord top = getFocusedStack().topActivity();
                if (top != caller) {
                    Slog.w(TAG, "showAssistFromActivity failed: caller " + caller + " is not current top " + top);
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(ident);
                    return false;
                } else if (top.nowVisible) {
                    resetPriorityAfterLockedSection();
                    boolean showSessionForActiveService = new AssistUtils(this.mContext).showSessionForActiveService(args, 8, null, token);
                    Binder.restoreCallingIdentity(ident);
                    return showSessionForActiveService;
                } else {
                    Slog.w(TAG, "showAssistFromActivity failed: caller " + caller + " is not visible");
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(ident);
                    return false;
                }
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean requestAssistContextExtras(int requestType, IResultReceiver receiver, Bundle receiverExtras, IBinder activityToken, boolean focused, boolean newSessionId) {
        return enqueueAssistContext(requestType, null, null, receiver, receiverExtras, activityToken, focused, newSessionId, UserHandle.getCallingUserId(), null, 2000) != null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private PendingAssistExtras enqueueAssistContext(int requestType, Intent intent, String hint, IResultReceiver receiver, Bundle receiverExtras, IBinder activityToken, boolean focused, boolean newSessionId, int userHandle, Bundle args, long timeout) {
        PendingAssistExtras pendingAssistExtras;
        enforceCallingPermission("android.permission.GET_TOP_ACTIVITY_INFO", "enqueueAssistContext()");
        synchronized (this) {
            ActivityRecord activity;
            try {
                boostPriorityForLockedSection();
                activity = getFocusedStack().topActivity();
                if (activity == null) {
                    Slog.w(TAG, "getAssistContextExtras failed: no top activity");
                    pendingAssistExtras = null;
                } else if (activity.app == null || activity.app.thread == null) {
                    Slog.w(TAG, "getAssistContextExtras failed: no process for " + activity);
                    resetPriorityAfterLockedSection();
                    return null;
                } else {
                    if (!focused) {
                        activity = ActivityRecord.forTokenLocked(activityToken);
                        if (activity == null) {
                            Slog.w(TAG, "enqueueAssistContext failed: activity for token=" + activityToken + " couldn't be found");
                            resetPriorityAfterLockedSection();
                            return null;
                        }
                    } else if (activityToken != null) {
                        ActivityRecord caller = ActivityRecord.forTokenLocked(activityToken);
                        if (activity != caller) {
                            Slog.w(TAG, "enqueueAssistContext failed: caller " + caller + " is not current top " + activity);
                            resetPriorityAfterLockedSection();
                            return null;
                        }
                    }
                    Bundle extras = new Bundle();
                    if (args != null) {
                        extras.putAll(args);
                    }
                    extras.putString("android.intent.extra.ASSIST_PACKAGE", activity.packageName);
                    extras.putInt("android.intent.extra.ASSIST_UID", activity.app.uid);
                    PendingAssistExtras pae = new PendingAssistExtras(activity, extras, intent, hint, receiver, receiverExtras, userHandle);
                    if (newSessionId) {
                        this.mViSessionId++;
                    }
                    activity.app.thread.requestAssistContextExtras(activity.appToken, pae, requestType, this.mViSessionId);
                    this.mPendingAssistExtras.add(pae);
                    pendingAssistExtras = this.mUiHandler;
                    pendingAssistExtras.postDelayed(pae, timeout);
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "getAssistContextExtras failed: crash calling " + activity);
                pendingAssistExtras = null;
                return pendingAssistExtras;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    void pendingAssistExtrasTimedOut(PendingAssistExtras pae) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mPendingAssistExtras.remove(pae);
                IResultReceiver receiver = pae.receiver;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        if (receiver != null) {
            Bundle sendBundle = new Bundle();
            sendBundle.putBundle("receiverExtras", pae.receiverExtras);
            try {
                pae.receiver.send(0, sendBundle);
            } catch (RemoteException e) {
            }
        }
    }

    private void buildAssistBundleLocked(PendingAssistExtras pae, Bundle result) {
        if (result != null) {
            pae.extras.putBundle("android.intent.extra.ASSIST_CONTEXT", result);
        }
        if (pae.hint != null) {
            pae.extras.putBoolean(pae.hint, true);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void reportAssistContextExtras(IBinder token, Bundle extras, AssistStructure structure, AssistContent content, Uri referrer) {
        PendingAssistExtras pae = (PendingAssistExtras) token;
        synchronized (pae) {
            pae.result = extras;
            pae.structure = structure;
            pae.content = content;
            if (referrer != null) {
                pae.extras.putParcelable("android.intent.extra.REFERRER", referrer);
            }
            pae.haveResult = true;
            pae.notifyAll();
            if (pae.intent == null && pae.receiver == null) {
                return;
            }
        }
        Binder.restoreCallingIdentity(ident);
    }

    public boolean launchAssistIntent(Intent intent, int requestType, String hint, int userHandle, Bundle args) {
        return enqueueAssistContext(requestType, intent, hint, null, null, null, true, true, userHandle, args, 500) != null;
    }

    public void registerProcessObserver(IProcessObserver observer) {
        enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "registerProcessObserver()");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mProcessObservers.register(observer);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void unregisterProcessObserver(IProcessObserver observer) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mProcessObservers.unregister(observer);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void registerUidObserver(IUidObserver observer, int which) {
        enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "registerUidObserver()");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mUidObservers.register(observer, Integer.valueOf(which));
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void unregisterUidObserver(IUidObserver observer) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mUidObservers.unregister(observer);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean convertFromTranslucent(IBinder token) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                    return false;
                }
                boolean translucentChanged = r.changeWindowTranslucency(true);
                if (translucentChanged) {
                    r.task.stack.releaseBackgroundResources(r);
                    this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
                }
                this.mWindowManager.setAppFullscreen(token, true);
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(origId);
                return translucentChanged;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public boolean convertToTranslucent(IBinder token, ActivityOptions options) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                    return false;
                }
                int index = r.task.mActivities.lastIndexOf(r);
                if (index > 0) {
                    ((ActivityRecord) r.task.mActivities.get(index - 1)).returningOptions = options;
                }
                boolean translucentChanged = r.changeWindowTranslucency(false);
                if (translucentChanged) {
                    r.task.stack.convertActivityToTranslucent(r);
                }
                this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
                this.mWindowManager.setAppFullscreen(token, false);
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(origId);
                return translucentChanged;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public boolean requestVisibleBehind(IBinder token, boolean visible) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    boolean requestVisibleBehindLocked = this.mStackSupervisor.requestVisibleBehindLocked(r, visible);
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                    return requestVisibleBehindLocked;
                }
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(origId);
                return false;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public boolean isBackgroundVisibleBehind(IBinder token) {
        long origId = Binder.clearCallingIdentity();
        try {
            boolean hasVisibleBehindActivity;
            synchronized (this) {
                boostPriorityForLockedSection();
                ActivityStack stack = ActivityRecord.getStackLocked(token);
                hasVisibleBehindActivity = stack == null ? false : stack.hasVisibleBehindActivity();
                if (ActivityManagerDebugConfig.DEBUG_VISIBLE_BEHIND) {
                    Slog.d(TAG_VISIBLE_BEHIND, "isBackgroundVisibleBehind: stack=" + stack + " visible=" + hasVisibleBehindActivity);
                }
            }
            resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(origId);
            return hasVisibleBehindActivity;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public ActivityOptions getActivityOptions(IBinder token) {
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r != null) {
                    ActivityOptions activityOptions = r.pendingOptions;
                    r.pendingOptions = null;
                    resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                    return activityOptions;
                }
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(origId);
                return null;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void setImmersive(IBinder token, boolean immersive) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                    throw new IllegalArgumentException();
                }
                r.immersive = immersive;
                if (r == this.mFocusedActivity) {
                    if (ActivityManagerDebugConfig.DEBUG_IMMERSIVE) {
                        Slog.d(TAG_IMMERSIVE, "Frontmost changed immersion: " + r);
                    }
                    applyUpdateLockStateLocked(r);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean isImmersive(IBinder token) {
        boolean z;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                    throw new IllegalArgumentException();
                }
                z = r.immersive;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    public void setVrThread(int tid) {
        if (this.mContext.getPackageManager().hasSystemFeature("android.software.vr.mode")) {
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    synchronized (this.mPidsSelfLocked) {
                        int pid = Binder.getCallingPid();
                        ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(pid);
                        if (proc != null && this.mInVrMode && tid >= 0) {
                            if (Process.isThreadInProcess(pid, tid)) {
                                if (proc.vrThreadTid != 0) {
                                    Process.setThreadScheduler(proc.vrThreadTid, 0, 0);
                                }
                                proc.vrThreadTid = tid;
                                try {
                                    if (proc.curSchedGroup == 2 && proc.vrThreadTid > 0) {
                                        Process.setThreadScheduler(proc.vrThreadTid, 1073741825, 1);
                                    }
                                } catch (IllegalArgumentException e) {
                                    Slog.e(TAG, "Failed to set scheduling policy, thread does not exist:\n" + e);
                                }
                            } else {
                                throw new IllegalArgumentException("VR thread does not belong to process");
                            }
                        }
                    }
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                }
            }
            resetPriorityAfterLockedSection();
            return;
        }
        throw new UnsupportedOperationException("VR mode not supported on this device!");
    }

    public void setRenderThread(int tid) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                synchronized (this.mPidsSelfLocked) {
                    int pid = Binder.getCallingPid();
                    ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(pid);
                    if (proc == null || proc.renderThreadTid != 0 || tid <= 0) {
                        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                            Slog.d("UI_FIFO", "Didn't set thread from setRenderThread? PID: " + pid + ", TID: " + tid + " FIFO: " + this.mUseFifoUiScheduling);
                        }
                    } else if (Process.isThreadInProcess(pid, tid)) {
                        proc.renderThreadTid = tid;
                        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                            Slog.d("UI_FIFO", "Set RenderThread tid " + tid + " for pid " + pid);
                        }
                        if (proc.curSchedGroup == 2) {
                            if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                                Slog.d("UI_FIFO", "Promoting " + tid + "out of band");
                            }
                            if (this.mUseFifoUiScheduling) {
                                Process.setThreadScheduler(proc.renderThreadTid, 1073741825, 1);
                            } else {
                                Process.setThreadPriority(proc.renderThreadTid, -10);
                            }
                        }
                    } else {
                        throw new IllegalArgumentException("Render thread does not belong to process");
                    }
                }
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
    }

    public int setVrMode(IBinder token, boolean enabled, ComponentName packageName) {
        if (this.mContext.getPackageManager().hasSystemFeature("android.software.vr.mode")) {
            VrManagerInternal vrService = (VrManagerInternal) LocalServices.getService(VrManagerInternal.class);
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    ActivityRecord r = ActivityRecord.isInStackLocked(token);
                    if (r == null) {
                        throw new IllegalArgumentException();
                    }
                    int err = vrService.hasVrPackage(packageName, r.userId);
                    if (err != 0) {
                        return err;
                    }
                    synchronized (this) {
                        try {
                            boostPriorityForLockedSection();
                            if (!enabled) {
                                packageName = null;
                            }
                            r.requestedVrComponent = packageName;
                            if (r == this.mFocusedActivity) {
                                applyUpdateVrModeLocked(r);
                            }
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    return 0;
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return 0;
        }
        throw new UnsupportedOperationException("VR mode not supported on this device!");
    }

    public boolean isVrModePackageEnabled(ComponentName packageName) {
        if (!this.mContext.getPackageManager().hasSystemFeature("android.software.vr.mode")) {
            throw new UnsupportedOperationException("VR mode not supported on this device!");
        } else if (((VrManagerInternal) LocalServices.getService(VrManagerInternal.class)).hasVrPackage(packageName, UserHandle.getCallingUserId()) == 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isTopActivityImmersive() {
        boolean z;
        enforceNotIsolatedCaller("startActivity");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord r = getFocusedStack().topRunningActivityLocked();
                Slog.d(TAG, "isTopActivityImmersive r: " + r);
                z = r != null ? r.immersive : false;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    public boolean isTopOfTask(IBinder token) {
        boolean z;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.isInStackLocked(token);
                if (r == null) {
                    throw new IllegalArgumentException();
                }
                if (r.task.getTopActivity() == r) {
                    z = true;
                } else {
                    z = false;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    public final void enterSafeMode() {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if (!this.mSystemReady) {
                    try {
                        AppGlobals.getPackageManager().enterSafeMode();
                    } catch (RemoteException e) {
                    }
                }
                this.mSafeMode = true;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public final void showSafeModeOverlay() {
        View v = LayoutInflater.from(this.mContext).inflate(17367244, null);
        LayoutParams lp = new LayoutParams();
        lp.type = 2015;
        lp.width = -2;
        lp.height = -2;
        lp.gravity = 8388691;
        lp.format = v.getBackground().getOpacity();
        lp.flags = 24;
        lp.privateFlags |= 16;
        ((WindowManager) this.mContext.getSystemService("window")).addView(v, lp);
    }

    public void noteWakeupAlarm(IIntentSender sender, int sourceUid, String sourcePkg, String tag) {
        if (sender == null || (sender instanceof PendingIntentRecord)) {
            PendingIntentRecord rec = (PendingIntentRecord) sender;
            BatteryStatsImpl stats = this.mBatteryStatsService.getActiveStatistics();
            synchronized (stats) {
                if (this.mBatteryStatsService.isOnBattery()) {
                    this.mBatteryStatsService.enforceCallingPermission();
                    int uid = sender == null ? sourceUid : rec.uid == Binder.getCallingUid() ? 1000 : rec.uid;
                    if (sourceUid < 0) {
                        sourceUid = uid;
                    }
                    if (sourcePkg == null) {
                        sourcePkg = rec.key.packageName;
                    }
                    stats.getPackageStatsLocked(sourceUid, sourcePkg).noteWakeupAlarmLocked(tag);
                }
            }
        }
    }

    public void noteAlarmStart(IIntentSender sender, int sourceUid, String tag) {
        if (sender == null || (sender instanceof PendingIntentRecord)) {
            PendingIntentRecord rec = (PendingIntentRecord) sender;
            synchronized (this.mBatteryStatsService.getActiveStatistics()) {
                this.mBatteryStatsService.enforceCallingPermission();
                int uid = sender == null ? sourceUid : rec.uid == Binder.getCallingUid() ? 1000 : rec.uid;
                BatteryStatsService batteryStatsService = this.mBatteryStatsService;
                if (sourceUid < 0) {
                    sourceUid = uid;
                }
                batteryStatsService.noteAlarmStart(tag, sourceUid);
            }
        }
    }

    public void noteAlarmFinish(IIntentSender sender, int sourceUid, String tag) {
        if (sender == null || (sender instanceof PendingIntentRecord)) {
            PendingIntentRecord rec = (PendingIntentRecord) sender;
            synchronized (this.mBatteryStatsService.getActiveStatistics()) {
                this.mBatteryStatsService.enforceCallingPermission();
                int uid = sender == null ? sourceUid : rec.uid == Binder.getCallingUid() ? 1000 : rec.uid;
                BatteryStatsService batteryStatsService = this.mBatteryStatsService;
                if (sourceUid < 0) {
                    sourceUid = uid;
                }
                batteryStatsService.noteAlarmFinish(tag, sourceUid);
            }
        }
    }

    public boolean killPids(int[] pids, String pReason, boolean secure) {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("killPids only available to the system");
        }
        String reason = pReason == null ? "Unknown" : pReason;
        boolean killed = false;
        synchronized (this.mPidsSelfLocked) {
            int worstType = 0;
            for (int i : pids) {
                ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(i);
                if (proc != null) {
                    int type = proc.setAdj;
                    if (type > worstType) {
                        worstType = type;
                    }
                }
            }
            if (worstType < 906 && worstType > 900) {
                worstType = 900;
            }
            if (!secure && worstType < 500) {
                worstType = 500;
            }
            Slog.w(TAG, "Killing processes " + reason + " at adjustment " + worstType);
            for (int i2 : pids) {
                proc = (ProcessRecord) this.mPidsSelfLocked.get(i2);
                if (!(proc == null || proc.setAdj < worstType || proc.killedByAm)) {
                    proc.kill(reason, true);
                    killed = true;
                }
            }
        }
        return killed;
    }

    public void killUid(int appId, int userId, String reason) {
        enforceCallingPermission("android.permission.KILL_UID", "killUid");
        synchronized (this) {
            long identity;
            try {
                boostPriorityForLockedSection();
                identity = Binder.clearCallingIdentity();
                killPackageProcessesLocked(null, appId, userId, -800, false, true, true, true, reason != null ? reason : "kill uid");
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
    }

    public boolean killProcessesBelowForeground(String reason) {
        if (Binder.getCallingUid() == 1000) {
            return killProcessesBelowAdj(0, reason);
        }
        throw new SecurityException("killProcessesBelowForeground() only available to system");
    }

    private boolean killProcessesBelowAdj(int belowAdj, String reason) {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("killProcessesBelowAdj() only available to system");
        }
        boolean killed = false;
        synchronized (this.mPidsSelfLocked) {
            int size = this.mPidsSelfLocked.size();
            for (int i = 0; i < size; i++) {
                int pid = this.mPidsSelfLocked.keyAt(i);
                ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.valueAt(i);
                if (!(proc == null || proc.setAdj <= belowAdj || proc.killedByAm)) {
                    proc.kill(reason, true);
                    killed = true;
                }
            }
        }
        return killed;
    }

    public void hang(IBinder who, boolean allowRestart) {
        if (checkCallingPermission("android.permission.SET_ACTIVITY_WATCHER") != 0) {
            throw new SecurityException("Requires permission android.permission.SET_ACTIVITY_WATCHER");
        }
        DeathRecipient death = new DeathRecipient() {
            public void binderDied() {
                synchronized (this) {
                    notifyAll();
                }
            }
        };
        try {
            who.linkToDeath(death, 0);
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    Watchdog.getInstance().setAllowRestart(allowRestart);
                    Slog.i(TAG, "Hanging system process at request of pid " + Binder.getCallingPid());
                    synchronized (death) {
                        while (who.isBinderAlive()) {
                            try {
                                death.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                    Watchdog.getInstance().setAllowRestart(true);
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                }
            }
            resetPriorityAfterLockedSection();
        } catch (RemoteException e2) {
            Slog.w(TAG, "hang: given caller IBinder is already dead.");
        }
    }

    public void restart() {
        if (checkCallingPermission("android.permission.SET_ACTIVITY_WATCHER") != 0) {
            throw new SecurityException("Requires permission android.permission.SET_ACTIVITY_WATCHER");
        }
        Log.i(TAG, "Sending shutdown broadcast...");
        BroadcastReceiver br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.i(ActivityManagerService.TAG, "Shutting down activity manager...");
                ActivityManagerService.this.shutdown(10000);
                Log.i(ActivityManagerService.TAG, "Shutdown complete, restarting!");
                Process.killProcess(Process.myPid());
                System.exit(10);
            }
        };
        Intent intent = new Intent("android.intent.action.ACTION_SHUTDOWN");
        intent.addFlags(268435456);
        intent.putExtra("android.intent.extra.SHUTDOWN_USERSPACE_ONLY", true);
        br.onReceive(this.mContext, intent);
    }

    private long getLowRamTimeSinceIdle(long now) {
        long j = 0;
        long j2 = this.mLowRamTimeSinceLastIdle;
        if (this.mLowRamStartTime > 0) {
            j = now - this.mLowRamStartTime;
        }
        return j + j2;
    }

    public void performIdleMaintenance() {
        if (checkCallingPermission("android.permission.SET_ACTIVITY_WATCHER") != 0) {
            throw new SecurityException("Requires permission android.permission.SET_ACTIVITY_WATCHER");
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                long now = SystemClock.uptimeMillis();
                long timeSinceLastIdle = now - this.mLastIdleTime;
                long lowRamSinceLastIdle = getLowRamTimeSinceIdle(now);
                this.mLastIdleTime = now;
                this.mLowRamTimeSinceLastIdle = 0;
                if (this.mLowRamStartTime != 0) {
                    this.mLowRamStartTime = now;
                }
                StringBuilder sb = new StringBuilder(128);
                sb.append("Idle maintenance over ");
                TimeUtils.formatDuration(timeSinceLastIdle, sb);
                sb.append(" low RAM for ");
                TimeUtils.formatDuration(lowRamSinceLastIdle, sb);
                Slog.i(TAG, sb.toString());
                boolean doKilling = lowRamSinceLastIdle > timeSinceLastIdle / 3;
                for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
                    ProcessRecord proc = (ProcessRecord) this.mLruProcesses.get(i);
                    if (proc.notCachedSinceIdle) {
                        if (proc.setProcState != 5 && proc.setProcState >= 4 && proc.setProcState <= 10 && doKilling && proc.initialIdlePss != 0 && proc.lastPss > (proc.initialIdlePss * 3) / 2) {
                            sb = new StringBuilder(128);
                            sb.append("Kill");
                            sb.append(proc.processName);
                            sb.append(" in idle maint: pss=");
                            sb.append(proc.lastPss);
                            sb.append(", swapPss=");
                            sb.append(proc.lastSwapPss);
                            sb.append(", initialPss=");
                            sb.append(proc.initialIdlePss);
                            sb.append(", period=");
                            TimeUtils.formatDuration(timeSinceLastIdle, sb);
                            sb.append(", lowRamPeriod=");
                            TimeUtils.formatDuration(lowRamSinceLastIdle, sb);
                            Slog.wtfQuiet(TAG, sb.toString());
                            proc.kill("idle maint (pss " + proc.lastPss + " from " + proc.initialIdlePss + ")", true);
                        }
                    } else if (proc.setProcState < 12 && proc.setProcState > -1) {
                        proc.notCachedSinceIdle = true;
                        proc.initialIdlePss = 0;
                        proc.nextPssTime = ProcessList.computeNextPssTime(proc.setProcState, true, this.mTestPssMode, isSleepingLocked(), now);
                    }
                }
                this.mHandler.removeMessages(39);
                this.mHandler.sendEmptyMessageDelayed(39, JobStatus.DEFAULT_TRIGGER_MAX_DELAY);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void sendIdleJobTrigger() {
        if (checkCallingPermission("android.permission.SET_ACTIVITY_WATCHER") != 0) {
            throw new SecurityException("Requires permission android.permission.SET_ACTIVITY_WATCHER");
        }
        long ident = Binder.clearCallingIdentity();
        try {
            broadcastIntent(null, new Intent(ACTION_TRIGGER_IDLE).setPackage("android").addFlags(1073741824), null, null, 0, null, null, null, -1, null, true, false, -1);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    protected void retrieveSettings() {
        ContentResolver resolver = this.mContext.getContentResolver();
        boolean freeformWindowManagement = !this.mContext.getPackageManager().hasSystemFeature("android.software.freeform_window_management") ? Global.getInt(resolver, "enable_freeform_support", 0) != 0 : true;
        boolean supportsPictureInPicture = this.mContext.getPackageManager().hasSystemFeature("android.software.picture_in_picture");
        boolean supportsMultiWindow = ActivityManager.supportsMultiWindow();
        String debugApp = Global.getString(resolver, "debug_app");
        boolean waitForDebugger = Global.getInt(resolver, "wait_for_debugger", 0) != 0;
        boolean alwaysFinishActivities = Global.getInt(resolver, "always_finish_activities", 0) != 0;
        boolean lenientBackgroundCheck = Global.getInt(resolver, "lenient_background_check", 0) != 0;
        boolean forceRtl = Global.getInt(resolver, "debug.force_rtl", 0) != 0;
        boolean forceResizable = Global.getInt(resolver, "force_resizable_activities", 0) != 0;
        boolean supportsLeanbackOnly = this.mContext.getPackageManager().hasSystemFeature("android.software.leanback_only");
        SystemProperties.set("debug.force_rtl", forceRtl ? "1" : "0");
        Configuration configuration = new Configuration();
        System.getConfiguration(resolver, configuration);
        HwThemeManager.retrieveSimpleUIConfig(resolver, configuration, this.mUserController.getCurrentUserIdLocked());
        if (forceRtl) {
            configuration.setLayoutDirection(configuration.locale);
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mOrigDebugApp = debugApp;
                this.mDebugApp = debugApp;
                this.mOrigWaitForDebugger = waitForDebugger;
                this.mWaitForDebugger = waitForDebugger;
                this.mAlwaysFinishActivities = alwaysFinishActivities;
                this.mLenientBackgroundCheck = lenientBackgroundCheck;
                this.mSupportsLeanbackOnly = supportsLeanbackOnly;
                this.mForceResizableActivities = forceResizable;
                this.mWindowManager.setForceResizableTasks(this.mForceResizableActivities);
                if (supportsMultiWindow || forceResizable) {
                    this.mSupportsMultiWindow = true;
                    this.mSupportsFreeformWindowManagement = !freeformWindowManagement ? forceResizable : true;
                    if (supportsPictureInPicture) {
                        forceResizable = true;
                    }
                    this.mSupportsPictureInPicture = forceResizable;
                } else {
                    this.mSupportsMultiWindow = false;
                    this.mSupportsFreeformWindowManagement = false;
                    this.mSupportsPictureInPicture = false;
                }
                updateConfigurationLocked(configuration, null, true);
                if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                    Slog.v(TAG_CONFIGURATION, "Initial config: " + this.mConfiguration);
                }
                Resources res = this.mContext.getResources();
                this.mHasRecents = res.getBoolean(17957000);
                this.mThumbnailWidth = res.getDimensionPixelSize(17104898);
                this.mThumbnailHeight = res.getDimensionPixelSize(17104897);
                this.mDefaultPinnedStackBounds = Rect.unflattenFromString(res.getString(17039469));
                this.mAppErrors.loadAppsNotReportingCrashesFromConfigLocked(res.getString(17039472));
                if ((this.mConfiguration.uiMode & 4) == 4) {
                    this.mFullscreenThumbnailScale = ((float) res.getInteger(17694932)) / ((float) this.mConfiguration.screenWidthDp);
                } else {
                    this.mFullscreenThumbnailScale = res.getFraction(18022406, 1, 1);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        if (alwaysFinishActivities) {
            setAlwaysFinish(false);
        }
    }

    public boolean testIsSystemReady() {
        return this.mSystemReady;
    }

    void killAppAtUsersRequest(ProcessRecord app, Dialog fromDialog) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mAppErrors.killAppAtUserRequestLocked(app, fromDialog);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private ProcessErrorStateInfo generateProcessError(ProcessRecord app, int condition, String activity, String shortMsg, String longMsg, String stackTrace) {
        ProcessErrorStateInfo report = new ProcessErrorStateInfo();
        report.condition = condition;
        report.processName = app.processName;
        report.pid = app.pid;
        report.uid = app.info.uid;
        report.tag = activity;
        report.shortMsg = shortMsg;
        report.longMsg = longMsg;
        report.stackTrace = stackTrace;
        return report;
    }

    void skipCurrentReceiverLocked(ProcessRecord app) {
        for (BroadcastQueue queue : this.mBroadcastQueues) {
            queue.skipCurrentReceiverLocked(app);
        }
    }

    public void handleApplicationCrash(IBinder app, CrashInfo crashInfo) {
        ProcessRecord r = findAppProcess(app, "Crash");
        String systemServer = "system_server";
        String processName = app == null ? "system_server" : r == null ? Binder.getCallingPid() == MY_PID ? "system_server" : "mystery" : r.processName;
        handleApplicationCrashInner("crash", r, processName, crashInfo);
    }

    void handleApplicationCrashInner(String eventType, ProcessRecord r, String processName, CrashInfo crashInfo) {
        int i = -1;
        Object[] objArr;
        if ("com.android.phone".equals(processName)) {
            objArr = new Object[8];
            objArr[0] = Integer.valueOf(Binder.getCallingPid());
            objArr[1] = Integer.valueOf(UserHandle.getUserId(Binder.getCallingUid()));
            objArr[2] = processName;
            if (r != null) {
                i = r.info.flags;
            }
            objArr[3] = Integer.valueOf(i);
            objArr[4] = crashInfo.exceptionClassName;
            objArr[5] = "xxxxxxxxxxxxxx";
            objArr[6] = crashInfo.throwFileName;
            objArr[7] = Integer.valueOf(crashInfo.throwLineNumber);
            EventLog.writeEvent(EventLogTags.AM_CRASH, objArr);
        } else {
            objArr = new Object[8];
            objArr[0] = Integer.valueOf(Binder.getCallingPid());
            objArr[1] = Integer.valueOf(UserHandle.getUserId(Binder.getCallingUid()));
            objArr[2] = processName;
            if (r != null) {
                i = r.info.flags;
            }
            objArr[3] = Integer.valueOf(i);
            objArr[4] = crashInfo.exceptionClassName;
            objArr[5] = crashInfo.exceptionMessage;
            objArr[6] = crashInfo.throwFileName;
            objArr[7] = Integer.valueOf(crashInfo.throwLineNumber);
            EventLog.writeEvent(EventLogTags.AM_CRASH, objArr);
        }
        addErrorToDropBox(eventType, r, processName, null, null, null, null, null, crashInfo);
        this.mAppErrors.crashApplication(r, crashInfo);
    }

    public void handleApplicationStrictModeViolation(IBinder app, int violationMask, ViolationInfo info) {
        ProcessRecord r = findAppProcess(app, "StrictMode");
        if (r != null) {
            if ((2097152 & violationMask) != 0) {
                Integer stackFingerprint = Integer.valueOf(info.hashCode());
                boolean logIt = true;
                synchronized (this.mAlreadyLoggedViolatedStacks) {
                    if (this.mAlreadyLoggedViolatedStacks.contains(stackFingerprint)) {
                        logIt = false;
                    } else {
                        if (this.mAlreadyLoggedViolatedStacks.size() >= 5000) {
                            this.mAlreadyLoggedViolatedStacks.clear();
                        }
                        this.mAlreadyLoggedViolatedStacks.add(stackFingerprint);
                    }
                }
                if (logIt) {
                    logStrictModeViolationToDropBox(r, info);
                }
            }
            if ((DumpState.DUMP_INTENT_FILTER_VERIFIERS & violationMask) != 0) {
                AppErrorResult result = new AppErrorResult();
                synchronized (this) {
                    try {
                        boostPriorityForLockedSection();
                        long origId = Binder.clearCallingIdentity();
                        Message msg = Message.obtain();
                        msg.what = 26;
                        HashMap<String, Object> data = new HashMap();
                        data.put("result", result);
                        data.put("app", r);
                        data.put("violationMask", Integer.valueOf(violationMask));
                        data.put("info", info);
                        msg.obj = data;
                        this.mUiHandler.sendMessage(msg);
                        Binder.restoreCallingIdentity(origId);
                    } finally {
                        resetPriorityAfterLockedSection();
                    }
                }
                Slog.w(TAG, "handleApplicationStrictModeViolation; res=" + result.get());
            }
        }
    }

    private void logStrictModeViolationToDropBox(ProcessRecord process, ViolationInfo info) {
        if (info != null) {
            boolean isSystemApp = process != null ? (process.info.flags & 129) != 0 : true;
            String processName = process == null ? "unknown" : process.processName;
            final String dropboxTag = isSystemApp ? "system_app_strictmode" : "data_app_strictmode";
            final DropBoxManager dbox = (DropBoxManager) this.mContext.getSystemService("dropbox");
            if (dbox != null && dbox.isTagEnabled(dropboxTag)) {
                boolean bufferWasEmpty;
                final StringBuilder sb = isSystemApp ? this.mStrictModeBuffer : new StringBuilder(1024);
                synchronized (sb) {
                    bufferWasEmpty = sb.length() == 0;
                    appendDropBoxProcessHeaders(process, processName, sb);
                    sb.append("Build: ").append(Build.FINGERPRINT).append("\n");
                    sb.append("System-App: ").append(isSystemApp).append("\n");
                    sb.append("Uptime-Millis: ").append(info.violationUptimeMillis).append("\n");
                    if (info.violationNumThisLoop != 0) {
                        sb.append("Loop-Violation-Number: ").append(info.violationNumThisLoop).append("\n");
                    }
                    if (info.numAnimationsRunning != 0) {
                        sb.append("Animations-Running: ").append(info.numAnimationsRunning).append("\n");
                    }
                    if (info.broadcastIntentAction != null) {
                        sb.append("Broadcast-Intent-Action: ").append(info.broadcastIntentAction).append("\n");
                    }
                    if (info.durationMillis != -1) {
                        sb.append("Duration-Millis: ").append(info.durationMillis).append("\n");
                    }
                    if (info.numInstances != -1) {
                        sb.append("Instance-Count: ").append(info.numInstances).append("\n");
                    }
                    if (info.tags != null) {
                        for (String tag : info.tags) {
                            sb.append("Span-Tag: ").append(tag).append("\n");
                        }
                    }
                    sb.append("\n");
                    if (!(info.crashInfo == null || info.crashInfo.stackTrace == null)) {
                        sb.append(info.crashInfo.stackTrace);
                        sb.append("\n");
                    }
                    if (info.message != null) {
                        sb.append(info.message);
                        sb.append("\n");
                    }
                    boolean needsFlush = sb.length() > DumpState.DUMP_INSTALLS;
                }
                if (!isSystemApp || needsFlush) {
                    new Thread("Error dump: " + dropboxTag) {
                        public void run() {
                            synchronized (sb) {
                                String report = sb.toString();
                                sb.delete(0, sb.length());
                                sb.trimToSize();
                            }
                            if (report.length() != 0) {
                                dbox.addText(dropboxTag, report);
                            }
                        }
                    }.start();
                } else if (bufferWasEmpty) {
                    new Thread("Error dump: " + dropboxTag) {
                        public void run() {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                            }
                            synchronized (ActivityManagerService.this.mStrictModeBuffer) {
                                String errorReport = ActivityManagerService.this.mStrictModeBuffer.toString();
                                if (errorReport.length() == 0) {
                                    return;
                                }
                                ActivityManagerService.this.mStrictModeBuffer.delete(0, ActivityManagerService.this.mStrictModeBuffer.length());
                                ActivityManagerService.this.mStrictModeBuffer.trimToSize();
                                dbox.addText(dropboxTag, errorReport);
                            }
                        }
                    }.start();
                }
            }
        }
    }

    public boolean handleApplicationWtf(IBinder app, String tag, boolean system, CrashInfo crashInfo) {
        final int callingUid = Binder.getCallingUid();
        final int callingPid = Binder.getCallingPid();
        if (system) {
            final IBinder iBinder = app;
            final String str = tag;
            final CrashInfo crashInfo2 = crashInfo;
            this.mHandler.post(new Runnable() {
                public void run() {
                    ActivityManagerService.this.handleApplicationWtfInner(callingUid, callingPid, iBinder, str, crashInfo2);
                }
            });
            return false;
        }
        ProcessRecord r = handleApplicationWtfInner(callingUid, callingPid, app, tag, crashInfo);
        if (r == null || r.pid == Process.myPid() || Global.getInt(this.mContext.getContentResolver(), "wtf_is_fatal", 0) == 0) {
            return false;
        }
        this.mAppErrors.crashApplication(r, crashInfo);
        return true;
    }

    ProcessRecord handleApplicationWtfInner(int callingUid, int callingPid, IBinder app, String tag, CrashInfo crashInfo) {
        ProcessRecord r = findAppProcess(app, "WTF");
        String processName = app == null ? "system_server" : r == null ? "unknown" : r.processName;
        Object[] objArr = new Object[6];
        objArr[0] = Integer.valueOf(UserHandle.getUserId(callingUid));
        objArr[1] = Integer.valueOf(callingPid);
        objArr[2] = processName;
        objArr[3] = Integer.valueOf(r == null ? -1 : r.info.flags);
        objArr[4] = tag;
        objArr[5] = crashInfo.exceptionMessage;
        EventLog.writeEvent(EventLogTags.AM_WTF, objArr);
        addErrorToDropBox("wtf", r, processName, null, null, tag, null, null, crashInfo);
        return r;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ProcessRecord findAppProcess(IBinder app, String reason) {
        if (app == null) {
            return null;
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                int NP = this.mProcessNames.getMap().size();
                for (int ip = 0; ip < NP; ip++) {
                    SparseArray<ProcessRecord> apps = (SparseArray) this.mProcessNames.getMap().valueAt(ip);
                    int NA = apps.size();
                    int ia = 0;
                    while (ia < NA) {
                        ProcessRecord p = (ProcessRecord) apps.valueAt(ia);
                        if (p.thread == null || p.thread.asBinder() != app) {
                            ia++;
                        }
                    }
                }
                Slog.w(TAG, "Can't find mystery application for " + reason + " from pid=" + Binder.getCallingPid() + " uid=" + Binder.getCallingUid() + ": " + app);
                resetPriorityAfterLockedSection();
                return null;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private void appendDropBoxProcessHeaders(ProcessRecord process, String processName, StringBuilder sb) {
        if (process == null) {
            sb.append("Process: ").append(processName).append("\n");
            return;
        }
        synchronized (this) {
            boostPriorityForLockedSection();
            sb.append("Process: ").append(processName).append("\n");
            int flags = process.info.flags;
            IPackageManager pm = AppGlobals.getPackageManager();
            sb.append("Flags: 0x").append(Integer.toString(flags, 16)).append("\n");
            for (int ip = 0; ip < process.pkgList.size(); ip++) {
                String pkg = (String) process.pkgList.keyAt(ip);
                sb.append("Package: ").append(pkg);
                try {
                    PackageInfo pi = pm.getPackageInfo(pkg, 0, UserHandle.getCallingUserId());
                    if (pi != null) {
                        sb.append(" v").append(pi.versionCode);
                        if (pi.versionName != null) {
                            sb.append(" (").append(pi.versionName).append(")");
                        }
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error getting package info: " + pkg, e);
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                }
                sb.append("\n");
            }
        }
        resetPriorityAfterLockedSection();
    }

    private static String processClass(ProcessRecord process) {
        if (process == null || process.pid == MY_PID) {
            return "system_server";
        }
        if ((process.info.flags & 1) != 0) {
            return "system_app";
        }
        return "data_app";
    }

    public void addErrorToDropBox(String eventType, ProcessRecord process, String processName, ActivityRecord activity, ActivityRecord parent, String subject, String report, File dataFile, CrashInfo crashInfo) {
        String dropboxTag;
        if ("mystery".equals(processName)) {
            dropboxTag = "data_app_" + eventType;
            processName = "unknown";
        } else {
            dropboxTag = processClass(process) + "_" + eventType;
        }
        final DropBoxManager dbox = (DropBoxManager) this.mContext.getSystemService("dropbox");
        if (dbox != null && dbox.isTagEnabled(dropboxTag)) {
            long now = SystemClock.elapsedRealtime();
            if (now - this.mWtfClusterStart > JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY) {
                this.mWtfClusterStart = now;
                this.mWtfClusterCount = 1;
            } else {
                int i = this.mWtfClusterCount;
                this.mWtfClusterCount = i + 1;
                if (i >= 5) {
                    return;
                }
            }
            final StringBuilder sb = new StringBuilder(1024);
            appendDropBoxProcessHeaders(process, processName, sb);
            if (process != null) {
                sb.append("Foreground: ").append(process.isInterestingToUserLocked() ? "Yes" : "No").append("\n");
            }
            if (activity != null) {
                sb.append("Activity: ").append(activity.shortComponentName).append("\n");
            }
            if (!(parent == null || parent.app == null || parent.app.pid == process.pid)) {
                sb.append("Parent-Process: ").append(parent.app.processName).append("\n");
            }
            if (!(parent == null || parent == activity)) {
                sb.append("Parent-Activity: ").append(parent.shortComponentName).append("\n");
            }
            if (subject != null) {
                sb.append("Subject: ").append(subject).append("\n");
            }
            if (process != null) {
                sb.append("Lifetime: ").append(((SystemClock.elapsedRealtime() - process.startTime) / 1000) + "s").append("\n");
            }
            sb.append("Build: ").append(Build.FINGERPRINT).append("\n");
            if (Debug.isDebuggerConnected()) {
                sb.append("Debugger: Connected\n");
            }
            sb.append("\n");
            if ("1".equals(SystemProperties.get(SYSTEM_DEBUGGABLE)) || this.mController != null) {
                boolean needCpuInfo = "watchdog".equals(eventType);
                if (!(!"crash".equals(eventType) || crashInfo == null || crashInfo.exceptionClassName == null)) {
                    needCpuInfo = crashInfo.exceptionClassName.contains("TimeoutException");
                }
                if (needCpuInfo) {
                    synchronized (this.mProcessCpuThread) {
                        sb.append(this.mProcessCpuTracker.printCurrentLoad());
                        sb.append(this.mProcessCpuTracker.printCurrentState(SystemClock.uptimeMillis()));
                    }
                    sb.append("\n");
                }
            }
            final String str = report;
            final File file = dataFile;
            final CrashInfo crashInfo2 = crashInfo;
            Thread worker = new Thread("Error dump: " + dropboxTag) {
                public void run() {
                    Throwable th;
                    if (str != null) {
                        sb.append(str);
                    }
                    int lines = Global.getInt(ActivityManagerService.this.mContext.getContentResolver(), "logcat_for_" + dropboxTag, 0);
                    int maxDataFileSize = (ActivityManagerService.DROPBOX_MAX_SIZE - sb.length()) - (lines * 100);
                    if (file != null && maxDataFileSize > 0) {
                        try {
                            if (Log.HWINFO) {
                                sb.append(FileUtils.readTextFile(file, 0, null));
                            } else {
                                sb.append(FileUtils.readTextFile(file, maxDataFileSize, "\n\n[[TRUNCATED]]"));
                            }
                        } catch (IOException e) {
                            IOException e2;
                            Slog.e(ActivityManagerService.TAG, "Error reading " + file, e2);
                        }
                    }
                    if (!(crashInfo2 == null || crashInfo2.stackTrace == null)) {
                        sb.append(crashInfo2.stackTrace);
                    }
                    if (lines > 0) {
                        sb.append("\n");
                        InputStreamReader inputStreamReader = null;
                        try {
                            Process logcat = new ProcessBuilder(new String[]{"/system/bin/timeout", "-k", "15s", "10s", "/system/bin/logcat", "-v", "time", "-b", "events", "-b", "system", "-b", "main", "-b", "crash", "-t", String.valueOf(lines)}).redirectErrorStream(true).start();
                            try {
                                logcat.getOutputStream().close();
                            } catch (IOException e3) {
                            }
                            try {
                                logcat.getErrorStream().close();
                            } catch (IOException e4) {
                            }
                            InputStreamReader input = new InputStreamReader(logcat.getInputStream());
                            try {
                                char[] buf = new char[DumpState.DUMP_PREFERRED_XML];
                                while (true) {
                                    int num = input.read(buf);
                                    if (num <= 0) {
                                        break;
                                    }
                                    sb.append(buf, 0, num);
                                }
                                if (input != null) {
                                    try {
                                        input.close();
                                    } catch (IOException e5) {
                                    }
                                }
                            } catch (IOException e6) {
                                e2 = e6;
                                inputStreamReader = input;
                                try {
                                    Slog.e(ActivityManagerService.TAG, "Error running logcat", e2);
                                    if (inputStreamReader != null) {
                                        try {
                                            inputStreamReader.close();
                                        } catch (IOException e7) {
                                        }
                                    }
                                    dbox.addText(dropboxTag, sb.toString());
                                } catch (Throwable th2) {
                                    th = th2;
                                    if (inputStreamReader != null) {
                                        try {
                                            inputStreamReader.close();
                                        } catch (IOException e8) {
                                        }
                                    }
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                inputStreamReader = input;
                                if (inputStreamReader != null) {
                                    inputStreamReader.close();
                                }
                                throw th;
                            }
                        } catch (IOException e9) {
                            e2 = e9;
                            Slog.e(ActivityManagerService.TAG, "Error running logcat", e2);
                            if (inputStreamReader != null) {
                                inputStreamReader.close();
                            }
                            dbox.addText(dropboxTag, sb.toString());
                        }
                    }
                    dbox.addText(dropboxTag, sb.toString());
                }
            };
            if (process == null) {
                worker.run();
            } else {
                worker.start();
            }
        }
    }

    private void sendAppCrashRadar(ProcessRecord r, String reason) {
        if (r != null && r.instrumentationClass == null) {
            long crashTimeInterval = SystemClock.elapsedRealtime() - r.startTime;
            if (crashTimeInterval < 5000) {
                try {
                    PackageInfo pi = AppGlobals.getPackageManager().getPackageInfo(r.info.packageName, 0, UserHandle.getCallingUserId());
                    if (pi != null && (r.info.flags & 1) == 0) {
                        RadarHeader header = new RadarHeader(r.info.packageName, pi.versionName, 2802, 65);
                        StringBuilder append = new StringBuilder().append("versionCode: ").append(pi.versionCode).append("\n").append("Interval :").append(crashTimeInterval).append("\n");
                        if (reason == null) {
                            reason = "null";
                        }
                        FrameworkRadar.msg(header, append.append(reason).toString());
                    }
                } catch (Exception e) {
                    Slog.e(TAG, "Error getting radar info.", e);
                }
            }
        }
    }

    public List<ProcessErrorStateInfo> getProcessesInErrorState() {
        Throwable th;
        enforceNotIsolatedCaller("getProcessesInErrorState");
        boolean allUsers = ActivityManager.checkUidPermission("android.permission.INTERACT_ACROSS_USERS_FULL", Binder.getCallingUid()) == 0;
        int userId = UserHandle.getUserId(Binder.getCallingUid());
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                int i = this.mLruProcesses.size() - 1;
                List<ProcessErrorStateInfo> errList = null;
                while (i >= 0) {
                    List<ProcessErrorStateInfo> errList2;
                    ProcessRecord app = (ProcessRecord) this.mLruProcesses.get(i);
                    if (!allUsers && app.userId != userId) {
                        errList2 = errList;
                    } else if (app.thread == null) {
                        errList2 = errList;
                    } else if (app.crashing || app.notResponding) {
                        Object report = null;
                        if (app.crashing) {
                            report = app.crashingReport;
                        } else {
                            try {
                                if (app.notResponding) {
                                    report = app.notRespondingReport;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                errList2 = errList;
                            }
                        }
                        if (report != null) {
                            if (errList == null) {
                                errList2 = new ArrayList(1);
                            } else {
                                errList2 = errList;
                            }
                            errList2.add(report);
                        } else {
                            Slog.w(TAG, "Missing app error report, app = " + app.processName + " crashing = " + app.crashing + " notResponding = " + app.notResponding);
                            errList2 = errList;
                        }
                    } else {
                        errList2 = errList;
                    }
                    i--;
                    errList = errList2;
                }
                resetPriorityAfterLockedSection();
                return errList;
            } catch (Throwable th3) {
                th = th3;
            }
        }
        resetPriorityAfterLockedSection();
        throw th;
    }

    static int procStateToImportance(int procState, int memAdj, RunningAppProcessInfo currApp) {
        int imp = RunningAppProcessInfo.procStateToImportance(procState);
        if (imp == 400) {
            currApp.lru = memAdj;
        } else {
            currApp.lru = 0;
        }
        return imp;
    }

    private void fillInProcMemInfo(ProcessRecord app, RunningAppProcessInfo outInfo) {
        outInfo.pid = app.pid;
        outInfo.uid = app.info.uid;
        if (this.mHeavyWeightProcess == app) {
            outInfo.flags |= 1;
        }
        if (app.persistent) {
            outInfo.flags |= 2;
        }
        if (app.activities.size() > 0) {
            outInfo.flags |= 4;
        }
        outInfo.lastTrimLevel = app.trimMemoryLevel;
        outInfo.importance = procStateToImportance(app.curProcState, app.curAdj, outInfo);
        outInfo.importanceReasonCode = app.adjTypeCode;
        outInfo.processState = app.curProcState;
    }

    public List<RunningAppProcessInfo> getRunningAppProcesses() {
        Throwable th;
        enforceNotIsolatedCaller("getRunningAppProcesses");
        int callingUid = Binder.getCallingUid();
        boolean allUsers = ActivityManager.checkUidPermission("android.permission.INTERACT_ACROSS_USERS_FULL", callingUid) == 0;
        int userId = UserHandle.getUserId(callingUid);
        boolean allUids = isGetTasksAllowed("getRunningAppProcesses", Binder.getCallingPid(), callingUid);
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                int i = this.mLruProcesses.size() - 1;
                List<RunningAppProcessInfo> runList = null;
                while (i >= 0) {
                    List<RunningAppProcessInfo> runList2;
                    try {
                        ProcessRecord app = (ProcessRecord) this.mLruProcesses.get(i);
                        String EXCLUDE_PROCESS = "com.huawei.android.pushagent.PushService";
                        if ((!allUsers && app.userId != userId) || ((!allUids && app.uid != callingUid) || "com.huawei.android.pushagent.PushService".equals(app.processName))) {
                            runList2 = runList;
                        } else if (app.thread == null || app.crashing || app.notResponding) {
                            runList2 = runList;
                        } else {
                            RunningAppProcessInfo currApp = new RunningAppProcessInfo(app.processName, app.pid, app.getPackageList());
                            fillInProcMemInfo(app, currApp);
                            if (app.adjSource instanceof ProcessRecord) {
                                currApp.importanceReasonPid = ((ProcessRecord) app.adjSource).pid;
                                currApp.importanceReasonImportance = RunningAppProcessInfo.procStateToImportance(app.adjSourceProcState);
                            } else if (app.adjSource instanceof ActivityRecord) {
                                ActivityRecord r = app.adjSource;
                                if (r.app != null) {
                                    currApp.importanceReasonPid = r.app.pid;
                                }
                            }
                            if (app.adjTarget instanceof ComponentName) {
                                currApp.importanceReasonComponent = (ComponentName) app.adjTarget;
                            }
                            if (runList == null) {
                                runList2 = new ArrayList();
                            } else {
                                runList2 = runList;
                            }
                            runList2.add(currApp);
                        }
                        i--;
                        runList = runList2;
                    } catch (Throwable th2) {
                        th = th2;
                        runList2 = runList;
                    }
                }
                resetPriorityAfterLockedSection();
                return runList;
            } catch (Throwable th3) {
                th = th3;
            }
        }
        resetPriorityAfterLockedSection();
        throw th;
    }

    public List<ApplicationInfo> getRunningExternalApplications() {
        enforceNotIsolatedCaller("getRunningExternalApplications");
        List<RunningAppProcessInfo> runningApps = getRunningAppProcesses();
        List<ApplicationInfo> retList = new ArrayList();
        if (runningApps != null && runningApps.size() > 0) {
            Set<String> extList = new HashSet();
            for (RunningAppProcessInfo app : runningApps) {
                if (app.pkgList != null) {
                    for (String pkg : app.pkgList) {
                        extList.add(pkg);
                    }
                }
            }
            IPackageManager pm = AppGlobals.getPackageManager();
            for (String pkg2 : extList) {
                try {
                    ApplicationInfo info = pm.getApplicationInfo(pkg2, 0, UserHandle.getCallingUserId());
                    if (!(info == null || (info.flags & DumpState.DUMP_DOMAIN_PREFERRED) == 0)) {
                        retList.add(info);
                    }
                } catch (RemoteException e) {
                }
            }
        }
        return retList;
    }

    public void getMyMemoryState(RunningAppProcessInfo outInfo) {
        enforceNotIsolatedCaller("getMyMemoryState");
        synchronized (this) {
            try {
                ProcessRecord proc;
                boostPriorityForLockedSection();
                synchronized (this.mPidsSelfLocked) {
                    proc = (ProcessRecord) this.mPidsSelfLocked.get(Binder.getCallingPid());
                }
                fillInProcMemInfo(proc, outInfo);
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
    }

    public int getMemoryTrimLevel() {
        int i;
        enforceNotIsolatedCaller("getMyMemoryState");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                i = this.mLastMemoryLevel;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return i;
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) {
        new ActivityManagerShellCommand(this, false).exec(this, in, out, err, args, resultReceiver);
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (checkCallingPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump ActivityManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
            return;
        }
        Log.i(TAG, "Start dump, calling from : pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        if (!HwSlog.handleLogRequest(args)) {
            boolean dumpAll = false;
            boolean dumpClient = false;
            boolean dumpCheckin = false;
            boolean dumpCheckinFormat = false;
            String dumpPackage = null;
            int opti = 0;
            while (opti < args.length) {
                String opt = args[opti];
                if (opt == null || opt.length() <= 0 || opt.charAt(0) != '-') {
                    break;
                }
                opti++;
                if ("-a".equals(opt)) {
                    dumpAll = true;
                } else if ("-c".equals(opt)) {
                    dumpClient = true;
                } else if ("-p".equals(opt)) {
                    if (opti < args.length) {
                        dumpPackage = args[opti];
                        opti++;
                        dumpClient = true;
                    } else {
                        pw.println("Error: -p option requires package argument");
                        return;
                    }
                } else if ("--checkin".equals(opt)) {
                    dumpCheckinFormat = true;
                    dumpCheckin = true;
                } else if ("-C".equals(opt)) {
                    dumpCheckinFormat = true;
                } else if ("-h".equals(opt)) {
                    ActivityManagerShellCommand.dumpHelp(pw, true);
                    return;
                } else {
                    pw.println("Unknown argument: " + opt + "; use -h for help");
                }
            }
            long origId = Binder.clearCallingIdentity();
            boolean more = false;
            if (opti < args.length) {
                String cmd = args[opti];
                opti++;
                if ("activities".equals(cmd) || "a".equals(cmd)) {
                    synchronized (this) {
                        try {
                            boostPriorityForLockedSection();
                            dumpActivitiesLocked(fd, pw, args, opti, true, dumpClient, dumpPackage);
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    Binder.restoreCallingIdentity(origId);
                } else if ("recents".equals(cmd) || "r".equals(cmd)) {
                    synchronized (this) {
                        try {
                            boostPriorityForLockedSection();
                            dumpRecentsLocked(fd, pw, args, opti, true, dumpPackage);
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    Binder.restoreCallingIdentity(origId);
                } else if ("broadcasts".equals(cmd) || "b".equals(cmd)) {
                    if (opti >= args.length) {
                        newArgs = EMPTY_STRING_ARRAY;
                    } else {
                        dumpPackage = args[opti];
                        opti++;
                        newArgs = new String[(args.length - opti)];
                        if (args.length > 2) {
                            System.arraycopy(args, opti, newArgs, 0, args.length - opti);
                        }
                    }
                    synchronized (this) {
                        try {
                            boostPriorityForLockedSection();
                            dumpBroadcastsLocked(fd, pw, args, opti, true, dumpPackage);
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    Binder.restoreCallingIdentity(origId);
                } else if ("broadcast-stats".equals(cmd)) {
                    if (opti >= args.length) {
                        newArgs = EMPTY_STRING_ARRAY;
                    } else {
                        dumpPackage = args[opti];
                        opti++;
                        newArgs = new String[(args.length - opti)];
                        if (args.length > 2) {
                            System.arraycopy(args, opti, newArgs, 0, args.length - opti);
                        }
                    }
                    synchronized (this) {
                        try {
                            boostPriorityForLockedSection();
                            if (dumpCheckinFormat) {
                                dumpBroadcastStatsCheckinLocked(fd, pw, args, opti, dumpCheckin, dumpPackage);
                            } else {
                                dumpBroadcastStatsLocked(fd, pw, args, opti, true, dumpPackage);
                            }
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    Binder.restoreCallingIdentity(origId);
                } else if ("intents".equals(cmd) || "i".equals(cmd)) {
                    if (opti >= args.length) {
                        newArgs = EMPTY_STRING_ARRAY;
                    } else {
                        dumpPackage = args[opti];
                        opti++;
                        newArgs = new String[(args.length - opti)];
                        if (args.length > 2) {
                            System.arraycopy(args, opti, newArgs, 0, args.length - opti);
                        }
                    }
                    synchronized (this) {
                        try {
                            boostPriorityForLockedSection();
                            dumpPendingIntentsLocked(fd, pw, args, opti, true, dumpPackage);
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    Binder.restoreCallingIdentity(origId);
                } else if ("processes".equals(cmd) || "p".equals(cmd)) {
                    if (opti >= args.length) {
                        newArgs = EMPTY_STRING_ARRAY;
                    } else {
                        dumpPackage = args[opti];
                        opti++;
                        newArgs = new String[(args.length - opti)];
                        if (args.length > 2) {
                            System.arraycopy(args, opti, newArgs, 0, args.length - opti);
                        }
                    }
                    synchronized (this) {
                        try {
                            boostPriorityForLockedSection();
                            dumpProcessesLocked(fd, pw, args, opti, true, dumpPackage);
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    Binder.restoreCallingIdentity(origId);
                } else if ("oom".equals(cmd) || "o".equals(cmd)) {
                    synchronized (this) {
                        try {
                            boostPriorityForLockedSection();
                            dumpOomLocked(fd, pw, args, opti, true);
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    Binder.restoreCallingIdentity(origId);
                } else if ("permissions".equals(cmd) || "perm".equals(cmd)) {
                    synchronized (this) {
                        try {
                            boostPriorityForLockedSection();
                            dumpPermissionsLocked(fd, pw, args, opti, true, null);
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    Binder.restoreCallingIdentity(origId);
                } else if ("provider".equals(cmd)) {
                    if (opti >= args.length) {
                        name = null;
                        newArgs = EMPTY_STRING_ARRAY;
                    } else {
                        name = args[opti];
                        opti++;
                        newArgs = new String[(args.length - opti)];
                        if (args.length > 2) {
                            System.arraycopy(args, opti, newArgs, 0, args.length - opti);
                        }
                    }
                    if (!dumpProvider(fd, pw, name, newArgs, 0, dumpAll)) {
                        pw.println("No providers match: " + name);
                        pw.println("Use -h for help.");
                    }
                } else if ("providers".equals(cmd) || "prov".equals(cmd)) {
                    synchronized (this) {
                        try {
                            boostPriorityForLockedSection();
                            dumpProvidersLocked(fd, pw, args, opti, true, null);
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    Binder.restoreCallingIdentity(origId);
                } else if ("service".equals(cmd)) {
                    if (opti >= args.length) {
                        name = null;
                        newArgs = EMPTY_STRING_ARRAY;
                    } else {
                        name = args[opti];
                        opti++;
                        newArgs = new String[(args.length - opti)];
                        if (args.length > 2) {
                            System.arraycopy(args, opti, newArgs, 0, args.length - opti);
                        }
                    }
                    if (!this.mServices.dumpService(fd, pw, name, newArgs, 0, dumpAll)) {
                        pw.println("No services match: " + name);
                        pw.println("Use -h for help.");
                    }
                } else if (HwBroadcastRadarUtil.KEY_PACKAGE.equals(cmd)) {
                    if (opti >= args.length) {
                        pw.println("package: no package name specified");
                        pw.println("Use -h for help.");
                    } else {
                        dumpPackage = args[opti];
                        opti++;
                        newArgs = new String[(args.length - opti)];
                        if (args.length > 2) {
                            System.arraycopy(args, opti, newArgs, 0, args.length - opti);
                        }
                        args = newArgs;
                        opti = 0;
                        more = true;
                    }
                } else if ("associations".equals(cmd) || "as".equals(cmd)) {
                    synchronized (this) {
                        try {
                            boostPriorityForLockedSection();
                            dumpAssociationsLocked(fd, pw, args, opti, true, dumpClient, dumpPackage);
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    Binder.restoreCallingIdentity(origId);
                } else if ("services".equals(cmd) || "s".equals(cmd)) {
                    if (dumpClient) {
                        synchronized (this) {
                            try {
                                boostPriorityForLockedSection();
                                ServiceDumper dumper = this.mServices.newServiceDumperLocked(fd, pw, args, opti, true, dumpPackage);
                                dumper.dumpWithClient();
                            } finally {
                                resetPriorityAfterLockedSection();
                            }
                        }
                        Binder.restoreCallingIdentity(origId);
                    }
                    synchronized (this) {
                        try {
                            boostPriorityForLockedSection();
                            this.mServices.newServiceDumperLocked(fd, pw, args, opti, true, dumpPackage).dumpLocked();
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    Binder.restoreCallingIdentity(origId);
                } else if ("locks".equals(cmd)) {
                    LockGuard.dump(fd, pw, args);
                } else if (!dumpActivity(fd, pw, cmd, args, opti, dumpAll)) {
                    if (new ActivityManagerShellCommand(this, true).exec(this, null, fd, null, args, new ResultReceiver(null)) < 0) {
                        pw.println("Bad activity command, or no activities match: " + cmd);
                        pw.println("Use -h for help.");
                    }
                }
                if (!more) {
                    Binder.restoreCallingIdentity(origId);
                    return;
                }
            }
            if (dumpCheckinFormat) {
                dumpBroadcastStatsCheckinLocked(fd, pw, args, opti, dumpCheckin, dumpPackage);
            } else if (dumpClient) {
                synchronized (this) {
                    try {
                        boostPriorityForLockedSection();
                        dumpPendingIntentsLocked(fd, pw, args, opti, dumpAll, dumpPackage);
                        pw.println();
                        if (dumpAll) {
                            pw.println("-------------------------------------------------------------------------------");
                        }
                        dumpBroadcastsLocked(fd, pw, args, opti, dumpAll, dumpPackage);
                        pw.println();
                        if (dumpAll) {
                            pw.println("-------------------------------------------------------------------------------");
                        }
                        if (dumpAll || dumpPackage != null) {
                            dumpBroadcastStatsLocked(fd, pw, args, opti, dumpAll, dumpPackage);
                            pw.println();
                            if (dumpAll) {
                                pw.println("-------------------------------------------------------------------------------");
                            }
                        }
                        dumpProvidersLocked(fd, pw, args, opti, dumpAll, dumpPackage);
                        pw.println();
                        if (dumpAll) {
                            pw.println("-------------------------------------------------------------------------------");
                        }
                        dumpPermissionsLocked(fd, pw, args, opti, dumpAll, dumpPackage);
                        pw.println();
                        if (dumpAll) {
                            pw.println("-------------------------------------------------------------------------------");
                        }
                        ServiceDumper sdumper = this.mServices.newServiceDumperLocked(fd, pw, args, opti, dumpAll, dumpPackage);
                        sdumper.dumpWithClient();
                        pw.println();
                        synchronized (this) {
                            try {
                                boostPriorityForLockedSection();
                                if (dumpAll) {
                                    pw.println("-------------------------------------------------------------------------------");
                                }
                                dumpRecentsLocked(fd, pw, args, opti, dumpAll, dumpPackage);
                                pw.println();
                                if (dumpAll) {
                                    pw.println("-------------------------------------------------------------------------------");
                                }
                                dumpActivitiesLocked(fd, pw, args, opti, dumpAll, dumpClient, dumpPackage);
                                if (this.mAssociations.size() > 0) {
                                    pw.println();
                                    if (dumpAll) {
                                        pw.println("-------------------------------------------------------------------------------");
                                    }
                                    dumpAssociationsLocked(fd, pw, args, opti, dumpAll, dumpClient, dumpPackage);
                                }
                                pw.println();
                                if (dumpAll) {
                                    pw.println("-------------------------------------------------------------------------------");
                                }
                                dumpProcessesLocked(fd, pw, args, opti, dumpAll, dumpPackage);
                            } finally {
                                resetPriorityAfterLockedSection();
                            }
                        }
                    } finally {
                        resetPriorityAfterLockedSection();
                    }
                }
            } else {
                synchronized (this) {
                    try {
                        boostPriorityForLockedSection();
                        dumpPendingIntentsLocked(fd, pw, args, opti, dumpAll, dumpPackage);
                        pw.println();
                        if (dumpAll) {
                            pw.println("-------------------------------------------------------------------------------");
                        }
                        dumpBroadcastsLocked(fd, pw, args, opti, dumpAll, dumpPackage);
                        pw.println();
                        if (dumpAll) {
                            pw.println("-------------------------------------------------------------------------------");
                        }
                        if (dumpAll || dumpPackage != null) {
                            dumpBroadcastStatsLocked(fd, pw, args, opti, dumpAll, dumpPackage);
                            pw.println();
                            if (dumpAll) {
                                pw.println("-------------------------------------------------------------------------------");
                            }
                        }
                        dumpProvidersLocked(fd, pw, args, opti, dumpAll, dumpPackage);
                        pw.println();
                        if (dumpAll) {
                            pw.println("-------------------------------------------------------------------------------");
                        }
                        dumpPermissionsLocked(fd, pw, args, opti, dumpAll, dumpPackage);
                        pw.println();
                        if (dumpAll) {
                            pw.println("-------------------------------------------------------------------------------");
                        }
                        this.mServices.newServiceDumperLocked(fd, pw, args, opti, dumpAll, dumpPackage).dumpLocked();
                        pw.println();
                        if (dumpAll) {
                            pw.println("-------------------------------------------------------------------------------");
                        }
                        dumpRecentsLocked(fd, pw, args, opti, dumpAll, dumpPackage);
                        pw.println();
                        if (dumpAll) {
                            pw.println("-------------------------------------------------------------------------------");
                        }
                        dumpActivitiesLocked(fd, pw, args, opti, dumpAll, dumpClient, dumpPackage);
                        if (this.mAssociations.size() > 0) {
                            pw.println();
                            if (dumpAll) {
                                pw.println("-------------------------------------------------------------------------------");
                            }
                            dumpAssociationsLocked(fd, pw, args, opti, dumpAll, dumpClient, dumpPackage);
                        }
                        pw.println();
                        if (dumpAll) {
                            pw.println("-------------------------------------------------------------------------------");
                        }
                        dumpProcessesLocked(fd, pw, args, opti, dumpAll, dumpPackage);
                    } finally {
                        resetPriorityAfterLockedSection();
                    }
                }
            }
            Binder.restoreCallingIdentity(origId);
        }
    }

    void dumpActivitiesLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, boolean dumpClient, String dumpPackage) {
        pw.println("ACTIVITY MANAGER ACTIVITIES (dumpsys activity activities)");
        boolean dumpActivitiesLocked = this.mStackSupervisor.dumpActivitiesLocked(fd, pw, dumpAll, dumpClient, dumpPackage);
        boolean needSep = dumpActivitiesLocked;
        if (ActivityStackSupervisor.printThisActivity(pw, this.mFocusedActivity, dumpPackage, dumpActivitiesLocked, "  mFocusedActivity: ")) {
            dumpActivitiesLocked = true;
            needSep = false;
        }
        if (dumpPackage == null) {
            if (needSep) {
                pw.println();
            }
            dumpActivitiesLocked = true;
            this.mStackSupervisor.dump(pw, "  ");
        }
        if (!dumpActivitiesLocked) {
            pw.println("  (nothing)");
        }
    }

    void dumpRecentsLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage) {
        pw.println("ACTIVITY MANAGER RECENT TASKS (dumpsys activity recents)");
        boolean printedAnything = false;
        if (this.mRecentTasks != null && this.mRecentTasks.size() > 0) {
            boolean printedHeader = false;
            int N = this.mRecentTasks.size();
            for (int i = 0; i < N; i++) {
                TaskRecord tr = (TaskRecord) this.mRecentTasks.get(i);
                if (dumpPackage == null || (tr.realActivity != null && dumpPackage.equals(tr.realActivity))) {
                    if (!printedHeader) {
                        pw.println("  Recent tasks:");
                        printedHeader = true;
                        printedAnything = true;
                    }
                    pw.print("  * Recent #");
                    pw.print(i);
                    pw.print(": ");
                    pw.println(tr);
                    if (dumpAll) {
                        ((TaskRecord) this.mRecentTasks.get(i)).dump(pw, "    ");
                    }
                }
            }
        }
        if (!printedAnything) {
            pw.println("  (nothing)");
        }
    }

    void dumpAssociationsLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, boolean dumpClient, String dumpPackage) {
        pw.println("ACTIVITY MANAGER ASSOCIATIONS (dumpsys activity associations)");
        int dumpUid = 0;
        if (dumpPackage != null) {
            try {
                dumpUid = AppGlobals.getPackageManager().getPackageUid(dumpPackage, DumpState.DUMP_PREFERRED_XML, 0);
            } catch (RemoteException e) {
            }
        }
        boolean printedAnything = false;
        long now = SystemClock.uptimeMillis();
        int N1 = this.mAssociations.size();
        for (int i1 = 0; i1 < N1; i1++) {
            ArrayMap<ComponentName, SparseArray<ArrayMap<String, Association>>> targetComponents = (ArrayMap) this.mAssociations.valueAt(i1);
            int N2 = targetComponents.size();
            for (int i2 = 0; i2 < N2; i2++) {
                SparseArray<ArrayMap<String, Association>> sourceUids = (SparseArray) targetComponents.valueAt(i2);
                int N3 = sourceUids.size();
                for (int i3 = 0; i3 < N3; i3++) {
                    ArrayMap<String, Association> sourceProcesses = (ArrayMap) sourceUids.valueAt(i3);
                    int N4 = sourceProcesses.size();
                    for (int i4 = 0; i4 < N4; i4++) {
                        Association ass = (Association) sourceProcesses.valueAt(i4);
                        if (dumpPackage == null || ass.mTargetComponent.getPackageName().equals(dumpPackage) || UserHandle.getAppId(ass.mSourceUid) == dumpUid) {
                            printedAnything = true;
                            pw.print("  ");
                            pw.print(ass.mTargetProcess);
                            pw.print("/");
                            UserHandle.formatUid(pw, ass.mTargetUid);
                            pw.print(" <- ");
                            pw.print(ass.mSourceProcess);
                            pw.print("/");
                            UserHandle.formatUid(pw, ass.mSourceUid);
                            pw.println();
                            pw.print("    via ");
                            pw.print(ass.mTargetComponent.flattenToShortString());
                            pw.println();
                            pw.print("    ");
                            long dur = ass.mTime;
                            if (ass.mNesting > 0) {
                                dur += now - ass.mStartTime;
                            }
                            TimeUtils.formatDuration(dur, pw);
                            pw.print(" (");
                            pw.print(ass.mCount);
                            pw.print(" times)");
                            pw.print("  ");
                            for (int i = 0; i < ass.mStateTimes.length; i++) {
                                long amt = ass.mStateTimes[i];
                                if (ass.mLastState + 1 == i) {
                                    amt += now - ass.mLastStateUptime;
                                }
                                if (amt != 0) {
                                    pw.print(" ");
                                    pw.print(ProcessList.makeProcStateString(i - 1));
                                    pw.print("=");
                                    TimeUtils.formatDuration(amt, pw);
                                    if (ass.mLastState + 1 == i) {
                                        pw.print("*");
                                    }
                                }
                            }
                            pw.println();
                            if (ass.mNesting > 0) {
                                pw.print("    Currently active: ");
                                TimeUtils.formatDuration(now - ass.mStartTime, pw);
                                pw.println();
                            }
                        }
                    }
                }
            }
        }
        if (!printedAnything) {
            pw.println("  (nothing)");
        }
    }

    boolean dumpUids(PrintWriter pw, String dumpPackage, SparseArray<UidRecord> uids, String header, boolean needSep) {
        boolean printed = false;
        int whichAppId = -1;
        if (dumpPackage != null) {
            try {
                whichAppId = UserHandle.getAppId(this.mContext.getPackageManager().getApplicationInfo(dumpPackage, 0).uid);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < uids.size(); i++) {
            UidRecord uidRec = (UidRecord) uids.valueAt(i);
            if (dumpPackage == null || UserHandle.getAppId(uidRec.uid) == whichAppId) {
                if (!printed) {
                    printed = true;
                    if (needSep) {
                        pw.println();
                    }
                    pw.print("  ");
                    pw.println(header);
                    needSep = true;
                }
                pw.print("    UID ");
                UserHandle.formatUid(pw, uidRec.uid);
                pw.print(": ");
                pw.println(uidRec);
            }
        }
        return printed;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void dumpProcessesLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage) {
        ProcessRecord r;
        boolean printed;
        int i;
        ArrayMap<String, SparseArray<Pair<Long, String>>> procs;
        String proc;
        SparseArray<Pair<Long, String>> uids;
        int j;
        StringBuilder sb;
        Pair<Long, String> val;
        long now;
        boolean needSep = false;
        boolean printedAnything = false;
        int numPers = 0;
        pw.println("ACTIVITY MANAGER RUNNING PROCESSES (dumpsys activity processes)");
        if (dumpAll) {
            int NP = this.mProcessNames.getMap().size();
            for (int ip = 0; ip < NP; ip++) {
                SparseArray<ProcessRecord> procs2 = (SparseArray) this.mProcessNames.getMap().valueAt(ip);
                int NA = procs2.size();
                for (int ia = 0; ia < NA; ia++) {
                    r = (ProcessRecord) procs2.valueAt(ia);
                    if (dumpPackage == null || r.pkgList.containsKey(dumpPackage)) {
                        if (!needSep) {
                            pw.println("  All known processes:");
                            needSep = true;
                            printedAnything = true;
                        }
                        pw.print(r.persistent ? "  *PERS*" : "  *APP*");
                        pw.print(" UID ");
                        pw.print(procs2.keyAt(ia));
                        pw.print(" ");
                        pw.println(r);
                        r.dump(pw, "    ");
                        if (r.persistent) {
                            numPers++;
                        }
                    }
                }
            }
        }
        if (this.mIsolatedProcesses.size() > 0) {
            printed = false;
            for (i = 0; i < this.mIsolatedProcesses.size(); i++) {
                r = (ProcessRecord) this.mIsolatedProcesses.valueAt(i);
                if (dumpPackage == null || r.pkgList.containsKey(dumpPackage)) {
                    if (!printed) {
                        if (needSep) {
                            pw.println();
                        }
                        pw.println("  Isolated process list (sorted by uid):");
                        printedAnything = true;
                        printed = true;
                        needSep = true;
                    }
                    pw.println(String.format("%sIsolated #%2d: %s", new Object[]{"    ", Integer.valueOf(i), r.toString()}));
                }
            }
        }
        if (this.mActiveUids.size() > 0) {
            if (dumpUids(pw, dumpPackage, this.mActiveUids, "UID states:", needSep)) {
                needSep = true;
                printedAnything = true;
            }
        }
        if (this.mValidateUids.size() > 0) {
            if (dumpUids(pw, dumpPackage, this.mValidateUids, "UID validation:", needSep)) {
                needSep = true;
                printedAnything = true;
            }
        }
        if (this.mLruProcesses.size() > 0) {
            if (needSep) {
                pw.println();
            }
            pw.print("  Process LRU list (sorted by oom_adj, ");
            pw.print(this.mLruProcesses.size());
            pw.print(" total, non-act at ");
            pw.print(this.mLruProcesses.size() - this.mLruProcessActivityStart);
            pw.print(", non-svc at ");
            pw.print(this.mLruProcesses.size() - this.mLruProcessServiceStart);
            pw.println("):");
            dumpProcessOomList(pw, this, this.mLruProcesses, "    ", "Proc", "PERS", false, dumpPackage);
            needSep = true;
            printedAnything = true;
        }
        if (dumpAll || dumpPackage != null) {
            synchronized (this.mPidsSelfLocked) {
                printed = false;
                for (i = 0; i < this.mPidsSelfLocked.size(); i++) {
                    r = (ProcessRecord) this.mPidsSelfLocked.valueAt(i);
                    if (dumpPackage == null || r.pkgList.containsKey(dumpPackage)) {
                        if (!printed) {
                            if (needSep) {
                                pw.println();
                            }
                            needSep = true;
                            pw.println("  PID mappings:");
                            printed = true;
                            printedAnything = true;
                        }
                        pw.print("    PID #");
                        pw.print(this.mPidsSelfLocked.keyAt(i));
                        pw.print(": ");
                        pw.println(this.mPidsSelfLocked.valueAt(i));
                    }
                }
            }
        }
        if (this.mForegroundProcesses.size() > 0) {
            synchronized (this.mPidsSelfLocked) {
                printed = false;
                for (i = 0; i < this.mForegroundProcesses.size(); i++) {
                    r = (ProcessRecord) this.mPidsSelfLocked.get(((ForegroundToken) this.mForegroundProcesses.valueAt(i)).pid);
                    if (dumpPackage == null || (r != null && r.pkgList.containsKey(dumpPackage))) {
                        if (!printed) {
                            if (needSep) {
                                pw.println();
                            }
                            needSep = true;
                            pw.println("  Foreground Processes:");
                            printed = true;
                            printedAnything = true;
                        }
                        pw.print("    PID #");
                        pw.print(this.mForegroundProcesses.keyAt(i));
                        pw.print(": ");
                        pw.println(this.mForegroundProcesses.valueAt(i));
                    }
                }
            }
        }
        if (this.mPersistentStartingProcesses.size() > 0) {
            if (needSep) {
                pw.println();
            }
            needSep = true;
            printedAnything = true;
            pw.println("  Persisent processes that are starting:");
            dumpProcessList(pw, this, this.mPersistentStartingProcesses, "    ", "Starting Norm", "Restarting PERS", dumpPackage);
        }
        if (this.mRemovedProcesses.size() > 0) {
            if (needSep) {
                pw.println();
            }
            needSep = true;
            printedAnything = true;
            pw.println("  Processes that are being removed:");
            dumpProcessList(pw, this, this.mRemovedProcesses, "    ", "Removed Norm", "Removed PERS", dumpPackage);
        }
        if (this.mProcessesOnHold.size() > 0) {
            if (needSep) {
                pw.println();
            }
            needSep = true;
            printedAnything = true;
            pw.println("  Processes that are on old until the system is ready:");
            dumpProcessList(pw, this, this.mProcessesOnHold, "    ", "OnHold Norm", "OnHold PERS", dumpPackage);
        }
        needSep = this.mAppErrors.dumpLocked(fd, pw, dumpProcessesToGc(fd, pw, args, opti, needSep, dumpAll, dumpPackage), dumpPackage);
        if (needSep) {
            printedAnything = true;
        }
        if (dumpPackage == null) {
            pw.println();
            needSep = false;
            this.mUserController.dump(pw, dumpAll);
        }
        if (this.mHomeProcess != null && (dumpPackage == null || this.mHomeProcess.pkgList.containsKey(dumpPackage))) {
            if (needSep) {
                pw.println();
                needSep = false;
            }
            pw.println("  mHomeProcess: " + this.mHomeProcess);
        }
        if (this.mPreviousProcess != null && (dumpPackage == null || this.mPreviousProcess.pkgList.containsKey(dumpPackage))) {
            if (needSep) {
                pw.println();
                needSep = false;
            }
            pw.println("  mPreviousProcess: " + this.mPreviousProcess);
        }
        if (dumpAll) {
            StringBuilder stringBuilder = new StringBuilder(128);
            stringBuilder.append("  mPreviousProcessVisibleTime: ");
            TimeUtils.formatDuration(this.mPreviousProcessVisibleTime, stringBuilder);
            pw.println(stringBuilder);
        }
        if (this.mHeavyWeightProcess != null && (dumpPackage == null || this.mHeavyWeightProcess.pkgList.containsKey(dumpPackage))) {
            if (needSep) {
                pw.println();
                needSep = false;
            }
            pw.println("  mHeavyWeightProcess: " + this.mHeavyWeightProcess);
        }
        if (dumpPackage == null) {
            pw.println("  mConfiguration: " + this.mConfiguration);
        }
        if (dumpAll) {
            pw.println("  mConfigWillChange: " + getFocusedStack().mConfigWillChange);
            if (this.mCompatModePackages.getPackages().size() > 0) {
                printed = false;
                for (Entry<String, Integer> entry : this.mCompatModePackages.getPackages().entrySet()) {
                    String pkg = (String) entry.getKey();
                    int mode = ((Integer) entry.getValue()).intValue();
                    if (dumpPackage == null || dumpPackage.equals(pkg)) {
                        if (!printed) {
                            pw.println("  mScreenCompatPackages:");
                            printed = true;
                        }
                        pw.print("    ");
                        pw.print(pkg);
                        pw.print(": ");
                        pw.print(mode);
                        pw.println();
                    }
                }
            }
        }
        if (dumpPackage == null) {
            pw.println("  mWakefulness=" + PowerManagerInternal.wakefulnessToString(this.mWakefulness));
            pw.println("  mSleepTokens=" + this.mSleepTokens);
            pw.println("  mSleeping=" + this.mSleeping + " mLockScreenShown=" + lockScreenShownToString());
            pw.println("  mShuttingDown=" + this.mShuttingDown + " mTestPssMode=" + this.mTestPssMode);
            if (this.mRunningVoice != null) {
                pw.println("  mRunningVoice=" + this.mRunningVoice);
                pw.println("  mVoiceWakeLock" + this.mVoiceWakeLock);
            }
        }
        if (this.mDebugApp == null && this.mOrigDebugApp == null && !this.mDebugTransient) {
            if (this.mOrigWaitForDebugger) {
            }
            if (this.mCurAppTimeTracker != null) {
                this.mCurAppTimeTracker.dumpWithHeader(pw, "  ", true);
            }
            if (this.mMemWatchProcesses.getMap().size() > 0) {
                pw.println("  Mem watch processes:");
                procs = this.mMemWatchProcesses.getMap();
                for (i = 0; i < procs.size(); i++) {
                    proc = (String) procs.keyAt(i);
                    uids = (SparseArray) procs.valueAt(i);
                    for (j = 0; j < uids.size(); j++) {
                        if (needSep) {
                            pw.println();
                            needSep = false;
                        }
                        sb = new StringBuilder();
                        sb.append("    ").append(proc).append('/');
                        UserHandle.formatUid(sb, uids.keyAt(j));
                        val = (Pair) uids.valueAt(j);
                        sb.append(": ");
                        DebugUtils.sizeValueToString(((Long) val.first).longValue(), sb);
                        if (val.second != null) {
                            sb.append(", report to ").append((String) val.second);
                        }
                        pw.println(sb.toString());
                    }
                }
                pw.print("  mMemWatchDumpProcName=");
                pw.println(this.mMemWatchDumpProcName);
                pw.print("  mMemWatchDumpFile=");
                pw.println(this.mMemWatchDumpFile);
                pw.print("  mMemWatchDumpPid=");
                pw.print(this.mMemWatchDumpPid);
                pw.print(" mMemWatchDumpUid=");
                pw.println(this.mMemWatchDumpUid);
            }
            if (this.mTrackAllocationApp != null) {
                if (dumpPackage != null) {
                }
                if (needSep) {
                    pw.println();
                    needSep = false;
                }
                pw.println("  mTrackAllocationApp=" + this.mTrackAllocationApp);
            }
            if (this.mProfileApp == null && this.mProfileProc == null && this.mProfileFile == null) {
                if (this.mProfileFd != null) {
                }
                if (this.mNativeDebuggingApp != null) {
                    if (dumpPackage != null) {
                    }
                    if (needSep) {
                        pw.println();
                    }
                    pw.println("  mNativeDebuggingApp=" + this.mNativeDebuggingApp);
                }
                if (dumpPackage == null) {
                    if (this.mAlwaysFinishActivities || this.mLenientBackgroundCheck) {
                        pw.println("  mAlwaysFinishActivities=" + this.mAlwaysFinishActivities + " mLenientBackgroundCheck=" + this.mLenientBackgroundCheck);
                    }
                    if (this.mController != null) {
                        pw.println("  mController=" + this.mController + " mControllerIsAMonkey=" + this.mControllerIsAMonkey);
                    }
                    if (dumpAll) {
                        pw.println("  Total persistent processes: " + numPers);
                        pw.println("  mProcessesReady=" + this.mProcessesReady + " mSystemReady=" + this.mSystemReady + " mBooted=" + this.mBooted + " mFactoryTest=" + this.mFactoryTest);
                        pw.println("  mBooting=" + this.mBooting + " mCallFinishBooting=" + this.mCallFinishBooting + " mBootAnimationComplete=" + this.mBootAnimationComplete);
                        pw.print("  mLastPowerCheckRealtime=");
                        TimeUtils.formatDuration(this.mLastPowerCheckRealtime, pw);
                        pw.println("");
                        pw.print("  mLastPowerCheckUptime=");
                        TimeUtils.formatDuration(this.mLastPowerCheckUptime, pw);
                        pw.println("");
                        pw.println("  mGoingToSleep=" + this.mStackSupervisor.mGoingToSleep);
                        pw.println("  mLaunchingActivity=" + this.mStackSupervisor.mLaunchingActivity);
                        pw.println("  mAdjSeq=" + this.mAdjSeq + " mLruSeq=" + this.mLruSeq);
                        pw.println("  mNumNonCachedProcs=" + this.mNumNonCachedProcs + " (" + this.mLruProcesses.size() + " total)" + " mNumCachedHiddenProcs=" + this.mNumCachedHiddenProcs + " mNumServiceProcs=" + this.mNumServiceProcs + " mNewNumServiceProcs=" + this.mNewNumServiceProcs);
                        pw.println("  mAllowLowerMemLevel=" + this.mAllowLowerMemLevel + " mLastMemoryLevel=" + this.mLastMemoryLevel + " mLastNumProcesses=" + this.mLastNumProcesses);
                        now = SystemClock.uptimeMillis();
                        pw.print("  mLastIdleTime=");
                        TimeUtils.formatDuration(now, this.mLastIdleTime, pw);
                        pw.print(" mLowRamSinceLastIdle=");
                        TimeUtils.formatDuration(getLowRamTimeSinceIdle(now), pw);
                        pw.println();
                    }
                }
                if (printedAnything) {
                    pw.println("  (nothing)");
                }
            }
            if (dumpPackage != null) {
            }
            if (needSep) {
                pw.println();
                needSep = false;
            }
            pw.println("  mProfileApp=" + this.mProfileApp + " mProfileProc=" + this.mProfileProc);
            pw.println("  mProfileFile=" + this.mProfileFile + " mProfileFd=" + this.mProfileFd);
            pw.println("  mSamplingInterval=" + this.mSamplingInterval + " mAutoStopProfiler=" + this.mAutoStopProfiler);
            pw.println("  mProfileType=" + this.mProfileType);
            if (this.mNativeDebuggingApp != null) {
                if (dumpPackage != null) {
                }
                if (needSep) {
                    pw.println();
                }
                pw.println("  mNativeDebuggingApp=" + this.mNativeDebuggingApp);
            }
            if (dumpPackage == null) {
                pw.println("  mAlwaysFinishActivities=" + this.mAlwaysFinishActivities + " mLenientBackgroundCheck=" + this.mLenientBackgroundCheck);
                if (this.mController != null) {
                    pw.println("  mController=" + this.mController + " mControllerIsAMonkey=" + this.mControllerIsAMonkey);
                }
                if (dumpAll) {
                    pw.println("  Total persistent processes: " + numPers);
                    pw.println("  mProcessesReady=" + this.mProcessesReady + " mSystemReady=" + this.mSystemReady + " mBooted=" + this.mBooted + " mFactoryTest=" + this.mFactoryTest);
                    pw.println("  mBooting=" + this.mBooting + " mCallFinishBooting=" + this.mCallFinishBooting + " mBootAnimationComplete=" + this.mBootAnimationComplete);
                    pw.print("  mLastPowerCheckRealtime=");
                    TimeUtils.formatDuration(this.mLastPowerCheckRealtime, pw);
                    pw.println("");
                    pw.print("  mLastPowerCheckUptime=");
                    TimeUtils.formatDuration(this.mLastPowerCheckUptime, pw);
                    pw.println("");
                    pw.println("  mGoingToSleep=" + this.mStackSupervisor.mGoingToSleep);
                    pw.println("  mLaunchingActivity=" + this.mStackSupervisor.mLaunchingActivity);
                    pw.println("  mAdjSeq=" + this.mAdjSeq + " mLruSeq=" + this.mLruSeq);
                    pw.println("  mNumNonCachedProcs=" + this.mNumNonCachedProcs + " (" + this.mLruProcesses.size() + " total)" + " mNumCachedHiddenProcs=" + this.mNumCachedHiddenProcs + " mNumServiceProcs=" + this.mNumServiceProcs + " mNewNumServiceProcs=" + this.mNewNumServiceProcs);
                    pw.println("  mAllowLowerMemLevel=" + this.mAllowLowerMemLevel + " mLastMemoryLevel=" + this.mLastMemoryLevel + " mLastNumProcesses=" + this.mLastNumProcesses);
                    now = SystemClock.uptimeMillis();
                    pw.print("  mLastIdleTime=");
                    TimeUtils.formatDuration(now, this.mLastIdleTime, pw);
                    pw.print(" mLowRamSinceLastIdle=");
                    TimeUtils.formatDuration(getLowRamTimeSinceIdle(now), pw);
                    pw.println();
                }
            }
            if (printedAnything) {
                pw.println("  (nothing)");
            }
        }
        if (dumpPackage != null) {
            if (!dumpPackage.equals(this.mDebugApp)) {
            }
        }
        if (needSep) {
            pw.println();
            needSep = false;
        }
        pw.println("  mDebugApp=" + this.mDebugApp + "/orig=" + this.mOrigDebugApp + " mDebugTransient=" + this.mDebugTransient + " mOrigWaitForDebugger=" + this.mOrigWaitForDebugger);
        if (this.mCurAppTimeTracker != null) {
            this.mCurAppTimeTracker.dumpWithHeader(pw, "  ", true);
        }
        if (this.mMemWatchProcesses.getMap().size() > 0) {
            pw.println("  Mem watch processes:");
            procs = this.mMemWatchProcesses.getMap();
            for (i = 0; i < procs.size(); i++) {
                proc = (String) procs.keyAt(i);
                uids = (SparseArray) procs.valueAt(i);
                for (j = 0; j < uids.size(); j++) {
                    if (needSep) {
                        pw.println();
                        needSep = false;
                    }
                    sb = new StringBuilder();
                    sb.append("    ").append(proc).append('/');
                    UserHandle.formatUid(sb, uids.keyAt(j));
                    val = (Pair) uids.valueAt(j);
                    sb.append(": ");
                    DebugUtils.sizeValueToString(((Long) val.first).longValue(), sb);
                    if (val.second != null) {
                        sb.append(", report to ").append((String) val.second);
                    }
                    pw.println(sb.toString());
                }
            }
            pw.print("  mMemWatchDumpProcName=");
            pw.println(this.mMemWatchDumpProcName);
            pw.print("  mMemWatchDumpFile=");
            pw.println(this.mMemWatchDumpFile);
            pw.print("  mMemWatchDumpPid=");
            pw.print(this.mMemWatchDumpPid);
            pw.print(" mMemWatchDumpUid=");
            pw.println(this.mMemWatchDumpUid);
        }
        if (this.mTrackAllocationApp != null) {
            if (dumpPackage != null) {
            }
            if (needSep) {
                pw.println();
                needSep = false;
            }
            pw.println("  mTrackAllocationApp=" + this.mTrackAllocationApp);
        }
        if (this.mProfileFd != null) {
            if (dumpPackage != null) {
            }
            if (needSep) {
                pw.println();
                needSep = false;
            }
            pw.println("  mProfileApp=" + this.mProfileApp + " mProfileProc=" + this.mProfileProc);
            pw.println("  mProfileFile=" + this.mProfileFile + " mProfileFd=" + this.mProfileFd);
            pw.println("  mSamplingInterval=" + this.mSamplingInterval + " mAutoStopProfiler=" + this.mAutoStopProfiler);
            pw.println("  mProfileType=" + this.mProfileType);
        }
        if (this.mNativeDebuggingApp != null) {
            if (dumpPackage != null) {
            }
            if (needSep) {
                pw.println();
            }
            pw.println("  mNativeDebuggingApp=" + this.mNativeDebuggingApp);
        }
        if (dumpPackage == null) {
            pw.println("  mAlwaysFinishActivities=" + this.mAlwaysFinishActivities + " mLenientBackgroundCheck=" + this.mLenientBackgroundCheck);
            if (this.mController != null) {
                pw.println("  mController=" + this.mController + " mControllerIsAMonkey=" + this.mControllerIsAMonkey);
            }
            if (dumpAll) {
                pw.println("  Total persistent processes: " + numPers);
                pw.println("  mProcessesReady=" + this.mProcessesReady + " mSystemReady=" + this.mSystemReady + " mBooted=" + this.mBooted + " mFactoryTest=" + this.mFactoryTest);
                pw.println("  mBooting=" + this.mBooting + " mCallFinishBooting=" + this.mCallFinishBooting + " mBootAnimationComplete=" + this.mBootAnimationComplete);
                pw.print("  mLastPowerCheckRealtime=");
                TimeUtils.formatDuration(this.mLastPowerCheckRealtime, pw);
                pw.println("");
                pw.print("  mLastPowerCheckUptime=");
                TimeUtils.formatDuration(this.mLastPowerCheckUptime, pw);
                pw.println("");
                pw.println("  mGoingToSleep=" + this.mStackSupervisor.mGoingToSleep);
                pw.println("  mLaunchingActivity=" + this.mStackSupervisor.mLaunchingActivity);
                pw.println("  mAdjSeq=" + this.mAdjSeq + " mLruSeq=" + this.mLruSeq);
                pw.println("  mNumNonCachedProcs=" + this.mNumNonCachedProcs + " (" + this.mLruProcesses.size() + " total)" + " mNumCachedHiddenProcs=" + this.mNumCachedHiddenProcs + " mNumServiceProcs=" + this.mNumServiceProcs + " mNewNumServiceProcs=" + this.mNewNumServiceProcs);
                pw.println("  mAllowLowerMemLevel=" + this.mAllowLowerMemLevel + " mLastMemoryLevel=" + this.mLastMemoryLevel + " mLastNumProcesses=" + this.mLastNumProcesses);
                now = SystemClock.uptimeMillis();
                pw.print("  mLastIdleTime=");
                TimeUtils.formatDuration(now, this.mLastIdleTime, pw);
                pw.print(" mLowRamSinceLastIdle=");
                TimeUtils.formatDuration(getLowRamTimeSinceIdle(now), pw);
                pw.println();
            }
        }
        if (printedAnything) {
            pw.println("  (nothing)");
        }
    }

    boolean dumpProcessesToGc(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean needSep, boolean dumpAll, String dumpPackage) {
        if (this.mProcessesToGc.size() > 0) {
            boolean printed = false;
            long now = SystemClock.uptimeMillis();
            for (int i = 0; i < this.mProcessesToGc.size(); i++) {
                ProcessRecord proc = (ProcessRecord) this.mProcessesToGc.get(i);
                if (dumpPackage == null || dumpPackage.equals(proc.info.packageName)) {
                    if (!printed) {
                        if (needSep) {
                            pw.println();
                        }
                        needSep = true;
                        pw.println("  Processes that are waiting to GC:");
                        printed = true;
                    }
                    pw.print("    Process ");
                    pw.println(proc);
                    pw.print("      lowMem=");
                    pw.print(proc.reportLowMemory);
                    pw.print(", last gced=");
                    pw.print(now - proc.lastRequestedGc);
                    pw.print(" ms ago, last lowMem=");
                    pw.print(now - proc.lastLowMemory);
                    pw.println(" ms ago");
                }
            }
        }
        return needSep;
    }

    void printOomLevel(PrintWriter pw, String name, int adj) {
        pw.print("    ");
        if (adj >= 0) {
            pw.print(' ');
            if (adj < 10) {
                pw.print(' ');
            }
        } else if (adj > -10) {
            pw.print(' ');
        }
        pw.print(adj);
        pw.print(": ");
        pw.print(name);
        pw.print(" (");
        pw.print(stringifySize(this.mProcessList.getMemLevel(adj), 1024));
        pw.println(")");
    }

    boolean dumpOomLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll) {
        boolean needSep = false;
        if (this.mLruProcesses.size() > 0) {
            if (null != null) {
                pw.println();
            }
            pw.println("  OOM levels:");
            printOomLevel(pw, "SYSTEM_ADJ", -900);
            printOomLevel(pw, "PERSISTENT_PROC_ADJ", -800);
            printOomLevel(pw, "PERSISTENT_SERVICE_ADJ", -700);
            printOomLevel(pw, "FOREGROUND_APP_ADJ", 0);
            printOomLevel(pw, "VISIBLE_APP_ADJ", 100);
            printOomLevel(pw, "PERCEPTIBLE_APP_ADJ", FIRST_BROADCAST_QUEUE_MSG);
            printOomLevel(pw, "BACKUP_APP_ADJ", FIRST_COMPAT_MODE_MSG);
            printOomLevel(pw, "HEAVY_WEIGHT_APP_ADJ", 400);
            printOomLevel(pw, "SERVICE_ADJ", 500);
            printOomLevel(pw, "HOME_APP_ADJ", 600);
            printOomLevel(pw, "PREVIOUS_APP_ADJ", 700);
            printOomLevel(pw, "SERVICE_B_ADJ", 800);
            printOomLevel(pw, "CACHED_APP_MIN_ADJ", 900);
            printOomLevel(pw, "CACHED_APP_MAX_ADJ", 906);
            if (true) {
                pw.println();
            }
            pw.print("  Process OOM control (");
            pw.print(this.mLruProcesses.size());
            pw.print(" total, non-act at ");
            pw.print(this.mLruProcesses.size() - this.mLruProcessActivityStart);
            pw.print(", non-svc at ");
            pw.print(this.mLruProcesses.size() - this.mLruProcessServiceStart);
            pw.println("):");
            dumpProcessOomList(pw, this, this.mLruProcesses, "    ", "Proc", "PERS", true, null);
            needSep = true;
        }
        dumpProcessesToGc(fd, pw, args, opti, needSep, dumpAll, null);
        pw.println();
        pw.println("  mHomeProcess: " + this.mHomeProcess);
        pw.println("  mPreviousProcess: " + this.mPreviousProcess);
        if (this.mHeavyWeightProcess != null) {
            pw.println("  mHeavyWeightProcess: " + this.mHeavyWeightProcess);
        }
        return true;
    }

    protected boolean dumpProvider(FileDescriptor fd, PrintWriter pw, String name, String[] args, int opti, boolean dumpAll) {
        return !this.mProviderMap.dumpProvider(fd, pw, name, args, opti, dumpAll) ? this.mProviderMapForClone.dumpProvider(fd, pw, name, args, opti, dumpAll) : true;
    }

    protected boolean dumpActivity(FileDescriptor fd, PrintWriter pw, String name, String[] args, int opti, boolean dumpAll) {
        ArrayList<ActivityRecord> activities;
        String[] newArgs;
        int i;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                activities = this.mStackSupervisor.getDumpActivitiesLocked(name);
                if (activities.size() <= 0) {
                    return false;
                }
                newArgs = new String[(args.length - opti)];
                System.arraycopy(args, opti, newArgs, 0, args.length - opti);
                TaskRecord lastTask = null;
                boolean needSep = false;
                for (i = activities.size() - 1; i >= 0; i--) {
                    ActivityRecord r = (ActivityRecord) activities.get(i);
                    if (needSep) {
                        pw.println();
                    }
                    needSep = true;
                    synchronized (this) {
                        try {
                            boostPriorityForLockedSection();
                            if (lastTask != r.task) {
                                lastTask = r.task;
                                pw.print("TASK ");
                                pw.print(lastTask.affinity);
                                pw.print(" id=");
                                pw.println(lastTask.taskId);
                                if (dumpAll) {
                                    lastTask.dump(pw, "  ");
                                }
                            }
                        } catch (Throwable th) {
                            resetPriorityAfterLockedSection();
                        }
                    }
                    resetPriorityAfterLockedSection();
                    dumpActivity("  ", fd, pw, (ActivityRecord) activities.get(i), newArgs, dumpAll);
                }
                return true;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
        dumpActivity("  ", fd, pw, (ActivityRecord) activities.get(i), newArgs, dumpAll);
    }

    private void dumpActivity(String prefix, FileDescriptor fd, PrintWriter pw, ActivityRecord r, String[] args, boolean dumpAll) {
        String innerPrefix = prefix + "  ";
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                pw.print(prefix);
                pw.print("ACTIVITY ");
                pw.print(r.shortComponentName);
                pw.print(" ");
                pw.print(Integer.toHexString(System.identityHashCode(r)));
                pw.print(" pid=");
                if (r.app != null) {
                    pw.println(r.app.pid);
                } else {
                    pw.println("(not running)");
                }
                if (dumpAll) {
                    r.dump(pw, innerPrefix);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        if (r.app != null && r.app.thread != null) {
            pw.flush();
            TransferPipe tp;
            try {
                tp = new TransferPipe();
                r.app.thread.dumpActivity(tp.getWriteFd().getFileDescriptor(), r.appToken, innerPrefix, args);
                tp.go(fd);
                tp.kill();
            } catch (IOException e) {
                pw.println(innerPrefix + "Failure while dumping the activity: " + e);
            } catch (RemoteException e2) {
                pw.println(innerPrefix + "Got a RemoteException while dumping the activity");
            } catch (Throwable th) {
                tp.kill();
            }
        }
    }

    void dumpBroadcastsLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage) {
        boolean needSep = false;
        boolean onlyHistory = false;
        int printedAnything = false;
        if ("history".equals(dumpPackage)) {
            if (opti < args.length && "-s".equals(args[opti])) {
                dumpAll = false;
            }
            onlyHistory = true;
            dumpPackage = null;
        }
        pw.println("ACTIVITY MANAGER BROADCAST STATE (dumpsys activity broadcasts)");
        if (!onlyHistory && dumpAll) {
            if (this.mRegisteredReceivers.size() > 0) {
                boolean printed = false;
                for (ReceiverList r : this.mRegisteredReceivers.values()) {
                    if (dumpPackage != null) {
                        if (r.app != null) {
                            if (!dumpPackage.equals(r.app.info.packageName)) {
                            }
                        }
                    }
                    if (!printed) {
                        pw.println("  Registered Receivers:");
                        needSep = true;
                        printed = true;
                        boolean printedAnything2 = true;
                    }
                    pw.print("  * ");
                    pw.println(r);
                    r.dump(pw, "    ");
                }
            }
            if (this.mReceiverResolver.dump(pw, needSep ? "\n  Receiver Resolver Table:" : "  Receiver Resolver Table:", "    ", dumpPackage, false, false)) {
                needSep = true;
                printedAnything = true;
            }
        }
        for (BroadcastQueue q : this.mBroadcastQueues) {
            needSep = q.dumpLocked(fd, pw, args, opti, dumpAll, dumpPackage, needSep);
            printedAnything |= needSep;
        }
        needSep = true;
        if (!(onlyHistory || this.mStickyBroadcasts == null || dumpPackage != null)) {
            for (int user = 0; user < this.mStickyBroadcasts.size(); user++) {
                if (needSep) {
                    pw.println();
                }
                needSep = true;
                printedAnything = 1;
                pw.print("  Sticky broadcasts for user ");
                pw.print(this.mStickyBroadcasts.keyAt(user));
                pw.println(":");
                StringBuilder sb = new StringBuilder(128);
                for (Entry<String, ArrayList<Intent>> ent : ((ArrayMap) this.mStickyBroadcasts.valueAt(user)).entrySet()) {
                    pw.print("  * Sticky action ");
                    pw.print((String) ent.getKey());
                    if (dumpAll) {
                        pw.println(":");
                        ArrayList<Intent> intents = (ArrayList) ent.getValue();
                        int N = intents.size();
                        for (int i = 0; i < N; i++) {
                            sb.setLength(0);
                            sb.append("    Intent: ");
                            ((Intent) intents.get(i)).toShortString(sb, true, true, false, false);
                            pw.println(sb.toString());
                            Bundle bundle = ((Intent) intents.get(i)).getExtras();
                            if (bundle != null) {
                                pw.print("      ");
                                pw.println(bundle.toString());
                            }
                        }
                    } else {
                        pw.println("");
                    }
                }
            }
        }
        if (!onlyHistory && dumpAll) {
            pw.println();
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                pw.println("  mBroadcastsScheduled [" + queue.mQueueName + "]=" + queue.mBroadcastsScheduled);
            }
            pw.println("  mHandler:");
            this.mHandler.dump(new PrintWriterPrinter(pw), "    ");
            printedAnything = 1;
        }
        if (printedAnything == 0) {
            pw.println("  (nothing)");
        }
    }

    void dumpBroadcastStatsLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage) {
        if (this.mCurBroadcastStats != null) {
            pw.println("ACTIVITY MANAGER BROADCAST STATS STATE (dumpsys activity broadcast-stats)");
            long now = SystemClock.elapsedRealtime();
            if (this.mLastBroadcastStats != null) {
                pw.print("  Last stats (from ");
                TimeUtils.formatDuration(this.mLastBroadcastStats.mStartRealtime, now, pw);
                pw.print(" to ");
                TimeUtils.formatDuration(this.mLastBroadcastStats.mEndRealtime, now, pw);
                pw.print(", ");
                TimeUtils.formatDuration(this.mLastBroadcastStats.mEndUptime - this.mLastBroadcastStats.mStartUptime, pw);
                pw.println(" uptime):");
                if (!this.mLastBroadcastStats.dumpStats(pw, "    ", dumpPackage)) {
                    pw.println("    (nothing)");
                }
                pw.println();
            }
            pw.print("  Current stats (from ");
            TimeUtils.formatDuration(this.mCurBroadcastStats.mStartRealtime, now, pw);
            pw.print(" to now, ");
            TimeUtils.formatDuration(SystemClock.uptimeMillis() - this.mCurBroadcastStats.mStartUptime, pw);
            pw.println(" uptime):");
            if (!this.mCurBroadcastStats.dumpStats(pw, "    ", dumpPackage)) {
                pw.println("    (nothing)");
            }
        }
    }

    void dumpBroadcastStatsCheckinLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean fullCheckin, String dumpPackage) {
        if (this.mCurBroadcastStats != null) {
            if (this.mLastBroadcastStats != null) {
                this.mLastBroadcastStats.dumpCheckinStats(pw, dumpPackage);
                if (fullCheckin) {
                    this.mLastBroadcastStats = null;
                    return;
                }
            }
            this.mCurBroadcastStats.dumpCheckinStats(pw, dumpPackage);
            if (fullCheckin) {
                this.mCurBroadcastStats = null;
            }
        }
    }

    void dumpProvidersLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage) {
        new ItemMatcher().build(args, opti);
        pw.println("ACTIVITY MANAGER CONTENT PROVIDERS (dumpsys activity providers)");
        boolean needSep = !this.mProviderMap.dumpProvidersLocked(pw, dumpAll, dumpPackage) ? this.mProviderMapForClone.dumpProvidersLocked(pw, dumpAll, dumpPackage) : true;
        boolean z = needSep;
        if (this.mLaunchingProviders.size() > 0) {
            boolean printed = false;
            for (int i = this.mLaunchingProviders.size() - 1; i >= 0; i--) {
                ContentProviderRecord r = (ContentProviderRecord) this.mLaunchingProviders.get(i);
                if (dumpPackage == null || dumpPackage.equals(r.name.getPackageName())) {
                    if (!printed) {
                        if (needSep) {
                            pw.println();
                        }
                        needSep = true;
                        pw.println("  Launching content providers:");
                        printed = true;
                        z = true;
                    }
                    pw.print("  Launching #");
                    pw.print(i);
                    pw.print(": ");
                    pw.println(r);
                }
            }
        }
        if (!z) {
            pw.println("  (nothing)");
        }
    }

    void dumpPermissionsLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage) {
        boolean needSep = false;
        boolean printedAnything = false;
        pw.println("ACTIVITY MANAGER URI PERMISSIONS (dumpsys activity permissions)");
        if (this.mGrantedUriPermissions.size() > 0) {
            boolean printed = false;
            int dumpUid = -2;
            if (dumpPackage != null) {
                try {
                    dumpUid = this.mContext.getPackageManager().getPackageUidAsUser(dumpPackage, DumpState.DUMP_PREFERRED_XML, 0);
                } catch (NameNotFoundException e) {
                    dumpUid = -1;
                }
            }
            for (int i = 0; i < this.mGrantedUriPermissions.size(); i++) {
                int uid = this.mGrantedUriPermissions.keyAt(i);
                if (dumpUid < -1 || UserHandle.getAppId(uid) == dumpUid) {
                    ArrayMap<GrantUri, UriPermission> perms = (ArrayMap) this.mGrantedUriPermissions.valueAt(i);
                    if (!printed) {
                        if (needSep) {
                            pw.println();
                        }
                        needSep = true;
                        pw.println("  Granted Uri Permissions:");
                        printed = true;
                        printedAnything = true;
                    }
                    pw.print("  * UID ");
                    pw.print(uid);
                    pw.println(" holds:");
                    for (UriPermission perm : perms.values()) {
                        pw.print("    ");
                        pw.println(perm);
                        if (dumpAll) {
                            perm.dump(pw, "      ");
                        }
                    }
                }
            }
        }
        if (!printedAnything) {
            pw.println("  (nothing)");
        }
    }

    void dumpPendingIntentsLocked(FileDescriptor fd, PrintWriter pw, String[] args, int opti, boolean dumpAll, String dumpPackage) {
        boolean printed = false;
        pw.println("ACTIVITY MANAGER PENDING INTENTS (dumpsys activity intents)");
        if (this.mIntentSenderRecords.size() > 0) {
            for (WeakReference<PendingIntentRecord> ref : this.mIntentSenderRecords.values()) {
                PendingIntentRecord pendingIntentRecord = ref != null ? (PendingIntentRecord) ref.get() : null;
                if (dumpPackage == null || (pendingIntentRecord != null && dumpPackage.equals(pendingIntentRecord.key.packageName))) {
                    printed = true;
                    if (pendingIntentRecord != null) {
                        pw.print("  * ");
                        pw.println(pendingIntentRecord);
                        if (dumpAll) {
                            pendingIntentRecord.dump(pw, "    ");
                        }
                    } else {
                        pw.print("  * ");
                        pw.println(ref);
                    }
                }
            }
        }
        if (!printed) {
            pw.println("  (nothing)");
        }
    }

    private static final int dumpProcessList(PrintWriter pw, ActivityManagerService service, List list, String prefix, String normalLabel, String persistentLabel, String dumpPackage) {
        int numPers = 0;
        for (int i = list.size() - 1; i >= 0; i--) {
            ProcessRecord r = (ProcessRecord) list.get(i);
            if (dumpPackage == null || dumpPackage.equals(r.info.packageName)) {
                Object obj;
                String str = "%s%s #%2d: %s";
                Object[] objArr = new Object[4];
                objArr[0] = prefix;
                if (r.persistent) {
                    obj = persistentLabel;
                } else {
                    String str2 = normalLabel;
                }
                objArr[1] = obj;
                objArr[2] = Integer.valueOf(i);
                objArr[3] = r.toString();
                pw.println(String.format(str, objArr));
                if (r.persistent) {
                    numPers++;
                }
            }
        }
        return numPers;
    }

    private static final boolean dumpProcessOomList(PrintWriter pw, ActivityManagerService service, List<ProcessRecord> origList, String prefix, String normalLabel, String persistentLabel, boolean inclDetails, String dumpPackage) {
        int i;
        ArrayList<Pair<ProcessRecord, Integer>> list = new ArrayList(origList.size());
        for (i = 0; i < origList.size(); i++) {
            ProcessRecord r = (ProcessRecord) origList.get(i);
            if (dumpPackage == null || r.pkgList.containsKey(dumpPackage)) {
                list.add(new Pair((ProcessRecord) origList.get(i), Integer.valueOf(i)));
            }
        }
        if (list.size() <= 0) {
            return false;
        }
        Collections.sort(list, new Comparator<Pair<ProcessRecord, Integer>>() {
            public int compare(Pair<ProcessRecord, Integer> object1, Pair<ProcessRecord, Integer> object2) {
                int i = -1;
                if (((ProcessRecord) object1.first).setAdj != ((ProcessRecord) object2.first).setAdj) {
                    return ((ProcessRecord) object1.first).setAdj > ((ProcessRecord) object2.first).setAdj ? -1 : 1;
                } else if (((ProcessRecord) object1.first).setProcState != ((ProcessRecord) object2.first).setProcState) {
                    if (((ProcessRecord) object1.first).setProcState <= ((ProcessRecord) object2.first).setProcState) {
                        i = 1;
                    }
                    return i;
                } else if (((Integer) object1.second).intValue() == ((Integer) object2.second).intValue()) {
                    return 0;
                } else {
                    if (((Integer) object1.second).intValue() <= ((Integer) object2.second).intValue()) {
                        i = 1;
                    }
                    return i;
                }
            }
        });
        long curRealtime = SystemClock.elapsedRealtime();
        long realtimeSince = curRealtime - service.mLastPowerCheckRealtime;
        long uptimeSince = SystemClock.uptimeMillis() - service.mLastPowerCheckUptime;
        for (i = list.size() - 1; i >= 0; i--) {
            char schedGroup;
            char foreground;
            String str;
            r = (ProcessRecord) ((Pair) list.get(i)).first;
            String oomAdj = ProcessList.makeOomAdjString(r.setAdj);
            switch (r.setSchedGroup) {
                case 0:
                    schedGroup = 'B';
                    break;
                case 1:
                    schedGroup = 'F';
                    break;
                case 2:
                    schedGroup = 'T';
                    break;
                default:
                    schedGroup = '?';
                    break;
            }
            if (r.foregroundActivities) {
                foreground = 'A';
            } else if (r.foregroundServices) {
                foreground = 'S';
            } else {
                foreground = ' ';
            }
            String procState = ProcessList.makeProcStateString(r.curProcState);
            pw.print(prefix);
            if (r.persistent) {
                str = persistentLabel;
            } else {
                str = normalLabel;
            }
            pw.print(str);
            pw.print(" #");
            int num = (origList.size() - 1) - ((Integer) ((Pair) list.get(i)).second).intValue();
            if (num < 10) {
                pw.print(' ');
            }
            pw.print(num);
            pw.print(": ");
            pw.print(oomAdj);
            pw.print(' ');
            pw.print(schedGroup);
            pw.print('/');
            pw.print(foreground);
            pw.print('/');
            pw.print(procState);
            pw.print(" trm:");
            if (r.trimMemoryLevel < 10) {
                pw.print(' ');
            }
            pw.print(r.trimMemoryLevel);
            pw.print(' ');
            pw.print(r.toShortString());
            pw.print(" (");
            pw.print(r.adjType);
            pw.println(')');
            if (!(r.adjSource == null && r.adjTarget == null)) {
                pw.print(prefix);
                pw.print("    ");
                if (r.adjTarget instanceof ComponentName) {
                    pw.print(((ComponentName) r.adjTarget).flattenToShortString());
                } else if (r.adjTarget != null) {
                    pw.print(r.adjTarget.toString());
                } else {
                    pw.print("{null}");
                }
                pw.print("<=");
                if (r.adjSource instanceof ProcessRecord) {
                    pw.print("Proc{");
                    pw.print(((ProcessRecord) r.adjSource).toShortString());
                    pw.println("}");
                } else if (r.adjSource != null) {
                    pw.println(r.adjSource.toString());
                } else {
                    pw.println("{null}");
                }
            }
            if (inclDetails) {
                pw.print(prefix);
                pw.print("    ");
                pw.print("oom: max=");
                pw.print(r.maxAdj);
                pw.print(" curRaw=");
                pw.print(r.curRawAdj);
                pw.print(" setRaw=");
                pw.print(r.setRawAdj);
                pw.print(" cur=");
                pw.print(r.curAdj);
                pw.print(" set=");
                pw.println(r.setAdj);
                pw.print(prefix);
                pw.print("    ");
                pw.print("state: cur=");
                pw.print(ProcessList.makeProcStateString(r.curProcState));
                pw.print(" set=");
                pw.print(ProcessList.makeProcStateString(r.setProcState));
                pw.print(" lastPss=");
                DebugUtils.printSizeValue(pw, r.lastPss * 1024);
                pw.print(" lastSwapPss=");
                DebugUtils.printSizeValue(pw, r.lastSwapPss * 1024);
                pw.print(" lastCachedPss=");
                DebugUtils.printSizeValue(pw, r.lastCachedPss * 1024);
                pw.println();
                pw.print(prefix);
                pw.print("    ");
                pw.print("cached=");
                pw.print(r.cached);
                pw.print(" empty=");
                pw.print(r.empty);
                pw.print(" hasAboveClient=");
                pw.println(r.hasAboveClient);
                if (r.setProcState < 10) {
                    continue;
                } else {
                    long timeUsed;
                    if (r.lastWakeTime != 0) {
                        long wtime;
                        BatteryStatsImpl stats = service.mBatteryStatsService.getActiveStatistics();
                        synchronized (stats) {
                            wtime = stats.getProcessWakeTime(r.info.uid, r.pid, curRealtime);
                        }
                        timeUsed = wtime - r.lastWakeTime;
                        pw.print(prefix);
                        pw.print("    ");
                        pw.print("keep awake over ");
                        TimeUtils.formatDuration(realtimeSince, pw);
                        pw.print(" used ");
                        TimeUtils.formatDuration(timeUsed, pw);
                        pw.print(" (");
                        pw.print((100 * timeUsed) / realtimeSince);
                        pw.println("%)");
                    }
                    if (r.lastCpuTime != 0) {
                        timeUsed = r.curCpuTime - r.lastCpuTime;
                        pw.print(prefix);
                        pw.print("    ");
                        pw.print("run cpu over ");
                        TimeUtils.formatDuration(uptimeSince, pw);
                        pw.print(" used ");
                        TimeUtils.formatDuration(timeUsed, pw);
                        pw.print(" (");
                        pw.print((100 * timeUsed) / uptimeSince);
                        pw.println("%)");
                    }
                }
            }
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    ArrayList<ProcessRecord> collectProcesses(PrintWriter pw, int start, boolean allPkgs, String[] args) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ArrayList<ProcessRecord> procs;
                if (args == null || args.length <= start || args[start].charAt(0) == '-') {
                    procs = new ArrayList(this.mLruProcesses);
                } else {
                    procs = new ArrayList();
                    int pid = -1;
                    try {
                        pid = Integer.parseInt(args[start]);
                    } catch (NumberFormatException e) {
                    }
                    for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
                        ProcessRecord proc = (ProcessRecord) this.mLruProcesses.get(i);
                        if (proc.pid == pid) {
                            procs.add(proc);
                        } else if (allPkgs && proc.pkgList != null && proc.pkgList.containsKey(args[start])) {
                            procs.add(proc);
                        } else if (proc.processName.equals(args[start])) {
                            procs.add(proc);
                        }
                    }
                    if (procs.size() <= 0) {
                    }
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    final void dumpGraphicsHardwareUsage(FileDescriptor fd, PrintWriter pw, String[] args) {
        ArrayList<ProcessRecord> procs = collectProcesses(pw, 0, false, args);
        if (procs == null) {
            pw.println("No process found for: " + args[0]);
            return;
        }
        long uptime = SystemClock.uptimeMillis();
        long realtime = SystemClock.elapsedRealtime();
        pw.println("Applications Graphics Acceleration Info:");
        pw.println("Uptime: " + uptime + " Realtime: " + realtime);
        for (int i = procs.size() - 1; i >= 0; i--) {
            ProcessRecord r = (ProcessRecord) procs.get(i);
            if (r.thread != null) {
                pw.println("\n** Graphics info for pid " + r.pid + " [" + r.processName + "] **");
                pw.flush();
                try {
                    TransferPipe tp = new TransferPipe();
                    r.thread.dumpGfxInfo(tp.getWriteFd().getFileDescriptor(), args);
                    tp.go(fd);
                    tp.kill();
                } catch (IOException e) {
                    pw.println("Failure while dumping the app: " + r);
                    pw.flush();
                } catch (RemoteException e2) {
                    pw.println("Got a RemoteException while dumping the app " + r);
                    pw.flush();
                } catch (Throwable th) {
                    tp.kill();
                }
            }
        }
    }

    final void dumpDbInfo(FileDescriptor fd, PrintWriter pw, String[] args) {
        ArrayList<ProcessRecord> procs = collectProcesses(pw, 0, false, args);
        if (procs == null) {
            pw.println("No process found for: " + args[0]);
            return;
        }
        pw.println("Applications Database Info:");
        for (int i = procs.size() - 1; i >= 0; i--) {
            ProcessRecord r = (ProcessRecord) procs.get(i);
            if (r.thread != null) {
                pw.println("\n** Database info for pid " + r.pid + " [" + r.processName + "] **");
                pw.flush();
                try {
                    TransferPipe tp = new TransferPipe();
                    r.thread.dumpDbInfo(tp.getWriteFd().getFileDescriptor(), args);
                    tp.go(fd);
                    tp.kill();
                } catch (IOException e) {
                    pw.println("Failure while dumping the app: " + r);
                    pw.flush();
                } catch (RemoteException e2) {
                    pw.println("Got a RemoteException while dumping the app " + r);
                    pw.flush();
                } catch (Throwable th) {
                    tp.kill();
                }
            }
        }
    }

    static final void dumpMemItems(PrintWriter pw, String prefix, String tag, ArrayList<MemItem> items, boolean sort, boolean isCompact, boolean dumpSwapPss) {
        if (sort && !isCompact) {
            Collections.sort(items, new Comparator<MemItem>() {
                public int compare(MemItem lhs, MemItem rhs) {
                    if (lhs.pss < rhs.pss) {
                        return 1;
                    }
                    if (lhs.pss > rhs.pss) {
                        return -1;
                    }
                    return 0;
                }
            });
        }
        for (int i = 0; i < items.size(); i++) {
            MemItem mi = (MemItem) items.get(i);
            if (isCompact) {
                if (mi.isProc) {
                    pw.print("proc,");
                    pw.print(tag);
                    pw.print(",");
                    pw.print(mi.shortLabel);
                    pw.print(",");
                    pw.print(mi.id);
                    pw.print(",");
                    pw.print(mi.pss);
                    pw.print(",");
                    pw.print(dumpSwapPss ? Long.valueOf(mi.swapPss) : "N/A");
                    pw.println(mi.hasActivities ? ",a" : ",e");
                } else {
                    pw.print(tag);
                    pw.print(",");
                    pw.print(mi.shortLabel);
                    pw.print(",");
                    pw.print(mi.pss);
                    pw.print(",");
                    pw.println(dumpSwapPss ? Long.valueOf(mi.swapPss) : "N/A");
                }
            } else if (dumpSwapPss) {
                pw.printf("%s%s: %-60s (%s in swap)\n", new Object[]{prefix, stringifyKBSize(mi.pss), mi.label, stringifyKBSize(mi.swapPss)});
            } else {
                pw.printf("%s%s: %s\n", new Object[]{prefix, stringifyKBSize(mi.pss), mi.label});
            }
            if (mi.subitems != null) {
                dumpMemItems(pw, prefix + "    ", mi.shortLabel, mi.subitems, true, isCompact, dumpSwapPss);
            }
        }
    }

    static final void appendMemBucket(StringBuilder out, long memKB, String label, boolean stackLike) {
        int start = label.lastIndexOf(46);
        if (start >= 0) {
            start++;
        } else {
            start = 0;
        }
        int end = label.length();
        for (int i = 0; i < DUMP_MEM_BUCKETS.length; i++) {
            if (DUMP_MEM_BUCKETS[i] >= memKB) {
                out.append(DUMP_MEM_BUCKETS[i] / 1024);
                out.append(stackLike ? "MB." : "MB ");
                out.append(label, start, end);
                return;
            }
        }
        out.append(memKB / 1024);
        out.append(stackLike ? "MB." : "MB ");
        out.append(label, start, end);
    }

    private final void dumpApplicationMemoryUsageHeader(PrintWriter pw, long uptime, long realtime, boolean isCheckinRequest, boolean isCompact) {
        if (isCompact) {
            pw.print("version,");
            pw.println(1);
        }
        if (isCheckinRequest || isCompact) {
            pw.print("time,");
            pw.print(uptime);
            pw.print(",");
            pw.println(realtime);
            return;
        }
        pw.println("Applications Memory Usage (in Kilobytes):");
        pw.println("Uptime: " + uptime + " Realtime: " + realtime);
    }

    private final long[] getKsmInfo() {
        long[] longOut = new long[4];
        int[] SINGLE_LONG_FORMAT = new int[]{8224};
        long[] longTmp = new long[1];
        Process.readProcFile("/sys/kernel/mm/ksm/pages_shared", SINGLE_LONG_FORMAT, null, longTmp, null);
        longOut[0] = (longTmp[0] * 4096) / 1024;
        longTmp[0] = 0;
        Process.readProcFile("/sys/kernel/mm/ksm/pages_sharing", SINGLE_LONG_FORMAT, null, longTmp, null);
        longOut[1] = (longTmp[0] * 4096) / 1024;
        longTmp[0] = 0;
        Process.readProcFile("/sys/kernel/mm/ksm/pages_unshared", SINGLE_LONG_FORMAT, null, longTmp, null);
        longOut[2] = (longTmp[0] * 4096) / 1024;
        longTmp[0] = 0;
        Process.readProcFile("/sys/kernel/mm/ksm/pages_volatile", SINGLE_LONG_FORMAT, null, longTmp, null);
        longOut[3] = (longTmp[0] * 4096) / 1024;
        return longOut;
    }

    private static String stringifySize(long size, int order) {
        Locale locale = Locale.US;
        switch (order) {
            case 1:
                return String.format(locale, "%,13d", new Object[]{Long.valueOf(size)});
            case 1024:
                return String.format(locale, "%,9dK", new Object[]{Long.valueOf(size / 1024)});
            case DumpState.DUMP_DEXOPT /*1048576*/:
                return String.format(locale, "%,5dM", new Object[]{Long.valueOf((size / 1024) / 1024)});
            case 1073741824:
                return String.format(locale, "%,1dG", new Object[]{Long.valueOf(((size / 1024) / 1024) / 1024)});
            default:
                throw new IllegalArgumentException("Invalid size order");
        }
    }

    private static String stringifyKBSize(long size) {
        return stringifySize(1024 * size, 1024);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final void dumpApplicationMemoryUsage(FileDescriptor fd, PrintWriter pw, String prefix, String[] args, boolean brief, PrintWriter categoryPw) {
        Throwable th;
        boolean dumpDetails = false;
        boolean dumpFullDetails = false;
        boolean dumpDalvik = false;
        boolean dumpSummaryOnly = false;
        boolean dumpUnreachable = false;
        boolean oomOnly = false;
        boolean isCompact = false;
        boolean localOnly = false;
        boolean packages = false;
        boolean isCheckinRequest = false;
        boolean dumpSwapPss = false;
        int opti = 0;
        while (opti < args.length) {
            String opt = args[opti];
            if (opt == null || opt.length() <= 0 || opt.charAt(0) != '-') {
                break;
            }
            opti++;
            if ("-a".equals(opt)) {
                dumpDetails = true;
                dumpFullDetails = true;
                dumpDalvik = true;
                dumpSwapPss = true;
            } else if ("-d".equals(opt)) {
                dumpDalvik = true;
            } else if ("-c".equals(opt)) {
                isCompact = true;
            } else if ("-s".equals(opt)) {
                dumpDetails = true;
                dumpSummaryOnly = true;
            } else if ("-S".equals(opt)) {
                dumpSwapPss = true;
            } else if ("--unreachable".equals(opt)) {
                dumpUnreachable = true;
            } else if ("--oom".equals(opt)) {
                oomOnly = true;
            } else if ("--local".equals(opt)) {
                localOnly = true;
            } else if ("--package".equals(opt)) {
                packages = true;
            } else if ("--checkin".equals(opt)) {
                isCheckinRequest = true;
            } else if ("-h".equals(opt)) {
                pw.println("meminfo dump options: [-a] [-d] [-c] [-s] [--oom] [process]");
                pw.println("  -a: include all available information for each process.");
                pw.println("  -d: include dalvik details.");
                pw.println("  -c: dump in a compact machine-parseable representation.");
                pw.println("  -s: dump only summary of application memory usage.");
                pw.println("  -S: dump also SwapPss.");
                pw.println("  --oom: only show processes organized by oom adj.");
                pw.println("  --local: only collect details locally, don't call process.");
                pw.println("  --package: interpret process arg as package, dumping all");
                pw.println("             processes that have loaded that package.");
                pw.println("  --checkin: dump data for a checkin");
                pw.println("If [process] is specified it can be the name or ");
                pw.println("pid of a specific process to dump.");
                return;
            } else {
                pw.println("Unknown argument: " + opt + "; use -h for help");
            }
        }
        long uptime = SystemClock.uptimeMillis();
        long realtime = SystemClock.elapsedRealtime();
        long[] tmpLong = new long[1];
        ArrayList<ProcessRecord> procs = collectProcesses(pw, opti, packages, args);
        int N;
        int i;
        Stats st;
        MemoryInfo mi;
        if (procs == null) {
            if (!(args == null || args.length <= opti || args[opti].charAt(0) == '-')) {
                ArrayList<Stats> nativeProcs = new ArrayList();
                updateCpuStatsNow();
                int findPid = -1;
                try {
                    findPid = Integer.parseInt(args[opti]);
                } catch (NumberFormatException e) {
                }
                synchronized (this.mProcessCpuTracker) {
                    N = this.mProcessCpuTracker.countStats();
                    for (i = 0; i < N; i++) {
                        st = this.mProcessCpuTracker.getStats(i);
                        if (st.pid == findPid || (st.baseName != null && st.baseName.equals(args[opti]))) {
                            nativeProcs.add(st);
                        }
                    }
                }
                if (nativeProcs.size() > 0) {
                    dumpApplicationMemoryUsageHeader(pw, uptime, realtime, isCheckinRequest, isCompact);
                    mi = null;
                    for (i = nativeProcs.size() - 1; i >= 0; i--) {
                        Stats r = (Stats) nativeProcs.get(i);
                        int pid = r.pid;
                        if (!isCheckinRequest && dumpDetails) {
                            pw.println("\n** MEMINFO in pid " + pid + " [" + r.baseName + "] **");
                        }
                        if (mi == null) {
                            mi = new MemoryInfo();
                        }
                        if (dumpDetails || !(brief || oomOnly)) {
                            Debug.getMemoryInfo(pid, mi);
                        } else {
                            mi.dalvikPss = (int) Debug.getPss(pid, tmpLong, null);
                            mi.dalvikPrivateDirty = (int) tmpLong[0];
                        }
                        ActivityThread.dumpMemInfoTable(pw, mi, isCheckinRequest, dumpFullDetails, dumpDalvik, dumpSummaryOnly, pid, r.baseName, 0, 0, 0, 0, 0, 0);
                        if (isCheckinRequest) {
                            pw.println();
                        }
                    }
                    return;
                }
            }
            pw.println("No process found for: " + args[opti]);
            return;
        }
        long[] dalvikSubitemPss;
        long[] dalvikSubitemSwapPss;
        if (!(brief || oomOnly || (procs.size() != 1 && !isCheckinRequest && !packages))) {
            dumpDetails = true;
        }
        dumpApplicationMemoryUsageHeader(pw, uptime, realtime, isCheckinRequest, isCompact);
        Object innerArgs = new String[(args.length - opti)];
        System.arraycopy(args, opti, innerArgs, 0, args.length - opti);
        ArrayList<MemItem> procMems = new ArrayList();
        SparseArray<MemItem> procMemsMap = new SparseArray();
        long nativePss = 0;
        long nativeSwapPss = 0;
        long dalvikPss = 0;
        long dalvikSwapPss = 0;
        if (dumpDalvik) {
            dalvikSubitemPss = new long[8];
        } else {
            dalvikSubitemPss = EmptyArray.LONG;
        }
        if (dumpDalvik) {
            dalvikSubitemSwapPss = new long[8];
        } else {
            dalvikSubitemSwapPss = EmptyArray.LONG;
        }
        long otherPss = 0;
        long otherSwapPss = 0;
        long[] miscPss = new long[17];
        long[] miscSwapPss = new long[17];
        long[] oomPss = new long[DUMP_MEM_OOM_LABEL.length];
        long[] oomSwapPss = new long[DUMP_MEM_OOM_LABEL.length];
        ArrayList<MemItem>[] oomProcs = new ArrayList[DUMP_MEM_OOM_LABEL.length];
        long totalPss = 0;
        long totalSwapPss = 0;
        long cachedPss = 0;
        long cachedSwapPss = 0;
        boolean hasSwapPss = false;
        mi = null;
        i = procs.size() - 1;
        while (i >= 0) {
            ProcessRecord r2 = (ProcessRecord) procs.get(i);
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    IApplicationThread iApplicationThread = r2.thread;
                    pid = r2.pid;
                    int oomAdj = r2.getSetAdjWithServices();
                    boolean z = r2.activities.size() > 0;
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        }
        long nativeProcTotalPss = 0;
        if (!(isCheckinRequest || procs.size() <= 1 || packages)) {
            updateCpuStatsNow();
            synchronized (this.mProcessCpuTracker) {
                N = this.mProcessCpuTracker.countStats();
                i = 0;
                MemoryInfo mi2 = null;
                while (i < N) {
                    try {
                        st = this.mProcessCpuTracker.getStats(i);
                        if (st.vsize > 0) {
                            if (procMemsMap.indexOfKey(st.pid) < 0) {
                                int j;
                                if (mi2 == null) {
                                    mi = new MemoryInfo();
                                } else {
                                    mi = mi2;
                                }
                                if (brief || oomOnly) {
                                    try {
                                        mi.nativePss = (int) Debug.getPss(st.pid, tmpLong, null);
                                        mi.nativePrivateDirty = (int) tmpLong[0];
                                    } catch (Throwable th2) {
                                        th = th2;
                                    }
                                } else {
                                    Debug.getMemoryInfo(st.pid, mi);
                                }
                                long myTotalPss = (long) mi.getTotalPss();
                                long myTotalSwapPss = (long) mi.getTotalSwappedOutPss();
                                totalPss += myTotalPss;
                                nativeProcTotalPss += myTotalPss;
                                MemItem memItem = new MemItem(st.name + " (pid " + st.pid + ")", st.name, myTotalPss, (long) mi.getSummaryTotalSwapPss(), st.pid, false);
                                procMems.add(memItem);
                                nativePss += (long) mi.nativePss;
                                nativeSwapPss += (long) mi.nativeSwappedOutPss;
                                dalvikPss += (long) mi.dalvikPss;
                                dalvikSwapPss += (long) mi.dalvikSwappedOutPss;
                                for (j = 0; j < dalvikSubitemPss.length; j++) {
                                    dalvikSubitemPss[j] = dalvikSubitemPss[j] + ((long) mi.getOtherPss(j + 17));
                                    dalvikSubitemSwapPss[j] = dalvikSubitemSwapPss[j] + ((long) mi.getOtherSwappedOutPss(j + 17));
                                }
                                otherPss += (long) mi.otherPss;
                                otherSwapPss += (long) mi.otherSwappedOutPss;
                                for (j = 0; j < 17; j++) {
                                    long mem = (long) mi.getOtherPss(j);
                                    miscPss[j] = miscPss[j] + mem;
                                    otherPss -= mem;
                                    mem = (long) mi.getOtherSwappedOutPss(j);
                                    miscSwapPss[j] = miscSwapPss[j] + mem;
                                    otherSwapPss -= mem;
                                }
                                oomPss[0] = oomPss[0] + myTotalPss;
                                oomSwapPss[0] = oomSwapPss[0] + myTotalSwapPss;
                                if (oomProcs[0] == null) {
                                    oomProcs[0] = new ArrayList();
                                }
                                oomProcs[0].add(memItem);
                                i++;
                                mi2 = mi;
                            }
                        }
                        mi = mi2;
                        i++;
                        mi2 = mi;
                    } catch (Throwable th3) {
                        th = th3;
                        mi = mi2;
                    }
                }
            }
        }
        return;
        throw th;
    }

    private void appendBasicMemEntry(StringBuilder sb, int oomAdj, int procState, long pss, long memtrack, String name) {
        sb.append("  ");
        sb.append(ProcessList.makeOomAdjString(oomAdj));
        sb.append(' ');
        sb.append(ProcessList.makeProcStateString(procState));
        sb.append(' ');
        ProcessList.appendRamKb(sb, pss);
        sb.append(": ");
        sb.append(name);
        if (memtrack > 0) {
            sb.append(" (");
            sb.append(stringifyKBSize(memtrack));
            sb.append(" memtrack)");
        }
    }

    private void appendMemInfo(StringBuilder sb, ProcessMemInfo mi) {
        appendBasicMemEntry(sb, mi.oomAdj, mi.procState, mi.pss, mi.memtrack, mi.name);
        sb.append(" (pid ");
        sb.append(mi.pid);
        sb.append(") ");
        sb.append(mi.adjType);
        sb.append('\n');
        if (mi.adjReason != null) {
            sb.append("                      ");
            sb.append(mi.adjReason);
            sb.append('\n');
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void reportMemUsage(ArrayList<ProcessMemInfo> memInfos) {
        int i;
        Writer catSw;
        long now;
        SparseArray<ProcessMemInfo> sparseArray = new SparseArray(memInfos.size());
        int N = memInfos.size();
        for (i = 0; i < N; i++) {
            ProcessMemInfo mi = (ProcessMemInfo) memInfos.get(i);
            sparseArray.put(mi.pid, mi);
        }
        updateCpuStatsNow();
        long[] memtrackTmp = new long[1];
        synchronized (this.mProcessCpuTracker) {
            N = this.mProcessCpuTracker.countStats();
            for (i = 0; i < N; i++) {
                Stats st = this.mProcessCpuTracker.getStats(i);
                if (st.vsize > 0) {
                    long pss = Debug.getPss(st.pid, null, memtrackTmp);
                    if (pss > 0) {
                        if (sparseArray.indexOfKey(st.pid) < 0) {
                            mi = new ProcessMemInfo(st.name, st.pid, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE, -1, "native", null);
                            mi.pss = pss;
                            mi.memtrack = memtrackTmp[0];
                            memInfos.add(mi);
                        }
                    }
                }
            }
        }
        long totalPss = 0;
        long totalMemtrack = 0;
        N = memInfos.size();
        for (i = 0; i < N; i++) {
            mi = (ProcessMemInfo) memInfos.get(i);
            if (mi.pss == 0) {
                mi.pss = Debug.getPss(mi.pid, null, memtrackTmp);
                mi.memtrack = memtrackTmp[0];
            }
            totalPss += mi.pss;
            totalMemtrack += mi.memtrack;
        }
        Collections.sort(memInfos, new Comparator<ProcessMemInfo>() {
            public int compare(ProcessMemInfo lhs, ProcessMemInfo rhs) {
                int i = 1;
                int i2 = -1;
                if (lhs.oomAdj != rhs.oomAdj) {
                    if (lhs.oomAdj >= rhs.oomAdj) {
                        i2 = 1;
                    }
                    return i2;
                } else if (lhs.pss == rhs.pss) {
                    return 0;
                } else {
                    if (lhs.pss >= rhs.pss) {
                        i = -1;
                    }
                    return i;
                }
            }
        });
        StringBuilder stringBuilder = new StringBuilder(128);
        stringBuilder = new StringBuilder(128);
        stringBuilder.append("Low on memory -- ");
        appendMemBucket(stringBuilder, totalPss, "total", false);
        appendMemBucket(stringBuilder, totalPss, "total", true);
        stringBuilder = new StringBuilder(1024);
        StringBuilder shortNativeBuilder = new StringBuilder(1024);
        stringBuilder = new StringBuilder(1024);
        boolean firstLine = true;
        int lastOomAdj = Integer.MIN_VALUE;
        long extraNativeRam = 0;
        long extraNativeMemtrack = 0;
        long cachedPss = 0;
        i = 0;
        N = memInfos.size();
        while (i < N) {
            mi = (ProcessMemInfo) memInfos.get(i);
            if (mi.oomAdj >= 900) {
                cachedPss += mi.pss;
            }
            if (mi.oomAdj != JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE && (mi.oomAdj < 500 || mi.oomAdj == 600 || mi.oomAdj == 700)) {
                if (lastOomAdj != mi.oomAdj) {
                    lastOomAdj = mi.oomAdj;
                    if (mi.oomAdj <= 0) {
                        stringBuilder.append(" / ");
                    }
                    if (mi.oomAdj >= 0) {
                        if (firstLine) {
                            stringBuilder.append(":");
                            firstLine = false;
                        }
                        stringBuilder.append("\n\t at ");
                    } else {
                        stringBuilder.append("$");
                    }
                } else {
                    stringBuilder.append(" ");
                    stringBuilder.append("$");
                }
                if (mi.oomAdj <= 0) {
                    appendMemBucket(stringBuilder, mi.pss, mi.name, false);
                }
                appendMemBucket(stringBuilder, mi.pss, mi.name, true);
                if (mi.oomAdj >= 0) {
                    if (i + 1 < N) {
                    }
                    stringBuilder.append("(");
                    for (int k = 0; k < DUMP_MEM_OOM_ADJ.length; k++) {
                        if (DUMP_MEM_OOM_ADJ[k] == mi.oomAdj) {
                            stringBuilder.append(DUMP_MEM_OOM_LABEL[k]);
                            stringBuilder.append(":");
                            stringBuilder.append(DUMP_MEM_OOM_ADJ[k]);
                        }
                    }
                    stringBuilder.append(")");
                }
            }
            appendMemInfo(stringBuilder, mi);
            if (mi.oomAdj != JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE) {
                if (extraNativeRam > 0) {
                    appendBasicMemEntry(shortNativeBuilder, JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE, -1, extraNativeRam, extraNativeMemtrack, "(Other native)");
                    shortNativeBuilder.append('\n');
                    extraNativeRam = 0;
                }
                appendMemInfo(stringBuilder, mi);
            } else if (mi.pss >= 512) {
                appendMemInfo(shortNativeBuilder, mi);
            } else {
                extraNativeRam += mi.pss;
                extraNativeMemtrack += mi.memtrack;
            }
            i++;
        }
        stringBuilder.append("           ");
        ProcessList.appendRamKb(stringBuilder, totalPss);
        stringBuilder.append(": TOTAL");
        if (totalMemtrack > 0) {
            stringBuilder.append(" (");
            stringBuilder.append(stringifyKBSize(totalMemtrack));
            stringBuilder.append(" memtrack)");
        }
        stringBuilder.append("\n");
        MemInfoReader memInfo = new MemInfoReader();
        memInfo.readMemInfo();
        long[] infos = memInfo.getRawInfo();
        stringBuilder = new StringBuilder(1024);
        Debug.getMemInfo(infos);
        stringBuilder.append("  MemInfo: ");
        stringBuilder.append(stringifyKBSize(infos[5])).append(" slab, ");
        stringBuilder.append(stringifyKBSize(infos[4])).append(" shmem, ");
        stringBuilder.append(stringifyKBSize(infos[10])).append(" vm alloc, ");
        stringBuilder.append(stringifyKBSize(infos[11])).append(" page tables ");
        stringBuilder.append(stringifyKBSize(infos[12])).append(" kernel stack\n");
        stringBuilder.append("           ");
        stringBuilder.append(stringifyKBSize(infos[2])).append(" buffers, ");
        stringBuilder.append(stringifyKBSize(infos[3])).append(" cached, ");
        stringBuilder.append(stringifyKBSize(infos[9])).append(" mapped, ");
        stringBuilder.append(stringifyKBSize(infos[1])).append(" free\n");
        if (infos[8] != 0) {
            stringBuilder.append("  ZRAM: ");
            stringBuilder.append(stringifyKBSize(infos[8]));
            stringBuilder.append(" RAM, ");
            stringBuilder.append(stringifyKBSize(infos[6]));
            stringBuilder.append(" swap total, ");
            stringBuilder.append(stringifyKBSize(infos[7]));
            stringBuilder.append(" swap free\n");
        }
        long[] ksm = getKsmInfo();
        if (ksm[1] == 0 && ksm[0] == 0 && ksm[2] == 0) {
            if (ksm[3] != 0) {
            }
            stringBuilder.append("  Free RAM: ");
            stringBuilder.append(stringifyKBSize((memInfo.getCachedSizeKb() + cachedPss) + memInfo.getFreeSizeKb()));
            stringBuilder.append("\n");
            stringBuilder.append("  Used RAM: ");
            stringBuilder.append(stringifyKBSize((totalPss - cachedPss) + memInfo.getKernelUsedSizeKb()));
            stringBuilder.append("\n");
            stringBuilder.append("  Lost RAM: ");
            stringBuilder.append(stringifyKBSize(((((memInfo.getTotalSizeKb() - totalPss) - memInfo.getFreeSizeKb()) - memInfo.getCachedSizeKb()) - memInfo.getKernelUsedSizeKb()) - memInfo.getZramTotalSizeKb()));
            stringBuilder.append("\n");
            Slog.i(TAG, "Low on memory:");
            Slog.i(TAG, shortNativeBuilder.toString());
            Slog.i(TAG, stringBuilder.toString());
            Slog.i(TAG, stringBuilder.toString());
            stringBuilder = new StringBuilder(1024);
            stringBuilder.append("Low on memory:");
            stringBuilder.append(stringBuilder);
            stringBuilder.append('\n');
            stringBuilder.append(stringBuilder);
            stringBuilder.append(stringBuilder);
            stringBuilder.append('\n');
            stringBuilder.append(stringBuilder);
            stringBuilder.append('\n');
            catSw = new StringWriter();
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    PrintWriter fastPrintWriter = new FastPrintWriter(catSw, false, 256);
                    String[] emptyArgs = new String[0];
                    fastPrintWriter.println();
                    dumpProcessesLocked(null, fastPrintWriter, emptyArgs, 0, false, null);
                    fastPrintWriter.println();
                    this.mServices.newServiceDumperLocked(null, fastPrintWriter, emptyArgs, 0, false, null).dumpLocked();
                    fastPrintWriter.println();
                    dumpActivitiesLocked(null, fastPrintWriter, emptyArgs, 0, false, false, null);
                    fastPrintWriter.flush();
                    stringBuilder.append(catSw.toString());
                    addErrorToDropBox("lowmem", null, "system_server", null, null, stringBuilder.toString(), stringBuilder.toString(), null, null);
                    synchronized (this) {
                        try {
                            boostPriorityForLockedSection();
                            now = SystemClock.uptimeMillis();
                            if (this.mLastMemUsageReportTime < now) {
                                this.mLastMemUsageReportTime = now;
                            }
                        } finally {
                            resetPriorityAfterLockedSection();
                        }
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        }
        stringBuilder.append("  KSM: ");
        stringBuilder.append(stringifyKBSize(ksm[1]));
        stringBuilder.append(" saved from shared ");
        stringBuilder.append(stringifyKBSize(ksm[0]));
        stringBuilder.append("\n       ");
        stringBuilder.append(stringifyKBSize(ksm[2]));
        stringBuilder.append(" unshared; ");
        stringBuilder.append(stringifyKBSize(ksm[3]));
        stringBuilder.append(" volatile\n");
        stringBuilder.append("  Free RAM: ");
        stringBuilder.append(stringifyKBSize((memInfo.getCachedSizeKb() + cachedPss) + memInfo.getFreeSizeKb()));
        stringBuilder.append("\n");
        stringBuilder.append("  Used RAM: ");
        stringBuilder.append(stringifyKBSize((totalPss - cachedPss) + memInfo.getKernelUsedSizeKb()));
        stringBuilder.append("\n");
        stringBuilder.append("  Lost RAM: ");
        stringBuilder.append(stringifyKBSize(((((memInfo.getTotalSizeKb() - totalPss) - memInfo.getFreeSizeKb()) - memInfo.getCachedSizeKb()) - memInfo.getKernelUsedSizeKb()) - memInfo.getZramTotalSizeKb()));
        stringBuilder.append("\n");
        Slog.i(TAG, "Low on memory:");
        Slog.i(TAG, shortNativeBuilder.toString());
        Slog.i(TAG, stringBuilder.toString());
        Slog.i(TAG, stringBuilder.toString());
        stringBuilder = new StringBuilder(1024);
        stringBuilder.append("Low on memory:");
        stringBuilder.append(stringBuilder);
        stringBuilder.append('\n');
        stringBuilder.append(stringBuilder);
        stringBuilder.append(stringBuilder);
        stringBuilder.append('\n');
        stringBuilder.append(stringBuilder);
        stringBuilder.append('\n');
        catSw = new StringWriter();
        synchronized (this) {
            boostPriorityForLockedSection();
            PrintWriter fastPrintWriter2 = new FastPrintWriter(catSw, false, 256);
            String[] emptyArgs2 = new String[0];
            fastPrintWriter2.println();
            dumpProcessesLocked(null, fastPrintWriter2, emptyArgs2, 0, false, null);
            fastPrintWriter2.println();
            this.mServices.newServiceDumperLocked(null, fastPrintWriter2, emptyArgs2, 0, false, null).dumpLocked();
            fastPrintWriter2.println();
            dumpActivitiesLocked(null, fastPrintWriter2, emptyArgs2, 0, false, false, null);
            fastPrintWriter2.flush();
            stringBuilder.append(catSw.toString());
            addErrorToDropBox("lowmem", null, "system_server", null, null, stringBuilder.toString(), stringBuilder.toString(), null, null);
            synchronized (this) {
                boostPriorityForLockedSection();
                now = SystemClock.uptimeMillis();
                if (this.mLastMemUsageReportTime < now) {
                    this.mLastMemUsageReportTime = now;
                }
            }
        }
    }

    private static boolean scanArgs(String[] args, String value) {
        if (args != null) {
            for (String arg : args) {
                if (value.equals(arg)) {
                    return true;
                }
            }
        }
        return false;
    }

    final boolean removeDyingProviderLocked(ProcessRecord proc, ContentProviderRecord cpr, boolean always) {
        boolean inLaunching = this.mLaunchingProviders.contains(cpr);
        if (!inLaunching || always) {
            synchronized (cpr) {
                cpr.launchingApp = null;
                cpr.notifyAll();
            }
            ProviderMap providerMap = cpr.info.applicationInfo.euid != 0 ? this.mProviderMapForClone : this.mProviderMap;
            providerMap.removeProviderByClass(cpr.name, UserHandle.getUserId(cpr.uid));
            String[] names = cpr.info.authority.split(";");
            for (String removeProviderByName : names) {
                providerMap.removeProviderByName(removeProviderByName, UserHandle.getUserId(cpr.uid));
            }
        }
        for (int i = cpr.connections.size() - 1; i >= 0; i--) {
            ContentProviderConnection conn = (ContentProviderConnection) cpr.connections.get(i);
            if (!conn.waiting || !inLaunching || always) {
                ProcessRecord capp = conn.client;
                conn.dead = true;
                if (conn.stableCount > 0) {
                    if (!(capp == this.mHomeProcess || capp.persistent || capp.thread == null || capp.pid == 0 || capp.pid == MY_PID)) {
                        capp.kill("depends on provider " + cpr.name.flattenToShortString() + " in dying proc " + (proc != null ? proc.processName : "??") + " (adj " + (proc != null ? Integer.valueOf(proc.setAdj) : "??") + ")", true);
                    }
                } else if (!(capp.thread == null || conn.provider.provider == null)) {
                    try {
                        capp.thread.unstableProviderDied(conn.provider.provider.asBinder());
                    } catch (RemoteException e) {
                    }
                    cpr.connections.remove(i);
                    if (conn.client.conProviders.remove(conn)) {
                        stopAssociationLocked(capp.uid, capp.processName, cpr.uid, cpr.name);
                    }
                    if (!(proc == null || proc.uid < 10000 || capp.pid == proc.pid || capp.info == null || proc.info == null || capp.info.packageName == null || capp.info.packageName.equals(proc.info.packageName))) {
                        LogPower.push(167, proc.processName, Integer.toString(capp.pid), Integer.toString(proc.pid), new String[]{"provider"});
                    }
                }
            }
        }
        if (inLaunching && always) {
            this.mLaunchingProviders.remove(cpr);
        }
        return inLaunching;
    }

    protected boolean cleanUpApplicationRecordLocked(ProcessRecord app, boolean restarting, boolean allowRestart, int index, boolean replacingPid) {
        int i;
        Slog.d(TAG, "cleanUpApplicationRecord -- " + app.pid);
        if (index >= 0) {
            removeLruProcessLocked(app);
            ProcessList.remove(app.pid);
        }
        this.mProcessesToGc.remove(app);
        this.mPendingPssProcesses.remove(app);
        if (!(app.crashDialog == null || app.forceCrashReport)) {
            app.crashDialog.dismiss();
            app.crashDialog = null;
        }
        if (app.anrDialog != null) {
            app.anrDialog.dismiss();
            app.anrDialog = null;
        }
        if (app.waitDialog != null) {
            app.waitDialog.dismiss();
            app.waitDialog = null;
        }
        app.crashing = false;
        app.notResponding = false;
        app.resetPackageList(this.mProcessStats);
        app.unlinkDeathRecipient();
        app.makeInactive(this.mProcessStats);
        app.waitingToKill = null;
        app.forcingToForeground = null;
        updateProcessForegroundLocked(app, false, false);
        app.foregroundActivities = false;
        app.hasShownUi = false;
        app.treatLikeActivity = false;
        app.hasAboveClient = false;
        app.hasClientActivities = false;
        this.mServices.killServicesLocked(app, allowRestart);
        boolean restart = false;
        for (i = app.pubProviders.size() - 1; i >= 0; i--) {
            ContentProviderRecord cpr = (ContentProviderRecord) app.pubProviders.valueAt(i);
            boolean always = app.bad || !allowRestart;
            if ((removeDyingProviderLocked(app, cpr, always) || always) && cpr.hasConnectionOrHandle()) {
                restart = true;
            }
            cpr.provider = null;
            cpr.proc = null;
        }
        app.pubProviders.clear();
        if (cleanupAppInLaunchingProvidersLocked(app, false)) {
            restart = true;
        }
        if (!app.conProviders.isEmpty()) {
            for (i = app.conProviders.size() - 1; i >= 0; i--) {
                ContentProviderConnection conn = (ContentProviderConnection) app.conProviders.get(i);
                conn.provider.connections.remove(conn);
                stopAssociationLocked(app.uid, app.processName, conn.provider.uid, conn.provider.name);
                if (!(conn.provider.proc == null || conn.provider.proc.uid < 10000 || app.pid == conn.provider.proc.pid || app.info == null || conn.provider.proc.info == null || app.info.packageName == null || app.info.packageName.equals(conn.provider.proc.info.packageName))) {
                    LogPower.push(167, conn.provider.proc.processName, Integer.toString(app.pid), Integer.toString(conn.provider.proc.pid), new String[]{"provider"});
                }
            }
            app.conProviders.clear();
        }
        unregisterCtrlSocketForMm(app.processName);
        skipCurrentReceiverLocked(app);
        if (!app.killedByAm) {
            cleanupBroadcastLocked(app);
            cleanupAlarmLocked(app);
        }
        for (i = app.receivers.size() - 1; i >= 0; i--) {
            removeReceiverLocked((ReceiverList) app.receivers.valueAt(i));
        }
        app.receivers.clear();
        if (this.mBackupTarget != null && app.pid == this.mBackupTarget.app.pid) {
            if (ActivityManagerDebugConfig.DEBUG_BACKUP || ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                Slog.d(TAG_CLEANUP, "App " + this.mBackupTarget.appInfo + " died during backup");
            }
            try {
                IBackupManager.Stub.asInterface(ServiceManager.getService("backup")).agentDisconnected(app.info.packageName);
            } catch (RemoteException e) {
            }
        }
        for (i = this.mPendingProcessChanges.size() - 1; i >= 0; i--) {
            ProcessChangeItem item = (ProcessChangeItem) this.mPendingProcessChanges.get(i);
            if (item.pid == app.pid) {
                this.mPendingProcessChanges.remove(i);
                this.mAvailProcessChanges.add(item);
            }
        }
        this.mUiHandler.obtainMessage(32, app.pid, app.info.uid, null).sendToTarget();
        if (restarting) {
            return false;
        }
        Boolean persistent_org = Boolean.valueOf(app.persistent);
        if (APP_AUTO_START) {
            this.mIsAppAutoStart = -1;
            if (this.mPerfHub == null) {
                this.mPerfHub = new PerfHub();
            }
            this.mIsAppAutoStart = this.mPerfHub.perfEvent(4098, "pkg_name=" + app.processName, new int[0]);
            if (this.mIsAppAutoStart == 0) {
                app.persistent = true;
                Slog.v(TAG, " handle died app=" + app.processName + ",removed=" + app.removed + ", crashing=" + app.crashing);
            }
        }
        if (!app.persistent || app.isolated) {
            if (ActivityManagerDebugConfig.DEBUG_PROCESSES || ActivityManagerDebugConfig.DEBUG_CLEANUP) {
                Slog.v(TAG_CLEANUP, "Removing non-persistent process during cleanup: " + app);
            }
            if (!replacingPid) {
                removeProcessNameLocked(app.processName, app.uid + app.info.euid);
            }
            if (this.mHeavyWeightProcess == app) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(25, this.mHeavyWeightProcess.userId, 0));
                this.mHeavyWeightProcess = null;
            }
        } else if (!app.removed && this.mPersistentStartingProcesses.indexOf(app) < 0) {
            this.mPersistentStartingProcesses.add(app);
            restart = true;
        }
        if ((ActivityManagerDebugConfig.DEBUG_PROCESSES || ActivityManagerDebugConfig.DEBUG_CLEANUP) && this.mProcessesOnHold.contains(app)) {
            Slog.v(TAG_CLEANUP, "Clean-up removing on hold: " + app);
        }
        this.mProcessesOnHold.remove(app);
        if (app == this.mHomeProcess) {
            this.mHomeProcess = null;
            reportHomeProcess(this.mHomeProcess);
        }
        if (app == this.mPreviousProcess) {
            this.mPreviousProcess = null;
        }
        Flog.i(100, "cleanUpApplicationRecordLocked, pid: " + app.pid + ", restart: " + restart);
        if (!isAcquireAppResourceLocked(app)) {
            restart = false;
            clearAppAndAppServiceResource(app);
            for (i = this.mLaunchingProviders.size() - 1; i >= 0; i--) {
                cpr = (ContentProviderRecord) this.mLaunchingProviders.get(i);
                if (cpr.launchingApp == app) {
                    Slog.i(TAG, "the host process of " + cpr + " do not restart anymore and " + cpr.connections.size() + " clients waiting for it, notify them to release the Binder!");
                    removeDyingProviderLocked(app, cpr, true);
                }
            }
        }
        if ((!restart || app.isolated) && this.mIsAppAutoStart != 0) {
            if (app.pid > 0 && app.pid != MY_PID) {
                synchronized (this.mPidsSelfLocked) {
                    this.mPidsSelfLocked.remove(app.pid);
                    this.mHandler.removeMessages(20, app);
                }
                this.mBatteryStatsService.noteProcessFinish(app.processName, app.info.uid);
                if (app.isolated) {
                    this.mBatteryStatsService.removeIsolatedUid(app.uid, app.info.uid);
                }
                Flog.i(100, "cleanUpApplicationRecordLocked, reset pid: " + app.pid + ", euid: " + app.info.euid);
                app.setPid(0);
            }
            if (APP_AUTO_START) {
                app.persistent = persistent_org.booleanValue();
            }
            return false;
        }
        if (index < 0) {
            ProcessList.remove(app.pid);
        }
        addProcessNameLocked(app);
        startProcessLocked(app, "restart", app.processName);
        if (APP_AUTO_START) {
            app.persistent = persistent_org.booleanValue();
        }
        return true;
    }

    boolean checkAppInLaunchingProvidersLocked(ProcessRecord app) {
        for (int i = this.mLaunchingProviders.size() - 1; i >= 0; i--) {
            if (((ContentProviderRecord) this.mLaunchingProviders.get(i)).launchingApp == app) {
                return true;
            }
        }
        return false;
    }

    private void cleanupAlarmLocked(ProcessRecord process) {
        if (!isThirdParty(process)) {
            return;
        }
        if (this.mAlms == null) {
            Log.w(TAG, "Could not get instance of AlarmManagerService.");
            return;
        }
        ArrayList<String> array = new ArrayList();
        for (String pkg : process.pkgList.keySet()) {
            array.add(pkg);
        }
        if (array.size() > 0) {
            this.mAlms.cleanupAlarmLocked(array);
        }
    }

    private static boolean isThirdParty(ProcessRecord process) {
        if (process == null || process.pid == MY_PID || (process.info.flags & 1) != 0) {
            return false;
        }
        return true;
    }

    boolean cleanupAppInLaunchingProvidersLocked(ProcessRecord app, boolean alwaysBad) {
        boolean restart = false;
        for (int i = this.mLaunchingProviders.size() - 1; i >= 0; i--) {
            ContentProviderRecord cpr = (ContentProviderRecord) this.mLaunchingProviders.get(i);
            if (cpr.launchingApp == app) {
                if (alwaysBad || app.bad || !cpr.hasConnectionOrHandle()) {
                    removeDyingProviderLocked(app, cpr, true);
                } else {
                    restart = true;
                }
            }
        }
        return restart;
    }

    public List<RunningServiceInfo> getServices(int maxNum, int flags) {
        List<RunningServiceInfo> runningServiceInfoLocked;
        enforceNotIsolatedCaller("getServices");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                runningServiceInfoLocked = this.mServices.getRunningServiceInfoLocked(maxNum, flags);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return runningServiceInfoLocked;
    }

    public PendingIntent getRunningServiceControlPanel(ComponentName name) {
        PendingIntent runningServiceControlPanelLocked;
        enforceNotIsolatedCaller("getRunningServiceControlPanel");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                runningServiceControlPanelLocked = this.mServices.getRunningServiceControlPanelLocked(name);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return runningServiceControlPanelLocked;
    }

    public ComponentName startService(IApplicationThread caller, Intent service, String resolvedType, String callingPackage, int userId) throws TransactionTooLargeException {
        enforceNotIsolatedCaller("startService");
        if (service != null && service.hasFileDescriptors()) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        } else if (callingPackage == null) {
            throw new IllegalArgumentException("callingPackage cannot be null");
        } else {
            ComponentName res;
            if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                Slog.v(TAG_SERVICE, "startService: " + service + " type=" + resolvedType);
            }
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    int callingPid = Binder.getCallingPid();
                    int callingUid = Binder.getCallingUid();
                    long origId = Binder.clearCallingIdentity();
                    res = this.mServices.startServiceLocked(caller, service, resolvedType, callingPid, callingUid, callingPackage, userId);
                    Binder.restoreCallingIdentity(origId);
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return res;
        }
    }

    ComponentName startServiceInPackage(int uid, Intent service, String resolvedType, String callingPackage, int userId) throws TransactionTooLargeException {
        ComponentName res;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if (ActivityManagerDebugConfig.DEBUG_SERVICE) {
                    Slog.v(TAG_SERVICE, "startServiceInPackage: " + service + " type=" + resolvedType);
                }
                long origId = Binder.clearCallingIdentity();
                res = this.mServices.startServiceLocked(null, service, resolvedType, -1, uid, callingPackage, userId);
                Binder.restoreCallingIdentity(origId);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return res;
    }

    public int stopService(IApplicationThread caller, Intent service, String resolvedType, int userId) {
        enforceNotIsolatedCaller("stopService");
        if (service == null || !service.hasFileDescriptors()) {
            int stopServiceLocked;
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    stopServiceLocked = this.mServices.stopServiceLocked(caller, service, resolvedType, userId);
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return stopServiceLocked;
        }
        throw new IllegalArgumentException("File descriptors passed in Intent");
    }

    public IBinder peekService(Intent service, String resolvedType, String callingPackage) {
        enforceNotIsolatedCaller("peekService");
        if (service != null && service.hasFileDescriptors()) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        } else if (callingPackage == null) {
            throw new IllegalArgumentException("callingPackage cannot be null");
        } else {
            IBinder peekServiceLocked;
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    peekServiceLocked = this.mServices.peekServiceLocked(service, resolvedType, callingPackage);
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return peekServiceLocked;
        }
    }

    public boolean stopServiceToken(ComponentName className, IBinder token, int startId) {
        boolean stopServiceTokenLocked;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                stopServiceTokenLocked = this.mServices.stopServiceTokenLocked(className, token, startId);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return stopServiceTokenLocked;
    }

    public void setServiceForeground(ComponentName className, IBinder token, int id, Notification notification, int flags) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mServices.setServiceForegroundLocked(className, token, id, notification, flags);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public int handleIncomingUser(int callingPid, int callingUid, int userId, boolean allowAll, boolean requireFull, String name, String callerPackage) {
        return this.mUserController.handleIncomingUser(callingPid, callingUid, userId, allowAll, requireFull ? 2 : 0, name, callerPackage);
    }

    boolean isSingleton(String componentProcessName, ApplicationInfo aInfo, String className, int flags) {
        boolean result = false;
        if (UserHandle.getAppId(aInfo.uid) >= 10000) {
            if ((flags & 1073741824) != 0) {
                if (ActivityManager.checkUidPermission("android.permission.INTERACT_ACROSS_USERS", aInfo.uid) != 0) {
                    String msg = "Permission Denial: Component " + new ComponentName(aInfo.packageName, className).flattenToShortString() + " requests FLAG_SINGLE_USER, but app does not hold " + "android.permission.INTERACT_ACROSS_USERS";
                    Slog.w(TAG, msg);
                    throw new SecurityException(msg);
                }
                result = true;
            }
        } else if ("system".equals(componentProcessName)) {
            result = true;
        } else if ("com.huawei.indexsearch".equals(componentProcessName)) {
            result = true;
        } else if ((flags & 1073741824) != 0) {
            result = !UserHandle.isSameApp(aInfo.uid, 1001) ? (aInfo.flags & 8) != 0 : true;
        }
        if (ActivityManagerDebugConfig.DEBUG_MU) {
            Slog.v(TAG_MU, "isSingleton(" + componentProcessName + ", " + aInfo + ", " + className + ", 0x" + Integer.toHexString(flags) + ") = " + result);
        }
        return result;
    }

    boolean isValidSingletonCall(int callingUid, int componentUid) {
        int componentAppId = UserHandle.getAppId(componentUid);
        if (UserHandle.isSameApp(callingUid, componentUid) || componentAppId == 1000 || componentAppId == 1001 || ActivityManager.checkUidPermission("android.permission.INTERACT_ACROSS_USERS_FULL", componentUid) == 0) {
            return true;
        }
        return false;
    }

    public int bindService(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, String callingPackage, int userId) throws TransactionTooLargeException {
        enforceNotIsolatedCaller("bindService");
        if (service != null && service.hasFileDescriptors()) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        } else if (callingPackage == null) {
            throw new IllegalArgumentException("callingPackage cannot be null");
        } else {
            int bindServiceLocked;
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    bindServiceLocked = this.mServices.bindServiceLocked(caller, token, service, resolvedType, connection, flags, callingPackage, userId);
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return bindServiceLocked;
        }
    }

    public boolean unbindService(IServiceConnection connection) {
        boolean unbindServiceLocked;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                unbindServiceLocked = this.mServices.unbindServiceLocked(connection);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return unbindServiceLocked;
    }

    public void publishService(IBinder token, Intent intent, IBinder service) {
        if (intent == null || !intent.hasFileDescriptors()) {
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    if (token instanceof ServiceRecord) {
                        this.mServices.publishServiceLocked((ServiceRecord) token, intent, service);
                    } else {
                        throw new IllegalArgumentException("Invalid service token");
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        throw new IllegalArgumentException("File descriptors passed in Intent");
    }

    public void unbindFinished(IBinder token, Intent intent, boolean doRebind) {
        if (intent == null || !intent.hasFileDescriptors()) {
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    this.mServices.unbindFinishedLocked((ServiceRecord) token, intent, doRebind);
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        throw new IllegalArgumentException("File descriptors passed in Intent");
    }

    public void serviceDoneExecuting(IBinder token, int type, int startId, int res) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if (token instanceof ServiceRecord) {
                    this.mServices.serviceDoneExecutingLocked((ServiceRecord) token, type, startId, res);
                } else {
                    Slog.e(TAG, "serviceDoneExecuting: Invalid service token=" + token);
                    throw new IllegalArgumentException("Invalid service token");
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean bindBackupAgent(String packageName, int backupMode, int userId) {
        if (ActivityManagerDebugConfig.DEBUG_BACKUP) {
            Slog.v(TAG, "bindBackupAgent: app=" + packageName + " mode=" + backupMode);
        }
        enforceCallingPermission("android.permission.CONFIRM_FULL_BACKUP", "bindBackupAgent");
        ApplicationInfo app = null;
        try {
            app = AppGlobals.getPackageManager().getApplicationInfo(packageName, 0, userId);
        } catch (RemoteException e) {
        }
        if (app == null) {
            Slog.w(TAG, "Unable to bind backup agent for " + packageName);
            return false;
        }
        synchronized (this) {
            try {
                Serv ss;
                ComponentName hostingName;
                boostPriorityForLockedSection();
                BatteryStatsImpl stats = this.mBatteryStatsService.getActiveStatistics();
                synchronized (stats) {
                    ss = stats.getServiceStatsLocked(app.uid, app.packageName, app.name);
                }
                try {
                    AppGlobals.getPackageManager().setPackageStoppedState(app.packageName, false, UserHandle.getUserId(app.uid));
                } catch (RemoteException e2) {
                } catch (IllegalArgumentException e3) {
                    Slog.w(TAG, "Failed trying to unstop package " + app.packageName + ": " + e3);
                }
                BackupRecord backupRecord = new BackupRecord(ss, app, backupMode);
                if (backupMode == 0) {
                    hostingName = new ComponentName(app.packageName, app.backupAgentName);
                } else {
                    hostingName = new ComponentName("android", "FullBackupAgent");
                }
                ProcessRecord proc = startProcessLocked(app.processName, app, false, 0, "backup", hostingName, false, false, false);
                if (proc == null) {
                    Slog.e(TAG, "Unable to start backup agent process " + backupRecord);
                    resetPriorityAfterLockedSection();
                    return false;
                }
                if (UserHandle.isApp(app.uid) && backupMode == 1) {
                    proc.inFullBackup = true;
                }
                backupRecord.app = proc;
                this.mBackupTarget = backupRecord;
                this.mBackupAppName = app.packageName;
                updateOomAdjLocked(proc);
                if (proc.thread != null) {
                    if (ActivityManagerDebugConfig.DEBUG_BACKUP) {
                        Slog.v(TAG_BACKUP, "Agent proc already running: " + proc);
                    }
                    try {
                        proc.thread.scheduleCreateBackupAgent(app, compatibilityInfoForPackageLocked(app), backupMode);
                    } catch (RemoteException e4) {
                    }
                } else if (ActivityManagerDebugConfig.DEBUG_BACKUP) {
                    Slog.v(TAG_BACKUP, "Agent proc not running, waiting for attach");
                }
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
        return true;
    }

    public void clearPendingBackup() {
        if (ActivityManagerDebugConfig.DEBUG_BACKUP) {
            Slog.v(TAG_BACKUP, "clearPendingBackup");
        }
        enforceCallingPermission("android.permission.BACKUP", "clearPendingBackup");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mBackupTarget = null;
                this.mBackupAppName = null;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void backupAgentCreated(String agentPackageName, IBinder agent) {
        if (ActivityManagerDebugConfig.DEBUG_BACKUP) {
            Slog.v(TAG_BACKUP, "backupAgentCreated: " + agentPackageName + " = " + agent);
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if (!agentPackageName.equals(this.mBackupAppName)) {
                    Slog.e(TAG, "Backup agent created for " + agentPackageName + " but not requested!");
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void unbindBackupAgent(ApplicationInfo appInfo) {
        if (ActivityManagerDebugConfig.DEBUG_BACKUP) {
            Slog.v(TAG_BACKUP, "unbindBackupAgent: " + appInfo);
        }
        if (appInfo == null) {
            Slog.w(TAG, "unbind backup agent for null app");
            return;
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                try {
                    if (this.mBackupAppName == null) {
                        Slog.w(TAG, "Unbinding backup agent with no active backup");
                        this.mBackupTarget = null;
                        this.mBackupAppName = null;
                    } else if (this.mBackupAppName.equals(appInfo.packageName)) {
                        ProcessRecord proc = this.mBackupTarget.app;
                        updateOomAdjLocked(proc);
                        if (proc.thread != null) {
                            proc.thread.scheduleDestroyBackupAgent(appInfo, compatibilityInfoForPackageLocked(appInfo));
                        }
                        this.mBackupTarget = null;
                        this.mBackupAppName = null;
                        resetPriorityAfterLockedSection();
                    } else {
                        Slog.e(TAG, "Unbind of " + appInfo + " but is not the current backup target");
                        this.mBackupTarget = null;
                        this.mBackupAppName = null;
                        resetPriorityAfterLockedSection();
                    }
                } catch (Exception e) {
                    Slog.e(TAG, "Exception when unbinding backup agent:");
                    e.printStackTrace();
                } catch (Throwable th) {
                    this.mBackupTarget = null;
                    this.mBackupAppName = null;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    boolean isPendingBroadcastProcessLocked(int pid) {
        if (this.mFgBroadcastQueue.isPendingBroadcastProcessLocked(pid) || this.mBgBroadcastQueue.isPendingBroadcastProcessLocked(pid) || isThirdPartyAppPendingBroadcastProcessLocked(pid)) {
            return true;
        }
        return isKeyAppPendingBroadcastProcessLocked(pid);
    }

    void skipPendingBroadcastLocked(int pid) {
        Slog.w(TAG, "Unattached app died before broadcast acknowledged, skipping");
        for (BroadcastQueue queue : this.mBroadcastQueues) {
            queue.skipPendingBroadcastLocked(pid);
        }
    }

    boolean sendPendingBroadcastsLocked(ProcessRecord app) {
        boolean didSomething = false;
        for (BroadcastQueue queue : this.mBroadcastQueues) {
            didSomething |= queue.sendPendingBroadcastsLocked(app);
        }
        return didSomething;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Intent registerReceiver(IApplicationThread caller, String callerPackage, IIntentReceiver receiver, IntentFilter filter, String permission, int userId) {
        Throwable th;
        enforceNotIsolatedCaller("registerReceiver");
        ArrayList<Intent> stickyIntents = null;
        ProcessRecord processRecord = null;
        synchronized (this) {
            try {
                int callingUid;
                int callingPid;
                boostPriorityForLockedSection();
                if (caller != null) {
                    processRecord = getRecordForAppLocked(caller);
                    if (processRecord == null) {
                        throw new SecurityException("Unable to find app for caller " + caller + " (pid=" + Binder.getCallingPid() + ") when registering receiver " + receiver);
                    } else if (processRecord.info.uid == 1000 || processRecord.pkgList.containsKey(callerPackage) || "android".equals(callerPackage)) {
                        callingUid = processRecord.info.uid;
                        callingPid = processRecord.pid;
                    } else {
                        throw new SecurityException("Given caller package " + callerPackage + " is not running in process " + processRecord);
                    }
                }
                callerPackage = null;
                callingUid = Binder.getCallingUid();
                callingPid = Binder.getCallingPid();
                userId = this.mUserController.handleIncomingUser(callingPid, callingUid, userId, true, 2, "registerReceiver", callerPackage);
                Iterator<String> actions = filter.actionsIterator();
                if (actions == null) {
                    ArrayList<String> arrayList = new ArrayList(1);
                    arrayList.add(null);
                    actions = arrayList.iterator();
                }
                int[] userIds = new int[]{-1, UserHandle.getUserId(callingUid)};
                while (actions.hasNext()) {
                    String action = (String) actions.next();
                    int i = 0;
                    int length = userIds.length;
                    ArrayList<Intent> stickyIntents2 = stickyIntents;
                    while (i < length) {
                        try {
                            ArrayMap<String, ArrayList<Intent>> stickies = (ArrayMap) this.mStickyBroadcasts.get(userIds[i]);
                            if (stickies != null) {
                                ArrayList<Intent> intents = (ArrayList) stickies.get(action);
                                if (intents != null) {
                                    if (stickyIntents2 == null) {
                                        stickyIntents = new ArrayList();
                                    } else {
                                        stickyIntents = stickyIntents2;
                                    }
                                    stickyIntents.addAll(intents);
                                    i++;
                                    stickyIntents2 = stickyIntents;
                                }
                            }
                            stickyIntents = stickyIntents2;
                            i++;
                            stickyIntents2 = stickyIntents;
                        } catch (Throwable th2) {
                            th = th2;
                            stickyIntents = stickyIntents2;
                        }
                    }
                    stickyIntents = stickyIntents2;
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }
        resetPriorityAfterLockedSection();
        throw th;
    }

    public void unregisterReceiver(IIntentReceiver receiver) {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v(TAG_BROADCAST, "Unregister receiver: " + receiver);
        }
        long origId = Binder.clearCallingIdentity();
        boolean doTrim = false;
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                ReceiverList rl = (ReceiverList) this.mRegisteredReceivers.get(receiver.asBinder());
                if (rl != null) {
                    BroadcastRecord r = rl.curBroadcast;
                    if (r != null && r == r.queue.getMatchingOrderedReceiver(r) && r.queue.finishReceiverLocked(r, r.resultCode, r.resultData, r.resultExtras, r.resultAbort, false)) {
                        doTrim = true;
                        r.queue.processNextBroadcast(false);
                    }
                    if (rl.app != null) {
                        rl.app.receivers.remove(rl);
                    }
                    removeReceiverLocked(rl);
                    if (rl.linkedToDeath) {
                        rl.linkedToDeath = false;
                        rl.receiver.asBinder().unlinkToDeath(rl, 0);
                    }
                }
            }
            resetPriorityAfterLockedSection();
            if (doTrim) {
                trimApplications();
                Binder.restoreCallingIdentity(origId);
                return;
            }
            Binder.restoreCallingIdentity(origId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
        }
    }

    void removeReceiverLocked(ReceiverList rl) {
        this.mRegisteredReceivers.remove(rl.receiver.asBinder());
        for (int i = rl.size() - 1; i >= 0; i--) {
            this.mReceiverResolver.removeFilter((BroadcastFilter) rl.get(i));
        }
    }

    private final void sendPackageBroadcastLocked(int cmd, String[] packages, int userId) {
        for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
            ProcessRecord r = (ProcessRecord) this.mLruProcesses.get(i);
            if (r.thread != null && (userId == -1 || r.userId == userId)) {
                try {
                    r.thread.dispatchPackageBroadcast(cmd, packages);
                } catch (RemoteException e) {
                }
            }
        }
    }

    private List<ResolveInfo> collectReceiverComponents(Intent intent, String resolvedType, int callingUid, int[] users, ProcessRecord callerApp) {
        List<ResolveInfo> list = null;
        HashSet<ComponentName> singleUserReceivers = null;
        boolean scannedFirstReceivers = false;
        for (int user : users) {
            if (callingUid != 2000 || !this.mUserController.hasUserRestriction("no_debugging_features", user) || isPermittedShellBroadcast(intent)) {
                int i;
                List<ResolveInfo> newReceivers = queryIntentReceivers(callerApp, intent, resolvedType, 268436480, user);
                if (!(user == 0 || newReceivers == null)) {
                    i = 0;
                    while (i < newReceivers.size()) {
                        if ((((ResolveInfo) newReceivers.get(i)).activityInfo.flags & 536870912) != 0) {
                            newReceivers.remove(i);
                            i--;
                        }
                        i++;
                    }
                }
                if (newReceivers != null && newReceivers.size() == 0) {
                    newReceivers = null;
                }
                if (list == null) {
                    list = newReceivers;
                } else if (newReceivers == null) {
                    continue;
                } else {
                    HashSet<ComponentName> singleUserReceivers2;
                    ResolveInfo ri;
                    ComponentName cn;
                    if (!scannedFirstReceivers) {
                        scannedFirstReceivers = true;
                        i = 0;
                        singleUserReceivers2 = singleUserReceivers;
                        while (i < list.size()) {
                            ri = (ResolveInfo) list.get(i);
                            if ((ri.activityInfo.flags & 1073741824) != 0) {
                                cn = new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
                                if (singleUserReceivers2 == null) {
                                    singleUserReceivers = new HashSet();
                                } else {
                                    singleUserReceivers = singleUserReceivers2;
                                }
                                singleUserReceivers.add(cn);
                            } else {
                                singleUserReceivers = singleUserReceivers2;
                            }
                            i++;
                            singleUserReceivers2 = singleUserReceivers;
                        }
                        singleUserReceivers = singleUserReceivers2;
                    }
                    i = 0;
                    singleUserReceivers2 = singleUserReceivers;
                    while (i < newReceivers.size()) {
                        ri = (ResolveInfo) newReceivers.get(i);
                        if ((ri.activityInfo.flags & 1073741824) != 0) {
                            cn = new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
                            if (singleUserReceivers2 == null) {
                                singleUserReceivers = new HashSet();
                            } else {
                                singleUserReceivers = singleUserReceivers2;
                            }
                            try {
                                if (!singleUserReceivers.contains(cn)) {
                                    singleUserReceivers.add(cn);
                                    list.add(ri);
                                }
                            } catch (RemoteException e) {
                            }
                        } else {
                            try {
                                list.add(ri);
                                singleUserReceivers = singleUserReceivers2;
                            } catch (RemoteException e2) {
                                singleUserReceivers = singleUserReceivers2;
                            }
                        }
                        i++;
                        singleUserReceivers2 = singleUserReceivers;
                    }
                    singleUserReceivers = singleUserReceivers2;
                }
            }
        }
        if (!(list == null || list.isEmpty())) {
            startupFilterReceiverList(intent, list);
        }
        if (!(list == null || list.isEmpty())) {
            filterBadAppsReceiverList(intent, list);
        }
        return list;
    }

    private boolean isPermittedShellBroadcast(Intent intent) {
        return INTENT_REMOTE_BUGREPORT_FINISHED.equals(intent.getAction());
    }

    int broadcastIntentLocked(ProcessRecord callerApp, String callerPackage, Intent intent, String resolvedType, IIntentReceiver resultTo, int resultCode, String resultData, Bundle resultExtras, String[] requiredPermissions, int appOp, Bundle bOptions, boolean ordered, boolean sticky, int callingPid, int callingUid, int userId) {
        Intent intent2 = new Intent(intent);
        intent2.addFlags(16);
        if (!this.mProcessesReady && (intent2.getFlags() & 33554432) == 0) {
            intent2.addFlags(1073741824);
        }
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT) {
            Slog.v(TAG_BROADCAST, (sticky ? "Broadcast sticky: " : "Broadcast: ") + intent2 + " ordered=" + ordered + " userid=" + userId);
        }
        if (!(resultTo == null || ordered)) {
            Slog.w(TAG, "Broadcast " + intent2 + " not ordered but result callback requested!");
        }
        userId = this.mUserController.handleIncomingUser(callingPid, callingUid, userId, true, 0, "broadcast", callerPackage);
        if (userId == -1 || this.mUserController.isUserRunningLocked(userId, 0) || ((callingUid == 1000 && (intent2.getFlags() & 33554432) != 0) || "android.intent.action.ACTION_SHUTDOWN".equals(intent2.getAction()))) {
            String msg;
            BroadcastOptions broadcastOptions = null;
            if (bOptions != null) {
                BroadcastOptions broadcastOptions2 = new BroadcastOptions(bOptions);
                if (broadcastOptions2.getTemporaryAppWhitelistDuration() > 0 && checkComponentPermission("android.permission.CHANGE_DEVICE_IDLE_TEMP_WHITELIST", Binder.getCallingPid(), Binder.getCallingUid(), -1, true) != 0) {
                    msg = "Permission Denial: " + intent2.getAction() + " broadcast from " + callerPackage + " (pid=" + callingPid + ", uid=" + callingUid + ")" + " requires " + "android.permission.CHANGE_DEVICE_IDLE_TEMP_WHITELIST";
                    Slog.w(TAG, msg);
                    throw new SecurityException(msg);
                }
            }
            String action = intent2.getAction();
            try {
                boolean isCallerSystem;
                Uri data;
                BroadcastQueue queue;
                int i;
                boolean isProtectedBroadcast = AppGlobals.getPackageManager().isProtectedBroadcast(action);
                switch (UserHandle.getAppId(callingUid)) {
                    case 0:
                    case 1000:
                    case 1001:
                    case 1002:
                    case 1027:
                        isCallerSystem = true;
                        break;
                    default:
                        if (callerApp == null) {
                            isCallerSystem = false;
                            break;
                        }
                        isCallerSystem = callerApp.persistent;
                        break;
                }
                if (isCallerSystem) {
                    if (!(isProtectedBroadcast || "android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action) || "android.intent.action.DISMISS_KEYBOARD_SHORTCUTS".equals(action) || "android.intent.action.MEDIA_BUTTON".equals(action) || "android.intent.action.MEDIA_SCANNER_SCAN_FILE".equals(action) || "android.intent.action.SHOW_KEYBOARD_SHORTCUTS".equals(action) || "android.appwidget.action.APPWIDGET_CONFIGURE".equals(action) || "android.appwidget.action.APPWIDGET_UPDATE".equals(action) || "android.location.HIGH_POWER_REQUEST_CHANGE".equals(action) || "com.android.omadm.service.CONFIGURATION_UPDATE".equals(action) || "android.text.style.SUGGESTION_PICKED".equals(action))) {
                        if (callerApp != null) {
                            Slog.w(TAG, "Sending non-protected broadcast " + action + " from system " + callerApp.toShortString() + " pkg " + callerPackage);
                        } else {
                            Slog.w(TAG, "Sending non-protected broadcast " + action + " from system uid " + UserHandle.formatUid(callingUid) + " pkg " + callerPackage);
                        }
                    }
                } else if (isProtectedBroadcast) {
                    if (!(checkPermission("android.permission.INSTALL_LOCATION_PROVIDER", callingPid, callingUid) == 0 && "android.intent.action.AIRPLANE_MODE".equals(intent2.getAction()))) {
                        msg = "Permission Denial: not allowed to send broadcast " + action + " from pid=" + callingPid + ", uid=" + callingUid;
                        Slog.w(TAG, msg);
                        throw new SecurityException(msg);
                    }
                } else if ("android.appwidget.action.APPWIDGET_CONFIGURE".equals(action) || "android.appwidget.action.APPWIDGET_UPDATE".equals(action)) {
                    if (callerPackage == null) {
                        msg = "Permission Denial: not allowed to send broadcast " + action + " from unknown caller.";
                        Slog.w(TAG, msg);
                        throw new SecurityException(msg);
                    } else if (intent2.getComponent() == null) {
                        intent2.setPackage(callerPackage);
                    } else if (!intent2.getComponent().getPackageName().equals(callerPackage)) {
                        msg = "Permission Denial: not allowed to send broadcast " + action + " to " + intent2.getComponent().getPackageName() + " from " + callerPackage;
                        Slog.w(TAG, msg);
                        throw new SecurityException(msg);
                    }
                }
                if (action != null) {
                    String ssp;
                    boolean replacing;
                    if (!action.equals("android.intent.action.UID_REMOVED")) {
                        if (!action.equals("android.intent.action.PACKAGE_REMOVED")) {
                            if (!action.equals("android.intent.action.PACKAGE_CHANGED")) {
                                if (!action.equals("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE")) {
                                    if (!action.equals("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE")) {
                                        if (!action.equals("android.intent.action.PACKAGES_SUSPENDED")) {
                                            if (!action.equals("android.intent.action.PACKAGES_UNSUSPENDED")) {
                                                if (action.equals("android.intent.action.PACKAGE_REPLACED")) {
                                                    data = intent2.getData();
                                                    if (data != null) {
                                                        ssp = data.getSchemeSpecificPart();
                                                        if (ssp != null) {
                                                            ApplicationInfo aInfo = getPackageManagerInternalLocked().getApplicationInfo(ssp, userId);
                                                            if (aInfo == null) {
                                                                Slog.w(TAG, "Dropping ACTION_PACKAGE_REPLACED for non-existent pkg: ssp=" + ssp + " data=" + data);
                                                                return 0;
                                                            }
                                                            this.mStackSupervisor.updateActivityApplicationInfoLocked(aInfo);
                                                            sendPackageBroadcastLocked(3, new String[]{ssp}, userId);
                                                        }
                                                    }
                                                } else {
                                                    if (!action.equals("android.intent.action.PACKAGE_ADDED")) {
                                                        if (action.equals("android.intent.action.PACKAGE_DATA_CLEARED")) {
                                                            data = intent2.getData();
                                                            if (data != null) {
                                                                ssp = data.getSchemeSpecificPart();
                                                                if (ssp != null) {
                                                                    if (this.mUnsupportedDisplaySizeDialog != null && ssp.equals(this.mUnsupportedDisplaySizeDialog.getPackageName())) {
                                                                        this.mUnsupportedDisplaySizeDialog.dismiss();
                                                                        this.mUnsupportedDisplaySizeDialog = null;
                                                                    }
                                                                    this.mCompatModePackages.handlePackageDataClearedLocked(ssp);
                                                                }
                                                            }
                                                        } else {
                                                            if (action.equals("android.intent.action.TIMEZONE_CHANGED")) {
                                                                this.mHandler.sendEmptyMessage(13);
                                                            } else {
                                                                if (action.equals("android.intent.action.TIME_SET")) {
                                                                    int is24Hour;
                                                                    if (intent2.getBooleanExtra("android.intent.extra.TIME_PREF_24_HOUR_FORMAT", false)) {
                                                                        is24Hour = 1;
                                                                    } else {
                                                                        is24Hour = 0;
                                                                    }
                                                                    this.mHandler.sendMessage(this.mHandler.obtainMessage(41, is24Hour, 0));
                                                                    BatteryStatsImpl stats = this.mBatteryStatsService.getActiveStatistics();
                                                                    synchronized (stats) {
                                                                        stats.noteCurrentTimeChangedLocked();
                                                                    }
                                                                } else {
                                                                    if (action.equals("android.intent.action.CLEAR_DNS_CACHE")) {
                                                                        this.mHandler.sendEmptyMessage(28);
                                                                    } else {
                                                                        if (action.equals("android.intent.action.PROXY_CHANGE")) {
                                                                            this.mHandler.sendMessage(this.mHandler.obtainMessage(29, (ProxyInfo) intent2.getParcelableExtra("android.intent.extra.PROXY_INFO")));
                                                                        } else {
                                                                            if (!action.equals("android.hardware.action.NEW_PICTURE")) {
                                                                                if (action.equals("android.hardware.action.NEW_VIDEO")) {
                                                                                }
                                                                            }
                                                                            Slog.w(TAG, action + " no longer allowed; dropping from " + UserHandle.formatUid(callingUid));
                                                                            if (resultTo != null) {
                                                                                queue = broadcastQueueForIntent(intent2);
                                                                                try {
                                                                                    queue.performReceiveLocked(callerApp, resultTo, intent2, 0, null, null, false, false, userId);
                                                                                } catch (Throwable e) {
                                                                                    Slog.w(TAG, "Failure [" + queue.mQueueName + "] sending broadcast result of " + intent2, e);
                                                                                }
                                                                            }
                                                                            return 0;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    } else if (isLimitedPackageBroadcast(intent2)) {
                                                        Flog.d(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "Android Wear-skip limited ACTION_PACKAGE_ADDED");
                                                    } else {
                                                        data = intent2.getData();
                                                        if (data != null) {
                                                            ssp = data.getSchemeSpecificPart();
                                                            if (ssp != null) {
                                                                replacing = intent2.getBooleanExtra("android.intent.extra.REPLACING", false);
                                                                this.mCompatModePackages.handlePackageAddedLocked(ssp, replacing);
                                                                HwCustNonHardwareAcceleratedPackagesManager.getDefault().handlePackageAdded(ssp, replacing);
                                                                try {
                                                                    ApplicationInfo ai = AppGlobals.getPackageManager().getApplicationInfo(ssp, 0, 0);
                                                                    this.mBatteryStatsService.notePackageInstalled(ssp, ai != null ? ai.versionCode : 0);
                                                                } catch (RemoteException e2) {
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (checkComponentPermission("android.permission.BROADCAST_PACKAGE_REMOVED", callingPid, callingUid, -1, true) != 0) {
                        msg = "Permission Denial: " + intent2.getAction() + " broadcast from " + callerPackage + " (pid=" + callingPid + ", uid=" + callingUid + ")" + " requires " + "android.permission.BROADCAST_PACKAGE_REMOVED";
                        Slog.w(TAG, msg);
                        throw new SecurityException(msg);
                    } else if (isLimitedPackageBroadcast(intent2)) {
                        Flog.d(HdmiCecKeycode.CEC_KEYCODE_RESTORE_VOLUME_FUNCTION, "Android Wear-skip limited ACTION_PACKAGE_REMOVED");
                    } else {
                        if (action.equals("android.intent.action.UID_REMOVED")) {
                            Bundle intentExtras = intent2.getExtras();
                            int uid = intentExtras != null ? intentExtras.getInt("android.intent.extra.UID") : -1;
                            if (uid >= 0) {
                                this.mBatteryStatsService.removeUid(uid);
                                this.mAppOpsService.uidRemoved(uid);
                            }
                        } else {
                            if (action.equals("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE")) {
                                String[] list = intent2.getStringArrayExtra("android.intent.extra.changed_package_list");
                                if (list != null && list.length > 0) {
                                    for (String ssp2 : list) {
                                        forceStopPackageLocked(ssp2, -1, false, true, true, false, false, userId, "storage unmount");
                                    }
                                    this.mRecentTasks.cleanupLocked(-1);
                                    sendPackageBroadcastLocked(1, list, userId);
                                }
                            } else {
                                if (action.equals("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE")) {
                                    this.mRecentTasks.cleanupLocked(-1);
                                } else {
                                    if (!action.equals("android.intent.action.PACKAGE_REMOVED")) {
                                        if (!action.equals("android.intent.action.PACKAGE_CHANGED")) {
                                            if (!action.equals("android.intent.action.PACKAGES_SUSPENDED")) {
                                                if (action.equals("android.intent.action.PACKAGES_UNSUSPENDED")) {
                                                }
                                            }
                                            boolean suspended = "android.intent.action.PACKAGES_SUSPENDED".equals(intent2.getAction());
                                            String[] packageNames = intent2.getStringArrayExtra("android.intent.extra.changed_package_list");
                                            int userHandle = intent2.getIntExtra("android.intent.extra.user_handle", -10000);
                                            synchronized (this) {
                                                try {
                                                    boostPriorityForLockedSection();
                                                    this.mRecentTasks.onPackagesSuspendedChanged(packageNames, suspended, userHandle);
                                                } finally {
                                                    resetPriorityAfterLockedSection();
                                                }
                                            }
                                        }
                                    }
                                    data = intent2.getData();
                                    if (data != null) {
                                        ssp2 = data.getSchemeSpecificPart();
                                        if (ssp2 != null) {
                                            boolean removed = "android.intent.action.PACKAGE_REMOVED".equals(action);
                                            replacing = intent2.getBooleanExtra("android.intent.extra.REPLACING", false);
                                            boolean killProcess = !intent2.getBooleanExtra("android.intent.extra.DONT_KILL_APP", false);
                                            boolean fullUninstall = removed && !replacing;
                                            if (removed) {
                                                int cmd;
                                                if (killProcess) {
                                                    forceStopPackageLocked(ssp2, UserHandle.getAppId(intent2.getIntExtra("android.intent.extra.UID", -1)), false, true, true, false, fullUninstall, userId, removed ? "pkg removed" : "pkg changed");
                                                }
                                                if (killProcess) {
                                                    cmd = 0;
                                                } else {
                                                    cmd = 2;
                                                }
                                                sendPackageBroadcastLocked(cmd, new String[]{ssp2}, userId);
                                                if (fullUninstall) {
                                                    this.mAppOpsService.packageRemoved(intent2.getIntExtra("android.intent.extra.UID", -1), ssp2);
                                                    removeUriPermissionsForPackageLocked(ssp2, userId, true);
                                                    removeTasksByPackageNameLocked(ssp2, userId);
                                                    if (this.mUnsupportedDisplaySizeDialog != null && ssp2.equals(this.mUnsupportedDisplaySizeDialog.getPackageName())) {
                                                        this.mUnsupportedDisplaySizeDialog.dismiss();
                                                        this.mUnsupportedDisplaySizeDialog = null;
                                                    }
                                                    this.mCompatModePackages.handlePackageUninstalledLocked(ssp2);
                                                    this.mBatteryStatsService.notePackageUninstalled(ssp2);
                                                }
                                            } else {
                                                if (killProcess) {
                                                    killPackageProcessesLocked(ssp2, UserHandle.getAppId(intent2.getIntExtra("android.intent.extra.UID", -1)), userId, -10000, false, true, true, false, "change " + ssp2);
                                                }
                                                cleanupDisabledPackageComponentsLocked(ssp2, userId, killProcess, intent2.getStringArrayExtra("android.intent.extra.changed_component_name_list"));
                                            }
                                            HwCustNonHardwareAcceleratedPackagesManager.getDefault().handlePackageRemoved(ssp2, removed);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (sticky) {
                    if (checkPermission("android.permission.BROADCAST_STICKY", callingPid, callingUid) != 0) {
                        msg = "Permission Denial: broadcastIntent() requesting a sticky broadcast from pid=" + callingPid + ", uid=" + callingUid + " requires " + "android.permission.BROADCAST_STICKY";
                        Slog.w(TAG, msg);
                        throw new SecurityException(msg);
                    } else if (requiredPermissions != null && requiredPermissions.length > 0) {
                        Slog.w(TAG, "Can't broadcast sticky intent " + intent2 + " and enforce permissions " + Arrays.toString(requiredPermissions));
                        return -1;
                    } else if (intent2.getComponent() != null) {
                        throw new SecurityException("Sticky broadcasts can't target a specific component");
                    } else {
                        ArrayMap<String, ArrayList<Intent>> stickies;
                        ArrayList<Intent> list2;
                        if (userId != -1) {
                            stickies = (ArrayMap) this.mStickyBroadcasts.get(-1);
                            if (stickies != null) {
                                list2 = (ArrayList) stickies.get(intent2.getAction());
                                if (list2 != null) {
                                    int N = list2.size();
                                    for (i = 0; i < N; i++) {
                                        if (intent2.filterEquals((Intent) list2.get(i))) {
                                            throw new IllegalArgumentException("Sticky broadcast " + intent2 + " for user " + userId + " conflicts with existing global broadcast");
                                        }
                                    }
                                }
                            }
                        }
                        stickies = (ArrayMap) this.mStickyBroadcasts.get(userId);
                        if (stickies == null) {
                            stickies = new ArrayMap();
                            this.mStickyBroadcasts.put(userId, stickies);
                        }
                        list2 = (ArrayList) stickies.get(intent2.getAction());
                        if (list2 == null) {
                            list2 = new ArrayList();
                            stickies.put(intent2.getAction(), list2);
                        }
                        int stickiesCount = list2.size();
                        i = 0;
                        while (i < stickiesCount) {
                            if (intent2.filterEquals((Intent) list2.get(i))) {
                                list2.set(i, new Intent(intent2));
                                if (i >= stickiesCount) {
                                    list2.add(new Intent(intent2));
                                }
                            } else {
                                i++;
                            }
                        }
                        if (i >= stickiesCount) {
                            list2.add(new Intent(intent2));
                        }
                    }
                }
                int[] users = userId == -1 ? this.mUserController.getStartedUserArrayLocked() : new int[]{userId};
                List receivers = null;
                List registeredReceivers = null;
                if ((intent2.getFlags() & 1073741824) == 0) {
                    receivers = collectReceiverComponents(intent2, resolvedType, callingUid, users, callerApp);
                }
                if (intent2.getComponent() == null) {
                    if (userId == -1 && callingUid == 2000) {
                        for (i = 0; i < users.length; i++) {
                            if (!this.mUserController.hasUserRestriction("no_debugging_features", users[i])) {
                                List<BroadcastFilter> registeredReceiversForUser = this.mReceiverResolver.queryIntent(intent2, resolvedType, false, users[i]);
                                if (registeredReceivers == null) {
                                    List<BroadcastFilter> registeredReceivers2 = registeredReceiversForUser;
                                } else if (registeredReceiversForUser != null) {
                                    registeredReceivers.addAll(registeredReceiversForUser);
                                }
                            }
                        }
                    } else {
                        registeredReceivers = this.mReceiverResolver.queryIntent(intent2, resolvedType, false, userId);
                    }
                }
                boolean replacePending = (intent2.getFlags() & 536870912) != 0;
                if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                    Slog.v(TAG_BROADCAST, "Enqueing broadcast: " + intent2.getAction() + " replacePending=" + replacePending);
                }
                checkBroadcastRecordSpeed(callingUid, callerPackage, callerApp);
                filterRegisterReceiversForEuid(registeredReceivers, callerApp);
                int NR = registeredReceivers != null ? registeredReceivers.size() : 0;
                if (!ordered && NR > 0) {
                    BroadcastRecord r;
                    if (!isKeyAppBroadcastQueue(1, callerPackage)) {
                        if (!isKeyAppBroadcastQueue(2, intent2.getAction())) {
                            if (isThirdPartyAppBroadcastQueue(callerApp)) {
                                queue = thirdPartyAppBroadcastQueueForIntent(intent2);
                            } else {
                                queue = broadcastQueueForIntent(intent2);
                            }
                            r = new BroadcastRecord(queue, intent2, callerApp, callerPackage, callingPid, callingUid, resolvedType, requiredPermissions, appOp, broadcastOptions, registeredReceivers, resultTo, resultCode, resultData, resultExtras, ordered, sticky, false, userId);
                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                Slog.v(TAG_BROADCAST, "Enqueueing parallel broadcast " + r);
                            }
                            if (!(replacePending ? queue.replaceParallelBroadcastLocked(r) : false)) {
                                queue.enqueueParallelBroadcastLocked(r);
                                queue.scheduleBroadcastsLocked();
                            }
                            registeredReceivers = null;
                            NR = 0;
                        }
                    }
                    queue = keyAppBroadcastQueueForIntent(intent2);
                    r = new BroadcastRecord(queue, intent2, callerApp, callerPackage, callingPid, callingUid, resolvedType, requiredPermissions, appOp, broadcastOptions, registeredReceivers, resultTo, resultCode, resultData, resultExtras, ordered, sticky, false, userId);
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.v(TAG_BROADCAST, "Enqueueing parallel broadcast " + r);
                    }
                    if (replacePending) {
                    }
                    if (replacePending ? queue.replaceParallelBroadcastLocked(r) : false) {
                        queue.enqueueParallelBroadcastLocked(r);
                        queue.scheduleBroadcastsLocked();
                    }
                    registeredReceivers = null;
                    NR = 0;
                }
                int ir = 0;
                if (receivers != null) {
                    int NT;
                    int it;
                    String[] strArr = null;
                    if ("android.intent.action.PACKAGE_ADDED".equals(intent2.getAction()) || "android.intent.action.PACKAGE_RESTARTED".equals(intent2.getAction()) || "android.intent.action.PACKAGE_DATA_CLEARED".equals(intent2.getAction())) {
                        data = intent2.getData();
                        if (!(data == null || data.getSchemeSpecificPart() == null)) {
                            strArr = new String[]{data.getSchemeSpecificPart()};
                        }
                    } else if ("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE".equals(intent2.getAction())) {
                        strArr = intent2.getStringArrayExtra("android.intent.extra.changed_package_list");
                    }
                    if (strArr != null && strArr.length > 0) {
                        for (String skipPackage : strArr) {
                            if (skipPackage != null) {
                                NT = receivers.size();
                                it = 0;
                                while (it < NT) {
                                    if (((ResolveInfo) receivers.get(it)).activityInfo.packageName.equals(skipPackage)) {
                                        receivers.remove(it);
                                        it--;
                                        NT--;
                                    }
                                    it++;
                                }
                            }
                        }
                    }
                    if (shouldDropCtsBroadcast(intent2)) {
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            Slog.d(TAG_BROADCAST, "shold Drop Broadcast for Cts test.");
                        }
                        receivers = null;
                    }
                    NT = receivers != null ? receivers.size() : 0;
                    it = 0;
                    ResolveInfo curt = null;
                    BroadcastFilter broadcastFilter = null;
                    while (it < NT && ir < NR) {
                        if (curt == null) {
                            curt = (ResolveInfo) receivers.get(it);
                        }
                        if (broadcastFilter == null) {
                            broadcastFilter = (BroadcastFilter) registeredReceivers.get(ir);
                        }
                        if (broadcastFilter.getPriority() >= curt.priority) {
                            receivers.add(it, broadcastFilter);
                            ir++;
                            broadcastFilter = null;
                            it++;
                            NT++;
                        } else {
                            it++;
                            curt = null;
                        }
                    }
                }
                while (ir < NR) {
                    if (receivers == null) {
                        receivers = new ArrayList();
                    }
                    receivers.add(registeredReceivers.get(ir));
                    ir++;
                }
                if ((receivers != null && receivers.size() > 0) || resultTo != null) {
                    BroadcastRecord broadcastRecord;
                    if (!isKeyAppBroadcastQueue(1, callerPackage)) {
                        if (!isKeyAppBroadcastQueue(2, intent2.getAction())) {
                            if (isThirdPartyAppBroadcastQueue(callerApp)) {
                                queue = thirdPartyAppBroadcastQueueForIntent(intent2);
                                intent2.addFlags(DumpState.DUMP_PREFERRED_XML);
                            } else {
                                queue = broadcastQueueForIntent(intent2);
                            }
                            broadcastRecord = new BroadcastRecord(queue, intent2, callerApp, callerPackage, callingPid, callingUid, resolvedType, requiredPermissions, appOp, broadcastOptions, receivers, resultTo, resultCode, resultData, resultExtras, ordered, sticky, false, userId);
                            Flog.i(HdmiCecKeycode.CEC_KEYCODE_SELECT_MEDIA_FUNCTION, "Enqueueing ordered broadcast[" + queue.mQueueName + "] " + broadcastRecord + ": prev had " + queue.mOrderedBroadcasts.size());
                            if (queue.mOrderedBroadcasts.size() > BroadcastQueue.MAX_BROADCAST_HISTORY / 10) {
                                Flog.i(HdmiCecKeycode.CEC_KEYCODE_SELECT_MEDIA_FUNCTION, "ordered broadcast[" + queue.mQueueName + "] head:" + queue.mOrderedBroadcasts.get(0));
                            }
                            if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                                Slog.i(TAG_BROADCAST, "Enqueueing broadcast " + broadcastRecord.intent.getAction());
                            }
                            if (!(replacePending ? queue.replaceOrderedBroadcastLocked(broadcastRecord) : false)) {
                                queue.enqueueOrderedBroadcastLocked(broadcastRecord);
                                queue.scheduleBroadcastsLocked();
                            }
                        }
                    }
                    queue = keyAppBroadcastQueueForIntent(intent2);
                    intent2.addFlags(4096);
                    broadcastRecord = new BroadcastRecord(queue, intent2, callerApp, callerPackage, callingPid, callingUid, resolvedType, requiredPermissions, appOp, broadcastOptions, receivers, resultTo, resultCode, resultData, resultExtras, ordered, sticky, false, userId);
                    Flog.i(HdmiCecKeycode.CEC_KEYCODE_SELECT_MEDIA_FUNCTION, "Enqueueing ordered broadcast[" + queue.mQueueName + "] " + broadcastRecord + ": prev had " + queue.mOrderedBroadcasts.size());
                    if (queue.mOrderedBroadcasts.size() > BroadcastQueue.MAX_BROADCAST_HISTORY / 10) {
                        Flog.i(HdmiCecKeycode.CEC_KEYCODE_SELECT_MEDIA_FUNCTION, "ordered broadcast[" + queue.mQueueName + "] head:" + queue.mOrderedBroadcasts.get(0));
                    }
                    if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                        Slog.i(TAG_BROADCAST, "Enqueueing broadcast " + broadcastRecord.intent.getAction());
                    }
                    if (replacePending) {
                    }
                    if (replacePending ? queue.replaceOrderedBroadcastLocked(broadcastRecord) : false) {
                        queue.enqueueOrderedBroadcastLocked(broadcastRecord);
                        queue.scheduleBroadcastsLocked();
                    }
                } else if (intent2.getComponent() == null && intent2.getPackage() == null && (intent2.getFlags() & 1073741824) == 0) {
                    addBroadcastStatLocked(intent2.getAction(), callerPackage, 0, 0, 0);
                }
                return 0;
            } catch (Throwable e3) {
                Slog.w(TAG, "Remote exception", e3);
                return 0;
            }
        }
        Slog.w(TAG, "Skipping broadcast of " + intent2 + ": user " + userId + " is stopped");
        return -2;
    }

    final void addBroadcastStatLocked(String action, String srcPackage, int receiveCount, int skipCount, long dispatchTime) {
        long now = SystemClock.elapsedRealtime();
        if (this.mCurBroadcastStats == null || this.mCurBroadcastStats.mStartRealtime + 86400000 < now) {
            this.mLastBroadcastStats = this.mCurBroadcastStats;
            if (this.mLastBroadcastStats != null) {
                this.mLastBroadcastStats.mEndRealtime = SystemClock.elapsedRealtime();
                this.mLastBroadcastStats.mEndUptime = SystemClock.uptimeMillis();
            }
            this.mCurBroadcastStats = new BroadcastStats();
        }
        this.mCurBroadcastStats.addBroadcast(action, srcPackage, receiveCount, skipCount, dispatchTime);
    }

    final Intent verifyBroadcastLocked(Intent intent) {
        if (intent == null || !intent.hasFileDescriptors()) {
            int flags = intent.getFlags();
            if (!this.mProcessesReady && (67108864 & flags) == 0 && (1073741824 & flags) == 0) {
                Slog.e(TAG, "Attempt to launch receivers of broadcast intent " + intent + " before boot completion");
                throw new IllegalStateException("Cannot broadcast before boot completed");
            } else if ((33554432 & flags) == 0) {
                return intent;
            } else {
                throw new IllegalArgumentException("Can't use FLAG_RECEIVER_BOOT_UPGRADE here");
            }
        }
        throw new IllegalArgumentException("File descriptors passed in Intent");
    }

    public final int broadcastIntent(IApplicationThread caller, Intent intent, String resolvedType, IIntentReceiver resultTo, int resultCode, String resultData, Bundle resultExtras, String[] requiredPermissions, int appOp, Bundle bOptions, boolean serialized, boolean sticky, int userId) {
        int res;
        enforceNotIsolatedCaller("broadcastIntent");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                intent = verifyBroadcastLocked(intent);
                ProcessRecord callerApp = getRecordForAppLocked(caller);
                int callingPid = Binder.getCallingPid();
                int callingUid = Binder.getCallingUid();
                long origId = Binder.clearCallingIdentity();
                res = broadcastIntentLocked(callerApp, callerApp != null ? callerApp.info.packageName : null, intent, resolvedType, resultTo, resultCode, resultData, resultExtras, requiredPermissions, appOp, bOptions, serialized, sticky, callingPid, callingUid, userId);
                Binder.restoreCallingIdentity(origId);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return res;
    }

    int broadcastIntentInPackage(String packageName, int uid, Intent intent, String resolvedType, IIntentReceiver resultTo, int resultCode, String resultData, Bundle resultExtras, String requiredPermission, Bundle bOptions, boolean serialized, boolean sticky, int userId) {
        int res;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                intent = verifyBroadcastLocked(intent);
                long origId = Binder.clearCallingIdentity();
                res = broadcastIntentLocked(null, packageName, intent, resolvedType, resultTo, resultCode, resultData, resultExtras, requiredPermission == null ? null : new String[]{requiredPermission}, -1, bOptions, serialized, sticky, -1, uid, userId);
                Binder.restoreCallingIdentity(origId);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return res;
    }

    public final void unbroadcastIntent(IApplicationThread caller, Intent intent, int userId) {
        if (intent == null || !intent.hasFileDescriptors()) {
            userId = this.mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, 0, "removeStickyBroadcast", null);
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    if (checkCallingPermission("android.permission.BROADCAST_STICKY") != 0) {
                        String msg = "Permission Denial: unbroadcastIntent() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.BROADCAST_STICKY";
                        Slog.w(TAG, msg);
                        throw new SecurityException(msg);
                    }
                    ArrayMap<String, ArrayList<Intent>> stickies = (ArrayMap) this.mStickyBroadcasts.get(userId);
                    if (stickies != null) {
                        ArrayList<Intent> list = (ArrayList) stickies.get(intent.getAction());
                        if (list != null) {
                            int N = list.size();
                            for (int i = 0; i < N; i++) {
                                if (intent.filterEquals((Intent) list.get(i))) {
                                    list.remove(i);
                                    break;
                                }
                            }
                            if (list.size() <= 0) {
                                stickies.remove(intent.getAction());
                            }
                        }
                        if (stickies.size() <= 0) {
                            this.mStickyBroadcasts.remove(userId);
                        }
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return;
        }
        throw new IllegalArgumentException("File descriptors passed in Intent");
    }

    void backgroundServicesFinishedLocked(int userId) {
        for (BroadcastQueue queue : this.mBroadcastQueues) {
            queue.backgroundServicesFinishedLocked(userId);
        }
    }

    public void finishReceiver(IBinder who, int resultCode, String resultData, Bundle resultExtras, boolean resultAbort, int flags) {
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
            Slog.v(TAG_BROADCAST, "Finish receiver: " + who);
        }
        if (resultExtras == null || !resultExtras.hasFileDescriptors()) {
            long origId = Binder.clearCallingIdentity();
            boolean doNext = false;
            BroadcastRecord broadcastRecord = null;
            try {
                synchronized (this) {
                    boostPriorityForLockedSection();
                    if ((flags & 4096) != 0) {
                        BroadcastQueue keyQueue = (268435456 & flags) != 0 ? this.mFgKeyAppBroadcastQueue : this.mBgKeyAppBroadcastQueue;
                        if (keyQueue != null) {
                            broadcastRecord = keyQueue.getMatchingOrderedReceiver(who);
                        }
                        if (ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                            Slog.v(TAG_BROADCAST, "Finish receiver find key broadcast: " + broadcastRecord);
                        }
                    } else if ((flags & DumpState.DUMP_PREFERRED_XML) != 0) {
                        BroadcastQueue thirdPartyQueue = (268435456 & flags) != 0 ? this.mFgThirdAppBroadcastQueue : this.mBgThirdAppBroadcastQueue;
                        if (thirdPartyQueue != null) {
                            broadcastRecord = thirdPartyQueue.getMatchingOrderedReceiver(who);
                        }
                    } else {
                        broadcastRecord = ((268435456 & flags) != 0 ? this.mFgBroadcastQueue : this.mBgBroadcastQueue).getMatchingOrderedReceiver(who);
                    }
                    if (broadcastRecord != null) {
                        doNext = broadcastRecord.queue.finishReceiverLocked(broadcastRecord, resultCode, resultData, resultExtras, resultAbort, true);
                    }
                }
                resetPriorityAfterLockedSection();
                if (doNext) {
                    broadcastRecord.queue.processNextBroadcast(false);
                }
                trimApplications();
                Binder.restoreCallingIdentity(origId);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
        } else {
            throw new IllegalArgumentException("File descriptors passed in Bundle");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean startInstrumentation(ComponentName className, String profileFile, int flags, Bundle arguments, IInstrumentationWatcher watcher, IUiAutomationConnection uiAutomationConnection, int userId, String abiOverride) {
        enforceNotIsolatedCaller("startInstrumentation");
        userId = this.mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, 2, "startInstrumentation", null);
        if (arguments == null || !arguments.hasFileDescriptors()) {
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    InstrumentationInfo instrumentationInfo = null;
                    ApplicationInfo ai = null;
                    try {
                        instrumentationInfo = this.mContext.getPackageManager().getInstrumentationInfo(className, 1024);
                        ai = AppGlobals.getPackageManager().getApplicationInfo(instrumentationInfo.targetPackage, 1024, userId);
                    } catch (NameNotFoundException e) {
                    } catch (RemoteException e2) {
                    }
                    if (instrumentationInfo == null) {
                        reportStartInstrumentationFailureLocked(watcher, className, "Unable to find instrumentation info for: " + className);
                    } else if (ai == null) {
                        reportStartInstrumentationFailureLocked(watcher, className, "Unable to find instrumentation target package: " + instrumentationInfo.targetPackage);
                        resetPriorityAfterLockedSection();
                        return false;
                    } else if (ai.hasCode()) {
                        int match = this.mContext.getPackageManager().checkSignatures(instrumentationInfo.targetPackage, instrumentationInfo.packageName);
                        if (match >= 0 || match == -1) {
                            long origId = Binder.clearCallingIdentity();
                            boolean evenPersistent = true;
                            if (arguments != null) {
                                Slog.i(TAG, "Arguments param disableAnalytics results = " + arguments.getString("disableAnalytics"));
                                evenPersistent = !"true".equals(arguments.getString("disableAnalytics"));
                            }
                            forceStopPackageLocked(instrumentationInfo.targetPackage, -1, true, false, true, evenPersistent, false, userId, "start instr");
                            ProcessRecord app = addAppLocked(ai, false, abiOverride);
                            app.instrumentationClass = className;
                            app.instrumentationInfo = ai;
                            app.instrumentationProfileFile = profileFile;
                            app.instrumentationArguments = arguments;
                            app.instrumentationWatcher = watcher;
                            app.instrumentationUiAutomationConnection = uiAutomationConnection;
                            app.instrumentationResultClass = className;
                            Binder.restoreCallingIdentity(origId);
                            resetPriorityAfterLockedSection();
                            return true;
                        }
                        String msg = "Permission Denial: starting instrumentation " + className + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingPid() + " not allowed because package " + instrumentationInfo.packageName + " does not have a signature matching the target " + instrumentationInfo.targetPackage;
                        reportStartInstrumentationFailureLocked(watcher, className, msg);
                        throw new SecurityException(msg);
                    } else {
                        reportStartInstrumentationFailureLocked(watcher, className, "Instrumentation target has no code: " + instrumentationInfo.targetPackage);
                        resetPriorityAfterLockedSection();
                        return false;
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new IllegalArgumentException("File descriptors passed in Bundle");
        }
    }

    private void reportStartInstrumentationFailureLocked(IInstrumentationWatcher watcher, ComponentName cn, String report) {
        Slog.w(TAG, report);
        if (watcher != null) {
            Bundle results = new Bundle();
            results.putString("id", "ActivityManagerService");
            results.putString("Error", report);
            this.mInstrumentationReporter.reportStatus(watcher, cn, -1, results);
        }
    }

    void finishInstrumentationLocked(ProcessRecord app, int resultCode, Bundle results) {
        if (app.instrumentationWatcher != null) {
            this.mInstrumentationReporter.reportFinished(app.instrumentationWatcher, app.instrumentationClass, resultCode, results);
        }
        if (app.instrumentationUiAutomationConnection != null) {
            this.mHandler.obtainMessage(SHUTDOWN_UI_AUTOMATION_CONNECTION_MSG, app.instrumentationUiAutomationConnection).sendToTarget();
        }
        app.instrumentationWatcher = null;
        app.instrumentationUiAutomationConnection = null;
        app.instrumentationClass = null;
        app.instrumentationInfo = null;
        app.instrumentationProfileFile = null;
        app.instrumentationArguments = null;
        boolean evenPersistent = true;
        if (results != null) {
            Slog.i(TAG, "Bundle param DontKillDeptProc = " + results.getString("DontKillDeptProc"));
            evenPersistent = !"true".equals(results.getString("DontKillDeptProc"));
        }
        forceStopPackageLocked(app.info.packageName, -1, false, false, true, evenPersistent, false, app.userId, "finished inst");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void finishInstrumentation(IApplicationThread target, int resultCode, Bundle results) {
        int userId = UserHandle.getCallingUserId();
        if (results == null || !results.hasFileDescriptors()) {
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    ProcessRecord app = getRecordForAppLocked(target);
                    if (app == null) {
                        Slog.w(TAG, "finishInstrumentation: no app for " + target);
                    } else {
                        long origId = Binder.clearCallingIdentity();
                        finishInstrumentationLocked(app, resultCode, results);
                        Binder.restoreCallingIdentity(origId);
                        resetPriorityAfterLockedSection();
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
        } else {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
    }

    public ConfigurationInfo getDeviceConfigurationInfo() {
        ConfigurationInfo config = new ConfigurationInfo();
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                config.reqTouchScreen = this.mConfiguration.touchscreen;
                config.reqKeyboardType = this.mConfiguration.keyboard;
                config.reqNavigation = this.mConfiguration.navigation;
                if (this.mConfiguration.navigation == 2 || this.mConfiguration.navigation == 3) {
                    config.reqInputFeatures |= 2;
                }
                if (!(this.mConfiguration.keyboard == 0 || this.mConfiguration.keyboard == 1)) {
                    config.reqInputFeatures |= 1;
                }
                config.reqGlEsVersion = this.GL_ES_VERSION;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return config;
    }

    ActivityStack getFocusedStack() {
        return this.mStackSupervisor.getFocusedStack();
    }

    public int getFocusedStackId() throws RemoteException {
        ActivityStack focusedStack = getFocusedStack();
        if (focusedStack != null) {
            return focusedStack.getStackId();
        }
        return -1;
    }

    public Configuration getConfiguration() {
        Configuration ci;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ci = new Configuration(this.mConfiguration);
                ci.userSetLocale = false;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return ci;
    }

    public void suppressResizeConfigChanges(boolean suppress) throws RemoteException {
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "suppressResizeConfigChanges()");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mSuppressResizeConfigChanges = suppress;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void moveTasksToFullscreenStack(int fromStackId, boolean onTop) {
        enforceCallingPermission("android.permission.MANAGE_ACTIVITY_STACKS", "moveTasksToFullscreenStack()");
        if (fromStackId == 0) {
            throw new IllegalArgumentException("You can't move tasks from the home stack.");
        }
        synchronized (this) {
            long origId;
            try {
                boostPriorityForLockedSection();
                origId = Binder.clearCallingIdentity();
                this.mStackSupervisor.moveTasksToFullscreenStackLocked(fromStackId, onTop);
                Binder.restoreCallingIdentity(origId);
            } catch (Throwable th) {
                resetPriorityAfterLockedSection();
            }
        }
        resetPriorityAfterLockedSection();
    }

    public void updatePersistentConfiguration(Configuration values) {
        enforceCallingPermission("android.permission.CHANGE_CONFIGURATION", "updateConfiguration()");
        enforceWriteSettingsPermission("updateConfiguration()");
        if (values == null) {
            throw new NullPointerException("Configuration must not be null");
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                updateConfigurationLocked(values, null, false, true, this.mUserController.getCurrentUserIdLocked());
                Binder.restoreCallingIdentity(origId);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private void updateFontScaleIfNeeded() {
        int currentUserId;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                currentUserId = this.mUserController.getCurrentUserIdLocked();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        float scaleFactor = System.getFloatForUser(this.mContext.getContentResolver(), "font_scale", 1.0f, currentUserId);
        if (this.mConfiguration.fontScale != scaleFactor) {
            Configuration configuration = this.mWindowManager.computeNewConfiguration();
            configuration.fontScale = scaleFactor;
            updatePersistentConfiguration(configuration);
        }
    }

    private void enforceWriteSettingsPermission(String func) {
        int uid = Binder.getCallingUid();
        if (uid != 0 && !Settings.checkAndNoteWriteSettingsOperation(this.mContext, uid, Settings.getPackageNameForUid(this.mContext, uid), false)) {
            String msg = "Permission Denial: " + func + " from pid=" + Binder.getCallingPid() + ", uid=" + uid + " requires " + "android.permission.WRITE_SETTINGS";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
    }

    public void updateConfiguration(Configuration values) {
        enforceCallingPermission("android.permission.CHANGE_CONFIGURATION", "updateConfiguration()");
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if (values == null && this.mWindowManager != null) {
                    values = this.mWindowManager.computeNewConfiguration();
                }
                if (this.mWindowManager != null) {
                    this.mProcessList.applyDisplaySize(this.mWindowManager);
                }
                long origId = Binder.clearCallingIdentity();
                if (values != null) {
                    System.clearConfiguration(values);
                }
                updateConfigurationLocked(values, null, false);
                Binder.restoreCallingIdentity(origId);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    void updateUserConfigurationLocked() {
        Configuration configuration = new Configuration(this.mConfiguration);
        System.adjustConfigurationForUser(this.mContext.getContentResolver(), configuration, this.mUserController.getCurrentUserIdLocked(), System.canWrite(this.mContext));
        updateConfigurationLocked(configuration, null, false);
    }

    boolean updateConfigurationLocked(Configuration values, ActivityRecord starting, boolean initLocale) {
        return updateConfigurationLocked(values, starting, initLocale, false, -10000);
    }

    private boolean updateConfigurationLocked(Configuration values, ActivityRecord starting, boolean initLocale, boolean persistent, int userId) {
        int changes = 0;
        if (this.mWindowManager != null) {
            this.mWindowManager.deferSurfaceLayout();
        }
        if (values != null) {
            Configuration configuration = new Configuration(this.mConfiguration);
            changes = configuration.updateFrom(values);
            if (changes != 0) {
                if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                    Slog.i(TAG_CONFIGURATION, "Updating configuration to: " + values);
                }
                boolean z = false;
                if (this.mConfiguration.extraConfig.getConfigItem(1) != configuration.extraConfig.getConfigItem(1)) {
                    int currentUserId = this.mUserController.getCurrentUserIdLocked();
                    Slog.i(TAG, "updateConfigurationLocked  currentUserId " + currentUserId);
                    z = Process.updateHwThemeZipsAndSomeIcons(currentUserId);
                    configuration.extraConfig.setConfigItem(3, currentUserId);
                    configuration.extraConfig.setConfigItem(4, 1);
                }
                EventLog.writeEvent(EventLogTags.CONFIGURATION_CHANGED, changes);
                if (!(initLocale || values.getLocales().isEmpty() || !values.userSetLocale)) {
                    LocaleList locales = values.getLocales();
                    int bestLocaleIndex = 0;
                    if (locales.size() > 1) {
                        if (this.mSupportedSystemLocales == null) {
                            this.mSupportedSystemLocales = Resources.getSystem().getAssets().getLocales();
                        }
                        bestLocaleIndex = Math.max(0, locales.getFirstMatchIndex(this.mSupportedSystemLocales));
                    }
                    SystemProperties.set("persist.sys.locale", locales.get(bestLocaleIndex).toLanguageTag());
                    LocaleList.setDefault(locales, bestLocaleIndex);
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(47, locales.get(bestLocaleIndex)));
                }
                HwThemeManager.updateSimpleUIConfig(this.mContext.getContentResolver(), configuration, changes);
                this.mConfigurationSeq++;
                if (this.mConfigurationSeq <= 0) {
                    this.mConfigurationSeq = 1;
                }
                configuration.seq = this.mConfigurationSeq;
                this.mConfiguration = configuration;
                Slog.i(TAG, "Config changes=" + Integer.toHexString(changes) + " " + configuration);
                this.mUsageStatsService.reportConfigurationChange(configuration, this.mUserController.getCurrentUserIdLocked());
                configuration = new Configuration(this.mConfiguration);
                if (z) {
                    this.mConfiguration.extraConfig.setConfigItem(4, 0);
                }
                this.mShowDialogs = shouldShowDialogs(configuration, this.mInVrMode);
                AttributeCache ac = AttributeCache.instance();
                if (ac != null) {
                    ac.updateConfiguration(configuration);
                }
                this.mSystemThread.applyConfigurationToResources(configuration);
                if (persistent && System.hasInterestingConfigurationChanges(changes)) {
                    Message msg = this.mHandler.obtainMessage(4);
                    msg.obj = new Configuration(configuration);
                    msg.arg1 = userId;
                    this.mHandler.sendMessage(msg);
                }
                if ((changes & 4096) != 0) {
                    this.mUiHandler.sendEmptyMessage(70);
                    killAllBackgroundProcessesExcept(24, 4);
                }
                for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
                    ProcessRecord app = (ProcessRecord) this.mLruProcesses.get(i);
                    try {
                        if (app.thread != null) {
                            if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                                Slog.v(TAG_CONFIGURATION, "Sending to proc " + app.processName + " new config " + this.mConfiguration);
                            }
                            app.thread.scheduleConfigurationChanged(configuration);
                        }
                    } catch (Exception e) {
                    }
                }
                Intent intent = new Intent("android.intent.action.CONFIGURATION_CHANGED");
                intent.addFlags(1879048192);
                broadcastIntentLocked(null, null, intent, null, null, 0, null, null, null, -1, null, false, false, MY_PID, 1000, -1);
                if ((changes & 4) != 0) {
                    ShortcutServiceInternal shortcutService = (ShortcutServiceInternal) LocalServices.getService(ShortcutServiceInternal.class);
                    if (shortcutService != null) {
                        shortcutService.onSystemLocaleChangedNoLock();
                    }
                    intent = new Intent("android.intent.action.LOCALE_CHANGED");
                    intent.addFlags(268435456);
                    if (!this.mProcessesReady) {
                        intent.addFlags(1073741824);
                    }
                    broadcastIntentLocked(null, null, intent, null, null, 0, null, null, null, -1, null, false, false, MY_PID, 1000, -1);
                }
            }
            if (this.mWindowManager != null) {
                int[] resizedStacks = this.mWindowManager.setNewConfiguration(this.mConfiguration);
                if (resizedStacks != null) {
                    for (int stackId : resizedStacks) {
                        this.mStackSupervisor.resizeStackLocked(stackId, this.mWindowManager.getBoundsForNewConfiguration(stackId), null, null, false, false, false);
                    }
                }
            }
        }
        boolean z2 = true;
        ActivityStack mainStack = this.mStackSupervisor.getFocusedStack();
        if (mainStack != null) {
            if (changes != 0 && starting == null) {
                starting = mainStack.topRunningActivityLocked();
            }
            if (starting != null) {
                z2 = mainStack.ensureActivityConfigurationLocked(starting, changes, false);
                this.mStackSupervisor.ensureActivitiesVisibleLocked(starting, changes, false);
            }
        }
        if (this.mWindowManager != null) {
            this.mWindowManager.continueSurfaceLayout();
        }
        return z2;
    }

    private static final boolean shouldShowDialogs(Configuration config, boolean inVrMode) {
        boolean inputMethodExists = (config.keyboard == 1 && config.touchscreen == 1) ? config.navigation != 1 : true;
        boolean uiIsNotCarType = (config.uiMode & 15) != 3;
        if (inputMethodExists && uiIsNotCarType && !inVrMode) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean shouldUpRecreateTask(IBinder token, String destAffinity) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord srec = ActivityRecord.forTokenLocked(token);
                if (srec != null) {
                    boolean shouldUpRecreateTaskLocked = srec.task.stack.shouldUpRecreateTaskLocked(srec, destAffinity);
                } else {
                    resetPriorityAfterLockedSection();
                    return false;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean navigateUpTo(IBinder token, Intent destIntent, int resultCode, Intent resultData) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord r = ActivityRecord.forTokenLocked(token);
                if (r != null) {
                    boolean navigateUpToLocked = r.task.stack.navigateUpToLocked(r, destIntent, resultCode, resultData);
                } else {
                    resetPriorityAfterLockedSection();
                    return false;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public int getLaunchedFromUid(IBinder activityToken) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord srec = ActivityRecord.forTokenLocked(activityToken);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        if (srec == null) {
            return -1;
        }
        return srec.launchedFromUid;
    }

    public String getLaunchedFromPackage(IBinder activityToken) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                ActivityRecord srec = ActivityRecord.forTokenLocked(activityToken);
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        if (srec == null) {
            return null;
        }
        return srec.launchedFromPackage;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private BroadcastQueue isReceivingBroadcast(ProcessRecord app) {
        BroadcastRecord r = app.curReceiver;
        if (r != null) {
            return r.queue;
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                BroadcastQueue[] broadcastQueueArr = this.mBroadcastQueues;
                int i = 0;
                int length = broadcastQueueArr.length;
                while (i < length) {
                    BroadcastQueue queue = broadcastQueueArr[i];
                    r = queue.mPendingBroadcast;
                    if (r == null || r.curApp != app) {
                        i++;
                    }
                }
                resetPriorityAfterLockedSection();
                return null;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    Association startAssociationLocked(int sourceUid, String sourceProcess, int sourceState, int targetUid, ComponentName targetComponent, String targetProcess) {
        if (!this.mTrackingAssociations) {
            return null;
        }
        ArrayMap<ComponentName, SparseArray<ArrayMap<String, Association>>> components = (ArrayMap) this.mAssociations.get(targetUid);
        if (components == null) {
            components = new ArrayMap();
            this.mAssociations.put(targetUid, components);
        }
        SparseArray<ArrayMap<String, Association>> sourceUids = (SparseArray) components.get(targetComponent);
        if (sourceUids == null) {
            sourceUids = new SparseArray();
            components.put(targetComponent, sourceUids);
        }
        ArrayMap<String, Association> sourceProcesses = (ArrayMap) sourceUids.get(sourceUid);
        if (sourceProcesses == null) {
            sourceProcesses = new ArrayMap();
            sourceUids.put(sourceUid, sourceProcesses);
        }
        Association ass = (Association) sourceProcesses.get(sourceProcess);
        if (ass == null) {
            ass = new Association(sourceUid, sourceProcess, targetUid, targetComponent, targetProcess);
            sourceProcesses.put(sourceProcess, ass);
        }
        ass.mCount++;
        ass.mNesting++;
        if (ass.mNesting == 1) {
            long uptimeMillis = SystemClock.uptimeMillis();
            ass.mLastStateUptime = uptimeMillis;
            ass.mStartTime = uptimeMillis;
            ass.mLastState = sourceState;
        }
        return ass;
    }

    void stopAssociationLocked(int sourceUid, String sourceProcess, int targetUid, ComponentName targetComponent) {
        if (this.mTrackingAssociations) {
            ArrayMap<ComponentName, SparseArray<ArrayMap<String, Association>>> components = (ArrayMap) this.mAssociations.get(targetUid);
            if (components != null) {
                SparseArray<ArrayMap<String, Association>> sourceUids = (SparseArray) components.get(targetComponent);
                if (sourceUids != null) {
                    ArrayMap<String, Association> sourceProcesses = (ArrayMap) sourceUids.get(sourceUid);
                    if (sourceProcesses != null) {
                        Association ass = (Association) sourceProcesses.get(sourceProcess);
                        if (ass != null && ass.mNesting > 0) {
                            ass.mNesting--;
                            if (ass.mNesting == 0) {
                                long uptime = SystemClock.uptimeMillis();
                                ass.mTime += uptime - ass.mStartTime;
                                long[] jArr = ass.mStateTimes;
                                int i = ass.mLastState + 1;
                                jArr[i] = jArr[i] + (uptime - ass.mLastStateUptime);
                                ass.mLastState = 18;
                            }
                        }
                    }
                }
            }
        }
    }

    private void noteUidProcessState(int uid, int state) {
        this.mBatteryStatsService.noteUidProcessState(uid, state);
        if (this.mTrackingAssociations) {
            int N1 = this.mAssociations.size();
            for (int i1 = 0; i1 < N1; i1++) {
                ArrayMap<ComponentName, SparseArray<ArrayMap<String, Association>>> targetComponents = (ArrayMap) this.mAssociations.valueAt(i1);
                int N2 = targetComponents.size();
                for (int i2 = 0; i2 < N2; i2++) {
                    ArrayMap<String, Association> sourceProcesses = (ArrayMap) ((SparseArray) targetComponents.valueAt(i2)).get(uid);
                    if (sourceProcesses != null) {
                        int N4 = sourceProcesses.size();
                        for (int i4 = 0; i4 < N4; i4++) {
                            Association ass = (Association) sourceProcesses.valueAt(i4);
                            if (ass.mNesting >= 1) {
                                long uptime = SystemClock.uptimeMillis();
                                long[] jArr = ass.mStateTimes;
                                int i = ass.mLastState + 1;
                                jArr[i] = jArr[i] + (uptime - ass.mLastStateUptime);
                                ass.mLastState = state;
                                ass.mLastStateUptime = uptime;
                            }
                        }
                    }
                }
            }
        }
    }

    protected int computeOomAdjLocked(ProcessRecord app, int cachedAdj, ProcessRecord TOP_APP, boolean doingAll, long now) {
        if (this.mAdjSeq == app.adjSeq) {
            return app.curRawAdj;
        }
        if (app.thread == null) {
            app.adjSeq = this.mAdjSeq;
            app.curSchedGroup = 0;
            app.curProcState = 16;
            app.curRawAdj = 906;
            app.curAdj = 906;
            return 906;
        }
        app.adjTypeCode = 0;
        app.adjSource = null;
        app.adjTarget = null;
        app.empty = false;
        app.cached = false;
        int activitiesSize = app.activities.size();
        boolean bConnectTopApp;
        int j;
        int is;
        ServiceRecord s;
        int conni;
        ArrayList<ConnectionRecord> clist;
        int i;
        ConnectionRecord cr;
        int provi;
        ContentProviderRecord cpr;
        if (app.maxAdj <= 0) {
            bConnectTopApp = false;
            app.adjType = "fixed";
            app.adjSeq = this.mAdjSeq;
            app.curRawAdj = app.maxAdj;
            app.foregroundActivities = false;
            app.curSchedGroup = 1;
            app.curProcState = 0;
            app.systemNoUi = true;
            if (app == TOP_APP) {
                app.systemNoUi = false;
                app.curSchedGroup = 2;
                app.adjType = "pers-top-activity";
            } else if (activitiesSize > 0) {
                for (j = 0; j < activitiesSize; j++) {
                    if (((ActivityRecord) app.activities.get(j)).visible) {
                        app.systemNoUi = false;
                    }
                }
            }
            if (!app.systemNoUi) {
                app.curProcState = 1;
                app.curSchedGroup = 5;
            }
            for (is = app.services.size() - 1; is >= 0; is--) {
                s = (ServiceRecord) app.services.valueAt(is);
                for (conni = s.connections.size() - 1; conni >= 0; conni--) {
                    clist = (ArrayList) s.connections.valueAt(conni);
                    for (i = 0; i < clist.size(); i++) {
                        cr = (ConnectionRecord) clist.get(i);
                        ProcessRecord client = cr.binding.client;
                        if ((cr.flags & 32) == 0 && (cr.flags & 4) == 0 && client.curSchedGroup == 5) {
                            app.curSchedGroup = 5;
                            bConnectTopApp = true;
                        }
                    }
                }
            }
            for (provi = app.pubProviders.size() - 1; provi >= 0; provi--) {
                cpr = (ContentProviderRecord) app.pubProviders.valueAt(provi);
                for (i = cpr.connections.size() - 1; i >= 0; i--) {
                    if (((ContentProviderConnection) cpr.connections.get(i)).client.curSchedGroup == 5) {
                        app.curSchedGroup = 5;
                        bConnectTopApp = true;
                    }
                }
            }
            if (app.curSchedGroup != 5 && this.mCpusetSwitch) {
                setWhiteListProcessGroup(app, TOP_APP, bConnectTopApp);
            }
            int i2 = app.maxAdj;
            app.curAdj = i2;
            return i2;
        }
        int adj;
        int schedGroup;
        int procState;
        app.systemNoUi = false;
        int PROCESS_STATE_CUR_TOP = this.mTopProcessState;
        boolean foregroundActivities = false;
        if (app == TOP_APP) {
            adj = 0;
            schedGroup = 2;
            app.adjType = "top-activity";
            foregroundActivities = true;
            procState = PROCESS_STATE_CUR_TOP;
        } else if (app.instrumentationClass != null) {
            adj = 0;
            schedGroup = 1;
            app.adjType = "instrumentation";
            procState = 4;
        } else {
            BroadcastQueue queue = isReceivingBroadcast(app);
            if (queue != null) {
                adj = 0;
                schedGroup = (queue == this.mFgBroadcastQueue || isThirdPartyAppFGBroadcastQueue(queue) || isKeyAppFGBroadcastQueue(queue)) ? 1 : 0;
                app.adjType = "broadcast";
                procState = 11;
            } else if (app.executingServices.size() > 0) {
                adj = 0;
                schedGroup = app.execServicesFg ? 1 : 0;
                app.adjType = "exec-service";
                procState = 10;
            } else {
                schedGroup = 0;
                adj = cachedAdj;
                procState = 16;
                app.cached = true;
                app.empty = true;
                app.adjType = "cch-empty";
            }
        }
        if (!foregroundActivities && activitiesSize > 0) {
            int minLayer = 99;
            j = 0;
            while (j < activitiesSize) {
                ActivityRecord r = (ActivityRecord) app.activities.get(j);
                if (r.app != app) {
                    Log.e(TAG, "Found activity " + r + " in proc activity list using " + r.app + " instead of expected " + app);
                    if (r.app == null || r.app.uid == app.uid) {
                        r.app = app;
                    } else {
                        j++;
                    }
                }
                if (r.visible) {
                    if (adj > 100) {
                        adj = 100;
                        app.adjType = "visible";
                    }
                    if (procState > PROCESS_STATE_CUR_TOP) {
                        procState = PROCESS_STATE_CUR_TOP;
                    }
                    schedGroup = 1;
                    app.cached = false;
                    app.empty = false;
                    foregroundActivities = true;
                    if (r.task != null) {
                        int layer = r.task.mLayerRank;
                        if (layer >= 0 && 99 > layer) {
                            minLayer = layer;
                        }
                    }
                    if (adj == 100) {
                        adj += minLayer;
                    }
                } else if (r.state == ActivityState.PAUSING || r.state == ActivityState.PAUSED) {
                    if (adj > FIRST_BROADCAST_QUEUE_MSG) {
                        adj = FIRST_BROADCAST_QUEUE_MSG;
                        app.adjType = "pausing";
                    }
                    if (procState > PROCESS_STATE_CUR_TOP) {
                        procState = PROCESS_STATE_CUR_TOP;
                    }
                    schedGroup = 1;
                    app.cached = false;
                    app.empty = false;
                    foregroundActivities = true;
                    j++;
                } else {
                    if (r.state == ActivityState.STOPPING) {
                        if (adj > FIRST_BROADCAST_QUEUE_MSG) {
                            adj = FIRST_BROADCAST_QUEUE_MSG;
                            app.adjType = "stopping";
                        }
                        if (!r.finishing && procState > 13) {
                            procState = 13;
                        }
                        app.cached = false;
                        app.empty = false;
                        foregroundActivities = true;
                    } else if (procState > 14) {
                        procState = 14;
                        app.adjType = "cch-act";
                    }
                    j++;
                }
            }
            if (adj == 100) {
                adj += minLayer;
            }
        }
        if (adj > FIRST_BROADCAST_QUEUE_MSG || procState > 4) {
            if (app.foregroundServices) {
                adj = FIRST_BROADCAST_QUEUE_MSG;
                procState = 4;
                app.cached = false;
                app.adjType = "fg-service";
                if (!this.mCpusetSwitch) {
                    schedGroup = 1;
                }
            } else if (app.forcingToForeground != null) {
                adj = FIRST_BROADCAST_QUEUE_MSG;
                procState = 6;
                app.cached = false;
                app.adjType = "force-fg";
                app.adjSource = app.forcingToForeground;
                schedGroup = 1;
            }
        }
        if (app == this.mHeavyWeightProcess) {
            if (adj > 400) {
                adj = 400;
                schedGroup = 0;
                app.cached = false;
                app.adjType = "heavy";
            }
            if (procState > 9) {
                procState = 9;
            }
        }
        if (app == this.mHomeProcess) {
            if (adj > 600) {
                adj = 600;
                schedGroup = 0;
                app.cached = false;
                app.adjType = "home";
            }
            if (procState > 12) {
                procState = 12;
            }
        }
        if (app == this.mPreviousProcess && app.activities.size() > 0) {
            if (adj > 700) {
                adj = 700;
                schedGroup = 0;
                app.cached = false;
                app.adjType = "previous";
            }
            if (procState > 13) {
                procState = 13;
            }
        }
        app.adjSeq = this.mAdjSeq;
        app.curRawAdj = adj;
        app.hasStartedServices = false;
        if (this.mBackupTarget != null && app == this.mBackupTarget.app) {
            if (adj > FIRST_COMPAT_MODE_MSG) {
                if (ActivityManagerDebugConfig.DEBUG_BACKUP) {
                    Slog.v(TAG_BACKUP, "oom BACKUP_APP_ADJ for " + app);
                }
                adj = FIRST_COMPAT_MODE_MSG;
                if (procState > 7) {
                    procState = 7;
                }
                app.adjType = "backup";
                app.cached = false;
            }
            if (procState > 8) {
                procState = 8;
            }
        }
        boolean mayBeTop = false;
        bConnectTopApp = false;
        for (is = app.services.size() - 1; is >= 0 && (adj > 0 || schedGroup == 0 || procState > 2); is--) {
            s = (ServiceRecord) app.services.valueAt(is);
            if (s.startRequested) {
                app.hasStartedServices = true;
                if (procState > 10) {
                    procState = 10;
                }
                if (!app.hasShownUi || app == this.mHomeProcess) {
                    if (now < s.lastActivity + 1800000 && adj > 500) {
                        adj = 500;
                        app.adjType = "started-services";
                        app.cached = false;
                    }
                    if (adj > 500) {
                        app.adjType = "cch-started-services";
                    }
                } else if (adj > 500) {
                    app.adjType = "cch-started-ui-services";
                }
            }
            for (conni = s.connections.size() - 1; conni >= 0 && (adj > 0 || schedGroup == 0 || procState > 2); conni--) {
                clist = (ArrayList) s.connections.valueAt(conni);
                for (i = 0; i < clist.size() && (adj > 0 || schedGroup == 0 || procState > 2); i++) {
                    int clientAdj;
                    int clientProcState;
                    cr = (ConnectionRecord) clist.get(i);
                    if (cr.binding.client != app) {
                        if ((cr.flags & 32) == 0) {
                            client = cr.binding.client;
                            if (this.mCpusetSwitch) {
                                setCurProcessGroup(app, schedGroup);
                            }
                            clientAdj = computeOomAdjLocked(client, cachedAdj, TOP_APP, doingAll, now);
                            clientProcState = client.curProcState;
                            if (clientProcState >= 14) {
                                clientProcState = 16;
                            }
                            String str = null;
                            if ((cr.flags & 16) != 0) {
                                if (app.hasShownUi && app != this.mHomeProcess) {
                                    if (adj > clientAdj) {
                                        str = "cch-bound-ui-services";
                                    }
                                    app.cached = false;
                                    clientAdj = adj;
                                    clientProcState = procState;
                                } else if (now >= s.lastActivity + 1800000) {
                                    if (adj > clientAdj) {
                                        str = "cch-bound-services";
                                    }
                                    clientAdj = adj;
                                }
                            }
                            if (adj > clientAdj) {
                                if (!app.hasShownUi || app == this.mHomeProcess || clientAdj <= FIRST_BROADCAST_QUEUE_MSG) {
                                    if ((cr.flags & 72) != 0) {
                                        adj = clientAdj >= -700 ? clientAdj : -700;
                                    } else if ((cr.flags & 1073741824) != 0 && clientAdj < FIRST_BROADCAST_QUEUE_MSG && adj > FIRST_BROADCAST_QUEUE_MSG) {
                                        adj = FIRST_BROADCAST_QUEUE_MSG;
                                    } else if (clientAdj >= FIRST_BROADCAST_QUEUE_MSG) {
                                        adj = clientAdj;
                                    } else if (adj > 100) {
                                        adj = Math.max(clientAdj, 100);
                                    }
                                    if (!client.cached) {
                                        app.cached = false;
                                    }
                                    str = "service";
                                } else {
                                    str = "cch-bound-ui-services";
                                }
                            }
                            if ((cr.flags & 4) == 0) {
                                if (client.curSchedGroup > schedGroup) {
                                    if ((cr.flags & 64) != 0) {
                                        schedGroup = client.curSchedGroup;
                                    } else {
                                        schedGroup = 1;
                                    }
                                }
                                if (client == TOP_APP) {
                                    bConnectTopApp = true;
                                }
                                if (clientProcState <= 2) {
                                    if (clientProcState == 2) {
                                        mayBeTop = true;
                                        clientProcState = 16;
                                    } else if ((cr.flags & 67108864) != 0) {
                                        clientProcState = 3;
                                    } else if (this.mWakefulness != 1 || (cr.flags & 33554432) == 0) {
                                        clientProcState = 6;
                                    } else {
                                        clientProcState = 3;
                                    }
                                }
                            } else if (clientProcState < 7) {
                                clientProcState = 7;
                            }
                            if (procState > clientProcState) {
                                procState = clientProcState;
                            }
                            if (procState < 7 && (cr.flags & 536870912) != 0) {
                                app.pendingUiClean = true;
                            }
                            if (str != null) {
                                app.adjType = str;
                                app.adjTypeCode = 2;
                                app.adjSource = cr.binding.client;
                                app.adjSourceProcState = clientProcState;
                                app.adjTarget = s.name;
                            }
                        }
                        if ((cr.flags & 134217728) != 0) {
                            app.treatLikeActivity = true;
                        }
                        ActivityRecord a = cr.activity;
                        if (!((cr.flags & 128) == 0 || a == null || adj <= 0)) {
                            if (!(a.visible || a.state == ActivityState.RESUMED)) {
                                if (a.state == ActivityState.PAUSING) {
                                }
                            }
                            adj = 0;
                            if ((cr.flags & 4) == 0) {
                                if ((cr.flags & 64) != 0) {
                                    schedGroup = 3;
                                } else {
                                    schedGroup = 1;
                                }
                            }
                            app.cached = false;
                            app.adjType = "service";
                            app.adjTypeCode = 2;
                            app.adjSource = a;
                            app.adjSourceProcState = procState;
                            app.adjTarget = s.name;
                        }
                    }
                }
            }
        }
        for (provi = app.pubProviders.size() - 1; provi >= 0 && (adj > 0 || schedGroup == 0 || procState > 2); provi--) {
            cpr = (ContentProviderRecord) app.pubProviders.valueAt(provi);
            for (i = cpr.connections.size() - 1; i >= 0 && (adj > 0 || schedGroup == 0 || procState > 2); i--) {
                client = ((ContentProviderConnection) cpr.connections.get(i)).client;
                if (client != app) {
                    if (this.mCpusetSwitch) {
                        setCurProcessGroup(app, schedGroup);
                    }
                    clientAdj = computeOomAdjLocked(client, cachedAdj, TOP_APP, doingAll, now);
                    clientProcState = client.curProcState;
                    if (clientProcState >= 14) {
                        clientProcState = 16;
                    }
                    if (adj > clientAdj) {
                        if (!app.hasShownUi || app == this.mHomeProcess || clientAdj <= FIRST_BROADCAST_QUEUE_MSG) {
                            adj = clientAdj > 0 ? clientAdj : 0;
                            app.adjType = "provider";
                        } else {
                            app.adjType = "cch-ui-provider";
                        }
                        app.cached &= client.cached;
                        app.adjTypeCode = 1;
                        app.adjSource = client;
                        app.adjSourceProcState = clientProcState;
                        app.adjTarget = cpr.name;
                    }
                    if (clientProcState <= 2) {
                        if (clientProcState == 2) {
                            mayBeTop = true;
                            clientProcState = 16;
                        } else {
                            clientProcState = 3;
                        }
                    }
                    if (procState > clientProcState) {
                        procState = clientProcState;
                    }
                    if (client.curSchedGroup > schedGroup) {
                        schedGroup = 1;
                    }
                    if (client == TOP_APP) {
                        bConnectTopApp = true;
                    }
                }
            }
            if (cpr.hasExternalProcessHandles()) {
                if (adj > 0) {
                    adj = 0;
                    schedGroup = 1;
                    app.cached = false;
                    app.adjType = "provider";
                    app.adjTarget = cpr.name;
                }
                if (procState > 6) {
                    procState = 6;
                }
            }
        }
        if (app.lastProviderTime > 0 && app.lastProviderTime + 20000 > now) {
            if (adj > 700) {
                adj = 700;
                schedGroup = 0;
                app.cached = false;
                app.adjType = "provider";
            }
            if (procState > 13) {
                procState = 13;
            }
        }
        if (mayBeTop && procState > 2) {
            switch (procState) {
                case 6:
                case 7:
                case 10:
                    procState = 3;
                    break;
                default:
                    procState = 2;
                    break;
            }
        }
        if (procState >= 16) {
            if (app.hasClientActivities) {
                procState = 15;
                app.adjType = "cch-client-act";
            } else if (app.treatLikeActivity) {
                procState = 14;
                app.adjType = "cch-as-act";
            }
        }
        if (adj == 500) {
            if (doingAll) {
                app.serviceb = this.mNewNumAServiceProcs > this.mNumServiceProcs / 3;
                this.mNewNumServiceProcs++;
                if (app.serviceb) {
                    app.serviceHighRam = false;
                } else if (this.mLastMemoryLevel <= 0 || app.lastPss < this.mProcessList.getCachedRestoreThresholdKb()) {
                    this.mNewNumAServiceProcs++;
                } else {
                    app.serviceHighRam = true;
                    app.serviceb = true;
                }
            }
            if (app.serviceb) {
                adj = 800;
            }
        }
        app.curRawAdj = adj;
        if (adj > app.maxAdj) {
            adj = app.maxAdj;
            if (app.maxAdj <= FIRST_BROADCAST_QUEUE_MSG) {
                schedGroup = 1;
            }
        }
        app.curAdj = app.modifyRawOomAdj(adj);
        app.curSchedGroup = schedGroup;
        app.curProcState = procState;
        app.foregroundActivities = foregroundActivities;
        if (this.mCpusetSwitch) {
            setWhiteListProcessGroup(app, TOP_APP, bConnectTopApp);
        }
        return app.curRawAdj;
    }

    void recordPssSampleLocked(ProcessRecord proc, int procState, long pss, long uss, long swapPss, long now) {
        EventLogTags.writeAmPss(proc.pid, proc.uid, proc.processName, 1024 * pss, 1024 * uss, 1024 * swapPss);
        proc.lastPssTime = now;
        proc.baseProcessTracker.addPss(pss, uss, true, proc.pkgList);
        if (ActivityManagerDebugConfig.DEBUG_PSS) {
            Slog.d(TAG_PSS, "PSS of " + proc.toShortString() + ": " + pss + " lastPss=" + proc.lastPss + " state=" + ProcessList.makeProcStateString(procState));
        }
        if (proc.initialIdlePss == 0) {
            proc.initialIdlePss = pss;
        }
        proc.lastPss = pss;
        proc.lastSwapPss = swapPss;
        if (procState >= 12) {
            proc.lastCachedPss = pss;
            proc.lastCachedSwapPss = swapPss;
        }
        SparseArray<Pair<Long, String>> watchUids = (SparseArray) this.mMemWatchProcesses.getMap().get(proc.processName);
        Long l = null;
        if (watchUids != null) {
            Pair<Long, String> val = (Pair) watchUids.get(proc.uid);
            if (val == null) {
                val = (Pair) watchUids.get(0);
            }
            if (val != null) {
                l = val.first;
            }
        }
        if (l != null && 1024 * pss >= l.longValue() && proc.thread != null && this.mMemWatchDumpProcName == null) {
            boolean isDebuggable = "1".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0"));
            if (!(isDebuggable || (proc.info.flags & 2) == 0)) {
                isDebuggable = true;
            }
            if (isDebuggable) {
                Slog.w(TAG, "Process " + proc + " exceeded pss limit " + l + "; reporting");
                ProcessRecord myProc = proc;
                final File heapdumpFile = DumpHeapProvider.getJavaFile();
                this.mMemWatchDumpProcName = proc.processName;
                this.mMemWatchDumpFile = heapdumpFile.toString();
                this.mMemWatchDumpPid = proc.pid;
                this.mMemWatchDumpUid = proc.uid;
                final ProcessRecord processRecord = proc;
                BackgroundThread.getHandler().post(new Runnable() {
                    public void run() {
                        ActivityManagerService.this.revokeUriPermission(ActivityThread.currentActivityThread().getApplicationThread(), DumpHeapActivity.JAVA_URI, 3, UserHandle.myUserId());
                        ParcelFileDescriptor parcelFileDescriptor = null;
                        try {
                            heapdumpFile.delete();
                            parcelFileDescriptor = ParcelFileDescriptor.open(heapdumpFile, 771751936);
                            IApplicationThread thread = processRecord.thread;
                            if (thread != null) {
                                try {
                                    if (ActivityManagerDebugConfig.DEBUG_PSS) {
                                        Slog.d(ActivityManagerService.TAG_PSS, "Requesting dump heap from " + processRecord + " to " + heapdumpFile);
                                    }
                                    thread.dumpHeap(true, heapdumpFile.toString(), parcelFileDescriptor);
                                } catch (RemoteException e) {
                                }
                            }
                            if (parcelFileDescriptor != null) {
                                try {
                                    parcelFileDescriptor.close();
                                } catch (IOException e2) {
                                }
                            }
                        } catch (FileNotFoundException e3) {
                            e3.printStackTrace();
                            if (parcelFileDescriptor != null) {
                                try {
                                    parcelFileDescriptor.close();
                                } catch (IOException e4) {
                                }
                            }
                        } catch (Throwable th) {
                            if (parcelFileDescriptor != null) {
                                try {
                                    parcelFileDescriptor.close();
                                } catch (IOException e5) {
                                }
                            }
                        }
                    }
                });
                return;
            }
            Slog.w(TAG, "Process " + proc + " exceeded pss limit " + l + ", but debugging not enabled");
        }
    }

    void requestPssLocked(ProcessRecord proc, int procState) {
        if (!this.mPendingPssProcesses.contains(proc)) {
            if (this.mPendingPssProcesses.size() == 0) {
                this.mBgHandler.sendEmptyMessage(1);
            }
            if (ActivityManagerDebugConfig.DEBUG_PSS) {
                Slog.d(TAG_PSS, "Requesting PSS of: " + proc);
            }
            proc.pssProcState = procState;
            this.mPendingPssProcesses.add(proc);
        }
    }

    void requestPssAllProcsLocked(long now, boolean always, boolean memLowered) {
        if (!always) {
            if (now < this.mLastFullPssTime + ((long) (memLowered ? FULL_PSS_LOWERED_INTERVAL : 600000))) {
                return;
            }
        }
        if (ActivityManagerDebugConfig.DEBUG_PSS) {
            Slog.d(TAG_PSS, "Requesting PSS of all procs!  memLowered=" + memLowered);
        }
        this.mLastFullPssTime = now;
        this.mFullPssPending = true;
        this.mPendingPssProcesses.ensureCapacity(this.mLruProcesses.size());
        this.mPendingPssProcesses.clear();
        for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
            ProcessRecord app = (ProcessRecord) this.mLruProcesses.get(i);
            if (!(app.thread == null || app.curProcState == -1 || ((!memLowered && now <= app.lastStateTime + LocationFudger.FASTEST_INTERVAL_MS) || app.curProcState < 0))) {
                app.pssProcState = app.setProcState;
                app.nextPssTime = ProcessList.computeNextPssTime(app.curProcState, true, this.mTestPssMode, isSleepingLocked(), now);
                this.mPendingPssProcesses.add(app);
            }
        }
        this.mBgHandler.sendEmptyMessage(1);
    }

    public void setTestPssMode(boolean enabled) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                this.mTestPssMode = enabled;
                if (enabled) {
                    requestPssAllProcsLocked(SystemClock.uptimeMillis(), true, true);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    final void performAppGcLocked(ProcessRecord app) {
        try {
            app.lastRequestedGc = SystemClock.uptimeMillis();
            if (app.thread == null) {
                return;
            }
            if (app.reportLowMemory) {
                app.reportLowMemory = false;
                app.thread.scheduleLowMemory();
                return;
            }
            app.thread.processInBackground();
        } catch (Exception e) {
        }
    }

    private final boolean canGcNowLocked() {
        boolean processingBroadcasts = false;
        for (BroadcastQueue q : this.mBroadcastQueues) {
            if (q.mParallelBroadcasts.size() != 0 || q.mOrderedBroadcasts.size() != 0) {
                processingBroadcasts = true;
            }
        }
        if (processingBroadcasts) {
            return false;
        }
        return !isSleepingLocked() ? this.mStackSupervisor.allResumedActivitiesIdle() : true;
    }

    final void performAppGcsLocked() {
        if (this.mProcessesToGc.size() > 0 && canGcNowLocked()) {
            while (this.mProcessesToGc.size() > 0) {
                ProcessRecord proc = (ProcessRecord) this.mProcessesToGc.remove(0);
                if (proc.curRawAdj <= FIRST_BROADCAST_QUEUE_MSG) {
                    if (proc.reportLowMemory) {
                    }
                }
                if (proc.lastRequestedGc + 60000 <= SystemClock.uptimeMillis()) {
                    performAppGcLocked(proc);
                    scheduleAppGcsLocked();
                    return;
                }
                addProcessToGcListLocked(proc);
                scheduleAppGcsLocked();
            }
            scheduleAppGcsLocked();
        }
    }

    final void performAppGcsIfAppropriateLocked() {
        if (canGcNowLocked()) {
            performAppGcsLocked();
        } else {
            scheduleAppGcsLocked();
        }
    }

    final void scheduleAppGcsLocked() {
        this.mHandler.removeMessages(5);
        if (this.mProcessesToGc.size() > 0) {
            ProcessRecord proc = (ProcessRecord) this.mProcessesToGc.get(0);
            Message msg = this.mHandler.obtainMessage(5);
            long when = proc.lastRequestedGc + 60000;
            long now = SystemClock.uptimeMillis();
            if (when < now + 5000) {
                when = now + 5000;
            }
            this.mHandler.sendMessageAtTime(msg, when);
        }
    }

    final void addProcessToGcListLocked(ProcessRecord proc) {
        boolean added = false;
        for (int i = this.mProcessesToGc.size() - 1; i >= 0; i--) {
            if (((ProcessRecord) this.mProcessesToGc.get(i)).lastRequestedGc < proc.lastRequestedGc) {
                added = true;
                this.mProcessesToGc.add(i + 1, proc);
                break;
            }
        }
        if (!added) {
            this.mProcessesToGc.add(0, proc);
        }
    }

    final void scheduleAppGcLocked(ProcessRecord app) {
        if (app.lastRequestedGc + 60000 <= SystemClock.uptimeMillis() && !this.mProcessesToGc.contains(app)) {
            addProcessToGcListLocked(app);
            scheduleAppGcsLocked();
        }
    }

    final void checkExcessivePowerUsageLocked(boolean doKills) {
        updateCpuStatsNow();
        BatteryStatsImpl stats = this.mBatteryStatsService.getActiveStatistics();
        boolean doWakeKills = doKills;
        boolean doCpuKills = doKills;
        if (this.mLastPowerCheckRealtime == 0) {
            doWakeKills = false;
        }
        if (this.mLastPowerCheckUptime == 0) {
            doCpuKills = false;
        }
        if (stats.isScreenOn()) {
            doWakeKills = false;
        }
        long curRealtime = SystemClock.elapsedRealtime();
        long realtimeSince = curRealtime - this.mLastPowerCheckRealtime;
        long curUptime = SystemClock.uptimeMillis();
        long uptimeSince = curUptime - this.mLastPowerCheckUptime;
        this.mLastPowerCheckRealtime = curRealtime;
        this.mLastPowerCheckUptime = curUptime;
        if (realtimeSince < ((long) WAKE_LOCK_MIN_CHECK_DURATION)) {
            doWakeKills = false;
        }
        if (uptimeSince < ((long) CPU_MIN_CHECK_DURATION)) {
            doCpuKills = false;
        }
        int i = this.mLruProcesses.size();
        while (i > 0) {
            i--;
            ProcessRecord app = (ProcessRecord) this.mLruProcesses.get(i);
            if (app.setProcState >= 12) {
                long wtime;
                synchronized (stats) {
                    wtime = stats.getProcessWakeTime(app.info.uid, app.pid, curRealtime);
                }
                long wtimeUsed = wtime - app.lastWakeTime;
                long cputimeUsed = app.curCpuTime - app.lastCpuTime;
                if (ActivityManagerDebugConfig.DEBUG_POWER) {
                    StringBuilder stringBuilder = new StringBuilder(128);
                    stringBuilder.append("Wake for ");
                    app.toShortString(stringBuilder);
                    stringBuilder.append(": over ");
                    TimeUtils.formatDuration(realtimeSince, stringBuilder);
                    stringBuilder.append(" used ");
                    TimeUtils.formatDuration(wtimeUsed, stringBuilder);
                    stringBuilder.append(" (");
                    stringBuilder.append((100 * wtimeUsed) / realtimeSince);
                    stringBuilder.append("%)");
                    Slog.i(TAG_POWER, stringBuilder.toString());
                    stringBuilder.setLength(0);
                    stringBuilder.append("CPU for ");
                    app.toShortString(stringBuilder);
                    stringBuilder.append(": over ");
                    TimeUtils.formatDuration(uptimeSince, stringBuilder);
                    stringBuilder.append(" used ");
                    TimeUtils.formatDuration(cputimeUsed, stringBuilder);
                    stringBuilder.append(" (");
                    stringBuilder.append((100 * cputimeUsed) / uptimeSince);
                    stringBuilder.append("%)");
                    Slog.i(TAG_POWER, stringBuilder.toString());
                }
                if (doWakeKills && realtimeSince > 0 && (100 * wtimeUsed) / realtimeSince >= 50) {
                    synchronized (stats) {
                        stats.reportExcessiveWakeLocked(app.info.uid, app.processName, realtimeSince, wtimeUsed);
                    }
                    app.kill("excessive wake held " + wtimeUsed + " during " + realtimeSince, true);
                    app.baseProcessTracker.reportExcessiveWake(app.pkgList);
                } else if (!doCpuKills || uptimeSince <= 0 || (100 * cputimeUsed) / uptimeSince < 25) {
                    app.lastWakeTime = wtime;
                    app.lastCpuTime = app.curCpuTime;
                } else {
                    synchronized (stats) {
                        stats.reportExcessiveCpuLocked(app.info.uid, app.processName, uptimeSince, cputimeUsed);
                    }
                    app.kill("excessive cpu " + cputimeUsed + " during " + uptimeSince, true);
                    app.baseProcessTracker.reportExcessiveCpu(app.pkgList);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final boolean applyOomAdjLocked(ProcessRecord app, boolean doingAll, long now, long nowElapsed) {
        boolean success = true;
        if (app.curRawAdj != app.setRawAdj) {
            app.setRawAdj = app.curRawAdj;
        }
        int changes = 0;
        if (app.curAdj != app.setAdj) {
            if (this.mCpusetSwitch) {
                ProcessList.setOomAdj(app.pid, app.info.uid, app.curAdj, app.processName);
            } else {
                ProcessList.setOomAdj(app.pid, app.info.uid, app.curAdj);
            }
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                Slog.v(TAG_OOM_ADJ, "Set " + app.pid + " " + app.processName + " adj " + app.curAdj + ": " + app.adjType);
            }
            app.setAdj = app.curAdj;
            app.verifiedAdj = -10000;
        }
        if (app.setSchedGroup != app.curSchedGroup) {
            int oldSchedGroup = app.setSchedGroup;
            app.setSchedGroup = app.curSchedGroup;
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                Slog.v(TAG_OOM_ADJ, "Setting sched group of " + app.processName + " to " + app.curSchedGroup);
            }
            if (app.waitingToKill != null && app.curReceiver == null && app.setSchedGroup == 0) {
                app.kill(app.waitingToKill, true);
                success = false;
            } else {
                int processGroup;
                switch (app.curSchedGroup) {
                    case 0:
                        processGroup = 0;
                        break;
                    case 2:
                    case 3:
                        processGroup = 5;
                        break;
                    case 4:
                        processGroup = 6;
                        break;
                    default:
                        processGroup = -1;
                        break;
                }
                long oldId = Binder.clearCallingIdentity();
                Process.setProcessGroup(app.pid, processGroup);
                if (app.curSchedGroup == 2) {
                    if (oldSchedGroup != 2) {
                        if (this.mInVrMode && app.vrThreadTid != 0) {
                            try {
                                Process.setThreadScheduler(app.vrThreadTid, 1073741825, 1);
                            } catch (IllegalArgumentException e) {
                            }
                        }
                        try {
                            if (this.mUseFifoUiScheduling) {
                                app.savedPriority = Process.getThreadPriority(app.pid);
                                try {
                                    Process.setThreadScheduler(app.pid, 1073741825, 1);
                                } catch (IllegalArgumentException e2) {
                                }
                                if (app.renderThreadTid != 0) {
                                    try {
                                        Process.setThreadScheduler(app.renderThreadTid, 1073741825, 1);
                                    } catch (IllegalArgumentException e3) {
                                    }
                                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                                        Slog.d("UI_FIFO", "Set RenderThread (TID " + app.renderThreadTid + ") to FIFO");
                                    }
                                } else if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                                    Slog.d("UI_FIFO", "Not setting RenderThread TID");
                                }
                            } else {
                                Process.setThreadPriority(app.pid, -10);
                                if (app.renderThreadTid != 0) {
                                    try {
                                        Process.setThreadPriority(app.renderThreadTid, -10);
                                    } catch (IllegalArgumentException e4) {
                                    }
                                }
                            }
                        } catch (Exception e5) {
                            Slog.w(TAG, "Failed setting process group of " + app.pid + " to " + app.curSchedGroup);
                            e5.printStackTrace();
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(oldId);
                        }
                    }
                } else if (oldSchedGroup == 2) {
                    if (app.curSchedGroup != 2) {
                        if (app.vrThreadTid != 0) {
                            Process.setThreadScheduler(app.vrThreadTid, 0, 0);
                        }
                        if (this.mUseFifoUiScheduling) {
                            Process.setThreadScheduler(app.pid, 0, 0);
                            Process.setThreadPriority(app.pid, app.savedPriority);
                            if (app.renderThreadTid != 0) {
                                Process.setThreadScheduler(app.renderThreadTid, 0, 0);
                                Process.setThreadPriority(app.renderThreadTid, -4);
                            }
                        } else {
                            Process.setThreadPriority(app.pid, 0);
                            if (app.renderThreadTid != 0) {
                                Process.setThreadPriority(app.renderThreadTid, 0);
                            }
                        }
                    }
                }
                Binder.restoreCallingIdentity(oldId);
                notifyProcessGroupChange(app.pid, app.uid);
                if (this.mCpusetSwitch) {
                    notifyProcessGroupChange(app.pid, app.uid, app.curSchedGroup);
                }
            }
        }
        if (app.repForegroundActivities != app.foregroundActivities) {
            app.repForegroundActivities = app.foregroundActivities;
            changes = 1;
        }
        if (app.repProcState != app.curProcState) {
            app.repProcState = app.curProcState;
            changes |= 2;
            if (app.thread != null) {
                try {
                    app.thread.setProcessState(app.repProcState);
                } catch (RemoteException e6) {
                }
            }
        }
        if (app.setProcState == -1 || ProcessList.procStatesDifferForMem(app.curProcState, app.setProcState)) {
            app.lastStateTime = now;
            app.nextPssTime = ProcessList.computeNextPssTime(app.curProcState, true, this.mTestPssMode, isSleepingLocked(), now);
            if (ActivityManagerDebugConfig.DEBUG_PSS) {
                Slog.d(TAG_PSS, "Process state change from " + ProcessList.makeProcStateString(app.setProcState) + " to " + ProcessList.makeProcStateString(app.curProcState) + " next pss in " + (app.nextPssTime - now) + ": " + app);
            }
        } else if (now > app.nextPssTime || (now > app.lastPssTime + 1800000 && now > app.lastStateTime + ProcessList.minTimeFromStateChange(this.mTestPssMode))) {
            requestPssLocked(app, app.setProcState);
            app.nextPssTime = ProcessList.computeNextPssTime(app.curProcState, false, this.mTestPssMode, isSleepingLocked(), now);
        }
        if (app.setProcState != app.curProcState) {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                Slog.v(TAG_OOM_ADJ, "Proc state change of " + app.processName + " to " + app.curProcState);
            }
            boolean setImportant = app.setProcState < 10;
            boolean curImportant = app.curProcState < 10;
            if (setImportant && !curImportant) {
                BatteryStatsImpl stats = this.mBatteryStatsService.getActiveStatistics();
                synchronized (stats) {
                    app.lastWakeTime = stats.getProcessWakeTime(app.info.uid, app.pid, nowElapsed);
                }
                app.lastCpuTime = app.curCpuTime;
            }
            maybeUpdateUsageStatsLocked(app, nowElapsed);
            app.setProcState = app.curProcState;
            if (app.setProcState >= 12) {
                app.notCachedSinceIdle = false;
            }
            if (doingAll) {
                app.procStateChanged = true;
            } else {
                setProcessTrackerStateLocked(app, this.mProcessStats.getMemFactorLocked(), now);
            }
        } else if (app.reportedInteraction && nowElapsed - app.interactionEventTime > 86400000) {
            maybeUpdateUsageStatsLocked(app, nowElapsed);
        }
        if (changes != 0) {
            int NA;
            if (ActivityManagerDebugConfig.DEBUG_PROCESS_OBSERVERS) {
                Slog.i(TAG_PROCESS_OBSERVERS, "Changes in " + app + ": " + changes);
            }
            int i = this.mPendingProcessChanges.size() - 1;
            ProcessChangeItem processChangeItem = null;
            while (i >= 0) {
                processChangeItem = (ProcessChangeItem) this.mPendingProcessChanges.get(i);
                if (processChangeItem.pid == app.pid) {
                    if (ActivityManagerDebugConfig.DEBUG_PROCESS_OBSERVERS) {
                        Slog.i(TAG_PROCESS_OBSERVERS, "Re-using existing item: " + processChangeItem);
                    }
                    if (i < 0) {
                        NA = this.mAvailProcessChanges.size();
                        if (NA <= 0) {
                            processChangeItem = (ProcessChangeItem) this.mAvailProcessChanges.remove(NA - 1);
                            if (ActivityManagerDebugConfig.DEBUG_PROCESS_OBSERVERS) {
                                Slog.i(TAG_PROCESS_OBSERVERS, "Retrieving available item: " + processChangeItem);
                            }
                        } else {
                            processChangeItem = new ProcessChangeItem();
                            if (ActivityManagerDebugConfig.DEBUG_PROCESS_OBSERVERS) {
                                Slog.i(TAG_PROCESS_OBSERVERS, "Allocating new item: " + processChangeItem);
                            }
                        }
                        processChangeItem.changes = 0;
                        processChangeItem.pid = app.pid;
                        processChangeItem.uid = app.info.uid;
                        if (this.mPendingProcessChanges.size() == 0) {
                            if (ActivityManagerDebugConfig.DEBUG_PROCESS_OBSERVERS) {
                                Slog.i(TAG_PROCESS_OBSERVERS, "*** Enqueueing dispatch processes changed!");
                            }
                            this.mUiHandler.obtainMessage(31).sendToTarget();
                        }
                        this.mPendingProcessChanges.add(processChangeItem);
                    }
                    processChangeItem.changes |= changes;
                    processChangeItem.processState = app.repProcState;
                    processChangeItem.foregroundActivities = app.repForegroundActivities;
                    if (ActivityManagerDebugConfig.DEBUG_PROCESS_OBSERVERS) {
                        Slog.i(TAG_PROCESS_OBSERVERS, "Item " + Integer.toHexString(System.identityHashCode(processChangeItem)) + " " + app.toShortString() + ": changes=" + processChangeItem.changes + " procState=" + processChangeItem.processState + " foreground=" + processChangeItem.foregroundActivities + " type=" + app.adjType + " source=" + app.adjSource + " target=" + app.adjTarget);
                    }
                } else {
                    i--;
                }
            }
            if (i < 0) {
                NA = this.mAvailProcessChanges.size();
                if (NA <= 0) {
                    processChangeItem = new ProcessChangeItem();
                    if (ActivityManagerDebugConfig.DEBUG_PROCESS_OBSERVERS) {
                        Slog.i(TAG_PROCESS_OBSERVERS, "Allocating new item: " + processChangeItem);
                    }
                } else {
                    processChangeItem = (ProcessChangeItem) this.mAvailProcessChanges.remove(NA - 1);
                    if (ActivityManagerDebugConfig.DEBUG_PROCESS_OBSERVERS) {
                        Slog.i(TAG_PROCESS_OBSERVERS, "Retrieving available item: " + processChangeItem);
                    }
                }
                processChangeItem.changes = 0;
                processChangeItem.pid = app.pid;
                processChangeItem.uid = app.info.uid;
                if (this.mPendingProcessChanges.size() == 0) {
                    if (ActivityManagerDebugConfig.DEBUG_PROCESS_OBSERVERS) {
                        Slog.i(TAG_PROCESS_OBSERVERS, "*** Enqueueing dispatch processes changed!");
                    }
                    this.mUiHandler.obtainMessage(31).sendToTarget();
                }
                this.mPendingProcessChanges.add(processChangeItem);
            }
            processChangeItem.changes |= changes;
            processChangeItem.processState = app.repProcState;
            processChangeItem.foregroundActivities = app.repForegroundActivities;
            if (ActivityManagerDebugConfig.DEBUG_PROCESS_OBSERVERS) {
                Slog.i(TAG_PROCESS_OBSERVERS, "Item " + Integer.toHexString(System.identityHashCode(processChangeItem)) + " " + app.toShortString() + ": changes=" + processChangeItem.changes + " procState=" + processChangeItem.processState + " foreground=" + processChangeItem.foregroundActivities + " type=" + app.adjType + " source=" + app.adjSource + " target=" + app.adjTarget);
            }
        }
        return success;
    }

    private final void enqueueUidChangeLocked(UidRecord uidRec, int uid, int change) {
        ChangeItem pendingChange;
        if (uidRec == null || uidRec.pendingChange == null) {
            if (this.mPendingUidChanges.size() == 0) {
                if (ActivityManagerDebugConfig.DEBUG_UID_OBSERVERS) {
                    Slog.i(TAG_UID_OBSERVERS, "*** Enqueueing dispatch uid changed!");
                }
                this.mUiHandler.obtainMessage(54).sendToTarget();
            }
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    int NA = this.mAvailUidChanges.size();
                    if (NA > 0) {
                        pendingChange = (ChangeItem) this.mAvailUidChanges.remove(NA - 1);
                        if (ActivityManagerDebugConfig.DEBUG_UID_OBSERVERS) {
                            Slog.i(TAG_UID_OBSERVERS, "Retrieving available item: " + pendingChange);
                        }
                    } else {
                        pendingChange = new ChangeItem();
                        if (ActivityManagerDebugConfig.DEBUG_UID_OBSERVERS) {
                            Slog.i(TAG_UID_OBSERVERS, "Allocating new item: " + pendingChange);
                        }
                    }
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            if (uidRec != null) {
                uidRec.pendingChange = pendingChange;
                if (change == 1 && !uidRec.idle) {
                    change = 2;
                }
            } else if (uid < 0) {
                throw new IllegalArgumentException("No UidRecord or uid");
            }
            pendingChange.uidRecord = uidRec;
            if (uidRec != null) {
                uid = uidRec.uid;
            }
            pendingChange.uid = uid;
            this.mPendingUidChanges.add(pendingChange);
        } else {
            pendingChange = uidRec.pendingChange;
            if (change == 1 && pendingChange.change == 3) {
                change = 2;
            }
        }
        pendingChange.change = change;
        pendingChange.processState = uidRec != null ? uidRec.setProcState : -1;
    }

    private void maybeUpdateProviderUsageStatsLocked(ProcessRecord app, String providerPkgName, String authority) {
        if (app != null && app.curProcState <= 6) {
            UserState userState = this.mUserController.getStartedUserStateLocked(app.userId);
            if (userState != null) {
                long now = SystemClock.elapsedRealtime();
                Long lastReported = (Long) userState.mProviderLastReportedFg.get(authority);
                if (lastReported == null || lastReported.longValue() < now - 60000) {
                    this.mUsageStatsService.reportContentProviderUsage(authority, providerPkgName, app.userId);
                    userState.mProviderLastReportedFg.put(authority, Long.valueOf(now));
                }
            }
        }
    }

    private void maybeUpdateUsageStatsLocked(ProcessRecord app, long nowElapsed) {
        if (ActivityManagerDebugConfig.DEBUG_USAGE_STATS) {
            Slog.d(TAG, "Checking proc [" + Arrays.toString(app.getPackageList()) + "] state changes: old = " + app.setProcState + ", new = " + app.curProcState);
        }
        if (this.mUsageStatsService != null) {
            boolean isInteraction;
            if (app.curProcState <= 3) {
                isInteraction = true;
                app.fgInteractionTime = 0;
            } else if (app.curProcState > 5) {
                isInteraction = app.curProcState <= 6;
                app.fgInteractionTime = 0;
            } else if (app.fgInteractionTime == 0) {
                app.fgInteractionTime = nowElapsed;
                isInteraction = false;
            } else {
                isInteraction = nowElapsed > app.fgInteractionTime + 1800000;
            }
            if (isInteraction && (!app.reportedInteraction || nowElapsed - app.interactionEventTime > 86400000)) {
                app.interactionEventTime = nowElapsed;
                String[] packages = app.getPackageList();
                if (packages != null) {
                    for (String reportEvent : packages) {
                        this.mUsageStatsService.reportEvent(reportEvent, app.userId, 6);
                    }
                }
            }
            app.reportedInteraction = isInteraction;
            if (!isInteraction) {
                app.interactionEventTime = 0;
            }
        }
    }

    private final void setProcessTrackerStateLocked(ProcessRecord proc, int memFactor, long now) {
        if (proc.thread != null && proc.baseProcessTracker != null) {
            proc.baseProcessTracker.setState(proc.repProcState, memFactor, now, proc.pkgList);
        }
    }

    private final boolean updateOomAdjLocked(ProcessRecord app, int cachedAdj, ProcessRecord TOP_APP, boolean doingAll, long now) {
        if (app.thread == null) {
            return false;
        }
        computeOomAdjLocked(app, cachedAdj, TOP_APP, doingAll, now);
        return applyOomAdjLocked(app, doingAll, now, SystemClock.elapsedRealtime());
    }

    final void updateProcessForegroundLocked(ProcessRecord proc, boolean isForeground, boolean oomAdj) {
        if (isForeground != proc.foregroundServices) {
            proc.foregroundServices = isForeground;
            ArrayList<ProcessRecord> curProcs = (ArrayList) this.mForegroundPackages.get(proc.info.packageName, proc.info.uid);
            if (isForeground) {
                if (curProcs == null) {
                    curProcs = new ArrayList();
                    this.mForegroundPackages.put(proc.info.packageName, proc.info.uid, curProcs);
                }
                if (!curProcs.contains(proc)) {
                    curProcs.add(proc);
                    this.mBatteryStatsService.noteEvent(32770, proc.info.packageName, proc.info.uid);
                }
            } else if (curProcs != null && curProcs.remove(proc)) {
                this.mBatteryStatsService.noteEvent(16386, proc.info.packageName, proc.info.uid);
                if (curProcs.size() <= 0) {
                    this.mForegroundPackages.remove(proc.info.packageName, proc.info.uid);
                }
            }
            if (oomAdj) {
                updateOomAdjLocked();
            }
        }
    }

    private final ActivityRecord resumedAppLocked() {
        String pkg;
        int uid;
        ActivityRecord act = this.mStackSupervisor.resumedAppLocked();
        if (act != null) {
            pkg = act.packageName;
            uid = act.info.applicationInfo.uid;
        } else {
            pkg = null;
            uid = -1;
        }
        if (uid != this.mCurResumedUid || (pkg != this.mCurResumedPackage && (pkg == null || !pkg.equals(this.mCurResumedPackage)))) {
            if (this.mCurResumedPackage != null) {
                this.mBatteryStatsService.noteEvent(16387, this.mCurResumedPackage, this.mCurResumedUid);
            }
            this.mCurResumedPackage = pkg;
            this.mCurResumedUid = uid;
            if (this.mCurResumedPackage != null) {
                this.mBatteryStatsService.noteEvent(32771, this.mCurResumedPackage, this.mCurResumedUid);
            }
        }
        return act;
    }

    final boolean updateOomAdjLocked(ProcessRecord app) {
        ActivityRecord TOP_ACT = resumedAppLocked();
        ProcessRecord processRecord = TOP_ACT != null ? TOP_ACT.app : null;
        boolean wasCached = app.cached;
        this.mAdjSeq++;
        boolean success = updateOomAdjLocked(app, app.curRawAdj >= 900 ? app.curRawAdj : 1001, processRecord, false, SystemClock.uptimeMillis());
        if (wasCached != app.cached || app.curRawAdj == 1001) {
            updateOomAdjLocked();
        }
        return success;
    }

    final void updateOomAdjLocked() {
        int i;
        int cachedProcessLimit;
        int emptyProcessLimit;
        ProcessRecord app;
        UidRecord uidRec;
        int memFactor;
        ActivityRecord TOP_ACT = resumedAppLocked();
        ProcessRecord processRecord = TOP_ACT != null ? TOP_ACT.app : null;
        long now = SystemClock.uptimeMillis();
        long nowElapsed = SystemClock.elapsedRealtime();
        long oldTime = now - 1800000;
        int N = this.mLruProcesses.size();
        for (i = this.mActiveUids.size() - 1; i >= 0; i--) {
            ((UidRecord) this.mActiveUids.valueAt(i)).reset();
        }
        this.mStackSupervisor.rankTaskLayersIfNeeded();
        this.mAdjSeq++;
        this.mNewNumServiceProcs = 0;
        this.mNewNumAServiceProcs = 0;
        if (this.mProcessLimit <= 0) {
            cachedProcessLimit = 0;
            emptyProcessLimit = 0;
        } else if (this.mProcessLimit == 1) {
            emptyProcessLimit = 1;
            cachedProcessLimit = 0;
        } else {
            emptyProcessLimit = ProcessList.computeEmptyProcessLimit(this.mProcessLimit);
            cachedProcessLimit = this.mProcessLimit - emptyProcessLimit;
        }
        int numEmptyProcs = (N - this.mNumNonCachedProcs) - this.mNumCachedHiddenProcs;
        if (numEmptyProcs > cachedProcessLimit) {
            numEmptyProcs = cachedProcessLimit;
        }
        int emptyFactor = numEmptyProcs / 3;
        if (emptyFactor < 1) {
            emptyFactor = 1;
        }
        int cachedFactor = (this.mNumCachedHiddenProcs > 0 ? this.mNumCachedHiddenProcs : 1) / 3;
        if (cachedFactor < 1) {
            cachedFactor = 1;
        }
        int stepCached = 0;
        int stepEmpty = 0;
        int numCached = 0;
        int numEmpty = 0;
        int numTrimming = 0;
        this.mNumNonCachedProcs = 0;
        this.mNumCachedHiddenProcs = 0;
        int curCachedAdj = 900;
        int nextCachedAdj = NetdResponseCode.ApLinkedStaListChangeQCOM;
        int curEmptyAdj = 900;
        int nextEmptyAdj = 902;
        ProcessRecord selectedAppRecord = null;
        long serviceLastActivity = 0;
        int numBServices = 0;
        for (i = this.mLruProcesses.size() - 1; i >= 0; i--) {
            try {
                app = (ProcessRecord) this.mLruProcesses.get(i);
                if (app != null) {
                    if (ProcessList.ENABLE_B_SERVICE_PROPAGATION && app.serviceb && app.curAdj == 800) {
                        numBServices++;
                        if ((app.info.flags & 129) == 0 || (app.info.hwFlags & 100663296) != 0) {
                            for (int s = app.services.size() - 1; s >= 0; s--) {
                                ServiceRecord sr = (ServiceRecord) app.services.valueAt(s);
                                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                                    Slog.d(TAG, "app.processName = " + app.processName + " serviceb = " + app.serviceb + " s = " + s + " sr.lastActivity = " + sr.lastActivity + " packageName = " + sr.packageName + " processName = " + sr.processName);
                                }
                                if (SystemClock.uptimeMillis() - sr.lastActivity < ((long) ProcessList.MIN_BSERVICE_AGING_TIME)) {
                                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                                        Slog.d(TAG, "Not aged enough!!!");
                                    }
                                } else if (serviceLastActivity == 0) {
                                    serviceLastActivity = sr.lastActivity;
                                    selectedAppRecord = app;
                                } else if (sr.lastActivity < serviceLastActivity) {
                                    serviceLastActivity = sr.lastActivity;
                                    selectedAppRecord = app;
                                }
                            }
                        }
                    }
                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ && selectedAppRecord != null) {
                        Slog.d(TAG, "Identified app.processName = " + selectedAppRecord.processName + " app.pid = " + selectedAppRecord.pid);
                    }
                    if (!(app.killedByAm || app.thread == null)) {
                        app.procStateChanged = false;
                        computeOomAdjLocked(app, 1001, processRecord, true, now);
                        if (app.curAdj >= 1001) {
                            switch (app.curProcState) {
                                case 14:
                                case 15:
                                    app.curRawAdj = curCachedAdj;
                                    app.curAdj = app.modifyRawOomAdj(curCachedAdj);
                                    if (ActivityManagerDebugConfig.DEBUG_LRU) {
                                    }
                                    if (curCachedAdj != nextCachedAdj) {
                                        stepCached++;
                                        if (stepCached >= cachedFactor) {
                                            stepCached = 0;
                                            curCachedAdj = nextCachedAdj;
                                            nextCachedAdj += 2;
                                            if (nextCachedAdj > 906) {
                                                nextCachedAdj = 906;
                                                break;
                                            }
                                        }
                                    }
                                    break;
                                default:
                                    app.curRawAdj = curEmptyAdj;
                                    app.curAdj = app.modifyRawOomAdj(curEmptyAdj);
                                    if (ActivityManagerDebugConfig.DEBUG_LRU) {
                                    }
                                    if (curEmptyAdj != nextEmptyAdj) {
                                        stepEmpty++;
                                        if (stepEmpty >= emptyFactor) {
                                            stepEmpty = 0;
                                            curEmptyAdj = nextEmptyAdj;
                                            nextEmptyAdj += 2;
                                            if (nextEmptyAdj > 906) {
                                                nextEmptyAdj = 906;
                                                break;
                                            }
                                        }
                                    }
                                    break;
                            }
                        }
                        applyOomAdjLocked(app, true, now, nowElapsed);
                        switch (app.curProcState) {
                            case 14:
                            case 15:
                                this.mNumCachedHiddenProcs++;
                                numCached++;
                                if (numCached > cachedProcessLimit) {
                                    app.kill("cached #" + numCached, true);
                                    break;
                                }
                                break;
                            case 16:
                                if (numEmpty > ProcessList.TRIM_EMPTY_APPS && app.lastActivityTime < oldTime) {
                                    app.kill("empty for " + (((1800000 + oldTime) - app.lastActivityTime) / 1000) + "s", true);
                                    break;
                                }
                                numEmpty++;
                                if (numEmpty > emptyProcessLimit) {
                                    AppDiedInfo appDiedInfo = new AppDiedInfo(app.userId, app.processName, -1, "emptyProcess");
                                    app.kill("empty #" + numEmpty, true);
                                    reportAppDiedMsg(appDiedInfo);
                                    break;
                                }
                                break;
                            default:
                                this.mNumNonCachedProcs++;
                                break;
                        }
                        if (!app.isolated || app.services.size() > 0) {
                            uidRec = app.uidRecord;
                            if (uidRec != null && uidRec.curProcState > app.curProcState) {
                                uidRec.curProcState = app.curProcState;
                            }
                        } else {
                            app.kill("isolated not needed", true);
                        }
                        if (app.curProcState >= 12 && !app.killedByAm) {
                            numTrimming++;
                        }
                    }
                }
            } catch (Throwable e) {
                Log.w(TAG, "ProcessRecord index out of bounds!", e);
            }
        }
        if (numBServices > ProcessList.BSERVICE_APP_THRESHOLD && this.mAllowLowerMemLevel && selectedAppRecord != null) {
            if (this.mCpusetSwitch) {
                ProcessList.setOomAdj(selectedAppRecord.pid, selectedAppRecord.info.uid, 906, selectedAppRecord.processName);
            } else {
                ProcessList.setOomAdj(selectedAppRecord.pid, selectedAppRecord.info.uid, 906);
            }
            selectedAppRecord.setAdj = selectedAppRecord.curAdj;
            if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                Slog.d(TAG, "app.processName = " + selectedAppRecord.processName + " app.pid = " + selectedAppRecord.pid + " is moved to higher adj");
            }
        }
        this.mNumServiceProcs = this.mNewNumServiceProcs;
        int numCachedAndEmpty = numCached + numEmpty;
        if (numCached > ProcessList.TRIM_CACHED_APPS || numEmpty > ProcessList.TRIM_EMPTY_APPS) {
            memFactor = 0;
        } else if (numCachedAndEmpty <= 3) {
            memFactor = 3;
        } else if (numCachedAndEmpty <= 5) {
            memFactor = 2;
        } else {
            memFactor = 1;
        }
        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
            Slog.d(TAG_OOM_ADJ, "oom: memFactor=" + memFactor + " last=" + this.mLastMemoryLevel + " allowLow=" + this.mAllowLowerMemLevel + " numProcs=" + this.mLruProcesses.size() + " last=" + this.mLastNumProcesses);
        }
        if (memFactor > this.mLastMemoryLevel && (!this.mAllowLowerMemLevel || this.mLruProcesses.size() >= this.mLastNumProcesses)) {
            memFactor = this.mLastMemoryLevel;
            if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                Slog.d(TAG_OOM_ADJ, "Keeping last mem factor!");
            }
        }
        if (memFactor != this.mLastMemoryLevel) {
            EventLogTags.writeAmMemFactor(memFactor, this.mLastMemoryLevel);
        }
        this.mLastMemoryLevel = memFactor;
        this.mLastNumProcesses = this.mLruProcesses.size();
        boolean allChanged = this.mProcessStats.setMemFactorLocked(memFactor, !isSleepingLocked(), now);
        int trackerMemFactor = this.mProcessStats.getMemFactorLocked();
        if (memFactor != 0) {
            int fgTrimLevel;
            if (this.mLowRamStartTime == 0) {
                this.mLowRamStartTime = now;
            }
            int step = 0;
            switch (memFactor) {
                case 2:
                    fgTrimLevel = 10;
                    break;
                case 3:
                    fgTrimLevel = 15;
                    break;
                default:
                    fgTrimLevel = 5;
                    break;
            }
            int factor = numTrimming / 3;
            int minFactor = 2;
            if (this.mHomeProcess != null) {
                minFactor = 3;
            }
            if (this.mPreviousProcess != null) {
                minFactor++;
            }
            if (factor < minFactor) {
                factor = minFactor;
            }
            int curLevel = 80;
            for (i = this.mLruProcesses.size() - 1; i >= 0; i--) {
                try {
                    app = (ProcessRecord) this.mLruProcesses.get(i);
                    if (app != null) {
                        if (allChanged || app.procStateChanged) {
                            setProcessTrackerStateLocked(app, trackerMemFactor, now);
                            app.procStateChanged = false;
                        }
                        if (app.curProcState >= 12 && !app.killedByAm) {
                            if (app.trimMemoryLevel < curLevel && app.thread != null) {
                                try {
                                    if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                                        Slog.v(TAG_OOM_ADJ, "Trimming memory of " + app.processName + " to " + curLevel);
                                    }
                                    SmartShrinker.reclaim(app.pid, 4);
                                    app.thread.scheduleTrimMemory(curLevel);
                                } catch (RemoteException e2) {
                                }
                            }
                            app.trimMemoryLevel = curLevel;
                            step++;
                            if (step >= factor) {
                                step = 0;
                                switch (curLevel) {
                                    case 60:
                                        curLevel = 40;
                                        break;
                                    case 80:
                                        curLevel = 60;
                                        break;
                                    default:
                                        break;
                                }
                            }
                        } else if (app.curProcState == 9) {
                            if (app.trimMemoryLevel < 40 && app.thread != null) {
                                try {
                                    if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                                        Slog.v(TAG_OOM_ADJ, "Trimming memory of heavy-weight " + app.processName + " to " + 40);
                                    }
                                    app.thread.scheduleTrimMemory(40);
                                } catch (RemoteException e3) {
                                }
                            }
                            app.trimMemoryLevel = 40;
                        } else {
                            if ((app.curProcState >= 7 || app.systemNoUi) && app.pendingUiClean) {
                                if (app.trimMemoryLevel < 20 && app.thread != null) {
                                    try {
                                        if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                                            Slog.v(TAG_OOM_ADJ, "Trimming memory of bg-ui " + app.processName + " to " + 20);
                                        }
                                        app.thread.scheduleTrimMemory(20);
                                    } catch (RemoteException e4) {
                                    }
                                }
                                app.pendingUiClean = false;
                            }
                            if (app.trimMemoryLevel < fgTrimLevel && app.thread != null) {
                                try {
                                    if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                                        Slog.v(TAG_OOM_ADJ, "Trimming memory of fg " + app.processName + " to " + fgTrimLevel);
                                    }
                                    app.thread.scheduleTrimMemory(fgTrimLevel);
                                } catch (RemoteException e5) {
                                }
                            }
                            app.trimMemoryLevel = fgTrimLevel;
                        }
                    }
                } catch (Throwable e6) {
                    Log.w(TAG, "ProcessRecord index out of bounds!", e6);
                }
            }
        } else {
            if (this.mLowRamStartTime != 0) {
                this.mLowRamTimeSinceLastIdle += now - this.mLowRamStartTime;
                this.mLowRamStartTime = 0;
            }
            for (i = this.mLruProcesses.size() - 1; i >= 0; i--) {
                try {
                    app = (ProcessRecord) this.mLruProcesses.get(i);
                    if (app != null) {
                        if (allChanged || app.procStateChanged) {
                            setProcessTrackerStateLocked(app, trackerMemFactor, now);
                            app.procStateChanged = false;
                        }
                        if ((app.curProcState >= 7 || app.systemNoUi) && app.pendingUiClean) {
                            if (app.trimMemoryLevel < 20 && app.thread != null) {
                                try {
                                    if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                                        Slog.v(TAG_OOM_ADJ, "Trimming memory of ui hidden " + app.processName + " to " + 20);
                                    }
                                    app.thread.scheduleTrimMemory(20);
                                } catch (RemoteException e7) {
                                }
                            }
                            app.pendingUiClean = false;
                        }
                        app.trimMemoryLevel = 0;
                    }
                } catch (Throwable e62) {
                    Log.w(TAG, "ProcessRecord index out of bounds!", e62);
                }
            }
        }
        if (this.mAlwaysFinishActivities) {
            this.mStackSupervisor.scheduleDestroyAllActivities(null, "always-finish");
        }
        if (allChanged) {
            requestPssAllProcsLocked(now, false, this.mProcessStats.isMemFactorLowered());
        }
        for (i = this.mActiveUids.size() - 1; i >= 0; i--) {
            uidRec = (UidRecord) this.mActiveUids.valueAt(i);
            int uidChange = 0;
            if (uidRec.setProcState != uidRec.curProcState) {
                if (ActivityManagerDebugConfig.DEBUG_UID_OBSERVERS) {
                    Slog.i(TAG_UID_OBSERVERS, "Changes in " + uidRec + ": proc state from " + uidRec.setProcState + " to " + uidRec.curProcState);
                }
                if (!ActivityManager.isProcStateBackground(uidRec.curProcState)) {
                    if (uidRec.idle) {
                        uidChange = 4;
                        uidRec.idle = false;
                    }
                    uidRec.lastBackgroundTime = 0;
                } else if (!ActivityManager.isProcStateBackground(uidRec.setProcState)) {
                    uidRec.lastBackgroundTime = nowElapsed;
                    if (!this.mHandler.hasMessages(60)) {
                        this.mHandler.sendEmptyMessageDelayed(60, 60000);
                    }
                }
                uidRec.setProcState = uidRec.curProcState;
                enqueueUidChangeLocked(uidRec, -1, uidChange);
                noteUidProcessState(uidRec.uid, uidRec.curProcState);
            }
        }
        if (this.mProcessStats.shouldWriteNowLocked(now)) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    synchronized (ActivityManagerService.this) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ActivityManagerService.this.mProcessStats.writeStateAsyncLocked();
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                }
            });
        }
        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
            Slog.d(TAG_OOM_ADJ, "Did OOM ADJ in " + (SystemClock.uptimeMillis() - now) + "ms");
        }
    }

    final void idleUids() {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                long nowElapsed = SystemClock.elapsedRealtime();
                long maxBgTime = nowElapsed - 60000;
                long nextTime = 0;
                for (int i = this.mActiveUids.size() - 1; i >= 0; i--) {
                    UidRecord uidRec = (UidRecord) this.mActiveUids.valueAt(i);
                    long bgTime = uidRec.lastBackgroundTime;
                    if (bgTime > 0 && !uidRec.idle) {
                        if (bgTime <= maxBgTime) {
                            uidRec.idle = true;
                            doStopUidLocked(uidRec.uid, uidRec);
                        } else if (nextTime == 0 || nextTime > bgTime) {
                            nextTime = bgTime;
                        }
                    }
                }
                if (nextTime > 0) {
                    this.mHandler.removeMessages(60);
                    this.mHandler.sendEmptyMessageDelayed(60, (60000 + nextTime) - nowElapsed);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    final void runInBackgroundDisabled(int uid) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                UidRecord uidRec = (UidRecord) this.mActiveUids.get(uid);
                if (uidRec == null) {
                    doStopUidLocked(uid, null);
                } else if (uidRec.idle) {
                    doStopUidLocked(uidRec.uid, uidRec);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    final void doStopUidLocked(int uid, UidRecord uidRec) {
        this.mServices.stopInBackgroundLocked(uid);
        enqueueUidChangeLocked(uidRec, uid, 3);
    }

    final void trimApplications() {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                for (int i = this.mRemovedProcesses.size() - 1; i >= 0; i--) {
                    ProcessRecord app = (ProcessRecord) this.mRemovedProcesses.get(i);
                    if (app.activities.size() == 0 && app.curReceiver == null && app.services.size() == 0) {
                        Object asBinder;
                        String str = TAG;
                        StringBuilder append = new StringBuilder().append("Exiting empty application process ").append(app.toShortString()).append(" (");
                        if (app.thread != null) {
                            asBinder = app.thread.asBinder();
                        } else {
                            asBinder = null;
                        }
                        Slog.i(str, append.append(asBinder).append(")\n").toString());
                        if (app.pid <= 0 || app.pid == MY_PID) {
                            try {
                                app.thread.scheduleExit();
                            } catch (Exception e) {
                            }
                        } else {
                            app.kill("empty", false);
                        }
                        cleanUpApplicationRecordLocked(app, false, true, -1, false);
                        this.mRemovedProcesses.remove(i);
                        if (app.persistent) {
                            addAppLocked(app.info, false, null);
                        }
                    }
                }
                updateOomAdjLocked();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    final void trimServicesAfterBoot() {
        int i = 0;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                for (int i2 = this.mLruProcesses.size() - 1; i2 >= 0; i2--) {
                    ProcessRecord app = (ProcessRecord) this.mLruProcesses.get(i2);
                    if (app.serviceb || app.curAdj == 500) {
                        SmartShrinker.reclaim(app.pid, 4);
                    }
                }
                if (SystemProperties.getBoolean("ro.config.reclaim_zygote", false) && "zygote32_64".equals(SystemProperties.get("ro.zygote", "zygote64_32"))) {
                    int[] pids = Process.getPidsForCommands(new String[]{"zygote64"});
                    if (pids != null) {
                        synchronized (this) {
                            try {
                                boostPriorityForLockedSection();
                                int length = pids.length;
                                while (i < length) {
                                    SmartShrinker.reclaim(pids[i], 2);
                                    i++;
                                }
                            } finally {
                                resetPriorityAfterLockedSection();
                            }
                        }
                    }
                    return;
                }
                return;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void signalPersistentProcesses(int sig) throws RemoteException {
        if (sig != 10) {
            throw new SecurityException("Only SIGNAL_USR1 is allowed");
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if (checkCallingPermission("android.permission.SIGNAL_PERSISTENT_PROCESSES") != 0) {
                    throw new SecurityException("Requires permission android.permission.SIGNAL_PERSISTENT_PROCESSES");
                }
                for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
                    ProcessRecord r = (ProcessRecord) this.mLruProcesses.get(i);
                    if (r.thread != null && r.persistent) {
                        Process.sendSignal(r.pid, sig);
                    }
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    private void stopProfilerLocked(ProcessRecord proc, int profileType) {
        if (proc == null || proc == this.mProfileProc) {
            proc = this.mProfileProc;
            profileType = this.mProfileType;
            clearProfilerLocked();
        }
        if (proc != null) {
            try {
                proc.thread.profilerControl(false, null, profileType);
            } catch (RemoteException e) {
                throw new IllegalStateException("Process disappeared");
            }
        }
    }

    private void clearProfilerLocked() {
        if (this.mProfileFd != null) {
            try {
                this.mProfileFd.close();
            } catch (IOException e) {
            }
        }
        this.mProfileApp = null;
        this.mProfileProc = null;
        this.mProfileFile = null;
        this.mProfileType = 0;
        this.mAutoStopProfiler = false;
        this.mSamplingInterval = 0;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean profileControl(String process, int userId, boolean start, ProfilerInfo profilerInfo, int profileType) throws RemoteException {
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                if (checkCallingPermission("android.permission.SET_ACTIVITY_WATCHER") != 0) {
                    throw new SecurityException("Requires permission android.permission.SET_ACTIVITY_WATCHER");
                }
                if (start) {
                    if (profilerInfo != null) {
                    }
                    throw new IllegalArgumentException("null profile info or fd");
                }
                ProcessRecord proc = null;
                if (process != null) {
                    proc = findProcessLocked(process, userId, "profileControl");
                }
                if (start && (proc == null || proc.thread == null)) {
                    throw new IllegalArgumentException("Unknown process: " + process);
                }
                if (start) {
                    ParcelFileDescriptor fd;
                    stopProfilerLocked(null, 0);
                    setProfileApp(proc.info, proc.processName, profilerInfo);
                    this.mProfileProc = proc;
                    this.mProfileType = profileType;
                    try {
                        fd = profilerInfo.profileFd.dup();
                    } catch (IOException e) {
                        fd = null;
                    }
                    profilerInfo.profileFd = fd;
                    proc.thread.profilerControl(start, profilerInfo, profileType);
                    this.mProfileFd = null;
                } else {
                    stopProfilerLocked(proc, profileType);
                    if (!(profilerInfo == null || profilerInfo.profileFd == null)) {
                        try {
                            profilerInfo.profileFd.close();
                        } catch (IOException e2) {
                        }
                    }
                }
            }
            resetPriorityAfterLockedSection();
            if (!(profilerInfo == null || profilerInfo.profileFd == null)) {
                try {
                    profilerInfo.profileFd.close();
                } catch (IOException e3) {
                }
            }
            return true;
        } catch (RemoteException e4) {
            try {
                throw new IllegalStateException("Process disappeared");
            } catch (Throwable th) {
                if (!(profilerInfo == null || profilerInfo.profileFd == null)) {
                    try {
                        profilerInfo.profileFd.close();
                    } catch (IOException e5) {
                    }
                }
            }
        } catch (Throwable th2) {
            resetPriorityAfterLockedSection();
        }
    }

    private ProcessRecord findProcessLocked(String process, int userId, String callName) {
        userId = this.mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, 2, callName, null);
        ProcessRecord proc = null;
        try {
            int pid = Integer.parseInt(process);
            synchronized (this.mPidsSelfLocked) {
                proc = (ProcessRecord) this.mPidsSelfLocked.get(pid);
            }
        } catch (NumberFormatException e) {
        }
        if (proc != null) {
            return proc;
        }
        SparseArray<ProcessRecord> procs = (SparseArray) this.mProcessNames.getMap().get(process);
        if (procs == null || procs.size() <= 0) {
            return proc;
        }
        proc = (ProcessRecord) procs.valueAt(0);
        if (userId == -1 || proc.userId == userId) {
            return proc;
        }
        for (int i = 1; i < procs.size(); i++) {
            ProcessRecord thisProc = (ProcessRecord) procs.valueAt(i);
            if (thisProc.userId == userId) {
                return thisProc;
            }
        }
        return proc;
    }

    public boolean dumpHeap(String process, int userId, boolean managed, String path, ParcelFileDescriptor fd) throws RemoteException {
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                if (checkCallingPermission("android.permission.SET_ACTIVITY_WATCHER") != 0) {
                    throw new SecurityException("Requires permission android.permission.SET_ACTIVITY_WATCHER");
                } else if (fd == null) {
                    throw new IllegalArgumentException("null fd");
                } else {
                    ProcessRecord proc = findProcessLocked(process, userId, "dumpHeap");
                    if (proc == null || proc.thread == null) {
                        throw new IllegalArgumentException("Unknown process: " + process);
                    } else if ("1".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0")) || (proc.info.flags & 2) != 0) {
                        proc.thread.dumpHeap(managed, path, fd);
                        fd = null;
                    } else {
                        throw new SecurityException("Process not debuggable: " + proc);
                    }
                }
            }
            resetPriorityAfterLockedSection();
            return true;
        } catch (RemoteException e) {
            try {
                throw new IllegalStateException("Process disappeared");
            } catch (Throwable th) {
                if (fd != null) {
                    try {
                        fd.close();
                    } catch (IOException e2) {
                    }
                }
            }
        } catch (Throwable th2) {
            resetPriorityAfterLockedSection();
        }
    }

    public void setDumpHeapDebugLimit(String processName, int uid, long maxMemSize, String reportPackage) {
        if (processName != null) {
            enforceCallingPermission("android.permission.SET_DEBUG_APP", "setDumpHeapDebugLimit()");
        } else {
            synchronized (this.mPidsSelfLocked) {
                ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(Binder.getCallingPid());
                if (proc == null) {
                    throw new SecurityException("No process found for calling pid " + Binder.getCallingPid());
                } else if (Build.IS_DEBUGGABLE || (proc.info.flags & 2) != 0) {
                    processName = proc.processName;
                    uid = proc.uid;
                    if (reportPackage == null || proc.pkgList.containsKey(reportPackage)) {
                    } else {
                        throw new SecurityException("Package " + reportPackage + " is not running in " + proc);
                    }
                } else {
                    throw new SecurityException("Not running a debuggable build");
                }
            }
        }
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if (maxMemSize > 0) {
                    this.mMemWatchProcesses.put(processName, uid, new Pair(Long.valueOf(maxMemSize), reportPackage));
                } else if (uid != 0) {
                    this.mMemWatchProcesses.remove(processName, uid);
                } else {
                    this.mMemWatchProcesses.getMap().remove(processName);
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dumpHeapFinished(String path) {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                if (Binder.getCallingPid() != this.mMemWatchDumpPid) {
                    Slog.w(TAG, "dumpHeapFinished: Calling pid " + Binder.getCallingPid() + " does not match last pid " + this.mMemWatchDumpPid);
                } else if (this.mMemWatchDumpFile == null || !this.mMemWatchDumpFile.equals(path)) {
                    Slog.w(TAG, "dumpHeapFinished: Calling path " + path + " does not match last path " + this.mMemWatchDumpFile);
                    resetPriorityAfterLockedSection();
                } else {
                    if (ActivityManagerDebugConfig.DEBUG_PSS) {
                        Slog.d(TAG_PSS, "Dump heap finished for " + path);
                    }
                    this.mHandler.sendEmptyMessage(51);
                    resetPriorityAfterLockedSection();
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    public void monitor() {
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    void onCoreSettingsChange(Bundle settings) {
        for (int i = this.mLruProcesses.size() - 1; i >= 0; i--) {
            ProcessRecord processRecord = (ProcessRecord) this.mLruProcesses.get(i);
            try {
                if (processRecord.thread != null) {
                    processRecord.thread.setCoreSettings(settings);
                }
            } catch (RemoteException e) {
            }
        }
    }

    public boolean startUserInBackground(int userId) {
        return this.mUserController.startUser(userId, false);
    }

    public boolean unlockUser(int userId, byte[] token, byte[] secret, IProgressListener listener) {
        return this.mUserController.unlockUser(userId, token, secret, listener);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean switchUser(int targetUserId) {
        enforceShellRestriction("no_debugging_features", targetUserId);
        Slog.i(TAG, "switchUser " + targetUserId + ", callingUid = " + Binder.getCallingUid() + ", callingPid = " + Binder.getCallingPid());
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                UserInfo currentUserInfo = this.mUserController.getUserInfo(this.mUserController.getCurrentUserIdLocked());
                UserInfo targetUserInfo = this.mUserController.getUserInfo(targetUserId);
                if (targetUserInfo == null) {
                    Slog.w(TAG, "No user info for user #" + targetUserId);
                } else if (!targetUserInfo.supportsSwitchTo()) {
                    Slog.w(TAG, "Cannot switch to User #" + targetUserId + ": not supported");
                    resetPriorityAfterLockedSection();
                    return false;
                } else if (targetUserInfo.isManagedProfile()) {
                    Slog.w(TAG, "Cannot switch to User #" + targetUserId + ": not a full user");
                    resetPriorityAfterLockedSection();
                    return false;
                } else {
                    this.mUserController.setTargetUserIdLocked(targetUserId);
                    resetPriorityAfterLockedSection();
                    Pair<UserInfo, UserInfo> userNames = new Pair(currentUserInfo, targetUserInfo);
                    this.mUiHandler.removeMessages(46);
                    this.mUiHandler.sendMessage(this.mUiHandler.obtainMessage(46, userNames));
                    return true;
                }
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
    }

    void scheduleStartProfilesLocked() {
        if (!this.mHandler.hasMessages(40)) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(40), 1000);
        }
    }

    public int stopUser(int userId, boolean force, IStopUserCallback callback) {
        Slog.i(TAG, "stopUser " + userId + ", force = " + force + ", callingUid = " + Binder.getCallingUid() + ", callingPid = " + Binder.getCallingPid());
        return this.mUserController.stopUser(userId, force, callback);
    }

    public UserInfo getCurrentUser() {
        return this.mUserController.getCurrentUser();
    }

    public boolean isUserRunning(int userId, int flags) {
        if (userId == UserHandle.getCallingUserId() || checkCallingPermission("android.permission.INTERACT_ACROSS_USERS") == 0) {
            boolean isUserRunningLocked;
            synchronized (this) {
                try {
                    boostPriorityForLockedSection();
                    isUserRunningLocked = this.mUserController.isUserRunningLocked(userId, flags);
                } finally {
                    resetPriorityAfterLockedSection();
                }
            }
            return isUserRunningLocked;
        }
        String msg = "Permission Denial: isUserRunning() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.INTERACT_ACROSS_USERS";
        Slog.w(TAG, msg);
        throw new SecurityException(msg);
    }

    public int[] getRunningUserIds() {
        if (checkCallingPermission("android.permission.INTERACT_ACROSS_USERS") != 0) {
            String msg = "Permission Denial: isUserRunning() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.INTERACT_ACROSS_USERS";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
        int[] startedUserArrayLocked;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                startedUserArrayLocked = this.mUserController.getStartedUserArrayLocked();
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return startedUserArrayLocked;
    }

    public void registerUserSwitchObserver(IUserSwitchObserver observer) {
        this.mUserController.registerUserSwitchObserver(observer);
    }

    public void unregisterUserSwitchObserver(IUserSwitchObserver observer) {
        this.mUserController.unregisterUserSwitchObserver(observer);
    }

    ApplicationInfo getAppInfoForUser(ApplicationInfo info, int userId) {
        if (info == null) {
            return null;
        }
        ApplicationInfo newInfo = new ApplicationInfo(info);
        newInfo.initForUser(userId);
        return newInfo;
    }

    public boolean isUserStopped(int userId) {
        boolean z;
        synchronized (this) {
            try {
                boostPriorityForLockedSection();
                z = this.mUserController.getStartedUserStateLocked(userId) == null;
            } finally {
                resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    ActivityInfo getActivityInfoForUser(ActivityInfo aInfo, int userId) {
        if (aInfo == null || (userId < 1 && aInfo.applicationInfo.uid < 100000)) {
            return aInfo;
        }
        ActivityInfo info = new ActivityInfo(aInfo);
        info.applicationInfo = getAppInfoForUser(info.applicationInfo, userId);
        return info;
    }

    private boolean processSanityChecksLocked(ProcessRecord process) {
        if (process == null || process.thread == null) {
            return false;
        }
        if ("1".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0")) || (process.info.flags & 2) != 0) {
            return true;
        }
        return false;
    }

    public boolean startBinderTracking() throws RemoteException {
        synchronized (this) {
            boostPriorityForLockedSection();
            this.mBinderTransactionTrackingEnabled = true;
            if (checkCallingPermission("android.permission.SET_ACTIVITY_WATCHER") != 0) {
                throw new SecurityException("Requires permission android.permission.SET_ACTIVITY_WATCHER");
            }
            for (int i = 0; i < this.mLruProcesses.size(); i++) {
                try {
                    ProcessRecord process = (ProcessRecord) this.mLruProcesses.get(i);
                    if (processSanityChecksLocked(process)) {
                        process.thread.startBinderTracking();
                    }
                } catch (RemoteException e) {
                    Log.v(TAG, "Process disappared");
                } catch (Throwable th) {
                    resetPriorityAfterLockedSection();
                }
            }
        }
        resetPriorityAfterLockedSection();
        return true;
    }

    public boolean stopBinderTrackingAndDump(ParcelFileDescriptor fd) throws RemoteException {
        TransferPipe tp;
        try {
            synchronized (this) {
                boostPriorityForLockedSection();
                this.mBinderTransactionTrackingEnabled = false;
                if (checkCallingPermission("android.permission.SET_ACTIVITY_WATCHER") != 0) {
                    throw new SecurityException("Requires permission android.permission.SET_ACTIVITY_WATCHER");
                } else if (fd == null) {
                    throw new IllegalArgumentException("null fd");
                } else {
                    PrintWriter pw = new FastPrintWriter(new FileOutputStream(fd.getFileDescriptor()));
                    pw.println("Binder transaction traces for all processes.\n");
                    for (ProcessRecord process : this.mLruProcesses) {
                        if (processSanityChecksLocked(process)) {
                            pw.println("Traces for process: " + process.processName);
                            pw.flush();
                            try {
                                tp = new TransferPipe();
                                process.thread.stopBinderTrackingAndDump(tp.getWriteFd().getFileDescriptor());
                                tp.go(fd.getFileDescriptor());
                                tp.kill();
                            } catch (IOException e) {
                                pw.println("Failure while dumping IPC traces from " + process + ".  Exception: " + e);
                                pw.flush();
                            } catch (RemoteException e2) {
                                pw.println("Got a RemoteException while dumping IPC traces from " + process + ".  Exception: " + e2);
                                pw.flush();
                            } catch (Throwable th) {
                                tp.kill();
                            }
                        }
                    }
                    fd = null;
                }
            }
            resetPriorityAfterLockedSection();
            return true;
        } catch (Throwable th2) {
            if (fd != null) {
                try {
                    fd.close();
                } catch (IOException e3) {
                }
            }
        }
    }

    public void killPackageDependents(String packageName, int userId) {
        enforceCallingPermission("android.permission.KILL_UID", "killPackageDependents()");
        if (packageName == null) {
            throw new NullPointerException("Cannot kill the dependents of a package without its name.");
        }
        long callingId = Binder.clearCallingIdentity();
        int pkgUid = -1;
        try {
            pkgUid = AppGlobals.getPackageManager().getPackageUid(packageName, 268435456, userId);
        } catch (RemoteException e) {
        }
        if (userId == -1 || pkgUid != -1) {
            try {
                synchronized (this) {
                    boostPriorityForLockedSection();
                    killPackageProcessesLocked(packageName, UserHandle.getAppId(pkgUid), userId, 0, false, true, true, false, "dep: " + packageName);
                }
                resetPriorityAfterLockedSection();
                Binder.restoreCallingIdentity(callingId);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(callingId);
            }
        } else {
            throw new IllegalArgumentException("Cannot kill dependents of non-existing package " + packageName);
        }
    }

    void cleanupBroadcastLocked(ProcessRecord app) {
        for (BroadcastQueue queue : this.mBroadcastQueues) {
            queue.cleanupBroadcastLocked(app);
        }
    }

    protected void exitSingleHandMode() {
    }

    private boolean shouldDropCtsBroadcast(Intent intent) {
        String data = intent.getDataString();
        if (data != null && this.mCtsActions.contains(intent.getAction()) && this.mCtsPackages.contains(data)) {
            return true;
        }
        return false;
    }

    private void initCtsDropActions() {
        this.mCtsActions.add("android.intent.action.PACKAGE_ADDED");
        this.mCtsActions.add("android.intent.action.PACKAGE_REMOVED");
        this.mCtsActions.add("android.intent.action.PACKAGE_REPLACED");
        this.mCtsActions.add("android.intent.action.PACKAGE_CHANGED");
    }

    private void initCtsDropPackages() {
        this.mCtsPackages.add("package:android.jobscheduler.cts.deviceside");
        this.mCtsPackages.add("package:android.tests.devicesetup");
        this.mCtsPackages.add("package:com.android.cts.launcherapps.simpleapp");
        this.mCtsPackages.add("package:com.android.cts.launchertests");
        this.mCtsPackages.add("package:com.android.cts.launchertests.support");
        this.mCtsPackages.add("package:com.google.android.xts.deviceowner");
        this.mCtsPackages.add("package:com.android.cts.deviceowner");
        this.mCtsPackages.add("package:com.android.cts.deviceandprofileowner");
        this.mCtsPackages.add("package:com.android.cts.intent.receiver");
        this.mCtsPackages.add("package:com.android.cts.intent.sender");
        this.mCtsPackages.add("package:com.android.cts.permissionapp");
        this.mCtsPackages.add("package:com.android.cts.location");
        this.mCtsPackages.add("package:com.android.compatibility.common.deviceinfo");
    }

    private boolean isForbidWifiAction(String action) {
        if (action.equals("android.net.wifi.SCAN_RESULTS") || action.equals("android.net.conn.CONNECTIVITY_CHANGE") || action.equals("android.net.wifi.WIFI_STATE_CHANGED") || action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            return true;
        }
        return false;
    }

    private boolean shouldPreventBadPackage(String pkg, String action) {
        if (!(pkg == null || action == null)) {
            Boolean bobj = (Boolean) this.mBadPkgs.get(pkg);
            boolean booleanValue = bobj != null ? bobj.booleanValue() : false;
            if ("com.icbc".equals(pkg) && booleanValue && isForbidWifiAction(action)) {
                Slog.d(TAG, "shouldPrevent bad pkg: " + pkg + " forceskip:" + booleanValue + " action:" + action);
                return true;
            }
        }
        return false;
    }

    public void filterBadAppsReceiverList(Intent intent, List<ResolveInfo> receivers) {
        String action = intent != null ? intent.getAction() : null;
        Iterator<ResolveInfo> iterator = receivers.iterator();
        while (iterator.hasNext()) {
            String targetPkg = ((ResolveInfo) iterator.next()).activityInfo.applicationInfo.packageName;
            if (shouldPreventBadPackage(targetPkg, action)) {
                Slog.i(TAG, "prevent start receiver of package " + targetPkg + " for action " + action);
                iterator.remove();
            }
        }
    }

    final boolean hwForceStopPackageLocked(String pkg, int appId, boolean callerWillRestart, boolean purgeCache, boolean doit, boolean evenPersistent, boolean uninstalling, int userId, String reason) {
        return forceStopPackageLocked(pkg, appId, callerWillRestart, purgeCache, doit, evenPersistent, uninstalling, userId, reason);
    }

    protected ArrayList getRecentTasks() {
        return this.mRecentTasks;
    }

    static boolean isInCallActivity(ActivityRecord r) {
        return "com.android.incallui/.InCallActivity".equals(r.shortComponentName);
    }

    private boolean shouldResponseForRog() {
        boolean isFullScreen = this.mStackSupervisor.getFocusedStack().mFullscreen;
        if (!isFullScreen) {
            Slog.i(TAG, "shouldResponseForRog->rog is not supported in multi-window mode");
        }
        return isFullScreen;
    }

    protected void applyRogStateChangedForStack(IHwRogListener listener, boolean rogEnable, AppRogInfo rogInfo, ActivityStack stack) {
    }

    protected void applyRogInfoUpdatedForStack(IHwRogListener listener, AppRogInfo rogInfo, ActivityStack stack) {
    }

    protected void attachRogInfoToApp(ProcessRecord app, ApplicationInfo appInfo) {
    }

    public boolean isTopProcessLocked(ProcessRecord processRecord) {
        if (processRecord == null) {
            return false;
        }
        ActivityRecord topActivity = getFocusedStack().topRunningActivityLocked();
        if (topActivity == null || topActivity.app == null || processRecord.pid != topActivity.app.pid) {
            return false;
        }
        return true;
    }
}
