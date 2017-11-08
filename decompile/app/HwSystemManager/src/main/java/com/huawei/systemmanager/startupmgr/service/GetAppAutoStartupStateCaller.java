package com.huawei.systemmanager.startupmgr.service;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.service.CustomCaller;
import com.huawei.systemmanager.startupmgr.comm.AwakedStartupInfo;
import com.huawei.systemmanager.startupmgr.db.StartupDataMgrHelper;
import com.huawei.systemmanager.util.HwLog;

public class GetAppAutoStartupStateCaller extends CustomCaller {
    private static final String CHECK_STATE_RESULT = "result_code";
    private static final String METHOD_NAME = "GetAppAutoStartupState";
    private static final int STARTUP_STATUS_ALLOW = 1;
    private static final int STARTUP_STATUS_FORBID = 2;
    private static final int STARTUP_STATUS_UNKNOWN = 0;
    private static final String TAG = "CheckAppAutoStartupStateCaller";

    public String getMethodName() {
        return METHOD_NAME;
    }

    public Bundle call(Bundle params) {
        if (Utility.isOwner()) {
            Bundle bundle = new Bundle();
            String packageName = params != null ? params.getString("packageName", "") : "";
            if (TextUtils.isEmpty(packageName)) {
                HwLog.e(TAG, "error : AppAutoStartupState, name is empty");
                bundle.putInt("result_code", 0);
                return bundle;
            }
            int startupStatus = getAutoStartupState(packageName);
            bundle.putInt("result_code", startupStatus);
            HwLog.i(TAG, "packageName = " + packageName + " startupStatus = " + startupStatus);
            return bundle;
        }
        HwLog.w(TAG, "current is not owner!");
        return null;
    }

    public boolean shouldEnforcePermission() {
        return true;
    }

    private int getAutoStartupState(String pkgName) {
        Context ctx = GlobalContext.getContext();
        if (ctx == null) {
            HwLog.w(TAG, "ctx is null!");
            return 0;
        }
        AwakedStartupInfo startupInfo = StartupDataMgrHelper.querySingleAwakedStartupInfo(ctx, pkgName);
        if (startupInfo == null) {
            return 0;
        }
        if (startupInfo.getStatus()) {
            return 1;
        }
        return 2;
    }
}
