package com.google.android.gms.auth.api.proxy;

import android.app.PendingIntent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class ProxyResponse implements SafeParcelable {
    public static final Creator<ProxyResponse> CREATOR = new zzc();
    public final byte[] body;
    public final int googlePlayServicesStatusCode;
    public final PendingIntent recoveryAction;
    public final int statusCode;
    final int versionCode;
    final Bundle zzRE;

    ProxyResponse(int version, int googlePlayServicesStatusCode, PendingIntent recoveryAction, int statusCode, Bundle headers, byte[] body) {
        this.versionCode = version;
        this.googlePlayServicesStatusCode = googlePlayServicesStatusCode;
        this.statusCode = statusCode;
        this.zzRE = headers;
        this.body = body;
        this.recoveryAction = recoveryAction;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        zzc.zza(this, parcel, flags);
    }
}
