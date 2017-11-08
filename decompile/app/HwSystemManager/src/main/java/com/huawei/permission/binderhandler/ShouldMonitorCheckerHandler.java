package com.huawei.permission.binderhandler;

import android.content.Context;
import android.os.Bundle;
import com.huawei.systemmanager.comm.grule.GRuleManager;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorScenario;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.util.HwLog;

public class ShouldMonitorCheckerHandler extends HoldServiceBinderHandler {
    private static final String LOG_TAG = "ShouldMonitorCheckerHandler";
    private Context mContext;

    public ShouldMonitorCheckerHandler(Context cxt) {
        this.mContext = cxt;
    }

    public Bundle handleTransact(Bundle params) {
        return checkShouldMonitor(params);
    }

    protected boolean ignorePermissionCheck() {
        return true;
    }

    private Bundle checkShouldMonitor(Bundle params) {
        if (!CustomizeWrapper.isPermissionEnabled()) {
            return new Bundle();
        }
        try {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        } catch (Exception e) {
            this.mContext.enforceCallingOrSelfPermission(Utility.SDK_API_PERMISSION, null);
        }
        String pkg = params.getString("packageName");
        if (pkg == null) {
            HwLog.w(LOG_TAG, "zzz checkShouldMonitor pkg is null.");
            return new Bundle();
        }
        Bundle result = new Bundle();
        result.putInt("shouldMonitor", GRuleManager.getInstance().shouldMonitor(this.mContext, MonitorScenario.SCENARIO_PERMISSION, pkg) ? 1 : 2);
        return result;
    }
}
