package com.loc;

import java.net.Proxy;

/* compiled from: DownloadManager */
public class bp {
    private bq a;
    private bs b;

    /* compiled from: DownloadManager */
    public interface a {
        void a(Throwable th);

        void a(byte[] bArr, long j);

        void b();

        void c();
    }

    public bp(bs bsVar) {
        this(bsVar, 0, -1);
    }

    public bp(bs bsVar, long j, long j2) {
        Proxy proxy = null;
        this.b = bsVar;
        if (bsVar.c != null) {
            proxy = bsVar.c;
        }
        this.a = new bq(this.b.a, this.b.b, proxy);
        this.a.b(j2);
        this.a.a(j);
    }

    public void a(a aVar) {
        this.a.a(this.b.c(), this.b.a(), this.b.b(), aVar);
    }
}
