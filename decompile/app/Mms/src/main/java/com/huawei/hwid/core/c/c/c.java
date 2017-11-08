package com.huawei.hwid.core.c.c;

import com.huawei.hwid.core.c.b.a;
import java.lang.reflect.Field;

/* compiled from: MultiCardFactory */
public class c {
    private static b a = b.MODE_SUPPORT_UNKNOWN;
    private static a b;

    public static synchronized a a() {
        a aVar;
        synchronized (c.class) {
            b();
            if (a != b.MODE_SUPPORT_MTK_GEMINI) {
                b = d.b();
            } else {
                b = e.b();
            }
            aVar = b;
        }
        return aVar;
    }

    public static synchronized boolean b() {
        boolean z = false;
        synchronized (c.class) {
            if (a == b.MODE_SUPPORT_UNKNOWN) {
                try {
                    if (d()) {
                        a(b.MODE_SUPPORT_MTK_GEMINI);
                        z = true;
                    } else if (c()) {
                        a(b.MODE_SUPPORT_HW_GEMINI);
                        z = true;
                    } else {
                        a(b.MODE_NOT_SUPPORT_GEMINI);
                    }
                } catch (Throwable e) {
                    a.a("mutiCardFactory", " " + e.toString(), e);
                } catch (Throwable e2) {
                    a.a("mutiCardFactory", "" + e2.toString(), e2);
                }
            } else if (a == b.MODE_SUPPORT_HW_GEMINI || a == b.MODE_SUPPORT_MTK_GEMINI) {
                z = true;
            }
        }
        return z;
    }

    private static synchronized void a(b bVar) {
        synchronized (c.class) {
            a = bVar;
        }
    }

    public static boolean c() {
        boolean z = false;
        try {
            boolean z2;
            Object c = d.c();
            if (c == null) {
                z2 = false;
            } else {
                z2 = ((Boolean) c.getClass().getMethod("isMultiSimEnabled", new Class[0]).invoke(c, new Object[0])).booleanValue();
            }
            z = z2;
        } catch (Throwable e) {
            a.a("mutiCardFactory", "MSimTelephonyManager.getDefault().isMultiSimEnabled()?" + e.toString(), e);
        } catch (Throwable e2) {
            a.a("mutiCardFactory", "MSimTelephonyManager.getDefault().isMultiSimEnabled()" + e2.toString(), e2);
        }
        a.b("mutiCardFactory", "isHwGeminiSupport1 " + z);
        return z;
    }

    private static boolean d() {
        boolean z;
        try {
            Field declaredField = Class.forName("com.mediatek.common.featureoption.FeatureOption").getDeclaredField("MTK_GEMINI_SUPPORT");
            declaredField.setAccessible(true);
            z = declaredField.getBoolean(null);
        } catch (Throwable e) {
            a.a("mutiCardFactory", "FeatureOption.MTK_GEMINI_SUPPORT" + e.toString(), e);
            z = false;
        } catch (Throwable e2) {
            a.a("mutiCardFactory", "FeatureOption.MTK_GEMINI_SUPPORT" + e2.toString(), e2);
            z = false;
        }
        a.b("mutiCardFactory", "isMtkGeminiSupport " + z);
        return z;
    }
}
