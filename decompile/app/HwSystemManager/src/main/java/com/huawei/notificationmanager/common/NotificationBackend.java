package com.huawei.notificationmanager.common;

import android.app.INotificationManager;
import android.app.INotificationManager.Stub;
import android.os.ServiceManager;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import com.hsm.notificationmanager.M2NAdapter;

public class NotificationBackend {
    private static final String TAG = "NotificationBackend";
    static INotificationManager sINM = Stub.asInterface(ServiceManager.getService("notification"));

    public boolean setNotificationsBanned(String pkg, int uid, boolean banned) {
        try {
            sINM.setNotificationsEnabledForPackage(pkg, uid, banned);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return false;
        }
    }

    public boolean getNotificationsBanned(String pkg, int uid) {
        try {
            return sINM.areNotificationsEnabledForPackage(pkg, uid);
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return false;
        }
    }

    public boolean getHighPriority(String pkg, int uid) {
        boolean z = false;
        try {
            if (M2NAdapter.getPriority(sINM, pkg, uid) == 2) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return false;
        }
    }

    public boolean setHighPriority(String pkg, int uid, boolean highPriority) {
        try {
            int i;
            INotificationManager iNotificationManager = sINM;
            if (highPriority) {
                i = 2;
            } else {
                i = 0;
            }
            M2NAdapter.setPriority(iNotificationManager, pkg, uid, i);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return false;
        }
    }

    public boolean getPeekable(String pkg, int uid) {
        try {
            return M2NAdapter.getPeekable(sINM, pkg, uid);
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return false;
        }
    }

    public boolean setPeekable(String pkg, int uid, boolean peekable) {
        try {
            M2NAdapter.setPeekable(sINM, pkg, uid, peekable);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return false;
        }
    }

    public int getSensitive(String pkg, int uid) {
        try {
            return M2NAdapter.getVisibilityOverride(sINM, pkg, uid);
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return NotificationManagerCompat.IMPORTANCE_UNSPECIFIED;
        }
    }

    public boolean setSensitive(String pkg, int uid, boolean showInLockscreen, boolean hideContent) {
        int visibility = showInLockscreen ? hideContent ? 0 : NotificationManagerCompat.IMPORTANCE_UNSPECIFIED : -1;
        return setSensitive(pkg, uid, visibility);
    }

    public boolean setSensitive(String pkg, int uid, int visibility) {
        try {
            M2NAdapter.setVisibilityOverride(sINM, pkg, uid, visibility);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return false;
        }
    }
}
