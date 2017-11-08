package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public final class CircleOptions implements SafeParcelable {
    public static final zzb CREATOR = new zzb();
    private final int mVersionCode;
    private LatLng zzaTc;
    private double zzaTd;
    private float zzaTe;
    private int zzaTf;
    private int zzaTg;
    private float zzaTh;
    private boolean zzaTi;

    public CircleOptions() {
        this.zzaTc = null;
        this.zzaTd = 0.0d;
        this.zzaTe = 10.0f;
        this.zzaTf = -16777216;
        this.zzaTg = 0;
        this.zzaTh = 0.0f;
        this.zzaTi = true;
        this.mVersionCode = 1;
    }

    CircleOptions(int versionCode, LatLng center, double radius, float strokeWidth, int strokeColor, int fillColor, float zIndex, boolean visible) {
        this.zzaTc = null;
        this.zzaTd = 0.0d;
        this.zzaTe = 10.0f;
        this.zzaTf = -16777216;
        this.zzaTg = 0;
        this.zzaTh = 0.0f;
        this.zzaTi = true;
        this.mVersionCode = versionCode;
        this.zzaTc = center;
        this.zzaTd = radius;
        this.zzaTe = strokeWidth;
        this.zzaTf = strokeColor;
        this.zzaTg = fillColor;
        this.zzaTh = zIndex;
        this.zzaTi = visible;
    }

    public CircleOptions center(LatLng center) {
        this.zzaTc = center;
        return this;
    }

    public int describeContents() {
        return 0;
    }

    public CircleOptions fillColor(int color) {
        this.zzaTg = color;
        return this;
    }

    public LatLng getCenter() {
        return this.zzaTc;
    }

    public int getFillColor() {
        return this.zzaTg;
    }

    public double getRadius() {
        return this.zzaTd;
    }

    public int getStrokeColor() {
        return this.zzaTf;
    }

    public float getStrokeWidth() {
        return this.zzaTe;
    }

    int getVersionCode() {
        return this.mVersionCode;
    }

    public float getZIndex() {
        return this.zzaTh;
    }

    public boolean isVisible() {
        return this.zzaTi;
    }

    public CircleOptions radius(double radius) {
        this.zzaTd = radius;
        return this;
    }

    public CircleOptions strokeColor(int color) {
        this.zzaTf = color;
        return this;
    }

    public CircleOptions strokeWidth(float width) {
        this.zzaTe = width;
        return this;
    }

    public CircleOptions visible(boolean visible) {
        this.zzaTi = visible;
        return this;
    }

    public void writeToParcel(Parcel out, int flags) {
        zzb.zza(this, out, flags);
    }

    public CircleOptions zIndex(float zIndex) {
        this.zzaTh = zIndex;
        return this;
    }
}
