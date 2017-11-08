package com.google.android.gms.common.images;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.android.deskclock.alarmclock.MetaballPath;
import com.google.android.gms.common.internal.safeparcel.zza;

/* compiled from: Unknown */
public class zzb implements Creator<WebImage> {
    static void zza(WebImage webImage, Parcel parcel, int i) {
        int zzak = com.google.android.gms.common.internal.safeparcel.zzb.zzak(parcel);
        com.google.android.gms.common.internal.safeparcel.zzb.zzc(parcel, 1, webImage.getVersionCode());
        com.google.android.gms.common.internal.safeparcel.zzb.zza(parcel, 2, webImage.getUrl(), i, false);
        com.google.android.gms.common.internal.safeparcel.zzb.zzc(parcel, 3, webImage.getWidth());
        com.google.android.gms.common.internal.safeparcel.zzb.zzc(parcel, 4, webImage.getHeight());
        com.google.android.gms.common.internal.safeparcel.zzb.zzH(parcel, zzak);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzab(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzbu(i);
    }

    public WebImage zzab(Parcel parcel) {
        int zzaj = zza.zzaj(parcel);
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        Uri uri = null;
        while (parcel.dataPosition() < zzaj) {
            int zzai = zza.zzai(parcel);
            switch (zza.zzbH(zzai)) {
                case 1:
                    i2 = zza.zzg(parcel, zzai);
                    break;
                case 2:
                    uri = (Uri) zza.zza(parcel, zzai, Uri.CREATOR);
                    break;
                case 3:
                    i = zza.zzg(parcel, zzai);
                    break;
                case MetaballPath.POINT_NUM /*4*/:
                    i3 = zza.zzg(parcel, zzai);
                    break;
                default:
                    zza.zzb(parcel, zzai);
                    break;
            }
            int i4 = i3;
            i3 = i;
            uri = uri;
            i = i3;
            i3 = i4;
        }
        if (parcel.dataPosition() == zzaj) {
            return new WebImage(i2, uri, i, i3);
        }
        throw new zza.zza("Overread allowed size end=" + zzaj, parcel);
    }

    public WebImage[] zzbu(int i) {
        return new WebImage[i];
    }
}
