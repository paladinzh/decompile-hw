package com.huawei.notificationmanager.rule;

import android.os.Bundle;

public class NotificationRuleMgr {
    public static final int BACKGROUND_INDEX_0 = 0;
    public static final int BACKGROUND_INDEX_1 = 1;
    public static final int BACKGROUND_INDEX_2 = 2;
    public static final int BACKGROUND_INDEX_3 = 3;
    public static final int BACKGROUND_INDEX_4 = 4;
    public static final int BACKGROUND_INDEX_5 = 5;
    public static final int BACKGROUND_INDEX_6 = 6;
    public static final int BACKGROUND_INDEX_7 = 7;
    private static final String HW_NOTIFICATION_BACKGROUND_INDEX = "huawei.notification.backgroundIndex";
    private static final String HW_NOTIFICATION_CONTENT_ICON = "huawei.notification.contentIcon";
    private static final String HW_NOTIFICATION_REPLACE_ICONID = "huawei.notification.replace.iconId";
    private static final String HW_NOTIFICATION_REPLACE_LOCATION = "huawei.notification.replace.location";
    public static final int REPLACE_LOCATION_BIGCONTENT = 4;
    public static final int REPLACE_LOCATION_CONTENT = 2;
    public static final int REPLACE_LOCATION_HEADSUP = 8;
    public static final int REPLACE_LOCATION_LARGEICON = 1;

    public static Bundle getNotificationThemeData(int contIconId, int repIconId, int bgIndex, int repLocation) {
        return getNotificationThemeData(null, contIconId, repIconId, bgIndex, repLocation);
    }

    public static Bundle getNotificationThemeData(Bundle bundle, int contIconId, int repIconId, int bgIndex, int repLocation) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        if (contIconId > 0) {
            bundle.putInt(HW_NOTIFICATION_CONTENT_ICON, contIconId);
        }
        if (repIconId > 0) {
            bundle.putInt(HW_NOTIFICATION_REPLACE_ICONID, repIconId);
        }
        if (bgIndex >= 0) {
            bundle.putInt(HW_NOTIFICATION_BACKGROUND_INDEX, bgIndex);
        }
        if (repLocation > 0) {
            bundle.putInt(HW_NOTIFICATION_REPLACE_LOCATION, repLocation);
        }
        return bundle;
    }
}
