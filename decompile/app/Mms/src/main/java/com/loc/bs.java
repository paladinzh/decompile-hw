package com.loc;

import android.text.TextUtils;
import java.net.Proxy;
import java.util.Map;

/* compiled from: Request */
public abstract class bs {
    int a = 20000;
    int b = 20000;
    Proxy c = null;

    public abstract Map<String, String> a();

    public final void a(int i) {
        this.a = i;
    }

    public final void a(Proxy proxy) {
        this.c = proxy;
    }

    public abstract Map<String, String> b();

    public final void b(int i) {
        this.b = i;
    }

    public abstract String c();

    public byte[] d() {
        return null;
    }

    String e() {
        byte[] d = d();
        if (d == null || d.length == 0) {
            return c();
        }
        Map b = b();
        if (b == null) {
            return c();
        }
        String a = bq.a(b);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(c()).append("?").append(a);
        return stringBuffer.toString();
    }

    byte[] f() {
        byte[] d = d();
        if (d != null && d.length != 0) {
            return d;
        }
        String a = bq.a(b());
        try {
            return TextUtils.isEmpty(a) ? d : a.getBytes("UTF-8");
        } catch (Throwable e) {
            byte[] bytes = a.getBytes();
            aa.a(e, "Request", "getConnectionDatas");
            return bytes;
        }
    }
}
