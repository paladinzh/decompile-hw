package com.huawei.systemmanager.applock.utils.sp;

import android.content.Context;
import com.huawei.systemmanager.applock.utils.DatabaseSharePrefUtil;

public class ReloadSwitchUtils {
    private static final String APPLIST_RELOAD_KEY = "applist_need_reload";

    public static boolean isApplicationListNeedReload(Context context) {
        return DatabaseSharePrefUtil.getPref(context, APPLIST_RELOAD_KEY, false, false);
    }

    public static void setApplicationListNeedReload(Context context) {
        DatabaseSharePrefUtil.setPref(context, APPLIST_RELOAD_KEY, true, false);
    }

    public static void setApplicationListAlreadyReload(Context context) {
        DatabaseSharePrefUtil.setPref(context, APPLIST_RELOAD_KEY, false, false);
    }
}
