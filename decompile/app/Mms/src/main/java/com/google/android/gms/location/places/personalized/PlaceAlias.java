package com.google.android.gms.location.places.personalized;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;

/* compiled from: Unknown */
public class PlaceAlias implements SafeParcelable {
    public static final zzb CREATOR = new zzb();
    public static final PlaceAlias zzaRc = new PlaceAlias(0, "Home");
    public static final PlaceAlias zzaRd = new PlaceAlias(0, "Work");
    final int mVersionCode;
    private final String zzaRe;

    PlaceAlias(int versionCode, String alias) {
        this.mVersionCode = versionCode;
        this.zzaRe = alias;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof PlaceAlias)) {
            return false;
        }
        return zzw.equal(this.zzaRe, ((PlaceAlias) object).zzaRe);
    }

    public int hashCode() {
        return zzw.hashCode(this.zzaRe);
    }

    public String toString() {
        return zzw.zzy(this).zzg("alias", this.zzaRe).toString();
    }

    public void writeToParcel(Parcel parcel, int flags) {
        zzb.zza(this, parcel, flags);
    }

    public String zzzB() {
        return this.zzaRe;
    }
}
