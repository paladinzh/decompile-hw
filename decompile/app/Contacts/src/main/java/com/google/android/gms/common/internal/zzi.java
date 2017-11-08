package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;

/* compiled from: Unknown */
public class zzi implements Creator<GetServiceRequest> {
    static void zza(GetServiceRequest getServiceRequest, Parcel parcel, int i) {
        int zzav = zzb.zzav(parcel);
        zzb.zzc(parcel, 1, getServiceRequest.version);
        zzb.zzc(parcel, 2, getServiceRequest.zzall);
        zzb.zzc(parcel, 3, getServiceRequest.zzalm);
        zzb.zza(parcel, 4, getServiceRequest.zzaln, false);
        zzb.zza(parcel, 5, getServiceRequest.zzalo, false);
        zzb.zza(parcel, 6, getServiceRequest.zzalp, i, false);
        zzb.zza(parcel, 7, getServiceRequest.zzalq, false);
        zzb.zza(parcel, 8, getServiceRequest.zzalr, i, false);
        zzb.zzI(parcel, zzav);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzao(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzbR(i);
    }

    public GetServiceRequest zzao(Parcel parcel) {
        int i = 0;
        Account account = null;
        int zzau = zza.zzau(parcel);
        Bundle bundle = null;
        Scope[] scopeArr = null;
        IBinder iBinder = null;
        String str = null;
        int i2 = 0;
        int i3 = 0;
        while (parcel.dataPosition() < zzau) {
            int zzat = zza.zzat(parcel);
            switch (zza.zzca(zzat)) {
                case 1:
                    i3 = zza.zzg(parcel, zzat);
                    break;
                case 2:
                    i2 = zza.zzg(parcel, zzat);
                    break;
                case 3:
                    i = zza.zzg(parcel, zzat);
                    break;
                case 4:
                    str = zza.zzp(parcel, zzat);
                    break;
                case 5:
                    iBinder = zza.zzq(parcel, zzat);
                    break;
                case 6:
                    scopeArr = (Scope[]) zza.zzb(parcel, zzat, Scope.CREATOR);
                    break;
                case 7:
                    bundle = zza.zzr(parcel, zzat);
                    break;
                case 8:
                    account = (Account) zza.zza(parcel, zzat, Account.CREATOR);
                    break;
                default:
                    zza.zzb(parcel, zzat);
                    break;
            }
        }
        if (parcel.dataPosition() == zzau) {
            return new GetServiceRequest(i3, i2, i, str, iBinder, scopeArr, bundle, account);
        }
        throw new zza.zza("Overread allowed size end=" + zzau, parcel);
    }

    public GetServiceRequest[] zzbR(int i) {
        return new GetServiceRequest[i];
    }
}
