package com.huawei.systemmanager.adblock.background;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import com.huawei.systemmanager.util.HwLog;

class AdBlockPref {
    private static final String SP_FILE_NAME = "ad_block_pref";
    private static final String SP_KEY_AD_ALARM_TIME = "ad_alarm_time";
    private static final String SP_KEY_AD_LAST_CHANGE_TIME = "ad_last_change_time";
    private static final String SP_KEY_AD_UPDATE_TYPE = "ad_update_type";
    private static final String TAG = "AdBlockPref";

    AdBlockPref() {
    }

    public static long getAlarmTime(Context context) {
        return getLong(context, SP_KEY_AD_ALARM_TIME, 0);
    }

    public static boolean setAlarmTime(Context context, long time) {
        return putLong(context, SP_KEY_AD_ALARM_TIME, time);
    }

    public static long getLastChangeTime(Context context) {
        return getLong(context, SP_KEY_AD_LAST_CHANGE_TIME, 0);
    }

    public static boolean setLastChangeTime(Context context, long time) {
        return putLong(context, SP_KEY_AD_LAST_CHANGE_TIME, time);
    }

    public static int getUpdateType(Context context) {
        return getInt(context, SP_KEY_AD_UPDATE_TYPE, 0);
    }

    public static boolean setUpdateType(Context context, int updateType) {
        return putInt(context, SP_KEY_AD_UPDATE_TYPE, updateType);
    }

    private static long getLong(Context context, String key, long defValue) {
        try {
            return context.getSharedPreferences(SP_FILE_NAME, 0).getLong(key, defValue);
        } catch (Exception e) {
            HwLog.e(TAG, "getLong Exception:", e);
            return defValue;
        }
    }

    private static boolean putLong(Context context, String key, long value) {
        try {
            Editor editor = context.getSharedPreferences(SP_FILE_NAME, 0).edit();
            editor.putLong(key, value);
            return editor.commit();
        } catch (Exception e) {
            HwLog.e(TAG, "setLong Exception:", e);
            return false;
        }
    }

    private static int getInt(Context context, String key, int defValue) {
        try {
            return context.getSharedPreferences(SP_FILE_NAME, 0).getInt(key, defValue);
        } catch (Exception e) {
            HwLog.e(TAG, "getInt Exception:", e);
            return defValue;
        }
    }

    private static boolean putInt(Context context, String key, int value) {
        try {
            Editor editor = context.getSharedPreferences(SP_FILE_NAME, 0).edit();
            editor.putInt(key, value);
            return editor.commit();
        } catch (Exception e) {
            HwLog.e(TAG, "putInt Exception:", e);
            return false;
        }
    }
}
