package com.huawei.netassistant.wifiap;

import android.content.Context;
import android.content.SharedPreferences.Editor;

public class WifiApPrefHelper {
    private static final long DFT_VALUE = 5000;
    private static final long DFT_VALUE_MAX = 30000;
    private static final long DFT_VALUE_MIN = 100;
    private static final String PREF_ENTRANCE = "wifiap_stats";
    private static final String PREF_KEY_INTERVAL_DFT = "stats_interval_dft";
    private static final String PREF_KEY_INTERVAL_MAX = "stats_interval_max";
    private static final String PREF_KEY_INTERVAL_MIN = "stats_interval_min";

    public static long getMinStatsInterval(Context c) {
        return getLongPreference(c, PREF_KEY_INTERVAL_MIN, DFT_VALUE_MIN);
    }

    public static long getMaxStatsInterval(Context c) {
        return getLongPreference(c, PREF_KEY_INTERVAL_MAX, DFT_VALUE_MAX);
    }

    public static long getDftStatsInterval(Context c) {
        return getLongPreference(c, PREF_KEY_INTERVAL_DFT, DFT_VALUE);
    }

    public static void setMinStatsInterval(Context c, long interval) {
        setLongPreference(c, PREF_KEY_INTERVAL_MIN, interval);
    }

    public static void setMaxStatsInterval(Context c, long interval) {
        setLongPreference(c, PREF_KEY_INTERVAL_MAX, interval);
    }

    public static void setDftStatsInterval(Context c, long interval) {
        setLongPreference(c, PREF_KEY_INTERVAL_DFT, interval);
    }

    private static void setLongPreference(Context c, String key, long value) {
        Editor editor = c.getSharedPreferences(PREF_ENTRANCE, 0).edit();
        editor.putLong(key, value);
        editor.commit();
    }

    private static long getLongPreference(Context c, String key, long dftValue) {
        return c.getSharedPreferences(PREF_ENTRANCE, 0).getLong(key, dftValue);
    }
}
