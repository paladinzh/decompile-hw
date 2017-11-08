package com.avast.android.sdk.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* compiled from: Unknown */
public class c {
    private static final Map<a, String> a = new HashMap();
    private static String b;

    /* compiled from: Unknown */
    public enum a {
        APP_INSTALL_SERVICE("app_install_service"),
        VPS_UPDATE_SERVICE("vps_update_service");
        
        private static final Map<String, a> c = null;
        private final String d;

        static {
            c = new HashMap();
            Iterator it = EnumSet.allOf(a.class).iterator();
            while (it.hasNext()) {
                a aVar = (a) it.next();
                c.put(aVar.a(), aVar);
            }
        }

        private a(String str) {
            this.d = str;
        }

        public static a a(String str) {
            return (a) c.get(str);
        }

        public String a() {
            return this.d;
        }
    }

    public static ComponentName a(a aVar) {
        String str = (String) a.get(aVar);
        return str != null ? new ComponentName(b, str) : null;
    }

    private static void a(PackageInfo packageInfo) {
        if (packageInfo.services != null) {
            for (ServiceInfo serviceInfo : packageInfo.services) {
                if (serviceInfo.metaData != null) {
                    a a = a.a(serviceInfo.metaData.getString("avast_sdk_component"));
                    if (a != null) {
                        if (serviceInfo.name.startsWith(".")) {
                            a.put(a, serviceInfo.packageName + serviceInfo.name);
                        } else {
                            a.put(a, serviceInfo.name);
                        }
                    }
                }
            }
        }
    }

    public static boolean a(Context context) {
        PackageManager packageManager = context.getPackageManager();
        b = context.getPackageName();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 135);
            b(packageInfo);
            a(packageInfo);
            c(packageInfo);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private static void b(PackageInfo packageInfo) {
        if (packageInfo.activities != null) {
            for (ActivityInfo activityInfo : packageInfo.activities) {
                if (activityInfo.metaData != null) {
                    a a = a.a(activityInfo.metaData.getString("avast_sdk_component"));
                    if (a != null) {
                        if (activityInfo.name.startsWith(".")) {
                            a.put(a, activityInfo.packageName + activityInfo.name);
                        } else {
                            a.put(a, activityInfo.name);
                        }
                    }
                }
            }
        }
    }

    private static void c(PackageInfo packageInfo) {
        if (packageInfo.receivers != null) {
            for (ActivityInfo activityInfo : packageInfo.receivers) {
                if (activityInfo.metaData != null) {
                    a a = a.a(activityInfo.metaData.getString("avast_sdk_component"));
                    if (a != null) {
                        if (activityInfo.name.startsWith(".")) {
                            a.put(a, activityInfo.packageName + activityInfo.name);
                        } else {
                            a.put(a, activityInfo.name);
                        }
                    }
                }
            }
        }
    }
}
