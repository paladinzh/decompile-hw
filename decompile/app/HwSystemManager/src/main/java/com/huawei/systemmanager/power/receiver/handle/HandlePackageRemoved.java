package com.huawei.systemmanager.power.receiver.handle;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.power.model.BatteryStatisticsHelper;
import com.huawei.systemmanager.power.model.UnifiedPowerAppControl;
import com.huawei.systemmanager.util.HwLog;

public class HandlePackageRemoved implements IBroadcastHandler {
    private static final String TAG = "HandlePackageRemoved";

    public void handleBroadcast(Context context, Intent intent) {
        handlePackageRemoved(context, intent);
    }

    private void handlePackageRemoved(Context context, Intent intent) {
        if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
            String pkgName = intent.getData().getSchemeSpecificPart();
            HwLog.i(TAG, "HandlePackageRemoved pkgName =" + pkgName);
            UnifiedPowerAppControl.getInstance(context).uninstallApp(pkgName);
            BatteryStatisticsHelper.updateBatteryInfos(pkgName);
        }
    }
}
