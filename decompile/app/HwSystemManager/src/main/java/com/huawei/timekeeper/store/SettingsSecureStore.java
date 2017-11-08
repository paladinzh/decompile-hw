package com.huawei.timekeeper.store;

import android.content.Context;
import android.provider.Settings.Secure;
import com.huawei.timekeeper.AbsTimeKeeper;

public class SettingsSecureStore extends Store {
    public SettingsSecureStore() {
        super(1);
    }

    public void checkPermission(Context context) {
        String permission = "android.permission.WRITE_SECURE_SETTINGS";
        if (context.checkCallingOrSelfPermission(permission) != 0) {
            throw new SecurityException("Access denied, must have permission " + permission);
        }
    }

    public String getStoredName(Context context, int userHandle, String originName) {
        if (AbsTimeKeeper.USER_NULL != userHandle) {
            return originName + "_" + userHandle;
        }
        return originName;
    }

    public boolean save(Context context, int userHandle, String name, String value) {
        if (AbsTimeKeeper.USER_NULL == userHandle) {
            return Secure.putString(context.getContentResolver(), name, value);
        }
        return Secure.putStringForUser(context.getContentResolver(), name, value, userHandle);
    }

    public String restore(Context context, int userHandle, String name) {
        if (AbsTimeKeeper.USER_NULL == userHandle) {
            return Secure.getString(context.getContentResolver(), name);
        }
        return Secure.getStringForUser(context.getContentResolver(), name, userHandle);
    }

    public boolean remove(Context context, int userHandle, String name) {
        if (AbsTimeKeeper.USER_NULL == userHandle) {
            return Secure.putString(context.getContentResolver(), name, "");
        }
        return Secure.putStringForUser(context.getContentResolver(), name, "", userHandle);
    }
}
