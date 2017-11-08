package com.huawei.gallery.map.app;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import com.huawei.gallery.map.amap.GaoDeMapFragment;
import com.huawei.gallery.map.google.GoogleMapFragment;

public abstract class MapFragmentFactory {
    public static final boolean IS_CHINESE_VERSION = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    private static boolean LIB_READY;

    static {
        LIB_READY = false;
        try {
            if (IS_CHINESE_VERSION) {
                System.loadLibrary("gdinamapv4sdk752");
                System.loadLibrary("gdinamapv4sdk752ex");
            }
            LIB_READY = true;
        } catch (UnsatisfiedLinkError e) {
        }
    }

    public static MapFragmentBase create(Context context) {
        return shouldUseGaoDeMapFragment(context) ? new GaoDeMapFragment() : new GoogleMapFragment();
    }

    public static boolean shouldUseGaoDeMapFragment(Context context) {
        boolean inChina = true;
        boolean googlePlayStoreExist = false;
        String networkOperator = ((TelephonyManager) context.getSystemService("phone")).getNetworkOperator();
        if (networkOperator != null && networkOperator.trim().length() >= 3) {
            inChina = !networkOperator.startsWith("99999") ? networkOperator.startsWith("460") : true;
        }
        if (!inChina) {
            googlePlayStoreExist = MapUtils.isPackagesExist(context, "com.google.android.gms", "com.android.vending");
        }
        if (!IS_CHINESE_VERSION || (!inChina && r0)) {
            return false;
        }
        return true;
    }

    public static boolean isMapReady() {
        return LIB_READY;
    }
}
