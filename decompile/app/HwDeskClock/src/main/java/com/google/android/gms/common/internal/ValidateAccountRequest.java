package com.google.android.gms.common.internal;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class ValidateAccountRequest implements SafeParcelable {
    public static final Creator<ValidateAccountRequest> CREATOR = new zzad();
    final int mVersionCode;
    final IBinder zzacD;
    private final Scope[] zzacE;
    private final int zzaes;
    private final Bundle zzaet;
    private final String zzaeu;

    ValidateAccountRequest(int versionCode, int clientVersion, IBinder accountAccessorBinder, Scope[] scopes, Bundle extraArgs, String callingPackage) {
        this.mVersionCode = versionCode;
        this.zzaes = clientVersion;
        this.zzacD = accountAccessorBinder;
        this.zzacE = scopes;
        this.zzaet = extraArgs;
        this.zzaeu = callingPackage;
    }

    public ValidateAccountRequest(zzp accountAccessor, Scope[] scopes, String callingPackage, Bundle extraArgs) {
        IBinder iBinder = null;
        int i = GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE;
        if (accountAccessor != null) {
            iBinder = accountAccessor.asBinder();
        }
        this(1, i, iBinder, scopes, extraArgs, callingPackage);
    }

    public int describeContents() {
        return 0;
    }

    public String getCallingPackage() {
        return this.zzaeu;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzad.zza(this, dest, flags);
    }

    public int zzoU() {
        return this.zzaes;
    }

    public Scope[] zzoV() {
        return this.zzacE;
    }

    public Bundle zzoW() {
        return this.zzaet;
    }
}
