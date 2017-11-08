package com.android.server.notification;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerInternal;
import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.AutomaticZenRule;
import android.app.IActivityManager;
import android.app.INotificationManager.Stub;
import android.app.ITransientNotification;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.NotificationManager.Policy;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.backup.BackupManager;
import android.app.usage.UsageStatsManagerInternal;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ParceledListSlice;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioManagerInternal;
import android.media.AudioSystem;
import android.media.IRingtonePlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.rms.HwSysResource;
import android.service.notification.Adjustment;
import android.service.notification.Condition;
import android.service.notification.IConditionProvider;
import android.service.notification.INotificationListener;
import android.service.notification.IStatusBarNotificationHolder;
import android.service.notification.NotificationRankingUpdate;
import android.service.notification.StatusBarNotification;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.ZenRule;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.RemoteViews;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.Preconditions;
import com.android.server.AbsLocationManagerService;
import com.android.server.DeviceIdleController.LocalService;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.audio.AudioService;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.notification.ManagedServices.Config;
import com.android.server.notification.ManagedServices.ManagedServiceInfo;
import com.android.server.notification.ManagedServices.UserProfiles;
import com.android.server.notification.ZenModeHelper.Callback;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.vr.VrManagerInternal;
import com.huawei.pgmng.log.LogPower;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import libcore.io.IoUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class NotificationManagerService extends AbsNotificationManager {
    private static final String ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE = "huawei.intent.action.HWSYSTEMMANAGER_CHANGE_POWERMODE";
    private static final String ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE = "huawei.intent.action.HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE";
    private static final String ATTR_VERSION = "version";
    private static final int CLOSE_SAVE_POWER = 0;
    static final boolean DBG = Log.isLoggable(TAG, 3);
    private static final int DB_VERSION = 1;
    static final float DEFAULT_MAX_NOTIFICATION_ENQUEUE_RATE = 50.0f;
    static final int DEFAULT_STREAM_TYPE = 5;
    static final long[] DEFAULT_VIBRATE_PATTERN = new long[]{0, 250, 250, 250};
    static final boolean ENABLE_BLOCKED_NOTIFICATIONS = true;
    static final boolean ENABLE_BLOCKED_TOASTS = true;
    public static final boolean ENABLE_CHILD_NOTIFICATIONS = SystemProperties.getBoolean("debug.child_notifs", true);
    private static final int EVENTLOG_ENQUEUE_STATUS_IGNORED = 2;
    private static final int EVENTLOG_ENQUEUE_STATUS_NEW = 0;
    private static final int EVENTLOG_ENQUEUE_STATUS_UPDATE = 1;
    static final int LONG_DELAY = 3500;
    static final int MATCHES_CALL_FILTER_CONTACTS_TIMEOUT_MS = 3000;
    static final float MATCHES_CALL_FILTER_TIMEOUT_AFFINITY = 1.0f;
    static final int MAX_PACKAGE_NOTIFICATIONS = 50;
    static final int MESSAGE_LISTENER_HINTS_CHANGED = 5;
    static final int MESSAGE_LISTENER_NOTIFICATION_FILTER_CHANGED = 6;
    private static final int MESSAGE_RANKING_SORT = 1001;
    private static final int MESSAGE_RECONSIDER_RANKING = 1000;
    static final int MESSAGE_SAVE_POLICY_FILE = 3;
    static final int MESSAGE_SEND_RANKING_UPDATE = 4;
    static final int MESSAGE_TIMEOUT = 2;
    private static final long MIN_PACKAGE_OVERRATE_LOG_INTERVAL = 5000;
    private static final int MY_PID = Process.myPid();
    private static final int MY_UID = Process.myUid();
    private static final int OPEN_SAVE_POWER = 3;
    private static final String PERMISSION = "com.huawei.android.launcher.permission.CHANGE_POWERMODE";
    private static final String POWER_MODE = "power_mode";
    private static final String POWER_SAVER_NOTIFICATION_WHITELIST = "super_power_save_notification_whitelist";
    static final int SHORT_DELAY = 2000;
    private static final String SHUTDOWN_LIMIT_POWERMODE = "shutdomn_limit_powermode";
    static final String TAG = "NotificationService";
    private static final String TAG_NOTIFICATION_POLICY = "notification-policy";
    static final int VIBRATE_PATTERN_MAXLEN = 17;
    private IActivityManager mAm;
    private AppOpsManager mAppOps;
    private UsageStatsManagerInternal mAppUsageStats;
    private Archive mArchive;
    Light mAttentionLight;
    AudioManager mAudioManager;
    AudioManagerInternal mAudioManagerInternal;
    final ArrayMap<Integer, ArrayMap<String, String>> mAutobundledSummaries = new ArrayMap();
    private final Runnable mBuzzBeepBlinked = new Runnable() {
        public void run() {
            if (NotificationManagerService.this.mStatusBar != null) {
                NotificationManagerService.this.mStatusBar.buzzBeepBlinked();
            }
        }
    };
    private int mCallState;
    private ConditionProviders mConditionProviders;
    private int mDefaultNotificationColor;
    private int mDefaultNotificationLedOff;
    private int mDefaultNotificationLedOn;
    private long[] mDefaultVibrationPattern;
    private boolean mDisableNotificationEffects;
    private List<ComponentName> mEffectsSuppressors = new ArrayList();
    private long[] mFallbackVibrationPattern;
    final IBinder mForegroundToken = new Binder();
    private Handler mHandler;
    private boolean mInCall = false;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_ON")) {
                NotificationManagerService.this.mScreenOn = true;
                Flog.i(400, "mIntentReceiver-ACTION_SCREEN_ON");
                NotificationManagerService.this.updateNotificationPulse();
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                NotificationManagerService.this.mScreenOn = false;
                Flog.i(400, "mIntentReceiver-ACTION_SCREEN_OFF");
                NotificationManagerService.this.updateNotificationPulse();
            } else if (action.equals("android.intent.action.PHONE_STATE")) {
                NotificationManagerService.this.mInCall = TelephonyManager.EXTRA_STATE_OFFHOOK.equals(intent.getStringExtra(AudioService.CONNECT_INTENT_KEY_STATE));
                Flog.i(400, "mIntentReceiver-ACTION_PHONE_STATE_CHANGED");
                NotificationManagerService.this.updateNotificationPulse();
            } else if (action.equals("android.intent.action.USER_STOPPED")) {
                Flog.i(400, "mIntentReceiver-ACTION_USER_STOPPED");
                userHandle = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (userHandle >= 0) {
                    NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, null, 0, 0, true, userHandle, 6, null);
                }
            } else if (action.equals("android.intent.action.MANAGED_PROFILE_UNAVAILABLE")) {
                userHandle = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (userHandle >= 0) {
                    NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, null, 0, 0, true, userHandle, 15, null);
                }
            } else if (action.equals("android.intent.action.USER_PRESENT")) {
                if (NotificationManagerService.this.mScreenOn) {
                    NotificationManagerService.this.mNotificationLight.turnOff();
                    if (NotificationManagerService.this.mStatusBar != null) {
                        NotificationManagerService.this.mStatusBar.notificationLightOff();
                    }
                    Flog.i(1100, "turn off notificationLight due to Receiver-ACTION_USER_PRESENT");
                    NotificationManagerService.this.updateLight(false, 0, 0);
                }
            } else if (action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
                if (!NotificationManagerService.this.mScreenOn) {
                    NotificationManagerService.this.mNotificationLight.turnOff();
                    NotificationManagerService.this.updateNotificationPulse();
                    Flog.i(1100, "turn off notificationLight due to Receiver-ACTION_POWER_DISCONNECTED");
                    NotificationManagerService.this.updateLight(false, 0, 0);
                }
            } else if (action.equals("android.intent.action.USER_SWITCHED")) {
                user = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                Flog.i(400, "mIntentReceiver-ACTION_USER_SWITCHED");
                NotificationManagerService.this.mSettingsObserver.update(null);
                NotificationManagerService.this.mUserProfiles.updateCache(context);
                NotificationManagerService.this.mConditionProviders.onUserSwitched(user);
                NotificationManagerService.this.mListeners.onUserSwitched(user);
                NotificationManagerService.this.mRankerServices.onUserSwitched(user);
                NotificationManagerService.this.mZenModeHelper.onUserSwitched(user);
                NotificationManagerService.this.handleUserSwitchEvents(user);
                NotificationManagerService.this.stopPlaySound();
            } else if (action.equals("android.intent.action.USER_ADDED")) {
                Flog.i(400, "mIntentReceiver-ACTION_USER_ADDED");
                NotificationManagerService.this.mUserProfiles.updateCache(context);
            } else if (action.equals("android.intent.action.USER_REMOVED")) {
                NotificationManagerService.this.mZenModeHelper.onUserRemoved(intent.getIntExtra("android.intent.extra.user_handle", -10000));
            } else if (action.equals("android.intent.action.USER_UNLOCKED")) {
                user = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                NotificationManagerService.this.mConditionProviders.onUserUnlocked(user);
                NotificationManagerService.this.mListeners.onUserUnlocked(user);
                NotificationManagerService.this.mRankerServices.onUserUnlocked(user);
                NotificationManagerService.this.mZenModeHelper.onUserUnlocked(user);
            }
        }
    };
    private final NotificationManagerInternal mInternalService = new NotificationManagerInternal() {
        public void enqueueNotification(String pkg, String opPkg, int callingUid, int callingPid, String tag, int id, Notification notification, int[] idReceived, int userId) {
            NotificationManagerService.this.enqueueNotificationInternal(pkg, opPkg, callingUid, callingPid, tag, id, notification, idReceived, userId);
        }

        public void removeForegroundServiceFlagFromNotification(String pkg, int notificationId, int userId) {
            NotificationManagerService.checkCallerIsSystem();
            synchronized (NotificationManagerService.this.mNotificationList) {
                int i = NotificationManagerService.this.indexOfNotificationLocked(pkg, null, notificationId, userId);
                if (i < 0) {
                    Log.d(NotificationManagerService.TAG, "stripForegroundServiceFlag: Could not find notification with pkg=" + pkg + " / id=" + notificationId + " / userId=" + userId);
                    return;
                }
                NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationList.get(i);
                StatusBarNotification sbn = r.sbn;
                sbn.getNotification().flags = r.mOriginalFlags & -65;
                NotificationManagerService.this.mRankingHelper.sort(NotificationManagerService.this.mNotificationList);
                NotificationManagerService.this.mListeners.notifyPostedLocked(sbn, sbn);
            }
        }
    };
    private int mInterruptionFilter = 0;
    private long mLastOverRateLogTime;
    ArrayList<String> mLights = new ArrayList();
    private int mListenerHints;
    private NotificationListeners mListeners;
    private final SparseArray<ArraySet<ManagedServiceInfo>> mListenersDisablingEffects = new SparseArray();
    private float mMaxPackageEnqueueRate = DEFAULT_MAX_NOTIFICATION_ENQUEUE_RATE;
    private final NotificationDelegate mNotificationDelegate = new NotificationDelegate() {
        public void onSetDisabled(int status) {
            boolean z = false;
            synchronized (NotificationManagerService.this.mNotificationList) {
                NotificationManagerService notificationManagerService = NotificationManagerService.this;
                if ((DumpState.DUMP_DOMAIN_PREFERRED & status) != 0) {
                    z = true;
                }
                notificationManagerService.mDisableNotificationEffects = z;
                if (NotificationManagerService.this.disableNotificationEffects(null) != null) {
                    long identity = Binder.clearCallingIdentity();
                    try {
                        IRingtonePlayer player = NotificationManagerService.this.mAudioManager.getRingtonePlayer();
                        if (player != null) {
                            player.stopAsync();
                        }
                    } catch (RemoteException e) {
                        identity = Binder.clearCallingIdentity();
                        NotificationManagerService.this.mVibrator.cancel();
                    } catch (Throwable th) {
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                    identity = Binder.clearCallingIdentity();
                    NotificationManagerService.this.mVibrator.cancel();
                }
            }
        }

        public void onClearAll(int callingUid, int callingPid, int userId) {
            synchronized (NotificationManagerService.this.mNotificationList) {
                NotificationManagerService.this.cancelAllLocked(callingUid, callingPid, userId, 3, null, true);
            }
        }

        public void onNotificationClick(int callingUid, int callingPid, String key) {
            synchronized (NotificationManagerService.this.mNotificationList) {
                Flog.i(400, "onNotificationClick called");
                NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r == null) {
                    Log.w(NotificationManagerService.TAG, "No notification with key: " + key);
                    return;
                }
                long now = System.currentTimeMillis();
                EventLogTags.writeNotificationClicked(key, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now));
                StatusBarNotification sbn = r.sbn;
                NotificationManagerService.this.cancelNotification(callingUid, callingPid, sbn.getPackageName(), sbn.getTag(), sbn.getId(), 16, 64, false, r.getUserId(), 1, null);
            }
        }

        public void onNotificationActionClick(int callingUid, int callingPid, String key, int actionIndex) {
            synchronized (NotificationManagerService.this.mNotificationList) {
                NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r == null) {
                    Log.w(NotificationManagerService.TAG, "No notification with key: " + key);
                    return;
                }
                long now = System.currentTimeMillis();
                EventLogTags.writeNotificationActionClicked(key, actionIndex, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now));
            }
        }

        public void onNotificationClear(int callingUid, int callingPid, String pkg, String tag, int id, int userId) {
            NotificationManagerService.this.cancelNotification(callingUid, callingPid, pkg, tag, id, 0, 66, true, userId, 2, null);
        }

        public void onPanelRevealed(boolean clearEffects, int items) {
            EventLogTags.writeNotificationPanelRevealed(items);
            if (clearEffects) {
                clearEffects();
            }
        }

        public void onPanelHidden() {
            EventLogTags.writeNotificationPanelHidden();
        }

        public void clearEffects() {
            synchronized (NotificationManagerService.this.mNotificationList) {
                if (NotificationManagerService.DBG) {
                    Slog.d(NotificationManagerService.TAG, "clearEffects");
                }
                NotificationManagerService.this.clearSoundLocked();
                NotificationManagerService.this.clearVibrateLocked();
                NotificationManagerService.this.clearLightsLocked();
            }
        }

        public void onNotificationError(int callingUid, int callingPid, String pkg, String tag, int id, int uid, int initialPid, String message, int userId) {
            Slog.d(NotificationManagerService.TAG, "onNotification error pkg=" + pkg + " tag=" + tag + " id=" + id + "; will crashApplication(uid=" + uid + ", pid=" + initialPid + ")");
            NotificationManagerService.this.cancelNotification(callingUid, callingPid, pkg, tag, id, 0, 0, false, userId, 4, null);
            long ident = Binder.clearCallingIdentity();
            try {
                ActivityManagerNative.getDefault().crashApplication(uid, initialPid, pkg, "Bad notification posted from package " + pkg + ": " + message);
            } catch (RemoteException e) {
            }
            Binder.restoreCallingIdentity(ident);
        }

        public void onNotificationVisibilityChanged(NotificationVisibility[] newlyVisibleKeys, NotificationVisibility[] noLongerVisibleKeys) {
            int i = 0;
            Flog.i(400, "onNotificationVisibilityChanged called");
            synchronized (NotificationManagerService.this.mNotificationList) {
                for (NotificationVisibility nv : newlyVisibleKeys) {
                    NotificationVisibility nv2;
                    NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(nv2.key);
                    if (r != null) {
                        r.setVisibility(true, nv2.rank);
                        nv2.recycle();
                    }
                }
                int length = noLongerVisibleKeys.length;
                while (i < length) {
                    nv2 = noLongerVisibleKeys[i];
                    r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(nv2.key);
                    if (r != null) {
                        r.setVisibility(false, nv2.rank);
                        nv2.recycle();
                    }
                    i++;
                }
            }
        }

        public void onNotificationExpansionChanged(String key, boolean userAction, boolean expanded) {
            int i = 1;
            Flog.i(400, "onNotificationExpansionChanged called");
            synchronized (NotificationManagerService.this.mNotificationList) {
                NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(key);
                if (r != null) {
                    int i2;
                    r.stats.onExpansionChanged(userAction, expanded);
                    long now = System.currentTimeMillis();
                    if (userAction) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    if (!expanded) {
                        i = 0;
                    }
                    EventLogTags.writeNotificationExpansion(key, i2, i, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now));
                }
            }
        }
    };
    private Light mNotificationLight;
    protected final ArrayList<NotificationRecord> mNotificationList = new ArrayList();
    private boolean mNotificationPulseEnabled;
    private HwSysResource mNotificationResource;
    final ArrayMap<String, NotificationRecord> mNotificationsByKey = new ArrayMap();
    private final BroadcastReceiver mPackageIntentReceiver = new BroadcastReceiver() {
        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                boolean booleanExtra;
                String[] pkgList;
                String pkgName;
                boolean z = false;
                boolean z2 = false;
                boolean z3 = false;
                boolean cancelNotifications = true;
                int reason = 5;
                if (!action.equals("android.intent.action.PACKAGE_ADDED")) {
                    z2 = action.equals("android.intent.action.PACKAGE_REMOVED");
                    if (!(z2 || action.equals("android.intent.action.PACKAGE_RESTARTED"))) {
                        z3 = action.equals("android.intent.action.PACKAGE_CHANGED");
                        if (!z3) {
                            z = action.equals("android.intent.action.QUERY_PACKAGE_RESTART");
                            if (!z) {
                                if (!action.equals("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE")) {
                                }
                            }
                        }
                    }
                }
                int changeUserId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                Flog.i(400, "mIntentReceiver-Package changed");
                if (z2) {
                    booleanExtra = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
                } else {
                    booleanExtra = false;
                }
                Flog.i(400, "action=" + action + " queryReplace=" + booleanExtra);
                if (action.equals("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE")) {
                    pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                } else if (action.equals("android.intent.action.PACKAGES_SUSPENDED")) {
                    pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                    reason = 14;
                } else if (z) {
                    pkgList = intent.getStringArrayExtra("android.intent.extra.PACKAGES");
                } else {
                    Uri uri = intent.getData();
                    if (uri != null) {
                        pkgName = uri.getSchemeSpecificPart();
                        if (pkgName != null) {
                            if (z2 || z3) {
                                int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                                if (!(uid == -1 || NotificationManagerService.this.mNotificationResource == null)) {
                                    NotificationManagerService.this.mNotificationResource.clear(uid, pkgName, -1);
                                }
                            }
                            if (z3) {
                                try {
                                    int i;
                                    IPackageManager pm = AppGlobals.getPackageManager();
                                    if (changeUserId != -1) {
                                        i = changeUserId;
                                    } else {
                                        i = 0;
                                    }
                                    int enabled = pm.getApplicationEnabledSetting(pkgName, i);
                                    if (enabled == 1 || enabled == 0) {
                                        cancelNotifications = false;
                                    }
                                } catch (IllegalArgumentException e) {
                                    Flog.i(400, "Exception trying to look up app enabled setting", e);
                                } catch (RemoteException e2) {
                                }
                            }
                            pkgList = new String[]{pkgName};
                        } else {
                            return;
                        }
                    }
                    return;
                }
                if (pkgList != null && pkgList.length > 0) {
                    for (String pkgName2 : pkgList) {
                        if (cancelNotifications) {
                            NotificationManagerService.this.cancelAllNotificationsInt(NotificationManagerService.MY_UID, NotificationManagerService.MY_PID, pkgName2, 0, 0, !z, changeUserId, reason, null);
                        }
                    }
                }
                NotificationManagerService.this.mListeners.onPackagesChanged(booleanExtra, pkgList);
                NotificationManagerService.this.mRankerServices.onPackagesChanged(booleanExtra, pkgList);
                NotificationManagerService.this.mConditionProviders.onPackagesChanged(booleanExtra, pkgList);
                NotificationManagerService.this.mRankingHelper.onPackagesChanged(booleanExtra, pkgList);
            }
        }
    };
    final PolicyAccess mPolicyAccess = new PolicyAccess();
    private AtomicFile mPolicyFile;
    private PowerSaverObserver mPowerSaverObserver;
    private String mRankerServicePackageName;
    private NotificationRankers mRankerServices;
    private RankingHandler mRankingHandler;
    private RankingHelper mRankingHelper;
    private final HandlerThread mRankingThread = new HandlerThread("ranker", 10);
    private boolean mScreenOn = true;
    private final IBinder mService = new Stub() {
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (1599293262 == code) {
                try {
                    int event = data.readInt();
                    Flog.i(400, "NotificationManagerService.onTransact: got ST_GET_NOTIFICATIONS event " + event);
                    switch (event) {
                        case 1:
                            NotificationManagerService.this.handleGetNotifications(data, reply);
                            return true;
                    }
                } catch (Exception e) {
                    Flog.i(400, "NotificationManagerService.onTransact: catch exception " + e.toString());
                    return false;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

        public void enqueueToast(String pkg, ITransientNotification callback, int duration) {
            enqueueToastEx(pkg, callback, duration, "");
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void enqueueToastEx(String pkg, ITransientNotification callback, int duration, String toastStr) {
            Flog.i(400, "enqueueToast pkg=" + pkg + " callback=" + callback + " duration=" + duration);
            if (pkg == null || callback == null) {
                Slog.e(NotificationManagerService.TAG, "Not doing toast. pkg=" + pkg + " callback=" + callback);
                return;
            }
            boolean equals = !NotificationManagerService.isCallerSystem() ? "android".equals(pkg) : true;
            boolean isPackageSuspended = NotificationManagerService.this.isPackageSuspendedForUser(pkg, Binder.getCallingUid());
            if ((!NotificationManagerService.this.noteNotificationOp(pkg, Binder.getCallingUid()) || isPackageSuspended) && !equals) {
                String str;
                String str2 = NotificationManagerService.TAG;
                StringBuilder append = new StringBuilder().append("Suppressing toast from package ").append(pkg);
                if (isPackageSuspended) {
                    str = " due to package suspended by administrator.";
                } else {
                    str = " by user request.";
                }
                Slog.e(str2, append.append(str).toString());
                return;
            }
            synchronized (NotificationManagerService.this.mToastQueue) {
                int callingPid = Binder.getCallingPid();
                long callingId = Binder.clearCallingIdentity();
                try {
                    int index = NotificationManagerService.this.indexOfToastLocked(pkg, callback);
                    if (index >= 0) {
                        ((ToastRecord) NotificationManagerService.this.mToastQueue.get(index)).update(duration);
                    } else {
                        if (!equals) {
                            int count = 0;
                            int N = NotificationManagerService.this.mToastQueue.size();
                            for (int i = 0; i < N; i++) {
                                if (((ToastRecord) NotificationManagerService.this.mToastQueue.get(i)).pkg.equals(pkg)) {
                                    count++;
                                    if (count >= 50) {
                                        Slog.e(NotificationManagerService.TAG, "Package has already posted " + count + " toasts. Not showing more. Package=" + pkg);
                                        Binder.restoreCallingIdentity(callingId);
                                        return;
                                    }
                                }
                            }
                        }
                        ToastRecord record = new ToastRecord(callingPid, pkg, callback, duration, toastStr);
                        boolean ignoreAdd = false;
                        if (!NotificationManagerService.this.mToastQueue.isEmpty()) {
                            String firstToastStr = ((ToastRecord) NotificationManagerService.this.mToastQueue.get(0)).toastStr;
                            if (firstToastStr == null) {
                                Slog.e(NotificationManagerService.TAG, "firstToastStr is null,maybe it's a custom toast.");
                                Binder.restoreCallingIdentity(callingId);
                                return;
                            } else if (firstToastStr.equals(toastStr)) {
                                ignoreAdd = true;
                            } else {
                                NotificationManagerService.this.mHandler.removeCallbacksAndMessages(NotificationManagerService.this.mToastQueue.get(0));
                                NotificationManagerService.this.cancelToastLocked(0);
                            }
                        }
                        if (!ignoreAdd) {
                            NotificationManagerService.this.mToastQueue.add(record);
                            index = NotificationManagerService.this.mToastQueue.size() - 1;
                            NotificationManagerService.this.keepProcessAliveLocked(callingPid);
                        }
                    }
                    if (index == 0) {
                        NotificationManagerService.this.showNextToastLocked();
                    }
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        }

        public void cancelToast(String pkg, ITransientNotification callback) {
            Slog.i(NotificationManagerService.TAG, "cancelToast pkg=" + pkg + " callback=" + callback);
            if (pkg == null || callback == null) {
                Slog.e(NotificationManagerService.TAG, "Not cancelling notification. pkg=" + pkg + " callback=" + callback);
                return;
            }
            synchronized (NotificationManagerService.this.mToastQueue) {
                long callingId = Binder.clearCallingIdentity();
                try {
                    int index = NotificationManagerService.this.indexOfToastLocked(pkg, callback);
                    if (index >= 0) {
                        NotificationManagerService.this.cancelToastLocked(index);
                    } else {
                        Slog.w(NotificationManagerService.TAG, "Toast already cancelled. pkg=" + pkg + " callback=" + callback);
                    }
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        }

        public void enqueueNotificationWithTag(String pkg, String opPkg, String tag, int id, Notification notification, int[] idOut, int userId) throws RemoteException {
            tag = NotificationManagerService.this.convertNotificationTag(tag, Binder.getCallingPid());
            NotificationManagerService.this.handleNotificationForClone(notification, Binder.getCallingPid());
            NotificationManagerService.this.addHwExtraForNotification(notification, pkg, Binder.getCallingPid());
            NotificationManagerService.this.enqueueNotificationInternal(NotificationManagerService.this.getNCTargetAppPkg(opPkg, pkg, notification), opPkg, Binder.getCallingUid(), Binder.getCallingPid(), tag, id, notification, idOut, userId);
        }

        public void cancelNotificationWithTag(String pkg, String tag, int id, int userId) {
            NotificationManagerService.checkCallerIsSystemOrSameApp(pkg);
            tag = NotificationManagerService.this.convertNotificationTag(tag, Binder.getCallingPid());
            userId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, false, "cancelNotificationWithTag", pkg);
            Flog.i(400, "cancelNotificationWithTag pid " + Binder.getCallingPid() + ",uid = " + Binder.getCallingUid() + ",tag = " + tag + ",pkg =" + pkg + ",id =" + id);
            NotificationManagerService.this.cancelNotification(Binder.getCallingUid(), Binder.getCallingPid(), pkg, tag, id, 0, (Binder.getCallingUid() == 1000 ? 0 : 64) | (Binder.getCallingUid() == 1000 ? 0 : 1024), false, userId, 8, null);
        }

        public void cancelAllNotifications(String pkg, int userId) {
            NotificationManagerService.checkCallerIsSystemOrSameApp(pkg);
            Flog.i(400, "cancelAllNotifications pid " + Binder.getCallingPid() + ",uid = " + Binder.getCallingUid());
            NotificationManagerService.this.cancelAllNotificationsInt(Binder.getCallingUid(), Binder.getCallingPid(), pkg, 0, 64, true, ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, false, "cancelAllNotifications", pkg), 9, null);
        }

        public void setNotificationsEnabledForPackage(String pkg, int uid, boolean enabled) {
            NotificationManagerService.checkCallerIsSystem();
            NotificationManagerService.this.setNotificationsEnabledForPackageImpl(pkg, uid, enabled);
            NotificationManagerService.this.mRankingHelper.setEnabled(pkg, uid, enabled);
            NotificationManagerService.this.savePolicyFile();
        }

        public boolean areNotificationsEnabled(String pkg) {
            return areNotificationsEnabledForPackage(pkg, Binder.getCallingUid());
        }

        public boolean areNotificationsEnabledForPackage(String pkg, int uid) {
            NotificationManagerService.checkCallerIsSystemOrSameApp(pkg);
            if (NotificationManagerService.this.mAppOps.checkOpNoThrow(11, uid, pkg) != 0 || NotificationManagerService.this.isPackageSuspendedForUser(pkg, uid)) {
                return false;
            }
            return true;
        }

        public void setPriority(String pkg, int uid, int priority) {
            NotificationManagerService.checkCallerIsSystem();
            NotificationManagerService.this.mRankingHelper.setPriority(pkg, uid, priority);
            NotificationManagerService.this.savePolicyFile();
        }

        public int getPriority(String pkg, int uid) {
            NotificationManagerService.checkCallerIsSystem();
            return NotificationManagerService.this.mRankingHelper.getPriority(pkg, uid);
        }

        public void setVisibilityOverride(String pkg, int uid, int visibility) {
            NotificationManagerService.checkCallerIsSystem();
            NotificationManagerService.this.mRankingHelper.setVisibilityOverride(pkg, uid, visibility);
            NotificationManagerService.this.savePolicyFile();
        }

        public int getVisibilityOverride(String pkg, int uid) {
            NotificationManagerService.checkCallerIsSystem();
            return NotificationManagerService.this.mRankingHelper.getVisibilityOverride(pkg, uid);
        }

        private boolean isCallerHasLegacyPermission() {
            int stat = NotificationManagerService.this.getContext().checkPermission("com.huawei.permission.USE_LEGACY_INTERFACE", Binder.getCallingPid(), Binder.getCallingUid());
            if (stat != 0) {
                Log.d(NotificationManagerService.TAG, "Legacy perm missing");
            }
            if (stat == 0) {
                return true;
            }
            return false;
        }

        public void setImportance(String pkg, int uid, int importance) {
            boolean z = false;
            enforceSystemOrSystemUI("Caller not system or systemui");
            NotificationManagerService notificationManagerService = NotificationManagerService.this;
            if (importance != 0) {
                z = true;
            }
            notificationManagerService.setNotificationsEnabledForPackageImpl(pkg, uid, z);
            NotificationManagerService.this.mRankingHelper.setImportance(pkg, uid, importance);
            NotificationManagerService.this.savePolicyFile();
        }

        public int getPackageImportance(String pkg) {
            NotificationManagerService.checkCallerIsSystemOrSameApp(pkg);
            return NotificationManagerService.this.mRankingHelper.getImportance(pkg, Binder.getCallingUid());
        }

        public int getImportance(String pkg, int uid) {
            enforceSystemOrSystemUI("Caller not system or systemui");
            return NotificationManagerService.this.mRankingHelper.getImportance(pkg, uid);
        }

        public StatusBarNotification[] getActiveNotifications(String callingPkg) {
            NotificationManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.ACCESS_NOTIFICATIONS", "NotificationManagerService.getActiveNotifications");
            StatusBarNotification[] tmp = null;
            if (NotificationManagerService.this.mAppOps.noteOpNoThrow(25, Binder.getCallingUid(), callingPkg) == 0) {
                synchronized (NotificationManagerService.this.mNotificationList) {
                    tmp = new StatusBarNotification[NotificationManagerService.this.mNotificationList.size()];
                    int N = NotificationManagerService.this.mNotificationList.size();
                    for (int i = 0; i < N; i++) {
                        tmp[i] = ((NotificationRecord) NotificationManagerService.this.mNotificationList.get(i)).sbn;
                    }
                }
            }
            return tmp;
        }

        public ParceledListSlice<StatusBarNotification> getAppActiveNotifications(String pkg, int incomingUserId) {
            NotificationManagerService.checkCallerIsSystemOrSameApp(pkg);
            int userId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), incomingUserId, true, false, "getAppActiveNotifications", pkg);
            ArrayList<StatusBarNotification> arrayList = new ArrayList(NotificationManagerService.this.mNotificationList.size());
            synchronized (NotificationManagerService.this.mNotificationList) {
                int N = NotificationManagerService.this.mNotificationList.size();
                for (int i = 0; i < N; i++) {
                    StatusBarNotification sbn = ((NotificationRecord) NotificationManagerService.this.mNotificationList.get(i)).sbn;
                    if (sbn.getPackageName().equals(pkg) && sbn.getUserId() == userId && (sbn.getNotification().flags & 1024) == 0) {
                        arrayList.add(new StatusBarNotification(sbn.getPackageName(), sbn.getOpPkg(), sbn.getId(), sbn.getTag(), sbn.getUid(), sbn.getInitialPid(), 0, sbn.getNotification().clone(), sbn.getUser(), sbn.getPostTime()));
                    }
                }
            }
            return new ParceledListSlice(arrayList);
        }

        public StatusBarNotification[] getHistoricalNotifications(String callingPkg, int count) {
            NotificationManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.ACCESS_NOTIFICATIONS", "NotificationManagerService.getHistoricalNotifications");
            StatusBarNotification[] tmp = null;
            if (NotificationManagerService.this.mAppOps.noteOpNoThrow(25, Binder.getCallingUid(), callingPkg) == 0) {
                synchronized (NotificationManagerService.this.mArchive) {
                    tmp = NotificationManagerService.this.mArchive.getArray(count);
                }
            }
            return tmp;
        }

        public void registerListener(INotificationListener listener, ComponentName component, int userid) {
            enforceSystemOrSystemUI("INotificationManager.registerListener");
            NotificationManagerService.this.mListeners.registerService(listener, component, userid);
        }

        public void unregisterListener(INotificationListener token, int userid) {
            NotificationManagerService.this.mListeners.unregisterService((IInterface) token, userid);
        }

        public void cancelNotificationsFromListener(INotificationListener token, String[] keys) {
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            long identity = Binder.clearCallingIdentity();
            try {
                Flog.i(400, "cancelNotificationsFromListener called,callingUid = " + callingUid + ",callingPid = " + callingPid);
                synchronized (NotificationManagerService.this.mNotificationList) {
                    ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                    if (keys != null) {
                        for (Object obj : keys) {
                            NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(obj);
                            if (r != null) {
                                int userId = r.sbn.getUserId();
                                if (userId == info.userid || userId == -1 || NotificationManagerService.this.mUserProfiles.isCurrentProfile(userId)) {
                                    cancelNotificationFromListenerLocked(info, callingUid, callingPid, r.sbn.getPackageName(), r.sbn.getTag(), r.sbn.getId(), userId);
                                } else {
                                    throw new SecurityException("Disallowed call from listener: " + info.service);
                                }
                            }
                        }
                    } else {
                        NotificationManagerService.this.cancelAllLocked(callingUid, callingPid, info.userid, 11, info, info.supportsProfiles());
                    }
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void requestBindListener(ComponentName component) {
            NotificationManagerService.checkCallerIsSystemOrSameApp(component.getPackageName());
            long identity = Binder.clearCallingIdentity();
            try {
                ManagedServices manager;
                if (NotificationManagerService.this.mRankerServices.isComponentEnabledForCurrentProfiles(component)) {
                    manager = NotificationManagerService.this.mRankerServices;
                } else {
                    manager = NotificationManagerService.this.mListeners;
                }
                manager.setComponentState(component, true);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void requestUnbindListener(INotificationListener token) {
            long identity = Binder.clearCallingIdentity();
            try {
                ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                info.getOwner().setComponentState(info.component, false);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void setNotificationsShownFromListener(INotificationListener token, String[] keys) {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationList) {
                    ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                    if (keys != null) {
                        int N = keys.length;
                        for (int i = 0; i < N; i++) {
                            NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(keys[i]);
                            if (r != null) {
                                int userId = r.sbn.getUserId();
                                if (userId != info.userid && userId != -1 && !NotificationManagerService.this.mUserProfiles.isCurrentProfile(userId)) {
                                    throw new SecurityException("Disallowed call from listener: " + info.service);
                                } else if (!r.isSeen()) {
                                    Flog.i(400, "Marking notification as seen " + keys[i]);
                                    UsageStatsManagerInternal -get3 = NotificationManagerService.this.mAppUsageStats;
                                    String packageName = r.sbn.getPackageName();
                                    if (userId == -1) {
                                        userId = 0;
                                    }
                                    -get3.reportEvent(packageName, userId, 7);
                                    r.setSeen();
                                }
                            }
                        }
                    }
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }

        private void cancelNotificationFromListenerLocked(ManagedServiceInfo info, int callingUid, int callingPid, String pkg, String tag, int id, int userId) {
            NotificationManagerService.this.cancelNotification(callingUid, callingPid, pkg, tag, id, 0, 66, true, userId, 10, info);
        }

        public void cancelNotificationFromListener(INotificationListener token, String pkg, String tag, int id) {
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationList) {
                    ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                    if (info.supportsProfiles()) {
                        Log.e(NotificationManagerService.TAG, "Ignoring deprecated cancelNotification(pkg, tag, id) from " + info.component + " use cancelNotification(key) instead.");
                    } else {
                        cancelNotificationFromListenerLocked(info, callingUid, callingPid, pkg, tag, id, info.userid);
                    }
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public ParceledListSlice<StatusBarNotification> getActiveNotificationsFromListener(INotificationListener token, String[] keys, int trim) {
            ParceledListSlice<StatusBarNotification> parceledListSlice;
            synchronized (NotificationManagerService.this.mNotificationList) {
                ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                boolean getKeys = keys != null;
                int N = getKeys ? keys.length : NotificationManagerService.this.mNotificationList.size();
                ArrayList<StatusBarNotification> list = new ArrayList(N);
                for (int i = 0; i < N; i++) {
                    NotificationRecord r;
                    if (getKeys) {
                        r = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(keys[i]);
                    } else {
                        r = (NotificationRecord) NotificationManagerService.this.mNotificationList.get(i);
                    }
                    if (r != null) {
                        StatusBarNotification sbn = r.sbn;
                        if (NotificationManagerService.this.isVisibleToListener(sbn, info)) {
                            list.add(trim == 0 ? sbn : sbn.cloneLight());
                        } else {
                            continue;
                        }
                    }
                }
                parceledListSlice = new ParceledListSlice(list);
            }
            return parceledListSlice;
        }

        public void requestHintsFromListener(INotificationListener token, int hints) {
            boolean disableEffects = false;
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationList) {
                    ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                    if ((hints & 7) != 0) {
                        disableEffects = true;
                    }
                    if (disableEffects) {
                        NotificationManagerService.this.addDisabledHints(info, hints);
                    } else {
                        NotificationManagerService.this.removeDisabledHints(info, hints);
                    }
                    NotificationManagerService.this.updateListenerHintsLocked();
                    NotificationManagerService.this.updateEffectsSuppressorLocked();
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public int getHintsFromListener(INotificationListener token) {
            int -get10;
            synchronized (NotificationManagerService.this.mNotificationList) {
                -get10 = NotificationManagerService.this.mListenerHints;
            }
            return -get10;
        }

        public void requestInterruptionFilterFromListener(INotificationListener token, int interruptionFilter) throws RemoteException {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationList) {
                    NotificationManagerService.this.mZenModeHelper.requestFromListener(NotificationManagerService.this.mListeners.checkServiceTokenLocked(token).component, interruptionFilter);
                    NotificationManagerService.this.updateInterruptionFilterLocked();
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public int getInterruptionFilterFromListener(INotificationListener token) throws RemoteException {
            int -get9;
            synchronized (NotificationManagerService.this.mNotificationLight) {
                -get9 = NotificationManagerService.this.mInterruptionFilter;
            }
            return -get9;
        }

        public void setOnNotificationPostedTrimFromListener(INotificationListener token, int trim) throws RemoteException {
            synchronized (NotificationManagerService.this.mNotificationList) {
                ManagedServiceInfo info = NotificationManagerService.this.mListeners.checkServiceTokenLocked(token);
                if (info == null) {
                    return;
                }
                NotificationManagerService.this.mListeners.setOnNotificationPostedTrimLocked(info, trim);
            }
        }

        public int getZenMode() {
            return NotificationManagerService.this.mZenModeHelper.getZenMode();
        }

        public ZenModeConfig getZenModeConfig() {
            enforceSystemOrSystemUIOrVolume("INotificationManager.getZenModeConfig");
            return NotificationManagerService.this.mZenModeHelper.getConfig();
        }

        public void setZenMode(int mode, Uri conditionId, String reason) throws RemoteException {
            enforceSystemOrSystemUIOrVolume("INotificationManager.setZenMode");
            long identity = Binder.clearCallingIdentity();
            try {
                NotificationManagerService.this.mZenModeHelper.setManualZenMode(mode, conditionId, reason);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public List<ZenRule> getZenRules() throws RemoteException {
            enforcePolicyAccess(Binder.getCallingUid(), "getAutomaticZenRules");
            return NotificationManagerService.this.mZenModeHelper.getZenRules();
        }

        public AutomaticZenRule getAutomaticZenRule(String id) throws RemoteException {
            Preconditions.checkNotNull(id, "Id is null");
            enforcePolicyAccess(Binder.getCallingUid(), "getAutomaticZenRule");
            return NotificationManagerService.this.mZenModeHelper.getAutomaticZenRule(id);
        }

        public String addAutomaticZenRule(AutomaticZenRule automaticZenRule) throws RemoteException {
            Preconditions.checkNotNull(automaticZenRule, "automaticZenRule is null");
            Preconditions.checkNotNull(automaticZenRule.getName(), "Name is null");
            Preconditions.checkNotNull(automaticZenRule.getOwner(), "Owner is null");
            Preconditions.checkNotNull(automaticZenRule.getConditionId(), "ConditionId is null");
            enforcePolicyAccess(Binder.getCallingUid(), "addAutomaticZenRule");
            return NotificationManagerService.this.mZenModeHelper.addAutomaticZenRule(automaticZenRule, "addAutomaticZenRule");
        }

        public boolean updateAutomaticZenRule(String id, AutomaticZenRule automaticZenRule) throws RemoteException {
            Preconditions.checkNotNull(automaticZenRule, "automaticZenRule is null");
            Preconditions.checkNotNull(automaticZenRule.getName(), "Name is null");
            Preconditions.checkNotNull(automaticZenRule.getOwner(), "Owner is null");
            Preconditions.checkNotNull(automaticZenRule.getConditionId(), "ConditionId is null");
            enforcePolicyAccess(Binder.getCallingUid(), "updateAutomaticZenRule");
            return NotificationManagerService.this.mZenModeHelper.updateAutomaticZenRule(id, automaticZenRule, "updateAutomaticZenRule");
        }

        public boolean removeAutomaticZenRule(String id) throws RemoteException {
            Preconditions.checkNotNull(id, "Id is null");
            enforcePolicyAccess(Binder.getCallingUid(), "removeAutomaticZenRule");
            return NotificationManagerService.this.mZenModeHelper.removeAutomaticZenRule(id, "removeAutomaticZenRule");
        }

        public boolean removeAutomaticZenRules(String packageName) throws RemoteException {
            Preconditions.checkNotNull(packageName, "Package name is null");
            enforceSystemOrSystemUI("removeAutomaticZenRules");
            return NotificationManagerService.this.mZenModeHelper.removeAutomaticZenRules(packageName, "removeAutomaticZenRules");
        }

        public int getRuleInstanceCount(ComponentName owner) throws RemoteException {
            Preconditions.checkNotNull(owner, "Owner is null");
            enforceSystemOrSystemUI("getRuleInstanceCount");
            return NotificationManagerService.this.mZenModeHelper.getCurrentInstanceCount(owner);
        }

        public void setInterruptionFilter(String pkg, int filter) throws RemoteException {
            enforcePolicyAccess(pkg, "setInterruptionFilter");
            int zen = NotificationManager.zenModeFromInterruptionFilter(filter, -1);
            if (zen == -1) {
                throw new IllegalArgumentException("Invalid filter: " + filter);
            }
            long identity = Binder.clearCallingIdentity();
            try {
                NotificationManagerService.this.mZenModeHelper.setManualZenMode(zen, null, "setInterruptionFilter");
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void notifyConditions(final String pkg, IConditionProvider provider, final Condition[] conditions) {
            final ManagedServiceInfo info = NotificationManagerService.this.mConditionProviders.checkServiceToken(provider);
            NotificationManagerService.checkCallerIsSystemOrSameApp(pkg);
            NotificationManagerService.this.mHandler.post(new Runnable() {
                public void run() {
                    NotificationManagerService.this.mConditionProviders.notifyConditions(pkg, info, conditions);
                }
            });
        }

        private void enforceSystemOrSystemUIOrVolume(String message) {
            if (NotificationManagerService.this.mAudioManagerInternal != null) {
                int vcuid = NotificationManagerService.this.mAudioManagerInternal.getVolumeControllerUid();
                if (vcuid > 0 && Binder.getCallingUid() == vcuid) {
                    return;
                }
            }
            enforceSystemOrSystemUI(message);
        }

        private void enforceSystemOrSystemUI(String message) {
            if (!NotificationManagerService.isCallerSystem()) {
                NotificationManagerService.this.getContext().enforceCallingPermission("android.permission.STATUS_BAR_SERVICE", message);
            }
        }

        private void enforceSystemOrSystemUIOrSamePackage(String pkg, String message) {
            try {
                NotificationManagerService.checkCallerIsSystemOrSameApp(pkg);
            } catch (SecurityException e) {
                NotificationManagerService.this.getContext().enforceCallingPermission("android.permission.STATUS_BAR_SERVICE", message);
            }
        }

        private void enforcePolicyAccess(int uid, String method) {
            if (NotificationManagerService.this.getContext().checkCallingPermission("android.permission.MANAGE_NOTIFICATIONS") != 0) {
                boolean accessAllowed = false;
                for (String checkPolicyAccess : NotificationManagerService.this.getContext().getPackageManager().getPackagesForUid(uid)) {
                    if (checkPolicyAccess(checkPolicyAccess)) {
                        accessAllowed = true;
                    }
                }
                if (!accessAllowed) {
                    Slog.w(NotificationManagerService.TAG, "Notification policy access denied calling " + method);
                    throw new SecurityException("Notification policy access denied");
                }
            }
        }

        private void enforcePolicyAccess(String pkg, String method) {
            if (NotificationManagerService.this.getContext().checkCallingPermission("android.permission.MANAGE_NOTIFICATIONS") != 0) {
                NotificationManagerService.checkCallerIsSameApp(pkg);
                if (!checkPolicyAccess(pkg)) {
                    Slog.w(NotificationManagerService.TAG, "Notification policy access denied calling " + method);
                    throw new SecurityException("Notification policy access denied");
                }
            }
        }

        private boolean checkPackagePolicyAccess(String pkg) {
            return NotificationManagerService.this.mPolicyAccess.isPackageGranted(pkg);
        }

        private boolean checkPolicyAccess(String pkg) {
            boolean z = true;
            try {
                if (ActivityManager.checkComponentPermission("android.permission.MANAGE_NOTIFICATIONS", NotificationManagerService.this.getContext().getPackageManager().getPackageUidAsUser(pkg, UserHandle.getCallingUserId()), -1, true) == 0) {
                    return true;
                }
                if (!checkPackagePolicyAccess(pkg)) {
                    z = NotificationManagerService.this.mListeners.isComponentEnabledForPackage(pkg);
                }
                return z;
            } catch (NameNotFoundException e) {
                return false;
            }
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (NotificationManagerService.this.getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump NotificationManager from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            DumpFilter filter = DumpFilter.parseFromArguments(args);
            if (filter == null || !filter.stats) {
                NotificationManagerService.this.dumpImpl(pw, filter);
            } else {
                NotificationManagerService.this.dumpJson(pw, filter);
            }
        }

        public ComponentName getEffectsSuppressor() {
            enforceSystemOrSystemUIOrVolume("INotificationManager.getEffectsSuppressor");
            return !NotificationManagerService.this.mEffectsSuppressors.isEmpty() ? (ComponentName) NotificationManagerService.this.mEffectsSuppressors.get(0) : null;
        }

        public boolean matchesCallFilter(Bundle extras) {
            enforceSystemOrSystemUI("INotificationManager.matchesCallFilter");
            return NotificationManagerService.this.mZenModeHelper.matchesCallFilter(Binder.getCallingUserHandle(), extras, (ValidateNotificationPeople) NotificationManagerService.this.mRankingHelper.findExtractor(ValidateNotificationPeople.class), NotificationManagerService.MATCHES_CALL_FILTER_CONTACTS_TIMEOUT_MS, NotificationManagerService.MATCHES_CALL_FILTER_TIMEOUT_AFFINITY);
        }

        public boolean isSystemConditionProviderEnabled(String path) {
            enforceSystemOrSystemUIOrVolume("INotificationManager.isSystemConditionProviderEnabled");
            return NotificationManagerService.this.mConditionProviders.isSystemProviderEnabled(path);
        }

        public byte[] getBackupPayload(int user) {
            if (NotificationManagerService.DBG) {
                Slog.d(NotificationManagerService.TAG, "getBackupPayload u=" + user);
            }
            if (user != 0) {
                Slog.w(NotificationManagerService.TAG, "getBackupPayload: cannot backup policy for user " + user);
                return null;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                NotificationManagerService.this.writePolicyXml(baos, true);
                return baos.toByteArray();
            } catch (IOException e) {
                Slog.w(NotificationManagerService.TAG, "getBackupPayload: error writing payload for user " + user, e);
                return null;
            }
        }

        public void applyRestore(byte[] payload, int user) {
            String str = null;
            if (NotificationManagerService.DBG) {
                String str2 = NotificationManagerService.TAG;
                StringBuilder append = new StringBuilder().append("applyRestore u=").append(user).append(" payload=");
                if (payload != null) {
                    str = new String(payload, StandardCharsets.UTF_8);
                }
                Slog.d(str2, append.append(str).toString());
            }
            if (payload == null) {
                Slog.w(NotificationManagerService.TAG, "applyRestore: no payload to restore for user " + user);
            } else if (user != 0) {
                Slog.w(NotificationManagerService.TAG, "applyRestore: cannot restore policy for user " + user);
            } else {
                try {
                    NotificationManagerService.this.readPolicyXml(new ByteArrayInputStream(payload), true);
                    NotificationManagerService.this.savePolicyFile();
                } catch (Exception e) {
                    Slog.w(NotificationManagerService.TAG, "applyRestore: error reading payload", e);
                }
            }
        }

        public boolean isNotificationPolicyAccessGranted(String pkg) {
            return checkPolicyAccess(pkg);
        }

        public boolean isNotificationPolicyAccessGrantedForPackage(String pkg) {
            enforceSystemOrSystemUIOrSamePackage(pkg, "request policy access status for another package");
            return checkPolicyAccess(pkg);
        }

        public String[] getPackagesRequestingNotificationPolicyAccess() throws RemoteException {
            enforceSystemOrSystemUI("request policy access packages");
            long identity = Binder.clearCallingIdentity();
            try {
                String[] requestingPackages = NotificationManagerService.this.mPolicyAccess.getRequestingPackages();
                return requestingPackages;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void setNotificationPolicyAccessGranted(String pkg, boolean granted) throws RemoteException {
            enforceSystemOrSystemUI("grant notification policy access");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationList) {
                    NotificationManagerService.this.mPolicyAccess.put(pkg, granted);
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public Policy getNotificationPolicy(String pkg) {
            enforcePolicyAccess(pkg, "getNotificationPolicy");
            long identity = Binder.clearCallingIdentity();
            try {
                Policy notificationPolicy = NotificationManagerService.this.mZenModeHelper.getNotificationPolicy();
                return notificationPolicy;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void setNotificationPolicy(String pkg, Policy policy) {
            enforcePolicyAccess(pkg, "setNotificationPolicy");
            long identity = Binder.clearCallingIdentity();
            try {
                NotificationManagerService.this.mZenModeHelper.setNotificationPolicy(policy);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void applyAdjustmentFromRankerService(INotificationListener token, Adjustment adjustment) throws RemoteException {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationList) {
                    NotificationManagerService.this.mRankerServices.checkServiceTokenLocked(token);
                    NotificationManagerService.this.applyAdjustmentLocked(adjustment);
                }
                NotificationManagerService.this.maybeAddAutobundleSummary(adjustment);
                NotificationManagerService.this.mRankingHandler.requestSort();
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void applyAdjustmentsFromRankerService(INotificationListener token, List<Adjustment> adjustments) throws RemoteException {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (NotificationManagerService.this.mNotificationList) {
                    NotificationManagerService.this.mRankerServices.checkServiceTokenLocked(token);
                    for (Adjustment adjustment : adjustments) {
                        NotificationManagerService.this.applyAdjustmentLocked(adjustment);
                    }
                }
                for (Adjustment adjustment2 : adjustments) {
                    NotificationManagerService.this.maybeAddAutobundleSummary(adjustment2);
                }
                NotificationManagerService.this.mRankingHandler.requestSort();
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }
    };
    private SettingsObserver mSettingsObserver;
    protected String mSoundNotificationKey;
    StatusBarManagerInternal mStatusBar;
    final ArrayMap<String, NotificationRecord> mSummaryByGroupKey = new ArrayMap();
    boolean mSystemReady;
    final ArrayList<ToastRecord> mToastQueue = new ArrayList();
    private NotificationUsageStats mUsageStats;
    private boolean mUseAttentionLight;
    private final UserProfiles mUserProfiles = new UserProfiles();
    private String mVibrateNotificationKey;
    Vibrator mVibrator;
    private VrManagerInternal mVrManagerInternal;
    private ZenModeHelper mZenModeHelper;
    private String[] noitfication_white_list = new String[]{"com.huawei.message", "com.android.mms", "com.android.contacts", "com.android.phone", "com.android.deskclock", "com.android.calendar", "com.android.systemui", "android", "com.android.incallui", "com.android.phone.recorder", "com.android.cellbroadcastreceiver", "com.android.server.telecom"};
    private String[] plus_notification_white_list = new String[]{"com.android.bluetooth"};
    private final BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (NotificationManagerService.ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE.equals(action)) {
                    if (intent.getIntExtra(NotificationManagerService.POWER_MODE, 0) == 3) {
                        if (NotificationManagerService.this.mPowerSaverObserver == null) {
                            NotificationManagerService.this.mPowerSaverObserver = new PowerSaverObserver(NotificationManagerService.this.mHandler);
                        }
                        NotificationManagerService.this.mPowerSaverObserver.observe();
                        Log.i(NotificationManagerService.TAG, "super power save 2.0 recevier brodcast register sqlite listener");
                    }
                } else if (NotificationManagerService.ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE.equals(action) && intent.getIntExtra(NotificationManagerService.SHUTDOWN_LIMIT_POWERMODE, 0) == 0 && NotificationManagerService.this.mPowerSaverObserver != null) {
                    NotificationManagerService.this.mPowerSaverObserver.unObserve();
                    NotificationManagerService.this.mPowerSaverObserver = null;
                    Log.i(NotificationManagerService.TAG, "super power save 2.0 recevier brodcast unregister sqlite listener");
                }
            }
        }
    };
    private HashSet<String> power_save_whiteSet = new HashSet();

    private static class Archive {
        final ArrayDeque<StatusBarNotification> mBuffer = new ArrayDeque(this.mBufferSize);
        final int mBufferSize;

        public Archive(int size) {
            this.mBufferSize = size;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            int N = this.mBuffer.size();
            sb.append("Archive (");
            sb.append(N);
            sb.append(" notification");
            sb.append(N == 1 ? ")" : "s)");
            return sb.toString();
        }

        public void record(StatusBarNotification nr) {
            if (this.mBuffer.size() == this.mBufferSize) {
                this.mBuffer.removeFirst();
            }
            this.mBuffer.addLast(nr.cloneLight());
        }

        public Iterator<StatusBarNotification> descendingIterator() {
            return this.mBuffer.descendingIterator();
        }

        public StatusBarNotification[] getArray(int count) {
            if (count == 0) {
                count = this.mBufferSize;
            }
            StatusBarNotification[] a = new StatusBarNotification[Math.min(count, this.mBuffer.size())];
            Iterator<StatusBarNotification> iter = descendingIterator();
            int i = 0;
            while (iter.hasNext() && i < count) {
                int i2 = i + 1;
                a[i] = (StatusBarNotification) iter.next();
                i = i2;
            }
            return a;
        }
    }

    public static final class DumpFilter {
        public boolean filtered = false;
        public String pkgFilter;
        public boolean redact = true;
        public long since;
        public boolean stats;
        public boolean zen;

        public static DumpFilter parseFromArguments(String[] args) {
            DumpFilter filter = new DumpFilter();
            int ai = 0;
            while (ai < args.length) {
                String a = args[ai];
                if ("--noredact".equals(a) || "--reveal".equals(a)) {
                    filter.redact = false;
                } else if ("p".equals(a) || AbsLocationManagerService.DEL_PKG.equals(a) || "--package".equals(a)) {
                    if (ai < args.length - 1) {
                        ai++;
                        filter.pkgFilter = args[ai].trim().toLowerCase();
                        if (filter.pkgFilter.isEmpty()) {
                            filter.pkgFilter = null;
                        } else {
                            filter.filtered = true;
                        }
                    }
                } else if ("--zen".equals(a) || "zen".equals(a)) {
                    filter.filtered = true;
                    filter.zen = true;
                } else if ("--stats".equals(a)) {
                    filter.stats = true;
                    if (ai < args.length - 1) {
                        ai++;
                        filter.since = Long.valueOf(args[ai]).longValue();
                    } else {
                        filter.since = 0;
                    }
                }
                ai++;
            }
            return filter;
        }

        public boolean matches(StatusBarNotification sbn) {
            boolean z = true;
            if (!this.filtered) {
                return true;
            }
            if (!this.zen) {
                if (sbn == null) {
                    z = false;
                } else if (!matches(sbn.getPackageName())) {
                    z = matches(sbn.getOpPkg());
                }
            }
            return z;
        }

        public boolean matches(ComponentName component) {
            boolean z = true;
            if (!this.filtered) {
                return true;
            }
            if (!this.zen) {
                z = component != null ? matches(component.getPackageName()) : false;
            }
            return z;
        }

        public boolean matches(String pkg) {
            boolean z = true;
            if (!this.filtered) {
                return true;
            }
            if (!this.zen) {
                z = pkg != null ? pkg.toLowerCase().contains(this.pkgFilter) : false;
            }
            return z;
        }

        public String toString() {
            if (this.stats) {
                return "stats";
            }
            return this.zen ? "zen" : '\'' + this.pkgFilter + '\'';
        }
    }

    private class EnqueueNotificationRunnable implements Runnable {
        private final NotificationRecord r;
        private final int userId;

        EnqueueNotificationRunnable(int userId, NotificationRecord r) {
            this.userId = userId;
            this.r = r;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (NotificationManagerService.this.mNotificationList) {
                StatusBarNotification n = this.r.sbn;
                if (NotificationManagerService.DBG) {
                    Slog.d(NotificationManagerService.TAG, "EnqueueNotificationRunnable.run for: " + n.getKey());
                }
                NotificationRecord old = (NotificationRecord) NotificationManagerService.this.mNotificationsByKey.get(n.getKey());
                if (old != null) {
                    this.r.copyRankingInformation(old);
                }
                int callingUid = n.getUid();
                int callingPid = n.getInitialPid();
                Notification notification = n.getNotification();
                String pkg = n.getPackageName();
                int id = n.getId();
                String tag = n.getTag();
                boolean isSystemNotification;
                if (NotificationManagerService.isUidSystem(callingUid)) {
                    isSystemNotification = true;
                } else {
                    isSystemNotification = "android".equals(pkg);
                }
                NotificationManagerService.this.handleGroupedNotificationLocked(this.r, old, callingUid, callingPid);
                if (!pkg.equals("com.android.providers.downloads") || Log.isLoggable("DownloadManager", 2)) {
                    int enqueueStatus = 0;
                    if (old != null) {
                        enqueueStatus = 1;
                    }
                    RemoteViews contentView = notification.contentView;
                    RemoteViews bigContentView = notification.bigContentView;
                    int contentViewSize = 0;
                    if (contentView != null) {
                        contentViewSize = contentView.getCacheSize();
                    }
                    int bigContentViewSize = 0;
                    if (bigContentView != null) {
                        bigContentViewSize = bigContentView.getCacheSize();
                    }
                    EventLogTags.writeNotificationEnqueue(callingUid, callingPid, pkg, id, tag, this.userId, notification.toString() + " contentViewSize = " + contentViewSize + " bigContentViewSize = " + bigContentViewSize + " N = " + NotificationManagerService.this.mNotificationList.size(), enqueueStatus);
                }
                NotificationManagerService.this.mRankingHelper.extractSignals(this.r);
                if (NotificationManagerService.this.mNotificationResource != null) {
                    NotificationManagerService.this.mNotificationResource.release(callingUid, pkg, -1);
                }
                boolean isPackageSuspended = NotificationManagerService.this.isPackageSuspendedForUser(pkg, callingUid);
                if ((this.r.getImportance() != 0 && NotificationManagerService.this.noteNotificationOp(pkg, callingUid) && !isPackageSuspended) || r18) {
                    if (NotificationManagerService.this.mRankerServices.isEnabled()) {
                        NotificationManagerService.this.mRankerServices.onNotificationEnqueued(this.r);
                    }
                    NotificationManagerService.this.hwEnqueueNotificationWithTag(pkg, callingUid, this.r);
                    int index = NotificationManagerService.this.indexOfNotificationLocked(n.getKey());
                    if (index < 0) {
                        NotificationManagerService.this.mNotificationList.add(this.r);
                        NotificationManagerService.this.mUsageStats.registerPostedByApp(this.r);
                    } else {
                        old = (NotificationRecord) NotificationManagerService.this.mNotificationList.get(index);
                        NotificationManagerService.this.mNotificationList.set(index, this.r);
                        NotificationManagerService.this.mUsageStats.registerUpdatedByApp(this.r, old);
                        notification.flags |= old.getNotification().flags & 64;
                        this.r.isUpdate = true;
                    }
                    Flog.i(400, "enqueueNotificationInternal: n.getKey = " + n.getKey());
                    NotificationManagerService.this.mNotificationsByKey.put(n.getKey(), this.r);
                    if ((notification.flags & 64) != 0) {
                        notification.flags |= 34;
                    }
                    NotificationManagerService.this.applyZenModeLocked(this.r);
                    NotificationManagerService.this.mRankingHelper.sort(NotificationManagerService.this.mNotificationList);
                    if (notification.getSmallIcon() != null) {
                        StatusBarNotification statusBarNotification = old != null ? old.sbn : null;
                        try {
                            NotificationManagerService.this.mListeners.notifyPostedLocked(n, statusBarNotification);
                            if (statusBarNotification == null) {
                                LogPower.push(122, n.getPackageName(), Integer.toString(n.getId()), n.getOpPkg());
                            }
                        } catch (IllegalArgumentException e) {
                            Log.e(NotificationManagerService.TAG, "notification:" + n + "extras:" + n.getNotification().extras);
                            return;
                        }
                    }
                    Slog.e(NotificationManagerService.TAG, "Not posting notification without small icon: " + notification);
                    if (!(old == null || old.isCanceled)) {
                        NotificationManagerService.this.mListeners.notifyRemovedLocked(n);
                    }
                    Slog.e(NotificationManagerService.TAG, "WARNING: In a future release this will crash the app: " + n.getPackageName());
                    NotificationManagerService.this.buzzBeepBlinkLocked(this.r);
                } else if (isPackageSuspended) {
                    Slog.e(NotificationManagerService.TAG, "Suppressing notification from package due to package suspended by administrator.");
                    NotificationManagerService.this.mUsageStats.registerSuspendedByAdmin(this.r);
                } else {
                    Slog.e(NotificationManagerService.TAG, "Suppressing notification from package by user request.");
                    NotificationManagerService.this.mUsageStats.registerBlocked(this.r);
                    NotificationManagerService.this.detectNotifyBySM(callingUid, pkg, notification);
                }
            }
        }
    }

    public class NotificationListeners extends ManagedServices {
        private final ArraySet<ManagedServiceInfo> mLightTrimListeners = new ArraySet();

        public NotificationListeners() {
            super(NotificationManagerService.this.getContext(), NotificationManagerService.this.mHandler, NotificationManagerService.this.mNotificationList, NotificationManagerService.this.mUserProfiles);
        }

        protected Config getConfig() {
            Config c = new Config();
            c.caption = "notification listener";
            c.serviceInterface = "android.service.notification.NotificationListenerService";
            c.secureSettingName = "enabled_notification_listeners";
            c.bindPermission = "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE";
            c.settingsAction = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
            c.clientLabel = 17040476;
            return c;
        }

        protected IInterface asInterface(IBinder binder) {
            return INotificationListener.Stub.asInterface(binder);
        }

        protected boolean checkType(IInterface service) {
            return service instanceof INotificationListener;
        }

        public void onServiceAdded(ManagedServiceInfo info) {
            INotificationListener listener = info.service;
            synchronized (NotificationManagerService.this.mNotificationList) {
                NotificationRankingUpdate update = NotificationManagerService.this.makeRankingUpdateLocked(info);
            }
            try {
                listener.onListenerConnected(update);
            } catch (RemoteException e) {
            }
        }

        protected void onServiceRemovedLocked(ManagedServiceInfo removed) {
            if (NotificationManagerService.this.removeDisabledHints(removed)) {
                NotificationManagerService.this.updateListenerHintsLocked();
                NotificationManagerService.this.updateEffectsSuppressorLocked();
            }
            this.mLightTrimListeners.remove(removed);
        }

        public void setOnNotificationPostedTrimLocked(ManagedServiceInfo info, int trim) {
            if (trim == 1) {
                this.mLightTrimListeners.add(info);
            } else {
                this.mLightTrimListeners.remove(info);
            }
        }

        public int getOnNotificationPostedTrim(ManagedServiceInfo info) {
            return this.mLightTrimListeners.contains(info) ? 1 : 0;
        }

        public void notifyPostedLocked(StatusBarNotification sbn, StatusBarNotification oldSbn) {
            TrimCache trimCache = new TrimCache(sbn);
            for (final ManagedServiceInfo info : this.mServices) {
                boolean sbnVisible = NotificationManagerService.this.isVisibleToListener(sbn, info);
                boolean oldSbnVisible = oldSbn != null ? NotificationManagerService.this.isVisibleToListener(oldSbn, info) : false;
                if (oldSbnVisible || sbnVisible) {
                    final NotificationRankingUpdate update = NotificationManagerService.this.makeRankingUpdateLocked(info);
                    if (!oldSbnVisible || sbnVisible) {
                        final StatusBarNotification sbnToPost = trimCache.ForListener(info);
                        NotificationManagerService.this.mHandler.post(new Runnable() {
                            public void run() {
                                NotificationListeners.this.notifyPosted(info, sbnToPost, update);
                            }
                        });
                    } else {
                        final StatusBarNotification oldSbnLightClone = oldSbn.cloneLight();
                        NotificationManagerService.this.mHandler.post(new Runnable() {
                            public void run() {
                                NotificationListeners.this.notifyRemoved(info, oldSbnLightClone, update);
                            }
                        });
                    }
                }
            }
        }

        public void notifyRemovedLocked(StatusBarNotification sbn) {
            final StatusBarNotification sbnLight = sbn.cloneLight();
            for (final ManagedServiceInfo info : this.mServices) {
                if (NotificationManagerService.this.isVisibleToListener(sbn, info)) {
                    final NotificationRankingUpdate update = NotificationManagerService.this.makeRankingUpdateLocked(info);
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationListeners.this.notifyRemoved(info, sbnLight, update);
                        }
                    });
                }
            }
        }

        public void notifyRankingUpdateLocked() {
            for (final ManagedServiceInfo serviceInfo : this.mServices) {
                if (serviceInfo.isEnabledForCurrentProfiles()) {
                    final NotificationRankingUpdate update = NotificationManagerService.this.makeRankingUpdateLocked(serviceInfo);
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationListeners.this.notifyRankingUpdate(serviceInfo, update);
                        }
                    });
                }
            }
        }

        public void notifyListenerHintsChangedLocked(final int hints) {
            for (final ManagedServiceInfo serviceInfo : this.mServices) {
                if (serviceInfo.isEnabledForCurrentProfiles()) {
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationListeners.this.notifyListenerHintsChanged(serviceInfo, hints);
                        }
                    });
                }
            }
        }

        public void notifyInterruptionFilterChanged(final int interruptionFilter) {
            for (final ManagedServiceInfo serviceInfo : this.mServices) {
                if (serviceInfo.isEnabledForCurrentProfiles()) {
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationListeners.this.notifyInterruptionFilterChanged(serviceInfo, interruptionFilter);
                        }
                    });
                }
            }
        }

        private void notifyPosted(ManagedServiceInfo info, StatusBarNotification sbn, NotificationRankingUpdate rankingUpdate) {
            INotificationListener listener = info.service;
            try {
                listener.onNotificationPosted(new StatusBarNotificationHolder(sbn), rankingUpdate);
            } catch (RemoteException ex) {
                Log.e(this.TAG, "unable to notify listener (posted): " + listener, ex);
            }
        }

        private void notifyRemoved(ManagedServiceInfo info, StatusBarNotification sbn, NotificationRankingUpdate rankingUpdate) {
            if (info.enabledAndUserMatches(sbn.getUserId())) {
                INotificationListener listener = info.service;
                try {
                    listener.onNotificationRemoved(new StatusBarNotificationHolder(sbn), rankingUpdate);
                    LogPower.push(123, sbn.getPackageName(), Integer.toString(sbn.getId()), sbn.getOpPkg());
                } catch (RemoteException ex) {
                    Log.e(this.TAG, "unable to notify listener (removed): " + listener, ex);
                }
            }
        }

        private void notifyRankingUpdate(ManagedServiceInfo info, NotificationRankingUpdate rankingUpdate) {
            INotificationListener listener = info.service;
            try {
                listener.onNotificationRankingUpdate(rankingUpdate);
            } catch (RemoteException ex) {
                Log.e(this.TAG, "unable to notify listener (ranking update): " + listener, ex);
            }
        }

        private void notifyListenerHintsChanged(ManagedServiceInfo info, int hints) {
            INotificationListener listener = info.service;
            try {
                listener.onListenerHintsChanged(hints);
            } catch (RemoteException ex) {
                Log.e(this.TAG, "unable to notify listener (listener hints): " + listener, ex);
            }
        }

        private void notifyInterruptionFilterChanged(ManagedServiceInfo info, int interruptionFilter) {
            INotificationListener listener = info.service;
            try {
                listener.onInterruptionFilterChanged(interruptionFilter);
            } catch (RemoteException ex) {
                Log.e(this.TAG, "unable to notify listener (interruption filter): " + listener, ex);
            }
        }

        private boolean isListenerPackage(String packageName) {
            if (packageName == null) {
                return false;
            }
            synchronized (NotificationManagerService.this.mNotificationList) {
                for (ManagedServiceInfo serviceInfo : this.mServices) {
                    if (packageName.equals(serviceInfo.component.getPackageName())) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public class NotificationRankers extends ManagedServices {
        public NotificationRankers() {
            super(NotificationManagerService.this.getContext(), NotificationManagerService.this.mHandler, NotificationManagerService.this.mNotificationList, NotificationManagerService.this.mUserProfiles);
        }

        protected Config getConfig() {
            Config c = new Config();
            c.caption = "notification ranker service";
            c.serviceInterface = "android.service.notification.NotificationRankerService";
            c.secureSettingName = null;
            c.bindPermission = "android.permission.BIND_NOTIFICATION_RANKER_SERVICE";
            c.settingsAction = "android.settings.MANAGE_DEFAULT_APPS_SETTINGS";
            c.clientLabel = 17040479;
            return c;
        }

        protected IInterface asInterface(IBinder binder) {
            return INotificationListener.Stub.asInterface(binder);
        }

        protected boolean checkType(IInterface service) {
            return service instanceof INotificationListener;
        }

        protected void onServiceAdded(ManagedServiceInfo info) {
            NotificationManagerService.this.mListeners.registerGuestService(info);
        }

        protected void onServiceRemovedLocked(ManagedServiceInfo removed) {
            NotificationManagerService.this.mListeners.unregisterService(removed.service, removed.userid);
        }

        public void onNotificationEnqueued(NotificationRecord r) {
            StatusBarNotification sbn = r.sbn;
            TrimCache trimCache = new TrimCache(sbn);
            for (final ManagedServiceInfo info : this.mServices) {
                if (NotificationManagerService.this.isVisibleToListener(sbn, info)) {
                    final int importance = r.getImportance();
                    final boolean fromUser = r.isImportanceFromUser();
                    final StatusBarNotification sbnToPost = trimCache.ForListener(info);
                    NotificationManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            NotificationRankers.this.notifyEnqueued(info, sbnToPost, importance, fromUser);
                        }
                    });
                }
            }
        }

        private void notifyEnqueued(ManagedServiceInfo info, StatusBarNotification sbn, int importance, boolean fromUser) {
            INotificationListener ranker = info.service;
            try {
                ranker.onNotificationEnqueued(new StatusBarNotificationHolder(sbn), importance, fromUser);
            } catch (RemoteException ex) {
                Log.e(this.TAG, "unable to notify ranker (enqueued): " + ranker, ex);
            }
        }

        public boolean isEnabled() {
            return !this.mServices.isEmpty();
        }

        public void onUserSwitched(int user) {
            synchronized (NotificationManagerService.this.mNotificationList) {
                int i = this.mServices.size() - 1;
                while (true) {
                    int i2 = i - 1;
                    if (i > 0) {
                        ManagedServiceInfo info = (ManagedServiceInfo) this.mServices.get(i2);
                        unregisterService(info.service, info.userid);
                        i = i2;
                    }
                }
            }
            registerRanker();
        }

        public void onPackagesChanged(boolean queryReplace, String[] pkgList) {
            Object obj = null;
            if (this.DEBUG) {
                String str = this.TAG;
                StringBuilder append = new StringBuilder().append("onPackagesChanged queryReplace=").append(queryReplace).append(" pkgList=");
                if (pkgList != null) {
                    obj = Arrays.asList(pkgList);
                }
                Slog.d(str, append.append(obj).toString());
            }
            if (!(NotificationManagerService.this.mRankerServicePackageName == null || pkgList == null || pkgList.length <= 0)) {
                for (String pkgName : pkgList) {
                    if (NotificationManagerService.this.mRankerServicePackageName.equals(pkgName)) {
                        registerRanker();
                    }
                }
            }
        }

        protected void registerRanker() {
            if (NotificationManagerService.this.mRankerServicePackageName == null) {
                Slog.w(this.TAG, "could not start ranker service: no package specified!");
                return;
            }
            Set<ComponentName> rankerComponents = queryPackageForServices(NotificationManagerService.this.mRankerServicePackageName, 0);
            Iterator<ComponentName> iterator = rankerComponents.iterator();
            if (iterator.hasNext()) {
                ComponentName rankerComponent = (ComponentName) iterator.next();
                if (iterator.hasNext()) {
                    Slog.e(this.TAG, "found multiple ranker services:" + rankerComponents);
                } else {
                    registerSystemService(rankerComponent, 0);
                }
            } else {
                Slog.w(this.TAG, "could not start ranker service: none found");
            }
        }
    }

    private final class PolicyAccess {
        private static final String SEPARATOR = ":";
        private final String[] PERM;

        private PolicyAccess() {
            this.PERM = new String[]{"android.permission.ACCESS_NOTIFICATION_POLICY"};
        }

        public boolean isPackageGranted(String pkg) {
            return pkg != null ? getGrantedPackages().contains(pkg) : false;
        }

        public void put(String pkg, boolean granted) {
            if (pkg != null) {
                boolean changed;
                ArraySet<String> pkgs = getGrantedPackages();
                if (granted) {
                    changed = pkgs.add(pkg);
                } else {
                    changed = pkgs.remove(pkg);
                }
                if (changed) {
                    String setting = TextUtils.join(SEPARATOR, pkgs);
                    int currentUser = ActivityManager.getCurrentUser();
                    Secure.putStringForUser(NotificationManagerService.this.getContext().getContentResolver(), "enabled_notification_policy_access_packages", setting, currentUser);
                    NotificationManagerService.this.getContext().sendBroadcastAsUser(new Intent("android.app.action.NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED").setPackage(pkg).addFlags(1073741824), new UserHandle(currentUser), null);
                }
            }
        }

        public ArraySet<String> getGrantedPackages() {
            ArraySet<String> pkgs = new ArraySet();
            long identity = Binder.clearCallingIdentity();
            try {
                String setting = Secure.getStringForUser(NotificationManagerService.this.getContext().getContentResolver(), "enabled_notification_policy_access_packages", ActivityManager.getCurrentUser());
                if (setting != null) {
                    String[] tokens = setting.split(SEPARATOR);
                    for (String token : tokens) {
                        String token2;
                        if (token2 != null) {
                            token2 = token2.trim();
                        }
                        if (!TextUtils.isEmpty(token2)) {
                            pkgs.add(token2);
                        }
                    }
                }
                Binder.restoreCallingIdentity(identity);
                return pkgs;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public String[] getRequestingPackages() throws RemoteException {
            List<PackageInfo> pkgs = AppGlobals.getPackageManager().getPackagesHoldingPermissions(this.PERM, 0, ActivityManager.getCurrentUser()).getList();
            if (pkgs == null || pkgs.isEmpty()) {
                return new String[0];
            }
            int N = pkgs.size();
            String[] rt = new String[N];
            for (int i = 0; i < N; i++) {
                rt[i] = ((PackageInfo) pkgs.get(i)).packageName;
            }
            return rt;
        }
    }

    private final class PowerSaverObserver extends ContentObserver {
        private final Uri SUPER_POWER_SAVE_NOTIFICATION_URI = Secure.getUriFor(NotificationManagerService.POWER_SAVER_NOTIFICATION_WHITELIST);
        private boolean initObserver = false;

        PowerSaverObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            if (!this.initObserver) {
                this.initObserver = true;
                NotificationManagerService.this.getContext().getContentResolver().registerContentObserver(this.SUPER_POWER_SAVE_NOTIFICATION_URI, false, this, -1);
                update(null);
            }
        }

        void unObserve() {
            this.initObserver = false;
            NotificationManagerService.this.getContext().getContentResolver().unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            if (uri == null || this.SUPER_POWER_SAVE_NOTIFICATION_URI.equals(uri)) {
                NotificationManagerService.this.setNotificationWhiteList();
            }
        }
    }

    private final class RankingHandlerWorker extends Handler implements RankingHandler {
        public RankingHandlerWorker(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    NotificationManagerService.this.handleRankingReconsideration(msg);
                    return;
                case NotificationManagerService.MESSAGE_RANKING_SORT /*1001*/:
                    NotificationManagerService.this.handleRankingSort();
                    return;
                default:
                    return;
            }
        }

        public void requestSort() {
            removeMessages(NotificationManagerService.MESSAGE_RANKING_SORT);
            sendEmptyMessage(NotificationManagerService.MESSAGE_RANKING_SORT);
        }

        public void requestReconsideration(RankingReconsideration recon) {
            sendMessageDelayed(Message.obtain(this, 1000, recon), recon.getDelay(TimeUnit.MILLISECONDS));
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri NOTIFICATION_LIGHT_PULSE_URI = System.getUriFor("notification_light_pulse");
        private final Uri NOTIFICATION_RATE_LIMIT_URI = Global.getUriFor("max_notification_enqueue_rate");

        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = NotificationManagerService.this.getContext().getContentResolver();
            resolver.registerContentObserver(this.NOTIFICATION_LIGHT_PULSE_URI, false, this, -1);
            resolver.registerContentObserver(this.NOTIFICATION_RATE_LIMIT_URI, false, this, -1);
            update(null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            ContentResolver resolver = NotificationManagerService.this.getContext().getContentResolver();
            if (uri == null || this.NOTIFICATION_LIGHT_PULSE_URI.equals(uri)) {
                boolean pulseEnabled = System.getIntForUser(resolver, "notification_light_pulse", 0, -2) != 0;
                if (NotificationManagerService.this.mNotificationPulseEnabled != pulseEnabled) {
                    NotificationManagerService.this.mNotificationPulseEnabled = pulseEnabled;
                    NotificationManagerService.this.updateNotificationPulse();
                }
            }
            if (uri == null || this.NOTIFICATION_RATE_LIMIT_URI.equals(uri)) {
                NotificationManagerService.this.mMaxPackageEnqueueRate = Global.getFloat(resolver, "max_notification_enqueue_rate", NotificationManagerService.this.mMaxPackageEnqueueRate);
            }
        }
    }

    private static final class StatusBarNotificationHolder extends IStatusBarNotificationHolder.Stub {
        private StatusBarNotification mValue;

        public StatusBarNotificationHolder(StatusBarNotification value) {
            this.mValue = value;
        }

        public StatusBarNotification get() {
            StatusBarNotification value = this.mValue;
            this.mValue = null;
            return value;
        }
    }

    private static final class ToastRecord {
        final ITransientNotification callback;
        int duration;
        final int pid;
        final String pkg;
        final String toastStr;

        ToastRecord(int pid, String pkg, ITransientNotification callback, int duration, String toastStr) {
            this.pid = pid;
            this.pkg = pkg;
            this.callback = callback;
            this.duration = duration;
            this.toastStr = toastStr;
        }

        void update(int duration) {
            this.duration = duration;
        }

        void dump(PrintWriter pw, String prefix, DumpFilter filter) {
            if (filter == null || filter.matches(this.pkg)) {
                pw.println(prefix + this);
            }
        }

        public final String toString() {
            return "ToastRecord{" + Integer.toHexString(System.identityHashCode(this)) + " pkg=" + this.pkg + " callback=" + this.callback + " duration=" + this.duration + " toastStr=" + this.toastStr;
        }
    }

    private class TrimCache {
        StatusBarNotification heavy;
        StatusBarNotification sbnClone;
        StatusBarNotification sbnCloneLight;

        TrimCache(StatusBarNotification sbn) {
            this.heavy = sbn;
        }

        StatusBarNotification ForListener(ManagedServiceInfo info) {
            if (NotificationManagerService.this.mListeners.getOnNotificationPostedTrim(info) == 1) {
                if (this.sbnCloneLight == null) {
                    this.sbnCloneLight = this.heavy.cloneLight();
                }
                return this.sbnCloneLight;
            }
            if (this.sbnClone == null) {
                this.sbnClone = this.heavy.clone();
            }
            return this.sbnClone;
        }
    }

    private final class WorkerHandler extends Handler {
        private WorkerHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    NotificationManagerService.this.handleTimeout((ToastRecord) msg.obj);
                    return;
                case 3:
                    new Thread() {
                        public void run() {
                            NotificationManagerService.this.handleSavePolicyFile();
                        }
                    }.start();
                    return;
                case 4:
                    NotificationManagerService.this.handleSendRankingUpdate();
                    return;
                case 5:
                    NotificationManagerService.this.handleListenerHintsChanged(msg.arg1);
                    return;
                case 6:
                    NotificationManagerService.this.handleListenerInterruptionFilterChanged(msg.arg1);
                    return;
                default:
                    return;
            }
        }
    }

    void buzzBeepBlinkLocked(com.android.server.notification.NotificationRecord r41) {
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
        r40 = this;
        r14 = 0;
        r12 = 0;
        r13 = 0;
        r0 = r41;
        r4 = r0.sbn;
        r29 = r4.getNotification();
        r27 = r41.getKey();
        r4 = r41.getImportance();
        r5 = 3;
        if (r4 < r5) goto L_0x025e;
    L_0x0016:
        r10 = 1;
    L_0x0017:
        if (r10 == 0) goto L_0x001f;
    L_0x0019:
        r4 = r41.isIntercepted();
        if (r4 == 0) goto L_0x0261;
    L_0x001f:
        r0 = r41;
        r4 = r0.sbn;
        r4 = r4.getPackageName();
        r0 = r40;
        r15 = r0.inNonDisturbMode(r4);
    L_0x002d:
        r4 = DBG;
        if (r4 != 0) goto L_0x0037;
    L_0x0031:
        r4 = r41.isIntercepted();
        if (r4 == 0) goto L_0x0073;
    L_0x0037:
        r4 = "NotificationService";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "pkg=";
        r5 = r5.append(r6);
        r0 = r41;
        r6 = r0.sbn;
        r6 = r6.getPackageName();
        r5 = r5.append(r6);
        r6 = " canInterrupt=";
        r5 = r5.append(r6);
        r5 = r5.append(r15);
        r6 = " intercept=";
        r5 = r5.append(r6);
        r6 = r41.isIntercepted();
        r5 = r5.append(r6);
        r5 = r5.toString();
        android.util.Slog.v(r4, r5);
    L_0x0073:
        r34 = android.os.Binder.clearCallingIdentity();
        r17 = android.app.ActivityManager.getCurrentUser();	 Catch:{ all -> 0x0264 }
        android.os.Binder.restoreCallingIdentity(r34);
        r18 = r40.disableNotificationEffects(r41);
        if (r18 == 0) goto L_0x008b;
    L_0x0084:
        r0 = r41;
        r1 = r18;
        com.android.server.notification.ZenLog.traceDisableEffects(r0, r1);
    L_0x008b:
        if (r27 == 0) goto L_0x0269;
    L_0x008d:
        r0 = r40;
        r4 = r0.mSoundNotificationKey;
        r0 = r27;
        r37 = r0.equals(r4);
    L_0x0097:
        if (r27 == 0) goto L_0x026d;
    L_0x0099:
        r0 = r40;
        r4 = r0.mVibrateNotificationKey;
        r0 = r27;
        r38 = r0.equals(r4);
    L_0x00a3:
        r22 = 0;
        r21 = 0;
        if (r18 == 0) goto L_0x00b5;
    L_0x00a9:
        r0 = r40;
        r1 = r18;
        r2 = r41;
        r4 = r0.allowNotificationsInCall(r1, r2);
        if (r4 == 0) goto L_0x027f;
    L_0x00b5:
        r4 = r41.getUserId();
        r5 = -1;
        if (r4 == r5) goto L_0x00c4;
    L_0x00bc:
        r4 = r41.getUserId();
        r0 = r17;
        if (r4 != r0) goto L_0x0271;
    L_0x00c4:
        if (r15 == 0) goto L_0x027f;
    L_0x00c6:
        r0 = r40;
        r4 = r0.mSystemReady;
        if (r4 == 0) goto L_0x027f;
    L_0x00cc:
        r0 = r40;
        r4 = r0.mAudioManager;
        if (r4 == 0) goto L_0x027f;
    L_0x00d2:
        r4 = DBG;
        if (r4 == 0) goto L_0x00df;
    L_0x00d6:
        r4 = "NotificationService";
        r5 = "Interrupting!";
        android.util.Slog.v(r4, r5);
    L_0x00df:
        r0 = r29;
        r4 = r0.defaults;
        r4 = r4 & 1;
        if (r4 != 0) goto L_0x02ca;
    L_0x00e7:
        r4 = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI;
        r0 = r29;
        r5 = r0.sound;
        r33 = r4.equals(r5);
    L_0x00f1:
        r32 = 0;
        if (r33 == 0) goto L_0x02d2;
    L_0x00f5:
        r32 = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI;
        r4 = r40.getContext();
        r31 = r4.getContentResolver();
        r4 = "notification_sound";
        r0 = r31;
        r4 = android.provider.Settings.System.getString(r0, r4);
        if (r4 == 0) goto L_0x02ce;
    L_0x010a:
        r21 = 1;
    L_0x010c:
        r0 = r29;
        r4 = r0.vibrate;
        if (r4 == 0) goto L_0x02e8;
    L_0x0112:
        r20 = 1;
    L_0x0114:
        r0 = r41;
        r4 = r0.sbn;
        r4 = r4.getPackageName();
        r5 = r41.getUserId();
        r0 = r40;
        r26 = r0.isHwSoundAllow(r4, r5);
        if (r26 != 0) goto L_0x02ec;
    L_0x0128:
        r0 = r40;
        r4 = r0.mAudioManager;
        r4 = r4.getRingerModeInternal();
        r5 = 2;
        if (r4 != r5) goto L_0x02ec;
    L_0x0133:
        r23 = 1;
    L_0x0135:
        if (r20 != 0) goto L_0x02f4;
    L_0x0137:
        if (r21 == 0) goto L_0x02f4;
    L_0x0139:
        r0 = r40;
        r4 = r0.mAudioManager;
        r4 = r4.getRingerModeInternal();
        r5 = 1;
        if (r4 == r5) goto L_0x02f0;
    L_0x0144:
        r16 = r23;
    L_0x0146:
        if (r21 == 0) goto L_0x02f8;
    L_0x0148:
        r21 = r26;
    L_0x014a:
        r0 = r29;
        r4 = r0.defaults;
        r4 = r4 & 2;
        if (r4 == 0) goto L_0x02fc;
    L_0x0152:
        r36 = 1;
    L_0x0154:
        if (r36 != 0) goto L_0x0300;
    L_0x0156:
        if (r16 != 0) goto L_0x0300;
    L_0x0158:
        r22 = r20;
    L_0x015a:
        if (r22 == 0) goto L_0x0304;
    L_0x015c:
        r0 = r41;
        r4 = r0.sbn;
        r4 = r4.getPackageName();
        r5 = r41.getUserId();
        r0 = r40;
        r22 = r0.isHwVibrateAllow(r4, r5);
    L_0x016e:
        r0 = r41;
        r4 = r0.isUpdate;
        if (r4 == 0) goto L_0x030b;
    L_0x0174:
        r0 = r29;
        r4 = r0.flags;
        r4 = r4 & 8;
        if (r4 == 0) goto L_0x0308;
    L_0x017c:
        r4 = 1;
    L_0x017d:
        if (r4 != 0) goto L_0x0204;
    L_0x017f:
        r0 = r41;
        r4 = r0.sbn;
        r4 = r4.getPackageName();
        r0 = r40;
        r1 = r29;
        r0.sendAccessibilityEvent(r1, r4);
        if (r21 == 0) goto L_0x01bc;
    L_0x0190:
        r0 = r29;
        r4 = r0.flags;
        r4 = r4 & 4;
        if (r4 == 0) goto L_0x030e;
    L_0x0198:
        r28 = 1;
    L_0x019a:
        r11 = audioAttributesForNotification(r29);
        r0 = r27;
        r1 = r40;
        r1.mSoundNotificationKey = r0;
        r0 = r40;
        r4 = r0.mAudioManager;
        r5 = android.media.AudioAttributes.toLegacyStreamType(r11);
        r4 = r4.getStreamVolume(r5);
        if (r4 == 0) goto L_0x01bc;
    L_0x01b2:
        r0 = r40;
        r4 = r0.mAudioManager;
        r4 = r4.isAudioFocusExclusive();
        if (r4 == 0) goto L_0x0312;
    L_0x01bc:
        if (r22 == 0) goto L_0x0204;
    L_0x01be:
        r0 = r40;
        r4 = r0.mAudioManager;
        r4 = r4.getRingerModeInternal();
        if (r4 == 0) goto L_0x0204;
    L_0x01c8:
        r0 = r27;
        r1 = r40;
        r1.mVibrateNotificationKey = r0;
        if (r36 != 0) goto L_0x01d2;
    L_0x01d0:
        if (r16 == 0) goto L_0x037b;
    L_0x01d2:
        r24 = android.os.Binder.clearCallingIdentity();
        r0 = r40;	 Catch:{ all -> 0x0376 }
        r4 = r0.mVibrator;	 Catch:{ all -> 0x0376 }
        r0 = r41;	 Catch:{ all -> 0x0376 }
        r5 = r0.sbn;	 Catch:{ all -> 0x0376 }
        r5 = r5.getUid();	 Catch:{ all -> 0x0376 }
        r0 = r41;	 Catch:{ all -> 0x0376 }
        r6 = r0.sbn;	 Catch:{ all -> 0x0376 }
        r6 = r6.getOpPkg();	 Catch:{ all -> 0x0376 }
        if (r36 == 0) goto L_0x036d;	 Catch:{ all -> 0x0376 }
    L_0x01ec:
        r0 = r40;	 Catch:{ all -> 0x0376 }
        r7 = r0.mDefaultVibrationPattern;	 Catch:{ all -> 0x0376 }
    L_0x01f0:
        r0 = r29;	 Catch:{ all -> 0x0376 }
        r8 = r0.flags;	 Catch:{ all -> 0x0376 }
        r8 = r8 & 4;	 Catch:{ all -> 0x0376 }
        if (r8 == 0) goto L_0x0373;	 Catch:{ all -> 0x0376 }
    L_0x01f8:
        r8 = 0;	 Catch:{ all -> 0x0376 }
    L_0x01f9:
        r9 = audioAttributesForNotification(r29);	 Catch:{ all -> 0x0376 }
        r4.vibrate(r5, r6, r7, r8, r9);	 Catch:{ all -> 0x0376 }
        r14 = 1;
        android.os.Binder.restoreCallingIdentity(r24);
    L_0x0204:
        if (r37 == 0) goto L_0x0208;
    L_0x0206:
        if (r21 == 0) goto L_0x03b3;
    L_0x0208:
        if (r38 == 0) goto L_0x020c;
    L_0x020a:
        if (r22 == 0) goto L_0x03b8;
    L_0x020c:
        r0 = r40;
        r4 = r0.mLights;
        r0 = r27;
        r39 = r4.remove(r0);
        r0 = r29;
        r4 = r0.flags;
        r4 = r4 & 1;
        if (r4 == 0) goto L_0x03bd;
    L_0x021e:
        if (r15 == 0) goto L_0x03bd;
    L_0x0220:
        r4 = r41.getSuppressedVisualEffects();
        r4 = r4 & 1;
        if (r4 != 0) goto L_0x03bd;
    L_0x0228:
        r0 = r40;
        r4 = r0.mLights;
        r0 = r27;
        r4.add(r0);
        r40.updateLightsLocked();
        r0 = r40;
        r4 = r0.mUseAttentionLight;
        if (r4 == 0) goto L_0x0241;
    L_0x023a:
        r0 = r40;
        r4 = r0.mAttentionLight;
        r4.pulse();
    L_0x0241:
        r13 = 1;
    L_0x0242:
        if (r14 != 0) goto L_0x0248;
    L_0x0244:
        if (r12 != 0) goto L_0x0248;
    L_0x0246:
        if (r13 == 0) goto L_0x025d;
    L_0x0248:
        r4 = r41.getSuppressedVisualEffects();
        r4 = r4 & 1;
        if (r4 == 0) goto L_0x03c4;
    L_0x0250:
        r4 = DBG;
        if (r4 == 0) goto L_0x025d;
    L_0x0254:
        r4 = "NotificationService";
        r5 = "Suppressed SystemUI from triggering screen on";
        android.util.Slog.v(r4, r5);
    L_0x025d:
        return;
    L_0x025e:
        r10 = 0;
        goto L_0x0017;
    L_0x0261:
        r15 = 1;
        goto L_0x002d;
    L_0x0264:
        r4 = move-exception;
        android.os.Binder.restoreCallingIdentity(r34);
        throw r4;
    L_0x0269:
        r37 = 0;
        goto L_0x0097;
    L_0x026d:
        r38 = 0;
        goto L_0x00a3;
    L_0x0271:
        r0 = r40;
        r4 = r0.mUserProfiles;
        r5 = r41.getUserId();
        r4 = r4.isCurrentProfile(r5);
        if (r4 != 0) goto L_0x00c4;
    L_0x027f:
        r4 = DBG;
        if (r4 != 0) goto L_0x0287;
    L_0x0283:
        r4 = android.util.Log.HWINFO;
        if (r4 == 0) goto L_0x0204;
    L_0x0287:
        r5 = "NotificationService";
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r6 = "disableEffects=";
        r4 = r4.append(r6);
        r0 = r18;
        r4 = r4.append(r0);
        r6 = " canInterrupt=";
        r4 = r4.append(r6);
        r4 = r4.append(r15);
        r6 = " once update: ";
        r6 = r4.append(r6);
        r0 = r41;
        r4 = r0.isUpdate;
        if (r4 == 0) goto L_0x03b0;
    L_0x02b4:
        r0 = r29;
        r4 = r0.flags;
        r4 = r4 & 8;
        if (r4 == 0) goto L_0x03b0;
    L_0x02bc:
        r4 = 1;
    L_0x02bd:
        r4 = r6.append(r4);
        r4 = r4.toString();
        android.util.Slog.v(r5, r4);
        goto L_0x0204;
    L_0x02ca:
        r33 = 1;
        goto L_0x00f1;
    L_0x02ce:
        r21 = 0;
        goto L_0x010c;
    L_0x02d2:
        r0 = r29;
        r4 = r0.sound;
        if (r4 == 0) goto L_0x010c;
    L_0x02d8:
        r0 = r29;
        r0 = r0.sound;
        r32 = r0;
        if (r32 == 0) goto L_0x02e4;
    L_0x02e0:
        r21 = 1;
        goto L_0x010c;
    L_0x02e4:
        r21 = 0;
        goto L_0x010c;
    L_0x02e8:
        r20 = 0;
        goto L_0x0114;
    L_0x02ec:
        r23 = 0;
        goto L_0x0135;
    L_0x02f0:
        r16 = 1;
        goto L_0x0146;
    L_0x02f4:
        r16 = 0;
        goto L_0x0146;
    L_0x02f8:
        r21 = 0;
        goto L_0x014a;
    L_0x02fc:
        r36 = 0;
        goto L_0x0154;
    L_0x0300:
        r22 = 1;
        goto L_0x015a;
    L_0x0304:
        r22 = 0;
        goto L_0x016e;
    L_0x0308:
        r4 = 0;
        goto L_0x017d;
    L_0x030b:
        r4 = 0;
        goto L_0x017d;
    L_0x030e:
        r28 = 0;
        goto L_0x019a;
    L_0x0312:
        r24 = android.os.Binder.clearCallingIdentity();
        r0 = r40;	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        r4 = r0.mAudioManager;	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        r30 = r4.getRingtonePlayer();	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        if (r30 == 0) goto L_0x035d;	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
    L_0x0320:
        r4 = DBG;	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        if (r4 == 0) goto L_0x034b;	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
    L_0x0324:
        r4 = "NotificationService";	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        r5 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        r5.<init>();	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        r6 = "Playing sound ";	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        r5 = r5.append(r6);	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        r0 = r32;	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        r5 = r5.append(r0);	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        r6 = " with attributes ";	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        r5 = r5.append(r6);	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        r5 = r5.append(r11);	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        r5 = r5.toString();	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        android.util.Slog.v(r4, r5);	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
    L_0x034b:
        r0 = r41;	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        r4 = r0.sbn;	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        r4 = r4.getUser();	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        r0 = r30;	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        r1 = r32;	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        r2 = r28;	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        r0.playAsync(r1, r4, r2, r11);	 Catch:{ RemoteException -> 0x0362, all -> 0x0368 }
        r12 = 1;
    L_0x035d:
        android.os.Binder.restoreCallingIdentity(r24);
        goto L_0x01bc;
    L_0x0362:
        r19 = move-exception;
        android.os.Binder.restoreCallingIdentity(r24);
        goto L_0x01bc;
    L_0x0368:
        r4 = move-exception;
        android.os.Binder.restoreCallingIdentity(r24);
        throw r4;
    L_0x036d:
        r0 = r40;	 Catch:{ all -> 0x0376 }
        r7 = r0.mFallbackVibrationPattern;	 Catch:{ all -> 0x0376 }
        goto L_0x01f0;
    L_0x0373:
        r8 = -1;
        goto L_0x01f9;
    L_0x0376:
        r4 = move-exception;
        android.os.Binder.restoreCallingIdentity(r24);
        throw r4;
    L_0x037b:
        r0 = r29;
        r4 = r0.vibrate;
        r4 = r4.length;
        r5 = 1;
        if (r4 <= r5) goto L_0x0204;
    L_0x0383:
        r0 = r40;
        r4 = r0.mVibrator;
        r0 = r41;
        r5 = r0.sbn;
        r5 = r5.getUid();
        r0 = r41;
        r6 = r0.sbn;
        r6 = r6.getOpPkg();
        r0 = r29;
        r7 = r0.vibrate;
        r0 = r29;
        r8 = r0.flags;
        r8 = r8 & 4;
        if (r8 == 0) goto L_0x03ae;
    L_0x03a3:
        r8 = 0;
    L_0x03a4:
        r9 = audioAttributesForNotification(r29);
        r4.vibrate(r5, r6, r7, r8, r9);
        r14 = 1;
        goto L_0x0204;
    L_0x03ae:
        r8 = -1;
        goto L_0x03a4;
    L_0x03b0:
        r4 = 0;
        goto L_0x02bd;
    L_0x03b3:
        r40.clearSoundLocked();
        goto L_0x0208;
    L_0x03b8:
        r40.clearVibrateLocked();
        goto L_0x020c;
    L_0x03bd:
        if (r39 == 0) goto L_0x0242;
    L_0x03bf:
        r40.updateLightsLocked();
        goto L_0x0242;
    L_0x03c4:
        if (r14 == 0) goto L_0x03e1;
    L_0x03c6:
        r4 = 1;
        r6 = r4;
    L_0x03c8:
        if (r12 == 0) goto L_0x03e4;
    L_0x03ca:
        r4 = 1;
        r5 = r4;
    L_0x03cc:
        if (r13 == 0) goto L_0x03e7;
    L_0x03ce:
        r4 = 1;
    L_0x03cf:
        r0 = r27;
        com.android.server.EventLogTags.writeNotificationAlert(r0, r6, r5, r4);
        r0 = r40;
        r4 = r0.mHandler;
        r0 = r40;
        r5 = r0.mBuzzBeepBlinked;
        r4.post(r5);
        goto L_0x025d;
    L_0x03e1:
        r4 = 0;
        r6 = r4;
        goto L_0x03c8;
    L_0x03e4:
        r4 = 0;
        r5 = r4;
        goto L_0x03cc;
    L_0x03e7:
        r4 = 0;
        goto L_0x03cf;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.notification.NotificationManagerService.buzzBeepBlinkLocked(com.android.server.notification.NotificationRecord):void");
    }

    private void readPolicyXml(InputStream stream, boolean forRestore) throws XmlPullParserException, NumberFormatException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(stream, StandardCharsets.UTF_8.name());
        while (parser.next() != 1) {
            this.mZenModeHelper.readXml(parser, forRestore);
            this.mRankingHelper.readXml(parser, forRestore);
        }
    }

    private void loadPolicyFile() {
        if (DBG) {
            Slog.d(TAG, "loadPolicyFile");
        }
        synchronized (this.mPolicyFile) {
            AutoCloseable autoCloseable = null;
            try {
                autoCloseable = this.mPolicyFile.openRead();
                readPolicyXml(autoCloseable, false);
                IoUtils.closeQuietly(autoCloseable);
            } catch (FileNotFoundException e) {
                IoUtils.closeQuietly(autoCloseable);
            } catch (IOException e2) {
                Log.wtf(TAG, "Unable to read notification policy", e2);
                IoUtils.closeQuietly(autoCloseable);
            } catch (NumberFormatException e3) {
                Log.wtf(TAG, "Unable to parse notification policy", e3);
                IoUtils.closeQuietly(autoCloseable);
            } catch (XmlPullParserException e4) {
                Log.wtf(TAG, "Unable to parse notification policy", e4);
                IoUtils.closeQuietly(autoCloseable);
            } catch (Throwable th) {
                IoUtils.closeQuietly(autoCloseable);
            }
        }
    }

    public void savePolicyFile() {
        this.mHandler.removeMessages(3);
        this.mHandler.sendEmptyMessage(3);
    }

    private void handleSavePolicyFile() {
        if (DBG) {
            Slog.d(TAG, "handleSavePolicyFile");
        }
        synchronized (this.mPolicyFile) {
            try {
                FileOutputStream stream = this.mPolicyFile.startWrite();
                try {
                    writePolicyXml(stream, false);
                    this.mPolicyFile.finishWrite(stream);
                } catch (IOException e) {
                    Slog.w(TAG, "Failed to save policy file, restoring backup", e);
                    this.mPolicyFile.failWrite(stream);
                }
            } catch (IOException e2) {
                Slog.w(TAG, "Failed to save policy file", e2);
                return;
            }
        }
        BackupManager.dataChanged(getContext().getPackageName());
    }

    private void writePolicyXml(OutputStream stream, boolean forBackup) throws IOException {
        XmlSerializer out = new FastXmlSerializer();
        out.setOutput(stream, StandardCharsets.UTF_8.name());
        out.startDocument(null, Boolean.valueOf(true));
        out.startTag(null, TAG_NOTIFICATION_POLICY);
        out.attribute(null, ATTR_VERSION, Integer.toString(1));
        this.mZenModeHelper.writeXml(out, forBackup);
        this.mRankingHelper.writeXml(out, forBackup);
        out.endTag(null, TAG_NOTIFICATION_POLICY);
        out.endDocument();
    }

    private boolean noteNotificationOp(String pkg, int uid) {
        if (this.mAppOps.noteOpNoThrow(11, uid, pkg) == 0) {
            return true;
        }
        Slog.v(TAG, "notifications are disabled by AppOps for " + pkg);
        return false;
    }

    private boolean checkNotificationOp(String pkg, int uid) {
        if (this.mAppOps.checkOp(11, uid, pkg) != 0 || isPackageSuspendedForUser(pkg, uid)) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void clearSoundLocked() {
        this.mSoundNotificationKey = null;
        long identity = Binder.clearCallingIdentity();
        try {
            IRingtonePlayer player = this.mAudioManager.getRingtonePlayer();
            if (player != null) {
                player.stopAsync();
            }
            Binder.restoreCallingIdentity(identity);
        } catch (RemoteException e) {
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void clearVibrateLocked() {
        this.mVibrateNotificationKey = null;
        long identity = Binder.clearCallingIdentity();
        try {
            this.mVibrator.cancel();
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void clearLightsLocked() {
        long token = Binder.clearCallingIdentity();
        try {
            int currentUser = ActivityManager.getCurrentUser();
            int i = this.mLights.size();
            while (i > 0) {
                i--;
                String owner = (String) this.mLights.get(i);
                NotificationRecord ledNotification = (NotificationRecord) this.mNotificationsByKey.get(owner);
                Slog.d(TAG, "clearEffects :" + owner);
                if (ledNotification == null) {
                    Slog.wtfStack(TAG, "LED Notification does not exist: " + owner);
                    this.mLights.remove(owner);
                } else {
                    if (!(ledNotification.getUser().getIdentifier() == currentUser || ledNotification.getUser().getIdentifier() == -1)) {
                        if (isAFWUserId(ledNotification.getUser().getIdentifier())) {
                        }
                    }
                    Slog.d(TAG, "clearEffects CurrentUser AFWuser or AllUser :" + owner);
                    this.mLights.remove(owner);
                }
            }
            updateLightsLocked();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    static long[] getLongArray(Resources r, int resid, int maxlen, long[] def) {
        int[] ar = r.getIntArray(resid);
        if (ar == null) {
            return def;
        }
        int len = ar.length > maxlen ? maxlen : ar.length;
        long[] out = new long[len];
        for (int i = 0; i < len; i++) {
            out[i] = (long) ar[i];
        }
        return out;
    }

    public NotificationManagerService(Context context) {
        super(context);
    }

    void setAudioManager(AudioManager audioMananger) {
        this.mAudioManager = audioMananger;
    }

    void setVibrator(Vibrator vibrator) {
        this.mVibrator = vibrator;
    }

    void setSystemReady(boolean systemReady) {
        this.mSystemReady = systemReady;
    }

    void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    public void onStart() {
        String[] extractorNames;
        Resources resources = getContext().getResources();
        this.mMaxPackageEnqueueRate = Global.getFloat(getContext().getContentResolver(), "max_notification_enqueue_rate", DEFAULT_MAX_NOTIFICATION_ENQUEUE_RATE);
        this.mAm = ActivityManagerNative.getDefault();
        this.mAppOps = (AppOpsManager) getContext().getSystemService("appops");
        this.mVibrator = (Vibrator) getContext().getSystemService("vibrator");
        this.mAppUsageStats = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
        this.mRankerServicePackageName = getContext().getPackageManager().getServicesSystemSharedLibraryPackageName();
        NotificationManagerService notificationManagerService = this;
        this.mHandler = new WorkerHandler();
        this.mRankingThread.start();
        try {
            extractorNames = resources.getStringArray(17236026);
        } catch (NotFoundException e) {
            extractorNames = new String[0];
        }
        this.mUsageStats = new NotificationUsageStats(getContext());
        this.mRankingHandler = new RankingHandlerWorker(this.mRankingThread.getLooper());
        this.mRankingHelper = new RankingHelper(getContext(), this.mRankingHandler, this.mUsageStats, extractorNames);
        this.mConditionProviders = new ConditionProviders(getContext(), this.mHandler, this.mUserProfiles);
        this.mZenModeHelper = new ZenModeHelper(getContext(), this.mHandler.getLooper(), this.mConditionProviders);
        this.mZenModeHelper.addCallback(new Callback() {
            public void onConfigChanged() {
                NotificationManagerService.this.savePolicyFile();
            }

            void onZenModeChanged() {
                NotificationManagerService.this.sendRegisteredOnlyBroadcast("android.app.action.INTERRUPTION_FILTER_CHANGED");
                NotificationManagerService.this.getContext().sendBroadcastAsUser(new Intent("android.app.action.INTERRUPTION_FILTER_CHANGED_INTERNAL").addFlags(67108864), UserHandle.ALL, "android.permission.MANAGE_NOTIFICATIONS");
                synchronized (NotificationManagerService.this.mNotificationList) {
                    NotificationManagerService.this.updateInterruptionFilterLocked();
                }
            }

            void onPolicyChanged() {
                NotificationManagerService.this.sendRegisteredOnlyBroadcast("android.app.action.NOTIFICATION_POLICY_CHANGED");
            }
        });
        this.mPolicyFile = new AtomicFile(new File(new File(Environment.getDataDirectory(), "system"), "notification_policy.xml"));
        syncBlockDb();
        this.mListeners = new NotificationListeners();
        this.mRankerServices = new NotificationRankers();
        this.mRankerServices.registerRanker();
        this.mStatusBar = (StatusBarManagerInternal) getLocalService(StatusBarManagerInternal.class);
        if (this.mStatusBar != null) {
            this.mStatusBar.setNotificationDelegate(this.mNotificationDelegate);
        }
        LightsManager lights = (LightsManager) getLocalService(LightsManager.class);
        this.mNotificationLight = lights.getLight(4);
        this.mAttentionLight = lights.getLight(5);
        this.mDefaultNotificationColor = resources.getColor(17170666);
        this.mDefaultNotificationLedOn = resources.getInteger(17694809);
        this.mDefaultNotificationLedOff = resources.getInteger(17694810);
        this.mDefaultVibrationPattern = getLongArray(resources, 17236022, 17, DEFAULT_VIBRATE_PATTERN);
        this.mFallbackVibrationPattern = getLongArray(resources, 17236023, 17, DEFAULT_VIBRATE_PATTERN);
        this.mUseAttentionLight = resources.getBoolean(17956904);
        if (Global.getInt(getContext().getContentResolver(), "device_provisioned", 0) == 0) {
            this.mDisableNotificationEffects = true;
        }
        this.mZenModeHelper.initZenMode();
        this.mInterruptionFilter = this.mZenModeHelper.getZenModeListenerInterruptionFilter();
        this.mUserProfiles.updateCache(getContext());
        listenForCallState();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.PHONE_STATE");
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        filter.addAction("android.intent.action.USER_PRESENT");
        filter.addAction("android.intent.action.USER_STOPPED");
        filter.addAction("android.intent.action.USER_SWITCHED");
        filter.addAction("android.intent.action.USER_ADDED");
        filter.addAction("android.intent.action.USER_REMOVED");
        filter.addAction("android.intent.action.USER_UNLOCKED");
        filter.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
        getContext().registerReceiver(this.mIntentReceiver, filter);
        IntentFilter powerFilter = new IntentFilter();
        powerFilter.addAction(ACTION_HWSYSTEMMANAGER_CHANGE_POWERMODE);
        powerFilter.addAction(ACTION_HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE);
        getContext().registerReceiver(this.powerReceiver, powerFilter, PERMISSION, null);
        IntentFilter pkgFilter = new IntentFilter();
        pkgFilter.addAction("android.intent.action.PACKAGE_ADDED");
        pkgFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        pkgFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        pkgFilter.addAction("android.intent.action.PACKAGE_RESTARTED");
        pkgFilter.addAction("android.intent.action.QUERY_PACKAGE_RESTART");
        pkgFilter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
        getContext().registerReceiverAsUser(this.mPackageIntentReceiver, UserHandle.ALL, pkgFilter, null, null);
        IntentFilter suspendedPkgFilter = new IntentFilter();
        suspendedPkgFilter.addAction("android.intent.action.PACKAGES_SUSPENDED");
        getContext().registerReceiverAsUser(this.mPackageIntentReceiver, UserHandle.ALL, suspendedPkgFilter, null, null);
        getContext().registerReceiverAsUser(this.mPackageIntentReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE"), null, null);
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mArchive = new Archive(resources.getInteger(17694816));
        publishBinderService("notification", this.mService);
        publishLocalService(NotificationManagerInternal.class, this.mInternalService);
    }

    private void sendRegisteredOnlyBroadcast(String action) {
        getContext().sendBroadcastAsUser(new Intent(action).addFlags(1073741824), UserHandle.ALL, null);
    }

    private void syncBlockDb() {
        loadPolicyFile();
        Map<Integer, String> packageBans = this.mRankingHelper.getPackageBans();
        for (Entry<Integer, String> ban : packageBans.entrySet()) {
            setNotificationsEnabledForPackageImpl((String) ban.getValue(), ((Integer) ban.getKey()).intValue(), false);
        }
        packageBans.clear();
        for (UserInfo user : UserManager.get(getContext()).getUsers()) {
            int userId = user.getUserHandle().getIdentifier();
            PackageManager packageManager = getContext().getPackageManager();
            List<PackageInfo> packages = packageManager.getInstalledPackagesAsUser(0, userId);
            int packageCount = packages.size();
            for (int p = 0; p < packageCount; p++) {
                String packageName = ((PackageInfo) packages.get(p)).packageName;
                try {
                    int uid = packageManager.getPackageUidAsUser(packageName, userId);
                    if (!checkNotificationOp(packageName, uid)) {
                        packageBans.put(Integer.valueOf(uid), packageName);
                    }
                } catch (NameNotFoundException e) {
                }
            }
        }
        for (Entry<Integer, String> ban2 : packageBans.entrySet()) {
            this.mRankingHelper.setImportance((String) ban2.getValue(), ((Integer) ban2.getKey()).intValue(), 0);
        }
        savePolicyFile();
    }

    public void onBootPhase(int phase) {
        if (phase == SystemService.PHASE_SYSTEM_SERVICES_READY) {
            this.mSystemReady = true;
            this.mAudioManager = (AudioManager) getContext().getSystemService("audio");
            this.mAudioManagerInternal = (AudioManagerInternal) getLocalService(AudioManagerInternal.class);
            this.mVrManagerInternal = (VrManagerInternal) getLocalService(VrManagerInternal.class);
            this.mZenModeHelper.onSystemReady();
        } else if (phase == 600) {
            this.mSettingsObserver.observe();
            this.mListeners.onBootPhaseAppsCanStart();
            this.mRankerServices.onBootPhaseAppsCanStart();
            this.mConditionProviders.onBootPhaseAppsCanStart();
        }
    }

    void setNotificationsEnabledForPackageImpl(String pkg, int uid, boolean enabled) {
        int i;
        Slog.v(TAG, (enabled ? "en" : "dis") + "abling notifications for " + pkg);
        AppOpsManager appOpsManager = this.mAppOps;
        if (enabled) {
            i = 0;
        } else {
            i = 1;
        }
        appOpsManager.setMode(11, uid, pkg, i);
        if (!enabled) {
            cancelAllNotificationsInt(MY_UID, MY_PID, pkg, 0, 0, true, UserHandle.getUserId(uid), 7, null);
        }
    }

    private void updateListenerHintsLocked() {
        int hints = calculateHints();
        if (hints != this.mListenerHints) {
            ZenLog.traceListenerHintsChanged(this.mListenerHints, hints, this.mEffectsSuppressors.size());
            this.mListenerHints = hints;
            scheduleListenerHintsChanged(hints);
        }
    }

    private void updateEffectsSuppressorLocked() {
        long updatedSuppressedEffects = calculateSuppressedEffects();
        if (updatedSuppressedEffects != this.mZenModeHelper.getSuppressedEffects()) {
            List<ComponentName> suppressors = getSuppressors();
            ZenLog.traceEffectsSuppressorChanged(this.mEffectsSuppressors, suppressors, updatedSuppressedEffects);
            this.mEffectsSuppressors = suppressors;
            this.mZenModeHelper.setSuppressedEffects(updatedSuppressedEffects);
            sendRegisteredOnlyBroadcast("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED");
        }
    }

    private ArrayList<ComponentName> getSuppressors() {
        ArrayList<ComponentName> names = new ArrayList();
        for (int i = this.mListenersDisablingEffects.size() - 1; i >= 0; i--) {
            for (ManagedServiceInfo info : (ArraySet) this.mListenersDisablingEffects.valueAt(i)) {
                names.add(info.component);
            }
        }
        return names;
    }

    private boolean removeDisabledHints(ManagedServiceInfo info) {
        return removeDisabledHints(info, 0);
    }

    private boolean removeDisabledHints(ManagedServiceInfo info, int hints) {
        boolean removed = false;
        for (int i = this.mListenersDisablingEffects.size() - 1; i >= 0; i--) {
            int hint = this.mListenersDisablingEffects.keyAt(i);
            ArraySet<ManagedServiceInfo> listeners = (ArraySet) this.mListenersDisablingEffects.valueAt(i);
            if (hints == 0 || (hint & hints) == hint) {
                if (removed) {
                    removed = true;
                } else {
                    removed = listeners.remove(info);
                }
            }
        }
        return removed;
    }

    private void addDisabledHints(ManagedServiceInfo info, int hints) {
        if ((hints & 1) != 0) {
            addDisabledHint(info, 1);
        }
        if ((hints & 2) != 0) {
            addDisabledHint(info, 2);
        }
        if ((hints & 4) != 0) {
            addDisabledHint(info, 4);
        }
    }

    private void addDisabledHint(ManagedServiceInfo info, int hint) {
        if (this.mListenersDisablingEffects.indexOfKey(hint) < 0) {
            this.mListenersDisablingEffects.put(hint, new ArraySet());
        }
        ((ArraySet) this.mListenersDisablingEffects.get(hint)).add(info);
    }

    private int calculateHints() {
        int hints = 0;
        for (int i = this.mListenersDisablingEffects.size() - 1; i >= 0; i--) {
            int hint = this.mListenersDisablingEffects.keyAt(i);
            if (!((ArraySet) this.mListenersDisablingEffects.valueAt(i)).isEmpty()) {
                hints |= hint;
            }
        }
        return hints;
    }

    private long calculateSuppressedEffects() {
        int hints = calculateHints();
        long suppressedEffects = 0;
        if ((hints & 1) != 0) {
            suppressedEffects = 3;
        }
        if ((hints & 2) != 0) {
            suppressedEffects |= 1;
        }
        if ((hints & 4) != 0) {
            return suppressedEffects | 2;
        }
        return suppressedEffects;
    }

    private void updateInterruptionFilterLocked() {
        int interruptionFilter = this.mZenModeHelper.getZenModeListenerInterruptionFilter();
        if (interruptionFilter != this.mInterruptionFilter) {
            this.mInterruptionFilter = interruptionFilter;
            scheduleInterruptionFilterChanged(interruptionFilter);
        }
    }

    private void applyAdjustmentLocked(Adjustment adjustment) {
        maybeClearAutobundleSummaryLocked(adjustment);
        NotificationRecord n = (NotificationRecord) this.mNotificationsByKey.get(adjustment.getKey());
        if (n != null) {
            if (adjustment.getImportance() != 0) {
                n.setImportance(adjustment.getImportance(), adjustment.getExplanation());
            }
            if (adjustment.getSignals() != null) {
                Bundle.setDefusable(adjustment.getSignals(), true);
                String autoGroupKey = adjustment.getSignals().getString("group_key_override", null);
                if (autoGroupKey == null) {
                    EventLogTags.writeNotificationUnautogrouped(adjustment.getKey());
                } else {
                    EventLogTags.writeNotificationAutogrouped(adjustment.getKey());
                }
                n.sbn.setOverrideGroupKey(autoGroupKey);
            }
        }
    }

    private void maybeClearAutobundleSummaryLocked(Adjustment adjustment) {
        if (adjustment.getSignals() != null) {
            Bundle.setDefusable(adjustment.getSignals(), true);
            if (adjustment.getSignals().containsKey("autogroup_needed") && !adjustment.getSignals().getBoolean("autogroup_needed", false)) {
                ArrayMap<String, String> summaries = (ArrayMap) this.mAutobundledSummaries.get(Integer.valueOf(adjustment.getUser()));
                if (summaries != null && summaries.containsKey(adjustment.getPackage())) {
                    NotificationRecord removed = (NotificationRecord) this.mNotificationsByKey.get(summaries.remove(adjustment.getPackage()));
                    if (removed != null) {
                        this.mNotificationList.remove(removed);
                        cancelNotificationLocked(removed, false, 16);
                    }
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void maybeAddAutobundleSummary(Adjustment adjustment) {
        Throwable th;
        if (adjustment.getSignals() != null) {
            Bundle.setDefusable(adjustment.getSignals(), true);
            if (adjustment.getSignals().getBoolean("autogroup_needed", false)) {
                String newAutoBundleKey = adjustment.getSignals().getString("group_key_override", null);
                NotificationRecord notificationRecord = null;
                synchronized (this.mNotificationList) {
                    try {
                        NotificationRecord notificationRecord2 = (NotificationRecord) this.mNotificationsByKey.get(adjustment.getKey());
                        if (notificationRecord2 == null) {
                            return;
                        }
                        StatusBarNotification adjustedSbn = notificationRecord2.sbn;
                        int userId = adjustedSbn.getUser().getIdentifier();
                        ArrayMap<String, String> summaries = (ArrayMap) this.mAutobundledSummaries.get(Integer.valueOf(userId));
                        if (summaries == null) {
                            summaries = new ArrayMap();
                        }
                        this.mAutobundledSummaries.put(Integer.valueOf(userId), summaries);
                        if (!(summaries.containsKey(adjustment.getPackage()) || newAutoBundleKey == null)) {
                            ApplicationInfo appInfo = (ApplicationInfo) adjustedSbn.getNotification().extras.getParcelable("android.appInfo");
                            Bundle extras = new Bundle();
                            extras.putParcelable("android.appInfo", appInfo);
                            Notification summaryNotification = new Builder(getContext()).setSmallIcon(adjustedSbn.getNotification().getSmallIcon()).setGroupSummary(true).setGroup(newAutoBundleKey).setFlag(1024, true).setFlag(512, true).setColor(adjustedSbn.getNotification().color).build();
                            summaryNotification.extras.putAll(extras);
                            Intent appIntent = getContext().getPackageManager().getLaunchIntentForPackage(adjustment.getPackage());
                            if (appIntent != null) {
                                summaryNotification.contentIntent = PendingIntent.getActivityAsUser(getContext(), 0, appIntent, 0, null, UserHandle.of(userId));
                            }
                            StatusBarNotification summarySbn = new StatusBarNotification(adjustedSbn.getPackageName(), adjustedSbn.getOpPkg(), Integer.MAX_VALUE, "group_key_override", adjustedSbn.getUid(), adjustedSbn.getInitialPid(), summaryNotification, adjustedSbn.getUser(), newAutoBundleKey, System.currentTimeMillis());
                            NotificationRecord notificationRecord3 = new NotificationRecord(getContext(), summarySbn);
                            try {
                                summaries.put(adjustment.getPackage(), summarySbn.getKey());
                                notificationRecord = notificationRecord3;
                            } catch (Throwable th2) {
                                th = th2;
                                notificationRecord = notificationRecord3;
                                throw th;
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
            }
        }
    }

    private String disableNotificationEffects(NotificationRecord record) {
        if (this.mDisableNotificationEffects) {
            return "booleanState";
        }
        if ((this.mListenerHints & 1) != 0) {
            return "listenerHints";
        }
        if (this.mCallState == 0 || this.mZenModeHelper.isCall(record)) {
            return null;
        }
        return "callState";
    }

    private void dumpJson(PrintWriter pw, DumpFilter filter) {
        JSONObject dump = new JSONObject();
        try {
            dump.put("service", "Notification Manager");
            dump.put("bans", this.mRankingHelper.dumpBansJson(filter));
            dump.put("ranking", this.mRankingHelper.dumpJson(filter));
            dump.put("stats", this.mUsageStats.dumpJson(filter));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pw.println(dump);
    }

    private boolean allowNotificationsInCall(String disableEffects, NotificationRecord record) {
        if (record == null || record.sbn == null) {
            return false;
        }
        boolean equals;
        boolean isMmsEnabled = isMmsNotificationEnable(record.sbn.getPackageName());
        if ("callState".equals(disableEffects) && "com.android.systemui".equals(record.sbn.getPackageName())) {
            equals = "low_battery".equals(record.sbn.getTag());
        } else {
            equals = false;
        }
        if (isMmsEnabled) {
            equals = true;
        }
        return equals;
    }

    void dumpImpl(PrintWriter pw, DumpFilter filter) {
        int N;
        int i;
        pw.print("Current Notification Manager state");
        if (filter.filtered) {
            pw.print(" (filtered to ");
            pw.print(filter);
            pw.print(")");
        }
        pw.println(':');
        boolean zenOnly = filter.filtered ? filter.zen : false;
        if (!zenOnly) {
            synchronized (this.mToastQueue) {
                N = this.mToastQueue.size();
                if (N > 0) {
                    pw.println("  Toast Queue:");
                    for (i = 0; i < N; i++) {
                        ((ToastRecord) this.mToastQueue.get(i)).dump(pw, "    ", filter);
                    }
                    pw.println("  ");
                }
            }
        }
        synchronized (this.mNotificationList) {
            if (!zenOnly) {
                N = this.mNotificationList.size();
                if (N > 0) {
                    pw.println("  Notification List:");
                    for (i = 0; i < N; i++) {
                        NotificationRecord nr = (NotificationRecord) this.mNotificationList.get(i);
                        if (filter.filtered) {
                            if (!filter.matches(nr.sbn)) {
                            }
                        }
                        nr.dump(pw, "    ", getContext(), filter.redact);
                    }
                    pw.println("  ");
                }
                if (!filter.filtered) {
                    N = this.mLights.size();
                    if (N > 0) {
                        pw.println("  Lights List:");
                        for (i = 0; i < N; i++) {
                            if (i == N - 1) {
                                pw.print("  > ");
                            } else {
                                pw.print("    ");
                            }
                            pw.println((String) this.mLights.get(i));
                        }
                        pw.println("  ");
                    }
                    pw.println("  mUseAttentionLight=" + this.mUseAttentionLight);
                    pw.println("  mNotificationPulseEnabled=" + this.mNotificationPulseEnabled);
                    pw.println("  mSoundNotificationKey=" + this.mSoundNotificationKey);
                    pw.println("  mVibrateNotificationKey=" + this.mVibrateNotificationKey);
                    pw.println("  mDisableNotificationEffects=" + this.mDisableNotificationEffects);
                    pw.println("  mCallState=" + callStateToString(this.mCallState));
                    pw.println("  mSystemReady=" + this.mSystemReady);
                    pw.println("  mMaxPackageEnqueueRate=" + this.mMaxPackageEnqueueRate);
                }
                pw.println("  mArchive=" + this.mArchive.toString());
                Iterator<StatusBarNotification> iter = this.mArchive.descendingIterator();
                i = 0;
                while (iter.hasNext()) {
                    StatusBarNotification sbn = (StatusBarNotification) iter.next();
                    if (filter == null || filter.matches(sbn)) {
                        pw.println("    " + sbn);
                        i++;
                        if (i >= 5) {
                            if (iter.hasNext()) {
                                pw.println("    ...");
                            }
                        }
                    }
                }
            }
            if (!zenOnly) {
                pw.println("\n  Usage Stats:");
                this.mUsageStats.dump(pw, "    ", filter);
            }
            if (!filter.filtered || zenOnly) {
                pw.println("\n  Zen Mode:");
                pw.print("    mInterruptionFilter=");
                pw.println(this.mInterruptionFilter);
                this.mZenModeHelper.dump(pw, "    ");
                pw.println("\n  Zen Log:");
                ZenLog.dump(pw, "    ");
            }
            if (!zenOnly) {
                pw.println("\n  Ranking Config:");
                this.mRankingHelper.dump(pw, "    ", filter);
                pw.println("\n  Notification listeners:");
                this.mListeners.dump(pw, filter);
                pw.print("    mListenerHints: ");
                pw.println(this.mListenerHints);
                pw.print("    mListenersDisablingEffects: (");
                N = this.mListenersDisablingEffects.size();
                for (i = 0; i < N; i++) {
                    int hint = this.mListenersDisablingEffects.keyAt(i);
                    if (i > 0) {
                        pw.print(';');
                    }
                    pw.print("hint[" + hint + "]:");
                    ArraySet<ManagedServiceInfo> listeners = (ArraySet) this.mListenersDisablingEffects.valueAt(i);
                    int listenerSize = listeners.size();
                    for (int j = 0; j < listenerSize; j++) {
                        if (i > 0) {
                            pw.print(',');
                        }
                        pw.print(((ManagedServiceInfo) listeners.valueAt(i)).component);
                    }
                }
                pw.println(')');
                pw.println("\n  mRankerServicePackageName: " + this.mRankerServicePackageName);
                pw.println("\n  Notification ranker services:");
                this.mRankerServices.dump(pw, filter);
            }
            pw.println("\n  Policy access:");
            pw.print("    mPolicyAccess: ");
            pw.println(this.mPolicyAccess);
            pw.println("\n  Condition providers:");
            this.mConditionProviders.dump(pw, filter);
            pw.println("\n  Group summaries:");
            for (Entry<String, NotificationRecord> entry : this.mSummaryByGroupKey.entrySet()) {
                NotificationRecord r = (NotificationRecord) entry.getValue();
                pw.println("    " + ((String) entry.getKey()) + " -> " + r.getKey());
                if (this.mNotificationsByKey.get(r.getKey()) != r) {
                    pw.println("!!!!!!LEAK: Record not found in mNotificationsByKey.");
                    r.dump(pw, "      ", getContext(), filter.redact);
                }
            }
            try {
                pw.println("\n  Banned Packages:");
                ArrayMap<Integer, ArrayList<String>> packageBans = getPackageBans(filter);
                for (Integer userId : packageBans.keySet()) {
                    for (String packageName : (ArrayList) packageBans.get(userId)) {
                        pw.println("    " + userId + ": " + packageName);
                    }
                }
            } catch (NameNotFoundException e) {
            }
        }
    }

    private void setNotificationWhiteList() {
        int i = 0;
        String apps_plus = Secure.getString(getContext().getContentResolver(), POWER_SAVER_NOTIFICATION_WHITELIST);
        Log.i(TAG, "getNotificationWhiteList from db: " + apps_plus);
        this.power_save_whiteSet.clear();
        for (String s : this.plus_notification_white_list) {
            this.power_save_whiteSet.add(s);
        }
        for (String s2 : this.noitfication_white_list) {
            this.power_save_whiteSet.add(s2);
        }
        if (!TextUtils.isEmpty(apps_plus)) {
            String[] split = apps_plus.split(";");
            int length = split.length;
            while (i < length) {
                this.power_save_whiteSet.add(split[i]);
                i++;
            }
        }
    }

    private boolean isNoitficationWhiteApp(String pkg) {
        return this.power_save_whiteSet.contains(pkg);
    }

    private ArrayMap<Integer, ArrayList<String>> getPackageBans(DumpFilter filter) throws NameNotFoundException {
        ArrayMap<Integer, ArrayList<String>> packageBans = new ArrayMap();
        ArrayList<String> packageNames = new ArrayList();
        for (UserInfo user : UserManager.get(getContext()).getUsers()) {
            int userId = user.getUserHandle().getIdentifier();
            PackageManager packageManager = getContext().getPackageManager();
            List<PackageInfo> packages = packageManager.getInstalledPackagesAsUser(0, userId);
            int packageCount = packages.size();
            for (int p = 0; p < packageCount; p++) {
                String packageName = ((PackageInfo) packages.get(p)).packageName;
                if ((filter == null || filter.matches(packageName)) && !checkNotificationOp(packageName, packageManager.getPackageUidAsUser(packageName, userId))) {
                    packageNames.add(packageName);
                }
            }
            if (!packageNames.isEmpty()) {
                packageBans.put(Integer.valueOf(userId), packageNames);
                packageNames = new ArrayList();
            }
        }
        return packageBans;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void enqueueNotificationInternal(String pkg, String opPkg, int callingUid, int callingPid, String tag, int id, Notification notification, int[] idOut, int incomingUserId) {
        if (!SystemProperties.getBoolean("sys.super_power_save", false) || isNoitficationWhiteApp(pkg)) {
            Flog.i(400, "enqueueNotificationInternal: pkg=" + pkg + " id=" + id + " notification=" + notification);
            int targetUid = getNCTargetAppUid(opPkg, pkg, callingUid, notification);
            if (targetUid == callingUid) {
                checkCallerIsSystemOrSameApp(pkg);
            } else {
                Slog.i(TAG, "NC " + callingUid + " calling " + targetUid);
            }
            boolean equals = !isUidSystem(targetUid) ? "android".equals(pkg) : true;
            boolean isNotificationFromListener = this.mListeners.isListenerPackage(pkg);
            int userId = ActivityManager.handleIncomingUser(callingPid, callingUid, incomingUserId, true, false, "enqueueNotification", pkg);
            UserHandle user = new UserHandle(userId);
            try {
                int i;
                int i2;
                PackageManager packageManager = getContext().getPackageManager();
                if (userId == -1) {
                    i = 0;
                } else {
                    i = userId;
                }
                Notification.addFieldsFromContext(packageManager.getApplicationInfoAsUser(pkg, 268435456, i), userId, notification);
                this.mUsageStats.registerEnqueuedByApp(pkg);
                if (!(equals || isNotificationFromListener)) {
                    synchronized (this.mNotificationList) {
                        float appEnqueueRate = this.mUsageStats.getAppEnqueueRate(pkg);
                        if (appEnqueueRate > this.mMaxPackageEnqueueRate) {
                            this.mUsageStats.registerOverRateQuota(pkg);
                            long now = SystemClock.elapsedRealtime();
                            if (now - this.mLastOverRateLogTime > MIN_PACKAGE_OVERRATE_LOG_INTERVAL) {
                                Slog.e(TAG, "Package enqueue rate is " + appEnqueueRate + ". Shedding events. package=" + pkg);
                                this.mLastOverRateLogTime = now;
                            }
                        } else {
                            int count = 0;
                            int N = this.mNotificationList.size();
                            for (i2 = 0; i2 < N; i2++) {
                                NotificationRecord r = (NotificationRecord) this.mNotificationList.get(i2);
                                if (r.sbn.getPackageName().equals(pkg) && r.sbn.getUserId() == userId) {
                                    if (r.sbn.getId() == id && TextUtils.equals(r.sbn.getTag(), tag)) {
                                        break;
                                    }
                                    count++;
                                    if (count >= 50) {
                                        this.mUsageStats.registerOverCountQuota(pkg);
                                        Slog.e(TAG, "Package has already posted " + count + " notifications.  Not showing more.  package=" + pkg);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
                if (pkg == null || notification == null) {
                    throw new IllegalArgumentException("null not allowed: pkg=" + pkg + " id=" + id + " notification=" + notification);
                }
                if (notification.allPendingIntents != null) {
                    int intentCount = notification.allPendingIntents.size();
                    if (intentCount > 0) {
                        ActivityManagerInternal am = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
                        long duration = ((LocalService) LocalServices.getService(LocalService.class)).getNotificationWhitelistDuration();
                        for (i2 = 0; i2 < intentCount; i2++) {
                            PendingIntent pendingIntent = (PendingIntent) notification.allPendingIntents.valueAt(i2);
                            if (pendingIntent != null) {
                                am.setPendingIntentWhitelistDuration(pendingIntent.getTarget(), duration);
                            }
                        }
                    }
                }
                if (!isImportantNotification(pkg, notification)) {
                    notification.priority = clamp(notification.priority, -2, 2);
                }
                NotificationRecord notificationRecord = new NotificationRecord(getContext(), new StatusBarNotification(pkg, opPkg, id, tag, targetUid, callingPid, 0, notification, user));
                if (this.mNotificationResource == null) {
                    if (DBG || Log.HWINFO) {
                        Log.i(TAG, " init notification resource");
                    }
                    this.mNotificationResource = HwFrameworkFactory.getHwResource(10);
                }
                if (this.mNotificationResource == null || 2 != this.mNotificationResource.acquire(targetUid, pkg, -1)) {
                    this.mHandler.post(new EnqueueNotificationRunnable(userId, notificationRecord));
                    idOut[0] = id;
                    return;
                }
                if (DBG || Log.HWINFO) {
                    Log.i(TAG, " enqueueNotificationInternal dont acquire resource");
                }
                return;
            } catch (Throwable e) {
                Slog.e(TAG, "Cannot create a context for sending app", e);
                return;
            }
        }
        Flog.i(400, "enqueueNotificationInternal  !isNoitficationWhiteApp package=" + pkg);
    }

    private void handleGroupedNotificationLocked(NotificationRecord r, NotificationRecord old, int callingUid, int callingPid) {
        StatusBarNotification sbn = r.sbn;
        Notification n = sbn.getNotification();
        if (n.isGroupSummary() && !sbn.isAppGroup()) {
            n.flags &= -513;
        }
        String group = sbn.getGroupKey();
        boolean isSummary = n.isGroupSummary();
        Notification notification = old != null ? old.sbn.getNotification() : null;
        String groupKey = old != null ? old.sbn.getGroupKey() : null;
        boolean oldIsSummary = old != null ? notification.isGroupSummary() : false;
        if (oldIsSummary) {
            NotificationRecord removedSummary = (NotificationRecord) this.mSummaryByGroupKey.remove(groupKey);
            if (removedSummary != old) {
                Slog.w(TAG, "Removed summary didn't match old notification: old=" + old.getKey() + ", removed=" + (removedSummary != null ? removedSummary.getKey() : "<null>"));
            }
        }
        if (isSummary) {
            this.mSummaryByGroupKey.put(group, r);
        }
        if (!oldIsSummary) {
            return;
        }
        if (!isSummary || !groupKey.equals(group)) {
            cancelGroupChildrenLocked(old, callingUid, callingPid, null, 12, false);
        }
    }

    private static AudioAttributes audioAttributesForNotification(Notification n) {
        if (n.audioAttributes != null && !Notification.AUDIO_ATTRIBUTES_DEFAULT.equals(n.audioAttributes)) {
            return n.audioAttributes;
        }
        if (n.audioStreamType >= 0 && n.audioStreamType < AudioSystem.getNumStreamTypes()) {
            return new AudioAttributes.Builder().setInternalLegacyStreamType(n.audioStreamType).build();
        }
        if (n.audioStreamType == -1) {
            return Notification.AUDIO_ATTRIBUTES_DEFAULT;
        }
        Log.w(TAG, String.format("Invalid stream type: %d", new Object[]{Integer.valueOf(n.audioStreamType)}));
        return Notification.AUDIO_ATTRIBUTES_DEFAULT;
    }

    void showNextToastLocked() {
        ToastRecord toastRecord = (ToastRecord) this.mToastQueue.get(0);
        while (toastRecord != null) {
            if (toastRecord.pkg.equals(getForegroundProgressPkg()) || isSystemApp(toastRecord.pkg)) {
                Flog.i(400, "Show pkg=" + toastRecord.pkg + " callback=" + toastRecord.callback);
                try {
                    toastRecord.callback.show();
                    scheduleTimeoutLocked(toastRecord);
                    return;
                } catch (RemoteException e) {
                    Slog.w(TAG, "Object died trying to show notification " + toastRecord.callback + " in package " + toastRecord.pkg);
                    int index = this.mToastQueue.indexOf(toastRecord);
                    if (index >= 0) {
                        this.mToastQueue.remove(index);
                    }
                    keepProcessAliveLocked(toastRecord.pid);
                    if (this.mToastQueue.size() > 0) {
                        toastRecord = (ToastRecord) this.mToastQueue.get(0);
                    } else {
                        toastRecord = null;
                    }
                }
            } else {
                cancelToastLocked(0);
                toastRecord = null;
            }
        }
    }

    void cancelToastLocked(int index) {
        ToastRecord record = (ToastRecord) this.mToastQueue.get(index);
        try {
            record.callback.hide();
        } catch (RemoteException e) {
            Slog.w(TAG, "Object died trying to hide notification " + record.callback + " in package " + record.pkg);
        }
        this.mToastQueue.remove(index);
        keepProcessAliveLocked(record.pid);
        if (this.mToastQueue.size() > 0) {
            showNextToastLocked();
        }
    }

    private void scheduleTimeoutLocked(ToastRecord r) {
        this.mHandler.removeCallbacksAndMessages(r);
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 2, r), (long) (r.duration == 1 ? LONG_DELAY : 2000));
    }

    private void handleTimeout(ToastRecord record) {
        Flog.i(400, "Timeout pkg=" + record.pkg + " callback=" + record.callback);
        synchronized (this.mToastQueue) {
            int index = indexOfToastLocked(record.pkg, record.callback);
            if (index >= 0) {
                cancelToastLocked(index);
            }
        }
    }

    int indexOfToastLocked(String pkg, ITransientNotification callback) {
        IBinder cbak = callback.asBinder();
        ArrayList<ToastRecord> list = this.mToastQueue;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            ToastRecord r = (ToastRecord) list.get(i);
            if (r.pkg.equals(pkg) && r.callback.asBinder() == cbak) {
                return i;
            }
        }
        return -1;
    }

    void keepProcessAliveLocked(int pid) {
        boolean z = false;
        int toastCount = 0;
        ArrayList<ToastRecord> list = this.mToastQueue;
        int N = list.size();
        for (int i = 0; i < N; i++) {
            if (((ToastRecord) list.get(i)).pid == pid) {
                toastCount++;
            }
        }
        try {
            IActivityManager iActivityManager = this.mAm;
            IBinder iBinder = this.mForegroundToken;
            if (toastCount > 0) {
                z = true;
            }
            iActivityManager.setProcessForeground(iBinder, pid, z);
        } catch (RemoteException e) {
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleRankingReconsideration(Message message) {
        if (message.obj instanceof RankingReconsideration) {
            RankingReconsideration recon = message.obj;
            recon.run();
            synchronized (this.mNotificationList) {
                NotificationRecord record = (NotificationRecord) this.mNotificationsByKey.get(recon.getKey());
                if (record == null) {
                    return;
                }
                int indexBefore = findNotificationRecordIndexLocked(record);
                boolean interceptBefore = record.isIntercepted();
                int visibilityBefore = record.getPackageVisibilityOverride();
                recon.applyChangesLocked(record);
                applyZenModeLocked(record);
                this.mRankingHelper.sort(this.mNotificationList);
                int indexAfter = findNotificationRecordIndexLocked(record);
                boolean interceptAfter = record.isIntercepted();
                boolean changed = (indexBefore == indexAfter && interceptBefore == interceptAfter) ? visibilityBefore != record.getPackageVisibilityOverride() : true;
                if (interceptBefore && !interceptAfter) {
                    buzzBeepBlinkLocked(record);
                }
            }
        }
    }

    private void handleRankingSort() {
        synchronized (this.mNotificationList) {
            int i;
            int N = this.mNotificationList.size();
            ArrayList<String> orderBefore = new ArrayList(N);
            ArrayList<String> groupOverrideBefore = new ArrayList(N);
            int[] visibilities = new int[N];
            int[] importances = new int[N];
            for (i = 0; i < N; i++) {
                NotificationRecord r = (NotificationRecord) this.mNotificationList.get(i);
                orderBefore.add(r.getKey());
                groupOverrideBefore.add(r.sbn.getGroupKey());
                visibilities[i] = r.getPackageVisibilityOverride();
                importances[i] = r.getImportance();
                this.mRankingHelper.extractSignals(r);
            }
            this.mRankingHelper.sort(this.mNotificationList);
            i = 0;
            while (i < N) {
                r = (NotificationRecord) this.mNotificationList.get(i);
                if (((String) orderBefore.get(i)).equals(r.getKey()) && visibilities[i] == r.getPackageVisibilityOverride()) {
                    if (importances[i] == r.getImportance() && ((String) groupOverrideBefore.get(i)).equals(r.sbn.getGroupKey())) {
                        i++;
                    }
                }
                scheduleSendRankingUpdate();
                return;
            }
        }
    }

    private void applyZenModeLocked(NotificationRecord record) {
        int i = 0;
        record.setIntercepted(this.mZenModeHelper.shouldIntercept(record));
        if (record.isIntercepted()) {
            int i2;
            if (this.mZenModeHelper.shouldSuppressWhenScreenOff()) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            if (this.mZenModeHelper.shouldSuppressWhenScreenOn()) {
                i = 2;
            }
            record.setSuppressedVisualEffects(i2 | i);
        }
    }

    private int findNotificationRecordIndexLocked(NotificationRecord target) {
        return this.mRankingHelper.indexOf(this.mNotificationList, target);
    }

    private void scheduleSendRankingUpdate() {
        if (!this.mHandler.hasMessages(4)) {
            this.mHandler.sendMessage(Message.obtain(this.mHandler, 4));
        }
    }

    private void handleSendRankingUpdate() {
        synchronized (this.mNotificationList) {
            this.mListeners.notifyRankingUpdateLocked();
        }
    }

    private void scheduleListenerHintsChanged(int state) {
        this.mHandler.removeMessages(5);
        this.mHandler.obtainMessage(5, state, 0).sendToTarget();
    }

    private void scheduleInterruptionFilterChanged(int listenerInterruptionFilter) {
        this.mHandler.removeMessages(6);
        this.mHandler.obtainMessage(6, listenerInterruptionFilter, 0).sendToTarget();
    }

    private void handleListenerHintsChanged(int hints) {
        synchronized (this.mNotificationList) {
            this.mListeners.notifyListenerHintsChangedLocked(hints);
        }
    }

    private void handleListenerInterruptionFilterChanged(int interruptionFilter) {
        synchronized (this.mNotificationList) {
            this.mListeners.notifyInterruptionFilterChanged(interruptionFilter);
        }
    }

    static int clamp(int x, int low, int high) {
        if (x < low) {
            return low;
        }
        return x > high ? high : x;
    }

    void sendAccessibilityEvent(Notification notification, CharSequence packageName) {
        AccessibilityManager manager = AccessibilityManager.getInstance(getContext());
        if (manager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(64);
            event.setPackageName(packageName);
            event.setClassName(Notification.class.getName());
            event.setParcelableData(notification);
            CharSequence tickerText = notification.tickerText;
            if (!TextUtils.isEmpty(tickerText)) {
                event.getText().add(tickerText);
            }
            manager.sendAccessibilityEvent(event);
        }
    }

    private void cancelNotificationLocked(NotificationRecord r, boolean sendDelete, int reason) {
        long identity;
        Flog.i(400, "cancelNotificationLocked called,tell the app,reason = " + reason);
        if (sendDelete && r.getNotification().deleteIntent != null) {
            try {
                r.getNotification().deleteIntent.send();
            } catch (CanceledException ex) {
                Slog.w(TAG, "canceled PendingIntent for " + r.sbn.getPackageName(), ex);
            }
        }
        if (r.getNotification().getSmallIcon() != null) {
            r.isCanceled = true;
            Flog.i(400, "cancelNotificationLocked:" + r.sbn.getKey());
            this.mListeners.notifyRemovedLocked(r.sbn);
        }
        String canceledKey = r.getKey();
        if (canceledKey.equals(this.mSoundNotificationKey)) {
            this.mSoundNotificationKey = null;
            identity = Binder.clearCallingIdentity();
            try {
                IRingtonePlayer player = this.mAudioManager.getRingtonePlayer();
                if (player != null) {
                    player.stopAsync();
                }
                Binder.restoreCallingIdentity(identity);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }
        if (canceledKey.equals(this.mVibrateNotificationKey)) {
            this.mVibrateNotificationKey = null;
            identity = Binder.clearCallingIdentity();
            try {
                this.mVibrator.cancel();
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
        this.mLights.remove(canceledKey);
        switch (reason) {
            case 2:
            case 3:
            case 10:
            case 11:
                this.mUsageStats.registerDismissedByUser(r);
                break;
            case 8:
            case 9:
                this.mUsageStats.registerRemovedByApp(r);
                break;
        }
        Flog.i(400, "cancelNotificationLocked,remove =" + r.sbn.getPackageName());
        this.mNotificationsByKey.remove(r.sbn.getKey());
        String groupKey = r.getGroupKey();
        NotificationRecord groupSummary = (NotificationRecord) this.mSummaryByGroupKey.get(groupKey);
        if (groupSummary != null && groupSummary.getKey().equals(r.getKey())) {
            this.mSummaryByGroupKey.remove(groupKey);
        }
        ArrayMap<String, String> summaries = (ArrayMap) this.mAutobundledSummaries.get(Integer.valueOf(r.sbn.getUserId()));
        if (summaries != null && r.sbn.getKey().equals(summaries.get(r.sbn.getPackageName()))) {
            summaries.remove(r.sbn.getPackageName());
        }
        this.mArchive.record(r.sbn);
        long now = System.currentTimeMillis();
        EventLogTags.writeNotificationCanceled(canceledKey, reason, r.getLifespanMs(now), r.getFreshnessMs(now), r.getExposureMs(now));
    }

    void cancelNotification(int callingUid, int callingPid, String pkg, String tag, int id, int mustHaveFlags, int mustNotHaveFlags, boolean sendDelete, int userId, int reason, ManagedServiceInfo listener) {
        final ManagedServiceInfo managedServiceInfo = listener;
        final int i = callingUid;
        final int i2 = callingPid;
        final String str = pkg;
        final int i3 = id;
        final String str2 = tag;
        final int i4 = userId;
        final int i5 = mustHaveFlags;
        final int i6 = mustNotHaveFlags;
        final int i7 = reason;
        final boolean z = sendDelete;
        this.mHandler.post(new Runnable() {
            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                String toShortString = managedServiceInfo == null ? null : managedServiceInfo.component.toShortString();
                EventLogTags.writeNotificationCancel(i, i2, str, i3, str2, i4, i5, i6, i7, toShortString);
                synchronized (NotificationManagerService.this.mNotificationList) {
                    int index = NotificationManagerService.this.indexOfNotificationLocked(str, str2, i3, i4);
                    Flog.i(400, "cancelNotification,index:" + index);
                    if (index >= 0) {
                        NotificationRecord r = (NotificationRecord) NotificationManagerService.this.mNotificationList.get(index);
                        if (i7 == 1) {
                            NotificationManagerService.this.mUsageStats.registerClickedByUser(r);
                        }
                        if ((r.getNotification().flags & i5) != i5) {
                        } else if ((r.getNotification().flags & i6) != 0) {
                        } else {
                            NotificationManagerService.this.mNotificationList.remove(index);
                            Flog.i(400, "cancelNotification,cancelNotificationLocked,callingUid = " + i + ",callingPid = " + i2);
                            NotificationManagerService.this.hwCancelNotification(str, str2, i3, i4);
                            NotificationManagerService.this.cancelNotificationLocked(r, z, i7);
                            NotificationManagerService.this.cancelGroupChildrenLocked(r, i, i2, toShortString, 12, z);
                            NotificationManagerService.this.updateLightsLocked();
                        }
                    }
                }
            }
        });
    }

    private boolean notificationMatchesUserId(NotificationRecord r, int userId) {
        if (userId == -1 || r.getUserId() == -1 || r.getUserId() == userId) {
            return true;
        }
        return false;
    }

    private boolean notificationMatchesCurrentProfiles(NotificationRecord r, int userId) {
        if (notificationMatchesUserId(r, userId)) {
            return true;
        }
        return this.mUserProfiles.isCurrentProfile(r.getUserId());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean cancelAllNotificationsInt(int callingUid, int callingPid, String pkg, int mustHaveFlags, int mustNotHaveFlags, boolean doit, int userId, int reason, ManagedServiceInfo listener) {
        String listenerName;
        if (listener == null) {
            listenerName = null;
        } else {
            listenerName = listener.component.toShortString();
        }
        EventLogTags.writeNotificationCancelAll(callingUid, callingPid, pkg, userId, mustHaveFlags, mustNotHaveFlags, reason, listenerName);
        synchronized (this.mNotificationList) {
            int i;
            Flog.i(400, "cancelAllNotificationsInt called");
            ArrayList canceledNotifications = null;
            for (i = this.mNotificationList.size() - 1; i >= 0; i--) {
                NotificationRecord r = (NotificationRecord) this.mNotificationList.get(i);
                if (notificationMatchesUserId(r, userId)) {
                    if (!(r.getUserId() == -1 && pkg == null) && (r.getFlags() & mustHaveFlags) == mustHaveFlags && (r.getFlags() & mustNotHaveFlags) == 0 && (pkg == null || r.sbn.getPackageName().equals(pkg))) {
                        if (isClonedAppDeleted(reason, r.sbn.getTag())) {
                            continue;
                        } else {
                            if (canceledNotifications == null) {
                                canceledNotifications = new ArrayList();
                            }
                            canceledNotifications.add(r);
                            if (doit) {
                                Flog.i(400, "cancelAllNotificationsInt:" + r.sbn.getKey());
                                this.mNotificationList.remove(i);
                                hwCancelNotification(pkg, r.sbn.getTag(), r.sbn.getId(), r.sbn.getUserId());
                                cancelNotificationLocked(r, false, reason);
                            } else {
                                return true;
                            }
                        }
                    }
                }
            }
            if (doit && canceledNotifications != null) {
                int M = canceledNotifications.size();
                for (i = 0; i < M; i++) {
                    cancelGroupChildrenLocked((NotificationRecord) canceledNotifications.get(i), callingUid, callingPid, listenerName, 12, false);
                }
            }
            if (canceledNotifications != null) {
                updateLightsLocked();
            }
            boolean z = canceledNotifications != null;
        }
    }

    void cancelAllLocked(int callingUid, int callingPid, int userId, int reason, ManagedServiceInfo listener, boolean includeCurrentProfiles) {
        String listenerName;
        int i;
        if (listener == null) {
            listenerName = null;
        } else {
            listenerName = listener.component.toShortString();
        }
        EventLogTags.writeNotificationCancelAll(callingUid, callingPid, null, userId, 0, 0, reason, listenerName);
        Flog.i(400, "cancelAllLocked called,callingUid = " + callingUid + ",callingPid = " + callingPid);
        ArrayList canceledNotifications = null;
        for (i = this.mNotificationList.size() - 1; i >= 0; i--) {
            NotificationRecord r = (NotificationRecord) this.mNotificationList.get(i);
            if (includeCurrentProfiles) {
                if (!notificationMatchesCurrentProfiles(r, userId)) {
                }
                if ((r.getFlags() & 34) == 0) {
                    this.mNotificationList.remove(i);
                    Flog.i(400, "cancelAllLocked,cancelNotificationLocked:" + r.sbn.getKey());
                    hwCancelNotification(r.sbn.getPackageName(), r.sbn.getTag(), r.sbn.getId(), r.sbn.getUserId());
                    cancelNotificationLocked(r, true, reason);
                    if (canceledNotifications == null) {
                        canceledNotifications = new ArrayList();
                    }
                    canceledNotifications.add(r);
                }
            } else {
                if (!notificationMatchesUserId(r, userId)) {
                }
                if ((r.getFlags() & 34) == 0) {
                    this.mNotificationList.remove(i);
                    Flog.i(400, "cancelAllLocked,cancelNotificationLocked:" + r.sbn.getKey());
                    hwCancelNotification(r.sbn.getPackageName(), r.sbn.getTag(), r.sbn.getId(), r.sbn.getUserId());
                    cancelNotificationLocked(r, true, reason);
                    if (canceledNotifications == null) {
                        canceledNotifications = new ArrayList();
                    }
                    canceledNotifications.add(r);
                }
            }
        }
        int M = canceledNotifications != null ? canceledNotifications.size() : 0;
        for (i = 0; i < M; i++) {
            cancelGroupChildrenLocked((NotificationRecord) canceledNotifications.get(i), callingUid, callingPid, listenerName, 12, false);
        }
        updateLightsLocked();
    }

    private void cancelGroupChildrenLocked(NotificationRecord r, int callingUid, int callingPid, String listenerName, int reason, boolean sendDelete) {
        if (r.getNotification().isGroupSummary()) {
            String pkg = r.sbn.getPackageName();
            int userId = r.getUserId();
            if (pkg == null) {
                Flog.e(400, "No package for group summary: " + r.getKey());
                return;
            }
            for (int i = this.mNotificationList.size() - 1; i >= 0; i--) {
                NotificationRecord childR = (NotificationRecord) this.mNotificationList.get(i);
                StatusBarNotification childSbn = childR.sbn;
                if (childSbn.isGroup() && !childSbn.getNotification().isGroupSummary() && childR.getGroupKey().equals(r.getGroupKey())) {
                    EventLogTags.writeNotificationCancel(callingUid, callingPid, pkg, childSbn.getId(), childSbn.getTag(), userId, 0, 0, reason, listenerName);
                    Flog.i(400, "cancelGroupChildrenLocked:" + childSbn.getKey());
                    this.mNotificationList.remove(i);
                    hwCancelNotification(pkg, childSbn.getTag(), childSbn.getId(), childSbn.getUserId());
                    cancelNotificationLocked(childR, sendDelete, reason);
                }
            }
        }
    }

    void updateLightsLocked() {
        NotificationRecord ledNotification = null;
        int i = this.mLights.size();
        long token = Binder.clearCallingIdentity();
        try {
            int currentUser = ActivityManager.getCurrentUser();
            while (ledNotification == null && i > 0) {
                i--;
                String owner = (String) this.mLights.get(i);
                ledNotification = (NotificationRecord) this.mNotificationsByKey.get(owner);
                if (ledNotification == null) {
                    Slog.wtfStack(TAG, "LED Notification does not exist: " + owner);
                    this.mLights.remove(owner);
                } else if (!(ledNotification.getUser().getIdentifier() == currentUser || ledNotification.getUser().getIdentifier() == -1 || isAFWUserId(ledNotification.getUser().getIdentifier()))) {
                    Slog.d(TAG, "ledNotification is not CurrentUser,AFWuser and AllUser:" + owner);
                    ledNotification = null;
                }
            }
            Flog.i(400, "updateLightsLocked,mInCall =" + this.mInCall + ",mScreenOn = " + this.mScreenOn + ",ledNotification == null?" + (ledNotification == null));
            if (ledNotification == null || this.mInCall || this.mScreenOn) {
                Flog.i(400, "updateLightsLocked,turn off notificationLight");
                this.mNotificationLight.turnOff();
                if (this.mStatusBar != null) {
                    this.mStatusBar.notificationLightOff();
                }
                Flog.i(1100, "turn off notificationLight due to incall or screenon");
                updateLight(false, 0, 0);
                return;
            }
            Notification ledno = ledNotification.sbn.getNotification();
            int ledARGB = ledno.ledARGB;
            int ledOnMS = ledno.ledOnMS;
            int ledOffMS = ledno.ledOffMS;
            if ((ledno.defaults & 4) != 0) {
                ledARGB = this.mDefaultNotificationColor;
                ledOnMS = this.mDefaultNotificationLedOn;
                ledOffMS = this.mDefaultNotificationLedOff;
            }
            if (this.mNotificationPulseEnabled) {
                this.mNotificationLight.setFlashing(ledARGB, 1, ledOnMS, ledOffMS);
                Flog.i(1100, "set flash ledARGB=0x" + Integer.toHexString(ledARGB) + ", ledOn:" + ledOnMS + ", ledOff:" + ledOffMS);
                updateLight(true, ledOnMS, ledOffMS);
            }
            if (this.mStatusBar != null) {
                this.mStatusBar.notificationLightPulse(ledARGB, ledOnMS, ledOffMS);
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    protected int indexOfNotificationLocked(String pkg, String tag, int id, int userId) {
        ArrayList<NotificationRecord> list = this.mNotificationList;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            NotificationRecord r = (NotificationRecord) list.get(i);
            if (notificationMatchesUserId(r, userId) && r.sbn.getId() == id && TextUtils.equals(r.sbn.getTag(), tag) && r.sbn.getPackageName().equals(pkg)) {
                return i;
            }
        }
        return -1;
    }

    private String getForegroundProgressPkg() {
        try {
            List<RunningTaskInfo> taskInfo = ((ActivityManager) getContext().getSystemService("activity")).getRunningTasks(1);
            if (taskInfo == null || taskInfo.size() == 0) {
                return null;
            }
            ComponentName componentInfo = ((RunningTaskInfo) taskInfo.get(0)).topActivity;
            if (componentInfo == null) {
                return null;
            }
            return componentInfo.getPackageName();
        } catch (SecurityException e) {
            Log.w(TAG, "can't get the foreground progress because the security issue. " + e.getMessage());
            return null;
        }
    }

    private boolean isSystemApp(String pkg) {
        boolean z = true;
        try {
            if (pkg.contains("android")) {
                return true;
            }
            if ((getContext().getPackageManager().getPackageInfo(pkg, 0).applicationInfo.flags & 1) == 0) {
                z = false;
            }
            return z;
        } catch (NameNotFoundException e) {
            Log.w(TAG, "can't find the packageName,please check it." + e.getMessage());
            return false;
        }
    }

    int indexOfNotificationLocked(String key) {
        int N = this.mNotificationList.size();
        for (int i = 0; i < N; i++) {
            if (key.equals(((NotificationRecord) this.mNotificationList.get(i)).getKey())) {
                return i;
            }
        }
        return -1;
    }

    private void updateNotificationPulse() {
        synchronized (this.mNotificationList) {
            updateLightsLocked();
        }
    }

    private static boolean isUidSystem(int uid) {
        int appid = UserHandle.getAppId(uid);
        if (appid == 1000 || appid == MESSAGE_RANKING_SORT || uid == 0) {
            return true;
        }
        return false;
    }

    private static boolean isCallerSystem() {
        return isUidSystem(Binder.getCallingUid());
    }

    private static void checkCallerIsSystem() {
        if (!isCallerSystem()) {
            throw new SecurityException("Disallowed call for uid " + Binder.getCallingUid());
        }
    }

    private static void checkCallerIsSystemOrSameApp(String pkg) {
        if (!isCallerSystem()) {
            checkCallerIsSameApp(pkg);
        }
    }

    private static void checkCallerIsSameApp(String pkg) {
        int uid = Binder.getCallingUid();
        try {
            ApplicationInfo ai = AppGlobals.getPackageManager().getApplicationInfo(pkg, 0, UserHandle.getCallingUserId());
            if (ai == null) {
                throw new SecurityException("Unknown package " + pkg);
            } else if (!UserHandle.isSameApp(ai.uid, uid)) {
                throw new SecurityException("Calling uid " + uid + " gave package" + pkg + " which is owned by uid " + ai.uid);
            }
        } catch (RemoteException re) {
            throw new SecurityException("Unknown package " + pkg + "\n" + re);
        }
    }

    private static String callStateToString(int state) {
        switch (state) {
            case 0:
                return "CALL_STATE_IDLE";
            case 1:
                return "CALL_STATE_RINGING";
            case 2:
                return "CALL_STATE_OFFHOOK";
            default:
                return "CALL_STATE_UNKNOWN_" + state;
        }
    }

    private void listenForCallState() {
        TelephonyManager.from(getContext()).listen(new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                if (NotificationManagerService.this.mCallState != state) {
                    if (NotificationManagerService.DBG) {
                        Slog.d(NotificationManagerService.TAG, "Call state changed: " + NotificationManagerService.callStateToString(state));
                    }
                    NotificationManagerService.this.mCallState = state;
                }
            }
        }, 32);
    }

    private NotificationRankingUpdate makeRankingUpdateLocked(ManagedServiceInfo info) {
        int i;
        int N = this.mNotificationList.size();
        ArrayList<String> arrayList = new ArrayList(N);
        ArrayList<String> interceptedKeys = new ArrayList(N);
        ArrayList<Integer> importance = new ArrayList(N);
        Bundle overrideGroupKeys = new Bundle();
        Bundle visibilityOverrides = new Bundle();
        Bundle suppressedVisualEffects = new Bundle();
        Bundle explanation = new Bundle();
        for (i = 0; i < N; i++) {
            NotificationRecord record = (NotificationRecord) this.mNotificationList.get(i);
            if (isVisibleToListener(record.sbn, info)) {
                String key = record.sbn.getKey();
                arrayList.add(key);
                importance.add(Integer.valueOf(record.getImportance()));
                if (record.getImportanceExplanation() != null) {
                    explanation.putCharSequence(key, record.getImportanceExplanation());
                }
                if (record.isIntercepted()) {
                    interceptedKeys.add(key);
                }
                suppressedVisualEffects.putInt(key, record.getSuppressedVisualEffects());
                if (record.getPackageVisibilityOverride() != -1000) {
                    visibilityOverrides.putInt(key, record.getPackageVisibilityOverride());
                }
                overrideGroupKeys.putString(key, record.sbn.getOverrideGroupKey());
            }
        }
        int M = arrayList.size();
        String[] keysAr = (String[]) arrayList.toArray(new String[M]);
        String[] interceptedKeysAr = (String[]) interceptedKeys.toArray(new String[interceptedKeys.size()]);
        int[] importanceAr = new int[M];
        for (i = 0; i < M; i++) {
            importanceAr[i] = ((Integer) importance.get(i)).intValue();
        }
        return new NotificationRankingUpdate(keysAr, interceptedKeysAr, visibilityOverrides, suppressedVisualEffects, importanceAr, explanation, overrideGroupKeys);
    }

    private boolean isVisibleToListener(StatusBarNotification sbn, ManagedServiceInfo listener) {
        if (listener.enabledAndUserMatches(sbn.getUserId())) {
            return true;
        }
        return false;
    }

    private boolean isPackageSuspendedForUser(String pkg, int uid) {
        try {
            return AppGlobals.getPackageManager().isPackageSuspendedForUser(pkg, UserHandle.getUserId(uid));
        } catch (RemoteException e) {
            throw new SecurityException("Could not talk to package manager service");
        } catch (IllegalArgumentException e2) {
            return false;
        }
    }
}
