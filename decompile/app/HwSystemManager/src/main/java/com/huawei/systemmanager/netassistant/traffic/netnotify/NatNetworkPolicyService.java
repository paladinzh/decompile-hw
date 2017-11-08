package com.huawei.systemmanager.netassistant.traffic.netnotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.INetworkManagementEventObserver;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.server.net.BaseNetworkObserver;
import com.google.android.collect.Lists;
import com.huawei.systemmanager.netassistant.traffic.netnotify.INatNetworkPolicyBinder.Stub;
import com.huawei.systemmanager.service.MainService.HsmService;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class NatNetworkPolicyService extends Stub implements HsmService {
    public static final String ACTION_NETWORK_STATS_UPDATED = "com.android.server.action.NETWORK_STATS_UPDATED";
    public static final String EXTRA_NETWORK_TEMPLATE = "android.net.NETWORK_TEMPLATE";
    public static final String NAT_NETWORK_POLICY = "com.huawei.systemmanager.netassistant.netpolicy.NatNetworkPolicyService";
    public static final String TAG = NatNetworkPolicyService.class.getSimpleName();
    List<ITrafficChangeListener> listenerList;
    private INetworkManagementEventObserver mAlertObserver = new BaseNetworkObserver() {
        public void limitReached(String limitName, String iface) {
            NatNetworkPolicyService.this.mContext.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            HwLog.d(NatNetworkPolicyService.TAG, "limitReached:listenerList size = " + NatNetworkPolicyService.this.listenerList.size());
            for (ITrafficChangeListener listener : NatNetworkPolicyService.this.listenerList) {
                try {
                    HwLog.d(NatNetworkPolicyService.TAG, "limitReached:listener = " + listener);
                    listener.onLimitReached(iface);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private Context mContext;
    final Handler mHandler;
    private Callback mHandlerCallback = new NATHandlerCallback();
    private INetworkManagementService mNetworkManager = null;
    private BroadcastReceiver mStatsReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            NatNetworkPolicyService.this.mContext.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            HwLog.d(NatNetworkPolicyService.TAG, "Network Stats Updated");
            for (ITrafficChangeListener listener : NatNetworkPolicyService.this.listenerList) {
                try {
                    listener.onNetworkStatsUpdated();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private static class NATHandlerCallback implements Callback {
        private NATHandlerCallback() {
        }

        public boolean handleMessage(Message msg) {
            HwLog.d("TAG", "Message Callback");
            return false;
        }
    }

    public NatNetworkPolicyService(Context context) {
        this.mContext = context;
        HandlerThread thread = new HandlerThread(TAG);
        this.mNetworkManager = INetworkManagementService.Stub.asInterface(ServiceManager.getService("network_management"));
        thread.start();
        this.mHandler = new Handler(thread.getLooper(), this.mHandlerCallback);
        this.listenerList = Lists.newArrayList();
    }

    private void registerObserver() {
        try {
            this.mNetworkManager.registerObserver(this.mAlertObserver);
            HwLog.d(TAG, "registerObserver: register Observer = " + this.mAlertObserver);
        } catch (RemoteException e) {
            HwLog.e(TAG, "registerObserver: Fail to register alert Observer");
            e.printStackTrace();
        }
    }

    private void unRegisterObserver() {
        try {
            this.mNetworkManager.unregisterObserver(this.mAlertObserver);
            HwLog.d(TAG, "unRegisterObserver: unregister ObServer = " + this.mAlertObserver);
        } catch (RemoteException e) {
            HwLog.e(TAG, "unRegisterObserver: Fail to unregister alert Observer");
            e.printStackTrace();
        }
    }

    private void registerReceiver() {
        this.mContext.registerReceiver(this.mStatsReceiver, new IntentFilter(ACTION_NETWORK_STATS_UPDATED), "android.permission.READ_NETWORK_USAGE_HISTORY", this.mHandler);
    }

    private void unRegisterReceiver() {
        this.mContext.unregisterReceiver(this.mStatsReceiver);
    }

    public void advisePersistThreshold(long alertBytes) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        try {
            this.mNetworkManager.setGlobalAlert(alertBytes);
        } catch (IllegalStateException e) {
            HwLog.e(TAG, "registerGlobalAlert: IllegalStateException " + e);
        } catch (RemoteException e2) {
            HwLog.e(TAG, "registerGlobalAlert: RemoteException " + e2);
        }
    }

    public void registerTrafficChangeListener(ITrafficChangeListener listener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        this.listenerList.add(listener);
    }

    public void unRegisterTrafficChangeListener(ITrafficChangeListener listener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        this.listenerList.remove(listener);
    }

    public void init() {
        registerReceiver();
        registerObserver();
    }

    public void onDestroy() {
        unRegisterReceiver();
        unRegisterObserver();
    }

    public void onConfigurationChange(Configuration newConfig) {
    }

    public void onStartCommand(Intent intent, int flags, int startId) {
    }
}
