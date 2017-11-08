package com.google.android.gms.signin.internal;

import android.accounts.Account;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;

/* compiled from: Unknown */
public class zzf implements Creator<RecordConsentRequest> {
    static void zza(RecordConsentRequest recordConsentRequest, Parcel parcel, int i) {
        int zzav = zzb.zzav(parcel);
        zzb.zzc(parcel, 1, recordConsentRequest.mVersionCode);
        zzb.zza(parcel, 2, recordConsentRequest.getAccount(), i, false);
        zzb.zza(parcel, 3, recordConsentRequest.zzFM(), i, false);
        zzb.zza(parcel, 4, recordConsentRequest.zzmR(), false);
        zzb.zzI(parcel, zzav);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzgT(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzkb(i);
    }

    public RecordConsentRequest zzgT(Parcel parcel) {
        int zzau = zza.zzau(parcel);
        Scope[] scopeArr = null;
        Account account = null;
        int i = 0;
        String str = null;
        while (parcel.dataPosition() < zzau) {
            int i2;
            Account account2;
            String str2;
            Scope[] scopeArr2;
            int zzat = zza.zzat(parcel);
            switch (zza.zzca(zzat)) {
                case 1:
                    i = zza.zzg(parcel, zzat);
                    break;
                case 2:
                    i2 = i;
                    Scope[] scopeArr3 = scopeArr;
                    account2 = (Account) zza.zza(parcel, zzat, Account.CREATOR);
                    str2 = str;
                    scopeArr2 = scopeArr3;
                    continue;
                case 3:
                    account2 = account;
                    i2 = i;
                    String str3 = str;
                    scopeArr2 = (Scope[]) zza.zzb(parcel, zzat, Scope.CREATOR);
                    str2 = str3;
                    continue;
                case 4:
                    str = zza.zzp(parcel, zzat);
                    break;
                default:
                    zza.zzb(parcel, zzat);
                    break;
            }
            str2 = str;
            scopeArr2 = scopeArr;
            account2 = account;
            i2 = i;
            i = i2;
            account = account2;
            scopeArr = scopeArr2;
            str = str2;
        }
        if (parcel.dataPosition() == zzau) {
            return new RecordConsentRequest(i, account, scopeArr, str);
        }
        throw new zza.zza("Overread allowed size end=" + zzau, parcel);
    }

    public RecordConsentRequest[] zzkb(int i) {
        return new RecordConsentRequest[i];
    }
}
