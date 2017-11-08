package com.android.server.wifi;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.DhcpResults;
import android.net.IpConfiguration.IpAssignment;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.os.Binder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.HwNetworkPropertyChecker;
import com.android.server.wifi.wifipro.PortalAutoFillManager;
import com.android.server.wifi.wifipro.WifiHandover;
import com.android.server.wifi.wifipro.WifiProStateMachine;
import com.android.server.wifipro.PortalDataBaseManager;
import com.android.server.wifipro.WifiProCommonUtils;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwSelfCureEngine extends StateMachine {
    private static final int CMD_AUTO_CONN_FAILED_CURE = 102;
    private static final int CMD_AUTO_CONN_FAILED_DETECT = 101;
    private static final int CMD_CONFIGURED_NETWORK_DELETED = 107;
    private static final int CMD_CURE_CONNECTED_TIMEOUT = 103;
    private static final int CMD_DHCP_OFFER_PKT_RCV = 126;
    private static final int CMD_DHCP_RESULTS_UPDATE = 125;
    private static final int CMD_DNS_FAILED_MONITOR = 123;
    private static final int CMD_GATEWAY_CHANGED_DETECT = 115;
    private static final int CMD_INTERNET_FAILED_SELF_CURE = 112;
    private static final int CMD_INTERNET_FAILURE_DETECTED = 122;
    private static final int CMD_INTERNET_RECOVERY_CONFIRM = 113;
    private static final int CMD_INTERNET_STATUS_DETECT = 111;
    private static final int CMD_IP_CONFIG_COMPLETED = 117;
    private static final int CMD_IP_CONFIG_LOST_EVENT = 124;
    private static final int CMD_IP_CONFIG_TIMEOUT = 116;
    private static final int CMD_NETWORK_CONNECTED_RCVD = 104;
    private static final int CMD_NETWORK_DISCONNECTED_RCVD = 108;
    private static final int CMD_NETWORK_ROAMING_DETECT = 110;
    private static final int CMD_NEW_RSSI_RCVD = 109;
    private static final int CMD_NEW_SCAN_RESULTS_RCV = 127;
    private static final int CMD_NO_TCP_RX_DETECTED = 121;
    private static final int CMD_P2P_DISCONNECTED_EVENT = 128;
    private static final int CMD_RESETUP_SELF_CURE_MONITOR = 118;
    private static final int CMD_SELF_CURE_WIFI_FAILED = 120;
    private static final int CMD_SELF_CURE_WIFI_LINK = 114;
    private static final int CMD_UPDATE_CONN_SELF_CURE_HISTORY = 119;
    private static final int CMD_WIFI_DISABLED_RCVD = 105;
    private static final int CMD_WIFI_ENABLED_RCVD = 106;
    private static final int CURE_OUT_OF_DATE_MS = 7200000;
    private static final int DHCP_RENEW_TIMEOUT_MS = 8000;
    private static final int DNS_UPDATE_CONFIRM_DELAYED_MS = 1000;
    private static final int HANDLE_WIFI_ON_DELAYED_MS = 1000;
    private static final int INTERNET_DETECT_INTERVAL_MS = 6000;
    private static final int INTERNET_FAILED_TYPE_DNS = 303;
    private static final int INTERNET_FAILED_TYPE_GATEWAY = 302;
    private static final int INTERNET_FAILED_TYPE_ROAMING = 301;
    private static final int INTERNET_FAILED_TYPE_TCP = 304;
    private static final int INTERNET_OK = 300;
    private static final int IP_CONFIG_CONFIRM_DELAYED_MS = 2000;
    private static final int REQ_HTTP_DELAYED_MS = 2000;
    private static final int SELF_CURE_DELAYED_MS = 1000;
    private static final int SELF_CURE_MONITOR_DELAYED_MS = 2000;
    private static final int SELF_CURE_TIMEOUT_MS = 20000;
    private static final int SET_STATIC_IP_TIMEOUT_MS = 3000;
    private static final String TAG = "HwSelfCureEngine";
    private static HwSelfCureEngine mHwSelfCureEngine = null;
    private Map<String, WifiConfiguration> autoConnectFailedNetworks = new HashMap();
    private Map<String, Integer> autoConnectFailedNetworksRssi = new HashMap();
    private State mConnectedMonitorState = new ConnectedMonitorState();
    private long mConnectedTimeMills;
    private State mConnectionSelfCureState = new ConnectionSelfCureState();
    private Context mContext;
    private State mDefaultState = new DefaultState();
    private Map<String, String> mDhcpOfferPackets = new HashMap();
    private ArrayList<String> mDhcpResultsTestDone = new ArrayList();
    private State mDisconnectedMonitorState = new DisconnectedMonitorState();
    private HwWifiStatStore mHwWifiStatStore;
    private boolean mInitialized = false;
    private State mInternetSelfCureState = new InternetSelfCureState();
    private boolean mInternetUnknown = false;
    private int mIpConfigLostCnt = 0;
    private AtomicBoolean mIsWifiBackground = new AtomicBoolean(false);
    private HwNetworkPropertyChecker mNetworkChecker;
    private WifiConfiguration mNoAutoConnConfig;
    private int mNoAutoConnCounter = 0;
    private int mNoAutoConnReason = -1;
    private int mNoTcpRxCounter = 0;
    private AtomicBoolean mP2pConnected = new AtomicBoolean(false);
    private WifiConfiguration mSelfCureConfig;
    private AtomicBoolean mSelfCureOngoing = new AtomicBoolean(false);
    private boolean mStaticIpCureSuccess = false;
    private WifiManager mWifiManager;
    private WifiStateMachine mWifiStateMachine;
    private Map<String, CureFailedNetworkInfo> networkCureFailedHistory = new HashMap();

    class ConnectedMonitorState extends State {
        private int mConfigAuthType = -1;
        private int mDnsConsecutiveFailedCounter;
        private boolean mHasInternetRecently;
        private boolean mIpv6DnsEnabled;
        private String mLastConnectedBssid;
        private int mLastDnsFailedCounter;
        private int mLastSignalLevel;
        private boolean mMobileHotspot;
        private boolean mPortalUnthenEver;
        private boolean mUserSetStaticIpConfig;
        private boolean mWifiSwitchAllowed;

        ConnectedMonitorState() {
        }

        public void enter() {
            HwSelfCureEngine.this.LOGD("==> ##ConnectedMonitorState");
            this.mLastConnectedBssid = HwSelfCureEngine.this.mWifiStateMachine.getCurrentBSSID();
            this.mLastDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
            this.mDnsConsecutiveFailedCounter = 0;
            HwSelfCureEngine.this.mNoTcpRxCounter = 0;
            this.mLastSignalLevel = 0;
            this.mHasInternetRecently = false;
            this.mPortalUnthenEver = false;
            HwSelfCureEngine.this.mInternetUnknown = false;
            this.mUserSetStaticIpConfig = false;
            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
            this.mIpv6DnsEnabled = true;
            this.mWifiSwitchAllowed = false;
            this.mMobileHotspot = HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(HwSelfCureEngine.this.mContext);
            WifiInfo wifiInfo = HwSelfCureEngine.this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                this.mLastSignalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(wifiInfo.getRssi());
                HwSelfCureEngine.this.LOGD("ConnectedMonitorState, network = " + wifiInfo.getSSID() + ", signal = " + this.mLastSignalLevel + ", mobileHotspot = " + this.mMobileHotspot);
            }
            if (!HwSelfCureEngine.this.mIsWifiBackground.get() && !setupSelfCureMonitor()) {
                HwSelfCureEngine.this.LOGD("ConnectedMonitorState, config is null when connected broadcast received, delay to setup again.");
                HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_RESETUP_SELF_CURE_MONITOR, 2000);
            }
        }

        public boolean processMessage(Message message) {
            String newBssid = null;
            switch (message.what) {
                case 104:
                    HwSelfCureEngine.this.LOGD("ConnectedMonitorState, CMD_NETWORK_CONNECTED_RCVD!");
                    HwSelfCureEngine.this.mIsWifiBackground.set(false);
                    break;
                case HwSelfCureEngine.CMD_NETWORK_DISCONNECTED_RCVD /*108*/:
                    if (HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_GATEWAY_CHANGED_DETECT)) {
                        HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_GATEWAY_CHANGED_DETECT);
                    }
                    if (HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_RESETUP_SELF_CURE_MONITOR)) {
                        HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_RESETUP_SELF_CURE_MONITOR);
                    }
                    HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mDisconnectedMonitorState);
                    break;
                case HwSelfCureEngine.CMD_NEW_RSSI_RCVD /*109*/:
                    this.mLastSignalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(message.arg1);
                    break;
                case 110:
                    if (!HwSelfCureEngine.this.mIsWifiBackground.get()) {
                        if (message.obj != null) {
                            newBssid = (String) message.obj;
                        }
                        if (newBssid == null || !newBssid.equals(this.mLastConnectedBssid)) {
                            if (!this.mUserSetStaticIpConfig) {
                                updateInternetAccessHistory();
                                if (!this.mHasInternetRecently && !this.mPortalUnthenEver && !HwSelfCureEngine.this.mInternetUnknown) {
                                    HwSelfCureEngine.this.LOGD("CMD_NETWORK_ROAMING_DETECT rcvd, but no internet access always.");
                                    break;
                                }
                                int isDnsReachable;
                                if (HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_GATEWAY_CHANGED_DETECT)) {
                                    HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_GATEWAY_CHANGED_DETECT);
                                }
                                this.mLastConnectedBssid = newBssid;
                                int cnt1 = HwSelfCureUtils.getCurrentDnsFailedCounter();
                                synchronized (HwSelfCureEngine.this.mNetworkChecker) {
                                    HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                                    isDnsReachable = HwSelfCureEngine.this.mNetworkChecker.syncCheckDnsResponse();
                                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                                }
                                int cnt2 = HwSelfCureUtils.getCurrentDnsFailedCounter();
                                if ((isDnsReachable == -1 && !HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_NETWORK_DISCONNECTED_RCVD)) || cnt2 - cnt1 > 0) {
                                    checkHttpResponseAndSelfCure(110);
                                    break;
                                }
                            }
                            HwSelfCureEngine.this.LOGD("CMD_NETWORK_ROAMING_DETECT rcvd, but user set static ip config, ignore it.");
                            break;
                        }
                        HwSelfCureEngine.this.LOGD("CMD_NETWORK_ROAMING_DETECT rcvd, but bssid is unchanged, ignore it.");
                        break;
                    }
                    HwSelfCureEngine.this.LOGD("CMD_NETWORK_ROAMING_DETECT rcvd, but it's background connection, ignore it.");
                    break;
                    break;
                case HwSelfCureEngine.CMD_GATEWAY_CHANGED_DETECT /*115*/:
                    if (HwSelfCureEngine.this.mDhcpOfferPackets.size() >= 2 || (this.mHasInternetRecently && (this.mConfigAuthType == 1 || this.mConfigAuthType == 4))) {
                        checkHttpResponseAndSelfCure(HwSelfCureEngine.CMD_GATEWAY_CHANGED_DETECT);
                        break;
                    }
                case HwSelfCureEngine.CMD_RESETUP_SELF_CURE_MONITOR /*118*/:
                    HwSelfCureEngine.this.LOGD("CMD_RESETUP_SELF_CURE_MONITOR rcvd");
                    setupSelfCureMonitor();
                    break;
                case 121:
                    if (!(this.mMobileHotspot || HwSelfCureEngine.this.mIsWifiBackground.get())) {
                        updateInternetAccessHistory();
                        handleNoTcpRxDetected((long) message.arg1, (long) message.arg2);
                        break;
                    }
                case 122:
                    if (!this.mMobileHotspot) {
                        if (!HwSelfCureEngine.this.mStaticIpCureSuccess && ((Boolean) message.obj).booleanValue()) {
                            if (this.mHasInternetRecently || this.mPortalUnthenEver || HwSelfCureEngine.this.mInternetUnknown) {
                                HwSelfCureEngine.this.LOGD("CMD_INTERNET_FAILURE_DETECTED, wifi has no internet when connected.");
                                transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS);
                                break;
                            }
                        }
                        HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                        HwSelfCureEngine.this.LOGD("CMD_INTERNET_FAILURE_DETECTED, start scan and parse the context for wifi 2 wifi.");
                        HwSelfCureEngine.this.mWifiStateMachine.startScan(Binder.getCallingUid(), 0, null, null);
                        if (!HwSelfCureEngine.this.isHttpReachable(false)) {
                            handleNewScanResults();
                            if (!this.mWifiSwitchAllowed) {
                                HwSelfCureEngine.this.LOGD("CMD_INTERNET_FAILURE_DETECTED, HTTP unreachable, transition to SelfCureState.");
                                int currentDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
                                int deltaFailedDns = currentDnsFailedCounter - this.mLastDnsFailedCounter;
                                this.mLastDnsFailedCounter = currentDnsFailedCounter;
                                transitionToSelfCureState(deltaFailedDns >= 2 ? HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS : HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP);
                                break;
                            }
                            HwSelfCureEngine.this.LOGD("CMD_INTERNET_FAILURE_DETECTED, notify WLAN+ to do wifi swtich first.");
                            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                            HwSelfCureEngine.this.notifyHttpReachableForWifiPro(false);
                            break;
                        }
                        HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                        this.mDnsConsecutiveFailedCounter = 0;
                        HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                        break;
                    }
                    break;
                case HwSelfCureEngine.CMD_DNS_FAILED_MONITOR /*123*/:
                    handleDnsFailedMonitor();
                    break;
                case 125:
                    updateDhcpResultsByBssid(HwSelfCureEngine.this.mWifiStateMachine.getCurrentBSSID(), (String) message.obj);
                    break;
                case 127:
                    handleNewScanResults();
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void handleNewScanResults() {
            List<ScanResult> scanResults = HwSelfCureEngine.this.mWifiManager.getScanResults();
            List<WifiConfiguration> configNetworks = HwSelfCureEngine.this.mWifiManager.getConfiguredNetworks();
            WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            this.mWifiSwitchAllowed = config != null ? WifiProCommonUtils.isAllowWifiSwitch(scanResults, configNetworks, WifiProCommonUtils.getCurrentBssid(HwSelfCureEngine.this.mWifiManager), WifiProCommonUtils.getCurrentSsid(HwSelfCureEngine.this.mWifiManager), config.configKey(), -75) : false;
        }

        private boolean setupSelfCureMonitor() {
            WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            if (config == null) {
                return false;
            }
            HwSelfCureEngine.this.mSelfCureConfig = config;
            this.mConfigAuthType = config.allowedKeyManagement.cardinality() > 1 ? -1 : config.getAuthType();
            boolean z = config.getIpAssignment() != null && config.getIpAssignment() == IpAssignment.STATIC;
            this.mUserSetStaticIpConfig = z;
            HwSelfCureEngine.this.mInternetUnknown = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 103);
            updateInternetAccessHistory();
            HwSelfCureEngine.this.LOGD("ConnectedMonitorState, hasInternet = " + this.mHasInternetRecently + ", portalUnthen = " + this.mPortalUnthenEver + ", userSetStaticIp = " + this.mUserSetStaticIpConfig + ", history empty = " + HwSelfCureEngine.this.mInternetUnknown);
            if (!this.mMobileHotspot) {
                DhcpResults dhcpResults = HwSelfCureEngine.this.mWifiStateMachine.syncGetDhcpResults();
                this.mIpv6DnsEnabled = parseIpv6Enabled(dhcpResults);
                StaticIpConfiguration staticIpConfig = WifiProCommonUtils.dhcpResults2StaticIpConfig(config.lastDhcpResults);
                boolean needGatewayDetect = (this.mUserSetStaticIpConfig || dhcpResults == null || staticIpConfig == null || dhcpResults.gateway == null || staticIpConfig.gateway == null || staticIpConfig.ipAddress == null || staticIpConfig.dnsServers == null) ? false : true;
                if (needGatewayDetect) {
                    boolean gatewayChanged;
                    String currentGateway = dhcpResults.gateway.getHostAddress();
                    String lastGateway = staticIpConfig.gateway.getHostAddress();
                    HwSelfCureEngine.this.LOGD("ConnectedMonitorState, currentGateway = " + currentGateway + ", lastGateway = " + lastGateway);
                    if (HwSelfCureEngine.this.mStaticIpCureSuccess || currentGateway == null || lastGateway == null) {
                        gatewayChanged = false;
                    } else {
                        gatewayChanged = !currentGateway.equals(lastGateway);
                    }
                    if (gatewayChanged) {
                        HwSelfCureEngine.this.LOGD("ConnectedMonitorState, current gateway is different with history gateway that has internet.");
                        HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_GATEWAY_CHANGED_DETECT, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                        return true;
                    }
                } else if (TextUtils.isEmpty(config.lastDhcpResults) && HwSelfCureEngine.this.mInternetUnknown) {
                    HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_GATEWAY_CHANGED_DETECT, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                    return true;
                }
            }
            if (!(this.mMobileHotspot || HwSelfCureEngine.this.mIsWifiBackground.get() || HwSelfCureEngine.this.mStaticIpCureSuccess || (!this.mHasInternetRecently && !HwSelfCureEngine.this.mInternetUnknown))) {
                HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_DNS_FAILED_MONITOR, 6000);
            }
            return true;
        }

        private boolean parseIpv6Enabled(DhcpResults dhcpResults) {
            if (dhcpResults == null || dhcpResults.dnsServers == null || dhcpResults.dnsServers.size() == 0) {
                return true;
            }
            for (int i = 0; i < dhcpResults.dnsServers.size(); i++) {
                InetAddress dns = (InetAddress) dhcpResults.dnsServers.get(i);
                if (dns != null && dns.getHostAddress() != null && ((dns instanceof Inet6Address) || dns.getHostAddress().contains(":"))) {
                    return true;
                }
            }
            return false;
        }

        private void handleDnsFailedMonitor() {
            if (this.mLastSignalLevel <= 1) {
                this.mLastDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
                HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_DNS_FAILED_MONITOR, 6000);
                return;
            }
            int currentDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
            int deltaFailedDns = currentDnsFailedCounter - this.mLastDnsFailedCounter;
            this.mLastDnsFailedCounter = currentDnsFailedCounter;
            if (deltaFailedDns >= 2) {
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                if (HwSelfCureEngine.this.isHttpReachable(true)) {
                    this.mDnsConsecutiveFailedCounter = 0;
                    HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                } else {
                    HwSelfCureEngine.this.LOGD("handleDnsFailedMonitor, deltaFailedDns = " + deltaFailedDns + ", and HTTP unreachable, transition to SelfCureState.");
                    transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS);
                }
            }
        }

        private void checkHttpResponseAndSelfCure(int eventType) {
            HwSelfCureEngine.this.mSelfCureOngoing.set(true);
            if (HwSelfCureEngine.this.isHttpReachable(false)) {
                HwSelfCureEngine.this.LOGD("checkHttpResponseAndSelfCure, HTTP reachable for eventType = " + eventType);
                this.mLastDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
                this.mDnsConsecutiveFailedCounter = 0;
                HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                return;
            }
            HwSelfCureEngine.this.LOGD("checkHttpResponseAndSelfCure, HTTP unreachable for eventType = " + eventType + ", dhcp offer size = " + HwSelfCureEngine.this.mDhcpOfferPackets.size());
            int internetFailedReason = 300;
            if (eventType == 110) {
                internetFailedReason = HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING;
            } else if (eventType == HwSelfCureEngine.CMD_GATEWAY_CHANGED_DETECT) {
                internetFailedReason = HwSelfCureEngine.INTERNET_FAILED_TYPE_GATEWAY;
            }
            transitionToSelfCureState(internetFailedReason);
        }

        private void handleNoTcpRxDetected(long deltaTcpTx, long deltaTcpReTx) {
            if (this.mLastSignalLevel > 2) {
                HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                hwSelfCureEngine.mNoTcpRxCounter = hwSelfCureEngine.mNoTcpRxCounter + 1;
                if (HwSelfCureEngine.this.mNoTcpRxCounter == 1) {
                    this.mLastDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
                } else if (HwSelfCureEngine.this.mNoTcpRxCounter == 2) {
                    HwSelfCureEngine.this.LOGD("handleNoTcpRxDetected, start scan and parse the context for wifi 2 wifi.");
                    HwSelfCureEngine.this.mWifiStateMachine.startScan(Binder.getCallingUid(), 0, null, null);
                } else if (HwSelfCureEngine.this.mNoTcpRxCounter == 3 && this.mWifiSwitchAllowed) {
                    HwSelfCureEngine.this.LOGD("handleNoTcpRxDetected, notify WLAN+ to do wifi swtich first.");
                    HwSelfCureEngine.this.notifyHttpReachableForWifiPro(false);
                    return;
                }
                if (HwSelfCureEngine.this.mNoTcpRxCounter >= 3) {
                    int currentDnsFailedCounter = HwSelfCureUtils.getCurrentDnsFailedCounter();
                    int deltaFailedDns = currentDnsFailedCounter - this.mLastDnsFailedCounter;
                    this.mLastDnsFailedCounter = currentDnsFailedCounter;
                    boolean mayDnsFailed = deltaFailedDns >= 2 || (deltaFailedDns == 1 && this.mDnsConsecutiveFailedCounter > 0);
                    if ((this.mHasInternetRecently || this.mPortalUnthenEver || HwSelfCureEngine.this.mInternetUnknown) && mayDnsFailed) {
                        HwSelfCureEngine.this.LOGD("handleNoTcpRxDetected, deltaFailedDns = " + deltaFailedDns + ", total failed counter = " + this.mDnsConsecutiveFailedCounter);
                        HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                        if (HwSelfCureEngine.this.isHttpReachable(true)) {
                            this.mDnsConsecutiveFailedCounter = 0;
                            HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                            return;
                        }
                        HwSelfCureEngine.this.LOGD("handleNoTcpRxDetected, HTTP unreachable, transition to SelfCureState.");
                        transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS);
                        return;
                    }
                    if (this.mHasInternetRecently || this.mPortalUnthenEver || HwSelfCureEngine.this.mInternetUnknown) {
                        boolean failed4StrongRssi = (this.mLastSignalLevel < 3 || deltaTcpTx < 3) ? false : deltaTcpReTx >= 2;
                        boolean failed4PoorRssi = (this.mLastSignalLevel != 2 || deltaTcpTx < 8) ? false : deltaTcpReTx >= 3;
                        if (failed4StrongRssi || failed4PoorRssi) {
                            HwSelfCureEngine.this.LOGD("handleNoTcpRxDetected, deltaTcpRx = 0, deltaTcpTx = " + deltaTcpTx + ", deltaTcpReTx = " + deltaTcpReTx + ", level = " + this.mLastSignalLevel);
                            HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                            if (HwSelfCureEngine.this.isHttpReachable(true)) {
                                this.mDnsConsecutiveFailedCounter = 0;
                                HwSelfCureEngine.this.mNoTcpRxCounter = 0;
                                HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                                return;
                            }
                            HwSelfCureEngine.this.LOGD("handleNoTcpRxDetected, HTTP unreachable, transition to SelfCureState.");
                            transitionToSelfCureState(HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP);
                            return;
                        }
                    }
                    if (deltaFailedDns == 1) {
                        this.mDnsConsecutiveFailedCounter++;
                    }
                }
            }
        }

        private void transitionToSelfCureState(int reason) {
            if (!this.mIpv6DnsEnabled) {
                Message dmsg = Message.obtain();
                dmsg.what = HwSelfCureEngine.CMD_INTERNET_FAILED_SELF_CURE;
                dmsg.arg1 = reason;
                HwSelfCureEngine.this.sendMessageDelayed(dmsg, 1000);
                HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mInternetSelfCureState);
            }
        }

        private void updateInternetAccessHistory() {
            WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            if (config != null) {
                this.mHasInternetRecently = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 100);
                this.mPortalUnthenEver = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 102);
            }
        }

        private void updateDhcpResultsByBssid(String bssid, String dhcpResults) {
            if (bssid != null && dhcpResults != null) {
                PortalDataBaseManager database = PortalDataBaseManager.getInstance(HwSelfCureEngine.this.mContext);
                if (database != null) {
                    database.updateDhcpResultsByBssid(bssid, dhcpResults);
                }
            }
        }
    }

    class ConnectionSelfCureState extends State {
        ConnectionSelfCureState() {
        }

        public boolean processMessage(Message message) {
            WifiConfiguration wifiConfiguration = null;
            switch (message.what) {
                case 103:
                    String str;
                    if (message.obj != null) {
                        str = (String) message.obj;
                    }
                    handleConnSelfCureFailed(str);
                    break;
                case 104:
                    if (HwSelfCureEngine.this.hasMessages(103)) {
                        HwSelfCureEngine.this.LOGD("CMD_CURE_CONNECTED_TIMEOUT msg removed");
                        HwSelfCureEngine.this.removeMessages(103);
                    }
                    HwSelfCureEngine.this.handleNetworkConnected();
                    break;
                case HwSelfCureEngine.CMD_WIFI_DISABLED_RCVD /*105*/:
                    if (HwSelfCureEngine.this.hasMessages(103)) {
                        HwSelfCureEngine.this.LOGD("CMD_CURE_CONNECTED_TIMEOUT msg removed");
                        HwSelfCureEngine.this.removeMessages(103);
                    }
                    HwSelfCureEngine.this.handleWifiDisabled(true);
                    break;
                case HwSelfCureEngine.CMD_CONFIGURED_NETWORK_DELETED /*107*/:
                    HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                    if (message.obj != null) {
                        wifiConfiguration = (WifiConfiguration) message.obj;
                    }
                    hwSelfCureEngine.handleNetworkRemoved(wifiConfiguration);
                    break;
                case HwSelfCureEngine.CMD_NETWORK_DISCONNECTED_RCVD /*108*/:
                    HwSelfCureEngine.this.LOGD("CMD_NETWORK_DISCONNECTED_RCVD during connection self cure state.");
                    if (HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_UPDATE_CONN_SELF_CURE_HISTORY)) {
                        HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_UPDATE_CONN_SELF_CURE_HISTORY);
                    }
                    HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mDisconnectedMonitorState);
                    break;
                case HwSelfCureEngine.CMD_UPDATE_CONN_SELF_CURE_HISTORY /*119*/:
                    HwSelfCureEngine.this.updateConnSelfCureFailedHistory();
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void handleConnSelfCureFailed(String configKey) {
            HwSelfCureEngine.this.LOGD("ENTER: handleConnSelfCureFailed(), configKey = " + configKey);
            if (configKey != null) {
                HwSelfCureEngine.this.mNoAutoConnCounter = 0;
                HwSelfCureEngine.this.autoConnectFailedNetworks.clear();
                HwSelfCureEngine.this.autoConnectFailedNetworksRssi.clear();
                CureFailedNetworkInfo cureHistory = (CureFailedNetworkInfo) HwSelfCureEngine.this.networkCureFailedHistory.get(configKey);
                if (cureHistory != null) {
                    cureHistory.cureFailedCounter++;
                    cureHistory.lastCureFailedTime = System.currentTimeMillis();
                } else {
                    HwSelfCureEngine.this.networkCureFailedHistory.put(configKey, new CureFailedNetworkInfo(configKey, 1, System.currentTimeMillis()));
                    HwSelfCureEngine.this.LOGD("handleConnSelfCureFailed, networkCureFailedHistory added, configKey = " + configKey);
                }
            }
            if (!(HwSelfCureEngine.this.mNoAutoConnReason == -1 || HwSelfCureEngine.this.mHwWifiStatStore == null)) {
                HwSelfCureEngine.this.mHwWifiStatStore.updateScCHRCount(HwSelfCureEngine.this.mNoAutoConnReason);
            }
            HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mDisconnectedMonitorState);
        }
    }

    static class CureFailedNetworkInfo {
        public String configKey;
        public int cureFailedCounter;
        public long lastCureFailedTime;

        public CureFailedNetworkInfo(String key, int counter, long time) {
            this.configKey = key;
            this.cureFailedCounter = counter;
            this.lastCureFailedTime = time;
        }

        public String toString() {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append("[ ");
            sbuf.append("configKey = ").append(this.configKey);
            sbuf.append(", cureFailedCounter = ").append(this.cureFailedCounter);
            sbuf.append(", lastCureFailedTime = ").append(DateFormat.getDateTimeInstance().format(new Date(this.lastCureFailedTime)));
            sbuf.append(" ]");
            return sbuf.toString();
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 126:
                    handleDhcpOfferPacketRcv((String) message.obj);
                    break;
            }
            return true;
        }

        private void handleDhcpOfferPacketRcv(String dhcpResutls) {
            if (dhcpResutls != null && HwSelfCureEngine.this.isSuppOnCompletedState() && !HwSelfCureEngine.this.mWifiStateMachine.isWifiProEvaluatingAP()) {
                String gateway = WifiProCommonUtils.dhcpResults2Gateway(dhcpResutls);
                if (gateway != null) {
                    HwSelfCureEngine.this.mDhcpOfferPackets.put(gateway.replace("/", ""), dhcpResutls);
                }
            }
        }
    }

    class DisconnectedMonitorState extends State {
        private boolean mSetStaticIpConfig;

        DisconnectedMonitorState() {
        }

        public void enter() {
            HwSelfCureEngine.this.LOGD("==> ##DisconnectedMonitorState");
            HwSelfCureEngine.this.mStaticIpCureSuccess = false;
            HwSelfCureEngine.this.mNoAutoConnCounter = 0;
            HwSelfCureEngine.this.mNoAutoConnReason = -1;
            HwSelfCureEngine.this.mNoAutoConnConfig = null;
            HwSelfCureEngine.this.mSelfCureConfig = null;
            HwSelfCureEngine.this.mIsWifiBackground.set(false);
            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
            this.mSetStaticIpConfig = false;
            HwSelfCureEngine.this.mConnectedTimeMills = 0;
            HwSelfCureEngine.this.mDhcpOfferPackets.clear();
            HwSelfCureEngine.this.mDhcpResultsTestDone.clear();
        }

        public boolean processMessage(Message message) {
            WifiConfiguration wifiConfiguration = null;
            switch (message.what) {
                case 101:
                    if (!(HwSelfCureEngine.this.isConnectingOrConnected() || HwSelfCureUtils.isOnWlanSettings(HwSelfCureEngine.this.mContext))) {
                        List<ScanResult> scanResults = HwSelfCureEngine.this.mWifiManager.getScanResults();
                        updateAutoConnFailedNetworks(scanResults);
                        HwSelfCureUtils.selectDisabledNetworks(scanResults, HwSelfCureEngine.this.mWifiManager.getConfiguredNetworks(), HwSelfCureEngine.this.autoConnectFailedNetworks, HwSelfCureEngine.this.autoConnectFailedNetworksRssi, HwSelfCureEngine.this.mWifiStateMachine);
                        selectHighestFailedNetworkAndCure();
                        break;
                    }
                case 102:
                    if (!HwSelfCureUtils.isOnWlanSettings(HwSelfCureEngine.this.mContext)) {
                        if (message.obj != null) {
                            wifiConfiguration = (WifiConfiguration) message.obj;
                        }
                        trySelfCureSelectedNetwork(wifiConfiguration);
                        break;
                    }
                    break;
                case 104:
                    HwSelfCureEngine.this.handleNetworkConnected();
                    break;
                case HwSelfCureEngine.CMD_WIFI_DISABLED_RCVD /*105*/:
                    HwSelfCureEngine.this.handleWifiDisabled(false);
                    break;
                case HwSelfCureEngine.CMD_WIFI_ENABLED_RCVD /*106*/:
                    HwSelfCureEngine.this.handleWifiEnabled();
                    break;
                case HwSelfCureEngine.CMD_CONFIGURED_NETWORK_DELETED /*107*/:
                    HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
                    if (message.obj != null) {
                        wifiConfiguration = (WifiConfiguration) message.obj;
                    }
                    hwSelfCureEngine.handleNetworkRemoved(wifiConfiguration);
                    break;
                case HwSelfCureEngine.CMD_IP_CONFIG_COMPLETED /*117*/:
                    if (this.mSetStaticIpConfig) {
                        this.mSetStaticIpConfig = false;
                        if (HwSelfCureEngine.this.mHwWifiStatStore != null) {
                            HwSelfCureEngine.this.mHwWifiStatStore.updateScCHRCount(12);
                            HwSelfCureEngine.this.mHwWifiStatStore.updateScCHRCount(25);
                            break;
                        }
                    }
                    break;
                case 124:
                    String currentBSSID = HwSelfCureEngine.this.mWifiStateMachine.getCurrentBSSID();
                    if (message.obj != null) {
                        wifiConfiguration = (WifiConfiguration) message.obj;
                    }
                    handleIpConfigLost(currentBSSID, wifiConfiguration);
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void handleIpConfigLost(String bssid, WifiConfiguration config) {
            if (bssid != null && config != null) {
                String dhcpResults = null;
                PortalDataBaseManager database = PortalDataBaseManager.getInstance(HwSelfCureEngine.this.mContext);
                if (database != null) {
                    dhcpResults = database.syncQueryDhcpResultsByBssid(bssid);
                }
                if (dhcpResults == null) {
                    dhcpResults = config.lastDhcpResults;
                }
                if (dhcpResults != null) {
                    StaticIpConfiguration staticIpConfig = WifiProCommonUtils.dhcpResults2StaticIpConfig(dhcpResults);
                    this.mSetStaticIpConfig = true;
                    HwSelfCureEngine.this.mWifiStateMachine.requestUseStaticIpConfig(staticIpConfig);
                }
            }
        }

        private void updateAutoConnFailedNetworks(List<ScanResult> scanResults) {
            List<String> refreshedNetworksKey = HwSelfCureUtils.getRefreshedCureFailedNetworks(HwSelfCureEngine.this.networkCureFailedHistory);
            for (int i = 0; i < refreshedNetworksKey.size(); i++) {
                HwSelfCureEngine.this.LOGD("updateAutoConnFailedNetworks, refreshed cure failed network, currKey = " + ((String) refreshedNetworksKey.get(i)));
                HwSelfCureEngine.this.networkCureFailedHistory.remove(refreshedNetworksKey.get(i));
            }
            List<String> unstableKey = HwSelfCureUtils.searchUnstableNetworks(HwSelfCureEngine.this.autoConnectFailedNetworks, scanResults);
            for (int j = 0; j < unstableKey.size(); j++) {
                HwSelfCureEngine.this.LOGD("updateAutoConnFailedNetworks, remove it due to signal unstable, currKey = " + ((String) unstableKey.get(j)));
                HwSelfCureEngine.this.autoConnectFailedNetworks.remove(unstableKey.get(j));
                HwSelfCureEngine.this.autoConnectFailedNetworksRssi.remove(unstableKey.get(j));
            }
        }

        private void selectHighestFailedNetworkAndCure() {
            if (HwSelfCureEngine.this.autoConnectFailedNetworks.size() == 0) {
                HwSelfCureEngine.this.mNoAutoConnCounter = 0;
                return;
            }
            HwSelfCureEngine hwSelfCureEngine = HwSelfCureEngine.this;
            if (hwSelfCureEngine.mNoAutoConnCounter = hwSelfCureEngine.mNoAutoConnCounter + 1 < 3) {
                HwSelfCureEngine.this.LOGD("selectHighestFailedNetworkAndCure, MAX_FAILED_CURE unmatched, wait more time for self cure.");
                return;
            }
            WifiConfiguration bestSelfCureCandidate = HwSelfCureUtils.selectHighestFailedNetwork(HwSelfCureEngine.this.networkCureFailedHistory, HwSelfCureEngine.this.autoConnectFailedNetworks, HwSelfCureEngine.this.autoConnectFailedNetworksRssi);
            if (bestSelfCureCandidate != null) {
                HwSelfCureEngine.this.LOGD("selectHighestFailedNetworkAndCure, delay 1s to self cure the selected candidate = " + bestSelfCureCandidate.configKey());
                Message dmsg = Message.obtain();
                dmsg.what = 102;
                dmsg.obj = bestSelfCureCandidate;
                HwSelfCureEngine.this.sendMessageDelayed(dmsg, 1000);
            }
        }

        private void trySelfCureSelectedNetwork(WifiConfiguration config) {
            if (config != null && config.networkId != -1 && !HwSelfCureEngine.this.isConnectingOrConnected()) {
                HwSelfCureEngine.this.LOGD("ENTER: trySelfCureSelectedNetwork(), config = " + config.configKey());
                if (HwSelfCureUtils.isWifiProEnabled()) {
                    if (WifiProCommonUtils.isOpenAndPortal(config) || WifiProCommonUtils.isOpenAndMaybePortal(config)) {
                        HwSelfCureEngine.this.mWifiStateMachine.setWifiBackgroundReason(0);
                        HwSelfCureEngine.this.LOGD("trySelfCureSelectedNetwork, self cure at background, due to [maybe] portal, candidate = " + config.configKey());
                    } else if (config.noInternetAccess && !NetworkHistoryUtils.allowWifiConfigRecovery(config.internetHistory)) {
                        HwSelfCureEngine.this.mWifiStateMachine.setWifiBackgroundReason(3);
                        HwSelfCureEngine.this.LOGD("trySelfCureSelectedNetwork, self cure at background, due to no internet, candidate = " + config.configKey());
                    }
                }
                HwSelfCureEngine.this.mWifiStateMachine.autoConnectToNetwork(config.networkId, null);
                int chrType = -1;
                NetworkSelectionStatus status = config.getNetworkSelectionStatus();
                int disableReason = status.getNetworkSelectionDisableReason();
                if (disableReason == 3) {
                    chrType = 10;
                } else if (disableReason == 2) {
                    chrType = 11;
                } else if (disableReason == 4) {
                    chrType = 12;
                } else if (disableReason == 9) {
                    chrType = 13;
                } else if (status.isNetworkEnabled()) {
                    chrType = 24;
                }
                if (chrType != -1) {
                    HwSelfCureEngine.this.mNoAutoConnReason = chrType;
                    HwSelfCureEngine.this.mNoAutoConnConfig = config;
                }
                Message dmsg = Message.obtain();
                dmsg.what = 103;
                dmsg.obj = config.configKey();
                HwSelfCureEngine.this.sendMessageDelayed(dmsg, 20000);
                HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mConnectionSelfCureState);
            }
        }
    }

    static class InternetSelfCureHistoryInfo {
        public int dnsSelfCureFailedCnt = 0;
        public long lastDnsSelfCureFailedTs = 0;
        public long lastReassocSelfCureConnectFailedTs = 0;
        public long lastReassocSelfCureFailedTs = 0;
        public long lastRenewDhcpSelfCureFailedTs = 0;
        public long lastResetSelfCureConnectFailedTs = 0;
        public long lastResetSelfCureFailedTs = 0;
        public long lastStaticIpSelfCureFailedTs = 0;
        public int reassocSelfCureConnectFailedCnt = 0;
        public int reassocSelfCureFailedCnt = 0;
        public int renewDhcpSelfCureFailedCnt = 0;
        public int resetSelfCureConnectFailedCnt = 0;
        public int resetSelfCureFailedCnt = 0;
        public int staticIpSelfCureFailedCnt = 0;

        public String toString() {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append("[ ");
            sbuf.append("dnsSelfCureFailedCnt = ").append(this.dnsSelfCureFailedCnt);
            sbuf.append(", renewDhcpSelfCureFailedCnt = ").append(this.renewDhcpSelfCureFailedCnt);
            sbuf.append(", staticIpSelfCureFailedCnt = ").append(this.staticIpSelfCureFailedCnt);
            sbuf.append(", reassocSelfCureFailedCnt = ").append(this.reassocSelfCureFailedCnt);
            sbuf.append(", resetSelfCureFailedCnt = ").append(this.resetSelfCureFailedCnt);
            sbuf.append(", reassocSelfCureConnectFailedCnt = ").append(this.reassocSelfCureConnectFailedCnt);
            sbuf.append(", resetSelfCureConnectFailedCnt = ").append(this.resetSelfCureConnectFailedCnt);
            sbuf.append(" ]");
            return sbuf.toString();
        }
    }

    class InternetSelfCureState extends State {
        private int mConfigAuthType = -1;
        private boolean mConfigStaticIp4GatewayChanged = false;
        private boolean mConfigStaticIp4MultiDhcpServer = false;
        private int mCurrentAbnormalType;
        private String mCurrentGateway;
        private int mCurrentRssi;
        private int mCurrentSelfCureLevel;
        private boolean mDelayedReassocSelfCure = false;
        private boolean mDelayedResetSelfCure = false;
        private boolean mFinalSelfCureUsed = false;
        private boolean mHasInternetRecently;
        private long mLastHasInetTimeMillis;
        private int mLastSelfCureLevel;
        private boolean mPortalUnthenEver;
        private int mSelfCureFailedCounter;
        private InternetSelfCureHistoryInfo mSelfCureHistoryInfo;
        private List<Integer> mTestedSelfCureLevel = new ArrayList();
        private boolean mUserSetStaticIpConfig;

        InternetSelfCureState() {
        }

        public void enter() {
            boolean z = false;
            HwSelfCureEngine.this.LOGD("==> ##InternetSelfCureState");
            this.mCurrentRssi = WifiHandover.INVALID_RSSI;
            this.mSelfCureFailedCounter = 0;
            this.mCurrentAbnormalType = -1;
            this.mLastSelfCureLevel = -1;
            this.mCurrentSelfCureLevel = 200;
            this.mHasInternetRecently = false;
            this.mPortalUnthenEver = false;
            this.mUserSetStaticIpConfig = false;
            this.mCurrentGateway = getCurrentGateway();
            this.mTestedSelfCureLevel.clear();
            this.mFinalSelfCureUsed = false;
            WifiInfo wifiInfo = HwSelfCureEngine.this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                this.mCurrentRssi = wifiInfo.getRssi();
                HwSelfCureEngine.this.LOGD("InternetSelfCureState, network = " + wifiInfo.getSSID() + ", signal rssi = " + this.mCurrentRssi);
            }
            WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            if (config != null) {
                this.mSelfCureHistoryInfo = HwSelfCureUtils.string2InternetSelfCureHistoryInfo(config.internetSelfCureHistory);
                this.mHasInternetRecently = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 100);
                this.mPortalUnthenEver = WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 102);
                if (config.getIpAssignment() != null && config.getIpAssignment() == IpAssignment.STATIC) {
                    z = true;
                }
                this.mUserSetStaticIpConfig = z;
                this.mLastHasInetTimeMillis = config.lastHasInternetTimestamp;
                this.mConfigAuthType = config.allowedKeyManagement.cardinality() > 1 ? -1 : config.getAuthType();
                HwSelfCureEngine.this.LOGD("InternetSelfCureState, hasInternet = " + this.mHasInternetRecently + ", portalUnthenEver = " + this.mPortalUnthenEver + ", userSetStaticIp = " + this.mUserSetStaticIpConfig + ", historyInfo = " + this.mSelfCureHistoryInfo + ", gw = " + this.mCurrentGateway);
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case HwSelfCureEngine.CMD_NETWORK_DISCONNECTED_RCVD /*108*/:
                    HwSelfCureEngine.this.LOGD("CMD_NETWORK_DISCONNECTED_RCVD during self cure state.");
                    HwSelfCureEngine.this.mWifiStateMachine.resetIpConfigStatus();
                    HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mDisconnectedMonitorState);
                    break;
                case HwSelfCureEngine.CMD_NEW_RSSI_RCVD /*109*/:
                    this.mCurrentRssi = message.arg1;
                    handleRssiChanged();
                    break;
                case 110:
                    handleRoamingDetected();
                    break;
                case HwSelfCureEngine.CMD_INTERNET_FAILED_SELF_CURE /*112*/:
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    if (HwSelfCureEngine.this.isSuppOnCompletedState()) {
                        selectSelfCureByFailedReason(message.arg1);
                        break;
                    }
                    break;
                case HwSelfCureEngine.CMD_INTERNET_RECOVERY_CONFIRM /*113*/:
                    HwSelfCureUtils.updateSelfCureConnectHistoryInfo(this.mSelfCureHistoryInfo, this.mCurrentSelfCureLevel, true);
                    if (confirmInternetSelfCure(this.mCurrentSelfCureLevel)) {
                        this.mCurrentSelfCureLevel = 200;
                        this.mSelfCureFailedCounter = 0;
                        this.mHasInternetRecently = true;
                        break;
                    }
                    break;
                case HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK /*114*/:
                    if (HwSelfCureEngine.this.isSuppOnCompletedState()) {
                        this.mCurrentSelfCureLevel = message.arg1;
                        selfCureWifiLink(message.arg1);
                        break;
                    }
                    break;
                case HwSelfCureEngine.CMD_IP_CONFIG_TIMEOUT /*116*/:
                    HwSelfCureEngine.this.LOGD("CMD_IP_CONFIG_TIMEOUT during self cure state.");
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    HwSelfCureEngine.this.mWifiStateMachine.resetIpConfigStatus();
                    break;
                case HwSelfCureEngine.CMD_IP_CONFIG_COMPLETED /*117*/:
                    if (HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_IP_CONFIG_TIMEOUT)) {
                        HwSelfCureEngine.this.LOGD("CMD_IP_CONFIG_TIMEOUT msg removed because of ip config success.");
                        HwSelfCureEngine.this.removeMessages(HwSelfCureEngine.CMD_IP_CONFIG_TIMEOUT);
                        HwSelfCureEngine.this.mWifiStateMachine.resetIpConfigStatus();
                        this.mCurrentGateway = getCurrentGateway();
                        HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_INTERNET_RECOVERY_CONFIRM, 2000);
                        break;
                    }
                    break;
                case 120:
                    HwSelfCureUtils.updateSelfCureConnectHistoryInfo(this.mSelfCureHistoryInfo, this.mCurrentSelfCureLevel, false);
                    updateWifiConfig(HwSelfCureEngine.this.mSelfCureConfig);
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    uploadConnectFailedCounter(this.mCurrentSelfCureLevel);
                    break;
                case 128:
                    HwSelfCureEngine.this.LOGD("CMD_P2P_DISCONNECTED_EVENT during self cure state.");
                    handleRssiChanged();
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void saveCurrentAbnormalType(int internetFailedType) {
            int chrType = -1;
            if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS) {
                chrType = 0;
            } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_GATEWAY) {
                chrType = 3;
            } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING) {
                chrType = 2;
            } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP) {
                chrType = 1;
            }
            if (chrType != -1) {
                this.mCurrentAbnormalType = chrType;
            }
        }

        private void uploadConnectFailedCounter(int connectType) {
            int chrType = -1;
            if (connectType == HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC) {
                chrType = 18;
            } else if (connectType == HwSelfCureUtils.RESET_LEVEL_HIGH_RESET) {
                chrType = 19;
            }
            if (chrType != -1 && HwSelfCureEngine.this.mHwWifiStatStore != null) {
                HwSelfCureEngine.this.mHwWifiStatStore.updateScCHRCount(chrType);
            }
        }

        private void uploadCurrentAbnormalStatistics() {
            if (this.mCurrentAbnormalType != -1 && HwSelfCureEngine.this.mHwWifiStatStore != null) {
                HwSelfCureEngine.this.mHwWifiStatStore.updateScCHRCount(this.mCurrentAbnormalType);
                this.mCurrentAbnormalType = -1;
            }
        }

        private void uploadInternetCureSuccCounter(int selfCureType) {
            uploadCurrentAbnormalStatistics();
            int chrType = -1;
            if (selfCureType == HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS) {
                chrType = 4;
            } else if (selfCureType == HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP) {
                chrType = 5;
            } else if (selfCureType == HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP) {
                chrType = 6;
            } else if (selfCureType == HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC) {
                chrType = 7;
            } else if (selfCureType == HwSelfCureUtils.RESET_LEVEL_HIGH_RESET) {
                if (this.mLastSelfCureLevel == HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS) {
                    chrType = 20;
                } else if (this.mLastSelfCureLevel == HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP) {
                    chrType = 21;
                } else if (this.mLastSelfCureLevel == HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP) {
                    chrType = 22;
                } else if (this.mLastSelfCureLevel == HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC) {
                    chrType = 8;
                }
            }
            if (chrType != -1 && HwSelfCureEngine.this.mHwWifiStatStore != null) {
                HwSelfCureEngine.this.mHwWifiStatStore.updateScCHRCount(chrType);
            }
        }

        private void handleInternetFailedAndUserSetStaticIp(int internetFailedType) {
            if (this.mHasInternetRecently && HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET)) {
                saveCurrentAbnormalType(internetFailedType);
                if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS) {
                    this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS;
                } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING) {
                    this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP;
                } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_GATEWAY) {
                    this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP;
                }
                HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET, 0);
                return;
            }
            HwSelfCureEngine.this.LOGD("handleInternetFailedAndUserSetStaticIp, user set static ip config, ignore to update config for user.");
            if (!HwSelfCureEngine.this.mInternetUnknown) {
                this.mCurrentAbnormalType = HwSelfCureUtils.RESET_REJECTED_BY_STATIC_IP_ENABLED;
                uploadCurrentAbnormalStatistics();
            }
        }

        private int selectBestSelfCureSolution(int internetFailedType) {
            boolean multipleDhcpServer = HwSelfCureEngine.this.mDhcpOfferPackets.size() >= 2;
            boolean noInternetWhenConnected = this.mLastHasInetTimeMillis <= 0 || this.mLastHasInetTimeMillis < HwSelfCureEngine.this.mConnectedTimeMills;
            HwSelfCureEngine.this.LOGD("selectBestSelfCureSolution, multipleDhcpServer = " + multipleDhcpServer + ", noInternetWhenConnected = " + noInternetWhenConnected);
            if (multipleDhcpServer && noInternetWhenConnected && getNextTestDhcpResults() != null && HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP) && (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS || internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP)) {
                this.mConfigStaticIp4MultiDhcpServer = true;
                return HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP;
            } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_GATEWAY && multipleDhcpServer && getNextTestDhcpResults() != null && HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP)) {
                this.mConfigStaticIp4MultiDhcpServer = true;
                return HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP;
            } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_GATEWAY && ((this.mConfigAuthType == 1 || this.mConfigAuthType == 4) && HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP))) {
                this.mConfigStaticIp4GatewayChanged = true;
                return HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP;
            } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING) {
                if (HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP)) {
                    return HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP;
                }
                return 200;
            } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS) {
                if (HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS)) {
                    return HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS;
                }
                return 200;
            } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP && HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC)) {
                return HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC;
            } else {
                return 200;
            }
        }

        private void selectSelfCureByFailedReason(int internetFailedType) {
            HwSelfCureEngine.this.LOGD("selectSelfCureByFailedReason, internetFailedType = " + internetFailedType + ", userSetStaticIp = " + this.mUserSetStaticIpConfig);
            if (this.mUserSetStaticIpConfig && (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS || internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_GATEWAY || internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING)) {
                handleInternetFailedAndUserSetStaticIp(internetFailedType);
                return;
            }
            int requestSelfCureLevel = selectBestSelfCureSolution(internetFailedType);
            if (requestSelfCureLevel != 200) {
                saveCurrentAbnormalType(internetFailedType);
                HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, requestSelfCureLevel, 0);
            } else if (HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET)) {
                HwSelfCureEngine.this.LOGD("selectSelfCureByFailedReason, use wifi reset to cure this failed type = " + internetFailedType);
                saveCurrentAbnormalType(internetFailedType);
                if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_DNS) {
                    this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS;
                } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_ROAMING) {
                    this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP;
                } else if (internetFailedType == HwSelfCureEngine.INTERNET_FAILED_TYPE_TCP) {
                    this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC;
                }
                HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET, 0);
            } else {
                HwSelfCureEngine.this.LOGD("selectSelfCureByFailedReason, no usable self cure for this failed type = " + internetFailedType);
            }
        }

        private boolean confirmInternetSelfCure(int currentCureLevel) {
            HwSelfCureEngine.this.LOGD("confirmInternetSelfCure, cureLevel = " + currentCureLevel + ", last failed counter = " + this.mSelfCureFailedCounter + ", finally = " + this.mFinalSelfCureUsed);
            if (currentCureLevel != 200) {
                if (HwSelfCureEngine.this.isHttpReachable(true)) {
                    handleHttpReachableAfterSelfCure(currentCureLevel);
                    HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mConnectedMonitorState);
                    return true;
                }
                this.mSelfCureFailedCounter++;
                HwSelfCureUtils.updateSelfCureHistoryInfo(this.mSelfCureHistoryInfo, currentCureLevel, false);
                updateWifiConfig(null);
                HwSelfCureEngine.this.LOGD("HTTP unreachable, self cure failed for " + currentCureLevel + ", selfCureHistoryInfo = " + this.mSelfCureHistoryInfo);
                HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                if (this.mFinalSelfCureUsed) {
                    handleHttpUnreachableFinally();
                    return false;
                } else if (currentCureLevel != HwSelfCureUtils.RESET_LEVEL_HIGH_RESET) {
                    if (currentCureLevel == HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP) {
                        if (getNextTestDhcpResults() != null) {
                            this.mLastSelfCureLevel = currentCureLevel;
                            HwSelfCureEngine.this.LOGD("HTTP unreachable, and has next dhcp results, try next one.");
                            HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP, 0);
                            return false;
                        }
                        this.mConfigStaticIp4MultiDhcpServer = false;
                        if (this.mCurrentAbnormalType == 0) {
                            this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS;
                            if (HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS)) {
                                HwSelfCureEngine.this.LOGD("HTTP unreachable, and no next dhcp results, use dns replace to cure for dns failed.");
                                HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS, 0);
                                return false;
                            }
                        } else if (this.mCurrentAbnormalType == 1) {
                            this.mLastSelfCureLevel = HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC;
                            if (HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC)) {
                                HwSelfCureEngine.this.LOGD("HTTP unreachable, and no next dhcp results, use reassoc to cure for no rx pkt.");
                                HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC, 0);
                                return false;
                            }
                        } else if (this.mCurrentAbnormalType == 2) {
                            currentCureLevel = HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP;
                        }
                    } else if (currentCureLevel == HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP && HwSelfCureEngine.this.mDhcpOfferPackets.size() >= 2) {
                        if (getNextTestDhcpResults() != null && HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP)) {
                            this.mLastSelfCureLevel = currentCureLevel;
                            this.mConfigStaticIp4MultiDhcpServer = true;
                            HwSelfCureEngine.this.LOGD("HTTP unreachable, and has next dhcp results, try next one for re-dhcp failed.");
                            HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP, 0);
                        }
                        return false;
                    }
                    if (hasBeenTested(HwSelfCureUtils.RESET_LEVEL_HIGH_RESET) || !HwSelfCureUtils.selectedSelfCureAcceptable(this.mSelfCureHistoryInfo, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET)) {
                        handleHttpUnreachableFinally();
                    } else {
                        this.mLastSelfCureLevel = currentCureLevel;
                        HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET, 0);
                    }
                } else if (getNextTestDhcpResults() == null || hasBeenTested(HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP)) {
                    handleHttpUnreachableFinally();
                } else {
                    this.mFinalSelfCureUsed = true;
                    this.mLastSelfCureLevel = currentCureLevel;
                    this.mConfigStaticIp4MultiDhcpServer = true;
                    HwSelfCureEngine.this.LOGD("HTTP unreachable, and has next dhcp results, try next one for wifi reset failed.");
                    HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP, 0);
                }
            }
            return false;
        }

        private boolean hasBeenTested(int cureLevel) {
            for (int i = 0; i < this.mTestedSelfCureLevel.size(); i++) {
                if (((Integer) this.mTestedSelfCureLevel.get(i)).intValue() == cureLevel) {
                    return true;
                }
            }
            return false;
        }

        private void handleHttpUnreachableFinally() {
            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
            if (!HwSelfCureEngine.this.mInternetUnknown) {
                uploadCurrentAbnormalStatistics();
            }
            HwSelfCureEngine.this.notifyHttpReachableForWifiPro(false);
        }

        private void handleHttpReachableAfterSelfCure(int currentCureLevel) {
            HwSelfCureEngine.this.LOGD("handleHttpReachableAfterSelfCure, cureLevel = " + currentCureLevel + ", HTTP reachable, --> ConnectedMonitorState.");
            HwSelfCureEngine.this.notifyHttpReachableForWifiPro(true);
            HwSelfCureUtils.updateSelfCureHistoryInfo(this.mSelfCureHistoryInfo, currentCureLevel, true);
            String strDhcpResults = WifiProCommonUtils.dhcpResults2String(HwSelfCureEngine.this.mWifiStateMachine.syncGetDhcpResults(), -1);
            WifiConfiguration currentConfig = WifiProCommonUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            if (!(currentConfig == null || strDhcpResults == null)) {
                currentConfig.lastDhcpResults = strDhcpResults;
            }
            updateWifiConfig(currentConfig);
            HwSelfCureEngine.this.mSelfCureOngoing.set(false);
            if (currentCureLevel == HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP) {
                this.mCurrentAbnormalType = 3;
                HwSelfCureEngine.this.mStaticIpCureSuccess = true;
            }
            uploadInternetCureSuccCounter(currentCureLevel);
            HwSelfCureEngine.this.sendMessageDelayed(125, strDhcpResults, 500);
        }

        private void handleRssiChanged() {
            if (this.mCurrentRssi > -76 && !HwSelfCureEngine.this.mSelfCureOngoing.get() && !HwSelfCureEngine.this.mP2pConnected.get()) {
                if (this.mDelayedReassocSelfCure || this.mDelayedResetSelfCure) {
                    HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                    if (HwSelfCureEngine.this.isHttpReachable(true)) {
                        HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                        this.mDelayedReassocSelfCure = false;
                        this.mDelayedResetSelfCure = false;
                        HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mConnectedMonitorState);
                        return;
                    }
                    HwSelfCureEngine.this.LOGD("handleRssiChanged, Http failed, delayedReassoc = " + this.mDelayedReassocSelfCure + ", delayedReset = " + this.mDelayedResetSelfCure);
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    if (this.mDelayedReassocSelfCure) {
                        HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC, 0);
                    } else if (this.mDelayedResetSelfCure) {
                        HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_HIGH_RESET, 0);
                    }
                }
            }
        }

        private void handleRoamingDetected() {
            if (!HwSelfCureEngine.this.hasMessages(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK) && !hasBeenTested(HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP) && !HwSelfCureEngine.this.mSelfCureOngoing.get() && !this.mDelayedReassocSelfCure && !this.mDelayedResetSelfCure) {
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                if (HwSelfCureEngine.this.isHttpReachable(false)) {
                    HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                    HwSelfCureEngine.this.transitionTo(HwSelfCureEngine.this.mConnectedMonitorState);
                    return;
                }
                HwSelfCureEngine.this.LOGD("handleRoamingDetected, and HTTP access failed, trigger Re-Dhcp for it first time.");
                HwSelfCureEngine.this.mSelfCureOngoing.set(false);
                HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_SELF_CURE_WIFI_LINK, HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP, 0);
            }
        }

        private String getCurrentGateway() {
            DhcpInfo dhcpInfo = HwSelfCureEngine.this.mWifiManager.getDhcpInfo();
            if (dhcpInfo == null || dhcpInfo.gateway == 0) {
                return null;
            }
            return NetworkUtils.intToInetAddress(dhcpInfo.gateway).getHostAddress();
        }

        private void updateWifiConfig(WifiConfiguration wifiConfig) {
            WifiConfiguration config;
            if (wifiConfig == null) {
                config = WifiProCommonUtils.getCurrentWifiConfig(HwSelfCureEngine.this.mWifiManager);
            } else {
                config = wifiConfig;
            }
            if (config != null) {
                config.internetSelfCureHistory = HwSelfCureUtils.internetSelfCureHistoryInfo2String(this.mSelfCureHistoryInfo);
                HwSelfCureEngine.this.mWifiStateMachine.sendMessage(HwWifiStateMachine.CMD_UPDATE_WIFIPRO_CONFIGURATIONS, config);
            }
        }

        private String getNextTestDhcpResults() {
            for (Entry entry : HwSelfCureEngine.this.mDhcpOfferPackets.entrySet()) {
                String gatewayKey = (String) entry.getKey();
                String dhcpResults = (String) entry.getValue();
                if (!(gatewayKey == null || gatewayKey.equals(this.mCurrentGateway))) {
                    boolean untested = true;
                    for (int i = 0; i < HwSelfCureEngine.this.mDhcpResultsTestDone.size(); i++) {
                        if (gatewayKey.equals(HwSelfCureEngine.this.mDhcpResultsTestDone.get(i))) {
                            untested = false;
                            break;
                        }
                    }
                    if (untested) {
                        return dhcpResults;
                    }
                }
            }
            return null;
        }

        private void selfCureWifiLink(int requestCureLevel) {
            HwSelfCureEngine.this.LOGD("selfCureWifiLink, cureLevel = " + requestCureLevel + ", signal rssi = " + this.mCurrentRssi);
            if (requestCureLevel == HwSelfCureUtils.RESET_LEVEL_LOW_1_DNS) {
                HwSelfCureEngine.this.LOGD("begin to self cure for internet access: RESET_LEVEL_LOW_1_DNS");
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                this.mTestedSelfCureLevel.add(Integer.valueOf(requestCureLevel));
                HwSelfCureEngine.this.mWifiStateMachine.requestUpdateDnsServers(HwSelfCureUtils.getPublicDnsServers());
                HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_INTERNET_RECOVERY_CONFIRM, 1000);
            } else if (requestCureLevel == HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP) {
                HwSelfCureEngine.this.LOGD("begin to self cure for internet access: RESET_LEVEL_LOW_2_RENEW_DHCP");
                HwSelfCureEngine.this.mDhcpOfferPackets.clear();
                HwSelfCureEngine.this.mDhcpResultsTestDone.clear();
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                this.mTestedSelfCureLevel.add(Integer.valueOf(requestCureLevel));
                HwSelfCureEngine.this.mWifiStateMachine.requestRenewDhcp();
                HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_IP_CONFIG_TIMEOUT, 8000);
            } else if (requestCureLevel == HwSelfCureUtils.RESET_LEVEL_LOW_3_STATIC_IP) {
                String dhcpResults = null;
                if (this.mConfigStaticIp4MultiDhcpServer) {
                    dhcpResults = getNextTestDhcpResults();
                } else if (this.mConfigStaticIp4GatewayChanged) {
                    dhcpResults = HwSelfCureEngine.this.getDhcpResultsHasInternet(HwSelfCureEngine.this.mWifiStateMachine.getCurrentBSSID(), HwSelfCureEngine.this.mSelfCureConfig);
                }
                String gatewayKey = WifiProCommonUtils.dhcpResults2Gateway(dhcpResults);
                if (dhcpResults != null && gatewayKey != null) {
                    gatewayKey = gatewayKey.replace("/", "");
                    HwSelfCureEngine.this.LOGD("begin to self cure for internet access: TRY_NEXT_DHCP_OFFER, gatewayKey = " + gatewayKey);
                    HwSelfCureEngine.this.mDhcpResultsTestDone.add(gatewayKey);
                    StaticIpConfiguration staticIpConfig = WifiProCommonUtils.dhcpResults2StaticIpConfig(dhcpResults);
                    HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                    this.mTestedSelfCureLevel.add(Integer.valueOf(requestCureLevel));
                    HwSelfCureEngine.this.mWifiStateMachine.requestUseStaticIpConfig(staticIpConfig);
                    HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_IP_CONFIG_TIMEOUT, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                }
            } else if (requestCureLevel == HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC) {
                if (this.mCurrentRssi <= -80 || HwSelfCureEngine.this.mP2pConnected.get()) {
                    this.mDelayedReassocSelfCure = true;
                    return;
                }
                HwSelfCureEngine.this.LOGD("begin to self cure for internet access: RESET_LEVEL_MIDDLE_REASSOC");
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                this.mTestedSelfCureLevel.add(Integer.valueOf(requestCureLevel));
                this.mDelayedReassocSelfCure = false;
                HwSelfCureEngine.this.mWifiStateMachine.requestReassocLink();
            } else if (requestCureLevel != HwSelfCureUtils.RESET_LEVEL_HIGH_RESET) {
            } else {
                if (this.mCurrentRssi <= -76 || HwSelfCureEngine.this.mP2pConnected.get()) {
                    this.mDelayedResetSelfCure = true;
                    return;
                }
                HwSelfCureEngine.this.LOGD("begin to self cure for internet access: RESET_LEVEL_HIGH_RESET");
                HwSelfCureEngine.this.mDhcpOfferPackets.clear();
                HwSelfCureEngine.this.mDhcpResultsTestDone.clear();
                HwSelfCureEngine.this.mSelfCureOngoing.set(true);
                this.mDelayedResetSelfCure = false;
                this.mTestedSelfCureLevel.add(Integer.valueOf(requestCureLevel));
                HwSelfCureEngine.this.mWifiStateMachine.requestResetWifi();
            }
        }
    }

    public static synchronized HwSelfCureEngine getInstance(Context context, WifiStateMachine wsm) {
        HwSelfCureEngine hwSelfCureEngine;
        synchronized (HwSelfCureEngine.class) {
            if (mHwSelfCureEngine == null) {
                mHwSelfCureEngine = new HwSelfCureEngine(context, wsm);
            }
            hwSelfCureEngine = mHwSelfCureEngine;
        }
        return hwSelfCureEngine;
    }

    public static synchronized HwSelfCureEngine getInstance() {
        HwSelfCureEngine hwSelfCureEngine;
        synchronized (HwSelfCureEngine.class) {
            hwSelfCureEngine = mHwSelfCureEngine;
        }
        return hwSelfCureEngine;
    }

    private HwSelfCureEngine(Context context, WifiStateMachine wsm) {
        super(TAG);
        this.mContext = context;
        this.mWifiStateMachine = wsm;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mNetworkChecker = new HwNetworkPropertyChecker(context, this.mWifiManager, null, true, null, true);
        this.mHwWifiStatStore = HwWifiStatStoreImpl.getDefault();
        addState(this.mDefaultState);
        addState(this.mConnectedMonitorState, this.mDefaultState);
        addState(this.mDisconnectedMonitorState, this.mDefaultState);
        addState(this.mConnectionSelfCureState, this.mDefaultState);
        addState(this.mInternetSelfCureState, this.mDefaultState);
        setInitialState(this.mDisconnectedMonitorState);
        start();
    }

    public synchronized void setup() {
        if (!this.mInitialized) {
            this.mInitialized = true;
            LOGD("setup DONE!");
            registerReceivers();
        }
    }

    public void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        intentFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        intentFilter.addAction("com.hw.wifipro.action.DHCP_OFFER_INFO");
        intentFilter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.net.wifi.SCAN_RESULTS".equals(intent.getAction())) {
                    if (!intent.getBooleanExtra("resultsUpdated", false)) {
                        return;
                    }
                    if (HwSelfCureEngine.this.getCurrentState() == HwSelfCureEngine.this.mConnectedMonitorState) {
                        HwSelfCureEngine.this.sendMessage(127);
                    } else {
                        HwSelfCureEngine.this.sendMessage(101);
                    }
                } else if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (info != null && info.getDetailedState() == DetailedState.DISCONNECTED) {
                        HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_NETWORK_DISCONNECTED_RCVD);
                    } else if (info != null && info.getDetailedState() == DetailedState.CONNECTED) {
                        HwSelfCureEngine.this.sendMessage(104);
                    }
                } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction())) {
                    if (HwSelfCureEngine.this.mWifiManager.isWifiEnabled()) {
                        HwSelfCureEngine.this.sendMessageDelayed(HwSelfCureEngine.CMD_WIFI_ENABLED_RCVD, 1000);
                    } else {
                        HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_WIFI_DISABLED_RCVD);
                    }
                } else if ("android.net.wifi.RSSI_CHANGED".equals(intent.getAction())) {
                    int newRssi = intent.getIntExtra("newRssi", -127);
                    if (newRssi != -127) {
                        HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_NEW_RSSI_RCVD, newRssi, 0);
                    }
                } else if ("android.net.wifi.CONFIGURED_NETWORKS_CHANGE".equals(intent.getAction())) {
                    WifiConfiguration config = (WifiConfiguration) intent.getParcelableExtra("wifiConfiguration");
                    if (intent.getIntExtra("changeReason", 0) == 1) {
                        HwSelfCureEngine.this.sendMessage(HwSelfCureEngine.CMD_CONFIGURED_NETWORK_DELETED, config);
                    }
                } else if ("com.hw.wifipro.action.DHCP_OFFER_INFO".equals(intent.getAction())) {
                    String dhcpResults = intent.getStringExtra("com.hw.wifipro.FLAG_DHCP_OFFER_INFO");
                    if (dhcpResults != null) {
                        HwSelfCureEngine.this.sendMessage(126, dhcpResults);
                    }
                } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(intent.getAction())) {
                    NetworkInfo p2pNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (p2pNetworkInfo != null && p2pNetworkInfo.isConnectedOrConnecting()) {
                        HwSelfCureEngine.this.mP2pConnected.set(true);
                    } else if (p2pNetworkInfo != null && p2pNetworkInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                        HwSelfCureEngine.this.mP2pConnected.set(false);
                        if (HwSelfCureEngine.this.getCurrentState() == HwSelfCureEngine.this.mInternetSelfCureState) {
                            HwSelfCureEngine.this.sendMessage(128);
                        }
                    }
                }
            }
        }, intentFilter);
    }

    private String getDhcpResultsHasInternet(String currentBssid, WifiConfiguration config) {
        String dhcpResults = null;
        if (currentBssid != null) {
            PortalDataBaseManager database = PortalDataBaseManager.getInstance(this.mContext);
            if (database != null) {
                dhcpResults = database.syncQueryDhcpResultsByBssid(currentBssid);
            }
        }
        if (dhcpResults != null || config == null) {
            return dhcpResults;
        }
        return config.lastDhcpResults;
    }

    private void notifyHttpReachableForWifiPro(boolean httpReachable) {
        WifiProStateMachine wifiProStateMachine = WifiProStateMachine.getWifiProStateMachineImpl();
        if (wifiProStateMachine != null) {
            wifiProStateMachine.notifyHttpReachable(httpReachable);
        }
    }

    private boolean isConnectingOrConnected() {
        WifiInfo info = this.mWifiStateMachine.getWifiInfo();
        if (info == null || info.getSupplicantState().ordinal() < SupplicantState.AUTHENTICATING.ordinal()) {
            return false;
        }
        LOGD("Supplicant is connectingOrConnected, no need to self cure for auto connection.");
        this.autoConnectFailedNetworks.clear();
        this.autoConnectFailedNetworksRssi.clear();
        this.mNoAutoConnCounter = 0;
        return true;
    }

    private boolean isSuppOnCompletedState() {
        WifiInfo info = this.mWifiStateMachine.getWifiInfo();
        if (info == null || info.getSupplicantState().ordinal() != SupplicantState.COMPLETED.ordinal()) {
            return false;
        }
        return true;
    }

    private void handleNetworkConnected() {
        LOGD("ENTER: handleNetworkConnected()");
        if (!updateConnSelfCureFailedHistory()) {
            LOGD("handleNetworkConnected, config is null for update, delay 2s to update again.");
            sendMessageDelayed(CMD_UPDATE_CONN_SELF_CURE_HISTORY, 2000);
        }
        this.mNoAutoConnCounter = 0;
        this.autoConnectFailedNetworks.clear();
        this.autoConnectFailedNetworksRssi.clear();
        enableAllNetowkrsByReason(new int[]{1, 8, 9});
        this.mConnectedTimeMills = System.currentTimeMillis();
        transitionTo(this.mConnectedMonitorState);
    }

    private boolean updateConnSelfCureFailedHistory() {
        WifiConfiguration config = WifiProCommonUtils.getCurrentWifiConfig(this.mWifiManager);
        if (config == null || config.configKey() == null) {
            return false;
        }
        this.networkCureFailedHistory.remove(config.configKey());
        LOGD("updateConnSelfCureFailedHistory(), networkCureFailedHistory remove " + config.configKey());
        if (this.mNoAutoConnConfig != null && config.configKey().equals(this.mNoAutoConnConfig.configKey())) {
            if (!(this.mNoAutoConnReason == -1 || this.mHwWifiStatStore == null)) {
                this.mHwWifiStatStore.updateScCHRCount(this.mNoAutoConnReason);
            }
            int chrType = -1;
            if (this.mNoAutoConnReason == 10) {
                chrType = 14;
            } else if (this.mNoAutoConnReason == 11) {
                chrType = 15;
            } else if (this.mNoAutoConnReason == 12) {
                chrType = 16;
            } else if (this.mNoAutoConnReason == 13) {
                chrType = 17;
            } else if (this.mNoAutoConnReason == 24) {
                chrType = 23;
            }
            if (!(chrType == -1 || this.mHwWifiStatStore == null)) {
                this.mHwWifiStatStore.updateScCHRCount(chrType);
            }
        }
        return true;
    }

    private void enableAllNetowkrsByReason(int[] enabledReasons) {
        List<WifiConfiguration> savedNetworks = this.mWifiManager.getConfiguredNetworks();
        if (savedNetworks == null || savedNetworks.size() == 0) {
            LOGD("enableAllNetowkrsByReason, no saved networks found.");
            return;
        }
        for (int i = 0; i < savedNetworks.size(); i++) {
            WifiConfiguration nextConfig = (WifiConfiguration) savedNetworks.get(i);
            NetworkSelectionStatus status = nextConfig.getNetworkSelectionStatus();
            int disableReason = status.getNetworkSelectionDisableReason();
            if (!status.isNetworkEnabled()) {
                for (int i2 : enabledReasons) {
                    if (disableReason == i2) {
                        LOGD("To enable network which status is " + disableReason + ", config = " + nextConfig.configKey() + ", id = " + nextConfig.networkId);
                        this.mWifiManager.enableNetwork(nextConfig.networkId, false);
                        break;
                    }
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isHttpReachable(boolean useDoubleServers) {
        synchronized (this.mNetworkChecker) {
            int mainSvrRespCode = this.mNetworkChecker.isCaptivePortal(true);
            int mainSvrRawRespCode = this.mNetworkChecker.getRawHttpRespCode();
            if (!WifiProCommonUtils.unreachableRespCode(mainSvrRespCode) || mainSvrRawRespCode == 602) {
            } else if (useDoubleServers) {
                int backupSvrRespCode = this.mNetworkChecker.recheckWithBakcupServer(true);
                int backupSvrRawRespCode = this.mNetworkChecker.getRawHttpRespCode();
                if (!WifiProCommonUtils.unreachableRespCode(backupSvrRespCode) || backupSvrRawRespCode == 602) {
                }
            }
        }
    }

    private void handleWifiDisabled(boolean selfCureGoing) {
        LOGD("ENTER: handleWifiDisabled(), selfCureGoing = " + selfCureGoing);
        this.mNoAutoConnCounter = 0;
        this.autoConnectFailedNetworks.clear();
        this.autoConnectFailedNetworksRssi.clear();
        this.networkCureFailedHistory.clear();
        if (selfCureGoing) {
            transitionTo(this.mDisconnectedMonitorState);
        }
    }

    private void handleWifiEnabled() {
        LOGD("ENTER: handleWifiEnabled()");
        enableAllNetowkrsByReason(new int[]{1, 8, 9});
    }

    private void handleNetworkRemoved(WifiConfiguration config) {
        if (config != null) {
            this.networkCureFailedHistory.remove(config.configKey());
            this.autoConnectFailedNetworks.remove(config.configKey());
            this.autoConnectFailedNetworksRssi.remove(config.configKey());
        }
    }

    private boolean hasDhcpResultsSaved(WifiConfiguration config) {
        return WifiProCommonUtils.dhcpResults2StaticIpConfig(config.lastDhcpResults) != null;
    }

    public synchronized void notifyWifiScanResultsAvailable(boolean success) {
        if (this.mInitialized && success) {
            sendMessage(101);
        }
    }

    public synchronized void notifyDhcpResultsInternetOk(String dhcpResults) {
        if (this.mInitialized && dhcpResults != null) {
            sendMessage(125, dhcpResults);
        }
    }

    public synchronized void notifyWifiConnectedBackground() {
        LOGD("ENTER: notifyWifiConnectedBackground()");
        if (this.mInitialized) {
            this.mIsWifiBackground.set(true);
            this.mIpConfigLostCnt = 0;
            sendMessage(104);
        }
    }

    public synchronized void notifyWifiRoamingCompleted(String bssid) {
        LOGD("ENTER: notifyWifiRoamingCompleted()");
        if (this.mInitialized && bssid != null) {
            sendMessageDelayed(110, bssid, 2000);
        }
    }

    public synchronized void notifySefCureCompleted(int status) {
        LOGD("ENTER: notifySefCureCompleted, status = " + status);
        if (this.mInitialized && 1 == status) {
            sendMessage(CMD_INTERNET_RECOVERY_CONFIRM);
        } else if (status == 0) {
            sendMessage(120);
        }
    }

    public synchronized void notifyTcpStatResults(long deltaRx, long deltaTx, long deltaReTx) {
        if (this.mInitialized) {
            if (deltaTx < 3 || deltaRx != 0) {
                if (deltaRx > 0) {
                    this.mNoTcpRxCounter = 0;
                    if (hasMessages(121)) {
                        removeMessages(121);
                    }
                }
            } else if (!hasMessages(121)) {
                sendMessage(121, (int) deltaTx, (int) deltaReTx);
            }
        }
    }

    public synchronized void notifyWifiDisconnected() {
        LOGD("ENTER: notifyWifiDisconnected()");
        if (this.mInitialized) {
            sendMessage(CMD_NETWORK_DISCONNECTED_RCVD);
        }
    }

    public synchronized void notifyIpConfigCompleted() {
        if (this.mInitialized) {
            LOGD("ENTER: notifyIpConfigCompleted()");
            this.mIpConfigLostCnt = 0;
            sendMessage(CMD_IP_CONFIG_COMPLETED);
        }
    }

    public synchronized boolean notifyIpConfigLostAndHandle(WifiConfiguration config) {
        if (this.mInitialized && config != null) {
            LOGD("ENTER: notifyIpConfigLostAndHandle() IpConfigLostCnt = " + this.mIpConfigLostCnt + ", ssid = " + config.SSID);
            if (getCurrentState() == this.mDisconnectedMonitorState) {
                int i = this.mIpConfigLostCnt + 1;
                this.mIpConfigLostCnt = i;
                if (i == 2 && hasDhcpResultsSaved(config)) {
                    sendMessage(124, config);
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized void requestChangeWifiStatus(boolean enabled) {
        if (this.mInitialized && this.mWifiManager != null) {
            this.mWifiManager.setWifiEnabled(enabled);
        }
    }

    public synchronized boolean isSelfCureOngoing() {
        if (!this.mInitialized) {
            return false;
        }
        return this.mSelfCureOngoing.get();
    }

    public synchronized void notifyInternetFailureDetected(boolean forceNoHttpCheck) {
        if (this.mInitialized) {
            LOGD("ENTER: notifyInternetFailureDetected, forceNoHttpCheck = " + forceNoHttpCheck);
            sendMessage(122, Boolean.valueOf(forceNoHttpCheck));
        }
    }

    public void LOGD(String msg) {
        Log.d(TAG, msg);
    }
}
