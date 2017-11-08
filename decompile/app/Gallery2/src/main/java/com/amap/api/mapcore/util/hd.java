package com.amap.api.mapcore.util;

import android.text.TextUtils;
import java.net.Proxy;
import java.util.Map;

/* compiled from: Request */
public abstract class hd {
    int f = 20000;
    int g = 20000;
    Proxy h = null;

    public abstract Map<String, String> a();

    public abstract Map<String, String> b();

    public abstract String c();

    String k() {
        byte[] g = g();
        if (g == null || g.length == 0) {
            return c();
        }
        Map b = b();
        if (b == null) {
            return c();
        }
        String a = ha.a(b);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(c()).append("?").append(a);
        return stringBuffer.toString();
    }

    byte[] l() {
        byte[] g = g();
        if (g != null && g.length != 0) {
            return g;
        }
        String a = ha.a(b());
        if (TextUtils.isEmpty(a)) {
            return g;
        }
        return fi.a(a);
    }

    public final void a(int i) {
        this.f = i;
    }

    public final void b(int i) {
        this.g = i;
    }

    public byte[] g() {
        return null;
    }

    public final void a(Proxy proxy) {
        this.h = proxy;
    }
}
