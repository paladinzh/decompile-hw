package com.google.android.gms.auth.api.signin;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.zzb;

/* compiled from: Unknown */
public class zza implements Creator<EmailSignInOptions> {
    static void zza(EmailSignInOptions emailSignInOptions, Parcel parcel, int i) {
        int zzav = zzb.zzav(parcel);
        zzb.zzc(parcel, 1, emailSignInOptions.versionCode);
        zzb.zza(parcel, 2, emailSignInOptions.zzmF(), i, false);
        zzb.zza(parcel, 3, emailSignInOptions.zzmH(), false);
        zzb.zza(parcel, 4, emailSignInOptions.zzmG(), i, false);
        zzb.zzI(parcel, zzav);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzQ(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzaL(i);
    }

    public EmailSignInOptions zzQ(Parcel parcel) {
        int zzau = com.google.android.gms.common.internal.safeparcel.zza.zzau(parcel);
        String str = null;
        Uri uri = null;
        int i = 0;
        Uri uri2 = null;
        while (parcel.dataPosition() < zzau) {
            int i2;
            Uri uri3;
            Uri uri4;
            String str2;
            int zzat = com.google.android.gms.common.internal.safeparcel.zza.zzat(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.zza.zzca(zzat)) {
                case 1:
                    i = com.google.android.gms.common.internal.safeparcel.zza.zzg(parcel, zzat);
                    break;
                case 2:
                    i2 = i;
                    String str3 = str;
                    uri3 = (Uri) com.google.android.gms.common.internal.safeparcel.zza.zza(parcel, zzat, Uri.CREATOR);
                    uri4 = uri2;
                    str2 = str3;
                    continue;
                case 3:
                    str = com.google.android.gms.common.internal.safeparcel.zza.zzp(parcel, zzat);
                    break;
                case 4:
                    uri4 = (Uri) com.google.android.gms.common.internal.safeparcel.zza.zza(parcel, zzat, Uri.CREATOR);
                    str2 = str;
                    uri3 = uri;
                    i2 = i;
                    continue;
                default:
                    com.google.android.gms.common.internal.safeparcel.zza.zzb(parcel, zzat);
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
        if (parcel.dataPosition() == zzau) {
            return new EmailSignInOptions(i, uri, str, uri2);
        }
        throw new com.google.android.gms.common.internal.safeparcel.zza.zza("Overread allowed size end=" + zzau, parcel);
    }

    public EmailSignInOptions[] zzaL(int i) {
        return new EmailSignInOptions[i];
    }
}
