package com.huawei.systemmanager.power.receiver.handle;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.systemmanager.optimize.smcs.SMCSDatabaseConstant.DlBlockColumns;
import com.huawei.systemmanager.power.util.SavingSettingUtil;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.util.HwLog;

public class HandlePGCleanPowerConsumeApp implements IBroadcastHandler {
    private static final String TAG = HandlePGCleanPowerConsumeApp.class.getSimpleName();

    public void handleBroadcast(Context ctx, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            HwLog.w(TAG, "HandlePGCleanPowerConsumeApp bundle is null");
            return;
        }
        String pkgName = bundle.getString("pkgName");
        long killAppTimestamp = bundle.getLong(DlBlockColumns.COLUMN_TIMESTAMP, -1);
        if (pkgName == null || killAppTimestamp == -1) {
            HwLog.w(TAG, "HandlePGCleanPowerConsumeApp intent is invalid");
            return;
        }
        HwLog.i(TAG, "PG_CLEAN_POWER_CONSUME_APP is received and pkgName =" + pkgName + " ,killAppTimestamp =" + killAppTimestamp);
        boolean autoClearSuperHighPowerApp = SysCoreUtils.getSuperHighPowerSwitchState(ctx);
        if (autoClearSuperHighPowerApp) {
            SavingSettingUtil.recordSuperHighPower(ctx, pkgName);
            HwLog.i(TAG, "HSM  record info of app killed by PG and pkgName =" + pkgName);
        } else {
            HwLog.i(TAG, "autoClearSuperHighPowerApp is " + autoClearSuperHighPowerApp);
        }
    }
}
