package com.android.server.wifi.wifipro;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.net.wifi.wifipro.WifiProStatusUtils;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.List;

public class WifiHandover {
    public static final String ACTION_REQUEST_DUAL_BAND_WIFI_HANDOVER = "com.huawei.wifi.action.REQUEST_DUAL_BAND_WIFI_HANDOVER";
    public static final String ACTION_REQUEST_WIFI_2_WIFI = "com.huawei.wifi.action.REQUEST_WIFI_2_WIFI";
    public static final String ACTION_RESPONSE_DUAL_BAND_WIFI_HANDOVER = "com.huawei.wifi.action.RESPONSE_DUAL_BAND_WIFI_HANDOVER";
    public static final String ACTION_RESPONSE_WIFI_2_WIFI = "com.huawei.wifi.action.RESPONSE_WIFI_2_WIFI";
    private static final int CURRENT_STATE_IDLE = 0;
    private static final int CURRENT_STATE_WAITING_CONNECTION_COMPLETED = 2;
    private static final int CURRENT_STATE_WAITING_SCAN_RESULTS_FOR_CONNECT_WIFI = 1;
    private static final int CURRENT_STATE_WAITING_SCAN_RESULTS_FOR_WIFI_SWITCH = 4;
    private static final int CURRENT_STATE_WAITING_WIFI_2_WIFI_COMPLETED = 3;
    private static final int HANDLER_CMD_NOTIFY_GOOD_AP = 4;
    private static final int HANDLER_CMD_SCAN_RESULTS = 1;
    private static final int HANDLER_CMD_WIFI_2_WIFI = 3;
    private static final int HANDLER_CMD_WIFI_CONNECTED = 2;
    private static final int HANDOVER_STATE_WAITING_DUAL_BAND_WIFI_CONNECT = 5;
    public static final int HANDOVER_STATUS_CONNECT_AUTH_FAILED = -7;
    public static final int HANDOVER_STATUS_CONNECT_REJECT_FAILED = -6;
    public static final int HANDOVER_STATUS_DISALLOWED = -4;
    public static final int HANDOVER_STATUS_IN_PROGRESS = -5;
    public static final int HANDOVER_STATUS_NO_NETWORKS = -2;
    public static final int HANDOVER_STATUS_NO_SCAN_RESULTS = -1;
    public static final int HANDOVER_STATUS_OK = 0;
    public static final int HANDOVER_STATUS_UNKNOWN_FAILURE = -3;
    public static final int INVALID_RSSI = -200;
    private static final int NETWORK_HANDLER_CMD_DUAL_BAND_WIFI_CONNECT = 5;
    public static final int NETWORK_HANDOVER_TYPE_CONNECT_SPECIFIC_WIFI = 2;
    public static final int NETWORK_HANDOVER_TYPE_DUAL_BAND_WIFI_CONNECT = 4;
    public static final int NETWORK_HANDOVER_TYPE_UNKNOWN = -1;
    public static final int NETWORK_HANDOVER_TYPE_WIFI_TO_WIFI = 1;
    public static final int QOS_LEVEL_NO_INTERNET = -1;
    public static final int QOS_LEVEL_POOR = 2;
    public static final int QOS_LEVEL_UNUSABLE = 0;
    public static final int QOS_LEVEL_VERY_POOR = 1;
    public static final String TAG = "WifiHandover";
    public static final String WIFI_HANDOVER_BLACKLIST = "com.huawei.wifi.handover.blacklist";
    public static final String WIFI_HANDOVER_COMPLETED_STATUS = "com.huawei.wifi.handover.status";
    public static final String WIFI_HANDOVER_NETWORK_BSSID = "com.huawei.wifi.handover.bssid";
    public static final String WIFI_HANDOVER_NETWORK_SSID = "com.huawei.wifi.handover.ssid";
    public static final String WIFI_HANDOVER_NETWORK_SWITCHTYPE = "com.huawei.wifi.handover.switchtype";
    public static final String WIFI_HANDOVER_NETWORK_WIFICONFIG = "com.huawei.wifi.handover.wificonfig";
    public static final String WIFI_HANDOVER_QOS_LEVEL = "com.huawei.wifi.handover.qos.level";
    public static final String WIFI_HANDOVER_RECV_PERMISSION = "com.huawei.wifipro.permission.RECV.WIFI_HANDOVER";
    public static final String WIFI_HANDOVER_THRESHOLD = "com.huawei.wifi.handover.threshold";
    private static Context mContext = null;
    private BroadcastReceiver mBroadcastReceiver;
    private INetworksHandoverCallBack mCallBack;
    private IntentFilter mIntentFilter;
    private int mNetwoksHandoverState;
    private int mNetwoksHandoverType;
    private Handler mNetworkHandler;
    private NetworkQosMonitor mNetworkQosMonitor;
    private String mOldHandoverSsid;
    private ArrayList<String> mSwitchBlacklist;
    private String mToConnectBssid;
    private String mToConnectSsid;
    private WifiManager mWifiManager;

