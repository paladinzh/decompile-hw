package com.amap.api.services.core;

import com.amap.api.services.core.ad.a;

/* compiled from: ConfigableConst */
public class c {
    public static final String[] a = new String[]{"com.amap.api.services"};

    public static String a() {
        if (ServiceSettings.getInstance().getProtocol() != 1) {
            return "https://restapi.amap.com/v3";
        }
        return "http://restapi.amap.com/v3";
    }

    public static String b() {
        return ServiceSettings.getInstance().getLanguage();
    }

    public static ad a(boolean z) {
        String str = "getSDKInfo";
        try {
            return new a("sea", "2.5.0", "AMAP SDK Android Search 2.5.0").a(a).a(z).a();
        } catch (Throwable e) {
            d.a(e, "ConfigableConst", str);
            return null;
        }
    }
}
