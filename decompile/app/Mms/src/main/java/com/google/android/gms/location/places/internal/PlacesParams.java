package com.google.android.gms.location.places.internal;

import android.annotation.SuppressLint;
import android.os.Parcel;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;
import java.util.Locale;

/* compiled from: Unknown */
public class PlacesParams implements SafeParcelable {
    public static final zzs CREATOR = new zzs();
    public static final PlacesParams zzaQW = new PlacesParams("com.google.android.gms", Locale.getDefault(), null);
    public final int versionCode;
    public final String zzaPU;
    public final String zzaQX;
    public final String zzaQY;
    public final String zzaQZ;
    public final int zzaRa;
    public final int zzaRb;

    public PlacesParams(int versionCode, String clientPackageName, String localeString, String accountName, String gCoreClientName, int gCoreClientJarVersion, int requestSource) {
        this.versionCode = versionCode;
        this.zzaQX = clientPackageName;
        this.zzaQY = localeString;
        this.zzaQZ = accountName;
        this.zzaPU = gCoreClientName;
        this.zzaRa = gCoreClientJarVersion;
        this.zzaRb = requestSource;
    }

    public PlacesParams(String clientPackageName, Locale locale, String accountName) {
        this(3, clientPackageName, locale.toString(), accountName, null, GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE, 0);
    }

    public PlacesParams(String clientPackageName, Locale locale, String accountName, String gCoreClientName, int requestSource) {
        this(3, clientPackageName, locale.toString(), accountName, gCoreClientName, GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE, requestSource);
    }

    public int describeContents() {
        zzs zzs = CREATOR;
        return 0;
    }

    public boolean equals(Object object) {
        boolean z = true;
        if (this == object) {
            return true;
        }
        if (object == null || !(object instanceof PlacesParams)) {
            return false;
        }
        PlacesParams placesParams = (PlacesParams) object;
        if (this.zzaRa == placesParams.zzaRa && this.zzaRb == placesParams.zzaRb && this.zzaQY.equals(placesParams.zzaQY) && this.zzaQX.equals(placesParams.zzaQX) && zzw.equal(this.zzaQZ, placesParams.zzaQZ)) {
            if (!zzw.equal(this.zzaPU, placesParams.zzaPU)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public int hashCode() {
        return zzw.hashCode(this.zzaQX, this.zzaQY, this.zzaQZ, this.zzaPU, Integer.valueOf(this.zzaRa), Integer.valueOf(this.zzaRb));
    }

    @SuppressLint({"DefaultLocale"})
    public String toString() {
        return zzw.zzy(this).zzg("clientPackageName", this.zzaQX).zzg("locale", this.zzaQY).zzg("accountName", this.zzaQZ).zzg("gCoreClientName", this.zzaPU).toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        zzs zzs = CREATOR;
        zzs.zza(this, out, flags);
    }
}
