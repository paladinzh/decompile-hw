package com.huawei.netassistant.wifisecure;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.UserHandle;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.sdk.tmsdk.TMSEngineFeature;
import com.huawei.systemmanager.util.HwLog;
import java.util.Random;
import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.wifidetect.IWifiDetectListener;
import tmsdk.bg.module.wifidetect.WifiDetectManager;

public class WifiPlusSecurityMonitor {
    public static final String ACTION_NOTIFY_WIFI_SECURITY_STATUS = "com.huawei.wifipro.ACTION_NOTIFY_WIFI_SECURITY_STATUS";
    public static final String ACTION_QUERY_WIFI_SECURITY = "com.huawei.wifipro.ACTION_QUERY_WIFI_SECURITY";
    private static final long DNS_PHISHING_TIMEOUT_MS = 28000;
    public static final String FLAG_ARP_RESULT = "com.huawei.wifipro.FLAG_ARP_RESULT";
    public static final String FLAG_BSSID = "com.huawei.wifipro.FLAG_BSSID";
    public static final String FLAG_DNS_PHISHING_RESULT = "com.huawei.wifipro.FLAG_DNS_PHISHING_RESULT";
    public static final String FLAG_SECURITY_STATUS = "com.huawei.wifipro.FLAG_SECURITY_STATUS";
    public static final String FLAG_SSID = "com.huawei.wifipro.FLAG_SSID";
    private static final int MSG_CHECK_ARP = 102;
    private static final int MSG_CHECK_DNS_PHISHING = 101;
    private static final int MSG_HANDLE_ARP_FAILED = 107;
    private static final int MSG_HANDLE_ARP_RESULT = 106;
    private static final int MSG_HANDLE_DNS_PHISHING_FAILED = 104;
    private static final int MSG_HANDLE_DNS_PHISHING_RESULT = 103;
    private static final int MSG_HANDLE_DNS_PHISHING_TIMEOUT = 105;
    private static final int MSG_NETWORK_DISCONNECTED = 108;
    private static final String TAG = "WifiPlusSecurityMonitor";
    public static final String WIFI_PRO_SECURITY_REQ_PERMISSION = "com.huawei.wifipro.permission.WIFI_SECURITY_CHECK";
    public static final String WIFI_PRO_SECURITY_RESP_PERMISSION = "com.huawei.permission.WIFIPRO_BQE_CLIENT_RECEIVE";
    public static final int WIFI_SECURITY_ARP_FAILED = 4;
    public static final int WIFI_SECURITY_DNS_FAILED = 2;
    public static final int WIFI_SECURITY_DNS_PHISHING_TIMEOUT = 1;
    public static final int WIFI_SECURITY_OK = 0;
    public static final int WIFI_SECURITY_PHISHING_FAILED = 3;
    public static final int WIFI_SECURITY_UNKNOWN = -1;
    private static WifiPlusSecurityMonitor sInstance = null;
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private String mCurrentBssid;
    private int mCurrentSessionId;
    private String mCurrentSsid;
    private Handler mHandler;
    private boolean mInitialized = false;
    private WifiDetectManager mWifiDetectManager;
    private Object mWifiDetectManagerLock = new Object();

    private class WifiDetectListener implements IWifiDetectListener {
        private String bssid;
        private int sessionId;
        private String ssid;

        public WifiDetectListener(String ssid, String bssid, int sessionId) {
            this.ssid = ssid;
            this.bssid = bssid;
            this.sessionId = sessionId;
        }

        public void onResult(int arg0) {
            if (this.bssid != null && this.bssid.equals(WifiPlusSecurityMonitor.this.mCurrentBssid) && this.ssid != null && this.ssid.equals(WifiPlusSecurityMonitor.this.mCurrentSsid) && this.sessionId == WifiPlusSecurityMonitor.this.mCurrentSessionId) {
                Bundle bundle = new Bundle();
                bundle.putString(WifiPlusSecurityMonitor.FLAG_SSID, this.ssid);
                bundle.putString(WifiPlusSecurityMonitor.FLAG_BSSID, this.bssid);
                bundle.putInt(WifiPlusSecurityMonitor.FLAG_DNS_PHISHING_RESULT, arg0);
                WifiPlusSecurityMonitor.this.LOGD("WifiDetectListener::onResult, ssid = " + this.ssid + ", sid = " + WifiPlusSecurityMonitor.this.mCurrentSessionId + ", result = " + arg0);
                WifiPlusSecurityMonitor.this.mHandler.sendMessage(Message.obtain(WifiPlusSecurityMonitor.this.mHandler, 103, bundle));
                return;
            }
            WifiPlusSecurityMonitor.this.LOGD("WifiDetectListener::onResult, skip the old, ssid = " + this.ssid + ", sid = " + WifiPlusSecurityMonitor.this.mCurrentSessionId + ", result = " + arg0);
        }
    }

