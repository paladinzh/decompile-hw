package com.android.server.net;

import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.INotificationManager;
import android.app.IUidObserver;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.app.usage.UsageStatsManagerInternal;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.net.INetworkManagementEventObserver;
import android.net.INetworkPolicyListener;
import android.net.INetworkPolicyManager.Stub;
import android.net.INetworkStatsService;
import android.net.LinkProperties;
import android.net.NetworkIdentity;
import android.net.NetworkInfo;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.NetworkQuotaInfo;
import android.net.NetworkState;
import android.net.NetworkTemplate;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.IDeviceIdleController;
import android.os.INetworkManagementService;
import android.os.IPowerManager;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.os.PowerManagerInternal;
import android.os.PowerManagerInternal.LowPowerModeListener;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.text.format.Time;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.DebugUtils;
import android.util.Log;
import android.util.NtpTrustedTime;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.TrustedTime;
import android.util.Xml;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.server.DeviceIdleController.LocalService;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.NetworkManagementService;
import com.android.server.SystemConfig;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.job.controllers.JobStatus;
import com.google.android.collect.Lists;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class NetworkPolicyManagerService extends Stub {
    private static final String ACTION_ALLOW_BACKGROUND = "com.android.server.net.action.ALLOW_BACKGROUND";
    private static final String ACTION_SNOOZE_WARNING = "com.android.server.net.action.SNOOZE_WARNING";
    private static final String ATTR_APP_ID = "appId";
    private static final String ATTR_CYCLE_DAY = "cycleDay";
    private static final String ATTR_CYCLE_TIMEZONE = "cycleTimezone";
    private static final String ATTR_INFERRED = "inferred";
    private static final String ATTR_LAST_LIMIT_SNOOZE = "lastLimitSnooze";
    private static final String ATTR_LAST_SNOOZE = "lastSnooze";
    private static final String ATTR_LAST_WARNING_SNOOZE = "lastWarningSnooze";
    private static final String ATTR_LIMIT_BYTES = "limitBytes";
    private static final String ATTR_METERED = "metered";
    private static final String ATTR_NETWORK_ID = "networkId";
    private static final String ATTR_NETWORK_TEMPLATE = "networkTemplate";
    private static final String ATTR_POLICY = "policy";
    private static final String ATTR_RESTRICT_BACKGROUND = "restrictBackground";
    private static final String ATTR_SUBSCRIBER_ID = "subscriberId";
    private static final String ATTR_UID = "uid";
    private static final String ATTR_VERSION = "version";
    private static final String ATTR_WARNING_BYTES = "warningBytes";
    private static final boolean GOOGLE_WARNING_DISABLED = true;
    private static final boolean LOGD = true;
    private static final boolean LOGV = false;
    private static final int MSG_ADVISE_PERSIST_THRESHOLD = 7;
    private static final int MSG_LIMIT_REACHED = 5;
    private static final int MSG_METERED_IFACES_CHANGED = 2;
    private static final int MSG_REMOVE_INTERFACE_QUOTA = 11;
    private static final int MSG_RESTRICT_BACKGROUND_BLACKLIST_CHANGED = 12;
    private static final int MSG_RESTRICT_BACKGROUND_CHANGED = 6;
    private static final int MSG_RESTRICT_BACKGROUND_WHITELIST_CHANGED = 9;
    private static final int MSG_RULES_CHANGED = 1;
    private static final int MSG_SCREEN_ON_CHANGED = 8;
    private static final int MSG_UPDATE_INTERFACE_QUOTA = 10;
    static final String TAG = "NetworkPolicy";
    private static final String TAG_APP_POLICY = "app-policy";
    private static final String TAG_NETWORK_POLICY = "network-policy";
    private static final String TAG_POLICY_LIST = "policy-list";
    private static final String TAG_RESTRICT_BACKGROUND = "restrict-background";
    private static final String TAG_REVOKED_RESTRICT_BACKGROUND = "revoked-restrict-background";
    private static final String TAG_UID_POLICY = "uid-policy";
    private static final String TAG_WHITELIST = "whitelist";
    private static final long TIME_CACHE_MAX_AGE = 86400000;
    public static final int TYPE_LIMIT = 2;
    public static final int TYPE_LIMIT_SNOOZED = 3;
    public static final int TYPE_WARNING = 1;
    private static final int VERSION_ADDED_INFERRED = 7;
    private static final int VERSION_ADDED_METERED = 4;
    private static final int VERSION_ADDED_NETWORK_ID = 9;
    private static final int VERSION_ADDED_RESTRICT_BACKGROUND = 3;
    private static final int VERSION_ADDED_SNOOZE = 2;
    private static final int VERSION_ADDED_TIMEZONE = 6;
    private static final int VERSION_INIT = 1;
    private static final int VERSION_LATEST = 10;
    private static final int VERSION_SPLIT_SNOOZE = 5;
    private static final int VERSION_SWITCH_APP_ID = 8;
    private static final int VERSION_SWITCH_UID = 10;
    private final ArraySet<String> mActiveNotifs;
    private final IActivityManager mActivityManager;
    private final INetworkManagementEventObserver mAlertObserver;
    private final BroadcastReceiver mAllowReceiver;
    private final AppOpsManager mAppOps;
    private IConnectivityManager mConnManager;
    private BroadcastReceiver mConnReceiver;
    private INetworkPolicyListener mConnectivityListener;
    private final Context mContext;
    private final SparseBooleanArray mDefaultRestrictBackgroundWhitelistUids;
    private IDeviceIdleController mDeviceIdleController;
    volatile boolean mDeviceIdleMode;
    final SparseBooleanArray mFirewallChainStates;
    final Handler mHandler;
    private Callback mHandlerCallback;
    private final IPackageManager mIPm;
    private final RemoteCallbackList<INetworkPolicyListener> mListeners;
    private ArraySet<String> mMeteredIfaces;
    private final INetworkManagementService mNetworkManager;
    final ArrayMap<NetworkTemplate, NetworkPolicy> mNetworkPolicy;
    final ArrayMap<NetworkPolicy, String[]> mNetworkRules;
    private final INetworkStatsService mNetworkStats;
    private INotificationManager mNotifManager;
    private final ArraySet<NetworkTemplate> mOverLimitNotified;
    private final BroadcastReceiver mPackageReceiver;
    private final AtomicFile mPolicyFile;
    private final IPowerManager mPowerManager;
    private PowerManagerInternal mPowerManagerInternal;
    private final SparseBooleanArray mPowerSaveTempWhitelistAppIds;
    private final SparseBooleanArray mPowerSaveWhitelistAppIds;
    private final SparseBooleanArray mPowerSaveWhitelistExceptIdleAppIds;
    private final BroadcastReceiver mPowerSaveWhitelistReceiver;
    volatile boolean mRestrictBackground;
    private final SparseBooleanArray mRestrictBackgroundWhitelistRevokedUids;
    private final SparseBooleanArray mRestrictBackgroundWhitelistUids;
    volatile boolean mRestrictPower;
    final Object mRulesLock;
    volatile boolean mScreenOn;
    private final BroadcastReceiver mScreenReceiver;
    private final BroadcastReceiver mSnoozeWarningReceiver;
    private final BroadcastReceiver mStatsReceiver;
    private final boolean mSuppressDefaultPolicy;
    volatile boolean mSystemReady;
    private final Runnable mTempPowerSaveChangedCallback;
    private final TrustedTime mTime;
    private long mTimeRefreshRealtime;
    final SparseIntArray mUidFirewallDozableRules;
    final SparseIntArray mUidFirewallPowerSaveRules;
    final SparseIntArray mUidFirewallStandbyRules;
    private final IUidObserver mUidObserver;
    final SparseIntArray mUidPolicy;
    private final BroadcastReceiver mUidRemovedReceiver;
    final SparseIntArray mUidRules;
    final SparseIntArray mUidState;
    private UsageStatsManagerInternal mUsageStats;
    private final UserManager mUserManager;
    private final BroadcastReceiver mUserReceiver;
    private final BroadcastReceiver mWifiConfigReceiver;
    private final BroadcastReceiver mWifiStateReceiver;

    private class AppIdleStateChangeListener extends android.app.usage.UsageStatsManagerInternal.AppIdleStateChangeListener {
        private AppIdleStateChangeListener() {
        }

        public void onAppIdleStateChanged(String packageName, int userId, boolean idle) {
            try {
                int uid = NetworkPolicyManagerService.this.mContext.getPackageManager().getPackageUidAsUser(packageName, DumpState.DUMP_PREFERRED_XML, userId);
                synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                    NetworkPolicyManagerService.this.updateRuleForAppIdleLocked(uid);
                    NetworkPolicyManagerService.this.updateRulesForPowerRestrictionsLocked(uid);
                }
            } catch (NameNotFoundException e) {
            }
        }

        public void onParoleStateChanged(boolean isParoleOn) {
            synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                NetworkPolicyManagerService.this.updateRulesForAppIdleParoleLocked();
            }
        }
    }

    private class NetworkPolicyManagerInternalImpl extends NetworkPolicyManagerInternal {
        private NetworkPolicyManagerInternalImpl() {
        }

        public void resetUserState(int userId) {
            synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                boolean changed = NetworkPolicyManagerService.this.removeUserStateLocked(userId, false);
                if (NetworkPolicyManagerService.this.addDefaultRestrictBackgroundWhitelistUidsLocked(userId)) {
                    changed = true;
                }
                if (changed) {
                    NetworkPolicyManagerService.this.writePolicyLocked();
                }
            }
        }
    }

    public void removeUidPolicy(int r1, int r2) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.net.NetworkPolicyManagerService.removeUidPolicy(int, int):void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.net.NetworkPolicyManagerService.removeUidPolicy(int, int):void");
    }

    public NetworkPolicyManagerService(Context context, IActivityManager activityManager, IPowerManager powerManager, INetworkStatsService networkStats, INetworkManagementService networkManagement) {
        this(context, activityManager, powerManager, networkStats, networkManagement, NtpTrustedTime.getInstance(context), getSystemDir(), false);
    }

    private static File getSystemDir() {
        return new File(Environment.getDataDirectory(), "system");
    }

    public NetworkPolicyManagerService(Context context, IActivityManager activityManager, IPowerManager powerManager, INetworkStatsService networkStats, INetworkManagementService networkManagement, TrustedTime time, File systemDir, boolean suppressDefaultPolicy) {
        this.mRulesLock = new Object();
        this.mNetworkPolicy = new ArrayMap();
        this.mNetworkRules = new ArrayMap();
        this.mUidPolicy = new SparseIntArray();
        this.mUidRules = new SparseIntArray();
        this.mUidFirewallStandbyRules = new SparseIntArray();
        this.mUidFirewallDozableRules = new SparseIntArray();
        this.mUidFirewallPowerSaveRules = new SparseIntArray();
        this.mFirewallChainStates = new SparseBooleanArray();
        this.mPowerSaveWhitelistExceptIdleAppIds = new SparseBooleanArray();
        this.mPowerSaveWhitelistAppIds = new SparseBooleanArray();
        this.mPowerSaveTempWhitelistAppIds = new SparseBooleanArray();
        this.mRestrictBackgroundWhitelistUids = new SparseBooleanArray();
        this.mDefaultRestrictBackgroundWhitelistUids = new SparseBooleanArray();
        this.mRestrictBackgroundWhitelistRevokedUids = new SparseBooleanArray();
        this.mMeteredIfaces = new ArraySet();
        this.mOverLimitNotified = new ArraySet();
        this.mActiveNotifs = new ArraySet();
        this.mUidState = new SparseIntArray();
        this.mListeners = new RemoteCallbackList();
        this.mUidObserver = new IUidObserver.Stub() {
            public void onUidStateChanged(int uid, int procState) throws RemoteException {
                synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                    NetworkPolicyManagerService.this.updateUidStateLocked(uid, procState);
                }
            }

            public void onUidGone(int uid) throws RemoteException {
                synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                    NetworkPolicyManagerService.this.removeUidStateLocked(uid);
                }
            }

            public void onUidActive(int uid) throws RemoteException {
            }

            public void onUidIdle(int uid) throws RemoteException {
            }
        };
        this.mPowerSaveWhitelistReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                    NetworkPolicyManagerService.this.updatePowerSaveWhitelistLocked();
                    NetworkPolicyManagerService.this.updateRulesForGlobalChangeLocked(false);
                }
            }
        };
        this.mTempPowerSaveChangedCallback = new Runnable() {
            public void run() {
                synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                    NetworkPolicyManagerService.this.updatePowerSaveTempWhitelistLocked();
                    NetworkPolicyManagerService.this.updateRulesForTempWhitelistChangeLocked();
                    NetworkPolicyManagerService.this.purgePowerSaveTempWhitelistLocked();
                }
            }
        };
        this.mScreenReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkPolicyManagerService.this.mHandler.obtainMessage(8).sendToTarget();
            }
        };
        this.mPackageReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                if (uid != -1 && "android.intent.action.PACKAGE_ADDED".equals(action)) {
                    synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                        NetworkPolicyManagerService.this.updateRestrictionRulesForUidLocked(uid);
                    }
                }
            }
        };
        this.mUidRemovedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                if (uid != -1) {
                    synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                        NetworkPolicyManagerService.this.mUidPolicy.delete(uid);
                        NetworkPolicyManagerService.this.removeRestrictBackgroundWhitelistedUidLocked(uid, true, true);
                        NetworkPolicyManagerService.this.updateRestrictionRulesForUidLocked(uid);
                        NetworkPolicyManagerService.this.writePolicyLocked();
                    }
                }
            }
        };
        this.mUserReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (userId != -1) {
                    if (!action.equals("android.intent.action.USER_REMOVED")) {
                        if (action.equals("android.intent.action.USER_ADDED")) {
                        }
                    }
                    synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                        NetworkPolicyManagerService.this.removeUserStateLocked(userId, true);
                        if (action == "android.intent.action.USER_ADDED") {
                            NetworkPolicyManagerService.this.addDefaultRestrictBackgroundWhitelistUidsLocked(userId);
                        }
                        NetworkPolicyManagerService.this.updateRulesForGlobalChangeLocked(true);
                    }
                }
            }
        };
        this.mStatsReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkPolicyManagerService.this.maybeRefreshTrustedTime();
                synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                    NetworkPolicyManagerService.this.updateNetworkEnabledLocked();
                    NetworkPolicyManagerService.this.updateNotificationsLocked();
                }
            }
        };
        this.mAllowReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkPolicyManagerService.this.setRestrictBackground(false);
            }
        };
        this.mSnoozeWarningReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkPolicyManagerService.this.performSnooze((NetworkTemplate) intent.getParcelableExtra("android.net.NETWORK_TEMPLATE"), 1);
            }
        };
        this.mWifiConfigReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getIntExtra("changeReason", 0) == 1) {
                    WifiConfiguration config = (WifiConfiguration) intent.getParcelableExtra("wifiConfiguration");
                    if (config.SSID != null) {
                        NetworkTemplate template = NetworkTemplate.buildTemplateWifi(config.SSID);
                        synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                            if (NetworkPolicyManagerService.this.mNetworkPolicy.containsKey(template)) {
                                NetworkPolicyManagerService.this.mNetworkPolicy.remove(template);
                                NetworkPolicyManagerService.this.writePolicyLocked();
                            }
                        }
                    }
                }
            }
        };
        this.mWifiStateReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (netInfo != null && netInfo.isConnected()) {
                    WifiInfo info = (WifiInfo) intent.getParcelableExtra("wifiInfo");
                    boolean meteredHint = info.getMeteredHint();
                    NetworkTemplate template = NetworkTemplate.buildTemplateWifi(info.getSSID());
                    synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                        NetworkPolicy policy = (NetworkPolicy) NetworkPolicyManagerService.this.mNetworkPolicy.get(template);
                        if (policy == null && meteredHint) {
                            NetworkPolicyManagerService.this.addNetworkPolicyLocked(NetworkPolicyManagerService.newWifiPolicy(template, meteredHint));
                        } else if (policy != null) {
                            if (policy.inferred) {
                                policy.metered = meteredHint;
                                NetworkPolicyManagerService.this.updateNetworkRulesLocked();
                            }
                        }
                    }
                }
            }
        };
        this.mAlertObserver = new BaseNetworkObserver() {
            public void limitReached(String limitName, String iface) {
                NetworkPolicyManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", NetworkPolicyManagerService.TAG);
                if (!NetworkManagementService.LIMIT_GLOBAL_ALERT.equals(limitName)) {
                    NetworkPolicyManagerService.this.mHandler.obtainMessage(5, iface).sendToTarget();
                }
            }
        };
        this.mConnReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkPolicyManagerService.this.maybeRefreshTrustedTime();
                synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                    NetworkPolicyManagerService.this.ensureActiveMobilePolicyLocked();
                    NetworkPolicyManagerService.this.normalizePoliciesLocked();
                    NetworkPolicyManagerService.this.updateNetworkEnabledLocked();
                    NetworkPolicyManagerService.this.updateNetworkRulesLocked();
                    NetworkPolicyManagerService.this.updateNotificationsLocked();
                }
            }
        };
        this.mHandlerCallback = new Callback() {
            public boolean handleMessage(Message msg) {
                int uid;
                int length;
                int i;
                Intent intent;
                switch (msg.what) {
                    case 1:
                        uid = msg.arg1;
                        int uidRules = msg.arg2;
                        NetworkPolicyManagerService.this.dispatchUidRulesChanged(NetworkPolicyManagerService.this.mConnectivityListener, uid, uidRules);
                        length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (i = 0; i < length; i++) {
                            NetworkPolicyManagerService.this.dispatchUidRulesChanged((INetworkPolicyListener) NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i), uid, uidRules);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        return true;
                    case 2:
                        String[] meteredIfaces = msg.obj;
                        NetworkPolicyManagerService.this.dispatchMeteredIfacesChanged(NetworkPolicyManagerService.this.mConnectivityListener, meteredIfaces);
                        length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (i = 0; i < length; i++) {
                            NetworkPolicyManagerService.this.dispatchMeteredIfacesChanged((INetworkPolicyListener) NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i), meteredIfaces);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        return true;
                    case 5:
                        String iface = msg.obj;
                        NetworkPolicyManagerService.this.maybeRefreshTrustedTime();
                        synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                            if (NetworkPolicyManagerService.this.mMeteredIfaces.contains(iface)) {
                                try {
                                    NetworkPolicyManagerService.this.mNetworkStats.forceUpdate();
                                } catch (RemoteException e) {
                                }
                                NetworkPolicyManagerService.this.updateNetworkEnabledLocked();
                                NetworkPolicyManagerService.this.updateNotificationsLocked();
                            }
                        }
                        return true;
                    case 6:
                        boolean restrictBackground = msg.arg1 != 0;
                        NetworkPolicyManagerService.this.dispatchRestrictBackgroundChanged(NetworkPolicyManagerService.this.mConnectivityListener, restrictBackground);
                        length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (i = 0; i < length; i++) {
                            NetworkPolicyManagerService.this.dispatchRestrictBackgroundChanged((INetworkPolicyListener) NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i), restrictBackground);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        intent = new Intent("android.net.conn.RESTRICT_BACKGROUND_CHANGED");
                        intent.setFlags(1073741824);
                        NetworkPolicyManagerService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                        return true;
                    case 7:
                        try {
                            NetworkPolicyManagerService.this.mNetworkStats.advisePersistThreshold(((Long) msg.obj).longValue() / 1000);
                        } catch (RemoteException e2) {
                        }
                        return true;
                    case 8:
                        NetworkPolicyManagerService.this.updateScreenOn();
                        return true;
                    case 9:
                        uid = msg.arg1;
                        boolean changed = msg.arg2 == 1;
                        Boolean whitelisted = msg.obj;
                        if (whitelisted != null) {
                            boolean whitelistedBool = whitelisted.booleanValue();
                            NetworkPolicyManagerService.this.dispatchRestrictBackgroundWhitelistChanged(NetworkPolicyManagerService.this.mConnectivityListener, uid, whitelistedBool);
                            length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                            for (i = 0; i < length; i++) {
                                NetworkPolicyManagerService.this.dispatchRestrictBackgroundWhitelistChanged((INetworkPolicyListener) NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i), uid, whitelistedBool);
                            }
                            NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        }
                        String[] packages = NetworkPolicyManagerService.this.mContext.getPackageManager().getPackagesForUid(uid);
                        if (changed && packages != null) {
                            int userId = UserHandle.getUserId(uid);
                            for (String packageName : packages) {
                                intent = new Intent("android.net.conn.RESTRICT_BACKGROUND_CHANGED");
                                intent.setPackage(packageName);
                                intent.setFlags(1073741824);
                                NetworkPolicyManagerService.this.mContext.sendBroadcastAsUser(intent, UserHandle.of(userId));
                            }
                        }
                        return true;
                    case 10:
                        NetworkPolicyManagerService.this.removeInterfaceQuota((String) msg.obj);
                        NetworkPolicyManagerService.this.setInterfaceQuota((String) msg.obj, (((long) msg.arg1) << 32) | (((long) msg.arg2) & 4294967295L));
                        return true;
                    case 11:
                        NetworkPolicyManagerService.this.removeInterfaceQuota((String) msg.obj);
                        return true;
                    case 12:
                        uid = msg.arg1;
                        boolean blacklisted = msg.arg2 == 1;
                        NetworkPolicyManagerService.this.dispatchRestrictBackgroundBlacklistChanged(NetworkPolicyManagerService.this.mConnectivityListener, uid, blacklisted);
                        length = NetworkPolicyManagerService.this.mListeners.beginBroadcast();
                        for (i = 0; i < length; i++) {
                            NetworkPolicyManagerService.this.dispatchRestrictBackgroundBlacklistChanged((INetworkPolicyListener) NetworkPolicyManagerService.this.mListeners.getBroadcastItem(i), uid, blacklisted);
                        }
                        NetworkPolicyManagerService.this.mListeners.finishBroadcast();
                        return true;
                    default:
                        return false;
                }
            }
        };
        this.mContext = (Context) Preconditions.checkNotNull(context, "missing context");
        this.mActivityManager = (IActivityManager) Preconditions.checkNotNull(activityManager, "missing activityManager");
        this.mPowerManager = (IPowerManager) Preconditions.checkNotNull(powerManager, "missing powerManager");
        this.mNetworkStats = (INetworkStatsService) Preconditions.checkNotNull(networkStats, "missing networkStats");
        this.mNetworkManager = (INetworkManagementService) Preconditions.checkNotNull(networkManagement, "missing networkManagement");
        this.mDeviceIdleController = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
        this.mTime = (TrustedTime) Preconditions.checkNotNull(time, "missing TrustedTime");
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mIPm = AppGlobals.getPackageManager();
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new Handler(thread.getLooper(), this.mHandlerCallback);
        this.mSuppressDefaultPolicy = suppressDefaultPolicy;
        this.mPolicyFile = new AtomicFile(new File(systemDir, "netpolicy.xml"));
        this.mAppOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        LocalServices.addService(NetworkPolicyManagerInternal.class, new NetworkPolicyManagerInternalImpl());
    }

    public void bindConnectivityManager(IConnectivityManager connManager) {
        this.mConnManager = (IConnectivityManager) Preconditions.checkNotNull(connManager, "missing IConnectivityManager");
    }

    public void bindNotificationManager(INotificationManager notifManager) {
        this.mNotifManager = (INotificationManager) Preconditions.checkNotNull(notifManager, "missing INotificationManager");
    }

    void updatePowerSaveWhitelistLocked() {
        int i = 0;
        try {
            int length;
            int[] whitelist = this.mDeviceIdleController.getAppIdWhitelistExceptIdle();
            this.mPowerSaveWhitelistExceptIdleAppIds.clear();
            if (whitelist != null) {
                for (int uid : whitelist) {
                    this.mPowerSaveWhitelistExceptIdleAppIds.put(uid, true);
                }
            }
            whitelist = this.mDeviceIdleController.getAppIdWhitelist();
            this.mPowerSaveWhitelistAppIds.clear();
            if (whitelist != null) {
                length = whitelist.length;
                while (i < length) {
                    this.mPowerSaveWhitelistAppIds.put(whitelist[i], true);
                    i++;
                }
            }
        } catch (RemoteException e) {
        }
    }

    boolean addDefaultRestrictBackgroundWhitelistUidsLocked() {
        List<UserInfo> users = this.mUserManager.getUsers();
        int numberUsers = users.size();
        boolean changed = false;
        for (int i = 0; i < numberUsers; i++) {
            if (addDefaultRestrictBackgroundWhitelistUidsLocked(((UserInfo) users.get(i)).id)) {
                changed = true;
            }
        }
        return changed;
    }

    private boolean addDefaultRestrictBackgroundWhitelistUidsLocked(int userId) {
        SystemConfig sysConfig = SystemConfig.getInstance();
        PackageManager pm = this.mContext.getPackageManager();
        ArraySet<String> allowDataUsage = sysConfig.getAllowInDataUsageSave();
        boolean changed = false;
        for (int i = 0; i < allowDataUsage.size(); i++) {
            String pkg = (String) allowDataUsage.valueAt(i);
            Slog.d(TAG, "checking restricted background whitelisting for package " + pkg + " and user " + userId);
            try {
                ApplicationInfo app = pm.getApplicationInfoAsUser(pkg, DumpState.DUMP_DEXOPT, userId);
                if (app.isPrivilegedApp()) {
                    int uid = UserHandle.getUid(userId, app.uid);
                    this.mDefaultRestrictBackgroundWhitelistUids.append(uid, true);
                    Slog.d(TAG, "Adding uid " + uid + " (user " + userId + ") to default restricted " + "background whitelist. Revoked status: " + this.mRestrictBackgroundWhitelistRevokedUids.get(uid));
                    if (!this.mRestrictBackgroundWhitelistRevokedUids.get(uid)) {
                        Slog.i(TAG, "adding default package " + pkg + " (uid " + uid + " for user " + userId + ") to restrict background whitelist");
                        this.mRestrictBackgroundWhitelistUids.append(uid, true);
                        changed = true;
                    }
                } else {
                    Slog.wtf(TAG, "pm.getApplicationInfoAsUser() returned non-privileged app: " + pkg);
                }
            } catch (NameNotFoundException e) {
                Slog.wtf(TAG, "No ApplicationInfo for package " + pkg);
            }
        }
        return changed;
    }

    void updatePowerSaveTempWhitelistLocked() {
        try {
            int N = this.mPowerSaveTempWhitelistAppIds.size();
            for (int i = 0; i < N; i++) {
                this.mPowerSaveTempWhitelistAppIds.setValueAt(i, false);
            }
            int[] whitelist = this.mDeviceIdleController.getAppIdTempWhitelist();
            if (whitelist != null) {
                for (int uid : whitelist) {
                    this.mPowerSaveTempWhitelistAppIds.put(uid, true);
                }
            }
        } catch (RemoteException e) {
        }
    }

    void purgePowerSaveTempWhitelistLocked() {
        for (int i = this.mPowerSaveTempWhitelistAppIds.size() - 1; i >= 0; i--) {
            if (!this.mPowerSaveTempWhitelistAppIds.valueAt(i)) {
                this.mPowerSaveTempWhitelistAppIds.removeAt(i);
            }
        }
    }

    public void systemReady() {
        if (isBandwidthControlEnabled()) {
            this.mUsageStats = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
            synchronized (this.mRulesLock) {
                updatePowerSaveWhitelistLocked();
                this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
                this.mPowerManagerInternal.registerLowPowerModeObserver(new LowPowerModeListener() {
                    public void onLowPowerModeChanged(boolean enabled) {
                        Slog.d(NetworkPolicyManagerService.TAG, "onLowPowerModeChanged(" + enabled + ")");
                        synchronized (NetworkPolicyManagerService.this.mRulesLock) {
                            if (NetworkPolicyManagerService.this.mRestrictPower != enabled) {
                                NetworkPolicyManagerService.this.mRestrictPower = enabled;
                                NetworkPolicyManagerService.this.updateRulesForRestrictPowerLocked();
                                NetworkPolicyManagerService.this.updateRulesForGlobalChangeLocked(true);
                            }
                        }
                    }
                });
                this.mRestrictPower = this.mPowerManagerInternal.getLowPowerModeEnabled();
                this.mSystemReady = true;
                readPolicyLocked();
                if (addDefaultRestrictBackgroundWhitelistUidsLocked()) {
                    writePolicyLocked();
                }
                updateRulesForGlobalChangeLocked(false);
                updateNotificationsLocked();
            }
            updateScreenOn();
            try {
                this.mActivityManager.registerUidObserver(this.mUidObserver, 3);
                this.mNetworkManager.registerObserver(this.mAlertObserver);
            } catch (RemoteException e) {
            }
            IntentFilter screenFilter = new IntentFilter();
            screenFilter.addAction("android.intent.action.SCREEN_ON");
            screenFilter.addAction("android.intent.action.SCREEN_OFF");
            this.mContext.registerReceiver(this.mScreenReceiver, screenFilter);
            IntentFilter whitelistFilter = new IntentFilter("android.os.action.POWER_SAVE_WHITELIST_CHANGED");
            this.mContext.registerReceiver(this.mPowerSaveWhitelistReceiver, whitelistFilter, null, this.mHandler);
            ((LocalService) LocalServices.getService(LocalService.class)).setNetworkPolicyTempWhitelistCallback(this.mTempPowerSaveChangedCallback);
            IntentFilter connFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            this.mContext.registerReceiver(this.mConnReceiver, connFilter, "android.permission.CONNECTIVITY_INTERNAL", this.mHandler);
            IntentFilter packageFilter = new IntentFilter();
            packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
            packageFilter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
            this.mContext.registerReceiver(this.mPackageReceiver, packageFilter, null, this.mHandler);
            this.mContext.registerReceiver(this.mUidRemovedReceiver, new IntentFilter("android.intent.action.UID_REMOVED"), null, this.mHandler);
            IntentFilter userFilter = new IntentFilter();
            userFilter.addAction("android.intent.action.USER_ADDED");
            userFilter.addAction("android.intent.action.USER_REMOVED");
            this.mContext.registerReceiver(this.mUserReceiver, userFilter, null, this.mHandler);
            IntentFilter statsFilter = new IntentFilter(NetworkStatsService.ACTION_NETWORK_STATS_UPDATED);
            this.mContext.registerReceiver(this.mStatsReceiver, statsFilter, "android.permission.READ_NETWORK_USAGE_HISTORY", this.mHandler);
            IntentFilter allowFilter = new IntentFilter(ACTION_ALLOW_BACKGROUND);
            this.mContext.registerReceiver(this.mAllowReceiver, allowFilter, "android.permission.MANAGE_NETWORK_POLICY", this.mHandler);
            IntentFilter snoozeWarningFilter = new IntentFilter(ACTION_SNOOZE_WARNING);
            this.mContext.registerReceiver(this.mSnoozeWarningReceiver, snoozeWarningFilter, "android.permission.MANAGE_NETWORK_POLICY", this.mHandler);
            IntentFilter wifiConfigFilter = new IntentFilter("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
            this.mContext.registerReceiver(this.mWifiConfigReceiver, wifiConfigFilter, null, this.mHandler);
            IntentFilter wifiStateFilter = new IntentFilter("android.net.wifi.STATE_CHANGE");
            this.mContext.registerReceiver(this.mWifiStateReceiver, wifiStateFilter, null, this.mHandler);
            this.mUsageStats.addAppIdleStateChangeListener(new AppIdleStateChangeListener());
            return;
        }
        Slog.w(TAG, "bandwidth controls disabled, unable to enforce policy");
    }

    static NetworkPolicy newWifiPolicy(NetworkTemplate template, boolean metered) {
        return new NetworkPolicy(template, -1, "UTC", -1, -1, -1, -1, metered, true);
    }

    void updateNotificationsLocked() {
        int i;
        ArraySet<String> beforeNotifs = new ArraySet(this.mActiveNotifs);
        this.mActiveNotifs.clear();
        long currentTime = currentTimeMillis();
        for (i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
            NetworkPolicy policy = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
            if (isTemplateRelevant(policy.template) && policy.hasCycle()) {
                long start = NetworkPolicyManager.computeLastCycleBoundary(currentTime, policy);
                long end = currentTime;
                long totalBytes = getTotalBytes(policy.template, start, currentTime);
                if (!policy.isOverLimit(totalBytes)) {
                    notifyUnderLimitLocked(policy.template);
                    if (policy.isOverWarning(totalBytes) && policy.lastWarningSnooze < start) {
                        enqueueNotification(policy, 1, totalBytes);
                    }
                } else if (policy.lastLimitSnooze >= start) {
                    enqueueNotification(policy, 3, totalBytes);
                } else {
                    enqueueNotification(policy, 2, totalBytes);
                    notifyOverLimitLocked(policy.template);
                }
            }
        }
        for (i = beforeNotifs.size() - 1; i >= 0; i--) {
            String tag = (String) beforeNotifs.valueAt(i);
            if (!this.mActiveNotifs.contains(tag)) {
                cancelNotification(tag);
            }
        }
    }

    private boolean isTemplateRelevant(NetworkTemplate template) {
        if (!template.isMatchRuleMobile()) {
            return true;
        }
        TelephonyManager tele = TelephonyManager.from(this.mContext);
        for (int subId : SubscriptionManager.from(this.mContext).getActiveSubscriptionIdList()) {
            if (template.matches(new NetworkIdentity(0, 0, tele.getSubscriberId(subId), null, false, true))) {
                return true;
            }
        }
        return false;
    }

    private void notifyOverLimitLocked(NetworkTemplate template) {
        if (!this.mOverLimitNotified.contains(template)) {
            this.mContext.startActivity(buildNetworkOverLimitIntent(template));
            this.mOverLimitNotified.add(template);
        }
    }

    private void notifyUnderLimitLocked(NetworkTemplate template) {
        this.mOverLimitNotified.remove(template);
    }

    private String buildNotificationTag(NetworkPolicy policy, int type) {
        return "NetworkPolicy:" + policy.template.hashCode() + ":" + type;
    }

    private void enqueueNotification(NetworkPolicy policy, int type, long totalBytes) {
        String tag = buildNotificationTag(policy, type);
        Builder builder = new Builder(this.mContext);
        builder.setOnlyAlertOnce(true);
        builder.setWhen(0);
        builder.setColor(this.mContext.getColor(17170519));
        Resources res = this.mContext.getResources();
        CharSequence body;
        CharSequence text;
        switch (type) {
            case 1:
                return;
            case 2:
                body = res.getText(17040573);
                int icon = 17303210;
                switch (policy.template.getMatchRule()) {
                    case 1:
                        text = res.getText(17040571);
                        break;
                    case 2:
                        text = res.getText(17040569);
                        break;
                    case 3:
                        text = res.getText(17040570);
                        break;
                    case 4:
                        text = res.getText(17040572);
                        icon = 17301624;
                        break;
                    default:
                        text = null;
                        break;
                }
                builder.setOngoing(true);
                builder.setSmallIcon(icon);
                Bitmap bmp2 = BitmapFactory.decodeResource(res, 33751589);
                if (bmp2 != null) {
                    builder.setLargeIcon(bmp2);
                }
                builder.setTicker(text);
                builder.setContentTitle(text);
                builder.setContentText(body);
                builder.setContentIntent(PendingIntent.getActivity(this.mContext, 0, buildNetworkOverLimitIntent(policy.template), 134217728));
                break;
            case 3:
                long overBytes = totalBytes - policy.limitBytes;
                body = res.getString(17040578, new Object[]{Formatter.formatFileSize(this.mContext, overBytes)});
                switch (policy.template.getMatchRule()) {
                    case 1:
                        text = res.getText(17040576);
                        break;
                    case 2:
                        text = res.getText(17040574);
                        break;
                    case 3:
                        text = res.getText(17040575);
                        break;
                    case 4:
                        text = res.getText(17040577);
                        break;
                    default:
                        text = null;
                        break;
                }
                builder.setOngoing(true);
                builder.setSmallIcon(17301624);
                Bitmap bmp3 = BitmapFactory.decodeResource(res, 33751591);
                if (bmp3 != null) {
                    builder.setLargeIcon(bmp3);
                }
                builder.setTicker(text);
                builder.setContentTitle(text);
                builder.setContentText(body);
                builder.setContentIntent(PendingIntent.getActivity(this.mContext, 0, buildViewDataUsageIntent(policy.template), 134217728));
                return;
        }
        try {
            String packageName = this.mContext.getPackageName();
            String str = packageName;
            this.mNotifManager.enqueueNotificationWithTag(packageName, str, tag, 0, builder.getNotification(), new int[1], -1);
            this.mActiveNotifs.add(tag);
        } catch (RemoteException e) {
        }
    }

    private void cancelNotification(String tag) {
        try {
            this.mNotifManager.cancelNotificationWithTag(this.mContext.getPackageName(), tag, 0, -1);
        } catch (RemoteException e) {
        }
    }

    void updateNetworkEnabledLocked() {
        long currentTime = currentTimeMillis();
        for (int i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
            NetworkPolicy policy = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
            if (policy.limitBytes == -1 || !policy.hasCycle()) {
                setNetworkTemplateEnabled(policy.template, true);
            } else {
                long start = NetworkPolicyManager.computeLastCycleBoundary(currentTime, policy);
                long end = currentTime;
                boolean overLimitWithoutSnooze = policy.isOverLimit(getTotalBytes(policy.template, start, currentTime)) ? policy.lastLimitSnooze < start : false;
                setNetworkTemplateEnabled(policy.template, !overLimitWithoutSnooze);
            }
        }
    }

    private void setNetworkTemplateEnabled(NetworkTemplate template, boolean enabled) {
    }

    void updateNetworkRulesLocked() {
        try {
            int i;
            NetworkPolicy policy;
            String iface;
            NetworkState[] states = this.mConnManager.getAllNetworkState();
            ArrayList<Pair<String, NetworkIdentity>> connIdents = new ArrayList(states.length);
            ArraySet<String> connIfaces = new ArraySet(states.length);
            for (NetworkState state : states) {
                if (!(state == null || state.networkInfo == null || !state.networkInfo.isConnected())) {
                    NetworkIdentity ident = NetworkIdentity.buildNetworkIdentity(this.mContext, state);
                    String baseIface = state.linkProperties.getInterfaceName();
                    if (baseIface != null) {
                        connIdents.add(Pair.create(baseIface, ident));
                    }
                    for (LinkProperties stackedLink : state.linkProperties.getStackedLinks()) {
                        String stackedIface = stackedLink.getInterfaceName();
                        if (stackedIface != null) {
                            connIdents.add(Pair.create(stackedIface, ident));
                        }
                    }
                }
            }
            this.mNetworkRules.clear();
            ArrayList<String> ifaceList = Lists.newArrayList();
            for (i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
                policy = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
                ifaceList.clear();
                for (int j = connIdents.size() - 1; j >= 0; j--) {
                    Pair<String, NetworkIdentity> ident2 = (Pair) connIdents.get(j);
                    if (policy.template.matches((NetworkIdentity) ident2.second)) {
                        ifaceList.add((String) ident2.first);
                    }
                }
                if (ifaceList.size() > 0) {
                    this.mNetworkRules.put(policy, (String[]) ifaceList.toArray(new String[ifaceList.size()]));
                }
            }
            long lowestRule = JobStatus.NO_LATEST_RUNTIME;
            ArraySet<String> arraySet = new ArraySet(states.length);
            long currentTime = currentTimeMillis();
            for (i = this.mNetworkRules.size() - 1; i >= 0; i--) {
                long start;
                long totalBytes;
                policy = (NetworkPolicy) this.mNetworkRules.keyAt(i);
                String[] ifaces = (String[]) this.mNetworkRules.valueAt(i);
                if (policy.hasCycle()) {
                    start = NetworkPolicyManager.computeLastCycleBoundary(currentTime, policy);
                    totalBytes = getTotalBytes(policy.template, start, currentTime);
                } else {
                    start = JobStatus.NO_LATEST_RUNTIME;
                    totalBytes = 0;
                }
                Slog.d(TAG, "applying policy " + policy + " to ifaces " + Arrays.toString(ifaces));
                boolean hasWarning = policy.warningBytes != -1;
                boolean hasLimit = policy.limitBytes != -1;
                if (hasLimit || policy.metered) {
                    long quotaBytes;
                    if (!hasLimit) {
                        quotaBytes = JobStatus.NO_LATEST_RUNTIME;
                    } else if (policy.lastLimitSnooze >= start) {
                        quotaBytes = JobStatus.NO_LATEST_RUNTIME;
                    } else {
                        quotaBytes = Math.max(1, policy.limitBytes - totalBytes);
                    }
                    if (ifaces.length > 1) {
                        Slog.w(TAG, "shared quota unsupported; generating rule for each iface");
                    }
                    for (String iface2 : ifaces) {
                        this.mHandler.obtainMessage(10, (int) (quotaBytes >> 32), (int) (-1 & quotaBytes), iface2).sendToTarget();
                        arraySet.add(iface2);
                    }
                }
                if (hasWarning && policy.warningBytes < lowestRule) {
                    lowestRule = policy.warningBytes;
                }
                if (hasLimit && policy.limitBytes < lowestRule) {
                    lowestRule = policy.limitBytes;
                }
            }
            for (i = connIfaces.size() - 1; i >= 0; i--) {
                iface2 = (String) connIfaces.valueAt(i);
                this.mHandler.obtainMessage(10, Integer.MAX_VALUE, -1, iface2).sendToTarget();
                arraySet.add(iface2);
            }
            this.mHandler.obtainMessage(7, Long.valueOf(lowestRule)).sendToTarget();
            for (i = this.mMeteredIfaces.size() - 1; i >= 0; i--) {
                iface2 = (String) this.mMeteredIfaces.valueAt(i);
                if (!arraySet.contains(iface2)) {
                    this.mHandler.obtainMessage(11, iface2).sendToTarget();
                }
            }
            this.mMeteredIfaces = arraySet;
            this.mHandler.obtainMessage(2, (String[]) this.mMeteredIfaces.toArray(new String[this.mMeteredIfaces.size()])).sendToTarget();
        } catch (RemoteException e) {
        }
    }

    private void ensureActiveMobilePolicyLocked() {
        if (!this.mSuppressDefaultPolicy) {
            TelephonyManager tele = TelephonyManager.from(this.mContext);
            for (int subId : SubscriptionManager.from(this.mContext).getActiveSubscriptionIdList()) {
                ensureActiveMobilePolicyLocked(tele.getSubscriberId(subId));
            }
        }
    }

    private void ensureActiveMobilePolicyLocked(String subscriberId) {
        NetworkIdentity probeIdent = new NetworkIdentity(0, 0, subscriberId, null, false, true);
        for (int i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
            NetworkTemplate template = (NetworkTemplate) this.mNetworkPolicy.keyAt(i);
            if (template.matches(probeIdent)) {
                Slog.d(TAG, "Found template " + template + " which matches subscriber " + NetworkIdentity.scrubSubscriberId(subscriberId));
                return;
            }
        }
        Slog.i(TAG, "No policy for subscriber " + NetworkIdentity.scrubSubscriberId(subscriberId) + "; generating default policy");
        Time time = new Time();
        time.setToNow();
        addNetworkPolicyLocked(new NetworkPolicy(NetworkTemplate.buildTemplateMobileAll(subscriberId), time.monthDay, time.timezone, -1, -1, -1, -1, true, true));
    }

    protected void readPolicyLocked() {
        this.mNetworkPolicy.clear();
        this.mUidPolicy.clear();
        AutoCloseable autoCloseable = null;
        try {
            autoCloseable = this.mPolicyFile.openRead();
            XmlPullParser in = Xml.newPullParser();
            in.setInput(autoCloseable, StandardCharsets.UTF_8.name());
            int version = 1;
            boolean insideWhitelist = false;
            while (true) {
                int type = in.next();
                if (type != 1) {
                    String tag = in.getName();
                    if (type == 2) {
                        if (TAG_POLICY_LIST.equals(tag)) {
                            boolean oldValue = this.mRestrictBackground;
                            version = XmlUtils.readIntAttribute(in, ATTR_VERSION);
                            if (version >= 3) {
                                this.mRestrictBackground = XmlUtils.readBooleanAttribute(in, ATTR_RESTRICT_BACKGROUND);
                            } else {
                                this.mRestrictBackground = false;
                            }
                            if (this.mRestrictBackground != oldValue) {
                                this.mHandler.obtainMessage(6, this.mRestrictBackground ? 1 : 0, 0).sendToTarget();
                            }
                        } else if (TAG_NETWORK_POLICY.equals(tag)) {
                            String attributeValue;
                            String cycleTimezone;
                            long lastLimitSnooze;
                            boolean metered;
                            long lastWarningSnooze;
                            boolean readBooleanAttribute;
                            int networkTemplate = XmlUtils.readIntAttribute(in, ATTR_NETWORK_TEMPLATE);
                            String subscriberId = in.getAttributeValue(null, ATTR_SUBSCRIBER_ID);
                            if (version >= 9) {
                                attributeValue = in.getAttributeValue(null, ATTR_NETWORK_ID);
                            } else {
                                attributeValue = null;
                            }
                            int cycleDay = XmlUtils.readIntAttribute(in, ATTR_CYCLE_DAY);
                            if (version >= 6) {
                                cycleTimezone = in.getAttributeValue(null, ATTR_CYCLE_TIMEZONE);
                            } else {
                                cycleTimezone = "UTC";
                            }
                            long warningBytes = XmlUtils.readLongAttribute(in, ATTR_WARNING_BYTES);
                            long limitBytes = XmlUtils.readLongAttribute(in, ATTR_LIMIT_BYTES);
                            if (version >= 5) {
                                lastLimitSnooze = XmlUtils.readLongAttribute(in, ATTR_LAST_LIMIT_SNOOZE);
                            } else if (version >= 2) {
                                lastLimitSnooze = XmlUtils.readLongAttribute(in, ATTR_LAST_SNOOZE);
                            } else {
                                lastLimitSnooze = -1;
                            }
                            if (version < 4) {
                                switch (networkTemplate) {
                                    case 1:
                                    case 2:
                                    case 3:
                                        metered = true;
                                        break;
                                    default:
                                        metered = false;
                                        break;
                                }
                            }
                            metered = XmlUtils.readBooleanAttribute(in, ATTR_METERED);
                            if (version >= 5) {
                                lastWarningSnooze = XmlUtils.readLongAttribute(in, ATTR_LAST_WARNING_SNOOZE);
                            } else {
                                lastWarningSnooze = -1;
                            }
                            if (version >= 7) {
                                readBooleanAttribute = XmlUtils.readBooleanAttribute(in, ATTR_INFERRED);
                            } else {
                                readBooleanAttribute = false;
                            }
                            NetworkTemplate template = new NetworkTemplate(networkTemplate, subscriberId, attributeValue);
                            if (template.isPersistable()) {
                                this.mNetworkPolicy.put(template, new NetworkPolicy(template, cycleDay, cycleTimezone, warningBytes, limitBytes, lastWarningSnooze, lastLimitSnooze, metered, readBooleanAttribute));
                            }
                        } else if (TAG_UID_POLICY.equals(tag)) {
                            uid = XmlUtils.readIntAttribute(in, ATTR_UID);
                            policy = XmlUtils.readIntAttribute(in, ATTR_POLICY);
                            if (UserHandle.isApp(uid)) {
                                setUidPolicyUncheckedLocked(uid, policy, false);
                            } else {
                                Slog.w(TAG, "unable to apply policy to UID " + uid + "; ignoring");
                            }
                        } else if (TAG_APP_POLICY.equals(tag)) {
                            int appId = XmlUtils.readIntAttribute(in, ATTR_APP_ID);
                            policy = XmlUtils.readIntAttribute(in, ATTR_POLICY);
                            uid = UserHandle.getUid(0, appId);
                            if (UserHandle.isApp(uid)) {
                                setUidPolicyUncheckedLocked(uid, policy, false);
                            } else {
                                Slog.w(TAG, "unable to apply policy to UID " + uid + "; ignoring");
                            }
                        } else if (TAG_WHITELIST.equals(tag)) {
                            insideWhitelist = true;
                        } else if (TAG_RESTRICT_BACKGROUND.equals(tag) && insideWhitelist) {
                            this.mRestrictBackgroundWhitelistUids.put(XmlUtils.readIntAttribute(in, ATTR_UID), true);
                        } else if (TAG_REVOKED_RESTRICT_BACKGROUND.equals(tag) && insideWhitelist) {
                            this.mRestrictBackgroundWhitelistRevokedUids.put(XmlUtils.readIntAttribute(in, ATTR_UID), true);
                        }
                    } else if (type == 3 && TAG_WHITELIST.equals(tag)) {
                        insideWhitelist = false;
                    }
                } else {
                    return;
                }
            }
        } catch (FileNotFoundException e) {
            upgradeLegacyBackgroundData();
        } catch (Throwable e2) {
            Log.wtf(TAG, "problem reading network policy", e2);
        } catch (Throwable e3) {
            Log.wtf(TAG, "problem reading network policy", e3);
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    private void upgradeLegacyBackgroundData() {
        boolean z = true;
        if (Secure.getInt(this.mContext.getContentResolver(), "background_data", 1) == 1) {
            z = false;
        }
        this.mRestrictBackground = z;
        if (this.mRestrictBackground) {
            this.mContext.sendBroadcastAsUser(new Intent("android.net.conn.BACKGROUND_DATA_SETTING_CHANGED"), UserHandle.ALL);
        }
    }

    void writePolicyLocked() {
        FileOutputStream fileOutputStream = null;
        try {
            int i;
            int uid;
            fileOutputStream = this.mPolicyFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fileOutputStream, StandardCharsets.UTF_8.name());
            out.startDocument(null, Boolean.valueOf(true));
            out.startTag(null, TAG_POLICY_LIST);
            XmlUtils.writeIntAttribute(out, ATTR_VERSION, 10);
            XmlUtils.writeBooleanAttribute(out, ATTR_RESTRICT_BACKGROUND, this.mRestrictBackground);
            for (i = 0; i < this.mNetworkPolicy.size(); i++) {
                NetworkPolicy policy = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
                NetworkTemplate template = policy.template;
                if (template.isPersistable()) {
                    out.startTag(null, TAG_NETWORK_POLICY);
                    XmlUtils.writeIntAttribute(out, ATTR_NETWORK_TEMPLATE, template.getMatchRule());
                    String subscriberId = template.getSubscriberId();
                    if (subscriberId != null) {
                        out.attribute(null, ATTR_SUBSCRIBER_ID, subscriberId);
                    }
                    String networkId = template.getNetworkId();
                    if (networkId != null) {
                        out.attribute(null, ATTR_NETWORK_ID, networkId);
                    }
                    XmlUtils.writeIntAttribute(out, ATTR_CYCLE_DAY, policy.cycleDay);
                    out.attribute(null, ATTR_CYCLE_TIMEZONE, policy.cycleTimezone);
                    XmlUtils.writeLongAttribute(out, ATTR_WARNING_BYTES, policy.warningBytes);
                    XmlUtils.writeLongAttribute(out, ATTR_LIMIT_BYTES, policy.limitBytes);
                    XmlUtils.writeLongAttribute(out, ATTR_LAST_WARNING_SNOOZE, policy.lastWarningSnooze);
                    XmlUtils.writeLongAttribute(out, ATTR_LAST_LIMIT_SNOOZE, policy.lastLimitSnooze);
                    XmlUtils.writeBooleanAttribute(out, ATTR_METERED, policy.metered);
                    XmlUtils.writeBooleanAttribute(out, ATTR_INFERRED, policy.inferred);
                    out.endTag(null, TAG_NETWORK_POLICY);
                }
            }
            for (i = 0; i < this.mUidPolicy.size(); i++) {
                uid = this.mUidPolicy.keyAt(i);
                int policy2 = this.mUidPolicy.valueAt(i);
                if (policy2 != 0) {
                    out.startTag(null, TAG_UID_POLICY);
                    XmlUtils.writeIntAttribute(out, ATTR_UID, uid);
                    XmlUtils.writeIntAttribute(out, ATTR_POLICY, policy2);
                    out.endTag(null, TAG_UID_POLICY);
                }
            }
            out.endTag(null, TAG_POLICY_LIST);
            out.startTag(null, TAG_WHITELIST);
            int size = this.mRestrictBackgroundWhitelistUids.size();
            for (i = 0; i < size; i++) {
                uid = this.mRestrictBackgroundWhitelistUids.keyAt(i);
                out.startTag(null, TAG_RESTRICT_BACKGROUND);
                XmlUtils.writeIntAttribute(out, ATTR_UID, uid);
                out.endTag(null, TAG_RESTRICT_BACKGROUND);
            }
            size = this.mRestrictBackgroundWhitelistRevokedUids.size();
            for (i = 0; i < size; i++) {
                uid = this.mRestrictBackgroundWhitelistRevokedUids.keyAt(i);
                out.startTag(null, TAG_REVOKED_RESTRICT_BACKGROUND);
                XmlUtils.writeIntAttribute(out, ATTR_UID, uid);
                out.endTag(null, TAG_REVOKED_RESTRICT_BACKGROUND);
            }
            out.endTag(null, TAG_WHITELIST);
            out.endDocument();
            this.mPolicyFile.finishWrite(fileOutputStream);
        } catch (IOException e) {
            if (fileOutputStream != null) {
                this.mPolicyFile.failWrite(fileOutputStream);
            }
        }
    }

    public void setUidPolicy(int uid, int policy) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        if (UserHandle.isApp(uid)) {
            synchronized (this.mRulesLock) {
                long token = Binder.clearCallingIdentity();
                try {
                    int oldPolicy = this.mUidPolicy.get(uid, 0);
                    if (oldPolicy != policy) {
                        setUidPolicyUncheckedLocked(uid, oldPolicy, policy, true);
                    }
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
            return;
        }
        throw new IllegalArgumentException("cannot apply policy to UID " + uid);
    }

    public void addUidPolicy(int uid, int policy) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        if (UserHandle.isApp(uid)) {
            synchronized (this.mRulesLock) {
                int oldPolicy = this.mUidPolicy.get(uid, 0);
                policy |= oldPolicy;
                if (oldPolicy != policy) {
                    setUidPolicyUncheckedLocked(uid, oldPolicy, policy, true);
                }
            }
            return;
        }
        throw new IllegalArgumentException("cannot apply policy to UID " + uid);
    }

    private void setUidPolicyUncheckedLocked(int uid, int oldPolicy, int policy, boolean persist) {
        int i = 0;
        setUidPolicyUncheckedLocked(uid, policy, persist);
        boolean isBlacklisted = policy == 1;
        Handler handler = this.mHandler;
        if (isBlacklisted) {
            i = 1;
        }
        handler.obtainMessage(12, uid, i).sendToTarget();
        boolean wasBlacklisted = oldPolicy == 1;
        if ((oldPolicy == 0 && isBlacklisted) || (wasBlacklisted && policy == 0)) {
            this.mHandler.obtainMessage(9, uid, 1, null).sendToTarget();
        }
    }

    private void setUidPolicyUncheckedLocked(int uid, int policy, boolean persist) {
        this.mUidPolicy.put(uid, policy);
        updateRulesForDataUsageRestrictionsLocked(uid);
        if (persist) {
            writePolicyLocked();
        }
    }

    public int getUidPolicy(int uid) {
        int i;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mRulesLock) {
            i = this.mUidPolicy.get(uid, 0);
        }
        return i;
    }

    public int[] getUidsWithPolicy(int policy) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        int[] uids = new int[0];
        synchronized (this.mRulesLock) {
            for (int i = 0; i < this.mUidPolicy.size(); i++) {
                int uid = this.mUidPolicy.keyAt(i);
                if (this.mUidPolicy.valueAt(i) == policy) {
                    uids = ArrayUtils.appendInt(uids, uid);
                }
            }
        }
        return uids;
    }

    boolean removeUserStateLocked(int userId, boolean writePolicy) {
        int i;
        int length;
        int i2 = 0;
        boolean changed = false;
        int[] wlUids = new int[0];
        for (i = 0; i < this.mRestrictBackgroundWhitelistUids.size(); i++) {
            int uid = this.mRestrictBackgroundWhitelistUids.keyAt(i);
            if (UserHandle.getUserId(uid) == userId) {
                wlUids = ArrayUtils.appendInt(wlUids, uid);
            }
        }
        if (wlUids.length > 0) {
            for (int uid2 : wlUids) {
                removeRestrictBackgroundWhitelistedUidLocked(uid2, false, false);
            }
            changed = true;
        }
        for (i = this.mRestrictBackgroundWhitelistRevokedUids.size() - 1; i >= 0; i--) {
            if (UserHandle.getUserId(this.mRestrictBackgroundWhitelistRevokedUids.keyAt(i)) == userId) {
                this.mRestrictBackgroundWhitelistRevokedUids.removeAt(i);
                changed = true;
            }
        }
        int[] uids = new int[0];
        for (i = 0; i < this.mUidPolicy.size(); i++) {
            uid2 = this.mUidPolicy.keyAt(i);
            if (UserHandle.getUserId(uid2) == userId) {
                uids = ArrayUtils.appendInt(uids, uid2);
            }
        }
        if (uids.length > 0) {
            length = uids.length;
            while (i2 < length) {
                this.mUidPolicy.delete(uids[i2]);
                i2++;
            }
            changed = true;
        }
        updateRulesForGlobalChangeLocked(true);
        if (writePolicy && changed) {
            writePolicyLocked();
        }
        return changed;
    }

    public void setConnectivityListener(INetworkPolicyListener listener) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (this.mConnectivityListener != null) {
            throw new IllegalStateException("Connectivity listener already registered");
        }
        this.mConnectivityListener = listener;
    }

    public void registerListener(INetworkPolicyListener listener) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        this.mListeners.register(listener);
    }

    public void unregisterListener(INetworkPolicyListener listener) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        this.mListeners.unregister(listener);
    }

    public void setNetworkPolicies(NetworkPolicy[] policies) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        long token = Binder.clearCallingIdentity();
        try {
            maybeRefreshTrustedTime();
            synchronized (this.mRulesLock) {
                normalizePoliciesLocked(policies);
                updateNetworkEnabledLocked();
                updateNetworkRulesLocked();
                updateNotificationsLocked();
                writePolicyLocked();
            }
            Binder.restoreCallingIdentity(token);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    void addNetworkPolicyLocked(NetworkPolicy policy) {
        setNetworkPolicies((NetworkPolicy[]) ArrayUtils.appendElement(NetworkPolicy.class, getNetworkPolicies(this.mContext.getOpPackageName()), policy));
    }

    public NetworkPolicy[] getNetworkPolicies(String callingPackage) {
        NetworkPolicy[] policies;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        try {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE", TAG);
        } catch (SecurityException e) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.READ_PHONE_STATE", TAG);
            if (this.mAppOps.noteOp(51, Binder.getCallingUid(), callingPackage) != 0) {
                return new NetworkPolicy[0];
            }
        }
        synchronized (this.mRulesLock) {
            int size = this.mNetworkPolicy.size();
            policies = new NetworkPolicy[size];
            for (int i = 0; i < size; i++) {
                policies[i] = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
            }
        }
        return policies;
    }

    private void normalizePoliciesLocked() {
        normalizePoliciesLocked(getNetworkPolicies(this.mContext.getOpPackageName()));
    }

    private void normalizePoliciesLocked(NetworkPolicy[] policies) {
        String[] merged = TelephonyManager.from(this.mContext).getMergedSubscriberIds();
        this.mNetworkPolicy.clear();
        for (NetworkPolicy policy : policies) {
            policy.template = NetworkTemplate.normalize(policy.template, merged);
            NetworkPolicy existing = (NetworkPolicy) this.mNetworkPolicy.get(policy.template);
            if (existing == null || existing.compareTo(policy) > 0) {
                if (existing != null) {
                    Slog.d(TAG, "Normalization replaced " + existing + " with " + policy);
                }
                this.mNetworkPolicy.put(policy.template, policy);
            }
        }
    }

    public void snoozeLimit(NetworkTemplate template) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        long token = Binder.clearCallingIdentity();
        try {
            performSnooze(template, 2);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    void performSnooze(NetworkTemplate template, int type) {
        maybeRefreshTrustedTime();
        long currentTime = currentTimeMillis();
        synchronized (this.mRulesLock) {
            NetworkPolicy policy = (NetworkPolicy) this.mNetworkPolicy.get(template);
            if (policy == null) {
                throw new IllegalArgumentException("unable to find policy for " + template);
            }
            switch (type) {
                case 1:
                    policy.lastWarningSnooze = currentTime;
                    break;
                case 2:
                    policy.lastLimitSnooze = currentTime;
                    break;
                default:
                    throw new IllegalArgumentException("unexpected type");
            }
            normalizePoliciesLocked();
            updateNetworkEnabledLocked();
            updateNetworkRulesLocked();
            updateNotificationsLocked();
            writePolicyLocked();
        }
    }

    public void onTetheringChanged(String iface, boolean tethering) {
        Log.d(TAG, "onTetherStateChanged(" + iface + ", " + tethering + ")");
        synchronized (this.mRulesLock) {
            if (this.mRestrictBackground && tethering) {
                Log.d(TAG, "Tethering on (" + iface + "); disable Data Saver");
                setRestrictBackground(false);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setRestrictBackground(boolean restrictBackground) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        long token = Binder.clearCallingIdentity();
        try {
            maybeRefreshTrustedTime();
            synchronized (this.mRulesLock) {
                if (restrictBackground == this.mRestrictBackground) {
                    Slog.w(TAG, "setRestrictBackground: already " + restrictBackground);
                    Binder.restoreCallingIdentity(token);
                    return;
                }
                setRestrictBackgroundLocked(restrictBackground);
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void setRestrictBackgroundLocked(boolean restrictBackground) {
        Slog.d(TAG, "setRestrictBackgroundLocked(): " + restrictBackground);
        boolean oldRestrictBackground = this.mRestrictBackground;
        this.mRestrictBackground = restrictBackground;
        updateRulesForRestrictBackgroundLocked();
        try {
            if (!this.mNetworkManager.setDataSaverModeEnabled(this.mRestrictBackground)) {
                Slog.e(TAG, "Could not change Data Saver Mode on NMS to " + this.mRestrictBackground);
                this.mRestrictBackground = oldRestrictBackground;
                return;
            }
        } catch (RemoteException e) {
        }
        updateNotificationsLocked();
        writePolicyLocked();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addRestrictBackgroundWhitelistedUid(int uid) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mRulesLock) {
            boolean oldStatus = this.mRestrictBackgroundWhitelistUids.get(uid);
            if (oldStatus) {
                Slog.d(TAG, "uid " + uid + " is already whitelisted");
                return;
            }
            boolean needFirewallRules = isUidValidForWhitelistRules(uid);
            Slog.i(TAG, "adding uid " + uid + " to restrict background whitelist");
            this.mRestrictBackgroundWhitelistUids.append(uid, true);
            if (this.mDefaultRestrictBackgroundWhitelistUids.get(uid) && this.mRestrictBackgroundWhitelistRevokedUids.get(uid)) {
                Slog.d(TAG, "Removing uid " + uid + " from revoked restrict background whitelist");
                this.mRestrictBackgroundWhitelistRevokedUids.delete(uid);
            }
            if (needFirewallRules) {
                updateRulesForDataUsageRestrictionsLocked(uid);
            }
            writePolicyLocked();
        }
    }

    public void removeRestrictBackgroundWhitelistedUid(int uid) {
        int i = 1;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mRulesLock) {
            boolean changed = removeRestrictBackgroundWhitelistedUidLocked(uid, false, true);
        }
        Handler handler = this.mHandler;
        if (!changed) {
            i = 0;
        }
        handler.obtainMessage(9, uid, i, Boolean.FALSE).sendToTarget();
    }

    private boolean removeRestrictBackgroundWhitelistedUidLocked(int uid, boolean uidDeleted, boolean updateNow) {
        boolean oldStatus = this.mRestrictBackgroundWhitelistUids.get(uid);
        if (oldStatus || uidDeleted) {
            boolean isUidValidForWhitelistRules = !uidDeleted ? isUidValidForWhitelistRules(uid) : true;
            if (oldStatus) {
                Slog.i(TAG, "removing uid " + uid + " from restrict background whitelist");
                this.mRestrictBackgroundWhitelistUids.delete(uid);
            }
            if (this.mDefaultRestrictBackgroundWhitelistUids.get(uid) && !this.mRestrictBackgroundWhitelistRevokedUids.get(uid)) {
                Slog.d(TAG, "Adding uid " + uid + " to revoked restrict background whitelist");
                this.mRestrictBackgroundWhitelistRevokedUids.append(uid, true);
            }
            if (isUidValidForWhitelistRules) {
                updateRulesForDataUsageRestrictionsLocked(uid, uidDeleted);
            }
            if (updateNow) {
                writePolicyLocked();
            }
            if (!this.mRestrictBackground) {
                isUidValidForWhitelistRules = false;
            }
            return isUidValidForWhitelistRules;
        }
        Slog.d(TAG, "uid " + uid + " was not whitelisted before");
        return false;
    }

    public int[] getRestrictBackgroundWhitelistedUids() {
        int[] whitelist;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mRulesLock) {
            int size = this.mRestrictBackgroundWhitelistUids.size();
            whitelist = new int[size];
            for (int i = 0; i < size; i++) {
                whitelist[i] = this.mRestrictBackgroundWhitelistUids.keyAt(i);
            }
        }
        return whitelist;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getRestrictBackgroundByCaller() {
        int i = 3;
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", TAG);
        int uid = Binder.getCallingUid();
        synchronized (this.mRulesLock) {
            long token = Binder.clearCallingIdentity();
            try {
                int policy = getUidPolicy(uid);
                if (policy == 1) {
                    return 3;
                } else if (!this.mRestrictBackground) {
                    return 1;
                } else if (this.mRestrictBackgroundWhitelistUids.get(uid)) {
                    i = 2;
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public boolean getRestrictBackground() {
        boolean z;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mRulesLock) {
            z = this.mRestrictBackground;
        }
        return z;
    }

    public void setDeviceIdleMode(boolean enabled) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mRulesLock) {
            if (this.mDeviceIdleMode != enabled) {
                this.mDeviceIdleMode = enabled;
                if (this.mSystemReady) {
                    updateRulesForGlobalChangeLocked(false);
                }
                if (enabled) {
                    EventLogTags.writeDeviceIdleOnPhase("net");
                } else {
                    EventLogTags.writeDeviceIdleOffPhase("net");
                }
            }
        }
    }

    private NetworkPolicy findPolicyForNetworkLocked(NetworkIdentity ident) {
        for (int i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
            NetworkPolicy policy = (NetworkPolicy) this.mNetworkPolicy.valueAt(i);
            if (policy.template.matches(ident)) {
                return policy;
            }
        }
        return null;
    }

    public NetworkQuotaInfo getNetworkQuotaInfo(NetworkState state) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE", TAG);
        long token = Binder.clearCallingIdentity();
        try {
            NetworkQuotaInfo networkQuotaInfoUnchecked = getNetworkQuotaInfoUnchecked(state);
            return networkQuotaInfoUnchecked;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private NetworkQuotaInfo getNetworkQuotaInfoUnchecked(NetworkState state) {
        NetworkIdentity ident = NetworkIdentity.buildNetworkIdentity(this.mContext, state);
        synchronized (this.mRulesLock) {
            NetworkPolicy policy = findPolicyForNetworkLocked(ident);
        }
        if (policy == null || !policy.hasCycle()) {
            return null;
        }
        long softLimitBytes;
        long hardLimitBytes;
        long currentTime = currentTimeMillis();
        long end = currentTime;
        long totalBytes = getTotalBytes(policy.template, NetworkPolicyManager.computeLastCycleBoundary(currentTime, policy), currentTime);
        if (policy.warningBytes != -1) {
            softLimitBytes = policy.warningBytes;
        } else {
            softLimitBytes = -1;
        }
        if (policy.limitBytes != -1) {
            hardLimitBytes = policy.limitBytes;
        } else {
            hardLimitBytes = -1;
        }
        return new NetworkQuotaInfo(totalBytes, softLimitBytes, hardLimitBytes);
    }

    public boolean isNetworkMetered(NetworkState state) {
        if (state.networkInfo == null) {
            return false;
        }
        NetworkIdentity ident = NetworkIdentity.buildNetworkIdentity(this.mContext, state);
        if (ident.getRoaming()) {
            return true;
        }
        synchronized (this.mRulesLock) {
            NetworkPolicy policy = findPolicyForNetworkLocked(ident);
        }
        if (policy != null) {
            return policy.metered;
        }
        int type = state.networkInfo.getType();
        return ConnectivityManager.isNetworkTypeMobile(type) || type == 6;
    }

    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        IndentingPrintWriter fout = new IndentingPrintWriter(writer, "  ");
        ArraySet<String> argSet = new ArraySet(args.length);
        for (String arg : args) {
            argSet.add(arg);
        }
        synchronized (this.mRulesLock) {
            int i;
            if (argSet.contains("--unsnooze")) {
                for (i = this.mNetworkPolicy.size() - 1; i >= 0; i--) {
                    ((NetworkPolicy) this.mNetworkPolicy.valueAt(i)).clearSnooze();
                }
                normalizePoliciesLocked();
                updateNetworkEnabledLocked();
                updateNetworkRulesLocked();
                updateNotificationsLocked();
                writePolicyLocked();
                fout.println("Cleared snooze timestamps");
                return;
            }
            fout.print("System ready: ");
            fout.println(this.mSystemReady);
            fout.print("Restrict background: ");
            fout.println(this.mRestrictBackground);
            fout.print("Restrict power: ");
            fout.println(this.mRestrictPower);
            fout.print("Device idle: ");
            fout.println(this.mDeviceIdleMode);
            fout.println("Network policies:");
            fout.increaseIndent();
            for (i = 0; i < this.mNetworkPolicy.size(); i++) {
                fout.println(((NetworkPolicy) this.mNetworkPolicy.valueAt(i)).toString());
            }
            fout.decreaseIndent();
            fout.print("Metered ifaces: ");
            fout.println(String.valueOf(this.mMeteredIfaces));
            fout.println("Policy for UIDs:");
            fout.increaseIndent();
            int size = this.mUidPolicy.size();
            for (i = 0; i < size; i++) {
                int uid = this.mUidPolicy.keyAt(i);
                int policy = this.mUidPolicy.valueAt(i);
                fout.print("UID=");
                fout.print(uid);
                fout.print(" policy=");
                fout.print(DebugUtils.flagsToString(NetworkPolicyManager.class, "POLICY_", policy));
                fout.println();
            }
            fout.decreaseIndent();
            size = this.mPowerSaveWhitelistExceptIdleAppIds.size();
            if (size > 0) {
                fout.println("Power save whitelist (except idle) app ids:");
                fout.increaseIndent();
                for (i = 0; i < size; i++) {
                    fout.print("UID=");
                    fout.print(this.mPowerSaveWhitelistExceptIdleAppIds.keyAt(i));
                    fout.print(": ");
                    fout.print(this.mPowerSaveWhitelistExceptIdleAppIds.valueAt(i));
                    fout.println();
                }
                fout.decreaseIndent();
            }
            size = this.mPowerSaveWhitelistAppIds.size();
            if (size > 0) {
                fout.println("Power save whitelist app ids:");
                fout.increaseIndent();
                for (i = 0; i < size; i++) {
                    fout.print("UID=");
                    fout.print(this.mPowerSaveWhitelistAppIds.keyAt(i));
                    fout.print(": ");
                    fout.print(this.mPowerSaveWhitelistAppIds.valueAt(i));
                    fout.println();
                }
                fout.decreaseIndent();
            }
            size = this.mRestrictBackgroundWhitelistUids.size();
            if (size > 0) {
                fout.println("Restrict background whitelist uids:");
                fout.increaseIndent();
                for (i = 0; i < size; i++) {
                    fout.print("UID=");
                    fout.print(this.mRestrictBackgroundWhitelistUids.keyAt(i));
                    fout.println();
                }
                fout.decreaseIndent();
            }
            size = this.mDefaultRestrictBackgroundWhitelistUids.size();
            if (size > 0) {
                fout.println("Default restrict background whitelist uids:");
                fout.increaseIndent();
                for (i = 0; i < size; i++) {
                    fout.print("UID=");
                    fout.print(this.mDefaultRestrictBackgroundWhitelistUids.keyAt(i));
                    fout.println();
                }
                fout.decreaseIndent();
            }
            size = this.mRestrictBackgroundWhitelistRevokedUids.size();
            if (size > 0) {
                fout.println("Default restrict background whitelist uids revoked by users:");
                fout.increaseIndent();
                for (i = 0; i < size; i++) {
                    fout.print("UID=");
                    fout.print(this.mRestrictBackgroundWhitelistRevokedUids.keyAt(i));
                    fout.println();
                }
                fout.decreaseIndent();
            }
            SparseBooleanArray knownUids = new SparseBooleanArray();
            collectKeys(this.mUidState, knownUids);
            collectKeys(this.mUidRules, knownUids);
            fout.println("Status for all known UIDs:");
            fout.increaseIndent();
            size = knownUids.size();
            for (i = 0; i < size; i++) {
                uid = knownUids.keyAt(i);
                fout.print("UID=");
                fout.print(uid);
                int state = this.mUidState.get(uid, 16);
                fout.print(" state=");
                fout.print(state);
                if (state <= 2) {
                    fout.print(" (fg)");
                } else {
                    fout.print(state <= 4 ? " (fg svc)" : " (bg)");
                }
                int uidRules = this.mUidRules.get(uid, 0);
                fout.print(" rules=");
                fout.print(NetworkPolicyManager.uidRulesToString(uidRules));
                fout.println();
            }
            fout.decreaseIndent();
            fout.println("Status for just UIDs with rules:");
            fout.increaseIndent();
            size = this.mUidRules.size();
            for (i = 0; i < size; i++) {
                uid = this.mUidRules.keyAt(i);
                fout.print("UID=");
                fout.print(uid);
                uidRules = this.mUidRules.get(uid, 0);
                fout.print(" rules=");
                fout.print(NetworkPolicyManager.uidRulesToString(uidRules));
                fout.println();
            }
            fout.decreaseIndent();
        }
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) throws RemoteException {
        new NetworkPolicyManagerShellCommand(this.mContext, this).exec(this, in, out, err, args, resultReceiver);
    }

    public boolean isUidForeground(int uid) {
        boolean isUidForegroundLocked;
        this.mContext.enforceCallingOrSelfPermission("android.permission.MANAGE_NETWORK_POLICY", TAG);
        synchronized (this.mRulesLock) {
            isUidForegroundLocked = isUidForegroundLocked(uid);
        }
        return isUidForegroundLocked;
    }

    private boolean isUidForegroundLocked(int uid) {
        return isUidStateForegroundLocked(this.mUidState.get(uid, 16));
    }

    private boolean isUidForegroundOnRestrictBackgroundLocked(int uid) {
        return isProcStateAllowedWhileOnRestrictBackgroundLocked(this.mUidState.get(uid, 16));
    }

    private boolean isUidForegroundOnRestrictPowerLocked(int uid) {
        return isProcStateAllowedWhileIdleOrPowerSaveMode(this.mUidState.get(uid, 16));
    }

    private boolean isUidStateForegroundLocked(int state) {
        return this.mScreenOn && state <= 2;
    }

    private void updateUidStateLocked(int uid, int uidState) {
        int oldUidState = this.mUidState.get(uid, 16);
        if (oldUidState != uidState) {
            this.mUidState.put(uid, uidState);
            updateRestrictBackgroundRulesOnUidStatusChangedLocked(uid, oldUidState, uidState);
            if (isProcStateAllowedWhileIdleOrPowerSaveMode(oldUidState) != isProcStateAllowedWhileIdleOrPowerSaveMode(uidState)) {
                if (isUidIdle(uid)) {
                    updateRuleForAppIdleLocked(uid);
                }
                if (this.mDeviceIdleMode) {
                    updateRuleForDeviceIdleLocked(uid);
                }
                if (this.mRestrictPower) {
                    updateRuleForRestrictPowerLocked(uid);
                }
                updateRulesForPowerRestrictionsLocked(uid);
            }
            updateNetworkStats(uid, isUidStateForegroundLocked(uidState));
        }
    }

    private void removeUidStateLocked(int uid) {
        int index = this.mUidState.indexOfKey(uid);
        if (index >= 0) {
            int oldUidState = this.mUidState.valueAt(index);
            this.mUidState.removeAt(index);
            if (oldUidState != 16) {
                updateRestrictBackgroundRulesOnUidStatusChangedLocked(uid, oldUidState, 16);
                if (this.mDeviceIdleMode) {
                    updateRuleForDeviceIdleLocked(uid);
                }
                if (this.mRestrictPower) {
                    updateRuleForRestrictPowerLocked(uid);
                }
                updateRulesForPowerRestrictionsLocked(uid);
                updateNetworkStats(uid, false);
            }
        }
    }

    private void updateNetworkStats(int uid, boolean uidForeground) {
        try {
            this.mNetworkStats.setUidForeground(uid, uidForeground);
        } catch (RemoteException e) {
        }
    }

    private void updateRestrictBackgroundRulesOnUidStatusChangedLocked(int uid, int oldUidState, int newUidState) {
        if (isProcStateAllowedWhileOnRestrictBackgroundLocked(oldUidState) != isProcStateAllowedWhileOnRestrictBackgroundLocked(newUidState)) {
            updateRulesForDataUsageRestrictionsLocked(uid);
        }
    }

    private void updateScreenOn() {
        synchronized (this.mRulesLock) {
            try {
                this.mScreenOn = this.mPowerManager.isInteractive();
            } catch (RemoteException e) {
            }
            updateRulesForScreenLocked();
        }
    }

    private void updateRulesForScreenLocked() {
        int size = this.mUidState.size();
        for (int i = 0; i < size; i++) {
            if (this.mUidState.valueAt(i) <= 4) {
                updateRestrictionRulesForUidLocked(this.mUidState.keyAt(i));
            }
        }
    }

    static boolean isProcStateAllowedWhileIdleOrPowerSaveMode(int procState) {
        return procState <= 4;
    }

    static boolean isProcStateAllowedWhileOnRestrictBackgroundLocked(int procState) {
        return procState <= 4;
    }

    void updateRulesForRestrictPowerLocked() {
        updateRulesForWhitelistedPowerSaveLocked(this.mRestrictPower, 3, this.mUidFirewallPowerSaveRules);
    }

    void updateRuleForRestrictPowerLocked(int uid) {
        updateRulesForWhitelistedPowerSaveLocked(uid, this.mRestrictPower, 3);
    }

    void updateRulesForDeviceIdleLocked() {
        updateRulesForWhitelistedPowerSaveLocked(this.mDeviceIdleMode, 1, this.mUidFirewallDozableRules);
    }

    void updateRuleForDeviceIdleLocked(int uid) {
        updateRulesForWhitelistedPowerSaveLocked(uid, this.mDeviceIdleMode, 1);
    }

    private void updateRulesForWhitelistedPowerSaveLocked(boolean enabled, int chain, SparseIntArray rules) {
        if (enabled) {
            int i;
            SparseIntArray uidRules = rules;
            rules.clear();
            List<UserInfo> users = this.mUserManager.getUsers();
            for (int ui = users.size() - 1; ui >= 0; ui--) {
                UserInfo user = (UserInfo) users.get(ui);
                for (i = this.mPowerSaveTempWhitelistAppIds.size() - 1; i >= 0; i--) {
                    if (this.mPowerSaveTempWhitelistAppIds.valueAt(i)) {
                        rules.put(UserHandle.getUid(user.id, this.mPowerSaveTempWhitelistAppIds.keyAt(i)), 1);
                    }
                }
                for (i = this.mPowerSaveWhitelistAppIds.size() - 1; i >= 0; i--) {
                    rules.put(UserHandle.getUid(user.id, this.mPowerSaveWhitelistAppIds.keyAt(i)), 1);
                }
            }
            for (i = this.mUidState.size() - 1; i >= 0; i--) {
                if (isProcStateAllowedWhileIdleOrPowerSaveMode(this.mUidState.valueAt(i))) {
                    rules.put(this.mUidState.keyAt(i), 1);
                }
            }
            setUidFirewallRules(chain, rules);
        }
        enableFirewallChainLocked(chain, enabled);
    }

    private void updateRulesForNonMeteredNetworksLocked() {
    }

    private boolean isWhitelistedBatterySaverLocked(int uid) {
        int appId = UserHandle.getAppId(uid);
        return !this.mPowerSaveTempWhitelistAppIds.get(appId) ? this.mPowerSaveWhitelistAppIds.get(appId) : true;
    }

    private void updateRulesForWhitelistedPowerSaveLocked(int uid, boolean enabled, int chain) {
        if (!enabled) {
            return;
        }
        if (isWhitelistedBatterySaverLocked(uid) || isProcStateAllowedWhileIdleOrPowerSaveMode(this.mUidState.get(uid))) {
            setUidFirewallRule(chain, uid, 1);
        } else {
            setUidFirewallRule(chain, uid, 0);
        }
    }

    void updateRulesForAppIdleLocked() {
        SparseIntArray uidRules = this.mUidFirewallStandbyRules;
        uidRules.clear();
        List<UserInfo> users = this.mUserManager.getUsers();
        for (int ui = users.size() - 1; ui >= 0; ui--) {
            for (int uid : this.mUsageStats.getIdleUidsForUser(((UserInfo) users.get(ui)).id)) {
                if (!this.mPowerSaveTempWhitelistAppIds.get(UserHandle.getAppId(uid), false) && hasInternetPermissions(uid)) {
                    uidRules.put(uid, 2);
                }
            }
        }
        setUidFirewallRules(2, uidRules);
    }

    void updateRuleForAppIdleLocked(int uid) {
        if (isUidValidForBlacklistRules(uid)) {
            if (this.mPowerSaveTempWhitelistAppIds.get(UserHandle.getAppId(uid)) || !isUidIdle(uid) || isUidForegroundOnRestrictPowerLocked(uid)) {
                setUidFirewallRule(2, uid, 0);
            } else {
                setUidFirewallRule(2, uid, 2);
            }
        }
    }

    void updateRulesForAppIdleParoleLocked() {
        enableFirewallChainLocked(2, !this.mUsageStats.isAppIdleParoleOn());
    }

    private void updateRulesForGlobalChangeLocked(boolean restrictedNetworksChanged) {
        long start = System.currentTimeMillis();
        updateRulesForDeviceIdleLocked();
        updateRulesForAppIdleLocked();
        updateRulesForRestrictPowerLocked();
        updateRulesForRestrictBackgroundLocked();
        setRestrictBackgroundLocked(this.mRestrictBackground);
        if (restrictedNetworksChanged) {
            normalizePoliciesLocked();
            updateNetworkRulesLocked();
        }
        Slog.d(TAG, "updateRulesForGlobalChangeLocked(" + restrictedNetworksChanged + ") took " + (System.currentTimeMillis() - start) + "ms");
    }

    private void updateRulesForRestrictBackgroundLocked() {
        PackageManager pm = this.mContext.getPackageManager();
        List<UserInfo> users = this.mUserManager.getUsers();
        List<ApplicationInfo> apps = pm.getInstalledApplications(795136);
        int usersSize = users.size();
        int appsSize = apps.size();
        for (int i = 0; i < usersSize; i++) {
            UserInfo user = (UserInfo) users.get(i);
            for (int j = 0; j < appsSize; j++) {
                int uid = UserHandle.getUid(user.id, ((ApplicationInfo) apps.get(j)).uid);
                updateRulesForDataUsageRestrictionsLocked(uid);
                updateRulesForPowerRestrictionsLocked(uid);
            }
        }
    }

    private void updateRulesForTempWhitelistChangeLocked() {
        List<UserInfo> users = this.mUserManager.getUsers();
        for (int i = 0; i < users.size(); i++) {
            UserInfo user = (UserInfo) users.get(i);
            for (int j = this.mPowerSaveTempWhitelistAppIds.size() - 1; j >= 0; j--) {
                int uid = UserHandle.getUid(user.id, this.mPowerSaveTempWhitelistAppIds.keyAt(j));
                updateRuleForAppIdleLocked(uid);
                updateRuleForDeviceIdleLocked(uid);
                updateRuleForRestrictPowerLocked(uid);
                updateRulesForPowerRestrictionsLocked(uid);
            }
        }
    }

    private boolean isUidValidForBlacklistRules(int uid) {
        if (uid == 1013 || uid == 1019 || (UserHandle.isApp(uid) && hasInternetPermissions(uid))) {
            return true;
        }
        return false;
    }

    private boolean isUidValidForWhitelistRules(int uid) {
        return UserHandle.isApp(uid) ? hasInternetPermissions(uid) : false;
    }

    private boolean isUidIdle(int uid) {
        String[] packages = this.mContext.getPackageManager().getPackagesForUid(uid);
        int userId = UserHandle.getUserId(uid);
        if (!ArrayUtils.isEmpty(packages)) {
            for (String packageName : packages) {
                if (!this.mUsageStats.isAppIdle(packageName, uid, userId)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasInternetPermissions(int uid) {
        try {
            if (this.mIPm.checkUidPermission("android.permission.INTERNET", uid) != 0) {
                return false;
            }
        } catch (RemoteException e) {
        }
        return true;
    }

    private void updateRestrictionRulesForUidLocked(int uid) {
        updateRuleForDeviceIdleLocked(uid);
        updateRuleForAppIdleLocked(uid);
        updateRuleForRestrictPowerLocked(uid);
        updateRulesForPowerRestrictionsLocked(uid);
        updateRulesForDataUsageRestrictionsLocked(uid);
    }

    private void updateRulesForDataUsageRestrictionsLocked(int uid) {
        updateRulesForDataUsageRestrictionsLocked(uid, false);
    }

    private void updateRulesForDataUsageRestrictionsLocked(int uid, boolean uidDeleted) {
        if (uidDeleted || isUidValidForWhitelistRules(uid)) {
            int uidPolicy = this.mUidPolicy.get(uid, 0);
            int oldUidRules = this.mUidRules.get(uid, 0);
            boolean isForeground = isUidForegroundOnRestrictBackgroundLocked(uid);
            boolean isBlacklisted = (uidPolicy & 1) != 0;
            boolean isWhitelisted = this.mRestrictBackgroundWhitelistUids.get(uid);
            int oldRule = oldUidRules & 15;
            int newRule = 0;
            if (isForeground) {
                if (isBlacklisted || (this.mRestrictBackground && !isWhitelisted)) {
                    newRule = 2;
                } else if (isWhitelisted) {
                    newRule = 1;
                }
            } else if (isBlacklisted) {
                newRule = 4;
            } else if (this.mRestrictBackground && isWhitelisted) {
                newRule = 1;
            }
            int newUidRules = newRule | (oldUidRules & 240);
            if (newUidRules == 0) {
                this.mUidRules.delete(uid);
            } else {
                this.mUidRules.put(uid, newUidRules);
            }
            if (newRule != oldRule) {
                if ((newRule & 2) != 0) {
                    setMeteredNetworkWhitelist(uid, true);
                    if (isBlacklisted) {
                        setMeteredNetworkBlacklist(uid, false);
                    }
                } else if ((oldRule & 2) != 0) {
                    if (!isWhitelisted) {
                        setMeteredNetworkWhitelist(uid, false);
                    }
                    if (isBlacklisted) {
                        setMeteredNetworkBlacklist(uid, true);
                    }
                } else if ((newRule & 4) != 0 || (oldRule & 4) != 0) {
                    setMeteredNetworkBlacklist(uid, isBlacklisted);
                    if ((oldRule & 4) != 0 && isWhitelisted) {
                        setMeteredNetworkWhitelist(uid, isWhitelisted);
                    }
                } else if ((newRule & 1) == 0 && (oldRule & 1) == 0) {
                    Log.wtf(TAG, "Unexpected change of metered UID state for " + uid + ": foreground=" + isForeground + ", whitelisted=" + isWhitelisted + ", blacklisted=" + isBlacklisted + ", newRule=" + NetworkPolicyManager.uidRulesToString(newUidRules) + ", oldRule=" + NetworkPolicyManager.uidRulesToString(oldUidRules));
                } else {
                    setMeteredNetworkWhitelist(uid, isWhitelisted);
                }
                this.mHandler.obtainMessage(1, uid, newUidRules).sendToTarget();
            }
        }
    }

    private void updateRulesForPowerRestrictionsLocked(int uid) {
        if (isUidValidForBlacklistRules(uid)) {
            boolean restrictMode = (isUidIdle(uid) || this.mRestrictPower) ? true : this.mDeviceIdleMode;
            int uidPolicy = this.mUidPolicy.get(uid, 0);
            int oldUidRules = this.mUidRules.get(uid, 0);
            boolean isForeground = isUidForegroundOnRestrictPowerLocked(uid);
            boolean isWhitelisted = isWhitelistedBatterySaverLocked(uid);
            int oldRule = oldUidRules & 240;
            int newRule = 0;
            if (isForeground) {
                if (restrictMode) {
                    newRule = 32;
                }
            } else if (restrictMode) {
                newRule = isWhitelisted ? 32 : 64;
            }
            int newUidRules = (oldUidRules & 15) | newRule;
            if (newUidRules == 0) {
                this.mUidRules.delete(uid);
            } else {
                this.mUidRules.put(uid, newUidRules);
            }
            if (newRule != oldRule) {
                if (newRule != 0 && (newRule & 32) == 0 && (newRule & 64) == 0) {
                    Log.wtf(TAG, "Unexpected change of non-metered UID state for " + uid + ": foreground=" + isForeground + ", whitelisted=" + isWhitelisted + ", newRule=" + NetworkPolicyManager.uidRulesToString(newUidRules) + ", oldRule=" + NetworkPolicyManager.uidRulesToString(oldUidRules));
                }
                this.mHandler.obtainMessage(1, uid, newUidRules).sendToTarget();
            }
        }
    }

    private void dispatchUidRulesChanged(INetworkPolicyListener listener, int uid, int uidRules) {
        if (listener != null) {
            try {
                listener.onUidRulesChanged(uid, uidRules);
            } catch (RemoteException e) {
            }
        }
    }

    private void dispatchMeteredIfacesChanged(INetworkPolicyListener listener, String[] meteredIfaces) {
        if (listener != null) {
            try {
                listener.onMeteredIfacesChanged(meteredIfaces);
            } catch (RemoteException e) {
            }
        }
    }

    private void dispatchRestrictBackgroundChanged(INetworkPolicyListener listener, boolean restrictBackground) {
        if (listener != null) {
            try {
                listener.onRestrictBackgroundChanged(restrictBackground);
            } catch (RemoteException e) {
            }
        }
    }

    private void dispatchRestrictBackgroundWhitelistChanged(INetworkPolicyListener listener, int uid, boolean whitelisted) {
        if (listener != null) {
            try {
                listener.onRestrictBackgroundWhitelistChanged(uid, whitelisted);
            } catch (RemoteException e) {
            }
        }
    }

    private void dispatchRestrictBackgroundBlacklistChanged(INetworkPolicyListener listener, int uid, boolean blacklisted) {
        if (listener != null) {
            try {
                listener.onRestrictBackgroundBlacklistChanged(uid, blacklisted);
            } catch (RemoteException e) {
            }
        }
    }

    private void setInterfaceQuota(String iface, long quotaBytes) {
        try {
            this.mNetworkManager.setInterfaceQuota(iface, quotaBytes);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem setting interface quota", e);
        } catch (RemoteException e2) {
        }
    }

    private void removeInterfaceQuota(String iface) {
        try {
            this.mNetworkManager.removeInterfaceQuota(iface);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem removing interface quota", e);
        } catch (RemoteException e2) {
        }
    }

    private void setMeteredNetworkBlacklist(int uid, boolean enable) {
        try {
            this.mNetworkManager.setUidMeteredNetworkBlacklist(uid, enable);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem setting blacklist (" + enable + ") rules for " + uid, e);
        } catch (RemoteException e2) {
        }
    }

    private void setMeteredNetworkWhitelist(int uid, boolean enable) {
        try {
            this.mNetworkManager.setUidMeteredNetworkWhitelist(uid, enable);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem setting whitelist (" + enable + ") rules for " + uid, e);
        } catch (RemoteException e2) {
        }
    }

    private void setUidFirewallRules(int chain, SparseIntArray uidRules) {
        try {
            int size = uidRules.size();
            int[] uids = new int[size];
            int[] rules = new int[size];
            for (int index = size - 1; index >= 0; index--) {
                uids[index] = uidRules.keyAt(index);
                rules[index] = uidRules.valueAt(index);
            }
            this.mNetworkManager.setFirewallUidRules(chain, uids, rules);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem setting firewall uid rules", e);
        } catch (RemoteException e2) {
        }
    }

    private void setUidFirewallRule(int chain, int uid, int rule) {
        if (chain == 1) {
            this.mUidFirewallDozableRules.put(uid, rule);
        } else if (chain == 2) {
            this.mUidFirewallStandbyRules.put(uid, rule);
        } else if (chain == 3) {
            this.mUidFirewallPowerSaveRules.put(uid, rule);
        }
        try {
            this.mNetworkManager.setFirewallUidRule(chain, uid, rule);
        } catch (IllegalStateException e) {
            Log.wtf(TAG, "problem setting firewall uid rules", e);
        } catch (RemoteException e2) {
        }
    }

    private void enableFirewallChainLocked(int chain, boolean enable) {
        if (this.mFirewallChainStates.indexOfKey(chain) < 0 || this.mFirewallChainStates.get(chain) != enable) {
            this.mFirewallChainStates.put(chain, enable);
            try {
                this.mNetworkManager.setFirewallChainEnabled(chain, enable);
            } catch (IllegalStateException e) {
                Log.wtf(TAG, "problem enable firewall chain", e);
            } catch (RemoteException e2) {
            }
        }
    }

    private long getTotalBytes(NetworkTemplate template, long start, long end) {
        try {
            return this.mNetworkStats.getNetworkTotalBytes(template, start, end);
        } catch (RuntimeException e) {
            Slog.w(TAG, "problem reading network stats: " + e);
            return 0;
        } catch (RemoteException e2) {
            return 0;
        }
    }

    private boolean isBandwidthControlEnabled() {
        long token = Binder.clearCallingIdentity();
        boolean isBandwidthControlEnabled;
        try {
            isBandwidthControlEnabled = this.mNetworkManager.isBandwidthControlEnabled();
            return isBandwidthControlEnabled;
        } catch (RemoteException e) {
            isBandwidthControlEnabled = false;
            return isBandwidthControlEnabled;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    void maybeRefreshTrustedTime() {
        if (getTimeRefreshElapsedRealtime() > 86400000) {
            this.mTimeRefreshRealtime = SystemClock.elapsedRealtime();
            new Thread() {
                public void run() {
                    NetworkPolicyManagerService.this.mTime.forceRefresh();
                }
            }.start();
        }
    }

    private long currentTimeMillis() {
        return this.mTime.hasCache() ? this.mTime.currentTimeMillis() : System.currentTimeMillis();
    }

    private static Intent buildAllowBackgroundDataIntent() {
        return new Intent(ACTION_ALLOW_BACKGROUND);
    }

    private static Intent buildNetworkOverLimitIntent(NetworkTemplate template) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.systemui", "com.android.systemui.net.NetworkOverLimitActivity"));
        intent.addFlags(268435456);
        intent.putExtra("android.net.NETWORK_TEMPLATE", template);
        return intent;
    }

    private static Intent buildViewDataUsageIntent(NetworkTemplate template) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
        intent.addFlags(268435456);
        intent.putExtra("android.net.NETWORK_TEMPLATE", template);
        return intent;
    }

    public void addIdleHandler(IdleHandler handler) {
        this.mHandler.getLooper().getQueue().addIdleHandler(handler);
    }

    private static void collectKeys(SparseIntArray source, SparseBooleanArray target) {
        int size = source.size();
        for (int i = 0; i < size; i++) {
            target.put(source.keyAt(i), true);
        }
    }

    public void factoryReset(String subscriber) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        if (!this.mUserManager.hasUserRestriction("no_network_reset")) {
            NetworkPolicy[] policies = getNetworkPolicies(this.mContext.getOpPackageName());
            NetworkTemplate template = NetworkTemplate.buildTemplateMobileAll(subscriber);
            for (NetworkPolicy policy : policies) {
                if (policy.template.equals(template)) {
                    policy.limitBytes = -1;
                    policy.inferred = false;
                    policy.clearSnooze();
                }
            }
            setNetworkPolicies(policies);
            setRestrictBackground(false);
            if (!this.mUserManager.hasUserRestriction("no_control_apps")) {
                for (int uid : getUidsWithPolicy(1)) {
                    setUidPolicy(uid, 0);
                }
            }
        }
    }

    public long getTimeRefreshElapsedRealtime() {
        if (this.mTimeRefreshRealtime != -1) {
            return SystemClock.elapsedRealtime() - this.mTimeRefreshRealtime;
        }
        return JobStatus.NO_LATEST_RUNTIME;
    }
}
