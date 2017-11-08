package com.amap.api.mapcore.util;

import android.text.TextUtils;
import java.util.HashMap;
import java.util.Map;

@cl(a = "a")
/* compiled from: SDKInfo */
public class bv {
    String a;
    String b;
    String c;
    @cm(a = "a1", b = 6)
    private String d;
    @cm(a = "a2", b = 6)
    private String e;
    @cm(a = "a6", b = 2)
    private int f;
    @cm(a = "a3", b = 6)
    private String g;
    @cm(a = "a4", b = 6)
    private String h;
    @cm(a = "a5", b = 6)
    private String i;
    private String j;
    private String[] k;

    /* compiled from: SDKInfo */
    public static class a {
        private String a;
        private String b;
        private String c;
        private boolean d = true;
        private String e = "standard";
        private String[] f = null;

        public a(String str, String str2, String str3) {
            this.a = str2;
            this.c = str3;
            this.b = str;
        }

        public a a(String[] strArr) {
            this.f = (String[]) strArr.clone();
            return this;
        }

        public bv a() throws bk {
            if (this.f != null) {
                return new bv();
            }
            throw new bk("sdk packages is null");
        }
    }

    private bv() {
        this.f = 1;
        this.k = null;
    }

    private bv(a aVar) {
        int i = 0;
        this.f = 1;
        this.k = null;
        this.a = aVar.a;
        this.c = aVar.b;
        this.b = aVar.c;
        if (aVar.d) {
            i = 1;
        }
        this.f = i;
        this.j = aVar.e;
        this.k = aVar.f;
        this.e = bx.b(this.a);
        this.d = bx.b(this.c);
        this.g = bx.b(this.b);
        this.h = bx.b(a(this.k));
        this.i = bx.b(this.j);
    }

    public void a(boolean z) {
        int i = 0;
        if (z) {
            i = 1;
        }
        this.f = i;
    }

    public String a() {
        if (TextUtils.isEmpty(this.c) && !TextUtils.isEmpty(this.d)) {
            this.c = bx.c(this.d);
        }
        return this.c;
    }

    public String b() {
        if (TextUtils.isEmpty(this.a) && !TextUtils.isEmpty(this.e)) {
            this.a = bx.c(this.e);
        }
        return this.a;
    }

    public String c() {
        if (TextUtils.isEmpty(this.b) && !TextUtils.isEmpty(this.g)) {
            this.b = bx.c(this.g);
        }
        return this.b;
    }

    public String d() {
        if (TextUtils.isEmpty(this.j) && !TextUtils.isEmpty(this.i)) {
            this.j = bx.c(this.i);
        }
        if (TextUtils.isEmpty(this.j)) {
            this.j = "standard";
        }
        return this.j;
    }

    public String[] e() {
        if (this.k == null || this.k.length == 0) {
            if (!TextUtils.isEmpty(this.h)) {
                this.k = b(bx.c(this.h));
            }
        }
        return (String[]) this.k.clone();
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
        hashMap.put("a1", bx.b(str));
        return ck.a(hashMap);
    }

    public static String f() {
        return "a6=1";
    }
}
