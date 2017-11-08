package com.huawei.rcs.utils.map.abs;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import com.huawei.rcs.utils.map.impl.RcsGaodeMapLoader;
import com.huawei.rcs.utils.map.impl.RcsGoogleMapLoader;

public class RcsMapLoaderFactory {
    public static final boolean IS_CHINESE_VERSION = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    private static RcsMapLoader mLoader;

    public static synchronized RcsMapLoader getMapLoader(Context context) {
        RcsMapLoader rcsMapLoader;
        synchronized (RcsMapLoaderFactory.class) {
            if (mLoader == null) {
                if (isInChina(context)) {
                    mLoader = new RcsGaodeMapLoader();
                } else {
                    mLoader = new RcsGoogleMapLoader();
                }
            }
            rcsMapLoader = mLoader;
        }
        return rcsMapLoader;
    }

    public static boolean isInChina(Context context) {
        boolean inChina = true;
        boolean googlePlayStoreExist = false;
        String networkOperator = ((TelephonyManager) context.getSystemService("phone")).getNetworkOperator();
        if (networkOperator != null && networkOperator.trim().length() >= 3) {
            inChina = networkOperator.startsWith("460");
        }
        if (!inChina) {
            googlePlayStoreExist = isPackagesExist(context, "com.google.android.gms", "com.android.vending");
        }
        if (inChina || !r0) {
            return true;
        }
        return false;
    }

    public static boolean isPackagesExist(Context context, String... pkgs) {
        if (pkgs == null) {
            return false;
        }
        try {
            for (String pkg : pkgs) {
                context.getPackageManager().getPackageGids(pkg);
            }
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
}
