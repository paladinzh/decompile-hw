package com.amap.api.services.road;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class Crossroad extends Road implements Parcelable {
    public static final Creator<Crossroad> CREATOR = new a();
    private float a;
    private String b;
    private String c;
    private String d;
    private String e;
    private String f;

    public float getDistance() {
        return this.a;
    }

    public void setDistance(float f) {
        this.a = f;
    }

    public String getDirection() {
        return this.b;
    }

    public void setDirection(String str) {
        this.b = str;
    }

    public String getFirstRoadId() {
        return this.c;
    }

    public void setFirstRoadId(String str) {
        this.c = str;
    }

    public String getFirstRoadName() {
        return this.d;
    }

    public void setFirstRoadName(String str) {
        this.d = str;
    }

    public String getSecondRoadId() {
        return this.e;
    }

    public void setSecondRoadId(String str) {
        this.e = str;
    }

    public String getSecondRoadName() {
        return this.f;
    }

    public void setSecondRoadName(String str) {
        this.f = str;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeFloat(this.a);
        parcel.writeString(this.b);
        parcel.writeString(this.c);
        parcel.writeString(this.d);
        parcel.writeString(this.e);
        parcel.writeString(this.f);
    }

    private Crossroad(Parcel parcel) {
        super(parcel);
        this.a = parcel.readFloat();
        this.b = parcel.readString();
        this.c = parcel.readString();
        this.d = parcel.readString();
        this.e = parcel.readString();
        this.f = parcel.readString();
    }
}
