package com.huawei.timekeeper.store;

import android.content.Context;

public abstract class Store {
    protected final int mMode;

    public abstract boolean remove(Context context, int i, String str);

    public abstract String restore(Context context, int i, String str);

    public abstract boolean save(Context context, int i, String str, String str2);

    public Store(int mode) {
        this.mMode = mode;
    }

    public int getMode() {
        return this.mMode;
    }

    public void checkPermission(Context context) throws SecurityException {
    }

    public String getStoredName(Context context, int userHandle, String originName) {
        return originName;
    }
}
