package com.android.systemui.utils;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import java.lang.reflect.Method;

@SuppressLint({"NewApi"})
public class UpdateNotificationInfos {
    private static final String THEME_DIR = (Environment.getDataDirectory() + "/skin");
    private static Method sGetjoinBitmapMethod = null;
    private static boolean sThemeSupportsetUnifiedNotification = false;

    public static Bundle getNotificationThemeData(int contIconId, int repIconId, int bgIndex, int repLocation) {
        return getNotificationThemeData(null, contIconId, repIconId, bgIndex, repLocation);
    }

    public static Bundle getNotificationThemeData(Bundle bundle, int contIconId, int repIconId, int bgIndex, int repLocation) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        if (contIconId > 0) {
            bundle.putInt("huawei.notification.contentIcon", contIconId);
        }
        if (repIconId > 0) {
            bundle.putInt("huawei.notification.replace.iconId", repIconId);
        }
        if (bgIndex >= 0) {
            bundle.putInt("huawei.notification.backgroundIndex", bgIndex);
        }
        if (repLocation > 0) {
            bundle.putInt("huawei.notification.replace.location", repLocation);
        }
        return bundle;
    }
}
