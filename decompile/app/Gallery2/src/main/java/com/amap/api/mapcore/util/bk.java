package com.amap.api.mapcore.util;

@fv(a = "update_item")
/* compiled from: DTInfo */
public class bk {
    @fw(a = "title", b = 6)
    protected String a = null;
    @fw(a = "url", b = 6)
    protected String b = null;
    @fw(a = "mAdcode", b = 6)
    protected String c = null;
    @fw(a = "fileName", b = 6)
    protected String d = null;
    @fw(a = "version", b = 6)
    protected String e = "";
    @fw(a = "lLocalLength", b = 5)
    protected long f = 0;
    @fw(a = "lRemoteLength", b = 5)
    protected long g = 0;
    @fw(a = "localPath", b = 6)
    protected String h;
    @fw(a = "isProvince", b = 2)
    protected int i = 0;
    @fw(a = "mCompleteCode", b = 2)
    protected int j;
    @fw(a = "mCityCode", b = 6)
    protected String k = "";
    @fw(a = "mState", b = 2)
    public int l;
    @fw(a = "mPinyin", b = 6)
    public String m = "";

    public String d() {
        return this.a;
    }

    public String e() {
        return this.e;
    }

    public String f() {
        return this.c;
    }

    public void c(String str) {
        this.c = str;
    }

    public int g() {
        return this.j;
    }

    public void d(String str) {
        this.k = str;
    }

    public String h() {
        return this.m;
    }

    public static String e(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mAdcode");
        stringBuilder.append("='");
        stringBuilder.append(str);
        stringBuilder.append("'");
        return stringBuilder.toString();
    }

    public static String f(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mPinyin");
        stringBuilder.append("='");
        stringBuilder.append(str);
        stringBuilder.append("'");
        return stringBuilder.toString();
    }
}
