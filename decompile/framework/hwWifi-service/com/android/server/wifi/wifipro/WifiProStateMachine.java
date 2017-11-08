package com.android.server.wifi.wifipro;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManagerPolicy;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.policy.AbsPhoneWindowManager;
import com.android.server.wifi.HwSelfCureEngine;
import com.android.server.wifi.HwSelfCureUtils;
import com.android.server.wifi.HwWifiCHRConstImpl;
import com.android.server.wifi.wifipro.hwintelligencewifi.HwIntelligenceWiFiManager;
import com.android.server.wifipro.WifiProCHRManager;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.List;

public class WifiProStateMachine extends StateMachine implements INetworkQosCallBack, INetworksHandoverCallBack, IWifiProUICallBack, IDualBandManagerCallback {
    private static final int ACCESS_TYPE = 1;
    private static final boolean AUTO_EVALUATE_SWITCH = false;
    private static final int BASE = 136168;
    private static final boolean BQE_TEST = false;
    private static final int CHR_AVAILIABLE_AP_COUNTER = 2;
    private static final int CMD_UPDATE_WIFIPRO_CONFIGURATIONS = 131672;
    private static final int CSP_INVISIBILITY = 0;
    private static final int CSP_VISIBILITY = 1;
    private static final boolean DBG = true;
    private static final boolean DDBG = false;
    private static final boolean DEFAULT_WIFI_PRO_ENABLED = false;
    private static final int DELAY_EVALUTE_NEXT_AP_TIME = 2000;
    private static int DUALBAND_CHR_HANDOVER_THRESHOLD = 60000;
    private static int DUALBAND_CHR_PINGPANG_THRESHOLD = 900000;
    private static int DUALBAND_CHR_VERIFY_THRESHOLD = 120000;
    private static final int DUALBAND_HANDOVER_FAILED_COUNT = 2;
    private static final int DUALBAND_HANDOVER_INBLACK_LIST_COUNT = 4;
    private static final int DUALBAND_HANDOVER_SCORE_NOT_SATISFY_COUNT = 3;
    private static final int DUALBAND_HANDOVER_SUC_COUNT = 1;
    private static int DUALBAND_TYPE_MIX = 2;
    private static int DUALBAND_TYPE_SINGLE = 1;
    private static int DUALBAND_TYPE_UNKONW = 0;
    private static final int EVALUATE_ALL_TIMEOUT = 75000;
    private static final int EVALUATE_VALIDITY_TIMEOUT = 120000;
    private static final int EVALUATE_WIFI_CONNECTED_TIMEOUT = 35000;
    private static final int EVALUATE_WIFI_RTT_BQE_INTERVAL = 3000;
    private static final int EVENT_CHECK_AVAILABLE_AP_RESULT = 136176;
    private static final int EVENT_CHECK_MOBILE_QOS_RESULT = 136180;
    private static final int EVENT_CHECK_WIFI_INTERNET = 136192;
    private static final int EVENT_CHECK_WIFI_INTERNET_RESULT = 136181;
    private static final int EVENT_CONFIGURED_NETWORKS_CHANGED = 136308;
    public static final int EVENT_DELAY_EVALUTE_NEXT_AP = 136314;
    private static final int EVENT_DELAY_REINITIALIZE_WIFI_MONITOR = 136184;
    private static final int EVENT_DEVICE_SCREEN_ON = 136170;
    private static final int EVENT_DIALOG_CANCEL = 136183;
    private static final int EVENT_DIALOG_OK = 136182;
    private static final int EVENT_DUALBAND_5GAP_AVAILABLE = 136370;
    private static final int EVENT_DUALBAND_DELAY_RETRY = 136372;
    private static final int EVENT_DUALBAND_RSSITH_RESULT = 136368;
    private static final int EVENT_DUALBAND_SCORE_RESULT = 136369;
    private static final int EVENT_DUALBAND_WIFI_HANDOVER_RESULT = 136371;
    private static final int EVENT_EMUI_CSP_SETTINGS_CHANGE = 136190;
    private static final int EVENT_EVALUATE_COMPLETE = 136295;
    private static final int EVENT_EVALUATE_INITIATE = 136294;
    private static final int EVENT_EVALUATE_START_CHECK_INTERNET = 136307;
    private static final int EVENT_EVALUATE_VALIDITY_TIMEOUT = 136296;
    private static final int EVENT_EVALUTE_ABANDON = 136305;
    private static final int EVENT_EVALUTE_STOP = 136303;
    private static final int EVENT_EVALUTE_TIMEOUT = 136304;
    private static final int EVENT_GET_WIFI_TCPRX = 136311;
    private static final int EVENT_HTTP_REACHABLE_RESULT = 136195;
    private static final int EVENT_LAST_EVALUTE_VALID = 136302;
    private static final int EVENT_MOBILE_CONNECTIVITY = 136175;
    private static final int EVENT_MOBILE_DATA_STATE_CHANGED_ACTION = 136186;
    private static final int EVENT_MOBILE_QOS_CHANGE = 136173;
    private static final int EVENT_MOBILE_RECOVERY_TO_WIFI = 136189;
    private static final int EVENT_MOBILE_SWITCH_DELAY = 136194;
    private static final int EVENT_NETWORK_CONNECTIVITY_CHANGE = 136177;
    private static final int EVENT_RETRY_WIFI_TO_WIFI = 136191;
    private static final int EVENT_SCAN_RESULTS_AVAILABLE = 136293;
    private static final int EVENT_START_BQE = 136306;
    private static final int EVENT_SUPPLICANT_STATE_CHANGE = 136297;
    private static final int EVENT_USER_ROVE_IN = 136193;
    private static final int EVENT_WIFIPRO_EVALUTE_STATE_CHANGE = 136298;
    private static final int EVENT_WIFIPRO_WORKING_STATE_CHANGE = 136171;
    private static final int EVENT_WIFI_CHECK_UNKOWN = 136309;
    private static final int EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT = 136301;
    private static final int EVENT_WIFI_EVALUTE_TCPRTT_RESULT = 136299;
    private static final int EVENT_WIFI_GOOD_INTERVAL_TIMEOUT = 136187;
    private static final int EVENT_WIFI_HANDOVER_WIFI_RESULT = 136178;
    private static final int EVENT_WIFI_NETWORK_STATE_CHANGE = 136169;
    private static final int EVENT_WIFI_P2P_CONNECTION_CHANGED = 136310;
    private static final int EVENT_WIFI_QOS_CHANGE = 136172;
    private static final int EVENT_WIFI_RECOVERY_TIMEOUT = 136188;
    private static final int EVENT_WIFI_RSSI_CHANGE = 136179;
    private static final int EVENT_WIFI_SECURITY_QUERY_TIMEOUT = 136313;
    private static final int EVENT_WIFI_SECURITY_RESPONSE = 136312;
    private static final int EVENT_WIFI_SEMIAUTO_EVALUTE_CHANGE = 136300;
    private static final int EVENT_WIFI_STATE_CHANGED_ACTION = 136185;
    private static final int GOOD_LINK_DETECTED = 131874;
    public static final int HANDOVER_5G_DIFFERENCE_SCORE = 5;
    private static final int HANDOVER_5G_DIRECTLY_RSSI = -70;
    public static final int HANDOVER_5G_DIRECTLY_SCORE = 40;
    public static final int HANDOVER_5G_MAX_RSSI = -45;
    public static final int HANDOVER_5G_SINGLE_RSSI = -55;
    private static final int HANDOVER_MIN_LEVEL_INTERVAL = 2;
    private static final String HUAWEI_SETTINGS = "com.android.settings.Settings$WifiSettingsActivity";
    private static final String ILLEGAL_BSSID_01 = "any";
    private static final String ILLEGAL_BSSID_02 = "00:00:00:00:00:00";
    private static final int INVALID_CHR_RCD_TIME = 0;
    private static final int INVALID_PID = -1;
    private static final int JUDGE_WIFI_FAST_REOPEN_TIME = 30000;
    private static final String KEY_EMUI_WIFI_TO_PDP = "wifi_to_pdp";
    private static final int KEY_MOBILE_HANDOVER_WIFI = 2;
    private static final String KEY_SMART_NETWORK_SWITCHING = "smart_network_switching";
    private static final String KEY_WIFIPRO_MANUAL_CONNECT = "wifipro_manual_connect_ap";
    private static final String KEY_WIFIPRO_RECOMMENDING_ACCESS_POINTS = "wifipro_recommending_access_points";
    private static final String KEY_WIFIPRO_RECOMMEND_NETWORK = "wifipro_auto_recommend";
    private static final String KEY_WIFIPRO_RECOMMEND_NETWORK_SAVED_STATE = "wifipro_auto_recommend_saved_state";
    private static final int KEY_WIFI_HANDOVER_MOBILE = 1;
    private static final String MAPS_LOCATION_FLAG = "hw_higeo_maps_location";
    private static final int MILLISECONDS_OF_ONE_SECOND = 1000;
    private static final int MOBILE = 0;
    private static final int MOBILE_DATA_OFF_SWITCH_DELAY_MS = 3000;
    private static final int NETWORK_POOR_LEVEL_THRESHOLD = 2;
    private static final int OOBE_COMPLETE = 1;
    private static final int POOR_LINK_DETECTED = 131873;
    private static final int PORTAL_CHECK_MAX_TIMERS = 20;
    private static final int PORTAL_HANDOVER_DELAY_TIME = 15000;
    private static final int QOS_LEVEL = 2;
    private static final int QOS_SCORE = 3;
    private static final int SEND_MESSAGE_DELAY_TIME = 30000;
    private static final String SETTING_SECURE_CONN_WIFI_PID = "wifipro_connect_wifi_app_pid";
    private static final String SETTING_SECURE_VPN_WORK_VALUE = "wifipro_network_vpn_state";
    private static final String SETTING_SECURE_WIFI_NO_INT = "wifi_no_internet_access";
    private static final int SYSTEM_UID = 1000;
    private static final String SYS_OPER_CMCC = "ro.config.operators";
    private static final String SYS_PROPERT_PDP = "ro.config.hw_RemindWifiToPdp";
    private static final String TAG = "WiFi_PRO_WifiProStateMachine";
    private static final int TCP_IP = 101;
    private static final int THRESHOD_RSSI = -82;
    private static final int THRESHOD_RSSI_HIGH = -76;
    private static final int THRESHOD_RSSI_LOW = -88;
    private static final int TURN_OFF_MOBILE = 0;
    private static final int TURN_OFF_WIFI = 1;
    private static final int TURN_OFF_WIFI_PRO = 2;
    private static final int TURN_ON_WIFI_PRO = 3;
    private static final int VALUE_WIFI_TO_PDP_ALWAYS_SHOW_DIALOG = 0;
    private static final int VALUE_WIFI_TO_PDP_AUTO_HANDOVER_MOBILE = 1;
    private static final int VALUE_WIFI_TO_PDP_CANNOT_HANDOVER_MOBILE = 2;
    private static final String VEHICLE_STATE_FLAG = "hw_higeo_vehicle_state";
    private static final int WIFI = 1;
    private static final int WIFI_CHECK_DELAY_TIME = 30000;
    private static final int WIFI_CHECK_UNKNOW_TIMER = 1;
    private static final String WIFI_CSP_DISPALY_STATE = "wifi_csp_dispaly_state";
    private static final String WIFI_EVALUATE_TAG = "wifipro_recommending_access_points";
    private static final int WIFI_GOOD_LINK_MAX_TIME_LIMIT = 1800000;
    private static final int WIFI_HANDOVER_MOBILE_TIMER_LIMIT = 4;
    private static final int WIFI_HANDOVER_TIMERS = 2;
    private static final int WIFI_SCAN_COUNT = 4;
    private static final int WIFI_SCAN_INTERVAL_MAX = 12;
    private static final int WIFI_TCPRX_STATISTICS_INTERVAL = 3000;
    private static final int WIFI_TO_WIFI_THRESHOLD = 3;
    private static final int WIFI_VERYY_INTERVAL_TIME = 30000;
    private static boolean mIsWifiManualEvaluating = false;
    private static boolean mIsWifiSemiAutoEvaluating = false;
    private static WifiProStateMachine mWifiProStateMachine;
    private boolean isMapNavigating;
    private boolean isVariableInited;
    private boolean isVehicleState;
    private AbsPhoneWindowManager mAbsPhoneWindowManager;
    private ActivityManager mActivityManager;
    private List<String> mAppWhitelists;
    private int mAvailable5GAPAuthType = 0;
    private String mAvailable5GAPBssid = null;
    private String mAvailable5GAPSsid = null;
    private String mBadBssid;
    private String mBadSsid;
    private BroadcastReceiver mBroadcastReceiver;
    private long mChrRoveOutStartTime = 0;
    private long mChrWifiDidableStartTime = 0;
    private long mChrWifiDisconnectStartTime = 0;
    private int mConnectWiFiAppPid;
    private ConnectivityManager mConnectivityManager;
    private ContentResolver mContentResolver;
    private Context mContext;
    private WifiInfo mCurrWifiInfo;
    private String mCurrentBssid;
    private int mCurrentRssi;
    private String mCurrentSsid;
    private int mCurrentVerfyCounter;
    private WifiConfiguration mCurrentWifiConfig;
    private int mCurrentWifiLevel;
    private DefaultState mDefaultState = new DefaultState();
    private boolean mDisableDuanBandHandover;
    private ArrayList<HwDualBandMonitorInfo> mDualBandCloneMonitorApList;
    private String mDualBandConnectAPSsid = null;
    private long mDualBandConnectTime;
    private ArrayList<WifiProEstimateApInfo> mDualBandEstimateApList = new ArrayList();
    private int mDualBandEstimateInfoSize = 0;
    HwDualBandManager mDualBandManager;
    private ArrayList<HwDualBandMonitorInfo> mDualBandMonitorApList = new ArrayList();
    private int mDualBandMonitorInfoSize = 0;
    private boolean mDualBandMonitorStart = false;
    private WifiProEstimateApInfo mDualbandCurrentAPInfo = null;
    private long mDualbandPingPangTime = 0;
    private int mDualbandRoveInConut = 0;
    private WifiProEstimateApInfo mDualbandSelectAPInfo = null;
    private long mDualbandhanoverTime = 0;
    private int mDuanBandHandoverType = 0;
    private volatile int mEmuiPdpSwichValue;
    private BroadcastReceiver mHMDBroadcastReceiver;
    private IntentFilter mHMDIntentFilter;
    private HwAutoConnectManager mHwAutoConnectManager;
    private HwDualBandBlackListManager mHwDualBandBlackListMgr;
    private HwIntelligenceWiFiManager mHwIntelligenceWiFiManager;
    private IntentFilter mIntentFilter;
    private boolean mIsAllowEvaluate;
    private boolean mIsDualbandhandover = false;
    private boolean mIsDualbandhandoverSucc = false;
    private boolean mIsManualConnectedWiFi;
    private boolean mIsMobileDataEnabled;
    private boolean mIsNetworkAuthen;
    private boolean mIsP2PConnectedOrConnecting;
    private boolean mIsPoorRssiRequestCheckWiFi;
    private boolean mIsPortalAp;
    private boolean mIsPrimaryUser;
    private boolean mIsRoveOutToDisconn = false;
    private boolean mIsScanedRssiLow;
    private boolean mIsScanedRssiMiddle;
    private boolean mIsUserHandoverWiFi;
    private boolean mIsUserManualConnectAp;
    private volatile boolean mIsVpnWorking;
    private boolean mIsWiFiNoInternet;
    private boolean mIsWiFiProAutoEvaluateAP;
    private boolean mIsWiFiProConnected;
    private boolean mIsWiFiProEnabled;
    private boolean mIsWifiSemiAutoEvaluateComplete;
    private int mLastCSPState = -1;
    private String mLastConnect5GAP = null;
    private int mLastWifiLevel;
    private boolean mLoseInetRoveOut = false;
    private ContentObserver mMapNavigatingStateChangeObserver = new ContentObserver(null) {
        public void onChange(boolean selfChange) {
            boolean z = true;
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            if (Secure.getInt(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.MAPS_LOCATION_FLAG, 0) != 1) {
                z = false;
            }
            wifiProStateMachine.isMapNavigating = z;
            WifiProStateMachine.this.logD("MapNavigating state change,MapNavigating: " + WifiProStateMachine.this.isMapNavigating);
        }
    };
    private boolean mNeedRetryMonitor;
    private NetworkBlackListManager mNetworkBlackListManager;
    private NetworkQosMonitor mNetworkQosMonitor;
    private String mNewSelect_bssid;
    private int mOpenAvailableAPCounter;
    private PowerManager mPowerManager;
    private String mRoSsid = null;
    private boolean mRoveOutStarted = false;
    private SampleCollectionManager mSampleCollectionManager;
    private List<ScanResult> mScanResultList;
    private TelephonyManager mTelephonyManager;
    private ContentObserver mVehicleStateChangeObserver = new ContentObserver(null) {
        public void onChange(boolean selfChange) {
            boolean z = true;
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            if (Secure.getInt(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.VEHICLE_STATE_FLAG, 0) != 1) {
                z = false;
            }
            wifiProStateMachine.isVehicleState = z;
            WifiProStateMachine.this.logD("VehicleState state change,VehicleState: " + WifiProStateMachine.this.isVehicleState);
        }
    };
    private WiFiLinkMonitorState mWiFiLinkMonitorState = new WiFiLinkMonitorState();
    private int mWiFiNoInternetReason;
    private WifiProCHRManager mWiFiProCHRMgr;
    private WiFiProDisabledState mWiFiProDisabledState = new WiFiProDisabledState();
    private WiFiProEnableState mWiFiProEnableState = new WiFiProEnableState();
    private WiFiProEvaluateController mWiFiProEvaluateController;
    private volatile int mWiFiProPdpSwichValue;
    private WiFiProPortalController mWiFiProProtalController;
    private WiFiProVerfyingLinkState mWiFiProVerfyingLinkState = new WiFiProVerfyingLinkState();
    private WifiConnectedState mWifiConnectedState = new WifiConnectedState();
    private WifiDisConnectedState mWifiDisConnectedState = new WifiDisConnectedState();
    private WifiHandover mWifiHandover;
    private WifiManager mWifiManager;
    private WifiProConfigStore mWifiProConfigStore;
    private WifiProConfigurationManager mWifiProConfigurationManager;
    private WifiProStatisticsManager mWifiProStatisticsManager;
    private WifiProUIDisplayManager mWifiProUIDisplayManager;
    private WifiSemiAutoEvaluateState mWifiSemiAutoEvaluateState = new WifiSemiAutoEvaluateState();
    private WifiSemiAutoScoreState mWifiSemiAutoScoreState = new WifiSemiAutoScoreState();
    private int mWifiTcpRxCount;
    private int mWifiToWifiType = 0;
    private AsyncChannel mWsmChannel;

    class DefaultState extends State {
        DefaultState() {
        }

        public void enter() {
            WifiProStateMachine.this.logD("DefaultState is Enter");
            WifiProStateMachine.this.defaulVariableInit();
        }

        public void exit() {
            WifiProStateMachine.this.logD("DefaultState is Exit");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE /*136171*/:
                    if (WifiProStateMachine.this.mIsWiFiProEnabled && WifiProStateMachine.this.mIsPrimaryUser) {
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiProEnableState);
                    } else {
                        WifiProStateMachine.this.onDisableWiFiPro();
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    class WiFiLinkMonitorState extends State {
        private int currWifiPoorlevel;
        private int internetFailureDetectedCnt;
        private boolean isAllowWiFiHandoverMobile;
        private boolean isBQERequestCheckWiFi;
        private boolean isCancelCHRTypeReport;
        private boolean isCheckWiFiForUpdateSetting;
        private boolean isDialogDisplayed;
        private boolean isRssiLevel0Scaned;
        private boolean isRssiLevel1Scaned;
        private boolean isRssiLevel2Scaned;
        private boolean isScreenOffMonitor;
        private boolean isSwitching;
        private boolean isToastDisplayed;
        private boolean isWiFiHandoverPriority;
        private boolean isWifi2MobileUIShowing;
        private boolean isWifi2WifiProcess;
        private int tcpRxStatisticsTimer;
        private long wifiLinkHoldTime;
        private int wifiMonitorCounter;

        WiFiLinkMonitorState() {
        }

        private void wiFiLinkMonitorStateInit() {
            WifiProStateMachine.this.logD("wiFiLinkMonitorStateInit is Start");
            WifiProStateMachine.this.mBadBssid = null;
            this.isSwitching = false;
            this.isWifi2WifiProcess = false;
            this.isWifi2MobileUIShowing = false;
            this.isCheckWiFiForUpdateSetting = false;
            this.isDialogDisplayed = false;
            WifiProStateMachine.this.setWifiCSPState(1);
            if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                WifiProStateMachine.this.logD("mIsWiFiNoInternet is true,sendMessage wifi Qos is -1");
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_QOS_CHANGE, -1);
            } else {
                WifiProStateMachine.this.setWifiMonitorEnabled(true);
            }
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
            WifiProStateMachine.this.mNeedRetryMonitor = false;
        }

