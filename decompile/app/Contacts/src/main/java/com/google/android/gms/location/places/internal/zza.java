package com.google.android.gms.location.places.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.zzb;
import com.google.android.gms.location.places.internal.AutocompletePredictionEntity.SubstringEntity;
import java.util.List;

/* compiled from: Unknown */
public class zza implements Creator<AutocompletePredictionEntity> {
    static void zza(AutocompletePredictionEntity autocompletePredictionEntity, Parcel parcel, int i) {
        int zzav = zzb.zzav(parcel);
        zzb.zza(parcel, 1, autocompletePredictionEntity.zzaQd, false);
        zzb.zzc(parcel, 1000, autocompletePredictionEntity.mVersionCode);
        zzb.zza(parcel, 2, autocompletePredictionEntity.zzaPH, false);
        zzb.zza(parcel, 3, autocompletePredictionEntity.zzaPd, false);
        zzb.zzc(parcel, 4, autocompletePredictionEntity.zzaQe, false);
        zzb.zzc(parcel, 5, autocompletePredictionEntity.zzaQf);
        zzb.zza(parcel, 6, autocompletePredictionEntity.zzaQg, false);
        zzb.zzc(parcel, 7, autocompletePredictionEntity.zzaQh, false);
        zzb.zza(parcel, 8, autocompletePredictionEntity.zzaQi, false);
        zzb.zzc(parcel, 9, autocompletePredictionEntity.zzaQj, false);
        zzb.zzI(parcel, zzav);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzfk(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzhW(i);
    }

    public AutocompletePredictionEntity zzfk(Parcel parcel) {
        int i = 0;
        List list = null;
        int zzau = com.google.android.gms.common.internal.safeparcel.zza.zzau(parcel);
        String str = null;
        List list2 = null;
        String str2 = null;
        List list3 = null;
        String str3 = null;
        List list4 = null;
        String str4 = null;
        int i2 = 0;
        while (parcel.dataPosition() < zzau) {
            int zzat = com.google.android.gms.common.internal.safeparcel.zza.zzat(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.zza.zzca(zzat)) {
                case 1:
                    str3 = com.google.android.gms.common.internal.safeparcel.zza.zzp(parcel, zzat);
                    break;
                case 2:
                    str4 = com.google.android.gms.common.internal.safeparcel.zza.zzp(parcel, zzat);
                    break;
                case 3:
                    list4 = com.google.android.gms.common.internal.safeparcel.zza.zzC(parcel, zzat);
                    break;
                case 4:
                    list3 = com.google.android.gms.common.internal.safeparcel.zza.zzc(parcel, zzat, SubstringEntity.CREATOR);
                    break;
                case 5:
                    i = com.google.android.gms.common.internal.safeparcel.zza.zzg(parcel, zzat);
                    break;
                case 6:
                    str2 = com.google.android.gms.common.internal.safeparcel.zza.zzp(parcel, zzat);
                    break;
                case 7:
                    list2 = com.google.android.gms.common.internal.safeparcel.zza.zzc(parcel, zzat, SubstringEntity.CREATOR);
                    break;
                case 8:
                    str = com.google.android.gms.common.internal.safeparcel.zza.zzp(parcel, zzat);
                    break;
                case 9:
                    list = com.google.android.gms.common.internal.safeparcel.zza.zzc(parcel, zzat, SubstringEntity.CREATOR);
                    break;
                case 1000:
                    i2 = com.google.android.gms.common.internal.safeparcel.zza.zzg(parcel, zzat);
                    break;
                default:
                    com.google.android.gms.common.internal.safeparcel.zza.zzb(parcel, zzat);
                    break;
            }
        }
        if (parcel.dataPosition() == zzau) {
            return new AutocompletePredictionEntity(i2, str4, list4, i, str3, list3, str2, list2, str, list);
        }
        throw new com.google.android.gms.common.internal.safeparcel.zza.zza("Overread allowed size end=" + zzau, parcel);
    }

    public AutocompletePredictionEntity[] zzhW(int i) {
        return new AutocompletePredictionEntity[i];
    }
}
