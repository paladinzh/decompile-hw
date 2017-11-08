package com.huawei.netassistant.wifisecure;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager.ActionListener;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.sdk.tmsdk.TMSEngineFeature;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.procpolicy.ProcessPolicy;
import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.wifidetect.IWifiDetectListener;
import tmsdk.bg.module.wifidetect.WifiDetectManager;

public class HsmWifiDetectManager {
    public static final String ACTION_WIFI_SECURE_NOTIFICATION = "com.huawei.systemmanager.action.wifisec.notification";
    public static final String ACTION_WIFI_SECURE_RESULT = "com.huawei.systemmanager.action.wifisec.result";
    private static final long DELAY_DETECT = 3000;
    public static final String KEY_EXTRA_WIFICONFIG = "WIFI_CONFIG";
    private static final String KEY_WIFISECURE = "wifi_cloud_security_check";
    private static final int MSG_ARP = 3;
    private static final int MSG_DNS_AND_PHISHING = 2;
    private static final int MSG_DO_WIFIDETECT = 5;
    private static final int MSG_NETWORK_STATE = 1;
    private static final int MSG_TIMEOUT_DNS_AND_PHIHING = 4;
    private static final int NM_ID_WIFISECURE_WARNING = 1074042323;
    private static final String SETTINGS_SYSTEM_CHECKING_STATE = "wifi_secure_check_state";
    private static final String TAG = "HsmWifiDetectManager";
    private static final long TIMEOUT_DETECT = 60000;
    private static final int VAL_WIFISECURE_DEFAULT = 0;
    private static final int VAL_WIFISECURE_ENABLED = 1;
    private static HsmWifiDetectManager sInstance = null;
    private static WifiStateReceiver sWifiStateReceiver = null;
    private long endCheckTime;
    private Context mContext;
    private WifiDetectResult mDetectResult;
    private WifiDetectManager mTmsWifiDetectMgr;
    private Handler mWifiDetectMsgHandler;
    private ContentObserver mWifiSecSwitchObserver;
    private WifiStateManager mWifiStateMgr;
    private long startCheckTime;

    class HsmWifiDetectListener implements IWifiDetectListener {
        private WifiConfiguration mWifiConfig;

        public HsmWifiDetectListener(WifiConfiguration wifiConfig) {
            this.mWifiConfig = wifiConfig;
        }

        public void onResult(int result) {
            HsmWifiDetectManager.this.sendResultMsg(2, result, this.mWifiConfig);
        }
    }

    class WifiConfigSaveListener implements ActionListener {
        WifiConfiguration mWifiConfig;

        public WifiConfigSaveListener(WifiConfiguration wifiConfig) {
            this.mWifiConfig = wifiConfig;
        }

        public void onFailure(int reason) {
            HwLog.w(HsmWifiDetectManager.TAG, "onFailure: Fail to save config. reason = " + reason + "SSID = " + this.mWifiConfig.SSID);
            WifiConfiguration config = WifiConfigHelper.getConnectedWifiConfig(HsmWifiDetectManager.this.mContext);
            if (config == null) {
                HwLog.e(HsmWifiDetectManager.TAG, "onFailure: Fail to get connected wifi config");
            } else {
                HwLog.i(HsmWifiDetectManager.TAG, "onFailure: config.cloudSecurityCheck = " + WifiConfigHelper.getWifiSecConfig(config) + ", expect = " + WifiConfigHelper.getWifiSecConfig(this.mWifiConfig));
            }
        }

        public void onSuccess() {
            HwLog.i(HsmWifiDetectManager.TAG, "onSuccess: Save config successfully, SSID = " + this.mWifiConfig.SSID);
            WifiConfiguration config = WifiConfigHelper.getConnectedWifiConfig(HsmWifiDetectManager.this.mContext);
            if (config == null) {
                HwLog.e(HsmWifiDetectManager.TAG, "onSuccess: Fail to get connected wifi config");
            } else {
                HwLog.i(HsmWifiDetectManager.TAG, "onSuccess: config.cloudSecurityCheck = " + WifiConfigHelper.getWifiSecConfig(config) + ", SSID = " + config.SSID + ", expect = " + WifiConfigHelper.getWifiSecConfig(this.mWifiConfig));
            }
        }
    }

