package com.huawei.systemmanager.applock.utils.sp;

import android.content.Context;
import com.huawei.systemmanager.applock.utils.DatabaseSharePrefUtil;

public class FunctionSwitchUtils {
    private static final String APP_LOCK_BIND_REMIND_KEY = "app_lock_bind_remind";
    public static final String APP_LOCK_FUNC_STATUS_KEY = "app_lock_func_status";

    public static boolean getFunctionSwitchStatus(Context context) {
        return DatabaseSharePrefUtil.getPref(context, APP_LOCK_FUNC_STATUS_KEY, true, true);
    }

    public static void setFunctionSwitchStatus(Context context, boolean status) {
        DatabaseSharePrefUtil.setPref(context, APP_LOCK_FUNC_STATUS_KEY, status, true);
        if (status) {
            resetBindRemindStatus(context);
        }
    }

    public static boolean getBindRemindStatus(Context context) {
        return DatabaseSharePrefUtil.getPref(context, APP_LOCK_BIND_REMIND_KEY, true, false);
    }

    public static void resetBindRemindStatus(Context context) {
        DatabaseSharePrefUtil.setPref(context, APP_LOCK_BIND_REMIND_KEY, true, false);
    }

    public static void clearBindRemindStatus(Context context) {
        DatabaseSharePrefUtil.setPref(context, APP_LOCK_BIND_REMIND_KEY, false, false);
    }
}
