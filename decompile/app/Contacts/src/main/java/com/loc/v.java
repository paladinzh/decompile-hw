package com.loc;

/* compiled from: SDKInfo */
public class v {
    String a;
    String b;
    String c;
    private boolean d;
    private String e;
    private String[] f;

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

        public a a(String str) {
            this.e = str;
            return this;
        }

        public a a(boolean z) {
            this.d = z;
            return this;
        }

        public a a(String[] strArr) {
            this.f = (String[]) strArr.clone();
            return this;
        }

        public v a() throws l {
            if (this.f != null) {
                return new v();
            }
            throw new l("sdk packages is null");
        }
    }

    private v(a aVar) {
        this.d = true;
        this.e = "standard";
        this.f = null;
        this.a = aVar.a;
        this.c = aVar.b;
        this.b = aVar.c;
        this.d = aVar.d;
        this.e = aVar.e;
        this.f = aVar.f;
    }

    public String a() {
        return this.c;
    }

    public void a(boolean z) {
        this.d = z;
    }

    public String b() {
        return this.a;
    }

    public String c() {
        return this.b;
    }

    public String d() {
        return this.e;
    }

    public boolean e() {
        return this.d;
    }

    public String[] f() {
        return (String[]) this.f.clone();
    }
}
