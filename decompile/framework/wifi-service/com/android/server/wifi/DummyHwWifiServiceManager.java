package com.android.server.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiScanner;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.UserManager;
import android.security.KeyStore;
import android.util.LocalLog;
import com.android.server.wifi.SoftApManager.Listener;
import com.android.server.wifi.WifiServiceImpl.LockList;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import java.util.ArrayList;

public class DummyHwWifiServiceManager implements HwWifiServiceManager {
    private static HwWifiServiceManager mInstance = new DummyHwWifiServiceManager();
    private SoftApManager mSoftApManager;

    public WifiServiceImpl createHwWifiService(Context context) {
        return new WifiServiceImpl(context);
    }

    public WifiController createHwWifiController(Context context, WifiStateMachine wsm, WifiSettingsStore wss, LockList locks, Looper looper, FrameworkFacade f) {
        return new WifiController(context, wsm, wss, locks, looper, f);
    }

    public static HwWifiServiceManager getDefault() {
        return mInstance;
    }

    public boolean custApConfiguration(WifiApConfigStore s, WifiConfiguration config, Context context) {
        return false;
    }

    public boolean autoConnectByMode(Message message) {
        return false;
    }

    public WifiP2pServiceImpl createHwWifiP2pService(Context context) {
        return null;
    }

    public WifiStateMachine createHwWifiStateMachine(Context context, FrameworkFacade facade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode countryCode) {
        return new WifiStateMachine(context, facade, looper, userManager, wifiInjector, backupManagerProxy, countryCode);
    }

    public String getAppendSsidWithRandomUuid(WifiConfiguration config, Context context) {
        return null;
    }

    public String getCustWifiApDefaultName(WifiConfiguration config) {
        return null;
    }

    public void createHwArpVerifier(Context context) {
    }

    public WifiQualifiedNetworkSelector createHwWifiQualifiedNetworkSelector(WifiConfigManager configureStore, Context context, WifiInfo wifiInfo, Clock clock, WifiStateMachine wsm, WifiNative wifiNative) {
        return null;
    }

    public WifiStateMachine getGlobalHwWifiStateMachine() {
        return null;
    }

    public WifiConfigManager createHwWifiConfigManager(Context context, WifiNative wifiNative, FrameworkFacade frameworkFacade, Clock clock, UserManager userManager, KeyStore keyStore) {
        return new WifiConfigManager(context, wifiNative, frameworkFacade, clock, userManager, keyStore);
    }

    public WifiCountryCode createHwWifiCountryCode(Context context, WifiNative wifiNative, String oemDefaultCountryCode, String persistentCountryCode, boolean revertCountryCodeOnCellularLoss) {
        return new WifiCountryCode(wifiNative, oemDefaultCountryCode, persistentCountryCode, revertCountryCodeOnCellularLoss);
    }

    public WifiConfigStore createHwWifiConfigStore(WifiNative wifiNative, KeyStore keyStore, LocalLog localLog, boolean showNetworks, boolean verboseDebug) {
        return null;
    }

    public SoftApManager createHwSoftApManager(Context context, Looper looper, WifiNative wifiNative, INetworkManagementService nmService, ConnectivityManager connectivityManager, String countryCode, ArrayList<Integer> allowed2GChannels, Listener listener) {
        if (this.mSoftApManager == null) {
            this.mSoftApManager = new SoftApManager(context, looper, wifiNative, nmService, connectivityManager, countryCode, allowed2GChannels, listener);
        }
        this.mSoftApManager.setCountryCode(countryCode);
        return this.mSoftApManager;
    }

    public WifiConnectivityManager createHwWifiConnectivityManager(Context context, WifiStateMachine stateMachine, WifiScanner scanner, WifiConfigManager configManager, WifiInfo wifiInfo, WifiQualifiedNetworkSelector qualifiedNetworkSelector, WifiInjector wifiInjector, Looper looper) {
        return null;
    }
}
