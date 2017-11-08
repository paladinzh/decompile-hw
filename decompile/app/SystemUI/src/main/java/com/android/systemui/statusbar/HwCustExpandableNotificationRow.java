package com.android.systemui.statusbar;

import android.content.Context;
import android.service.notification.StatusBarNotification;

public class HwCustExpandableNotificationRow {
    public boolean isCustomUnClearable(StatusBarNotification mStatusBarNotification, Context mContext) {
        return false;
    }
}
