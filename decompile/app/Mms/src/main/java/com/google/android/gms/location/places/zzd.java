package com.google.android.gms.location.places;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;
import java.util.List;

/* compiled from: Unknown */
public class zzd implements Creator<NearbyAlertFilter> {
    static void zza(NearbyAlertFilter nearbyAlertFilter, Parcel parcel, int i) {
        int zzav = zzb.zzav(parcel);
        zzb.zzb(parcel, 1, nearbyAlertFilter.zzaPj, false);
        zzb.zzc(parcel, 1000, nearbyAlertFilter.mVersionCode);
        zzb.zza(parcel, 2, nearbyAlertFilter.zzaPk, false);
        zzb.zzc(parcel, 3, nearbyAlertFilter.zzaPl, false);
        zzb.zza(parcel, 4, nearbyAlertFilter.zzaPm, false);
        zzb.zza(parcel, 5, nearbyAlertFilter.zzaPn);
        zzb.zzI(parcel, zzav);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzfc(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzhM(i);
    }

    public NearbyAlertFilter zzfc(Parcel parcel) {
        boolean z = false;
        String str = null;
        int zzau = zza.zzau(parcel);
        List list = null;
        List list2 = null;
        List list3 = null;
        int i = 0;
        while (parcel.dataPosition() < zzau) {
            int zzat = zza.zzat(parcel);
            switch (zza.zzca(zzat)) {
                case 1:
                    list3 = zza.zzD(parcel, zzat);
                    break;
                case 2:
                    list2 = zza.zzC(parcel, zzat);
                    break;
                case 3:
                    list = zza.zzc(parcel, zzat, UserDataType.CREATOR);
                    break;
                case 4:
                    str = zza.zzp(parcel, zzat);
                    break;
                case 5:
                    z = zza.zzc(parcel, zzat);
                    break;
                case 1000:
                    i = zza.zzg(parcel, zzat);
                    break;
                default:
                    zza.zzb(parcel, zzat);
                    break;
            }
        }
        if (parcel.dataPosition() == zzau) {
            return new NearbyAlertFilter(i, list3, list2, list, str, z);
        }
        throw new zza.zza("Overread allowed size end=" + zzau, parcel);
    }

    public NearbyAlertFilter[] zzhM(int i) {
        return new NearbyAlertFilter[i];
    }
}