    public static synchronized HsmWifiDetectManager getInstance() {
        HsmWifiDetectManager hsmWifiDetectManager;
        synchronized (HsmWifiDetectManager.class) {
            if (sInstance == null) {
                sInstance = new HsmWifiDetectManager();
            }
            hsmWifiDetectManager = sInstance;
        }
        return hsmWifiDetectManager;
    }

    public static boolean initWifiSecFeature(Context context) {
        if (sWifiStateReceiver != null) {
            HwLog.i(TAG, "initWifiSecFeature: Already initialized");
            return true;
        } else if (!ProcessPolicy.shouldEnableWifiSec()) {
            HwLog.i(TAG, "initWifiSecFeature: Should not enable wifi secure detection");
            return false;
        } else if (isSupportWifiSecDetct()) {
            registerWifiStateReceiver(context);
            WifiPlusSecurityMonitor.getInstance(context).init();
            HwLog.i(TAG, "initWifiSecFeature: Finished");
            return true;
        } else {
            HwLog.i("trustspace", "initWifiSecFeature: Not support wifi secure detection");
            return false;
        }
    }

    public static boolean isWifiSecDetectOn(Context context) {
        boolean z = true;
        if (context == null) {
            HwLog.e(TAG, "isWifiSecDetectOn: Invalid context");
            return false;
        }
        if (1 != Global.getInt(context.getContentResolver(), KEY_WIFISECURE, 0)) {
            z = false;
        }
        return z;
    }

    public static boolean isSupportWifiSecDetct() {
        if (!TMSEngineFeature.shouldInitTmsEngine()) {
            HwLog.w(TAG, "isSupportWifiSecDetct: TMS is should not init");
            return false;
        } else if (WifiConfigHelper.isFwkSupportedWifiSecConfig()) {
            return true;
        } else {
            HwLog.w(TAG, "isSupportWifiSecDetct: Framework is not supported");
            return false;
        }
    }

    public synchronized void handleWifiStateChangeEvent(Context context, Intent intent) {
        if (this.mWifiStateMgr.isNewWifiConnection(context, intent)) {
            sendMsgDelayed(5, DELAY_DETECT);
            cancelSecureWarningNotification();
        } else if (this.mWifiStateMgr.isWifiDisconnected(context, intent)) {
            removeMsg(5);
            stopDetect();
        }
    }

