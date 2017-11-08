package com.huawei.harassmentinterception.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import com.huawei.harassmentinterception.strategy.StrategyConfigs.StrategyId;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.useragreement.UserAgreementHelper;
import com.huawei.systemmanager.util.HwLog;

public class PreferenceHelper {
    public static final String KEY_AUTO_UPDATE_STATE = "harassment_auto_update_state";
    public static final String KEY_BLACK_WHITE_DB_HOTLINEWITHOUT_AREA_UPDATE_STATE = "harassment_blackwhite_hotline_noareacode_update_handle_status";
    public static final String KEY_BOOL_UPDATED = "has_update";
    public static final String KEY_LAST_ALARM_TIME = "last_alarm_time";
    public static final String KEY_LAST_WATCH_CALL_TIME = "theLastWatchCallTime";
    public static final String KEY_LAST_WATCH_MESSAGE_TIME = "theLastWatchMessageTime";
    public static final String KEY_ONLY_WIFI_UPDATE_STATE = "harassment_only_wifi_update_state";
    public static final String KEY_PREFERENCE_ENTRANCE = "com.huawei.harassmentinterception.setting_preference";
    public static final String KEY_RULE = "harassment_interception_rule";
    public static final String KEY_STRATEGY_CONFIG = "harassment_interception_strategy";
    public static final String KEY_UPDATE_RATE = "harassment_update_rate";
    public static final String TAG = "PreferenceHelper";
    public static final int VALUE_DEFAULT_RATE_STATE = 1;
    public static final boolean VALUE_DEFAULT_STATE_AUTOUPDATE = (!Utility.isLowRamDevice());
    public static final boolean VALUE_DEFAULT_STATE_UPDATE_ONLY_WIFI;
    public static final int VALUE_RULE_BLACKLIST = 0;
    public static final int VALUE_RULE_INTELLIGENT = 1;

    static {
        boolean z = false;
        if (!Utility.isLowRamDevice()) {
            z = true;
        }
        VALUE_DEFAULT_STATE_UPDATE_ONLY_WIFI = z;
    }

    public static void setState(Context c, String pre, boolean state) {
        Editor editor = c.getSharedPreferences(KEY_PREFERENCE_ENTRANCE, 4).edit();
        editor.putBoolean(pre, state);
        editor.commit();
    }

    public static void setUpdateRate(Context c, int updateRate) {
        Editor editor = c.getSharedPreferences(KEY_PREFERENCE_ENTRANCE, 4).edit();
        editor.putInt(KEY_UPDATE_RATE, updateRate);
        editor.commit();
    }

    public static boolean getState(Context c, String pre) {
        SharedPreferences sp = c.getSharedPreferences(KEY_PREFERENCE_ENTRANCE, 4);
        if (!"harassment_auto_update_state".equals(pre)) {
            return false;
        }
        if (isAccessNetworkAuthorized(c)) {
            return sp.getBoolean(pre, VALUE_DEFAULT_STATE_AUTOUPDATE);
        }
        return sp.getBoolean(pre, false);
    }

    public static boolean isAccessNetworkAuthorized(Context c) {
        if (CustomizeManager.getInstance().isFeatureEnabled(8)) {
            return UserAgreementHelper.getUserAgreementState(c);
        }
        return true;
    }

    public static int getUpdateRate(Context c) {
        return c.getSharedPreferences(KEY_PREFERENCE_ENTRANCE, 4).getInt(KEY_UPDATE_RATE, 1);
    }

    public static boolean isSuccessAutoUpdateInTime(Context c) {
        long interval = ((long) getUpdateRate(c)) * 259200000;
        long lastTime = getLastAlarmTime(c);
        long currTime = System.currentTimeMillis();
        if (lastTime <= 0) {
            setLastAlarmTime(c, currTime);
            HwLog.i(TAG, "Init update time.");
            return true;
        } else if (lastTime > currTime) {
            HwLog.i(TAG, "Time is changed to past, so update. lastTime = " + lastTime);
            return false;
        } else {
            boolean isInTime = currTime < (interval + lastTime) + 21600000;
            HwLog.i(TAG, "Is auto update in time :" + isInTime);
            return isInTime;
        }
    }

    public static void setInterceptionStrategy(Context c, int strategyId) {
        if (StrategyId.INVALID.getValue() == strategyId) {
            strategyId = StrategyId.PASS_ALL.getValue();
            HwLog.d(TAG, "setInterceptionStrategy: Correct strategy to " + strategyId);
        }
        Editor editor = c.getSharedPreferences(KEY_PREFERENCE_ENTRANCE, 4).edit();
        editor.putInt(KEY_STRATEGY_CONFIG, strategyId);
        editor.commit();
        CommonHelper.notifyInterceptionSettingChange(c);
    }

