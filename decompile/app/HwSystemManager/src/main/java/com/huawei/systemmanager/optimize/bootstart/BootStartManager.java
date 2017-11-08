package com.huawei.systemmanager.optimize.bootstart;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import com.huawei.systemmanager.comm.grule.GRuleManager;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorScenario;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.optimize.base.Const;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import com.huawei.systemmanager.util.procpolicy.ProcessPolicy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BootStartManager {
    public static final boolean ALLOW = true;
    private static final String BATCH_DELETE_THREAD = "batch_delete_thread";
    private static final String TAG = "BootStartManager";
    private static BootStartManager sInstance;
    private Context mContext = null;

    public BootStartManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static synchronized BootStartManager getInstance(Context context) {
        BootStartManager bootStartManager;
        synchronized (BootStartManager.class) {
            if (sInstance == null) {
                sInstance = new BootStartManager(context);
            }
            bootStartManager = sInstance;
        }
        return bootStartManager;
    }

    public void onCreate() {
        if (CustomizeWrapper.isBootstartupEnabled() && new ProcessPolicy().shouldCheckConsisteny()) {
            checkConsisteny();
        }
    }

    public void checkConsisteny() {
    }

    private Iterator<ResolveInfo> getStartUpAppInfo() {
        try {
            PackageManager packageManager = this.mContext.getPackageManager();
            List<ResolveInfo> packages = PackageManagerWrapper.queryBroadcastReceivers(packageManager, new Intent("android.intent.action.BOOT_COMPLETED"), 512);
            packages.addAll(PackageManagerWrapper.queryBroadcastReceivers(packageManager, new Intent("android.net.conn.CONNECTIVITY_CHANGE"), 514));
            return packages.iterator();
        } catch (Exception e) {
            HwLog.d(TAG, "getStartUpAppInfo exception", e);
            return new ArrayList().iterator();
        }
    }

    public boolean isStartUpApp(String packageName) {
        List<String> startUppckNamelist = new ArrayList();
        Iterator<ResolveInfo> StartUpAppInfoIterator = getStartUpAppInfo();
        while (StartUpAppInfoIterator.hasNext()) {
            ComponentInfo componentInfo;
            ResolveInfo app = (ResolveInfo) StartUpAppInfoIterator.next();
            if (app.activityInfo != null) {
                componentInfo = app.activityInfo;
            } else {
                componentInfo = app.serviceInfo;
            }
            String appPackageName = componentInfo.packageName;
            if (GRuleManager.getInstance().shouldMonitor(this.mContext, MonitorScenario.SCENARIO_BOOTSTART, appPackageName) && !startUppckNamelist.contains(appPackageName)) {
                startUppckNamelist.add(appPackageName);
            }
        }
        return startUppckNamelist.contains(packageName);
    }

    public void installApp(String packageName) {
    }

    public ArrayList<String> getForbidAppListInTable() {
        return null;
    }

    public ArrayList<String> getAllowAppListInTable() {
        return null;
    }

    public void updateStartupDBWhenRemove(String packageName) {
        deleteFromDB(Const.START_UP_FORBIDDEN_APPS_URI, packageName);
        deleteFromDB(Const.STARTUP_ALLOW_APPS_URI, packageName);
    }

    private void deleteFromDB(Uri uri, String packageName) {
        HwLog.d(TAG, "deleteFromDB and  packageName is !" + packageName);
        this.mContext.getContentResolver().delete(uri, Const.START_UP_SELECTION_PACKAGE, new String[]{packageName});
    }

    public void updateStartupDBWhenRemove(String[] packageNameList) {
    }
}
