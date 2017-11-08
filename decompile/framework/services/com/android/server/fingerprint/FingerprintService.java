package com.android.server.fingerprint;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.SynchronousUserSwitchObserver;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.IFingerprintDaemon;
import android.hardware.fingerprint.IFingerprintDaemonCallback;
import android.hardware.fingerprint.IFingerprintDaemonCallback.Stub;
import android.hardware.fingerprint.IFingerprintService;
import android.hardware.fingerprint.IFingerprintServiceLockoutResetCallback;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.Binder;
import android.os.DeadObjectException;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import com.android.internal.logging.MetricsLogger;
import com.android.server.FingerprintUnlockDataCollector;
import com.android.server.ServiceThread;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FingerprintService extends AbsFingerprintService implements DeathRecipient {
    private static final String ACTION_LOCKOUT_RESET = "com.android.server.fingerprint.ACTION_LOCKOUT_RESET";
    private static final long CANCEL_TIMEOUT_LIMIT = 3000;
    private static final int CODE_GET_TOKEN_LEN_RULE = 1103;
    private static final int CODE_IS_FP_NEED_CALIBRATE_RULE = 1101;
    private static final int CODE_SET_CALIBRATE_MODE_RULE = 1102;
    static final boolean DEBUG = true;
    private static boolean DEBUG_FPLOG = false;
    private static final long FAIL_LOCKOUT_TIMEOUT_MS = 30000;
    private static final String FINGERPRINTD = "android.hardware.fingerprint.IFingerprintDaemon";
    private static final int FINGERPRINT_ACQUIRED_FINGER_DOWN = 2002;
    private static final long FINGERPRINT_GRACE = 1000;
    private static final String FP_DATA_DIR = "fpdata";
    private static final int HIDDEN_SPACE_ID = -100;
    private static final int HW_FP_NO_COUNT_FAILED_ATTEMPS = 16777216;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int MSG_USER_SWITCHING = 10;
    private static final int PRIMARY_USER_ID = 0;
    private static final String SCREENLOCK_PACKAGE = "com.celltick.lockscreen";
    private static final int SPECIAL_USER_ID = -101;
    static final String TAG = "FingerprintService";
    private long auTime;
    private long downTime;
    private FingerprintUnlockDataCollector fpDataCollector;
    private AlarmManager mAlarmManager;
    private final AppOpsManager mAppOps;
    private Context mContext;
    private long mCurrentAuthenticatorId;
    private ClientMonitor mCurrentClient;
    private int mCurrentUserId = -2;
    private IFingerprintDaemon mDaemon;
    private IFingerprintDaemonCallback mDaemonCallback = new Stub() {
        public void onEnrollResult(long deviceId, int fingerId, int groupId, int remaining) {
            Slog.w(FingerprintService.TAG, "onEnrollResult 1");
            final long j = deviceId;
            final int i = fingerId;
            final int i2 = groupId;
            final int i3 = remaining;
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    Slog.w(FingerprintService.TAG, "onEnrollResult 2");
                    FingerprintService.this.handleEnrollResult(j, i, i2, i3);
                }
            });
        }

        public void onAcquired(final long deviceId, final int acquiredInfo) {
            Slog.w(FingerprintService.TAG, "onAcquired 1");
            if (FingerprintService.DEBUG_FPLOG) {
                if (acquiredInfo == FingerprintService.FINGERPRINT_ACQUIRED_FINGER_DOWN && FingerprintService.this.fpDataCollector != null) {
                    FingerprintService.this.fpDataCollector.reportFingerDown();
                    FingerprintService.this.downTime = System.currentTimeMillis();
                } else if (acquiredInfo == 0 && FingerprintService.this.fpDataCollector != null) {
                    FingerprintService.this.fpDataCollector.reportCaptureCompleted();
                }
            }
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    Slog.w(FingerprintService.TAG, "onAcquired 2");
                    FingerprintService.this.handleAcquired(deviceId, acquiredInfo);
                }
            });
        }

        public void onAuthenticated(long deviceId, int fingerId, int groupId) {
            Slog.w(FingerprintService.TAG, "onAuthenticated 1");
            if (FingerprintService.DEBUG_FPLOG && FingerprintService.this.fpDataCollector != null) {
                FingerprintService.this.fpDataCollector.reportFingerprintAuthenticated(fingerId != 0 ? FingerprintService.DEBUG : false);
                FingerprintService.this.auTime = System.currentTimeMillis();
            }
            final long j = deviceId;
            final int i = fingerId;
            final int i2 = groupId;
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    Slog.w(FingerprintService.TAG, "onAuthenticated 2");
                    FingerprintService.this.handleAuthenticated(j, i, i2);
                }
            });
        }

        public void onError(final long deviceId, final int error) {
            Slog.w(FingerprintService.TAG, "onError 1");
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    Slog.w(FingerprintService.TAG, "onError 2");
                    FingerprintService.this.handleError(deviceId, error);
                }
            });
        }

        public void onRemoved(long deviceId, int fingerId, int groupId) {
            Slog.w(FingerprintService.TAG, "onRemoved 1");
            final long j = deviceId;
            final int i = fingerId;
            final int i2 = groupId;
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    Slog.w(FingerprintService.TAG, "onRemoved 2");
                    FingerprintService.this.handleRemoved(j, i, i2);
                }
            });
        }

        public void onEnumerate(long deviceId, int[] fingerIds, int[] groupIds) {
            final long j = deviceId;
            final int[] iArr = fingerIds;
            final int[] iArr2 = groupIds;
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    FingerprintService.this.handleEnumerate(j, iArr, iArr2);
                }
            });
        }
    };
    private long mFPGraceTimeStarted = 0;
    private int mFailedAttempts;
    private final FingerprintUtils mFingerprintUtils = FingerprintUtils.getInstance();
    private long mHalDeviceId;
    Handler mHandler = null;
    int mHwFailedAttempts = 0;
    private final String mKeyguardPackage;
    private final ArrayList<FingerprintServiceLockoutResetMonitor> mLockoutMonitors = new ArrayList();
    private final BroadcastReceiver mLockoutReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (FingerprintService.ACTION_LOCKOUT_RESET.equals(intent.getAction())) {
                FingerprintService.this.resetFailedAttempts();
            }
        }
    };
    private final Runnable mLockoutReset = new Runnable() {
        public void run() {
            FingerprintService.this.resetFailedAttempts();
        }
    };
    long mLockoutTime = 0;
    private ClientMonitor mPendingClient;
    private final PowerManager mPowerManager;
    private final Runnable mResetClientState = new Runnable() {
        public void run() {
            Slog.w(FingerprintService.TAG, "Client " + (FingerprintService.this.mCurrentClient != null ? FingerprintService.this.mCurrentClient.getOwnerString() : "null") + " failed to respond to cancel, starting client " + (FingerprintService.this.mPendingClient != null ? FingerprintService.this.mPendingClient.getOwnerString() : "null"));
            FingerprintService.this.mCurrentClient = null;
            FingerprintService.this.startClient(FingerprintService.this.mPendingClient, false);
            FingerprintService.this.mPendingClient = null;
        }
    };
    private final UserManager mUserManager;
    private String opPackageName;

    private class FingerprintServiceLockoutResetMonitor {
        private final IFingerprintServiceLockoutResetCallback mCallback;
        private final Runnable mRemoveCallbackRunnable = new Runnable() {
            public void run() {
                FingerprintService.this.removeLockoutResetCallback(FingerprintServiceLockoutResetMonitor.this);
            }
        };

        public FingerprintServiceLockoutResetMonitor(IFingerprintServiceLockoutResetCallback callback) {
            this.mCallback = callback;
        }

        public void sendLockoutReset() {
            if (this.mCallback != null) {
                try {
                    this.mCallback.onLockoutReset(FingerprintService.this.mHalDeviceId);
                } catch (DeadObjectException e) {
                    Slog.w(FingerprintService.TAG, "Death object while invoking onLockoutReset: ", e);
                    FingerprintService.this.mHandler.post(this.mRemoveCallbackRunnable);
                } catch (RemoteException e2) {
                    Slog.w(FingerprintService.TAG, "Failed to invoke onLockoutReset: ", e2);
                }
            }
        }
    }

    private final class FingerprintServiceWrapper extends IFingerprintService.Stub {
        private FingerprintServiceWrapper() {
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Slog.i(FingerprintService.TAG, "FingerprintService onTransact");
            if (!FingerprintService.this.isHwTransactInterest(code)) {
                return super.onTransact(code, data, reply, flags);
            }
            FingerprintService.this.checkPermission("android.permission.USE_FINGERPRINT");
            return FingerprintService.this.onHwTransact(code, data, reply, flags);
        }

        public boolean isInGraceTime() {
            return FingerprintService.this.graceTime();
        }

        public long preEnroll(IBinder token) {
            Flog.i(1303, "FingerprintService preEnroll");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            return FingerprintService.this.startPreEnroll(token);
        }

        public int postEnroll(IBinder token) {
            Flog.i(1303, "FingerprintService postEnroll");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            Flog.i(1303, "postEnroll client uid = " + Binder.getCallingUid() + ", postEnroll client pid = " + Binder.getCallingPid());
            return FingerprintService.this.startPostEnroll(token);
        }

        public void enroll(IBinder token, byte[] cryptoToken, int userId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) {
            Flog.i(1303, "FingerprintService enroll");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            if (FingerprintService.this.getEnrolledFingerprints(userId).size() >= FingerprintService.this.mContext.getResources().getInteger(17694880)) {
                Slog.w(FingerprintService.TAG, "Too many fingerprints registered");
            } else if (FingerprintService.this.isCurrentUserOrProfile(userId)) {
                final boolean restricted = isRestricted();
                final IBinder iBinder = token;
                final byte[] bArr = cryptoToken;
                final int i = userId;
                final IFingerprintServiceReceiver iFingerprintServiceReceiver = receiver;
                final int i2 = flags;
                final String str = opPackageName;
                FingerprintService.this.mHandler.post(new Runnable() {
                    public void run() {
                        FingerprintService.this.startEnrollment(iBinder, bArr, i, iFingerprintServiceReceiver, i2, restricted, str);
                    }
                });
            }
        }

        private boolean isRestricted() {
            return FingerprintService.this.hasPermission("android.permission.MANAGE_FINGERPRINT") ? false : FingerprintService.DEBUG;
        }

        public void cancelEnrollment(final IBinder token) {
            Flog.i(1303, "FingerprintService cancelEnrollment");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            Flog.i(1303, "cancelEnrollment client uid = " + Binder.getCallingUid() + ", cancelEnrollment client pid = " + Binder.getCallingPid());
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    ClientMonitor client = FingerprintService.this.mCurrentClient;
                    if ((client instanceof EnrollClient) && client.getToken() == token) {
                        client.stop(client.getToken() == token ? FingerprintService.DEBUG : false);
                    }
                }
            });
        }

        public void authenticate(IBinder token, long opId, int groupId, IFingerprintServiceReceiver receiver, int flags, String opPackageName) {
            Flog.i(1303, "FingerprintService authenticate");
            final int callingUid = Binder.getCallingUid();
            final int callingUserId = UserHandle.getCallingUserId();
            final int pid = Binder.getCallingPid();
            final boolean restricted = isRestricted();
            final long j = opId;
            final String str = opPackageName;
            final IBinder iBinder = token;
            final int i = groupId;
            final IFingerprintServiceReceiver iFingerprintServiceReceiver = receiver;
            final int i2 = flags;
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    MetricsLogger.histogram(FingerprintService.this.mContext, "fingerprint_token", j != 0 ? 1 : 0);
                    FingerprintService.this.setLivenessSwitch(str);
                    if (FingerprintService.this.canUseFingerprint(str, FingerprintService.DEBUG, callingUid, pid)) {
                        FingerprintService.this.startAuthentication(iBinder, j, callingUserId, i, iFingerprintServiceReceiver, i2, restricted, str);
                    } else {
                        Slog.v(FingerprintService.TAG, "authenticate(): reject " + str);
                    }
                }
            });
        }

        public void cancelAuthentication(IBinder token, String opPackageName) {
            Flog.i(1303, "FingerprintService cancelAuthentication");
            final int uid = Binder.getCallingUid();
            final int pid = Binder.getCallingPid();
            final String str = opPackageName;
            final IBinder iBinder = token;
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    boolean z = false;
                    if (FingerprintService.this.canUseFingerprint(str, false, uid, pid)) {
                        ClientMonitor client = FingerprintService.this.mCurrentClient;
                        if (client instanceof AuthenticationClient) {
                            if (client.getToken() == iBinder) {
                                Slog.v(FingerprintService.TAG, "stop client " + client.getOwnerString());
                                if (client.getToken() == iBinder) {
                                    z = FingerprintService.DEBUG;
                                }
                                client.stop(z);
                                return;
                            }
                            Slog.v(FingerprintService.TAG, "can't stop client " + client.getOwnerString() + " since tokens don't match");
                            return;
                        } else if (client != null) {
                            Slog.v(FingerprintService.TAG, "can't cancel non-authenticating client " + client.getOwnerString());
                            return;
                        } else {
                            return;
                        }
                    }
                    Slog.v(FingerprintService.TAG, "cancelAuthentication(): reject " + str);
                }
            });
        }

        public void setActiveUser(final int userId) {
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    FingerprintService.this.updateActiveGroup(userId, null);
                }
            });
        }

        public void remove(IBinder token, int fingerId, int groupId, int userId, IFingerprintServiceReceiver receiver) {
            Flog.i(1303, "FingerprintService remove");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            final boolean restricted = isRestricted();
            final IBinder iBinder = token;
            final int i = fingerId;
            final int i2 = groupId;
            final int i3 = userId;
            final IFingerprintServiceReceiver iFingerprintServiceReceiver = receiver;
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    FingerprintService.this.startRemove(iBinder, i, i2, i3, iFingerprintServiceReceiver, restricted);
                }
            });
        }

        public boolean isHardwareDetected(long deviceId, String opPackageName) {
            boolean z = false;
            if (!FingerprintService.this.canUseFingerprint(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid())) {
                return false;
            }
            if (FingerprintService.this.mHalDeviceId != 0) {
                z = FingerprintService.DEBUG;
            }
            return z;
        }

        public void rename(final int fingerId, final int groupId, final String name) {
            Flog.i(1303, "FingerprintService rename");
            FingerprintService.this.checkPermission("android.permission.MANAGE_FINGERPRINT");
            if (FingerprintService.this.isCurrentUserOrProfile(groupId)) {
                FingerprintService.this.mHandler.post(new Runnable() {
                    public void run() {
                        FingerprintService.this.mFingerprintUtils.renameFingerprintForUser(FingerprintService.this.mContext, fingerId, groupId, name);
                    }
                });
            }
        }

        public List<Fingerprint> getEnrolledFingerprints(int userId, String opPackageName) {
            Flog.i(1303, "FingerprintService getEnrolledFingerprints");
            if (!FingerprintService.this.canUseFingerprint(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid())) {
                return Collections.emptyList();
            }
            if (FingerprintService.this.isCurrentUserOrProfile(userId)) {
                return FingerprintService.this.getEnrolledFingerprints(userId);
            }
            return Collections.emptyList();
        }

        public boolean hasEnrolledFingerprints(int userId, String opPackageName) {
            Flog.i(1303, "FingerprintService hasEnrolledFingerprints");
            if (FingerprintService.this.canUseFingerprint(opPackageName, false, Binder.getCallingUid(), Binder.getCallingPid()) && FingerprintService.this.isCurrentUserOrProfile(userId)) {
                return FingerprintService.this.hasEnrolledFingerprints(userId);
            }
            return false;
        }

        public long getAuthenticatorId(String opPackageName) {
            Flog.i(1303, "FingerprintService getAuthenticatorId");
            return FingerprintService.this.getAuthenticatorId(opPackageName);
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (FingerprintService.this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump Fingerprint from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                FingerprintService.this.dumpInternal(pw);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void resetTimeout(byte[] token) {
            FingerprintService.this.checkPermission("android.permission.RESET_FINGERPRINT_LOCKOUT");
            FingerprintService.this.mHandler.post(FingerprintService.this.mLockoutReset);
        }

        public void addLockoutResetCallback(final IFingerprintServiceLockoutResetCallback callback) throws RemoteException {
            FingerprintService.this.mHandler.post(new Runnable() {
                public void run() {
                    FingerprintService.this.addLockoutResetMonitor(new FingerprintServiceLockoutResetMonitor(callback));
                }
            });
        }

        public int getRemainingNum() {
            FingerprintService.this.checkPermission("android.permission.USE_FINGERPRINT");
            Slog.d(FingerprintService.TAG, " Remaining Num Attempts = " + (5 - FingerprintService.this.mHwFailedAttempts));
            return 5 - FingerprintService.this.mHwFailedAttempts;
        }

        public long getRemainingTime() {
            FingerprintService.this.checkPermission("android.permission.USE_FINGERPRINT");
            long now = SystemClock.elapsedRealtime();
            long nowToLockout = now - FingerprintService.this.mLockoutTime;
            Slog.d(FingerprintService.TAG, "Remaining Time mLockoutTime = " + FingerprintService.this.mLockoutTime + "  now = " + now);
            if (nowToLockout <= 0 || nowToLockout >= FingerprintService.FAIL_LOCKOUT_TIMEOUT_MS) {
                return 0;
            }
            return FingerprintService.FAIL_LOCKOUT_TIMEOUT_MS - nowToLockout;
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : DEBUG;
        DEBUG_FPLOG = isLoggable;
    }

    public FingerprintService(Context context) {
        super(context);
        this.mContext = context;
        this.mKeyguardPackage = ComponentName.unflattenFromString(context.getResources().getString(17039463)).getPackageName();
        this.mAppOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
        this.mContext.registerReceiver(this.mLockoutReceiver, new IntentFilter(ACTION_LOCKOUT_RESET), "android.permission.RESET_FINGERPRINT_LOCKOUT", null);
        this.mUserManager = UserManager.get(this.mContext);
        this.fpDataCollector = FingerprintUnlockDataCollector.getInstance();
        ServiceThread fingerprintThread = new ServiceThread("fingerprintServcie", -8, false);
        fingerprintThread.start();
        this.mHandler = new Handler(fingerprintThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 10:
                        FingerprintService.this.handleUserSwitching(msg.arg1);
                        return;
                    default:
                        Slog.w(FingerprintService.TAG, "Unknown message:" + msg.what);
                        return;
                }
            }
        };
    }

    public void binderDied() {
        Slog.v(TAG, "fingerprintd died");
        this.mDaemon = null;
        this.mCurrentUserId = -2;
        handleError(this.mHalDeviceId, 1);
    }

    public IFingerprintDaemon getFingerprintDaemon() {
        if (this.mDaemon == null) {
            this.mDaemon = IFingerprintDaemon.Stub.asInterface(ServiceManager.getService(FINGERPRINTD));
            if (this.mDaemon != null) {
                try {
                    this.mDaemon.asBinder().linkToDeath(this, 0);
                    this.mDaemon.init(this.mDaemonCallback);
                    this.mHalDeviceId = this.mDaemon.openHal();
                    if (this.mHalDeviceId != 0) {
                        updateActiveGroup(ActivityManager.getCurrentUser(), null);
                    } else {
                        Slog.w(TAG, "Failed to open Fingerprint HAL!");
                        this.mDaemon = null;
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Failed to open fingeprintd HAL", e);
                    this.mDaemon = null;
                }
            } else {
                Slog.w(TAG, "fingerprint service not available");
            }
        }
        return this.mDaemon;
    }

    protected void handleEnumerate(long deviceId, int[] fingerIds, int[] groupIds) {
        if (fingerIds.length != groupIds.length) {
            Slog.w(TAG, "fingerIds and groupIds differ in length: f[]=" + Arrays.toString(fingerIds) + ", g[]=" + Arrays.toString(groupIds));
        } else {
            Slog.w(TAG, "Enumerate: f[]=" + fingerIds + ", g[]=" + groupIds);
        }
    }

    protected void handleError(long deviceId, int error) {
        ClientMonitor client = this.mCurrentClient;
        if (client != null && client.onError(error)) {
            removeClient(client);
        }
        Slog.v(TAG, "handleError(client=" + (client != null ? client.getOwnerString() : "null") + ", error = " + error + ")");
        if (error == 5) {
            this.mHandler.removeCallbacks(this.mResetClientState);
            if (this.mPendingClient != null) {
                Slog.v(TAG, "start pending client " + this.mPendingClient.getOwnerString());
                startClient(this.mPendingClient, false);
                this.mPendingClient = null;
            }
        }
    }

    protected void handleRemoved(long deviceId, int fingerId, int groupId) {
        ClientMonitor client = this.mCurrentClient;
        groupId = getRealUserIdForApp(groupId);
        if (client != null && client.onRemoved(fingerId, groupId)) {
            removeClient(client);
        }
    }

    protected void handleAuthenticated(long deviceId, int fingerId, int groupId) {
        ClientMonitor client = this.mCurrentClient;
        groupId = getRealUserIdForApp(groupId);
        if (client != null && SCREENLOCK_PACKAGE.equals(client.getOwnerString())) {
            this.mFPGraceTimeStarted = System.currentTimeMillis();
        }
        if (client != null && client.onAuthenticated(fingerId, groupId)) {
            removeClient(client);
        }
    }

    protected boolean graceTime() {
        if (System.currentTimeMillis() - this.mFPGraceTimeStarted < 1000) {
            return DEBUG;
        }
        return false;
    }

    protected void handleAcquired(long deviceId, int acquiredInfo) {
        ClientMonitor client = this.mCurrentClient;
        if (client != null && client.onAcquired(acquiredInfo)) {
            removeClient(client);
        }
    }

    protected void handleEnrollResult(long deviceId, int fingerId, int groupId, int remaining) {
        ClientMonitor client = this.mCurrentClient;
        groupId = getRealUserIdForApp(groupId);
        if (client == null || !client.onEnrollResult(fingerId, groupId, remaining)) {
            Slog.w(TAG, "no eroll client, remove erolled fingerprint");
            if (remaining == 0) {
                IFingerprintDaemon daemon = getFingerprintDaemon();
                if (daemon != null) {
                    try {
                        daemon.remove(fingerId, ActivityManager.getCurrentUser());
                    } catch (RemoteException e) {
                    }
                } else {
                    return;
                }
            }
        }
        removeClient(client);
    }

    protected int getRealUserIdForApp(int groupId) {
        if (groupId != HIDDEN_SPACE_ID) {
            return groupId;
        }
        for (UserInfo user : this.mUserManager.getUsers(DEBUG)) {
            if (user != null && user.isHwHiddenSpace()) {
                return user.id;
            }
        }
        Slog.w(TAG, "getRealUserIdForApp error return 0");
        return 0;
    }

    private void userActivity() {
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), 2, 0);
    }

    void handleUserSwitching(int userId) {
        updateActiveGroup(userId, null);
    }

    private void removeClient(ClientMonitor client) {
        if (client != null) {
            client.destroy();
            if (!(client == this.mCurrentClient || this.mCurrentClient == null)) {
                Slog.w(TAG, new StringBuilder().append("Unexpected client: ").append(client.getOwnerString()).append("expected: ").append(this.mCurrentClient).toString() != null ? this.mCurrentClient.getOwnerString() : "null");
            }
        }
        if (this.mCurrentClient != null) {
            Slog.v(TAG, "Done with client: " + client.getOwnerString());
            this.mCurrentClient = null;
        }
    }

    private boolean inLockoutMode() {
        return this.mFailedAttempts >= 5 ? DEBUG : false;
    }

    private void scheduleLockoutReset() {
        if (this.mAlarmManager == null) {
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService(AlarmManager.class);
        }
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + FAIL_LOCKOUT_TIMEOUT_MS, getLockoutResetIntent());
    }

    private void cancelLockoutReset() {
        if (this.mAlarmManager == null) {
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService(AlarmManager.class);
        }
        this.mAlarmManager.cancel(getLockoutResetIntent());
    }

    private PendingIntent getLockoutResetIntent() {
        return PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_LOCKOUT_RESET), 134217728);
    }

    protected void resetFailedAttempts() {
        if (inLockoutMode()) {
            Slog.v(TAG, "Reset fingerprint lockout");
        }
        this.mFailedAttempts = 0;
        cancelLockoutReset();
        notifyLockoutResetMonitors();
        this.mHandler.removeCallbacks(this.mLockoutReset);
        this.mLockoutTime = 0;
        this.mHwFailedAttempts = 0;
    }

    protected void handleHwFailedAttempt(int flags, String packagesName) {
        if (flags == HW_FP_NO_COUNT_FAILED_ATTEMPS && "com.android.settings".equals(packagesName)) {
            Slog.i(TAG, "no need count hw failed attempts");
        } else {
            this.mHwFailedAttempts++;
        }
    }

    public long startPreEnroll(IBinder token) {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            Slog.w(TAG, "startPreEnroll: no fingeprintd!");
            return 0;
        }
        try {
            return daemon.preEnroll();
        } catch (RemoteException e) {
            Slog.e(TAG, "startPreEnroll failed", e);
            return 0;
        }
    }

    public int startPostEnroll(IBinder token) {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon == null) {
            Slog.w(TAG, "startPostEnroll: no fingeprintd!");
            return 0;
        }
        try {
            return daemon.postEnroll();
        } catch (RemoteException e) {
            Slog.e(TAG, "startPostEnroll failed", e);
            return 0;
        }
    }

    protected void setLivenessSwitch(String opPackageName) {
        Slog.w(TAG, "father class call setLivenessSwitch");
    }

    private void startClient(ClientMonitor newClient, boolean initiatedByClient) {
        ClientMonitor currentClient = this.mCurrentClient;
        this.mHandler.removeCallbacks(this.mResetClientState);
        if (currentClient != null) {
            Slog.v(TAG, "request stop current client " + currentClient.getOwnerString());
            currentClient.stop(initiatedByClient);
            if (this.mPendingClient != null) {
                this.mPendingClient.destroy();
            }
            this.mPendingClient = newClient;
            this.mHandler.removeCallbacks(this.mResetClientState);
            this.mHandler.postDelayed(this.mResetClientState, CANCEL_TIMEOUT_LIMIT);
        } else if (newClient != null) {
            this.mCurrentClient = newClient;
            Slog.v(TAG, "starting client " + newClient.getClass().getSuperclass().getSimpleName() + "(" + newClient.getOwnerString() + ")" + ", initiatedByClient = " + initiatedByClient + ")");
            newClient.start();
        }
    }

    void startRemove(IBinder token, int fingerId, int groupId, int userId, IFingerprintServiceReceiver receiver, boolean restricted) {
        if (getFingerprintDaemon() == null) {
            Slog.w(TAG, "startRemove: no fingeprintd!");
            return;
        }
        startClient(new RemovalClient(getContext(), this.mHalDeviceId, token, receiver, fingerId, groupId, userId, restricted, token.toString()) {
            public void notifyUserActivity() {
                FingerprintService.this.userActivity();
            }

            public IFingerprintDaemon getFingerprintDaemon() {
                return FingerprintService.this.getFingerprintDaemon();
            }
        }, DEBUG);
    }

    public List<Fingerprint> getEnrolledFingerprints(int userId) {
        return this.mFingerprintUtils.getFingerprintsForUser(this.mContext, userId);
    }

    public boolean hasEnrolledFingerprints(int userId) {
        if (userId != UserHandle.getCallingUserId()) {
            checkPermission("android.permission.INTERACT_ACROSS_USERS");
        }
        if (this.mFingerprintUtils.getFingerprintsForUser(this.mContext, userId).size() > 0) {
            return DEBUG;
        }
        return false;
    }

    boolean hasPermission(String permission) {
        return getContext().checkCallingOrSelfPermission(permission) == 0 ? DEBUG : false;
    }

    void checkPermission(String permission) {
        getContext().enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }

    int getEffectiveUserId(int userId) {
        UserManager um = UserManager.get(this.mContext);
        if (um != null) {
            long callingIdentity = Binder.clearCallingIdentity();
            userId = um.getCredentialOwnerProfile(userId);
            Binder.restoreCallingIdentity(callingIdentity);
            return userId;
        }
        Slog.e(TAG, "Unable to acquire UserManager");
        return userId;
    }

    boolean isCurrentUserOrProfile(int userId) {
        for (int profileId : UserManager.get(this.mContext).getEnabledProfileIds(userId)) {
            if (profileId == userId) {
                return DEBUG;
            }
        }
        return false;
    }

    private boolean isForegroundActivity(int uid, int pid) {
        try {
            List<RunningAppProcessInfo> procs = ActivityManagerNative.getDefault().getRunningAppProcesses();
            int N = procs.size();
            for (int i = 0; i < N; i++) {
                RunningAppProcessInfo proc = (RunningAppProcessInfo) procs.get(i);
                if (proc.pid == pid && proc.uid == uid && proc.importance == 100) {
                    return DEBUG;
                }
            }
        } catch (RemoteException e) {
            Slog.w(TAG, "am.getRunningAppProcesses() failed");
        }
        return false;
    }

    private boolean canUseFingerprint(String opPackageName, boolean foregroundOnly, int uid, int pid) {
        checkPermission("android.permission.USE_FINGERPRINT");
        this.opPackageName = opPackageName;
        if (opPackageName != null && (opPackageName.equals("com.huawei.hwasm") || opPackageName.equals("com.huawei.securitymgr") || isKeyguard(opPackageName))) {
            return DEBUG;
        }
        if (!isCurrentUserOrProfile(UserHandle.getCallingUserId())) {
            Slog.w(TAG, "Rejecting " + opPackageName + " ; not a current user or profile");
            return false;
        } else if (this.mAppOps.noteOp(55, uid, opPackageName) != 0) {
            Slog.w(TAG, "Rejecting " + opPackageName + " ; permission denied");
            return false;
        } else if (!foregroundOnly || isForegroundActivity(uid, pid)) {
            return DEBUG;
        } else {
            Slog.w(TAG, "Rejecting " + opPackageName + " ; not in foreground");
            return false;
        }
    }

    private boolean isKeyguard(String clientPackage) {
        return this.mKeyguardPackage.equals(clientPackage);
    }

    private void addLockoutResetMonitor(FingerprintServiceLockoutResetMonitor monitor) {
        if (!this.mLockoutMonitors.contains(monitor)) {
            this.mLockoutMonitors.add(monitor);
        }
    }

    private void removeLockoutResetCallback(FingerprintServiceLockoutResetMonitor monitor) {
        this.mLockoutMonitors.remove(monitor);
    }

    private void notifyLockoutResetMonitors() {
        for (int i = 0; i < this.mLockoutMonitors.size(); i++) {
            ((FingerprintServiceLockoutResetMonitor) this.mLockoutMonitors.get(i)).sendLockoutReset();
        }
    }

    private void startAuthentication(IBinder token, long opId, int callingUserId, int groupId, IFingerprintServiceReceiver receiver, int flags, boolean restricted, String opPackageName) {
        int newGroupId = groupId;
        updateActiveGroup(groupId, opPackageName);
        Slog.v(TAG, "startAuthentication(" + opPackageName + ")");
        if (shouldAuthBothSpaceFingerprints(opPackageName, flags)) {
            Slog.i(TAG, "should authenticate both space fingerprints");
            newGroupId = SPECIAL_USER_ID;
        }
        final String str = opPackageName;
        AuthenticationClient client = new AuthenticationClient(getContext(), this.mHalDeviceId, token, receiver, this.mCurrentUserId, newGroupId, opId, restricted, opPackageName, flags) {
            public boolean onAuthenticated(int fingerId, int groupId) {
                IFingerprintServiceReceiver receiver = getReceiver();
                boolean authenticated = fingerId != 0 ? FingerprintService.DEBUG : false;
                if (receiver != null) {
                    if (authenticated) {
                        Flog.bdReport(FingerprintService.this.mContext, 8, "{pkg:" + str + ",ErrorCount:" + FingerprintService.this.mHwFailedAttempts + "}");
                    } else if (FingerprintService.this.auTime - FingerprintService.this.downTime > 0) {
                        Flog.bdReport(FingerprintService.this.mContext, 7, "{CostTime:" + (FingerprintService.this.auTime - FingerprintService.this.downTime) + "}");
                    }
                }
                return super.onAuthenticated(fingerId, groupId);
            }

            public boolean handleFailedAttempt() {
                boolean z = false;
                boolean noNeedAddFailedAttemps = false;
                if (this.mFlags == FingerprintService.HW_FP_NO_COUNT_FAILED_ATTEMPS && "com.android.settings".equals(getOwnerString())) {
                    noNeedAddFailedAttemps = FingerprintService.DEBUG;
                    Slog.i(FingerprintService.TAG, "no need count failed attempts");
                }
                if (!noNeedAddFailedAttemps) {
                    FingerprintService fingerprintService = FingerprintService.this;
                    fingerprintService.mFailedAttempts = fingerprintService.mFailedAttempts + 1;
                }
                if (!inLockoutMode()) {
                    return false;
                }
                FingerprintService.this.mLockoutTime = SystemClock.elapsedRealtime();
                FingerprintService.this.scheduleLockoutReset();
                if (!FingerprintService.this.isKeyguard(str)) {
                    z = FingerprintService.DEBUG;
                }
                return z;
            }

            public void resetFailedAttempts() {
                if (inLockoutMode()) {
                    Slog.v(FingerprintService.TAG, "resetFailedAttempts should be called from APP");
                } else {
                    FingerprintService.this.resetFailedAttempts();
                }
            }

            public void notifyUserActivity() {
                FingerprintService.this.userActivity();
            }

            public IFingerprintDaemon getFingerprintDaemon() {
                return FingerprintService.this.getFingerprintDaemon();
            }

            public void handleHwFailedAttempt(int flags, String packagesName) {
                FingerprintService.this.handleHwFailedAttempt(flags, packagesName);
            }

            public boolean inLockoutMode() {
                return FingerprintService.this.inLockoutMode();
            }
        };
        if (!inLockoutMode() || isKeyguard(opPackageName)) {
            startClient(client, DEBUG);
            return;
        }
        Slog.v(TAG, "In lockout mode; disallowing authentication");
        if (!client.onError(7)) {
            Slog.w(TAG, "Cannot send timeout message to client");
        }
    }

    private void startEnrollment(IBinder token, byte[] cryptoToken, int userId, IFingerprintServiceReceiver receiver, int flags, boolean restricted, String opPackageName) {
        updateActiveGroup(userId, opPackageName);
        int groupId = userId;
        startClient(new EnrollClient(getContext(), this.mHalDeviceId, token, receiver, userId, userId, cryptoToken, restricted, opPackageName) {
            public IFingerprintDaemon getFingerprintDaemon() {
                return FingerprintService.this.getFingerprintDaemon();
            }

            public void notifyUserActivity() {
                FingerprintService.this.userActivity();
            }
        }, DEBUG);
    }

    private boolean isHwTransactInterest(int code) {
        if (code == CODE_IS_FP_NEED_CALIBRATE_RULE || code == CODE_SET_CALIBRATE_MODE_RULE || code == CODE_GET_TOKEN_LEN_RULE) {
            return DEBUG;
        }
        return false;
    }

    private void dumpInternal(PrintWriter pw) {
        JSONObject dump = new JSONObject();
        try {
            dump.put("service", "Fingerprint Manager");
            JSONArray sets = new JSONArray();
            for (UserInfo user : UserManager.get(getContext()).getUsers()) {
                int userId = user.getUserHandle().getIdentifier();
                int N = this.mFingerprintUtils.getFingerprintsForUser(this.mContext, userId).size();
                JSONObject set = new JSONObject();
                set.put("id", userId);
                set.put("count", N);
                sets.put(set);
            }
            dump.put("prints", sets);
        } catch (JSONException e) {
            Slog.e(TAG, "dump formatting failure", e);
        }
        pw.println(dump);
    }

    public void onStart() {
        publishBinderService("fingerprint", new FingerprintServiceWrapper());
        IFingerprintDaemon daemon = getFingerprintDaemon();
        Slog.v(TAG, "Fingerprint HAL id: " + this.mHalDeviceId);
        listenForUserSwitches();
    }

    public void onBootPhase(int phase) {
        Slog.d(TAG, "Fingerprint daemon is phase :" + phase);
        if (phase == 1000) {
            Slog.d(TAG, "Fingerprint mDaemon is " + this.mDaemon);
            if (getFingerprintDaemon() == null) {
                Slog.w(TAG, "Fingerprint daemon is null");
            }
        }
    }

    private void updateActiveGroup(int userId, String clientPackage) {
        IFingerprintDaemon daemon = getFingerprintDaemon();
        if (daemon != null) {
            try {
                userId = getUserOrWorkProfileId(clientPackage, userId);
                if (userId != this.mCurrentUserId) {
                    File systemDir;
                    int userIdForHal = userId;
                    UserInfo info = this.mUserManager.getUserInfo(userId);
                    if (info != null && info.isHwHiddenSpace()) {
                        userIdForHal = HIDDEN_SPACE_ID;
                        Slog.i(TAG, "userIdForHal is " + HIDDEN_SPACE_ID);
                    }
                    if (userIdForHal == HIDDEN_SPACE_ID) {
                        Slog.i(TAG, "userIdForHal == HIDDEN_SPACE_ID");
                        systemDir = Environment.getUserSystemDirectory(0);
                    } else {
                        systemDir = Environment.getUserSystemDirectory(userId);
                    }
                    File fpDir = new File(systemDir, FP_DATA_DIR);
                    if (!fpDir.exists()) {
                        if (!fpDir.mkdir()) {
                            Slog.v(TAG, "Cannot make directory: " + fpDir.getAbsolutePath());
                            return;
                        } else if (!SELinux.restorecon(fpDir)) {
                            Slog.w(TAG, "Restorecons failed. Directory will have wrong label.");
                            return;
                        }
                    }
                    daemon.setActiveGroup(userIdForHal, fpDir.getAbsolutePath().getBytes());
                    this.mCurrentUserId = userId;
                    updateFingerprints(userId);
                }
                this.mCurrentAuthenticatorId = daemon.getAuthenticatorId();
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to setActiveGroup():", e);
            }
        }
    }

    private int getUserOrWorkProfileId(String clientPackage, int userId) {
        if (isKeyguard(clientPackage) || !isWorkProfile(userId)) {
            return getEffectiveUserId(userId);
        }
        return userId;
    }

    private boolean isWorkProfile(int userId) {
        UserInfo info = this.mUserManager.getUserInfo(userId);
        return info != null ? info.isManagedProfile() : false;
    }

    private void listenForUserSwitches() {
        try {
            ActivityManagerNative.getDefault().registerUserSwitchObserver(new SynchronousUserSwitchObserver() {
                public void onUserSwitching(int newUserId) throws RemoteException {
                    FingerprintService.this.mHandler.obtainMessage(10, newUserId, 0).sendToTarget();
                }

                public void onUserSwitchComplete(int newUserId) throws RemoteException {
                }

                public void onForegroundProfileSwitch(int newProfileId) {
                }
            });
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed to listen for user switching event", e);
        }
    }

    public long getAuthenticatorId(String opPackageName) {
        return this.mCurrentAuthenticatorId;
    }
}
