package com.huawei.powergenie.core.policy;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public final class SharedPref {
    private static SharedPreferences getSettingsPref(Context context) {
        if (context != null) {
            return context.getSharedPreferences("pg_settings", 0);
        }
        return null;
    }

    public static void updateInitStatus(Context context, boolean value) {
        updateSettings(context, "init_finish", value);
    }

    public static boolean getInitStatus(Context context, boolean def) {
        return getSettings(context, "init_finish", def);
    }

    public static String getVersion(Context context) {
        SharedPreferences pref = getSettingsPref(context);
        if (pref != null) {
            return pref.getString("version", null);
        }
        return null;
    }

    public static void updateVersion(Context context, String version) {
        Editor prefEditor = getSettingsPref(context).edit();
        prefEditor.putString("version", version);
        prefEditor.commit();
    }

    public static void clearSettingsPref(Context context) {
        Editor prefEditor = getSettingsPref(context).edit();
        prefEditor.clear();
        prefEditor.commit();
    }

    public static boolean getSettings(Context context, String key, boolean def) {
        SharedPreferences pref = getSettingsPref(context);
        if (pref != null) {
            return pref.getBoolean(key, def);
        }
        return def;
    }

    public static void updateSettings(Context context, String key, boolean value) {
        Editor prefEditor = getSettingsPref(context).edit();
        prefEditor.putBoolean(key, value);
        prefEditor.commit();
    }

    public static void updateCrashPid(Context context, int value) {
        Editor prefEditor = getSettingsPref(context).edit();
        prefEditor.putInt("crash_pid", value);
        prefEditor.commit();
    }

    public static int getCrashPid(Context context, int defVal) {
        SharedPreferences pref = getSettingsPref(context);
        if (pref != null) {
            return pref.getInt("crash_pid", defVal);
        }
        return defVal;
    }

    public static void updateLongSettings(Context context, String key, long value) {
        Editor prefEditor = getSettingsPref(context).edit();
        prefEditor.putLong(key, value);
        prefEditor.commit();
    }

    public static long getLongSettings(Context context, String key, long def) {
        SharedPreferences pref = getSettingsPref(context);
        if (pref != null) {
            return pref.getLong(key, def);
        }
        return def;
    }

    public static void updateIntSettings(Context context, String key, int value) {
        Editor prefEditor = getSettingsPref(context).edit();
        prefEditor.putInt(key, value);
        prefEditor.commit();
    }

    public static int getIntSettings(Context context, String key, int def) {
        SharedPreferences pref = getSettingsPref(context);
        if (pref != null) {
            return pref.getInt(key, def);
        }
        return def;
    }

    public static void removeSettingsKey(Context context, String key) {
        Editor prefEditor = getSettingsPref(context).edit();
        if (prefEditor != null && key != null) {
            prefEditor.remove(key);
            prefEditor.commit();
        }
    }

    public static String getStringSettings(Context context, String key, String def) {
        SharedPreferences pref = getSettingsPref(context);
        if (pref != null) {
            return pref.getString(key, def);
        }
        return def;
    }

    public static void updateStringSettings(Context context, String key, String val) {
        Editor prefEditor = getSettingsPref(context).edit();
        prefEditor.putString(key, val);
        prefEditor.commit();
    }

    private static SharedPreferences getPermanentPref(Context context) {
        if (context != null) {
            return context.getSharedPreferences("permanent_settings", 0);
        }
        return null;
    }

    protected static boolean isDisabledGsfGms(Context context) {
        SharedPreferences pref = getPermanentPref(context);
        if (pref != null) {
            return pref.getBoolean("disable_gms", false);
        }
        return false;
    }

    protected static void writeDisabledGsfGms(Context context, boolean disable) {
        Editor prefEditor = getPermanentPref(context).edit();
        prefEditor.putBoolean("disable_gms", disable);
        prefEditor.commit();
    }
}
