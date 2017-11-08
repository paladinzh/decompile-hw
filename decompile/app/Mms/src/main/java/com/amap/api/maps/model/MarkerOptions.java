package com.amap.api.maps.model;

import android.os.Parcel;
import android.os.Parcelable;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import java.util.ArrayList;

public final class MarkerOptions implements Parcelable {
    public static final MarkerOptionsCreator CREATOR = new MarkerOptionsCreator();
    String a;
    private LatLng b;
    private String c;
    private String d;
    private float e = 0.5f;
    private float f = ContentUtil.FONT_SIZE_NORMAL;
    private float g = 0.0f;
    private boolean h = false;
    private boolean i = true;
    private boolean j = false;
    private int k = 0;
    private int l = 0;
    private ArrayList<BitmapDescriptor> m = new ArrayList();
    private int n = 20;
    private boolean o = false;
    private boolean p = false;

    public MarkerOptions icons(ArrayList<BitmapDescriptor> arrayList) {
        this.m = arrayList;
        return this;
    }

    public ArrayList<BitmapDescriptor> getIcons() {
        return this.m;
    }

    public MarkerOptions period(int i) {
        if (i > 1) {
            this.n = i;
        } else {
            this.n = 1;
        }
        return this;
    }

    public int getPeriod() {
        return this.n;
    }

    public boolean isPerspective() {
        return this.j;
    }

    public MarkerOptions perspective(boolean z) {
        this.j = z;
        return this;
    }

    public MarkerOptions position(LatLng latLng) {
        this.b = latLng;
        return this;
    }

    public MarkerOptions setFlat(boolean z) {
        this.p = z;
        return this;
    }

    private void a() {
        if (this.m == null) {
            this.m = new ArrayList();
        }
    }

    public MarkerOptions icon(BitmapDescriptor bitmapDescriptor) {
        a();
        this.m.clear();
        this.m.add(bitmapDescriptor);
        return this;
    }

    public MarkerOptions anchor(float f, float f2) {
        this.e = f;
        this.f = f2;
        return this;
    }

    public MarkerOptions setInfoWindowOffset(int i, int i2) {
        this.k = i;
        this.l = i2;
        return this;
    }

    public MarkerOptions title(String str) {
        this.c = str;
        return this;
    }

    public MarkerOptions snippet(String str) {
        this.d = str;
        return this;
    }

    public MarkerOptions draggable(boolean z) {
        this.h = z;
        return this;
    }

    public MarkerOptions visible(boolean z) {
        this.i = z;
        return this;
    }

    public MarkerOptions setGps(boolean z) {
        this.o = z;
        return this;
    }

    public LatLng getPosition() {
        return this.b;
    }

    public String getTitle() {
        return this.c;
    }

    public String getSnippet() {
        return this.d;
    }

    public BitmapDescriptor getIcon() {
        if (this.m == null || this.m.size() == 0) {
            return null;
        }
        return (BitmapDescriptor) this.m.get(0);
    }

    public float getAnchorU() {
        return this.e;
    }

    public int getInfoWindowOffsetX() {
        return this.k;
    }

    public int getInfoWindowOffsetY() {
        return this.l;
    }

    public float getAnchorV() {
        return this.f;
    }

    public boolean isDraggable() {
        return this.h;
    }

    public boolean isVisible() {
        return this.i;
    }

    public boolean isGps() {
        return this.o;
    }

    public boolean isFlat() {
        return this.p;
    }

    public MarkerOptions zIndex(float f) {
        this.g = f;
        return this;
    }

    public float getZIndex() {
        return this.g;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(this.b, i);
        if (!(this.m == null || this.m.size() == 0)) {
            parcel.writeParcelable((Parcelable) this.m.get(0), i);
        }
        parcel.writeString(this.c);
        parcel.writeString(this.d);
        parcel.writeFloat(this.e);
        parcel.writeFloat(this.f);
        parcel.writeInt(this.k);
        parcel.writeInt(this.l);
        parcel.writeBooleanArray(new boolean[]{this.i, this.h, this.o, this.p});
        parcel.writeString(this.a);
        parcel.writeInt(this.n);
        parcel.writeList(this.m);
        parcel.writeFloat(this.g);
    }
}
