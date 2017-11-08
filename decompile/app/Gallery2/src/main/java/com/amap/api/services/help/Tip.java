package com.amap.api.services.help;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class Tip implements Parcelable {
    public static final Creator<Tip> CREATOR = new a();
    private String a;
    private String b;
    private String c;

    public String getName() {
        return this.a;
    }

    public void setName(String str) {
        this.a = str;
    }

    public String getDistrict() {
        return this.b;
    }

    public void setDistrict(String str) {
        this.b = str;
    }

    public String getAdcode() {
        return this.c;
    }

    public void setAdcode(String str) {
        this.c = str;
    }

    public String toString() {
        return "name:" + this.a + " district:" + this.b + " adcode:" + this.c;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.a);
        parcel.writeString(this.c);
        parcel.writeString(this.b);
    }

    private Tip(Parcel parcel) {
        this.a = parcel.readString();
        this.c = parcel.readString();
        this.b = parcel.readString();
    }
}
