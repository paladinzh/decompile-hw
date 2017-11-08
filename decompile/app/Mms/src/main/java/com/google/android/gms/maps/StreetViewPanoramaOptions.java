package com.google.android.gms.maps;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.maps.internal.zza;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;

/* compiled from: Unknown */
public final class StreetViewPanoramaOptions implements SafeParcelable {
    public static final zzb CREATOR = new zzb();
    private final int mVersionCode;
    private Boolean zzaRQ;
    private Boolean zzaRW;
    private StreetViewPanoramaCamera zzaSD;
    private String zzaSE;
    private LatLng zzaSF;
    private Integer zzaSG;
    private Boolean zzaSH;
    private Boolean zzaSI;
    private Boolean zzaSJ;

    public StreetViewPanoramaOptions() {
        this.zzaSH = Boolean.valueOf(true);
        this.zzaRW = Boolean.valueOf(true);
        this.zzaSI = Boolean.valueOf(true);
        this.zzaSJ = Boolean.valueOf(true);
        this.mVersionCode = 1;
    }

    StreetViewPanoramaOptions(int versionCode, StreetViewPanoramaCamera camera, String panoId, LatLng position, Integer radius, byte userNavigationEnabled, byte zoomGesturesEnabled, byte panningGesturesEnabled, byte streetNamesEnabled, byte useViewLifecycleInFragment) {
        this.zzaSH = Boolean.valueOf(true);
        this.zzaRW = Boolean.valueOf(true);
        this.zzaSI = Boolean.valueOf(true);
        this.zzaSJ = Boolean.valueOf(true);
        this.mVersionCode = versionCode;
        this.zzaSD = camera;
        this.zzaSF = position;
        this.zzaSG = radius;
        this.zzaSE = panoId;
        this.zzaSH = zza.zza(userNavigationEnabled);
        this.zzaRW = zza.zza(zoomGesturesEnabled);
        this.zzaSI = zza.zza(panningGesturesEnabled);
        this.zzaSJ = zza.zza(streetNamesEnabled);
        this.zzaRQ = zza.zza(useViewLifecycleInFragment);
    }

    public int describeContents() {
        return 0;
    }

    public Boolean getPanningGesturesEnabled() {
        return this.zzaSI;
    }

    public String getPanoramaId() {
        return this.zzaSE;
    }

    public LatLng getPosition() {
        return this.zzaSF;
    }

    public Integer getRadius() {
        return this.zzaSG;
    }

    public Boolean getStreetNamesEnabled() {
        return this.zzaSJ;
    }

    public StreetViewPanoramaCamera getStreetViewPanoramaCamera() {
        return this.zzaSD;
    }

    public Boolean getUseViewLifecycleInFragment() {
        return this.zzaRQ;
    }

    public Boolean getUserNavigationEnabled() {
        return this.zzaSH;
    }

    int getVersionCode() {
        return this.mVersionCode;
    }

    public Boolean getZoomGesturesEnabled() {
        return this.zzaRW;
    }

    public StreetViewPanoramaOptions panningGesturesEnabled(boolean enabled) {
        this.zzaSI = Boolean.valueOf(enabled);
        return this;
    }

    public StreetViewPanoramaOptions panoramaCamera(StreetViewPanoramaCamera camera) {
        this.zzaSD = camera;
        return this;
    }

    public StreetViewPanoramaOptions panoramaId(String panoId) {
        this.zzaSE = panoId;
        return this;
    }

    public StreetViewPanoramaOptions position(LatLng position) {
        this.zzaSF = position;
        return this;
    }

    public StreetViewPanoramaOptions position(LatLng position, Integer radius) {
        this.zzaSF = position;
        this.zzaSG = radius;
        return this;
    }

    public StreetViewPanoramaOptions streetNamesEnabled(boolean enabled) {
        this.zzaSJ = Boolean.valueOf(enabled);
        return this;
    }

    public StreetViewPanoramaOptions useViewLifecycleInFragment(boolean useViewLifecycleInFragment) {
        this.zzaRQ = Boolean.valueOf(useViewLifecycleInFragment);
        return this;
    }

    public StreetViewPanoramaOptions userNavigationEnabled(boolean enabled) {
        this.zzaSH = Boolean.valueOf(enabled);
        return this;
    }

    public void writeToParcel(Parcel out, int flags) {
        zzb.zza(this, out, flags);
    }

    public StreetViewPanoramaOptions zoomGesturesEnabled(boolean enabled) {
        this.zzaRW = Boolean.valueOf(enabled);
        return this;
    }

    byte zzAa() {
        return zza.zze(this.zzaSH);
    }

    byte zzAb() {
        return zza.zze(this.zzaSI);
    }

    byte zzAc() {
        return zza.zze(this.zzaSJ);
    }

    byte zzzL() {
        return zza.zze(this.zzaRQ);
    }

    byte zzzP() {
        return zza.zze(this.zzaRW);
    }
}
