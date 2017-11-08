package com.google.android.gms.maps.model;

import android.os.IBinder;
import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.dynamic.b.a;
import com.google.android.gms.maps.internal.r;

/* compiled from: Unknown */
public final class GroundOverlayOptions implements SafeParcelable {
    public static final GroundOverlayOptionsCreator CREATOR = new GroundOverlayOptionsCreator();
    private float PI;
    private float PP;
    private boolean PQ;
    private BitmapDescriptor PS;
    private LatLng PT;
    private float PU;
    private float PV;
    private LatLngBounds PW;
    private float PX;
    private float PY;
    private float PZ;
    private final int wj;

    public GroundOverlayOptions() {
        this.PQ = true;
        this.PX = 0.0f;
        this.PY = 0.5f;
        this.PZ = 0.5f;
        this.wj = 1;
    }

    GroundOverlayOptions(int versionCode, IBinder wrappedImage, LatLng location, float width, float height, LatLngBounds bounds, float bearing, float zIndex, boolean visible, float transparency, float anchorU, float anchorV) {
        this.PQ = true;
        this.PX = 0.0f;
        this.PY = 0.5f;
        this.PZ = 0.5f;
        this.wj = versionCode;
        this.PS = new BitmapDescriptor(a.G(wrappedImage));
        this.PT = location;
        this.PU = width;
        this.PV = height;
        this.PW = bounds;
        this.PI = bearing;
        this.PP = zIndex;
        this.PQ = visible;
        this.PX = transparency;
        this.PY = anchorU;
        this.PZ = anchorV;
    }

    public int describeContents() {
        return 0;
    }

    public float getAnchorU() {
        return this.PY;
    }

    public float getAnchorV() {
        return this.PZ;
    }

    public float getBearing() {
        return this.PI;
    }

    public LatLngBounds getBounds() {
        return this.PW;
    }

    public float getHeight() {
        return this.PV;
    }

    public LatLng getLocation() {
        return this.PT;
    }

    public float getTransparency() {
        return this.PX;
    }

    int getVersionCode() {
        return this.wj;
    }

    public float getWidth() {
        return this.PU;
    }

    public float getZIndex() {
        return this.PP;
    }

    IBinder he() {
        return this.PS.gK().asBinder();
    }

    public boolean isVisible() {
        return this.PQ;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (r.hc()) {
            c.a(this, out, flags);
        } else {
            GroundOverlayOptionsCreator.a(this, out, flags);
        }
    }
}
