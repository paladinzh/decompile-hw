package com.huawei.systemmanager.securitythreats.background;

import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.securitythreats.comm.SecurityThreatsConst;
import com.huawei.systemmanager.service.CustomCaller;
import com.huawei.systemmanager.util.HwLog;

public class NotifyInstallVirusCaller extends CustomCaller {
    private static final String TAG = "NotifyInstallVirusCaller";

    public String getMethodName() {
        return SecurityThreatsConst.METHOD_NOTIFY_INSTALL_VIRUS;
    }

    public Bundle call(Bundle params) {
        if (params == null) {
            HwLog.w(TAG, "notifyInstallVirus params is null, return");
            return null;
        }
        String name = params.getString("name", "");
        String path = params.getString(SecurityThreatsConst.CHECK_UNINSTALL_PKG_PATH, "");
        if (TextUtils.isEmpty(name)) {
            HwLog.w(TAG, "notifyInstallVirus name is empty");
            return null;
        }
        HwLog.i(TAG, "notifyInstallVirus name=" + name + ", path is empty?" + TextUtils.isEmpty(path));
        VirusNotifyControl.getInstance(GlobalContext.getContext()).addUninstallVirus(name);
        return null;
    }
}
