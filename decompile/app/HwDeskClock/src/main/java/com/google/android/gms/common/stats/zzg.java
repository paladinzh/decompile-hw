package com.google.android.gms.common.stats;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.android.deskclock.alarmclock.MetaballPath;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;
import java.util.List;

/* compiled from: Unknown */
public class zzg implements Creator<WakeLockEvent> {
    static void zza(WakeLockEvent wakeLockEvent, Parcel parcel, int i) {
        int zzak = zzb.zzak(parcel);
        zzb.zzc(parcel, 1, wakeLockEvent.mVersionCode);
        zzb.zza(parcel, 2, wakeLockEvent.getTimeMillis());
        zzb.zza(parcel, 4, wakeLockEvent.zzpG(), false);
        zzb.zzc(parcel, 5, wakeLockEvent.zzpI());
        zzb.zzb(parcel, 6, wakeLockEvent.zzpJ(), false);
        zzb.zza(parcel, 8, wakeLockEvent.zzpE());
        zzb.zza(parcel, 10, wakeLockEvent.zzpH(), false);
        zzb.zzc(parcel, 11, wakeLockEvent.getEventType());
        zzb.zza(parcel, 12, wakeLockEvent.zzpC(), false);
        zzb.zza(parcel, 13, wakeLockEvent.zzpL(), false);
        zzb.zzc(parcel, 14, wakeLockEvent.zzpK());
        zzb.zza(parcel, 15, wakeLockEvent.zzpM());
        zzb.zzH(parcel, zzak);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzav(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzbU(i);
    }

    public WakeLockEvent zzav(Parcel parcel) {
        int zzaj = zza.zzaj(parcel);
        int i = 0;
        long j = 0;
        int i2 = 0;
        String str = null;
        int i3 = 0;
        List list = null;
        String str2 = null;
        long j2 = 0;
        int i4 = 0;
        String str3 = null;
        String str4 = null;
        float f = 0.0f;
        while (parcel.dataPosition() < zzaj) {
            int zzai = zza.zzai(parcel);
            switch (zza.zzbH(zzai)) {
                case 1:
                    i = zza.zzg(parcel, zzai);
                    break;
                case 2:
                    j = zza.zzi(parcel, zzai);
                    break;
                case MetaballPath.POINT_NUM /*4*/:
                    str = zza.zzo(parcel, zzai);
                    break;
                case 5:
                    i3 = zza.zzg(parcel, zzai);
                    break;
                case 6:
                    list = zza.zzC(parcel, zzai);
                    break;
                case 8:
                    j2 = zza.zzi(parcel, zzai);
                    break;
                case 10:
                    str3 = zza.zzo(parcel, zzai);
                    break;
                case 11:
                    i2 = zza.zzg(parcel, zzai);
                    break;
                case 12:
                    str2 = zza.zzo(parcel, zzai);
                    break;
                case 13:
                    str4 = zza.zzo(parcel, zzai);
                    break;
                case 14:
                    i4 = zza.zzg(parcel, zzai);
                    break;
                case 15:
                    f = zza.zzl(parcel, zzai);
                    break;
                default:
                    zza.zzb(parcel, zzai);
                    break;
            }
        }
        if (parcel.dataPosition() == zzaj) {
            return new WakeLockEvent(i, j, i2, str, i3, list, str2, j2, i4, str3, str4, f);
        }
        throw new zza.zza("Overread allowed size end=" + zzaj, parcel);
    }

    public WakeLockEvent[] zzbU(int i) {
        return new WakeLockEvent[i];
    }
}
