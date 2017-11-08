package com.android.contacts.compatibility;

import android.content.Context;
import android.support.v4.os.UserManagerCompat;

public class UserManagerSdkCompat {
    public static boolean isUserUnlocked(Context context) {
        return UserManagerCompat.isUserUnlocked(context);
    }
}
