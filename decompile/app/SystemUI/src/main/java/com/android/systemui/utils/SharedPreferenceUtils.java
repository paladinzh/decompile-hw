package com.android.systemui.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class SharedPreferenceUtils {
    public static void writeString(Context context, String spName, String key, String value) {
        SharedPreferences preferences;
        if (spName == null) {
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
        } else {
            preferences = context.getSharedPreferences(spName, 0);
        }
        Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
        editor.commit();
    }

    public static String getString(Context context, String spName, String key) {
        SharedPreferences preferences;
        if (spName == null) {
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
        } else {
            preferences = context.getSharedPreferences(spName, 0);
        }
        return preferences.getString(key, null);
    }
}
