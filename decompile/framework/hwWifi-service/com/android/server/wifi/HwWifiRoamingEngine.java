package com.android.server.wifi;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.view.WindowManagerPolicy;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.LocalServices;
import com.android.server.policy.AbsPhoneWindowManager;
import com.android.server.wifipro.WifiProCommonUtils;
import java.text.DateFormat;
import java.util.Date;

public class HwWifiRoamingEngine extends StateMachine {
    public static final String ACTION_11v_ROAMING_NETWORK_FOUND = "com.huawei.wifi.action.11v_ROAMING_NETWORK_FOUND";
    private static final int CMD_11v_ROAMING_TIMEOUT = 108;
    private static final int CMD_DISCONNECT_POOR_LINK = 105;
    private static final int CMD_NETWORK_CONNECTED_RCVD = 101;
    private static final int CMD_NETWORK_DISCONNECTED_RCVD = 102;
    private static final int CMD_NEW_RSSI_RCVD = 104;
    private static final int CMD_QUERY_11v_ROAMING_NETWORK = 103;
    private static final int CMD_REQUEST_ROAMING_NETWORK = 109;
    private static final int CMD_ROAMING_COMPLETED_RCVD = 107;
    private static final int CMD_ROAMING_STARTED_RCVD = 106;
    private static final int[] DELAYED_MS_TABLE = new int[]{2000, 4000, 10000, 30000, 0};
    private static final int POOR_LINK_MONITOR_MS = 6000;
    private static final String PROP_DISABLE_AUTO_DISC = "hw.wifi.disable_auto_disc";
    private static final int QUERY_11v_ROAMING_NETWORK_DELAYED_MS = 5000;
    private static final int QUERY_REASON_LOW_RSSI = 16;
    private static final int QUERY_REASON_PREFERRED_BSS = 19;
    private static final int ROAMING_11v_NETWORK_TIMEOUT_MS = 8000;
    private static final int SIGNAL_LEVEL_0 = 0;
    private static final int SIGNAL_LEVEL_2 = 2;
    private static final int SIGNAL_LEVEL_4 = 4;
    private static final String TAG = "HwWifiRoamingEngine";
    private static HwWifiRoamingEngine mHwWifiRoamingEngine = null;
    private State mConnectedMonitorState = new ConnectedMonitorState();
    private Context mContext;
    private State mDefaultState = new DefaultState();
    private State mDisconnectedMonitorState = new DisconnectedMonitorState();
    private boolean mInitialized = false;
    private WifiManager mWifiManager;
    private WifiNative mWifiNative;
    private WifiStateMachine mWifiStateMachine;

    class ConnectedMonitorState extends State {
        private int m11vRoamingFailedCounter;
        private boolean m11vRoamingOnGoing;
        private long mLast11vRoamingFailedTs;
        private int mLastSignalLevel;
        private boolean mRoamingOnGoing;

        ConnectedMonitorState() {
        }

