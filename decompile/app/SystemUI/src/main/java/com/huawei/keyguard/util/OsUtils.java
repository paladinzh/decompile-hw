package com.huawei.keyguard.util;

import android.app.ActivityManagerNative;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import com.huawei.keyguard.GlobalContext;
import java.util.List;

public class OsUtils {
    private static UserHandle sCurrent = UserHandle.OWNER;
    private static long sCurrentSN = -1;
    private static UserManager sUserManager = null;

    public static synchronized void setCurrentUser(int currentUser) {
        synchronized (OsUtils.class) {
            if (sCurrent.getIdentifier() == currentUser) {
                return;
            }
            HwLog.w("KG_OsUtils", "setCurrentUser from " + sCurrent.getIdentifier() + " to " + currentUser);
            sCurrent = new UserHandle(currentUser);
            sCurrentSN = -1;
        }
    }

    public static boolean isOwner() {
        return getCurrentUser() == 0;
    }

    public static synchronized int getCurrentUser() {
        int identifier;
        synchronized (OsUtils.class) {
            identifier = sCurrent.getIdentifier();
        }
        return identifier;
    }

    public static synchronized long getCurrentUserSN() {
        long j;
        synchronized (OsUtils.class) {
            if (sCurrentSN == -1) {
                sCurrentSN = getUserSN(getCurrentUser());
            }
            j = sCurrentSN;
        }
        return j;
    }

    public static synchronized long getUserSN(int uId) {
        synchronized (OsUtils.class) {
            if (sUserManager == null) {
                sUserManager = (UserManager) GlobalContext.getContext().getSystemService("user");
                if (sUserManager == null) {
                    return -1;
                }
            }
            long userSerialNumber = (long) sUserManager.getUserSerialNumber(uId);
            return userSerialNumber;
        }
    }

    public static void startUserActivity(Context context, Intent intent) {
        try {
            context.startActivityAsUser(intent, UserHandle.CURRENT);
        } catch (ActivityNotFoundException e) {
            HwLog.e("KG_OsUtils", "transitionToCamera:startActivity failed", e);
        }
    }

    public static void sendUserBroadcastWithPermission(Context context, Intent intent, String permission) {
        context.sendBroadcastAsUser(intent, UserHandle.CURRENT, permission);
    }

    public static int getSecureInt(Context context, String name, int def) {
        return getSecureInt(context, name, def, getCurrentUser());
    }

    public static int getSecureInt(Context context, String name, int def, int uid) {
        return Secure.getIntForUser(context.getContentResolver(), name, def, uid);
    }

    public static boolean putSecureInt(Context context, String name, int val) {
        return Secure.putIntForUser(context.getContentResolver(), name, val, getCurrentUser());
    }

    public static int getSystemInt(Context context, String name, int def) {
        return System.getIntForUser(context.getContentResolver(), name, def, getCurrentUser());
    }

    public static String getSystemString(Context context, String name) {
        return System.getStringForUser(context.getContentResolver(), name, getCurrentUser());
    }

    public static boolean putSystemInt(Context context, String name, int val) {
        return System.putIntForUser(context.getContentResolver(), name, val, getCurrentUser());
    }

    public static boolean putSystemString(Context context, String name, String val) {
        return System.putStringForUser(context.getContentResolver(), name, val, getCurrentUser());
    }

    public static void registerContentObserver(Context context, Uri uri, boolean notifyForDescendents, ContentObserver observer) {
        try {
            context.getContentResolver().registerContentObserver(uri, notifyForDescendents, observer, -1);
        } catch (SecurityException e) {
            HwLog.e("KG_OsUtils", "registerContentObserver fail for " + uri, e);
        }
    }

    public static void registerSystemObserver(Context context, String strUri, boolean notifyForDescendents, ContentObserver observer) {
        context.getContentResolver().registerContentObserver(System.getUriFor(strUri), notifyForDescendents, observer, -1);
    }

    public static int getGlobalInt(Context context, String name, int def) {
        return Global.getInt(context.getContentResolver(), name, def);
    }

    public static final boolean isSupportMultiUser(Context context) {
        boolean z = true;
        if (!UserManager.supportsMultipleUsers()) {
            return false;
        }
        UserManager userManager = (UserManager) context.getSystemService("user");
        if (userManager == null || userManager.getUserCount() <= 1) {
            z = false;
        }
        return z;
    }

    public static final boolean switchUser(int id) {
        boolean ret = false;
        try {
            ret = ActivityManagerNative.getDefault().switchUser(id);
        } catch (RemoteException re) {
            HwLog.e("KG_OsUtils", "Couldn't switch user " + re);
        }
        if (ret) {
            setCurrentUser(id);
        }
        return ret;
    }

    public static final List<UserInfo> getAllUsers(Context context) {
        UserManager um = (UserManager) context.getSystemService("user");
        if (um != null) {
            return um.getUsers(true);
        }
        HwLog.e("KG_OsUtils", "get users fail");
        return null;
    }

    public static final Uri getUserUri(String content) {
        StringBuilder sb = new StringBuilder("content://");
        int uid = getCurrentUser();
        if (uid != 0) {
            sb.append(uid).append("@");
        }
        sb.append(content);
        return Uri.parse(sb.toString());
    }
}
