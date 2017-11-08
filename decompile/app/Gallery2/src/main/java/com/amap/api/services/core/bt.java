package com.amap.api.services.core;

import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;

/* compiled from: Request */
public abstract class bt {
    int e = 20000;
    int f = 20000;
    HttpHost g = null;

    public abstract String b();

    public abstract Map<String, String> c_();

    public abstract Map<String, String> d_();

    public abstract HttpEntity e();

    public final void c(int i) {
        this.e = i;
    }

    public final void d(int i) {
        this.f = i;
    }

    public byte[] e_() {
        return null;
    }

    public final void a(HttpHost httpHost) {
        this.g = httpHost;
    }
}