    private static class WifiSwitchCandidate {
        private ScanResult bestScanResult;
        private WifiConfiguration bestWifiConfig;

        public WifiSwitchCandidate(WifiConfiguration bestWifiConfig, ScanResult bestScanResult) {
            this.bestWifiConfig = bestWifiConfig;
            this.bestScanResult = bestScanResult;
        }

        public WifiConfiguration getWifiConfig() {
            return this.bestWifiConfig;
        }

        public ScanResult getScanResult() {
            return this.bestScanResult;
        }
    }

    public WifiHandover(Context context, INetworksHandoverCallBack callBack) {
        this.mCallBack = callBack;
        setWifiproContext(context);
        initialize();
        registerReceiverAndHandler();
    }

    public void registerCallBack(INetworksHandoverCallBack callBack, NetworkQosMonitor qosMonitor) {
        if (this.mCallBack == null) {
            this.mCallBack = callBack;
        }
        if (this.mBroadcastReceiver == null && mContext != null) {
            registerReceiverAndHandler();
        }
        this.mNetworkQosMonitor = qosMonitor;
    }

    public void unRegisterCallBack() {
        this.mCallBack = null;
        if (this.mBroadcastReceiver != null && mContext != null) {
            mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.mBroadcastReceiver = null;
            this.mIntentFilter = null;
            this.mNetworkHandler = null;
        }
    }

    private void initialize() {
        this.mNetwoksHandoverType = -1;
        this.mNetwoksHandoverState = 0;
        this.mWifiManager = (WifiManager) mContext.getSystemService("wifi");
    }

