package com.amap.api.services.geocoder;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.amap.api.services.core.LatLonPoint;

public final class GeocodeAddress implements Parcelable {
    public static final Creator<GeocodeAddress> CREATOR = new b();
    private String a;
    private String b;
    private String c;
    private String d;
    private String e;
    private String f;
    private String g;
    private String h;
    private LatLonPoint i;
    private String j;

    public String getFormatAddress() {
        return this.a;
    }

    public void setFormatAddress(String str) {
        this.a = str;
    }

    public String getProvince() {
        return this.b;
    }

    public void setProvince(String str) {
        this.b = str;
    }

    public String getCity() {
        return this.c;
    }

    public void setCity(String str) {
        this.c = str;
    }

    public String getDistrict() {
        return this.d;
    }

    public void setDistrict(String str) {
        this.d = str;
    }

    public String getTownship() {
        return this.e;
    }

    public void setTownship(String str) {
        this.e = str;
    }

    public String getNeighborhood() {
        return this.f;
    }

    public void setNeighborhood(String str) {
        this.f = str;
    }

    public String getBuilding() {
        return this.g;
    }

    public void setBuilding(String str) {
        this.g = str;
    }

    public String getAdcode() {
        return this.h;
    }

    public void setAdcode(String str) {
        this.h = str;
    }

    public LatLonPoint getLatLonPoint() {
        return this.i;
    }

    public void setLatLonPoint(LatLonPoint latLonPoint) {
        this.i = latLonPoint;
    }

    public String getLevel() {
        return this.j;
    }

    public void setLevel(String str) {
        this.j = str;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.a);
        parcel.writeString(this.b);
        parcel.writeString(this.c);
        parcel.writeString(this.d);
        parcel.writeString(this.e);
        parcel.writeString(this.f);
        parcel.writeString(this.g);
        parcel.writeString(this.h);
        parcel.writeValue(this.i);
        parcel.writeString(this.j);
    }

    private GeocodeAddress(Parcel parcel) {
        this.a = parcel.readString();
        this.b = parcel.readString();
        this.c = parcel.readString();
        this.d = parcel.readString();
        this.e = parcel.readString();
        this.f = parcel.readString();
        this.g = parcel.readString();
        this.h = parcel.readString();
        this.i = (LatLonPoint) parcel.readValue(LatLonPoint.class.getClassLoader());
        this.j = parcel.readString();
    }
}
