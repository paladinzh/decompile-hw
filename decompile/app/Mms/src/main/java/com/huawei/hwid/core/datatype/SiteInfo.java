package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SiteInfo implements Parcelable {
    public static final Creator CREATOR = new i();
    private String a;
    private String b;

    public void a(String str) {
        this.a = str;
    }

    public String a() {
        return this.b;
    }

    public void b(String str) {
        this.b = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.a);
        parcel.writeString(this.b);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "[" + this.a + "," + this.b + "]";
    }
}
