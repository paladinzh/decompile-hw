package com.google.android.gms.maps.model;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;

/* compiled from: Unknown */
public class zzd implements Creator<LatLngBounds> {
    static void zza(LatLngBounds latLngBounds, Parcel parcel, int i) {
        int zzav = zzb.zzav(parcel);
        zzb.zzc(parcel, 1, latLngBounds.getVersionCode());
        zzb.zza(parcel, 2, latLngBounds.southwest, i, false);
        zzb.zza(parcel, 3, latLngBounds.northeast, i, false);
        zzb.zzI(parcel, zzav);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzfy(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzim(i);
    }

    public LatLngBounds zzfy(Parcel parcel) {
        int zzau = zza.zzau(parcel);
        LatLng latLng = null;
        int i = 0;
        LatLng latLng2 = null;
        while (parcel.dataPosition() < zzau) {
            int i2;
            LatLng latLng3;
            int zzat = zza.zzat(parcel);
            switch (zza.zzca(zzat)) {
                case 1:
                    i = zza.zzg(parcel, zzat);
                    break;
                case 2:
                    i2 = i;
                    LatLng latLng4 = (LatLng) zza.zza(parcel, zzat, LatLng.CREATOR);
                    latLng3 = latLng2;
                    latLng2 = latLng4;
                    continue;
                case 3:
                    latLng3 = (LatLng) zza.zza(parcel, zzat, LatLng.CREATOR);
                    latLng2 = latLng;
                    i2 = i;
                    continue;
                default:
                    zza.zzb(parcel, zzat);
                    break;
            }
            latLng3 = latLng2;
            latLng2 = latLng;
            i2 = i;
            i = i2;
            latLng = latLng2;
            latLng2 = latLng3;
        }
        if (parcel.dataPosition() == zzau) {
            return new LatLngBounds(i, latLng, latLng2);
        }
        throw new zza.zza("Overread allowed size end=" + zzau, parcel);
    }

    public LatLngBounds[] zzim(int i) {
        return new LatLngBounds[i];
    }
}
