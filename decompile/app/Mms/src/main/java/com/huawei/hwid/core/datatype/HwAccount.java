package com.huawei.hwid.core.datatype;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import cn.com.xy.sms.sdk.net.NetUtil;
import com.huawei.hwid.core.encrypt.f;

public class HwAccount implements Parcelable {
    public static final Creator CREATOR = new f();
    private String a;
    private String b;
    private String c;
    private String d;
    private int e = 0;
    private String f;
    private String g;
    private String h;
    private String i;

    public String a() {
        return this.c;
    }

    public void a(String str) {
        this.c = str;
    }

    public String b() {
        return this.a;
    }

    public void b(String str) {
        this.a = str;
    }

    public String c() {
        return this.d;
    }

    public void c(String str) {
        this.d = str;
    }

    public int d() {
        return this.e;
    }

    public void a(int i) {
        this.e = i;
    }

    public String e() {
        return this.f;
    }

    public void d(String str) {
        this.f = str;
    }

    public String f() {
        return this.b;
    }

    public void e(String str) {
        this.b = str;
    }

    public String g() {
        return this.i;
    }

    public void f(String str) {
        this.i = str;
    }

    public String toString() {
        Bundle bundle = new Bundle();
        bundle.putString("accountName", f.c(this.c));
        bundle.putString(NetUtil.REQ_QUERY_TOEKN, f.a(this.b));
        bundle.putString("tokenType", this.a);
        bundle.putString("userId", f.a(this.d));
        bundle.putString("siteId", String.valueOf(this.e));
        bundle.putString("cookie", this.f);
        bundle.putString("deviceId", f.a(this.g));
        bundle.putString("deviceType", this.h);
        bundle.putString("accountType", this.i);
        return f.a(bundle);
    }

    public String h() {
        return this.g;
    }

    public void g(String str) {
        this.g = str;
    }

    public String i() {
        return this.h;
    }

    public void h(String str) {
        this.h = str;
    }

    public Bundle j() {
        Bundle bundle = new Bundle();
        bundle.putString("requestTokenType", b());
        bundle.putString("serviceToken", f());
        bundle.putString("accountName", a());
        bundle.putString("userId", c());
        bundle.putInt("siteId", d());
        bundle.putString("Cookie", e());
        bundle.putString("deviceId", h());
        bundle.putString("deviceType", i());
        bundle.putString("accountType", g());
        return bundle;
    }

    public Bundle k() {
        Bundle bundle = new Bundle();
        bundle.putString("requestTokenType", b());
        bundle.putString("serviceToken", f());
        bundle.putString("accountName", a());
        bundle.putString("userId", c());
        bundle.putInt("siteId", d());
        bundle.putString("deviceId", h());
        bundle.putString("deviceType", i());
        bundle.putString("accountType", g());
        return bundle;
    }

    public HwAccount a(Bundle bundle) {
        b(bundle.getString("requestTokenType"));
        e(bundle.getString("serviceToken"));
        a(bundle.getString("accountName"));
        c(bundle.getString("userId"));
        a(bundle.getInt("siteId"));
        d(bundle.getString("Cookie"));
        g(bundle.getString("deviceId"));
        h(bundle.getString("deviceType"));
        f(bundle.getString("accountType"));
        return this;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.a);
        parcel.writeString(this.b);
        parcel.writeString(this.c);
        parcel.writeString(this.d);
        parcel.writeInt(this.e);
        parcel.writeString(this.f);
        parcel.writeString(this.g);
        parcel.writeString(this.h);
        parcel.writeString(this.i);
    }
}
