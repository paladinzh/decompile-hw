package com.amap.api.maps.model;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class NavigateArrowOptions implements Parcelable {
    public static final NavigateArrowOptionsCreator CREATOR = new NavigateArrowOptionsCreator();
    String a;
    private final List<LatLng> b = new ArrayList();
    private float c = 10.0f;
    private int d = Color.argb(221, 87, 235, 204);
    private int e = Color.argb(170, 0, 172, 146);
    private float f = 0.0f;
    private boolean g = true;

    public NavigateArrowOptions add(LatLng latLng) {
        this.b.add(latLng);
        return this;
    }

    public NavigateArrowOptions add(LatLng... latLngArr) {
        this.b.addAll(Arrays.asList(latLngArr));
        return this;
    }

    public NavigateArrowOptions addAll(Iterable<LatLng> iterable) {
        for (LatLng add : iterable) {
            this.b.add(add);
        }
        return this;
    }

    public NavigateArrowOptions width(float f) {
        this.c = f;
        return this;
    }

    public NavigateArrowOptions topColor(int i) {
        this.d = i;
        return this;
    }

    @Deprecated
    public NavigateArrowOptions sideColor(int i) {
        this.e = i;
        return this;
    }

    public NavigateArrowOptions zIndex(float f) {
        this.f = f;
        return this;
    }

    public NavigateArrowOptions visible(boolean z) {
        this.g = z;
        return this;
    }

    public List<LatLng> getPoints() {
        return this.b;
    }

    public float getWidth() {
        return this.c;
    }

    public int getTopColor() {
        return this.d;
    }

    @Deprecated
    public int getSideColor() {
        return this.e;
    }

    public float getZIndex() {
        return this.f;
    }

    public boolean isVisible() {
        return this.g;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        int i2 = 0;
        parcel.writeTypedList(this.b);
        parcel.writeFloat(this.c);
        parcel.writeInt(this.d);
        parcel.writeInt(this.e);
        parcel.writeFloat(this.f);
        if (this.g) {
            i2 = 1;
        }
        parcel.writeByte((byte) i2);
        parcel.writeString(this.a);
    }
}
