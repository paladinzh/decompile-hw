package com.amap.api.services.core;

import com.amap.api.services.core.ar.a;

/* compiled from: ConfigableConst */
public class h {
    public static final String[] a = new String[]{"com.amap.api.services"};

    public static String a() {
        if (ServiceSettings.getInstance().getProtocol() != 1) {
            return "https://restapi.amap.com/v3";
        }
        return "http://restapi.amap.com/v3";
    }

    public static String b() {
        if (ServiceSettings.getInstance().getProtocol() != 1) {
            return "https://yuntuapi.amap.com";
        }
        return "http://yuntuapi.amap.com";
    }

    public static String c() {
        return ServiceSettings.getInstance().getLanguage();
    }

    public static ar a(boolean z) {
        String str = "getSDKInfo";
        try {
            return new a("sea", "3.2.1", "AMAP SDK Android Search 3.2.1").a(a).a(z).a();
        } catch (Throwable e) {
            i.a(e, "ConfigableConst", str);
            return null;
        }
    }

    public static ar b(boolean z) {
        String str = "getCloudSDKInfo";
        try {
            return new a("cloud", "3.2.1", "AMAP SDK Android Search 3.2.1").a(a).a(z).a();
        } catch (Throwable e) {
            i.a(e, "ConfigableConst", str);
            return null;
        }
    }
}
