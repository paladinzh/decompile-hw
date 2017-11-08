package com.google.android.gms.wearable;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.android.deskclock.alarmclock.MetaballPath;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;

/* compiled from: Unknown */
public class zzh implements Creator<PutDataRequest> {
    static void zza(PutDataRequest putDataRequest, Parcel parcel, int i) {
        int zzak = zzb.zzak(parcel);
        zzb.zzc(parcel, 1, putDataRequest.mVersionCode);
        zzb.zza(parcel, 2, putDataRequest.getUri(), i, false);
        zzb.zza(parcel, 4, putDataRequest.zzCt(), false);
        zzb.zza(parcel, 5, putDataRequest.getData(), false);
        zzb.zzH(parcel, zzak);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzhn(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzkv(i);
    }

    public PutDataRequest zzhn(Parcel parcel) {
        Uri uri = null;
        int zzaj = zza.zzaj(parcel);
        Bundle bundle = null;
        int i = 0;
        byte[] bArr = null;
        while (parcel.dataPosition() < zzaj) {
            int zzai = zza.zzai(parcel);
            switch (zza.zzbH(zzai)) {
                case 1:
                    i = zza.zzg(parcel, zzai);
                    break;
                case 2:
                    uri = (Uri) zza.zza(parcel, zzai, Uri.CREATOR);
                    break;
                case MetaballPath.POINT_NUM /*4*/:
                    bundle = zza.zzq(parcel, zzai);
                    break;
                case 5:
                    bArr = zza.zzr(parcel, zzai);
                    break;
                default:
                    zza.zzb(parcel, zzai);
                    break;
            }
            byte[] bArr2 = bArr;
            Bundle bundle2 = bundle;
            uri = uri;
            bundle = bundle2;
            bArr = bArr2;
        }
        if (parcel.dataPosition() == zzaj) {
            return new PutDataRequest(i, uri, bundle, bArr);
        }
        throw new zza.zza("Overread allowed size end=" + zzaj, parcel);
    }

    public PutDataRequest[] zzkv(int i) {
        return new PutDataRequest[i];
    }
}
