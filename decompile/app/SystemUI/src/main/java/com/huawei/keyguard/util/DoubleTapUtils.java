package com.huawei.keyguard.util;

import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings.Secure;

public class DoubleTapUtils {
    private static int getIndex(Context context) {
        return getIndexFrmoDB("Double_Touch_index", context);
    }

    private static int getIndexFrmoDB(String str, Context context) {
        int result = -1;
        try {
            result = Secure.getInt(context.getContentResolver(), str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean readWakeupCheckValue(Context context) {
        if (context == null) {
            return false;
        }
        int index = getIndex(context);
        if (index < 0) {
            return false;
        }
        return getCheckValue(getFlagValue(context), index);
    }

    private static int getFlagValue(Context context) {
        try {
            return Secure.getInt(context.getContentResolver(), "persist.sys.easyflag");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static boolean getCheckValue(int allValue, int index) {
        if (((1 << index) & allValue) == 0) {
            return false;
        }
        return true;
    }

    public static void offScreen(Context context) {
        if (context != null) {
            try {
                ((PowerManager) context.getSystemService("power")).goToSleep(SystemClock.uptimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