    private WifiPlusSecurityMonitor(Context context) {
        this.mContext = context;
    }

    public static synchronized WifiPlusSecurityMonitor getInstance(Context context) {
        WifiPlusSecurityMonitor wifiPlusSecurityMonitor;
        synchronized (WifiPlusSecurityMonitor.class) {
            if (sInstance == null) {
                sInstance = new WifiPlusSecurityMonitor(context);
            }
            wifiPlusSecurityMonitor = sInstance;
        }
        return wifiPlusSecurityMonitor;
    }

    public synchronized void init() {
        if (!this.mInitialized) {
            HandlerThread handlerThread = new HandlerThread("wifipro_network_security_monitor");
            handlerThread.start();
            this.mHandler = new Handler(handlerThread.getLooper()) {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 101:
                            WifiPlusSecurityMonitor.this.asyncCheckDnsPhishing(msg);
                            break;
                        case 102:
                            WifiPlusSecurityMonitor.this.asyncCheckArp(msg);
                            break;
                        case 103:
                        case 104:
                            if (WifiPlusSecurityMonitor.this.mHandler.hasMessages(105)) {
                                WifiPlusSecurityMonitor.this.LOGD("MSG_HANDLE_DNS_PHISHING_TIMEOUT msg removed because of result received.");
                                WifiPlusSecurityMonitor.this.mHandler.removeMessages(105);
                            }
                            WifiPlusSecurityMonitor.this.handleDnsPhishingResult(msg);
                            break;
                        case 105:
                            WifiPlusSecurityMonitor.this.handleDnsPhishingTimeout(msg);
                            break;
                        case 106:
                        case WifiPlusSecurityMonitor.MSG_HANDLE_ARP_FAILED /*107*/:
                            WifiPlusSecurityMonitor.this.handleArpResult(msg);
                            break;
                        case 108:
                            if (WifiPlusSecurityMonitor.this.mHandler.hasMessages(105)) {
                                WifiPlusSecurityMonitor.this.LOGD("MSG_HANDLE_DNS_PHISHING_TIMEOUT msg removed because of disconnected.");
                                WifiPlusSecurityMonitor.this.mHandler.removeMessages(105);
                            }
                            WifiPlusSecurityMonitor.this.releaseDetectManager();
                            break;
                    }
                    super.handleMessage(msg);
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.wifi.STATE_CHANGE");
            intentFilter.addAction(ACTION_QUERY_WIFI_SECURITY);
            this.mBroadcastReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (WifiPlusSecurityMonitor.ACTION_QUERY_WIFI_SECURITY.equals(intent.getAction())) {
                        WifiPlusSecurityMonitor.this.LOGD("receive broadcast, action = " + intent.getAction());
                        if (HsmWifiDetectManager.shouldTriggerWifiDetect(WifiPlusSecurityMonitor.this.mContext)) {
                            WifiPlusSecurityMonitor.this.mCurrentSsid = intent.getStringExtra(WifiPlusSecurityMonitor.FLAG_SSID);
                            WifiPlusSecurityMonitor.this.mCurrentBssid = intent.getStringExtra(WifiPlusSecurityMonitor.FLAG_BSSID);
                            WifiPlusSecurityMonitor.this.queryWifiSecurity(WifiPlusSecurityMonitor.this.mCurrentSsid, WifiPlusSecurityMonitor.this.mCurrentBssid);
                        }
                    } else if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                        NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                        if (info != null && DetailedState.DISCONNECTED == info.getDetailedState() && WifiPlusSecurityMonitor.this.mCurrentSsid != null) {
                            WifiPlusSecurityMonitor.this.LOGD("NETWORK_STATE_CHANGED_ACTION, network is connected --> disconnected.");
                            WifiPlusSecurityMonitor.this.mHandler.sendMessage(Message.obtain(WifiPlusSecurityMonitor.this.mHandler, 108, 0, 0));
                        }
                    }
                }
            };
            this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter, WIFI_PRO_SECURITY_REQ_PERMISSION, null);
            this.mInitialized = true;
            LOGD("rsetup DONE!!!");
        }
    }

    private boolean initDetectManager() {
        synchronized (this.mWifiDetectManagerLock) {
            if (this.mWifiDetectManager != null) {
                this.mWifiDetectManager.free();
                this.mWifiDetectManager = null;
            }
            Utility.initSDK(this.mContext);
            if (TMSEngineFeature.isSupportTMS()) {
                try {
                    this.mWifiDetectManager = (WifiDetectManager) ManagerCreatorB.getManager(WifiDetectManager.class);
                    if (this.mWifiDetectManager == null) {
                        LOGW("initDetectManager: Fail to create WifiDetectManager");
                        return false;
                    }
                    int initCode = this.mWifiDetectManager.init();
                    if (initCode < 0) {
                        LOGW("initDetectManager: Failed, initCode = " + initCode);
                        return false;
                    }
                    LOGD("initDetectManager: WifiDetectManager initCode = " + initCode);
                    return true;
                } catch (NullPointerException e) {
                    LOGW("initDetectManager:NullPointerException found ");
                    return false;
                } catch (NoClassDefFoundError e2) {
                    LOGW("initDetectManager: tms jar not exists.");
                    return false;
                } catch (UnsatisfiedLinkError e3) {
                    LOGW("initDetectManager: UnsatisfiedLinkError.");
                    return false;
                }
            }
            LOGW("initDetectManager: TMS is not supported after initSDK");
            return false;
        }
    }

    private void releaseDetectManager() {
        synchronized (this.mWifiDetectManagerLock) {
            if (this.mWifiDetectManager != null) {
                LOGD("releaseDetectManager, ssid = " + this.mCurrentSsid + ", sid = " + this.mCurrentSessionId);
                this.mWifiDetectManager.free();
                this.mWifiDetectManager = null;
                this.mCurrentSsid = null;
                this.mCurrentBssid = null;
                this.mCurrentSessionId = -1;
            }
        }
    }

    private void queryWifiSecurity(String ssid, String bssid) {
        if (this.mHandler != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FLAG_SSID, ssid);
            bundle.putString(FLAG_BSSID, bssid);
            this.mHandler.sendMessage(Message.obtain(this.mHandler, 101, bundle));
        }
    }

    private void asyncCheckDnsPhishing(Message msg) {
        if (initDetectManager()) {
            final Bundle bundle = msg.obj;
            String ssid = bundle.getString(FLAG_SSID);
            String bssid = bundle.getString(FLAG_BSSID);
            this.mCurrentSessionId = new Random().nextInt(100000);
            final WifiDetectListener wifiDetectListener = new WifiDetectListener(ssid, bssid, this.mCurrentSessionId);
            setProcessDefaultWlan();
            LOGD("asyncCheckDnsPhishing, ssid = " + ssid + ", sid = " + this.mCurrentSessionId);
            Thread detectThread = new Thread(new Runnable() {
                public void run() {
                    synchronized (WifiPlusSecurityMonitor.this.mWifiDetectManagerLock) {
                        if (WifiPlusSecurityMonitor.this.mWifiDetectManager == null) {
                            WifiPlusSecurityMonitor.this.LOGW("Warnning: WifiPlusSecurityMonitor-detectDnsAndPhishing, mWifiDetectManager is null.");
                            return;
                        }
                        int ret = WifiPlusSecurityMonitor.this.mWifiDetectManager.detectDnsAndPhishing(wifiDetectListener);
                        if (ret < 0) {
                            WifiPlusSecurityMonitor.this.LOGW("detectDnsAndPhishing: Fail to start detect, ret = " + ret);
                            bundle.putInt(WifiPlusSecurityMonitor.FLAG_DNS_PHISHING_RESULT, ret);
                            WifiPlusSecurityMonitor.this.mHandler.sendMessage(Message.obtain(WifiPlusSecurityMonitor.this.mHandler, 104, bundle));
                            return;
                        }
                        WifiPlusSecurityMonitor.this.LOGD("detectDnsAndPhishing: Detect started, ret = " + ret);
                    }
                }
            });
            detectThread.setName("WifiPlusSecurityMonitor-detectDnsAndPhishing");
            detectThread.start();
            Message message = new Message();
            message.what = 105;
            message.obj = bundle;
            this.mHandler.sendMessageDelayed(message, DNS_PHISHING_TIMEOUT_MS);
        }
    }

    private void asyncCheckArp(Message msg) {
        synchronized (this.mWifiDetectManagerLock) {
            if (this.mWifiDetectManager == null) {
                LOGW("Warnning: asyncCheckArp, mWifiDetectManager is null.");
                return;
            }
            final Bundle bundle = msg.obj;
            LOGD("asyncCheckNetworkArp, ssid = " + bundle.getString(FLAG_SSID) + ", sid = " + this.mCurrentSessionId);
            Thread detectThread = new Thread(new Runnable() {
                public void run() {
                    synchronized (WifiPlusSecurityMonitor.this.mWifiDetectManagerLock) {
                        if (WifiPlusSecurityMonitor.this.mWifiDetectManager == null) {
                            WifiPlusSecurityMonitor.this.LOGW("Warnning: WifiPlusSecurityMonitor-detectARP, mWifiDetectManager is null.");
                            return;
                        }
                        int ret = WifiPlusSecurityMonitor.this.mWifiDetectManager.detectARP("mdetector");
                        bundle.putInt(WifiPlusSecurityMonitor.FLAG_ARP_RESULT, ret);
                        if (ret < 0) {
                            WifiPlusSecurityMonitor.this.LOGW("detectARP: Fail to start detect, nRet = " + ret);
                            WifiPlusSecurityMonitor.this.mHandler.sendMessage(Message.obtain(WifiPlusSecurityMonitor.this.mHandler, WifiPlusSecurityMonitor.MSG_HANDLE_ARP_FAILED, bundle));
                            return;
                        }
                        WifiPlusSecurityMonitor.this.mHandler.sendMessage(Message.obtain(WifiPlusSecurityMonitor.this.mHandler, 106, bundle));
                    }
                }
            });
            detectThread.setName("WifiPlusSecurityMonitor-detectARP");
            detectThread.start();
        }
    }

    private void handleDnsPhishingResult(Message msg) {
        Bundle bundle = msg.obj;
        String ssid = bundle.getString(FLAG_SSID);
        String bssid = bundle.getString(FLAG_BSSID);
        int result = bundle.getInt(FLAG_DNS_PHISHING_RESULT);
        LOGD("handleDnsPhishingResult, ssid = " + ssid + ", sid = " + this.mCurrentSessionId + ", result = " + result);
        if (result == 17) {
            this.mHandler.sendMessage(Message.obtain(this.mHandler, 102, bundle));
        } else if (result == 18) {
            notifyWifiSecurityStatus(ssid, bssid, 2);
        } else if (result == 19) {
            notifyWifiSecurityStatus(ssid, bssid, 3);
        } else {
            notifyWifiSecurityStatus(ssid, bssid, -1);
        }
    }

    private void handleDnsPhishingTimeout(Message msg) {
        if (this.mCurrentSsid != null) {
            Bundle bundle = msg.obj;
            String ssid = bundle.getString(FLAG_SSID);
            String bssid = bundle.getString(FLAG_BSSID);
            LOGD("handleDnsPhishingTimeout, ssid = " + ssid + ", sid = " + this.mCurrentSessionId);
            notifyWifiSecurityStatus(ssid, bssid, 1);
        }
    }

    private void handleArpResult(Message msg) {
        Bundle bundle = msg.obj;
        String ssid = bundle.getString(FLAG_SSID);
        String bssid = bundle.getString(FLAG_BSSID);
        int result = bundle.getInt(FLAG_ARP_RESULT);
        LOGD("handleArpResult, ssid = " + ssid + ", sid = " + this.mCurrentSessionId + ", result = " + result);
        if (result == 261) {
            notifyWifiSecurityStatus(ssid, bssid, 0);
        } else if (result == 262) {
            notifyWifiSecurityStatus(ssid, bssid, 4);
        } else {
            notifyWifiSecurityStatus(ssid, bssid, -1);
        }
    }

    private void notifyWifiSecurityStatus(String ssid, String bssid, int status) {
        LOGD("notifyWifiSecurityStatus, ssid = " + ssid + ", sid = " + this.mCurrentSessionId + ", status = " + status);
        Bundle bundle = new Bundle();
        bundle.putString(FLAG_SSID, ssid);
        bundle.putString(FLAG_BSSID, bssid);
        bundle.putInt(FLAG_SECURITY_STATUS, status);
        Intent intent = new Intent(ACTION_NOTIFY_WIFI_SECURITY_STATUS);
        intent.setFlags(67108864);
        intent.putExtras(bundle);
        if (!(this.mContext == null || this.mCurrentSsid == null || !HsmWifiDetectManager.shouldTriggerWifiDetect(this.mContext))) {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, WIFI_PRO_SECURITY_RESP_PERMISSION);
        }
        releaseDetectManager();
    }

    private void setProcessDefaultWlan() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        Network[] networks = connectivityManager.getAllNetworks();
        if (networks != null) {
            int i = 0;
            while (i < networks.length) {
                NetworkInfo netInfo = connectivityManager.getNetworkInfo(networks[i]);
                if (netInfo == null || netInfo.getType() != 1) {
                    i++;
                } else if (connectivityManager.bindProcessToNetwork(networks[i])) {
                    LOGD("Success! Bind Device to: " + netInfo.getTypeName());
                    return;
                } else {
                    LOGW("Fail! Unable Bind Device to: " + netInfo.getTypeName());
                    return;
                }
            }
        }
    }

    private void LOGD(String msg) {
        HwLog.i(TAG, msg);
    }

    private void LOGW(String msg) {
        HwLog.w(TAG, msg);
    }
}
