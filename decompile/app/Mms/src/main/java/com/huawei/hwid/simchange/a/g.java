package com.huawei.hwid.simchange.a;

import com.huawei.hwid.core.c.b.a;
import java.lang.reflect.Method;

/* compiled from: VSimAPIWrapper */
public final class g {
    private static final Class a = a("com.huawei.telephony.HuaweiTelephonyManager");
    private static final Method b = a(a, "getDefault", new Class[0]);
    private static final Method c = a(a, "getVSimSubId", new Class[0]);
    private static g d;
    private Object e;

    private g() {
    }

    public static g a() {
        g gVar;
        synchronized (g.class) {
            if (d == null) {
                d = new g();
                d.e = a(null, b, new Object[0]);
            }
            gVar = d;
        }
        return gVar;
    }

    public int b() {
        if (c != null) {
            Object a = a(this.e, c, new Object[0]);
            if (a == null) {
                return -1;
            }
            return ((Integer) a).intValue();
        }
        a.a("VSimAPIWrapper", "method getVSimSubId not found");
        return -1;
    }

    public static Class a(String str) {
        try {
            return Class.forName(str);
        } catch (ClassNotFoundException e) {
            a.c("VSimAPIWrapper", "className not found: " + str);
            return null;
        }
    }

    public static Method a(Class cls, String str, Class... clsArr) {
        try {
            return cls.getMethod(str, clsArr);
        } catch (SecurityException e) {
            a.c("VSimAPIWrapper", "SecurityException");
            return null;
        } catch (NoSuchMethodException e2) {
            a.c("VSimAPIWrapper", str + ", no such method.");
            return null;
        }
    }

    public static Object a(Object obj, Method method, Object... objArr) {
        try {
            return method.invoke(obj, objArr);
        } catch (RuntimeException e) {
            a.c("VSimAPIWrapper", "Exception in invoke: " + e.getClass().getSimpleName());
            return null;
        } catch (Exception e2) {
            a.c("VSimAPIWrapper", "Exception in invoke: " + e2.getCause() + "; method=" + method.getName());
            return null;
        }
    }
}
