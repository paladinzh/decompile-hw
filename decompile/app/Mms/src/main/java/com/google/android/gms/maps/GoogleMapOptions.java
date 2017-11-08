package com.google.android.gms.maps;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.util.AttributeSet;
import com.google.android.gms.R;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.maps.internal.zza;
import com.google.android.gms.maps.model.CameraPosition;

/* compiled from: Unknown */
public final class GoogleMapOptions implements SafeParcelable {
    public static final zza CREATOR = new zza();
    private final int mVersionCode;
    private Boolean zzaRP;
    private Boolean zzaRQ;
    private int zzaRR;
    private CameraPosition zzaRS;
    private Boolean zzaRT;
    private Boolean zzaRU;
    private Boolean zzaRV;
    private Boolean zzaRW;
    private Boolean zzaRX;
    private Boolean zzaRY;
    private Boolean zzaRZ;
    private Boolean zzaSa;
    private Boolean zzaSb;

    public GoogleMapOptions() {
        this.zzaRR = -1;
        this.mVersionCode = 1;
    }

    GoogleMapOptions(int versionCode, byte zOrderOnTop, byte useViewLifecycleInFragment, int mapType, CameraPosition camera, byte zoomControlsEnabled, byte compassEnabled, byte scrollGesturesEnabled, byte zoomGesturesEnabled, byte tiltGesturesEnabled, byte rotateGesturesEnabled, byte liteMode, byte mapToolbarEnabled, byte ambientEnabled) {
        this.zzaRR = -1;
        this.mVersionCode = versionCode;
        this.zzaRP = zza.zza(zOrderOnTop);
        this.zzaRQ = zza.zza(useViewLifecycleInFragment);
        this.zzaRR = mapType;
        this.zzaRS = camera;
        this.zzaRT = zza.zza(zoomControlsEnabled);
        this.zzaRU = zza.zza(compassEnabled);
        this.zzaRV = zza.zza(scrollGesturesEnabled);
        this.zzaRW = zza.zza(zoomGesturesEnabled);
        this.zzaRX = zza.zza(tiltGesturesEnabled);
        this.zzaRY = zza.zza(rotateGesturesEnabled);
        this.zzaRZ = zza.zza(liteMode);
        this.zzaSa = zza.zza(mapToolbarEnabled);
        this.zzaSb = zza.zza(ambientEnabled);
    }

