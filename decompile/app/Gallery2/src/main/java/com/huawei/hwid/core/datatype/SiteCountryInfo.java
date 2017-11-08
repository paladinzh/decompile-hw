package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class SiteCountryInfo implements Parcelable {
    public static final Creator<SiteCountryInfo> CREATOR = new g();
    private String a = "";
    private String b = "";
    private String c = "";
    private String d = "";
    private int e = 0;
    private int f = 0;
    private String g = "";
    private int h = 0;
    private int i = 0;
    private int j = 0;
    private int k = 0;
    private int l = 0;
    private List<String> m = new ArrayList();

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.a);
        parcel.writeString(this.b);
        parcel.writeString(this.c);
        parcel.writeString(this.d);
        parcel.writeString(this.g);
        parcel.writeInt(this.e);
        parcel.writeInt(this.f);
        parcel.writeInt(this.h);
        parcel.writeInt(this.i);
        parcel.writeInt(this.j);
        parcel.writeInt(this.k);
        parcel.writeInt(this.l);
        parcel.readStringList(this.m);
    }
}
