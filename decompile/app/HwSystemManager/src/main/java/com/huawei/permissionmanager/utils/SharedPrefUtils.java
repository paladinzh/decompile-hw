package com.huawei.permissionmanager.utils;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import com.huawei.systemmanager.util.HwLog;

public class SharedPrefUtils {
    private static final String LOG_TAG = "SharedPrefUtils";
    private static final String PERMISSION_DATA_CLEARED_FLAG = "PermissionDataCleardFlag";
    private static final String PERMISSION_EMENDATION = "com.huawei.permission.emendation";
    private static final String PERMISSION_EMENDATION_FLAG = "PermissionEmendationFlag";
    private static final String PERMISSION_M_UPGRADE_FLAG = "PermissionMUpgradeFlag";

    public static void setPermissionEmendationFlag(Context context, boolean Flag) {
        if (context == null) {
            HwLog.w(LOG_TAG, "setPermissionEmendationFlag, Received null context!.");
            new Exception().printStackTrace();
            return;
        }
        try {
            Editor editor = context.getSharedPreferences(PERMISSION_EMENDATION, 4).edit();
            editor.putBoolean(PERMISSION_EMENDATION_FLAG, Flag);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setMPermissionUpgradeFlag(Context context, boolean flag) {
        if (context == null) {
            HwLog.w(LOG_TAG, "setMPermissionUpgradeFlag, Received null context!.");
            new Exception().printStackTrace();
            return;
        }
        try {
            Editor editor = context.getSharedPreferences(PERMISSION_EMENDATION, 4).edit();
            editor.putBoolean(PERMISSION_M_UPGRADE_FLAG, flag);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean getPermissionEmendationFlag(Context context) {
        boolean emendationFlag = false;
        if (context == null) {
            HwLog.w(LOG_TAG, "getPermissionEmendationFlag, Received null context!.");
            new Exception().printStackTrace();
            return emendationFlag;
        }
        try {
            emendationFlag = context.getSharedPreferences(PERMISSION_EMENDATION, 4).getBoolean(PERMISSION_EMENDATION_FLAG, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return emendationFlag;
    }

    public static boolean getMPermissionUpgradeFlag(Context context) {
        boolean resultFlag = false;
        if (context == null) {
            HwLog.w(LOG_TAG, "getMPermissionUpgradeFlag, Received null context!.");
            new Exception().printStackTrace();
            return resultFlag;
        }
        try {
            resultFlag = context.getSharedPreferences(PERMISSION_EMENDATION, 4).getBoolean(PERMISSION_M_UPGRADE_FLAG, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultFlag;
    }

    public static void setDataClearedFlag(Context context, boolean Flag) {
        if (context == null) {
            HwLog.w(LOG_TAG, "setDataClearedFlag, Received null context!.");
            new Exception().printStackTrace();
            return;
        }
        try {
            Editor editor = context.getSharedPreferences(PERMISSION_EMENDATION, 4).edit();
            editor.putBoolean(PERMISSION_DATA_CLEARED_FLAG, Flag);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean getDataClearedFlag(Context context) {
        boolean emendationFlag = false;
        if (context == null) {
            HwLog.w(LOG_TAG, "getDataClearedFlag, Received null context!.");
            new Exception().printStackTrace();
            return emendationFlag;
        }
        try {
            emendationFlag = context.getSharedPreferences(PERMISSION_EMENDATION, 4).getBoolean(PERMISSION_DATA_CLEARED_FLAG, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return emendationFlag;
    }
}
