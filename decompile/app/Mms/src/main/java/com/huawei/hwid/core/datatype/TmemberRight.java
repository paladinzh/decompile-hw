package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class TmemberRight implements Parcelable {
    public static final Creator CREATOR = new j();
    private long a;
    private int b;
    private String c;
    private String d;
    private String e;
    private int f;
    private String g;
    private String h;

    public void a(long j) {
        this.a = j;
    }

    public void a(int i) {
        this.b = i;
    }

    public String a() {
        return this.c;
    }

    public void a(String str) {
        this.c = str;
    }

    public String b() {
        return this.d;
    }

    public void b(String str) {
        this.d = this.c;
    }

    public void c(String str) {
        this.e = str;
    }

    public int c() {
        return this.f;
    }

    public void b(int i) {
        this.f = i;
    }

    public void d(String str) {
        this.g = str;
    }

    public String d() {
        return this.h;
    }

    public void e(String str) {
        this.h = str;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(this.a);
        parcel.writeInt(this.b);
        parcel.writeString(this.c);
        parcel.writeString(this.d);
        parcel.writeString(this.e);
        parcel.writeInt(this.f);
        parcel.writeString(this.g);
        parcel.writeString(this.h);
    }
}
