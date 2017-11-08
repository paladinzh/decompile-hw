package com.huawei.timekeeper.store;

import android.content.Context;
import android.content.SharedPreferences.Editor;

public class AppDataStore extends Store {
    private static final String PREFERENCES_NAME = "timekeeper";

    public AppDataStore() {
        super(0);
    }

    public boolean save(Context context, int userHandle, String name, String value) {
        Editor editor = context.getSharedPreferences(PREFERENCES_NAME, 0).edit();
        editor.putString(name, value);
        return editor.commit();
    }

    public String restore(Context context, int userHandle, String name) {
        return context.getSharedPreferences(PREFERENCES_NAME, 0).getString(name, "");
    }

    public boolean remove(Context context, int userHandle, String name) {
        Editor editor = context.getSharedPreferences(PREFERENCES_NAME, 0).edit();
        editor.remove(name);
        return editor.commit();
    }
}
