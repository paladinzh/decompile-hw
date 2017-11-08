package com.huawei.systemmanager.comm.wrapper;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import java.util.Set;

public final class SharePrefWrapper {
    public static int getPrefValue(Context context, String fileName, String key, int dftValue) {
        return context.getSharedPreferences(fileName, 4).getInt(key, dftValue);
    }

    public static long getPrefValue(Context context, String fileName, String key, long dftValue) {
        return context.getSharedPreferences(fileName, 4).getLong(key, dftValue);
    }

    public static boolean getPrefValue(Context context, String fileName, String key, boolean dftValue) {
        return context.getSharedPreferences(fileName, 4).getBoolean(key, dftValue);
    }

    public static String getPrefValue(Context context, String fileName, String key, String dftValue) {
        return context.getSharedPreferences(fileName, 4).getString(key, dftValue);
    }

    public static Set<String> getPrefValue(Context context, String fileName, String key, Set<String> dftValue) {
        return context.getSharedPreferences(fileName, 4).getStringSet(key, dftValue);
    }

    public static void setPrefValue(Context context, String fileName, String key, int value) {
        Editor editor = context.getSharedPreferences(fileName, 4).edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void setPrefValue(Context context, String fileName, String key, long value) {
        Editor editor = context.getSharedPreferences(fileName, 4).edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static void setPrefValue(Context context, String fileName, String key, boolean value) {
        Editor editor = context.getSharedPreferences(fileName, 4).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void setPrefValue(Context context, String fileName, String key, String value) {
        Editor editor = context.getSharedPreferences(fileName, 4).edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void setPrefValue(Context context, String fileName, String key, Set<String> value) {
        Editor editor = context.getSharedPreferences(fileName, 4).edit();
        editor.putStringSet(key, value);
        editor.commit();
    }
}
