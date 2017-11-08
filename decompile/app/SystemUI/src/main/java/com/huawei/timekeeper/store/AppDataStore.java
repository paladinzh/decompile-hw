package com.huawei.timekeeper.store;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import fyusion.vislib.BuildConfig;

public class AppDataStore extends Store {
    public AppDataStore() {
        super(0);
    }

    public boolean save(Context context, int userHandle, String name, String value) {
        Editor editor = context.getSharedPreferences("timekeeper", 0).edit();
        editor.putString(name, value);
        return editor.commit();
    }

    public String restore(Context context, int userHandle, String name) {
        return context.getSharedPreferences("timekeeper", 0).getString(name, BuildConfig.FLAVOR);
    }

    public boolean remove(Context context, int userHandle, String name) {
        Editor editor = context.getSharedPreferences("timekeeper", 0).edit();
        editor.remove(name);
        return editor.commit();
    }
}
