package com.huawei.netassistant.wifiap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.INetworkManagementEventObserver;
import android.net.NetworkStats;
import android.net.NetworkStats.Entry;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.INetworkManagementService.Stub;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.telephony.MSimTelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.MathUtils;
import com.android.server.net.BaseNetworkObserver;
import com.huawei.netassistant.calculator.CalculateTrafficManager;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.permissionmanager.db.DBHelper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;

public class WifiApWatchingService extends Service {
    private static final String ACTION_NETWORK_STATS_UPDATED = "com.android.server.action.NETWORK_STATS_UPDATED";
    public static final String ACTION_START_WATCHING_WIFIAP = "com.huawei.netassistant.wifiap_startwatching";
    private static final String LIMIT_GLOBAL_ALERT = "globalAlert";
    private static final long MAX_LIMIT = 8796093022207L;
    private static final int MSG_AP_DISABLED = 4;
    private static final int MSG_FORCE_UPDATE = 1;
    private static final int MSG_GLOBAL_ALERT = 2;
    private static final int MSG_LIMIT_CHANGE = 3;
    private static final int MSG_START_WATCHING = 5;
    private static final int MSG_STATS_UPDATE = 0;
    private static final String MSIM_TELEPHONY_SERVICE = "phone_msim";
    private static final String NETSTATS_GLOBAL_ALERT_BYTES = "netstats_global_alert_bytes";
    private static final long NETSTATS_THRESHOD_DFT = 1048576;
    private static final long NETSTATS_THRESHOD_MAX = 2097152;
    private static final long NETSTATS_THRESHOD_MIN = 131072;
    private static final int NO_LIMIT = -1;
    private static long STATS_INTERVAL_DFT = 5000;
    private static long STATS_INTERVAL_LOG = 3000;
    private static long STATS_INTERVAL_MAX = DBHelper.HISTORY_MAX_SIZE;
    private static long STATS_INTERVAL_MIN = 100;
    private static final String TAG = "WifiApWatchingService";
    private static final String WIFIAP_LIMIT = "wifiap_one_usage_limit";
    private static final String WIFIAP_STATS = "wifiap_one_usage_stats";
    private INetworkManagementEventObserver mAlertObserver = new BaseNetworkObserver() {
        public void limitReached(String limitName, String iface) {
            WifiApWatchingService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", WifiApWatchingService.TAG);
            HwLog.i(WifiApWatchingService.TAG, "limitReached: limitName = " + limitName + ", iface = " + iface);
            if (WifiApWatchingService.LIMIT_GLOBAL_ALERT.equals(limitName)) {
                WifiApWatchingService.this.mServiceHandler.sendEmptyMessage(2);
            }
        }
    };
    private Context mContext;
    private volatile boolean mDataActivityChangeFlag = true;
    private boolean mIsApDisabling = false;
    private boolean mIsApShutDownOnLimit = false;
    private volatile boolean mIsDataConnAvailable = true;
    private boolean mIsMultiSimEnabled = false;
    protected boolean mIsScreenOn = true;
    private volatile boolean mIsWatchingStarted = false;
    private volatile int mLastDataActivity = 3;
    private long mLastStatsLogMills = 0;
    private long mLastStatsLogSize = 0;
    private long mLastStatsMills = 0;
    private long mLastStatsSize = 0;
    private long mLimitSize = -1;
    private ContentObserver mLimitSizeObserver = null;
    private MSimTelephonyManager mMSimTelManager = null;
    private long mMaxSpeed = 0;
    private long mNetSpeed = 0;
    private int mNetworkClass = 0;
    private INetworkManagementService mNetworkManager = null;
    private PhoneStateListener mPhoneCallStateListener = null;
    private long mRemainSize = -1;
    private ServiceHandler mServiceHandler = null;
    private Looper mServiceLooper = null;
    private long mStartMills = 0;
    private long mStartSize = 0;
    private TelephonyManager mTelManager = null;
    private WatchingReceiver mWatchingReceiver = null;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    HwLog.v(WifiApWatchingService.TAG, "handleMessage: MSG_STATS_UPDATE");
                    WifiApWatchingService.this.mServiceHandler.removeMessages(0);
                    WifiApWatchingService.this.onSysStatsUpdate();
                    return;
                case 1:
                    WifiApWatchingService.this.onForceUpdateStats();
                    return;
                case 2:
                    HwLog.v(WifiApWatchingService.TAG, "handleMessage: MSG_GLOBAL_ALERT");
                    WifiApWatchingService.this.onGlobalAlert();
                    return;
                case 3:
                    WifiApWatchingService.this.onWifiApLimitChange();
                    return;
                case 4:
                    WifiApWatchingService.this.onApDisabled();
                    return;
                case 5:
                    WifiApWatchingService.this.onStartWatching();
                    return;
                default:
                    return;
            }
        }
    }

    private class WatchingReceiver extends HsmBroadcastReceiver {
        private WatchingReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                HwLog.w(WifiApWatchingService.TAG, "onReceive: Invalid params");
                return;
            }
            String action = intent.getAction();
            if ("com.android.server.action.NETWORK_STATS_UPDATED".equals(action)) {
                WifiApWatchingService.this.mServiceHandler.sendEmptyMessageDelayed(0, WifiApWatchingService.STATS_INTERVAL_MIN);
            } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                WifiApWatchingService.this.handleWifiAPStateChange(context, intent.getIntExtra("wifi_state", 11));
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                WifiApWatchingService.this.mIsScreenOn = true;
                HwLog.d(WifiApWatchingService.TAG, "onReceive: Screen is on");
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                WifiApWatchingService.this.mIsScreenOn = false;
                HwLog.d(WifiApWatchingService.TAG, "onReceive: Screen is off");
            }
        }
    }

    public void onCreate() {
        super.onCreate();
        this.mContext = getApplicationContext();
        this.mNetworkManager = Stub.asInterface(ServiceManager.getService("network_management"));
        if (this.mNetworkManager == null) {
            HwLog.e(TAG, "onCreate: Fail to get network manager service");
        } else if (startServiceLoop()) {
            initTelephonyMgr();
            initStatsConfigs();
            registerBroadcastRecievers();
            registerCellularDataListener();
            registerWifiApLimitObserver();
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            HwLog.w(TAG, "onStartCommand: Invalid intent");
            return 2;
        } else if (this.mNetworkManager == null || this.mServiceHandler == null) {
            return 2;
        } else {
            String action = intent.getAction();
            if (this.mIsWatchingStarted || !ACTION_START_WATCHING_WIFIAP.equals(action)) {
                return 2;
            }
            WifiApHelper.cancelReachLimitNotification(this.mContext);
            this.mServiceHandler.sendEmptyMessage(5);
            this.mServiceHandler.sendEmptyMessageDelayed(1, STATS_INTERVAL_DFT);
            return 3;
        }
    }

    private void initTelephonyMgr() {
        this.mIsMultiSimEnabled = SimCardManager.getInstance().isPhoneSupportDualCard();
        HwLog.i(TAG, "onCreate: mIsMultiSimEnabled = " + this.mIsMultiSimEnabled);
        try {
            if (this.mIsMultiSimEnabled) {
                this.mMSimTelManager = (MSimTelephonyManager) getSystemService(MSIM_TELEPHONY_SERVICE);
                if (this.mMSimTelManager == null) {
                    HwLog.w(TAG, "onCreate: Fail to get MSIM_TELEPHONY_SERVICE");
                }
            }
            if (this.mMSimTelManager != null) {
                int nSubId = this.mMSimTelManager.getPreferredDataSubscription();
                HwLog.i(TAG, "onCreate: nSubId = " + nSubId + ", nNetworkType = " + MSimTelephonyManager.getNetworkType(nSubId));
                return;
            }
            this.mTelManager = (TelephonyManager) getSystemService("phone");
            HwLog.i(TAG, "onCreate: nNetworkType = " + this.mTelManager.getNetworkType());
        } catch (Exception e) {
            HwLog.e(TAG, "onCreate: Exception", e);
        }
    }

    private void initNetworkInfo() {
        boolean z = true;
        int nDataState = 0;
        if (this.mIsMultiSimEnabled && this.mMSimTelManager != null) {
            nDataState = this.mMSimTelManager.getDataState();
            if (2 != nDataState) {
                z = false;
            }
            this.mIsDataConnAvailable = z;
            this.mNetworkClass = WifiApHelper.getNetworkClass(MSimTelephonyManager.getNetworkType(this.mMSimTelManager.getPreferredDataSubscription()));
            this.mMaxSpeed = WifiApHelper.getMaxNetworkSpeed(this.mNetworkClass);
        } else if (this.mTelManager != null) {
            nDataState = this.mTelManager.getDataState();
            if (2 != nDataState) {
                z = false;
            }
            this.mIsDataConnAvailable = z;
            this.mNetworkClass = WifiApHelper.getNetworkClass(this.mTelManager.getNetworkType());
            this.mMaxSpeed = WifiApHelper.getMaxNetworkSpeed(this.mNetworkClass);
        }
        HwLog.i(TAG, "initNetworkInfo: nDataState = " + nDataState + ", mNetworkClass = " + this.mNetworkClass);
    }

    private void initStatsConfigs() {
        STATS_INTERVAL_MIN = WifiApPrefHelper.getMinStatsInterval(this.mContext);
        STATS_INTERVAL_MAX = WifiApPrefHelper.getMaxStatsInterval(this.mContext);
        STATS_INTERVAL_DFT = WifiApPrefHelper.getDftStatsInterval(this.mContext);
        HwLog.i(TAG, "initStatsConfigs: Min = " + STATS_INTERVAL_MIN + ", max = " + STATS_INTERVAL_MAX + ", dft = " + STATS_INTERVAL_DFT);
    }

    private void initStatsInfo() {
        Secure.putLong(getContentResolver(), WIFIAP_STATS, 0);
        initNetworkInfo();
        this.mStartMills = SystemClock.elapsedRealtime();
        this.mStartSize = getTetherStats();
        this.mLastStatsMills = SystemClock.elapsedRealtime();
        this.mLastStatsSize = 0;
        this.mLimitSize = getWifiApLimit();
        this.mRemainSize = this.mLimitSize;
        this.mNetSpeed = 0;
        this.mLastStatsLogMills = this.mLastStatsMills;
        this.mLastStatsLogSize = 0;
        HwLog.i(TAG, "initStatsInfo: Start time = " + this.mStartMills + ",startSize = " + (this.mStartSize / 1024) + ", LimitSize = " + (this.mLimitSize < 0 ? -1 : this.mLimitSize / 1024));
    }

    private void adjustAlertSetting() {
        CalculateTrafficManager.getInstance().getStatsSession().advisePersistThreshold(NETSTATS_THRESHOD_DFT);
    }

    private void restoreAlertSetting() {
        long globalAlertBytes = Global.getLong(getContentResolver(), NETSTATS_GLOBAL_ALERT_BYTES, 0);
        if (globalAlertBytes <= 0) {
            globalAlertBytes = NETSTATS_THRESHOD_MAX;
        }
        CalculateTrafficManager.getInstance().getStatsSession().advisePersistThreshold(globalAlertBytes);
    }

    private boolean startServiceLoop() {
        HandlerThread thread = new HandlerThread("WifiAPWatchingThread", 10);
        thread.start();
        this.mServiceLooper = thread.getLooper();
        if (this.mServiceLooper == null) {
            try {
                Thread.sleep(10);
                this.mServiceLooper = thread.getLooper();
            } catch (Exception e) {
                HwLog.e(TAG, "startServiceLoop: Exception", e);
                return false;
            }
        }
        if (this.mServiceLooper == null) {
            return false;
        }
        this.mServiceHandler = new ServiceHandler(this.mServiceLooper);
        return true;
    }

    private long getWifiApLimit() {
        long wifiApLimit = (long) Secure.getInt(getContentResolver(), WIFIAP_LIMIT, -1);
        if (wifiApLimit < 0) {
            return -1;
        }
        if (wifiApLimit - MAX_LIMIT > 0) {
            wifiApLimit = MAX_LIMIT;
            HwLog.w(TAG, "getWifiApLimit: Too large limit , correct to MAX_LIMIT");
        }
        return NETSTATS_THRESHOD_DFT * wifiApLimit;
    }

    private void registerCellularDataListener() {
        if (this.mPhoneCallStateListener != null || (this.mTelManager == null && this.mMSimTelManager == null)) {
            HwLog.w(TAG, "registerCellularDataListener: Already registered or invalid TelManager");
            return;
        }
        this.mPhoneCallStateListener = new PhoneStateListener() {
            public void onDataActivity(int direction) {
                HwLog.d(WifiApWatchingService.TAG, "onDataActivity: direction = " + direction);
                WifiApWatchingService.this.mDataActivityChangeFlag = true;
                WifiApWatchingService.this.mLastDataActivity = direction;
                if (!WifiApWatchingService.this.mIsApDisabling && !WifiApWatchingService.this.mServiceHandler.hasMessages(1)) {
                    HwLog.i(WifiApWatchingService.TAG, "onDataActivity: Trigger stats update");
                    WifiApWatchingService.this.mServiceHandler.sendEmptyMessage(1);
                }
            }

            public void onDataConnectionStateChanged(int state, int networkType) {
                switch (state) {
                    case 0:
                    case 3:
                        WifiApWatchingService.this.mIsDataConnAvailable = false;
                        HwLog.d(WifiApWatchingService.TAG, "onDataConnectionStateChanged: Disconnected. state = " + state);
                        return;
                    case 2:
                        WifiApWatchingService.this.mIsDataConnAvailable = true;
                        WifiApWatchingService.this.mNetworkClass = WifiApHelper.getNetworkClass(networkType);
                        WifiApWatchingService.this.mMaxSpeed = WifiApHelper.getMaxNetworkSpeed(WifiApWatchingService.this.mNetworkClass);
                        HwLog.d(WifiApWatchingService.TAG, "onDataConnectionStateChanged: Connected, networkType = " + networkType);
                        return;
                    default:
                        HwLog.d(WifiApWatchingService.TAG, "onDataConnectionStateChanged: State = " + state);
                        return;
                }
            }
        };
        if (this.mIsMultiSimEnabled && this.mMSimTelManager != null) {
            this.mMSimTelManager.listen(this.mPhoneCallStateListener, 192);
        } else if (this.mTelManager != null) {
            this.mTelManager.listen(this.mPhoneCallStateListener, 192);
        }
    }

    private void unregisterCellularDataListener() {
        if (this.mPhoneCallStateListener != null && (this.mTelManager != null || this.mMSimTelManager != null)) {
            if (this.mIsMultiSimEnabled && this.mMSimTelManager != null) {
                this.mMSimTelManager.listen(this.mPhoneCallStateListener, 0);
            } else if (this.mTelManager != null) {
                this.mTelManager.listen(this.mPhoneCallStateListener, 0);
            }
            this.mPhoneCallStateListener = null;
        }
    }

    private void registerBroadcastRecievers() {
        if (this.mWatchingReceiver == null) {
            this.mWatchingReceiver = new WatchingReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.android.server.action.NETWORK_STATS_UPDATED");
            filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.SCREEN_OFF");
            registerReceiver(this.mWatchingReceiver, filter);
        }
    }

    private void unregisterBroadcastReceivers() {
        if (this.mWatchingReceiver != null) {
            unregisterReceiver(this.mWatchingReceiver);
            this.mWatchingReceiver = null;
        }
    }

    private void registerGlobalAlertObserver() {
        try {
            this.mNetworkManager.registerObserver(this.mAlertObserver);
            HwLog.i(TAG, "registerAlertObserver: register Observer = " + this.mAlertObserver);
        } catch (RemoteException e) {
            HwLog.e(TAG, "registerAlertObserver: Fail to register alert Observer");
        }
    }

    private void unregisterGlobalAlertObserver() {
        try {
            this.mNetworkManager.unregisterObserver(this.mAlertObserver);
            HwLog.i(TAG, "unregisterGlobalAlertObserver: unregister ObServer = " + this.mAlertObserver);
        } catch (RemoteException e) {
            HwLog.e(TAG, "unregisterAlertObserver: Fail to unregister alert Observer");
        }
    }

    private void registerWifiApLimitObserver() {
        if (this.mLimitSizeObserver == null) {
            this.mLimitSizeObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean selfChange) {
                    WifiApWatchingService.this.mServiceHandler.sendEmptyMessage(3);
                }
            };
            getContentResolver().registerContentObserver(Secure.getUriFor(WIFIAP_LIMIT), true, this.mLimitSizeObserver);
        }
    }

    private void unregisterWifiApLimitObserver() {
        if (this.mLimitSizeObserver != null) {
            getContentResolver().unregisterContentObserver(this.mLimitSizeObserver);
            this.mLimitSizeObserver = null;
        }
    }

    private void registerGlobalAlert(boolean isFirstRegist) {
        long alertThreshod;
        if (isFirstRegist) {
            try {
                if (!this.mNetworkManager.isBandwidthControlEnabled()) {
                    HwLog.w(TAG, "registerGlobalAlert: Bandwidth control is not enabled");
                    return;
                }
            } catch (IllegalStateException e) {
                HwLog.e(TAG, "registerGlobalAlert: IllegalStateException " + e);
            } catch (RemoteException e2) {
                HwLog.e(TAG, "registerGlobalAlert: RemoteException " + e2);
            }
        }
        long globalAlertBytes = Global.getLong(getContentResolver(), NETSTATS_GLOBAL_ALERT_BYTES, 0);
        if (this.mLimitSize < 0) {
            alertThreshod = NETSTATS_THRESHOD_MAX;
        } else if (this.mRemainSize > 131072) {
            alertThreshod = this.mRemainSize;
        } else {
            alertThreshod = 131072;
        }
        this.mNetworkManager.setGlobalAlert(alertThreshod);
        if (isFirstRegist) {
            HwLog.i(TAG, "registerGlobalAlert: System pre-config global alert = " + (globalAlertBytes / 1024) + ", Set to " + (alertThreshod / 1024));
        }
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onDestroy() {
        HwLog.i(TAG, "onDestroy: End time = " + SystemClock.elapsedRealtime());
        unregisterBroadcastReceivers();
        unregisterWifiApLimitObserver();
        unregisterCellularDataListener();
        if (this.mServiceLooper != null) {
            this.mServiceLooper.quit();
        }
        super.onDestroy();
    }

    private long getTetherStats() {
        try {
            NetworkStats stats = this.mNetworkManager.getNetworkStatsTethering();
            if (stats == null) {
                return 0;
            }
            long statsSize = 0;
            Entry entry = null;
            int size = stats.size();
            for (int i = 0; i < size; i++) {
                entry = stats.getValues(i, entry);
                statsSize += entry.rxBytes + entry.txBytes;
            }
            return statsSize;
        } catch (IllegalStateException e) {
            HwLog.e(TAG, "getTetherStats: IllegalStateException,problem reading network stats", e);
            return 0;
        } catch (RemoteException e2) {
            HwLog.e(TAG, "getTetherStats: RemoteException", e2);
            return 0;
        } catch (Exception e3) {
            HwLog.e(TAG, "getTetherStats: Exception", e3);
            return 0;
        }
    }

    private void updateStats(boolean isForceLog) {
        long tetherStats = getTetherStats();
        long statsMills = SystemClock.elapsedRealtime();
        if (tetherStats < this.mStartSize) {
            HwLog.w(TAG, "updateStats: Some error may happen ,skip. mLastStatsSize = " + (this.mLastStatsSize / 1024) + ", tetherStats = " + (tetherStats / 1024));
            return;
        }
        long statsSize = tetherStats - this.mStartSize;
        long elapsedSize = statsSize - this.mLastStatsSize;
        long elapsedMills = statsMills - this.mLastStatsMills;
        if (elapsedMills != 0) {
            this.mNetSpeed = (long) ((int) (elapsedSize / elapsedMills));
            this.mLastStatsMills = statsMills;
            this.mLastStatsSize = statsSize;
            if (this.mLimitSize >= 0) {
                this.mRemainSize = this.mLimitSize - statsSize;
            }
            Secure.putLong(getContentResolver(), WIFIAP_STATS, statsSize);
            long elapsedLogMills = statsMills - this.mLastStatsLogMills;
            if (isForceLog || elapsedLogMills >= STATS_INTERVAL_LOG) {
                elapsedSize = statsSize - this.mLastStatsLogSize;
                long speed = elapsedSize / elapsedLogMills;
                this.mLastStatsLogMills = statsMills;
                this.mLastStatsLogSize = this.mLastStatsSize;
                HwLog.i(TAG, "updateStats: statsSize = " + (statsSize / 1024) + ", Limit = " + (this.mLimitSize >= 0 ? this.mLimitSize / 1024 : -1) + ", mRemainSize = " + (this.mRemainSize / 1024) + ", elapsedSize = " + (elapsedSize / 1024) + ", elapsedMills = " + elapsedLogMills + ", speed = " + ((1000 * speed) / 1024));
            }
        }
    }

    private boolean shouldUpdateStats() {
        if (this.mDataActivityChangeFlag || !this.mIsScreenOn) {
            HwLog.i(TAG, "shouldUpdateStats: mDataActivityChangeFlag = " + this.mDataActivityChangeFlag + ", mIsScreenOn = " + this.mIsScreenOn);
            this.mDataActivityChangeFlag = false;
            return true;
        }
        HwLog.i(TAG, "shouldUpdateStats: mLastDataActivity = " + this.mLastDataActivity);
        switch (this.mLastDataActivity) {
            case 1:
            case 2:
            case 3:
                return true;
            default:
                return false;
        }
    }

    private void checkStatsData() {
        if (this.mIsApDisabling) {
            HwLog.d(TAG, "checkStatsData: Ap is disabling ,skip");
            return;
        }
        updateStats(false);
        if (this.mLimitSize < 0) {
            if (this.mIsDataConnAvailable) {
                this.mServiceHandler.removeMessages(1);
                this.mServiceHandler.sendEmptyMessageDelayed(1, STATS_INTERVAL_MAX);
                HwLog.i(TAG, "checkStatsData: keep update when mLimitSize= " + this.mLimitSize);
            }
        } else if (this.mRemainSize <= this.mNetSpeed * STATS_INTERVAL_MIN) {
            HwLog.i(TAG, "checkStatsData: reach limit , shut down AP now. Stats error = " + ((-this.mRemainSize) / 1024));
            shutdownWifiAp();
            sendLimitReachNotification();
        } else {
            if (this.mIsDataConnAvailable) {
                long updateDelay = getNextUpdateDelay();
                this.mServiceHandler.removeMessages(1);
                this.mServiceHandler.sendEmptyMessageDelayed(1, updateDelay);
                HwLog.i(TAG, "checkStatsData: keep update when mIsDataConnAvailable = " + this.mIsDataConnAvailable + " , UpdateDelay = " + updateDelay);
            }
        }
    }

    private long getNextUpdateDelay() {
        long maxDelay = STATS_INTERVAL_MAX;
        if (this.mMaxSpeed > 0) {
            maxDelay = MathUtils.constrain((this.mRemainSize * 1000) / this.mMaxSpeed, STATS_INTERVAL_MIN, STATS_INTERVAL_MAX);
        }
        if (0 == this.mNetSpeed) {
            return maxDelay;
        }
        return MathUtils.constrain((this.mRemainSize / this.mNetSpeed) / 3, STATS_INTERVAL_MIN, maxDelay);
    }

    private void onWifiApLimitChange() {
        this.mLimitSize = getWifiApLimit();
        HwLog.i(TAG, "onWifiApLimitChange: New limit = " + (this.mLimitSize < 0 ? -1 : this.mLimitSize / 1024));
        this.mServiceHandler.sendEmptyMessage(1);
    }

    private void onForceUpdateStats() {
        if (shouldUpdateStats()) {
            checkStatsData();
        }
    }

    private void onSysStatsUpdate() {
        checkStatsData();
    }

    private void onGlobalAlert() {
        checkStatsData();
        registerGlobalAlert(false);
    }

    private void onApDisabled() {
        updateStats(true);
        restoreAlertSetting();
        unregisterGlobalAlertObserver();
        this.mIsWatchingStarted = false;
        if (this.mIsApShutDownOnLimit) {
            Secure.putLong(getContentResolver(), WIFIAP_STATS, this.mLimitSize);
        }
        WifiApHelper.destroyInstance();
        HwLog.i(TAG, "onApDisabled: Finish update, Exit now.");
        stopSelf();
    }

    private void onStartWatching() {
        registerGlobalAlertObserver();
        registerGlobalAlert(true);
        adjustAlertSetting();
        initStatsInfo();
        this.mIsWatchingStarted = true;
    }

    private void shutdownWifiAp() {
        try {
            WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
            if (wifiManager == null) {
                HwLog.e(TAG, "shutdownWifiAp: Fail to get wifi manager");
                return;
            }
            wifiManager.setWifiApEnabled(null, false);
            this.mIsApShutDownOnLimit = true;
        } catch (Exception e) {
            HwLog.e(TAG, "shutdownWifiAp: Exception", e);
        }
    }

    private void sendLimitReachNotification() {
        WifiApHelper.sendReachLimitNotification(this.mContext, String.valueOf(this.mLimitSize / NETSTATS_THRESHOD_DFT) + "MB");
    }

    private void handleWifiAPStateChange(Context context, int apState) {
        switch (apState) {
            case 10:
                this.mServiceHandler.removeMessages(1);
                WifiApHelper.setApState(false);
                this.mIsApDisabling = true;
                HwLog.i(TAG, "handleWifiAPStateChange : Wifi AP is disabling");
                return;
            case 11:
                this.mServiceHandler.removeMessages(1);
                HwLog.i(TAG, "handleWifiAPStateChange : Wifi AP is disabled, trigger last update");
                this.mServiceHandler.sendEmptyMessage(4);
                return;
            default:
                return;
        }
    }
}
