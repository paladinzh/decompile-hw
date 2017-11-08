package com.amap.api.maps.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PolylineOptions implements Parcelable {
    public static final PolylineOptionsCreator CREATOR = new PolylineOptionsCreator();
    String a;
    private final List<LatLng> b = new ArrayList();
    private float c = 10.0f;
    private int d = -16777216;
    private float e = 0.0f;
    private boolean f = true;
    private BitmapDescriptor g;
    private List<BitmapDescriptor> h;
    private List<Integer> i;
    private List<Integer> j;
    private boolean k = true;
    private boolean l = false;
    private boolean m = false;
    private boolean n = false;

    public PolylineOptions setUseTexture(boolean z) {
        this.k = z;
        return this;
    }

    public PolylineOptions setCustomTexture(BitmapDescriptor bitmapDescriptor) {
        this.g = bitmapDescriptor;
        return this;
    }

    public BitmapDescriptor getCustomTexture() {
        return this.g;
    }

    public PolylineOptions setCustomTextureList(List<BitmapDescriptor> list) {
        this.h = list;
        return this;
    }

    public List<BitmapDescriptor> getCustomTextureList() {
        return this.h;
    }

    public PolylineOptions setCustomTextureIndex(List<Integer> list) {
        this.j = list;
        return this;
    }

    public List<Integer> getCustomTextureIndex() {
        return this.j;
    }

    public PolylineOptions colorValues(List<Integer> list) {
        this.i = list;
        return this;
    }

    public List<Integer> getColorValues() {
        return this.i;
    }

    public PolylineOptions useGradient(boolean z) {
        this.n = z;
        return this;
    }

    public boolean isUseGradient() {
        return this.n;
    }

    public boolean isUseTexture() {
        return this.k;
    }

    public boolean isGeodesic() {
        return this.l;
    }

    public PolylineOptions add(LatLng latLng) {
        this.b.add(latLng);
        return this;
    }

    public PolylineOptions add(LatLng... latLngArr) {
        this.b.addAll(Arrays.asList(latLngArr));
        return this;
    }

    public PolylineOptions addAll(Iterable<LatLng> iterable) {
        for (LatLng add : iterable) {
            this.b.add(add);
        }
        return this;
    }

    public PolylineOptions width(float f) {
        this.c = f;
        return this;
    }

    public PolylineOptions color(int i) {
        this.d = i;
        return this;
    }

    public PolylineOptions zIndex(float f) {
        this.e = f;
        return this;
    }

    public PolylineOptions visible(boolean z) {
        this.f = z;
        return this;
    }

    public PolylineOptions geodesic(boolean z) {
        this.l = z;
        return this;
    }

    public PolylineOptions setDottedLine(boolean z) {
        this.m = z;
        return this;
    }

    public boolean isDottedLine() {
        return this.m;
    }

    public List<LatLng> getPoints() {
        return this.b;
    }

    public float getWidth() {
        return this.c;
    }

    public int getColor() {
        return this.d;
    }

    public float getZIndex() {
        return this.e;
    }

    public boolean isVisible() {
        return this.f;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(this.b);
        parcel.writeFloat(this.c);
        parcel.writeInt(this.d);
        parcel.writeFloat(this.e);
        parcel.writeString(this.a);
        parcel.writeBooleanArray(new boolean[]{this.f, this.m, this.l, this.n});
        if (this.g != null) {
            parcel.writeParcelable(this.g, i);
        }
        if (this.h != null) {
            parcel.writeList(this.h);
        }
        if (this.j != null) {
            parcel.writeList(this.j);
        }
        if (this.i != null) {
            parcel.writeList(this.i);
        }
    }
}
