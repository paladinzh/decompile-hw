package com.avast.android.sdk.engine.obfuscated;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import java.util.Iterator;
import java.util.List;

/* compiled from: Unknown */
public class bi {
    public static List<ApplicationInfo> a(PackageManager packageManager, String... strArr) {
        List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(0);
        Iterator it = installedApplications.iterator();
        while (it.hasNext()) {
            ApplicationInfo applicationInfo = (ApplicationInfo) it.next();
            if (applicationInfo.sourceDir.startsWith("/system")) {
                it.remove();
            } else if (strArr != null) {
                for (String str : strArr) {
                    if (applicationInfo.packageName.equals(str)) {
                        ao.a("Skipping: " + str);
                        it.remove();
                        break;
                    }
                }
            }
        }
        return installedApplications;
    }
}
