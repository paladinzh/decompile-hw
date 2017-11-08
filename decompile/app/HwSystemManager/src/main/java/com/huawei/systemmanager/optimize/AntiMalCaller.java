package com.huawei.systemmanager.optimize;

import android.os.Bundle;
import com.huawei.systemmanager.antimal.AntiMalManager;
import com.huawei.systemmanager.antimal.AntiMalService;
import com.huawei.systemmanager.antimal.MalAppInfo;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.service.CustomCaller;
import com.huawei.systemmanager.util.HwLog;

public class AntiMalCaller extends CustomCaller {
    private static final String ANTIMAL_MODULE = "antiMalware";
    private static final int INSERT_FAIL = 1;
    private static final String INSERT_RESULT = "result";
    private static final int INSERT_SUCCESS = 0;
    private static final String INSTALL_BEGIN = "begin";
    private static final String INSTALL_END = "end";
    private static final String LOG_TAG = "AntiMalCaller";
    private static final String PACKAGE_NAME = "pkg";
    private static final String SOURCE_PACKAGE_NAME = "src";
    private AntiMalManager amm;

    public String getMethodName() {
        return ANTIMAL_MODULE;
    }

    public AntiMalCaller() {
        this.amm = null;
        this.amm = AntiMalService.getInstance(GlobalContext.getContext()).getAntiMalMgr();
    }

    public Bundle call(Bundle params) {
        String pkgName = params.getString("pkg");
        int srcPkg = params.getInt("src");
        long beginTime = params.getLong(INSTALL_BEGIN);
        long endTime = params.getLong(INSTALL_END);
        HwLog.i(LOG_TAG, "pkgName:" + pkgName + " srcPkg:" + srcPkg + " beginTime:" + beginTime + " endTime:" + endTime);
        Bundle res = new Bundle();
        if (this.amm != null && this.amm.isInitialed() && this.amm.needCollectData()) {
            MalAppInfo info = new MalAppInfo(GlobalContext.getContext());
            info.mPackageName = pkgName;
            info.mInstaller = srcPkg;
            info.mInstallBeginTime = beginTime;
            info.mInstallEndTime = endTime;
            info.mAppStatus = 0;
            info.mAppVersion = "";
            info.mAppName = "";
            info.mSpaceTime = 0;
            info.mSignHash = "";
            this.amm.insertAppInfo(info);
            res.putInt("result", 0);
        } else {
            res.putInt("result", 1);
        }
        return res;
    }

    public boolean shouldEnforcePermission() {
        return true;
    }
}
