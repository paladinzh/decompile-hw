package com.huawei.systemmanager.antivirus.engine.trustlook;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import java.util.Iterator;
import java.util.List;

public class TrustLookUtils {
    public static List<ApplicationInfo> queryLocalApps(Context context) {
        return filter(context.getPackageManager());
    }

    public static ScanResultEntity createScanResultEntity(PackageInfo packageInfo, String filepath, boolean isUninstalledApk) {
        ScanResultEntity entity = new ScanResultEntity(GlobalContext.getContext(), packageInfo);
        entity.apkFilePath = filepath;
        packageInfo.applicationInfo.publicSourceDir = filepath;
        packageInfo.applicationInfo.sourceDir = filepath;
        entity.isUninstalledApk = isUninstalledApk;
        entity.type = 301;
        entity.virusName = "";
        return entity;
    }

    private static List<ApplicationInfo> filter(PackageManager pm) {
        List<ApplicationInfo> pkgLists = pm.getInstalledApplications(0);
        Iterator<ApplicationInfo> iterator = pkgLists.iterator();
        while (iterator.hasNext()) {
            if ((((ApplicationInfo) iterator.next()).flags & 1) != 0) {
                iterator.remove();
            }
        }
        return pkgLists;
    }
}
