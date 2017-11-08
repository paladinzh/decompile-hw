package com.google.android.gms.playlog.internal;

import android.os.Bundle;
import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class LogEvent implements SafeParcelable {
    public static final zzc CREATOR = new zzc();
    public final String tag;
    public final int versionCode;
    public final long zzaKI;
    public final long zzaKJ;
    public final byte[] zzaKK;
    public final Bundle zzaKL;

    LogEvent(int versionCode, long eventTime, long eventUptime, String tag, byte[] sourceExtensionBytes, Bundle keyValuePairs) {
        this.versionCode = versionCode;
        this.zzaKI = eventTime;
        this.zzaKJ = eventUptime;
        this.tag = tag;
        this.zzaKK = sourceExtensionBytes;
        this.zzaKL = keyValuePairs;
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("tag=").append(this.tag).append(",");
        stringBuilder.append("eventTime=").append(this.zzaKI).append(",");
        stringBuilder.append("eventUptime=").append(this.zzaKJ).append(",");
        if (!(this.zzaKL == null || this.zzaKL.isEmpty())) {
            stringBuilder.append("keyValues=");
            for (String str : this.zzaKL.keySet()) {
                stringBuilder.append("(").append(str).append(",");
                stringBuilder.append(this.zzaKL.getString(str)).append(")");
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        zzc.zza(this, out, flags);
    }
}
