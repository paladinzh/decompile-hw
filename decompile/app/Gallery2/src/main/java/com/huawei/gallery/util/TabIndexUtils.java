package com.huawei.gallery.util;

import android.content.Context;
import android.preference.PreferenceManager;

public class TabIndexUtils {
    private static Object sLock = new Object();
    private static int sTabIndex = -1;

    public static void init(Context context) {
        synchronized (sLock) {
            sTabIndex = PreferenceManager.getDefaultSharedPreferences(context).getInt("Gallery_TabMode_Index", 0);
        }
    }

    public static int getsTabIndex(Context context) {
        int i;
        synchronized (sLock) {
            if (sTabIndex == -1) {
                sTabIndex = PreferenceManager.getDefaultSharedPreferences(context).getInt("Gallery_TabMode_Index", 0);
            }
            i = sTabIndex;
        }
        return i;
    }

    public static void setIndex(Context context, int index) {
        synchronized (sLock) {
            sTabIndex = index;
        }
    }

    public static void saveIndex(Context context, int index) {
        synchronized (sLock) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("Gallery_TabMode_Index", index).commit();
            sTabIndex = index;
        }
    }
}
