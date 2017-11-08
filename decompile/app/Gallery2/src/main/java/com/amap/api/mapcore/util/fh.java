package com.amap.api.mapcore.util;

import android.text.TextUtils;
import java.util.HashMap;
import java.util.Map;

@fv(a = "a")
/* compiled from: SDKInfo */
public class fh {
    @fw(a = "a1", b = 6)
    private String a;
    @fw(a = "a2", b = 6)
    private String b;
    @fw(a = "a6", b = 2)
    private int c;
    @fw(a = "a3", b = 6)
    private String d;
    @fw(a = "a4", b = 6)
    private String e;
    @fw(a = "a5", b = 6)
    private String f;
    private String g;
    private String h;
    private String i;
    private String j;
    private String k;
    private String[] l;

    /* compiled from: SDKInfo */
    public static class a {
        private String a;
        private String b;
        private String c;
        private String d;
        private boolean e = true;
        private String f = "standard";
        private String[] g = null;

        public a(String str, String str2, String str3) {
            this.a = str2;
            this.b = str2;
            this.d = str3;
            this.c = str;
        }

        public a a(String[] strArr) {
            this.g = (String[]) strArr.clone();
            return this;
        }

        public a a(String str) {
            this.b = str;
            return this;
        }

        public fh a() throws ex {
            if (this.g != null) {
                return new fh();
            }
            throw new ex("sdk packages is null");
        }
    }

    private fh() {
        this.c = 1;
        this.l = null;
    }

    private fh(a aVar) {
        int i = 0;
        this.c = 1;
        this.l = null;
        this.g = aVar.a;
        this.h = aVar.b;
        this.j = aVar.c;
        this.i = aVar.d;
        if (aVar.e) {
            i = 1;
        }
        this.c = i;
        this.k = aVar.f;
        this.l = aVar.g;
        this.b = fi.b(this.h);
        this.a = fi.b(this.j);
        this.d = fi.b(this.i);
        this.e = fi.b(a(this.l));
        this.f = fi.b(this.k);
    }

    public void a(boolean z) {
        int i = 0;
        if (z) {
            i = 1;
        }
        this.c = i;
    }

    public String a() {
        if (TextUtils.isEmpty(this.j) && !TextUtils.isEmpty(this.a)) {
            this.j = fi.c(this.a);
        }
        return this.j;
    }

    public String b() {
        return this.g;
    }

    public String c() {
        if (TextUtils.isEmpty(this.h) && !TextUtils.isEmpty(this.b)) {
            this.h = fi.c(this.b);
        }
        return this.h;
    }

    public String d() {
        if (TextUtils.isEmpty(this.i) && !TextUtils.isEmpty(this.d)) {
            this.i = fi.c(this.d);
        }
        return this.i;
    }

    public String e() {
        if (TextUtils.isEmpty(this.k) && !TextUtils.isEmpty(this.f)) {
            this.k = fi.c(this.f);
        }
        if (TextUtils.isEmpty(this.k)) {
            this.k = "standard";
        }
        return this.k;
    }

    public String[] f() {
        if (this.l == null || this.l.length == 0) {
            if (!TextUtils.isEmpty(this.e)) {
                this.l = b(fi.c(this.e));
            }
        }
        return (String[]) this.l.clone();
    }

    private String[] b(String str) {
        try {
            return str.split(";");
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    private String a(String[] strArr) {
        if (strArr == null) {
            return null;
        }
        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (String append : strArr) {
                stringBuilder.append(append).append(";");
            }
            return stringBuilder.toString();
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    public static String a(String str) {
        Map hashMap = new HashMap();
        hashMap.put("a1", fi.b(str));
        return fu.a(hashMap);
    }

    public static String g() {
        return "a6=1";
    }
}
