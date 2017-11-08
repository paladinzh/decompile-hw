package com.huawei.systemmanager.comm.daulapp;

import android.content.Context;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import com.google.common.collect.Sets;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.util.HwLog;
import java.util.Set;

public class DualAppUtil {
    public static final String CLONE_APP_LIST = "clone_app_list";
    private static final String TAG = DualAppUtil.class.getSimpleName();

    public static boolean isPackageCloned(Context context, String packageName) {
        if (context == null) {
            HwLog.e(TAG, "context is null when check'" + packageName + "' whether a clone app");
            return false;
        } else if (TextUtils.isEmpty(packageName)) {
            return false;
        } else {
            return getClonePackages(context).contains(packageName);
        }
    }

    public static Set<String> getClonePackages(Context context) {
        String appList = Secure.getString(context.getContentResolver(), CLONE_APP_LIST);
        if (TextUtils.isEmpty(appList)) {
            return Sets.newHashSet();
        }
        return Sets.newHashSet(appList.split(SqlMarker.SQL_END));
    }
}
