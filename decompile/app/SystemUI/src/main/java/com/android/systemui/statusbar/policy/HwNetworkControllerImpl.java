package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.HwTelephonyManager;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.systemui.R;
import com.android.systemui.linkplus.RoamPlus;
import com.android.systemui.observer.SystemUIObserver;
import com.android.systemui.statusbar.policy.MobileSignalController.MobileState;
import com.android.systemui.traffic.TrafficPanelManager;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SimCardMethod;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.SystemUiUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class HwNetworkControllerImpl extends NetworkControllerImpl implements HwNetworkInterface {
    private static final boolean IS_4G_PLUS = SystemProperties.getBoolean("ro.config.hw_show_4G_Plus_icon", true);
    private static final boolean IS_DSDS_MODE = "dsds".equals(SystemProperties.get("persist.radio.multisim.config"));
    private static final String TAG = HwNetworkControllerImpl.class.getSimpleName();
    private static int USING_CA_SUB = -1;
    static final Object mLock = new Object();
    private int mCallSubscription = -1;
    private ContentObserver mDefaultSubscriptionObserver;
    private BroadcastReceiver mLocalIntentReceiver = new BroadcastReceiver() {
        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            if (!(intent == null || intent.getAction() == null || !intent.getAction().equals("com.android.systemui.signalclusterview.onAttach"))) {
                Log.d(HwNetworkControllerImpl.TAG, "Force SIGNALCLUSTERVIEW_ONATTATCH");
                for (MobileSignalController controller : HwNetworkControllerImpl.this.mMobileSignalControllers.values()) {
                    if (controller.mCurrentState instanceof MobileState) {
                        Log.d(HwNetworkControllerImpl.TAG, "notifyListenersForce");
                        controller.notifyListenersForce();
                    }
                }
            }
        }
    };
    private BroadcastReceiver mTJTReceiver = new BroadcastReceiver() {
        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            if (!(intent == null || intent.getAction() == null || !intent.getAction().equals("com.huawei.vsim.action.VSIM_CARD_RELOAD"))) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    int cardtype = bundle.getInt("vsim_cardtype", -1);
                    HwNetworkControllerImpl.this.setCardType(cardtype);
                    Log.d(HwNetworkControllerImpl.TAG, "vsimcardtype change  " + cardtype);
                }
            }
        }
    };
    private ContentObserver mVsimIdObserver;

    public HwNetworkControllerImpl(Context context, Looper bgLooper) {
        super(context, bgLooper);
        init();
    }

    private void init() {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                try {
                    NetWorkUtils.setVSimCurCardType(HwTelephonyManager.getDefault().getVSimCurCardType());
                    NetWorkUtils.setVSimSubId(HwNetworkControllerImpl.this.getVSimEnabledSubId());
                    NetWorkUtils.setDefaultSlot(HwNetworkControllerImpl.this.getUserDefaultSubscription());
                    HwNetworkControllerImpl.this.createObserver();
                    HwNetworkControllerImpl.this.registerObserver();
                } catch (SecurityException e) {
                    Log.w(HwNetworkControllerImpl.TAG, "init fail for HwNetworkController");
                }
                return false;
            }
        });
    }

    public void setVId(int vsimid) {
        NetWorkUtils.setVSimSubId(vsimid);
        updateMobileControllers();
    }

    public int getVSimEnabledSubId() {
        int enabledSubid = -1;
        try {
            enabledSubid = System.getInt(this.mContext.getContentResolver(), "vsim_enabled_subid");
        } catch (SettingNotFoundException e) {
            HwLog.e(TAG, "SettingNotFoundException");
        }
        return enabledSubid;
    }

    public int getUserDefaultSubscription() {
        int defaultslot = -1;
        try {
            defaultslot = System.getInt(this.mContext.getContentResolver(), "switch_dual_card_slots");
        } catch (SettingNotFoundException e) {
            HwLog.e(TAG, "SettingNotFoundException");
        }
        return defaultslot;
    }

    private void createObserver() {
        Handler handler = new Handler();
        this.mVsimIdObserver = new ContentObserver(handler) {
            public void onChange(boolean selfChange) {
                Log.d(HwNetworkControllerImpl.TAG, "mVsimIdObserver ");
                SystemUIThread.runAsync(new SimpleAsyncTask() {
                    int vsimid = -1;

                    public boolean runInThread() {
                        this.vsimid = HwNetworkControllerImpl.this.getVSimEnabledSubId();
                        HwNetworkControllerImpl.this.setVId(this.vsimid);
                        Log.d(HwNetworkControllerImpl.TAG, "VSimSubId changed  " + this.vsimid);
                        return false;
                    }
                });
            }
        };
        this.mDefaultSubscriptionObserver = new ContentObserver(handler) {
            public void onChange(boolean selfChange) {
                Log.d(HwNetworkControllerImpl.TAG, "mDefaultSubscriptionObserver ");
                SystemUIThread.runAsync(new SimpleAsyncTask() {
                    int defaultslot = -1;

                    public boolean runInThread() {
                        this.defaultslot = HwNetworkControllerImpl.this.getUserDefaultSubscription();
                        NetWorkUtils.setDefaultSlot(this.defaultslot);
                        Log.d(HwNetworkControllerImpl.TAG, "mDefaultSubscription changed  " + this.defaultslot);
                        return false;
                    }
                });
            }
        };
    }

    private void registerObserver() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("vsim_enabled_subid"), true, this.mVsimIdObserver);
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("switch_dual_card_slots"), true, this.mDefaultSubscriptionObserver);
    }

    public int getAirplaneIcon(int defaultIcon) {
        return R.drawable.stat_sys_signal_flightmode;
    }

    public void updateSimUpdate(Intent intent, boolean hasNoSims) {
        TrafficPanelManager.getInstance().adjustTrafficMeal();
    }

    public void registerHuawei(IntentFilter filter, BroadcastReceiver broadcastReceiver, Handler handler) {
        if (SystemUiUtil.isSupportVSim()) {
            filter.addAction("com.huawei.vsim.action.HW_WIFI_ICON_ENTER_ACTION");
            filter.addAction("com.huawei.vsim.action.HW_WIFI_ICON_EXIT_ACTION");
            filter.addAction("android.provider.Telephony.SPN_STRINGS_UPDATED_VSIM");
        }
        if (SystemUiUtil.isMulityCard(this.mContext)) {
            updateCardsState();
        }
        if (IS_4G_PLUS) {
            filter.addAction("android.intent.action.LTE_CA_STATE");
        }
        filter.addAction("com.android.huawei.DATASERVICE_SETTING_CHANGED");
        filter.addAction("android.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT");
        filter.addAction("com.android.systemui.huawei.sim_inactive_action");
        IntentFilter noInternetWifiIntentFilter = new IntentFilter();
        noInternetWifiIntentFilter.addAction("huawei.wifi.pro.INTERNET_ACCESS_CHANGE");
        this.mContext.registerReceiver(broadcastReceiver, noInternetWifiIntentFilter, "huawei.permission.RECEIVE_WIFI_PRO_STATE", handler);
        registerTJTReceiver();
        IntentFilter onAttatchIntentFilter = new IntentFilter();
        onAttatchIntentFilter.addAction("com.android.systemui.signalclusterview.onAttach");
        this.mContext.registerReceiver(this.mLocalIntentReceiver, onAttatchIntentFilter);
    }

    private void registerTJTReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huawei.vsim.action.VSIM_CARD_RELOAD");
        this.mContext.registerReceiver(this.mTJTReceiver, filter, "com.huawei.skytone.permission.VSIM_BUSSINESS", null);
    }

    private void setCardType(int cardtype) {
        NetWorkUtils.setVSimCurCardType(cardtype);
    }

    public static int getLteCASub() {
        int i;
        synchronized (mLock) {
            i = USING_CA_SUB;
        }
        return i;
    }

    public static void sendSimInactiveBroadcast(final Context context) {
        if (context == null) {
            HwLog.e(TAG, "sendSimInactiveBroadcast context == null, return");
        } else {
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                public boolean runInThread() {
                    HwLog.i(HwNetworkControllerImpl.TAG, "sendSimInactiveBroadcast");
                    Intent intent = new Intent("com.android.systemui.huawei.sim_inactive_action");
                    intent.setPackage(context.getPackageName());
                    context.sendBroadcast(intent);
                    return false;
                }
            });
        }
    }

    private void handleInactiveBroadcast(final SubscriptionManager subscriptionManager) {
        if (subscriptionManager == null || this.mCallbackHandler == null) {
            HwLog.e(TAG, "subscriptionManager == null || mCallbackHandler == null, return!");
        } else if (SystemUiUtil.isChinaTelecomArea()) {
            HwLog.i(TAG, "do not handle ACTION_SUBINFO_CONTENT_CHANGE in CL phone, return ");
        } else {
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                int sub1State;
                int sub2State;

                public boolean runInThread() {
                    boolean airplaneMode;
                    if (Global.getInt(HwNetworkControllerImpl.this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1) {
                        airplaneMode = true;
                    } else {
                        airplaneMode = false;
                    }
                    if (airplaneMode) {
                        HwLog.i(HwNetworkControllerImpl.TAG, "current is airplaneMode, and return");
                        return false;
                    } else if (subscriptionManager != null) {
                        List<SubscriptionInfo> subscriptionInfos = subscriptionManager.getActiveSubscriptionInfoList();
                        if (subscriptionInfos == null || subscriptionInfos.size() != 2) {
                            HwLog.i(HwNetworkControllerImpl.TAG, " ACTION_SUBSCRIPTION_SET_UICC_RESULT, subscriptionInfos == null or subscriptionInfos size is not 2");
                            return false;
                        }
                        HwLog.i(HwNetworkControllerImpl.TAG, "ACTION_SUBSCRIPTION_SET_UICC_RESULT ,two cards are active");
                        this.sub1State = HwTelephonyManager.getDefault().getSubState(0);
                        this.sub2State = HwTelephonyManager.getDefault().getSubState(1);
                        return true;
                    } else {
                        HwLog.i(HwNetworkControllerImpl.TAG, "ACTION_SUBSCRIPTION_SET_UICC_RESULT , mSubscriptionManager == null, unnormal");
                        return false;
                    }
                }

                public void runInUI() {
                    HwLog.i(HwNetworkControllerImpl.TAG, "ACTION_SUBSCRIPTION_SET_UICC_RESULT sub1State:" + this.sub1State + " sub2State:" + this.sub2State);
                    HwNetworkControllerImpl.this.mCallbackHandler.updateSubs(this.sub1State, this.sub2State);
                }
            });
        }
    }

    public static void setLteCASub(int sub) {
        synchronized (mLock) {
            USING_CA_SUB = sub;
        }
    }

    public void handleLteCaBroadcast(boolean isCAstate, MobileSignalController controller, boolean show4gForlte, int sub) {
        if (controller != null && controller.mNetworkToIconLookup != null) {
            if (isCAstate) {
                setLteCASub(sub);
                controller.mNetworkToIconLookup.put(13, HwTelephonyIcons.FORGPLUS);
            } else {
                setLteCASub(-1);
                if (show4gForlte) {
                    controller.mNetworkToIconLookup.put(13, TelephonyIcons.FOUR_G);
                } else {
                    controller.mNetworkToIconLookup.put(13, TelephonyIcons.LTE);
                }
            }
            controller.updateTelephony(true);
        }
    }

    public void unregisterHuawei() {
        if (SystemUiUtil.isSupportVSim()) {
            if (this.mTJTReceiver != null) {
                this.mContext.unregisterReceiver(this.mTJTReceiver);
                this.mTJTReceiver = null;
            }
            if (this.mVsimIdObserver != null) {
                this.mContext.getContentResolver().unregisterContentObserver(this.mVsimIdObserver);
                this.mVsimIdObserver = null;
            }
            if (this.mDefaultSubscriptionObserver != null) {
                this.mContext.getContentResolver().unregisterContentObserver(this.mDefaultSubscriptionObserver);
                this.mDefaultSubscriptionObserver = null;
            }
        }
    }

    public List<SubscriptionInfo> updateSubcriptions(List<SubscriptionInfo> subscriptions) {
        if (!SystemUiUtil.isSupportVSim()) {
            return subscriptions;
        }
        if (subscriptions == null || subscriptions.size() == 0) {
            subscriptions = new ArrayList();
        }
        Log.d(TAG, "vsim SUB number: " + subscriptions.size());
        int vimSubid = NetWorkUtils.getVSimSubId();
        Log.d(TAG, "vsim updateMobileControllers vimSubid:  " + vimSubid);
        if (vimSubid != -1) {
            List<SubscriptionInfo> list = subscriptions;
            list.add(new SubscriptionInfo(vimSubid, "vimSub", vimSubid, null, null, -1, -1, "2", -1, null, -1, -1, "-1", -1));
        }
        Log.d(TAG, "vsim SUB number +1: " + subscriptions.size());
        return subscriptions;
    }

    public List<SubscriptionInfo> sortSubsriptions(List<SubscriptionInfo> subscriptions) {
        if (subscriptions == null) {
            return new ArrayList();
        }
        if (!SystemUiUtil.isSupportVSim()) {
            return subscriptions;
        }
        int vimSub = NetWorkUtils.getVSimSubId();
        Log.d(TAG, "vimSub:" + vimSub);
        Iterator<SubscriptionInfo> it = subscriptions.iterator();
        while (it.hasNext()) {
            SubscriptionInfo sub = (SubscriptionInfo) it.next();
            if (sub.getSubscriptionId() == vimSub) {
                Log.d(TAG, "getSubscriptionId:" + sub.getSubscriptionId() + ", vimSub" + vimSub);
                SubscriptionInfo tempSub = sub;
                it.remove();
                subscriptions.add(0, sub);
                break;
            }
        }
        return subscriptions;
    }

    public void initMobileState() {
        for (Entry<Integer, MobileSignalController> mapEntry : this.mMobileSignalControllers.entrySet()) {
            MobileSignalController c = (MobileSignalController) mapEntry.getValue();
            if (!(c == null || c.mLastState == null)) {
                ((MobileState) c.mLastState).copyFrom(c.cleanState());
            }
        }
    }

    public int getDefaultDataSubId(int sub) {
        if (!SystemUiUtil.isSupportVSim()) {
            return sub;
        }
        int vimSubid = NetWorkUtils.getVSimSubId();
        if (vimSubid != -1) {
            return vimSubid;
        }
        return sub;
    }

    public MobileSignalController getControllerBySubId(int subId) {
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            if (DEBUG) {
                Log.e(TAG, "No data sim selected");
            }
            return this.mDefaultSignalController;
        } else if (this.mMobileSignalControllers.containsKey(Integer.valueOf(subId))) {
            return (MobileSignalController) this.mMobileSignalControllers.get(Integer.valueOf(subId));
        } else {
            if (DEBUG) {
                Log.e(TAG, "Cannot find controller for sub: " + subId);
            }
            return this.mDefaultSignalController;
        }
    }

    private void updateCardsState() {
        if (!((Boolean) SystemUIObserver.get(2)).booleanValue()) {
            TelephonyManager mTelphony = TelephonyManager.from(this.mContext);
            boolean isCard1Present = SimCardMethod.isCardPresent(mTelphony, 0);
            boolean isCard2Present = SimCardMethod.isCardPresent(mTelphony, 1);
            if (!isCard1Present || !isCard2Present) {
                notifyListeners();
            }
        }
    }

    public void updateOtherSubState(int simSubId, int state) {
        if (IS_DSDS_MODE) {
            boolean isNotifyListeners = false;
            boolean isRefresh = false;
            if (state != 0 && this.mCallSubscription == -1) {
                this.mCallSubscription = simSubId;
                isNotifyListeners = true;
            } else if (state == 0 && this.mCallSubscription == simSubId) {
                this.mCallSubscription = -1;
                isRefresh = true;
            }
            HwLog.i(TAG, "updateOtherSubState() simSubId=" + simSubId + " isNotifyListeners=" + isNotifyListeners + " isRefresh=" + isRefresh);
            if (isNotifyListeners || isRefresh) {
                for (MobileSignalController mobileSignalController : this.mMobileSignalControllers.values()) {
                    int subId = mobileSignalController.mSubscriptionInfo.getSubscriptionId();
                    if (!(subId == simSubId || subId == NetWorkUtils.getVSimSubId())) {
                        if (isRefresh) {
                            mobileSignalController.mPhoneStateListener.onServiceStateChanged(mobileSignalController.mServiceStateLast);
                            mobileSignalController.mPhoneStateListener.onSignalStrengthsChanged(mobileSignalController.mSignalStrengthLast);
                        }
                        mobileSignalController.notifyListeners();
                    }
                }
            }
        }
    }

    public boolean isSuspend(int simSubId, boolean airplaneMode, ServiceState serviceState) {
        if (!IS_DSDS_MODE || airplaneMode || !isValidCallSub() || this.mCallSubscription == simSubId || simSubId == NetWorkUtils.getVSimSubId() || serviceState == null || (serviceState.getState() != 0 && serviceState.getDataRegState() != 0)) {
            return false;
        }
        return true;
    }

    private boolean isValidCallSub() {
        if (this.mCallSubscription != -1) {
            if (this.mMobileSignalControllers.size() > 1) {
                return true;
            }
            HwLog.e(TAG, "isValidCallSub() reset mCallSubscription");
            this.mCallSubscription = -1;
        }
        return false;
    }

    public void initLTEPlusState(Intent intent) {
        if (intent != null && getLteCASub() == intent.getIntExtra("subscription", 0) && "ABSENT".equals(intent.getStringExtra("ss"))) {
            setLteCASub(-1);
        }
    }

    public void handleBroadcastHuawei(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            HwLog.e(TAG, "handleBroadcastHuawei::intent or action is null!");
            return;
        }
        String action = intent.getAction();
        HwLog.i(TAG, "onReceive: action = " + action);
        if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
            if (!this.mAirplaneMode) {
                RoamPlus.resetAlreadyShowRoaming(this.mContext);
            }
            handleInactiveBroadcast(this.mSubscriptionManager);
        } else if ("com.android.huawei.DATASERVICE_SETTING_CHANGED".equals(action)) {
            for (MobileSignalController controller : this.mMobileSignalControllers.values()) {
                boolean isDataEnable = intent.getBooleanExtra("dataEnable", true);
                HwLog.d(TAG, "isDataEnable:" + isDataEnable + " subId:" + controller.mSubscriptionInfo.getSubscriptionId());
                if ((controller.mCurrentState instanceof MobileState) && ((MobileState) controller.mCurrentState).dataSim && !isDataEnable) {
                    ((MobileState) controller.mCurrentState).dataConnected = isDataEnable;
                    controller.notifyListenersIfNecessary();
                }
            }
        } else if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
            updateSimUpdate(intent, this.mHasNoSims);
        } else if ("android.intent.action.LTE_CA_STATE".equals(action)) {
            boolean isCAstate = intent.getBooleanExtra("LteCAstate", false);
            int subId = intent.getIntExtra("subscription", -1);
            if (SubscriptionManager.isValidSubscriptionId(subId)) {
                controller = (MobileSignalController) this.mMobileSignalControllers.get(Integer.valueOf(subId));
                if (controller != null) {
                    controller.setCAState(isCAstate);
                } else {
                    this.mCABeforeSimActive.put(subId, isCAstate);
                }
                handleLteCaBroadcast(isCAstate, controller, this.mConfig.show4gForLte, subId);
            }
        } else if ("android.intent.action.ACTION_SUBSCRIPTION_SET_UICC_RESULT".equals(action) || "com.android.systemui.huawei.sim_inactive_action".equals(action)) {
            handleInactiveBroadcast(this.mSubscriptionManager);
        } else if (!SubscriptionManager.isValidSubscriptionId(intent.getIntExtra("subscription", -1))) {
            this.mWifiSignalController.updateWifiCloudState(intent);
            this.mWifiSignalController.notifyListenersIfNecessary();
        }
    }
}