        public void enter() {
            this.mRoamingOnGoing = false;
            this.m11vRoamingOnGoing = false;
            this.m11vRoamingFailedCounter = 0;
            this.mLast11vRoamingFailedTs = 0;
            WifiInfo wifiInfo = HwWifiRoamingEngine.this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                this.mLastSignalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(wifiInfo.getRssi());
                HwWifiRoamingEngine.this.LOGD("ConnectedMonitorState, network = " + wifiInfo.getSSID() + ", 802.11v = " + is11vNetworkConnected() + ", 2.4GHz = " + wifiInfo.is24GHz() + ", current level = " + this.mLastSignalLevel);
                if (!is11vNetworkConnected()) {
                    return;
                }
                if (wifiInfo.is24GHz() || this.mLastSignalLevel <= 2) {
                    HwWifiRoamingEngine.this.sendMessageDelayed(103, 5000);
                }
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 102:
                    if (HwWifiRoamingEngine.this.hasMessages(103)) {
                        HwWifiRoamingEngine.this.removeMessages(103);
                    }
                    if (HwWifiRoamingEngine.this.hasMessages(HwWifiRoamingEngine.CMD_11v_ROAMING_TIMEOUT)) {
                        HwWifiRoamingEngine.this.removeMessages(HwWifiRoamingEngine.CMD_11v_ROAMING_TIMEOUT);
                    }
                    if (HwWifiRoamingEngine.this.hasMessages(HwWifiRoamingEngine.CMD_DISCONNECT_POOR_LINK)) {
                        HwWifiRoamingEngine.this.removeMessages(HwWifiRoamingEngine.CMD_DISCONNECT_POOR_LINK);
                    }
                    HwWifiRoamingEngine.this.transitionTo(HwWifiRoamingEngine.this.mDisconnectedMonitorState);
                    break;
                case 103:
                    query11vRoamingNetowrk(16);
                    break;
                case 104:
                    handleNewRssiRcvd(message.arg1);
                    break;
                case HwWifiRoamingEngine.CMD_DISCONNECT_POOR_LINK /*105*/:
                    disconnectPoorWifiConnection(false);
                    break;
                case HwWifiRoamingEngine.CMD_ROAMING_STARTED_RCVD /*106*/:
                    if (HwWifiRoamingEngine.this.hasMessages(HwWifiRoamingEngine.CMD_DISCONNECT_POOR_LINK)) {
                        HwWifiRoamingEngine.this.LOGD("CMD_DISCONNECT_POOR_LINK remove due to roaming received.");
                        HwWifiRoamingEngine.this.removeMessages(HwWifiRoamingEngine.CMD_DISCONNECT_POOR_LINK);
                    }
                    this.mRoamingOnGoing = true;
                    break;
                case HwWifiRoamingEngine.CMD_ROAMING_COMPLETED_RCVD /*107*/:
                    if (HwWifiRoamingEngine.this.hasMessages(HwWifiRoamingEngine.CMD_11v_ROAMING_TIMEOUT)) {
                        HwWifiRoamingEngine.this.LOGD("CMD_11v_ROAMING_TIMEOUT remove due to roaming completed received.");
                        HwWifiRoamingEngine.this.removeMessages(HwWifiRoamingEngine.CMD_11v_ROAMING_TIMEOUT);
                    }
                    this.mRoamingOnGoing = false;
                    this.m11vRoamingOnGoing = false;
                    this.m11vRoamingFailedCounter = 0;
                    this.mLast11vRoamingFailedTs = 0;
                    break;
                case HwWifiRoamingEngine.CMD_11v_ROAMING_TIMEOUT /*108*/:
                    if (HwWifiRoamingEngine.this.hasMessages(103)) {
                        HwWifiRoamingEngine.this.removeMessages(103);
                    }
                    this.m11vRoamingOnGoing = false;
                    this.m11vRoamingFailedCounter++;
                    this.mLast11vRoamingFailedTs = System.currentTimeMillis();
                    HwWifiRoamingEngine.this.LOGD("CMD_11v_ROAMING_TIMEOUT received, counter = " + this.m11vRoamingFailedCounter + ", ts = " + DateFormat.getDateTimeInstance().format(new Date(this.mLast11vRoamingFailedTs)));
                    if (this.mLastSignalLevel == 0) {
                        disconnectPoorWifiConnection(true);
                        break;
                    }
                    break;
                case HwWifiRoamingEngine.CMD_REQUEST_ROAMING_NETWORK /*109*/:
                    if (is11vNetworkConnected()) {
                        if (HwWifiRoamingEngine.this.hasMessages(103)) {
                            HwWifiRoamingEngine.this.removeMessages(103);
                        }
                        query11vRoamingNetowrk(16);
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void handleNewRssiRcvd(int newRssi) {
            int currentSignalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(newRssi);
            if (currentSignalLevel >= 0 && currentSignalLevel != this.mLastSignalLevel) {
                HwWifiRoamingEngine.this.LOGD("handleNewRssiRcvd, signal level changed: " + this.mLastSignalLevel + " --> " + currentSignalLevel + ", 802.11v = " + is11vNetworkConnected());
                if (currentSignalLevel == 0 && !HwWifiRoamingEngine.this.hasMessages(HwWifiRoamingEngine.CMD_DISCONNECT_POOR_LINK)) {
                    HwWifiRoamingEngine.this.sendMessageDelayed(HwWifiRoamingEngine.CMD_DISCONNECT_POOR_LINK, 6000);
                } else if (currentSignalLevel > 0 && HwWifiRoamingEngine.this.hasMessages(HwWifiRoamingEngine.CMD_DISCONNECT_POOR_LINK)) {
                    HwWifiRoamingEngine.this.removeMessages(HwWifiRoamingEngine.CMD_DISCONNECT_POOR_LINK);
                }
                if (is11vNetworkConnected() && !this.m11vRoamingOnGoing && currentSignalLevel <= 2) {
                    if (HwWifiRoamingEngine.this.hasMessages(103)) {
                        HwWifiRoamingEngine.this.removeMessages(103);
                    }
                    HwWifiRoamingEngine.this.LOGD("to delay " + HwWifiRoamingEngine.DELAYED_MS_TABLE[currentSignalLevel] + " ms to request roaming 802.11v network.");
                    HwWifiRoamingEngine.this.sendMessageDelayed(103, (long) HwWifiRoamingEngine.DELAYED_MS_TABLE[currentSignalLevel]);
                }
            }
            this.mLastSignalLevel = currentSignalLevel;
        }

        private void disconnectPoorWifiConnection(boolean forceDisconnect) {
            boolean isRoaming;
            if (this.mRoamingOnGoing || this.m11vRoamingOnGoing) {
                isRoaming = true;
            } else {
                isRoaming = HwWifiRoamingEngine.this.hasMessages(103);
            }
            HwWifiRoamingEngine.this.LOGD("ENTER: disconnectPoorWifiConnection, isRoaming = " + isRoaming + ", forceDisconnect = " + forceDisconnect);
            boolean disableAutoDisconnect = SystemProperties.getBoolean(HwWifiRoamingEngine.PROP_DISABLE_AUTO_DISC, false);
            AbsPhoneWindowManager policy = (AbsPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
            boolean isFullScreen = policy != null ? policy.isTopIsFullscreen() : false;
            if (HwWifiRoamingEngine.this.mWifiManager == null) {
                return;
            }
            if ((!isRoaming || forceDisconnect) && !disableAutoDisconnect && !isFullScreen && !HwWifiRoamingEngine.this.isMobileDataInactive()) {
                HwWifiRoamingEngine.this.LOGD("to auto disconnect network quickly due to poor rssi and no roaming (signal level = 0)");
                HwWifiRoamingEngine.this.mWifiManager.disconnect();
            }
        }

        private void query11vRoamingNetowrk(int reason) {
            HwWifiRoamingEngine.this.LOGD("query11vRoamingNetowrk, mRoamingOnGoing = " + this.mRoamingOnGoing + ", m11vRoamingOnGoing = " + this.m11vRoamingOnGoing);
            if (!this.mRoamingOnGoing && !this.m11vRoamingOnGoing) {
                HwWifiRoamingEngine.this.mWifiNative.query11vRoamingNetwork(reason);
                this.m11vRoamingOnGoing = true;
                if (HwWifiRoamingEngine.this.hasMessages(HwWifiRoamingEngine.CMD_11v_ROAMING_TIMEOUT)) {
                    HwWifiRoamingEngine.this.removeMessages(HwWifiRoamingEngine.CMD_11v_ROAMING_TIMEOUT);
                }
                HwWifiRoamingEngine.this.sendMessageDelayed(HwWifiRoamingEngine.CMD_11v_ROAMING_TIMEOUT, 8000);
            }
        }

        private boolean is11vNetworkConnected() {
            ScanResult currentScanResult = HwWifiRoamingEngine.this.mWifiStateMachine.getCurrentScanResult();
            if (currentScanResult == null || !currentScanResult.dot11vNetwork) {
                return false;
            }
            return true;
        }
    }

    static class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            return true;
        }
    }

