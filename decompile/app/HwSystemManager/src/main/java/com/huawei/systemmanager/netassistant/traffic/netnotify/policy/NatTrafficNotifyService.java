package com.huawei.systemmanager.netassistant.traffic.netnotify.policy;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import com.google.android.collect.Lists;
import com.huawei.netassistant.analyse.TrafficNotifyOverMarkForDay;
import com.huawei.netassistant.calculator.CalculateTrafficManager;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.netassistant.db.NetAssistantStore.SettingTable;
import com.huawei.netassistant.db.NetAssistantStore.TrafficAdjustTable;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.systemmanager.comm.concurrent.HsmExecutor;
import com.huawei.systemmanager.netassistant.traffic.netnotify.INatNetworkPolicyBinder;
import com.huawei.systemmanager.netassistant.traffic.netnotify.ITrafficChangeListener;
import com.huawei.systemmanager.netassistant.traffic.netnotify.policy.INatTrafficNotifyBinder.Stub;
import com.huawei.systemmanager.netassistant.traffic.notrafficapp.NoTrafficAppDbInfo;
import com.huawei.systemmanager.netassistant.traffic.trafficstatistics.ITrafficInfo;
import com.huawei.systemmanager.netassistant.traffic.trafficstatistics.TrafficState;
import com.huawei.systemmanager.sdk.tmsdk.TMSdkEngine;
import com.huawei.systemmanager.service.MainService.HsmService;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

