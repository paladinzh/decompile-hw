package com.google.android.gms.location.places.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;

/* compiled from: Unknown */
public class zzm implements Creator<PlaceLikelihoodEntity> {
    static void zza(PlaceLikelihoodEntity placeLikelihoodEntity, Parcel parcel, int i) {
        int zzav = zzb.zzav(parcel);
        zzb.zza(parcel, 1, placeLikelihoodEntity.zzaQM, i, false);
        zzb.zzc(parcel, 1000, placeLikelihoodEntity.mVersionCode);
        zzb.zza(parcel, 2, placeLikelihoodEntity.zzaQN);
        zzb.zzI(parcel, zzav);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzfm(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzhZ(i);
    }

    public PlaceLikelihoodEntity zzfm(Parcel parcel) {
        int zzau = zza.zzau(parcel);
        int i = 0;
        PlaceImpl placeImpl = null;
        float f = 0.0f;
        while (parcel.dataPosition() < zzau) {
            int zzat = zza.zzat(parcel);
            switch (zza.zzca(zzat)) {
                case 1:
                    placeImpl = (PlaceImpl) zza.zza(parcel, zzat, PlaceImpl.CREATOR);
                    break;
                case 2:
                    f = zza.zzl(parcel, zzat);
                    break;
                case 1000:
                    i = zza.zzg(parcel, zzat);
                    break;
                default:
                    zza.zzb(parcel, zzat);
                    break;
            }
            float f2 = f;
            placeImpl = placeImpl;
            f = f2;
        }
        if (parcel.dataPosition() == zzau) {
            return new PlaceLikelihoodEntity(i, placeImpl, f);
        }
        throw new zza.zza("Overread allowed size end=" + zzau, parcel);
    }

    public PlaceLikelihoodEntity[] zzhZ(int i) {
        return new PlaceLikelihoodEntity[i];
    }
}
