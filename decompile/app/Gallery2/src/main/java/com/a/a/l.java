package com.a.a;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import com.a.a.n.b;
import com.android.gallery3d.gadget.XmlUtils;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

/* compiled from: Unknown */
public abstract class l<T> implements Comparable<l<T>> {
    private final a a;
    private final int b;
    private final String c;
    private String d;
    private final int e;
    private final b f;
    private Integer g;
    private m h;
    private boolean i;
    private boolean j;
    private boolean k;
    private long l;
    private p m;
    private com.a.a.b.a n;

    /* compiled from: Unknown */
    public enum a {
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE
    }

    public l(int i, String str, b bVar) {
        this.a = !a.a ? null : new a();
        this.i = true;
        this.j = false;
        this.k = false;
        this.l = 0;
        this.n = null;
        this.b = i;
        this.c = str;
        this.f = bVar;
        a(new d());
        this.e = d(str);
    }

    private byte[] a(Map<String, String> map, String str) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            for (Entry entry : map.entrySet()) {
                stringBuilder.append(URLEncoder.encode((String) entry.getKey(), str));
                stringBuilder.append('=');
                stringBuilder.append(URLEncoder.encode((String) entry.getValue(), str));
                stringBuilder.append('&');
            }
            return stringBuilder.toString().getBytes(str);
        } catch (Throwable e) {
            throw new RuntimeException("Encoding not supported: " + str, e);
        }
    }

    private static int d(String str) {
        if (!TextUtils.isEmpty(str)) {
            Uri parse = Uri.parse(str);
            if (parse != null) {
                String host = parse.getHost();
                if (host != null) {
                    return host.hashCode();
                }
            }
        }
        return 0;
    }

    public int a() {
        return this.b;
    }

    public int a(l<T> lVar) {
        a r = r();
        a r2 = lVar.r();
        return r != r2 ? r2.ordinal() - r.ordinal() : this.g.intValue() - lVar.g.intValue();
    }

    public final l<?> a(int i) {
        this.g = Integer.valueOf(i);
        return this;
    }

    public l<?> a(com.a.a.b.a aVar) {
        this.n = aVar;
        return this;
    }

    public l<?> a(m mVar) {
        this.h = mVar;
        return this;
    }

    public l<?> a(p pVar) {
        this.m = pVar;
        return this;
    }

    protected abstract n<T> a(i iVar);

    protected s a(s sVar) {
        return sVar;
    }

    protected abstract void a(T t);

    public void a(String str) {
        if (a.a) {
            this.a.a(str, Thread.currentThread().getId());
        } else if (this.l == 0) {
            this.l = SystemClock.elapsedRealtime();
        }
    }

    public int b() {
        return this.e;
    }

    public void b(s sVar) {
        if (this.f != null) {
            this.f.a(sVar);
        }
    }

    void b(final String str) {
        if (this.h != null) {
            this.h.b(this);
        }
        if (a.a) {
            final long id = Thread.currentThread().getId();
            if (Looper.myLooper() == Looper.getMainLooper()) {
                this.a.a(str, id);
                this.a.a(toString());
            } else {
                new Handler(Looper.getMainLooper()).post(new Runnable(this) {
                    final /* synthetic */ l c;

                    public void run() {
                        this.c.a.a(str, id);
                        this.c.a.a(toString());
                    }
                });
                return;
            }
        }
        if ((SystemClock.elapsedRealtime() - this.l < 3000 ? 1 : 0) == 0) {
            t.b("%d ms: %s", Long.valueOf(SystemClock.elapsedRealtime() - this.l), toString());
        }
    }

    public String c() {
        return this.d == null ? this.c : this.d;
    }

    public void c(String str) {
        this.d = str;
    }

    public /* synthetic */ int compareTo(Object obj) {
        return a((l) obj);
    }

    public String d() {
        return this.c;
    }

    public String e() {
        return c();
    }

    public com.a.a.b.a f() {
        return this.n;
    }

    public boolean g() {
        return this.j;
    }

    public Map<String, String> h() throws a {
        return Collections.emptyMap();
    }

    @Deprecated
    protected Map<String, String> i() throws a {
        return m();
    }

    @Deprecated
    protected String j() {
        return n();
    }

    @Deprecated
    public String k() {
        return o();
    }

    @Deprecated
    public byte[] l() throws a {
        Map i = i();
        return (i != null && i.size() > 0) ? a(i, j()) : null;
    }

    protected Map<String, String> m() throws a {
        return null;
    }

    protected String n() {
        return XmlUtils.INPUT_ENCODING;
    }

    public String o() {
        return "application/x-www-form-urlencoded; charset=" + n();
    }

    public byte[] p() throws a {
        Map m = m();
        return (m != null && m.size() > 0) ? a(m, n()) : null;
    }

    public final boolean q() {
        return this.i;
    }

    public a r() {
        return a.NORMAL;
    }

    public final int s() {
        return this.m.a();
    }

    public p t() {
        return this.m;
    }

    public String toString() {
        return (!this.j ? "[ ] " : "[X] ") + c() + " " + ("0x" + Integer.toHexString(b())) + " " + r() + " " + this.g;
    }

    public void u() {
        this.k = true;
    }

    public boolean v() {
        return this.k;
    }
}
