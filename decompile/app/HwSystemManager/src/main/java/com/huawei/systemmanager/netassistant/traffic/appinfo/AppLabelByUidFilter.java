package com.huawei.systemmanager.netassistant.traffic.appinfo;

import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.UserManager;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import java.util.Arrays;

class AppLabelByUidFilter {
    public static final String TAG = "AppLabelByUidFilter";

    AppLabelByUidFilter() {
    }

    public static NetAppInfo buildUidDetail(int uid, NetAppInfo info) {
        PackageManager pm = GlobalContext.getContext().getPackageManager();
        String[] pkgs = pm.getPackagesForUid(uid);
        info.mUid = uid;
        if (SpecialUid.isOtherUserUid(uid)) {
            processOtherUserUid(uid, info);
        } else if (SpecialUid.isSpecialNameUid(uid)) {
            info.mAppLabel = GlobalContext.getString(SpecialUid.getSpecialUidName(uid));
        } else {
            processMultiApp(pkgs, pm, info);
        }
        checkAtLast(pkgs, pm, info);
        return info;
    }

    private static void processOtherUserUid(int uid, NetAppInfo info) {
        UserInfo userInfo = UserManager.get(GlobalContext.getContext()).getUserInfo(Math.abs(uid) - 2000);
        info.mAppLabel = userInfo != null ? userInfo.name : "";
    }

    private static void processMultiApp(String[] pkgs, PackageManager pm, NetAppInfo info) {
        if (pkgs != null && pm != null && info != null) {
            Arrays.sort(pkgs);
            int N = pkgs.length;
            int i = 0;
            while (i < N) {
                try {
                    info.mAppLabel = pm.getApplicationLabel(pm.getApplicationInfo(pkgs[i], 128)).toString();
                    return;
                } catch (Exception e) {
                    HwLog.i(TAG, "exception in processMultiApp");
                    i++;
                }
            }
        }
    }

    private static void checkAtLast(String[] pkgs, PackageManager pm, NetAppInfo info) {
        if (TextUtils.isEmpty(info.mAppLabel)) {
            info.mAppLabel = String.valueOf(info.mUid);
        }
        info.mAppLabel = info.mAppLabel.replaceAll("\\s", " ").trim();
        if (pkgs != null && pkgs.length > 1) {
            info.isMultiPkg = true;
        }
    }
}
