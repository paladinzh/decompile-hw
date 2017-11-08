package com.android.server.pm;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.ResourcesManager;
import android.app.admin.IDevicePolicyManager;
import android.app.admin.SecurityLog;
import android.app.backup.IBackupManager;
import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.AuthorityEntry;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.AppsQueryHelper;
import android.content.pm.EphemeralApplicationInfo;
import android.content.pm.EphemeralResolveInfo;
import android.content.pm.EphemeralResolveInfo.EphemeralResolveIntentInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.IOnPermissionsChangeListener;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.IPackageInstaller;
import android.content.pm.IPackageMoveObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.InstrumentationInfo;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.KeySet;
import android.content.pm.PackageCleanItem;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInfoLite;
import android.content.pm.PackageInstaller.SessionParams;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.LegacyPackageDeleteObserver;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageManagerInternal.PackagesProvider;
import android.content.pm.PackageManagerInternal.SyncAdapterPackagesProvider;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Activity;
import android.content.pm.PackageParser.ActivityIntentInfo;
import android.content.pm.PackageParser.Instrumentation;
import android.content.pm.PackageParser.NewPermissionInfo;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.PackageParserException;
import android.content.pm.PackageParser.Permission;
import android.content.pm.PackageParser.PermissionGroup;
import android.content.pm.PackageParser.Provider;
import android.content.pm.PackageParser.ProviderIntentInfo;
import android.content.pm.PackageParser.Service;
import android.content.pm.PackageParser.ServiceIntentInfo;
import android.content.pm.PackageStats;
import android.content.pm.PackageUserState;
import android.content.pm.ParceledListSlice;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.content.pm.UserInfo;
import android.content.pm.VerifierDeviceIdentity;
import android.content.pm.VerifierInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hdm.HwDeviceManager;
import android.hwtheme.HwThemeManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Environment.UserEnvironment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerInternal;
import android.os.storage.IMountService;
import android.os.storage.MountServiceInternal;
import android.os.storage.MountServiceInternal.ExternalStorageMountPolicy;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.security.KeyStore;
import android.security.SystemKeyStore;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.ExceptionUtils;
import android.util.Flog;
import android.util.Jlog;
import android.util.Log;
import android.util.LogPrinter;
import android.util.MathUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.Xml;
import android.util.jar.StrictJarFile;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IMediaContainerService;
import com.android.internal.app.IMediaContainerService.Stub;
import com.android.internal.app.IntentForwarderActivity;
import com.android.internal.app.ResolverActivity;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.content.NativeLibraryHelper.Handle;
import com.android.internal.content.PackageHelper;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.HwBootFail;
import com.android.internal.os.InstallerConnection.InstallerException;
import com.android.internal.os.SomeArgs;
import com.android.internal.telephony.CarrierAppUtils;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.server.AttributeCache;
import com.android.server.EventLogTags;
import com.android.server.FgThread;
import com.android.server.HwServiceFactory;
import com.android.server.HwServiceFactory.IHwUserManagerService;
import com.android.server.IntentResolver;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.SmartShrinker;
import com.android.server.SystemConfig;
import com.android.server.SystemConfig.PermissionEntry;
import com.android.server.Watchdog;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.net.NetworkPolicyManagerInternal;
import com.android.server.pm.PackageDexOptimizer.ForcedUpdatePackageDexOptimizer;
import com.android.server.pm.PermissionsState.PermissionState;
import com.android.server.pm.Settings.VersionInfo;
import com.android.server.radar.FrameworkRadar;
import com.android.server.storage.DeviceStorageMonitorInternal;
import com.huawei.cust.HwCfgFilePolicy;
import com.huawei.indexsearch.IndexSearchManager;
import dalvik.system.CloseGuard;
import dalvik.system.DexFile;
import dalvik.system.VMRuntime;
import huawei.android.app.HwCustEmergDataManager;
import huawei.cust.HwCustUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import libcore.io.IoUtils;
import libcore.util.EmptyArray;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class PackageManagerService extends AbsPackageManagerService {
    private static final String ATTR_IS_GRANTED = "g";
    private static final String ATTR_PACKAGE_NAME = "pkg";
    private static final String ATTR_PERMISSION_NAME = "name";
    private static final String ATTR_REVOKE_ON_UPGRADE = "rou";
    private static final String ATTR_USER_FIXED = "fixed";
    private static final String ATTR_USER_SET = "set";
    private static final int BLUETOOTH_UID = 1002;
    static final int BROADCAST_DELAY = 10000;
    static final int CHECK_PENDING_VERIFICATION = 16;
    static final boolean CLEAR_RUNTIME_PERMISSIONS_ON_UPGRADE = false;
    private static final boolean DEBUG_ABI_SELECTION = false;
    private static final boolean DEBUG_APP_DATA = false;
    private static final boolean DEBUG_BACKUP = false;
    private static final boolean DEBUG_BROADCASTS = false;
    private static final boolean DEBUG_DELAPP = false;
    static final boolean DEBUG_DEXOPT = false;
    static final boolean DEBUG_DOMAIN_VERIFICATION = false;
    private static final boolean DEBUG_EPHEMERAL = false;
    private static final boolean DEBUG_FILTERS = false;
    private static final boolean DEBUG_INSTALL = false;
    private static final boolean DEBUG_INTENT_MATCHING = false;
    private static final boolean DEBUG_PACKAGE_INFO = false;
    private static final boolean DEBUG_PACKAGE_SCANNING = false;
    static final boolean DEBUG_PREFERRED = false;
    private static final boolean DEBUG_REMOVE = false;
    static final boolean DEBUG_SD_INSTALL = false;
    static final boolean DEBUG_SETTINGS = false;
    private static final boolean DEBUG_SHOW_INFO = false;
    private static final boolean DEBUG_TRIAGED_MISSING = false;
    static final boolean DEBUG_UPGRADE = false;
    private static final boolean DEBUG_VERIFY = false;
    static final ComponentName DEFAULT_CONTAINER_COMPONENT = new ComponentName(DEFAULT_CONTAINER_PACKAGE, "com.android.defcontainer.DefaultContainerService");
    static final String DEFAULT_CONTAINER_PACKAGE = "com.android.defcontainer";
    private static final long DEFAULT_MANDATORY_FSTRIM_INTERVAL = 259200000;
    private static final int DEFAULT_VERIFICATION_RESPONSE = 1;
    private static final long DEFAULT_VERIFICATION_TIMEOUT = 10000;
    private static final boolean DEFAULT_VERIFY_ENABLE = true;
    private static final boolean DISABLE_EPHEMERAL_APPS = true;
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    static final int END_COPY = 4;
    static final int FIND_INSTALL_LOC = 8;
    private static final int GRANT_DENIED = 1;
    private static final int GRANT_INSTALL = 2;
    private static final int GRANT_RUNTIME = 3;
    private static final int GRANT_UPGRADE = 4;
    protected static final boolean HWFLOW;
    static final int INIT_COPY = 5;
    private static final String INSTALL_PACKAGE_SUFFIX = "-";
    static final int INTENT_FILTER_VERIFIED = 18;
    private static final String KILL_APP_REASON_GIDS_CHANGED = "permission grant or revoke changed gids";
    private static final String KILL_APP_REASON_PERMISSIONS_REVOKED = "permissions revoked";
    private static final int LOG_UID = 1007;
    private static final int MAX_PERMISSION_TREE_FOOTPRINT = 32768;
    static final int MCS_BOUND = 3;
    static final int MCS_GIVE_UP = 11;
    static final int MCS_RECONNECT = 10;
    static final int MCS_UNBIND = 6;
    private static final int NFC_UID = 1027;
    private static final String PACKAGE_MIME_TYPE = "application/vnd.android.package-archive";
    private static final String PACKAGE_NAME_BASICADMINRECEIVER_CTS_DEIVCEOWNER = "com.android.cts.deviceowner";
    private static final String PACKAGE_NAME_BASICADMINRECEIVER_CTS_DEVICEANDPROFILEOWNER = "com.android.cts.deviceandprofileowner";
    private static final String PACKAGE_NAME_BASICADMINRECEIVER_CTS_PACKAGEINSTALLER = "com.android.cts.packageinstaller";
    static final int PACKAGE_VERIFIED = 15;
    static final String PLATFORM_PACKAGE_NAME = "android";
    static final int POST_INSTALL = 9;
    private static final Set<String> PROTECTED_ACTIONS = new ArraySet();
    private static final int RADIO_UID = 1001;
    public static final int REASON_AB_OTA = 4;
    public static final int REASON_BACKGROUND_DEXOPT = 3;
    public static final int REASON_BOOT = 1;
    public static final int REASON_CORE_APP = 8;
    public static final int REASON_FIRST_BOOT = 0;
    public static final int REASON_FORCED_DEXOPT = 7;
    public static final int REASON_INSTALL = 2;
    public static final int REASON_LAST = 8;
    public static final int REASON_NON_SYSTEM_LIBRARY = 5;
    public static final int REASON_SHARED_APK = 6;
    static final int REMOVE_CHATTY = 65536;
    static final int SCAN_BOOTING = 256;
    static final int SCAN_CHECK_ONLY = 32768;
    static final int SCAN_DEFER_DEX = 128;
    static final int SCAN_DELETE_DATA_ON_FAILURES = 1024;
    static final int SCAN_DONT_KILL_APP = 131072;
    static final int SCAN_FORCE_DEX = 4;
    static final int SCAN_IGNORE_FROZEN = 262144;
    static final int SCAN_INITIAL = 16384;
    static final int SCAN_MOVE = 8192;
    static final int SCAN_NEW_INSTALL = 16;
    static final int SCAN_NO_DEX = 2;
    static final int SCAN_NO_PATHS = 32;
    static final int SCAN_REPLACING = 2048;
    static final int SCAN_REQUIRE_KNOWN = 4096;
    static final int SCAN_TRUSTED_OVERLAY = 512;
    static final int SCAN_UNPACKING_LIB = 262144;
    static final int SCAN_UPDATE_SIGNATURE = 8;
    static final int SCAN_UPDATE_TIME = 64;
    private static final String SD_ENCRYPTION_ALGORITHM = "AES";
    private static final String SD_ENCRYPTION_KEYSTORE_NAME = "AppsOnSD";
    static final int SEND_PENDING_BROADCAST = 1;
    private static final int SHELL_UID = 2000;
    private static final String SKIP_SHARED_LIBRARY_CHECK = "&";
    private static final int SPI_UID = 1054;
    static final int START_CLEANING_PACKAGE = 7;
    static final int START_INTENT_FILTER_VERIFICATIONS = 17;
    private static final int SYSTEM_RUNTIME_GRANT_MASK = 52;
    static final String TAG = "PackageManager";
    private static final String TAG_ALL_GRANTS = "rt-grants";
    private static final String TAG_DEFAULT_APPS = "da";
    private static final String TAG_GRANT = "grant";
    private static final String TAG_INTENT_FILTER_VERIFICATION = "iv";
    private static final String TAG_PERMISSION = "perm";
    private static final String TAG_PERMISSION_BACKUP = "perm-grant-backup";
    private static final String TAG_PREFERRED_BACKUP = "pa";
    static final int UPDATED_MEDIA_STATUS = 12;
    static final int UPDATE_PERMISSIONS_ALL = 1;
    static final int UPDATE_PERMISSIONS_REPLACE_ALL = 4;
    static final int UPDATE_PERMISSIONS_REPLACE_PKG = 2;
    private static final int USER_RUNTIME_GRANT_MASK = 11;
    private static final String VENDOR_OVERLAY_DIR = "/product/overlay";
    private static final long WATCHDOG_TIMEOUT = 600000;
    static final int WRITE_PACKAGE_LIST = 19;
    static final int WRITE_PACKAGE_RESTRICTIONS = 14;
    static final int WRITE_SETTINGS = 13;
    static final int WRITE_SETTINGS_DELAY = 10000;
    private static boolean mOptimizeBootOn = SystemProperties.getBoolean("ro.config.hw_optimizeBoot", true);
    private static final Comparator<ProviderInfo> mProviderInitOrderSorter = new Comparator<ProviderInfo>() {
        public int compare(ProviderInfo p1, ProviderInfo p2) {
            int v1 = p1.initOrder;
            int v2 = p2.initOrder;
            if (v1 > v2) {
                return -1;
            }
            return v1 < v2 ? 1 : 0;
        }
    };
    private static final Comparator<ResolveInfo> mResolvePrioritySorter = new Comparator<ResolveInfo>() {
        public int compare(ResolveInfo r1, ResolveInfo r2) {
            int i = -1;
            int v1 = r1.priority;
            int v2 = r2.priority;
            if (v1 != v2) {
                if (v1 <= v2) {
                    i = 1;
                }
                return i;
            }
            v1 = r1.preferredOrder;
            v2 = r2.preferredOrder;
            if (v1 != v2) {
                if (v1 <= v2) {
                    i = 1;
                }
                return i;
            } else if (r1.isDefault != r2.isDefault) {
                if (!r1.isDefault) {
                    i = 1;
                }
                return i;
            } else {
                v1 = r1.match;
                v2 = r2.match;
                if (v1 != v2) {
                    if (v1 <= v2) {
                        i = 1;
                    }
                    return i;
                } else if (r1.system != r2.system) {
                    if (!r1.system) {
                        i = 1;
                    }
                    return i;
                } else if (r1.activityInfo != null) {
                    return r1.activityInfo.packageName.compareTo(r2.activityInfo.packageName);
                } else {
                    if (r1.serviceInfo != null) {
                        return r1.serviceInfo.packageName.compareTo(r2.serviceInfo.packageName);
                    }
                    if (r1.providerInfo != null) {
                        return r1.providerInfo.packageName.compareTo(r2.providerInfo.packageName);
                    }
                    return 0;
                }
            }
        }
    };
    private static final int mThreadnum = (Runtime.getRuntime().availableProcessors() + 1);
    private static final Intent sBrowserIntent = new Intent();
    static UserManagerService sUserManager;
    ExecutorService clearDirectoryThread;
    final ActivityIntentResolver mActivities = new ActivityIntentResolver();
    ApplicationInfo mAndroidApplication;
    final File mAppInstallDir;
    private File mAppLib32InstallDir;
    final ArrayMap<String, ArraySet<String>> mAppOpPermissionPackages;
    final String mAsecInternalPath;
    final ArrayMap<String, FeatureInfo> mAvailableFeatures;
    private IMediaContainerService mContainerService;
    final Context mContext;
    private HwCustPackageManagerService mCustPms = ((HwCustPackageManagerService) HwCustUtils.createObj(HwCustPackageManagerService.class, new Object[0]));
    ComponentName mCustomResolverComponentName;
    private final DefaultContainerConnection mDefContainerConn;
    final int mDefParseFlags;
    private DefaultPermissionGrantPolicy mDefaultPermissionPolicy;
    private boolean mDeferProtectedFilters = true;
    private ArraySet<Integer> mDirtyUsers;
    final File mDrmAppPrivateInstallDir;
    private final EphemeralApplicationRegistry mEphemeralApplicationRegistry;
    final File mEphemeralInstallDir;
    final ActivityInfo mEphemeralInstallerActivity;
    final ComponentName mEphemeralInstallerComponent;
    final ResolveInfo mEphemeralInstallerInfo;
    final ComponentName mEphemeralResolverComponent;
    final EphemeralResolverConnection mEphemeralResolverConnection;
    ExecutorService mExecutorService;
    private final ArraySet<String> mExistingSystemPackages = new ArraySet();
    private final ArrayMap<String, File> mExpectingBetter = new ArrayMap();
    final boolean mFactoryTest;
    boolean mFoundPolicyFile;
    @GuardedBy("mPackages")
    final ArraySet<String> mFrozenPackages = new ArraySet();
    final int[] mGlobalGids;
    final PackageHandler mHandler;
    final ServiceThread mHandlerThread;
    volatile boolean mHasSystemUidErrors;
    final Object mInstallLock = new Object();
    @GuardedBy("mInstallLock")
    final Installer mInstaller;
    final PackageInstallerService mInstallerService;
    final ArrayMap<ComponentName, Instrumentation> mInstrumentation;
    final SparseArray<IntentFilterVerificationState> mIntentFilterVerificationStates;
    private int mIntentFilterVerificationToken;
    private final IntentFilterVerifier<ActivityIntentInfo> mIntentFilterVerifier;
    private final ComponentName mIntentFilterVerifierComponent;
    boolean mIsPackageScanMultiThread;
    final boolean mIsPreNUpgrade;
    final boolean mIsUpgrade;
    private List<String> mKeepUninstalledPackages;
    final ArrayMap<String, Set<String>> mKnownCodebase = new ArrayMap();
    private boolean mMediaMounted;
    final DisplayMetrics mMetrics;
    private HwFrameworkMonitor mMonitor;
    private final MoveCallbacks mMoveCallbacks;
    public boolean mNeedClearDeviceForCTS;
    int mNextInstallToken;
    private AtomicInteger mNextMoveId;
    private final OnPermissionChangeListeners mOnPermissionChangeListeners;
    final boolean mOnlyCore;
    final ArrayMap<String, ArrayMap<String, Package>> mOverlays = new ArrayMap();
    protected final PackageDexOptimizer mPackageDexOptimizer;
    private final PackageUsage mPackageUsage;
    @GuardedBy("mPackages")
    final ArrayMap<String, Package> mPackages = new ArrayMap();
    final PendingPackageBroadcasts mPendingBroadcasts;
    final SparseArray<PackageVerificationState> mPendingVerification;
    private int mPendingVerificationToken;
    final ArrayMap<String, PermissionGroup> mPermissionGroups;
    Package mPlatformPackage;
    private ArrayList<Message> mPostSystemReadyMessages;
    private final ProcessLoggingHandler mProcessLoggingHandler;
    boolean mPromoteSystemApps;
    final ArraySet<String> mProtectedBroadcasts;
    private final List<ActivityIntentInfo> mProtectedFilters = new ArrayList();
    final ProtectedPackages mProtectedPackages = new ProtectedPackages();
    final ProviderIntentResolver mProviders;
    final ArrayMap<String, Provider> mProvidersByAuthority;
    final ActivityIntentResolver mReceivers = new ActivityIntentResolver();
    final String mRequiredInstallerPackage;
    final String mRequiredVerifierPackage;
    final ActivityInfo mResolveActivity;
    ComponentName mResolveComponentName;
    final ResolveInfo mResolveInfo;
    boolean mResolverReplaced;
    boolean mRestoredSettings;
    final SparseArray<PostInstallData> mRunningInstalls;
    volatile boolean mSafeMode;
    final int mSdkVersion = VERSION.SDK_INT;
    final String[] mSeparateProcesses;
    final ServiceIntentResolver mServices;
    final String mServicesSystemSharedLibraryPackageName;
    @GuardedBy("mPackages")
    final Settings mSettings;
    final String mSetupWizardPackage;
    final ArrayMap<String, SharedLibraryEntry> mSharedLibraries = new ArrayMap();
    final String mSharedSystemSharedLibraryPackageName;
    private boolean mShouldRestoreconSdAppData = false;
    private StorageEventListener mStorageListener;
    final SparseArray<ArraySet<String>> mSystemPermissions;
    volatile boolean mSystemReady;
    private int mTimerCounter = 0;
    final ArraySet<String> mTransferedPackages;
    private UserManagerInternal mUserManagerInternal;
    SparseBooleanArray mUserNeedsBadging;
    private long startTimer = 0;

    private interface BlobXmlRestorer {
        void apply(XmlPullParser xmlPullParser, int i) throws IOException, XmlPullParserException;
    }

    final class ActivityIntentResolver extends IntentResolver<ActivityIntentInfo, ResolveInfo> {
        private final ArrayMap<ComponentName, Activity> mActivities = new ArrayMap();
        private int mFlags;

        public class IterGenerator<E> {
            public Iterator<E> generate(ActivityIntentInfo info) {
                return null;
            }
        }

        public class ActionIterGenerator extends IterGenerator<String> {
            public ActionIterGenerator() {
                super();
            }

            public Iterator<String> generate(ActivityIntentInfo info) {
                return info.actionsIterator();
            }
        }

        public class AuthoritiesIterGenerator extends IterGenerator<AuthorityEntry> {
            public AuthoritiesIterGenerator() {
                super();
            }

            public Iterator<AuthorityEntry> generate(ActivityIntentInfo info) {
                return info.authoritiesIterator();
            }
        }

        public class CategoriesIterGenerator extends IterGenerator<String> {
            public CategoriesIterGenerator() {
                super();
            }

            public Iterator<String> generate(ActivityIntentInfo info) {
                return info.categoriesIterator();
            }
        }

        public class SchemesIterGenerator extends IterGenerator<String> {
            public SchemesIterGenerator() {
                super();
            }

            public Iterator<String> generate(ActivityIntentInfo info) {
                return info.schemesIterator();
            }
        }

        ActivityIntentResolver() {
        }

        public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, boolean defaultOnly, int userId) {
            if (!PackageManagerService.sUserManager.exists(userId)) {
                return null;
            }
            this.mFlags = defaultOnly ? 65536 : 0;
            return super.queryIntent(intent, resolvedType, defaultOnly, userId);
        }

        public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, int flags, int userId) {
            boolean z = false;
            if (!PackageManagerService.sUserManager.exists(userId)) {
                return null;
            }
            this.mFlags = flags;
            if ((65536 & flags) != 0) {
                z = true;
            }
            return super.queryIntent(intent, resolvedType, z, userId);
        }

        public List<ResolveInfo> queryIntentForPackage(Intent intent, String resolvedType, int flags, ArrayList<Activity> packageActivities, int userId) {
            if (!PackageManagerService.sUserManager.exists(userId) || packageActivities == null) {
                return null;
            }
            this.mFlags = flags;
            boolean defaultOnly = (65536 & flags) != 0;
            int N = packageActivities.size();
            ArrayList<ActivityIntentInfo[]> listCut = new ArrayList(N);
            for (int i = 0; i < N; i++) {
                ArrayList<ActivityIntentInfo> intentFilters = ((Activity) packageActivities.get(i)).intents;
                if (intentFilters != null && intentFilters.size() > 0) {
                    ActivityIntentInfo[] array = new ActivityIntentInfo[intentFilters.size()];
                    intentFilters.toArray(array);
                    listCut.add(array);
                }
            }
            return super.queryIntentFromList(intent, resolvedType, defaultOnly, listCut, userId);
        }

        private Activity findMatchingActivity(List<Activity> activityList, ActivityInfo activityInfo) {
            for (Activity sysActivity : activityList) {
                if (sysActivity.info.name.equals(activityInfo.name) || sysActivity.info.name.equals(activityInfo.targetActivity)) {
                    return sysActivity;
                }
                if (sysActivity.info.targetActivity != null && (sysActivity.info.targetActivity.equals(activityInfo.name) || sysActivity.info.targetActivity.equals(activityInfo.targetActivity))) {
                    return sysActivity;
                }
            }
            return null;
        }

        private <T> void getIntentListSubset(List<ActivityIntentInfo> intentList, IterGenerator<T> generator, Iterator<T> searchIterator) {
            while (searchIterator.hasNext() && intentList.size() != 0) {
                T searchAction = searchIterator.next();
                Iterator<ActivityIntentInfo> intentIter = intentList.iterator();
                while (intentIter.hasNext()) {
                    boolean selectionFound = false;
                    Iterator<T> intentSelectionIter = generator.generate((ActivityIntentInfo) intentIter.next());
                    while (intentSelectionIter != null && intentSelectionIter.hasNext()) {
                        T intentSelection = intentSelectionIter.next();
                        if (intentSelection != null && intentSelection.equals(searchAction)) {
                            selectionFound = true;
                            break;
                        }
                    }
                    if (!selectionFound) {
                        intentIter.remove();
                    }
                }
            }
        }

        private boolean isProtectedAction(ActivityIntentInfo filter) {
            Iterator<String> actionsIter = filter.actionsIterator();
            while (actionsIter != null && actionsIter.hasNext()) {
                if (PackageManagerService.PROTECTED_ACTIONS.contains((String) actionsIter.next())) {
                    return true;
                }
            }
            return false;
        }

        private void adjustPriority(List<Activity> systemActivities, ActivityIntentInfo intent) {
            if (intent.getPriority() > 0) {
                ActivityInfo activityInfo = intent.activity.info;
                ApplicationInfo applicationInfo = activityInfo.applicationInfo;
                if (!((applicationInfo.privateFlags & 8) != 0)) {
                    Slog.w(PackageManagerService.TAG, "Non-privileged app; cap priority to 0; package: " + applicationInfo.packageName + " activity: " + intent.activity.className + " origPrio: " + intent.getPriority());
                    intent.setPriority(0);
                } else if (systemActivities != null) {
                    Activity foundActivity = findMatchingActivity(systemActivities, activityInfo);
                    if (foundActivity == null) {
                        intent.setPriority(0);
                        return;
                    }
                    List<ActivityIntentInfo> intentListCopy = new ArrayList(foundActivity.intents);
                    List<ActivityIntentInfo> foundFilters = findFilters(intent);
                    Iterator<String> actionsIterator = intent.actionsIterator();
                    if (actionsIterator != null) {
                        getIntentListSubset(intentListCopy, new ActionIterGenerator(), actionsIterator);
                        if (intentListCopy.size() == 0) {
                            intent.setPriority(0);
                            return;
                        }
                    }
                    Iterator<String> categoriesIterator = intent.categoriesIterator();
                    if (categoriesIterator != null) {
                        getIntentListSubset(intentListCopy, new CategoriesIterGenerator(), categoriesIterator);
                        if (intentListCopy.size() == 0) {
                            intent.setPriority(0);
                            return;
                        }
                    }
                    Iterator<String> schemesIterator = intent.schemesIterator();
                    if (schemesIterator != null) {
                        getIntentListSubset(intentListCopy, new SchemesIterGenerator(), schemesIterator);
                        if (intentListCopy.size() == 0) {
                            intent.setPriority(0);
                            return;
                        }
                    }
                    Iterator<AuthorityEntry> authoritiesIterator = intent.authoritiesIterator();
                    if (authoritiesIterator != null) {
                        getIntentListSubset(intentListCopy, new AuthoritiesIterGenerator(), authoritiesIterator);
                        if (intentListCopy.size() == 0) {
                            intent.setPriority(0);
                            return;
                        }
                    }
                    int cappedPriority = 0;
                    for (int i = intentListCopy.size() - 1; i >= 0; i--) {
                        cappedPriority = Math.max(cappedPriority, ((ActivityIntentInfo) intentListCopy.get(i)).getPriority());
                    }
                    if (intent.getPriority() > cappedPriority) {
                        intent.setPriority(cappedPriority);
                    }
                } else if (!isProtectedAction(intent)) {
                } else {
                    if (PackageManagerService.this.mDeferProtectedFilters) {
                        PackageManagerService.this.mProtectedFilters.add(intent);
                    } else if (!intent.activity.info.packageName.equals(PackageManagerService.this.mSetupWizardPackage)) {
                        Slog.w(PackageManagerService.TAG, "Protected action; cap priority to 0; package: " + intent.activity.info.packageName + " activity: " + intent.activity.className + " origPrio: " + intent.getPriority());
                        intent.setPriority(0);
                    }
                }
            }
        }

        public final void addActivity(Activity a, String type) {
            this.mActivities.put(a.getComponentName(), a);
            int NI = a.intents.size();
            for (int j = 0; j < NI; j++) {
                ActivityIntentInfo intent = (ActivityIntentInfo) a.intents.get(j);
                if ("activity".equals(type)) {
                    List<Activity> systemActivities;
                    PackageSetting ps = PackageManagerService.this.mSettings.getDisabledSystemPkgLPr(intent.activity.info.packageName);
                    if (ps == null || ps.pkg == null) {
                        systemActivities = null;
                    } else {
                        systemActivities = ps.pkg.activities;
                    }
                    adjustPriority(systemActivities, intent);
                }
                if (!intent.debugCheck()) {
                    Log.w(PackageManagerService.TAG, "==> For Activity " + a.info.name);
                }
                addFilter(intent);
            }
        }

        public final void removeActivity(Activity a, String type) {
            this.mActivities.remove(a.getComponentName());
            int NI = a.intents.size();
            for (int j = 0; j < NI; j++) {
                removeFilter((ActivityIntentInfo) a.intents.get(j));
            }
        }

        protected boolean allowFilterResult(ActivityIntentInfo filter, List<ResolveInfo> dest) {
            ActivityInfo filterAi = filter.activity.info;
            for (int i = dest.size() - 1; i >= 0; i--) {
                ActivityInfo destAi = ((ResolveInfo) dest.get(i)).activityInfo;
                if (destAi.name == filterAi.name && destAi.packageName == filterAi.packageName) {
                    return false;
                }
            }
            return true;
        }

        protected ActivityIntentInfo[] newArray(int size) {
            return new ActivityIntentInfo[size];
        }

        protected boolean isFilterStopped(ActivityIntentInfo filter, int userId) {
            boolean z = false;
            if (!PackageManagerService.sUserManager.exists(userId)) {
                return true;
            }
            Package p = filter.activity.owner;
            if (p != null) {
                PackageSetting ps = p.mExtras;
                if (ps != null) {
                    if ((ps.pkgFlags & 1) == 0 || HwServiceFactory.isCustedCouldStopped(p.packageName, true, ps.getStopped(userId))) {
                        z = ps.getStopped(userId);
                    }
                    return z;
                }
            }
            return false;
        }

        protected boolean isPackageForFilter(String packageName, ActivityIntentInfo info) {
            return packageName.equals(info.activity.owner.packageName);
        }

        protected ResolveInfo newResult(ActivityIntentInfo info, int match, int userId) {
            if (!PackageManagerService.sUserManager.exists(userId) || !PackageManagerService.this.mSettings.isEnabledAndMatchLPr(info.activity.info, this.mFlags, userId)) {
                return null;
            }
            Activity activity = info.activity;
            PackageSetting ps = activity.owner.mExtras;
            if (ps == null) {
                return null;
            }
            if (PackageManagerService.this.mSafeMode && !PackageManagerService.this.isSystemPathApp(ps)) {
                return null;
            }
            ActivityInfo ai = PackageParser.generateActivityInfo(activity, this.mFlags, ps.readUserState(userId), userId);
            if (ai == null) {
                return null;
            }
            ResolveInfo res = new ResolveInfo();
            res.activityInfo = ai;
            if ((this.mFlags & 64) != 0) {
                res.filter = info;
            }
            if (info != null) {
                res.handleAllWebDataURI = info.handleAllWebDataURI();
            }
            res.priority = info.getPriority();
            res.preferredOrder = activity.owner.mPreferredOrder;
            res.match = match;
            res.isDefault = info.hasDefault;
            res.labelRes = info.labelRes;
            res.nonLocalizedLabel = info.nonLocalizedLabel;
            if (PackageManagerService.this.userNeedsBadging(userId)) {
                res.noResourceId = true;
            } else {
                res.icon = info.icon;
            }
            res.iconResourceId = info.icon;
            res.system = res.activityInfo.applicationInfo.isSystemApp();
            return res;
        }

        protected void sortResults(List<ResolveInfo> results) {
            Collections.sort(results, PackageManagerService.mResolvePrioritySorter);
        }

        protected void dumpFilter(PrintWriter out, String prefix, ActivityIntentInfo filter) {
            out.print(prefix);
            out.print(Integer.toHexString(System.identityHashCode(filter.activity)));
            out.print(' ');
            filter.activity.printComponentShortName(out);
            out.print(" filter ");
            out.println(Integer.toHexString(System.identityHashCode(filter)));
        }

        protected Object filterToLabel(ActivityIntentInfo filter) {
            return filter.activity;
        }

        protected void dumpFilterLabel(PrintWriter out, String prefix, Object label, int count) {
            Activity activity = (Activity) label;
            out.print(prefix);
            out.print(Integer.toHexString(System.identityHashCode(activity)));
            out.print(' ');
            activity.printComponentShortName(out);
            if (count > 1) {
                out.print(" (");
                out.print(count);
                out.print(" filters)");
            }
            out.println();
        }
    }

    static abstract class InstallArgs {
        final String abiOverride;
        final Certificate[][] certificates;
        final int installFlags;
        final String[] installGrantPermissions;
        final String installerPackageName;
        String[] instructionSets;
        final MoveInfo move;
        final IPackageInstallObserver2 observer;
        final OriginInfo origin;
        final int traceCookie;
        final String traceMethod;
        final UserHandle user;
        final String volumeUuid;

        abstract void cleanUpResourcesLI();

        abstract int copyApk(IMediaContainerService iMediaContainerService, boolean z) throws RemoteException;

        abstract boolean doPostDeleteLI(boolean z);

        abstract int doPostInstall(int i, int i2);

        abstract int doPreInstall(int i);

        abstract boolean doRename(int i, Package packageR, String str);

        abstract String getCodePath();

        abstract String getResourcePath();

        InstallArgs(OriginInfo origin, MoveInfo move, IPackageInstallObserver2 observer, int installFlags, String installerPackageName, String volumeUuid, UserHandle user, String[] instructionSets, String abiOverride, String[] installGrantPermissions, String traceMethod, int traceCookie, Certificate[][] certificates) {
            this.origin = origin;
            this.move = move;
            this.installFlags = installFlags;
            this.observer = observer;
            this.installerPackageName = installerPackageName;
            this.volumeUuid = volumeUuid;
            this.user = user;
            this.instructionSets = instructionSets;
            this.abiOverride = abiOverride;
            this.installGrantPermissions = installGrantPermissions;
            this.traceMethod = traceMethod;
            this.traceCookie = traceCookie;
            this.certificates = certificates;
        }

        int doPreCopy() {
            return 1;
        }

        int doPostCopy(int uid) {
            return 1;
        }

        protected boolean isFwdLocked() {
            return (this.installFlags & 1) != 0;
        }

        protected boolean isExternalAsec() {
            return (this.installFlags & 8) != 0;
        }

        protected boolean isEphemeral() {
            return (this.installFlags & 2048) != 0;
        }

        UserHandle getUser() {
            return this.user;
        }
    }

    class AsecInstallArgs extends InstallArgs {
        static final String PUBLIC_RES_FILE_NAME = "res.zip";
        static final String RES_FILE_NAME = "pkg.apk";
        String cid;
        String packagePath;
        String resourcePath;

        AsecInstallArgs(InstallParams params) {
            super(params.origin, params.move, params.observer, params.installFlags, params.installerPackageName, params.volumeUuid, params.getUser(), null, params.packageAbiOverride, params.grantedRuntimePermissions, params.traceMethod, params.traceCookie, params.certificates);
        }

        AsecInstallArgs(String fullCodePath, String[] instructionSets, boolean isExternal, boolean isForwardLocked) {
            super(OriginInfo.fromNothing(), null, null, (isExternal ? 8 : 0) | (isForwardLocked ? 1 : 0), null, null, null, instructionSets, null, null, null, 0, null);
            if (!fullCodePath.endsWith(RES_FILE_NAME)) {
                fullCodePath = new File(fullCodePath, RES_FILE_NAME).getAbsolutePath();
            }
            int eidx = fullCodePath.lastIndexOf("/");
            String subStr1 = fullCodePath.substring(0, eidx);
            this.cid = subStr1.substring(subStr1.lastIndexOf("/") + 1, eidx);
            setMountPath(subStr1);
        }

        AsecInstallArgs(String cid, String[] instructionSets, boolean isForwardLocked) {
            super(OriginInfo.fromNothing(), null, null, (PackageManagerService.this.isAsecExternal(cid) ? 8 : 0) | (isForwardLocked ? 1 : 0), null, null, null, instructionSets, null, null, null, 0, null);
            this.cid = cid;
            String sdDir = PackageHelper.getSdDir(cid);
            if (sdDir != null) {
                setMountPath(sdDir);
            }
        }

        void createCopyFile() {
            this.cid = PackageManagerService.this.mInstallerService.allocateExternalStageCidLegacy();
        }

        int copyApk(IMediaContainerService imcs, boolean temp) throws RemoteException {
            if (!this.origin.staged || this.origin.cid == null) {
                if (temp) {
                    createCopyFile();
                } else {
                    PackageHelper.destroySdDir(this.cid);
                }
                String newMountPath = imcs.copyPackageToContainer(this.origin.file.getAbsolutePath(), this.cid, PackageManagerService.getEncryptKey(), isExternalAsec(), isFwdLocked(), PackageManagerService.deriveAbiOverride(this.abiOverride, null));
                if (newMountPath != null) {
                    setMountPath(newMountPath);
                    return 1;
                }
                String reason = "DCS:cPTC;f(" + this.origin.file.getAbsolutePath() + ")c(" + this.cid + ")iExA(" + isExternalAsec() + ")iFL(" + isFwdLocked() + ")";
                FrameworkRadar.msg(65, FrameworkRadar.RADAR_FWK_ERR_INSTALL_SD, "AIA::cA", reason);
                PackageManagerService.this.uploadInstallErrRadar(reason);
                return -18;
            }
            this.cid = this.origin.cid;
            setMountPath(PackageHelper.getSdDir(this.cid));
            return 1;
        }

        String getCodePath() {
            return this.packagePath;
        }

        String getResourcePath() {
            return this.resourcePath;
        }

        int doPreInstall(int status) {
            if (status != 1) {
                PackageHelper.destroySdDir(this.cid);
            } else if (!PackageHelper.isContainerMounted(this.cid)) {
                String newMountPath = PackageHelper.mountSdDir(this.cid, PackageManagerService.getEncryptKey(), 1000);
                if (newMountPath != null) {
                    setMountPath(newMountPath);
                } else {
                    String reason = "PH:mSdD;c(" + this.cid + ")";
                    FrameworkRadar.msg(65, FrameworkRadar.RADAR_FWK_ERR_INSTALL_SD, "AIA::dPrI", reason);
                    PackageManagerService.this.uploadInstallErrRadar(reason);
                    return -18;
                }
            }
            return status;
        }

        boolean doRename(int status, Package pkg, String oldCodePath) {
            String newCacheId = PackageManagerService.getNextCodePath(oldCodePath, pkg.packageName, "/pkg.apk");
            if (!PackageHelper.isContainerMounted(this.cid) || PackageHelper.unMountSdDir(this.cid)) {
                String newMountPath;
                if (!PackageHelper.renameSdDir(this.cid, newCacheId)) {
                    Slog.e(PackageManagerService.TAG, "Failed to rename " + this.cid + " to " + newCacheId + " which might be stale. Will try to clean up.");
                    if (!PackageHelper.destroySdDir(newCacheId)) {
                        Slog.e(PackageManagerService.TAG, "Very strange. Cannot clean up stale container " + newCacheId);
                        return false;
                    } else if (!PackageHelper.renameSdDir(this.cid, newCacheId)) {
                        Slog.e(PackageManagerService.TAG, "Failed to rename " + this.cid + " to " + newCacheId + " inspite of cleaning it up.");
                        return false;
                    }
                }
                if (PackageHelper.isContainerMounted(newCacheId)) {
                    newMountPath = PackageHelper.getSdDir(newCacheId);
                } else {
                    Slog.w(PackageManagerService.TAG, "Mounting container " + newCacheId);
                    newMountPath = PackageHelper.mountSdDir(newCacheId, PackageManagerService.getEncryptKey(), 1000);
                }
                if (newMountPath == null) {
                    Slog.w(PackageManagerService.TAG, "Failed to get cache path for  " + newCacheId);
                    return false;
                }
                Log.i(PackageManagerService.TAG, "Succesfully renamed " + this.cid + " to " + newCacheId + " at new path: " + newMountPath);
                this.cid = newCacheId;
                File beforeCodeFile = new File(this.packagePath);
                setMountPath(newMountPath);
                File afterCodeFile = new File(this.packagePath);
                pkg.setCodePath(afterCodeFile.getAbsolutePath());
                pkg.setBaseCodePath(FileUtils.rewriteAfterRename(beforeCodeFile, afterCodeFile, pkg.baseCodePath));
                pkg.setSplitCodePaths(FileUtils.rewriteAfterRename(beforeCodeFile, afterCodeFile, pkg.splitCodePaths));
                pkg.setApplicationVolumeUuid(pkg.volumeUuid);
                pkg.setApplicationInfoCodePath(pkg.codePath);
                pkg.setApplicationInfoBaseCodePath(pkg.baseCodePath);
                pkg.setApplicationInfoSplitCodePaths(pkg.splitCodePaths);
                pkg.setApplicationInfoResourcePath(pkg.codePath);
                pkg.setApplicationInfoBaseResourcePath(pkg.baseCodePath);
                pkg.setApplicationInfoSplitResourcePaths(pkg.splitCodePaths);
                return true;
            }
            Slog.i(PackageManagerService.TAG, "Failed to unmount " + this.cid + " before renaming");
            return false;
        }

        private void setMountPath(String mountPath) {
            File mountFile = new File(mountPath);
            File monolithicFile = new File(mountFile, RES_FILE_NAME);
            if (monolithicFile.exists()) {
                this.packagePath = monolithicFile.getAbsolutePath();
                if (isFwdLocked()) {
                    this.resourcePath = new File(mountFile, PUBLIC_RES_FILE_NAME).getAbsolutePath();
                    return;
                } else {
                    this.resourcePath = this.packagePath;
                    return;
                }
            }
            this.packagePath = mountFile.getAbsolutePath();
            this.resourcePath = this.packagePath;
        }

        int doPostInstall(int status, int uid) {
            if (status != 1) {
                cleanUp();
                PackageManagerService.this.hwCertCleanUp();
            } else {
                int groupOwner;
                String str;
                if (isFwdLocked()) {
                    groupOwner = UserHandle.getSharedAppGid(uid);
                    str = RES_FILE_NAME;
                } else {
                    groupOwner = -1;
                    str = null;
                }
                if (uid < 10000 || !PackageHelper.fixSdPermissions(this.cid, groupOwner, r2)) {
                    Slog.e(PackageManagerService.TAG, "Failed to finalize " + this.cid);
                    PackageHelper.destroySdDir(this.cid);
                    String reason = "PH:fSP;c(" + this.cid + ")g(" + groupOwner + ")";
                    FrameworkRadar.msg(65, FrameworkRadar.RADAR_FWK_ERR_INSTALL_SD, "AIA::dPstI", reason);
                    PackageManagerService.this.uploadInstallErrRadar(reason);
                    return -18;
                } else if (!PackageHelper.isContainerMounted(this.cid)) {
                    PackageHelper.mountSdDir(this.cid, PackageManagerService.getEncryptKey(), Process.myUid());
                }
            }
            return status;
        }

        private void cleanUp() {
            PackageHelper.destroySdDir(this.cid);
        }

        private List<String> getAllCodePaths() {
            File codeFile = new File(getCodePath());
            if (codeFile != null && codeFile.exists()) {
                try {
                    return PackageParser.parsePackageLite(codeFile, 0).getAllCodePaths();
                } catch (PackageParserException e) {
                }
            }
            return Collections.EMPTY_LIST;
        }

        void cleanUpResourcesLI() {
            cleanUpResourcesLI(getAllCodePaths());
        }

        private void cleanUpResourcesLI(List<String> allCodePaths) {
            cleanUp();
            PackageManagerService.this.removeDexFiles(allCodePaths, this.instructionSets);
        }

        String getPackageName() {
            return PackageManagerService.getAsecPackageName(this.cid);
        }

        boolean doPostDeleteLI(boolean delete) {
            List<String> allCodePaths = getAllCodePaths();
            boolean mounted = PackageHelper.isContainerMounted(this.cid);
            if (mounted && PackageHelper.unMountSdDir(this.cid)) {
                mounted = false;
            }
            if (!mounted && delete) {
                cleanUpResourcesLI(allCodePaths);
            }
            return !mounted;
        }

        int doPreCopy() {
            if (!isFwdLocked() || PackageHelper.fixSdPermissions(this.cid, PackageManagerService.this.getPackageUid(PackageManagerService.DEFAULT_CONTAINER_PACKAGE, DumpState.DUMP_DEXOPT, 0), RES_FILE_NAME)) {
                return 1;
            }
            String reason = "PH:fSP;c(" + this.cid + ")g(" + PackageManagerService.this.getPackageUid(PackageManagerService.DEFAULT_CONTAINER_PACKAGE, 268435456, 0) + ")";
            FrameworkRadar.msg(65, FrameworkRadar.RADAR_FWK_ERR_INSTALL_SD, "AIA::dPrC", reason);
            PackageManagerService.this.uploadInstallErrRadar(reason);
            return -18;
        }

        int doPostCopy(int uid) {
            if (!isFwdLocked() || (uid >= 10000 && PackageHelper.fixSdPermissions(this.cid, UserHandle.getSharedAppGid(uid), RES_FILE_NAME))) {
                return 1;
            }
            Slog.e(PackageManagerService.TAG, "Failed to finalize " + this.cid);
            PackageHelper.destroySdDir(this.cid);
            String reason = "PH:dPstC;c(" + this.cid + ")g(" + UserHandle.getSharedAppGid(uid) + ")";
            FrameworkRadar.msg(65, FrameworkRadar.RADAR_FWK_ERR_INSTALL_SD, "AIA::dPstC", reason);
            PackageManagerService.this.uploadInstallErrRadar(reason);
            return -18;
        }
    }

    private final class ClearStorageConnection implements ServiceConnection {
        IMediaContainerService mContainerService;

        private ClearStorageConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (this) {
                this.mContainerService = Stub.asInterface(service);
                notifyAll();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (this) {
                if (this.mContainerService == null) {
                    Slog.w(PackageManagerService.TAG, "onServiceDisconnected unknown reason");
                    notifyAll();
                }
            }
        }
    }

    private static class CrossProfileDomainInfo {
        int bestDomainVerificationStatus;
        ResolveInfo resolveInfo;

        private CrossProfileDomainInfo() {
        }
    }

    class DefaultContainerConnection implements ServiceConnection {
        DefaultContainerConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            PackageManagerService.this.mHandler.sendMessage(PackageManagerService.this.mHandler.obtainMessage(3, Stub.asInterface(service)));
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    }

    static class DumpState {
        public static final int DUMP_ACTIVITY_RESOLVERS = 4;
        public static final int DUMP_CONTENT_RESOLVERS = 32;
        public static final int DUMP_DEXOPT = 1048576;
        public static final int DUMP_DOMAIN_PREFERRED = 262144;
        public static final int DUMP_FEATURES = 2;
        public static final int DUMP_FROZEN = 524288;
        public static final int DUMP_INSTALLS = 65536;
        public static final int DUMP_INTENT_FILTER_VERIFIERS = 131072;
        public static final int DUMP_KEYSETS = 16384;
        public static final int DUMP_LIBS = 1;
        public static final int DUMP_MESSAGES = 512;
        public static final int DUMP_PACKAGES = 128;
        public static final int DUMP_PERMISSIONS = 64;
        public static final int DUMP_PREFERRED = 4096;
        public static final int DUMP_PREFERRED_XML = 8192;
        public static final int DUMP_PROVIDERS = 1024;
        public static final int DUMP_RECEIVER_RESOLVERS = 16;
        public static final int DUMP_SERVICE_RESOLVERS = 8;
        public static final int DUMP_SHARED_USERS = 256;
        public static final int DUMP_VERIFIERS = 2048;
        public static final int DUMP_VERSION = 32768;
        public static final int OPTION_SHOW_FILTERS = 1;
        private int mOptions;
        private SharedUserSetting mSharedUser;
        private boolean mTitlePrinted;
        private int mTypes;

        DumpState() {
        }

        public boolean isDumping(int type) {
            boolean z = true;
            if (this.mTypes == 0 && type != 8192) {
                return true;
            }
            if ((this.mTypes & type) == 0) {
                z = false;
            }
            return z;
        }

        public void setDump(int type) {
            this.mTypes |= type;
        }

        public boolean isOptionEnabled(int option) {
            return (this.mOptions & option) != 0;
        }

        public void setOptionEnabled(int option) {
            this.mOptions |= option;
        }

        public boolean onTitlePrinted() {
            boolean printed = this.mTitlePrinted;
            this.mTitlePrinted = true;
            return printed;
        }

        public boolean getTitlePrinted() {
            return this.mTitlePrinted;
        }

        public void setTitlePrinted(boolean enabled) {
            this.mTitlePrinted = enabled;
        }

        public SharedUserSetting getSharedUser() {
            return this.mSharedUser;
        }

        public void setSharedUser(SharedUserSetting user) {
            this.mSharedUser = user;
        }
    }

    private static final class EphemeralIntentResolver extends IntentResolver<EphemeralResolveIntentInfo, EphemeralResolveInfo> {
        private EphemeralIntentResolver() {
        }

        protected EphemeralResolveIntentInfo[] newArray(int size) {
            return new EphemeralResolveIntentInfo[size];
        }

        protected boolean isPackageForFilter(String packageName, EphemeralResolveIntentInfo info) {
            return true;
        }

        protected EphemeralResolveInfo newResult(EphemeralResolveIntentInfo info, int match, int userId) {
            if (PackageManagerService.sUserManager.exists(userId)) {
                return info.getEphemeralResolveInfo();
            }
            return null;
        }
    }

    class FileInstallArgs extends InstallArgs {
        private File codeFile;
        private File resourceFile;

        private int doCopyApk(com.android.internal.app.IMediaContainerService r11, boolean r12) throws android.os.RemoteException {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:28:? in {3, 7, 12, 13, 16, 20, 25, 27, 29, 30} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r10 = this;
            r9 = 1;
            r7 = r10.origin;
            r7 = r7.staged;
            if (r7 == 0) goto L_0x0014;
        L_0x0007:
            r7 = r10.origin;
            r7 = r7.file;
            r10.codeFile = r7;
            r7 = r10.origin;
            r7 = r7.file;
            r10.resourceFile = r7;
            return r9;
        L_0x0014:
            r7 = r10.installFlags;	 Catch:{ IOException -> 0x0049 }
            r7 = r7 & 2048;	 Catch:{ IOException -> 0x0049 }
            if (r7 == 0) goto L_0x0047;	 Catch:{ IOException -> 0x0049 }
        L_0x001a:
            r2 = 1;	 Catch:{ IOException -> 0x0049 }
        L_0x001b:
            r7 = com.android.server.pm.PackageManagerService.this;	 Catch:{ IOException -> 0x0049 }
            r7 = r7.mInstallerService;	 Catch:{ IOException -> 0x0049 }
            r8 = r10.volumeUuid;	 Catch:{ IOException -> 0x0049 }
            r6 = r7.allocateStageDirLegacy(r8, r2);	 Catch:{ IOException -> 0x0049 }
            r10.codeFile = r6;	 Catch:{ IOException -> 0x0049 }
            r10.resourceFile = r6;	 Catch:{ IOException -> 0x0049 }
            r5 = new com.android.server.pm.PackageManagerService$FileInstallArgs$1;
            r5.<init>();
            r4 = 1;
            r7 = r10.origin;
            r7 = r7.file;
            r7 = r7.getAbsolutePath();
            r4 = r11.copyPackage(r7, r5);
            if (r4 == r9) goto L_0x0066;
        L_0x003d:
            r7 = "PackageManager";
            r8 = "Failed to copy package";
            android.util.Slog.e(r7, r8);
            return r4;
        L_0x0047:
            r2 = 0;
            goto L_0x001b;
        L_0x0049:
            r0 = move-exception;
            r7 = "PackageManager";
            r8 = new java.lang.StringBuilder;
            r8.<init>();
            r9 = "Failed to create copy file: ";
            r8 = r8.append(r9);
            r8 = r8.append(r0);
            r8 = r8.toString();
            android.util.Slog.w(r7, r8);
            r7 = -4;
            return r7;
        L_0x0066:
            r3 = new java.io.File;
            r7 = r10.codeFile;
            r8 = "lib";
            r3.<init>(r7, r8);
            r1 = 0;
            r7 = r10.codeFile;	 Catch:{ IOException -> 0x0081, all -> 0x0091 }
            r1 = com.android.internal.content.NativeLibraryHelper.Handle.create(r7);	 Catch:{ IOException -> 0x0081, all -> 0x0091 }
            r7 = r10.abiOverride;	 Catch:{ IOException -> 0x0081, all -> 0x0091 }
            r4 = com.android.internal.content.NativeLibraryHelper.copyNativeBinariesWithOverride(r1, r3, r7);	 Catch:{ IOException -> 0x0081, all -> 0x0091 }
            libcore.io.IoUtils.closeQuietly(r1);
        L_0x0080:
            return r4;
        L_0x0081:
            r0 = move-exception;
            r7 = "PackageManager";	 Catch:{ IOException -> 0x0081, all -> 0x0091 }
            r8 = "Copying native libraries failed";	 Catch:{ IOException -> 0x0081, all -> 0x0091 }
            android.util.Slog.e(r7, r8, r0);	 Catch:{ IOException -> 0x0081, all -> 0x0091 }
            r4 = -110; // 0xffffffffffffff92 float:NaN double:NaN;
            libcore.io.IoUtils.closeQuietly(r1);
            goto L_0x0080;
        L_0x0091:
            r7 = move-exception;
            libcore.io.IoUtils.closeQuietly(r1);
            throw r7;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.PackageManagerService.FileInstallArgs.doCopyApk(com.android.internal.app.IMediaContainerService, boolean):int");
        }

        FileInstallArgs(InstallParams params) {
            super(params.origin, params.move, params.observer, params.installFlags, params.installerPackageName, params.volumeUuid, params.getUser(), null, params.packageAbiOverride, params.grantedRuntimePermissions, params.traceMethod, params.traceCookie, params.certificates);
            if (isFwdLocked()) {
                throw new IllegalArgumentException("Forward locking only supported in ASEC");
            }
        }

        FileInstallArgs(String codePath, String resourcePath, String[] instructionSets) {
            super(OriginInfo.fromNothing(), null, null, 0, null, null, null, instructionSets, null, null, null, 0, null);
            this.codeFile = codePath != null ? new File(codePath) : null;
            this.resourceFile = resourcePath != null ? new File(resourcePath) : null;
        }

        int copyApk(IMediaContainerService imcs, boolean temp) throws RemoteException {
            Trace.traceBegin(262144, "copyApk");
            try {
                int doCopyApk = doCopyApk(imcs, temp);
                return doCopyApk;
            } finally {
                Trace.traceEnd(262144);
            }
        }

        int doPreInstall(int status) {
            if (status != 1) {
                cleanUp();
            }
            return status;
        }

        boolean doRename(int status, Package pkg, String oldCodePath) {
            if (status != 1) {
                cleanUp();
                return false;
            }
            File targetDir = this.codeFile.getParentFile();
            File beforeCodeFile = this.codeFile;
            File afterCodeFile = PackageManagerService.this.getNextCodePath(targetDir, pkg.packageName);
            try {
                Os.rename(beforeCodeFile.getAbsolutePath(), afterCodeFile.getAbsolutePath());
                if (SELinux.restoreconRecursive(afterCodeFile)) {
                    this.codeFile = afterCodeFile;
                    this.resourceFile = afterCodeFile;
                    pkg.setCodePath(afterCodeFile.getAbsolutePath());
                    pkg.setBaseCodePath(FileUtils.rewriteAfterRename(beforeCodeFile, afterCodeFile, pkg.baseCodePath));
                    pkg.setSplitCodePaths(FileUtils.rewriteAfterRename(beforeCodeFile, afterCodeFile, pkg.splitCodePaths));
                    pkg.setApplicationVolumeUuid(pkg.volumeUuid);
                    pkg.setApplicationInfoCodePath(pkg.codePath);
                    pkg.setApplicationInfoBaseCodePath(pkg.baseCodePath);
                    pkg.setApplicationInfoSplitCodePaths(pkg.splitCodePaths);
                    pkg.setApplicationInfoResourcePath(pkg.codePath);
                    pkg.setApplicationInfoBaseResourcePath(pkg.baseCodePath);
                    pkg.setApplicationInfoSplitResourcePaths(pkg.splitCodePaths);
                    return true;
                }
                Slog.w(PackageManagerService.TAG, "Failed to restorecon");
                return false;
            } catch (ErrnoException e) {
                Slog.w(PackageManagerService.TAG, "Failed to rename", e);
                return false;
            }
        }

        int doPostInstall(int status, int uid) {
            if (status != 1) {
                cleanUp();
                PackageManagerService.this.hwCertCleanUp();
            }
            return status;
        }

        String getCodePath() {
            return this.codeFile != null ? this.codeFile.getAbsolutePath() : null;
        }

        String getResourcePath() {
            return this.resourceFile != null ? this.resourceFile.getAbsolutePath() : null;
        }

        private boolean cleanUp() {
            if (this.codeFile == null || !this.codeFile.exists()) {
                return false;
            }
            PackageManagerService.this.removeCodePathLI(this.codeFile);
            if (!(this.resourceFile == null || FileUtils.contains(this.codeFile, this.resourceFile))) {
                this.resourceFile.delete();
            }
            return true;
        }

        void cleanUpResourcesLI() {
            List<String> allCodePaths = Collections.EMPTY_LIST;
            if (this.codeFile != null && this.codeFile.exists()) {
                try {
                    allCodePaths = PackageParser.parsePackageLite(this.codeFile, 0).getAllCodePaths();
                } catch (PackageParserException e) {
                }
            }
            cleanUp();
            PackageManagerService.this.removeDexFiles(allCodePaths, this.instructionSets);
        }

        boolean doPostDeleteLI(boolean delete) {
            cleanUpResourcesLI();
            return true;
        }
    }

    private abstract class HandlerParams {
        private static final int MAX_RETRIES = 4;
        private int mRetries = 0;
        private final UserHandle mUser;
        int traceCookie;
        String traceMethod;

        abstract void handleReturnCode();

        abstract void handleServiceError();

        abstract void handleStartCopy() throws RemoteException;

        HandlerParams(UserHandle user) {
            this.mUser = user;
        }

        UserHandle getUser() {
            return this.mUser;
        }

        HandlerParams setTraceMethod(String traceMethod) {
            this.traceMethod = traceMethod;
            return this;
        }

        HandlerParams setTraceCookie(int traceCookie) {
            this.traceCookie = traceCookie;
            return this;
        }

        final boolean startCopy() {
            boolean res;
            try {
                int i = this.mRetries + 1;
                this.mRetries = i;
                if (i > 4) {
                    Slog.w(PackageManagerService.TAG, "Failed to invoke remote methods on default container service. Giving up");
                    PackageManagerService.this.mHandler.sendEmptyMessage(11);
                    handleServiceError();
                    return false;
                }
                handleStartCopy();
                res = true;
                if (!(this instanceof MeasureParams)) {
                    handleReturnCode();
                }
                return res;
            } catch (RemoteException e) {
                PackageManagerService.this.mHandler.sendEmptyMessage(10);
                res = false;
            } catch (Exception e2) {
                Log.e(PackageManagerService.TAG, "Posting install MCS_GIVE_UP");
                PackageManagerService.this.mHandler.sendEmptyMessage(11);
                res = false;
            }
        }

        final void serviceError() {
            handleServiceError();
            handleReturnCode();
        }
    }

    private static class IFVerificationParams {
        Package pkg;
        boolean replacing;
        int userId;
        int verifierUid;

        public IFVerificationParams(Package _pkg, boolean _replacing, int _userId, int _verifierUid) {
            this.pkg = _pkg;
            this.replacing = _replacing;
            this.userId = _userId;
            this.replacing = _replacing;
            this.verifierUid = _verifierUid;
        }
    }

    class InstallParams extends HandlerParams {
        final Certificate[][] certificates;
        final String[] grantedRuntimePermissions;
        int installFlags;
        final String installerPackageName;
        private InstallArgs mArgs;
        private int mRet;
        final MoveInfo move;
        final IPackageInstallObserver2 observer;
        final OriginInfo origin;
        final String packageAbiOverride;
        final VerificationInfo verificationInfo;
        final String volumeUuid;

        InstallParams(OriginInfo origin, MoveInfo move, IPackageInstallObserver2 observer, int installFlags, String installerPackageName, String volumeUuid, VerificationInfo verificationInfo, UserHandle user, String packageAbiOverride, String[] grantedPermissions, Certificate[][] certificates) {
            super(user);
            this.origin = origin;
            this.move = move;
            this.observer = observer;
            this.installFlags = installFlags;
            this.installerPackageName = installerPackageName;
            this.volumeUuid = volumeUuid;
            this.verificationInfo = verificationInfo;
            this.packageAbiOverride = packageAbiOverride;
            this.grantedRuntimePermissions = grantedPermissions;
            this.certificates = certificates;
        }

        public String toString() {
            return "InstallParams{" + Integer.toHexString(System.identityHashCode(this)) + " file=" + this.origin.file + " cid=" + this.origin.cid + "}";
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private int installLocationPolicy(PackageInfoLite pkgLite) {
            boolean downgradePermitted = false;
            String packageName = pkgLite.packageName;
            int installLocation = pkgLite.installLocation;
            boolean onSd = (this.installFlags & 8) != 0;
            synchronized (PackageManagerService.this.mPackages) {
                Package installedPkg = (Package) PackageManagerService.this.mPackages.get(packageName);
                Package dataOwnerPkg = installedPkg;
                if (installedPkg == null) {
                    PackageSetting ps = (PackageSetting) PackageManagerService.this.mSettings.mPackages.get(packageName);
                    if (ps != null) {
                        dataOwnerPkg = ps.pkg;
                    }
                }
                if (dataOwnerPkg != null) {
                    boolean downgradeRequested = (this.installFlags & 128) != 0;
                    boolean packageDebuggable = (dataOwnerPkg.applicationInfo.flags & 2) != 0;
                    if (downgradeRequested) {
                        if (Build.IS_DEBUGGABLE) {
                            downgradePermitted = true;
                        } else {
                            downgradePermitted = packageDebuggable;
                        }
                    }
                    if (!downgradePermitted) {
                        try {
                            PackageManagerService.checkDowngrade(dataOwnerPkg, pkgLite);
                        } catch (PackageManagerException e) {
                            Slog.w(PackageManagerService.TAG, "Downgrade detected: " + e.getMessage());
                            return -7;
                        }
                    }
                }
                if (installedPkg != null) {
                    if ((this.installFlags & 2) == 0) {
                        return -4;
                    } else if ((installedPkg.applicationInfo.flags & 1) != 0) {
                        if (onSd) {
                            Slog.w(PackageManagerService.TAG, "Cannot install update to system app on sdcard");
                            return -3;
                        }
                        return 1;
                    } else if (onSd) {
                        return 2;
                    } else if (installLocation == 1) {
                        return 1;
                    } else if (installLocation != 2) {
                        if (PackageManagerService.isExternal(installedPkg)) {
                            return 2;
                        }
                        return 1;
                    }
                }
            }
        }

        public void handleStartCopy() throws RemoteException {
            int ret = 1;
            if (this.origin.staged) {
                if (this.origin.file != null) {
                    this.installFlags |= 16;
                    this.installFlags &= -9;
                } else if (this.origin.cid != null) {
                    this.installFlags |= 8;
                    this.installFlags &= -17;
                } else {
                    throw new IllegalStateException("Invalid stage location");
                }
            }
            boolean onSd = (this.installFlags & 8) != 0;
            boolean onInt = (this.installFlags & 16) != 0;
            boolean ephemeral = (this.installFlags & 2048) != 0;
            PackageInfoLite pkgLite = null;
            if (onInt && onSd) {
                Slog.w(PackageManagerService.TAG, "Conflicting flags specified for installing on both internal and external");
                ret = -19;
            } else if (onSd && ephemeral) {
                Slog.w(PackageManagerService.TAG, "Conflicting flags specified for installing ephemeral on external");
                ret = -19;
            } else {
                pkgLite = PackageManagerService.this.mContainerService.getMinimalPackageInfo(this.origin.resolvedPath, this.installFlags, this.packageAbiOverride);
                if (!this.origin.staged && pkgLite.recommendedInstallLocation == -1) {
                    try {
                        PackageManagerService.this.mInstaller.freeCache(null, PackageManagerService.this.mContainerService.calculateInstalledSize(this.origin.resolvedPath, isForwardLocked(), this.packageAbiOverride) + StorageManager.from(PackageManagerService.this.mContext).getStorageLowBytes(Environment.getDataDirectory()));
                        pkgLite = PackageManagerService.this.mContainerService.getMinimalPackageInfo(this.origin.resolvedPath, this.installFlags, this.packageAbiOverride);
                    } catch (InstallerException e) {
                        Slog.w(PackageManagerService.TAG, "Failed to free cache", e);
                    }
                    if (pkgLite.recommendedInstallLocation == -6) {
                        pkgLite.recommendedInstallLocation = -1;
                    }
                }
            }
            if (ret == 1) {
                int loc = pkgLite.recommendedInstallLocation;
                if (loc == -3) {
                    ret = -19;
                } else if (loc == -4) {
                    ret = -1;
                } else if (loc == -1) {
                    ret = -4;
                } else if (loc == -2) {
                    ret = -2;
                } else if (loc == -6) {
                    ret = -3;
                } else if (loc == -5) {
                    ret = -20;
                } else {
                    loc = installLocationPolicy(pkgLite);
                    if (loc == -7) {
                        ret = -25;
                    } else if (!(onSd || onInt)) {
                        if (loc == 2) {
                            this.installFlags |= 8;
                            this.installFlags &= -17;
                        } else if (loc == 3) {
                            this.installFlags |= 2048;
                            this.installFlags &= -25;
                        } else {
                            this.installFlags |= 16;
                            this.installFlags &= -9;
                        }
                    }
                }
            }
            InstallArgs args = PackageManagerService.this.createInstallArgs(this);
            this.mArgs = args;
            if (ret == 1) {
                int requiredUid;
                UserHandle verifierUser = getUser();
                if (verifierUser == UserHandle.ALL) {
                    verifierUser = UserHandle.SYSTEM;
                }
                if (PackageManagerService.this.mRequiredVerifierPackage == null) {
                    requiredUid = -1;
                } else {
                    requiredUid = PackageManagerService.this.getPackageUid(PackageManagerService.this.mRequiredVerifierPackage, 268435456, verifierUser.getIdentifier());
                }
                if (this.origin.existing || requiredUid == -1 || !PackageManagerService.this.isVerificationEnabled(verifierUser.getIdentifier(), this.installFlags)) {
                    ret = args.copyApk(PackageManagerService.this.mContainerService, true);
                } else {
                    Intent verification = new Intent("android.intent.action.PACKAGE_NEEDS_VERIFICATION");
                    verification.addFlags(268435456);
                    verification.setDataAndType(Uri.fromFile(new File(this.origin.resolvedPath)), PackageManagerService.PACKAGE_MIME_TYPE);
                    verification.addFlags(1);
                    List<ResolveInfo> receivers = PackageManagerService.this.queryIntentReceiversInternal(verification, PackageManagerService.PACKAGE_MIME_TYPE, 0, verifierUser.getIdentifier());
                    PackageManagerService packageManagerService = PackageManagerService.this;
                    int verificationId = packageManagerService.mPendingVerificationToken;
                    packageManagerService.mPendingVerificationToken = verificationId + 1;
                    verification.putExtra("android.content.pm.extra.VERIFICATION_ID", verificationId);
                    verification.putExtra("android.content.pm.extra.VERIFICATION_INSTALLER_PACKAGE", this.installerPackageName);
                    verification.putExtra("android.content.pm.extra.VERIFICATION_INSTALL_FLAGS", this.installFlags);
                    verification.putExtra("android.content.pm.extra.VERIFICATION_PACKAGE_NAME", pkgLite.packageName);
                    verification.putExtra("android.content.pm.extra.VERIFICATION_VERSION_CODE", pkgLite.versionCode);
                    if (this.verificationInfo != null) {
                        if (this.verificationInfo.originatingUri != null) {
                            verification.putExtra("android.intent.extra.ORIGINATING_URI", this.verificationInfo.originatingUri);
                        }
                        if (this.verificationInfo.referrer != null) {
                            verification.putExtra("android.intent.extra.REFERRER", this.verificationInfo.referrer);
                        }
                        if (this.verificationInfo.originatingUid >= 0) {
                            verification.putExtra("android.intent.extra.ORIGINATING_UID", this.verificationInfo.originatingUid);
                        }
                        if (this.verificationInfo.installerUid >= 0) {
                            verification.putExtra("android.content.pm.extra.VERIFICATION_INSTALLER_UID", this.verificationInfo.installerUid);
                        }
                    }
                    PackageVerificationState packageVerificationState = new PackageVerificationState(requiredUid, args);
                    PackageManagerService.this.mPendingVerification.append(verificationId, packageVerificationState);
                    List<ComponentName> sufficientVerifiers = PackageManagerService.this.matchVerifiers(pkgLite, receivers, packageVerificationState);
                    if (sufficientVerifiers != null) {
                        int N = sufficientVerifiers.size();
                        if (N == 0) {
                            Slog.i(PackageManagerService.TAG, "Additional verifiers required, but none installed.");
                            ret = -22;
                        } else {
                            for (int i = 0; i < N; i++) {
                                ComponentName verifierComponent = (ComponentName) sufficientVerifiers.get(i);
                                Intent intent = new Intent(verification);
                                intent.setComponent(verifierComponent);
                                PackageManagerService.this.mContext.sendBroadcastAsUser(intent, verifierUser);
                            }
                        }
                    }
                    ComponentName requiredVerifierComponent = PackageManagerService.this.matchComponentForVerifier(PackageManagerService.this.mRequiredVerifierPackage, receivers);
                    if (ret == 1 && PackageManagerService.this.mRequiredVerifierPackage != null) {
                        Trace.asyncTraceBegin(262144, "verification", verificationId);
                        verification.setComponent(requiredVerifierComponent);
                        final int i2 = verificationId;
                        PackageManagerService.this.mContext.sendOrderedBroadcastAsUser(verification, verifierUser, "android.permission.PACKAGE_VERIFICATION_AGENT", new BroadcastReceiver() {
                            public void onReceive(Context context, Intent intent) {
                                Message msg = PackageManagerService.this.mHandler.obtainMessage(16);
                                msg.arg1 = i2;
                                PackageManagerService.this.mHandler.sendMessageDelayed(msg, PackageManagerService.this.getVerificationTimeout());
                            }
                        }, null, 0, null, null);
                        this.mArgs = null;
                    }
                }
            }
            this.mRet = ret;
        }

        void handleReturnCode() {
            if (this.mArgs != null) {
                PackageManagerService.this.processPendingInstall(this.mArgs, this.mRet);
            }
        }

        void handleServiceError() {
            this.mArgs = PackageManagerService.this.createInstallArgs(this);
            this.mRet = -110;
        }

        public boolean isForwardLocked() {
            return (this.installFlags & 1) != 0;
        }
    }

    private interface IntentFilterVerifier<T extends IntentFilter> {
        boolean addOneIntentFilterVerification(int i, int i2, int i3, T t, String str);

        void receiveVerificationResponse(int i);

        void startVerifications(int i);
    }

    private class IntentVerifierProxy implements IntentFilterVerifier<ActivityIntentInfo> {
        private Context mContext;
        private ArrayList<Integer> mCurrentIntentFilterVerifications = new ArrayList();
        private ComponentName mIntentFilterVerifierComponent;

        public IntentVerifierProxy(Context context, ComponentName verifierComponent) {
            this.mContext = context;
            this.mIntentFilterVerifierComponent = verifierComponent;
        }

        private String getDefaultScheme() {
            return "https";
        }

        public void startVerifications(int userId) {
            int count = this.mCurrentIntentFilterVerifications.size();
            for (int n = 0; n < count; n++) {
                int verificationId = ((Integer) this.mCurrentIntentFilterVerifications.get(n)).intValue();
                IntentFilterVerificationState ivs = (IntentFilterVerificationState) PackageManagerService.this.mIntentFilterVerificationStates.get(verificationId);
                String packageName = ivs.getPackageName();
                ArrayList<ActivityIntentInfo> filters = ivs.getFilters();
                int filterCount = filters.size();
                ArraySet<String> domainsSet = new ArraySet();
                for (int m = 0; m < filterCount; m++) {
                    domainsSet.addAll(((ActivityIntentInfo) filters.get(m)).getHostsList());
                }
                ArrayList<String> domainsList = new ArrayList(domainsSet);
                synchronized (PackageManagerService.this.mPackages) {
                    if (PackageManagerService.this.mSettings.createIntentFilterVerificationIfNeededLPw(packageName, domainsList) != null) {
                        PackageManagerService.this.scheduleWriteSettingsLocked();
                    }
                }
                sendVerificationRequest(userId, verificationId, ivs);
            }
            this.mCurrentIntentFilterVerifications.clear();
        }

        private void sendVerificationRequest(int userId, int verificationId, IntentFilterVerificationState ivs) {
            Intent verificationIntent = new Intent("android.intent.action.INTENT_FILTER_NEEDS_VERIFICATION");
            verificationIntent.putExtra("android.content.pm.extra.INTENT_FILTER_VERIFICATION_ID", verificationId);
            verificationIntent.putExtra("android.content.pm.extra.INTENT_FILTER_VERIFICATION_URI_SCHEME", getDefaultScheme());
            verificationIntent.putExtra("android.content.pm.extra.INTENT_FILTER_VERIFICATION_HOSTS", ivs.getHostsString());
            verificationIntent.putExtra("android.content.pm.extra.INTENT_FILTER_VERIFICATION_PACKAGE_NAME", ivs.getPackageName());
            verificationIntent.setComponent(this.mIntentFilterVerifierComponent);
            verificationIntent.addFlags(268435456);
            this.mContext.sendBroadcastAsUser(verificationIntent, new UserHandle(userId));
        }

        public void receiveVerificationResponse(int verificationId) {
            IntentFilterVerificationState ivs = (IntentFilterVerificationState) PackageManagerService.this.mIntentFilterVerificationStates.get(verificationId);
            boolean verified = ivs.isVerified();
            ArrayList<ActivityIntentInfo> filters = ivs.getFilters();
            int count = filters.size();
            for (int n = 0; n < count; n++) {
                ((ActivityIntentInfo) filters.get(n)).setVerified(verified);
            }
            PackageManagerService.this.mIntentFilterVerificationStates.remove(verificationId);
            String packageName = ivs.getPackageName();
            synchronized (PackageManagerService.this.mPackages) {
                IntentFilterVerificationInfo ivi = PackageManagerService.this.mSettings.getIntentFilterVerificationLPr(packageName);
            }
            if (ivi == null) {
                Slog.w(PackageManagerService.TAG, "IntentFilterVerificationInfo not found for verificationId:" + verificationId + " packageName:" + packageName);
                return;
            }
            synchronized (PackageManagerService.this.mPackages) {
                if (verified) {
                    ivi.setStatus(2);
                } else {
                    ivi.setStatus(1);
                }
                PackageManagerService.this.scheduleWriteSettingsLocked();
                int userId = ivs.getUserId();
                if (userId != -1) {
                    int updatedStatus = 0;
                    boolean needUpdate = false;
                    switch (PackageManagerService.this.mSettings.getIntentFilterVerificationStatusLPr(packageName, userId)) {
                        case 0:
                            if (verified) {
                                updatedStatus = 2;
                            } else {
                                updatedStatus = 1;
                            }
                            needUpdate = true;
                            break;
                        case 1:
                            if (verified) {
                                updatedStatus = 2;
                                needUpdate = true;
                                break;
                            }
                            break;
                    }
                    if (needUpdate) {
                        PackageManagerService.this.mSettings.updateIntentFilterVerificationStatusLPw(packageName, updatedStatus, userId);
                        PackageManagerService.this.scheduleWritePackageRestrictionsLocked(userId);
                    }
                }
            }
        }

        public boolean addOneIntentFilterVerification(int verifierUid, int userId, int verificationId, ActivityIntentInfo filter, String packageName) {
            if (!PackageManagerService.hasValidDomains(filter)) {
                return false;
            }
            IntentFilterVerificationState ivs = (IntentFilterVerificationState) PackageManagerService.this.mIntentFilterVerificationStates.get(verificationId);
            if (ivs == null) {
                ivs = createDomainVerificationState(verifierUid, userId, verificationId, packageName);
            }
            ivs.addFilter(filter);
            return true;
        }

        private IntentFilterVerificationState createDomainVerificationState(int verifierUid, int userId, int verificationId, String packageName) {
            IntentFilterVerificationState ivs = new IntentFilterVerificationState(verifierUid, userId, packageName);
            ivs.setPendingState();
            synchronized (PackageManagerService.this.mPackages) {
                PackageManagerService.this.mIntentFilterVerificationStates.append(verificationId, ivs);
                this.mCurrentIntentFilterVerifications.add(Integer.valueOf(verificationId));
            }
            return ivs;
        }
    }

    class MeasureParams extends HandlerParams implements Runnable {
        private final IPackageStatsObserver mObserver;
        private final PackageStats mStats;
        private boolean mSuccess;

        public MeasureParams(PackageStats stats, IPackageStatsObserver observer) {
            super(new UserHandle(stats.userHandle));
            this.mObserver = observer;
            this.mStats = stats;
        }

        public String toString() {
            return "MeasureParams{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.mStats.packageName + "}";
        }

        void handleStartCopy() throws RemoteException {
            PackageManagerService.this.mExecutorService.submit(this);
        }

        public void run() {
            try {
                measureTask();
            } catch (RemoteException e) {
                Log.e(PackageManagerService.TAG, "Posting measureTask RemoteException");
            } catch (Exception e2) {
                Log.e(PackageManagerService.TAG, "Posting measureTask Exception");
            }
            handleReturnCode();
        }

        void measureTask() throws RemoteException {
            synchronized (PackageManagerService.this.mInstallLock) {
                Flog.i(206, "measureTask getPackageSizeInfoLI : " + this.mStats + ", for user " + this.mStats.userHandle);
                this.mSuccess = PackageManagerService.this.getPackageSizeInfoLI(this.mStats.packageName, this.mStats.userHandle, this.mStats);
            }
            if (this.mSuccess) {
                boolean mounted = false;
                try {
                    String status = Environment.getExternalStorageState();
                    if ("mounted".equals(status)) {
                        mounted = true;
                    } else {
                        mounted = "mounted_ro".equals(status);
                    }
                } catch (Exception e) {
                }
                if (mounted) {
                    UserEnvironment userEnv = new UserEnvironment(this.mStats.userHandle);
                    this.mStats.externalCacheSize = PackageManagerService.calculateDirectorySize(PackageManagerService.this.mContainerService, userEnv.buildExternalStorageAppCacheDirs(this.mStats.packageName));
                    this.mStats.externalDataSize = PackageManagerService.calculateDirectorySize(PackageManagerService.this.mContainerService, userEnv.buildExternalStorageAppDataDirs(this.mStats.packageName));
                    PackageStats packageStats = this.mStats;
                    packageStats.externalDataSize -= this.mStats.externalCacheSize;
                    this.mStats.externalMediaSize = PackageManagerService.calculateDirectorySize(PackageManagerService.this.mContainerService, userEnv.buildExternalStorageAppMediaDirs(this.mStats.packageName));
                    this.mStats.externalObbSize = PackageManagerService.calculateDirectorySize(PackageManagerService.this.mContainerService, userEnv.buildExternalStorageAppObbDirs(this.mStats.packageName));
                }
            }
        }

        void handleReturnCode() {
            if (this.mObserver != null) {
                try {
                    this.mObserver.onGetStatsCompleted(this.mStats, this.mSuccess);
                } catch (RemoteException e) {
                    Slog.i(PackageManagerService.TAG, "Observer no longer exists.");
                }
            }
        }

        void handleServiceError() {
            Slog.e(PackageManagerService.TAG, "Could not measure application " + this.mStats.packageName + " external storage");
        }
    }

    private static class MoveCallbacks extends Handler {
        private static final int MSG_CREATED = 1;
        private static final int MSG_STATUS_CHANGED = 2;
        private final RemoteCallbackList<IPackageMoveObserver> mCallbacks = new RemoteCallbackList();
        private final SparseIntArray mLastStatus = new SparseIntArray();

        public MoveCallbacks(Looper looper) {
            super(looper);
        }

        public void register(IPackageMoveObserver callback) {
            this.mCallbacks.register(callback);
        }

        public void unregister(IPackageMoveObserver callback) {
            this.mCallbacks.unregister(callback);
        }

        public void handleMessage(Message msg) {
            SomeArgs args = msg.obj;
            int n = this.mCallbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    invokeCallback((IPackageMoveObserver) this.mCallbacks.getBroadcastItem(i), msg.what, args);
                } catch (RemoteException e) {
                }
            }
            this.mCallbacks.finishBroadcast();
            args.recycle();
        }

        private void invokeCallback(IPackageMoveObserver callback, int what, SomeArgs args) throws RemoteException {
            switch (what) {
                case 1:
                    callback.onCreated(args.argi1, (Bundle) args.arg2);
                    return;
                case 2:
                    callback.onStatusChanged(args.argi1, args.argi2, ((Long) args.arg3).longValue());
                    return;
                default:
                    return;
            }
        }

        private void notifyCreated(int moveId, Bundle extras) {
            Slog.v(PackageManagerService.TAG, "Move " + moveId + " created " + extras.toString());
            SomeArgs args = SomeArgs.obtain();
            args.argi1 = moveId;
            args.arg2 = extras;
            obtainMessage(1, args).sendToTarget();
        }

        private void notifyStatusChanged(int moveId, int status) {
            notifyStatusChanged(moveId, status, -1);
        }

        private void notifyStatusChanged(int moveId, int status, long estMillis) {
            Slog.v(PackageManagerService.TAG, "Move " + moveId + " status " + status);
            SomeArgs args = SomeArgs.obtain();
            args.argi1 = moveId;
            args.argi2 = status;
            args.arg3 = Long.valueOf(estMillis);
            obtainMessage(2, args).sendToTarget();
            synchronized (this.mLastStatus) {
                this.mLastStatus.put(moveId, status);
            }
        }
    }

    static class MoveInfo {
        final int appId;
        final String dataAppName;
        final String fromUuid;
        final int moveId;
        final String packageName;
        final String seinfo;
        final int targetSdkVersion;
        final String toUuid;

        public MoveInfo(int moveId, String fromUuid, String toUuid, String packageName, String dataAppName, int appId, String seinfo, int targetSdkVersion) {
            this.moveId = moveId;
            this.fromUuid = fromUuid;
            this.toUuid = toUuid;
            this.packageName = packageName;
            this.dataAppName = dataAppName;
            this.appId = appId;
            this.seinfo = seinfo;
            this.targetSdkVersion = targetSdkVersion;
        }
    }

    class MoveInstallArgs extends InstallArgs {
        private File codeFile;
        private File resourceFile;

        MoveInstallArgs(InstallParams params) {
            super(params.origin, params.move, params.observer, params.installFlags, params.installerPackageName, params.volumeUuid, params.getUser(), null, params.packageAbiOverride, params.grantedRuntimePermissions, params.traceMethod, params.traceCookie, params.certificates);
        }

        int copyApk(IMediaContainerService imcs, boolean temp) {
            synchronized (PackageManagerService.this.mInstaller) {
                try {
                    PackageManagerService.this.mInstaller.moveCompleteApp(this.move.fromUuid, this.move.toUuid, this.move.packageName, this.move.dataAppName, this.move.appId, this.move.seinfo, this.move.targetSdkVersion);
                } catch (InstallerException e) {
                    Slog.w(PackageManagerService.TAG, "Failed to move app", e);
                    return -110;
                }
            }
            this.codeFile = new File(Environment.getDataAppDirectory(this.move.toUuid), this.move.dataAppName);
            this.resourceFile = this.codeFile;
            return 1;
        }

        int doPreInstall(int status) {
            if (status != 1) {
                cleanUp(this.move.toUuid);
            }
            return status;
        }

        boolean doRename(int status, Package pkg, String oldCodePath) {
            if (status != 1) {
                cleanUp(this.move.toUuid);
                return false;
            }
            pkg.setApplicationVolumeUuid(pkg.volumeUuid);
            pkg.setApplicationInfoCodePath(pkg.codePath);
            pkg.setApplicationInfoBaseCodePath(pkg.baseCodePath);
            pkg.setApplicationInfoSplitCodePaths(pkg.splitCodePaths);
            pkg.setApplicationInfoResourcePath(pkg.codePath);
            pkg.setApplicationInfoBaseResourcePath(pkg.baseCodePath);
            pkg.setApplicationInfoSplitResourcePaths(pkg.splitCodePaths);
            return true;
        }

        int doPostInstall(int status, int uid) {
            if (status == 1) {
                cleanUp(this.move.fromUuid);
            } else {
                PackageManagerService.this.hwCertCleanUp();
                cleanUp(this.move.toUuid);
            }
            return status;
        }

        String getCodePath() {
            return this.codeFile != null ? this.codeFile.getAbsolutePath() : null;
        }

        String getResourcePath() {
            return this.resourceFile != null ? this.resourceFile.getAbsolutePath() : null;
        }

        private boolean cleanUp(String volumeUuid) {
            File codeFile = new File(Environment.getDataAppDirectory(volumeUuid), this.move.dataAppName);
            Slog.d(PackageManagerService.TAG, "Cleaning up " + this.move.packageName + " on " + volumeUuid);
            synchronized (PackageManagerService.this.mInstallLock) {
                for (int userId : PackageManagerService.sUserManager.getUserIds()) {
                    try {
                        PackageManagerService.this.mInstaller.destroyAppData(volumeUuid, this.move.packageName, userId, 3, 0);
                    } catch (InstallerException e) {
                        Slog.w(PackageManagerService.TAG, String.valueOf(e));
                    }
                }
                PackageManagerService.this.removeCodePathLI(codeFile);
            }
            return true;
        }

        void cleanUpResourcesLI() {
            throw new UnsupportedOperationException();
        }

        boolean doPostDeleteLI(boolean delete) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class OnPermissionChangeListeners extends Handler {
        private static final int MSG_ON_PERMISSIONS_CHANGED = 1;
        private final RemoteCallbackList<IOnPermissionsChangeListener> mPermissionListeners = new RemoteCallbackList();

        public OnPermissionChangeListeners(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    handleOnPermissionsChanged(msg.arg1);
                    return;
                default:
                    return;
            }
        }

        public void addListenerLocked(IOnPermissionsChangeListener listener) {
            this.mPermissionListeners.register(listener);
        }

        public void removeListenerLocked(IOnPermissionsChangeListener listener) {
            this.mPermissionListeners.unregister(listener);
        }

        public void onPermissionsChanged(int uid) {
            if (this.mPermissionListeners.getRegisteredCallbackCount() > 0) {
                obtainMessage(1, uid, 0).sendToTarget();
            }
        }

        private void handleOnPermissionsChanged(int uid) {
            int count = this.mPermissionListeners.beginBroadcast();
            for (int i = 0; i < count; i++) {
                try {
                    ((IOnPermissionsChangeListener) this.mPermissionListeners.getBroadcastItem(i)).onPermissionsChanged(uid);
                } catch (RemoteException e) {
                    Log.e(PackageManagerService.TAG, "Permission listener is dead", e);
                } catch (Throwable th) {
                    this.mPermissionListeners.finishBroadcast();
                }
            }
            this.mPermissionListeners.finishBroadcast();
        }
    }

    static class OriginInfo {
        final String cid;
        final boolean existing;
        final File file;
        final File resolvedFile;
        final String resolvedPath;
        final boolean staged;

        static OriginInfo fromNothing() {
            return new OriginInfo(null, null, false, false);
        }

        static OriginInfo fromUntrustedFile(File file) {
            return new OriginInfo(file, null, false, false);
        }

        static OriginInfo fromExistingFile(File file) {
            return new OriginInfo(file, null, false, true);
        }

        static OriginInfo fromStagedFile(File file) {
            return new OriginInfo(file, null, true, false);
        }

        static OriginInfo fromStagedContainer(String cid) {
            return new OriginInfo(null, cid, true, false);
        }

        private OriginInfo(File file, String cid, boolean staged, boolean existing) {
            this.file = file;
            this.cid = cid;
            this.staged = staged;
            this.existing = existing;
            if (cid != null) {
                this.resolvedPath = PackageHelper.getSdDir(cid);
                this.resolvedFile = new File(this.resolvedPath);
            } else if (file != null) {
                this.resolvedPath = file.getAbsolutePath();
                this.resolvedFile = file;
            } else {
                this.resolvedPath = null;
                this.resolvedFile = null;
            }
        }
    }

    private class PackageFreezer implements AutoCloseable {
        private final PackageFreezer[] mChildren;
        private final CloseGuard mCloseGuard;
        private final AtomicBoolean mClosed;
        private final String mPackageName;
        private final boolean mWeFroze;

        public PackageFreezer() {
            this.mClosed = new AtomicBoolean();
            this.mCloseGuard = CloseGuard.get();
            this.mPackageName = null;
            this.mChildren = null;
            this.mWeFroze = false;
            this.mCloseGuard.open("close");
        }

        public PackageFreezer(String packageName, int userId, String killReason) {
            this.mClosed = new AtomicBoolean();
            this.mCloseGuard = CloseGuard.get();
            synchronized (PackageManagerService.this.mPackages) {
                this.mPackageName = packageName;
                this.mWeFroze = PackageManagerService.this.mFrozenPackages.add(this.mPackageName);
                PackageSetting ps = (PackageSetting) PackageManagerService.this.mSettings.mPackages.get(this.mPackageName);
                if (ps != null) {
                    PackageManagerService.this.killApplication(ps.name, ps.appId, userId, killReason);
                }
                Package p = (Package) PackageManagerService.this.mPackages.get(packageName);
                if (p == null || p.childPackages == null) {
                    this.mChildren = null;
                } else {
                    int N = p.childPackages.size();
                    this.mChildren = new PackageFreezer[N];
                    for (int i = 0; i < N; i++) {
                        this.mChildren[i] = new PackageFreezer(((Package) p.childPackages.get(i)).packageName, userId, killReason);
                    }
                }
            }
            this.mCloseGuard.open("close");
        }

        protected void finalize() throws Throwable {
            try {
                this.mCloseGuard.warnIfOpen();
                close();
            } finally {
                super.finalize();
            }
        }

        public void close() {
            int i = 0;
            this.mCloseGuard.close();
            if (this.mClosed.compareAndSet(false, true)) {
                synchronized (PackageManagerService.this.mPackages) {
                    if (this.mWeFroze) {
                        PackageManagerService.this.mFrozenPackages.remove(this.mPackageName);
                    }
                    if (this.mChildren != null) {
                        PackageFreezer[] packageFreezerArr = this.mChildren;
                        int length = packageFreezerArr.length;
                        while (i < length) {
                            packageFreezerArr[i].close();
                            i++;
                        }
                    }
                }
            }
        }
    }

    class PackageHandler extends Handler {
        private boolean mBound = false;
        final ArrayList<HandlerParams> mPendingInstalls = new ArrayList();

        private boolean connectToService() {
            Intent service = new Intent().setComponent(PackageManagerService.DEFAULT_CONTAINER_COMPONENT);
            Process.setThreadPriority(0);
            if (PackageManagerService.this.mContext.bindServiceAsUser(service, PackageManagerService.this.mDefContainerConn, 1, UserHandle.SYSTEM)) {
                Process.setThreadPriority(10);
                this.mBound = true;
                return true;
            }
            Process.setThreadPriority(10);
            return false;
        }

        private void disconnectService() {
            PackageManagerService.this.mContainerService = null;
            this.mBound = false;
            Process.setThreadPriority(0);
            PackageManagerService.this.mContext.unbindService(PackageManagerService.this.mDefContainerConn);
            Process.setThreadPriority(10);
        }

        PackageHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            try {
                doHandleMessage(msg);
            } finally {
                Process.setThreadPriority(10);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        void doHandleMessage(Message msg) {
            int i;
            int uid;
            HandlerParams params;
            Iterator params$iterator;
            int userId;
            InstallArgs args;
            int verificationId;
            PackageVerificationState state;
            Uri originUri;
            int ret;
            switch (msg.what) {
                case 1:
                    Process.setThreadPriority(0);
                    synchronized (PackageManagerService.this.mPackages) {
                        if (PackageManagerService.this.mPendingBroadcasts != null) {
                            int size = PackageManagerService.this.mPendingBroadcasts.size();
                            if (size > 0) {
                                String[] packages = new String[size];
                                ArrayList<String>[] components = new ArrayList[size];
                                int[] uids = new int[size];
                                i = 0;
                                for (int n = 0; n < PackageManagerService.this.mPendingBroadcasts.userIdCount(); n++) {
                                    int packageUserId = PackageManagerService.this.mPendingBroadcasts.userIdAt(n);
                                    Iterator<Entry<String, ArrayList<String>>> it = PackageManagerService.this.mPendingBroadcasts.packagesForUserId(packageUserId).entrySet().iterator();
                                    while (it.hasNext() && i < size) {
                                        Entry<String, ArrayList<String>> ent = (Entry) it.next();
                                        packages[i] = (String) ent.getKey();
                                        components[i] = (ArrayList) ent.getValue();
                                        PackageSetting ps = (PackageSetting) PackageManagerService.this.mSettings.mPackages.get(ent.getKey());
                                        if (ps != null) {
                                            uid = UserHandle.getUid(packageUserId, ps.appId);
                                        } else {
                                            uid = -1;
                                        }
                                        uids[i] = uid;
                                        i++;
                                    }
                                }
                                size = i;
                                PackageManagerService.this.mPendingBroadcasts.clear();
                                break;
                            }
                            return;
                        }
                        return;
                    }
                    break;
                case 3:
                    if (msg.obj != null) {
                        PackageManagerService.this.mContainerService = (IMediaContainerService) msg.obj;
                        Trace.asyncTraceEnd(262144, "bindingMCS", System.identityHashCode(PackageManagerService.this.mHandler));
                    }
                    if (PackageManagerService.this.mContainerService != null) {
                        if (this.mPendingInstalls.size() <= 0) {
                            Slog.w(PackageManagerService.TAG, "Empty queue");
                            break;
                        }
                        params = (HandlerParams) this.mPendingInstalls.get(0);
                        if (params != null) {
                            Trace.asyncTraceEnd(262144, "queueInstall", System.identityHashCode(params));
                            Trace.traceBegin(262144, "startCopy");
                            if (params.startCopy()) {
                                if (this.mPendingInstalls.size() > 0) {
                                    this.mPendingInstalls.remove(0);
                                }
                                if (this.mPendingInstalls.size() != 0) {
                                    PackageManagerService.this.mHandler.sendEmptyMessage(3);
                                } else if (this.mBound) {
                                    removeMessages(6);
                                    sendMessageDelayed(obtainMessage(6), 10000);
                                }
                            }
                            Trace.traceEnd(262144);
                            break;
                        }
                    } else if (!this.mBound) {
                        Slog.e(PackageManagerService.TAG, "Cannot bind to media container service");
                        params$iterator = this.mPendingInstalls.iterator();
                        if (!params$iterator.hasNext()) {
                            this.mPendingInstalls.clear();
                            break;
                        }
                        params = (HandlerParams) params$iterator.next();
                        params.serviceError();
                        Trace.asyncTraceEnd(262144, "queueInstall", System.identityHashCode(params));
                        if (params.traceMethod != null) {
                            Trace.asyncTraceEnd(262144, params.traceMethod, params.traceCookie);
                        }
                        return;
                    } else {
                        Slog.w(PackageManagerService.TAG, "Waiting to connect to media container service");
                        break;
                    }
                    break;
                case 5:
                    params = msg.obj;
                    int idx = this.mPendingInstalls.size();
                    if (!this.mBound) {
                        Trace.asyncTraceBegin(262144, "bindingMCS", System.identityHashCode(PackageManagerService.this.mHandler));
                        if (connectToService()) {
                            this.mPendingInstalls.add(idx, params);
                            break;
                        }
                        Slog.e(PackageManagerService.TAG, "Failed to bind to media container service");
                        params.serviceError();
                        Trace.asyncTraceEnd(262144, "bindingMCS", System.identityHashCode(PackageManagerService.this.mHandler));
                        if (params.traceMethod != null) {
                            Trace.asyncTraceEnd(262144, params.traceMethod, params.traceCookie);
                        }
                        return;
                    }
                    this.mPendingInstalls.add(idx, params);
                    if (idx == 0) {
                        PackageManagerService.this.mHandler.sendEmptyMessage(3);
                        break;
                    }
                    break;
                case 6:
                    if (this.mPendingInstalls.size() != 0 || PackageManagerService.this.mPendingVerification.size() != 0) {
                        if (this.mPendingInstalls.size() > 0) {
                            PackageManagerService.this.mHandler.sendEmptyMessage(3);
                            break;
                        }
                    } else if (this.mBound) {
                        disconnectService();
                        break;
                    }
                    break;
                case 7:
                    Process.setThreadPriority(0);
                    String packageName = msg.obj;
                    userId = msg.arg1;
                    boolean andCode = msg.arg2 != 0;
                    synchronized (PackageManagerService.this.mPackages) {
                        if (userId == -1) {
                            for (int user : PackageManagerService.sUserManager.getUserIds()) {
                                PackageManagerService.this.mSettings.addPackageToCleanLPw(new PackageCleanItem(user, packageName, andCode));
                            }
                        } else {
                            PackageManagerService.this.mSettings.addPackageToCleanLPw(new PackageCleanItem(userId, packageName, andCode));
                        }
                    }
                    Process.setThreadPriority(10);
                    PackageManagerService.this.startCleaningPackages();
                    break;
                case 9:
                    PostInstallData data = (PostInstallData) PackageManagerService.this.mRunningInstalls.get(msg.arg1);
                    boolean didRestore = msg.arg2 != 0;
                    PackageManagerService.this.mRunningInstalls.delete(msg.arg1);
                    if (data != null) {
                        args = data.args;
                        PackageInstalledInfo parentRes = data.res;
                        boolean grantPermissions = (args.installFlags & 256) != 0;
                        if (parentRes.pkg != null) {
                            PackageManagerService.this.addGrantedInstalledPkg(parentRes.pkg.packageName, grantPermissions);
                        }
                        boolean killApp = (args.installFlags & 4096) == 0;
                        String[] grantedPermissions = args.installGrantPermissions;
                        PackageManagerService.this.handlePackagePostInstall(parentRes, grantPermissions, killApp, grantedPermissions, didRestore, args.installerPackageName, args.observer);
                        int childCount = parentRes.addedChildPackages != null ? parentRes.addedChildPackages.size() : 0;
                        for (i = 0; i < childCount; i++) {
                            PackageManagerService.this.handlePackagePostInstall((PackageInstalledInfo) parentRes.addedChildPackages.valueAt(i), grantPermissions, killApp, grantedPermissions, false, args.installerPackageName, args.observer);
                        }
                        if (!(parentRes.pkg == null || (PackageManagerService.isSystemApp(parentRes.pkg) && (parentRes.pkg.applicationInfo.hwFlags & 33554432) == 0 && (parentRes.pkg.applicationInfo.hwFlags & 67108864) == 0))) {
                            PackageManagerService.this.parseInstalledPkgInfo(args, parentRes);
                        }
                        SmartShrinker.reclaim(Process.myPid(), 3);
                        if (args.traceMethod != null) {
                            Trace.asyncTraceEnd(262144, args.traceMethod, args.traceCookie);
                        }
                    } else {
                        Slog.e(PackageManagerService.TAG, "Bogus post-install token " + msg.arg1);
                    }
                    Trace.asyncTraceEnd(262144, "postInstall", msg.arg1);
                    break;
                case 10:
                    if (this.mPendingInstalls.size() > 0) {
                        if (this.mBound) {
                            disconnectService();
                        }
                        if (!connectToService()) {
                            Slog.e(PackageManagerService.TAG, "Failed to bind to media container service");
                            for (HandlerParams params2 : this.mPendingInstalls) {
                                params2.serviceError();
                                Trace.asyncTraceEnd(262144, "queueInstall", System.identityHashCode(params2));
                            }
                            this.mPendingInstalls.clear();
                            break;
                        }
                    }
                    break;
                case 11:
                    Trace.asyncTraceEnd(262144, "queueInstall", System.identityHashCode((HandlerParams) this.mPendingInstalls.remove(0)));
                    break;
                case 12:
                    boolean reportStatus = msg.arg1 == 1;
                    if (msg.arg2 == 1) {
                        Runtime.getRuntime().gc();
                    }
                    if (msg.obj != null) {
                        PackageManagerService.this.unloadAllContainers(msg.obj);
                    }
                    if (reportStatus) {
                        try {
                            PackageHelper.getMountService().finishMediaUpdate();
                            break;
                        } catch (RemoteException e) {
                            Log.e(PackageManagerService.TAG, "MountService not running?");
                            break;
                        }
                    }
                    break;
                case 13:
                    Process.setThreadPriority(0);
                    synchronized (PackageManagerService.this.mPackages) {
                        removeMessages(13);
                        removeMessages(14);
                        PackageManagerService.this.mSettings.writeLPr();
                        PackageManagerService.this.mDirtyUsers.clear();
                    }
                    Process.setThreadPriority(10);
                    break;
                case 14:
                    Process.setThreadPriority(0);
                    synchronized (PackageManagerService.this.mPackages) {
                        removeMessages(14);
                        for (Integer intValue : PackageManagerService.this.mDirtyUsers) {
                            PackageManagerService.this.mSettings.writePackageRestrictionsLPr(intValue.intValue());
                        }
                        PackageManagerService.this.mDirtyUsers.clear();
                    }
                    Process.setThreadPriority(10);
                    break;
                case 15:
                    verificationId = msg.arg1;
                    state = (PackageVerificationState) PackageManagerService.this.mPendingVerification.get(verificationId);
                    if (state != null) {
                        PackageVerificationResponse response = msg.obj;
                        state.setVerifierResponse(response.callerUid, response.code);
                        if (state.isVerificationComplete()) {
                            PackageManagerService.this.mPendingVerification.remove(verificationId);
                            args = state.getInstallArgs();
                            originUri = Uri.fromFile(args.origin.resolvedFile);
                            if (state.isInstallAllowed()) {
                                ret = -110;
                                PackageManagerService.this.broadcastPackageVerified(verificationId, originUri, response.code, state.getInstallArgs().getUser());
                                try {
                                    ret = args.copyApk(PackageManagerService.this.mContainerService, true);
                                } catch (RemoteException e2) {
                                    Slog.e(PackageManagerService.TAG, "Could not contact the ContainerService");
                                }
                            } else {
                                ret = -22;
                            }
                            Trace.asyncTraceEnd(262144, "verification", verificationId);
                            PackageManagerService.this.processPendingInstall(args, ret);
                            PackageManagerService.this.mHandler.sendEmptyMessage(6);
                            break;
                        }
                    }
                    Slog.w(PackageManagerService.TAG, "Invalid verification token " + verificationId + " received");
                    break;
                    break;
                case 16:
                    verificationId = msg.arg1;
                    state = (PackageVerificationState) PackageManagerService.this.mPendingVerification.get(verificationId);
                    if (!(state == null || state.timeoutExtended())) {
                        args = state.getInstallArgs();
                        originUri = Uri.fromFile(args.origin.resolvedFile);
                        Slog.i(PackageManagerService.TAG, "Verification timed out for " + originUri);
                        PackageManagerService.this.mPendingVerification.remove(verificationId);
                        ret = -22;
                        if (PackageManagerService.this.getDefaultVerificationResponse() == 1) {
                            Slog.i(PackageManagerService.TAG, "Continuing with installation of " + originUri);
                            state.setVerifierResponse(Binder.getCallingUid(), 2);
                            PackageManagerService.this.broadcastPackageVerified(verificationId, originUri, 1, state.getInstallArgs().getUser());
                            try {
                                ret = args.copyApk(PackageManagerService.this.mContainerService, true);
                            } catch (RemoteException e3) {
                                Slog.e(PackageManagerService.TAG, "Could not contact the ContainerService");
                            }
                        } else {
                            PackageManagerService.this.broadcastPackageVerified(verificationId, originUri, -1, state.getInstallArgs().getUser());
                        }
                        Trace.asyncTraceEnd(262144, "verification", verificationId);
                        PackageManagerService.this.processPendingInstall(args, ret);
                        PackageManagerService.this.mHandler.sendEmptyMessage(6);
                        break;
                    }
                case 17:
                    IFVerificationParams params3 = msg.obj;
                    PackageManagerService.this.verifyIntentFiltersIfNeeded(params3.userId, params3.verifierUid, params3.replacing, params3.pkg);
                    break;
                case 18:
                    verificationId = msg.arg1;
                    IntentFilterVerificationState state2 = (IntentFilterVerificationState) PackageManagerService.this.mIntentFilterVerificationStates.get(verificationId);
                    if (state2 != null) {
                        userId = state2.getUserId();
                        IntentFilterVerificationResponse response2 = msg.obj;
                        state2.setVerifierResponse(response2.callerUid, response2.code);
                        if (response2.code == -1) {
                        }
                        if (state2.isVerificationComplete()) {
                            PackageManagerService.this.mIntentFilterVerifier.receiveVerificationResponse(verificationId);
                            break;
                        }
                    }
                    Slog.w(PackageManagerService.TAG, "Invalid IntentFilter verification token " + verificationId + " received");
                    break;
                    break;
                case 19:
                    Process.setThreadPriority(0);
                    synchronized (PackageManagerService.this.mPackages) {
                        removeMessages(19);
                        PackageManagerService.this.mSettings.writePackageListLPr(msg.arg1);
                    }
                    Process.setThreadPriority(10);
                    break;
            }
        }
    }

    static class PackageInstalledInfo {
        ArrayMap<String, PackageInstalledInfo> addedChildPackages;
        String name;
        int[] newUsers;
        String origPackage;
        String origPermission;
        int[] origUsers;
        Package pkg;
        PackageRemovedInfo removedInfo;
        int returnCode;
        String returnMsg;
        int uid;

        PackageInstalledInfo() {
        }

        public void setError(int code, String msg) {
            setReturnCode(code);
            setReturnMessage(msg);
            Slog.w(PackageManagerService.TAG, msg);
        }

        public void setError(String msg, PackageParserException e) {
            setReturnCode(e.error);
            setReturnMessage(ExceptionUtils.getCompleteMessage(msg, e));
            Slog.w(PackageManagerService.TAG, msg, e);
        }

        public void setError(String msg, PackageManagerException e) {
            this.returnCode = e.error;
            setReturnMessage(ExceptionUtils.getCompleteMessage(msg, e));
            Slog.w(PackageManagerService.TAG, msg, e);
        }

        public void setReturnCode(int returnCode) {
            this.returnCode = returnCode;
            int childCount = this.addedChildPackages != null ? this.addedChildPackages.size() : 0;
            for (int i = 0; i < childCount; i++) {
                ((PackageInstalledInfo) this.addedChildPackages.valueAt(i)).returnCode = returnCode;
            }
        }

        private void setReturnMessage(String returnMsg) {
            this.returnMsg = returnMsg;
            int childCount = this.addedChildPackages != null ? this.addedChildPackages.size() : 0;
            for (int i = 0; i < childCount; i++) {
                ((PackageInstalledInfo) this.addedChildPackages.valueAt(i)).returnMsg = returnMsg;
            }
        }
    }

    private class PackageManagerInternalImpl extends PackageManagerInternal {
        private PackageManagerInternalImpl() {
        }

        public void setLocationPackagesProvider(PackagesProvider provider) {
            synchronized (PackageManagerService.this.mPackages) {
                PackageManagerService.this.mDefaultPermissionPolicy.setLocationPackagesProviderLPw(provider);
            }
        }

        public void setVoiceInteractionPackagesProvider(PackagesProvider provider) {
            synchronized (PackageManagerService.this.mPackages) {
                PackageManagerService.this.mDefaultPermissionPolicy.setVoiceInteractionPackagesProviderLPw(provider);
            }
        }

        public void setSmsAppPackagesProvider(PackagesProvider provider) {
            synchronized (PackageManagerService.this.mPackages) {
                PackageManagerService.this.mDefaultPermissionPolicy.setSmsAppPackagesProviderLPw(provider);
            }
        }

        public void setDialerAppPackagesProvider(PackagesProvider provider) {
            synchronized (PackageManagerService.this.mPackages) {
                PackageManagerService.this.mDefaultPermissionPolicy.setDialerAppPackagesProviderLPw(provider);
            }
        }

        public void setSimCallManagerPackagesProvider(PackagesProvider provider) {
            synchronized (PackageManagerService.this.mPackages) {
                PackageManagerService.this.mDefaultPermissionPolicy.setSimCallManagerPackagesProviderLPw(provider);
            }
        }

        public void setSyncAdapterPackagesprovider(SyncAdapterPackagesProvider provider) {
            synchronized (PackageManagerService.this.mPackages) {
                PackageManagerService.this.mDefaultPermissionPolicy.setSyncAdapterPackagesProviderLPw(provider);
            }
        }

        public void grantDefaultPermissionsToDefaultSmsApp(String packageName, int userId) {
            synchronized (PackageManagerService.this.mPackages) {
                PackageManagerService.this.mDefaultPermissionPolicy.grantDefaultPermissionsToDefaultSmsAppLPr(packageName, userId);
            }
        }

        public void grantDefaultPermissionsToDefaultDialerApp(String packageName, int userId) {
            synchronized (PackageManagerService.this.mPackages) {
                PackageManagerService.this.mSettings.setDefaultDialerPackageNameLPw(packageName, userId);
                PackageManagerService.this.mDefaultPermissionPolicy.grantDefaultPermissionsToDefaultDialerAppLPr(packageName, userId);
            }
        }

        public void grantDefaultPermissionsToDefaultSimCallManager(String packageName, int userId) {
            synchronized (PackageManagerService.this.mPackages) {
                PackageManagerService.this.mDefaultPermissionPolicy.grantDefaultPermissionsToDefaultSimCallManagerLPr(packageName, userId);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void setKeepUninstalledPackages(List<String> packageList) {
            Throwable th;
            Preconditions.checkNotNull(packageList);
            List list = null;
            synchronized (PackageManagerService.this.mPackages) {
                int i;
                if (PackageManagerService.this.mKeepUninstalledPackages != null) {
                    int packagesCount = PackageManagerService.this.mKeepUninstalledPackages.size();
                    i = 0;
                    List<String> removedFromList = null;
                    while (i < packagesCount) {
                        List<String> removedFromList2;
                        try {
                            String oldPackage = (String) PackageManagerService.this.mKeepUninstalledPackages.get(i);
                            if (packageList == null || !packageList.contains(oldPackage)) {
                                if (removedFromList == null) {
                                    removedFromList2 = new ArrayList();
                                } else {
                                    removedFromList2 = removedFromList;
                                }
                                try {
                                    removedFromList2.add(oldPackage);
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            } else {
                                removedFromList2 = removedFromList;
                            }
                            i++;
                            removedFromList = removedFromList2;
                        } catch (Throwable th3) {
                            th = th3;
                            removedFromList2 = removedFromList;
                        }
                    }
                    list = removedFromList;
                }
                PackageManagerService.this.mKeepUninstalledPackages = new ArrayList(packageList);
                if (list != null) {
                    int removedCount = list.size();
                    for (i = 0; i < removedCount; i++) {
                        PackageManagerService.this.deletePackageIfUnusedLPr((String) list.get(i));
                    }
                }
            }
            throw th;
        }

        public boolean isPermissionsReviewRequired(String packageName, int userId) {
            synchronized (PackageManagerService.this.mPackages) {
                if (Build.PERMISSIONS_REVIEW_REQUIRED) {
                    PackageSetting packageSetting = (PackageSetting) PackageManagerService.this.mSettings.mPackages.get(packageName);
                    if (packageSetting == null) {
                        return false;
                    } else if (packageSetting.pkg.applicationInfo.targetSdkVersion >= 23) {
                        return false;
                    } else {
                        boolean isPermissionReviewRequired = packageSetting.getPermissionsState().isPermissionReviewRequired(userId);
                        return isPermissionReviewRequired;
                    }
                }
                return false;
            }
        }

        public ApplicationInfo getApplicationInfo(String packageName, int userId) {
            return PackageManagerService.this.getApplicationInfo(packageName, 0, userId);
        }

        public ComponentName getHomeActivitiesAsUser(List<ResolveInfo> allHomeCandidates, int userId) {
            return PackageManagerService.this.getHomeActivitiesAsUser(allHomeCandidates, userId);
        }

        public void setDeviceAndProfileOwnerPackages(int deviceOwnerUserId, String deviceOwnerPackage, SparseArray<String> profileOwnerPackages) {
            PackageManagerService.this.mProtectedPackages.setDeviceAndProfileOwnerPackages(deviceOwnerUserId, deviceOwnerPackage, profileOwnerPackages);
        }

        public boolean canPackageBeWiped(int userId, String packageName) {
            return PackageManagerService.this.mProtectedPackages.canPackageBeWiped(userId, packageName);
        }

        public boolean isInMWPortraitWhiteList(String packageName) {
            return PackageManagerService.this.isInMWPortraitWhiteList(packageName);
        }
    }

    class PackageRemovedInfo {
        ArrayMap<String, PackageInstalledInfo> appearedChildPackages;
        InstallArgs args = null;
        boolean dataRemoved;
        boolean isRemovedPackageSystemUpdate = false;
        boolean isUpdate;
        int[] origUsers;
        int removedAppId = -1;
        ArrayMap<String, PackageRemovedInfo> removedChildPackages;
        boolean removedForAllUsers;
        String removedPackage;
        int[] removedUsers = null;
        int uid = -1;

        PackageRemovedInfo() {
        }

        void sendPackageRemovedBroadcasts(boolean killApp) {
            sendPackageRemovedBroadcastInternal(killApp);
            int childCount = this.removedChildPackages != null ? this.removedChildPackages.size() : 0;
            for (int i = 0; i < childCount; i++) {
                ((PackageRemovedInfo) this.removedChildPackages.valueAt(i)).sendPackageRemovedBroadcastInternal(killApp);
            }
        }

        void sendSystemPackageUpdatedBroadcasts() {
            if (this.isRemovedPackageSystemUpdate) {
                sendSystemPackageUpdatedBroadcastsInternal();
                int childCount = this.removedChildPackages != null ? this.removedChildPackages.size() : 0;
                for (int i = 0; i < childCount; i++) {
                    PackageRemovedInfo childInfo = (PackageRemovedInfo) this.removedChildPackages.valueAt(i);
                    if (childInfo.isRemovedPackageSystemUpdate) {
                        childInfo.sendSystemPackageUpdatedBroadcastsInternal();
                    }
                }
            }
        }

        void sendSystemPackageAppearedBroadcasts() {
            int packageCount = this.appearedChildPackages != null ? this.appearedChildPackages.size() : 0;
            for (int i = 0; i < packageCount; i++) {
                PackageInstalledInfo installedInfo = (PackageInstalledInfo) this.appearedChildPackages.valueAt(i);
                for (int userId : installedInfo.newUsers) {
                    PackageManagerService.this.sendPackageAddedForUser(installedInfo.name, true, UserHandle.getAppId(installedInfo.uid), userId);
                }
            }
        }

        private void sendSystemPackageUpdatedBroadcastsInternal() {
            Bundle extras = new Bundle(2);
            extras.putInt("android.intent.extra.UID", this.removedAppId >= 0 ? this.removedAppId : this.uid);
            extras.putBoolean("android.intent.extra.REPLACING", true);
            PackageManagerService.this.sendPackageBroadcast("android.intent.action.PACKAGE_ADDED", this.removedPackage, extras, 0, null, null, null);
            PackageManagerService.this.sendPackageBroadcast("android.intent.action.PACKAGE_REPLACED", this.removedPackage, extras, 0, null, null, null);
            PackageManagerService.this.sendPackageBroadcast("android.intent.action.MY_PACKAGE_REPLACED", null, null, 0, this.removedPackage, null, null);
        }

        private void sendPackageRemovedBroadcastInternal(boolean killApp) {
            boolean z;
            boolean z2 = true;
            Bundle extras = new Bundle(2);
            extras.putInt("android.intent.extra.UID", this.removedAppId >= 0 ? this.removedAppId : this.uid);
            extras.putBoolean("android.intent.extra.DATA_REMOVED", this.dataRemoved);
            String str = "android.intent.extra.DONT_KILL_APP";
            if (killApp) {
                z = false;
            } else {
                z = true;
            }
            extras.putBoolean(str, z);
            if (this.isUpdate || this.isRemovedPackageSystemUpdate) {
                extras.putBoolean("android.intent.extra.REPLACING", true);
            }
            extras.putBoolean("android.intent.extra.REMOVED_FOR_ALL_USERS", this.removedForAllUsers);
            PackageManagerService packageManagerService = PackageManagerService.this;
            str = this.removedPackage;
            if (!this.isUpdate) {
                z2 = this.isRemovedPackageSystemUpdate;
            }
            packageManagerService.updateCloneAppList(str, z2, this.removedUsers);
            if (this.removedPackage != null) {
                PackageManagerService.this.sendPackageBroadcast("android.intent.action.PACKAGE_REMOVED", this.removedPackage, extras, 0, null, null, this.removedUsers);
                if (this.dataRemoved && !this.isRemovedPackageSystemUpdate) {
                    PackageManagerService.this.sendPackageBroadcast("android.intent.action.PACKAGE_FULLY_REMOVED", this.removedPackage, extras, 0, null, null, this.removedUsers);
                }
            }
            if (this.removedAppId >= 0) {
                PackageManagerService.this.sendPackageBroadcast("android.intent.action.UID_REMOVED", null, extras, 0, null, null, this.removedUsers);
            }
            PackageManagerService.this.deleteClonedProfileIfNeed(this.removedUsers);
        }
    }

    private class PackageUsage {
        private static final String USAGE_FILE_MAGIC = "PACKAGE_USAGE__VERSION_";
        private static final String USAGE_FILE_MAGIC_VERSION_1 = "PACKAGE_USAGE__VERSION_1";
        private static final int WRITE_INTERVAL = 1800000;
        private final AtomicBoolean mBackgroundWriteRunning;
        private final Object mFileLock;
        private boolean mIsHistoricalPackageUsageAvailable;
        private final AtomicLong mLastWritten;

        private PackageUsage() {
            this.mFileLock = new Object();
            this.mLastWritten = new AtomicLong(0);
            this.mBackgroundWriteRunning = new AtomicBoolean(false);
            this.mIsHistoricalPackageUsageAvailable = true;
        }

        boolean isHistoricalPackageUsageAvailable() {
            return this.mIsHistoricalPackageUsageAvailable;
        }

        void write(boolean force) {
            if (force) {
                writeInternal();
                return;
            }
            if (SystemClock.elapsedRealtime() - this.mLastWritten.get() >= HwBroadcastRadarUtil.SYSTEM_BOOT_COMPLETED_TIME && this.mBackgroundWriteRunning.compareAndSet(false, true)) {
                new Thread("PackageUsage_DiskWriter") {
                    public void run() {
                        try {
                            PackageUsage.this.writeInternal();
                        } finally {
                            PackageUsage.this.mBackgroundWriteRunning.set(false);
                        }
                    }
                }.start();
            }
        }

        private void writeInternal() {
            synchronized (PackageManagerService.this.mPackages) {
                synchronized (this.mFileLock) {
                    AtomicFile file = getFile();
                    FileOutputStream fileOutputStream = null;
                    try {
                        fileOutputStream = file.startWrite();
                        BufferedOutputStream out = new BufferedOutputStream(fileOutputStream);
                        FileUtils.setPermissions(file.getBaseFile().getPath(), 416, 1000, 1032);
                        StringBuilder sb = new StringBuilder();
                        sb.append(USAGE_FILE_MAGIC_VERSION_1);
                        sb.append('\n');
                        out.write(sb.toString().getBytes(StandardCharsets.US_ASCII));
                        for (Package pkg : PackageManagerService.this.mPackages.values()) {
                            if (pkg.getLatestPackageUseTimeInMills() != 0) {
                                sb.setLength(0);
                                sb.append(pkg.packageName);
                                for (long usageTimeInMillis : pkg.mLastPackageUsageTimeInMills) {
                                    sb.append(' ');
                                    sb.append(usageTimeInMillis);
                                }
                                sb.append('\n');
                                out.write(sb.toString().getBytes(StandardCharsets.US_ASCII));
                            }
                        }
                        out.flush();
                        file.finishWrite(fileOutputStream);
                    } catch (IOException e) {
                        if (fileOutputStream != null) {
                            file.failWrite(fileOutputStream);
                        }
                        Log.e(PackageManagerService.TAG, "Failed to write package usage times", e);
                    }
                }
            }
            this.mLastWritten.set(SystemClock.elapsedRealtime());
        }

        void readLP() {
            Throwable th;
            IOException e;
            NullPointerException e2;
            Object in;
            synchronized (this.mFileLock) {
                AutoCloseable autoCloseable = null;
                try {
                    BufferedInputStream in2 = new BufferedInputStream(getFile().openRead());
                    try {
                        StringBuffer sb = new StringBuffer();
                        String firstLine = readLine(in2, sb);
                        if (firstLine != null) {
                            if (USAGE_FILE_MAGIC_VERSION_1.equals(firstLine)) {
                                readVersion1LP(in2, sb);
                            } else {
                                readVersion0LP(in2, sb, firstLine);
                            }
                        }
                        IoUtils.closeQuietly(in2);
                        BufferedInputStream bufferedInputStream = in2;
                    } catch (FileNotFoundException e3) {
                        autoCloseable = in2;
                        try {
                            this.mIsHistoricalPackageUsageAvailable = false;
                            IoUtils.closeQuietly(autoCloseable);
                            this.mLastWritten.set(SystemClock.elapsedRealtime());
                        } catch (Throwable th2) {
                            th = th2;
                            IoUtils.closeQuietly(autoCloseable);
                            throw th;
                        }
                    } catch (IOException e4) {
                        e = e4;
                        autoCloseable = in2;
                        Log.w(PackageManagerService.TAG, "Failed to read package usage times", e);
                        IoUtils.closeQuietly(autoCloseable);
                        this.mLastWritten.set(SystemClock.elapsedRealtime());
                    } catch (NullPointerException e5) {
                        e2 = e5;
                        in = in2;
                        Log.w(PackageManagerService.TAG, "error NullPointerException", e2);
                        HwBootFail.brokenFileBootFail(83886087, "/data/system/package-usage.list", new Throwable());
                        IoUtils.closeQuietly(autoCloseable);
                        this.mLastWritten.set(SystemClock.elapsedRealtime());
                    } catch (Throwable th3) {
                        th = th3;
                        in = in2;
                        IoUtils.closeQuietly(autoCloseable);
                        throw th;
                    }
                } catch (FileNotFoundException e6) {
                    this.mIsHistoricalPackageUsageAvailable = false;
                    IoUtils.closeQuietly(autoCloseable);
                    this.mLastWritten.set(SystemClock.elapsedRealtime());
                } catch (IOException e7) {
                    e = e7;
                    Log.w(PackageManagerService.TAG, "Failed to read package usage times", e);
                    IoUtils.closeQuietly(autoCloseable);
                    this.mLastWritten.set(SystemClock.elapsedRealtime());
                } catch (NullPointerException e8) {
                    e2 = e8;
                    Log.w(PackageManagerService.TAG, "error NullPointerException", e2);
                    HwBootFail.brokenFileBootFail(83886087, "/data/system/package-usage.list", new Throwable());
                    IoUtils.closeQuietly(autoCloseable);
                    this.mLastWritten.set(SystemClock.elapsedRealtime());
                }
            }
            this.mLastWritten.set(SystemClock.elapsedRealtime());
        }

        private void readVersion0LP(InputStream in, StringBuffer sb, String firstLine) throws IOException {
            String line = firstLine;
            while (line != null) {
                String[] tokens = line.split(" ");
                if (tokens.length != 2) {
                    throw new IOException("Failed to parse " + line + " as package-timestamp pair.");
                }
                Package pkg = (Package) PackageManagerService.this.mPackages.get(tokens[0]);
                if (pkg != null) {
                    long timestamp = parseAsLong(tokens[1]);
                    for (int reason = 0; reason < 8; reason++) {
                        pkg.mLastPackageUsageTimeInMills[reason] = timestamp;
                    }
                }
                line = readLine(in, sb);
            }
        }

        private void readVersion1LP(InputStream in, StringBuffer sb) throws IOException {
            while (true) {
                String line = readLine(in, sb);
                if (line != null) {
                    String[] tokens = line.split(" ");
                    if (tokens.length != 9) {
                        throw new IOException("Failed to parse " + line + " as a timestamp array.");
                    }
                    Package pkg = (Package) PackageManagerService.this.mPackages.get(tokens[0]);
                    if (pkg != null) {
                        for (int reason = 0; reason < 8; reason++) {
                            pkg.mLastPackageUsageTimeInMills[reason] = parseAsLong(tokens[reason + 1]);
                        }
                    }
                } else {
                    return;
                }
            }
        }

        private long parseAsLong(String token) throws IOException {
            try {
                return Long.parseLong(token);
            } catch (NumberFormatException e) {
                throw new IOException("Failed to parse " + token + " as a long.", e);
            }
        }

        private String readLine(InputStream in, StringBuffer sb) throws IOException {
            return readToken(in, sb, '\n');
        }

        private String readToken(InputStream in, StringBuffer sb, char endOfToken) throws IOException {
            sb.setLength(0);
            while (true) {
                char ch = in.read();
                if (ch == '￿') {
                    break;
                } else if (ch == endOfToken) {
                    return sb.toString();
                } else {
                    sb.append((char) ch);
                }
            }
            if (sb.length() == 0) {
                return null;
            }
            throw new IOException("Unexpected EOF");
        }

        private AtomicFile getFile() {
            return new AtomicFile(new File(new File(Environment.getDataDirectory(), "system"), "package-usage.list"));
        }
    }

    static class PendingPackageBroadcasts {
        final SparseArray<ArrayMap<String, ArrayList<String>>> mUidMap = new SparseArray(2);

        public ArrayList<String> get(int userId, String packageName) {
            return (ArrayList) getOrAllocate(userId).get(packageName);
        }

        public void put(int userId, String packageName, ArrayList<String> components) {
            getOrAllocate(userId).put(packageName, components);
        }

        public void remove(int userId, String packageName) {
            ArrayMap<String, ArrayList<String>> packages = (ArrayMap) this.mUidMap.get(userId);
            if (packages != null) {
                packages.remove(packageName);
            }
        }

        public void remove(int userId) {
            this.mUidMap.remove(userId);
        }

        public int userIdCount() {
            return this.mUidMap.size();
        }

        public int userIdAt(int n) {
            return this.mUidMap.keyAt(n);
        }

        public ArrayMap<String, ArrayList<String>> packagesForUserId(int userId) {
            return (ArrayMap) this.mUidMap.get(userId);
        }

        public int size() {
            int num = 0;
            for (int i = 0; i < this.mUidMap.size(); i++) {
                num += ((ArrayMap) this.mUidMap.valueAt(i)).size();
            }
            return num;
        }

        public void clear() {
            this.mUidMap.clear();
        }

        private ArrayMap<String, ArrayList<String>> getOrAllocate(int userId) {
            ArrayMap<String, ArrayList<String>> map = (ArrayMap) this.mUidMap.get(userId);
            if (map != null) {
                return map;
            }
            map = new ArrayMap();
            this.mUidMap.put(userId, map);
            return map;
        }
    }

    static class PostInstallData {
        public InstallArgs args;
        public PackageInstalledInfo res;

        PostInstallData(InstallArgs _a, PackageInstalledInfo _r) {
            this.args = _a;
            this.res = _r;
        }
    }

    private final class ProviderIntentResolver extends IntentResolver<ProviderIntentInfo, ResolveInfo> {
        private int mFlags;
        private final ArrayMap<ComponentName, Provider> mProviders;

        private ProviderIntentResolver() {
            this.mProviders = new ArrayMap();
        }

        public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, boolean defaultOnly, int userId) {
            this.mFlags = defaultOnly ? 65536 : 0;
            return super.queryIntent(intent, resolvedType, defaultOnly, userId);
        }

        public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, int flags, int userId) {
            boolean z = false;
            if (!PackageManagerService.sUserManager.exists(userId)) {
                return null;
            }
            this.mFlags = flags;
            if ((65536 & flags) != 0) {
                z = true;
            }
            return super.queryIntent(intent, resolvedType, z, userId);
        }

        public List<ResolveInfo> queryIntentForPackage(Intent intent, String resolvedType, int flags, ArrayList<Provider> packageProviders, int userId) {
            if (!PackageManagerService.sUserManager.exists(userId) || packageProviders == null) {
                return null;
            }
            this.mFlags = flags;
            boolean defaultOnly = (65536 & flags) != 0;
            int N = packageProviders.size();
            ArrayList<ProviderIntentInfo[]> listCut = new ArrayList(N);
            for (int i = 0; i < N; i++) {
                ArrayList<ProviderIntentInfo> intentFilters = ((Provider) packageProviders.get(i)).intents;
                if (intentFilters != null && intentFilters.size() > 0) {
                    ProviderIntentInfo[] array = new ProviderIntentInfo[intentFilters.size()];
                    intentFilters.toArray(array);
                    listCut.add(array);
                }
            }
            return super.queryIntentFromList(intent, resolvedType, defaultOnly, listCut, userId);
        }

        public final void addProvider(Provider p) {
            if (this.mProviders.containsKey(p.getComponentName())) {
                Slog.w(PackageManagerService.TAG, "Provider " + p.getComponentName() + " already defined; ignoring");
                return;
            }
            this.mProviders.put(p.getComponentName(), p);
            int NI = p.intents.size();
            for (int j = 0; j < NI; j++) {
                ProviderIntentInfo intent = (ProviderIntentInfo) p.intents.get(j);
                if (!intent.debugCheck()) {
                    Log.w(PackageManagerService.TAG, "==> For Provider " + p.info.name);
                }
                addFilter(intent);
            }
        }

        public final void removeProvider(Provider p) {
            this.mProviders.remove(p.getComponentName());
            int NI = p.intents.size();
            for (int j = 0; j < NI; j++) {
                removeFilter((ProviderIntentInfo) p.intents.get(j));
            }
        }

        protected boolean allowFilterResult(ProviderIntentInfo filter, List<ResolveInfo> dest) {
            ProviderInfo filterPi = filter.provider.info;
            for (int i = dest.size() - 1; i >= 0; i--) {
                ProviderInfo destPi = ((ResolveInfo) dest.get(i)).providerInfo;
                if (destPi.name == filterPi.name && destPi.packageName == filterPi.packageName) {
                    return false;
                }
            }
            return true;
        }

        protected ProviderIntentInfo[] newArray(int size) {
            return new ProviderIntentInfo[size];
        }

        protected boolean isFilterStopped(ProviderIntentInfo filter, int userId) {
            boolean z = false;
            if (!PackageManagerService.sUserManager.exists(userId)) {
                return true;
            }
            Package p = filter.provider.owner;
            if (p != null) {
                PackageSetting ps = p.mExtras;
                if (ps != null) {
                    if ((ps.pkgFlags & 1) == 0) {
                        z = ps.getStopped(userId);
                    }
                    return z;
                }
            }
            return false;
        }

        protected boolean isPackageForFilter(String packageName, ProviderIntentInfo info) {
            return packageName.equals(info.provider.owner.packageName);
        }

        protected ResolveInfo newResult(ProviderIntentInfo filter, int match, int userId) {
            if (!PackageManagerService.sUserManager.exists(userId)) {
                return null;
            }
            ProviderIntentInfo info = filter;
            if (!PackageManagerService.this.mSettings.isEnabledAndMatchLPr(filter.provider.info, this.mFlags, userId)) {
                return null;
            }
            Provider provider = filter.provider;
            PackageSetting ps = provider.owner.mExtras;
            if (ps == null) {
                return null;
            }
            if (PackageManagerService.this.mSafeMode && !PackageManagerService.this.isSystemPathApp(ps)) {
                return null;
            }
            ProviderInfo pi = PackageParser.generateProviderInfo(provider, this.mFlags, ps.readUserState(userId), userId);
            if (pi == null) {
                return null;
            }
            ResolveInfo res = new ResolveInfo();
            res.providerInfo = pi;
            if ((this.mFlags & 64) != 0) {
                res.filter = filter;
            }
            res.priority = filter.getPriority();
            res.preferredOrder = provider.owner.mPreferredOrder;
            res.match = match;
            res.isDefault = filter.hasDefault;
            res.labelRes = filter.labelRes;
            res.nonLocalizedLabel = filter.nonLocalizedLabel;
            res.icon = filter.icon;
            res.system = res.providerInfo.applicationInfo.isSystemApp();
            return res;
        }

        protected void sortResults(List<ResolveInfo> results) {
            Collections.sort(results, PackageManagerService.mResolvePrioritySorter);
        }

        protected void dumpFilter(PrintWriter out, String prefix, ProviderIntentInfo filter) {
            out.print(prefix);
            out.print(Integer.toHexString(System.identityHashCode(filter.provider)));
            out.print(' ');
            filter.provider.printComponentShortName(out);
            out.print(" filter ");
            out.println(Integer.toHexString(System.identityHashCode(filter)));
        }

        protected Object filterToLabel(ProviderIntentInfo filter) {
            return filter.provider;
        }

        protected void dumpFilterLabel(PrintWriter out, String prefix, Object label, int count) {
            Provider provider = (Provider) label;
            out.print(prefix);
            out.print(Integer.toHexString(System.identityHashCode(provider)));
            out.print(' ');
            provider.printComponentShortName(out);
            if (count > 1) {
                out.print(" (");
                out.print(count);
                out.print(" filters)");
            }
            out.println();
        }
    }

    private final class ServiceIntentResolver extends IntentResolver<ServiceIntentInfo, ResolveInfo> {
        private int mFlags;
        private final ArrayMap<ComponentName, Service> mServices;

        private ServiceIntentResolver() {
            this.mServices = new ArrayMap();
        }

        public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, boolean defaultOnly, int userId) {
            this.mFlags = defaultOnly ? 65536 : 0;
            return super.queryIntent(intent, resolvedType, defaultOnly, userId);
        }

        public List<ResolveInfo> queryIntent(Intent intent, String resolvedType, int flags, int userId) {
            boolean z = false;
            if (!PackageManagerService.sUserManager.exists(userId)) {
                return null;
            }
            this.mFlags = flags;
            if ((65536 & flags) != 0) {
                z = true;
            }
            return super.queryIntent(intent, resolvedType, z, userId);
        }

        public List<ResolveInfo> queryIntentForPackage(Intent intent, String resolvedType, int flags, ArrayList<Service> packageServices, int userId) {
            if (!PackageManagerService.sUserManager.exists(userId) || packageServices == null) {
                return null;
            }
            this.mFlags = flags;
            boolean defaultOnly = (65536 & flags) != 0;
            int N = packageServices.size();
            ArrayList<ServiceIntentInfo[]> listCut = new ArrayList(N);
            for (int i = 0; i < N; i++) {
                ArrayList<ServiceIntentInfo> intentFilters = ((Service) packageServices.get(i)).intents;
                if (intentFilters != null && intentFilters.size() > 0) {
                    ServiceIntentInfo[] array = new ServiceIntentInfo[intentFilters.size()];
                    intentFilters.toArray(array);
                    listCut.add(array);
                }
            }
            return super.queryIntentFromList(intent, resolvedType, defaultOnly, listCut, userId);
        }

        public final void addService(Service s) {
            this.mServices.put(s.getComponentName(), s);
            int NI = s.intents.size();
            for (int j = 0; j < NI; j++) {
                ServiceIntentInfo intent = (ServiceIntentInfo) s.intents.get(j);
                if (!intent.debugCheck()) {
                    Log.w(PackageManagerService.TAG, "==> For Service " + s.info.name);
                }
                addFilter(intent);
            }
        }

        public final void removeService(Service s) {
            this.mServices.remove(s.getComponentName());
            int NI = s.intents.size();
            for (int j = 0; j < NI; j++) {
                removeFilter((ServiceIntentInfo) s.intents.get(j));
            }
        }

        protected boolean allowFilterResult(ServiceIntentInfo filter, List<ResolveInfo> dest) {
            ServiceInfo filterSi = filter.service.info;
            for (int i = dest.size() - 1; i >= 0; i--) {
                ServiceInfo destAi = ((ResolveInfo) dest.get(i)).serviceInfo;
                if (destAi.name == filterSi.name && destAi.packageName == filterSi.packageName) {
                    return false;
                }
            }
            return true;
        }

        protected ServiceIntentInfo[] newArray(int size) {
            return new ServiceIntentInfo[size];
        }

        protected boolean isFilterStopped(ServiceIntentInfo filter, int userId) {
            boolean z = false;
            if (!PackageManagerService.sUserManager.exists(userId)) {
                return true;
            }
            Package p = filter.service.owner;
            if (p != null) {
                PackageSetting ps = p.mExtras;
                if (ps != null) {
                    if ((ps.pkgFlags & 1) == 0) {
                        z = ps.getStopped(userId);
                    }
                    return z;
                }
            }
            return false;
        }

        protected boolean isPackageForFilter(String packageName, ServiceIntentInfo info) {
            return packageName.equals(info.service.owner.packageName);
        }

        protected ResolveInfo newResult(ServiceIntentInfo filter, int match, int userId) {
            if (!PackageManagerService.sUserManager.exists(userId)) {
                return null;
            }
            ServiceIntentInfo info = filter;
            if (!PackageManagerService.this.mSettings.isEnabledAndMatchLPr(filter.service.info, this.mFlags, userId)) {
                return null;
            }
            Service service = filter.service;
            PackageSetting ps = service.owner.mExtras;
            if (ps == null) {
                return null;
            }
            if (PackageManagerService.this.mSafeMode && !PackageManagerService.this.isSystemPathApp(ps)) {
                return null;
            }
            ServiceInfo si = PackageParser.generateServiceInfo(service, this.mFlags, ps.readUserState(userId), userId);
            if (si == null) {
                return null;
            }
            ResolveInfo res = new ResolveInfo();
            res.serviceInfo = si;
            if ((this.mFlags & 64) != 0) {
                res.filter = filter;
            }
            res.priority = filter.getPriority();
            res.preferredOrder = service.owner.mPreferredOrder;
            res.match = match;
            res.isDefault = filter.hasDefault;
            res.labelRes = filter.labelRes;
            res.nonLocalizedLabel = filter.nonLocalizedLabel;
            res.icon = filter.icon;
            res.system = res.serviceInfo.applicationInfo.isSystemApp();
            return res;
        }

        protected void sortResults(List<ResolveInfo> results) {
            Collections.sort(results, PackageManagerService.mResolvePrioritySorter);
        }

        protected void dumpFilter(PrintWriter out, String prefix, ServiceIntentInfo filter) {
            out.print(prefix);
            out.print(Integer.toHexString(System.identityHashCode(filter.service)));
            out.print(' ');
            filter.service.printComponentShortName(out);
            out.print(" filter ");
            out.println(Integer.toHexString(System.identityHashCode(filter)));
        }

        protected Object filterToLabel(ServiceIntentInfo filter) {
            return filter.service;
        }

        protected void dumpFilterLabel(PrintWriter out, String prefix, Object label, int count) {
            Service service = (Service) label;
            out.print(prefix);
            out.print(Integer.toHexString(System.identityHashCode(service)));
            out.print(' ');
            service.printComponentShortName(out);
            if (count > 1) {
                out.print(" (");
                out.print(count);
                out.print(" filters)");
            }
            out.println();
        }
    }

    public static final class SharedLibraryEntry {
        public final String apk;
        public final String path;

        SharedLibraryEntry(String _path, String _apk) {
            this.path = _path;
            this.apk = _apk;
        }
    }

    static class VerificationInfo {
        public static final int NO_UID = -1;
        final int installerUid;
        final int originatingUid;
        final Uri originatingUri;
        final Uri referrer;

        VerificationInfo(Uri originatingUri, Uri referrer, int originatingUid, int installerUid) {
            this.originatingUri = originatingUri;
            this.referrer = referrer;
            this.originatingUid = originatingUid;
            this.installerUid = installerUid;
        }
    }

    private void installPackageLI(com.android.server.pm.PackageManagerService.InstallArgs r50, com.android.server.pm.PackageManagerService.PackageInstalledInfo r51) {
        /* JADX: method processing error */
/*
Error: java.lang.OutOfMemoryError: Java heap space
	at java.util.Arrays.copyOf(Arrays.java:3181)
	at java.util.ArrayList.grow(ArrayList.java:261)
	at java.util.ArrayList.ensureExplicitCapacity(ArrayList.java:235)
	at java.util.ArrayList.ensureCapacityInternal(ArrayList.java:227)
	at java.util.ArrayList.add(ArrayList.java:458)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:463)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
	at jadx.core.utils.BlockUtils.collectWhileDominates(BlockUtils.java:464)
*/
        /*
        r49 = this;
        r0 = r50;
        r0 = r0.installFlags;
        r28 = r0;
        r0 = r50;
        r0 = r0.installerPackageName;
        r29 = r0;
        r0 = r50;
        r10 = r0.volumeUuid;
        r46 = new java.io.File;
        r4 = r50.getCodePath();
        r0 = r46;
        r0.<init>(r4);
        r4 = r28 & 1;
        if (r4 == 0) goto L_0x0094;
    L_0x001f:
        r23 = 1;
    L_0x0021:
        r4 = r28 & 8;
        if (r4 != 0) goto L_0x0097;
    L_0x0025:
        r0 = r50;
        r4 = r0.volumeUuid;
        if (r4 == 0) goto L_0x009a;
    L_0x002b:
        r35 = 1;
    L_0x002d:
        r0 = r28;
        r4 = r0 & 2048;
        if (r4 == 0) goto L_0x009d;
    L_0x0033:
        r21 = 1;
    L_0x0035:
        r0 = r28;
        r4 = r0 & 8192;
        if (r4 == 0) goto L_0x00a0;
    L_0x003b:
        r22 = 1;
    L_0x003d:
        r42 = 0;
        r43 = 24;
        r0 = r50;
        r4 = r0.move;
        if (r4 == 0) goto L_0x0049;
    L_0x0047:
        r43 = 16408; // 0x4018 float:2.2993E-41 double:8.1066E-320;
    L_0x0049:
        r0 = r28;
        r4 = r0 & 4096;
        if (r4 == 0) goto L_0x0053;
    L_0x004f:
        r4 = 131072; // 0x20000 float:1.83671E-40 double:6.47582E-319;
        r43 = r43 | r4;
    L_0x0053:
        r4 = 1;
        r0 = r51;
        r0.setReturnCode(r4);
        r26 = android.os.SystemClock.elapsedRealtime();
        if (r21 == 0) goto L_0x00a3;
    L_0x005f:
        if (r23 != 0) goto L_0x0063;
    L_0x0061:
        if (r35 == 0) goto L_0x00a3;
    L_0x0063:
        r4 = "PackageManager";
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r7 = "Incompatible ephemeral install; fwdLocked=";
        r6 = r6.append(r7);
        r0 = r23;
        r6 = r6.append(r0);
        r7 = " external=";
        r6 = r6.append(r7);
        r0 = r35;
        r6 = r6.append(r0);
        r6 = r6.toString();
        android.util.Slog.i(r4, r6);
        r4 = -116; // 0xffffffffffffff8c float:NaN double:NaN;
        r0 = r51;
        r0.setReturnCode(r4);
        return;
    L_0x0094:
        r23 = 0;
        goto L_0x0021;
    L_0x0097:
        r35 = 1;
        goto L_0x002d;
    L_0x009a:
        r35 = 0;
        goto L_0x002d;
    L_0x009d:
        r21 = 0;
        goto L_0x0035;
    L_0x00a0:
        r22 = 0;
        goto L_0x003d;
    L_0x00a3:
        r0 = r49;
        r4 = r0.mDefParseFlags;
        r4 = r4 | 2;
        r6 = r4 | 1024;
        if (r23 == 0) goto L_0x0199;
    L_0x00ad:
        r4 = 16;
    L_0x00af:
        r6 = r6 | r4;
        if (r35 == 0) goto L_0x019c;
    L_0x00b2:
        r4 = 32;
    L_0x00b4:
        r6 = r6 | r4;
        if (r21 == 0) goto L_0x019f;
    L_0x00b7:
        r4 = 2048; // 0x800 float:2.87E-42 double:1.0118E-320;
    L_0x00b9:
        r6 = r6 | r4;
        if (r22 == 0) goto L_0x01a2;
    L_0x00bc:
        r4 = 4096; // 0x1000 float:5.74E-42 double:2.0237E-320;
    L_0x00be:
        r36 = r6 | r4;
        r40 = new android.content.pm.PackageParser;
        r40.<init>();
        r0 = r49;
        r4 = r0.mSeparateProcesses;
        r0 = r40;
        r0.setSeparateProcesses(r4);
        r0 = r49;
        r4 = r0.mMetrics;
        r0 = r40;
        r0.setDisplayMetrics(r4);
        r4 = "parsePackage";
        r6 = 262144; // 0x40000 float:3.67342E-40 double:1.295163E-318;
        android.os.Trace.traceBegin(r6, r4);
        r0 = r40;	 Catch:{ PackageParserException -> 0x01a5, all -> 0x01b7 }
        r1 = r46;	 Catch:{ PackageParserException -> 0x01a5, all -> 0x01b7 }
        r2 = r36;	 Catch:{ PackageParserException -> 0x01a5, all -> 0x01b7 }
        r5 = r0.parsePackage(r1, r2);	 Catch:{ PackageParserException -> 0x01a5, all -> 0x01b7 }
        if (r5 == 0) goto L_0x00f9;	 Catch:{ PackageParserException -> 0x01a5, all -> 0x01b7 }
    L_0x00ec:
        r4 = r5.packageName;	 Catch:{ PackageParserException -> 0x01a5, all -> 0x01b7 }
        r0 = r49;	 Catch:{ PackageParserException -> 0x01a5, all -> 0x01b7 }
        r4 = r0.isInMultiWinWhiteList(r4);	 Catch:{ PackageParserException -> 0x01a5, all -> 0x01b7 }
        if (r4 == 0) goto L_0x00f9;	 Catch:{ PackageParserException -> 0x01a5, all -> 0x01b7 }
    L_0x00f6:
        r5.forceResizeableAllActivity();	 Catch:{ PackageParserException -> 0x01a5, all -> 0x01b7 }
    L_0x00f9:
        r6 = 262144; // 0x40000 float:3.67342E-40 double:1.295163E-318;
        android.os.Trace.traceEnd(r6);
        r4 = r5.childPackages;
        if (r4 == 0) goto L_0x01c0;
    L_0x0103:
        r0 = r49;
        r6 = r0.mPackages;
        monitor-enter(r6);
        r4 = r5.childPackages;	 Catch:{ all -> 0x0225 }
        r15 = r4.size();	 Catch:{ all -> 0x0225 }
        r25 = 0;	 Catch:{ all -> 0x0225 }
    L_0x0110:
        r0 = r25;	 Catch:{ all -> 0x0225 }
        if (r0 >= r15) goto L_0x01bf;	 Catch:{ all -> 0x0225 }
    L_0x0114:
        r4 = r5.childPackages;	 Catch:{ all -> 0x0225 }
        r0 = r25;	 Catch:{ all -> 0x0225 }
        r16 = r4.get(r0);	 Catch:{ all -> 0x0225 }
        r16 = (android.content.pm.PackageParser.Package) r16;	 Catch:{ all -> 0x0225 }
        r18 = new com.android.server.pm.PackageManagerService$PackageInstalledInfo;	 Catch:{ all -> 0x0225 }
        r18.<init>();	 Catch:{ all -> 0x0225 }
        r4 = 1;	 Catch:{ all -> 0x0225 }
        r0 = r18;	 Catch:{ all -> 0x0225 }
        r0.setReturnCode(r4);	 Catch:{ all -> 0x0225 }
        r0 = r16;	 Catch:{ all -> 0x0225 }
        r1 = r18;	 Catch:{ all -> 0x0225 }
        r1.pkg = r0;	 Catch:{ all -> 0x0225 }
        r0 = r16;	 Catch:{ all -> 0x0225 }
        r4 = r0.packageName;	 Catch:{ all -> 0x0225 }
        r0 = r18;	 Catch:{ all -> 0x0225 }
        r0.name = r4;	 Catch:{ all -> 0x0225 }
        r0 = r49;	 Catch:{ all -> 0x0225 }
        r4 = r0.mSettings;	 Catch:{ all -> 0x0225 }
        r0 = r16;	 Catch:{ all -> 0x0225 }
        r7 = r0.packageName;	 Catch:{ all -> 0x0225 }
        r17 = r4.peekPackageLPr(r7);	 Catch:{ all -> 0x0225 }
        if (r17 == 0) goto L_0x0156;	 Catch:{ all -> 0x0225 }
    L_0x0145:
        r4 = sUserManager;	 Catch:{ all -> 0x0225 }
        r4 = r4.getUserIds();	 Catch:{ all -> 0x0225 }
        r7 = 1;	 Catch:{ all -> 0x0225 }
        r0 = r17;	 Catch:{ all -> 0x0225 }
        r4 = r0.queryInstalledUsers(r4, r7);	 Catch:{ all -> 0x0225 }
        r0 = r18;	 Catch:{ all -> 0x0225 }
        r0.origUsers = r4;	 Catch:{ all -> 0x0225 }
    L_0x0156:
        r0 = r49;	 Catch:{ all -> 0x0225 }
        r4 = r0.mPackages;	 Catch:{ all -> 0x0225 }
        r0 = r16;	 Catch:{ all -> 0x0225 }
        r7 = r0.packageName;	 Catch:{ all -> 0x0225 }
        r4 = r4.containsKey(r7);	 Catch:{ all -> 0x0225 }
        if (r4 == 0) goto L_0x0179;	 Catch:{ all -> 0x0225 }
    L_0x0164:
        r4 = new com.android.server.pm.PackageManagerService$PackageRemovedInfo;	 Catch:{ all -> 0x0225 }
        r0 = r49;	 Catch:{ all -> 0x0225 }
        r4.<init>();	 Catch:{ all -> 0x0225 }
        r0 = r18;	 Catch:{ all -> 0x0225 }
        r0.removedInfo = r4;	 Catch:{ all -> 0x0225 }
        r0 = r18;	 Catch:{ all -> 0x0225 }
        r4 = r0.removedInfo;	 Catch:{ all -> 0x0225 }
        r0 = r16;	 Catch:{ all -> 0x0225 }
        r7 = r0.packageName;	 Catch:{ all -> 0x0225 }
        r4.removedPackage = r7;	 Catch:{ all -> 0x0225 }
    L_0x0179:
        r0 = r51;	 Catch:{ all -> 0x0225 }
        r4 = r0.addedChildPackages;	 Catch:{ all -> 0x0225 }
        if (r4 != 0) goto L_0x0188;	 Catch:{ all -> 0x0225 }
    L_0x017f:
        r4 = new android.util.ArrayMap;	 Catch:{ all -> 0x0225 }
        r4.<init>();	 Catch:{ all -> 0x0225 }
        r0 = r51;	 Catch:{ all -> 0x0225 }
        r0.addedChildPackages = r4;	 Catch:{ all -> 0x0225 }
    L_0x0188:
        r0 = r51;	 Catch:{ all -> 0x0225 }
        r4 = r0.addedChildPackages;	 Catch:{ all -> 0x0225 }
        r0 = r16;	 Catch:{ all -> 0x0225 }
        r7 = r0.packageName;	 Catch:{ all -> 0x0225 }
        r0 = r18;	 Catch:{ all -> 0x0225 }
        r4.put(r7, r0);	 Catch:{ all -> 0x0225 }
        r25 = r25 + 1;
        goto L_0x0110;
    L_0x0199:
        r4 = 0;
        goto L_0x00af;
    L_0x019c:
        r4 = 0;
        goto L_0x00b4;
    L_0x019f:
        r4 = 0;
        goto L_0x00b9;
    L_0x01a2:
        r4 = 0;
        goto L_0x00be;
    L_0x01a5:
        r19 = move-exception;
        r4 = "Failed parse during installPackageLI";	 Catch:{ PackageParserException -> 0x01a5, all -> 0x01b7 }
        r0 = r51;	 Catch:{ PackageParserException -> 0x01a5, all -> 0x01b7 }
        r1 = r19;	 Catch:{ PackageParserException -> 0x01a5, all -> 0x01b7 }
        r0.setError(r4, r1);	 Catch:{ PackageParserException -> 0x01a5, all -> 0x01b7 }
        r6 = 262144; // 0x40000 float:3.67342E-40 double:1.295163E-318;
        android.os.Trace.traceEnd(r6);
        return;
    L_0x01b7:
        r4 = move-exception;
        r6 = 262144; // 0x40000 float:3.67342E-40 double:1.295163E-318;
        android.os.Trace.traceEnd(r6);
        throw r4;
    L_0x01bf:
        monitor-exit(r6);
    L_0x01c0:
        r4 = r5.cpuAbiOverride;
        r4 = android.text.TextUtils.isEmpty(r4);
        if (r4 == 0) goto L_0x01ce;
    L_0x01c8:
        r0 = r50;
        r4 = r0.abiOverride;
        r5.cpuAbiOverride = r4;
    L_0x01ce:
        r0 = r5.packageName;
        r38 = r0;
        r0 = r38;
        r1 = r51;
        r1.name = r0;
        r0 = r49;
        r1 = r29;
        r2 = r38;
        r4 = r0.isAppInstallAllowed(r1, r2);
        if (r4 == 0) goto L_0x01f8;
    L_0x01e4:
        r0 = r50;
        r4 = r0.origin;
        if (r4 == 0) goto L_0x0228;
    L_0x01ea:
        r0 = r50;
        r4 = r0.origin;
        r4 = r4.resolvedPath;
        r0 = r49;
        r4 = r0.isUnAppInstallAllowed(r4);
        if (r4 == 0) goto L_0x0228;
    L_0x01f8:
        r4 = "Disallow install new apps";
        r6 = -111; // 0xffffffffffffff91 float:NaN double:NaN;
        r0 = r51;
        r0.setError(r6, r4);
        r4 = "PackageManager";
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r0 = r29;
        r6 = r6.append(r0);
        r7 = " is disallowed to install new app ";
        r6 = r6.append(r7);
        r0 = r38;
        r6 = r6.append(r0);
        r6 = r6.toString();
        android.util.Slog.i(r4, r6);
        return;
    L_0x0225:
        r4 = move-exception;
        monitor-exit(r6);
        throw r4;
    L_0x0228:
        r4 = r5.applicationInfo;
        r4 = r4.flags;
        r4 = r4 & 256;
        if (r4 == 0) goto L_0x023f;
    L_0x0230:
        r4 = r28 & 4;
        if (r4 != 0) goto L_0x023f;
    L_0x0234:
        r4 = "installPackageLI";
        r6 = -15;
        r0 = r51;
        r0.setError(r6, r4);
        return;
    L_0x023f:
        r0 = r49;
        r0.computeMetaHash(r5);
        r0 = r50;	 Catch:{ PackageParserException -> 0x02db }
        r4 = r0.certificates;	 Catch:{ PackageParserException -> 0x02db }
        if (r4 == 0) goto L_0x02e7;
    L_0x024a:
        r0 = r50;	 Catch:{ PackageParserException -> 0x02d3 }
        r4 = r0.certificates;	 Catch:{ PackageParserException -> 0x02d3 }
        android.content.pm.PackageParser.populateCertificates(r5, r4);	 Catch:{ PackageParserException -> 0x02d3 }
    L_0x0251:
        r40 = 0;
        r31 = 0;
        r45 = 0;
        r0 = r49;
        r6 = r0.mPackages;
        monitor-enter(r6);
        r4 = r28 & 2;
        if (r4 == 0) goto L_0x03ae;
    L_0x0260:
        r0 = r49;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.mSettings;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.mRenamedPackages;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r38;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r32 = r4.get(r0);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r32 = (java.lang.String) r32;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r5.mOriginalPackages;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 == 0) goto L_0x02ee;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x0272:
        r4 = r5.mOriginalPackages;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r32;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.contains(r0);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 == 0) goto L_0x02ee;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x027c:
        r0 = r49;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.mPackages;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r32;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.containsKey(r0);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 == 0) goto L_0x02ee;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x0288:
        r0 = r32;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r5.setPackageName(r0);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r5.packageName;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r38 = r0;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r42 = 1;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x0293:
        r4 = r5.parentPackage;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 == 0) goto L_0x02fd;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x0297:
        r4 = new java.lang.StringBuilder;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4.<init>();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = "Package ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r5.packageName;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = " is child of package ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r5.parentPackage;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.parentPackage;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = ". Child packages ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = "can be updated only through the parent package.";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.toString();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = -106; // 0xffffffffffffff96 float:NaN double:NaN;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r51;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0.setError(r7, r4);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        monitor-exit(r6);
        return;
    L_0x02d3:
        r19 = move-exception;
        r0 = r36;	 Catch:{ PackageParserException -> 0x02db }
        android.content.pm.PackageParser.collectCertificates(r5, r0);	 Catch:{ PackageParserException -> 0x02db }
        goto L_0x0251;
    L_0x02db:
        r19 = move-exception;
        r4 = "Failed collect during installPackageLI";
        r0 = r51;
        r1 = r19;
        r0.setError(r4, r1);
        return;
    L_0x02e7:
        r0 = r36;	 Catch:{ PackageParserException -> 0x02db }
        android.content.pm.PackageParser.collectCertificates(r5, r0);	 Catch:{ PackageParserException -> 0x02db }
        goto L_0x0251;
    L_0x02ee:
        r0 = r49;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.mPackages;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r38;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.containsKey(r0);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 == 0) goto L_0x0293;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x02fa:
        r42 = 1;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        goto L_0x0293;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x02fd:
        if (r42 == 0) goto L_0x03ae;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x02ff:
        r0 = r49;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.mPackages;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r38;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r33 = r4.get(r0);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r33 = (android.content.pm.PackageParser.Package) r33;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r33;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.applicationInfo;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r4.targetSdkVersion;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r34 = r0;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r5.applicationInfo;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r4.targetSdkVersion;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r30 = r0;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = 22;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r34;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r0 <= r4) goto L_0x036c;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x031f:
        r4 = 22;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r30;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r0 > r4) goto L_0x036c;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x0325:
        r4 = new java.lang.StringBuilder;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4.<init>();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = "Package ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r5.packageName;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = " new target SDK ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r30;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r0);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = " doesn't support runtime permissions but the old";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = " target SDK ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r34;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r0);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = " does.";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.toString();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = -26;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r51;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0.setError(r7, r4);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        monitor-exit(r6);
        return;
    L_0x036c:
        r0 = r33;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.parentPackage;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 == 0) goto L_0x03ae;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x0372:
        r4 = new java.lang.StringBuilder;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4.<init>();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = "Package ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r5.packageName;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = " is child of package ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r33;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r0.parentPackage;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = ". Child packages ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = "can be updated only through the parent package.";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.toString();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = -106; // 0xffffffffffffff96 float:NaN double:NaN;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r51;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0.setError(r7, r4);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        monitor-exit(r6);
        return;
    L_0x03ae:
        r0 = r49;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.mSettings;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.mPackages;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r38;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r41 = r4.get(r0);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r41 = (com.android.server.pm.PackageSetting) r41;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = 1;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r49;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0.checkHwCertification(r5, r4);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = 0;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = 1;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r49;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r1 = r41;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0.replaceSignatureIfNeeded(r1, r5, r4, r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r41 == 0) goto L_0x047d;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x03cd:
        r0 = r49;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r1 = r41;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r2 = r43;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.shouldCheckUpgradeKeySetLP(r1, r2);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 == 0) goto L_0x040f;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x03d9:
        r0 = r49;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r1 = r41;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.checkUpgradeKeySetLP(r1, r5);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 != 0) goto L_0x0416;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x03e3:
        r4 = new java.lang.StringBuilder;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4.<init>();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = "Package ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r5.packageName;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = " upgrade keys do not match the ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = "previously installed version";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.toString();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = -7;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r51;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0.setError(r7, r4);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        monitor-exit(r6);
        return;
    L_0x040f:
        r0 = r49;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r1 = r41;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0.verifySignaturesLP(r1, r5);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x0416:
        r4 = "ro.config.hw_optb";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = "0";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = android.os.SystemProperties.get(r4, r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = "156";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.equals(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 == 0) goto L_0x043e;
    L_0x0429:
        r0 = r50;	 Catch:{ PackageManagerException -> 0x0584 }
        r4 = r0.user;	 Catch:{ PackageManagerException -> 0x0584 }
        r4 = r4.getIdentifier();	 Catch:{ PackageManagerException -> 0x0584 }
        r7 = android.os.Binder.getCallingUid();	 Catch:{ PackageManagerException -> 0x0584 }
        r0 = r49;	 Catch:{ PackageManagerException -> 0x0584 }
        r1 = r29;	 Catch:{ PackageManagerException -> 0x0584 }
        r2 = r38;	 Catch:{ PackageManagerException -> 0x0584 }
        r0.verifyValidVerifierInstall(r1, r2, r4, r7);	 Catch:{ PackageManagerException -> 0x0584 }
    L_0x043e:
        r0 = r49;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.mSettings;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.mPackages;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r38;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.get(r0);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = (com.android.server.pm.PackageSetting) r4;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r4.codePathString;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r31 = r0;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r41;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.pkg;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 == 0) goto L_0x046c;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x0456:
        r0 = r41;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.pkg;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.applicationInfo;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 == 0) goto L_0x046c;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x045e:
        r0 = r41;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.pkg;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.applicationInfo;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.flags;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4 & 1;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 == 0) goto L_0x0594;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x046a:
        r45 = 1;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x046c:
        r4 = sUserManager;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.getUserIds();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = 1;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r41;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.queryInstalledUsers(r4, r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r51;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0.origUsers = r4;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x047d:
        r4 = r5.permissions;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r12 = r4.size();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r25 = r12 + -1;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x0485:
        if (r25 < 0) goto L_0x0699;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x0487:
        r4 = r5.permissions;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r25;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r37 = r4.get(r0);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r37 = (android.content.pm.PackageParser.Permission) r37;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r49;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.mSettings;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.mPermissions;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r37;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r0.info;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.name;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r14 = r4.get(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r14 = (com.android.server.pm.BasePermission) r14;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r14 == 0) goto L_0x05f6;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x04a5:
        r4 = r14.sourcePackage;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r5.packageName;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.equals(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 == 0) goto L_0x0598;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x04af:
        r4 = r14.packageSetting;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4 instanceof com.android.server.pm.PackageSetting;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 == 0) goto L_0x0598;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x04b5:
        r4 = r14.packageSetting;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = (com.android.server.pm.PackageSetting) r4;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r49;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r1 = r43;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.shouldCheckUpgradeKeySetLP(r4, r1);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 == 0) goto L_0x0598;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x04c3:
        r4 = r14.packageSetting;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = (com.android.server.pm.PackageSetting) r4;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r49;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r44 = r0.checkUpgradeKeySetLP(r4, r5);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x04cd:
        if (r44 != 0) goto L_0x0636;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x04cf:
        r4 = r14.sourcePackage;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = "android";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.equals(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 != 0) goto L_0x05fa;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x04da:
        r4 = r14.protectionLevel;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r37;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r0.info;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.protectionLevel;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4 | r7;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4 & 15;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = 2;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 != r7) goto L_0x05ae;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x04e8:
        r4 = new java.lang.StringBuilder;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4.<init>();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = "Package ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r5.packageName;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = " attempting to redeclare permission ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r37;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r0.info;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.name;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = " already owned by ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r14.sourcePackage;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.append(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.toString();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = -112; // 0xffffffffffffff90 float:NaN double:NaN;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r51;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0.setError(r7, r4);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r37;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.info;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.name;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r51;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0.origPermission = r4;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r14.sourcePackage;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r51;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0.origPackage = r4;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        monitor-exit(r6);
        return;
    L_0x0535:
        r20 = move-exception;
        r0 = r41;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.sharedUser;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 != 0) goto L_0x0575;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x053c:
        r0 = r41;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.signatures;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.mSignatures;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r5.mSignatures;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r49;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.isSystemSignatureUpdated(r4, r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 == 0) goto L_0x0575;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x054c:
        r4 = "PackageManager";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = new java.lang.StringBuilder;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7.<init>();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = r5.packageName;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = " system signature updated";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.toString();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        android.util.Slog.d(r4, r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r41;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.signatures;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r5.mSignatures;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4.mSignatures = r7;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        goto L_0x0416;
    L_0x0572:
        r4 = move-exception;
        monitor-exit(r6);
        throw r4;
    L_0x0575:
        r0 = r20;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.error;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r20.getMessage();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r51;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0.setError(r4, r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        monitor-exit(r6);
        return;
    L_0x0584:
        r20 = move-exception;
        r0 = r20;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.error;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r20.getMessage();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r51;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0.setError(r4, r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        monitor-exit(r6);
        return;
    L_0x0594:
        r45 = 0;
        goto L_0x046c;
    L_0x0598:
        r4 = r14.packageSetting;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.signatures;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.mSignatures;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r5.mSignatures;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = compareSignatures(r4, r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 != 0) goto L_0x05aa;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x05a6:
        r44 = 1;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        goto L_0x04cd;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x05aa:
        r44 = 0;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        goto L_0x04cd;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x05ae:
        r4 = "PackageManager";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = new java.lang.StringBuilder;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7.<init>();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = "Package ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = r5.packageName;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = " attempting to redeclare permission ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r37;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = r0.info;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = r8.name;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = " already owned by ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = r14.sourcePackage;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = "; ignoring new declaration";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.toString();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        android.util.Slog.w(r4, r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r5.permissions;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r25;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4.remove(r0);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x05f6:
        r25 = r25 + -1;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        goto L_0x0485;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x05fa:
        r4 = "PackageManager";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = new java.lang.StringBuilder;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7.<init>();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = "Package ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = r5.packageName;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = " attempting to redeclare system permission ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r37;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = r0.info;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = r8.name;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = "; ignoring new declaration";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.toString();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        android.util.Slog.w(r4, r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r5.permissions;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r25;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4.remove(r0);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        goto L_0x05f6;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x0636:
        r4 = "android";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r5.packageName;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.equals(r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 != 0) goto L_0x05f6;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x0641:
        r0 = r37;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.info;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4.protectionLevel;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r4 & 15;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = 1;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 != r7) goto L_0x05f6;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x064c:
        if (r14 == 0) goto L_0x05f6;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x064e:
        r4 = r14.isRuntime();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        if (r4 != 0) goto L_0x05f6;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
    L_0x0654:
        r4 = "PackageManager";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = new java.lang.StringBuilder;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7.<init>();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = "Package ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = r5.packageName;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = " trying to change a ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = "non-runtime permission ";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r37;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = r0.info;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = r8.name;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r8 = " to runtime; keeping old protection level";	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r7.toString();	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        android.util.Slog.w(r4, r7);	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r0 = r37;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4 = r0.info;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r7 = r14.protectionLevel;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        r4.protectionLevel = r7;	 Catch:{ PackageManagerException -> 0x0535, all -> 0x0572 }
        goto L_0x05f6;
    L_0x0699:
        monitor-exit(r6);
        if (r45 == 0) goto L_0x06b6;
    L_0x069c:
        if (r35 == 0) goto L_0x06a9;
    L_0x069e:
        r4 = "Cannot install updates to system apps on sdcard";
        r6 = -19;
        r0 = r51;
        r0.setError(r6, r4);
        return;
    L_0x06a9:
        if (r21 == 0) goto L_0x06b6;
    L_0x06ab:
        r4 = "Cannot update a system app with an ephemeral app";
        r6 = -116; // 0xffffffffffffff8c float:NaN double:NaN;
        r0 = r51;
        r0.setError(r6, r4);
        return;
    L_0x06b6:
        r0 = r50;
        r4 = r0.move;
        if (r4 == 0) goto L_0x0743;
    L_0x06bc:
        r43 = r43 | 2;
        r0 = r43;
        r0 = r0 | 8192;
        r43 = r0;
        r0 = r49;
        r6 = r0.mPackages;
        monitor-enter(r6);
        r0 = r49;	 Catch:{ all -> 0x0740 }
        r4 = r0.mSettings;	 Catch:{ all -> 0x0740 }
        r4 = r4.mPackages;	 Catch:{ all -> 0x0740 }
        r0 = r38;	 Catch:{ all -> 0x0740 }
        r41 = r4.get(r0);	 Catch:{ all -> 0x0740 }
        r41 = (com.android.server.pm.PackageSetting) r41;	 Catch:{ all -> 0x0740 }
        if (r41 != 0) goto L_0x06f6;	 Catch:{ all -> 0x0740 }
    L_0x06d9:
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0740 }
        r4.<init>();	 Catch:{ all -> 0x0740 }
        r7 = "Missing settings for moved package ";	 Catch:{ all -> 0x0740 }
        r4 = r4.append(r7);	 Catch:{ all -> 0x0740 }
        r0 = r38;	 Catch:{ all -> 0x0740 }
        r4 = r4.append(r0);	 Catch:{ all -> 0x0740 }
        r4 = r4.toString();	 Catch:{ all -> 0x0740 }
        r7 = -110; // 0xffffffffffffff92 float:NaN double:NaN;	 Catch:{ all -> 0x0740 }
        r0 = r51;	 Catch:{ all -> 0x0740 }
        r0.setError(r7, r4);	 Catch:{ all -> 0x0740 }
    L_0x06f6:
        r4 = r5.applicationInfo;	 Catch:{ all -> 0x0740 }
        r0 = r41;	 Catch:{ all -> 0x0740 }
        r7 = r0.primaryCpuAbiString;	 Catch:{ all -> 0x0740 }
        r4.primaryCpuAbi = r7;	 Catch:{ all -> 0x0740 }
        r4 = r5.applicationInfo;	 Catch:{ all -> 0x0740 }
        r0 = r41;	 Catch:{ all -> 0x0740 }
        r7 = r0.secondaryCpuAbiString;	 Catch:{ all -> 0x0740 }
        r4.secondaryCpuAbi = r7;	 Catch:{ all -> 0x0740 }
        monitor-exit(r6);
    L_0x0707:
        r0 = r49;
        r4 = r0.mCustPms;
        if (r4 == 0) goto L_0x0728;
    L_0x070d:
        r0 = r49;
        r4 = r0.mCustPms;
        r4 = r4.needDerivePkgAbi(r5);
        if (r4 == 0) goto L_0x0728;
    L_0x0717:
        r4 = new java.io.File;	 Catch:{ PackageManagerException -> 0x07d7 }
        r6 = r5.codePath;	 Catch:{ PackageManagerException -> 0x07d7 }
        r4.<init>(r6);	 Catch:{ PackageManagerException -> 0x07d7 }
        r0 = r50;	 Catch:{ PackageManagerException -> 0x07d7 }
        r6 = r0.abiOverride;	 Catch:{ PackageManagerException -> 0x07d7 }
        r7 = 1;	 Catch:{ PackageManagerException -> 0x07d7 }
        r0 = r49;	 Catch:{ PackageManagerException -> 0x07d7 }
        r0.derivePackageAbi(r5, r4, r6, r7);	 Catch:{ PackageManagerException -> 0x07d7 }
    L_0x0728:
        r0 = r51;
        r4 = r0.returnCode;
        r0 = r50;
        r1 = r31;
        r4 = r0.doRename(r4, r5, r1);
        if (r4 != 0) goto L_0x07ee;
    L_0x0736:
        r4 = "Failed rename";
        r6 = -4;
        r0 = r51;
        r0.setError(r6, r4);
        return;
    L_0x0740:
        r4 = move-exception;
        monitor-exit(r6);
        throw r4;
    L_0x0743:
        if (r23 != 0) goto L_0x0707;
    L_0x0745:
        r4 = r5.applicationInfo;
        r4 = r4.isExternalAsec();
        if (r4 != 0) goto L_0x0707;
    L_0x074d:
        r43 = r43 | 2;
        r4 = r5.cpuAbiOverride;	 Catch:{ PackageManagerException -> 0x079d }
        r4 = android.text.TextUtils.isEmpty(r4);	 Catch:{ PackageManagerException -> 0x079d }
        if (r4 == 0) goto L_0x079a;	 Catch:{ PackageManagerException -> 0x079d }
    L_0x0757:
        r0 = r50;	 Catch:{ PackageManagerException -> 0x079d }
        r13 = r0.abiOverride;	 Catch:{ PackageManagerException -> 0x079d }
    L_0x075b:
        r4 = new java.io.File;	 Catch:{ PackageManagerException -> 0x079d }
        r6 = r5.codePath;	 Catch:{ PackageManagerException -> 0x079d }
        r4.<init>(r6);	 Catch:{ PackageManagerException -> 0x079d }
        r6 = 1;	 Catch:{ PackageManagerException -> 0x079d }
        r0 = r49;	 Catch:{ PackageManagerException -> 0x079d }
        r0.derivePackageAbi(r5, r4, r13, r6);	 Catch:{ PackageManagerException -> 0x079d }
        r0 = r49;
        r6 = r0.mPackages;
        monitor-enter(r6);
        r4 = 0;
        r0 = r49;	 Catch:{ PackageManagerException -> 0x07b4, all -> 0x07d4 }
        r0.updateSharedLibrariesLPw(r5, r4);	 Catch:{ PackageManagerException -> 0x07b4, all -> 0x07d4 }
    L_0x0773:
        monitor-exit(r6);
        r4 = "dexopt";
        r6 = 262144; // 0x40000 float:3.67342E-40 double:1.295163E-318;
        android.os.Trace.traceBegin(r6, r4);
        r0 = r49;
        r4 = r0.mPackageDexOptimizer;
        r6 = r5.usesLibraryFiles;
        r7 = 2;
        r9 = com.android.server.pm.PackageManagerServiceCompilerMapping.getCompilerFilterForReason(r7);
        r7 = 0;
        r8 = 0;
        r4.performDexOpt(r5, r6, r7, r8, r9);
        r6 = 262144; // 0x40000 float:3.67342E-40 double:1.295163E-318;
        android.os.Trace.traceEnd(r6);
        r4 = r5.packageName;
        com.android.server.pm.BackgroundDexOptService.notifyPackageChanged(r4);
        goto L_0x0707;
    L_0x079a:
        r13 = r5.cpuAbiOverride;	 Catch:{ PackageManagerException -> 0x079d }
        goto L_0x075b;
    L_0x079d:
        r39 = move-exception;
        r4 = "PackageManager";
        r6 = "Error deriving application ABI";
        r0 = r39;
        android.util.Slog.e(r4, r6, r0);
        r4 = "Error deriving application ABI";
        r6 = -110; // 0xffffffffffffff92 float:NaN double:NaN;
        r0 = r51;
        r0.setError(r6, r4);
        return;
    L_0x07b4:
        r20 = move-exception;
        r4 = "PackageManager";	 Catch:{ PackageManagerException -> 0x07b4, all -> 0x07d4 }
        r7 = new java.lang.StringBuilder;	 Catch:{ PackageManagerException -> 0x07b4, all -> 0x07d4 }
        r7.<init>();	 Catch:{ PackageManagerException -> 0x07b4, all -> 0x07d4 }
        r8 = "updateSharedLibrariesLPw failed: ";	 Catch:{ PackageManagerException -> 0x07b4, all -> 0x07d4 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x07b4, all -> 0x07d4 }
        r8 = r20.getMessage();	 Catch:{ PackageManagerException -> 0x07b4, all -> 0x07d4 }
        r7 = r7.append(r8);	 Catch:{ PackageManagerException -> 0x07b4, all -> 0x07d4 }
        r7 = r7.toString();	 Catch:{ PackageManagerException -> 0x07b4, all -> 0x07d4 }
        android.util.Slog.e(r4, r7);	 Catch:{ PackageManagerException -> 0x07b4, all -> 0x07d4 }
        goto L_0x0773;
    L_0x07d4:
        r4 = move-exception;
        monitor-exit(r6);
        throw r4;
    L_0x07d7:
        r39 = move-exception;
        r4 = "PackageManager";
        r6 = "Error deriving application ABI install app to sdcard";
        r0 = r39;
        android.util.Slog.e(r4, r6, r0);
        r4 = "Error deriving application ABI";
        r6 = -110; // 0xffffffffffffff92 float:NaN double:NaN;
        r0 = r51;
        r0.setError(r6, r4);
        return;
    L_0x07ee:
        r0 = r50;
        r4 = r0.user;
        r4 = r4.getIdentifier();
        r0 = r49;
        r1 = r42;
        r0.startIntentFilterVerifications(r4, r1, r5);
        r47 = 0;
        r24 = 0;
        r4 = "installPackageLI";	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        r0 = r49;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        r1 = r38;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        r2 = r28;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        r24 = r0.freezePackageForInstall(r1, r2, r4);	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        if (r42 == 0) goto L_0x082f;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
    L_0x0810:
        r0 = r43;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        r4 = r0 | 2048;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        r6 = 262144; // 0x40000 float:3.67342E-40 double:1.295163E-318;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        r7 = r4 | r6;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        r0 = r50;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        r8 = r0.user;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        r4 = r49;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        r6 = r36;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        r9 = r29;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        r10 = r51;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        r4.replacePackageLIF(r5, r6, r7, r8, r9, r10);	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
    L_0x0827:
        if (r24 == 0) goto L_0x082c;
    L_0x0829:
        r24.close();	 Catch:{ Throwable -> 0x0853 }
    L_0x082c:
        if (r47 == 0) goto L_0x0861;
    L_0x082e:
        throw r47;
    L_0x082f:
        r0 = r43;
        r7 = r0 | 1024;
        r0 = r50;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        r8 = r0.user;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        r4 = r49;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        r6 = r36;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        r9 = r29;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        r11 = r51;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        r4.installNewPackageLIF(r5, r6, r7, r8, r9, r10, r11);	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
        goto L_0x0827;
    L_0x0843:
        r4 = move-exception;
        throw r4;	 Catch:{ Throwable -> 0x0843, all -> 0x08f7, all -> 0x0845 }
    L_0x0845:
        r6 = move-exception;
        r48 = r6;
        r6 = r4;
        r4 = r48;
    L_0x084b:
        if (r24 == 0) goto L_0x0850;
    L_0x084d:
        r24.close();	 Catch:{ Throwable -> 0x0855 }
    L_0x0850:
        if (r6 == 0) goto L_0x0860;
    L_0x0852:
        throw r6;
    L_0x0853:
        r47 = move-exception;
        goto L_0x082c;
    L_0x0855:
        r7 = move-exception;
        if (r6 != 0) goto L_0x085a;
    L_0x0858:
        r6 = r7;
        goto L_0x0850;
    L_0x085a:
        if (r6 == r7) goto L_0x0850;
    L_0x085c:
        r6.addSuppressed(r7);
        goto L_0x0850;
    L_0x0860:
        throw r4;
    L_0x0861:
        r0 = r49;
        r6 = r0.mPackages;
        monitor-enter(r6);
        r0 = r49;	 Catch:{ all -> 0x08f4 }
        r4 = r0.mSettings;	 Catch:{ all -> 0x08f4 }
        r4 = r4.mPackages;	 Catch:{ all -> 0x08f4 }
        r0 = r38;	 Catch:{ all -> 0x08f4 }
        r41 = r4.get(r0);	 Catch:{ all -> 0x08f4 }
        r41 = (com.android.server.pm.PackageSetting) r41;	 Catch:{ all -> 0x08f4 }
        if (r41 == 0) goto L_0x0887;	 Catch:{ all -> 0x08f4 }
    L_0x0876:
        r4 = sUserManager;	 Catch:{ all -> 0x08f4 }
        r4 = r4.getUserIds();	 Catch:{ all -> 0x08f4 }
        r7 = 1;	 Catch:{ all -> 0x08f4 }
        r0 = r41;	 Catch:{ all -> 0x08f4 }
        r4 = r0.queryInstalledUsers(r4, r7);	 Catch:{ all -> 0x08f4 }
        r0 = r51;	 Catch:{ all -> 0x08f4 }
        r0.newUsers = r4;	 Catch:{ all -> 0x08f4 }
    L_0x0887:
        r0 = r51;	 Catch:{ all -> 0x08f4 }
        r4 = r0.returnCode;	 Catch:{ all -> 0x08f4 }
        r7 = 1;	 Catch:{ all -> 0x08f4 }
        if (r4 != r7) goto L_0x0894;	 Catch:{ all -> 0x08f4 }
    L_0x088e:
        r4 = 0;	 Catch:{ all -> 0x08f4 }
        r0 = r49;	 Catch:{ all -> 0x08f4 }
        r0.writeCertCompatPackages(r4);	 Catch:{ all -> 0x08f4 }
    L_0x0894:
        r4 = r5.childPackages;	 Catch:{ all -> 0x08f4 }
        if (r4 == 0) goto L_0x08de;	 Catch:{ all -> 0x08f4 }
    L_0x0898:
        r4 = r5.childPackages;	 Catch:{ all -> 0x08f4 }
        r15 = r4.size();	 Catch:{ all -> 0x08f4 }
    L_0x089e:
        r25 = 0;	 Catch:{ all -> 0x08f4 }
    L_0x08a0:
        r0 = r25;	 Catch:{ all -> 0x08f4 }
        if (r0 >= r15) goto L_0x08e0;	 Catch:{ all -> 0x08f4 }
    L_0x08a4:
        r4 = r5.childPackages;	 Catch:{ all -> 0x08f4 }
        r0 = r25;	 Catch:{ all -> 0x08f4 }
        r16 = r4.get(r0);	 Catch:{ all -> 0x08f4 }
        r16 = (android.content.pm.PackageParser.Package) r16;	 Catch:{ all -> 0x08f4 }
        r0 = r51;	 Catch:{ all -> 0x08f4 }
        r4 = r0.addedChildPackages;	 Catch:{ all -> 0x08f4 }
        r0 = r16;	 Catch:{ all -> 0x08f4 }
        r7 = r0.packageName;	 Catch:{ all -> 0x08f4 }
        r18 = r4.get(r7);	 Catch:{ all -> 0x08f4 }
        r18 = (com.android.server.pm.PackageManagerService.PackageInstalledInfo) r18;	 Catch:{ all -> 0x08f4 }
        r0 = r49;	 Catch:{ all -> 0x08f4 }
        r4 = r0.mSettings;	 Catch:{ all -> 0x08f4 }
        r0 = r16;	 Catch:{ all -> 0x08f4 }
        r7 = r0.packageName;	 Catch:{ all -> 0x08f4 }
        r17 = r4.peekPackageLPr(r7);	 Catch:{ all -> 0x08f4 }
        if (r17 == 0) goto L_0x08db;	 Catch:{ all -> 0x08f4 }
    L_0x08ca:
        r4 = sUserManager;	 Catch:{ all -> 0x08f4 }
        r4 = r4.getUserIds();	 Catch:{ all -> 0x08f4 }
        r7 = 1;	 Catch:{ all -> 0x08f4 }
        r0 = r17;	 Catch:{ all -> 0x08f4 }
        r4 = r0.queryInstalledUsers(r4, r7);	 Catch:{ all -> 0x08f4 }
        r0 = r18;	 Catch:{ all -> 0x08f4 }
        r0.newUsers = r4;	 Catch:{ all -> 0x08f4 }
    L_0x08db:
        r25 = r25 + 1;
        goto L_0x08a0;
    L_0x08de:
        r15 = 0;
        goto L_0x089e;
    L_0x08e0:
        monitor-exit(r6);
        r0 = r51;
        r4 = r0.returnCode;
        r6 = 1;
        if (r4 != r6) goto L_0x08f3;
    L_0x08e8:
        r4 = r5.packageName;
        r0 = r49;
        r1 = r26;
        r3 = r28;
        r0.recordInstallAppInfo(r4, r1, r3);
    L_0x08f3:
        return;
    L_0x08f4:
        r4 = move-exception;
        monitor-exit(r6);
        throw r4;
    L_0x08f7:
        r4 = move-exception;
        r6 = r47;
        goto L_0x084b;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.PackageManagerService.installPackageLI(com.android.server.pm.PackageManagerService$InstallArgs, com.android.server.pm.PackageManagerService$PackageInstalledInfo):void");
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
        sBrowserIntent.setAction("android.intent.action.VIEW");
        sBrowserIntent.addCategory("android.intent.category.BROWSABLE");
        sBrowserIntent.setData(Uri.parse("http:"));
        PROTECTED_ACTIONS.add("android.intent.action.SEND");
        PROTECTED_ACTIONS.add("android.intent.action.SENDTO");
        PROTECTED_ACTIONS.add("android.intent.action.SEND_MULTIPLE");
        PROTECTED_ACTIONS.add("android.intent.action.VIEW");
    }

    private static boolean hasValidDomains(ActivityIntentInfo filter) {
        if (!filter.hasCategory("android.intent.category.BROWSABLE")) {
            return false;
        }
        if (filter.hasDataScheme("http")) {
            return true;
        }
        return filter.hasDataScheme("https");
    }

    private void handlePackagePostInstall(PackageInstalledInfo res, boolean grantPermissions, boolean killApp, String[] grantedPermissions, boolean launchedForRestore, String installerPackage, IPackageInstallObserver2 installObserver) {
        if (res.returnCode == 1) {
            if (res.removedInfo != null) {
                res.removedInfo.sendPackageRemovedBroadcasts(killApp);
            }
            if (grantPermissions && res.pkg.applicationInfo.targetSdkVersion >= 23) {
                grantRequestedRuntimePermissions(res.pkg, res.newUsers, grantedPermissions);
            }
            boolean update = res.removedInfo != null ? res.removedInfo.removedPackage != null : false;
            if (res.pkg.parentPackage != null) {
                synchronized (this.mPackages) {
                    grantRuntimePermissionsGrantedToDisabledPrivSysPackageParentLPw(res.pkg);
                }
            }
            synchronized (this.mPackages) {
                this.mEphemeralApplicationRegistry.onPackageInstalledLPw(res.pkg);
            }
            String packageName = res.pkg.applicationInfo.packageName;
            Bundle extras = new Bundle(1);
            extras.putInt("android.intent.extra.UID", res.uid);
            int[] firstUsers = EMPTY_INT_ARRAY;
            int[] updateUsers = EMPTY_INT_ARRAY;
            if (res.origUsers == null || res.origUsers.length == 0) {
                firstUsers = res.newUsers;
            } else {
                for (int newUser : res.newUsers) {
                    boolean isNew = true;
                    for (int i : res.origUsers) {
                        if (i == newUser) {
                            isNew = false;
                            break;
                        }
                    }
                    if (isNew) {
                        firstUsers = ArrayUtils.appendInt(firstUsers, newUser);
                    } else {
                        updateUsers = ArrayUtils.appendInt(updateUsers, newUser);
                    }
                }
            }
            setNeedClearDeviceForCTS(true, packageName);
            try {
                updatePackageBlackListInfo(packageName);
            } catch (Exception e) {
                Slog.e(TAG, "update BlackList info failed");
            }
            if (!isEphemeral(res.pkg)) {
                this.mProcessLoggingHandler.invalidateProcessLoggingBaseApkHash(res.pkg.baseCodePath);
                sendIncompatibleNotificationIfNeeded(packageName);
                sendPackageBroadcast("android.intent.action.PACKAGE_ADDED", packageName, extras, 0, null, null, firstUsers);
                if (update) {
                    extras.putBoolean("android.intent.extra.REPLACING", true);
                }
                sendPackageBroadcast("android.intent.action.PACKAGE_ADDED", packageName, extras, 0, null, null, updateUsers);
                if (update) {
                    sendPackageBroadcast("android.intent.action.PACKAGE_REPLACED", packageName, extras, 0, null, null, updateUsers);
                    sendPackageBroadcast("android.intent.action.MY_PACKAGE_REPLACED", null, null, 0, packageName, null, updateUsers);
                } else if (launchedForRestore && !isSystemApp(res.pkg)) {
                    sendFirstLaunchBroadcast(packageName, installerPackage, firstUsers);
                }
                if (res.pkg.isForwardLocked() || isExternal(res.pkg)) {
                    int[] uidArray = new int[]{res.pkg.applicationInfo.uid};
                    ArrayList pkgList = new ArrayList(1);
                    pkgList.add(packageName);
                    sendResourcesChangedBroadcast(true, true, pkgList, uidArray, null);
                }
            }
            if (firstUsers != null && firstUsers.length > 0) {
                synchronized (this.mPackages) {
                    for (int userId : firstUsers) {
                        if (packageIsBrowser(packageName, userId)) {
                            this.mSettings.setDefaultBrowserPackageNameLPw(null, userId);
                        }
                        this.mSettings.applyPendingPermissionGrantsLPw(packageName, userId);
                    }
                }
            }
            EventLog.writeEvent(EventLogTags.UNKNOWN_SOURCES_ENABLED, getUnknownSourcesSettings());
            Runtime.getRuntime().gc();
            if (!(res.removedInfo == null || res.removedInfo.args == null)) {
                synchronized (this.mInstallLock) {
                    res.removedInfo.args.doPostDeleteLI(true);
                }
            }
        }
        if (installObserver != null) {
            try {
                installObserver.onPackageInstalled(res.name, res.returnCode, res.returnMsg, extrasForInstallResult(res));
            } catch (RemoteException e2) {
                Slog.i(TAG, "Observer no longer exists.");
            }
        }
    }

    private void grantRuntimePermissionsGrantedToDisabledPrivSysPackageParentLPw(Package pkg) {
        if (pkg.parentPackage != null && pkg.requestedPermissions != null) {
            PackageSetting disabledSysParentPs = this.mSettings.getDisabledSystemPkgLPr(pkg.parentPackage.packageName);
            if (disabledSysParentPs != null && disabledSysParentPs.pkg != null && disabledSysParentPs.isPrivileged() && (disabledSysParentPs.childPackageNames == null || disabledSysParentPs.childPackageNames.isEmpty())) {
                int[] allUserIds = sUserManager.getUserIds();
                int permCount = pkg.requestedPermissions.size();
                for (int i = 0; i < permCount; i++) {
                    String permission = (String) pkg.requestedPermissions.get(i);
                    BasePermission bp = (BasePermission) this.mSettings.mPermissions.get(permission);
                    if (bp != null && (bp.isRuntime() || bp.isDevelopment())) {
                        for (int userId : allUserIds) {
                            if (disabledSysParentPs.getPermissionsState().hasRuntimePermission(permission, userId)) {
                                grantRuntimePermission(pkg.packageName, permission, userId);
                            }
                        }
                    }
                }
            }
        }
    }

    public void setNeedClearDeviceForCTS(boolean needvalue, String packageName) {
        if (packageName == null) {
            this.mNeedClearDeviceForCTS = false;
        } else if (packageName.equals(PACKAGE_NAME_BASICADMINRECEIVER_CTS_DEIVCEOWNER) || packageName.equals(PACKAGE_NAME_BASICADMINRECEIVER_CTS_DEVICEANDPROFILEOWNER) || packageName.equals(PACKAGE_NAME_BASICADMINRECEIVER_CTS_PACKAGEINSTALLER)) {
            this.mNeedClearDeviceForCTS = needvalue;
            Log.d(TAG, "setmNeedClearDeviceForCTS:" + this.mNeedClearDeviceForCTS);
        } else {
            this.mNeedClearDeviceForCTS = false;
        }
    }

    public boolean getNeedClearDeviceForCTS() {
        return this.mNeedClearDeviceForCTS;
    }

    private void grantRequestedRuntimePermissions(Package pkg, int[] userIds, String[] grantedPermissions) {
        for (int userId : userIds) {
            grantRequestedRuntimePermissionsForUser(pkg, userId, grantedPermissions);
        }
        synchronized (this.mPackages) {
            this.mSettings.writePackageListLPr();
        }
    }

    private void grantRequestedRuntimePermissionsForUser(Package pkg, int userId, String[] grantedPermissions) {
        SettingBase sb = pkg.mExtras;
        if (sb != null) {
            PermissionsState permissionsState = sb.getPermissionsState();
            for (String permission : pkg.requestedPermissions) {
                BasePermission bp;
                synchronized (this.mPackages) {
                    bp = (BasePermission) this.mSettings.mPermissions.get(permission);
                }
                if (bp != null && ((bp.isRuntime() || bp.isDevelopment()) && ((grantedPermissions == null || ArrayUtils.contains(grantedPermissions, permission)) && (permissionsState.getPermissionFlags(permission, userId) & 20) == 0))) {
                    grantRuntimePermission(pkg.packageName, permission, userId);
                }
            }
        }
    }

    Bundle extrasForInstallResult(PackageInstalledInfo res) {
        boolean z = false;
        Bundle extras;
        switch (res.returnCode) {
            case -112:
                extras = new Bundle();
                extras.putString("android.content.pm.extra.FAILURE_EXISTING_PERMISSION", res.origPermission);
                extras.putString("android.content.pm.extra.FAILURE_EXISTING_PACKAGE", res.origPackage);
                return extras;
            case 1:
                extras = new Bundle();
                String str = "android.intent.extra.REPLACING";
                if (!(res.removedInfo == null || res.removedInfo.removedPackage == null)) {
                    z = true;
                }
                extras.putBoolean(str, z);
                return extras;
            default:
                return null;
        }
    }

    void scheduleWriteSettingsLocked() {
        if (!this.mHandler.hasMessages(13)) {
            this.mHandler.sendEmptyMessageDelayed(13, 10000);
        }
    }

    void scheduleWritePackageListLocked(int userId) {
        if (!this.mHandler.hasMessages(19)) {
            Message msg = this.mHandler.obtainMessage(19);
            msg.arg1 = userId;
            this.mHandler.sendMessageDelayed(msg, 10000);
        }
    }

    void scheduleWritePackageRestrictionsLocked(UserHandle user) {
        scheduleWritePackageRestrictionsLocked(user == null ? -1 : user.getIdentifier());
    }

    void scheduleWritePackageRestrictionsLocked(int userId) {
        int i = 0;
        int[] userIds = userId == -1 ? sUserManager.getUserIds() : new int[]{userId};
        int length = userIds.length;
        while (i < length) {
            int nextUserId = userIds[i];
            if (sUserManager.exists(nextUserId)) {
                this.mDirtyUsers.add(Integer.valueOf(nextUserId));
                if (!this.mHandler.hasMessages(14)) {
                    this.mHandler.sendEmptyMessageDelayed(14, 10000);
                }
                i++;
            } else {
                return;
            }
        }
    }

    public static PackageManagerService main(Context context, Installer installer, boolean factoryTest, boolean onlyCore) {
        long startTime = SystemClock.uptimeMillis();
        PackageManagerServiceCompilerMapping.checkProperties();
        PackageManagerService m = HwServiceFactory.getHuaweiPackageManagerService(context, installer, factoryTest, onlyCore);
        m.enableSystemUserPackages();
        CarrierAppUtils.disableCarrierAppsUntilPrivileged(context.getOpPackageName(), m, 0);
        ServiceManager.addService(HwBroadcastRadarUtil.KEY_PACKAGE, m);
        Slog.i(TAG, "PackageManagerService booting timestamp : " + (SystemClock.uptimeMillis() - startTime) + " ms");
        return m;
    }

    private void enableSystemUserPackages() {
        if (UserManager.isSplitSystemUser()) {
            AppsQueryHelper queryHelper = new AppsQueryHelper(this);
            Set<String> enableApps = new ArraySet();
            enableApps.addAll(queryHelper.queryApps((AppsQueryHelper.GET_NON_LAUNCHABLE_APPS | AppsQueryHelper.GET_APPS_WITH_INTERACT_ACROSS_USERS_PERM) | AppsQueryHelper.GET_IMES, true, UserHandle.SYSTEM));
            enableApps.addAll(SystemConfig.getInstance().getSystemUserWhitelistedApps());
            enableApps.addAll(queryHelper.queryApps(AppsQueryHelper.GET_REQUIRED_FOR_SYSTEM_USER, false, UserHandle.SYSTEM));
            enableApps.removeAll(SystemConfig.getInstance().getSystemUserBlacklistedApps());
            Log.i(TAG, "Applications installed for system user: " + enableApps);
            List<String> allAps = queryHelper.queryApps(0, false, UserHandle.SYSTEM);
            int allAppsSize = allAps.size();
            synchronized (this.mPackages) {
                for (int i = 0; i < allAppsSize; i++) {
                    String pName = (String) allAps.get(i);
                    PackageSetting pkgSetting = (PackageSetting) this.mSettings.mPackages.get(pName);
                    if (pkgSetting != null) {
                        boolean install = enableApps.contains(pName);
                        if (pkgSetting.getInstalled(0) != install) {
                            Log.i(TAG, (install ? "Installing " : "Uninstalling ") + pName + " for system user");
                            pkgSetting.setInstalled(install, 0);
                        } else {
                            continue;
                        }
                    }
                }
            }
        }
    }

    private static void getDefaultDisplayMetrics(Context context, DisplayMetrics metrics) {
        ((DisplayManager) context.getSystemService("display")).getDisplay(0).getMetrics(metrics);
    }

    public PackageManagerService(Context context, Installer installer, boolean factoryTest, boolean onlyCore) {
        File file;
        PackageSetting ps;
        PackageManagerService packageManagerService = this;
        this.mServices = new ServiceIntentResolver();
        packageManagerService = this;
        this.mProviders = new ProviderIntentResolver();
        this.mProvidersByAuthority = new ArrayMap();
        this.mInstrumentation = new ArrayMap();
        this.mPermissionGroups = new ArrayMap();
        this.mTransferedPackages = new ArraySet();
        this.mProtectedBroadcasts = new ArraySet();
        this.mPendingVerification = new SparseArray();
        this.mAppOpPermissionPackages = new ArrayMap();
        this.mNextMoveId = new AtomicInteger();
        this.mUserNeedsBadging = new SparseBooleanArray();
        this.mPendingVerificationToken = 0;
        this.mIsPackageScanMultiThread = SystemProperties.getBoolean("ro.config.hw_packagescan_multi", true);
        this.mResolveActivity = new ActivityInfo();
        this.mResolveInfo = new ResolveInfo();
        this.mResolverReplaced = false;
        this.mExecutorService = Executors.newSingleThreadExecutor();
        this.clearDirectoryThread = Executors.newSingleThreadExecutor();
        this.mIntentFilterVerificationToken = 0;
        this.mEphemeralInstallerActivity = new ActivityInfo();
        this.mEphemeralInstallerInfo = new ResolveInfo();
        this.mIntentFilterVerificationStates = new SparseArray();
        this.mDefaultPermissionPolicy = null;
        this.mPendingBroadcasts = new PendingPackageBroadcasts();
        this.mContainerService = null;
        this.mDirtyUsers = new ArraySet();
        this.mDefContainerConn = new DefaultContainerConnection();
        this.mRunningInstalls = new SparseArray();
        this.mNextInstallToken = 1;
        packageManagerService = this;
        this.mPackageUsage = new PackageUsage();
        this.mNeedClearDeviceForCTS = false;
        this.mStorageListener = new StorageEventListener() {
            public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
                if (vol.type == 1) {
                    if (vol.state == 2) {
                        String volumeUuid = vol.getFsUuid();
                        PackageManagerService.this.reconcileUsers(volumeUuid);
                        PackageManagerService.this.reconcileApps(volumeUuid);
                        PackageManagerService.this.mInstallerService.onPrivateVolumeMounted(volumeUuid);
                        PackageManagerService.this.loadPrivatePackages(vol);
                    } else if (vol.state == 5) {
                        PackageManagerService.this.unloadPrivatePackages(vol);
                    }
                }
                if ((vol.type == 0 && vol.isPrimary()) || (PackageManagerService.this.mCustPms != null && PackageManagerService.this.mCustPms.isSdVol(vol))) {
                    if (vol.state == 2) {
                        PackageManagerService.this.updateExternalMediaStatus(true, false);
                    } else if (vol.state == 5) {
                        PackageManagerService.this.updateExternalMediaStatus(false, false);
                    }
                }
            }

            public void onVolumeForgotten(String fsUuid) {
                if (TextUtils.isEmpty(fsUuid)) {
                    Slog.e(PackageManagerService.TAG, "Forgetting internal storage is probably a mistake; ignoring");
                    return;
                }
                synchronized (PackageManagerService.this.mPackages) {
                    for (PackageSetting ps : PackageManagerService.this.mSettings.getVolumePackagesLPr(fsUuid)) {
                        Slog.d(PackageManagerService.TAG, "Destroying " + ps.name + " because volume was forgotten");
                        PackageManagerService.this.deletePackage(ps.name, new LegacyPackageDeleteObserver(null).getBinder(), 0, 2);
                    }
                    PackageManagerService.this.mSettings.onVolumeForgotten(fsUuid);
                    PackageManagerService.this.mSettings.writeLPr();
                }
            }
        };
        this.mMediaMounted = false;
        this.mMonitor = HwFrameworkFactory.getHwFrameworkMonitor();
        EventLog.writeEvent(EventLogTags.BOOT_PROGRESS_PMS_START, SystemClock.uptimeMillis());
        Jlog.d(31, "JL_BOOT_PROGRESS_PMS_START");
        if (this.mSdkVersion <= 0) {
            Slog.w(TAG, "**** ro.build.version.sdk not set!");
        }
        this.mContext = context;
        this.mDefaultPermissionPolicy = HwServiceFactory.getHwDefaultPermissionGrantPolicy(this.mContext, this);
        Slog.i(TAG, "mDefaultPermissionPolicy :" + this.mDefaultPermissionPolicy);
        if (HWFLOW) {
            this.startTimer = SystemClock.uptimeMillis();
        }
        this.mFactoryTest = factoryTest;
        this.mOnlyCore = onlyCore;
        this.mMetrics = new DisplayMetrics();
        this.mSettings = new Settings(this.mPackages);
        this.mSettings.addSharedUserLPw("android.uid.system", 1000, 1, 8);
        this.mSettings.addSharedUserLPw("android.uid.phone", RADIO_UID, 1, 8);
        this.mSettings.addSharedUserLPw("android.uid.log", LOG_UID, 1, 8);
        this.mSettings.addSharedUserLPw("android.uid.nfc", NFC_UID, 1, 8);
        this.mSettings.addSharedUserLPw("com.nxp.uid.nfceeapi", SPI_UID, 1, 8);
        this.mSettings.addSharedUserLPw("android.uid.bluetooth", BLUETOOTH_UID, 1, 8);
        this.mSettings.addSharedUserLPw("android.uid.shell", 2000, 1, 8);
        HwServiceFactory.getHwPackageServiceManager().addHwSharedUserLP(this.mSettings);
        String separateProcesses = SystemProperties.get("debug.separate_processes");
        if (separateProcesses == null || separateProcesses.length() <= 0) {
            this.mDefParseFlags = 0;
            this.mSeparateProcesses = null;
        } else if ("*".equals(separateProcesses)) {
            this.mDefParseFlags = 8;
            this.mSeparateProcesses = null;
            Slog.w(TAG, "Running with debug.separate_processes: * (ALL)");
        } else {
            this.mDefParseFlags = 0;
            this.mSeparateProcesses = separateProcesses.split(",");
            Slog.w(TAG, "Running with debug.separate_processes: " + separateProcesses);
        }
        this.mInstaller = installer;
        this.mPackageDexOptimizer = new PackageDexOptimizer(installer, this.mInstallLock, context, "*dexopt*");
        this.mMoveCallbacks = new MoveCallbacks(FgThread.get().getLooper());
        this.mOnPermissionChangeListeners = new OnPermissionChangeListeners(FgThread.get().getLooper());
        getDefaultDisplayMetrics(context, this.mMetrics);
        SystemConfig systemConfig = SystemConfig.getInstance();
        this.mGlobalGids = systemConfig.getGlobalGids();
        this.mSystemPermissions = systemConfig.getSystemPermissions();
        this.mAvailableFeatures = systemConfig.getAvailableFeatures();
        if (HWFLOW) {
            String str = TAG;
            StringBuilder append = new StringBuilder().append("TimerCounter = ");
            int i = this.mTimerCounter + 1;
            this.mTimerCounter = i;
            Slog.i(str, append.append(i).append(" **** Config Init  ************ Time to elapsed: ").append(SystemClock.uptimeMillis() - this.startTimer).append(" ms").toString());
        }
        synchronized (this.mInstallLock) {
            synchronized (this.mPackages) {
                int i2;
                this.mHandlerThread = new ServiceThread(TAG, 10, true);
                this.mHandlerThread.start();
                this.mHandler = new PackageHandler(this.mHandlerThread.getLooper());
                this.mProcessLoggingHandler = new ProcessLoggingHandler();
                Watchdog.getInstance().addThread(this.mHandler, 600000);
                File dataDir = Environment.getDataDirectory();
                this.mAppInstallDir = new File(dataDir, "app");
                this.mAppLib32InstallDir = new File(dataDir, "app-lib");
                this.mEphemeralInstallDir = new File(dataDir, "app-ephemeral");
                this.mAsecInternalPath = new File(dataDir, "app-asec").getPath();
                this.mDrmAppPrivateInstallDir = new File(dataDir, "app-private");
                try {
                    Slog.i(TAG, "UserManagerService");
                    IHwUserManagerService service = HwServiceFactory.getHwUserManagerService();
                    if (service != null) {
                        sUserManager = service.getInstance(context, this, this.mPackages);
                    } else {
                        sUserManager = new UserManagerService(context, this, this.mPackages);
                    }
                } catch (Throwable e) {
                    Slog.e(TAG, "UserManagerService error " + e);
                }
                ArrayMap<String, PermissionEntry> permConfig = systemConfig.getPermissions();
                for (i2 = 0; i2 < permConfig.size(); i2++) {
                    PermissionEntry perm = (PermissionEntry) permConfig.valueAt(i2);
                    BasePermission bp = (BasePermission) this.mSettings.mPermissions.get(perm.name);
                    if (bp == null) {
                        BasePermission basePermission = new BasePermission(perm.name, PLATFORM_PACKAGE_NAME, 1);
                        this.mSettings.mPermissions.put(perm.name, basePermission);
                    }
                    if (perm.gids != null) {
                        bp.setGids(perm.gids, perm.perUser);
                    }
                }
                ArrayMap<String, String> libConfig = systemConfig.getSharedLibraries();
                for (i2 = 0; i2 < libConfig.size(); i2++) {
                    this.mSharedLibraries.put((String) libConfig.keyAt(i2), new SharedLibraryEntry((String) libConfig.valueAt(i2), null));
                }
                this.mFoundPolicyFile = SELinuxMMAC.readInstallPolicy();
                initHwCertificationManager();
                this.mRestoredSettings = this.mSettings.readLPw(sUserManager.getUsers(false));
                readPackagesAbiLPw();
                initCertCompatSettings();
                String customResolverActivity = Resources.getSystem().getString(17039455);
                customResolverActivity = HwFrameworkFactory.getHuaweiResolverActivity(this.mContext);
                if (!TextUtils.isEmpty(customResolverActivity)) {
                    this.mCustomResolverComponentName = ComponentName.unflattenFromString(customResolverActivity);
                }
                long startTime = SystemClock.uptimeMillis();
                EventLog.writeEvent(EventLogTags.BOOT_PROGRESS_PMS_SYSTEM_SCAN_START, startTime);
                String bootClassPath = System.getenv("BOOTCLASSPATH");
                String systemServerClassPath = System.getenv("SYSTEMSERVERCLASSPATH");
                if (bootClassPath == null) {
                    Slog.w(TAG, "No BOOTCLASSPATH found!");
                }
                if (systemServerClassPath == null) {
                    Slog.w(TAG, "No SYSTEMSERVERCLASSPATH found!");
                }
                List<String> allInstructionSets = InstructionSets.getAllInstructionSets();
                String[] dexCodeInstructionSets = InstructionSets.getDexCodeInstructionSets((String[]) allInstructionSets.toArray(new String[allInstructionSets.size()]));
                if (HWFLOW) {
                    this.startTimer = SystemClock.uptimeMillis();
                }
                if (this.mSharedLibraries.size() > 0) {
                    for (String dexCodeInstructionSet : dexCodeInstructionSets) {
                        for (SharedLibraryEntry libEntry : this.mSharedLibraries.values()) {
                            String lib = libEntry.path;
                            if (lib != null) {
                                try {
                                    int dexoptNeeded = DexFile.getDexOptNeeded(lib, dexCodeInstructionSet, PackageManagerServiceCompilerMapping.getCompilerFilterForReason(6), false);
                                    if (dexoptNeeded != 0) {
                                        this.mInstaller.dexopt(lib, 1000, dexCodeInstructionSet, dexoptNeeded, 2, PackageManagerServiceCompilerMapping.getCompilerFilterForReason(6), StorageManager.UUID_PRIVATE_INTERNAL, SKIP_SHARED_LIBRARY_CHECK);
                                    }
                                } catch (FileNotFoundException e2) {
                                    Slog.w(TAG, "Library not found: " + lib);
                                } catch (Exception e3) {
                                    Slog.w(TAG, "Cannot dexopt " + lib + "; is it an APK or JAR? " + e3.getMessage());
                                }
                            }
                        }
                    }
                }
                if (HWFLOW) {
                    str = TAG;
                    append = new StringBuilder().append("TimerCounter = ");
                    i = this.mTimerCounter + 1;
                    this.mTimerCounter = i;
                    Slog.i(str, append.append(i).append(" **** for: SharedLibraryEntry ************ Time to elapsed: ").append(SystemClock.uptimeMillis() - this.startTimer).append(" ms").toString());
                }
                file = new File(Environment.getRootDirectory(), "framework");
            }
        }
        VersionInfo ver = this.mSettings.getInternalVersion();
        boolean z = (ver == null || Build.FINGERPRINT.equals(ver.fingerprint)) ? false : true;
        this.mIsUpgrade = z;
        if (HWFLOW) {
            this.startTimer = SystemClock.uptimeMillis();
        }
        loadSysWhitelist();
        if (HWFLOW) {
            str = TAG;
            append = new StringBuilder().append("TimerCounter = ");
            i = this.mTimerCounter + 1;
            this.mTimerCounter = i;
            Slog.i(str, append.append(i).append(" **** loadSysWhitelist  ************ Time to elapsed: ").append(SystemClock.uptimeMillis() - this.startTimer).append(" ms").toString());
        }
        synchronized (this.mPackages) {
            resetSharedUserSignaturesIfNeeded();
        }
        if (this.mIsUpgrade && !onlyCore) {
            deletePackagesAbiFile();
        }
        z = this.mIsUpgrade && ver.sdkVersion <= 22;
        this.mPromoteSystemApps = z;
        z = this.mIsUpgrade && ver.sdkVersion < 24;
        this.mIsPreNUpgrade = z;
        if (this.mPromoteSystemApps) {
            for (PackageSetting ps2 : this.mSettings.mPackages.values()) {
                if (isSystemApp(ps2)) {
                    this.mExistingSystemPackages.add(ps2.name);
                }
            }
        }
        Iterable fileList = null;
        try {
            fileList = HwCfgFilePolicy.getCfgFileList("/overlay", 1);
        } catch (NoClassDefFoundError er) {
            Slog.e(TAG, er.getMessage());
        }
        if (r50 != null) {
            for (File file2 : r50) {
                scanDirTracedLI(file2, ((this.mDefParseFlags | 1) | 64) | 512, 17312, 0);
            }
        } else {
            scanDirTracedLI(new File(VENDOR_OVERLAY_DIR), ((this.mDefParseFlags | 1) | 64) | 512, 17312, 0);
        }
        if (HWFLOW) {
            this.startTimer = SystemClock.uptimeMillis();
        }
        getUninstallApk();
        if (HWFLOW) {
            str = TAG;
            append = new StringBuilder().append("TimerCounter = ");
            i = this.mTimerCounter + 1;
            this.mTimerCounter = i;
            Slog.i(str, append.append(i).append(" **** getUninstallApk  ************ Time to elapsed: ").append(SystemClock.uptimeMillis() - this.startTimer).append(" ms").toString());
        }
        scanDirTracedLI(file, ((this.mDefParseFlags | 1) | 64) | 128, 16802, 0);
        File privilegedAppDir = new File(Environment.getRootDirectory(), "priv-app");
        scanDirTracedLI(privilegedAppDir, ((this.mDefParseFlags | 1) | 64) | 128, 16800, 0);
        file = new File(Environment.getRootDirectory(), "app");
        scanDirTracedLI(file, (this.mDefParseFlags | 1) | 64, 16800, 0);
        file = new File("/vendor/app");
        try {
            File vendorAppDir = file.getCanonicalFile();
        } catch (IOException e4) {
        }
        if (HWFLOW) {
            this.startTimer = SystemClock.uptimeMillis();
        }
        scanNonSystemPartitionDir(16800);
        if (HWFLOW) {
            str = TAG;
            append = new StringBuilder().append("TimerCounter = ");
            i = this.mTimerCounter + 1;
            this.mTimerCounter = i;
            Slog.i(str, append.append(i).append(" **** scanNonSystemPartitionDir  ************ Time to elapsed: ").append(SystemClock.uptimeMillis() - this.startTimer).append(" ms").toString());
        }
        if (HWFLOW) {
            this.startTimer = SystemClock.uptimeMillis();
        }
        scanRemovableAppDir(16800);
        if (HWFLOW) {
            str = TAG;
            append = new StringBuilder().append("TimerCounter = ");
            i = this.mTimerCounter + 1;
            this.mTimerCounter = i;
            Slog.i(str, append.append(i).append(" **** scanRemovableAppDir  ************ Time to elapsed: ").append(SystemClock.uptimeMillis() - this.startTimer).append(" ms").toString());
        }
        file = new File(Environment.getOemDirectory(), "app");
        scanDirTracedLI(file, (this.mDefParseFlags | 1) | 64, 16800, 0);
        List<String> possiblyDeletedUpdatedSystemApps = new ArrayList();
        synchronized (this.mInstallLock) {
            synchronized (this.mPackages) {
                if (!this.mOnlyCore) {
                    Iterator<PackageSetting> psit = this.mSettings.mPackages.values().iterator();
                    while (psit.hasNext()) {
                        ps2 = (PackageSetting) psit.next();
                        if ((ps2.pkgFlags & 1) != 0) {
                            makeSetupDisabled(ps2.name);
                            Package scannedPkg = (Package) this.mPackages.get(ps2.name);
                            if (scannedPkg != null) {
                                if (this.mSettings.isDisabledSystemPackageLPr(ps2.name)) {
                                    logCriticalInfo(5, "Expecting better updated system app for " + ps2.name + "; removing system app.  Last known codePath=" + ps2.codePathString + ", installStatus=" + ps2.installStatus + ", versionCode=" + ps2.versionCode + "; scanned versionCode=" + scannedPkg.mVersionCode);
                                    removePackageLI(scannedPkg, true);
                                    if (!skipSetupEnable(ps2.name)) {
                                        this.mExpectingBetter.put(ps2.name, ps2.codePath);
                                    }
                                }
                            } else if (this.mSettings.isDisabledSystemPackageLPr(ps2.name)) {
                                PackageSetting disabledPs = this.mSettings.getDisabledSystemPkgLPr(ps2.name);
                                if (disabledPs.codePath == null || !disabledPs.codePath.exists()) {
                                    possiblyDeletedUpdatedSystemApps.add(ps2.name);
                                }
                            } else {
                                psit.remove();
                                logCriticalInfo(5, "System package " + ps2.name + " no longer exists; it's data will be wiped");
                                writeNetQinFlag(ps2.name);
                            }
                        }
                    }
                }
                ArrayList<PackageSetting> deletePkgsList = this.mSettings.getListOfIncompleteInstallPackagesLPr();
                for (i2 = 0; i2 < deletePkgsList.size(); i2++) {
                    String packageName = ((PackageSetting) deletePkgsList.get(i2)).name;
                    logCriticalInfo(5, "Cleaning up incompletely installed app: " + packageName);
                    synchronized (this.mPackages) {
                        this.mSettings.removePackageLPw(packageName);
                    }
                }
                deleteTempPackageFiles();
                this.mSettings.pruneSharedUsersLPw();
            }
        }
        if (!this.mOnlyCore) {
            EventLog.writeEvent(EventLogTags.BOOT_PROGRESS_PMS_DATA_SCAN_START, SystemClock.uptimeMillis());
            scanDirTracedLI(this.mAppInstallDir, 0, 20896, 0);
            scanDirTracedLI(this.mDrmAppPrivateInstallDir, this.mDefParseFlags | 16, 20896, 0);
            if (HWFLOW) {
                this.startTimer = SystemClock.uptimeMillis();
            }
            scanDirLI(this.mEphemeralInstallDir, this.mDefParseFlags | 2048, 20896, 0);
            if (HWFLOW) {
                str = TAG;
                append = new StringBuilder().append("TimerCounter = ");
                i = this.mTimerCounter + 1;
                this.mTimerCounter = i;
                Slog.i(str, append.append(i).append(" **** mEphemeralInstallDir  ************ Time to elapsed: ").append(SystemClock.uptimeMillis() - this.startTimer).append(" ms").toString());
            }
            synchronized (this.mPackages) {
                for (String deletedAppName : possiblyDeletedUpdatedSystemApps) {
                    String msg;
                    Package deletedPkg = (Package) this.mPackages.get(deletedAppName);
                    this.mSettings.removeDisabledSystemPackageLPw(deletedAppName);
                    if (deletedPkg == null) {
                        msg = "Updated system package " + deletedAppName + " no longer exists; it's data will be wiped";
                    } else {
                        msg = "Updated system app + " + deletedAppName + " no longer present; removing system privileges for " + deletedAppName;
                        ApplicationInfo applicationInfo = deletedPkg.applicationInfo;
                        applicationInfo.flags &= -2;
                        PackageSetting deletedPs = (PackageSetting) this.mSettings.mPackages.get(deletedAppName);
                        deletedPs.pkgFlags &= -2;
                    }
                    logCriticalInfo(5, msg);
                }
                for (i2 = 0; i2 < this.mExpectingBetter.size(); i2++) {
                    packageName = (String) this.mExpectingBetter.keyAt(i2);
                    if (!this.mPackages.containsKey(packageName)) {
                        File scanFile = (File) this.mExpectingBetter.valueAt(i2);
                        logCriticalInfo(5, "Expected better " + packageName + " but never showed up; reverting to system");
                        int reparseFlags = this.mDefParseFlags;
                        if (FileUtils.contains(privilegedAppDir, scanFile)) {
                            reparseFlags = HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_PLUS;
                        } else if (FileUtils.contains(file, scanFile)) {
                            reparseFlags = 65;
                        } else if (FileUtils.contains(vendorAppDir, scanFile)) {
                            reparseFlags = 65;
                        } else if (FileUtils.contains(file, scanFile)) {
                            reparseFlags = 65;
                        } else {
                            Slog.e(TAG, "Ignoring unexpected fallback path " + scanFile);
                        }
                        this.mSettings.enableSystemPackageLPw(packageName);
                        try {
                            scanPackageTracedLI(scanFile, reparseFlags, 16800, 0, null);
                        } catch (PackageManagerException e5) {
                            Slog.e(TAG, "Failed to parse original system package: " + e5.getMessage());
                        }
                    }
                }
            }
            this.mExpectingBetter.clear();
        }
        this.mSetupWizardPackage = getSetupWizardPackageName();
        if (this.mProtectedFilters.size() > 0) {
            for (ActivityIntentInfo filter : this.mProtectedFilters) {
                if (!filter.activity.info.packageName.equals(this.mSetupWizardPackage)) {
                    Slog.w(TAG, "Protected action; cap priority to 0; package: " + filter.activity.info.packageName + " activity: " + filter.activity.className + " origPrio: " + filter.getPriority());
                    filter.setPriority(0);
                }
            }
        }
        this.mDeferProtectedFilters = false;
        this.mProtectedFilters.clear();
        synchronized (this.mInstallLock) {
            synchronized (this.mPackages) {
                int storageFlags;
                updateAllSharedLibrariesLPw();
                for (SharedUserSetting setting : this.mSettings.getAllSharedUsersLPw()) {
                    adjustCpuAbisForSharedUserLPw(setting.packages, null, false);
                }
                this.mPackageUsage.readLP();
                EventLog.writeEvent(EventLogTags.BOOT_PROGRESS_PMS_SCAN_END, SystemClock.uptimeMillis());
                Slog.i(TAG, "Time to scan packages: " + (((float) (SystemClock.uptimeMillis() - startTime)) / 1000.0f) + " seconds");
                if (HWFLOW) {
                    this.startTimer = SystemClock.uptimeMillis();
                }
                int updateFlags = 1;
                if (!(ver == null || ver.sdkVersion == this.mSdkVersion)) {
                    Slog.i(TAG, "Platform changed from " + ver.sdkVersion + " to " + this.mSdkVersion + "; regranting permissions for internal storage");
                    updateFlags = 7;
                }
                updatePermissionsLPw(null, null, StorageManager.UUID_PRIVATE_INTERNAL, updateFlags);
                if (ver != null) {
                    ver.sdkVersion = this.mSdkVersion;
                }
                if (!onlyCore && (this.mPromoteSystemApps || !this.mRestoredSettings)) {
                    for (UserInfo user : sUserManager.getUsers(true)) {
                        this.mSettings.applyDefaultPreferredAppsLPw(this, user.id);
                        applyFactoryDefaultBrowserLPw(user.id);
                        primeDomainVerificationsLPw(user.id);
                    }
                }
                if (StorageManager.isFileEncryptedNativeOrEmulated()) {
                    storageFlags = 1;
                } else {
                    storageFlags = 3;
                }
                reconcileAppsDataLI(StorageManager.UUID_PRIVATE_INTERNAL, 0, storageFlags);
                if (this.mIsUpgrade && !onlyCore) {
                    Slog.i(TAG, "Build fingerprint changed; clearing code caches");
                    for (i2 = 0; i2 < this.mSettings.mPackages.size(); i2++) {
                        ps2 = (PackageSetting) this.mSettings.mPackages.valueAt(i2);
                        if (Objects.equals(StorageManager.UUID_PRIVATE_INTERNAL, ps2.volumeUuid)) {
                            clearAppDataLIF(ps2.pkg, -1, 515);
                        }
                    }
                    ver.fingerprint = Build.FINGERPRINT;
                }
                checkDefaultBrowser();
                this.mExistingSystemPackages.clear();
                this.mPromoteSystemApps = false;
                if (ver != null) {
                    ver.databaseVersion = 3;
                }
                if (HWFLOW) {
                    str = TAG;
                    StringBuilder append2 = new StringBuilder().append("TimerCounter = ");
                    int i3 = this.mTimerCounter + 1;
                    this.mTimerCounter = i3;
                    Slog.i(str, append2.append(i3).append(" **** checkDefaultBrowser  ************ Time to elapsed: ").append(SystemClock.uptimeMillis() - this.startTimer).append(" ms").toString());
                }
                if (HWFLOW) {
                    this.startTimer = SystemClock.uptimeMillis();
                }
                this.mSettings.writeLPr();
                if (HWFLOW) {
                    str = TAG;
                    append2 = new StringBuilder().append("TimerCounter = ");
                    i3 = this.mTimerCounter + 1;
                    this.mTimerCounter = i3;
                    Slog.i(str, append2.append(i3).append(" **** mSettings.writeLPr  ************ Time to elapsed: ").append(SystemClock.uptimeMillis() - this.startTimer).append(" ms").toString());
                }
                writePackagesAbi();
                writeCertCompatPackages(true);
                if (HWFLOW) {
                    this.startTimer = SystemClock.uptimeMillis();
                }
                if ((isFirstBoot() || isUpgrade() || VMRuntime.didPruneDalvikCache()) && !onlyCore) {
                    long start = System.nanoTime();
                    List<Package> coreApps = new ArrayList();
                    for (Package pkg : this.mPackages.values()) {
                        if (pkg.coreApp) {
                            coreApps.add(pkg);
                        }
                    }
                    int[] stats = performDexOptUpgrade(coreApps, false, PackageManagerServiceCompilerMapping.getCompilerFilterForReason(8));
                    MetricsLogger.histogram(this.mContext, "opt_coreapps_time_s", (int) TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start));
                }
                if (HWFLOW) {
                    str = TAG;
                    append2 = new StringBuilder().append("TimerCounter = ");
                    i3 = this.mTimerCounter + 1;
                    this.mTimerCounter = i3;
                    Slog.i(str, append2.append(i3).append(" **** performDexOpt  ************ Time to elapsed: ").append(SystemClock.uptimeMillis() - this.startTimer).append(" ms").toString());
                }
                EventLog.writeEvent(EventLogTags.BOOT_PROGRESS_PMS_READY, SystemClock.uptimeMillis());
                Jlog.d(32, "JL_BOOT_PROGRESS_PMS_READY");
                if (this.mOnlyCore) {
                    this.mRequiredVerifierPackage = null;
                    this.mRequiredInstallerPackage = null;
                    this.mIntentFilterVerifierComponent = null;
                    this.mIntentFilterVerifier = null;
                    this.mServicesSystemSharedLibraryPackageName = null;
                    this.mSharedSystemSharedLibraryPackageName = null;
                } else {
                    this.mRequiredVerifierPackage = getRequiredButNotReallyRequiredVerifierLPr();
                    this.mRequiredInstallerPackage = getRequiredInstallerLPr();
                    this.mIntentFilterVerifierComponent = getIntentFilterVerifierComponentNameLPr();
                    this.mIntentFilterVerifier = new IntentVerifierProxy(this.mContext, this.mIntentFilterVerifierComponent);
                    this.mServicesSystemSharedLibraryPackageName = getRequiredSharedLibraryLPr("android.ext.services");
                    this.mSharedSystemSharedLibraryPackageName = getRequiredSharedLibraryLPr("android.ext.shared");
                }
                this.mInstallerService = new PackageInstallerService(context, this);
                ComponentName ephemeralResolverComponent = getEphemeralResolverLPr();
                ComponentName ephemeralInstallerComponent = getEphemeralInstallerLPr();
                if (ephemeralInstallerComponent == null || ephemeralResolverComponent == null) {
                    this.mEphemeralResolverComponent = null;
                    this.mEphemeralInstallerComponent = null;
                    this.mEphemeralResolverConnection = null;
                } else {
                    this.mEphemeralResolverComponent = ephemeralResolverComponent;
                    this.mEphemeralInstallerComponent = ephemeralInstallerComponent;
                    setUpEphemeralInstallerActivityLP(this.mEphemeralInstallerComponent);
                    this.mEphemeralResolverConnection = new EphemeralResolverConnection(this.mContext, this.mEphemeralResolverComponent);
                }
                this.mEphemeralApplicationRegistry = new EphemeralApplicationRegistry(this);
            }
        }
        Runtime.getRuntime().gc();
        this.mInstaller.setWarnIfHeld(this.mPackages);
        packageManagerService = this;
        LocalServices.addService(PackageManagerInternal.class, new PackageManagerInternalImpl());
    }

    public boolean isFirstBoot() {
        return !this.mRestoredSettings;
    }

    public boolean isOnlyCoreApps() {
        return this.mOnlyCore;
    }

    public boolean isUpgrade() {
        return this.mIsUpgrade;
    }

    private String getRequiredButNotReallyRequiredVerifierLPr() {
        List<ResolveInfo> matches = queryIntentReceiversInternal(new Intent("android.intent.action.PACKAGE_NEEDS_VERIFICATION"), PACKAGE_MIME_TYPE, 1835008, 0);
        if (matches.size() == 1) {
            return ((ResolveInfo) matches.get(0)).getComponentInfo().packageName;
        }
        Log.e(TAG, "There should probably be exactly one verifier; found " + matches);
        return null;
    }

    private String getRequiredSharedLibraryLPr(String libraryName) {
        String str;
        synchronized (this.mPackages) {
            SharedLibraryEntry libraryEntry = (SharedLibraryEntry) this.mSharedLibraries.get(libraryName);
            if (libraryEntry == null) {
                throw new IllegalStateException("Missing required shared library:" + libraryName);
            }
            str = libraryEntry.apk;
        }
        return str;
    }

    private String getRequiredInstallerLPr() {
        Intent intent = new Intent("android.intent.action.INSTALL_PACKAGE");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.fromFile(new File("foo.apk")), PACKAGE_MIME_TYPE);
        List<ResolveInfo> matches = queryIntentActivitiesInternal(intent, PACKAGE_MIME_TYPE, 1835008, 0);
        if (matches.size() != 1) {
            throw new RuntimeException("There must be exactly one installer; found " + matches);
        } else if (((ResolveInfo) matches.get(0)).activityInfo.applicationInfo.isPrivilegedApp()) {
            return ((ResolveInfo) matches.get(0)).getComponentInfo().packageName;
        } else {
            throw new RuntimeException("The installer must be a privileged app");
        }
    }

    private ComponentName getIntentFilterVerifierComponentNameLPr() {
        List<ResolveInfo> matches = queryIntentReceiversInternal(new Intent("android.intent.action.INTENT_FILTER_NEEDS_VERIFICATION"), PACKAGE_MIME_TYPE, 1835008, 0);
        ResolveInfo best = null;
        int N = matches.size();
        for (int i = 0; i < N; i++) {
            ResolveInfo cur = (ResolveInfo) matches.get(i);
            if (checkPermission("android.permission.INTENT_FILTER_VERIFICATION_AGENT", cur.getComponentInfo().packageName, 0) == 0 && (best == null || cur.priority > best.priority)) {
                best = cur;
            }
        }
        if (best != null) {
            return best.getComponentInfo().getComponentName();
        }
        throw new RuntimeException("There must be at least one intent filter verifier");
    }

    private ComponentName getEphemeralResolverLPr() {
        String[] packageArray = this.mContext.getResources().getStringArray(17236012);
        if (packageArray.length == 0) {
            return null;
        }
        List<ResolveInfo> resolvers = queryIntentServicesInternal(new Intent("android.intent.action.RESOLVE_EPHEMERAL_PACKAGE"), null, 1835008, 0);
        int N = resolvers.size();
        if (N == 0) {
            return null;
        }
        Set<String> possiblePackages = new ArraySet(Arrays.asList(packageArray));
        for (int i = 0; i < N; i++) {
            ResolveInfo info = (ResolveInfo) resolvers.get(i);
            if (info.serviceInfo != null) {
                String packageName = info.serviceInfo.packageName;
                if (possiblePackages.contains(packageName)) {
                    return new ComponentName(packageName, info.serviceInfo.name);
                }
            }
        }
        return null;
    }

    private ComponentName getEphemeralInstallerLPr() {
        Intent intent = new Intent("android.intent.action.INSTALL_EPHEMERAL_PACKAGE");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.fromFile(new File("foo.apk")), PACKAGE_MIME_TYPE);
        List<ResolveInfo> matches = queryIntentActivitiesInternal(intent, PACKAGE_MIME_TYPE, 1835008, 0);
        if (matches.size() == 0) {
            return null;
        }
        if (matches.size() == 1) {
            return ((ResolveInfo) matches.get(0)).getComponentInfo().getComponentName();
        }
        throw new RuntimeException("There must be at most one ephemeral installer; found " + matches);
    }

    private void primeDomainVerificationsLPw(int userId) {
        ArraySet<String> packages = SystemConfig.getInstance().getLinkedApps();
        ArraySet<String> domains = new ArraySet();
        for (String packageName : packages) {
            Package pkg = (Package) this.mPackages.get(packageName);
            if (pkg == null) {
                Slog.w(TAG, "Unknown package " + packageName + " in sysconfig <app-link>");
            } else if (pkg.isSystemApp()) {
                domains.clear();
                for (Activity a : pkg.activities) {
                    for (ActivityIntentInfo filter : a.intents) {
                        if (hasValidDomains(filter)) {
                            domains.addAll(filter.getHostsList());
                        }
                    }
                }
                if (domains.size() > 0) {
                    this.mSettings.createIntentFilterVerificationIfNeededLPw(packageName, new ArrayList(domains)).setStatus(0);
                    this.mSettings.updateIntentFilterVerificationStatusLPw(packageName, 2, userId);
                } else {
                    Slog.w(TAG, "Sysconfig <app-link> package '" + packageName + "' does not handle web links");
                }
            } else {
                Slog.w(TAG, "Non-system app '" + packageName + "' in sysconfig <app-link>");
            }
        }
        scheduleWritePackageRestrictionsLocked(userId);
        scheduleWriteSettingsLocked();
    }

    private void applyFactoryDefaultBrowserLPw(int userId) {
        String browserPkg = this.mContext.getResources().getString(17039431);
        if (!TextUtils.isEmpty(browserPkg)) {
            if (((PackageSetting) this.mSettings.mPackages.get(browserPkg)) == null) {
                Slog.e(TAG, "Product default browser app does not exist: " + browserPkg);
                browserPkg = null;
            } else {
                this.mSettings.setDefaultBrowserPackageNameLPw(browserPkg, userId);
            }
        }
        if (browserPkg == null) {
            calculateDefaultBrowserLPw(userId);
        }
    }

    private void calculateDefaultBrowserLPw(int userId) {
        List<String> allBrowsers = resolveAllBrowserApps(userId);
        this.mSettings.setDefaultBrowserPackageNameLPw(allBrowsers.size() == 1 ? (String) allBrowsers.get(0) : null, userId);
    }

    private List<String> resolveAllBrowserApps(int userId) {
        List<ResolveInfo> list = queryIntentActivitiesInternal(sBrowserIntent, null, 131072, userId);
        int count = list.size();
        List<String> result = new ArrayList(count);
        for (int i = 0; i < count; i++) {
            ResolveInfo info = (ResolveInfo) list.get(i);
            if (!(info.activityInfo == null || !info.handleAllWebDataURI || (info.activityInfo.applicationInfo.flags & 1) == 0 || result.contains(info.activityInfo.packageName))) {
                result.add(info.activityInfo.packageName);
            }
        }
        return result;
    }

    private boolean packageIsBrowser(String packageName, int userId) {
        List<ResolveInfo> list = queryIntentActivitiesInternal(sBrowserIntent, null, 131072, userId);
        int N = list.size();
        for (int i = 0; i < N; i++) {
            if (packageName.equals(((ResolveInfo) list.get(i)).activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    private void checkDefaultBrowser() {
        int myUserId = UserHandle.myUserId();
        String packageName = getDefaultBrowserPackageName(myUserId);
        if (packageName != null && getPackageInfo(packageName, 0, myUserId) == null) {
            Slog.w(TAG, "Default browser no longer installed: " + packageName);
            synchronized (this.mPackages) {
                applyFactoryDefaultBrowserLPw(myUserId);
            }
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!((e instanceof SecurityException) || (e instanceof IllegalArgumentException))) {
                Slog.wtf(TAG, "Package Manager Crash", e);
            }
            throw e;
        }
    }

    static int[] appendInts(int[] cur, int[] add) {
        if (add == null) {
            return cur;
        }
        if (cur == null) {
            return add;
        }
        for (int appendInt : add) {
            cur = ArrayUtils.appendInt(cur, appendInt);
        }
        return cur;
    }

    private PackageInfo generatePackageInfo(PackageSetting ps, int flags, int userId) {
        if (!sUserManager.exists(userId) || ps == null) {
            return null;
        }
        Package p = ps.pkg;
        if (p == null) {
            return null;
        }
        PermissionsState permissionsState = ps.getPermissionsState();
        return PackageParser.generatePackageInfo(p, (flags & 256) == 0 ? EMPTY_INT_ARRAY : permissionsState.computeGids(userId), flags, ps.firstInstallTime, ps.lastUpdateTime, ArrayUtils.isEmpty(p.requestedPermissions) ? Collections.emptySet() : permissionsState.getPermissions(userId), ps.readUserState(userId), userId);
    }

    public void checkPackageStartable(String packageName, int userId) {
        boolean userKeyUnlocked = StorageManager.isUserKeyUnlocked(userId);
        synchronized (this.mPackages) {
            PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(packageName);
            if (ps == null) {
                throw new SecurityException("Package " + packageName + " was not found!");
            } else if (!ps.getInstalled(userId)) {
                throw new SecurityException("Package " + packageName + " was not installed for user " + userId + "!");
            } else if (this.mSafeMode && !ps.isSystem()) {
                throw new SecurityException("Package " + packageName + " not a system app!");
            } else if (this.mFrozenPackages.contains(packageName)) {
                throw new SecurityException("Package " + packageName + " is currently frozen!");
            } else if (userKeyUnlocked || ps.pkg.applicationInfo.isDirectBootAware() || ps.pkg.applicationInfo.isPartiallyDirectBootAware()) {
            } else {
                throw new SecurityException("Package " + packageName + " is not encryption aware!");
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isPackageAvailable(String packageName, int userId) {
        if (!sUserManager.exists(userId)) {
            return false;
        }
        enforceCrossUserPermission(Binder.getCallingUid(), userId, false, false, "is package available");
        synchronized (this.mPackages) {
            Package p = (Package) this.mPackages.get(packageName);
            if (p != null) {
                PackageSetting ps = p.mExtras;
                if (ps != null) {
                    PackageUserState state = ps.readUserState(userId);
                    if (state != null) {
                        boolean isAvailable = PackageParser.isAvailable(state);
                        return isAvailable;
                    }
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public PackageInfo getPackageInfo(String packageName, int flags, int userId) {
        if (!sUserManager.exists(userId)) {
            return null;
        }
        flags = updateFlagsForPackage(flags, userId, packageName);
        enforceCrossUserPermission(Binder.getCallingUid(), userId, false, false, "get package info");
        synchronized (this.mPackages) {
            boolean matchFactoryOnly;
            if ((2097152 & flags) != 0) {
                matchFactoryOnly = true;
            } else {
                matchFactoryOnly = false;
            }
            if (matchFactoryOnly) {
                PackageSetting ps = this.mSettings.getDisabledSystemPkgLPr(packageName);
                if (ps != null) {
                    PackageInfo generatePackageInfo = generatePackageInfo(ps, flags, userId);
                    return generatePackageInfo;
                }
            }
            Package p = (Package) this.mPackages.get(packageName);
            if (matchFactoryOnly && p != null && !isSystemApp(p)) {
                return null;
            } else if (isHwCustHiddenInfoPackage(p)) {
                return null;
            } else if (p != null) {
                generatePackageInfo = generatePackageInfo((PackageSetting) p.mExtras, flags, userId);
                return generatePackageInfo;
            } else if (matchFactoryOnly || (flags & 8192) == 0) {
            } else {
                generatePackageInfo = generatePackageInfo((PackageSetting) this.mSettings.mPackages.get(packageName), flags, userId);
                return generatePackageInfo;
            }
        }
    }

    public String[] currentToCanonicalPackageNames(String[] names) {
        String[] out = new String[names.length];
        synchronized (this.mPackages) {
            int i = names.length - 1;
            while (i >= 0) {
                PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(names[i]);
                String str = (ps == null || ps.realName == null) ? names[i] : ps.realName;
                out[i] = str;
                i--;
            }
        }
        return out;
    }

    public String[] canonicalToCurrentPackageNames(String[] names) {
        String[] out = new String[names.length];
        synchronized (this.mPackages) {
            for (int i = names.length - 1; i >= 0; i--) {
                String cur = (String) this.mSettings.mRenamedPackages.get(names[i]);
                if (cur == null) {
                    cur = names[i];
                }
                out[i] = cur;
            }
        }
        return out;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getPackageUid(String packageName, int flags, int userId) {
        if (!sUserManager.exists(userId)) {
            return -1;
        }
        flags = updateFlagsForPackage(flags, userId, packageName);
        enforceCrossUserPermission(Binder.getCallingUid(), userId, false, false, "get package uid");
        synchronized (this.mPackages) {
            Package p = (Package) this.mPackages.get(packageName);
            int uid;
            if (p != null && p.isMatch(flags)) {
                uid = UserHandle.getUid(userId, p.applicationInfo.uid);
                return uid;
            } else if ((flags & 8192) != 0) {
                PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(packageName);
                if (ps != null && ps.isMatch(flags)) {
                    uid = UserHandle.getUid(userId, ps.appId);
                    return uid;
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int[] getPackageGids(String packageName, int flags, int userId) {
        if (!sUserManager.exists(userId)) {
            return null;
        }
        flags = updateFlagsForPackage(flags, userId, packageName);
        enforceCrossUserPermission(Binder.getCallingUid(), userId, false, false, "getPackageGids");
        synchronized (this.mPackages) {
            Package p = (Package) this.mPackages.get(packageName);
            int[] computeGids;
            if (p != null && p.isMatch(flags)) {
                computeGids = p.mExtras.getPermissionsState().computeGids(userId);
                return computeGids;
            } else if ((flags & 8192) != 0) {
                PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(packageName);
                if (ps != null && ps.isMatch(flags)) {
                    computeGids = ps.getPermissionsState().computeGids(userId);
                    return computeGids;
                }
            }
        }
    }

    static PermissionInfo generatePermissionInfo(BasePermission bp, int flags) {
        if (bp.perm != null) {
            return PackageParser.generatePermissionInfo(bp.perm, flags);
        }
        PermissionInfo pi = new PermissionInfo();
        pi.name = bp.name;
        pi.packageName = bp.sourcePackage;
        pi.nonLocalizedLabel = bp.name;
        pi.protectionLevel = bp.protectionLevel;
        return pi;
    }

    public PermissionInfo getPermissionInfo(String name, int flags) {
        synchronized (this.mPackages) {
            BasePermission p = (BasePermission) this.mSettings.mPermissions.get(name);
            if (p != null) {
                PermissionInfo generatePermissionInfo = generatePermissionInfo(p, flags);
                return generatePermissionInfo;
            }
            return null;
        }
    }

    public ParceledListSlice<PermissionInfo> queryPermissionsByGroup(String group, int flags) {
        synchronized (this.mPackages) {
            if (group != null) {
                if (!this.mPermissionGroups.containsKey(group)) {
                    return null;
                }
            }
            ArrayList<PermissionInfo> out = new ArrayList(10);
            for (BasePermission p : this.mSettings.mPermissions.values()) {
                if (group == null) {
                    if (p.perm == null || p.perm.info.group == null) {
                        out.add(generatePermissionInfo(p, flags));
                    }
                } else if (p.perm != null && group.equals(p.perm.info.group)) {
                    out.add(PackageParser.generatePermissionInfo(p.perm, flags));
                }
            }
            ParceledListSlice<PermissionInfo> parceledListSlice = new ParceledListSlice(out);
            return parceledListSlice;
        }
    }

    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) {
        PermissionGroupInfo generatePermissionGroupInfo;
        synchronized (this.mPackages) {
            generatePermissionGroupInfo = PackageParser.generatePermissionGroupInfo((PermissionGroup) this.mPermissionGroups.get(name), flags);
        }
        return generatePermissionGroupInfo;
    }

    public ParceledListSlice<PermissionGroupInfo> getAllPermissionGroups(int flags) {
        ParceledListSlice<PermissionGroupInfo> parceledListSlice;
        synchronized (this.mPackages) {
            ArrayList<PermissionGroupInfo> out = new ArrayList(this.mPermissionGroups.size());
            for (PermissionGroup pg : this.mPermissionGroups.values()) {
                out.add(PackageParser.generatePermissionGroupInfo(pg, flags));
            }
            parceledListSlice = new ParceledListSlice(out);
        }
        return parceledListSlice;
    }

    private ApplicationInfo generateApplicationInfoFromSettingsLPw(String packageName, int flags, int userId) {
        if (!sUserManager.exists(userId)) {
            return null;
        }
        PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(packageName);
        if (ps == null) {
            return null;
        }
        if (ps.pkg != null) {
            return PackageParser.generateApplicationInfo(ps.pkg, flags, ps.readUserState(userId), userId);
        }
        PackageInfo pInfo = generatePackageInfo(ps, flags, userId);
        if (pInfo != null) {
            return pInfo.applicationInfo;
        }
        return null;
    }

    public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) {
        if (!sUserManager.exists(userId)) {
            return null;
        }
        flags = updateFlagsForApplication(flags, userId, packageName);
        enforceCrossUserPermission(Binder.getCallingUid(), userId, false, false, "get application info");
        synchronized (this.mPackages) {
            Package p = (Package) this.mPackages.get(packageName);
            if (isHwCustHiddenInfoPackage(p)) {
                return null;
            } else if (p != null) {
                PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(packageName);
                if (ps == null) {
                    return null;
                }
                r0 = PackageParser.generateApplicationInfo(p, flags, ps.readUserState(userId), userId);
                return r0;
            } else if (PLATFORM_PACKAGE_NAME.equals(packageName) || "system".equals(packageName)) {
                r0 = this.mAndroidApplication;
                return r0;
            } else if ((flags & 8192) != 0) {
                r0 = generateApplicationInfoFromSettingsLPw(packageName, flags, userId);
                return r0;
            } else {
                return null;
            }
        }
    }

    public void freeStorageAndNotify(String volumeUuid, long freeStorageSize, IPackageDataObserver observer) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CLEAR_APP_CACHE", null);
        final String str = volumeUuid;
        final long j = freeStorageSize;
        final IPackageDataObserver iPackageDataObserver = observer;
        this.mHandler.post(new Runnable() {
            public void run() {
                PackageManagerService.this.mHandler.removeCallbacks(this);
                boolean success = true;
                synchronized (PackageManagerService.this.mInstallLock) {
                    try {
                        PackageManagerService.this.mInstaller.freeCache(str, j);
                    } catch (InstallerException e) {
                        Slog.w(PackageManagerService.TAG, "Couldn't clear application caches: " + e);
                        success = false;
                    }
                }
                if (iPackageDataObserver != null) {
                    try {
                        iPackageDataObserver.onRemoveCompleted(null, success);
                    } catch (RemoteException e2) {
                        Slog.w(PackageManagerService.TAG, "RemoveException when invoking call back");
                    }
                }
            }
        });
    }

    public void freeStorage(String volumeUuid, long freeStorageSize, IntentSender pi) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CLEAR_APP_CACHE", null);
        final String str = volumeUuid;
        final long j = freeStorageSize;
        final IntentSender intentSender = pi;
        this.mHandler.post(new Runnable() {
            public void run() {
                PackageManagerService.this.mHandler.removeCallbacks(this);
                boolean success = true;
                synchronized (PackageManagerService.this.mInstallLock) {
                    try {
                        PackageManagerService.this.mInstaller.freeCache(str, j);
                    } catch (InstallerException e) {
                        Slog.w(PackageManagerService.TAG, "Couldn't clear application caches: " + e);
                        success = false;
                    }
                }
                if (intentSender != null) {
                    try {
                        intentSender.sendIntent(null, success ? 1 : 0, null, null, null);
                    } catch (SendIntentException e2) {
                        Slog.i(PackageManagerService.TAG, "Failed to send pending intent");
                    }
                }
            }
        });
    }

    void freeStorage(String volumeUuid, long freeStorageSize) throws IOException {
        synchronized (this.mInstallLock) {
            try {
                this.mInstaller.freeCache(volumeUuid, freeStorageSize);
            } catch (InstallerException e) {
                throw new IOException("Failed to free enough space", e);
            }
        }
    }

    private int updateFlags(int flags, int userId) {
        if ((flags & 786432) != 0) {
            return flags;
        }
        if (getUserManagerInternal().isUserUnlockingOrUnlocked(userId)) {
            return flags | 786432;
        }
        return flags | DumpState.DUMP_FROZEN;
    }

    UserManagerInternal getUserManagerInternal() {
        if (this.mUserManagerInternal == null) {
            this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        }
        return this.mUserManagerInternal;
    }

    private int updateFlagsForPackage(int flags, int userId, Object cookie) {
        if ((flags & 15) != 0 && (269221888 & flags) == 0) {
        }
        if ((269492224 & flags) == 0) {
        }
        return updateFlags(flags, userId);
    }

    private int updateFlagsForApplication(int flags, int userId, Object cookie) {
        return updateFlagsForPackage(flags, userId, cookie);
    }

    private int updateFlagsForComponent(int flags, int userId, Object cookie) {
        if ((cookie instanceof Intent) && (((Intent) cookie).getFlags() & 256) != 0) {
            flags |= 268435456;
        }
        if ((269221888 & flags) == 0) {
        }
        return updateFlags(flags, userId);
    }

    int updateFlagsForResolve(int flags, int userId, Object cookie) {
        if (this.mSafeMode) {
            flags |= DumpState.DUMP_DEXOPT;
        }
        return updateFlagsForComponent(flags, userId, cookie);
    }

    public ActivityInfo getActivityInfo(ComponentName component, int flags, int userId) {
        if (!sUserManager.exists(userId)) {
            return null;
        }
        flags = updateFlagsForComponent(flags, userId, component);
        enforceCrossUserPermission(Binder.getCallingUid(), userId, false, false, "get activity info");
        synchronized (this.mPackages) {
            Activity a = (Activity) this.mActivities.mActivities.get(component);
            ActivityInfo generateActivityInfo;
            if (a != null && this.mSettings.isEnabledAndMatchLPr(a.info, flags, userId)) {
                PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(component.getPackageName());
                if (ps == null) {
                    return null;
                }
                generateActivityInfo = PackageParser.generateActivityInfo(a, flags, ps.readUserState(userId), userId);
                return generateActivityInfo;
            } else if (this.mResolveComponentName.equals(component)) {
                generateActivityInfo = PackageParser.generateActivityInfo(this.mResolveActivity, flags, new PackageUserState(), userId);
                return generateActivityInfo;
            } else {
                return null;
            }
        }
    }

    public boolean activitySupportsIntent(ComponentName component, Intent intent, String resolvedType) {
        synchronized (this.mPackages) {
            if (component.equals(this.mResolveComponentName)) {
                return true;
            }
            Activity a = (Activity) this.mActivities.mActivities.get(component);
            if (a == null) {
                return false;
            }
            for (int i = 0; i < a.intents.size(); i++) {
                if (((ActivityIntentInfo) a.intents.get(i)).match(intent.getAction(), resolvedType, intent.getScheme(), intent.getData(), intent.getCategories(), TAG) >= 0) {
                    return true;
                }
            }
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ActivityInfo getReceiverInfo(ComponentName component, int flags, int userId) {
        if (!sUserManager.exists(userId)) {
            return null;
        }
        flags = updateFlagsForComponent(flags, userId, component);
        enforceCrossUserPermission(Binder.getCallingUid(), userId, false, false, "get receiver info");
        synchronized (this.mPackages) {
            Activity a = (Activity) this.mReceivers.mActivities.get(component);
            if (a == null || !this.mSettings.isEnabledAndMatchLPr(a.info, flags, userId)) {
            } else {
                PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(component.getPackageName());
                if (ps == null) {
                    return null;
                }
                ActivityInfo generateActivityInfo = PackageParser.generateActivityInfo(a, flags, ps.readUserState(userId), userId);
                return generateActivityInfo;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ServiceInfo getServiceInfo(ComponentName component, int flags, int userId) {
        if (!sUserManager.exists(userId)) {
            return null;
        }
        flags = updateFlagsForComponent(flags, userId, component);
        enforceCrossUserPermission(Binder.getCallingUid(), userId, false, false, "get service info");
        synchronized (this.mPackages) {
            Service s = (Service) this.mServices.mServices.get(component);
            if (s == null || !this.mSettings.isEnabledAndMatchLPr(s.info, flags, userId)) {
            } else {
                PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(component.getPackageName());
                if (ps == null) {
                    return null;
                }
                ServiceInfo generateServiceInfo = PackageParser.generateServiceInfo(s, flags, ps.readUserState(userId), userId);
                return generateServiceInfo;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ProviderInfo getProviderInfo(ComponentName component, int flags, int userId) {
        if (!sUserManager.exists(userId)) {
            return null;
        }
        flags = updateFlagsForComponent(flags, userId, component);
        enforceCrossUserPermission(Binder.getCallingUid(), userId, false, false, "get provider info");
        synchronized (this.mPackages) {
            Provider p = (Provider) this.mProviders.mProviders.get(component);
            if (p == null || !this.mSettings.isEnabledAndMatchLPr(p.info, flags, userId)) {
            } else {
                PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(component.getPackageName());
                if (ps == null) {
                    return null;
                }
                ProviderInfo generateProviderInfo = PackageParser.generateProviderInfo(p, flags, ps.readUserState(userId), userId);
                return generateProviderInfo;
            }
        }
    }

    public String[] getSystemSharedLibraryNames() {
        synchronized (this.mPackages) {
            Set<String> libSet = this.mSharedLibraries.keySet();
            int size = libSet.size();
            if (size > 0) {
                String[] libs = new String[size];
                libSet.toArray(libs);
                return libs;
            }
            return null;
        }
    }

    public String getServicesSystemSharedLibraryPackageName() {
        String str;
        synchronized (this.mPackages) {
            str = this.mServicesSystemSharedLibraryPackageName;
        }
        return str;
    }

    public String getSharedSystemSharedLibraryPackageName() {
        String str;
        synchronized (this.mPackages) {
            str = this.mSharedSystemSharedLibraryPackageName;
        }
        return str;
    }

    public ParceledListSlice<FeatureInfo> getSystemAvailableFeatures() {
        ParceledListSlice<FeatureInfo> parceledListSlice;
        synchronized (this.mPackages) {
            ArrayList<FeatureInfo> res = new ArrayList(this.mAvailableFeatures.values());
            FeatureInfo fi = new FeatureInfo();
            fi.reqGlEsVersion = SystemProperties.getInt("ro.opengles.version", 0);
            res.add(fi);
            parceledListSlice = new ParceledListSlice(res);
        }
        return parceledListSlice;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hasSystemFeature(String name, int version) {
        boolean z = false;
        synchronized (this.mPackages) {
            FeatureInfo feat = (FeatureInfo) this.mAvailableFeatures.get(name);
            if (feat == null) {
                return false;
            } else if (feat.version >= version) {
                z = true;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int checkPermission(String permName, String pkgName, int userId) {
        if (!sUserManager.exists(userId)) {
            return -1;
        }
        synchronized (this.mPackages) {
            Package p = (Package) this.mPackages.get(pkgName);
            if (!(p == null || p.mExtras == null)) {
                PermissionsState permissionsState = p.mExtras.getPermissionsState();
                if (permissionsState.hasPermission(permName, userId)) {
                    return 0;
                } else if ("android.permission.ACCESS_COARSE_LOCATION".equals(permName) && permissionsState.hasPermission("android.permission.ACCESS_FINE_LOCATION", userId)) {
                    return 0;
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int checkUidPermission(String permName, int uid) {
        int userId = UserHandle.getUserId(uid);
        if (!sUserManager.exists(userId)) {
            return -1;
        }
        synchronized (this.mPackages) {
            Object obj = this.mSettings.getUserIdLPr(UserHandle.getAppId(uid));
            if (obj != null) {
                PermissionsState permissionsState = ((SettingBase) obj).getPermissionsState();
                if (permissionsState.hasPermission(permName, userId)) {
                    return 0;
                } else if ("android.permission.ACCESS_COARSE_LOCATION".equals(permName) && permissionsState.hasPermission("android.permission.ACCESS_FINE_LOCATION", userId)) {
                    return 0;
                }
            }
            ArraySet<String> perms = (ArraySet) this.mSystemPermissions.get(uid);
            if (perms != null) {
                if (perms.contains(permName)) {
                    return 0;
                } else if ("android.permission.ACCESS_COARSE_LOCATION".equals(permName) && perms.contains("android.permission.ACCESS_FINE_LOCATION")) {
                    return 0;
                }
            }
        }
    }

    public boolean isPermissionRevokedByPolicy(String permission, String packageName, int userId) {
        boolean z = false;
        if (UserHandle.getCallingUserId() != userId) {
            this.mContext.enforceCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "isPermissionRevokedByPolicy for user " + userId);
        }
        if (checkPermission(permission, packageName, userId) == 0) {
            return false;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            if ((getPermissionFlags(permission, packageName, userId) & 4) != 0) {
                z = true;
            }
            Binder.restoreCallingIdentity(identity);
            return z;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public String getPermissionControllerPackageName() {
        String str;
        synchronized (this.mPackages) {
            str = this.mRequiredInstallerPackage;
        }
        return str;
    }

    void enforceCrossUserPermission(int callingUid, int userId, boolean requireFullPermission, boolean checkShell, String message) {
        if (userId < 0) {
            throw new IllegalArgumentException("Invalid userId " + userId);
        }
        if (checkShell) {
            enforceShellRestriction("no_debugging_features", callingUid, userId);
        }
        if (!(userId == UserHandle.getUserId(callingUid) || callingUid == 1000 || callingUid == 0)) {
            if (requireFullPermission) {
                this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", message);
            } else {
                try {
                    this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", message);
                } catch (SecurityException e) {
                    this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS", message);
                }
            }
        }
    }

    void enforceShellRestriction(String restriction, int callingUid, int userHandle) {
        if (callingUid != 2000) {
            return;
        }
        if (userHandle >= 0 && sUserManager.hasUserRestriction(restriction, userHandle)) {
            throw new SecurityException("Shell does not have permission to access user " + userHandle);
        } else if (userHandle < 0) {
            Slog.e(TAG, "Unable to check shell permission for user " + userHandle + "\n\t" + Debug.getCallers(3));
        }
    }

    private BasePermission findPermissionTreeLP(String permName) {
        for (BasePermission bp : this.mSettings.mPermissionTrees.values()) {
            if (permName.startsWith(bp.name) && permName.length() > bp.name.length() && permName.charAt(bp.name.length()) == '.') {
                return bp;
            }
        }
        return null;
    }

    private BasePermission checkPermissionTreeLP(String permName) {
        if (permName != null) {
            BasePermission bp = findPermissionTreeLP(permName);
            if (bp != null) {
                if (bp.uid == UserHandle.getAppId(Binder.getCallingUid())) {
                    return bp;
                }
                throw new SecurityException("Calling uid " + Binder.getCallingUid() + " is not allowed to add to permission tree " + bp.name + " owned by uid " + bp.uid);
            }
        }
        throw new SecurityException("No permission tree found for " + permName);
    }

    static boolean compareStrings(CharSequence s1, CharSequence s2) {
        boolean z = false;
        if (s1 == null) {
            if (s2 == null) {
                z = true;
            }
            return z;
        } else if (s2 != null && s1.getClass() == s2.getClass()) {
            return s1.equals(s2);
        } else {
            return false;
        }
    }

    static boolean comparePermissionInfos(PermissionInfo pi1, PermissionInfo pi2) {
        if (pi1.icon == pi2.icon && pi1.logo == pi2.logo && pi1.protectionLevel == pi2.protectionLevel && compareStrings(pi1.name, pi2.name) && compareStrings(pi1.nonLocalizedLabel, pi2.nonLocalizedLabel) && compareStrings(pi1.packageName, pi2.packageName)) {
            return true;
        }
        return false;
    }

    int permissionInfoFootprint(PermissionInfo info) {
        int size = info.name.length();
        if (info.nonLocalizedLabel != null) {
            size += info.nonLocalizedLabel.length();
        }
        if (info.nonLocalizedDescription != null) {
            return size + info.nonLocalizedDescription.length();
        }
        return size;
    }

    int calculateCurrentPermissionFootprintLocked(BasePermission tree) {
        int size = 0;
        for (BasePermission perm : this.mSettings.mPermissions.values()) {
            if (perm.uid == tree.uid) {
                size += perm.name.length() + permissionInfoFootprint(perm.perm.info);
            }
        }
        return size;
    }

    void enforcePermissionCapLocked(PermissionInfo info, BasePermission tree) {
        if (tree.uid != 1000) {
            if (permissionInfoFootprint(info) + calculateCurrentPermissionFootprintLocked(tree) > DumpState.DUMP_VERSION) {
                throw new SecurityException("Permission tree size cap exceeded");
            }
        }
    }

    boolean addPermissionLocked(PermissionInfo info, boolean async) {
        if (info.labelRes == 0 && info.nonLocalizedLabel == null) {
            throw new SecurityException("Label must be specified in permission");
        }
        BasePermission tree = checkPermissionTreeLP(info.name);
        BasePermission bp = (BasePermission) this.mSettings.mPermissions.get(info.name);
        boolean added = bp == null;
        boolean changed = true;
        int fixedLevel = PermissionInfo.fixProtectionLevel(info.protectionLevel);
        if (added) {
            enforcePermissionCapLocked(info, tree);
            bp = new BasePermission(info.name, tree.sourcePackage, 2);
        } else if (bp.type != 2) {
            throw new SecurityException("Not allowed to modify non-dynamic permission " + info.name);
        } else if (bp.protectionLevel == fixedLevel && bp.perm.owner.equals(tree.perm.owner) && bp.uid == tree.uid && comparePermissionInfos(bp.perm.info, info)) {
            changed = false;
        }
        bp.protectionLevel = fixedLevel;
        PermissionInfo info2 = new PermissionInfo(info);
        info2.protectionLevel = fixedLevel;
        bp.perm = new Permission(tree.perm.owner, info2);
        bp.perm.info.packageName = tree.perm.info.packageName;
        bp.uid = tree.uid;
        if (added) {
            this.mSettings.mPermissions.put(info2.name, bp);
        }
        if (changed) {
            if (async) {
                scheduleWriteSettingsLocked();
            } else {
                this.mSettings.writeLPr();
            }
        }
        return added;
    }

    public boolean addPermission(PermissionInfo info) {
        boolean addPermissionLocked;
        synchronized (this.mPackages) {
            addPermissionLocked = addPermissionLocked(info, false);
        }
        return addPermissionLocked;
    }

    public boolean addPermissionAsync(PermissionInfo info) {
        boolean addPermissionLocked;
        synchronized (this.mPackages) {
            addPermissionLocked = addPermissionLocked(info, true);
        }
        return addPermissionLocked;
    }

    public void removePermission(String name) {
        synchronized (this.mPackages) {
            checkPermissionTreeLP(name);
            BasePermission bp = (BasePermission) this.mSettings.mPermissions.get(name);
            if (bp != null) {
                if (bp.type != 2) {
                    throw new SecurityException("Not allowed to modify non-dynamic permission " + name);
                }
                this.mSettings.mPermissions.remove(name);
                this.mSettings.writeLPr();
            }
        }
    }

    private static void enforceDeclaredAsUsedAndRuntimeOrDevelopmentPermission(Package pkg, BasePermission bp) {
        if (pkg.requestedPermissions.indexOf(bp.name) == -1) {
            throw new SecurityException("Package " + pkg.packageName + " has not requested permission " + bp.name);
        } else if (!bp.isRuntime() && !bp.isDevelopment()) {
            throw new SecurityException("Permission " + bp.name + " is not a changeable permission type");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void grantRuntimePermission(String packageName, String name, int userId) {
        if (sUserManager.exists(userId)) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "grantRuntimePermission");
            enforceCrossUserPermission(Binder.getCallingUid(), userId, true, true, "grantRuntimePermission");
            synchronized (this.mPackages) {
                Package pkg = (Package) this.mPackages.get(packageName);
                if (pkg == null) {
                    throw new IllegalArgumentException("Unknown package: " + packageName);
                }
                BasePermission bp = (BasePermission) this.mSettings.mPermissions.get(name);
                if (bp == null) {
                    throw new IllegalArgumentException("Unknown permission: " + name);
                }
                enforceDeclaredAsUsedAndRuntimeOrDevelopmentPermission(pkg, bp);
                if (Build.PERMISSIONS_REVIEW_REQUIRED && pkg.applicationInfo.targetSdkVersion < 23 && bp.isRuntime()) {
                    return;
                }
                int uid = UserHandle.getUid(userId, pkg.applicationInfo.uid);
                SettingBase sb = pkg.mExtras;
                if (sb == null) {
                    throw new IllegalArgumentException("Unknown package: " + packageName);
                }
                PermissionsState permissionsState = sb.getPermissionsState();
                if ((permissionsState.getPermissionFlags(name, userId) & 16) != 0) {
                    throw new SecurityException("Cannot grant system fixed permission " + name + " for package " + packageName);
                } else if (bp.isDevelopment()) {
                    if (permissionsState.grantInstallPermission(bp) != -1) {
                        scheduleWriteSettingsLocked();
                    }
                } else if (pkg.applicationInfo.targetSdkVersion < 23) {
                    Slog.w(TAG, "Cannot grant runtime permission to a legacy app");
                } else {
                    int result = permissionsState.grantRuntimePermission(bp, userId);
                    switch (result) {
                        case -1:
                            Flog.i(201, "grantRuntimePermission : for " + packageName + ", Permission " + name + ", userId " + userId + ", result " + result);
                            return;
                        case 1:
                            final int appId = UserHandle.getAppId(pkg.applicationInfo.uid);
                            final int i = userId;
                            this.mHandler.post(new Runnable() {
                                public void run() {
                                    PackageManagerService.this.killUid(appId, i, PackageManagerService.KILL_APP_REASON_GIDS_CHANGED);
                                }
                            });
                            break;
                    }
                    this.mOnPermissionChangeListeners.onPermissionsChanged(uid);
                    this.mSettings.writeRuntimePermissionsForUserLPr(userId, false);
                }
            }
        } else {
            Log.e(TAG, "No such user:" + userId);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void revokeRuntimePermission(String packageName, String name, int userId) {
        Flog.i(201, "revokeRuntimePermission : for " + packageName + ", Permission " + name + ", userId " + userId + ", calling pid " + Binder.getCallingPid());
        if (sUserManager.exists(userId)) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.REVOKE_RUNTIME_PERMISSIONS", "revokeRuntimePermission");
            enforceCrossUserPermission(Binder.getCallingUid(), userId, true, true, "revokeRuntimePermission");
            synchronized (this.mPackages) {
                Package pkg = (Package) this.mPackages.get(packageName);
                if (pkg == null) {
                    throw new IllegalArgumentException("Unknown package: " + packageName);
                }
                BasePermission bp = (BasePermission) this.mSettings.mPermissions.get(name);
                if (bp == null) {
                    throw new IllegalArgumentException("Unknown permission: " + name);
                }
                enforceDeclaredAsUsedAndRuntimeOrDevelopmentPermission(pkg, bp);
                if (Build.PERMISSIONS_REVIEW_REQUIRED && pkg.applicationInfo.targetSdkVersion < 23 && bp.isRuntime()) {
                    return;
                }
                SettingBase sb = pkg.mExtras;
                if (sb == null) {
                    throw new IllegalArgumentException("Unknown package: " + packageName);
                }
                PermissionsState permissionsState = sb.getPermissionsState();
                if ((permissionsState.getPermissionFlags(name, userId) & 16) != 0) {
                    throw new SecurityException("Cannot revoke system fixed permission " + name + " for package " + packageName);
                } else if (bp.isDevelopment()) {
                    if (permissionsState.revokeInstallPermission(bp) != -1) {
                        scheduleWriteSettingsLocked();
                    }
                } else if (permissionsState.revokeRuntimePermission(bp, userId) == -1) {
                    return;
                } else {
                    this.mOnPermissionChangeListeners.onPermissionsChanged(pkg.applicationInfo.uid);
                    this.mSettings.writeRuntimePermissionsForUserLPr(userId, true);
                    int appId = UserHandle.getAppId(pkg.applicationInfo.uid);
                    killUid(appId, userId, KILL_APP_REASON_PERMISSIONS_REVOKED);
                    return;
                }
            }
        }
        Log.e(TAG, "No such user:" + userId);
    }

    public void resetRuntimePermissions() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.REVOKE_RUNTIME_PERMISSIONS", "revokeRuntimePermission");
        int callingUid = Binder.getCallingUid();
        if (!(callingUid == 1000 || callingUid == 0)) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "resetRuntimePermissions");
        }
        synchronized (this.mPackages) {
            updatePermissionsLPw(null, null, 1);
            for (int userId : UserManagerService.getInstance().getUserIds()) {
                int packageCount = this.mPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    Package pkg = (Package) this.mPackages.valueAt(i);
                    if (pkg.mExtras instanceof PackageSetting) {
                        resetUserChangesToRuntimePermissionsAndFlagsLPw(pkg.mExtras, userId);
                    }
                }
            }
        }
    }

    public int getPermissionFlags(String name, String packageName, int userId) {
        if (!sUserManager.exists(userId)) {
            return 0;
        }
        enforceGrantRevokeRuntimePermissionPermissions("getPermissionFlags");
        enforceCrossUserPermission(Binder.getCallingUid(), userId, true, false, "getPermissionFlags");
        synchronized (this.mPackages) {
            Package pkg = (Package) this.mPackages.get(packageName);
            if (pkg == null) {
                return 0;
            } else if (((BasePermission) this.mSettings.mPermissions.get(name)) == null) {
                return 0;
            } else {
                SettingBase sb = pkg.mExtras;
                if (sb == null) {
                    return 0;
                }
                int permissionFlags = sb.getPermissionsState().getPermissionFlags(name, userId);
                return permissionFlags;
            }
        }
    }

    public void updatePermissionFlags(String name, String packageName, int flagMask, int flagValues, int userId) {
        if (sUserManager.exists(userId)) {
            enforceGrantRevokeRuntimePermissionPermissions("updatePermissionFlags");
            enforceCrossUserPermission(Binder.getCallingUid(), userId, true, true, "updatePermissionFlags");
            if (getCallingUid() != 1000) {
                flagMask = (flagMask & -17) & -33;
                flagValues = ((flagValues & -17) & -33) & -65;
            }
            synchronized (this.mPackages) {
                Package pkg = (Package) this.mPackages.get(packageName);
                if (pkg == null) {
                    throw new IllegalArgumentException("Unknown package: " + packageName);
                }
                BasePermission bp = (BasePermission) this.mSettings.mPermissions.get(name);
                if (bp == null) {
                    throw new IllegalArgumentException("Unknown permission: " + name);
                }
                SettingBase sb = pkg.mExtras;
                if (sb == null) {
                    throw new IllegalArgumentException("Unknown package: " + packageName);
                }
                PermissionsState permissionsState = sb.getPermissionsState();
                boolean hadState = permissionsState.getRuntimePermissionState(name, userId) != null;
                if (permissionsState.updatePermissionFlags(bp, userId, flagMask, flagValues)) {
                    if (permissionsState.getInstallPermissionState(name) != null) {
                        scheduleWriteSettingsLocked();
                    } else if (permissionsState.getRuntimePermissionState(name, userId) != null || hadState) {
                        this.mSettings.writeRuntimePermissionsForUserLPr(userId, false);
                    }
                }
            }
        }
    }

    public void updatePermissionFlagsForAllApps(int flagMask, int flagValues, int userId) {
        if (sUserManager.exists(userId)) {
            enforceGrantRevokeRuntimePermissionPermissions("updatePermissionFlagsForAllApps");
            enforceCrossUserPermission(Binder.getCallingUid(), userId, true, true, "updatePermissionFlagsForAllApps");
            if (getCallingUid() != 1000) {
                flagMask &= -17;
                flagValues &= -17;
            }
            synchronized (this.mPackages) {
                boolean changed = false;
                int packageCount = this.mPackages.size();
                for (int pkgIndex = 0; pkgIndex < packageCount; pkgIndex++) {
                    SettingBase sb = ((Package) this.mPackages.valueAt(pkgIndex)).mExtras;
                    if (sb != null) {
                        changed |= sb.getPermissionsState().updatePermissionFlagsForAllPermissions(userId, flagMask, flagValues);
                    }
                }
                if (changed) {
                    this.mSettings.writeRuntimePermissionsForUserLPr(userId, false);
                }
            }
        }
    }

    private void enforceGrantRevokeRuntimePermissionPermissions(String message) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS") != 0 && this.mContext.checkCallingOrSelfPermission("android.permission.REVOKE_RUNTIME_PERMISSIONS") != 0) {
            throw new SecurityException(message + " requires " + "android.permission.GRANT_RUNTIME_PERMISSIONS" + " or " + "android.permission.REVOKE_RUNTIME_PERMISSIONS");
        }
    }

    public boolean shouldShowRequestPermissionRationale(String permissionName, String packageName, int userId) {
        boolean z = false;
        if (UserHandle.getCallingUserId() != userId) {
            this.mContext.enforceCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "canShowRequestPermissionRationale for user " + userId);
        }
        if (UserHandle.getAppId(getCallingUid()) != UserHandle.getAppId(getPackageUid(packageName, 268435456, userId)) || checkPermission(permissionName, packageName, userId) == 0) {
            return false;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            int flags = getPermissionFlags(permissionName, packageName, userId);
            if ((flags & 22) != 0) {
                return false;
            }
            if ((flags & 1) != 0) {
                z = true;
            }
            return z;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void addOnPermissionsChangeListener(IOnPermissionsChangeListener listener) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.OBSERVE_GRANT_REVOKE_PERMISSIONS", "addOnPermissionsChangeListener");
        synchronized (this.mPackages) {
            this.mOnPermissionChangeListeners.addListenerLocked(listener);
        }
    }

    public void removeOnPermissionsChangeListener(IOnPermissionsChangeListener listener) {
        synchronized (this.mPackages) {
            this.mOnPermissionChangeListeners.removeListenerLocked(listener);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isProtectedBroadcast(String actionName) {
        synchronized (this.mPackages) {
            if (this.mProtectedBroadcasts.contains(actionName)) {
                return true;
            } else if (actionName != null) {
                if (actionName.startsWith("android.net.netmon.lingerExpired") || actionName.startsWith("com.android.server.sip.SipWakeupTimer") || actionName.startsWith("com.android.internal.telephony.data-reconnect") || actionName.startsWith("android.net.netmon.launchCaptivePortalApp")) {
                }
            }
        }
    }

    public int checkSignatures(String pkg1, String pkg2) {
        synchronized (this.mPackages) {
            Package p1 = (Package) this.mPackages.get(pkg1);
            Package p2 = (Package) this.mPackages.get(pkg2);
            if (!(p1 == null || p1.mExtras == null || p2 == null)) {
                if (p2.mExtras != null) {
                    int compareSignatures = compareSignatures(p1.mSignatures, p2.mSignatures);
                    return compareSignatures;
                }
            }
            return -4;
        }
    }

    public int checkUidSignatures(int uid1, int uid2) {
        uid1 = UserHandle.getAppId(uid1);
        uid2 = UserHandle.getAppId(uid2);
        synchronized (this.mPackages) {
            Object obj = this.mSettings.getUserIdLPr(uid1);
            if (obj != null) {
                Signature[] s1;
                if (obj instanceof SharedUserSetting) {
                    s1 = ((SharedUserSetting) obj).signatures.mSignatures;
                } else if (obj instanceof PackageSetting) {
                    s1 = ((PackageSetting) obj).signatures.mSignatures;
                } else {
                    return -4;
                }
                obj = this.mSettings.getUserIdLPr(uid2);
                if (obj != null) {
                    Signature[] s2;
                    if (obj instanceof SharedUserSetting) {
                        s2 = ((SharedUserSetting) obj).signatures.mSignatures;
                    } else if (obj instanceof PackageSetting) {
                        s2 = ((PackageSetting) obj).signatures.mSignatures;
                    } else {
                        return -4;
                    }
                    int compareSignatures = compareSignatures(s1, s2);
                    return compareSignatures;
                }
                return -4;
            }
            return -4;
        }
    }

    private void killUid(int appId, int userId, String reason) {
        long identity = Binder.clearCallingIdentity();
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            if (am != null) {
                try {
                    am.killUid(appId, userId, reason);
                } catch (RemoteException e) {
                }
            }
            Binder.restoreCallingIdentity(identity);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    static int compareSignatures(Signature[] s1, Signature[] s2) {
        int i = 1;
        if (s1 == null) {
            if (s2 != null) {
                i = -1;
            }
            return i;
        } else if (s2 == null) {
            return -2;
        } else {
            if (s1.length != s2.length) {
                return -3;
            }
            if (s1.length == 1) {
                return s1[0].equals(s2[0]) ? 0 : -3;
            }
            ArraySet<Signature> set1 = new ArraySet();
            for (Signature sig : s1) {
                set1.add(sig);
            }
            ArraySet<Signature> set2 = new ArraySet();
            for (Signature sig2 : s2) {
                set2.add(sig2);
            }
            return set1.equals(set2) ? 0 : -3;
        }
    }

    private boolean isCompatSignatureUpdateNeeded(Package scannedPkg) {
        VersionInfo ver = getSettingsVersionForPackage(scannedPkg);
        if (ver == null || ver.databaseVersion >= 2) {
            return false;
        }
        return true;
    }

    private int compareSignaturesCompat(PackageSignatures existingSigs, Package scannedPkg) {
        if (!isCompatSignatureUpdateNeeded(scannedPkg)) {
            return -3;
        }
        ArraySet<Signature> existingSet = new ArraySet();
        for (Signature sig : existingSigs.mSignatures) {
            existingSet.add(sig);
        }
        ArraySet<Signature> scannedCompatSet = new ArraySet();
        for (Signature sig2 : scannedPkg.mSignatures) {
            try {
                for (Signature chainSig : sig2.getChainSignatures()) {
                    scannedCompatSet.add(chainSig);
                }
            } catch (CertificateEncodingException e) {
                scannedCompatSet.add(sig2);
            }
        }
        if (!scannedCompatSet.equals(existingSet)) {
            return -3;
        }
        existingSigs.assignSignatures(scannedPkg.mSignatures);
        synchronized (this.mPackages) {
            this.mSettings.mKeySetManagerService.removeAppKeySetDataLPw(scannedPkg.packageName);
        }
        return 0;
    }

    private boolean isRecoverSignatureUpdateNeeded(Package scannedPkg) {
        VersionInfo ver = getSettingsVersionForPackage(scannedPkg);
        if (ver == null || ver.databaseVersion >= 3) {
            return false;
        }
        return true;
    }

    private int compareSignaturesRecover(PackageSignatures existingSigs, Package scannedPkg) {
        if (!isRecoverSignatureUpdateNeeded(scannedPkg)) {
            return -3;
        }
        String msg = null;
        try {
            if (Signature.areEffectiveMatch(existingSigs.mSignatures, scannedPkg.mSignatures)) {
                logCriticalInfo(4, "Recovered effectively matching certificates for " + scannedPkg.packageName);
                return 0;
            }
        } catch (CertificateException e) {
            msg = e.getMessage();
        }
        logCriticalInfo(4, "Failed to recover certificates for " + scannedPkg.packageName + ": " + msg);
        return -3;
    }

    public List<String> getAllPackages() {
        List arrayList;
        synchronized (this.mPackages) {
            arrayList = new ArrayList(this.mPackages.keySet());
        }
        return arrayList;
    }

    public String[] getPackagesForUid(int uid) {
        uid = UserHandle.getAppId(uid);
        synchronized (this.mPackages) {
            Object obj = this.mSettings.getUserIdLPr(uid);
            if (obj instanceof SharedUserSetting) {
                SharedUserSetting sus = (SharedUserSetting) obj;
                int N = sus.packages.size();
                String[] res = new String[N];
                for (int i = 0; i < N; i++) {
                    res[i] = ((PackageSetting) sus.packages.valueAt(i)).name;
                }
                return res;
            } else if (obj instanceof PackageSetting) {
                String[] strArr = new String[]{((PackageSetting) obj).name};
                return strArr;
            } else {
                return null;
            }
        }
    }

    public String getNameForUid(int uid) {
        synchronized (this.mPackages) {
            Object obj = this.mSettings.getUserIdLPr(UserHandle.getAppId(uid));
            String str;
            if (obj instanceof SharedUserSetting) {
                SharedUserSetting sus = (SharedUserSetting) obj;
                str = sus.name + ":" + sus.userId;
                return str;
            } else if (obj instanceof PackageSetting) {
                str = ((PackageSetting) obj).name;
                return str;
            } else {
                return null;
            }
        }
    }

    public int getUidForSharedUser(String sharedUserName) {
        if (sharedUserName == null) {
            return -1;
        }
        synchronized (this.mPackages) {
            SharedUserSetting suid = this.mSettings.getSharedUserLPw(sharedUserName, 0, 0, false);
            if (suid == null) {
                return -1;
            }
            int i = suid.userId;
            return i;
        }
    }

    public int getFlagsForUid(int uid) {
        synchronized (this.mPackages) {
            Object obj = this.mSettings.getUserIdLPr(UserHandle.getAppId(uid));
            int i;
            if (obj instanceof SharedUserSetting) {
                i = ((SharedUserSetting) obj).pkgFlags;
                return i;
            } else if (obj instanceof PackageSetting) {
                i = ((PackageSetting) obj).pkgFlags;
                return i;
            } else {
                return 0;
            }
        }
    }

    public int getPrivateFlagsForUid(int uid) {
        synchronized (this.mPackages) {
            Object obj = this.mSettings.getUserIdLPr(UserHandle.getAppId(uid));
            int i;
            if (obj instanceof SharedUserSetting) {
                i = ((SharedUserSetting) obj).pkgPrivateFlags;
                return i;
            } else if (obj instanceof PackageSetting) {
                i = ((PackageSetting) obj).pkgPrivateFlags;
                return i;
            } else {
                return 0;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isUidPrivileged(int uid) {
        uid = UserHandle.getAppId(uid);
        synchronized (this.mPackages) {
            Object obj = this.mSettings.getUserIdLPr(uid);
            if (obj instanceof SharedUserSetting) {
                Iterator<PackageSetting> it = ((SharedUserSetting) obj).packages.iterator();
                while (it.hasNext()) {
                    if (((PackageSetting) it.next()).isPrivileged()) {
                        return true;
                    }
                }
            } else if (obj instanceof PackageSetting) {
                boolean isPrivileged = ((PackageSetting) obj).isPrivileged();
                return isPrivileged;
            }
        }
    }

    public String[] getAppOpPermissionPackages(String permissionName) {
        synchronized (this.mPackages) {
            ArraySet<String> pkgs = (ArraySet) this.mAppOpPermissionPackages.get(permissionName);
            if (pkgs == null) {
                return null;
            }
            String[] strArr = (String[]) pkgs.toArray(new String[pkgs.size()]);
            return strArr;
        }
    }

    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId) {
        try {
            Trace.traceBegin(262144, "resolveIntent");
            if (!sUserManager.exists(userId)) {
                return null;
            }
            flags = updateFlagsForResolve(flags, userId, intent);
            enforceCrossUserPermission(Binder.getCallingUid(), userId, false, false, "resolve intent");
            Trace.traceBegin(262144, "queryIntentActivities");
            List<ResolveInfo> query = queryIntentActivitiesInternal(intent, resolvedType, flags, userId);
            Trace.traceEnd(262144);
            ResolveInfo bestChoice = chooseBestActivity(intent, resolvedType, flags, query, userId);
            if (isEphemeralAllowed(intent, query, userId)) {
                Trace.traceBegin(262144, "resolveEphemeral");
                EphemeralResolveInfo ai = getEphemeralResolveInfo(intent, resolvedType, userId);
                if (ai != null) {
                    bestChoice.ephemeralInstaller = this.mEphemeralInstallerInfo;
                    bestChoice.ephemeralResolveInfo = ai;
                }
                Trace.traceEnd(262144);
            }
            Trace.traceEnd(262144);
            return bestChoice;
        } finally {
            Trace.traceEnd(262144);
        }
    }

    public void setLastChosenActivity(Intent intent, String resolvedType, int flags, IntentFilter filter, int match, ComponentName activity) {
        int userId = UserHandle.getCallingUserId();
        intent.setComponent(null);
        findPreferredActivity(intent, resolvedType, flags, queryIntentActivitiesInternal(intent, resolvedType, flags, userId), 0, false, true, false, userId);
        addPreferredActivityInternal(filter, match, null, activity, false, userId, "Setting last chosen");
    }

    public ResolveInfo getLastChosenActivity(Intent intent, String resolvedType, int flags) {
        int userId = UserHandle.getCallingUserId();
        return findPreferredActivity(intent, resolvedType, flags, queryIntentActivitiesInternal(intent, resolvedType, flags, userId), 0, false, false, false, userId);
    }

    private boolean isEphemeralAllowed(Intent intent, List<ResolveInfo> list, int userId) {
        return false;
    }

    private EphemeralResolveInfo getEphemeralResolveInfo(Intent intent, String resolvedType, int userId) {
        try {
            byte[] digestBytes = MessageDigest.getInstance("SHA-256").digest(intent.getData().getHost().getBytes());
            List<EphemeralResolveInfo> ephemeralResolveInfoList = this.mEphemeralResolverConnection.getEphemeralResolveInfoList((((digestBytes[0] << 24) | (digestBytes[1] << 16)) | (digestBytes[2] << 8)) | (digestBytes[3] << 0));
            if (ephemeralResolveInfoList == null || ephemeralResolveInfoList.size() == 0) {
                return null;
            }
            for (int i = ephemeralResolveInfoList.size() - 1; i >= 0; i--) {
                EphemeralResolveInfo ephemeralApplication = (EphemeralResolveInfo) ephemeralResolveInfoList.get(i);
                if (Arrays.equals(digestBytes, ephemeralApplication.getDigestBytes())) {
                    List<IntentFilter> filters = ephemeralApplication.getFilters();
                    if (filters.isEmpty()) {
                        continue;
                    } else {
                        EphemeralIntentResolver ephemeralResolver = new EphemeralIntentResolver();
                        for (int j = filters.size() - 1; j >= 0; j--) {
                            ephemeralResolver.addFilter(new EphemeralResolveIntentInfo((IntentFilter) filters.get(j), ephemeralApplication));
                        }
                        List<EphemeralResolveInfo> matchedResolveInfoList = ephemeralResolver.queryIntent(intent, resolvedType, false, userId);
                        if (!matchedResolveInfoList.isEmpty()) {
                            return (EphemeralResolveInfo) matchedResolveInfoList.get(0);
                        }
                    }
                }
            }
            return null;
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private ResolveInfo chooseBestActivity(Intent intent, String resolvedType, int flags, List<ResolveInfo> query, int userId) {
        if (query != null) {
            int N = query.size();
            if (N == 1) {
                return (ResolveInfo) query.get(0);
            }
            if (N > 1) {
                boolean debug = (intent.getFlags() & 8) != 0;
                ResolveInfo r0 = (ResolveInfo) query.get(0);
                ResolveInfo r1 = (ResolveInfo) query.get(1);
                if (debug) {
                    Slog.v(TAG, r0.activityInfo.name + "=" + r0.priority + " vs " + r1.activityInfo.name + "=" + r1.priority);
                }
                if (r0.priority != r1.priority || r0.preferredOrder != r1.preferredOrder || r0.isDefault != r1.isDefault) {
                    return (ResolveInfo) query.get(0);
                }
                ResolveInfo ri = findPreferredActivity(intent, resolvedType, flags, query, r0.priority, true, false, debug, userId);
                if (ri != null) {
                    return ri;
                }
                ResolveInfo resolveInfo = new ResolveInfo(this.mResolveInfo);
                resolveInfo.activityInfo = new ActivityInfo(resolveInfo.activityInfo);
                resolveInfo.activityInfo.labelRes = ResolverActivity.getLabelRes(intent.getAction());
                String intentPackage = intent.getPackage();
                if (!TextUtils.isEmpty(intentPackage) && allHavePackage(query, intentPackage)) {
                    ApplicationInfo appi = ((ResolveInfo) query.get(0)).activityInfo.applicationInfo;
                    resolveInfo.resolvePackageName = intentPackage;
                    if (userNeedsBadging(userId)) {
                        resolveInfo.noResourceId = true;
                    } else {
                        resolveInfo.icon = appi.icon;
                    }
                    resolveInfo.iconResourceId = appi.icon;
                    resolveInfo.labelRes = appi.labelRes;
                }
                resolveInfo.activityInfo.applicationInfo = new ApplicationInfo(resolveInfo.activityInfo.applicationInfo);
                if (userId != 0) {
                    resolveInfo.activityInfo.applicationInfo.uid = UserHandle.getUid(userId, UserHandle.getAppId(resolveInfo.activityInfo.applicationInfo.uid));
                }
                if (resolveInfo.activityInfo.metaData == null) {
                    resolveInfo.activityInfo.metaData = new Bundle();
                }
                resolveInfo.activityInfo.metaData.putBoolean("android.dock_home", true);
                return resolveInfo;
            }
        }
        return null;
    }

    private boolean allHavePackage(List<ResolveInfo> list, String packageName) {
        if (ArrayUtils.isEmpty(list)) {
            return false;
        }
        int N = list.size();
        for (int i = 0; i < N; i++) {
            ActivityInfo ai;
            ResolveInfo ri = (ResolveInfo) list.get(i);
            if (ri != null) {
                ai = ri.activityInfo;
            } else {
                ai = null;
            }
            if (ai == null || !packageName.equals(ai.packageName)) {
                return false;
            }
        }
        return true;
    }

    private ResolveInfo findPersistentPreferredActivityLP(Intent intent, String resolvedType, int flags, List<ResolveInfo> query, boolean debug, int userId) {
        List<PersistentPreferredActivity> pprefs;
        int N = query.size();
        PersistentPreferredIntentResolver ppir = (PersistentPreferredIntentResolver) this.mSettings.mPersistentPreferredActivities.get(userId);
        if (debug) {
            Slog.v(TAG, "Looking for presistent preferred activities...");
        }
        if (ppir != null) {
            pprefs = ppir.queryIntent(intent, resolvedType, (65536 & flags) != 0, userId);
        } else {
            pprefs = null;
        }
        if (pprefs != null && pprefs.size() > 0) {
            int M = pprefs.size();
            for (int i = 0; i < M; i++) {
                PersistentPreferredActivity ppa = (PersistentPreferredActivity) pprefs.get(i);
                if (debug) {
                    Slog.v(TAG, "Checking PersistentPreferredActivity ds=" + (ppa.countDataSchemes() > 0 ? ppa.getDataScheme(0) : "<none>") + "\n  component=" + ppa.mComponent);
                    ppa.dump(new LogPrinter(2, TAG, 3), "  ");
                }
                ActivityInfo ai = getActivityInfo(ppa.mComponent, flags | 512, userId);
                if (debug) {
                    Slog.v(TAG, "Found persistent preferred activity:");
                    if (ai != null) {
                        ai.dump(new LogPrinter(2, TAG, 3), "  ");
                    } else {
                        Slog.v(TAG, "  null");
                    }
                }
                if (ai != null) {
                    for (int j = 0; j < N; j++) {
                        ResolveInfo ri = (ResolveInfo) query.get(j);
                        if (ri.activityInfo.applicationInfo.packageName.equals(ai.applicationInfo.packageName) && ri.activityInfo.name.equals(ai.name)) {
                            if (debug) {
                                Slog.v(TAG, "Returning persistent preferred activity: " + ri.activityInfo.packageName + "/" + ri.activityInfo.name);
                            }
                            return ri;
                        }
                    }
                    continue;
                }
            }
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    ResolveInfo findPreferredActivity(Intent intent, String resolvedType, int flags, List<ResolveInfo> query, int priority, boolean always, boolean removeMatches, boolean debug, int userId) {
        if (!sUserManager.exists(userId)) {
            return null;
        }
        flags = updateFlagsForResolve(flags, userId, intent);
        synchronized (this.mPackages) {
            if (intent.getSelector() != null) {
                intent = intent.getSelector();
            }
            ResolveInfo pri = findPersistentPreferredActivityLP(intent, resolvedType, flags, query, debug, userId);
            if (pri != null) {
                return pri;
            }
            List<PreferredActivity> prefs;
            PreferredIntentResolver pir = (PreferredIntentResolver) this.mSettings.mPreferredActivities.get(userId);
            if (debug) {
                Slog.v(TAG, "Looking for preferred activities...");
            }
            if (pir != null) {
                prefs = pir.queryIntent(intent, resolvedType, (65536 & flags) != 0, userId);
            } else {
                prefs = null;
            }
            if (prefs != null && prefs.size() > 0) {
                int j;
                ResolveInfo ri;
                changed = false;
                int match = 0;
                if (debug) {
                    Slog.v(TAG, "Figuring out best match...");
                }
                int N = query.size();
                for (j = 0; j < N; j++) {
                    ri = (ResolveInfo) query.get(j);
                    if (debug) {
                        Slog.v(TAG, "Match for " + ri.activityInfo + ": 0x" + Integer.toHexString(match));
                    }
                    if (ri.match > match) {
                        match = ri.match;
                    }
                }
                if (debug) {
                    Slog.v(TAG, "Best match: 0x" + Integer.toHexString(match));
                }
                match &= 268369920;
                int M = prefs.size();
                for (int i = 0; i < M; i++) {
                    PreferredActivity pa = (PreferredActivity) prefs.get(i);
                    if (debug) {
                        Slog.v(TAG, "Checking PreferredActivity ds=" + (pa.countDataSchemes() > 0 ? pa.getDataScheme(0) : "<none>") + "\n  component=" + pa.mPref.mComponent);
                        pa.dump(new LogPrinter(2, TAG, 3), "  ");
                    }
                    if (pa.mPref.mMatch != match) {
                        if (debug) {
                            Slog.v(TAG, "Skipping bad match " + Integer.toHexString(pa.mPref.mMatch));
                        }
                    } else if (!always || pa.mPref.mAlways) {
                        ActivityInfo ai = getActivityInfo(pa.mPref.mComponent, ((flags | 512) | DumpState.DUMP_FROZEN) | DumpState.DUMP_DOMAIN_PREFERRED, userId);
                        if (debug) {
                            Slog.v(TAG, "Found preferred activity:");
                            if (ai != null) {
                                ai.dump(new LogPrinter(2, TAG, 3), "  ");
                            } else {
                                try {
                                    Slog.v(TAG, "  null");
                                } catch (Throwable th) {
                                    boolean changed;
                                    if (changed) {
                                        scheduleWritePackageRestrictionsLocked(userId);
                                    }
                                }
                            }
                        }
                        if (ai == null) {
                            Slog.w(TAG, "Removing dangling preferred activity: " + pa.mPref.mComponent);
                            pir.removeFilter(pa);
                            changed = true;
                        } else {
                            j = 0;
                            while (j < N) {
                                ri = (ResolveInfo) query.get(j);
                                if (!ri.activityInfo.applicationInfo.packageName.equals(ai.applicationInfo.packageName) || !ri.activityInfo.name.equals(ai.name)) {
                                    j++;
                                } else if (removeMatches) {
                                    pir.removeFilter(pa);
                                    changed = true;
                                } else {
                                    boolean audioType;
                                    boolean notSameSet = always && !pa.mPref.sameSet((List) query);
                                    if (intent.getAction() == null || !intent.getAction().equals("android.intent.action.VIEW") || intent.getData() == null || intent.getData().getScheme() == null || (!(intent.getData().getScheme().equals("file") || intent.getData().getScheme().equals("content")) || intent.getType() == null)) {
                                        audioType = false;
                                    } else {
                                        audioType = intent.getType().startsWith("audio/");
                                    }
                                    if (audioType) {
                                        Slog.i(TAG, "preferred activity for " + intent + " type " + resolvedType + ", do not dropping preferred activity");
                                        notSameSet = false;
                                    }
                                    if (notSameSet) {
                                        if (!intent.hasCategory("android.intent.category.HOME") || (intent.getFlags() & 512) == 0) {
                                            Slog.i(TAG, "Result set changed, dropping preferred activity for " + intent + " type " + resolvedType);
                                            pir.removeFilter(pa);
                                            pir.addFilter(new PreferredActivity(pa, pa.mPref.mMatch, null, pa.mPref.mComponent, false));
                                            if (true) {
                                                scheduleWritePackageRestrictionsLocked(userId);
                                            }
                                            return null;
                                        }
                                    }
                                    if (debug) {
                                        Slog.v(TAG, "Returning preferred activity: " + ri.activityInfo.packageName + "/" + ri.activityInfo.name);
                                    }
                                    if (changed) {
                                        scheduleWritePackageRestrictionsLocked(userId);
                                    }
                                }
                            }
                            continue;
                        }
                    } else if (debug) {
                        Slog.v(TAG, "Skipping mAlways=false entry");
                    }
                }
                if (changed) {
                    scheduleWritePackageRestrictionsLocked(userId);
                }
            }
        }
    }

    public boolean canForwardTo(Intent intent, String resolvedType, int sourceUserId, int targetUserId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", null);
        List<CrossProfileIntentFilter> matches = getMatchingCrossProfileIntentFilters(intent, resolvedType, sourceUserId);
        if (matches != null) {
            int size = matches.size();
            for (int i = 0; i < size; i++) {
                if (((CrossProfileIntentFilter) matches.get(i)).getTargetUserId() == targetUserId) {
                    return true;
                }
            }
        }
        if (!hasWebURI(intent)) {
            return false;
        }
        boolean z;
        UserInfo parent = getProfileParent(sourceUserId);
        synchronized (this.mPackages) {
            z = getCrossProfileDomainPreferredLpr(intent, resolvedType, updateFlagsForResolve(0, parent.id, intent), sourceUserId, parent.id) != null;
        }
        return z;
    }

    private UserInfo getProfileParent(int userId) {
        long identity = Binder.clearCallingIdentity();
        try {
            UserInfo profileParent = sUserManager.getProfileParent(userId);
            return profileParent;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private List<CrossProfileIntentFilter> getMatchingCrossProfileIntentFilters(Intent intent, String resolvedType, int userId) {
        CrossProfileIntentResolver resolver = (CrossProfileIntentResolver) this.mSettings.mCrossProfileIntentResolvers.get(userId);
        if (resolver != null) {
            return resolver.queryIntent(intent, resolvedType, false, userId);
        }
        return null;
    }

    public ParceledListSlice<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) {
        try {
            Trace.traceBegin(262144, "queryIntentActivities");
            ParceledListSlice<ResolveInfo> parceledListSlice = new ParceledListSlice(queryIntentActivitiesInternal(intent, resolvedType, flags, userId));
            return parceledListSlice;
        } finally {
            Trace.traceEnd(262144);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected List<ResolveInfo> queryIntentActivitiesInternal(Intent intent, String resolvedType, int flags, int userId) {
        if (!sUserManager.exists(userId)) {
            return Collections.emptyList();
        }
        flags = updateFlagsForResolve(flags, userId, intent);
        enforceCrossUserPermission(Binder.getCallingUid(), userId, false, false, "query intent activities");
        ComponentName comp = intent.getComponent();
        if (comp == null && intent.getSelector() != null) {
            intent = intent.getSelector();
            comp = intent.getComponent();
        }
        if (comp != null) {
            List<ResolveInfo> arrayList = new ArrayList(1);
            ActivityInfo ai = getActivityInfo(comp, flags, userId);
            if (ai != null) {
                ResolveInfo ri = new ResolveInfo();
                ri.activityInfo = ai;
                arrayList.add(ri);
            }
            return arrayList;
        }
        synchronized (this.mPackages) {
            String pkgName = intent.getPackage();
            List<ResolveInfo> filterIfNotSystemUser;
            if (pkgName == null) {
                List<CrossProfileIntentFilter> matchingFilters = getMatchingCrossProfileIntentFilters(intent, resolvedType, userId);
                List<ResolveInfo> xpResolveInfos = querySkipCurrentProfileIntents(matchingFilters, intent, resolvedType, flags, userId);
                if (xpResolveInfos.size() > 0) {
                    filterIfNotSystemUser = filterIfNotSystemUser(xpResolveInfos, userId);
                    return filterIfNotSystemUser;
                }
                List<ResolveInfo> result = filterIfNotSystemUser(this.mActivities.queryIntent(intent, resolvedType, flags, userId), userId);
                xpResolveInfos = queryCrossProfileIntents(matchingFilters, intent, resolvedType, flags, userId, hasNonNegativePriority(result));
                if (xpResolveInfos.size() > 0) {
                    for (ResolveInfo ri2 : xpResolveInfos) {
                        if (isUserEnabled(ri2.targetUserId)) {
                            if (filterIfNotSystemUser(Collections.singletonList(ri2), userId).size() > 0) {
                                result.add(ri2);
                            }
                        }
                    }
                    Collections.sort(result, mResolvePrioritySorter);
                }
                if (hasWebURI(intent)) {
                    CrossProfileDomainInfo crossProfileDomainInfo = null;
                    UserInfo parent = getProfileParent(userId);
                    if (parent != null) {
                        crossProfileDomainInfo = getCrossProfileDomainPreferredLpr(intent, resolvedType, flags, userId, parent.id);
                    }
                    if (crossProfileDomainInfo != null) {
                        if (xpResolveInfos.size() > 0) {
                            for (ResolveInfo ri22 : xpResolveInfos) {
                                if (parent.id == ri22.targetUserId) {
                                    result.remove(ri22);
                                }
                            }
                        }
                        if (result.size() == 0) {
                            result.add(crossProfileDomainInfo.resolveInfo);
                            return result;
                        }
                    } else if (result.size() <= 1) {
                        return result;
                    }
                    result = filterCandidatesWithDomainPreferredActivitiesLPr(intent, flags, result, crossProfileDomainInfo, userId);
                    Collections.sort(result, mResolvePrioritySorter);
                }
            } else {
                Package pkg = (Package) this.mPackages.get(pkgName);
                if (pkg != null) {
                    filterIfNotSystemUser = filterIfNotSystemUser(this.mActivities.queryIntentForPackage(intent, resolvedType, flags, pkg.activities, userId), userId);
                    return filterIfNotSystemUser;
                }
                List arrayList2 = new ArrayList();
                return arrayList2;
            }
        }
    }

    private CrossProfileDomainInfo getCrossProfileDomainPreferredLpr(Intent intent, String resolvedType, int flags, int sourceUserId, int parentUserId) {
        if (!sUserManager.hasUserRestriction("allow_parent_profile_app_linking", sourceUserId)) {
            return null;
        }
        List<ResolveInfo> resultTargetUser = this.mActivities.queryIntent(intent, resolvedType, flags, parentUserId);
        if (resultTargetUser == null || resultTargetUser.isEmpty()) {
            return null;
        }
        CrossProfileDomainInfo result = null;
        int size = resultTargetUser.size();
        for (int i = 0; i < size; i++) {
            ResolveInfo riTargetUser = (ResolveInfo) resultTargetUser.get(i);
            if (!riTargetUser.handleAllWebDataURI) {
                PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(riTargetUser.activityInfo.packageName);
                if (ps != null) {
                    int status = (int) (getDomainVerificationStatusLPr(ps, parentUserId) >> 32);
                    if (result == null) {
                        result = new CrossProfileDomainInfo();
                        result.resolveInfo = createForwardingResolveInfoUnchecked(new IntentFilter(), sourceUserId, parentUserId);
                        result.bestDomainVerificationStatus = status;
                    } else {
                        result.bestDomainVerificationStatus = bestDomainVerificationStatus(status, result.bestDomainVerificationStatus);
                    }
                }
            }
        }
        if (result == null || result.bestDomainVerificationStatus != 3) {
            return result;
        }
        return null;
    }

    private int bestDomainVerificationStatus(int status1, int status2) {
        if (status1 == 3) {
            return status2;
        }
        if (status2 == 3) {
            return status1;
        }
        return (int) MathUtils.max(status1, status2);
    }

    private boolean isUserEnabled(int userId) {
        long callingId = Binder.clearCallingIdentity();
        try {
            UserInfo userInfo = sUserManager.getUserInfo(userId);
            boolean isEnabled = userInfo != null ? userInfo.isEnabled() : false;
            Binder.restoreCallingIdentity(callingId);
            return isEnabled;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    private List<ResolveInfo> filterIfNotSystemUser(List<ResolveInfo> resolveInfos, int userId) {
        if (userId == 0) {
            return resolveInfos;
        }
        for (int i = resolveInfos.size() - 1; i >= 0; i--) {
            if ((((ResolveInfo) resolveInfos.get(i)).activityInfo.flags & 536870912) != 0) {
                resolveInfos.remove(i);
            }
        }
        return resolveInfos;
    }

    private boolean hasNonNegativePriority(List<ResolveInfo> resolveInfos) {
        return resolveInfos.size() > 0 && ((ResolveInfo) resolveInfos.get(0)).priority >= 0;
    }

    private static boolean hasWebURI(Intent intent) {
        if (intent.getData() == null) {
            return false;
        }
        String scheme = intent.getScheme();
        if (TextUtils.isEmpty(scheme)) {
            return false;
        }
        return !scheme.equals("http") ? scheme.equals("https") : true;
    }

    private List<ResolveInfo> filterCandidatesWithDomainPreferredActivitiesLPr(Intent intent, int matchFlags, List<ResolveInfo> candidates, CrossProfileDomainInfo xpDomainInfo, int userId) {
        boolean debug = (intent.getFlags() & 8) != 0;
        ArrayList<ResolveInfo> result = new ArrayList();
        ArrayList<ResolveInfo> alwaysList = new ArrayList();
        ArrayList<ResolveInfo> undefinedList = new ArrayList();
        ArrayList<ResolveInfo> alwaysAskList = new ArrayList();
        ArrayList<ResolveInfo> neverList = new ArrayList();
        ArrayList<ResolveInfo> matchAllList = new ArrayList();
        synchronized (this.mPackages) {
            int n;
            int count = candidates.size();
            for (n = 0; n < count; n++) {
                ResolveInfo info = (ResolveInfo) candidates.get(n);
                PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(info.activityInfo.packageName);
                if (ps != null) {
                    if (info.handleAllWebDataURI) {
                        matchAllList.add(info);
                    } else {
                        long packedStatus = getDomainVerificationStatusLPr(ps, userId);
                        int status = (int) (packedStatus >> 32);
                        int linkGeneration = (int) (-1 & packedStatus);
                        if (status == 2) {
                            info.preferredOrder = linkGeneration;
                            alwaysList.add(info);
                        } else if (status == 3) {
                            neverList.add(info);
                        } else if (status == 4) {
                            alwaysAskList.add(info);
                        } else if (status == 0 || status == 1) {
                            undefinedList.add(info);
                        }
                    }
                }
            }
            boolean includeBrowser = false;
            if (alwaysList.size() > 0) {
                result.addAll(alwaysList);
            } else {
                result.addAll(undefinedList);
                if (!(xpDomainInfo == null || xpDomainInfo.bestDomainVerificationStatus == 3)) {
                    result.add(xpDomainInfo.resolveInfo);
                }
                includeBrowser = true;
            }
            if (alwaysAskList.size() > 0) {
                for (ResolveInfo i : result) {
                    i.preferredOrder = 0;
                }
                result.addAll(alwaysAskList);
                includeBrowser = true;
            }
            if (includeBrowser) {
                if ((131072 & matchFlags) != 0) {
                    result.addAll(matchAllList);
                } else {
                    String defaultBrowserPackageName = getDefaultBrowserPackageName(userId);
                    int maxMatchPrio = 0;
                    ResolveInfo defaultBrowserMatch = null;
                    int numCandidates = matchAllList.size();
                    for (n = 0; n < numCandidates; n++) {
                        info = (ResolveInfo) matchAllList.get(n);
                        if (info.priority > maxMatchPrio) {
                            maxMatchPrio = info.priority;
                        }
                        if (info.activityInfo.packageName.equals(defaultBrowserPackageName) && (defaultBrowserMatch == null || defaultBrowserMatch.priority < info.priority)) {
                            if (debug) {
                                Slog.v(TAG, "Considering default browser match " + info);
                            }
                            defaultBrowserMatch = info;
                        }
                    }
                    if (defaultBrowserMatch == null || defaultBrowserMatch.priority < maxMatchPrio || TextUtils.isEmpty(defaultBrowserPackageName)) {
                        result.addAll(matchAllList);
                    } else {
                        if (debug) {
                            Slog.v(TAG, "Default browser match " + defaultBrowserMatch);
                        }
                        result.add(defaultBrowserMatch);
                    }
                }
                if (result.size() == 0) {
                    result.addAll(candidates);
                    result.removeAll(neverList);
                }
            }
        }
        return result;
    }

    private long getDomainVerificationStatusLPr(PackageSetting ps, int userId) {
        long result = ps.getDomainVerificationStatusForUser(userId);
        if ((result >> 32) != 0 || ps.getIntentFilterVerificationInfo() == null) {
            return result;
        }
        return ((long) ps.getIntentFilterVerificationInfo().getStatus()) << 32;
    }

    private List<ResolveInfo> querySkipCurrentProfileIntents(List<CrossProfileIntentFilter> matchingFilters, Intent intent, String resolvedType, int flags, int sourceUserId) {
        List<ResolveInfo> result = new ArrayList();
        if (matchingFilters != null) {
            int size = matchingFilters.size();
            for (int i = 0; i < size; i++) {
                CrossProfileIntentFilter filter = (CrossProfileIntentFilter) matchingFilters.get(i);
                if ((filter.getFlags() & 2) != 0) {
                    ResolveInfo resolveInfo = createForwardingResolveInfo(filter, intent, resolvedType, flags, sourceUserId);
                    if (resolveInfo != null) {
                        result.add(resolveInfo);
                    }
                }
            }
        }
        return result;
    }

    private List<ResolveInfo> queryCrossProfileIntents(List<CrossProfileIntentFilter> matchingFilters, Intent intent, String resolvedType, int flags, int sourceUserId, boolean matchInCurrentProfile) {
        List<ResolveInfo> result = new ArrayList();
        if (matchingFilters != null) {
            SparseBooleanArray alreadyTriedUserIds = new SparseBooleanArray();
            int size = matchingFilters.size();
            for (int i = 0; i < size; i++) {
                CrossProfileIntentFilter filter = (CrossProfileIntentFilter) matchingFilters.get(i);
                int targetUserId = filter.getTargetUserId();
                boolean skipCurrentProfile = (filter.getFlags() & 2) != 0;
                boolean skipCurrentProfileIfNoMatchFound = (filter.getFlags() & 4) != 0;
                if (!(skipCurrentProfile || alreadyTriedUserIds.get(targetUserId) || (skipCurrentProfileIfNoMatchFound && matchInCurrentProfile))) {
                    ResolveInfo resolveInfo = createForwardingResolveInfo(filter, intent, resolvedType, flags, sourceUserId);
                    if (resolveInfo != null) {
                        result.add(resolveInfo);
                    }
                    alreadyTriedUserIds.put(targetUserId, true);
                }
            }
        }
        return result;
    }

    private ResolveInfo createForwardingResolveInfo(CrossProfileIntentFilter filter, Intent intent, String resolvedType, int flags, int sourceUserId) {
        int targetUserId = filter.getTargetUserId();
        List<ResolveInfo> resultTargetUser = this.mActivities.queryIntent(intent, resolvedType, flags, targetUserId);
        if (resultTargetUser != null && isUserEnabled(targetUserId)) {
            for (int i = resultTargetUser.size() - 1; i >= 0; i--) {
                if ((((ResolveInfo) resultTargetUser.get(i)).activityInfo.applicationInfo.flags & 1073741824) == 0) {
                    return createForwardingResolveInfoUnchecked(filter, sourceUserId, targetUserId);
                }
            }
        }
        return null;
    }

    protected ResolveInfo createForwardingResolveInfoUnchecked(IntentFilter filter, int sourceUserId, int targetUserId) {
        ResolveInfo forwardingResolveInfo = new ResolveInfo();
        long ident = Binder.clearCallingIdentity();
        try {
            String className;
            boolean targetIsProfile = sUserManager.getUserInfo(targetUserId).isManagedProfile();
            if (targetIsProfile) {
                className = IntentForwarderActivity.FORWARD_INTENT_TO_MANAGED_PROFILE;
            } else {
                className = IntentForwarderActivity.FORWARD_INTENT_TO_PARENT;
            }
            ActivityInfo forwardingActivityInfo = getActivityInfo(new ComponentName(this.mAndroidApplication.packageName, className), 0, sourceUserId);
            if (!targetIsProfile) {
                forwardingActivityInfo.showUserIcon = targetUserId;
                forwardingResolveInfo.noResourceId = true;
            }
            forwardingResolveInfo.activityInfo = forwardingActivityInfo;
            forwardingResolveInfo.priority = 0;
            forwardingResolveInfo.preferredOrder = 0;
            forwardingResolveInfo.match = 0;
            forwardingResolveInfo.isDefault = true;
            forwardingResolveInfo.filter = filter;
            forwardingResolveInfo.targetUserId = targetUserId;
            return forwardingResolveInfo;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public ParceledListSlice<ResolveInfo> queryIntentActivityOptions(ComponentName caller, Intent[] specifics, String[] specificTypes, Intent intent, String resolvedType, int flags, int userId) {
        return new ParceledListSlice(queryIntentActivityOptionsInternal(caller, specifics, specificTypes, intent, resolvedType, flags, userId));
    }

    private List<ResolveInfo> queryIntentActivityOptionsInternal(ComponentName caller, Intent[] specifics, String[] specificTypes, Intent intent, String resolvedType, int flags, int userId) {
        if (!sUserManager.exists(userId)) {
            return Collections.emptyList();
        }
        int i;
        String action;
        int N;
        int j;
        flags = updateFlagsForResolve(flags, userId, intent);
        enforceCrossUserPermission(Binder.getCallingUid(), userId, false, false, "query intent activity options");
        String resultsAction = intent.getAction();
        List<ResolveInfo> results = queryIntentActivitiesInternal(intent, resolvedType, flags | 64, userId);
        int specificsPos = 0;
        if (specifics != null) {
            i = 0;
            while (i < specifics.length) {
                Intent sintent = specifics[i];
                if (sintent != null) {
                    ActivityInfo ai;
                    action = sintent.getAction();
                    if (resultsAction != null && resultsAction.equals(action)) {
                        action = null;
                    }
                    ResolveInfo resolveInfo = null;
                    ComponentName comp = sintent.getComponent();
                    if (comp == null) {
                        resolveInfo = resolveIntent(sintent, specificTypes != null ? specificTypes[i] : null, flags, userId);
                        if (resolveInfo != null) {
                            if (resolveInfo == this.mResolveInfo) {
                                ai = resolveInfo.activityInfo;
                                comp = new ComponentName(ai.applicationInfo.packageName, ai.name);
                            } else {
                                ai = resolveInfo.activityInfo;
                                comp = new ComponentName(ai.applicationInfo.packageName, ai.name);
                            }
                        }
                    } else {
                        ai = getActivityInfo(comp, flags, userId);
                        if (ai == null) {
                        }
                    }
                    N = results.size();
                    j = specificsPos;
                    while (j < N) {
                        ResolveInfo sri = (ResolveInfo) results.get(j);
                        if ((sri.activityInfo.name.equals(comp.getClassName()) && sri.activityInfo.applicationInfo.packageName.equals(comp.getPackageName())) || (r11 != null && sri.filter.matchAction(r11))) {
                            results.remove(j);
                            if (resolveInfo == null) {
                                resolveInfo = sri;
                            }
                            j--;
                            N--;
                        }
                        j++;
                    }
                    if (resolveInfo == null) {
                        resolveInfo = new ResolveInfo();
                        resolveInfo.activityInfo = ai;
                    }
                    results.add(specificsPos, resolveInfo);
                    resolveInfo.specificIndex = i;
                    specificsPos++;
                }
                i++;
            }
        }
        N = results.size();
        for (i = specificsPos; i < N - 1; i++) {
            ResolveInfo rii = (ResolveInfo) results.get(i);
            if (rii.filter != null) {
                Iterator<String> it = rii.filter.actionsIterator();
                if (it != null) {
                    while (it.hasNext()) {
                        action = (String) it.next();
                        if (resultsAction == null || !resultsAction.equals(action)) {
                            j = i + 1;
                            while (j < N) {
                                ResolveInfo rij = (ResolveInfo) results.get(j);
                                if (rij.filter != null && rij.filter.hasAction(action)) {
                                    results.remove(j);
                                    j--;
                                    N--;
                                }
                                j++;
                            }
                        }
                    }
                    if ((flags & 64) == 0) {
                        rii.filter = null;
                    }
                }
            }
        }
        if (caller != null) {
            N = results.size();
            for (i = 0; i < N; i++) {
                ActivityInfo ainfo = ((ResolveInfo) results.get(i)).activityInfo;
                if (caller.getPackageName().equals(ainfo.applicationInfo.packageName) && caller.getClassName().equals(ainfo.name)) {
                    results.remove(i);
                    break;
                }
            }
        }
        if ((flags & 64) == 0) {
            N = results.size();
            for (i = 0; i < N; i++) {
                ((ResolveInfo) results.get(i)).filter = null;
            }
        }
        return results;
    }

    public ParceledListSlice<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, int flags, int userId) {
        return new ParceledListSlice(queryIntentReceiversInternal(intent, resolvedType, flags, userId));
    }

    private List<ResolveInfo> queryIntentReceiversInternal(Intent intent, String resolvedType, int flags, int userId) {
        if (!sUserManager.exists(userId)) {
            return Collections.emptyList();
        }
        flags = updateFlagsForResolve(flags, userId, intent);
        ComponentName comp = intent.getComponent();
        if (comp == null && intent.getSelector() != null) {
            intent = intent.getSelector();
            comp = intent.getComponent();
        }
        if (comp != null) {
            List<ResolveInfo> list = new ArrayList(1);
            ActivityInfo ai = getReceiverInfo(comp, flags, userId);
            if (ai != null) {
                ResolveInfo ri = new ResolveInfo();
                ri.activityInfo = ai;
                list.add(ri);
            }
            return list;
        }
        synchronized (this.mPackages) {
            String pkgName = intent.getPackage();
            if (pkgName == null) {
                List<ResolveInfo> queryIntent = this.mReceivers.queryIntent(intent, resolvedType, flags, userId);
                return queryIntent;
            }
            Package pkg = (Package) this.mPackages.get(pkgName);
            if (pkg != null) {
                queryIntent = this.mReceivers.queryIntentForPackage(intent, resolvedType, flags, pkg.receivers, userId);
                return queryIntent;
            }
            queryIntent = Collections.emptyList();
            return queryIntent;
        }
    }

    public ResolveInfo resolveService(Intent intent, String resolvedType, int flags, int userId) {
        if (!sUserManager.exists(userId)) {
            return null;
        }
        List<ResolveInfo> query = queryIntentServicesInternal(intent, resolvedType, updateFlagsForResolve(flags, userId, intent), userId);
        if (query == null || query.size() < 1) {
            return null;
        }
        return (ResolveInfo) query.get(0);
    }

    public ParceledListSlice<ResolveInfo> queryIntentServices(Intent intent, String resolvedType, int flags, int userId) {
        return new ParceledListSlice(queryIntentServicesInternal(intent, resolvedType, flags, userId));
    }

    private List<ResolveInfo> queryIntentServicesInternal(Intent intent, String resolvedType, int flags, int userId) {
        if (!sUserManager.exists(userId)) {
            return Collections.emptyList();
        }
        flags = updateFlagsForResolve(flags, userId, intent);
        ComponentName comp = intent.getComponent();
        if (comp == null && intent.getSelector() != null) {
            intent = intent.getSelector();
            comp = intent.getComponent();
        }
        if (comp != null) {
            List<ResolveInfo> list = new ArrayList(1);
            ServiceInfo si = getServiceInfo(comp, flags, userId);
            if (si != null) {
                ResolveInfo ri = new ResolveInfo();
                ri.serviceInfo = si;
                list.add(ri);
            }
            return list;
        }
        synchronized (this.mPackages) {
            String pkgName = intent.getPackage();
            if (pkgName == null) {
                List<ResolveInfo> queryIntent = this.mServices.queryIntent(intent, resolvedType, flags, userId);
                return queryIntent;
            }
            Package pkg = (Package) this.mPackages.get(pkgName);
            if (pkg != null) {
                queryIntent = this.mServices.queryIntentForPackage(intent, resolvedType, flags, pkg.services, userId);
                return queryIntent;
            }
            queryIntent = Collections.emptyList();
            return queryIntent;
        }
    }

    public ParceledListSlice<ResolveInfo> queryIntentContentProviders(Intent intent, String resolvedType, int flags, int userId) {
        return new ParceledListSlice(queryIntentContentProvidersInternal(intent, resolvedType, flags, userId));
    }

    private List<ResolveInfo> queryIntentContentProvidersInternal(Intent intent, String resolvedType, int flags, int userId) {
        if (!sUserManager.exists(userId)) {
            return Collections.emptyList();
        }
        flags = updateFlagsForResolve(flags, userId, intent);
        ComponentName comp = intent.getComponent();
        if (comp == null && intent.getSelector() != null) {
            intent = intent.getSelector();
            comp = intent.getComponent();
        }
        if (comp != null) {
            List<ResolveInfo> list = new ArrayList(1);
            ProviderInfo pi = getProviderInfo(comp, flags, userId);
            if (pi != null) {
                ResolveInfo ri = new ResolveInfo();
                ri.providerInfo = pi;
                list.add(ri);
            }
            return list;
        }
        synchronized (this.mPackages) {
            String pkgName = intent.getPackage();
            if (pkgName == null) {
                List<ResolveInfo> queryIntent = this.mProviders.queryIntent(intent, resolvedType, flags, userId);
                return queryIntent;
            }
            Package pkg = (Package) this.mPackages.get(pkgName);
            if (pkg != null) {
                queryIntent = this.mProviders.queryIntentForPackage(intent, resolvedType, flags, pkg.providers, userId);
                return queryIntent;
            }
            queryIntent = Collections.emptyList();
            return queryIntent;
        }
    }

    public ParceledListSlice<PackageInfo> getInstalledPackages(int flags, int userId) {
        if (!sUserManager.exists(userId)) {
            return ParceledListSlice.emptyList();
        }
        ParceledListSlice<PackageInfo> parceledListSlice;
        flags = updateFlagsForPackage(flags, userId, null);
        boolean listUninstalled = (flags & 8192) != 0;
        enforceCrossUserPermission(Binder.getCallingUid(), userId, true, false, "get installed packages");
        synchronized (this.mPackages) {
            ArrayList<PackageInfo> list;
            Object pi;
            if (listUninstalled) {
                list = new ArrayList(this.mSettings.mPackages.size());
                for (PackageSetting ps : this.mSettings.mPackages.values()) {
                    pi = null;
                    if (ps.pkg == null) {
                        pi = generatePackageInfo(ps, flags, userId);
                    } else if (!isHwCustHiddenInfoPackage(ps.pkg)) {
                        pi = generatePackageInfo(ps, flags, userId);
                    }
                    if (pi != null) {
                        list.add(pi);
                    }
                }
            } else {
                list = new ArrayList(this.mPackages.size());
                for (Package p : this.mPackages.values()) {
                    pi = null;
                    if (!isHwCustHiddenInfoPackage(p)) {
                        pi = generatePackageInfo((PackageSetting) p.mExtras, flags, userId);
                    }
                    if (pi != null) {
                        list.add(pi);
                    }
                }
            }
            parceledListSlice = new ParceledListSlice(list);
        }
        return parceledListSlice;
    }

    private void addPackageHoldingPermissions(ArrayList<PackageInfo> list, PackageSetting ps, String[] permissions, boolean[] tmp, int flags, int userId) {
        int i;
        int numMatch = 0;
        PermissionsState permissionsState = ps.getPermissionsState();
        for (i = 0; i < permissions.length; i++) {
            if (permissionsState.hasPermission(permissions[i], userId)) {
                tmp[i] = true;
                numMatch++;
            } else {
                tmp[i] = false;
            }
        }
        if (numMatch != 0) {
            PackageInfo pi;
            if (ps.pkg != null) {
                pi = generatePackageInfo(ps, flags, userId);
            } else {
                pi = generatePackageInfo(ps, flags, userId);
            }
            if (pi != null) {
                if ((flags & 4096) == 0) {
                    if (numMatch == permissions.length) {
                        pi.requestedPermissions = permissions;
                    } else {
                        pi.requestedPermissions = new String[numMatch];
                        numMatch = 0;
                        for (i = 0; i < permissions.length; i++) {
                            if (tmp[i]) {
                                pi.requestedPermissions[numMatch] = permissions[i];
                                numMatch++;
                            }
                        }
                    }
                }
                list.add(pi);
            }
        }
    }

    public ParceledListSlice<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags, int userId) {
        if (!sUserManager.exists(userId)) {
            return ParceledListSlice.emptyList();
        }
        ParceledListSlice<PackageInfo> parceledListSlice;
        flags = updateFlagsForPackage(flags, userId, permissions);
        boolean listUninstalled = (flags & 8192) != 0;
        synchronized (this.mPackages) {
            ArrayList<PackageInfo> list = new ArrayList();
            boolean[] tmpBools = new boolean[permissions.length];
            PackageSetting ps;
            if (listUninstalled) {
                for (PackageSetting ps2 : this.mSettings.mPackages.values()) {
                    addPackageHoldingPermissions(list, ps2, permissions, tmpBools, flags, userId);
                }
            } else {
                for (Package pkg : this.mPackages.values()) {
                    ps2 = (PackageSetting) pkg.mExtras;
                    if (ps2 != null) {
                        addPackageHoldingPermissions(list, ps2, permissions, tmpBools, flags, userId);
                    }
                }
            }
            parceledListSlice = new ParceledListSlice(list);
        }
        return parceledListSlice;
    }

    public ParceledListSlice<ApplicationInfo> getInstalledApplications(int flags, int userId) {
        if (!sUserManager.exists(userId)) {
            return ParceledListSlice.emptyList();
        }
        ParceledListSlice<ApplicationInfo> parceledListSlice;
        flags = updateFlagsForApplication(flags, userId, null);
        boolean listUninstalled = (flags & 8192) != 0;
        synchronized (this.mPackages) {
            ArrayList<ApplicationInfo> list;
            Object ai;
            if (listUninstalled) {
                list = new ArrayList(this.mSettings.mPackages.size());
                for (PackageSetting ps : this.mSettings.mPackages.values()) {
                    ai = null;
                    if (ps.pkg == null) {
                        ai = generateApplicationInfoFromSettingsLPw(ps.name, flags, userId);
                    } else if (!isHwCustHiddenInfoPackage(ps.pkg)) {
                        ai = PackageParser.generateApplicationInfo(ps.pkg, flags, ps.readUserState(userId), userId);
                    }
                    if (ai != null) {
                        list.add(ai);
                    }
                }
            } else {
                list = new ArrayList(this.mPackages.size());
                for (Package p : this.mPackages.values()) {
                    if (p.mExtras != null) {
                        ai = null;
                        if (!isHwCustHiddenInfoPackage(p)) {
                            ai = PackageParser.generateApplicationInfo(p, flags, ((PackageSetting) p.mExtras).readUserState(userId), userId);
                        }
                        if (ai != null) {
                            list.add(ai);
                        }
                    }
                }
            }
            parceledListSlice = new ParceledListSlice(list);
        }
        return parceledListSlice;
    }

    public ParceledListSlice<EphemeralApplicationInfo> getEphemeralApplications(int userId) {
        return null;
    }

    public boolean isEphemeralApplication(String packageName, int userId) {
        enforceCrossUserPermission(Binder.getCallingUid(), userId, true, false, "isEphemeral");
        return false;
    }

    public byte[] getEphemeralApplicationCookie(String packageName, int userId) {
        return null;
    }

    public boolean setEphemeralApplicationCookie(String packageName, byte[] cookie, int userId) {
        return true;
    }

    public Bitmap getEphemeralApplicationIcon(String packageName, int userId) {
        return null;
    }

    private boolean isCallerSameApp(String packageName) {
        Package pkg = (Package) this.mPackages.get(packageName);
        if (pkg == null || UserHandle.getAppId(Binder.getCallingUid()) != pkg.applicationInfo.uid) {
            return false;
        }
        return true;
    }

    public ParceledListSlice<ApplicationInfo> getPersistentApplications(int flags) {
        return new ParceledListSlice(getPersistentApplicationsInternal(flags));
    }

    private List<ApplicationInfo> getPersistentApplicationsInternal(int flags) {
        ArrayList<ApplicationInfo> finalList = new ArrayList();
        synchronized (this.mPackages) {
            int userId = UserHandle.getCallingUserId();
            for (Package p : this.mPackages.values()) {
                if (p.applicationInfo != null) {
                    boolean matchesUnaware = (DumpState.DUMP_DOMAIN_PREFERRED & flags) != 0 ? !p.applicationInfo.isDirectBootAware() : false;
                    boolean isDirectBootAware;
                    if ((DumpState.DUMP_FROZEN & flags) != 0) {
                        isDirectBootAware = p.applicationInfo.isDirectBootAware();
                    } else {
                        isDirectBootAware = false;
                    }
                    if ((p.applicationInfo.flags & 8) != 0 && ((!this.mSafeMode || isSystemApp(p)) && (matchesUnaware || r3))) {
                        PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(p.packageName);
                        if (ps != null) {
                            ApplicationInfo ai = PackageParser.generateApplicationInfo(p, flags, ps.readUserState(userId), userId);
                            if (ai != null) {
                                finalList.add(ai);
                            }
                        }
                    }
                }
            }
        }
        return finalList;
    }

    public ProviderInfo resolveContentProvider(String name, int flags, int userId) {
        ProviderInfo providerInfo = null;
        if (!sUserManager.exists(userId)) {
            return null;
        }
        flags = updateFlagsForComponent(flags, userId, name);
        synchronized (this.mPackages) {
            PackageSetting packageSetting;
            Provider provider = (Provider) this.mProvidersByAuthority.get(name);
            if (provider != null) {
                packageSetting = (PackageSetting) this.mSettings.mPackages.get(provider.owner.packageName);
            } else {
                packageSetting = null;
            }
            if (packageSetting != null && this.mSettings.isEnabledAndMatchLPr(provider.info, flags, userId)) {
                providerInfo = PackageParser.generateProviderInfo(provider, flags, packageSetting.readUserState(userId), userId);
            }
        }
        return providerInfo;
    }

    @Deprecated
    public void querySyncProviders(List<String> outNames, List<ProviderInfo> outInfo) {
        synchronized (this.mPackages) {
            int userId = UserHandle.getCallingUserId();
            for (Entry<String, Provider> entry : this.mProvidersByAuthority.entrySet()) {
                Provider p = (Provider) entry.getValue();
                PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(p.owner.packageName);
                if (ps != null && p.syncable) {
                    if (!this.mSafeMode || (p.info.applicationInfo.flags & 1) != 0) {
                        ProviderInfo info = PackageParser.generateProviderInfo(p, 0, ps.readUserState(userId), userId);
                        if (info != null) {
                            outNames.add((String) entry.getKey());
                            outInfo.add(info);
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ParceledListSlice<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
        int userId;
        Throwable th;
        if (processName != null) {
            userId = UserHandle.getUserId(uid);
        } else {
            userId = UserHandle.getCallingUserId();
        }
        if (!sUserManager.exists(userId)) {
            return ParceledListSlice.emptyList();
        }
        flags = updateFlagsForComponent(flags, userId, processName);
        synchronized (this.mPackages) {
            try {
                ArrayList<ProviderInfo> finalList = null;
                for (Provider p : this.mProviders.mProviders.values()) {
                    ArrayList<ProviderInfo> finalList2;
                    try {
                        PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(p.owner.packageName);
                        if (ps == null || p.info.authority == null || !(processName == null || (p.info.processName.equals(processName) && UserHandle.isSameApp(p.info.applicationInfo.uid, uid)))) {
                            finalList2 = finalList;
                        } else if (this.mSettings.isEnabledAndMatchLPr(p.info, flags, userId)) {
                            if (finalList == null) {
                                finalList2 = new ArrayList(3);
                            } else {
                                finalList2 = finalList;
                            }
                            ProviderInfo info = PackageParser.generateProviderInfo(p, flags, ps.readUserState(userId), userId);
                            if (info != null) {
                                finalList2.add(info);
                            }
                        } else {
                            finalList2 = finalList;
                        }
                        finalList = finalList2;
                    } catch (Throwable th2) {
                        th = th2;
                        finalList2 = finalList;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }
        throw th;
    }

    public InstrumentationInfo getInstrumentationInfo(ComponentName name, int flags) {
        InstrumentationInfo generateInstrumentationInfo;
        synchronized (this.mPackages) {
            generateInstrumentationInfo = PackageParser.generateInstrumentationInfo((Instrumentation) this.mInstrumentation.get(name), flags);
        }
        return generateInstrumentationInfo;
    }

    public ParceledListSlice<InstrumentationInfo> queryInstrumentation(String targetPackage, int flags) {
        return new ParceledListSlice(queryInstrumentationInternal(targetPackage, flags));
    }

    private List<InstrumentationInfo> queryInstrumentationInternal(String targetPackage, int flags) {
        ArrayList<InstrumentationInfo> finalList = new ArrayList();
        synchronized (this.mPackages) {
            for (Instrumentation p : this.mInstrumentation.values()) {
                if (targetPackage == null || targetPackage.equals(p.info.targetPackage)) {
                    InstrumentationInfo ii = PackageParser.generateInstrumentationInfo(p, flags);
                    if (ii != null) {
                        finalList.add(ii);
                    }
                }
            }
        }
        return finalList;
    }

    private void createIdmapsForPackageLI(Package pkg) {
        ArrayMap<String, Package> overlays = (ArrayMap) this.mOverlays.get(pkg.packageName);
        if (overlays == null) {
            Slog.w(TAG, "Unable to create idmap for " + pkg.packageName + ": no overlay packages");
            return;
        }
        for (Package opkg : overlays.values()) {
            createIdmapForPackagePairLI(pkg, opkg);
        }
    }

    private boolean createIdmapForPackagePairLI(Package pkg, Package opkg) {
        int i = 0;
        if (opkg.mTrustedOverlay) {
            ArrayMap<String, Package> overlaySet = (ArrayMap) this.mOverlays.get(pkg.packageName);
            if (overlaySet == null) {
                Slog.e(TAG, "was about to create idmap for " + pkg.baseCodePath + " and " + opkg.baseCodePath + " but target package has no known overlays");
                return false;
            }
            try {
                this.mInstaller.idmap(pkg.baseCodePath, opkg.baseCodePath, UserHandle.getSharedAppGid(pkg.applicationInfo.uid));
                Package[] overlayArray = (Package[]) overlaySet.values().toArray(new Package[0]);
                Arrays.sort(overlayArray, new Comparator<Package>() {
                    public int compare(Package p1, Package p2) {
                        return p1.mOverlayPriority - p2.mOverlayPriority;
                    }
                });
                pkg.applicationInfo.resourceDirs = new String[overlayArray.length];
                int length = overlayArray.length;
                int i2 = 0;
                while (i < length) {
                    int i3 = i2 + 1;
                    pkg.applicationInfo.resourceDirs[i2] = overlayArray[i].baseCodePath;
                    i++;
                    i2 = i3;
                }
                return true;
            } catch (InstallerException e) {
                Slog.e(TAG, "Failed to generate idmap for " + pkg.baseCodePath + " and " + opkg.baseCodePath);
                return false;
            }
        }
        Slog.w(TAG, "Skipping target and overlay pair " + pkg.baseCodePath + " and " + opkg.baseCodePath + ": overlay not trusted");
        return false;
    }

    private void scanDirTracedLI(File dir, int parseFlags, int scanFlags, long currentTime) {
        Trace.traceBegin(262144, "scanDir");
        try {
            scanDirLI(dir, parseFlags, scanFlags, currentTime);
        } finally {
            Trace.traceEnd(262144);
        }
    }

    protected void scanDirLI(File dir, int parseFlags, int scanFlags, long currentTime) {
        scanDirLI(dir, parseFlags, scanFlags, currentTime, 0);
    }

    protected void scanDirLI(File dir, int parseFlags, int scanFlags, long currentTime, int hwFlags) {
        if (HWFLOW) {
            this.startTimer = SystemClock.uptimeMillis();
        }
        File[] files = dir.listFiles();
        if (ArrayUtils.isEmpty(files)) {
            Log.d(TAG, "No files in app dir " + dir);
            return;
        }
        final int i;
        ExecutorService executorService = Executors.newFixedThreadPool(mThreadnum);
        for (final File file : files) {
            boolean isPackage = (PackageParser.isApkFile(file) || file.isDirectory()) ? !PackageInstallerService.isStageName(file.getName()) : false;
            if (isPackage && !isUninstallApk(file.getPath() + ".apk")) {
                HwCustEmergDataManager emergDataManager = HwCustEmergDataManager.getDefault();
                if (emergDataManager == null || emergDataManager.isEmergencyState() || !emergDataManager.getEmergencyPkgName().contains(file.getName())) {
                    if (this.mIsPackageScanMultiThread) {
                        final int i2 = parseFlags;
                        i = scanFlags;
                        final long j = currentTime;
                        final int i3 = hwFlags;
                        try {
                            executorService.submit(new Runnable() {
                                public void run() {
                                    try {
                                        PackageManagerService.this.scanPackageTracedLI(file, i2 | 4, i, j, null, i3);
                                    } catch (PackageManagerException e) {
                                        Slog.w(PackageManagerService.TAG, "Failed to parse " + file + ": " + e.getMessage());
                                        if ((i2 & 1) == 0 && e.error == -2) {
                                            PackageManagerService.logCriticalInfo(5, "Deleting invalid package at " + file);
                                            PackageManagerService.this.removeCodePathLI(file);
                                        }
                                    }
                                }
                            });
                        } catch (Exception e) {
                            logCriticalInfo(5, "can't submit task");
                            this.mIsPackageScanMultiThread = false;
                        }
                    }
                    if (!this.mIsPackageScanMultiThread) {
                        try {
                            scanPackageTracedLI(file, parseFlags | 4, scanFlags, currentTime, null, hwFlags);
                        } catch (PackageManagerException e2) {
                            Slog.w(TAG, "Failed to parse " + file + ": " + e2.getMessage());
                            if ((parseFlags & 1) == 0 && e2.error == -2) {
                                logCriticalInfo(5, "Deleting invalid package at " + file);
                                removeCodePathLI(file);
                            }
                        }
                    }
                } else {
                    Log.i(TAG, "dont scan EmergencyData.apk");
                }
            }
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e3) {
            e3.printStackTrace();
        }
        if (HWFLOW) {
            String str = TAG;
            StringBuilder append = new StringBuilder().append("TimerCounter = ");
            i = this.mTimerCounter + 1;
            this.mTimerCounter = i;
            Slog.i(str, append.append(i).append(" **** ScanDir = ").append(dir.getName()).append(" *** fileNum = ").append(files.length).append("************ Time to elapsed: ").append(SystemClock.uptimeMillis() - this.startTimer).append(" ms").toString());
        }
    }

    private static File getSettingsProblemFile() {
        return new File(new File(Environment.getDataDirectory(), "system"), "uiderrors.txt");
    }

    static void reportSettingsProblem(int priority, String msg) {
        logCriticalInfo(priority, msg);
    }

    static void logCriticalInfo(int priority, String msg) {
        Slog.println(priority, TAG, msg);
        EventLogTags.writePmCriticalInfo(msg);
        try {
            File fname = getSettingsProblemFile();
            PrintWriter pw = new FastPrintWriter(new FileOutputStream(fname, true));
            pw.println(new SimpleDateFormat().format(new Date(System.currentTimeMillis())) + ": " + msg);
            pw.close();
            FileUtils.setPermissions(fname.toString(), 508, -1, -1);
        } catch (IOException e) {
        }
    }

    private void collectCertificatesLI(PackageSetting ps, Package pkg, File srcFile, int policyFlags) throws PackageManagerException {
        if (ps == null || !ps.codePath.equals(srcFile) || ps.timeStamp != srcFile.lastModified() || isCompatSignatureUpdateNeeded(pkg) || isRecoverSignatureUpdateNeeded(pkg)) {
            Log.i(TAG, srcFile.toString() + " changed; collecting certs");
        } else {
            ArraySet<PublicKey> signingKs;
            long mSigningKeySetId = ps.keySetData.getProperSigningKeySet();
            KeySetManagerService ksms = this.mSettings.mKeySetManagerService;
            synchronized (this.mPackages) {
                signingKs = ksms.getPublicKeysFromKeySetLPr(mSigningKeySetId);
            }
            if (ps.signatures.mSignatures == null || ps.signatures.mSignatures.length == 0 || signingKs == null) {
                Slog.w(TAG, "PackageSetting for " + ps.name + " is missing signatures.  Collecting certs again to recover them.");
            } else {
                pkg.mSignatures = ps.signatures.mSignatures;
                pkg.mSigningKeys = signingKs;
                return;
            }
        }
        try {
            PackageParser.collectCertificates(pkg, policyFlags);
        } catch (PackageParserException e) {
            throw PackageManagerException.from(e);
        }
    }

    private Package scanPackageTracedLI(File scanFile, int parseFlags, int scanFlags, long currentTime, UserHandle user) throws PackageManagerException {
        return scanPackageTracedLI(scanFile, parseFlags, scanFlags, currentTime, user, 0);
    }

    private Package scanPackageTracedLI(File scanFile, int parseFlags, int scanFlags, long currentTime, UserHandle user, int hwFlags) throws PackageManagerException {
        Trace.traceBegin(262144, "scanPackage");
        try {
            Package scanPackageLI = scanPackageLI(scanFile, parseFlags, scanFlags, currentTime, user, hwFlags);
            return scanPackageLI;
        } finally {
            Trace.traceEnd(262144);
        }
    }

    protected Package scanPackageLI(File scanFile, int parseFlags, int scanFlags, long currentTime, UserHandle user) throws PackageManagerException {
        return scanPackageLI(scanFile, parseFlags, scanFlags, currentTime, user, 0);
    }

    protected Package scanPackageLI(File scanFile, int parseFlags, int scanFlags, long currentTime, UserHandle user, int hwFlags) throws PackageManagerException {
        if (isUninstallApk(scanFile.getPath() + ".apk")) {
            return null;
        }
        PackageParser pp = new PackageParser();
        pp.setSeparateProcesses(this.mSeparateProcesses);
        pp.setOnlyCoreApps(this.mOnlyCore);
        pp.setDisplayMetrics(this.mMetrics);
        if ((scanFlags & 512) != 0) {
            parseFlags |= 512;
        }
        if ((134217728 & hwFlags) != 0 && !isCustApkRecorded(scanFile)) {
            return null;
        }
        Trace.traceBegin(262144, "parsePackage");
        try {
            Package pkg = pp.parsePackage(scanFile, parseFlags, hwFlags);
            if (pkg != null && isInMultiWinWhiteList(pkg.packageName)) {
                pkg.forceResizeableAllActivity();
            }
            Trace.traceEnd(262144);
            return scanPackageLI(pkg, scanFile, parseFlags, scanFlags, currentTime, user, hwFlags);
        } catch (PackageParserException e) {
            throw PackageManagerException.from(e);
        } catch (Throwable th) {
            Trace.traceEnd(262144);
        }
    }

    private Package scanPackageLI(Package pkg, File scanFile, int policyFlags, int scanFlags, long currentTime, UserHandle user) throws PackageManagerException {
        return scanPackageLI(pkg, scanFile, policyFlags, scanFlags, currentTime, user, 0);
    }

    private Package scanPackageLI(Package pkg, File scanFile, int policyFlags, int scanFlags, long currentTime, UserHandle user, int hwFlags) throws PackageManagerException {
        if ((DumpState.DUMP_VERSION & scanFlags) != 0) {
            scanFlags &= -32769;
        } else if (pkg.childPackages != null && pkg.childPackages.size() > 0) {
            scanFlags |= DumpState.DUMP_VERSION;
        }
        Package scannedPkg = scanPackageInternalLI(pkg, scanFile, policyFlags, scanFlags, currentTime, user, hwFlags);
        int childCount = pkg.childPackages != null ? pkg.childPackages.size() : 0;
        for (int i = 0; i < childCount; i++) {
            scanPackageInternalLI((Package) pkg.childPackages.get(i), scanFile, policyFlags, scanFlags, currentTime, user, hwFlags);
        }
        if ((DumpState.DUMP_VERSION & scanFlags) != 0) {
            return scanPackageLI(pkg, scanFile, policyFlags, scanFlags, currentTime, user, hwFlags);
        }
        return scannedPkg;
    }

    private Package scanPackageInternalLI(Package pkg, File scanFile, int policyFlags, int scanFlags, long currentTime, UserHandle user) throws PackageManagerException {
        return scanPackageInternalLI(pkg, scanFile, policyFlags, scanFlags, currentTime, user, 0);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Package scanPackageInternalLI(Package pkg, File scanFile, int policyFlags, int scanFlags, long currentTime, UserHandle user, int hwFlags) throws PackageManagerException {
        Throwable th;
        synchronized (this.mPackages) {
            if (!needInstallRemovablePreApk(pkg, hwFlags)) {
                return null;
            }
        }
        if (r24 != null) {
            try {
                r24.close();
            } catch (Throwable th2) {
                if (th == null) {
                    th = th2;
                } else if (th != th2) {
                    th.addSuppressed(th2);
                }
            }
        }
        if (th != null) {
            throw th;
        }
        throw r4;
        if (r37 != null) {
            throw r37;
        }
        PackageSetting packageSetting = null;
        if (!((policyFlags & 64) != 0 || packageSetting == null || packageSetting.codePath.equals(packageSetting.resourcePath))) {
            policyFlags |= 16;
        }
        String str = null;
        String baseResourcePath = null;
        if ((policyFlags & 16) == 0 || updatedPkgBetter) {
            str = pkg.codePath;
            baseResourcePath = pkg.baseCodePath;
        } else if (packageSetting == null || packageSetting.resourcePathString == null) {
            Slog.e(TAG, "Resource path not set for package " + pkg.packageName);
        } else {
            str = packageSetting.resourcePathString;
            baseResourcePath = packageSetting.resourcePathString;
        }
        if (str == null || !new File(str).exists()) {
            Slog.e(TAG, "ResourcePath invalid! Change from " + str + " to " + pkg.codePath);
            str = pkg.codePath;
            baseResourcePath = pkg.baseCodePath;
        }
        pkg.setApplicationVolumeUuid(pkg.volumeUuid);
        pkg.setApplicationInfoCodePath(pkg.codePath);
        pkg.setApplicationInfoBaseCodePath(pkg.baseCodePath);
        pkg.setApplicationInfoSplitCodePaths(pkg.splitCodePaths);
        pkg.setApplicationInfoResourcePath(str);
        pkg.setApplicationInfoBaseResourcePath(baseResourcePath);
        pkg.setApplicationInfoSplitResourcePaths(pkg.splitCodePaths);
        Package scannedPkg = scanPackageLI(pkg, policyFlags, scanFlags | 8, currentTime, user, hwFlags);
        if (shouldHideSystemApp) {
            synchronized (this.mPackages) {
                this.mSettings.disableSystemPackageLPw(pkg.packageName, true);
            }
        }
        addPreinstalledPkgToList(scannedPkg);
        return scannedPkg;
    }

    private static String fixProcessName(String defProcessName, String processName, int uid) {
        if (processName == null) {
            return defProcessName;
        }
        return processName;
    }

    private void verifySignaturesLP(PackageSetting pkgSetting, Package pkg) throws PackageManagerException {
        boolean match;
        if (pkgSetting.signatures.mSignatures != null) {
            match = compareSignatures(pkgSetting.signatures.mSignatures, pkg.mSignatures) == 0;
            if (!match) {
                match = compareSignaturesCompat(pkgSetting.signatures, pkg) == 0;
            }
            if (!match) {
                match = compareSignaturesRecover(pkgSetting.signatures, pkg) == 0;
            }
            if (!match) {
                throw new PackageManagerException(-7, "Package " + pkg.packageName + " signatures do not match the " + "previously installed version; ignoring!");
            }
        }
        if (pkgSetting.sharedUser != null && pkgSetting.sharedUser.signatures.mSignatures != null) {
            match = compareSignatures(pkgSetting.sharedUser.signatures.mSignatures, pkg.mSignatures) == 0;
            if (!match) {
                match = compareSignaturesCompat(pkgSetting.sharedUser.signatures, pkg) == 0;
            }
            if (!match) {
                match = compareSignaturesRecover(pkgSetting.sharedUser.signatures, pkg) == 0;
            }
            if (!match) {
                throw new PackageManagerException(-8, "Package " + pkg.packageName + " has no signatures that match those in shared user " + pkgSetting.sharedUser.name + "; ignoring!");
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void verifyValidVerifierInstall(String installerPackageName, String pkgName, int userId, int appId) throws PackageManagerException {
        String checkInstallPackage = "com.android.vending";
        if (pkgName.equals(checkInstallPackage) && ((TextUtils.isEmpty(installerPackageName) || (!installerPackageName.equals("com.android.packageinstaller") && !installerPackageName.equals("com.huawei.appmarket"))) && pkgName.equals(checkInstallPackage) && ((TextUtils.isEmpty(installerPackageName) || !installerPackageName.equals(checkInstallPackage)) && checkPermission("android.permission.INSTALL_PACKAGES", installerPackageName, userId) != -1 && appId != 0 && appId != 2000 && appId != 1000))) {
            throw new PackageManagerException(-110, "Invalid installer for " + checkInstallPackage + "!");
        }
    }

    private static final void enforceSystemOrRoot(String message) {
        int uid = Binder.getCallingUid();
        if (uid != 1000 && uid != 0) {
            throw new SecurityException(message);
        }
    }

    public void performFstrimIfNeeded() {
        enforceSystemOrRoot("Only the system can request fstrim");
        HwThemeManager.applyDefaultHwTheme(false, this.mContext, 0);
        HwThemeManager.linkDataSkinDirAsUser(0);
        try {
            IMountService ms = PackageHelper.getMountService();
            if (ms != null) {
                boolean isUpgrade = isUpgrade();
                boolean doTrim = isUpgrade;
                if (isUpgrade) {
                    Slog.w(TAG, "Running disk maintenance immediately due to system update");
                } else {
                    long interval = Global.getLong(this.mContext.getContentResolver(), "fstrim_mandatory_interval", DEFAULT_MANDATORY_FSTRIM_INTERVAL);
                    if (interval > 0) {
                        long timeSinceLast = System.currentTimeMillis() - ms.lastMaintenance();
                        if (timeSinceLast > interval) {
                            doTrim = true;
                            Slog.w(TAG, "No disk maintenance in " + timeSinceLast + "; running immediately");
                        }
                    }
                }
                if (doTrim && !isFirstBoot()) {
                    Slog.w(TAG, "Optimizing storage...");
                    return;
                }
                return;
            }
            Slog.e(TAG, "Mount service unavailable!");
        } catch (RemoteException e) {
        }
    }

    public void updatePackagesIfNeeded() {
        enforceSystemOrRoot("Only the system can request package update");
        boolean causeUpgrade = isUpgrade();
        boolean z = !isFirstBoot() ? this.mIsPreNUpgrade : true;
        boolean causePrunedCache = VMRuntime.didPruneDalvikCache();
        if (causeUpgrade || z || causePrunedCache) {
            List<Package> pkgs;
            int i;
            synchronized (this.mPackages) {
                pkgs = PackageManagerServiceUtils.getPackagesForDexopt(this.mPackages.values(), this);
            }
            long startTime = System.nanoTime();
            boolean z2 = this.mIsPreNUpgrade;
            if (z) {
                i = 0;
            } else {
                i = 1;
            }
            int[] stats = performDexOptUpgrade(pkgs, z2, PackageManagerServiceCompilerMapping.getCompilerFilterForReason(i));
            int elapsedTimeSeconds = (int) TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime);
            MetricsLogger.histogram(this.mContext, "opt_dialog_num_dexopted", stats[0]);
            MetricsLogger.histogram(this.mContext, "opt_dialog_num_skipped", stats[1]);
            MetricsLogger.histogram(this.mContext, "opt_dialog_num_failed", stats[2]);
            MetricsLogger.histogram(this.mContext, "opt_dialog_num_total", getOptimizablePackages().size());
            MetricsLogger.histogram(this.mContext, "opt_dialog_time_s", elapsedTimeSeconds);
        }
    }

    private int[] performDexOptUpgrade(List<Package> pkgs, boolean showDialog, String compilerFilter) {
        int numberOfPackagesVisited = 0;
        int numberOfPackagesOptimized = 0;
        int numberOfPackagesSkipped = 0;
        int numberOfPackagesFailed = 0;
        int numberOfPackagesToDexopt = pkgs.size();
        for (Package pkg : pkgs) {
            numberOfPackagesVisited++;
            if (PackageDexOptimizer.canOptimizePackage(pkg)) {
                if (showDialog) {
                    try {
                        Flog.i(204, "Optimizing app " + numberOfPackagesVisited + " of " + numberOfPackagesToDexopt + ": " + pkg.packageName);
                        ActivityManagerNative.getDefault().showBootMessage("HOTA:" + numberOfPackagesVisited + ":" + numberOfPackagesToDexopt, true);
                    } catch (RemoteException e) {
                    }
                }
                if (isSystemApp(pkg) && DexFile.isProfileGuidedCompilerFilter(compilerFilter) && !Environment.getReferenceProfile(pkg.packageName).exists()) {
                    compilerFilter = PackageManagerServiceCompilerMapping.getNonProfileGuidedCompilerFilter(compilerFilter);
                }
                int dexOptStatus = performDexOptTraced(pkg.packageName, false, compilerFilter, false);
                switch (dexOptStatus) {
                    case -1:
                        numberOfPackagesFailed++;
                        break;
                    case 0:
                        numberOfPackagesSkipped++;
                        break;
                    case 1:
                        numberOfPackagesOptimized++;
                        break;
                    default:
                        Log.e(TAG, "Unexpected dexopt return code " + dexOptStatus);
                        break;
                }
            }
            numberOfPackagesSkipped++;
        }
        return new int[]{numberOfPackagesOptimized, numberOfPackagesSkipped, numberOfPackagesFailed};
    }

    public void notifyPackageUse(String packageName, int reason) {
        synchronized (this.mPackages) {
            Package p = (Package) this.mPackages.get(packageName);
            if (p == null) {
                return;
            }
            p.mLastPackageUsageTimeInMills[reason] = System.currentTimeMillis();
        }
    }

    public boolean performDexOptIfNeeded(String packageName) {
        if (performDexOptTraced(packageName, false, PackageManagerServiceCompilerMapping.getFullCompilerFilter(), false) != -1) {
            return true;
        }
        return false;
    }

    public boolean performDexOpt(String packageName, boolean checkProfiles, int compileReason, boolean force) {
        return performDexOptTraced(packageName, checkProfiles, PackageManagerServiceCompilerMapping.getCompilerFilterForReason(compileReason), force) != -1;
    }

    public boolean performDexOptMode(String packageName, boolean checkProfiles, String targetCompilerFilter, boolean force) {
        return performDexOptTraced(packageName, checkProfiles, targetCompilerFilter, force) != -1;
    }

    private int performDexOptTraced(String packageName, boolean checkProfiles, String targetCompilerFilter, boolean force) {
        Trace.traceBegin(262144, "dexopt");
        try {
            int performDexOptInternal = performDexOptInternal(packageName, checkProfiles, targetCompilerFilter, force);
            return performDexOptInternal;
        } finally {
            Trace.traceEnd(262144);
        }
    }

    private int performDexOptInternal(String packageName, boolean checkProfiles, String targetCompilerFilter, boolean force) {
        synchronized (this.mPackages) {
            Package p = (Package) this.mPackages.get(packageName);
            if (p == null) {
                return -1;
            }
            this.mPackageUsage.write(false);
            long callingId = Binder.clearCallingIdentity();
            try {
                int performDexOptInternalWithDependenciesLI;
                synchronized (this.mInstallLock) {
                    performDexOptInternalWithDependenciesLI = performDexOptInternalWithDependenciesLI(p, checkProfiles, targetCompilerFilter, force);
                }
                Binder.restoreCallingIdentity(callingId);
                return performDexOptInternalWithDependenciesLI;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(callingId);
            }
        }
    }

    public ArraySet<String> getOptimizablePackages() {
        ArraySet<String> pkgs = new ArraySet();
        synchronized (this.mPackages) {
            for (Package p : this.mPackages.values()) {
                if (PackageDexOptimizer.canOptimizePackage(p)) {
                    pkgs.add(p.packageName);
                }
            }
        }
        return pkgs;
    }

    private int performDexOptInternalWithDependenciesLI(Package p, boolean checkProfiles, String targetCompilerFilter, boolean force) {
        PackageDexOptimizer pdo;
        if (force) {
            pdo = new ForcedUpdatePackageDexOptimizer(this.mPackageDexOptimizer);
        } else {
            pdo = this.mPackageDexOptimizer;
        }
        Collection<Package> deps = findSharedNonSystemLibraries(p);
        String[] instructionSets = InstructionSets.getAppDexInstructionSets(p.applicationInfo);
        if (!deps.isEmpty()) {
            for (Package depPackage : deps) {
                pdo.performDexOpt(depPackage, null, instructionSets, false, PackageManagerServiceCompilerMapping.getCompilerFilterForReason(5));
            }
        }
        return pdo.performDexOpt(p, p.usesLibraryFiles, instructionSets, checkProfiles, targetCompilerFilter);
    }

    Collection<Package> findSharedNonSystemLibraries(Package p) {
        if (p.usesLibraries == null && p.usesOptionalLibraries == null) {
            return Collections.emptyList();
        }
        Collection retValue = new ArrayList();
        findSharedNonSystemLibrariesRecursive(p, retValue, new HashSet());
        retValue.remove(p);
        return retValue;
    }

    private void findSharedNonSystemLibrariesRecursive(Package p, Collection<Package> collected, Set<String> collectedNames) {
        if (!collectedNames.contains(p.packageName)) {
            collectedNames.add(p.packageName);
            collected.add(p);
            if (p.usesLibraries != null) {
                findSharedNonSystemLibrariesRecursive(p.usesLibraries, (Collection) collected, (Set) collectedNames);
            }
            if (p.usesOptionalLibraries != null) {
                findSharedNonSystemLibrariesRecursive(p.usesOptionalLibraries, (Collection) collected, (Set) collectedNames);
            }
        }
    }

    private void findSharedNonSystemLibrariesRecursive(Collection<String> libs, Collection<Package> collected, Set<String> collectedNames) {
        for (String libName : libs) {
            Package libPkg = findSharedNonSystemLibrary(libName);
            if (libPkg != null) {
                findSharedNonSystemLibrariesRecursive(libPkg, (Collection) collected, (Set) collectedNames);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Package findSharedNonSystemLibrary(String libName) {
        synchronized (this.mPackages) {
            SharedLibraryEntry lib = (SharedLibraryEntry) this.mSharedLibraries.get(libName);
            if (lib == null || lib.apk == null) {
            } else {
                Package packageR = (Package) this.mPackages.get(lib.apk);
                return packageR;
            }
        }
    }

    public void shutdown() {
        this.mPackageUsage.write(true);
    }

    public void dumpProfiles(String packageName) {
        synchronized (this.mPackages) {
            Package pkg = (Package) this.mPackages.get(packageName);
            if (pkg == null) {
                throw new IllegalArgumentException("Unknown package: " + packageName);
            }
        }
        int callingUid = Binder.getCallingUid();
        if (callingUid == 2000 || callingUid == 0 || callingUid == pkg.applicationInfo.uid) {
            synchronized (this.mInstallLock) {
                Trace.traceBegin(262144, "dump profiles");
                int sharedGid = UserHandle.getSharedAppGid(pkg.applicationInfo.uid);
                try {
                    List<String> allCodePaths = pkg.getAllCodePathsExcludingResourceOnly();
                    this.mInstaller.dumpProfiles(Integer.toString(sharedGid), packageName, TextUtils.join(";", allCodePaths));
                } catch (InstallerException e) {
                    Slog.w(TAG, "Failed to dump profiles", e);
                }
                Trace.traceEnd(262144);
            }
            return;
        }
        throw new SecurityException("dumpProfiles");
    }

    public void forceDexOpt(String packageName) {
        enforceSystemOrRoot("forceDexOpt");
        synchronized (this.mPackages) {
            Package pkg = (Package) this.mPackages.get(packageName);
            if (pkg == null) {
                throw new IllegalArgumentException("Unknown package: " + packageName);
            }
        }
        synchronized (this.mInstallLock) {
            Trace.traceBegin(262144, "dexopt");
            int res = performDexOptInternalWithDependenciesLI(pkg, false, PackageManagerServiceCompilerMapping.getCompilerFilterForReason(7), true);
            Trace.traceEnd(262144);
            if (res != 1) {
                throw new IllegalStateException("Failed to dexopt: " + res);
            }
        }
    }

    private boolean verifyPackageUpdateLPr(PackageSetting oldPkg, Package newPkg) {
        if ((oldPkg.pkgFlags & 1) == 0) {
            Slog.w(TAG, "Unable to update from " + oldPkg.name + " to " + newPkg.packageName + ": old package not in system partition");
            return false;
        } else if (this.mPackages.get(oldPkg.name) == null) {
            return true;
        } else {
            Slog.w(TAG, "Unable to update from " + oldPkg.name + " to " + newPkg.packageName + ": old package still exists");
            return false;
        }
    }

    void removeCodePathLI(File codePath) {
        if (codePath.isDirectory()) {
            try {
                this.mInstaller.rmPackageDir(codePath.getAbsolutePath());
                return;
            } catch (InstallerException e) {
                Slog.w(TAG, "Failed to remove code path", e);
                return;
            }
        }
        codePath.delete();
    }

    private int[] resolveUserIds(int userId) {
        if (userId == -1) {
            return sUserManager.getUserIds();
        }
        return new int[]{userId};
    }

    private void clearAppDataLIF(Package pkg, int userId, int flags) {
        if (pkg == null) {
            Slog.wtf(TAG, "Package was null!", new Throwable());
            return;
        }
        clearAppDataLeafLIF(pkg, userId, flags);
        int childCount = pkg.childPackages != null ? pkg.childPackages.size() : 0;
        for (int i = 0; i < childCount; i++) {
            clearAppDataLeafLIF((Package) pkg.childPackages.get(i), userId, flags);
        }
    }

    private void clearAppDataLeafLIF(Package pkg, int userId, int flags) {
        synchronized (this.mPackages) {
            PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(pkg.packageName);
        }
        for (int realUserId : resolveUserIds(userId)) {
            try {
                this.mInstaller.clearAppData(pkg.volumeUuid, pkg.packageName, realUserId, flags, ps != null ? ps.getCeDataInode(realUserId) : 0);
            } catch (InstallerException e) {
                Slog.w(TAG, String.valueOf(e));
            }
        }
    }

    private void destroyAppDataLIF(Package pkg, int userId, int flags) {
        if (pkg == null) {
            Slog.wtf(TAG, "Package was null!", new Throwable());
            return;
        }
        destroyAppDataLeafLIF(pkg, userId, flags);
        int childCount = pkg.childPackages != null ? pkg.childPackages.size() : 0;
        for (int i = 0; i < childCount; i++) {
            destroyAppDataLeafLIF((Package) pkg.childPackages.get(i), userId, flags);
        }
    }

    private void destroyAppDataLeafLIF(Package pkg, int userId, int flags) {
        synchronized (this.mPackages) {
            PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(pkg.packageName);
        }
        for (int realUserId : resolveUserIds(userId)) {
            try {
                this.mInstaller.destroyAppData(pkg.volumeUuid, pkg.packageName, realUserId, flags, ps != null ? ps.getCeDataInode(realUserId) : 0);
            } catch (InstallerException e) {
                Slog.w(TAG, String.valueOf(e));
            }
        }
    }

    private void destroyAppProfilesLIF(Package pkg, int userId) {
        if (pkg == null) {
            Slog.wtf(TAG, "Package was null!", new Throwable());
            return;
        }
        destroyAppProfilesLeafLIF(pkg);
        destroyAppReferenceProfileLeafLIF(pkg, userId, true);
        int childCount = pkg.childPackages != null ? pkg.childPackages.size() : 0;
        for (int i = 0; i < childCount; i++) {
            destroyAppProfilesLeafLIF((Package) pkg.childPackages.get(i));
            destroyAppReferenceProfileLeafLIF((Package) pkg.childPackages.get(i), userId, true);
        }
    }

    private void destroyAppReferenceProfileLeafLIF(Package pkg, int userId, boolean removeBaseMarker) {
        if (!pkg.isForwardLocked()) {
            for (String path : pkg.getAllCodePathsExcludingResourceOnly()) {
                try {
                    String useMarker = PackageManagerServiceUtils.realpath(new File(path)).replace('/', '@');
                    for (int realUserId : resolveUserIds(userId)) {
                        File profileDir = Environment.getDataProfilesDeForeignDexDirectory(realUserId);
                        if (removeBaseMarker) {
                            File foreignUseMark = new File(profileDir, useMarker);
                            if (foreignUseMark.exists() && !foreignUseMark.delete()) {
                                Slog.w(TAG, "Unable to delete foreign user mark for package: " + pkg.packageName);
                            }
                        }
                        File[] markers = profileDir.listFiles();
                        if (markers != null) {
                            String searchString = "@" + pkg.packageName + "@";
                            for (File marker : markers) {
                                if (marker.getName().indexOf(searchString) > 0 && !marker.delete()) {
                                    Slog.w(TAG, "Unable to delete foreign user mark for package: " + pkg.packageName);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    Slog.w(TAG, "Failed to get canonical path", e);
                }
            }
        }
    }

    private void destroyAppProfilesLeafLIF(Package pkg) {
        try {
            this.mInstaller.destroyAppProfiles(pkg.packageName);
        } catch (InstallerException e) {
            Slog.w(TAG, String.valueOf(e));
        }
    }

    private void clearAppProfilesLIF(Package pkg, int userId) {
        if (pkg == null) {
            Slog.wtf(TAG, "Package was null!", new Throwable());
            return;
        }
        clearAppProfilesLeafLIF(pkg);
        destroyAppReferenceProfileLeafLIF(pkg, userId, false);
        int childCount = pkg.childPackages != null ? pkg.childPackages.size() : 0;
        for (int i = 0; i < childCount; i++) {
            clearAppProfilesLeafLIF((Package) pkg.childPackages.get(i));
        }
    }

    private void clearAppProfilesLeafLIF(Package pkg) {
        try {
            this.mInstaller.clearAppProfiles(pkg.packageName);
        } catch (InstallerException e) {
            Slog.w(TAG, String.valueOf(e));
        }
    }

    private void setInstallAndUpdateTime(Package pkg, long firstInstallTime, long lastUpdateTime) {
        PackageSetting ps = pkg.mExtras;
        if (ps != null) {
            ps.firstInstallTime = firstInstallTime;
            ps.lastUpdateTime = lastUpdateTime;
        }
        int childCount = pkg.childPackages != null ? pkg.childPackages.size() : 0;
        for (int i = 0; i < childCount; i++) {
            ps = ((Package) pkg.childPackages.get(i)).mExtras;
            if (ps != null) {
                ps.firstInstallTime = firstInstallTime;
                ps.lastUpdateTime = lastUpdateTime;
            }
        }
    }

    private void addSharedLibraryLPw(ArraySet<String> usesLibraryFiles, SharedLibraryEntry file, Package changingLib) {
        if (file.path != null) {
            usesLibraryFiles.add(file.path);
            return;
        }
        Package p = (Package) this.mPackages.get(file.apk);
        if (changingLib != null && changingLib.packageName.equals(file.apk) && (p == null || p.packageName.equals(changingLib.packageName))) {
            p = changingLib;
        }
        if (p != null) {
            usesLibraryFiles.addAll(p.getAllCodePaths());
        }
    }

    private void updateSharedLibrariesLPw(Package pkg, Package changingLib) throws PackageManagerException {
        if (pkg.usesLibraries != null || pkg.usesOptionalLibraries != null) {
            int i;
            SharedLibraryEntry file;
            ArraySet<String> usesLibraryFiles = new ArraySet();
            int N = pkg.usesLibraries != null ? pkg.usesLibraries.size() : 0;
            for (i = 0; i < N; i++) {
                file = (SharedLibraryEntry) this.mSharedLibraries.get(pkg.usesLibraries.get(i));
                if (file == null) {
                    throw new PackageManagerException(-9, "Package " + pkg.packageName + " requires unavailable shared library " + ((String) pkg.usesLibraries.get(i)) + "; failing!");
                }
                addSharedLibraryLPw(usesLibraryFiles, file, changingLib);
            }
            N = pkg.usesOptionalLibraries != null ? pkg.usesOptionalLibraries.size() : 0;
            for (i = 0; i < N; i++) {
                file = (SharedLibraryEntry) this.mSharedLibraries.get(pkg.usesOptionalLibraries.get(i));
                if (file == null) {
                    Slog.w(TAG, "Package " + pkg.packageName + " desires unavailable shared library " + ((String) pkg.usesOptionalLibraries.get(i)) + "; ignoring!");
                } else {
                    addSharedLibraryLPw(usesLibraryFiles, file, changingLib);
                }
            }
            N = usesLibraryFiles.size();
            if (N > 0) {
                pkg.usesLibraryFiles = (String[]) usesLibraryFiles.toArray(new String[N]);
            } else {
                pkg.usesLibraryFiles = null;
            }
        }
    }

    private static boolean hasString(List<String> list, List<String> which) {
        if (list == null) {
            return false;
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            for (int j = which.size() - 1; j >= 0; j--) {
                if (((String) which.get(j)).equals(list.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateAllSharedLibrariesLPw() {
        for (Package pkg : this.mPackages.values()) {
            try {
                updateSharedLibrariesLPw(pkg, null);
            } catch (PackageManagerException e) {
                Slog.e(TAG, "updateAllSharedLibrariesLPw failed: " + e.getMessage());
            }
        }
    }

    private ArrayList<Package> updateAllSharedLibrariesLPw(Package changingPkg) {
        ArrayList<Package> res = null;
        for (Package pkg : this.mPackages.values()) {
            if (hasString(pkg.usesLibraries, changingPkg.libraryNames) || hasString(pkg.usesOptionalLibraries, changingPkg.libraryNames)) {
                if (res == null) {
                    res = new ArrayList();
                }
                res.add(pkg);
                try {
                    updateSharedLibrariesLPw(pkg, changingPkg);
                } catch (PackageManagerException e) {
                    Slog.e(TAG, "updateAllSharedLibrariesLPw failed: " + e.getMessage());
                }
            }
        }
        return res;
    }

    private static String deriveAbiOverride(String abiOverride, PackageSetting settings) {
        if (INSTALL_PACKAGE_SUFFIX.equals(abiOverride)) {
            return null;
        }
        if (abiOverride != null) {
            return abiOverride;
        }
        if (settings != null) {
            return settings.cpuAbiOverrideString;
        }
        return null;
    }

    private Package scanPackageTracedLI(Package pkg, int policyFlags, int scanFlags, long currentTime, UserHandle user) throws PackageManagerException {
        Trace.traceBegin(262144, "scanPackage");
        if ((DumpState.DUMP_VERSION & scanFlags) != 0) {
            scanFlags &= -32769;
        } else if (pkg.childPackages != null && pkg.childPackages.size() > 0) {
            scanFlags |= DumpState.DUMP_VERSION;
        }
        try {
            Package scannedPkg = scanPackageLI(pkg, policyFlags, scanFlags, currentTime, user);
            int childCount = pkg.childPackages != null ? pkg.childPackages.size() : 0;
            for (int i = 0; i < childCount; i++) {
                scanPackageLI((Package) pkg.childPackages.get(i), policyFlags, scanFlags, currentTime, user);
            }
            if ((DumpState.DUMP_VERSION & scanFlags) != 0) {
                return scanPackageTracedLI(pkg, policyFlags, scanFlags, currentTime, user);
            }
            return scannedPkg;
        } finally {
            Trace.traceEnd(262144);
        }
    }

    private Package scanPackageLI(Package pkg, int policyFlags, int scanFlags, long currentTime, UserHandle user) throws PackageManagerException {
        return scanPackageLI(pkg, policyFlags, scanFlags, currentTime, user, 0);
    }

    private Package scanPackageLI(Package pkg, int policyFlags, int scanFlags, long currentTime, UserHandle user, int hwFlags) throws PackageManagerException {
        boolean success = false;
        try {
            if (isSystemApp(pkg) || !HwDeviceManager.disallowOp(7, pkg.packageName)) {
                Package res = scanPackageDirtyLI(pkg, policyFlags, scanFlags, currentTime, user, hwFlags);
                success = true;
                return res;
            }
            throw new PackageManagerException(-110, "app is not in the installpackage_whitelist");
        } finally {
            if (!(success || (scanFlags & 1024) == 0)) {
                destroyAppDataLIF(pkg, -1, 3);
                destroyAppProfilesLIF(pkg, -1);
            }
        }
    }

    private static boolean apkHasCode(String fileName) {
        Throwable th;
        boolean z = false;
        StrictJarFile jarFile = null;
        try {
            StrictJarFile jarFile2 = new StrictJarFile(fileName, false, false);
            try {
                if (jarFile2.findEntry("classes.dex") != null) {
                    z = true;
                }
                try {
                    jarFile2.close();
                } catch (IOException e) {
                }
                return z;
            } catch (IOException e2) {
                jarFile = jarFile2;
                try {
                    jarFile.close();
                } catch (IOException e3) {
                }
                return false;
            } catch (Throwable th2) {
                th = th2;
                jarFile = jarFile2;
                try {
                    jarFile.close();
                } catch (IOException e4) {
                }
                throw th;
            }
        } catch (IOException e5) {
            jarFile.close();
            return false;
        } catch (Throwable th3) {
            th = th3;
            jarFile.close();
            throw th;
        }
    }

    private static void enforceCodePolicy(Package pkg) throws PackageManagerException {
        boolean shouldHaveCode;
        if ((pkg.applicationInfo.flags & 4) != 0) {
            shouldHaveCode = true;
        } else {
            shouldHaveCode = false;
        }
        if (shouldHaveCode && !apkHasCode(pkg.baseCodePath)) {
            throw new PackageManagerException(-2, "Package " + pkg.baseCodePath + " code is missing");
        } else if (!ArrayUtils.isEmpty(pkg.splitCodePaths)) {
            int i = 0;
            while (i < pkg.splitCodePaths.length) {
                boolean splitShouldHaveCode;
                if ((pkg.splitFlags[i] & 4) != 0) {
                    splitShouldHaveCode = true;
                } else {
                    splitShouldHaveCode = false;
                }
                if (!splitShouldHaveCode || apkHasCode(pkg.splitCodePaths[i])) {
                    i++;
                } else {
                    throw new PackageManagerException(-2, "Package " + pkg.splitCodePaths[i] + " code is missing");
                }
            }
        }
    }

    private Package scanPackageDirtyLI(Package pkg, int policyFlags, int scanFlags, long currentTime, UserHandle user) throws PackageManagerException {
        return scanPackageDirtyLI(pkg, policyFlags, scanFlags, currentTime, user, 0);
    }

    private Package scanPackageDirtyLI(Package pkg, int policyFlags, int scanFlags, long currentTime, UserHandle user, int hwFlags) throws PackageManagerException {
        File file = new File(pkg.codePath);
        if (pkg.applicationInfo.getCodePath() == null || pkg.applicationInfo.getResourcePath() == null) {
            throw new PackageManagerException(-2, "Code and resource paths haven't been set correctly");
        }
        ApplicationInfo applicationInfo;
        int i;
        PackageSettingBase pkgSetting;
        int N;
        int j;
        String str;
        if ((policyFlags & 1) != 0) {
            applicationInfo = pkg.applicationInfo;
            applicationInfo.flags |= 1;
            if (pkg.applicationInfo.isDirectBootAware()) {
                ActivityInfo activityInfo;
                for (Service s : pkg.services) {
                    Service s2;
                    ServiceInfo serviceInfo = s2.info;
                    s2.info.directBootAware = true;
                    serviceInfo.encryptionAware = true;
                }
                for (Provider p : pkg.providers) {
                    Provider p2;
                    ProviderInfo providerInfo = p2.info;
                    p2.info.directBootAware = true;
                    providerInfo.encryptionAware = true;
                }
                for (Activity a : pkg.activities) {
                    Activity a2;
                    activityInfo = a2.info;
                    a2.info.directBootAware = true;
                    activityInfo.encryptionAware = true;
                }
                for (Activity r : pkg.receivers) {
                    activityInfo = r.info;
                    r.info.directBootAware = true;
                    activityInfo.encryptionAware = true;
                }
            }
        } else {
            pkg.coreApp = false;
            applicationInfo = pkg.applicationInfo;
            applicationInfo.privateFlags &= -33;
            applicationInfo = pkg.applicationInfo;
            applicationInfo.privateFlags &= -65;
        }
        addFlagsForRemovablePreApk(pkg, hwFlags);
        addFlagsForUpdatedRemovablePreApk(pkg, hwFlags);
        pkg.mTrustedOverlay = (policyFlags & 512) != 0;
        if ((policyFlags & 128) != 0) {
            applicationInfo = pkg.applicationInfo;
            applicationInfo.privateFlags |= 8;
        }
        if ((policyFlags & 1024) != 0) {
            enforceCodePolicy(pkg);
        }
        if (this.mCustomResolverComponentName != null && this.mCustomResolverComponentName.getPackageName().equals(pkg.packageName)) {
            setUpCustomResolverActivity(pkg);
        }
        if (pkg.packageName.equals(PLATFORM_PACKAGE_NAME)) {
            synchronized (this.mPackages) {
                if (this.mAndroidApplication != null) {
                    Slog.w(TAG, "*************************************************");
                    Slog.w(TAG, "Core android package being redefined.  Skipping.");
                    Slog.w(TAG, " file=" + file);
                    Slog.w(TAG, "*************************************************");
                    throw new PackageManagerException(-5, "Core android package being redefined.  Skipping.");
                }
                if ((DumpState.DUMP_VERSION & scanFlags) == 0) {
                    this.mPlatformPackage = pkg;
                    pkg.mVersionCode = this.mSdkVersion;
                    this.mAndroidApplication = pkg.applicationInfo;
                    if (!this.mResolverReplaced) {
                        this.mResolveActivity.applicationInfo = this.mAndroidApplication;
                        this.mResolveActivity.name = ResolverActivity.class.getName();
                        this.mResolveActivity.packageName = this.mAndroidApplication.packageName;
                        this.mResolveActivity.processName = "system:ui";
                        this.mResolveActivity.launchMode = 0;
                        this.mResolveActivity.documentLaunchMode = 3;
                        this.mResolveActivity.flags = 32;
                        this.mResolveActivity.theme = 16974374;
                        this.mResolveActivity.exported = true;
                        this.mResolveActivity.enabled = true;
                        this.mResolveActivity.resizeMode = 2;
                        this.mResolveActivity.configChanges = 3504;
                        this.mResolveInfo.activityInfo = this.mResolveActivity;
                        this.mResolveInfo.priority = 0;
                        this.mResolveInfo.preferredOrder = 0;
                        this.mResolveInfo.match = 0;
                        this.mResolveComponentName = new ComponentName(this.mAndroidApplication.packageName, this.mResolveActivity.name);
                    }
                }
            }
        }
        setGMSPackage(pkg);
        synchronized (this.mPackages) {
            if (this.mPackages.containsKey(pkg.packageName) || this.mSharedLibraries.containsKey(pkg.packageName)) {
                throw new PackageManagerException(-5, "Application package " + pkg.packageName + " already installed.  Skipping duplicate.");
            }
            if ((scanFlags & 4096) != 0) {
                if (this.mExpectingBetter.containsKey(pkg.packageName)) {
                    logCriticalInfo(5, "Relax SCAN_REQUIRE_KNOWN requirement for package " + pkg.packageName);
                } else {
                    PackageSetting known = this.mSettings.peekPackageLPr(pkg.packageName);
                    if (!(known == null || (pkg.applicationInfo.getCodePath().equals(known.codePathString) && pkg.applicationInfo.getResourcePath().equals(known.resourcePathString)))) {
                        throw new PackageManagerException(-23, "Application package " + pkg.packageName + " found at " + pkg.applicationInfo.getCodePath() + " but expected at " + known.codePathString + "; ignoring.");
                    }
                }
            }
        }
        File destCodeFile = new File(pkg.applicationInfo.getCodePath());
        File destResourceFile = new File(pkg.applicationInfo.getResourcePath());
        SharedUserSetting sharedUserSetting = null;
        if (!isSystemApp(pkg)) {
            pkg.mOriginalPackages = null;
            pkg.mRealPackage = null;
            pkg.mAdoptPermissions = null;
        }
        PackageSetting packageSetting = null;
        synchronized (this.mPackages) {
            if (pkg.mSharedUserId != null) {
                sharedUserSetting = this.mSettings.getSharedUserLPw(pkg.mSharedUserId, 0, 0, true);
                if (sharedUserSetting == null) {
                    throw new PackageManagerException(-4, "Creating application package " + pkg.packageName + " for shared user failed");
                }
            }
            PackageSetting packageSetting2 = null;
            String str2 = null;
            if (pkg.mOriginalPackages != null) {
                String renamed = (String) this.mSettings.mRenamedPackages.get(pkg.mRealPackage);
                if (pkg.mOriginalPackages.contains(renamed)) {
                    str2 = pkg.mRealPackage;
                    if (!pkg.packageName.equals(renamed)) {
                        pkg.setPackageName(renamed);
                    }
                } else {
                    for (i = pkg.mOriginalPackages.size() - 1; i >= 0; i--) {
                        packageSetting2 = this.mSettings.peekPackageLPr((String) pkg.mOriginalPackages.get(i));
                        if (packageSetting2 != null) {
                            if (verifyPackageUpdateLPr(packageSetting2, pkg)) {
                                if (packageSetting2.sharedUser == null || packageSetting2.sharedUser.name.equals(pkg.mSharedUserId)) {
                                    break;
                                }
                                Slog.w(TAG, "Unable to migrate data from " + packageSetting2.name + " to " + pkg.packageName + ": old uid " + packageSetting2.sharedUser.name + " differs from " + pkg.mSharedUserId);
                                packageSetting2 = null;
                            } else {
                                packageSetting2 = null;
                            }
                        }
                    }
                }
            }
            if (this.mTransferedPackages.contains(pkg.packageName)) {
                Slog.w(TAG, "Package " + pkg.packageName + " was transferred to another, but its .apk remains");
            }
            if ((DumpState.DUMP_VERSION & scanFlags) != 0) {
                PackageSetting foundPs = this.mSettings.peekPackageLPr(pkg.packageName);
                if (foundPs != null) {
                    packageSetting = new PackageSetting(foundPs);
                }
            }
            pkgSetting = this.mSettings.getPackageLPw(pkg, packageSetting2, str2, sharedUserSetting, destCodeFile, destResourceFile, pkg.applicationInfo.nativeLibraryRootDir, pkg.applicationInfo.primaryCpuAbi, pkg.applicationInfo.secondaryCpuAbi, pkg.applicationInfo.flags, pkg.applicationInfo.privateFlags, user, false);
            if (pkgSetting == null) {
                throw new PackageManagerException(-4, "Creating application package " + pkg.packageName + " failed");
            }
            String[] names;
            if (pkgSetting.origPackage != null) {
                pkg.setPackageName(packageSetting2.name);
                reportSettingsProblem(5, "New package " + pkgSetting.realName + " renamed to replace old package " + pkgSetting.name);
                if ((DumpState.DUMP_VERSION & scanFlags) == 0) {
                    this.mTransferedPackages.add(packageSetting2.name);
                }
                pkgSetting.origPackage = null;
            }
            if ((DumpState.DUMP_VERSION & scanFlags) == 0 && str2 != null) {
                this.mTransferedPackages.add(pkg.packageName);
            }
            if (this.mSettings.isDisabledSystemPackageLPr(pkg.packageName)) {
                applicationInfo = pkg.applicationInfo;
                applicationInfo.flags |= 128;
                String disableSysPath = this.mSettings.getDisabledSysPackagesPath(pkg.packageName);
                if (!(disableSysPath == null || new File(disableSysPath).exists())) {
                    Log.i(TAG, "sysPackagesPath " + disableSysPath + ", has removed, remove its FLAG_SYSTEM & removeDisabledSystemPackageLPw");
                    applicationInfo = pkg.applicationInfo;
                    applicationInfo.flags &= -2;
                    applicationInfo = pkg.applicationInfo;
                    applicationInfo.flags &= -129;
                    this.mSettings.removeDisabledSystemPackageLPw(pkg.packageName);
                }
            }
            if ((policyFlags & 64) == 0) {
                updateSharedLibrariesLPw(pkg, null);
            } else {
                String deletedSysAppName = this.mSettings.getDisabledSystemPackageName(pkg.codePath);
                if (deletedSysAppName != null) {
                    if (!deletedSysAppName.equals(pkg.packageName) && (this.mCustPms == null || this.mCustPms.isListedApp(deletedSysAppName) == -1)) {
                        Log.i(TAG, "deletedSysAppName " + deletedSysAppName + ", IN " + pkg.codePath + ", REMOVED");
                        this.mSettings.removeDisabledSystemPackageLPw(deletedSysAppName);
                    }
                }
            }
            if (this.mFoundPolicyFile) {
                SELinuxMMAC.assignSeinfoValue(pkg);
            }
            pkg.applicationInfo.uid = pkgSetting.appId;
            pkg.mExtras = pkgSetting;
            if (!shouldCheckUpgradeKeySetLP(pkgSetting, scanFlags)) {
                try {
                    verifySignaturesLP(pkgSetting, pkg);
                    pkgSetting.signatures.mSignatures = pkg.mSignatures;
                } catch (PackageManagerException e) {
                    if ((policyFlags & 64) == 0) {
                        throw e;
                    }
                    boolean isSystemSignUpdate = isSystemSignatureUpdated(pkgSetting.signatures.mSignatures, pkg.mSignatures);
                    pkgSetting.signatures.mSignatures = pkg.mSignatures;
                    if (pkgSetting.sharedUser == null || isSystemSignUpdate || compareSignatures(pkgSetting.sharedUser.signatures.mSignatures, pkg.mSignatures) == 0) {
                        reportSettingsProblem(5, "System package " + pkg.packageName + " signature changed; retaining data.");
                    } else {
                        throw new PackageManagerException(-104, "Signature mismatch for shared user: " + pkgSetting.sharedUser);
                    }
                }
            } else if (checkUpgradeKeySetLP(pkgSetting, pkg)) {
                pkgSetting.signatures.mSignatures = pkg.mSignatures;
            } else if ((policyFlags & 64) == 0) {
                throw new PackageManagerException(-7, "Package " + pkg.packageName + " upgrade keys do not match the " + "previously installed version");
            } else {
                pkgSetting.signatures.mSignatures = pkg.mSignatures;
                reportSettingsProblem(5, "System package " + pkg.packageName + " signature changed; retaining data.");
            }
            if ((scanFlags & 16) != 0) {
                N = pkg.providers.size();
                for (i = 0; i < N; i++) {
                    p2 = (Provider) pkg.providers.get(i);
                    if (p2.info.authority != null) {
                        names = p2.info.authority.split(";");
                        for (j = 0; j < names.length; j++) {
                            if (this.mProvidersByAuthority.containsKey(names[j])) {
                                Provider other = (Provider) this.mProvidersByAuthority.get(names[j]);
                                String otherPackageName = (other == null || other.getComponentName() == null) ? "?" : other.getComponentName().getPackageName();
                                throw new PackageManagerException(-13, "Can't install because provider name " + names[j] + " (in package " + pkg.applicationInfo.packageName + ") is already used by " + otherPackageName);
                            }
                        }
                        continue;
                    }
                }
            }
            if ((DumpState.DUMP_VERSION & scanFlags) == 0 && pkg.mAdoptPermissions != null) {
                for (i = pkg.mAdoptPermissions.size() - 1; i >= 0; i--) {
                    String origName = (String) pkg.mAdoptPermissions.get(i);
                    PackageSetting orig = this.mSettings.peekPackageLPr(origName);
                    if (orig != null && verifyPackageUpdateLPr(orig, pkg)) {
                        Slog.i(TAG, "Adopting permissions from " + origName + " to " + pkg.packageName);
                        this.mSettings.transferPermissionsLPw(origName, pkg.packageName);
                    }
                }
            }
        }
        String pkgName = pkg.packageName;
        long scanFileTime = file.lastModified();
        if ((scanFlags & 4) != 0) {
        }
        pkg.applicationInfo.processName = fixProcessName(pkg.applicationInfo.packageName, pkg.applicationInfo.processName, pkg.applicationInfo.uid);
        if (pkg != this.mPlatformPackage) {
            pkg.applicationInfo.initForUser(0);
        }
        String path = file.getPath();
        String cpuAbiOverride = deriveAbiOverride(pkg.cpuAbiOverride, pkgSetting);
        if ((scanFlags & 16) == 0) {
            boolean extractLibs = (!mOptimizeBootOn || this.mSystemReady) ? true : (DumpState.DUMP_DOMAIN_PREFERRED & scanFlags) == DumpState.DUMP_DOMAIN_PREFERRED;
            if ((scanFlags & 256) == 0 || isMultiArch(pkg.applicationInfo)) {
                derivePackageAbi(pkg, file, cpuAbiOverride, extractLibs);
            } else {
                if (pkg.mVersionCode == getPackageVersion(pkg.packageName)) {
                    if (isPackageAbiRestored(pkg.packageName)) {
                        readLastedAbi(pkg, file, cpuAbiOverride);
                    }
                }
                derivePackageAbi(pkg, file, cpuAbiOverride, extractLibs);
            }
            if (isSystemApp(pkg) && !pkg.isUpdatedSystemApp() && pkg.applicationInfo.primaryCpuAbi == null) {
                setBundledAppAbisAndRoots(pkg, pkgSetting);
                setNativeLibraryPaths(pkg);
            }
            if (isSystemApp(pkg) && !pkg.isUpdatedSystemApp()) {
                synchronized (this.mInstallLock) {
                    if (pkg.applicationInfo.primaryCpuAbi != null) {
                        try {
                            this.mInstaller.linkNativeLibraryDirectory(pkg.volumeUuid, pkg.packageName, pkg.applicationInfo.nativeLibraryDir, 0);
                        } catch (InstallerException e2) {
                        }
                    }
                }
            }
        } else {
            if ((scanFlags & 8192) != 0) {
                pkg.applicationInfo.primaryCpuAbi = pkgSetting.primaryCpuAbiString;
                pkg.applicationInfo.secondaryCpuAbi = pkgSetting.secondaryCpuAbiString;
            }
            setNativeLibraryPaths(pkg);
        }
        if (this.mPlatformPackage == pkg) {
            ApplicationInfo applicationInfo2 = pkg.applicationInfo;
            if (VMRuntime.getRuntime().is64Bit()) {
                str = Build.SUPPORTED_64_BIT_ABIS[0];
            } else {
                str = Build.SUPPORTED_32_BIT_ABIS[0];
            }
            applicationInfo2.primaryCpuAbi = str;
        }
        if ((scanFlags & 2) == 0 && (scanFlags & 16) != 0 && cpuAbiOverride == null && pkgSetting.cpuAbiOverrideString != null) {
            Slog.w(TAG, "Ignoring persisted ABI override " + cpuAbiOverride + " for package " + pkg.packageName);
        }
        pkgSetting.primaryCpuAbiString = pkg.applicationInfo.primaryCpuAbi;
        pkgSetting.secondaryCpuAbiString = pkg.applicationInfo.secondaryCpuAbi;
        pkgSetting.cpuAbiOverrideString = cpuAbiOverride;
        pkg.cpuAbiOverride = cpuAbiOverride;
        pkgSetting.legacyNativeLibraryPathString = pkg.applicationInfo.nativeLibraryRootDir;
        if ((scanFlags & 256) == 0 && pkgSetting.sharedUser != null) {
            adjustCpuAbisForSharedUserLPw(pkgSetting.sharedUser.packages, pkg, true);
        }
        if (this.mFactoryTest && pkg.requestedPermissions.contains("android.permission.FACTORY_TEST")) {
            applicationInfo = pkg.applicationInfo;
            applicationInfo.flags |= 16;
        }
        ArrayList arrayList = null;
        if ((DumpState.DUMP_VERSION & scanFlags) != 0) {
            if (packageSetting != null) {
                synchronized (this.mPackages) {
                    this.mSettings.mPackages.put(packageSetting.name, packageSetting);
                }
            }
            return pkg;
        }
        if (!(pkg.childPackages == null || pkg.childPackages.isEmpty())) {
            if ((policyFlags & 128) == 0) {
                throw new PackageManagerException("Only privileged apps and updated privileged apps can add child packages. Ignoring package " + pkg.packageName);
            }
            int childCount = pkg.childPackages.size();
            for (i = 0; i < childCount; i++) {
                if (this.mSettings.hasOtherDisabledSystemPkgWithChildLPr(pkg.packageName, ((Package) pkg.childPackages.get(i)).packageName)) {
                    throw new PackageManagerException("Cannot override a child package of another disabled system app. Ignoring package " + pkg.packageName);
                }
            }
        }
        synchronized (this.mPackages) {
            if (!((pkg.applicationInfo.flags & 1) == 0 || pkg.libraryNames == null)) {
                for (i = 0; i < pkg.libraryNames.size(); i++) {
                    String name = (String) pkg.libraryNames.get(i);
                    boolean allowed = false;
                    if (pkg.isUpdatedSystemApp()) {
                        PackageSetting sysPs = this.mSettings.getDisabledSystemPkgLPr(pkg.packageName);
                        if (sysPs.pkg != null && sysPs.pkg.libraryNames != null) {
                            for (j = 0; j < sysPs.pkg.libraryNames.size(); j++) {
                                if (name.equals(sysPs.pkg.libraryNames.get(j))) {
                                    allowed = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        allowed = true;
                    }
                    if (!allowed) {
                        Slog.w(TAG, "Package " + pkg.packageName + " declares lib " + name + " that is not declared on system image; skipping");
                    } else if (this.mSharedLibraries.containsKey(name)) {
                        if (!name.equals(pkg.packageName)) {
                            Slog.w(TAG, "Package " + pkg.packageName + " library " + name + " already exists; skipping");
                        }
                    } else {
                        this.mSharedLibraries.put(name, new SharedLibraryEntry(null, pkg.packageName));
                    }
                }
                if ((scanFlags & 256) == 0) {
                    arrayList = updateAllSharedLibrariesLPw(pkg);
                }
            }
        }
        if ((scanFlags & 256) == 0 && (131072 & scanFlags) == 0 && (DumpState.DUMP_DOMAIN_PREFERRED & scanFlags) == 0) {
            checkPackageFrozen(pkgName);
        }
        if (arrayList != null) {
            for (i = 0; i < arrayList.size(); i++) {
                Package clientPkg = (Package) arrayList.get(i);
                killApplication(clientPkg.applicationInfo.packageName, clientPkg.applicationInfo.uid, "update lib");
            }
        }
        KeySetManagerService ksms = this.mSettings.mKeySetManagerService;
        ksms.assertScannedPackageValid(pkg);
        Trace.traceBegin(262144, "updateSettings");
        boolean createIdmapFailed = false;
        synchronized (this.mPackages) {
            if (pkgSetting.pkg != null) {
                Package packageR = pkgSetting.pkg;
                if (user == null) {
                    user = UserHandle.ALL;
                }
                maybeRenameForeignDexMarkers(packageR, pkg, user);
            }
            this.mSettings.insertPackageSettingLPw(pkgSetting, pkg);
            updateCertCompatPackage(pkg, pkgSetting);
            this.mPackages.put(pkg.applicationInfo.packageName, pkg);
            Iterator<PackageCleanItem> iter = this.mSettings.mPackagesToBeCleaned.iterator();
            while (iter.hasNext()) {
                if (pkgName.equals(((PackageCleanItem) iter.next()).packageName)) {
                    iter.remove();
                }
            }
            if (currentTime != 0) {
                if (pkgSetting.firstInstallTime == 0) {
                    pkgSetting.lastUpdateTime = currentTime;
                    pkgSetting.firstInstallTime = currentTime;
                } else if ((scanFlags & 64) != 0) {
                    pkgSetting.lastUpdateTime = currentTime;
                }
            } else if (pkgSetting.firstInstallTime == 0) {
                pkgSetting.lastUpdateTime = scanFileTime;
                pkgSetting.firstInstallTime = scanFileTime;
            } else if (!((policyFlags & 64) == 0 || scanFileTime == pkgSetting.timeStamp)) {
                pkgSetting.lastUpdateTime = scanFileTime;
            }
            ksms.addScannedPackageLPw(pkg);
            N = pkg.providers.size();
            StringBuilder r2 = null;
            for (i = 0; i < N; i++) {
                p2 = (Provider) pkg.providers.get(i);
                p2.info.processName = fixProcessName(pkg.applicationInfo.processName, p2.info.processName, pkg.applicationInfo.uid);
                this.mProviders.addProvider(p2);
                p2.syncable = p2.info.isSyncable;
                if (p2.info.authority != null) {
                    names = p2.info.authority.split(";");
                    p2.info.authority = null;
                    j = 0;
                    Provider p3 = p2;
                    while (j < names.length) {
                        if (j != 1) {
                            p2 = p3;
                        } else if (p3.syncable) {
                            p2 = new Provider(p3);
                            p2.syncable = false;
                        } else {
                            p2 = p3;
                        }
                        if (this.mProvidersByAuthority.containsKey(names[j])) {
                            other = (Provider) this.mProvidersByAuthority.get(names[j]);
                            String str3 = TAG;
                            StringBuilder append = new StringBuilder().append("Skipping provider name ").append(names[j]).append(" (in package ").append(pkg.applicationInfo.packageName).append("): name already used by ");
                            str = (other == null || other.getComponentName() == null) ? "?" : other.getComponentName().getPackageName();
                            Slog.w(str3, append.append(str).toString());
                        } else {
                            this.mProvidersByAuthority.put(names[j], p2);
                            if (p2.info.authority == null) {
                                p2.info.authority = names[j];
                            } else {
                                p2.info.authority += ";" + names[j];
                            }
                        }
                        j++;
                        p3 = p2;
                    }
                    p2 = p3;
                }
                if ((policyFlags & 2) != 0) {
                    if (r2 == null) {
                        StringBuilder stringBuilder = new StringBuilder(256);
                    } else {
                        r2.append(' ');
                    }
                    r2.append(p2.info.name);
                }
            }
            if (r2 != null) {
                N = pkg.services.size();
                r2 = null;
            } else {
                N = pkg.services.size();
                r2 = null;
            }
            for (i = 0; i < N; i++) {
                s2 = (Service) pkg.services.get(i);
                s2.info.processName = fixProcessName(pkg.applicationInfo.processName, s2.info.processName, pkg.applicationInfo.uid);
                this.mServices.addService(s2);
                if ((policyFlags & 2) != 0) {
                    if (r2 == null) {
                        stringBuilder = new StringBuilder(256);
                    } else {
                        r2.append(' ');
                    }
                    r2.append(s2.info.name);
                }
            }
            if (r2 != null) {
                N = pkg.receivers.size();
                r2 = null;
            } else {
                N = pkg.receivers.size();
                r2 = null;
            }
            for (i = 0; i < N; i++) {
                a2 = (Activity) pkg.receivers.get(i);
                a2.info.processName = fixProcessName(pkg.applicationInfo.processName, a2.info.processName, pkg.applicationInfo.uid);
                this.mReceivers.addActivity(a2, HwBroadcastRadarUtil.KEY_RECEIVER);
                if ((policyFlags & 2) != 0) {
                    if (r2 == null) {
                        stringBuilder = new StringBuilder(256);
                    } else {
                        r2.append(' ');
                    }
                    r2.append(a2.info.name);
                }
            }
            if (r2 != null) {
                N = pkg.activities.size();
                r2 = null;
            } else {
                N = pkg.activities.size();
                r2 = null;
            }
            for (i = 0; i < N; i++) {
                a2 = (Activity) pkg.activities.get(i);
                a2.info.processName = fixProcessName(pkg.applicationInfo.processName, a2.info.processName, pkg.applicationInfo.uid);
                this.mActivities.addActivity(a2, "activity");
                if ((policyFlags & 2) != 0) {
                    if (r2 == null) {
                        stringBuilder = new StringBuilder(256);
                    } else {
                        r2.append(' ');
                    }
                    r2.append(a2.info.name);
                }
            }
            if (r2 != null) {
                N = pkg.permissionGroups.size();
                r2 = null;
            } else {
                N = pkg.permissionGroups.size();
                r2 = null;
            }
            for (i = 0; i < N; i++) {
                PermissionGroup pg = (PermissionGroup) pkg.permissionGroups.get(i);
                PermissionGroup cur = (PermissionGroup) this.mPermissionGroups.get(pg.info.name);
                if (cur == null) {
                    this.mPermissionGroups.put(pg.info.name, pg);
                    if ((policyFlags & 2) != 0) {
                        if (r2 == null) {
                            stringBuilder = new StringBuilder(256);
                        } else {
                            r2.append(' ');
                        }
                        r2.append(pg.info.name);
                    }
                } else {
                    Slog.w(TAG, "Permission group " + pg.info.name + " from package " + pg.info.packageName + " ignored: original from " + cur.info.packageName);
                    if ((policyFlags & 2) != 0) {
                        if (r2 == null) {
                            stringBuilder = new StringBuilder(256);
                        } else {
                            r2.append(' ');
                        }
                        r2.append("DUP:");
                        r2.append(pg.info.name);
                    }
                }
            }
            if (r2 != null) {
                N = pkg.permissions.size();
                r2 = null;
            } else {
                N = pkg.permissions.size();
                r2 = null;
            }
            for (i = 0; i < N; i++) {
                ArrayMap<String, BasePermission> permissionMap;
                Permission p4 = (Permission) pkg.permissions.get(i);
                PermissionInfo permissionInfo = p4.info;
                permissionInfo.flags &= -1073741825;
                if (pkg.applicationInfo.targetSdkVersion > 22) {
                    p4.group = (PermissionGroup) this.mPermissionGroups.get(p4.info.group);
                    if (p4.info.group != null && p4.group == null) {
                        Slog.w(TAG, "Permission " + p4.info.name + " from package " + p4.info.packageName + " in an unknown group " + p4.info.group);
                    }
                }
                if (p4.tree) {
                    permissionMap = this.mSettings.mPermissionTrees;
                } else {
                    permissionMap = this.mSettings.mPermissions;
                }
                BasePermission bp = (BasePermission) permissionMap.get(p4.info.name);
                if (!(bp == null || Objects.equals(bp.sourcePackage, p4.info.packageName))) {
                    boolean currentOwnerIsSystem;
                    if (bp.perm != null) {
                        currentOwnerIsSystem = isSystemApp(bp.perm.owner);
                    } else {
                        currentOwnerIsSystem = false;
                    }
                    if (isSystemApp(p4.owner)) {
                        if (bp.type == 1 && bp.perm == null) {
                            bp.packageSetting = pkgSetting;
                            bp.perm = p4;
                            bp.uid = pkg.applicationInfo.uid;
                            bp.sourcePackage = p4.info.packageName;
                            permissionInfo = p4.info;
                            permissionInfo.flags |= 1073741824;
                        } else if (!currentOwnerIsSystem) {
                            reportSettingsProblem(5, "New decl " + p4.owner + " of permission  " + p4.info.name + " is system; overriding " + bp.sourcePackage);
                            bp = null;
                        }
                    }
                }
                if (bp == null) {
                    BasePermission basePermission = new BasePermission(p4.info.name, p4.info.packageName, 0);
                    permissionMap.put(p4.info.name, basePermission);
                }
                if (bp.perm == null) {
                    if (bp.sourcePackage == null || bp.sourcePackage.equals(p4.info.packageName)) {
                        BasePermission tree = findPermissionTreeLP(p4.info.name);
                        if (tree == null || tree.sourcePackage.equals(p4.info.packageName)) {
                            bp.packageSetting = pkgSetting;
                            bp.perm = p4;
                            bp.uid = pkg.applicationInfo.uid;
                            bp.sourcePackage = p4.info.packageName;
                            permissionInfo = p4.info;
                            permissionInfo.flags |= 1073741824;
                            if ((policyFlags & 2) != 0) {
                                if (r2 == null) {
                                    stringBuilder = new StringBuilder(256);
                                } else {
                                    r2.append(' ');
                                }
                                r2.append(p4.info.name);
                            }
                        } else {
                            Slog.w(TAG, "Permission " + p4.info.name + " from package " + p4.info.packageName + " ignored: base tree " + tree.name + " is from package " + tree.sourcePackage);
                        }
                    } else {
                        Slog.w(TAG, "Permission " + p4.info.name + " from package " + p4.info.packageName + " ignored: original from " + bp.sourcePackage);
                    }
                } else if ((policyFlags & 2) != 0) {
                    if (r2 == null) {
                        stringBuilder = new StringBuilder(256);
                    } else {
                        r2.append(' ');
                    }
                    r2.append("DUP:");
                    r2.append(p4.info.name);
                }
                if (bp.perm == p4) {
                    bp.protectionLevel = p4.info.protectionLevel;
                }
            }
            if (r2 != null) {
                N = pkg.instrumentation.size();
                r2 = null;
            } else {
                N = pkg.instrumentation.size();
                r2 = null;
            }
            for (i = 0; i < N; i++) {
                Instrumentation a3 = (Instrumentation) pkg.instrumentation.get(i);
                a3.info.packageName = pkg.applicationInfo.packageName;
                a3.info.sourceDir = pkg.applicationInfo.sourceDir;
                a3.info.publicSourceDir = pkg.applicationInfo.publicSourceDir;
                a3.info.splitSourceDirs = pkg.applicationInfo.splitSourceDirs;
                a3.info.splitPublicSourceDirs = pkg.applicationInfo.splitPublicSourceDirs;
                a3.info.dataDir = pkg.applicationInfo.dataDir;
                a3.info.deviceProtectedDataDir = pkg.applicationInfo.deviceProtectedDataDir;
                a3.info.credentialProtectedDataDir = pkg.applicationInfo.credentialProtectedDataDir;
                a3.info.nativeLibraryDir = pkg.applicationInfo.nativeLibraryDir;
                a3.info.secondaryNativeLibraryDir = pkg.applicationInfo.secondaryNativeLibraryDir;
                this.mInstrumentation.put(a3.getComponentName(), a3);
                if ((policyFlags & 2) != 0) {
                    if (r2 == null) {
                        stringBuilder = new StringBuilder(256);
                    } else {
                        r2.append(' ');
                    }
                    r2.append(a3.info.name);
                }
            }
            if (r2 != null) {
            }
            if (pkg.protectedBroadcasts != null) {
                N = pkg.protectedBroadcasts.size();
                for (i = 0; i < N; i++) {
                    this.mProtectedBroadcasts.add((String) pkg.protectedBroadcasts.get(i));
                }
            }
            pkgSetting.setTimeStamp(scanFileTime);
            if (pkg.mOverlayTarget != null) {
                if (!(pkg.mOverlayTarget == null || pkg.mOverlayTarget.equals(PLATFORM_PACKAGE_NAME))) {
                    if (!this.mOverlays.containsKey(pkg.mOverlayTarget)) {
                        this.mOverlays.put(pkg.mOverlayTarget, new ArrayMap());
                    }
                    ((ArrayMap) this.mOverlays.get(pkg.mOverlayTarget)).put(pkg.packageName, pkg);
                    Package orig2 = (Package) this.mPackages.get(pkg.mOverlayTarget);
                    if (!(orig2 == null || createIdmapForPackagePairLI(orig2, pkg))) {
                        createIdmapFailed = true;
                    }
                }
            } else if (this.mOverlays.containsKey(pkg.packageName) && !pkg.packageName.equals(PLATFORM_PACKAGE_NAME)) {
                createIdmapsForPackageLI(pkg);
            }
        }
        Trace.traceEnd(262144);
        if (!createIdmapFailed) {
            return pkg;
        }
        throw new PackageManagerException(-7, "scanPackageLI failed to createIdmap");
    }

    private void maybeRenameForeignDexMarkers(Package existing, Package update, UserHandle user) {
        if (existing.applicationInfo != null && update.applicationInfo != null) {
            File oldCodePath = new File(existing.applicationInfo.getCodePath());
            File newCodePath = new File(update.applicationInfo.getCodePath());
            if (!Objects.equals(oldCodePath, newCodePath)) {
                try {
                    File canonicalNewCodePath = new File(PackageManagerServiceUtils.realpath(newCodePath));
                    String oldMarkerPrefix = new File(canonicalNewCodePath.getParentFile(), oldCodePath.getName()).getAbsolutePath().replace('/', '@');
                    if (!oldMarkerPrefix.endsWith("@")) {
                        oldMarkerPrefix = oldMarkerPrefix + "@";
                    }
                    String newMarkerPrefix = canonicalNewCodePath.getAbsolutePath().replace('/', '@');
                    if (!newMarkerPrefix.endsWith("@")) {
                        newMarkerPrefix = newMarkerPrefix + "@";
                    }
                    List<String> updatedPaths = update.getAllCodePathsExcludingResourceOnly();
                    List<String> markerSuffixes = new ArrayList(updatedPaths.size());
                    for (String updatedPath : updatedPaths) {
                        markerSuffixes.add(new File(updatedPath).getName().replace('/', '@'));
                    }
                    for (int userId : resolveUserIds(user.getIdentifier())) {
                        File profileDir = Environment.getDataProfilesDeForeignDexDirectory(userId);
                        for (String markerSuffix : markerSuffixes) {
                            File oldForeignUseMark = new File(profileDir, oldMarkerPrefix + markerSuffix);
                            File newForeignUseMark = new File(profileDir, newMarkerPrefix + markerSuffix);
                            if (oldForeignUseMark.exists()) {
                                try {
                                    Os.rename(oldForeignUseMark.getAbsolutePath(), newForeignUseMark.getAbsolutePath());
                                } catch (ErrnoException e) {
                                    Slog.w(TAG, "Failed to rename foreign use marker", e);
                                    oldForeignUseMark.delete();
                                }
                            }
                        }
                    }
                } catch (IOException e2) {
                    Slog.w(TAG, "Failed to get canonical path.", e2);
                }
            }
        }
    }

    private void derivePackageAbi(Package pkg, File scanFile, String cpuAbiOverride, boolean extractLibs) throws PackageManagerException {
        setNativeLibraryPaths(pkg);
        if (pkg.isForwardLocked() || pkg.applicationInfo.isExternalAsec() || ((isSystemApp(pkg) && !pkg.isUpdatedSystemApp()) || isPackagePathWithNoSysFlag(scanFile))) {
            extractLibs = false;
        }
        String nativeLibraryRootStr = pkg.applicationInfo.nativeLibraryRootDir;
        boolean useIsaSpecificSubdirs = pkg.applicationInfo.nativeLibraryRootRequiresIsa;
        AutoCloseable autoCloseable = null;
        try {
            autoCloseable = Handle.create(pkg);
            File nativeLibraryRoot = new File(nativeLibraryRootStr);
            pkg.applicationInfo.primaryCpuAbi = null;
            pkg.applicationInfo.secondaryCpuAbi = null;
            if (isMultiArch(pkg.applicationInfo)) {
                if (!(pkg.cpuAbiOverride == null || INSTALL_PACKAGE_SUFFIX.equals(pkg.cpuAbiOverride))) {
                    Slog.w(TAG, "Ignoring abiOverride for multi arch application.");
                }
                int abi32 = -114;
                int abi64 = -114;
                if (Build.SUPPORTED_32_BIT_ABIS.length > 0) {
                    if (extractLibs) {
                        abi32 = NativeLibraryHelper.copyNativeBinariesForSupportedAbi(autoCloseable, nativeLibraryRoot, Build.SUPPORTED_32_BIT_ABIS, useIsaSpecificSubdirs);
                    } else {
                        abi32 = NativeLibraryHelper.findSupportedAbi(autoCloseable, Build.SUPPORTED_32_BIT_ABIS);
                    }
                }
                maybeThrowExceptionForMultiArchCopy("Error unpackaging 32 bit native libs for multiarch app.", abi32);
                if (Build.SUPPORTED_64_BIT_ABIS.length > 0) {
                    if (extractLibs) {
                        abi64 = NativeLibraryHelper.copyNativeBinariesForSupportedAbi(autoCloseable, nativeLibraryRoot, Build.SUPPORTED_64_BIT_ABIS, useIsaSpecificSubdirs);
                    } else {
                        abi64 = NativeLibraryHelper.findSupportedAbi(autoCloseable, Build.SUPPORTED_64_BIT_ABIS);
                    }
                }
                maybeThrowExceptionForMultiArchCopy("Error unpackaging 64 bit native libs for multiarch app.", abi64);
                if (abi64 >= 0) {
                    pkg.applicationInfo.primaryCpuAbi = Build.SUPPORTED_64_BIT_ABIS[abi64];
                }
                if (abi32 >= 0) {
                    String abi = Build.SUPPORTED_32_BIT_ABIS[abi32];
                    if (abi64 < 0) {
                        pkg.applicationInfo.primaryCpuAbi = abi;
                    } else if (pkg.use32bitAbi) {
                        pkg.applicationInfo.secondaryCpuAbi = pkg.applicationInfo.primaryCpuAbi;
                        pkg.applicationInfo.primaryCpuAbi = abi;
                    } else {
                        pkg.applicationInfo.secondaryCpuAbi = abi;
                    }
                }
                Flog.i(203, "derivePackageAbi for MultiArch : " + pkg + ", path " + scanFile + ", need extractLibs " + extractLibs + ", abi32 " + abi32 + ", abi64 " + abi64);
            } else {
                int copyRet;
                String[] abiList = cpuAbiOverride != null ? new String[]{cpuAbiOverride} : Build.SUPPORTED_ABIS;
                boolean needsRenderScriptOverride = false;
                if (Build.SUPPORTED_64_BIT_ABIS.length > 0 && cpuAbiOverride == null && NativeLibraryHelper.hasRenderscriptBitcode(autoCloseable)) {
                    abiList = Build.SUPPORTED_32_BIT_ABIS;
                    needsRenderScriptOverride = true;
                }
                if (extractLibs) {
                    copyRet = NativeLibraryHelper.copyNativeBinariesForSupportedAbi(autoCloseable, nativeLibraryRoot, abiList, useIsaSpecificSubdirs);
                } else {
                    copyRet = NativeLibraryHelper.findSupportedAbi(autoCloseable, abiList);
                }
                if (copyRet >= 0 || copyRet == -114) {
                    if (this.mSettings != null) {
                        synchronized (this.mPackages) {
                            addPackagesAbiLPw(pkg.packageName, copyRet, true, pkg.mVersionCode);
                        }
                    }
                    if (copyRet >= 0) {
                        pkg.applicationInfo.primaryCpuAbi = abiList[copyRet];
                    } else if (copyRet == -114 && cpuAbiOverride != null) {
                        pkg.applicationInfo.primaryCpuAbi = cpuAbiOverride;
                    } else if (needsRenderScriptOverride) {
                        pkg.applicationInfo.primaryCpuAbi = abiList[0];
                    }
                    Flog.i(203, "derivePackageAbi for non MultiArch : " + pkg + ", path " + scanFile + ", need extractLibs " + extractLibs + ", primaryCpuAbi " + pkg.applicationInfo.primaryCpuAbi);
                } else {
                    throw new PackageManagerException(-110, "Error unpackaging native libs for app, errorCode=" + copyRet);
                }
            }
            IoUtils.closeQuietly(autoCloseable);
        } catch (IOException ioe) {
            Slog.e(TAG, "Unable to get canonical file " + ioe.toString());
            IoUtils.closeQuietly(autoCloseable);
        } catch (Throwable th) {
            IoUtils.closeQuietly(autoCloseable);
        }
        setNativeLibraryPaths(pkg);
    }

    private void adjustCpuAbisForSharedUserLPw(Set<PackageSetting> packagesForUser, Package scannedPackage, boolean bootComplete) {
        String requiredInstructionSet = null;
        if (!(scannedPackage == null || scannedPackage.applicationInfo.primaryCpuAbi == null)) {
            requiredInstructionSet = VMRuntime.getInstructionSet(scannedPackage.applicationInfo.primaryCpuAbi);
        }
        PackageSetting requirer = null;
        for (PackageSetting ps : packagesForUser) {
            if ((scannedPackage == null || !scannedPackage.packageName.equals(ps.name)) && ps.primaryCpuAbiString != null) {
                String instructionSet = VMRuntime.getInstructionSet(ps.primaryCpuAbiString);
                if (!(requiredInstructionSet == null || instructionSet.equals(requiredInstructionSet))) {
                    Object obj;
                    StringBuilder append = new StringBuilder().append("Instruction set mismatch, ");
                    if (requirer == null) {
                        obj = "[caller]";
                    } else {
                        PackageSetting packageSetting = requirer;
                    }
                    Slog.w(TAG, append.append(obj).append(" requires ").append(requiredInstructionSet).append(" whereas ").append(ps).append(" requires ").append(instructionSet).toString());
                }
                if (requiredInstructionSet == null) {
                    requiredInstructionSet = instructionSet;
                    requirer = ps;
                }
            }
        }
        if (requiredInstructionSet != null) {
            String adjustedAbi;
            if (requirer != null) {
                adjustedAbi = requirer.primaryCpuAbiString;
                if (scannedPackage != null) {
                    scannedPackage.applicationInfo.primaryCpuAbi = adjustedAbi;
                }
            } else {
                adjustedAbi = scannedPackage.applicationInfo.primaryCpuAbi;
            }
            for (PackageSetting ps2 : packagesForUser) {
                if ((scannedPackage == null || !scannedPackage.packageName.equals(ps2.name)) && ps2.primaryCpuAbiString == null) {
                    if (SystemProperties.get("persist.sys.shareduid_abi_check", "1").equals("0")) {
                        ps2.primaryCpuAbiString = adjustedAbi;
                    }
                    if (!(ps2.pkg == null || ps2.pkg.applicationInfo == null || TextUtils.equals(adjustedAbi, ps2.pkg.applicationInfo.primaryCpuAbi))) {
                        if (SystemProperties.get("persist.sys.shareduid_abi_check", "1").equals("0")) {
                            ps2.pkg.applicationInfo.primaryCpuAbi = adjustedAbi;
                        }
                        String str = TAG;
                        StringBuilder append2 = new StringBuilder().append("Adjusting ABI for ").append(ps2.name).append(" to ").append(adjustedAbi).append(" (requirer=");
                        String str2 = requirer == null ? "null" : requirer.pkg == null ? "null" : requirer.pkg.packageName;
                        Slog.i(str, append2.append(str2).append(", scannedPackage=").append(scannedPackage != null ? scannedPackage.packageName : "null").append(")").toString());
                        if (SystemProperties.get("persist.sys.shareduid_abi_check", "1").equals("0")) {
                            try {
                                this.mInstaller.rmdex(ps2.codePathString, InstructionSets.getDexCodeInstructionSet(InstructionSets.getPreferredInstructionSet()));
                            } catch (InstallerException e) {
                            }
                        }
                    }
                }
            }
        }
    }

    void setUpCustomResolverActivity(Package pkg) {
        synchronized (this.mPackages) {
            this.mResolverReplaced = true;
            this.mResolveActivity.applicationInfo = pkg.applicationInfo;
            this.mResolveActivity.name = this.mCustomResolverComponentName.getClassName();
            this.mResolveActivity.packageName = pkg.applicationInfo.packageName;
            this.mResolveActivity.processName = pkg.applicationInfo.packageName;
            this.mResolveActivity.launchMode = 0;
            this.mResolveActivity.flags = 288;
            this.mResolveActivity.theme = 0;
            this.mResolveActivity.exported = true;
            this.mResolveActivity.enabled = true;
            this.mResolveInfo.activityInfo = this.mResolveActivity;
            this.mResolveInfo.priority = 0;
            this.mResolveInfo.preferredOrder = 0;
            this.mResolveInfo.match = 0;
            this.mResolveComponentName = this.mCustomResolverComponentName;
            Slog.i(TAG, "Replacing default ResolverActivity with custom activity: " + this.mResolveComponentName);
        }
    }

    private void setUpEphemeralInstallerActivityLP(ComponentName installerComponent) {
        Package pkg = (Package) this.mPackages.get(installerComponent.getPackageName());
        this.mEphemeralInstallerActivity.applicationInfo = pkg.applicationInfo;
        this.mEphemeralInstallerActivity.name = this.mEphemeralInstallerComponent.getClassName();
        this.mEphemeralInstallerActivity.packageName = pkg.applicationInfo.packageName;
        this.mEphemeralInstallerActivity.processName = pkg.applicationInfo.packageName;
        this.mEphemeralInstallerActivity.launchMode = 0;
        this.mEphemeralInstallerActivity.flags = 288;
        this.mEphemeralInstallerActivity.theme = 0;
        this.mEphemeralInstallerActivity.exported = true;
        this.mEphemeralInstallerActivity.enabled = true;
        this.mEphemeralInstallerInfo.activityInfo = this.mEphemeralInstallerActivity;
        this.mEphemeralInstallerInfo.priority = 0;
        this.mEphemeralInstallerInfo.preferredOrder = 0;
        this.mEphemeralInstallerInfo.match = 0;
    }

    private static String calculateBundledApkRoot(String codePathString) {
        File codeRoot;
        File codePath = new File(codePathString);
        if (FileUtils.contains(Environment.getRootDirectory(), codePath)) {
            codeRoot = Environment.getRootDirectory();
        } else if (FileUtils.contains(Environment.getOemDirectory(), codePath)) {
            codeRoot = Environment.getOemDirectory();
        } else if (FileUtils.contains(Environment.getVendorDirectory(), codePath)) {
            codeRoot = Environment.getVendorDirectory();
        } else {
            try {
                File f = codePath.getCanonicalFile();
                File parent = f.getParentFile();
                while (true) {
                    File tmp = parent.getParentFile();
                    if (tmp == null) {
                        break;
                    }
                    f = parent;
                    parent = tmp;
                }
                codeRoot = f;
                Slog.w(TAG, "Unrecognized code path " + codePath + " - using " + codeRoot);
            } catch (IOException e) {
                Slog.w(TAG, "Can't canonicalize code path " + codePath);
                return Environment.getRootDirectory().getPath();
            }
        }
        return codeRoot.getPath();
    }

    protected void setNativeLibraryPaths(Package pkg) {
        ApplicationInfo info = pkg.applicationInfo;
        String codePath = pkg.codePath;
        File codeFile = new File(codePath);
        boolean bundledApp = info.isSystemApp() && !info.isUpdatedSystemApp();
        boolean isExternalAsec = !info.isForwardLocked() ? info.isExternalAsec() : true;
        info.nativeLibraryRootDir = null;
        info.nativeLibraryRootRequiresIsa = false;
        info.nativeLibraryDir = null;
        info.secondaryNativeLibraryDir = null;
        if (PackageParser.isApkFile(codeFile)) {
            if (bundledApp) {
                String apkRoot = calculateBundledApkRoot(info.sourceDir);
                boolean is64Bit = VMRuntime.is64BitInstructionSet(InstructionSets.getPrimaryInstructionSet(info));
                String apkName = deriveCodePathName(codePath);
                String libDir = is64Bit ? "lib64" : "lib";
                info.nativeLibraryRootDir = Environment.buildPath(new File(apkRoot), new String[]{libDir, apkName}).getAbsolutePath();
                if (info.secondaryCpuAbi != null) {
                    String secondaryLibDir = is64Bit ? "lib" : "lib64";
                    info.secondaryNativeLibraryDir = Environment.buildPath(new File(apkRoot), new String[]{secondaryLibDir, apkName}).getAbsolutePath();
                }
            } else if (isExternalAsec) {
                info.nativeLibraryRootDir = new File(codeFile.getParentFile(), "lib").getAbsolutePath();
            } else {
                info.nativeLibraryRootDir = new File(this.mAppLib32InstallDir, deriveCodePathName(codePath)).getAbsolutePath();
            }
            info.nativeLibraryRootRequiresIsa = false;
            info.nativeLibraryDir = info.nativeLibraryRootDir;
            return;
        }
        info.nativeLibraryRootDir = new File(codeFile, "lib").getAbsolutePath();
        info.nativeLibraryRootRequiresIsa = true;
        info.nativeLibraryDir = new File(info.nativeLibraryRootDir, InstructionSets.getPrimaryInstructionSet(info)).getAbsolutePath();
        if (info.secondaryCpuAbi != null) {
            info.secondaryNativeLibraryDir = new File(info.nativeLibraryRootDir, VMRuntime.getInstructionSet(info.secondaryCpuAbi)).getAbsolutePath();
        }
    }

    private void setBundledAppAbisAndRoots(Package pkg, PackageSetting pkgSetting) {
        setBundledAppAbi(pkg, calculateBundledApkRoot(pkg.applicationInfo.sourceDir), deriveCodePathName(pkg.applicationInfo.getCodePath()));
        if (pkgSetting != null) {
            pkgSetting.primaryCpuAbiString = pkg.applicationInfo.primaryCpuAbi;
            pkgSetting.secondaryCpuAbiString = pkg.applicationInfo.secondaryCpuAbi;
        }
    }

    private static void setBundledAppAbi(Package pkg, String apkRoot, String apkName) {
        boolean has64BitLibs;
        boolean exists;
        File codeFile = new File(pkg.codePath);
        if (PackageParser.isApkFile(codeFile)) {
            has64BitLibs = new File(apkRoot, new File("lib64", apkName).getPath()).exists();
            exists = new File(apkRoot, new File("lib", apkName).getPath()).exists();
        } else {
            File rootDir = new File(codeFile, "lib");
            if (ArrayUtils.isEmpty(Build.SUPPORTED_64_BIT_ABIS) || TextUtils.isEmpty(Build.SUPPORTED_64_BIT_ABIS[0])) {
                has64BitLibs = false;
            } else {
                has64BitLibs = new File(rootDir, VMRuntime.getInstructionSet(Build.SUPPORTED_64_BIT_ABIS[0])).exists();
            }
            if (ArrayUtils.isEmpty(Build.SUPPORTED_32_BIT_ABIS) || TextUtils.isEmpty(Build.SUPPORTED_32_BIT_ABIS[0])) {
                exists = false;
            } else {
                exists = new File(rootDir, VMRuntime.getInstructionSet(Build.SUPPORTED_32_BIT_ABIS[0])).exists();
            }
        }
        if (has64BitLibs && !exists) {
            pkg.applicationInfo.primaryCpuAbi = Build.SUPPORTED_64_BIT_ABIS[0];
            pkg.applicationInfo.secondaryCpuAbi = null;
        } else if (exists && !has64BitLibs) {
            pkg.applicationInfo.primaryCpuAbi = Build.SUPPORTED_32_BIT_ABIS[0];
            pkg.applicationInfo.secondaryCpuAbi = null;
        } else if (exists && has64BitLibs) {
            if ((pkg.applicationInfo.flags & Integer.MIN_VALUE) == 0) {
                Slog.e(TAG, "Package " + pkg + " has multiple bundled libs, but is not multiarch.");
            }
            if (VMRuntime.is64BitInstructionSet(InstructionSets.getPreferredInstructionSet())) {
                pkg.applicationInfo.primaryCpuAbi = Build.SUPPORTED_64_BIT_ABIS[0];
                pkg.applicationInfo.secondaryCpuAbi = Build.SUPPORTED_32_BIT_ABIS[0];
                return;
            }
            pkg.applicationInfo.primaryCpuAbi = Build.SUPPORTED_32_BIT_ABIS[0];
            pkg.applicationInfo.secondaryCpuAbi = Build.SUPPORTED_64_BIT_ABIS[0];
        } else {
            pkg.applicationInfo.primaryCpuAbi = null;
            pkg.applicationInfo.secondaryCpuAbi = null;
        }
    }

    private void killApplication(String pkgName, int appId, String reason) {
        killApplication(pkgName, appId, -1, reason);
    }

    private void killApplication(String pkgName, int appId, int userId, String reason) {
        long token = Binder.clearCallingIdentity();
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            if (am != null) {
                try {
                    am.killApplication(pkgName, appId, userId, reason);
                } catch (RemoteException e) {
                }
            }
            Binder.restoreCallingIdentity(token);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void removePackageLI(Package pkg, boolean chatty) {
        PackageSetting ps = pkg.mExtras;
        if (ps != null) {
            removePackageLI(ps, chatty);
        }
        int childCount = pkg.childPackages != null ? pkg.childPackages.size() : 0;
        for (int i = 0; i < childCount; i++) {
            ps = ((Package) pkg.childPackages.get(i)).mExtras;
            if (ps != null) {
                removePackageLI(ps, chatty);
            }
        }
    }

    void removePackageLI(PackageSetting ps, boolean chatty) {
        synchronized (this.mPackages) {
            this.mPackages.remove(ps.name);
            if (this.mSettings != null) {
                removePackageAbiLPw(ps.name);
            }
            Package pkg = ps.pkg;
            if (pkg != null) {
                cleanPackageDataStructuresLILPw(pkg, chatty);
            }
        }
    }

    void removeInstalledPackageLI(Package pkg, boolean chatty) {
        synchronized (this.mPackages) {
            this.mPackages.remove(pkg.applicationInfo.packageName);
            cleanPackageDataStructuresLILPw(pkg, chatty);
            int childCount = pkg.childPackages != null ? pkg.childPackages.size() : 0;
            for (int i = 0; i < childCount; i++) {
                Package childPkg = (Package) pkg.childPackages.get(i);
                this.mPackages.remove(childPkg.applicationInfo.packageName);
                cleanPackageDataStructuresLILPw(childPkg, chatty);
            }
        }
    }

    void cleanPackageDataStructuresLILPw(Package pkg, boolean chatty) {
        int i;
        int N = pkg.providers.size();
        for (i = 0; i < N; i++) {
            Provider p = (Provider) pkg.providers.get(i);
            this.mProviders.removeProvider(p);
            if (p.info.authority != null) {
                String[] names = p.info.authority.split(";");
                for (int j = 0; j < names.length; j++) {
                    if (this.mProvidersByAuthority.get(names[j]) == p) {
                        this.mProvidersByAuthority.remove(names[j]);
                    }
                }
            }
        }
        N = pkg.services.size();
        StringBuilder r = null;
        for (i = 0; i < N; i++) {
            Service s = (Service) pkg.services.get(i);
            this.mServices.removeService(s);
            if (chatty) {
                if (r == null) {
                    r = new StringBuilder(256);
                } else {
                    r.append(' ');
                }
                r.append(s.info.name);
            }
        }
        if (r != null) {
            N = pkg.receivers.size();
        } else {
            N = pkg.receivers.size();
        }
        for (i = 0; i < N; i++) {
            Activity a = (Activity) pkg.receivers.get(i);
            this.mReceivers.removeActivity(a, HwBroadcastRadarUtil.KEY_RECEIVER);
        }
        N = pkg.activities.size();
        for (i = 0; i < N; i++) {
            this.mActivities.removeActivity((Activity) pkg.activities.get(i), "activity");
        }
        N = pkg.permissions.size();
        for (i = 0; i < N; i++) {
            ArraySet<String> appOpPkgs;
            Permission p2 = (Permission) pkg.permissions.get(i);
            BasePermission bp = (BasePermission) this.mSettings.mPermissions.get(p2.info.name);
            if (bp == null) {
                bp = (BasePermission) this.mSettings.mPermissionTrees.get(p2.info.name);
            }
            if (bp != null && bp.perm == p2) {
                bp.perm = null;
            }
            if ((p2.info.protectionLevel & 64) != 0) {
                appOpPkgs = (ArraySet) this.mAppOpPermissionPackages.get(p2.info.name);
                if (appOpPkgs != null) {
                    appOpPkgs.remove(pkg.packageName);
                }
            }
        }
        N = pkg.requestedPermissions.size();
        for (i = 0; i < N; i++) {
            String perm = (String) pkg.requestedPermissions.get(i);
            bp = (BasePermission) this.mSettings.mPermissions.get(perm);
            if (!(bp == null || (bp.protectionLevel & 64) == 0)) {
                appOpPkgs = (ArraySet) this.mAppOpPermissionPackages.get(perm);
                if (appOpPkgs != null) {
                    appOpPkgs.remove(pkg.packageName);
                    if (appOpPkgs.isEmpty()) {
                        this.mAppOpPermissionPackages.remove(perm);
                    }
                }
            }
        }
        N = pkg.instrumentation.size();
        for (i = 0; i < N; i++) {
            this.mInstrumentation.remove(((Instrumentation) pkg.instrumentation.get(i)).getComponentName());
        }
        if (!((pkg.applicationInfo.flags & 1) == 0 || pkg.libraryNames == null)) {
            for (i = 0; i < pkg.libraryNames.size(); i++) {
                String name = (String) pkg.libraryNames.get(i);
                SharedLibraryEntry cur = (SharedLibraryEntry) this.mSharedLibraries.get(name);
                if (!(cur == null || cur.apk == null || !cur.apk.equals(pkg.packageName))) {
                    this.mSharedLibraries.remove(name);
                }
            }
        }
        writePackagesAbi();
    }

    private static boolean hasPermission(Package pkgInfo, String perm) {
        for (int i = pkgInfo.permissions.size() - 1; i >= 0; i--) {
            if (((Permission) pkgInfo.permissions.get(i)).info.name.equals(perm)) {
                return true;
            }
        }
        return false;
    }

    private void updatePermissionsLPw(Package pkg, int flags) {
        updatePermissionsLPw(pkg.packageName, pkg, flags);
        int childCount = pkg.childPackages != null ? pkg.childPackages.size() : 0;
        for (int i = 0; i < childCount; i++) {
            Package childPkg = (Package) pkg.childPackages.get(i);
            updatePermissionsLPw(childPkg.packageName, childPkg, flags);
        }
    }

    private void updatePermissionsLPw(String changingPkg, Package pkgInfo, int flags) {
        String volumeUuid = null;
        if (pkgInfo != null) {
            volumeUuid = getVolumeUuidForPackage(pkgInfo);
        }
        updatePermissionsLPw(changingPkg, pkgInfo, volumeUuid, flags);
    }

    private void updatePermissionsLPw(String changingPkg, Package pkgInfo, String replaceVolumeUuid, int flags) {
        String volumeUuid;
        boolean replace;
        Iterator<BasePermission> it = this.mSettings.mPermissionTrees.values().iterator();
        while (it.hasNext()) {
            BasePermission bp = (BasePermission) it.next();
            if (bp.packageSetting == null) {
                bp.packageSetting = (PackageSettingBase) this.mSettings.mPackages.get(bp.sourcePackage);
            }
            if (bp.packageSetting == null) {
                Slog.w(TAG, "Removing dangling permission tree: " + bp.name + " from package " + bp.sourcePackage);
                it.remove();
            } else if (changingPkg != null && changingPkg.equals(bp.sourcePackage)) {
                if (pkgInfo == null || !hasPermission(pkgInfo, bp.name)) {
                    Slog.i(TAG, "Removing old permission tree: " + bp.name + " from package " + bp.sourcePackage);
                    flags |= 1;
                    it.remove();
                }
            }
        }
        it = this.mSettings.mPermissions.values().iterator();
        while (it.hasNext()) {
            bp = (BasePermission) it.next();
            if (bp.type == 2 && bp.packageSetting == null && bp.pendingInfo != null) {
                BasePermission tree = findPermissionTreeLP(bp.name);
                if (!(tree == null || tree.perm == null)) {
                    bp.packageSetting = tree.packageSetting;
                    bp.perm = new Permission(tree.perm.owner, new PermissionInfo(bp.pendingInfo));
                    bp.perm.info.packageName = tree.perm.info.packageName;
                    bp.perm.info.name = bp.name;
                    bp.uid = tree.uid;
                }
            }
            if (bp.packageSetting == null) {
                bp.packageSetting = (PackageSettingBase) this.mSettings.mPackages.get(bp.sourcePackage);
            }
            if (bp.packageSetting == null) {
                Slog.w(TAG, "Removing dangling permission: " + bp.name + " from package " + bp.sourcePackage);
                it.remove();
            } else if (changingPkg != null && changingPkg.equals(bp.sourcePackage)) {
                if (pkgInfo == null || !hasPermission(pkgInfo, bp.name)) {
                    Slog.i(TAG, "Removing old permission: " + bp.name + " from package " + bp.sourcePackage);
                    flags |= 1;
                    it.remove();
                }
            }
        }
        if ((flags & 1) != 0) {
            for (Package pkg : this.mPackages.values()) {
                if (pkg != pkgInfo) {
                    volumeUuid = getVolumeUuidForPackage(pkg);
                    if ((flags & 4) != 0) {
                        replace = Objects.equals(replaceVolumeUuid, volumeUuid);
                    } else {
                        replace = false;
                    }
                    grantPermissionsLPw(pkg, replace, changingPkg);
                }
            }
        }
        if (pkgInfo != null) {
            volumeUuid = getVolumeUuidForPackage(pkgInfo);
            if ((flags & 2) != 0) {
                replace = Objects.equals(replaceVolumeUuid, volumeUuid);
            } else {
                replace = false;
            }
            grantPermissionsLPw(pkgInfo, replace, changingPkg);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void grantPermissionsLPw(Package pkg, boolean replace, String packageOfInterest) {
        PackageSetting ps = pkg.mExtras;
        if (ps != null) {
            Trace.traceBegin(262144, "grantPermissions");
            PermissionsState permissionsState = ps.getPermissionsState();
            PermissionsState origPermissions = permissionsState;
            int[] currentUserIds = UserManagerService.getInstance().getUserIds();
            boolean runtimePermissionsRevoked = false;
            int[] changedRuntimePermissionUserIds = EMPTY_INT_ARRAY;
            boolean changedInstallPermission = false;
            if (replace) {
                ps.installPermissionsFixed = false;
                if (ps.isSharedUser()) {
                    changedRuntimePermissionUserIds = revokeUnusedSharedUserPermissionsLPw(ps.sharedUser, UserManagerService.getInstance().getUserIds());
                    if (!ArrayUtils.isEmpty(changedRuntimePermissionUserIds)) {
                        runtimePermissionsRevoked = true;
                    }
                } else {
                    PermissionsState permissionsState2 = new PermissionsState(permissionsState);
                    permissionsState.reset();
                }
            }
            permissionsState.setGlobalGids(this.mGlobalGids);
            int N = pkg.requestedPermissions.size();
            for (int i = 0; i < N; i++) {
                String name = (String) pkg.requestedPermissions.get(i);
                BasePermission bp = (BasePermission) this.mSettings.mPermissions.get(name);
                if (bp == null || bp.packageSetting == null) {
                    if (packageOfInterest != null) {
                        if (!packageOfInterest.equals(pkg.packageName)) {
                        }
                    }
                    Slog.w(TAG, "Unknown permission " + name + " in package " + pkg.packageName);
                } else {
                    String perm = bp.name;
                    boolean allowedSig = false;
                    int grant = 1;
                    if ((bp.protectionLevel & 64) != 0) {
                        ArraySet<String> pkgs = (ArraySet) this.mAppOpPermissionPackages.get(bp.name);
                        if (pkgs == null) {
                            pkgs = new ArraySet();
                            this.mAppOpPermissionPackages.put(bp.name, pkgs);
                        }
                        pkgs.add(pkg.packageName);
                    }
                    int level = bp.protectionLevel & 15;
                    boolean appSupportsRuntimePermissions = pkg.applicationInfo.targetSdkVersion >= 23;
                    switch (level) {
                        case 0:
                            grant = 2;
                            break;
                        case 1:
                            if (!appSupportsRuntimePermissions && !Build.PERMISSIONS_REVIEW_REQUIRED) {
                                grant = 2;
                                break;
                            }
                            if (!origPermissions.hasInstallPermission(bp.name)) {
                                if (!this.mPromoteSystemApps || !isSystemApp(ps) || !this.mExistingSystemPackages.contains(ps.name)) {
                                    grant = 3;
                                    break;
                                } else {
                                    grant = 4;
                                    break;
                                }
                            }
                            grant = 4;
                            break;
                        case 2:
                            allowedSig = grantSignaturePermission(perm, pkg, bp, origPermissions);
                            if (allowedSig) {
                                grant = 2;
                                break;
                            }
                            break;
                    }
                    if (grant != 1) {
                        if (!(isSystemApp(ps) || !ps.installPermissionsFixed || r5 || origPermissions.hasInstallPermission(perm) || isNewPlatformPermissionForPackage(perm, pkg))) {
                            grant = 1;
                        }
                        PermissionState permissionState;
                        int flags;
                        switch (grant) {
                            case 2:
                                for (int userId : UserManagerService.getInstance().getUserIds()) {
                                    if (origPermissions.getRuntimePermissionState(bp.name, userId) != null) {
                                        origPermissions.revokeRuntimePermission(bp, userId);
                                        origPermissions.updatePermissionFlags(bp, userId, 255, 0);
                                        changedRuntimePermissionUserIds = ArrayUtils.appendInt(changedRuntimePermissionUserIds, userId);
                                    }
                                }
                                if (permissionsState.grantInstallPermission(bp) == -1) {
                                    break;
                                }
                                changedInstallPermission = true;
                                break;
                            case 3:
                                for (int userId2 : UserManagerService.getInstance().getUserIds()) {
                                    permissionState = origPermissions.getRuntimePermissionState(bp.name, userId2);
                                    flags = permissionState != null ? permissionState.getFlags() : 0;
                                    if (origPermissions.hasRuntimePermission(bp.name, userId2)) {
                                        if (permissionsState.grantRuntimePermission(bp, userId2) == -1) {
                                            changedRuntimePermissionUserIds = ArrayUtils.appendInt(changedRuntimePermissionUserIds, userId2);
                                        }
                                        if (Build.PERMISSIONS_REVIEW_REQUIRED && appSupportsRuntimePermissions && (flags & 64) != 0) {
                                            flags &= -65;
                                            changedRuntimePermissionUserIds = ArrayUtils.appendInt(changedRuntimePermissionUserIds, userId2);
                                        }
                                    } else if (Build.PERMISSIONS_REVIEW_REQUIRED && !appSupportsRuntimePermissions) {
                                        if (PLATFORM_PACKAGE_NAME.equals(bp.sourcePackage) && (flags & 64) == 0) {
                                            flags |= 64;
                                            changedRuntimePermissionUserIds = ArrayUtils.appendInt(changedRuntimePermissionUserIds, userId2);
                                        }
                                        if (permissionsState.grantRuntimePermission(bp, userId2) != -1) {
                                            changedRuntimePermissionUserIds = ArrayUtils.appendInt(changedRuntimePermissionUserIds, userId2);
                                        }
                                    }
                                    permissionsState.updatePermissionFlags(bp, userId2, flags, flags);
                                }
                                break;
                            case 4:
                                permissionState = origPermissions.getInstallPermissionState(bp.name);
                                flags = permissionState != null ? permissionState.getFlags() : 0;
                                if (origPermissions.revokeInstallPermission(bp) != -1) {
                                    origPermissions.updatePermissionFlags(bp, -1, 255, 0);
                                    changedInstallPermission = true;
                                }
                                if ((flags & 8) != 0) {
                                    break;
                                }
                                for (int userId22 : currentUserIds) {
                                    if (permissionsState.grantRuntimePermission(bp, userId22) != -1) {
                                        permissionsState.updatePermissionFlags(bp, userId22, flags, flags);
                                        changedRuntimePermissionUserIds = ArrayUtils.appendInt(changedRuntimePermissionUserIds, userId22);
                                    }
                                }
                                break;
                            default:
                                if (packageOfInterest != null) {
                                    if (!packageOfInterest.equals(pkg.packageName)) {
                                        break;
                                    }
                                }
                                Slog.w(TAG, "Not granting permission " + perm + " to package " + pkg.packageName + " because it was previously installed without");
                                break;
                        }
                    } else if (permissionsState.revokeInstallPermission(bp) != -1) {
                        permissionsState.updatePermissionFlags(bp, -1, 255, 0);
                        changedInstallPermission = true;
                        Slog.i(TAG, "Un-granting permission " + perm + " from package " + pkg.packageName + " (protectionLevel=" + bp.protectionLevel + " flags=0x" + Integer.toHexString(pkg.applicationInfo.flags) + ")");
                    } else if ((bp.protectionLevel & 64) == 0) {
                        if (packageOfInterest != null) {
                            if (!packageOfInterest.equals(pkg.packageName)) {
                            }
                        }
                        Slog.w(TAG, "Not granting permission " + perm + " to package " + pkg.packageName + " (protectionLevel=" + bp.protectionLevel + " flags=0x" + Integer.toHexString(pkg.applicationInfo.flags) + ")");
                    }
                }
            }
            if ((changedInstallPermission || replace) && !ps.installPermissionsFixed) {
                if (isSystemApp(ps)) {
                }
                ps.installPermissionsFixed = true;
                for (int userId222 : changedRuntimePermissionUserIds) {
                    this.mSettings.writeRuntimePermissionsForUserLPr(userId222, runtimePermissionsRevoked);
                }
                Trace.traceEnd(262144);
            }
        }
    }

    private boolean isNewPlatformPermissionForPackage(String perm, Package pkg) {
        int NP = PackageParser.NEW_PERMISSIONS.length;
        int ip = 0;
        while (ip < NP) {
            NewPermissionInfo npi = PackageParser.NEW_PERMISSIONS[ip];
            if (!npi.name.equals(perm) || pkg.applicationInfo.targetSdkVersion >= npi.sdkVersion) {
                ip++;
            } else {
                Log.i(TAG, "Auto-granting " + perm + " to old pkg " + pkg.packageName);
                return true;
            }
        }
        return false;
    }

    private boolean grantSignaturePermission(String perm, Package pkg, BasePermission bp, PermissionsState origPermissions) {
        boolean z = true;
        if (!(compareSignatures(bp.packageSetting.signatures.mSignatures, pkg.mSignatures) == 0 || compareSignatures(this.mPlatformPackage.mSignatures, pkg.mSignatures) == 0)) {
            z = false;
        }
        if (!z && (bp.protectionLevel & 16) != 0) {
            if (!isSystemApp(pkg)) {
                z = getGMSPackagePermission(pkg);
            } else if (pkg.isUpdatedSystemApp()) {
                PackageSetting sysPs = this.mSettings.getDisabledSystemPkgLPr(pkg.packageName);
                if (sysPs == null || !sysPs.getPermissionsState().hasInstallPermission(perm)) {
                    if (sysPs != null && sysPs.pkg != null && sysPs.isPrivileged()) {
                        for (int j = 0; j < sysPs.pkg.requestedPermissions.size(); j++) {
                            if (perm.equals(sysPs.pkg.requestedPermissions.get(j))) {
                                z = true;
                                break;
                            }
                        }
                    }
                    if (pkg.parentPackage != null) {
                        PackageSetting disabledSysParentPs = this.mSettings.getDisabledSystemPkgLPr(pkg.parentPackage.packageName);
                        if (disabledSysParentPs != null && disabledSysParentPs.pkg != null && disabledSysParentPs.isPrivileged()) {
                            if (isPackageRequestingPermission(disabledSysParentPs.pkg, perm)) {
                                z = true;
                            } else if (disabledSysParentPs.pkg.childPackages != null) {
                                int count = disabledSysParentPs.pkg.childPackages.size();
                                for (int i = 0; i < count; i++) {
                                    if (isPackageRequestingPermission((Package) disabledSysParentPs.pkg.childPackages.get(i), perm)) {
                                        z = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else if (sysPs.isPrivileged()) {
                    z = true;
                }
            } else {
                z = isPrivilegedApp(pkg);
            }
        }
        if (!z) {
            if (!(z || (bp.protectionLevel & 128) == 0 || pkg.applicationInfo.targetSdkVersion >= 23)) {
                z = true;
            }
            if (!(z || (bp.protectionLevel & 256) == 0 || !pkg.packageName.equals(this.mRequiredInstallerPackage))) {
                z = true;
            }
            if (!(z || (bp.protectionLevel & 512) == 0 || !pkg.packageName.equals(this.mRequiredVerifierPackage))) {
                z = true;
            }
            if (!(z || (bp.protectionLevel & 1024) == 0 || !isSystemApp(pkg))) {
                z = true;
            }
            if (!(z || (bp.protectionLevel & 32) == 0)) {
                z = origPermissions.hasInstallPermission(perm);
            }
            if (!(z || (bp.protectionLevel & 2048) == 0 || !pkg.packageName.equals(this.mSetupWizardPackage))) {
                z = true;
            }
        }
        if (z) {
            return z;
        }
        return getHwCertificationPermission(z, pkg, perm);
    }

    private boolean isPackageRequestingPermission(Package pkg, String permission) {
        int permCount = pkg.requestedPermissions.size();
        for (int j = 0; j < permCount; j++) {
            if (permission.equals((String) pkg.requestedPermissions.get(j))) {
                return true;
            }
        }
        return false;
    }

    void sendPackageBroadcast(String action, String pkg, Bundle extras, int flags, String targetPkg, IIntentReceiver finishedReceiver, int[] userIds) {
        final int[] iArr = userIds;
        final String str = action;
        final String str2 = pkg;
        final Bundle bundle = extras;
        final String str3 = targetPkg;
        final int i = flags;
        final IIntentReceiver iIntentReceiver = finishedReceiver;
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    IActivityManager am = ActivityManagerNative.getDefault();
                    if (am != null) {
                        int[] resolvedUserIds;
                        if (iArr == null) {
                            resolvedUserIds = am.getRunningUserIds();
                        } else {
                            resolvedUserIds = iArr;
                        }
                        for (int id : resolvedUserIds) {
                            Intent intent = new Intent(str, str2 != null ? Uri.fromParts(HwBroadcastRadarUtil.KEY_PACKAGE, str2, null) : null);
                            if (bundle != null) {
                                intent.putExtras(bundle);
                            }
                            if (str3 != null) {
                                intent.setPackage(str3);
                            }
                            int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                            if (uid > 0 && UserHandle.getUserId(uid) != id) {
                                intent.putExtra("android.intent.extra.UID", UserHandle.getUid(id, UserHandle.getAppId(uid)));
                            }
                            intent.putExtra("android.intent.extra.user_handle", id);
                            intent.addFlags(i | 67108864);
                            am.broadcastIntent(null, intent, null, iIntentReceiver, 0, null, null, null, -1, null, iIntentReceiver != null, false, id);
                        }
                    }
                } catch (RemoteException e) {
                }
            }
        });
    }

    private boolean isExternalMediaAvailable() {
        return !this.mMediaMounted ? Environment.isExternalStorageEmulated() : true;
    }

    public PackageCleanItem nextPackageToClean(PackageCleanItem lastPackage) {
        synchronized (this.mPackages) {
            if (isExternalMediaAvailable()) {
                ArrayList<PackageCleanItem> pkgs = this.mSettings.mPackagesToBeCleaned;
                if (lastPackage != null) {
                    pkgs.remove(lastPackage);
                }
                if (pkgs.size() > 0) {
                    PackageCleanItem packageCleanItem = (PackageCleanItem) pkgs.get(0);
                    return packageCleanItem;
                }
                return null;
            }
            return null;
        }
    }

    void schedulePackageCleaning(String packageName, int userId, boolean andCode) {
        Message msg = this.mHandler.obtainMessage(7, userId, andCode ? 1 : 0, packageName);
        if (this.mSystemReady) {
            msg.sendToTarget();
            return;
        }
        if (this.mPostSystemReadyMessages == null) {
            this.mPostSystemReadyMessages = new ArrayList();
        }
        this.mPostSystemReadyMessages.add(msg);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void startCleaningPackages() {
        if (isExternalMediaAvailable()) {
            synchronized (this.mPackages) {
                if (this.mSettings.mPackagesToBeCleaned.isEmpty()) {
                }
            }
        }
    }

    public void installPackageAsUser(String originPath, IPackageInstallObserver2 observer, int installFlags, String installerPackageName, int userId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.INSTALL_PACKAGES", null);
        int callingUid = Binder.getCallingUid();
        enforceCrossUserPermission(callingUid, userId, true, true, "installPackageAsUser");
        if (isUserRestricted(userId, "no_install_apps")) {
            if (observer != null) {
                try {
                    observer.onPackageInstalled("", -111, null, null);
                } catch (RemoteException e) {
                }
            }
        } else if (!HwDeviceManager.disallowOp(6)) {
            UserHandle user;
            if (callingUid == 2000 || callingUid == 0) {
                installFlags |= 32;
            } else {
                installFlags = (installFlags & -33) & -65;
            }
            if ((installFlags & 64) != 0) {
                user = UserHandle.ALL;
            } else {
                user = new UserHandle(userId);
            }
            if ((installFlags & 256) == 0 || this.mContext.checkCallingOrSelfPermission("android.permission.INSTALL_GRANT_RUNTIME_PERMISSIONS") != -1) {
                File file = new File(originPath);
                OriginInfo origin = OriginInfo.fromUntrustedFile(file);
                Message msg = this.mHandler.obtainMessage(5);
                InstallParams params = new InstallParams(origin, null, observer, installFlags, installerPackageName, null, new VerificationInfo(null, null, -1, callingUid), user, null, null, null);
                params.setTraceMethod("installAsUser").setTraceCookie(System.identityHashCode(params));
                msg.obj = params;
                Trace.asyncTraceBegin(262144, "installAsUser", System.identityHashCode(msg.obj));
                Trace.asyncTraceBegin(262144, "queueInstall", System.identityHashCode(msg.obj));
                this.mHandler.sendMessage(msg);
                parseInstallerInfo(callingUid, file.toString());
                return;
            }
            throw new SecurityException("You need the android.permission.INSTALL_GRANT_RUNTIME_PERMISSIONS permission to use the PackageManager.INSTALL_GRANT_RUNTIME_PERMISSIONS flag");
        }
    }

    void installStage(String packageName, File stagedDir, String stagedCid, IPackageInstallObserver2 observer, SessionParams sessionParams, String installerPackageName, int installerUid, UserHandle user, Certificate[][] certificates) {
        OriginInfo origin;
        VerificationInfo verificationInfo = new VerificationInfo(sessionParams.originatingUri, sessionParams.referrerUri, sessionParams.originatingUid, installerUid);
        if (stagedDir != null) {
            origin = OriginInfo.fromStagedFile(stagedDir);
        } else {
            origin = OriginInfo.fromStagedContainer(stagedCid);
        }
        Message msg = this.mHandler.obtainMessage(5);
        InstallParams params = new InstallParams(origin, null, observer, sessionParams.installFlags, installerPackageName, sessionParams.volumeUuid, verificationInfo, user, sessionParams.abiOverride, sessionParams.grantedRuntimePermissions, certificates);
        params.setTraceMethod("installStage").setTraceCookie(System.identityHashCode(params));
        msg.obj = params;
        Trace.asyncTraceBegin(262144, "installStage", System.identityHashCode(msg.obj));
        Trace.asyncTraceBegin(262144, "queueInstall", System.identityHashCode(msg.obj));
        this.mHandler.sendMessage(msg);
    }

    private void sendPackageAddedForUser(String packageName, PackageSetting pkgSetting, int userId) {
        sendPackageAddedForUser(packageName, !isSystemApp(pkgSetting) ? isUpdatedSystemApp(pkgSetting) : true, pkgSetting.appId, userId);
    }

    private void sendPackageAddedForUser(String packageName, boolean isSystem, int appId, int userId) {
        Bundle extras = new Bundle(1);
        extras.putInt("android.intent.extra.UID", UserHandle.getUid(userId, appId));
        sendPackageBroadcast("android.intent.action.PACKAGE_ADDED", packageName, extras, 0, null, null, new int[]{userId});
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            if (isSystem && am.isUserRunning(userId, 0)) {
                am.broadcastIntent(null, new Intent("android.intent.action.BOOT_COMPLETED").addFlags(32).setPackage(packageName), null, null, 0, null, null, null, -1, null, false, false, userId);
            }
        } catch (Throwable e) {
            Slog.w(TAG, "Unable to bootstrap installed package", e);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean setApplicationHiddenSettingAsUser(String packageName, boolean hidden, int userId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USERS", null);
        enforceCrossUserPermission(Binder.getCallingUid(), userId, true, true, "setApplicationHiddenSetting for user " + userId);
        if (hidden && isPackageDeviceAdmin(packageName, userId)) {
            Slog.w(TAG, "Not hiding package " + packageName + ": has active device admin");
            return false;
        }
        long callingId = Binder.clearCallingIdentity();
        boolean sendAdded = false;
        boolean sendRemoved = false;
        try {
            synchronized (this.mPackages) {
                PackageSetting pkgSetting = (PackageSetting) this.mSettings.mPackages.get(packageName);
                if (pkgSetting == null) {
                    Binder.restoreCallingIdentity(callingId);
                    return false;
                } else if (pkgSetting.getHidden(userId) != hidden) {
                    pkgSetting.setHidden(hidden, userId);
                    this.mSettings.writePackageRestrictionsLPr(userId);
                    if (hidden) {
                        sendRemoved = true;
                    } else {
                        sendAdded = true;
                    }
                }
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    private void sendApplicationHiddenForUser(String packageName, PackageSetting pkgSetting, int userId) {
        PackageRemovedInfo info = new PackageRemovedInfo();
        info.removedPackage = packageName;
        info.removedUsers = new int[]{userId};
        info.uid = UserHandle.getUid(userId, pkgSetting.appId);
        info.sendPackageRemovedBroadcasts(true);
    }

    private void sendPackagesSuspendedForUser(String[] pkgList, int userId, boolean suspended) {
        if (pkgList.length > 0) {
            String str;
            Bundle extras = new Bundle(1);
            extras.putStringArray("android.intent.extra.changed_package_list", pkgList);
            if (suspended) {
                str = "android.intent.action.PACKAGES_SUSPENDED";
            } else {
                str = "android.intent.action.PACKAGES_UNSUSPENDED";
            }
            sendPackageBroadcast(str, null, extras, 1073741824, null, null, new int[]{userId});
        }
    }

    public boolean getApplicationHiddenSettingAsUser(String packageName, int userId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USERS", null);
        enforceCrossUserPermission(Binder.getCallingUid(), userId, true, false, "getApplicationHidden for user " + userId);
        long callingId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mPackages) {
                PackageSetting pkgSetting = (PackageSetting) this.mSettings.mPackages.get(packageName);
                if (pkgSetting == null) {
                    Binder.restoreCallingIdentity(callingId);
                    return true;
                }
                boolean hidden = pkgSetting.getHidden(userId);
                Binder.restoreCallingIdentity(callingId);
                return hidden;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int installExistingPackageAsUser(String packageName, int userId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.INSTALL_PACKAGES", null);
        enforceCrossUserPermission(Binder.getCallingUid(), userId, true, true, "installExistingPackage for user " + userId);
        if (isUserRestricted(userId, "no_install_apps")) {
            return -111;
        }
        long callingId = Binder.clearCallingIdentity();
        boolean installed = false;
        try {
            synchronized (this.mPackages) {
                PackageSetting pkgSetting = (PackageSetting) this.mSettings.mPackages.get(packageName);
                if (pkgSetting == null) {
                    Binder.restoreCallingIdentity(callingId);
                    return -3;
                } else if (!pkgSetting.getInstalled(userId)) {
                    pkgSetting.setInstalled(true, userId);
                    pkgSetting.setHidden(false, userId);
                    this.mSettings.writePackageRestrictionsLPr(userId);
                    installed = true;
                }
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    boolean isUserRestricted(int userId, String restrictionKey) {
        if (!sUserManager.getUserRestrictions(userId).getBoolean(restrictionKey, false)) {
            return false;
        }
        Log.w(TAG, "User is restricted: " + restrictionKey);
        return true;
    }

    public String[] setPackagesSuspendedAsUser(String[] packageNames, boolean suspended, int userId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_USERS", null);
        enforceCrossUserPermission(Binder.getCallingUid(), userId, true, true, "setPackagesSuspended for user " + userId);
        if (ArrayUtils.isEmpty(packageNames)) {
            return packageNames;
        }
        List<String> changedPackages = new ArrayList(packageNames.length);
        List<String> arrayList = new ArrayList(packageNames.length);
        long callingId = Binder.clearCallingIdentity();
        for (String packageName : packageNames) {
            boolean changed = false;
            synchronized (this.mPackages) {
                PackageSetting pkgSetting = (PackageSetting) this.mSettings.mPackages.get(packageName);
                if (pkgSetting == null) {
                    Slog.w(TAG, "Could not find package setting for package \"" + packageName + "\". Skipping suspending/un-suspending.");
                    arrayList.add(packageName);
                    try {
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(callingId);
                    }
                } else {
                    int appId = pkgSetting.appId;
                    if (pkgSetting.getSuspended(userId) != suspended) {
                        if (canSuspendPackageForUserLocked(packageName, userId)) {
                            pkgSetting.setSuspended(suspended, userId);
                            this.mSettings.writePackageRestrictionsLPr(userId);
                            changed = true;
                            changedPackages.add(packageName);
                        } else {
                            arrayList.add(packageName);
                        }
                    }
                    if (changed && suspended) {
                        killApplication(packageName, UserHandle.getUid(userId, appId), "suspending package");
                    }
                }
            }
        }
        Binder.restoreCallingIdentity(callingId);
        if (!changedPackages.isEmpty()) {
            sendPackagesSuspendedForUser((String[]) changedPackages.toArray(new String[changedPackages.size()]), userId, suspended);
        }
        return (String[]) arrayList.toArray(new String[arrayList.size()]);
    }

    public boolean isPackageSuspendedForUser(String packageName, int userId) {
        boolean suspended;
        enforceCrossUserPermission(Binder.getCallingUid(), userId, true, false, "isPackageSuspendedForUser for user " + userId);
        synchronized (this.mPackages) {
            PackageSetting pkgSetting = (PackageSetting) this.mSettings.mPackages.get(packageName);
            if (pkgSetting == null) {
                throw new IllegalArgumentException("Unknown target package: " + packageName);
            }
            suspended = pkgSetting.getSuspended(userId);
        }
        return suspended;
    }

    private boolean canSuspendPackageForUserLocked(String packageName, int userId) {
        if (isPackageDeviceAdmin(packageName, userId)) {
            Slog.w(TAG, "Cannot suspend/un-suspend package \"" + packageName + "\": has an active device admin");
            return false;
        } else if (packageName.equals(getActiveLauncherPackageName(userId))) {
            Slog.w(TAG, "Cannot suspend/un-suspend package \"" + packageName + "\": contains the active launcher");
            return false;
        } else if (packageName.equals(this.mRequiredInstallerPackage)) {
            Slog.w(TAG, "Cannot suspend/un-suspend package \"" + packageName + "\": required for package installation");
            return false;
        } else if (packageName.equals(this.mRequiredVerifierPackage)) {
            Slog.w(TAG, "Cannot suspend/un-suspend package \"" + packageName + "\": required for package verification");
            return false;
        } else if (!packageName.equals(getDefaultDialerPackageName(userId))) {
            return true;
        } else {
            Slog.w(TAG, "Cannot suspend/un-suspend package \"" + packageName + "\": is the default dialer");
            return false;
        }
    }

    private String getActiveLauncherPackageName(int userId) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        ResolveInfo resolveInfo = resolveIntent(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 65536, userId);
        if (resolveInfo == null) {
            return null;
        }
        return resolveInfo.activityInfo.packageName;
    }

    private String getDefaultDialerPackageName(int userId) {
        String defaultDialerPackageNameLPw;
        synchronized (this.mPackages) {
            defaultDialerPackageNameLPw = this.mSettings.getDefaultDialerPackageNameLPw(userId);
        }
        return defaultDialerPackageNameLPw;
    }

    public void verifyPendingInstall(int id, int verificationCode) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.PACKAGE_VERIFICATION_AGENT", "Only package verification agents can verify applications");
        Message msg = this.mHandler.obtainMessage(15);
        PackageVerificationResponse response = new PackageVerificationResponse(verificationCode, Binder.getCallingUid());
        msg.arg1 = id;
        msg.obj = response;
        this.mHandler.sendMessage(msg);
    }

    public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.PACKAGE_VERIFICATION_AGENT", "Only package verification agents can extend verification timeouts");
        PackageVerificationState state = (PackageVerificationState) this.mPendingVerification.get(id);
        PackageVerificationResponse response = new PackageVerificationResponse(verificationCodeAtTimeout, Binder.getCallingUid());
        if (millisecondsToDelay > 3600000) {
            millisecondsToDelay = 3600000;
        }
        if (millisecondsToDelay < 0) {
            millisecondsToDelay = 0;
        }
        if (!(verificationCodeAtTimeout == 1 || verificationCodeAtTimeout == -1)) {
        }
        if (state != null && !state.timeoutExtended()) {
            state.extendTimeout();
            Message msg = this.mHandler.obtainMessage(15);
            msg.arg1 = id;
            msg.obj = response;
            this.mHandler.sendMessageDelayed(msg, millisecondsToDelay);
        }
    }

    private void broadcastPackageVerified(int verificationId, Uri packageUri, int verificationCode, UserHandle user) {
        Intent intent = new Intent("android.intent.action.PACKAGE_VERIFIED");
        intent.setDataAndType(packageUri, PACKAGE_MIME_TYPE);
        intent.addFlags(1);
        intent.putExtra("android.content.pm.extra.VERIFICATION_ID", verificationId);
        intent.putExtra("android.content.pm.extra.VERIFICATION_RESULT", verificationCode);
        this.mContext.sendBroadcastAsUser(intent, user, "android.permission.PACKAGE_VERIFICATION_AGENT");
    }

    private ComponentName matchComponentForVerifier(String packageName, List<ResolveInfo> receivers) {
        ActivityInfo targetReceiver = null;
        int NR = receivers.size();
        for (int i = 0; i < NR; i++) {
            ResolveInfo info = (ResolveInfo) receivers.get(i);
            if (info.activityInfo != null && packageName.equals(info.activityInfo.packageName)) {
                targetReceiver = info.activityInfo;
                break;
            }
        }
        if (targetReceiver == null) {
            return null;
        }
        return new ComponentName(targetReceiver.packageName, targetReceiver.name);
    }

    private List<ComponentName> matchVerifiers(PackageInfoLite pkgInfo, List<ResolveInfo> receivers, PackageVerificationState verificationState) {
        if (pkgInfo.verifiers.length == 0) {
            return null;
        }
        List<ComponentName> sufficientVerifiers = new ArrayList(N + 1);
        for (VerifierInfo verifierInfo : pkgInfo.verifiers) {
            ComponentName comp = matchComponentForVerifier(verifierInfo.packageName, receivers);
            if (comp != null) {
                int verifierUid = getUidForVerifier(verifierInfo);
                if (verifierUid != -1) {
                    sufficientVerifiers.add(comp);
                    verificationState.addSufficientVerifier(verifierUid);
                }
            }
        }
        return sufficientVerifiers;
    }

    private int getUidForVerifier(VerifierInfo verifierInfo) {
        synchronized (this.mPackages) {
            Package pkg = (Package) this.mPackages.get(verifierInfo.packageName);
            if (pkg == null) {
                return -1;
            } else if (pkg.mSignatures.length != 1) {
                Slog.i(TAG, "Verifier package " + verifierInfo.packageName + " has more than one signature; ignoring");
                return -1;
            } else {
                try {
                    if (Arrays.equals(verifierInfo.publicKey.getEncoded(), pkg.mSignatures[0].getPublicKey().getEncoded())) {
                        int i = pkg.applicationInfo.uid;
                        return i;
                    }
                    Slog.i(TAG, "Verifier package " + verifierInfo.packageName + " does not have the expected public key; ignoring");
                    return -1;
                } catch (CertificateException e) {
                    return -1;
                }
            }
        }
    }

    public void finishPackageInstall(int token, boolean didLaunch) {
        enforceSystemOrRoot("Only the system is allowed to finish installs");
        Trace.asyncTraceEnd(262144, "restore", token);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(9, token, didLaunch ? 1 : 0));
    }

    private long getVerificationTimeout() {
        return Global.getLong(this.mContext.getContentResolver(), "verifier_timeout", 10000);
    }

    private int getDefaultVerificationResponse() {
        return Global.getInt(this.mContext.getContentResolver(), "verifier_default_response", 1);
    }

    private boolean isVerificationEnabled(int userId, int installFlags) {
        boolean z = true;
        if ((installFlags & 2048) != 0) {
            return false;
        }
        boolean ensureVerifyAppsEnabled = isUserRestricted(userId, "ensure_verify_apps");
        if ((installFlags & 32) != 0) {
            if (ActivityManager.isRunningInTestHarness()) {
                return false;
            }
            if (ensureVerifyAppsEnabled) {
                return true;
            }
            if (Global.getInt(this.mContext.getContentResolver(), "verifier_verify_adb_installs", 1) == 0) {
                return false;
            }
        }
        if (ensureVerifyAppsEnabled) {
            return true;
        }
        if (Global.getInt(this.mContext.getContentResolver(), "package_verifier_enable", 1) != 1) {
            z = false;
        }
        return z;
    }

    public void verifyIntentFilter(int id, int verificationCode, List<String> failedDomains) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.INTENT_FILTER_VERIFICATION_AGENT", "Only intentfilter verification agents can verify applications");
        Message msg = this.mHandler.obtainMessage(18);
        IntentFilterVerificationResponse response = new IntentFilterVerificationResponse(Binder.getCallingUid(), verificationCode, failedDomains);
        msg.arg1 = id;
        msg.obj = response;
        this.mHandler.sendMessage(msg);
    }

    public int getIntentVerificationStatus(String packageName, int userId) {
        int intentFilterVerificationStatusLPr;
        synchronized (this.mPackages) {
            intentFilterVerificationStatusLPr = this.mSettings.getIntentFilterVerificationStatusLPr(packageName, userId);
        }
        return intentFilterVerificationStatusLPr;
    }

    public boolean updateIntentVerificationStatus(String packageName, int status, int userId) {
        boolean result;
        this.mContext.enforceCallingOrSelfPermission("android.permission.SET_PREFERRED_APPLICATIONS", null);
        synchronized (this.mPackages) {
            result = this.mSettings.updateIntentFilterVerificationStatusLPw(packageName, status, userId);
        }
        if (result) {
            scheduleWritePackageRestrictionsLocked(userId);
        }
        return result;
    }

    public ParceledListSlice<IntentFilterVerificationInfo> getIntentFilterVerifications(String packageName) {
        ParceledListSlice<IntentFilterVerificationInfo> parceledListSlice;
        synchronized (this.mPackages) {
            parceledListSlice = new ParceledListSlice(this.mSettings.getIntentFilterVerificationsLPr(packageName));
        }
        return parceledListSlice;
    }

    public ParceledListSlice<IntentFilter> getAllIntentFilters(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return ParceledListSlice.emptyList();
        }
        synchronized (this.mPackages) {
            Package pkg = (Package) this.mPackages.get(packageName);
            if (pkg == null || pkg.activities == null) {
                ParceledListSlice<IntentFilter> emptyList = ParceledListSlice.emptyList();
                return emptyList;
            }
            int count = pkg.activities.size();
            ArrayList<IntentFilter> result = new ArrayList();
            for (int n = 0; n < count; n++) {
                Activity activity = (Activity) pkg.activities.get(n);
                if (activity.intents != null && activity.intents.size() > 0) {
                    result.addAll(activity.intents);
                }
            }
            emptyList = new ParceledListSlice(result);
            return emptyList;
        }
    }

    public boolean setDefaultBrowserPackageName(String packageName, int userId) {
        boolean result;
        this.mContext.enforceCallingOrSelfPermission("android.permission.SET_PREFERRED_APPLICATIONS", null);
        synchronized (this.mPackages) {
            result = this.mSettings.setDefaultBrowserPackageNameLPw(packageName, userId);
            if (packageName != null) {
                result |= updateIntentVerificationStatus(packageName, 2, userId);
                this.mDefaultPermissionPolicy.grantDefaultPermissionsToDefaultBrowserLPr(packageName, userId);
            }
        }
        return result;
    }

    public String getDefaultBrowserPackageName(int userId) {
        String defaultBrowserPackageNameLPw;
        synchronized (this.mPackages) {
            defaultBrowserPackageNameLPw = this.mSettings.getDefaultBrowserPackageNameLPw(userId);
        }
        return defaultBrowserPackageNameLPw;
    }

    private int getUnknownSourcesSettings() {
        return Secure.getInt(this.mContext.getContentResolver(), "install_non_market_apps", -1);
    }

    public void setInstallerPackageName(String targetPackage, String installerPackageName) {
        int uid = Binder.getCallingUid();
        synchronized (this.mPackages) {
            PackageSetting targetPackageSetting = (PackageSetting) this.mSettings.mPackages.get(targetPackage);
            if (targetPackageSetting == null) {
                throw new IllegalArgumentException("Unknown target package: " + targetPackage);
            }
            PackageSetting packageSetting;
            if (installerPackageName != null) {
                packageSetting = (PackageSetting) this.mSettings.mPackages.get(installerPackageName);
                if (packageSetting == null) {
                    throw new IllegalArgumentException("Unknown installer package: " + installerPackageName);
                }
            }
            packageSetting = null;
            Object obj = this.mSettings.getUserIdLPr(uid);
            if (obj != null) {
                Signature[] callerSignature;
                if (obj instanceof SharedUserSetting) {
                    callerSignature = ((SharedUserSetting) obj).signatures.mSignatures;
                } else if (obj instanceof PackageSetting) {
                    callerSignature = ((PackageSetting) obj).signatures.mSignatures;
                } else {
                    throw new SecurityException("Bad object " + obj + " for uid " + uid);
                }
                if (packageSetting == null || compareSignatures(callerSignature, packageSetting.signatures.mSignatures) == 0) {
                    if (targetPackageSetting.installerPackageName != null) {
                        PackageSetting setting = (PackageSetting) this.mSettings.mPackages.get(targetPackageSetting.installerPackageName);
                        if (!(setting == null || compareSignatures(callerSignature, setting.signatures.mSignatures) == 0)) {
                            throw new SecurityException("Caller does not have same cert as old installer package " + targetPackageSetting.installerPackageName);
                        }
                    }
                    targetPackageSetting.installerPackageName = installerPackageName;
                    if (installerPackageName != null) {
                        this.mSettings.mInstallerPackages.add(installerPackageName);
                    }
                    scheduleWriteSettingsLocked();
                } else {
                    throw new SecurityException("Caller does not have same cert as new installer package " + installerPackageName);
                }
            }
            throw new SecurityException("Unknown calling UID: " + uid);
        }
    }

    private void processPendingInstall(final InstallArgs args, final int currentStatus) {
        this.mHandler.post(new Runnable() {
            public void run() {
                PackageManagerService.this.mHandler.removeCallbacks(this);
                PackageInstalledInfo res = new PackageInstalledInfo();
                res.setReturnCode(currentStatus);
                res.uid = -1;
                res.pkg = null;
                res.removedInfo = null;
                if (res.returnCode == 1) {
                    args.doPreInstall(res.returnCode);
                    synchronized (PackageManagerService.this.mInstallLock) {
                        PackageManagerService.this.installPackageTracedLI(args, res);
                    }
                    args.doPostInstall(res.returnCode, res.uid);
                }
                boolean update = res.removedInfo != null ? res.removedInfo.removedPackage != null : false;
                boolean doRestore = !update ? (DumpState.DUMP_VERSION & (res.pkg == null ? 0 : res.pkg.applicationInfo.flags)) != 0 : false;
                if (PackageManagerService.this.mNextInstallToken < 0) {
                    PackageManagerService.this.mNextInstallToken = 1;
                }
                PackageManagerService packageManagerService = PackageManagerService.this;
                int token = packageManagerService.mNextInstallToken;
                packageManagerService.mNextInstallToken = token + 1;
                PackageManagerService.this.mRunningInstalls.put(token, new PostInstallData(args, res));
                if (res.returnCode == 1 && doRestore) {
                    IBackupManager bm = IBackupManager.Stub.asInterface(ServiceManager.getService("backup"));
                    if (bm != null) {
                        Trace.asyncTraceBegin(262144, "restore", token);
                        try {
                            if (bm.isBackupServiceActive(0)) {
                                bm.restoreAtInstall(res.pkg.applicationInfo.packageName, token);
                            } else {
                                doRestore = false;
                            }
                        } catch (RemoteException e) {
                        } catch (Exception e2) {
                            Slog.e(PackageManagerService.TAG, "Exception trying to enqueue restore", e2);
                            doRestore = false;
                        }
                    } else {
                        Slog.e(PackageManagerService.TAG, "Backup Manager not found!");
                        doRestore = false;
                    }
                }
                if (!doRestore) {
                    Trace.asyncTraceBegin(262144, "postInstall", token);
                    PackageManagerService.this.mHandler.sendMessage(PackageManagerService.this.mHandler.obtainMessage(9, token, 0));
                }
            }
        });
    }

    void notifyFirstLaunch(final String pkgName, final String installerPackage, final int userId) {
        this.mHandler.post(new Runnable() {
            public void run() {
                for (int i = 0; i < PackageManagerService.this.mRunningInstalls.size(); i++) {
                    PostInstallData data = (PostInstallData) PackageManagerService.this.mRunningInstalls.valueAt(i);
                    if (data.res.pkg != null && pkgName.equals(data.res.pkg.applicationInfo.packageName)) {
                        int uIndex = 0;
                        while (uIndex < data.res.newUsers.length) {
                            if (userId != data.res.newUsers[uIndex]) {
                                uIndex++;
                            } else {
                                return;
                            }
                        }
                        continue;
                    }
                }
                PackageManagerService.this.sendFirstLaunchBroadcast(pkgName, installerPackage, new int[]{userId});
            }
        });
    }

    private void sendFirstLaunchBroadcast(String pkgName, String installerPkg, int[] userIds) {
        sendPackageBroadcast("android.intent.action.PACKAGE_FIRST_LAUNCH", pkgName, null, 0, installerPkg, null, userIds);
    }

    private static long calculateDirectorySize(IMediaContainerService mcs, File[] paths) throws RemoteException {
        long result = 0;
        for (File path : paths) {
            result += mcs.calculateDirectorySize(path.getAbsolutePath());
        }
        return result;
    }

    private static void clearDirectory(IMediaContainerService mcs, File[] paths) {
        for (File path : paths) {
            try {
                mcs.clearDirectory(path.getAbsolutePath());
            } catch (RemoteException e) {
            }
        }
    }

    private static boolean installOnExternalAsec(int installFlags) {
        if ((installFlags & 16) == 0 && (installFlags & 8) != 0) {
            return true;
        }
        return false;
    }

    private static boolean installForwardLocked(int installFlags) {
        return (installFlags & 1) != 0;
    }

    private InstallArgs createInstallArgs(InstallParams params) {
        if (params.move != null) {
            return new MoveInstallArgs(params);
        }
        if (installOnExternalAsec(params.installFlags) || params.isForwardLocked()) {
            return new AsecInstallArgs(params);
        }
        return new FileInstallArgs(params);
    }

    private InstallArgs createInstallArgsForExisting(int installFlags, String codePath, String resourcePath, String[] instructionSets) {
        boolean isInAsec;
        if (installOnExternalAsec(installFlags)) {
            isInAsec = true;
        } else if (!installForwardLocked(installFlags) || codePath.startsWith(this.mDrmAppPrivateInstallDir.getAbsolutePath())) {
            isInAsec = false;
        } else {
            isInAsec = true;
        }
        if (!isInAsec) {
            return new FileInstallArgs(codePath, resourcePath, instructionSets);
        }
        return new AsecInstallArgs(codePath, instructionSets, installOnExternalAsec(installFlags), installForwardLocked(installFlags));
    }

    private void removeDexFiles(List<String> allCodePaths, String[] instructionSets) {
        if (!allCodePaths.isEmpty()) {
            if (instructionSets == null) {
                throw new IllegalStateException("instructionSet == null");
            }
            String[] dexCodeInstructionSets = InstructionSets.getDexCodeInstructionSets(instructionSets);
            for (String codePath : allCodePaths) {
                for (String dexCodeInstructionSet : dexCodeInstructionSets) {
                    try {
                        this.mInstaller.rmdex(codePath, dexCodeInstructionSet);
                    } catch (InstallerException e) {
                    }
                }
            }
        }
    }

    private boolean isAsecExternal(String cid) {
        boolean z = false;
        String asecPath = PackageHelper.getSdFilesystem(cid);
        if (asecPath == null) {
            return false;
        }
        if (!asecPath.startsWith(this.mAsecInternalPath)) {
            z = true;
        }
        return z;
    }

    private static void maybeThrowExceptionForMultiArchCopy(String message, int copyRet) throws PackageManagerException {
        if (copyRet < 0 && copyRet != -114 && copyRet != -113) {
            throw new PackageManagerException(copyRet, message);
        }
    }

    static String cidFromCodePath(String fullCodePath) {
        int eidx = fullCodePath.lastIndexOf("/");
        String subStr1 = fullCodePath.substring(0, eidx);
        return subStr1.substring(subStr1.lastIndexOf("/") + 1, eidx);
    }

    static String getAsecPackageName(String packageCid) {
        int idx = packageCid.lastIndexOf(INSTALL_PACKAGE_SUFFIX);
        if (idx == -1) {
            return packageCid;
        }
        return packageCid.substring(0, idx);
    }

    private static String getNextCodePath(String oldCodePath, String prefix, String suffix) {
        String idxStr = "";
        int idx = 1;
        if (oldCodePath != null) {
            String subStr = oldCodePath;
            if (suffix != null && oldCodePath.endsWith(suffix)) {
                subStr = oldCodePath.substring(0, oldCodePath.length() - suffix.length());
            }
            int sidx = subStr.lastIndexOf(prefix);
            if (sidx != -1) {
                subStr = subStr.substring(prefix.length() + sidx);
                if (subStr != null) {
                    if (subStr.startsWith(INSTALL_PACKAGE_SUFFIX)) {
                        subStr = subStr.substring(INSTALL_PACKAGE_SUFFIX.length());
                    }
                    try {
                        idx = Integer.parseInt(subStr);
                        idx = idx <= 1 ? idx + 1 : idx - 1;
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
        return prefix + (INSTALL_PACKAGE_SUFFIX + Integer.toString(idx));
    }

    private File getNextCodePath(File targetDir, String packageName) {
        File result;
        int suffix = 1;
        do {
            result = new File(targetDir, packageName + INSTALL_PACKAGE_SUFFIX + suffix);
            suffix++;
        } while (result.exists());
        return result;
    }

    static String deriveCodePathName(String codePath) {
        if (codePath == null) {
            return null;
        }
        File codeFile = new File(codePath);
        String name = codeFile.getName();
        if (codeFile.isDirectory()) {
            return name;
        }
        if (name.endsWith(".apk") || name.endsWith(".tmp")) {
            return name.substring(0, name.lastIndexOf(46));
        }
        Slog.w(TAG, "Odd, " + codePath + " doesn't look like an APK");
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void installNewPackageLIF(Package pkg, int policyFlags, int scanFlags, UserHandle user, String installerPackageName, String volumeUuid, PackageInstalledInfo res) {
        Trace.traceBegin(262144, "installNewPackage");
        String pkgName = pkg.packageName;
        synchronized (this.mPackages) {
            if (this.mSettings.mRenamedPackages.containsKey(pkgName)) {
                res.setError(-1, "Attempt to re-install " + pkgName + " without first uninstalling package running as " + ((String) this.mSettings.mRenamedPackages.get(pkgName)));
                return;
            } else if (this.mPackages.containsKey(pkgName)) {
                res.setError(-1, "Attempt to re-install " + pkgName + " without first uninstalling.");
                return;
            }
        }
        Trace.traceEnd(262144);
    }

    private boolean shouldCheckUpgradeKeySetLP(PackageSetting oldPs, int scanFlags) {
        if (oldPs == null || (scanFlags & 16384) != 0 || oldPs.sharedUser != null || !oldPs.keySetData.isUsingUpgradeKeySets()) {
            return false;
        }
        KeySetManagerService ksms = this.mSettings.mKeySetManagerService;
        long[] upgradeKeySets = oldPs.keySetData.getUpgradeKeySets();
        int i = 0;
        while (i < upgradeKeySets.length) {
            if (ksms.isIdValidKeySetId(upgradeKeySets[i])) {
                i++;
            } else {
                Slog.wtf(TAG, "Package " + (oldPs.name != null ? oldPs.name : "<null>") + " contains upgrade-key-set reference to unknown key-set: " + upgradeKeySets[i] + " reverting to signatures check.");
                return false;
            }
        }
        return true;
    }

    private boolean checkUpgradeKeySetLP(PackageSetting oldPS, Package newPkg) {
        long[] upgradeKeySets = oldPS.keySetData.getUpgradeKeySets();
        KeySetManagerService ksms = this.mSettings.mKeySetManagerService;
        for (long publicKeysFromKeySetLPr : upgradeKeySets) {
            Set<PublicKey> upgradeSet = ksms.getPublicKeysFromKeySetLPr(publicKeysFromKeySetLPr);
            if (upgradeSet != null && newPkg.mSigningKeys.containsAll(upgradeSet)) {
                return true;
            }
        }
        return false;
    }

    private static void updateDigest(MessageDigest digest, File file) throws IOException {
        Throwable th;
        Throwable th2 = null;
        DigestInputStream digestInputStream = null;
        try {
            DigestInputStream digestStream = new DigestInputStream(new FileInputStream(file), digest);
            do {
                try {
                } catch (Throwable th3) {
                    th = th3;
                    digestInputStream = digestStream;
                }
            } while (digestStream.read() != -1);
            if (digestStream != null) {
                try {
                    digestStream.close();
                } catch (Throwable th4) {
                    th2 = th4;
                }
            }
            if (th2 != null) {
                throw th2;
            }
        } catch (Throwable th5) {
            th = th5;
            if (digestInputStream != null) {
                try {
                    digestInputStream.close();
                } catch (Throwable th6) {
                    if (th2 == null) {
                        th2 = th6;
                    } else if (th2 != th6) {
                        th2.addSuppressed(th6);
                    }
                }
            }
            if (th2 != null) {
                throw th2;
            }
            throw th;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void replacePackageLIF(Package pkg, int policyFlags, int scanFlags, UserHandle user, String installerPackageName, PackageInstalledInfo res) {
        boolean isEphemeral = (policyFlags & 2048) != 0;
        String pkgName = pkg.packageName;
        synchronized (this.mPackages) {
            Package oldPackage = (Package) this.mPackages.get(pkgName);
            boolean oldTargetsPreRelease = oldPackage.applicationInfo.targetSdkVersion == 10000;
            boolean newTargetsPreRelease = pkg.applicationInfo.targetSdkVersion == 10000;
            if (oldTargetsPreRelease && !newTargetsPreRelease && (policyFlags & 4096) == 0) {
                Slog.w(TAG, "Can't install package targeting released sdk");
                res.setReturnCode(-7);
                return;
            }
            boolean oldIsEphemeral = oldPackage.applicationInfo.isEphemeralApp();
            if (!isEphemeral || oldIsEphemeral) {
                PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(pkgName);
                if (shouldCheckUpgradeKeySetLP(ps, scanFlags)) {
                    if (!checkUpgradeKeySetLP(ps, pkg)) {
                        res.setError(-7, "New package not signed by keys specified by upgrade-keysets: " + pkgName);
                        return;
                    }
                } else if (compareSignatures(oldPackage.mSignatures, pkg.mSignatures) != 0) {
                    if (isSystemSignatureUpdated(oldPackage.mSignatures, pkg.mSignatures)) {
                        Slog.d(TAG, pkg.packageName + " system signature update");
                    } else {
                        res.setError(-7, "New package has a different signature: " + pkgName);
                        return;
                    }
                }
                if (oldPackage.restrictUpdateHash != null && oldPackage.isSystemApp()) {
                    try {
                        MessageDigest digest = MessageDigest.getInstance("SHA-512");
                        updateDigest(digest, new File(pkg.baseCodePath));
                        if (!ArrayUtils.isEmpty(pkg.splitCodePaths)) {
                            for (String path : pkg.splitCodePaths) {
                                updateDigest(digest, new File(path));
                            }
                        }
                        if (Arrays.equals(oldPackage.restrictUpdateHash, digest.digest())) {
                            pkg.restrictUpdateHash = oldPackage.restrictUpdateHash;
                        } else {
                            res.setError(-2, "New package fails restrict-update check: " + pkgName);
                            return;
                        }
                    } catch (NoSuchAlgorithmException e) {
                        res.setError(-2, "Could not compute hash: " + pkgName);
                        return;
                    }
                }
                String invalidPackageName = getParentOrChildPackageChangedSharedUser(oldPackage, pkg);
                if (invalidPackageName != null) {
                    res.setError(-8, "Package " + invalidPackageName + " tried to change user " + oldPackage.mSharedUserId);
                    return;
                }
                int[] allUsers = sUserManager.getUserIds();
                int[] installedUsers = ps.queryInstalledUsers(allUsers, true);
            } else {
                Slog.w(TAG, "Can't replace app with ephemeral: " + pkgName);
                res.setReturnCode(-116);
            }
        }
    }

    public List<String> getPreviousCodePaths(String packageName) {
        PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(packageName);
        List<String> result = new ArrayList();
        if (!(ps == null || ps.oldCodePaths == null)) {
            result.addAll(ps.oldCodePaths);
        }
        return result;
    }

    private void replaceNonSystemPackageLIF(Package deletedPackage, Package pkg, int policyFlags, int scanFlags, UserHandle user, int[] allUsers, String installerPackageName, PackageInstalledInfo res) {
        PackageSetting ps;
        int i;
        String pkgName = deletedPackage.packageName;
        boolean deletedPkg = true;
        boolean addedPkg = false;
        boolean killApp = (131072 & scanFlags) == 0;
        int deleteFlags = (killApp ? 0 : 8) | 1;
        long origUpdateTime = pkg.mExtras != null ? ((PackageSetting) pkg.mExtras).lastUpdateTime : 0;
        if (deletePackageLIF(pkgName, null, true, allUsers, deleteFlags, res.removedInfo, true, pkg)) {
            if (deletedPackage.isForwardLocked() || isExternal(deletedPackage)) {
                int[] uidArray = new int[]{deletedPackage.applicationInfo.uid};
                ArrayList pkgList = new ArrayList(1);
                pkgList.add(deletedPackage.applicationInfo.packageName);
                sendResourcesChangedBroadcast(false, true, pkgList, uidArray, null);
            }
            clearAppDataLIF(pkg, -1, 515);
            clearAppProfilesLIF(deletedPackage, -1);
            try {
                Package newPackage = scanPackageTracedLI(pkg, policyFlags, scanFlags | 64, System.currentTimeMillis(), user);
                updateSettingsLI(newPackage, installerPackageName, allUsers, res, user);
                ps = (PackageSetting) this.mSettings.mPackages.get(pkgName);
                if (killApp) {
                    ps.oldCodePaths = null;
                } else {
                    if (ps.oldCodePaths == null) {
                        ps.oldCodePaths = new ArraySet();
                    }
                    Collections.addAll(ps.oldCodePaths, new String[]{deletedPackage.baseCodePath});
                    if (deletedPackage.splitCodePaths != null) {
                        Collections.addAll(ps.oldCodePaths, deletedPackage.splitCodePaths);
                    }
                }
                if (ps.childPackageNames != null) {
                    for (i = ps.childPackageNames.size() - 1; i >= 0; i--) {
                        ((PackageSetting) this.mSettings.mPackages.get((String) ps.childPackageNames.get(i))).oldCodePaths = ps.oldCodePaths;
                    }
                }
                prepareAppDataAfterInstallLIF(newPackage);
                addedPkg = true;
            } catch (PackageManagerException e) {
                res.setError("Package couldn't be installed in " + pkg.codePath, e);
            }
        } else {
            res.setError(-10, "replaceNonSystemPackageLI");
            deletedPkg = false;
        }
        if (res.returnCode != 1) {
            if (addedPkg) {
                deletePackageLIF(pkgName, null, true, allUsers, deleteFlags, res.removedInfo, true, null);
            }
            if (deletedPkg) {
                try {
                    scanPackageTracedLI(new File(deletedPackage.codePath), ((this.mDefParseFlags | 2) | (deletedPackage.isForwardLocked() ? 16 : 0)) | (isExternal(deletedPackage) ? 32 : 0), 72, origUpdateTime, null);
                    synchronized (this.mPackages) {
                        setInstallerPackageNameLPw(deletedPackage, installerPackageName);
                        updatePermissionsLPw(deletedPackage, 1);
                        this.mSettings.writeLPr();
                    }
                    Slog.i(TAG, "Successfully restored package : " + pkgName + " after failed upgrade");
                } catch (PackageManagerException e2) {
                    Slog.e(TAG, "Failed to restore package : " + pkgName + " after failed upgrade: " + e2.getMessage());
                    return;
                }
            }
        }
        synchronized (this.mPackages) {
            ps = this.mSettings.peekPackageLPr(pkg.packageName);
            if (ps != null) {
                res.removedInfo.removedForAllUsers = this.mPackages.get(ps.name) == null;
                if (res.removedInfo.removedChildPackages != null) {
                    for (i = res.removedInfo.removedChildPackages.size() - 1; i >= 0; i--) {
                        if (res.addedChildPackages.containsKey((String) res.removedInfo.removedChildPackages.keyAt(i))) {
                            res.removedInfo.removedChildPackages.removeAt(i);
                        } else {
                            boolean z;
                            PackageRemovedInfo childInfo = (PackageRemovedInfo) res.removedInfo.removedChildPackages.valueAt(i);
                            if (this.mPackages.get(childInfo.removedPackage) == null) {
                                z = true;
                            } else {
                                z = false;
                            }
                            childInfo.removedForAllUsers = z;
                        }
                    }
                }
            }
        }
    }

    private void replaceSystemPackageLIF(Package deletedPackage, Package pkg, int policyFlags, int scanFlags, UserHandle user, int[] allUsers, String installerPackageName, PackageInstalledInfo res) {
        Package newPackage;
        PackageManagerException e;
        String packageName = deletedPackage.packageName;
        PackageSetting deletePackageSetting = (PackageSetting) this.mSettings.mPackages.get(packageName);
        removePackageLI(deletedPackage, true);
        synchronized (this.mPackages) {
            boolean disabledSystem = disableSystemPackageLPw(deletedPackage, pkg);
        }
        if (disabledSystem) {
            res.removedInfo.args = null;
        } else {
            res.removedInfo.args = createInstallArgsForExisting(0, deletedPackage.applicationInfo.getCodePath(), deletedPackage.applicationInfo.getResourcePath(), InstructionSets.getAppDexInstructionSets(deletedPackage.applicationInfo));
        }
        clearAppDataLIF(pkg, -1, 515);
        clearAppProfilesLIF(deletedPackage, -1);
        res.setReturnCode(1);
        pkg.setApplicationInfoFlags(128, 128);
        synchronized (this.mPackages) {
            String disableSysPath = this.mSettings.getDisabledSysPackagesPath(packageName);
            PackageSetting disableSysSetting = this.mSettings.getDisabledSystemPkgLPr(packageName);
            boolean disableSysPathInDel = false;
            boolean disableSysInData = false;
            if (disableSysPath != null && containDelPath(disableSysPath)) {
                disableSysPathInDel = true;
            }
            if (disableSysSetting != null && isDelappInData(disableSysSetting)) {
                disableSysInData = true;
            }
            if (disableSysSetting != null && isDelappInCust(disableSysSetting)) {
                disableSysInData = true;
            }
            if (containDelPath(deletedPackage.applicationInfo.sourceDir) || isDelappInData(deletePackageSetting) || isDelappInCust(deletePackageSetting) || disableSysPathInDel || disableSysInData) {
                ApplicationInfo applicationInfo = pkg.applicationInfo;
                applicationInfo.hwFlags |= 67108864;
            }
        }
        try {
            newPackage = scanPackageTracedLI(pkg, policyFlags, scanFlags, 0, user);
            try {
                setInstallAndUpdateTime(newPackage, deletedPackage.mExtras.firstInstallTime, System.currentTimeMillis());
                if (res.returnCode == 1) {
                    int deletedChildCount = deletedPackage.childPackages != null ? deletedPackage.childPackages.size() : 0;
                    int newChildCount = newPackage.childPackages != null ? newPackage.childPackages.size() : 0;
                    for (int i = 0; i < deletedChildCount; i++) {
                        Package deletedChildPkg = (Package) deletedPackage.childPackages.get(i);
                        boolean childPackageDeleted = true;
                        for (int j = 0; j < newChildCount; j++) {
                            if (deletedChildPkg.packageName.equals(((Package) newPackage.childPackages.get(j)).packageName)) {
                                childPackageDeleted = false;
                                break;
                            }
                        }
                        if (childPackageDeleted) {
                            PackageSetting ps = this.mSettings.getDisabledSystemPkgLPr(deletedChildPkg.packageName);
                            if (!(ps == null || res.removedInfo.removedChildPackages == null)) {
                                boolean z;
                                PackageRemovedInfo removedChildRes = (PackageRemovedInfo) res.removedInfo.removedChildPackages.get(deletedChildPkg.packageName);
                                removePackageDataLIF(ps, allUsers, removedChildRes, 0, false);
                                if (this.mPackages.get(ps.name) == null) {
                                    z = true;
                                } else {
                                    z = false;
                                }
                                removedChildRes.removedForAllUsers = z;
                            }
                        }
                    }
                    updateSettingsLI(newPackage, installerPackageName, allUsers, res, user);
                    prepareAppDataAfterInstallLIF(newPackage);
                }
            } catch (PackageManagerException e2) {
                e = e2;
                res.setReturnCode(-110);
                res.setError("Package couldn't be installed in " + pkg.codePath, e);
                if (res.returnCode == 1) {
                    if (newPackage != null) {
                        removeInstalledPackageLI(newPackage, true);
                    }
                    try {
                        scanPackageTracedLI(deletedPackage, policyFlags, 8, 0, user);
                    } catch (PackageManagerException e3) {
                        Slog.e(TAG, "Failed to restore original package: " + e3.getMessage());
                    }
                    synchronized (this.mPackages) {
                        if (disabledSystem) {
                            enableSystemPackageLPw(deletedPackage);
                        }
                        setInstallerPackageNameLPw(deletedPackage, installerPackageName);
                        updatePermissionsLPw(deletedPackage, 1);
                        this.mSettings.writeLPr();
                    }
                    Slog.i(TAG, "Successfully restored package : " + deletedPackage.packageName + " after failed upgrade");
                }
            }
        } catch (PackageManagerException e4) {
            e3 = e4;
            newPackage = null;
            res.setReturnCode(-110);
            res.setError("Package couldn't be installed in " + pkg.codePath, e3);
            if (res.returnCode == 1) {
                if (newPackage != null) {
                    removeInstalledPackageLI(newPackage, true);
                }
                scanPackageTracedLI(deletedPackage, policyFlags, 8, 0, user);
                synchronized (this.mPackages) {
                    if (disabledSystem) {
                        enableSystemPackageLPw(deletedPackage);
                    }
                    setInstallerPackageNameLPw(deletedPackage, installerPackageName);
                    updatePermissionsLPw(deletedPackage, 1);
                    this.mSettings.writeLPr();
                }
                Slog.i(TAG, "Successfully restored package : " + deletedPackage.packageName + " after failed upgrade");
            }
        }
        if (res.returnCode == 1) {
            if (newPackage != null) {
                removeInstalledPackageLI(newPackage, true);
            }
            scanPackageTracedLI(deletedPackage, policyFlags, 8, 0, user);
            synchronized (this.mPackages) {
                if (disabledSystem) {
                    enableSystemPackageLPw(deletedPackage);
                }
                setInstallerPackageNameLPw(deletedPackage, installerPackageName);
                updatePermissionsLPw(deletedPackage, 1);
                this.mSettings.writeLPr();
            }
            Slog.i(TAG, "Successfully restored package : " + deletedPackage.packageName + " after failed upgrade");
        }
    }

    private String getParentOrChildPackageChangedSharedUser(Package oldPkg, Package newPkg) {
        if (!Objects.equals(oldPkg.mSharedUserId, newPkg.mSharedUserId)) {
            return newPkg.packageName;
        }
        int oldChildCount = oldPkg.childPackages != null ? oldPkg.childPackages.size() : 0;
        int newChildCount = newPkg.childPackages != null ? newPkg.childPackages.size() : 0;
        for (int i = 0; i < newChildCount; i++) {
            Package newChildPkg = (Package) newPkg.childPackages.get(i);
            for (int j = 0; j < oldChildCount; j++) {
                Package oldChildPkg = (Package) oldPkg.childPackages.get(j);
                if (newChildPkg.packageName.equals(oldChildPkg.packageName) && !Objects.equals(newChildPkg.mSharedUserId, oldChildPkg.mSharedUserId)) {
                    return newChildPkg.packageName;
                }
            }
        }
        return null;
    }

    private void removeNativeBinariesLI(PackageSetting ps) {
        if (ps != null) {
            NativeLibraryHelper.removeNativeBinariesLI(ps.legacyNativeLibraryPathString);
            int childCount = ps.childPackageNames != null ? ps.childPackageNames.size() : 0;
            for (int i = 0; i < childCount; i++) {
                PackageSetting childPs;
                synchronized (this.mPackages) {
                    childPs = this.mSettings.peekPackageLPr((String) ps.childPackageNames.get(i));
                }
                if (childPs != null) {
                    NativeLibraryHelper.removeNativeBinariesLI(childPs.legacyNativeLibraryPathString);
                }
            }
        }
    }

    private void enableSystemPackageLPw(Package pkg) {
        this.mSettings.enableSystemPackageLPw(pkg.packageName);
        int childCount = pkg.childPackages != null ? pkg.childPackages.size() : 0;
        for (int i = 0; i < childCount; i++) {
            this.mSettings.enableSystemPackageLPw(((Package) pkg.childPackages.get(i)).packageName);
        }
    }

    private boolean disableSystemPackageLPw(Package oldPkg, Package newPkg) {
        boolean disabled = this.mSettings.disableSystemPackageLPw(oldPkg.packageName, true);
        for (int i = 0; i < (oldPkg.childPackages != null ? oldPkg.childPackages.size() : 0); i++) {
            Package childPkg = (Package) oldPkg.childPackages.get(i);
            disabled |= this.mSettings.disableSystemPackageLPw(childPkg.packageName, newPkg.hasChildPackage(childPkg.packageName));
        }
        return disabled;
    }

    private void setInstallerPackageNameLPw(Package pkg, String installerPackageName) {
        this.mSettings.setInstallerPackageName(pkg.packageName, installerPackageName);
        int childCount = pkg.childPackages != null ? pkg.childPackages.size() : 0;
        for (int i = 0; i < childCount; i++) {
            this.mSettings.setInstallerPackageName(((Package) pkg.childPackages.get(i)).packageName, installerPackageName);
        }
    }

    private int[] revokeUnusedSharedUserPermissionsLPw(SharedUserSetting su, int[] allUserIds) {
        int i;
        ArraySet<String> usedPermissions = new ArraySet();
        int packageCount = su.packages.size();
        for (i = 0; i < packageCount; i++) {
            PackageSetting ps = (PackageSetting) su.packages.valueAt(i);
            if (ps.pkg != null) {
                int requestedPermCount = ps.pkg.requestedPermissions.size();
                for (int j = 0; j < requestedPermCount; j++) {
                    String permission = (String) ps.pkg.requestedPermissions.get(j);
                    if (((BasePermission) this.mSettings.mPermissions.get(permission)) != null) {
                        usedPermissions.add(permission);
                    }
                }
            }
        }
        PermissionsState permissionsState = su.getPermissionsState();
        List<PermissionState> installPermStates = permissionsState.getInstallPermissionStates();
        for (i = installPermStates.size() - 1; i >= 0; i--) {
            BasePermission bp;
            PermissionState permissionState = (PermissionState) installPermStates.get(i);
            if (!usedPermissions.contains(permissionState.getName())) {
                bp = (BasePermission) this.mSettings.mPermissions.get(permissionState.getName());
                if (bp != null) {
                    permissionsState.revokeInstallPermission(bp);
                    permissionsState.updatePermissionFlags(bp, -1, 255, 0);
                }
            }
        }
        int[] runtimePermissionChangedUserIds = EmptyArray.INT;
        for (int userId : allUserIds) {
            List<PermissionState> runtimePermStates = permissionsState.getRuntimePermissionStates(userId);
            for (i = runtimePermStates.size() - 1; i >= 0; i--) {
                permissionState = (PermissionState) runtimePermStates.get(i);
                if (!usedPermissions.contains(permissionState.getName())) {
                    bp = (BasePermission) this.mSettings.mPermissions.get(permissionState.getName());
                    if (bp != null) {
                        permissionsState.revokeRuntimePermission(bp, userId);
                        permissionsState.updatePermissionFlags(bp, userId, 255, 0);
                        runtimePermissionChangedUserIds = ArrayUtils.appendInt(runtimePermissionChangedUserIds, userId);
                    }
                }
            }
        }
        return runtimePermissionChangedUserIds;
    }

    private void updateSettingsLI(Package newPackage, String installerPackageName, int[] allUsers, PackageInstalledInfo res, UserHandle user) {
        updateSettingsInternalLI(newPackage, installerPackageName, allUsers, res.origUsers, res, user);
        int childCount = newPackage.childPackages != null ? newPackage.childPackages.size() : 0;
        for (int i = 0; i < childCount; i++) {
            Package childPackage = (Package) newPackage.childPackages.get(i);
            PackageInstalledInfo childRes = (PackageInstalledInfo) res.addedChildPackages.get(childPackage.packageName);
            updateSettingsInternalLI(childPackage, installerPackageName, allUsers, childRes.origUsers, childRes, user);
        }
    }

    private void updateSettingsInternalLI(Package newPackage, String installerPackageName, int[] allUsers, int[] installedForUsers, PackageInstalledInfo res, UserHandle user) {
        Trace.traceBegin(262144, "updateSettings");
        String pkgName = newPackage.packageName;
        synchronized (this.mPackages) {
            this.mSettings.setInstallStatus(pkgName, 0);
            Trace.traceBegin(262144, "writeSettings");
            this.mSettings.writeLPr();
            Trace.traceEnd(262144);
        }
        synchronized (this.mPackages) {
            updatePermissionsLPw(newPackage.packageName, newPackage, (newPackage.permissions.size() > 0 ? 1 : 0) | 2);
            PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(pkgName);
            int userId = user.getIdentifier();
            if (ps != null) {
                if (isSystemApp(newPackage)) {
                    if (res.origUsers != null) {
                        for (int origUserId : res.origUsers) {
                            if (userId == -1 || userId == origUserId) {
                                ps.setEnabled(0, origUserId, installerPackageName);
                            }
                        }
                    }
                    if (!(allUsers == null || installedForUsers == null)) {
                        for (int currentUserId : allUsers) {
                            ps.setInstalled(ArrayUtils.contains(installedForUsers, currentUserId), currentUserId);
                        }
                    }
                }
                if (userId != -1) {
                    ps.setInstalled(true, userId);
                    ps.setEnabled(0, userId, installerPackageName);
                }
            }
            res.name = pkgName;
            res.uid = newPackage.applicationInfo.uid;
            res.pkg = newPackage;
            this.mSettings.setInstallStatus(pkgName, 1);
            this.mSettings.setInstallerPackageName(pkgName, installerPackageName);
            res.setReturnCode(1);
            Trace.traceBegin(262144, "writeSettings");
            this.mSettings.writeLPr();
            Trace.traceEnd(262144);
        }
        Trace.traceEnd(262144);
    }

    private void installPackageTracedLI(InstallArgs args, PackageInstalledInfo res) {
        try {
            Trace.traceBegin(262144, "installPackage");
            installPackageLI(args, res);
        } finally {
            Trace.traceEnd(262144);
        }
    }

    private void startIntentFilterVerifications(int userId, boolean replacing, Package pkg) {
        if (this.mIntentFilterVerifierComponent == null) {
            Slog.w(TAG, "No IntentFilter verification will not be done as there is no IntentFilterVerifier available!");
            return;
        }
        int i;
        String packageName = this.mIntentFilterVerifierComponent.getPackageName();
        if (userId == -1) {
            i = 0;
        } else {
            i = userId;
        }
        int verifierUid = getPackageUid(packageName, 268435456, i);
        Message msg = this.mHandler.obtainMessage(17);
        msg.obj = new IFVerificationParams(pkg, replacing, userId, verifierUid);
        this.mHandler.sendMessage(msg);
        int childCount = pkg.childPackages != null ? pkg.childPackages.size() : 0;
        for (int i2 = 0; i2 < childCount; i2++) {
            Package childPkg = (Package) pkg.childPackages.get(i2);
            msg = this.mHandler.obtainMessage(17);
            msg.obj = new IFVerificationParams(childPkg, replacing, userId, verifierUid);
            this.mHandler.sendMessage(msg);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void verifyIntentFiltersIfNeeded(int userId, int verifierUid, boolean replacing, Package pkg) {
        if (pkg.activities.size() != 0 && hasDomainURLs(pkg)) {
            int count = 0;
            String packageName = pkg.packageName;
            synchronized (this.mPackages) {
                if (!replacing) {
                    if (this.mSettings.getIntentFilterVerificationLPr(packageName) != null) {
                        return;
                    }
                }
                boolean needToVerify = false;
                for (Activity a : pkg.activities) {
                    for (ActivityIntentInfo filter : a.intents) {
                        if (filter.needsVerification() && needsNetworkVerificationLPr(filter)) {
                            needToVerify = true;
                            break;
                        }
                    }
                }
                if (needToVerify) {
                    int verificationId = this.mIntentFilterVerificationToken;
                    this.mIntentFilterVerificationToken = verificationId + 1;
                    for (Activity a2 : pkg.activities) {
                        for (ActivityIntentInfo filter2 : a2.intents) {
                            if (filter2.handlesWebUris(true) && needsNetworkVerificationLPr(filter2)) {
                                this.mIntentFilterVerifier.addOneIntentFilterVerification(verifierUid, userId, verificationId, filter2, packageName);
                                count++;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean needsNetworkVerificationLPr(ActivityIntentInfo filter) {
        IntentFilterVerificationInfo ivi = this.mSettings.getIntentFilterVerificationLPr(filter.activity.getComponentName().getPackageName());
        if (ivi == null) {
            return true;
        }
        switch (ivi.getStatus()) {
            case 0:
            case 1:
                return true;
            default:
                return false;
        }
    }

    private static boolean isMultiArch(ApplicationInfo info) {
        return (info.flags & Integer.MIN_VALUE) != 0;
    }

    private static boolean isExternal(Package pkg) {
        return (pkg.applicationInfo.flags & DumpState.DUMP_DOMAIN_PREFERRED) != 0;
    }

    private static boolean isExternal(PackageSetting ps) {
        return (ps.pkgFlags & DumpState.DUMP_DOMAIN_PREFERRED) != 0;
    }

    private static boolean isEphemeral(Package pkg) {
        return pkg.applicationInfo.isEphemeralApp();
    }

    private static boolean isEphemeral(PackageSetting ps) {
        return ps.pkg != null ? isEphemeral(ps.pkg) : false;
    }

    private static boolean isSystemApp(Package pkg) {
        return (pkg.applicationInfo.flags & 1) != 0;
    }

    private static boolean isPrivilegedApp(Package pkg) {
        return (pkg.applicationInfo.privateFlags & 8) != 0;
    }

    private static boolean hasDomainURLs(Package pkg) {
        return (pkg.applicationInfo.privateFlags & 16) != 0;
    }

    private static boolean isSystemApp(PackageSetting ps) {
        return (ps.pkgFlags & 1) != 0;
    }

    private static boolean isUpdatedSystemApp(PackageSetting ps) {
        return (ps.pkgFlags & 128) != 0;
    }

    private int packageFlagsToInstallFlags(PackageSetting ps) {
        int installFlags = 0;
        if (isEphemeral(ps)) {
            installFlags = 2048;
        }
        if (isExternal(ps) && TextUtils.isEmpty(ps.volumeUuid)) {
            installFlags |= 8;
        }
        if (ps.isForwardLocked()) {
            return installFlags | 1;
        }
        return installFlags;
    }

    private String getVolumeUuidForPackage(Package pkg) {
        if (!isExternal(pkg)) {
            return StorageManager.UUID_PRIVATE_INTERNAL;
        }
        if (TextUtils.isEmpty(pkg.volumeUuid)) {
            return "primary_physical";
        }
        return pkg.volumeUuid;
    }

    private VersionInfo getSettingsVersionForPackage(Package pkg) {
        if (!isExternal(pkg)) {
            return this.mSettings.getInternalVersion();
        }
        if (TextUtils.isEmpty(pkg.volumeUuid)) {
            return this.mSettings.getExternalVersion();
        }
        return this.mSettings.findOrCreateVersion(pkg.volumeUuid);
    }

    private void deleteTempPackageFiles() {
        for (File file : this.mDrmAppPrivateInstallDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("vmdl") ? name.endsWith(".tmp") : false;
            }
        })) {
            file.delete();
        }
    }

    public void deletePackageAsUser(String packageName, IPackageDeleteObserver observer, int userId, int flags) {
        deletePackage(packageName, new LegacyPackageDeleteObserver(observer).getBinder(), userId, flags);
    }

    public void deletePackage(String packageName, IPackageDeleteObserver2 observer, int userId, int deleteFlags) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DELETE_PACKAGES", null);
        Preconditions.checkNotNull(packageName);
        Preconditions.checkNotNull(observer);
        int uid = Binder.getCallingUid();
        final boolean deleteAllUsers = (deleteFlags & 2) != 0;
        final int[] users = deleteAllUsers ? sUserManager.getUserIds() : new int[]{userId};
        if (UserHandle.getUserId(uid) != userId || (deleteAllUsers && users.length > 1)) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "deletePackage for user " + userId);
        }
        if (isUserRestricted(userId, "no_uninstall_apps")) {
            try {
                observer.onPackageDeleted(packageName, -3, null);
            } catch (RemoteException e) {
            }
        } else if (deleteAllUsers || !getBlockUninstallForUser(packageName, userId)) {
            final String str = packageName;
            final int i = userId;
            final int i2 = deleteFlags;
            final IPackageDeleteObserver2 iPackageDeleteObserver2 = observer;
            this.mHandler.post(new Runnable() {
                public void run() {
                    int returnCode;
                    PackageManagerService.this.mHandler.removeCallbacks(this);
                    if (deleteAllUsers) {
                        int[] blockUninstallUserIds = PackageManagerService.this.getBlockUninstallForUsers(str, users);
                        if (ArrayUtils.isEmpty(blockUninstallUserIds)) {
                            returnCode = PackageManagerService.this.deletePackageX(str, i, i2);
                        } else {
                            int userFlags = i2 & -3;
                            for (int userId : users) {
                                if (!ArrayUtils.contains(blockUninstallUserIds, userId)) {
                                    returnCode = PackageManagerService.this.deletePackageX(str, userId, userFlags);
                                    if (returnCode != 1) {
                                        Slog.w(PackageManagerService.TAG, "Package delete failed for user " + userId + ", returnCode " + returnCode);
                                    }
                                }
                            }
                            returnCode = -4;
                        }
                    } else {
                        returnCode = PackageManagerService.this.deletePackageX(str, i, i2);
                    }
                    try {
                        iPackageDeleteObserver2.onPackageDeleted(str, returnCode, null);
                    } catch (RemoteException e) {
                        Log.i(PackageManagerService.TAG, "Observer no longer exists.");
                    }
                }
            });
            setNeedClearDeviceForCTS(false, packageName);
            Log.d(TAG, "setmNeedClearDeviceForCTS:false ");
        } else {
            try {
                observer.onPackageDeleted(packageName, -4, null);
            } catch (RemoteException e2) {
            }
        }
    }

    private int[] getBlockUninstallForUsers(String packageName, int[] userIds) {
        int[] result = EMPTY_INT_ARRAY;
        for (int userId : userIds) {
            if (getBlockUninstallForUser(packageName, userId)) {
                result = ArrayUtils.appendInt(result, userId);
            }
        }
        return result;
    }

    public boolean isPackageDeviceAdminOnAnyUser(String packageName) {
        return isPackageDeviceAdmin(packageName, -1);
    }

    private boolean isPackageDeviceAdmin(String packageName, int userId) {
        IDevicePolicyManager dpm = IDevicePolicyManager.Stub.asInterface(ServiceManager.getService("device_policy"));
        if (dpm != null) {
            try {
                Object obj;
                ComponentName deviceOwnerComponentName = dpm.getDeviceOwnerComponent(false);
                if (deviceOwnerComponentName == null) {
                    obj = null;
                } else {
                    obj = deviceOwnerComponentName.getPackageName();
                }
                if (packageName.equals(obj)) {
                    return true;
                }
                int[] users = userId == -1 ? sUserManager.getUserIds() : new int[]{userId};
                for (int packageHasActiveAdmins : users) {
                    if (dpm.packageHasActiveAdmins(packageName, packageHasActiveAdmins)) {
                        return true;
                    }
                }
            } catch (RemoteException e) {
            }
        }
        return false;
    }

    private boolean shouldKeepUninstalledPackageLPr(String packageName) {
        return this.mKeepUninstalledPackages != null ? this.mKeepUninstalledPackages.contains(packageName) : false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int deletePackageX(String packageName, int userId, int deleteFlags) {
        int removeUser;
        PackageSetting uninstalledPs;
        Throwable th;
        final PackageRemovedInfo info = new PackageRemovedInfo();
        if ((deleteFlags & 2) != 0) {
            removeUser = -1;
        } else {
            removeUser = userId;
        }
        if (isPackageDeviceAdmin(packageName, removeUser)) {
            Slog.w(TAG, "Not removing package " + packageName + ": has active device admin");
            return -2;
        } else if (HwDeviceManager.disallowOp(5, packageName)) {
            return -4;
        } else {
            synchronized (this.mPackages) {
                uninstalledPs = (PackageSetting) this.mSettings.mPackages.get(packageName);
                if (uninstalledPs == null) {
                    Slog.w(TAG, "Not removing non-existent package " + packageName);
                    return -1;
                }
                int[] allUsers = sUserManager.getUserIds();
                info.origUsers = uninstalledPs.queryInstalledUsers(allUsers, true);
            }
        }
        int i;
        sendIncompatibleNotificationIfNeeded(packageName);
        info.sendPackageRemovedBroadcasts((deleteFlags & 8) != 0);
        info.sendSystemPackageUpdatedBroadcasts();
        info.sendSystemPackageAppearedBroadcasts();
        Runtime.getRuntime().gc();
        if (info.args != null) {
            if (this.mCustPms == null && this.mCustPms.isSdInstallEnabled()) {
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        synchronized (PackageManagerService.this.mInstallLock) {
                            info.args.doPostDeleteLI(true);
                        }
                    }
                }, 500);
            } else {
                synchronized (this.mInstallLock) {
                    info.args.doPostDeleteLI(true);
                }
            }
        }
        if (res) {
            i = -1;
        } else {
            i = 1;
        }
        return i;
        if (r15 != null) {
            try {
                r15.close();
            } catch (Throwable th2) {
                if (th == null) {
                    th = th2;
                } else if (th != th2) {
                    th.addSuppressed(th2);
                }
            }
        }
        if (th != null) {
            throw th;
        }
        throw r4;
        if (r22 != null) {
            throw r22;
        } else {
            synchronized (this.mPackages) {
                if (res) {
                    this.mEphemeralApplicationRegistry.onPackageUninstalledLPw(uninstalledPs.pkg);
                }
            }
            if (res) {
                boolean removedForAllUsers = false;
                boolean systemUpdate = info.isRemovedPackageSystemUpdate;
                synchronized (this.mPackages) {
                    if (!systemUpdate) {
                        if (this.mPackages.get(packageName) == null) {
                            removedForAllUsers = true;
                        }
                    }
                }
                if (removedForAllUsers || systemUpdate) {
                    try {
                        updatePackageBlackListInfo(packageName);
                    } catch (Exception e) {
                        Slog.e(TAG, "update BlackListApp info failed");
                    }
                }
                sendIncompatibleNotificationIfNeeded(packageName);
                if ((deleteFlags & 8) != 0) {
                }
                info.sendPackageRemovedBroadcasts((deleteFlags & 8) != 0);
                info.sendSystemPackageUpdatedBroadcasts();
                info.sendSystemPackageAppearedBroadcasts();
            }
            Runtime.getRuntime().gc();
            if (info.args != null) {
                if (this.mCustPms == null) {
                }
                synchronized (this.mInstallLock) {
                    info.args.doPostDeleteLI(true);
                }
            }
            if (res) {
                i = -1;
            } else {
                i = 1;
            }
            return i;
        }
    }

    private void removePackageDataLIF(PackageSetting ps, int[] allUserHandles, PackageRemovedInfo outInfo, int flags, boolean writeSettings) {
        String packageName = ps.name;
        synchronized (this.mPackages) {
            Package deletedPkg = (Package) this.mPackages.get(packageName);
            final PackageSetting deletedPs = (PackageSetting) this.mSettings.mPackages.get(packageName);
            if (outInfo != null) {
                int[] queryInstalledUsers;
                outInfo.removedPackage = packageName;
                if (deletedPs != null) {
                    queryInstalledUsers = deletedPs.queryInstalledUsers(sUserManager.getUserIds(), true);
                } else {
                    queryInstalledUsers = null;
                }
                outInfo.removedUsers = queryInstalledUsers;
            }
        }
        removePackageLI(ps, (65536 & flags) != 0);
        if ((flags & 1) == 0) {
            Package resolvedPkg;
            if (deletedPkg != null) {
                resolvedPkg = deletedPkg;
            } else {
                resolvedPkg = new Package(ps.name);
                resolvedPkg.setVolumeUuid(ps.volumeUuid);
            }
            destroyAppDataLIF(resolvedPkg, -1, 3);
            destroyAppProfilesLIF(resolvedPkg, -1);
            if (outInfo != null) {
                outInfo.dataRemoved = true;
            }
            schedulePackageCleaning(packageName, -1, true);
        } else {
            Flog.i(206, "removePackageDataLI : " + ps.name + ", keep data");
        }
        synchronized (this.mPackages) {
            if (deletedPs != null) {
                if ((flags & 1) == 0) {
                    clearIntentFilterVerificationsLPw(deletedPs.name, -1);
                    clearDefaultBrowserIfNeeded(packageName);
                    if (outInfo != null) {
                        this.mSettings.mKeySetManagerService.removeAppKeySetDataLPw(packageName);
                        outInfo.removedAppId = this.mSettings.removePackageLPw(packageName);
                    }
                    updatePermissionsLPw(deletedPs.name, null, 0);
                    if (deletedPs.sharedUser != null) {
                        for (int userId : UserManagerService.getInstance().getUserIds()) {
                            int userIdToKill = this.mSettings.updateSharedUserPermsLPw(deletedPs, userId);
                            if (userIdToKill == -1 || userIdToKill >= 0) {
                                this.mHandler.post(new Runnable() {
                                    public void run() {
                                        PackageManagerService.this.killApplication(deletedPs.name, deletedPs.appId, PackageManagerService.KILL_APP_REASON_GIDS_CHANGED);
                                    }
                                });
                                break;
                            }
                        }
                    }
                    clearPackagePreferredActivitiesLPw(deletedPs.name, -1);
                }
                if (!(allUserHandles == null || outInfo == null || outInfo.origUsers == null)) {
                    for (int userId2 : allUserHandles) {
                        ps.setInstalled(ArrayUtils.contains(outInfo.origUsers, userId2), userId2);
                    }
                }
            }
            if (writeSettings) {
                this.mSettings.writeLPr();
                writePackagesAbi();
            }
        }
        if (outInfo != null) {
            removeKeystoreDataIfNeeded(-1, outInfo.removedAppId);
        }
    }

    static boolean locationIsPrivileged(File path) {
        boolean z = true;
        try {
            if (!(path.getCanonicalPath().startsWith(new File(Environment.getRootDirectory(), "priv-app").getCanonicalPath()) || HwServiceFactory.isPrivAppNonSystemPartitionDir(path))) {
                z = HwServiceFactory.isPrivAppInCust(path);
            }
            return z;
        } catch (IOException e) {
            Slog.e(TAG, "Unable to access code path " + path);
            return false;
        }
    }

    private boolean deleteSystemPackageLIF(Package deletedPkg, PackageSetting deletedPs, int[] allUserHandles, int flags, PackageRemovedInfo outInfo, boolean writeSettings) {
        if (deletedPs.parentPackageName != null) {
            Slog.w(TAG, "Attempt to delete child system package " + deletedPkg.packageName);
            return false;
        }
        boolean applyUserRestrictions = (allUserHandles == null || outInfo.origUsers == null) ? false : true;
        synchronized (this.mPackages) {
            PackageSetting disabledPs = this.mSettings.getDisabledSystemPkgLPr(deletedPs.name);
        }
        if (disabledPs == null || disabledPs.pkg == null) {
            Slog.w(TAG, "Attempt to delete unknown system package " + deletedPkg.packageName);
            return false;
        }
        synchronized (this.mPackages) {
            if (isDelapp(disabledPs) || isDelappInData(disabledPs) || isDelappInCust(disabledPs)) {
                this.mSettings.removeDisabledSystemPackageLPw(deletedPs.name);
            }
        }
        outInfo.isRemovedPackageSystemUpdate = true;
        if (outInfo.removedChildPackages != null) {
            int childCount = deletedPs.childPackageNames != null ? deletedPs.childPackageNames.size() : 0;
            for (int i = 0; i < childCount; i++) {
                String childPackageName = (String) deletedPs.childPackageNames.get(i);
                if (disabledPs.childPackageNames != null && disabledPs.childPackageNames.contains(childPackageName)) {
                    PackageRemovedInfo childInfo = (PackageRemovedInfo) outInfo.removedChildPackages.get(childPackageName);
                    if (childInfo != null) {
                        childInfo.isRemovedPackageSystemUpdate = true;
                    }
                }
            }
        }
        if (disabledPs.versionCode < deletedPs.versionCode) {
            flags &= -2;
        } else if ((isDelapp(disabledPs) || isDelappInData(disabledPs) || isDelappInCust(disabledPs)) && disabledPs.versionCode == deletedPs.versionCode) {
            flags &= -2;
        } else {
            flags |= 1;
        }
        if (!deleteInstalledPackageLIF(deletedPs, true, flags, allUserHandles, outInfo, writeSettings, disabledPs.pkg)) {
            return false;
        }
        synchronized (this.mPackages) {
            enableSystemPackageLPw(disabledPs.pkg);
            removeNativeBinariesLI(deletedPs);
        }
        int parseFlags = ((this.mDefParseFlags | 4) | 1) | 64;
        if (locationIsPrivileged(disabledPs.codePath)) {
            parseFlags |= 128;
        }
        Package newPkg = null;
        boolean unInstallDel = false;
        PackageRemovedInfo packageRemovedInfo = new PackageRemovedInfo();
        try {
            if (isDelapp(disabledPs) || isDelappInData(disabledPs) || isDelappInCust(disabledPs)) {
                recordUninstalledDelapp(deletedPkg.packageName);
                packageRemovedInfo.removedPackage = deletedPkg.packageName;
                packageRemovedInfo.sendPackageRemovedBroadcasts(true);
                unInstallDel = true;
            } else {
                newPkg = scanPackageTracedLI(disabledPs.codePath, parseFlags, 32, 0, null);
            }
            if (newPkg != null) {
                prepareAppDataAfterInstallLIF(newPkg);
                synchronized (this.mPackages) {
                    PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(newPkg.packageName);
                    ps.getPermissionsState().copyFrom(deletedPs.getPermissionsState());
                    updatePermissionsLPw(newPkg.packageName, newPkg, 3);
                    if (applyUserRestrictions) {
                        for (int userId : allUserHandles) {
                            ps.setInstalled(ArrayUtils.contains(outInfo.origUsers, userId), userId);
                            this.mSettings.writeRuntimePermissionsForUserLPr(userId, false);
                        }
                        this.mSettings.writeAllUsersPackageRestrictionsLPr();
                    }
                    if (writeSettings) {
                        this.mSettings.writeLPr();
                    }
                }
                return true;
            } else if (unInstallDel) {
                return true;
            } else {
                return false;
            }
        } catch (PackageManagerException e) {
            Slog.w(TAG, "Failed to restore system package:" + deletedPkg.packageName + ": " + e.getMessage());
            return false;
        } catch (Exception e2) {
            Slog.w(TAG, "Failed to delete system package:" + deletedPkg.packageName + ": " + e2.getMessage());
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean deleteInstalledPackageLIF(PackageSetting ps, boolean deleteCodeAndResources, int flags, int[] allUserHandles, PackageRemovedInfo outInfo, boolean writeSettings, Package replacingPackage) {
        synchronized (this.mPackages) {
            if (outInfo != null) {
                outInfo.uid = ps.appId;
            }
            if (!(outInfo == null || outInfo.removedChildPackages == null)) {
                int childCount = ps.childPackageNames != null ? ps.childPackageNames.size() : 0;
                for (int i = 0; i < childCount; i++) {
                    String childPackageName = (String) ps.childPackageNames.get(i);
                    PackageSetting childPs = (PackageSetting) this.mSettings.mPackages.get(childPackageName);
                    if (childPs == null) {
                        return false;
                    }
                    PackageRemovedInfo childInfo = (PackageRemovedInfo) outInfo.removedChildPackages.get(childPackageName);
                    if (childInfo != null) {
                        childInfo.uid = childPs.appId;
                    }
                }
            }
        }
    }

    public boolean setBlockUninstallForUser(String packageName, boolean blockUninstall, int userId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DELETE_PACKAGES", null);
        synchronized (this.mPackages) {
            PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(packageName);
            if (ps == null) {
                Log.i(TAG, "Package doesn't exist in set block uninstall " + packageName);
                return false;
            } else if (ps.getInstalled(userId)) {
                ps.setBlockUninstall(blockUninstall, userId);
                this.mSettings.writePackageRestrictionsLPr(userId);
                return true;
            } else {
                Log.i(TAG, "Package not installed in set block uninstall " + packageName);
                return false;
            }
        }
    }

    public boolean getBlockUninstallForUser(String packageName, int userId) {
        synchronized (this.mPackages) {
            PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(packageName);
            if (ps == null) {
                Log.i(TAG, "Package doesn't exist in get block uninstall " + packageName);
                return false;
            }
            boolean blockUninstall = ps.getBlockUninstall(userId);
            return blockUninstall;
        }
    }

    public boolean setRequiredForSystemUser(String packageName, boolean systemUserApp) {
        int callingUid = Binder.getCallingUid();
        if (callingUid == 1000 || callingUid == 0) {
            synchronized (this.mPackages) {
                PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(packageName);
                if (ps == null) {
                    Log.w(TAG, "Package doesn't exist: " + packageName);
                    return false;
                }
                if (systemUserApp) {
                    ps.pkgPrivateFlags |= 1024;
                } else {
                    ps.pkgPrivateFlags &= -1025;
                }
                this.mSettings.writeLPr();
                return true;
            }
        }
        throw new SecurityException("setRequiredForSystemUser can only be run by the system or root");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean deletePackageLIF(String packageName, UserHandle user, boolean deleteCodeAndResources, int[] allUserHandles, int flags, PackageRemovedInfo outInfo, boolean writeSettings, Package replacingPackage) {
        if (packageName == null) {
            Slog.w(TAG, "Attempt to delete null packageName.");
            return false;
        }
        synchronized (this.mPackages) {
            PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(packageName);
            if (ps == null) {
                Slog.w(TAG, "Package named '" + packageName + "' doesn't exist.");
                return false;
            } else if (ps.parentPackageName == null || (isSystemApp(ps) && (flags & 4) == 0)) {
            } else {
                int removedUserId;
                if (user != null) {
                    removedUserId = user.getIdentifier();
                } else {
                    removedUserId = -1;
                }
                if (clearPackageStateForUserLIF(ps, removedUserId, outInfo)) {
                    markPackageUninstalledForUserLPw(ps, user);
                    scheduleWritePackageRestrictionsLocked(user);
                    return true;
                }
                return false;
            }
        }
    }

    private void markPackageUninstalledForUserLPw(PackageSetting ps, UserHandle user) {
        int[] userIds = (user == null || user.getIdentifier() == -1) ? sUserManager.getUserIds() : new int[]{user.getIdentifier()};
        for (int nextUserId : userIds) {
            ps.setUserState(nextUserId, 0, 0, false, true, true, false, false, null, null, null, false, ps.readUserState(nextUserId).domainVerificationStatus, 0);
        }
    }

    private boolean clearPackageStateForUserLIF(PackageSetting ps, int userId, PackageRemovedInfo outInfo) {
        synchronized (this.mPackages) {
            Package pkg = (Package) this.mPackages.get(ps.name);
        }
        int[] userIds = userId == -1 ? sUserManager.getUserIds() : new int[]{userId};
        for (int nextUserId : userIds) {
            destroyAppDataLIF(pkg, userId, 3);
            destroyAppProfilesLIF(pkg, userId);
            removeKeystoreDataIfNeeded(nextUserId, ps.appId);
            schedulePackageCleaning(ps.name, nextUserId, false);
            synchronized (this.mPackages) {
                if (clearPackagePreferredActivitiesLPw(ps.name, nextUserId)) {
                    scheduleWritePackageRestrictionsLocked(nextUserId);
                }
                resetUserChangesToRuntimePermissionsAndFlagsLPw(ps, nextUserId);
            }
        }
        if (outInfo != null) {
            outInfo.removedPackage = ps.name;
            outInfo.removedAppId = ps.appId;
            outInfo.removedUsers = userIds;
        }
        return true;
    }

    private void checkMemoryExec(boolean succeeded) {
        if (succeeded) {
            DeviceStorageMonitorInternal dsm = (DeviceStorageMonitorInternal) LocalServices.getService(DeviceStorageMonitorInternal.class);
            if (dsm != null) {
                dsm.checkMemory();
            }
        }
    }

    private void removeCompletedExec(String packageName, IPackageDataObserver observer, boolean succeeded) {
        if (observer != null) {
            try {
                observer.onRemoveCompleted(packageName, succeeded);
            } catch (RemoteException e) {
                Log.i(TAG, "Observer no longer exists.");
            }
        }
    }

    private void clearApplicationUserDataExec(String packageName, int userId, boolean allData, boolean succeeded, IPackageDataObserver observer) {
        final String str = packageName;
        final int i = userId;
        final boolean z = allData;
        final boolean z2 = succeeded;
        final IPackageDataObserver iPackageDataObserver = observer;
        this.clearDirectoryThread.submit(new Runnable() {
            public void run() {
                PackageManagerService.this.clearExternalStorageDataSync(str, i, z);
                PackageManagerService.this.checkMemoryExec(z2);
                PackageManagerService.this.removeCompletedExec(str, iPackageDataObserver, z2);
            }
        });
    }

    private void deleteApplicationCacheFilesExec(String packageName, int userId, boolean allData, boolean succeeded, IPackageDataObserver observer) {
        final String str = packageName;
        final int i = userId;
        final boolean z = allData;
        final IPackageDataObserver iPackageDataObserver = observer;
        final boolean z2 = succeeded;
        this.clearDirectoryThread.submit(new Runnable() {
            public void run() {
                PackageManagerService.this.clearExternalStorageDataSync(str, i, z);
                PackageManagerService.this.removeCompletedExec(str, iPackageDataObserver, z2);
            }
        });
    }

    private void clearExternalStorageDataSync(String packageName, int userId, boolean allData) {
        if (!DEFAULT_CONTAINER_PACKAGE.equals(packageName)) {
            boolean z;
            if (Environment.isExternalStorageEmulated()) {
                z = true;
            } else {
                String status = Environment.getExternalStorageState();
                if (status.equals("mounted")) {
                    z = true;
                } else {
                    z = status.equals("mounted_ro");
                }
            }
            if (z) {
                Intent containerIntent = new Intent().setComponent(DEFAULT_CONTAINER_COMPONENT);
                int[] users = userId == -1 ? sUserManager.getUserIds() : new int[]{userId};
                PackageManagerService packageManagerService = this;
                ClearStorageConnection conn = new ClearStorageConnection();
                if (this.mContext.bindServiceAsUser(containerIntent, conn, 1, UserHandle.SYSTEM)) {
                    int i = 0;
                    int length = users.length;
                    while (i < length) {
                        int curUser = users[i];
                        long timeout = SystemClock.uptimeMillis() + 480000;
                        synchronized (conn) {
                            while (conn.mContainerService == null) {
                                long now = SystemClock.uptimeMillis();
                                if (now < timeout) {
                                    try {
                                        conn.wait(timeout - now);
                                    } catch (InterruptedException e) {
                                    }
                                    now = SystemClock.uptimeMillis();
                                }
                            }
                        }
                        if (conn.mContainerService == null) {
                            Slog.w(TAG, "clearExternalStorageDataSync fail reason: Bind ContainerService Timeout");
                            this.mContext.unbindService(conn);
                            return;
                        }
                        try {
                            UserEnvironment userEnv = new UserEnvironment(curUser);
                            clearDirectory(conn.mContainerService, userEnv.buildExternalStorageAppCacheDirs(packageName));
                            if (allData) {
                                clearDirectory(conn.mContainerService, userEnv.buildExternalStorageAppDataDirs(packageName));
                                clearDirectory(conn.mContainerService, userEnv.buildExternalStorageAppMediaDirs(packageName));
                            }
                            i++;
                        } catch (Throwable th) {
                            this.mContext.unbindService(conn);
                        }
                    }
                    this.mContext.unbindService(conn);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void clearApplicationProfileData(String packageName) {
        Throwable th = null;
        enforceSystemOrRoot("Only the system can clear all profile data");
        synchronized (this.mPackages) {
            Package pkg = (Package) this.mPackages.get(packageName);
        }
        PackageFreezer packageFreezer = null;
        try {
            packageFreezer = freezePackage(packageName, "clearApplicationProfileData");
            synchronized (this.mInstallLock) {
                clearAppProfilesLIF(pkg, -1);
                destroyAppReferenceProfileLeafLIF(pkg, -1, true);
            }
            if (packageFreezer != null) {
                try {
                    packageFreezer.close();
                } catch (Throwable th2) {
                    th = th2;
                }
            }
            if (th != null) {
                throw th;
            }
        } catch (Throwable th3) {
            Throwable th4 = th3;
            if (packageFreezer != null) {
                try {
                    packageFreezer.close();
                } catch (Throwable th5) {
                    if (th == null) {
                        th = th5;
                    } else if (th != th5) {
                        th.addSuppressed(th5);
                    }
                }
            }
            if (th != null) {
                throw th;
            }
            throw th4;
        }
    }

    public void clearApplicationUserData(final String packageName, final IPackageDataObserver observer, final int userId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CLEAR_APP_USER_DATA", null);
        enforceCrossUserPermission(Binder.getCallingUid(), userId, true, false, "clear application data");
        if (this.mProtectedPackages.canPackageBeWiped(userId, packageName)) {
            throw new SecurityException("Cannot clear data for a device owner or a profile owner");
        }
        this.clearDirectoryThread.submit(new Runnable() {
            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                Throwable th = null;
                PackageManagerService.this.mHandler.removeCallbacks(this);
                PackageFreezer packageFreezer = null;
                try {
                    boolean succeeded;
                    packageFreezer = PackageManagerService.this.freezePackage(packageName, "clearApplicationUserData");
                    synchronized (PackageManagerService.this.mInstallLock) {
                        succeeded = PackageManagerService.this.clearApplicationUserDataLIF(packageName, userId);
                    }
                    PackageManagerService.this.clearExternalStorageDataSync(packageName, userId, true);
                    if (packageFreezer != null) {
                        try {
                            packageFreezer.close();
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    }
                    if (th != null) {
                        throw th;
                    }
                    PackageManagerService.this.clearApplicationUserDataExec(packageName, userId, true, succeeded, observer);
                    if (succeeded) {
                        DeviceStorageMonitorInternal dsm = (DeviceStorageMonitorInternal) LocalServices.getService(DeviceStorageMonitorInternal.class);
                        if (dsm != null) {
                            dsm.checkMemory();
                        }
                        IndexSearchManager.getInstance().clearUserIndexSearchData(packageName, userId);
                    }
                    if (observer != null) {
                        try {
                            observer.onRemoveCompleted(packageName, succeeded);
                        } catch (RemoteException e) {
                            Log.i(PackageManagerService.TAG, "Observer no longer exists.");
                        }
                    }
                } catch (Throwable th3) {
                    Throwable th4 = th3;
                    if (packageFreezer != null) {
                        try {
                            packageFreezer.close();
                        } catch (Throwable th5) {
                            if (th == null) {
                                th = th5;
                            } else if (th != th5) {
                                th.addSuppressed(th5);
                            }
                        }
                    }
                    if (th != null) {
                        throw th;
                    }
                    throw th4;
                }
            }
        });
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean clearApplicationUserDataLIF(String packageName, int userId) {
        if (packageName == null) {
            Slog.w(TAG, "Attempt to delete null packageName.");
            return false;
        }
        synchronized (this.mPackages) {
            Package pkg = (Package) this.mPackages.get(packageName);
            if (pkg == null) {
                PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(packageName);
                if (ps != null) {
                    pkg = ps.pkg;
                }
            }
            if (pkg == null) {
                Slog.w(TAG, "Package named '" + packageName + "' doesn't exist.");
                return false;
            }
            resetUserChangesToRuntimePermissionsAndFlagsLPw((PackageSetting) pkg.mExtras, userId);
        }
    }

    private void resetUserChangesToRuntimePermissionsAndFlagsLPw(int userId) {
        int packageCount = this.mPackages.size();
        for (int i = 0; i < packageCount; i++) {
            resetUserChangesToRuntimePermissionsAndFlagsLPw(((Package) this.mPackages.valueAt(i)).mExtras, userId);
        }
    }

    private void resetNetworkPolicies(int userId) {
        ((NetworkPolicyManagerInternal) LocalServices.getService(NetworkPolicyManagerInternal.class)).resetUserState(userId);
    }

    private void resetUserChangesToRuntimePermissionsAndFlagsLPw(PackageSetting ps, int userId) {
        if (ps.pkg != null) {
            boolean writeInstallPermissions = false;
            boolean writeRuntimePermissions = false;
            int permissionCount = ps.pkg.requestedPermissions.size();
            for (int i = 0; i < permissionCount; i++) {
                String permission = (String) ps.pkg.requestedPermissions.get(i);
                BasePermission bp = (BasePermission) this.mSettings.mPermissions.get(permission);
                if (bp != null) {
                    if (ps.sharedUser != null) {
                        boolean used = false;
                        int packageCount = ps.sharedUser.packages.size();
                        int j = 0;
                        while (j < packageCount) {
                            PackageSetting pkg = (PackageSetting) ps.sharedUser.packages.valueAt(j);
                            if (pkg.pkg == null || pkg.pkg.packageName.equals(ps.pkg.packageName) || !pkg.pkg.requestedPermissions.contains(permission)) {
                                j++;
                            } else {
                                used = true;
                                if (used) {
                                }
                            }
                        }
                        if (used) {
                        }
                    }
                    PermissionsState permissionsState = ps.getPermissionsState();
                    int oldFlags = permissionsState.getPermissionFlags(bp.name, userId);
                    boolean hasInstallState = permissionsState.getInstallPermissionState(bp.name) != null;
                    int flags = 0;
                    if (Build.PERMISSIONS_REVIEW_REQUIRED && ps.pkg.applicationInfo.targetSdkVersion < 23) {
                        flags = 64;
                    }
                    if (permissionsState.updatePermissionFlags(bp, userId, 75, flags)) {
                        if (hasInstallState) {
                            writeInstallPermissions = true;
                        } else {
                            writeRuntimePermissions = true;
                        }
                    }
                    if (bp.isRuntime() && (oldFlags & 20) == 0) {
                        if ((oldFlags & 32) != 0) {
                            if (permissionsState.grantRuntimePermission(bp, userId) != -1) {
                                writeRuntimePermissions = true;
                            }
                        } else if ((flags & 64) == 0) {
                            switch (permissionsState.revokeRuntimePermission(bp, userId)) {
                                case 0:
                                case 1:
                                    writeRuntimePermissions = true;
                                    final int appId = ps.appId;
                                    final int i2 = userId;
                                    this.mHandler.post(new Runnable() {
                                        public void run() {
                                            PackageManagerService.this.killUid(appId, i2, PackageManagerService.KILL_APP_REASON_PERMISSIONS_REVOKED);
                                        }
                                    });
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            }
            if (writeRuntimePermissions) {
                this.mSettings.writeRuntimePermissionsForUserLPr(userId, true);
            }
            if (writeInstallPermissions) {
                this.mSettings.writeLPr();
            }
        }
    }

    private static void removeKeystoreDataIfNeeded(int userId, int appId) {
        if (appId >= 0) {
            KeyStore keyStore = KeyStore.getInstance();
            if (keyStore == null) {
                Slog.w(TAG, "Could not contact keystore to clear entries for app id " + appId);
            } else if (userId == -1) {
                for (int individual : sUserManager.getUserIds()) {
                    keyStore.clearUid(UserHandle.getUid(individual, appId));
                }
            } else {
                keyStore.clearUid(UserHandle.getUid(userId, appId));
            }
        }
    }

    public void deleteApplicationCacheFiles(String packageName, IPackageDataObserver observer) {
        deleteApplicationCacheFilesAsUser(packageName, UserHandle.getCallingUserId(), observer);
    }

    public void deleteApplicationCacheFilesAsUser(String packageName, int userId, IPackageDataObserver observer) {
        final Package pkg;
        this.mContext.enforceCallingOrSelfPermission("android.permission.DELETE_CACHE_FILES", null);
        enforceCrossUserPermission(Binder.getCallingUid(), userId, true, false, "delete application cache files");
        synchronized (this.mPackages) {
            pkg = (Package) this.mPackages.get(packageName);
        }
        final int i = userId;
        final String str = packageName;
        final IPackageDataObserver iPackageDataObserver = observer;
        this.clearDirectoryThread.submit(new Runnable() {
            public void run() {
                synchronized (PackageManagerService.this.mInstallLock) {
                    PackageManagerService.this.clearAppDataLIF(pkg, i, 259);
                    PackageManagerService.this.clearAppDataLIF(pkg, i, 515);
                }
                PackageManagerService.this.clearExternalStorageDataSync(str, i, false);
                if (iPackageDataObserver != null) {
                    try {
                        iPackageDataObserver.onRemoveCompleted(str, true);
                    } catch (RemoteException e) {
                        Log.i(PackageManagerService.TAG, "Observer no longer exists.");
                    }
                }
            }
        });
    }

    public void getPackageSizeInfo(String packageName, int userHandle, IPackageStatsObserver observer) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.GET_PACKAGE_SIZE", null);
        if (packageName == null) {
            throw new IllegalArgumentException("Attempt to get size of null packageName");
        }
        PackageStats stats = new PackageStats(packageName, userHandle);
        Message msg = this.mHandler.obtainMessage(5);
        msg.obj = new MeasureParams(stats, observer);
        this.mHandler.sendMessage(msg);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean getPackageSizeInfoLI(String packageName, int userId, PackageStats stats) {
        synchronized (this.mPackages) {
            PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(packageName);
            if (ps == null) {
                Slog.w(TAG, "Failed to find settings for " + packageName);
                return false;
            }
        }
    }

    private int getUidTargetSdkVersionLockedLPr(int uid) {
        SharedUserSetting obj = this.mSettings.getUserIdLPr(uid);
        PackageSetting ps;
        if (obj instanceof SharedUserSetting) {
            int vers = 10000;
            Iterator<PackageSetting> it = obj.packages.iterator();
            while (it.hasNext()) {
                ps = (PackageSetting) it.next();
                if (ps.pkg != null) {
                    int v = ps.pkg.applicationInfo.targetSdkVersion;
                    if (v < vers) {
                        vers = v;
                    }
                }
            }
            return vers;
        }
        if (obj instanceof PackageSetting) {
            ps = (PackageSetting) obj;
            if (ps.pkg != null) {
                return ps.pkg.applicationInfo.targetSdkVersion;
            }
        }
        return 10000;
    }

    public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
        addPreferredActivityInternal(filter, match, set, activity, true, userId, "Adding preferred");
    }

    protected void addPreferredActivityInternal(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, boolean always, int userId, String opname) {
        int callingUid = Binder.getCallingUid();
        if (activity != null) {
            Slog.d(TAG, opname + "add pref activity " + activity.flattenToShortString() + " from uid " + Binder.getCallingUid());
        }
        enforceCrossUserPermission(callingUid, userId, true, false, "add preferred activity");
        if (filter.countActions() == 0) {
            Slog.w(TAG, "Cannot set a preferred activity with no filter actions");
            return;
        }
        synchronized (this.mPackages) {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.SET_PREFERRED_APPLICATIONS") != 0) {
                if (getUidTargetSdkVersionLockedLPr(callingUid) < 8) {
                    Slog.w(TAG, "Ignoring addPreferredActivity() from uid " + callingUid);
                    return;
                }
                this.mContext.enforceCallingOrSelfPermission("android.permission.SET_PREFERRED_APPLICATIONS", null);
            }
            PreferredIntentResolver pir = this.mSettings.editPreferredActivitiesLPw(userId);
            if (activity == null) {
                Slog.w(TAG, "Cannot set a preferred activity with activity is null");
                return;
            }
            Slog.i(TAG, opname + " activity " + activity.flattenToShortString() + " for user " + userId + ":");
            filter.dump(new LogPrinter(4, TAG), "  ");
            pir.addFilter(new PreferredActivity(filter, match, set, activity, always));
            scheduleWritePackageRestrictionsLocked(userId);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void replacePreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
        if (filter.countActions() != 1) {
            throw new IllegalArgumentException("replacePreferredActivity expects filter to have only 1 action.");
        } else if (filter.countDataAuthorities() == 0 && filter.countDataPaths() == 0 && filter.countDataSchemes() <= 1 && filter.countDataTypes() == 0) {
            int callingUid = Binder.getCallingUid();
            enforceCrossUserPermission(callingUid, userId, true, false, "replace preferred activity");
            synchronized (this.mPackages) {
                if (this.mContext.checkCallingOrSelfPermission("android.permission.SET_PREFERRED_APPLICATIONS") != 0) {
                    if (getUidTargetSdkVersionLockedLPr(callingUid) < 8) {
                        Slog.w(TAG, "Ignoring replacePreferredActivity() from uid " + Binder.getCallingUid());
                        return;
                    }
                    this.mContext.enforceCallingOrSelfPermission("android.permission.SET_PREFERRED_APPLICATIONS", null);
                }
                PreferredIntentResolver pir = (PreferredIntentResolver) this.mSettings.mPreferredActivities.get(userId);
                if (pir != null) {
                    ArrayList<PreferredActivity> existing = pir.findFilters(filter);
                    if (existing != null && existing.size() == 1) {
                        PreferredActivity cur = (PreferredActivity) existing.get(0);
                        if (cur.mPref.mAlways && cur.mPref.mComponent.equals(activity) && cur.mPref.mMatch == (268369920 & match) && cur.mPref.sameSet(set)) {
                            return;
                        }
                    }
                    if (existing != null) {
                        for (int i = 0; i < existing.size(); i++) {
                            pir.removeFilter((PreferredActivity) existing.get(i));
                        }
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("replacePreferredActivity expects filter to have no data authorities, paths, or types; and at most one scheme.");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void clearPackagePreferredActivities(String packageName) {
        int uid = Binder.getCallingUid();
        synchronized (this.mPackages) {
            Package pkg = (Package) this.mPackages.get(packageName);
            if ((pkg == null || pkg.applicationInfo.uid != uid) && this.mContext.checkCallingOrSelfPermission("android.permission.SET_PREFERRED_APPLICATIONS") != 0) {
                if (getUidTargetSdkVersionLockedLPr(Binder.getCallingUid()) < 8) {
                    Slog.w(TAG, "Ignoring clearPackagePreferredActivities() from uid " + Binder.getCallingUid());
                    return;
                }
                this.mContext.enforceCallingOrSelfPermission("android.permission.SET_PREFERRED_APPLICATIONS", null);
            }
            int user = UserHandle.getCallingUserId();
            if (clearPackagePreferredActivitiesLPw(packageName, user)) {
                scheduleWritePackageRestrictionsLocked(user);
            }
        }
    }

    boolean clearPackagePreferredActivitiesLPw(String packageName, int userId) {
        Slog.d(TAG, "clear pref activity " + packageName + " from uid " + Binder.getCallingUid());
        ArrayList removed = null;
        boolean changed = false;
        for (int i = 0; i < this.mSettings.mPreferredActivities.size(); i++) {
            int thisUserId = this.mSettings.mPreferredActivities.keyAt(i);
            PreferredIntentResolver pir = (PreferredIntentResolver) this.mSettings.mPreferredActivities.valueAt(i);
            if (userId == -1 || userId == thisUserId) {
                Iterator<PreferredActivity> it = pir.filterIterator();
                while (it.hasNext()) {
                    PreferredActivity pa = (PreferredActivity) it.next();
                    if (packageName == null || (pa.mPref.mComponent.getPackageName().equals(packageName) && pa.mPref.mAlways)) {
                        if (!HwDeviceManager.disallowOp(17) || !pa.hasAction("android.intent.action.MAIN") || !pa.hasCategory("android.intent.category.HOME") || !pa.hasCategory("android.intent.category.DEFAULT")) {
                            if (removed == null) {
                                removed = new ArrayList();
                            }
                            removed.add(pa);
                        }
                    }
                }
                if (removed != null) {
                    for (int j = 0; j < removed.size(); j++) {
                        pir.removeFilter((PreferredActivity) removed.get(j));
                    }
                    changed = true;
                }
            }
        }
        return changed;
    }

    private void clearIntentFilterVerificationsLPw(int userId) {
        int packageCount = this.mPackages.size();
        for (int i = 0; i < packageCount; i++) {
            clearIntentFilterVerificationsLPw(((Package) this.mPackages.valueAt(i)).packageName, userId);
        }
    }

    void clearIntentFilterVerificationsLPw(String packageName, int userId) {
        if (userId == -1) {
            if (this.mSettings.removeIntentFilterVerificationLPw(packageName, sUserManager.getUserIds())) {
                for (int oneUserId : sUserManager.getUserIds()) {
                    scheduleWritePackageRestrictionsLocked(oneUserId);
                }
            }
        } else if (this.mSettings.removeIntentFilterVerificationLPw(packageName, userId)) {
            scheduleWritePackageRestrictionsLocked(userId);
        }
    }

    void clearDefaultBrowserIfNeeded(String packageName) {
        for (int oneUserId : sUserManager.getUserIds()) {
            String defaultBrowserPackageName = getDefaultBrowserPackageName(oneUserId);
            if (!TextUtils.isEmpty(defaultBrowserPackageName) && packageName.equals(defaultBrowserPackageName)) {
                setDefaultBrowserPackageName(null, oneUserId);
            }
        }
    }

    public void resetApplicationPreferences(int userId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.SET_PREFERRED_APPLICATIONS", null);
        long identity = Binder.clearCallingIdentity();
        try {
            synchronized (this.mPackages) {
                clearPackagePreferredActivitiesLPw(null, userId);
                this.mSettings.applyDefaultPreferredAppsLPw(this, userId);
                applyFactoryDefaultBrowserLPw(userId);
                clearIntentFilterVerificationsLPw(userId);
                primeDomainVerificationsLPw(userId);
                resetUserChangesToRuntimePermissionsAndFlagsLPw(userId);
                scheduleWritePackageRestrictionsLocked(userId);
            }
            resetNetworkPolicies(userId);
            Binder.restoreCallingIdentity(identity);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) {
        int userId = UserHandle.getCallingUserId();
        synchronized (this.mPackages) {
            PreferredIntentResolver pir = (PreferredIntentResolver) this.mSettings.mPreferredActivities.get(userId);
            if (pir != null) {
                Iterator<PreferredActivity> it = pir.filterIterator();
                while (it.hasNext()) {
                    PreferredActivity pa = (PreferredActivity) it.next();
                    if (packageName == null || (pa.mPref.mComponent.getPackageName().equals(packageName) && pa.mPref.mAlways)) {
                        if (outFilters != null) {
                            outFilters.add(new IntentFilter(pa));
                        }
                        if (outActivities != null) {
                            outActivities.add(pa.mPref.mComponent);
                        }
                    }
                }
            }
        }
        return 0;
    }

    public void addPersistentPreferredActivity(IntentFilter filter, ComponentName activity, int userId) {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("addPersistentPreferredActivity can only be run by the system");
        } else if (filter.countActions() == 0) {
            Slog.w(TAG, "Cannot set a preferred activity with no filter actions");
        } else {
            synchronized (this.mPackages) {
                Slog.i(TAG, "Adding persistent preferred activity " + activity + " for user " + userId + ":");
                filter.dump(new LogPrinter(4, TAG), "  ");
                this.mSettings.editPersistentPreferredActivitiesLPw(userId).addFilter(new PersistentPreferredActivity(filter, activity));
                scheduleWritePackageRestrictionsLocked(userId);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void clearPackagePersistentPreferredActivities(String packageName, int userId) {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("clearPackagePersistentPreferredActivities can only be run by the system");
        }
        ArrayList<PersistentPreferredActivity> removed = null;
        boolean changed = false;
        synchronized (this.mPackages) {
            int i = 0;
            while (i < this.mSettings.mPersistentPreferredActivities.size()) {
                try {
                    PersistentPreferredIntentResolver ppir = (PersistentPreferredIntentResolver) this.mSettings.mPersistentPreferredActivities.valueAt(i);
                    if (userId == this.mSettings.mPersistentPreferredActivities.keyAt(i)) {
                        Iterator<PersistentPreferredActivity> it = ppir.filterIterator();
                        ArrayList<PersistentPreferredActivity> removed2 = removed;
                        while (it.hasNext()) {
                            PersistentPreferredActivity ppa = (PersistentPreferredActivity) it.next();
                            if (ppa.mComponent.getPackageName().equals(packageName)) {
                                if (removed2 == null) {
                                    removed = new ArrayList();
                                } else {
                                    removed = removed2;
                                }
                                removed.add(ppa);
                            } else {
                                removed = removed2;
                            }
                            removed2 = removed;
                        }
                        if (removed2 != null) {
                            int j = 0;
                            while (j < removed2.size()) {
                                try {
                                    ppir.removeFilter((PersistentPreferredActivity) removed2.get(j));
                                    j++;
                                } catch (Throwable th) {
                                    Throwable th2 = th;
                                    removed = removed2;
                                }
                            }
                            changed = true;
                            removed = removed2;
                        } else {
                            removed = removed2;
                        }
                    }
                    i++;
                } catch (Throwable th3) {
                    th2 = th3;
                }
            }
            if (changed) {
                scheduleWritePackageRestrictionsLocked(userId);
            }
        }
        throw th2;
    }

    private void restoreFromXml(XmlPullParser parser, int userId, String expectedStartTag, BlobXmlRestorer functor) throws IOException, XmlPullParserException {
        int type;
        do {
            type = parser.next();
            if (type == 2) {
                break;
            }
        } while (type != 1);
        if (type == 2) {
            Slog.v(TAG, ":: restoreFromXml() : got to tag " + parser.getName());
            if (expectedStartTag.equals(parser.getName())) {
                do {
                } while (parser.next() == 4);
                Slog.v(TAG, ":: stepped forward, applying functor at tag " + parser.getName());
                functor.apply(parser, userId);
            }
        }
    }

    public byte[] getPreferredActivityBackup(int userId) {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Only the system may call getPreferredActivityBackup()");
        }
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        try {
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(dataStream, StandardCharsets.UTF_8.name());
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.startTag(null, TAG_PREFERRED_BACKUP);
            synchronized (this.mPackages) {
                this.mSettings.writePreferredActivitiesLPr(serializer, userId, true);
            }
            serializer.endTag(null, TAG_PREFERRED_BACKUP);
            serializer.endDocument();
            serializer.flush();
            return dataStream.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    public void restorePreferredActivities(byte[] backup, int userId) {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Only the system may call restorePreferredActivities()");
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new ByteArrayInputStream(backup), StandardCharsets.UTF_8.name());
            restoreFromXml(parser, userId, TAG_PREFERRED_BACKUP, new BlobXmlRestorer() {
                public void apply(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
                    synchronized (PackageManagerService.this.mPackages) {
                        PackageManagerService.this.mSettings.readPreferredActivitiesLPw(parser, userId);
                    }
                }
            });
        } catch (Exception e) {
        }
    }

    public byte[] getDefaultAppsBackup(int userId) {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Only the system may call getDefaultAppsBackup()");
        }
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        try {
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(dataStream, StandardCharsets.UTF_8.name());
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.startTag(null, TAG_DEFAULT_APPS);
            synchronized (this.mPackages) {
                this.mSettings.writeDefaultAppsLPr(serializer, userId);
            }
            serializer.endTag(null, TAG_DEFAULT_APPS);
            serializer.endDocument();
            serializer.flush();
            return dataStream.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    public void restoreDefaultApps(byte[] backup, int userId) {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Only the system may call restoreDefaultApps()");
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new ByteArrayInputStream(backup), StandardCharsets.UTF_8.name());
            restoreFromXml(parser, userId, TAG_DEFAULT_APPS, new BlobXmlRestorer() {
                public void apply(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
                    synchronized (PackageManagerService.this.mPackages) {
                        PackageManagerService.this.mSettings.readDefaultAppsLPw(parser, userId);
                    }
                }
            });
        } catch (Exception e) {
        }
    }

    public byte[] getIntentFilterVerificationBackup(int userId) {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Only the system may call getIntentFilterVerificationBackup()");
        }
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        try {
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(dataStream, StandardCharsets.UTF_8.name());
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.startTag(null, TAG_INTENT_FILTER_VERIFICATION);
            synchronized (this.mPackages) {
                this.mSettings.writeAllDomainVerificationsLPr(serializer, userId);
            }
            serializer.endTag(null, TAG_INTENT_FILTER_VERIFICATION);
            serializer.endDocument();
            serializer.flush();
            return dataStream.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    public void restoreIntentFilterVerification(byte[] backup, int userId) {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Only the system may call restorePreferredActivities()");
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new ByteArrayInputStream(backup), StandardCharsets.UTF_8.name());
            restoreFromXml(parser, userId, TAG_INTENT_FILTER_VERIFICATION, new BlobXmlRestorer() {
                public void apply(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
                    synchronized (PackageManagerService.this.mPackages) {
                        PackageManagerService.this.mSettings.readAllDomainVerificationsLPr(parser, userId);
                        PackageManagerService.this.mSettings.writeLPr();
                    }
                }
            });
        } catch (Exception e) {
        }
    }

    public byte[] getPermissionGrantBackup(int userId) {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Only the system may call getPermissionGrantBackup()");
        }
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        try {
            XmlSerializer serializer = new FastXmlSerializer();
            serializer.setOutput(dataStream, StandardCharsets.UTF_8.name());
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.startTag(null, TAG_PERMISSION_BACKUP);
            synchronized (this.mPackages) {
                serializeRuntimePermissionGrantsLPr(serializer, userId);
            }
            serializer.endTag(null, TAG_PERMISSION_BACKUP);
            serializer.endDocument();
            serializer.flush();
            return dataStream.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    public void restorePermissionGrants(byte[] backup, int userId) {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("Only the system may call restorePermissionGrants()");
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new ByteArrayInputStream(backup), StandardCharsets.UTF_8.name());
            restoreFromXml(parser, userId, TAG_PERMISSION_BACKUP, new BlobXmlRestorer() {
                public void apply(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
                    synchronized (PackageManagerService.this.mPackages) {
                        PackageManagerService.this.processRestoredPermissionGrantsLPr(parser, userId);
                    }
                }
            });
        } catch (Exception e) {
        }
    }

    private void serializeRuntimePermissionGrantsLPr(XmlSerializer serializer, int userId) throws IOException {
        serializer.startTag(null, TAG_ALL_GRANTS);
        int N = this.mSettings.mPackages.size();
        for (int i = 0; i < N; i++) {
            boolean pkgGrantsKnown = false;
            for (PermissionState state : ((PackageSetting) this.mSettings.mPackages.valueAt(i)).getPermissionsState().getRuntimePermissionStates(userId)) {
                int grantFlags = state.getFlags();
                if ((grantFlags & 52) == 0) {
                    boolean isGranted = state.isGranted();
                    if (isGranted || (grantFlags & 11) != 0) {
                        String packageName = (String) this.mSettings.mPackages.keyAt(i);
                        if (!pkgGrantsKnown) {
                            serializer.startTag(null, TAG_GRANT);
                            serializer.attribute(null, "pkg", packageName);
                            pkgGrantsKnown = true;
                        }
                        boolean userSet = (grantFlags & 1) != 0;
                        boolean userFixed = (grantFlags & 2) != 0;
                        boolean revoke = (grantFlags & 8) != 0;
                        serializer.startTag(null, TAG_PERMISSION);
                        serializer.attribute(null, ATTR_PERMISSION_NAME, state.getName());
                        if (isGranted) {
                            serializer.attribute(null, ATTR_IS_GRANTED, "true");
                        }
                        if (userSet) {
                            serializer.attribute(null, ATTR_USER_SET, "true");
                        }
                        if (userFixed) {
                            serializer.attribute(null, ATTR_USER_FIXED, "true");
                        }
                        if (revoke) {
                            serializer.attribute(null, ATTR_REVOKE_ON_UPGRADE, "true");
                        }
                        serializer.endTag(null, TAG_PERMISSION);
                    }
                }
            }
            if (pkgGrantsKnown) {
                serializer.endTag(null, TAG_GRANT);
            }
        }
        serializer.endTag(null, TAG_ALL_GRANTS);
    }

    private void processRestoredPermissionGrantsLPr(XmlPullParser parser, int userId) throws XmlPullParserException, IOException {
        String pkgName = null;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                scheduleWriteSettingsLocked();
                this.mSettings.writeRuntimePermissionsForUserLPr(userId, false);
            } else if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (tagName.equals(TAG_GRANT)) {
                    pkgName = parser.getAttributeValue(null, "pkg");
                } else if (tagName.equals(TAG_PERMISSION)) {
                    boolean isGranted = "true".equals(parser.getAttributeValue(null, ATTR_IS_GRANTED));
                    String permName = parser.getAttributeValue(null, ATTR_PERMISSION_NAME);
                    int newFlagSet = 0;
                    if ("true".equals(parser.getAttributeValue(null, ATTR_USER_SET))) {
                        newFlagSet = 1;
                    }
                    if ("true".equals(parser.getAttributeValue(null, ATTR_USER_FIXED))) {
                        newFlagSet |= 2;
                    }
                    if ("true".equals(parser.getAttributeValue(null, ATTR_REVOKE_ON_UPGRADE))) {
                        newFlagSet |= 8;
                    }
                    PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(pkgName);
                    if (ps != null) {
                        PermissionsState perms = ps.getPermissionsState();
                        BasePermission bp = (BasePermission) this.mSettings.mPermissions.get(permName);
                        if (bp != null) {
                            if (isGranted) {
                                perms.grantRuntimePermission(bp, userId);
                            }
                            if (newFlagSet != 0) {
                                perms.updatePermissionFlags(bp, userId, 11, newFlagSet);
                            }
                        }
                    } else {
                        this.mSettings.processRestoredPermissionGrantLPr(pkgName, permName, isGranted, newFlagSet, userId);
                    }
                } else {
                    reportSettingsProblem(5, "Unknown element under <perm-grant-backup>: " + tagName);
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        scheduleWriteSettingsLocked();
        this.mSettings.writeRuntimePermissionsForUserLPr(userId, false);
    }

    public void addCrossProfileIntentFilter(IntentFilter intentFilter, String ownerPackage, int sourceUserId, int targetUserId, int flags) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", null);
        int callingUid = Binder.getCallingUid();
        enforceOwnerRights(ownerPackage, callingUid);
        enforceShellRestriction("no_debugging_features", callingUid, sourceUserId);
        if (intentFilter.countActions() == 0) {
            Slog.w(TAG, "Cannot set a crossProfile intent filter with no filter actions");
            return;
        }
        synchronized (this.mPackages) {
            CrossProfileIntentFilter newFilter = new CrossProfileIntentFilter(intentFilter, ownerPackage, targetUserId, flags);
            CrossProfileIntentResolver resolver = this.mSettings.editCrossProfileIntentResolverLPw(sourceUserId);
            ArrayList<CrossProfileIntentFilter> existing = resolver.findFilters(intentFilter);
            if (existing != null) {
                int size = existing.size();
                for (int i = 0; i < size; i++) {
                    if (newFilter.equalsIgnoreFilter((CrossProfileIntentFilter) existing.get(i))) {
                        return;
                    }
                }
            }
            resolver.addFilter(newFilter);
            scheduleWritePackageRestrictionsLocked(sourceUserId);
        }
    }

    public void clearCrossProfileIntentFilters(int sourceUserId, String ownerPackage) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", null);
        int callingUid = Binder.getCallingUid();
        enforceOwnerRights(ownerPackage, callingUid);
        enforceShellRestriction("no_debugging_features", callingUid, sourceUserId);
        synchronized (this.mPackages) {
            CrossProfileIntentResolver resolver = this.mSettings.editCrossProfileIntentResolverLPw(sourceUserId);
            for (CrossProfileIntentFilter filter : new ArraySet(resolver.filterSet())) {
                if (filter.getOwnerPackage().equals(ownerPackage)) {
                    resolver.removeFilter(filter);
                }
            }
            scheduleWritePackageRestrictionsLocked(sourceUserId);
        }
    }

    private void enforceOwnerRights(String pkg, int callingUid) {
        if (UserHandle.getAppId(callingUid) != 1000) {
            int callingUserId = UserHandle.getUserId(callingUid);
            PackageInfo pi = getPackageInfo(pkg, 0, callingUserId);
            if (pi == null) {
                throw new IllegalArgumentException("Unknown package " + pkg + " on user " + callingUserId);
            } else if (!UserHandle.isSameApp(pi.applicationInfo.uid, callingUid)) {
                throw new SecurityException("Calling uid " + callingUid + " does not own package " + pkg);
            }
        }
    }

    public ComponentName getHomeActivities(List<ResolveInfo> allHomeCandidates) {
        return getHomeActivitiesAsUser(allHomeCandidates, UserHandle.getCallingUserId());
    }

    private Intent getHomeIntent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        return intent;
    }

    private IntentFilter getHomeFilter() {
        IntentFilter filter = new IntentFilter("android.intent.action.MAIN");
        filter.addCategory("android.intent.category.HOME");
        filter.addCategory("android.intent.category.DEFAULT");
        return filter;
    }

    ComponentName getHomeActivitiesAsUser(List<ResolveInfo> allHomeCandidates, int userId) {
        Intent intent = getHomeIntent();
        List<ResolveInfo> list = queryIntentActivitiesInternal(intent, null, 128, userId);
        ResolveInfo preferred = findPreferredActivity(intent, null, 0, list, 0, true, false, false, userId);
        allHomeCandidates.clear();
        if (list != null) {
            for (ResolveInfo ri : list) {
                allHomeCandidates.add(ri);
            }
        }
        if (preferred == null || preferred.activityInfo == null) {
            return null;
        }
        return new ComponentName(preferred.activityInfo.packageName, preferred.activityInfo.name);
    }

    public void setHomeActivity(ComponentName comp, int userId) {
        ArrayList<ResolveInfo> homeActivities = new ArrayList();
        getHomeActivitiesAsUser(homeActivities, userId);
        boolean found = false;
        int size = homeActivities.size();
        ComponentName[] set = new ComponentName[size];
        for (int i = 0; i < size; i++) {
            ActivityInfo info = ((ResolveInfo) homeActivities.get(i)).activityInfo;
            ComponentName activityName = new ComponentName(info.packageName, info.name);
            set[i] = activityName;
            if (!found && activityName.equals(comp)) {
                found = true;
            }
        }
        if (found) {
            replacePreferredActivity(getHomeFilter(), DumpState.DUMP_DEXOPT, set, comp, userId);
            return;
        }
        throw new IllegalArgumentException("Component " + comp + " cannot be home on user " + userId);
    }

    private String getSetupWizardPackageName() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.SETUP_WIZARD");
        List<ResolveInfo> matches = queryIntentActivitiesInternal(intent, null, 1835520, UserHandle.myUserId());
        if (matches.size() == 1) {
            return ((ResolveInfo) matches.get(0)).getComponentInfo().packageName;
        }
        Slog.e(TAG, "There should probably be exactly one setup wizard; found " + matches.size() + ": matches=" + matches);
        return null;
    }

    public void setApplicationEnabledSetting(String appPackageName, int newState, int flags, int userId, String callingPackage) {
        if (sUserManager.exists(userId)) {
            if (callingPackage == null) {
                callingPackage = Integer.toString(Binder.getCallingUid());
            }
            setEnabledSetting(appPackageName, null, newState, flags, userId, callingPackage);
        }
    }

    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags, int userId) {
        if (sUserManager.exists(userId)) {
            Flog.i(206, "setComponentEnabledSetting pkg:" + componentName.getClassName() + ", newState:" + newState + ", flags:" + flags + ", userId:" + userId + ", CallingPid:" + Binder.getCallingPid() + ", CallingUid:" + Binder.getCallingUid());
            setEnabledSetting(componentName.getPackageName(), componentName.getClassName(), newState, flags, userId, null);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setEnabledSetting(String packageName, String className, int newState, int flags, int userId, String callingPackage) {
        if (newState == 0 || newState == 1 || newState == 2 || newState == 3 || newState == 4) {
            int permission;
            PackageSetting pkgSetting;
            int uid = Binder.getCallingUid();
            if (uid == 1000) {
                permission = 0;
            } else {
                permission = this.mContext.checkCallingOrSelfPermission("android.permission.CHANGE_COMPONENT_ENABLED_STATE");
            }
            enforceCrossUserPermission(uid, userId, false, true, "set enabled");
            boolean allowedByPermission = permission == 0;
            boolean sendNow = false;
            String componentName = className == null ? packageName : className;
            synchronized (this.mPackages) {
                pkgSetting = (PackageSetting) this.mSettings.mPackages.get(packageName);
                if (pkgSetting != null) {
                } else if (className == null) {
                    throw new IllegalArgumentException("Unknown package: " + packageName);
                } else {
                    throw new IllegalArgumentException("Unknown component: " + packageName + "/" + className);
                }
            }
            if (!UserHandle.isSameApp(uid, pkgSetting.appId)) {
                if (!allowedByPermission) {
                    throw new SecurityException("Permission Denial: attempt to change component state from pid=" + Binder.getCallingPid() + ", uid=" + uid + ", package uid=" + pkgSetting.appId);
                } else if (this.mProtectedPackages.canPackageStateBeChanged(userId, packageName)) {
                    throw new SecurityException("Cannot disable a device owner or a profile owner");
                }
            }
            synchronized (this.mPackages) {
                ArrayList<String> components;
                boolean newPackage;
                if (uid == 2000) {
                    int oldState = pkgSetting.getEnabled(userId);
                    if (className == null && (oldState == 3 || oldState == 0 || oldState == 1)) {
                        if (!(newState == 3 || newState == 0)) {
                            if (newState == 1) {
                            }
                        }
                    }
                    throw new SecurityException("Shell cannot change component state for " + packageName + "/" + className + " to " + newState);
                }
                if (className != null) {
                    Package pkg = pkgSetting.pkg;
                    if (pkg == null || !pkg.hasComponentClassName(className)) {
                        if (pkg != null) {
                            if (pkg.applicationInfo.targetSdkVersion >= 16) {
                                throw new IllegalArgumentException("Component class " + className + " does not exist in " + packageName);
                            }
                        }
                        Slog.w(TAG, "Failed setComponentEnabledSetting: component class " + className + " does not exist in " + packageName);
                    }
                    switch (newState) {
                        case 0:
                            if (!pkgSetting.restoreComponentLPw(className, userId)) {
                                return;
                            }
                        case 1:
                            if (!pkgSetting.enableComponentLPw(className, userId)) {
                                return;
                            }
                            scheduleWritePackageRestrictionsLocked(userId);
                            components = this.mPendingBroadcasts.get(userId, packageName);
                            newPackage = components != null;
                            if (newPackage) {
                                components = new ArrayList();
                            }
                            if (!components.contains(componentName)) {
                                components.add(componentName);
                            }
                            if ((flags & 1) == 0) {
                                if (newPackage) {
                                    this.mPendingBroadcasts.put(userId, packageName, components);
                                }
                                if (!this.mHandler.hasMessages(1)) {
                                    this.mHandler.sendEmptyMessageDelayed(1, 10000);
                                    break;
                                }
                            }
                            sendNow = true;
                            this.mPendingBroadcasts.remove(userId, packageName);
                            break;
                            break;
                        case 2:
                            if (!pkgSetting.disableComponentLPw(className, userId)) {
                                return;
                            }
                        default:
                            Slog.e(TAG, "Invalid new component state: " + newState);
                            return;
                    }
                } else if (pkgSetting.getEnabled(userId) == newState) {
                    return;
                } else {
                    if (newState == 0 || newState == 1) {
                        callingPackage = null;
                    }
                    pkgSetting.setEnabled(newState, userId, callingPackage);
                }
                scheduleWritePackageRestrictionsLocked(userId);
                components = this.mPendingBroadcasts.get(userId, packageName);
                if (components != null) {
                }
                if (newPackage) {
                    components = new ArrayList();
                }
                if (components.contains(componentName)) {
                    components.add(componentName);
                }
                if ((flags & 1) == 0) {
                    if (newPackage) {
                        this.mPendingBroadcasts.put(userId, packageName, components);
                    }
                    if (this.mHandler.hasMessages(1)) {
                        this.mHandler.sendEmptyMessageDelayed(1, 10000);
                    }
                } else {
                    sendNow = true;
                    this.mPendingBroadcasts.remove(userId, packageName);
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid new component state: " + newState);
        }
    }

    public void flushPackageRestrictionsAsUser(int userId) {
        if (sUserManager.exists(userId)) {
            enforceCrossUserPermission(Binder.getCallingUid(), userId, false, false, "flushPackageRestrictions");
            synchronized (this.mPackages) {
                this.mSettings.writePackageRestrictionsLPr(userId);
                this.mDirtyUsers.remove(Integer.valueOf(userId));
                if (this.mDirtyUsers.isEmpty()) {
                    this.mHandler.removeMessages(14);
                }
            }
        }
    }

    protected void sendPackageChangedBroadcast(String packageName, boolean killFlag, ArrayList<String> componentNames, int packageUid) {
        Bundle extras = new Bundle(4);
        extras.putString("android.intent.extra.changed_component_name", (String) componentNames.get(0));
        String[] nameList = new String[componentNames.size()];
        componentNames.toArray(nameList);
        extras.putStringArray("android.intent.extra.changed_component_name_list", nameList);
        extras.putBoolean("android.intent.extra.DONT_KILL_APP", killFlag);
        extras.putInt("android.intent.extra.UID", packageUid);
        sendPackageBroadcast("android.intent.action.PACKAGE_CHANGED", packageName, extras, !componentNames.contains(packageName) ? 1073741824 : 0, null, null, new int[]{UserHandle.getUserId(packageUid)});
    }

    public void setPackageStoppedState(String packageName, boolean stopped, int userId) {
        if (sUserManager.exists(userId)) {
            int uid = Binder.getCallingUid();
            boolean allowedByPermission = this.mContext.checkCallingOrSelfPermission("android.permission.CHANGE_COMPONENT_ENABLED_STATE") == 0;
            enforceCrossUserPermission(uid, userId, true, true, "stop package");
            synchronized (this.mPackages) {
                if (this.mSettings.setPackageStoppedStateLPw(this, packageName, stopped, allowedByPermission, uid, userId)) {
                    scheduleWritePackageRestrictionsLocked(userId);
                }
            }
        }
    }

    public String getInstallerPackageName(String packageName) {
        String installerPackageNameLPr;
        synchronized (this.mPackages) {
            installerPackageNameLPr = this.mSettings.getInstallerPackageNameLPr(packageName);
        }
        return installerPackageNameLPr;
    }

    public boolean isOrphaned(String packageName) {
        boolean isOrphaned;
        synchronized (this.mPackages) {
            isOrphaned = this.mSettings.isOrphaned(packageName);
        }
        return isOrphaned;
    }

    public int getApplicationEnabledSetting(String packageName, int userId) {
        if (!sUserManager.exists(userId)) {
            return 2;
        }
        int applicationEnabledSettingLPr;
        enforceCrossUserPermission(Binder.getCallingUid(), userId, false, false, "get enabled");
        synchronized (this.mPackages) {
            applicationEnabledSettingLPr = this.mSettings.getApplicationEnabledSettingLPr(packageName, userId);
        }
        return applicationEnabledSettingLPr;
    }

    public int getComponentEnabledSetting(ComponentName componentName, int userId) {
        if (!sUserManager.exists(userId)) {
            return 2;
        }
        int componentEnabledSettingLPr;
        enforceCrossUserPermission(Binder.getCallingUid(), userId, false, false, "get component enabled");
        synchronized (this.mPackages) {
            componentEnabledSettingLPr = this.mSettings.getComponentEnabledSettingLPr(componentName, userId);
        }
        return componentEnabledSettingLPr;
    }

    public void enterSafeMode() {
        enforceSystemOrRoot("Only the system can request entering safe mode");
        if (!this.mSystemReady) {
            this.mSafeMode = true;
        }
    }

    public void systemReady() {
        this.mSystemReady = true;
        PackageParser.setCompatibilityModeEnabled(Global.getInt(this.mContext.getContentResolver(), "compatibility_mode", 1) == 1);
        int[] grantPermissionsUserIds = EMPTY_INT_ARRAY;
        synchronized (this.mPackages) {
            ArrayList<PreferredActivity> removed = new ArrayList();
            for (int i = 0; i < this.mSettings.mPreferredActivities.size(); i++) {
                PreferredIntentResolver pir = (PreferredIntentResolver) this.mSettings.mPreferredActivities.valueAt(i);
                removed.clear();
                for (PreferredActivity pa : pir.filterSet()) {
                    PreferredActivity pa2;
                    if (this.mActivities.mActivities.get(pa2.mPref.mComponent) == null) {
                        removed.add(pa2);
                    }
                }
                if (removed.size() > 0) {
                    for (int r = 0; r < removed.size(); r++) {
                        pa2 = (PreferredActivity) removed.get(r);
                        Slog.w(TAG, "Removing dangling preferred activity: " + pa2.mPref.mComponent);
                        pir.removeFilter(pa2);
                    }
                    this.mSettings.writePackageRestrictionsLPr(this.mSettings.mPreferredActivities.keyAt(i));
                }
            }
            for (int userId : UserManagerService.getInstance().getUserIds()) {
                if (!this.mSettings.areDefaultRuntimePermissionsGrantedLPr(userId)) {
                    grantPermissionsUserIds = ArrayUtils.appendInt(grantPermissionsUserIds, userId);
                }
            }
        }
        sUserManager.systemReady();
        HwThemeManager.applyDefaultHwTheme(true, this.mContext, 0);
        for (int userId2 : grantPermissionsUserIds) {
            this.mDefaultPermissionPolicy.grantDefaultPermissions(userId2);
        }
        for (int userId22 : UserManagerService.getInstance().getUserIds()) {
            this.mDefaultPermissionPolicy.grantCustDefaultPermissions(userId22);
        }
        if (this.mPostSystemReadyMessages != null) {
            for (Message msg : this.mPostSystemReadyMessages) {
                msg.sendToTarget();
            }
            this.mPostSystemReadyMessages = null;
        }
        ((StorageManager) this.mContext.getSystemService(StorageManager.class)).registerListener(this.mStorageListener);
        this.mInstallerService.systemReady();
        this.mPackageDexOptimizer.systemReady();
        ((MountServiceInternal) LocalServices.getService(MountServiceInternal.class)).addExternalStoragePolicy(new ExternalStorageMountPolicy() {
            public int getMountMode(int uid, String packageName) {
                if (Process.isIsolated(uid)) {
                    return 0;
                }
                if (PackageManagerService.this.checkUidPermission("android.permission.WRITE_MEDIA_STORAGE", uid) == 0 || PackageManagerService.this.checkUidPermission("android.permission.READ_EXTERNAL_STORAGE", uid) == -1) {
                    return 1;
                }
                if (PackageManagerService.this.checkUidPermission("android.permission.WRITE_EXTERNAL_STORAGE", uid) == -1) {
                    return 2;
                }
                return 3;
            }

            public boolean hasExternalStorage(int uid, String packageName) {
                return true;
            }
        });
        reconcileUsers(StorageManager.UUID_PRIVATE_INTERNAL);
        reconcileApps(StorageManager.UUID_PRIVATE_INTERNAL);
    }

    public boolean isSafeMode() {
        return this.mSafeMode;
    }

    public boolean hasSystemUidErrors() {
        return this.mHasSystemUidErrors;
    }

    static String arrayToString(int[] array) {
        StringBuffer buf = new StringBuffer(128);
        buf.append('[');
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(array[i]);
            }
        }
        buf.append(']');
        return buf.toString();
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) {
        new PackageManagerShellCommand(this).exec(this, in, out, err, args, resultReceiver);
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        BufferedReader in;
        AutoCloseable autoCloseable;
        Throwable th;
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump ActivityManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
            return;
        }
        int user;
        String name;
        Log.i(TAG, "Start dump, calling from : pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        DumpState dumpState = new DumpState();
        boolean fullPreferred = false;
        boolean checkin = false;
        String str = null;
        ArraySet<String> permissionNames = null;
        int opti = 0;
        while (opti < args.length) {
            String opt = args[opti];
            if (opt == null || opt.length() <= 0 || opt.charAt(0) != '-') {
                break;
            }
            opti++;
            if (!"-a".equals(opt)) {
                if ("-h".equals(opt)) {
                    pw.println("Package manager dump options:");
                    pw.println("  [-h] [-f] [--checkin] [cmd] ...");
                    pw.println("    --checkin: dump for a checkin");
                    pw.println("    -f: print details of intent filters");
                    pw.println("    -h: print this help");
                    pw.println("  cmd may be one of:");
                    pw.println("    l[ibraries]: list known shared libraries");
                    pw.println("    f[eatures]: list device features");
                    pw.println("    k[eysets]: print known keysets");
                    pw.println("    r[esolvers] [activity|service|receiver|content]: dump intent resolvers");
                    pw.println("    perm[issions]: dump permissions");
                    pw.println("    permission [name ...]: dump declaration and use of given permission");
                    pw.println("    pref[erred]: print preferred package settings");
                    pw.println("    preferred-xml [--full]: print preferred package settings as xml");
                    pw.println("    prov[iders]: dump content providers");
                    pw.println("    p[ackages]: dump installed packages");
                    pw.println("    s[hared-users]: dump shared user IDs");
                    pw.println("    m[essages]: print collected runtime messages");
                    pw.println("    v[erifiers]: print package verifier info");
                    pw.println("    d[omain-preferred-apps]: print domains preferred apps");
                    pw.println("    i[ntent-filter-verifiers]|ifv: print intent filter verifier info");
                    pw.println("    version: print database version info");
                    pw.println("    write: write current settings now");
                    pw.println("    installs: details about install sessions");
                    pw.println("    check-permission <permission> <package> [<user>]: does pkg hold perm?");
                    pw.println("    dexopt: dump dexopt state");
                    pw.println("    <package.name>: info about given package");
                    return;
                } else if ("--checkin".equals(opt)) {
                    checkin = true;
                } else if ("-f".equals(opt)) {
                    dumpState.setOptionEnabled(1);
                } else {
                    pw.println("Unknown argument: " + opt + "; use -h for help");
                }
            }
        }
        if (opti < args.length) {
            String cmd = args[opti];
            opti++;
            if (PLATFORM_PACKAGE_NAME.equals(cmd) || cmd.contains(".")) {
                str = cmd;
                dumpState.setOptionEnabled(1);
            } else if ("check-permission".equals(cmd)) {
                if (opti >= args.length) {
                    pw.println("Error: check-permission missing permission argument");
                    return;
                }
                String perm = args[opti];
                opti++;
                if (opti >= args.length) {
                    pw.println("Error: check-permission missing package argument");
                    return;
                }
                String pkg = args[opti];
                opti++;
                user = UserHandle.getUserId(Binder.getCallingUid());
                if (opti < args.length) {
                    try {
                        user = Integer.parseInt(args[opti]);
                    } catch (NumberFormatException e) {
                        pw.println("Error: check-permission user argument is not a number: " + args[opti]);
                        return;
                    }
                }
                pw.println(checkPermission(perm, pkg, user));
                return;
            } else if ("l".equals(cmd) || "libraries".equals(cmd)) {
                dumpState.setDump(1);
            } else if ("f".equals(cmd) || "features".equals(cmd)) {
                dumpState.setDump(2);
            } else if ("r".equals(cmd) || "resolvers".equals(cmd)) {
                if (opti >= args.length) {
                    dumpState.setDump(60);
                } else {
                    while (opti < args.length) {
                        name = args[opti];
                        if ("a".equals(name) || "activity".equals(name)) {
                            dumpState.setDump(4);
                        } else if ("s".equals(name) || "service".equals(name)) {
                            dumpState.setDump(8);
                        } else if ("r".equals(name) || HwBroadcastRadarUtil.KEY_RECEIVER.equals(name)) {
                            dumpState.setDump(16);
                        } else if ("c".equals(name) || "content".equals(name)) {
                            dumpState.setDump(32);
                        } else {
                            pw.println("Error: unknown resolver table type: " + name);
                            return;
                        }
                        opti++;
                    }
                }
            } else if (TAG_PERMISSION.equals(cmd) || "permissions".equals(cmd)) {
                dumpState.setDump(64);
            } else if ("permission".equals(cmd)) {
                if (opti >= args.length) {
                    pw.println("Error: permission requires permission name");
                    return;
                }
                permissionNames = new ArraySet();
                while (opti < args.length) {
                    permissionNames.add(args[opti]);
                    opti++;
                }
                dumpState.setDump(448);
            } else if ("pref".equals(cmd) || "preferred".equals(cmd)) {
                dumpState.setDump(4096);
            } else if ("preferred-xml".equals(cmd)) {
                dumpState.setDump(8192);
                if (opti < args.length && "--full".equals(args[opti])) {
                    fullPreferred = true;
                    opti++;
                }
            } else if ("d".equals(cmd) || "domain-preferred-apps".equals(cmd)) {
                dumpState.setDump(DumpState.DUMP_DOMAIN_PREFERRED);
            } else if ("p".equals(cmd) || "packages".equals(cmd)) {
                dumpState.setDump(128);
            } else if ("s".equals(cmd) || "shared-users".equals(cmd)) {
                dumpState.setDump(256);
            } else if ("prov".equals(cmd) || "providers".equals(cmd)) {
                dumpState.setDump(1024);
            } else if ("m".equals(cmd) || "messages".equals(cmd)) {
                dumpState.setDump(512);
            } else if ("v".equals(cmd) || "verifiers".equals(cmd)) {
                dumpState.setDump(2048);
            } else if ("i".equals(cmd) || "ifv".equals(cmd) || "intent-filter-verifiers".equals(cmd)) {
                dumpState.setDump(131072);
            } else if ("version".equals(cmd)) {
                dumpState.setDump(DumpState.DUMP_VERSION);
            } else if ("k".equals(cmd) || "keysets".equals(cmd)) {
                dumpState.setDump(16384);
            } else if ("installs".equals(cmd)) {
                dumpState.setDump(65536);
            } else if ("frozen".equals(cmd)) {
                dumpState.setDump(DumpState.DUMP_FROZEN);
            } else if ("dexopt".equals(cmd)) {
                dumpState.setDump(DumpState.DUMP_DEXOPT);
            } else if ("write".equals(cmd)) {
                synchronized (this.mPackages) {
                    this.mSettings.writeLPr();
                    pw.println("Settings written.");
                }
                return;
            }
        }
        if (checkin) {
            pw.println("vers,1");
        }
        synchronized (this.mPackages) {
            ActivityIntentResolver activityIntentResolver;
            String str2;
            int i;
            BufferedReader bufferedReader;
            String line;
            if (dumpState.isDumping(DumpState.DUMP_VERSION) && str == null && !checkin) {
                if (dumpState.onTitlePrinted()) {
                    pw.println();
                }
                pw.println("Database versions:");
                this.mSettings.dumpVersionLPr(new IndentingPrintWriter(pw, "  "));
            }
            if (dumpState.isDumping(2048) && str == null) {
                if (!checkin) {
                    if (dumpState.onTitlePrinted()) {
                        pw.println();
                    }
                    pw.println("Verifiers:");
                    pw.print("  Required: ");
                    pw.print(this.mRequiredVerifierPackage);
                    pw.print(" (uid=");
                    pw.print(getPackageUid(this.mRequiredVerifierPackage, 268435456, 0));
                    pw.println(")");
                } else if (this.mRequiredVerifierPackage != null) {
                    pw.print("vrfy,");
                    pw.print(this.mRequiredVerifierPackage);
                    pw.print(",");
                    pw.println(getPackageUid(this.mRequiredVerifierPackage, 268435456, 0));
                }
            }
            if (dumpState.isDumping(131072) && str == null) {
                if (this.mIntentFilterVerifierComponent != null) {
                    String verifierPackageName = this.mIntentFilterVerifierComponent.getPackageName();
                    if (!checkin) {
                        if (dumpState.onTitlePrinted()) {
                            pw.println();
                        }
                        pw.println("Intent Filter Verifier:");
                        pw.print("  Using: ");
                        pw.print(verifierPackageName);
                        pw.print(" (uid=");
                        pw.print(getPackageUid(verifierPackageName, 268435456, 0));
                        pw.println(")");
                    } else if (verifierPackageName != null) {
                        pw.print("ifv,");
                        pw.print(verifierPackageName);
                        pw.print(",");
                        pw.println(getPackageUid(verifierPackageName, 268435456, 0));
                    }
                } else {
                    pw.println();
                    pw.println("No Intent Filter Verifier available!");
                }
            }
            if (dumpState.isDumping(1) && str == null) {
                boolean printedHeader = false;
                for (String name2 : this.mSharedLibraries.keySet()) {
                    SharedLibraryEntry ent = (SharedLibraryEntry) this.mSharedLibraries.get(name2);
                    if (checkin) {
                        pw.print("lib,");
                    } else {
                        if (!printedHeader) {
                            if (dumpState.onTitlePrinted()) {
                                pw.println();
                            }
                            pw.println("Libraries:");
                            printedHeader = true;
                        }
                        pw.print("  ");
                    }
                    pw.print(name2);
                    if (!checkin) {
                        pw.print(" -> ");
                    }
                    if (ent.path != null) {
                        if (checkin) {
                            pw.print(",jar,");
                            pw.print(ent.path);
                        } else {
                            pw.print("(jar) ");
                            pw.print(ent.path);
                        }
                    } else if (checkin) {
                        pw.print(",apk,");
                        pw.print(ent.apk);
                    } else {
                        pw.print("(apk) ");
                        pw.print(ent.apk);
                    }
                    pw.println();
                }
            }
            if (dumpState.isDumping(2) && str == null) {
                if (dumpState.onTitlePrinted()) {
                    pw.println();
                }
                if (!checkin) {
                    pw.println("Features:");
                }
                for (FeatureInfo feat : this.mAvailableFeatures.values()) {
                    if (checkin) {
                        pw.print("feat,");
                        pw.print(feat.name);
                        pw.print(",");
                        pw.println(feat.version);
                    } else {
                        pw.print("  ");
                        pw.print(feat.name);
                        if (feat.version > 0) {
                            pw.print(" version=");
                            pw.print(feat.version);
                        }
                        pw.println();
                    }
                }
            }
            if (!checkin && dumpState.isDumping(4)) {
                activityIntentResolver = this.mActivities;
                if (dumpState.getTitlePrinted()) {
                    str2 = "\nActivity Resolver Table:";
                } else {
                    str2 = "Activity Resolver Table:";
                }
                if (activityIntentResolver.dump(pw, str2, "  ", str, dumpState.isOptionEnabled(1), true)) {
                    dumpState.setTitlePrinted(true);
                }
            }
            if (!checkin && dumpState.isDumping(16)) {
                activityIntentResolver = this.mReceivers;
                if (dumpState.getTitlePrinted()) {
                    str2 = "\nReceiver Resolver Table:";
                } else {
                    str2 = "Receiver Resolver Table:";
                }
                if (activityIntentResolver.dump(pw, str2, "  ", str, dumpState.isOptionEnabled(1), true)) {
                    dumpState.setTitlePrinted(true);
                }
            }
            if (!checkin && dumpState.isDumping(8)) {
                ServiceIntentResolver serviceIntentResolver = this.mServices;
                if (dumpState.getTitlePrinted()) {
                    str2 = "\nService Resolver Table:";
                } else {
                    str2 = "Service Resolver Table:";
                }
                if (serviceIntentResolver.dump(pw, str2, "  ", str, dumpState.isOptionEnabled(1), true)) {
                    dumpState.setTitlePrinted(true);
                }
            }
            if (!checkin && dumpState.isDumping(32)) {
                ProviderIntentResolver providerIntentResolver = this.mProviders;
                if (dumpState.getTitlePrinted()) {
                    str2 = "\nProvider Resolver Table:";
                } else {
                    str2 = "Provider Resolver Table:";
                }
                if (providerIntentResolver.dump(pw, str2, "  ", str, dumpState.isOptionEnabled(1), true)) {
                    dumpState.setTitlePrinted(true);
                }
            }
            if (!checkin && dumpState.isDumping(4096)) {
                for (i = 0; i < this.mSettings.mPreferredActivities.size(); i++) {
                    PreferredIntentResolver pir = (PreferredIntentResolver) this.mSettings.mPreferredActivities.valueAt(i);
                    user = this.mSettings.mPreferredActivities.keyAt(i);
                    if (dumpState.getTitlePrinted()) {
                        str2 = "\nPreferred Activities User " + user + ":";
                    } else {
                        str2 = "Preferred Activities User " + user + ":";
                    }
                    if (pir.dump(pw, str2, "  ", str, true, false)) {
                        dumpState.setTitlePrinted(true);
                    }
                }
            }
            if (!checkin && dumpState.isDumping(8192)) {
                pw.flush();
                OutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fd));
                XmlSerializer serializer = new FastXmlSerializer();
                try {
                    serializer.setOutput(bufferedOutputStream, StandardCharsets.UTF_8.name());
                    serializer.startDocument(null, Boolean.valueOf(true));
                    serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                    this.mSettings.writePreferredActivitiesLPr(serializer, 0, fullPreferred);
                    serializer.endDocument();
                    serializer.flush();
                } catch (IllegalArgumentException e2) {
                    pw.println("Failed writing: " + e2);
                } catch (IllegalStateException e3) {
                    pw.println("Failed writing: " + e3);
                } catch (IOException e4) {
                    pw.println("Failed writing: " + e4);
                }
            }
            if (!checkin) {
                if (dumpState.isDumping(DumpState.DUMP_DOMAIN_PREFERRED) && str == null) {
                    pw.println();
                    if (this.mSettings.mPackages.size() == 0) {
                        pw.println("No applications!");
                        pw.println();
                    } else {
                        String prefix = "  ";
                        Collection<PackageSetting> allPackageSettings = this.mSettings.mPackages.values();
                        if (allPackageSettings.size() == 0) {
                            pw.println("No domain preferred apps!");
                            pw.println();
                        } else {
                            pw.println("App verification status:");
                            pw.println();
                            int count = 0;
                            for (PackageSetting ps : allPackageSettings) {
                                IntentFilterVerificationInfo ivi = ps.getIntentFilterVerificationInfo();
                                if (!(ivi == null || ivi.getPackageName() == null)) {
                                    pw.println("  Package: " + ivi.getPackageName());
                                    pw.println("  Domains: " + ivi.getDomainsString());
                                    pw.println("  Status:  " + ivi.getStatusString());
                                    pw.println();
                                    count++;
                                }
                            }
                            if (count == 0) {
                                pw.println("  No app verification established.");
                                pw.println();
                            }
                            for (int userId : sUserManager.getUserIds()) {
                                pw.println("App linkages for user " + userId + ":");
                                pw.println();
                                count = 0;
                                for (PackageSetting ps2 : allPackageSettings) {
                                    long status = ps2.getDomainVerificationStatusForUser(userId);
                                    if ((status >> 32) != 0) {
                                        pw.println("  Package: " + ps2.name);
                                        pw.println("  Domains: " + dumpDomainString(ps2.name));
                                        pw.println("  Status:  " + IntentFilterVerificationInfo.getStatusStringFromValue(status));
                                        pw.println();
                                        count++;
                                    }
                                }
                                if (count == 0) {
                                    pw.println("  No configured app linkages.");
                                    pw.println();
                                }
                            }
                        }
                    }
                }
            }
            if (!checkin && dumpState.isDumping(64)) {
                this.mSettings.dumpPermissionsLPr(pw, str, permissionNames, dumpState);
                if (str == null && permissionNames == null) {
                    for (int iperm = 0; iperm < this.mAppOpPermissionPackages.size(); iperm++) {
                        if (iperm == 0) {
                            if (dumpState.onTitlePrinted()) {
                                pw.println();
                            }
                            pw.println("AppOp Permissions:");
                        }
                        pw.print("  AppOp Permission ");
                        pw.print((String) this.mAppOpPermissionPackages.keyAt(iperm));
                        pw.println(":");
                        ArraySet<String> pkgs = (ArraySet) this.mAppOpPermissionPackages.valueAt(iperm);
                        for (int ipkg = 0; ipkg < pkgs.size(); ipkg++) {
                            pw.print("    ");
                            pw.println((String) pkgs.valueAt(ipkg));
                        }
                    }
                }
            }
            if (!checkin && dumpState.isDumping(1024)) {
                Provider p;
                boolean printedSomething = false;
                for (Provider p2 : this.mProviders.mProviders.values()) {
                    if (str == null || str.equals(p2.info.packageName)) {
                        if (!printedSomething) {
                            if (dumpState.onTitlePrinted()) {
                                pw.println();
                            }
                            pw.println("Registered ContentProviders:");
                            printedSomething = true;
                        }
                        pw.print("  ");
                        p2.printComponentShortName(pw);
                        pw.println(":");
                        pw.print("    ");
                        pw.println(p2.toString());
                    }
                }
                printedSomething = false;
                for (Entry<String, Provider> entry : this.mProvidersByAuthority.entrySet()) {
                    p2 = (Provider) entry.getValue();
                    if (str == null || str.equals(p2.info.packageName)) {
                        if (!printedSomething) {
                            if (dumpState.onTitlePrinted()) {
                                pw.println();
                            }
                            pw.println("ContentProvider Authorities:");
                            printedSomething = true;
                        }
                        pw.print("  [");
                        pw.print((String) entry.getKey());
                        pw.println("]:");
                        pw.print("    ");
                        pw.println(p2.toString());
                        if (!(p2.info == null || p2.info.applicationInfo == null)) {
                            String appInfo = p2.info.applicationInfo.toString();
                            pw.print("      applicationInfo=");
                            pw.println(appInfo);
                        }
                    }
                }
            }
            if (!checkin && dumpState.isDumping(16384)) {
                this.mSettings.mKeySetManagerService.dumpLPr(pw, str, dumpState);
            }
            if (dumpState.isDumping(128)) {
                this.mSettings.dumpPackagesLPr(pw, str, permissionNames, dumpState, checkin);
            }
            if (dumpState.isDumping(256)) {
                this.mSettings.dumpSharedUsersLPr(pw, str, permissionNames, dumpState, checkin);
            }
            if (!checkin && dumpState.isDumping(64) && str == null) {
                this.mSettings.dumpRestoredPermissionGrantsLPr(pw, dumpState);
            }
            if (!checkin && dumpState.isDumping(65536) && str == null) {
                if (dumpState.onTitlePrinted()) {
                    pw.println();
                }
                this.mInstallerService.dump(new IndentingPrintWriter(pw, "  ", 120));
            }
            if (!checkin && dumpState.isDumping(DumpState.DUMP_FROZEN) && str == null) {
                if (dumpState.onTitlePrinted()) {
                    pw.println();
                }
                IndentingPrintWriter indentingPrintWriter = new IndentingPrintWriter(pw, "  ", 120);
                indentingPrintWriter.println();
                indentingPrintWriter.println("Frozen packages:");
                indentingPrintWriter.increaseIndent();
                if (this.mFrozenPackages.size() == 0) {
                    indentingPrintWriter.println("(none)");
                } else {
                    for (i = 0; i < this.mFrozenPackages.size(); i++) {
                        indentingPrintWriter.println((String) this.mFrozenPackages.valueAt(i));
                    }
                }
                indentingPrintWriter.decreaseIndent();
            }
            if (!checkin && dumpState.isDumping(DumpState.DUMP_DEXOPT)) {
                if (dumpState.onTitlePrinted()) {
                    pw.println();
                }
                dumpDexoptStateLPr(pw, str);
            }
            if (!checkin && dumpState.isDumping(512) && str == null) {
                if (dumpState.onTitlePrinted()) {
                    pw.println();
                }
                this.mSettings.dumpReadMessagesLPr(pw, dumpState);
                pw.println();
                pw.println("Package warning messages:");
                in = null;
                try {
                    bufferedReader = new BufferedReader(new FileReader(getSettingsProblemFile()));
                    while (true) {
                        try {
                            line = bufferedReader.readLine();
                            if (line == null) {
                                break;
                            }
                            if (!line.contains("ignored: updated version")) {
                                pw.println(line);
                            }
                        } catch (IOException e5) {
                            autoCloseable = bufferedReader;
                        } catch (Throwable th2) {
                            th = th2;
                            in = bufferedReader;
                        }
                    }
                    IoUtils.closeQuietly(bufferedReader);
                } catch (IOException e6) {
                    IoUtils.closeQuietly(autoCloseable);
                    in = null;
                    try {
                        bufferedReader = new BufferedReader(new FileReader(getSettingsProblemFile()));
                        while (true) {
                            try {
                                line = bufferedReader.readLine();
                                if (line != null) {
                                    break;
                                }
                                if (!line.contains("ignored: updated version")) {
                                    pw.print("msg,");
                                    pw.println(line);
                                }
                            } catch (IOException e7) {
                                autoCloseable = bufferedReader;
                            } catch (Throwable th3) {
                                th = th3;
                                in = bufferedReader;
                            }
                        }
                        IoUtils.closeQuietly(bufferedReader);
                    } catch (IOException e8) {
                        IoUtils.closeQuietly(autoCloseable);
                    } catch (Throwable th4) {
                        th = th4;
                        IoUtils.closeQuietly(in);
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    IoUtils.closeQuietly(in);
                    throw th;
                }
            }
            if (checkin && dumpState.isDumping(512)) {
                in = null;
                bufferedReader = new BufferedReader(new FileReader(getSettingsProblemFile()));
                while (true) {
                    line = bufferedReader.readLine();
                    if (line != null) {
                        break;
                    }
                    if (!line.contains("ignored: updated version")) {
                        pw.print("msg,");
                        pw.println(line);
                    }
                }
                IoUtils.closeQuietly(bufferedReader);
            }
        }
    }

    private void dumpDexoptStateLPr(PrintWriter pw, String packageName) {
        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "  ", 120);
        ipw.println();
        ipw.println("Dexopt state:");
        ipw.increaseIndent();
        if (packageName != null) {
            Package targetPackage = (Package) this.mPackages.get(packageName);
            if (targetPackage != null) {
                Collection<Package> packages = Collections.singletonList(targetPackage);
            } else {
                ipw.println("Unable to find package: " + packageName);
                return;
            }
        }
        packages = this.mPackages.values();
        for (Package pkg : packages) {
            ipw.println("[" + pkg.packageName + "]");
            ipw.increaseIndent();
            this.mPackageDexOptimizer.dumpDexoptState(ipw, pkg);
            ipw.decreaseIndent();
        }
    }

    private String dumpDomainString(String packageName) {
        List<IntentFilterVerificationInfo> iviList = getIntentFilterVerifications(packageName).getList();
        List<IntentFilter> filters = getAllIntentFilters(packageName).getList();
        ArraySet<String> result = new ArraySet();
        if (iviList.size() > 0) {
            for (IntentFilterVerificationInfo ivi : iviList) {
                for (String host : ivi.getDomains()) {
                    result.add(host);
                }
            }
        }
        if (filters != null && filters.size() > 0) {
            for (IntentFilter filter : filters) {
                if (filter.hasCategory("android.intent.category.BROWSABLE") && (filter.hasDataScheme("http") || filter.hasDataScheme("https"))) {
                    result.addAll(filter.getHostsList());
                }
            }
        }
        StringBuilder sb = new StringBuilder(result.size() * 16);
        for (String domain : result) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(domain);
        }
        return sb.toString();
    }

    static String getEncryptKey() {
        try {
            String sdEncKey = SystemKeyStore.getInstance().retrieveKeyHexString(SD_ENCRYPTION_KEYSTORE_NAME);
            if (sdEncKey == null) {
                sdEncKey = SystemKeyStore.getInstance().generateNewKeyHexString(128, SD_ENCRYPTION_ALGORITHM, SD_ENCRYPTION_KEYSTORE_NAME);
                if (sdEncKey == null) {
                    Slog.e(TAG, "Failed to create encryption keys");
                    return null;
                }
            }
            return sdEncKey;
        } catch (NoSuchAlgorithmException nsae) {
            Slog.e(TAG, "Failed to create encryption keys with exception: " + nsae);
            return null;
        } catch (IOException ioe) {
            Slog.e(TAG, "Failed to retrieve encryption keys with exception: " + ioe);
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateExternalMediaStatus(final boolean mediaStatus, final boolean reportStatus) {
        int i = 1;
        int callingUid = Binder.getCallingUid();
        if (callingUid == 0 || callingUid == 1000) {
            synchronized (this.mPackages) {
                Log.i(TAG, "Updating external media status from " + (this.mMediaMounted ? "mounted" : "unmounted") + " to " + (mediaStatus ? "mounted" : "unmounted"));
                if (mediaStatus == this.mMediaMounted) {
                    PackageHandler packageHandler = this.mHandler;
                    if (!reportStatus) {
                        i = 0;
                    }
                    this.mHandler.sendMessage(packageHandler.obtainMessage(12, i, -1));
                    return;
                }
                this.mMediaMounted = mediaStatus;
            }
        } else {
            throw new SecurityException("Media status can only be updated by the system");
        }
    }

    public void scanAvailableAsecs() {
        updateExternalMediaStatusInner(true, false, false);
    }

    private void updateExternalMediaStatusInner(boolean isMounted, boolean reportStatus, boolean externalStorage) {
        ArrayMap<AsecInstallArgs, String> processCids = new ArrayMap();
        int[] uidArr = EmptyArray.INT;
        String[] list = PackageHelper.getSecureContainerList();
        if (ArrayUtils.isEmpty(list)) {
            Log.i(TAG, "No secure containers found");
        } else {
            synchronized (this.mPackages) {
                for (String cid : list) {
                    if (!PackageInstallerService.isStageName(cid)) {
                        String pkgName = getAsecPackageName(cid);
                        if (pkgName == null) {
                            Slog.i(TAG, "Found stale container " + cid + " with no package name");
                        } else {
                            PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(pkgName);
                            if (ps == null) {
                                Slog.i(TAG, "Found stale container " + cid + " with no matching settings");
                            } else {
                                if (externalStorage && !isMounted) {
                                    if (!isExternal(ps)) {
                                        continue;
                                    }
                                }
                                try {
                                    AsecInstallArgs args = new AsecInstallArgs(cid, InstructionSets.getAppDexInstructionSets(ps), ps.isForwardLocked());
                                    if (ps.codePathString == null || args == null || args.getCodePath() == null || !ps.codePathString.startsWith(args.getCodePath())) {
                                        Slog.i(TAG, "Found stale container " + cid + ": expected codePath=" + ps.codePathString);
                                    } else {
                                        processCids.put(args, ps.codePathString);
                                        int uid = ps.appId;
                                        if (uid != -1) {
                                            uidArr = ArrayUtils.appendInt(uidArr, uid);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.w(TAG, "avoid exception is " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
            Arrays.sort(uidArr);
        }
        if (isMounted) {
            loadMediaPackages(processCids, uidArr, externalStorage);
            startCleaningPackages();
            this.mInstallerService.onSecureContainersAvailable();
            return;
        }
        unloadMediaPackages(processCids, uidArr, reportStatus);
    }

    private void sendResourcesChangedBroadcast(boolean mediaStatus, boolean replacing, ArrayList<ApplicationInfo> infos, IIntentReceiver finishedReceiver) {
        int size = infos.size();
        String[] packageNames = new String[size];
        int[] packageUids = new int[size];
        for (int i = 0; i < size; i++) {
            ApplicationInfo info = (ApplicationInfo) infos.get(i);
            packageNames[i] = info.packageName;
            packageUids[i] = info.uid;
        }
        sendResourcesChangedBroadcast(mediaStatus, replacing, packageNames, packageUids, finishedReceiver);
    }

    private void sendResourcesChangedBroadcast(boolean mediaStatus, boolean replacing, ArrayList<String> pkgList, int[] uidArr, IIntentReceiver finishedReceiver) {
        sendResourcesChangedBroadcast(mediaStatus, replacing, (String[]) pkgList.toArray(new String[pkgList.size()]), uidArr, finishedReceiver);
    }

    private void sendResourcesChangedBroadcast(boolean mediaStatus, boolean replacing, String[] pkgList, int[] uidArr, IIntentReceiver finishedReceiver) {
        if (pkgList.length > 0) {
            String action;
            Bundle extras = new Bundle();
            extras.putStringArray("android.intent.extra.changed_package_list", pkgList);
            if (uidArr != null) {
                extras.putIntArray("android.intent.extra.changed_uid_list", uidArr);
            }
            if (replacing) {
                extras.putBoolean("android.intent.extra.REPLACING", replacing);
            }
            if (mediaStatus) {
                action = "android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE";
            } else {
                action = "android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE";
            }
            sendPackageBroadcast(action, null, extras, 0, null, finishedReceiver, null);
        }
    }

    private void loadMediaPackages(ArrayMap<AsecInstallArgs, String> processCids, int[] uidArr, boolean externalStorage) {
        ArrayList<String> pkgList = new ArrayList();
        for (AsecInstallArgs args : processCids.keySet()) {
            String codePath = (String) processCids.get(args);
            int retCode = -18;
            if (this.mCustPms != null && this.mCustPms.isSdInstallEnabled()) {
                PackageHelper.unMountSdDir(args.cid);
            }
            if (args.doPreInstall(1) != 1) {
                Slog.e(TAG, "Failed to mount cid : " + args.cid + " when installing from sdcard");
                Log.w(TAG, "Container " + args.cid + " is stale, retCode=" + -18);
            } else {
                if (codePath != null) {
                    if (codePath.startsWith(args.getCodePath())) {
                        int parseFlags = this.mDefParseFlags;
                        if (args.isExternalAsec()) {
                            parseFlags |= 32;
                        }
                        if (args.isFwdLocked()) {
                            parseFlags |= 16;
                        }
                        synchronized (this.mInstallLock) {
                            Package pkg = null;
                            try {
                                pkg = scanPackageTracedLI(new File(codePath), parseFlags, (int) DumpState.DUMP_DOMAIN_PREFERRED, 0, null);
                            } catch (PackageManagerException e) {
                                Slog.w(TAG, "Failed to scan " + codePath + ": " + e.getMessage());
                            }
                            if (pkg != null) {
                                synchronized (this.mPackages) {
                                    retCode = 1;
                                    pkgList.add(pkg.packageName);
                                    args.doPostInstall(1, pkg.applicationInfo.uid);
                                }
                            } else {
                                Slog.i(TAG, "Failed to install pkg from  " + codePath + " from sdcard");
                            }
                            try {
                            } catch (Throwable th) {
                                if (retCode != 1) {
                                    Log.w(TAG, "Container " + args.cid + " is stale, retCode=" + retCode);
                                }
                            }
                        }
                        if (retCode != 1) {
                            Log.w(TAG, "Container " + args.cid + " is stale, retCode=" + retCode);
                        }
                    }
                }
                Slog.e(TAG, "Container " + args.cid + " cachepath " + args.getCodePath() + " does not match one in settings " + codePath);
                Log.w(TAG, "Container " + args.cid + " is stale, retCode=" + -18);
            }
        }
        synchronized (this.mPackages) {
            VersionInfo ver;
            String volumeUuid;
            if (externalStorage) {
                ver = this.mSettings.getExternalVersion();
            } else {
                ver = this.mSettings.getInternalVersion();
            }
            if (externalStorage) {
                volumeUuid = "primary_physical";
            } else {
                volumeUuid = StorageManager.UUID_PRIVATE_INTERNAL;
            }
            int updateFlags = 1;
            if (!(ver == null || ver.sdkVersion == this.mSdkVersion)) {
                logCriticalInfo(4, "Platform changed from " + ver.sdkVersion + " to " + this.mSdkVersion + "; regranting permissions for external");
                updateFlags = 7;
            }
            if (this.mCustPms != null && this.mCustPms.isSdInstallEnabled()) {
                updateFlags |= 6;
            }
            updatePermissionsLPw(null, null, volumeUuid, updateFlags);
            if (ver != null) {
                ver.forceCurrent();
            }
            this.mSettings.writeLPr();
        }
        if (pkgList.size() > 0) {
            sendResourcesChangedBroadcast(true, false, (ArrayList) pkgList, uidArr, null);
        }
    }

    private void unloadAllContainers(Set<AsecInstallArgs> cidArgs) {
        for (AsecInstallArgs arg : cidArgs) {
            synchronized (this.mInstallLock) {
                arg.doPostDeleteLI(false);
            }
        }
    }

    private void unloadMediaPackages(ArrayMap<AsecInstallArgs, String> processCids, int[] uidArr, boolean reportStatus) {
        Throwable th;
        Throwable th2;
        ArrayList pkgList = new ArrayList();
        ArrayList<AsecInstallArgs> failedList = new ArrayList();
        Set<AsecInstallArgs> keys = processCids.keySet();
        for (AsecInstallArgs args : keys) {
            String pkgName = args.getPackageName();
            PackageRemovedInfo outInfo = new PackageRemovedInfo();
            synchronized (this.mInstallLock) {
                Throwable th3 = null;
                PackageFreezer packageFreezer = null;
                try {
                    packageFreezer = freezePackageForDelete(pkgName, 1, "unloadMediaPackages");
                    boolean res = deletePackageLIF(pkgName, null, false, null, 1, outInfo, false, null);
                    if (packageFreezer != null) {
                        try {
                            packageFreezer.close();
                        } catch (Throwable th4) {
                            th3 = th4;
                        }
                    }
                    if (th3 != null) {
                        throw th3;
                    } else {
                        if (res) {
                            pkgList.add(pkgName);
                        } else {
                            Slog.e(TAG, "Failed to delete pkg from sdcard : " + pkgName);
                            failedList.add(args);
                        }
                    }
                } catch (Throwable th22) {
                    Throwable th5 = th22;
                    th22 = th;
                    th = th5;
                }
            }
        }
        synchronized (this.mPackages) {
            this.mSettings.writeLPr();
        }
        if (pkgList.size() > 0) {
            final boolean z = reportStatus;
            final Set<AsecInstallArgs> set = keys;
            sendResourcesChangedBroadcast(false, false, pkgList, uidArr, new IIntentReceiver.Stub() {
                public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) throws RemoteException {
                    PackageManagerService.this.mHandler.sendMessage(PackageManagerService.this.mHandler.obtainMessage(12, z ? 1 : 0, 1, set));
                }
            });
            return;
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(12, reportStatus ? 1 : 0, -1, keys));
        return;
        if (packageFreezer != null) {
            try {
                packageFreezer.close();
            } catch (Throwable th6) {
                if (th22 == null) {
                    th22 = th6;
                } else if (th22 != th6) {
                    th22.addSuppressed(th6);
                }
            }
        }
        if (th22 != null) {
            throw th22;
        }
        throw th;
    }

    private void loadPrivatePackages(final VolumeInfo vol) {
        this.mHandler.post(new Runnable() {
            public void run() {
                PackageManagerService.this.loadPrivatePackagesInner(vol);
            }
        });
    }

    private void loadPrivatePackagesInner(VolumeInfo vol) {
        String volumeUuid = vol.fsUuid;
        if (TextUtils.isEmpty(volumeUuid)) {
            Slog.e(TAG, "Loading internal storage is probably a mistake; ignoring");
            return;
        }
        ArrayList<PackageFreezer> freezers = new ArrayList();
        ArrayList<ApplicationInfo> loaded = new ArrayList();
        int parseFlags = this.mDefParseFlags | 32;
        synchronized (this.mPackages) {
            VersionInfo ver = this.mSettings.findOrCreateVersion(volumeUuid);
            List<PackageSetting> packages = this.mSettings.getVolumePackagesLPr(volumeUuid);
        }
        for (PackageSetting ps : packages) {
            freezers.add(freezePackage(ps.name, "loadPrivatePackagesInner"));
            synchronized (this.mInstallLock) {
                try {
                    loaded.add(scanPackageTracedLI(ps.codePath, parseFlags, 16384, 0, null).applicationInfo);
                } catch (PackageManagerException e) {
                    Slog.w(TAG, "Failed to scan " + ps.codePath + ": " + e.getMessage());
                }
                if (!Build.FINGERPRINT.equals(ver.fingerprint)) {
                    clearAppDataLIF(ps.pkg, -1, 515);
                }
            }
        }
        StorageManager sm = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        UserManager um = (UserManager) this.mContext.getSystemService(UserManager.class);
        UserManagerInternal umInternal = getUserManagerInternal();
        for (UserInfo user : um.getUsers()) {
            int flags;
            if (umInternal.isUserUnlockingOrUnlocked(user.id)) {
                flags = 3;
            } else {
                if (umInternal.isUserRunning(user.id)) {
                    flags = 1;
                } else {
                    continue;
                }
            }
            try {
                sm.prepareUserStorage(volumeUuid, user.id, user.serialNumber, flags);
                synchronized (this.mInstallLock) {
                    reconcileAppsDataLI(volumeUuid, user.id, flags);
                }
            } catch (IllegalStateException e2) {
                Slog.w(TAG, "Failed to prepare storage: " + e2);
            }
        }
        synchronized (this.mPackages) {
            int updateFlags = 1;
            if (ver.sdkVersion != this.mSdkVersion) {
                logCriticalInfo(4, "Platform changed from " + ver.sdkVersion + " to " + this.mSdkVersion + "; regranting permissions for " + volumeUuid);
                updateFlags = 7;
            }
            updatePermissionsLPw(null, null, volumeUuid, updateFlags);
            ver.forceCurrent();
            this.mSettings.writeLPr();
        }
        for (PackageFreezer freezer : freezers) {
            freezer.close();
        }
        sendResourcesChangedBroadcast(true, false, loaded, null);
    }

    private void unloadPrivatePackages(final VolumeInfo vol) {
        this.mHandler.post(new Runnable() {
            public void run() {
                PackageManagerService.this.unloadPrivatePackagesInner(vol);
            }
        });
    }

    private void unloadPrivatePackagesInner(VolumeInfo vol) {
        PackageFreezer packageFreezer;
        Throwable th;
        Throwable th2;
        String volumeUuid = vol.fsUuid;
        if (TextUtils.isEmpty(volumeUuid)) {
            Slog.e(TAG, "Unloading internal storage is probably a mistake; ignoring");
            return;
        }
        ArrayList<ApplicationInfo> unloaded = new ArrayList();
        synchronized (this.mInstallLock) {
            synchronized (this.mPackages) {
                for (PackageSetting ps : this.mSettings.getVolumePackagesLPr(volumeUuid)) {
                    if (ps.pkg != null) {
                        ApplicationInfo info = ps.pkg.applicationInfo;
                        PackageRemovedInfo outInfo = new PackageRemovedInfo();
                        Throwable th3 = null;
                        packageFreezer = null;
                        try {
                            packageFreezer = freezePackageForDelete(ps.name, 1, "unloadPrivatePackagesInner");
                            if (deletePackageLIF(ps.name, null, false, null, 1, outInfo, false, null)) {
                                unloaded.add(info);
                            } else {
                                Slog.w(TAG, "Failed to unload " + ps.codePath);
                            }
                            if (packageFreezer != null) {
                                try {
                                    packageFreezer.close();
                                } catch (Throwable th4) {
                                    th3 = th4;
                                }
                            }
                            if (th3 != null) {
                                throw th3;
                            } else {
                                AttributeCache.instance().removePackage(ps.name);
                            }
                        } catch (Throwable th22) {
                            Throwable th5 = th22;
                            th22 = th;
                            th = th5;
                        }
                    }
                }
                this.mSettings.writeLPr();
            }
        }
        sendResourcesChangedBroadcast(false, false, unloaded, null);
        ResourcesManager.getInstance().invalidatePath(vol.getPath().getAbsolutePath());
        for (int i = 0; i < 3; i++) {
            System.gc();
            System.runFinalization();
        }
        return;
        if (packageFreezer != null) {
            try {
                packageFreezer.close();
            } catch (Throwable th6) {
                if (th22 == null) {
                    th22 = th6;
                } else if (th22 != th6) {
                    th22.addSuppressed(th6);
                }
            }
        }
        if (th22 != null) {
            throw th22;
        }
        throw th;
    }

    void prepareUserData(int userId, int userSerial, int flags) {
        synchronized (this.mInstallLock) {
            for (VolumeInfo vol : ((StorageManager) this.mContext.getSystemService(StorageManager.class)).getWritablePrivateVolumes()) {
                prepareUserDataLI(vol.getFsUuid(), userId, userSerial, flags, true);
            }
        }
    }

    private void prepareUserDataLI(String volumeUuid, int userId, int userSerial, int flags, boolean allowRecover) {
        try {
            ((StorageManager) this.mContext.getSystemService(StorageManager.class)).prepareUserStorage(volumeUuid, userId, userSerial, flags);
            if (!((flags & 1) == 0 || this.mOnlyCore)) {
                UserManagerService.enforceSerialNumber(Environment.getDataUserDeDirectory(volumeUuid, userId), userSerial);
                if (Objects.equals(volumeUuid, StorageManager.UUID_PRIVATE_INTERNAL)) {
                    UserManagerService.enforceSerialNumber(Environment.getDataSystemDeDirectory(userId), userSerial);
                }
            }
            if (!((flags & 2) == 0 || this.mOnlyCore)) {
                UserManagerService.enforceSerialNumber(Environment.getDataUserCeDirectory(volumeUuid, userId), userSerial);
                if (Objects.equals(volumeUuid, StorageManager.UUID_PRIVATE_INTERNAL)) {
                    UserManagerService.enforceSerialNumber(Environment.getDataSystemCeDirectory(userId), userSerial);
                }
            }
            synchronized (this.mInstallLock) {
                this.mInstaller.createUserData(volumeUuid, userId, userSerial, flags);
            }
            resetRebootTimes();
        } catch (Exception e) {
            logCriticalInfo(5, "Destroying user " + userId + " on volume " + volumeUuid + " because we failed to prepare: " + e);
            tryToReboot();
        }
    }

    void destroyUserData(int userId, int flags) {
        synchronized (this.mInstallLock) {
            for (VolumeInfo vol : ((StorageManager) this.mContext.getSystemService(StorageManager.class)).getWritablePrivateVolumes()) {
                destroyUserDataLI(vol.getFsUuid(), userId, flags);
            }
        }
    }

    private void destroyUserDataLI(String volumeUuid, int userId, int flags) {
        StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        try {
            this.mInstaller.destroyUserData(volumeUuid, userId, flags);
            if (Objects.equals(volumeUuid, StorageManager.UUID_PRIVATE_INTERNAL)) {
                if ((flags & 1) != 0) {
                    FileUtils.deleteContentsAndDir(Environment.getUserSystemDirectory(userId));
                    FileUtils.deleteContentsAndDir(Environment.getDataSystemDeDirectory(userId));
                }
                if ((flags & 2) != 0) {
                    FileUtils.deleteContentsAndDir(Environment.getDataSystemCeDirectory(userId));
                }
            }
            storage.destroyUserStorage(volumeUuid, userId, flags);
        } catch (Exception e) {
            logCriticalInfo(5, "Failed to destroy user " + userId + " on volume " + volumeUuid + ": " + e);
        }
    }

    private void reconcileUsers(String volumeUuid) {
        List<File> files = new ArrayList();
        Collections.addAll(files, FileUtils.listFilesOrEmpty(Environment.getDataUserDeDirectory(volumeUuid)));
        Collections.addAll(files, FileUtils.listFilesOrEmpty(Environment.getDataUserCeDirectory(volumeUuid)));
        Collections.addAll(files, FileUtils.listFilesOrEmpty(Environment.getDataSystemDeDirectory()));
        Collections.addAll(files, FileUtils.listFilesOrEmpty(Environment.getDataSystemCeDirectory()));
        for (File file : files) {
            if (file.isDirectory()) {
                try {
                    int userId = Integer.parseInt(file.getName());
                    UserInfo info = sUserManager.getUserInfo(userId);
                    boolean destroyUser = false;
                    if (info == null) {
                        logCriticalInfo(5, "Destroying user directory " + file + " because no matching user was found");
                        destroyUser = true;
                    } else if (!this.mOnlyCore) {
                        try {
                            UserManagerService.enforceSerialNumber(file, info.serialNumber);
                        } catch (IOException e) {
                            logCriticalInfo(5, "Destroying user directory " + file + " because we failed to enforce serial number: " + e);
                            destroyUser = true;
                        }
                    }
                    if (destroyUser) {
                        synchronized (this.mInstallLock) {
                            destroyUserDataLI(volumeUuid, userId, 3);
                        }
                    } else {
                        continue;
                    }
                } catch (NumberFormatException e2) {
                    Slog.w(TAG, "Invalid user directory " + file);
                }
            }
        }
    }

    private void assertPackageKnown(String volumeUuid, String packageName) throws PackageManagerException {
        synchronized (this.mPackages) {
            PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(packageName);
            if (ps == null) {
                throw new PackageManagerException("Package " + packageName + " is unknown");
            } else if (TextUtils.equals(volumeUuid, ps.volumeUuid)) {
            } else {
                throw new PackageManagerException("Package " + packageName + " found on unknown volume " + volumeUuid + "; expected volume " + ps.volumeUuid);
            }
        }
    }

    private void assertPackageKnownAndInstalled(String volumeUuid, String packageName, int userId) throws PackageManagerException {
        synchronized (this.mPackages) {
            PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(packageName);
            if (ps == null) {
                throw new PackageManagerException("Package " + packageName + " is unknown");
            } else if (!TextUtils.equals(volumeUuid, ps.volumeUuid)) {
                throw new PackageManagerException("Package " + packageName + " found on unknown volume " + volumeUuid + "; expected volume " + ps.volumeUuid);
            } else if (ps.getInstalled(userId)) {
            } else {
                throw new PackageManagerException("Package " + packageName + " not installed for user " + userId);
            }
        }
    }

    private void reconcileApps(String volumeUuid) {
        for (File file : FileUtils.listFilesOrEmpty(Environment.getDataAppDirectory(volumeUuid))) {
            boolean isPackage = (PackageParser.isApkFile(file) || file.isDirectory()) ? !PackageInstallerService.isStageName(file.getName()) : false;
            if (isPackage) {
                try {
                    assertPackageKnown(volumeUuid, PackageParser.parsePackageLite(file, 4).packageName);
                } catch (Exception e) {
                    logCriticalInfo(5, "Destroying " + file + " due to: " + e);
                    synchronized (this.mInstallLock) {
                        removeCodePathLI(file);
                    }
                }
            }
        }
    }

    void reconcileAppsData(int userId, int flags) {
        for (VolumeInfo vol : ((StorageManager) this.mContext.getSystemService(StorageManager.class)).getWritablePrivateVolumes()) {
            String volumeUuid = vol.getFsUuid();
            synchronized (this.mInstallLock) {
                reconcileAppsDataLI(volumeUuid, userId, flags);
            }
        }
    }

    private void reconcileAppsDataLI(String volumeUuid, int userId, int flags) {
        String packageName;
        Slog.v(TAG, "reconcileAppsData for " + volumeUuid + " u" + userId + " 0x" + Integer.toHexString(flags));
        File ceDir = Environment.getDataUserCeDirectory(volumeUuid, userId);
        File deDir = Environment.getDataUserDeDirectory(volumeUuid, userId);
        boolean z = false;
        if ((flags & 2) != 0) {
            if (!StorageManager.isFileEncryptedNativeOrEmulated() || StorageManager.isUserKeyUnlocked(userId)) {
                z = SELinuxMMAC.isRestoreconNeeded(ceDir);
                for (File file : FileUtils.listFilesOrEmpty(ceDir)) {
                    packageName = file.getName();
                    if (!this.mIsPreNUpgrade || this.mCustPms == null || this.mCustPms.isListedApp(packageName) != 1) {
                        try {
                            assertPackageKnownAndInstalled(volumeUuid, packageName, userId);
                        } catch (PackageManagerException e) {
                            logCriticalInfo(5, "Destroying " + file + " due to: " + e);
                            try {
                                this.mInstaller.destroyAppData(volumeUuid, packageName, userId, 2, 0);
                            } catch (InstallerException e2) {
                                logCriticalInfo(5, "Failed to destroy: " + e2);
                            }
                        }
                    }
                }
            } else {
                throw new RuntimeException("Yikes, someone asked us to reconcile CE storage while " + userId + " was still locked; this would have caused massive data loss!");
            }
        }
        if ((flags & 1) != 0) {
            z |= SELinuxMMAC.isRestoreconNeeded(deDir);
            for (File file2 : FileUtils.listFilesOrEmpty(deDir)) {
                packageName = file2.getName();
                try {
                    assertPackageKnownAndInstalled(volumeUuid, packageName, userId);
                } catch (PackageManagerException e3) {
                    logCriticalInfo(5, "Destroying " + file2 + " due to: " + e3);
                    try {
                        this.mInstaller.destroyAppData(volumeUuid, packageName, userId, 1, 0);
                    } catch (InstallerException e22) {
                        logCriticalInfo(5, "Failed to destroy: " + e22);
                    }
                }
            }
        }
        synchronized (this.mPackages) {
        }
        int preparedCount = 0;
        for (PackageSetting ps : this.mSettings.getVolumePackagesLPr(volumeUuid)) {
            packageName = ps.name;
            if (ps.pkg == null) {
                Slog.w(TAG, "Odd, missing scanned package " + packageName);
            } else if (ps.getInstalled(userId)) {
                prepareAppDataLIF(ps.pkg, userId, flags, z);
                if (maybeMigrateAppDataLIF(ps.pkg, userId)) {
                    prepareAppDataLIF(ps.pkg, userId, flags, z);
                }
                preparedCount++;
            }
        }
        if (z) {
            if ((flags & 2) != 0) {
                SELinuxMMAC.setRestoreconDone(ceDir);
            }
            if ((flags & 1) != 0) {
                SELinuxMMAC.setRestoreconDone(deDir);
            }
        }
        Slog.v(TAG, "reconcileAppsData finished " + preparedCount + " packages; restoreconNeeded was " + z);
    }

    private void prepareAppDataAfterInstallLIF(Package pkg) {
        synchronized (this.mPackages) {
            PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(pkg.packageName);
            this.mSettings.writeKernelMappingLPr(ps);
        }
        UserManager um = (UserManager) this.mContext.getSystemService(UserManager.class);
        UserManagerInternal umInternal = getUserManagerInternal();
        for (UserInfo user : um.getUsers()) {
            int flags;
            if (umInternal.isUserUnlockingOrUnlocked(user.id)) {
                flags = 3;
            } else if (umInternal.isUserRunning(user.id)) {
                flags = 1;
            }
            if (ps.getInstalled(user.id)) {
                prepareAppDataLIF(pkg, user.id, flags, true);
            }
        }
    }

    private void prepareAppDataLIF(Package pkg, int userId, int flags, boolean restoreconNeeded) {
        if (pkg == null) {
            Slog.wtf(TAG, "Package was null!", new Throwable());
            return;
        }
        prepareAppDataLeafLIF(pkg, userId, flags, restoreconNeeded);
        int childCount = pkg.childPackages != null ? pkg.childPackages.size() : 0;
        for (int i = 0; i < childCount; i++) {
            prepareAppDataLeafLIF((Package) pkg.childPackages.get(i), userId, flags, restoreconNeeded);
        }
    }

    private void prepareAppDataLeafLIF(Package pkg, int userId, int flags, boolean restoreconNeeded) {
        String volumeUuid = pkg.volumeUuid;
        String packageName = pkg.packageName;
        ApplicationInfo app = pkg.applicationInfo;
        int appId = UserHandle.getAppId(app.uid);
        Preconditions.checkNotNull(app.seinfo);
        try {
            this.mInstaller.createAppDataHwFixup(volumeUuid, packageName, userId, flags, appId, app.seinfo, app.targetSdkVersion);
        } catch (InstallerException e) {
            logCriticalInfo(6, "Failed to create app data for " + packageName + ": " + e);
        }
        if (restoreconNeeded) {
            try {
                this.mInstaller.restoreconAppData(volumeUuid, packageName, userId, flags, appId, app.seinfo);
            } catch (InstallerException e2) {
                Slog.e(TAG, "Failed to restorecon for " + packageName + ": " + e2);
            }
        }
        if ((flags & 2) != 0) {
            try {
                long ceDataInode = this.mInstaller.getAppDataInode(volumeUuid, packageName, userId, 2);
                synchronized (this.mPackages) {
                    PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(packageName);
                    if (ps != null) {
                        ps.setCeDataInode(ceDataInode, userId);
                    }
                }
            } catch (InstallerException e22) {
                Slog.e(TAG, "Failed to find inode for " + packageName + ": " + e22);
            }
        }
        prepareAppDataContentsLeafLIF(pkg, userId, flags);
    }

    private void prepareAppDataContentsLIF(Package pkg, int userId, int flags) {
        if (pkg == null) {
            Slog.wtf(TAG, "Package was null!", new Throwable());
            return;
        }
        prepareAppDataContentsLeafLIF(pkg, userId, flags);
        int childCount = pkg.childPackages != null ? pkg.childPackages.size() : 0;
        for (int i = 0; i < childCount; i++) {
            prepareAppDataContentsLeafLIF((Package) pkg.childPackages.get(i), userId, flags);
        }
    }

    private void prepareAppDataContentsLeafLIF(Package pkg, int userId, int flags) {
        String volumeUuid = pkg.volumeUuid;
        String packageName = pkg.packageName;
        ApplicationInfo app = pkg.applicationInfo;
        if ((flags & 2) != 0 && app.primaryCpuAbi != null && !VMRuntime.is64BitAbi(app.primaryCpuAbi)) {
            try {
                this.mInstaller.linkNativeLibraryDirectory(volumeUuid, packageName, app.nativeLibraryDir, userId);
            } catch (InstallerException e) {
                Slog.e(TAG, "Failed to link native for " + packageName + ": " + e);
            }
        }
    }

    private boolean maybeMigrateAppDataLIF(Package pkg, int userId) {
        if (!pkg.isSystemApp() || StorageManager.isFileEncryptedNativeOrEmulated()) {
            return false;
        }
        try {
            this.mInstaller.migrateAppData(pkg.volumeUuid, pkg.packageName, userId, pkg.applicationInfo.isDefaultToDeviceProtectedStorage() ? 1 : 2);
        } catch (InstallerException e) {
            logCriticalInfo(5, "Failed to migrate " + pkg.packageName + ": " + e.getMessage());
        }
        return true;
    }

    public PackageFreezer freezePackage(String packageName, String killReason) {
        return freezePackage(packageName, -1, killReason);
    }

    public PackageFreezer freezePackage(String packageName, int userId, String killReason) {
        return new PackageFreezer(packageName, userId, killReason);
    }

    public PackageFreezer freezePackageForInstall(String packageName, int installFlags, String killReason) {
        return freezePackageForInstall(packageName, -1, installFlags, killReason);
    }

    public PackageFreezer freezePackageForInstall(String packageName, int userId, int installFlags, String killReason) {
        if ((installFlags & 4096) != 0) {
            return new PackageFreezer();
        }
        return freezePackage(packageName, userId, killReason);
    }

    public PackageFreezer freezePackageForDelete(String packageName, int deleteFlags, String killReason) {
        return freezePackageForDelete(packageName, -1, deleteFlags, killReason);
    }

    public PackageFreezer freezePackageForDelete(String packageName, int userId, int deleteFlags, String killReason) {
        if ((deleteFlags & 8) != 0) {
            return new PackageFreezer();
        }
        return freezePackage(packageName, userId, killReason);
    }

    private void checkPackageFrozen(String packageName) {
        synchronized (this.mPackages) {
            if (!this.mFrozenPackages.contains(packageName)) {
                Slog.wtf(TAG, "Expected " + packageName + " to be frozen!", new Throwable());
            }
        }
    }

    public int movePackage(String packageName, String volumeUuid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MOVE_PACKAGE", null);
        final UserHandle user = new UserHandle(UserHandle.getCallingUserId());
        final int moveId = this.mNextMoveId.getAndIncrement();
        final String str = packageName;
        final String str2 = volumeUuid;
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    PackageManagerService.this.movePackageInternal(str, str2, moveId, user);
                } catch (PackageManagerException e) {
                    Slog.w(PackageManagerService.TAG, "Failed to move " + str, e);
                    PackageManagerService.this.mMoveCallbacks.notifyStatusChanged(moveId, -6);
                }
            }
        });
        return moveId;
    }

    private void movePackageInternal(String packageName, String volumeUuid, int moveId, UserHandle user) throws PackageManagerException {
        boolean currentAsec;
        String currentVolumeUuid;
        File file;
        String label;
        int installFlags;
        boolean moveCompleteApp;
        File measurePath;
        long sizeBytes;
        StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        PackageManager pm = this.mContext.getPackageManager();
        synchronized (this.mPackages) {
            Package pkg = (Package) this.mPackages.get(packageName);
            PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(packageName);
            if (pkg == null || ps == null) {
                throw new PackageManagerException(-2, "Missing package");
            } else if (pkg.applicationInfo.isSystemApp()) {
                throw new PackageManagerException(-3, "Cannot move system application");
            } else {
                if (pkg.applicationInfo.isExternalAsec()) {
                    currentAsec = true;
                    currentVolumeUuid = "primary_physical";
                } else if (pkg.applicationInfo.isForwardLocked()) {
                    currentAsec = true;
                    currentVolumeUuid = "forward_locked";
                } else {
                    currentAsec = false;
                    currentVolumeUuid = ps.volumeUuid;
                    file = new File(pkg.codePath);
                    file = new File(file, "oat");
                    if (!(file.isDirectory() && file.isDirectory())) {
                        throw new PackageManagerException(-6, "Move only supported for modern cluster style installs");
                    }
                }
                if (Objects.equals(currentVolumeUuid, volumeUuid)) {
                    throw new PackageManagerException(-6, "Package already moved to " + volumeUuid);
                } else if (pkg.applicationInfo.isInternal() && isPackageDeviceAdminOnAnyUser(packageName)) {
                    throw new PackageManagerException(-8, "Device admin cannot be moved");
                } else if (this.mFrozenPackages.contains(packageName)) {
                    throw new PackageManagerException(-7, "Failed to move already frozen package");
                } else {
                    file = new File(pkg.codePath);
                    String installerPackageName = ps.installerPackageName;
                    String packageAbiOverride = ps.cpuAbiOverrideString;
                    int appId = UserHandle.getAppId(pkg.applicationInfo.uid);
                    String seinfo = pkg.applicationInfo.seinfo;
                    label = String.valueOf(pm.getApplicationLabel(pkg.applicationInfo));
                    int targetSdkVersion = pkg.applicationInfo.targetSdkVersion;
                    PackageFreezer freezer = freezePackage(packageName, "movePackageInternal");
                    int[] installedUserIds = ps.queryInstalledUsers(sUserManager.getUserIds(), true);
                }
            }
        }
        Bundle extras = new Bundle();
        extras.putString("android.intent.extra.PACKAGE_NAME", packageName);
        extras.putString("android.intent.extra.TITLE", label);
        this.mMoveCallbacks.notifyCreated(moveId, extras);
        if (Objects.equals(StorageManager.UUID_PRIVATE_INTERNAL, volumeUuid)) {
            installFlags = 16;
            moveCompleteApp = !currentAsec;
            measurePath = Environment.getDataAppDirectory(volumeUuid);
        } else if (Objects.equals("primary_physical", volumeUuid)) {
            installFlags = 8;
            moveCompleteApp = false;
            measurePath = storage.getPrimaryPhysicalVolume().getPath();
        } else {
            VolumeInfo volume = storage.findVolumeByUuid(volumeUuid);
            if (this.mCustPms != null && this.mCustPms.canAppMoveToPublicSd(volume)) {
                installFlags = 8;
                moveCompleteApp = false;
                measurePath = volume.getPath();
            } else if (volume != null && volume.getType() == 1 && volume.isMountedWritable()) {
                Preconditions.checkState(!currentAsec);
                installFlags = 16;
                moveCompleteApp = true;
                measurePath = Environment.getDataAppDirectory(volumeUuid);
            } else {
                freezer.close();
                throw new PackageManagerException(-6, "Move location not mounted private volume");
            }
        }
        PackageStats packageStats = new PackageStats(null, -1);
        synchronized (this.mInstaller) {
            int i = 0;
            int length = installedUserIds.length;
            while (i < length) {
                if (getPackageSizeInfoLI(packageName, installedUserIds[i], packageStats)) {
                    i++;
                } else {
                    freezer.close();
                    throw new PackageManagerException(-6, "Failed to measure package size");
                }
            }
        }
        final long startFreeBytes = measurePath.getFreeSpace();
        if (moveCompleteApp) {
            sizeBytes = packageStats.codeSize + packageStats.dataSize;
        } else {
            sizeBytes = packageStats.codeSize;
        }
        if (sizeBytes > storage.getStorageBytesUntilLow(measurePath)) {
            freezer.close();
            throw new PackageManagerException(-6, "Not enough free space to move");
        }
        MoveInfo moveInfo;
        this.mMoveCallbacks.notifyStatusChanged(moveId, 10);
        final CountDownLatch installedLatch = new CountDownLatch(1);
        final PackageFreezer packageFreezer = freezer;
        final int i2 = moveId;
        IPackageInstallObserver2 anonymousClass31 = new IPackageInstallObserver2.Stub() {
            public void onUserActionRequired(Intent intent) throws RemoteException {
                throw new IllegalStateException();
            }

            public void onPackageInstalled(String basePackageName, int returnCode, String msg, Bundle extras) throws RemoteException {
                installedLatch.countDown();
                packageFreezer.close();
                switch (PackageManager.installStatusToPublicStatus(returnCode)) {
                    case 0:
                        PackageManagerService.this.mMoveCallbacks.notifyStatusChanged(i2, -100);
                        return;
                    case 6:
                        PackageManagerService.this.mMoveCallbacks.notifyStatusChanged(i2, -1);
                        return;
                    default:
                        PackageManagerService.this.mMoveCallbacks.notifyStatusChanged(i2, -6);
                        return;
                }
            }
        };
        if (moveCompleteApp) {
            final int i3 = moveId;
            new Thread() {
                public void run() {
                    while (!installedLatch.await(1, TimeUnit.SECONDS)) {
                        try {
                        } catch (InterruptedException e) {
                        }
                        PackageManagerService.this.mMoveCallbacks.notifyStatusChanged(i3, ((int) MathUtils.constrain(((startFreeBytes - measurePath.getFreeSpace()) * 80) / sizeBytes, 0, 80)) + 10);
                    }
                }
            }.start();
            moveInfo = new MoveInfo(moveId, currentVolumeUuid, volumeUuid, packageName, file.getName(), appId, seinfo, targetSdkVersion);
        } else {
            moveInfo = null;
        }
        installFlags |= 2;
        Message msg = this.mHandler.obtainMessage(5);
        InstallParams params = new InstallParams(OriginInfo.fromExistingFile(file), moveInfo, anonymousClass31, installFlags, installerPackageName, volumeUuid, null, user, packageAbiOverride, null, null);
        params.setTraceMethod("movePackage").setTraceCookie(System.identityHashCode(params));
        msg.obj = params;
        Trace.asyncTraceBegin(262144, "movePackage", System.identityHashCode(msg.obj));
        Trace.asyncTraceBegin(262144, "queueInstall", System.identityHashCode(msg.obj));
        this.mHandler.sendMessage(msg);
    }

    public int movePrimaryStorage(String volumeUuid) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MOVE_PACKAGE", null);
        final int realMoveId = this.mNextMoveId.getAndIncrement();
        Bundle extras = new Bundle();
        extras.putString("android.os.storage.extra.FS_UUID", volumeUuid);
        this.mMoveCallbacks.notifyCreated(realMoveId, extras);
        ((StorageManager) this.mContext.getSystemService(StorageManager.class)).setPrimaryStorageUuid(volumeUuid, new IPackageMoveObserver.Stub() {
            public void onCreated(int moveId, Bundle extras) {
            }

            public void onStatusChanged(int moveId, int status, long estMillis) {
                PackageManagerService.this.mMoveCallbacks.notifyStatusChanged(realMoveId, status, estMillis);
            }
        });
        return realMoveId;
    }

    public int getMoveStatus(int moveId) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS", null);
        return this.mMoveCallbacks.mLastStatus.get(moveId);
    }

    public void registerMoveCallback(IPackageMoveObserver callback) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS", null);
        this.mMoveCallbacks.register(callback);
    }

    public void unregisterMoveCallback(IPackageMoveObserver callback) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS", null);
        this.mMoveCallbacks.unregister(callback);
    }

    public boolean setInstallLocation(int loc) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS", null);
        if (getInstallLocation() == loc) {
            return true;
        }
        if (loc != 0 && loc != 1 && loc != 2) {
            return false;
        }
        Global.putInt(this.mContext.getContentResolver(), "default_install_location", loc);
        return true;
    }

    public int getInstallLocation() {
        return Global.getInt(this.mContext.getContentResolver(), "default_install_location", 0);
    }

    void cleanUpUser(UserManagerService userManager, int userHandle) {
        synchronized (this.mPackages) {
            this.mDirtyUsers.remove(Integer.valueOf(userHandle));
            this.mUserNeedsBadging.delete(userHandle);
            this.mSettings.removeUserLPw(userHandle);
            this.mPendingBroadcasts.remove(userHandle);
            this.mEphemeralApplicationRegistry.onUserRemovedLPw(userHandle);
            removeUnusedPackagesLPw(userManager, userHandle);
        }
    }

    private void removeUnusedPackagesLPw(UserManagerService userManager, final int userHandle) {
        int[] users = userManager.getUserIds();
        for (PackageSetting ps : this.mSettings.mPackages.values()) {
            if (ps.pkg != null) {
                final String packageName = ps.pkg.packageName;
                if ((ps.pkgFlags & 1) == 0) {
                    boolean keep = shouldKeepUninstalledPackageLPr(packageName);
                    if (!keep) {
                        int i = 0;
                        while (i < users.length) {
                            if (users[i] != userHandle && ps.getInstalled(users[i])) {
                                keep = true;
                                break;
                            }
                            i++;
                        }
                    }
                    if (!keep) {
                        this.mHandler.post(new Runnable() {
                            public void run() {
                                PackageManagerService.this.deletePackageX(packageName, userHandle, 0);
                            }
                        });
                    }
                }
            }
        }
    }

    void createNewUser(int userId) {
        synchronized (this.mInstallLock) {
            this.mSettings.createNewUserLI(this, this.mInstaller, userId);
        }
        synchronized (this.mPackages) {
            scheduleWritePackageRestrictionsLocked(userId);
            scheduleWritePackageListLocked(userId);
            applyFactoryDefaultBrowserLPw(userId);
            primeDomainVerificationsLPw(userId);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void onBeforeUserStartUninitialized(int userId) {
        synchronized (this.mPackages) {
            if (this.mSettings.areDefaultRuntimePermissionsGrantedLPr(userId)) {
            }
        }
    }

    public VerifierDeviceIdentity getVerifierDeviceIdentity() throws RemoteException {
        VerifierDeviceIdentity verifierDeviceIdentityLPw;
        this.mContext.enforceCallingOrSelfPermission("android.permission.PACKAGE_VERIFICATION_AGENT", "Only package verification agents can read the verifier device identity");
        synchronized (this.mPackages) {
            verifierDeviceIdentityLPw = this.mSettings.getVerifierDeviceIdentityLPw();
        }
        return verifierDeviceIdentityLPw;
    }

    public void setPermissionEnforced(String permission, boolean enforced) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.GRANT_RUNTIME_PERMISSIONS", "setPermissionEnforced");
        if ("android.permission.READ_EXTERNAL_STORAGE".equals(permission)) {
            synchronized (this.mPackages) {
                if (this.mSettings.mReadExternalStorageEnforced == null || this.mSettings.mReadExternalStorageEnforced.booleanValue() != enforced) {
                    this.mSettings.mReadExternalStorageEnforced = Boolean.valueOf(enforced);
                    this.mSettings.writeLPr();
                }
            }
            IActivityManager am = ActivityManagerNative.getDefault();
            if (am != null) {
                long token = Binder.clearCallingIdentity();
                try {
                    am.killProcessesBelowForeground("setPermissionEnforcement");
                } catch (RemoteException e) {
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
                return;
            }
            return;
        }
        throw new IllegalArgumentException("No selective enforcement for " + permission);
    }

    @Deprecated
    public boolean isPermissionEnforced(String permission) {
        return true;
    }

    public boolean isStorageLow() {
        long token = Binder.clearCallingIdentity();
        try {
            DeviceStorageMonitorInternal dsm = (DeviceStorageMonitorInternal) LocalServices.getService(DeviceStorageMonitorInternal.class);
            if (dsm != null) {
                boolean isMemoryLow = dsm.isMemoryLow();
                return isMemoryLow;
            }
            Binder.restoreCallingIdentity(token);
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public IPackageInstaller getPackageInstaller() {
        return this.mInstallerService;
    }

    private boolean userNeedsBadging(int userId) {
        int index = this.mUserNeedsBadging.indexOfKey(userId);
        if (index >= 0) {
            return this.mUserNeedsBadging.valueAt(index);
        }
        long token = Binder.clearCallingIdentity();
        try {
            boolean b;
            UserInfo userInfo = sUserManager.getUserInfo(userId);
            if (userInfo == null || !userInfo.isManagedProfile()) {
                b = false;
            } else {
                b = true;
            }
            this.mUserNeedsBadging.put(userId, b);
            return b;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public KeySet getKeySetByAlias(String packageName, String alias) {
        if (packageName == null || alias == null) {
            return null;
        }
        KeySet keySet;
        synchronized (this.mPackages) {
            if (((Package) this.mPackages.get(packageName)) == null) {
                Slog.w(TAG, "KeySet requested for unknown package: " + packageName);
                throw new IllegalArgumentException("Unknown package: " + packageName);
            }
            keySet = new KeySet(this.mSettings.mKeySetManagerService.getKeySetByAliasAndPackageNameLPr(packageName, alias));
        }
        return keySet;
    }

    public KeySet getSigningKeySet(String packageName) {
        if (packageName == null) {
            return null;
        }
        KeySet keySet;
        synchronized (this.mPackages) {
            Package pkg = (Package) this.mPackages.get(packageName);
            if (pkg == null) {
                Slog.w(TAG, "KeySet requested for unknown package: " + packageName);
                throw new IllegalArgumentException("Unknown package: " + packageName);
            } else if (pkg.applicationInfo.uid == Binder.getCallingUid() || 1000 == Binder.getCallingUid()) {
                keySet = new KeySet(this.mSettings.mKeySetManagerService.getSigningKeySetByPackageNameLPr(packageName));
            } else {
                throw new SecurityException("May not access signing KeySet of other apps.");
            }
        }
        return keySet;
    }

    public boolean isPackageSignedByKeySet(String packageName, KeySet ks) {
        if (packageName == null || ks == null) {
            return false;
        }
        synchronized (this.mPackages) {
            if (((Package) this.mPackages.get(packageName)) == null) {
                Slog.w(TAG, "KeySet requested for unknown package: " + packageName);
                throw new IllegalArgumentException("Unknown package: " + packageName);
            }
            IBinder ksh = ks.getToken();
            if (ksh instanceof KeySetHandle) {
                boolean packageIsSignedByLPr = this.mSettings.mKeySetManagerService.packageIsSignedByLPr(packageName, (KeySetHandle) ksh);
                return packageIsSignedByLPr;
            }
            return false;
        }
    }

    public boolean isPackageSignedByKeySetExactly(String packageName, KeySet ks) {
        if (packageName == null || ks == null) {
            return false;
        }
        synchronized (this.mPackages) {
            if (((Package) this.mPackages.get(packageName)) == null) {
                Slog.w(TAG, "KeySet requested for unknown package: " + packageName);
                throw new IllegalArgumentException("Unknown package: " + packageName);
            }
            IBinder ksh = ks.getToken();
            if (ksh instanceof KeySetHandle) {
                boolean packageIsSignedByExactlyLPr = this.mSettings.mKeySetManagerService.packageIsSignedByExactlyLPr(packageName, (KeySetHandle) ksh);
                return packageIsSignedByExactlyLPr;
            }
            return false;
        }
    }

    private static String getParam(String params, String prefix, String separator) {
        int left = params.indexOf(prefix);
        if (left < 0) {
            Log.e(TAG, params + " not contains " + prefix);
            return null;
        }
        left += prefix.length();
        int right = params.indexOf(separator, left);
        if (right >= 0) {
            return params.substring(left, right);
        }
        Log.e(TAG, params + " not contains " + separator);
        return null;
    }

    private static void saveDex2oatList(List<String> list) {
        IOException e;
        Throwable th;
        BufferedWriter bufferedWriter = null;
        try {
            BufferedWriter fout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/data/system/dex2oat.list"), "UTF-8"));
            try {
                for (String i : list) {
                    fout.write(i + "\n");
                }
                if (fout != null) {
                    try {
                        fout.close();
                    } catch (IOException e2) {
                    }
                }
            } catch (IOException e3) {
                e = e3;
                bufferedWriter = fout;
                try {
                    Log.e(TAG, "saveDex2oatList error: ", e);
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e4) {
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e5) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                bufferedWriter = fout;
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                throw th;
            }
        } catch (IOException e6) {
            e = e6;
            Log.e(TAG, "saveDex2oatList error: ", e);
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        }
    }

    private void deletePackageIfUnusedLPr(final String packageName) {
        PackageSetting ps = (PackageSetting) this.mSettings.mPackages.get(packageName);
        if (!(ps == null || ps.isAnyInstalled(sUserManager.getUserIds()))) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    PackageManagerService.this.deletePackageX(packageName, 0, 2);
                }
            });
        }
    }

    private static void checkDowngrade(Package before, PackageInfoLite after) throws PackageManagerException {
        if (after.versionCode < before.mVersionCode) {
            throw new PackageManagerException(-25, "Update version code " + after.versionCode + " is older than current " + before.mVersionCode);
        } else if (after.versionCode != before.mVersionCode) {
        } else {
            if (after.baseRevisionCode < before.baseRevisionCode) {
                throw new PackageManagerException(-25, "Update base revision code " + after.baseRevisionCode + " is older than current " + before.baseRevisionCode);
            } else if (!ArrayUtils.isEmpty(after.splitNames)) {
                int i = 0;
                while (i < after.splitNames.length) {
                    String splitName = after.splitNames[i];
                    int j = ArrayUtils.indexOf(before.splitNames, splitName);
                    if (j == -1 || after.splitRevisionCodes[i] >= before.splitRevisionCodes[j]) {
                        i++;
                    } else {
                        throw new PackageManagerException(-25, "Update split " + splitName + " revision code " + after.splitRevisionCodes[i] + " is older than current " + before.splitRevisionCodes[j]);
                    }
                }
            }
        }
    }

    public void grantDefaultPermissionsToEnabledCarrierApps(String[] packageNames, int userId) {
        enforceSystemOrPhoneCaller("grantPermissionsToEnabledCarrierApps");
        synchronized (this.mPackages) {
            long identity = Binder.clearCallingIdentity();
            try {
                this.mDefaultPermissionPolicy.grantDefaultPermissionsToEnabledCarrierAppsLPr(packageNames, userId);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private static void enforceSystemOrPhoneCaller(String tag) {
        int callingUid = Binder.getCallingUid();
        if (callingUid != RADIO_UID && callingUid != 1000) {
            throw new SecurityException("Cannot call " + tag + " from UID " + callingUid);
        }
    }

    boolean isHistoricalPackageUsageAvailable() {
        return this.mPackageUsage.isHistoricalPackageUsageAvailable();
    }

    Collection<Package> getPackages() {
        Collection arrayList;
        synchronized (this.mPackages) {
            arrayList = new ArrayList(this.mPackages.values());
        }
        return arrayList;
    }

    public void logAppProcessStartIfNeeded(String processName, int uid, String seinfo, String apkFile, int pid) {
        if (SecurityLog.isLoggingEnabled()) {
            Bundle data = new Bundle();
            data.putLong("startTimestamp", System.currentTimeMillis());
            data.putString("processName", processName);
            data.putInt("uid", uid);
            data.putString("seinfo", seinfo);
            data.putString("apkFile", apkFile);
            data.putInt("pid", pid);
            Message msg = this.mProcessLoggingHandler.obtainMessage(1);
            msg.setData(data);
            this.mProcessLoggingHandler.sendMessage(msg);
        }
    }

    private void parseInstalledPkgInfo(InstallArgs args, PackageInstalledInfo res) {
        String pkgPath = null;
        int pkgInstallResult = 0;
        int pkgVersionCode = 0;
        String pkgVersionName = "";
        String pkgName = "";
        boolean pkgUpdate = false;
        if (!(args == null || args.origin == null || args.origin.file == null)) {
            pkgPath = args.origin.file.toString();
        }
        if (res != null) {
            pkgInstallResult = res.returnCode;
            if (res.pkg != null) {
                pkgVersionCode = res.pkg.mVersionCode;
                pkgVersionName = res.pkg.mVersionName;
                if (res.pkg.applicationInfo != null) {
                    pkgName = res.pkg.applicationInfo.packageName;
                }
            }
            if (res.removedInfo != null) {
                pkgUpdate = res.removedInfo.removedPackage != null;
            }
        }
        parseInstalledPkgInfo(pkgPath, pkgName, pkgVersionName, pkgVersionCode, pkgInstallResult, pkgUpdate);
    }

    protected boolean isAppInstallAllowed(String installer, String appName) {
        return true;
    }

    protected boolean isUnAppInstallAllowed(String originPath) {
        return false;
    }

    public void loadSysWhitelist() {
    }

    public void checkIllegalSysApk(Package pkg, int hwFlags) throws PackageManagerException {
    }

    protected void addGrantedInstalledPkg(String pkgName, boolean grant) {
    }

    protected Signature[] getRealSignature(Package pkg) {
        if (pkg == null) {
            return new Signature[0];
        }
        return pkg.mRealSignatures;
    }

    protected void setRealSignature(Package pkg, Signature[] sign) {
        if (pkg != null) {
            pkg.mRealSignatures = sign;
        }
    }

    private void uploadInstallErrRadar(String reason) {
        Bundle data = new Bundle();
        data.putString(HwBroadcastRadarUtil.KEY_PACKAGE, "PMS");
        data.putString(HwBroadcastRadarUtil.KEY_VERSION_NAME, "0");
        data.putString("extra", reason);
        if (this.mMonitor != null) {
            this.mMonitor.monitor(907400000, data);
        }
    }

    private void writeNetQinFlag(String pkgName) {
        if ("com.nqmobile.antivirus20.hw".equalsIgnoreCase(pkgName)) {
            File file = new File(new File(Environment.getDataDirectory(), "system"), "netqin.tmp");
            synchronized (this.mPackages) {
                if (file.exists()) {
                    return;
                }
                try {
                    if (file.createNewFile()) {
                        FileUtils.setPermissions(file.getPath(), 416, -1, -1);
                        Log.i(TAG, "Create netqin flag successfully");
                    }
                } catch (IOException e) {
                    Log.i(TAG, "Fail to create netqin flag");
                }
            }
        } else {
            return;
        }
    }
}
