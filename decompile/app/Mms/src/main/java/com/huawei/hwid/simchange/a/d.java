package com.huawei.hwid.simchange.a;

import android.content.Context;
import com.huawei.hwid.core.c.b.a;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/* compiled from: SIMUtils */
final class d {
    private static d b = null;
    private a a;

    private d() {
        c();
    }

    static synchronized d a() {
        d dVar;
        synchronized (d.class) {
            if (b == null) {
                b = new d();
            }
            dVar = b;
        }
        return dVar;
    }

    boolean b() {
        return this.a.a();
    }

    String a(Context context, int i) {
        return this.a.a(context, i);
    }

    private void c() {
        try {
            if (d()) {
                a.a("SIMUtils", "init SIMUtils choose MTK mutil");
                this.a = new b();
                if (this.a != null) {
                    a.a("SIMUtils", "Fail to create sim, so init singleSIM");
                    this.a = new e();
                }
            }
            if (e()) {
                a.a("SIMUtils", "init SIMUtils choose HW mutil");
                this.a = new c();
            } else {
                a.a("SIMUtils", "init SIMUtils choose single");
                this.a = new e();
            }
            if (this.a != null) {
                a.a("SIMUtils", "Fail to create sim, so init singleSIM");
                this.a = new e();
            }
        } catch (Exception e) {
            a.a("SIMUtils", "Exception ");
        } catch (Error e2) {
            a.a("SIMUtils", "Error error");
        }
    }

    private boolean d() {
        try {
            Field declaredField = Class.forName("com.mediatek.common.featureoption.FeatureOption").getDeclaredField("MTK_GEMINI_SUPPORT");
            declaredField.setAccessible(true);
            return declaredField.getBoolean(null);
        } catch (Exception e) {
            a.d("SIMUtils", "no FeatureOption");
            return g();
        } catch (Error e2) {
            a.d("SIMUtils", "no FeatureOption");
            return g();
        }
    }

    private boolean e() {
        try {
            return ((Boolean) Class.forName("android.telephony.MSimTelephonyManager").getMethod("isMultiSimEnabled", new Class[0]).invoke(f(), new Object[0])).booleanValue();
        } catch (Exception e) {
            a.a("SIMUtils", "MSimTelephonyManager.getDefault().isMultiSimEnabled()?");
            return false;
        } catch (Error e2) {
            a.a("SIMUtils", "MSimTelephonyManager.getDefault().isMultiSimEnabled()");
            return false;
        }
    }

    private Object f() {
        Object obj = null;
        try {
            Class cls = Class.forName("android.telephony.MSimTelephonyManager");
            obj = cls.getDeclaredMethod("getDefault", new Class[0]).invoke(cls, new Object[0]);
        } catch (Exception e) {
            a.a("SIMUtils", " getHWMSimTelephonyManager wrong ");
        }
        return obj;
    }

    private boolean g() {
        try {
            return "1".equals((String) Class.forName("android.os.SystemProperties").getMethod("get", new Class[]{String.class}).invoke(null, new Object[]{"ro.mtk_gemini_support"}));
        } catch (NoSuchMethodException e) {
            a.b("SIMUtils", "NoSuchMethodException");
            return false;
        } catch (ClassNotFoundException e2) {
            a.b("SIMUtils", "ClassNotFoundException");
            return false;
        } catch (NumberFormatException e3) {
            a.b("SIMUtils", "NumberFormatException");
            return false;
        } catch (IllegalAccessException e4) {
            a.b("SIMUtils", "IllegalAccessException");
            return false;
        } catch (IllegalArgumentException e5) {
            a.b("SIMUtils", "IllegalArgumentException");
            return false;
        } catch (InvocationTargetException e6) {
            a.b("SIMUtils", "InvocationTargetException");
            return false;
        } catch (Exception e7) {
            a.b("SIMUtils", "Exception");
            return false;
        }
    }

    int b(Context context, int i) {
        return this.a.b(context, i);
    }
}
