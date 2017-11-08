package com.android.settingslib;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.MSimTelephonyManager;
import fyusion.vislib.BuildConfig;
import java.text.NumberFormat;

public class Utils {
    public static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", BuildConfig.FLAVOR));
    private static String sPermissionControllerPackageName;
    private static String sServicesSystemSharedLibPackageName;
    private static String sSharedSystemSharedLibPackageName;
    private static Signature[] sSystemSignature;

    public static String formatPercentage(int percentage) {
        return formatPercentage(((double) percentage) / 100.0d);
    }

    private static String formatPercentage(double percentage) {
        return NumberFormat.getPercentInstance().format(percentage);
    }

    public static int getBatteryLevel(Intent batteryChangedIntent) {
        int level = batteryChangedIntent.getIntExtra("level", 0);
        return (level * 100) / batteryChangedIntent.getIntExtra("scale", 100);
    }

    public static String getBatteryStatus(Resources res, Intent batteryChangedIntent, boolean shortString) {
        int plugType = batteryChangedIntent.getIntExtra("plugged", 0);
        int status = batteryChangedIntent.getIntExtra("status", 1);
        if (status == 2) {
            int resId;
            if (plugType == 1) {
                if (shortString) {
                    resId = R$string.battery_info_status_charging_ac_short;
                } else {
                    resId = R$string.battery_info_status_charging_ac;
                }
            } else if (plugType == 2) {
                if (shortString) {
                    resId = R$string.battery_info_status_charging_usb_short;
                } else {
                    resId = R$string.battery_info_status_charging_usb;
                }
            } else if (plugType != 4) {
                resId = R$string.battery_info_status_charging;
            } else if (shortString) {
                resId = R$string.battery_info_status_charging_wireless_short;
            } else {
                resId = R$string.battery_info_status_charging_wireless;
            }
            return res.getString(resId);
        } else if (status == 3) {
            return res.getString(R$string.battery_info_status_discharging);
        } else {
            if (status == 4) {
                return res.getString(R$string.battery_info_status_not_charging);
            }
            if (status == 5) {
                return res.getString(R$string.battery_info_status_full);
            }
            return res.getString(R$string.battery_info_status_unknown);
        }
    }

    public static boolean isSystemPackage(PackageManager pm, PackageInfo pkg) {
        if (sSystemSignature == null) {
            sSystemSignature = new Signature[]{getSystemSignature(pm)};
        }
        if (sPermissionControllerPackageName == null) {
            sPermissionControllerPackageName = pm.getPermissionControllerPackageName();
        }
        if (sServicesSystemSharedLibPackageName == null) {
            sServicesSystemSharedLibPackageName = pm.getServicesSystemSharedLibraryPackageName();
        }
        if (sSharedSystemSharedLibPackageName == null) {
            sSharedSystemSharedLibPackageName = pm.getSharedSystemSharedLibraryPackageName();
        }
        if ((sSystemSignature[0] != null && sSystemSignature[0].equals(getFirstSignature(pkg))) || pkg.packageName.equals(sPermissionControllerPackageName) || pkg.packageName.equals(sServicesSystemSharedLibPackageName)) {
            return true;
        }
        return pkg.packageName.equals(sSharedSystemSharedLibPackageName);
    }

    private static Signature getFirstSignature(PackageInfo pkg) {
        if (pkg == null || pkg.signatures == null || pkg.signatures.length <= 0) {
            return null;
        }
        return pkg.signatures[0];
    }

    private static Signature getSystemSignature(PackageManager pm) {
        try {
            return getFirstSignature(pm.getPackageInfo("android", 64));
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public static boolean isMultiSimEnabled() {
        try {
            return MSimTelephonyManager.getDefault().isMultiSimEnabled();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isChinaArea() {
        return SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    }

    public static boolean isChinaTelecomArea() {
        return SystemProperties.get("ro.config.hw_opta", "0").equals("92") ? isChinaArea() : false;
    }

    public static boolean isWifiOnly(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (cm == null || cm.isNetworkSupported(0)) {
            return false;
        }
        return true;
    }

    public static boolean isOwnerUser() {
        return UserHandle.myUserId() == 0;
    }

    public static boolean hasPackageInfo(PackageManager manager, String name) {
        try {
            manager.getPackageInfo(name, 128);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
}
