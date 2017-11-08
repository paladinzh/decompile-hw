package com.huawei.systemmanager.power.receiver.handle;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.PowerSavingMgr;
import com.huawei.systemmanager.power.notification.UserNotifier;
import com.huawei.systemmanager.power.util.SavingSettingUtil;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import java.util.ArrayList;

public class HandleNotifierActions implements IBroadcastHandler {
    public void handleBroadcast(Context ctx, Intent intent) {
        String action = intent.getAction();
        if (UserNotifier.ACTION_NOTIFICATION_NOT_REMIND.equals(action)) {
            HsmStat.statE(PowerSavingMgr.ACTION_CLICK_POWER_COST_NOTIFICATION, "i");
            SavingSettingUtil.setRogue(ctx.getContentResolver(), intent.getStringExtra("pkgName"), 1, Integer.valueOf(1));
        } else if (UserNotifier.ACTION_NOTIFICATION_CLOSE_APP.equals(action)) {
            ArrayList<String> al = new ArrayList();
            al.add(intent.getStringExtra("pkgName"));
            SysCoreUtils.forceStopPackageAndSyncSaving(ctx, al);
        }
        UserNotifier.destroyNotification(ctx);
    }
}
