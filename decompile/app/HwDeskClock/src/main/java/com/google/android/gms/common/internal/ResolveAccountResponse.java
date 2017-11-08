package com.google.android.gms.common.internal;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzp.zza;

/* compiled from: Unknown */
public class ResolveAccountResponse implements SafeParcelable {
    public static final Creator<ResolveAccountResponse> CREATOR = new zzz();
    final int mVersionCode;
    private boolean zzZF;
    private ConnectionResult zzaaW;
    IBinder zzacD;
    private boolean zzaep;

    public ResolveAccountResponse(int connectionResultStatusCode) {
        this(new ConnectionResult(connectionResultStatusCode, null));
    }

    ResolveAccountResponse(int versionCode, IBinder accountAccessorBinder, ConnectionResult connectionResult, boolean saveDefaultAccount, boolean isFromCrossClientAuth) {
        this.mVersionCode = versionCode;
        this.zzacD = accountAccessorBinder;
        this.zzaaW = connectionResult;
        this.zzZF = saveDefaultAccount;
        this.zzaep = isFromCrossClientAuth;
    }

    public ResolveAccountResponse(ConnectionResult result) {
        this(1, null, result, false, false);
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResolveAccountResponse)) {
            return false;
        }
        ResolveAccountResponse resolveAccountResponse = (ResolveAccountResponse) o;
        if (this.zzaaW.equals(resolveAccountResponse.zzaaW)) {
            if (!zzoQ().equals(resolveAccountResponse.zzoQ())) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzz.zza(this, dest, flags);
    }

    public zzp zzoQ() {
        return zza.zzaH(this.zzacD);
    }

    public ConnectionResult zzoR() {
        return this.zzaaW;
    }

    public boolean zzoS() {
        return this.zzZF;
    }

    public boolean zzoT() {
        return this.zzaep;
    }
}
