package com.google.android.gms.location.places;

import android.os.Parcel;
import android.support.annotation.Nullable;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* compiled from: Unknown */
public class AutocompleteFilter implements SafeParcelable {
    public static final zzc CREATOR = new zzc();
    public static final int TYPE_FILTER_ADDRESS = 2;
    public static final int TYPE_FILTER_CITIES = 5;
    public static final int TYPE_FILTER_ESTABLISHMENT = 34;
    public static final int TYPE_FILTER_GEOCODE = 1007;
    public static final int TYPE_FILTER_NONE = 0;
    public static final int TYPE_FILTER_REGIONS = 4;
    final int mVersionCode;
    final boolean zzaPg;
    final List<Integer> zzaPh;
    final int zzaPi;

    /* compiled from: Unknown */
    public static final class Builder {
        private boolean zzaPg = false;
        private int zzaPi = 0;

        public AutocompleteFilter build() {
            return new AutocompleteFilter(1, this.zzaPg, AutocompleteFilter.zzhJ(this.zzaPi));
        }

        public Builder setTypeFilter(int typeFilter) {
            this.zzaPi = typeFilter;
            return this;
        }
    }

    AutocompleteFilter(int versionCode, boolean includeQueryPredictions, List<Integer> typeFilterAsList) {
        boolean z = false;
        this.mVersionCode = versionCode;
        this.zzaPh = typeFilterAsList;
        this.zzaPi = zzg(typeFilterAsList);
        if (this.mVersionCode >= 1) {
            this.zzaPg = includeQueryPredictions;
            return;
        }
        if (!includeQueryPredictions) {
            z = true;
        }
        this.zzaPg = z;
    }

    @Deprecated
    public static AutocompleteFilter create(@Nullable Collection<Integer> placeTypes) {
        return new Builder().setTypeFilter(zzg(placeTypes)).build();
    }

    private static int zzg(@Nullable Collection<Integer> collection) {
        return (collection == null || collection.isEmpty()) ? 0 : ((Integer) collection.iterator().next()).intValue();
    }

    private static List<Integer> zzhJ(int i) {
        return Arrays.asList(new Integer[]{Integer.valueOf(i)});
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object object) {
        boolean z = true;
        if (this == object) {
            return true;
        }
        if (!(object instanceof AutocompleteFilter)) {
            return false;
        }
        AutocompleteFilter autocompleteFilter = (AutocompleteFilter) object;
        if (this.zzaPi == this.zzaPi) {
            if (this.zzaPg != autocompleteFilter.zzaPg) {
            }
            return z;
        }
        z = false;
        return z;
    }

    @Deprecated
    public Set<Integer> getPlaceTypes() {
        return new HashSet(zzhJ(this.zzaPi));
    }

    public int getTypeFilter() {
        return this.zzaPi;
    }

    public int hashCode() {
        return zzw.hashCode(Boolean.valueOf(this.zzaPg), Integer.valueOf(this.zzaPi));
    }

    public String toString() {
        return zzw.zzy(this).zzg("includeQueryPredictions", Boolean.valueOf(this.zzaPg)).zzg("typeFilter", Integer.valueOf(this.zzaPi)).toString();
    }

    public void writeToParcel(Parcel parcel, int flags) {
        zzc.zza(this, parcel, flags);
    }
}
