package com.google.android.gms.wearable.internal;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.wearable.internal.zzav.zza;

/* compiled from: Unknown */
public class RemoveListenerRequest implements SafeParcelable {
    public static final Creator<RemoveListenerRequest> CREATOR = new zzbf();
    final int mVersionCode;
    public final zzav zzaZt;

    RemoveListenerRequest(int versionCode, IBinder listener) {
        this.mVersionCode = versionCode;
        if (listener == null) {
            this.zzaZt = null;
        } else {
            this.zzaZt = zza.zzdZ(listener);
        }
    }

    public RemoveListenerRequest(zzav listener) {
        this.mVersionCode = 1;
        this.zzaZt = listener;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzbf.zza(this, dest, flags);
    }

    IBinder zzCv() {
        return this.zzaZt != null ? this.zzaZt.asBinder() : null;
    }
}