    private void registerReceiverAndHandler() {
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.mIntentFilter.addAction(ACTION_RESPONSE_WIFI_2_WIFI);
        this.mIntentFilter.addAction(ACTION_RESPONSE_DUAL_BAND_WIFI_HANDOVER);
        this.mNetwoksHandoverType = -1;
        this.mNetwoksHandoverState = 0;
        this.mNetworkHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        WifiHandover.this.handleScanResultsForConnectWiFi();
                        break;
                    case 2:
                        boolean connectStatus = false;
                        WifiInfo newWifiInfo = WifiHandover.this.mWifiManager.getConnectionInfo();
                        WifiHandover.LOGD("HANDLER_CMD_WIFI_CONNECTED, newWifiInfo = " + newWifiInfo);
                        if (!(newWifiInfo == null || newWifiInfo.getBSSID() == null || !newWifiInfo.getBSSID().equals(WifiHandover.this.mToConnectBssid))) {
                            connectStatus = true;
                        }
                        WifiHandover.this.sendNetworkHandoverResult(WifiHandover.this.mNetwoksHandoverType, connectStatus, WifiHandover.this.mToConnectBssid, 0);
                        WifiHandover.this.mNetwoksHandoverType = -1;
                        WifiHandover.this.mNetwoksHandoverState = 0;
                        WifiHandover.this.mToConnectBssid = null;
                        break;
                    case 3:
                        int code = msg.arg1;
                        String ssid = msg.obj.getString(WifiHandover.WIFI_HANDOVER_NETWORK_SSID);
                        boolean handoverStatusOk = code == 0;
                        if (handoverStatusOk) {
                            WifiHandover.this.mNetworkQosMonitor.resetMonitorStatus();
                        }
                        WifiHandover.this.sendNetworkHandoverResult(WifiHandover.this.mNetwoksHandoverType, handoverStatusOk, ssid, 0);
                        WifiHandover.this.mNetwoksHandoverType = -1;
                        WifiHandover.this.mNetwoksHandoverState = 0;
                        WifiHandover.this.mOldHandoverSsid = null;
                        WifiHandover.this.mSwitchBlacklist = null;
                        break;
                    case 4:
                        WifiHandover.this.notifyGoodApFound();
                        break;
                    case 5:
                        int handoverStatus = msg.arg1;
                        String apSsid = msg.obj.getString(WifiHandover.WIFI_HANDOVER_NETWORK_SSID);
                        boolean dualbandhandoverOK = false;
                        if (handoverStatus == 0 && WifiHandover.this.mToConnectSsid.equals(apSsid)) {
                            dualbandhandoverOK = true;
                        }
                        if (dualbandhandoverOK) {
                            WifiHandover.this.mNetworkQosMonitor.resetMonitorStatus();
                        }
                        WifiHandover.this.sendNetworkHandoverResult(WifiHandover.this.mNetwoksHandoverType, dualbandhandoverOK, apSsid, handoverStatus);
                        WifiHandover.this.mNetwoksHandoverType = -1;
                        WifiHandover.this.mNetwoksHandoverState = 0;
                        break;
                }
                super.handleMessage(msg);
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.net.wifi.SCAN_RESULTS".equals(intent.getAction())) {
                    if (WifiHandover.this.mNetwoksHandoverState == 1) {
                        WifiHandover.this.mNetworkHandler.sendMessage(Message.obtain(WifiHandover.this.mNetworkHandler, 1));
                    } else if (WifiHandover.this.mNetwoksHandoverState == 4) {
                        WifiHandover.this.trySwitchWifiNetwork(WifiHandover.this.selectQualifiedCandidate());
                    } else if (WifiProCommonUtils.isWifiConnected(WifiHandover.this.mWifiManager) && WifiHandover.this.mNetwoksHandoverState == 0) {
                        WifiHandover.this.trySwitchWifiNetwork(WifiHandover.this.selectQualifiedCandidate());
                        WifiHandover.this.mNetworkHandler.sendMessage(Message.obtain(WifiHandover.this.mNetworkHandler, 4));
                    }
                } else if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    if (WifiHandover.this.mNetwoksHandoverState == 2) {
                        NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                        if (netInfo != null && netInfo.getState() == State.CONNECTED) {
                            WifiHandover.this.mNetworkHandler.sendMessage(Message.obtain(WifiHandover.this.mNetworkHandler, 2));
                        }
                    }
                } else if (WifiHandover.ACTION_RESPONSE_WIFI_2_WIFI.equals(intent.getAction())) {
                    if (WifiHandover.this.mNetwoksHandoverState == 3) {
                        status = intent.getIntExtra(WifiHandover.WIFI_HANDOVER_COMPLETED_STATUS, -100);
                        bssid = intent.getStringExtra(WifiHandover.WIFI_HANDOVER_NETWORK_BSSID);
                        ssid = intent.getStringExtra(WifiHandover.WIFI_HANDOVER_NETWORK_SSID);
                        bundle = new Bundle();
                        bundle.putString(WifiHandover.WIFI_HANDOVER_NETWORK_BSSID, bssid);
                        bundle.putString(WifiHandover.WIFI_HANDOVER_NETWORK_SSID, ssid);
                        WifiHandover.LOGW("ACTION_RESPONSE_WIFI_2_WIFI received, status = " + status + ", bssid = " + bssid + ", new ssid = " + ssid + ", old ssid = " + WifiHandover.this.mOldHandoverSsid);
                        WifiHandover.this.mNetworkHandler.sendMessage(Message.obtain(WifiHandover.this.mNetworkHandler, 3, status, -1, bundle));
                    }
                } else if (WifiHandover.ACTION_RESPONSE_DUAL_BAND_WIFI_HANDOVER.equals(intent.getAction()) && WifiHandover.this.mNetwoksHandoverState == 5) {
                    status = intent.getIntExtra(WifiHandover.WIFI_HANDOVER_COMPLETED_STATUS, -100);
                    bssid = intent.getStringExtra(WifiHandover.WIFI_HANDOVER_NETWORK_BSSID);
                    ssid = intent.getStringExtra(WifiHandover.WIFI_HANDOVER_NETWORK_SSID);
                    bundle = new Bundle();
                    bundle.putString(WifiHandover.WIFI_HANDOVER_NETWORK_BSSID, bssid);
                    bundle.putString(WifiHandover.WIFI_HANDOVER_NETWORK_SSID, ssid);
                    WifiHandover.LOGD("ACTION_RESPONSE_DUAL_BAND_WIFI_HANDOVER received, status = " + status + ", bssid = " + bssid + ", ssid = " + ssid);
                    WifiHandover.this.mNetworkHandler.sendMessage(Message.obtain(WifiHandover.this.mNetworkHandler, 5, status, -1, bundle));
                }
            }
        };
        mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter, WIFI_HANDOVER_RECV_PERMISSION, null);
    }

    private void sendNetworkHandoverResult(int type, boolean status, String bssid, int errorReason) {
        if (this.mCallBack != null && type != -1) {
            LOGW("sendNetworkHandoverResult, type = " + type + ", status = " + status + ", bssid = " + bssid + " ,errorReason = " + errorReason);
            this.mCallBack.onWifiHandoverChange(type, status, bssid, errorReason);
        }
    }

    private void notifyWifiAvailableStatus(boolean status, int bestRssi, String targetSsid) {
        if (this.mCallBack == null) {
            LOGW("notifyWifiAvailableStatus, mCallBack == null");
        } else {
            this.mCallBack.onCheckAvailableWifi(status, bestRssi, targetSsid);
        }
    }

    private int notifyGoodApFound() {
        int nextRssi = INVALID_RSSI;
        String nextSsid = null;
        List<ScanResult> scanResults = this.mWifiManager.getScanResults();
        if (scanResults == null || scanResults.size() == 0) {
            LOGW("notifyGoodApFound, WiFi scan results are invalid, getScanResults = " + scanResults);
            return INVALID_RSSI;
        }
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            LOGW("notifyGoodApFound, WiFi configured networks are invalid, getConfiguredNetworks = " + configNetworks);
            return INVALID_RSSI;
        }
        WifiConfiguration current = WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager);
        String currentSsid = WifiProCommonUtils.getCurrentSsid(this.mWifiManager);
        String currentBssid = WifiProCommonUtils.getCurrentBssid(this.mWifiManager);
        String configKey = current != null ? current.configKey() : null;
        for (int i = 0; i < scanResults.size(); i++) {
            ScanResult nextResult = (ScanResult) scanResults.get(i);
            String scanSsid = "\"" + nextResult.SSID + "\"";
            String scanResultEncrypt = nextResult.capabilities;
            boolean equals = currentBssid != null ? currentBssid.equals(nextResult.BSSID) : false;
            boolean sameConfigKey;
            if (currentSsid == null || !currentSsid.equals(scanSsid)) {
                sameConfigKey = false;
            } else {
                sameConfigKey = WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, configKey);
            }
            if (!equals && !r17 && nextResult.level >= -82 && nextResult.level > nextRssi) {
                for (int k = 0; k < configNetworks.size(); k++) {
                    WifiConfiguration nextConfig = (WifiConfiguration) configNetworks.get(k);
                    int disableReason = nextConfig.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
                    if ((!nextConfig.noInternetAccess || NetworkHistoryUtils.allowWifiConfigRecovery(nextConfig.internetHistory)) && disableReason <= 0 && (!(nextConfig.portalNetwork && WifiProCommonUtils.matchedRequestByHistory(nextConfig.internetHistory, 102)) && nextConfig.SSID != null && nextConfig.SSID.equals(scanSsid) && WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, nextConfig.configKey()))) {
                        nextRssi = nextResult.level;
                        int nextId = nextConfig.networkId;
                        nextSsid = nextResult.SSID;
                        break;
                    }
                }
            }
        }
        notifyWifiAvailableStatus(nextRssi != -200, nextRssi, nextSsid);
        return nextRssi;
    }

    private boolean handleScanResultsForConnectWiFi() {
        List<ScanResult> scanResults = this.mWifiManager.getScanResults();
        if (scanResults == null || scanResults.size() == 0) {
            LOGW("handleScanResultsForConnectWiFi, WiFi scan results are invalid, getScanResults = " + scanResults);
            return false;
        }
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            LOGW("handleScanResultsForConnectWiFi, WiFi configured networks are invalid, getConfiguredNetworks = " + configNetworks);
            return false;
        }
        int nextId = -1;
        int nextRssi = INVALID_RSSI;
        boolean found = false;
        int i = 0;
        while (!found && i < scanResults.size()) {
            ScanResult nextResult = (ScanResult) scanResults.get(i);
            String scanSsid = "\"" + nextResult.SSID + "\"";
            if (nextResult.BSSID.equals(this.mToConnectBssid)) {
                for (int k = 0; k < configNetworks.size(); k++) {
                    WifiConfiguration nextConfig = (WifiConfiguration) configNetworks.get(k);
                    if (nextConfig.SSID != null && nextConfig.SSID.equals(scanSsid)) {
                        nextRssi = nextResult.level;
                        nextId = nextConfig.networkId;
                        found = true;
                        break;
                    }
                }
            }
            i++;
        }
        LOGD("handleScanResultsForConnectWiFi, nextId = " + nextId + ", nextRssi = " + nextRssi);
        if (nextId == -1) {
            return false;
        }
        this.mNetwoksHandoverState = 2;
        this.mWifiManager.connect(nextId, null);
        return true;
    }

    private boolean invalidConfigNetwork(WifiConfiguration config) {
        if (this.mNetwoksHandoverState == 4 && this.mSwitchBlacklist != null) {
            for (int l = 0; l < this.mSwitchBlacklist.size(); l++) {
                if (config.SSID.equals(this.mSwitchBlacklist.get(l))) {
                    LOGW("selectQualifiedCandidate, switch black list filter it, ssid = " + config.SSID);
                    return true;
                }
            }
        }
        int disableReason = config.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
        if (disableReason >= 2 && disableReason <= 7) {
            LOGW("selectQualifiedCandidate, wifi switch, ssid = " + config.SSID + ", disableReason = " + disableReason);
            return true;
        } else if (config.portalNetwork && WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 102)) {
            LOGW("selectQualifiedCandidate, wifi switch, ignore the portal network, ssid = " + config.SSID);
            return true;
        } else if (!config.noInternetAccess || NetworkHistoryUtils.allowWifiConfigRecovery(config.internetHistory)) {
            return false;
        } else {
            LOGW("selectQualifiedCandidate, wifi switch, ignore the network has no internet, ssid = " + config.SSID);
            return true;
        }
    }

    private WifiSwitchCandidate getBetterSignalCandidate(WifiSwitchCandidate last, WifiSwitchCandidate current) {
        if (current == null || current.bestScanResult == null || (last != null && last.bestScanResult != null && last.bestScanResult.level >= current.bestScanResult.level)) {
            return last;
        }
        return current;
    }

    private WifiSwitchCandidate getBackupSwitchCandidate(WifiConfiguration currentConfig, ScanResult currentResult, WifiSwitchCandidate lastCandidate) {
        if (this.mNetwoksHandoverState == 4 && currentConfig.noInternetAccess && NetworkHistoryUtils.allowWifiConfigRecovery(currentConfig.internetHistory)) {
            return getBetterSignalCandidate(lastCandidate, new WifiSwitchCandidate(currentConfig, currentResult));
        }
        return null;
    }

    private WifiSwitchCandidate getBestSwitchCandidate(WifiConfiguration currentConfig, ScanResult currentResult, WifiSwitchCandidate lastCandidate) {
        if (currentConfig == null || currentResult == null) {
            return lastCandidate;
        }
        if (lastCandidate == null || lastCandidate.bestWifiConfig == null || lastCandidate.bestScanResult == null) {
            return new WifiSwitchCandidate(currentConfig, currentResult);
        }
        if (this.mNetwoksHandoverState == 0 && SystemClock.elapsedRealtime() - (currentResult.timestamp / 1000) > 200) {
            return lastCandidate;
        }
        ScanResult lastResult = lastCandidate.bestScanResult;
        if (((ScanResult.is5GHz(lastResult.frequency) && ScanResult.is5GHz(currentResult.frequency)) || (ScanResult.is24GHz(lastResult.frequency) && ScanResult.is24GHz(currentResult.frequency))) && currentResult.level > lastResult.level) {
            return new WifiSwitchCandidate(currentConfig, currentResult);
        }
        if (ScanResult.is5GHz(lastResult.frequency) && ScanResult.is24GHz(currentResult.frequency) && lastResult.level < -75 && currentResult.level > lastResult.level) {
            return new WifiSwitchCandidate(currentConfig, currentResult);
        }
        if (ScanResult.is24GHz(lastResult.frequency) && ScanResult.is5GHz(currentResult.frequency) && (currentResult.level > lastResult.level || currentResult.level >= -75)) {
            return new WifiSwitchCandidate(currentConfig, currentResult);
        }
        return lastCandidate;
    }

    private WifiSwitchCandidate getAutoRoamCandidate(int currentSignalRssi, WifiSwitchCandidate bestCandidate) {
        if (!(this.mNetwoksHandoverState != 0 || bestCandidate == null || bestCandidate.bestScanResult == null)) {
            if (HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(bestCandidate.bestScanResult.level) - HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(currentSignalRssi) < 2 || bestCandidate.bestScanResult.level - currentSignalRssi < 10) {
                return null;
            }
        }
        return bestCandidate;
    }

    private WifiSwitchCandidate selectQualifiedCandidate() {
        List<ScanResult> scanResults = this.mWifiManager.getScanResults();
        WifiConfiguration current = WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager);
        int currentRssi = WifiProCommonUtils.getCurrentRssi(this.mWifiManager);
        int currentLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(currentRssi);
        if (scanResults == null || scanResults.size() == 0) {
            LOGW("selectQualifiedCandidate, WiFi scan results are invalid, getScanResults = " + scanResults);
            return null;
        } else if (current == null || current.SSID == null) {
            LOGW("selectQualifiedCandidate, current connected wifi configuration is null");
            return null;
        } else if (this.mNetwoksHandoverState == 0 && currentLevel >= 4) {
            return null;
        } else {
            List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
            if (configNetworks == null || configNetworks.size() == 0) {
                LOGW("selectQualifiedCandidate, WiFi configured networks are invalid, getConfiguredNetworks = " + configNetworks);
                return null;
            }
            String currentSsid = current.SSID;
            String currentBssid = WifiProCommonUtils.getCurrentBssid(this.mWifiManager);
            String currentEncrypt = current.configKey();
            WifiSwitchCandidate bestSwitchCandidate = null;
            WifiSwitchCandidate backupSwitchCandidate = null;
            for (int i = 0; i < scanResults.size(); i++) {
                ScanResult nextResult = (ScanResult) scanResults.get(i);
                String scanSsid = "\"" + nextResult.SSID + "\"";
                String scanResultEncrypt = nextResult.capabilities;
                boolean equals = currentBssid != null ? currentBssid.equals(nextResult.BSSID) : false;
                boolean isSameEncryptType;
                if (this.mNetwoksHandoverState == 4 && currentSsid.equals(scanSsid)) {
                    isSameEncryptType = WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, currentEncrypt);
                } else {
                    isSameEncryptType = false;
                }
                boolean idleConfigKeyDiff = this.mNetwoksHandoverState == 0 ? (currentSsid.equals(scanSsid) && WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, currentEncrypt)) ? false : true : false;
                if (!equals && !r23 && !idleConfigKeyDiff && nextResult.level >= -82) {
                    for (int k = 0; k < configNetworks.size(); k++) {
                        boolean networkMatched;
                        WifiConfiguration nextConfig = (WifiConfiguration) configNetworks.get(k);
                        if (nextConfig == null || nextConfig.SSID == null || !nextConfig.SSID.equals(scanSsid)) {
                            networkMatched = false;
                        } else {
                            networkMatched = WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, nextConfig.configKey());
                        }
                        if (networkMatched && !invalidConfigNetwork(nextConfig)) {
                            backupSwitchCandidate = getBackupSwitchCandidate(nextConfig, nextResult, backupSwitchCandidate);
                            bestSwitchCandidate = getBestSwitchCandidate(nextConfig, nextResult, bestSwitchCandidate);
                            break;
                        }
                    }
                }
            }
            if (bestSwitchCandidate == null && backupSwitchCandidate != null) {
                bestSwitchCandidate = backupSwitchCandidate;
            }
            return getAutoRoamCandidate(currentRssi, bestSwitchCandidate);
        }
    }

    private void trySwitchWifiNetwork(WifiSwitchCandidate switchCandidate) {
        WifiConfiguration current = WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager);
        if (current == null || switchCandidate == null) {
            if (this.mNetwoksHandoverState == 4) {
                sendNetworkHandoverResult(this.mNetwoksHandoverType, false, null, 0);
            }
            this.mNetwoksHandoverType = -1;
            this.mNetwoksHandoverState = 0;
            this.mOldHandoverSsid = null;
            this.mSwitchBlacklist = null;
            return;
        }
        WifiConfiguration best = switchCandidate.getWifiConfig();
        WifiStateMachine globalHwWifiStateMachine = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
        if (globalHwWifiStateMachine != null) {
            if (this.mNetwoksHandoverState == 4) {
                this.mNetwoksHandoverState = 3;
                globalHwWifiStateMachine.startWifi2WifiRequest();
            }
            if (current.networkId == best.networkId || current.isLinked(best)) {
                LOGD("trySwitchWifiNetwork: Roaming from " + current.SSID + " to " + best.SSID);
                globalHwWifiStateMachine.autoRoamToNetwork(best.networkId, switchCandidate.getScanResult());
            } else if (this.mNetwoksHandoverState == 3) {
                LOGD("trySwitchWifiNetwork: Reconnect from " + current.SSID + " to " + best.SSID);
                globalHwWifiStateMachine.autoConnectToNetwork(best.networkId, switchCandidate.getScanResult().BSSID);
            }
        }
    }

    public boolean handleWifiToWifi(ArrayList<String> invalidNetworks, int threshold, int qosLevel) {
        if (invalidNetworks == null) {
            LOGW("handleWifiToWifi, inputed arg is invalid, invalidNetworks is null");
            return false;
        }
        this.mSwitchBlacklist = (ArrayList) invalidNetworks.clone();
        WifiInfo currWifiInfo = this.mWifiManager.getConnectionInfo();
        if (currWifiInfo == null || currWifiInfo.getSupplicantState() != SupplicantState.COMPLETED) {
            LOGW("handleWifiToWifi, WiFi connection info is invalid, getConnectionInfo = " + currWifiInfo);
            return false;
        }
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            LOGW("handleWifiToWifi, WiFi configured networks are invalid, getConfiguredNetworks = " + configNetworks);
            return false;
        }
        LOGW("handleWifiToWifi, old bssid = " + currWifiInfo.getBSSID() + ", old ssid = " + currWifiInfo.getSSID() + ", qosLevel = " + qosLevel + ", blacklist = " + this.mSwitchBlacklist);
        this.mOldHandoverSsid = currWifiInfo.getSSID();
        this.mNetwoksHandoverType = 1;
        this.mNetwoksHandoverState = 4;
        requestScan();
        return true;
    }

    public boolean hasAvailableWifiNetwork(List<String> invalidNetworks, int threshold, String currBssid, String currSSid) {
        LOGD("hasAvailableWifiNetwork, invalidNetworks = " + invalidNetworks + ", threshold = " + threshold + ", currSSid = " + currSSid + ", currBssid = " + currBssid);
        List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
        if (configNetworks == null || configNetworks.size() == 0) {
            LOGW("hasAvailableWifiNetwork, WiFi configured networks are invalid, getConfiguredNetworks = " + configNetworks);
            return false;
        }
        this.mNetwoksHandoverType = -1;
        this.mNetwoksHandoverState = 0;
        requestScan();
        return true;
    }

    private void requestScan() {
        WifiStateMachine globalHwWifiStateMachine = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
        if (globalHwWifiStateMachine != null) {
            globalHwWifiStateMachine.startScan(Binder.getCallingUid(), 0, null, null);
        }
    }

    public boolean connectWifiNetwork(String bssid) {
        if (bssid == null || bssid.length() == 0) {
            LOGW("connectWifiNetwork, inputed arg is invalid, bssid = " + bssid);
            return false;
        }
        LOGD("connectWifiNetwork, bssid = " + bssid);
        WifiInfo currWifiInfo = this.mWifiManager.getConnectionInfo();
        if (currWifiInfo == null || !bssid.equals(currWifiInfo.getBSSID())) {
            List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
            if (configNetworks == null || configNetworks.size() == 0) {
                LOGW("connectWifiNetwork, WiFi configured networks are invalid, getConfiguredNetworks = " + configNetworks);
                return false;
            }
            this.mToConnectBssid = bssid;
            this.mNetwoksHandoverType = 2;
            this.mNetwoksHandoverState = 1;
            requestScan();
            return true;
        }
        LOGW("connectWifiNetwork, already connected, ignore it.");
        return true;
    }

    public boolean handleDualBandWifiConnect(String bssid, String ssid, int authType, int switchType) {
        Log.d(TAG, "DualBandWifiConnect, bssid = " + bssid + ", ssid = " + ssid + ", authType = " + authType + ", switchType = " + switchType);
        if (bssid == null || ssid == null || switchType < 1 || switchType > 3) {
            Log.d(TAG, "DualBandWifiConnect, inputed arg is invalid, bssid = " + bssid + ", ssid = " + ssid + " , switchType = " + switchType);
            return false;
        }
        WifiInfo currWifiInfo = this.mWifiManager.getConnectionInfo();
        if (currWifiInfo == null || !bssid.equals(currWifiInfo.getBSSID())) {
            List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
            if (configNetworks == null || configNetworks.size() == 0) {
                Log.d(TAG, "DualBandWifiConnect, WiFi configured networks are invalid, getConfiguredNetworks = " + configNetworks);
                return false;
            }
            Parcelable changeConfig = null;
            for (WifiConfiguration nextConfig : configNetworks) {
                LOGD("DualBandWifiConnect, nextConfig.BSSID = " + nextConfig.BSSID + " SSID = " + nextConfig.SSID);
                if (isValidConfig(nextConfig) && ssid.equals(nextConfig.SSID) && authType == nextConfig.getAuthType()) {
                    changeConfig = nextConfig;
                    break;
                }
            }
            if (changeConfig == null) {
                Log.d(TAG, "DualBandWifiConnect, WifiConfiguration is null ");
                HwDualBandInformationManager mManager = HwDualBandInformationManager.getInstance();
                if (mManager != null) {
                    mManager.delectDualBandAPInfoBySsid(ssid, authType);
                }
                return false;
            }
            Log.d(TAG, "DualBandWifiConnect, changeConfig.SSID = " + changeConfig.SSID + ", AuthType = " + changeConfig.getAuthType());
            this.mToConnectSsid = ssid;
            this.mNetwoksHandoverType = 4;
            this.mNetwoksHandoverState = 5;
            Intent intent = new Intent(ACTION_REQUEST_DUAL_BAND_WIFI_HANDOVER);
            Bundle mBundle = new Bundle();
            mBundle.putParcelable(WIFI_HANDOVER_NETWORK_WIFICONFIG, changeConfig);
            intent.putExtras(mBundle);
            intent.putExtra(WIFI_HANDOVER_NETWORK_SWITCHTYPE, switchType);
            mContext.sendBroadcastAsUser(intent, UserHandle.ALL, WIFI_HANDOVER_RECV_PERMISSION);
            return true;
        }
        Log.d(TAG, "DualBandWifiConnect, already connected, ignore it.");
        return true;
    }

    public int getNetwoksHandoverType() {
        return this.mNetwoksHandoverType;
    }

    private boolean isValidConfig(WifiConfiguration config) {
        boolean z = true;
        if (config == null) {
            return false;
        }
        int cc = config.allowedKeyManagement.cardinality();
        Log.e(TAG, "config isValid cardinality=" + cc);
        if (cc > 1) {
            z = false;
        }
        return z;
    }

    private static void setWifiproContext(Context context) {
        mContext = context;
    }

    public static boolean isWifiProEnabled() {
        if (mContext == null) {
            return false;
        }
        return WifiProStatusUtils.isWifiProEnabledViaXml(mContext);
    }

    private static void LOGD(String msg) {
        Log.d(TAG, msg);
    }

    private static void LOGW(String msg) {
        Log.w(TAG, msg);
    }
}
