package com.google.android.gms.location.internal;

import android.app.PendingIntent;
import android.os.IBinder;
import android.os.Parcel;
import android.support.annotation.Nullable;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.location.zzc;
import com.google.android.gms.location.zzd;
import com.google.android.gms.location.zzd.zza;

/* compiled from: Unknown */
public class LocationRequestUpdateData implements SafeParcelable {
    public static final zzn CREATOR = new zzn();
    PendingIntent mPendingIntent;
    private final int mVersionCode;
    int zzaOU;
    LocationRequestInternal zzaOV;
    zzd zzaOW;
    zzc zzaOX;
    zzg zzaOY;

    LocationRequestUpdateData(int versionCode, int operation, LocationRequestInternal locationRequest, IBinder locationListenerBinder, PendingIntent pendingIntent, IBinder locationCallbackBinder, IBinder fusedLocationProviderCallbackBinder) {
        zzg zzg = null;
        this.mVersionCode = versionCode;
        this.zzaOU = operation;
        this.zzaOV = locationRequest;
        this.zzaOW = locationListenerBinder != null ? zza.zzcf(locationListenerBinder) : null;
        this.mPendingIntent = pendingIntent;
        this.zzaOX = locationCallbackBinder != null ? zzc.zza.zzce(locationCallbackBinder) : null;
        if (fusedLocationProviderCallbackBinder != null) {
            zzg = zzg.zza.zzch(fusedLocationProviderCallbackBinder);
        }
        this.zzaOY = zzg;
    }

    public static LocationRequestUpdateData zza(LocationRequestInternal locationRequestInternal, PendingIntent pendingIntent, @Nullable zzg zzg) {
        return new LocationRequestUpdateData(1, 1, locationRequestInternal, null, pendingIntent, null, zzg == null ? null : zzg.asBinder());
    }

    public static LocationRequestUpdateData zza(LocationRequestInternal locationRequestInternal, zzc zzc, @Nullable zzg zzg) {
        return new LocationRequestUpdateData(1, 1, locationRequestInternal, null, null, zzc.asBinder(), zzg == null ? null : zzg.asBinder());
    }

    public static LocationRequestUpdateData zza(LocationRequestInternal locationRequestInternal, zzd zzd, @Nullable zzg zzg) {
        return new LocationRequestUpdateData(1, 1, locationRequestInternal, zzd.asBinder(), null, null, zzg == null ? null : zzg.asBinder());
    }

    public static LocationRequestUpdateData zza(zzc zzc, @Nullable zzg zzg) {
        return new LocationRequestUpdateData(1, 2, null, null, null, zzc.asBinder(), zzg == null ? null : zzg.asBinder());
    }

    public static LocationRequestUpdateData zza(zzd zzd, @Nullable zzg zzg) {
        return new LocationRequestUpdateData(1, 2, null, zzd.asBinder(), null, null, zzg == null ? null : zzg.asBinder());
    }

    public static LocationRequestUpdateData zzb(PendingIntent pendingIntent, @Nullable zzg zzg) {
        return new LocationRequestUpdateData(1, 2, null, null, pendingIntent, null, zzg == null ? null : zzg.asBinder());
    }

    public int describeContents() {
        return 0;
    }

    int getVersionCode() {
        return this.mVersionCode;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        zzn.zza(this, parcel, flags);
    }

    IBinder zzyQ() {
        return this.zzaOW != null ? this.zzaOW.asBinder() : null;
    }

    IBinder zzyR() {
        return this.zzaOX != null ? this.zzaOX.asBinder() : null;
    }

    IBinder zzyS() {
        return this.zzaOY != null ? this.zzaOY.asBinder() : null;
    }
}
