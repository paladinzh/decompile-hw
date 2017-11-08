package com.huawei.systemmanager.netassistant.traffic.statusspeed;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.provider.Settings.System;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;

public class NatSettingInfo {
    private static final String DB_KEY_TRAFFIC_SWITCH = "traffic_switch";
    private static final String DB_KEY_UNLOCK_NOTIFY = "unlock_screen_notify";
    private static final String KEY_SHOW_NETWORK_SPEED_ENABLED = "show_network_speed_enabled";
    private static final String PACKAGE_NAME_SETTINGS = "com.android.settings";
    private static final int SWITCH_OFF = 0;
    private static final int SWITCH_ON = 1;
    private static final String TAG = NatSettingInfo.class.getSimpleName();

    public static String getStatusBarSpeedTitle() {
        String title = null;
        Resources res = null;
        try {
            res = GlobalContext.getContext().getPackageManager().getResourcesForApplication("com.android.settings");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (res == null) {
            return null;
        }
        int resource = res.getIdentifier("eu3_su_lf_displaysettings_displaynetworkspeed", "string", "com.android.settings");
        if (resource != 0) {
            title = res.getString(resource);
        } else {
            HwLog.e(TAG, "there is no string found");
        }
        return title;
    }

    public static String getStatusBarSpeedSummary() {
        String summary = null;
        Resources res = null;
        try {
            res = GlobalContext.getContext().getPackageManager().getResourcesForApplication("com.android.settings");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (res == null) {
            return null;
        }
        int resource = res.getIdentifier("eu3_su_ls_displaysetting_displaynetworkspeeddetails", "string", "com.android.settings");
        if (resource != 0) {
            summary = res.getString(resource);
        } else {
            HwLog.e(TAG, "there is no string found");
        }
        return summary;
    }

    public static int getStatusBarSpeedSet(Context context) {
        return System.getInt(context.getContentResolver(), KEY_SHOW_NETWORK_SPEED_ENABLED, -1);
    }

    public static void setStatusBarSpeedEnable(boolean value, Context context) {
        System.putInt(context.getContentResolver(), KEY_SHOW_NETWORK_SPEED_ENABLED, Boolean.valueOf(value).booleanValue() ? 1 : 0);
    }

    public static int getTrafficDisplaySet(Context context) {
        return System.getInt(context.getContentResolver(), DB_KEY_TRAFFIC_SWITCH, 0);
    }

    public static void setTrafficDisplayEnable(boolean value, Context context) {
        System.putInt(context.getContentResolver(), DB_KEY_TRAFFIC_SWITCH, Boolean.valueOf(value).booleanValue() ? 1 : 0);
    }

    public static boolean getUnlockScreenNotify(Context context) {
        if (System.getInt(context.getContentResolver(), DB_KEY_UNLOCK_NOTIFY, 0) == 1) {
            return true;
        }
        return false;
    }

    public static void setUnlockScreenNotify(Context context, boolean switchState) {
        int i;
        int i2 = 1;
        String[] strArr = new String[2];
        strArr[0] = HsmStatConst.PARAM_VAL;
        if (switchState) {
            i = 1;
        } else {
            i = 0;
        }
        strArr[1] = String.valueOf(i);
        HsmStat.statE((int) Events.E_NETASSISTANT_LOCK_NOTIFY_SWITCH, HsmStatConst.constructJsonParams(strArr));
        ContentResolver contentResolver = context.getContentResolver();
        String str = DB_KEY_UNLOCK_NOTIFY;
        if (!switchState) {
            i2 = 0;
        }
        System.putInt(contentResolver, str, i2);
    }
}
