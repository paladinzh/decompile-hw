package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;

/* compiled from: Unknown */
public class zzy implements Creator<ResolveAccountRequest> {
    static void zza(ResolveAccountRequest resolveAccountRequest, Parcel parcel, int i) {
        int zzav = zzb.zzav(parcel);
        zzb.zzc(parcel, 1, resolveAccountRequest.mVersionCode);
        zzb.zza(parcel, 2, resolveAccountRequest.getAccount(), i, false);
        zzb.zzc(parcel, 3, resolveAccountRequest.getSessionId());
        zzb.zza(parcel, 4, resolveAccountRequest.zzqW(), i, false);
        zzb.zzI(parcel, zzav);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzap(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzbW(i);
    }

    public ResolveAccountRequest zzap(Parcel parcel) {
        int zzau = zza.zzau(parcel);
        int i = 0;
        Account account = null;
        int i2 = 0;
        GoogleSignInAccount googleSignInAccount = null;
        while (parcel.dataPosition() < zzau) {
            int i3;
            Account account2;
            GoogleSignInAccount googleSignInAccount2;
            int i4;
            int zzat = zza.zzat(parcel);
            switch (zza.zzca(zzat)) {
                case 1:
                    i2 = zza.zzg(parcel, zzat);
                    break;
                case 2:
                    i3 = i2;
                    int i5 = i;
                    account2 = (Account) zza.zza(parcel, zzat, Account.CREATOR);
                    googleSignInAccount2 = googleSignInAccount;
                    i4 = i5;
                    continue;
                case 3:
                    i = zza.zzg(parcel, zzat);
                    break;
                case 4:
                    googleSignInAccount2 = (GoogleSignInAccount) zza.zza(parcel, zzat, GoogleSignInAccount.CREATOR);
                    i4 = i;
                    account2 = account;
                    i3 = i2;
                    continue;
                default:
                    zza.zzb(parcel, zzat);
                    break;
            }
            googleSignInAccount2 = googleSignInAccount;
            i4 = i;
            account2 = account;
            i3 = i2;
            i2 = i3;
            account = account2;
            i = i4;
            googleSignInAccount = googleSignInAccount2;
        }
        if (parcel.dataPosition() == zzau) {
            return new ResolveAccountRequest(i2, account, i, googleSignInAccount);
        }
        throw new zza.zza("Overread allowed size end=" + zzau, parcel);
    }

    public ResolveAccountRequest[] zzbW(int i) {
        return new ResolveAccountRequest[i];
    }
}