    private HsmWifiDetectManager() {
        this.startCheckTime = 0;
        this.endCheckTime = 0;
        this.mContext = null;
        this.mTmsWifiDetectMgr = null;
        this.mWifiSecSwitchObserver = null;
        this.mWifiStateMgr = new WifiStateManager();
        this.mDetectResult = new WifiDetectResult();
        this.mContext = GlobalContext.getContext();
        this.mWifiDetectMsgHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                if (HsmWifiDetectManager.isWifiSecDetectOn(HsmWifiDetectManager.this.mContext)) {
                    switch (msg.what) {
                        case 1:
                        case 2:
                        case 3:
                            HsmWifiDetectManager.this.handleDetectResultMsgs(msg);
                            break;
                        case 4:
                            HsmWifiDetectManager.this.handleDetectTimeout(msg);
                            break;
                        case 5:
                            HsmWifiDetectManager.this.removeMsg(5);
                            HsmWifiDetectManager.this.startDetect();
                            break;
                        default:
                            return;
                    }
                    return;
                }
                HwLog.w(HsmWifiDetectManager.TAG, "handleMessage: Wifi secure is disabled ,drop it");
            }
        };
        registerWifiSecSwitchObserver();
    }

    private WifiConfiguration getWifiConfigFromMsg(Message msg) {
        if (msg.obj == null || !WifiConfiguration.class.isInstance(msg.obj)) {
            HwLog.w(TAG, "getWifiConfigFromMsg: Fail to get wifi config. msg.what = " + msg.what);
            return null;
        }
        WifiConfiguration wifiConfig = msg.obj;
        if (WifiConfigHelper.isCurrentlyConnected(this.mContext, wifiConfig)) {
            return wifiConfig;
        }
        HwLog.i(TAG, "getWifiConfigFromMsg: Expired wifi ,SSID = " + (wifiConfig != null ? wifiConfig.SSID : "") + ", msg = " + msg.what + ", result = " + msg.arg1);
        return null;
    }

    private void handleDetectResultMsgs(Message msg) {
        WifiConfiguration wifiConfig = getWifiConfigFromMsg(msg);
        if (wifiConfig != null) {
            switch (msg.what) {
                case 1:
                    handleNetworkStateResult(wifiConfig, msg.arg1);
                    break;
                case 2:
                    this.mWifiDetectMsgHandler.removeMessages(4);
                    handleDnsAndPhishingResult(wifiConfig, msg.arg1);
                    break;
                case 3:
                    handleARPResult(wifiConfig, msg.arg1);
                    break;
                default:
                    return;
            }
            if (this.mDetectResult.isDetectFinished()) {
                onDetectFinished(wifiConfig, this.mDetectResult);
                this.mDetectResult.reset();
            }
        }
    }

    private void handleDetectTimeout(Message msg) {
        WifiConfiguration wifiConfig = getWifiConfigFromMsg(msg);
        if (wifiConfig != null) {
            switch (msg.what) {
                case 4:
                    HwLog.w(TAG, "handleDetectTimeout: DNS and phishing detect timeout");
                    this.mDetectResult.setDnsAndPhishingResult(15);
                    if (this.mDetectResult.isDetectFinished()) {
                        onDetectFinished(wifiConfig, this.mDetectResult);
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private synchronized void startDetect() {
        if (!shouldTriggerWifiDetect(this.mContext)) {
            return;
        }
        if (initTmsWifiDetectManager()) {
            WifiConfiguration wifiConfig = WifiConfigHelper.getConnectedWifiConfig(this.mContext);
            if (wifiConfig == null) {
                HwLog.w(TAG, "startDetect: Fail to get connected wifi config, skip");
                return;
            }
            setDetectingState();
            this.mDetectResult.reset();
            HwLog.i(TAG, "startDetect: SSID = " + wifiConfig.SSID);
            detectDnsAndPhishing(this.mTmsWifiDetectMgr, wifiConfig, new HsmWifiDetectListener(wifiConfig));
            detectARP(this.mTmsWifiDetectMgr, wifiConfig);
            detectNetworkState(this.mTmsWifiDetectMgr, wifiConfig);
        }
    }

    private void setDetectingState() {
        if (this.mContext != null) {
            HwLog.i(TAG, "setDetectingState");
            System.putIntForUser(this.mContext.getContentResolver(), SETTINGS_SYSTEM_CHECKING_STATE, 1, 0);
        }
    }

    private void setDetectFinishedState() {
        if (this.mContext != null) {
            HwLog.i(TAG, "setDetectFinishedState");
            System.putIntForUser(this.mContext.getContentResolver(), SETTINGS_SYSTEM_CHECKING_STATE, 0, 0);
        }
    }

    private synchronized void stopDetect() {
        if (this.mTmsWifiDetectMgr != null) {
            HwLog.i(TAG, "stopDetect: do stop");
            this.mTmsWifiDetectMgr.free();
            this.mTmsWifiDetectMgr = null;
            this.mDetectResult.reset();
            cancelSecureWarningNotification();
        }
    }

    public static boolean shouldTriggerWifiDetect(Context context) {
        if (!isSupportWifiSecDetct()) {
            HwLog.w(TAG, "shouldTriggerWifiDetect: Wifi secure detect feature is not supported,skip");
            return false;
        } else if (isWifiSecDetectOn(context)) {
            return true;
        } else {
            HwLog.i(TAG, "shouldTriggerWifiDetect: Wifi secure detect is disabled");
            return false;
        }
    }

    private void registerWifiSecSwitchObserver() {
        HwLog.i(TAG, "registerWifiSecSwitchObserver:Starts");
        if (!isSupportWifiSecDetct()) {
            HwLog.w(TAG, "registerWifiSecSwitchObserver: Wifi secure detect is not supported ,skip");
        } else if (this.mWifiSecSwitchObserver != null) {
            HwLog.i(TAG, "registerWifiSecSwitchObserver: Already registered ,skip");
        } else {
            this.mWifiSecSwitchObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
                public void onChange(boolean selfChange) {
                    HsmWifiDetectManager.this.startCheckTime = System.currentTimeMillis();
                    if (HsmWifiDetectManager.isWifiSecDetectOn(HsmWifiDetectManager.this.mContext)) {
                        HwLog.i(HsmWifiDetectManager.TAG, "onChange: Wifi secure enabled ,trigger detect later, delay =  3000");
                        HsmWifiDetectManager.this.sendMsgDelayed(5, HsmWifiDetectManager.DELAY_DETECT);
                        return;
                    }
                    HwLog.i(HsmWifiDetectManager.TAG, "onChange: Wifi secure disabled, stop detect");
                    HsmWifiDetectManager.this.removeMsg(5);
                    HsmWifiDetectManager.this.stopDetect();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(KEY_WIFISECURE), true, this.mWifiSecSwitchObserver);
        }
    }

    private static void registerWifiStateReceiver(Context context) {
        if (context == null) {
            HwLog.e(TAG, "registerWifiStateReceiver: Invalid context");
            return;
        }
        sWifiStateReceiver = new WifiStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction(WifiStateManager.ACTION_ARP_RECONNECT_WIFI);
        filter.addAction("android.net.wifi.STATE_CHANGE");
        context.registerReceiverAsUser(sWifiStateReceiver, UserHandle.CURRENT, filter, null, null);
    }

    private synchronized boolean initTmsWifiDetectManager() {
        Utility.initSDK(this.mContext);
        if (!TMSEngineFeature.isSupportTMS()) {
            HwLog.w(TAG, "initTmsWifiDetectManager: TMS is not supported after initSDK");
            return false;
        } else if (this.mTmsWifiDetectMgr != null) {
            return true;
        } else {
            try {
                this.mTmsWifiDetectMgr = (WifiDetectManager) ManagerCreatorB.getManager(WifiDetectManager.class);
                if (this.mTmsWifiDetectMgr == null) {
                    HwLog.e(TAG, "initTmsWifiDetectManager: Fail to create WifiDetectManager");
                    return false;
                }
                int initCode = this.mTmsWifiDetectMgr.init();
                if (initCode < 0) {
                    HwLog.e(TAG, "initTmsWifiDetectManager: Failed, initCode = " + initCode);
                    return false;
                }
                HwLog.i(TAG, "initTmsWifiDetectManager: WifiDetectManager initCode = " + initCode);
                return true;
            } catch (NullPointerException e) {
                HwLog.e(TAG, "initTmsWifiDetectManager:NullPointerException found ");
                return false;
            } catch (NoClassDefFoundError e2) {
                HwLog.e(TAG, "initTmsWifiDetectManager: tms jar not exists.");
                return false;
            } catch (UnsatisfiedLinkError e3) {
                HwLog.e(TAG, "initTmsWifiDetectManager: UnsatisfiedLinkError.", e3);
                return false;
            }
        }
    }

    private void detectNetworkState(final WifiDetectManager wifiDetectMgr, final WifiConfiguration wifiConfig) {
        new Thread(new Runnable() {
            public void run() {
                HsmWifiDetectManager.this.sendResultMsg(1, wifiDetectMgr.detectNetworkState(), wifiConfig);
            }
        }).setName("HsmWifiDetectManager-detectNetworkState");
    }

    private void detectDnsAndPhishing(final WifiDetectManager wifiDetectMgr, final WifiConfiguration wifiConfig, final HsmWifiDetectListener detectListener) {
        Thread detectThread = new Thread(new Runnable() {
            public void run() {
                int nRet = wifiDetectMgr.detectDnsAndPhishing(detectListener);
                if (nRet < 0) {
                    HwLog.w(HsmWifiDetectManager.TAG, "detectDnsAndPhishing: Fail to start detect, nRet = " + nRet);
                    HsmWifiDetectManager.this.sendResultMsg(2, 15, wifiConfig);
                    return;
                }
                HwLog.i(HsmWifiDetectManager.TAG, "detectDnsAndPhishing: Detect starts, nRet = " + nRet);
            }
        });
        detectThread.setName("HsmWifiDetectManager-detectDnsAndPhishing");
        detectThread.start();
        sendMsgDelayed(4, 60000, wifiConfig);
    }

    private void detectARP(final WifiDetectManager wifiDetectMgr, final WifiConfiguration wifiConfig) {
        new Thread(new Runnable() {
            public void run() {
                int nRet = wifiDetectMgr.detectARP("mdetector");
                if (nRet < 0) {
                    HwLog.e(HsmWifiDetectManager.TAG, "detectARP: Fail to start detect, nRet = " + nRet);
                    HsmWifiDetectManager.this.sendResultMsg(3, 15, wifiConfig);
                    return;
                }
                HsmWifiDetectManager.this.sendResultMsg(3, nRet, wifiConfig);
            }
        }).setName("HsmWifiDetectManager-detectARP");
    }

    private void sendResultMsg(int msgId, int result, WifiConfiguration wifiConfig) {
        Message msg = this.mWifiDetectMsgHandler.obtainMessage(msgId, wifiConfig);
        msg.arg1 = result;
        this.mWifiDetectMsgHandler.sendMessage(msg);
    }

    private void sendMsgDelayed(int msgId, long delay, Object object) {
        this.mWifiDetectMsgHandler.sendMessageDelayed(this.mWifiDetectMsgHandler.obtainMessage(msgId, object), delay);
    }

    private void sendMsgDelayed(int msgId, long delay) {
        this.mWifiDetectMsgHandler.sendMessageDelayed(this.mWifiDetectMsgHandler.obtainMessage(msgId), delay);
    }

    private void removeMsg(int msgId) {
        this.mWifiDetectMsgHandler.removeMessages(msgId);
    }

    private void handleNetworkStateResult(WifiConfiguration wifiConfig, int result) {
        switch (result) {
            case 1:
                this.mDetectResult.setNetworkStateResult(1);
                HwLog.i(TAG, "handleNetworkStateResult: result = NETWORK_AVILABLE");
                return;
            case 2:
                this.mDetectResult.setNetworkStateResult(2);
                HwLog.i(TAG, "handleNetworkStateResult: result = NETWORK_NOTAVILABLE");
                return;
            case 3:
                this.mDetectResult.setNetworkStateResult(3);
                HwLog.i(TAG, "handleNetworkStateResult: result = NETWORK_NOTAVILABLE_APPROVE");
                return;
            case 15:
                this.mDetectResult.setNetworkStateResult(15);
                HwLog.i(TAG, "handleNetworkStateResult: result = RESULT_ERROR");
                return;
            default:
                this.mDetectResult.setNetworkStateResult(15);
                HwLog.w(TAG, "handleNetworkStateResult: Invalid result = " + result);
                return;
        }
    }

    private void handleDnsAndPhishingResult(WifiConfiguration wifiConfig, int result) {
        switch (result) {
            case 15:
                this.mDetectResult.setDnsAndPhishingResult(15);
                HwLog.i(TAG, "handleDnsAndPhishingResult: result = RESULT_ERROR");
                return;
            case 16:
                this.mDetectResult.setDnsAndPhishingResult(7);
                HwLog.i(TAG, "handleDnsAndPhishingResult: result = CLOUND_CHECK_NETWORK_ERROR");
                return;
            case 17:
                HwLog.i(TAG, "handleDnsAndPhishingResult: result = CLOUND_CHECK_NO_FAKE");
                if ("80:38:bc:01:68:71".equals(wifiConfig.BSSID)) {
                    HwLog.w(TAG, "handleDnsAndPhishingResult: Test AP, change result to CLOUND_CHECK_DNS_FAKE");
                    this.mDetectResult.setDnsAndPhishingResult(2);
                    return;
                }
                this.mDetectResult.setDnsAndPhishingResult(1);
                return;
            case 18:
                this.mDetectResult.setDnsAndPhishingResult(2);
                HsmStat.statE(3000);
                HwLog.i(TAG, "handleDnsAndPhishingResult: result = CLOUND_CHECK_DNS_FAKE");
                return;
            case 19:
                this.mDetectResult.setDnsAndPhishingResult(3);
                HsmStat.statE(Events.E_WIFI_SECURE_PHISHING_FAKE);
                HwLog.i(TAG, "handleDnsAndPhishingResult: result = CLOUND_CHECK_PHISHING_FAKE");
                return;
            default:
                this.mDetectResult.setDnsAndPhishingResult(15);
                HwLog.w(TAG, "handleDnsAndPhishingResult: Invalid result = " + result);
                return;
        }
    }

    private void handleARPResult(WifiConfiguration wifiConfig, int result) {
        switch (result) {
            case 15:
                this.mDetectResult.setArpResult(15);
                HwLog.w(TAG, "handleARPResult: Invalid result = RESULT_ERROR");
                return;
            case 261:
                this.mDetectResult.setArpResult(1);
                HwLog.i(TAG, "handleARPResult: result = ARP_OK");
                return;
            case 262:
                this.mDetectResult.setArpResult(2);
                HsmStat.statE(Events.E_WIFI_SECURE_ARP_FAKE);
                HwLog.i(TAG, "handleARPResult: result = ARP_FAKE");
                return;
            default:
                this.mDetectResult.setArpResult(15);
                HwLog.w(TAG, "handleARPResult: Invalid result = " + result);
                return;
        }
    }

    private void broadcastDetectResult(WifiConfiguration wifiConfig) {
        Intent intent = new Intent(ACTION_WIFI_SECURE_RESULT);
        intent.putExtra(KEY_EXTRA_WIFICONFIG, wifiConfig);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
        HwLog.i(TAG, "broadcastDetectResult: cloudSecurityCheck = " + WifiConfigHelper.getWifiSecConfig(wifiConfig));
    }

    private void sendSecureWarningNotification(WifiConfiguration wifiConfig) {
        Builder nBuilder = new Builder(this.mContext);
        String notifyTicker = this.mContext.getResources().getString(R.string.wifi_secure_notification_title);
        String notifyTitle = notifyTicker;
        String notifyContent = this.mContext.getResources().getString(R.string.wifi_secure_notification_content);
        Intent intent = new Intent(ACTION_WIFI_SECURE_NOTIFICATION);
        intent.putExtra(KEY_EXTRA_WIFICONFIG, wifiConfig);
        intent.setPackage(this.mContext.getPackageName());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728);
        nBuilder.setSmallIcon(R.drawable.ic_wlan_close_notification);
        nBuilder.setTicker(notifyTicker);
        nBuilder.setContentTitle(notifyTicker);
        nBuilder.setContentText(notifyContent);
        nBuilder.setContentIntent(pendingIntent);
        nBuilder.setAutoCancel(true);
        nBuilder.setDefaults(2);
        nBuilder.setPriority(2);
        Notification notification = nBuilder.build();
        NotificationManager nm = (NotificationManager) this.mContext.getSystemService("notification");
        nm.cancel(NM_ID_WIFISECURE_WARNING);
        nm.notify(NM_ID_WIFISECURE_WARNING, notification);
    }

    private void cancelSecureWarningNotification() {
        ((NotificationManager) this.mContext.getSystemService("notification")).cancel(NM_ID_WIFISECURE_WARNING);
        HwLog.i(TAG, "clearSecureWarningNotification");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void onDetectFinished(WifiConfiguration wifiConfig, WifiDetectResult result) {
        this.endCheckTime = System.currentTimeMillis();
        HwLog.i(TAG, "detect costs: " + (this.endCheckTime - this.startCheckTime) + " milliseconds");
        setDetectFinishedState();
        int nSavedResult = WifiConfigHelper.saveDetectResult(this.mContext, wifiConfig, result, new WifiConfigSaveListener(wifiConfig));
        if (nSavedResult < 0) {
            HwLog.w(TAG, "onDetectFinished: Some error happens ,skip");
            return;
        }
        WifiConfigHelper.setWifiSecConfig(wifiConfig, nSavedResult);
        broadcastDetectResult(wifiConfig);
        if (nSavedResult > 0) {
            HwLog.w(TAG, "onDetectFinished: Wifi is not secure , SSID = " + wifiConfig.SSID + ", result = " + WifiConfigHelper.getWifiSecConfig(wifiConfig));
            sendSecureWarningNotification(wifiConfig);
        } else {
            HwLog.i(TAG, "onDetectFinished: Wifi is secure , SSID = " + wifiConfig.SSID + ", result = " + WifiConfigHelper.getWifiSecConfig(wifiConfig));
            cancelSecureWarningNotification();
        }
    }
}
