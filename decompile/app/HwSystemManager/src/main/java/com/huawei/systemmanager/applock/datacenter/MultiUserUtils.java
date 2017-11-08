package com.huawei.systemmanager.applock.datacenter;

import android.app.ActivityManager;
import com.huawei.systemmanager.comm.misc.Utility;

public class MultiUserUtils {
    public static boolean isInMultiUserMode() {
        return !Utility.isOwnerUser(false);
    }

    public static boolean currentUserIsOwner() {
        if (ActivityManager.getCurrentUser() == 0) {
            return true;
        }
        return false;
    }
}
