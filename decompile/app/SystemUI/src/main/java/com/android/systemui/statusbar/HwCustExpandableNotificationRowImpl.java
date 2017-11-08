package com.android.systemui.statusbar;

import android.content.Context;
import android.provider.Settings.System;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import com.android.systemui.utils.HwLog;
import java.util.HashSet;

public class HwCustExpandableNotificationRowImpl extends HwCustExpandableNotificationRow {
    private static final String TAG = "HwCustExpandableNotificationRowImpl";
    private static final String THREE_IN_TOUCH = "com.hutchison3g.threeintouch";
    private HashSet<String> packageNames = new HashSet();

    public boolean isCustomUnClearable(StatusBarNotification mStatusBarNotification, Context mContext) {
        if (!(mStatusBarNotification == null || mStatusBarNotification.isClearable())) {
            String nowPackageName = mStatusBarNotification.getPackageName();
            if (THREE_IN_TOUCH.equals(nowPackageName) || isUnclearPackages(nowPackageName, mContext)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUnclearPackages(String name, Context mContext) {
        if (this.packageNames.size() == 0 && mContext != null) {
            String unclearName = System.getString(mContext.getContentResolver(), "hw_unclear_notifys_pkgs");
            if (!TextUtils.isEmpty(unclearName)) {
                String[] names = unclearName.split(";");
                for (String trim : names) {
                    this.packageNames.add(trim.trim());
                }
            }
        }
        if (this.packageNames.size() == 0 || !this.packageNames.contains(name)) {
            return false;
        }
        HwLog.i(TAG, "Unclear the notification package name: " + name);
        return true;
    }
}
