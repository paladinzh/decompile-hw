package com.huawei.systemmanager.applock.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.google.common.collect.Sets;
import com.huawei.systemmanager.applock.datacenter.AppLockProvider.AuthSuccessPackageProvider;
import com.huawei.systemmanager.applock.datacenter.AppLockProvider.LockStatusProvider;
import com.huawei.systemmanager.applock.datacenter.AppLockProvider.LockedPackageProvider;
import com.huawei.systemmanager.applock.datacenter.AppLockProvider.UnlockedPackageProvider;
import com.huawei.systemmanager.applock.datacenter.tbl.AppLockStatusTable;
import com.huawei.systemmanager.applock.utils.sp.ReloadSwitchUtils;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.Set;

public class ProviderWrapperUtils {
    private static final String TAG = "ProviderWrapperUtils";

    public static Set<String> getLockedApps(Context context) {
        return getPackageListOfAppLockView(context, LockedPackageProvider.CONTENT_URI, "packageName");
    }

    public static Set<String> getUnLockedApps(Context context) {
        return getPackageListOfAppLockView(context, UnlockedPackageProvider.CONTENT_URI, "packageName");
    }

    public static void replaceAppLockStatus(Context context, String pkgName, int lockStatus) {
        HwLog.d(TAG, "unLockOnApp:" + pkgName);
        ContentValues contentValues = new ContentValues();
        contentValues.put("packageName", pkgName);
        contentValues.put(AppLockStatusTable.COL_LOCK_STATUS, Integer.valueOf(lockStatus));
        context.getContentResolver().update(LockStatusProvider.CONTENT_URI, contentValues, null, null);
        ReloadSwitchUtils.setApplicationListNeedReload(context);
    }

    public static void addAuthSuccessPackage(Context context, String pkgName) {
        HwLog.d(TAG, "addAuthSuccessPackage:" + pkgName);
        ContentValues contentValues = new ContentValues();
        contentValues.put("packageName", pkgName);
        context.getContentResolver().insert(AuthSuccessPackageProvider.CONTENT_URI, contentValues);
    }

    public static int delAuthSuccessPackage(Context context, String pkgName) {
        return context.getContentResolver().delete(AuthSuccessPackageProvider.CONTENT_URI, "packageName = ?", new String[]{pkgName});
    }

    private static Set<String> getPackageListOfAppLockView(Context context, Uri uri, String colName) {
        Set<String> pkgSet = Sets.newHashSet();
        try {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{colName}, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    pkgSet.add(cursor.getString(0));
                }
            }
            CursorHelper.closeCursor(cursor);
            return pkgSet;
        } catch (Throwable th) {
            CursorHelper.closeCursor(null);
        }
    }
}
