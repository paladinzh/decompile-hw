package com.google.android.gms.playlog.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;

/* compiled from: Unknown */
public class PlayLoggerContext implements SafeParcelable {
    public static final zze CREATOR = new zze();
    public final String packageName;
    public final int versionCode;
    public final int zzaKT;
    public final int zzaKU;
    public final String zzaKV;
    public final String zzaKW;
    public final boolean zzaKX;
    public final String zzaKY;
    public final boolean zzaKZ;
    public final int zzaLa;

    public PlayLoggerContext(int versionCode, String packageName, int packageVersionCode, int logSource, String uploadAccountName, String loggingId, boolean logAndroidId, String logSourceName, boolean isAnonymous, int qosTier) {
        this.versionCode = versionCode;
        this.packageName = packageName;
        this.zzaKT = packageVersionCode;
        this.zzaKU = logSource;
        this.zzaKV = uploadAccountName;
        this.zzaKW = loggingId;
        this.zzaKX = logAndroidId;
        this.zzaKY = logSourceName;
        this.zzaKZ = isAnonymous;
        this.zzaLa = qosTier;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object object) {
        boolean z = true;
        if (this == object) {
            return true;
        }
        if (!(object instanceof PlayLoggerContext)) {
            return false;
        }
        PlayLoggerContext playLoggerContext = (PlayLoggerContext) object;
        if (this.versionCode == playLoggerContext.versionCode && this.packageName.equals(playLoggerContext.packageName) && this.zzaKT == playLoggerContext.zzaKT && this.zzaKU == playLoggerContext.zzaKU && zzw.equal(this.zzaKY, playLoggerContext.zzaKY) && zzw.equal(this.zzaKV, playLoggerContext.zzaKV) && zzw.equal(this.zzaKW, playLoggerContext.zzaKW) && this.zzaKX == playLoggerContext.zzaKX && this.zzaKZ == playLoggerContext.zzaKZ) {
            if (this.zzaLa != playLoggerContext.zzaLa) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public int hashCode() {
        return zzw.hashCode(Integer.valueOf(this.versionCode), this.packageName, Integer.valueOf(this.zzaKT), Integer.valueOf(this.zzaKU), this.zzaKY, this.zzaKV, this.zzaKW, Boolean.valueOf(this.zzaKX), Boolean.valueOf(this.zzaKZ), Integer.valueOf(this.zzaLa));
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PlayLoggerContext[");
        stringBuilder.append("versionCode=").append(this.versionCode).append(',');
        stringBuilder.append("package=").append(this.packageName).append(',');
        stringBuilder.append("packageVersionCode=").append(this.zzaKT).append(',');
        stringBuilder.append("logSource=").append(this.zzaKU).append(',');
        stringBuilder.append("logSourceName=").append(this.zzaKY).append(',');
        stringBuilder.append("uploadAccount=").append(this.zzaKV).append(',');
        stringBuilder.append("loggingId=").append(this.zzaKW).append(',');
        stringBuilder.append("logAndroidId=").append(this.zzaKX).append(',');
        stringBuilder.append("isAnonymous=").append(this.zzaKZ).append(',');
        stringBuilder.append("qosTier=").append(this.zzaLa);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        zze.zza(this, out, flags);
    }
}
