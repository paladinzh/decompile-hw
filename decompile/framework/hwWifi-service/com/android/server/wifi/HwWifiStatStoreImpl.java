package com.android.server.wifi;

import android.net.TrafficStats;
import android.net.wifi.SupplicantState;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.connectivitylog.LogManager;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_STABILITY_SSIDSTAT;
import com.huawei.device.connectivitychrlog.CSegEVENT_WIFI_STABILITY_STAT;
import com.huawei.device.connectivitychrlog.ChrLogBaseModel;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HwWifiStatStoreImpl implements HwWifiStatStore {
    private static final int ASSOC_REJECT_ACCESSFULL = 17;
    private static final String DEFAULT_WLAN_IFACE = "wlan0";
    private static final String DNAMESTR = "dname=";
    private static final int HW_CONNECT_REASON_CONNECTING = 1;
    private static final int HW_CONNECT_REASON_REKEY = 3;
    private static final int HW_CONNECT_REASON_ROAMING = 2;
    private static final int HW_CONNECT_REASON_UNINITIAL = 0;
    private static final String KEY_ABDISCONNECT_CNT = "key_abdisconnect_cnt:";
    private static final String KEY_ABS_ASSOCIATE_FAILED_TIMES = "key_abs_associate_failed_times:";
    private static final String KEY_ABS_ASSOCIATE_TIMES = "key_abs_associate_times:";
    private static final String KEY_ABS_MIMO_SCREEN_ON_TIME = "key_abs_mimo_screen_on_time:";
    private static final String KEY_ABS_MIMO_TIME = "key_abs_mimo_time:";
    private static final String KEY_ABS_SISO_SCREEN_ON_TIME = "key_abs_siso_screen_on_time:";
    private static final String KEY_ABS_SISO_TIME = "key_abs_siso_time:";
    private static final String KEY_ACCESS_WEB_CNT = "key_on_access_web_cnt:";
    private static final String KEY_ACCESS_WEB_FAILED_BY_PORTAL_CONNECT = "key_access_web_failed_portal_connect:";
    private static final String KEY_ACCESS_WEB_FAILED_BY_PORTAL_REDHCP = "key_access_web_failed_portal_redhcp:";
    private static final String KEY_ACCESS_WEB_FAILED_BY_PORTAL_ROAMING = "key_access_web_failed_portal_roaming:";
    private static final String KEY_ACCESS_WEB_SUCC_CNT = "key_on_access_web_succ_cnt:";
    private static final String KEY_APP_DISABLED_ABNORMAL = "key_app_disabled_abnormal:";
    private static final String KEY_APP_DISABLED_SC_SUCC = "key_disabled_sc_succ:";
    private static final String KEY_AP_AUTH_ALG = "key_auth_alg:";
    private static final String KEY_AP_EAP = "key_EAP:";
    private static final String KEY_AP_GROUP = "key_gruop:";
    private static final String KEY_AP_KEY_MGMT = "key_key_mgmt:";
    private static final String KEY_AP_PAIRWISE = "key_pairwise:";
    private static final String KEY_AP_PROTO = "key_PROTO:";
    private static final String KEY_AP_VENDORINFO = "key_APVENDORINFO:";
    private static final String KEY_ARP_REASSOC_OK_CNT = "key_arp_reassoc_ok_cnt:";
    private static final String KEY_ARP_UNREACHABLE_CNT = "key_arp_unreachable_cnt:";
    private static final String KEY_ASSOC_BY_ABS_CNT = "key_assoc_by_abs_cnt:";
    private static final String KEY_ASSOC_CNT = "key_assoc_cnt:";
    private static final String KEY_ASSOC_DURATION = "key_assoc_duration:";
    private static final String KEY_ASSOC_REJECTED_ABNORMAL = "key_assoc_rejected_abnormal:";
    private static final String KEY_ASSOC_REJECTED_SC_SUCC = "key_assoc_rejected_sc_succ:";
    private static final String KEY_ASSOC_REJECT_CNT = "key_assoc_reject_cnt:";
    private static final String KEY_ASSOC_SUCC_CNT = "key_assoc_succ_cnt:";
    private static final String KEY_AUTH_CNT = "key_auth_cnt:";
    private static final String KEY_AUTH_DURATION = "key_auth_duration:";
    private static final String KEY_AUTH_FAILED_ABNORMAL = "key_auth_failed_abnormal:";
    private static final String KEY_AUTH_FAILED_SC_SUCC = "key_auth_failed_sc_succ:";
    private static final String KEY_AUTH_SUCC_CNT = "key_auth_succ_cnt:";
    private static final String KEY_BLACK_LIST_ABNORMAL = "key_black_list_abnormal:";
    private static final String KEY_BLACK_LIST_SC_SUCC = "key_black_list_sc_succ:";
    private static final String KEY_BSSID = "key_BSSID:";
    private static final String KEY_CHR_CONNECTING_DURATION = "key_chr_connecting_duration:";
    private static final String KEY_CLOSE_CNT = "key_close_cnt:";
    private static final String KEY_CLOSE_DURATION = "key_close_duration:";
    private static final String KEY_CLOSE_SUCC_CNT = "key_close_succ_cnt:";
    private static final String KEY_CONNECTED_CNT = "key_connected_cnt:";
    private static final String KEY_CONNECTED_DURATION = "key_connected_duration:";
    private static final String KEY_CONNECT_TOTAL_CNT = "key_connect_total_cnt:";
    private static final String KEY_DHCP_AUTO_IP_CNT = "key_dhcp_auto_ip_cnt:";
    private static final String KEY_DHCP_CNT = "key_dhcp_cnt:";
    private static final String KEY_DHCP_DURATION = "key_dhcp_duration:";
    private static final String KEY_DHCP_FAILED_ABNORMAL = "key_dhcp_failed_abnormal:";
    private static final String KEY_DHCP_FAILED_SC_SUCC = "key_dhcp_failed_sc_succ:";
    private static final String KEY_DHCP_FAILED_STATIC_SC_SUCC = "key_dhcp_failed_static_sc_succ:";
    private static final String KEY_DHCP_STATIC_CNT = "key_dhcp_static_cnt:";
    private static final String KEY_DHCP_STATIC_SUCC_CNT = "key_dhcp_static_succ_cnt:";
    private static final String KEY_DHCP_SUCC_CNT = "key_dhcp_succ_cnt:";
    private static final String KEY_DISCONNECT_CNT = "key_disconnect_cnt:";
    private static final String KEY_DNS_ABNORMAL = "key_dns_abnormal:";
    private static final String KEY_DNS_MAX_TIME = "key_dns_max_time:";
    private static final String KEY_DNS_MIN_TIME = "key_dns_min_time:";
    private static final String KEY_DNS_PARSE_FAIL_CNT = "key_dns_parse_fail_cnt:";
    private static final String KEY_DNS_REQ_CNT = "key_dns_req_cnt:";
    private static final String KEY_DNS_REQ_FAIL = "key_dns_req_fail:";
    private static final String KEY_DNS_RESET_SC_SUCC = "key_dns_reset_sc_succ:";
    private static final String KEY_DNS_SC_SUCC = "key_dns_sc_succ:";
    private static final String KEY_DNS_TOT_TIME = "key_dns_tot_time:";
    private static final String KEY_FIRST_CONN_INTERNERT_FAIL_CNT = "key_first_conn_fail_cnt:";
    private static final String KEY_FIRST_CONN_INTERNET_FAIL_DURATION = "key_first_conn_fail_duration:";
    private static final String KEY_GATEWAY_ABNORMAL = "key_gateway_abnormal:";
    private static final String KEY_GOOD_RECONNECTSUCC_CNT = "key_good_reconnectsucc_cnt:";
    private static final String KEY_GOOD_RECONNECT_CNT = "key_good_reconnect_cnt:";
    private static final String KEY_GW_RESET_SC_SUCC = "key_gw_reset_sc_succ:";
    private static final String KEY_LAST_TIMESTAMP = "key_last_timestamp:";
    private static final String KEY_MOBILE_CONNECTED_DURATION = "key_mobile_connected_duration:";
    private static final String KEY_MOBILE_TRAFFIC_BYTES = "key_mobile_traffic_bytes:";
    private static final String KEY_MULTIGWCOUNT = "key_MultiGWCount:";
    private static final String KEY_NO_USERPROC_CNT = "key_no_user_proc_cnt:";
    private static final String KEY_ONLY_THE_TX_NO_RX_CNT = "key_only_tx_no_rx_cnt:";
    private static final String KEY_ONSCREEN_ABDICONNECT_CNT = "key_on_abdisconnected_cnt:";
    private static final String KEY_ONSCREEN_CONNECTED_CNT = "key_on_connected_cnt:";
    private static final String KEY_ONSCREEN_CONNECT_CNT = "key_on_connect_cnt:";
    private static final String KEY_ONSCREEN_CONNECT_DURATION = "key_on_connect_duration:";
    private static final String KEY_ONSCREEN_DISCONNECT_CNT = "key_on_disconnected_cnt:";
    private static final String KEY_ONSCREEN_RECONNECT_CNT = "key_on_reconnect_cnt:";
    private static final String KEY_ONSCREEN_RECONNECT_DURATION = "key_on_reconnect_duration:";
    private static final String KEY_OPEN_CNT = "key_open_cnt:";
    private static final String KEY_OPEN_DURATION = "key_open_duration:";
    private static final String KEY_OPEN_SUCC_CNT = "key_open_succ_cnt:";
    private static final String KEY_REASSOC_SC_CONNECT_FAILED = "key_reassoc_sc_connect_failed:";
    private static final String KEY_REASSOC_SC_SUCC = "key_reassoc_sc_succ:";
    private static final String KEY_REDHCP_ACCESS_WEB_SUCC_CNT = "key_redhcp_access_web_succ_cnt:";
    private static final String KEY_REDHCP_CNT = "key_redhcp_cnt:";
    private static final String KEY_REDHCP_DURATION = "key_redhcp_duration:";
    private static final String KEY_REDHCP_SUCC_CNT = "key_redhcp_succ_cnt:";
    private static final String KEY_REKEY_CNT = "key_rekey_cnt:";
    private static final String KEY_REKEY_DURATION = "key_rekey_duration:";
    private static final String KEY_REKEY_SUCC_CNT = "key_rekey_succ_cnt:";
    private static final String KEY_RESET_SC_CONNECT_FAILED = "key_reset_sc_connect_failed:";
    private static final String KEY_RESET_SC_SUCC = "key_reset_sc_succ:";
    private static final String KEY_RE_DHCP_SC_SUCC = "key_re_dhcp_sc_succ:";
    private static final String KEY_ROAMING_ABNORMAL = "key_roaming_abnormal:";
    private static final String KEY_ROAMING_ACCESS_WEB_CNT = "key_roaming_access_web_succ_cnt:";
    private static final String KEY_ROAMING_CNT = "key_roaming_cnt:";
    private static final String KEY_ROAMING_DURATION = "key_roaming_duration:";
    private static final String KEY_ROAMING_RESET_SC_SUCC = "key_roaming_reset_sc_succ:";
    private static final String KEY_ROAMING_SUCC_CNT = "key_roaming_succ_cnt:";
    private static final String KEY_SOFTWARE_VERSION = "key_software_version:";
    private static final String KEY_SSID = "key_SSID:";
    private static final String KEY_START_TIMESTAMP = "key_start_timestamp:";
    private static final String KEY_STATIC_IP_SC_SUCC = "key_static_ip_sc_succ:";
    private static final String KEY_TCP_RX_ABNORMAL = "key_tcp_rx_abnormal:";
    private static final String KEY_TIMESTAMP = "key_timestamp:";
    private static final String KEY_USER_ENABLE_STATIC_IP = "key_user_enable_static_ip:";
    private static final String KEY_USER_IN_LONGWAITED_CNT = "key_user_in_longwaiting_cnt:";
    private static final String KEY_WEAK_RECONNECTSUCC_CNT = "key_weak_reconnectsucc_cnt:";
    private static final String KEY_WEAK_RECONNECT_CNT = "key_weak_reconnect_cnt:";
    private static final String KEY_WLAN_CONNECTED_DURATION = "key_wlan_connected_duration:";
    private static final String KEY_WLAN_TRAFFIC_BYTES = "key_wlan_traffic_bytes:";
    private static final int MINLENOFDNAME = DNAMESTR.length();
    private static final long MIN_PERIOD_TRIGGER_BETA = 7200000;
    private static final long MIN_PERIOD_TRIGGER_CML = 86400000;
    private static int MIN_WRITE_STAT_SPAN = HwWifiStateMachine.AP_CAP_CACHE_COUNT;
    private static final long MSG_SEND_DELAY_DURATION = 1800000;
    private static final int MSG_SEND_DELAY_ID = 100;
    private static final String SEPARATOR_KEY = "\n";
    private static final String TAG = "HwWifiStatStore";
    private static HwWifiStatStore hwStatStoreIns = new HwWifiStatStoreImpl();
    private static final String mWifiStatConf = (Environment.getDataDirectory() + "/misc/wifi/wifiStatistics.txt");
    private final String WLAN_IFACE = SystemProperties.get("wifi.interface", DEFAULT_WLAN_IFACE);
    private String connectInternetFailedType = "CONNECT_INTERNET_INITIAL";
    private int connectedNetwork = 0;
    private String disConnectSSID = "";
    private long disconnectDate = 0;
    private boolean isAbnormalDisconnect = false;
    private boolean isConnectToNetwork = false;
    private boolean isScreen = false;
    private int mCloseCnt = 0;
    private int mCloseDuration = 0;
    private int mCloseSuccCnt = 0;
    private long mConnectingStartTimestamp = 0;
    private SSIDStat mCurrentStat = null;
    private long mDhcpTimestamp = 0;
    private int mDnsMaxTime = 0;
    private int mDnsMinTime = 0;
    private int mDnsReqCnt = 0;
    private int mDnsReqFail = 0;
    private int mDnsTotTime = 0;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    HwWifiStatStoreImpl.this.triggerTotalConnetedDuration(HwWifiStatStoreImpl.this.connectedNetwork);
                    HwWifiStatStoreImpl.this.triggerTotalTrafficBytes();
                    HwWifiStatStoreImpl.this.triggerConnectedDuration(SystemClock.elapsedRealtime(), HwWifiStatStoreImpl.this.mCurrentStat);
                    HwWifiStatStoreImpl.this.writeWifiCHRStat(true, true);
                    HwWifiStatStoreImpl.this.mHandler.sendEmptyMessageDelayed(100, HwWifiStatStoreImpl.MSG_SEND_DELAY_DURATION);
                    return;
                default:
                    return;
            }
        }
    };
    private int mLastConnetReason = 0;
    private long mLastDnsStatReq = 0;
    private int mLastUpdDHCPReason = 0;
    private SupplicantState mLastWpaState;
    private long mMobileTotalConnectedDuration = 0;
    private long mMobileTotalTrafficBytes = 0;
    private int mOpenCnt = 0;
    private int mOpenDuration = 0;
    private int mOpenSuccCnt = 0;
    private long mPreMobileBytes = 0;
    private long mPreTimestamp = 0;
    private long mPreWLANBytes = 0;
    private SSIDStat mPreviousStat = null;
    private ArrayList<SSIDStat> mSSIDStatList = new ArrayList();
    private String mSoftwareVersion = "";
    private long mTimestamp = 0;
    private boolean mUserTypeCommercial = true;
    private long mWifiConnectTimestamp = 0;
    private long mWifiConnectedTimestamp = 0;
    private long mWifiSwitchTimestamp = 0;
    private long mWlanTotalConnectedDuration = 0;
    private long mWlanTotalTrafficBytes = 0;
    private Object mWriteStatLock = new Object();
    private long mWriteStatTimestamp = 0;
    private long onScreenTimestamp = 0;

    private class SSIDStat {
        public String BSSID;
        public String SSID;
        private String apVendorInfo;
        private int mABSAssociateFailedTimes;
        private int mABSAssociateTimes;
        private long mABSMimoScreenOnTime;
        private long mABSMimoTime;
        private long mABSSisoScreenOnTime;
        private long mABSSisoTime;
        private int mAbDisconnectCnt;
        private int mAccessWEBCnt;
        private int mAccessWEBSuccCnt;
        private int mAccessWebFailedPortal;
        private int mAccessWebReDHCPFailedPortal;
        private int mAccessWebRoamingFailedPortal;
        private int mAppDisabledAbnromalCnt;
        private int mAppDisabledScSuccCnt;
        private int mArpReassocOkCnt;
        private int mArpUnreachableCnt;
        private int mAssocByABSCnt;
        public int mAssocCnt;
        public int mAssocDuration;
        private int mAssocRejectAccessFullCnt;
        private int mAssocRejectedAbnormalCnt;
        private int mAssocRejectedScSuccCnt;
        public int mAssocSuccCnt;
        public long mAssocingTimestamp;
        public int mAuthCnt;
        public int mAuthDuration;
        private int mAuthFailedAbnormalCnt;
        private int mAuthFailedScSuccCnt;
        public int mAuthSuccCnt;
        private int mBlackListAbnormalCnt;
        private int mBlackListScSuccCnt;
        public int mCHRConnectingDuration;
        private Date mConStart;
        public int mConnectTotalCnt;
        public int mConnectedCnt;
        public int mConnectedDuration;
        public int mConnectingDuration;
        public int mDhcpAutoIpCnt;
        public int mDhcpCnt;
        public int mDhcpDuration;
        private int mDhcpFailedAbnormalCnt;
        private int mDhcpFailedScSuccCnt;
        private int mDhcpFailedStaticScSuccCnt;
        public int mDhcpStaticCnt;
        public int mDhcpStaticSuccCnt;
        public int mDhcpSuccCnt;
        public int mDisconnectCnt;
        private int mDnsAbnormalCnt;
        private int mDnsParseFailCnt;
        private int mDnsResetScSuccCnt;
        private int mDnsScSuccCnt;
        private int mFirstConnInternetFailCnt;
        public int mFirstConnInternetFailDuration;
        private int mGatewayAbnormalCnt;
        private int mGoodReConnectCnt;
        private int mGoodReConnectSuccCnt;
        private int mGwResetScSuccCnt;
        private boolean mIsWifiproFlag;
        private Date mLastUpdate;
        private int mMultiGWCount;
        private int mNoUserProcCnt;
        private int mOnScreenAbDisconnectCnt;
        private int mOnScreenConnectCnt;
        private int mOnScreenConnectDuration;
        private int mOnScreenConnectedCnt;
        private int mOnScreenDisconnectCnt;
        private int mOnScreenReConnectDuration;
        private int mOnScreenReConnectedCnt;
        private int mOnlyTheTxNoRxCnt;
        private int mReDHCPAccessWebSuccCnt;
        private int mReDHCPCnt;
        private int mReDHCPDuration;
        private int mReDHCPSuccCnt;
        private int mReDhcpScSuccCnt;
        private int mReKEYCnt;
        private int mReKEYDuration;
        private int mReKEYSuccCnt;
        private int mReassocScCnt;
        private int mReassocScConnectFailedCnt;
        private int mResetScConnectFailedCnt;
        private int mResetScSuccCnt;
        private int mRoamingAbnormalCnt;
        private int mRoamingAccessWebSuccCnt;
        private int mRoamingCnt;
        private int mRoamingDuration;
        private int mRoamingResetScSuccCnt;
        private int mRoamingSuccCnt;
        private int mStaticIpScSuccCnt;
        private int mTcpRxAbnormalCnt;
        private int mUserEnableStaticIpCnt;
        private int mUserLongTimeWaitedCnt;
        private int mWeakReConnectCnt;
        private int mWeakReConnectSuccCnt;
        private String strAP_auth_alg;
        private String strAP_eap;
        private String strAP_gruop;
        private String strAP_key_mgmt;
        private String strAP_pairwise;
        private String strAP_proto;

        private SSIDStat() {
            this.SSID = "";
            this.BSSID = "";
            this.mAssocCnt = 0;
            this.mAssocSuccCnt = 0;
            this.mAuthCnt = 0;
            this.mAuthSuccCnt = 0;
            this.mDhcpCnt = 0;
            this.mDhcpSuccCnt = 0;
            this.mDhcpStaticCnt = 0;
            this.mDhcpAutoIpCnt = 0;
            this.mAssocingTimestamp = 0;
            this.mConnectingDuration = 0;
            this.mDhcpStaticSuccCnt = 0;
            this.mConnectedCnt = 0;
            this.mDisconnectCnt = 0;
            this.mAssocDuration = 0;
            this.mAuthDuration = 0;
            this.mDhcpDuration = 0;
            this.mConnectedDuration = 0;
            this.mFirstConnInternetFailDuration = 0;
            this.mConnectTotalCnt = 0;
            this.mCHRConnectingDuration = 0;
            this.mRoamingCnt = 0;
            this.mRoamingSuccCnt = 0;
            this.mRoamingDuration = 0;
            this.mReDHCPCnt = 0;
            this.mReDHCPSuccCnt = 0;
            this.mReDHCPDuration = 0;
            this.mReKEYCnt = 0;
            this.mReKEYSuccCnt = 0;
            this.mReKEYDuration = 0;
            this.mWeakReConnectCnt = 0;
            this.mWeakReConnectSuccCnt = 0;
            this.mGoodReConnectCnt = 0;
            this.mGoodReConnectSuccCnt = 0;
            this.mOnScreenConnectCnt = 0;
            this.mOnScreenConnectedCnt = 0;
            this.mOnScreenAbDisconnectCnt = 0;
            this.mOnScreenReConnectedCnt = 0;
            this.mOnScreenDisconnectCnt = 0;
            this.mOnScreenConnectDuration = 0;
            this.mOnScreenReConnectDuration = 0;
            this.mAccessWEBCnt = 0;
            this.mAccessWEBSuccCnt = 0;
            this.mFirstConnInternetFailCnt = 0;
            this.mOnlyTheTxNoRxCnt = 0;
            this.mDnsParseFailCnt = 0;
            this.mArpUnreachableCnt = 0;
            this.mArpReassocOkCnt = 0;
            this.mDnsAbnormalCnt = 0;
            this.mTcpRxAbnormalCnt = 0;
            this.mRoamingAbnormalCnt = 0;
            this.mGatewayAbnormalCnt = 0;
            this.mDnsScSuccCnt = 0;
            this.mReDhcpScSuccCnt = 0;
            this.mStaticIpScSuccCnt = 0;
            this.mReassocScCnt = 0;
            this.mResetScSuccCnt = 0;
            this.mUserEnableStaticIpCnt = 0;
            this.mAuthFailedAbnormalCnt = 0;
            this.mAssocRejectedAbnormalCnt = 0;
            this.mDhcpFailedAbnormalCnt = 0;
            this.mAppDisabledAbnromalCnt = 0;
            this.mAuthFailedScSuccCnt = 0;
            this.mAssocRejectedScSuccCnt = 0;
            this.mDhcpFailedScSuccCnt = 0;
            this.mAppDisabledScSuccCnt = 0;
            this.mDnsResetScSuccCnt = 0;
            this.mRoamingResetScSuccCnt = 0;
            this.mGwResetScSuccCnt = 0;
            this.mReassocScConnectFailedCnt = 0;
            this.mResetScConnectFailedCnt = 0;
            this.mBlackListScSuccCnt = 0;
            this.mBlackListAbnormalCnt = 0;
            this.mDhcpFailedStaticScSuccCnt = 0;
            this.apVendorInfo = "";
            this.strAP_proto = "";
            this.strAP_key_mgmt = "";
            this.strAP_auth_alg = "";
            this.strAP_pairwise = "";
            this.strAP_gruop = "";
            this.strAP_eap = "";
            this.mConStart = new Date();
            this.mLastUpdate = new Date();
            this.mRoamingAccessWebSuccCnt = 0;
            this.mReDHCPAccessWebSuccCnt = 0;
            this.mNoUserProcCnt = 0;
            this.mUserLongTimeWaitedCnt = 0;
            this.mMultiGWCount = 0;
            this.mAccessWebFailedPortal = 0;
            this.mAccessWebRoamingFailedPortal = 0;
            this.mAccessWebReDHCPFailedPortal = 0;
            this.mAbDisconnectCnt = 0;
            this.mIsWifiproFlag = false;
            this.mAssocRejectAccessFullCnt = 0;
            this.mAssocByABSCnt = 0;
            this.mABSAssociateTimes = 0;
            this.mABSAssociateFailedTimes = 0;
            this.mABSMimoTime = 0;
            this.mABSSisoTime = 0;
            this.mABSMimoScreenOnTime = 0;
            this.mABSSisoScreenOnTime = 0;
        }

        public boolean cmp(String ssid) {
            if (ssid == null || this.SSID == null) {
                return false;
            }
            return this.SSID.equals(ssid);
        }

        public boolean hasDataToTrigger() {
            if ((((((this.mAssocCnt + this.mAssocSuccCnt) + this.mAuthCnt) + this.mAuthSuccCnt) + (((this.mDhcpCnt + this.mDhcpSuccCnt) + this.mDhcpStaticCnt) + this.mDhcpAutoIpCnt)) + ((this.mConnectedCnt + this.mDisconnectCnt) + this.mAccessWEBSuccCnt)) + ((this.mRoamingAccessWebSuccCnt + this.mReDHCPAccessWebSuccCnt) + this.mAbDisconnectCnt) > 0) {
                return true;
            }
            return false;
        }
    }

    public static HwWifiStatStore getDefault() {
        return hwStatStoreIns;
    }

    private HwWifiStatStoreImpl() {
    }

    public void updateScreenState(boolean on) {
        this.isScreen = on;
    }

    private void rstDisconnectFlg() {
        this.disconnectDate = 0;
        this.isAbnormalDisconnect = false;
        this.disConnectSSID = "";
    }

    public void setMultiGWCount(byte count) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            stat.mMultiGWCount = count;
        }
    }

    public void incrWebSpeedStatus(int addNoUsrCnt, int addLongWaitingCnt) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            stat.mNoUserProcCnt = stat.mNoUserProcCnt + addNoUsrCnt;
            stat.mUserLongTimeWaitedCnt = stat.mUserLongTimeWaitedCnt + addLongWaitingCnt;
        }
    }

    public int getLastUpdDHCPReason() {
        return this.mLastUpdDHCPReason;
    }

    public int getLastUpdConnectReason() {
        return this.mLastConnetReason;
    }

    public void updateUserType(boolean commercialUser) {
        this.mUserTypeCommercial = commercialUser;
        LOGW("This method is no longer use");
    }

    public void updateConnectState(boolean connected) {
        SSIDStat stat = this.mCurrentStat;
        boolean flushNow = false;
        if (stat != null && !stat.mIsWifiproFlag) {
            LOGD("updateConnectState connected: " + connected + " mLastUpdDHCPReason:" + this.mLastUpdDHCPReason);
            long now = SystemClock.elapsedRealtime();
            if (connected) {
                if (this.mLastUpdDHCPReason == 2 || this.mLastUpdDHCPReason == 9) {
                    stat.mConnectedCnt++;
                    stat.mConnectingDuration += (int) (now - stat.mAssocingTimestamp);
                    this.mWifiConnectedTimestamp = now;
                    stat.mConnectTotalCnt++;
                    this.isConnectToNetwork = true;
                    if (this.isScreen && this.onScreenTimestamp > 0) {
                        stat.mOnScreenConnectedCnt = stat.mOnScreenConnectedCnt + 1;
                        stat.mOnScreenConnectDuration = stat.mOnScreenConnectDuration + ((int) (now - this.onScreenTimestamp));
                        this.onScreenTimestamp = 0;
                    }
                    flushNow = true;
                }
                triggerConnectedDuration(now, stat);
                writeWifiCHRStat(flushNow, false);
            } else {
                updateDisconnectCnt();
                if (this.mWifiConnectedTimestamp > 0) {
                    stat.mConnectedDuration = (int) (((long) stat.mConnectedDuration) + (now - this.mWifiConnectedTimestamp));
                    this.mWifiConnectedTimestamp = 0;
                    this.isAbnormalDisconnect = true;
                    if (this.isScreen) {
                        stat.mOnScreenAbDisconnectCnt = stat.mOnScreenAbDisconnectCnt + 1;
                    }
                }
                triggerConnectedDuration(now, stat);
                updateConnectInternetFailedType("CONNECT_INTERNET_INITIAL");
                writeWifiCHRStat(false, true);
            }
        }
    }

    public void updateCHRConnectFailedCount(int type) {
        SSIDStat stat = null;
        if (type == 0) {
            stat = this.mCurrentStat;
        } else if (type == 1) {
            stat = this.mPreviousStat;
        }
        if (stat != null) {
            stat.mConnectTotalCnt++;
        }
    }

    private SSIDStat geStatBySSID(String SSID) {
        for (int i = 0; i < this.mSSIDStatList.size(); i++) {
            SSIDStat item = (SSIDStat) this.mSSIDStatList.get(i);
            if (item.cmp(SSID)) {
                return item;
            }
        }
        return null;
    }

    public void setAPSSID(String ssid) {
        if (!TextUtils.isEmpty(ssid) && ssid.length() > 0 && (this.mCurrentStat == null || !ssid.equals(this.mCurrentStat.SSID))) {
            this.mPreviousStat = this.mCurrentStat;
            this.mCurrentStat = geStatBySSID(ssid);
            if (this.mCurrentStat == null) {
                this.mCurrentStat = new SSIDStat();
                this.mCurrentStat.SSID = ssid;
                this.mSSIDStatList.add(this.mCurrentStat);
            }
            LOGD("setAPSSID: " + ssid);
        }
    }

    public void updateAssocByABS() {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            stat.mAssocByABSCnt = stat.mAssocByABSCnt + 1;
        }
    }

    private void incrAccesFailedByPortal(int reason, boolean isFailedByPortal, SSIDStat stat) {
        if (isFailedByPortal) {
            switch (reason) {
                case 0:
                    stat.mAccessWebFailedPortal = stat.mAccessWebFailedPortal + 1;
                    break;
                case 2:
                    stat.mAccessWebRoamingFailedPortal = stat.mAccessWebRoamingFailedPortal + 1;
                    break;
                case 3:
                    stat.mAccessWebReDHCPFailedPortal = stat.mAccessWebReDHCPFailedPortal + 1;
                    break;
            }
        }
    }

    public void incrAccessWebRecord(int reason, boolean succ, boolean isFailedByPortal) {
        LOGD(" incrAccessWebRecord mCurrentStat= " + this.mCurrentStat + " succ=" + succ + "  reason=" + reason);
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            if (reason == 0) {
                stat.mAccessWEBCnt = stat.mAccessWEBCnt + 1;
            }
            if (succ) {
                switch (reason) {
                    case 0:
                        stat.mAccessWEBSuccCnt = stat.mAccessWEBSuccCnt + 1;
                        break;
                    case 2:
                        stat.mRoamingAccessWebSuccCnt = stat.mRoamingAccessWebSuccCnt + 1;
                        break;
                    case 3:
                        stat.mReDHCPAccessWebSuccCnt = stat.mReDHCPAccessWebSuccCnt + 1;
                        break;
                }
                triggerConnectedDuration(SystemClock.elapsedRealtime(), stat);
                writeWifiCHRStat(false, false);
                return;
            }
            incrAccesFailedByPortal(reason, isFailedByPortal, stat);
        }
    }

    public void setApVendorInfo(String apVendorInfo) {
        SSIDStat stat = this.mCurrentStat;
        if (stat == null) {
            LOGD("ignore setApVendorInfo because mCurrentStat is null, apVendorInfo:" + apVendorInfo);
            return;
        }
        int dnameIndex = stat.apVendorInfo.indexOf(DNAMESTR);
        if (dnameIndex < 0 || stat.apVendorInfo.substring(MINLENOFDNAME + dnameIndex).equals("")) {
            stat.apVendorInfo = apVendorInfo;
        } else {
            LOGD("ignore setApVendorInfo because dname is not null,apVendorInfo:" + apVendorInfo);
        }
    }

    public void setApencInfo(String strAP_proto, String strAP_key_mgmt, String strAP_auth_alg, String strAP_pairwise, String strAP_gruop, String strAP_eap) {
        SSIDStat stat = this.mCurrentStat;
        if (stat == null) {
            LOGD("ignore setApencInfo because mCurrentStat is null, strAP_key_mgmt:" + strAP_key_mgmt);
            return;
        }
        stat.strAP_proto = strAP_proto;
        stat.strAP_key_mgmt = strAP_key_mgmt;
        stat.strAP_auth_alg = strAP_auth_alg;
        stat.strAP_pairwise = strAP_pairwise;
        stat.strAP_gruop = strAP_gruop;
        stat.strAP_eap = strAP_eap;
    }

    public void setCHRConnectingSartTimestamp(long connectingStartTimestamp) {
        if (connectingStartTimestamp > 0) {
            this.mConnectingStartTimestamp = connectingStartTimestamp;
        }
    }

    private void addReConnectCnt() {
        long now = SystemClock.elapsedRealtime();
        if (this.disconnectDate == 0 || now - this.disconnectDate >= 60000) {
            LOGD("addReConnectCnt return disconnectDate=" + this.disconnectDate + ", now=" + now);
            rstDisconnectFlg();
            return;
        }
        SSIDStat stat = geStatBySSID(this.disConnectSSID);
        if (stat == null) {
            stat = new SSIDStat();
            stat.SSID = this.disConnectSSID;
            this.mSSIDStatList.add(stat);
        }
        LOGD("addReConnectCnt return disConnectSSID=" + this.disConnectSSID + ", isAbnormalDisconnect=" + this.isAbnormalDisconnect);
        if (this.isAbnormalDisconnect) {
            stat.mGoodReConnectCnt = stat.mGoodReConnectCnt + 1;
        } else {
            stat.mWeakReConnectCnt = stat.mWeakReConnectCnt + 1;
        }
    }

    private void addReConnectSuccCnt() {
        long now = SystemClock.elapsedRealtime();
        if (this.disconnectDate == 0 || now - this.disconnectDate >= 60000) {
            LOGD("addReConnectCnt return disconnectDate=" + this.disconnectDate + ", now=" + now);
            rstDisconnectFlg();
            return;
        }
        SSIDStat stat = geStatBySSID(this.disConnectSSID);
        if (stat == null) {
            LOGD("geStatBySSID null");
            stat = new SSIDStat();
            stat.SSID = this.disConnectSSID;
            this.mSSIDStatList.add(stat);
        }
        LOGD("addReConnectSuccCnt  isAbnormalDisconnect=" + this.isAbnormalDisconnect);
        if (this.isAbnormalDisconnect) {
            if (this.isScreen || this.onScreenTimestamp > 0) {
                stat.mOnScreenReConnectedCnt = stat.mOnScreenReConnectedCnt + 1;
                stat.mOnScreenReConnectDuration = (int) (((long) stat.mOnScreenReConnectDuration) + (now - this.disconnectDate));
            }
            stat.mGoodReConnectSuccCnt = stat.mGoodReConnectSuccCnt + 1;
        } else {
            stat.mWeakReConnectSuccCnt = stat.mWeakReConnectSuccCnt + 1;
        }
    }

    private void triggerConnectedDuration(long now, SSIDStat stat) {
        if (stat != null && this.mWifiConnectedTimestamp > 0) {
            stat.mConnectedDuration = (int) (((long) stat.mConnectedDuration) + (now - this.mWifiConnectedTimestamp));
            if ("FIRST_CONNECT_INTERNET_FAILED".equals(this.connectInternetFailedType)) {
                stat.mFirstConnInternetFailDuration = (int) (((long) stat.mFirstConnInternetFailDuration) + (now - this.mWifiConnectedTimestamp));
            }
            this.mWifiConnectedTimestamp = now;
        }
    }

    public void triggerConnectedDuration() {
        SSIDStat stat = this.mCurrentStat;
        long now = SystemClock.elapsedRealtime();
        if (stat != null && this.mWifiConnectedTimestamp > 0) {
            stat.mConnectedDuration = (int) (((long) stat.mConnectedDuration) + (now - this.mWifiConnectedTimestamp));
            if ("FIRST_CONNECT_INTERNET_FAILED".equals(this.connectInternetFailedType)) {
                stat.mFirstConnInternetFailDuration = (int) (((long) stat.mFirstConnInternetFailDuration) + (now - this.mWifiConnectedTimestamp));
            }
            this.mWifiConnectedTimestamp = now;
            triggerTotalTrafficBytes();
            triggerTotalConnetedDuration(this.connectedNetwork);
            writeWifiCHRStat(true, false);
        }
    }

    public void triggerCHRConnectingDuration(long connectingDuration) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null && connectingDuration > 0) {
            HwWifiCHRStateManager hwWifiCHRStateManager = HwWifiCHRStateManagerImpl.getDefault();
            if (hwWifiCHRStateManager != null) {
                hwWifiCHRStateManager.updateConnectSuccessTime(connectingDuration);
            }
            stat.mCHRConnectingDuration = (int) (((long) stat.mCHRConnectingDuration) + connectingDuration);
        }
    }

    public void setAbDisconnectFlg(String AP_SSID) {
        SSIDStat stat = this.mCurrentStat;
        this.isAbnormalDisconnect = true;
        this.disConnectSSID = AP_SSID;
        this.disconnectDate = SystemClock.elapsedRealtime();
        if (stat != null) {
            stat.mAbDisconnectCnt = stat.mAbDisconnectCnt + 1;
        }
    }

    public void updateCurrentConnectType(int type) {
        long now = SystemClock.elapsedRealtime();
        Handler handler;
        switch (type) {
            case 0:
                triggerTotalTrafficBytes();
                triggerTotalConnetedDuration(this.connectedNetwork);
                this.connectedNetwork = 0;
                handler = this.mHandler;
                synchronized (handler) {
                    this.mHandler.removeMessages(100);
                    break;
                }
            case 1:
                this.mPreTimestamp = now;
                this.connectedNetwork = 1;
                handler = this.mHandler;
                synchronized (handler) {
                    this.mHandler.removeMessages(100);
                    this.mHandler.sendEmptyMessageDelayed(100, MSG_SEND_DELAY_DURATION);
                    break;
                }
            case 2:
                this.mPreTimestamp = now;
                triggerTotalTrafficBytes();
                this.connectedNetwork = 2;
                handler = this.mHandler;
                synchronized (handler) {
                    this.mHandler.removeMessages(100);
                    this.mHandler.sendEmptyMessageDelayed(100, MSG_SEND_DELAY_DURATION);
                    break;
                }
            default:
                return;
        }
    }

    public void triggerTotalTrafficBytes() {
        long currentMobileTrafficBytes = (TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes()) - this.mPreMobileBytes;
        long currentWlanTrafficByts = (TrafficStats.getTxBytes(this.WLAN_IFACE) + TrafficStats.getRxBytes(this.WLAN_IFACE)) - this.mPreWLANBytes;
        if (currentMobileTrafficBytes > 0) {
            this.mMobileTotalTrafficBytes += currentMobileTrafficBytes;
        }
        if (currentWlanTrafficByts > 0) {
            this.mWlanTotalTrafficBytes += currentWlanTrafficByts;
        }
        if (TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes() > 0) {
            this.mPreMobileBytes = TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes();
        }
        if (TrafficStats.getTxBytes(this.WLAN_IFACE) + TrafficStats.getRxBytes(this.WLAN_IFACE) > 0) {
            this.mPreWLANBytes = TrafficStats.getTxBytes(this.WLAN_IFACE) + TrafficStats.getRxBytes(this.WLAN_IFACE);
        }
    }

    public void triggerTotalConnetedDuration(int connectedType) {
        long now = SystemClock.elapsedRealtime();
        if (connectedType != 0) {
            switch (connectedType) {
                case 1:
                    long currentWlanConnectedDuration = now - this.mPreTimestamp;
                    if (currentWlanConnectedDuration > 0) {
                        this.mWlanTotalConnectedDuration += currentWlanConnectedDuration;
                        this.mPreTimestamp = now;
                        break;
                    }
                    return;
                case 2:
                    long currentMobileConnectedDuration = now - this.mPreTimestamp;
                    if (currentMobileConnectedDuration > 0) {
                        this.mMobileTotalConnectedDuration += currentMobileConnectedDuration;
                        this.mPreTimestamp = now;
                        break;
                    }
                    return;
            }
        }
    }

    public void setApMac(String apMac) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            if (LogManager.getInstance().isCommercialUser()) {
                apMac = maskMacAddress(apMac);
            }
            stat.BSSID = apMac;
        }
    }

    private String maskMacAddress(String macAddress) {
        if (macAddress != null) {
            if (macAddress.split(":").length >= 4) {
                return String.format("%s:%s:%s:%s:FF:FF", new Object[]{macAddress.split(":")[0], macAddress.split(":")[1], macAddress.split(":")[2], macAddress.split(":")[3]});
            }
        }
        return macAddress;
    }

    public void updateConnectInternetFailedType(String reasonType) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            if (reasonType.equals("CONNECT_INTERNET_INITIAL")) {
                this.connectInternetFailedType = "CONNECT_INTERNET_INITIAL";
            } else if (reasonType.equals("FIRST_CONNECT_INTERNET_FAILED")) {
                this.connectInternetFailedType = "FIRST_CONNECT_INTERNET_FAILED";
                stat.mFirstConnInternetFailCnt = stat.mFirstConnInternetFailCnt + 1;
            } else if (reasonType.equals("ONLY_THE_TX_NO_RX")) {
                this.connectInternetFailedType = "ONLY_THE_TX_NO_RX";
                stat.mOnlyTheTxNoRxCnt = stat.mOnlyTheTxNoRxCnt + 1;
            } else if (reasonType.equals("DNS_PARSE_FAILED")) {
                this.connectInternetFailedType = "DNS_PARSE_FAILED";
                stat.mDnsParseFailCnt = stat.mDnsParseFailCnt + 1;
            } else if (reasonType.equals("ARP_UNREACHABLE")) {
                this.connectInternetFailedType = "ARP_UNREACHABLE";
                stat.mArpUnreachableCnt = stat.mArpUnreachableCnt + 1;
            } else if (reasonType.equals("ARP_REASSOC_OK")) {
                this.connectInternetFailedType = "ARP_REASSOC_OK";
                stat.mArpReassocOkCnt = stat.mArpReassocOkCnt + 1;
            }
            writeWifiCHRStat(true, false);
        }
    }

    public void handleSupplicantStateChange(SupplicantState state, boolean wifiprotempflag) {
        SSIDStat stat = this.mCurrentStat;
        if (stat == null) {
            LOGD("ignore handleSupplicantStateChange because stat is null, state:" + state);
            return;
        }
        if (state == SupplicantState.ASSOCIATING) {
            stat.mIsWifiproFlag = wifiprotempflag;
        }
        if (!stat.mIsWifiproFlag) {
            long now = SystemClock.elapsedRealtime();
            boolean triggerChr = false;
            if (state == SupplicantState.ASSOCIATING) {
                this.mLastConnetReason = 1;
                stat.mAssocCnt++;
                addReConnectCnt();
                this.onScreenTimestamp = 0;
                if (this.isScreen) {
                    stat.mOnScreenConnectCnt = stat.mOnScreenConnectCnt + 1;
                    this.onScreenTimestamp = now;
                }
                this.mWifiConnectTimestamp = now;
                stat.mAssocingTimestamp = this.mWifiConnectTimestamp;
            } else if (state == SupplicantState.ASSOCIATED) {
                if (this.mLastWpaState == SupplicantState.ASSOCIATING) {
                    stat.mAssocSuccCnt++;
                    stat.mAssocDuration += (int) (now - this.mWifiConnectTimestamp);
                    stat.mAuthCnt++;
                    this.mWifiConnectTimestamp = now;
                } else if (this.mLastWpaState == SupplicantState.COMPLETED) {
                    this.mLastConnetReason = 2;
                    stat.mRoamingCnt = stat.mRoamingCnt + 1;
                    this.mWifiConnectTimestamp = now;
                }
            } else if (state == SupplicantState.COMPLETED) {
                if (1 == this.mLastConnetReason) {
                    stat.mAuthSuccCnt++;
                    stat.mAuthDuration += (int) (now - this.mWifiConnectTimestamp);
                    this.mWifiConnectTimestamp = now;
                    addReConnectSuccCnt();
                    rstDisconnectFlg();
                } else if (2 == this.mLastConnetReason) {
                    stat.mRoamingSuccCnt = stat.mRoamingSuccCnt + 1;
                    stat.mRoamingDuration = stat.mRoamingDuration + ((int) (now - this.mWifiConnectTimestamp));
                    this.mWifiConnectTimestamp = now;
                    triggerChr = false;
                } else if (3 == this.mLastConnetReason) {
                    stat.mReKEYSuccCnt = stat.mReKEYSuccCnt + 1;
                    stat.mReKEYDuration = stat.mReKEYDuration + ((int) (now - this.mWifiConnectTimestamp));
                    this.mWifiConnectTimestamp = now;
                    triggerChr = false;
                }
                this.mLastConnetReason = 0;
            } else if (state == SupplicantState.FOUR_WAY_HANDSHAKE || state == SupplicantState.GROUP_HANDSHAKE) {
                if (this.mLastWpaState == SupplicantState.COMPLETED) {
                    this.mLastConnetReason = 3;
                    stat.mReKEYCnt = stat.mReKEYCnt + 1;
                    this.mWifiConnectTimestamp = now;
                }
            } else if (state == SupplicantState.DISCONNECTED) {
                this.disConnectSSID = stat.SSID;
                if (this.mLastConnetReason != 0) {
                    this.mWifiConnectTimestamp = now;
                    triggerChr = true;
                    this.mLastConnetReason = 0;
                }
                if (this.mWifiConnectedTimestamp > 0) {
                    stat.mConnectedDuration = (int) (((long) stat.mConnectedDuration) + (now - this.mWifiConnectedTimestamp));
                    this.mWifiConnectedTimestamp = 0;
                    this.disconnectDate = now;
                    triggerChr = false;
                    if (this.isScreen) {
                        stat.mOnScreenDisconnectCnt = stat.mOnScreenDisconnectCnt + 1;
                    }
                }
            }
            stat.mLastUpdate = new Date();
            this.mLastWpaState = state;
            if (this.mWifiConnectTimestamp == now) {
                triggerConnectedDuration(now, stat);
                writeWifiCHRStat(false, triggerChr);
            }
        }
    }

    public void updateDhcpState(int state) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null && !stat.mIsWifiproFlag) {
            long now = SystemClock.elapsedRealtime();
            boolean triggerChr = false;
            if (state == 0) {
                this.mDhcpTimestamp = now;
                stat.mDhcpCnt++;
            } else if (state == 2) {
                stat.mDhcpDuration += (int) (now - this.mDhcpTimestamp);
                stat.mDhcpSuccCnt++;
                triggerChr = false;
            } else if (state == 10) {
                this.mDhcpTimestamp = now;
                stat.mReDHCPCnt = stat.mReDHCPCnt + 1;
            } else if (state == 3) {
                stat.mReDHCPDuration = stat.mReDHCPDuration + ((int) (now - this.mDhcpTimestamp));
                stat.mReDHCPSuccCnt = stat.mReDHCPSuccCnt + 1;
            } else if (state == 4) {
                stat.mDhcpCnt++;
            } else if (state == 5) {
                stat.mReDHCPCnt = stat.mReDHCPCnt + 1;
            } else if (state == 8) {
                stat.mDhcpStaticCnt++;
            } else if (state == 9) {
                stat.mDhcpStaticSuccCnt++;
            } else if (state == 16) {
                stat.mDhcpAutoIpCnt++;
            } else {
                return;
            }
            this.mLastUpdDHCPReason = state;
            triggerConnectedDuration(now, stat);
            writeWifiCHRStat(false, triggerChr);
        }
    }

    public void updateWifiState(int state) {
    }

    public void updateWifiState(boolean enable, boolean success) {
        if (enable) {
            this.mOpenCnt++;
            if (success) {
                this.mOpenSuccCnt++;
                this.mOpenDuration += (int) (SystemClock.elapsedRealtime() - this.mWifiSwitchTimestamp);
            }
            triggerConnectedDuration(SystemClock.elapsedRealtime(), this.mCurrentStat);
            writeWifiCHRStat(true, false);
        } else {
            this.mCloseCnt++;
            if (success) {
                this.mCloseSuccCnt++;
                this.mCloseDuration += (int) (SystemClock.elapsedRealtime() - this.mWifiSwitchTimestamp);
                updateDisconnectCnt();
            }
            triggerConnectedDuration(SystemClock.elapsedRealtime(), this.mCurrentStat);
            updateConnectInternetFailedType("CONNECT_INTERNET_INITIAL");
            this.mWifiConnectedTimestamp = 0;
            writeWifiCHRStat(true, true);
        }
        this.mWifiSwitchTimestamp = SystemClock.elapsedRealtime();
    }

    public void handleWiFiDnsStats(int r23) {
        /* JADX: method processing error */
/*
Error: java.lang.IndexOutOfBoundsException: bitIndex < 0: -1
	at java.util.BitSet.get(BitSet.java:623)
	at jadx.core.dex.visitors.CodeShrinker$ArgsInfo.usedArgAssign(CodeShrinker.java:138)
	at jadx.core.dex.visitors.CodeShrinker$ArgsInfo.canMove(CodeShrinker.java:129)
	at jadx.core.dex.visitors.CodeShrinker$ArgsInfo.checkInline(CodeShrinker.java:93)
	at jadx.core.dex.visitors.CodeShrinker.shrinkBlock(CodeShrinker.java:223)
	at jadx.core.dex.visitors.CodeShrinker.shrinkMethod(CodeShrinker.java:38)
	at jadx.core.dex.visitors.regions.LoopRegionVisitor.checkArrayForEach(LoopRegionVisitor.java:196)
	at jadx.core.dex.visitors.regions.LoopRegionVisitor.checkForIndexedLoop(LoopRegionVisitor.java:119)
	at jadx.core.dex.visitors.regions.LoopRegionVisitor.processLoopRegion(LoopRegionVisitor.java:65)
	at jadx.core.dex.visitors.regions.LoopRegionVisitor.enterRegion(LoopRegionVisitor.java:52)
	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseInternal(DepthRegionTraversal.java:56)
	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseInternal(DepthRegionTraversal.java:58)
	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseInternal(DepthRegionTraversal.java:58)
	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseInternal(DepthRegionTraversal.java:58)
	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseInternal(DepthRegionTraversal.java:58)
	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseInternal(DepthRegionTraversal.java:58)
	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseInternal(DepthRegionTraversal.java:58)
	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseInternal(DepthRegionTraversal.java:58)
	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverse(DepthRegionTraversal.java:18)
	at jadx.core.dex.visitors.regions.LoopRegionVisitor.visit(LoopRegionVisitor.java:46)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r22 = this;
        r12 = android.os.SystemClock.elapsedRealtime();
        if (r23 != 0) goto L_0x000b;
    L_0x0006:
        r0 = r22;
        r0.mLastDnsStatReq = r12;
        return;
    L_0x000b:
        if (r23 < 0) goto L_0x001c;
    L_0x000d:
        r0 = r22;
        r0 = r0.mLastDnsStatReq;
        r18 = r0;
        r18 = r12 - r18;
        r20 = 300000; // 0x493e0 float:4.2039E-40 double:1.482197E-318;
        r18 = (r18 > r20 ? 1 : (r18 == r20 ? 0 : -1));
        if (r18 >= 0) goto L_0x001d;
    L_0x001c:
        return;
    L_0x001d:
        r0 = r22;
        r0.mLastDnsStatReq = r12;
        r4 = "";
        r18 = android.common.HwFrameworkFactory.getHwInnerNetworkManager();	 Catch:{ Exception -> 0x00d1 }
        r0 = r18;	 Catch:{ Exception -> 0x00d1 }
        r1 = r23;	 Catch:{ Exception -> 0x00d1 }
        r4 = r0.getWiFiDnsStats(r1);	 Catch:{ Exception -> 0x00d1 }
    L_0x0030:
        r18 = android.text.TextUtils.isEmpty(r4);
        if (r18 != 0) goto L_0x00ea;
    L_0x0036:
        r18 = ";";
        r0 = r18;
        r16 = r4.split(r0);
        r8 = 0;
    L_0x0040:
        r0 = r16;
        r0 = r0.length;
        r18 = r0;
        r0 = r18;
        if (r8 >= r0) goto L_0x00dd;
    L_0x0049:
        r15 = r16[r8];
        r18 = ",";
        r0 = r18;
        r14 = r15.split(r0);
        r0 = r14.length;
        r18 = r0;
        r19 = 6;
        r0 = r18;
        r1 = r19;
        if (r0 != r1) goto L_0x00cd;
    L_0x005f:
        r18 = 1;
        r18 = r14[r18];	 Catch:{ NumberFormatException -> 0x00eb }
        r11 = java.lang.Integer.parseInt(r18);	 Catch:{ NumberFormatException -> 0x00eb }
        r18 = 2;	 Catch:{ NumberFormatException -> 0x00eb }
        r18 = r14[r18];	 Catch:{ NumberFormatException -> 0x00eb }
        r7 = java.lang.Integer.parseInt(r18);	 Catch:{ NumberFormatException -> 0x00eb }
        r18 = 3;	 Catch:{ NumberFormatException -> 0x00eb }
        r18 = r14[r18];	 Catch:{ NumberFormatException -> 0x00eb }
        r9 = java.lang.Integer.parseInt(r18);	 Catch:{ NumberFormatException -> 0x00eb }
        r18 = 4;	 Catch:{ NumberFormatException -> 0x00eb }
        r18 = r14[r18];	 Catch:{ NumberFormatException -> 0x00eb }
        r10 = java.lang.Integer.parseInt(r18);	 Catch:{ NumberFormatException -> 0x00eb }
        r18 = 5;	 Catch:{ NumberFormatException -> 0x00eb }
        r18 = r14[r18];	 Catch:{ NumberFormatException -> 0x00eb }
        r17 = java.lang.Integer.parseInt(r18);	 Catch:{ NumberFormatException -> 0x00eb }
        r0 = r22;	 Catch:{ NumberFormatException -> 0x00eb }
        r0 = r0.mDnsReqCnt;	 Catch:{ NumberFormatException -> 0x00eb }
        r18 = r0;	 Catch:{ NumberFormatException -> 0x00eb }
        r18 = r18 + r11;	 Catch:{ NumberFormatException -> 0x00eb }
        r0 = r18;	 Catch:{ NumberFormatException -> 0x00eb }
        r1 = r22;	 Catch:{ NumberFormatException -> 0x00eb }
        r1.mDnsReqCnt = r0;	 Catch:{ NumberFormatException -> 0x00eb }
        r0 = r22;	 Catch:{ NumberFormatException -> 0x00eb }
        r0 = r0.mDnsReqFail;	 Catch:{ NumberFormatException -> 0x00eb }
        r18 = r0;	 Catch:{ NumberFormatException -> 0x00eb }
        r18 = r18 + r7;	 Catch:{ NumberFormatException -> 0x00eb }
        r0 = r18;	 Catch:{ NumberFormatException -> 0x00eb }
        r1 = r22;	 Catch:{ NumberFormatException -> 0x00eb }
        r1.mDnsReqFail = r0;	 Catch:{ NumberFormatException -> 0x00eb }
        r0 = r22;	 Catch:{ NumberFormatException -> 0x00eb }
        r0 = r0.mDnsMaxTime;	 Catch:{ NumberFormatException -> 0x00eb }
        r18 = r0;	 Catch:{ NumberFormatException -> 0x00eb }
        r18 = r18 + r9;	 Catch:{ NumberFormatException -> 0x00eb }
        r0 = r18;	 Catch:{ NumberFormatException -> 0x00eb }
        r1 = r22;	 Catch:{ NumberFormatException -> 0x00eb }
        r1.mDnsMaxTime = r0;	 Catch:{ NumberFormatException -> 0x00eb }
        r0 = r22;	 Catch:{ NumberFormatException -> 0x00eb }
        r0 = r0.mDnsMinTime;	 Catch:{ NumberFormatException -> 0x00eb }
        r18 = r0;	 Catch:{ NumberFormatException -> 0x00eb }
        r18 = r18 + r10;	 Catch:{ NumberFormatException -> 0x00eb }
        r0 = r18;	 Catch:{ NumberFormatException -> 0x00eb }
        r1 = r22;	 Catch:{ NumberFormatException -> 0x00eb }
        r1.mDnsMinTime = r0;	 Catch:{ NumberFormatException -> 0x00eb }
        r0 = r22;	 Catch:{ NumberFormatException -> 0x00eb }
        r0 = r0.mDnsTotTime;	 Catch:{ NumberFormatException -> 0x00eb }
        r18 = r0;	 Catch:{ NumberFormatException -> 0x00eb }
        r18 = r18 + r17;	 Catch:{ NumberFormatException -> 0x00eb }
        r0 = r18;	 Catch:{ NumberFormatException -> 0x00eb }
        r1 = r22;	 Catch:{ NumberFormatException -> 0x00eb }
        r1.mDnsTotTime = r0;	 Catch:{ NumberFormatException -> 0x00eb }
    L_0x00cd:
        r8 = r8 + 1;
        goto L_0x0040;
    L_0x00d1:
        r5 = move-exception;
        r18 = "HwWifiStatStore";
        r19 = "Exception in handleWiFiDnsStats";
        android.util.Log.e(r18, r19);
        goto L_0x0030;
    L_0x00dd:
        r18 = 1;
        r19 = 0;
        r0 = r22;
        r1 = r18;
        r2 = r19;
        r0.writeWifiCHRStat(r1, r2);
    L_0x00ea:
        return;
    L_0x00eb:
        r6 = move-exception;
        goto L_0x00cd;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.HwWifiStatStoreImpl.handleWiFiDnsStats(int):void");
    }

    public void updateWifiTriggerState(boolean enable) {
        this.mWifiSwitchTimestamp = SystemClock.elapsedRealtime();
        if (!enable) {
            HwWifiCHRStateManagerImpl.getDefault().clearDisconnectData();
        }
    }

    public void updateReasonCode(int EventId, int reasonCode) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null && !stat.mIsWifiproFlag && EventId == 83 && reasonCode == 17) {
            stat.mAssocRejectAccessFullCnt = stat.mAssocRejectAccessFullCnt + 1;
        }
    }

    public void updateScCHRCount(int type) {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null) {
            switch (type) {
                case 0:
                    stat.mDnsAbnormalCnt = stat.mDnsAbnormalCnt + 1;
                    break;
                case 1:
                    stat.mTcpRxAbnormalCnt = stat.mTcpRxAbnormalCnt + 1;
                    break;
                case 2:
                    stat.mRoamingAbnormalCnt = stat.mRoamingAbnormalCnt + 1;
                    break;
                case 3:
                    stat.mGatewayAbnormalCnt = stat.mGatewayAbnormalCnt + 1;
                    break;
                case 4:
                    stat.mDnsScSuccCnt = stat.mDnsScSuccCnt + 1;
                    break;
                case 5:
                    stat.mReDhcpScSuccCnt = stat.mReDhcpScSuccCnt + 1;
                    break;
                case 6:
                    stat.mStaticIpScSuccCnt = stat.mStaticIpScSuccCnt + 1;
                    break;
                case 7:
                    stat.mReassocScCnt = stat.mReassocScCnt + 1;
                    break;
                case 8:
                    stat.mResetScSuccCnt = stat.mResetScSuccCnt + 1;
                    break;
                case 9:
                    stat.mUserEnableStaticIpCnt = stat.mUserEnableStaticIpCnt + 1;
                    break;
                case 10:
                    stat.mAuthFailedAbnormalCnt = stat.mAuthFailedAbnormalCnt + 1;
                    break;
                case 11:
                    stat.mAssocRejectedAbnormalCnt = stat.mAssocRejectedAbnormalCnt + 1;
                    break;
                case 12:
                    stat.mDhcpFailedAbnormalCnt = stat.mDhcpFailedAbnormalCnt + 1;
                    break;
                case 13:
                    stat.mAppDisabledAbnromalCnt = stat.mAppDisabledAbnromalCnt + 1;
                    break;
                case 14:
                    stat.mAuthFailedScSuccCnt = stat.mAuthFailedScSuccCnt + 1;
                    break;
                case 15:
                    stat.mAssocRejectedScSuccCnt = stat.mAssocRejectedScSuccCnt + 1;
                    break;
                case 16:
                    stat.mDhcpFailedScSuccCnt = stat.mDhcpFailedScSuccCnt + 1;
                    break;
                case 17:
                    stat.mAppDisabledScSuccCnt = stat.mAppDisabledScSuccCnt + 1;
                    break;
                case 18:
                    stat.mReassocScConnectFailedCnt = stat.mReassocScConnectFailedCnt + 1;
                    break;
                case 19:
                    stat.mResetScConnectFailedCnt = stat.mResetScConnectFailedCnt + 1;
                    break;
                case 20:
                    stat.mDnsResetScSuccCnt = stat.mDnsResetScSuccCnt + 1;
                    break;
                case 21:
                    stat.mRoamingResetScSuccCnt = stat.mRoamingResetScSuccCnt + 1;
                    break;
                case 22:
                    stat.mGwResetScSuccCnt = stat.mGwResetScSuccCnt + 1;
                    break;
                case 23:
                    stat.mBlackListScSuccCnt = stat.mBlackListScSuccCnt + 1;
                    break;
                case 24:
                    stat.mBlackListAbnormalCnt = stat.mBlackListAbnormalCnt + 1;
                    break;
                case 25:
                    stat.mDhcpFailedStaticScSuccCnt = stat.mDhcpFailedStaticScSuccCnt + 1;
                    break;
            }
            writeWifiCHRStat(true, false);
        }
    }

    public void updateDisconnectCnt() {
        SSIDStat stat = this.mCurrentStat;
        if (stat != null && this.isConnectToNetwork) {
            stat.mDisconnectCnt++;
            this.isConnectToNetwork = false;
        }
    }

    public boolean isConnectToNetwork() {
        return this.isConnectToNetwork;
    }

    public List<ChrLogBaseModel> getWifiStatModel(Date date) {
        List<ChrLogBaseModel> result = new ArrayList();
        CSegEVENT_WIFI_STABILITY_STAT model = new CSegEVENT_WIFI_STABILITY_STAT();
        model.tmTimeStamp.setValue(date);
        model.ucCardIndex.setValue(0);
        if ((((this.mOpenCnt + this.mOpenSuccCnt) + this.mCloseCnt) + this.mCloseSuccCnt) + this.mDnsReqCnt > 0) {
            model.iOpenCount.setValue(this.mOpenCnt);
            model.iOpenSuccCount.setValue(this.mOpenSuccCnt);
            model.iCloseCount.setValue(this.mCloseCnt);
            model.iCloseSuccCount.setValue(this.mCloseSuccCnt);
            model.iOpenDuration.setValue(this.mOpenDuration);
            model.iCloseDuration.setValue(this.mCloseDuration);
            model.iDnsReqCnt.setValue(this.mDnsReqCnt);
            model.iDnsReqFail.setValue(this.mDnsReqFail);
            model.iDnsMaxTime.setValue(this.mDnsMaxTime);
            model.iDnsMinTime.setValue(this.mDnsMinTime);
            model.iDnsTotTime.setValue(this.mDnsTotTime);
            model.lWlanTotalTrafficBytes.setValue(this.mWlanTotalTrafficBytes);
            model.lMobileTotalTrafficBytes.setValue(this.mMobileTotalTrafficBytes);
            model.lWlanTotalConnectedDuration.setValue(this.mWlanTotalConnectedDuration);
            model.lMobileTotalConnectedDuration.setValue(this.mMobileTotalConnectedDuration);
            result.add(model);
        }
        for (int i = 0; i < this.mSSIDStatList.size(); i++) {
            SSIDStat stat = (SSIDStat) this.mSSIDStatList.get(i);
            if (stat.hasDataToTrigger()) {
                CSegEVENT_WIFI_STABILITY_SSIDSTAT SSIDItem = new CSegEVENT_WIFI_STABILITY_SSIDSTAT();
                SSIDItem.tmTimeStamp.setValue(date);
                SSIDItem.strSSID.setValue(stat.SSID);
                SSIDItem.strBSSID.setValue(stat.BSSID);
                SSIDItem.iAssocCount.setValue(stat.mAssocCnt);
                SSIDItem.iAssocRejectAccessFullCnt.setValue(stat.mAssocRejectAccessFullCnt);
                SSIDItem.iAssocByABSCnt.setValue(stat.mAssocByABSCnt);
                SSIDItem.iAssocSuccCount.setValue(stat.mAssocSuccCnt);
                SSIDItem.iAuthCount.setValue(stat.mAuthCnt);
                SSIDItem.iAuthSuccCount.setValue(stat.mAuthSuccCnt);
                SSIDItem.iDhcpCount.setValue(stat.mDhcpCnt);
                SSIDItem.iDhcpSuccCount.setValue(stat.mDhcpSuccCnt);
                SSIDItem.iDhcpStaticCount.setValue(stat.mDhcpStaticCnt);
                SSIDItem.iDHCPStaticAccessCount.setValue(stat.mDhcpStaticSuccCnt);
                SSIDItem.iDHCPAutoIpCount.setValue(stat.mDhcpAutoIpCnt);
                SSIDItem.iConnectedCount.setValue(stat.mConnectedCnt);
                SSIDItem.iConnectTotalCount.setValue(stat.mConnectTotalCnt);
                SSIDItem.iAbnormalDisconnCount.setValue(stat.mAbDisconnectCnt);
                SSIDItem.iDisconnectCnt.setValue(stat.mDisconnectCnt);
                SSIDItem.iAssocDuration.setValue(stat.mAssocDuration);
                SSIDItem.iAuthDuration.setValue(stat.mAuthDuration);
                SSIDItem.iDhcpDuration.setValue(stat.mDhcpDuration);
                SSIDItem.iConnectedDuration.setValue(stat.mConnectedDuration);
                SSIDItem.iFirstConnInternetFailDuration.setValue(stat.mFirstConnInternetFailDuration);
                SSIDItem.iCHRConnectingDuration.setValue(stat.mCHRConnectingDuration);
                SSIDItem.iRoamingCnt.setValue(stat.mRoamingCnt);
                SSIDItem.iRoamingSuccCnt.setValue(stat.mRoamingSuccCnt);
                SSIDItem.iRoamingDuration.setValue(stat.mRoamingDuration);
                SSIDItem.iReDHCPCnt.setValue(stat.mReDHCPCnt);
                SSIDItem.iReDHCPSuccCnt.setValue(stat.mReDHCPSuccCnt);
                SSIDItem.iReDHCPDuration.setValue(stat.mReDHCPDuration);
                SSIDItem.iReKeyCnt.setValue(stat.mReKEYCnt);
                SSIDItem.iReKeySuccCnt.setValue(stat.mReKEYSuccCnt);
                SSIDItem.iReKeyDuration.setValue(stat.mReKEYDuration);
                SSIDItem.iGoodReConnectCnt.setValue(stat.mGoodReConnectCnt);
                SSIDItem.iGoodReConnectSuccCnt.setValue(stat.mGoodReConnectSuccCnt);
                SSIDItem.iWeakReConnectCnt.setValue(stat.mWeakReConnectCnt);
                SSIDItem.iWeakReConnectSuccCnt.setValue(stat.mWeakReConnectSuccCnt);
                SSIDItem.iOnScreenConnectCnt.setValue(stat.mOnScreenConnectCnt);
                SSIDItem.iOnScreenConnectedCnt.setValue(stat.mOnScreenConnectedCnt);
                SSIDItem.iOnScreenAbDisconnectCnt.setValue(stat.mOnScreenAbDisconnectCnt);
                SSIDItem.iOnScreenDisconnectCnt.setValue(stat.mOnScreenDisconnectCnt);
                SSIDItem.iOnSceenConnectedDuration.setValue(stat.mOnScreenConnectDuration);
                SSIDItem.iOnSceenReConnectedCnt.setValue(stat.mOnScreenReConnectedCnt);
                SSIDItem.iDnsAbnormalCnt.setValue(stat.mDnsAbnormalCnt);
                SSIDItem.iTcpRxAbnormalCnt.setValue(stat.mTcpRxAbnormalCnt);
                SSIDItem.iRoamingAbnormalCnt.setValue(stat.mRoamingAbnormalCnt);
                SSIDItem.iGatewayAbnormalCnt.setValue(stat.mGatewayAbnormalCnt);
                SSIDItem.iDnsScSuccCnt.setValue(stat.mDnsScSuccCnt);
                SSIDItem.iReDhcpScSuccCnt.setValue(stat.mReDhcpScSuccCnt);
                SSIDItem.iStaticIpScSuccCnt.setValue(stat.mStaticIpScSuccCnt);
                SSIDItem.iReassocScSuccCnt.setValue(stat.mReassocScCnt);
                SSIDItem.iResetScSuccCnt.setValue(stat.mResetScSuccCnt);
                SSIDItem.iUserEnableStaticIpCnt.setValue(stat.mUserEnableStaticIpCnt);
                SSIDItem.iAuthFailedAbnormalCnt.setValue(stat.mAuthFailedAbnormalCnt);
                SSIDItem.iAssocRejectedAbnormalCnt.setValue(stat.mAssocRejectedAbnormalCnt);
                SSIDItem.iDhcpFailedAbnormalCnt.setValue(stat.mDhcpFailedAbnormalCnt);
                SSIDItem.iAppDisabledAbnromalCnt.setValue(stat.mAppDisabledAbnromalCnt);
                SSIDItem.iAuthFailedScSuccCnt.setValue(stat.mAuthFailedScSuccCnt);
                SSIDItem.iAssocRejectedScSuccCnt.setValue(stat.mAssocRejectedScSuccCnt);
                SSIDItem.iDhcpFailedScSuccCnt.setValue(stat.mDhcpFailedScSuccCnt);
                SSIDItem.iAppDisabledScSuccCnt.setValue(stat.mAppDisabledScSuccCnt);
                SSIDItem.iDnsResetScSuccCnt.setValue(stat.mDnsResetScSuccCnt);
                SSIDItem.iRoamingResetScSuccCnt.setValue(stat.mRoamingResetScSuccCnt);
                SSIDItem.iGwResetScSuccCnt.setValue(stat.mGwResetScSuccCnt);
                SSIDItem.iReassocSelfCureConnectFailedCnt.setValue(stat.mReassocScConnectFailedCnt);
                SSIDItem.iResetSelfCureConnectFailedCnt.setValue(stat.mResetScConnectFailedCnt);
                SSIDItem.iBlackListAbnormalCnt.setValue(stat.mBlackListAbnormalCnt);
                SSIDItem.iBlackListScSuccCnt.setValue(stat.mBlackListScSuccCnt);
                SSIDItem.iDhcpFailedStaticScSuccCnt.setValue(stat.mDhcpFailedStaticScSuccCnt);
                SSIDItem.strapVendorInfo.setValue(stat.apVendorInfo);
                SSIDItem.strAP_proto.setValue(stat.strAP_proto);
                SSIDItem.strAP_key_mgmt.setValue(stat.strAP_key_mgmt);
                SSIDItem.strAP_auth_alg.setValue(stat.strAP_auth_alg);
                SSIDItem.strAP_pairwise.setValue(stat.strAP_pairwise);
                SSIDItem.strAP_eap.setValue(stat.strAP_eap);
                SSIDItem.strAP_group.setValue(stat.strAP_gruop);
                SSIDItem.iAccessWebCnt.setValue(stat.mAccessWEBCnt);
                SSIDItem.iAccessWebSuccCnt.setValue(stat.mAccessWEBSuccCnt);
                SSIDItem.iFirstConnInternetFailCnt.setValue(stat.mFirstConnInternetFailCnt);
                SSIDItem.iOnlyTheTxNoRxCnt.setValue(stat.mOnlyTheTxNoRxCnt);
                SSIDItem.iDnsParseFailCnt.setValue(stat.mDnsParseFailCnt);
                SSIDItem.iArpUnreachableCnt.setValue(stat.mArpUnreachableCnt);
                SSIDItem.iArpReassocOkCnt.setValue(stat.mArpReassocOkCnt);
                SSIDItem.tmTimeStartedStamp.setValue(stat.mConStart);
                SSIDItem.tmTimeLastUpdateStamp.setValue(stat.mLastUpdate);
                SSIDItem.iRoamingAccessWebSuccCnt.setValue(stat.mRoamingAccessWebSuccCnt);
                SSIDItem.iReDHCPAccessWebSuccCnt.setValue(stat.mReDHCPAccessWebSuccCnt);
                SSIDItem.iNoUserProcRunCnt.setValue(stat.mNoUserProcCnt);
                SSIDItem.iAccessWebSlowlyCnt.setValue(stat.mUserLongTimeWaitedCnt);
                SSIDItem.ucMultiGWCount.setValue(stat.mMultiGWCount);
                SSIDItem.iAccessWebFailedPortal.setValue(stat.mAccessWebFailedPortal);
                SSIDItem.iAccessWebRoamingFailedPortal.setValue(stat.mAccessWebRoamingFailedPortal);
                SSIDItem.iAccessWebReDHCPFailedPortal.setValue(stat.mAccessWebReDHCPFailedPortal);
                SSIDItem.iABSAssociateTimes.setValue(stat.mABSAssociateTimes);
                SSIDItem.iABSAssociateFailedTimes.setValue(stat.mABSAssociateFailedTimes);
                SSIDItem.lABSMimoTime.setValue(stat.mABSMimoTime);
                SSIDItem.lABSSisoTime.setValue(stat.mABSSisoTime);
                SSIDItem.lABSMimoScreenOnTime.setValue(stat.mABSMimoScreenOnTime);
                SSIDItem.lABSSisoScreenOnTime.setValue(stat.mABSSisoScreenOnTime);
                result.add(SSIDItem);
            }
        }
        return result;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeWifiCHRStat(boolean flushNow, boolean triggerChr) {
        synchronized (this.mWriteStatLock) {
            long now = SystemClock.elapsedRealtime();
            if (flushNow || now - this.mWriteStatTimestamp >= ((long) MIN_WRITE_STAT_SPAN)) {
                this.mWriteStatTimestamp = now;
                if (0 == this.mTimestamp) {
                    this.mTimestamp = System.currentTimeMillis();
                }
            } else {
                return;
            }
        }
        DataOutputStream dataOutputStream = out;
        if (triggerChr) {
            triggerUploadIfNeed();
        }
    }

    private void triggerUploadIfNeed() {
        String softwareVersion = SystemProperties.get("ro.build.display.id", "");
        if (softwareVersion.equals(this.mSoftwareVersion)) {
            long now = System.currentTimeMillis();
            long minPeriod = MIN_PERIOD_TRIGGER_BETA;
            if (LogManager.getInstance().isCommercialUser()) {
                minPeriod = MIN_PERIOD_TRIGGER_CML;
            }
            if (now - this.mTimestamp >= minPeriod && hasDataToTrigger()) {
                HwWifiCHRStateManager hwWifiCHRStateManager = HwWifiCHRStateManagerImpl.getDefault();
                if (hwWifiCHRStateManager != null) {
                    hwWifiCHRStateManager.uploadWifiStat();
                    hwWifiCHRStateManager.uploadDFTEvent(909001001);
                    hwWifiCHRStateManager.uploadDFTEvent(909001002);
                }
                this.mTimestamp = now;
                clearStatInfo();
                return;
            }
            return;
        }
        clearStatInfo();
        this.mSoftwareVersion = softwareVersion;
    }

    private boolean hasDataToTrigger() {
        if (((long) ((((this.mOpenCnt + this.mOpenSuccCnt) + this.mCloseCnt) + this.mCloseSuccCnt) + this.mDnsReqCnt)) > 0) {
            return true;
        }
        for (int i = 0; i < this.mSSIDStatList.size(); i++) {
            if (((SSIDStat) this.mSSIDStatList.get(i)).hasDataToTrigger()) {
                return true;
            }
        }
        return false;
    }

    public void readWifiCHRStat() {
        Exception e;
        LOGD("readWifiCHRStat");
        DataInputStream dataInputStream = null;
        try {
            this.mSSIDStatList.clear();
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(mWifiStatConf)));
            SSIDStat stat = null;
            while (true) {
                SSIDStat stat2;
                try {
                    String key = in.readUTF();
                    readWifiCHRTrafficBytesStat(key);
                    if (key.startsWith(KEY_TIMESTAMP)) {
                        this.mTimestamp = Long.parseLong(key.replace(KEY_TIMESTAMP, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_OPEN_CNT)) {
                        this.mOpenCnt = Integer.parseInt(key.replace(KEY_OPEN_CNT, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_OPEN_SUCC_CNT)) {
                        this.mOpenSuccCnt = Integer.parseInt(key.replace(KEY_OPEN_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_CLOSE_CNT)) {
                        this.mCloseCnt = Integer.parseInt(key.replace(KEY_CLOSE_CNT, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_CLOSE_SUCC_CNT)) {
                        this.mCloseSuccCnt = Integer.parseInt(key.replace(KEY_CLOSE_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_OPEN_DURATION)) {
                        this.mOpenDuration = Integer.parseInt(key.replace(KEY_OPEN_DURATION, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_CLOSE_DURATION)) {
                        this.mCloseDuration = Integer.parseInt(key.replace(KEY_CLOSE_DURATION, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_DNS_REQ_CNT)) {
                        this.mDnsReqCnt = Integer.parseInt(key.replace(KEY_DNS_REQ_CNT, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_DNS_REQ_FAIL)) {
                        this.mDnsReqFail = Integer.parseInt(key.replace(KEY_DNS_REQ_FAIL, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_DNS_MAX_TIME)) {
                        this.mDnsMaxTime = Integer.parseInt(key.replace(KEY_DNS_MAX_TIME, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_DNS_MIN_TIME)) {
                        this.mDnsMinTime = Integer.parseInt(key.replace(KEY_DNS_MIN_TIME, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_DNS_TOT_TIME)) {
                        this.mDnsTotTime = Integer.parseInt(key.replace(KEY_DNS_TOT_TIME, "").replace(SEPARATOR_KEY, ""));
                        stat2 = stat;
                    } else if (key.startsWith(KEY_SSID)) {
                        String tmp = key.replace(KEY_SSID, "").replace(SEPARATOR_KEY, "");
                        stat2 = new SSIDStat();
                        try {
                            stat2.SSID = tmp;
                            this.mSSIDStatList.add(stat2);
                        } catch (EOFException e2) {
                            dataInputStream = in;
                        } catch (Exception e3) {
                            e = e3;
                            dataInputStream = in;
                        }
                    } else if (stat != null) {
                        readWifiCHRScStat(stat, key);
                        if (key.startsWith(KEY_ASSOC_CNT)) {
                            stat.mAssocCnt = Integer.parseInt(key.replace(KEY_ASSOC_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_ASSOC_SUCC_CNT)) {
                            stat.mAssocSuccCnt = Integer.parseInt(key.replace(KEY_ASSOC_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_AUTH_CNT)) {
                            stat.mAuthCnt = Integer.parseInt(key.replace(KEY_AUTH_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_AUTH_SUCC_CNT)) {
                            stat.mAuthSuccCnt = Integer.parseInt(key.replace(KEY_AUTH_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_DHCP_CNT)) {
                            stat.mDhcpCnt = Integer.parseInt(key.replace(KEY_DHCP_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_DHCP_SUCC_CNT)) {
                            stat.mDhcpSuccCnt = Integer.parseInt(key.replace(KEY_DHCP_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_DHCP_STATIC_CNT)) {
                            stat.mDhcpStaticCnt = Integer.parseInt(key.replace(KEY_DHCP_STATIC_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_DHCP_STATIC_SUCC_CNT)) {
                            stat.mDhcpStaticSuccCnt = Integer.parseInt(key.replace(KEY_DHCP_STATIC_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_CONNECTED_CNT)) {
                            stat.mConnectedCnt = Integer.parseInt(key.replace(KEY_CONNECTED_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_CONNECT_TOTAL_CNT)) {
                            stat.mConnectTotalCnt = Integer.parseInt(key.replace(KEY_CONNECT_TOTAL_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_ABDISCONNECT_CNT)) {
                            stat.mAbDisconnectCnt = Integer.parseInt(key.replace(KEY_ABDISCONNECT_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_ASSOC_DURATION)) {
                            stat.mAssocDuration = Integer.parseInt(key.replace(KEY_ASSOC_DURATION, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_AUTH_DURATION)) {
                            stat.mAuthDuration = Integer.parseInt(key.replace(KEY_AUTH_DURATION, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_DHCP_DURATION)) {
                            stat.mDhcpDuration = Integer.parseInt(key.replace(KEY_DHCP_DURATION, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_CONNECTED_DURATION)) {
                            stat.mConnectedDuration = Integer.parseInt(key.replace(KEY_CONNECTED_DURATION, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_CHR_CONNECTING_DURATION)) {
                            stat.mCHRConnectingDuration = Integer.parseInt(key.replace(KEY_CHR_CONNECTING_DURATION, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_AP_VENDORINFO)) {
                            stat.apVendorInfo = key.replace(KEY_AP_VENDORINFO, "").replace(SEPARATOR_KEY, "");
                        } else if (key.startsWith(KEY_AP_PROTO)) {
                            stat.strAP_proto = key.replace(KEY_AP_PROTO, "").replace(SEPARATOR_KEY, "");
                        } else if (key.startsWith(KEY_AP_KEY_MGMT)) {
                            stat.strAP_key_mgmt = key.replace(KEY_AP_KEY_MGMT, "").replace(SEPARATOR_KEY, "");
                        } else if (key.startsWith(KEY_AP_AUTH_ALG)) {
                            stat.strAP_auth_alg = key.replace(KEY_AP_AUTH_ALG, "").replace(SEPARATOR_KEY, "");
                        } else if (key.startsWith(KEY_AP_PAIRWISE)) {
                            stat.strAP_pairwise = key.replace(KEY_AP_PAIRWISE, "").replace(SEPARATOR_KEY, "");
                        } else if (key.startsWith(KEY_AP_EAP)) {
                            stat.strAP_eap = key.replace(KEY_AP_EAP, "").replace(SEPARATOR_KEY, "");
                        } else if (key.startsWith(KEY_AP_GROUP)) {
                            stat.strAP_gruop = key.replace(KEY_AP_GROUP, "").replace(SEPARATOR_KEY, "");
                        } else if (key.startsWith(KEY_ROAMING_CNT)) {
                            stat.mRoamingCnt = Integer.parseInt(key.replace(KEY_ROAMING_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_ROAMING_SUCC_CNT)) {
                            stat.mRoamingSuccCnt = Integer.parseInt(key.replace(KEY_ROAMING_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_ROAMING_DURATION)) {
                            stat.mRoamingDuration = Integer.parseInt(key.replace(KEY_ROAMING_DURATION, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_REDHCP_CNT)) {
                            stat.mReDHCPCnt = Integer.parseInt(key.replace(KEY_REDHCP_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_REDHCP_SUCC_CNT)) {
                            stat.mReDHCPSuccCnt = Integer.parseInt(key.replace(KEY_REDHCP_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_REDHCP_DURATION)) {
                            stat.mReDHCPDuration = Integer.parseInt(key.replace(KEY_REDHCP_DURATION, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_REKEY_CNT)) {
                            stat.mReKEYCnt = Integer.parseInt(key.replace(KEY_REKEY_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_REKEY_SUCC_CNT)) {
                            stat.mReKEYSuccCnt = Integer.parseInt(key.replace(KEY_REKEY_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_REKEY_DURATION)) {
                            stat.mReKEYDuration = Integer.parseInt(key.replace(KEY_REKEY_DURATION, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_GOOD_RECONNECT_CNT)) {
                            stat.mGoodReConnectCnt = Integer.parseInt(key.replace(KEY_GOOD_RECONNECT_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_GOOD_RECONNECTSUCC_CNT)) {
                            stat.mGoodReConnectSuccCnt = Integer.parseInt(key.replace(KEY_GOOD_RECONNECTSUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_WEAK_RECONNECT_CNT)) {
                            stat.mWeakReConnectCnt = Integer.parseInt(key.replace(KEY_WEAK_RECONNECT_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_WEAK_RECONNECTSUCC_CNT)) {
                            stat.mWeakReConnectSuccCnt = Integer.parseInt(key.replace(KEY_WEAK_RECONNECTSUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_ONSCREEN_CONNECT_CNT)) {
                            stat.mOnScreenConnectCnt = Integer.parseInt(key.replace(KEY_ONSCREEN_CONNECT_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_ONSCREEN_CONNECTED_CNT)) {
                            stat.mOnScreenConnectedCnt = Integer.parseInt(key.replace(KEY_ONSCREEN_CONNECTED_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_ONSCREEN_ABDICONNECT_CNT)) {
                            stat.mOnScreenAbDisconnectCnt = Integer.parseInt(key.replace(KEY_ONSCREEN_ABDICONNECT_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_ONSCREEN_DISCONNECT_CNT)) {
                            stat.mOnScreenDisconnectCnt = Integer.parseInt(key.replace(KEY_ONSCREEN_DISCONNECT_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_ONSCREEN_CONNECT_DURATION)) {
                            stat.mOnScreenConnectDuration = Integer.parseInt(key.replace(KEY_ONSCREEN_CONNECT_DURATION, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_ONSCREEN_RECONNECT_CNT)) {
                            stat.mOnScreenReConnectedCnt = Integer.parseInt(key.replace(KEY_ONSCREEN_RECONNECT_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_ONSCREEN_RECONNECT_DURATION)) {
                            stat.mOnScreenReConnectDuration = Integer.parseInt(key.replace(KEY_ONSCREEN_RECONNECT_DURATION, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_ACCESS_WEB_CNT)) {
                            stat.mAccessWEBCnt = Integer.parseInt(key.replace(KEY_ACCESS_WEB_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_ACCESS_WEB_SUCC_CNT)) {
                            stat.mAccessWEBSuccCnt = Integer.parseInt(key.replace(KEY_ACCESS_WEB_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_START_TIMESTAMP)) {
                            stat.mConStart = new Date(Long.parseLong(key.replace(KEY_START_TIMESTAMP, "").replace(SEPARATOR_KEY, "")));
                        } else if (key.startsWith(KEY_LAST_TIMESTAMP)) {
                            stat.mLastUpdate = new Date(Long.parseLong(key.replace(KEY_LAST_TIMESTAMP, "").replace(SEPARATOR_KEY, "")));
                        } else if (key.startsWith(KEY_ROAMING_ACCESS_WEB_CNT)) {
                            stat.mRoamingAccessWebSuccCnt = Integer.parseInt(key.replace(KEY_ROAMING_ACCESS_WEB_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_REDHCP_ACCESS_WEB_SUCC_CNT)) {
                            stat.mReDHCPAccessWebSuccCnt = Integer.parseInt(key.replace(KEY_REDHCP_ACCESS_WEB_SUCC_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_NO_USERPROC_CNT)) {
                            stat.mNoUserProcCnt = Integer.parseInt(key.replace(KEY_NO_USERPROC_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_USER_IN_LONGWAITED_CNT)) {
                            stat.mUserLongTimeWaitedCnt = Integer.parseInt(key.replace(KEY_USER_IN_LONGWAITED_CNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_MULTIGWCOUNT)) {
                            stat.mMultiGWCount = Integer.parseInt(key.replace(KEY_MULTIGWCOUNT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_ACCESS_WEB_FAILED_BY_PORTAL_CONNECT)) {
                            stat.mAccessWebFailedPortal = Integer.parseInt(key.replace(KEY_ACCESS_WEB_FAILED_BY_PORTAL_CONNECT, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_ACCESS_WEB_FAILED_BY_PORTAL_ROAMING)) {
                            stat.mAccessWebRoamingFailedPortal = Integer.parseInt(key.replace(KEY_ACCESS_WEB_FAILED_BY_PORTAL_ROAMING, "").replace(SEPARATOR_KEY, ""));
                        } else if (key.startsWith(KEY_ACCESS_WEB_FAILED_BY_PORTAL_REDHCP)) {
                            stat.mAccessWebReDHCPFailedPortal = Integer.parseInt(key.replace(KEY_ACCESS_WEB_FAILED_BY_PORTAL_REDHCP, "").replace(SEPARATOR_KEY, ""));
                        }
                        readABSCHRStat(stat, key);
                        stat2 = stat;
                    } else {
                        stat2 = stat;
                    }
                    stat = stat2;
                } catch (EOFException e4) {
                    stat2 = stat;
                    dataInputStream = in;
                } catch (Exception e5) {
                    e = e5;
                    dataInputStream = in;
                }
            }
        } catch (EOFException e6) {
            if (dataInputStream != null) {
                try {
                    dataInputStream.close();
                } catch (Exception e7) {
                    LOGW("readWifiCHRStat: Error reading file" + e7);
                }
            }
            if (dataInputStream != null) {
                try {
                    dataInputStream.close();
                } catch (Exception e72) {
                    LOGW("readWifiCHRStat: Error closing file" + e72);
                }
            }
        } catch (Exception e8) {
            e72 = e8;
            LOGW("readWifiCHRStat: No config file, revert to default" + e72);
            if (dataInputStream != null) {
                dataInputStream.close();
            }
        }
    }

    private void readWifiCHRTrafficBytesStat(String key) {
        if (key.startsWith(KEY_WLAN_TRAFFIC_BYTES)) {
            this.mWlanTotalTrafficBytes = Long.parseLong(key.replace(KEY_WLAN_TRAFFIC_BYTES, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_MOBILE_TRAFFIC_BYTES)) {
            this.mMobileTotalTrafficBytes = Long.parseLong(key.replace(KEY_MOBILE_TRAFFIC_BYTES, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_WLAN_CONNECTED_DURATION)) {
            this.mWlanTotalConnectedDuration = Long.parseLong(key.replace(KEY_WLAN_CONNECTED_DURATION, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_MOBILE_CONNECTED_DURATION)) {
            this.mMobileTotalConnectedDuration = Long.parseLong(key.replace(KEY_MOBILE_CONNECTED_DURATION, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_SOFTWARE_VERSION)) {
            this.mSoftwareVersion = key.replace(KEY_SOFTWARE_VERSION, "").replace(SEPARATOR_KEY, "");
        }
    }

    private void readWifiCHRScStat(SSIDStat stat, String key) {
        if (key.startsWith(KEY_BSSID)) {
            stat.BSSID = key.replace(KEY_BSSID, "").replace(SEPARATOR_KEY, "");
        } else if (key.startsWith(KEY_ASSOC_BY_ABS_CNT)) {
            stat.mAssocByABSCnt = Integer.parseInt(key.replace(KEY_ASSOC_BY_ABS_CNT, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_ASSOC_REJECT_CNT)) {
            stat.mAssocRejectAccessFullCnt = Integer.parseInt(key.replace(KEY_ASSOC_REJECT_CNT, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_DNS_ABNORMAL)) {
            stat.mDnsAbnormalCnt = Integer.parseInt(key.replace(KEY_DNS_ABNORMAL, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_TCP_RX_ABNORMAL)) {
            stat.mTcpRxAbnormalCnt = Integer.parseInt(key.replace(KEY_TCP_RX_ABNORMAL, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_ROAMING_ABNORMAL)) {
            stat.mRoamingAbnormalCnt = Integer.parseInt(key.replace(KEY_ROAMING_ABNORMAL, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_GATEWAY_ABNORMAL)) {
            stat.mGatewayAbnormalCnt = Integer.parseInt(key.replace(KEY_GATEWAY_ABNORMAL, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_DNS_SC_SUCC)) {
            stat.mDnsScSuccCnt = Integer.parseInt(key.replace(KEY_DNS_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_RE_DHCP_SC_SUCC)) {
            stat.mReDhcpScSuccCnt = Integer.parseInt(key.replace(KEY_RE_DHCP_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_STATIC_IP_SC_SUCC)) {
            stat.mStaticIpScSuccCnt = Integer.parseInt(key.replace(KEY_STATIC_IP_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_REASSOC_SC_SUCC)) {
            stat.mReassocScCnt = Integer.parseInt(key.replace(KEY_REASSOC_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_RESET_SC_SUCC)) {
            stat.mResetScSuccCnt = Integer.parseInt(key.replace(KEY_RESET_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_USER_ENABLE_STATIC_IP)) {
            stat.mUserEnableStaticIpCnt = Integer.parseInt(key.replace(KEY_USER_ENABLE_STATIC_IP, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_AUTH_FAILED_ABNORMAL)) {
            stat.mAuthFailedAbnormalCnt = Integer.parseInt(key.replace(KEY_AUTH_FAILED_ABNORMAL, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_ASSOC_REJECTED_ABNORMAL)) {
            stat.mAssocRejectedAbnormalCnt = Integer.parseInt(key.replace(KEY_ASSOC_REJECTED_ABNORMAL, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_DHCP_FAILED_ABNORMAL)) {
            stat.mDhcpFailedAbnormalCnt = Integer.parseInt(key.replace(KEY_DHCP_FAILED_ABNORMAL, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_APP_DISABLED_ABNORMAL)) {
            stat.mAppDisabledAbnromalCnt = Integer.parseInt(key.replace(KEY_APP_DISABLED_ABNORMAL, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_AUTH_FAILED_SC_SUCC)) {
            stat.mAuthFailedScSuccCnt = Integer.parseInt(key.replace(KEY_AUTH_FAILED_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_ASSOC_REJECTED_SC_SUCC)) {
            stat.mAssocRejectedScSuccCnt = Integer.parseInt(key.replace(KEY_ASSOC_REJECTED_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_DHCP_FAILED_SC_SUCC)) {
            stat.mDhcpFailedScSuccCnt = Integer.parseInt(key.replace(KEY_DHCP_FAILED_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_APP_DISABLED_SC_SUCC)) {
            stat.mAppDisabledScSuccCnt = Integer.parseInt(key.replace(KEY_APP_DISABLED_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_REASSOC_SC_CONNECT_FAILED)) {
            stat.mReassocScConnectFailedCnt = Integer.parseInt(key.replace(KEY_REASSOC_SC_CONNECT_FAILED, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_RESET_SC_CONNECT_FAILED)) {
            stat.mResetScConnectFailedCnt = Integer.parseInt(key.replace(KEY_RESET_SC_CONNECT_FAILED, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_DISCONNECT_CNT)) {
            stat.mDisconnectCnt = Integer.parseInt(key.replace(KEY_DISCONNECT_CNT, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_DNS_RESET_SC_SUCC)) {
            stat.mDnsResetScSuccCnt = Integer.parseInt(key.replace(KEY_DNS_RESET_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_ROAMING_RESET_SC_SUCC)) {
            stat.mRoamingResetScSuccCnt = Integer.parseInt(key.replace(KEY_ROAMING_RESET_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_GW_RESET_SC_SUCC)) {
            stat.mGwResetScSuccCnt = Integer.parseInt(key.replace(KEY_GW_RESET_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_DHCP_AUTO_IP_CNT)) {
            stat.mDhcpAutoIpCnt = Integer.parseInt(key.replace(KEY_DHCP_AUTO_IP_CNT, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_BLACK_LIST_ABNORMAL)) {
            stat.mBlackListAbnormalCnt = Integer.parseInt(key.replace(KEY_BLACK_LIST_ABNORMAL, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_BLACK_LIST_SC_SUCC)) {
            stat.mBlackListScSuccCnt = Integer.parseInt(key.replace(KEY_BLACK_LIST_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_DHCP_FAILED_STATIC_SC_SUCC)) {
            stat.mDhcpFailedStaticScSuccCnt = Integer.parseInt(key.replace(KEY_DHCP_FAILED_STATIC_SC_SUCC, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_FIRST_CONN_INTERNET_FAIL_DURATION)) {
            stat.mFirstConnInternetFailDuration = Integer.parseInt(key.replace(KEY_FIRST_CONN_INTERNET_FAIL_DURATION, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_FIRST_CONN_INTERNERT_FAIL_CNT)) {
            stat.mFirstConnInternetFailCnt = Integer.parseInt(key.replace(KEY_FIRST_CONN_INTERNERT_FAIL_CNT, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_ONLY_THE_TX_NO_RX_CNT)) {
            stat.mOnlyTheTxNoRxCnt = Integer.parseInt(key.replace(KEY_ONLY_THE_TX_NO_RX_CNT, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_DNS_PARSE_FAIL_CNT)) {
            stat.mDnsParseFailCnt = Integer.parseInt(key.replace(KEY_DNS_PARSE_FAIL_CNT, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_ARP_UNREACHABLE_CNT)) {
            stat.mArpUnreachableCnt = Integer.parseInt(key.replace(KEY_ARP_UNREACHABLE_CNT, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_ARP_REASSOC_OK_CNT)) {
            stat.mArpReassocOkCnt = Integer.parseInt(key.replace(KEY_ARP_REASSOC_OK_CNT, "").replace(SEPARATOR_KEY, ""));
        }
    }

    private void clearStatInfo() {
        this.mOpenCnt = 0;
        this.mOpenSuccCnt = 0;
        this.mCloseCnt = 0;
        this.mCloseSuccCnt = 0;
        this.mOpenDuration = 0;
        this.mCloseDuration = 0;
        this.mDnsReqCnt = 0;
        this.mDnsReqFail = 0;
        this.mDnsMaxTime = 0;
        this.mDnsMinTime = 0;
        this.mDnsTotTime = 0;
        this.mWlanTotalTrafficBytes = 0;
        this.mMobileTotalTrafficBytes = 0;
        this.mWlanTotalConnectedDuration = 0;
        this.mMobileTotalConnectedDuration = 0;
        this.mSSIDStatList.clear();
        new HwWifiDFTUtilImpl().clearSwCnt();
        if (this.mCurrentStat != null) {
            String currSSID = this.mCurrentStat.SSID;
            this.mCurrentStat = null;
            setAPSSID(currSSID);
        }
        writeWifiCHRStat(true, false);
    }

    private void LOGD(String msg) {
        Log.d(TAG, msg);
    }

    private void LOGW(String msg) {
        Log.e(TAG, msg);
    }

    public void getWifiStabilityStat(HwWifiDFTStabilityStat hwWifiDFTStabilityStat) {
        try {
            HwWifiDFTUtilImpl hwWifiDFTUtilImpl = new HwWifiDFTUtilImpl();
            hwWifiDFTStabilityStat.mOpenCount = this.mOpenCnt;
            hwWifiDFTStabilityStat.mOPenSuccCount = this.mOpenSuccCnt;
            hwWifiDFTStabilityStat.mOPenDuration = this.mOpenDuration;
            hwWifiDFTStabilityStat.mCloseCount = this.mCloseCnt;
            hwWifiDFTStabilityStat.mCloseSuccCount = this.mCloseSuccCnt;
            hwWifiDFTStabilityStat.mCloseDuration = this.mCloseDuration;
            hwWifiDFTStabilityStat.mIsWifiProON = hwWifiDFTUtilImpl.getWifiProState();
            hwWifiDFTStabilityStat.mWifiProSwcnt = hwWifiDFTUtilImpl.getWifiProSwcnt();
            hwWifiDFTStabilityStat.mIsScanAlwaysAvalible = hwWifiDFTUtilImpl.getWifiAlwaysScanState();
            hwWifiDFTStabilityStat.mScanAlwaysSwCnt = hwWifiDFTUtilImpl.getScanAlwaysSwCnt();
            hwWifiDFTStabilityStat.mIsWifiNotificationOn = hwWifiDFTUtilImpl.getWifiNetworkNotificationState();
            hwWifiDFTStabilityStat.mWifiNotifationSwCnt = hwWifiDFTUtilImpl.getWifiNotifationSwCnt();
            hwWifiDFTStabilityStat.mWifiSleepPolicy = hwWifiDFTUtilImpl.getWifiSleepPolicyState();
            hwWifiDFTStabilityStat.mWifiSleepSwCnt = hwWifiDFTUtilImpl.getWifiSleepSwCnt();
            hwWifiDFTStabilityStat.mWifiToPdp = hwWifiDFTUtilImpl.getWifiToPdpState();
            hwWifiDFTStabilityStat.mWifiToPdpSwCnt = hwWifiDFTUtilImpl.getWifiToPdpSwCnt();
        } catch (Exception e) {
            Log.e(TAG, "setWifiStabilityStat error.");
            e.printStackTrace();
        }
    }

    public void getWifiStabilitySsidStat(List<HwWifiDFTStabilitySsidStat> listHwWifiDFTStabilitySsidStat) {
        int i = 0;
        while (i < this.mSSIDStatList.size()) {
            try {
                int i2;
                SSIDStat stat = (SSIDStat) this.mSSIDStatList.get(i);
                HwWifiDFTUtilImpl hwWifiDFTUtilImpl = new HwWifiDFTUtilImpl();
                HwWifiDFTStabilitySsidStat hwWifiDFTStabilitySsidStat = new HwWifiDFTStabilitySsidStat();
                hwWifiDFTStabilitySsidStat.mApSsid = stat.SSID;
                hwWifiDFTStabilitySsidStat.mPublicEssCount = (byte) 0;
                hwWifiDFTStabilitySsidStat.mAssocCount = (short) ((stat.mAssocCnt - stat.mAssocRejectAccessFullCnt) - stat.mAssocByABSCnt);
                hwWifiDFTStabilitySsidStat.mAssocSuccCount = (short) stat.mAssocSuccCnt;
                hwWifiDFTStabilitySsidStat.mAuthCount = (short) stat.mAuthCnt;
                hwWifiDFTStabilitySsidStat.mAuthSuccCount = (short) stat.mAuthSuccCnt;
                hwWifiDFTStabilitySsidStat.mIpDhcpCount = (short) stat.mDhcpCnt;
                hwWifiDFTStabilitySsidStat.mDhcpSuccCount = (short) stat.mDhcpSuccCnt;
                hwWifiDFTStabilitySsidStat.mIpStaticCount = (short) stat.mDhcpStaticCnt;
                hwWifiDFTStabilitySsidStat.mIpAutoCount = (short) stat.mDhcpAutoIpCnt;
                hwWifiDFTStabilitySsidStat.mConnectedCount = (short) stat.mConnectedCnt;
                hwWifiDFTStabilitySsidStat.mAbnormalDisconnCount = (short) stat.mAbDisconnectCnt;
                hwWifiDFTStabilitySsidStat.mAssocDuration = stat.mAssocDuration;
                hwWifiDFTStabilitySsidStat.mAuthDuration = stat.mAuthDuration;
                hwWifiDFTStabilitySsidStat.mDhcpDuration = stat.mDhcpDuration;
                hwWifiDFTStabilitySsidStat.mConnectingDuration = stat.mConnectingDuration;
                hwWifiDFTStabilitySsidStat.mConnectionDuration = stat.mConnectedDuration;
                hwWifiDFTStabilitySsidStat.mDnsReqCnt = this.mDnsReqCnt;
                hwWifiDFTStabilitySsidStat.mDnsReqFail = this.mDnsReqFail;
                if (this.mDnsReqCnt - this.mDnsReqFail == 0) {
                    i2 = 0;
                } else {
                    i2 = this.mDnsTotTime / (this.mDnsReqCnt - this.mDnsReqFail);
                }
                hwWifiDFTStabilitySsidStat.mDnsAvgTime = i2;
                hwWifiDFTStabilitySsidStat.mDhcpRenewCount = stat.mReDHCPCnt;
                hwWifiDFTStabilitySsidStat.mDhcpRenewSuccCount = stat.mReDHCPSuccCnt;
                hwWifiDFTStabilitySsidStat.mDhcpRenewDuration = stat.mReDHCPDuration;
                hwWifiDFTStabilitySsidStat.mRoamingCount = stat.mRoamingCnt;
                hwWifiDFTStabilitySsidStat.mRoamingSuccCount = stat.mRoamingSuccCnt;
                hwWifiDFTStabilitySsidStat.mRoamingDuration = stat.mRoamingDuration;
                hwWifiDFTStabilitySsidStat.mRekeyCount = stat.mReKEYCnt;
                hwWifiDFTStabilitySsidStat.mRekeySuccCount = stat.mReKEYSuccCnt;
                hwWifiDFTStabilitySsidStat.mRekeyDuration = stat.mReKEYDuration;
                hwWifiDFTStabilitySsidStat.mAccessWebfailCnt = hwWifiDFTUtilImpl.getAccessNetFailedCount();
                hwWifiDFTStabilitySsidStat.mAccessWebSlowlyCnt = (short) stat.mUserLongTimeWaitedCnt;
                hwWifiDFTStabilitySsidStat.mGwIpCount = (byte) stat.mMultiGWCount;
                hwWifiDFTStabilitySsidStat.mGwMacCount = (byte) 0;
                hwWifiDFTStabilitySsidStat.mRssiAvg = 0;
                listHwWifiDFTStabilitySsidStat.add(hwWifiDFTStabilitySsidStat);
                i++;
            } catch (Exception e) {
                Log.e(TAG, "setWifiStabilitySsidStat error.");
                e.printStackTrace();
                return;
            }
        }
    }

    private void readABSCHRStat(SSIDStat stat, String key) {
        if (key.startsWith(KEY_ABS_MIMO_TIME)) {
            stat.mABSMimoTime = Long.parseLong(key.replace(KEY_ABS_MIMO_TIME, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_ABS_SISO_TIME)) {
            stat.mABSSisoTime = Long.parseLong(key.replace(KEY_ABS_SISO_TIME, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_ABS_MIMO_SCREEN_ON_TIME)) {
            stat.mABSMimoScreenOnTime = Long.parseLong(key.replace(KEY_ABS_MIMO_SCREEN_ON_TIME, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_ABS_SISO_SCREEN_ON_TIME)) {
            stat.mABSSisoScreenOnTime = Long.parseLong(key.replace(KEY_ABS_SISO_SCREEN_ON_TIME, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_ABS_ASSOCIATE_TIMES)) {
            stat.mABSAssociateTimes = Integer.parseInt(key.replace(KEY_ABS_ASSOCIATE_TIMES, "").replace(SEPARATOR_KEY, ""));
        } else if (key.startsWith(KEY_ABS_ASSOCIATE_FAILED_TIMES)) {
            stat.mABSAssociateFailedTimes = Integer.parseInt(key.replace(KEY_ABS_ASSOCIATE_FAILED_TIMES, "").replace(SEPARATOR_KEY, ""));
        }
    }

    public void updateABSTime(String ssid, int associateTimes, int associateFailedTimes, long mimoTime, long sisoTime, long mimoScreenOnTime, long sisoScreenOnTime) {
        SSIDStat sta = geStatBySSID(ssid);
        if (sta == null) {
            Log.e(TAG, "sta == null error");
            sta = new SSIDStat();
            sta.SSID = ssid;
            this.mSSIDStatList.add(sta);
        }
        sta.mABSAssociateTimes = sta.mABSAssociateTimes + associateTimes;
        sta.mABSAssociateFailedTimes = sta.mABSAssociateFailedTimes + associateFailedTimes;
        sta.mABSMimoTime = sta.mABSMimoTime + mimoTime;
        sta.mABSSisoTime = sta.mABSSisoTime + sisoTime;
        sta.mABSMimoScreenOnTime = sta.mABSMimoScreenOnTime + mimoScreenOnTime;
        sta.mABSSisoScreenOnTime = sta.mABSSisoScreenOnTime + sisoScreenOnTime;
        writeWifiCHRStat(true, false);
    }
}
