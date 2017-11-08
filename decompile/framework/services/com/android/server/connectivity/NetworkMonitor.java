package com.android.server.connectivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkRequest;
import android.net.ProxyInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.metrics.NetworkEvent;
import android.net.metrics.ValidationProbeEvent;
import android.net.util.Stopwatch;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.LocalLog.ReadOnlyLocalLog;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.WakeupMessage;
import com.android.server.AbsNetworkMonitor;
import com.android.server.HwConnectivityManager;
import com.android.server.HwServiceFactory;
import com.android.server.am.ProcessList;
import com.android.server.location.FlpHardwareProvider;
import com.android.server.location.LocationFudger;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class NetworkMonitor extends AbsNetworkMonitor {
    public static final String ACTION_NETWORK_CONDITIONS_MEASURED = "android.net.conn.NETWORK_CONDITIONS_MEASURED";
    private static final String BAKUP_SERVER = "www.baidu.com";
    private static final String BAKUP_SERV_PAGE = "/";
    private static final int BASE = 532480;
    private static final int BLAME_FOR_EVALUATION_ATTEMPTS = 5;
    private static final int CAPTIVE_PORTAL_REEVALUATE_DELAY_MS = 600000;
    private static final int CMD_CAPTIVE_PORTAL_APP_FINISHED = 532489;
    private static final int CMD_CAPTIVE_PORTAL_RECHECK = 532492;
    public static final int CMD_FORCE_REEVALUATION = 532488;
    private static final int CMD_LAUNCH_CAPTIVE_PORTAL_APP = 532491;
    private static final int CMD_LINGER_EXPIRED = 532484;
    public static final int CMD_NETWORK_CONNECTED = 532481;
    public static final int CMD_NETWORK_DISCONNECTED = 532487;
    public static final int CMD_NETWORK_LINGER = 532483;
    private static final int CMD_REEVALUATE = 532486;
    private static final String COUNTRY_CODE_CN = "460";
    private static final boolean DBG = true;
    private static int DEFAULT_LINGER_DELAY_MS = 30000;
    private static final String DEFAULT_SERVER = "connectivitycheck.gstatic.com";
    private static final String DEFAULT_SERV_PAGE = "/generate_204";
    public static final int EVENT_NETWORK_LINGER_COMPLETE = 532485;
    public static final int EVENT_NETWORK_TESTED = 532482;
    public static final int EVENT_PROVISIONING_NOTIFICATION = 532490;
    public static final String EXTRA_BSSID = "extra_bssid";
    public static final String EXTRA_CELL_ID = "extra_cellid";
    public static final String EXTRA_CONNECTIVITY_TYPE = "extra_connectivity_type";
    public static final String EXTRA_IS_CAPTIVE_PORTAL = "extra_is_captive_portal";
    public static final String EXTRA_NETWORK_TYPE = "extra_network_type";
    public static final String EXTRA_REQUEST_TIMESTAMP_MS = "extra_request_timestamp_ms";
    public static final String EXTRA_RESPONSE_RECEIVED = "extra_response_received";
    public static final String EXTRA_RESPONSE_TIMESTAMP_MS = "extra_response_timestamp_ms";
    public static final String EXTRA_SSID = "extra_ssid";
    private static final int IGNORE_REEVALUATE_ATTEMPTS = 5;
    private static final int INITIAL_REEVALUATE_DELAY_MS = 1000;
    private static final int INVALID_UID = -1;
    private static final String LINGER_DELAY_PROPERTY = "persist.netmon.linger";
    private static final int MAX_REEVALUATE_DELAY_MS = 600000;
    public static final int NETWORK_TEST_RESULT_INVALID = 1;
    public static final int NETWORK_TEST_RESULT_VALID = 0;
    private static final String PERMISSION_ACCESS_NETWORK_CONDITIONS = "android.permission.ACCESS_NETWORK_CONDITIONS";
    private static final String SERVER_BAIDU = "baidu";
    private static final int SOCKET_TIMEOUT_MS = 10000;
    private static final String TAG = NetworkMonitor.class.getSimpleName();
    private final AlarmManager mAlarmManager;
    private final State mCaptivePortalState = new CaptivePortalState();
    private final Handler mConnectivityServiceHandler;
    private final Context mContext;
    private final NetworkRequest mDefaultRequest;
    private final State mDefaultState = new DefaultState();
    private boolean mDontDisplaySigninNotification = false;
    private final State mEvaluatingState = new EvaluatingState();
    private final Stopwatch mEvaluationTimer = new Stopwatch();
    private boolean mIsCaptivePortalCheckEnabled;
    private CustomIntentReceiver mLaunchCaptivePortalAppBroadcastReceiver = null;
    private final int mLingerDelayMs;
    private int mLingerToken = 0;
    private final State mLingeringState = new LingeringState();
    private final State mMaybeNotifyState = new MaybeNotifyState();
    private final int mNetId;
    private final NetworkAgentInfo mNetworkAgentInfo;
    private int mReevaluateToken = 0;
    private final TelephonyManager mTelephonyManager;
    private int mUidResponsibleForReeval = -1;
    private String mUrlHeadFieldLocation = null;
    private boolean mUseHttps;
    private boolean mUserDoesNotWant = false;
    private final State mValidatedState = new ValidatedState();
    private final WifiManager mWifiManager;
    public boolean systemReady = false;
    private final LocalLog validationLogs = new LocalLog(20);

    /* renamed from: com.android.server.connectivity.NetworkMonitor$1ProbeThread */
    final class AnonymousClass1ProbeThread extends Thread {
        private final boolean mIsHttps;
        private volatile CaptivePortalProbeResult mResult;
        final /* synthetic */ AtomicReference val$finalResult;
        final /* synthetic */ URL val$httpUrl;
        final /* synthetic */ URL val$httpsUrl;
        final /* synthetic */ CountDownLatch val$latch;

        public AnonymousClass1ProbeThread(boolean isHttps, URL val$httpsUrl, URL val$httpUrl, AtomicReference val$finalResult, CountDownLatch val$latch) {
            this.val$httpsUrl = val$httpsUrl;
            this.val$httpUrl = val$httpUrl;
            this.val$finalResult = val$finalResult;
            this.val$latch = val$latch;
            this.mIsHttps = isHttps;
        }

        public CaptivePortalProbeResult getResult() {
            return this.mResult;
        }

        public void run() {
            if (this.mIsHttps) {
                this.mResult = NetworkMonitor.this.sendHttpProbe(this.val$httpsUrl, 2);
            } else {
                this.mResult = NetworkMonitor.this.sendHttpProbe(this.val$httpUrl, 1);
            }
            if ((this.mIsHttps && this.mResult.isSuccessful()) || (!this.mIsHttps && this.mResult.isPortal())) {
                this.val$finalResult.compareAndSet(null, this.mResult);
                this.val$latch.countDown();
            }
            this.val$latch.countDown();
        }
    }

    public static final class CaptivePortalProbeResult {
        static final CaptivePortalProbeResult FAILED = new CaptivePortalProbeResult(599, null);
        int mHttpResponseCode;
        final String mRedirectUrl;

        public CaptivePortalProbeResult(int httpResponseCode, String redirectUrl) {
            this.mHttpResponseCode = httpResponseCode;
            this.mRedirectUrl = redirectUrl;
        }

        boolean isSuccessful() {
            return this.mHttpResponseCode == 204 ? NetworkMonitor.DBG : false;
        }

        boolean isPortal() {
            return (isSuccessful() || this.mHttpResponseCode < 200 || this.mHttpResponseCode > 399) ? false : NetworkMonitor.DBG;
        }
    }

    private class CaptivePortalState extends State {
        private static final String ACTION_LAUNCH_CAPTIVE_PORTAL_APP = "android.net.netmon.launchCaptivePortalApp";

        private CaptivePortalState() {
        }

        public void enter() {
            if (NetworkMonitor.this.mEvaluationTimer.isRunning()) {
                NetworkEvent.logCaptivePortalFound(NetworkMonitor.this.mNetId, NetworkMonitor.this.mEvaluationTimer.stop());
                NetworkMonitor.this.mEvaluationTimer.reset();
            }
            if (!NetworkMonitor.this.mDontDisplaySigninNotification) {
                if (NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver == null) {
                    NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver = new CustomIntentReceiver(ACTION_LAUNCH_CAPTIVE_PORTAL_APP, new Random().nextInt(), NetworkMonitor.CMD_LAUNCH_CAPTIVE_PORTAL_APP);
                }
                NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_PROVISIONING_NOTIFICATION, 1, NetworkMonitor.this.mNetworkAgentInfo.network.netId, NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver.getPendingIntent()));
                NetworkMonitor.this.sendMessageDelayed(NetworkMonitor.CMD_CAPTIVE_PORTAL_RECHECK, 0, LocationFudger.FASTEST_INTERVAL_MS);
            }
        }

        public void exit() {
            NetworkMonitor.this.removeMessages(NetworkMonitor.CMD_CAPTIVE_PORTAL_RECHECK);
        }
    }

    private class CustomIntentReceiver extends BroadcastReceiver {
        private final String mAction;
        private final int mToken;
        private final int mWhat;

        CustomIntentReceiver(String action, int token, int what) {
            this.mToken = token;
            this.mWhat = what;
            this.mAction = action + "_" + NetworkMonitor.this.mNetworkAgentInfo.network.netId + "_" + token;
            NetworkMonitor.this.mContext.registerReceiver(this, new IntentFilter(this.mAction));
        }

        public PendingIntent getPendingIntent() {
            Intent intent = new Intent(this.mAction);
            intent.setPackage(NetworkMonitor.this.mContext.getPackageName());
            return PendingIntent.getBroadcast(NetworkMonitor.this.mContext, 0, intent, 0);
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(this.mAction)) {
                NetworkMonitor.this.sendMessage(NetworkMonitor.this.obtainMessage(this.mWhat, this.mToken));
            }
        }
    }

    private class DefaultState extends State {
        private DefaultState() {
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case NetworkMonitor.CMD_NETWORK_CONNECTED /*532481*/:
                    NetworkEvent.logEvent(NetworkMonitor.this.mNetId, 1);
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mEvaluatingState);
                    return NetworkMonitor.DBG;
                case NetworkMonitor.CMD_NETWORK_LINGER /*532483*/:
                    NetworkMonitor.this.log("Lingering");
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mLingeringState);
                    return NetworkMonitor.DBG;
                case NetworkMonitor.CMD_NETWORK_DISCONNECTED /*532487*/:
                    NetworkEvent.logEvent(NetworkMonitor.this.mNetId, 7);
                    if (NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver != null) {
                        NetworkMonitor.this.mContext.unregisterReceiver(NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver);
                        NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver = null;
                    }
                    NetworkMonitor.this.releaseNetworkPropertyChecker();
                    NetworkMonitor.this.quit();
                    return NetworkMonitor.DBG;
                case NetworkMonitor.CMD_FORCE_REEVALUATION /*532488*/:
                case NetworkMonitor.CMD_CAPTIVE_PORTAL_RECHECK /*532492*/:
                    NetworkMonitor.this.log("Forcing reevaluation for UID " + message.arg1);
                    NetworkMonitor.this.mUidResponsibleForReeval = message.arg1;
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mEvaluatingState);
                    return NetworkMonitor.DBG;
                case NetworkMonitor.CMD_CAPTIVE_PORTAL_APP_FINISHED /*532489*/:
                    NetworkMonitor.this.log("CaptivePortal App responded with " + message.arg1);
                    NetworkMonitor.this.mUseHttps = false;
                    switch (message.arg1) {
                        case 0:
                            NetworkMonitor.this.sendMessage(NetworkMonitor.CMD_FORCE_REEVALUATION, 0, 0);
                            break;
                        case 1:
                            NetworkMonitor.this.mDontDisplaySigninNotification = NetworkMonitor.DBG;
                            NetworkMonitor.this.mUserDoesNotWant = NetworkMonitor.DBG;
                            NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, 1, NetworkMonitor.this.mNetId, null));
                            NetworkMonitor.this.mUidResponsibleForReeval = 0;
                            NetworkMonitor.this.transitionTo(NetworkMonitor.this.mEvaluatingState);
                            break;
                        case 2:
                            NetworkMonitor.this.mDontDisplaySigninNotification = NetworkMonitor.DBG;
                            NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                            break;
                    }
                    return NetworkMonitor.DBG;
                case AbsNetworkMonitor.CMD_NETWORK_ROAMING_CONNECTED /*532581*/:
                    NetworkMonitor.this.log("DefaultState receive CMD_NETWORK_ROAMING_CONNECTED");
                    NetworkMonitor.this.resetNetworkMonitor();
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mEvaluatingState);
                    return NetworkMonitor.DBG;
                default:
                    return NetworkMonitor.DBG;
            }
        }
    }

    private class EvaluatingState extends State {
        private int mAttempts;
        private int mReevaluateDelayMs;

        private EvaluatingState() {
        }

        public void enter() {
            if (!NetworkMonitor.this.mEvaluationTimer.isStarted()) {
                NetworkMonitor.this.mEvaluationTimer.start();
            }
            NetworkMonitor networkMonitor = NetworkMonitor.this;
            NetworkMonitor networkMonitor2 = NetworkMonitor.this;
            networkMonitor.sendMessage(NetworkMonitor.CMD_REEVALUATE, networkMonitor2.mReevaluateToken = networkMonitor2.mReevaluateToken + 1, 0);
            if (NetworkMonitor.this.mUidResponsibleForReeval != -1) {
                TrafficStats.setThreadStatsUid(NetworkMonitor.this.mUidResponsibleForReeval);
                NetworkMonitor.this.mUidResponsibleForReeval = -1;
            }
            this.mReevaluateDelayMs = 1000;
            this.mAttempts = 0;
        }

        public boolean processMessage(Message message) {
            boolean z = NetworkMonitor.DBG;
            switch (message.what) {
                case NetworkMonitor.CMD_REEVALUATE /*532486*/:
                    if (message.arg1 != NetworkMonitor.this.mReevaluateToken || NetworkMonitor.this.mUserDoesNotWant) {
                        return NetworkMonitor.DBG;
                    }
                    if (NetworkMonitor.this.mDefaultRequest.networkCapabilities.satisfiedByNetworkCapabilities(NetworkMonitor.this.mNetworkAgentInfo.networkCapabilities)) {
                        this.mAttempts++;
                        if (NetworkMonitor.this.mNetworkAgentInfo.networkInfo.getType() == 0) {
                            NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                            return NetworkMonitor.DBG;
                        }
                        CaptivePortalProbeResult probeResult = new CaptivePortalProbeResult(599, null);
                        if (!NetworkMonitor.this.isWifiProEnabled() || !NetworkMonitor.this.mIsCaptivePortalCheckEnabled) {
                            probeResult = NetworkMonitor.this.isCaptivePortal(NetworkMonitor.getCaptivePortalServerUrl(NetworkMonitor.this.mContext), NetworkMonitor.DEFAULT_SERV_PAGE);
                            if (probeResult.mHttpResponseCode < 200 || probeResult.mHttpResponseCode > 399) {
                                String operator = NetworkMonitor.this.mTelephonyManager.getNetworkOperator();
                                if (!(operator == null || operator.length() == 0 || !operator.startsWith(NetworkMonitor.COUNTRY_CODE_CN))) {
                                    NetworkMonitor.this.log("NetworkMonitor isCaptivePortal transit to link baidu");
                                    probeResult = NetworkMonitor.this.isCaptivePortal(NetworkMonitor.BAKUP_SERVER, NetworkMonitor.BAKUP_SERV_PAGE);
                                    if (probeResult.mHttpResponseCode >= 200 && probeResult.mHttpResponseCode <= 399 && probeResult.mHttpResponseCode != 301 && probeResult.mHttpResponseCode != 302) {
                                        probeResult.mHttpResponseCode = 204;
                                    } else if (probeResult.mHttpResponseCode == 301 || probeResult.mHttpResponseCode == 302) {
                                        NetworkMonitor.this.log("mUrlHeadFieldLocation" + NetworkMonitor.this.mUrlHeadFieldLocation);
                                        String host = NetworkMonitor.this.parseHostByLocation(NetworkMonitor.this.mUrlHeadFieldLocation);
                                        if (host != null && host.contains(NetworkMonitor.SERVER_BAIDU)) {
                                            NetworkMonitor.this.log("host contains baidu ,change httpResponseCode to 204");
                                            probeResult.mHttpResponseCode = 204;
                                        }
                                    }
                                }
                            }
                        } else if (NetworkMonitor.this.isCheckCompletedByWifiPro()) {
                            return NetworkMonitor.DBG;
                        } else {
                            probeResult.mHttpResponseCode = NetworkMonitor.this.getRespCodeByWifiPro();
                            if (probeResult.mHttpResponseCode != 599) {
                                NetworkMonitor.this.sendNetworkConditionsBroadcast(NetworkMonitor.DBG, probeResult.mHttpResponseCode != 204 ? NetworkMonitor.DBG : false, NetworkMonitor.this.getReqTimestamp(), NetworkMonitor.this.getRespTimestamp());
                            }
                        }
                        HwConnectivityManager hwConnectivityManager = HwServiceFactory.getHwConnectivityManager();
                        Context -get2 = NetworkMonitor.this.mContext;
                        boolean z2 = (probeResult.mHttpResponseCode == 204 || probeResult.mHttpResponseCode == 599) ? false : NetworkMonitor.DBG;
                        hwConnectivityManager.captivePortalCheckCompleted(-get2, z2);
                        if (probeResult.isSuccessful()) {
                            NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                        } else if (probeResult.isPortal()) {
                            if (NetworkMonitor.this.isWifiProEnabled()) {
                                NetworkMonitor.this.reportPortalNetwork(NetworkMonitor.this.mConnectivityServiceHandler, NetworkMonitor.this.mNetId, probeResult.mRedirectUrl);
                            } else {
                                NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, 1, NetworkMonitor.this.mNetId, probeResult.mRedirectUrl));
                            }
                            NetworkMonitor.this.transitionTo(NetworkMonitor.this.mCaptivePortalState);
                        } else {
                            NetworkMonitor networkMonitor = NetworkMonitor.this;
                            NetworkMonitor networkMonitor2 = NetworkMonitor.this;
                            Message msg = networkMonitor.obtainMessage(NetworkMonitor.CMD_REEVALUATE, networkMonitor2.mReevaluateToken = networkMonitor2.mReevaluateToken + 1, 0);
                            if (!NetworkMonitor.this.isWifiProEnabled() || NetworkMonitor.this.isCheckCompletedByWifiPro()) {
                                NetworkMonitor.this.sendMessageDelayed(msg, (long) this.mReevaluateDelayMs);
                                NetworkEvent.logEvent(NetworkMonitor.this.mNetId, 3);
                                NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, 1, NetworkMonitor.this.mNetId, probeResult.mRedirectUrl));
                                if (this.mAttempts >= 5) {
                                    TrafficStats.clearThreadStatsUid();
                                }
                                this.mReevaluateDelayMs *= 2;
                                if (this.mReevaluateDelayMs > ProcessList.PSS_ALL_INTERVAL) {
                                    this.mReevaluateDelayMs = ProcessList.PSS_ALL_INTERVAL;
                                }
                            } else {
                                NetworkMonitor.this.sendMessageDelayed(msg, (long) NetworkMonitor.this.resetReevaluateDelayMs(this.mReevaluateDelayMs));
                                return NetworkMonitor.DBG;
                            }
                        }
                        return NetworkMonitor.DBG;
                    }
                    NetworkMonitor.this.validationLog("Network would not satisfy default request, not validating");
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                    return NetworkMonitor.DBG;
                case NetworkMonitor.CMD_FORCE_REEVALUATION /*532488*/:
                    if (this.mAttempts >= 5) {
                        z = false;
                    }
                    return z;
                default:
                    return false;
            }
        }

        public void exit() {
            TrafficStats.clearThreadStatsUid();
        }
    }

    private class LingeringState extends State {
        private static final String ACTION_LINGER_EXPIRED = "android.net.netmon.lingerExpired";
        private WakeupMessage mWakeupMessage;

        private LingeringState() {
        }

        public void enter() {
            NetworkMonitor.this.mEvaluationTimer.reset();
            this.mWakeupMessage = NetworkMonitor.this.makeWakeupMessage(NetworkMonitor.this.mContext, NetworkMonitor.this.getHandler(), "android.net.netmon.lingerExpired." + NetworkMonitor.this.mNetId, NetworkMonitor.CMD_LINGER_EXPIRED);
            this.mWakeupMessage.schedule(SystemClock.elapsedRealtime() + ((long) NetworkMonitor.this.mLingerDelayMs));
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case NetworkMonitor.CMD_NETWORK_CONNECTED /*532481*/:
                    NetworkMonitor.this.log("Unlingered");
                    if (!NetworkMonitor.this.mNetworkAgentInfo.lastValidated) {
                        return false;
                    }
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                    return NetworkMonitor.DBG;
                case NetworkMonitor.CMD_LINGER_EXPIRED /*532484*/:
                    NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_LINGER_COMPLETE, NetworkMonitor.this.mNetworkAgentInfo));
                    return NetworkMonitor.DBG;
                case NetworkMonitor.CMD_FORCE_REEVALUATION /*532488*/:
                    return NetworkMonitor.DBG;
                case NetworkMonitor.CMD_CAPTIVE_PORTAL_APP_FINISHED /*532489*/:
                    return NetworkMonitor.DBG;
                default:
                    return false;
            }
        }

        public void exit() {
            this.mWakeupMessage.cancel();
        }
    }

    private class MaybeNotifyState extends State {
        private MaybeNotifyState() {
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case NetworkMonitor.CMD_LAUNCH_CAPTIVE_PORTAL_APP /*532491*/:
                    try {
                        HwServiceFactory.getHwConnectivityManager().startBrowserOnClickNotification(NetworkMonitor.this.mContext, new URL(NetworkMonitor.getCaptivePortalServerUrl(NetworkMonitor.this.mContext)).toString());
                    } catch (MalformedURLException e) {
                        NetworkMonitor.this.log("MalformedURLException " + e);
                    }
                    return NetworkMonitor.DBG;
                default:
                    return false;
            }
        }

        public void exit() {
            NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_PROVISIONING_NOTIFICATION, 0, NetworkMonitor.this.mNetworkAgentInfo.network.netId, null));
        }
    }

    private class ValidatedState extends State {
        private ValidatedState() {
        }

        public void enter() {
            if (NetworkMonitor.this.mEvaluationTimer.isRunning()) {
                NetworkEvent.logValidated(NetworkMonitor.this.mNetId, NetworkMonitor.this.mEvaluationTimer.stop());
                NetworkMonitor.this.mEvaluationTimer.reset();
            }
            NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, 0, NetworkMonitor.this.mNetworkAgentInfo.network.netId, null));
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case NetworkMonitor.CMD_NETWORK_CONNECTED /*532481*/:
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                    return NetworkMonitor.DBG;
                default:
                    return false;
            }
        }
    }

    public NetworkMonitor(Context context, Handler handler, NetworkAgentInfo networkAgentInfo, NetworkRequest defaultRequest) {
        boolean z = DBG;
        super(TAG + networkAgentInfo.name());
        this.mContext = context;
        this.mConnectivityServiceHandler = handler;
        this.mNetworkAgentInfo = networkAgentInfo;
        this.mNetId = this.mNetworkAgentInfo.network.netId;
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mDefaultRequest = defaultRequest;
        addState(this.mDefaultState);
        addState(this.mValidatedState, this.mDefaultState);
        addState(this.mMaybeNotifyState, this.mDefaultState);
        addState(this.mEvaluatingState, this.mMaybeNotifyState);
        addState(this.mCaptivePortalState, this.mMaybeNotifyState);
        addState(this.mLingeringState, this.mDefaultState);
        setInitialState(this.mDefaultState);
        this.mLingerDelayMs = SystemProperties.getInt(LINGER_DELAY_PROPERTY, DEFAULT_LINGER_DELAY_MS);
        this.mIsCaptivePortalCheckEnabled = Global.getInt(this.mContext.getContentResolver(), "captive_portal_detection_enabled", 1) == 1 ? DBG : false;
        if (Global.getInt(this.mContext.getContentResolver(), "captive_portal_use_https", 1) != 1) {
            z = false;
        }
        this.mUseHttps = z;
        start();
    }

    protected void log(String s) {
        Log.d(TAG + BAKUP_SERV_PAGE + this.mNetworkAgentInfo.name(), s);
    }

    private void validationLog(String s) {
        log(s);
        this.validationLogs.log(s);
    }

    public ReadOnlyLocalLog getValidationLogs() {
        return this.validationLogs.readOnlyLocalLog();
    }

    private static String getCaptivePortalServerUrl(Context context, boolean isHttps) {
        String server = Global.getString(context.getContentResolver(), "captive_portal_server");
        if (server == null) {
            server = DEFAULT_SERVER;
        }
        return (isHttps ? "https" : "http") + "://" + server + DEFAULT_SERV_PAGE;
    }

    public static String getCaptivePortalServerUrl(Context context) {
        return getCaptivePortalServerUrl(context, false);
    }

    protected CaptivePortalProbeResult isCaptivePortal() {
        return isCaptivePortal(getCaptivePortalServerUrl(this.mContext));
    }

    protected CaptivePortalProbeResult isCaptivePortal(String server_url, String page) {
        if (!(!server_url.startsWith("http://") ? server_url.startsWith("https://") : DBG)) {
            server_url = "http://" + server_url;
        }
        if (!server_url.endsWith(page)) {
            server_url = server_url + page;
        }
        return isCaptivePortal(server_url);
    }

    protected CaptivePortalProbeResult isCaptivePortal(String urlString) {
        if (!this.mIsCaptivePortalCheckEnabled) {
            return new CaptivePortalProbeResult(204, null);
        }
        String hostToResolve;
        CaptivePortalProbeResult result;
        URL pacUrl = null;
        URL url = null;
        URL url2 = null;
        ProxyInfo proxyInfo = this.mNetworkAgentInfo.linkProperties.getHttpProxy();
        if (!(proxyInfo == null || Uri.EMPTY.equals(proxyInfo.getPacFileUrl()))) {
            try {
                URL url3 = new URL(proxyInfo.getPacFileUrl().toString());
            } catch (MalformedURLException e) {
                validationLog("Invalid PAC URL: " + proxyInfo.getPacFileUrl().toString());
                return CaptivePortalProbeResult.FAILED;
            }
        }
        if (pacUrl == null) {
            try {
                url3 = new URL(urlString);
                try {
                    url3 = new URL(urlString);
                    url = url3;
                } catch (MalformedURLException e2) {
                    url = url3;
                    validationLog("Bad validation URL: " + getCaptivePortalServerUrl(this.mContext, false));
                    return CaptivePortalProbeResult.FAILED;
                }
            } catch (MalformedURLException e3) {
                validationLog("Bad validation URL: " + getCaptivePortalServerUrl(this.mContext, false));
                return CaptivePortalProbeResult.FAILED;
            }
        }
        long startTime = SystemClock.elapsedRealtime();
        if (pacUrl != null) {
            hostToResolve = pacUrl.getHost();
        } else if (proxyInfo != null) {
            hostToResolve = proxyInfo.getHost();
        } else {
            hostToResolve = url.getHost();
        }
        if (!TextUtils.isEmpty(hostToResolve)) {
            String probeName = ValidationProbeEvent.getProbeName(0);
            Stopwatch dnsTimer = new Stopwatch().start();
            long dnsLatency;
            try {
                InetAddress[] addresses = this.mNetworkAgentInfo.network.getAllByName(hostToResolve);
                dnsLatency = dnsTimer.stop();
                ValidationProbeEvent.logEvent(this.mNetId, dnsLatency, 0, 1);
                StringBuffer connectInfo = new StringBuffer(", " + hostToResolve + "=");
                for (InetAddress address : addresses) {
                    connectInfo.append(address.getHostAddress());
                    if (address != addresses[addresses.length - 1]) {
                        connectInfo.append(",");
                    }
                }
                validationLog(probeName + " OK " + dnsLatency + "ms" + connectInfo);
            } catch (UnknownHostException e4) {
                dnsLatency = dnsTimer.stop();
                ValidationProbeEvent.logEvent(this.mNetId, dnsLatency, 0, 0);
                validationLog(probeName + " FAIL " + dnsLatency + "ms, " + hostToResolve);
            }
        }
        if (pacUrl != null) {
            result = sendHttpProbe(pacUrl, 3);
        } else if (this.mUseHttps) {
            result = sendParallelHttpProbes(url2, url);
        } else {
            result = sendHttpProbe(url, 1);
        }
        sendNetworkConditionsBroadcast(DBG, result.isPortal(), startTime, SystemClock.elapsedRealtime());
        return result;
    }

    protected CaptivePortalProbeResult sendHttpProbe(URL url, int probeType) {
        HttpURLConnection httpURLConnection = null;
        int httpResponseCode = 599;
        String redirectUrl = null;
        Stopwatch probeTimer = new Stopwatch().start();
        try {
            httpURLConnection = (HttpURLConnection) this.mNetworkAgentInfo.network.openConnection(url);
            httpURLConnection.setInstanceFollowRedirects(probeType == 3 ? DBG : false);
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setUseCaches(false);
            long requestTimestamp = SystemClock.elapsedRealtime();
            httpResponseCode = httpURLConnection.getResponseCode();
            redirectUrl = httpURLConnection.getHeaderField("location");
            long responseTimestamp = SystemClock.elapsedRealtime();
            this.mUrlHeadFieldLocation = httpURLConnection.getHeaderField(FlpHardwareProvider.LOCATION);
            validationLog(ValidationProbeEvent.getProbeName(probeType) + " " + url + " time=" + (responseTimestamp - requestTimestamp) + "ms" + " ret=" + httpResponseCode + " headers=" + httpURLConnection.getHeaderFields());
            if (httpResponseCode == 200 && httpURLConnection.getContentLength() == 0) {
                validationLog("Empty 200 response interpreted as 204 response.");
                httpResponseCode = 204;
            }
            if (httpResponseCode == 200 && probeType == 3) {
                validationLog("PAC fetch 200 response interpreted as 204 response.");
                httpResponseCode = 204;
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (IOException e) {
            validationLog("Probably not a portal: exception " + e);
            if (599 == 599) {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            } else if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (Throwable th) {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        ValidationProbeEvent.logEvent(this.mNetId, probeTimer.stop(), probeType, httpResponseCode);
        return new CaptivePortalProbeResult(httpResponseCode, redirectUrl);
    }

    private CaptivePortalProbeResult sendParallelHttpProbes(URL httpsUrl, URL httpUrl) {
        CountDownLatch latch = new CountDownLatch(2);
        AtomicReference<CaptivePortalProbeResult> finalResult = new AtomicReference();
        AnonymousClass1ProbeThread httpsProbe = new AnonymousClass1ProbeThread(DBG, httpsUrl, httpUrl, finalResult, latch);
        AnonymousClass1ProbeThread httpProbe = new AnonymousClass1ProbeThread(false, httpsUrl, httpUrl, finalResult, latch);
        httpsProbe.start();
        httpProbe.start();
        try {
            latch.await();
            finalResult.compareAndSet(null, httpsProbe.getResult());
            return (CaptivePortalProbeResult) finalResult.get();
        } catch (InterruptedException e) {
            validationLog("Error: probe wait interrupted!");
            return CaptivePortalProbeResult.FAILED;
        }
    }

    private void sendNetworkConditionsBroadcast(boolean responseReceived, boolean isCaptivePortal, long requestTimestampMs, long responseTimestampMs) {
        if (Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0) != 0 && this.systemReady) {
            Intent latencyBroadcast = new Intent(ACTION_NETWORK_CONDITIONS_MEASURED);
            switch (this.mNetworkAgentInfo.networkInfo.getType()) {
                case 0:
                    latencyBroadcast.putExtra(EXTRA_NETWORK_TYPE, this.mTelephonyManager.getNetworkType());
                    List<CellInfo> info = this.mTelephonyManager.getAllCellInfo();
                    if (info != null) {
                        int numRegisteredCellInfo = 0;
                        for (CellInfo cellInfo : info) {
                            if (cellInfo.isRegistered()) {
                                numRegisteredCellInfo++;
                                if (numRegisteredCellInfo > 1) {
                                    log("more than one registered CellInfo.  Can't tell which is active.  Bailing.");
                                    return;
                                } else if (cellInfo instanceof CellInfoCdma) {
                                    latencyBroadcast.putExtra(EXTRA_CELL_ID, ((CellInfoCdma) cellInfo).getCellIdentity());
                                } else if (cellInfo instanceof CellInfoGsm) {
                                    latencyBroadcast.putExtra(EXTRA_CELL_ID, ((CellInfoGsm) cellInfo).getCellIdentity());
                                } else if (cellInfo instanceof CellInfoLte) {
                                    latencyBroadcast.putExtra(EXTRA_CELL_ID, ((CellInfoLte) cellInfo).getCellIdentity());
                                } else if (cellInfo instanceof CellInfoWcdma) {
                                    latencyBroadcast.putExtra(EXTRA_CELL_ID, ((CellInfoWcdma) cellInfo).getCellIdentity());
                                } else {
                                    logw("Registered cellinfo is unrecognized");
                                    return;
                                }
                            }
                        }
                        break;
                    }
                    return;
                case 1:
                    WifiInfo currentWifiInfo = this.mWifiManager.getConnectionInfo();
                    if (currentWifiInfo != null) {
                        latencyBroadcast.putExtra(EXTRA_SSID, currentWifiInfo.getSSID());
                        latencyBroadcast.putExtra(EXTRA_BSSID, currentWifiInfo.getBSSID());
                        break;
                    }
                    logw("network info is TYPE_WIFI but no ConnectionInfo found");
                    return;
                default:
                    return;
            }
            latencyBroadcast.putExtra(EXTRA_CONNECTIVITY_TYPE, this.mNetworkAgentInfo.networkInfo.getType());
            latencyBroadcast.putExtra(EXTRA_RESPONSE_RECEIVED, responseReceived);
            latencyBroadcast.putExtra(EXTRA_REQUEST_TIMESTAMP_MS, requestTimestampMs);
            if (responseReceived) {
                latencyBroadcast.putExtra(EXTRA_IS_CAPTIVE_PORTAL, isCaptivePortal);
                latencyBroadcast.putExtra(EXTRA_RESPONSE_TIMESTAMP_MS, responseTimestampMs);
            }
            this.mContext.sendBroadcastAsUser(latencyBroadcast, UserHandle.CURRENT, PERMISSION_ACCESS_NETWORK_CONDITIONS);
        }
    }

    public static void SetDefaultLingerTime(int time_ms) {
        if (Process.myUid() == 1000) {
            throw new SecurityException("SetDefaultLingerTime only for internal testing.");
        }
        DEFAULT_LINGER_DELAY_MS = time_ms;
    }

    protected WakeupMessage makeWakeupMessage(Context c, Handler h, String s, int i) {
        return new WakeupMessage(c, h, s, i);
    }

    private String parseHostByLocation(String location) {
        if (location != null) {
            int start = 0;
            if (location.startsWith("http://")) {
                start = 7;
            } else if (location.startsWith("https://")) {
                start = 8;
            }
            int end = location.indexOf(BAKUP_SERV_PAGE, start);
            if (end == -1) {
                end = location.length();
            }
            if (start <= end && end <= location.length()) {
                return location.substring(start, end);
            }
        }
        return null;
    }
}
