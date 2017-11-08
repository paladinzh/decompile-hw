package com.huawei.netassistant.binder;

import android.os.RemoteException;
import com.huawei.netassistant.binder.INotificationCallback.Stub;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.service.MainService.GenericService;
import com.huawei.systemmanager.util.HwLog;

public class NotificationCallbackBinder extends Stub implements GenericService {
    public static final String NOTIFICATION_CALLBACK = "com.huawei.netassistant.binder.notificationcallbackbinder";
    private static final String TAG = "NotificationCallbackBinder";

    public void onNotificationPanelExpanded() throws RemoteException {
        HwLog.d(TAG, "onNotificationPanelExpanded");
        GlobalContext.getContext().enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
    }

    public void onNotificationPanelClosed() throws RemoteException {
        HwLog.d(TAG, "onNotificationPanelClosed");
        GlobalContext.getContext().enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
    }

    public void init() {
    }
}
