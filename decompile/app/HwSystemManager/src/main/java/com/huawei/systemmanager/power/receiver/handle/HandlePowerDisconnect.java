package com.huawei.systemmanager.power.receiver.handle;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.power.notification.PowerNotificationMgr;
import com.huawei.systemmanager.power.notification.UserNotifier;

public class HandlePowerDisconnect implements IBroadcastHandler {
    public void handleBroadcast(Context ctx, Intent intent) {
        PowerNotificationMgr.clearNotifiedPkgPreference(ctx);
        UserNotifier.destroyNotification(ctx);
    }
}
