package com.android.contacts.compatibility;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import com.android.contacts.list.DirectorySearchManager;

public class ProviderFeatureChecker {
    private static volatile ProviderFeatureChecker sChecker;
    private boolean isHAPProvider;
    private boolean isSupportDualSim;

    private ProviderFeatureChecker() {
    }

    public static ProviderFeatureChecker getInstance(Context c) {
        if (sChecker == null) {
            synchronized (ProviderFeatureChecker.class) {
                if (sChecker == null) {
                    sChecker = new ProviderFeatureChecker();
                    sChecker.init(c);
                }
            }
        }
        return sChecker;
    }

    public static void refreshInstance(Context c) {
        if (sChecker != null) {
            synchronized (ProviderFeatureChecker.class) {
                sChecker.init(c);
            }
        }
    }

    private void init(Context c) {
        PackageManager pm = c.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo("com.android.providers.contacts", 16384);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null || !packageInfo.versionName.startsWith("Huawei")) {
            this.isHAPProvider = false;
            this.isSupportDualSim = false;
        } else {
            this.isHAPProvider = true;
            this.isSupportDualSim = true;
        }
        DirectorySearchManager.updateW3PackageInfo(pm);
    }

    public boolean isSupportDualSim() {
        return this.isSupportDualSim;
    }

    public boolean isHAPProviderInstalled() {
        return this.isHAPProvider;
    }
}
