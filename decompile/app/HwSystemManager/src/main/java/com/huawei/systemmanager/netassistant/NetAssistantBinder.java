package com.huawei.systemmanager.netassistant;

import android.annotation.TargetApi;
import android.net.HwNetworkPolicyManager;
import android.os.RemoteException;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import com.huawei.systemmanager.binder.INetAssistantBinder.Stub;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.task.ServiceStateMonitor;
import com.huawei.systemmanager.util.HwLog;

@TargetApi(22)
public class NetAssistantBinder extends Stub {
    private static final String TAG = "NetAssistantBinder";
    private ServiceStateMonitor mServiceStateMonitor;

    public NetAssistantBinder(ServiceStateMonitor serviceStateMonitor) {
        this.mServiceStateMonitor = serviceStateMonitor;
    }

    public boolean isDataRoamingState(int subId) throws RemoteException {
        return this.mServiceStateMonitor.getRoamingStateBySubId(subId);
    }

    public boolean isNetworkAccessInState(int uid, int networkType) throws RemoteException {
        boolean wifiAccess = true;
        int policy = HwNetworkPolicyManager.from(GlobalContext.getContext()).getHwUidPolicy(uid);
        if (networkType == 1) {
            boolean isRoaming = isCurrentDataRaomingState();
            boolean mobileAccess = (policy & 1) == 0;
            boolean roamingAccess = (policy & 4) == 0;
            if (!isRoaming) {
                return mobileAccess;
            }
            if (!mobileAccess) {
                roamingAccess = false;
            }
            return roamingAccess;
        } else if (networkType != 2) {
            return true;
        } else {
            if ((policy & 2) != 0) {
                wifiAccess = false;
            }
            return wifiAccess;
        }
    }

    public void setNetworkAccessInState(int uid, int networkType, boolean access) throws RemoteException {
        HwNetworkPolicyManager policyManager = HwNetworkPolicyManager.from(GlobalContext.getContext());
        if (networkType == 1) {
            boolean isRoaming = isCurrentDataRaomingState();
            if (access) {
                policyManager.removeHwUidPolicy(uid, 1);
                if (isRoaming) {
                    policyManager.removeHwUidPolicy(uid, 4);
                    return;
                }
                return;
            }
            policyManager.addHwUidPolicy(uid, 1);
            if (isRoaming) {
                policyManager.addHwUidPolicy(uid, 4);
            }
        } else if (networkType != 2) {
        } else {
            if (access) {
                policyManager.removeHwUidPolicy(uid, 2);
            } else {
                policyManager.addHwUidPolicy(uid, 2);
            }
        }
    }

    private boolean isCurrentDataRaomingState() throws RemoteException {
        SubscriptionInfo info = SubscriptionManager.from(GlobalContext.getContext()).getDefaultDataSubscriptionInfo();
        if (info != null) {
            return isDataRoamingState(info.getSubscriptionId());
        }
        HwLog.e(TAG, "SubsciptionInfo is null");
        return false;
    }
}
