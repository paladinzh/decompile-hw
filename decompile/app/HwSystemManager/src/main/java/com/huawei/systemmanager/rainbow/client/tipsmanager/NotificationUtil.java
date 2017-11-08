package com.huawei.systemmanager.rainbow.client.tipsmanager;

import android.content.Context;
import android.database.Cursor;
import com.huawei.notificationmanager.common.ConstValues;
import com.huawei.notificationmanager.db.DBProvider;
import com.huawei.notificationmanager.util.Helper;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.util.app.HsmPkgUtils;

public class NotificationUtil {
    public static int getNotificationStatus(Context context, String pkgName) {
        int uid = HsmPkgUtils.getPackageUid(pkgName);
        boolean allowSendNotification = false;
        if (-1 != uid) {
            allowSendNotification = Helper.areNotificationsEnabledForPackage(pkgName, uid);
        }
        if (!allowSendNotification) {
            return 1;
        }
        int notificationStatus;
        Cursor cursorNotification = context.getContentResolver().query(DBProvider.URI_NOTIFICATION_CFG, null, "packageName = ? ", new String[]{pkgName}, null);
        if (CursorHelper.checkCursorValid(cursorNotification)) {
            cursorNotification.moveToNext();
            if (cursorNotification.getInt(cursorNotification.getColumnIndex(ConstValues.NOTIFICATION_CFG)) == 0) {
                notificationStatus = 2;
            } else {
                notificationStatus = 0;
            }
            cursorNotification.close();
        } else {
            notificationStatus = 0;
        }
        return notificationStatus;
    }
}
