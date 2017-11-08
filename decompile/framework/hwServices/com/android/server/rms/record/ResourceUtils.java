package com.android.server.rms.record;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.android.server.rms.utils.Utils;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.Calendar;
import java.util.List;

public class ResourceUtils {
    private static final String TAG = "RMS.ResourceUtils";
    private static String[] defaultPermissionList = new String[]{"android.permission.CAMERA"};
    private static String[] mResourceNameList = new String[]{"NOTIFICATION", "BROADCAST", "RECEIVER", "ALARM", "APPOPS", "PROVIDER", "PIDS", "CURSOR", "APPSERVICE", "APP", "MEMORY", "CPU", "IO", "SCHEDGROUP", "ANR", "DELAY", "FRAMELOST", "IOABN_WR_UID", "IOABN_WR_TOTAL", "IOABN_DEV_LIFE_TIME_A", "IOABN_DEV_LIFE_TIME_B", "IOABN_DEV_RSV_BLK", "APPMNGMEMNORMALNATIVE", "APPMNGMEMNORMALPERS", "APPMNGWHITELIST", "CONTENTOBSERVER", "ACTIVITY", "ORDEREDBROADCAST"};

    public static long getResourceId(int callingUid, int pid, int resourceType) {
        long id = resourceType == 16 ? (((long) pid) << 32) + ((long) callingUid) : (((long) resourceType) << 32) + ((long) callingUid);
        if (Utils.DEBUG) {
            Log.d(TAG, "getResourceId  resourceType/" + resourceType + " callingUid/" + callingUid + " id/" + id);
        }
        return id;
    }

    public static String getResourceName(int resourceType) {
        if (resourceType < 10 || resourceType > 37) {
            return null;
        }
        if (resourceType - 10 >= mResourceNameList.length) {
            Log.w(TAG, "fail to get resourceName");
            return null;
        }
        String resourceName = mResourceNameList[resourceType - 10];
        if (Utils.DEBUG || Utils.HWFLOW) {
            Log.w(TAG, " getResourceName: resourceName:" + resourceName);
        }
        return resourceName;
    }

    public static int getResourcesType(String resourceName) {
        if ("pids".equals(resourceName)) {
            return 16;
        }
        return 0;
    }

    public static String composeName(String pkg, int resourceType) {
        if (pkg != null) {
            return pkg + "__" + getResourceName(resourceType);
        }
        return getResourceName(resourceType);
    }

    public static int checkSysProcPermission(int pid, int uid) {
        if (pid == Process.myPid() || uid == 0 || uid == 1000) {
            return 0;
        }
        return -1;
    }

    public static int checkAppUidPermission(int uid) {
        if (uid == 0 || uid == 1000) {
            return 0;
        }
        if (UserHandle.isIsolated(uid)) {
            return -1;
        }
        try {
            for (String s : defaultPermissionList) {
                if (ActivityManager.checkUidPermission(s, uid) == 0) {
                    return 0;
                }
            }
        } catch (Exception e) {
            Slog.e(TAG, "PackageManager exception", e);
        }
        return -1;
    }

    public static int getProcessTypeId(int callingUid, String pkg, int processTpye) {
        int typeID = processTpye;
        if (-1 == processTpye && pkg != null) {
            try {
                ApplicationInfo appInfo = AppGlobals.getPackageManager().getApplicationInfo(pkg, 0, 0);
                if (appInfo == null) {
                    Log.w(TAG, "get appInfo is null from package: " + pkg);
                    return -1;
                }
                typeID = ((appInfo.flags & 1) != 0 && (appInfo.hwFlags & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) == 0 && (appInfo.hwFlags & 67108864) == 0) ? 2 : 0;
            } catch (RemoteException e) {
                Log.w(TAG, " get PacakgeManager failed!");
            }
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "third app packageName: " + pkg + ", uid: " + callingUid + ", typeID: " + typeID);
        }
        return typeID;
    }

    public static long getAppTime(Context context, String pkg) {
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(2, -1);
        List<UsageStats> queryUsageStats = ((UsageStatsManager) context.getSystemService("usagestats")).queryUsageStats(2, calendar.getTimeInMillis(), endTime);
        if (!(queryUsageStats == null || queryUsageStats.isEmpty())) {
            for (UsageStats usageStats : queryUsageStats) {
                if (pkg != null && pkg.equals(usageStats.getPackageName())) {
                    return usageStats.getTotalTimeInForeground();
                }
            }
        }
        return 0;
    }
}
