package com.google.android.gms.location;

import android.content.Intent;
import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;

/* compiled from: Unknown */
public final class LocationAvailability implements SafeParcelable {
    public static final LocationAvailabilityCreator CREATOR = new LocationAvailabilityCreator();
    private final int mVersionCode;
    int zzaNU;
    int zzaNV;
    long zzaNW;
    int zzaNX;

    LocationAvailability(int versionCode, int locationStatus, int cellStatus, int wifiStatus, long elapsedRealtimeNs) {
        this.mVersionCode = versionCode;
        this.zzaNX = locationStatus;
        this.zzaNU = cellStatus;
        this.zzaNV = wifiStatus;
        this.zzaNW = elapsedRealtimeNs;
    }

    public static LocationAvailability extractLocationAvailability(Intent intent) {
        return hasLocationAvailability(intent) ? (LocationAvailability) intent.getExtras().getParcelable("com.google.android.gms.location.EXTRA_LOCATION_AVAILABILITY") : null;
    }

    public static boolean hasLocationAvailability(Intent intent) {
        return intent != null ? intent.hasExtra("com.google.android.gms.location.EXTRA_LOCATION_AVAILABILITY") : false;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!(other instanceof LocationAvailability)) {
            return false;
        }
        LocationAvailability locationAvailability = (LocationAvailability) other;
        if (this.zzaNX == locationAvailability.zzaNX && this.zzaNU == locationAvailability.zzaNU && this.zzaNV == locationAvailability.zzaNV && this.zzaNW == locationAvailability.zzaNW) {
            z = true;
        }
        return z;
    }

    int getVersionCode() {
        return this.mVersionCode;
    }

    public int hashCode() {
        return zzw.hashCode(Integer.valueOf(this.zzaNX), Integer.valueOf(this.zzaNU), Integer.valueOf(this.zzaNV), Long.valueOf(this.zzaNW));
    }

    public boolean isLocationAvailable() {
        return this.zzaNX < 1000;
    }

    public String toString() {
        return "LocationAvailability[isLocationAvailable: " + isLocationAvailable() + "]";
    }

    public void writeToParcel(Parcel parcel, int flags) {
        LocationAvailabilityCreator.zza(this, parcel, flags);
    }
}
