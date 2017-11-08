package com.huawei.systemmanager.power.receiver.handle;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.power.model.UnifiedPowerAppControl;
import com.huawei.systemmanager.power.model.UnifiedPowerBean;
import com.huawei.systemmanager.power.provider.SmartProviderHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class HandlePackageInstallForUserOption implements IBroadcastHandler {
    private static final String TAG = "HandlePackageInstallForUserOption";

    public void handleBroadcast(Context context, Intent intent) {
        handlePackageInstallForUserOption(context, intent);
    }

    private void handlePackageInstallForUserOption(Context context, Intent intent) {
        int powerState = intent.getIntExtra("POWER_SET", 1);
        String packageName1 = intent.getStringExtra("packageName");
        UnifiedPowerBean unifiedPowerBean = SmartProviderHelper.getProtectAppFromDbByPKG(context, packageName1);
        HwLog.i(TAG, "handlePackageInstallForUserOption unifiedPowerBean = " + unifiedPowerBean + " powerState=" + powerState + " packageName1 =" + packageName1);
        if (unifiedPowerBean != null) {
            ArrayList<String> list = new ArrayList();
            list.add(packageName1);
            if (powerState == 1) {
                SmartProviderHelper.updateUnifiedPowerAppListForDB(list, 1, context);
                UnifiedPowerAppControl.getInstance(context).addAppToFWKForDOZEAndAppStandby(packageName1);
            } else if (powerState == 0) {
                SmartProviderHelper.updateUnifiedPowerAppListForDB(list, 0, context);
                UnifiedPowerAppControl.getInstance(context).removeAppToFWKForDOZEAndAppStandby(packageName1);
            }
        } else if (powerState == 1) {
            SmartProviderHelper.insertUnifiedPowerAppListForDB(packageName1, true, true, context);
            UnifiedPowerAppControl.getInstance(context).addAppToFWKForDOZEAndAppStandby(packageName1);
        } else if (powerState == 0) {
            SmartProviderHelper.insertUnifiedPowerAppListForDB(packageName1, false, true, context);
            UnifiedPowerAppControl.getInstance(context).removeAppToFWKForDOZEAndAppStandby(packageName1);
        }
    }
}
