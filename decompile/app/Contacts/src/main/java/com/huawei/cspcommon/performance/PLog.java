package com.huawei.cspcommon.performance;

import android.content.Context;
import android.text.TextUtils;
import com.android.contacts.util.HwLog;

public class PLog {
    public static final boolean DEBUG = HwLog.HWDBG;

    public static void open(Context context) {
        if (context.getSharedPreferences("PLog", 0).getBoolean("PLog_on", false)) {
            PLogManager.init();
        }
        ContactPerformanceRadar.init();
    }

    public static void d(int tagId, String msg) {
        HwLog.i("PLog", msg);
        PLogManager.addLog(tagId, msg);
        jlog(tagId, msg);
    }

    private static void jlog(int tagId, String msg) {
        String tagName = (String) PConstants.sMappingJLogIds.get(tagId);
        if (!TextUtils.isEmpty(tagName)) {
            ContactPerformanceRadar.reportPerformanceRadar(tagName, msg);
        }
    }
}