    public static GoogleMapOptions createFromAttributes(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return null;
        }
        TypedArray obtainAttributes = context.getResources().obtainAttributes(attrs, R.styleable.MapAttrs);
        GoogleMapOptions googleMapOptions = new GoogleMapOptions();
        if (obtainAttributes.hasValue(R.styleable.MapAttrs_mapType)) {
            googleMapOptions.mapType(obtainAttributes.getInt(R.styleable.MapAttrs_mapType, -1));
        }
        if (obtainAttributes.hasValue(R.styleable.MapAttrs_zOrderOnTop)) {
            googleMapOptions.zOrderOnTop(obtainAttributes.getBoolean(R.styleable.MapAttrs_zOrderOnTop, false));
        }
        if (obtainAttributes.hasValue(R.styleable.MapAttrs_useViewLifecycle)) {
            googleMapOptions.useViewLifecycleInFragment(obtainAttributes.getBoolean(R.styleable.MapAttrs_useViewLifecycle, false));
        }
        if (obtainAttributes.hasValue(R.styleable.MapAttrs_uiCompass)) {
            googleMapOptions.compassEnabled(obtainAttributes.getBoolean(R.styleable.MapAttrs_uiCompass, true));
        }
        if (obtainAttributes.hasValue(R.styleable.MapAttrs_uiRotateGestures)) {
            googleMapOptions.rotateGesturesEnabled(obtainAttributes.getBoolean(R.styleable.MapAttrs_uiRotateGestures, true));
        }
        if (obtainAttributes.hasValue(R.styleable.MapAttrs_uiScrollGestures)) {
            googleMapOptions.scrollGesturesEnabled(obtainAttributes.getBoolean(R.styleable.MapAttrs_uiScrollGestures, true));
        }
        if (obtainAttributes.hasValue(R.styleable.MapAttrs_uiTiltGestures)) {
            googleMapOptions.tiltGesturesEnabled(obtainAttributes.getBoolean(R.styleable.MapAttrs_uiTiltGestures, true));
        }
        if (obtainAttributes.hasValue(R.styleable.MapAttrs_uiZoomGestures)) {
            googleMapOptions.zoomGesturesEnabled(obtainAttributes.getBoolean(R.styleable.MapAttrs_uiZoomGestures, true));
        }
        if (obtainAttributes.hasValue(R.styleable.MapAttrs_uiZoomControls)) {
            googleMapOptions.zoomControlsEnabled(obtainAttributes.getBoolean(R.styleable.MapAttrs_uiZoomControls, true));
        }
        if (obtainAttributes.hasValue(R.styleable.MapAttrs_liteMode)) {
            googleMapOptions.liteMode(obtainAttributes.getBoolean(R.styleable.MapAttrs_liteMode, false));
        }
        if (obtainAttributes.hasValue(R.styleable.MapAttrs_uiMapToolbar)) {
            googleMapOptions.mapToolbarEnabled(obtainAttributes.getBoolean(R.styleable.MapAttrs_uiMapToolbar, true));
        }
        if (obtainAttributes.hasValue(R.styleable.MapAttrs_ambientEnabled)) {
            googleMapOptions.ambientEnabled(obtainAttributes.getBoolean(R.styleable.MapAttrs_ambientEnabled, false));
        }
        googleMapOptions.camera(CameraPosition.createFromAttributes(context, attrs));
        obtainAttributes.recycle();
        return googleMapOptions;
    }

    public GoogleMapOptions ambientEnabled(boolean enabled) {
        this.zzaSb = Boolean.valueOf(enabled);
        return this;
    }

    public GoogleMapOptions camera(CameraPosition camera) {
        this.zzaRS = camera;
        return this;
    }

    public GoogleMapOptions compassEnabled(boolean enabled) {
        this.zzaRU = Boolean.valueOf(enabled);
        return this;
    }

    public int describeContents() {
        return 0;
    }

    public Boolean getAmbientEnabled() {
        return this.zzaSb;
    }

    public CameraPosition getCamera() {
        return this.zzaRS;
    }

    public Boolean getCompassEnabled() {
        return this.zzaRU;
    }

    public Boolean getLiteMode() {
        return this.zzaRZ;
    }

    public Boolean getMapToolbarEnabled() {
        return this.zzaSa;
    }

    public int getMapType() {
        return this.zzaRR;
    }

    public Boolean getRotateGesturesEnabled() {
        return this.zzaRY;
    }

    public Boolean getScrollGesturesEnabled() {
        return this.zzaRV;
    }

    public Boolean getTiltGesturesEnabled() {
        return this.zzaRX;
    }

    public Boolean getUseViewLifecycleInFragment() {
        return this.zzaRQ;
    }

    int getVersionCode() {
        return this.mVersionCode;
    }

    public Boolean getZOrderOnTop() {
        return this.zzaRP;
    }

    public Boolean getZoomControlsEnabled() {
        return this.zzaRT;
    }

    public Boolean getZoomGesturesEnabled() {
        return this.zzaRW;
    }

    public GoogleMapOptions liteMode(boolean enabled) {
        this.zzaRZ = Boolean.valueOf(enabled);
        return this;
    }

    public GoogleMapOptions mapToolbarEnabled(boolean enabled) {
        this.zzaSa = Boolean.valueOf(enabled);
        return this;
    }

    public GoogleMapOptions mapType(int mapType) {
        this.zzaRR = mapType;
        return this;
    }

    public GoogleMapOptions rotateGesturesEnabled(boolean enabled) {
        this.zzaRY = Boolean.valueOf(enabled);
        return this;
    }

    public GoogleMapOptions scrollGesturesEnabled(boolean enabled) {
        this.zzaRV = Boolean.valueOf(enabled);
        return this;
    }

    public GoogleMapOptions tiltGesturesEnabled(boolean enabled) {
        this.zzaRX = Boolean.valueOf(enabled);
        return this;
    }

    public GoogleMapOptions useViewLifecycleInFragment(boolean useViewLifecycleInFragment) {
        this.zzaRQ = Boolean.valueOf(useViewLifecycleInFragment);
        return this;
    }

    public void writeToParcel(Parcel out, int flags) {
        zza.zza(this, out, flags);
    }

    public GoogleMapOptions zOrderOnTop(boolean zOrderOnTop) {
        this.zzaRP = Boolean.valueOf(zOrderOnTop);
        return this;
    }

    public GoogleMapOptions zoomControlsEnabled(boolean enabled) {
        this.zzaRT = Boolean.valueOf(enabled);
        return this;
    }

    public GoogleMapOptions zoomGesturesEnabled(boolean enabled) {
        this.zzaRW = Boolean.valueOf(enabled);
        return this;
    }

    byte zzzK() {
        return zza.zze(this.zzaRP);
    }

    byte zzzL() {
        return zza.zze(this.zzaRQ);
    }

    byte zzzM() {
        return zza.zze(this.zzaRT);
    }

    byte zzzN() {
        return zza.zze(this.zzaRU);
    }

    byte zzzO() {
        return zza.zze(this.zzaRV);
    }

    byte zzzP() {
        return zza.zze(this.zzaRW);
    }

    byte zzzQ() {
        return zza.zze(this.zzaRX);
    }

    byte zzzR() {
        return zza.zze(this.zzaRY);
    }

    byte zzzS() {
        return zza.zze(this.zzaRZ);
    }

    byte zzzT() {
        return zza.zze(this.zzaSa);
    }

    byte zzzU() {
        return zza.zze(this.zzaSb);
    }
}
