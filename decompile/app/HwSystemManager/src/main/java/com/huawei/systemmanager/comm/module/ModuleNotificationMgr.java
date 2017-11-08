package com.huawei.systemmanager.comm.module;

import android.content.Context;
import android.content.Intent;
import com.huawei.notificationmanager.ui.NotificationManagmentActivity;
import com.huawei.systemmanager.comm.module.IHsmModule.AbsHsmModule;

public class ModuleNotificationMgr extends AbsHsmModule {
    public Intent getMainEntry(Context ctx) {
        return new Intent(ctx, NotificationManagmentActivity.class);
    }
}
