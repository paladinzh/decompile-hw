package com.google.android.gms.location;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/* compiled from: Unknown */
public final class LocationSettingsRequest implements SafeParcelable {
    public static final Creator<LocationSettingsRequest> CREATOR = new zzf();
    private final int mVersionCode;
    private final List<LocationRequest> zzaBm;
    private final boolean zzaOf;
    private final boolean zzaOg;

    /* compiled from: Unknown */
    public static final class Builder {
        private boolean zzaOf = false;
        private boolean zzaOg = false;
        private final ArrayList<LocationRequest> zzaOh = new ArrayList();

        public Builder addAllLocationRequests(Collection<LocationRequest> requests) {
            this.zzaOh.addAll(requests);
            return this;
        }

        public Builder addLocationRequest(LocationRequest request) {
            this.zzaOh.add(request);
            return this;
        }

        public LocationSettingsRequest build() {
            return new LocationSettingsRequest(this.zzaOh, this.zzaOf, this.zzaOg);
        }

        public Builder setAlwaysShow(boolean show) {
            this.zzaOf = show;
            return this;
        }

        public Builder setNeedBle(boolean needBle) {
            this.zzaOg = needBle;
            return this;
        }
    }

    LocationSettingsRequest(int version, List<LocationRequest> locationRequests, boolean alwaysShow, boolean needBle) {
        this.mVersionCode = version;
        this.zzaBm = locationRequests;
        this.zzaOf = alwaysShow;
        this.zzaOg = needBle;
    }

    private LocationSettingsRequest(List<LocationRequest> locationRequests, boolean alwaysShow, boolean needBle) {
        this(3, (List) locationRequests, alwaysShow, needBle);
    }

    public int describeContents() {
        return 0;
    }

    public int getVersionCode() {
        return this.mVersionCode;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzf.zza(this, dest, flags);
    }

    public List<LocationRequest> zzuZ() {
        return Collections.unmodifiableList(this.zzaBm);
    }

    public boolean zzyK() {
        return this.zzaOf;
    }

    public boolean zzyL() {
        return this.zzaOg;
    }
}
