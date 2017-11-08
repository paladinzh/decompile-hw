package com.huawei.systemmanager.comm.misc;

import android.content.Context;
import android.content.SharedPreferences;

public class TipsUtil {
    public static final String KEY_VIEWED_COMPETITORS = "viewd_competitors";
    private static final String PREFERENCE_TIPS = "preferences_tips";

    public static SharedPreferences getSharedPrefer(Context ctx) {
        return ctx.getSharedPreferences(PREFERENCE_TIPS, 0);
    }
}
