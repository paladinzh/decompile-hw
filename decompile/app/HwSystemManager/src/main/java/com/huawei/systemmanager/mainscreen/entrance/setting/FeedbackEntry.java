package com.huawei.systemmanager.mainscreen.entrance.setting;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.securitythreats.comm.SecurityThreatsConst;
import com.huawei.systemmanager.util.HwLog;

public class FeedbackEntry {
    private static final String TAG = "FeedbackEntry";

    public static Intent getSettingEntryIntent(Context ctx) {
        Intent intent = new Intent("com.huawei.phoneservice.FEEDBACK");
        intent.putExtra("appId", 2);
        intent.putExtra("questionType", SecurityThreatsConst.PUSH_FILE_MODULE);
        return intent;
    }

    public static boolean isEnable(Context ctx) {
        if (!Utility.checkIntentAlivable(ctx, getSettingEntryIntent(ctx))) {
            HwLog.i(TAG, "can not resolve feed back intent");
            return false;
        } else if (!AbroadUtils.isAbroad()) {
            return true;
        } else {
            HwLog.i(TAG, "current is abroad");
            return false;
        }
    }
}
