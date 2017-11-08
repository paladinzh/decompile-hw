package com.android.server.fingerprint;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.IFingerprintDaemon;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.security.GateKeeper;
import android.util.Log;
import android.util.Slog;
import com.android.server.fingerprint.HwFingerprintSets.HwFingerprintGroup;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.os.UserManagerEx;
import com.huawei.fingerprint.IAuthenticator;
import com.huawei.fingerprint.IAuthenticator.Stub;
import com.huawei.fingerprint.IAuthenticatorListener;
import com.huawei.fingerprint.IFidoAuthenticationCallback;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

public class HwFingerprintService extends FingerprintService {
    public static final int CHECK_NEED_CALIBRATE_FINGER = 1004;
    public static final int CHECK_NEED_REENROLL_FINGER = 1003;
    private static final int CODE_GET_TOKEN_LEN_RULE = 1103;
    private static final int CODE_IS_FP_NEED_CALIBRATE_RULE = 1101;
    private static final int CODE_SET_CALIBRATE_MODE_RULE = 1102;
    private static final String DESCRIPTOR = "android.hardware.fingerprint.IFingerprintDaemon";
    private static final String DESCRIPTOR_FINGERPRINT_SERVICE = "android.hardware.fingerprint.IFingerprintService";
    private static final String FIDO_ASM = "com.huawei.hwasm";
    private static final String FINGERPRINTD = "android.hardware.fingerprint.IFingerprintDaemon";
    public static final int GET_CALIBRATE_TOKEN_LEN = 1006;
    public static final int GET_OLD_DATA = 100;
    private static final int HIDDEN_SPACE_ID = -100;
    private static final int HW_FP_AUTH_BOTH_SPACE = 100;
    private static final int PRIMARY_USER_ID = 0;
    public static final int REMOVE_USER_DATA = 101;
    private static final String SECURE_USER_ID_UPDATED = "is_secure_user_id_updated";
    public static final int SET_CALIBRATE_MODE = 1005;
    public static final int SET_LIVENESS_SWITCH = 1002;
    private static final String TAG = "HwFingerprintService";
    private static final int UPDATE_SECURITY_USER_ID = 102;
    public static final int VERIFY_USER = 1001;
    private static boolean mCheckNeedEnroll = true;
    private static boolean mLivenessNeedBetaQualification = false;
    private final Context mContext;
    private IAuthenticator mIAuthenticator = new Stub() {
        public int verifyUser(IFingerprintServiceReceiver receiver, IAuthenticatorListener listener, int userid, byte[] nonce, String aaid) {
            Log.d(HwFingerprintService.TAG, "verifyUser");
            if (!HwFingerprintService.this.isCurrentUserOrProfile(UserHandle.getCallingUserId())) {
                Log.w(HwFingerprintService.TAG, "Can't authenticate non-current user");
                return -1;
            } else if (receiver == null || listener == null || nonce == null || aaid == null) {
                Log.e(HwFingerprintService.TAG, "wrong paramers.");
                return -1;
            } else {
                int uid = Binder.getCallingUid();
                int pid = Binder.getCallingPid();
                Log.d(HwFingerprintService.TAG, "uid =" + uid);
                if (uid != 1000) {
                    Log.e(HwFingerprintService.TAG, "permission denied.");
                    return -1;
                }
                if (((Boolean) HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "canUseFingerprint", new Class[]{String.class, Boolean.TYPE, Integer.TYPE, Integer.TYPE}, new Object[]{HwFingerprintService.FIDO_ASM, Boolean.valueOf(true), Integer.valueOf(uid), Integer.valueOf(pid)})).booleanValue()) {
                    final int effectiveGroupId = HwFingerprintService.this.getEffectiveUserId(userid);
                    final int callingUserId = UserHandle.getCallingUserId();
                    final IFingerprintServiceReceiver iFingerprintServiceReceiver = receiver;
                    final IAuthenticatorListener iAuthenticatorListener = listener;
                    final String str = aaid;
                    final byte[] bArr = nonce;
                    HwFingerprintService.this.mHandler.post(new Runnable() {
                        public void run() {
                            HwFingerprintService.this.setLivenessSwitch("fido");
                            HwFingerprintService.this.startAuthentication(iFingerprintServiceReceiver.asBinder(), 0, callingUserId, effectiveGroupId, iFingerprintServiceReceiver, 0, true, HwFingerprintService.FIDO_ASM, iAuthenticatorListener, str, bArr);
                        }
                    });
                    return 0;
                }
                Log.w(HwFingerprintService.TAG, "FIDO_ASM can't use fingerprint");
                return -1;
            }
        }
    };
    private BroadcastReceiver mUserDeletedMonitor = new BroadcastReceiver() {
        private static final String FP_DATA_DIR = "fpdata";

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("android.intent.action.USER_REMOVED")) {
                    int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                    Slog.i(HwFingerprintService.TAG, "user deleted:" + userId);
                    if (userId == -1) {
                        Slog.i(HwFingerprintService.TAG, "get User id failed");
                        return;
                    }
                    int newUserId = userId;
                    int newPathId = userId;
                    if (UserManagerEx.isHwHiddenSpace(UserManager.get(HwFingerprintService.this.mContext).getUserInfo(userId))) {
                        newUserId = HwFingerprintService.HIDDEN_SPACE_ID;
                        newPathId = 0;
                    }
                    File fpDir = new File(Environment.getUserSystemDirectory(newPathId), FP_DATA_DIR);
                    if (fpDir.exists()) {
                        try {
                            HwFingerprintService.this.removeUserData(newUserId, fpDir.getAbsolutePath().getBytes("utf-8"));
                        } catch (UnsupportedEncodingException e) {
                            Log.e(HwFingerprintService.TAG, "UnsupportedEncodingException");
                        }
                    } else {
                        Slog.v(HwFingerprintService.TAG, "no fpdata!");
                    }
                } else if (action.equals("android.intent.action.USER_PRESENT")) {
                    if (HwFingerprintService.mCheckNeedEnroll) {
                        int checkValReEnroll = HwFingerprintService.this.checkNeedReEnrollFingerPrints();
                        int checkValCalibrate = HwFingerprintService.this.checkNeedCalibrateFingerPrint();
                        Log.e(HwFingerprintService.TAG, "USER_PRESENT mUserDeletedMonitor need enrol : " + checkValReEnroll + "need calibrate:" + checkValCalibrate);
                        if (checkValReEnroll == 1 && checkValCalibrate != 1) {
                            HwFingerprintService.this.intentOthers(context);
                        }
                        HwFingerprintService.mCheckNeedEnroll = false;
                    }
                    if (Secure.getInt(context.getContentResolver(), HwFingerprintService.SECURE_USER_ID_UPDATED, 0) != 1) {
                        try {
                            if (HwFingerprintService.this.updateSecurityUserId(GateKeeper.getSecureUserId()) == 0) {
                                Secure.putInt(context.getContentResolver(), HwFingerprintService.SECURE_USER_ID_UPDATED, 1);
                            }
                        } catch (IllegalStateException ex) {
                            Slog.e(HwFingerprintService.TAG, "getSecureUserId failed ex = " + ex);
                        }
                    }
                }
            }
        }
    };

    private class HwFIDOAuthenticationClient extends AuthenticationClient {
        private String aaid;
        private int groupId;
        private IAuthenticatorListener listener;
        private IFidoAuthenticationCallback mFidoAuthenticationCallback = new IFidoAuthenticationCallback.Stub() {
            public int onUserVerificationResult(final int result, long opId, final byte[] userId, final byte[] encapsulatedResult) {
                Log.d(HwFingerprintService.TAG, "onUserVerificationResult");
                HwFingerprintService.this.mHandler.post(new Runnable() {
                    public void run() {
                        Log.d(HwFingerprintService.TAG, "onUserVerificationResult-run");
                        if (HwFIDOAuthenticationClient.this.listener != null) {
                            try {
                                HwFIDOAuthenticationClient.this.listener.onUserVerificationResult(result, userId, encapsulatedResult);
                            } catch (RemoteException e) {
                                Log.w(HwFingerprintService.TAG, "onUserVerificationResult RemoteException");
                            }
                        }
                    }
                });
                return 0;
            }
        };
        private byte[] nonce;
        private String pkgName;

        public HwFIDOAuthenticationClient(Context context, long halDeviceId, IBinder token, IFingerprintServiceReceiver receiver, int callingUserId, int groupId, long opId, boolean restricted, String owner, IAuthenticatorListener listener, String aaid, byte[] nonce) {
            super(context, halDeviceId, token, receiver, callingUserId, groupId, opId, restricted, owner);
            this.pkgName = owner;
            this.listener = listener;
            this.groupId = groupId;
            this.aaid = aaid;
            this.nonce = nonce;
        }

        public boolean onAuthenticated(int fingerId, int groupId) {
            if (fingerId != 0) {
            }
            return super.onAuthenticated(fingerId, groupId);
        }

        public boolean handleFailedAttempt() {
            HwFingerprintService.setParentPrivateField(HwFingerprintService.this, "mFailedAttempts", Integer.valueOf(((Integer) HwFingerprintService.getParentPrivateField(HwFingerprintService.this, "mFailedAttempts")).intValue() + 1));
            if (!inLockoutMode()) {
                return false;
            }
            HwFingerprintService.setParentPrivateField(HwFingerprintService.this, "mLockoutTime", Long.valueOf(SystemClock.elapsedRealtime()));
            HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "scheduleLockoutReset", null, null);
            onError(7);
            stop(true);
            return !((Boolean) HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "isKeyguard", new Class[]{String.class}, new Object[]{this.pkgName})).booleanValue();
        }

        public void resetFailedAttempts() {
            HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "resetFailedAttempts", null, null);
        }

        public void notifyUserActivity() {
            HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "userActivity", null, null);
        }

        public IFingerprintDaemon getFingerprintDaemon() {
            return (IFingerprintDaemon) HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "getFingerprintDaemon", null, null);
        }

        public void handleHwFailedAttempt(int flags, String packagesName) {
            HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "handleHwFailedAttempt", new Class[]{Integer.TYPE, String.class}, new Object[]{Integer.valueOf(0), null});
        }

        public boolean inLockoutMode() {
            return ((Boolean) HwFingerprintService.invokeParentPrivateFunction(HwFingerprintService.this, "inLockoutMode", null, null)).booleanValue();
        }

        public int start() {
            try {
                doVerifyUser(this.groupId, this.aaid, this.nonce);
            } catch (RemoteException e) {
                Log.w(HwFingerprintService.TAG, "call fingerprintD verify user failed");
            }
            return 0;
        }

        private void doVerifyUser(int groupId, String aaid, byte[] nonce) throws RemoteException {
            if (HwFingerprintService.this.isFingerprintDReady()) {
                IBinder remote = ServiceManager.getService("android.hardware.fingerprint.IFingerprintDaemon");
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken("android.hardware.fingerprint.IFingerprintDaemon");
                    data.writeStrongBinder(this.mFidoAuthenticationCallback.asBinder());
                    data.writeInt(groupId);
                    data.writeString(aaid);
                    data.writeByteArray(nonce);
                    remote.transact(1001, data, reply, 0);
                    reply.readException();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }
        }
    }

    private void startAuthentication(IBinder token, long opId, int callingUserId, int groupId, IFingerprintServiceReceiver receiver, int flags, boolean restricted, String opPackageName, IAuthenticatorListener listener, String aaid, byte[] nonce) {
        invokeParentPrivateFunction(this, "updateActiveGroup", new Class[]{Integer.TYPE, String.class}, new Object[]{Integer.valueOf(groupId), opPackageName});
        Log.v(TAG, "HwFingerprintService-startAuthentication(" + opPackageName + ")");
        AuthenticationClient client = new HwFIDOAuthenticationClient(getContext(), 0, token, receiver, callingUserId, groupId, opId, restricted, opPackageName, listener, aaid, nonce);
        if (((Boolean) invokeParentPrivateFunction(this, "inLockoutMode", null, null)).booleanValue()) {
            if (!((Boolean) invokeParentPrivateFunction(this, "isKeyguard", new Class[]{String.class}, new Object[]{opPackageName})).booleanValue()) {
                Log.v(TAG, "In lockout mode; disallowing authentication");
                if (!client.onError(7)) {
                    Log.w(TAG, "Cannot send timeout message to client");
                }
                return;
            }
        }
        invokeParentPrivateFunction(this, "startClient", new Class[]{ClientMonitor.class, Boolean.TYPE}, new Object[]{client, Boolean.valueOf(true)});
    }

    private static Object getParentPrivateField(Object instance, String variableName) {
        Class targetClass = instance.getClass().getSuperclass();
        Object superInst = targetClass.cast(instance);
        try {
            final Field field = targetClass.getDeclaredField(variableName);
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    field.setAccessible(true);
                    return null;
                }
            });
            return field.get(superInst);
        } catch (Exception e) {
            Log.v(TAG, "getParentPrivateField error", e);
            return null;
        }
    }

    private static void setParentPrivateField(Object instance, String variableName, Object value) {
        Class targetClass = instance.getClass().getSuperclass();
        Object superInst = targetClass.cast(instance);
        try {
            final Field field = targetClass.getDeclaredField(variableName);
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    field.setAccessible(true);
                    return null;
                }
            });
            field.set(superInst, value);
        } catch (Exception e) {
            Log.v(TAG, "setParentPrivateField error", e);
        }
    }

    private static Object invokeParentPrivateFunction(Object instance, String method, Class[] paramTypes, Object[] params) {
        Class targetClass = instance.getClass().getSuperclass();
        Object superInst = targetClass.cast(instance);
        try {
            final Method med = targetClass.getDeclaredMethod(method, paramTypes);
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    med.setAccessible(true);
                    return null;
                }
            });
            return med.invoke(superInst, params);
        } catch (Exception e) {
            Log.v(TAG, "invokeParentPrivateFunction error", e);
            return null;
        }
    }

    private boolean isBetaUser() {
        int userType = SystemProperties.getInt("ro.logsystem.usertype", 0);
        if (userType == 3 || userType == 5) {
            return true;
        }
        return false;
    }

    private void intentOthers(Context context) {
        Intent intent = new Intent();
        intent.setAction("com.android.settings.fingerprint.FingerprintMainSettings");
        intent.setPackage(WifiProCommonUtils.HUAWEI_SETTINGS);
        intent.addFlags(268435456);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Activity not found");
        }
    }

    public HwFingerprintService(Context context) {
        super(context);
        this.mContext = context;
    }

    public void onStart() {
        super.onStart();
        publishBinderService("fido_authenticator", this.mIAuthenticator.asBinder());
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_REMOVED");
        filter.addAction("android.intent.action.USER_PRESENT");
        this.mContext.registerReceiver(this.mUserDeletedMonitor, filter);
        Slog.v(TAG, "HwFingerprintService onstart");
    }

    public void updateFingerprints(int userId) {
        HwFingerprintSets hwFpSets = remoteGetOldData();
        if (hwFpSets != null) {
            FingerprintUtils utils = FingerprintUtils.getInstance();
            if (utils != null) {
                int i;
                ArrayList mNewFingerprints = null;
                for (i = 0; i < hwFpSets.mFingerprintGroups.size(); i++) {
                    HwFingerprintGroup fpGroup = (HwFingerprintGroup) hwFpSets.mFingerprintGroups.get(i);
                    int realGroupId = fpGroup.mGroupId;
                    if (fpGroup.mGroupId == HIDDEN_SPACE_ID) {
                        realGroupId = getRealUserIdForApp(fpGroup.mGroupId);
                    }
                    if (realGroupId == userId) {
                        mNewFingerprints = fpGroup.mFingerprints;
                    }
                }
                if (mNewFingerprints == null) {
                    mNewFingerprints = new ArrayList();
                }
                for (Fingerprint oldFp : utils.getFingerprintsForUser(this.mContext, userId)) {
                    if (!checkItemExist(oldFp.getFingerId(), mNewFingerprints)) {
                        utils.removeFingerprintIdForUser(this.mContext, oldFp.getFingerId(), userId);
                    }
                }
                for (i = 0; i < mNewFingerprints.size(); i++) {
                    Fingerprint fp = (Fingerprint) mNewFingerprints.get(i);
                    utils.addFingerprintForUser(this.mContext, fp.getFingerId(), userId);
                    CharSequence fpName = fp.getName();
                    if (!(fpName == null || fpName.toString().isEmpty())) {
                        utils.renameFingerprintForUser(this.mContext, fp.getFingerId(), userId, fpName);
                    }
                }
            }
        }
    }

    public int removeUserData(int groupId, byte[] path) {
        if (!isFingerprintDReady()) {
            return -1;
        }
        IBinder remote = ServiceManager.getService("android.hardware.fingerprint.IFingerprintDaemon");
        if (remote == null) {
            return -1;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("android.hardware.fingerprint.IFingerprintDaemon");
            data.writeInt(groupId);
            data.writeByteArray(path);
            remote.transact(101, data, reply, 0);
            reply.readException();
            reply.readInt();
        } catch (RemoteException e) {
            Slog.e(TAG, "removeUserData RemoteException:" + e);
        } finally {
            reply.recycle();
            data.recycle();
        }
        return 0;
    }

    private int checkNeedReEnrollFingerPrints() {
        Log.w(TAG, "checkNeedReEnrollFingerPrints");
        if (!isFingerprintDReady()) {
            return -1;
        }
        IBinder remote = ServiceManager.getService("android.hardware.fingerprint.IFingerprintDaemon");
        if (remote == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return -1;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int result = -1;
        Log.w(TAG, "pacel  packaged ");
        try {
            data.writeInterfaceToken("android.hardware.fingerprint.IFingerprintDaemon");
            remote.transact(1003, data, reply, 0);
            reply.readException();
            result = reply.readInt();
        } catch (RemoteException e) {
            Log.e(TAG, "checkNeedReEnrollFingerPrints RemoteException:" + e);
        } finally {
            reply.recycle();
            data.recycle();
        }
        Log.w(TAG, "framework setLivenessSwitch is finish return = " + result);
        return result;
    }

    public boolean onHwTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        int result;
        if (code == CODE_IS_FP_NEED_CALIBRATE_RULE) {
            Slog.d(TAG, "code == CODE_IS_FP_NEED_CALIBRATE_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            result = checkNeedCalibrateFingerPrint();
            reply.writeNoException();
            reply.writeInt(result);
            return true;
        } else if (code == CODE_SET_CALIBRATE_MODE_RULE) {
            Slog.d(TAG, "code == CODE_SET_CALIBRATE_MODE_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            setCalibrateMode(data.readInt());
            reply.writeNoException();
            return true;
        } else if (code != CODE_GET_TOKEN_LEN_RULE) {
            return super.onHwTransact(code, data, reply, flags);
        } else {
            Slog.d(TAG, "code == CODE_GET_TOKEN_LEN_RULE");
            data.enforceInterface(DESCRIPTOR_FINGERPRINT_SERVICE);
            result = getTokenLen();
            reply.writeNoException();
            reply.writeInt(result);
            return true;
        }
    }

    public int checkNeedCalibrateFingerPrint() {
        if (!isFingerprintDReady()) {
            return -1;
        }
        IBinder remote = ServiceManager.getService("android.hardware.fingerprint.IFingerprintDaemon");
        if (remote == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return -1;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int result = -1;
        Slog.d(TAG, "pacel  packaged :checkNeedCalibrateFingerPrint");
        try {
            data.writeInterfaceToken("android.hardware.fingerprint.IFingerprintDaemon");
            remote.transact(1004, data, reply, 0);
            reply.readException();
            result = reply.readInt();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException:" + e);
        } finally {
            reply.recycle();
            data.recycle();
        }
        Slog.d(TAG, "fingerprintd calibrate return = " + result);
        return result;
    }

    public void setCalibrateMode(int mode) {
        if (isFingerprintDReady()) {
            IBinder remote = ServiceManager.getService("android.hardware.fingerprint.IFingerprintDaemon");
            if (remote == null) {
                Slog.e(TAG, "Fingerprintd is not available!");
                return;
            }
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            Slog.d(TAG, "pacel  packaged setCalibrateMode: " + mode);
            try {
                data.writeInterfaceToken("android.hardware.fingerprint.IFingerprintDaemon");
                data.writeInt(mode);
                remote.transact(1005, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException:" + e);
            } finally {
                reply.recycle();
                data.recycle();
            }
            return;
        }
        Log.w(TAG, "FingerprintD is not Ready");
    }

    public int getTokenLen() {
        if (!isFingerprintDReady()) {
            return -1;
        }
        IBinder remote = ServiceManager.getService("android.hardware.fingerprint.IFingerprintDaemon");
        if (remote == null) {
            Slog.e(TAG, "Fingerprintd is not available!");
            return -1;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int result = -1;
        Slog.d(TAG, "pacel  packaged :getTokenLen");
        try {
            data.writeInterfaceToken("android.hardware.fingerprint.IFingerprintDaemon");
            remote.transact(1006, data, reply, 0);
            reply.readException();
            result = reply.readInt();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException:" + e);
        } finally {
            reply.recycle();
            data.recycle();
        }
        Slog.d(TAG, "fingerprintd getTokenLen token len = " + result);
        return result;
    }

    private int checkForegroundNeedLiveness() {
        Slog.w(TAG, "checkForegroundNeedLiveness:start");
        try {
            List<RunningAppProcessInfo> procs = ActivityManagerNative.getDefault().getRunningAppProcesses();
            if (procs == null) {
                return 0;
            }
            int N = procs.size();
            for (int i = 0; i < N; i++) {
                RunningAppProcessInfo proc = (RunningAppProcessInfo) procs.get(i);
                if (proc.importance == 100) {
                    if ("com.alipay.security.mobile.authentication.huawei".equals(proc.processName)) {
                        Slog.w(TAG, "ForegroundActivity is " + proc.processName + "need liveness auth");
                        return 1;
                    } else if ("com.huawei.wallet".equals(proc.processName)) {
                        Slog.w(TAG, "ForegroundActivity is " + proc.processName + "need liveness auth");
                        return 1;
                    } else if ("com.huawei.android.hwpay".equals(proc.processName)) {
                        Slog.w(TAG, "ForegroundActivity is " + proc.processName + "need liveness auth");
                        return 1;
                    }
                }
            }
            return 0;
        } catch (RemoteException e) {
            Slog.w(TAG, "am.getRunningAppProcesses() failed in checkForegroundNeedLiveness");
        }
    }

    private int checkNeedLivenessList(String opPackageName) {
        Slog.w(TAG, "checkNeedLivenessList:start");
        if (opPackageName == null || opPackageName.equals("com.android.keyguard")) {
            return 0;
        }
        if (opPackageName.equals("com.huawei.securitymgr")) {
            return checkForegroundNeedLiveness();
        }
        return (opPackageName.equals("com.eg.android.AlipayGphone") || opPackageName.equals("fido") || opPackageName.equals("com.alipay.security.mobile.authentication.huawei") || opPackageName.equals("com.huawei.wallet") || opPackageName.equals("com.huawei.android.hwpay")) ? 1 : 0;
    }

    protected void setLivenessSwitch(String opPackageName) {
        Slog.w(TAG, "setLivenessSwitch:start");
        if ((!mLivenessNeedBetaQualification || isBetaUser()) && isFingerprintDReady()) {
            int NEED_LIVENESS_AUTHENTICATION = checkNeedLivenessList(opPackageName);
            Slog.w(TAG, "NEED_LIVENESS_AUTHENTICATION = " + NEED_LIVENESS_AUTHENTICATION);
            IBinder remote = ServiceManager.getService("android.hardware.fingerprint.IFingerprintDaemon");
            if (remote == null) {
                Slog.e(TAG, "Fingerprintd is not available!");
                return;
            }
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken("android.hardware.fingerprint.IFingerprintDaemon");
                data.writeInt(NEED_LIVENESS_AUTHENTICATION);
                remote.transact(1002, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                Slog.e(TAG, "setLivenessSwitch RemoteException:" + e);
            } finally {
                reply.recycle();
                data.recycle();
            }
            Slog.w(TAG, "framework setLivenessSwitch is ok ---end");
        }
    }

    private boolean checkPackageName(String opPackageName) {
        if (opPackageName == null || !opPackageName.equals("com.android.systemui")) {
            return false;
        }
        return true;
    }

    public boolean shouldAuthBothSpaceFingerprints(String opPackageName, int flags) {
        if (checkPackageName(opPackageName) && flags == 100) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private HwFingerprintSets remoteGetOldData() {
        if (!isFingerprintDReady()) {
            return null;
        }
        IBinder remote = ServiceManager.getService("android.hardware.fingerprint.IFingerprintDaemon");
        if (remote == null) {
            return null;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        HwFingerprintSets hwFingerprintSets = null;
        try {
            _data.writeInterfaceToken("android.hardware.fingerprint.IFingerprintDaemon");
            remote.transact(100, _data, _reply, 0);
            _reply.readException();
            if (_reply.readInt() != 0) {
                hwFingerprintSets = (HwFingerprintSets) HwFingerprintSets.CREATOR.createFromParcel(_reply);
            } else {
                hwFingerprintSets = null;
            }
            _reply.recycle();
            _data.recycle();
        } catch (RemoteException e) {
            Slog.e(TAG, "remoteGetOldData RemoteException:" + e);
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
        }
        return hwFingerprintSets;
    }

    private static boolean checkItemExist(int oldFpId, ArrayList<Fingerprint> fingerprints) {
        for (int i = 0; i < fingerprints.size(); i++) {
            if (((Fingerprint) fingerprints.get(i)).getFingerId() == oldFpId) {
                fingerprints.remove(i);
                return true;
            }
        }
        return false;
    }

    private int updateSecurityUserId(long securityId) {
        int result = -1;
        if (!isFingerprintDReady()) {
            return result;
        }
        IBinder remote = ServiceManager.getService("android.hardware.fingerprint.IFingerprintDaemon");
        if (remote == null) {
            Slog.e(TAG, "updateSecurityUserId getService is null");
            return result;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("android.hardware.fingerprint.IFingerprintDaemon");
            data.writeLong(securityId);
            remote.transact(102, data, reply, 0);
            reply.readException();
            result = reply.readInt();
        } catch (RemoteException e) {
            Slog.e(TAG, "updateSecurityId RemoteException:" + e);
        } finally {
            reply.recycle();
            data.recycle();
        }
        Slog.i(TAG, "updateSecurityUserId result = " + result);
        return result;
    }

    private boolean isFingerprintDReady() {
        if (getFingerprintDaemon() != null) {
            return true;
        }
        Slog.w(TAG, "isFingerprintDReady: no fingeprintd!");
        return false;
    }
}
