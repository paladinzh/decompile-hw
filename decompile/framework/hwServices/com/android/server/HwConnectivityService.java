package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.common.HwFrameworkFactory;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.net.CaptivePortal;
import android.net.ConnectivityManager;
import android.net.ICaptivePortal;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.NetworkRequest;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.HwVSimManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Slog;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import com.android.internal.telephony.HwModemCapability;
import com.android.internal.telephony.HwPhoneConstants;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.util.ArrayUtils;
import com.android.server.GcmFixer.HeartbeatReceiver;
import com.android.server.GcmFixer.NetworkStateReceiver;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.location.HwGnssCommParam;
import com.android.server.net.HwNetworkStatsService;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.android.server.wifipro.WifiProCommonUtils;
import com.hisi.mapcon.IMapconService;
import com.hisi.mapcon.IMapconService.Stub;
import com.huawei.android.bastet.IBastetManager;
import com.huawei.deliver.info.HwDeliverInfo;
import com.huawei.utils.reflect.EasyInvokeFactory;
import huawei.HwFeatureConfig;
import huawei.android.telephony.wrapper.WrapperFactory;
import huawei.cust.HwCustUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class HwConnectivityService extends ConnectivityService {
    private static final /* synthetic */ int[] -android-net-NetworkInfo$StateSwitchesValues = null;
    private static final int ACTION_BASTET_FILTER_ADD_LIST = 4;
    private static final int ACTION_BASTET_FILTER_CHECK = 1;
    private static final int ACTION_BASTET_FILTER_DEL_LIST = 5;
    private static final int ACTION_BASTET_FILTER_START = 2;
    private static final int ACTION_BASTET_FILTER_STOP = 3;
    private static final int ACTION_BASTET_FILTER_UNKNOWN = 0;
    public static final String ACTION_MAPCON_SERVICE_FAILED = "com.hisi.mapcon.servicefailed";
    public static final String ACTION_MAPCON_SERVICE_START = "com.hisi.mapcon.serviceStartResult";
    private static final String ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY = "com.huawei.wifipro.action.ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY";
    private static final String ACTION_OF_ANDROID_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static final String BASTET_SERVICE = "BastetService";
    private static final int BASTET_SERVICE_CHECK_DELAY = 500;
    public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private static final int CONNECTIVITY_SERVICE_NEED_SET_USER_DATA = 1100;
    private static final String COUNTRY_CODE_CN = "460";
    public static final int DEFAULT_PHONE_ID = 0;
    private static final String DEFAULT_SERVER = "connectivitycheck.android.com";
    private static final int DELAY_MAX_RETRY = 5;
    public static final String DISABEL_DATA_SERVICE_ACTION = "android.net.conn.DISABEL_DATA_SERVICE_ACTION";
    private static final String DISABLE_PORTAL_CHECK = "disable_portal_check";
    private static String ENABLE_NOT_REMIND_FUNCTION = "enable_not_remind_function";
    private static final int FILTER_START_DELAY_TIME = 15000;
    public static final String FLAG_SETUP_WIZARD = "flag_setup_wizard";
    private static final int HOUR_OF_MORNING = 5;
    private static final int HSM_NETWORKMANAGER_SERVICE_TRANSACTION_CODE = 201;
    private static final int HSM_NETWORK_POLICY_SERVICE_TRANSACTION_CODE = 204;
    private static final int HW_RULE_ALL_ACCESS = 0;
    private static final int HW_RULE_MOBILE_RESTRICT = 1;
    private static final int HW_RULE_WIFI_RESTRICT = 2;
    private static final int IM_ACTIVE_MILLIS = 30000;
    private static final int IM_HOUR_OF_MORNING = 5;
    private static final int IM_HOUR_OF_NIGHT = 23;
    public static final String IM_SPECIAL_PROC = "com.tencent.mm;com.tencent.mm:push;com.tencent.mobileqq;com.tencent.mobileqq:MSF;com.huawei.parentcontrol.parent;com.huawei.parentcontrol;com.huawei.hidisk";
    private static final int IM_TIMER_DAY_INTERVAL_MILLIS = 2400000;
    private static final int IM_TIMER_MORN_INTERVAL_MILLIS = 3600000;
    private static final int IM_TIMER_NIGHT_INTERVAL_MILLIS = 7200000;
    private static final int IM_TURNOFF_DC_DELAY_TIME = 5000;
    protected static final String INTENT_DAY_CLOCK = "android.filter.day.clock";
    protected static final String INTENT_IM_PENDING_PROCESS = "android.im.pending.process";
    protected static final String INTENT_IM_RESUME_PROCESS = "android.im.resume.process";
    protected static final String INTENT_NIGHT_CLOCK = "android.filter.night.clock";
    protected static final String INTENT_TURNOFF_DC = "android.telephony.turnoff_DC";
    private static final int INVALID_PID = -1;
    private static final boolean IS_CHINA = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", AppHibernateCst.INVALID_PKG));
    private static final int LTE_SERVICE_OFF = 0;
    private static final int LTE_SERVICE_ON = 1;
    public static final String MAPCON_START_INTENT = "com.hisi.mmsut.started";
    private static final int MINUTE_OF_MORNIG = 45;
    public static final int MMS_DOMAIN_CELLULAR_PREFER = 3;
    public static final int MMS_DOMAIN_VOLTE_PREFER = 1;
    public static final int MMS_DOMAIN_WIFI_ONLY = 0;
    public static final int MMS_DOMAIN_WIFI_PREFER = 2;
    public static final int MMS_TIMER_DELAYED = 10000;
    public static final String MM_PKG_NAME = "com.tencent.mm";
    public static final String MM_PUSH_NAME = "com.tencent.mm:push";
    private static final String MODULE_POWERSAVING = "powersaving";
    private static final String MODULE_WIFI = "wifi";
    public static final String MSG_ALL_CTRLSOCKET_ALLOWED = "android.ctrlsocket.all.allowed";
    public static final String MSG_SCROFF_CTRLSOCKET_STATS = "android.scroff.ctrlsocket.status";
    public static final String PG_PENDING_ACTION = "huawei.intent.action.PG_PENDING_ALARM_ACTION";
    protected static final String POWER_SAVING_ON = "power_saving_on";
    protected static final String PROPERTY_BTHOTSPOT_ON = "sys.isbthotspoton";
    protected static final String PROPERTY_USBTETHERING_ON = "sys.isusbtetheringon";
    protected static final String PROPERTY_WIFIHOTSPOT_ON = "sys.iswifihotspoton";
    private static final int RANDOM_TIME_SECOND = 1800;
    public static final int SERVICE_STATE_MMS = 1;
    public static final int SERVICE_TYPE_MMS = 0;
    public static final int SERVICE_TYPE_OTHERS = 2;
    private static final String SYSTEM_MANAGER_PKG_NAME = "com.huawei.systemmanager";
    private static final String TAG = "HwConnectivityService";
    protected static final int TURNOFF_DC_MILLIS = 1800000;
    protected static final String TURN_OFF_DC_STATE = "turn_off_dc_state";
    private static String VALUE_DISABLE_NOT_REMIND_FUNCTION = "false";
    private static String VALUE_ENABLE_NOT_REMIND_FUNCTION = "true";
    private static int VALUE_NOT_SHOW_PDP = 0;
    private static int VALUE_SHOW_PDP = 1;
    private static String WHETHER_SHOW_PDP_WARNING = "whether_show_pdp_warning";
    private static final String WIFI_AP_MANUAL_CONNECT = "wifi_ap_manual_connect";
    public static final int WIFI_PULS_CSP_DISENABLED = 1;
    public static final int WIFI_PULS_CSP_ENABLED = 0;
    private static ConnectivityServiceUtils connectivityServiceUtils = ((ConnectivityServiceUtils) EasyInvokeFactory.getInvokeUtils(ConnectivityServiceUtils.class));
    private static final String ctrl_socket_version = "v2";
    private static final String descriptor = "android.net.IConnectivityManager";
    private static final boolean isAllowBastetFilter = SystemProperties.getBoolean("ro.config.hw_bastet_filter", true);
    protected static final boolean isAlwaysAllowMMS = SystemProperties.getBoolean("ro.config.hw_always_allow_mms", false);
    private static boolean mBastetFilterEnable = false;
    private static int mLteMobileDataState = 3;
    private static INetworkStatsService mStatsService;
    private int ALLOW_ALL_CTRL_SOCKET_LEVEL = 2;
    private int ALLOW_NO_CTRL_SOCKET_LEVEL = 0;
    private int ALLOW_PART_CTRL_SOCKET_LEVEL = 1;
    private int ALLOW_SPECIAL_CTRL_SOCKET_LEVEL = 3;
    private int CANCEL_SPECIAL_PID = 103;
    private int GET_KEEP_SOCKET_STATS = 104;
    private int KEEP_SOCKET = 1;
    private int MAX_REGISTERED_PKG_NUM = 10;
    private int NORMAL_POWER_SAVING_MODE = 0;
    private int POWER_SAVING_MODE = 4;
    private int PUSH_AVAILABLE = 2;
    private int SET_SAVING = 100;
    private int SET_SPECIAL_PID = 102;
    private int SUPER_POWER_SAVING_MODE = 3;
    private final Uri WHITELIST_URI = Secure.getUriFor("push_white_apps");
    private int curMmsDataSub = -1;
    private int curPrefDataSubscription = -1;
    private boolean isWaitWifiMms = false;
    private boolean isWifiMmsAlready = false;
    private ActivityManager mActivityManager;
    private DeathRecipient mBastetDeathRecipient = new DeathRecipient() {
        public void binderDied() {
            Log.e(HwConnectivityService.TAG, "Bastet service has died!");
            if (HwConnectivityService.isAllowBastetFilter) {
                synchronized (HwConnectivityService.this.mBastetFilterLock) {
                    if (HwConnectivityService.this.mBastetService != null) {
                        HwConnectivityService.this.mBastetService.unlinkToDeath(this, 0);
                        HwConnectivityService.this.mBastetService = null;
                        HwConnectivityService.this.mIBastetManager = null;
                    }
                    Message msg = HwConnectivityService.this.mHandler.obtainMessage();
                    msg.what = 2;
                    HwConnectivityService.this.mHandler.sendMessageDelayed(msg, 500);
                }
            }
        }
    };
    private int mBastetDiedRetry = 0;
    private Object mBastetFilterLock = new Object();
    private IBinder mBastetService = null;
    private Context mContext;
    private CtrlSocketInfo mCtrlSocketInfo = new CtrlSocketInfo();
    private HwCustConnectivityService mCust = ((HwCustConnectivityService) HwCustUtils.createObj(HwCustConnectivityService.class, new Object[0]));
    private AlertDialog mDataServiceToPdpDialog = null;
    protected PendingIntent mDayClockIntent = null;
    private ContentObserver mDbObserver;
    protected long mDeltaTime = 0;
    private Object mFilterDelayLock = new Object();
    private Handler mFilterHandler;
    private int mFilterKeepPid = 0;
    private int mFilterMsgFlag = 0;
    private int mFilterSpecialSocket = 0;
    private Object mFilterUidlistLock = new Object();
    protected boolean mFirst = true;
    private NetworkStateReceiver mGcmFixIntentReceiver = new NetworkStateReceiver();
    private Handler mHandler;
    private HeartbeatReceiver mHeartbeatReceiver = new HeartbeatReceiver();
    private IBastetManager mIBastetManager = null;
    private ArrayList<String> mIMArrayList = new ArrayList();
    protected PendingIntent mIMPendingIntent = null;
    protected PendingIntent mIMResumeIntent = null;
    private WakeLock mIMWakeLock;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            HwConnectivityService.log("mIntentReceiver begin");
            String action = intent.getAction();
            boolean disable = HwFrameworkFactory.getHwKeyguardManager().isLockScreenDisabled(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this));
            if ("android.intent.action.USER_PRESENT".equals(action)) {
                HwConnectivityService.this.mIsUnlockStats = true;
            }
            boolean locked = !HwConnectivityService.this.mIsUnlockStats;
            HwConnectivityService.log("CtrlSocket Receiver,disable: " + disable + " locked: " + locked + " action: " + action + " mSmartKeyguardLevel: " + HwConnectivityExService.mSmartKeyguardLevel + " mStartPowerSaving: " + HwConnectivityService.this.mStartPowerSaving);
            if (!locked && "android.intent.action.USER_PRESENT".equals(action)) {
                Log.d(HwConnectivityService.TAG, "receive ACTION_USER_PRESENT and unlock.");
                HwConnectivityService.this.processCtrlSocket(Process.myUid(), HwConnectivityService.this.SET_SAVING, 0);
            }
            if ("max_level".equals(HwConnectivityExService.mSmartKeyguardLevel) && !disable && (locked || "android.intent.action.USER_PRESENT".equals(action))) {
                locked = HwConnectivityService.this.mStartPowerSaving;
                if ("android.intent.action.USER_PRESENT".equals(action)) {
                    HwConnectivityService.log("CtrlSocket receive keyguard unlock intent!");
                    HwConnectivityService.this.restoreScrOnStatus();
                }
            } else {
                if ("max_level".equals(HwConnectivityExService.mSmartKeyguardLevel) && !disable) {
                    locked = HwConnectivityService.this.mStartPowerSaving;
                }
                if ("android.intent.action.SCREEN_ON".equals(action)) {
                    HwConnectivityService.log("CtrlSocket receive screen on intent!");
                    HwConnectivityService.this.restoreScrOnStatus();
                }
            }
            Object obj;
            if ("android.intent.action.SCREEN_OFF".equals(action)) {
                HwConnectivityService.this.mIsUnlockStats = false;
                obj = HwConnectivityService.this.mPowerSavingLock;
                synchronized (obj) {
                    HwConnectivityService.log("receive screen off intent!");
                    boolean isWifiApOn = SystemProperties.getBoolean(HwConnectivityService.PROPERTY_WIFIHOTSPOT_ON, false);
                    boolean isUsbTetheringOn = SystemProperties.getBoolean(HwConnectivityService.PROPERTY_USBTETHERING_ON, false);
                    boolean isBtTetheringOn = SystemProperties.getBoolean(HwConnectivityService.PROPERTY_BTHOTSPOT_ON, false);
                    boolean isCharging = false;
                    HwConnectivityService.log("wifi tethering: " + isWifiApOn);
                    HwConnectivityService.log("Usb tethering: " + isUsbTetheringOn);
                    HwConnectivityService.log("bt tethering: " + isBtTetheringOn);
                    Intent batteryStatus = context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
                    if (batteryStatus != null) {
                        int pluggedStatus = batteryStatus.getIntExtra("plugged", 0);
                        HwConnectivityService.log("pluggedStatus: " + pluggedStatus);
                        isCharging = pluggedStatus != 0;
                    }
                    HwConnectivityService.log("is charging: " + isCharging);
                    boolean isFlightMode = Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0;
                    if (isCharging || isWifiApOn || isUsbTetheringOn || isBtTetheringOn || isFlightMode) {
                        HwConnectivityService.this.cancelPowerSaving();
                        HwConnectivityService.this.mFirst = true;
                    } else if (HwConnectivityService.this.getMobileDataEnabled() && HwConnectivityService.this.getPowerSavingState() && (!HwConnectivityService.this.mStartPowerSaving || ("max_level".equals(HwConnectivityExService.mSmartKeyguardLevel) && !disable))) {
                        HwConnectivityService.log("start powersaving action!");
                        if (disable || "min_level".equals(HwConnectivityExService.mSmartKeyguardLevel)) {
                            HwConnectivityService.this.tryPowerSaving();
                        } else {
                            if ("max_level".equals(HwConnectivityExService.mSmartKeyguardLevel) && !disable) {
                                locked = HwConnectivityService.this.mStartPowerSaving;
                            }
                            HwConnectivityService.this.tryPowerSavingI(locked);
                        }
                        HwConnectivityService.this.mStartPowerSaving = true;
                    }
                }
                return;
            } else if (HwConnectivityService.INTENT_TURNOFF_DC.equals(action)) {
                obj = HwConnectivityService.this.mPowerSavingLock;
                synchronized (obj) {
                    if (HwConnectivityService.this.mStartPowerSaving) {
                        HwConnectivityService.this.calcUseCtrlSocketLevel();
                        Log.d(HwConnectivityService.TAG, "CtrlSocket allowCtrlSocketLevel = " + HwConnectivityService.this.mCtrlSocketInfo.mAllowCtrlSocketLevel);
                        if (HwConnectivityService.this.ALLOW_NO_CTRL_SOCKET_LEVEL == HwConnectivityService.this.mCtrlSocketInfo.mAllowCtrlSocketLevel) {
                            Slog.d(HwConnectivityService.TAG, "turn off Data Connection!");
                            HwConnectivityService.this.turnoffDC();
                            it = new Intent(HwConnectivityService.MSG_SCROFF_CTRLSOCKET_STATS);
                            it.putExtra("ctrl_socket_status", false);
                            HwConnectivityService.this.mContext.sendBroadcastAsUser(it, UserHandle.ALL);
                            Log.d(HwConnectivityService.TAG, "CtrlSocket allow no broadcast");
                        } else if (HwConnectivityService.this.isWifiAvailable()) {
                            Log.d(HwConnectivityService.TAG, "[PS filter]turnoffDC mShouldPowerSave true");
                            HwConnectivityService.this.mShouldPowerSave = true;
                        } else {
                            Log.d(HwConnectivityService.TAG, "startMobileFilter");
                            HwConnectivityService.this.startMobileFilter();
                        }
                    }
                }
            } else {
                if (HwConnectivityService.ACTION_OF_ANDROID_BOOT_COMPLETED.equals(action)) {
                    HwConnectivityService.log("receive Intent.ACTION_BOOT_COMPLETED!");
                    HwConnectivityService.this.sendWifiBroadcastAfterBootCompleted = true;
                    if (SystemProperties.getBoolean("ro.config.hw_power_saving", false) && HwConnectivityService.this.getTurnOffDCState()) {
                        HwConnectivityService.log("exception of power saving when power off,then turnonDC");
                        HwConnectivityService.this.turnonDC();
                    }
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    if (HwConnectivityService.this.mShouldPowerSave) {
                        NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                        if (info != null && info.isConnected()) {
                            HwConnectivityService.this.processCtrlSocket(Process.myUid(), HwConnectivityService.this.SET_SAVING, 0);
                        } else if (!(info == null || info.isConnected() || HwConnectivityService.this.ALLOW_NO_CTRL_SOCKET_LEVEL == HwConnectivityService.this.getUseCtrlSocketLevel())) {
                            HwConnectivityService.this.startMobileFilter();
                        }
                    } else {
                        Log.d(HwConnectivityService.TAG, "[PS filter] should not power save");
                        return;
                    }
                } else if (HwConnectivityService.INTENT_IM_RESUME_PROCESS.equals(action)) {
                    if (HwConnectivityService.this.mCtrlSocketInfo.mAllowCtrlSocketLevel != HwConnectivityService.this.ALLOW_NO_CTRL_SOCKET_LEVEL) {
                        HwConnectivityService.this.turnoffDC();
                        if (HwConnectivityService.this.mIMWakeLock.isHeld()) {
                            HwConnectivityService.this.mIMWakeLock.release();
                        }
                        HwConnectivityService.this.mIMWakeLock.acquire(30000);
                        HwConnectivityService.this.mHandler.sendMessageDelayed(HwConnectivityService.this.mHandler.obtainMessage(1), 5000);
                    } else {
                        return;
                    }
                } else if (HwConnectivityService.INTENT_IM_PENDING_PROCESS.equals(action)) {
                    if (HwConnectivityService.this.mCtrlSocketInfo.mAllowCtrlSocketLevel != HwConnectivityService.this.ALLOW_NO_CTRL_SOCKET_LEVEL) {
                        HwConnectivityService.this.mIMArrayList.clear();
                        HwConnectivityService.this.mIMArrayList.add(HwConnectivityService.MM_PKG_NAME);
                        if (!HwConnectivityService.this.isWifiAvailable()) {
                            Log.d(HwConnectivityService.TAG, "CtrlSocketDo set Power Saving Mode 1");
                            HwConnectivityService.this.processCtrlSocket(Process.myUid(), HwConnectivityService.this.SET_SAVING, 1);
                        }
                        it = new Intent(HwConnectivityService.PG_PENDING_ACTION);
                        it.putExtra("enable", true);
                        it.putExtra("applist", HwConnectivityService.this.mIMArrayList);
                        it.putExtra(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE, 1);
                        HwConnectivityService.this.mContext.sendBroadcast(it, "com.huawei.powergenie.receiverPermission");
                        Log.d(HwConnectivityService.TAG, "tell powegenie to pending applist = " + HwConnectivityService.this.mIMArrayList);
                        HwConnectivityService.this.setIMResumeTimer();
                        if (HwConnectivityService.this.mIMWakeLock != null && HwConnectivityService.this.mIMWakeLock.isHeld()) {
                            HwConnectivityService.this.mIMWakeLock.release();
                        }
                    } else {
                        return;
                    }
                } else if (HwConnectivityService.INTENT_NIGHT_CLOCK.equals(action)) {
                    Log.d(HwConnectivityService.TAG, "[PS filter]receive INTENT_NIGHT_CLOCK and curHour = " + HwConnectivityService.this.getHourOfDay());
                    HwConnectivityService.this.turnoffDC();
                    HwConnectivityService.this.setDayClockTimer();
                } else if (HwConnectivityService.INTENT_DAY_CLOCK.equals(action)) {
                    Log.d(HwConnectivityService.TAG, "[PS filter]receive INTENT_DAY_CLOCK and curHour = " + HwConnectivityService.this.getHourOfDay());
                    HwConnectivityService.this.turnonDC();
                    HwConnectivityService.this.setNightClockTimer();
                }
            }
        }
    };
    private boolean mIsSimStateChanged = false;
    private boolean mIsUnlockStats = true;
    protected long mLastPowerOffTime = 0;
    private NetworkRequest mLteMmsNetworkRequest = null;
    LteMmsTimer mLteMmsTimer = new LteMmsTimer();
    protected BroadcastReceiver mMapconIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent mapconIntent) {
            if (mapconIntent == null) {
                HwConnectivityService.log("intent is null");
                return;
            }
            String action = mapconIntent.getAction();
            HwConnectivityService.log("onReceive: action=" + action);
            if (HwConnectivityService.MAPCON_START_INTENT.equals(action)) {
                ServiceConnection mConnection = new ServiceConnection() {
                    public void onServiceConnected(ComponentName className, IBinder service) {
                        HwConnectivityService.this.mMapconService = Stub.asInterface(service);
                    }

                    public void onServiceDisconnected(ComponentName className) {
                        HwConnectivityService.this.mMapconService = null;
                    }
                };
                HwConnectivityService.this.mContext.bindServiceAsUser(new Intent().setClassName("com.hisi.mapcon", "com.hisi.mapcon.MapconService"), mConnection, 1, UserHandle.OWNER);
            } else if (HwConnectivityService.CONNECTIVITY_CHANGE_ACTION.equals(action)) {
                NetworkInfo networkInfo = (NetworkInfo) mapconIntent.getParcelableExtra("networkInfo");
                if (networkInfo != null && 2 == networkInfo.getType() && networkInfo.isConnected()) {
                    HwConnectivityService.log("onReceive: mms connection ok");
                    HwConnectivityService.this.mWifiMmsTimer.stop();
                }
            } else if (HwConnectivityService.ACTION_MAPCON_SERVICE_START.equals(action) && mapconIntent.getIntExtra("serviceType", 2) == 0) {
                HwConnectivityService.loge("Recive ACTION_MAPCON_SERVICE_START");
                HwConnectivityService.this.mLteMmsTimer.stop();
                HwConnectivityService.this.isWifiMmsAlready = true;
            } else if (HwConnectivityService.ACTION_MAPCON_SERVICE_FAILED.equals(action) && mapconIntent.getIntExtra("serviceType", 2) == 0) {
                HwConnectivityService.loge("Recive ACTION_MAPCON_SERVICE_FAILED");
                if (HwConnectivityService.this.mLteMmsTimer.isRunning()) {
                    HwConnectivityService.loge("stop mLteMmsTimer");
                    if (HwConnectivityService.this.mLteMmsTimer.isRunning()) {
                        HwConnectivityService.loge("stop mLteMmsTimer and back to cellular sendUpdatedScoreToFactories");
                        HwConnectivityService.this.sendUpdatedScoreToFactories(HwConnectivityService.this.mLteMmsNetworkRequest, 0);
                    }
                    HwConnectivityService.this.mLteMmsTimer.stop();
                    HwConnectivityService.this.isWaitWifiMms = false;
                }
            }
        }
    };
    protected IMapconService mMapconService;
    protected PendingIntent mNightClockIntent = null;
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            HwConnectivityService.this.updateCallState(state);
        }
    };
    protected Object mPowerSavingLock = new Object();
    private boolean mRemindService = SystemProperties.getBoolean("ro.config.DataPopFirstBoot", false);
    private String mServer;
    protected boolean mShouldPowerSave = false;
    private boolean mShowDlgEndCall = false;
    private boolean mShowDlgTurnOfDC = true;
    private BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                Log.d(HwConnectivityService.TAG, "CtrlSocket receive ACTION_SIM_STATE_CHANGED");
                HwConnectivityService.this.mIsSimStateChanged = true;
            }
        }
    };
    protected boolean mStartPowerSaving = false;
    private BroadcastReceiver mTetheringReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && HwGnssCommParam.ACTION_USB_STATE.equals(action)) {
                boolean usbConnected = intent.getBooleanExtra(HwGnssCommParam.USB_CONNECTED, false);
                boolean rndisEnabled = intent.getBooleanExtra("rndis", false);
                int is_usb_tethering_on = Secure.getInt(HwConnectivityService.this.mContext.getContentResolver(), "usb_tethering_on", 0);
                Log.d(HwConnectivityService.TAG, "mTetheringReceiver usbConnected = " + usbConnected + ",rndisEnabled = " + rndisEnabled + ", is_usb_tethering_on = " + is_usb_tethering_on);
                if (1 == is_usb_tethering_on && usbConnected && !rndisEnabled) {
                    new Thread() {
                        public void run() {
                            do {
                                try {
                                    if (HwConnectivityService.this.isSystemBootComplete()) {
                                        Thread.sleep(200);
                                    } else {
                                        Thread.sleep(1000);
                                    }
                                } catch (InterruptedException e) {
                                    Log.e(HwConnectivityService.TAG, "wait to boot complete error");
                                }
                            } while (!HwConnectivityService.this.isSystemBootComplete());
                            HwConnectivityService.this.setUsbTethering(true);
                        }
                    }.start();
                }
            }
        }
    };
    private HandlerThread mThread;
    protected PendingIntent mTurnoffDCIntent = null;
    private URL mURL;
    WifiMmsTimer mWifiMmsTimer = new WifiMmsTimer();
    private Messenger mWifiNetworkMessenger;
    private boolean m_filterIsStarted = false;
    private Set<Integer> m_filterUidSet = new HashSet();
    private int phoneId = -1;
    private boolean sendWifiBroadcastAfterBootCompleted = false;
    private Set<Integer> uidSet = new HashSet();
    private WifiDisconnectManager wifiDisconnectManager = new WifiDisconnectManager();

    private static class CtrlSocketInfo {
        public int mAllowCtrlSocketLevel;
        public List<String> mPushWhiteListPkg;
        public int mRegisteredCount;
        public List<String> mRegisteredPkg;
        public List<String> mScrOffActPkg;
        public int mScrOffActiveCount;
        public Map<String, Integer> mSpecialPkgMap;
        public List<String> mSpecialWhiteListPkg;

        CtrlSocketInfo() {
            this.mRegisteredPkg = null;
            this.mScrOffActPkg = null;
            this.mPushWhiteListPkg = null;
            this.mSpecialWhiteListPkg = null;
            this.mSpecialPkgMap = null;
            this.mAllowCtrlSocketLevel = 0;
            this.mRegisteredCount = 0;
            this.mScrOffActiveCount = 0;
            this.mRegisteredPkg = new ArrayList();
            this.mScrOffActPkg = new ArrayList();
            this.mPushWhiteListPkg = new ArrayList();
            this.mSpecialWhiteListPkg = new ArrayList();
            this.mSpecialPkgMap = new HashMap();
        }
    }

    private class HwConnectivityServiceHandler extends Handler {
        private static final int EVENT_SHOW_ENABLE_PDP_DIALOG = 0;
        private static final int EVENT_TURN_OFF_DC_TIMEOUT = 1;
        private static final int MESSAGE_BASTET_SERVICE_DIED = 2;

        private HwConnectivityServiceHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    HwConnectivityService.this.handleShowEnablePdpDialog();
                    return;
                case 1:
                    HwConnectivityService.this.handleIMReceivingMsgAction();
                    return;
                case 2:
                    if (HwConnectivityService.isAllowBastetFilter) {
                        HwConnectivityService.this.handleBastetServiceDied();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class HwFilterHandler extends Handler {
        private static final int MSG_SET_OPTION = 1;
        private static final int MSG_START_STOP = 0;

        public HwFilterHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    HwConnectivityService.this.handleFilterMsg(msg.arg1, msg.arg2);
                    return;
                case 1:
                    if (HwConnectivityService.mBastetFilterEnable) {
                        HwConnectivityService.this.setBastetFilterInfo(msg.arg1, msg.arg2);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class LteMmsTimer {
        private boolean isRunning = false;
        private TimerTask mTimerTask;
        private Timer timer = new Timer();

        private void getTimerTask() {
            this.mTimerTask = new TimerTask() {
                public void run() {
                    LteMmsTimer.this.isRunning = false;
                    HwConnectivityService.this.sendUpdatedScoreToFactories(HwConnectivityService.this.mLteMmsNetworkRequest, 0);
                }
            };
        }

        public LteMmsTimer() {
            getTimerTask();
        }

        public void start() {
            HwConnectivityService.log("LteMmsTimer start");
            this.mTimerTask.cancel();
            getTimerTask();
            this.timer.schedule(this.mTimerTask, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
            this.isRunning = true;
        }

        public void stop() {
            this.mTimerTask.cancel();
            this.isRunning = false;
        }

        public boolean isRunning() {
            return this.isRunning;
        }
    }

    private class MobileEnabledSettingObserver extends ContentObserver {
        public MobileEnabledSettingObserver(Handler handler) {
            super(handler);
        }

        public void register() {
            HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver().registerContentObserver(Global.getUriFor("device_provisioned"), true, this);
        }

        public void onChange(boolean selfChange) {
            if (HwConnectivityService.this.mRemindService || HwConnectivityService.this.checkDataServiceRemindMsim()) {
                super.onChange(selfChange);
                if (!HwConnectivityService.this.getMobileDataEnabled() && HwConnectivityService.this.mDataServiceToPdpDialog == null) {
                    HwConnectivityService.this.mDataServiceToPdpDialog = HwConnectivityService.this.createWarningToPdp();
                    HwConnectivityService.this.mDataServiceToPdpDialog.show();
                }
            }
        }
    }

    private class WifiDisconnectManager {
        private static final String ACTION_SWITCH_TO_MOBILE_NETWORK = "android.intent.action.SWITCH_TO_MOBILE_NETWORK";
        private static final String ACTION_WIFI_NETWORK_CONNECTION_CHANGED = "android.intent.action.WIFI_NETWORK_CONNECTION_CHANGED";
        private static final String CONNECT_STATE = "connect_state";
        private static final String SWITCH_STATE = "switch_state";
        private static final int SWITCH_TO_WIFI_AUTO = 0;
        private static final String SWITCH_TO_WIFI_TYPE = "wifi_connect_type";
        private static final String WIFI_TO_PDP = "wifi_to_pdp";
        private static final int WIFI_TO_PDP_AUTO = 1;
        private static final int WIFI_TO_PDP_NEVER = 2;
        private static final int WIFI_TO_PDP_NOTIFY = 0;
        private final boolean REMIND_WIFI_TO_PDP;
        private boolean mDialogHasShown;
        State mLastWifiState;
        private BroadcastReceiver mNetworkSwitchReceiver;
        private boolean mShouldStartMobile;
        private Handler mSwitchHandler;
        private OnDismissListener mSwitchPdpListener;
        protected AlertDialog mWifiToPdpDialog;
        private boolean shouldShowDialogWhenConnectFailed;

        private WifiDisconnectManager() {
            this.REMIND_WIFI_TO_PDP = SystemProperties.getBoolean("ro.config.hw_RemindWifiToPdp", false);
            this.mWifiToPdpDialog = null;
            this.mShouldStartMobile = false;
            this.shouldShowDialogWhenConnectFailed = true;
            this.mDialogHasShown = false;
            this.mLastWifiState = State.DISCONNECTED;
            this.mSwitchPdpListener = new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    HwTelephonyFactory.getHwDataServiceChrManager().sendMonitorWifiSwitchToMobileMessage(HwConnectivityService.IM_TURNOFF_DC_DELAY_TIME);
                    if (WifiDisconnectManager.this.mShouldStartMobile) {
                        HwConnectivityService.this.setMobileDataEnabled("wifi", true);
                        HwConnectivityService.log("you have restart Mobile data service!");
                    }
                    WifiDisconnectManager.this.mShouldStartMobile = false;
                    WifiDisconnectManager.this.mWifiToPdpDialog = null;
                }
            };
            this.mNetworkSwitchReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (!WifiDisconnectManager.ACTION_SWITCH_TO_MOBILE_NETWORK.equals(intent.getAction())) {
                        return;
                    }
                    if (intent.getBooleanExtra(WifiDisconnectManager.SWITCH_STATE, true)) {
                        HwConnectivityService.this.wifiDisconnectManager.switchToMobileNetwork();
                    } else {
                        HwConnectivityService.this.wifiDisconnectManager.cancelSwitchToMobileNetwork();
                    }
                }
            };
            this.mSwitchHandler = new Handler() {
                public void handleMessage(Message msg) {
                    HwConnectivityService.log("mSwitchHandler recieve msg =" + msg.what);
                    switch (msg.what) {
                        case 0:
                            WifiDisconnectManager.this.switchToMobileNetwork();
                            return;
                        default:
                            return;
                    }
                }
            };
        }

        private boolean getAirplaneModeEnable() {
            boolean retVal = System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), "airplane_mode_on", 0) == 1;
            HwConnectivityService.log("getAirplaneModeEnable returning " + retVal);
            return retVal;
        }

        private AlertDialog createSwitchToPdpWarning() {
            HwConnectivityService.log("create dialog of switch to pdp");
            HwTelephonyFactory.getHwDataServiceChrManager().removeMonitorWifiSwitchToMobileMessage();
            Builder buider = new Builder(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this), 33947691);
            View view = LayoutInflater.from(buider.getContext()).inflate(34013282, null);
            final CheckBox checkBox = (CheckBox) view.findViewById(34603229);
            buider.setView(view);
            buider.setTitle(33685520);
            buider.setIcon(17301543);
            buider.setPositiveButton(33685559, new OnClickListener() {
                public void onClick(DialogInterface dialoginterface, int i) {
                    WifiDisconnectManager.this.mShouldStartMobile = true;
                    HwConnectivityService.log("setPositiveButton: mShouldStartMobile set true");
                    WifiDisconnectManager.this.checkUserChoice(checkBox.isChecked(), true);
                }
            });
            buider.setNegativeButton(33685560, new OnClickListener() {
                public void onClick(DialogInterface dialoginterface, int i) {
                    HwConnectivityService.log("you have chose to disconnect Mobile data service!");
                    WifiDisconnectManager.this.mShouldStartMobile = false;
                    WifiDisconnectManager.this.checkUserChoice(checkBox.isChecked(), false);
                }
            });
            AlertDialog dialog = buider.create();
            dialog.setCancelable(false);
            dialog.getWindow().setType(2008);
            return dialog;
        }

        private void checkUserChoice(boolean rememberChoice, boolean enableDataConnect) {
            int showPopState;
            if (!rememberChoice) {
                showPopState = 0;
            } else if (enableDataConnect) {
                showPopState = 1;
            } else {
                showPopState = 0;
            }
            HwConnectivityService.log("checkUserChoice showPopState:" + showPopState + ", enableDataConnect:" + enableDataConnect);
            System.putInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), WIFI_TO_PDP, showPopState);
        }

        private void sendWifiBroadcast(boolean isConnectingOrConnected) {
            if (ActivityManagerNative.isSystemReady() && HwConnectivityService.this.sendWifiBroadcastAfterBootCompleted) {
                HwConnectivityService.log("notify settings:" + isConnectingOrConnected);
                Intent intent = new Intent(ACTION_WIFI_NETWORK_CONNECTION_CHANGED);
                intent.putExtra(CONNECT_STATE, isConnectingOrConnected);
                HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).sendBroadcast(intent);
            }
        }

        private boolean shouldNotifySettings() {
            if (!isSwitchToWifiSupported() || System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), SWITCH_TO_WIFI_TYPE, 0) == 0) {
                return false;
            }
            return true;
        }

        private boolean isSwitchToWifiSupported() {
            if ("CMCC".equalsIgnoreCase(SystemProperties.get("ro.config.operators", AppHibernateCst.INVALID_PKG))) {
                return true;
            }
            return HwConnectivityService.this.mCust.isSupportWifiConnectMode();
        }

        private void switchToMobileNetwork() {
            if (getAirplaneModeEnable()) {
                HwConnectivityService.this.enableDefaultTypeAPN(true);
            } else if (this.shouldShowDialogWhenConnectFailed || !this.mDialogHasShown) {
                int value = System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), WIFI_TO_PDP, 1);
                HwConnectivityService.log("WIFI_TO_PDP value =" + value);
                int wifiplusvalue = System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), "wifi_csp_dispaly_state", 1);
                HwConnectivityService.log("wifiplus_csp_dispaly_state value =" + wifiplusvalue);
                HwVSimManager hwVSimManager = HwVSimManager.getDefault();
                if (hwVSimManager != null && hwVSimManager.isVSimEnabled()) {
                    HwConnectivityService.log("vsim is enabled and following process will execute enableDefaultTypeAPN(true), so do nothing that likes value == WIFI_TO_PDP_AUTO");
                } else if (value == 0) {
                    if (wifiplusvalue == 0) {
                        HwConnectivityService.log("wifi_csp_dispaly_state = 0, Don't create WifiToPdpDialog");
                        HwConnectivityService.log("enableDefaultTypeAPN(true) in switchToMobileNetwork()  ");
                        HwConnectivityService.this.enableDefaultTypeAPN(true);
                        return;
                    }
                    HwConnectivityService.this.setMobileDataEnabled("wifi", false);
                    this.mShouldStartMobile = true;
                    this.mDialogHasShown = true;
                    this.mWifiToPdpDialog = createSwitchToPdpWarning();
                    this.mWifiToPdpDialog.setOnDismissListener(this.mSwitchPdpListener);
                    this.mWifiToPdpDialog.show();
                } else if (value != 1) {
                    if (1 == wifiplusvalue) {
                        HwConnectivityService.this.setMobileDataEnabled("wifi", false);
                    } else {
                        HwConnectivityService.log("wifi_csp_dispaly_state = 0, Don't setMobileDataEnabled");
                    }
                }
                HwConnectivityService.log("enableDefaultTypeAPN(true) in switchToMobileNetwork( )");
                HwConnectivityService.this.enableDefaultTypeAPN(true);
            }
        }

        private void cancelSwitchToMobileNetwork() {
            if (this.mWifiToPdpDialog != null) {
                Log.d(HwConnectivityService.TAG, "cancelSwitchToMobileNetwork and mWifiToPdpDialog is showing");
                this.mShouldStartMobile = true;
                this.mWifiToPdpDialog.dismiss();
            }
        }

        private void registerReceiver() {
            if (this.REMIND_WIFI_TO_PDP && isSwitchToWifiSupported()) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_SWITCH_TO_MOBILE_NETWORK);
                HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).registerReceiver(this.mNetworkSwitchReceiver, filter);
            }
        }

        protected void hintUserSwitchToMobileWhileWifiDisconnected(State state, int type) {
            HwConnectivityService.log("hintUserSwitchToMobileWhileWifiDisconnected, state=" + state + "  type =" + type);
            boolean shouldEnableDefaultTypeAPN = true;
            if (this.REMIND_WIFI_TO_PDP) {
                if (state == State.DISCONNECTED && type == 1 && HwConnectivityService.this.getMobileDataEnabled()) {
                    if (this.mLastWifiState == State.CONNECTED) {
                        int value = System.getInt(HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).getContentResolver(), WIFI_TO_PDP, 1);
                        HwConnectivityService.log("WIFI_TO_PDP value     =" + value);
                        if (value == 1) {
                            HwConnectivityService.this.enableDefaultTypeAPN(true);
                            return;
                        }
                        this.shouldShowDialogWhenConnectFailed = true;
                        HwConnectivityService.log("mShouldEnableDefaultTypeAPN was set false");
                        shouldEnableDefaultTypeAPN = false;
                    }
                    if (shouldNotifySettings()) {
                        sendWifiBroadcast(false);
                    } else if (getAirplaneModeEnable()) {
                        shouldEnableDefaultTypeAPN = true;
                    } else {
                        this.mSwitchHandler.sendMessageDelayed(this.mSwitchHandler.obtainMessage(0), 5000);
                        HwConnectivityService.log("switch message will be send in 5 seconds");
                    }
                    if (this.mLastWifiState == State.CONNECTING) {
                        this.shouldShowDialogWhenConnectFailed = false;
                    }
                } else if ((state == State.CONNECTED || state == State.CONNECTING) && type == 1) {
                    if (state == State.CONNECTED) {
                        this.mDialogHasShown = false;
                    }
                    if (shouldNotifySettings()) {
                        sendWifiBroadcast(true);
                    } else if (this.mSwitchHandler.hasMessages(0)) {
                        this.mSwitchHandler.removeMessages(0);
                        HwConnectivityService.log("switch message was removed");
                    }
                    if (this.mWifiToPdpDialog != null) {
                        this.mShouldStartMobile = true;
                        this.mWifiToPdpDialog.dismiss();
                    }
                }
                if (type == 1) {
                    HwConnectivityService.log("mLastWifiState =" + this.mLastWifiState);
                    this.mLastWifiState = state;
                }
            }
            if (shouldEnableDefaultTypeAPN && state == State.DISCONNECTED && type == 1) {
                HwConnectivityService.log("enableDefaultTypeAPN(true) in hintUserSwitchToMobileWhileWifiDisconnected");
                HwConnectivityService.this.enableDefaultTypeAPN(true);
            }
        }

        protected void makeDefaultAndHintUser(NetworkAgentInfo newNetwork) {
        }
    }

    private class WifiMmsTimer {
        private boolean isRunning = false;
        private TimerTask mTimerTask;
        private Timer timer = new Timer();

        private void getTimerTask() {
            this.mTimerTask = new TimerTask() {
                public void run() {
                    WifiMmsTimer.this.isRunning = false;
                    try {
                        HwConnectivityService.this.mMapconService.setupTunnelOverWifi(0, 0, null, null);
                    } catch (RemoteException e) {
                        HwConnectivityService.loge("WifiMmsTimer,setupTunnelOverWifi,err=" + e.toString());
                    }
                }
            };
        }

        public WifiMmsTimer() {
            getTimerTask();
        }

        public void start() {
            HwConnectivityService.log("WifiMmsTimer start");
            this.mTimerTask.cancel();
            getTimerTask();
            this.timer.schedule(this.mTimerTask, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
            this.isRunning = true;
        }

        public void stop() {
            this.mTimerTask.cancel();
            this.isRunning = false;
        }

        public boolean isRunning() {
            return this.isRunning;
        }
    }

    private static /* synthetic */ int[] -getandroid-net-NetworkInfo$StateSwitchesValues() {
        if (-android-net-NetworkInfo$StateSwitchesValues != null) {
            return -android-net-NetworkInfo$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.CONNECTED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.CONNECTING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.DISCONNECTED.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.DISCONNECTING.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.SUSPENDED.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.UNKNOWN.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        -android-net-NetworkInfo$StateSwitchesValues = iArr;
        return iArr;
    }

    private static void log(String s) {
        Slog.d(TAG, s);
    }

    private static void loge(String s) {
        Slog.e(TAG, s);
    }

    public HwConnectivityService(Context context, INetworkManagementService netd, INetworkStatsService statsService, INetworkPolicyManager policyManager) {
        super(context, netd, statsService, policyManager);
        this.mContext = context;
        initCtrlSocket(context);
        registerSimStateReceiver(context);
        this.wifiDisconnectManager.registerReceiver();
        registerPhoneStateListener(context);
        registerBootStateListener(context);
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mHandler = new HwConnectivityServiceHandler();
        if (Boolean.valueOf(SystemProperties.getBoolean("ro.config.hw_vowifi", false)).booleanValue()) {
            registerMapconIntentReceiver(context);
        }
        this.mServer = Global.getString(context.getContentResolver(), "captive_portal_server");
        if (this.mServer == null) {
            this.mServer = DEFAULT_SERVER;
        }
        SystemProperties.set("sys.defaultapn.enabled", "true");
        registerTetheringReceiver(context);
        this.mIMWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "IMReceiveMsg");
        if (isAllowBastetFilter) {
            checkBastetFilter();
        }
        initFilterThread();
        initGCMFixer(context);
    }

    private void initGCMFixer(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OF_ANDROID_BOOT_COMPLETED);
        filter.addAction(CONNECTIVITY_CHANGE_ACTION);
        this.mContext.registerReceiver(this.mGcmFixIntentReceiver, filter);
        filter = new IntentFilter();
        filter.addAction(HeartbeatReceiver.HEARTBEAT_FIXER_ACTION);
        this.mContext.registerReceiver(this.mHeartbeatReceiver, filter, "android.permission.CONNECTIVITY_INTERNAL", null);
    }

    private String[] getFeature(String str) {
        if (str == null) {
            throw new IllegalArgumentException("getFeature() received null string");
        }
        String[] result = new String[2];
        int subId = 0;
        if (WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
            subId = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
            if (str.equals("enableMMS_sub2")) {
                str = "enableMMS";
                subId = 1;
            } else if (str.equals("enableMMS_sub1")) {
                str = "enableMMS";
                subId = 0;
            }
        }
        result[0] = str;
        result[1] = String.valueOf(subId);
        Slog.d(TAG, "getFeature: return feature=" + str + " subId=" + subId);
        return result;
    }

    protected String getMmsFeature(String feature) {
        Slog.d(TAG, "getMmsFeature HwFeatureConfig.dual_card_mms_switch" + HwFeatureConfig.dual_card_mms_switch);
        if (!HwFeatureConfig.dual_card_mms_switch) {
            return feature;
        }
        String[] result = getFeature(feature);
        feature = result[0];
        this.phoneId = Integer.parseInt(result[1]);
        this.curMmsDataSub = -1;
        return feature;
    }

    protected boolean isAlwaysAllowMMSforRoaming(int networkType, String feature) {
        if (networkType == 0) {
            boolean isAlwaysAllowMMSforRoaming = isAlwaysAllowMMS;
            if (HwPhoneConstants.IS_CHINA_TELECOM) {
                boolean roaming = WrapperFactory.getMSimTelephonyManagerWrapper().isNetworkRoaming(this.phoneId);
                if (isAlwaysAllowMMSforRoaming) {
                    if (roaming) {
                    }
                }
            }
        }
        return true;
    }

    protected boolean isMmsAutoSetSubDiffFromDataSub(int networkType, String feature) {
        if (!HwFeatureConfig.dual_card_mms_switch) {
            return false;
        }
        this.curPrefDataSubscription = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        this.curMmsDataSub = WrapperFactory.getMSimTelephonyManagerWrapper().getMmsAutoSetDataSubscription();
        if (!feature.equals("enableMMS") || networkType != 0) {
            return false;
        }
        if ((this.curMmsDataSub != 0 && 1 != this.curMmsDataSub) || this.phoneId == this.curMmsDataSub) {
            return false;
        }
        log("DSMMS dds is switching now, do not response request from another card, curMmsDataSub: " + this.curMmsDataSub);
        return true;
    }

    protected boolean isMmsSubDiffFromDataSub(int networkType, String feature) {
        if (HwFeatureConfig.dual_card_mms_switch && feature.equals("enableMMS") && networkType == 0 && this.curPrefDataSubscription != this.phoneId) {
            return true;
        }
        return false;
    }

    protected boolean isNetRequestersPidsContainCurrentPid(List<Integer>[] mNetRequestersPids, int usedNetworkType, Integer currentPid) {
        if (!HwFeatureConfig.dual_card_mms_switch || !WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled() || mNetRequestersPids[usedNetworkType].contains(currentPid)) {
            return true;
        }
        Slog.w(TAG, "not tearing down special network - not found pid " + currentPid);
        return false;
    }

    protected boolean isNeedTearMmsAndRestoreData(int networkType, String feature, Handler mHandler) {
        if (!HwFeatureConfig.dual_card_mms_switch) {
            return true;
        }
        if (networkType != 0 || !feature.equals("enableMMS")) {
            return true;
        }
        if (!WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
            return true;
        }
        int curMmsDataSub = WrapperFactory.getMSimTelephonyManagerWrapper().getMmsAutoSetDataSubscription();
        if (curMmsDataSub != 0 && 1 != curMmsDataSub) {
            return true;
        }
        int lastPrefDataSubscription;
        if (curMmsDataSub == 0) {
            lastPrefDataSubscription = 1;
        } else {
            lastPrefDataSubscription = 0;
        }
        int curPrefDataSubscription = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        log("isNeedTearDataAndRestoreData lastPrefDataSubscription" + lastPrefDataSubscription + "curPrefDataSubscription" + curPrefDataSubscription);
        if (lastPrefDataSubscription != curPrefDataSubscription) {
            log("DSMMS >>>> disable a connection, after MMS net disconnected will switch back to phone " + lastPrefDataSubscription);
            WrapperFactory.getMSimTelephonyManagerWrapper().setPreferredDataSubscription(lastPrefDataSubscription);
        } else {
            log("DSMMS unexpected case, data subscription is already on " + curPrefDataSubscription);
        }
        WrapperFactory.getMSimTelephonyManagerWrapper().setMmsAutoSetDataSubscription(-1);
        return true;
    }

    private void cancelPowerSaving() {
        if (this.mTurnoffDCIntent != null) {
            ((AlarmManager) connectivityServiceUtils.getContext(this).getSystemService("alarm")).cancel(this.mTurnoffDCIntent);
            this.mTurnoffDCIntent = null;
        }
        processCtrlSocket(Process.myUid(), this.SET_SAVING, 0);
    }

    private void tryPowerSaving() {
        AlarmManager am = (AlarmManager) connectivityServiceUtils.getContext(this).getSystemService("alarm");
        if (this.mTurnoffDCIntent != null) {
            am.cancel(this.mTurnoffDCIntent);
            this.mTurnoffDCIntent = null;
        }
        Intent intent = new Intent(INTENT_TURNOFF_DC);
        intent.setPackage(connectivityServiceUtils.getContext(this).getPackageName());
        this.mFilterMsgFlag = -1;
        this.mTurnoffDCIntent = PendingIntent.getBroadcast(connectivityServiceUtils.getContext(this), 0, intent, 0);
        am.setExactAndAllowWhileIdle(2, SystemClock.elapsedRealtime() + HwNetworkStatsService.UPLOAD_INTERVAL, this.mTurnoffDCIntent);
        Log.d(TAG, "CtrlSocket tryPowerSaving timer duration = 1800000");
    }

    private void tryPowerSavingI(boolean keyguardlocked) {
        AlarmManager am = (AlarmManager) connectivityServiceUtils.getContext(this).getSystemService("alarm");
        if (this.mTurnoffDCIntent != null) {
            am.cancel(this.mTurnoffDCIntent);
            this.mTurnoffDCIntent = null;
        }
        if (this.mFirst || !keyguardlocked) {
            configAppUidList();
            this.mDeltaTime = HwNetworkStatsService.UPLOAD_INTERVAL;
            this.mFirst = false;
        } else {
            this.mDeltaTime -= SystemClock.elapsedRealtime() - this.mLastPowerOffTime;
            if (this.mDeltaTime < 0) {
                this.mDeltaTime = 0;
            }
        }
        if (this.mDeltaTime > 0) {
            this.mFilterMsgFlag = -1;
        }
        Intent intent = new Intent(INTENT_TURNOFF_DC);
        intent.setPackage(connectivityServiceUtils.getContext(this).getPackageName());
        this.mTurnoffDCIntent = PendingIntent.getBroadcast(connectivityServiceUtils.getContext(this), 0, intent, 0);
        am.setExactAndAllowWhileIdle(2, SystemClock.elapsedRealtime() + this.mDeltaTime, this.mTurnoffDCIntent);
        this.mLastPowerOffTime = SystemClock.elapsedRealtime();
        Log.d(TAG, "CtrlSocket tryPowerSavingI timer duration = " + this.mDeltaTime);
    }

    private void setIMResumeTimer() {
        if (this.mCtrlSocketInfo.mAllowCtrlSocketLevel != this.ALLOW_NO_CTRL_SOCKET_LEVEL) {
            AlarmManager am = (AlarmManager) connectivityServiceUtils.getContext(this).getSystemService("alarm");
            Intent intent = new Intent(INTENT_IM_RESUME_PROCESS);
            intent.setPackage(connectivityServiceUtils.getContext(this).getPackageName());
            this.mIMResumeIntent = PendingIntent.getBroadcast(connectivityServiceUtils.getContext(this), 0, intent, 0);
            int hr = getHourOfDay();
            long timeout = 2400000;
            if (hr >= 0 && hr <= 5) {
                timeout = (long) (hr == 5 ? IM_TIMER_MORN_INTERVAL_MILLIS : IM_TIMER_NIGHT_INTERVAL_MILLIS);
            } else if (hr == 23) {
                timeout = 7200000;
            }
            am.setExactAndAllowWhileIdle(2, SystemClock.elapsedRealtime() + timeout, this.mIMResumeIntent);
            Log.d(TAG, "CtrlSocket Resume IM  timer duration = " + timeout);
        }
    }

    private String getAllActPkgInWhiteList() {
        StringBuffer buf = new StringBuffer();
        for (String pkg : this.mCtrlSocketInfo.mScrOffActPkg) {
            String pkg2;
            buf.append(pkg2);
            buf.append("\t");
        }
        for (Entry entry : this.mCtrlSocketInfo.mSpecialPkgMap.entrySet()) {
            pkg2 = (String) entry.getKey();
            if (pkg2 != null) {
                String[] arraypkg = pkg2.split(":");
                if (arraypkg.length > 0) {
                    buf.append(arraypkg[0]);
                    buf.append("\t");
                }
            }
        }
        String activePkg = buf.toString();
        Log.d(TAG, "getAllActPkgInWhiteList " + activePkg);
        return activePkg;
    }

    private void startMobileFilter() {
        if (isAllowBastetFilter) {
            processCtrlSocket(Process.myUid(), this.SET_SAVING, 1);
            int curHour = getHourOfDay();
            Log.d(TAG, "[PS filter]turnoffDC intent curHour = " + curHour);
            if (curHour < 0 || curHour >= 5) {
                setNightClockTimer();
            } else {
                turnoffDC();
                setDayClockTimer();
            }
            Intent it = new Intent(MSG_SCROFF_CTRLSOCKET_STATS);
            it.putExtra("ctrl_socket_status", true);
            it.putExtra("ctrl_socket_list", getAllActPkgInWhiteList());
            this.mContext.sendBroadcastAsUser(it, UserHandle.ALL);
            Log.d(TAG, "CtrlSocket allow part broadcast");
            return;
        }
        Log.d(TAG, "not support");
    }

    private int generateRangeRandom(int sencond) {
        if (sencond <= 0) {
            Log.e(TAG, "Illegal Argument");
            return 0;
        }
        int rand = new Random().nextInt(sencond);
        if (rand >= sencond || rand < 0) {
            rand = 0;
        }
        return rand;
    }

    private void setDayClockTimer() {
        AlarmManager am = (AlarmManager) connectivityServiceUtils.getContext(this).getSystemService("alarm");
        Intent intent = new Intent(INTENT_DAY_CLOCK);
        intent.setPackage(connectivityServiceUtils.getContext(this).getPackageName());
        this.mDayClockIntent = PendingIntent.getBroadcast(connectivityServiceUtils.getContext(this), 0, intent, 0);
        long interval = getCurTimeToFixedTimeInMs(5, MINUTE_OF_MORNIG, 0) + ((long) (generateRangeRandom(RANDOM_TIME_SECOND) * 1000));
        Log.d(TAG, "[PS filter]Day clock timer duration = " + interval);
        if (interval < 0) {
            Log.d(TAG, "[PS filter]Day clock timer is invalid");
        } else {
            am.setExactAndAllowWhileIdle(2, SystemClock.elapsedRealtime() + interval, this.mDayClockIntent);
        }
    }

    private void cancelDayClockTimer() {
        if (this.mDayClockIntent != null) {
            AlarmManager am = (AlarmManager) connectivityServiceUtils.getContext(this).getSystemService("alarm");
            Log.d(TAG, "[PS filter]cancelDayClockTimer");
            am.cancel(this.mDayClockIntent);
        }
    }

    private void setNightClockTimer() {
        AlarmManager am = (AlarmManager) connectivityServiceUtils.getContext(this).getSystemService("alarm");
        Intent intent = new Intent(INTENT_NIGHT_CLOCK);
        intent.setPackage(connectivityServiceUtils.getContext(this).getPackageName());
        this.mNightClockIntent = PendingIntent.getBroadcast(connectivityServiceUtils.getContext(this), 0, intent, 0);
        long interval = getCurTimeToFixedTimeInMs(24, 0, 0);
        Log.d(TAG, "[PS filter]Night clock timer duration = " + interval);
        if (interval < 0) {
            Log.d(TAG, "[PS filter]Night clock timer is invalid");
        } else {
            am.setExactAndAllowWhileIdle(2, SystemClock.elapsedRealtime() + interval, this.mNightClockIntent);
        }
    }

    private void cancelNightClockTimer() {
        if (this.mNightClockIntent != null) {
            AlarmManager am = (AlarmManager) connectivityServiceUtils.getContext(this).getSystemService("alarm");
            Log.d(TAG, "[PS filter]cancelNightClockTimer");
            am.cancel(this.mNightClockIntent);
        }
    }

    private long getCurTimeToFixedTimeInMs(int hour, int minite, int second) {
        Calendar cal = Calendar.getInstance();
        long curTimeInMs = cal.getTimeInMillis();
        cal.set(14, 0);
        cal.set(13, second);
        cal.set(12, minite);
        cal.set(11, hour);
        return cal.getTimeInMillis() - curTimeInMs;
    }

    private void setIMPendingTimer() {
        if (this.mCtrlSocketInfo.mAllowCtrlSocketLevel != this.ALLOW_NO_CTRL_SOCKET_LEVEL) {
            AlarmManager am = (AlarmManager) connectivityServiceUtils.getContext(this).getSystemService("alarm");
            Intent intent = new Intent(INTENT_IM_PENDING_PROCESS);
            intent.setPackage(connectivityServiceUtils.getContext(this).getPackageName());
            this.mIMPendingIntent = PendingIntent.getBroadcast(connectivityServiceUtils.getContext(this), 0, intent, 0);
            am.setExactAndAllowWhileIdle(2, SystemClock.elapsedRealtime() + 30000, this.mIMPendingIntent);
            Log.d(TAG, "CtrlSocket Pending IM timer duration = 30000");
        }
    }

    private void turnoffDC() {
        try {
            if (getDataEnabled()) {
                setMobileDataEnabled(MODULE_POWERSAVING, false);
                this.mShowDlgTurnOfDC = false;
                setTurnOffDCState(1);
            }
        } catch (Exception e) {
            loge("have exception in turnoffDC function!");
        }
    }

    private void turnonDC() {
        try {
            if (getTurnOffDCState()) {
                setMobileDataEnabled(MODULE_POWERSAVING, true);
                setTurnOffDCState(0);
            }
        } catch (Exception e) {
            loge("have exception in turnonDC function!");
        }
    }

    private boolean getPowerSavingState() {
        if (System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), POWER_SAVING_ON, 0) == 1) {
            return true;
        }
        return false;
    }

    private void setTurnOffDCState(int val) {
        System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), TURN_OFF_DC_STATE, val);
    }

    private boolean getTurnOffDCState() {
        boolean retVal = System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), TURN_OFF_DC_STATE, 0) == 1;
        log("TurnOffDCState: " + retVal);
        return retVal;
    }

    private boolean isConnectedOrConnectingOrSuspended(NetworkInfo info) {
        boolean z = true;
        synchronized (this) {
            if (!(info.getState() == State.CONNECTED || info.getState() == State.CONNECTING)) {
                if (info.getState() != State.SUSPENDED) {
                    z = false;
                }
            }
        }
        return z;
    }

    private AlertDialog createWarningToPdp() {
        Builder buider;
        final String enable_Not_Remind_Function = Systemex.getString(connectivityServiceUtils.getContext(this).getContentResolver(), ENABLE_NOT_REMIND_FUNCTION);
        CheckBox checkBox = null;
        if (VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function)) {
            int themeID = connectivityServiceUtils.getContext(this).getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
            buider = new Builder(new ContextThemeWrapper(connectivityServiceUtils.getContext(this), themeID), themeID);
            View view = LayoutInflater.from(buider.getContext()).inflate(34013280, null);
            checkBox = (CheckBox) view.findViewById(34603226);
            buider.setView(view);
            buider.setTitle(17039380);
        } else {
            buider = new Builder(connectivityServiceUtils.getContext(this), connectivityServiceUtils.getContext(this).getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
            buider.setTitle(17039380);
            buider.setMessage(33685526);
        }
        final CheckBox finalBox = checkBox;
        buider.setIcon(17301543);
        buider.setPositiveButton(17040512, new OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(true);
                if (HwConnectivityService.VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function) && finalBox != null) {
                    HwConnectivityService.this.updateReminderSetting(finalBox.isChecked());
                }
                HwConnectivityService.this.mDataServiceToPdpDialog = null;
            }
        });
        buider.setNegativeButton(17040513, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).sendBroadcast(new Intent(HwConnectivityService.DISABEL_DATA_SERVICE_ACTION));
                HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(false);
                if (HwConnectivityService.VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function) && finalBox != null) {
                    HwConnectivityService.this.updateReminderSetting(finalBox.isChecked());
                }
                HwConnectivityService.this.mDataServiceToPdpDialog = null;
            }
        });
        buider.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                HwConnectivityService.connectivityServiceUtils.getContext(HwConnectivityService.this).sendBroadcast(new Intent(HwConnectivityService.DISABEL_DATA_SERVICE_ACTION));
                HwTelephonyManagerInner.getDefault().setDataEnabledWithoutPromp(false);
                if (HwConnectivityService.VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function) && finalBox != null) {
                    HwConnectivityService.this.updateReminderSetting(finalBox.isChecked());
                }
                HwConnectivityService.this.mDataServiceToPdpDialog = null;
            }
        });
        AlertDialog dialog = buider.create();
        dialog.getWindow().setType(2008);
        return dialog;
    }

    protected void registerPhoneStateListener(Context context) {
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 32);
    }

    private final void updateCallState(int state) {
        if (this.mRemindService || SystemProperties.getBoolean("gsm.huawei.RemindDataService", false)) {
            int phoneState = state;
            if (state == 0) {
                if (this.mShowDlgEndCall && this.mDataServiceToPdpDialog == null) {
                    this.mDataServiceToPdpDialog = createWarningToPdp();
                    this.mDataServiceToPdpDialog.show();
                    this.mShowDlgEndCall = false;
                }
            } else if (this.mDataServiceToPdpDialog != null) {
                this.mDataServiceToPdpDialog.dismiss();
                this.mDataServiceToPdpDialog = null;
                this.mShowDlgEndCall = true;
            }
        }
    }

    protected void registerBootStateListener(Context context) {
        new MobileEnabledSettingObserver(new Handler()).register();
    }

    protected boolean needSetUserDataEnabled(boolean enabled) {
        int dataStatus = Global.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), "mobile_data", 1);
        if (!shouldShowThePdpWarning() || dataStatus != 0 || !enabled) {
            return true;
        }
        if (this.mShowDlgTurnOfDC) {
            this.mHandler.sendEmptyMessage(0);
            return false;
        }
        this.mShowDlgTurnOfDC = true;
        return true;
    }

    private void updateReminderSetting(boolean chooseNotRemind) {
        if (chooseNotRemind) {
            System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), WHETHER_SHOW_PDP_WARNING, VALUE_NOT_SHOW_PDP);
        }
    }

    private boolean shouldShowThePdpWarning() {
        boolean z = false;
        if (WrapperFactory.getMSimTelephonyManagerWrapper().isMultiSimEnabled()) {
            return shouldShowThePdpWarningMsim();
        }
        String enable_Not_Remind_Function = Systemex.getString(connectivityServiceUtils.getContext(this).getContentResolver(), ENABLE_NOT_REMIND_FUNCTION);
        boolean remindDataAllow = SystemProperties.getBoolean("gsm.huawei.RemindDataService", false);
        int pdpWarningValue = System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), WHETHER_SHOW_PDP_WARNING, VALUE_SHOW_PDP);
        if (!VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enable_Not_Remind_Function)) {
            return remindDataAllow;
        }
        if (remindDataAllow && pdpWarningValue == VALUE_SHOW_PDP) {
            z = true;
        }
        return z;
    }

    private boolean checkDataServiceRemindMsim() {
        int lDataVal = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        if (lDataVal == 0) {
            if (TelephonyManager.getDefault().hasIccCard(lDataVal)) {
                return SystemProperties.getBoolean("gsm.huawei.RemindDataService", false);
            }
            return SystemProperties.getBoolean("gsm.huawei.RemindDataService_1", false);
        } else if (1 == lDataVal) {
            return SystemProperties.getBoolean("gsm.huawei.RemindDataService_1", false);
        } else {
            return false;
        }
    }

    private boolean shouldShowThePdpWarningMsim() {
        boolean z = true;
        String enableNotRemindFunction = Systemex.getString(this.mContext.getContentResolver(), ENABLE_NOT_REMIND_FUNCTION);
        boolean remindDataAllow = false;
        int lDataVal = WrapperFactory.getMSimTelephonyManagerWrapper().getPreferredDataSubscription();
        if (1 == lDataVal) {
            remindDataAllow = SystemProperties.getBoolean("gsm.huawei.RemindDataService_1", false);
        } else if (lDataVal == 0) {
            remindDataAllow = SystemProperties.getBoolean("gsm.huawei.RemindDataService", false);
        }
        int pdpWarningValue = System.getInt(this.mContext.getContentResolver(), WHETHER_SHOW_PDP_WARNING, VALUE_SHOW_PDP);
        if (!VALUE_ENABLE_NOT_REMIND_FUNCTION.equals(enableNotRemindFunction)) {
            return remindDataAllow;
        }
        if (!(remindDataAllow && pdpWarningValue == VALUE_SHOW_PDP)) {
            z = false;
        }
        return z;
    }

    private boolean shouldDisablePortalCheck(String ssid) {
        if (ssid != null) {
            log("wifi ssid: " + ssid);
            if (ssid.length() > 2 && ssid.charAt(0) == '\"' && ssid.charAt(ssid.length() - 1) == '\"') {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
        }
        if (1 == Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) && 1 == Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) && SystemProperties.getBoolean("ro.config.hw_disable_portal", false)) {
            log("stop portal check for orange");
            return true;
        } else if ("CMCC".equalsIgnoreCase(SystemProperties.get("ro.config.operators", AppHibernateCst.INVALID_PKG)) && "CMCC".equals(ssid)) {
            log("stop portal check for CMCC");
            return true;
        } else if (1 == System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), DISABLE_PORTAL_CHECK, 0)) {
            System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), DISABLE_PORTAL_CHECK, 0);
            log("stop portal check for airsharing");
            return true;
        } else if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 0 && "true".equals(Systemex.getString(this.mContext.getContentResolver(), "wifi.challenge.required"))) {
            log("setup guide wifi disable portal, and does not start browser!");
            return true;
        } else if (1 == System.getInt(connectivityServiceUtils.getContext(this).getContentResolver(), WIFI_AP_MANUAL_CONNECT, 0)) {
            System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), WIFI_AP_MANUAL_CONNECT, 0);
            log("portal ap manual connect");
            return false;
        } else if (WifiProCommonUtils.isWifiProEnable(this.mContext) && WifiProCommonUtils.isQueryActivityMatched(this.mContext, WifiProCommonUtils.HUAWEI_SETTINGS_WLAN)) {
            return false;
        } else {
            log("portal ap auto connect");
            return true;
        }
    }

    protected boolean startBrowserForWifiPortal(Notification notification, String ssid) {
        if (WifiProCommonUtils.isWifiProEnable(this.mContext) && WifiProCommonUtils.isPortalBackground()) {
            log("WLAN+ enabled, don't pop up portal notification status bar again!");
            WifiProCommonUtils.portalBackgroundStatusChanged(false);
            return true;
        } else if (shouldDisablePortalCheck(ssid)) {
            log("do not start browser, popup system notification");
            return false;
        } else {
            log("setNotificationVisible: cancel notification and start browser directly for TYPE_WIFI..");
            try {
                Intent intent;
                if (IS_CHINA) {
                    String operator = TelephonyManager.getDefault().getNetworkOperator();
                    if (operator == null || operator.length() == 0 || !operator.startsWith("460")) {
                        this.mURL = new URL("http://" + this.mServer + "/generate_204");
                        intent = new Intent("android.intent.action.VIEW", Uri.parse(this.mURL.toString()));
                        intent.setFlags(272629760);
                        notification.contentIntent = PendingIntent.getActivity(connectivityServiceUtils.getContext(this), 0, intent, 0);
                        try {
                            intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
                            connectivityServiceUtils.getContext(this).startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            try {
                                log("default browser not exist..");
                                if (isSetupWizardCompleted()) {
                                    notification.contentIntent.send();
                                } else {
                                    log("setup wizard is not completed");
                                    Network network = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetwork();
                                    Intent intentPortal = new Intent("android.net.conn.CAPTIVE_PORTAL");
                                    intentPortal.putExtra("android.net.extra.NETWORK", network);
                                    intentPortal.putExtra("android.net.extra.CAPTIVE_PORTAL", new CaptivePortal(new ICaptivePortal.Stub() {
                                        public void appResponse(int response) {
                                        }
                                    }));
                                    intentPortal.putExtra("android.net.extra.CAPTIVE_PORTAL_URL", this.mURL.toString());
                                    intentPortal.setFlags(272629760);
                                    intentPortal.putExtra(FLAG_SETUP_WIZARD, true);
                                    connectivityServiceUtils.getContext(this).startActivity(intentPortal);
                                }
                            } catch (CanceledException e2) {
                                log("Sending contentIntent failed: " + e2);
                            } catch (ActivityNotFoundException e3) {
                                log("Activity not found: " + e3);
                            }
                        }
                        return true;
                    }
                    this.mURL = new URL(HwNetworkPropertyChecker.CHINA_MAINLAND_MAIN_SERVER);
                    intent = new Intent("android.intent.action.VIEW", Uri.parse(this.mURL.toString()));
                    intent.setFlags(272629760);
                    notification.contentIntent = PendingIntent.getActivity(connectivityServiceUtils.getContext(this), 0, intent, 0);
                    intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
                    connectivityServiceUtils.getContext(this).startActivity(intent);
                    return true;
                }
                this.mURL = new URL("http://" + this.mServer + "/generate_204");
                intent = new Intent("android.intent.action.VIEW", Uri.parse(this.mURL.toString()));
                intent.setFlags(272629760);
                notification.contentIntent = PendingIntent.getActivity(connectivityServiceUtils.getContext(this), 0, intent, 0);
                intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
                connectivityServiceUtils.getContext(this).startActivity(intent);
                return true;
            } catch (MalformedURLException e4) {
                log("MalformedURLException " + e4);
            }
        }
    }

    public boolean isSystemBootComplete() {
        return this.sendWifiBroadcastAfterBootCompleted;
    }

    protected void hintUserSwitchToMobileWhileWifiDisconnected(State state, int type) {
        if (WifiProCommonUtils.isWifiSelfCuring() && state == State.DISCONNECTED && type == 1) {
            Log.d("HwSelfCureEngine", "DISCONNECTED, but enableDefaultTypeAPN-->UP is ignored due to wifi self curing.");
        } else {
            this.wifiDisconnectManager.hintUserSwitchToMobileWhileWifiDisconnected(state, type);
        }
    }

    protected void enableDefaultTypeApnWhenWifiConnectionStateChanged(State state, int type) {
        if (!(state == State.DISCONNECTED && type == 1) && state == State.CONNECTED && type == 1) {
            if (WifiProCommonUtils.isWifiSelfCuring()) {
                Log.d("HwSelfCureEngine", "CONNECTED, but enableDefaultTypeAPN-->DOWN is ignored due to wifi self curing.");
                return;
            }
            enableDefaultTypeAPN(false);
        }
    }

    private void sendBlueToothTetheringBroadcast(boolean isBttConnected) {
        log("sendBroad bt_tethering_connect_state = " + isBttConnected);
        Intent intent = new Intent("android.intent.action.BlueToothTethering_NETWORK_CONNECTION_CHANGED");
        intent.putExtra("btt_connect_state", isBttConnected);
        connectivityServiceUtils.getContext(this).sendBroadcast(intent);
    }

    protected void enableDefaultTypeApnWhenBlueToothTetheringStateChanged(NetworkAgentInfo networkAgent, NetworkInfo newInfo) {
        if (newInfo.getType() == 7) {
            log("enter BlueToothTethering State Changed");
            State state = newInfo.getState();
            if (state == State.CONNECTED) {
                sendBlueToothTetheringBroadcast(true);
                enableDefaultTypeAPN(false);
            } else if (state == State.DISCONNECTED) {
                sendBlueToothTetheringBroadcast(false);
                enableDefaultTypeAPN(true);
            }
        }
    }

    public void makeDefaultAndHintUser(NetworkAgentInfo newNetwork) {
        this.wifiDisconnectManager.makeDefaultAndHintUser(newNetwork);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 1001:
                String register_pkg = data.readString();
                Log.d(TAG, "CtrlSocket registerPushSocket pkg = " + register_pkg);
                registerPushSocket(register_pkg);
                return true;
            case 1002:
                String unregister_pkg = data.readString();
                Log.d(TAG, "CtrlSocket unregisterPushSocket pkg = " + unregister_pkg);
                unregisterPushSocket(unregister_pkg);
                return true;
            case 1003:
                int pid = data.readInt();
                int cmd = data.readInt();
                int para = data.readInt();
                Log.d(TAG, "CtrlSocket processCtrlSocket pid = " + pid + " cmd = " + cmd + " para = " + para);
                processCtrlSocket(pid, cmd, para);
                return true;
            case 1004:
                reply.writeString(getActPkgInWhiteList());
                return true;
            case 1005:
                reply.writeInt(this.mCtrlSocketInfo.mAllowCtrlSocketLevel);
                return true;
            case 1006:
                Log.d(TAG, "CtrlSocket getCtrlSocketVersion = v2");
                reply.writeString(ctrl_socket_version);
                return true;
            case 1007:
                String register_spkg = data.readString();
                int call_pid = Binder.getCallingPid();
                Log.d(TAG, "CtrlSocket registerSpecialPid name = " + register_spkg + " pid = " + call_pid);
                registerSpecialSocket(call_pid, register_spkg);
                break;
            case 1008:
                String unregiter_spkg = data.readString();
                Log.d(TAG, "CtrlSocket unregistSpecialPid name = " + unregiter_spkg);
                unregisterSpecialSocket(unregiter_spkg);
                break;
            case 1009:
                String reg_pkg = data.readString();
                int register_pid = data.readInt();
                Log.d(TAG, "CtrlSocket registSpecialPid name = " + reg_pkg + " reg_pid = " + register_pid);
                registerSpecialSocket(register_pid, reg_pkg);
                break;
            case 1101:
                data.enforceInterface(descriptor);
                int enableInt = data.readInt();
                Log.d(TAG, "needSetUserDataEnabled enableInt = " + enableInt);
                boolean result = needSetUserDataEnabled(enableInt == 1);
                Log.d(TAG, "needSetUserDataEnabled result = " + result);
                reply.writeNoException();
                reply.writeInt(result ? 1 : 0);
                return true;
        }
        return super.onTransact(code, data, reply, flags);
    }

    private void registerPushSocket(String pkgName) {
        if (pkgName != null) {
            boolean isToAdd = false;
            for (String pkg : this.mCtrlSocketInfo.mRegisteredPkg) {
                if (pkg.equals(pkgName)) {
                    return;
                }
            }
            if (this.mCtrlSocketInfo.mRegisteredCount >= this.MAX_REGISTERED_PKG_NUM) {
                for (String pkg2 : this.mCtrlSocketInfo.mPushWhiteListPkg) {
                    if (pkg2.equals(pkgName)) {
                        isToAdd = true;
                    }
                }
            } else {
                isToAdd = true;
            }
            if (isToAdd) {
                CtrlSocketInfo ctrlSocketInfo = this.mCtrlSocketInfo;
                ctrlSocketInfo.mRegisteredCount++;
                this.mCtrlSocketInfo.mRegisteredPkg.add(pkgName);
                updateRegisteredPkg();
            }
        }
    }

    private void unregisterPushSocket(String pkgName) {
        if (pkgName != null) {
            int count = 0;
            boolean isMatch = false;
            for (String pkg : this.mCtrlSocketInfo.mRegisteredPkg) {
                if (pkg.equals(pkgName)) {
                    isMatch = true;
                    break;
                }
                count++;
            }
            if (isMatch) {
                CtrlSocketInfo ctrlSocketInfo = this.mCtrlSocketInfo;
                ctrlSocketInfo.mRegisteredCount--;
                this.mCtrlSocketInfo.mRegisteredPkg.remove(count);
                updateRegisteredPkg();
            }
        }
    }

    private void registerSpecialSocket(int pid, String name) {
        if (name != null) {
            boolean isInWhiteList = false;
            for (String pkg : this.mCtrlSocketInfo.mSpecialWhiteListPkg) {
                if (pkg.equals(name)) {
                    isInWhiteList = true;
                }
            }
            if (isInWhiteList) {
                if (this.mCtrlSocketInfo.mSpecialPkgMap.containsKey(name)) {
                    int recordPid = ((Integer) this.mCtrlSocketInfo.mSpecialPkgMap.get(name)).intValue();
                    if (!(pid == recordPid || pid == recordPid)) {
                        this.mCtrlSocketInfo.mSpecialPkgMap.put(name, Integer.valueOf(pid));
                        if (isAllowBastetFilter) {
                            procCtrlSockets(pid, this.CANCEL_SPECIAL_PID, 1);
                            procCtrlSockets(pid, this.SET_SPECIAL_PID, 1);
                        }
                    }
                } else {
                    Log.d(TAG, "CtrlSocket add to SpecialMap pid = " + pid);
                    this.mCtrlSocketInfo.mSpecialPkgMap.put(name, Integer.valueOf(pid));
                    if (isAllowBastetFilter && mBastetFilterEnable) {
                        procCtrlSockets(pid, this.SET_SPECIAL_PID, 0);
                    }
                }
            }
        }
    }

    private void unregisterSpecialSocket(String name) {
        if (name != null && this.mCtrlSocketInfo.mSpecialPkgMap.containsKey(name)) {
            int pid = ((Integer) this.mCtrlSocketInfo.mSpecialPkgMap.get(name)).intValue();
            if (isAllowBastetFilter) {
                procCtrlSockets(pid, this.CANCEL_SPECIAL_PID, 1);
            }
            this.mCtrlSocketInfo.mSpecialPkgMap.remove(name);
        }
    }

    private int processCtrlSocket(int pid, int cmd, int param) {
        int ret = -1;
        if (isAllowBastetFilter) {
            ret = procCtrlSockets(pid, cmd, param);
        }
        if (ret >= 0 && this.SET_SAVING == cmd) {
            System.putInt(connectivityServiceUtils.getContext(this).getContentResolver(), "CtrlSocketSaving", param);
        }
        Log.d(TAG, "CtrlSocket processCtrlSocket pid = " + pid + " cmd = " + cmd + " param = " + param + " ret = " + ret);
        return ret;
    }

    private String getActPkgInWhiteList() {
        if (this.ALLOW_PART_CTRL_SOCKET_LEVEL != this.mCtrlSocketInfo.mAllowCtrlSocketLevel) {
            return null;
        }
        StringBuffer activePkg = new StringBuffer();
        for (String pkg : this.mCtrlSocketInfo.mScrOffActPkg) {
            activePkg.append(pkg);
            activePkg.append("\t");
        }
        return activePkg.toString();
    }

    private void calcUseCtrlSocketLevel() {
        this.mCtrlSocketInfo.mAllowCtrlSocketLevel = this.ALLOW_NO_CTRL_SOCKET_LEVEL;
        this.mCtrlSocketInfo.mScrOffActiveCount = 0;
        this.mCtrlSocketInfo.mScrOffActPkg.clear();
        int pwrSaveMode = readSysPwrSaveMode();
        if (this.SUPER_POWER_SAVING_MODE != pwrSaveMode && this.POWER_SAVING_MODE != pwrSaveMode) {
            CtrlSocketInfo ctrlSocketInfo;
            if (this.NORMAL_POWER_SAVING_MODE == pwrSaveMode) {
                this.mCtrlSocketInfo.mAllowCtrlSocketLevel = this.ALLOW_ALL_CTRL_SOCKET_LEVEL;
                for (String pkg : this.mCtrlSocketInfo.mRegisteredPkg) {
                    this.mCtrlSocketInfo.mScrOffActPkg.add(pkg);
                    ctrlSocketInfo = this.mCtrlSocketInfo;
                    ctrlSocketInfo.mScrOffActiveCount++;
                }
                return;
            }
            if (this.mCtrlSocketInfo.mRegisteredCount != 0) {
                for (String pkg2 : this.mCtrlSocketInfo.mRegisteredPkg) {
                    for (String wlPkg : this.mCtrlSocketInfo.mPushWhiteListPkg) {
                        if (pkg2.equals(wlPkg)) {
                            ctrlSocketInfo = this.mCtrlSocketInfo;
                            ctrlSocketInfo.mScrOffActiveCount++;
                            this.mCtrlSocketInfo.mScrOffActPkg.add(pkg2);
                        }
                    }
                }
                Log.d(TAG, "CtrlSocket calcUseCtrlSocketLevel Active.Count = " + this.mCtrlSocketInfo.mScrOffActiveCount);
                if (this.mCtrlSocketInfo.mScrOffActiveCount > 0) {
                    this.mCtrlSocketInfo.mAllowCtrlSocketLevel = this.ALLOW_PART_CTRL_SOCKET_LEVEL;
                    return;
                }
            }
            if (this.mCtrlSocketInfo.mSpecialPkgMap.size() != 0) {
                Log.d(TAG, "CtrlSocket calcUseCtrlSocketLevel Special.size = " + this.mCtrlSocketInfo.mSpecialPkgMap.size());
                this.mCtrlSocketInfo.mAllowCtrlSocketLevel = this.ALLOW_SPECIAL_CTRL_SOCKET_LEVEL;
            }
        }
    }

    private int getUseCtrlSocketLevel() {
        return this.mCtrlSocketInfo.mAllowCtrlSocketLevel;
    }

    private void resetUseCtrlSocketLevel() {
        this.mCtrlSocketInfo.mAllowCtrlSocketLevel = this.ALLOW_NO_CTRL_SOCKET_LEVEL;
    }

    private boolean isWifiAvailable() {
        ConnectivityManager connMgr = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (connMgr == null) {
            return false;
        }
        NetworkInfo ni = connMgr.getNetworkInfo(1);
        return ni != null && ni.isConnected();
    }

    private void getCtrlSocketPushWhiteList() {
        String wlPkg = Secure.getString(this.mContext.getContentResolver(), "push_white_apps");
        if (wlPkg != null) {
            String[] str = wlPkg.split(";");
            if (str != null && str.length > 0) {
                this.mCtrlSocketInfo.mPushWhiteListPkg.clear();
                for (int i = 0; i < str.length; i++) {
                    this.mCtrlSocketInfo.mPushWhiteListPkg.add(str[i]);
                    Log.d(TAG, "CtrlSocket PushWhiteList[" + i + "] = " + str[i]);
                }
            }
        }
    }

    private void getCtrlSocketSpecialWhiteList() {
        String wlPkg = Secure.getString(this.mContext.getContentResolver(), "net_huawei_apps");
        if (wlPkg == null) {
            wlPkg = IM_SPECIAL_PROC;
        }
        String[] str = wlPkg.split(";");
        if (str != null && str.length > 0) {
            this.mCtrlSocketInfo.mSpecialWhiteListPkg.clear();
            for (int i = 0; i < str.length; i++) {
                this.mCtrlSocketInfo.mSpecialWhiteListPkg.add(str[i]);
                Log.d(TAG, "CtrlSocket SpecialWhiteList[" + i + "] = " + str[i]);
            }
        }
    }

    private int readSysPwrSaveMode() {
        if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            return this.SUPER_POWER_SAVING_MODE;
        }
        return System.getInt(this.mContext.getContentResolver(), "SmartModeStatus", -1);
    }

    private void restoreScrOnStatus() {
        cancelNightClockTimer();
        cancelDayClockTimer();
        if (this.mIMWakeLock.isHeld()) {
            this.mIMWakeLock.release();
        }
        synchronized (this.mPowerSavingLock) {
            Log.d(TAG, "CtrlSocket restoreScrOnStatus");
            this.mShouldPowerSave = false;
            if (this.ALLOW_PART_CTRL_SOCKET_LEVEL == this.mCtrlSocketInfo.mAllowCtrlSocketLevel || this.ALLOW_SPECIAL_CTRL_SOCKET_LEVEL == this.mCtrlSocketInfo.mAllowCtrlSocketLevel) {
                this.mContext.sendBroadcastAsUser(new Intent(MSG_ALL_CTRLSOCKET_ALLOWED), UserHandle.ALL);
                Log.d(TAG, "CtrlSocket restoreScrOnStatus all allowed");
            }
            resetUseCtrlSocketLevel();
            cancelPowerSaving();
            Log.d(TAG, "CtrlSocket restoreScrOnStatus reset");
            if (this.mStartPowerSaving) {
                if (this.wifiDisconnectManager.mWifiToPdpDialog == null) {
                    log("CtrlSocket restoreScrOnStatus turnonDC");
                    turnonDC();
                }
                this.mStartPowerSaving = false;
            }
        }
    }

    private void getCtrlSocketRegisteredPkg() {
        String registeredPkg = Secure.getString(this.mContext.getContentResolver(), "registered_pkgs");
        if (registeredPkg != null) {
            String[] str = registeredPkg.split(";");
            if (str != null && str.length > 0) {
                this.mCtrlSocketInfo.mRegisteredPkg.clear();
                this.mCtrlSocketInfo.mRegisteredCount = 0;
                for (Object add : str) {
                    this.mCtrlSocketInfo.mRegisteredPkg.add(add);
                    CtrlSocketInfo ctrlSocketInfo = this.mCtrlSocketInfo;
                    ctrlSocketInfo.mRegisteredCount++;
                }
            }
        }
    }

    private void updateRegisteredPkg() {
        StringBuffer registeredPkg = new StringBuffer();
        for (String pkg : this.mCtrlSocketInfo.mRegisteredPkg) {
            registeredPkg.append(pkg);
            registeredPkg.append(";");
        }
        Secure.putString(this.mContext.getContentResolver(), "registered_pkgs", registeredPkg.toString());
    }

    private void initCtrlSocket(Context context) {
        if (SystemProperties.getBoolean("ro.config.hw_power_saving", false)) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.addAction(INTENT_TURNOFF_DC);
            filter.addAction(ACTION_OF_ANDROID_BOOT_COMPLETED);
            filter.addAction("android.net.wifi.STATE_CHANGE");
            filter.addAction("android.intent.action.USER_PRESENT");
            filter.addAction(INTENT_NIGHT_CLOCK);
            filter.addAction(INTENT_DAY_CLOCK);
            this.mContext.registerReceiver(this.mIntentReceiver, filter);
            getCtrlSocketRegisteredPkg();
            getCtrlSocketPushWhiteList();
            getCtrlSocketSpecialWhiteList();
            this.mDbObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean selfChange) {
                    HwConnectivityService.this.getCtrlSocketPushWhiteList();
                }
            };
            context.getContentResolver().registerContentObserver(this.WHITELIST_URI, false, this.mDbObserver);
        }
    }

    private void setMobileDataEnabled(String module, boolean enabled) {
        Log.d(TAG, "module:" + module + " setMobileDataEnabled enabled = " + enabled);
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            tm.setDataEnabled(enabled);
            tm.setDataEnabledProperties(module, enabled);
        }
    }

    private boolean getDataEnabled() {
        boolean ret = false;
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            ret = tm.getDataEnabled();
        }
        Log.d(TAG, "CtrlSocket getMobileDataEnabled enabled = " + ret);
        return ret;
    }

    public boolean getMobileDataEnabled() {
        boolean ret = false;
        if (!this.mIsSimStateChanged) {
            return false;
        }
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm != null) {
            boolean ret2 = false;
            int slotId = 0;
            while (slotId < tm.getPhoneCount()) {
                try {
                    if (tm.getSimState(slotId) == 5) {
                        ret2 = true;
                    }
                    slotId++;
                } catch (NullPointerException e) {
                    Log.d(TAG, "getMobileDataEnabled NPE");
                }
            }
            if (ret2) {
                ret = tm.getDataEnabled();
            } else {
                Log.d(TAG, "all sim card not ready,return false");
                return false;
            }
        }
        Log.d(TAG, "CtrlSocket getMobileDataEnabled = " + ret);
        return ret;
    }

    private void enableDefaultTypeAPN(boolean enabled) {
        Log.d(TAG, "enableDefaultTypeAPN= " + enabled);
        Log.d(TAG, "DEFAULT_MOBILE_ENABLE before state is " + SystemProperties.get("sys.defaultapn.enabled", "true"));
        SystemProperties.set("sys.defaultapn.enabled", enabled ? "true" : "false");
        HwTelephonyManagerInner hwTm = HwTelephonyManagerInner.getDefault();
        if (hwTm != null) {
            hwTm.setDefaultMobileEnable(enabled);
        }
    }

    private void registerSimStateReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        context.registerReceiver(this.mSimStateReceiver, filter);
    }

    private void handleShowEnablePdpDialog() {
        if (this.mDataServiceToPdpDialog == null) {
            this.mDataServiceToPdpDialog = createWarningToPdp();
            this.mDataServiceToPdpDialog.show();
        }
    }

    private void handleIMReceivingMsgAction() {
        Log.d(TAG, "handleIMReceivingMsgAction");
        Log.d(TAG, "CtrlSocketDo set Power Saving Mode 0");
        processCtrlSocket(Process.myUid(), this.SET_SAVING, 0);
        if (this.mCtrlSocketInfo.mAllowCtrlSocketLevel != this.ALLOW_NO_CTRL_SOCKET_LEVEL) {
            turnonDC();
            this.mIMArrayList.clear();
            this.mIMArrayList.add(MM_PKG_NAME);
            Intent it = new Intent(PG_PENDING_ACTION);
            it.putExtra("enable", false);
            it.putExtra("applist", this.mIMArrayList);
            it.putExtra(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE, 1);
            this.mContext.sendBroadcast(it, "com.huawei.powergenie.receiverPermission");
            Log.d(TAG, "tell powergenie to resume applist = " + this.mIMArrayList);
            setIMPendingTimer();
        }
    }

    private void registerTetheringReceiver(Context context) {
        if (HwDeliverInfo.isIOTVersion() && SystemProperties.getBoolean("ro.config.persist_usb_tethering", false)) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(HwGnssCommParam.ACTION_USB_STATE);
            context.registerReceiver(this.mTetheringReceiver, filter);
        }
    }

    protected void setExplicitlyUnselected(NetworkAgentInfo nai) {
        if (nai != null) {
            nai.networkMisc.explicitlySelected = false;
            nai.networkMisc.acceptUnvalidated = false;
            if (nai.networkInfo != null && ConnectivityManager.getNetworkTypeName(1).equals(nai.networkInfo.getTypeName())) {
                log("setExplicitlyUnselected, WiFi+ switch from WiFi to Cellular, enableDefaultTypeAPN explicitly.");
                enableDefaultTypeAPN(true);
            }
        }
    }

    protected void updateNetworkConcurrently(NetworkAgentInfo networkAgent, NetworkInfo newInfo) {
        State state = newInfo.getState();
        INetworkManagementService netd = connectivityServiceUtils.getNetd(this);
        synchronized (networkAgent) {
            NetworkInfo oldInfo = networkAgent.networkInfo;
            networkAgent.networkInfo = newInfo;
        }
        if (oldInfo != null && oldInfo.getState() == state) {
            log("updateNetworkConcurrently, ignoring duplicate network state non-change");
        } else if (netd == null) {
            loge("updateNetworkConcurrently, invalid member, netd = null");
        } else {
            networkAgent.setCurrentScore(0);
            try {
                String str;
                int i = networkAgent.network.netId;
                if (networkAgent.networkCapabilities.hasCapability(13)) {
                    str = null;
                } else {
                    str = "SYSTEM";
                }
                netd.createPhysicalNetwork(i, str);
                networkAgent.created = true;
                connectivityServiceUtils.updateLinkProperties(this, networkAgent, null);
                log("updateNetworkConcurrently, nai.networkInfo = " + networkAgent.networkInfo);
                networkAgent.asyncChannel.sendMessage(528391, 4, 0, null);
            } catch (Exception e) {
                loge("updateNetworkConcurrently, Error creating network " + networkAgent.network.netId + ": " + e.getMessage());
            }
        }
    }

    public void triggerRoamingNetworkMonitor(NetworkAgentInfo networkAgent) {
        if (networkAgent != null && networkAgent.networkMonitor != null) {
            log("triggerRoamingNetworkMonitor, nai.networkInfo = " + networkAgent.networkInfo);
            networkAgent.networkMonitor.sendMessage(532581);
        }
    }

    protected boolean reportPortalNetwork(NetworkAgentInfo nai, int result) {
        if (result != 2) {
            return false;
        }
        nai.asyncChannel.sendMessage(528391, 3, 0, null);
        return true;
    }

    protected boolean ignoreRemovedByWifiPro(NetworkAgentInfo nai) {
        if (nai.networkInfo.getType() == 1 && WifiProCommonUtils.isWifiProEnable(this.mContext)) {
            return true;
        }
        return false;
    }

    protected void holdWifiNetworkMessenger(NetworkAgentInfo nai) {
        if (nai.networkInfo.getType() == 1) {
            this.mWifiNetworkMessenger = nai.messenger;
        }
    }

    private NetworkAgentInfo getWifiNai() {
        NetworkAgentInfo networkAgentInfo = null;
        HashMap<Messenger, NetworkAgentInfo> networkAgentInfos = connectivityServiceUtils.getNetworkAgentInfos(this);
        if (this.mWifiNetworkMessenger == null) {
            log("csagent getWifiNai WIFI NetworkAgent not register Error, return null.");
            return null;
        }
        if (networkAgentInfos != null && networkAgentInfos.containsKey(this.mWifiNetworkMessenger)) {
            networkAgentInfo = (NetworkAgentInfo) networkAgentInfos.get(this.mWifiNetworkMessenger);
        }
        return networkAgentInfo;
    }

    public Network getNetworkForTypeWifi() {
        enforceAccessPermission();
        NetworkAgentInfo nai = getWifiNai();
        if (nai == null) {
            return null;
        }
        return nai.network;
    }

    public NetworkInfo getNetworkInfoForWifi() {
        enforceAccessPermission();
        NetworkAgentInfo nai = getWifiNai();
        if (nai != null) {
            NetworkInfo result = new NetworkInfo(nai.networkInfo);
            result.setType(1);
            return result;
        }
        result = new NetworkInfo(1, 0, ConnectivityManager.getNetworkTypeName(1), AppHibernateCst.INVALID_PKG);
        result.setDetailedState(DetailedState.DISCONNECTED, null, null);
        return result;
    }

    protected void setVpnSettingValue(boolean enable) {
        log("WiFi_PRO, setVpnSettingValue =" + enable);
        System.putInt(this.mContext.getContentResolver(), "wifipro_network_vpn_state", enable ? 1 : 0);
    }

    private boolean isRequestedByPkgName(int pID, String pkgName) {
        List<RunningAppProcessInfo> appProcessList = this.mActivityManager.getRunningAppProcesses();
        if (appProcessList == null || pkgName == null) {
            return false;
        }
        for (RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess != null && appProcess.pid == pID) {
                String[] pkgNameList = appProcess.pkgList;
                for (Object equals : pkgNameList) {
                    if (pkgName.equals(equals)) {
                        return true;
                    }
                }
                continue;
            }
        }
        return false;
    }

    public NetworkInfo getActiveNetworkInfo() {
        NetworkInfo networkInfo = super.getActiveNetworkInfo();
        if (networkInfo != null || !isRequestedByPkgName(Binder.getCallingPid(), SYSTEM_MANAGER_PKG_NAME)) {
            return networkInfo;
        }
        Slog.d(TAG, "return the background wifi network info for system manager.");
        return HwServiceFactory.getHwConnectivityManager().getNetworkInfoForWifi();
    }

    protected boolean isNetworkRequestBip(NetworkRequest nr) {
        if (nr == null) {
            loge("network request is null!");
            return false;
        } else if (nr.networkCapabilities.hasCapability(18) || nr.networkCapabilities.hasCapability(19) || nr.networkCapabilities.hasCapability(20) || nr.networkCapabilities.hasCapability(21) || nr.networkCapabilities.hasCapability(22) || nr.networkCapabilities.hasCapability(23) || nr.networkCapabilities.hasCapability(24)) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean checkNetworkSupportBip(NetworkAgentInfo nai, NetworkRequest nri) {
        if (HwModemCapability.isCapabilitySupport(1)) {
            log("MODEM is support BIP!");
            return false;
        } else if (nai == null || nri == null || nai.networkInfo == null) {
            loge("network agent or request is null, just return false!");
            return false;
        } else if (nai.networkInfo.getType() != 0 || !nai.isInternet()) {
            loge("NOT support internet or NOT mobile!");
            return false;
        } else if (isNetworkRequestBip(nri)) {
            String defaultApn = SystemProperties.get("gsm.default.apn");
            String bipApn = SystemProperties.get("gsm.bip.apn");
            if (defaultApn == null || bipApn == null) {
                loge("default apn is null or bip apn is null, default: " + defaultApn + ", bip: " + bipApn);
                return false;
            } else if (MemoryConstant.MEM_SCENE_DEFAULT.equals(bipApn)) {
                log("bip use default network, return true");
                return true;
            } else {
                String[] buffers = bipApn.split(",");
                if (buffers.length <= 1 || !defaultApn.equalsIgnoreCase(buffers[1].trim())) {
                    log("network do NOT support bip, default: " + defaultApn + ", bip: " + bipApn);
                    return false;
                }
                log("default apn support bip, default: " + defaultApn + ", bip: " + buffers[1].trim());
                return true;
            }
        } else {
            loge("network request is NOT bip!");
            return false;
        }
    }

    public void setLteMobileDataEnabled(boolean enable) {
        log("[enter]setLteMobileDataEnabled " + enable);
        enforceChangePermission();
        HwTelephonyManagerInner.getDefault().setLteServiceAbility(enable ? 1 : 0);
        sendLteDataStateBroadcast(mLteMobileDataState);
    }

    public int checkLteConnectState() {
        enforceAccessPermission();
        return mLteMobileDataState;
    }

    protected void handleLteMobileDataStateChange(NetworkInfo info) {
        if (info == null) {
            Slog.e(TAG, "NetworkInfo got null!");
            return;
        }
        Slog.d(TAG, "[enter]handleLteMobileDataStateChange type=" + info.getType() + ",subType=" + info.getSubtype());
        if (info.getType() == 0) {
            int lteState;
            if (13 == info.getSubtype()) {
                lteState = mapDataStateToLteDataState(info.getState());
            } else {
                lteState = 3;
            }
            setLteMobileDataState(lteState);
        }
    }

    private int mapDataStateToLteDataState(State state) {
        log("[enter]mapDataStateToLteDataState state=" + state);
        switch (-getandroid-net-NetworkInfo$StateSwitchesValues()[state.ordinal()]) {
            case 1:
                return 1;
            case 2:
                return 0;
            case 3:
                return 3;
            case 4:
                return 2;
            default:
                Slog.d(TAG, "mapDataStateToLteDataState ignore state = " + state);
                return 3;
        }
    }

    private synchronized void setLteMobileDataState(int state) {
        Slog.d(TAG, "[enter]setLteMobileDataState state=" + state);
        mLteMobileDataState = state;
        sendLteDataStateBroadcast(mLteMobileDataState);
    }

    private void sendLteDataStateBroadcast(int state) {
        Intent intent = new Intent("android.net.wifi.LTEDATA_COMPLETED_ACTION");
        intent.putExtra("lte_mobile_data_status", state);
        Slog.d(TAG, "Send sticky broadcast from ConnectivityService. intent=" + intent);
        sendStickyBroadcast(intent);
    }

    public long getLteTotalRxBytes() {
        Slog.d(TAG, "[enter]getLteTotalRxBytes");
        enforceAccessPermission();
        long lteRxBytes = 0;
        try {
            NetworkStatsHistory.Entry entry = getLteStatsEntry(2);
            if (entry != null) {
                lteRxBytes = entry.rxBytes;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "lteTotalRxBytes=" + lteRxBytes);
        return lteRxBytes;
    }

    public long getLteTotalTxBytes() {
        Slog.d(TAG, "[enter]getLteTotalTxBytes");
        enforceAccessPermission();
        long lteTxBytes = 0;
        try {
            NetworkStatsHistory.Entry entry = getLteStatsEntry(8);
            if (entry != null) {
                lteTxBytes = entry.txBytes;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "LteTotalTxBytes=" + lteTxBytes);
        return lteTxBytes;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private NetworkStatsHistory.Entry getLteStatsEntry(int fields) {
        Log.d(TAG, "[enter]getLteStatsEntry fields=" + fields);
        NetworkStatsHistory.Entry entry = null;
        try {
            NetworkTemplate mobile4gTemplate = NetworkTemplate.buildTemplateMobile4g(((TelephonyManager) this.mContext.getSystemService("phone")).getSubscriberId());
            getStatsService().forceUpdate();
            INetworkStatsSession session = getStatsService().openSession();
            if (session != null) {
                NetworkStatsHistory networkStatsHistory = session.getHistoryForNetwork(mobile4gTemplate, fields);
                if (networkStatsHistory != null) {
                    entry = networkStatsHistory.getValues(Long.MIN_VALUE, Long.MAX_VALUE, null);
                }
            }
            TrafficStats.closeQuietly(session);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable th) {
            TrafficStats.closeQuietly(null);
        }
        return entry;
    }

    private static synchronized INetworkStatsService getStatsService() {
        INetworkStatsService iNetworkStatsService;
        synchronized (HwConnectivityService.class) {
            Log.d(TAG, "[enter]getStatsService");
            if (mStatsService == null) {
                mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats"));
            }
            iNetworkStatsService = mStatsService;
        }
        return iNetworkStatsService;
    }

    private int[] getHwUidsWithPolicy(int policy) {
        int[] uids = new int[0];
        IBinder networkPolicyManager = ServiceManager.getService("netpolicy");
        if (networkPolicyManager == null) {
            Log.e(TAG, "getService NETWORK_POLICY_SERVICE failed!");
            return uids;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if (data == null) {
            return uids;
        }
        data.writeInt(policy);
        try {
            networkPolicyManager.transact(204, data, reply, 0);
            reply.readInt();
            int num = reply.readInt();
            Log.d(TAG, "getHwUidsWithPolicy uid num: " + num);
            if (num <= 0) {
                data.recycle();
                if (reply != null) {
                    reply.recycle();
                }
                return uids;
            }
            for (int i = 0; i < num; i++) {
                uids = ArrayUtils.appendInt(uids, reply.readInt());
            }
            data.recycle();
            if (reply != null) {
                reply.recycle();
            }
            return uids;
        } catch (Exception e) {
            e.printStackTrace();
            data.recycle();
            if (reply != null) {
                reply.recycle();
            }
            return uids;
        } catch (Throwable th) {
            data.recycle();
            if (reply != null) {
                reply.recycle();
            }
            return uids;
        }
    }

    private void setNetworkRestrictByUid(int uid, boolean isRestrict, boolean isMobileNetwork) {
        IBinder networkManager = ServiceManager.getService("network_management");
        if (networkManager == null) {
            Log.e(TAG, "getService NETWORKMANAGEMENT_SERVICE failed!");
            return;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        String cmd = "bandwidth";
        String[] args = new String[5];
        args[0] = "firewall";
        args[1] = isRestrict ? "block" : "allow";
        args[2] = isMobileNetwork ? "mobile" : "wifi";
        args[3] = String.valueOf(uid);
        args[4] = String.valueOf(0);
        try {
            data.writeString(cmd);
            data.writeArray(args);
            networkManager.transact(201, data, reply, 1);
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
        } catch (Throwable th) {
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
        }
    }

    private void startFilter() {
        Log.d(TAG, "begin startFilter m_filterIsStarted:" + this.m_filterIsStarted);
        synchronized (this.mFilterUidlistLock) {
            if (this.m_filterIsStarted) {
                return;
            }
            int[] restrictUids = getHwUidsWithPolicy(1);
            for (int valueOf : restrictUids) {
                this.m_filterUidSet.remove(Integer.valueOf(valueOf));
            }
            String[] str = getAllActPkgInWhiteList().split("\t");
            for (String appUidByName : str) {
                int uid = getAppUidByName(appUidByName);
                Log.d(TAG, "remove white list uid: " + uid);
                this.m_filterUidSet.remove(Integer.valueOf(uid));
            }
            this.m_filterUidSet.remove(Integer.valueOf(Process.myUid()));
            for (Integer intValue : this.m_filterUidSet) {
                setNetworkRestrictByUid(intValue.intValue(), true, true);
            }
            this.m_filterIsStarted = true;
        }
    }

    private void stopFilter() {
        Log.d(TAG, "stopFilter m_filterIsStarted:" + this.m_filterIsStarted);
        synchronized (this.mFilterUidlistLock) {
            if (this.m_filterIsStarted) {
                for (Integer intValue : this.m_filterUidSet) {
                    setNetworkRestrictByUid(intValue.intValue(), false, true);
                }
                this.m_filterIsStarted = false;
                return;
            }
        }
    }

    private int getAppUidByName(String name) {
        try {
            return this.mContext.getPackageManager().getApplicationInfo(name, 1).uid;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "getApplicationInfo failed");
            return -1;
        }
    }

    private int getHourOfDay() {
        return Calendar.getInstance().get(11);
    }

    private void checkBastetFilter() {
        if ("true".equals(SystemProperties.get("bastet.service.enable", "false")) && setBastetFilterInfo(1, -1) == -24) {
            Log.d(TAG, "Bastet filter feature is supported");
            mBastetFilterEnable = true;
        }
    }

    private void getBastetService() {
        synchronized (this.mBastetFilterLock) {
            if (this.mBastetService == null) {
                this.mBastetService = ServiceManager.getService(BASTET_SERVICE);
                if (this.mBastetService == null) {
                    Log.e(TAG, "Failed to get bastet service!");
                    return;
                }
                try {
                    this.mBastetService.linkToDeath(this.mBastetDeathRecipient, 0);
                    this.mIBastetManager = IBastetManager.Stub.asInterface(this.mBastetService);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int setBastetFilterInfo(int action, int pid) {
        int ret = -6;
        if (action == 1 || mBastetFilterEnable) {
            try {
                getBastetService();
                synchronized (this.mBastetFilterLock) {
                    if (this.mIBastetManager != null) {
                        ret = this.mIBastetManager.setFilterInfo(action, pid);
                    }
                }
            } catch (RemoteException e) {
                Log.e(TAG, "set bastet filter information failed");
                e.printStackTrace();
            }
            return ret;
        }
        Log.d(TAG, "[tiger]setBastetFilterInfo action " + action + "pid " + pid);
        if (2 == action) {
            startFilter();
        } else if (3 == action) {
            stopFilter();
        } else {
            Log.d(TAG, "setBastetFilterInfo other action:" + action);
        }
        return -6;
    }

    private int procCtrlSockets(int pid, int cmd, int param) {
        int action = 0;
        Log.d(TAG, "pid=" + pid + ", cmd=" + cmd + ", param=" + param);
        if (cmd == this.CANCEL_SPECIAL_PID) {
            action = 5;
        } else if (cmd == this.KEEP_SOCKET || cmd == this.SET_SPECIAL_PID) {
            action = 4;
            Log.d(TAG, "pid = " + pid + "socket = " + param);
            this.mFilterKeepPid = pid;
            this.mFilterSpecialSocket = param;
        } else if (cmd == this.SET_SAVING) {
            if (param == 0) {
                action = 3;
            } else {
                action = 2;
            }
        } else if (cmd == this.PUSH_AVAILABLE) {
            this.mFilterSpecialSocket = param;
        } else if (cmd == this.GET_KEEP_SOCKET_STATS) {
            int ret;
            Log.d(TAG, "procCtrlSockets cmd is GET_KEEP_SOCKET_STATS");
            if (this.mFilterKeepPid == 0 || this.mFilterSpecialSocket == 0) {
                ret = 0;
            } else {
                ret = 1;
            }
            return ret;
        } else {
            Log.e(TAG, "unknown cmd: " + cmd);
            return 0;
        }
        sendFilterMsg(action, pid);
        return 0;
    }

    private void handleFilterMsg(int action, int pid) {
        Log.d(TAG, "handleFilterMsg action: " + action + "mFilterMsgFlag: " + this.mFilterMsgFlag);
        synchronized (this.mFilterDelayLock) {
            if (this.mFilterMsgFlag != action) {
                this.mFilterMsgFlag = 0;
                return;
            }
            this.mFilterMsgFlag = 0;
            setBastetFilterInfo(action, pid);
        }
    }

    private void initFilterThread() {
        this.mThread = new HandlerThread("FilterThread");
        this.mThread.start();
        this.mFilterHandler = new HwFilterHandler(this.mThread.getLooper());
    }

    private void sendFilterMsg(int arg1, int arg2) {
        Message msg = this.mFilterHandler.obtainMessage();
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        Log.d(TAG, "mFilterMsgFlag: " + this.mFilterMsgFlag + ",msg.arg1: " + msg.arg1);
        synchronized (this.mFilterDelayLock) {
            if (msg.arg1 == 2) {
                msg.what = 0;
                if (this.mFilterMsgFlag == 0) {
                    this.mFilterHandler.sendMessageDelayed(msg, 15000);
                } else if (-1 == this.mFilterMsgFlag) {
                    this.mFilterHandler.sendMessage(msg);
                }
                this.mFilterMsgFlag = msg.arg1;
            } else if (msg.arg1 == 3) {
                msg.what = 0;
                if (this.mFilterMsgFlag == 0 || -1 == this.mFilterMsgFlag) {
                    this.mFilterHandler.sendMessage(msg);
                }
                this.mFilterMsgFlag = msg.arg1;
            } else {
                msg.what = 1;
                this.mFilterHandler.sendMessage(msg);
            }
        }
    }

    private void handleBastetServiceDied() {
        if (setBastetFilterInfo(3, -1) == 0) {
            Log.d(TAG, "Stop bastet filter success");
            this.mBastetDiedRetry = 0;
            return;
        }
        this.mBastetDiedRetry++;
        if (this.mBastetDiedRetry < 5) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 2;
            this.mHandler.sendMessageDelayed(msg, 500);
            return;
        }
        Log.e(TAG, "ERROR!!! CANNOT STOP BASTET FILTER");
        this.mBastetDiedRetry = 0;
    }

    private int[] getUidList() {
        for (PackageInfo pkgInfo : this.mContext.getPackageManager().getInstalledPackages(12288)) {
            String[] permissions = pkgInfo.requestedPermissions;
            if (permissions != null) {
                for (String permission : permissions) {
                    if (permission.equals("android.permission.INTERNET")) {
                        this.uidSet.add(Integer.valueOf(pkgInfo.applicationInfo.uid));
                        synchronized (this.mFilterUidlistLock) {
                            this.m_filterUidSet.add(Integer.valueOf(pkgInfo.applicationInfo.uid));
                        }
                    }
                }
                continue;
            }
        }
        Integer[] temp = (Integer[]) this.uidSet.toArray(new Integer[0]);
        int[] uids = new int[temp.length];
        Log.d(TAG, "configAppUidList uid num: " + temp.length);
        for (int i = 0; i < temp.length; i++) {
            uids[i] = temp[i].intValue();
        }
        return uids;
    }

    private int configAppUidList() {
        int ret = -6;
        int[] uids = getUidList();
        if (!mBastetFilterEnable) {
            return -6;
        }
        try {
            getBastetService();
            synchronized (this.mBastetFilterLock) {
                if (this.mIBastetManager != null) {
                    ret = this.mIBastetManager.configAppUidList(uids);
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "config AppUidList information failed");
            e.printStackTrace();
        }
        return ret;
    }

    protected void registerMapconIntentReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MAPCON_START_INTENT);
        filter.addAction(CONNECTIVITY_CHANGE_ACTION);
        filter.addAction(ACTION_MAPCON_SERVICE_START);
        filter.addAction(ACTION_MAPCON_SERVICE_FAILED);
        context.registerReceiver(this.mMapconIntentReceiver, filter);
    }

    protected boolean ifNeedToStartLteMmsTimer(NetworkRequest request) {
        if (this.mMapconService == null) {
            return false;
        }
        int wifiMmsSwitchOn = -1;
        try {
            wifiMmsSwitchOn = this.mMapconService.getVoWifiServiceState(0, 1);
        } catch (RemoteException e) {
            loge("getVoWifiServiceState,err=" + e.toString());
        }
        loge("handleRegisterNetworkRequest,wifiMmsSwitchOn=" + wifiMmsSwitchOn);
        int domain = -1;
        try {
            domain = this.mMapconService.getVoWifiServiceDomain(0, 0);
        } catch (RemoteException e2) {
            loge("getVoWifiServiceDomain,err=" + e2.toString());
        }
        loge(" before LteMmsTimer start wifiMmsSwitchOn=" + wifiMmsSwitchOn);
        if (!request.networkCapabilities.hasCapability(0) || 1 != wifiMmsSwitchOn || 2 != domain || !this.isWaitWifiMms) {
            return false;
        }
        if (this.isWifiMmsAlready) {
            loge("WifiMmsAlready,dont need to LteMmstimer");
            this.isWifiMmsAlready = false;
        } else {
            this.mLteMmsNetworkRequest = request;
            this.mLteMmsTimer.stop();
            this.mLteMmsTimer.start();
        }
        return true;
    }

    protected NetworkCapabilities changeWifiMmsNetworkCapabilities(NetworkCapabilities networkCapabilities) {
        if (this.mMapconService == null) {
            return networkCapabilities;
        }
        Boolean isWifiMmsUtOn = Boolean.valueOf(SystemProperties.getBoolean("ro.config.hw_vowifi_mmsut", false));
        try {
            int wifiMmsSwitchOn = this.mMapconService.getVoWifiServiceState(0, 1);
            log("changeWifiMmsNetworkCapabilities,wifiMmsSwitchOn=" + wifiMmsSwitchOn);
            if (!networkCapabilities.hasCapability(0) || !isWifiMmsUtOn.booleanValue() || 1 != wifiMmsSwitchOn) {
                return networkCapabilities;
            }
            try {
                int domain = this.mMapconService.getVoWifiServiceDomain(0, 0);
                log("changeNetworkCapabilities,domain=" + domain);
                if (2 == domain) {
                    this.isWaitWifiMms = true;
                    this.isWifiMmsAlready = false;
                    try {
                        this.mMapconService.setupTunnelOverWifi(0, 0, null, null);
                    } catch (RemoteException e) {
                        loge("changeWifiMmsNetworkCapabilities,setupTunnelOverWifi,err=" + e.toString());
                        return networkCapabilities;
                    }
                } else if (3 == domain || 1 == domain) {
                    this.mWifiMmsTimer.stop();
                    this.mWifiMmsTimer.start();
                } else if (domain == 0) {
                    String networkSpecifier = networkCapabilities.getNetworkSpecifier();
                    networkCapabilities.setNetworkSpecifier(null);
                    networkCapabilities.addTransportType(1);
                    networkCapabilities.removeTransportType(0);
                    networkCapabilities.setNetworkSpecifier(networkSpecifier);
                }
                return networkCapabilities;
            } catch (RemoteException e2) {
                loge("changeWifiMmsNetworkCapabilities,getVoWifiServiceDomain,err=" + e2.toString());
                return networkCapabilities;
            }
        } catch (RemoteException e22) {
            loge("changeWifiMmsNetworkCapabilities,getVoWifiServiceState,err=" + e22.toString());
            return networkCapabilities;
        }
    }

    protected void wifiMmsRelease(NetworkRequest networkRequest) {
        if (networkRequest.networkCapabilities.hasCapability(0) && this.mMapconService != null && Boolean.valueOf(SystemProperties.getBoolean("ro.config.hw_vowifi_mmsut", false)).booleanValue()) {
            try {
                int wifiMmsSwitchOn = this.mMapconService.getVoWifiServiceState(0, 1);
                log("wifiMmsRelease,wifiMmsSwitchOn=" + wifiMmsSwitchOn);
                if (wifiMmsSwitchOn == 1) {
                    try {
                        int domain = this.mMapconService.getVoWifiServiceDomain(0, 0);
                        log("wifiMmsRelease,domain=" + domain);
                        if (!(2 == domain || 3 == domain)) {
                            if (1 == domain) {
                            }
                        }
                        try {
                            this.mMapconService.teardownTunnelOverWifi(0, 0, null, null);
                        } catch (RemoteException e) {
                            loge("wifiMmsRelease,setupTunnelOverWifi,err=" + e.toString());
                        }
                    } catch (RemoteException e2) {
                        loge("wifiMmsRelease,getVoWifiServiceDomain,err=" + e2.toString());
                    }
                }
            } catch (RemoteException e22) {
                loge("wifiMmsRelease,getVoWifiServiceState,err=" + e22.toString());
            }
        }
    }

    private boolean isSetupWizardCompleted() {
        if (1 == Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0)) {
            return 1 == Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", 0);
        } else {
            return false;
        }
    }
}
