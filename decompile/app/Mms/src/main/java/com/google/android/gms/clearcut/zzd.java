package com.google.android.gms.clearcut;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;
import com.google.android.gms.playlog.internal.PlayLoggerContext;

/* compiled from: Unknown */
public class zzd implements Creator<LogEventParcelable> {
    static void zza(LogEventParcelable logEventParcelable, Parcel parcel, int i) {
        int zzav = zzb.zzav(parcel);
        zzb.zzc(parcel, 1, logEventParcelable.versionCode);
        zzb.zza(parcel, 2, logEventParcelable.zzafh, i, false);
        zzb.zza(parcel, 3, logEventParcelable.zzafi, false);
        zzb.zza(parcel, 4, logEventParcelable.zzafj, false);
        zzb.zzI(parcel, zzav);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzaf(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzbs(i);
    }

    public LogEventParcelable zzaf(Parcel parcel) {
        PlayLoggerContext playLoggerContext = null;
        int zzau = zza.zzau(parcel);
        byte[] bArr = null;
        int i = 0;
        int[] iArr = null;
        while (parcel.dataPosition() < zzau) {
            int zzat = zza.zzat(parcel);
            switch (zza.zzca(zzat)) {
                case 1:
                    i = zza.zzg(parcel, zzat);
                    break;
                case 2:
                    playLoggerContext = (PlayLoggerContext) zza.zza(parcel, zzat, PlayLoggerContext.CREATOR);
                    break;
                case 3:
                    bArr = zza.zzs(parcel, zzat);
                    break;
                case 4:
                    iArr = zza.zzv(parcel, zzat);
                    break;
                default:
                    zza.zzb(parcel, zzat);
                    break;
            }
            int[] iArr2 = iArr;
            byte[] bArr2 = bArr;
            playLoggerContext = playLoggerContext;
            bArr = bArr2;
            iArr = iArr2;
        }
        if (parcel.dataPosition() == zzau) {
            return new LogEventParcelable(i, playLoggerContext, bArr, iArr);
        }
        throw new zza.zza("Overread allowed size end=" + zzau, parcel);
    }

    public LogEventParcelable[] zzbs(int i) {
        return new LogEventParcelable[i];
    }
}
