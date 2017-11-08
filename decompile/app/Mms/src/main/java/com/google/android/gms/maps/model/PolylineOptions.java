package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* compiled from: Unknown */
public final class PolylineOptions implements SafeParcelable {
    public static final zzi CREATOR = new zzi();
    private int mColor;
    private final int mVersionCode;
    private final List<LatLng> zzaTJ;
    private boolean zzaTL;
    private float zzaTh;
    private boolean zzaTi;
    private float zzaTm;
    private boolean zzaTr;

    public PolylineOptions() {
        this.zzaTm = 10.0f;
        this.mColor = -16777216;
        this.zzaTh = 0.0f;
        this.zzaTi = true;
        this.zzaTL = false;
        this.zzaTr = false;
        this.mVersionCode = 1;
        this.zzaTJ = new ArrayList();
    }

    PolylineOptions(int versionCode, List points, float width, int color, float zIndex, boolean visible, boolean geodesic, boolean clickable) {
        this.zzaTm = 10.0f;
        this.mColor = -16777216;
        this.zzaTh = 0.0f;
        this.zzaTi = true;
        this.zzaTL = false;
        this.zzaTr = false;
        this.mVersionCode = versionCode;
        this.zzaTJ = points;
        this.zzaTm = width;
        this.mColor = color;
        this.zzaTh = zIndex;
        this.zzaTi = visible;
        this.zzaTL = geodesic;
        this.zzaTr = clickable;
    }

    public PolylineOptions add(LatLng point) {
        this.zzaTJ.add(point);
        return this;
    }

    public PolylineOptions add(LatLng... points) {
        this.zzaTJ.addAll(Arrays.asList(points));
        return this;
    }

    public PolylineOptions addAll(Iterable<LatLng> points) {
        for (LatLng add : points) {
            this.zzaTJ.add(add);
        }
        return this;
    }

    public PolylineOptions clickable(boolean clickable) {
        this.zzaTr = clickable;
        return this;
    }

    public PolylineOptions color(int color) {
        this.mColor = color;
        return this;
    }

    public int describeContents() {
        return 0;
    }

    public PolylineOptions geodesic(boolean geodesic) {
        this.zzaTL = geodesic;
        return this;
    }

    public int getColor() {
        return this.mColor;
    }

    public List<LatLng> getPoints() {
        return this.zzaTJ;
    }

    int getVersionCode() {
        return this.mVersionCode;
    }

    public float getWidth() {
        return this.zzaTm;
    }

    public float getZIndex() {
        return this.zzaTh;
    }

    public boolean isClickable() {
        return this.zzaTr;
    }

    public boolean isGeodesic() {
        return this.zzaTL;
    }

    public boolean isVisible() {
        return this.zzaTi;
    }

    public PolylineOptions visible(boolean visible) {
        this.zzaTi = visible;
        return this;
    }

    public PolylineOptions width(float width) {
        this.zzaTm = width;
        return this;
    }

    public void writeToParcel(Parcel out, int flags) {
        zzi.zza(this, out, flags);
    }

    public PolylineOptions zIndex(float zIndex) {
        this.zzaTh = zIndex;
        return this;
    }
}
