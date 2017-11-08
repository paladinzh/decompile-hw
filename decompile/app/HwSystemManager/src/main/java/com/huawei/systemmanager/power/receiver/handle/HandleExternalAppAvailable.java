package com.huawei.systemmanager.power.receiver.handle;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.IntentCompat;
import android.text.TextUtils;
import com.huawei.systemmanager.power.model.UnifiedPowerAppControl;
import com.huawei.systemmanager.util.HwLog;

public class HandleExternalAppAvailable implements IBroadcastHandler {
    private static final String TAG = "HandleExternalAppAvailable";

    public void handleBroadcast(Context context, Intent intent) {
        handleExternalAppAvailable(context, intent);
    }

    private void handleExternalAppAvailable(Context context, Intent intent) {
        String[] pkgArray = intent.getStringArrayExtra(IntentCompat.EXTRA_CHANGED_PACKAGE_LIST);
        if (pkgArray == null) {
            HwLog.i(TAG, "sdcard packge pkgArray == null ");
            return;
        }
        UnifiedPowerAppControl unifiedPowerAppControl = UnifiedPowerAppControl.getInstance(context);
        for (String pkgName : pkgArray) {
            if (TextUtils.isEmpty(pkgName)) {
                HwLog.i(TAG, "sdcard packge pkgName = " + pkgName);
            } else if (unifiedPowerAppControl.checkExsist(pkgName)) {
                HwLog.i(TAG, "sdcard packge:" + pkgName + "already exist");
            } else {
                HwLog.i(TAG, "add sdcard packge:" + pkgName);
                unifiedPowerAppControl.installApp(pkgName);
            }
        }
    }
}
