package com.android.contacts.compatibility;

import android.content.Context;

public class UserManagerCompat {
    public static boolean isUserUnlocked(Context context) {
        if (CompatUtils.isNCompatible()) {
            return UserManagerSdkCompat.isUserUnlocked(context);
        }
        return true;
    }
}
