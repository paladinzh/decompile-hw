package com.amap.api.mapcore.util;

@cl(a = "update_item")
/* compiled from: DTInfo */
public class v {
    @cm(a = "title", b = 6)
    protected String a = null;
    @cm(a = "url", b = 6)
    protected String b = null;
    @cm(a = "mAdcode", b = 6)
    protected String c = null;
    @cm(a = "fileName", b = 6)
    protected String d = null;
    @cm(a = "version", b = 6)
    protected String e = "";
    @cm(a = "lLocalLength", b = 5)
    protected long f = 0;
    @cm(a = "lRemoteLength", b = 5)
    protected long g = 0;
    @cm(a = "localPath", b = 6)
    protected String h;
    @cm(a = "isProvince", b = 2)
    protected int i = 0;
    @cm(a = "mCompleteCode", b = 2)
    protected int j;
    @cm(a = "mCityCode", b = 6)
    protected String k = "";
    @cm(a = "mState", b = 2)
    public int l;

    public String e() {
        return this.a;
    }

    public String f() {
        return this.e;
    }

    public String g() {
        return this.c;
    }

    public String h() {
        return this.b;
    }

    public long i() {
        return this.g;
    }

    public void a(long j) {
        this.f = j;
    }

    public void a(int i) {
        this.j = i;
    }

    public int j() {
        return this.j;
    }

    public void c(String str) {
        this.k = str;
    }

    public static String d(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mAdcode");
        stringBuilder.append("='");
        stringBuilder.append(str);
        stringBuilder.append("'");
        return stringBuilder.toString();
    }
}