    public static int getInterceptionStrategy(Context c) {
        SharedPreferences sp = c.getSharedPreferences(KEY_PREFERENCE_ENTRANCE, 4);
        int strategy = sp.getInt(KEY_STRATEGY_CONFIG, -1);
        if (-1 != strategy) {
            if (StrategyId.INVALID.getValue() == strategy) {
                strategy = StrategyId.PASS_ALL.getValue();
                setInterceptionStrategy(c, strategy);
                HwLog.d(TAG, "getInterceptionStrategy: Correct strategy to " + strategy);
            }
            return StrategyId.PASS_WHITELIST.getValue() | strategy;
        }
        strategy = sp.getInt(KEY_RULE, -1);
        if (-1 != strategy) {
            if (1 == strategy) {
                strategy = StrategyId.BLOCK_INTELLIGENT.getValue();
            } else {
                strategy = StrategyId.BLOCK_BLACKLIST.getValue();
            }
            setInterceptionStrategy(c, strategy);
            HwLog.i(TAG, "getInterceptionStrategy: Read config from older version , strategy = " + strategy);
            return StrategyId.PASS_WHITELIST.getValue() | strategy;
        }
        if (Utility.isLowRamDevice()) {
            strategy = StrategyId.BLOCK_BLACKLIST.getValue() | StrategyId.BLOCK_KEYWORDS.getValue();
            HwLog.i(TAG, "getInterceptionStrategy: Low ram device, disable intelligent rule by default");
        } else {
            strategy = (StrategyId.BLOCK_BLACKLIST.getValue() | StrategyId.BLOCK_KEYWORDS.getValue()) | StrategyId.BLOCK_INTELLIGENT.getValue();
        }
        return StrategyId.PASS_WHITELIST.getValue() | strategy;
    }

    public static String getLastUpdateTimeString(Context context) {
        return CommonHelper.getSystemDateStyle(context, getLastAlarmTime(context));
    }

    public static boolean hasUpdate(Context context) {
        return context.getSharedPreferences(KEY_PREFERENCE_ENTRANCE, 4).getBoolean(KEY_BOOL_UPDATED, false);
    }

    public static long getLastAlarmTime(Context context) {
        SharedPreferences sp = context.getSharedPreferences(KEY_PREFERENCE_ENTRANCE, 4);
        String lastAlarmTime = sp.getString(KEY_LAST_ALARM_TIME, null);
        HwLog.i(TAG, "lastAlarmTime: " + lastAlarmTime);
        if (lastAlarmTime != null) {
            return Long.parseLong(lastAlarmTime);
        }
        long currentTime = System.currentTimeMillis();
        Editor editor = sp.edit();
        editor.putString(KEY_LAST_ALARM_TIME, String.valueOf(currentTime));
        editor.commit();
        HwLog.i(TAG, "getLastAlarmTime: Current time is set as last update time : " + CommonHelper.getSystemDateStyle(context, currentTime));
        return System.currentTimeMillis();
    }

    public static void setLastAlarmTime(Context context, long lastAlarmTime) {
        HwLog.i(TAG, "setLastAlarmTime: " + lastAlarmTime);
        Editor editor = context.getSharedPreferences(KEY_PREFERENCE_ENTRANCE, 4).edit();
        if (0 == lastAlarmTime) {
            editor.putString(KEY_LAST_ALARM_TIME, String.valueOf(System.currentTimeMillis()));
        } else {
            editor.putString(KEY_LAST_ALARM_TIME, String.valueOf(lastAlarmTime));
        }
        editor.putBoolean(KEY_BOOL_UPDATED, true);
        editor.commit();
    }

    public static void saveLastAlarmTime(Context context) {
        setLastAlarmTime(context, 0);
    }

    public static long getLastWatchCallTime(Context context) {
        return getSystemSettingLong(context, KEY_LAST_WATCH_CALL_TIME);
    }

    public static long getLastWatchMessageTime(Context context) {
        return getSystemSettingLong(context, KEY_LAST_WATCH_MESSAGE_TIME);
    }

    private static long getSystemSettingLong(Context context, String settingKey) {
        long lValue = 0;
        try {
            lValue = System.getLong(context.getContentResolver(), settingKey);
        } catch (SettingNotFoundException e) {
            HwLog.w(TAG, "getSystemSettingLong: Value is not set ,key = " + settingKey);
        }
        return lValue;
    }

    public static void setLastWatchCallTime(Context context) {
        System.putLong(context.getContentResolver(), KEY_LAST_WATCH_CALL_TIME, System.currentTimeMillis());
    }

    public static void setLastWatchMessageTime(Context context) {
        System.putLong(context.getContentResolver(), KEY_LAST_WATCH_MESSAGE_TIME, System.currentTimeMillis());
    }

    public static int getBlackWhiteListDBUpdatedStatus(Context context) {
        return context.getSharedPreferences(KEY_PREFERENCE_ENTRANCE, 4).getInt(KEY_BLACK_WHITE_DB_HOTLINEWITHOUT_AREA_UPDATE_STATE, 0);
    }

    public static void setBlackWhiteListDBUpdatedStatus(Context context) {
        Editor editor = context.getSharedPreferences(KEY_PREFERENCE_ENTRANCE, 4).edit();
        editor.putInt(KEY_BLACK_WHITE_DB_HOTLINEWITHOUT_AREA_UPDATE_STATE, 1);
        editor.commit();
    }
}