        public void enter() {
            WifiProStateMachine.this.logD("WiFiLinkMonitorState is Enter");
            NetworkInfo wifi_info = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(1);
            if (wifi_info != null && wifi_info.getDetailedState() == DetailedState.VERIFYING_POOR_LINK) {
                WifiProStateMachine.this.logD(" POOR_LINK_DETECTED sendMessageDelayed");
                WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.GOOD_LINK_DETECTED);
            }
            if (WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                updateWifiQosLevel(WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mNetworkQosMonitor.getCurrentWiFiLevel());
            }
            this.wifiMonitorCounter = 0;
            this.tcpRxStatisticsTimer = 0;
            this.internetFailureDetectedCnt = 0;
            this.isScreenOffMonitor = false;
            this.isAllowWiFiHandoverMobile = true;
            this.isCancelCHRTypeReport = false;
            this.isRssiLevel2Scaned = false;
            this.isRssiLevel1Scaned = false;
            this.isRssiLevel0Scaned = false;
            wiFiLinkMonitorStateInit();
            this.wifiLinkHoldTime = System.currentTimeMillis();
            if (0 != WifiProStateMachine.this.mChrRoveOutStartTime && (WifiProStateMachine.this.mChrWifiDisconnectStartTime > WifiProStateMachine.this.mChrRoveOutStartTime || WifiProStateMachine.this.mChrWifiDidableStartTime > WifiProStateMachine.this.mChrRoveOutStartTime)) {
                long disableRestoreTime = System.currentTimeMillis() - WifiProStateMachine.this.mChrWifiDidableStartTime;
                boolean ssidIsSame = false;
                if (!(WifiProStateMachine.this.mRoSsid == null || WifiProStateMachine.this.mCurrentSsid == null)) {
                    ssidIsSame = WifiProStateMachine.this.mRoSsid.equals(WifiProStateMachine.this.mCurrentSsid);
                }
                if (ssidIsSame && disableRestoreTime <= 30000) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.increaseUserReopenWifiRiCount();
                }
            }
            WifiProStateMachine.this.mChrRoveOutStartTime = 0;
            WifiProStateMachine.this.mChrWifiDisconnectStartTime = 0;
            WifiProStateMachine.this.mChrWifiDidableStartTime = 0;
        }

        public void exit() {
            WifiProStateMachine.this.logD("WiFiLinkMonitorState is Exit");
            WifiProStateMachine.this.mNetworkQosMonitor.stopBqeService();
            WifiProStateMachine.this.setWifiMonitorEnabled(false);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_DUALBAND_DELAY_RETRY);
            WifiProStateMachine.this.stopDualBandMonitor();
            this.isToastDisplayed = false;
            this.isDialogDisplayed = false;
            WifiProStateMachine.this.mIsPoorRssiRequestCheckWiFi = false;
            this.isWiFiHandoverPriority = false;
            if (System.currentTimeMillis() - this.wifiLinkHoldTime > 1800000) {
                WifiProStateMachine.this.mCurrentVerfyCounter = 0;
            }
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /*136169*/:
                    NetworkInfo networkInfo = (NetworkInfo) msg.obj.getParcelableExtra("networkInfo");
                    if (networkInfo == null || DetailedState.VERIFYING_POOR_LINK != networkInfo.getDetailedState()) {
                        if (networkInfo != null && NetworkInfo.State.DISCONNECTED == networkInfo.getState()) {
                            WifiProStateMachine.this.logD("wifi has disconnected,isWifi2WifiProcess = " + this.isWifi2WifiProcess);
                            if (!(this.isWifi2WifiProcess && WifiProStateMachine.this.mWifiManager.isWifiEnabled())) {
                                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                                break;
                            }
                        }
                    }
                    WifiProStateMachine.this.logD("wifi handover mobile is Complete!");
                    this.isSwitching = false;
                    WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProToast(1);
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiProVerfyingLinkState);
                    break;
                    break;
                case WifiProStateMachine.EVENT_DEVICE_SCREEN_ON /*136170*/:
                    if (!this.isScreenOffMonitor) {
                        WifiProStateMachine.this.logD("device screen on,but isScreenOffMonitor is false");
                        break;
                    }
                    WifiProStateMachine.this.logD("device screen on,reinitialize wifi monitor");
                    this.isScreenOffMonitor = false;
                    this.wifiMonitorCounter = 0;
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR);
                    break;
                case WifiProStateMachine.EVENT_WIFI_QOS_CHANGE /*136172*/:
                    if (!pendingMsgBySelfCureEngine(msg.arg1)) {
                        if (!handleMsgBySwitchOrDialogStatus(msg.arg1)) {
                            WifiProStateMachine.this.logD("WiFiLinkMonitorState receive wifi Qos currWifiPoorlevel == " + msg.arg1);
                            if (-103 != msg.arg1) {
                                if (-104 != msg.arg1) {
                                    if (!this.isCheckWiFiForUpdateSetting) {
                                        if (msg.arg1 <= 2) {
                                            this.currWifiPoorlevel = msg.arg1;
                                            WifiProStateMachine.this.logD("currWifiPoorlevel == " + this.currWifiPoorlevel);
                                            if (this.currWifiPoorlevel == -1) {
                                                WifiProStateMachine.this.mIsWiFiNoInternet = true;
                                            }
                                            WifiProStateMachine.this.mWifiToWifiType = 0;
                                            if (this.currWifiPoorlevel != -2) {
                                                updateWifiQosLevel(WifiProStateMachine.this.mIsWiFiNoInternet, 1);
                                                if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                                                    WifiProStateMachine.this.mWifiToWifiType = 1;
                                                }
                                                this.isSwitching = true;
                                                WifiProStateMachine.this.mBadBssid = WifiProStateMachine.this.mCurrentBssid;
                                                WifiProStateMachine.this.mBadSsid = WifiProStateMachine.this.mCurrentSsid;
                                                WifiProStateMachine.this.logW("WiFiLinkMonitorState : try wifi --> wifi");
                                                this.isWiFiHandoverPriority = false;
                                                this.isWifi2WifiProcess = true;
                                                if (!WifiProStateMachine.this.mWifiHandover.handleWifiToWifi(WifiProStateMachine.this.mNetworkBlackListManager.getWifiBlacklist(), WifiProStateMachine.THRESHOD_RSSI, 0)) {
                                                    wifi2WifiFailed();
                                                    break;
                                                }
                                            }
                                            WifiProStateMachine.this.refreshConnectedNetWork();
                                            tryWifiHandoverPreferentially(WifiProStateMachine.this.mCurrentRssi);
                                            break;
                                        }
                                    } else if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                                        WifiProStateMachine.this.setWifiMonitorEnabled(true);
                                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT, WifiProStateMachine.this.mNetworkQosMonitor.getCurrentWiFiLevel());
                                        break;
                                    }
                                }
                                WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                                WifiProStateMachine.this.mIsPoorRssiRequestCheckWiFi = true;
                                this.isBQERequestCheckWiFi = true;
                                break;
                            }
                            WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                            WifiProStateMachine.this.mIsPoorRssiRequestCheckWiFi = false;
                            this.isBQERequestCheckWiFi = true;
                            break;
                        }
                    }
                    break;
                case WifiProStateMachine.EVENT_CHECK_AVAILABLE_AP_RESULT /*136176*/:
                    if (!(WifiProStateMachine.this.isFullscreen() || WifiProStateMachine.this.mIsWiFiNoInternet || !((Boolean) msg.obj).booleanValue() || this.isWiFiHandoverPriority || this.isWifi2WifiProcess || WifiProStateMachine.this.mNetworkQosMonitor.isHighDataFlowModel() || (WifiProStateMachine.this.mCurrentWifiConfig != null && WifiProStateMachine.this.mCurrentWifiConfig.networkQosLevel == 3))) {
                        int curRssiLevel = WifiProStateMachine.this.mWiFiProEvaluateController.calculateSignalLevelHW(WifiProStateMachine.this.mCurrentRssi);
                        if (curRssiLevel < 3) {
                            int targetRssiLevel = WifiProStateMachine.this.mWiFiProEvaluateController.calculateSignalLevelHW(Integer.valueOf(msg.arg1).intValue());
                            WifiProStateMachine.this.logD("curRssiLevel = " + curRssiLevel + ", targetRssiLevel " + targetRssiLevel);
                            if (targetRssiLevel - curRssiLevel >= 2) {
                                tryWifiHandoverPreferentially(WifiProStateMachine.this.mCurrentRssi);
                                break;
                            }
                        }
                    }
                    break;
                case WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE /*136177*/:
                    break;
                case WifiProStateMachine.EVENT_WIFI_HANDOVER_WIFI_RESULT /*136178*/:
                    WifiProStateMachine.this.logD("receive wifi handover wifi Result,isWifi2WifiProcess = " + this.isWifi2WifiProcess);
                    if (this.isWifi2WifiProcess) {
                        if (!((Boolean) msg.obj).booleanValue()) {
                            wifi2WifiFailed();
                            break;
                        }
                        WifiProStateMachine.this.logD(" wifi --> wifi is  succeed");
                        this.isSwitching = false;
                        WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mBadSsid);
                        WifiProStateMachine.this.addDualBandBlackList(WifiProStateMachine.this.mBadSsid);
                        WifiProStateMachine.this.mIsWiFiProConnected = true;
                        WifiProStateMachine.this.refreshConnectedNetWork();
                        WifiProStateMachine.this.mWiFiProEvaluateController.reSetEvaluateRecord(WifiProStateMachine.this.mCurrentSsid);
                        WifiProStateMachine.this.mWifiProConfigStore.cleanWifiProConfig(WifiProStateMachine.this.mCurrentWifiConfig);
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_WIFI_RSSI_CHANGE /*136179*/:
                    WifiProStateMachine.this.mCurrentRssi = msg.obj.getIntExtra("newRssi", WifiHandover.INVALID_RSSI);
                    if (!WifiProStateMachine.this.isFullscreen()) {
                        int rssilevel = WifiProStateMachine.this.mWiFiProEvaluateController.calculateSignalLevelHW(WifiProStateMachine.this.mCurrentRssi);
                        if (!(rssilevel >= 3 || WifiProStateMachine.this.mIsWiFiNoInternet || ((this.isRssiLevel2Scaned && this.isRssiLevel1Scaned && this.isRssiLevel0Scaned) || this.isWiFiHandoverPriority || this.isWifi2WifiProcess || !WifiProStateMachine.this.mPowerManager.isScreenOn() || WifiProStateMachine.this.isSettingsActivity()))) {
                            if (rssilevel != 2 || this.isRssiLevel2Scaned) {
                                if (rssilevel != 1 || this.isRssiLevel1Scaned) {
                                    if (rssilevel == 0 && !this.isRssiLevel0Scaned) {
                                        WifiProStateMachine.this.logD("rssilevel = " + rssilevel + ", isRssiLevel0Scaned " + this.isRssiLevel0Scaned);
                                        this.isRssiLevel0Scaned = true;
                                        WifiProStateMachine.this.mWifiManager.startScan();
                                        break;
                                    }
                                }
                                WifiProStateMachine.this.logD("rssilevel = " + rssilevel + ", isRssiLevel1Scaned " + this.isRssiLevel1Scaned);
                                this.isRssiLevel1Scaned = true;
                                WifiProStateMachine.this.mWifiManager.startScan();
                                break;
                            }
                            this.isRssiLevel2Scaned = true;
                            WifiProStateMachine.this.logD("rssilevel = " + rssilevel + ", isRssiLevel2Scaned " + this.isRssiLevel2Scaned);
                            WifiProStateMachine.this.mWifiManager.startScan();
                            break;
                        }
                    }
                    break;
                case WifiProStateMachine.EVENT_CHECK_MOBILE_QOS_RESULT /*136180*/:
                    tryWifi2Mobile(msg.arg1);
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT /*136181*/:
                    int wifi_internet_level = msg.arg1;
                    WifiProStateMachine.this.logD("WiFiLinkMonitorState : wifi_internet_level = " + wifi_internet_level);
                    if (-1 == msg.arg1 || 6 == msg.arg1) {
                        WifiProStateMachine.this.mIsWiFiNoInternet = true;
                        this.currWifiPoorlevel = -1;
                        wifi_internet_level = this.currWifiPoorlevel;
                        if (this.isBQERequestCheckWiFi) {
                            WifiProStateMachine.this.mWifiProStatisticsManager.increaseNoInetRemindCount(false);
                        }
                        if (this.isCheckWiFiForUpdateSetting) {
                            WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                            if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                            }
                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                        }
                    } else {
                        WifiProStateMachine.this.mIsWiFiNoInternet = false;
                        updateWifiQosLevel(WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mNetworkQosMonitor.getCurrentWiFiLevel());
                        WifiProStateMachine.this.reSetWifiInternetState();
                    }
                    this.isBQERequestCheckWiFi = false;
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_QOS_CHANGE, wifi_internet_level);
                    break;
                case WifiProStateMachine.EVENT_DIALOG_OK /*136182*/:
                    if (this.isWifi2MobileUIShowing) {
                        this.isDialogDisplayed = false;
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                        this.isWifi2MobileUIShowing = false;
                        WifiProStateMachine.this.mWiFiProPdpSwichValue = 1;
                        WifiProStateMachine.this.setWifiCSPState(0);
                        WifiProStateMachine.this.logD("Click OK ,is send message to wifi handover mobile ,WiFiProPdp is AUTO");
                        if (WifiProStateMachine.this.mIsMobileDataEnabled && WifiProStateMachine.this.mPowerManager.isScreenOn() && WifiProStateMachine.this.mEmuiPdpSwichValue != 2) {
                            int roReason;
                            this.isAllowWiFiHandoverMobile = true;
                            WifiProStateMachine.this.logD("mWsmChannel send Poor Link Detected");
                            WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetWorkHandover(1);
                            WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.POOR_LINK_DETECTED);
                            if (this.currWifiPoorlevel == -1) {
                                roReason = 2;
                                WifiProStateMachine.this.mWifiProStatisticsManager.increaseNoInetHandoverCount();
                            } else {
                                roReason = 1;
                            }
                            WifiProStateMachine.this.logD("roReason = " + roReason);
                            WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveOutEvent(roReason);
                        }
                        WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_DIALOG_CANCEL /*136183*/:
                    if (this.isWifi2MobileUIShowing) {
                        WifiProStateMachine.this.logD("isDialogDisplayed : " + this.isDialogDisplayed + ", mIsWiFiNoInternet " + WifiProStateMachine.this.mIsWiFiNoInternet);
                        if (this.isDialogDisplayed) {
                            if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                                WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetUserCancelCount();
                            } else {
                                WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(2);
                            }
                        } else if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                            WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetSettingCancelCount();
                        } else {
                            WifiProStateMachine.this.mWifiProStatisticsManager.increaseBQE_BadSettingCancelCount();
                        }
                        this.isDialogDisplayed = false;
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                        this.isWifi2MobileUIShowing = false;
                        this.isAllowWiFiHandoverMobile = false;
                        WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
                        this.isSwitching = false;
                        WifiProStateMachine.this.mWiFiProPdpSwichValue = 2;
                        WifiProStateMachine.this.logD("Click Cancel ,is not allow wifi handover mobile, WiFiProPdp is CANNOT");
                        if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                            WifiProStateMachine.this.setWifiMonitorEnabled(true);
                            break;
                        }
                        this.isCheckWiFiForUpdateSetting = true;
                        WifiProStateMachine.this.setWifiMonitorEnabled(false);
                        if (WifiProStateMachine.this.mCurrentWifiConfig == null || !WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetAccess || WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetReason != 0) {
                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                            break;
                        }
                        WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                        if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                        }
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR /*136184*/:
                    WifiProStateMachine.this.logD("ReIniitalize,ScreenOn == " + WifiProStateMachine.this.mPowerManager.isScreenOn());
                    if (!WifiProStateMachine.this.mPowerManager.isScreenOn()) {
                        this.isScreenOffMonitor = true;
                        break;
                    }
                    this.wifiMonitorCounter++;
                    if (this.wifiMonitorCounter >= 4) {
                        this.wifiMonitorCounter = Math.min(this.wifiMonitorCounter, 12);
                        long delay_time = (((long) Math.pow(2.0d, (double) (this.wifiMonitorCounter / 4))) * 60) * 1000;
                        WifiProStateMachine.this.logD("delay_time = " + delay_time);
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI, delay_time);
                        if (WifiProStateMachine.this.mIsWiFiNoInternet && !this.isCheckWiFiForUpdateSetting) {
                            this.isCheckWiFiForUpdateSetting = true;
                            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                        }
                        if (WifiProStateMachine.this.mCurrentWifiConfig != null && WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetAccess && WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetReason == 0) {
                            WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                            if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                            }
                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                        }
                    } else {
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI);
                    }
                    WifiProStateMachine.this.logD("wifiMonitorCounter = " + this.wifiMonitorCounter);
                    break;
                case WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION /*136186*/:
                    WifiProStateMachine.this.logD("WiFiLinkMonitorState : Receive Mobile changed");
                    if (WifiProStateMachine.this.isMobileDataConnected() && this.isAllowWiFiHandoverMobile) {
                        this.isCheckWiFiForUpdateSetting = false;
                        if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                            WifiProStateMachine.this.setWifiMonitorEnabled(true);
                            break;
                        }
                        WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                        break;
                    }
                case WifiProStateMachine.EVENT_EMUI_CSP_SETTINGS_CHANGE /*136190*/:
                    if (WifiProStateMachine.this.mEmuiPdpSwichValue != 2) {
                        this.isAllowWiFiHandoverMobile = true;
                        this.isCheckWiFiForUpdateSetting = false;
                        this.isSwitching = false;
                        if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                            WifiProStateMachine.this.setWifiMonitorEnabled(true);
                            break;
                        }
                        WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI /*136191*/:
                    WifiProStateMachine.this.logD("receive : EVENT_RETRY_WIFI_TO_WIFI");
                    if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                        WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                    }
                    WifiProStateMachine.this.mIsWiFiNoInternet = false;
                    this.isCheckWiFiForUpdateSetting = false;
                    wiFiLinkMonitorStateInit();
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET /*136192*/:
                    if (WifiProStateMachine.this.mIsWiFiNoInternet && (this.isCheckWiFiForUpdateSetting || this.isDialogDisplayed)) {
                        WifiProStateMachine.this.logD("queryNetworkQos for wifi , isCheckWiFiForUpdateSetting =" + this.isCheckWiFiForUpdateSetting + ", isDialogDisplayed =" + this.isDialogDisplayed);
                        WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                        break;
                    }
                case WifiProStateMachine.EVENT_HTTP_REACHABLE_RESULT /*136195*/:
                    if (msg.obj == null || !((Boolean) msg.obj).booleanValue()) {
                        if (!(msg.obj == null || ((Boolean) msg.obj).booleanValue())) {
                            WifiProStateMachine.this.logD("EVENT_HTTP_REACHABLE_RESULT = false, SCE force to request wifi switch.");
                            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_QOS_CHANGE, 0);
                            break;
                        }
                    }
                    this.internetFailureDetectedCnt = 0;
                    break;
                    break;
                case WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT /*136299*/:
                    int rttlevel = msg.arg1;
                    WifiProStateMachine.this.logD(WifiProStateMachine.this.mCurrentSsid + "  TCPRTT  level = " + rttlevel);
                    if (rttlevel <= 0 || rttlevel > 3) {
                        rttlevel = 0;
                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(23);
                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBG_AP_SSID(WifiProStateMachine.this.mCurrentSsid);
                    }
                    updateWifiQosLevel(false, rttlevel);
                    break;
                case WifiProStateMachine.EVENT_WIFI_SEMIAUTO_EVALUTE_CHANGE /*136300*/:
                    if (WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                        int accessType;
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT);
                        if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                            accessType = 4;
                        } else {
                            accessType = 2;
                        }
                        if (WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, accessType)) {
                            WifiProStateMachine.this.logD("mCurrentSsid   = " + WifiProStateMachine.this.mCurrentSsid + ", updateScoreInfoType  " + accessType);
                            WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, accessType, WifiProStateMachine.this.mCurrentSsid);
                        }
                        if (accessType == 4) {
                            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT, WifiProStateMachine.this.mNetworkQosMonitor.getCurrentWiFiLevel());
                            break;
                        }
                    }
                    break;
                case WifiProStateMachine.EVENT_GET_WIFI_TCPRX /*136311*/:
                    int currentWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                    WifiProStateMachine.this.logD("current rx= " + currentWifiTcpRxCount + ", last rx = " + WifiProStateMachine.this.mWifiTcpRxCount);
                    if (currentWifiTcpRxCount - WifiProStateMachine.this.mWifiTcpRxCount <= 0) {
                        WifiProStateMachine.this.logD("tcpRxStatisticsTimer = " + this.tcpRxStatisticsTimer + ", % 10 = " + (this.tcpRxStatisticsTimer % 10));
                        if (this.tcpRxStatisticsTimer > 0 && this.tcpRxStatisticsTimer % 10 == 0) {
                            this.tcpRxStatisticsTimer = 0;
                            WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                            break;
                        }
                        if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                        }
                        this.tcpRxStatisticsTimer++;
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                        break;
                    }
                    this.tcpRxStatisticsTimer = 0;
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                    break;
                    break;
                case WifiProStateMachine.EVENT_DUALBAND_RSSITH_RESULT /*136368*/:
                    if (!this.isWifi2WifiProcess) {
                        WifiProEstimateApInfo apInfo = msg.obj;
                        if (WifiProStateMachine.this.mDualBandMonitorInfoSize > 0) {
                            WifiProStateMachine.this.mDualBandMonitorInfoSize = WifiProStateMachine.this.mDualBandMonitorInfoSize - 1;
                            WifiProStateMachine.this.updateDualBandMonitorInfo(apInfo);
                        }
                        if (WifiProStateMachine.this.mDualBandMonitorInfoSize == 0) {
                            WifiProStateMachine.this.mDualBandMonitorStart = true;
                            WifiProStateMachine.this.logD("Start dual band Manager monitor");
                            WifiProStateMachine.this.mDualBandManager.startMonitor(WifiProStateMachine.this.mDualBandMonitorApList);
                            break;
                        }
                    }
                    WifiProStateMachine.this.logD("isWifi2WifiProcess is true, ignore this message");
                    WifiProStateMachine.this.mNeedRetryMonitor = true;
                    break;
                    break;
                case WifiProStateMachine.EVENT_DUALBAND_SCORE_RESULT /*136369*/:
                    if (!this.isWifi2WifiProcess) {
                        WifiProEstimateApInfo estimateApInfo = msg.obj;
                        WifiProStateMachine.this.logD("EVENT_DUALBAND_SCORE_RESULT estimateApInfo: " + estimateApInfo.toString());
                        if (WifiProStateMachine.this.mDualBandEstimateInfoSize > 0) {
                            WifiProStateMachine.this.mDualBandEstimateInfoSize = WifiProStateMachine.this.mDualBandEstimateInfoSize - 1;
                            WifiProStateMachine.this.updateDualBandEstimateInfo(estimateApInfo);
                        }
                        WifiProStateMachine.this.logD("mDualBandEstimateInfoSize = " + WifiProStateMachine.this.mDualBandEstimateInfoSize);
                        if (WifiProStateMachine.this.mDualBandEstimateInfoSize == 0) {
                            WifiProStateMachine.this.chooseAvalibleDualBandAp();
                            break;
                        }
                    }
                    WifiProStateMachine.this.logD("isWifi2WifiProcess is true, ignore this message");
                    WifiProStateMachine.this.mNeedRetryMonitor = true;
                    break;
                    break;
                case WifiProStateMachine.EVENT_DUALBAND_5GAP_AVAILABLE /*136370*/:
                    WifiProStateMachine.this.logD("tbc receive EVENT_DUALBAND_5GAP_AVAILABLE isSwitching = " + this.isSwitching);
                    if (!this.isSwitching) {
                        this.isSwitching = true;
                        this.isWifi2WifiProcess = true;
                        WifiProStateMachine.this.mBadBssid = WifiProStateMachine.this.mCurrentBssid;
                        WifiProStateMachine.this.mBadSsid = WifiProStateMachine.this.mCurrentSsid;
                        WifiProStateMachine.this.logD("do dual band wifi handover, mCurrentBssid:" + WifiProStateMachine.this.mCurrentBssid + ", mCurrentSsid:" + WifiProStateMachine.this.mCurrentSsid + ", mAvailable5GAPBssid = " + WifiProStateMachine.this.mAvailable5GAPBssid + ", mAvailable5GAPSsid =" + WifiProStateMachine.this.mAvailable5GAPSsid + ", mDuanBandHandoverType = " + WifiProStateMachine.this.mDuanBandHandoverType);
                        int switchType = WifiProStateMachine.this.mDuanBandHandoverType == 1 ? 2 : 1;
                        WifiProStateMachine.this.logD("do dual band wifi handover, switchType = " + switchType);
                        if (!WifiProStateMachine.this.isFullscreen() || switchType != 1) {
                            WifiProStateMachine.this.mNewSelect_bssid = WifiProStateMachine.this.mAvailable5GAPBssid;
                            if (!WifiProStateMachine.this.mWifiHandover.handleDualBandWifiConnect(WifiProStateMachine.this.mAvailable5GAPBssid, WifiProStateMachine.this.mAvailable5GAPSsid, WifiProStateMachine.this.mAvailable5GAPAuthType, switchType)) {
                                dualBandhandoverFailed(0);
                                break;
                            }
                        }
                        WifiProStateMachine.this.logD("keep in current AP,now is in full screen and switch by hardhandover");
                        break;
                    }
                    WifiProStateMachine.this.mNeedRetryMonitor = true;
                    break;
                    break;
                case WifiProStateMachine.EVENT_DUALBAND_WIFI_HANDOVER_RESULT /*136371*/:
                    WifiProStateMachine.this.logD("receive dual band wifi handover resust");
                    if (this.isWifi2WifiProcess) {
                        int errorReason = msg.arg1;
                        if (errorReason != 0) {
                            WifiProStateMachine.this.logD("dual band wifi handover is  failure");
                            dualBandhandoverFailed(errorReason);
                            break;
                        }
                        WifiProStateMachine.this.logD("dual band wifi handover is  succeed, ssid =" + WifiProStateMachine.this.mNewSelect_bssid);
                        this.isSwitching = false;
                        WifiProStateMachine.this.mDualBandConnectAPSsid = WifiProStateMachine.this.mNewSelect_bssid;
                        WifiProStateMachine.this.mDualBandConnectTime = System.currentTimeMillis();
                        WifiProStateMachine.this.reportPingPangCHR(WifiProStateMachine.this.mDualBandConnectAPSsid);
                        WifiProStateMachine.this.reportDualbandStatisticsCHR(1);
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_DUALBAND_DELAY_RETRY /*136372*/:
                    WifiProStateMachine.this.logD("receive dual band wifi handover delay retry");
                    WifiProStateMachine.this.retryDualBandAPMonitor();
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void tryWifiHandoverPreferentially(int rssi) {
            if (rssi <= WifiProStateMachine.THRESHOD_RSSI_HIGH) {
                if (rssi < WifiProStateMachine.THRESHOD_RSSI_LOW && !WifiProStateMachine.this.mIsScanedRssiLow) {
                    WifiProStateMachine.this.mIsScanedRssiLow = true;
                } else if (rssi >= WifiProStateMachine.THRESHOD_RSSI_LOW && !WifiProStateMachine.this.mIsScanedRssiMiddle) {
                    WifiProStateMachine.this.mIsScanedRssiMiddle = true;
                } else {
                    return;
                }
                this.isSwitching = true;
                this.isWiFiHandoverPriority = true;
                this.isWifi2WifiProcess = true;
                WifiProStateMachine.this.mBadBssid = WifiProStateMachine.this.mCurrentBssid;
                WifiProStateMachine.this.mBadSsid = WifiProStateMachine.this.mCurrentSsid;
                WifiProStateMachine.this.logW("try wifi --> wifi Preferentially ,current rssi = " + rssi);
                if (!WifiProStateMachine.this.mWifiHandover.handleWifiToWifi(WifiProStateMachine.this.mNetworkBlackListManager.getWifiBlacklist(), Math.max(WifiProStateMachine.THRESHOD_RSSI, rssi + 10), 0)) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_HANDOVER_WIFI_RESULT, Boolean.valueOf(false));
                }
            }
        }

        private void tryWifi2Mobile(int mobile_level) {
            WifiProStateMachine.this.logD("Receive mobile QOS  mobile_level = " + mobile_level + ", isSwitching =" + this.isSwitching);
            if (!this.isWifi2WifiProcess && WifiProStateMachine.this.isAllowWifi2Mobile() && this.isAllowWiFiHandoverMobile) {
                if (!WifiProStateMachine.this.isWiFiPoorer(this.currWifiPoorlevel, mobile_level)) {
                    WifiProStateMachine.this.logD("mobile is poorer,continue monitor");
                    if (WifiProStateMachine.this.mIsWiFiNoInternet && !this.isToastDisplayed) {
                        this.isToastDisplayed = true;
                        WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProToast(3);
                    }
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR, 30000);
                } else if (this.isSwitching && WifiProStateMachine.this.isAllowWifi2Mobile()) {
                    WifiProStateMachine.this.logD("mobile is better than wifi,and ScreenOn, try wifi --> mobile,show Dialog mEmuiPdpSwichValue = " + WifiProStateMachine.this.mEmuiPdpSwichValue + ", mIsWiFiNoInternet =" + WifiProStateMachine.this.mIsWiFiNoInternet);
                    if (this.isWifi2MobileUIShowing) {
                        WifiProStateMachine.this.logD("isWifi2MobileUIShowing = true, not dispaly " + this.isWifi2MobileUIShowing);
                        return;
                    }
                    this.isWifi2MobileUIShowing = true;
                    if (WifiProStateMachine.this.isPdpAvailable()) {
                        WifiProStateMachine.this.logD("mobile is cmcc and wifi pdp, mEmuiPdpSwichValue = " + WifiProStateMachine.this.mEmuiPdpSwichValue + " ,mWiFiProPdpSwichValue = " + WifiProStateMachine.this.mWiFiProPdpSwichValue);
                        switch (WifiProStateMachine.this.mEmuiPdpSwichValue) {
                            case 0:
                                if (WifiProStateMachine.this.mWiFiProPdpSwichValue != 1) {
                                    if (WifiProStateMachine.this.mWiFiProPdpSwichValue != 2) {
                                        int dialogId;
                                        if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                                            dialogId = 2;
                                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                                        } else {
                                            dialogId = 1;
                                        }
                                        WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProDialog(dialogId);
                                        this.isDialogDisplayed = true;
                                        break;
                                    }
                                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_CANCEL);
                                    break;
                                }
                                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_OK);
                                break;
                            case 1:
                                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_OK);
                                break;
                            case 2:
                                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_CANCEL);
                                break;
                            default:
                                break;
                        }
                    }
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_OK);
                } else {
                    WifiProStateMachine.this.logW("no handover,DELAY Transit to Monitor");
                    this.isSwitching = false;
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR, 30000);
                }
                return;
            }
            WifiProStateMachine.this.logD("isWifi2WifiProcess = " + this.isWifi2WifiProcess + ", isAllowWifi2Mobile = " + WifiProStateMachine.this.isAllowWifi2Mobile() + ", mIsAllowWiFiHandoverMobile = " + this.isAllowWiFiHandoverMobile);
        }

        private void wifi2WifiFailed() {
            if (!(WifiProStateMachine.this.mNewSelect_bssid == null || WifiProStateMachine.this.mNewSelect_bssid.equals(WifiProStateMachine.this.mBadSsid))) {
                WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mNewSelect_bssid);
            }
            WifiProStateMachine.this.logD("wifi to Wifi Failed Finally!");
            this.isWifi2WifiProcess = false;
            if (WifiProStateMachine.this.isWifiConnected()) {
                if (WifiProStateMachine.this.mNeedRetryMonitor) {
                    WifiProStateMachine.this.logD("need retry dualband handover monitor");
                    WifiProStateMachine.this.retryDualBandAPMonitor();
                    WifiProStateMachine.this.mNeedRetryMonitor = false;
                }
                if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                    WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetAccessChange(true);
                }
                if (this.isWiFiHandoverPriority) {
                    WifiProStateMachine.this.logD("wifi handover wifi failed,continue monitor wifi Qos");
                    this.isWiFiHandoverPriority = false;
                    this.isSwitching = false;
                    return;
                }
                WifiProStateMachine.this.logD("wifi --> wifi is Failure, but wifi is connected, isMobileDataConnected() = " + WifiProStateMachine.this.isMobileDataConnected() + ",isAllowWiFiHandoverMobile =  " + this.isAllowWiFiHandoverMobile + " , mEmuiPdpSwichValue = " + WifiProStateMachine.this.mEmuiPdpSwichValue + ",mPowerManager.isScreenOn =" + WifiProStateMachine.this.mPowerManager.isScreenOn() + ", currWifiPoorlevel = " + this.currWifiPoorlevel);
                if (WifiProStateMachine.this.isAllowWifi2Mobile()) {
                    WifiProStateMachine.this.logD("try to wifi --> mobile,Query mobile Qos");
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(0, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                    return;
                }
                WifiProStateMachine.this.logD("wifi --> wifi is Failure,and can not handover to mobile ,delay 30s go to Monitor");
                if (WifiProStateMachine.this.isMobileDataConnected() && WifiProStateMachine.this.mPowerManager.isScreenOn() && WifiProStateMachine.this.mEmuiPdpSwichValue == 2 && !this.isCancelCHRTypeReport) {
                    this.isCancelCHRTypeReport = true;
                    if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                        WifiProStateMachine.this.logD("call increaseNotInetSettingCancelCount.");
                        WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetSettingCancelCount();
                    } else {
                        WifiProStateMachine.this.logD("call increaseBQE_BadSettingCancelCount.");
                        WifiProStateMachine.this.mWifiProStatisticsManager.increaseBQE_BadSettingCancelCount();
                    }
                }
                if (WifiProStateMachine.this.mIsWiFiNoInternet && !this.isToastDisplayed) {
                    this.isToastDisplayed = true;
                    WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProToast(3);
                }
                WifiProStateMachine.this.setWifiMonitorEnabled(false);
                this.isSwitching = false;
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_REINITIALIZE_WIFI_MONITOR, 30000);
                if (WifiProStateMachine.this.mCurrentWifiConfig != null && WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetAccess && WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoInternetReason == 0) {
                    WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                    if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                    }
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                }
            } else {
                WifiProStateMachine.this.logD("wifi handover over Failed and system auto conning ap");
                if (!WifiProStateMachine.this.mIsWiFiNoInternet) {
                    WifiProStateMachine.this.logD("try to connect : " + WifiProStateMachine.this.mBadSsid);
                    WifiProStateMachine.this.mWifiHandover.connectWifiNetwork(WifiProStateMachine.this.mBadBssid);
                }
            }
        }

        private void dualBandhandoverFailed(int reason) {
            if (!(WifiProStateMachine.this.mNewSelect_bssid == null || WifiProStateMachine.this.mNewSelect_bssid.equals(WifiProStateMachine.this.mBadBssid))) {
                WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mAvailable5GAPSsid);
                WifiProStateMachine.this.mHwDualBandBlackListMgr.addWifiBlacklist(WifiProStateMachine.this.mAvailable5GAPSsid, false);
            }
            WifiProStateMachine.this.logD("dual band handover failed. reason = " + reason);
            if (reason == -7 && WifiProStateMachine.this.mAvailable5GAPBssid != null) {
                WifiProStateMachine.this.logD("dualBandhandoverFailed  mAvailable5GAPBssid = " + WifiProStateMachine.this.mAvailable5GAPBssid);
                WifiProDualBandApInfoRcd mRecrd = HwDualBandInformationManager.getInstance().getDualBandAPInfo(WifiProStateMachine.this.mAvailable5GAPBssid);
                if (mRecrd != null) {
                    mRecrd.isInBlackList = 1;
                    HwDualBandInformationManager.getInstance().updateAPInfo(mRecrd);
                }
            }
            this.isWifi2WifiProcess = false;
            if (WifiProStateMachine.this.isWifiConnected()) {
                this.isSwitching = false;
            } else {
                WifiProStateMachine.this.logD("wifi dual band handover over Failed and system auto connecting ap");
                WifiProStateMachine.this.mWifiHandover.connectWifiNetwork(WifiProStateMachine.this.mBadBssid);
            }
            WifiProStateMachine.this.reportDualbandStatisticsCHR(2);
        }

        private void updateWifiQosLevel(boolean isWiFiNoInternet, int qosLevel) {
            WifiProStateMachine.this.refreshConnectedNetWork();
            WifiProStateMachine.this.mWiFiProEvaluateController.addEvaluateRecords(WifiProStateMachine.this.mCurrWifiInfo, 1);
            WifiProStateMachine.this.logD("updateWifiQosLevel, mCurrentSsid: " + WifiProStateMachine.this.mCurrentSsid + " ,isWiFiNoInternet: " + isWiFiNoInternet + ", qosLevel: " + qosLevel);
            if (isWiFiNoInternet) {
                WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 2);
                WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, 2, WifiProStateMachine.this.mCurrentSsid);
                return;
            }
            WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 4);
            WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoLevel(WifiProStateMachine.this.mCurrentSsid, qosLevel);
            WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 4, qosLevel, 0);
        }

        private boolean handleMsgBySwitchOrDialogStatus(int level) {
            if (!this.isSwitching || !this.isDialogDisplayed) {
                return this.isSwitching;
            } else {
                if (level > 2 && level != 6) {
                    WifiProStateMachine.this.logD("Dialog is  Displayed, Qos is" + level + ", Cancel dialog.");
                    WifiProStateMachine.this.updateWifiInternetStateChange(level);
                    WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
                    WifiProStateMachine.this.mIsWiFiNoInternet = false;
                    wiFiLinkMonitorStateInit();
                }
                return true;
            }
        }

        private boolean pendingMsgBySelfCureEngine(int level) {
            if (level == WifiproUtils.REQUEST_WIFI_INET_CHECK && !WifiProCommonUtils.isOpenType(WifiProStateMachine.this.mCurrentWifiConfig)) {
                this.internetFailureDetectedCnt++;
                if (HwSelfCureEngine.getInstance().isSelfCureOngoing()) {
                    WifiProStateMachine.this.logD("rcv EVENT_WIFI_QOS_CHANGE, level = " + level + ", but ignored because of wifi self cure ongoing.");
                    return true;
                } else if (this.internetFailureDetectedCnt == 1 && !HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(WifiProStateMachine.this.mContext) && WifiProStateMachine.this.mCurrentRssi >= -75) {
                    HwSelfCureEngine.getInstance().notifyInternetFailureDetected(false);
                    WifiProStateMachine.this.logD("rcv EVENT_WIFI_QOS_CHANGE, level = " + level + ", but ignored because of requesting self cure.");
                    return true;
                }
            }
            return false;
        }
    }

    class WiFiProDisabledState extends State {
        WiFiProDisabledState() {
        }

        public void enter() {
            WifiProStateMachine.this.logD("WiFiProDisabledState is Enter");
            WifiProStateMachine.mIsWifiManualEvaluating = false;
            WifiProStateMachine.mIsWifiSemiAutoEvaluating = false;
            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
            WifiProStateMachine.this.mNetworkQosMonitor.stopBqeService();
            WifiProStateMachine.this.unRegisterCallBack();
            WifiProStateMachine.this.mSampleCollectionManager.unRegisterBroadcastReceiver();
            WifiProStateMachine.this.mWiFiProProtalController.handleWifiProStatusChanged(false, WifiProStateMachine.this.mIsPortalAp);
            WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
            WifiProStateMachine.this.mWifiProUIDisplayManager.shownAccessNotification(false);
            WifiProStateMachine.this.mWiFiProEvaluateController.cleanEvaluateRecords();
            WifiProStateMachine.this.mHwIntelligenceWiFiManager.stop();
            WifiProStateMachine.this.mNetworkQosMonitor.setWifiWatchDogEnabled(false);
            WifiProStateMachine.this.stopDualBandManager();
            if (WifiProStateMachine.this.isWifiConnected()) {
                WifiProStateMachine.this.logD("WiFiProDisabledState , wifi is connect ");
                WifiInfo cInfo = WifiProStateMachine.this.mWifiManager.getConnectionInfo();
                if (cInfo != null && SupplicantState.COMPLETED == cInfo.getSupplicantState() && DetailedState.OBTAINING_IPADDR == WifiInfo.getDetailedStateOf(SupplicantState.COMPLETED)) {
                    WifiProStateMachine.this.logD("wifi State == VERIFYING_POOR_LINK");
                    WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.GOOD_LINK_DETECTED);
                }
                WifiProStateMachine.this.setWifiCSPState(1);
            }
            WifiProStateMachine.this.resetVariables();
        }

        public void exit() {
            WifiProStateMachine.this.logD("WiFiProDisabledState is Exit");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.POOR_LINK_DETECTED /*131873*/:
                    WifiProStateMachine.this.logD("receive POOR_LINK_DETECTED sendMessageDelayed");
                    WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.GOOD_LINK_DETECTED);
                    break;
                case WifiProStateMachine.GOOD_LINK_DETECTED /*131874*/:
                case WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE /*136177*/:
                case WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION /*136186*/:
                    break;
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /*136169*/:
                    NetworkInfo networkInfo = (NetworkInfo) msg.obj.getParcelableExtra("networkInfo");
                    if (networkInfo == null || DetailedState.VERIFYING_POOR_LINK != networkInfo.getDetailedState()) {
                        if (networkInfo == null || NetworkInfo.State.CONNECTING != networkInfo.getState()) {
                            if (networkInfo != null && NetworkInfo.State.CONNECTED == networkInfo.getState()) {
                                WifiProStateMachine.this.setWifiCSPState(1);
                                break;
                            }
                        }
                        WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                        break;
                    }
                    WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.GOOD_LINK_DETECTED);
                    break;
                    break;
                case WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE /*136171*/:
                    if (!WifiProStateMachine.this.mIsWiFiProEnabled || !WifiProStateMachine.this.mIsPrimaryUser) {
                        WifiProStateMachine.this.onDisableWiFiPro();
                        break;
                    }
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiProEnableState);
                    break;
                    break;
                case WifiProStateMachine.EVENT_WIFI_STATE_CHANGED_ACTION /*136185*/:
                    if (WifiProStateMachine.this.mWifiManager.getWifiState() == 3) {
                        WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_CONFIGURED_NETWORKS_CHANGED /*136308*/:
                    Intent confg_intent = msg.obj;
                    WifiConfiguration conn_cfg = (WifiConfiguration) confg_intent.getParcelableExtra("wifiConfiguration");
                    if (conn_cfg != null) {
                        int change_reason = confg_intent.getIntExtra("changeReason", -1);
                        if (conn_cfg.isTempCreated && change_reason != 1) {
                            WifiProStateMachine.this.logD("WiFiProDisabledState, forget " + conn_cfg.SSID);
                            WifiProStateMachine.this.mWifiManager.forget(conn_cfg.networkId, null);
                            break;
                        }
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class WiFiProEnableState extends State {
        WiFiProEnableState() {
        }

        public void enter() {
            WifiProStateMachine.this.logD("WiFiProEnableState is Enter");
            WifiProStateMachine.this.mIsWiFiNoInternet = false;
            WifiProStateMachine.this.mWiFiProPdpSwichValue = 0;
            WifiProStateMachine.this.registerCallBack();
            WifiProStateMachine.this.mNetworkQosMonitor.setWifiWatchDogEnabled(true);
            WifiProStateMachine.mIsWifiManualEvaluating = false;
            WifiProStateMachine.mIsWifiSemiAutoEvaluating = false;
            WifiProStateMachine.this.mIsWifiSemiAutoEvaluateComplete = false;
            if (WifiProStateMachine.this.mIsWiFiProEnabled) {
                WifiProStateMachine.this.startDualBandManager();
                WifiProStateMachine.this.mHwIntelligenceWiFiManager.start();
                WifiProStateMachine.this.mSampleCollectionManager.registerBroadcastReceiver();
                WifiProStateMachine.this.mWiFiProProtalController.handleWifiProStatusChanged(true, WifiProStateMachine.this.mIsPortalAp);
            }
            transitionNetState();
        }

        public void exit() {
            WifiProStateMachine.this.logD("WiFiProEnableState is Exit");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE /*136171*/:
                    if (!WifiProStateMachine.this.mIsWiFiProEnabled || !WifiProStateMachine.this.mIsPrimaryUser) {
                        WifiProStateMachine.this.onDisableWiFiPro();
                        break;
                    }
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiProEnableState);
                    break;
                case WifiProStateMachine.EVENT_WIFI_STATE_CHANGED_ACTION /*136185*/:
                    if (WifiProStateMachine.this.mWifiManager.getWifiState() != 1) {
                        if (WifiProStateMachine.this.mWifiManager.getWifiState() == 3) {
                            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                            WifiProStateMachine.this.mWiFiProEvaluateController.initWifiProEvaluateRecords();
                            break;
                        }
                    }
                    WifiProStateMachine.this.logD("wifi state is DISABLED, go to wifi disconnected");
                    if (0 != WifiProStateMachine.this.mChrRoveOutStartTime) {
                        WifiProStateMachine.this.logD("BQE bad rove out, wifi disable time recorded.");
                        WifiProStateMachine.this.mChrWifiDidableStartTime = System.currentTimeMillis();
                    }
                    WifiProStateMachine.this.mWifiProUIDisplayManager.shownAccessNotification(false);
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                    break;
                    break;
                case WifiProStateMachine.EVENT_SCAN_RESULTS_AVAILABLE /*136293*/:
                    if (!WifiProStateMachine.this.mIsManualConnectedWiFi) {
                        if (WifiProStateMachine.this.isAllowWiFiAutoEvaluate() && WifiProStateMachine.this.mPowerManager.isScreenOn() && WifiProStateMachine.this.mWifiManager.isWifiEnabled()) {
                            NetworkInfo wifi_info = HwServiceFactory.getHwConnectivityManager().getNetworkInfoForWifi();
                            if (wifi_info != null) {
                                WifiProStateMachine.this.mScanResultList = WifiProStateMachine.this.mWifiManager.getScanResults();
                                WifiProStateMachine.this.mScanResultList = WifiProStateMachine.this.mWiFiProEvaluateController.scanResultListFilter(WifiProStateMachine.this.mScanResultList);
                                if (!(WifiProStateMachine.this.mScanResultList == null || WifiProStateMachine.this.mScanResultList.size() == 0)) {
                                    boolean issetting = WifiProStateMachine.this.isSettingsActivity();
                                    int evaluate_type = 0;
                                    if (issetting) {
                                        evaluate_type = 1;
                                    }
                                    if (wifi_info.getDetailedState() == DetailedState.DISCONNECTED) {
                                        if (!WifiProStateMachine.this.isMapNavigating && !WifiProStateMachine.this.isVehicleState) {
                                            if (!WifiProStateMachine.this.mIsP2PConnectedOrConnecting) {
                                                if (!WifiProStateMachine.this.mWiFiProEvaluateController.isAllowAutoEvaluate(WifiProStateMachine.this.mScanResultList)) {
                                                    WifiProStateMachine.this.mWiFiProEvaluateController.updateEvaluateRecords(WifiProStateMachine.this.mScanResultList, evaluate_type, WifiProStateMachine.this.mCurrentSsid);
                                                    break;
                                                }
                                                for (ScanResult scanResult : WifiProStateMachine.this.mScanResultList) {
                                                    if (WifiProStateMachine.this.mWiFiProEvaluateController.isAllowEvaluate(scanResult, evaluate_type) && !WifiProStateMachine.this.mWiFiProEvaluateController.isLastEvaluateValid(scanResult, evaluate_type)) {
                                                        WifiProStateMachine.this.mWiFiProEvaluateController.addEvaluateRecords(scanResult, evaluate_type);
                                                    }
                                                }
                                                WifiProStateMachine.this.mWiFiProEvaluateController.orderByRssi();
                                                if (!WifiProStateMachine.this.mWiFiProEvaluateController.isUnEvaluateAPRecordsEmpty()) {
                                                    WifiProStateMachine.this.mWiFiProEvaluateController.unEvaluateAPQueueDump();
                                                    WifiProStateMachine.this.logD("transition to mwifiSemiAutoEvaluateState, to evaluate ap");
                                                    if (issetting) {
                                                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(2);
                                                    } else {
                                                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(1);
                                                    }
                                                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                                                    break;
                                                }
                                                WifiProStateMachine.this.logD("UnEvaluateAPRecords is Empty");
                                                break;
                                            }
                                            WifiProStateMachine.this.logD("P2PConnectedOrConnecting, ignor this scan result");
                                            WifiProStateMachine.this.mWiFiProEvaluateController.updateEvaluateRecords(WifiProStateMachine.this.mScanResultList, evaluate_type, WifiProStateMachine.this.mCurrentSsid);
                                            break;
                                        }
                                        WifiProStateMachine.this.logD("MapNavigatingOrVehicleState, ignor this scan result");
                                        break;
                                    }
                                    WifiProStateMachine.this.mWiFiProEvaluateController.updateEvaluateRecords(WifiProStateMachine.this.mScanResultList, evaluate_type, WifiProStateMachine.this.mCurrentSsid);
                                    break;
                                }
                            }
                        }
                    }
                    WifiProStateMachine.this.logD("mIsManualConnectedWiFi, ignor this scan result");
                    break;
                    break;
                case WifiProStateMachine.EVENT_WIFIPRO_EVALUTE_STATE_CHANGE /*136298*/:
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiProEnableState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void transitionNetState() {
            if (WifiProStateMachine.this.isWifiConnected()) {
                WifiProStateMachine.this.logD("WiFiProEnableState,go to WifiConnectedState");
                WifiProStateMachine.this.mIsWiFiProConnected = false;
                WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                return;
            }
            WifiProStateMachine.this.logD("WiFiProEnableState, go to mWifiDisConnectedState");
            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
        }
    }

    class WiFiProVerfyingLinkState extends State {
        private volatile boolean isRecoveryWifi;
        private boolean isWifiGoodIntervalTimerOut;
        private volatile boolean isWifiHandoverWifi;
        private boolean isWifiRecoveryTimerOut;
        private boolean isWifiScanScreenOff;
        private int wifiQosLevel;
        private int wifiScanCounter;
        private int wifi_internet_level;

        WiFiProVerfyingLinkState() {
        }

        private void startScanAndMonitor(long time) {
            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI, 120000);
            WifiProStateMachine.this.mNetworkQosMonitor.setIpQosEnabled(true);
            WifiProStateMachine.this.mNetworkQosMonitor.setMonitorMobileQos(true);
            if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                WifiProStateMachine.this.mCurrentWifiLevel = -1;
                WifiProStateMachine.this.logD("WiFiProVerfyingLinkState, wifi is No Internet,delay check time = " + time);
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, time);
                return;
            }
            WifiProStateMachine.this.mNetworkQosMonitor.setMonitorWifiQos(2, true);
        }

        private void cancelScanAndMonitor() {
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI);
            WifiProStateMachine.this.mNetworkQosMonitor.setIpQosEnabled(false);
            WifiProStateMachine.this.mNetworkQosMonitor.setMonitorMobileQos(false);
            WifiProStateMachine.this.mNetworkQosMonitor.setMonitorWifiQos(2, false);
        }

        private void restoreWifiConnect() {
            cancelScanAndMonitor();
            WifiProStateMachine.this.logD("restoreWifiConnect, mWsmChannel send GOOD Link Detected");
            WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetWorkHandover(2);
            WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.GOOD_LINK_DETECTED);
        }

        public void enter() {
            WifiProStateMachine.this.logD("WiFiProVerfyingLinkState is Enter");
            this.isRecoveryWifi = false;
            this.isWifiHandoverWifi = false;
            this.isWifiGoodIntervalTimerOut = true;
            WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
            this.wifiScanCounter = 0;
            this.isWifiScanScreenOff = false;
            if (WifiProStateMachine.this.mCurrentVerfyCounter > 4) {
                WifiProStateMachine.this.mCurrentVerfyCounter = 4;
            }
            long delay_time = (((long) Math.pow(2.0d, (double) WifiProStateMachine.this.mCurrentVerfyCounter)) * 60) * 1000;
            WifiProStateMachine.this.logD("WiFiProVerfyingLinkState : CurrentWifiLevel = " + WifiProStateMachine.this.mCurrentWifiLevel + ", CurrentVerfyCounter = " + WifiProStateMachine.this.mCurrentVerfyCounter + ", delay_time = " + delay_time);
            WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
            wifiProStateMachine.mCurrentVerfyCounter = wifiProStateMachine.mCurrentVerfyCounter + 1;
            startScanAndMonitor(delay_time);
            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WIFI_RECOVERY_TIMEOUT, delay_time);
            WifiProStateMachine.this.mSampleCollectionManager.notifyWifiDisconnected(WifiProStateMachine.this.mIsPortalAp);
            if (WifiProStateMachine.this.mCurrentVerfyCounter == 3) {
                WifiProStateMachine.this.logW("network has handover 3 times,maybe ping-pong");
                WifiProStateMachine.this.mWifiProStatisticsManager.increasePingPongCount();
            }
            WifiProStateMachine.this.mNetworkQosMonitor.setRoveOutToMobileState(1);
            if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                WifiProStateMachine.this.mLoseInetRoveOut = true;
            } else {
                WifiProStateMachine.this.logD("BQE bad rove out started.");
                WifiProStateMachine.this.mChrRoveOutStartTime = System.currentTimeMillis();
                WifiProStateMachine.this.mRoSsid = WifiProStateMachine.this.mCurrentSsid;
                WifiProStateMachine.this.mLoseInetRoveOut = false;
            }
            WifiProStateMachine.this.mRoveOutStarted = true;
            WifiProStateMachine.this.mIsRoveOutToDisconn = false;
            WifiProStateMachine.this.reportDualbandVerifyCHR();
        }

        public void exit() {
            WifiProStateMachine.this.logD("WiFiProVerfyingLinkState is Exit");
            cancelScanAndMonitor();
            WifiProStateMachine.this.mIsWiFiNoInternet = false;
            this.isWifiRecoveryTimerOut = false;
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_GOOD_INTERVAL_TIMEOUT);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_RECOVERY_TIMEOUT);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_MOBILE_SWITCH_DELAY);
            WifiProStateMachine.this.mNetworkQosMonitor.setRoveOutToMobileState(0);
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /*136169*/:
                    Intent intent = msg.obj;
                    if (!(intent == null || this.isWifiHandoverWifi)) {
                        NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                        if (networkInfo != null) {
                            WifiProStateMachine.this.logD("WiFiProVerfyingLinkState :Network state change " + networkInfo.getDetailedState());
                        }
                        if (networkInfo == null || NetworkInfo.State.DISCONNECTED != networkInfo.getState()) {
                            if (networkInfo != null && NetworkInfo.State.CONNECTED == networkInfo.getState() && WifiProStateMachine.this.isWifiConnected()) {
                                this.isRecoveryWifi = false;
                                WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProToast(4);
                                WifiProStateMachine.this.logD("WiFiProVerfyingLinkState: Restore the wifi connection successful,go to mWiFiLinkMonitorState");
                                WifiProStateMachine.this.mIsWiFiProConnected = true;
                                WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                                break;
                            }
                        }
                        WifiProStateMachine.this.logD("WiFiProVerfyingLinkState : wifi has disconnected");
                        WifiProStateMachine.this.mIsRoveOutToDisconn = true;
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_DEVICE_SCREEN_ON /*136170*/:
                    if (!this.isWifiScanScreenOff) {
                        WifiProStateMachine.this.logD("isWifiScanScreenOff = false, wait a moment, retry scan wifi later");
                        break;
                    }
                    WifiProStateMachine.this.logD("isWifiScanScreenOff = true, retry scan wifi");
                    this.isWifiScanScreenOff = false;
                    this.wifiScanCounter = 0;
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI);
                    break;
                case WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE /*136171*/:
                    if (!(WifiProStateMachine.this.mIsWiFiProEnabled && WifiProStateMachine.this.mIsPrimaryUser)) {
                        WifiProStateMachine.this.logD("wifiprochr user close wifipro");
                        WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(5);
                        WifiProStateMachine.this.mRoveOutStarted = false;
                        WifiProStateMachine.this.onDisableWiFiPro();
                        break;
                    }
                case WifiProStateMachine.EVENT_WIFI_QOS_CHANGE /*136172*/:
                    if (!this.isWifiHandoverWifi && this.isWifiRecoveryTimerOut) {
                        this.wifiQosLevel = msg.arg1;
                        if (this.wifiQosLevel > 2 && !this.isRecoveryWifi && !WifiProStateMachine.this.mIsWiFiNoInternet && this.isWifiGoodIntervalTimerOut) {
                            WifiProStateMachine.this.logD("wifi Qos is [" + this.wifiQosLevel + " ]Ok, start check wifi internet");
                            this.isRecoveryWifi = true;
                            this.isWifiGoodIntervalTimerOut = false;
                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WIFI_GOOD_INTERVAL_TIMEOUT, 30000);
                            WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                            break;
                        }
                    }
                    WifiProStateMachine.this.logD("isWifiHandoverWifi = " + this.isWifiHandoverWifi + ", isWifiRecoveryTimerOut = " + this.isWifiRecoveryTimerOut);
                    break;
                    break;
                case WifiProStateMachine.EVENT_MOBILE_QOS_CHANGE /*136173*/:
                    if (msg.arg1 <= 2 && this.isWifiRecoveryTimerOut) {
                        if (!WifiProStateMachine.this.mIsWiFiNoInternet || msg.arg1 > 0) {
                            if (!(this.isRecoveryWifi || WifiProStateMachine.this.isWiFiPoorer(WifiProStateMachine.this.mCurrentWifiLevel, msg.arg1) || this.isWifiHandoverWifi)) {
                                WifiProStateMachine.this.logD("Mobile Qos is poor,try restore wifi,mobile handover wifi");
                                this.isRecoveryWifi = true;
                                restoreWifiConnect();
                                WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(7);
                                break;
                            }
                        }
                        WifiProStateMachine.this.logD("both wifi and mobile is unusable,can not restore wifi ");
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_CHECK_AVAILABLE_AP_RESULT /*136176*/:
                    if (!this.isRecoveryWifi && !this.isWifiHandoverWifi) {
                        if (!((Boolean) msg.obj).booleanValue()) {
                            WifiProStateMachine.this.logD("There is no vailble ap, continue verfyinglink");
                            break;
                        }
                        WifiProStateMachine.this.logD("Exist a vailable AP,connect this AP and cancel Sacn Timer");
                        this.isWifiHandoverWifi = true;
                        if (!WifiProStateMachine.this.mWifiHandover.handleWifiToWifi(WifiProStateMachine.this.mNetworkBlackListManager.getWifiBlacklist(), WifiProStateMachine.THRESHOD_RSSI, 0)) {
                            this.isWifiHandoverWifi = false;
                            break;
                        }
                    }
                    WifiProStateMachine.this.logD("receive check available ap result,but is isRecoveryWifi");
                    break;
                    break;
                case WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE /*136177*/:
                    NetworkInfo mobileInfo = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(0);
                    WifiProStateMachine.this.logD("networkConnetc change :mobileInfo : " + mobileInfo + ", mIsMobileDataEnabled = " + WifiProStateMachine.this.mIsMobileDataEnabled);
                    if (WifiProStateMachine.this.mIsMobileDataEnabled && mobileInfo != null && NetworkInfo.State.DISCONNECTED == mobileInfo.getState()) {
                        WifiProStateMachine.this.logD("mobile network service is disconnected, mIsWiFiNoInternet = " + WifiProStateMachine.this.mIsWiFiNoInternet);
                        NetworkInfo wInfo = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(1);
                        if (!(WifiProStateMachine.this.mIsWiFiNoInternet || wInfo == null || DetailedState.VERIFYING_POOR_LINK != wInfo.getDetailedState())) {
                            this.isWifiHandoverWifi = false;
                            restoreWifiConnect();
                            break;
                        }
                    }
                case WifiProStateMachine.EVENT_WIFI_HANDOVER_WIFI_RESULT /*136178*/:
                    this.isWifiHandoverWifi = false;
                    if (!((Boolean) msg.obj).booleanValue()) {
                        if (WifiProStateMachine.this.mNewSelect_bssid == null || WifiProStateMachine.this.mNewSelect_bssid.equals(WifiProStateMachine.this.mCurrentSsid)) {
                            if (WifiProStateMachine.this.isWifiConnected()) {
                                WifiProStateMachine.this.logD("wifi handover wifi fail, continue monitor");
                                break;
                            }
                        }
                        WifiProStateMachine.this.logW("connect other AP wifi : Fallure");
                        WifiProStateMachine.this.mNetworkBlackListManager.addWifiBlacklist(WifiProStateMachine.this.mNewSelect_bssid);
                        WifiProStateMachine.this.mWifiHandover.connectWifiNetwork(WifiProStateMachine.this.mCurrentBssid);
                        break;
                    }
                    WifiProStateMachine.this.logD("connect other AP wifi : succeed ,go to WifiConnectedState");
                    cancelScanAndMonitor();
                    WifiProStateMachine.this.mIsWiFiProConnected = true;
                    WifiProStateMachine.this.refreshConnectedNetWork();
                    WifiProStateMachine.this.mWiFiProEvaluateController.reSetEvaluateRecord(WifiProStateMachine.this.mCurrentSsid);
                    WifiProStateMachine.this.mWifiProConfigStore.cleanWifiProConfig(WifiProStateMachine.this.mCurrentWifiConfig);
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                    break;
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT /*136181*/:
                    this.wifi_internet_level = msg.arg1;
                    WifiProStateMachine.this.logD("WiFi internet level = " + this.wifi_internet_level + ", wifiQosLevel = " + this.wifiQosLevel);
                    WifiProStateMachine.this.logD("mIsWiFiNoInternet = " + WifiProStateMachine.this.mIsWiFiNoInternet + " ,isWifiHandoverWifi = " + this.isWifiHandoverWifi + ", isWifiRecoveryTimerOut = " + this.isWifiRecoveryTimerOut);
                    if (this.isWifiRecoveryTimerOut) {
                        if (!(WifiProStateMachine.this.mIsWiFiNoInternet || this.isWifiHandoverWifi || !this.isRecoveryWifi)) {
                            if (this.wifi_internet_level == -1 || this.wifi_internet_level == 6 || this.wifiQosLevel <= 2) {
                                this.isRecoveryWifi = false;
                            } else {
                                WifiProStateMachine.this.logD("wifi Qos is [" + this.wifiQosLevel + " ]Ok, wifi_internet_level is [" + this.wifi_internet_level + "] Restore the wifi connection");
                                WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(1);
                                WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 4);
                                WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoLevel(WifiProStateMachine.this.mCurrentSsid, 3);
                                WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 4, 3, 0);
                                restoreWifiConnect();
                            }
                        }
                        if (!(!WifiProStateMachine.this.mIsWiFiNoInternet || this.wifi_internet_level == -1 || this.wifi_internet_level == 6 || this.isWifiHandoverWifi)) {
                            WifiProStateMachine.this.mIsWiFiNoInternet = false;
                            if (!this.isRecoveryWifi) {
                                this.isRecoveryWifi = true;
                                WifiProStateMachine.this.logD("wifi Internet is better ,try restore wifi 2,mobile handover wifi");
                                WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetRestoreRICount();
                                WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 4);
                                WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, 4, WifiProStateMachine.this.mCurrentSsid);
                                restoreWifiConnect();
                                break;
                            }
                        }
                    }
                    break;
                case WifiProStateMachine.EVENT_DIALOG_OK /*136182*/:
                case WifiProStateMachine.EVENT_DIALOG_CANCEL /*136183*/:
                case WifiProStateMachine.EVENT_SCAN_RESULTS_AVAILABLE /*136293*/:
                    break;
                case WifiProStateMachine.EVENT_WIFI_STATE_CHANGED_ACTION /*136185*/:
                    if (WifiProStateMachine.this.mWifiManager.getWifiState() != 1) {
                        if (WifiProStateMachine.this.mWifiManager.getWifiState() == 3) {
                            WifiProStateMachine.this.logD("wifi state is : enabled, forgetUntrustedOpenAp");
                            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                            break;
                        }
                    }
                    WifiProStateMachine.this.logD("wifi state is : " + WifiProStateMachine.this.mWifiManager.getWifiState() + " ,go to wifi disconnected");
                    WifiProStateMachine.this.mIsRoveOutToDisconn = true;
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                    break;
                    break;
                case WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION /*136186*/:
                    if (WifiProStateMachine.this.mIsMobileDataEnabled) {
                        if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_MOBILE_RECOVERY_TO_WIFI)) {
                            WifiProStateMachine.this.logD("In verifying link state, MOBILE DATA is ON within delay time, cancel switching back to wifi.");
                            WifiProStateMachine.this.getHandler().removeMessages(WifiProStateMachine.EVENT_MOBILE_RECOVERY_TO_WIFI);
                            break;
                        }
                    }
                    int delayMs = WifiProStateMachine.this.mIsWiFiNoInternet ? HwSelfCureUtils.SELFCURE_WIFI_ON_TIMEOUT : 0;
                    WifiProStateMachine.this.logD("In verifying link state, MOBILE DATA is OFF, try to delay " + delayMs + " ms to switch back to wifi.");
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.this.obtainMessage(WifiProStateMachine.EVENT_MOBILE_RECOVERY_TO_WIFI), (long) delayMs);
                    break;
                    break;
                case WifiProStateMachine.EVENT_WIFI_GOOD_INTERVAL_TIMEOUT /*136187*/:
                    this.isWifiGoodIntervalTimerOut = true;
                    break;
                case WifiProStateMachine.EVENT_WIFI_RECOVERY_TIMEOUT /*136188*/:
                    this.isWifiRecoveryTimerOut = true;
                    WifiProStateMachine.this.logD("isWifiRecoveryTimerOut is true,mobile can handover to wifi");
                    break;
                case WifiProStateMachine.EVENT_MOBILE_RECOVERY_TO_WIFI /*136189*/:
                    WifiProStateMachine.this.logW("WiFiProVerfyingLinkState::EVENT_MOBILE_RECOVERY_TO_WIFI, handle it.");
                    this.isWifiHandoverWifi = false;
                    restoreWifiConnect();
                    WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(3);
                    break;
                case WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI /*136191*/:
                    if (!WifiProStateMachine.this.mPowerManager.isScreenOn()) {
                        this.isWifiScanScreenOff = true;
                        break;
                    }
                    WifiProStateMachine.this.logD("inquire the surrounding AP for wifiHandover");
                    WifiProStateMachine.this.mWifiHandover.hasAvailableWifiNetwork(WifiProStateMachine.this.mNetworkBlackListManager.getWifiBlacklist(), WifiProStateMachine.THRESHOD_RSSI, WifiProStateMachine.this.mCurrentBssid, WifiProStateMachine.this.mCurrentSsid);
                    this.wifiScanCounter++;
                    this.wifiScanCounter = Math.min(this.wifiScanCounter, 12);
                    long delay_scan_time = (((long) Math.pow(2.0d, (double) (this.wifiScanCounter / 4))) * 60) * 1000;
                    WifiProStateMachine.this.logD("delay_scan_time = " + delay_scan_time);
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_RETRY_WIFI_TO_WIFI, delay_scan_time);
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET /*136192*/:
                    WifiProStateMachine.this.logW("start check wifi internet ");
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                    break;
                case WifiProStateMachine.EVENT_USER_ROVE_IN /*136193*/:
                    WifiProStateMachine.this.mIsUserHandoverWiFi = true;
                    this.isWifiHandoverWifi = false;
                    if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                        WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetUserManualRICount();
                    } else {
                        WifiProStateMachine.this.mWifiProStatisticsManager.sendWifiproRoveInEvent(4);
                    }
                    restoreWifiConnect();
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class WifiConnectedState extends State {
        private boolean isChrShouldReport;
        private boolean isDialogDisplay;
        private boolean isIgnorAvailableWifiCheck;
        private boolean isKeepConnected;
        private boolean isPortalAP;
        private boolean isToastDisplayed;
        private int oldType;
        private int portalCheckCounter;
        private int tcpRxStatisticsTimer;

        WifiConnectedState() {
        }

        private void initConnectedState() {
            WifiProStateMachine.this.setWifiEvaluateTag(false);
            WifiProStateMachine.this.mIsWiFiNoInternet = false;
            this.isKeepConnected = false;
            this.isPortalAP = false;
            this.portalCheckCounter = 0;
            this.tcpRxStatisticsTimer = 0;
            WifiProStateMachine.this.mIsScanedRssiLow = false;
            WifiProStateMachine.this.mIsScanedRssiMiddle = false;
            this.isIgnorAvailableWifiCheck = true;
            WifiProStateMachine.this.refreshConnectedNetWork();
            WifiProStateMachine.this.mLastWifiLevel = 0;
            WifiProStateMachine.this.setWifiCSPState(1);
            if (WifiProStateMachine.this.isKeepCurrWiFiConnected()) {
                WifiProStateMachine.this.refreshConnectedNetWork();
                WifiProStateMachine.this.mWifiProConfigStore.cleanWifiProConfig(WifiProStateMachine.this.mCurrentWifiConfig);
                WifiProStateMachine.this.mWiFiProEvaluateController.reSetEvaluateRecord(WifiProStateMachine.this.mCurrentSsid);
                WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetAccessChange(false);
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DIALOG_CANCEL);
            }
            WifiProStateMachine.this.logD("isAllowWiFiAutoEvaluate == " + WifiProStateMachine.this.isAllowWiFiAutoEvaluate());
            WifiConfiguration cfg = WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(WifiProStateMachine.this.mCurrentSsid);
            if (cfg != null) {
                int accessType = cfg.internetAccessType;
                WifiProStateMachine.this.logD("accessType = : " + accessType + ",qosLevel = " + cfg.networkQosLevel + ",wifiProNoInternetAccess = " + cfg.wifiProNoInternetAccess);
                if (cfg.isTempCreated) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(19);
                }
                if (4 == accessType) {
                    int temporaryQoeLevel = WifiProStateMachine.this.mNetworkQosMonitor.getCurrentWiFiLevel();
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoLevel(WifiProStateMachine.this.mCurrentSsid, temporaryQoeLevel);
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 4, temporaryQoeLevel, 0);
                    WifiProStateMachine.this.logD("WiFiProConnected temporaryQosLevel = " + temporaryQoeLevel);
                } else if (3 == accessType || 2 == accessType) {
                    WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetAccessChange(true);
                } else {
                    WiFiProScoreInfo wiFiProScoreInfo = WiFiProEvaluateController.getCurrentWiFiProScore(WifiProStateMachine.this.mCurrentSsid);
                    if (wiFiProScoreInfo != null && (3 == wiFiProScoreInfo.internetAccessType || 2 == wiFiProScoreInfo.internetAccessType)) {
                        WifiProStateMachine.this.logD("WiFiProConnected internetAccessType = " + wiFiProScoreInfo.internetAccessType);
                        WifiProStateMachine.this.mWifiProUIDisplayManager.notificateNetAccessChange(true);
                    }
                }
            } else {
                WifiProStateMachine.this.logD("cfg= null ");
            }
            if (WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                WifiProStateMachine.this.mWiFiProEvaluateController.addEvaluateRecords(WifiProStateMachine.this.mCurrWifiInfo, 1);
            }
        }

        private void showNoInternetDialog(int reason) {
            WifiProStateMachine.this.logD("showNoInternetDialog reason = " + reason);
            if (1 == reason) {
                WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProDialog(5);
            } else {
                WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProDialog(4);
            }
        }

        private void reportDiffTypeCHR(int newType) {
            if (!this.isChrShouldReport) {
                this.isChrShouldReport = true;
                WifiProStateMachine.this.mWiFiProEvaluateController.updateWifiProbeMode(WifiProStateMachine.this.mCurrentSsid, 0);
                int diffType = WifiProStateMachine.this.mWiFiProEvaluateController.getChrDiffType(this.oldType, newType);
                WifiProStateMachine.this.logD("reportDiffTypeCHR is Enter, diffType  == " + diffType);
                if (diffType != 0) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBG_AP_SSID(WifiProStateMachine.this.mCurrentSsid);
                    WifiProStateMachine.this.mWifiProStatisticsManager.increaseBG_AC_DiffType(diffType);
                }
                if (this.oldType == newType) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.increaseActiveCheckRS_Same();
                }
            }
        }

        public void enter() {
            WifiProStateMachine.this.logD("WifiConnectedState is Enter, WiFiProConnected == " + WifiProStateMachine.this.mIsWiFiProConnected);
            WifiProStateMachine.this.mWifiProUIDisplayManager.shownAccessNotification(false);
            if (!WifiProStateMachine.this.mIsWiFiProConnected) {
                WifiProStateMachine.this.refreshConnectedNetWork();
                this.oldType = WifiProStateMachine.this.mWiFiProEvaluateController.getOldNetworkType(WifiProStateMachine.this.mCurrentSsid);
                WifiProStateMachine.this.logD("WiFiProConnected oldType = " + this.oldType);
            }
            NetworkInfo wifi_info = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(1);
            if (wifi_info != null && wifi_info.getDetailedState() == DetailedState.VERIFYING_POOR_LINK) {
                WifiProStateMachine.this.logD(" POOR_LINK_DETECTED sendMessageDelayed");
                WifiProStateMachine.this.mWsmChannel.sendMessage(WifiProStateMachine.GOOD_LINK_DETECTED);
            }
            initConnectedState();
            WifiProStateMachine.this.initDualBandhanoverConnectedCHR();
        }

        public void exit() {
            WifiProStateMachine.this.logD("WifiConnectedState is Exit");
            this.isDialogDisplay = false;
            this.isToastDisplayed = false;
            this.isChrShouldReport = false;
            this.oldType = 0;
            WifiProStateMachine.this.mWifiProUIDisplayManager.cancelAllDialog();
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
            WifiProStateMachine.this.mSampleCollectionManager.setPortalAuthenticating(false);
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /*136169*/:
                    NetworkInfo n = (NetworkInfo) msg.obj.getParcelableExtra("networkInfo");
                    if (n != null && NetworkInfo.State.DISCONNECTED == n.getState()) {
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                        break;
                    }
                case WifiProStateMachine.EVENT_DEVICE_SCREEN_ON /*136170*/:
                case WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION /*136186*/:
                case WifiProStateMachine.EVENT_EMUI_CSP_SETTINGS_CHANGE /*136190*/:
                    if (WifiProStateMachine.this.mCurrentWifiConfig != null && WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoHandoverNetwork && WifiProStateMachine.this.mIsWiFiNoInternet && WifiProStateMachine.this.isAllowWifi2Mobile()) {
                        if (!this.isDialogDisplay) {
                            WifiProStateMachine.this.logD("surroundings change, allow wifi2mobile,should show intelligent dialog");
                            showNoInternetDialog(WifiProStateMachine.this.mWiFiNoInternetReason);
                            this.isDialogDisplay = true;
                        }
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mWiFiNoInternetReason, false);
                        break;
                    }
                case WifiProStateMachine.EVENT_WIFI_QOS_CHANGE /*136172*/:
                    handleWifiQosChangeWithConnected(msg);
                    break;
                case WifiProStateMachine.EVENT_CHECK_AVAILABLE_AP_RESULT /*136176*/:
                    if (!this.isIgnorAvailableWifiCheck) {
                        if (!WifiProStateMachine.this.mIsWiFiNoInternet || (!((Boolean) msg.obj).booleanValue() && !WifiProStateMachine.this.isAllowWifi2Mobile())) {
                            if (!this.isToastDisplayed) {
                                WifiProStateMachine.this.logW("There is no network can switch");
                                this.isToastDisplayed = true;
                                WifiProStateMachine.this.mWifiProUIDisplayManager.showWifiProToast(3);
                                WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mWiFiNoInternetReason, true);
                                if (WifiProStateMachine.this.mWiFiNoInternetReason != 0) {
                                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 60000);
                                    break;
                                }
                                WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                                if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                                    WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                                }
                                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                                break;
                            }
                        }
                        WifiProStateMachine.this.logD("AllowWifi2Mobile,should show intelligent dialog");
                        if (!this.isDialogDisplay) {
                            showNoInternetDialog(WifiProStateMachine.this.mWiFiNoInternetReason);
                            this.isDialogDisplay = true;
                        }
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mWiFiNoInternetReason, false);
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE /*136177*/:
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT /*136181*/:
                    WifiProStateMachine.this.logD("WiFi internet check level = " + msg.arg1 + ", isKeepConnected = " + this.isKeepConnected + ", mIsUserHandoverWiFi = " + WifiProStateMachine.this.mIsUserHandoverWiFi);
                    WifiProStateMachine.this.notifyNetworkCheckResult(msg.arg1);
                    reportDiffTypeCHR(WifiProStateMachine.this.mWiFiProEvaluateController.getNewNetworkType(msg.arg1));
                    WifiProStateMachine.this.reportDualbandInternetResultCHR(msg.arg1);
                    if (this.isKeepConnected || WifiProStateMachine.this.mIsUserHandoverWiFi) {
                        if (-1 == msg.arg1 || 6 == msg.arg1) {
                            if (WifiProStateMachine.this.mWiFiNoInternetReason != 0) {
                                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                                break;
                            }
                            WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                            if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                            }
                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                            break;
                        }
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, false, 0, false);
                        break;
                    }
                    this.isKeepConnected = false;
                    int internet_level = msg.arg1;
                    if (!this.isDialogDisplay || (internet_level != -1 && internet_level != 6)) {
                        if (this.isPortalAP) {
                            this.portalCheckCounter++;
                            WifiProStateMachine.this.logD("portalCheckCounter = " + this.portalCheckCounter);
                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                            if (this.portalCheckCounter < 20 && (internet_level == 6 || internet_level == -1)) {
                                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 15000);
                                break;
                            } else if (internet_level == 6 || internet_level == -1) {
                                internet_level = -1;
                                WifiProStateMachine.this.mWifiProStatisticsManager.increasePortalUnauthCount();
                            }
                        }
                        WifiProStateMachine.this.mSampleCollectionManager.setPortalAuthenticating(false);
                        if (internet_level != -1) {
                            if (internet_level != 6) {
                                this.isKeepConnected = false;
                                WifiProStateMachine.this.mIsWiFiNoInternet = false;
                                WifiProStateMachine.this.mIsNetworkAuthen = true;
                                WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 4);
                                WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, 4, WifiProStateMachine.this.mCurrentSsid);
                                WifiProStateMachine.this.mWiFiProProtalController.notifyPortalAuthenStatus(WifiProStateMachine.this.mIsPortalAp, true);
                                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiLinkMonitorState);
                                break;
                            }
                            WifiProStateMachine.this.logD("WifiConnectedState: WiFi is protal");
                            this.isPortalAP = true;
                            WifiProStateMachine.this.setWifiMonitorEnabled(true);
                            WifiProStateMachine.this.mIsPortalAp = true;
                            WifiProStateMachine.this.mIsNetworkAuthen = false;
                            WifiProStateMachine.this.mWiFiNoInternetReason = 1;
                            WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 3);
                            WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, 3, WifiProStateMachine.this.mCurrentSsid);
                            WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, true, WifiProStateMachine.this.mWiFiNoInternetReason, false);
                            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 15000);
                            if (WifiProStateMachine.this.mIsWiFiProEnabled && WifiProStateMachine.this.mIsPrimaryUser) {
                                WifiProStateMachine.this.mSampleCollectionManager.setPortalAuthenticating(true);
                                WifiProStateMachine.this.mSampleCollectionManager.notifyPortalConnected(WifiProStateMachine.this.mCurrentSsid, WifiProStateMachine.this.mCurrentBssid);
                                WifiProStateMachine.this.mWiFiProProtalController.notifyPortalConnected(WifiProStateMachine.this.mCurrentSsid, WifiProStateMachine.this.mCurrentBssid);
                                break;
                            }
                        }
                        WifiProStateMachine.this.logD("WiFi NO internet,isPortalAP = " + this.isPortalAP);
                        WifiProStateMachine.this.mIsWiFiNoInternet = true;
                        if (this.isPortalAP) {
                            this.isPortalAP = false;
                            WifiProStateMachine.this.mWiFiNoInternetReason = 1;
                            WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 3);
                            WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, 3, WifiProStateMachine.this.mCurrentSsid);
                        } else {
                            WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(WifiProStateMachine.this.mCurrentSsid, 2);
                            WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mCurrentWifiConfig, 1, 2, WifiProStateMachine.this.mCurrentSsid);
                            WifiProStateMachine.this.mWiFiNoInternetReason = 0;
                            WifiProStateMachine.this.mWifiProStatisticsManager.increaseNoInetRemindCount(true);
                        }
                        if (this.isIgnorAvailableWifiCheck) {
                            WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mWiFiNoInternetReason, false);
                        } else if (WifiProStateMachine.this.mCurrentWifiConfig != null) {
                            WifiProStateMachine.this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(WifiProStateMachine.this.mCurrentWifiConfig, WifiProStateMachine.this.mIsWiFiNoInternet, WifiProStateMachine.this.mWiFiNoInternetReason, WifiProStateMachine.this.mCurrentWifiConfig.wifiProNoHandoverNetwork);
                        }
                        if (!WifiProStateMachine.this.isNotShowIntelligentDialog()) {
                            if (!this.isIgnorAvailableWifiCheck) {
                                if (WifiProStateMachine.this.mWiFiNoInternetReason != 0) {
                                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                                    break;
                                }
                                WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                                if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                                    WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                                }
                                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                                break;
                            }
                            WifiProStateMachine.this.logD("inquire the surrounding AP for wifiHandover");
                            this.isIgnorAvailableWifiCheck = false;
                            WifiProStateMachine.this.mWifiHandover.hasAvailableWifiNetwork(WifiProStateMachine.this.mNetworkBlackListManager.getWifiBlacklist(), WifiProStateMachine.THRESHOD_RSSI, WifiProStateMachine.this.mCurrentBssid, WifiProStateMachine.this.mCurrentSsid);
                            break;
                        }
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiLinkMonitorState);
                        break;
                    }
                    WifiProStateMachine.this.logD("AP is noInternet or Protal AP , Continue DisplayDialog");
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                    break;
                    break;
                case WifiProStateMachine.EVENT_DIALOG_OK /*136182*/:
                    WifiProStateMachine.this.logD("Intelligent choice other network,go to  mWiFiLinkMonitorState");
                    WifiProStateMachine.this.mIsWiFiNoInternet = true;
                    this.isKeepConnected = false;
                    WifiProStateMachine.this.mWiFiProProtalController.notifyPortalAuthenStatus(WifiProStateMachine.this.mIsPortalAp, false);
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiLinkMonitorState);
                    break;
                case WifiProStateMachine.EVENT_DIALOG_CANCEL /*136183*/:
                    handleDialogCancel();
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET /*136192*/:
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                    break;
                case WifiProStateMachine.EVENT_HTTP_REACHABLE_RESULT /*136195*/:
                    if (msg.obj == null || !((Boolean) msg.obj).booleanValue() || !WifiProStateMachine.this.hasMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET)) {
                        if (!(msg.obj == null || ((Boolean) msg.obj).booleanValue())) {
                            WifiProStateMachine.this.logD("EVENT_HTTP_REACHABLE_RESULT, SCE notify WLAN+ the http unreachable.");
                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT, -1);
                            break;
                        }
                    }
                    WifiProStateMachine.this.logD("EVENT_HTTP_REACHABLE_RESULT, SCE notify WLAN+ to check wifi immediately.");
                    WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET);
                    break;
                    break;
                case WifiProStateMachine.EVENT_WIFI_SEMIAUTO_EVALUTE_CHANGE /*136300*/:
                    if (WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                        WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_WIFI_CHECK_UNKOWN /*136309*/:
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                    break;
                case WifiProStateMachine.EVENT_GET_WIFI_TCPRX /*136311*/:
                    int currentWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                    WifiProStateMachine.this.logD("current rx= " + currentWifiTcpRxCount + ", last rx = " + WifiProStateMachine.this.mWifiTcpRxCount);
                    if (currentWifiTcpRxCount - WifiProStateMachine.this.mWifiTcpRxCount <= 0) {
                        WifiProStateMachine.this.logD("tcpRxStatisticsTimer = " + this.tcpRxStatisticsTimer + ", % 10 = " + (this.tcpRxStatisticsTimer % 10));
                        if (this.tcpRxStatisticsTimer > 0 && this.tcpRxStatisticsTimer % 10 == 0) {
                            this.tcpRxStatisticsTimer = 0;
                            WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                            break;
                        }
                        if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                        }
                        this.tcpRxStatisticsTimer++;
                        WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                        break;
                    }
                    this.tcpRxStatisticsTimer = 0;
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                    break;
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void handleDialogCancel() {
            WifiProStateMachine.this.logD("Keep this network,do nothing!!!");
            this.isIgnorAvailableWifiCheck = true;
            WifiProStateMachine.this.mNetworkQosMonitor.stopBqeService();
            this.isKeepConnected = true;
            if (WifiProStateMachine.this.mIsWiFiNoInternet) {
                if (WifiProStateMachine.this.mWiFiNoInternetReason == 0) {
                    WifiProStateMachine.this.mWifiTcpRxCount = WifiProStateMachine.this.mNetworkQosMonitor.requestTcpRxPacketsCounter();
                    if (WifiProStateMachine.this.getHandler().hasMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX)) {
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_GET_WIFI_TCPRX);
                    }
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_GET_WIFI_TCPRX, PortalAutoFillManager.AUTO_FILL_PW_DELAY_MS);
                } else {
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET, 30000);
                }
            }
            if (this.isDialogDisplay && WifiProStateMachine.this.mIsWiFiNoInternet) {
                WifiProStateMachine.this.mWifiProStatisticsManager.increaseNotInetUserCancelCount();
            }
        }

        private void handleWifiQosChangeWithConnected(Message msg) {
            if (this.isPortalAP && msg.arg1 <= 2) {
                WifiProStateMachine.this.logD("handleWifiQosChangeWithConnected, PortalAP Network Link Poor!");
                this.portalCheckCounter = 20;
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT, -1);
            }
        }
    }

    class WifiDisConnectedState extends State {
        WifiDisConnectedState() {
        }

        public void enter() {
            WifiProStateMachine.mIsWifiManualEvaluating = false;
            WifiProStateMachine.mIsWifiSemiAutoEvaluating = false;
            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
            WifiProStateMachine.this.setWifiEvaluateTag(false);
            if (WifiProStateMachine.this.mOpenAvailableAPCounter >= 2) {
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(10);
                WifiProStateMachine.this.mOpenAvailableAPCounter = 0;
            }
            WifiProStateMachine.this.logD("WifiDisConnectedState is Enter");
            if (WifiProStateMachine.this.mIsWiFiProEnabled && WifiProStateMachine.this.mIsPrimaryUser) {
                WifiProStateMachine.this.mWiFiProProtalController.handleWifiDisconnected(WifiProStateMachine.this.mIsPortalAp);
                WifiProStateMachine.this.mSampleCollectionManager.notifyWifiDisconnected(WifiProStateMachine.this.mIsPortalAp);
            }
            WifiProStateMachine.this.mIsPortalAp = false;
            WifiProStateMachine.this.mIsNetworkAuthen = false;
            WifiProStateMachine.this.resetVariables();
            if (0 != WifiProStateMachine.this.mChrRoveOutStartTime) {
                WifiProStateMachine.this.logD("BQE bad rove out, disconnect time recorded.");
                WifiProStateMachine.this.mChrWifiDisconnectStartTime = System.currentTimeMillis();
            }
            if (WifiProStateMachine.this.mRoveOutStarted && WifiProStateMachine.this.mIsRoveOutToDisconn) {
                if (WifiProStateMachine.this.mLoseInetRoveOut) {
                    WifiProStateMachine.this.logD("Not Inet rove out and WIFI disconnect.");
                    WifiProStateMachine.this.mWifiProStatisticsManager.accuNotInetRoDisconnectData();
                } else {
                    WifiProStateMachine.this.logD("Qoe bad rove out and WIFI disconnect.");
                    WifiProStateMachine.this.mWifiProStatisticsManager.accuQOEBadRoDisconnectData();
                }
            }
            WifiProStateMachine.this.mRoveOutStarted = false;
            WifiProStateMachine.this.mIsRoveOutToDisconn = false;
        }

        public void exit() {
            WifiProStateMachine.this.logD("WifiDisConnectedState is Exit");
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /*136169*/:
                    NetworkInfo networkInfo = (NetworkInfo) msg.obj.getParcelableExtra("networkInfo");
                    if (networkInfo == null || NetworkInfo.State.CONNECTED != networkInfo.getState() || !WifiProStateMachine.this.isWifiConnected()) {
                        if (networkInfo != null && NetworkInfo.State.CONNECTING == networkInfo.getState()) {
                            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                            break;
                        }
                    }
                    WifiProStateMachine.this.logD("WifiDisConnectedState: wifi connect,to go connected");
                    WifiProStateMachine.this.mIsWiFiProConnected = false;
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                    break;
                    break;
                case WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION /*136186*/:
                    if (!WifiProStateMachine.this.isMobileDataConnected()) {
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_CONFIGURED_NETWORKS_CHANGED /*136308*/:
                    Intent confg_intent = msg.obj;
                    WifiConfiguration conn_cfg = (WifiConfiguration) confg_intent.getParcelableExtra("wifiConfiguration");
                    if (conn_cfg != null) {
                        int change_reason = confg_intent.getIntExtra("changeReason", -1);
                        WifiProStateMachine.this.logD("WifiDisConnectedState, change reson " + change_reason + ", isTempCreated = " + conn_cfg.isTempCreated);
                        if (conn_cfg.isTempCreated && change_reason != 1) {
                            WifiProStateMachine.this.logD("WifiDisConnectedState, forget " + conn_cfg.SSID);
                            WifiProStateMachine.this.mWifiManager.forget(conn_cfg.networkId, null);
                            break;
                        }
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class WifiSemiAutoEvaluateState extends State {
        WifiSemiAutoEvaluateState() {
        }

        public void enter() {
            WifiProStateMachine.this.logD("WifiSemiAutoEvaluateState enter");
            WifiProStateMachine.this.setWifiCSPState(0);
            if (!WifiProStateMachine.mIsWifiSemiAutoEvaluating) {
                WifiProStateMachine.this.setWifiEvaluateTag(true);
                WifiProStateMachine.mIsWifiSemiAutoEvaluating = true;
                WifiProStateMachine.this.mIsAllowEvaluate = true;
                if (WifiProStateMachine.this.mWiFiProEvaluateController.isUnEvaluateAPRecordsEmpty()) {
                    WifiProStateMachine.this.logW("UnEvaluate AP records is empty !");
                } else {
                    WifiProStateMachine.this.mOpenAvailableAPCounter = 0;
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                    return;
                }
            }
            WifiProStateMachine.mIsWifiSemiAutoEvaluating = false;
            WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EVALUATE_COMPLETE);
        }

        public void exit() {
            WifiProStateMachine.this.logD("WifiSemiAutoEvaluateState exit");
            if (WifiProStateMachine.this.mIsWifiSemiAutoEvaluateComplete || !WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                WifiProStateMachine.this.setWifiEvaluateTag(false);
                WifiProStateMachine.this.mNetworkQosMonitor.stopBqeService();
            }
            WifiProStateMachine.this.mWiFiProEvaluateController.cleanEvaluateCacheRecords();
            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /*136169*/:
                    NetworkInfo networkInfo = (NetworkInfo) msg.obj.getParcelableExtra("networkInfo");
                    if (networkInfo == null || DetailedState.CONNECTED != networkInfo.getDetailedState() || !WifiProStateMachine.this.isWifiConnected()) {
                        if (networkInfo != null && DetailedState.DISCONNECTED == networkInfo.getDetailedState() && (WifiProStateMachine.this.mIsWifiSemiAutoEvaluateComplete || !WifiProStateMachine.this.isAllowWiFiAutoEvaluate())) {
                            WifiProStateMachine.this.logW("Evaluate has complete, go to mWifiDisConnectedState");
                            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                            WifiProStateMachine.this.setWifiEvaluateTag(false);
                            break;
                        }
                    }
                    WifiProStateMachine.this.setWifiEvaluateTag(false);
                    WifiProStateMachine.this.logD("mIsWifiSemiAutoEvaluateComplete == " + WifiProStateMachine.this.mIsWifiSemiAutoEvaluateComplete);
                    WifiProStateMachine.this.logD("******WifiSemiAutoEvaluateState go to mWifiConnectedState *****");
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiConnectedState);
                    break;
                    break;
                case WifiProStateMachine.EVENT_SCAN_RESULTS_AVAILABLE /*136293*/:
                    break;
                case WifiProStateMachine.EVENT_EVALUATE_COMPLETE /*136295*/:
                    WifiProStateMachine.this.logD("Evaluate has complete, restore wifi Config, mOpenAvailableAPCounter = " + WifiProStateMachine.this.mOpenAvailableAPCounter);
                    if (WifiProStateMachine.this.mOpenAvailableAPCounter >= 2) {
                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(10);
                        WifiProStateMachine.this.mOpenAvailableAPCounter = 0;
                    }
                    WifiProStateMachine.this.mWiFiProEvaluateController;
                    WiFiProEvaluateController.evaluateAPHashMapDump();
                    WifiProStateMachine.this.mWiFiProEvaluateController.cleanEvaluateRecords();
                    WifiProStateMachine.mIsWifiSemiAutoEvaluating = false;
                    WifiProStateMachine.this.mIsWifiSemiAutoEvaluateComplete = true;
                    WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                    NetworkInfo wifi_info = WifiProStateMachine.this.mConnectivityManager.getNetworkInfo(1);
                    if (wifi_info != null) {
                        if (wifi_info.getState() == NetworkInfo.State.DISCONNECTED) {
                            WifiProStateMachine.this.logD("wifi has disconnected, go to mWifiDisConnectedState");
                            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                            break;
                        }
                    }
                    WifiProStateMachine.this.logD("wifi_info is null, go to mWiFiProEnableState");
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiDisConnectedState);
                    break;
                    break;
                case WifiProStateMachine.EVENT_WIFI_SEMIAUTO_EVALUTE_CHANGE /*136300*/:
                    if (!WifiProStateMachine.this.isAllowWiFiAutoEvaluate()) {
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EVALUATE_COMPLETE);
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class WifiSemiAutoScoreState extends State {
        private int checkCounter;
        private long checkTime;
        private long connectTime;
        private boolean isCheckRuning;
        private String nextBSSID = null;
        private String nextSSID = null;

        WifiSemiAutoScoreState() {
        }

        public void enter() {
            WifiProStateMachine.this.logD("WifiSemiAutoScoreState enter,  mIsAllowEvaluate = " + WifiProStateMachine.this.mIsAllowEvaluate);
            if (isStopEevaluteNextAP()) {
                WifiProStateMachine.this.logD("WiFiPro auto Evaluate has  closed");
                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                return;
            }
            this.connectTime = 0;
            this.checkTime = 0;
            this.checkCounter = 0;
            this.isCheckRuning = false;
            this.nextSSID = WifiProStateMachine.this.mWiFiProEvaluateController.getNextEvaluateWiFiSSID();
            if (TextUtils.isEmpty(this.nextSSID)) {
                WifiProStateMachine.this.logD("ALL SemiAutoScore has Evaluate complete!");
                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                return;
            }
            WifiProStateMachine.this.logD("***********start SemiAuto Evaluate nextSSID :" + this.nextSSID);
            if (WifiProStateMachine.this.mWiFiProEvaluateController.isAbandonEvaluate(this.nextSSID)) {
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EVALUTE_ABANDON);
                return;
            }
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_EVALUTE_TIMEOUT);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_EVALUATE_START_CHECK_INTERNET);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT);
            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_EVALUTE_TIMEOUT, 75000);
            WifiProStateMachine.this.mWifiProUIDisplayManager.showToastL("start  evaluate :" + this.nextSSID);
            WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoLevel(this.nextSSID, 0);
            WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), 1, 0, this.nextSSID);
            WifiProStateMachine.this.refreshConnectedNetWork();
            if (WifiProStateMachine.this.isWifiConnected() && this.nextSSID.equals(WifiProStateMachine.this.mCurrentSsid)) {
                WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT);
                WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                this.isCheckRuning = true;
                return;
            }
            WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_EVALUTE_NEXT_AP, 2000);
        }

        public void exit() {
            WifiProStateMachine.this.logD("WifiSemiAutoScoreState exit");
            WifiProStateMachine.this.mNetworkQosMonitor.resetMonitorStatus();
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_EVALUTE_TIMEOUT);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT);
            WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_DELAY_EVALUTE_NEXT_AP);
            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
            this.nextBSSID = null;
            if (!TextUtils.isEmpty(this.nextSSID)) {
                WifiProStateMachine.this.mWiFiProEvaluateController.updateWifiProbeMode(this.nextSSID, 1);
            }
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE /*136169*/:
                    NetworkInfo networkInfo = (NetworkInfo) msg.obj.getParcelableExtra("networkInfo");
                    WifiProStateMachine.this.logD(", nextSSID SSID = " + this.nextSSID + ", networkInfo = " + networkInfo);
                    if (networkInfo != null && NetworkInfo.State.DISCONNECTED == networkInfo.getState()) {
                        if (!TextUtils.isEmpty(this.nextSSID) && this.nextSSID.equals(networkInfo.getExtraInfo())) {
                            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                            WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(11);
                            WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(this.nextSSID, 1);
                            WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(22);
                            WifiProStateMachine.this.mWifiProStatisticsManager.updateBG_AP_SSID(this.nextSSID);
                            WifiProStateMachine.this.mWiFiProEvaluateController.increaseFailCounter(this.nextSSID);
                            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                            break;
                        }
                    } else if (networkInfo == null || NetworkInfo.State.CONNECTED != networkInfo.getState()) {
                        if (networkInfo != null && NetworkInfo.State.CONNECTING == networkInfo.getState()) {
                            String currssid = networkInfo.getExtraInfo();
                            if (!(TextUtils.isEmpty(this.nextSSID) || TextUtils.isEmpty(currssid) || this.nextSSID.equals(currssid))) {
                                WifiProStateMachine.this.logD("Connect other ap ,ssid : " + currssid);
                                WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                                break;
                            }
                        }
                    } else {
                        String extssid = networkInfo.getExtraInfo();
                        if (!TextUtils.isEmpty(this.nextSSID) && !TextUtils.isEmpty(extssid) && !this.nextSSID.equals(extssid)) {
                            WifiProStateMachine.this.logD("Connected other ap ,ssid : " + extssid);
                            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                            break;
                        }
                        WifiConfiguration wifiConfig = WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID);
                        if (wifiConfig != null) {
                            WifiProStateMachine.this.logD(this.nextSSID + "is Connected, wifiConfig isTempCreated = " + wifiConfig.isTempCreated + ", Tag = " + Secure.getInt(WifiProStateMachine.this.mContentResolver, "wifipro_recommending_access_points", -1));
                        }
                        WifiProStateMachine.this.logD("receive connect msg ,ssid : " + extssid);
                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(16);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_CHECK_WIFI_INTERNET_RESULT /*136181*/:
                    if (!TextUtils.isEmpty(this.nextSSID) && this.isCheckRuning) {
                        this.checkTime = (System.currentTimeMillis() - this.checkTime) / 1000;
                        WifiProStateMachine.this.logD(this.nextSSID + " checkTime = " + this.checkTime + " s");
                        int result = msg.arg1;
                        int type = handleWifiCheckResult(result);
                        if (7 == result) {
                            if (this.checkCounter == 1) {
                                WifiProStateMachine.this.logD("internet check timeout ,check again");
                                this.checkTime = System.currentTimeMillis();
                                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EVALUATE_START_CHECK_INTERNET);
                                break;
                            }
                            type = 1;
                            WifiProStateMachine.this.mWiFiProEvaluateController.increaseFailCounter(this.nextSSID);
                            WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(11);
                            WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(21);
                            WifiProStateMachine.this.mWifiProStatisticsManager.updateBG_AP_SSID(this.nextSSID);
                        }
                        WifiProStateMachine.this.logD(this.nextSSID + " type = " + type);
                        WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(this.nextSSID, type);
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), 1, type, this.nextSSID);
                        WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreEvaluateStatus(this.nextSSID, true);
                        if (type != 4) {
                            WifiProStateMachine.this.logD("clean evaluate ap :" + this.nextSSID);
                            WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                            break;
                        }
                        WifiProStateMachine.this.mNetworkQosMonitor.startWiFiBqeDetect(HwSelfCureUtils.SELFCURE_WIFI_ON_TIMEOUT);
                        break;
                    }
                case WifiProStateMachine.EVENT_SCAN_RESULTS_AVAILABLE /*136293*/:
                    WifiProStateMachine.this.mScanResultList = WifiProStateMachine.this.mWiFiProEvaluateController.scanResultListFilter(WifiProStateMachine.this.mWifiManager.getScanResults());
                    WifiProStateMachine.this.mIsAllowEvaluate = WifiProStateMachine.this.mWiFiProEvaluateController.isAllowAutoEvaluate(WifiProStateMachine.this.mScanResultList);
                    if (!WifiProStateMachine.this.mIsAllowEvaluate) {
                        WifiProStateMachine.this.logD("discover save ap, stop allow evaluate");
                        WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_SUPPLICANT_STATE_CHANGE /*136297*/:
                case WifiProStateMachine.EVENT_WIFI_SEMIAUTO_EVALUTE_CHANGE /*136300*/:
                    break;
                case WifiProStateMachine.EVENT_WIFI_EVALUTE_TCPRTT_RESULT /*136299*/:
                    int level = msg.arg1;
                    WifiProStateMachine.this.logD(this.nextSSID + "  TCPRTT  level = " + level);
                    if (WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoLevel(this.nextSSID, level)) {
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), 2, level, this.nextSSID);
                    }
                    if (level == 0) {
                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(11);
                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(23);
                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBG_AP_SSID(this.nextSSID);
                    }
                    boolean enabled = WifiProCommonUtils.isWifiSecDetectOn(WifiProStateMachine.this.mContext);
                    int security = WifiProStateMachine.this.mWiFiProEvaluateController.getWifiSecurityInfo(this.nextSSID);
                    WifiProStateMachine.this.logD("security switch enabled = " + enabled + ", current security value = " + security);
                    if (!enabled || !WifiProCommonUtils.isWifiConnected(WifiProStateMachine.this.mWifiManager) || (security != -1 && security != 1)) {
                        WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreEvaluateStatus(this.nextSSID, true);
                        WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                        break;
                    }
                    this.nextBSSID = WifiProCommonUtils.getCurrentBssid(WifiProStateMachine.this.mWifiManager);
                    WifiProStateMachine.this.logD("recv BQE level = " + level + ", start to query wifi security, ssid = " + this.nextSSID + ", bssid = " + this.nextBSSID);
                    WifiProStateMachine.this.mNetworkQosMonitor.queryWifiSecurity(this.nextSSID, this.nextBSSID);
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WIFI_SECURITY_QUERY_TIMEOUT, 30000);
                    break;
                case WifiProStateMachine.EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT /*136301*/:
                    WifiProStateMachine.this.logD(this.nextSSID + " Conenct Time Out,connect fail! conenct Time = 35s");
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(15);
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBG_AP_SSID(this.nextSSID);
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(20);
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(this.nextSSID, 1);
                    WifiProStateMachine.this.mWiFiProEvaluateController.increaseFailCounter(this.nextSSID);
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), 1, 1, this.nextSSID);
                    WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                    break;
                case WifiProStateMachine.EVENT_LAST_EVALUTE_VALID /*136302*/:
                    WiFiProScoreInfo wiFiProScoreInfo = WifiProStateMachine.this.mWiFiProEvaluateController.getCurrentWiFiProScoreInfo(this.nextSSID);
                    if (wiFiProScoreInfo != null) {
                        WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), wiFiProScoreInfo.internetAccessType, wiFiProScoreInfo.networkQosLevel, wiFiProScoreInfo.networkQosScore);
                    }
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                    break;
                case WifiProStateMachine.EVENT_EVALUTE_TIMEOUT /*136304*/:
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(11);
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(this.nextSSID, 1);
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBG_AP_SSID(this.nextSSID);
                    WifiProStateMachine.this.mWiFiProEvaluateController.increaseFailCounter(this.nextSSID);
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), 1, 1, this.nextSSID);
                    WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                    WifiProStateMachine.this.logD(this.nextSSID + " evaluate Time = 70s");
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                    break;
                case WifiProStateMachine.EVENT_EVALUTE_ABANDON /*136305*/:
                    WifiProStateMachine.this.logD(this.nextSSID + "abandon evalute ");
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreInfoType(this.nextSSID, 1);
                    WifiProStateMachine.this.mWifiProConfigStore.updateWifiEvaluateConfig(WifiProStateMachine.this.mWiFiProEvaluateController.getWifiConfiguration(this.nextSSID), 1, 1, this.nextSSID);
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                    break;
                case WifiProStateMachine.EVENT_EVALUATE_START_CHECK_INTERNET /*136307*/:
                    WifiProStateMachine.this.logW("wifi conenct, start check internet,  checkCounter =   " + this.checkCounter);
                    if (this.checkCounter == 0) {
                        this.connectTime = (System.currentTimeMillis() - this.connectTime) / 1000;
                        WifiProStateMachine.this.logD(this.nextSSID + " background conenct Time =" + this.connectTime + " s");
                        WifiProStateMachine.this.mWiFiProCHRMgr.updateSSID(this.nextSSID);
                        WifiProStateMachine.this.mWiFiProCHRMgr.updateWifiproTimeLen((short) ((int) this.connectTime));
                        WifiProStateMachine.this.mWiFiProCHRMgr.updateWifiException(HwWifiCHRConstImpl.WIFI_WIFIPRO_EXCEPTION_EVENT, "BG_CONN_AP_TIME_LEN");
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT);
                    }
                    this.checkTime = System.currentTimeMillis();
                    this.checkCounter++;
                    WifiProStateMachine.this.mNetworkQosMonitor.startBqeService();
                    WifiProStateMachine.this.mNetworkQosMonitor.queryNetworkQos(1, WifiProStateMachine.this.mIsPortalAp, WifiProStateMachine.this.mIsNetworkAuthen);
                    this.isCheckRuning = true;
                    break;
                case WifiProStateMachine.EVENT_CONFIGURED_NETWORKS_CHANGED /*136308*/:
                    Intent confg_intent = msg.obj;
                    int change_reason = confg_intent.getIntExtra("changeReason", -1);
                    WifiConfiguration conn_cfg = (WifiConfiguration) confg_intent.getParcelableExtra("wifiConfiguration");
                    if (conn_cfg != null) {
                        WifiProStateMachine.this.logD(", nextSSID SSID = " + this.nextSSID + ", conf  " + conn_cfg.SSID);
                        if (change_reason != 0) {
                            if (change_reason == 2) {
                                WifiProStateMachine.this.logD("--- change_reason =change,  change a ssid = " + conn_cfg.SSID);
                                if (!WifiProStateMachine.this.isWifiConnected()) {
                                    WifiProStateMachine.this.logD("--- wifi has disconnect ----");
                                    WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                                } else if (!TextUtils.isEmpty(this.nextSSID) && this.nextSSID.equals(conn_cfg.SSID)) {
                                    WifiProStateMachine.this.mWiFiProEvaluateController.clearUntrustedOpenApList();
                                    WifiProStateMachine.this.mWifiProConfigStore.resetTempCreatedConfig(conn_cfg);
                                    if (conn_cfg.status == 1) {
                                        WifiProStateMachine.this.mWifiManager.connect(conn_cfg, null);
                                        WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(18);
                                    }
                                }
                                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                                break;
                            }
                        }
                        WifiProStateMachine.this.logD("--- change_reason =add,  add a new conn_cfg--,isTempCreated : " + conn_cfg.isTempCreated);
                        if (TextUtils.isEmpty(this.nextSSID) || !this.nextSSID.equals(conn_cfg.SSID)) {
                            if (!TextUtils.isEmpty(conn_cfg.SSID)) {
                                WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                                break;
                            }
                        }
                        WifiProStateMachine.this.mWiFiProEvaluateController.addUntrustedOpenApList(conn_cfg.SSID);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_WIFI_P2P_CONNECTION_CHANGED /*136310*/:
                    if (WifiProStateMachine.this.mIsP2PConnectedOrConnecting) {
                        WifiProStateMachine.this.logD("P2PConnectedOrConnecting  , stop allow evaluate");
                        WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                        WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoEvaluateState);
                        break;
                    }
                    break;
                case WifiProStateMachine.EVENT_WIFI_SECURITY_RESPONSE /*136312*/:
                case WifiProStateMachine.EVENT_WIFI_SECURITY_QUERY_TIMEOUT /*136313*/:
                    if (msg.obj != null) {
                        WifiProStateMachine.this.removeMessages(WifiProStateMachine.EVENT_WIFI_SECURITY_QUERY_TIMEOUT);
                        Bundle bundle = msg.obj;
                        String ssid = bundle.getString("com.huawei.wifipro.FLAG_SSID");
                        if (ssid != null) {
                            if (ssid.equals(this.nextSSID)) {
                                String bssid = bundle.getString("com.huawei.wifipro.FLAG_BSSID");
                                int status = bundle.getInt("com.huawei.wifipro.FLAG_SECURITY_STATUS");
                                WifiProStateMachine.this.logD("handle EVENT_WIFI_SECURITY_RESPONSE, ssid = " + ssid + ", bssid = " + bssid + ", status = " + status);
                                WifiProStateMachine.this.mWiFiProEvaluateController.updateWifiSecurityInfo(this.nextSSID, status);
                                if (status >= 2) {
                                    WifiProStateMachine.this.logD("handle EVENT_WIFI_SECURITY_RESPONSE, unsecurity, upload CHR statistic.");
                                    WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(4);
                                }
                            }
                        }
                        WifiProStateMachine.this.logD("handle EVENT_WIFI_SECURITY_RESPONSE, it's invalid ssid = " + ssid + ", ignore the result.");
                        break;
                    }
                    WifiProStateMachine.this.logW("EVENT_WIFI_SECURITY_RESPONSE, timeout happend.");
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateWifiSecurityInfo(this.nextSSID, -1);
                    WifiProStateMachine.this.mWiFiProEvaluateController.updateScoreEvaluateStatus(this.nextSSID, true);
                    WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                    WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
                    break;
                case WifiProStateMachine.EVENT_DELAY_EVALUTE_NEXT_AP /*136314*/:
                    if (!WifiProStateMachine.this.isWifiConnected()) {
                        evaluteNextAP();
                        break;
                    }
                    WifiProStateMachine.this.logD("wifi still connectd, delay 2s to evalute next ap");
                    WifiProStateMachine.this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
                    WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_DELAY_EVALUTE_NEXT_AP, 2000);
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void evaluteNextAP() {
            WifiProStateMachine.this.logD("start evalute next ap");
            if (WifiProStateMachine.this.mWiFiProEvaluateController.connectWifi(this.nextSSID)) {
                this.connectTime = System.currentTimeMillis();
                WifiProStateMachine.this.sendMessageDelayed(WifiProStateMachine.EVENT_WIFI_EVALUTE_CONNECT_TIMEOUT, 35000);
                return;
            }
            WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(11);
            WifiProStateMachine.this.mWifiProStatisticsManager.updateBG_AP_SSID(this.nextSSID);
            WifiProStateMachine.this.logD("background connect fail!");
            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWifiSemiAutoScoreState);
        }

        private int handleWifiCheckResult(int result) {
            if (7 != result) {
                WifiProStateMachine.this.mWiFiProCHRMgr.updateSSID(this.nextSSID);
                WifiProStateMachine.this.mWiFiProCHRMgr.updateWifiproTimeLen((short) ((int) this.checkTime));
                WifiProStateMachine.this.mWiFiProCHRMgr.updateWifiException(HwWifiCHRConstImpl.WIFI_WIFIPRO_EXCEPTION_EVENT, "BG_AC_TIME_LEN");
            }
            if (5 == result) {
                WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                wifiProStateMachine.mOpenAvailableAPCounter = wifiProStateMachine.mOpenAvailableAPCounter + 1;
                WifiProStateMachine.this.mWifiProUIDisplayManager.shownAccessNotification(true);
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(3);
                return 4;
            } else if (6 == result) {
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(6);
                return 3;
            } else if (-1 != result) {
                return 0;
            } else {
                WifiProStateMachine.this.mWifiProStatisticsManager.updateBGChrStatistic(5);
                return 2;
            }
        }

        private boolean isStopEevaluteNextAP() {
            return (WifiProStateMachine.this.isAllowWiFiAutoEvaluate() && !WifiProStateMachine.this.mIsManualConnectedWiFi && WifiProStateMachine.this.mIsAllowEvaluate) ? isConnectedOrConnectingAP() : true;
        }

        private boolean isConnectedOrConnectingAP() {
            NetworkInfo wifi_info = HwServiceFactory.getHwConnectivityManager().getNetworkInfoForWifi();
            if (wifi_info == null || !wifi_info.isConnectedOrConnecting()) {
                return false;
            }
            WifiProStateMachine.this.logD("isConnectedOrConnectingAP: " + wifi_info.getExtraInfo());
            return true;
        }
    }

    private static boolean getSettingsSystemBoolean(ContentResolver cr, String name, boolean def) {
        return System.getInt(cr, name, def ? 1 : 0) == 1;
    }

    private static boolean getSettingsGlobalBoolean(ContentResolver cr, String name, boolean def) {
        return Global.getInt(cr, name, def ? 1 : 0) == 1;
    }

    private static boolean getSettingsSecureBoolean(ContentResolver cr, String name, boolean def) {
        return Secure.getInt(cr, name, def ? 1 : 0) == 1;
    }

    private static int getSettingsSystemInt(ContentResolver cr, String name, int def) {
        return System.getInt(cr, name, def);
    }

    public static WifiProStateMachine createWifiProStateMachine(Context context, Messenger dstMessenger) {
        if (mWifiProStateMachine == null) {
            mWifiProStateMachine = new WifiProStateMachine(context, dstMessenger);
        }
        mWifiProStateMachine.start();
        return mWifiProStateMachine;
    }

    public static WifiProStateMachine getWifiProStateMachineImpl() {
        return mWifiProStateMachine;
    }

    private WifiProStateMachine(Context context, Messenger dstMessenger) {
        super("WifiProStateMachine");
        this.mContext = context;
        this.mWsmChannel = new AsyncChannel();
        this.mWsmChannel.connectSync(this.mContext, getHandler(), dstMessenger);
        this.mContentResolver = context.getContentResolver();
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mActivityManager = (ActivityManager) context.getSystemService("activity");
        WifiProStatisticsManager.initStatisticsManager(this.mContext);
        this.mWifiProStatisticsManager = WifiProStatisticsManager.getInstance();
        this.mWiFiProCHRMgr = WifiProCHRManager.getInstance();
        this.mNetworkBlackListManager = NetworkBlackListManager.getNetworkBlackListManagerInstance(this.mContext);
        this.mWifiProUIDisplayManager = WifiProUIDisplayManager.createInstance(context, this);
        this.mHwIntelligenceWiFiManager = HwIntelligenceWiFiManager.createInstance(context, this.mWifiProUIDisplayManager);
        this.mWifiProConfigurationManager = WifiProConfigurationManager.createWifiProConfigurationManager(this.mContext);
        this.mWifiProConfigStore = new WifiProConfigStore(this.mContext, this.mWsmChannel);
        this.mAppWhitelists = this.mWifiProConfigurationManager.getAppWhitelists();
        this.mNetworkQosMonitor = new NetworkQosMonitor(this.mContext, this, dstMessenger, this.mWifiProUIDisplayManager);
        this.mAbsPhoneWindowManager = (AbsPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
        this.mWifiHandover = new WifiHandover(this.mContext, this);
        this.mIsWiFiProEnabled = getSettingsSystemBoolean(this.mContentResolver, KEY_SMART_NETWORK_SWITCHING, false);
        this.mIsPrimaryUser = ActivityManager.getCurrentUser() == 0;
        logD("UserID =  " + ActivityManager.getCurrentUser() + ", mIsPrimaryUser = " + this.mIsPrimaryUser);
        this.mSampleCollectionManager = new SampleCollectionManager(this.mContext, this.mWifiProConfigurationManager);
        this.mWiFiProProtalController = new WiFiProPortalController(this.mContext, this.mTelephonyManager, this.mWifiProConfigurationManager, this.mSampleCollectionManager);
        this.mHwAutoConnectManager = new HwAutoConnectManager(context, this.mWsmChannel, this.mNetworkQosMonitor);
        this.mDualBandManager = HwDualBandManager.createInstance(context, this);
        this.mHwDualBandBlackListMgr = HwDualBandBlackListManager.getHwDualBandBlackListMgrInstance();
        addState(this.mDefaultState);
        addState(this.mWiFiProDisabledState, this.mDefaultState);
        addState(this.mWiFiProEnableState, this.mDefaultState);
        addState(this.mWifiConnectedState, this.mWiFiProEnableState);
        addState(this.mWiFiProVerfyingLinkState, this.mWiFiProEnableState);
        addState(this.mWifiDisConnectedState, this.mWiFiProEnableState);
        addState(this.mWiFiLinkMonitorState, this.mWiFiProEnableState);
        addState(this.mWifiSemiAutoEvaluateState, this.mWiFiProEnableState);
        addState(this.mWifiSemiAutoScoreState, this.mWifiSemiAutoEvaluateState);
        this.mWiFiProEvaluateController = new WiFiProEvaluateController(context);
        this.mIsPortalAp = false;
        this.mIsNetworkAuthen = false;
        registerMapNavigatingStateChanges();
        registerVehicleStateChanges();
        registerForSettingsChanges();
        registerForMobileDataChanges();
        registerForMobilePDPSwitchChanges();
        registerNetworkReceiver();
        registerOOBECompleted();
        registerForVpnSettingsChanges();
        registerForAppPidChanges();
        registerForAPEvaluateChanges();
        registerForManualConnectChanges();
        this.mHwAutoConnectManager.init();
        setInitialState(this.mWiFiProEnableState);
        logD("System Create WifiProStateMachine Complete!");
    }

    private void defaulVariableInit() {
        if (!this.isVariableInited) {
            this.mIsMobileDataEnabled = getSettingsGlobalBoolean(this.mContentResolver, "mobile_data", false);
            this.mEmuiPdpSwichValue = getSettingsSystemInt(this.mContentResolver, KEY_EMUI_WIFI_TO_PDP, 1);
            this.mIsWiFiProAutoEvaluateAP = getSettingsSecureBoolean(this.mContentResolver, KEY_WIFIPRO_RECOMMEND_NETWORK, false);
            this.mIsVpnWorking = getSettingsSystemBoolean(this.mContentResolver, SETTING_SECURE_VPN_WORK_VALUE, false);
            if (this.mIsVpnWorking) {
                System.putInt(this.mContext.getContentResolver(), SETTING_SECURE_VPN_WORK_VALUE, 0);
                this.mIsVpnWorking = false;
            }
            System.putInt(this.mContext.getContentResolver(), KEY_WIFIPRO_MANUAL_CONNECT, 0);
            setWifiEvaluateTag(false);
            this.isVariableInited = true;
            logD("Variable Init Complete!");
        }
    }

    private void registerNetworkReceiver() {
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean z = true;
                String action = intent.getAction();
                if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (WifiProStateMachine.isWifiEvaluating() && WifiProStateMachine.this.mIsWiFiProEnabled) {
                        WifiProStateMachine.this.mIsManualConnectedWiFi = WifiProStateMachine.getSettingsSystemBoolean(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.KEY_WIFIPRO_MANUAL_CONNECT, false);
                        if (WifiProStateMachine.this.mIsManualConnectedWiFi) {
                            WifiProStateMachine.this.logD("ManualConnectedWiFi  AP, ,isWifiEvaluating ");
                            WifiProStateMachine.this.setWifiEvaluateTag(false);
                            WifiProStateMachine.this.mWiFiProEvaluateController.cleanEvaluateRecords();
                            WifiProStateMachine.this.transitionTo(WifiProStateMachine.this.mWiFiProEnableState);
                        }
                    }
                    if (info != null && DetailedState.OBTAINING_IPADDR == info.getDetailedState()) {
                        WifiProStateMachine.this.mIsWiFiProConnected = false;
                        WifiProStateMachine.this.logD("wifi is conencted, WiFiProEnabled = " + WifiProStateMachine.this.mIsWiFiProEnabled + ", VpnWorking " + WifiProStateMachine.this.mIsVpnWorking);
                    } else if (info != null && DetailedState.CONNECTED == info.getDetailedState()) {
                        WifiProStateMachine.this.setDuanBandManualConnect();
                        WifiProStateMachine.this.resetWifiProManualConnect();
                    } else if (info != null && DetailedState.DISCONNECTED == info.getDetailedState()) {
                        WifiProStateMachine.this.mWiFiProEvaluateController.restoreEvaluateRecord(info);
                        if (WifiProStateMachine.this.mDisableDuanBandHandover) {
                            WifiProStateMachine.this.logD("set mDisableDuanBandHandover false");
                            WifiProStateMachine.this.mDisableDuanBandHandover = false;
                        }
                    }
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_NETWORK_STATE_CHANGE, intent);
                } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_NETWORK_CONNECTIVITY_CHANGE);
                } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                    if (WifiProStateMachine.this.mWifiManager.getWifiState() == 1) {
                        WifiProStateMachine.this.logD("wifi state is disabled, set mIsUserManualConnectAp false");
                        WifiProStateMachine.this.mDisableDuanBandHandover = false;
                        WifiProStateMachine.this.mIsUserManualConnectAp = false;
                        WifiProStateMachine.this.resetDualBandhanoverCHR();
                    }
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_STATE_CHANGED_ACTION);
                } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_DEVICE_SCREEN_ON);
                    if (WifiProStateMachine.this.mWifiProStatisticsManager != null) {
                        WifiProStateMachine.this.mWifiProStatisticsManager.sendScreenOnEvent();
                    }
                } else if ("android.net.wifi.SCAN_RESULTS".equals(action)) {
                    if (WifiProStateMachine.this.mWifiProUIDisplayManager.mIsNotificationShown && WifiProStateMachine.this.mWiFiProEvaluateController.isAccessAPOutOfRange(WifiProStateMachine.this.mWifiManager.getScanResults())) {
                        WifiProStateMachine.this.mWifiProUIDisplayManager.shownAccessNotification(false);
                    }
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_SCAN_RESULTS_AVAILABLE);
                } else if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action)) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_SUPPLICANT_STATE_CHANGE, intent);
                } else if ("android.net.wifi.CONFIGURED_NETWORKS_CHANGE".equals(action)) {
                    WifiProStateMachine.this.mWiFiProEvaluateController.reSetEvaluateRecord(intent);
                    if (WifiProStateMachine.this.getCurrentState() != WifiProStateMachine.this.mWifiSemiAutoScoreState) {
                        WifiConfiguration conn_cfg = (WifiConfiguration) intent.getParcelableExtra("wifiConfiguration");
                        if (conn_cfg != null) {
                            int change_reason = intent.getIntExtra("changeReason", -1);
                            WifiProStateMachine.this.logD("ssid = " + conn_cfg.SSID + ", change reson " + change_reason + ", isTempCreated = " + conn_cfg.isTempCreated);
                            if (conn_cfg.isTempCreated && change_reason != 1) {
                                WifiProStateMachine.this.logD("WiFiProDisabledState, forget " + conn_cfg.SSID);
                                WifiProStateMachine.this.mWifiManager.forget(conn_cfg.networkId, null);
                            }
                        }
                    }
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_CONFIGURED_NETWORKS_CHANGED, intent);
                } else if ("android.net.wifi.RSSI_CHANGED".equals(action)) {
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_RSSI_CHANGE, intent);
                } else if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    int userID = intent.getIntExtra("android.intent.extra.user_handle", 0);
                    WifiProStateMachine wifiProStateMachine = WifiProStateMachine.this;
                    if (userID != 0) {
                        z = false;
                    }
                    wifiProStateMachine.mIsPrimaryUser = z;
                    WifiProStateMachine.this.logD("user has switched,new userID = " + userID);
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE);
                } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                    NetworkInfo p2pNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (p2pNetworkInfo != null) {
                        WifiProStateMachine.this.mIsP2PConnectedOrConnecting = p2pNetworkInfo.isConnectedOrConnecting();
                    }
                    if (!WifiProStateMachine.this.mIsP2PConnectedOrConnecting) {
                        WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_P2P_CONNECTION_CHANGED);
                    }
                } else if ("android.net.wifi.p2p.CONNECT_STATE_CHANGE".equals(action)) {
                    int p2pState = intent.getIntExtra("extraState", -1);
                    if (p2pState == 1 || p2pState == 2) {
                        WifiProStateMachine.this.mIsP2PConnectedOrConnecting = true;
                    } else {
                        WifiProStateMachine.this.mIsP2PConnectedOrConnecting = false;
                    }
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFI_P2P_CONNECTION_CHANGED);
                }
            }
        };
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.mIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mIntentFilter.addAction("android.intent.action.SCREEN_ON");
        this.mIntentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        this.mIntentFilter.addAction(WifiProUIDisplayManager.ACTION_HIGH_MOBILE_DATA_ROVE_IN);
        this.mIntentFilter.addAction(WifiProUIDisplayManager.ACTION_HIGH_MOBILE_DATA_DELETE);
        this.mIntentFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.mIntentFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.p2p.CONNECT_STATE_CHANGE");
        this.mIntentFilter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
        this.mHMDBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (WifiProUIDisplayManager.ACTION_HIGH_MOBILE_DATA_ROVE_IN.equals(action)) {
                    WifiProStateMachine.this.logD("ACTION_HIGH_MOBILE_DATA  rove in event received.");
                    WifiProStateMachine.this.userHandoverWifi();
                    if (WifiProStateMachine.this.mWifiProStatisticsManager != null) {
                        WifiProStateMachine.this.mWifiProStatisticsManager.increaseHighMobileDataBtnRiCount();
                    }
                } else if (WifiProUIDisplayManager.ACTION_HIGH_MOBILE_DATA_DELETE.equals(action)) {
                    WifiProStateMachine.this.logD("ACTION_HIGH_MOBILE_DATA  delete event received, stop notify.");
                    if (WifiProStateMachine.this.mNetworkQosMonitor != null) {
                        WifiProStateMachine.this.mNetworkQosMonitor.setRoveOutToMobileState(0);
                    }
                    if (WifiProStateMachine.this.mWifiProStatisticsManager != null) {
                        WifiProStateMachine.this.mWifiProStatisticsManager.increaseUserDelNotifyCount();
                    }
                } else if (WifiproUtils.ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY.equals(action)) {
                    WifiProStateMachine.this.logD("**receive wifi connected concurrently********");
                    WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EVALUATE_START_CHECK_INTERNET);
                }
            }
        };
        this.mHMDIntentFilter = new IntentFilter();
        this.mHMDIntentFilter.addAction(WifiProUIDisplayManager.ACTION_HIGH_MOBILE_DATA_ROVE_IN);
        this.mHMDIntentFilter.addAction(WifiProUIDisplayManager.ACTION_HIGH_MOBILE_DATA_DELETE);
        this.mHMDIntentFilter.addAction(WifiproUtils.ACTION_NOTIFY_WIFI_CONNECTED_CONCURRENTLY);
        this.mContext.registerReceiverAsUser(this.mHMDBroadcastReceiver, UserHandle.ALL, this.mHMDIntentFilter, null, null);
    }

    private void unregisterReceiver() {
        if (this.mBroadcastReceiver != null) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.mBroadcastReceiver = null;
        }
        if (this.mHMDBroadcastReceiver != null) {
            this.mContext.unregisterReceiver(this.mHMDBroadcastReceiver);
            this.mHMDBroadcastReceiver = null;
        }
    }

    private void registerOOBECompleted() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("device_provisioned"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                if (WifiProStateMachine.getSettingsSystemInt(WifiProStateMachine.this.mContentResolver, "device_provisioned", 0) == 1) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.updateInitialWifiproState(WifiProStateMachine.this.mIsWiFiProEnabled);
                }
            }
        });
    }

    private void registerForSettingsChanges() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(KEY_SMART_NETWORK_SWITCHING), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                WifiProStateMachine.this.mIsWiFiProEnabled = WifiProStateMachine.getSettingsSystemBoolean(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.KEY_SMART_NETWORK_SWITCHING, false);
                WifiProStateMachine.this.logD("Wifi pro setting has changed,WiFiProEnabled == " + WifiProStateMachine.this.mIsWiFiProEnabled);
                if (WifiProStateMachine.isWifiEvaluating() && !WifiProStateMachine.this.mIsWiFiProEnabled) {
                    WifiProStateMachine.this.restoreWiFiConfig();
                    WifiProStateMachine.this.setWifiEvaluateTag(false);
                }
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE);
                WifiProStateMachine.this.mWifiProStatisticsManager.updateWifiproState(WifiProStateMachine.this.mIsWiFiProEnabled);
            }
        });
    }

    private void registerForMobileDataChanges() {
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("mobile_data"), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                WifiProStateMachine.this.mIsMobileDataEnabled = WifiProStateMachine.getSettingsGlobalBoolean(WifiProStateMachine.this.mContentResolver, "mobile_data", false);
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_MOBILE_DATA_STATE_CHANGED_ACTION);
                WifiProStateMachine.this.logD("MobileData has changed,isMobileDataEnabled = " + WifiProStateMachine.this.mIsMobileDataEnabled);
            }
        });
    }

    private void registerForMobilePDPSwitchChanges() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(KEY_EMUI_WIFI_TO_PDP), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                WifiProStateMachine.this.mEmuiPdpSwichValue = WifiProStateMachine.getSettingsSystemInt(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.KEY_EMUI_WIFI_TO_PDP, 1);
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_EMUI_CSP_SETTINGS_CHANGE);
                WifiProStateMachine.this.mWiFiProPdpSwichValue = WifiProStateMachine.this.mEmuiPdpSwichValue;
                if (WifiProStateMachine.this.mWifiProStatisticsManager != null) {
                    WifiProStateMachine.this.mWifiProStatisticsManager.increaseSelCspSettingChgCount(WifiProStateMachine.this.mWiFiProPdpSwichValue);
                }
                WifiProStateMachine.this.logD("Mobile PDP setting changed, mWiFiProPdpSwichValue = mWiFiProPdpSwichValue = " + WifiProStateMachine.this.mWiFiProPdpSwichValue);
            }
        });
    }

    private void registerForVpnSettingsChanges() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(SETTING_SECURE_VPN_WORK_VALUE), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                WifiProStateMachine.this.mIsVpnWorking = WifiProStateMachine.getSettingsSystemBoolean(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.SETTING_SECURE_VPN_WORK_VALUE, false);
                WifiProStateMachine.this.logD("vpn state has changed,mIsVpnWorking == " + WifiProStateMachine.this.mIsVpnWorking);
                WifiProStateMachine.this.sendMessage(WifiProStateMachine.EVENT_WIFIPRO_WORKING_STATE_CHANGE);
            }
        });
    }

    private void registerForAppPidChanges() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(SETTING_SECURE_CONN_WIFI_PID), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                WifiProStateMachine.this.mConnectWiFiAppPid = WifiProStateMachine.getSettingsSystemInt(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.SETTING_SECURE_CONN_WIFI_PID, -1);
                WifiProStateMachine.this.logD("current APP name == " + WifiProStateMachine.this.getAppName(WifiProStateMachine.this.mConnectWiFiAppPid));
            }
        });
    }

    private void registerForAPEvaluateChanges() {
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor(KEY_WIFIPRO_RECOMMEND_NETWORK), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                WifiProStateMachine.this.mIsWiFiProAutoEvaluateAP = WifiProStateMachine.getSettingsSecureBoolean(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.KEY_WIFIPRO_RECOMMEND_NETWORK, false);
            }
        });
    }

    private void registerForManualConnectChanges() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(KEY_WIFIPRO_MANUAL_CONNECT), false, new ContentObserver(getHandler()) {
            public void onChange(boolean selfChange) {
                WifiProStateMachine.this.mIsManualConnectedWiFi = WifiProStateMachine.getSettingsSystemBoolean(WifiProStateMachine.this.mContentResolver, WifiProStateMachine.KEY_WIFIPRO_MANUAL_CONNECT, false);
                WifiProStateMachine.this.logD("mIsManualConnectedWiFi has change:  " + WifiProStateMachine.this.mIsManualConnectedWiFi + ", wifipro state = " + WifiProStateMachine.this.getCurrentState().getName());
                if (WifiProStateMachine.this.mIsManualConnectedWiFi) {
                    WifiProStateMachine.this.mIsUserManualConnectAp = true;
                }
            }
        });
    }

    private void resetVariables() {
        this.mNetworkQosMonitor.stopBqeService();
        this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
        this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, false, 0, false);
        this.mWifiProUIDisplayManager.notificateNetAccessChange(false);
        this.mLastWifiLevel = 0;
        this.mIsWiFiNoInternet = false;
        this.mWiFiProPdpSwichValue = 0;
        reSetWifiInternetState();
        this.mNetworkQosMonitor.stopALLMonitor();
        this.mNetworkQosMonitor.resetMonitorStatus();
        this.mWifiProUIDisplayManager.cancelAllDialog();
        this.mCurrentVerfyCounter = 0;
        this.mIsUserHandoverWiFi = false;
        refreshConnectedNetWork();
        this.mIsWifiSemiAutoEvaluateComplete = false;
        resetWifiProManualConnect();
        stopDualBandMonitor();
    }

    private void updateWifiInternetStateChange(int lenvel) {
        if (isWifiConnected()) {
            if (this.mLastWifiLevel == lenvel) {
                logD("wifi lenvel is not change, don't report, lenvel = " + lenvel);
                return;
            }
            this.mLastWifiLevel = lenvel;
            if (-1 == lenvel) {
                Secure.putString(this.mContext.getContentResolver(), SETTING_SECURE_WIFI_NO_INT, "true," + this.mCurrentSsid);
                this.mWifiProUIDisplayManager.notificateNetAccessChange(true);
                logD("mIsPortalAp = " + this.mIsPortalAp + ", mIsNetworkAuthen = " + this.mIsNetworkAuthen);
                if (!this.mIsPortalAp || this.mIsNetworkAuthen) {
                    this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, true, 0, false);
                    this.mWifiProConfigStore.updateWifiEvaluateConfig(this.mCurrentWifiConfig, 1, 2, this.mCurrentSsid);
                    this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, 2);
                } else {
                    this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, true, 1, false);
                    this.mWifiProConfigStore.updateWifiEvaluateConfig(this.mCurrentWifiConfig, 1, 3, this.mCurrentSsid);
                    this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, 3);
                }
            } else if (6 == lenvel) {
                Secure.putString(this.mContext.getContentResolver(), SETTING_SECURE_WIFI_NO_INT, "true," + this.mCurrentSsid);
                this.mWifiProUIDisplayManager.notificateNetAccessChange(true);
                this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, true, 1, false);
                this.mWifiProConfigStore.updateWifiEvaluateConfig(this.mCurrentWifiConfig, 1, 3, this.mCurrentSsid);
                this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, 3);
            } else {
                Secure.putString(this.mContext.getContentResolver(), SETTING_SECURE_WIFI_NO_INT, "");
                this.mWifiProUIDisplayManager.notificateNetAccessChange(false);
                this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, false, 0, false);
                this.mWifiProConfigStore.updateWifiEvaluateConfig(this.mCurrentWifiConfig, 1, 4, this.mCurrentSsid);
                this.mWiFiProEvaluateController.updateScoreInfoType(this.mCurrentSsid, 4);
            }
        }
    }

    private void reSetWifiInternetState() {
        logD("reSetWifiInternetState");
        Secure.putString(this.mContext.getContentResolver(), SETTING_SECURE_WIFI_NO_INT, "");
    }

    private void setWifiCSPState(int state) {
        if (this.mLastCSPState == state) {
            logD("setWifiCSPState state is not change,ignor! mLastCSPState:" + this.mLastCSPState);
            return;
        }
        logD("setWifiCSPState new state = " + state);
        this.mLastCSPState = state;
        System.putInt(this.mContext.getContentResolver(), WIFI_CSP_DISPALY_STATE, state);
    }

    private void registerCallBack() {
        this.mNetworkQosMonitor.registerCallBack(this);
        this.mWifiHandover.registerCallBack(this, this.mNetworkQosMonitor);
        this.mWifiProUIDisplayManager.registerCallBack(this);
    }

    private void unRegisterCallBack() {
        this.mNetworkQosMonitor.unRegisterCallBack();
        this.mWifiHandover.unRegisterCallBack();
        this.mWifiProUIDisplayManager.unRegisterCallBack();
    }

    private boolean isWiFiPoorer(int wifi_level, int mobile_level) {
        boolean z = true;
        logD("WiFi Qos =[ " + wifi_level + " ] ,  Mobile Qos =[ " + mobile_level + "]");
        if (mobile_level == 0) {
            return false;
        }
        if (this.mIsWiFiNoInternet) {
            if (-1 >= mobile_level) {
                z = false;
            }
            return z;
        }
        if (wifi_level >= mobile_level) {
            z = false;
        }
        return z;
    }

    private boolean isMobileDataConnected() {
        if (5 == this.mTelephonyManager.getSimState() && this.mIsMobileDataEnabled) {
            return true;
        }
        return false;
    }

    private synchronized boolean isWifiConnected() {
        if (this.mWifiManager.isWifiEnabled()) {
            WifiInfo conInfo = this.mWifiManager.getConnectionInfo();
            if (!(conInfo == null || conInfo.getNetworkId() == -1 || conInfo.getBSSID() == null || "00:00:00:00:00:00".equals(conInfo.getBSSID()))) {
                if (conInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isNotShowIntelligentDialog() {
        logD("mIsWiFiProConnected = " + this.mIsWiFiProConnected);
        return this.mIsWiFiProConnected;
    }

    public static void putConnectWifiAppPid(Context context, int pid) {
    }

    private boolean isKeepCurrWiFiConnected() {
        if (this.mIsVpnWorking) {
            logW("vpn is working,shuld keep current connect");
        }
        return (this.mIsVpnWorking || this.mIsUserHandoverWiFi) ? true : isAppinWhitelists();
    }

    private boolean isAllowWiFiAutoEvaluate() {
        if (this.mIsWiFiProAutoEvaluateAP) {
        }
        if (!this.mIsWiFiProEnabled || this.mIsVpnWorking) {
            return false;
        }
        return true;
    }

    private void refreshConnectedNetWork() {
        if (isWifiConnected()) {
            WifiInfo conInfo = this.mWifiManager.getConnectionInfo();
            this.mCurrWifiInfo = conInfo;
            if (conInfo != null) {
                this.mCurrentBssid = conInfo.getBSSID();
                this.mCurrentSsid = conInfo.getSSID();
                this.mCurrentRssi = conInfo.getRssi();
                List<WifiConfiguration> configNetworks = this.mWifiManager.getConfiguredNetworks();
                if (configNetworks != null) {
                    for (WifiConfiguration config : configNetworks) {
                        if (config.networkId == conInfo.getNetworkId()) {
                            this.mCurrentWifiConfig = config;
                        }
                    }
                }
                return;
            }
        }
        this.mCurrentBssid = null;
        this.mCurrentSsid = null;
        this.mCurrentRssi = WifiHandover.INVALID_RSSI;
    }

    private boolean isAllowWifi2Mobile() {
        if (this.mIsWiFiProEnabled && this.mIsPrimaryUser && isMobileDataConnected() && this.mPowerManager.isScreenOn() && this.mEmuiPdpSwichValue != 2) {
            return true;
        }
        return false;
    }

    private boolean isPdpAvailable() {
        if (SystemProperties.getBoolean(SYS_PROPERT_PDP, false)) {
            logD("SYS_PROPERT_PDP RemindWifiToPdp is true");
            return true;
        }
        logD("SYS_PROPERT_PDP RemindWifiToPdp is false");
        return false;
    }

    private String getAppName(int pid) {
        String processName = "";
        List<RunningAppProcessInfo> appProcessList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    private boolean isAppinWhitelists() {
        if (this.mCurrentWifiConfig != null) {
            String currAppName = this.mCurrentWifiConfig.lastUpdateName;
            if (TextUtils.isEmpty(currAppName)) {
                currAppName = this.mCurrentWifiConfig.creatorName;
            }
            if (!(TextUtils.isEmpty(currAppName) || this.mAppWhitelists == null)) {
                for (String str : this.mAppWhitelists) {
                    if (currAppName.equals(str)) {
                        logD("curr name in the  Whitelists ");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void resetWifiProManualConnect() {
        if (this.mIsManualConnectedWiFi) {
            System.putInt(this.mContext.getContentResolver(), KEY_WIFIPRO_MANUAL_CONNECT, 0);
        }
    }

    public static boolean isWifiEvaluating() {
        return !mIsWifiManualEvaluating ? mIsWifiSemiAutoEvaluating : true;
    }

    private boolean isSettingsActivity() {
        List<RunningTaskInfo> runningTaskInfos = this.mActivityManager.getRunningTasks(1);
        if (!(runningTaskInfos == null || runningTaskInfos.isEmpty())) {
            ComponentName cn = ((RunningTaskInfo) runningTaskInfos.get(0)).topActivity;
            return (cn == null || cn.getClassName() == null || !cn.getClassName().startsWith(HUAWEI_SETTINGS)) ? false : true;
        }
    }

    public void setWifiApEvaluateEnabled(boolean enable) {
        logD("setWifiApEvaluateEnabled enabled " + enable);
        logD("system can not eavluate ap, ignor setting cmd");
    }

    private void setWifiEvaluateTag(boolean evaluate) {
        logD("setWifiEvaluateTag Tag :" + evaluate);
        Secure.putInt(this.mContentResolver, "wifipro_recommending_access_points", evaluate ? 1 : 0);
    }

    private boolean restoreWiFiConfig() {
        this.mIsWiFiProAutoEvaluateAP = getSettingsSecureBoolean(this.mContentResolver, KEY_WIFIPRO_RECOMMEND_NETWORK, false);
        this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
        NetworkInfo wifi_info = this.mConnectivityManager.getNetworkInfo(1);
        if (wifi_info == null || wifi_info.getDetailedState() != DetailedState.VERIFYING_POOR_LINK) {
            return false;
        }
        this.mWifiManager.disconnect();
        return true;
    }

    public synchronized void onNetworkQosChange(int type, int level) {
        if (1 == type) {
            this.mCurrentWifiLevel = level;
            logD("receive wifi Qos: currentWifiLevel == " + this.mCurrentWifiLevel + ", mIsWiFiNoInternet = " + this.mIsWiFiNoInternet);
            sendMessage(EVENT_WIFI_QOS_CHANGE, level);
            if (this.mIsWiFiNoInternet && 3 == level) {
                updateWifiInternetStateChange(level);
            }
        } else if (type == 0) {
            sendMessage(EVENT_MOBILE_QOS_CHANGE, level);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void onNetworkDetectionResult(int type, int level) {
        if (1 == type) {
            logD("wifi Detection level == " + level);
            if (isKeepCurrWiFiConnected()) {
                logD("keep curreny connect,ignore wifi check result");
                sendMessage(EVENT_CHECK_WIFI_INTERNET_RESULT, 5);
            } else if (WifiproUtils.NET_INET_QOS_LEVEL_UNKNOWN == level) {
                sendMessage(EVENT_WIFI_CHECK_UNKOWN, level);
            } else if (isWifiEvaluating()) {
                sendMessage(EVENT_CHECK_WIFI_INTERNET_RESULT, level);
            } else {
                if (7 == level) {
                    level = -1;
                }
                if (!this.mIsPoorRssiRequestCheckWiFi) {
                    updateWifiInternetStateChange(level);
                    sendMessage(EVENT_CHECK_WIFI_INTERNET_RESULT, level);
                } else if (-1 == level) {
                    onNetworkQosChange(1, 1);
                }
            }
        } else if (type == 0) {
            sendMessage(EVENT_CHECK_MOBILE_QOS_RESULT, level);
        }
    }

    public synchronized void onWifiHandoverChange(int type, boolean result, String bssid, int errorReason) {
        if (1 == type) {
            if (result) {
                this.mWifiProStatisticsManager.increaseWiFiHandoverWiFiCount(this.mWifiToWifiType);
            }
            this.mNewSelect_bssid = bssid;
            sendMessage(EVENT_WIFI_HANDOVER_WIFI_RESULT, Boolean.valueOf(result));
        } else if (4 == type) {
            this.mNewSelect_bssid = bssid;
            sendMessage(EVENT_DUALBAND_WIFI_HANDOVER_RESULT, errorReason);
        }
    }

    public synchronized void onDualBandNetWorkType(int type, List<HwDualBandMonitorInfo> apList) {
        if (apList != null) {
            if (apList.size() != 0) {
                if (this.mDisableDuanBandHandover) {
                    logD("keep curreny connect,ignore dualband ap handover");
                    return;
                }
                logD("onDualBandNetWorkType type = " + type + " apList.size() = " + apList.size());
                this.mDualBandMonitorApList.clear();
                this.mDualBandMonitorInfoSize = apList.size();
                for (HwDualBandMonitorInfo monitorInfo : apList) {
                    if (this.mCurrentWifiConfig == null || this.mCurrentWifiConfig.SSID == null || !this.mCurrentWifiConfig.SSID.equals(monitorInfo.mSsid) || this.mCurrentWifiConfig.allowedKeyManagement.cardinality() > 1 || this.mCurrentWifiConfig.getAuthType() != monitorInfo.mAuthType) {
                        this.mDualBandMonitorApList.add(monitorInfo);
                        WifiProEstimateApInfo apInfo = new WifiProEstimateApInfo();
                        apInfo.setApBssid(monitorInfo.mBssid);
                        apInfo.setApRssi(monitorInfo.mCurrentRssi);
                        apInfo.setApAuthType(monitorInfo.mAuthType);
                        this.mNetworkQosMonitor.get5GApRssiThreshold(apInfo);
                    } else {
                        logD("onDualBandNetWorkType 5G and 2.4G AP have the same ssid and auth type");
                    }
                }
                initMonitorApListCHR();
                return;
            }
        }
        loge("onDualBandNetWorkType apList null error");
    }

    public synchronized void onDualBandNetWorkFind(List<HwDualBandMonitorInfo> apList) {
        if (apList != null) {
            if (apList.size() != 0) {
                if (this.mDualBandMonitorStart) {
                    logD("onDualBandNetWorkFind  apList.size() = " + apList.size());
                    this.mDualBandMonitorStart = false;
                    this.mDualBandEstimateApList.clear();
                    this.mAvailable5GAPBssid = null;
                    this.mDualBandEstimateInfoSize = apList.size();
                    for (HwDualBandMonitorInfo monitorInfo : apList) {
                        WifiProEstimateApInfo apInfo = new WifiProEstimateApInfo();
                        apInfo.setApBssid(monitorInfo.mBssid);
                        apInfo.setEstimateApSsid(monitorInfo.mSsid);
                        apInfo.setApAuthType(monitorInfo.mAuthType);
                        apInfo.setApRssi(monitorInfo.mCurrentRssi);
                        apInfo.setDualbandAPType(monitorInfo.mIsDualbandAP);
                        this.mDualBandEstimateApList.add(apInfo);
                        this.mNetworkQosMonitor.getApHistoryQualityScore(apInfo);
                    }
                    refreshConnectedNetWork();
                    this.mDualBandEstimateInfoSize++;
                    WifiProEstimateApInfo currentApInfo = new WifiProEstimateApInfo();
                    currentApInfo.setApBssid(this.mCurrentBssid);
                    currentApInfo.setEstimateApSsid(this.mCurrentSsid);
                    currentApInfo.setApRssi(this.mCurrentRssi);
                    currentApInfo.set5GAP(false);
                    this.mDualBandEstimateApList.add(currentApInfo);
                    this.mNetworkQosMonitor.getApHistoryQualityScore(currentApInfo);
                    return;
                }
            }
        }
        loge("onDualBandNetWorkFind apList null error or mDualBandMonitorStart = " + this.mDualBandMonitorStart);
    }

    public synchronized void onWifiBqeReturnRssiTH(WifiProEstimateApInfo apInfo) {
        if (apInfo == null) {
            loge("onWifiBqeReturnRssiTH apInfo null error");
        } else {
            sendMessage(EVENT_DUALBAND_RSSITH_RESULT, apInfo);
        }
    }

    public synchronized void onWifiBqeReturnHistoryScore(WifiProEstimateApInfo apInfo) {
        if (apInfo == null) {
            loge("onWifiBqeReturnHistoryScore apInfo null error");
        } else {
            sendMessage(EVENT_DUALBAND_SCORE_RESULT, apInfo);
        }
    }

    public synchronized void onWifiBqeReturnCurrentRssi(int rssi) {
        this.mDualBandManager.updateCurrentRssi(rssi);
    }

    private void retryDualBandAPMonitor() {
        this.mDualBandMonitorInfoSize = this.mDualBandMonitorApList.size();
        if (this.mDualBandMonitorInfoSize == 0) {
            loge("retry dual band monitor error, monitorinfo size is zero");
            return;
        }
        for (HwDualBandMonitorInfo monitorInfo : this.mDualBandMonitorApList) {
            WifiProEstimateApInfo apInfo = new WifiProEstimateApInfo();
            apInfo.setApBssid(monitorInfo.mBssid);
            apInfo.setApRssi(monitorInfo.mCurrentRssi);
            this.mNetworkQosMonitor.get5GApRssiThreshold(apInfo);
        }
    }

    private void updateDualBandMonitorInfo(WifiProEstimateApInfo apInfo) {
        for (HwDualBandMonitorInfo monitorInfo : this.mDualBandMonitorApList) {
            String bssid = monitorInfo.mBssid;
            if (bssid != null && bssid.equals(apInfo.getApBssid())) {
                monitorInfo.mTargetRssi = apInfo.getRetRssiTH();
                return;
            }
        }
    }

    private void updateDualBandEstimateInfo(WifiProEstimateApInfo apInfo) {
        for (WifiProEstimateApInfo estimateApInfo : this.mDualBandEstimateApList) {
            String bssid = estimateApInfo.getApBssid();
            if (bssid != null && bssid.equals(apInfo.getApBssid())) {
                estimateApInfo.setRetHistoryScore(apInfo.getRetHistoryScore());
                return;
            }
        }
    }

    private void chooseAvalibleDualBandAp() {
        logD("chooseAvalibleDualBandAp DualBandEstimateApList =" + this.mDualBandEstimateApList.toString());
        if (this.mDualBandEstimateApList.size() == 0 || this.mCurrentBssid == null) {
            Log.e(TAG, "chooseAvalibleDualBandAp ap size error");
            return;
        }
        this.mAvailable5GAPBssid = null;
        this.mAvailable5GAPSsid = null;
        this.mAvailable5GAPAuthType = 0;
        this.mDuanBandHandoverType = 0;
        WifiProEstimateApInfo bestAp = new WifiProEstimateApInfo();
        int currentApScore = 0;
        for (WifiProEstimateApInfo apInfo : this.mDualBandEstimateApList) {
            if (this.mCurrentBssid.equals(apInfo.getApBssid())) {
                currentApScore = apInfo.getRetHistoryScore();
                this.mDualbandCurrentAPInfo = apInfo;
            } else if (apInfo.getRetHistoryScore() > bestAp.getRetHistoryScore()) {
                bestAp = apInfo;
            }
        }
        logD("chooseAvalibleDualBandAp bestAp =" + bestAp.toString() + ", currentApScore =" + currentApScore);
        int score = bestAp.getRetHistoryScore();
        if (score >= 40 && bestAp.getApRssi() >= HANDOVER_5G_DIRECTLY_RSSI) {
            this.mAvailable5GAPBssid = bestAp.getApBssid();
            this.mAvailable5GAPSsid = bestAp.getApSsid();
            this.mAvailable5GAPAuthType = bestAp.getApAuthType();
            this.mDuanBandHandoverType = bestAp.getDualbandAPType();
            this.mDualbandSelectAPInfo = bestAp;
        } else if (score >= currentApScore + 5 || (bestAp.getDualbandAPType() == 1 && bestAp.getApRssi() >= -55)) {
            this.mAvailable5GAPBssid = bestAp.getApBssid();
            this.mAvailable5GAPSsid = bestAp.getApSsid();
            this.mAvailable5GAPAuthType = bestAp.getApAuthType();
            this.mDuanBandHandoverType = bestAp.getDualbandAPType();
            this.mDualbandSelectAPInfo = bestAp;
        } else {
            reportDualbandStatisticsCHR(3);
        }
        if (this.mAvailable5GAPSsid == null) {
            ArrayList<HwDualBandMonitorInfo> mDualBandDeleteList = new ArrayList();
            for (HwDualBandMonitorInfo monitorInfo : this.mDualBandMonitorApList) {
                String bssid = monitorInfo.mBssid;
                if (bssid != null && bssid.equals(bestAp.getApBssid()) && monitorInfo.mTargetRssi < -45) {
                    monitorInfo.mTargetRssi += 10;
                    break;
                } else if (monitorInfo.mCurrentRssi >= -45) {
                    mDualBandDeleteList.add(monitorInfo);
                }
            }
            if (mDualBandDeleteList.size() > 0) {
                for (HwDualBandMonitorInfo deleteInfo : mDualBandDeleteList) {
                    logD("remove mix AP for RSSI > -45 DB RSSi = " + deleteInfo.mSsid);
                    this.mDualBandMonitorApList.remove(deleteInfo);
                }
            }
            if (this.mDualBandMonitorApList.size() != 0) {
                this.mDualBandMonitorStart = true;
                this.mDualBandManager.startMonitor(this.mDualBandMonitorApList);
            }
        } else if (this.mHwDualBandBlackListMgr.isInWifiBlacklist(this.mAvailable5GAPSsid)) {
            long expiretime = this.mHwDualBandBlackListMgr.getExpireTimeForRetry(this.mAvailable5GAPSsid);
            logD("getExpireTimeForRetry for ssid " + this.mAvailable5GAPSsid + ", time =" + expiretime);
            sendMessageDelayed(EVENT_DUALBAND_DELAY_RETRY, expiretime);
            reportDualbandStatisticsCHR(4);
        } else {
            logD("do dualband handover : " + bestAp.toString());
            sendMessage(EVENT_DUALBAND_5GAP_AVAILABLE);
        }
    }

    private void addDualBandBlackList(String ssid) {
        logD("addDualBandBlackList ssid = " + ssid + ", mDualBandConnectAPSsid = " + this.mDualBandConnectAPSsid);
        if (ssid == null || this.mDualBandConnectAPSsid == null || !this.mDualBandConnectAPSsid.equals(ssid)) {
            logD("addDualBandBlackList do nothing");
            return;
        }
        this.mDualBandConnectAPSsid = null;
        if (System.currentTimeMillis() - this.mDualBandConnectTime > 1800000) {
            this.mHwDualBandBlackListMgr.addWifiBlacklist(ssid, true);
        } else {
            this.mHwDualBandBlackListMgr.addWifiBlacklist(ssid, false);
        }
    }

    private void setDuanBandManualConnect() {
        if (this.mIsUserManualConnectAp) {
            logD("set mDisableDuanBandHandover true");
            this.mDisableDuanBandHandover = true;
            this.mIsUserManualConnectAp = false;
        }
    }

    private void startDualBandManager() {
        this.mDualBandManager.startDualBandManger();
    }

    private void stopDualBandManager() {
        stopDualBandMonitor();
        this.mDualBandManager.stopDualBandManger();
    }

    private void stopDualBandMonitor() {
        if (this.mDualBandMonitorStart) {
            this.mDualBandMonitorStart = false;
            this.mDualBandManager.stopMonitor();
        }
    }

    public int getNetwoksHandoverType() {
        return this.mWifiHandover.getNetwoksHandoverType();
    }

    private void sendNetworkCheckingStatus(String action, String flag, int property) {
        Intent intent = new Intent(action);
        intent.setFlags(67108864);
        intent.putExtra(flag, property);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void notifyNetworkCheckResult(int result) {
        int internet_level = result;
        if (result == 5 && this.mCurrentWifiConfig != null && WifiProCommonUtils.matchedRequestByHistory(this.mCurrentWifiConfig.internetHistory, 102)) {
            internet_level = 6;
        }
        sendNetworkCheckingStatus("huawei.conn.NETWORK_CONDITIONS_MEASURED", "extra_is_internet_ready", internet_level);
    }

    private int getDualbandType() {
        logW("getDualbandType mDualBandCloneMonitorApList.size() = " + this.mDualBandCloneMonitorApList.size());
        if (this.mDualBandCloneMonitorApList.size() == 0) {
            return DUALBAND_TYPE_UNKONW;
        }
        if (this.mDualBandCloneMonitorApList.size() == 1 && ((HwDualBandMonitorInfo) this.mDualBandCloneMonitorApList.get(0)).mIsDualbandAP == 1) {
            return DUALBAND_TYPE_SINGLE;
        }
        return DUALBAND_TYPE_MIX;
    }

    private void reportDualbandStatisticsCHR(int type) {
        int dualBandType = getDualbandType();
        if (dualBandType != DUALBAND_TYPE_UNKONW) {
            logW("reportDualbandStatisticsCHR dualBandType = " + dualBandType + " type = " + type);
            switch (type) {
                case 1:
                    if (dualBandType != DUALBAND_TYPE_MIX) {
                        if (dualBandType == DUALBAND_TYPE_SINGLE) {
                            this.mWifiProStatisticsManager.increaseDualbandStatisticCount(8);
                            break;
                        }
                    }
                    this.mWifiProStatisticsManager.increaseDualbandStatisticCount(20);
                    break;
                    break;
                case 2:
                    if (dualBandType != DUALBAND_TYPE_MIX) {
                        if (dualBandType == DUALBAND_TYPE_SINGLE) {
                            this.mWifiProStatisticsManager.increaseDualbandStatisticCount(9);
                            break;
                        }
                    }
                    this.mWifiProStatisticsManager.increaseDualbandStatisticCount(21);
                    break;
                    break;
                case 3:
                    if (dualBandType != DUALBAND_TYPE_MIX) {
                        if (dualBandType == DUALBAND_TYPE_SINGLE) {
                            this.mWifiProStatisticsManager.increaseDualbandStatisticCount(7);
                            break;
                        }
                    }
                    this.mWifiProStatisticsManager.increaseDualbandStatisticCount(19);
                    break;
                    break;
                case 4:
                    if (dualBandType != DUALBAND_TYPE_MIX) {
                        if (dualBandType == DUALBAND_TYPE_SINGLE) {
                            this.mWifiProStatisticsManager.increaseDualbandStatisticCount(6);
                            break;
                        }
                    }
                    this.mWifiProStatisticsManager.increaseDualbandStatisticCount(18);
                    break;
                    break;
            }
        }
    }

    private void reportDualbandInternetResultCHR(int result) {
        if (!this.mIsDualbandhandover) {
            return;
        }
        if (result == -1 || result == 6) {
            logW("reportDualbandVerifyCHR mIsDualbandhandover = " + this.mIsDualbandhandover + " result = " + result);
            this.mWifiProStatisticsManager.increaseDualbandStatisticCount(27);
        }
    }

    private void reportDualbandVerifyCHR() {
        long timer = System.currentTimeMillis() - this.mDualbandhanoverTime;
        if (this.mIsDualbandhandover && timer <= ((long) DUALBAND_CHR_VERIFY_THRESHOLD)) {
            logW("reportDualbandVerifyCHR");
            this.mWifiProStatisticsManager.increaseDualbandStatisticCount(29);
            uploadDuanBandException("DUALBAND_HANDOVER_TO_BAD_5G", 0);
        }
    }

    private void reportDualbandConnectAP() {
        long timer = System.currentTimeMillis() - this.mDualbandhanoverTime;
        if (this.mIsDualbandhandover && this.mDisableDuanBandHandover && timer <= ((long) DUALBAND_CHR_HANDOVER_THRESHOLD)) {
            logW("reportDualbandConnectAP mIsDualbandhandover = " + this.mIsDualbandhandover + " mIsUserManualConnectAp = " + this.mIsUserManualConnectAp + " mDisableDuanBandHandover = " + this.mDisableDuanBandHandover + " timer = " + timer);
            this.mWifiProStatisticsManager.increaseDualbandStatisticCount(30);
            uploadDuanBandException("DUALBAND_HANDOVER_USER_REJECT", 0);
        }
    }

    private void initDualBandhanoverConnectedCHR() {
        logW("mDualBandMonitorApList.size() = " + this.mDualBandMonitorApList.size());
        logW("initDualBandhanoverCHR mIsDualbandhandoverSucc = " + this.mIsDualbandhandoverSucc);
        if (this.mIsDualbandhandoverSucc) {
            this.mIsDualbandhandover = true;
            this.mDualbandhanoverTime = System.currentTimeMillis();
        } else {
            reportDualbandConnectAP();
            initDualBandhanoverCHR();
        }
        this.mIsDualbandhandoverSucc = false;
    }

    private void initDualBandhanoverCHR() {
        logW("initDualBandhanoverCHR");
        this.mIsDualbandhandover = false;
        this.mDualbandhanoverTime = 0;
        this.mDualbandSelectAPInfo = null;
    }

    private void initMonitorApListCHR() {
        if (this.mDualBandMonitorApList.size() > 0) {
            this.mDualBandCloneMonitorApList = (ArrayList) this.mDualBandMonitorApList.clone();
        }
    }

    private void resetDualBandhanoverCHR() {
        logW("resetDualBandhanoverCHR");
        this.mIsDualbandhandover = false;
        this.mDualbandhanoverTime = 0;
        this.mDualbandSelectAPInfo = null;
        this.mDualbandCurrentAPInfo = null;
        this.mDualbandPingPangTime = 0;
        this.mDualbandRoveInConut = 0;
        this.mLastConnect5GAP = null;
        this.mDualBandCloneMonitorApList = null;
    }

    private void reportPingPangCHR(String bssid) {
        logW("reportPingPangCHR mLastConnect5GAP = " + this.mLastConnect5GAP + " bssid = " + bssid);
        this.mIsDualbandhandoverSucc = true;
        if (this.mLastConnect5GAP == null || this.mLastConnect5GAP.equals(bssid)) {
            logW("reportPingPangCHR mDualbandRoveInConut = " + this.mDualbandRoveInConut);
            if (this.mDualbandRoveInConut >= 3) {
                if (System.currentTimeMillis() - this.mDualbandPingPangTime <= ((long) DUALBAND_CHR_PINGPANG_THRESHOLD)) {
                    this.mWifiProStatisticsManager.increaseDualbandStatisticCount(31);
                    uploadDuanBandException("DUALBAND_HANDOVER_PINGPONG", 0);
                }
                this.mDualbandPingPangTime = System.currentTimeMillis();
                this.mDualbandRoveInConut = 0;
                return;
            }
            this.mDualbandRoveInConut++;
            return;
        }
        this.mLastConnect5GAP = bssid;
        this.mDualbandPingPangTime = System.currentTimeMillis();
        this.mDualbandRoveInConut = 1;
    }

    private void uploadDuanBandException(String event, int reason) {
        logW("uploadDuanBandException");
        if (this.mDualbandSelectAPInfo != null && this.mDualbandCurrentAPInfo != null) {
            WifiProDualbandExceptionRecord rec = new WifiProDualbandExceptionRecord();
            rec.mSSID_2G = this.mDualbandCurrentAPInfo.getApSsid();
            rec.mSSID_5G = this.mDualbandSelectAPInfo.getApSsid();
            rec.mSingleOrMixed = (short) this.mDualbandSelectAPInfo.getDualbandAPType();
            rec.mRSSI_2G = (short) this.mDualbandCurrentAPInfo.getApRssi();
            rec.mRSSI_5G = (short) this.mDualbandSelectAPInfo.getApRssi();
            rec.mTarget_RSSI_5G = (short) this.mDualbandSelectAPInfo.getRetRssiTH();
            rec.mScore_2G = (short) this.mDualbandCurrentAPInfo.getRetHistoryScore();
            rec.mScore_5G = (short) this.mDualbandSelectAPInfo.getRetHistoryScore();
            rec.mHandOverErrCode = (short) reason;
            this.mWifiProStatisticsManager.uploadWifiProDualbandExceptionEvent(event, rec);
        }
    }

    public void onWifiConnected(boolean result, int reason) {
    }

    public void onCheckAvailableWifi(boolean exist, int bestRssi, String targetSsid) {
        if (!isKeepCurrWiFiConnected()) {
            if (exist && this.mNetworkBlackListManager.containedInWifiBlacklists(targetSsid)) {
                logW("onCheckAvailableWifi, but wifi blacklists contain it, ignore the result.");
                exist = false;
            }
            sendMessage(EVENT_CHECK_AVAILABLE_AP_RESULT, bestRssi, bestRssi, Boolean.valueOf(exist));
        }
    }

    public void onWifiBqeDetectionResult(int result) {
        logD("onWifiBqeDetectionResult =  " + result);
        sendMessage(EVENT_WIFI_EVALUTE_TCPRTT_RESULT, result);
    }

    public void onNotifyWifiSecurityStatus(Bundle bundle) {
        logD("onNotifyWifiSecurityStatus, bundle =  " + bundle);
        sendMessage(EVENT_WIFI_SECURITY_RESPONSE, bundle);
    }

    public synchronized void onUserConfirm(int type) {
        if (2 == type) {
            logD("UserConfirm  is OK ");
            sendMessage(EVENT_DIALOG_OK);
        } else if (1 == type) {
            logD("UserConfirm  is CANCEL");
            sendMessage(EVENT_DIALOG_CANCEL);
        }
    }

    public synchronized void userHandoverWifi() {
        logD("User Chose Rove In WiFi");
        sendMessage(EVENT_USER_ROVE_IN);
    }

    public void notifyHttpReachable(boolean isReachable) {
        logD("SEC notifyHttpReachable " + isReachable);
        sendMessage(EVENT_HTTP_REACHABLE_RESULT, Boolean.valueOf(isReachable));
    }

    private void logD(String info) {
        Log.d(TAG, info);
    }

    private void logW(String info) {
        Log.w(TAG, info);
    }

    public static void resetParameter() {
        mIsWifiManualEvaluating = false;
        mIsWifiSemiAutoEvaluating = false;
    }

    public void onDisableWiFiPro() {
        logD("WiFiProDisabledState is Enter");
        resetParameter();
        this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
        this.mSampleCollectionManager.unRegisterBroadcastReceiver();
        this.mWiFiProProtalController.handleWifiProStatusChanged(false, this.mIsPortalAp);
        this.mWifiProUIDisplayManager.cancelAllDialog();
        this.mWifiProUIDisplayManager.shownAccessNotification(false);
        this.mWiFiProEvaluateController.cleanEvaluateRecords();
        this.mHwIntelligenceWiFiManager.stop();
        stopDualBandManager();
        if (isWifiConnected()) {
            logD("WiFiProDisabledState , wifi is connect ");
            WifiInfo cInfo = this.mWifiManager.getConnectionInfo();
            if (cInfo != null && SupplicantState.COMPLETED == cInfo.getSupplicantState() && DetailedState.OBTAINING_IPADDR == WifiInfo.getDetailedStateOf(SupplicantState.COMPLETED)) {
                logD("wifi State == VERIFYING_POOR_LINK");
                this.mWsmChannel.sendMessage(GOOD_LINK_DETECTED);
            }
            setWifiCSPState(1);
        }
        diableResetVariables();
        disableTransitionNetState();
    }

    private void diableResetVariables() {
        this.mWiFiProEvaluateController.forgetUntrustedOpenAp();
        this.mWifiProConfigStore.updateWifiNoInternetAccessConfig(this.mCurrentWifiConfig, false, 0, false);
        this.mWifiProUIDisplayManager.notificateNetAccessChange(false);
        this.mLastWifiLevel = 0;
        this.mIsWiFiNoInternet = false;
        this.mWiFiProPdpSwichValue = 0;
        reSetWifiInternetState();
        this.mWifiProUIDisplayManager.cancelAllDialog();
        this.mCurrentVerfyCounter = 0;
        this.mIsUserHandoverWiFi = false;
        refreshConnectedNetWork();
        this.mIsWifiSemiAutoEvaluateComplete = false;
        resetWifiProManualConnect();
        stopDualBandMonitor();
    }

    private void disableTransitionNetState() {
        if (isWifiConnected()) {
            logD("onDisableWiFiPro,go to WifiConnectedState");
            this.mIsWiFiProConnected = false;
            this.mNetworkQosMonitor.queryNetworkQos(1, this.mIsPortalAp, this.mIsNetworkAuthen);
            transitionTo(this.mWifiConnectedState);
            return;
        }
        logD("onDisableWiFiPro, go to mWifiDisConnectedState");
        transitionTo(this.mWifiDisConnectedState);
    }

    private void registerMapNavigatingStateChanges() {
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor(MAPS_LOCATION_FLAG), false, this.mMapNavigatingStateChangeObserver);
    }

    private void registerVehicleStateChanges() {
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor(VEHICLE_STATE_FLAG), false, this.mVehicleStateChangeObserver);
    }

    private boolean isFullscreen() {
        boolean isFullscreen = false;
        if (this.mAbsPhoneWindowManager != null) {
            isFullscreen = this.mAbsPhoneWindowManager.isTopIsFullscreen();
        }
        logD("isFullscreen: " + isFullscreen);
        return isFullscreen;
    }

    private void setWifiMonitorEnabled(boolean enabled) {
        logD("setWifiLinkDataMonitorEnabled  is " + enabled);
        this.mNetworkQosMonitor.setMonitorWifiQos(1, enabled);
        this.mNetworkQosMonitor.setIpQosEnabled(enabled);
    }
}
