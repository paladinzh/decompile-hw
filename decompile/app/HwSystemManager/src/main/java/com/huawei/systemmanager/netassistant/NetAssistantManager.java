package com.huawei.systemmanager.netassistant;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.huawei.systemmanager.binder.INetAssistantBinder;
import com.huawei.systemmanager.binder.INetAssistantBinder.Stub;
import com.huawei.systemmanager.util.HwLog;

public class NetAssistantManager {
    public static final String BINDER_NAME = "com.huawei.netassistant.binder.notificationcallbackbinder";
    public static final int NETWORK_TYPE_MOBILE = 1;
    public static final int NETWORK_TYPE_WIFI = 2;
    private static final String TAG = "NetAssistantManager";
    private static INetAssistantBinder sDefault;

    private static synchronized INetAssistantBinder getDefault() {
        INetAssistantBinder iNetAssistantBinder;
        synchronized (NetAssistantManager.class) {
            if (sDefault == null) {
                sDefault = getBinder();
            }
            iNetAssistantBinder = sDefault;
        }
        return iNetAssistantBinder;
    }

    private static INetAssistantBinder getBinder() {
        IBinder b = ServiceManager.getService("com.huawei.netassistant.binder.notificationcallbackbinder");
        if (b == null) {
            HwLog.e(TAG, "can't find the binder of net assistant binder");
            return null;
        }
        INetAssistantBinder service = Stub.asInterface(b);
        if (service != null) {
            return service;
        }
        HwLog.e(TAG, "the netassistant binder is null");
        return null;
    }

    public static boolean isNetworkAccessInState(int uid, int networkType) {
        try {
            if (getDefault() != null) {
                return getDefault().isNetworkAccessInState(uid, networkType);
            }
            HwLog.e(TAG, "isNetworkAccessInState binder is null");
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return true;
        }
    }

    public static void setNetworkAccessInState(int uid, int type, boolean access) {
        try {
            if (getDefault() == null) {
                HwLog.e(TAG, "setNetworkAccessInState binder is null");
            } else {
                getDefault().setNetworkAccessInState(uid, type, access);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
