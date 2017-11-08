package com.google.android.gms.location.places.personalized;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;

/* compiled from: Unknown */
public class zzc implements Creator<PlaceAliasResult> {
    static void zza(PlaceAliasResult placeAliasResult, Parcel parcel, int i) {
        int zzav = zzb.zzav(parcel);
        zzb.zza(parcel, 1, placeAliasResult.getStatus(), i, false);
        zzb.zzc(parcel, 1000, placeAliasResult.mVersionCode);
        zzb.zza(parcel, 2, placeAliasResult.zzzC(), i, false);
        zzb.zzI(parcel, zzav);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzfr(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzie(i);
    }

    public PlaceAliasResult zzfr(Parcel parcel) {
        int zzau = zza.zzau(parcel);
        Status status = null;
        int i = 0;
        PlaceUserData placeUserData = null;
        while (parcel.dataPosition() < zzau) {
            int i2;
            PlaceUserData placeUserData2;
            Status status2;
            int zzat = zza.zzat(parcel);
            switch (zza.zzca(zzat)) {
                case 1:
                    i2 = i;
                    Status status3 = (Status) zza.zza(parcel, zzat, Status.CREATOR);
                    placeUserData2 = placeUserData;
                    status2 = status3;
                    continue;
                case 2:
                    placeUserData2 = (PlaceUserData) zza.zza(parcel, zzat, PlaceUserData.CREATOR);
                    status2 = status;
                    i2 = i;
                    continue;
                case 1000:
                    i = zza.zzg(parcel, zzat);
                    break;
                default:
                    zza.zzb(parcel, zzat);
                    break;
            }
            placeUserData2 = placeUserData;
            status2 = status;
            i2 = i;
            i = i2;
            status = status2;
            placeUserData = placeUserData2;
        }
        if (parcel.dataPosition() == zzau) {
            return new PlaceAliasResult(i, status, placeUserData);
        }
        throw new zza.zza("Overread allowed size end=" + zzau, parcel);
    }

    public PlaceAliasResult[] zzie(int i) {
        return new PlaceAliasResult[i];
    }
}
