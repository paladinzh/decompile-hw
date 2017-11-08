package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;

/* compiled from: Unknown */
public class StreetViewPanoramaLink implements SafeParcelable {
    public static final zzk CREATOR = new zzk();
    public final float bearing;
    private final int mVersionCode;
    public final String panoId;

    StreetViewPanoramaLink(int versionCode, String panoId, float bearing) {
        this.mVersionCode = versionCode;
        this.panoId = panoId;
        if (((double) bearing) <= 0.0d) {
            bearing = (bearing % 360.0f) + 360.0f;
        }
        this.bearing = bearing % 360.0f;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof StreetViewPanoramaLink)) {
            return false;
        }
        StreetViewPanoramaLink streetViewPanoramaLink = (StreetViewPanoramaLink) o;
        if (this.panoId.equals(streetViewPanoramaLink.panoId)) {
            if (Float.floatToIntBits(this.bearing) != Float.floatToIntBits(streetViewPanoramaLink.bearing)) {
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
        return zzw.hashCode(this.panoId, Float.valueOf(this.bearing));
    }

    public String toString() {
        return zzw.zzy(this).zzg("panoId", this.panoId).zzg("bearing", Float.valueOf(this.bearing)).toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        zzk.zza(this, out, flags);
    }
}
