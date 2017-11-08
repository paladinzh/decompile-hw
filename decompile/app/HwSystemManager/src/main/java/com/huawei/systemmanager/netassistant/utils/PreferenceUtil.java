package com.huawei.systemmanager.netassistant.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferenceUtil {
    public static final void clearNetAppTag(Context context, String uid) {
        SharedPreferences mNoteFlagsShared = context.getSharedPreferences("note_preferences", 4);
        if (!mNoteFlagsShared.getString(uid, "").isEmpty()) {
            Editor editor = mNoteFlagsShared.edit();
            editor.remove(uid);
            editor.commit();
        }
    }

    public static final void resetAllNetAppTag(Context context) {
        Editor editor = context.getSharedPreferences("note_preferences", 4).edit();
        editor.clear();
        editor.commit();
    }
}
