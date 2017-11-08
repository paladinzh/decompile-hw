package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.maps.internal.r;

/* compiled from: Unknown */
public final class CircleOptions implements SafeParcelable {
    public static final CircleOptionsCreator CREATOR = new CircleOptionsCreator();
    private LatLng PK;
    private double PL;
    private float PM;
    private int PN;
    private int PO;
    private float PP;
    private boolean PQ;
    private final int wj;

    public CircleOptions() {
        this.PK = null;
        this.PL = 0.0d;
        this.PM = 10.0f;
        this.PN = -16777216;
        this.PO = 0;
        this.PP = 0.0f;
        this.PQ = true;
        this.wj = 1;
    }

    CircleOptions(int versionCode, LatLng center, double radius, float strokeWidth, int strokeColor, int fillColor, float zIndex, boolean visible) {
        this.PK = null;
        this.PL = 0.0d;
        this.PM = 10.0f;
        this.PN = -16777216;
        this.PO = 0;
        this.PP = 0.0f;
        this.PQ = true;
        this.wj = versionCode;
        this.PK = center;
        this.PL = radius;
        this.PM = strokeWidth;
        this.PN = strokeColor;
        this.PO = fillColor;
        this.PP = zIndex;
        this.PQ = visible;
    }

    public int describeContents() {
        return 0;
    }

    public LatLng getCenter() {
        return this.PK;
    }

    public int getFillColor() {
        return this.PO;
    }

    public double getRadius() {
        return this.PL;
    }

    public int getStrokeColor() {
        return this.PN;
    }

    public float getStrokeWidth() {
        return this.PM;
    }

    int getVersionCode() {
        return this.wj;
    }

    public float getZIndex() {
        return this.PP;
    }

    public boolean isVisible() {
        return this.PQ;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (r.hc()) {
            b.a(this, out, flags);
        } else {
            CircleOptionsCreator.a(this, out, flags);
        }
    }
}
