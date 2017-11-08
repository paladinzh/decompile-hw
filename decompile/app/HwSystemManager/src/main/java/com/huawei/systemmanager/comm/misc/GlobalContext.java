package com.huawei.systemmanager.comm.misc;

import android.content.Context;

public class GlobalContext {
    private static Context sContext;

    public static void setContext(Context ctx) {
        sContext = ctx;
    }

    public static Context getContext() {
        return sContext;
    }

    public static String getString(int resId) {
        if (sContext == null) {
            return null;
        }
        return sContext.getString(resId);
    }

    public static String getString(int resId, Object... formatArgs) {
        if (sContext == null) {
            return null;
        }
        return sContext.getString(resId, formatArgs);
    }

    public static int getDimensionPixelOffset(int id) {
        if (sContext == null) {
            return 0;
        }
        return sContext.getResources().getDimensionPixelOffset(id);
    }

    public static int getInteger(int id) {
        if (sContext == null) {
            return 0;
        }
        return sContext.getResources().getInteger(id);
    }
}
