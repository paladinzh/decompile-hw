package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Flog;
import android.util.Log;
import com.android.server.wifi.wifipro.PortalAutoFillManager;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.util.List;
import java.util.Objects;

public class HiLinkController {
    private static final int PERIODIC_SCAN_TIMEOUT_MS = 30000;
    private static final int PERIODIC_SINGLE_SCAN_INTERVAL_MS = 3000;
    private static final String TAG = "HiLinkController";
    private static WifiConnectivityManagerUtils wifiConnectivityManagerUtils = ((WifiConnectivityManagerUtils) EasyInvokeFactory.getInvokeUtils(WifiConnectivityManagerUtils.class));
    private static WifiStateMachineUtils wifiStateMachineUtils = ((WifiStateMachineUtils) EasyInvokeFactory.getInvokeUtils(WifiStateMachineUtils.class));
    private Context mContext = null;
    private boolean mIsHiLinkActive = false;
    private Handler mPeriodicScanHandler = null;
    private HandlerThread mPeriodicScanThread = null;
    private Runnable mPeriodicScanTimeoutRunnable = new Runnable() {
        public void run() {
            Log.d(HiLinkController.TAG, "mPeriodicScanTimeoutRunnable");
            HiLinkController.this.stopPeriodicScan();
        }
    };
    private Runnable mPeriodicSingleScanRunnable = new Runnable() {
        public void run() {
            Log.d(HiLinkController.TAG, "mPeriodicSingleScanRunnable");
            if (HiLinkController.this.mWifiConnectivityManager != null && HiLinkController.wifiConnectivityManagerUtils.getScreenOn(HiLinkController.this.mWifiConnectivityManager).booleanValue()) {
                HiLinkController.this.startPeriodicSingleScan();
            } else if (HiLinkController.this.mWifiConnectivityManager == null && HiLinkController.this.mPeriodicScanHandler != null && HiLinkController.this.mPeriodicScanThread.isAlive()) {
                HiLinkController.this.mPeriodicScanHandler.postDelayed(HiLinkController.this.mPeriodicSingleScanRunnable, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
            }
        }
    };
    private WifiConnectivityManager mWifiConnectivityManager = null;
    private WifiStateMachine mWifiStateMachine = null;

    public HiLinkController(Context context, WifiStateMachine wifiStateMachine) {
        this.mContext = context;
        this.mWifiStateMachine = wifiStateMachine;
        this.mWifiConnectivityManager = wifiStateMachineUtils.getWifiConnectivityManager(wifiStateMachine);
    }

    public boolean isHiLinkActive() {
        return this.mIsHiLinkActive;
    }

    public void enableHiLinkHandshake(boolean uiEnable, String bssid) {
        Log.d(TAG, "enableHiLinkHandshake: uiEnable");
        if (uiEnable) {
            this.mIsHiLinkActive = true;
            startPeriodicScan();
            return;
        }
        this.mIsHiLinkActive = false;
        stopPeriodicScan();
    }

    public void sendWpsOkcStartedBroadcast() {
        Log.d(TAG, "sendBroadcast: android.net.wifi.action.HILINK_STATE_CHANGED");
        this.mContext.sendBroadcast(new Intent("android.net.wifi.action.HILINK_STATE_CHANGED"), "com.android.server.wifi.permission.HILINK_STATE_CHANGED");
        Log.d(TAG, "report HiLink connect action. EventId:400 ret:" + Flog.bdReport(this.mContext, HwCHRWifiSpeedBaseChecker.RTT_THRESHOLD_400));
    }

    public void saveWpsOkcConfiguration(int connectionNetId, String connectionBssid, List<ScanResult> scanResults) {
        ScanResult connectionScanResult = null;
        for (ScanResult result : scanResults) {
            if (Objects.equals(connectionBssid, result.BSSID)) {
                connectionScanResult = result;
                break;
            }
        }
        if (connectionScanResult == null || !connectionScanResult.isHiLinkNetwork) {
            Log.d(TAG, "saveWpsOkcConfiguration: return");
            return;
        }
        Log.d(TAG, "saveWpsOkcConfiguration: enter");
        WifiConfiguration config = new WifiConfiguration();
        int netId = connectionNetId;
        config.networkId = connectionNetId;
        config.SSID = connectionScanResult.SSID;
        config.BSSID = connectionBssid;
        config.isHiLinkNetwork = true;
        String configKey = config.configKey(true);
        WifiConfigManager wifiConfigManager = wifiStateMachineUtils.getWifiConfigManager(this.mWifiStateMachine);
        WifiConfiguration savedConfig = wifiConfigManager.getWifiConfiguration(configKey);
        if (savedConfig != null) {
            config = savedConfig;
            savedConfig.ephemeral = false;
            wifiConfigManager.updateNetworkSelectionStatus(savedConfig, 0);
        }
        netId = wifiConfigManager.saveNetwork(config, Process.myUid()).getNetworkId();
        this.mWifiStateMachine.saveConnectingNetwork(wifiConfigManager.getWifiConfiguration(netId));
        wifiConfigManager.setAndEnableLastSelectedConfiguration(netId);
    }

    private synchronized void startPeriodicScan() {
        Log.d(TAG, "startPeriodicScan.");
        stopPeriodicScan();
        this.mPeriodicScanThread = new HandlerThread("PeriodicScanThread");
        this.mPeriodicScanThread.start();
        this.mPeriodicScanHandler = new Handler(this.mPeriodicScanThread.getLooper());
        this.mPeriodicScanHandler.postDelayed(this.mPeriodicScanTimeoutRunnable, 30000);
        startPeriodicSingleScan();
    }

    private synchronized void stopPeriodicScan() {
        Log.d(TAG, "stopPeriodicScan.");
        if (this.mPeriodicScanHandler != null) {
            this.mPeriodicScanHandler.removeCallbacks(this.mPeriodicSingleScanRunnable);
            this.mPeriodicScanHandler.removeCallbacks(this.mPeriodicScanTimeoutRunnable);
            this.mPeriodicScanHandler = null;
        }
        if (this.mPeriodicScanThread != null) {
            this.mPeriodicScanThread.quit();
            this.mPeriodicScanThread = null;
        }
    }

    private void startPeriodicSingleScan() {
        Log.d(TAG, "startPeriodicSingleScan");
        this.mWifiConnectivityManager = wifiStateMachineUtils.getWifiConnectivityManager(this.mWifiStateMachine);
        if (this.mWifiConnectivityManager != null) {
            int wifiState = wifiConnectivityManagerUtils.getWifiState(this.mWifiConnectivityManager);
            WifiInfo wifiInfo = wifiConnectivityManagerUtils.getWifiInfo(this.mWifiConnectivityManager);
            boolean isFullBandScan = true;
            if (wifiState == 1 && (wifiInfo.txSuccessRate > 8.0d || wifiInfo.rxSuccessRate > 16.0d)) {
                isFullBandScan = false;
            }
            Log.d(TAG, "startSingleScan");
            wifiConnectivityManagerUtils.startSingleScan(this.mWifiConnectivityManager, false, isFullBandScan);
        }
        if (this.mPeriodicScanHandler != null && this.mPeriodicScanThread.isAlive()) {
            this.mPeriodicScanHandler.postDelayed(this.mPeriodicSingleScanRunnable, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
        }
    }
}
