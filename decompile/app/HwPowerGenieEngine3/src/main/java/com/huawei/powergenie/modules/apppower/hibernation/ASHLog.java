package com.huawei.powergenie.modules.apppower.hibernation;

import android.util.Log;

public class ASHLog {
    public static void i(String logMsg) {
        i("ash", logMsg);
    }

    public static void i(String tag, String logMsg) {
        Log.i(tag, logMsg);
    }

    public static void d(String logMsg) {
    }

    public static void w(String logMsg) {
        Log.w("ash", logMsg);
    }

    public static void e(String logMsg) {
        Log.e("ash", logMsg);
    }
}
