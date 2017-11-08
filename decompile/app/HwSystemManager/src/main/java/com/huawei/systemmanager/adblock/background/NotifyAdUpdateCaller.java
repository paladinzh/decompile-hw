package com.huawei.systemmanager.adblock.background;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import com.huawei.systemmanager.adblock.comm.AdUtils;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.process.HsmProcessUtil;
import com.huawei.systemmanager.rainbow.CloudSwitchHelper;
import com.huawei.systemmanager.service.CustomCaller;
import com.huawei.systemmanager.util.HwLog;

public class NotifyAdUpdateCaller extends CustomCaller {
    private static final String TAG = "AdBlock_NotifyAdUpdateCaller";

    public String getMethodName() {
        return "notifyAdUpdate";
    }

    public Bundle call(Bundle params) {
        if (CloudSwitchHelper.isCloudEnabled()) {
            if (checkCallingPermission()) {
                Context context = GlobalContext.getContext();
                AdBlockPref.setUpdateType(context, 2);
                AdUtils.update(context, 2);
            }
            return null;
        }
        HwLog.i(TAG, "cloud is not enable, just return");
        return null;
    }

    public boolean shouldEnforcePermission() {
        return false;
    }

    private boolean checkCallingPermission() {
        int uid = Binder.getCallingUid();
        if (1000 == uid) {
            HwLog.i(TAG, "checkCallingPermission by SYSTEM_UID");
            return true;
        }
        String callingApp = HsmProcessUtil.getAppInfoByUidAndPid(GlobalContext.getContext(), uid, Binder.getCallingPid());
        boolean isValid = AdUtils.checkAppmarket(GlobalContext.getContext(), callingApp);
        HwLog.i(TAG, "checkCallingPermission callingApp=" + callingApp + ", isValid=" + isValid);
        return isValid;
    }
}
