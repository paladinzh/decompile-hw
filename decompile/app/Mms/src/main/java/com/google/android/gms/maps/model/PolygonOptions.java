package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* compiled from: Unknown */
public final class PolygonOptions implements SafeParcelable {
    public static final zzh CREATOR = new zzh();
    private final int mVersionCode;
    private final List<LatLng> zzaTJ;
    private final List<List<LatLng>> zzaTK;
    private boolean zzaTL;
    private float zzaTe;
    private int zzaTf;
    private int zzaTg;
    private float zzaTh;
    private boolean zzaTi;
    private boolean zzaTr;

    public PolygonOptions() {
        this.zzaTe = 10.0f;
        this.zzaTf = -16777216;
        this.zzaTg = 0;
        this.zzaTh = 0.0f;
        this.zzaTi = true;
        this.zzaTL = false;
        this.zzaTr = false;
        this.mVersionCode = 1;
        this.zzaTJ = new ArrayList();
        this.zzaTK = new ArrayList();
    }

    PolygonOptions(int versionCode, List<LatLng> points, List holes, float strokeWidth, int strokeColor, int fillColor, float zIndex, boolean visible, boolean geodesic, boolean clickable) {
        this.zzaTe = 10.0f;
        this.zzaTf = -16777216;
        this.zzaTg = 0;
        this.zzaTh = 0.0f;
        this.zzaTi = true;
        this.zzaTL = false;
        this.zzaTr = false;
        this.mVersionCode = versionCode;
        this.zzaTJ = points;
        this.zzaTK = holes;
        this.zzaTe = strokeWidth;
        this.zzaTf = strokeColor;
        this.zzaTg = fillColor;
        this.zzaTh = zIndex;
        this.zzaTi = visible;
        this.zzaTL = geodesic;
        this.zzaTr = clickable;
    }

    public PolygonOptions add(LatLng point) {
        this.zzaTJ.add(point);
        return this;
    }

    public PolygonOptions add(LatLng... points) {
        this.zzaTJ.addAll(Arrays.asList(points));
        return this;
    }

    public PolygonOptions addAll(Iterable<LatLng> points) {
        for (LatLng add : points) {
            this.zzaTJ.add(add);
        }
        return this;
    }

    public PolygonOptions addHole(Iterable<LatLng> points) {
        ArrayList arrayList = new ArrayList();
        for (LatLng add : points) {
            arrayList.add(add);
        }
        this.zzaTK.add(arrayList);
        return this;
    }

    public PolygonOptions clickable(boolean clickable) {
        this.zzaTr = clickable;
        return this;
    }

    public int describeContents() {
        return 0;
    }

    public PolygonOptions fillColor(int color) {
        this.zzaTg = color;
        return this;
    }

    public PolygonOptions geodesic(boolean geodesic) {
        this.zzaTL = geodesic;
        return this;
    }

    public int getFillColor() {
        return this.zzaTg;
    }

    public List<List<LatLng>> getHoles() {
        return this.zzaTK;
    }

    public List<LatLng> getPoints() {
        return this.zzaTJ;
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

    public boolean isClickable() {
        return this.zzaTr;
    }

    public boolean isGeodesic() {
        return this.zzaTL;
    }

    public boolean isVisible() {
        return this.zzaTi;
    }

    public PolygonOptions strokeColor(int color) {
        this.zzaTf = color;
        return this;
    }

    public PolygonOptions strokeWidth(float width) {
        this.zzaTe = width;
        return this;
    }

    public PolygonOptions visible(boolean visible) {
        this.zzaTi = visible;
        return this;
    }

    public void writeToParcel(Parcel out, int flags) {
        zzh.zza(this, out, flags);
    }

    public PolygonOptions zIndex(float zIndex) {
        this.zzaTh = zIndex;
        return this;
    }

    List zzAl() {
        return this.zzaTK;
    }
}
