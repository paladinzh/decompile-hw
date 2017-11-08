package com.huawei.systemmanager.comm;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.os.RemoteException;
import com.huawei.systemmanager.util.HwLog;

public final class SimpleProcessObserver {
    private static final String TAG = "SimpleProcessObserver";

    public static class Stub extends android.app.IProcessObserver.Stub {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        }

        public void onProcessDied(int pid, int uid) {
        }

        public void onImportanceChanged(int pid, int uid, int importance) {
        }

        public void onProcessStateChanged(int pid, int uid, int procState) {
        }
    }

    public static void addObserver(Stub o) {
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            if (am != null) {
                am.registerProcessObserver(o);
            }
        } catch (RemoteException e) {
            HwLog.e(TAG, "unregisterObserver RemoteException!");
        }
    }

    public static void removeObserver(Stub o) {
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            if (am != null) {
                am.unregisterProcessObserver(o);
            }
        } catch (RemoteException e) {
            HwLog.e(TAG, "unregisterObserver RemoteException!");
        }
    }
}
