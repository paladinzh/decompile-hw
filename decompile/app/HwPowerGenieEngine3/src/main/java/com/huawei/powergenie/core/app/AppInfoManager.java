package com.huawei.powergenie.core.app;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import com.huawei.powergenie.api.IAppType;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.core.policy.DBWrapper;
import com.huawei.powergenie.core.policy.DBWrapper.AppInfoItem;
import com.huawei.powergenie.core.policy.IntelligentProvider;
import com.huawei.powergenie.core.policy.SharedPref;
import java.util.ArrayList;
import java.util.HashMap;

public final class AppInfoManager {
    private AppInfoHandler mAppInfoHandler;
    private final AppManager mAppManager;
    private final HashMap<String, AppInfoRecord> mAppsInfoList = new HashMap();
    private Context mContext;
    private final DBWrapper mDBWrapper;
    private final IAppType mIAppType;

    private final class AppInfoHandler extends Handler {
        public AppInfoHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    AppInfoManager.this.initAppsInfo();
                    return;
                case 102:
                    String pkgName = msg.obj;
                    if (pkgName == null || AppInfoManager.this.isAppInfoInDB(pkgName)) {
                        Log.i("AppInfoManager", "app info is exist in db about pkg: " + pkgName);
                        return;
                    } else {
                        AppInfoManager.this.writeAppInfoToDB(pkgName);
                        return;
                    }
                case 103:
                    String pkg = msg.obj;
                    AppInfoManager.this.deleteAppInfoFromDB(pkg);
                    AppInfoManager.this.mAppsInfoList.remove(pkg);
                    return;
                default:
                    return;
            }
        }
    }

    public AppInfoManager(ICoreContext coreContext, AppManager appManager) {
        this.mAppManager = appManager;
        this.mIAppType = (IAppType) coreContext.getService("appmamager");
        this.mDBWrapper = new DBWrapper(coreContext.getContext());
        this.mContext = coreContext.getContext();
        HandlerThread thread = new HandlerThread("AppInfoHandler", 10);
        thread.start();
        this.mAppInfoHandler = new AppInfoHandler(thread.getLooper());
    }

    protected void handleBootComplete() {
        if (!SharedPref.getSettings(this.mContext, "init_apps_finish", false)) {
            this.mAppInfoHandler.sendMessageDelayed(this.mAppInfoHandler.obtainMessage(101), 0);
        }
    }

    protected void handlePackageState(boolean added, String pkgName) {
        if (pkgName != null) {
            this.mAppInfoHandler.sendMessageDelayed(this.mAppInfoHandler.obtainMessage(added ? 102 : 103, pkgName), 0);
        } else {
            Log.w("AppInfoManager", "package name is null...");
        }
    }

    private void initAppsInfo() {
        Log.i("AppInfoManager", "Init apps info ...");
        long start = SystemClock.elapsedRealtime();
        ArrayList<String> allAppsList = this.mAppManager.getAllApps(this.mContext);
        if (allAppsList != null && allAppsList.size() > 0) {
            IntelligentProvider.beginTransaction();
            for (String pName : allAppsList) {
                writeAppInfoToDB(pName);
            }
            IntelligentProvider.setTransactionSuccessful();
            IntelligentProvider.endTransaction();
            Log.i("AppInfoManager", "spent: " + (SystemClock.elapsedRealtime() - start) + "ms for pkgs num:" + allAppsList.size());
            SharedPref.updateSettings(this.mContext, "init_apps_finish", true);
        }
    }

    private void writeAppInfoToDB(String pName) {
        if (pName == null) {
            Log.w("AppInfoManager", "the package is not exist.");
            return;
        }
        int uid = this.mAppManager.getUidByPkg(pName);
        if (UserHandle.getAppId(uid) >= 10000) {
            AppInfoItem item = getAppInfo(pName);
            Log.i("AppInfoManager", "Write app info to db : " + pName + " type: " + item.appType + " uid:" + uid);
            this.mDBWrapper.addAppInfo(item);
        }
    }

    private void deleteAppInfoFromDB(String pName) {
        if (pName == null) {
            Log.w("AppInfoManager", "the package is not exist.");
            return;
        }
        if (UserHandle.getAppId(this.mAppManager.getUidByPkgFromOwner(pName)) >= 10000) {
            Log.i("AppInfoManager", "not delete app info from db : " + pName + " because app exists in other Users");
        } else {
            Log.i("AppInfoManager", "Delete app info from db : " + pName);
            this.mDBWrapper.deleteAppInfo(pName);
        }
    }

    private boolean isAppInfoInDB(String pkg) {
        return this.mDBWrapper.hasAppInfo(pkg);
    }

    private AppInfoItem getAppInfo(String pName) {
        int i = 1;
        AppInfoItem item = new AppInfoItem();
        item.appName = pName;
        item.signature = this.mAppManager.getSignature(this.mContext, pName);
        item.sysApp = this.mAppManager.isSystemApp(this.mContext, pName) ? 1 : 0;
        if (!this.mAppManager.hasLauncherIcon(this.mContext, pName)) {
            i = 0;
        }
        item.hasIcon = i;
        item.useHardware = 0;
        item.appType = this.mIAppType.getAppType(pName);
        item.ownerCom = -1;
        return item;
    }

    protected AppInfoRecord getAppInfoRecord(String pkg) {
        if (pkg == null) {
            return null;
        }
        AppInfoRecord appInfo = (AppInfoRecord) this.mAppsInfoList.get(pkg);
        if (appInfo == null) {
            appInfo = new AppInfoRecord(this.mContext, pkg, this.mAppManager.getCurUserId());
            this.mAppsInfoList.put(pkg, appInfo);
        }
        return appInfo;
    }
}
