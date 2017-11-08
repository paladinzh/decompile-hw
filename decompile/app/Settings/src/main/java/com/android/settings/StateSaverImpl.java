package com.android.settings;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class StateSaverImpl extends AbstractStateSaver {
    public boolean update(Context context, String key, long newCount) {
        if (!isChanged(context, key, newCount)) {
            return false;
        }
        save(context, key, newCount);
        setChanged();
        notifyObservers();
        return true;
    }

    public long query(Context context, String key, long defaultValue) {
        return context.getSharedPreferences("com.android.settings_preferences", 0).getLong(key, defaultValue);
    }

    private boolean isChanged(Context context, String key, long newCount) {
        return newCount != query(context, key, 0);
    }

    private static void save(Context context, String key, long value) {
        Editor editor = context.getSharedPreferences("com.android.settings_preferences", 0).edit();
        editor.putLong(key, value);
        editor.commit();
        Log.i("StateSaver", key + " state is changed: " + value);
    }
}
