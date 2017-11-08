package com.huawei.powergenie.core.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.util.Log;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public final class AppInfoRecord {
    private final HashSet<String> mAllProcessNames = new HashSet();
    private boolean mAllowUninstalled = true;
    private long mFirstInstallTime;
    private int mHostUid = -1;
    private boolean mIsHideApp = false;
    private boolean mIsHostOwner = true;
    private boolean mIsSystemApp = true;
    private long mLastUpdateTime;
    private final String mPkgName;
    private int mProvidersNum = 0;
    private int mReceiversNum = 0;
    private int mServicesNum = 0;
    private String mSharedUserId;
    private final HashMap<Integer, Integer> mUserIdMapUid = new HashMap();

    public AppInfoRecord(Context context, String pkg, int curUserId) {
        int i = 0;
        this.mPkgName = pkg;
        PackageManager pm = context.getPackageManager();
        PackageInfo pi = getPkgInfo(pm, pkg, curUserId);
        if (pi != null) {
            int i2;
            boolean z;
            this.mServicesNum = pi.services == null ? 0 : pi.services.length;
            this.mProvidersNum = pi.providers == null ? 0 : pi.providers.length;
            if (pi.receivers == null) {
                i2 = 0;
            } else {
                i2 = pi.receivers.length;
            }
            this.mReceiversNum = i2;
            this.mSharedUserId = pi.sharedUserId;
            this.mFirstInstallTime = pi.firstInstallTime;
            this.mLastUpdateTime = pi.lastUpdateTime;
            ApplicationInfo applicationInfo = pi.applicationInfo;
            if (applicationInfo != null) {
                if (this.mIsHostOwner) {
                    this.mHostUid = applicationInfo.uid;
                    this.mUserIdMapUid.put(Integer.valueOf(0), Integer.valueOf(applicationInfo.uid));
                } else {
                    this.mHostUid = -1;
                    this.mUserIdMapUid.put(Integer.valueOf(curUserId), Integer.valueOf(applicationInfo.uid));
                }
                this.mIsSystemApp = isSystemApp(applicationInfo);
                this.mAllowUninstalled = isAllowedUninstallPkg(applicationInfo);
            }
            if (hasLauncherIcon(pm, pkg, curUserId)) {
                z = false;
            } else {
                z = true;
            }
            this.mIsHideApp = z;
            if (pi.applicationInfo != null) {
                if (pi.applicationInfo.processName != null) {
                    this.mAllProcessNames.add(pi.applicationInfo.processName);
                } else {
                    this.mAllProcessNames.add(pkg);
                }
            }
            if (pi.activities != null) {
                for (ActivityInfo info : pi.activities) {
                    if (info.processName != null) {
                        this.mAllProcessNames.add(info.processName);
                    }
                }
            }
            if (pi.services != null) {
                for (ServiceInfo info2 : pi.services) {
                    if (info2.processName != null) {
                        this.mAllProcessNames.add(info2.processName);
                    }
                }
            }
            if (pi.providers != null) {
                ProviderInfo[] providerInfoArr = pi.providers;
                int length = providerInfoArr.length;
                while (i < length) {
                    ProviderInfo info3 = providerInfoArr[i];
                    if (info3.processName != null) {
                        this.mAllProcessNames.add(info3.processName);
                    }
                    i++;
                }
            }
        }
    }

    private boolean isSystemApp(ApplicationInfo info) {
        if ((info.flags & 1) == 0 && (info.flags & 128) == 0) {
            return false;
        }
        return true;
    }

    private boolean hasLauncherIcon(PackageManager pm, String appPkg, int curUserId) {
        Intent verification = new Intent("android.intent.action.MAIN");
        verification.addCategory("android.intent.category.LAUNCHER");
        verification.setPackage(appPkg);
        List<ResolveInfo> findActivities = pm.queryIntentActivitiesAsUser(verification, 786432, curUserId);
        if (findActivities == null || findActivities.size() <= 0) {
            return false;
        }
        return true;
    }

    private boolean isAllowedUninstallPkg(ApplicationInfo applicationInfo) {
        if (!isSystemApp(applicationInfo)) {
            return true;
        }
        if (applicationInfo.sourceDir == null || !applicationInfo.sourceDir.contains("/system/delapp")) {
            return false;
        }
        return true;
    }

    private PackageInfo getPkgInfo(PackageManager pm, String pkg, int curUserId) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo(pkg, 14);
            this.mIsHostOwner = true;
        } catch (NameNotFoundException e) {
            Log.w("AppInfoRecord", "in host ower not found package:" + pkg);
        }
        if (packageInfo != null) {
            return packageInfo;
        }
        try {
            packageInfo = pm.getPackageInfoAsUser(pkg, 14, curUserId);
            this.mIsHostOwner = false;
            return packageInfo;
        } catch (NameNotFoundException e2) {
            Log.e("AppInfoRecord", "missing package:" + pkg + " cur user:" + curUserId);
            return packageInfo;
        }
    }

    public boolean hasProcName(String procName) {
        return procName != null ? this.mAllProcessNames.contains(procName) : false;
    }

    public boolean isHostOwerContain() {
        return this.mIsHostOwner;
    }

    public int getHostUid() {
        return this.mHostUid;
    }

    public int getUserUid(int userId) {
        Integer uid = (Integer) this.mUserIdMapUid.get(Integer.valueOf(userId));
        return uid == null ? -1 : uid.intValue();
    }

    public String toString() {
        return "AppInfoRecord{ " + this.mPkgName + " host_uid:" + this.mHostUid + " system:" + this.mIsSystemApp + " hide:" + this.mIsHideApp + " uninstall:" + this.mAllowUninstalled + " shareUid:" + this.mSharedUserId + "}";
    }
}
