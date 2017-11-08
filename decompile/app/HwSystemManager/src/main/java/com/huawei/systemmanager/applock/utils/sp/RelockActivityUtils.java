package com.huawei.systemmanager.applock.utils.sp;

import android.content.Context;
import com.huawei.systemmanager.applock.utils.DatabaseSharePrefUtil;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;

public class RelockActivityUtils {
    private static String RELOCK_ACTIVITYS_KEYS = "relock_activitys_name_flag";
    private static String RELOCK_FLAG_KEY = "relock_activity_flag";

    public static boolean shouldRelock(Context context) {
        return DatabaseSharePrefUtil.getPref(context, RELOCK_FLAG_KEY, false, false);
    }

    public static void setRelockFlag(Context context, boolean flag) {
        DatabaseSharePrefUtil.setPref(context, RELOCK_FLAG_KEY, flag, false);
    }

    public static boolean isRelockActivity(Context context, String baseClassName) {
        if (DatabaseSharePrefUtil.getPref(context, RELOCK_ACTIVITYS_KEYS, "", false).contains(baseClassName)) {
            return true;
        }
        return false;
    }

    public static void addRelockActivity(Context context, String baseClassName) {
        String existNames = DatabaseSharePrefUtil.getPref(context, RELOCK_ACTIVITYS_KEYS, "", false);
        if (!existNames.contains(baseClassName)) {
            DatabaseSharePrefUtil.setPref(context, RELOCK_ACTIVITYS_KEYS, existNames + SqlMarker.SQL_END + baseClassName, false);
        }
    }
}
