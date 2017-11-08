package com.amap.api.mapcore.util;

import android.text.TextUtils;
import java.net.Proxy;
import java.util.Map;

/* compiled from: Request */
public abstract class dj {
    int g = 20000;
    int h = 20000;
    Proxy i = null;

    public abstract String a();

    public abstract Map<String, String> b();

    public abstract Map<String, String> c();

    String f() {
        byte[] a_ = a_();
        if (a_ == null || a_.length == 0) {
            return a();
        }
        Map b = b();
        if (b == null) {
            return a();
        }
        String a = df.a(b);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(a()).append("?").append(a);
        return stringBuffer.toString();
    }

    byte[] g() {
        byte[] a_ = a_();
        if (a_ != null && a_.length != 0) {
            return a_;
        }
        String a = df.a(b());
        if (TextUtils.isEmpty(a)) {
            return a_;
        }
        return bx.a(a);
    }

    public final void a(int i) {
        this.g = i;
    }

    public final void b(int i) {
        this.h = i;
    }

    public byte[] a_() {
        return null;
    }

    public final void a(Proxy proxy) {
        this.i = proxy;
    }
}
