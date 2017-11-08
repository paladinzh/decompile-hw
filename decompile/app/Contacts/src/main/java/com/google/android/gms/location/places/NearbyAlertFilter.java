package com.google.android.gms.location.places;

import android.os.Parcel;
import android.support.annotation.Nullable;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;
import com.google.android.gms.common.internal.zzw.zza;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/* compiled from: Unknown */
public final class NearbyAlertFilter extends zza implements SafeParcelable {
    public static final zzd CREATOR = new zzd();
    final int mVersionCode;
    final List<String> zzaPj;
    final List<Integer> zzaPk;
    final List<UserDataType> zzaPl;
    final String zzaPm;
    final boolean zzaPn;
    private final Set<String> zzaPo;
    private final Set<Integer> zzaPp;
    private final Set<UserDataType> zzaPq;

    NearbyAlertFilter(int versionCode, @Nullable List<String> placeIdsList, @Nullable List<Integer> placeTypesList, @Nullable List<UserDataType> requestedUserDataTypesList, @Nullable String chainName, boolean isBeaconRequired) {
        this.mVersionCode = versionCode;
        this.zzaPk = placeTypesList != null ? Collections.unmodifiableList(placeTypesList) : Collections.emptyList();
        this.zzaPl = requestedUserDataTypesList != null ? Collections.unmodifiableList(requestedUserDataTypesList) : Collections.emptyList();
        this.zzaPj = placeIdsList != null ? Collections.unmodifiableList(placeIdsList) : Collections.emptyList();
        this.zzaPp = zza.zzw(this.zzaPk);
        this.zzaPq = zza.zzw(this.zzaPl);
        this.zzaPo = zza.zzw(this.zzaPj);
        this.zzaPm = chainName;
        this.zzaPn = isBeaconRequired;
    }

    public static NearbyAlertFilter zzh(Collection<String> collection) {
        if (collection != null && !collection.isEmpty()) {
            return new NearbyAlertFilter(0, zza.zzf(collection), null, null, null, false);
        }
        throw new IllegalArgumentException("NearbyAlertFilters must contain at least oneplace ID to match results with.");
    }

    public static NearbyAlertFilter zzi(Collection<Integer> collection) {
        if (collection != null && !collection.isEmpty()) {
            return new NearbyAlertFilter(0, null, zza.zzf(collection), null, null, false);
        }
        throw new IllegalArgumentException("NearbyAlertFilters must contain at least oneplace type to match results with.");
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object object) {
        boolean z = true;
        if (this == object) {
            return true;
        }
        if (!(object instanceof NearbyAlertFilter)) {
            return false;
        }
        NearbyAlertFilter nearbyAlertFilter = (NearbyAlertFilter) object;
        if (this.zzaPm == null && nearbyAlertFilter.zzaPm != null) {
            return false;
        }
        if (this.zzaPp.equals(nearbyAlertFilter.zzaPp) && this.zzaPq.equals(nearbyAlertFilter.zzaPq) && this.zzaPo.equals(nearbyAlertFilter.zzaPo)) {
            if (this.zzaPm == null || this.zzaPm.equals(nearbyAlertFilter.zzaPm)) {
                if (this.zzaPn != nearbyAlertFilter.zzyX()) {
                }
                return z;
            }
        }
        z = false;
        return z;
    }

    public Set<String> getPlaceIds() {
        return this.zzaPo;
    }

    public int hashCode() {
        return zzw.hashCode(this.zzaPp, this.zzaPq, this.zzaPo, this.zzaPm, Boolean.valueOf(this.zzaPn));
    }

    public String toString() {
        zza zzy = zzw.zzy(this);
        if (!this.zzaPp.isEmpty()) {
            zzy.zzg("types", this.zzaPp);
        }
        if (!this.zzaPo.isEmpty()) {
            zzy.zzg("placeIds", this.zzaPo);
        }
        if (!this.zzaPq.isEmpty()) {
            zzy.zzg("requestedUserDataTypes", this.zzaPq);
        }
        if (this.zzaPm != null) {
            zzy.zzg("chainName", this.zzaPm);
        }
        zzy.zzg("Beacon required: ", Boolean.valueOf(this.zzaPn));
        return zzy.toString();
    }

    public void writeToParcel(Parcel parcel, int flags) {
        zzd.zza(this, parcel, flags);
    }

    public boolean zzyX() {
        return this.zzaPn;
    }
}
