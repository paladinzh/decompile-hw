package com.amap.api.mapcore.util;

import java.net.Proxy;

/* compiled from: DownloadManager */
public class de {
    private df a;
    private dj b;

    /* compiled from: DownloadManager */
    public interface a {
        void a(Throwable th);

        void a(byte[] bArr, long j);

        void d();

        void e();
    }

    public de(dj djVar) {
        this(djVar, 0, -1);
    }

    public de(dj djVar, long j, long j2) {
        Proxy proxy = null;
        this.b = djVar;
        if (djVar.i != null) {
            proxy = djVar.i;
        }
        this.a = new df(this.b.g, this.b.h, proxy);
        this.a.b(j2);
        this.a.a(j);
    }

    public void a(a aVar) {
        this.a.a(this.b.a(), this.b.c(), this.b.b(), aVar);
    }

    public void a() {
        this.a.a();
    }
}
