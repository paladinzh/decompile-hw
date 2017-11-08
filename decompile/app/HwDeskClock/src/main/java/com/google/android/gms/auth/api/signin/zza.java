package com.google.android.gms.auth.api.signin;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.android.deskclock.alarmclock.MetaballPath;
import com.google.android.gms.common.internal.safeparcel.zzb;

/* compiled from: Unknown */
public class zza implements Creator<EmailSignInConfig> {
    static void zza(EmailSignInConfig emailSignInConfig, Parcel parcel, int i) {
        int zzak = zzb.zzak(parcel);
        zzb.zzc(parcel, 1, emailSignInConfig.versionCode);
        zzb.zza(parcel, 2, emailSignInConfig.zzlA(), i, false);
        zzb.zza(parcel, 3, emailSignInConfig.zzlC(), false);
        zzb.zza(parcel, 4, emailSignInConfig.zzlB(), i, false);
        zzb.zzH(parcel, zzak);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzN(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzaD(i);
    }

    public EmailSignInConfig zzN(Parcel parcel) {
        int zzaj = com.google.android.gms.common.internal.safeparcel.zza.zzaj(parcel);
        String str = null;
        Uri uri = null;
        int i = 0;
        Uri uri2 = null;
        while (parcel.dataPosition() < zzaj) {
            int i2;
            Uri uri3;
            Uri uri4;
            String str2;
            int zzai = com.google.android.gms.common.internal.safeparcel.zza.zzai(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.zza.zzbH(zzai)) {
                case 1:
                    i = com.google.android.gms.common.internal.safeparcel.zza.zzg(parcel, zzai);
                    break;
                case 2:
                    i2 = i;
                    String str3 = str;
                    uri3 = (Uri) com.google.android.gms.common.internal.safeparcel.zza.zza(parcel, zzai, Uri.CREATOR);
                    uri4 = uri2;
                    str2 = str3;
                    continue;
                case 3:
                    str = com.google.android.gms.common.internal.safeparcel.zza.zzo(parcel, zzai);
                    break;
                case MetaballPath.POINT_NUM /*4*/:
                    uri4 = (Uri) com.google.android.gms.common.internal.safeparcel.zza.zza(parcel, zzai, Uri.CREATOR);
                    str2 = str;
                    uri3 = uri;
                    i2 = i;
                    continue;
                default:
                    com.google.android.gms.common.internal.safeparcel.zza.zzb(parcel, zzai);
                    break;
            }
            uri4 = uri2;
            str2 = str;
            uri3 = uri;
            i2 = i;
            i = i2;
            uri = uri3;
            str = str2;
            uri2 = uri4;
        }
        if (parcel.dataPosition() == zzaj) {
            return new EmailSignInConfig(i, uri, str, uri2);
        }
        throw new com.google.android.gms.common.internal.safeparcel.zza.zza("Overread allowed size end=" + zzaj, parcel);
    }

    public EmailSignInConfig[] zzaD(int i) {
        return new EmailSignInConfig[i];
    }
}
