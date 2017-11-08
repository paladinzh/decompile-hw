package com.google.android.gms.maps.model;

import android.os.IBinder;
import android.os.Parcel;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.dynamic.zzd.zza;

/* compiled from: Unknown */
public final class GroundOverlayOptions implements SafeParcelable {
    public static final zzc CREATOR = new zzc();
    public static final float NO_DIMENSION = -1.0f;
    private final int mVersionCode;
    private LatLngBounds zzaRk;
    private float zzaTa;
    private float zzaTh;
    private boolean zzaTi;
    private BitmapDescriptor zzaTk;
    private LatLng zzaTl;
    private float zzaTm;
    private float zzaTn;
    private float zzaTo;
    private float zzaTp;
    private float zzaTq;
    private boolean zzaTr;

    public GroundOverlayOptions() {
        this.zzaTi = true;
        this.zzaTo = 0.0f;
        this.zzaTp = 0.5f;
        this.zzaTq = 0.5f;
        this.zzaTr = false;
        this.mVersionCode = 1;
    }

    GroundOverlayOptions(int versionCode, IBinder wrappedImage, LatLng location, float width, float height, LatLngBounds bounds, float bearing, float zIndex, boolean visible, float transparency, float anchorU, float anchorV, boolean clickable) {
        this.zzaTi = true;
        this.zzaTo = 0.0f;
        this.zzaTp = 0.5f;
        this.zzaTq = 0.5f;
        this.zzaTr = false;
        this.mVersionCode = versionCode;
        this.zzaTk = new BitmapDescriptor(zza.zzbs(wrappedImage));
        this.zzaTl = location;
        this.zzaTm = width;
        this.zzaTn = height;
        this.zzaRk = bounds;
        this.zzaTa = bearing;
        this.zzaTh = zIndex;
        this.zzaTi = visible;
        this.zzaTo = transparency;
        this.zzaTp = anchorU;
        this.zzaTq = anchorV;
        this.zzaTr = clickable;
    }

    private GroundOverlayOptions zza(LatLng latLng, float f, float f2) {
        this.zzaTl = latLng;
        this.zzaTm = f;
        this.zzaTn = f2;
        return this;
    }

    public GroundOverlayOptions anchor(float u, float v) {
        this.zzaTp = u;
        this.zzaTq = v;
        return this;
    }

    public GroundOverlayOptions bearing(float bearing) {
        this.zzaTa = ((bearing % 360.0f) + 360.0f) % 360.0f;
        return this;
    }

    public GroundOverlayOptions clickable(boolean clickable) {
        this.zzaTr = clickable;
        return this;
    }

    public int describeContents() {
        return 0;
    }

    public float getAnchorU() {
        return this.zzaTp;
    }

    public float getAnchorV() {
        return this.zzaTq;
    }

    public float getBearing() {
        return this.zzaTa;
    }

    public LatLngBounds getBounds() {
        return this.zzaRk;
    }

    public float getHeight() {
        return this.zzaTn;
    }

    public BitmapDescriptor getImage() {
        return this.zzaTk;
    }

    public LatLng getLocation() {
        return this.zzaTl;
    }

    public float getTransparency() {
        return this.zzaTo;
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

    public GroundOverlayOptions image(BitmapDescriptor image) {
        this.zzaTk = image;
        return this;
    }

    public boolean isClickable() {
        return this.zzaTr;
    }

    public boolean isVisible() {
        return this.zzaTi;
    }

    public GroundOverlayOptions position(LatLng location, float width) {
        boolean z = true;
        zzx.zza(this.zzaRk == null, (Object) "Position has already been set using positionFromBounds");
        zzx.zzb(location != null, (Object) "Location must be specified");
        if (width < 0.0f) {
            z = false;
        }
        zzx.zzb(z, (Object) "Width must be non-negative");
        return zza(location, width, -1.0f);
    }

    public GroundOverlayOptions position(LatLng location, float width, float height) {
        boolean z = true;
        zzx.zza(this.zzaRk == null, (Object) "Position has already been set using positionFromBounds");
        zzx.zzb(location != null, (Object) "Location must be specified");
        zzx.zzb(width >= 0.0f, (Object) "Width must be non-negative");
        if (height < 0.0f) {
            z = false;
        }
        zzx.zzb(z, (Object) "Height must be non-negative");
        return zza(location, width, height);
    }

    public GroundOverlayOptions positionFromBounds(LatLngBounds bounds) {
        zzx.zza(this.zzaTl == null, "Position has already been set using position: " + this.zzaTl);
        this.zzaRk = bounds;
        return this;
    }

    public GroundOverlayOptions transparency(float transparency) {
        boolean z = transparency >= 0.0f && transparency <= ContentUtil.FONT_SIZE_NORMAL;
        zzx.zzb(z, (Object) "Transparency must be in the range [0..1]");
        this.zzaTo = transparency;
        return this;
    }

    public GroundOverlayOptions visible(boolean visible) {
        this.zzaTi = visible;
        return this;
    }

    public void writeToParcel(Parcel out, int flags) {
        zzc.zza(this, out, flags);
    }

    public GroundOverlayOptions zIndex(float zIndex) {
        this.zzaTh = zIndex;
        return this;
    }

    IBinder zzAj() {
        return this.zzaTk.zzzH().asBinder();
    }
}
