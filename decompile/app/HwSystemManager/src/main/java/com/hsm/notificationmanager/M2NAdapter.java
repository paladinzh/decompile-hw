package com.hsm.notificationmanager;

import android.app.INotificationManager;
import android.content.pm.ApplicationInfo;
import android.os.RemoteException;
import android.support.v4.app.NotificationManagerCompat;
import java.util.List;

public class M2NAdapter {
    public static int getPriority(INotificationManager inm, String pkg, int uid) throws RemoteException {
        return inm.getPriority(pkg, uid);
    }

    public static void setPriority(INotificationManager inm, String pkg, int uid, int priority) throws RemoteException {
        inm.setPriority(pkg, uid, priority);
    }

    public static int getVisibilityOverride(INotificationManager inm, String pkg, int uid) throws RemoteException {
        return inm.getVisibilityOverride(pkg, uid);
    }

    public static void setVisibilityOverride(INotificationManager inm, String pkg, int uid, int visibility) throws RemoteException {
        inm.setVisibilityOverride(pkg, uid, visibility);
    }

    public static void setPeekable(INotificationManager inm, String pkg, int uid, boolean peekable) throws RemoteException {
    }

    public static boolean getPeekable(INotificationManager inm, String pkg, int uid) throws RemoteException {
        return false;
    }

    public static void reStoreBrokenApps(INotificationManager inm, List<ApplicationInfo> pkgLists) throws RemoteException {
        for (ApplicationInfo appInfo : pkgLists) {
            int importance = inm.getImportance(appInfo.packageName, appInfo.uid);
            if (importance == 3 || importance == 4) {
                inm.setImportance(appInfo.packageName, appInfo.uid, NotificationManagerCompat.IMPORTANCE_UNSPECIFIED);
            }
        }
    }
}
