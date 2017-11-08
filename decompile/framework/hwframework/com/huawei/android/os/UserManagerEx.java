package com.huawei.android.os;

import android.content.pm.UserInfo;

public class UserManagerEx {
    public static final int FLAG_HW_HIDDENSPACE = 33554432;

    public static boolean isHwHiddenSpace(UserInfo info) {
        return info != null && (info.flags & 33554432) == 33554432;
    }
}
