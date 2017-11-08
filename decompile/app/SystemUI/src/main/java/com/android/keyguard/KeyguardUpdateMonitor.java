package com.android.keyguard;

import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.IUserSwitchObserver.Stub;
import android.app.PendingIntent;
import android.app.trust.TrustManager;
import android.app.trust.TrustManager.TrustListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import android.hardware.fingerprint.FingerprintManager.LockoutResetCallback;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.util.ArraySet;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.HwKeyguardUpdateMonitor.SafeArrayList;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.policy.RetryPolicy;
import com.huawei.keyguard.support.HiddenSpace;
import com.huawei.keyguard.support.RemoteLockUtils;
import com.huawei.keyguard.support.magazine.MagazineUtils;
import com.huawei.keyguard.theme.KeyguardTheme;
import com.huawei.keyguard.util.DisabledFeatureUtils;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.OsUtils;
import fyusion.vislib.BuildConfig;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public abstract class KeyguardUpdateMonitor implements TrustListener, OnCancelListener {
    private AlarmManager mAlarmManager;
    private AuthenticationCallback mAuthenticationCallback = new AuthenticationCallback() {
        public void onAuthenticationFailed() {
            if (KeyguardUpdateMonitor.this.isFingerprintDetectionRunning()) {
                HwLog.v("KeyguardUpdateMonitor", "KGSvcFp Finger onAuthenticationFailed ");
                KeyguardUpdateMonitor.this.handleFingerprintAuthFailed();
                return;
            }
            HwLog.w("KeyguardUpdateMonitor", "KGSvcFp onAuthenticationFailed Fingerprint should stoped." + KeyguardUpdateMonitor.this.mFingerprintRunningState);
        }

        public void onAuthenticationSucceeded(AuthenticationResult result) {
            if (KeyguardUpdateMonitor.this.isFingerprintDetectionRunning()) {
                HwLog.v("KeyguardUpdateMonitor", "KGSvcFp Finger onAuthenticationSucceeded result= " + result);
                int fingerId = 0;
                int groupId = 0;
                int authUid = -1;
                if (result != null) {
                    Fingerprint fingerprint = result.getFingerprint();
                    fingerId = fingerprint == null ? 0 : fingerprint.getFingerId();
                    groupId = fingerprint == null ? 0 : fingerprint.getGroupId();
                    authUid = result.getUserId();
                }
                KeyguardUpdateMonitor.this.handleFingerprintAuthenticated(groupId, fingerId, authUid);
                return;
            }
            HwLog.w("KeyguardUpdateMonitor", "KGSvcFp onAuthenticationSucceeded FP should stoped." + KeyguardUpdateMonitor.this.mFingerprintRunningState);
        }

        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
            if (!KeyguardUpdateMonitor.this.isFingerprintDetectionRunning()) {
                HwLog.w("KeyguardUpdateMonitor", "KGSvcFp onAuthenticationHelp FP not expected." + KeyguardUpdateMonitor.this.mFingerprintRunningState);
            }
            HwLog.v("KeyguardUpdateMonitor", "KGSvcFp onAuthenticationHelp. msg:" + helpMsgId);
            KeyguardUpdateMonitor.this.handleFingerprintHelp(helpMsgId, helpString.toString());
        }

        public void onAuthenticationError(int errMsgId, CharSequence errString) {
            HwLog.v("KeyguardUpdateMonitor", "KGSvcFp onAuthenticationError." + errMsgId);
            KeyguardUpdateMonitor.this.handleFingerprintError(errMsgId, errString.toString());
        }

        public void onAuthenticationAcquired(int acquireInfo) {
            if (KeyguardUpdateMonitor.this.isFingerprintDetectionRunning()) {
                HwLog.v("KeyguardUpdateMonitor", "KGSvcFp onAuthenticationAcquired." + acquireInfo);
                KeyguardUpdateMonitor.this.handleFingerprintAcquired(acquireInfo);
                return;
            }
            HwLog.w("KeyguardUpdateMonitor", "onAuthenticationAcquired Fingerprint should stoped." + KeyguardUpdateMonitor.this.mFingerprintRunningState);
        }
    };
    private BatteryStatus mBatteryStatus;
    private boolean mBootCompleted;
    protected boolean mBouncer;
    private final BroadcastReceiver mBroadcastAllReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.app.action.NEXT_ALARM_CLOCK_CHANGED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(301);
            } else if ("android.intent.action.USER_INFO_CHANGED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(317, intent.getIntExtra("android.intent.extra.user_handle", getSendingUserId()), 0));
            } else if ("com.android.facelock.FACE_UNLOCK_STARTED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(327, 1, getSendingUserId()));
            } else if ("com.android.facelock.FACE_UNLOCK_STOPPED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(327, 0, getSendingUserId()));
            } else if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(309);
            }
        }
    };
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            HwLog.d("KeyguardUpdateMonitor", "received broadcast " + action);
            if ("android.intent.action.TIME_TICK".equals(action) || "android.intent.action.TIME_SET".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(301);
            } else if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                int maxChargingMicroWatt;
                int status = intent.getIntExtra("status", 1);
                int plugged = intent.getIntExtra("plugged", 0);
                int level = intent.getIntExtra("level", 0);
                int health = intent.getIntExtra("health", 1);
                int maxChargingMicroAmp = intent.getIntExtra("max_charging_current", -1);
                int maxChargingMicroVolt = intent.getIntExtra("max_charging_voltage", -1);
                HwLog.w("KeyguardUpdateMonitor", "ACTION_BATTERY_CHANGED AMP: " + maxChargingMicroAmp + "; Volt: " + maxChargingMicroVolt);
                if (maxChargingMicroVolt <= 0) {
                    maxChargingMicroVolt = 5000000;
                }
                if (maxChargingMicroAmp > 0) {
                    maxChargingMicroWatt = (maxChargingMicroAmp / 1000) * (maxChargingMicroVolt / 1000);
                } else {
                    maxChargingMicroWatt = -1;
                }
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(302, new BatteryStatus(status, level, plugged, health, maxChargingMicroWatt)));
            } else if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                SimData args = SimData.fromIntent(intent);
                HwLog.v("KeyguardUpdateMonitor", "action " + action + " state: " + intent.getStringExtra("ss") + " slotId: " + args.slotId + " subid: " + args.subId);
                KeyguardUpdateMonitor.this.mHandler.obtainMessage(304, args.subId, args.slotId, args.simState).sendToTarget();
            } else if ("android.media.RINGER_MODE_CHANGED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(305, intent.getIntExtra("android.media.EXTRA_RINGER_MODE", -1), 0));
            } else if ("android.intent.action.PHONE_STATE".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(306, intent.getStringExtra("state")));
            } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(329);
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                KeyguardUpdateMonitor.this.dispatchBootCompleted();
            } else if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                KeyguardUpdateMonitor.this.dispatchShutDown();
            } else if ("android.intent.action.SERVICE_STATE".equals(action)) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    ServiceState serviceState = ServiceState.newFromBundle(bundle);
                    int subId = intent.getIntExtra("subscription", -1);
                    HwLog.v("KeyguardUpdateMonitor", "action " + action + " serviceState=" + serviceState + " subId=" + subId);
                    KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(330, subId, 0, serviceState));
                }
            } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                MagazineUtils.sendConnectivityActionToMagazine(context);
            }
        }
    };
    protected final SafeArrayList mCallbacks = new SafeArrayList();
    protected int mCancelSubscriptId = -1;
    protected int mCancelSubscriptSoltId = -1;
    protected final Context mContext;
    private boolean mDeviceInteractive = true;
    protected boolean mDeviceProvisioned;
    private ContentObserver mDeviceProvisionedObserver;
    private DisplayClientState mDisplayClientState = new DisplayClientState();
    private SparseIntArray mFailedAttempts = new SparseIntArray();
    protected boolean mFingerprintAlreadyAuthenticated;
    private CancellationSignal mFingerprintCancelSignal;
    protected int mFingerprintRunningState = 0;
    private FingerprintManager mFpm;
    private boolean mGoingToSleep;
    protected final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            boolean z = false;
            switch (msg.what) {
                case 301:
                    KeyguardUpdateMonitor.this.handleTimeUpdate();
                    return;
                case 302:
                    KeyguardUpdateMonitor.this.handleBatteryUpdate((BatteryStatus) msg.obj);
                    return;
                case 304:
                    KeyguardUpdateMonitor.this.handleSimStateChange(msg.arg1, msg.arg2, (State) msg.obj);
                    return;
                case 305:
                    KeyguardUpdateMonitor.this.handleRingerModeChange(msg.arg1);
                    return;
                case 306:
                    KeyguardUpdateMonitor.this.handlePhoneStateChanged((String) msg.obj);
                    return;
                case 308:
                    KeyguardUpdateMonitor.this.handleDeviceProvisioned();
                    return;
                case 309:
                    KeyguardUpdateMonitor.this.handleDevicePolicyManagerStateChanged();
                    return;
                case 310:
                    KeyguardUpdateMonitor.this.handleUserSwitching(msg.arg1, (IRemoteCallback) msg.obj);
                    return;
                case 312:
                    KeyguardUpdateMonitor.this.handleKeyguardReset();
                    return;
                case 313:
                    KeyguardUpdateMonitor.this.handleBootCompleted();
                    return;
                case 314:
                    KeyguardUpdateMonitor.this.handleUserSwitchComplete(msg.arg1);
                    return;
                case 317:
                    KeyguardUpdateMonitor.this.handleUserInfoChanged(msg.arg1);
                    return;
                case 318:
                    KeyguardUpdateMonitor.this.handleReportEmergencyCallAction();
                    return;
                case 319:
                    KeyguardUpdateMonitor.this.handleStartedWakingUp();
                    return;
                case 320:
                    KeyguardUpdateMonitor.this.handleFinishedGoingToSleep(msg.arg1);
                    return;
                case 321:
                    KeyguardUpdateMonitor.this.handleStartedGoingToSleep(msg.arg1);
                    return;
                case 322:
                    KeyguardUpdateMonitor.this.handleKeyguardBouncerChanged(msg.arg1);
                    return;
                case 327:
                    KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.this;
                    if (msg.arg1 != 0) {
                        z = true;
                    }
                    keyguardUpdateMonitor.handleFaceUnlockStateChanged(z, msg.arg2);
                    return;
                case 328:
                    KeyguardUpdateMonitor.this.handleSimSubscriptionInfoChanged();
                    return;
                case 329:
                    KeyguardUpdateMonitor.this.handleAirplaneModeChanged();
                    return;
                case 330:
                    KeyguardUpdateMonitor.this.handleServiceStateChange(msg.arg1, (ServiceState) msg.obj);
                    return;
                case 331:
                    KeyguardUpdateMonitor.this.handleScreenTurnedOn();
                    return;
                case 332:
                    KeyguardUpdateMonitor.this.handleScreenTurnedOff();
                    return;
                default:
                    KeyguardUpdateMonitor.this.handleExtMessage(msg);
                    return;
            }
        }
    };
    protected boolean mKeyguardIsVisible;
    protected LockPatternUtils mLockPatternUtils = null;
    private final LockoutResetCallback mLockoutResetCallback = new LockoutResetCallback() {
        public void onLockoutReset() {
            KeyguardUpdateMonitor.this.handleFingerprintLockoutReset();
        }
    };
    protected int mPhoneState;
    private boolean mRemoveSimInfoListen = true;
    private int mRingMode;
    private boolean mScreenOn;
    HashMap<Integer, ServiceState> mServiceStates = new HashMap();
    protected HashMap<Integer, SimData> mSimDatas = new HashMap();
    private int mStartFpUserId = -1;
    protected ArraySet<Integer> mStrongAuthNotTimedOut = new ArraySet();
    private final BroadcastReceiver mStrongAuthTimeoutReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("com.android.systemui.ACTION_STRONG_AUTH_TIMEOUT".equals(intent.getAction())) {
                int userId = intent.getIntExtra("com.android.systemui.USER_ID", -1);
                if (KeyguardCfg.isSupportFpPasswordTimeout()) {
                    KeyguardUpdateMonitor.this.onUserAuthTimeout(userId);
                } else {
                    KeyguardUpdateMonitor.this.mStrongAuthNotTimedOut.remove(Integer.valueOf(userId));
                }
                KeyguardUpdateMonitor.this.notifyStrongAuthStateChanged(userId);
            }
        }
    };
    private final StrongAuthTracker mStrongAuthTracker;
    private List<SubscriptionInfo> mSubscriptionInfo;
    private OnSubscriptionsChangedListener mSubscriptionListener = new OnSubscriptionsChangedListener() {
        public void onSubscriptionsChanged() {
            KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(328);
        }
    };
    private SubscriptionManager mSubscriptionManager;
    private Runnable mSuccessfulStrongAuthUnlockReporter = new Runnable() {
        public void run() {
            KeyguardUpdateMonitor.this.scheduleStrongAuthTimeout();
            if (KeyguardUpdateMonitor.this.mFpm != null) {
                KeyguardUpdateMonitor.this.mFpm.resetTimeout(null);
            }
            if (KeyguardCfg.isSupportFpPasswordTimeout()) {
                KeyguardUpdateMonitor.this.afterUserAuthed();
            }
        }
    };
    private boolean mSwitchingUser;
    private TrustManager mTrustManager;
    private SparseBooleanArray mUserFaceUnlockRunning = new SparseBooleanArray();
    private SparseBooleanArray mUserFingerprintAuthenticated = new SparseBooleanArray();
    private SparseBooleanArray mUserHasTrust = new SparseBooleanArray();
    private SparseBooleanArray mUserTrustIsManaged = new SparseBooleanArray();

    public static class BatteryStatus {
        public final int health;
        public final int level;
        public final int maxChargingWattage;
        public final int plugged;
        public final int status;

        public BatteryStatus(int status, int level, int plugged, int health, int maxChargingWattage) {
            this.status = status;
            this.level = level;
            this.plugged = plugged;
            this.health = health;
            this.maxChargingWattage = maxChargingWattage;
        }

        public boolean isPluggedIn() {
            if (this.plugged == 1 || this.plugged == 2 || this.plugged == 4) {
                return true;
            }
            return false;
        }

        public boolean isCharged() {
            return this.status == 5 || this.level >= 100;
        }

        public boolean isBatteryLow() {
            HwLog.v("KeyguardUpdateMonitor", "RefreshBatteryInfo isBatteryLow: " + this.level);
            return this.level < 20;
        }

        public final int getChargingSpeed(int slowThreshold, int fastThreshold) {
            HwLog.w("KeyguardUpdateMonitor", "ChargingSpeed  Wattage: " + this.maxChargingWattage + " ST: " + slowThreshold + " --> " + fastThreshold);
            if (this.maxChargingWattage <= 0) {
                return -1;
            }
            if (this.maxChargingWattage < slowThreshold) {
                return 0;
            }
            if (this.maxChargingWattage > fastThreshold) {
                return 2;
            }
            return 1;
        }
    }

    static class DisplayClientState {
        DisplayClientState() {
        }
    }

    protected static class SimData {
        public State simState;
        public int slotId;
        public int subId;

        SimData(State state, int slot, int id) {
            this.simState = state;
            this.slotId = slot;
            this.subId = id;
        }

        static SimData fromIntent(Intent intent) {
            if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                State state;
                String stateExtra = intent.getStringExtra("ss");
                int slotId = intent.getIntExtra("slot", 0);
                int subId = intent.getIntExtra("subscription", -1);
                if ("ABSENT".equals(stateExtra)) {
                    if ("PERM_DISABLED".equals(intent.getStringExtra("reason"))) {
                        state = State.PERM_DISABLED;
                    } else {
                        state = State.ABSENT;
                    }
                } else if ("READY".equals(stateExtra)) {
                    state = State.READY;
                } else if ("LOCKED".equals(stateExtra)) {
                    String lockedReason = intent.getStringExtra("reason");
                    if ("PIN".equals(lockedReason)) {
                        state = State.PIN_REQUIRED;
                    } else if ("PUK".equals(lockedReason)) {
                        state = State.PUK_REQUIRED;
                    } else {
                        state = State.UNKNOWN;
                    }
                } else if ("NETWORK".equals(stateExtra)) {
                    state = State.NETWORK_LOCKED;
                } else if ("LOADED".equals(stateExtra) || "IMSI".equals(stateExtra)) {
                    state = State.READY;
                } else {
                    state = State.UNKNOWN;
                }
                return new SimData(state, slotId, subId);
            }
            throw new IllegalArgumentException("only handles intent ACTION_SIM_STATE_CHANGED");
        }

        public String toString() {
            return "SimData{state=" + this.simState + ",slotId=" + this.slotId + ",subId=" + this.subId + "}";
        }
    }

    public class StrongAuthTracker extends com.android.internal.widget.LockPatternUtils.StrongAuthTracker {
        public StrongAuthTracker(Context context) {
            super(context);
        }

        public boolean isUnlockingWithFingerprintAllowed() {
            int userId = KeyguardUpdateMonitor.getCurrentUser();
            boolean ret = isFingerprintAllowedForUser(userId);
            if (!ret) {
                HwLog.v("KeyguardUpdateMonitor", "UnlockingWithFingerprint Not Allowed: " + userId + " for reason flag : " + getStrongAuthForUser(userId));
            }
            return ret;
        }

        public boolean hasUserAuthenticatedSinceBoot() {
            return hasUserAuthenticatedSinceBoot(KeyguardUpdateMonitor.getCurrentUser());
        }

        public boolean hasUserAuthenticatedSinceBoot(int userId) {
            return (getStrongAuthForUser(userId) & 1) == 0;
        }

        public void onStrongAuthRequiredChanged(int userId) {
            KeyguardUpdateMonitor.this.notifyStrongAuthStateChanged(userId);
        }
    }

    protected abstract void afterUserAuthed();

    public abstract boolean canUseFingerWhenOcclued();

    public abstract void checkSecurityMode(SecurityMode securityMode);

    public abstract void dispatchShutDown();

    protected abstract void handleExtMessage(Message message);

    public abstract boolean isChangeTipsForAbnormalReboot();

    public abstract boolean isInDreamingState();

    public abstract boolean isOccluded();

    protected abstract boolean isScreenshotProcess();

    public abstract boolean isShowing();

    protected abstract void onUserAuthTimeout(int i);

    public abstract void resetCancelSubscriptId();

    public abstract void setCancelSubscriptId(int i);

    public abstract void setCustLanguageHw(State state, Context context);

    public abstract void setDreamingState(boolean z);

    public abstract void setFpAuthenticated(boolean z);

    public abstract void setKeyguardViewState(boolean z, boolean z2);

    public static void setCurrentUser(int currentUser) {
        OsUtils.setCurrentUser(currentUser);
    }

    public static int getCurrentUser() {
        return OsUtils.getCurrentUser();
    }

    public void onTrustChanged(boolean enabled, int userId, int flags) {
        HwLog.i("KeyguardUpdateMonitor", "KGSvcCall TrustManager TrustChanged " + userId + (enabled ? " enabled" : BuildConfig.FLAVOR));
        this.mUserHasTrust.put(userId, enabled);
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onTrustChanged(userId);
                if (enabled && flags != 0) {
                    cb.onTrustGrantedWithFlags(flags, userId);
                }
            }
        }
    }

    protected void handleSimSubscriptionInfoChanged() {
        int i;
        HwLog.v("KeyguardUpdateMonitor", "onSubscriptionInfoChanged()");
        List<SubscriptionInfo> sil = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        if (sil != null) {
            for (SubscriptionInfo subInfo : sil) {
                HwLog.v("KeyguardUpdateMonitor", "SubInfo:" + subInfo);
            }
        } else {
            HwLog.v("KeyguardUpdateMonitor", "onSubscriptionInfoChanged: list is null");
        }
        List<SubscriptionInfo> subscriptionInfos = getSubscriptionInfo(true);
        ArrayList<SubscriptionInfo> changedSubscriptions = new ArrayList();
        for (i = 0; i < subscriptionInfos.size(); i++) {
            SubscriptionInfo info = (SubscriptionInfo) subscriptionInfos.get(i);
            if (refreshSimState(info.getSubscriptionId(), info.getSimSlotIndex())) {
                changedSubscriptions.add(info);
            }
        }
        for (i = 0; i < changedSubscriptions.size(); i++) {
            int j;
            SimData data = (SimData) this.mSimDatas.get(Integer.valueOf(((SubscriptionInfo) changedSubscriptions.get(i)).getSubscriptionId()));
            for (j = 0; j < this.mCallbacks.size(); j++) {
                KeyguardUpdateMonitorCallback cb = (KeyguardUpdateMonitorCallback) ((WeakReference) this.mCallbacks.get(j)).get();
                if (cb != null) {
                    cb.onSimStateChanged(data.subId, data.slotId, data.simState);
                }
            }
        }
        for (j = 0; j < this.mCallbacks.size(); j++) {
            cb = (KeyguardUpdateMonitorCallback) ((WeakReference) this.mCallbacks.get(j)).get();
            if (cb != null) {
                cb.onRefreshCarrierInfo();
            }
        }
    }

    private void handleAirplaneModeChanged() {
        for (int j = 0; j < this.mCallbacks.size(); j++) {
            KeyguardUpdateMonitorCallback cb = (KeyguardUpdateMonitorCallback) ((WeakReference) this.mCallbacks.get(j)).get();
            if (cb != null) {
                cb.onRefreshCarrierInfo();
            }
        }
    }

    List<SubscriptionInfo> getSubscriptionInfo(boolean forceReload) {
        List<SubscriptionInfo> sil = this.mSubscriptionInfo;
        if (sil == null || forceReload) {
            sil = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        }
        if (sil == null) {
            this.mSubscriptionInfo = new ArrayList();
        } else {
            this.mSubscriptionInfo = sil;
        }
        return this.mSubscriptionInfo;
    }

    public void onTrustManagedChanged(boolean managed, int userId) {
        this.mUserTrustIsManaged.put(userId, managed);
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onTrustManagedChanged(userId);
            }
        }
    }

    public void onFingerprintAuthenticated(int userId, int fingerId) {
        boolean z = false;
        if (isUnlockingWithFingerprintAllowed() && RetryPolicy.getPswdRetryPolicy(this.mContext, userId).getRemainingChance() > 0) {
            z = HiddenSpace.isFingerPrintAllowForHiddenSpace(this.mContext, userId);
        }
        this.mFingerprintAlreadyAuthenticated = z;
        if (this.mFingerprintAlreadyAuthenticated) {
            this.mUserFingerprintAuthenticated.put(userId, true);
        } else {
            HwLog.i("KeyguardUpdateMonitor", "FP-Auth not Allowed");
        }
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onFingerprintAuthenticated(userId, fingerId);
            }
        }
    }

    private void handleFingerprintAuthFailed() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onFingerprintAuthFailed();
            }
        }
        handleFingerprintHelp(-1, this.mContext.getString(R$string.fingerprint_not_recognized));
    }

    private void handleFingerprintAcquired(int acquireInfo) {
        if (acquireInfo == 0 || ((long) acquireInfo) == 2002 || ((long) acquireInfo) == 2001) {
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
                if (cb != null) {
                    cb.onFingerprintAcquired(acquireInfo);
                }
            }
        }
    }

    private void handleFingerprintAuthenticated(int uid, int fingerId, int authUserId) {
        try {
            int userId = getCurrentUser();
            if (userId != authUserId) {
                HwLog.d("KeyguardUpdateMonitor", "Fingerprint authenticated for wrong user: " + authUserId);
            } else if (this.mStartFpUserId != userId || isSimPinSecure()) {
                HwLog.d("KeyguardUpdateMonitor", "Fingerprint disabled by DPM for userId: " + userId);
                setFingerprintRunningState(0);
            } else {
                onFingerprintAuthenticated(uid, fingerId);
                setFingerprintRunningState(0);
            }
        } finally {
            setFingerprintRunningState(0);
        }
    }

    private void handleFingerprintHelp(int msgId, String helpString) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onFingerprintHelp(msgId, helpString);
            }
        }
    }

    private void handleFingerprintError(int msgId, String errString) {
        if (msgId == 5 && this.mFingerprintRunningState == 3) {
            setFingerprintRunningState(0);
            startListeningForFingerprint();
        } else {
            setFingerprintRunningState(10);
        }
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onFingerprintError(msgId, errString);
            }
        }
    }

    private void handleFingerprintLockoutReset() {
        updateFingerprintListeningState();
    }

    protected void setFingerprintRunningState(int fingerprintRunningState) {
        boolean wasRunning = this.mFingerprintRunningState == 1;
        boolean isRunning = fingerprintRunningState == 1;
        if (this.mFingerprintRunningState != fingerprintRunningState) {
            HwLog.w("KeyguardUpdateMonitor", "Fingerprint RunningStateChanged. " + fingerprintRunningState);
        }
        this.mFingerprintRunningState = fingerprintRunningState;
        if (wasRunning != isRunning) {
            notifyFingerprintRunningStateChanged();
        }
    }

    private void notifyFingerprintRunningStateChanged() {
        boolean running = isFingerprintDetectionRunning();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onFingerprintRunningStateChanged(running);
            }
        }
    }

    private void handleFaceUnlockStateChanged(boolean running, int userId) {
        this.mUserFaceUnlockRunning.put(userId, running);
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onFaceUnlockStateChanged(running, userId);
            }
        }
    }

    public boolean isFaceUnlockRunning(int userId) {
        return this.mUserFaceUnlockRunning.get(userId);
    }

    public boolean isFingerprintDetectionRunning() {
        boolean z = true;
        if (this.mFingerprintRunningState == 10) {
            HwLog.w("KeyguardUpdateMonitor", "Fingerprint Detection Running with error report");
            return true;
        }
        if (this.mFingerprintRunningState != 1) {
            z = false;
        }
        return z;
    }

    private boolean isTrustDisabled(int userId) {
        return isSimPinSecure();
    }

    protected boolean isFingerprintDisabled(int userId) {
        return !DisabledFeatureUtils.getFingerprintDisabled(this.mContext, userId) ? isSimPinSecure() : true;
    }

    public boolean getUserCanSkipBouncer(int userId) {
        if (getUserHasTrust(userId)) {
            HwLog.w("KeyguardUpdateMonitor", "Skip bouncer as Trusted");
            return true;
        }
        if (this.mUserFingerprintAuthenticated.get(userId)) {
            if (isUnlockingWithFingerprintAllowed()) {
                HwLog.w("KeyguardUpdateMonitor", "Skip bouncer as Finger");
                return true;
            }
            HwLog.w("KeyguardUpdateMonitor", "Skip bouncer cancel as Finger not allowed now");
        }
        return false;
    }

    public boolean getUserHasTrust(int userId) {
        return !isTrustDisabled(userId) ? this.mUserHasTrust.get(userId) : false;
    }

    public boolean getUserTrustIsManaged(int userId) {
        return this.mUserTrustIsManaged.get(userId) && !isTrustDisabled(userId);
    }

    public boolean isUnlockingWithFingerprintAllowed() {
        if (!this.mStrongAuthTracker.isUnlockingWithFingerprintAllowed() || hasFingerprintUnlockTimedOut(OsUtils.getCurrentUser())) {
            return false;
        }
        return true;
    }

    public StrongAuthTracker getStrongAuthTracker() {
        return this.mStrongAuthTracker;
    }

    public boolean hasFingerprintUnlockTimedOut(int userId) {
        boolean z;
        boolean auth = this.mStrongAuthNotTimedOut.contains(Integer.valueOf(userId));
        String str = "KeyguardUpdateMonitor";
        StringBuilder append = new StringBuilder().append("hasFingerprintUnlockTimedOut = ");
        if (auth) {
            z = false;
        } else {
            z = true;
        }
        HwLog.i(str, append.append(z).toString());
        if (auth) {
            return false;
        }
        return true;
    }

    public void reportSuccessfulStrongAuthUnlockAttempt() {
        this.mStrongAuthNotTimedOut.add(Integer.valueOf(OsUtils.getCurrentUser()));
        GlobalContext.getBackgroundHandler().post(this.mSuccessfulStrongAuthUnlockReporter);
    }

    protected void scheduleStrongAuthTimeout() {
        int currentUser = OsUtils.getCurrentUser();
        long when = SystemClock.elapsedRealtime() + 259200000;
        Intent intent = new Intent("com.android.systemui.ACTION_STRONG_AUTH_TIMEOUT");
        intent.putExtra("com.android.systemui.USER_ID", currentUser);
        this.mAlarmManager.set(3, when, PendingIntent.getBroadcast(this.mContext, currentUser, intent, 268435456));
        notifyStrongAuthStateChanged(currentUser);
    }

    private void notifyStrongAuthStateChanged(int userId) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onStrongAuthStateChanged(userId);
            }
        }
    }

    public static KeyguardUpdateMonitor getInstance(Context context) {
        return HwKeyguardUpdateMonitor.getInstance(context);
    }

    protected void handleStartedWakingUp() {
        updateFingerprintListeningState();
        int count = this.mCallbacks.size();
        for (int i = 0; i < count; i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onStartedWakingUp();
            }
        }
        if (RemoteLockUtils.isDeviceRemoteLocked(this.mContext)) {
            AppHandler.sendSingleMessage(101, 0);
        }
    }

    protected void handleStartedGoingToSleep(int arg1) {
        clearFingerprintRecognized();
        int count = this.mCallbacks.size();
        for (int i = 0; i < count; i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onStartedGoingToSleep(arg1);
            }
        }
        this.mGoingToSleep = true;
        this.mFingerprintAlreadyAuthenticated = false;
        updateFingerprintListeningState();
    }

    protected void handleFinishedGoingToSleep(int arg1) {
        this.mGoingToSleep = false;
        int count = this.mCallbacks.size();
        for (int i = 0; i < count; i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onFinishedGoingToSleep(arg1);
            }
        }
        this.mFingerprintAlreadyAuthenticated = false;
        updateFingerprintListeningState();
    }

    protected void handleScreenTurnedOn() {
        int count = this.mCallbacks.size();
        for (int i = 0; i < count; i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onScreenTurnedOn();
            }
        }
    }

    private void handleScreenTurnedOff() {
        int count = this.mCallbacks.size();
        for (int i = 0; i < count; i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onScreenTurnedOff();
            }
        }
    }

    public void dispatchSetBackground(Bitmap bmp) {
        HwLog.d("KeyguardUpdateMonitor", "dispatchSetBackground");
        int count = this.mCallbacks.size();
        for (int i = 0; i < count; i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onSetBackground(bmp);
            }
        }
    }

    private void handleUserInfoChanged(int userId) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onUserInfoChanged(userId);
            }
        }
    }

    protected KeyguardUpdateMonitor(Context context) {
        this.mContext = context;
        this.mSubscriptionManager = SubscriptionManager.from(context);
        this.mAlarmManager = (AlarmManager) context.getSystemService(AlarmManager.class);
        this.mDeviceProvisioned = isDeviceProvisionedInSettingsDb();
        this.mStrongAuthTracker = new StrongAuthTracker(context);
        if (!this.mDeviceProvisioned) {
            watchForDeviceProvisioning();
        }
        this.mBatteryStatus = new BatteryStatus(1, 100, 0, 0, 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.TIME_TICK");
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        filter.addAction("android.intent.action.AIRPLANE_MODE");
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        filter.addAction("android.intent.action.SERVICE_STATE");
        filter.addAction("android.intent.action.PHONE_STATE");
        filter.addAction("android.media.RINGER_MODE_CHANGED");
        if (KeyguardCfg.isMagazineUpdateEnabled()) {
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        }
        context.registerReceiver(this.mBroadcastReceiver, filter);
        IntentFilter bootCompleteFilter = new IntentFilter();
        bootCompleteFilter.setPriority(1000);
        bootCompleteFilter.addAction("android.intent.action.BOOT_COMPLETED");
        bootCompleteFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        context.registerReceiver(this.mBroadcastReceiver, bootCompleteFilter);
        IntentFilter allUserFilter = new IntentFilter();
        allUserFilter.addAction("android.intent.action.USER_INFO_CHANGED");
        allUserFilter.addAction("android.app.action.NEXT_ALARM_CLOCK_CHANGED");
        allUserFilter.addAction("com.android.facelock.FACE_UNLOCK_STARTED");
        allUserFilter.addAction("com.android.facelock.FACE_UNLOCK_STOPPED");
        allUserFilter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        try {
            context.registerReceiverAsUser(this.mBroadcastAllReceiver, UserHandle.ALL, allUserFilter, null, null);
        } catch (SecurityException e) {
            HwLog.w("KeyguardUpdateMonitor", "registerReceiverAsUser fail ", e);
        }
        if (!this.mRemoveSimInfoListen) {
            this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mSubscriptionListener);
        }
        try {
            ActivityManagerNative.getDefault().registerUserSwitchObserver(new Stub() {
                public void onUserSwitching(int newUserId, IRemoteCallback reply) {
                    KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(310, newUserId, 0, reply));
                }

                public void onUserSwitchComplete(int newUserId) throws RemoteException {
                    KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(314, newUserId, 0));
                }

                public void onForegroundProfileSwitch(int newProfileId) {
                }
            });
        } catch (RemoteException e2) {
            e2.printStackTrace();
        } catch (SecurityException e3) {
            HwLog.w("KeyguardUpdateMonitor", "registerReceiverAsUser fail ", e3);
        }
        IntentFilter strongAuthTimeoutFilter = new IntentFilter();
        strongAuthTimeoutFilter.addAction("com.android.systemui.ACTION_STRONG_AUTH_TIMEOUT");
        context.registerReceiver(this.mStrongAuthTimeoutReceiver, strongAuthTimeoutFilter, "com.android.systemui.permission.SELF", null);
        this.mTrustManager = (TrustManager) context.getSystemService("trust");
        this.mTrustManager.registerTrustListener(this);
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mLockPatternUtils.registerStrongAuthTracker(this.mStrongAuthTracker);
        this.mFpm = (FingerprintManager) context.getSystemService("fingerprint");
        updateFingerprintListeningState();
        if (this.mFpm != null) {
            this.mFpm.addLockoutResetCallback(this.mLockoutResetCallback);
        }
    }

    public void updateFingerprintListeningState() {
        if (isScreenshotProcess()) {
            HwLog.w("KeyguardUpdateMonitor", "updateFingerprintListeningState do nothing for screenshot process");
        } else if (UserHandle.myUserId() != 0) {
            HwLog.w("KeyguardUpdateMonitor", "updateFingerprintListeningState do nothing for non primary user process");
        } else {
            boolean shouldListenForFingerprint = shouldListenForFingerprint();
            if (this.mFingerprintRunningState == 1 && !shouldListenForFingerprint) {
                stopListeningForFingerprint();
            } else if (this.mFingerprintRunningState == 1 || !shouldListenForFingerprint) {
                HwLog.i("KeyguardUpdateMonitor", "Ignore update FP: " + this.mFingerprintRunningState + " " + shouldListenForFingerprint);
            } else {
                startListeningForFingerprint();
            }
        }
    }

    private boolean shouldListenForFingerprint() {
        boolean z = false;
        if (RemoteLockUtils.isDeviceRemoteLocked(this.mContext)) {
            HwLog.i("KeyguardUpdateMonitor", "startListeningForFingerprint() Forbidden fingerprint unlock for remote lock");
            return false;
        }
        HwLog.v("KeyguardUpdateMonitor", "FingerStat showing: " + isShowing() + "; Interactive: " + this.mDeviceInteractive + "; Switching: " + this.mSwitchingUser + "; Bouncer:" + this.mBouncer + "; GoingToSleep: " + this.mGoingToSleep + " Occluded: " + isOccluded() + "; dreaming: " + isInDreamingState() + "; Authenticated: " + this.mFingerprintAlreadyAuthenticated);
        if (!((!isShowing() && this.mDeviceInteractive && !this.mBouncer && !this.mGoingToSleep && ((!isInDreamingState() || !isShowing()) && !canUseFingerWhenOcclued())) || this.mSwitchingUser || this.mFingerprintAlreadyAuthenticated || isFingerprintDisabled(getCurrentUser()))) {
            z = true;
        }
        return z;
    }

    public void startListeningForFingerprint() {
        if (this.mFingerprintRunningState == 2) {
            setFingerprintRunningState(3);
            return;
        }
        this.mStartFpUserId = getCurrentUser();
        if (isUnlockWithFingerprintPossible(this.mStartFpUserId)) {
            HwLog.v("KeyguardUpdateMonitor", "KGSvcFp startListeningForFingerprint()");
            if (this.mFingerprintCancelSignal != null) {
                this.mFingerprintCancelSignal.cancel();
            }
            this.mFingerprintCancelSignal = new CancellationSignal();
            this.mFingerprintCancelSignal.setOnCancelListener(this);
            int fPmode = 100;
            if (!SystemProperties.getBoolean("ro.config.hw_privacySpace", true) || KeyguardTheme.getInst().getLockStyle() == 5 || !HiddenSpace.isHiddenSpaceOrOwnerSwitchOnFpUnlock(this.mContext, this.mStartFpUserId)) {
                fPmode = 0;
            }
            HwLog.i("KeyguardUpdateMonitor", "authenticate by " + fPmode + " mode!");
            this.mFpm.authenticate(null, this.mFingerprintCancelSignal, fPmode, this.mAuthenticationCallback, null, this.mStartFpUserId);
            setFingerprintRunningState(1);
        } else {
            HwLog.w("KeyguardUpdateMonitor", "KGSvcFp skip startListeningForFingerprint()");
        }
    }

    public boolean isUnlockWithFingerprintPossible(int userId) {
        if (this.mFpm == null || !this.mFpm.isHardwareDetected() || isFingerprintDisabled(userId) || this.mFpm.getEnrolledFingerprints(userId).size() <= 0) {
            return false;
        }
        return true;
    }

    public void stopListeningForFingerprint() {
        HwLog.v("KeyguardUpdateMonitor", "KGSvcFp stopListeningForFingerprint(). " + this.mFingerprintRunningState);
        if (this.mFingerprintRunningState == 1) {
            this.mFingerprintCancelSignal.cancel();
            this.mFingerprintCancelSignal = null;
            setFingerprintRunningState(2);
        }
        if (this.mFingerprintRunningState == 3) {
            setFingerprintRunningState(2);
        }
    }

    private boolean isDeviceProvisionedInSettingsDb() {
        return Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0;
    }

    private void watchForDeviceProvisioning() {
        this.mDeviceProvisionedObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                KeyguardUpdateMonitor.this.mDeviceProvisioned = KeyguardUpdateMonitor.this.isDeviceProvisionedInSettingsDb();
                if (KeyguardUpdateMonitor.this.mDeviceProvisioned) {
                    KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(308);
                }
                HwLog.d("KeyguardUpdateMonitor", "DEVICE_PROVISIONED state = " + KeyguardUpdateMonitor.this.mDeviceProvisioned);
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("device_provisioned"), false, this.mDeviceProvisionedObserver);
        boolean provisioned = isDeviceProvisionedInSettingsDb();
        if (provisioned != this.mDeviceProvisioned) {
            this.mDeviceProvisioned = provisioned;
            if (this.mDeviceProvisioned) {
                this.mHandler.sendEmptyMessage(308);
            }
        }
    }

    protected void handleDevicePolicyManagerStateChanged() {
        updateFingerprintListeningState();
        for (int i = this.mCallbacks.size() - 1; i >= 0; i--) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onDevicePolicyManagerStateChanged();
            }
        }
    }

    protected void handleUserSwitching(int userId, IRemoteCallback reply) {
        HwLog.w("KeyguardUpdateMonitor", "UserSwitching " + userId);
        this.mSwitchingUser = true;
        updateFingerprintListeningState();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onUserSwitching(userId);
            }
        }
        try {
            reply.sendResult(null);
        } catch (RemoteException e) {
        }
    }

    protected void handleUserSwitchComplete(int userId) {
        HwLog.w("KeyguardUpdateMonitor", "UserSwitchComplete " + userId);
        this.mSwitchingUser = false;
        updateFingerprintListeningState();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onUserSwitchComplete(userId);
            }
        }
        setCurrentUser(userId);
    }

    public void dispatchBootCompleted() {
        this.mHandler.sendEmptyMessage(313);
    }

    protected void handleBootCompleted() {
        if (!this.mBootCompleted) {
            this.mBootCompleted = true;
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
                if (cb != null) {
                    cb.onBootCompleted();
                }
            }
        }
    }

    public boolean hasBootCompleted() {
        return this.mBootCompleted;
    }

    protected void handleDeviceProvisioned() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onDeviceProvisioned();
            }
        }
        if (this.mDeviceProvisionedObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mDeviceProvisionedObserver);
            this.mDeviceProvisionedObserver = null;
        }
    }

    protected void handlePhoneStateChanged(String newState) {
        HwLog.d("KeyguardUpdateMonitor", "handlePhoneStateChanged(" + newState + ")");
        if (TelephonyManager.EXTRA_STATE_IDLE.equals(newState)) {
            this.mPhoneState = 0;
        } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(newState)) {
            this.mPhoneState = 2;
        } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(newState)) {
            this.mPhoneState = 1;
        }
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onPhoneStateChanged(this.mPhoneState);
            }
        }
    }

    protected void handleRingerModeChange(int mode) {
        HwLog.d("KeyguardUpdateMonitor", "handleRingerModeChange(" + mode + ")");
        this.mRingMode = mode;
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onRingerModeChanged(mode);
            }
        }
    }

    private void handleTimeUpdate() {
        HwLog.d("KeyguardUpdateMonitor", "handleTimeUpdate");
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onTimeChanged();
            }
        }
    }

    protected void handleBatteryUpdate(BatteryStatus status) {
        HwLog.d("KeyguardUpdateMonitor", "handleBatteryUpdate");
        boolean batteryUpdateInteresting = isBatteryUpdateInteresting(this.mBatteryStatus, status);
        this.mBatteryStatus = status;
        if (batteryUpdateInteresting) {
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
                if (cb != null) {
                    cb.onRefreshBatteryInfo(status);
                }
            }
        }
    }

    private void handleSimStateChange(int subId, int slotId, State state) {
        HwLog.d("KeyguardUpdateMonitor", "handleSimStateChange(subId=" + subId + ", slotId=" + slotId + ", state=" + state + ")");
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            boolean changed;
            SimData data = (SimData) this.mSimDatas.get(Integer.valueOf(subId));
            if (data == null) {
                this.mSimDatas.put(Integer.valueOf(subId), new SimData(state, slotId, subId));
                changed = true;
            } else {
                changed = (data.simState == state && data.subId == subId && data.slotId == slotId) ? false : true;
                data.simState = state;
                data.subId = subId;
                data.slotId = slotId;
            }
            if (changed && state != State.UNKNOWN) {
                for (int i = 0; i < this.mCallbacks.size(); i++) {
                    KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
                    if (cb != null) {
                        cb.onSimStateChanged(subId, slotId, state);
                    }
                }
                setCustLanguageHw(state, this.mContext);
            }
            return;
        }
        HwLog.w("KeyguardUpdateMonitor", "invalid subId in handleSimStateChange()");
    }

    private void handleServiceStateChange(int subId, ServiceState serviceState) {
        HwLog.d("KeyguardUpdateMonitor", "handleServiceStateChange(subId=" + subId + ", serviceState=" + serviceState);
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            this.mServiceStates.put(Integer.valueOf(subId), serviceState);
            for (int j = 0; j < this.mCallbacks.size(); j++) {
                KeyguardUpdateMonitorCallback cb = (KeyguardUpdateMonitorCallback) ((WeakReference) this.mCallbacks.get(j)).get();
                if (cb != null) {
                    cb.onRefreshCarrierInfo();
                }
            }
            AppHandler.sendMessage(117);
            return;
        }
        HwLog.w("KeyguardUpdateMonitor", "invalid subId in handleServiceStateChange()");
    }

    public boolean isSupportEmergencyCall() {
        Set<Entry<Integer, ServiceState>> entrys = this.mServiceStates.entrySet();
        if (entrys != null) {
            for (Entry<Integer, ServiceState> entry : entrys) {
                ServiceState state = (ServiceState) entry.getValue();
                if (!hasService(state)) {
                    if (isEmergencyOnly(state)) {
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean hasService(ServiceState state) {
        if (state == null) {
            return false;
        }
        switch (state.getState()) {
            case 1:
            case 3:
                return false;
            default:
                return true;
        }
    }

    private boolean isEmergencyOnly(ServiceState state) {
        return state != null ? state.isEmergencyOnly() : false;
    }

    public void onKeyguardVisibilityChanged(boolean showing) {
        HwLog.d("KeyguardUpdateMonitor", "onKeyguardVisibilityChanged(" + showing + ")");
        this.mKeyguardIsVisible = showing;
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onKeyguardVisibilityChangedRaw(showing);
            }
        }
        updateFingerprintListeningState();
    }

    protected void handleKeyguardReset() {
        updateFingerprintListeningState();
    }

    protected void handleKeyguardBouncerChanged(int bouncer) {
        boolean isBouncer = true;
        HwLog.d("KeyguardUpdateMonitor", "handleKeyguardBouncerChanged(" + bouncer + ")");
        if (bouncer != 1) {
            isBouncer = false;
        }
        this.mBouncer = isBouncer;
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onKeyguardBouncerChanged(isBouncer);
            }
        }
        updateFingerprintListeningState();
    }

    private void handleReportEmergencyCallAction() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback cb = this.mCallbacks.getCallBack(i);
            if (cb != null) {
                cb.onEmergencyCallAction();
            }
        }
    }

    private static boolean isBatteryUpdateInteresting(BatteryStatus old, BatteryStatus current) {
        boolean nowPluggedIn = current.isPluggedIn();
        boolean wasPluggedIn = old.isPluggedIn();
        boolean stateChangedWhilePluggedIn = (wasPluggedIn && nowPluggedIn) ? old.status != current.status : false;
        if (wasPluggedIn != nowPluggedIn || stateChangedWhilePluggedIn) {
            return true;
        }
        if (nowPluggedIn && old.level != current.level) {
            return true;
        }
        if (!nowPluggedIn && current.isBatteryLow() && current.level != old.level) {
            return true;
        }
        if (!nowPluggedIn || current.maxChargingWattage == old.maxChargingWattage) {
            return false;
        }
        return true;
    }

    public void removeCallback(KeyguardUpdateMonitorCallback callback) {
        HwLog.v("KeyguardUpdateMonitor", "*** unregister callback for " + callback);
        this.mCallbacks.removeCallback(callback);
    }

    public void registerCallback(KeyguardUpdateMonitorCallback callback) {
        HwLog.v("KeyguardUpdateMonitor", "*** register callback for " + callback);
        this.mCallbacks.regist(callback);
        removeCallback(null);
        sendUpdates(callback);
    }

    private void sendUpdates(KeyguardUpdateMonitorCallback callback) {
        callback.onRefreshBatteryInfo(this.mBatteryStatus);
        callback.onTimeChanged();
        callback.onRingerModeChanged(this.mRingMode);
        callback.onPhoneStateChanged(this.mPhoneState);
        callback.onRefreshCarrierInfo();
        callback.onClockVisibilityChanged();
        for (Entry<Integer, SimData> data : this.mSimDatas.entrySet()) {
            SimData state = (SimData) data.getValue();
            callback.onSimStateChanged(state.subId, state.slotId, state.simState);
        }
    }

    public void sendKeyguardReset() {
        this.mHandler.obtainMessage(312).sendToTarget();
    }

    public void sendKeyguardBouncerChanged(boolean showingBouncer) {
        HwLog.d("KeyguardUpdateMonitor", "sendKeyguardBouncerChanged(" + showingBouncer + ")");
        Message message = this.mHandler.obtainMessage(322);
        message.arg1 = showingBouncer ? 1 : 0;
        message.sendToTarget();
    }

    public void reportSimUnlocked(int subId) {
        HwLog.v("KeyguardUpdateMonitor", "reportSimUnlocked(subId=" + subId + ")");
        handleSimStateChange(subId, SubscriptionManager.getSlotId(subId), State.READY);
    }

    public void reportEmergencyCallAction(boolean bypassHandler) {
        if (bypassHandler) {
            handleReportEmergencyCallAction();
        } else {
            this.mHandler.obtainMessage(318).sendToTarget();
        }
    }

    public boolean isDeviceProvisioned() {
        return this.mDeviceProvisioned;
    }

    public void clearFailedUnlockAttempts() {
        this.mFailedAttempts.delete(OsUtils.getCurrentUser());
    }

    public int getFailedUnlockAttempts(int userId) {
        return this.mFailedAttempts.get(userId, 0);
    }

    public void reportFailedStrongAuthUnlockAttempt(int userId) {
        this.mFailedAttempts.put(userId, getFailedUnlockAttempts(userId) + 1);
    }

    public void clearFingerprintRecognized() {
        this.mUserFingerprintAuthenticated.clear();
    }

    public boolean isSimPinVoiceSecure() {
        return isSimPinSecure();
    }

    public boolean isSimPinSecure() {
        for (SubscriptionInfo info : getSubscriptionInfo(false)) {
            if (isSimPinSecure(getSimState(info.getSubscriptionId()))) {
                return true;
            }
        }
        return false;
    }

    public State getSimState(int subId) {
        if (this.mSimDatas.containsKey(Integer.valueOf(subId))) {
            return ((SimData) this.mSimDatas.get(Integer.valueOf(subId))).simState;
        }
        return State.UNKNOWN;
    }

    private boolean refreshSimState(int subId, int slotId) {
        State state;
        boolean changed;
        int simState = TelephonyManager.from(this.mContext).getSimState(slotId);
        try {
            state = State.intToState(simState);
        } catch (IllegalArgumentException e) {
            HwLog.w("KeyguardUpdateMonitor", "Unknown sim state: " + simState);
            state = State.UNKNOWN;
        }
        SimData data = (SimData) this.mSimDatas.get(Integer.valueOf(subId));
        if (data == null) {
            this.mSimDatas.put(Integer.valueOf(subId), new SimData(state, slotId, subId));
            changed = true;
        } else if (this.mCancelSubscriptId == subId && this.mCancelSubscriptSoltId == slotId) {
            changed = false;
        } else {
            changed = data.simState != state;
            data.simState = state;
        }
        if (changed) {
            setCustLanguageHw(state, this.mContext);
        }
        return changed;
    }

    public static boolean isSimPinSecure(State state) {
        State simState = state;
        if (state == State.PIN_REQUIRED || state == State.PUK_REQUIRED || state == State.PERM_DISABLED) {
            return true;
        }
        return false;
    }

    public void dispatchStartedWakingUp() {
        synchronized (this) {
            this.mDeviceInteractive = true;
        }
        this.mHandler.removeMessages(319);
        this.mHandler.sendEmptyMessage(319);
        AppHandler.sendMessage(14);
    }

    public void dispatchStartedGoingToSleep(int why) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(321, why, 0));
    }

    public void dispatchFinishedGoingToSleep(int why) {
        synchronized (this) {
            this.mDeviceInteractive = false;
        }
        this.mHandler.removeMessages(320);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(320, why, 0));
        AppHandler.sendMessage(15);
    }

    public void dispatchScreenTurnedOn() {
        synchronized (this) {
            this.mScreenOn = true;
        }
        this.mHandler.sendEmptyMessage(331);
    }

    public void dispatchScreenTurnedOff() {
        synchronized (this) {
            this.mScreenOn = false;
        }
        this.mHandler.sendEmptyMessage(332);
        AppHandler.sendMessage(13);
    }

    public boolean isDeviceInteractive() {
        return this.mDeviceInteractive;
    }

    public boolean isGoingToSleep() {
        return this.mGoingToSleep;
    }

    public int getNextSubIdForState(State state) {
        List<SubscriptionInfo> list = getSubscriptionInfo(false);
        int resultId = -1;
        int bestSlotId = Integer.MAX_VALUE;
        for (int i = 0; i < list.size(); i++) {
            int id = ((SubscriptionInfo) list.get(i)).getSubscriptionId();
            int slotId = SubscriptionManager.getSlotId(id);
            if (state == getSimState(id) && bestSlotId > slotId) {
                resultId = id;
                bestSlotId = slotId;
            }
        }
        return resultId;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("KeyguardUpdateMonitor state:");
        pw.println("  SIM States:");
        for (SimData data : this.mSimDatas.values()) {
            pw.println("    " + data.toString());
        }
        pw.println("  Subs:");
        if (this.mSubscriptionInfo != null) {
            for (int i = 0; i < this.mSubscriptionInfo.size(); i++) {
                pw.println("    " + this.mSubscriptionInfo.get(i));
            }
        }
        pw.println("  Service states:");
        for (Integer intValue : this.mServiceStates.keySet()) {
            int subId = intValue.intValue();
            pw.println("    " + subId + "=" + this.mServiceStates.get(Integer.valueOf(subId)));
        }
        if (this.mFpm != null && this.mFpm.isHardwareDetected()) {
            int userId = getCurrentUser();
            int strongAuthFlags = this.mStrongAuthTracker.getStrongAuthForUser(userId);
            pw.println("  Fingerprint state (user=" + userId + ")");
            pw.println("    allowed=" + isUnlockingWithFingerprintAllowed());
            pw.println("    auth'd=" + this.mUserFingerprintAuthenticated.get(userId));
            pw.println("    authSinceBoot=" + getStrongAuthTracker().hasUserAuthenticatedSinceBoot());
            pw.println("    disabled(DPM)=" + isFingerprintDisabled(userId));
            pw.println("    possible=" + isUnlockWithFingerprintPossible(userId));
            pw.println("    strongAuthFlags=" + Integer.toHexString(strongAuthFlags));
            pw.println("    timedout=" + hasFingerprintUnlockTimedOut(userId));
            pw.println("    trustManaged=" + getUserTrustIsManaged(userId));
        }
    }
}
