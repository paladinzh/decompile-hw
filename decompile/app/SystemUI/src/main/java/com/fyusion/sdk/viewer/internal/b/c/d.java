package com.fyusion.sdk.viewer.internal.b.c;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.fyusion.sdk.viewer.internal.b.e;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Map;

/* compiled from: Unknown */
public class d implements e {
    private final e b;
    @Nullable
    private final URL c;
    @Nullable
    private final String d;
    @Nullable
    private String e;
    @Nullable
    private URL f;
    @Nullable
    private volatile byte[] g;
    private String h;

    public d(String str) {
        this(str, e.b);
    }

    public d(String str, e eVar) {
        this.c = null;
        this.d = com.fyusion.sdk.viewer.internal.f.d.a(str);
        this.b = (e) com.fyusion.sdk.viewer.internal.f.d.a((Object) eVar);
    }

    public d(String str, String str2) {
        this(str2, e.b);
        this.h = str;
    }

    private URL e() throws MalformedURLException {
        if (this.f == null) {
            this.f = new URL(f());
        }
        return this.f;
    }

    private String f() {
        if (TextUtils.isEmpty(this.e)) {
            String str = this.d;
            if (TextUtils.isEmpty(str)) {
                str = this.c.toString();
            }
            this.e = Uri.encode(str, "@#&=*+-_.,:!?()/~'%");
        }
        return this.e;
    }

    private byte[] g() {
        if (this.g == null) {
            this.g = c().getBytes(a);
        }
        return this.g;
    }

    public URL a() throws MalformedURLException {
        return e();
    }

    public void a(MessageDigest messageDigest) {
        messageDigest.update(g());
    }

    public Map<String, String> b() {
        return this.b.a();
    }

    public String c() {
        return this.d == null ? this.c.toString() : this.d;
    }

    public String d() {
        return this.h;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof d)) {
            return false;
        }
        d dVar = (d) obj;
        if (c().equals(dVar.c()) && this.b.equals(dVar.b)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return (c().hashCode() * 31) + this.b.hashCode();
    }

    public String toString() {
        return c();
    }
}
