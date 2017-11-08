package com.huawei.systemmanager.antivirus.cache;

import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.systemmanager.service.CustomCaller;
import com.huawei.systemmanager.util.HwLog;

public class CheckIsVirusApkCaller extends CustomCaller {
    private static final String CHECK_VIRUS_RESULT = "result_code";
    private static final String METHOD_NAME = "isVirusApk";
    private static final int RESULT_CODE_ERROR = -1;
    private static final int RESULT_CODE_NOT_EXIST = 0;
    private static final String TAG = "CheckIsVirusApkCaller";

    public String getMethodName() {
        return METHOD_NAME;
    }

    public Bundle call(Bundle params) {
        HwLog.w(TAG, "CheckIsVirusApkCaller : call");
        Bundle bundle = new Bundle();
        String packageName = params != null ? params.getString("packageName", "") : "";
        if (TextUtils.isEmpty(packageName)) {
            HwLog.e(TAG, "error : isVirusApk, name is empty");
            bundle.putInt("result_code", -1);
            return bundle;
        }
        bundle.putInt("result_code", VirusAppsManager.getIntance().queryVirusLevel(packageName));
        return bundle;
    }

    public boolean shouldEnforcePermission() {
        return true;
    }
}
