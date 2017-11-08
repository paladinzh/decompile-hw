package com.amap.api.services.core;

import android.text.TextUtils;
import java.net.Proxy;
import java.util.Map;

/* compiled from: Request */
public abstract class cj {
    int e = 20000;
    int f = 20000;
    Proxy g = null;

    public abstract Map<String, String> b();

    public abstract Map<String, String> c();

    public abstract String g();

    String n() {
        byte[] f = f();
        if (f == null || f.length == 0) {
            return g();
        }
        Map b = b();
        if (b == null) {
            return g();
        }
        String a = cg.a(b);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(g()).append("?").append(a);
        return stringBuffer.toString();
    }

    byte[] o() {
        byte[] f = f();
        if (f != null && f.length != 0) {
            return f;
        }
        String a = cg.a(b());
        try {
            if (TextUtils.isEmpty(a)) {
                return f;
            }
            return a.getBytes("UTF-8");
        } catch (Throwable e) {
            byte[] bytes = a.getBytes();
            ay.a(e, "Request", "getConnectionDatas");
            return bytes;
        }
    }

    public final void c(int i) {
        this.e = i;
    }

    public final void d(int i) {
        this.f = i;
    }

    public byte[] f() {
        return null;
    }

    public final void a(Proxy proxy) {
        this.g = proxy;
    }
}
