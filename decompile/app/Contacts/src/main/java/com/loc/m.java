package com.loc;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import java.security.MessageDigest;
import java.util.Locale;

/* compiled from: AppInfo */
public class m {
    private static String a = "";
    private static String b = "";
    private static String c = "";
    private static String d = "";
    private static String e = null;

    public static String a(Context context) {
        try {
            return g(context);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return d;
        } catch (Throwable th) {
            th.printStackTrace();
            return d;
        }
    }

    public static void a(String str) {
        b = str;
    }

    public static String b(Context context) {
        try {
            if (!"".equals(a)) {
                return a;
            }
            PackageManager packageManager = context.getPackageManager();
            a = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(context.getPackageName(), 0));
            return a;
        } catch (Throwable e) {
            aa.a(e, "AppInfo", "getApplicationName");
        } catch (Throwable e2) {
            aa.a(e2, "AppInfo", "getApplicationName");
        }
    }

    public static void b(String str) {
        e = str;
    }

    public static String c(Context context) {
        try {
            if (b != null) {
                if (!"".equals(b)) {
                    return b;
                }
            }
            b = context.getApplicationContext().getPackageName();
        } catch (Throwable th) {
            aa.a(th, "AppInfo", "getPackageName");
        }
        return b;
    }

    static void c(String str) {
        d = str;
    }

    public static String d(Context context) {
        try {
            if (!"".equals(c)) {
                return c;
            }
            c = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            return c;
        } catch (Throwable e) {
            aa.a(e, "AppInfo", "getApplicationVersion");
        } catch (Throwable e2) {
            aa.a(e2, "AppInfo", "getApplicationVersion");
        }
    }

    public static String e(Context context) {
        try {
            if (e != null && !"".equals(e)) {
                return e;
            }
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 64);
            byte[] digest = MessageDigest.getInstance("SHA1").digest(packageInfo.signatures[0].toByteArray());
            StringBuffer stringBuffer = new StringBuffer();
            for (byte b : digest) {
                String toUpperCase = Integer.toHexString(b & 255).toUpperCase(Locale.US);
                if (toUpperCase.length() == 1) {
                    stringBuffer.append("0");
                }
                stringBuffer.append(toUpperCase);
                stringBuffer.append(":");
            }
            stringBuffer.append(packageInfo.packageName);
            e = stringBuffer.toString();
            return e;
        } catch (Throwable e) {
            aa.a(e, "AppInfo", "getSHA1AndPackage");
            return e;
        } catch (Throwable e2) {
            aa.a(e2, "AppInfo", "getSHA1AndPackage");
            return e;
        } catch (Throwable e22) {
            aa.a(e22, "AppInfo", "getSHA1AndPackage");
            return e;
        }
    }

    public static String f(Context context) {
        try {
            return g(context);
        } catch (Throwable e) {
            aa.a(e, "AppInfo", "getKey");
            return d;
        } catch (Throwable e2) {
            aa.a(e2, "AppInfo", "getKey");
            return d;
        }
    }

    private static String g(Context context) throws NameNotFoundException {
        if (d == null || d.equals("")) {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
            if (applicationInfo == null) {
                return d;
            }
            d = applicationInfo.metaData.getString("com.amap.api.v2.apikey");
        }
        return d;
    }
}
