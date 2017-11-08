package com.huawei.hwid.core.d.c;

import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.d.c.a.a;
import java.lang.reflect.Field;

public class b {
    private static a a = a.MODE_SUPPORT_UNKNOWN;
    private static a b;

    public static synchronized a a() {
        a aVar;
        synchronized (b.class) {
            b();
            if (a != a.MODE_SUPPORT_MTK_GEMINI) {
                b = c.b();
            } else {
                b = d.b();
            }
            aVar = b;
        }
        return aVar;
    }

    public static synchronized boolean b() {
        boolean z = false;
        synchronized (b.class) {
            if (a == a.MODE_SUPPORT_UNKNOWN) {
                try {
                    if (d()) {
                        a(a.MODE_SUPPORT_MTK_GEMINI);
                        z = true;
                    } else if (c()) {
                        a(a.MODE_SUPPORT_HW_GEMINI);
                        z = true;
                    } else {
                        a(a.MODE_NOT_SUPPORT_GEMINI);
                    }
                } catch (Throwable e) {
                    e.a("mutiCardFactory", " " + e.getMessage(), e);
                } catch (Throwable e2) {
                    e.a("mutiCardFactory", "" + e2.toString(), e2);
                }
            } else if (a == a.MODE_SUPPORT_HW_GEMINI || a == a.MODE_SUPPORT_MTK_GEMINI) {
                z = true;
            }
        }
        return z;
    }

    private static synchronized void a(a aVar) {
        synchronized (b.class) {
            a = aVar;
        }
    }

    public static boolean c() {
        boolean z = false;
        try {
            boolean z2;
            Object c = c.c();
            if (c == null) {
                z2 = false;
            } else {
                z2 = ((Boolean) c.getClass().getMethod("isMultiSimEnabled", new Class[0]).invoke(c, new Object[0])).booleanValue();
            }
            z = z2;
        } catch (Throwable e) {
            e.a("mutiCardFactory", "MSimTelephonyManager.getDefault().isMultiSimEnabled()?" + e.getMessage(), e);
        } catch (Throwable e2) {
            e.a("mutiCardFactory", "MSimTelephonyManager.getDefault().isMultiSimEnabled()" + e2.getMessage(), e2);
        }
        e.b("mutiCardFactory", "isHwGeminiSupport1 " + z);
        return z;
    }

    private static boolean d() {
        boolean z;
        try {
            Field declaredField = Class.forName("com.mediatek.common.featureoption.FeatureOption").getDeclaredField("MTK_GEMINI_SUPPORT");
            declaredField.setAccessible(true);
            z = declaredField.getBoolean(null);
        } catch (Throwable e) {
            e.a("mutiCardFactory", "FeatureOption.MTK_GEMINI_SUPPORT" + e.getMessage(), e);
            z = false;
        } catch (Throwable e2) {
            e.a("mutiCardFactory", "FeatureOption.MTK_GEMINI_SUPPORT" + e2.toString(), e2);
            z = false;
        }
        e.b("mutiCardFactory", "isMtkGeminiSupport " + z);
        return z;
    }
}
