package com.huawei.systemmanager.comm.process;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import com.huawei.systemmanager.util.HwLog;

public class HsmProcessUtil {
    private static final String TAG = "HsmProcessUtil";

    public static String getAppInfoByUidAndPid(Context context, int uid, int pid) {
        String[] pkgs = context.getPackageManager().getPackagesForUid(uid);
        if (pkgs == null || pkgs.length == 0) {
            return "";
        }
        if (1 == pkgs.length) {
            return pkgs[0];
        }
        String pkgName = "";
        for (RunningAppProcessInfo runningInfo : ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses()) {
            int runningPid = runningInfo.pid;
            int runningUid = runningInfo.uid;
            if (runningPid == pid && runningUid == uid) {
                String[] pkgNameList = runningInfo.pkgList;
                if (pkgNameList != null && pkgNameList.length > 0) {
                    pkgName = pkgNameList[0];
                }
                HwLog.i(TAG, "getAppInfoByUidAndPid pkgName = " + pkgName);
                return pkgName;
            }
        }
        return pkgName;
    }
}
