package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings.Global;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.MathUtils;
import android.util.SparseBooleanArray;
import com.android.settingslib.net.DataUsageController;
import com.android.settingslib.net.DataUsageController.Callback;
import com.android.settingslib.net.DataUsageController.NetworkNameProvider;
import com.android.systemui.DemoMode;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.MobileSignalController.MobileState;
import com.android.systemui.statusbar.policy.NetworkController.AccessPointController;
import com.android.systemui.statusbar.policy.NetworkController.EmergencyListener;
import com.android.systemui.statusbar.policy.NetworkController.IconState;
import com.android.systemui.statusbar.policy.NetworkController.SignalCallback;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import fyusion.vislib.BuildConfig;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class NetworkControllerImpl extends BroadcastReceiver implements NetworkController, DemoMode, NetworkNameProvider, HwNetworkInterface {
    static final boolean CHATTY = Log.isLoggable("NetworkControllerChat", 3);
    static final boolean DEBUG = Log.isLoggable("NetworkController", 3);
    private final AccessPointControllerImpl mAccessPoints;
    protected boolean mAirplaneMode;
    protected SparseBooleanArray mCABeforeSimActive;
    protected final CallbackHandler mCallbackHandler;
    protected Config mConfig;
    private final BitSet mConnectedTransports;
    private final ConnectivityManager mConnectivityManager;
    protected final Context mContext;
    private List<SubscriptionInfo> mCurrentSubscriptions;
    private int mCurrentUserId;
    private final DataSaverController mDataSaverController;
    private final DataUsageController mDataUsageController;
    protected MobileSignalController mDefaultSignalController;
    private boolean mDemoInetCondition;
    private boolean mDemoMode;
    private WifiState mDemoWifiState;
    private int mEmergencySource;
    final EthernetSignalController mEthernetSignalController;
    private final boolean mHasMobileDataFeature;
    protected boolean mHasNoSims;
    private boolean mInetCondition;
    private boolean mIsEmergency;
    ServiceState mLastServiceState;
    boolean mListening;
    private Locale mLocale;
    protected final Map<Integer, MobileSignalController> mMobileSignalControllers;
    private final TelephonyManager mPhone;
    private final Handler mReceiverHandler;
    private final Runnable mRegisterListeners;
    private final SubscriptionDefaults mSubDefaults;
    private OnSubscriptionsChangedListener mSubscriptionListener;
    protected final SubscriptionManager mSubscriptionManager;
    private boolean mUserSetup;
    private final BitSet mValidatedTransports;
    private final WifiManager mWifiManager;
    final HwWifiSignalController mWifiSignalController;

    static class Config {
        boolean alwaysShowCdmaRssi = false;
        boolean hspaDataDistinguishable;
        boolean show4gForLte = false;
        boolean showAtLeast3G = false;

        Config() {
        }

        static Config readConfig(Context context) {
            Config config = new Config();
            Resources res = context.getResources();
            config.showAtLeast3G = res.getBoolean(R.bool.config_showMin3G);
            config.alwaysShowCdmaRssi = res.getBoolean(17956968);
            config.show4gForLte = res.getBoolean(R.bool.config_show4GForLTE);
            config.hspaDataDistinguishable = res.getBoolean(R.bool.config_hspa_data_distinguishable);
            return config;
        }
    }

    private class SubListener extends OnSubscriptionsChangedListener {
        private SubListener() {
        }

        public void onSubscriptionsChanged() {
            HwLog.i("NetworkController", "OnSubscriptionsChangedListener onSubscriptionsChanged()");
            NetworkControllerImpl.this.updateMobileControllers();
        }
    }

    public static class SubscriptionDefaults {
        public int getDefaultVoiceSubId() {
            return SubscriptionManager.getDefaultVoiceSubscriptionId();
        }

        public int getDefaultDataSubId() {
            return SubscriptionManager.getDefaultDataSubscriptionId();
        }
    }

    protected abstract void handleLteCaBroadcast(boolean z, MobileSignalController mobileSignalController, boolean z2, int i);

    public NetworkControllerImpl(Context context, Looper bgLooper) {
        this(context, (ConnectivityManager) context.getSystemService("connectivity"), (TelephonyManager) context.getSystemService("phone"), (WifiManager) context.getSystemService("wifi"), SubscriptionManager.from(context), Config.readConfig(context), bgLooper, new CallbackHandler(), new AccessPointControllerImpl(context, bgLooper), new DataUsageController(context), new SubscriptionDefaults());
        this.mReceiverHandler.post(this.mRegisterListeners);
    }

    NetworkControllerImpl(Context context, ConnectivityManager connectivityManager, TelephonyManager telephonyManager, WifiManager wifiManager, SubscriptionManager subManager, Config config, Looper bgLooper, CallbackHandler callbackHandler, AccessPointControllerImpl accessPointController, DataUsageController dataUsageController, SubscriptionDefaults defaultsHandler) {
        this.mMobileSignalControllers = new ConcurrentHashMap(new HashMap());
        this.mConnectedTransports = new BitSet();
        this.mValidatedTransports = new BitSet();
        this.mAirplaneMode = false;
        this.mLocale = null;
        this.mCurrentSubscriptions = new ArrayList();
        this.mCABeforeSimActive = new SparseBooleanArray();
        this.mRegisterListeners = new Runnable() {
            public void run() {
                NetworkControllerImpl.this.registerListeners();
            }
        };
        this.mContext = context;
        this.mConfig = config;
        this.mReceiverHandler = new Handler(bgLooper);
        this.mCallbackHandler = callbackHandler;
        this.mDataSaverController = new DataSaverController(context);
        this.mSubscriptionManager = subManager;
        this.mSubDefaults = defaultsHandler;
        this.mConnectivityManager = connectivityManager;
        this.mHasMobileDataFeature = this.mConnectivityManager.isNetworkSupported(0);
        this.mPhone = telephonyManager;
        this.mWifiManager = wifiManager;
        this.mLocale = this.mContext.getResources().getConfiguration().locale;
        this.mAccessPoints = accessPointController;
        this.mDataUsageController = dataUsageController;
        this.mDataUsageController.setNetworkController(this);
        this.mDataUsageController.setCallback(new Callback() {
            public void onMobileDataEnabled(boolean enabled) {
                NetworkControllerImpl.this.mCallbackHandler.setMobileDataEnabled(enabled);
            }
        });
        this.mWifiSignalController = new HwWifiSignalController(this.mContext, this.mHasMobileDataFeature, this.mCallbackHandler, this);
        this.mEthernetSignalController = new EthernetSignalController(this.mContext, this.mCallbackHandler, this);
        updateAirplaneMode(true);
    }

    public DataSaverController getDataSaverController() {
        return this.mDataSaverController;
    }

    private void registerListeners() {
        for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
            mobileSignalController.registerListener();
        }
        if (this.mSubscriptionListener == null) {
            this.mSubscriptionListener = new SubListener();
        }
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mSubscriptionListener);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.RSSI_CHANGED");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        filter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        filter.addAction("android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED");
        filter.addAction("android.intent.action.SERVICE_STATE");
        filter.addAction("android.provider.Telephony.SPN_STRINGS_UPDATED");
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.conn.INET_CONDITION_ACTION");
        filter.addAction("android.intent.action.AIRPLANE_MODE");
        registerHuawei(filter, this, this.mReceiverHandler);
        this.mContext.registerReceiver(this, filter, null, this.mReceiverHandler);
        this.mListening = true;
        updateMobileControllers();
    }

    private void unregisterListeners() {
        this.mListening = false;
        for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
            mobileSignalController.unregisterListener();
        }
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mSubscriptionListener);
        this.mContext.unregisterReceiver(this);
        unregisterHuawei();
    }

    public AccessPointController getAccessPointController() {
        return this.mAccessPoints;
    }

    public DataUsageController getMobileDataController() {
        return this.mDataUsageController;
    }

    public void addEmergencyListener(EmergencyListener listener) {
        this.mCallbackHandler.setListening(listener, true);
        this.mCallbackHandler.setEmergencyCallsOnly(isEmergencyOnly());
    }

    public void removeEmergencyListener(EmergencyListener listener) {
        this.mCallbackHandler.setListening(listener, false);
    }

    public boolean hasVoiceCallingFeature() {
        return this.mPhone.getPhoneType() != 0;
    }

    public MobileSignalController getDataController() {
        return getDataController(this.mSubDefaults.getDefaultDataSubId());
    }

    public MobileSignalController getDataController(int dataSubId) {
        dataSubId = getDefaultDataSubId(dataSubId);
        if (!SubscriptionManager.isValidSubscriptionId(dataSubId)) {
            if (DEBUG) {
                Log.e("NetworkController", "No data sim selected");
            }
            return this.mDefaultSignalController;
        } else if (this.mMobileSignalControllers.containsKey(Integer.valueOf(dataSubId))) {
            return (MobileSignalController) this.mMobileSignalControllers.get(Integer.valueOf(dataSubId));
        } else {
            if (DEBUG) {
                Log.e("NetworkController", "Cannot find controller for data sub: " + dataSubId);
            }
            return this.mDefaultSignalController;
        }
    }

    public WifiSignalController getWiFiController() {
        return this.mWifiSignalController;
    }

    public String getMobileDataNetworkName() {
        MobileSignalController controller = getDataController();
        return controller != null ? ((MobileState) controller.getState()).networkNameData : BuildConfig.FLAVOR;
    }

    public boolean isEmergencyOnly() {
        if (this.mMobileSignalControllers.size() == 0) {
            this.mEmergencySource = 0;
            return this.mLastServiceState != null ? this.mLastServiceState.isEmergencyOnly() : false;
        }
        int voiceSubId = this.mSubDefaults.getDefaultVoiceSubId();
        if (!SubscriptionManager.isValidSubscriptionId(voiceSubId)) {
            for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
                if (!((MobileState) mobileSignalController.getState()).isEmergency) {
                    this.mEmergencySource = mobileSignalController.mSubscriptionInfo.getSubscriptionId() + 100;
                    if (DEBUG) {
                        Log.d("NetworkController", "Found emergency " + mobileSignalController.mTag);
                    }
                    return false;
                }
            }
        }
        if (this.mMobileSignalControllers.containsKey(Integer.valueOf(voiceSubId))) {
            this.mEmergencySource = voiceSubId + 200;
            if (DEBUG) {
                Log.d("NetworkController", "Getting emergency from " + voiceSubId);
            }
            return ((MobileState) ((MobileSignalController) this.mMobileSignalControllers.get(Integer.valueOf(voiceSubId))).getState()).isEmergency;
        }
        if (DEBUG) {
            Log.e("NetworkController", "Cannot find controller for voice sub: " + voiceSubId);
        }
        this.mEmergencySource = voiceSubId + 300;
        return true;
    }

    void recalculateEmergency() {
        this.mIsEmergency = isEmergencyOnly();
        this.mCallbackHandler.setEmergencyCallsOnly(this.mIsEmergency);
    }

    public void addSignalCallback(SignalCallback cb) {
        cb.setSubs(this.mCurrentSubscriptions);
        cb.setIsAirplaneMode(new IconState(this.mAirplaneMode, getAirplaneIcon(R.drawable.stat_sys_airplane_mode), R.string.accessibility_airplane_mode, this.mContext));
        cb.setNoSims(this.mHasNoSims);
        this.mWifiSignalController.notifyListeners(cb);
        this.mEthernetSignalController.notifyListeners(cb);
        for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
            mobileSignalController.notifyListeners(cb);
        }
        this.mCallbackHandler.setListening(cb, true);
    }

    public void removeSignalCallback(SignalCallback cb) {
        this.mCallbackHandler.setListening(cb, false);
    }

    public void setWifiEnabled(final boolean enabled) {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... args) {
                int wifiApState = NetworkControllerImpl.this.mWifiManager.getWifiApState();
                if (enabled && (wifiApState == 12 || wifiApState == 13)) {
                    try {
                        NetworkControllerImpl.this.mWifiManager.setWifiApEnabled(null, false);
                    } catch (SecurityException e) {
                        Log.e("NetworkController", "user has be restricted to open WifiAp.");
                        return null;
                    }
                }
                NetworkControllerImpl.this.mWifiManager.setWifiEnabled(enabled);
                return null;
            }
        }.execute(new Void[0]);
    }

    public void onReceive(Context context, Intent intent) {
        if (CHATTY) {
            Log.d("NetworkController", "onReceive: intent=" + intent);
        }
        String action = intent.getAction();
        if (action.equals("android.net.conn.CONNECTIVITY_CHANGE") || action.equals("android.net.conn.INET_CONDITION_ACTION")) {
            updateConnectivity();
        } else if (action.equals("android.intent.action.AIRPLANE_MODE")) {
            refreshLocale();
            updateAirplaneMode(false);
        } else if (action.equals("android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED")) {
            recalculateEmergency();
        } else if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
            for (MobileSignalController controller : this.mMobileSignalControllers.values()) {
                controller.handleBroadcast(intent);
            }
        } else if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
            initLTEPlusState(intent);
            updateMobileControllers();
        } else if (action.equals("android.intent.action.SERVICE_STATE")) {
            this.mLastServiceState = ServiceState.newFromBundle(intent.getExtras());
            if (this.mMobileSignalControllers.size() == 0) {
                recalculateEmergency();
            }
        } else {
            int subId = intent.getIntExtra("subscription", -1);
            if (!SubscriptionManager.isValidSubscriptionId(subId)) {
                this.mWifiSignalController.handleBroadcast(intent);
            } else if (this.mMobileSignalControllers.containsKey(Integer.valueOf(subId))) {
                ((MobileSignalController) this.mMobileSignalControllers.get(Integer.valueOf(subId))).handleBroadcast(intent);
            } else {
                HwLog.i("NetworkController", "get a subId " + subId + " from receiver, but is not in show, so recreate it, act:" + action);
                updateMobileControllers();
            }
        }
        handleBroadcastHuawei(intent);
    }

    public void onConfigurationChanged() {
        this.mConfig = Config.readConfig(this.mContext);
        this.mReceiverHandler.post(new Runnable() {
            public void run() {
                NetworkControllerImpl.this.handleConfigurationChanged();
            }
        });
    }

    void handleConfigurationChanged() {
        for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
            mobileSignalController.setConfiguration(this.mConfig);
        }
        refreshLocale();
    }

    protected void updateMobileControllers() {
        HwLog.i("NetworkController", "updateMobileControllers() mListening:" + this.mListening);
        if (this.mListening) {
            doUpdateMobileControllers();
        }
    }

    void doUpdateMobileControllers() {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            List<SubscriptionInfo> subscriptions = null;

            public boolean runInThread() {
                this.subscriptions = NetworkControllerImpl.this.mSubscriptionManager.getActiveSubscriptionInfoList();
                HwLog.i("NetworkController", "updateMobileControllers() runInThread() subscriptions:" + (this.subscriptions == null ? "null" : " size:" + this.subscriptions.size()));
                return true;
            }

            public void runInUI() {
                if (this.subscriptions == null) {
                    this.subscriptions = Collections.emptyList();
                }
                this.subscriptions = NetworkControllerImpl.this.updateSubcriptions(this.subscriptions);
                if (NetworkControllerImpl.this.hasCorrectMobileControllers(this.subscriptions)) {
                    HwLog.i("NetworkController", "hasCorrectMobileControllers");
                    NetworkControllerImpl.this.updateNoSims();
                    return;
                }
                HwLog.i("NetworkController", "NOT hasCorrectMobileControllers, current size:" + this.subscriptions.size());
                NetworkControllerImpl.this.setCurrentSubscriptions(this.subscriptions);
                NetworkControllerImpl.this.updateNoSims();
                NetworkControllerImpl.this.recalculateEmergency();
            }
        });
    }

    protected void updateNoSims() {
        boolean hasNoSims = this.mHasMobileDataFeature && this.mMobileSignalControllers.size() == 0;
        HwLog.i("NetworkController", "mHasMobileDataFeature:" + this.mHasMobileDataFeature + " mMobileSignalControllers.size():" + this.mMobileSignalControllers.size() + " hasNoSims:" + hasNoSims + " mHasNoSims:" + this.mHasNoSims);
        if (hasNoSims != this.mHasNoSims) {
            this.mHasNoSims = hasNoSims;
            this.mCallbackHandler.setNoSims(this.mHasNoSims);
        }
    }

    void setCurrentSubscriptions(List<SubscriptionInfo> subscriptions) {
        Collections.sort(subscriptions, new Comparator<SubscriptionInfo>() {
            public int compare(SubscriptionInfo lhs, SubscriptionInfo rhs) {
                if (lhs.getSimSlotIndex() == rhs.getSimSlotIndex()) {
                    return lhs.getSubscriptionId() - rhs.getSubscriptionId();
                }
                return lhs.getSimSlotIndex() - rhs.getSimSlotIndex();
            }
        });
        subscriptions = sortSubsriptions(subscriptions);
        this.mCurrentSubscriptions = subscriptions;
        HashMap<Integer, MobileSignalController> cachedControllers = new HashMap(this.mMobileSignalControllers);
        this.mMobileSignalControllers.clear();
        int num = subscriptions.size();
        HwLog.i("NetworkController", "setCurrentSubscriptions subscriptions.size():" + num);
        for (int i = 0; i < num; i++) {
            int subId = ((SubscriptionInfo) subscriptions.get(i)).getSubscriptionId();
            if (cachedControllers.containsKey(Integer.valueOf(subId))) {
                this.mMobileSignalControllers.put(Integer.valueOf(subId), (MobileSignalController) cachedControllers.remove(Integer.valueOf(subId)));
            } else {
                MobileSignalController controller = new HwMobileSignalController(this.mContext, this.mConfig, this.mHasMobileDataFeature, this.mPhone, this.mCallbackHandler, this, (SubscriptionInfo) subscriptions.get(i), this.mSubDefaults, this.mReceiverHandler.getLooper());
                controller.setUserSetupComplete(this.mUserSetup);
                this.mMobileSignalControllers.put(Integer.valueOf(subId), controller);
                if (((SubscriptionInfo) subscriptions.get(i)).getSimSlotIndex() == 0) {
                    this.mDefaultSignalController = controller;
                }
                if (this.mListening) {
                    controller.registerListener();
                }
            }
            if (this.mCABeforeSimActive.get(subId, false)) {
                ((MobileSignalController) this.mMobileSignalControllers.get(Integer.valueOf(subId))).setCAState(true);
                handleLteCaBroadcast(true, (MobileSignalController) this.mMobileSignalControllers.get(Integer.valueOf(subId)), this.mConfig.show4gForLte, subId);
                this.mCABeforeSimActive.delete(subId);
            }
        }
        initMobileState();
        if (this.mListening) {
            for (Integer key : cachedControllers.keySet()) {
                if (cachedControllers.get(key) == this.mDefaultSignalController) {
                    this.mDefaultSignalController = null;
                }
                ((MobileSignalController) cachedControllers.get(key)).unregisterListener();
            }
        }
        this.mCallbackHandler.setSubs(subscriptions);
        notifyAllListeners();
        pushConnectivityToSignals();
        updateAirplaneMode(true);
    }

    public void setUserSetupComplete(final boolean userSetup) {
        this.mReceiverHandler.post(new Runnable() {
            public void run() {
                NetworkControllerImpl.this.handleSetUserSetupComplete(userSetup);
            }
        });
    }

    void handleSetUserSetupComplete(boolean userSetup) {
        this.mUserSetup = userSetup;
        for (MobileSignalController controller : this.mMobileSignalControllers.values()) {
            controller.setUserSetupComplete(this.mUserSetup);
        }
    }

    boolean hasCorrectMobileControllers(List<SubscriptionInfo> allSubscriptions) {
        if (allSubscriptions.size() != this.mMobileSignalControllers.size()) {
            return false;
        }
        for (SubscriptionInfo info : allSubscriptions) {
            if (!this.mMobileSignalControllers.containsKey(Integer.valueOf(info.getSubscriptionId()))) {
                return false;
            }
        }
        return true;
    }

    private void updateAirplaneMode(boolean force) {
        boolean airplaneMode = Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
        if (airplaneMode != this.mAirplaneMode || force) {
            this.mAirplaneMode = airplaneMode;
            for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
                mobileSignalController.setAirplaneMode(this.mAirplaneMode);
            }
            notifyListeners();
        }
    }

    private void refreshLocale() {
        Locale current = this.mContext.getResources().getConfiguration().locale;
        if (!current.equals(this.mLocale)) {
            this.mLocale = current;
            notifyAllListeners();
        }
    }

    private void notifyAllListeners() {
        notifyListeners();
        for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
            mobileSignalController.notifyListeners();
        }
        this.mWifiSignalController.notifyListeners();
        this.mEthernetSignalController.notifyListeners();
    }

    public void notifyListeners() {
        this.mCallbackHandler.setNoSims(this.mHasNoSims);
        this.mCallbackHandler.setIsAirplaneMode(new IconState(this.mAirplaneMode, getAirplaneIcon(R.drawable.stat_sys_airplane_mode), R.string.accessibility_airplane_mode, this.mContext));
    }

    private void updateConnectivity() {
        boolean z = false;
        this.mConnectedTransports.clear();
        this.mValidatedTransports.clear();
        for (NetworkCapabilities nc : this.mConnectivityManager.getDefaultNetworkCapabilitiesForUser(this.mCurrentUserId)) {
            if (nc != null) {
                for (int transportType : nc.getTransportTypes()) {
                    this.mConnectedTransports.set(transportType);
                    if (nc.hasCapability(16)) {
                        this.mValidatedTransports.set(transportType);
                    }
                }
            }
        }
        if (CHATTY) {
            Log.d("NetworkController", "updateConnectivity: mConnectedTransports=" + this.mConnectedTransports);
            Log.d("NetworkController", "updateConnectivity: mValidatedTransports=" + this.mValidatedTransports);
        }
        if (!this.mValidatedTransports.isEmpty()) {
            z = true;
        }
        this.mInetCondition = z;
        pushConnectivityToSignals();
    }

    private void pushConnectivityToSignals() {
        for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
            mobileSignalController.updateConnectivity(this.mConnectedTransports, this.mValidatedTransports);
        }
        this.mWifiSignalController.updateConnectivity(this.mConnectedTransports, this.mValidatedTransports);
        this.mEthernetSignalController.updateConnectivity(this.mConnectedTransports, this.mValidatedTransports);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("NetworkController state:");
        pw.println("  - telephony ------");
        pw.print("  hasVoiceCallingFeature()=");
        pw.println(hasVoiceCallingFeature());
        pw.println("  - connectivity ------");
        pw.print("  mConnectedTransports=");
        pw.println(this.mConnectedTransports);
        pw.print("  mValidatedTransports=");
        pw.println(this.mValidatedTransports);
        pw.print("  mInetCondition=");
        pw.println(this.mInetCondition);
        pw.print("  mAirplaneMode=");
        pw.println(this.mAirplaneMode);
        pw.print("  mLocale=");
        pw.println(this.mLocale);
        pw.print("  mLastServiceState=");
        pw.println(this.mLastServiceState);
        pw.print("  mIsEmergency=");
        pw.println(this.mIsEmergency);
        pw.print("  mEmergencySource=");
        pw.println(emergencyToString(this.mEmergencySource));
        for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
            mobileSignalController.dump(pw);
        }
        this.mWifiSignalController.dump(pw);
        this.mEthernetSignalController.dump(pw);
        this.mAccessPoints.dump(pw);
    }

    private static final String emergencyToString(int emergencySource) {
        if (emergencySource > 300) {
            return "NO_SUB(" + (emergencySource - 300) + ")";
        }
        if (emergencySource > 200) {
            return "VOICE_CONTROLLER(" + (emergencySource - 200) + ")";
        }
        if (emergencySource > 100) {
            return "FIRST_CONTROLLER(" + (emergencySource - 100) + ")";
        }
        if (emergencySource == 0) {
            return "NO_CONTROLLERS";
        }
        return "UNKNOWN_SOURCE";
    }

    public void dispatchDemoCommand(String command, Bundle args) {
        HwLog.d("NetworkController", "dispatchDemoCommand command:" + command);
        if (!this.mDemoMode && command.equals("enter")) {
            if (DEBUG) {
                Log.d("NetworkController", "Entering demo mode");
            }
            unregisterListeners();
            this.mDemoMode = true;
            this.mDemoInetCondition = this.mInetCondition;
            this.mDemoWifiState = (WifiState) this.mWifiSignalController.getState();
        } else if (this.mDemoMode && command.equals("exit")) {
            if (DEBUG) {
                Log.d("NetworkController", "Exiting demo mode");
            }
            this.mDemoMode = false;
            updateMobileControllers();
            for (MobileSignalController controller : this.mMobileSignalControllers.values()) {
                controller.resetLastState();
            }
            this.mWifiSignalController.resetLastState();
            this.mReceiverHandler.post(this.mRegisterListeners);
            notifyAllListeners();
        } else if (this.mDemoMode && command.equals("network")) {
            boolean show;
            String level;
            List<SubscriptionInfo> subs;
            String airplane = args.getString("airplane");
            if (airplane != null) {
                this.mCallbackHandler.setIsAirplaneMode(new IconState(airplane.equals("show"), getAirplaneIcon(R.drawable.stat_sys_airplane_mode), R.string.accessibility_airplane_mode, this.mContext));
            }
            String fully = args.getString("fully");
            if (fully != null) {
                this.mDemoInetCondition = Boolean.parseBoolean(fully);
                BitSet connected = new BitSet();
                if (this.mDemoInetCondition) {
                    connected.set(this.mWifiSignalController.mTransportType);
                }
                this.mWifiSignalController.updateConnectivity(connected, connected);
                for (MobileSignalController controller2 : this.mMobileSignalControllers.values()) {
                    if (this.mDemoInetCondition) {
                        connected.set(controller2.mTransportType);
                    }
                    controller2.updateConnectivity(connected, connected);
                }
            }
            String wifi = args.getString("wifi");
            if (wifi != null) {
                show = wifi.equals("show");
                level = args.getString("level");
                if (level != null) {
                    int i;
                    WifiState wifiState = this.mDemoWifiState;
                    if (level.equals("null")) {
                        i = -1;
                    } else {
                        i = Math.min(Integer.parseInt(level), WifiIcons.WIFI_LEVEL_COUNT - 1);
                    }
                    wifiState.level = i;
                    this.mDemoWifiState.connected = this.mDemoWifiState.level >= 0;
                }
                this.mDemoWifiState.enabled = show;
                this.mWifiSignalController.notifyListeners();
            }
            String sims = args.getString("sims");
            if (sims != null) {
                int num = MathUtils.constrain(Integer.parseInt(sims), 1, 8);
                subs = new ArrayList();
                if (num != this.mMobileSignalControllers.size()) {
                    this.mMobileSignalControllers.clear();
                    int start = this.mSubscriptionManager.getActiveSubscriptionInfoCountMax();
                    String slotStart = args.getString("slot_start");
                    if (!TextUtils.isEmpty(slotStart)) {
                        start = Integer.parseInt(slotStart);
                    }
                    for (int i2 = start; i2 < start + num; i2++) {
                        subs.add(addSignalController(i2, i2));
                    }
                    this.mCallbackHandler.setSubs(subs);
                }
            }
            String nosim = args.getString("nosim");
            if (nosim != null) {
                this.mHasNoSims = nosim.equals("show");
                this.mCallbackHandler.setNoSims(this.mHasNoSims);
            }
            String mobile = args.getString("mobile");
            if (mobile != null) {
                MobileState mobileState;
                show = mobile.equals("show");
                String datatype = args.getString("datatype");
                String slotString = args.getString("slot");
                int slot = MathUtils.constrain(TextUtils.isEmpty(slotString) ? 0 : Integer.parseInt(slotString), 0, 8);
                subs = new ArrayList();
                while (this.mMobileSignalControllers.size() <= slot) {
                    int nextSlot = this.mMobileSignalControllers.size();
                    subs.add(addSignalController(nextSlot, nextSlot));
                }
                if (!subs.isEmpty()) {
                    this.mCallbackHandler.setSubs(subs);
                }
                controller2 = ((MobileSignalController[]) this.mMobileSignalControllers.values().toArray(new MobileSignalController[0]))[slot];
                ((MobileState) controller2.getState()).dataSim = datatype != null;
                if (datatype != null) {
                    IconGroup iconGroup;
                    mobileState = (MobileState) controller2.getState();
                    if (datatype.equals("1x")) {
                        iconGroup = TelephonyIcons.ONE_X;
                    } else if (datatype.equals("3g")) {
                        iconGroup = TelephonyIcons.THREE_G;
                    } else if (datatype.equals("4g")) {
                        iconGroup = TelephonyIcons.FOUR_G;
                    } else if (datatype.equals("e")) {
                        iconGroup = TelephonyIcons.E;
                    } else if (datatype.equals("g")) {
                        iconGroup = TelephonyIcons.G;
                    } else if (datatype.equals("h")) {
                        iconGroup = TelephonyIcons.H;
                    } else if (datatype.equals("lte")) {
                        iconGroup = TelephonyIcons.LTE;
                    } else if (datatype.equals("roam")) {
                        iconGroup = TelephonyIcons.ROAMING;
                    } else {
                        iconGroup = TelephonyIcons.UNKNOWN;
                    }
                    mobileState.iconGroup = iconGroup;
                }
                int[][] icons = TelephonyIcons.TELEPHONY_SIGNAL_STRENGTH;
                level = args.getString("level");
                if (level != null) {
                    int i3;
                    mobileState = (MobileState) controller2.getState();
                    if (level.equals("null")) {
                        i3 = -1;
                    } else {
                        i3 = Math.min(Integer.parseInt(level), icons[0].length - 1);
                    }
                    mobileState.level = i3;
                    ((MobileState) controller2.getState()).connected = ((MobileState) controller2.getState()).level >= 0;
                }
                ((MobileState) controller2.getState()).enabled = show;
                controller2.notifyListeners();
            }
            String carrierNetworkChange = args.getString("carriernetworkchange");
            if (carrierNetworkChange != null) {
                show = carrierNetworkChange.equals("show");
                for (MobileSignalController controller22 : this.mMobileSignalControllers.values()) {
                    controller22.setCarrierNetworkChangeMode(show);
                }
            }
        }
    }

    private SubscriptionInfo addSignalController(int id, int simSlotIndex) {
        SubscriptionInfo info = new SubscriptionInfo(id, BuildConfig.FLAVOR, simSlotIndex, BuildConfig.FLAVOR, BuildConfig.FLAVOR, 0, 0, BuildConfig.FLAVOR, 0, null, 0, 0, BuildConfig.FLAVOR, 0);
        this.mMobileSignalControllers.put(Integer.valueOf(id), new HwMobileSignalController(this.mContext, this.mConfig, this.mHasMobileDataFeature, this.mPhone, this.mCallbackHandler, this, info, this.mSubDefaults, this.mReceiverHandler.getLooper()));
        return info;
    }
}
