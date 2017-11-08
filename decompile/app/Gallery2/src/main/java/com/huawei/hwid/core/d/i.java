package com.huawei.hwid.core.d;

import com.huawei.hwid.core.d.b.e;
import com.huawei.watermark.manager.parse.WMConfig;

public class i {
    public static boolean a() {
        String str = "";
        String str2 = "";
        String str3;
        try {
            Object a = g.a("android.os.SystemProperties", "get", new Class[]{String.class}, new Object[]{"ro.product.locale.language"});
            Object a2 = g.a("android.os.SystemProperties", "get", new Class[]{String.class}, new Object[]{"ro.product.locale.region"});
            if (a != null) {
                str = (String) a;
            }
            if (a2 == null) {
                str3 = str2;
            } else {
                str3 = (String) a2;
            }
        } catch (Exception e) {
            String str4 = str;
            e.c("PropertyUtils", e.getMessage());
            str3 = str2;
            str = str4;
        }
        if (WMConfig.SUPPORTZH.equalsIgnoreCase(str) && "cn".equalsIgnoreCase(r1)) {
            return true;
        }
        return false;
    }
}
