package com.android.gallery3d.util;

import android.content.Context;
import android.content.SharedPreferences.Editor;

public class SharePreferenceUtils {
    public static boolean getBooleanValue(Context context, String preferencesFileName, String key) {
        return context.getSharedPreferences(preferencesFileName, 0).getBoolean(key, true);
    }

    public static void putBooleanValue(Context context, String preferencesFileName, String key, boolean value) {
        Editor editor = context.getSharedPreferences(preferencesFileName, 0).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
}
