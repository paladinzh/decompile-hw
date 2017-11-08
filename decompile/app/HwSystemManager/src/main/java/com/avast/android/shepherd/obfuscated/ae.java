package com.avast.android.shepherd.obfuscated;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import java.util.LinkedHashSet;
import java.util.Set;

/* compiled from: Unknown */
class ae implements Runnable {
    final /* synthetic */ ad a;

    ae(ad adVar) {
        this.a = adVar;
    }

    public void run() {
        Set linkedHashSet = new LinkedHashSet();
        PackageManager packageManager = this.a.d.getPackageManager();
        try {
            for (PackageInfo packageInfo : packageManager.getInstalledPackages(256)) {
                try {
                    linkedHashSet.add(packageManager.getInstallerPackageName(packageInfo.packageName));
                } catch (IllegalArgumentException e) {
                }
            }
            this.a.o.a(linkedHashSet);
        } catch (Throwable th) {
        }
    }
}
