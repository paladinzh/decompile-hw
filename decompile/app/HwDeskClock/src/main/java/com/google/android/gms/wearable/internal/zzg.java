package com.google.android.gms.wearable.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.android.deskclock.alarmclock.MetaballPath;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;

/* compiled from: Unknown */
public class zzg implements Creator<AncsNotificationParcelable> {
    static void zza(AncsNotificationParcelable ancsNotificationParcelable, Parcel parcel, int i) {
        int zzak = zzb.zzak(parcel);
        zzb.zzc(parcel, 1, ancsNotificationParcelable.mVersionCode);
        zzb.zzc(parcel, 2, ancsNotificationParcelable.getId());
        zzb.zza(parcel, 3, ancsNotificationParcelable.zztY(), false);
        zzb.zza(parcel, 4, ancsNotificationParcelable.zzCy(), false);
        zzb.zza(parcel, 5, ancsNotificationParcelable.zzCz(), false);
        zzb.zza(parcel, 6, ancsNotificationParcelable.getTitle(), false);
        zzb.zza(parcel, 7, ancsNotificationParcelable.zztp(), false);
        zzb.zza(parcel, 8, ancsNotificationParcelable.getDisplayName(), false);
        zzb.zza(parcel, 9, ancsNotificationParcelable.zzCA());
        zzb.zza(parcel, 10, ancsNotificationParcelable.zzCB());
        zzb.zza(parcel, 11, ancsNotificationParcelable.zzCC());
        zzb.zza(parcel, 12, ancsNotificationParcelable.zzCD());
        zzb.zzH(parcel, zzak);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzhr(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzkz(i);
    }

    public AncsNotificationParcelable zzhr(Parcel parcel) {
        int zzaj = zza.zzaj(parcel);
        int i = 0;
        int i2 = 0;
        String str = null;
        String str2 = null;
        String str3 = null;
        String str4 = null;
        String str5 = null;
        String str6 = null;
        byte b = (byte) 0;
        byte b2 = (byte) 0;
        byte b3 = (byte) 0;
        byte b4 = (byte) 0;
        while (parcel.dataPosition() < zzaj) {
            int zzai = zza.zzai(parcel);
            switch (zza.zzbH(zzai)) {
                case 1:
                    i = zza.zzg(parcel, zzai);
                    break;
                case 2:
                    i2 = zza.zzg(parcel, zzai);
                    break;
                case 3:
                    str = zza.zzo(parcel, zzai);
                    break;
                case MetaballPath.POINT_NUM /*4*/:
                    str2 = zza.zzo(parcel, zzai);
                    break;
                case 5:
                    str3 = zza.zzo(parcel, zzai);
                    break;
                case 6:
                    str4 = zza.zzo(parcel, zzai);
                    break;
                case 7:
                    str5 = zza.zzo(parcel, zzai);
                    break;
                case 8:
                    str6 = zza.zzo(parcel, zzai);
                    break;
                case 9:
                    b = zza.zze(parcel, zzai);
                    break;
                case 10:
                    b2 = zza.zze(parcel, zzai);
                    break;
                case 11:
                    b3 = zza.zze(parcel, zzai);
                    break;
                case 12:
                    b4 = zza.zze(parcel, zzai);
                    break;
                default:
                    zza.zzb(parcel, zzai);
                    break;
            }
        }
        if (parcel.dataPosition() == zzaj) {
            return new AncsNotificationParcelable(i, i2, str, str2, str3, str4, str5, str6, b, b2, b3, b4);
        }
        throw new zza.zza("Overread allowed size end=" + zzaj, parcel);
    }

    public AncsNotificationParcelable[] zzkz(int i) {
        return new AncsNotificationParcelable[i];
    }
}
