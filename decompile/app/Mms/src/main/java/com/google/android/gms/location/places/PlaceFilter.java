package com.google.android.gms.location.places;

import android.os.Parcel;
import android.support.annotation.Nullable;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/* compiled from: Unknown */
public final class PlaceFilter extends zza implements SafeParcelable {
    public static final zzg CREATOR = new zzg();
    private static final PlaceFilter zzaPz = new PlaceFilter();
    final int mVersionCode;
    final boolean zzaPA;
    final List<String> zzaPj;
    final List<Integer> zzaPk;
    final List<UserDataType> zzaPl;
    private final Set<String> zzaPo;
    private final Set<Integer> zzaPp;
    private final Set<UserDataType> zzaPq;

    @Deprecated
    /* compiled from: Unknown */
    public static final class zza {
        private boolean zzaPA;
        private Collection<Integer> zzaPB;
        private Collection<UserDataType> zzaPC;
        private String[] zzaPD;

        private zza() {
            this.zzaPB = null;
            this.zzaPA = false;
            this.zzaPC = null;
            this.zzaPD = null;
        }

        public PlaceFilter zzze() {
            Collection collection = null;
            Collection arrayList = this.zzaPB == null ? null : new ArrayList(this.zzaPB);
            Collection arrayList2 = this.zzaPC == null ? null : new ArrayList(this.zzaPC);
            if (this.zzaPD != null) {
                collection = Arrays.asList(this.zzaPD);
            }
            return new PlaceFilter(arrayList, this.zzaPA, collection, arrayList2);
        }
    }

    public PlaceFilter() {
        this(false, null);
    }

    PlaceFilter(int versionCode, @Nullable List<Integer> placeTypesList, boolean requireOpenNow, @Nullable List<String> placeIdsList, @Nullable List<UserDataType> requestedUserDataTypesList) {
        this.mVersionCode = versionCode;
        this.zzaPk = placeTypesList != null ? Collections.unmodifiableList(placeTypesList) : Collections.emptyList();
        this.zzaPA = requireOpenNow;
        this.zzaPl = requestedUserDataTypesList != null ? Collections.unmodifiableList(requestedUserDataTypesList) : Collections.emptyList();
        this.zzaPj = placeIdsList != null ? Collections.unmodifiableList(placeIdsList) : Collections.emptyList();
        this.zzaPp = zza.zzw(this.zzaPk);
        this.zzaPq = zza.zzw(this.zzaPl);
        this.zzaPo = zza.zzw(this.zzaPj);
    }

    public PlaceFilter(@Nullable Collection<Integer> restrictToPlaceTypes, boolean requireOpenNow, @Nullable Collection<String> restrictToPlaceIds, @Nullable Collection<UserDataType> requestedUserDataTypes) {
        this(0, zza.zzf(restrictToPlaceTypes), requireOpenNow, zza.zzf(restrictToPlaceIds), zza.zzf(requestedUserDataTypes));
    }

    public PlaceFilter(boolean requireOpenNow, @Nullable Collection<String> restrictToPlaceIds) {
        this(null, requireOpenNow, restrictToPlaceIds, null);
    }

    @Deprecated
    public static PlaceFilter zzzd() {
        return new zza().zzze();
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object object) {
        boolean z = true;
        if (this == object) {
            return true;
        }
        if (!(object instanceof PlaceFilter)) {
            return false;
        }
        PlaceFilter placeFilter = (PlaceFilter) object;
        if (this.zzaPp.equals(placeFilter.zzaPp) && this.zzaPA == placeFilter.zzaPA && this.zzaPq.equals(placeFilter.zzaPq)) {
            if (!this.zzaPo.equals(placeFilter.zzaPo)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public Set<String> getPlaceIds() {
        return this.zzaPo;
    }

    public Set<Integer> getPlaceTypes() {
        return this.zzaPp;
    }

    public int hashCode() {
        return zzw.hashCode(this.zzaPp, Boolean.valueOf(this.zzaPA), this.zzaPq, this.zzaPo);
    }

    public boolean isRestrictedToPlacesOpenNow() {
        return this.zzaPA;
    }

    public String toString() {
        com.google.android.gms.common.internal.zzw.zza zzy = zzw.zzy(this);
        if (!this.zzaPp.isEmpty()) {
            zzy.zzg("types", this.zzaPp);
        }
        zzy.zzg("requireOpenNow", Boolean.valueOf(this.zzaPA));
        if (!this.zzaPo.isEmpty()) {
            zzy.zzg("placeIds", this.zzaPo);
        }
        if (!this.zzaPq.isEmpty()) {
            zzy.zzg("requestedUserDataTypes", this.zzaPq);
        }
        return zzy.toString();
    }

    public void writeToParcel(Parcel parcel, int flags) {
        zzg.zza(this, parcel, flags);
    }
}
