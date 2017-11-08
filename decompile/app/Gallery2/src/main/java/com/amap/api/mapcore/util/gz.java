package com.amap.api.mapcore.util;

import java.net.Proxy;

/* compiled from: DownloadManager */
public class gz {
    private ha a;
    private hd b;

    /* compiled from: DownloadManager */
    public interface a {
        void a(Throwable th);

        void a(byte[] bArr, long j);

        void d();

        void e();
    }

    public gz(hd hdVar) {
        this(hdVar, 0, -1);
    }

    public gz(hd hdVar, long j, long j2) {
        Proxy proxy = null;
        this.b = hdVar;
        if (hdVar.h != null) {
            proxy = hdVar.h;
        }
        this.a = new ha(this.b.f, this.b.g, proxy);
        this.a.b(j2);
        this.a.a(j);
    }

    public void a(a aVar) {
        this.a.a(this.b.c(), this.b.a(), this.b.b(), aVar);
    }

    public void a() {
        this.a.a();
    }
}
