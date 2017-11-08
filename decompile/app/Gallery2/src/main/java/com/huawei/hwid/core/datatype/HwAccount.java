package com.huawei.hwid.core.datatype;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import com.huawei.hwid.core.encrypt.f;

public class HwAccount implements Parcelable {
    public static final Creator<HwAccount> CREATOR = new d();
    private String a;
    private String b;
    private String c;
    private String d;
    private int e = 0;
    private String f;
    private String g;
    private String h;
    private String i;
    private String j = "";
    private String k;

    public String a() {
        return this.k;
    }

    public void a(String str) {
        this.k = str;
    }

    public String b() {
        return this.c;
    }

    public void b(String str) {
        this.c = str;
    }

    public String c() {
        return this.a;
    }

    public void c(String str) {
        this.a = str;
    }

    public String d() {
        return this.d;
    }

    public void d(String str) {
        this.d = str;
    }

    public int e() {
        return this.e;
    }

    public void a(int i) {
        this.e = i;
    }

    public String f() {
        return this.f;
    }

    public void e(String str) {
        this.f = str;
    }

    public String g() {
        return this.b;
    }

    public void f(String str) {
        this.b = str;
    }

    public String h() {
        return this.i;
    }

    public void g(String str) {
        this.i = str;
    }

    public String toString() {
        Bundle bundle = new Bundle();
        bundle.putString("accountName", f.c(this.c));
        bundle.putString("token", f.a(this.b, true));
        bundle.putString("tokenType", this.a);
        bundle.putString("userId", f.a(this.d));
        bundle.putString("siteId", String.valueOf(this.e));
        bundle.putString("cookie", this.f);
        bundle.putString("deviceId", f.a(this.g));
        bundle.putString("deviceType", this.h);
        bundle.putString("accountType", this.i);
        bundle.putString("loginUserName", this.j);
        bundle.putString("isoCountryCode", this.k);
        return f.a(bundle);
    }

    public String i() {
        return this.g;
    }

    public void h(String str) {
        this.g = str;
    }

    public String j() {
        return this.h;
    }

    public void i(String str) {
        this.h = str;
    }

    public Bundle k() {
        Bundle bundle = new Bundle();
        bundle.putString("requestTokenType", c());
        bundle.putString("serviceToken", g());
        bundle.putString("accountName", b());
        bundle.putString("userId", d());
        bundle.putInt("siteId", e());
        bundle.putString("Cookie", f());
        bundle.putString("deviceId", i());
        bundle.putString("deviceType", j());
        bundle.putString("accountType", h());
        this.j = m();
        if (TextUtils.isEmpty(this.j)) {
            this.j = b();
        }
        bundle.putString("loginUserName", this.j);
        bundle.putString("countryIsoCode", a());
        return bundle;
    }

    public HwAccount a(Bundle bundle) {
        c(bundle.getString("requestTokenType"));
        f(bundle.getString("serviceToken"));
        b(bundle.getString("accountName"));
        d(bundle.getString("userId"));
        a(bundle.getInt("siteId"));
        e(bundle.getString("Cookie"));
        h(bundle.getString("deviceId"));
        i(bundle.getString("deviceType"));
        g(bundle.getString("accountType"));
        j(bundle.getString("loginUserName"));
        a(bundle.getString("countryIsoCode"));
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
        parcel.writeString(this.j);
        parcel.writeString(this.k);
    }

    public String m() {
        return this.j;
    }

    public void j(String str) {
        this.j = str;
    }
}
