package com.google.android.gms.location;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.location.internal.ParcelableGeofence;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public class GeofencingRequest implements SafeParcelable {
    public static final Creator<GeofencingRequest> CREATOR = new zza();
    public static final int INITIAL_TRIGGER_DWELL = 4;
    public static final int INITIAL_TRIGGER_ENTER = 1;
    public static final int INITIAL_TRIGGER_EXIT = 2;
    private final int mVersionCode;
    private final List<ParcelableGeofence> zzaNN;
    private final int zzaNO;

    /* compiled from: Unknown */
    public static final class Builder {
        private final List<ParcelableGeofence> zzaNN = new ArrayList();
        private int zzaNO = 5;

        public static int zzhp(int i) {
            return i & 7;
        }

        public Builder addGeofence(Geofence geofence) {
            zzx.zzb((Object) geofence, (Object) "geofence can't be null.");
            zzx.zzb(geofence instanceof ParcelableGeofence, (Object) "Geofence must be created using Geofence.Builder.");
            this.zzaNN.add((ParcelableGeofence) geofence);
            return this;
        }

        public Builder addGeofences(List<Geofence> geofences) {
            if (geofences == null || geofences.isEmpty()) {
                return this;
            }
            for (Geofence geofence : geofences) {
                if (geofence != null) {
                    addGeofence(geofence);
                }
            }
            return this;
        }

        public GeofencingRequest build() {
            boolean z = false;
            if (!this.zzaNN.isEmpty()) {
                z = true;
            }
            zzx.zzb(z, (Object) "No geofence has been added to this request.");
            return new GeofencingRequest(this.zzaNN, this.zzaNO);
        }

        public Builder setInitialTrigger(int initialTrigger) {
            this.zzaNO = zzhp(initialTrigger);
            return this;
        }
    }

    GeofencingRequest(int version, List<ParcelableGeofence> geofences, int initialTrigger) {
        this.mVersionCode = version;
        this.zzaNN = geofences;
        this.zzaNO = initialTrigger;
    }

    private GeofencingRequest(List<ParcelableGeofence> geofences, int initialTrigger) {
        this(1, (List) geofences, initialTrigger);
    }

    public int describeContents() {
        return 0;
    }

    public List<Geofence> getGeofences() {
        List<Geofence> arrayList = new ArrayList();
        arrayList.addAll(this.zzaNN);
        return arrayList;
    }

    public int getInitialTrigger() {
        return this.zzaNO;
    }

    public int getVersionCode() {
        return this.mVersionCode;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zza.zza(this, dest, flags);
    }

    public List<ParcelableGeofence> zzyI() {
        return this.zzaNN;
    }
}