    class DisconnectedMonitorState extends State {
        DisconnectedMonitorState() {
        }

        public void enter() {
            HwWifiRoamingEngine.this.LOGD("InitialState::DisconnectedMonitorState, enter()");
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case 101:
                    HwWifiRoamingEngine.this.transitionTo(HwWifiRoamingEngine.this.mConnectedMonitorState);
                    return true;
                default:
                    return false;
            }
        }
    }

    public static synchronized HwWifiRoamingEngine getInstance(Context context, WifiStateMachine wsm) {
        HwWifiRoamingEngine hwWifiRoamingEngine;
        synchronized (HwWifiRoamingEngine.class) {
            if (mHwWifiRoamingEngine == null) {
                mHwWifiRoamingEngine = new HwWifiRoamingEngine(context, wsm);
            }
            hwWifiRoamingEngine = mHwWifiRoamingEngine;
        }
        return hwWifiRoamingEngine;
    }

    public static synchronized HwWifiRoamingEngine getInstance() {
        HwWifiRoamingEngine hwWifiRoamingEngine;
        synchronized (HwWifiRoamingEngine.class) {
            hwWifiRoamingEngine = mHwWifiRoamingEngine;
        }
        return hwWifiRoamingEngine;
    }

    private HwWifiRoamingEngine(Context context, WifiStateMachine wsm) {
        super(TAG);
        this.mContext = context;
        this.mWifiStateMachine = wsm;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mWifiNative = WifiNative.getWlanNativeInterface();
        addState(this.mDefaultState);
        addState(this.mConnectedMonitorState, this.mDefaultState);
        addState(this.mDisconnectedMonitorState, this.mDefaultState);
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
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        intentFilter.addAction(ACTION_11v_ROAMING_NETWORK_FOUND);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (info != null && info.getDetailedState() == DetailedState.DISCONNECTED) {
                        HwWifiRoamingEngine.this.sendMessage(102);
                    } else if (info != null && info.getDetailedState() == DetailedState.CONNECTED) {
                        HwWifiRoamingEngine.this.sendMessage(101);
                    }
                } else if ("android.net.wifi.RSSI_CHANGED".equals(intent.getAction())) {
                    int newRssi = intent.getIntExtra("newRssi", -127);
                    if (newRssi != -127) {
                        HwWifiRoamingEngine.this.sendMessage(104, newRssi, 0);
                    }
                }
            }
        }, intentFilter);
    }

    public synchronized void notifyWifiRoamingStarted() {
        LOGD("ENTER: notifyWifiRoamingStarted()");
        if (this.mInitialized) {
            sendMessage(CMD_ROAMING_STARTED_RCVD);
        }
    }

    public synchronized void notifyWifiRoamingCompleted() {
        LOGD("ENTER: notifyWifiRoamingCompleted()");
        if (this.mInitialized) {
            sendMessage(CMD_ROAMING_COMPLETED_RCVD);
        }
    }

    public synchronized void requestRoamingByNoInternet() {
        LOGD("ENTER: requestRoamingByNoInternet()");
        if (this.mInitialized) {
            sendMessage(CMD_REQUEST_ROAMING_NETWORK);
        }
    }

    private boolean isMobileDataInactive() {
        return !WifiProCommonUtils.isMobileDataOff(this.mContext) ? WifiProCommonUtils.isNoSIMCard(this.mContext) : true;
    }

    public void LOGD(String msg) {
        Log.d(TAG, msg);
    }
}
