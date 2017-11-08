package com.android.contacts.hap.util;

import android.content.Context;
import android.os.UserHandle;
import android.os.UserManager;

public class MultiUsersUtils {
    private static final boolean IS_CURRENT_USER_OWNER;
    private static final boolean IS_SUPPORT_MULTI_USERS = UserManager.supportsMultipleUsers();

    static {
        boolean z = true;
        if (IS_SUPPORT_MULTI_USERS && UserHandle.myUserId() != 0) {
            z = false;
        }
        IS_CURRENT_USER_OWNER = z;
    }

    public static boolean isCurrentUserOwner() {
        return IS_CURRENT_USER_OWNER;
    }

    public static boolean isCurrentUserGuest() {
        return !IS_CURRENT_USER_OWNER;
    }

    public static boolean isSmsEnabledForCurrentUser(Context context) {
        if (IS_CURRENT_USER_OWNER) {
            return true;
        }
        return !((UserManager) context.getSystemService("user")).hasUserRestriction("no_sms");
    }
}
