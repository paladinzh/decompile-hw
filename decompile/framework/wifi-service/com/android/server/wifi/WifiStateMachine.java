package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.DhcpResults;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkMisc;
import android.net.NetworkRequest;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.StaticIpConfiguration;
import android.net.dhcp.DhcpClient;
import android.net.dhcp.HwDhcpClient;
import android.net.ip.IpManager;
import android.net.ip.IpManager.Callback;
import android.net.ip.IpManager.ProvisioningConfiguration;
import android.net.wifi.PasspointManagementObjectDefinition;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.ScanSettings;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiChannel;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.net.wifi.WifiConnectionStatistics;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiLinkLayerStats;
import android.net.wifi.WifiScanner;
import android.net.wifi.WifiScanner.ChannelSpec;
import android.net.wifi.WifiScanner.ScanData;
import android.net.wifi.WifiScanner.ScanListener;
import android.net.wifi.WifiSsid;
import android.net.wifi.WpsInfo;
import android.net.wifi.WpsResult;
import android.net.wifi.WpsResult.Status;
import android.net.wifi.p2p.IWifiP2pManager;
import android.net.wifi.wifipro.HwNetworkAgent;
import android.os.Binder;
import android.os.Bundle;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.telephony.HwTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.app.IBatteryStats;
import com.android.internal.app.IBatteryStats.Stub;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.State;
import com.android.server.connectivity.KeepalivePacketData;
import com.android.server.wifi.SoftApManager.Listener;
import com.android.server.wifi.WifiNative.WifiRssiEventHandler;
import com.android.server.wifi.hotspot2.IconEvent;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.hotspot2.omadm.PasspointManagementObjectManager;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.util.ScanDetailUtil;
import huawei.cust.HwCustUtils;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WifiStateMachine extends AbsWifiStateMachine implements WifiRssiEventHandler {
    private static final int ADD_OR_UPDATE_SOURCE = -3;
    private static final long ALLOW_SEND_SCAN_RESULTS_BROADCAST_INTERVAL_MS = 3000;
    static final int BASE = 131072;
    static final int CMD_ACCEPT_UNVALIDATED = 131225;
    static final int CMD_ADD_OR_UPDATE_NETWORK = 131124;
    static final int CMD_ADD_PASSPOINT_MO = 131174;
    static final int CMD_AP_STARTED_GET_STA_LIST = 131104;
    static final int CMD_AP_STARTED_SET_DISASSOCIATE_STA = 131106;
    static final int CMD_AP_STARTED_SET_MAC_FILTER = 131105;
    static final int CMD_AP_STOPPED = 131096;
    static final int CMD_ASSOCIATED_BSSID = 131219;
    static final int CMD_AUTO_CONNECT = 131215;
    static final int CMD_AUTO_ROAM = 131217;
    static final int CMD_AUTO_SAVE_NETWORK = 131218;
    static final int CMD_BLACKLIST_NETWORK = 131128;
    static final int CMD_BLUETOOTH_ADAPTER_STATE_CHANGE = 131103;
    public static final int CMD_BOOT_COMPLETED = 131206;
    public static final int CMD_CHANGE_TO_AP_P2P_CONNECT = 131574;
    public static final int CMD_CHANGE_TO_STA_P2P_CONNECT = 131573;
    static final int CMD_CLEAR_BLACKLIST = 131129;
    static final int CMD_CONFIG_ND_OFFLOAD = 131276;
    static final int CMD_DELAYED_NETWORK_DISCONNECT = 131159;
    static final int CMD_DISABLE_EPHEMERAL_NETWORK = 131170;
    public static final int CMD_DISABLE_P2P_REQ = 131204;
    public static final int CMD_DISABLE_P2P_RSP = 131205;
    static final int CMD_DISCONNECT = 131145;
    static final int CMD_DISCONNECTING_WATCHDOG_TIMER = 131168;
    static final int CMD_DRIVER_START_TIMED_OUT = 131091;
    static final int CMD_ENABLE_ALL_NETWORKS = 131127;
    static final int CMD_ENABLE_AUTOJOIN_WHEN_ASSOCIATED = 131239;
    static final int CMD_ENABLE_NETWORK = 131126;
    public static final int CMD_ENABLE_P2P = 131203;
    static final int CMD_ENABLE_RSSI_POLL = 131154;
    static final int CMD_ENABLE_TDLS = 131164;
    static final int CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER = 131238;
    static final int CMD_FIRMWARE_ALERT = 131172;
    static final int CMD_GET_CAPABILITY_FREQ = 131132;
    static final int CMD_GET_CHANNEL_LIST_5G = 131572;
    static final int CMD_GET_CONFIGURED_NETWORKS = 131131;
    static final int CMD_GET_CONNECTION_STATISTICS = 131148;
    static final int CMD_GET_LINK_LAYER_STATS = 131135;
    static final int CMD_GET_MATCHING_CONFIG = 131171;
    static final int CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS = 131134;
    static final int CMD_GET_SUPPORTED_FEATURES = 131133;
    static final int CMD_GET_SUPPORT_VOWIFI_DETECT = 131774;
    static final int CMD_INSTALL_PACKET_FILTER = 131274;
    static final int CMD_IPV4_PROVISIONING_FAILURE = 131273;
    static final int CMD_IPV4_PROVISIONING_SUCCESS = 131272;
    static final int CMD_IP_CONFIGURATION_LOST = 131211;
    static final int CMD_IP_CONFIGURATION_SUCCESSFUL = 131210;
    static final int CMD_IP_REACHABILITY_LOST = 131221;
    static final int CMD_MATCH_PROVIDER_NETWORK = 131177;
    static final int CMD_MODIFY_PASSPOINT_MO = 131175;
    static final int CMD_NETWORK_STATUS = 131220;
    static final int CMD_NO_NETWORKS_PERIODIC_SCAN = 131160;
    static final int CMD_OBTAINING_IP_ADDRESS_WATCHDOG_TIMER = 131165;
    static final int CMD_PING_SUPPLICANT = 131123;
    static final int CMD_PNO_PERIODIC_SCAN = 131575;
    static final int CMD_QUERY_OSU_ICON = 131176;
    static final int CMD_REASSOCIATE = 131147;
    static final int CMD_RECONNECT = 131146;
    static final int CMD_RELOAD_TLS_AND_RECONNECT = 131214;
    static final int CMD_REMOVE_APP_CONFIGURATIONS = 131169;
    static final int CMD_REMOVE_NETWORK = 131125;
    static final int CMD_REMOVE_USER_CONFIGURATIONS = 131224;
    static final int CMD_RESET_SIM_NETWORKS = 131173;
    static final int CMD_RESET_SUPPLICANT_STATE = 131183;
    static final int CMD_ROAM_WATCHDOG_TIMER = 131166;
    static final int CMD_RSSI_POLL = 131155;
    static final int CMD_RSSI_THRESHOLD_BREACH = 131236;
    static final int CMD_SAVE_CONFIG = 131130;
    public static final int CMD_SCE_NOTIFY_WIFI_DISABLED = 131894;
    public static final int CMD_SCE_RESTORE = 131893;
    public static final int CMD_SCE_STOP_SELF_CURE = 131892;
    public static final int CMD_SCE_WIFI_CONNECT_TIMEOUT = 131890;
    public static final int CMD_SCE_WIFI_OFF_TIMEOUT = 131888;
    public static final int CMD_SCE_WIFI_ON_TIMEOUT = 131889;
    public static final int CMD_SCE_WIFI_REASSOC_TIMEOUT = 131891;
    static final int CMD_SCREEN_STATE_CHANGED = 131167;
    static final int CMD_SET_DETECTMODE_CONF = 131772;
    static final int CMD_SET_DETECT_PERIOD = 131773;
    static final int CMD_SET_FALLBACK_PACKET_FILTERING = 131275;
    static final int CMD_SET_FREQUENCY_BAND = 131162;
    static final int CMD_SET_HIGH_PERF_MODE = 131149;
    static final int CMD_SET_OPERATIONAL_MODE = 131144;
    static final int CMD_SET_SUSPEND_OPT_ENABLED = 131158;
    static final int CMD_START_AP = 131093;
    static final int CMD_START_AP_FAILURE = 131094;
    static final int CMD_START_DRIVER = 131085;
    static final int CMD_START_IP_PACKET_OFFLOAD = 131232;
    static final int CMD_START_RSSI_MONITORING_OFFLOAD = 131234;
    static final int CMD_START_SCAN = 131143;
    static final int CMD_START_SUPPLICANT = 131083;
    static final int CMD_STATIC_IP_FAILURE = 131088;
    static final int CMD_STATIC_IP_SUCCESS = 131087;
    static final int CMD_STOP_AP = 131095;
    static final int CMD_STOP_DRIVER = 131086;
    static final int CMD_STOP_IP_PACKET_OFFLOAD = 131233;
    static final int CMD_STOP_RSSI_MONITORING_OFFLOAD = 131235;
    static final int CMD_STOP_SUPPLICANT = 131084;
    static final int CMD_STOP_SUPPLICANT_FAILED = 131089;
    public static final int CMD_STOP_WIFI_REPEATER = 131577;
    static final int CMD_TARGET_BSSID = 131213;
    static final int CMD_TEST_NETWORK_DISCONNECT = 131161;
    static final int CMD_UNWANTED_NETWORK = 131216;
    static final int CMD_UPDATE_ASSOCIATED_SCAN_PERMISSION = 131230;
    static final int CMD_UPDATE_LINKPROPERTIES = 131212;
    static final int CMD_UPDATE_WIFIPRO_CONFIGURATIONS = 131672;
    static final int CMD_USER_SWITCH = 131237;
    public static final int CMD_WIFI_SCAN_REJECT_SEND_SCAN_RESULT = 131578;
    static final int CMD_WPS_PIN_RETRY = 131576;
    public static final int CONNECT_MODE = 1;
    private static final int CONNECT_TIMEOUT_MSEC = 3000;
    private static final String CUSTOMIZED_SCAN_SETTING = "customized_scan_settings";
    private static final String CUSTOMIZED_SCAN_WORKSOURCE = "customized_scan_worksource";
    private static boolean DBG = HWFLOW;
    private static final boolean DEBUG_PARSE = false;
    private static final int DEFAULT_WIFI_AP_CHANNEL = 0;
    private static final int DEFAULT_WIFI_AP_MAXSCB = 8;
    public static final int DFS_RESTRICTED_SCAN_REQUEST = -6;
    static final int DISCONNECTING_GUARD_TIMER_MSEC = 5000;
    private static final int DRIVER_START_TIME_OUT_MSECS = 10000;
    private static final int ENABLE_WIFI = -5;
    private static final int FAILURE = -1;
    public static final int GOOD_LINK_DETECTED = 131874;
    private static final String GOOGLE_OUI = "DA-A1-19";
    private static final int HILINK_REJECT_SEND_TIME = 300000;
    private static final String HILINK_STATE_CHANGE_ACTION = "com.android.server.wifi.huawei.action.NETWORK_CONNECTED";
    protected static final boolean HWFLOW;
    private static boolean HWLOGW_E = true;
    private static final int LINK_FLAPPING_DEBOUNCE_MSEC = 4000;
    private static final String LOGD_LEVEL_DEBUG = "D";
    private static final String LOGD_LEVEL_VERBOSE = "V";
    private static final int LONGSCAN_INTERVALMS = 30000;
    private static int MESSAGE_HANDLING_STATUS_DEFERRED = SET_ALLOW_UNTRUSTED_SOURCE;
    private static int MESSAGE_HANDLING_STATUS_DISCARD = ENABLE_WIFI;
    private static int MESSAGE_HANDLING_STATUS_FAIL = -2;
    private static int MESSAGE_HANDLING_STATUS_HANDLING_ERROR = -7;
    private static int MESSAGE_HANDLING_STATUS_LOOPED = -6;
    private static int MESSAGE_HANDLING_STATUS_OBSOLETE = ADD_OR_UPDATE_SOURCE;
    private static int MESSAGE_HANDLING_STATUS_OK = 1;
    private static int MESSAGE_HANDLING_STATUS_PROCESSED = 2;
    private static int MESSAGE_HANDLING_STATUS_REFUSED = -1;
    private static int MESSAGE_HANDLING_STATUS_UNKNOWN = 0;
    private static final int MIN_INTERVAL_ENABLE_ALL_NETWORKS_MS = 600000;
    private static final String NETWORKTYPE = "WIFI";
    private static final String NETWORKTYPE_UNTRUSTED = "WIFI_UT";
    private static final int NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN = 2;
    private static final int NETWORK_STATUS_UNWANTED_DISCONNECT = 0;
    private static final int NETWORK_STATUS_UNWANTED_VALIDATION_FAILED = 1;
    public static final short NUM_LOG_RECS_NORMAL = (short) 100;
    public static final short NUM_LOG_RECS_VERBOSE = (short) 3000;
    public static final short NUM_LOG_RECS_VERBOSE_LOW_MEMORY = (short) 200;
    static final int OBTAINING_IP_ADDRESS_GUARD_TIMER_MSEC = 40000;
    private static final int ONE_HOUR_MILLI = 3600000;
    private static boolean PDBG = HWFLOW;
    private static final int POLL_RSSI_INTERVAL_MSECS = 3000;
    public static final int POOR_LINK_DETECTED = 131873;
    public static final int PORTAL_NOTIFY_CHANGED = 131875;
    static final int ROAM_GUARD_TIMER_MSEC = 15000;
    public static final int SCAN_ONLY_MODE = 2;
    public static final int SCAN_ONLY_WITH_WIFI_OFF_MODE = 3;
    static final long SCAN_PERMISSION_UPDATE_THROTTLE_MILLI = 20000;
    private static final int SCAN_REQUEST = 0;
    private static final int SCAN_REQUEST_BUFFER_MAX_SIZE = 10;
    private static final String SCAN_REQUEST_TIME = "scan_request_time";
    public static final int SCE_REQUEST_REASSOC_WIFI = 131886;
    public static final int SCE_REQUEST_RENEW_DHCP = 131883;
    public static final int SCE_REQUEST_RESET_WIFI = 131887;
    public static final int SCE_REQUEST_SET_STATIC_IP = 131884;
    public static final int SCE_REQUEST_UPDATE_DNS_SERVER = 131882;
    public static final int SCE_START_SET_STATIC_IP = 131885;
    private static final int SET_ALLOW_UNTRUSTED_SOURCE = -4;
    private static final int SHORTSCAN_INTERVALMS = 15000;
    private static final int SHORTSCAN_MAXCOUNT = 2;
    private static final String SOFTAP_IFACE = "wlan0";
    private static final int SUCCESS = 1;
    private static final int SUPPLICANT_RESTART_INTERVAL_MSECS = 5000;
    private static final int SUPPLICANT_RESTART_TRIES = 5;
    private static final int SUSPEND_DUE_TO_DHCP = 1;
    private static final int SUSPEND_DUE_TO_HIGH_PERF = 2;
    private static final int SUSPEND_DUE_TO_SCREEN = 4;
    private static final String SYSTEM_PROPERTY_LOG_CONTROL_WIFIHAL = "log.tag.WifiHAL";
    private static final String TAG = "WifiStateMachine";
    private static final int TETHER_NOTIFICATION_TIME_OUT_MSECS = 5000;
    private static final int UNKNOWN_SCAN_SOURCE = -1;
    private static boolean USE_PAUSE_SCANS = false;
    private static boolean VDBG = false;
    private static boolean VVDBG = false;
    public static final WorkSource WIFI_WORK_SOURCE = new WorkSource(1010);
    private static final int WPS_PIN_RETRY_INTERVAL_MSECS = 50000;
    private static boolean mLogMessages = HWFLOW;
    private static Random mRandom = new Random(Calendar.getInstance().getTimeInMillis());
    private static final int sFrameworkMinScanIntervalSaneValue = 10000;
    private static final Class[] sMessageClasses = new Class[]{AsyncChannel.class, WifiStateMachine.class, DhcpClient.class};
    private static int sScanAlarmIntentCount = 0;
    private static final SparseArray<String> sSmToString = MessageUtils.findMessageNames(sMessageClasses);
    private boolean didBlackListBSSID = false;
    int disconnectingWatchdogCount = 0;
    private long lastConnectAttemptTimestamp = 0;
    private WifiConfiguration lastForgetConfigurationAttempt = null;
    private long lastLinkLayerStatsUpdate = 0;
    private long lastOntimeReportTimeStamp = 0;
    private WifiConfiguration lastSavedConfigurationAttempt = null;
    private Set<Integer> lastScanFreqs = null;
    private long lastScreenStateChangeTimeStamp = 0;
    private boolean linkDebouncing = false;
    private int mAggressiveHandover = 0;
    private AlarmManager mAlarmManager;
    private boolean mAutoRoaming = false;
    private final boolean mBackgroundScanSupported;
    private final BackupManagerProxy mBackupManagerProxy;
    private final IBatteryStats mBatteryStats;
    private boolean mBluetoothConnectionActive = false;
    private final Queue<Message> mBufferedScanMsg = new LinkedList();
    private final BuildProperties mBuildProperties;
    private final Clock mClock;
    private ConnectivityManager mCm;
    private State mConnectModeState = new ConnectModeState();
    boolean mConnectedModeGScanOffloadStarted = false;
    private State mConnectedState = new ConnectedState();
    private long mConnectingStartTimestamp = 0;
    private int mConnectionRequests = 0;
    private Context mContext;
    private final WifiCountryCode mCountryCode;
    private int mCurrentAssociateNetworkId = -1;
    HwCustWifiStateMachineReference mCust = ((HwCustWifiStateMachineReference) HwCustUtils.createObj(HwCustWifiStateMachineReference.class, new Object[0]));
    private final int mDefaultFrameworkScanIntervalMs;
    private State mDefaultState = new DefaultState();
    private final NetworkCapabilities mDfltNetworkCapabilities;
    private DhcpResults mDhcpResults;
    private final Object mDhcpResultsLock = new Object();
    private State mDisconnectedState = new DisconnectedState();
    private long mDisconnectedTimeStamp = 0;
    private State mDisconnectingState = new DisconnectingState();
    private int mDriverStartToken = 0;
    private State mDriverStartedState = new DriverStartedState();
    private State mDriverStartingState = new DriverStartingState();
    private State mDriverStoppedState = new DriverStoppedState();
    private State mDriverStoppingState = new DriverStoppingState();
    private int mEmptyScanResultCount = 0;
    private boolean mEnableRssiPolling = false;
    private FrameworkFacade mFacade;
    private int mFeatureSet = 0;
    private int mFrameworkShortScanCount = 0;
    private AtomicInteger mFrequencyBand = new AtomicInteger(0);
    private long mGScanPeriodMilli;
    private long mGScanStartTimeMilli;
    private long mHilinkLastConnectedTime = 0;
    private long mHilinkLastDisconnectedTime = 0;
    private long mHilinkLastHashCode = 0;
    private long mHilinkLastLevelCode = 0;
    private HwWifiCHRService mHwWifiCHRService;
    private HwWifiDFTUtil mHwWifiDFTUtil;
    private State mInitialState = new InitialState();
    private final String mInterfaceName;
    private final IpManager mIpManager;
    private boolean mIsFullScanOngoing = false;
    public boolean mIsRandomMacCleared = false;
    private boolean mIsRunning = false;
    private boolean mIsScanOngoing = false;
    private State mL2ConnectedState = new L2ConnectedState();
    private long mLastAllowSendingTime = 0;
    private String mLastBssid;
    private long mLastDriverRoamAttempt = 0;
    private long mLastEnableAllNetworksTime;
    private int mLastNetworkId;
    private final WorkSource mLastRunningWifiUids = new WorkSource();
    long mLastScanPermissionUpdate = 0;
    private int mLastSignalLevel = -1;
    private LinkProperties mLinkProperties;
    private WifiNetworkAgent mNetworkAgent;
    private final NetworkCapabilities mNetworkCapabilitiesFilter = new NetworkCapabilities();
    private WifiNetworkFactory mNetworkFactory;
    private NetworkInfo mNetworkInfo;
    private final NetworkMisc mNetworkMisc = new NetworkMisc();
    private final int mNoNetworksPeriodicScan;
    private int mNumScanResultsKnown;
    private int mNumScanResultsReturned;
    private INetworkManagementService mNwService;
    private State mObtainingIpState = new ObtainingIpState();
    private int mOnTime = 0;
    private int mOnTimeLastReport = 0;
    private int mOnTimeScreenStateChange = 0;
    private int mOperationalMode = 1;
    private final AtomicBoolean mP2pConnected = new AtomicBoolean(false);
    private final boolean mP2pSupported;
    private int mPeriodicScanToken = 0;
    private final String mPrimaryDeviceType;
    private final PropertyService mPropertyService;
    private AsyncChannel mReplyChannel = new AsyncChannel();
    private boolean mReportedRunning = false;
    private int mRoamFailCount = 0;
    private State mRoamingState = new RoamingState();
    private int mRssiPollToken = 0;
    private byte[] mRssiRanges;
    int mRunningBeaconCount = 0;
    private final WorkSource mRunningWifiUids = new WorkSource();
    private int mRxTime = 0;
    private int mRxTimeLastReport = 0;
    private PendingIntent mScanIntent;
    private State mScanModeState = new ScanModeState();
    private List<ScanDetail> mScanResults = new ArrayList();
    private final Object mScanResultsLock = new Object();
    private final boolean mScanUpdate = SystemProperties.get("persist.sys.wifi.scanupdate", "false").equalsIgnoreCase("true");
    private WorkSource mScanWorkSource = null;
    private AtomicBoolean mScreenBroadcastReceived = new AtomicBoolean(false);
    private boolean mScreenOn = false;
    private boolean mSendScanResultsBroadcast = false;
    private State mSoftApState = new SoftApState();
    private int mSupplicantRestartCount = 0;
    private long mSupplicantScanIntervalMs;
    private State mSupplicantStartedState = new SupplicantStartedState();
    private State mSupplicantStartingState = new SupplicantStartingState();
    private SupplicantStateTracker mSupplicantStateTracker;
    private int mSupplicantStopFailureToken = 0;
    private State mSupplicantStoppingState = new SupplicantStoppingState();
    private int mSuspendOptNeedsDisabled = 0;
    private WakeLock mSuspendWakeLock;
    private int mSystemUiUid = -1;
    private int mTargetNetworkId = -1;
    private String mTargetRoamBSSID = WifiLastResortWatchdog.BSSID_ANY;
    private final String mTcpBufferSizes;
    private boolean mTemporarilyDisconnectWifi = false;
    private String mTetherInterfaceName;
    private int mTetherToken = 0;
    private String mTls12ConfKey = null;
    private int mTxTime = 0;
    private int mTxTimeLastReport = 0;
    private UntrustedWifiNetworkFactory mUntrustedNetworkFactory;
    private AtomicBoolean mUserWantsSuspendOpt = new AtomicBoolean(true);
    private int mVerboseLoggingLevel = 0;
    private State mWaitForP2pDisableState = new WaitForP2pDisableState();
    private WakeLock mWakeLock;
    private String[] mWhiteListedSsids = null;
    private HwWifiCHRStateManager mWiFiCHRManager;
    private WifiApConfigStore mWifiApConfigStore;
    private final AtomicInteger mWifiApState = new AtomicInteger(11);
    private WifiConfigManager mWifiConfigManager;
    private WifiConnectionStatistics mWifiConnectionStatistics = new WifiConnectionStatistics();
    private WifiConnectivityManager mWifiConnectivityManager;
    private final WifiInfo mWifiInfo;
    private WifiInjector mWifiInjector;
    private WifiLastResortWatchdog mWifiLastResortWatchdog;
    private int mWifiLinkLayerStatsSupported = 0;
    private BaseWifiLogger mWifiLogger;
    private WifiMetrics mWifiMetrics;
    private WifiMonitor mWifiMonitor;
    private WifiNative mWifiNative;
    private AsyncChannel mWifiP2pChannel;
    private WifiP2pServiceImpl mWifiP2pServiceImpl;
    private WifiQualifiedNetworkSelector mWifiQualifiedNetworkSelector;
    private WifiScanner mWifiScanner;
    WifiScoreReport mWifiScoreReport = null;
    private WifiSettingsStore mWifiSettingStore;
    private HwWifiStatStore mWifiStatStore;
    private final AtomicInteger mWifiState = new AtomicInteger(1);
    public WifiStateMachineHisiExt mWifiStateMachineHisiExt = null;
    private State mWpsRunningState = new WpsRunningState();
    private int messageHandlingStatus = 0;
    int obtainingIpWatchdogCount = 0;
    int roamWatchdogCount = 0;
    private WifiConfiguration targetWificonfiguration = null;
    private boolean testNetworkDisconnect = false;
    private int testNetworkDisconnectCounter = 0;
    private DataUploader uploader;

    class ConnectModeState extends State {
        ConnectModeState() {
        }

        public void enter() {
            if (((PowerManager) WifiStateMachine.this.mContext.getSystemService("power")).isScreenOn()) {
                WifiStateMachine.this.enableAllNetworks();
            }
            if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                WifiStateMachine.this.mWifiConnectivityManager.setWifiEnabled(true);
            }
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            if (WifiStateMachine.this.handleWapiFailureEvent(message, WifiStateMachine.this.mSupplicantStateTracker)) {
                return true;
            }
            WifiConfiguration config;
            int res;
            boolean persist;
            WifiConnectionStatistics -get86;
            int netId;
            boolean ok;
            boolean didDisconnect;
            switch (message.what) {
                case WifiStateMachine.CMD_ADD_OR_UPDATE_NETWORK /*131124*/:
                    if (!WifiStateMachine.this.mWifiConfigManager.isCurrentUserProfile(UserHandle.getUserId(message.sendingUid))) {
                        WifiStateMachine.this.loge("Only the current foreground user can modify networks  currentUserId=" + WifiStateMachine.this.mWifiConfigManager.getCurrentUserId() + " sendingUserId=" + UserHandle.getUserId(message.sendingUid));
                        WifiStateMachine.this.replyToMessage(message, message.what, -1);
                        break;
                    }
                    config = message.obj;
                    if (!WifiStateMachine.this.recordUidIfAuthorized(config, message.sendingUid, false)) {
                        WifiStateMachine.this.logw("Not authorized to update network  config=" + config.SSID + " cnid=" + config.networkId + " uid=" + message.sendingUid);
                        WifiStateMachine.this.replyToMessage(message, message.what, -1);
                        break;
                    }
                    res = WifiStateMachine.this.mWifiConfigManager.addOrUpdateNetwork(config, message.sendingUid);
                    if (res < 0) {
                        WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                    } else {
                        WifiConfiguration curConfig = WifiStateMachine.this.getCurrentWifiConfiguration();
                        if (!(curConfig == null || config == null)) {
                            NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
                            if (!(curConfig.priority >= config.priority || networkStatus == null || networkStatus.isNetworkPermanentlyDisabled())) {
                                WifiStateMachine.this.mWifiConfigManager.setAndEnableLastSelectedConfiguration(res);
                                WifiStateMachine.this.mWifiConfigManager.updateLastConnectUid(config, message.sendingUid);
                                persist = WifiStateMachine.this.mWifiConfigManager.checkConfigOverridePermission(message.sendingUid);
                                if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                                    WifiStateMachine.this.mWifiConnectivityManager.connectToUserSelectNetwork(res, persist);
                                }
                                WifiStateMachine.this.lastConnectAttemptTimestamp = System.currentTimeMillis();
                                -get86 = WifiStateMachine.this.mWifiConnectionStatistics;
                                -get86.numWifiManagerJoinAttempt++;
                                WifiStateMachine.this.startScan(WifiStateMachine.ADD_OR_UPDATE_SOURCE, 0, null, WifiStateMachine.WIFI_WORK_SOURCE);
                            }
                        }
                    }
                    WifiStateMachine.this.replyToMessage(message, WifiStateMachine.CMD_ADD_OR_UPDATE_NETWORK, res);
                    break;
                case WifiStateMachine.CMD_REMOVE_NETWORK /*131125*/:
                    if (!WifiStateMachine.this.mWifiConfigManager.isCurrentUserProfile(UserHandle.getUserId(message.sendingUid))) {
                        WifiStateMachine.this.loge("Only the current foreground user can modify networks  currentUserId=" + WifiStateMachine.this.mWifiConfigManager.getCurrentUserId() + " sendingUserId=" + UserHandle.getUserId(message.sendingUid));
                        WifiStateMachine.this.replyToMessage(message, message.what, -1);
                        break;
                    }
                    netId = message.arg1;
                    if (!WifiStateMachine.this.mWifiConfigManager.canModifyNetwork(message.sendingUid, netId, false)) {
                        WifiStateMachine.this.logw("Not authorized to remove network  cnid=" + netId + " uid=" + message.sendingUid);
                        WifiStateMachine.this.replyToMessage(message, message.what, -1);
                        break;
                    }
                    ok = WifiStateMachine.this.mWifiConfigManager.removeNetwork(message.arg1);
                    if (!ok) {
                        WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                    }
                    WifiStateMachine.this.replyToMessage(message, message.what, ok ? 1 : -1);
                    break;
                case WifiStateMachine.CMD_ENABLE_NETWORK /*131126*/:
                    if (!WifiStateMachine.this.mWifiConfigManager.isCurrentUserProfile(UserHandle.getUserId(message.sendingUid))) {
                        WifiStateMachine.this.loge("Only the current foreground user can modify networks  currentUserId=" + WifiStateMachine.this.mWifiConfigManager.getCurrentUserId() + " sendingUserId=" + UserHandle.getUserId(message.sendingUid));
                        WifiStateMachine.this.replyToMessage(message, message.what, -1);
                        break;
                    }
                    boolean disableOthers = message.arg2 == 1;
                    netId = message.arg1;
                    config = WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(netId);
                    if (config != null) {
                        if (disableOthers) {
                            WifiStateMachine.this.lastConnectAttemptTimestamp = System.currentTimeMillis();
                            -get86 = WifiStateMachine.this.mWifiConnectionStatistics;
                            -get86.numWifiManagerJoinAttempt++;
                            WifiStateMachine.this.saveConnectingNetwork(config);
                        }
                        WifiStateMachine.this.autoRoamSetBSSID(message.arg1, WifiLastResortWatchdog.BSSID_ANY);
                        if (!WifiStateMachine.this.processConnectModeSetMode(message)) {
                            WifiStateMachine.this.autoRoamSetBSSID(netId, WifiLastResortWatchdog.BSSID_ANY);
                            ok = WifiStateMachine.this.mWifiConfigManager.enableNetwork(config, disableOthers, message.sendingUid);
                            if (!ok) {
                                WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                            } else if (disableOthers) {
                                WifiStateMachine.this.mTargetNetworkId = netId;
                            }
                            WifiStateMachine.this.replyToMessage(message, message.what, ok ? 1 : -1);
                            break;
                        }
                    }
                    WifiStateMachine.this.loge("No network with id = " + netId);
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                    WifiStateMachine.this.replyToMessage(message, message.what, -1);
                    break;
                    break;
                case WifiStateMachine.CMD_ENABLE_ALL_NETWORKS /*131127*/:
                    long time = SystemClock.elapsedRealtime();
                    if (time - WifiStateMachine.this.mLastEnableAllNetworksTime > 600000) {
                        WifiStateMachine.this.mWifiConfigManager.enableAllNetworks();
                        WifiStateMachine.this.mLastEnableAllNetworksTime = time;
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_BLACKLIST_NETWORK /*131128*/:
                    WifiStateMachine.this.mWifiConfigManager.blackListBssid((String) message.obj);
                    break;
                case WifiStateMachine.CMD_CLEAR_BLACKLIST /*131129*/:
                    WifiStateMachine.this.mWifiConfigManager.clearBssidBlacklist();
                    break;
                case WifiStateMachine.CMD_SAVE_CONFIG /*131130*/:
                    ok = WifiStateMachine.this.mWifiConfigManager.saveConfig();
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.logd("did save config " + ok);
                    }
                    WifiStateMachine.this.replyToMessage(message, WifiStateMachine.CMD_SAVE_CONFIG, ok ? 1 : -1);
                    WifiStateMachine.this.mBackupManagerProxy.notifyDataChanged();
                    break;
                case WifiStateMachine.CMD_GET_CONFIGURED_NETWORKS /*131131*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.mWifiConfigManager.getSavedNetworks());
                    break;
                case WifiStateMachine.CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS /*131134*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.mWifiConfigManager.getPrivilegedSavedNetworks());
                    break;
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                    WifiStateMachine.this.mWifiConfigManager.setAndEnableLastSelectedConfiguration(-1);
                    WifiStateMachine.this.log("ConnectModeState, case CMD_DISCONNECT, do disconnect");
                    WifiStateMachine.this.mWifiNative.disconnect();
                    break;
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                    if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                        WifiStateMachine.this.mWifiConnectivityManager.forceConnectivityScan();
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                    WifiStateMachine.this.lastConnectAttemptTimestamp = System.currentTimeMillis();
                    WifiStateMachine.this.log("ConnectModeState, case CMD_REASSOCIATE, do reassociate");
                    WifiStateMachine.this.mWifiNative.reassociate();
                    break;
                case WifiStateMachine.CMD_REMOVE_APP_CONFIGURATIONS /*131169*/:
                    WifiStateMachine.this.mWifiConfigManager.removeNetworksForApp((ApplicationInfo) message.obj);
                    break;
                case WifiStateMachine.CMD_DISABLE_EPHEMERAL_NETWORK /*131170*/:
                    config = WifiStateMachine.this.mWifiConfigManager.disableEphemeralNetwork((String) message.obj);
                    if (config != null && config.networkId == WifiStateMachine.this.mLastNetworkId) {
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        break;
                    }
                case WifiStateMachine.CMD_GET_MATCHING_CONFIG /*131171*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.mWifiConfigManager.getMatchingConfig((ScanResult) message.obj));
                    break;
                case WifiStateMachine.CMD_ADD_PASSPOINT_MO /*131174*/:
                    if (message.obj != null) {
                        res = WifiStateMachine.this.mWifiConfigManager.addPasspointManagementObject((String) message.obj);
                    } else {
                        res = -1;
                    }
                    WifiStateMachine.this.replyToMessage(message, message.what, res);
                    break;
                case WifiStateMachine.CMD_MODIFY_PASSPOINT_MO /*131175*/:
                    if (message.obj != null) {
                        Bundle bundle = message.obj;
                        res = WifiStateMachine.this.mWifiConfigManager.modifyPasspointMo(bundle.getString(PasspointManagementObjectManager.TAG_FQDN), bundle.getParcelableArrayList("MOS"));
                    } else {
                        res = 0;
                    }
                    WifiStateMachine.this.replyToMessage(message, message.what, res);
                    break;
                case WifiStateMachine.CMD_QUERY_OSU_ICON /*131176*/:
                    if (WifiStateMachine.this.mWifiConfigManager.queryPasspointIcon(((Bundle) message.obj).getLong("BSSID"), ((Bundle) message.obj).getString("FILENAME"))) {
                        res = 1;
                    } else {
                        res = 0;
                    }
                    WifiStateMachine.this.replyToMessage(message, message.what, res);
                    break;
                case WifiStateMachine.CMD_MATCH_PROVIDER_NETWORK /*131177*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, WifiStateMachine.this.mWifiConfigManager.matchProviderWithCurrentNetwork((String) message.obj));
                    break;
                case WifiStateMachine.CMD_RELOAD_TLS_AND_RECONNECT /*131214*/:
                    WifiConfiguration wifiConfig = WifiStateMachine.this.getCurrentWifiConfiguration();
                    if ((wifiConfig == null || wifiConfig.cloudSecurityCheck == 0) && (wifiConfig == null || (wifiConfig.allowedKeyManagement.get(2) && wifiConfig.allowedKeyManagement.get(3)))) {
                        WifiStateMachine.this.log("currentWifiConfiguration is EAP type or no currentWifiConfiguration");
                        if (WifiStateMachine.this.mWifiConfigManager.needsUnlockedKeyStore()) {
                            WifiStateMachine.this.logd("Reconnecting to give a chance to un-connected TLS networks");
                            WifiStateMachine.this.mWifiNative.disconnect();
                            WifiStateMachine.this.lastConnectAttemptTimestamp = System.currentTimeMillis();
                            WifiStateMachine.this.mWifiNative.reconnect();
                            break;
                        }
                    }
                    break;
                case WifiStateMachine.CMD_AUTO_CONNECT /*131215*/:
                    if (!WifiStateMachine.this.isHiLinkActive()) {
                        if (!WifiStateMachine.this.isSupplicantTransientState()) {
                            didDisconnect = false;
                            if (WifiStateMachine.this.getCurrentState() != WifiStateMachine.this.mDisconnectedState) {
                                didDisconnect = true;
                                WifiStateMachine.this.mWifiNative.disconnect();
                            }
                            netId = message.arg1;
                            WifiStateMachine.this.mTargetNetworkId = netId;
                            WifiStateMachine.this.mTargetRoamBSSID = (String) message.obj;
                            config = WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(netId);
                            WifiStateMachine.this.logd("CMD_AUTO_CONNECT sup state " + WifiStateMachine.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + WifiStateMachine.this.getCurrentState().getName() + " nid=" + Integer.toString(netId) + " roam=" + Boolean.toString(WifiStateMachine.this.mAutoRoaming));
                            if (config != null) {
                                if (!WifiStateMachine.this.processConnectModeAutoConnectByMode()) {
                                    if (WifiStateMachine.this.isEnterpriseHotspot(config)) {
                                        WifiStateMachine.this.logd(config.SSID + "is enterprise hotspot ");
                                        WifiStateMachine.this.mTargetRoamBSSID = WifiLastResortWatchdog.BSSID_ANY;
                                    }
                                    WifiStateMachine.this.setTargetBssid(config, WifiStateMachine.this.mTargetRoamBSSID);
                                    WifiStateMachine.this.logd("CMD_AUTO_CONNECT will save config -> " + config.SSID + " nid=" + Integer.toString(netId));
                                    netId = WifiStateMachine.this.mWifiConfigManager.saveNetwork(config, -1).getNetworkId();
                                    WifiStateMachine.this.logd("CMD_AUTO_CONNECT did save config ->  nid=" + Integer.toString(netId));
                                    config = WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(netId);
                                    if (config != null) {
                                        if (netId == config.networkId) {
                                            if (!WifiStateMachine.this.deferForUserInput(message, netId, false)) {
                                                if (WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(netId).userApproved != 2) {
                                                    int lastConnectUid = -1;
                                                    WifiStateMachine.this.mWifiMetrics.startConnectionEvent(config, WifiStateMachine.this.mTargetRoamBSSID, 5);
                                                    if (!didDisconnect) {
                                                        WifiStateMachine.this.mWifiMetrics.setConnectionEventRoamType(1);
                                                    }
                                                    WifiStateMachine.this.saveConnectingNetwork(config);
                                                    if (WifiStateMachine.this.mWifiConfigManager.isLastSelectedConfiguration(config) && WifiStateMachine.this.mWifiConfigManager.isCurrentUserProfile(UserHandle.getUserId(config.lastConnectUid))) {
                                                        lastConnectUid = config.lastConnectUid;
                                                        WifiStateMachine.this.mWifiMetrics.setConnectionEventRoamType(4);
                                                    }
                                                    if (!WifiStateMachine.this.mWifiConfigManager.selectNetwork(config, !HuaweiTelephonyConfigs.isChinaMobile(), lastConnectUid) || !WifiStateMachine.this.mWifiNative.reconnect()) {
                                                        WifiStateMachine.this.loge("Failed to connect config: " + config + " netId: " + netId);
                                                        WifiStateMachine.this.replyToMessage(message, 151554, 0);
                                                        WifiStateMachine.this.reportConnectionAttemptEnd(5, 1);
                                                        break;
                                                    }
                                                    WifiStateMachine.this.lastConnectAttemptTimestamp = System.currentTimeMillis();
                                                    WifiStateMachine.this.targetWificonfiguration = WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(netId);
                                                    config = WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(netId);
                                                    if (!(config == null || WifiStateMachine.this.mWifiConfigManager.isLastSelectedConfiguration(config))) {
                                                        WifiStateMachine.this.mWifiConfigManager.setAndEnableLastSelectedConfiguration(-1);
                                                    }
                                                    WifiStateMachine.this.mAutoRoaming = false;
                                                    if (!WifiStateMachine.this.isRoaming() && !WifiStateMachine.this.linkDebouncing) {
                                                        if (didDisconnect) {
                                                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                                                            break;
                                                        }
                                                    }
                                                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mRoamingState);
                                                    break;
                                                }
                                                WifiStateMachine.this.replyToMessage(message, 151554, 9);
                                                break;
                                            }
                                        }
                                        WifiStateMachine.this.loge("CMD_AUTO_CONNECT couldn't update the config, want nid=" + Integer.toString(netId) + " but got" + config.networkId);
                                        break;
                                    }
                                    WifiStateMachine.this.loge("CMD_AUTO_CONNECT couldn't update the config, got null config");
                                    break;
                                }
                            }
                            WifiStateMachine.this.loge("AUTO_CONNECT and no config, bail out...");
                            break;
                        }
                        WifiStateMachine.this.logd("SupplicantState is TransientState, refuse auto connect");
                        break;
                    }
                    Log.d(WifiStateMachine.TAG, "HiLink is active, refuse auto connect");
                    break;
                    break;
                case WifiStateMachine.CMD_AUTO_ROAM /*131217*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    return true;
                case WifiStateMachine.CMD_AUTO_SAVE_NETWORK /*131218*/:
                    break;
                case WifiStateMachine.CMD_ASSOCIATED_BSSID /*131219*/:
                    String someBssid = message.obj;
                    if (someBssid != null) {
                        ScanDetailCache scanDetailCache = WifiStateMachine.this.mWifiConfigManager.getScanDetailCache(WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(WifiStateMachine.this.mTargetNetworkId));
                        if (scanDetailCache != null) {
                            WifiStateMachine.this.mWifiMetrics.setConnectionScanDetail(scanDetailCache.getScanDetail(someBssid));
                        }
                    }
                    return false;
                case WifiStateMachine.CMD_REMOVE_USER_CONFIGURATIONS /*131224*/:
                    WifiStateMachine.this.mWifiConfigManager.removeNetworksForUser(message.arg1);
                    break;
                case WifiStateMachine.CMD_UPDATE_WIFIPRO_CONFIGURATIONS /*131672*/:
                    WifiStateMachine.this.updateWifiproWifiConfiguration(message);
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*143372*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.log("ConnectModeState, case WifiP2pService.DISCONNECT_WIFI_REQUEST, do reconnect");
                        WifiStateMachine.this.mWifiNative.reconnect();
                        WifiStateMachine.this.mTemporarilyDisconnectWifi = false;
                        break;
                    }
                    WifiStateMachine.this.log("ConnectModeState, case WifiP2pService.DISCONNECT_WIFI_REQUEST, do disconnect");
                    WifiStateMachine.this.mWifiNative.disconnect();
                    WifiStateMachine.this.mTemporarilyDisconnectWifi = true;
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Network connection established");
                    }
                    WifiStateMachine.this.mLastNetworkId = message.arg1;
                    if (WifiStateMachine.this.mHwWifiCHRService != null) {
                        WifiStateMachine.this.mHwWifiCHRService.updateWIFIConfiguraionByConfig(WifiStateMachine.this.getCurrentWifiConfiguration());
                    }
                    WifiStateMachine.this.mLastBssid = (String) message.obj;
                    WifiStateMachine.this.saveWpsOkcConfiguration(WifiStateMachine.this.mLastNetworkId, WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.mWifiInfo.setBSSID(WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.mWifiInfo.setNetworkId(WifiStateMachine.this.mLastNetworkId);
                    WifiStateMachine.this.mWifiQualifiedNetworkSelector.enableBssidForQualityNetworkSelection(WifiStateMachine.this.mLastBssid, true);
                    WifiStateMachine.this.uploader.e(54, "{RT:6,SPEED:" + WifiStateMachine.this.mWifiInfo.getLinkSpeed() + "}");
                    WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.log("ConnectModeState, case WifiMonitor.NETWORK_CONNECTION_EVENT, go to mObtainingIpState");
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mObtainingIpState);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    WifiStateMachine.this.sendHilinkdisConnectedBroadcast();
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("ConnectModeState: Network connection lost ");
                    }
                    WifiStateMachine.this.handleNetworkDisconnect();
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    SupplicantState state = WifiStateMachine.this.handleSupplicantStateChange(message);
                    if (!SupplicantState.isDriverActive(state)) {
                        if (WifiStateMachine.this.mNetworkInfo.getState() != NetworkInfo.State.DISCONNECTED) {
                            WifiStateMachine.this.handleNetworkDisconnect();
                        }
                        WifiStateMachine.this.log("Detected an interface down, restart driver");
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDriverStoppedState);
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_START_DRIVER);
                        break;
                    }
                    if (!(WifiStateMachine.this.linkDebouncing || state != SupplicantState.DISCONNECTED || WifiStateMachine.this.mNetworkInfo.getState() == NetworkInfo.State.DISCONNECTED)) {
                        if (WifiStateMachine.DBG) {
                            WifiStateMachine.this.log("Missed CTRL-EVENT-DISCONNECTED, disconnect");
                        }
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.sendHilinkdisConnectedBroadcast();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    }
                    if (state == SupplicantState.COMPLETED) {
                        WifiStateMachine.this.mIpManager.confirmConfiguration();
                    }
                    if (state == SupplicantState.ASSOCIATED) {
                        StateChangeResult stateChangeResult = message.obj;
                        if (stateChangeResult != null) {
                            WifiStateMachine.this.mCurrentAssociateNetworkId = stateChangeResult.networkId;
                            break;
                        }
                    }
                    break;
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                    WifiStateMachine.this.mWifiLogger.captureBugReportData(2);
                    WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiMonitor.AUTHENTICATION_FAILURE_EVENT);
                    if (WifiStateMachine.this.mTargetNetworkId != -1) {
                        WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(WifiStateMachine.this.mTargetNetworkId, 3);
                    }
                    WifiStateMachine.this.reportConnectionAttemptEnd(3, 1);
                    if (WifiStateMachine.this.mCust != null && WifiStateMachine.this.mCust.isShowWifiAuthenticationFailurerNotification()) {
                        WifiStateMachine.this.mCust.handleWifiAuthenticationFailureEvent(WifiStateMachine.this.mContext, WifiStateMachine.this);
                    }
                    WifiStateMachine.this.mWifiLastResortWatchdog.noteConnectionFailureAndTriggerIfNeeded(WifiStateMachine.this.getTargetSsid(), WifiStateMachine.this.mTargetRoamBSSID, 2);
                    break;
                case WifiMonitor.SSID_TEMP_DISABLED /*147469*/:
                    Log.e(WifiStateMachine.TAG, "Supplicant SSID temporary disabled:" + WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(message.arg1));
                    WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(message.arg1, 3);
                    WifiStateMachine.this.reportConnectionAttemptEnd(4, 1);
                    WifiStateMachine.this.mWifiLastResortWatchdog.noteConnectionFailureAndTriggerIfNeeded(WifiStateMachine.this.getTargetSsid(), WifiStateMachine.this.mTargetRoamBSSID, 2);
                    WifiStateMachine.this.handleDualbandHandoverFailed(3);
                    break;
                case WifiMonitor.SSID_REENABLED /*147470*/:
                    Log.d(WifiStateMachine.TAG, "Supplicant SSID reenable:" + WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(message.arg1));
                    break;
                case WifiMonitor.SUP_REQUEST_IDENTITY /*147471*/:
                    int networkId = message.arg2;
                    boolean identitySent = false;
                    int eapMethod = -1;
                    if (!(WifiStateMachine.this.targetWificonfiguration == null || WifiStateMachine.this.targetWificonfiguration.enterpriseConfig == null)) {
                        eapMethod = WifiStateMachine.this.targetWificonfiguration.enterpriseConfig.getEapMethod();
                    }
                    if (WifiStateMachine.this.targetWificonfiguration != null && WifiStateMachine.this.targetWificonfiguration.networkId == networkId && WifiStateMachine.this.targetWificonfiguration.allowedKeyManagement.get(3)) {
                        if (!(eapMethod == 4 || eapMethod == 5)) {
                            if (eapMethod == 6) {
                            }
                        }
                        TelephonyManager tm = (TelephonyManager) WifiStateMachine.this.mContext.getSystemService("phone");
                        if (tm != null) {
                            String imsi = tm.getSubscriberId();
                            String cdmaGsmImsi = HwTelephonyManager.getDefault().getCdmaGsmImsi();
                            if (cdmaGsmImsi != null) {
                                String[] cdmaGsmImsiArray = cdmaGsmImsi.split(",");
                                if (2 == cdmaGsmImsiArray.length) {
                                    imsi = cdmaGsmImsiArray[1];
                                    if (WifiStateMachine.DBG) {
                                        WifiStateMachine.this.logd("cdma prefer USIM/GSM imsi");
                                    }
                                }
                            }
                            String mccMnc = "";
                            if (tm.getSimState() == 5) {
                                mccMnc = tm.getSimOperator();
                            }
                            String identity = WifiStateMachine.this.buildIdentity(eapMethod, imsi, mccMnc);
                            if (!identity.isEmpty()) {
                                WifiStateMachine.this.mWifiNative.simIdentityResponse(networkId, identity);
                                identitySent = true;
                            }
                        }
                    }
                    if (!identitySent) {
                        String ssid = message.obj;
                        if (!(WifiStateMachine.this.targetWificonfiguration == null || ssid == null || WifiStateMachine.this.targetWificonfiguration.SSID == null || !WifiStateMachine.this.targetWificonfiguration.SSID.equals("\"" + ssid + "\""))) {
                            Log.d(WifiStateMachine.TAG, "updateNetworkSelectionStatus(DISABLED_AUTHENTICATION_NO_CREDENTIALS)");
                            WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(WifiStateMachine.this.targetWificonfiguration, 7);
                        }
                        WifiStateMachine.this.mWifiConfigManager.setAndEnableLastSelectedConfiguration(-1);
                        WifiStateMachine.this.mWifiNative.disconnect();
                        break;
                    }
                    break;
                case WifiMonitor.SUP_REQUEST_SIM_AUTH /*147472*/:
                    WifiStateMachine.this.logd("Received SUP_REQUEST_SIM_AUTH");
                    SimAuthRequestData requestData = message.obj;
                    if (requestData != null) {
                        if (requestData.protocol != 4) {
                            if (requestData.protocol == 5 || requestData.protocol == 6) {
                                WifiStateMachine.this.handle3GAuthRequest(requestData);
                                break;
                            }
                        }
                        WifiStateMachine.this.handleGsmAuthRequest(requestData);
                        break;
                    }
                    WifiStateMachine.this.loge("Invalid sim auth request");
                    break;
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                    WifiStateMachine.this.mWifiLogger.captureBugReportData(1);
                    WifiStateMachine.this.didBlackListBSSID = false;
                    String bssid = message.obj;
                    if (bssid == null || TextUtils.isEmpty(bssid)) {
                        bssid = WifiStateMachine.this.mTargetRoamBSSID;
                    }
                    if (!(bssid == null || WifiStateMachine.this.mWifiConnectivityManager == null)) {
                        WifiStateMachine.this.didBlackListBSSID = WifiStateMachine.this.mWifiConnectivityManager.trackBssid(bssid, false);
                    }
                    WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(WifiStateMachine.this.mTargetNetworkId, 2);
                    WifiStateMachine.this.recordAssociationRejectStatusCode(message.arg2);
                    WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiMonitor.ASSOCIATION_REJECTION_EVENT);
                    WifiStateMachine.this.reportConnectionAttemptEnd(2, 1);
                    WifiStateMachine.this.mWifiLastResortWatchdog.noteConnectionFailureAndTriggerIfNeeded(WifiStateMachine.this.getTargetSsid(), bssid, 1);
                    break;
                case WifiMonitor.AUTHENTICATION_TIMEOUT_EVENT /*147501*/:
                    if (WifiStateMachine.this.mWifiInfo != null && WifiStateMachine.this.mWifiInfo.getSupplicantState() == SupplicantState.ASSOCIATED) {
                        WifiStateMachine.this.loge("auth timeout in associated state, handle as associate reject event");
                        WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiMonitor.ASSOCIATION_REJECTION_EVENT);
                        break;
                    }
                case WifiMonitor.WPS_START_OKC_EVENT /*147656*/:
                    WifiStateMachine.this.sendWpsOkcStartedBroadcast();
                    break;
                case 151553:
                    if (!WifiStateMachine.this.mWifiConfigManager.isCurrentUserProfile(UserHandle.getUserId(message.sendingUid)) && message.sendingUid != WifiStateMachine.this.mSystemUiUid) {
                        WifiStateMachine.this.loge("Only the current foreground user can modify networks  currentUserId=" + WifiStateMachine.this.mWifiConfigManager.getCurrentUserId() + " sendingUserId=" + UserHandle.getUserId(message.sendingUid));
                        WifiStateMachine.this.replyToMessage(message, 151554, 9);
                        break;
                    }
                    netId = message.arg1;
                    config = (WifiConfiguration) message.obj;
                    -get86 = WifiStateMachine.this.mWifiConnectionStatistics;
                    -get86.numWifiManagerJoinAttempt++;
                    boolean updatedExisting = false;
                    String strConfigCRC = "*";
                    if (!(WifiStateMachine.this.mWifiConnectivityManager == null || config == null || !"com.android.settings".equals(config.callingPackage))) {
                        WifiStateMachine.this.mWifiConnectivityManager.resetContinuousScanThreshold();
                    }
                    if (config != null) {
                        strConfigCRC = config.preSharedKey;
                        if (!WifiStateMachine.this.recordUidIfAuthorized(config, message.sendingUid, true)) {
                            WifiStateMachine.this.logw("Not authorized to update network  config=" + config.SSID + " cnid=" + config.networkId + " uid=" + message.sendingUid);
                            WifiStateMachine.this.replyToMessage(message, 151554, 9);
                            break;
                        }
                        String configKey = config.configKey(true);
                        WifiConfiguration savedConfig = WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(configKey);
                        if (savedConfig != null) {
                            config = savedConfig;
                            WifiStateMachine.this.logd("CONNECT_NETWORK updating existing config with id=" + savedConfig.networkId + " configKey=" + configKey);
                            savedConfig.ephemeral = false;
                            WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(savedConfig, 0);
                            updatedExisting = true;
                        }
                        netId = WifiStateMachine.this.mWifiConfigManager.saveNetwork(config, message.sendingUid).getNetworkId();
                        if (WifiStateMachine.this.mWiFiCHRManager != null) {
                            WifiStateMachine.this.mWiFiCHRManager.setLastNetIdFromUI(config, netId);
                        }
                    }
                    config = WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(netId);
                    if (config == null) {
                        WifiStateMachine.this.logd("CONNECT_NETWORK no config for id=" + Integer.toString(netId) + " " + WifiStateMachine.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + WifiStateMachine.this.getCurrentState().getName());
                        WifiStateMachine.this.replyToMessage(message, 151554, 0);
                        break;
                    }
                    WifiStateMachine.this.mTargetNetworkId = netId;
                    if (WifiStateMachine.this.mHwWifiCHRService != null) {
                        WifiConfiguration wifiConfiguration = new WifiConfiguration(config);
                        wifiConfiguration.preSharedKey = strConfigCRC;
                        WifiStateMachine.this.mHwWifiCHRService.connectFromUserByConfig(wifiConfiguration);
                    }
                    WifiStateMachine.this.saveConnectingNetwork(config);
                    WifiStateMachine.this.exitWifiSelfCure();
                    WifiStateMachine.this.autoRoamSetBSSID(netId, WifiLastResortWatchdog.BSSID_ANY);
                    if (message.sendingUid == 1010 || message.sendingUid == 1000) {
                        WifiStateMachine.this.clearConfigBSSID(config, "CONNECT_NETWORK");
                    }
                    if (!WifiStateMachine.this.deferForUserInput(message, netId, true)) {
                        if (WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(netId).userApproved == 2) {
                            WifiStateMachine.this.replyToMessage(message, 151554, 9);
                            break;
                        }
                        WifiStateMachine.this.mAutoRoaming = false;
                        persist = WifiStateMachine.this.mWifiConfigManager.checkConfigOverridePermission(message.sendingUid);
                        WifiStateMachine.this.mWifiConfigManager.setAndEnableLastSelectedConfiguration(netId);
                        WifiStateMachine.this.resetWifiproEvaluateConfig(WifiStateMachine.this.mWifiInfo, netId);
                        if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                            WifiStateMachine.this.mWifiConnectivityManager.connectToUserSelectNetwork(netId, persist);
                        }
                        didDisconnect = false;
                        if (WifiStateMachine.this.mLastNetworkId == -1 || WifiStateMachine.this.mLastNetworkId == netId) {
                            if (WifiStateMachine.this.getCurrentState() != WifiStateMachine.this.mDisconnectedState) {
                            }
                            WifiStateMachine.this.mWifiMetrics.startConnectionEvent(config, WifiStateMachine.this.mTargetRoamBSSID, 4);
                            if (!WifiStateMachine.this.mWifiConfigManager.selectNetwork(config, true, message.sendingUid) && WifiStateMachine.this.mWifiNative.reconnect()) {
                                WifiStateMachine.this.lastConnectAttemptTimestamp = System.currentTimeMillis();
                                WifiStateMachine.this.targetWificonfiguration = WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(netId);
                                WifiStateMachine.this.removeMessages(WifiStateMachine.CMD_AUTO_CONNECT);
                                WifiStateMachine.this.enableAllNetworksByMode();
                                WifiStateMachine.this.mSupplicantStateTracker.sendMessage(151553, WifiStateMachine.this.mOperationalMode);
                                WifiStateMachine.this.replyToMessage(message, 151555);
                                if (!didDisconnect) {
                                    if (!updatedExisting || WifiStateMachine.this.getCurrentState() != WifiStateMachine.this.mConnectedState || WifiStateMachine.this.getCurrentWifiConfiguration().networkId != netId) {
                                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                                        break;
                                    }
                                    WifiStateMachine.this.updateCapabilities(config);
                                    break;
                                }
                                WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                                break;
                            }
                            WifiStateMachine.this.loge("Failed to connect config: " + config + " netId: " + netId);
                            WifiStateMachine.this.replyToMessage(message, 151554, 0);
                            WifiStateMachine.this.reportConnectionAttemptEnd(5, 1);
                            break;
                        }
                        didDisconnect = true;
                        WifiStateMachine.this.mWifiNative.disconnect();
                        WifiStateMachine.this.mWifiMetrics.startConnectionEvent(config, WifiStateMachine.this.mTargetRoamBSSID, 4);
                        if (!WifiStateMachine.this.mWifiConfigManager.selectNetwork(config, true, message.sendingUid)) {
                            break;
                        }
                        WifiStateMachine.this.loge("Failed to connect config: " + config + " netId: " + netId);
                        WifiStateMachine.this.replyToMessage(message, 151554, 0);
                        WifiStateMachine.this.reportConnectionAttemptEnd(5, 1);
                    }
                    break;
                case 151556:
                    if (!WifiStateMachine.this.mWifiConfigManager.isCurrentUserProfile(UserHandle.getUserId(message.sendingUid))) {
                        WifiStateMachine.this.loge("Only the current foreground user can modify networks  currentUserId=" + WifiStateMachine.this.mWifiConfigManager.getCurrentUserId() + " sendingUserId=" + UserHandle.getUserId(message.sendingUid));
                        WifiStateMachine.this.replyToMessage(message, 151557, 9);
                        break;
                    }
                    WifiConfiguration toRemove = WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(message.arg1);
                    if (toRemove == null) {
                        WifiStateMachine.this.lastForgetConfigurationAttempt = null;
                    } else {
                        WifiStateMachine.this.lastForgetConfigurationAttempt = new WifiConfiguration(toRemove);
                    }
                    netId = message.arg1;
                    if (!WifiStateMachine.this.mWifiConfigManager.canModifyNetwork(message.sendingUid, netId, false)) {
                        WifiStateMachine.this.logw("Not authorized to forget network  cnid=" + netId + " uid=" + message.sendingUid);
                        WifiStateMachine.this.replyToMessage(message, 151557, 9);
                        break;
                    }
                    WifiStateMachine.this.exitWifiSelfCure();
                    if (!WifiStateMachine.this.mWifiConfigManager.forgetNetwork(message.arg1)) {
                        WifiStateMachine.this.loge("Failed to forget network");
                        WifiStateMachine.this.replyToMessage(message, 151557, 0);
                        break;
                    }
                    WifiStateMachine.this.replyToMessage(message, 151558);
                    WifiStateMachine.this.broadcastWifiCredentialChanged(1, (WifiConfiguration) message.obj);
                    break;
                case 151559:
                    -get86 = WifiStateMachine.this.mWifiConnectionStatistics;
                    -get86.numWifiManagerJoinAttempt++;
                    break;
                case 151562:
                    WpsResult wpsResult;
                    WpsInfo wpsInfo = message.obj;
                    switch (wpsInfo.setup) {
                        case 0:
                            WifiStateMachine.this.clearRandomMacOui();
                            WifiStateMachine.this.mIsRandomMacCleared = true;
                            wpsResult = WifiStateMachine.this.mWifiConfigManager.startWpsPbc(wpsInfo);
                            break;
                        case 1:
                            wpsResult = WifiStateMachine.this.mWifiConfigManager.startWpsWithPinFromDevice(wpsInfo);
                            if (!TextUtils.isEmpty(wpsResult.pin)) {
                                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_WPS_PIN_RETRY, wpsResult);
                                break;
                            }
                            break;
                        case 2:
                            wpsResult = WifiStateMachine.this.mWifiConfigManager.startWpsWithPinFromAccessPoint(wpsInfo);
                            break;
                        default:
                            wpsResult = new WpsResult(Status.FAILURE);
                            WifiStateMachine.this.loge("Invalid setup for WPS");
                            break;
                    }
                    WifiStateMachine.this.mWifiConfigManager.setAndEnableLastSelectedConfiguration(-1);
                    if (wpsResult.status != Status.SUCCESS) {
                        WifiStateMachine.this.loge("Failed to start WPS with config " + wpsInfo.toString());
                        WifiStateMachine.this.replyToMessage(message, 151564, 0);
                        break;
                    }
                    WifiStateMachine.this.replyToMessage(message, 151563, (Object) wpsResult);
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mWpsRunningState);
                    break;
                case 151569:
                    int reason = 9;
                    int appId = UserHandle.getAppId(message.sendingUid);
                    if (appId == 0 || appId == 1000) {
                        reason = 10;
                    }
                    Log.d(WifiStateMachine.TAG, "updateNetworkSelectionStatus:" + reason + "  " + WifiStateMachine.this.mContext.getPackageManager().getNameForUid(message.sendingUid));
                    config = WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(message.arg1);
                    if (config != null && WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(config, reason)) {
                        config.getNetworkSelectionStatus().setNetworkSelectionDisableName(WifiStateMachine.this.mContext.getPackageManager().getNameForUid(message.sendingUid));
                        WifiStateMachine.this.replyToMessage(message, 151571);
                        break;
                    }
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                    WifiStateMachine.this.replyToMessage(message, 151570, 0);
                    break;
                    break;
                default:
                    return false;
            }
            if (WifiStateMachine.this.mWifiConfigManager.isCurrentUserProfile(UserHandle.getUserId(message.sendingUid))) {
                WifiStateMachine.this.lastSavedConfigurationAttempt = null;
                config = (WifiConfiguration) message.obj;
                if (config == null) {
                    WifiStateMachine.this.loge("ERROR: SAVE_NETWORK with null configuration" + WifiStateMachine.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + WifiStateMachine.this.getCurrentState().getName());
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                    WifiStateMachine.this.replyToMessage(message, 151560, 0);
                } else {
                    WifiStateMachine.this.lastSavedConfigurationAttempt = new WifiConfiguration(config);
                    WifiStateMachine.this.logd("SAVE_NETWORK id=" + Integer.toString(config.networkId) + " config=" + config.SSID + " nid=" + config.networkId + " supstate=" + WifiStateMachine.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + WifiStateMachine.this.getCurrentState().getName());
                    if (message.what == 151559) {
                        if (!WifiStateMachine.this.recordUidIfAuthorized(config, message.sendingUid, false)) {
                            WifiStateMachine.this.logw("Not authorized to update network  config=" + config.SSID + " cnid=" + config.networkId + " uid=" + message.sendingUid);
                            WifiStateMachine.this.replyToMessage(message, 151560, 9);
                        }
                    }
                    NetworkUpdateResult result = WifiStateMachine.this.mWifiConfigManager.saveNetwork(config, -1);
                    if (result.getNetworkId() != -1) {
                        if (WifiStateMachine.this.mWifiInfo.getNetworkId() == result.getNetworkId()) {
                            if (result.hasIpChanged()) {
                                WifiStateMachine.this.log("Reconfiguring IP on connection");
                                WifiStateMachine.this.transitionTo(WifiStateMachine.this.mObtainingIpState);
                            }
                            if (result.hasProxyChanged()) {
                                WifiStateMachine.this.log("Reconfiguring proxy on connection");
                                WifiStateMachine.this.mIpManager.setHttpProxy(WifiStateMachine.this.mWifiConfigManager.getProxyProperties(WifiStateMachine.this.mLastNetworkId));
                            }
                        }
                        WifiStateMachine.this.replyToMessage(message, 151561);
                        WifiStateMachine.this.broadcastWifiCredentialChanged(0, config);
                        if (WifiStateMachine.DBG) {
                            WifiStateMachine.this.logd("Success save network nid=" + Integer.toString(result.getNetworkId()));
                        }
                        boolean user = message.what == 151559;
                        boolean persistConnect = WifiStateMachine.this.mWifiConfigManager.checkConfigOverridePermission(message.sendingUid);
                        if (user) {
                            WifiStateMachine.this.mWifiConfigManager.updateLastConnectUid(config, message.sendingUid);
                            WifiStateMachine.this.mWifiConfigManager.writeKnownNetworkHistory();
                        }
                        if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                            WifiStateMachine.this.mWifiConnectivityManager.connectToUserSelectNetwork(result.getNetworkId(), persistConnect);
                        }
                    } else {
                        WifiStateMachine.this.loge("Failed to save network");
                        WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                        WifiStateMachine.this.replyToMessage(message, 151560, 0);
                    }
                }
            } else {
                WifiStateMachine.this.loge("Only the current foreground user can modify networks  currentUserId=" + WifiStateMachine.this.mWifiConfigManager.getCurrentUserId() + " sendingUserId=" + UserHandle.getUserId(message.sendingUid));
                WifiStateMachine.this.replyToMessage(message, 151560, 9);
            }
            return true;
        }

        public void exit() {
            if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                WifiStateMachine.this.mWifiConnectivityManager.setWifiEnabled(false);
            }
        }
    }

    class ConnectedState extends State {
        private Message mSourceMessage = null;

        ConnectedState() {
        }

        public void enter() {
            WifiStateMachine.this.logd("WifiStateMachine: enter Connected state" + getName());
            WifiStateMachine.this.processStatistics(0);
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.log(getName());
            }
            new Thread(new Runnable() {
                public void run() {
                    WifiStateMachine.this.updateDefaultRouteMacAddress(1000);
                }
            }).start();
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.log("Enter ConnectedState  mScreenOn=" + WifiStateMachine.this.mScreenOn);
            }
            WifiStateMachine.this.triggerRoamingNetworkMonitor(WifiStateMachine.this.mAutoRoaming);
            WifiStateMachine.this.handleConnectedInWifiPro();
            if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                WifiStateMachine.this.mWifiConnectivityManager.handleConnectionStateChanged(1);
                if (WifiStateMachine.this.mWifiStatStore != null && WifiStateMachine.this.mConnectingStartTimestamp > 0) {
                    WifiStateMachine.this.mWifiStatStore.triggerCHRConnectingDuration(SystemClock.elapsedRealtime() - WifiStateMachine.this.mConnectingStartTimestamp);
                    WifiStateMachine.this.mConnectingStartTimestamp = 0;
                }
            }
            WifiStateMachine.this.registerConnected();
            WifiStateMachine.this.lastConnectAttemptTimestamp = 0;
            WifiStateMachine.this.targetWificonfiguration = null;
            WifiStateMachine.this.linkDebouncing = false;
            WifiStateMachine.this.mAutoRoaming = false;
            if (WifiStateMachine.this.testNetworkDisconnect) {
                WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                wifiStateMachine.testNetworkDisconnectCounter = wifiStateMachine.testNetworkDisconnectCounter + 1;
                WifiStateMachine.this.logd("ConnectedState Enter start disconnect test " + WifiStateMachine.this.testNetworkDisconnectCounter);
                WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_TEST_NETWORK_DISCONNECT, WifiStateMachine.this.testNetworkDisconnectCounter, 0), 15000);
            }
            WifiStateMachine.this.mWifiConfigManager.enableAllNetworks();
            WifiStateMachine.this.mLastDriverRoamAttempt = 0;
            WifiStateMachine.this.mTargetNetworkId = -1;
            WifiStateMachine.this.mWifiLastResortWatchdog.connectedStateTransition(true);
            WifiStateMachine.this.triggerUpdateAPInfo();
            synchronized (WifiStateMachine.this.mDhcpResultsLock) {
                if (WifiStateMachine.this.mDhcpResults != null) {
                    WifiStateMachine.this.updateCHRDNS(WifiStateMachine.this.mDhcpResults.dnsServers);
                }
            }
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            String str;
            WifiConfiguration config;
            switch (message.what) {
                case WifiStateMachine.CMD_TEST_NETWORK_DISCONNECT /*131161*/:
                    if (message.arg1 == WifiStateMachine.this.testNetworkDisconnectCounter) {
                        WifiStateMachine.this.mWifiNative.disconnect();
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_UNWANTED_NETWORK /*131216*/:
                    if (message.arg1 == 0) {
                        WifiStateMachine.this.mWifiConfigManager.handleBadNetworkDisconnectReport(WifiStateMachine.this.mLastNetworkId, WifiStateMachine.this.mWifiInfo);
                        WifiStateMachine.this.mWifiNative.disconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    } else {
                        if (!(message.arg1 == 2 || message.arg1 == 1)) {
                            if (message.arg1 == 3) {
                            }
                        }
                        String str2 = WifiStateMachine.TAG;
                        if (message.arg1 == 2) {
                            str = "NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN";
                        } else {
                            str = "NETWORK_STATUS_UNWANTED_VALIDATION_FAILED";
                        }
                        Log.d(str2, str);
                        config = WifiStateMachine.this.getCurrentWifiConfiguration();
                        if (config != null) {
                            if (message.arg1 == 2) {
                                config.validatedInternetAccess = false;
                                if (WifiStateMachine.this.mWifiConfigManager.isLastSelectedConfiguration(config)) {
                                    WifiStateMachine.this.mWifiConfigManager.setAndEnableLastSelectedConfiguration(-1);
                                }
                                Log.d(WifiStateMachine.TAG, "updateNetworkSelectionStatus(DISABLED_NO_INTERNET)");
                                WifiStateMachine.this.mWifiConfigManager.updateNetworkSelectionStatus(config, 8);
                            }
                            config.numNoInternetAccessReports++;
                            WifiStateMachine.this.handleUnwantedNetworkInWifiPro(config, message.arg1);
                        }
                    }
                    return true;
                case WifiStateMachine.CMD_AUTO_ROAM /*131217*/:
                    WifiStateMachine.this.mLastDriverRoamAttempt = 0;
                    ScanResult candidate = message.obj;
                    String bssid = WifiLastResortWatchdog.BSSID_ANY;
                    if (candidate != null) {
                        bssid = candidate.BSSID;
                    }
                    int netId = message.arg1;
                    if (netId != -1) {
                        config = WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(netId);
                        if (config != null) {
                            WifiStateMachine.this.logd("CMD_AUTO_ROAM sup state " + WifiStateMachine.this.mSupplicantStateTracker.getSupplicantStateName() + " my state " + WifiStateMachine.this.getCurrentState().getName() + " nid=" + Integer.toString(netId) + " config " + config.configKey() + " roam=" + Integer.toString(message.arg2) + " to " + bssid + " targetRoamBSSID " + WifiStateMachine.this.mTargetRoamBSSID);
                            WifiStateMachine.this.setTargetBssid(config, bssid);
                            WifiStateMachine.this.mTargetNetworkId = netId;
                            WifiConfiguration currentConfig = WifiStateMachine.this.getCurrentWifiConfiguration();
                            if (currentConfig == null || !currentConfig.isLinked(config)) {
                                WifiStateMachine.this.mWifiMetrics.startConnectionEvent(config, WifiStateMachine.this.mTargetRoamBSSID, 3);
                            } else {
                                WifiStateMachine.this.mWifiMetrics.startConnectionEvent(config, WifiStateMachine.this.mTargetRoamBSSID, 2);
                            }
                            if (!WifiStateMachine.this.deferForUserInput(message, netId, false)) {
                                if (WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(netId).userApproved != 2) {
                                    boolean ret = false;
                                    if (WifiStateMachine.this.mLastNetworkId == netId) {
                                        ret = WifiStateMachine.this.mWifiNative.reassociate();
                                    } else if (WifiStateMachine.this.mWifiConfigManager.selectNetwork(config, false, -1) && WifiStateMachine.this.mWifiNative.reconnect()) {
                                        ret = true;
                                    }
                                    if (!ret) {
                                        WifiStateMachine.this.loge("Failed to connect config: " + config + " netId: " + netId);
                                        WifiStateMachine.this.replyToMessage(message, 151554, 0);
                                        WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                                        WifiStateMachine.this.reportConnectionAttemptEnd(5, 1);
                                        break;
                                    }
                                    WifiStateMachine.this.lastConnectAttemptTimestamp = System.currentTimeMillis();
                                    WifiStateMachine.this.targetWificonfiguration = WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(netId);
                                    WifiStateMachine.this.mAutoRoaming = true;
                                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mRoamingState);
                                    break;
                                }
                                WifiStateMachine.this.replyToMessage(message, 151554, 9);
                                WifiStateMachine.this.reportConnectionAttemptEnd(5, 1);
                                break;
                            }
                            WifiStateMachine.this.reportConnectionAttemptEnd(5, 1);
                            break;
                        }
                        WifiStateMachine.this.loge("AUTO_ROAM and no config, bail out...");
                        break;
                    }
                    WifiStateMachine.this.loge("AUTO_ROAM and no config, bail out...");
                    break;
                case WifiStateMachine.CMD_ASSOCIATED_BSSID /*131219*/:
                    WifiStateMachine.this.mLastDriverRoamAttempt = System.currentTimeMillis();
                    WifiStateMachine.this.notifyWifiRoamingStarted();
                    return false;
                case WifiStateMachine.CMD_NETWORK_STATUS /*131220*/:
                    if (message.arg1 == 1) {
                        config = WifiStateMachine.this.getCurrentWifiConfiguration();
                        if (config != null) {
                            WifiStateMachine.this.handleValidNetworkInWifiPro(config);
                            config.numNoInternetAccessReports = 0;
                            config.validatedInternetAccess = true;
                            WifiStateMachine.this.mWifiConfigManager.writeKnownNetworkHistory();
                        }
                    }
                    return true;
                case WifiStateMachine.CMD_ACCEPT_UNVALIDATED /*131225*/:
                    boolean accept = message.arg1 != 0;
                    config = WifiStateMachine.this.getCurrentWifiConfiguration();
                    if (config != null) {
                        config.noInternetAccessExpected = accept;
                        WifiStateMachine.this.mWifiConfigManager.writeKnownNetworkHistory();
                    }
                    return true;
                case WifiStateMachine.CMD_UPDATE_ASSOCIATED_SCAN_PERMISSION /*131230*/:
                    WifiStateMachine.this.updateAssociatedScanPermission();
                    break;
                case WifiStateMachine.CMD_START_IP_PACKET_OFFLOAD /*131232*/:
                    int slot = message.arg1;
                    int intervalSeconds = message.arg2;
                    KeepalivePacketData pkt = message.obj;
                    try {
                        pkt.dstMac = WifiStateMachine.this.macAddressFromString(WifiStateMachine.this.macAddressFromRoute(RouteInfo.selectBestRoute(WifiStateMachine.this.mLinkProperties.getRoutes(), pkt.dstAddress).getGateway().getHostAddress()));
                        WifiStateMachine.this.mNetworkAgent.onPacketKeepaliveEvent(slot, WifiStateMachine.this.startWifiIPPacketOffload(slot, pkt, intervalSeconds));
                        break;
                    } catch (NullPointerException e) {
                        WifiStateMachine.this.loge("Can't find MAC address for next hop to " + pkt.dstAddress);
                        WifiStateMachine.this.mNetworkAgent.onPacketKeepaliveEvent(slot, -21);
                        break;
                    }
                case WifiStateMachine.CMD_SET_DETECTMODE_CONF /*131772*/:
                    WifiStateMachine.this.processSetVoWifiDetectMode(message);
                    break;
                case WifiStateMachine.CMD_SET_DETECT_PERIOD /*131773*/:
                    WifiStateMachine.this.processSetVoWifiDetectPeriod(message);
                    break;
                case WifiStateMachine.POOR_LINK_DETECTED /*131873*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: POOR_LINK_DETECTED");
                    WifiStateMachine.this.wifiNetworkExplicitlyUnselected();
                    WifiStateMachine.this.setNetworkDetailedState(DetailedState.VERIFYING_POOR_LINK);
                    WifiStateMachine.this.mWifiConfigManager.updateStatus(WifiStateMachine.this.mLastNetworkId, DetailedState.VERIFYING_POOR_LINK);
                    WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                    break;
                case WifiStateMachine.GOOD_LINK_DETECTED /*131874*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: GOOD_LINK_DETECTED");
                    WifiStateMachine.this.updateWifiBackgroudStatus(message.arg1);
                    WifiStateMachine.this.wifiNetworkExplicitlySelected();
                    WifiStateMachine.this.setWifiBackgroundStatus(false);
                    WifiStateMachine.this.sendConnectedState();
                    break;
                case WifiStateMachine.SCE_REQUEST_UPDATE_DNS_SERVER /*131882*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: SCE_REQUEST_UPDATE_DNS_SERVER");
                    WifiStateMachine.this.sendUpdateDnsServersRequest(message, WifiStateMachine.this.mLinkProperties);
                    break;
                case WifiStateMachine.SCE_REQUEST_RENEW_DHCP /*131883*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: SCE_REQUEST_RENEW_DHCP");
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mObtainingIpState);
                    break;
                case WifiStateMachine.SCE_REQUEST_SET_STATIC_IP /*131884*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: SCE_REQUEST_SET_STATIC_IP");
                    WifiStateMachine.this.stopIpManager();
                    WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.SCE_START_SET_STATIC_IP, message.obj, 1000);
                    break;
                case WifiStateMachine.SCE_START_SET_STATIC_IP /*131885*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: SCE_START_SET_STATIC_IP");
                    WifiStateMachine.this.handleStaticIpConfig(WifiStateMachine.this.mIpManager, WifiStateMachine.this.mWifiNative, (StaticIpConfiguration) message.obj);
                    break;
                case WifiStateMachine.SCE_REQUEST_REASSOC_WIFI /*131886*/:
                    WifiStateMachine.this.startSelfCureWifiReassoc();
                    break;
                case WifiStateMachine.SCE_REQUEST_RESET_WIFI /*131887*/:
                    WifiStateMachine.this.startSelfCureWifiReset();
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    long lastRoam = 0;
                    WifiStateMachine.this.reportConnectionAttemptEnd(6, 1);
                    if (WifiStateMachine.this.mLastDriverRoamAttempt != 0) {
                        lastRoam = System.currentTimeMillis() - WifiStateMachine.this.mLastDriverRoamAttempt;
                        WifiStateMachine.this.mLastDriverRoamAttempt = 0;
                    }
                    if (WifiStateMachine.unexpectedDisconnectedReason(message.arg2)) {
                        WifiStateMachine.this.mWifiLogger.captureBugReportData(5);
                    }
                    config = WifiStateMachine.this.getCurrentWifiConfiguration();
                    if ((WifiStateMachine.this.mScreenOn || WifiStateMachine.this.isWifiRepeaterStarted()) && !WifiStateMachine.this.linkDebouncing && config != null && config.getNetworkSelectionStatus().isNetworkEnabled() && !WifiStateMachine.this.mWifiConfigManager.isLastSelectedConfiguration(config) && (message.arg2 != 3 || (lastRoam > 0 && lastRoam < 2000))) {
                        if (!ScanResult.is24GHz(WifiStateMachine.this.mWifiInfo.getFrequency()) || WifiStateMachine.this.mWifiInfo.getRssi() <= -73) {
                            if (ScanResult.is5GHz(WifiStateMachine.this.mWifiInfo.getFrequency()) && WifiStateMachine.this.mWifiInfo.getRssi() > WifiStateMachine.this.mWifiConfigManager.mThresholdQualifiedRssi5.get()) {
                            }
                        }
                        WifiStateMachine.this.startScanForConfiguration(WifiStateMachine.this.getCurrentWifiConfiguration());
                        WifiStateMachine.this.linkDebouncing = true;
                        WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_DELAYED_NETWORK_DISCONNECT, 0, WifiStateMachine.this.mLastNetworkId), 4000);
                        if (WifiStateMachine.DBG) {
                            WifiStateMachine.this.log("NETWORK_DISCONNECTION_EVENT in connected state BSSID=" + WifiStateMachine.this.mWifiInfo.getBSSID() + " RSSI=" + WifiStateMachine.this.mWifiInfo.getRssi() + " freq=" + WifiStateMachine.this.mWifiInfo.getFrequency() + " reason=" + message.arg2 + " -> debounce");
                        }
                        return true;
                    }
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                        StringBuilder append = new StringBuilder().append("NETWORK_DISCONNECTION_EVENT in connected state BSSID=").append(WifiStateMachine.this.mWifiInfo.getBSSID()).append(" RSSI=").append(WifiStateMachine.this.mWifiInfo.getRssi()).append(" freq=").append(WifiStateMachine.this.mWifiInfo.getFrequency()).append(" was debouncing=").append(WifiStateMachine.this.linkDebouncing).append(" reason=").append(message.arg2).append(" Network Selection Status=");
                        if (config == null) {
                            str = "Unavailable";
                        } else {
                            str = config.getNetworkSelectionStatus().getNetworkStatusString();
                        }
                        wifiStateMachine.log(append.append(str).toString());
                        break;
                    }
                    break;
                case WifiMonitor.VOWIFI_DETECT_IRQ_STR_EVENT /*147520*/:
                    WifiStateMachine.this.logd("receive Vo WifiDetect event 1");
                    if (this.mSourceMessage != null) {
                        WifiStateMachine.this.logd("receive Vo WifiDetect event 2");
                        WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151576);
                        break;
                    }
                    break;
                case 151575:
                    WifiStateMachine.this.logd("start VoWifiDetect ");
                    this.mSourceMessage = Message.obtain(message);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            WifiStateMachine.this.logd("WifiStateMachine: Leaving Connected state");
            WifiStateMachine.this.processStatistics(1);
            if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                WifiStateMachine.this.mWifiConnectivityManager.handleConnectionStateChanged(3);
            }
            WifiStateMachine.this.mLastDriverRoamAttempt = 0;
            WifiStateMachine.this.mWhiteListedSsids = null;
            WifiStateMachine.this.mWifiLastResortWatchdog.connectedStateTransition(false);
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            String str = null;
            boolean z = false;
            WifiStateMachine.this.logStateAndMessage(message, this);
            WifiStateMachine wifiStateMachine;
            switch (message.what) {
                case 69632:
                    if (message.obj == WifiStateMachine.this.mWifiP2pChannel) {
                        if (message.arg1 != 0) {
                            WifiStateMachine.this.loge("WifiP2pService connection failure, error=" + message.arg1);
                            break;
                        }
                        WifiStateMachine.this.mWifiP2pChannel.sendMessage(69633);
                        break;
                    }
                    WifiStateMachine.this.loge("got HALF_CONNECTED for unknown channel");
                    break;
                case 69636:
                    if (((AsyncChannel) message.obj) == WifiStateMachine.this.mWifiP2pChannel) {
                        WifiStateMachine.this.loge("WifiP2pService channel lost, message.arg1 =" + message.arg1);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_START_SUPPLICANT /*131083*/:
                case WifiStateMachine.CMD_STOP_SUPPLICANT /*131084*/:
                case WifiStateMachine.CMD_START_DRIVER /*131085*/:
                case WifiStateMachine.CMD_STOP_DRIVER /*131086*/:
                case WifiStateMachine.CMD_STOP_SUPPLICANT_FAILED /*131089*/:
                case WifiStateMachine.CMD_DRIVER_START_TIMED_OUT /*131091*/:
                case WifiStateMachine.CMD_START_AP /*131093*/:
                case WifiStateMachine.CMD_START_AP_FAILURE /*131094*/:
                case WifiStateMachine.CMD_STOP_AP /*131095*/:
                case WifiStateMachine.CMD_AP_STOPPED /*131096*/:
                case WifiStateMachine.CMD_ENABLE_ALL_NETWORKS /*131127*/:
                case WifiStateMachine.CMD_BLACKLIST_NETWORK /*131128*/:
                case WifiStateMachine.CMD_CLEAR_BLACKLIST /*131129*/:
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                case WifiStateMachine.CMD_RSSI_POLL /*131155*/:
                case WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN /*131160*/:
                case WifiStateMachine.CMD_TEST_NETWORK_DISCONNECT /*131161*/:
                case WifiStateMachine.CMD_SET_FREQUENCY_BAND /*131162*/:
                case WifiStateMachine.CMD_OBTAINING_IP_ADDRESS_WATCHDOG_TIMER /*131165*/:
                case WifiStateMachine.CMD_ROAM_WATCHDOG_TIMER /*131166*/:
                case WifiStateMachine.CMD_DISCONNECTING_WATCHDOG_TIMER /*131168*/:
                case WifiStateMachine.CMD_DISABLE_EPHEMERAL_NETWORK /*131170*/:
                case WifiStateMachine.CMD_DISABLE_P2P_RSP /*131205*/:
                case WifiStateMachine.CMD_TARGET_BSSID /*131213*/:
                case WifiStateMachine.CMD_RELOAD_TLS_AND_RECONNECT /*131214*/:
                case WifiStateMachine.CMD_AUTO_CONNECT /*131215*/:
                case WifiStateMachine.CMD_UNWANTED_NETWORK /*131216*/:
                case WifiStateMachine.CMD_AUTO_ROAM /*131217*/:
                case WifiStateMachine.CMD_AUTO_SAVE_NETWORK /*131218*/:
                case WifiStateMachine.CMD_ASSOCIATED_BSSID /*131219*/:
                case WifiStateMachine.CMD_UPDATE_ASSOCIATED_SCAN_PERMISSION /*131230*/:
                case WifiStateMachine.CMD_WPS_PIN_RETRY /*131576*/:
                case WifiStateMachine.CMD_SET_DETECTMODE_CONF /*131772*/:
                case WifiStateMachine.CMD_SET_DETECT_PERIOD /*131773*/:
                case WifiMonitor.SUP_CONNECTION_EVENT /*147457*/:
                case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                case WifiMonitor.SCAN_RESULTS_EVENT /*147461*/:
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                case WifiMonitor.WPS_OVERLAP_EVENT /*147466*/:
                case WifiMonitor.SUP_REQUEST_IDENTITY /*147471*/:
                case WifiMonitor.SUP_REQUEST_SIM_AUTH /*147472*/:
                case WifiMonitor.SCAN_FAILED_EVENT /*147473*/:
                case WifiMonitor.WAPI_AUTHENTICATION_FAILURE_EVENT /*147474*/:
                case WifiMonitor.WAPI_CERTIFICATION_FAILURE_EVENT /*147475*/:
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                case WifiMonitor.VOWIFI_DETECT_IRQ_STR_EVENT /*147520*/:
                case WifiMonitor.WPS_START_OKC_EVENT /*147656*/:
                case 151575:
                case 196611:
                case 196612:
                case 196614:
                case 196618:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_BLUETOOTH_ADAPTER_STATE_CHANGE /*131103*/:
                    WifiStateMachine.this.mBluetoothConnectionActive = message.arg1 != 0;
                    break;
                case WifiStateMachine.CMD_AP_STARTED_GET_STA_LIST /*131104*/:
                    WifiStateMachine.this.loge("DefaultState: cannot get Soft AP current connected stations list");
                    WifiStateMachine.this.mReplyChannel.replyToMessage(message, message.what, null);
                    break;
                case WifiStateMachine.CMD_PING_SUPPLICANT /*131123*/:
                case WifiStateMachine.CMD_ADD_OR_UPDATE_NETWORK /*131124*/:
                case WifiStateMachine.CMD_REMOVE_NETWORK /*131125*/:
                case WifiStateMachine.CMD_ENABLE_NETWORK /*131126*/:
                case WifiStateMachine.CMD_SAVE_CONFIG /*131130*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, -1);
                    break;
                case WifiStateMachine.CMD_GET_CONFIGURED_NETWORKS /*131131*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) (List) null);
                    break;
                case WifiStateMachine.CMD_GET_CAPABILITY_FREQ /*131132*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, null);
                    break;
                case WifiStateMachine.CMD_GET_SUPPORTED_FEATURES /*131133*/:
                    if (WifiStateMachine.this.mFeatureSet <= 0) {
                        WifiStateMachine.this.mFeatureSet = WifiStateMachine.this.mWifiNative.getSupportedFeatureSet();
                        if (WifiStateMachine.DBG) {
                            Log.d(WifiStateMachine.TAG, "CMD_GET_SUPPORTED_FEATURES: " + WifiStateMachine.this.mFeatureSet);
                        }
                    }
                    WifiStateMachine.this.replyToMessage(message, message.what, WifiStateMachine.this.mFeatureSet);
                    break;
                case WifiStateMachine.CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS /*131134*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) (List) null);
                    break;
                case WifiStateMachine.CMD_GET_LINK_LAYER_STATS /*131135*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, null);
                    break;
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_GET_CONNECTION_STATISTICS /*131148*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.mWifiConnectionStatistics);
                    break;
                case WifiStateMachine.CMD_SET_HIGH_PERF_MODE /*131149*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.setSuspendOptimizations(2, true);
                        break;
                    }
                    WifiStateMachine.this.setSuspendOptimizations(2, false);
                    break;
                case WifiStateMachine.CMD_ENABLE_RSSI_POLL /*131154*/:
                    wifiStateMachine = WifiStateMachine.this;
                    if (message.arg1 == 1) {
                        z = true;
                    }
                    wifiStateMachine.mEnableRssiPolling = z;
                    break;
                case WifiStateMachine.CMD_SET_SUSPEND_OPT_ENABLED /*131158*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.setSuspendOptimizations(4, false);
                        break;
                    }
                    WifiStateMachine.this.mSuspendWakeLock.release();
                    WifiStateMachine.this.setSuspendOptimizations(4, true);
                    break;
                case WifiStateMachine.CMD_SCREEN_STATE_CHANGED /*131167*/:
                    wifiStateMachine = WifiStateMachine.this;
                    if (message.arg1 != 0) {
                        z = true;
                    }
                    wifiStateMachine.handleScreenStateChanged(z);
                    break;
                case WifiStateMachine.CMD_REMOVE_APP_CONFIGURATIONS /*131169*/:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_GET_MATCHING_CONFIG /*131171*/:
                    WifiStateMachine.this.replyToMessage(message, message.what);
                    break;
                case WifiStateMachine.CMD_FIRMWARE_ALERT /*131172*/:
                    if (WifiStateMachine.this.mWifiLogger != null) {
                        WifiStateMachine.this.mWifiLogger.captureAlertData(message.arg1, message.obj);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_RESET_SIM_NETWORKS /*131173*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_ADD_PASSPOINT_MO /*131174*/:
                case WifiStateMachine.CMD_MODIFY_PASSPOINT_MO /*131175*/:
                case WifiStateMachine.CMD_QUERY_OSU_ICON /*131176*/:
                case WifiStateMachine.CMD_MATCH_PROVIDER_NETWORK /*131177*/:
                    WifiStateMachine.this.replyToMessage(message, message.what);
                    break;
                case WifiStateMachine.CMD_BOOT_COMPLETED /*131206*/:
                    WifiStateMachine.this.maybeRegisterNetworkFactory();
                    break;
                case WifiStateMachine.CMD_IP_CONFIGURATION_SUCCESSFUL /*131210*/:
                case WifiStateMachine.CMD_IP_CONFIGURATION_LOST /*131211*/:
                case WifiStateMachine.CMD_IP_REACHABILITY_LOST /*131221*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_UPDATE_LINKPROPERTIES /*131212*/:
                    WifiStateMachine.this.updateLinkProperties((LinkProperties) message.obj);
                    break;
                case WifiStateMachine.CMD_REMOVE_USER_CONFIGURATIONS /*131224*/:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_START_IP_PACKET_OFFLOAD /*131232*/:
                    if (WifiStateMachine.this.mNetworkAgent != null) {
                        WifiStateMachine.this.mNetworkAgent.onPacketKeepaliveEvent(message.arg1, -20);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_STOP_IP_PACKET_OFFLOAD /*131233*/:
                    if (WifiStateMachine.this.mNetworkAgent != null) {
                        WifiStateMachine.this.mNetworkAgent.onPacketKeepaliveEvent(message.arg1, -20);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_START_RSSI_MONITORING_OFFLOAD /*131234*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_STOP_RSSI_MONITORING_OFFLOAD /*131235*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_USER_SWITCH /*131237*/:
                    WifiStateMachine.this.mWifiConfigManager.handleUserSwitch(message.arg1);
                    break;
                case WifiStateMachine.CMD_INSTALL_PACKET_FILTER /*131274*/:
                    WifiStateMachine.this.mWifiNative.installPacketFilter((byte[]) message.obj);
                    break;
                case WifiStateMachine.CMD_SET_FALLBACK_PACKET_FILTERING /*131275*/:
                    if (!((Boolean) message.obj).booleanValue()) {
                        WifiStateMachine.this.mWifiNative.stopFilteringMulticastV4Packets();
                        break;
                    }
                    WifiStateMachine.this.mWifiNative.startFilteringMulticastV4Packets();
                    break;
                case WifiStateMachine.CMD_GET_CHANNEL_LIST_5G /*131572*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, null);
                    break;
                case WifiStateMachine.CMD_PNO_PERIODIC_SCAN /*131575*/:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_UPDATE_WIFIPRO_CONFIGURATIONS /*131672*/:
                    break;
                case WifiStateMachine.CMD_GET_SUPPORT_VOWIFI_DETECT /*131774*/:
                    WifiStateMachine.this.processIsSupportVoWifiDetect(message);
                    break;
                case WifiStateMachine.GOOD_LINK_DETECTED /*131874*/:
                    WifiStateMachine.this.log("GOOD_LINK_DETECTED, state = DefaultState");
                    WifiStateMachine.this.setWifiBackgroundStatus(false);
                    break;
                case WifiStateMachine.PORTAL_NOTIFY_CHANGED /*131875*/:
                    WifiQualifiedNetworkSelector -get98 = WifiStateMachine.this.mWifiQualifiedNetworkSelector;
                    if (message.arg1 == 1) {
                        z = true;
                    }
                    if (message.obj != null) {
                        str = (String) message.obj;
                    }
                    -get98.portalNotifyChanged(z, str);
                    break;
                case WifiStateMachine.CMD_SCE_WIFI_OFF_TIMEOUT /*131888*/:
                case WifiStateMachine.CMD_SCE_WIFI_ON_TIMEOUT /*131889*/:
                case WifiStateMachine.CMD_SCE_WIFI_CONNECT_TIMEOUT /*131890*/:
                case WifiStateMachine.CMD_SCE_WIFI_REASSOC_TIMEOUT /*131891*/:
                    WifiStateMachine.this.log("wifi self cure timeout!" + message);
                    WifiStateMachine.this.notifySelfCureComplete(false);
                    break;
                case WifiStateMachine.CMD_SCE_STOP_SELF_CURE /*131892*/:
                    WifiStateMachine.this.log("CMD_SCE_STOP_SELF_CURE, arg1 =" + message.arg1);
                    WifiStateMachine.this.stopSelfCureWifi(message.arg1);
                    if (message.arg1 == 0) {
                        if (WifiStateMachine.this.getCurrentState() == WifiStateMachine.this.mDisconnectedState) {
                            WifiStateMachine.this.setNetworkDetailedState(DetailedState.DISCONNECTED);
                            WifiStateMachine.this.sendNetworkStateChangeBroadcast(null);
                            break;
                        }
                        WifiStateMachine.this.log("CMD_SCE_STOP_SELF_CURE, to disconnect because of wifi self cure failed.");
                        WifiStateMachine.this.mWifiNative.disconnect();
                        WifiStateMachine.this.handleNetworkDisconnect();
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_SCE_RESTORE /*131893*/:
                    if (WifiStateMachine.this.mNetworkAgent == null) {
                        WifiStateMachine.this.log("CMD_SCE_RESTORE, use networkAgent to sendNetworkInfo");
                        new WifiNetworkAgent(WifiStateMachine.this.getHandler().getLooper(), WifiStateMachine.this.mContext, "WifiNetworkAgent", WifiStateMachine.this.mNetworkInfo, WifiStateMachine.this.mNetworkCapabilitiesFilter, WifiStateMachine.this.mLinkProperties, 100, WifiStateMachine.this.mNetworkMisc).sendNetworkInfo(WifiStateMachine.this.mNetworkInfo);
                        break;
                    }
                    WifiStateMachine.this.log("CMD_SCE_RESTORE, use mNetworkAgent to sendNetworkInfo");
                    WifiStateMachine.this.mNetworkAgent.sendNetworkInfo(WifiStateMachine.this.mNetworkInfo);
                    break;
                case WifiStateMachine.CMD_SCE_NOTIFY_WIFI_DISABLED /*131894*/:
                    WifiStateMachine.this.log("CMD_SCE_NOTIFY_WIFI_DISABLED, set WIFI_STATE_DISABLED");
                    WifiStateMachine.this.setWifiState(1);
                    break;
                case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /*143371*/:
                    WifiStateMachine.this.mP2pConnected.set(message.obj.isConnected());
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*143372*/:
                    wifiStateMachine = WifiStateMachine.this;
                    if (message.arg1 == 1) {
                        z = true;
                    }
                    wifiStateMachine.mTemporarilyDisconnectWifi = z;
                    WifiStateMachine.this.replyToMessage(message, WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE);
                    break;
                case WifiMonitor.DRIVER_HUNG_EVENT /*147468*/:
                    WifiStateMachine.this.setSupplicantRunning(false);
                    WifiStateMachine.this.setSupplicantRunning(true);
                    break;
                case WifiMonitor.EVENT_ANT_CORE_ROB /*147757*/:
                    WifiStateMachine.this.handleAntenaPreempted();
                    break;
                case 151553:
                    WifiStateMachine.this.replyToMessage(message, 151554, 2);
                    break;
                case 151556:
                    WifiStateMachine.this.replyToMessage(message, 151557, 2);
                    break;
                case 151559:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_FAIL;
                    WifiStateMachine.this.replyToMessage(message, 151560, 2);
                    break;
                case 151562:
                    WifiStateMachine.this.replyToMessage(message, 151564, 2);
                    break;
                case 151566:
                    WifiStateMachine.this.replyToMessage(message, 151567, 2);
                    break;
                case 151569:
                    WifiStateMachine.this.replyToMessage(message, 151570, 2);
                    break;
                case 151572:
                    WifiStateMachine.this.replyToMessage(message, 151574, 2);
                    break;
                default:
                    WifiStateMachine.this.loge("Error! unhandled message" + message);
                    break;
            }
            return true;
        }
    }

    class DisconnectedState extends State {
        DisconnectedState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.log(getName());
            }
            if (WifiStateMachine.this.mTemporarilyDisconnectWifi) {
                WifiStateMachine.this.mWifiP2pChannel.sendMessage(WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE);
                return;
            }
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.logd(" Enter DisconnectedState screenOn=" + WifiStateMachine.this.mScreenOn);
            }
            WifiStateMachine.this.handleDisconnectedInWifiPro();
            WifiStateMachine.this.handleStopWifiRepeater(WifiStateMachine.this.mWifiP2pChannel);
            WifiStateMachine.this.mAutoRoaming = false;
            if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                WifiStateMachine.this.mWifiConnectivityManager.handleConnectionStateChanged(2);
            }
            if (!(WifiStateMachine.this.mNoNetworksPeriodicScan == 0 || WifiStateMachine.this.mP2pConnected.get() || WifiStateMachine.this.mWifiConfigManager.getSavedNetworks().size() != 0)) {
                WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                WifiStateMachine wifiStateMachine2 = WifiStateMachine.this;
                WifiStateMachine wifiStateMachine3 = WifiStateMachine.this;
                wifiStateMachine.sendMessageDelayed(wifiStateMachine2.obtainMessage(WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN, wifiStateMachine3.mPeriodicScanToken = wifiStateMachine3.mPeriodicScanToken + 1, 0), (long) WifiStateMachine.this.mNoNetworksPeriodicScan);
            }
            WifiStateMachine.this.mDisconnectedTimeStamp = System.currentTimeMillis();
            if (WifiStateMachine.this.mScanUpdate) {
                WifiStateMachine.this.mFrameworkShortScanCount = 0;
            }
        }

        public boolean processMessage(Message message) {
            boolean z = true;
            boolean z2 = false;
            boolean ret = true;
            WifiStateMachine.this.logStateAndMessage(message, this);
            WifiStateMachine wifiStateMachine;
            WifiStateMachine wifiStateMachine2;
            WifiStateMachine wifiStateMachine3;
            switch (message.what) {
                case WifiStateMachine.CMD_REMOVE_NETWORK /*131125*/:
                case WifiStateMachine.CMD_REMOVE_APP_CONFIGURATIONS /*131169*/:
                case WifiStateMachine.CMD_REMOVE_USER_CONFIGURATIONS /*131224*/:
                case 151556:
                    wifiStateMachine = WifiStateMachine.this;
                    wifiStateMachine2 = WifiStateMachine.this;
                    wifiStateMachine3 = WifiStateMachine.this;
                    wifiStateMachine.sendMessageDelayed(wifiStateMachine2.obtainMessage(WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN, wifiStateMachine3.mPeriodicScanToken = wifiStateMachine3.mPeriodicScanToken + 1, 0), (long) WifiStateMachine.this.mNoNetworksPeriodicScan);
                    ret = false;
                    break;
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    if (WifiStateMachine.this.checkOrDeferScanAllowed(message)) {
                        ret = false;
                        break;
                    }
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_REFUSED;
                    return true;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (WifiStateMachine.this.processDisconnectedSetMode(message)) {
                        Log.d("HwWifiStateMachine", "DisconnectedState process CMD_SET_OPERATIONAL_MODE");
                    } else if (message.arg1 != 1) {
                        WifiStateMachine.this.mOperationalMode = message.arg1;
                        WifiStateMachine.this.mWifiConfigManager.disableAllNetworksNative();
                        if (WifiStateMachine.this.mOperationalMode == 3) {
                            WifiStateMachine.this.mWifiP2pChannel.sendMessage(WifiStateMachine.CMD_DISABLE_P2P_REQ);
                            WifiStateMachine.this.setWifiState(1);
                        }
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mScanModeState);
                    }
                    WifiStateMachine.this.mWifiConfigManager.setAndEnableLastSelectedConfiguration(-1);
                    break;
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                    if (!WifiStateMachine.this.mTemporarilyDisconnectWifi) {
                        ret = false;
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN /*131160*/:
                    if (!WifiStateMachine.this.mP2pConnected.get() && WifiStateMachine.this.mNoNetworksPeriodicScan != 0 && message.arg1 == WifiStateMachine.this.mPeriodicScanToken && WifiStateMachine.this.mWifiConfigManager.getSavedNetworks().size() == 0) {
                        WifiStateMachine.this.startScan(-1, -1, null, WifiStateMachine.WIFI_WORK_SOURCE);
                        wifiStateMachine = WifiStateMachine.this;
                        wifiStateMachine2 = WifiStateMachine.this;
                        wifiStateMachine3 = WifiStateMachine.this;
                        wifiStateMachine.sendMessageDelayed(wifiStateMachine2.obtainMessage(WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN, wifiStateMachine3.mPeriodicScanToken = wifiStateMachine3.mPeriodicScanToken + 1, 0), (long) WifiStateMachine.this.mNoNetworksPeriodicScan);
                        break;
                    }
                case WifiStateMachine.CMD_SCREEN_STATE_CHANGED /*131167*/:
                    wifiStateMachine2 = WifiStateMachine.this;
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    wifiStateMachine2.handleScreenStateChanged(z);
                    break;
                case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /*143371*/:
                    WifiStateMachine.this.mP2pConnected.set(message.obj.isConnected());
                    if (!WifiStateMachine.this.mP2pConnected.get()) {
                        if (WifiStateMachine.this.mWifiConfigManager.getSavedNetworks().size() == 0) {
                            if (WifiStateMachine.DBG) {
                                WifiStateMachine.this.log("Turn on scanning after p2p disconnected");
                            }
                            wifiStateMachine = WifiStateMachine.this;
                            wifiStateMachine2 = WifiStateMachine.this;
                            wifiStateMachine3 = WifiStateMachine.this;
                            wifiStateMachine.sendMessageDelayed(wifiStateMachine2.obtainMessage(WifiStateMachine.CMD_NO_NETWORKS_PERIODIC_SCAN, wifiStateMachine3.mPeriodicScanToken = wifiStateMachine3.mPeriodicScanToken + 1, 0), (long) WifiStateMachine.this.mNoNetworksPeriodicScan);
                            break;
                        }
                    }
                    WifiStateMachine.this.mWifiNative.setScanInterval(((int) WifiStateMachine.this.mFacade.getLongSetting(WifiStateMachine.this.mContext, "wifi_scan_interval_p2p_connected_ms", (long) WifiStateMachine.this.mContext.getResources().getInteger(17694768))) / 1000);
                    break;
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    StateChangeResult stateChangeResult = message.obj;
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.logd("SUPPLICANT_STATE_CHANGE_EVENT state=" + stateChangeResult.state + " -> state= " + WifiInfo.getDetailedStateOf(stateChangeResult.state) + " debouncing=" + WifiStateMachine.this.linkDebouncing);
                    }
                    WifiStateMachine.this.setNetworkDetailedState(WifiInfo.getDetailedStateOf(stateChangeResult.state));
                    ret = false;
                    break;
                case WifiMonitor.RSN_PMKID_MISMATCH_EVENT /*147519*/:
                    int nid = WifiStateMachine.this.mCurrentAssociateNetworkId;
                    WifiConfiguration currentNet = new WifiConfiguration(WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(nid));
                    if (currentNet.enterpriseConfig != null) {
                        WifiStateMachine.this.mTls12ConfKey = currentNet.configKey(true);
                        WifiEnterpriseConfig wifiEnterpriseConfig = currentNet.enterpriseConfig;
                        if (!currentNet.enterpriseConfig.getTls12Enable()) {
                            z2 = true;
                        }
                        wifiEnterpriseConfig.setTls12Enable(z2);
                        WifiStateMachine.this.mWifiConfigManager.saveNetwork(currentNet, -1);
                        Log.e(WifiStateMachine.TAG, "NetWork ID =" + nid + " switch to TLS1.2: " + currentNet.enterpriseConfig.getTls12Enable());
                        break;
                    }
                    break;
                default:
                    ret = false;
                    break;
            }
            return ret;
        }

        public void exit() {
            if (WifiStateMachine.this.mScanUpdate) {
                WifiStateMachine.this.mFrameworkShortScanCount = 0;
            }
            if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                WifiStateMachine.this.mWifiConnectivityManager.handleConnectionStateChanged(3);
            }
        }
    }

    class DisconnectingState extends State {
        DisconnectingState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.logd(" Enter DisconnectingState State screenOn=" + WifiStateMachine.this.mScreenOn);
            }
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            wifiStateMachine.disconnectingWatchdogCount++;
            WifiStateMachine.this.logd("Start Disconnecting Watchdog " + WifiStateMachine.this.disconnectingWatchdogCount);
            WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_DISCONNECTING_WATCHDOG_TIMER, WifiStateMachine.this.disconnectingWatchdogCount, 0), 5000);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.deferMessage(message);
                    return true;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.deferMessage(message);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_DISCONNECTING_WATCHDOG_TIMER /*131168*/:
                    if (WifiStateMachine.this.disconnectingWatchdogCount == message.arg1) {
                        if (WifiStateMachine.DBG) {
                            WifiStateMachine.this.log("disconnecting watchdog! -> disconnect");
                        }
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        break;
                    }
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    WifiStateMachine.this.deferMessage(message);
                    WifiStateMachine.this.handleNetworkDisconnect();
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class DriverStartedState extends State {
        DriverStartedState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.logd("DriverStartedState enter");
            }
            if (WifiStateMachine.this.mWifiScanner == null) {
                WifiStateMachine.this.mWifiScanner = WifiStateMachine.this.mFacade.makeWifiScanner(WifiStateMachine.this.mContext, WifiStateMachine.this.getHandler().getLooper());
                WifiStateMachine.this.logd("***************init  mWifiConnectivityManager");
                WifiStateMachine.this.mWifiConnectivityManager = HwWifiServiceFactory.getHwWifiServiceManager().createHwWifiConnectivityManager(WifiStateMachine.this.mContext, WifiStateMachine.this, WifiStateMachine.this.mWifiScanner, WifiStateMachine.this.mWifiConfigManager, WifiStateMachine.this.mWifiInfo, WifiStateMachine.this.mWifiQualifiedNetworkSelector, WifiStateMachine.this.mWifiInjector, WifiStateMachine.this.getHandler().getLooper());
                if (WifiStateMachine.this.mWifiConnectivityManager == null) {
                    WifiStateMachine.this.loge("null == mWifiConnectivityManager");
                }
            }
            WifiStateMachine.this.mWifiLogger.startLogging(WifiStateMachine.DBG);
            WifiStateMachine.this.mIsRunning = true;
            WifiStateMachine.this.updateBatteryWorkSource(null);
            WifiStateMachine.this.mWifiNative.setBluetoothCoexistenceScanMode(WifiStateMachine.this.mBluetoothConnectionActive);
            WifiStateMachine.this.setNetworkDetailedState(DetailedState.DISCONNECTED);
            WifiStateMachine.this.mWifiNative.stopFilteringMulticastV4Packets();
            WifiStateMachine.this.mWifiNative.stopFilteringMulticastV6Packets();
            if (WifiStateMachine.this.enterDriverStartedStateByMode()) {
                Log.d("HwWifiStateMachine", "DriverStartedState enter transitionTo mDisconnectedState");
            } else if (WifiStateMachine.this.mOperationalMode != 1) {
                WifiStateMachine.this.mWifiNative.disconnect();
                WifiStateMachine.this.mWifiConfigManager.disableAllNetworksNative();
                if (WifiStateMachine.this.mOperationalMode == 3) {
                    if (WifiStateMachine.this.mWifiSettingStore.isWifiToggleEnabled()) {
                        WifiStateMachine.this.loge("isWifiToggleEnabled = true,donot setwifistate to disable");
                    } else {
                        WifiStateMachine.this.setWifiState(1);
                    }
                }
                WifiStateMachine.this.transitionTo(WifiStateMachine.this.mScanModeState);
            } else {
                WifiStateMachine.this.mWifiNative.status();
                WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
            }
            if (WifiStateMachine.this.mScreenBroadcastReceived.get()) {
                boolean z;
                WifiNative -get95 = WifiStateMachine.this.mWifiNative;
                if (WifiStateMachine.this.mSuspendOptNeedsDisabled == 0) {
                    z = WifiStateMachine.this.mUserWantsSuspendOpt.get();
                } else {
                    z = false;
                }
                -get95.setSuspendOptimizations(z);
                WifiStateMachine.this.mWifiConnectivityManager.handleScreenStateChanged(WifiStateMachine.this.mScreenOn);
            } else {
                WifiStateMachine.this.handleScreenStateChanged(((PowerManager) WifiStateMachine.this.mContext.getSystemService("power")).isScreenOn());
            }
            WifiStateMachine.this.mWifiNative.setPowerSave(true);
            if (WifiStateMachine.this.mP2pSupported && (WifiStateMachine.this.mOperationalMode == 1 || WifiStateMachine.this.isScanAndManualConnectMode())) {
                WifiStateMachine.this.mWifiP2pChannel.sendMessage(WifiStateMachine.CMD_ENABLE_P2P);
            }
            Intent intent = new Intent("wifi_scan_available");
            intent.addFlags(67108864);
            intent.putExtra("scan_enabled", 3);
            WifiStateMachine.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            WifiStateMachine.this.mWifiNative.setWifiLinkLayerStats("wlan0", 1);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_DRIVER /*131085*/:
                    if (WifiStateMachine.this.mOperationalMode == 1) {
                        WifiStateMachine.this.mWifiConfigManager.enableAllNetworks();
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_STOP_DRIVER /*131086*/:
                    int mode = message.arg1;
                    WifiStateMachine.this.log("stop driver");
                    WifiStateMachine.this.mWifiConfigManager.disableAllNetworksNative();
                    WifiStateMachine.this.mLastEnableAllNetworksTime = 0;
                    if (WifiStateMachine.this.getCurrentState() != WifiStateMachine.this.mDisconnectedState) {
                        WifiStateMachine.this.mWifiNative.disconnect();
                        WifiStateMachine.this.handleNetworkDisconnect();
                    }
                    WifiStateMachine.this.mWakeLock.acquire();
                    WifiStateMachine.this.mWifiNative.stopDriver();
                    WifiStateMachine.this.mWakeLock.release();
                    if (!WifiStateMachine.this.mP2pSupported) {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDriverStoppingState);
                        break;
                    }
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mWaitForP2pDisableState);
                    break;
                case WifiStateMachine.CMD_BLUETOOTH_ADAPTER_STATE_CHANGE /*131103*/:
                    WifiStateMachine.this.mBluetoothConnectionActive = message.arg1 != 0;
                    WifiStateMachine.this.mWifiNative.setBluetoothCoexistenceScanMode(WifiStateMachine.this.mBluetoothConnectionActive);
                    break;
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.handleScanRequest(message);
                    break;
                case WifiStateMachine.CMD_SET_HIGH_PERF_MODE /*131149*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.setSuspendOptimizationsNative(2, true);
                        break;
                    }
                    WifiStateMachine.this.setSuspendOptimizationsNative(2, false);
                    break;
                case WifiStateMachine.CMD_SET_SUSPEND_OPT_ENABLED /*131158*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.setSuspendOptimizationsNative(4, false);
                        break;
                    }
                    WifiStateMachine.this.setSuspendOptimizationsNative(4, true);
                    WifiStateMachine.this.mSuspendWakeLock.release();
                    break;
                case WifiStateMachine.CMD_SET_FREQUENCY_BAND /*131162*/:
                    int band = message.arg1;
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("set frequency band " + band);
                    }
                    if (!WifiStateMachine.this.mWifiNative.setBand(band)) {
                        WifiStateMachine.this.loge("Failed to set frequency band " + band);
                        break;
                    }
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.logd("did set frequency band " + band);
                    }
                    WifiStateMachine.this.mFrequencyBand.set(band);
                    WifiStateMachine.this.mWifiNative.bssFlush();
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.logd("done set frequency band " + band);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_ENABLE_TDLS /*131164*/:
                    if (message.obj != null) {
                        WifiStateMachine.this.mWifiNative.startTdls(message.obj, message.arg1 == 1);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_STOP_IP_PACKET_OFFLOAD /*131233*/:
                    int slot = message.arg1;
                    int ret = WifiStateMachine.this.stopWifiIPPacketOffload(slot);
                    if (WifiStateMachine.this.mNetworkAgent != null) {
                        WifiStateMachine.this.mNetworkAgent.onPacketKeepaliveEvent(slot, ret);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER /*131238*/:
                    if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                        WifiStateMachine.this.mWifiConnectivityManager.enable(message.arg1 == 1);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_ENABLE_AUTOJOIN_WHEN_ASSOCIATED /*131239*/:
                    boolean allowed = message.arg1 > 0;
                    boolean old_state = WifiStateMachine.this.mWifiConfigManager.getEnableAutoJoinWhenAssociated();
                    WifiStateMachine.this.mWifiConfigManager.setEnableAutoJoinWhenAssociated(allowed);
                    if (!old_state && allowed && WifiStateMachine.this.mScreenOn && WifiStateMachine.this.getCurrentState() == WifiStateMachine.this.mConnectedState && WifiStateMachine.this.mWifiConnectivityManager != null) {
                        WifiStateMachine.this.mWifiConnectivityManager.forceConnectivityScan();
                        break;
                    }
                case WifiStateMachine.CMD_CONFIG_ND_OFFLOAD /*131276*/:
                    WifiStateMachine.this.mWifiNative.configureNeighborDiscoveryOffload(message.arg1 > 0);
                    break;
                case WifiStateMachine.CMD_GET_CHANNEL_LIST_5G /*131572*/:
                    int[] channel = WifiStateMachine.this.mWifiNative.getChannelsForBand(2);
                    if (channel != null && channel.length > 0) {
                        for (int i = 0; i < channel.length; i++) {
                            channel[i] = WifiStateMachine.this.convertFrequencyToChannelNumber(channel[i]);
                        }
                    }
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) channel);
                    break;
                case WifiStateMachine.CMD_WIFI_SCAN_REJECT_SEND_SCAN_RESULT /*131578*/:
                    WifiStateMachine.this.logd("**wifi_scan_reject sendScanResultsAvailableBroadcast*****");
                    WifiStateMachine.this.sendScanResultsAvailableBroadcast(false);
                    break;
                case WifiMonitor.ANQP_DONE_EVENT /*147500*/:
                    WifiStateMachine.this.mWifiConfigManager.notifyANQPDone((Long) message.obj, message.arg1 != 0);
                    break;
                case WifiMonitor.RX_HS20_ANQP_ICON_EVENT /*147509*/:
                    WifiStateMachine.this.mWifiConfigManager.notifyIconReceived((IconEvent) message.obj);
                    break;
                case WifiMonitor.HS20_REMEDIATION_EVENT /*147517*/:
                    WifiStateMachine.this.wnmFrameReceived((WnmData) message.obj);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            WifiStateMachine.this.mWifiLogger.stopLogging();
            WifiStateMachine.this.mIsRunning = false;
            WifiStateMachine.this.updateBatteryWorkSource(null);
            WifiStateMachine.this.setWiFiProScanResultList(WifiStateMachine.this.syncGetScanResultsList());
            WifiStateMachine.this.mScanResults = new ArrayList();
            Intent intent = new Intent("wifi_scan_available");
            intent.addFlags(67108864);
            intent.putExtra("scan_enabled", 1);
            WifiStateMachine.this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            WifiStateMachine.this.mBufferedScanMsg.clear();
        }
    }

    class DriverStartingState extends State {
        private int mTries;

        DriverStartingState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.log(getName() + "\n");
            }
            this.mTries = 1;
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            WifiStateMachine wifiStateMachine2 = WifiStateMachine.this;
            WifiStateMachine wifiStateMachine3 = WifiStateMachine.this;
            wifiStateMachine.sendMessageDelayed(wifiStateMachine2.obtainMessage(WifiStateMachine.CMD_DRIVER_START_TIMED_OUT, wifiStateMachine3.mDriverStartToken = wifiStateMachine3.mDriverStartToken + 1, 0), 10000);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_DRIVER /*131085*/:
                case WifiStateMachine.CMD_STOP_DRIVER /*131086*/:
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                case WifiStateMachine.CMD_SET_FREQUENCY_BAND /*131162*/:
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                case WifiMonitor.WPS_OVERLAP_EVENT /*147466*/:
                case WifiMonitor.WAPI_AUTHENTICATION_FAILURE_EVENT /*147474*/:
                case WifiMonitor.WAPI_CERTIFICATION_FAILURE_EVENT /*147475*/:
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                case WifiMonitor.VOWIFI_DETECT_IRQ_STR_EVENT /*147520*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_DRIVER_START_TIMED_OUT /*131091*/:
                    if (message.arg1 == WifiStateMachine.this.mDriverStartToken) {
                        if (this.mTries < 2) {
                            WifiStateMachine.this.loge("Driver start failed, retrying");
                            WifiStateMachine.this.mWakeLock.acquire();
                            WifiStateMachine.this.mWifiNative.startDriver();
                            WifiStateMachine.this.mWakeLock.release();
                            this.mTries++;
                            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                            WifiStateMachine wifiStateMachine2 = WifiStateMachine.this;
                            WifiStateMachine wifiStateMachine3 = WifiStateMachine.this;
                            wifiStateMachine.sendMessageDelayed(wifiStateMachine2.obtainMessage(WifiStateMachine.CMD_DRIVER_START_TIMED_OUT, wifiStateMachine3.mDriverStartToken = wifiStateMachine3.mDriverStartToken + 1, 0), 10000);
                            break;
                        }
                        WifiStateMachine.this.loge("Failed to start driver after " + this.mTries);
                        WifiStateMachine.this.setSupplicantRunning(false);
                        WifiStateMachine.this.setSupplicantRunning(true);
                        break;
                    }
                    break;
                case WifiMonitor.SCAN_RESULTS_EVENT /*147461*/:
                case WifiMonitor.SCAN_FAILED_EVENT /*147473*/:
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    if (SupplicantState.isDriverActive(WifiStateMachine.this.handleSupplicantStateChange(message))) {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDriverStartedState);
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class DriverStoppedState extends State {
        DriverStoppedState() {
        }

        public void enter() {
            WifiStateMachine.this.log(getName() + " enter");
            if (WifiStateMachine.DBG) {
                Log.d(WifiStateMachine.TAG, "Setting country code will cause driver hanging while driver stopped, turn off WifiCountryCode switcher.");
            }
            WifiStateMachine.this.mCountryCode.setReadyForChange(false);
        }

        public void exit() {
            if (WifiStateMachine.DBG) {
                Log.d(WifiStateMachine.TAG, "Leaving DriverStoppedState, turn on WifiCountryCode switcher.");
            }
            WifiStateMachine.this.mCountryCode.setReadyForChange(true);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_DRIVER /*131085*/:
                    WifiStateMachine.this.mWakeLock.acquire();
                    WifiStateMachine.this.mWifiNative.startDriver();
                    WifiStateMachine.this.mWakeLock.release();
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDriverStartingState);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    if (SupplicantState.isDriverActive(message.obj.state)) {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDriverStartedState);
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class DriverStoppingState extends State {
        DriverStoppingState() {
        }

        public void enter() {
            WifiStateMachine.this.log(getName() + " enter");
            WifiStateMachine.this.mWakeLock.acquire();
            WifiStateMachine.this.mWifiNative.stopDriver();
            WifiStateMachine.this.mWakeLock.release();
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_DRIVER /*131085*/:
                case WifiStateMachine.CMD_STOP_DRIVER /*131086*/:
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                case WifiStateMachine.CMD_SET_FREQUENCY_BAND /*131162*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    if (WifiStateMachine.this.handleSupplicantStateChange(message) == SupplicantState.INTERFACE_DISABLED) {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDriverStoppedState);
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class InitialState extends State {
        InitialState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.log(getName() + "\n");
            }
            WifiStateMachine.this.mFeatureSet = 0;
            WifiStateMachine.this.mWifiNative.stopHal();
            WifiStateMachine.this.mWifiNative.unloadDriver();
            if (WifiStateMachine.this.mWifiP2pChannel == null) {
                WifiStateMachine.this.mWifiP2pChannel = new AsyncChannel();
                WifiStateMachine.this.mWifiP2pChannel.connect(WifiStateMachine.this.mContext, WifiStateMachine.this.getHandler(), WifiStateMachine.this.mWifiP2pServiceImpl.getP2pStateMachineMessenger());
            }
            if (WifiStateMachine.this.mWifiApConfigStore == null) {
                WifiStateMachine.this.mWifiApConfigStore = WifiStateMachine.this.mFacade.makeApConfigStore(WifiStateMachine.this.mContext, WifiStateMachine.this.mBackupManagerProxy);
            }
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SUPPLICANT /*131083*/:
                    if (WifiStateMachine.this.mWifiStatStore != null) {
                        WifiStateMachine.this.mWifiStatStore.updateWifiTriggerState(true);
                    }
                    if (!WifiStateMachine.this.mWifiNative.loadDriver()) {
                        WifiStateMachine.this.uploader.e(52, "{ACT:1,STATUS:failed,DETAIL:load driver}");
                        WifiStateMachine.this.loge("Failed to load driver");
                        WifiStateMachine.this.setWifiState(4);
                        if (WifiStateMachine.this.mWiFiCHRManager != null) {
                            WifiStateMachine.this.mWiFiCHRManager.updateWifiException(80, "DIRVER_FAILED");
                            break;
                        }
                    }
                    try {
                        WifiStateMachine.this.mNwService.wifiFirmwareReload(WifiStateMachine.this.mInterfaceName, "STA");
                        try {
                            WifiStateMachine.this.mNwService.setInterfaceDown(WifiStateMachine.this.mInterfaceName);
                            WifiStateMachine.this.mNwService.clearInterfaceAddresses(WifiStateMachine.this.mInterfaceName);
                            WifiStateMachine.this.mNwService.setInterfaceIpv6PrivacyExtensions(WifiStateMachine.this.mInterfaceName, true);
                            WifiStateMachine.this.mNwService.disableIpv6(WifiStateMachine.this.mInterfaceName);
                        } catch (RemoteException re) {
                            WifiStateMachine.this.loge("Unable to change interface settings: " + re);
                        } catch (IllegalStateException ie) {
                            WifiStateMachine.this.loge("Unable to change interface settings: " + ie);
                        }
                        WifiStateMachine.this.mWifiMonitor.killSupplicant(WifiStateMachine.this.mP2pSupported);
                        if (WifiStateMachine.this.mWifiNative.startHal()) {
                            if (!WifiStateMachine.this.mWifiNative.startSupplicant(WifiStateMachine.this.mP2pSupported)) {
                                WifiStateMachine.this.uploader.e(52, "{ACT:1,STATUS:failed,DETAIL:start supplicant}");
                                WifiStateMachine.this.loge("Failed to start supplicant!");
                                WifiStateMachine.this.setWifiState(4);
                                if (WifiStateMachine.this.mWiFiCHRManager != null) {
                                    WifiStateMachine.this.mWiFiCHRManager.updateWifiException(80, "START_SUPPLICANT_FAILED");
                                    break;
                                }
                            }
                            WifiStateMachine.this.setWifiState(2);
                            if (WifiStateMachine.DBG) {
                                WifiStateMachine.this.log("Supplicant start successful");
                            }
                            WifiStateMachine.this.uploader.e(52, "{ACT:1,STATUS:success,DETAIL:start supplicant}");
                            WifiStateMachine.this.mWifiMonitor.startMonitoring(WifiStateMachine.this.mInterfaceName);
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSupplicantStartingState);
                            break;
                        }
                        WifiStateMachine.this.loge("Failed to start HAL");
                        WifiStateMachine.this.setWifiState(4);
                        break;
                    } catch (Exception e) {
                        WifiStateMachine.this.uploader.e(52, "{ACT:1,STATUS:failed,DETAIL:reload STA firmware}");
                        WifiStateMachine.this.loge("Failed to reload STA firmware " + e);
                        if (WifiStateMachine.this.mWiFiCHRManager != null) {
                            WifiStateMachine.this.mWiFiCHRManager.updateWifiException(80, "FIRMWARE_FAILED");
                        }
                        WifiStateMachine.this.setWifiState(4);
                        return true;
                    }
                    break;
                case WifiStateMachine.CMD_START_AP /*131093*/:
                    if (!WifiStateMachine.this.setupDriverForSoftAp()) {
                        WifiStateMachine.this.setWifiApState(14, 0);
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                        break;
                    }
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSoftApState);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class IpManagerCallback extends Callback {
        IpManagerCallback() {
        }

        public void onPreDhcpAction() {
            WifiStateMachine.this.sendMessage(196611);
        }

        public void onPostDhcpAction() {
            WifiStateMachine.this.sendMessage(196612);
        }

        public void onNewDhcpResults(DhcpResults dhcpResults) {
            if (dhcpResults == null) {
                if (WifiStateMachine.this.mWifiStatStore != null) {
                    if (WifiStateMachine.this.mIpManager.getDhcpFlag() == 196609) {
                        WifiStateMachine.this.mWifiStatStore.updateDhcpState(4);
                    } else {
                        WifiStateMachine.this.mWifiStatStore.updateDhcpState(5);
                    }
                }
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_IPV4_PROVISIONING_FAILURE);
                WifiStateMachine.this.mWifiLastResortWatchdog.noteConnectionFailureAndTriggerIfNeeded(WifiStateMachine.this.getTargetSsid(), WifiStateMachine.this.mTargetRoamBSSID, 3);
            } else if ("CMD_TRY_CACHED_IP".equals(dhcpResults.domains)) {
                WifiStateMachine.this.sendMessage(196618);
            } else {
                if (!(WifiStateMachine.this.mWifiStatStore == null || dhcpResults.ipAddress == null || WifiStateMachine.this.mWifiConfigManager == null || WifiStateMachine.this.mNetworkInfo == null)) {
                    if (WifiStateMachine.this.mWifiConfigManager.isUsingStaticIp(WifiStateMachine.this.mLastNetworkId)) {
                        WifiStateMachine.this.mWifiStatStore.updateDhcpState(9);
                    } else if (DetailedState.OBTAINING_IPADDR == WifiStateMachine.this.mNetworkInfo.getDetailedState()) {
                        if ("getCachedDhcpResultsForCurrentConfig".equals(dhcpResults.domains)) {
                            WifiStateMachine.this.mWifiStatStore.updateDhcpState(16);
                        } else {
                            WifiStateMachine.this.mWifiStatStore.updateDhcpState(2);
                        }
                    } else if (DetailedState.CONNECTED == WifiStateMachine.this.mNetworkInfo.getDetailedState()) {
                        WifiStateMachine.this.mWifiStatStore.updateDhcpState(3);
                    }
                }
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_IPV4_PROVISIONING_SUCCESS, dhcpResults);
            }
        }

        public void onProvisioningSuccess(LinkProperties newLp) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_UPDATE_LINKPROPERTIES, newLp);
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_IP_CONFIGURATION_SUCCESSFUL);
        }

        public void onProvisioningFailure(LinkProperties newLp) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_IP_CONFIGURATION_LOST);
        }

        public void onLinkPropertiesChange(LinkProperties newLp) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_UPDATE_LINKPROPERTIES, newLp);
        }

        public void onReachabilityLost(String logMsg) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_IP_REACHABILITY_LOST, logMsg);
        }

        public void installPacketFilter(byte[] filter) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_INSTALL_PACKET_FILTER, filter);
        }

        public void setFallbackMulticastFilter(boolean enabled) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_SET_FALLBACK_PACKET_FILTERING, Boolean.valueOf(enabled));
        }

        public void setNeighborDiscoveryOffload(boolean enabled) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_CONFIG_ND_OFFLOAD, enabled ? 1 : 0);
        }
    }

    class L2ConnectedState extends State {
        L2ConnectedState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.log(getName());
            }
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            wifiStateMachine.mRssiPollToken = wifiStateMachine.mRssiPollToken + 1;
            if (WifiStateMachine.this.mEnableRssiPolling) {
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_RSSI_POLL, WifiStateMachine.this.mRssiPollToken, 0);
            }
            if (WifiStateMachine.this.mNetworkAgent != null) {
                WifiStateMachine.this.loge("Have NetworkAgent when entering L2Connected");
                WifiStateMachine.this.setNetworkDetailedState(DetailedState.DISCONNECTED);
            }
            WifiStateMachine.this.setNetworkDetailedState(DetailedState.CONNECTING);
            WifiStateMachine.this.mNetworkAgent = new WifiNetworkAgent(WifiStateMachine.this.getHandler().getLooper(), WifiStateMachine.this.mContext, "WifiNetworkAgent", WifiStateMachine.this.mNetworkInfo, WifiStateMachine.this.mNetworkCapabilitiesFilter, WifiStateMachine.this.mLinkProperties, 100, WifiStateMachine.this.mNetworkMisc);
            WifiStateMachine.this.clearCurrentConfigBSSID("L2ConnectedState");
            WifiStateMachine.this.mCountryCode.setReadyForChange(false);
            WifiStateMachine.this.mWifiMetrics.setWifiState(3);
        }

        public void exit() {
            WifiStateMachine.this.mIpManager.stop();
            if (WifiStateMachine.DBG) {
                StringBuilder sb = new StringBuilder();
                sb.append("leaving L2ConnectedState state nid=").append(Integer.toString(WifiStateMachine.this.mLastNetworkId));
                if (WifiStateMachine.this.mLastBssid != null) {
                    sb.append(" ").append(WifiStateMachine.this.mLastBssid);
                }
            }
            if (!(WifiStateMachine.this.mLastBssid == null && WifiStateMachine.this.mLastNetworkId == -1)) {
                WifiStateMachine.this.handleNetworkDisconnect();
            }
            WifiStateMachine.this.mCountryCode.setReadyForChange(true);
            WifiStateMachine.this.mWifiMetrics.setWifiState(2);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (WifiStateMachine.this.processL2ConnectedSetMode(message)) {
                        Log.d("HwWifiStateMachine", "L2ConnectedState process CMD_SET_OPERATIONAL_MODE");
                    } else if (message.arg1 != 1) {
                        WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_DISCONNECT);
                        WifiStateMachine.this.deferMessage(message);
                        if (message.arg1 == 3) {
                            WifiStateMachine.this.noteWifiDisabledWhileAssociated();
                        }
                    }
                    WifiStateMachine.this.mWifiConfigManager.setAndEnableLastSelectedConfiguration(-1);
                    break;
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                    WifiStateMachine.this.log("L2ConnectedState, case CMD_DISCONNECT, do disconnect");
                    WifiStateMachine.this.mWifiNative.disconnect();
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    break;
                case WifiStateMachine.CMD_ENABLE_RSSI_POLL /*131154*/:
                    WifiStateMachine.this.cleanWifiScore();
                    if (WifiStateMachine.this.mWifiConfigManager.mEnableRssiPollWhenAssociated.get()) {
                        WifiStateMachine.this.mEnableRssiPolling = message.arg1 == 1;
                    } else {
                        WifiStateMachine.this.mEnableRssiPolling = false;
                    }
                    WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                    wifiStateMachine.mRssiPollToken = wifiStateMachine.mRssiPollToken + 1;
                    if (WifiStateMachine.this.mEnableRssiPolling) {
                        WifiStateMachine.this.fetchRssiLinkSpeedAndFrequencyNative();
                        WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_RSSI_POLL, WifiStateMachine.this.mRssiPollToken, 0), WifiStateMachine.ALLOW_SEND_SCAN_RESULTS_BROADCAST_INTERVAL_MS);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_RSSI_POLL /*131155*/:
                    if (message.arg1 == WifiStateMachine.this.mRssiPollToken) {
                        if (WifiStateMachine.this.mWifiConfigManager.mEnableChipWakeUpWhenAssociated.get()) {
                            if (WifiStateMachine.DBG) {
                                WifiStateMachine.this.log(" get link layer stats " + WifiStateMachine.this.mWifiLinkLayerStatsSupported);
                            }
                            WifiLinkLayerStats stats = WifiStateMachine.this.getWifiLinkLayerStats(WifiStateMachine.DBG);
                            if (!(stats == null || WifiStateMachine.this.mWifiInfo.getRssi() == -127 || (stats.rssi_mgmt != 0 && stats.beacon_rx != 0))) {
                            }
                            WifiStateMachine.this.fetchRssiLinkSpeedAndFrequencyNative();
                            WifiStateMachine.this.mWifiScoreReport = WifiScoreReport.calculateScore(WifiStateMachine.this.mWifiInfo, WifiStateMachine.this.getCurrentWifiConfiguration(), WifiStateMachine.this.mWifiConfigManager, WifiStateMachine.this.mNetworkAgent, WifiStateMachine.this.mWifiScoreReport, WifiStateMachine.this.mAggressiveHandover);
                        }
                        WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_RSSI_POLL, WifiStateMachine.this.mRssiPollToken, 0), WifiStateMachine.ALLOW_SEND_SCAN_RESULTS_BROADCAST_INTERVAL_MS);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_DELAYED_NETWORK_DISCONNECT /*131159*/:
                    if (WifiStateMachine.this.linkDebouncing || !WifiStateMachine.this.mWifiConfigManager.mEnableLinkDebouncing) {
                        WifiStateMachine.this.logd("CMD_DELAYED_NETWORK_DISCONNECT and debouncing - disconnect " + message.arg1);
                        WifiStateMachine.this.linkDebouncing = false;
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        break;
                    }
                    WifiStateMachine.this.logd("CMD_DELAYED_NETWORK_DISCONNECT and not debouncing - ignore " + message.arg1);
                    return true;
                case WifiStateMachine.CMD_RESET_SIM_NETWORKS /*131173*/:
                    if (WifiStateMachine.this.mLastNetworkId != -1) {
                        if (WifiStateMachine.this.mWifiConfigManager.isSimConfig(WifiStateMachine.this.mWifiConfigManager.getWifiConfiguration(WifiStateMachine.this.mLastNetworkId))) {
                            WifiStateMachine.this.mWifiNative.disconnect();
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                        }
                    }
                    return false;
                case WifiStateMachine.CMD_IP_CONFIGURATION_SUCCESSFUL /*131210*/:
                    WifiStateMachine.this.log("L2ConnectedState, case CMD_IP_CONFIGURATION_SUCCESSFUL");
                    WifiStateMachine.this.handleSuccessfulIpConfiguration();
                    WifiStateMachine.this.reportConnectionAttemptEnd(1, 1);
                    if (WifiStateMachine.this.isHiLinkActive()) {
                        WifiStateMachine.this.setWifiBackgroundReason(6);
                    }
                    if (WifiStateMachine.this.mWiFiCHRManager != null) {
                        WifiStateMachine.this.mWiFiCHRManager.setLastNetIdFromUI(-1);
                    }
                    WifiStateMachine.this.notifyIpConfigCompleted();
                    if (!WifiStateMachine.this.ignoreEnterConnectedState()) {
                        if (!WifiStateMachine.this.isWifiProEvaluatingAP()) {
                            WifiStateMachine.this.sendConnectedState();
                            WifiStateMachine.this.sendHilinkConnectedBroadcast();
                            WifiStateMachine.this.transitionTo(WifiStateMachine.this.mConnectedState);
                            break;
                        }
                        WifiStateMachine.this.log("****WiFi's connected background, don't let Mobile Data down, keep dual networks up.");
                        WifiStateMachine.this.updateNetworkConcurrently();
                        WifiStateMachine.this.sendHilinkConnectedBroadcast();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mConnectedState);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_IP_CONFIGURATION_LOST /*131211*/:
                    WifiStateMachine.this.log("L2ConnectedState, case CMD_IP_CONFIGURATION_LOST");
                    WifiStateMachine.this.getWifiLinkLayerStats(true);
                    if (!WifiStateMachine.this.notifyIpConfigLostAndFixedBySce(WifiStateMachine.this.getCurrentWifiConfiguration())) {
                        WifiStateMachine.this.handleIpConfigurationLost();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                        break;
                    }
                    WifiStateMachine.this.log("L2ConnectedState, notifyIpConfigLostAndFixedBySce!!!!");
                    WifiStateMachine.this.removeMessages(WifiStateMachine.CMD_OBTAINING_IP_ADDRESS_WATCHDOG_TIMER);
                    break;
                case WifiStateMachine.CMD_ASSOCIATED_BSSID /*131219*/:
                    if (((String) message.obj) != null) {
                        WifiStateMachine.this.mLastBssid = (String) message.obj;
                        if (WifiStateMachine.this.mLastBssid != null && (WifiStateMachine.this.mWifiInfo.getBSSID() == null || !WifiStateMachine.this.mLastBssid.equals(WifiStateMachine.this.mWifiInfo.getBSSID()))) {
                            WifiStateMachine.this.mWifiInfo.setBSSID((String) message.obj);
                            WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                            break;
                        }
                    }
                    WifiStateMachine.this.logw("Associated command w/o BSSID");
                    break;
                case WifiStateMachine.CMD_IP_REACHABILITY_LOST /*131221*/:
                    if (WifiStateMachine.DBG && message.obj != null) {
                        WifiStateMachine.this.log((String) message.obj);
                    }
                    WifiStateMachine.this.handleIpReachabilityLost();
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    break;
                case WifiStateMachine.CMD_START_RSSI_MONITORING_OFFLOAD /*131234*/:
                case WifiStateMachine.CMD_RSSI_THRESHOLD_BREACH /*131236*/:
                    WifiStateMachine.this.processRssiThreshold((byte) message.arg1, message.what);
                    break;
                case WifiStateMachine.CMD_STOP_RSSI_MONITORING_OFFLOAD /*131235*/:
                    WifiStateMachine.this.stopRssiMonitoringOffload();
                    break;
                case WifiStateMachine.CMD_IPV4_PROVISIONING_SUCCESS /*131272*/:
                    WifiStateMachine.this.handleIPv4Success((DhcpResults) message.obj);
                    WifiStateMachine.this.makeHwDefaultIPTable((DhcpResults) message.obj);
                    if (WifiStateMachine.this.mWiFiCHRManager != null) {
                        DhcpResults dr = message.obj;
                        WifiStateMachine.this.mWiFiCHRManager.updateLeaseIP((long) (dr.leaseDuration >= 0 ? dr.leaseDuration : 31536000));
                    }
                    WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                    break;
                case WifiStateMachine.CMD_IPV4_PROVISIONING_FAILURE /*131273*/:
                    WifiStateMachine.this.mWifiLogger.captureBugReportData(4);
                    if (WifiStateMachine.this.mHwWifiCHRService != null) {
                        WifiStateMachine.this.mHwWifiCHRService.updateDhcpFailedState();
                    }
                    if (WifiStateMachine.this.mWiFiCHRManager != null) {
                        WifiStateMachine.this.mWiFiCHRManager.uploadDhcpException(DhcpClient.getDhcpError());
                    }
                    if (WifiStateMachine.DBG) {
                        if (WifiStateMachine.this.getCurrentWifiConfiguration() != null) {
                            WifiStateMachine.this.log("DHCP failure count=" + -1);
                        } else {
                            WifiStateMachine.this.log("DHCP failure count=" + -1);
                        }
                    }
                    WifiStateMachine.this.handleIPv4Failure();
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*143372*/:
                    if (message.arg1 == 1) {
                        WifiStateMachine.this.log("L2ConnectedState, case WifiP2pService.DISCONNECT_WIFI_REQUEST, do disconnect");
                        WifiStateMachine.this.mWifiNative.disconnect();
                        WifiStateMachine.this.mTemporarilyDisconnectWifi = true;
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                    WifiStateMachine.this.mWifiInfo.setBSSID((String) message.obj);
                    WifiStateMachine.this.mLastNetworkId = message.arg1;
                    WifiStateMachine.this.mWifiInfo.setNetworkId(WifiStateMachine.this.mLastNetworkId);
                    if (!WifiStateMachine.this.mLastBssid.equals((String) message.obj)) {
                        WifiStateMachine.this.mLastBssid = (String) message.obj;
                        WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                    }
                    WifiStateMachine.this.checkSelfCureWifiResult();
                    WifiStateMachine.this.saveWpsOkcConfiguration(WifiStateMachine.this.mLastNetworkId, WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.notifyWifiRoamingCompleted(WifiStateMachine.this.mLastBssid);
                    break;
                case 151553:
                    if (WifiStateMachine.this.mWifiInfo.getNetworkId() != message.arg1) {
                        return false;
                    }
                    if (WifiStateMachine.this.isWifiProEvaluatingAP()) {
                        WifiStateMachine.this.logd("==connection to same network==");
                        return false;
                    }
                    break;
                case 151572:
                    RssiPacketCountInfo info = new RssiPacketCountInfo();
                    WifiStateMachine.this.fetchRssiLinkSpeedAndFrequencyNative();
                    info.rssi = WifiStateMachine.this.mWifiInfo.getRssi();
                    WifiStateMachine.this.fetchPktcntNative(info);
                    WifiStateMachine.this.replyToMessage(message, 151573, (Object) info);
                    break;
                case 196611:
                    WifiStateMachine.this.handlePreDhcpSetup();
                    if (WifiStateMachine.this.mWifiStatStore != null) {
                        if (DetailedState.OBTAINING_IPADDR != WifiStateMachine.this.mNetworkInfo.getDetailedState()) {
                            if (DetailedState.CONNECTED == WifiStateMachine.this.mNetworkInfo.getDetailedState()) {
                                WifiStateMachine.this.mWifiStatStore.updateDhcpState(10);
                                break;
                            }
                        }
                        WifiStateMachine.this.mWifiStatStore.updateDhcpState(0);
                        break;
                    }
                    break;
                case 196612:
                    WifiStateMachine.this.handlePostDhcpSetup();
                    break;
                case 196614:
                    WifiStateMachine.this.mIpManager.completedPreDhcpAction();
                    break;
                case 196618:
                    DhcpResults dhcpResults = WifiStateMachine.this.getCachedDhcpResultsForCurrentConfig();
                    if (dhcpResults != null) {
                        WifiStateMachine.this.stopIpManager();
                        dhcpResults.domains = "getCachedDhcpResultsForCurrentConfig";
                        WifiStateMachine.this.mIpManager;
                        WifiStateMachine.this.mIpManager.startProvisioning(IpManager.buildProvisioningConfiguration().withStaticConfiguration(dhcpResults).withoutIpReachabilityMonitor().withApfCapabilities(WifiStateMachine.this.mWifiNative.getApfCapabilities()).build());
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class ObtainingIpState extends State {
        ObtainingIpState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                String key = "";
                if (WifiStateMachine.this.getCurrentWifiConfiguration() != null) {
                    key = WifiStateMachine.this.getCurrentWifiConfiguration().configKey();
                }
                WifiStateMachine.this.log("enter ObtainingIpState netId=" + Integer.toString(WifiStateMachine.this.mLastNetworkId) + " " + key + " " + " roam=" + WifiStateMachine.this.mAutoRoaming + " static=" + WifiStateMachine.this.mWifiConfigManager.isUsingStaticIp(WifiStateMachine.this.mLastNetworkId) + " watchdog= " + WifiStateMachine.this.obtainingIpWatchdogCount);
            }
            WifiStateMachine.this.linkDebouncing = false;
            WifiStateMachine.this.setNetworkDetailedState(DetailedState.OBTAINING_IPADDR);
            WifiStateMachine.this.clearCurrentConfigBSSID("ObtainingIpAddress");
            WifiStateMachine.this.stopIpManager();
            WifiStateMachine.this.mIpManager.setHttpProxy(WifiStateMachine.this.mWifiConfigManager.getProxyProperties(WifiStateMachine.this.mLastNetworkId));
            if (!TextUtils.isEmpty(WifiStateMachine.this.mTcpBufferSizes)) {
                WifiStateMachine.this.mIpManager.setTcpBufferSizes(WifiStateMachine.this.mTcpBufferSizes);
            }
            WifiStateMachine.this.tryUseStaticIpForFastConnecting(WifiStateMachine.this.mLastNetworkId);
            if (WifiStateMachine.this.mWifiConfigManager.isUsingStaticIp(WifiStateMachine.this.mLastNetworkId)) {
                StaticIpConfiguration config = WifiStateMachine.this.mWifiConfigManager.getStaticIpConfiguration(WifiStateMachine.this.mLastNetworkId);
                if (config.ipAddress == null) {
                    WifiStateMachine.this.logd("Static IP lacks address");
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_IPV4_PROVISIONING_FAILURE);
                    return;
                }
                WifiStateMachine.this.mIpManager;
                WifiStateMachine.this.mIpManager.startProvisioning(IpManager.buildProvisioningConfiguration().withStaticConfiguration(config).withoutIpReachabilityMonitor().withApfCapabilities(WifiStateMachine.this.mWifiNative.getApfCapabilities()).build());
                if (WifiStateMachine.this.mWifiStatStore != null) {
                    WifiStateMachine.this.mWifiStatStore.updateDhcpState(8);
                    return;
                }
                return;
            }
            WifiStateMachine.this.mIpManager;
            ProvisioningConfiguration prov = IpManager.buildProvisioningConfiguration().withPreDhcpAction().withoutIpReachabilityMonitor().withApfCapabilities(WifiStateMachine.this.mWifiNative.getApfCapabilities()).build();
            HwDhcpClient.putPendingSSID(WifiStateMachine.this.mWifiInfo.getBSSID());
            WifiStateMachine.this.setForceDhcpDiscovery(WifiStateMachine.this.mIpManager);
            WifiStateMachine.this.mIpManager.startProvisioning(prov);
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            wifiStateMachine.obtainingIpWatchdogCount++;
            WifiStateMachine.this.logd("Start Dhcp Watchdog " + WifiStateMachine.this.obtainingIpWatchdogCount);
            WifiStateMachine.this.getWifiLinkLayerStats(true);
            WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_OBTAINING_IP_ADDRESS_WATCHDOG_TIMER, WifiStateMachine.this.obtainingIpWatchdogCount, 0), 40000);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_SET_HIGH_PERF_MODE /*131149*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_OBTAINING_IP_ADDRESS_WATCHDOG_TIMER /*131165*/:
                    if (message.arg1 != WifiStateMachine.this.obtainingIpWatchdogCount) {
                        WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                        break;
                    }
                    WifiStateMachine.this.logd("ObtainingIpAddress: Watchdog Triggered, count=" + WifiStateMachine.this.obtainingIpWatchdogCount);
                    if (WifiStateMachine.this.mWiFiCHRManager != null) {
                        WifiStateMachine.this.mWiFiCHRManager.uploadDhcpException(DhcpClient.getDhcpError());
                    }
                    WifiStateMachine.this.handleIpConfigurationLost();
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    break;
                case WifiStateMachine.CMD_AUTO_CONNECT /*131215*/:
                case WifiStateMachine.CMD_AUTO_ROAM /*131217*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiStateMachine.CMD_AUTO_SAVE_NETWORK /*131218*/:
                case 151559:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.SCE_REQUEST_SET_STATIC_IP /*131884*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: SCE_REQUEST_SET_STATIC_IP");
                    WifiStateMachine.this.stopIpManager();
                    WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.SCE_START_SET_STATIC_IP, message.obj, 1000);
                    break;
                case WifiStateMachine.SCE_START_SET_STATIC_IP /*131885*/:
                    WifiStateMachine.this.logd("handle WifiStateMachine: SCE_START_SET_STATIC_IP");
                    WifiStateMachine.this.handleStaticIpConfig(WifiStateMachine.this.mIpManager, WifiStateMachine.this.mWifiNative, (StaticIpConfiguration) message.obj);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    WifiStateMachine.this.reportConnectionAttemptEnd(6, 1);
                    return false;
                default:
                    return false;
            }
            return true;
        }
    }

    class RoamingState extends State {
        boolean mAssociated;

        RoamingState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.log("RoamingState Enter mScreenOn=" + WifiStateMachine.this.mScreenOn);
            }
            WifiStateMachine.this.enterConnectedStateByMode();
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            wifiStateMachine.roamWatchdogCount++;
            WifiStateMachine.this.logd("Start Roam Watchdog " + WifiStateMachine.this.roamWatchdogCount);
            WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.this.obtainMessage(WifiStateMachine.CMD_ROAM_WATCHDOG_TIMER, WifiStateMachine.this.roamWatchdogCount, 0), 15000);
            this.mAssociated = false;
            WifiStateMachine.this.setWiFiProRoamingSSID(null);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            WifiConfiguration config;
            switch (message.what) {
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (message.arg1 != 1) {
                        WifiStateMachine.this.deferMessage(message);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_ROAM_WATCHDOG_TIMER /*131166*/:
                    if (WifiStateMachine.this.roamWatchdogCount == message.arg1) {
                        if (WifiStateMachine.DBG) {
                            WifiStateMachine.this.log("roaming watchdog! -> disconnect");
                        }
                        WifiStateMachine.this.mWifiMetrics.endConnectionEvent(9, 1);
                        WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                        wifiStateMachine.mRoamFailCount = wifiStateMachine.mRoamFailCount + 1;
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.mWifiNative.disconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_IP_CONFIGURATION_LOST /*131211*/:
                    config = WifiStateMachine.this.getCurrentWifiConfiguration();
                    if (config != null) {
                        WifiStateMachine.this.mWifiLogger.captureBugReportData(3);
                        WifiStateMachine.this.mWifiConfigManager.noteRoamingFailure(config, WifiConfiguration.ROAMING_FAILURE_IP_CONFIG);
                    }
                    return false;
                case WifiStateMachine.CMD_UNWANTED_NETWORK /*131216*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Roaming and CS doesnt want the network -> ignore");
                    }
                    return true;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                    if (!this.mAssociated) {
                        WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                        break;
                    }
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("roaming and Network connection established");
                    }
                    WifiStateMachine.this.mLastNetworkId = message.arg1;
                    WifiStateMachine.this.mLastBssid = (String) message.obj;
                    WifiStateMachine.this.saveWpsOkcConfiguration(WifiStateMachine.this.mLastNetworkId, WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.mWifiInfo.setBSSID(WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.mWifiInfo.setNetworkId(WifiStateMachine.this.mLastNetworkId);
                    if (WifiStateMachine.this.isWiFiProSwitchOnGoing() && WifiStateMachine.this.getWiFiProRoamingSSID() != null) {
                        WifiStateMachine.this.mWifiInfo.setSSID(WifiStateMachine.this.getWiFiProRoamingSSID());
                    }
                    if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                        WifiStateMachine.this.mWifiConnectivityManager.trackBssid(WifiStateMachine.this.mLastBssid, true);
                    }
                    WifiStateMachine.this.sendNetworkStateChangeBroadcast(WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.reportConnectionAttemptEnd(1, 1);
                    WifiStateMachine.this.clearCurrentConfigBSSID("RoamingCompleted");
                    WifiStateMachine.this.notifyWifiRoamingCompleted(WifiStateMachine.this.mLastBssid);
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mConnectedState);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    String bssid = message.obj;
                    String target = "";
                    if (WifiStateMachine.this.mTargetRoamBSSID != null) {
                        target = WifiStateMachine.this.mTargetRoamBSSID;
                    }
                    WifiStateMachine.this.log("NETWORK_DISCONNECTION_EVENT in roaming state BSSID=" + bssid + " target=" + target);
                    if (message.arg2 == 15) {
                        WifiStateMachine.this.handleDualbandHandoverFailed(3);
                    }
                    if (bssid != null && bssid.equals(WifiStateMachine.this.mTargetRoamBSSID)) {
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        break;
                    }
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    StateChangeResult stateChangeResult = message.obj;
                    if (!(stateChangeResult.state == SupplicantState.DISCONNECTED || stateChangeResult.state == SupplicantState.INACTIVE)) {
                        if (stateChangeResult.state == SupplicantState.INTERFACE_DISABLED) {
                        }
                        if (stateChangeResult.state == SupplicantState.ASSOCIATED) {
                            this.mAssociated = true;
                            if (stateChangeResult.BSSID != null) {
                                WifiStateMachine.this.mTargetRoamBSSID = stateChangeResult.BSSID;
                            }
                            WifiStateMachine.this.notifyWifiRoamingStarted();
                            WifiStateMachine.this.setWiFiProRoamingSSID(stateChangeResult.wifiSsid);
                            break;
                        }
                    }
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("STATE_CHANGE_EVENT in roaming state " + stateChangeResult.toString());
                    }
                    if (stateChangeResult.BSSID != null && stateChangeResult.BSSID.equals(WifiStateMachine.this.mTargetRoamBSSID)) {
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    }
                    if (stateChangeResult.state == SupplicantState.ASSOCIATED) {
                        this.mAssociated = true;
                        if (stateChangeResult.BSSID != null) {
                            WifiStateMachine.this.mTargetRoamBSSID = stateChangeResult.BSSID;
                        }
                        WifiStateMachine.this.notifyWifiRoamingStarted();
                        WifiStateMachine.this.setWiFiProRoamingSSID(stateChangeResult.wifiSsid);
                    }
                    break;
                case WifiMonitor.SSID_TEMP_DISABLED /*147469*/:
                    WifiStateMachine.this.logd("SSID_TEMP_DISABLED nid=" + Integer.toString(WifiStateMachine.this.mLastNetworkId) + " id=" + Integer.toString(message.arg1) + " isRoaming=" + WifiStateMachine.this.isRoaming() + " roam=" + WifiStateMachine.this.mAutoRoaming);
                    if (message.arg1 == WifiStateMachine.this.mLastNetworkId) {
                        config = WifiStateMachine.this.getCurrentWifiConfiguration();
                        if (config != null) {
                            WifiStateMachine.this.mWifiLogger.captureBugReportData(3);
                            WifiStateMachine.this.mWifiConfigManager.noteRoamingFailure(config, WifiConfiguration.ROAMING_FAILURE_AUTH_FAILURE);
                        }
                        WifiStateMachine.this.handleNetworkDisconnect();
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectingState);
                    }
                    return false;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            WifiStateMachine.this.logd("WifiStateMachine: Leaving Roaming state");
        }
    }

    class ScanModeState extends State {
        private int mLastOperationMode;

        ScanModeState() {
        }

        public void enter() {
            this.mLastOperationMode = WifiStateMachine.this.mOperationalMode;
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_GET_CONFIGURED_NETWORKS /*131131*/:
                    WifiStateMachine.this.getConfiguredNetworks(message);
                    break;
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.handleScanRequest(message);
                    break;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    if (message.arg1 == 1) {
                        if (this.mLastOperationMode == 3) {
                            WifiStateMachine.this.setWifiState(3);
                            WifiStateMachine.this.mWifiConfigManager.loadAndEnableAllNetworks();
                            if (WifiStateMachineHisiExt.hisiWifiEnabled()) {
                                WifiStateMachine.this.mWifiStateMachineHisiExt.startWifiForP2pCheck();
                            }
                        } else {
                            WifiStateMachine.this.mWifiConfigManager.enableAllNetworks();
                        }
                        WifiStateMachine.this.mWifiConfigManager.setAndEnableLastSelectedConfiguration(-1);
                        WifiStateMachine.this.mOperationalMode = 1;
                        if (WifiStateMachine.this.mP2pSupported) {
                            WifiStateMachine.this.mWifiP2pChannel.sendMessage(WifiStateMachine.CMD_ENABLE_P2P);
                        }
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                        break;
                    } else if (WifiStateMachine.this.processScanModeSetMode(message, this.mLastOperationMode)) {
                        Log.d("HwWifiStateMachine", "ScanModeState process CMD_SET_OPERATIONAL_MODE");
                        break;
                    } else if (message.arg1 == 3) {
                        if (this.mLastOperationMode == 2) {
                            if (WifiStateMachine.this.mWifiP2pChannel != null) {
                                WifiStateMachine.this.mWifiP2pChannel.sendMessage(WifiStateMachine.CMD_DISABLE_P2P_REQ);
                            }
                            WifiStateMachine.this.mOperationalMode = 3;
                            this.mLastOperationMode = 3;
                            WifiStateMachine.this.setWifiState(1);
                            break;
                        }
                    } else if (message.arg1 == 2) {
                        WifiStateMachine.this.mWifiConfigManager.loadConfiguredNetworks();
                        WifiStateMachine.this.mOperationalMode = 2;
                        this.mLastOperationMode = 2;
                        WifiStateMachine.this.setWifiState(3);
                        break;
                    } else {
                        return true;
                    }
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    SupplicantState state = WifiStateMachine.this.handleSupplicantStateChange(message);
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("SupplicantState= " + state);
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    public static class SimAuthRequestData {
        String[] data;
        int networkId;
        int protocol;
        String ssid;
    }

    class SoftApState extends State {
        private SoftApManager mSoftApManager;

        private class SoftApListener implements Listener {
            private SoftApListener() {
            }

            public void onStateChanged(int state, int reason) {
                if (state == 11) {
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_AP_STOPPED);
                } else if (state == 14) {
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_START_AP_FAILURE);
                } else if (state == 13) {
                    WifiStateMachine.this.handleSetWifiApConfigurationHw();
                }
                WifiStateMachine.this.setWifiApState(state, reason);
            }
        }

        SoftApState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.log(getName());
            }
            Message message = WifiStateMachine.this.getCurrentMessage();
            if (message.what == WifiStateMachine.CMD_START_AP) {
                WifiConfiguration config = message.obj;
                if (config == null) {
                    config = WifiStateMachine.this.mWifiApConfigStore.getApConfiguration();
                } else {
                    WifiStateMachine.this.mWifiApConfigStore.setApConfiguration(config);
                }
                WifiStateMachine.this.checkAndSetConnectivityInstance();
                this.mSoftApManager = WifiStateMachine.this.mFacade.makeSoftApManager(WifiStateMachine.this.mContext, WifiStateMachine.this.getHandler().getLooper(), WifiStateMachine.this.mWifiNative, WifiStateMachine.this.mNwService, WifiStateMachine.this.mCm, WifiStateMachine.this.mCountryCode.getCurrentCountryCode(), WifiStateMachine.this.mWifiApConfigStore.getAllowed2GChannel(), new SoftApListener());
                this.mSoftApManager.start(config);
                return;
            }
            throw new RuntimeException("Illegal transition to SoftApState: " + message);
        }

        public void exit() {
            this.mSoftApManager = null;
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_AP /*131093*/:
                    break;
                case WifiStateMachine.CMD_START_AP_FAILURE /*131094*/:
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                    break;
                case WifiStateMachine.CMD_STOP_AP /*131095*/:
                    this.mSoftApManager.stop();
                    break;
                case WifiStateMachine.CMD_AP_STOPPED /*131096*/:
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                    break;
                case WifiStateMachine.CMD_AP_STARTED_GET_STA_LIST /*131104*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Get Soft AP current connected stations list");
                    }
                    Object mStaList = null;
                    if (13 == WifiStateMachine.this.mWifiApState.get()) {
                        mStaList = WifiStateMachine.this.handleGetApLinkedStaList();
                    }
                    WifiStateMachine.this.mReplyChannel.replyToMessage(message, message.what, mStaList);
                    break;
                case WifiStateMachine.CMD_AP_STARTED_SET_MAC_FILTER /*131105*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Set Soft AP MAC filter rule");
                    }
                    String macFilter = message.obj;
                    if (13 == WifiStateMachine.this.mWifiApState.get()) {
                        WifiStateMachine.this.handleSetSoftapMacFilter(macFilter);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_AP_STARTED_SET_DISASSOCIATE_STA /*131106*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Set Soft AP disassociate a sta");
                    }
                    String mac = message.obj;
                    if (13 == WifiStateMachine.this.mWifiApState.get()) {
                        WifiStateMachine.this.handleSetSoftapDisassociateSta(mac);
                        break;
                    }
                    break;
                case WifiStateMachine.CMD_GET_CHANNEL_LIST_5G /*131572*/:
                    Object obj = null;
                    if (13 == WifiStateMachine.this.mWifiApState.get()) {
                        obj = WifiStateMachine.this.mWifiNative.getChannelsForBand(2);
                        if (obj != null && obj.length > 0) {
                            for (int i = 0; i < obj.length; i++) {
                                obj[i] = WifiStateMachine.this.convertFrequencyToChannelNumber(obj[i]);
                            }
                        }
                    }
                    WifiStateMachine.this.replyToMessage(message, message.what, obj);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class SupplicantStartedState extends State {
        SupplicantStartedState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.log(getName() + "\n");
            }
            WifiStateMachine.this.mNetworkInfo.setIsAvailable(true);
            if (WifiStateMachine.this.mNetworkAgent != null) {
                WifiStateMachine.this.mNetworkAgent.sendNetworkInfo(WifiStateMachine.this.mNetworkInfo);
            }
            WifiStateMachine.this.mSupplicantScanIntervalMs = WifiStateMachine.this.mFacade.getLongSetting(WifiStateMachine.this.mContext, "wifi_supplicant_scan_interval_ms", (long) WifiStateMachine.this.mContext.getResources().getInteger(17694766));
            WifiStateMachine.this.mWifiNative.setScanInterval(((int) WifiStateMachine.this.mSupplicantScanIntervalMs) / 1000);
            WifiStateMachine.this.mWifiNative.setExternalSim(true);
            WifiStateMachine.this.mWifiNative.setDfsFlag(true);
            WifiStateMachine.this.setRandomMacOui();
            WifiStateMachine.this.mWifiNative.enableAutoConnect(false);
            WifiStateMachine.this.mWifiConfigManager.setSupportWapiType();
            WifiStateMachine.this.mCountryCode.setCountryCode(WifiStateMachine.this.mCountryCode.getCurrentCountryCode(), true);
            WifiStateMachine.this.mCountryCode.setReadyForChange(true);
        }

        public boolean processMessage(Message message) {
            int i = -1;
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_STOP_SUPPLICANT /*131084*/:
                    if (WifiStateMachine.this.mWifiStatStore != null) {
                        WifiStateMachine.this.mWifiStatStore.updateWifiTriggerState(false);
                    }
                    if (!WifiStateMachine.this.mP2pSupported) {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mSupplicantStoppingState);
                        break;
                    }
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mWaitForP2pDisableState);
                    break;
                case WifiStateMachine.CMD_START_AP /*131093*/:
                    WifiStateMachine.this.loge("Failed to start soft AP with a running supplicant");
                    WifiStateMachine.this.setWifiApState(14, 0);
                    break;
                case WifiStateMachine.CMD_PING_SUPPLICANT /*131123*/:
                    boolean ok = WifiStateMachine.this.mWifiNative.ping();
                    WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                    int i2 = message.what;
                    if (ok) {
                        i = 1;
                    }
                    wifiStateMachine.replyToMessage(message, i2, i);
                    break;
                case WifiStateMachine.CMD_GET_CAPABILITY_FREQ /*131132*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.mWifiNative.getFreqCapability());
                    break;
                case WifiStateMachine.CMD_GET_LINK_LAYER_STATS /*131135*/:
                    WifiStateMachine.this.replyToMessage(message, message.what, (Object) WifiStateMachine.this.getWifiLinkLayerStats(WifiStateMachine.DBG));
                    break;
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    WifiStateMachine.this.mOperationalMode = message.arg1;
                    WifiStateMachine.this.mWifiConfigManager.setAndEnableLastSelectedConfiguration(-1);
                    break;
                case WifiStateMachine.CMD_RESET_SIM_NETWORKS /*131173*/:
                    WifiStateMachine.this.log("resetting EAP-SIM/AKA/AKA' networks since SIM was removed");
                    WifiStateMachine.this.mWifiConfigManager.resetSimNetworks();
                    break;
                case WifiStateMachine.CMD_TARGET_BSSID /*131213*/:
                    if (message.obj != null) {
                        WifiStateMachine.this.mTargetRoamBSSID = (String) message.obj;
                        break;
                    }
                    break;
                case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                    WifiStateMachine.this.loge("Connection lost, restart supplicant");
                    WifiStateMachine.this.handleSupplicantConnectionLoss(true);
                    WifiStateMachine.this.handleNetworkDisconnect();
                    WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiStateMachine.CMD_RESET_SUPPLICANT_STATE);
                    if (WifiStateMachine.this.mP2pSupported) {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mWaitForP2pDisableState);
                    } else {
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                    }
                    WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.CMD_START_SUPPLICANT, 5000);
                    break;
                case WifiMonitor.SCAN_RESULTS_EVENT /*147461*/:
                case WifiMonitor.SCAN_FAILED_EVENT /*147473*/:
                    WifiStateMachine.this.maybeRegisterNetworkFactory();
                    WifiStateMachine.this.setScanResults();
                    if (WifiStateMachine.this.isWlanSettingsActivity()) {
                        WifiStateMachine.this.mSendScanResultsBroadcast = true;
                    }
                    if (WifiStateMachine.this.mIsFullScanOngoing || WifiStateMachine.this.mSendScanResultsBroadcast || WifiStateMachine.this.isWifiProEvaluatingAP()) {
                        boolean scanSucceeded = message.what == WifiMonitor.SCAN_RESULTS_EVENT;
                        if (!(WifiStateMachine.this.mIsFullScanOngoing || WifiStateMachine.this.mSendScanResultsBroadcast)) {
                            WifiStateMachine.this.logd("******send sendScanResultsAvailableBroadcast*****");
                        }
                        if (scanSucceeded && WifiStateMachine.this.mIsFullScanOngoing) {
                            WifiStateMachine.this.updateLastScanRequestTimestamp();
                        }
                        WifiStateMachine.this.sendScanResultsAvailableBroadcast(scanSucceeded);
                        WifiStateMachine.this.sendHilinkscanResultBroadcast();
                    } else {
                        WifiStateMachine.this.notifyWifiScanResultsAvailable(message.what == WifiMonitor.SCAN_RESULTS_EVENT);
                    }
                    WifiStateMachine.this.mSendScanResultsBroadcast = false;
                    WifiStateMachine.this.mIsScanOngoing = false;
                    WifiStateMachine.this.mIsFullScanOngoing = false;
                    if (WifiStateMachine.this.mBufferedScanMsg.size() > 0) {
                        WifiStateMachine.this.sendMessage((Message) WifiStateMachine.this.mBufferedScanMsg.remove());
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            WifiStateMachine.this.mNetworkInfo.setIsAvailable(false);
            if (WifiStateMachine.this.mNetworkAgent != null) {
                WifiStateMachine.this.mNetworkAgent.sendNetworkInfo(WifiStateMachine.this.mNetworkInfo);
            }
            WifiStateMachine.this.mCountryCode.setReadyForChange(false);
        }
    }

    class SupplicantStartingState extends State {
        SupplicantStartingState() {
        }

        private void initializeWpsDetails() {
            String detail = WifiStateMachine.this.mPropertyService.get("ro.product.name", "");
            if (!WifiStateMachine.this.mWifiNative.setDeviceName(detail)) {
                WifiStateMachine.this.loge("Failed to set device name " + detail);
            }
            detail = WifiStateMachine.this.mPropertyService.get("ro.product.manufacturer", "");
            if (!WifiStateMachine.this.mWifiNative.setManufacturer(detail)) {
                WifiStateMachine.this.loge("Failed to set manufacturer " + detail);
            }
            detail = WifiStateMachine.this.mPropertyService.get("ro.product.model", "");
            if (!WifiStateMachine.this.mWifiNative.setModelName(detail)) {
                WifiStateMachine.this.loge("Failed to set model name " + detail);
            }
            detail = WifiStateMachine.this.mPropertyService.get("ro.product.model", "");
            if (!WifiStateMachine.this.mWifiNative.setModelNumber(detail)) {
                WifiStateMachine.this.loge("Failed to set model number " + detail);
            }
            detail = WifiStateMachine.this.mPropertyService.get("ro.serialno", "");
            if (!WifiStateMachine.this.mWifiNative.setSerialNumber(detail)) {
                WifiStateMachine.this.loge("Failed to set serial number " + detail);
            }
            if (!WifiStateMachine.this.mWifiNative.setConfigMethods("physical_display virtual_push_button")) {
                WifiStateMachine.this.loge("Failed to set WPS config methods");
            }
            if (!WifiStateMachine.this.mWifiNative.setDeviceType(WifiStateMachine.this.mPrimaryDeviceType)) {
                WifiStateMachine.this.loge("Failed to set primary device type " + WifiStateMachine.this.mPrimaryDeviceType);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SUPPLICANT /*131083*/:
                case WifiStateMachine.CMD_STOP_SUPPLICANT /*131084*/:
                case WifiStateMachine.CMD_START_DRIVER /*131085*/:
                case WifiStateMachine.CMD_STOP_DRIVER /*131086*/:
                case WifiStateMachine.CMD_START_AP /*131093*/:
                case WifiStateMachine.CMD_STOP_AP /*131095*/:
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                    break;
                case WifiStateMachine.CMD_SET_FREQUENCY_BAND /*131162*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiMonitor.SUP_CONNECTION_EVENT /*147457*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Supplicant connection established");
                    }
                    WifiStateMachine.this.setWifiState(3);
                    WifiStateMachine.this.mSupplicantRestartCount = 0;
                    WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiStateMachine.CMD_RESET_SUPPLICANT_STATE);
                    WifiStateMachine.this.mLastBssid = null;
                    WifiStateMachine.this.mLastNetworkId = -1;
                    WifiStateMachine.this.mLastSignalLevel = -1;
                    WifiStateMachine.this.mWifiInfo.setMacAddress(WifiStateMachine.this.mWifiNative.getMacAddress());
                    if (WifiStateMachine.this.mWiFiCHRManager != null) {
                        WifiStateMachine.this.mWiFiCHRManager.updateStaMAC(WifiStateMachine.this.mWifiInfo.getMacAddress());
                    }
                    WifiStateMachine.this.setFrequencyBand();
                    WifiStateMachine.this.mWifiNative.enableSaveConfig();
                    WifiStateMachine.this.loadAndEnableAllNetworksByMode();
                    if (WifiStateMachine.this.mWifiConfigManager.mEnableVerboseLogging.get() > 0) {
                        WifiStateMachine.this.enableVerboseLogging(WifiStateMachine.this.mWifiConfigManager.mEnableVerboseLogging.get());
                    }
                    initializeWpsDetails();
                    if (WifiStateMachineHisiExt.hisiWifiEnabled()) {
                        WifiStateMachine.this.mWifiStateMachineHisiExt.startWifiForP2pCheck();
                    }
                    WifiStateMachine.this.sendSupplicantConnectionChangedBroadcast(true);
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDriverStartedState);
                    break;
                case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                    WifiStateMachine wifiStateMachine = WifiStateMachine.this;
                    if (wifiStateMachine.mSupplicantRestartCount = wifiStateMachine.mSupplicantRestartCount + 1 > 5) {
                        if (WifiStateMachine.this.mWiFiCHRManager != null) {
                            WifiStateMachine.this.mWiFiCHRManager.updateWifiException(80, "CONNECT_SUPPLICANT_FAILED");
                        }
                        WifiStateMachine.this.loge("Failed " + WifiStateMachine.this.mSupplicantRestartCount + " times to start supplicant, unload driver");
                        WifiStateMachine.this.mSupplicantRestartCount = 0;
                        WifiStateMachine.this.setWifiState(4);
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                        break;
                    }
                    WifiStateMachine.this.loge("Failed to setup control channel, restart supplicant");
                    WifiStateMachine.this.mWifiMonitor.killSupplicant(WifiStateMachine.this.mP2pSupported);
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                    WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.CMD_START_SUPPLICANT, 5000);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class SupplicantStoppingState extends State {
        SupplicantStoppingState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.log(getName() + "\n");
            }
            WifiStateMachine.this.handleNetworkDisconnect();
            String suppState = System.getProperty("init.svc.wpa_supplicant");
            if (suppState == null) {
                suppState = "unknown";
            }
            String p2pSuppState = System.getProperty("init.svc.p2p_supplicant");
            if (p2pSuppState == null) {
                p2pSuppState = "unknown";
            }
            WifiStateMachine.this.logd("SupplicantStoppingState: stopSupplicant  init.svc.wpa_supplicant=" + suppState + " init.svc.p2p_supplicant=" + p2pSuppState);
            WifiStateMachine.this.mWifiMonitor.stopSupplicant();
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            WifiStateMachine wifiStateMachine2 = WifiStateMachine.this;
            WifiStateMachine wifiStateMachine3 = WifiStateMachine.this;
            wifiStateMachine.sendMessageDelayed(wifiStateMachine2.obtainMessage(WifiStateMachine.CMD_STOP_SUPPLICANT_FAILED, wifiStateMachine3.mSupplicantStopFailureToken = wifiStateMachine3.mSupplicantStopFailureToken + 1, 0), 5000);
            WifiStateMachine.this.setWifiState(0);
            WifiStateMachine.this.mSupplicantStateTracker.sendMessage(WifiStateMachine.CMD_RESET_SUPPLICANT_STATE);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SUPPLICANT /*131083*/:
                case WifiStateMachine.CMD_STOP_SUPPLICANT /*131084*/:
                case WifiStateMachine.CMD_START_DRIVER /*131085*/:
                case WifiStateMachine.CMD_STOP_DRIVER /*131086*/:
                case WifiStateMachine.CMD_START_AP /*131093*/:
                case WifiStateMachine.CMD_STOP_AP /*131095*/:
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                case WifiStateMachine.CMD_SET_FREQUENCY_BAND /*131162*/:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_STOP_SUPPLICANT_FAILED /*131089*/:
                    if (message.arg1 == WifiStateMachine.this.mSupplicantStopFailureToken) {
                        WifiStateMachine.this.uploader.e(52, "{ACT:0,STATUS:failed,DETAIL:time out when stop supplicant}");
                        WifiStateMachine.this.loge("Timed out on a supplicant stop, kill and proceed");
                        WifiStateMachine.this.handleSupplicantConnectionLoss(true);
                        WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                        break;
                    }
                    break;
                case WifiMonitor.SUP_CONNECTION_EVENT /*147457*/:
                    WifiStateMachine.this.loge("Supplicant connection received while stopping");
                    break;
                case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Supplicant connection lost");
                    }
                    WifiStateMachine.this.handleSupplicantConnectionLoss(false);
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mInitialState);
                    break;
                case WifiMonitor.DRIVER_HUNG_EVENT /*147468*/:
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    private class UntrustedWifiNetworkFactory extends NetworkFactory {
        private int mUntrustedReqCount;

        public UntrustedWifiNetworkFactory(Looper l, Context c, String tag, NetworkCapabilities f) {
            super(l, c, tag, f);
        }

        protected void needNetworkFor(NetworkRequest networkRequest, int score) {
            if (!networkRequest.networkCapabilities.hasCapability(14)) {
                int i = this.mUntrustedReqCount + 1;
                this.mUntrustedReqCount = i;
                if (i == 1 && WifiStateMachine.this.mWifiConnectivityManager != null) {
                    WifiStateMachine.this.mWifiConnectivityManager.setUntrustedConnectionAllowed(true);
                }
            }
        }

        protected void releaseNetworkFor(NetworkRequest networkRequest) {
            if (!networkRequest.networkCapabilities.hasCapability(14)) {
                int i = this.mUntrustedReqCount - 1;
                this.mUntrustedReqCount = i;
                if (i == 0 && WifiStateMachine.this.mWifiConnectivityManager != null) {
                    WifiStateMachine.this.mWifiConnectivityManager.setUntrustedConnectionAllowed(false);
                }
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            pw.println("mUntrustedReqCount " + this.mUntrustedReqCount);
        }
    }

    class WaitForP2pDisableState extends State {
        private State mTransitionToState;

        WaitForP2pDisableState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.log(getName() + "\n");
            }
            switch (WifiStateMachine.this.getCurrentMessage().what) {
                case WifiStateMachine.CMD_STOP_SUPPLICANT /*131084*/:
                    this.mTransitionToState = WifiStateMachine.this.mSupplicantStoppingState;
                    break;
                case WifiStateMachine.CMD_STOP_DRIVER /*131086*/:
                    this.mTransitionToState = WifiStateMachine.this.mDriverStoppingState;
                    break;
                case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                    this.mTransitionToState = WifiStateMachine.this.mInitialState;
                    break;
                default:
                    this.mTransitionToState = WifiStateMachine.this.mDriverStoppingState;
                    break;
            }
            WifiStateMachine.this.mWifiP2pChannel.sendMessage(WifiStateMachine.CMD_DISABLE_P2P_REQ);
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_SUPPLICANT /*131083*/:
                case WifiStateMachine.CMD_STOP_SUPPLICANT /*131084*/:
                case WifiStateMachine.CMD_START_DRIVER /*131085*/:
                case WifiStateMachine.CMD_STOP_DRIVER /*131086*/:
                case WifiStateMachine.CMD_START_AP /*131093*/:
                case WifiStateMachine.CMD_STOP_AP /*131095*/:
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                case WifiStateMachine.CMD_DISCONNECT /*131145*/:
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                case WifiStateMachine.CMD_SET_FREQUENCY_BAND /*131162*/:
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DEFERRED;
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_DISABLE_P2P_RSP /*131205*/:
                    WifiStateMachine.this.transitionTo(this.mTransitionToState);
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    private class WifiNetworkAgent extends HwNetworkAgent {
        public WifiNetworkAgent(Looper l, Context c, String TAG, NetworkInfo ni, NetworkCapabilities nc, LinkProperties lp, int score, NetworkMisc misc) {
            super(l, c, TAG, ni, nc, lp, score, misc);
        }

        protected void unwanted() {
            if (this == WifiStateMachine.this.mNetworkAgent) {
                if (WifiStateMachine.DBG) {
                    log("WifiNetworkAgent -> Wifi unwanted score " + Integer.toString(WifiStateMachine.this.mWifiInfo.score));
                }
                WifiStateMachine.this.unwantedNetwork(0);
            }
        }

        protected void networkStatus(int status, String redirectUrl) {
            if (this == WifiStateMachine.this.mNetworkAgent) {
                if (status == 2) {
                    if (WifiStateMachine.DBG) {
                        log("WifiNetworkAgent -> Wifi networkStatus invalid, score=" + Integer.toString(WifiStateMachine.this.mWifiInfo.score));
                    }
                    WifiStateMachine.this.unwantedNetwork(1);
                } else if (status == 1) {
                    if (WifiStateMachine.DBG) {
                        log("WifiNetworkAgent -> Wifi networkStatus valid, score= " + Integer.toString(WifiStateMachine.this.mWifiInfo.score));
                    }
                    WifiStateMachine.this.doNetworkStatus(status);
                } else if (status == 3) {
                    WifiStateMachine.this.reportPortalNetworkStatus();
                } else if (status == 4) {
                    WifiStateMachine.this.notifyWifiConnectedBackgroundReady();
                }
            }
        }

        protected void saveAcceptUnvalidated(boolean accept) {
            if (this == WifiStateMachine.this.mNetworkAgent) {
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_ACCEPT_UNVALIDATED, accept ? 1 : 0);
            }
        }

        protected void startPacketKeepalive(Message msg) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_START_IP_PACKET_OFFLOAD, msg.arg1, msg.arg2, msg.obj);
        }

        protected void stopPacketKeepalive(Message msg) {
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_STOP_IP_PACKET_OFFLOAD, msg.arg1, msg.arg2, msg.obj);
        }

        protected void setSignalStrengthThresholds(int[] thresholds) {
            log("Received signal strength thresholds: " + Arrays.toString(thresholds));
            if (thresholds.length == 0) {
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_STOP_RSSI_MONITORING_OFFLOAD, WifiStateMachine.this.mWifiInfo.getRssi());
                return;
            }
            int[] rssiVals = Arrays.copyOf(thresholds, thresholds.length + 2);
            rssiVals[rssiVals.length - 2] = WifiNetworkScoreCache.INVALID_NETWORK_SCORE;
            rssiVals[rssiVals.length - 1] = 127;
            Arrays.sort(rssiVals);
            byte[] rssiRange = new byte[rssiVals.length];
            for (int i = 0; i < rssiVals.length; i++) {
                int val = rssiVals[i];
                if (val > 127 || val < WifiNetworkScoreCache.INVALID_NETWORK_SCORE) {
                    Log.e(WifiStateMachine.TAG, "Illegal value " + val + " for RSSI thresholds: " + Arrays.toString(rssiVals));
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_STOP_RSSI_MONITORING_OFFLOAD, WifiStateMachine.this.mWifiInfo.getRssi());
                    return;
                }
                rssiRange[i] = (byte) val;
            }
            WifiStateMachine.this.mRssiRanges = rssiRange;
            WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_START_RSSI_MONITORING_OFFLOAD, WifiStateMachine.this.mWifiInfo.getRssi());
        }

        protected void preventAutomaticReconnect() {
            if (this == WifiStateMachine.this.mNetworkAgent) {
                WifiStateMachine.this.unwantedNetwork(2);
            }
        }
    }

    private class WifiNetworkFactory extends NetworkFactory {
        public WifiNetworkFactory(Looper l, Context c, String TAG, NetworkCapabilities f) {
            super(l, c, TAG, f);
        }

        protected void needNetworkFor(NetworkRequest networkRequest, int score) {
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            wifiStateMachine.mConnectionRequests = wifiStateMachine.mConnectionRequests + 1;
        }

        protected void releaseNetworkFor(NetworkRequest networkRequest) {
            WifiStateMachine wifiStateMachine = WifiStateMachine.this;
            wifiStateMachine.mConnectionRequests = wifiStateMachine.mConnectionRequests - 1;
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            pw.println("mConnectionRequests " + WifiStateMachine.this.mConnectionRequests);
        }
    }

    class WpsRunningState extends State {
        private Message mSourceMessage;

        WpsRunningState() {
        }

        public void enter() {
            if (WifiStateMachine.DBG) {
                WifiStateMachine.this.log(getName());
            }
            this.mSourceMessage = Message.obtain(WifiStateMachine.this.getCurrentMessage());
        }

        public boolean processMessage(Message message) {
            WifiStateMachine.this.logStateAndMessage(message, this);
            switch (message.what) {
                case WifiStateMachine.CMD_START_DRIVER /*131085*/:
                case WifiStateMachine.CMD_STOP_DRIVER /*131086*/:
                case WifiStateMachine.CMD_ENABLE_NETWORK /*131126*/:
                case WifiStateMachine.CMD_ENABLE_ALL_NETWORKS /*131127*/:
                case WifiStateMachine.CMD_SET_OPERATIONAL_MODE /*131144*/:
                case WifiStateMachine.CMD_RECONNECT /*131146*/:
                case WifiStateMachine.CMD_REASSOCIATE /*131147*/:
                case 151553:
                    WifiStateMachine.this.deferMessage(message);
                    break;
                case WifiStateMachine.CMD_START_SCAN /*131143*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    return true;
                case WifiStateMachine.CMD_AUTO_CONNECT /*131215*/:
                case WifiStateMachine.CMD_AUTO_ROAM /*131217*/:
                    WifiStateMachine.this.messageHandlingStatus = WifiStateMachine.MESSAGE_HANDLING_STATUS_DISCARD;
                    return true;
                case WifiStateMachine.CMD_WPS_PIN_RETRY /*131576*/:
                    WpsResult wpsResult = message.obj;
                    if (!TextUtils.isEmpty(wpsResult.pin)) {
                        WifiStateMachine.this.mWifiNative.startWpsPinKeypad(wpsResult.pin);
                        WifiStateMachine.this.sendMessageDelayed(WifiStateMachine.CMD_WPS_PIN_RETRY, wpsResult, 50000);
                        break;
                    }
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                    WifiStateMachine.this.removeMessages(WifiStateMachine.CMD_WPS_PIN_RETRY);
                    WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151565);
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    WifiStateMachine.this.deferMessage(message);
                    WifiStateMachine.this.saveWpsNetIdInWifiPro(message.arg1);
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Network connection lost");
                    }
                    WifiStateMachine.this.handleNetworkDisconnect();
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    return false;
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Ignore auth failure during WPS connection");
                        break;
                    }
                    break;
                case WifiMonitor.WPS_SUCCESS_EVENT /*147464*/:
                    break;
                case WifiMonitor.WPS_FAIL_EVENT /*147465*/:
                    if (message.arg1 == 0 && message.arg2 == 0) {
                        if (WifiStateMachine.DBG) {
                            WifiStateMachine.this.log("Ignore unspecified fail event during WPS connection");
                            break;
                        }
                    }
                    WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151564, message.arg1);
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                    break;
                case WifiMonitor.WPS_OVERLAP_EVENT /*147466*/:
                    WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151564, 3);
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                case WifiMonitor.WPS_TIMEOUT_EVENT /*147467*/:
                    WifiStateMachine.this.removeMessages(WifiStateMachine.CMD_WPS_PIN_RETRY);
                    WifiStateMachine.this.replyToMessage(this.mSourceMessage, 151564, 7);
                    this.mSourceMessage.recycle();
                    this.mSourceMessage = null;
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                    if (WifiStateMachine.DBG) {
                        WifiStateMachine.this.log("Ignore Assoc reject event during WPS Connection");
                        break;
                    }
                    break;
                case 151562:
                    WifiStateMachine.this.replyToMessage(message, 151564, 1);
                    break;
                case 151566:
                    WifiStateMachine.this.removeMessages(WifiStateMachine.CMD_WPS_PIN_RETRY);
                    if (WifiStateMachine.this.mWifiNative.cancelWps()) {
                        WifiStateMachine.this.replyToMessage(message, 151568);
                    } else {
                        WifiStateMachine.this.replyToMessage(message, 151567, 0);
                    }
                    WifiStateMachine.this.transitionTo(WifiStateMachine.this.mDisconnectedState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            WifiStateMachine.this.mWifiConfigManager.enableAllNetworks();
            WifiStateMachine.this.mWifiConfigManager.loadConfiguredNetworks();
            if (WifiStateMachine.this.mIsRandomMacCleared) {
                WifiStateMachine.this.setRandomMacOui();
                WifiStateMachine.this.mIsRandomMacCleared = false;
            }
        }
    }

    private void setSuspendOptimizations(int r1, boolean r2) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.WifiStateMachine.setSuspendOptimizations(int, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiStateMachine.setSuspendOptimizations(int, boolean):void");
    }

    private void setSuspendOptimizationsNative(int r1, boolean r2) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.WifiStateMachine.setSuspendOptimizationsNative(int, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiStateMachine.setSuspendOptimizationsNative(int, boolean):void");
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    protected void loge(String s) {
        Log.e(getName(), s);
    }

    protected void logd(String s) {
        Log.d(getName(), s);
    }

    protected void log(String s) {
        Log.d(getName(), s);
    }

    public void onRssiThresholdBreached(byte curRssi) {
        if (DBG) {
            Log.e(TAG, "onRssiThresholdBreach event. Cur Rssi = " + curRssi);
        }
        sendMessage(CMD_RSSI_THRESHOLD_BREACH, curRssi);
    }

    public void processRssiThreshold(byte curRssi, int reason) {
        if (curRssi == Byte.MAX_VALUE || curRssi == Byte.MIN_VALUE) {
            Log.wtf(TAG, "processRssiThreshold: Invalid rssi " + curRssi);
            return;
        }
        for (int i = 0; i < this.mRssiRanges.length; i++) {
            if (curRssi < this.mRssiRanges[i]) {
                byte maxRssi = this.mRssiRanges[i];
                byte minRssi = this.mRssiRanges[i - 1];
                this.mWifiInfo.setRssi(curRssi);
                updateCapabilities(getCurrentWifiConfiguration());
                Log.d(TAG, "Re-program RSSI thresholds for " + smToString(reason) + ": [" + minRssi + ", " + maxRssi + "], curRssi=" + curRssi + " ret=" + startRssiMonitoringOffload(maxRssi, minRssi));
                break;
            }
        }
    }

    boolean isRoaming() {
        return this.mAutoRoaming;
    }

    public void autoRoamSetBSSID(int netId, String bssid) {
        autoRoamSetBSSID(this.mWifiConfigManager.getWifiConfiguration(netId), bssid);
    }

    public boolean autoRoamSetBSSID(WifiConfiguration config, String bssid) {
        boolean ret = true;
        if (this.mTargetRoamBSSID == null) {
            this.mTargetRoamBSSID = WifiLastResortWatchdog.BSSID_ANY;
        }
        if (bssid == null) {
            bssid = WifiLastResortWatchdog.BSSID_ANY;
        }
        if (config == null) {
            return false;
        }
        if (this.mTargetRoamBSSID != null && bssid.equals(this.mTargetRoamBSSID) && bssid.equals(config.BSSID)) {
            return false;
        }
        if (!this.mTargetRoamBSSID.equals(WifiLastResortWatchdog.BSSID_ANY) && bssid.equals(WifiLastResortWatchdog.BSSID_ANY)) {
            ret = false;
        }
        if (config.BSSID != null) {
            bssid = config.BSSID;
            if (DBG) {
                Log.d(TAG, "force BSSID to " + bssid + "due to config");
            }
        }
        if (DBG) {
            logd("autoRoamSetBSSID " + bssid + " key=" + config.configKey());
        }
        this.mTargetRoamBSSID = bssid;
        this.mWifiConfigManager.saveWifiConfigBSSID(config, bssid);
        return ret;
    }

    private boolean setTargetBssid(WifiConfiguration config, String bssid) {
        if (config == null) {
            return false;
        }
        if (config.BSSID != null) {
            bssid = config.BSSID;
            if (DBG) {
                Log.d(TAG, "force BSSID to " + bssid + "due to config");
            }
        }
        if (bssid == null) {
            bssid = WifiLastResortWatchdog.BSSID_ANY;
        }
        String networkSelectionBSSID = config.getNetworkSelectionStatus().getNetworkSelectionBSSID();
        if (networkSelectionBSSID == null || !networkSelectionBSSID.equals(bssid)) {
            if (DBG) {
                Log.d(TAG, "target set to " + config.SSID + ":" + bssid);
            }
            this.mTargetRoamBSSID = bssid;
            this.mWifiConfigManager.saveWifiConfigBSSID(config, bssid);
            return true;
        }
        if (DBG) {
            Log.d(TAG, "Current preferred BSSID is the same as the target one");
        }
        return false;
    }

    boolean recordUidIfAuthorized(WifiConfiguration config, int uid, boolean onlyAnnotate) {
        if (!this.mWifiConfigManager.isNetworkConfigured(config)) {
            config.creatorUid = uid;
            config.creatorName = this.mContext.getPackageManager().getNameForUid(uid);
        } else if (!this.mWifiConfigManager.canModifyNetwork(uid, config, onlyAnnotate)) {
            return false;
        }
        config.lastUpdateUid = uid;
        config.lastUpdateName = this.mContext.getPackageManager().getNameForUid(uid);
        return true;
    }

    boolean deferForUserInput(Message message, int netId, boolean allowOverride) {
        WifiConfiguration config = this.mWifiConfigManager.getWifiConfiguration(netId);
        if (config == null) {
            logd("deferForUserInput: configuration for netId=" + netId + " not stored");
            return true;
        }
        switch (config.userApproved) {
            case 1:
            case 2:
                return false;
            default:
                config.userApproved = 1;
                return false;
        }
    }

    public WifiStateMachine(Context context, FrameworkFacade facade, Looper looper, UserManager userManager, WifiInjector wifiInjector, BackupManagerProxy backupManagerProxy, WifiCountryCode countryCode) {
        super(TAG, looper);
        this.mWifiInjector = wifiInjector;
        this.mWifiMetrics = this.mWifiInjector.getWifiMetrics();
        this.mWifiLastResortWatchdog = wifiInjector.getWifiLastResortWatchdog();
        this.mClock = wifiInjector.getClock();
        this.mPropertyService = wifiInjector.getPropertyService();
        this.mBuildProperties = wifiInjector.getBuildProperties();
        this.mContext = context;
        this.mFacade = facade;
        this.mWifiNative = WifiNative.getWlanNativeInterface();
        this.mBackupManagerProxy = backupManagerProxy;
        this.mWifiNative.initContext(this.mContext);
        this.mInterfaceName = this.mWifiNative.getInterfaceName();
        this.mNetworkInfo = new NetworkInfo(1, 0, NETWORKTYPE, "");
        this.mBatteryStats = Stub.asInterface(this.mFacade.getService("batterystats"));
        this.mNwService = INetworkManagementService.Stub.asInterface(this.mFacade.getService("network_management"));
        this.mP2pSupported = this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.direct");
        this.mWifiConfigManager = this.mFacade.makeWifiConfigManager(context, this.mWifiNative, facade, this.mWifiInjector.getClock(), userManager, this.mWifiInjector.getKeyStore());
        this.mWifiMonitor = WifiMonitor.getInstance();
        if (this.mContext.getResources().getBoolean(17956892)) {
            this.mWifiLogger = facade.makeRealLogger(this, this.mWifiNative, this.mBuildProperties);
        } else {
            this.mWifiLogger = facade.makeBaseLogger();
        }
        this.mWifiInfo = new WifiInfo();
        this.mWifiQualifiedNetworkSelector = HwWifiServiceFactory.getHwWifiServiceManager().createHwWifiQualifiedNetworkSelector(this.mWifiConfigManager, this.mContext, this.mWifiInfo, this.mClock, this, this.mWifiNative);
        this.mSupplicantStateTracker = this.mFacade.makeSupplicantStateTracker(context, this.mWifiConfigManager, getHandler());
        this.mLinkProperties = new LinkProperties();
        this.mWifiP2pServiceImpl = (WifiP2pServiceImpl) IWifiP2pManager.Stub.asInterface(this.mFacade.getService("wifip2p"));
        if (WifiStateMachineHisiExt.hisiWifiEnabled()) {
            this.mWifiStateMachineHisiExt = new WifiStateMachineHisiExt(this.mContext, this.mWifiConfigManager, this.mWifiState, this.mWifiApState);
        }
        this.uploader = DataUploader.getInstance();
        this.mNetworkInfo.setIsAvailable(false);
        this.mLastBssid = null;
        this.mLastNetworkId = -1;
        this.mLastSignalLevel = -1;
        this.mIpManager = this.mFacade.makeIpManager(this.mContext, this.mInterfaceName, new IpManagerCallback());
        this.mIpManager.setMulticastFilter(true);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        int period = this.mContext.getResources().getInteger(17694769);
        if (period < WifiConnectivityManager.HW_MIN_PERIODIC_SCAN_INTERVAL_MS) {
            period = WifiConnectivityManager.HW_MIN_PERIODIC_SCAN_INTERVAL_MS;
        }
        this.mDefaultFrameworkScanIntervalMs = period;
        this.mNoNetworksPeriodicScan = this.mContext.getResources().getInteger(17694770);
        this.mBackgroundScanSupported = this.mContext.getResources().getBoolean(17956888);
        this.mPrimaryDeviceType = this.mContext.getResources().getString(17039415);
        this.mCountryCode = countryCode;
        this.mUserWantsSuspendOpt.set(this.mFacade.getIntegerSetting(this.mContext, "wifi_suspend_optimizations_enabled", 1) == 1);
        this.mNetworkCapabilitiesFilter.addTransportType(1);
        this.mNetworkCapabilitiesFilter.addCapability(12);
        this.mNetworkCapabilitiesFilter.addCapability(11);
        this.mNetworkCapabilitiesFilter.addCapability(13);
        this.mNetworkCapabilitiesFilter.setLinkUpstreamBandwidthKbps(WifiLogger.RING_BUFFER_BYTE_LIMIT_LARGE);
        this.mNetworkCapabilitiesFilter.setLinkDownstreamBandwidthKbps(WifiLogger.RING_BUFFER_BYTE_LIMIT_LARGE);
        this.mDfltNetworkCapabilities = new NetworkCapabilities(this.mNetworkCapabilitiesFilter);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (WifiStateMachine.VDBG) {
                    WifiStateMachine.this.log("receive action: " + action);
                }
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_SCREEN_STATE_CHANGED, 1);
                    if (WifiStateMachine.this.mWiFiCHRManager != null) {
                        WifiStateMachine.this.mWiFiCHRManager.updateScreenState(true);
                    }
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_SCREEN_STATE_CHANGED, 0);
                    if (WifiStateMachine.this.mWiFiCHRManager != null) {
                        WifiStateMachine.this.mWiFiCHRManager.updateScreenState(false);
                    }
                }
            }
        }, filter);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_suspend_optimizations_enabled"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                boolean z = true;
                AtomicBoolean -get79 = WifiStateMachine.this.mUserWantsSuspendOpt;
                if (WifiStateMachine.this.mFacade.getIntegerSetting(WifiStateMachine.this.mContext, "wifi_suspend_optimizations_enabled", 1) != 1) {
                    z = false;
                }
                -get79.set(z);
            }
        });
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_scan_always_enabled"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                if (WifiStateMachine.this.mHwWifiDFTUtil != null) {
                    WifiStateMachine.this.mHwWifiDFTUtil.updateWifiScanAlwaysState(WifiStateMachine.this.mContext);
                }
            }
        });
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_sleep_policy"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                if (WifiStateMachine.this.mHwWifiDFTUtil != null) {
                    WifiStateMachine.this.mHwWifiDFTUtil.updateWifiSleepPolicyState(WifiStateMachine.this.mContext);
                }
            }
        });
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_networks_available_notification_on"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                if (WifiStateMachine.this.mHwWifiDFTUtil != null) {
                    WifiStateMachine.this.mHwWifiDFTUtil.updateWifiNetworkNotificationState(WifiStateMachine.this.mContext);
                }
            }
        });
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("wifi_to_pdp"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                if (WifiStateMachine.this.mHwWifiDFTUtil != null) {
                    WifiStateMachine.this.mHwWifiDFTUtil.updateWifiToPdpState(WifiStateMachine.this.mContext);
                }
            }
        });
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("smart_network_switching"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                if (WifiStateMachine.this.mHwWifiDFTUtil != null) {
                    WifiStateMachine.this.mHwWifiDFTUtil.updateWifiProState(WifiStateMachine.this.mContext);
                }
            }
        });
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                WifiStateMachine.this.sendMessage(WifiStateMachine.CMD_BOOT_COMPLETED);
            }
        }, new IntentFilter("android.intent.action.BOOT_COMPLETED"));
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                WifiStateMachine.this.mIsRunning = false;
                if (WifiStateMachine.DBG) {
                    WifiStateMachine.this.log("shut down so update battery");
                }
                WifiStateMachine.this.updateBatteryWorkSource(null);
            }
        }, new IntentFilter("android.intent.action.ACTION_SHUTDOWN"));
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mWakeLock = powerManager.newWakeLock(1, getName());
        this.mSuspendWakeLock = powerManager.newWakeLock(1, "WifiSuspend");
        this.mSuspendWakeLock.setReferenceCounted(false);
        this.mTcpBufferSizes = this.mContext.getResources().getString(17039452);
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mSupplicantStartingState, this.mDefaultState);
        addState(this.mSupplicantStartedState, this.mDefaultState);
        addState(this.mDriverStartingState, this.mSupplicantStartedState);
        addState(this.mDriverStartedState, this.mSupplicantStartedState);
        addState(this.mScanModeState, this.mDriverStartedState);
        addState(this.mConnectModeState, this.mDriverStartedState);
        addState(this.mL2ConnectedState, this.mConnectModeState);
        addState(this.mObtainingIpState, this.mL2ConnectedState);
        addState(this.mConnectedState, this.mL2ConnectedState);
        addState(this.mRoamingState, this.mL2ConnectedState);
        addState(this.mDisconnectingState, this.mConnectModeState);
        addState(this.mDisconnectedState, this.mConnectModeState);
        addState(this.mWpsRunningState, this.mConnectModeState);
        addState(this.mWaitForP2pDisableState, this.mSupplicantStartedState);
        addState(this.mDriverStoppingState, this.mSupplicantStartedState);
        addState(this.mDriverStoppedState, this.mSupplicantStartedState);
        addState(this.mSupplicantStoppingState, this.mDefaultState);
        addState(this.mSoftApState, this.mDefaultState);
        setInitialState(this.mInitialState);
        Intent intent;
        if (ActivityManager.isLowRamDeviceStatic() || SystemProperties.getBoolean("ro.config.hw_low_ram", false)) {
            setLogRecSize(100);
            setLogOnlyTransitions(false);
            start();
            this.mWifiMonitor.registerHandler(this.mInterfaceName, CMD_TARGET_BSSID, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, CMD_ASSOCIATED_BSSID, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.ANQP_DONE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.ASSOCIATION_REJECTION_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.AUTHENTICATION_FAILURE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.DRIVER_HUNG_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.GAS_QUERY_DONE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.GAS_QUERY_START_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.HS20_REMEDIATION_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.NETWORK_CONNECTION_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.NETWORK_DISCONNECTION_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.RX_HS20_ANQP_ICON_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SCAN_FAILED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SCAN_RESULTS_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SSID_REENABLED, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SSID_TEMP_DISABLED, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUP_CONNECTION_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUP_DISCONNECTION_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUP_REQUEST_IDENTITY, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUP_REQUEST_SIM_AUTH, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_FAIL_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_OVERLAP_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_SUCCESS_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_TIMEOUT_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_START_OKC_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.VOWIFI_DETECT_IRQ_STR_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.EVENT_ANT_CORE_ROB, getHandler());
            this.mWiFiCHRManager = HwWifiServiceFactory.getHwWifiCHRStateManager();
            this.mWifiStatStore = HwWifiServiceFactory.getHwWifiStatStore();
            this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
            this.mHwWifiDFTUtil = HwWifiServiceFactory.getHwWifiDFTUtil();
            intent = new Intent("wifi_scan_available");
            intent.addFlags(67108864);
            intent.putExtra("scan_enabled", 1);
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        } else {
            setLogRecSize(100);
            setLogOnlyTransitions(false);
            start();
            this.mWifiMonitor.registerHandler(this.mInterfaceName, CMD_TARGET_BSSID, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, CMD_ASSOCIATED_BSSID, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.ANQP_DONE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.ASSOCIATION_REJECTION_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.AUTHENTICATION_FAILURE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.DRIVER_HUNG_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.GAS_QUERY_DONE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.GAS_QUERY_START_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.HS20_REMEDIATION_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.NETWORK_CONNECTION_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.NETWORK_DISCONNECTION_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.RX_HS20_ANQP_ICON_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SCAN_FAILED_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SCAN_RESULTS_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SSID_REENABLED, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SSID_TEMP_DISABLED, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUP_CONNECTION_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUP_DISCONNECTION_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUP_REQUEST_IDENTITY, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.SUP_REQUEST_SIM_AUTH, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_FAIL_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_OVERLAP_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_SUCCESS_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_TIMEOUT_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.WPS_START_OKC_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.VOWIFI_DETECT_IRQ_STR_EVENT, getHandler());
            this.mWifiMonitor.registerHandler(this.mInterfaceName, WifiMonitor.EVENT_ANT_CORE_ROB, getHandler());
            this.mWiFiCHRManager = HwWifiServiceFactory.getHwWifiCHRStateManager();
            this.mWifiStatStore = HwWifiServiceFactory.getHwWifiStatStore();
            this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
            this.mHwWifiDFTUtil = HwWifiServiceFactory.getHwWifiDFTUtil();
            intent = new Intent("wifi_scan_available");
            intent.addFlags(67108864);
            intent.putExtra("scan_enabled", 1);
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
        try {
            this.mSystemUiUid = this.mContext.getPackageManager().getPackageUidAsUser("com.android.systemui", WifiLogger.RING_BUFFER_BYTE_LIMIT_LARGE, 0);
        } catch (NameNotFoundException e) {
            loge("Unable to resolve SystemUI's UID.");
        }
        this.mVerboseLoggingLevel = this.mFacade.getIntegerSetting(this.mContext, "wifi_verbose_logging_enabled", 0);
        updateLoggingLevel();
        if (this.mWifiStatStore != null) {
            this.mWifiStatStore.readWifiCHRStat();
        }
        HwWifiServiceFactory.getIsmCoexWifiStateTrack(context, this, this.mWifiNative);
    }

    private void stopIpManager() {
        handlePostDhcpSetup();
        this.mIpManager.stop();
    }

    PendingIntent getPrivateBroadcast(String action, int requestCode) {
        Intent intent = new Intent(action, null);
        intent.addFlags(67108864);
        intent.setPackage("android");
        return this.mFacade.getBroadcast(this.mContext, requestCode, intent, 0);
    }

    int getVerboseLoggingLevel() {
        return this.mVerboseLoggingLevel;
    }

    void enableVerboseLogging(int verbose) {
        this.mVerboseLoggingLevel = verbose;
        this.mFacade.setIntegerSetting(this.mContext, "wifi_verbose_logging_enabled", verbose);
        updateLoggingLevel();
    }

    void updateLoggingLevel() {
        if (this.mVerboseLoggingLevel > 0) {
            DBG = true;
            this.mWifiNative.setSupplicantLogLevel("DEBUG");
            setLogRecSize(ActivityManager.isLowRamDeviceStatic() ? 200 : 3000);
            configureVerboseHalLogging(true);
        } else {
            DBG = false;
            this.mWifiNative.setSupplicantLogLevel("INFO");
            setLogRecSize(100);
            configureVerboseHalLogging(false);
        }
        this.mCountryCode.enableVerboseLogging(this.mVerboseLoggingLevel);
        this.mWifiLogger.startLogging(DBG);
        this.mWifiMonitor.enableVerboseLogging(this.mVerboseLoggingLevel);
        this.mWifiNative.enableVerboseLogging(this.mVerboseLoggingLevel);
        this.mWifiConfigManager.enableVerboseLogging(this.mVerboseLoggingLevel);
        this.mSupplicantStateTracker.enableVerboseLogging(this.mVerboseLoggingLevel);
        if (this.mWifiQualifiedNetworkSelector != null) {
            this.mWifiQualifiedNetworkSelector.enableVerboseLogging(this.mVerboseLoggingLevel);
        }
        if (this.mWifiConnectivityManager != null) {
            this.mWifiConnectivityManager.enableVerboseLogging(this.mVerboseLoggingLevel);
        }
    }

    private void configureVerboseHalLogging(boolean enableVerbose) {
        if (!this.mBuildProperties.isUserBuild()) {
            this.mPropertyService.set(SYSTEM_PROPERTY_LOG_CONTROL_WIFIHAL, enableVerbose ? LOGD_LEVEL_VERBOSE : LOGD_LEVEL_DEBUG);
        }
    }

    void updateAssociatedScanPermission() {
    }

    int getAggressiveHandover() {
        return this.mAggressiveHandover;
    }

    void enableAggressiveHandover(int enabled) {
        this.mAggressiveHandover = enabled;
    }

    public void clearANQPCache() {
        this.mWifiConfigManager.trimANQPCache(true);
    }

    public void setAllowScansWithTraffic(int enabled) {
        this.mWifiConfigManager.mAlwaysEnableScansWhileAssociated.set(enabled);
    }

    public int getAllowScansWithTraffic() {
        return this.mWifiConfigManager.mAlwaysEnableScansWhileAssociated.get();
    }

    public boolean setEnableAutoJoinWhenAssociated(boolean enabled) {
        sendMessage(CMD_ENABLE_AUTOJOIN_WHEN_ASSOCIATED, enabled ? 1 : 0);
        return true;
    }

    public boolean getEnableAutoJoinWhenAssociated() {
        return this.mWifiConfigManager.getEnableAutoJoinWhenAssociated();
    }

    public boolean setRandomMacOui() {
        String oui = this.mContext.getResources().getString(17039416);
        if (TextUtils.isEmpty(oui)) {
            oui = GOOGLE_OUI;
        }
        String[] ouiParts = oui.split("-");
        byte[] ouiBytes = new byte[]{(byte) (Integer.parseInt(ouiParts[0], 16) & 255), (byte) (Integer.parseInt(ouiParts[1], 16) & 255), (byte) (Integer.parseInt(ouiParts[2], 16) & 255)};
        logd("Setting OUI to " + oui);
        return this.mWifiNative.setScanningMacOui(ouiBytes);
    }

    public boolean clearRandomMacOui() {
        byte[] ouiBytes = new byte[]{(byte) 0, (byte) 0, (byte) 0};
        logd("Clear random OUI");
        return this.mWifiNative.setScanningMacOui(ouiBytes);
    }

    public Messenger getMessenger() {
        return new Messenger(getHandler());
    }

    public boolean syncPingSupplicant(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_PING_SUPPLICANT);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public void startScan(int callingUid, int scanCounter, ScanSettings settings, WorkSource workSource) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(CUSTOMIZED_SCAN_SETTING, settings);
        bundle.putParcelable(CUSTOMIZED_SCAN_WORKSOURCE, workSource);
        bundle.putLong(SCAN_REQUEST_TIME, System.currentTimeMillis());
        sendMessage(CMD_START_SCAN, callingUid, scanCounter, bundle);
    }

    public long getDisconnectedTimeMilli() {
        if (getCurrentState() != this.mDisconnectedState || this.mDisconnectedTimeStamp == 0) {
            return 0;
        }
        return System.currentTimeMillis() - this.mDisconnectedTimeStamp;
    }

    private boolean checkOrDeferScanAllowed(Message msg) {
        long now = System.currentTimeMillis();
        if (this.lastConnectAttemptTimestamp == 0 || now - this.lastConnectAttemptTimestamp >= 10000) {
            return true;
        }
        if (now - this.lastConnectAttemptTimestamp < 0) {
            logd("checkOrDeferScanAllowed time is jump!!!");
            this.lastConnectAttemptTimestamp = now;
        }
        sendMessageDelayed(Message.obtain(msg), 11000 - (now - this.lastConnectAttemptTimestamp));
        return false;
    }

    String reportOnTime() {
        long now = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        int on = this.mOnTime - this.mOnTimeLastReport;
        this.mOnTimeLastReport = this.mOnTime;
        int tx = this.mTxTime - this.mTxTimeLastReport;
        this.mTxTimeLastReport = this.mTxTime;
        int rx = this.mRxTime - this.mRxTimeLastReport;
        this.mRxTimeLastReport = this.mRxTime;
        int period = (int) (now - this.lastOntimeReportTimeStamp);
        this.lastOntimeReportTimeStamp = now;
        sb.append(String.format("[on:%d tx:%d rx:%d period:%d]", new Object[]{Integer.valueOf(on), Integer.valueOf(tx), Integer.valueOf(rx), Integer.valueOf(period)}));
        on = this.mOnTime - this.mOnTimeScreenStateChange;
        period = (int) (now - this.lastScreenStateChangeTimeStamp);
        sb.append(String.format(" from screen [on:%d period:%d]", new Object[]{Integer.valueOf(on), Integer.valueOf(period)}));
        return sb.toString();
    }

    WifiLinkLayerStats getWifiLinkLayerStats(boolean dbg) {
        WifiLinkLayerStats wifiLinkLayerStats = null;
        if (this.mWifiLinkLayerStatsSupported > 0) {
            String name = "wlan0";
            wifiLinkLayerStats = this.mWifiNative.getWifiLinkLayerStats(name);
            if (name != null && wifiLinkLayerStats == null && this.mWifiLinkLayerStatsSupported > 0) {
                this.mWifiLinkLayerStatsSupported--;
            } else if (wifiLinkLayerStats != null) {
                this.lastLinkLayerStatsUpdate = System.currentTimeMillis();
                this.mOnTime = wifiLinkLayerStats.on_time;
                this.mTxTime = wifiLinkLayerStats.tx_time;
                this.mRxTime = wifiLinkLayerStats.rx_time;
                this.mRunningBeaconCount = wifiLinkLayerStats.beacon_rx;
            }
        }
        if (wifiLinkLayerStats == null || this.mWifiLinkLayerStatsSupported <= 0) {
            this.mWifiInfo.updatePacketRates(this.mFacade.getTxPackets(this.mInterfaceName), this.mFacade.getRxPackets(this.mInterfaceName));
        } else {
            this.mWifiInfo.updatePacketRates(wifiLinkLayerStats);
        }
        return wifiLinkLayerStats;
    }

    int startWifiIPPacketOffload(int slot, KeepalivePacketData packetData, int intervalSeconds) {
        int ret = this.mWifiNative.startSendingOffloadedPacket(slot, packetData, intervalSeconds * 1000);
        if (ret == 0) {
            return 0;
        }
        loge("startWifiIPPacketOffload(" + slot + ", " + intervalSeconds + "): hardware error " + ret);
        return -31;
    }

    int stopWifiIPPacketOffload(int slot) {
        int ret = this.mWifiNative.stopSendingOffloadedPacket(slot);
        if (ret == 0) {
            return 0;
        }
        loge("stopWifiIPPacketOffload(" + slot + "): hardware error " + ret);
        return -31;
    }

    int startRssiMonitoringOffload(byte maxRssi, byte minRssi) {
        return this.mWifiNative.startRssiMonitoring(maxRssi, minRssi, this);
    }

    int stopRssiMonitoringOffload() {
        return this.mWifiNative.stopRssiMonitoring();
    }

    private void handleScanRequest(Message message) {
        ScanSettings scanSettings = null;
        Parcelable parcelable = null;
        Bundle bundle = message.obj;
        if (bundle != null) {
            scanSettings = (ScanSettings) bundle.getParcelable(CUSTOMIZED_SCAN_SETTING);
            parcelable = (WorkSource) bundle.getParcelable(CUSTOMIZED_SCAN_WORKSOURCE);
        }
        Set set = null;
        if (!(scanSettings == null || scanSettings.channelSet == null)) {
            set = new HashSet();
            for (WifiChannel channel : scanSettings.channelSet) {
                set.add(Integer.valueOf(channel.freqMHz));
            }
        }
        if (startScanNative(set, this.mWifiConfigManager.getHiddenConfiguredNetworkIds(), parcelable)) {
            if (set == null) {
                this.mBufferedScanMsg.clear();
            }
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_OK;
            if (parcelable != null) {
                this.mSendScanResultsBroadcast = true;
            }
            return;
        }
        if (!this.mIsScanOngoing) {
            if (this.mBufferedScanMsg.size() > 0) {
                sendMessage((Message) this.mBufferedScanMsg.remove());
            }
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
        } else if (this.mIsFullScanOngoing) {
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
        } else {
            if (set == null) {
                this.mBufferedScanMsg.clear();
            }
            if (this.mBufferedScanMsg.size() < 10) {
                this.mBufferedScanMsg.add(obtainMessage(CMD_START_SCAN, message.arg1, message.arg2, bundle));
            } else {
                bundle = new Bundle();
                bundle.putParcelable(CUSTOMIZED_SCAN_SETTING, null);
                bundle.putParcelable(CUSTOMIZED_SCAN_WORKSOURCE, parcelable);
                Message msg = obtainMessage(CMD_START_SCAN, message.arg1, message.arg2, bundle);
                this.mBufferedScanMsg.clear();
                this.mBufferedScanMsg.add(msg);
            }
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_LOOPED;
        }
    }

    private boolean startScanNative(Set<Integer> freqs, Set<Integer> hiddenNetworkIds, WorkSource workSource) {
        WifiScanner.ScanSettings settings = new WifiScanner.ScanSettings();
        if (freqs == null) {
            settings.band = 7;
        } else {
            settings.band = 0;
            int index = 0;
            settings.channels = new ChannelSpec[freqs.size()];
            for (Integer freq : freqs) {
                int index2 = index + 1;
                settings.channels[index] = new ChannelSpec(freq.intValue());
                index = index2;
            }
        }
        settings.reportEvents = 3;
        if (hiddenNetworkIds != null && hiddenNetworkIds.size() > 0) {
            int i = 0;
            settings.hiddenNetworkIds = new int[hiddenNetworkIds.size()];
            for (Integer netId : hiddenNetworkIds) {
                int i2 = i + 1;
                settings.hiddenNetworkIds[i] = netId.intValue();
                i = i2;
            }
        }
        this.mWifiScanner.startScan(settings, new ScanListener() {
            private List<ScanDetail> mScanDetails = new ArrayList();

            private void clearScanDetails() {
                this.mScanDetails.clear();
            }

            public void onSuccess() {
            }

            public void onFailure(int reason, String description) {
                WifiStateMachine.this.mIsScanOngoing = false;
                WifiStateMachine.this.mIsFullScanOngoing = false;
            }

            public void onResults(ScanData[] results) {
                if (WifiStateMachine.this.mWifiConnectivityManager != null) {
                    Log.d(WifiStateMachine.TAG, "onResults implement nativeScanListener");
                    WifiStateMachine.this.mWifiConnectivityManager.handleScanResults(this.mScanDetails, "nativeScanListener");
                } else {
                    Log.d(WifiStateMachine.TAG, "onResults mWifiConnectivityManager is null");
                }
                clearScanDetails();
            }

            public void onFullResult(ScanResult fullScanResult) {
                this.mScanDetails.add(ScanDetailUtil.toScanDetail(fullScanResult));
            }

            public void onPeriodChanged(int periodInMs) {
            }
        }, workSource);
        this.mIsScanOngoing = true;
        this.mIsFullScanOngoing = freqs == null;
        this.lastScanFreqs = freqs;
        return true;
    }

    public void setSupplicantRunning(boolean enable) {
        if (enable) {
            sendMessage(CMD_START_SUPPLICANT);
        } else {
            sendMessage(CMD_STOP_SUPPLICANT);
        }
    }

    public void setHostApRunning(WifiConfiguration wifiConfig, boolean enable) {
        if (enable) {
            sendMessage(CMD_START_AP, wifiConfig);
        } else {
            sendMessage(CMD_STOP_AP);
        }
    }

    public void setWifiApConfiguration(WifiConfiguration config) {
        this.mWifiApConfigStore.setApConfiguration(config);
    }

    public WifiConfiguration syncGetWifiApConfiguration() {
        return this.mWifiApConfigStore.getApConfiguration();
    }

    public int syncGetWifiState() {
        return this.mWifiState.get();
    }

    public String syncGetWifiStateByName() {
        switch (this.mWifiState.get()) {
            case 0:
                return "disabling";
            case 1:
                return "disabled";
            case 2:
                return "enabling";
            case 3:
                return "enabled";
            case 4:
                return "unknown state";
            default:
                return "[invalid state]";
        }
    }

    public int syncGetWifiApState() {
        return this.mWifiApState.get();
    }

    public String syncGetWifiApStateByName() {
        switch (this.mWifiApState.get()) {
            case 10:
                return "disabling";
            case 11:
                return "disabled";
            case 12:
                return "enabling";
            case 13:
                return "enabled";
            case 14:
                return "failed";
            default:
                return "[invalid state]";
        }
    }

    public boolean isConnected() {
        return getCurrentState() == this.mConnectedState;
    }

    public boolean isDisconnected() {
        return getCurrentState() == this.mDisconnectedState;
    }

    public boolean isSupplicantTransientState() {
        SupplicantState SupplicantState = this.mWifiInfo.getSupplicantState();
        if (SupplicantState == SupplicantState.ASSOCIATING || SupplicantState == SupplicantState.AUTHENTICATING || SupplicantState == SupplicantState.FOUR_WAY_HANDSHAKE || SupplicantState == SupplicantState.GROUP_HANDSHAKE) {
            if (DBG) {
                Log.d(TAG, "Supplicant is under transient state: " + SupplicantState);
            }
            return true;
        }
        if (DBG) {
            Log.d(TAG, "Supplicant is under steady state: " + SupplicantState);
        }
        return false;
    }

    public boolean isLinkDebouncing() {
        return this.linkDebouncing;
    }

    public WifiInfo syncRequestConnectionInfo() {
        return getWiFiInfoForUid(Binder.getCallingUid());
    }

    public WifiInfo getWifiInfo() {
        return this.mWifiInfo;
    }

    public DhcpResults syncGetDhcpResults() {
        DhcpResults dhcpResults;
        synchronized (this.mDhcpResultsLock) {
            dhcpResults = new DhcpResults(this.mDhcpResults);
        }
        return dhcpResults;
    }

    public void setDriverStart(boolean enable) {
        if (enable) {
            sendMessage(CMD_START_DRIVER);
        } else {
            sendMessage(CMD_STOP_DRIVER);
        }
    }

    public void setOperationalMode(int mode) {
        if (DBG) {
            log("setting operational mode to " + String.valueOf(mode));
        }
        sendMessage(CMD_SET_OPERATIONAL_MODE, mode, 0);
    }

    public List<ScanResult> syncGetScanResultsList() {
        List<ScanResult> scanList;
        synchronized (this.mScanResultsLock) {
            scanList = new ArrayList();
            for (ScanDetail result : this.mScanResults) {
                scanList.add(new ScanResult(result.getScanResult()));
            }
            if (scanList.size() == 0) {
                Log.w(TAG, "Can't find ssid.");
            }
        }
        return scanList;
    }

    public int syncAddPasspointManagementObject(AsyncChannel channel, String managementObject) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_ADD_PASSPOINT_MO, managementObject);
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result;
    }

    public int syncModifyPasspointManagementObject(AsyncChannel channel, String fqdn, List<PasspointManagementObjectDefinition> managementObjectDefinitions) {
        Bundle bundle = new Bundle();
        bundle.putString(PasspointManagementObjectManager.TAG_FQDN, fqdn);
        bundle.putParcelableList("MOS", managementObjectDefinitions);
        Message resultMsg = channel.sendMessageSynchronously(CMD_MODIFY_PASSPOINT_MO, bundle);
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result;
    }

    public boolean syncQueryPasspointIcon(AsyncChannel channel, long bssid, String fileName) {
        Bundle bundle = new Bundle();
        bundle.putLong("BSSID", bssid);
        bundle.putString("FILENAME", fileName);
        Message resultMsg = channel.sendMessageSynchronously(CMD_QUERY_OSU_ICON, bundle);
        int result = resultMsg.arg1;
        resultMsg.recycle();
        if (result == 1) {
            return true;
        }
        return false;
    }

    public int matchProviderWithCurrentNetwork(AsyncChannel channel, String fqdn) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_MATCH_PROVIDER_NETWORK, fqdn);
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result;
    }

    public void deauthenticateNetwork(AsyncChannel channel, long holdoff, boolean ess) {
    }

    public void disableEphemeralNetwork(String SSID) {
        if (SSID != null) {
            sendMessage(CMD_DISABLE_EPHEMERAL_NETWORK, SSID);
        }
    }

    public List<ScanDetail> getScanResultsListNoCopyUnsync() {
        return this.mScanResults;
    }

    public void disconnectCommand() {
        sendMessage(CMD_DISCONNECT);
    }

    public void disconnectCommand(int uid, int reason) {
        sendMessage(CMD_DISCONNECT, uid, reason);
    }

    public void reconnectCommand() {
        sendMessage(CMD_RECONNECT);
    }

    public void reassociateCommand() {
        sendMessage(CMD_REASSOCIATE);
    }

    public void reloadTlsNetworksAndReconnect() {
        sendMessage(CMD_RELOAD_TLS_AND_RECONNECT);
    }

    public int syncAddOrUpdateNetwork(AsyncChannel channel, WifiConfiguration config) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_ADD_OR_UPDATE_NETWORK, config);
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result;
    }

    public List<WifiConfiguration> syncGetConfiguredNetworks(int uuid, AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_CONFIGURED_NETWORKS, uuid);
        List<WifiConfiguration> result = resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public List<WifiConfiguration> syncGetPrivilegedConfiguredNetwork(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS);
        List<WifiConfiguration> result = resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public WifiConfiguration syncGetMatchingWifiConfig(ScanResult scanResult, AsyncChannel channel) {
        return (WifiConfiguration) channel.sendMessageSynchronously(CMD_GET_MATCHING_CONFIG, scanResult).obj;
    }

    public WifiConnectionStatistics syncGetConnectionStatistics(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_CONNECTION_STATISTICS);
        WifiConnectionStatistics result = resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public int syncGetSupportedFeatures(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_SUPPORTED_FEATURES);
        int supportedFeatureSet = resultMsg.arg1;
        resultMsg.recycle();
        return supportedFeatureSet;
    }

    public WifiLinkLayerStats syncGetLinkLayerStats(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_LINK_LAYER_STATS);
        WifiLinkLayerStats result = resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public boolean syncRemoveNetwork(AsyncChannel channel, int networkId) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_REMOVE_NETWORK, networkId);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public boolean syncEnableNetwork(AsyncChannel channel, int netId, boolean disableOthers) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_ENABLE_NETWORK, netId, disableOthers ? 1 : 0);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public boolean syncDisableNetwork(AsyncChannel channel, int netId) {
        Message resultMsg = channel.sendMessageSynchronously(151569, netId);
        boolean result = resultMsg.arg1 != 151570;
        resultMsg.recycle();
        return result;
    }

    public String syncGetWpsNfcConfigurationToken(int netId) {
        return this.mWifiNative.getNfcWpsConfigurationToken(netId);
    }

    public void addToBlacklist(String bssid) {
        sendMessage(CMD_BLACKLIST_NETWORK, bssid);
    }

    public void clearBlacklist() {
        sendMessage(CMD_CLEAR_BLACKLIST);
    }

    public void enableRssiPolling(boolean enabled) {
        int i;
        if (enabled) {
            i = 1;
        } else {
            i = 0;
        }
        sendMessage(CMD_ENABLE_RSSI_POLL, i, 0);
    }

    public void enableAllNetworks() {
        sendMessage(CMD_ENABLE_ALL_NETWORKS);
    }

    public void startFilteringMulticastPackets() {
        this.mIpManager.setMulticastFilter(true);
    }

    public void stopFilteringMulticastPackets() {
        this.mIpManager.setMulticastFilter(false);
    }

    public void setHighPerfModeEnabled(boolean enable) {
        int i;
        if (enable) {
            i = 1;
        } else {
            i = 0;
        }
        sendMessage(CMD_SET_HIGH_PERF_MODE, i, 0);
    }

    public synchronized void resetSimAuthNetworks() {
        sendMessage(CMD_RESET_SIM_NETWORKS);
    }

    public Network getCurrentNetwork() {
        if (this.mNetworkAgent != null) {
            return new Network(this.mNetworkAgent.netId);
        }
        return null;
    }

    public void setFrequencyBand(int band, boolean persist) {
        if (persist) {
            Global.putInt(this.mContext.getContentResolver(), "wifi_frequency_band", band);
        }
        if (this.mCust == null || !this.mCust.setHwCustWifiBand(this.mWifiNative, this.mFrequencyBand)) {
            sendMessage(CMD_SET_FREQUENCY_BAND, band, 0);
        }
    }

    public void enableTdls(String remoteMacAddress, boolean enable) {
        int enabler;
        if (enable) {
            enabler = 1;
        } else {
            enabler = 0;
        }
        sendMessage(CMD_ENABLE_TDLS, enabler, 0, remoteMacAddress);
    }

    public int getFrequencyBand() {
        return this.mFrequencyBand.get();
    }

    public String getConfigFile() {
        if (VDBG) {
            log("getConfigFile" + this.mWifiConfigManager.getConfigFile());
        }
        return this.mWifiConfigManager.getConfigFile();
    }

    public void sendBluetoothAdapterStateChange(int state) {
        sendMessage(CMD_BLUETOOTH_ADAPTER_STATE_CHANGE, state, 0);
    }

    public void removeAppConfigs(String packageName, int uid) {
        ApplicationInfo ai = new ApplicationInfo();
        ai.packageName = packageName;
        ai.uid = uid;
        sendMessage(CMD_REMOVE_APP_CONFIGURATIONS, ai);
    }

    public void removeUserConfigs(int userId) {
        sendMessage(CMD_REMOVE_USER_CONFIGURATIONS, userId);
    }

    public boolean syncSaveConfig(AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(CMD_SAVE_CONFIG);
        boolean result = resultMsg.arg1 != -1;
        resultMsg.recycle();
        return result;
    }

    public void updateBatteryWorkSource(WorkSource newSource) {
        synchronized (this.mRunningWifiUids) {
            if (newSource != null) {
                try {
                    this.mRunningWifiUids.set(newSource);
                } catch (RemoteException e) {
                }
            }
            if (this.mIsRunning) {
                if (!this.mReportedRunning) {
                    this.mBatteryStats.noteWifiRunning(this.mRunningWifiUids);
                    this.mLastRunningWifiUids.set(this.mRunningWifiUids);
                    this.mReportedRunning = true;
                } else if (this.mLastRunningWifiUids.diff(this.mRunningWifiUids)) {
                    this.mBatteryStats.noteWifiRunningChanged(this.mLastRunningWifiUids, this.mRunningWifiUids);
                    this.mLastRunningWifiUids.set(this.mRunningWifiUids);
                }
            } else if (this.mReportedRunning) {
                this.mBatteryStats.noteWifiStopped(this.mLastRunningWifiUids);
                this.mLastRunningWifiUids.clear();
                this.mReportedRunning = false;
            }
            this.mWakeLock.setWorkSource(newSource);
        }
    }

    public void dumpIpManager(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mIpManager.dump(fd, pw, args);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dump(fd, pw, args);
        this.mSupplicantStateTracker.dump(fd, pw, args);
        pw.println("mLinkProperties " + this.mLinkProperties);
        pw.println("mWifiInfo " + this.mWifiInfo);
        pw.println("mDhcpResults " + this.mDhcpResults);
        pw.println("mNetworkInfo " + this.mNetworkInfo);
        pw.println("mLastSignalLevel " + this.mLastSignalLevel);
        pw.println("mLastBssid " + this.mLastBssid);
        pw.println("mLastNetworkId " + this.mLastNetworkId);
        pw.println("mOperationalMode " + this.mOperationalMode);
        pw.println("mUserWantsSuspendOpt " + this.mUserWantsSuspendOpt);
        pw.println("mSuspendOptNeedsDisabled " + this.mSuspendOptNeedsDisabled);
        String status = this.mWifiNative.status(true);
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        String rexpMac = ":[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}:";
        if (status != null) {
            pw.println("Supplicant status " + status.replaceAll(rexpMac, ":**:**:**:**:").replaceAll(rexp, "xxx.xxx.xxx.xxx"));
        } else {
            pw.println("Supplicant status null!");
        }
        if (this.mCountryCode.getCurrentCountryCode() != null) {
            pw.println("CurrentCountryCode " + this.mCountryCode.getCurrentCountryCode());
        } else {
            pw.println("CurrentCountryCode is not initialized");
        }
        pw.println("mConnectedModeGScanOffloadStarted " + this.mConnectedModeGScanOffloadStarted);
        pw.println("mGScanPeriodMilli " + this.mGScanPeriodMilli);
        if (this.mWhiteListedSsids != null && this.mWhiteListedSsids.length > 0) {
            pw.println("SSID whitelist :");
            for (String str : this.mWhiteListedSsids) {
                pw.println("       " + str);
            }
        }
        if (this.mNetworkFactory != null) {
            this.mNetworkFactory.dump(fd, pw, args);
        } else {
            pw.println("mNetworkFactory is not initialized");
        }
        if (this.mUntrustedNetworkFactory != null) {
            this.mUntrustedNetworkFactory.dump(fd, pw, args);
        } else {
            pw.println("mUntrustedNetworkFactory is not initialized");
        }
        pw.println("Wlan Wake Reasons:" + this.mWifiNative.getWlanWakeReasonCount());
        pw.println();
        updateWifiMetrics();
        this.mWifiMetrics.dump(fd, pw, args);
        pw.println();
        this.mWifiConfigManager.dump(fd, pw, args);
        pw.println();
        this.mWifiLogger.captureBugReportData(7);
        this.mWifiLogger.dump(fd, pw, args);
        this.mWifiQualifiedNetworkSelector.dump(fd, pw, args);
        dumpIpManager(fd, pw, args);
        if (this.mWifiConnectivityManager != null) {
            this.mWifiConnectivityManager.dump(fd, pw, args);
        }
    }

    public void handleUserSwitch(int userId) {
        sendMessage(CMD_USER_SWITCH, userId);
    }

    private void logStateAndMessage(Message message, State state) {
        this.messageHandlingStatus = 0;
        if (mLogMessages) {
            switch (message.what) {
                case CMD_GET_CONFIGURED_NETWORKS /*131131*/:
                case CMD_GET_SUPPORTED_FEATURES /*131133*/:
                case CMD_GET_LINK_LAYER_STATS /*131135*/:
                case CMD_START_SCAN /*131143*/:
                case CMD_RSSI_POLL /*131155*/:
                case CMD_UPDATE_LINKPROPERTIES /*131212*/:
                case WifiMonitor.SCAN_RESULTS_EVENT /*147461*/:
                case 151572:
                    if (DBG) {
                        logd(" " + state.getClass().getSimpleName() + " " + getLogRecString(message));
                        return;
                    }
                    return;
                default:
                    logd(" " + state.getClass().getSimpleName() + " " + getLogRecString(message));
                    return;
            }
        }
    }

    String printTime() {
        StringBuilder sb = new StringBuilder();
        sb.append(" rt=").append(SystemClock.uptimeMillis());
        sb.append("/").append(SystemClock.elapsedRealtime());
        return sb.toString();
    }

    protected String getLogRecString(Message msg) {
        StringBuilder sb = new StringBuilder();
        if (this.mScreenOn) {
            sb.append("!");
        }
        if (this.messageHandlingStatus != MESSAGE_HANDLING_STATUS_UNKNOWN) {
            sb.append("(").append(this.messageHandlingStatus).append(")");
        }
        sb.append(smToString(msg));
        if (msg.sendingUid > 0 && msg.sendingUid != 1010) {
            sb.append(" uid=").append(msg.sendingUid);
        }
        sb.append(" ").append(printTime());
        WifiConfiguration config;
        String key;
        Long now;
        String report;
        switch (msg.what) {
            case CMD_ADD_OR_UPDATE_NETWORK /*131124*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.obj != null) {
                    config = (WifiConfiguration) msg.obj;
                    sb.append(" ").append(config.configKey());
                    sb.append(" prio=").append(config.priority);
                    sb.append(" status=").append(config.status);
                    if (config.BSSID != null) {
                        sb.append(" ").append(config.BSSID);
                    }
                    WifiConfiguration curConfig = getCurrentWifiConfiguration();
                    if (curConfig != null) {
                        if (!curConfig.configKey().equals(config.configKey())) {
                            sb.append(" current=").append(curConfig.configKey());
                            sb.append(" prio=").append(curConfig.priority);
                            sb.append(" status=").append(curConfig.status);
                            break;
                        }
                        sb.append(" is current");
                        break;
                    }
                }
                break;
            case CMD_ENABLE_NETWORK /*131126*/:
            case 151569:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                key = this.mWifiConfigManager.getLastSelectedConfiguration();
                if (key != null) {
                    sb.append(" last=").append(key);
                }
                config = this.mWifiConfigManager.getWifiConfiguration(msg.arg1);
                if (config != null && (key == null || !config.configKey().equals(key))) {
                    sb.append(" target=").append(key);
                    break;
                }
            case CMD_GET_CONFIGURED_NETWORKS /*131131*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" num=").append(this.mWifiConfigManager.getConfiguredNetworksSize());
                break;
            case CMD_START_SCAN /*131143*/:
                now = Long.valueOf(System.currentTimeMillis());
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" ic=");
                sb.append(Integer.toString(sScanAlarmIntentCount));
                if (msg.obj != null) {
                    Long request = Long.valueOf(msg.obj.getLong(SCAN_REQUEST_TIME, 0));
                    if (request.longValue() != 0) {
                        sb.append(" proc(ms):").append(now.longValue() - request.longValue());
                    }
                }
                if (this.mIsScanOngoing) {
                    sb.append(" onGoing");
                }
                if (this.mIsFullScanOngoing) {
                    sb.append(" full");
                }
                sb.append(" rssi=").append(this.mWifiInfo.getRssi());
                sb.append(" f=").append(this.mWifiInfo.getFrequency());
                sb.append(" sc=").append(this.mWifiInfo.score);
                sb.append(" link=").append(this.mWifiInfo.getLinkSpeed());
                sb.append(String.format(" tx=%.1f,", new Object[]{Double.valueOf(this.mWifiInfo.txSuccessRate)}));
                sb.append(String.format(" %.1f,", new Object[]{Double.valueOf(this.mWifiInfo.txRetriesRate)}));
                sb.append(String.format(" %.1f ", new Object[]{Double.valueOf(this.mWifiInfo.txBadRate)}));
                sb.append(String.format(" rx=%.1f", new Object[]{Double.valueOf(this.mWifiInfo.rxSuccessRate)}));
                if (this.lastScanFreqs != null) {
                    sb.append(" list=");
                    for (Integer intValue : this.lastScanFreqs) {
                        sb.append(intValue.intValue()).append(",");
                    }
                }
                report = reportOnTime();
                if (report != null) {
                    sb.append(" ").append(report);
                    break;
                }
                break;
            case CMD_RSSI_POLL /*131155*/:
            case CMD_UNWANTED_NETWORK /*131216*/:
            case 151572:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (!(this.mWifiInfo.getSSID() == null || this.mWifiInfo.getSSID() == null)) {
                    sb.append(" ").append(this.mWifiInfo.getSSID());
                }
                if (this.mWifiInfo.getBSSID() != null) {
                    sb.append(" ").append(this.mWifiInfo.getBSSID());
                }
                sb.append(" rssi=").append(this.mWifiInfo.getRssi());
                sb.append(" f=").append(this.mWifiInfo.getFrequency());
                sb.append(" sc=").append(this.mWifiInfo.score);
                sb.append(" link=").append(this.mWifiInfo.getLinkSpeed());
                sb.append(String.format(" tx=%.1f,", new Object[]{Double.valueOf(this.mWifiInfo.txSuccessRate)}));
                sb.append(String.format(" %.1f,", new Object[]{Double.valueOf(this.mWifiInfo.txRetriesRate)}));
                sb.append(String.format(" %.1f ", new Object[]{Double.valueOf(this.mWifiInfo.txBadRate)}));
                sb.append(String.format(" rx=%.1f", new Object[]{Double.valueOf(this.mWifiInfo.rxSuccessRate)}));
                sb.append(String.format(" bcn=%d", new Object[]{Integer.valueOf(this.mRunningBeaconCount)}));
                report = reportOnTime();
                if (report != null) {
                    sb.append(" ").append(report);
                }
                if (this.mWifiScoreReport != null) {
                    sb.append(this.mWifiScoreReport.getReport());
                }
                if (!this.mConnectedModeGScanOffloadStarted) {
                    sb.append(" offload-stopped");
                    break;
                }
                sb.append(" offload-started periodMilli ").append(this.mGScanPeriodMilli);
                break;
            case CMD_ROAM_WATCHDOG_TIMER /*131166*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=").append(this.roamWatchdogCount);
                break;
            case CMD_DISCONNECTING_WATCHDOG_TIMER /*131168*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=").append(this.disconnectingWatchdogCount);
                break;
            case CMD_IP_CONFIGURATION_LOST /*131211*/:
                int count = -1;
                WifiConfiguration c = getCurrentWifiConfiguration();
                if (c != null) {
                    count = c.getNetworkSelectionStatus().getDisableReasonCounter(4);
                }
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" failures: ");
                sb.append(Integer.toString(count));
                sb.append("/");
                sb.append(Integer.toString(this.mWifiConfigManager.getMaxDhcpRetries()));
                if (this.mWifiInfo.getBSSID() != null) {
                    sb.append(" ").append(this.mWifiInfo.getBSSID());
                }
                sb.append(String.format(" bcn=%d", new Object[]{Integer.valueOf(this.mRunningBeaconCount)}));
                break;
            case CMD_UPDATE_LINKPROPERTIES /*131212*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (this.mLinkProperties != null) {
                    sb.append(" ");
                    sb.append(getLinkPropertiesSummary(this.mLinkProperties));
                    break;
                }
                break;
            case CMD_TARGET_BSSID /*131213*/:
            case CMD_ASSOCIATED_BSSID /*131219*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.obj != null) {
                    sb.append(" BSSID=").append((String) msg.obj);
                }
                if (this.mTargetRoamBSSID != null) {
                    sb.append(" Target=").append(this.mTargetRoamBSSID);
                }
                sb.append(" roam=").append(Boolean.toString(this.mAutoRoaming));
                break;
            case CMD_AUTO_CONNECT /*131215*/:
            case 151553:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                config = this.mWifiConfigManager.getWifiConfiguration(msg.arg1);
                if (config != null) {
                    sb.append(" ").append(config.configKey());
                    if (config.visibility != null) {
                        sb.append(" ").append(config.visibility.toString());
                    }
                }
                if (this.mTargetRoamBSSID != null) {
                    sb.append(" ").append(this.mTargetRoamBSSID);
                }
                sb.append(" roam=").append(Boolean.toString(this.mAutoRoaming));
                config = getCurrentWifiConfiguration();
                if (config != null) {
                    sb.append(config.configKey());
                    if (config.visibility != null) {
                        sb.append(" ").append(config.visibility.toString());
                        break;
                    }
                }
                break;
            case CMD_AUTO_ROAM /*131217*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                ScanResult result = msg.obj;
                if (result != null) {
                    now = Long.valueOf(System.currentTimeMillis());
                    sb.append(" bssid=").append(result.BSSID);
                    sb.append(" rssi=").append(result.level);
                    sb.append(" freq=").append(result.frequency);
                    if (result.seen <= 0 || result.seen >= now.longValue()) {
                        sb.append(" !seen=").append(result.seen);
                    } else {
                        sb.append(" seen=").append(now.longValue() - result.seen);
                    }
                }
                if (this.mTargetRoamBSSID != null) {
                    sb.append(" ").append(this.mTargetRoamBSSID);
                }
                sb.append(" roam=").append(Boolean.toString(this.mAutoRoaming));
                sb.append(" fail count=").append(Integer.toString(this.mRoamFailCount));
                break;
            case CMD_AUTO_SAVE_NETWORK /*131218*/:
            case 151559:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (this.lastSavedConfigurationAttempt != null) {
                    sb.append(" ").append(this.lastSavedConfigurationAttempt.configKey());
                    sb.append(" nid=").append(this.lastSavedConfigurationAttempt.networkId);
                    if (this.lastSavedConfigurationAttempt.hiddenSSID) {
                        sb.append(" hidden");
                    }
                    if (!(this.lastSavedConfigurationAttempt.preSharedKey == null || this.lastSavedConfigurationAttempt.preSharedKey.equals("*"))) {
                        sb.append(" hasPSK");
                    }
                    if (this.lastSavedConfigurationAttempt.ephemeral) {
                        sb.append(" ephemeral");
                    }
                    if (this.lastSavedConfigurationAttempt.selfAdded) {
                        sb.append(" selfAdded");
                    }
                    sb.append(" cuid=").append(this.lastSavedConfigurationAttempt.creatorUid);
                    sb.append(" suid=").append(this.lastSavedConfigurationAttempt.lastUpdateUid);
                    break;
                }
                break;
            case CMD_IP_REACHABILITY_LOST /*131221*/:
                if (msg.obj != null) {
                    sb.append(" ").append((String) msg.obj);
                    break;
                }
                break;
            case CMD_UPDATE_ASSOCIATED_SCAN_PERMISSION /*131230*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" autojoinAllowed=");
                sb.append(this.mWifiConfigManager.getEnableAutoJoinWhenAssociated());
                sb.append(" withTraffic=").append(getAllowScansWithTraffic());
                sb.append(" tx=").append(this.mWifiInfo.txSuccessRate);
                sb.append("/").append(8);
                sb.append(" rx=").append(this.mWifiInfo.rxSuccessRate);
                sb.append("/").append(16);
                sb.append(" -> ").append(this.mConnectedModeGScanOffloadStarted);
                break;
            case CMD_START_RSSI_MONITORING_OFFLOAD /*131234*/:
            case CMD_STOP_RSSI_MONITORING_OFFLOAD /*131235*/:
            case CMD_RSSI_THRESHOLD_BREACH /*131236*/:
                sb.append(" rssi=");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" thresholds=");
                sb.append(Arrays.toString(this.mRssiRanges));
                break;
            case CMD_USER_SWITCH /*131237*/:
                sb.append(" userId=");
                sb.append(Integer.toString(msg.arg1));
                break;
            case CMD_IPV4_PROVISIONING_SUCCESS /*131272*/:
                sb.append(" ");
                if (msg.arg1 != 1) {
                    if (msg.arg1 != CMD_STATIC_IP_SUCCESS) {
                        sb.append(Integer.toString(msg.arg1));
                        break;
                    }
                    sb.append("STATIC_OK");
                    break;
                }
                sb.append("DHCP_OK");
                break;
            case CMD_IPV4_PROVISIONING_FAILURE /*131273*/:
                sb.append(" ");
                if (msg.arg1 != 2) {
                    if (msg.arg1 != CMD_STATIC_IP_FAILURE) {
                        sb.append(Integer.toString(msg.arg1));
                        break;
                    }
                    sb.append("STATIC_FAIL");
                    break;
                }
                sb.append("DHCP_FAIL");
                break;
            case CMD_INSTALL_PACKET_FILTER /*131274*/:
                sb.append(" len=").append(((byte[]) msg.obj).length);
                break;
            case CMD_SET_FALLBACK_PACKET_FILTERING /*131275*/:
                sb.append(" enabled=").append(((Boolean) msg.obj).booleanValue());
                break;
            case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /*143371*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.obj != null) {
                    NetworkInfo info = msg.obj;
                    NetworkInfo.State state = info.getState();
                    DetailedState detailedState = info.getDetailedState();
                    if (state != null) {
                        sb.append(" st=").append(state);
                    }
                    if (detailedState != null) {
                        sb.append("/").append(detailedState);
                        break;
                    }
                }
                break;
            case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" ").append(this.mLastBssid);
                sb.append(" nid=").append(this.mLastNetworkId);
                config = getCurrentWifiConfiguration();
                if (config != null) {
                    sb.append(" ").append(config.configKey());
                }
                key = this.mWifiConfigManager.getLastSelectedConfiguration();
                if (key != null) {
                    sb.append(" last=").append(key);
                    break;
                }
                break;
            case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                if (msg.obj != null) {
                    sb.append(" ").append((String) msg.obj);
                }
                sb.append(" nid=").append(msg.arg1);
                sb.append(" reason=").append(msg.arg2);
                if (this.mLastBssid != null) {
                    sb.append(" lastbssid=").append(this.mLastBssid);
                }
                if (this.mWifiInfo.getFrequency() != -1) {
                    sb.append(" freq=").append(this.mWifiInfo.getFrequency());
                    sb.append(" rssi=").append(this.mWifiInfo.getRssi());
                }
                if (this.linkDebouncing) {
                    sb.append(" debounce");
                    break;
                }
                break;
            case WifiMonitor.SCAN_RESULTS_EVENT /*147461*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (this.mScanResults != null) {
                    sb.append(" found=");
                    sb.append(this.mScanResults.size());
                }
                sb.append(" known=").append(this.mNumScanResultsKnown);
                sb.append(" got=").append(this.mNumScanResultsReturned);
                sb.append(String.format(" bcn=%d", new Object[]{Integer.valueOf(this.mRunningBeaconCount)}));
                sb.append(String.format(" con=%d", new Object[]{Integer.valueOf(this.mConnectionRequests)}));
                key = this.mWifiConfigManager.getLastSelectedConfiguration();
                if (key != null) {
                    sb.append(" last=").append(key);
                    break;
                }
                break;
            case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                StateChangeResult stateChangeResult = msg.obj;
                if (stateChangeResult != null) {
                    sb.append(stateChangeResult.toString());
                    break;
                }
                break;
            case WifiMonitor.SSID_TEMP_DISABLED /*147469*/:
            case WifiMonitor.SSID_REENABLED /*147470*/:
                sb.append(" nid=").append(msg.arg1);
                if (msg.obj != null) {
                    sb.append(" ").append((String) msg.obj);
                }
                config = getCurrentWifiConfiguration();
                if (config != null) {
                    NetworkSelectionStatus netWorkSelectionStatus = config.getNetworkSelectionStatus();
                    sb.append(" cur=").append(config.configKey());
                    sb.append(" ajst=").append(netWorkSelectionStatus.getNetworkStatusString());
                    if (config.selfAdded) {
                        sb.append(" selfAdded");
                    }
                    if (config.status != 0) {
                        sb.append(" st=").append(config.status);
                        sb.append(" rs=").append(netWorkSelectionStatus.getNetworkDisableReasonString());
                    }
                    if (config.lastConnected != 0) {
                        sb.append(" lastconn=").append(Long.valueOf(System.currentTimeMillis()).longValue() - config.lastConnected).append("(ms)");
                    }
                    if (this.mLastBssid != null) {
                        sb.append(" lastbssid=").append(this.mLastBssid);
                    }
                    if (this.mWifiInfo.getFrequency() != -1) {
                        sb.append(" freq=").append(this.mWifiInfo.getFrequency());
                        sb.append(" rssi=").append(this.mWifiInfo.getRssi());
                        sb.append(" bssid=").append(this.mWifiInfo.getBSSID());
                        break;
                    }
                }
                break;
            case WifiMonitor.SCAN_FAILED_EVENT /*147473*/:
                break;
            case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                String bssid = msg.obj;
                if (bssid != null && bssid.length() > 0) {
                    sb.append(" ");
                    sb.append(bssid);
                }
                sb.append(" blacklist=").append(Boolean.toString(this.didBlackListBSSID));
                break;
            case 151556:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (this.lastForgetConfigurationAttempt != null) {
                    sb.append(" ").append(this.lastForgetConfigurationAttempt.configKey());
                    sb.append(" nid=").append(this.lastForgetConfigurationAttempt.networkId);
                    if (this.lastForgetConfigurationAttempt.hiddenSSID) {
                        sb.append(" hidden");
                    }
                    if (this.lastForgetConfigurationAttempt.preSharedKey != null) {
                        sb.append(" hasPSK");
                    }
                    if (this.lastForgetConfigurationAttempt.ephemeral) {
                        sb.append(" ephemeral");
                    }
                    if (this.lastForgetConfigurationAttempt.selfAdded) {
                        sb.append(" selfAdded");
                    }
                    sb.append(" cuid=").append(this.lastForgetConfigurationAttempt.creatorUid);
                    sb.append(" suid=").append(this.lastForgetConfigurationAttempt.lastUpdateUid);
                    sb.append(" ajst=").append(this.lastForgetConfigurationAttempt.getNetworkSelectionStatus().getNetworkStatusString());
                    break;
                }
                break;
            case 196611:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" txpkts=").append(this.mWifiInfo.txSuccess);
                sb.append(",").append(this.mWifiInfo.txBad);
                sb.append(",").append(this.mWifiInfo.txRetries);
                break;
            case 196612:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.arg1 == 1) {
                    sb.append(" OK ");
                } else if (msg.arg1 == 2) {
                    sb.append(" FAIL ");
                }
                if (this.mLinkProperties != null) {
                    sb.append(" ");
                    sb.append(getLinkPropertiesSummary(this.mLinkProperties));
                    break;
                }
                break;
            default:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                break;
        }
        return sb.toString();
    }

    private void handleScreenStateChanged(boolean screenOn) {
        this.mScreenOn = screenOn;
        if (DBG) {
            logd(" handleScreenStateChanged Enter: screenOn=" + screenOn + " mUserWantsSuspendOpt=" + this.mUserWantsSuspendOpt + " state " + getCurrentState().getName() + " suppState:" + this.mSupplicantStateTracker.getSupplicantStateName());
        }
        if (this.mWifiStatStore != null) {
            this.mWifiStatStore.updateScreenState(screenOn);
        }
        enableRssiPolling(screenOn);
        if (this.mUserWantsSuspendOpt.get()) {
            if (screenOn) {
                sendMessage(CMD_SET_SUSPEND_OPT_ENABLED, 0, 0);
            } else {
                this.mSuspendWakeLock.acquire(2000);
                sendMessage(CMD_SET_SUSPEND_OPT_ENABLED, 1, 0);
            }
        }
        this.mScreenBroadcastReceived.set(true);
        if (this.mIsRunning) {
            getWifiLinkLayerStats(false);
            this.mOnTimeScreenStateChange = this.mOnTime;
            this.lastScreenStateChangeTimeStamp = this.lastLinkLayerStatsUpdate;
        }
        this.mWifiMetrics.setScreenState(screenOn);
        if (this.mWifiConnectivityManager != null) {
            long currenTime = SystemClock.elapsedRealtime();
            if (screenOn && currenTime - this.mLastAllowSendingTime > ALLOW_SEND_SCAN_RESULTS_BROADCAST_INTERVAL_MS) {
                Log.d(TAG, "handleScreenStateChanged: allow send scan results broadcast.");
                this.mSendScanResultsBroadcast = true;
                this.mLastAllowSendingTime = currenTime;
            }
            this.mWifiConnectivityManager.handleScreenStateChanged(screenOn);
        }
        if (DBG) {
            log("handleScreenStateChanged Exit: " + screenOn);
        }
    }

    private void checkAndSetConnectivityInstance() {
        if (this.mCm == null) {
            this.mCm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        }
    }

    private void setFrequencyBand() {
        if (this.mCust == null || !this.mCust.setHwCustWifiBand(this.mWifiNative, this.mFrequencyBand)) {
            if (this.mWifiNative.setBand(0)) {
                this.mFrequencyBand.set(0);
                if (this.mWifiConnectivityManager != null) {
                    this.mWifiConnectivityManager.setUserPreferredBand(0);
                }
                if (DBG) {
                    logd("done set frequency band " + 0);
                }
            } else {
                loge("Failed to set frequency band " + 0);
            }
        }
    }

    private void setWifiState(int wifiState) {
        int previousWifiState = this.mWifiState.get();
        if (wifiState == 3) {
            try {
                this.mBatteryStats.noteWifiOn();
            } catch (RemoteException e) {
                loge("Failed to note battery stats in wifi");
            }
        } else if (wifiState == 1) {
            this.mBatteryStats.noteWifiOff();
        }
        if (this.mHwWifiCHRService != null) {
            this.mHwWifiCHRService.updateWifiState(wifiState);
        }
        this.mWifiState.set(wifiState);
        if (DBG) {
            log("setWifiState: " + syncGetWifiStateByName());
        }
        if (checkSelfCureWifiResult()) {
            log("setWifiState, ignore to send intent due to wifi self curing.");
            return;
        }
        Intent intent = new Intent("android.net.wifi.WIFI_STATE_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("wifi_state", wifiState);
        intent.putExtra("previous_wifi_state", previousWifiState);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void setWifiApState(int wifiApState, int reason) {
        int previousWifiApState = this.mWifiApState.get();
        if (wifiApState == 13) {
            try {
                this.mBatteryStats.noteWifiOn();
            } catch (RemoteException e) {
                loge("Failed to note battery stats in wifi");
            }
        } else if (wifiApState == 11) {
            this.mBatteryStats.noteWifiOff();
        }
        this.mWifiApState.set(wifiApState);
        if (DBG) {
            log("setWifiApState: " + syncGetWifiApStateByName());
        }
        Intent intent = new Intent("android.net.wifi.WIFI_AP_STATE_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("wifi_state", wifiApState);
        intent.putExtra("previous_wifi_state", previousWifiApState);
        if (wifiApState == 14) {
            intent.putExtra("wifi_ap_error_code", reason);
        }
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public void setmSettingsStore(WifiSettingsStore settingsStore) {
        this.mWifiSettingStore = settingsStore;
    }

    private void setScanResults() {
        this.mNumScanResultsKnown = 0;
        this.mNumScanResultsReturned = 0;
        ArrayList<ScanDetail> scanResults = this.mWifiNative.getScanResults();
        if (scanResults.isEmpty()) {
            this.mScanResults = new ArrayList();
            this.mEmptyScanResultCount++;
            if (this.mEmptyScanResultCount > 10 && this.mWiFiCHRManager != null) {
                this.mWiFiCHRManager.syncSetScanResultsList(this.mScanResults);
            }
            return;
        }
        this.mEmptyScanResultCount = 0;
        this.mWifiConfigManager.trimANQPCache(false);
        boolean connected = this.mLastBssid != null;
        long activeBssid = 0;
        if (connected) {
            try {
                activeBssid = Utils.parseMac(this.mLastBssid);
            } catch (IllegalArgumentException e) {
                connected = false;
            }
        }
        synchronized (this.mScanResultsLock) {
            ScanDetail activeScanDetail = null;
            this.mScanResults = scanResults;
            this.mNumScanResultsReturned = this.mScanResults.size();
            for (ScanDetail resultDetail : this.mScanResults) {
                updateScanDetailByWifiPro(resultDetail);
                if (connected && resultDetail.getNetworkDetail().getBSSID() == activeBssid) {
                    if (activeScanDetail != null && activeScanDetail.getNetworkDetail().getBSSID() == activeBssid) {
                        if (activeScanDetail.getNetworkDetail().getANQPElements() == null) {
                        }
                    }
                    activeScanDetail = resultDetail;
                }
                NetworkDetail networkDetail = resultDetail.getNetworkDetail();
                if (networkDetail != null && networkDetail.getDtimInterval() > 0) {
                    List<WifiConfiguration> associatedWifiConfigurations = this.mWifiConfigManager.getSavedNetworkFromScanDetail(resultDetail);
                    if (associatedWifiConfigurations != null) {
                        for (WifiConfiguration associatedConf : associatedWifiConfigurations) {
                            if (associatedConf != null) {
                                associatedConf.dtimInterval = networkDetail.getDtimInterval();
                            }
                        }
                    }
                }
            }
            this.mWifiConfigManager.setActiveScanDetail(activeScanDetail);
        }
        if (this.mWiFiCHRManager != null) {
            this.mWiFiCHRManager.syncSetScanResultsList(this.mScanResults);
        }
        if (this.linkDebouncing) {
            sendMessage(CMD_AUTO_ROAM, this.mLastNetworkId, 1, null);
        }
    }

    public boolean isTargetNetworkEnabled() {
        if (this.targetWificonfiguration == null) {
            return false;
        }
        NetworkSelectionStatus status = this.targetWificonfiguration.getNetworkSelectionStatus();
        if (status == null) {
            return false;
        }
        return status.isNetworkEnabled();
    }

    public boolean attemptAutoConnect() {
        SupplicantState state = this.mWifiInfo.getSupplicantState();
        if (getCurrentState() != this.mRoamingState && getCurrentState() != this.mObtainingIpState && getCurrentState() != this.mScanModeState && getCurrentState() != this.mDisconnectingState && ((getCurrentState() != this.mConnectedState || getEnableAutoJoinWhenAssociated()) && !this.linkDebouncing && state != SupplicantState.ASSOCIATING && state != SupplicantState.AUTHENTICATING && state != SupplicantState.FOUR_WAY_HANDSHAKE && state != SupplicantState.GROUP_HANDSHAKE)) {
            return true;
        }
        Log.w(TAG, "attemptAutoConnect: false");
        return false;
    }

    public void setCHRConnectingSartTimestamp(long connectingStartTimestamp) {
        if (connectingStartTimestamp > 0) {
            this.mConnectingStartTimestamp = connectingStartTimestamp;
        }
    }

    private void fetchRssiLinkSpeedAndFrequencyNative() {
        Integer num = null;
        Integer num2 = null;
        Integer num3 = null;
        if (SupplicantState.ASSOCIATED.compareTo(this.mWifiInfo.getSupplicantState()) > 0 || SupplicantState.COMPLETED.compareTo(this.mWifiInfo.getSupplicantState()) < 0) {
            loge("error state to fetch rssi");
            return;
        }
        String signalPoll = this.mWifiNative.signalPoll();
        if (signalPoll != null) {
            for (String line : signalPoll.split("\n")) {
                String[] prop = line.split("=");
                if (prop.length >= 2) {
                    try {
                        if (prop[0].equals("RSSI")) {
                            num = Integer.valueOf(Integer.parseInt(prop[1]));
                        } else if (prop[0].equals("LINKSPEED")) {
                            num2 = Integer.valueOf(Integer.parseInt(prop[1]));
                            if (this.mWiFiCHRManager != null) {
                                this.mWiFiCHRManager.updateLinkSpeed(num2.intValue());
                            }
                        } else if (prop[0].equals("FREQUENCY")) {
                            num3 = Integer.valueOf(Integer.parseInt(prop[1]));
                            if (this.mWiFiCHRManager != null) {
                                this.mWiFiCHRManager.updateChannel(num3.intValue());
                            }
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
        if (DBG) {
            logd("fetchRssiLinkSpeedAndFrequencyNative rssi=" + num + " linkspeed=" + num2 + " freq=" + num3);
        }
        if (num == null || num.intValue() <= -127 || num.intValue() >= 200) {
            this.mWifiInfo.setRssi(-127);
            updateCapabilities(getCurrentWifiConfiguration());
        } else {
            if (num.intValue() > 0) {
                num = Integer.valueOf(num.intValue() - 256);
            }
            if (this.mWiFiCHRManager != null) {
                this.mWiFiCHRManager.updateRSSI(num.intValue());
            }
            this.mWifiInfo.setRssi(num.intValue());
            int newSignalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(num.intValue());
            if (newSignalLevel != this.mLastSignalLevel) {
                updateCapabilities(getCurrentWifiConfiguration());
                sendRssiChangeBroadcast(num.intValue());
            }
            this.mLastSignalLevel = newSignalLevel;
            if (!(this.mWiFiCHRManager == null || num3 == null)) {
                this.mWiFiCHRManager.updateSignalLevel(num.intValue(), num3.intValue(), newSignalLevel);
            }
        }
        if (num2 != null) {
            this.mWifiInfo.setLinkSpeed(num2.intValue());
        }
        if (num3 != null && num3.intValue() > 0) {
            WifiConnectionStatistics wifiConnectionStatistics;
            if (ScanResult.is5GHz(num3.intValue())) {
                wifiConnectionStatistics = this.mWifiConnectionStatistics;
                wifiConnectionStatistics.num5GhzConnected++;
            }
            if (ScanResult.is24GHz(num3.intValue())) {
                wifiConnectionStatistics = this.mWifiConnectionStatistics;
                wifiConnectionStatistics.num24GhzConnected++;
            }
            this.mWifiInfo.setFrequency(num3.intValue());
            sendStaFrequency(num3.intValue());
        }
        this.mWifiConfigManager.updateConfiguration(this.mWifiInfo);
    }

    private void cleanWifiScore() {
        this.mWifiInfo.txBadRate = 0.0d;
        this.mWifiInfo.txSuccessRate = 0.0d;
        this.mWifiInfo.txRetriesRate = 0.0d;
        this.mWifiInfo.rxSuccessRate = 0.0d;
        this.mWifiScoreReport = null;
    }

    public double getTxPacketRate() {
        return this.mWifiInfo.txSuccessRate;
    }

    public double getRxPacketRate() {
        return this.mWifiInfo.rxSuccessRate;
    }

    private void fetchPktcntNative(RssiPacketCountInfo info) {
        String pktcntPoll = this.mWifiNative.pktcntPoll();
        if (pktcntPoll != null) {
            for (String line : pktcntPoll.split("\n")) {
                String[] prop = line.split("=");
                if (prop.length >= 2) {
                    try {
                        if (prop[0].equals("TXGOOD")) {
                            info.txgood = Integer.parseInt(prop[1]);
                        } else if (prop[0].equals("TXBAD")) {
                            info.txbad = Integer.parseInt(prop[1]);
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
    }

    private void updateLinkProperties(LinkProperties newLp) {
        if (DBG) {
            log("Link configuration changed for netId: " + this.mLastNetworkId + " old: " + this.mLinkProperties + " new: " + newLp);
        }
        this.mLinkProperties = newLp;
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendLinkProperties(this.mLinkProperties);
        }
        if (getNetworkDetailedState() == DetailedState.CONNECTED) {
            sendLinkConfigurationChangedBroadcast();
        }
        if (DBG) {
            StringBuilder sb = new StringBuilder();
            sb.append("updateLinkProperties nid: ").append(this.mLastNetworkId);
            sb.append(" state: ").append(getNetworkDetailedState());
            if (this.mLinkProperties != null) {
                sb.append(" ");
                sb.append(getLinkPropertiesSummary(this.mLinkProperties));
            }
            logd(sb.toString());
        }
    }

    private void clearLinkProperties() {
        synchronized (this.mDhcpResultsLock) {
            if (this.mDhcpResults != null) {
                this.mDhcpResults.clear();
            }
        }
        this.mLinkProperties.clear();
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendLinkProperties(this.mLinkProperties);
        }
    }

    private String updateDefaultRouteMacAddress(int timeout) {
        String address = null;
        for (RouteInfo route : this.mLinkProperties.getRoutes()) {
            if (route.isDefaultRoute() && route.hasGateway()) {
                InetAddress gateway = route.getGateway();
                if (gateway instanceof Inet4Address) {
                    if (DBG) {
                        logd("updateDefaultRouteMacAddress found Ipv4 default :" + gateway.getHostAddress());
                    }
                    address = macAddressFromRoute(gateway.getHostAddress());
                    if (address == null && timeout > 0) {
                        try {
                            Thread.sleep((long) timeout);
                        } catch (InterruptedException e) {
                        }
                        address = macAddressFromRoute(gateway.getHostAddress());
                        if (PDBG) {
                            loge("updateDefaultRouteMacAddress reachable (tried again) :" + gateway.getHostAddress() + " found " + address);
                        }
                    }
                    if (address != null) {
                        this.mWifiConfigManager.setDefaultGwMacAddress(this.mLastNetworkId, address);
                    }
                }
            }
        }
        return address;
    }

    void sendScanResultsAvailableBroadcast(boolean scanSucceeded) {
        Intent intent = new Intent("android.net.wifi.SCAN_RESULTS");
        intent.addFlags(67108864);
        intent.putExtra("resultsUpdated", scanSucceeded);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void sendRssiChangeBroadcast(int newRssi) {
        try {
            this.mBatteryStats.noteWifiRssiChanged(newRssi);
        } catch (RemoteException e) {
        }
        Intent intent = new Intent("android.net.wifi.RSSI_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("newRssi", newRssi);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void sendNetworkStateChangeBroadcast(String bssid) {
        Intent intent = new Intent("android.net.wifi.STATE_CHANGE");
        intent.addFlags(268435456);
        intent.addFlags(67108864);
        intent.putExtra("networkInfo", new NetworkInfo(this.mNetworkInfo));
        intent.putExtra("linkProperties", new LinkProperties(this.mLinkProperties));
        if (bssid != null) {
            intent.putExtra("bssid", bssid);
        }
        if (this.mNetworkInfo.getDetailedState() == DetailedState.VERIFYING_POOR_LINK || this.mNetworkInfo.getDetailedState() == DetailedState.CONNECTED) {
            fetchRssiLinkSpeedAndFrequencyNative();
            WifiInfo sentWifiInfo = new WifiInfo(this.mWifiInfo);
            sentWifiInfo.setMacAddress("02:00:00:00:00:00");
            intent.putExtra("wifiInfo", sentWifiInfo);
        }
        checkSelfCureWifiResult();
        if (!ignoreNetworkStateChange(this.mNetworkInfo)) {
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private WifiInfo getWiFiInfoForUid(int uid) {
        WifiInfo result;
        if (isWifiSelfCuring()) {
            result = new WifiInfo(this.mWifiInfo);
            result.setNetworkId(getSelfCureNetworkId());
            if (result.getRssi() <= -127) {
                result.setRssi(-70);
            }
            result.setSupplicantState(SupplicantState.COMPLETED);
            return result;
        } else if (Binder.getCallingUid() == Process.myUid()) {
            return this.mWifiInfo;
        } else {
            boolean apiLevel23App;
            result = new WifiInfo(this.mWifiInfo);
            result.setMacAddress("02:00:00:00:00:00");
            IPackageManager packageManager = IPackageManager.Stub.asInterface(this.mFacade.getService("package"));
            try {
                apiLevel23App = isMApp(this.mContext, getAppName(Binder.getCallingPid()));
            } catch (Exception e) {
                apiLevel23App = true;
            }
            if (apiLevel23App) {
                try {
                } catch (RemoteException e2) {
                    Log.e(TAG, "Error checking receiver permission", e2);
                }
            }
            result.setMacAddress(this.mWifiInfo.getMacAddress());
            return result;
        }
    }

    private String getAppName(int pID) {
        String processName = "";
        List<RunningAppProcessInfo> appProcessList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == pID) {
                return appProcess.processName;
            }
        }
        return null;
    }

    private static boolean isMApp(Context context, String pkgName) {
        boolean z = true;
        if (pkgName == null) {
            return true;
        }
        try {
            if (context.getPackageManager().getApplicationInfo(pkgName, 0).targetSdkVersion < 23) {
                z = false;
            }
            return z;
        } catch (NameNotFoundException e) {
            return true;
        }
    }

    private void sendLinkConfigurationChangedBroadcast() {
        Intent intent = new Intent("android.net.wifi.LINK_CONFIGURATION_CHANGED");
        intent.addFlags(67108864);
        intent.putExtra("linkProperties", new LinkProperties(this.mLinkProperties));
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void sendSupplicantConnectionChangedBroadcast(boolean connected) {
        Intent intent = new Intent("android.net.wifi.supplicant.CONNECTION_CHANGE");
        intent.addFlags(67108864);
        intent.putExtra("connected", connected);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private boolean setNetworkDetailedState(DetailedState state) {
        boolean hidden = false;
        if (this.linkDebouncing || isRoaming()) {
            hidden = true;
        }
        if (DBG) {
            log("setDetailed state, old =" + this.mNetworkInfo.getDetailedState() + " and new state=" + state + " hidden=" + hidden);
        }
        if (!(this.mNetworkInfo.getExtraInfo() == null || this.mWifiInfo.getSSID() == null || this.mWifiInfo.getSSID().equals("<unknown ssid>") || this.mNetworkInfo.getExtraInfo().equals(this.mWifiInfo.getSSID()))) {
            if (DBG) {
                log("setDetailed state send new extra info" + this.mWifiInfo.getSSID());
            }
            this.mNetworkInfo.setExtraInfo(this.mWifiInfo.getSSID());
            sendNetworkStateChangeBroadcast(null);
        }
        if (hidden || state == this.mNetworkInfo.getDetailedState()) {
            return false;
        }
        this.mNetworkInfo.setDetailedState(state, null, this.mWifiInfo.getSSID());
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
        }
        sendNetworkStateChangeBroadcast(null);
        return true;
    }

    private DetailedState getNetworkDetailedState() {
        return this.mNetworkInfo.getDetailedState();
    }

    private SupplicantState handleSupplicantStateChange(Message message) {
        StateChangeResult stateChangeResult = message.obj;
        SupplicantState state = stateChangeResult.state;
        this.mWifiInfo.setSupplicantState(state);
        if ((stateChangeResult.wifiSsid == null || stateChangeResult.wifiSsid.toString().isEmpty()) && this.linkDebouncing) {
            return state;
        }
        if (SupplicantState.AUTHENTICATING == state || SupplicantState.ASSOCIATED == state) {
            fetchRssiLinkSpeedAndFrequencyNative();
        }
        if (SupplicantState.isConnecting(state)) {
            this.mWifiInfo.setNetworkId(stateChangeResult.networkId);
        } else {
            this.mWifiInfo.setNetworkId(-1);
        }
        this.mWifiInfo.setBSSID(stateChangeResult.BSSID);
        if (!(this.mWhiteListedSsids == null || this.mWhiteListedSsids.length <= 0 || stateChangeResult.wifiSsid == null)) {
            String SSID = stateChangeResult.wifiSsid.toString();
            String currentSSID = this.mWifiInfo.getSSID();
            if (!(SSID == null || currentSSID == null || SSID.equals("<unknown ssid>"))) {
                if (SSID.length() >= 2 && SSID.charAt(0) == '\"' && SSID.charAt(SSID.length() - 1) == '\"') {
                    SSID = SSID.substring(1, SSID.length() - 1);
                }
                if (currentSSID.length() >= 2 && currentSSID.charAt(0) == '\"' && currentSSID.charAt(currentSSID.length() - 1) == '\"') {
                    currentSSID = currentSSID.substring(1, currentSSID.length() - 1);
                }
                if (!SSID.equals(currentSSID) && getCurrentState() == this.mConnectedState) {
                    this.lastConnectAttemptTimestamp = System.currentTimeMillis();
                    this.targetWificonfiguration = this.mWifiConfigManager.getWifiConfiguration(this.mWifiInfo.getNetworkId());
                    transitionTo(this.mRoamingState);
                }
            }
        }
        this.mWifiInfo.setSSID(stateChangeResult.wifiSsid);
        this.mWifiInfo.setEphemeral(this.mWifiConfigManager.isEphemeral(this.mWifiInfo.getNetworkId()));
        if (!this.mWifiInfo.getMeteredHint()) {
            this.mWifiInfo.setMeteredHint(this.mWifiConfigManager.getMeteredHint(this.mWifiInfo.getNetworkId()));
        }
        if (this.mWiFiCHRManager != null) {
            this.mWiFiCHRManager.updateAPSsid(state, this.mWifiInfo);
        }
        this.mSupplicantStateTracker.sendMessage(Message.obtain(message));
        return state;
    }

    protected void handleNetworkDisconnect() {
        if (DBG) {
            log("handleNetworkDisconnect: Stopping DHCP and clearing IP stack:" + Thread.currentThread().getStackTrace()[2].getMethodName() + " - " + Thread.currentThread().getStackTrace()[3].getMethodName() + " - " + Thread.currentThread().getStackTrace()[4].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
        }
        stopRssiMonitoringOffload();
        clearCurrentConfigBSSID("handleNetworkDisconnect");
        stopIpManager();
        this.mWifiScoreReport = null;
        this.mWifiInfo.reset();
        this.linkDebouncing = false;
        this.mAutoRoaming = false;
        setNetworkDetailedState(DetailedState.DISCONNECTED);
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendNetworkInfo(this.mNetworkInfo);
            this.mNetworkAgent = null;
        }
        this.mWifiConfigManager.updateStatus(this.mLastNetworkId, DetailedState.DISCONNECTED);
        clearLinkProperties();
        sendNetworkStateChangeBroadcast(this.mLastBssid);
        autoRoamSetBSSID(this.mLastNetworkId, WifiLastResortWatchdog.BSSID_ANY);
        this.mLastBssid = null;
        registerDisconnected();
        this.mLastNetworkId = -1;
    }

    private void handleSupplicantConnectionLoss(boolean killSupplicant) {
        if (killSupplicant) {
            this.mWifiMonitor.killSupplicant(this.mP2pSupported);
        }
        this.mWifiNative.closeSupplicantConnection();
        sendSupplicantConnectionChangedBroadcast(false);
        setWifiState(1);
        if (this.mWiFiCHRManager != null) {
            this.mWiFiCHRManager.handleSupplicantException();
        }
    }

    void handlePreDhcpSetup() {
        this.mWifiNative.setBluetoothCoexistenceMode(1);
        setSuspendOptimizationsNative(1, false);
        this.mWifiNative.setPowerSave(false);
        getWifiLinkLayerStats(false);
        Message msg = new Message();
        msg.what = WifiP2pServiceImpl.BLOCK_DISCOVERY;
        msg.arg1 = 1;
        msg.arg2 = 196614;
        msg.obj = this;
        this.mWifiP2pChannel.sendMessage(msg);
    }

    void handlePostDhcpSetup() {
        setSuspendOptimizationsNative(1, true);
        this.mWifiNative.setPowerSave(true);
        this.mWifiP2pChannel.sendMessage(WifiP2pServiceImpl.BLOCK_DISCOVERY, 0);
        this.mWifiNative.setBluetoothCoexistenceMode(2);
    }

    private void reportConnectionAttemptEnd(int level2FailureCode, int connectivityFailureCode) {
        this.mWifiMetrics.endConnectionEvent(level2FailureCode, connectivityFailureCode);
        switch (level2FailureCode) {
            case 1:
            case 8:
                return;
            default:
                this.mWifiLogger.reportConnectionFailure();
                return;
        }
    }

    private void handleIPv4Success(DhcpResults dhcpResults) {
        Inet4Address addr;
        if (DBG) {
            logd("handleIPv4Success <" + dhcpResults.toString() + ">");
            logd("link address " + dhcpResults.ipAddress);
        }
        synchronized (this.mDhcpResultsLock) {
            this.mDhcpResults = dhcpResults;
            addr = (Inet4Address) dhcpResults.ipAddress.getAddress();
        }
        if (isRoaming() && this.mWifiInfo.getIpAddress() != NetworkUtils.inetAddressToInt(addr)) {
            logd("handleIPv4Success, roaming and address changed" + this.mWifiInfo + " got: " + addr);
        }
        this.mWifiInfo.setInetAddress(addr);
        if (!this.mWifiInfo.getMeteredHint()) {
            this.mWifiInfo.setMeteredHint(!dhcpResults.hasMeteredHint() ? hasMeteredHintForWi(addr) : true);
            updateCapabilities(getCurrentWifiConfiguration());
        }
        if (this.mWiFiCHRManager != null) {
            this.mWiFiCHRManager.handleIPv4SuccessException(addr);
        }
    }

    private void handleSuccessfulIpConfiguration() {
        this.mLastSignalLevel = -1;
        WifiConfiguration c = getCurrentWifiConfiguration();
        if (c != null) {
            c.getNetworkSelectionStatus().clearDisableReasonCounter(4);
            updateCapabilities(c);
        }
        if (c != null) {
            ScanResult result = getCurrentScanResult();
            if (result == null) {
                logd("WifiStateMachine: handleSuccessfulIpConfiguration and no scan results" + c.configKey());
                return;
            }
            result.numIpConfigFailures = 0;
            this.mWifiConfigManager.clearBssidBlacklist();
        }
    }

    private void handleIPv4Failure() {
        this.mWifiLogger.captureBugReportData(4);
        if (DBG) {
            int count = -1;
            WifiConfiguration config = getCurrentWifiConfiguration();
            if (config != null) {
                count = config.getNetworkSelectionStatus().getDisableReasonCounter(4);
            }
            log("DHCP failure count=" + count);
        }
        reportConnectionAttemptEnd(10, 2);
        synchronized (this.mDhcpResultsLock) {
            if (this.mDhcpResults != null) {
                this.mDhcpResults.clear();
            }
        }
        if (DBG) {
            logd("handleIPv4Failure");
        }
    }

    private void handleIpConfigurationLost() {
        this.mWifiInfo.setInetAddress(null);
        this.mWifiInfo.setMeteredHint(false);
        if (DBG) {
            loge("handleIpConfigurationLost: SSID = " + this.mWifiInfo.getSSID() + ", BSSID = " + this.mWifiInfo.getBSSID());
        }
        this.mWifiConfigManager.updateNetworkSelectionStatus(this.mLastNetworkId, 4);
        this.mWifiNative.disconnect();
    }

    private void handleIpReachabilityLost() {
        this.mWifiInfo.setInetAddress(null);
        this.mWifiInfo.setMeteredHint(false);
        this.mWifiNative.disconnect();
    }

    private int convertFrequencyToChannelNumber(int frequency) {
        if (frequency >= 2412 && frequency <= 2484) {
            return ((frequency - 2412) / 5) + 1;
        }
        if (frequency < 5170 || frequency > 5825) {
            return 0;
        }
        return ((frequency - 5170) / 5) + 34;
    }

    private int chooseApChannel(int apBand) {
        int apChannel;
        if (apBand == 0) {
            ArrayList<Integer> allowed2GChannel = this.mWifiApConfigStore.getAllowed2GChannel();
            if (allowed2GChannel == null || allowed2GChannel.size() == 0) {
                if (DBG) {
                    Log.d(TAG, "No specified 2G allowed channel list");
                }
                apChannel = 6;
            } else {
                apChannel = ((Integer) allowed2GChannel.get(mRandom.nextInt(allowed2GChannel.size()))).intValue();
            }
        } else {
            int[] channel = this.mWifiNative.getChannelsForBand(2);
            if (channel == null || channel.length <= 0) {
                Log.e(TAG, "SoftAp do not get available channel list");
                apChannel = 0;
            } else {
                apChannel = convertFrequencyToChannelNumber(channel[mRandom.nextInt(channel.length)]);
            }
        }
        if (DBG) {
            Log.d(TAG, "SoftAp set on channel " + apChannel);
        }
        return apChannel;
    }

    private boolean setupDriverForSoftAp() {
        if (this.mWifiNative.loadDriver()) {
            if (this.mWifiNative.queryInterfaceIndex(this.mInterfaceName) != -1) {
                if (!this.mWifiNative.setInterfaceUp(false)) {
                    Log.e(TAG, "toggleInterface failed");
                    return false;
                }
            } else if (DBG) {
                Log.d(TAG, "No interfaces to bring down");
            }
            try {
                this.mNwService.wifiFirmwareReload(this.mInterfaceName, "AP");
                if (DBG) {
                    Log.d(TAG, "Firmware reloaded in AP mode");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to reload AP firmware " + e);
            }
            if (!this.mWifiNative.startHal()) {
                Log.e(TAG, "Failed to start HAL");
            }
            return true;
        }
        Log.e(TAG, "Failed to load driver for softap");
        return false;
    }

    private byte[] macAddressFromString(String macString) {
        String[] macBytes = macString.split(":");
        if (macBytes.length != 6) {
            throw new IllegalArgumentException("MAC address should be 6 bytes long!");
        }
        byte[] mac = new byte[6];
        for (int i = 0; i < macBytes.length; i++) {
            mac[i] = Integer.valueOf(Integer.parseInt(macBytes[i], 16)).byteValue();
        }
        return mac;
    }

    private String macAddressFromRoute(String ipAddress) {
        Throwable th;
        String str = null;
        BufferedReader bufferedReader = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/net/arp"));
            try {
                String mac;
                String readLine = reader.readLine();
                while (true) {
                    readLine = reader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    String[] tokens = readLine.split("[ ]+");
                    if (tokens.length >= 6) {
                        String ip = tokens[0];
                        mac = tokens[3];
                        if (ipAddress.equals(ip)) {
                            break;
                        }
                    }
                }
                str = mac;
                if (str == null) {
                    loge("Did not find remoteAddress {" + ipAddress + "} in " + "/proc/net/arp");
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
                bufferedReader = reader;
            } catch (FileNotFoundException e2) {
                bufferedReader = reader;
                loge("Could not open /proc/net/arp to lookup mac address");
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e3) {
                    }
                }
                return str;
            } catch (IOException e4) {
                bufferedReader = reader;
                try {
                    loge("Could not read /proc/net/arp to lookup mac address");
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e5) {
                        }
                    }
                    return str;
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e6) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                bufferedReader = reader;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            loge("Could not open /proc/net/arp to lookup mac address");
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            return str;
        } catch (IOException e8) {
            loge("Could not read /proc/net/arp to lookup mac address");
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            return str;
        }
        return str;
    }

    void maybeRegisterNetworkFactory() {
        if (this.mNetworkFactory == null) {
            checkAndSetConnectivityInstance();
            if (this.mCm != null) {
                this.mNetworkFactory = new WifiNetworkFactory(getHandler().getLooper(), this.mContext, NETWORKTYPE, this.mNetworkCapabilitiesFilter);
                this.mNetworkFactory.setScoreFilter(60);
                this.mNetworkFactory.register();
                this.mUntrustedNetworkFactory = new UntrustedWifiNetworkFactory(getHandler().getLooper(), this.mContext, NETWORKTYPE_UNTRUSTED, this.mNetworkCapabilitiesFilter);
                this.mUntrustedNetworkFactory.setScoreFilter(Integer.MAX_VALUE);
                this.mUntrustedNetworkFactory.register();
            }
        }
    }

    String smToString(Message message) {
        return smToString(message.what);
    }

    String smToString(int what) {
        String s = (String) sSmToString.get(what);
        if (s != null) {
            return s;
        }
        switch (what) {
            case 69632:
                s = "AsyncChannel.CMD_CHANNEL_HALF_CONNECTED";
                break;
            case 69636:
                s = "AsyncChannel.CMD_CHANNEL_DISCONNECTED";
                break;
            case WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT /*143361*/:
                s = "GROUP_CREATING_TIMED_OUT";
                break;
            case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED /*143371*/:
                s = "P2P_CONNECTION_CHANGED";
                break;
            case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST /*143372*/:
                s = "WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST";
                break;
            case WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE /*143373*/:
                s = "P2P.DISCONNECT_WIFI_RESPONSE";
                break;
            case WifiP2pServiceImpl.SET_MIRACAST_MODE /*143374*/:
                s = "P2P.SET_MIRACAST_MODE";
                break;
            case WifiP2pServiceImpl.BLOCK_DISCOVERY /*143375*/:
                s = "P2P.BLOCK_DISCOVERY";
                break;
            case WifiMonitor.SUP_CONNECTION_EVENT /*147457*/:
                s = "SUP_CONNECTION_EVENT";
                break;
            case WifiMonitor.SUP_DISCONNECTION_EVENT /*147458*/:
                s = "SUP_DISCONNECTION_EVENT";
                break;
            case WifiMonitor.NETWORK_CONNECTION_EVENT /*147459*/:
                s = "NETWORK_CONNECTION_EVENT";
                break;
            case WifiMonitor.NETWORK_DISCONNECTION_EVENT /*147460*/:
                s = "NETWORK_DISCONNECTION_EVENT";
                break;
            case WifiMonitor.SCAN_RESULTS_EVENT /*147461*/:
                s = "SCAN_RESULTS_EVENT";
                break;
            case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                s = "SUPPLICANT_STATE_CHANGE_EVENT";
                break;
            case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                s = "AUTHENTICATION_FAILURE_EVENT";
                break;
            case WifiMonitor.WPS_SUCCESS_EVENT /*147464*/:
                s = "WPS_SUCCESS_EVENT";
                break;
            case WifiMonitor.WPS_FAIL_EVENT /*147465*/:
                s = "WPS_FAIL_EVENT";
                break;
            case WifiMonitor.DRIVER_HUNG_EVENT /*147468*/:
                s = "DRIVER_HUNG_EVENT";
                break;
            case WifiMonitor.SSID_TEMP_DISABLED /*147469*/:
                s = "SSID_TEMP_DISABLED";
                break;
            case WifiMonitor.SSID_REENABLED /*147470*/:
                s = "SSID_REENABLED";
                break;
            case WifiMonitor.SUP_REQUEST_IDENTITY /*147471*/:
                s = "SUP_REQUEST_IDENTITY";
                break;
            case WifiMonitor.SCAN_FAILED_EVENT /*147473*/:
                s = "SCAN_FAILED_EVENT";
                break;
            case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                s = "ASSOCIATION_REJECTION_EVENT";
                break;
            case WifiMonitor.ANQP_DONE_EVENT /*147500*/:
                s = "WifiMonitor.ANQP_DONE_EVENT";
                break;
            case WifiMonitor.GAS_QUERY_START_EVENT /*147507*/:
                s = "WifiMonitor.GAS_QUERY_START_EVENT";
                break;
            case WifiMonitor.GAS_QUERY_DONE_EVENT /*147508*/:
                s = "WifiMonitor.GAS_QUERY_DONE_EVENT";
                break;
            case WifiMonitor.RX_HS20_ANQP_ICON_EVENT /*147509*/:
                s = "WifiMonitor.RX_HS20_ANQP_ICON_EVENT";
                break;
            case WifiMonitor.HS20_REMEDIATION_EVENT /*147517*/:
                s = "WifiMonitor.HS20_REMEDIATION_EVENT";
                break;
            case 151553:
                s = "CONNECT_NETWORK";
                break;
            case 151556:
                s = "FORGET_NETWORK";
                break;
            case 151559:
                s = "SAVE_NETWORK";
                break;
            case 151562:
                s = "START_WPS";
                break;
            case 151563:
                s = "START_WPS_SUCCEEDED";
                break;
            case 151564:
                s = "WPS_FAILED";
                break;
            case 151565:
                s = "WPS_COMPLETED";
                break;
            case 151566:
                s = "CANCEL_WPS";
                break;
            case 151567:
                s = "CANCEL_WPS_FAILED";
                break;
            case 151568:
                s = "CANCEL_WPS_SUCCEDED";
                break;
            case 151569:
                s = "WifiManager.DISABLE_NETWORK";
                break;
            case 151572:
                s = "RSSI_PKTCNT_FETCH";
                break;
            default:
                s = "what:" + Integer.toString(what);
                break;
        }
        return s;
    }

    void registerConnected() {
        if (this.mLastNetworkId != -1) {
            WifiConfiguration config = this.mWifiConfigManager.getWifiConfiguration(this.mLastNetworkId);
            if (config != null) {
                config.lastConnected = System.currentTimeMillis();
                config.numAssociation++;
                NetworkSelectionStatus networkSelectionStatus = config.getNetworkSelectionStatus();
                networkSelectionStatus.clearDisableReasonCounter();
                networkSelectionStatus.setHasEverConnected(true);
            }
            this.mWifiScoreReport = null;
        }
    }

    void registerDisconnected() {
        if (this.mLastNetworkId != -1) {
            WifiConfiguration config = this.mWifiConfigManager.getWifiConfiguration(this.mLastNetworkId);
            if (config != null) {
                config.lastDisconnected = System.currentTimeMillis();
                if (config.ephemeral) {
                    this.mWifiConfigManager.forgetNetwork(this.mLastNetworkId);
                }
            }
        }
    }

    void noteWifiDisabledWhileAssociated() {
        int rssi = this.mWifiInfo.getRssi();
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (getCurrentState() == this.mConnectedState && rssi != -127 && config != null) {
            boolean is24GHz = this.mWifiInfo.is24GHz();
            boolean isBadRSSI = (!is24GHz || rssi >= this.mWifiConfigManager.mThresholdMinimumRssi24.get()) ? !is24GHz && rssi < this.mWifiConfigManager.mThresholdMinimumRssi5.get() : true;
            boolean isLowRSSI = (!is24GHz || rssi >= this.mWifiConfigManager.mThresholdQualifiedRssi24.get()) ? !is24GHz && this.mWifiInfo.getRssi() < this.mWifiConfigManager.mThresholdQualifiedRssi5.get() : true;
            boolean isHighRSSI = (!is24GHz || rssi < this.mWifiConfigManager.mThresholdSaturatedRssi24.get()) ? !is24GHz && this.mWifiInfo.getRssi() >= this.mWifiConfigManager.mThresholdSaturatedRssi5.get() : true;
            if (isBadRSSI) {
                config.numUserTriggeredWifiDisableLowRSSI++;
            } else if (isLowRSSI) {
                config.numUserTriggeredWifiDisableBadRSSI++;
            } else if (!isHighRSSI) {
                config.numUserTriggeredWifiDisableNotHighRSSI++;
            }
        }
    }

    public WifiConfiguration getCurrentWifiConfiguration() {
        if (this.mLastNetworkId == -1) {
            return null;
        }
        return this.mWifiConfigManager.getWifiConfiguration(this.mLastNetworkId);
    }

    ScanResult getCurrentScanResult() {
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config == null) {
            return null;
        }
        String BSSID = this.mWifiInfo.getBSSID();
        if (BSSID == null) {
            BSSID = this.mTargetRoamBSSID;
        }
        ScanDetailCache scanDetailCache = this.mWifiConfigManager.getScanDetailCache(config);
        if (scanDetailCache == null) {
            return null;
        }
        return scanDetailCache.get(BSSID);
    }

    String getCurrentBSSID() {
        if (this.linkDebouncing) {
            return null;
        }
        return this.mLastBssid;
    }

    private void updateCapabilities(WifiConfiguration config) {
        NetworkCapabilities networkCapabilities = new NetworkCapabilities(this.mDfltNetworkCapabilities);
        if (config != null) {
            int rssi;
            if (config.ephemeral) {
                networkCapabilities.removeCapability(14);
            } else {
                networkCapabilities.addCapability(14);
            }
            if (this.mWifiInfo.getRssi() != -127) {
                rssi = this.mWifiInfo.getRssi();
            } else {
                rssi = Integer.MIN_VALUE;
            }
            networkCapabilities.setSignalStrength(rssi);
        }
        if (this.mWifiInfo.getMeteredHint()) {
            networkCapabilities.removeCapability(11);
        }
        if (this.mNetworkAgent != null) {
            this.mNetworkAgent.sendNetworkCapabilities(networkCapabilities);
        }
    }

    void unwantedNetwork(int reason) {
        sendMessage(CMD_UNWANTED_NETWORK, reason);
    }

    void doNetworkStatus(int status) {
        sendMessage(CMD_NETWORK_STATUS, status);
    }

    private String buildIdentity(int eapMethod, String imsi, String mccMnc) {
        if (imsi == null || imsi.isEmpty()) {
            return "";
        }
        String prefix;
        String mcc;
        String mnc;
        if (eapMethod == 4) {
            prefix = "1";
        } else if (eapMethod == 5) {
            prefix = HwWifiCHRStateManager.TYPE_AP_VENDOR;
        } else if (eapMethod != 6) {
            return "";
        } else {
            prefix = "6";
        }
        if (mccMnc == null || mccMnc.isEmpty()) {
            mcc = imsi.substring(0, 3);
            mnc = imsi.substring(3, 6);
        } else {
            mcc = mccMnc.substring(0, 3);
            mnc = mccMnc.substring(3);
            if (mnc.length() == 2) {
                mnc = HwWifiCHRStateManager.TYPE_AP_VENDOR + mnc;
            }
        }
        return prefix + imsi + "@wlan.mnc" + mnc + ".mcc" + mcc + ".3gppnetwork.org";
    }

    boolean startScanForConfiguration(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        ScanDetailCache scanDetailCache = this.mWifiConfigManager.getScanDetailCache(config);
        if (scanDetailCache == null || !config.allowedKeyManagement.get(1) || scanDetailCache.size() > 6) {
            return true;
        }
        HashSet<Integer> freqs = this.mWifiConfigManager.makeChannelList(config, ONE_HOUR_MILLI);
        if (freqs == null || freqs.size() == 0) {
            if (DBG) {
                logd("no channels for " + config.configKey());
            }
            return false;
        }
        logd("starting scan for " + config.configKey() + " with " + freqs);
        Set<Integer> hiddenNetworkIds = new HashSet();
        if (config.hiddenSSID) {
            hiddenNetworkIds.add(Integer.valueOf(config.networkId));
        }
        if (startScanNative(freqs, hiddenNetworkIds, WIFI_WORK_SOURCE)) {
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_OK;
        } else {
            this.messageHandlingStatus = MESSAGE_HANDLING_STATUS_HANDLING_ERROR;
        }
        return true;
    }

    void clearCurrentConfigBSSID(String dbg) {
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config != null) {
            clearConfigBSSID(config, dbg);
        }
    }

    void clearConfigBSSID(WifiConfiguration config, String dbg) {
        if (config != null) {
            if (DBG) {
                logd(dbg + " " + this.mTargetRoamBSSID + " config " + config.configKey() + " config.NetworkSelectionStatus.mNetworkSelectionBSSID " + config.getNetworkSelectionStatus().getNetworkSelectionBSSID());
            }
            if (DBG) {
                logd(dbg + " " + config.SSID + " nid=" + Integer.toString(config.networkId));
            }
            this.mWifiConfigManager.saveWifiConfigBSSID(config, WifiLastResortWatchdog.BSSID_ANY);
        }
    }

    private void sendConnectedState() {
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (this.mWifiConfigManager.isLastSelectedConfiguration(config)) {
            boolean prompt = this.mWifiConfigManager.checkConfigOverridePermission(config.lastConnectUid);
            if (DBG) {
                log("Network selected by UID " + config.lastConnectUid + " prompt=" + prompt);
            }
            if (prompt) {
                if (DBG) {
                    log("explictlySelected acceptUnvalidated=" + config.noInternetAccessExpected);
                }
                this.mNetworkAgent.explicitlySelected(config.noInternetAccessExpected);
            }
        }
        setNetworkDetailedState(DetailedState.CONNECTED);
        this.mWifiConfigManager.updateStatus(this.mLastNetworkId, DetailedState.CONNECTED);
        sendNetworkStateChangeBroadcast(this.mLastBssid);
        if (this.mWifiStatStore != null) {
            this.mWifiStatStore.updateConnectState(true);
        }
        if (this.mHwWifiCHRService != null) {
            this.mHwWifiCHRService.updateConnectStateByConfig(getCurrentWifiConfiguration());
        }
    }

    public void triggerUpdateAPInfo() {
        Log.d(TAG, "triggerUpdateAPInfo");
    }

    private void sendHilinkConnectedBroadcast() {
        long currentTime = SystemClock.elapsedRealtime();
        if (this.mHilinkLastConnectedTime == 0 || currentTime - this.mHilinkLastConnectedTime >= 300000) {
            Log.d(TAG, "Hilink sendHilinkConnectedBroadcast");
            Intent intent = new Intent(HILINK_STATE_CHANGE_ACTION);
            intent.putExtra("networkInfo", new NetworkInfo(this.mNetworkInfo));
            intent.putExtra("linkProperties", new LinkProperties(this.mLinkProperties));
            WifiInfo sentWifiInfo = new WifiInfo(this.mWifiInfo);
            sentWifiInfo.setMacAddress("02:00:00:00:00:00");
            intent.putExtra("wifiInfo", sentWifiInfo);
            intent.putExtra("TYPE", "NETWORK_CONNECTED");
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            this.mHilinkLastConnectedTime = currentTime;
        }
    }

    private void sendHilinkdisConnectedBroadcast() {
        long currentTime = SystemClock.elapsedRealtime();
        if (this.mHilinkLastDisconnectedTime == 0 || currentTime - this.mHilinkLastDisconnectedTime >= 300000) {
            Log.d(TAG, "Hilink sendHilinkdisConnectedBroadcast");
            Intent intent = new Intent(HILINK_STATE_CHANGE_ACTION);
            intent.putExtra("TYPE", "NETWORK_DIS_CONNECTED");
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            this.mHilinkLastDisconnectedTime = currentTime;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void sendHilinkscanResultBroadcast() {
        long currentHilinkHashCode = 0;
        long currentLevelHilinkHashCode = 0;
        synchronized (this.mScanResultsLock) {
            if (this.mScanResults == null) {
                return;
            }
            for (ScanDetail scanDetail : this.mScanResults) {
                if (scanDetail != null) {
                    ScanResult item = scanDetail.getScanResult();
                    if (item != null && item.SSID.length() == 32 && item.SSID.startsWith("Hi")) {
                        int itemHashCode = item.SSID.hashCode();
                        if (itemHashCode < 0) {
                            itemHashCode = -itemHashCode;
                        }
                        currentHilinkHashCode += (long) itemHashCode;
                        if (item.level >= -45) {
                            currentLevelHilinkHashCode += (long) itemHashCode;
                        }
                    }
                }
            }
        }
    }

    private void replyToMessage(Message msg, int what) {
        if (msg.replyTo != null) {
            this.mReplyChannel.replyToMessage(msg, obtainMessageWithWhatAndArg2(msg, what));
        }
    }

    private void replyToMessage(Message msg, int what, int arg1) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessageWithWhatAndArg2(msg, what);
            dstMsg.arg1 = arg1;
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }

    private void replyToMessage(Message msg, int what, Object obj) {
        if (msg.replyTo != null) {
            Message dstMsg = obtainMessageWithWhatAndArg2(msg, what);
            dstMsg.obj = obj;
            this.mReplyChannel.replyToMessage(msg, dstMsg);
        }
    }

    private Message obtainMessageWithWhatAndArg2(Message srcMsg, int what) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg2 = srcMsg.arg2;
        return msg;
    }

    private void broadcastWifiCredentialChanged(int wifiCredentialEventType, WifiConfiguration config) {
        if (config != null && config.preSharedKey != null) {
            Intent intent = new Intent("android.net.wifi.WIFI_CREDENTIAL_CHANGED");
            intent.putExtra("ssid", config.SSID);
            intent.putExtra("et", wifiCredentialEventType);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, "android.permission.RECEIVE_WIFI_CREDENTIAL_CHANGE");
        }
    }

    private static int parseHex(char ch) {
        if ('0' <= ch && ch <= '9') {
            return ch - 48;
        }
        if ('a' <= ch && ch <= 'f') {
            return (ch - 97) + 10;
        }
        if ('A' <= ch && ch <= 'F') {
            return (ch - 65) + 10;
        }
        throw new NumberFormatException("" + ch + " is not a valid hex digit");
    }

    private byte[] parseHex(String hex) {
        if (hex == null) {
            return new byte[0];
        }
        if (hex.length() % 2 != 0) {
            throw new NumberFormatException(hex + " is not a valid hex string");
        }
        byte[] result = new byte[((hex.length() / 2) + 1)];
        result[0] = (byte) (hex.length() / 2);
        int i = 0;
        int j = 1;
        while (i < hex.length()) {
            result[j] = (byte) (((parseHex(hex.charAt(i)) * 16) + parseHex(hex.charAt(i + 1))) & 255);
            i += 2;
            j++;
        }
        return result;
    }

    private static String makeHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02x", new Object[]{Byte.valueOf(bytes[i])}));
        }
        return sb.toString();
    }

    private static String makeHex(byte[] bytes, int from, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(String.format("%02x", new Object[]{Byte.valueOf(bytes[from + i])}));
        }
        return sb.toString();
    }

    private static byte[] concat(byte[] array1, byte[] array2, byte[] array3) {
        int length;
        int i = 0;
        int len = (array1.length + array2.length) + array3.length;
        if (array1.length != 0) {
            len++;
        }
        if (array2.length != 0) {
            len++;
        }
        if (array3.length != 0) {
            len++;
        }
        byte[] result = new byte[len];
        int index = 0;
        if (array1.length != 0) {
            result[0] = (byte) (array1.length & 255);
            index = 1;
            for (byte b : array1) {
                result[index] = b;
                index++;
            }
        }
        if (array2.length != 0) {
            result[index] = (byte) (array2.length & 255);
            index++;
            for (byte b2 : array2) {
                result[index] = b2;
                index++;
            }
        }
        if (array3.length != 0) {
            result[index] = (byte) (array3.length & 255);
            index++;
            length = array3.length;
            while (i < length) {
                result[index] = array3[i];
                index++;
                i++;
            }
        }
        return result;
    }

    private static byte[] concatHex(byte[] array1, byte[] array2) {
        int length;
        int i = 0;
        byte[] result = new byte[(array1.length + array2.length)];
        int index = 0;
        if (array1.length != 0) {
            for (byte b : array1) {
                result[index] = b;
                index++;
            }
        }
        if (array2.length != 0) {
            length = array2.length;
            while (i < length) {
                result[index] = array2[i];
                index++;
                i++;
            }
        }
        return result;
    }

    String getGsmSimAuthResponse(String[] requestData, TelephonyManager tm) {
        StringBuilder sb = new StringBuilder();
        for (String challenge : requestData) {
            if (!(challenge == null || challenge.isEmpty())) {
                logd("RAND = " + challenge);
                try {
                    String base64Challenge = Base64.encodeToString(parseHex(challenge), 2);
                    String tmResponse = tm.getIccAuthentication(2, 128, base64Challenge);
                    if (tmResponse == null) {
                        tmResponse = tm.getIccAuthentication(1, 128, base64Challenge);
                    }
                    logv("Raw Response - " + tmResponse);
                    if (tmResponse == null || tmResponse.length() <= 4) {
                        loge("bad response - " + tmResponse);
                        return null;
                    }
                    byte[] result = Base64.decode(tmResponse, 0);
                    logv("Hex Response -" + makeHex(result));
                    int sres_len = result[0];
                    if (sres_len >= result.length) {
                        loge("malfomed response - " + tmResponse);
                        return null;
                    }
                    String sres = makeHex(result, 1, sres_len);
                    int kc_offset = sres_len + 1;
                    if (kc_offset >= result.length) {
                        loge("malfomed response - " + tmResponse);
                        return null;
                    }
                    int kc_len = result[kc_offset];
                    if (kc_offset + kc_len > result.length) {
                        loge("malfomed response - " + tmResponse);
                        return null;
                    }
                    String kc = makeHex(result, kc_offset + 1, kc_len);
                    sb.append(":").append(kc).append(":").append(sres);
                    logv("kc:" + kc + " sres:" + sres);
                } catch (NumberFormatException e) {
                    loge("malformed challenge");
                }
            }
        }
        return sb.toString();
    }

    void handleGsmAuthRequest(SimAuthRequestData requestData) {
        if (this.targetWificonfiguration == null || this.targetWificonfiguration.networkId == requestData.networkId) {
            logd("id matches targetWifiConfiguration");
            TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
            if (tm == null) {
                loge("could not get telephony manager");
                this.mWifiNative.simAuthFailedResponse(requestData.networkId);
                return;
            }
            String response = getGsmSimAuthResponse(requestData.data, tm);
            if (response == null) {
                this.mWifiNative.simAuthFailedResponse(requestData.networkId);
            } else {
                logv("Supplicant Response -" + response);
                this.mWifiNative.simAuthResponse(requestData.networkId, "GSM-AUTH", response);
            }
            return;
        }
        logd("id does not match targetWifiConfiguration");
    }

    void handle3GAuthRequest(SimAuthRequestData requestData) {
        StringBuilder sb = new StringBuilder();
        byte[] rand = null;
        byte[] authn = null;
        String res_type = "UMTS-AUTH";
        if (this.targetWificonfiguration == null || this.targetWificonfiguration.networkId == requestData.networkId) {
            logd("id matches targetWifiConfiguration");
            if (requestData.data.length == 2) {
                try {
                    rand = parseHex(requestData.data[0]);
                    authn = parseHex(requestData.data[1]);
                } catch (NumberFormatException e) {
                    loge("malformed challenge");
                }
            } else {
                loge("malformed challenge");
            }
            String tmResponse = "";
            if (!(rand == null || authn == null)) {
                String base64Challenge = Base64.encodeToString(concatHex(rand, authn), 2);
                TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
                if (tm != null) {
                    tmResponse = tm.getIccAuthentication(2, 129, base64Challenge);
                    logv("Raw Response - " + tmResponse);
                } else {
                    loge("could not get telephony manager");
                }
            }
            boolean good_response = false;
            if (tmResponse == null || tmResponse.length() <= 4) {
                loge("bad response - " + tmResponse);
            } else {
                byte[] result = Base64.decode(tmResponse, 0);
                loge("Hex Response - " + makeHex(result));
                byte tag = result[0];
                if (tag == (byte) -37) {
                    logv("successful 3G authentication ");
                    int res_len = result[1];
                    String res = makeHex(result, 2, res_len);
                    int ck_len = result[res_len + 2];
                    String ck = makeHex(result, res_len + 3, ck_len);
                    String ik = makeHex(result, (res_len + ck_len) + 4, result[(res_len + ck_len) + 3]);
                    sb.append(":").append(ik).append(":").append(ck).append(":").append(res);
                    logv("ik:" + ik + "ck:" + ck + " res:" + res);
                    good_response = true;
                } else if (tag == (byte) -36) {
                    loge("synchronisation failure");
                    String auts = makeHex(result, 2, result[1]);
                    res_type = "UMTS-AUTS";
                    sb.append(":").append(auts);
                    logv("auts:" + auts);
                    good_response = true;
                } else {
                    loge("bad response - unknown tag = " + tag);
                }
            }
            if (good_response) {
                String response = sb.toString();
                logv("Supplicant Response -" + response);
                this.mWifiNative.simAuthResponse(requestData.networkId, res_type, response);
            } else {
                this.mWifiNative.umtsAuthFailedResponse(requestData.networkId);
            }
            return;
        }
        logd("id does not match targetWifiConfiguration");
    }

    public void autoConnectToNetwork(int networkId, String bssid) {
        sendMessage(CMD_AUTO_CONNECT, networkId, 0, bssid);
    }

    public void autoRoamToNetwork(int networkId, ScanResult scanResult) {
        sendMessage(CMD_AUTO_ROAM, networkId, 0, scanResult);
    }

    public void enableWifiConnectivityManager(boolean enabled) {
        sendMessage(CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER, enabled ? 1 : 0);
    }

    static boolean unexpectedDisconnectedReason(int reason) {
        if (reason == 2 || reason == 6 || reason == 7 || reason == 8 || reason == 9 || reason == 14 || reason == 15 || reason == 16 || reason == 18 || reason == 19 || reason == 23 || reason == 34) {
            return true;
        }
        return false;
    }

    void updateWifiMetrics() {
        int numSavedNetworks = this.mWifiConfigManager.getConfiguredNetworksSize();
        int numOpenNetworks = 0;
        int numPersonalNetworks = 0;
        int numEnterpriseNetworks = 0;
        int numNetworksAddedByUser = 0;
        int numNetworksAddedByApps = 0;
        for (WifiConfiguration config : this.mWifiConfigManager.getSavedNetworks()) {
            if (config.allowedKeyManagement.get(0)) {
                numOpenNetworks++;
            } else if (config.isEnterprise()) {
                numEnterpriseNetworks++;
            } else {
                numPersonalNetworks++;
            }
            if (config.selfAdded) {
                numNetworksAddedByUser++;
            } else {
                numNetworksAddedByApps++;
            }
        }
        this.mWifiMetrics.setNumSavedNetworks(numSavedNetworks);
        this.mWifiMetrics.setNumOpenNetworks(numOpenNetworks);
        this.mWifiMetrics.setNumPersonalNetworks(numPersonalNetworks);
        this.mWifiMetrics.setNumEnterpriseNetworks(numEnterpriseNetworks);
        this.mWifiMetrics.setNumNetworksAddedByUser(numNetworksAddedByUser);
        this.mWifiMetrics.setNumNetworksAddedByApps(numNetworksAddedByApps);
    }

    private static String getLinkPropertiesSummary(LinkProperties lp) {
        List<String> attributes = new ArrayList(6);
        if (lp.hasIPv4Address()) {
            attributes.add("v4");
        }
        if (lp.hasIPv4DefaultRoute()) {
            attributes.add("v4r");
        }
        if (lp.hasIPv4DnsServer()) {
            attributes.add("v4dns");
        }
        if (lp.hasGlobalIPv6Address()) {
            attributes.add("v6");
        }
        if (lp.hasIPv6DefaultRoute()) {
            attributes.add("v6r");
        }
        if (lp.hasIPv6DnsServer()) {
            attributes.add("v6dns");
        }
        return TextUtils.join(" ", attributes);
    }

    private void wnmFrameReceived(WnmData event) {
        Intent intent = new Intent("android.net.wifi.PASSPOINT_WNM_FRAME_RECEIVED");
        intent.addFlags(67108864);
        intent.putExtra("bssid", event.getBssid());
        intent.putExtra("url", event.getUrl());
        if (event.isDeauthEvent()) {
            intent.putExtra("ess", event.isEss());
            intent.putExtra("delay", event.getDelay());
        } else {
            intent.putExtra("method", event.getMethod());
            WifiConfiguration config = getCurrentWifiConfiguration();
            if (!(config == null || config.FQDN == null)) {
                intent.putExtra("match", this.mWifiConfigManager.matchProviderWithCurrentNetwork(config.FQDN));
            }
        }
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private String getTargetSsid() {
        WifiConfiguration currentConfig = this.mWifiConfigManager.getWifiConfiguration(this.mTargetNetworkId);
        if (currentConfig != null) {
            return currentConfig.SSID;
        }
        return null;
    }

    public void updateWifiBackgroudStatus(int msgType) {
    }

    public WifiConfigManager getWifiConfigManager() {
        return null;
    }

    public void notifyWifiScanResultsAvailable(boolean success) {
    }

    public void notifyWifiRoamingStarted() {
    }

    public void notifyWifiRoamingCompleted(String newBssid) {
    }

    public void requestUpdateDnsServers(ArrayList<String> arrayList) {
    }

    public void sendUpdateDnsServersRequest(Message message, LinkProperties lp) {
    }

    public void requestRenewDhcp() {
    }

    public void setForceDhcpDiscovery(IpManager ipManager) {
    }

    public void resetIpConfigStatus() {
    }

    public void notifyIpConfigCompleted() {
    }

    public boolean isRenewDhcpSelfCuring() {
        return false;
    }

    public void requestUseStaticIpConfig(StaticIpConfiguration staticIpConfig) {
    }

    public void handleStaticIpConfig(IpManager ipManager, WifiNative wifiNative, StaticIpConfiguration config) {
    }

    public boolean isWifiSelfCuring() {
        return false;
    }

    public void exitWifiSelfCure() {
    }

    public void notifySelfCureComplete(boolean succ) {
    }

    public void startSelfCureWifiReset() {
    }

    public void startSelfCureWifiReassoc() {
    }

    public boolean checkSelfCureWifiResult() {
        return false;
    }

    public void requestResetWifi() {
    }

    public void requestReassocLink() {
    }

    public void stopSelfCureWifi(int status) {
    }

    public void stopSelfCureDelay(int status, int delay) {
    }

    public void setWifiBackgroundStatus(boolean background) {
    }

    public int getSelfCureNetworkId() {
        return -1;
    }

    public boolean notifyIpConfigLostAndFixedBySce(WifiConfiguration config) {
        return false;
    }

    public boolean isBssidDisabled(String bssid) {
        return false;
    }

    public boolean isWiFiProSwitchOnGoing() {
        return false;
    }

    public void handleAntenaPreempted() {
    }

    public boolean isWlanSettingsActivity() {
        return false;
    }

    public void handleDualbandHandoverFailed(int disableReason) {
    }

    public void updateCHRDNS(List<InetAddress> list) {
    }

    public void setWiFiProRoamingSSID(WifiSsid SSID) {
    }

    public WifiSsid getWiFiProRoamingSSID() {
        return null;
    }

    public boolean isEnterpriseHotspot(WifiConfiguration config) {
        return false;
    }
}
