package com.huawei.permission.filter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.UserHandle;
import com.huawei.permission.FilterPolicy.Filter;
import com.huawei.permission.FilterPolicy.FilterResult;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;

public class ProfileTestEnvFilter extends Filter {
    private static final String TAG = "ProfileTestEnvFilter";
    private static final String mtestPkg = "com.android.cts.deviceandprofileowner";
    private Context mContext;

    public ProfileTestEnvFilter(Context context) {
        this.mContext = context;
    }

    public FilterResult filterOperation(int uid, int pid, int type) {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm != null) {
            try {
                HwLog.i(TAG, "It's in test env: " + PackageManagerWrapper.getPackageInfo(pm, mtestPkg, 0));
                if (UserHandle.getUserId(uid) != 0) {
                    HwLog.i(TAG, "It's running in multi user mode.");
                    String[] pkgs = pm.getPackagesForUid(uid);
                    if (pkgs != null) {
                        for (String pkg : pkgs) {
                            if (HsmPackageManager.getInstance().isSystem(pkg) && type == 67108864) {
                                HwLog.i(TAG, "Package of uid is system:" + uid);
                                return new FilterResult(true, 1);
                            }
                        }
                    }
                }
            } catch (NameNotFoundException e) {
            }
        }
        return super.filterOperation(uid, pid, type);
    }
}
