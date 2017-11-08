package com.google.android.gms.signin.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.internal.ResolveAccountResponse;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;

/* compiled from: Unknown */
public class zzj implements Creator<SignInResponse> {
    static void zza(SignInResponse signInResponse, Parcel parcel, int i) {
        int zzav = zzb.zzav(parcel);
        zzb.zzc(parcel, 1, signInResponse.mVersionCode);
        zzb.zza(parcel, 2, signInResponse.zzqY(), i, false);
        zzb.zza(parcel, 3, signInResponse.zzFP(), i, false);
        zzb.zzI(parcel, zzav);
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return zzgV(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return zzkd(i);
    }

    public SignInResponse zzgV(Parcel parcel) {
        int zzau = zza.zzau(parcel);
        ConnectionResult connectionResult = null;
        int i = 0;
        ResolveAccountResponse resolveAccountResponse = null;
        while (parcel.dataPosition() < zzau) {
            int i2;
            ResolveAccountResponse resolveAccountResponse2;
            ConnectionResult connectionResult2;
            int zzat = zza.zzat(parcel);
            switch (zza.zzca(zzat)) {
                case 1:
                    i = zza.zzg(parcel, zzat);
                    break;
                case 2:
                    i2 = i;
                    ConnectionResult connectionResult3 = (ConnectionResult) zza.zza(parcel, zzat, ConnectionResult.CREATOR);
                    resolveAccountResponse2 = resolveAccountResponse;
                    connectionResult2 = connectionResult3;
                    continue;
                case 3:
                    resolveAccountResponse2 = (ResolveAccountResponse) zza.zza(parcel, zzat, ResolveAccountResponse.CREATOR);
                    connectionResult2 = connectionResult;
                    i2 = i;
                    continue;
                default:
                    zza.zzb(parcel, zzat);
                    break;
            }
            resolveAccountResponse2 = resolveAccountResponse;
            connectionResult2 = connectionResult;
            i2 = i;
            i = i2;
            connectionResult = connectionResult2;
            resolveAccountResponse = resolveAccountResponse2;
        }
        if (parcel.dataPosition() == zzau) {
            return new SignInResponse(i, connectionResult, resolveAccountResponse);
        }
        throw new zza.zza("Overread allowed size end=" + zzau, parcel);
    }

    public SignInResponse[] zzkd(int i) {
        return new SignInResponse[i];
    }
}
