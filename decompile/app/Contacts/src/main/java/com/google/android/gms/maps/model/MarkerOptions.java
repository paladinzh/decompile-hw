package com.google.android.gms.maps.model;

import android.os.IBinder;
import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.dynamic.zzd.zza;

/* compiled from: Unknown */
public final class MarkerOptions implements SafeParcelable {
    public static final zzf CREATOR = new zzf();
    private float mAlpha;
    private final int mVersionCode;
    private LatLng zzaSF;
    private BitmapDescriptor zzaTA;
    private boolean zzaTB;
    private boolean zzaTC;
    private float zzaTD;
    private float zzaTE;
    private float zzaTF;
    private boolean zzaTi;
    private float zzaTp;
    private float zzaTq;
    private String zzaTz;
    private String zzapg;

    public MarkerOptions() {
        this.zzaTp = 0.5f;
        this.zzaTq = 1.0f;
        this.zzaTi = true;
        this.zzaTC = false;
        this.zzaTD = 0.0f;
        this.zzaTE = 0.5f;
        this.zzaTF = 0.0f;
        this.mAlpha = 1.0f;
        this.mVersionCode = 1;
    }

    MarkerOptions(int versionCode, LatLng position, String title, String snippet, IBinder wrappedIcon, float anchorU, float anchorV, boolean draggable, boolean visible, boolean flat, float rotation, float infoWindowAnchorU, float infoWindowAnchorV, float alpha) {
        this.zzaTp = 0.5f;
        this.zzaTq = 1.0f;
        this.zzaTi = true;
        this.zzaTC = false;
        this.zzaTD = 0.0f;
        this.zzaTE = 0.5f;
        this.zzaTF = 0.0f;
        this.mAlpha = 1.0f;
        this.mVersionCode = versionCode;
        this.zzaSF = position;
        this.zzapg = title;
        this.zzaTz = snippet;
        this.zzaTA = wrappedIcon != null ? new BitmapDescriptor(zza.zzbs(wrappedIcon)) : null;
        this.zzaTp = anchorU;
        this.zzaTq = anchorV;
        this.zzaTB = draggable;
        this.zzaTi = visible;
        this.zzaTC = flat;
        this.zzaTD = rotation;
        this.zzaTE = infoWindowAnchorU;
        this.zzaTF = infoWindowAnchorV;
        this.mAlpha = alpha;
    }

    public MarkerOptions alpha(float alpha) {
        this.mAlpha = alpha;
        return this;
    }

    public MarkerOptions anchor(float u, float v) {
        this.zzaTp = u;
        this.zzaTq = v;
        return this;
    }

    public int describeContents() {
        return 0;
    }

    public MarkerOptions draggable(boolean draggable) {
        this.zzaTB = draggable;
        return this;
    }

    public MarkerOptions flat(boolean flat) {
        this.zzaTC = flat;
        return this;
    }

    public float getAlpha() {
        return this.mAlpha;
    }

    public float getAnchorU() {
        return this.zzaTp;
    }

    public float getAnchorV() {
        return this.zzaTq;
    }

    public BitmapDescriptor getIcon() {
        return this.zzaTA;
    }

    public float getInfoWindowAnchorU() {
        return this.zzaTE;
    }

    public float getInfoWindowAnchorV() {
        return this.zzaTF;
    }

    public LatLng getPosition() {
        return this.zzaSF;
    }

    public float getRotation() {
        return this.zzaTD;
    }

    public String getSnippet() {
        return this.zzaTz;
    }

    public String getTitle() {
        return this.zzapg;
    }

    int getVersionCode() {
        return this.mVersionCode;
    }

    public MarkerOptions icon(BitmapDescriptor icon) {
        this.zzaTA = icon;
        return this;
    }

    public MarkerOptions infoWindowAnchor(float u, float v) {
        this.zzaTE = u;
        this.zzaTF = v;
        return this;
    }

    public boolean isDraggable() {
        return this.zzaTB;
    }

    public boolean isFlat() {
        return this.zzaTC;
    }

    public boolean isVisible() {
        return this.zzaTi;
    }

    public MarkerOptions position(LatLng position) {
        this.zzaSF = position;
        return this;
    }

    public MarkerOptions rotation(float rotation) {
        this.zzaTD = rotation;
        return this;
    }

    public MarkerOptions snippet(String snippet) {
        this.zzaTz = snippet;
        return this;
    }

    public MarkerOptions title(String title) {
        this.zzapg = title;
        return this;
    }

    public MarkerOptions visible(boolean visible) {
        this.zzaTi = visible;
        return this;
    }

    public void writeToParcel(Parcel out, int flags) {
        zzf.zza(this, out, flags);
    }

    IBinder zzAk() {
        return this.zzaTA != null ? this.zzaTA.zzzH().asBinder() : null;
    }
}
