package com.amap.api.mapcore.util;

import android.content.Context;
import com.amap.api.mapcore.s;
import com.amap.api.maps.AMapException;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/* compiled from: NetFileFetch */
public class ac implements com.amap.api.mapcore.util.de.a {
    ad a = null;
    long b = 0;
    long c = 0;
    long d;
    boolean e = true;
    x f;
    long g = 0;
    a h;
    private Context i;
    private ah j;
    private String k;
    private de l;
    private y m;

    /* compiled from: NetFileFetch */
    public interface a {
        void d();
    }

    public ac(ad adVar, String str, Context context, ah ahVar) throws IOException {
        this.f = x.a(context.getApplicationContext());
        this.a = adVar;
        this.i = context;
        this.k = str;
        this.j = ahVar;
        g();
    }

    private void f() throws IOException {
        dj aiVar = new ai(this.k);
        aiVar.a(1800000);
        aiVar.b(1800000);
        this.l = new de(aiVar, this.b, this.c);
        this.m = new y(this.a.b() + File.separator + this.a.c(), this.b);
    }

    private void g() {
        if (this.f.f(this.a.e())) {
            this.e = false;
            l();
            return;
        }
        this.b = 0;
        this.c = 0;
    }

    public void a() {
        try {
            if (bj.c(this.i)) {
                i();
                if (bm.a == 1) {
                    if (!h()) {
                        this.e = true;
                    }
                    if (this.e) {
                        this.d = b();
                        if (this.d == -1) {
                            af.a("File Length is not known!");
                        } else if (this.d == -2) {
                            af.a("File is not access!");
                        } else {
                            this.c = this.d;
                        }
                        this.b = 0;
                    }
                    if (this.j != null) {
                        this.j.m();
                    }
                    f();
                    this.l.a(this);
                    return;
                }
                if (this.j != null) {
                    this.j.a(com.amap.api.mapcore.util.ah.a.amap_exception);
                }
                return;
            }
            if (this.j != null) {
                this.j.a(com.amap.api.mapcore.util.ah.a.network_exception);
            }
        } catch (Throwable e) {
            ce.a(e, "SiteFileFetch", "download");
            if (this.j != null) {
                this.j.a(com.amap.api.mapcore.util.ah.a.amap_exception);
            }
        } catch (IOException e2) {
            if (this.j != null) {
                this.j.a(com.amap.api.mapcore.util.ah.a.file_io_exception);
            }
        }
    }

    private boolean h() {
        return (new File(new StringBuilder().append(this.a.b()).append(File.separator).append(this.a.c()).toString()).length() > 10 ? 1 : (new File(new StringBuilder().append(this.a.b()).append(File.separator).append(this.a.c()).toString()).length() == 10 ? 0 : -1)) >= 0;
    }

    private void i() throws AMapException {
        if (bm.a != 1) {
            int i = 0;
            while (i < 3) {
                try {
                    if (!bm.b(this.i, bj.e())) {
                        i++;
                    } else {
                        return;
                    }
                } catch (Throwable th) {
                    ce.a(th, "SiteFileFetch", "authOffLineDownLoad");
                    th.printStackTrace();
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long b() throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(this.a.a()).openConnection();
        httpURLConnection.setRequestProperty("User-Agent", s.d);
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode < 400) {
            String headerFieldKey;
            responseCode = 1;
            while (true) {
                headerFieldKey = httpURLConnection.getHeaderFieldKey(responseCode);
                if (headerFieldKey != null) {
                    if (headerFieldKey.equalsIgnoreCase("Content-Length")) {
                        break;
                    }
                    responseCode++;
                } else {
                    break;
                }
                return (long) r0;
            }
            int parseInt = Integer.parseInt(httpURLConnection.getHeaderField(headerFieldKey));
            return (long) parseInt;
        }
        a(responseCode);
        return -2;
    }

    private void j() {
        long currentTimeMillis = System.currentTimeMillis();
        if (this.a != null) {
            if ((currentTimeMillis - this.g <= 500 ? 1 : null) == null) {
                k();
                this.g = currentTimeMillis;
                a(this.b);
            }
        }
    }

    private void k() {
        this.f.a(this.a.e(), this.a.d(), this.d, this.b, this.c);
    }

    private void a(long j) {
        if ((this.d <= 0 ? 1 : null) == null && this.j != null) {
            this.j.a(this.d, j);
            this.g = System.currentTimeMillis();
        }
    }

    private boolean l() {
        if (!this.f.f(this.a.e())) {
            return false;
        }
        this.d = (long) this.f.d(this.a.e());
        long[] a = this.f.a(this.a.e(), 0);
        this.b = a[0];
        this.c = a[1];
        return true;
    }

    private void a(int i) {
        System.err.println("Error Code : " + i);
    }

    public void c() {
        if (this.l != null) {
            this.l.a();
        }
    }

    public void d() {
        if (this.j != null) {
            this.j.o();
        }
        k();
    }

    public void e() {
        j();
        if (this.j != null) {
            this.j.n();
        }
        if (this.m != null) {
            this.m.a();
        }
        if (this.h != null) {
            this.h.d();
        }
    }

    public void a(Throwable th) {
        if (this.j != null) {
            this.j.a(com.amap.api.mapcore.util.ah.a.network_exception);
        }
        if (!((th instanceof IOException) || this.m == null)) {
            this.m.a();
        }
    }

    public void a(byte[] bArr, long j) {
        try {
            this.m.a(bArr);
            this.b = j;
            j();
        } catch (Throwable e) {
            e.printStackTrace();
            ce.a(e, "fileAccessI", "fileAccessI.write(byte[] data)");
            if (this.j != null) {
                this.j.a(com.amap.api.mapcore.util.ah.a.file_io_exception);
            }
            if (this.l != null) {
                this.l.a();
            }
        }
    }

    public void a(a aVar) {
        this.h = aVar;
    }
}
