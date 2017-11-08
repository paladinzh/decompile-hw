package com.google.android.gms.wearable.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.android.deskclock.alarmclock.MetaballPath;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;

/* compiled from: Unknown */
public class zze implements Creator<AmsEntityUpdateParcelable> {
    static void zza(AmsEntityUpdateParcelable amsEntityUpdateParcelable, Parcel parcel, int i) {
        int zzak = zzb.zzak(parcel);
        zzb.zzc(parcel, 1, amsEntityUpdateParcelable.mVersionCode);
        zzb.zza(parcel, 2, amsEntityUpdateParcelable.zzCw());
        zzb.zza(parcel, 3, amsEntityUpdateParcelable.zzCx());
        zzb.zza(parcel, 4, amsEntityUpdateParcelable.getValue(), false);
        zzb.zzH(parcel, zzak);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzhq(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzky(i);
    }

    public AmsEntityUpdateParcelable zzhq(Parcel parcel) {
        byte b = (byte) 0;
        int zzaj = zza.zzaj(parcel);
        String str = null;
        byte b2 = (byte) 0;
        int i = 0;
        while (parcel.dataPosition() < zzaj) {
            int zzai = zza.zzai(parcel);
            switch (zza.zzbH(zzai)) {
                case 1:
                    i = zza.zzg(parcel, zzai);
                    break;
                case 2:
                    b2 = zza.zze(parcel, zzai);
                    break;
                case 3:
                    b = zza.zze(parcel, zzai);
                    break;
                case MetaballPath.POINT_NUM /*4*/:
                    str = zza.zzo(parcel, zzai);
                    break;
                default:
                    zza.zzb(parcel, zzai);
                    break;
            }
        }
        if (parcel.dataPosition() == zzaj) {
            return new AmsEntityUpdateParcelable(i, b2, b, str);
        }
        throw new zza.zza("Overread allowed size end=" + zzaj, parcel);
    }

    public AmsEntityUpdateParcelable[] zzky(int i) {
        return new AmsEntityUpdateParcelable[i];
    }
}
