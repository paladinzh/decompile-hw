package com.huawei.notificationmanager.common;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.UserHandle;
import com.huawei.systemmanager.comm.grule.rules.appflag.UnRemovableAppRule;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import com.huawei.systemmanager.util.app.HsmPkgUtils;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.List;

public class NotificationUtils {
    private static final String BACKGROUND_APP_MAIN_VALUE = "mainvalue";
    private static final String BACKGROUND_APP_NAME = "appname";
    private static final String BACKGROUND_APP_PEEKABLE_VALUE = "Peekablevalue";
    private static final String BACKGROUND_APP_PRIORITY_VALUE = "Priorityvalue";
    private static final String BACKGROUND_APP_SENSITIVE_VALUE = "Sensitivevalue";
    private static final String TAG = "NotificationUtils";
    private static PackageManager sPm = GlobalContext.getContext().getPackageManager();
    private static Signature[] sSystemSignature = new Signature[]{getSystemSignature()};
    private static final UnRemovableAppRule sUnRemovableAppRule = new UnRemovableAppRule();

    private static Signature getFirstSignature(PackageInfo pkg) {
        if (pkg == null || pkg.signatures == null || pkg.signatures.length <= 0) {
            return null;
        }
        return pkg.signatures[0];
    }

    private static Signature getSystemSignature() {
        try {
            return getFirstSignature(PackageManagerWrapper.getPackageInfo(sPm, "android", 64));
        } catch (Exception e) {
            HwLog.i(TAG, "exception in getSystemSignature");
            return null;
        }
    }

    private static boolean isSystemPackage(PackageInfo pkg) {
        if (sSystemSignature == null || sSystemSignature[0] == null) {
            return false;
        }
        return sSystemSignature[0].equals(getFirstSignature(pkg));
    }

    private static PackageInfo findPackageInfo(String pkg, int uid) {
        String[] packages = sPm.getPackagesForUid(uid);
        if (!(packages == null || pkg == null)) {
            int N = packages.length;
            int i = 0;
            while (i < N) {
                if (pkg.equals(packages[i])) {
                    try {
                        return PackageManagerWrapper.getPackageInfo(sPm, pkg, 64);
                    } catch (Exception e) {
                        HwLog.w(TAG, "Failed to load package " + pkg, e);
                    }
                } else {
                    i++;
                }
            }
        }
        return null;
    }

    public static boolean isAppCanForbid(String pkg, int uid) {
        boolean z = true;
        PackageInfo info = findPackageInfo(pkg, uid);
        if (info == null) {
            return true;
        }
        if (isSystemPackage(info)) {
            z = false;
        }
        return z;
    }

    public static Cursor getAndroidNotificationCursor() {
        List<HsmPkgInfo> apps = HsmPackageManager.getInstance().getAllPackages();
        if (Utility.isNullOrEmptyList(apps)) {
            return null;
        }
        NotificationBackend backend = new NotificationBackend();
        MatrixCursor cursor = new MatrixCursor(new String[]{"appname", BACKGROUND_APP_MAIN_VALUE, BACKGROUND_APP_PRIORITY_VALUE, BACKGROUND_APP_PEEKABLE_VALUE, BACKGROUND_APP_SENSITIVE_VALUE});
        for (HsmPkgInfo appInfo : apps) {
            cursor.addRow(new Object[]{appInfo.mPkgName, String.valueOf(backend.getNotificationsBanned(appInfo.mPkgName, appInfo.mUid)), String.valueOf(backend.getHighPriority(appInfo.mPkgName, appInfo.mUid)), String.valueOf(backend.getPeekable(appInfo.mPkgName, appInfo.mUid)), String.valueOf(backend.getSensitive(appInfo.mPkgName, appInfo.mUid))});
        }
        HwLog.i(TAG, "getAndroidNotificationCursor, size is " + cursor.getCount());
        return cursor;
    }

    public static void setAndroidNotification(ContentValues value) {
        if (value == null) {
            HwLog.w(TAG, "setAndroidNotification ,  Invalid values");
            return;
        }
        String packageName = value.getAsString("appname");
        int uid = HsmPkgUtils.getPackageUid(packageName);
        if (-1 == uid) {
            HwLog.w(TAG, "setAndroidNotification ,  packageName:  " + packageName + "   uid is invalid");
            return;
        }
        Boolean mainChecked = value.getAsBoolean(BACKGROUND_APP_MAIN_VALUE);
        Boolean priorityChecked = value.getAsBoolean(BACKGROUND_APP_PRIORITY_VALUE);
        Boolean peekableChecked = value.getAsBoolean(BACKGROUND_APP_PEEKABLE_VALUE);
        Integer sensitive = value.getAsInteger(BACKGROUND_APP_SENSITIVE_VALUE);
        NotificationBackend backend = new NotificationBackend();
        backend.setNotificationsBanned(packageName, uid, mainChecked.booleanValue());
        backend.setHighPriority(packageName, uid, priorityChecked.booleanValue());
        backend.setPeekable(packageName, uid, peekableChecked.booleanValue());
        if (sensitive != null) {
            backend.setSensitive(packageName, uid, sensitive.intValue());
        }
    }

    public static int setAndroidNotifications(ContentValues[] value) {
        if (value == null) {
            HwLog.w(TAG, "setAndroidNotification ,  Invalid values");
            return 1;
        }
        for (ContentValues v : value) {
            setAndroidNotification(v);
        }
        return 1;
    }

    public static void notifyCfgChange(Context context, boolean fromNotificationSettings) {
        if (context == null) {
            HwLog.e(TAG, "notifyCfgChange context is null, fromNotificationSettings=" + fromNotificationSettings);
            return;
        }
        Intent intent = new Intent(fromNotificationSettings ? ConstValues.CFG_CHANGE_INTENT : ConstValues.CFG_CHANGE_BACKGROUND_INTENT);
        intent.putExtra(ConstValues.CFG_CHANGE_VALUE, true);
        intent.setPackage(context.getPackageName());
        GlobalContext.getContext().sendBroadcastAsUser(intent, UserHandle.CURRENT, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
    }

    public static boolean isSystemRemovable(String pkgName) {
        return sUnRemovableAppRule.match(null, pkgName);
    }
}
