package com.google.android.gms.signin.internal;

import android.accounts.Account;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.android.deskclock.alarmclock.MetaballPath;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;

/* compiled from: Unknown */
public class zzg implements Creator<RecordConsentRequest> {
    static void zza(RecordConsentRequest recordConsentRequest, Parcel parcel, int i) {
        int zzak = zzb.zzak(parcel);
        zzb.zzc(parcel, 1, recordConsentRequest.mVersionCode);
        zzb.zza(parcel, 2, recordConsentRequest.getAccount(), i, false);
        zzb.zza(parcel, 3, recordConsentRequest.zzzu(), i, false);
        zzb.zza(parcel, 4, recordConsentRequest.zzlG(), false);
        zzb.zzH(parcel, zzak);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzgl(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzjb(i);
    }

    public RecordConsentRequest zzgl(Parcel parcel) {
        int zzaj = zza.zzaj(parcel);
        Scope[] scopeArr = null;
        Account account = null;
        int i = 0;
        String str = null;
        while (parcel.dataPosition() < zzaj) {
            int i2;
            Account account2;
            String str2;
            Scope[] scopeArr2;
            int zzai = zza.zzai(parcel);
            switch (zza.zzbH(zzai)) {
                case 1:
                    i = zza.zzg(parcel, zzai);
                    break;
                case 2:
                    i2 = i;
                    Scope[] scopeArr3 = scopeArr;
                    account2 = (Account) zza.zza(parcel, zzai, Account.CREATOR);
                    str2 = str;
                    scopeArr2 = scopeArr3;
                    continue;
                case 3:
                    account2 = account;
                    i2 = i;
                    String str3 = str;
                    scopeArr2 = (Scope[]) zza.zzb(parcel, zzai, Scope.CREATOR);
                    str2 = str3;
                    continue;
                case MetaballPath.POINT_NUM /*4*/:
                    str = zza.zzo(parcel, zzai);
                    break;
                default:
                    zza.zzb(parcel, zzai);
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
        if (parcel.dataPosition() == zzaj) {
            return new RecordConsentRequest(i, account, scopeArr, str);
        }
        throw new zza.zza("Overread allowed size end=" + zzaj, parcel);
    }

    public RecordConsentRequest[] zzjb(int i) {
        return new RecordConsentRequest[i];
    }
}