@TargetApi(22)
public class NatTrafficNotifyService extends Stub implements HsmService {
    private static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    private static final int MSG_NETWROK_STATS_UPDATED = 2;
    private static final int MSG_NO_TRAFFIC_DB = 3;
    private static final int MSG_SETTINGS_DB_CHANGE = 1;
    private static final int MSG_SIM_STATS_CHANGE = 4;
    public static final String NAT_TRAFFIC_NOTIFY = "com.huawei.systemmanager.netassistant.netnotify.policy.NatTrafficNotifyService";
    public static final String TAG = NatTrafficNotifyService.class.getSimpleName();
    private static final String VSIM_EANBLED_SUBID = "vsim_enabled_subid";
    @FindBugsSuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
    private static BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            HwLog.i(NatTrafficNotifyService.TAG, "new sub = " + intent.getIntExtra("subscription", -1));
            NormalTrafficAnalysis.getDefault().onSubscriptionChanged();
            NoAppTrafficAnalysis.getDefault().onSubscriptionChanged();
            HwLog.i(NatTrafficNotifyService.TAG, "default mobile sub change ");
        }
    };
    private Context mContext;
    private TrafficNotifyOverMarkForDay mExcessForAll;
    private final Handler mHandler;
    private Callback mHandlerCallback = new Callback() {
        public boolean handleMessage(Message msg) {
            String imsi = SimCardManager.getInstance().getPreferredDataSubscriberId();
            switch (msg.what) {
                case 1:
                    HwLog.d(NatTrafficNotifyService.TAG, "call back MSG_SETTINGS_DB_CHANGE");
                    CalculateTrafficManager.getInstance().getStatsSession().forceUpdate();
                    return true;
                case 2:
                    HwLog.d(NatTrafficNotifyService.TAG, "call back MSG_NETWROK_STATS_UPDATED");
                    if (NatTrafficNotifyService.this.misVSimEnable) {
                        HwLog.e(NatTrafficNotifyService.TAG, "VSim is open, traffic should not be record");
                        return true;
                    } else if (imsi == null) {
                        HwLog.e(NatTrafficNotifyService.TAG, "imsi is null, so return");
                        return true;
                    } else {
                        NatTrafficNotifyService.this.mExcessForAll.notifyTraffic();
                        long deltaByte = NormalTrafficAnalysis.getDefault().getDeltaTraffic();
                        long noTrafficDelta = NoAppTrafficAnalysis.getDefault().getDeltaTraffic();
                        int state = TrafficState.getCurrentTrafficState(imsi);
                        if (state != 303) {
                            deltaByte -= noTrafficDelta;
                        }
                        if (deltaByte < 0) {
                            HwLog.e(NatTrafficNotifyService.TAG, "deltaByte < 0, so return");
                            return true;
                        }
                        int yearMonth = DateUtil.getYearMonth(imsi);
                        HwLog.i(NatTrafficNotifyService.TAG, "delta byte = " + deltaByte + " no traffic delta = " + noTrafficDelta + "Traffic state is " + state);
                        ITrafficInfo.create(imsi, yearMonth, state).updateBytes(deltaByte);
                        return true;
                    }
                case 3:
                    HwLog.d(NatTrafficNotifyService.TAG, "MSG_NO_TRAFFIC_DB");
                    NoAppTrafficAnalysis.getDefault().onUidListChanged();
                    return true;
                case 4:
                    HwLog.d(NatTrafficNotifyService.TAG, "call back MSG_SIM_STATS_CHANGE");
                    TMSdkEngine.imsiChanged();
                    return true;
                default:
                    return false;
            }
        }
    };
    private List<INatTrafficNotifyListener> mListenerList;
    private INatNetworkPolicyBinder mNatNetworkPolicyService;
    private ContentObserver mSettingsDbObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange);
            if (uri == null) {
                HwLog.e(NatTrafficNotifyService.TAG, "onChange with null uri");
                return;
            }
            HwLog.d(NatTrafficNotifyService.TAG, "ContentObserver onChange, update settings config");
            if (uri.compareTo(NoTrafficAppDbInfo.URI) == 0) {
                NatTrafficNotifyService.this.mHandler.removeMessages(3);
                NatTrafficNotifyService.this.mHandler.sendEmptyMessage(3);
            } else if (uri.compareTo(System.getUriFor(NatTrafficNotifyService.VSIM_EANBLED_SUBID)) == 0) {
                HwLog.e(NatTrafficNotifyService.TAG, "VSim Changed");
                NatTrafficNotifyService.this.misVSimEnable = NatTrafficNotifyService.this.isVSimEnable();
            } else {
                NatTrafficNotifyService.this.mHandler.removeMessages(1);
                NatTrafficNotifyService.this.mHandler.sendEmptyMessage(1);
            }
        }
    };
    private BroadcastReceiver mSimStateChanageReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            HwLog.i(NatTrafficNotifyService.TAG, "Sim state chanage.");
            if (intent == null) {
                HwLog.w(NatTrafficNotifyService.TAG, "Sim state chanage intent null.");
            } else if (NatTrafficNotifyService.ACTION_SIM_STATE_CHANGED.equals(intent.getAction())) {
                try {
                    int simState = ((TelephonyManager) NatTrafficNotifyService.this.mContext.getSystemService("phone")).getSimState();
                    HwLog.i(NatTrafficNotifyService.TAG, "Sim state chanage ACTION_SIM_STATE_CHANGED. simState = " + simState);
                    if (simState == 5) {
                        NatTrafficNotifyService.this.mHandler.removeMessages(4);
                        NatTrafficNotifyService.this.mHandler.sendEmptyMessage(4);
                    }
                } catch (Exception e) {
                    HwLog.e(NatTrafficNotifyService.TAG, "Sim state chanage ,get simState fail, e = " + e.getMessage());
                }
            } else {
                HwLog.w(NatTrafficNotifyService.TAG, "Sim state chanage ,intent action is not ACTION_SIM_STATE_CHANGED.");
            }
        }
    };
    @FindBugsSuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
    OnSubscriptionsChangedListener mSubscriptionChangeListener = new OnSubscriptionsChangedListener() {
        public void onSubscriptionsChanged() {
            super.onSubscriptionsChanged();
            HwLog.i(NatTrafficNotifyService.TAG, "onUidListChanged");
            NoAppTrafficAnalysis.getDefault().onUidListChanged();
        }
    };
    private SubscriptionManager mSubscriptionManager;
    private boolean misVSimEnable;
    private ITrafficChangeListener trafficChangeListener = new ITrafficChangeListener() {
        public void onLimitReached(String iface) throws RemoteException {
        }

        public void onNetworkStatsUpdated() throws RemoteException {
            HwLog.d(NatTrafficNotifyService.TAG, "onNetworkStatsUpdated");
            NatTrafficNotifyService.this.mHandler.removeMessages(2);
            NatTrafficNotifyService.this.mHandler.sendEmptyMessage(2);
        }

        public IBinder asBinder() {
            return null;
        }
    };

    public NatTrafficNotifyService(Context context, INatNetworkPolicyBinder networkPolicyBinder) {
        this.mContext = context;
        this.mListenerList = Lists.newArrayList();
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new Handler(thread.getLooper(), this.mHandlerCallback);
        this.mNatNetworkPolicyService = networkPolicyBinder;
        postRunnableAsync(new Runnable() {
            public void run() {
                NatTrafficNotifyService.this.initTrafficAnaysis();
            }
        });
    }

    private void initTrafficAnaysis() {
        NormalTrafficAnalysis.getDefault().init();
        NoAppTrafficAnalysis.getDefault().init();
    }

    public void registerTrafficNotifyListener(INatTrafficNotifyListener listener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        this.mListenerList.add(listener);
    }

    public void unRegisterTrafficNotifyListener(INatTrafficNotifyListener listener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        this.mListenerList.remove(listener);
    }

    public void init() {
        analyseExcessCircularly();
        registerObserver();
        registerReceiver();
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        this.mContext.registerReceiver(mReceiver, intentFilter);
        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SIM_STATE_CHANGED);
        this.mContext.registerReceiver(this.mSimStateChanageReceiver, intentFilter);
    }

    private void unRegisterReceiver() {
        this.mContext.unregisterReceiver(mReceiver);
        this.mContext.unregisterReceiver(this.mSimStateChanageReceiver);
    }

    public void onDestroy() {
        unRegisterObserver();
        unRegisterReceiver();
    }

    public void onConfigurationChange(Configuration newConfig) {
    }

    public void onStartCommand(Intent intent, int flags, int startId) {
    }

    private void registerObserver() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        if (contentResolver == null) {
            HwLog.e(TAG, "registerDBObserver failed: contentResolver is null!");
            return;
        }
        contentResolver.registerContentObserver(SettingTable.getContentUri(), true, this.mSettingsDbObserver);
        contentResolver.registerContentObserver(TrafficAdjustTable.getContentUri(), true, this.mSettingsDbObserver);
        contentResolver.registerContentObserver(NoTrafficAppDbInfo.URI, true, this.mSettingsDbObserver);
        this.misVSimEnable = isVSimEnable();
        contentResolver.registerContentObserver(System.getUriFor(VSIM_EANBLED_SUBID), false, this.mSettingsDbObserver);
        try {
            this.mNatNetworkPolicyService.registerTrafficChangeListener(this.trafficChangeListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.mSubscriptionManager = (SubscriptionManager) this.mContext.getSystemService("telephony_subscription_service");
        if (this.mSubscriptionManager != null) {
            this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mSubscriptionChangeListener);
        }
    }

    private void unRegisterObserver() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        if (contentResolver == null) {
            HwLog.e(TAG, "registerDBObserver failed: contentResolver is null!");
            return;
        }
        contentResolver.unregisterContentObserver(this.mSettingsDbObserver);
        try {
            this.mNatNetworkPolicyService.unRegisterTrafficChangeListener(this.trafficChangeListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void analyseExcessCircularly() {
        this.mExcessForAll = new TrafficNotifyOverMarkForDay();
    }

    public void notifyDailyWarn(String imsi, String iface, long bytes) throws RemoteException {
        HwLog.d(TAG, "notifyDailyWarn iface = " + iface + " bytes = " + bytes);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        for (INatTrafficNotifyListener listener : this.mListenerList) {
            listener.onDailyWarningReached(imsi, iface, bytes);
        }
    }

    public void notifyMonthLimit(String imsi, String iface, long bytes) throws RemoteException {
        HwLog.d(TAG, "notifyMonthLimit iface = " + iface + " bytes = " + bytes);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        for (INatTrafficNotifyListener listener : this.mListenerList) {
            listener.onMonthLimitReached(imsi, iface, bytes);
        }
    }

    public void notifyMonthWarn(String imsi, String iface, long bytes) throws RemoteException {
        HwLog.i(TAG, "notifyMonthWarn iface = " + iface + " bytes = " + bytes);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        for (INatTrafficNotifyListener listener : this.mListenerList) {
            listener.onMonthWarningReached(imsi, iface, bytes);
        }
    }

    public static void postRunnableAsync(Runnable r) {
        HsmExecutor.THREAD_POOL_EXECUTOR.execute(r);
    }

    public boolean isVSimEnable() {
        int enabledSubid = -1;
        try {
            enabledSubid = System.getInt(this.mContext.getContentResolver(), VSIM_EANBLED_SUBID);
        } catch (SettingNotFoundException snfe) {
            HwLog.e(TAG, snfe.getMessage());
        }
        return enabledSubid != -1;
    }
}
