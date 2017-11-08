package com.google.android.gms.wearable.internal;

import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.wearable.internal.zzav.zza;

/* compiled from: Unknown */
public class AddListenerRequest implements SafeParcelable {
    public static final Creator<AddListenerRequest> CREATOR = new zzb();
    final int mVersionCode;
    public final zzav zzaZt;
    public final IntentFilter[] zzaZu;
    public final String zzaZv;
    public final String zzaZw;

    AddListenerRequest(int versionCode, IBinder listener, IntentFilter[] filters, String channelToken, String capability) {
        this.mVersionCode = versionCode;
        if (listener == null) {
            this.zzaZt = null;
        } else {
            this.zzaZt = zza.zzdZ(listener);
        }
        this.zzaZu = filters;
        this.zzaZv = channelToken;
        this.zzaZw = capability;
    }

    public AddListenerRequest(zzbo stub) {
        this.mVersionCode = 1;
        this.zzaZt = stub;
        this.zzaZu = stub.zzCL();
        this.zzaZv = stub.zzCM();
        this.zzaZw = stub.zzCN();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzb.zza(this, dest, flags);
    }

    IBinder zzCv() {
        return this.zzaZt != null ? this.zzaZt.asBinder() : null;
    }
}
