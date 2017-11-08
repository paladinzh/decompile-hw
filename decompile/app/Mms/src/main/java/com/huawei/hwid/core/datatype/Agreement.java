package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class Agreement implements Parcelable {
    public static final Creator CREATOR = new a();
    private String a;
    private String b;
    private String c;
    private String d;
    private String e;
    private String f;
    private String g;
    private String h;

    public String a() {
        return this.a;
    }

    public String b() {
        return this.d;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.a);
        parcel.writeString(this.e);
        parcel.writeString(this.c);
        parcel.writeString(this.f);
        parcel.writeString(this.d);
        parcel.writeString(this.g);
        parcel.writeString(this.b);
        parcel.writeString(this.h);
    }

    public String toString() {
        return "[" + this.a + "," + this.c + "," + this.d + "," + this.b + "]";
    }
}
