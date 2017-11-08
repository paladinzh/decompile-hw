package com.huawei.systemmanager.competitorintercept;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.text.TextUtils;

public class InterceptRecords {
    public static final String COMPETITOR_INTECEPT_PERFERENCE = "competitor_intercept";
    public static final String ELAPSED_REALTIME = "elapsedRealtime";

    static void clearAllRecords(Context ctx) {
        ctx.getSharedPreferences(COMPETITOR_INTECEPT_PERFERENCE, 0).edit().clear().commit();
    }

    static boolean checkAndRecords(Context ctx, String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        SharedPreferences perfer = ctx.getSharedPreferences(COMPETITOR_INTECEPT_PERFERENCE, 0);
        if (perfer.getLong(pkgName, -1) > 0) {
            return false;
        }
        perfer.edit().putLong(pkgName, System.currentTimeMillis()).commit();
        return true;
    }

    static boolean checkAndSetElapsedTime(Context ctx) {
        long curTime = SystemClock.elapsedRealtime();
        SharedPreferences perfer = ctx.getSharedPreferences(COMPETITOR_INTECEPT_PERFERENCE, 0);
        long preTime = perfer.getLong(ELAPSED_REALTIME, 0);
        perfer.edit().putLong(ELAPSED_REALTIME, curTime).commit();
        if (curTime >= preTime) {
            return true;
        }
        return false;
    }
}
