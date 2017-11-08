package com.huawei.notificationmanager.restore;

import android.app.INotificationManager;
import android.app.INotificationManager.Stub;
import android.app.IntentService;
import android.content.Intent;
import android.os.ServiceManager;
import com.hsm.notificationmanager.M2NAdapter;
import com.huawei.notificationmanager.util.Helper;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;

public class ReStoreNotificationService extends IntentService {
    private static final String TAG = "ReStoreNotificationService";
    static INotificationManager sINM = Stub.asInterface(ServiceManager.getService("notification"));

    public ReStoreNotificationService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        try {
            M2NAdapter.reStoreBrokenApps(sINM, GlobalContext.getContext().getPackageManager().getInstalledApplications(0));
            Helper.setRestoreBrokenNotification(GlobalContext.getContext(), true);
        } catch (Exception e) {
            HwLog.e(TAG, e.getMessage());
        }
    }
}
