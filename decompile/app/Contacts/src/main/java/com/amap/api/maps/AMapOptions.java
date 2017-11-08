package com.amap.api.maps;

import android.os.Parcel;
import android.os.Parcelable;
import com.amap.api.maps.model.CameraPosition;

public class AMapOptions implements Parcelable {
    public static final AMapOptionsCreator CREATOR = new AMapOptionsCreator();
    public static final int LOGO_POSITION_BOTTOM_CENTER = 1;
    public static final int LOGO_POSITION_BOTTOM_LEFT = 0;
    public static final int LOGO_POSITION_BOTTOM_RIGHT = 2;
    public static final int ZOOM_POSITION_RIGHT_BUTTOM = 2;
    public static final int ZOOM_POSITION_RIGHT_CENTER = 1;
    private int a = 1;
    private boolean b = true;
    private boolean c = true;
    private boolean d = true;
    private boolean e = true;
    private boolean f = true;
    private boolean g = false;
    private CameraPosition h;
    private boolean i = false;
    private boolean j = false;
    private int k = 0;

    public AMapOptions logoPosition(int i) {
        this.k = i;
        return this;
    }

    public AMapOptions zOrderOnTop(boolean z) {
        this.g = z;
        return this;
    }

    public AMapOptions mapType(int i) {
        this.a = i;
        return this;
    }

    public AMapOptions camera(CameraPosition cameraPosition) {
        this.h = cameraPosition;
        return this;
    }

    public AMapOptions scaleControlsEnabled(boolean z) {
        this.j = z;
        return this;
    }

    public AMapOptions zoomControlsEnabled(boolean z) {
        this.f = z;
        return this;
    }

    public AMapOptions compassEnabled(boolean z) {
        this.i = z;
        return this;
    }

    public AMapOptions scrollGesturesEnabled(boolean z) {
        this.c = z;
        return this;
    }

    public AMapOptions zoomGesturesEnabled(boolean z) {
        this.e = z;
        return this;
    }

    public AMapOptions tiltGesturesEnabled(boolean z) {
        this.d = z;
        return this;
    }

    public AMapOptions rotateGesturesEnabled(boolean z) {
        this.b = z;
        return this;
    }

    public int getLogoPosition() {
        return this.k;
    }

    public Boolean getZOrderOnTop() {
        return Boolean.valueOf(this.g);
    }

    public int getMapType() {
        return this.a;
    }

    public CameraPosition getCamera() {
        return this.h;
    }

    public Boolean getScaleControlsEnabled() {
        return Boolean.valueOf(this.j);
    }

    public Boolean getZoomControlsEnabled() {
        return Boolean.valueOf(this.f);
    }

    public Boolean getCompassEnabled() {
        return Boolean.valueOf(this.i);
    }

    public Boolean getScrollGesturesEnabled() {
        return Boolean.valueOf(this.c);
    }

    public Boolean getZoomGesturesEnabled() {
        return Boolean.valueOf(this.e);
    }

    public Boolean getTiltGesturesEnabled() {
        return Boolean.valueOf(this.d);
    }

    public Boolean getRotateGesturesEnabled() {
        return Boolean.valueOf(this.b);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(this.h, i);
        parcel.writeInt(this.a);
        parcel.writeBooleanArray(new boolean[]{this.b, this.c, this.d, this.e, this.f, this.g, this.i, this.j});
    }
}
