package com.huawei.hwid.simchange.a;

import com.huawei.hwid.core.c.b.a;
import java.lang.reflect.Method;

/* compiled from: VSimAPIWrapperMVersion */
public final class h {
    private static final Class a = g.a("android.telephony.HwTelephonyManager");
    private static final Method b = g.a(a, "getDefault", new Class[0]);
    private static final Method c = g.a(a, "isVSimEnabled", new Class[0]);
    private static h d;
    private Object e;

    private h() {
    }

    public static h a() {
        h hVar;
        synchronized (h.class) {
            if (d == null) {
                d = new h();
                d.e = g.a(null, b, new Object[0]);
            }
            hVar = d;
        }
        return hVar;
    }

    public boolean b() {
        if (c != null) {
            Object a = g.a(this.e, c, new Object[0]);
            if (a == null) {
                return false;
            }
            return ((Boolean) a).booleanValue();
        }
        a.a("VSimAPIWrapperMVersion", "method isVSimEnabled not found");
        return false;
    }
}
