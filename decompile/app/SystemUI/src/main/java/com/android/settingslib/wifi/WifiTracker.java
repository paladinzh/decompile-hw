package com.android.settingslib.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Builder;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.settingslib.R$string;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public class WifiTracker {
    public static int sVerboseLogging = 0;
    private ArrayList<AccessPoint> mAccessPoints;
    private final AtomicBoolean mConnected;
    private final ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private final IntentFilter mFilter;
    private final boolean mIncludePasspoints;
    private final boolean mIncludeSaved;
    private final boolean mIncludeScans;
    private WifiInfo mLastInfo;
    private NetworkInfo mLastNetworkInfo;
    private final WifiListener mListener;
    private WifiListenerEx mListenerEx;
    private final MainHandler mMainHandler;
    private WifiTrackerNetworkCallback mNetworkCallback;
    private final NetworkRequest mNetworkRequest;
    final BroadcastReceiver mReceiver;
    private boolean mRegistered;
    private boolean mSavedNetworksExist;
    private Integer mScanId;
    private HashMap<String, ScanResult> mScanResultCache;
    Scanner mScanner;
    private HashMap<String, Integer> mSeenBssids;
    private final WifiManager mWifiManager;
    private final WorkHandler mWorkHandler;

    private final class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (WifiTracker.this.mListener != null) {
                switch (msg.what) {
                    case 0:
                        WifiTracker.this.mListener.onConnectedChanged();
                        break;
                    case 1:
                        WifiTracker.this.mListener.onWifiStateChanged(msg.arg1);
                        break;
                    case 2:
                        WifiTracker.this.mListener.onAccessPointsChanged();
                        break;
                    case 3:
                        if (WifiTracker.this.mScanner != null) {
                            WifiTracker.this.mScanner.resume();
                            break;
                        }
                        break;
                    case 4:
                        if (WifiTracker.this.mScanner != null) {
                            WifiTracker.this.mScanner.pause();
                            break;
                        }
                        break;
                    case 301:
                        if (WifiTracker.this.mListenerEx != null) {
                            WifiTracker.this.mListenerEx.onP2pEnabledStateChanged();
                            break;
                        }
                        return;
                }
            }
        }
    }

    private static class Multimap<K, V> {
        private final HashMap<K, List<V>> store;

        private Multimap() {
            this.store = new HashMap();
        }

        List<V> getAll(K key) {
            List<V> values = (List) this.store.get(key);
            return values != null ? values : Collections.emptyList();
        }

        void put(K key, V val) {
            List<V> curVals = (List) this.store.get(key);
            if (curVals == null) {
                curVals = new ArrayList(3);
                this.store.put(key, curVals);
            }
            curVals.add(val);
        }
    }

    class Scanner extends Handler {
        private int mRetry = 0;

        Scanner() {
        }

        void resume() {
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }

        void forceScan() {
            removeMessages(0);
            sendEmptyMessage(0);
        }

        void pause() {
            this.mRetry = 0;
            removeMessages(0);
            if (WifiTracker.this.mListenerEx != null) {
                WifiTracker.this.mListenerEx.onProgressChanged(false);
            }
        }

        boolean isScanning() {
            return hasMessages(0);
        }

        public void handleMessage(Message message) {
            if (WifiTracker.this.mListenerEx != null) {
                WifiTracker.this.mListenerEx.onProgressChanged(false);
            }
            if (message.what == 0) {
                if (WifiTracker.this.mWifiManager.startScan()) {
                    this.mRetry = 0;
                    if (WifiTracker.this.mListenerEx != null) {
                        WifiTracker.this.mListenerEx.onProgressChanged(true);
                    }
                } else {
                    int i = this.mRetry + 1;
                    this.mRetry = i;
                    if (i >= 3) {
                        this.mRetry = 0;
                        if (WifiTracker.this.mContext != null) {
                            Toast.makeText(WifiTracker.this.mContext, R$string.wifi_fail_to_scan, 1).show();
                        }
                        return;
                    }
                }
                sendEmptyMessageDelayed(0, 10000);
            }
        }
    }

    public interface WifiListener {
        void onAccessPointsChanged();

        void onConnectedChanged();

        void onWifiStateChanged(int i);
    }

    public interface WifiListenerEx {
        void onP2pEnabledStateChanged();

        void onProgressChanged(boolean z);
    }

    private final class WifiTrackerNetworkCallback extends NetworkCallback {
        private WifiTrackerNetworkCallback() {
        }

        public void onCapabilitiesChanged(Network network, NetworkCapabilities nc) {
            if (network.equals(WifiTracker.this.mWifiManager.getCurrentNetwork())) {
                WifiTracker.this.mWorkHandler.sendEmptyMessage(1);
            }
        }
    }

    private final class WorkHandler extends Handler {
        public WorkHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    WifiTracker.this.updateAccessPoints();
                    return;
                case 1:
                    WifiTracker.this.updateNetworkInfo((NetworkInfo) msg.obj);
                    return;
                case 2:
                    WifiTracker.this.handleResume();
                    return;
                case 3:
                    if (msg.arg1 != 3) {
                        WifiTracker.this.mLastInfo = null;
                        WifiTracker.this.mLastNetworkInfo = null;
                        if (WifiTracker.this.mScanner != null) {
                            WifiTracker.this.mScanner.pause();
                        }
                    } else if (WifiTracker.this.mScanner != null) {
                        WifiTracker.this.mScanner.resume();
                    }
                    WifiTracker.this.mMainHandler.obtainMessage(1, msg.arg1, 0).sendToTarget();
                    return;
                default:
                    return;
            }
        }
    }

    public WifiTracker(Context context, WifiListener wifiListener, Looper workerLooper, boolean includeSaved, boolean includeScans) {
        this(context, wifiListener, workerLooper, includeSaved, includeScans, false);
    }

    public WifiTracker(Context context, WifiListener wifiListener, Looper workerLooper, boolean includeSaved, boolean includeScans, boolean includePasspoints) {
        this(context, wifiListener, workerLooper, includeSaved, includeScans, includePasspoints, (WifiManager) context.getSystemService(WifiManager.class), (ConnectivityManager) context.getSystemService(ConnectivityManager.class), Looper.myLooper());
    }

    WifiTracker(Context context, WifiListener wifiListener, Looper workerLooper, boolean includeSaved, boolean includeScans, boolean includePasspoints, WifiManager wifiManager, ConnectivityManager connectivityManager, Looper currentLooper) {
        this.mConnected = new AtomicBoolean(false);
        this.mAccessPoints = new ArrayList();
        this.mSeenBssids = new HashMap();
        this.mScanResultCache = new HashMap();
        this.mScanId = Integer.valueOf(0);
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                    WifiTracker.this.updateWifiState(intent.getIntExtra("wifi_state", 4));
                } else if ("android.net.wifi.SCAN_RESULTS".equals(action) || "android.net.wifi.CONFIGURED_NETWORKS_CHANGE".equals(action) || "android.net.wifi.LINK_CONFIGURATION_CHANGED".equals(action)) {
                    WifiTracker.this.mWorkHandler.sendEmptyMessage(0);
                    if (WifiTracker.this.mListenerEx != null && "android.net.wifi.SCAN_RESULTS".equals(action)) {
                        WifiTracker.this.mListenerEx.onProgressChanged(false);
                    }
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    WifiTracker.this.mConnected.set(info.isConnected());
                    WifiTracker.this.mMainHandler.sendEmptyMessage(0);
                    WifiTracker.this.mWorkHandler.sendEmptyMessage(0);
                    WifiTracker.this.mWorkHandler.obtainMessage(1, info).sendToTarget();
                } else if ("android.net.wifi.p2p.ENABLE_CHANGED".equals(action)) {
                    WifiTracker.this.mMainHandler.sendEmptyMessage(301);
                }
            }
        };
        if (includeSaved || includeScans) {
            this.mContext = context;
            if (currentLooper == null) {
                currentLooper = Looper.getMainLooper();
            }
            this.mMainHandler = new MainHandler(currentLooper);
            if (workerLooper == null) {
                workerLooper = currentLooper;
            }
            this.mWorkHandler = new WorkHandler(workerLooper);
            this.mWifiManager = wifiManager;
            this.mIncludeSaved = includeSaved;
            this.mIncludeScans = includeScans;
            this.mIncludePasspoints = includePasspoints;
            this.mListener = wifiListener;
            this.mConnectivityManager = connectivityManager;
            sVerboseLogging = this.mWifiManager.getVerboseLoggingLevel();
            prepareWifiInfo();
            this.mFilter = new IntentFilter();
            this.mFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            this.mFilter.addAction("android.net.wifi.SCAN_RESULTS");
            this.mFilter.addAction("android.net.wifi.NETWORK_IDS_CHANGED");
            this.mFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
            this.mFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
            this.mFilter.addAction("android.net.wifi.LINK_CONFIGURATION_CHANGED");
            this.mFilter.addAction("android.net.wifi.STATE_CHANGE");
            this.mNetworkRequest = new Builder().clearCapabilities().addTransportType(1).build();
            this.mFilter.addAction("android.net.wifi.p2p.ENABLE_CHANGED");
            return;
        }
        throw new IllegalArgumentException("Must include either saved or scans");
    }

    public void forceScan() {
        if (this.mWifiManager.isWifiEnabled() && this.mScanner != null) {
            this.mScanner.forceScan();
        }
    }

    public void pauseScanning() {
        if (this.mScanner != null) {
            this.mScanner.pause();
            this.mScanner = null;
        }
    }

    public void resumeScanning() {
        if (this.mScanner == null) {
            this.mScanner = new Scanner();
        }
        this.mWorkHandler.sendEmptyMessage(2);
        if (this.mWifiManager.isWifiEnabled()) {
            this.mScanner.resume();
        }
        this.mWorkHandler.sendEmptyMessage(0);
    }

    public void startTracking() {
        resumeScanning();
        if (!this.mRegistered) {
            this.mContext.registerReceiver(this.mReceiver, this.mFilter);
            this.mNetworkCallback = new WifiTrackerNetworkCallback();
            this.mConnectivityManager.registerNetworkCallback(this.mNetworkRequest, this.mNetworkCallback);
            this.mRegistered = true;
        }
    }

    public void stopTracking() {
        if (this.mRegistered) {
            this.mWorkHandler.removeMessages(0);
            this.mWorkHandler.removeMessages(1);
            this.mWorkHandler.removeMessages(3);
            this.mMainHandler.removeMessages(0);
            this.mMainHandler.removeMessages(1);
            this.mMainHandler.removeMessages(2);
            this.mMainHandler.removeMessages(3);
            this.mMainHandler.removeMessages(4);
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mConnectivityManager.unregisterNetworkCallback(this.mNetworkCallback);
            this.mRegistered = false;
        }
        pauseScanning();
    }

    public List<AccessPoint> getAccessPoints() {
        List arrayList;
        synchronized (this.mAccessPoints) {
            arrayList = new ArrayList(this.mAccessPoints);
        }
        return arrayList;
    }

    public WifiManager getManager() {
        return this.mWifiManager;
    }

    public void dump(PrintWriter pw) {
        pw.println("  - wifi tracker ------");
        for (AccessPoint accessPoint : getAccessPoints()) {
            pw.println("  " + accessPoint);
        }
    }

    private void handleResume() {
        this.mScanResultCache.clear();
        this.mSeenBssids.clear();
        this.mScanId = Integer.valueOf(0);
    }

    private Collection<ScanResult> fetchScanResults() {
        this.mScanId = Integer.valueOf(this.mScanId.intValue() + 1);
        List<ScanResult> newResults = this.mWifiManager.getScanResults();
        if (newResults == null) {
            return this.mScanResultCache.values();
        }
        int huaweiEmployeeCount = 0;
        for (ScanResult newResult : newResults) {
            if (!TextUtils.isEmpty(newResult.SSID)) {
                this.mScanResultCache.put(newResult.BSSID, newResult);
                this.mSeenBssids.put(newResult.BSSID, this.mScanId);
                if ("Huawei-Employee".equals(newResult.SSID)) {
                    huaweiEmployeeCount++;
                }
            }
        }
        if (huaweiEmployeeCount > 0) {
            Log.d("WifiTracker", "huaweiEmployeeCount:" + huaweiEmployeeCount);
        }
        if (this.mScanId.intValue() > 3) {
            Integer threshold = Integer.valueOf(this.mScanId.intValue() - 3);
            int newResultsSize = newResults.size();
            Iterator<Entry<String, Integer>> it = this.mSeenBssids.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, Integer> e = (Entry) it.next();
                if (((Integer) e.getValue()).intValue() < threshold.intValue()) {
                    ScanResult result = (ScanResult) this.mScanResultCache.get(e.getKey());
                    if (!(newResultsSize <= 0 || result == null || AccessPoint.getSecurity(result) == 3)) {
                        Log.d("WifiTracker", "Removing " + ((String) e.getKey()) + ":(" + result.SSID + ")");
                    }
                    this.mScanResultCache.remove(e.getKey());
                    it.remove();
                }
            }
        }
        return this.mScanResultCache.values();
    }

    private WifiConfiguration getWifiConfigurationForNetworkId(int networkId) {
        List<WifiConfiguration> configs = this.mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                if (this.mLastInfo != null && networkId == config.networkId) {
                    if (!config.selfAdded || config.numAssociation != 0) {
                        return config;
                    }
                }
            }
        }
        return null;
    }

    private void updateAccessPoints() {
        List<AccessPoint> cachedAccessPoints = getAccessPoints();
        ArrayList<AccessPoint> accessPoints = new ArrayList();
        Multimap<String, AccessPoint> apMap = new Multimap();
        WifiConfiguration connectionConfig = null;
        if (this.mLastInfo != null) {
            connectionConfig = getWifiConfigurationForNetworkId(this.mLastInfo.getNetworkId());
        }
        Collection<ScanResult> results = fetchScanResults();
        try {
            AccessPoint accessPoint;
            WifiConfiguration config;
            boolean found;
            List<WifiConfiguration> configs = this.mWifiManager.getConfiguredNetworks();
            for (AccessPoint accessPoint2 : cachedAccessPoints) {
                accessPoint2.clearConfig();
            }
            if (configs != null) {
                this.mSavedNetworksExist = configs.size() != 0;
                for (WifiConfiguration config2 : configs) {
                    if (!config2.selfAdded || config2.numAssociation != 0) {
                        accessPoint2 = getCachedOrCreate(config2, (List) cachedAccessPoints);
                        accessPoint2.setFoundInScanResult(false);
                        if (!(this.mLastInfo == null || this.mLastNetworkInfo == null || config2.isPasspoint())) {
                            accessPoint2.update(connectionConfig, this.mLastInfo, this.mLastNetworkInfo);
                        }
                        if (this.mIncludeSaved || showPresetAccessPoints(accessPoint2)) {
                            if (!config2.isPasspoint() || this.mIncludePasspoints || showPresetAccessPoints(accessPoint2)) {
                                boolean apFound = false;
                                for (ScanResult result : results) {
                                    if (result.SSID.equals(accessPoint2.getSsidStr())) {
                                        apFound = true;
                                        break;
                                    }
                                }
                                if (!apFound) {
                                    accessPoint2.setRssi(Integer.MAX_VALUE);
                                }
                                accessPoints.add(accessPoint2);
                            }
                            if (!config2.isPasspoint()) {
                                apMap.put(accessPoint2.getSsidStr(), accessPoint2);
                            }
                        } else {
                            cachedAccessPoints.add(accessPoint2);
                        }
                    }
                }
            }
            if (results != null) {
                for (ScanResult result2 : results) {
                    if (!(result2.SSID == null || result2.SSID.length() == 0 || result2.capabilities.contains("[IBSS]"))) {
                        found = false;
                        for (AccessPoint accessPoint22 : apMap.getAll(result2.SSID)) {
                            if (accessPoint22.update(result2)) {
                                accessPoint22.setFoundInScanResult(true);
                                found = true;
                                break;
                            }
                        }
                        if (!found && this.mIncludeScans) {
                            accessPoint22 = getCachedOrCreate(result2, (List) cachedAccessPoints);
                            if (!(this.mLastInfo == null || this.mLastNetworkInfo == null)) {
                                accessPoint22.update(connectionConfig, this.mLastInfo, this.mLastNetworkInfo);
                            }
                            if (result2.isPasspointNetwork()) {
                                config2 = this.mWifiManager.getMatchingWifiConfig(result2);
                                if (config2 != null) {
                                    accessPoint22.update(config2);
                                }
                            }
                            if (!(this.mLastInfo == null || this.mLastInfo.getBSSID() == null || !this.mLastInfo.getBSSID().equals(result2.BSSID) || connectionConfig == null || !connectionConfig.isPasspoint())) {
                                accessPoint22.update(connectionConfig);
                            }
                            accessPoints.add(accessPoint22);
                            apMap.put(accessPoint22.getSsidStr(), accessPoint22);
                        }
                    }
                }
            }
            Collections.sort(accessPoints);
            for (AccessPoint prevAccessPoint : this.mAccessPoints) {
                if (prevAccessPoint.getSsid() != null) {
                    String prevSsid = prevAccessPoint.getSsidStr();
                    found = false;
                    for (AccessPoint newAccessPoint : accessPoints) {
                        if (newAccessPoint.getSsid() != null && newAccessPoint.getSsid().equals(prevSsid)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found && "Huawei-Employee".equals(prevSsid)) {
                        Log.d("WifiTracker", "Did not find " + prevSsid + " in this scan");
                    }
                }
            }
            this.mAccessPoints = accessPoints;
            this.mMainHandler.sendEmptyMessage(2);
        } catch (RuntimeException e) {
            Log.e("WifiTracker", e.getMessage());
        }
    }

    private AccessPoint getCachedOrCreate(ScanResult result, List<AccessPoint> cache) {
        int N = cache.size();
        for (int i = 0; i < N; i++) {
            if (((AccessPoint) cache.get(i)).matches(result)) {
                AccessPoint ret = (AccessPoint) cache.remove(i);
                ret.update(result);
                return ret;
            }
        }
        return new AccessPoint(this.mContext, result);
    }

    private AccessPoint getCachedOrCreate(WifiConfiguration config, List<AccessPoint> cache) {
        int N = cache.size();
        for (int i = 0; i < N; i++) {
            if (((AccessPoint) cache.get(i)).matches(config)) {
                AccessPoint ret = (AccessPoint) cache.remove(i);
                ret.loadConfig(config);
                return ret;
            }
        }
        return new AccessPoint(this.mContext, config);
    }

    private void updateNetworkInfo(NetworkInfo networkInfo) {
        if (this.mWifiManager.isWifiEnabled()) {
            if (networkInfo == null || networkInfo.getDetailedState() != DetailedState.OBTAINING_IPADDR) {
                this.mMainHandler.sendEmptyMessage(3);
            } else {
                this.mMainHandler.sendEmptyMessage(4);
            }
            if (networkInfo != null) {
                this.mLastNetworkInfo = networkInfo;
            }
            WifiConfiguration connectionConfig = null;
            this.mLastInfo = this.mWifiManager.getConnectionInfo();
            if (this.mLastInfo != null) {
                connectionConfig = getWifiConfigurationForNetworkId(this.mLastInfo.getNetworkId());
            }
            boolean reorder = false;
            for (int i = this.mAccessPoints.size() - 1; i >= 0; i--) {
                if (((AccessPoint) this.mAccessPoints.get(i)).update(connectionConfig, this.mLastInfo, this.mLastNetworkInfo)) {
                    reorder = true;
                }
            }
            if (reorder) {
                synchronized (this.mAccessPoints) {
                    Collections.sort(this.mAccessPoints);
                }
                this.mMainHandler.sendEmptyMessage(2);
            }
            return;
        }
        this.mMainHandler.sendEmptyMessage(4);
    }

    private void updateWifiState(int state) {
        this.mWorkHandler.obtainMessage(3, state, 0).sendToTarget();
    }

    public boolean showPresetAccessPoints(AccessPoint accessPoint) {
        String wifiNotdelNotedit = System.getString(this.mContext.getContentResolver(), "wifi_notdel_notedit");
        if (TextUtils.isEmpty(wifiNotdelNotedit) || accessPoint == null) {
            return false;
        }
        for (String ssid : wifiNotdelNotedit.split(";")) {
            if (ssid.equals(accessPoint.ssid)) {
                return true;
            }
        }
        return false;
    }

    public void prepareWifiInfo() {
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            this.mLastInfo = wifiInfo;
        }
        ConnectivityManager connectivity = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (connectivity != null) {
            NetworkInfo activeNetworkInfo = connectivity.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                this.mLastNetworkInfo = activeNetworkInfo;
            }
        }
    }
}
