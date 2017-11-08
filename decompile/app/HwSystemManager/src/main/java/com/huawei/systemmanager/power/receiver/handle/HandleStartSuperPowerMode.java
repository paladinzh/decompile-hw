package com.huawei.systemmanager.power.receiver.handle;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.comm.SharedPrefKeyConst;
import com.huawei.systemmanager.power.service.SuperDialogShowService;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.util.HwLog;

public class HandleStartSuperPowerMode implements IBroadcastHandler {
    private static final String TAG = HandleStartSuperPowerMode.class.getSimpleName();

    public void handleBroadcast(Context ctx, Intent intent) {
        if (ActivityManager.isUserAMonkey()) {
            HwLog.d(TAG, "Monkey testing!");
            return;
        }
        String packageName = intent.getStringExtra("package_name");
        if (!TextUtils.isEmpty(packageName)) {
            HwLog.i(TAG, "HandleStartSuperPowerMode, packageName = " + packageName);
        }
        showRemindDialog(ctx, packageName);
    }

    private void showRemindDialog(Context context, String packageNameFrom) {
        HsmStat.statSuperPowerDialogAction("d", packageNameFrom);
        if (SharePrefWrapper.getPrefValue(context, SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.SUPER_POWER_SAVE_MODE_DIALOG_REMIND_KEY, String.valueOf(0)).equals("0")) {
            HwLog.i(TAG, "Super Power Save Mode Remind Dialog need show from " + packageNameFrom);
            Intent serviceIntent = new Intent();
            serviceIntent.setClass(context, SuperDialogShowService.class);
            Bundle bundle = new Bundle();
            bundle.putString(ApplicationConstant.SUPER_DIALOG_PACKAGEFROM, packageNameFrom);
            bundle.putString(ApplicationConstant.SUPER_DIALOG_LABEL, ApplicationConstant.LOW_BATTERY_SUPER_DIALOG);
            serviceIntent.putExtras(bundle);
            context.startServiceAsUser(serviceIntent, UserHandle.CURRENT);
            return;
        }
        HwLog.i(TAG, "open super Power Save mode directly from " + packageNameFrom);
        SysCoreUtils.enterSuperPowerSavingMode(context);
    }
}
