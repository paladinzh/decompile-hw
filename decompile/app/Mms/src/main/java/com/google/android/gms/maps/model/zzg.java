package com.google.android.gms.maps.model;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;

/* compiled from: Unknown */
public class zzg implements Creator<PointOfInterest> {
    static void zza(PointOfInterest pointOfInterest, Parcel parcel, int i) {
        int zzav = zzb.zzav(parcel);
        zzb.zzc(parcel, 1, pointOfInterest.getVersionCode());
        zzb.zza(parcel, 2, pointOfInterest.zzaTG, i, false);
        zzb.zza(parcel, 3, pointOfInterest.zzaTH, false);
        zzb.zza(parcel, 4, pointOfInterest.name, false);
        zzb.zzI(parcel, zzav);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzfB(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzip(i);
    }

    public PointOfInterest zzfB(Parcel parcel) {
        LatLng latLng = null;
        int zzau = zza.zzau(parcel);
        String str = null;
        int i = 0;
        String str2 = null;
        while (parcel.dataPosition() < zzau) {
            int zzat = zza.zzat(parcel);
            switch (zza.zzca(zzat)) {
                case 1:
                    i = zza.zzg(parcel, zzat);
                    break;
                case 2:
                    latLng = (LatLng) zza.zza(parcel, zzat, LatLng.CREATOR);
                    break;
                case 3:
                    str = zza.zzp(parcel, zzat);
                    break;
                case 4:
                    str2 = zza.zzp(parcel, zzat);
                    break;
                default:
                    zza.zzb(parcel, zzat);
                    break;
            }
            String str3 = str2;
            str2 = str;
            latLng = latLng;
            str = str2;
            str2 = str3;
        }
        if (parcel.dataPosition() == zzau) {
            return new PointOfInterest(i, latLng, str, str2);
        }
        throw new zza.zza("Overread allowed size end=" + zzau, parcel);
    }

    public PointOfInterest[] zzip(int i) {
        return new PointOfInterest[i];
    }
}
