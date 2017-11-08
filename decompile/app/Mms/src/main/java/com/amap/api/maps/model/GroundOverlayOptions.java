package com.amap.api.maps.model;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.amap.api.mapcore.util.au;

public final class GroundOverlayOptions implements Parcelable {
    public static final GroundOverlayOptionsCreator CREATOR = new GroundOverlayOptionsCreator();
    public static final float NO_DIMENSION = -1.0f;
    private final int a;
    private BitmapDescriptor b;
    private LatLng c;
    private float d;
    private float e;
    private LatLngBounds f;
    private float g;
    private float h;
    private boolean i;
    private float j;
    private float k;
    private float l;

    GroundOverlayOptions(int i, IBinder iBinder, LatLng latLng, float f, float f2, LatLngBounds latLngBounds, float f3, float f4, boolean z, float f5, float f6, float f7) {
        this.h = 0.0f;
        this.i = true;
        this.j = 0.0f;
        this.k = 0.5f;
        this.l = 0.5f;
        this.a = i;
        this.b = BitmapDescriptorFactory.fromBitmap(null);
        this.c = latLng;
        this.d = f;
        this.e = f2;
        this.f = latLngBounds;
        this.g = f3;
        this.h = f4;
        this.i = z;
        this.j = f5;
        this.k = f6;
        this.l = f7;
    }

    public GroundOverlayOptions() {
        this.h = 0.0f;
        this.i = true;
        this.j = 0.0f;
        this.k = 0.5f;
        this.l = 0.5f;
        this.a = 1;
    }

    public GroundOverlayOptions image(BitmapDescriptor bitmapDescriptor) {
        this.b = bitmapDescriptor;
        return this;
    }

    public GroundOverlayOptions anchor(float f, float f2) {
        this.k = f;
        this.l = f2;
        return this;
    }

    public GroundOverlayOptions position(LatLng latLng, float f) {
        boolean z;
        boolean z2 = true;
        if (this.f != null) {
            z = false;
        } else {
            z = true;
        }
        au.a(z, (Object) "Position has already been set using positionFromBounds");
        if (latLng == null) {
            z = false;
        } else {
            z = true;
        }
        au.b(z, "Location must be specified");
        if (f < 0.0f) {
            z2 = false;
        }
        au.b(z2, "Width must be non-negative");
        return a(latLng, f, f);
    }

    public GroundOverlayOptions position(LatLng latLng, float f, float f2) {
        boolean z;
        boolean z2 = true;
        if (this.f != null) {
            z = false;
        } else {
            z = true;
        }
        au.a(z, (Object) "Position has already been set using positionFromBounds");
        if (latLng == null) {
            z = false;
        } else {
            z = true;
        }
        au.b(z, "Location must be specified");
        if (f >= 0.0f) {
            z = true;
        } else {
            z = false;
        }
        au.b(z, "Width must be non-negative");
        if (f2 < 0.0f) {
            z2 = false;
        }
        au.b(z2, "Height must be non-negative");
        return a(latLng, f, f2);
    }

    private GroundOverlayOptions a(LatLng latLng, float f, float f2) {
        this.c = latLng;
        this.d = f;
        this.e = f2;
        return this;
    }

    public GroundOverlayOptions positionFromBounds(LatLngBounds latLngBounds) {
        boolean z;
        if (this.c != null) {
            z = false;
        } else {
            z = true;
        }
        au.a(z, "Position has already been set using position: " + this.c);
        this.f = latLngBounds;
        return this;
    }

    public GroundOverlayOptions bearing(float f) {
        this.g = f;
        return this;
    }

    public GroundOverlayOptions zIndex(float f) {
        this.h = f;
        return this;
    }

    public GroundOverlayOptions visible(boolean z) {
        this.i = z;
        return this;
    }

    public GroundOverlayOptions transparency(float f) {
        boolean z;
        if (f < 0.0f || f > ContentUtil.FONT_SIZE_NORMAL) {
            z = false;
        } else {
            z = true;
        }
        au.b(z, "Transparency must be in the range [0..1]");
        this.j = f;
        return this;
    }

    public BitmapDescriptor getImage() {
        return this.b;
    }

    public LatLng getLocation() {
        return this.c;
    }

    public float getWidth() {
        return this.d;
    }

    public float getHeight() {
        return this.e;
    }

    public LatLngBounds getBounds() {
        return this.f;
    }

    public float getBearing() {
        return this.g;
    }

    public float getZIndex() {
        return this.h;
    }

    public float getTransparency() {
        return this.j;
    }

    public float getAnchorU() {
        return this.k;
    }

    public float getAnchorV() {
        return this.l;
    }

    public boolean isVisible() {
        return this.i;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        int i2 = 0;
        parcel.writeInt(this.a);
        parcel.writeParcelable(this.b, i);
        parcel.writeParcelable(this.c, i);
        parcel.writeFloat(this.d);
        parcel.writeFloat(this.e);
        parcel.writeParcelable(this.f, i);
        parcel.writeFloat(this.g);
        parcel.writeFloat(this.h);
        if (this.i) {
            i2 = 1;
        }
        parcel.writeByte((byte) i2);
        parcel.writeFloat(this.j);
        parcel.writeFloat(this.k);
        parcel.writeFloat(this.l);
    }
}
