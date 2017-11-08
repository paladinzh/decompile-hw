package com.huawei.hwid.core.d;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import com.huawei.hwid.core.d.b.e;
import java.util.HashMap;
import java.util.Map;

public class a {
    private static Map<String, com.huawei.hwid.core.datatype.a> a = new HashMap();

    private static synchronized void a(Context context) {
        synchronized (a.class) {
            try {
                a = b(context);
            } catch (Throwable e) {
                e.d("AppInfoUtil", "initAppInfos error:" + e.getMessage(), e);
            }
        }
    }

    public static String a(Context context, String str) {
        String str2;
        String b = b(context, str);
        String str3 = "";
        if (((com.huawei.hwid.core.datatype.a) a.get(b)) == null) {
            str2 = str3;
        } else {
            str2 = ((com.huawei.hwid.core.datatype.a) a.get(b)).a();
        }
        if (TextUtils.isEmpty(str2)) {
            str2 = "7000000";
        }
        try {
            Integer.parseInt(str2);
        } catch (NumberFormatException e) {
            e.d("AppInfoUtil", e.getMessage());
            str2 = "7000000";
        }
        e.b("AppInfoUtil", "getAppChannel is:" + str2);
        return str2;
    }

    public static String a() {
        return b() + "AB09070647056445";
    }

    private static String b() {
        return "99E790F6FBA9FDA8";
    }

    private static String b(Context context, String str) {
        if (TextUtils.isEmpty(str) || "com.huawei.hwid".equalsIgnoreCase(str)) {
            str = "com.huawei.hwid";
        }
        if (a == null || a.isEmpty()) {
            a(context);
        }
        return str;
    }

    private static Map<String, com.huawei.hwid.core.datatype.a> b(Context context) {
        e.b("AppInfoUtil", "initAppInfos");
        XmlResourceParser xml = context.getResources().getXml(j.b(context, "appinfo"));
        Map<String, com.huawei.hwid.core.datatype.a> hashMap = new HashMap();
        if (xml == null) {
            return hashMap;
        }
        int eventType = xml.getEventType();
        com.huawei.hwid.core.datatype.a aVar = new com.huawei.hwid.core.datatype.a();
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
                                aVar.b(xml.nextText());
                                break;
                            }
                            aVar.a(xml.nextText());
                            break;
                        }
                        aVar.c(xml.nextText());
                        break;
                    case 3:
                        if (!"appInfo".equals(name)) {
                            break;
                        }
                        hashMap.put(aVar.b(), aVar);
                        aVar = new com.huawei.hwid.core.datatype.a();
                        break;
                    default:
                        break;
                }
                eventType = xml.next();
            } catch (Throwable e) {
                e.d("AppInfoUtil", "initAppInfos error:" + e.getMessage(), e);
            } finally {
                xml.close();
            }
        }
        return hashMap;
    }
}
