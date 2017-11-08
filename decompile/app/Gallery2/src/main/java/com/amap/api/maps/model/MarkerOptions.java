package com.amap.api.maps.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.ArrayList;

public final class MarkerOptions implements Parcelable {
    public static final MarkerOptionsCreator CREATOR = new MarkerOptionsCreator();
    String a;
    float b = WMElement.CAMERASIZEVALUE1B1;
    boolean c = false;
    boolean d = true;
    int e = 5;
    float f;
    private LatLng g;
    private String h;
    private String i;
    private float j = 0.5f;
    private float k = WMElement.CAMERASIZEVALUE1B1;
    private float l = 0.0f;
    private boolean m = false;
    private boolean n = true;
    private boolean o = false;
    private int p = 0;
    private int q = 0;
    private ArrayList<BitmapDescriptor> r = new ArrayList();
    private int s = 20;
    private boolean t = false;
    private boolean u = false;
    private boolean v;

    public MarkerOptions icons(ArrayList<BitmapDescriptor> arrayList) {
        this.r = arrayList;
        return this;
    }

    public ArrayList<BitmapDescriptor> getIcons() {
        return this.r;
    }

    public MarkerOptions period(int i) {
        if (i > 1) {
            this.s = i;
        } else {
            this.s = 1;
        }
        return this;
    }

    public int getPeriod() {
        return this.s;
    }

    public boolean isPerspective() {
        return this.o;
    }

    public MarkerOptions perspective(boolean z) {
        this.o = z;
        return this;
    }

    public MarkerOptions position(LatLng latLng) {
        this.g = latLng;
        return this;
    }

    public MarkerOptions setFlat(boolean z) {
        this.u = z;
        return this;
    }

    private void a() {
        if (this.r == null) {
            this.r = new ArrayList();
        }
    }

    public MarkerOptions icon(BitmapDescriptor bitmapDescriptor) {
        a();
        this.r.clear();
        this.r.add(bitmapDescriptor);
        return this;
    }

    public MarkerOptions anchor(float f, float f2) {
        this.j = f;
        this.k = f2;
        return this;
    }

    public MarkerOptions setInfoWindowOffset(int i, int i2) {
        this.p = i;
        this.q = i2;
        return this;
    }

    public MarkerOptions title(String str) {
        this.h = str;
        return this;
    }

    public MarkerOptions snippet(String str) {
        this.i = str;
        return this;
    }

    public MarkerOptions draggable(boolean z) {
        this.m = z;
        return this;
    }

    public MarkerOptions visible(boolean z) {
        this.n = z;
        return this;
    }

    public MarkerOptions setGps(boolean z) {
        this.t = z;
        return this;
    }

    public LatLng getPosition() {
        return this.g;
    }

    public String getTitle() {
        return this.h;
    }

    public String getSnippet() {
        return this.i;
    }

    public BitmapDescriptor getIcon() {
        if (this.r == null || this.r.size() == 0) {
            return null;
        }
        return (BitmapDescriptor) this.r.get(0);
    }

    public float getAnchorU() {
        return this.j;
    }

    public int getInfoWindowOffsetX() {
        return this.p;
    }

    public int getInfoWindowOffsetY() {
        return this.q;
    }

    public float getAnchorV() {
        return this.k;
    }

    public boolean isDraggable() {
        return this.m;
    }

    public boolean isVisible() {
        return this.n;
    }

    public boolean isGps() {
        return this.t;
    }

    public boolean isFlat() {
        return this.u;
    }

    public MarkerOptions zIndex(float f) {
        this.l = f;
        return this;
    }

    public float getZIndex() {
        return this.l;
    }

    public MarkerOptions alpha(float f) {
        this.b = f;
        return this;
    }

    public float getAlpha() {
        return this.b;
    }

    public MarkerOptions autoOverturnInfoWindow(boolean z) {
        this.c = z;
        return this;
    }

    public boolean isInfoWindowAutoOverturn() {
        return this.c;
    }

    public MarkerOptions displayLevel(int i) {
        this.e = i;
        return this;
    }

    public int getDisplayLevel() {
        return this.e;
    }

    public MarkerOptions rotateAngle(float f) {
        this.f = f;
        return this;
    }

    public float getRotateAngle() {
        return this.f;
    }

    public MarkerOptions infoWindowEnable(boolean z) {
        this.d = z;
        return this;
    }

    public boolean isInfoWindowEnable() {
        return this.d;
    }

    public MarkerOptions belowMaskLayer(boolean z) {
        this.v = z;
        return this;
    }

    public boolean isBelowMaskLayer() {
        return this.v;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(this.g, i);
        parcel.writeString(this.h);
        parcel.writeString(this.i);
        parcel.writeFloat(this.j);
        parcel.writeFloat(this.k);
        parcel.writeInt(this.p);
        parcel.writeInt(this.q);
        parcel.writeBooleanArray(new boolean[]{this.n, this.m, this.t, this.u, this.c, this.d, this.v});
        parcel.writeString(this.a);
        parcel.writeInt(this.s);
        parcel.writeList(this.r);
        parcel.writeFloat(this.l);
        parcel.writeFloat(this.b);
        parcel.writeInt(this.e);
        parcel.writeFloat(this.f);
        if (this.r != null && this.r.size() != 0) {
            parcel.writeParcelable((Parcelable) this.r.get(0), i);
        }
    }
}
