package com.amap.api.services.core;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class LatLonSharePoint extends LatLonPoint implements Parcelable {
    public static final Creator<LatLonSharePoint> CREATOR = new p();
    private String a;

    public LatLonSharePoint(double d, double d2, String str) {
        super(d, d2);
        this.a = str;
    }

    public String getSharePointName() {
        return this.a;
    }

    public void setSharePointName(String str) {
        this.a = str;
    }

    protected LatLonSharePoint(Parcel parcel) {
        super(parcel);
        this.a = parcel.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(this.a);
    }

    public int hashCode() {
        int hashCode;
        int hashCode2 = super.hashCode() * 31;
        if (this.a != null) {
            hashCode = this.a.hashCode();
        } else {
            hashCode = 0;
        }
        return hashCode + hashCode2;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        LatLonSharePoint latLonSharePoint = (LatLonSharePoint) obj;
        if (this.a != null) {
            return this.a.equals(latLonSharePoint.a);
        } else {
            if (latLonSharePoint.a != null) {
                return false;
            }
        }
    }

    public String toString() {
        return super.toString() + "," + this.a;
    }
}
