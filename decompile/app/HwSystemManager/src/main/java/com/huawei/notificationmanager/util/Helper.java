package com.huawei.notificationmanager.util;

import android.app.INotificationManager;
import android.app.INotificationManager.Stub;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import com.hsm.notificationmanager.M2NAdapter;
import com.huawei.cust.HwCustUtils;
import com.huawei.notificationmanager.common.CommonObjects.NotificationCfgInfo;
import com.huawei.notificationmanager.common.ConstValues;
import com.huawei.systemmanager.comm.grule.GRuleManager;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import com.huawei.systemmanager.util.app.HsmPkgUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Helper {
    private static final String FLAG_BACKUP_COMPELETED = "NotificationMgrBackupCompleted";
    private static final String FLAG_CFG_CHANGE = "NotificationMgrCfgChangeFlag";
    private static final String FLAG_LOG_CHANGE = "NotificationMgrLogChangeFlag";
    private static final String FLAG_RESTORE_BROKEN_NOTIFICATION = "NotificationMgrBrokenFlag";
    private static final String PREFERENCE_KEY = "com.huawei.notificationmanager.setting_preference";
    private static final String TAG = "NotificaitonManagerHelper";
    private static final HwCustHelper mHelper = ((HwCustHelper) HwCustUtils.createObj(HwCustHelper.class, new Object[0]));

    public static boolean getCfgChangeFlag(Context context) {
        return getBooleanPreference(context, FLAG_CFG_CHANGE);
    }

    public static void setCfgChangeFlag(Context context, boolean isChange) {
        setBooleanPreference(context, FLAG_CFG_CHANGE, isChange);
    }

    public static boolean isRestoreBrokenNotificationCompleted(Context context) {
        return getBooleanPreference(context, FLAG_RESTORE_BROKEN_NOTIFICATION);
    }

    public static void setRestoreBrokenNotification(Context context, boolean isFinished) {
        setBooleanPreference(context, FLAG_RESTORE_BROKEN_NOTIFICATION, isFinished);
    }

    public static boolean isCfgBackupCompleted(Context context) {
        return getBooleanPreference(context, FLAG_BACKUP_COMPELETED);
    }

    public static void setCfgBackupCompleted(Context context, boolean isCompleted) {
        setBooleanPreference(context, FLAG_BACKUP_COMPELETED, isCompleted);
    }

    public static boolean getLogChangeFlag(Context context) {
        return getBooleanPreference(context, FLAG_LOG_CHANGE);
    }

    public static void setLogChangeFlag(Context context, boolean isChange) {
        setBooleanPreference(context, FLAG_LOG_CHANGE, isChange);
    }

    private static boolean getBooleanPreference(Context context, String preKey) {
        boolean isValue = false;
        if (context == null) {
            HwLog.w(TAG, "getBooleanPreference: Invalid context");
            return isValue;
        }
        try {
            isValue = context.getSharedPreferences(PREFERENCE_KEY, 4).getBoolean(preKey, false);
        } catch (Exception e) {
            HwLog.e(TAG, "getBooleanPreference excetion,key = " + preKey, e);
        }
        return isValue;
    }

    private static void setBooleanPreference(Context context, String preKey, boolean isValue) {
        if (context == null) {
            HwLog.w(TAG, "setBooleanPreference: Invalid context");
            return;
        }
        try {
            Editor editor = context.getSharedPreferences(PREFERENCE_KEY, 4).edit();
            editor.putBoolean(preKey, isValue);
            editor.commit();
        } catch (Exception e) {
            HwLog.e(TAG, "setBooleanPreference excetion ,key = " + preKey, e);
        }
    }

    public static boolean areNotificationsEnabledForPackage(String pkgName, int uId) {
        boolean result = true;
        INotificationManager nm = Stub.asInterface(ServiceManager.getService("notification"));
        if (nm == null) {
            HwLog.e(TAG, "areNotificationsEnabledForPackage: Fail to get notification service");
            return result;
        }
        try {
            result = nm.areNotificationsEnabledForPackage(pkgName, uId);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return result;
    }

    public static boolean areNotificationHighPriority(String pkg, int uid) {
        boolean z = false;
        try {
            if (M2NAdapter.getPriority(Stub.asInterface(ServiceManager.getService("notification")), pkg, uid) == 2) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            HwLog.w(TAG, "Error calling NoMan", e);
            return false;
        }
    }

    public static boolean setHighPriority(String pkg, int uid, boolean highPriority) {
        try {
            int i;
            INotificationManager sINM = Stub.asInterface(ServiceManager.getService("notification"));
            if (highPriority) {
                i = 2;
            } else {
                i = 0;
            }
            M2NAdapter.setPriority(sINM, pkg, uid, i);
            HwLog.d(TAG, "set high priority of " + pkg + " to " + highPriority);
            return true;
        } catch (Exception e) {
            HwLog.w(TAG, "Error calling NoMan", e);
            return false;
        }
    }

    public static void setNotificationsEnabledForPackage(String pkgName, int uId, Boolean isEnable) {
        INotificationManager nm = Stub.asInterface(ServiceManager.getService("notification"));
        if (nm == null) {
            HwLog.e(TAG, "setNotificationsEnabledForPackage: Fail to get notification service");
            return;
        }
        try {
            nm.setNotificationsEnabledForPackage(pkgName, uId, isEnable.booleanValue());
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public static void reset(Map<String, NotificationCfgInfo> infos) {
        for (NotificationCfgInfo info : infos.values()) {
            String pkgName = info.mPkgName;
            if (!TextUtils.isEmpty(pkgName)) {
                int uid = HsmPkgUtils.getPackageUid(pkgName);
                if (-1 != uid) {
                    setNotificationsEnabledForPackage(pkgName, uid, Boolean.valueOf(true));
                }
            }
        }
    }

    public static String getDateTimeStyle(Context context, long time) {
        int flags;
        if (DateUtils.isToday(time)) {
            flags = 1;
        } else {
            flags = 65552;
        }
        return DateUtils.formatDateTime(context, time, flags);
    }

    public static int getNotificationCfgFromOldMask(int nOldCfg, int nOldCode) {
        if ((nOldCfg & nOldCode) == 4096) {
            return 1;
        }
        return 0;
    }

    public static ContentValues getDefaultValue(NmCenterDefValueXmlHelper parser, Context ctx, String pkg, boolean firstBoot) {
        if (parser != null) {
            ContentValues cfg = parser.getCloudPreferedDefaultConfig(ctx, pkg);
            if (cfg != null) {
                cfg.put(ConstValues.NOTIFICATION_SOUND_VIBRATE, Integer.valueOf(3));
                return cfg;
            } else if (firstBoot) {
                return null;
            }
        }
        return null;
    }

    public static List<HsmPkgInfo> getMonitoredAppList(Context context) {
        List<HsmPkgInfo> installedAppList = HsmPackageManager.getInstance().getInstalledPackages(0);
        Iterator<HsmPkgInfo> iterator = installedAppList.iterator();
        while (iterator.hasNext()) {
            HsmPkgInfo hsmPkgInfo = (HsmPkgInfo) iterator.next();
            if (!GRuleManager.getInstance().shouldMonitor(context, "notification", hsmPkgInfo.mPkgName)) {
                iterator.remove();
            } else if (mHelper != null && mHelper.isPackageDisabledForNoticationCenter(context, hsmPkgInfo.mPkgName)) {
                iterator.remove();
            }
        }
        return installedAppList;
    }

    public static ArrayList<String> getMonitoredPackageName() {
        ArrayList<String> result = new ArrayList();
        for (HsmPkgInfo info : getMonitoredAppList(GlobalContext.getContext())) {
            if (!(info == null || TextUtils.isEmpty(info.getPackageName()))) {
                result.add(info.getPackageName());
            }
        }
        return result;
    }

    public static List<HsmPkgInfo> getMonitoredUserAppList(Context context) {
        int userId = getProfileId(context);
        if (userId == UserHandle.myUserId()) {
            return null;
        }
        List<PackageInfo> AppList = com.hsm.pm.M2NAdapter.getInstalledPackagesAsUser(context.getPackageManager(), 64, userId);
        Iterator<PackageInfo> iterator = AppList.iterator();
        while (iterator.hasNext()) {
            if (!GRuleManager.getInstance().shouldMonitor(context, "notification", ((PackageInfo) iterator.next()).packageName)) {
                iterator.remove();
            }
        }
        List<HsmPkgInfo> installedUserAppList = new ArrayList();
        PackageManager pm = context.getPackageManager();
        for (PackageInfo packageInfo : AppList) {
            installedUserAppList.add(new HsmPkgInfo(packageInfo, pm));
        }
        return installedUserAppList;
    }

    public static int getProfileId(Context context) {
        UserManager um = (UserManager) context.getSystemService("user");
        if (um.getUserProfiles().size() > 1) {
            for (UserHandle userHandle : um.getUserProfiles()) {
                boolean isAFWrunning;
                int userId = userHandle.getIdentifier();
                UserInfo user = um.getUserInfo(userId);
                if (user != null) {
                    isAFWrunning = user.isManagedProfile();
                    continue;
                } else {
                    isAFWrunning = false;
                    continue;
                }
                if (isAFWrunning) {
                    return userId;
                }
            }
        }
        return UserHandle.myUserId();
    }
}
