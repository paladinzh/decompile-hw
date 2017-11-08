package com.amap.api.maps.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import cn.com.xy.sms.sdk.constant.Constant;

public final class CircleOptions implements Parcelable {
    public static final CircleOptionsCreator CREATOR = new CircleOptionsCreator();
    String a;
    private LatLng b = null;
    private double c = 0.0d;
    private float d = 10.0f;
    private int e = -16777216;
    private int f = 0;
    private float g = 0.0f;
    private boolean h = true;

    public void writeToParcel(Parcel parcel, int i) {
        int i2 = 0;
        Bundle bundle = new Bundle();
        if (this.b != null) {
            bundle.putDouble(Constant.LOACTION_LATITUDE, this.b.latitude);
            bundle.putDouble(Constant.LOACTION_LONGITUDE, this.b.longitude);
        }
        parcel.writeBundle(bundle);
        parcel.writeDouble(this.c);
        parcel.writeFloat(this.d);
        parcel.writeInt(this.e);
        parcel.writeInt(this.f);
        parcel.writeFloat(this.g);
        if (this.h) {
            i2 = 1;
        }
        parcel.writeByte((byte) i2);
        parcel.writeString(this.a);
    }

    public int describeContents() {
        return 0;
    }

    public CircleOptions center(LatLng latLng) {
        this.b = latLng;
        return this;
    }

    public CircleOptions radius(double d) {
        this.c = d;
        return this;
    }

    public CircleOptions strokeWidth(float f) {
        this.d = f;
        return this;
    }

    public CircleOptions strokeColor(int i) {
        this.e = i;
        return this;
    }

    public CircleOptions fillColor(int i) {
        this.f = i;
        return this;
    }

    public CircleOptions zIndex(float f) {
        this.g = f;
        return this;
    }

    public CircleOptions visible(boolean z) {
        this.h = z;
        return this;
    }

    public LatLng getCenter() {
        return this.b;
    }

    public double getRadius() {
        return this.c;
    }

    public float getStrokeWidth() {
        return this.d;
    }

    public int getStrokeColor() {
        return this.e;
    }

    public int getFillColor() {
        return this.f;
    }

    public float getZIndex() {
        return this.g;
    }

    public boolean isVisible() {
        return this.h;
    }
}
