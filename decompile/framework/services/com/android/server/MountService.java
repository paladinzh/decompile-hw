package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.IPackageMoveObserver;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.content.res.ObbInfo;
import android.hdm.HwDeviceManager;
import android.net.Uri;
import android.os.Binder;
import android.os.DropBoxManager;
import android.os.Environment;
import android.os.Environment.UserEnvironment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.OnCloseListener;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.DiskInfo;
import android.os.storage.IMountService.Stub;
import android.os.storage.IMountServiceListener;
import android.os.storage.IMountShutdownObserver;
import android.os.storage.IObbActionListener;
import android.os.storage.MountServiceInternal;
import android.os.storage.MountServiceInternal.ExternalStorageMountPolicy;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.os.storage.VolumeRecord;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import android.util.TimeUtils;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IMediaContainerService;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.HexDump;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.NativeDaemonConnector.Command;
import com.android.server.NativeDaemonConnector.SensitiveArg;
import com.android.server.Watchdog.Monitor;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.PackageManagerService;
import com.android.server.usage.UnixCalendar;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import libcore.io.IoUtils;
import libcore.util.EmptyArray;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class MountService extends Stub implements INativeDaemonConnectorCallbacks, Monitor {
    private static final String ATTR_CREATED_MILLIS = "createdMillis";
    private static final String ATTR_FORCE_ADOPTABLE = "forceAdoptable";
    private static final String ATTR_FS_UUID = "fsUuid";
    private static final String ATTR_LAST_BENCH_MILLIS = "lastBenchMillis";
    private static final String ATTR_LAST_TRIM_MILLIS = "lastTrimMillis";
    private static final String ATTR_NICKNAME = "nickname";
    private static final String ATTR_PART_GUID = "partGuid";
    private static final String ATTR_PRIMARY_STORAGE_UUID = "primaryStorageUuid";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_USER_FLAGS = "userFlags";
    private static final String ATTR_VERSION = "version";
    private static final String CRYPTD_TAG = "CryptdConnector";
    private static final int CRYPTO_ALGORITHM_KEY_SIZE = 128;
    public static final String[] CRYPTO_TYPES = new String[]{"password", "default", "pattern", "pin"};
    private static final boolean DEBUG_EVENTS = false;
    private static final boolean DEBUG_OBB = false;
    static final ComponentName DEFAULT_CONTAINER_COMPONENT = new ComponentName("com.android.defcontainer", "com.android.defcontainer.DefaultContainerService");
    private static final int H_ABNORMAL_SD_BROADCAST = 11;
    private static final int H_DAEMON_CONNECTED = 2;
    private static final int H_FSTRIM = 4;
    private static final int H_INTERNAL_BROADCAST = 7;
    private static final int H_PARTITION_FORGET = 9;
    private static final int H_RESET = 10;
    private static final int H_SHUTDOWN = 3;
    private static final int H_SYSTEM_READY = 1;
    private static final int H_VOLUME_BROADCAST = 6;
    private static final int H_VOLUME_MOUNT = 5;
    private static final int H_VOLUME_UNMOUNT = 8;
    private static final String LAST_FSTRIM_FILE = "last-fstrim";
    private static final int MAX_CONTAINERS = 250;
    private static final int MOVE_STATUS_COPY_FINISHED = 82;
    private static final int OBB_FLUSH_MOUNT_STATE = 5;
    private static final int OBB_MCS_BOUND = 2;
    private static final int OBB_MCS_RECONNECT = 4;
    private static final int OBB_MCS_UNBIND = 3;
    private static final int OBB_RUN_ACTION = 1;
    private static final int PBKDF2_HASH_ROUNDS = 1024;
    private static final String TAG = "MountService";
    private static final String TAG_STORAGE_BENCHMARK = "storage_benchmark";
    private static final String TAG_STORAGE_TRIM = "storage_trim";
    private static final String TAG_VOLUME = "volume";
    private static final String TAG_VOLUMES = "volumes";
    private static final int VERSION_ADD_PRIMARY = 2;
    private static final int VERSION_FIX_PRIMARY = 3;
    private static final int VERSION_INIT = 1;
    private static final String VOLD_TAG = "VoldConnector";
    private static final boolean WATCHDOG_ENABLE = false;
    static MountService sSelf = null;
    private final HashSet<String> mAsecMountSet = new HashSet();
    private final CountDownLatch mAsecsScanned = new CountDownLatch(1);
    private volatile boolean mBootCompleted = false;
    private final Callbacks mCallbacks;
    private final CountDownLatch mConnectedSignal = new CountDownLatch(2);
    private final NativeDaemonConnector mConnector;
    private final Thread mConnectorThread;
    private IMediaContainerService mContainerService = null;
    private final Context mContext;
    private final NativeDaemonConnector mCryptConnector;
    private final Thread mCryptConnectorThread;
    private volatile int mCurrentUserId = 0;
    private HwCustMountService mCustMountService = ((HwCustMountService) HwCustUtils.createObj(HwCustMountService.class, new Object[0]));
    private volatile boolean mDaemonConnected = false;
    private final DefaultContainerConnection mDefContainerConn = new DefaultContainerConnection();
    @GuardedBy("mLock")
    private ArrayMap<String, CountDownLatch> mDiskScanLatches = new ArrayMap();
    @GuardedBy("mLock")
    private ArrayMap<String, DiskInfo> mDisks = new ArrayMap();
    @GuardedBy("mLock")
    private boolean mForceAdoptable;
    private final Handler mHandler;
    private long mLastMaintenance;
    private final File mLastMaintenanceFile;
    @GuardedBy("mLock")
    private int[] mLocalUnlockedUsers = EmptyArray.INT;
    private final Object mLock = new Object();
    private final LockPatternUtils mLockPatternUtils;
    private final MountServiceInternalImpl mMountServiceInternal = new MountServiceInternalImpl();
    @GuardedBy("mLock")
    private IPackageMoveObserver mMoveCallback;
    @GuardedBy("mLock")
    private String mMoveTargetUuid;
    private final ObbActionHandler mObbActionHandler;
    private final Map<IBinder, List<ObbState>> mObbMounts = new HashMap();
    private final Map<String, ObbState> mObbPathToStateMap = new HashMap();
    private PackageManagerService mPms;
    @GuardedBy("mLock")
    private String mPrimaryStorageUuid;
    @GuardedBy("mLock")
    private ArrayMap<String, VolumeRecord> mRecords = new ArrayMap();
    private final AtomicFile mSettingsFile;
    private volatile boolean mSystemReady = false;
    @GuardedBy("mLock")
    private int[] mSystemUnlockedUsers = EmptyArray.INT;
    private final BroadcastReceiver mToVoldBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("android.intent.action.BOOT_COMPLETED")) {
                MountService.this.bootCompleteToVold();
            }
        }
    };
    private final Object mUnmountLock = new Object();
    @GuardedBy("mUnmountLock")
    private CountDownLatch mUnmountSignal;
    private BroadcastReceiver mUserReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean z = true;
            String action = intent.getAction();
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
            if (userId < 0) {
                z = false;
            }
            Preconditions.checkArgument(z);
            try {
                if ("android.intent.action.USER_ADDED".equals(action)) {
                    int userSerialNumber = ((UserManager) MountService.this.mContext.getSystemService(UserManager.class)).getUserSerialNumber(userId);
                    MountService.this.mConnector.execute(MountService.TAG_VOLUME, "user_added", Integer.valueOf(userId), Integer.valueOf(userSerialNumber));
                } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                    synchronized (MountService.this.mVolumes) {
                        int size = MountService.this.mVolumes.size();
                        for (int i = 0; i < size; i++) {
                            VolumeInfo vol = (VolumeInfo) MountService.this.mVolumes.valueAt(i);
                            if (vol.mountUserId == userId) {
                                vol.mountUserId = -10000;
                                MountService.this.mHandler.obtainMessage(8, vol).sendToTarget();
                            }
                        }
                    }
                    MountService.this.mConnector.execute(MountService.TAG_VOLUME, "user_removed", Integer.valueOf(userId));
                }
            } catch (NativeDaemonConnectorException e) {
                Slog.w(MountService.TAG, "Failed to send user details to vold", e);
            }
        }
    };
    private boolean mUserStartedFinish = false;
    @GuardedBy("mLock")
    private final ArrayMap<String, VolumeInfo> mVolumes = new ArrayMap();

    private static class Callbacks extends Handler {
        private static final int MSG_DISK_DESTROYED = 6;
        private static final int MSG_DISK_SCANNED = 5;
        private static final int MSG_STORAGE_STATE_CHANGED = 1;
        private static final int MSG_VOLUME_FORGOTTEN = 4;
        private static final int MSG_VOLUME_RECORD_CHANGED = 3;
        private static final int MSG_VOLUME_STATE_CHANGED = 2;
        private final RemoteCallbackList<IMountServiceListener> mCallbacks = new RemoteCallbackList();

        public Callbacks(Looper looper) {
            super(looper);
        }

        public void register(IMountServiceListener callback) {
            this.mCallbacks.register(callback);
        }

        public void unregister(IMountServiceListener callback) {
            this.mCallbacks.unregister(callback);
        }

        public void handleMessage(Message msg) {
            SomeArgs args = msg.obj;
            int n = this.mCallbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    invokeCallback((IMountServiceListener) this.mCallbacks.getBroadcastItem(i), msg.what, args);
                } catch (RemoteException e) {
                }
            }
            this.mCallbacks.finishBroadcast();
            args.recycle();
        }

        private void invokeCallback(IMountServiceListener callback, int what, SomeArgs args) throws RemoteException {
            switch (what) {
                case 1:
                    callback.onStorageStateChanged((String) args.arg1, (String) args.arg2, (String) args.arg3);
                    return;
                case 2:
                    callback.onVolumeStateChanged((VolumeInfo) args.arg1, args.argi2, args.argi3);
                    return;
                case 3:
                    callback.onVolumeRecordChanged((VolumeRecord) args.arg1);
                    return;
                case 4:
                    callback.onVolumeForgotten((String) args.arg1);
                    return;
                case 5:
                    callback.onDiskScanned((DiskInfo) args.arg1, args.argi2);
                    return;
                case 6:
                    callback.onDiskDestroyed((DiskInfo) args.arg1);
                    return;
                default:
                    return;
            }
        }

        private void notifyStorageStateChanged(String path, String oldState, String newState) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = path;
            args.arg2 = oldState;
            args.arg3 = newState;
            obtainMessage(1, args).sendToTarget();
        }

        private void notifyVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = vol.clone();
            args.argi2 = oldState;
            args.argi3 = newState;
            obtainMessage(2, args).sendToTarget();
        }

        private void notifyVolumeRecordChanged(VolumeRecord rec) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = rec.clone();
            obtainMessage(3, args).sendToTarget();
        }

        private void notifyVolumeForgotten(String fsUuid) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = fsUuid;
            obtainMessage(4, args).sendToTarget();
        }

        private void notifyDiskScanned(DiskInfo disk, int volumeCount) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = disk.clone();
            args.argi2 = volumeCount;
            obtainMessage(5, args).sendToTarget();
        }

        private void notifyDiskDestroyed(DiskInfo disk) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = disk.clone();
            obtainMessage(6, args).sendToTarget();
        }
    }

    class DefaultContainerConnection implements ServiceConnection {
        DefaultContainerConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            MountService.this.mObbActionHandler.sendMessage(MountService.this.mObbActionHandler.obtainMessage(2, IMediaContainerService.Stub.asInterface(service)));
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    }

    public static class Lifecycle extends SystemService {
        private MountService mMountService;

        public Lifecycle(Context context) {
            super(context);
        }

        public void onStart() {
            this.mMountService = new MountService(getContext());
            publishBinderService("mount", this.mMountService);
            this.mMountService.start();
        }

        public void onBootPhase(int phase) {
            if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
                this.mMountService.systemReady();
            } else if (phase == 1000) {
                this.mMountService.bootCompleted();
            }
        }

        public void onSwitchUser(int userHandle) {
            this.mMountService.mCurrentUserId = userHandle;
        }

        public void onUnlockUser(int userHandle) {
            this.mMountService.onUnlockUser(userHandle);
        }

        public void onCleanupUser(int userHandle) {
            this.mMountService.onCleanupUser(userHandle);
        }
    }

    abstract class ObbAction {
        private static final int MAX_RETRIES = 3;
        ObbState mObbState;
        private int mRetries;

        abstract void handleError();

        abstract void handleExecute() throws RemoteException, IOException;

        ObbAction(ObbState obbState) {
            this.mObbState = obbState;
        }

        public void execute(ObbActionHandler handler) {
            try {
                this.mRetries++;
                if (this.mRetries > 3) {
                    Slog.w(MountService.TAG, "Failed to invoke remote methods on default container service. Giving up");
                    MountService.this.mObbActionHandler.sendEmptyMessage(3);
                    handleError();
                    return;
                }
                handleExecute();
                MountService.this.mObbActionHandler.sendEmptyMessage(3);
            } catch (RemoteException e) {
                MountService.this.mObbActionHandler.sendEmptyMessage(4);
            } catch (Exception e2) {
                handleError();
                MountService.this.mObbActionHandler.sendEmptyMessage(3);
            }
        }

        protected ObbInfo getObbInfo() throws IOException {
            ObbInfo obbInfo;
            try {
                obbInfo = MountService.this.mContainerService.getObbInfo(this.mObbState.canonicalPath);
            } catch (RemoteException e) {
                Slog.d(MountService.TAG, "Couldn't call DefaultContainerService to fetch OBB info for " + this.mObbState.canonicalPath);
                obbInfo = null;
            }
            if (obbInfo != null) {
                return obbInfo;
            }
            throw new IOException("Couldn't read OBB file: " + this.mObbState.canonicalPath);
        }

        protected void sendNewStatusOrIgnore(int status) {
            if (this.mObbState != null && this.mObbState.token != null) {
                try {
                    this.mObbState.token.onObbResult(this.mObbState.rawPath, this.mObbState.nonce, status);
                } catch (RemoteException e) {
                    Slog.w(MountService.TAG, "MountServiceListener went away while calling onObbStateChanged");
                }
            }
        }
    }

    class MountObbAction extends ObbAction {
        private final int mCallingUid;
        private final String mKey;

        MountObbAction(ObbState obbState, String key, int callingUid) {
            super(obbState);
            this.mKey = key;
            this.mCallingUid = callingUid;
        }

        public void handleExecute() throws IOException, RemoteException {
            MountService.this.waitForReady();
            MountService.this.warnOnNotMounted();
            ObbInfo obbInfo = getObbInfo();
            if (MountService.this.isUidOwnerOfPackageOrSystem(obbInfo.packageName, this.mCallingUid)) {
                boolean isMounted;
                synchronized (MountService.this.mObbMounts) {
                    isMounted = MountService.this.mObbPathToStateMap.containsKey(this.mObbState.rawPath);
                }
                if (isMounted) {
                    Slog.w(MountService.TAG, "Attempt to mount OBB which is already mounted: " + obbInfo.filename);
                    sendNewStatusOrIgnore(24);
                    return;
                }
                String hashedKey;
                if (this.mKey == null) {
                    hashedKey = "none";
                } else {
                    try {
                        hashedKey = new BigInteger(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(new PBEKeySpec(this.mKey.toCharArray(), obbInfo.salt, 1024, 128)).getEncoded()).toString(16);
                    } catch (NoSuchAlgorithmException e) {
                        Slog.e(MountService.TAG, "Could not load PBKDF2 algorithm", e);
                        sendNewStatusOrIgnore(20);
                        return;
                    } catch (InvalidKeySpecException e2) {
                        Slog.e(MountService.TAG, "Invalid key spec when loading PBKDF2 algorithm", e2);
                        sendNewStatusOrIgnore(20);
                        return;
                    }
                }
                int rc = 0;
                try {
                    MountService.this.mConnector.execute("obb", "mount", this.mObbState.canonicalPath, new SensitiveArg(hashedKey), Integer.valueOf(this.mObbState.ownerGid));
                } catch (NativeDaemonConnectorException e3) {
                    if (e3.getCode() != VoldResponseCode.OpFailedStorageBusy) {
                        rc = -1;
                    }
                }
                if (rc == 0) {
                    synchronized (MountService.this.mObbMounts) {
                        MountService.this.addObbStateLocked(this.mObbState);
                    }
                    sendNewStatusOrIgnore(1);
                } else {
                    Slog.e(MountService.TAG, "Couldn't mount OBB file: " + rc);
                    sendNewStatusOrIgnore(21);
                }
                return;
            }
            Slog.w(MountService.TAG, "Denied attempt to mount OBB " + obbInfo.filename + " which is owned by " + obbInfo.packageName);
            sendNewStatusOrIgnore(25);
        }

        public void handleError() {
            sendNewStatusOrIgnore(20);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("MountObbAction{");
            sb.append(this.mObbState);
            sb.append('}');
            return sb.toString();
        }
    }

    class MountServiceHandler extends Handler {
        public MountServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    MountService.this.handleSystemReady();
                    return;
                case 2:
                    MountService.this.handleDaemonConnected();
                    return;
                case 3:
                    IMountShutdownObserver obs = msg.obj;
                    boolean success = false;
                    try {
                        success = MountService.this.mConnector.execute(MountService.TAG_VOLUME, "shutdown").isClassOk();
                    } catch (NativeDaemonConnectorException e) {
                    }
                    if (obs != null) {
                        try {
                            obs.onShutDownComplete(success ? 0 : -1);
                            return;
                        } catch (RemoteException e2) {
                            return;
                        }
                    }
                    return;
                case 4:
                    if (MountService.this.isReady()) {
                        Slog.i(MountService.TAG, "Running fstrim idle maintenance");
                        try {
                            MountService.this.mLastMaintenance = System.currentTimeMillis();
                            MountService.this.mLastMaintenanceFile.setLastModified(MountService.this.mLastMaintenance);
                        } catch (Exception e3) {
                            Slog.e(MountService.TAG, "Unable to record last fstrim!");
                        }
                        boolean shouldBenchmark = MountService.this.shouldBenchmark();
                        try {
                            NativeDaemonConnector -get0 = MountService.this.mConnector;
                            String str = "fstrim";
                            Object[] objArr = new Object[1];
                            objArr[0] = shouldBenchmark ? "dotrimbench" : "dotrim";
                            -get0.execute(str, objArr);
                        } catch (NativeDaemonConnectorException e4) {
                            Slog.e(MountService.TAG, "Failed to run fstrim!");
                        }
                        Runnable callback = msg.obj;
                        if (callback != null) {
                            callback.run();
                            return;
                        }
                        return;
                    }
                    Slog.i(MountService.TAG, "fstrim requested, but no daemon connection yet; trying again");
                    sendMessageDelayed(obtainMessage(4, msg.obj), 1000);
                    return;
                case 5:
                    VolumeInfo vol = msg.obj;
                    if (MountService.this.isMountDisallowed(vol)) {
                        Slog.i(MountService.TAG, "Ignoring mount " + vol.getId() + " due to policy");
                        return;
                    } else if (HwDeviceManager.disallowOp(12) && !vol.id.contains("public:179") && vol.id.contains("public:")) {
                        Slog.i(MountService.TAG, "Usb mass device is disabled by dpm");
                        return;
                    } else if (MountService.this.isSDCardDisable(vol)) {
                        Slog.i(MountService.TAG, "Ignoring mount " + vol.getId() + " due to cryptsd");
                        return;
                    } else if (MountService.this.mUserStartedFinish || vol.getType() != 0 || vol.getDiskId() == null) {
                        try {
                            MountService.this.mConnector.execute(MountService.TAG_VOLUME, "mount", vol.id, Integer.valueOf(vol.mountFlags), Integer.valueOf(vol.mountUserId));
                            return;
                        } catch (NativeDaemonConnectorException e5) {
                            return;
                        }
                    } else {
                        Slog.i(MountService.TAG, "waiting user start finish; trying again , diskId = " + vol.getDiskId());
                        sendMessageDelayed(obtainMessage(5, msg.obj), 500);
                        return;
                    }
                case 6:
                    StorageVolume userVol = msg.obj;
                    String envState = userVol.getState();
                    Slog.d(MountService.TAG, "Volume " + userVol.getId() + " broadcasting " + envState + " to " + userVol.getOwner());
                    String action = VolumeInfo.getBroadcastForEnvironment(envState);
                    if (action != null) {
                        Intent intent = new Intent(action, Uri.fromFile(userVol.getPathFile()));
                        intent.putExtra("android.os.storage.extra.STORAGE_VOLUME", userVol);
                        intent.addFlags(67108864);
                        MountService.this.mContext.sendBroadcastAsUser(intent, userVol.getOwner());
                        return;
                    }
                    return;
                case 7:
                    MountService.this.mContext.sendBroadcastAsUser((Intent) msg.obj, UserHandle.ALL, "android.permission.WRITE_MEDIA_STORAGE");
                    return;
                case 8:
                    MountService.this.unmount(((VolumeInfo) msg.obj).getId());
                    return;
                case 9:
                    MountService.this.forgetPartition(msg.obj);
                    return;
                case 10:
                    MountService.this.resetIfReadyAndConnected();
                    return;
                case 11:
                    MountService.this.mContext.sendBroadcastAsUser((Intent) msg.obj, UserHandle.ALL);
                    return;
                default:
                    return;
            }
        }
    }

    private final class MountServiceInternalImpl extends MountServiceInternal {
        private final CopyOnWriteArrayList<ExternalStorageMountPolicy> mPolicies;

        private MountServiceInternalImpl() {
            this.mPolicies = new CopyOnWriteArrayList();
        }

        public void addExternalStoragePolicy(ExternalStorageMountPolicy policy) {
            this.mPolicies.add(policy);
        }

        public void onExternalStoragePolicyChanged(int uid, String packageName) {
            MountService.this.remountUidExternalStorage(uid, getExternalStorageMountMode(uid, packageName));
        }

        public int getExternalStorageMountMode(int uid, String packageName) {
            int mountMode = Integer.MAX_VALUE;
            for (ExternalStorageMountPolicy policy : this.mPolicies) {
                int policyMode = policy.getMountMode(uid, packageName);
                if (policyMode == 0) {
                    return 0;
                }
                mountMode = Math.min(mountMode, policyMode);
            }
            if (mountMode == Integer.MAX_VALUE) {
                return 0;
            }
            return mountMode;
        }

        public boolean hasExternalStorage(int uid, String packageName) {
            if (uid == 1000) {
                return true;
            }
            for (ExternalStorageMountPolicy policy : this.mPolicies) {
                if (!policy.hasExternalStorage(uid, packageName)) {
                    return false;
                }
            }
            return true;
        }
    }

    private class ObbActionHandler extends Handler {
        private final List<ObbAction> mActions = new LinkedList();
        private boolean mBound = false;

        ObbActionHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            ObbAction action;
            switch (msg.what) {
                case 1:
                    action = msg.obj;
                    if (this.mBound || connectToService()) {
                        this.mActions.add(action);
                        break;
                    }
                    Slog.e(MountService.TAG, "Failed to bind to media container service");
                    action.handleError();
                    return;
                case 2:
                    if (msg.obj != null) {
                        MountService.this.mContainerService = (IMediaContainerService) msg.obj;
                    }
                    if (MountService.this.mContainerService != null) {
                        if (this.mActions.size() <= 0) {
                            Slog.w(MountService.TAG, "Empty queue");
                            break;
                        }
                        action = (ObbAction) this.mActions.get(0);
                        if (action != null) {
                            action.execute(this);
                            break;
                        }
                    }
                    Slog.e(MountService.TAG, "Cannot bind to media container service");
                    for (ObbAction action2 : this.mActions) {
                        action2.handleError();
                    }
                    this.mActions.clear();
                    break;
                    break;
                case 3:
                    if (this.mActions.size() > 0) {
                        this.mActions.remove(0);
                    }
                    if (this.mActions.size() == 0) {
                        if (this.mBound) {
                            disconnectService();
                            break;
                        }
                    }
                    MountService.this.mObbActionHandler.sendEmptyMessage(2);
                    break;
                    break;
                case 4:
                    if (this.mActions.size() > 0) {
                        if (this.mBound) {
                            disconnectService();
                        }
                        if (!connectToService()) {
                            Slog.e(MountService.TAG, "Failed to bind to media container service");
                            for (ObbAction action22 : this.mActions) {
                                action22.handleError();
                            }
                            this.mActions.clear();
                            break;
                        }
                    }
                    break;
                case 5:
                    String path = msg.obj;
                    synchronized (MountService.this.mObbMounts) {
                        List<ObbState> obbStatesToRemove = new LinkedList();
                        for (ObbState state : MountService.this.mObbPathToStateMap.values()) {
                            if (state.canonicalPath.startsWith(path)) {
                                obbStatesToRemove.add(state);
                            }
                        }
                        for (ObbState obbState : obbStatesToRemove) {
                            MountService.this.removeObbStateLocked(obbState);
                            try {
                                obbState.token.onObbResult(obbState.rawPath, obbState.nonce, 2);
                            } catch (RemoteException e) {
                                Slog.i(MountService.TAG, "Couldn't send unmount notification for  OBB: " + obbState.rawPath);
                            }
                        }
                    }
                    break;
            }
        }

        private boolean connectToService() {
            if (!MountService.this.mContext.bindServiceAsUser(new Intent().setComponent(MountService.DEFAULT_CONTAINER_COMPONENT), MountService.this.mDefContainerConn, 1, UserHandle.SYSTEM)) {
                return false;
            }
            this.mBound = true;
            return true;
        }

        private void disconnectService() {
            MountService.this.mContainerService = null;
            this.mBound = false;
            MountService.this.mContext.unbindService(MountService.this.mDefContainerConn);
        }
    }

    class ObbState implements DeathRecipient {
        final String canonicalPath;
        final int nonce;
        final int ownerGid;
        final String rawPath;
        final IObbActionListener token;

        public ObbState(String rawPath, String canonicalPath, int callingUid, IObbActionListener token, int nonce) {
            this.rawPath = rawPath;
            this.canonicalPath = canonicalPath;
            this.ownerGid = UserHandle.getSharedAppGid(callingUid);
            this.token = token;
            this.nonce = nonce;
        }

        public IBinder getBinder() {
            return this.token.asBinder();
        }

        public void binderDied() {
            MountService.this.mObbActionHandler.sendMessage(MountService.this.mObbActionHandler.obtainMessage(1, new UnmountObbAction(this, true)));
        }

        public void link() throws RemoteException {
            getBinder().linkToDeath(this, 0);
        }

        public void unlink() {
            getBinder().unlinkToDeath(this, 0);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("ObbState{");
            sb.append("rawPath=").append(this.rawPath);
            sb.append(",canonicalPath=").append(this.canonicalPath);
            sb.append(",ownerGid=").append(this.ownerGid);
            sb.append(",token=").append(this.token);
            sb.append(",binder=").append(getBinder());
            sb.append('}');
            return sb.toString();
        }
    }

    class UnmountObbAction extends ObbAction {
        private final boolean mForceUnmount;

        UnmountObbAction(ObbState obbState, boolean force) {
            super(obbState);
            this.mForceUnmount = force;
        }

        public void handleExecute() throws IOException {
            MountService.this.waitForReady();
            MountService.this.warnOnNotMounted();
            synchronized (MountService.this.mObbMounts) {
                ObbState existingState = (ObbState) MountService.this.mObbPathToStateMap.get(this.mObbState.rawPath);
            }
            if (existingState == null) {
                sendNewStatusOrIgnore(23);
            } else if (existingState.ownerGid != this.mObbState.ownerGid) {
                Slog.w(MountService.TAG, "Permission denied attempting to unmount OBB " + existingState.rawPath + " (owned by GID " + existingState.ownerGid + ")");
                sendNewStatusOrIgnore(25);
            } else {
                int rc = 0;
                try {
                    Command cmd = new Command("obb", "unmount", this.mObbState.canonicalPath);
                    if (this.mForceUnmount) {
                        cmd.appendArg("force");
                    }
                    MountService.this.mConnector.execute(cmd);
                } catch (NativeDaemonConnectorException e) {
                    int code = e.getCode();
                    if (code == VoldResponseCode.OpFailedStorageBusy) {
                        rc = -7;
                    } else if (code == VoldResponseCode.OpFailedStorageNotFound) {
                        rc = 0;
                    } else {
                        rc = -1;
                    }
                }
                if (rc == 0) {
                    synchronized (MountService.this.mObbMounts) {
                        MountService.this.removeObbStateLocked(existingState);
                    }
                    sendNewStatusOrIgnore(2);
                } else {
                    Slog.w(MountService.TAG, "Could not unmount OBB: " + existingState);
                    sendNewStatusOrIgnore(22);
                }
            }
        }

        public void handleError() {
            sendNewStatusOrIgnore(20);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("UnmountObbAction{");
            sb.append(this.mObbState);
            sb.append(",force=");
            sb.append(this.mForceUnmount);
            sb.append('}');
            return sb.toString();
        }
    }

    class VoldResponseCode {
        public static final int AsecListResult = 111;
        public static final int AsecPathResult = 211;
        public static final int BENCHMARK_RESULT = 661;
        public static final int CryptfsGetfieldResult = 113;
        public static final int DISK_CREATED = 640;
        public static final int DISK_DESTROYED = 649;
        public static final int DISK_LABEL_CHANGED = 642;
        public static final int DISK_SCANNED = 643;
        public static final int DISK_SIZE_CHANGED = 641;
        public static final int DISK_SYS_PATH_CHANGED = 644;
        public static final int FILE_SYSTE_MERROR = 855;
        public static final int MOVE_STATUS = 660;
        public static final int OpFailedMediaBlank = 402;
        public static final int OpFailedMediaCorrupt = 403;
        public static final int OpFailedNoMedia = 401;
        public static final int OpFailedStorageBusy = 405;
        public static final int OpFailedStorageNotFound = 406;
        public static final int OpFailedVolNotMounted = 404;
        public static final int ShareEnabledResult = 212;
        public static final int ShareStatusResult = 210;
        public static final int StorageUsersListResult = 112;
        public static final int TRIM_RESULT = 662;
        public static final int VOLUME_BAD_SD = 850;
        public static final int VOLUME_CREATED = 650;
        public static final int VOLUME_DESTROYED = 659;
        public static final int VOLUME_FS_LABEL_CHANGED = 654;
        public static final int VOLUME_FS_TYPE_CHANGED = 652;
        public static final int VOLUME_FS_UUID_CHANGED = 653;
        public static final int VOLUME_INTERNAL_PATH_CHANGED = 656;
        public static final int VOLUME_LOWSPEED_SPEC_SD = 856;
        public static final int VOLUME_LOW_SPEED_SD = 851;
        public static final int VOLUME_PATH_CHANGED = 655;
        public static final int VOLUME_READ_ERROR = 854;
        public static final int VOLUME_RO_SD = 852;
        public static final int VOLUME_STATE_CHANGED = 651;
        public static final int VOLUME_WRITE_ERROR = 853;
        public static final int VolumeListResult = 110;

        VoldResponseCode() {
        }
    }

    public void setVolumeUserFlags(java.lang.String r1, int r2, int r3) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.MountService.setVolumeUserFlags(java.lang.String, int, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.MountService.setVolumeUserFlags(java.lang.String, int, int):void");
    }

    private VolumeInfo findVolumeByIdOrThrow(String id) {
        synchronized (this.mLock) {
            VolumeInfo vol = (VolumeInfo) this.mVolumes.get(id);
            if (vol != null) {
                return vol;
            }
            throw new IllegalArgumentException("No volume found for ID " + id);
        }
    }

    private String findVolumeIdForPathOrThrow(String path) {
        synchronized (this.mLock) {
            int i = 0;
            while (i < this.mVolumes.size()) {
                VolumeInfo vol = (VolumeInfo) this.mVolumes.valueAt(i);
                if (vol.path == null || !path.startsWith(vol.path)) {
                    i++;
                } else {
                    String str = vol.id;
                    return str;
                }
            }
            throw new IllegalArgumentException("No volume found for path " + path);
        }
    }

    private VolumeRecord findRecordForPath(String path) {
        synchronized (this.mLock) {
            int i = 0;
            while (i < this.mVolumes.size()) {
                VolumeInfo vol = (VolumeInfo) this.mVolumes.valueAt(i);
                if (vol.path == null || !path.startsWith(vol.path)) {
                    i++;
                } else {
                    VolumeRecord volumeRecord = (VolumeRecord) this.mRecords.get(vol.fsUuid);
                    return volumeRecord;
                }
            }
            return null;
        }
    }

    private String scrubPath(String path) {
        if (path.startsWith(Environment.getDataDirectory().getAbsolutePath())) {
            return "internal";
        }
        VolumeRecord rec = findRecordForPath(path);
        if (rec == null || rec.createdMillis == 0) {
            return "unknown";
        }
        return "ext:" + ((int) ((System.currentTimeMillis() - rec.createdMillis) / UnixCalendar.WEEK_IN_MILLIS)) + "w";
    }

    private VolumeInfo findStorageForUuid(String volumeUuid) {
        StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        if (Objects.equals(StorageManager.UUID_PRIVATE_INTERNAL, volumeUuid)) {
            return storage.findVolumeById("emulated");
        }
        if (Objects.equals("primary_physical", volumeUuid)) {
            return storage.getPrimaryPhysicalVolume();
        }
        return storage.findEmulatedForPrivate(storage.findVolumeByUuid(volumeUuid));
    }

    private boolean shouldBenchmark() {
        long benchInterval = Global.getLong(this.mContext.getContentResolver(), "storage_benchmark_interval", UnixCalendar.WEEK_IN_MILLIS);
        if (benchInterval == -1) {
            return false;
        }
        if (benchInterval == 0) {
            return true;
        }
        synchronized (this.mLock) {
            int i = 0;
            while (i < this.mVolumes.size()) {
                VolumeInfo vol = (VolumeInfo) this.mVolumes.valueAt(i);
                VolumeRecord rec = (VolumeRecord) this.mRecords.get(vol.fsUuid);
                if (!vol.isMountedWritable() || rec == null || System.currentTimeMillis() - rec.lastBenchMillis < benchInterval) {
                    i++;
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    private CountDownLatch findOrCreateDiskScanLatch(String diskId) {
        CountDownLatch latch;
        synchronized (this.mLock) {
            latch = (CountDownLatch) this.mDiskScanLatches.get(diskId);
            if (latch == null) {
                latch = new CountDownLatch(1);
                this.mDiskScanLatches.put(diskId, latch);
            }
        }
        return latch;
    }

    private static String escapeNull(String arg) {
        if (TextUtils.isEmpty(arg)) {
            return "!";
        }
        if (arg.indexOf(0) == -1 && arg.indexOf(32) == -1) {
            return arg;
        }
        throw new IllegalArgumentException(arg);
    }

    public void waitForAsecScan() {
        waitForLatch(this.mAsecsScanned, "mAsecsScanned");
    }

    private void waitForReady() {
        waitForLatch(this.mConnectedSignal, "mConnectedSignal");
    }

    private void waitForLatch(CountDownLatch latch, String condition) {
        try {
            waitForLatch(latch, condition, -1);
        } catch (TimeoutException e) {
        }
    }

    private void waitForLatch(CountDownLatch latch, String condition, long timeoutMillis) throws TimeoutException {
        long startMillis = SystemClock.elapsedRealtime();
        while (!latch.await(5000, TimeUnit.MILLISECONDS)) {
            try {
                Slog.w(TAG, "Thread " + Thread.currentThread().getName() + " still waiting for " + condition + "...");
            } catch (InterruptedException e) {
                Slog.w(TAG, "Interrupt while waiting for " + condition);
            }
            if (timeoutMillis > 0 && SystemClock.elapsedRealtime() > startMillis + timeoutMillis) {
                throw new TimeoutException("Thread " + Thread.currentThread().getName() + " gave up waiting for " + condition + " after " + timeoutMillis + "ms");
            }
        }
    }

    private boolean isReady() {
        try {
            return this.mConnectedSignal.await(0, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    private void handleSystemReady() {
        initIfReadyAndConnected();
        resetIfReadyAndConnected();
        MountServiceIdler.scheduleIdlePass(this.mContext);
    }

    @Deprecated
    private void killMediaProvider(List<UserInfo> users) {
        if (users != null) {
            long token = Binder.clearCallingIdentity();
            try {
                for (UserInfo user : users) {
                    if (!user.isSystemOnly()) {
                        ProviderInfo provider = this.mPms.resolveContentProvider("media", 786432, user.id);
                        if (provider != null) {
                            try {
                                ActivityManagerNative.getDefault().killApplication(provider.applicationInfo.packageName, UserHandle.getAppId(provider.applicationInfo.uid), -1, "vold reset");
                                break;
                            } catch (RemoteException e) {
                            }
                        } else {
                            continue;
                        }
                    }
                }
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    private void addInternalVolumeLocked() {
        VolumeInfo internal = new VolumeInfo("private", 1, null, null);
        internal.state = 2;
        internal.path = Environment.getDataDirectory().getAbsolutePath();
        this.mVolumes.put(internal.id, internal);
    }

    private void initIfReadyAndConnected() {
        Slog.d(TAG, "Thinking about init, mSystemReady=" + this.mSystemReady + ", mDaemonConnected=" + this.mDaemonConnected);
        if (this.mSystemReady && this.mDaemonConnected && !StorageManager.isFileEncryptedNativeOnly()) {
            boolean initLocked = StorageManager.isFileEncryptedEmulatedOnly();
            Slog.d(TAG, "Setting up emulation state, initlocked=" + initLocked);
            for (UserInfo user : ((UserManager) this.mContext.getSystemService(UserManager.class)).getUsers()) {
                if (initLocked) {
                    try {
                        this.mCryptConnector.execute("cryptfs", "lock_user_key", Integer.valueOf(user.id));
                    } catch (NativeDaemonConnectorException e) {
                        Slog.w(TAG, "Failed to init vold", e);
                    }
                } else {
                    this.mCryptConnector.execute("cryptfs", "unlock_user_key", Integer.valueOf(user.id), Integer.valueOf(user.serialNumber), "!", "!");
                }
            }
        }
    }

    private void resetIfReadyAndConnected() {
        Slog.d(TAG, "Thinking about reset, mSystemReady=" + this.mSystemReady + ", mDaemonConnected=" + this.mDaemonConnected);
        if (this.mSystemReady && this.mDaemonConnected) {
            List<UserInfo> users = ((UserManager) this.mContext.getSystemService(UserManager.class)).getUsers();
            killMediaProvider(users);
            synchronized (this.mLock) {
                int[] systemUnlockedUsers = this.mSystemUnlockedUsers;
                this.mDisks.clear();
                this.mVolumes.clear();
                addInternalVolumeLocked();
            }
            try {
                this.mConnector.execute(TAG_VOLUME, "reset");
                for (UserInfo user : users) {
                    this.mConnector.execute(TAG_VOLUME, "user_added", Integer.valueOf(user.id), Integer.valueOf(user.serialNumber));
                }
                for (int userId : systemUnlockedUsers) {
                    this.mConnector.execute(TAG_VOLUME, "user_started", Integer.valueOf(userId));
                }
            } catch (NativeDaemonConnectorException e) {
                Slog.w(TAG, "Failed to reset vold", e);
            }
        }
    }

    private void onUnlockUser(int userId) {
        Slog.d(TAG, "onUnlockUser " + userId);
        try {
            this.mConnector.execute(TAG_VOLUME, "user_started", Integer.valueOf(userId));
        } catch (NativeDaemonConnectorException e) {
        }
        Slog.d(TAG, "hava send user_started to vold , userId = " + userId);
        this.mUserStartedFinish = true;
        synchronized (this.mVolumes) {
            for (int i = 0; i < this.mVolumes.size(); i++) {
                VolumeInfo vol = (VolumeInfo) this.mVolumes.valueAt(i);
                if (vol.isVisibleForRead(userId) && vol.isMountedReadable()) {
                    StorageVolume userVol = vol.buildStorageVolume(this.mContext, userId, false);
                    this.mHandler.obtainMessage(6, userVol).sendToTarget();
                    String envState = VolumeInfo.getEnvironmentForState(vol.getState());
                    this.mCallbacks.notifyStorageStateChanged(userVol.getPath(), envState, envState);
                }
            }
            this.mSystemUnlockedUsers = ArrayUtils.appendInt(this.mSystemUnlockedUsers, userId);
        }
    }

    private void onCleanupUser(int userId) {
        Slog.d(TAG, "onCleanupUser " + userId);
        try {
            this.mConnector.execute(TAG_VOLUME, "user_stopped", Integer.valueOf(userId));
        } catch (NativeDaemonConnectorException e) {
        }
        synchronized (this.mVolumes) {
            this.mSystemUnlockedUsers = ArrayUtils.removeInt(this.mSystemUnlockedUsers, userId);
        }
    }

    void runIdleMaintenance(Runnable callback) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(4, callback));
    }

    public void runMaintenance() {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        runIdleMaintenance(null);
    }

    public long lastMaintenance() {
        return this.mLastMaintenance;
    }

    public void onDaemonConnected() {
        this.mDaemonConnected = true;
        this.mHandler.obtainMessage(2).sendToTarget();
    }

    private void handleDaemonConnected() {
        initIfReadyAndConnected();
        resetIfReadyAndConnected();
        this.mConnectedSignal.countDown();
        if (this.mConnectedSignal.getCount() == 0) {
            if ("".equals(SystemProperties.get("vold.encrypt_progress"))) {
                copyLocaleFromMountService();
            }
            if (this.mCustMountService != null) {
                Slog.i(TAG, "physicalWarnOnNotMounted , start ");
                this.mCustMountService.physicalWarnOnNotMounted(this.mVolumes, this.mConnector);
            }
            this.mPms.scanAvailableAsecs();
            this.mAsecsScanned.countDown();
        }
    }

    private void copyLocaleFromMountService() {
        try {
            String systemLocale = getField("SystemLocale");
            if (!TextUtils.isEmpty(systemLocale)) {
                Slog.d(TAG, "Got locale " + systemLocale + " from mount service");
                Locale locale = Locale.forLanguageTag(systemLocale);
                Configuration config = new Configuration();
                config.setLocale(locale);
                try {
                    ActivityManagerNative.getDefault().updatePersistentConfiguration(config);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error setting system locale from mount service", e);
                }
                Slog.d(TAG, "Setting system properties to " + systemLocale + " from mount service");
                SystemProperties.set("persist.sys.locale", locale.toLanguageTag());
            }
        } catch (RemoteException e2) {
        } catch (IllegalStateException e3) {
            Slog.e(TAG, "copyLocaleFromMountService, getField threw IllegalStateException");
        }
    }

    public boolean onCheckHoldWakeLock(int code) {
        return false;
    }

    public boolean onEvent(int code, String raw, String[] cooked) {
        boolean onEventLocked;
        synchronized (this.mLock) {
            onEventLocked = onEventLocked(code, raw, cooked);
        }
        return onEventLocked;
    }

    private boolean onEventLocked(int code, String raw, String[] cooked) {
        StringBuilder cookedBuilder = new StringBuilder();
        cookedBuilder.append("onEvent::");
        if (cooked == null) {
            return false;
        }
        cookedBuilder.append(" cooked = ");
        for (String str : cooked) {
            cookedBuilder.append(" ").append(str);
        }
        Flog.i(1002, cookedBuilder.toString());
        String id;
        DiskInfo disk;
        StringBuilder builder;
        int i;
        VolumeInfo vol;
        int oldState;
        int newState;
        String path;
        DropBoxManager dropBox;
        VolumeRecord rec;
        switch (code) {
            case VoldResponseCode.DISK_CREATED /*640*/:
                if (cooked.length == 3) {
                    id = cooked[1];
                    int flags = Integer.parseInt(cooked[2]);
                    if (SystemProperties.getBoolean("persist.fw.force_adoptable", true) || this.mForceAdoptable) {
                        flags |= 1;
                    }
                    this.mDisks.put(id, new DiskInfo(id, flags));
                    break;
                }
                break;
            case VoldResponseCode.DISK_SIZE_CHANGED /*641*/:
                if (cooked.length == 3) {
                    disk = (DiskInfo) this.mDisks.get(cooked[1]);
                    if (disk != null) {
                        disk.size = Long.parseLong(cooked[2]);
                        break;
                    }
                }
                break;
            case VoldResponseCode.DISK_LABEL_CHANGED /*642*/:
                disk = (DiskInfo) this.mDisks.get(cooked[1]);
                if (disk != null) {
                    builder = new StringBuilder();
                    for (i = 2; i < cooked.length; i++) {
                        builder.append(cooked[i]).append(' ');
                    }
                    disk.label = builder.toString().trim();
                    break;
                }
                break;
            case VoldResponseCode.DISK_SCANNED /*643*/:
                if (cooked.length == 2) {
                    disk = (DiskInfo) this.mDisks.get(cooked[1]);
                    if (disk != null) {
                        onDiskScannedLocked(disk);
                        break;
                    }
                }
                break;
            case VoldResponseCode.DISK_SYS_PATH_CHANGED /*644*/:
                if (cooked.length == 3) {
                    disk = (DiskInfo) this.mDisks.get(cooked[1]);
                    if (disk != null) {
                        disk.sysPath = cooked[2];
                        break;
                    }
                }
                break;
            case VoldResponseCode.DISK_DESTROYED /*649*/:
                if (cooked.length == 2) {
                    disk = (DiskInfo) this.mDisks.remove(cooked[1]);
                    if (disk != null) {
                        this.mCallbacks.notifyDiskDestroyed(disk);
                        break;
                    }
                }
                break;
            case VoldResponseCode.VOLUME_CREATED /*650*/:
                id = cooked[1];
                int type = Integer.parseInt(cooked[2]);
                String str2 = id;
                int i2 = type;
                VolumeInfo volumeInfo = new VolumeInfo(str2, i2, (DiskInfo) this.mDisks.get(TextUtils.nullIfEmpty(cooked[3])), TextUtils.nullIfEmpty(cooked[4]));
                this.mVolumes.put(id, volumeInfo);
                onVolumeCreatedLocked(volumeInfo);
                break;
            case 651:
                if (cooked.length == 3) {
                    vol = (VolumeInfo) this.mVolumes.get(cooked[1]);
                    if (vol != null) {
                        oldState = vol.state;
                        newState = Integer.parseInt(cooked[2]);
                        vol.state = newState;
                        onVolumeStateChangedLocked(vol, oldState, newState);
                        break;
                    }
                }
                break;
            case VoldResponseCode.VOLUME_FS_TYPE_CHANGED /*652*/:
                if (cooked.length == 3) {
                    vol = (VolumeInfo) this.mVolumes.get(cooked[1]);
                    if (vol != null) {
                        vol.fsType = cooked[2];
                        break;
                    }
                }
                break;
            case VoldResponseCode.VOLUME_FS_UUID_CHANGED /*653*/:
                if (cooked.length == 3) {
                    vol = (VolumeInfo) this.mVolumes.get(cooked[1]);
                    if (vol != null) {
                        vol.fsUuid = cooked[2];
                        break;
                    }
                }
                break;
            case VoldResponseCode.VOLUME_FS_LABEL_CHANGED /*654*/:
                vol = (VolumeInfo) this.mVolumes.get(cooked[1]);
                if (vol != null) {
                    builder = new StringBuilder();
                    for (i = 2; i < cooked.length; i++) {
                        builder.append(cooked[i]).append(' ');
                    }
                    vol.fsLabel = builder.toString().trim();
                    break;
                }
                break;
            case VoldResponseCode.VOLUME_PATH_CHANGED /*655*/:
                if (cooked.length == 3) {
                    vol = (VolumeInfo) this.mVolumes.get(cooked[1]);
                    if (vol != null) {
                        vol.path = cooked[2];
                        break;
                    }
                }
                break;
            case VoldResponseCode.VOLUME_INTERNAL_PATH_CHANGED /*656*/:
                if (cooked.length == 3) {
                    vol = (VolumeInfo) this.mVolumes.get(cooked[1]);
                    if (vol != null) {
                        vol.internalPath = cooked[2];
                        break;
                    }
                }
                break;
            case VoldResponseCode.VOLUME_DESTROYED /*659*/:
                if (cooked.length == 2) {
                    this.mVolumes.remove(cooked[1]);
                    break;
                }
                break;
            case 660:
                onMoveStatusLocked(Integer.parseInt(cooked[1]));
                break;
            case 661:
                if (cooked.length == 7) {
                    path = cooked[1];
                    String ident = cooked[2];
                    long create = Long.parseLong(cooked[3]);
                    long drop = Long.parseLong(cooked[4]);
                    long run = Long.parseLong(cooked[5]);
                    long destroy = Long.parseLong(cooked[6]);
                    dropBox = (DropBoxManager) this.mContext.getSystemService(DropBoxManager.class);
                    if (dropBox != null) {
                        dropBox.addText(TAG_STORAGE_BENCHMARK, scrubPath(path) + " " + ident + " " + create + " " + run + " " + destroy);
                    }
                    rec = findRecordForPath(path);
                    if (rec != null) {
                        rec.lastBenchMillis = System.currentTimeMillis();
                        writeSettingsLocked();
                        break;
                    }
                }
                break;
            case VoldResponseCode.TRIM_RESULT /*662*/:
                if (cooked.length == 4) {
                    path = cooked[1];
                    long bytes = Long.parseLong(cooked[2]);
                    long time = Long.parseLong(cooked[3]);
                    dropBox = (DropBoxManager) this.mContext.getSystemService(DropBoxManager.class);
                    if (dropBox != null) {
                        dropBox.addText(TAG_STORAGE_TRIM, scrubPath(path) + " " + bytes + " " + time);
                    }
                    rec = findRecordForPath(path);
                    if (rec != null) {
                        rec.lastTrimMillis = System.currentTimeMillis();
                        writeSettingsLocked();
                        break;
                    }
                }
                break;
            case VoldResponseCode.VOLUME_BAD_SD /*850*/:
            case VoldResponseCode.VOLUME_LOW_SPEED_SD /*851*/:
            case VoldResponseCode.VOLUME_RO_SD /*852*/:
            case VoldResponseCode.VOLUME_WRITE_ERROR /*853*/:
            case VoldResponseCode.VOLUME_READ_ERROR /*854*/:
            case VoldResponseCode.FILE_SYSTE_MERROR /*855*/:
            case VoldResponseCode.VOLUME_LOWSPEED_SPEC_SD /*856*/:
                if (cooked != null && cooked.length == 3) {
                    vol = (VolumeInfo) this.mVolumes.get(cooked[1]);
                    if (vol != null) {
                        oldState = vol.state;
                        newState = Integer.parseInt(cooked[2]);
                        Intent intent = new Intent("android.intent.action.MEDIA_ABNORMAL_SD");
                        intent.putExtra("android.os.storage.extra.STORAGE_VOLUME", vol);
                        intent.putExtra("android.os.storage.extra.VOLUME_OLD_STATE", oldState);
                        intent.putExtra("android.os.storage.extra.VOLUME_NEW_STATE", newState);
                        Flog.i(1002, "AbNormal SD card volumeInfo = " + vol.toString() + ",errorCode = " + newState);
                        this.mHandler.obtainMessage(11, intent).sendToTarget();
                        break;
                    }
                }
                break;
            default:
                Slog.d(TAG, "Unhandled vold event " + code);
                break;
        }
        return true;
    }

    private void onDiskScannedLocked(DiskInfo disk) {
        int volumeCount = 0;
        for (int i = 0; i < this.mVolumes.size(); i++) {
            if (Objects.equals(disk.id, ((VolumeInfo) this.mVolumes.valueAt(i)).getDiskId())) {
                volumeCount++;
            }
        }
        Intent intent = new Intent("android.os.storage.action.DISK_SCANNED");
        intent.addFlags(83886080);
        intent.putExtra("android.os.storage.extra.DISK_ID", disk.id);
        intent.putExtra("android.os.storage.extra.VOLUME_COUNT", volumeCount);
        this.mHandler.obtainMessage(7, intent).sendToTarget();
        CountDownLatch latch = (CountDownLatch) this.mDiskScanLatches.remove(disk.id);
        if (latch != null) {
            latch.countDown();
        }
        disk.volumeCount = volumeCount;
        this.mCallbacks.notifyDiskScanned(disk, volumeCount);
    }

    private void onVolumeCreatedLocked(VolumeInfo vol) {
        if (this.mPms.isOnlyCoreApps()) {
            Slog.d(TAG, "System booted in core-only mode; ignoring volume " + vol.getId());
            return;
        }
        if (vol.type == 2) {
            VolumeInfo privateVol = ((StorageManager) this.mContext.getSystemService(StorageManager.class)).findPrivateForEmulated(vol);
            if (Objects.equals(StorageManager.UUID_PRIVATE_INTERNAL, this.mPrimaryStorageUuid) && "private".equals(privateVol.id)) {
                Slog.v(TAG, "Found primary storage at " + vol);
                vol.mountFlags |= 1;
                vol.mountFlags |= 2;
                this.mHandler.obtainMessage(5, vol).sendToTarget();
            } else if (Objects.equals(privateVol.fsUuid, this.mPrimaryStorageUuid)) {
                Slog.v(TAG, "Found primary storage at " + vol);
                vol.mountFlags |= 1;
                vol.mountFlags |= 2;
                this.mHandler.obtainMessage(5, vol).sendToTarget();
            }
        } else if (vol.type == 0) {
            if (Objects.equals("primary_physical", this.mPrimaryStorageUuid) && vol.disk.isDefaultPrimary()) {
                Slog.v(TAG, "Found primary storage at " + vol);
                vol.mountFlags |= 1;
                vol.mountFlags |= 2;
            }
            if (vol.disk.isAdoptable()) {
                vol.mountFlags |= 2;
            }
            vol.mountUserId = this.mCurrentUserId;
            this.mHandler.obtainMessage(5, vol).sendToTarget();
        } else if (vol.type == 1) {
            this.mHandler.obtainMessage(5, vol).sendToTarget();
        } else {
            Slog.d(TAG, "Skipping automatic mounting of " + vol);
        }
    }

    private boolean isBroadcastWorthy(VolumeInfo vol) {
        switch (vol.getType()) {
            case 0:
            case 1:
            case 2:
                switch (vol.getState()) {
                    case 0:
                    case 2:
                    case 3:
                    case 5:
                    case 6:
                    case 8:
                        return true;
                    default:
                        return false;
                }
            default:
                return false;
        }
    }

    private void onVolumeStateChangedLocked(VolumeInfo vol, int oldState, int newState) {
        if (vol.isMountedReadable() && !TextUtils.isEmpty(vol.fsUuid)) {
            VolumeRecord rec = (VolumeRecord) this.mRecords.get(vol.fsUuid);
            if (rec == null) {
                rec = new VolumeRecord(vol.type, vol.fsUuid);
                rec.partGuid = vol.partGuid;
                rec.createdMillis = System.currentTimeMillis();
                if (vol.type == 1) {
                    rec.nickname = vol.disk.getDescription();
                }
                this.mRecords.put(rec.fsUuid, rec);
                writeSettingsLocked();
            } else if (TextUtils.isEmpty(rec.partGuid)) {
                rec.partGuid = vol.partGuid;
                writeSettingsLocked();
            }
        }
        this.mCallbacks.notifyVolumeStateChanged(vol, oldState, newState);
        if (this.mBootCompleted && isBroadcastWorthy(vol)) {
            Flog.i(1002, "onVolumeStateChangedLocked volumeInfo = " + vol.toString());
            Intent intent = new Intent("android.os.storage.action.VOLUME_STATE_CHANGED");
            intent.putExtra("android.os.storage.extra.VOLUME_ID", vol.id);
            intent.putExtra("android.os.storage.extra.VOLUME_STATE", newState);
            intent.putExtra("android.os.storage.extra.FS_UUID", vol.fsUuid);
            intent.addFlags(83886080);
            this.mHandler.obtainMessage(7, intent).sendToTarget();
        }
        String oldStateEnv = VolumeInfo.getEnvironmentForState(oldState);
        String newStateEnv = VolumeInfo.getEnvironmentForState(newState);
        if (!Objects.equals(oldStateEnv, newStateEnv)) {
            for (int userId : this.mSystemUnlockedUsers) {
                if (vol.isVisibleForRead(userId)) {
                    StorageVolume userVol = vol.buildStorageVolume(this.mContext, userId, false);
                    this.mHandler.obtainMessage(6, userVol).sendToTarget();
                    this.mCallbacks.notifyStorageStateChanged(userVol.getPath(), oldStateEnv, newStateEnv);
                }
            }
        }
        if (vol.type == 0 && vol.state == 5) {
            this.mObbActionHandler.sendMessage(this.mObbActionHandler.obtainMessage(5, vol.path));
        }
    }

    private void onMoveStatusLocked(int status) {
        if (this.mMoveCallback == null) {
            Slog.w(TAG, "Odd, status but no move requested");
            return;
        }
        try {
            this.mMoveCallback.onStatusChanged(-1, status, -1);
        } catch (RemoteException e) {
        }
        if (status == 82) {
            Slog.d(TAG, "Move to " + this.mMoveTargetUuid + " copy phase finshed; persisting");
            this.mPrimaryStorageUuid = this.mMoveTargetUuid;
            writeSettingsLocked();
        }
        if (PackageManager.isMoveStatusFinished(status)) {
            Slog.d(TAG, "Move to " + this.mMoveTargetUuid + " finished with status " + status);
            this.mMoveCallback = null;
            this.mMoveTargetUuid = null;
        }
    }

    private void enforcePermission(String perm) {
        this.mContext.enforceCallingOrSelfPermission(perm, perm);
    }

    private boolean isMountDisallowed(VolumeInfo vol) {
        if (vol.type == 0 || vol.type == 1) {
            return ((UserManager) this.mContext.getSystemService(UserManager.class)).hasUserRestriction("no_physical_media", Binder.getCallingUserHandle());
        }
        return false;
    }

    private boolean isSDCardDisable(VolumeInfo vol) {
        if (!"wait_unlock".equals(getCryptsdState()) || vol.type != 0 || vol.getDisk() == null || !vol.getDisk().isSd()) {
            return false;
        }
        Slog.i(TAG, "wait_unlock, disallow domount");
        return true;
    }

    private String getCryptsdState() {
        long ident = Binder.clearCallingIdentity();
        try {
            if (SystemProperties.getBoolean("ro.config.support_sdcard_crypt", true)) {
                String state = SystemProperties.get("vold.cryptsd.state", "none");
                Slog.i(TAG, "CryptsdState: " + state);
                Binder.restoreCallingIdentity(ident);
                return state;
            }
            String str = "none";
            return str;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void enforceAdminUser() {
        UserManager um = (UserManager) this.mContext.getSystemService("user");
        int callingUserId = UserHandle.getCallingUserId();
        long token = Binder.clearCallingIdentity();
        try {
            boolean isAdmin = um.getUserInfo(callingUserId).isAdmin();
            if (!isAdmin) {
                throw new SecurityException("Only admin users can adopt sd cards");
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public MountService(Context context) {
        sSelf = this;
        this.mContext = context;
        this.mCallbacks = new Callbacks(FgThread.get().getLooper());
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mPms = (PackageManagerService) ServiceManager.getService(HwBroadcastRadarUtil.KEY_PACKAGE);
        HandlerThread hthread = new HandlerThread(TAG);
        hthread.start();
        this.mHandler = new MountServiceHandler(hthread.getLooper());
        this.mObbActionHandler = new ObbActionHandler(IoThread.get().getLooper());
        this.mLastMaintenanceFile = new File(new File(Environment.getDataDirectory(), "system"), LAST_FSTRIM_FILE);
        if (this.mLastMaintenanceFile.exists()) {
            this.mLastMaintenance = this.mLastMaintenanceFile.lastModified();
        } else {
            try {
                new FileOutputStream(this.mLastMaintenanceFile).close();
            } catch (IOException e) {
                Slog.e(TAG, "Unable to create fstrim record " + this.mLastMaintenanceFile.getPath());
            }
        }
        this.mSettingsFile = new AtomicFile(new File(Environment.getDataSystemDirectory(), "storage.xml"));
        synchronized (this.mLock) {
            readSettingsLocked();
        }
        LocalServices.addService(MountServiceInternal.class, this.mMountServiceInternal);
        this.mConnector = new NativeDaemonConnector(this, "vold", SystemService.PHASE_SYSTEM_SERVICES_READY, VOLD_TAG, 25, null);
        this.mConnector.setDebug(true);
        this.mConnector.setWarnIfHeld(this.mLock);
        this.mConnectorThread = new Thread(this.mConnector, VOLD_TAG);
        this.mCryptConnector = new NativeDaemonConnector(this, "cryptd", SystemService.PHASE_SYSTEM_SERVICES_READY, CRYPTD_TAG, 25, null);
        this.mCryptConnector.setDebug(true);
        this.mCryptConnectorThread = new Thread(this.mCryptConnector, CRYPTD_TAG);
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_ADDED");
        userFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiver(this.mUserReceiver, userFilter, null, this.mHandler);
        IntentFilter bootFilter = new IntentFilter();
        bootFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(this.mToVoldBroadcastReceiver, bootFilter, null, this.mHandler);
        synchronized (this.mLock) {
            addInternalVolumeLocked();
        }
    }

    private void start() {
        this.mConnectorThread.start();
        this.mCryptConnectorThread.start();
    }

    private void systemReady() {
        this.mSystemReady = true;
        this.mHandler.obtainMessage(1).sendToTarget();
    }

    private void bootCompleted() {
        this.mBootCompleted = true;
    }

    private String getDefaultPrimaryStorageUuid() {
        if (SystemProperties.getBoolean("ro.vold.primary_physical", false)) {
            return "primary_physical";
        }
        return StorageManager.UUID_PRIVATE_INTERNAL;
    }

    private void readSettingsLocked() {
        this.mRecords.clear();
        this.mPrimaryStorageUuid = getDefaultPrimaryStorageUuid();
        this.mForceAdoptable = false;
        AutoCloseable autoCloseable = null;
        try {
            autoCloseable = this.mSettingsFile.openRead();
            XmlPullParser in = Xml.newPullParser();
            in.setInput(autoCloseable, StandardCharsets.UTF_8.name());
            while (true) {
                int type = in.next();
                if (type == 1) {
                    break;
                } else if (type == 2) {
                    String tag = in.getName();
                    if (TAG_VOLUMES.equals(tag)) {
                        int version = XmlUtils.readIntAttribute(in, ATTR_VERSION, 1);
                        boolean validAttr = version < 3 ? version >= 2 && !SystemProperties.getBoolean("ro.vold.primary_physical", false) : true;
                        if (validAttr) {
                            this.mPrimaryStorageUuid = XmlUtils.readStringAttribute(in, ATTR_PRIMARY_STORAGE_UUID);
                        }
                        this.mForceAdoptable = XmlUtils.readBooleanAttribute(in, ATTR_FORCE_ADOPTABLE, false);
                    } else if (TAG_VOLUME.equals(tag)) {
                        VolumeRecord rec = readVolumeRecord(in);
                        this.mRecords.put(rec.fsUuid, rec);
                    }
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e2) {
            Slog.wtf(TAG, "Failed reading metadata", e2);
        } catch (XmlPullParserException e3) {
            Slog.wtf(TAG, "Failed reading metadata", e3);
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    private void writeSettingsLocked() {
        try {
            FileOutputStream fos = this.mSettingsFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, StandardCharsets.UTF_8.name());
            out.startDocument(null, Boolean.valueOf(true));
            out.startTag(null, TAG_VOLUMES);
            XmlUtils.writeIntAttribute(out, ATTR_VERSION, 3);
            XmlUtils.writeStringAttribute(out, ATTR_PRIMARY_STORAGE_UUID, this.mPrimaryStorageUuid);
            XmlUtils.writeBooleanAttribute(out, ATTR_FORCE_ADOPTABLE, this.mForceAdoptable);
            int size = this.mRecords.size();
            for (int i = 0; i < size; i++) {
                writeVolumeRecord(out, (VolumeRecord) this.mRecords.valueAt(i));
            }
            out.endTag(null, TAG_VOLUMES);
            out.endDocument();
            this.mSettingsFile.finishWrite(fos);
        } catch (IOException e) {
            if (null != null) {
                this.mSettingsFile.failWrite(null);
            }
        }
    }

    public static VolumeRecord readVolumeRecord(XmlPullParser in) throws IOException {
        VolumeRecord meta = new VolumeRecord(XmlUtils.readIntAttribute(in, "type"), XmlUtils.readStringAttribute(in, ATTR_FS_UUID));
        meta.partGuid = XmlUtils.readStringAttribute(in, ATTR_PART_GUID);
        meta.nickname = XmlUtils.readStringAttribute(in, ATTR_NICKNAME);
        meta.userFlags = XmlUtils.readIntAttribute(in, ATTR_USER_FLAGS);
        meta.createdMillis = XmlUtils.readLongAttribute(in, ATTR_CREATED_MILLIS);
        meta.lastTrimMillis = XmlUtils.readLongAttribute(in, ATTR_LAST_TRIM_MILLIS);
        meta.lastBenchMillis = XmlUtils.readLongAttribute(in, ATTR_LAST_BENCH_MILLIS);
        return meta;
    }

    public static void writeVolumeRecord(XmlSerializer out, VolumeRecord rec) throws IOException {
        out.startTag(null, TAG_VOLUME);
        XmlUtils.writeIntAttribute(out, "type", rec.type);
        XmlUtils.writeStringAttribute(out, ATTR_FS_UUID, rec.fsUuid);
        XmlUtils.writeStringAttribute(out, ATTR_PART_GUID, rec.partGuid);
        XmlUtils.writeStringAttribute(out, ATTR_NICKNAME, rec.nickname);
        XmlUtils.writeIntAttribute(out, ATTR_USER_FLAGS, rec.userFlags);
        XmlUtils.writeLongAttribute(out, ATTR_CREATED_MILLIS, rec.createdMillis);
        XmlUtils.writeLongAttribute(out, ATTR_LAST_TRIM_MILLIS, rec.lastTrimMillis);
        XmlUtils.writeLongAttribute(out, ATTR_LAST_BENCH_MILLIS, rec.lastBenchMillis);
        out.endTag(null, TAG_VOLUME);
    }

    public void registerListener(IMountServiceListener listener) {
        this.mCallbacks.register(listener);
    }

    public void unregisterListener(IMountServiceListener listener) {
        this.mCallbacks.unregister(listener);
    }

    public void shutdown(IMountShutdownObserver observer) {
        enforcePermission("android.permission.SHUTDOWN");
        Slog.i(TAG, "Shutting down");
        this.mHandler.obtainMessage(3, observer).sendToTarget();
    }

    public boolean isUsbMassStorageConnected() {
        throw new UnsupportedOperationException();
    }

    public void setUsbMassStorageEnabled(boolean enable) {
        throw new UnsupportedOperationException();
    }

    public boolean isUsbMassStorageEnabled() {
        throw new UnsupportedOperationException();
    }

    public String getVolumeState(String mountPoint) {
        throw new UnsupportedOperationException();
    }

    public boolean isExternalStorageEmulated() {
        throw new UnsupportedOperationException();
    }

    private void bootCompleteToVold() {
        try {
            Flog.i(1002, "begin send BootCompleteToVold");
            this.mConnector.execute(TAG_VOLUME, "bootcompleted");
        } catch (NativeDaemonConnectorException e) {
            Flog.e(1002, "vold not realizate bootcom");
        } catch (Exception e2) {
            Flog.e(1002, "other Exception ");
        }
    }

    public int mountVolume(String path) {
        mount(findVolumeIdForPathOrThrow(path));
        return 0;
    }

    public void unmountVolume(String path, boolean force, boolean removeEncryption) {
        unmount(findVolumeIdForPathOrThrow(path));
    }

    public int formatVolume(String path) {
        format(findVolumeIdForPathOrThrow(path));
        return 0;
    }

    public void mount(String volId) {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        waitForReady();
        VolumeInfo vol = findVolumeByIdOrThrow(volId);
        if (isMountDisallowed(vol)) {
            throw new SecurityException("Mounting " + volId + " restricted by policy");
        } else if (HwDeviceManager.disallowOp(12) && !vol.id.contains("public:179") && vol.id.contains("public:")) {
            Slog.i(TAG, "Usb mass device is disabled by dpm");
            throw new SecurityException("Mounting " + volId + " failed because of USB otg is disabled by dpm");
        } else if (isSDCardDisable(vol)) {
            throw new SecurityException("Mounting " + volId + " restricted by cryptsd");
        } else {
            try {
                this.mConnector.execute(TAG_VOLUME, "mount", vol.id, Integer.valueOf(vol.mountFlags), Integer.valueOf(vol.mountUserId));
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
    }

    public void unmount(String volId) {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        waitForReady();
        VolumeInfo vol = findVolumeByIdOrThrow(volId);
        if (vol.isPrimaryPhysical() || (this.mCustMountService != null && this.mCustMountService.isSdVol(vol))) {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mUnmountLock) {
                    this.mUnmountSignal = new CountDownLatch(1);
                    this.mPms.updateExternalMediaStatus(false, true);
                    waitForLatch(this.mUnmountSignal, "mUnmountSignal");
                    this.mUnmountSignal = null;
                }
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
        try {
            this.mConnector.execute(TAG_VOLUME, "unmount", vol.id);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void format(String volId) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        waitForReady();
        VolumeInfo vol = findVolumeByIdOrThrow(volId);
        try {
            this.mConnector.execute(TAG_VOLUME, "format", vol.id, "auto");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public long benchmark(String volId) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        waitForReady();
        try {
            return Long.parseLong(this.mConnector.execute(180000, TAG_VOLUME, "benchmark", volId).getMessage());
        } catch (NativeDaemonTimeoutException e) {
            return JobStatus.NO_LATEST_RUNTIME;
        } catch (NativeDaemonConnectorException e2) {
            throw e2.rethrowAsParcelableException();
        }
    }

    public void partitionPublic(String diskId) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        waitForReady();
        CountDownLatch latch = findOrCreateDiskScanLatch(diskId);
        try {
            this.mConnector.execute(TAG_VOLUME, "partition", diskId, "public");
            waitForLatch(latch, "partitionPublic", 180000);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        } catch (TimeoutException e2) {
            throw new IllegalStateException(e2);
        }
    }

    public void partitionPrivate(String diskId) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        enforceAdminUser();
        waitForReady();
        CountDownLatch latch = findOrCreateDiskScanLatch(diskId);
        try {
            this.mConnector.execute(TAG_VOLUME, "partition", diskId, "private");
            waitForLatch(latch, "partitionPrivate", 180000);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        } catch (TimeoutException e2) {
            throw new IllegalStateException(e2);
        }
    }

    public void partitionMixed(String diskId, int ratio) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        enforceAdminUser();
        waitForReady();
        CountDownLatch latch = findOrCreateDiskScanLatch(diskId);
        try {
            this.mConnector.execute(TAG_VOLUME, "partition", diskId, "mixed", Integer.valueOf(ratio));
            waitForLatch(latch, "partitionMixed", 180000);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        } catch (TimeoutException e2) {
            throw new IllegalStateException(e2);
        }
    }

    public void setVolumeNickname(String fsUuid, String nickname) {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        waitForReady();
        Preconditions.checkNotNull(fsUuid);
        synchronized (this.mLock) {
            VolumeRecord rec = (VolumeRecord) this.mRecords.get(fsUuid);
            rec.nickname = nickname;
            this.mCallbacks.notifyVolumeRecordChanged(rec);
            writeSettingsLocked();
        }
    }

    public void forgetVolume(String fsUuid) {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        waitForReady();
        Preconditions.checkNotNull(fsUuid);
        synchronized (this.mLock) {
            VolumeRecord rec = (VolumeRecord) this.mRecords.remove(fsUuid);
            if (!(rec == null || TextUtils.isEmpty(rec.partGuid))) {
                this.mHandler.obtainMessage(9, rec.partGuid).sendToTarget();
            }
            this.mCallbacks.notifyVolumeForgotten(fsUuid);
            if (Objects.equals(this.mPrimaryStorageUuid, fsUuid)) {
                this.mPrimaryStorageUuid = getDefaultPrimaryStorageUuid();
                this.mHandler.obtainMessage(10).sendToTarget();
            }
            writeSettingsLocked();
        }
    }

    public void forgetAllVolumes() {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        waitForReady();
        synchronized (this.mLock) {
            for (int i = 0; i < this.mRecords.size(); i++) {
                String fsUuid = (String) this.mRecords.keyAt(i);
                VolumeRecord rec = (VolumeRecord) this.mRecords.valueAt(i);
                if (!TextUtils.isEmpty(rec.partGuid)) {
                    this.mHandler.obtainMessage(9, rec.partGuid).sendToTarget();
                }
                this.mCallbacks.notifyVolumeForgotten(fsUuid);
            }
            this.mRecords.clear();
            if (!Objects.equals(StorageManager.UUID_PRIVATE_INTERNAL, this.mPrimaryStorageUuid)) {
                this.mPrimaryStorageUuid = getDefaultPrimaryStorageUuid();
            }
            writeSettingsLocked();
            this.mHandler.obtainMessage(10).sendToTarget();
        }
    }

    private void forgetPartition(String partGuid) {
        try {
            this.mConnector.execute(TAG_VOLUME, "forget_partition", partGuid);
        } catch (NativeDaemonConnectorException e) {
            Slog.w(TAG, "Failed to forget key for " + partGuid + ": " + e);
        }
    }

    private void remountUidExternalStorage(int uid, int mode) {
        waitForReady();
        String modeName = "none";
        switch (mode) {
            case 1:
                modeName = "default";
                break;
            case 2:
                modeName = "read";
                break;
            case 3:
                modeName = "write";
                break;
        }
        try {
            this.mConnector.execute(TAG_VOLUME, "remount_uid", Integer.valueOf(uid), modeName);
        } catch (NativeDaemonConnectorException e) {
            Slog.w(TAG, "Failed to remount UID " + uid + " as " + modeName + ": " + e);
        }
    }

    public void setDebugFlags(int flags, int mask) {
        long token;
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        waitForReady();
        if ((mask & 2) != 0) {
            if (StorageManager.isFileEncryptedNativeOnly()) {
                throw new IllegalStateException("Emulation not available on device with native FBE");
            } else if (this.mLockPatternUtils.isCredentialRequiredToDecrypt(false)) {
                throw new IllegalStateException("Emulation requires disabling 'Secure start-up' in Settings > Security");
            } else {
                token = Binder.clearCallingIdentity();
                try {
                    SystemProperties.set("persist.sys.emulate_fbe", Boolean.toString((flags & 2) != 0));
                    ((PowerManager) this.mContext.getSystemService(PowerManager.class)).reboot(null);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
        }
        if ((mask & 1) != 0) {
            synchronized (this.mLock) {
                boolean z;
                if ((flags & 1) != 0) {
                    z = true;
                } else {
                    z = false;
                }
                this.mForceAdoptable = z;
                writeSettingsLocked();
                this.mHandler.obtainMessage(10).sendToTarget();
            }
        }
        if ((mask & 12) != 0) {
            String value;
            if ((flags & 4) != 0) {
                value = "force_on";
            } else if ((flags & 8) != 0) {
                value = "force_off";
            } else {
                value = "";
            }
            token = Binder.clearCallingIdentity();
            try {
                SystemProperties.set("persist.sys.sdcardfs", value);
                this.mHandler.obtainMessage(10).sendToTarget();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public String getPrimaryStorageUuid() {
        String str;
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        waitForReady();
        synchronized (this.mLock) {
            str = this.mPrimaryStorageUuid;
        }
        return str;
    }

    public void setPrimaryStorageUuid(String volumeUuid, IPackageMoveObserver callback) {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        waitForReady();
        synchronized (this.mLock) {
            if (Objects.equals(this.mPrimaryStorageUuid, volumeUuid)) {
                throw new IllegalArgumentException("Primary storage already at " + volumeUuid);
            } else if (this.mMoveCallback != null) {
                throw new IllegalStateException("Move already in progress");
            } else {
                this.mMoveCallback = callback;
                this.mMoveTargetUuid = volumeUuid;
                if (Objects.equals("primary_physical", this.mPrimaryStorageUuid) || Objects.equals("primary_physical", volumeUuid)) {
                    Slog.d(TAG, "Skipping move to/from primary physical");
                    onMoveStatusLocked(82);
                    onMoveStatusLocked(-100);
                    this.mHandler.obtainMessage(10).sendToTarget();
                    return;
                }
                VolumeInfo from = findStorageForUuid(this.mPrimaryStorageUuid);
                VolumeInfo to = findStorageForUuid(volumeUuid);
                if (from == null) {
                    Slog.w(TAG, "Failing move due to missing from volume " + this.mPrimaryStorageUuid);
                    onMoveStatusLocked(-6);
                } else if (to == null) {
                    Slog.w(TAG, "Failing move due to missing to volume " + volumeUuid);
                    onMoveStatusLocked(-6);
                } else {
                    try {
                        this.mConnector.execute(TAG_VOLUME, "move_storage", from.id, to.id);
                    } catch (NativeDaemonConnectorException e) {
                        throw e.rethrowAsParcelableException();
                    }
                }
            }
        }
    }

    public int[] getStorageUsers(String path) {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        waitForReady();
        try {
            String[] r = NativeDaemonEvent.filterMessageList(this.mConnector.executeForList("storage", SoundModelContract.KEY_USERS, path), 112);
            int[] data = new int[r.length];
            int i = 0;
            while (i < r.length) {
                try {
                    data[i] = Integer.parseInt(r[i].split(" ")[0]);
                    i++;
                } catch (NumberFormatException e) {
                    Slog.e(TAG, String.format("Error parsing pid %s", new Object[]{tok[0]}));
                    return new int[0];
                }
            }
            return data;
        } catch (NativeDaemonConnectorException e2) {
            Slog.e(TAG, "Failed to retrieve storage users list", e2);
            return new int[0];
        }
    }

    private void warnOnNotMounted() {
        synchronized (this.mLock) {
            for (int i = 0; i < this.mVolumes.size(); i++) {
                VolumeInfo vol = (VolumeInfo) this.mVolumes.valueAt(i);
                if (vol.isPrimary() && vol.isMountedWritable()) {
                    return;
                }
            }
            Slog.w(TAG, "No primary storage mounted!");
        }
    }

    public String[] getSecureContainerList() {
        enforcePermission("android.permission.ASEC_ACCESS");
        waitForReady();
        warnOnNotMounted();
        try {
            return NativeDaemonEvent.filterMessageList(this.mConnector.executeForList("asec", "list"), 111);
        } catch (NativeDaemonConnectorException e) {
            return new String[0];
        }
    }

    public int createSecureContainer(String id, int sizeMb, String fstype, String key, int ownerUid, boolean external) {
        enforcePermission("android.permission.ASEC_CREATE");
        waitForReady();
        warnOnNotMounted();
        int rc = 0;
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mConnector;
            String str = "asec";
            Object[] objArr = new Object[7];
            objArr[0] = "create";
            objArr[1] = id;
            objArr[2] = Integer.valueOf(sizeMb);
            objArr[3] = fstype;
            objArr[4] = new SensitiveArg(key);
            objArr[5] = Integer.valueOf(ownerUid);
            objArr[6] = external ? "1" : "0";
            nativeDaemonConnector.execute(str, objArr);
        } catch (NativeDaemonConnectorException e) {
            rc = -1;
        }
        if (rc == 0) {
            synchronized (this.mAsecMountSet) {
                this.mAsecMountSet.add(id);
            }
        }
        return rc;
    }

    public int resizeSecureContainer(String id, int sizeMb, String key) {
        enforcePermission("android.permission.ASEC_CREATE");
        waitForReady();
        warnOnNotMounted();
        try {
            this.mConnector.execute("asec", "resize", id, Integer.valueOf(sizeMb), new SensitiveArg(key));
            return 0;
        } catch (NativeDaemonConnectorException e) {
            return -1;
        }
    }

    public int finalizeSecureContainer(String id) {
        enforcePermission("android.permission.ASEC_CREATE");
        warnOnNotMounted();
        try {
            this.mConnector.execute("asec", "finalize", id);
            return 0;
        } catch (NativeDaemonConnectorException e) {
            return -1;
        }
    }

    public int fixPermissionsSecureContainer(String id, int gid, String filename) {
        enforcePermission("android.permission.ASEC_CREATE");
        warnOnNotMounted();
        try {
            this.mConnector.execute("asec", "fixperms", id, Integer.valueOf(gid), filename);
            return 0;
        } catch (NativeDaemonConnectorException e) {
            return -1;
        }
    }

    public int destroySecureContainer(String id, boolean force) {
        enforcePermission("android.permission.ASEC_DESTROY");
        waitForReady();
        warnOnNotMounted();
        Runtime.getRuntime().gc();
        int rc = 0;
        try {
            Command cmd = new Command("asec", "destroy", id);
            if (force) {
                cmd.appendArg("force");
            }
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            if (e.getCode() == VoldResponseCode.OpFailedStorageBusy) {
                rc = -7;
            } else {
                rc = -1;
            }
        }
        if (rc == 0) {
            synchronized (this.mAsecMountSet) {
                if (this.mAsecMountSet.contains(id)) {
                    this.mAsecMountSet.remove(id);
                }
            }
        }
        return rc;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int mountSecureContainer(String id, String key, int ownerUid, boolean readOnly) {
        enforcePermission("android.permission.ASEC_MOUNT_UNMOUNT");
        waitForReady();
        warnOnNotMounted();
        synchronized (this.mAsecMountSet) {
            if (this.mAsecMountSet.contains(id)) {
                return -6;
            }
        }
    }

    public int unmountSecureContainer(String id, boolean force) {
        enforcePermission("android.permission.ASEC_MOUNT_UNMOUNT");
        waitForReady();
        warnOnNotMounted();
        if (!(this.mCustMountService == null || this.mCustMountService.isSdInstallEnabled())) {
            synchronized (this.mAsecMountSet) {
                if (!this.mAsecMountSet.contains(id)) {
                    return -5;
                }
            }
        }
        Runtime.getRuntime().gc();
        int rc = 0;
        try {
            Command cmd = new Command("asec", "unmount", id);
            if (force) {
                cmd.appendArg("force");
            }
            this.mConnector.execute(cmd);
        } catch (NativeDaemonConnectorException e) {
            if (e.getCode() == VoldResponseCode.OpFailedStorageBusy) {
                rc = -7;
            } else {
                rc = -1;
            }
        }
        if (rc == 0) {
            synchronized (this.mAsecMountSet) {
                this.mAsecMountSet.remove(id);
            }
        }
        return rc;
    }

    public boolean isSecureContainerMounted(String id) {
        boolean contains;
        enforcePermission("android.permission.ASEC_ACCESS");
        waitForReady();
        warnOnNotMounted();
        synchronized (this.mAsecMountSet) {
            contains = this.mAsecMountSet.contains(id);
        }
        return contains;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int renameSecureContainer(String oldId, String newId) {
        enforcePermission("android.permission.ASEC_RENAME");
        waitForReady();
        warnOnNotMounted();
        synchronized (this.mAsecMountSet) {
            if (this.mAsecMountSet.contains(oldId) || this.mAsecMountSet.contains(newId)) {
                return -6;
            }
        }
        return rc;
    }

    public String getSecureContainerPath(String id) {
        enforcePermission("android.permission.ASEC_ACCESS");
        waitForReady();
        warnOnNotMounted();
        try {
            NativeDaemonEvent event = this.mConnector.execute("asec", "path", id);
            event.checkCode(211);
            return event.getMessage();
        } catch (NativeDaemonConnectorException e) {
            if (e.getCode() == VoldResponseCode.OpFailedStorageNotFound) {
                Slog.i(TAG, String.format("Container '%s' not found", new Object[]{id}));
                return null;
            }
            throw new IllegalStateException(String.format("Unexpected response code %d", new Object[]{Integer.valueOf(e.getCode())}));
        }
    }

    public String getSecureContainerFilesystemPath(String id) {
        enforcePermission("android.permission.ASEC_ACCESS");
        waitForReady();
        warnOnNotMounted();
        try {
            NativeDaemonEvent event = this.mConnector.execute("asec", "fspath", id);
            event.checkCode(211);
            return event.getMessage();
        } catch (NativeDaemonConnectorException e) {
            if (e.getCode() == VoldResponseCode.OpFailedStorageNotFound) {
                Slog.i(TAG, String.format("Container '%s' not found", new Object[]{id}));
                return null;
            }
            throw new IllegalStateException(String.format("Unexpected response code %d", new Object[]{Integer.valueOf(e.getCode())}));
        }
    }

    public void finishMediaUpdate() {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("no permission to call finishMediaUpdate()");
        } else if (this.mUnmountSignal != null) {
            this.mUnmountSignal.countDown();
        } else {
            Slog.w(TAG, "Odd, nobody asked to unmount?");
        }
    }

    private boolean isUidOwnerOfPackageOrSystem(String packageName, int callerUid) {
        boolean z = true;
        if (callerUid == 1000) {
            return true;
        }
        if (packageName == null) {
            return false;
        }
        if (callerUid != this.mPms.getPackageUid(packageName, 268435456, UserHandle.getUserId(callerUid))) {
            z = false;
        }
        return z;
    }

    public String getMountedObbPath(String rawPath) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        waitForReady();
        warnOnNotMounted();
        synchronized (this.mObbMounts) {
            ObbState state = (ObbState) this.mObbPathToStateMap.get(rawPath);
        }
        if (state == null) {
            Slog.w(TAG, "Failed to find OBB mounted at " + rawPath);
            return null;
        }
        try {
            NativeDaemonEvent event = this.mConnector.execute("obb", "path", state.canonicalPath);
            event.checkCode(211);
            return event.getMessage();
        } catch (NativeDaemonConnectorException e) {
            if (e.getCode() == VoldResponseCode.OpFailedStorageNotFound) {
                return null;
            }
            throw new IllegalStateException(String.format("Unexpected response code %d", new Object[]{Integer.valueOf(e.getCode())}));
        }
    }

    public boolean isObbMounted(String rawPath) {
        boolean containsKey;
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        synchronized (this.mObbMounts) {
            containsKey = this.mObbPathToStateMap.containsKey(rawPath);
        }
        return containsKey;
    }

    public void mountObb(String rawPath, String canonicalPath, String key, IObbActionListener token, int nonce) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        Preconditions.checkNotNull(canonicalPath, "canonicalPath cannot be null");
        Preconditions.checkNotNull(token, "token cannot be null");
        int callingUid = Binder.getCallingUid();
        this.mObbActionHandler.sendMessage(this.mObbActionHandler.obtainMessage(1, new MountObbAction(new ObbState(rawPath, canonicalPath, callingUid, token, nonce), key, callingUid)));
    }

    public void unmountObb(String rawPath, boolean force, IObbActionListener token, int nonce) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        synchronized (this.mObbMounts) {
            ObbState existingState = (ObbState) this.mObbPathToStateMap.get(rawPath);
        }
        if (existingState != null) {
            String str = rawPath;
            this.mObbActionHandler.sendMessage(this.mObbActionHandler.obtainMessage(1, new UnmountObbAction(new ObbState(str, existingState.canonicalPath, Binder.getCallingUid(), token, nonce), force)));
            return;
        }
        Slog.w(TAG, "Unknown OBB mount at " + rawPath);
    }

    public int getEncryptionState() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        waitForReady();
        try {
            return Integer.parseInt(this.mCryptConnector.execute("cryptfs", "cryptocomplete").getMessage());
        } catch (NumberFormatException e) {
            Slog.w(TAG, "Unable to parse result from cryptfs cryptocomplete");
            return -1;
        } catch (NativeDaemonConnectorException e2) {
            Slog.w(TAG, "Error in communicating with cryptfs in validating");
            return -1;
        }
    }

    public int decryptStorage(String password) {
        if (TextUtils.isEmpty(password)) {
            throw new IllegalArgumentException("password cannot be empty");
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        waitForReady();
        try {
            int code = Integer.parseInt(this.mCryptConnector.execute("cryptfs", "checkpw", new SensitiveArg(password)).getMessage());
            if (code == 0) {
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        try {
                            MountService.this.mCryptConnector.execute("cryptfs", "restart");
                        } catch (NativeDaemonConnectorException e) {
                            Slog.e(MountService.TAG, "problem executing in background", e);
                        }
                    }
                }, 1000);
            }
            return code;
        } catch (NativeDaemonConnectorException e) {
            return e.getCode();
        }
    }

    public int encryptStorage(int type, String password) {
        if (!TextUtils.isEmpty(password) || type == 1) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
            waitForReady();
            if (type == 1) {
                try {
                    this.mCryptConnector.execute("cryptfs", "enablecrypto", "inplace", CRYPTO_TYPES[type]);
                } catch (NativeDaemonConnectorException e) {
                    return e.getCode();
                }
            }
            this.mCryptConnector.execute("cryptfs", "enablecrypto", "inplace", CRYPTO_TYPES[type], new SensitiveArg(password));
            return 0;
        }
        throw new IllegalArgumentException("password cannot be empty");
    }

    public int changeEncryptionPassword(int type, String password) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        waitForReady();
        try {
            return Integer.parseInt(this.mCryptConnector.execute("cryptfs", "changepw", CRYPTO_TYPES[type], new SensitiveArg(password)).getMessage());
        } catch (NativeDaemonConnectorException e) {
            return e.getCode();
        }
    }

    public int verifyEncryptionPassword(String password) throws RemoteException {
        if (Binder.getCallingUid() != 1000) {
            throw new SecurityException("no permission to access the crypt keeper");
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        if (TextUtils.isEmpty(password)) {
            throw new IllegalArgumentException("password cannot be empty");
        }
        waitForReady();
        try {
            NativeDaemonEvent event = this.mCryptConnector.execute("cryptfs", "verifypw", new SensitiveArg(password));
            Slog.i(TAG, "cryptfs verifypw => " + event.getMessage());
            return Integer.parseInt(event.getMessage());
        } catch (NativeDaemonConnectorException e) {
            return e.getCode();
        }
    }

    public int getPasswordType() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STORAGE_INTERNAL", "no permission to access the crypt keeper");
        waitForReady();
        try {
            NativeDaemonEvent event = this.mCryptConnector.execute("cryptfs", "getpwtype");
            for (int i = 0; i < CRYPTO_TYPES.length; i++) {
                if (CRYPTO_TYPES[i].equals(event.getMessage())) {
                    return i;
                }
            }
            throw new IllegalStateException("unexpected return from cryptfs");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void setField(String field, String contents) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STORAGE_INTERNAL", "no permission to access the crypt keeper");
        waitForReady();
        try {
            NativeDaemonEvent event = this.mCryptConnector.execute("cryptfs", "setfield", field, contents);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public String getField(String field) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STORAGE_INTERNAL", "no permission to access the crypt keeper");
        waitForReady();
        try {
            String[] contents = NativeDaemonEvent.filterMessageList(this.mCryptConnector.executeForList("cryptfs", "getfield", field), 113);
            String result = new String();
            for (String content : contents) {
                result = result + content;
            }
            return result;
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public boolean isConvertibleToFBE() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STORAGE_INTERNAL", "no permission to access the crypt keeper");
        waitForReady();
        try {
            if (Integer.parseInt(this.mCryptConnector.execute("cryptfs", "isConvertibleToFBE").getMessage()) != 0) {
                return true;
            }
            return false;
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public String getPassword() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STORAGE_INTERNAL", "only keyguard can retrieve password");
        if (!isReady()) {
            return new String();
        }
        try {
            NativeDaemonEvent event = this.mCryptConnector.execute("cryptfs", "getpw");
            if ("-1".equals(event.getMessage())) {
                return null;
            }
            return event.getMessage();
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        } catch (IllegalArgumentException e2) {
            Slog.e(TAG, "Invalid response to getPassword");
            return null;
        }
    }

    public void clearPassword() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.STORAGE_INTERNAL", "only keyguard can clear password");
        if (isReady()) {
            try {
                NativeDaemonEvent event = this.mCryptConnector.execute("cryptfs", "clearpw");
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
    }

    public void createUserKey(int userId, int serialNumber, boolean ephemeral) {
        int i = 1;
        enforcePermission("android.permission.STORAGE_INTERNAL");
        waitForReady();
        try {
            NativeDaemonConnector nativeDaemonConnector = this.mCryptConnector;
            String str = "cryptfs";
            Object[] objArr = new Object[4];
            objArr[0] = "create_user_key";
            objArr[1] = Integer.valueOf(userId);
            objArr[2] = Integer.valueOf(serialNumber);
            if (!ephemeral) {
                i = 0;
            }
            objArr[3] = Integer.valueOf(i);
            nativeDaemonConnector.execute(str, objArr);
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void destroyUserKey(int userId) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        waitForReady();
        try {
            this.mCryptConnector.execute("cryptfs", "destroy_user_key", Integer.valueOf(userId));
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    private SensitiveArg encodeBytes(byte[] bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            return new SensitiveArg("!");
        }
        return new SensitiveArg(HexDump.toHexString(bytes));
    }

    public void addUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        waitForReady();
        try {
            this.mCryptConnector.execute("cryptfs", "add_user_key_auth", Integer.valueOf(userId), Integer.valueOf(serialNumber), encodeBytes(token), encodeBytes(secret));
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void fixateNewestUserKeyAuth(int userId) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        waitForReady();
        try {
            this.mCryptConnector.execute("cryptfs", "fixate_newest_user_key_auth", Integer.valueOf(userId));
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void unlockUserKey(int userId, int serialNumber, byte[] token, byte[] secret) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        waitForReady();
        if (StorageManager.isFileEncryptedNativeOrEmulated()) {
            if (this.mLockPatternUtils.isSecure(userId) && ArrayUtils.isEmpty(token)) {
                throw new IllegalStateException("Token required to unlock secure user " + userId);
            }
            try {
                this.mCryptConnector.execute("cryptfs", "unlock_user_key", Integer.valueOf(userId), Integer.valueOf(serialNumber), encodeBytes(token), encodeBytes(secret));
            } catch (NativeDaemonConnectorException e) {
                throw e.rethrowAsParcelableException();
            }
        }
        synchronized (this.mLock) {
            this.mLocalUnlockedUsers = ArrayUtils.appendInt(this.mLocalUnlockedUsers, userId);
        }
    }

    public void lockUserKey(int userId) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        waitForReady();
        try {
            this.mCryptConnector.execute("cryptfs", "lock_user_key", Integer.valueOf(userId));
            synchronized (this.mLock) {
                this.mLocalUnlockedUsers = ArrayUtils.removeInt(this.mLocalUnlockedUsers, userId);
            }
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public boolean isUserKeyUnlocked(int userId) {
        boolean contains;
        synchronized (this.mLock) {
            contains = ArrayUtils.contains(this.mLocalUnlockedUsers, userId);
        }
        return contains;
    }

    public void prepareUserStorage(String volumeUuid, int userId, int serialNumber, int flags) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        waitForReady();
        try {
            this.mCryptConnector.execute("cryptfs", "prepare_user_storage", escapeNull(volumeUuid), Integer.valueOf(userId), Integer.valueOf(serialNumber), Integer.valueOf(flags));
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public void destroyUserStorage(String volumeUuid, int userId, int flags) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        waitForReady();
        try {
            this.mCryptConnector.execute("cryptfs", "destroy_user_storage", escapeNull(volumeUuid), Integer.valueOf(userId), Integer.valueOf(flags));
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        }
    }

    public ParcelFileDescriptor mountAppFuse(final String name) throws RemoteException {
        try {
            final int uid = Binder.getCallingUid();
            final int pid = Binder.getCallingPid();
            NativeDaemonEvent event = this.mConnector.execute("appfuse", "mount", Integer.valueOf(uid), Integer.valueOf(pid), name);
            if (event.getFileDescriptors() != null) {
                return ParcelFileDescriptor.fromFd(event.getFileDescriptors()[0], this.mHandler, new OnCloseListener() {
                    public void onClose(IOException e) {
                        try {
                            NativeDaemonEvent execute = MountService.this.mConnector.execute("appfuse", "unmount", Integer.valueOf(uid), Integer.valueOf(pid), name);
                        } catch (NativeDaemonConnectorException e2) {
                            Log.e(MountService.TAG, "Failed to unmount appfuse.");
                        }
                    }
                });
            }
            throw new RemoteException("AppFuse FD from vold is null.");
        } catch (NativeDaemonConnectorException e) {
            throw e.rethrowAsParcelableException();
        } catch (IOException e2) {
            throw new RemoteException(e2.getMessage());
        }
    }

    public int mkdirs(String callingPkg, String appPath) {
        UserEnvironment userEnv = new UserEnvironment(UserHandle.getUserId(Binder.getCallingUid()));
        ((AppOpsManager) this.mContext.getSystemService("appops")).checkPackage(Binder.getCallingUid(), callingPkg);
        try {
            File appFile = new File(appPath).getCanonicalFile();
            if (FileUtils.contains(userEnv.buildExternalStorageAppDataDirs(callingPkg), appFile) || FileUtils.contains(userEnv.buildExternalStorageAppObbDirs(callingPkg), appFile) || FileUtils.contains(userEnv.buildExternalStorageAppMediaDirs(callingPkg), appFile)) {
                appPath = appFile.getAbsolutePath();
                if (!appPath.endsWith("/")) {
                    appPath = appPath + "/";
                }
                try {
                    this.mConnector.execute(TAG_VOLUME, "mkdirs", appPath);
                    return 0;
                } catch (NativeDaemonConnectorException e) {
                    return -1;
                }
            }
            throw new SecurityException("Invalid mkdirs path: " + appFile);
        } catch (IOException e2) {
            Slog.e(TAG, "Failed to resolve " + appPath + ": " + e2);
            return -1;
        }
    }

    public StorageVolume[] getVolumeList(int uid, String packageName, int flags) {
        int userId = UserHandle.getUserId(uid);
        boolean forWrite = (flags & 256) != 0;
        boolean realState = (flags & 512) != 0;
        boolean includeInvisible = (flags & 1024) != 0;
        long token = Binder.clearCallingIdentity();
        try {
            boolean userKeyUnlocked = isUserKeyUnlocked(userId);
            boolean storagePermission = this.mMountServiceInternal.hasExternalStorage(uid, packageName);
            boolean foundPrimary = false;
            ArrayList<StorageVolume> res = new ArrayList();
            synchronized (this.mLock) {
                for (int i = 0; i < this.mVolumes.size(); i++) {
                    VolumeInfo vol = (VolumeInfo) this.mVolumes.valueAt(i);
                    switch (vol.getType()) {
                        case 0:
                        case 2:
                            boolean match = forWrite ? vol.isVisibleForWrite(userId) : !vol.isVisibleForRead(userId) ? includeInvisible && vol.getPath() != null : true;
                            if (!match) {
                                break;
                            }
                            boolean reportUnmounted = false;
                            if (vol.getType() == 2 && !userKeyUnlocked) {
                                reportUnmounted = true;
                            } else if (!(storagePermission || realState)) {
                                reportUnmounted = true;
                            }
                            StorageVolume userVol = vol.buildStorageVolume(this.mContext, userId, reportUnmounted);
                            if (!vol.isPrimary()) {
                                res.add(userVol);
                                break;
                            }
                            res.add(0, userVol);
                            foundPrimary = true;
                            break;
                        default:
                            break;
                    }
                }
            }
            if (!foundPrimary) {
                Log.w(TAG, "No primary storage defined yet; hacking together a stub");
                boolean primaryPhysical = SystemProperties.getBoolean("ro.vold.primary_physical", false);
                String id = "stub_primary";
                boolean removable = primaryPhysical;
                String state = "removed";
                res.add(0, new StorageVolume("stub_primary", 0, Environment.getLegacyExternalStorageDirectory(), this.mContext.getString(17039374), true, primaryPhysical, !primaryPhysical, 0, false, 0, new UserHandle(userId), null, "removed"));
            }
            return (StorageVolume[]) res.toArray(new StorageVolume[res.size()]);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public DiskInfo[] getDisks() {
        DiskInfo[] res;
        synchronized (this.mLock) {
            res = new DiskInfo[this.mDisks.size()];
            for (int i = 0; i < this.mDisks.size(); i++) {
                res[i] = (DiskInfo) this.mDisks.valueAt(i);
            }
        }
        return res;
    }

    public VolumeInfo[] getVolumes(int flags) {
        VolumeInfo[] res;
        synchronized (this.mLock) {
            res = new VolumeInfo[this.mVolumes.size()];
            for (int i = 0; i < this.mVolumes.size(); i++) {
                res[i] = (VolumeInfo) this.mVolumes.valueAt(i);
            }
        }
        return res;
    }

    public VolumeRecord[] getVolumeRecords(int flags) {
        VolumeRecord[] res;
        synchronized (this.mLock) {
            res = new VolumeRecord[this.mRecords.size()];
            for (int i = 0; i < this.mRecords.size(); i++) {
                res[i] = (VolumeRecord) this.mRecords.valueAt(i);
            }
        }
        return res;
    }

    private void addObbStateLocked(ObbState obbState) throws RemoteException {
        IBinder binder = obbState.getBinder();
        List<ObbState> obbStates = (List) this.mObbMounts.get(binder);
        if (obbStates == null) {
            obbStates = new ArrayList();
            this.mObbMounts.put(binder, obbStates);
        } else {
            for (ObbState o : obbStates) {
                if (o.rawPath.equals(obbState.rawPath)) {
                    throw new IllegalStateException("Attempt to add ObbState twice. This indicates an error in the MountService logic.");
                }
            }
        }
        obbStates.add(obbState);
        try {
            obbState.link();
            this.mObbPathToStateMap.put(obbState.rawPath, obbState);
        } catch (RemoteException e) {
            obbStates.remove(obbState);
            if (obbStates.isEmpty()) {
                this.mObbMounts.remove(binder);
            }
            throw e;
        }
    }

    private void removeObbStateLocked(ObbState obbState) {
        IBinder binder = obbState.getBinder();
        List<ObbState> obbStates = (List) this.mObbMounts.get(binder);
        if (obbStates != null) {
            if (obbStates.remove(obbState)) {
                obbState.unlink();
            }
            if (obbStates.isEmpty()) {
                this.mObbMounts.remove(binder);
            }
        }
        this.mObbPathToStateMap.remove(obbState.rawPath);
    }

    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ", 160);
        synchronized (this.mLock) {
            int i;
            pw.println("Disks:");
            pw.increaseIndent();
            for (i = 0; i < this.mDisks.size(); i++) {
                ((DiskInfo) this.mDisks.valueAt(i)).dump(pw);
            }
            pw.decreaseIndent();
            pw.println();
            pw.println("Volumes:");
            pw.increaseIndent();
            for (i = 0; i < this.mVolumes.size(); i++) {
                VolumeInfo vol = (VolumeInfo) this.mVolumes.valueAt(i);
                if (!"private".equals(vol.id)) {
                    vol.dump(pw);
                }
            }
            pw.decreaseIndent();
            pw.println();
            pw.println("Records:");
            pw.increaseIndent();
            for (i = 0; i < this.mRecords.size(); i++) {
                ((VolumeRecord) this.mRecords.valueAt(i)).dump(pw);
            }
            pw.decreaseIndent();
            pw.println();
            pw.println("Primary storage UUID: " + this.mPrimaryStorageUuid);
            pw.println("Force adoptable: " + this.mForceAdoptable);
            pw.println();
            pw.println("Local unlocked users: " + Arrays.toString(this.mLocalUnlockedUsers));
            pw.println("System unlocked users: " + Arrays.toString(this.mSystemUnlockedUsers));
        }
        synchronized (this.mObbMounts) {
            pw.println();
            pw.println("mObbMounts:");
            pw.increaseIndent();
            for (Entry<IBinder, List<ObbState>> e : this.mObbMounts.entrySet()) {
                pw.println(e.getKey() + ":");
                pw.increaseIndent();
                for (ObbState obbState : (List) e.getValue()) {
                    pw.println(obbState);
                }
                pw.decreaseIndent();
            }
            pw.decreaseIndent();
            pw.println();
            pw.println("mObbPathToStateMap:");
            pw.increaseIndent();
            for (Entry<String, ObbState> e2 : this.mObbPathToStateMap.entrySet()) {
                pw.print((String) e2.getKey());
                pw.print(" -> ");
                pw.println(e2.getValue());
            }
            pw.decreaseIndent();
        }
        pw.println();
        pw.println("mConnector:");
        pw.increaseIndent();
        this.mConnector.dump(fd, pw, args);
        pw.decreaseIndent();
        pw.println();
        pw.println("mCryptConnector:");
        pw.increaseIndent();
        this.mCryptConnector.dump(fd, pw, args);
        pw.decreaseIndent();
        pw.println();
        pw.print("Last maintenance: ");
        pw.println(TimeUtils.formatForLogging(this.mLastMaintenance));
    }

    public void monitor() {
        if (this.mConnector != null) {
            this.mConnector.monitor();
        }
        if (this.mCryptConnector != null) {
            this.mCryptConnector.monitor();
        }
    }

    public boolean isSecure() {
        return new LockPatternUtils(this.mContext).isSecure(ActivityManager.getCurrentUser());
    }
}
