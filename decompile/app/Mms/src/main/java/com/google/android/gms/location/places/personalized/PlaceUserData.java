package com.google.android.gms.location.places.personalized;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;
import java.util.List;

/* compiled from: Unknown */
public class PlaceUserData implements SafeParcelable {
    public static final zze CREATOR = new zze();
    final int mVersionCode;
    private final String zzVa;
    private final String zzaPH;
    private final List<PlaceAlias> zzaRg;

    PlaceUserData(int versionCode, String accountName, String placeId, List<PlaceAlias> placeAliases) {
        this.mVersionCode = versionCode;
        this.zzVa = accountName;
        this.zzaPH = placeId;
        this.zzaRg = placeAliases;
    }

    public int describeContents() {
        zze zze = CREATOR;
        return 0;
    }

    public boolean equals(Object object) {
        boolean z = true;
        if (this == object) {
            return true;
        }
        if (!(object instanceof PlaceUserData)) {
            return false;
        }
        PlaceUserData placeUserData = (PlaceUserData) object;
        if (this.zzVa.equals(placeUserData.zzVa) && this.zzaPH.equals(placeUserData.zzaPH)) {
            if (!this.zzaRg.equals(placeUserData.zzaRg)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public String getPlaceId() {
        return this.zzaPH;
    }

    public int hashCode() {
        return zzw.hashCode(this.zzVa, this.zzaPH, this.zzaRg);
    }

    public String toString() {
        return zzw.zzy(this).zzg("accountName", this.zzVa).zzg("placeId", this.zzaPH).zzg("placeAliases", this.zzaRg).toString();
    }

    public void writeToParcel(Parcel parcel, int flags) {
        zze zze = CREATOR;
        zze.zza(this, parcel, flags);
    }

    public String zzzD() {
        return this.zzVa;
    }

    public List<PlaceAlias> zzzE() {
        return this.zzaRg;
    }
}
