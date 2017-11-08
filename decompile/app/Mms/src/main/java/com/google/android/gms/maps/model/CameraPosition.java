package com.google.android.gms.maps.model;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.util.AttributeSet;
import com.google.android.gms.R;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;
import com.google.android.gms.common.internal.zzx;

/* compiled from: Unknown */
public final class CameraPosition implements SafeParcelable {
    public static final zza CREATOR = new zza();
    public final float bearing;
    private final int mVersionCode;
    public final LatLng target;
    public final float tilt;
    public final float zoom;

    /* compiled from: Unknown */
    public static final class Builder {
        private LatLng zzaSX;
        private float zzaSY;
        private float zzaSZ;
        private float zzaTa;

        public Builder(CameraPosition previous) {
            this.zzaSX = previous.target;
            this.zzaSY = previous.zoom;
            this.zzaSZ = previous.tilt;
            this.zzaTa = previous.bearing;
        }

        public Builder bearing(float bearing) {
            this.zzaTa = bearing;
            return this;
        }

        public CameraPosition build() {
            return new CameraPosition(this.zzaSX, this.zzaSY, this.zzaSZ, this.zzaTa);
        }

        public Builder target(LatLng location) {
            this.zzaSX = location;
            return this;
        }

        public Builder tilt(float tilt) {
            this.zzaSZ = tilt;
            return this;
        }

        public Builder zoom(float zoom) {
            this.zzaSY = zoom;
            return this;
        }
    }

    CameraPosition(int versionCode, LatLng target, float zoom, float tilt, float bearing) {
        zzx.zzb((Object) target, (Object) "null camera target");
        boolean z = 0.0f <= tilt && tilt <= 90.0f;
        zzx.zzb(z, "Tilt needs to be between 0 and 90 inclusive: %s", Float.valueOf(tilt));
        this.mVersionCode = versionCode;
        this.target = target;
        this.zoom = zoom;
        this.tilt = tilt + 0.0f;
        if (((double) bearing) <= 0.0d) {
            bearing = (bearing % 360.0f) + 360.0f;
        }
        this.bearing = bearing % 360.0f;
    }

    public CameraPosition(LatLng target, float zoom, float tilt, float bearing) {
        this(1, target, zoom, tilt, bearing);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(CameraPosition camera) {
        return new Builder(camera);
    }

    public static CameraPosition createFromAttributes(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return null;
        }
        TypedArray obtainAttributes = context.getResources().obtainAttributes(attrs, R.styleable.MapAttrs);
        LatLng latLng = new LatLng((double) (!obtainAttributes.hasValue(R.styleable.MapAttrs_cameraTargetLat) ? 0.0f : obtainAttributes.getFloat(R.styleable.MapAttrs_cameraTargetLat, 0.0f)), (double) (!obtainAttributes.hasValue(R.styleable.MapAttrs_cameraTargetLng) ? 0.0f : obtainAttributes.getFloat(R.styleable.MapAttrs_cameraTargetLng, 0.0f)));
        Builder builder = builder();
        builder.target(latLng);
        if (obtainAttributes.hasValue(R.styleable.MapAttrs_cameraZoom)) {
            builder.zoom(obtainAttributes.getFloat(R.styleable.MapAttrs_cameraZoom, 0.0f));
        }
        if (obtainAttributes.hasValue(R.styleable.MapAttrs_cameraBearing)) {
            builder.bearing(obtainAttributes.getFloat(R.styleable.MapAttrs_cameraBearing, 0.0f));
        }
        if (obtainAttributes.hasValue(R.styleable.MapAttrs_cameraTilt)) {
            builder.tilt(obtainAttributes.getFloat(R.styleable.MapAttrs_cameraTilt, 0.0f));
        }
        return builder.build();
    }

    public static final CameraPosition fromLatLngZoom(LatLng target, float zoom) {
        return new CameraPosition(target, zoom, 0.0f, 0.0f);
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof CameraPosition)) {
            return false;
        }
        CameraPosition cameraPosition = (CameraPosition) o;
        if (this.target.equals(cameraPosition.target) && Float.floatToIntBits(this.zoom) == Float.floatToIntBits(cameraPosition.zoom) && Float.floatToIntBits(this.tilt) == Float.floatToIntBits(cameraPosition.tilt)) {
            if (Float.floatToIntBits(this.bearing) != Float.floatToIntBits(cameraPosition.bearing)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    int getVersionCode() {
        return this.mVersionCode;
    }

    public int hashCode() {
        return zzw.hashCode(this.target, Float.valueOf(this.zoom), Float.valueOf(this.tilt), Float.valueOf(this.bearing));
    }

    public String toString() {
        return zzw.zzy(this).zzg("target", this.target).zzg("zoom", Float.valueOf(this.zoom)).zzg("tilt", Float.valueOf(this.tilt)).zzg("bearing", Float.valueOf(this.bearing)).toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        zza.zza(this, out, flags);
    }
}
