package com.huawei.systemmanager.power.util;

import android.content.Context;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.power.comm.SharedPrefKeyConst;

public class PowerDialogLifeCycle {
    public static final String DIALOG_ENABLE_SLEEP_SAVE = "DIALOG_SLEEP_SAVE";
    private static final String DIALOG_LIFE_SUFFIX = "_LIFE_SUFFIX";

    public static boolean needShowDialog(Context ctx, String dialogType) {
        return SharePrefWrapper.getPrefValue(ctx, SharedPrefKeyConst.POWER_SLEEPING_SAVE_KEY, dialogType + DIALOG_LIFE_SUFFIX, true);
    }

    public static void setDiaglogNeverShow(Context ctx, String dialogType) {
        SharePrefWrapper.setPrefValue(ctx, SharedPrefKeyConst.POWER_SLEEPING_SAVE_KEY, dialogType + DIALOG_LIFE_SUFFIX, false);
    }
}
