package com.huawei.systemmanager.power.receiver.handle;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.huawei.systemmanager.power.model.UnifiedPowerAppControl;
import com.huawei.systemmanager.util.HwLog;

public class HandlePackageAdd implements IBroadcastHandler {
    private static final String TAG = "HandlePackageAdd";

    public void handleBroadcast(Context context, Intent intent) {
        handlePackageAdded(context, intent);
    }

    private void handlePackageAdded(Context context, Intent intent) {
        boolean replacing = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
        HwLog.i(TAG, "HandlePackageAdd replacing=" + replacing);
        if (!replacing) {
            String packageName = intent.getData().getSchemeSpecificPart();
            if (TextUtils.isEmpty(packageName)) {
                HwLog.i(TAG, "package name is mepty");
                return;
            }
            UnifiedPowerAppControl.getInstance(context).installApp(packageName);
        }
    }
}
