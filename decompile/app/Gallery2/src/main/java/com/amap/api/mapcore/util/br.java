package com.amap.api.mapcore.util;

import android.content.Context;
import com.amap.api.maps.AMapException;
import com.huawei.gallery.app.AbsAlbumPage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/* compiled from: NetFileFetch */
public class br implements com.amap.api.mapcore.util.gz.a {
    bs a = null;
    long b = 0;
    long c = 0;
    long d;
    boolean e = true;
    bm f;
    long g = 0;
    a h;
    private Context i;
    private bw j;
    private String k;
    private gz l;
    private bn m;

    /* compiled from: NetFileFetch */
    public interface a {
        void d();
    }

    public br(bs bsVar, String str, Context context, bw bwVar) throws IOException {
        this.f = bm.a(context.getApplicationContext());
        this.a = bsVar;
        this.i = context;
        this.k = str;
        this.j = bwVar;
        g();
    }

    private void f() throws IOException {
        hd bxVar = new bx(this.k);
        bxVar.a(1800000);
        bxVar.b(1800000);
        this.l = new gz(bxVar, this.b, this.c);
        this.m = new bn(this.a.b() + File.separator + this.a.c(), this.b);
    }

    private void g() {
        File file = new File(this.a.b() + this.a.c());
        if (file.exists()) {
            this.e = false;
            this.b = file.length();
            try {
                this.d = b();
                this.c = this.d;
                return;
            } catch (IOException e) {
                if (this.j != null) {
                    this.j.a(com.amap.api.mapcore.util.bw.a.file_io_exception);
                    return;
                }
                return;
            }
        }
        this.b = 0;
        this.c = 0;
    }

    public void a() {
        Object obj = 1;
        try {
            if (eh.c(this.i)) {
                i();
                if (ez.a == 1) {
                    if (!h()) {
                        this.e = true;
                    }
                    if (this.e) {
                        this.d = b();
                        if (this.d == -1) {
                            bu.a("File Length is not known!");
                        } else if (this.d == -2) {
                            bu.a("File is not access!");
                        } else {
                            this.c = this.d;
                        }
                        this.b = 0;
                    }
                    if (this.j != null) {
                        this.j.n();
                    }
                    if (this.b >= this.c) {
                        obj = null;
                    }
                    if (obj == null) {
                        e();
                    } else {
                        f();
                        this.l.a(this);
                    }
                    return;
                }
                if (this.j != null) {
                    this.j.a(com.amap.api.mapcore.util.bw.a.amap_exception);
                }
                return;
            }
            if (this.j != null) {
                this.j.a(com.amap.api.mapcore.util.bw.a.network_exception);
            }
        } catch (Throwable e) {
            fo.b(e, "SiteFileFetch", "download");
            if (this.j != null) {
                this.j.a(com.amap.api.mapcore.util.bw.a.amap_exception);
            }
        } catch (IOException e2) {
            if (this.j != null) {
                this.j.a(com.amap.api.mapcore.util.bw.a.file_io_exception);
            }
        }
    }

    private boolean h() {
        return (new File(new StringBuilder().append(this.a.b()).append(File.separator).append(this.a.c()).toString()).length() > 10 ? 1 : (new File(new StringBuilder().append(this.a.b()).append(File.separator).append(this.a.c()).toString()).length() == 10 ? 0 : -1)) >= 0;
    }

    private void i() throws AMapException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("11K");
        String stringBuilder2 = stringBuilder.toString();
        if (ez.a != 1) {
            int i = 0;
            while (i < 3) {
                try {
                    ez.a(this.i, eh.e(), stringBuilder2, null);
                    if (ez.a != 1) {
                        i++;
                    } else {
                        return;
                    }
                } catch (Throwable th) {
                    fo.b(th, "NetFileFetch", "authOffLineDownLoad");
                    th.printStackTrace();
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long b() throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(this.a.a()).openConnection();
        httpURLConnection.setRequestProperty("User-Agent", g.d);
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode < AbsAlbumPage.LAUNCH_QUIK_ACTIVITY) {
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
            this.j.p();
        }
        k();
    }

    public void e() {
        j();
        if (this.j != null) {
            this.j.o();
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
            this.j.a(com.amap.api.mapcore.util.bw.a.network_exception);
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
            fo.b(e, "fileAccessI", "fileAccessI.write(byte[] data)");
            if (this.j != null) {
                this.j.a(com.amap.api.mapcore.util.bw.a.file_io_exception);
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
