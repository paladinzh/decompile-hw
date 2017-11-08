package com.android.server.wifi;

import android.security.KeyStore;

public class WifiInjector {
    private final BuildProperties mBuildProperties = new SystemBuildProperties();
    private final Clock mClock = new Clock();
    private final KeyStore mKeyStore = KeyStore.getInstance();
    private final PropertyService mPropertyService = new SystemPropertyService();
    private final WifiLastResortWatchdog mWifiLastResortWatchdog = new WifiLastResortWatchdog(this.mWifiMetrics);
    private final WifiMetrics mWifiMetrics = new WifiMetrics(this.mClock);

    private static class LazyHolder {
        public static final WifiInjector sInstance = new WifiInjector();

        private LazyHolder() {
        }
    }

    public static WifiInjector getInstance() {
        return LazyHolder.sInstance;
    }

    public WifiMetrics getWifiMetrics() {
        return this.mWifiMetrics;
    }

    public WifiLastResortWatchdog getWifiLastResortWatchdog() {
        return this.mWifiLastResortWatchdog;
    }

    public Clock getClock() {
        return this.mClock;
    }

    public PropertyService getPropertyService() {
        return this.mPropertyService;
    }

    public BuildProperties getBuildProperties() {
        return this.mBuildProperties;
    }

    public KeyStore getKeyStore() {
        return this.mKeyStore;
    }
}
