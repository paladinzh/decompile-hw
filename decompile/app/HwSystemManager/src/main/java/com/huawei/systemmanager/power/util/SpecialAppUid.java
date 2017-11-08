package com.huawei.systemmanager.power.util;

import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.os.UserManager;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;

public class SpecialAppUid {
    public static final int OTHER_USER_RANGE_START = -2000;

    public static boolean isOtherUserUid(int uid) {
        return uid <= -2000;
    }

    public static int collapseUidsTogether(int uid, int currentUserId) {
        if (UserHandle.getUserId(uid) != currentUserId) {
            return -(UserHandle.getUserId(uid) + Events.E_PERMISSION_RECOMMEND_CLICK);
        }
        return uid;
    }

    public static Drawable getBadgedIconOfSpecialUidApp(Drawable icon, int userId) {
        return UserManager.get(GlobalContext.getContext()).getBadgedIconForUser(icon, new UserHandle(userId));
    }
}
