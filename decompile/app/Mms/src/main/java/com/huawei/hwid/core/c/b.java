package com.huawei.hwid.core.c;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.datatype.c;
import java.util.HashMap;
import java.util.Map;

/* compiled from: AppInfoUtil */
public class b {
    private static Map a = new HashMap();

    private static synchronized void a(Context context) {
        synchronized (b.class) {
            try {
                a = b(context);
            } catch (Throwable e) {
                a.d("AppInfoUtil", "initAppInfos error:" + e.getMessage(), e);
            }
        }
    }

    public static String a(Context context, String str) {
        String str2;
        String f = f(context, str);
        String str3 = "";
        if (((c) a.get(f)) == null) {
            str2 = str3;
        } else {
            str2 = ((c) a.get(f)).c();
        }
        if (p.e(str2)) {
            str2 = "7000000";
        }
        a.b("AppInfoUtil", "getAppChannel is:" + str2);
        return str2;
    }

    public static String a() {
        return b() + "AB09070647056445";
    }

    private static String b() {
        return "99E790F6FBA9FDA8";
    }

    public static String b(Context context, String str) {
        String str2;
        c cVar = (c) a.get(f(context, str));
        String str3 = "";
        if (cVar == null || "com.huawei.hwid".equals(context.getPackageName())) {
            str2 = str3;
        } else {
            str2 = cVar.b();
        }
        if (p.e(str2)) {
            str2 = "7";
        }
        a.b("AppInfoUtil", "getAppClientType is:" + str2);
        return str2;
    }

    private static String f(Context context, String str) {
        if (p.e(str) || "cloud".equalsIgnoreCase(str)) {
            str = "com.huawei.hwid";
        }
        if (a == null || a.isEmpty()) {
            a(context);
        }
        return str;
    }

    private static boolean a(String str) {
        a.b("AppInfoUtil", "get client channel =" + str);
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return true;
    }

    private static Map b(Context context) {
        a.b("AppInfoUtil", "initAppInfos");
        XmlResourceParser xml = context.getResources().getXml(m.b(context, "appinfo"));
        Map hashMap = new HashMap();
        if (xml == null) {
            return hashMap;
        }
        int eventType = xml.getEventType();
        c cVar = new c();
        while (1 != eventType) {
            try {
                String name = xml.getName();
                switch (eventType) {
                    case 2:
                        if (!"appID".equals(name)) {
                            if (!"reqClientType".equals(name)) {
                                if (!"defaultChannel".equals(name)) {
                                    break;
                                }
                                cVar.b(xml.nextText());
                                break;
                            }
                            cVar.a(xml.nextText());
                            break;
                        }
                        cVar.c(xml.nextText());
                        break;
                    case 3:
                        if (!"appInfo".equals(name)) {
                            break;
                        }
                        hashMap.put(cVar.d(), cVar);
                        cVar = new c();
                        break;
                    default:
                        break;
                }
                eventType = xml.next();
            } catch (Throwable e) {
                a.d("AppInfoUtil", "initAppInfos error:" + e.getMessage(), e);
            } finally {
                xml.close();
            }
        }
        return hashMap;
    }

    public static boolean c(Context context, String str) {
        c cVar = (c) a.get(f(context, str));
        if (cVar == null) {
            return true;
        }
        return cVar.a();
    }

    public static boolean d(Context context, String str) {
        c cVar = (c) a.get(f(context, str));
        if (cVar == null) {
            return false;
        }
        return cVar.e();
    }

    public static String e(Context context, String str) {
        String f = f(context, str);
        String str2 = "";
        if (((c) a.get(f)) == null) {
            return str2;
        }
        return ((c) a.get(f)).f();
    }

    public static void a(Context context, String str, String str2, Bundle bundle) {
        if (TextUtils.isEmpty(str)) {
            a.b("AppInfoUtil", "appId is null");
        } else {
            b(context, str, str2, bundle);
        }
    }

    private static void b(Context context, String str, String str2, Bundle bundle) {
        if (bundle != null) {
            String str3;
            if (str2 != null) {
                str3 = str2;
            } else {
                str3 = bundle.getString("accountName");
            }
            String b = b(context, str);
            String a = a(context, str);
            String valueOf = String.valueOf(bundle.getInt("reqClientType", Integer.parseInt(b)));
            String valueOf2 = String.valueOf(bundle.getInt("loginChannel", Integer.parseInt(a)));
            int i = bundle.getInt("scope", 0);
            boolean z = bundle.getBoolean("isFromApk", false);
            boolean z2 = bundle.getBoolean("popLogin", false);
            boolean z3 = bundle.getBoolean("chooseAccount", false);
            boolean z4 = bundle.getBoolean("needAuth", true);
            boolean z5 = bundle.getBoolean("chooseWindow", false);
            int i2 = bundle.getInt("sdkType", 0);
            boolean z6 = bundle.getBoolean("activateVip", false);
            a.b("AppInfoUtil", "activateVip from other app is:" + z6);
            a(context, str, str3, valueOf, valueOf2, i, z, z2, z3, z4, false, z5, i2, z6);
            return;
        }
        a.b("AppInfoUtil", "bundle is null");
    }

    private static void a(Context context, String str, String str2, String str3, String str4, int i, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, int i2, boolean z7) {
        c cVar;
        String f = f(context, str);
        boolean containsKey = a.containsKey(f);
        if (containsKey) {
            cVar = (c) a.get(f);
        } else {
            cVar = new c();
        }
        cVar.d(str2);
        cVar.a(str3);
        if (a(str4)) {
            cVar.b(str4);
        }
        cVar.a(i);
        cVar.d(z);
        cVar.b(z2);
        cVar.c(z3);
        cVar.a(z4);
        cVar.e(z6);
        cVar.b(i2);
        cVar.f(z7);
        if (!containsKey) {
            a.put(f, cVar);
        }
        a.b("AppInfoUtil", "save params success");
    }
}
