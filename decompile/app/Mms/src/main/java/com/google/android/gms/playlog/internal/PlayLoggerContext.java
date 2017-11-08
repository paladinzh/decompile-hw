package com.google.android.gms.playlog.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;
import com.google.android.gms.common.internal.zzx;

/* compiled from: Unknown */
public class PlayLoggerContext implements SafeParcelable {
    public static final zze CREATOR = new zze();
    public final String packageName;
    public final int versionCode;
    public final int zzbdL;
    public final int zzbdM;
    public final String zzbdN;
    public final String zzbdO;
    public final boolean zzbdP;
    public final String zzbdQ;
    public final boolean zzbdR;
    public final int zzbdS;

    public PlayLoggerContext(int versionCode, String packageName, int packageVersionCode, int logSource, String uploadAccountName, String loggingId, boolean logAndroidId, String logSourceName, boolean isAnonymous, int qosTier) {
        this.versionCode = versionCode;
        this.packageName = packageName;
        this.zzbdL = packageVersionCode;
        this.zzbdM = logSource;
        this.zzbdN = uploadAccountName;
        this.zzbdO = loggingId;
        this.zzbdP = logAndroidId;
        this.zzbdQ = logSourceName;
        this.zzbdR = isAnonymous;
        this.zzbdS = qosTier;
    }

    public PlayLoggerContext(String packageName, int packageVersionCode, int logSource, String logSourceName, String uploadAccountName, String loggingId, boolean isAnonymous, int qosTier) {
        this.versionCode = 1;
        this.packageName = (String) zzx.zzz(packageName);
        this.zzbdL = packageVersionCode;
        this.zzbdM = logSource;
        this.zzbdQ = logSourceName;
        this.zzbdN = uploadAccountName;
        this.zzbdO = loggingId;
        this.zzbdP = !isAnonymous;
        this.zzbdR = isAnonymous;
        this.zzbdS = qosTier;
    }

    @Deprecated
    public PlayLoggerContext(String packageName, int packageVersionCode, int logSource, String uploadAccountName, String loggingId, boolean logAndroidId) {
        this.versionCode = 1;
        this.packageName = (String) zzx.zzz(packageName);
        this.zzbdL = packageVersionCode;
        this.zzbdM = logSource;
        this.zzbdQ = null;
        this.zzbdN = uploadAccountName;
        this.zzbdO = loggingId;
        this.zzbdP = logAndroidId;
        this.zzbdR = false;
        this.zzbdS = 0;
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
        if (this.versionCode == playLoggerContext.versionCode && this.packageName.equals(playLoggerContext.packageName) && this.zzbdL == playLoggerContext.zzbdL && this.zzbdM == playLoggerContext.zzbdM && zzw.equal(this.zzbdQ, playLoggerContext.zzbdQ) && zzw.equal(this.zzbdN, playLoggerContext.zzbdN) && zzw.equal(this.zzbdO, playLoggerContext.zzbdO) && this.zzbdP == playLoggerContext.zzbdP && this.zzbdR == playLoggerContext.zzbdR) {
            if (this.zzbdS != playLoggerContext.zzbdS) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public int hashCode() {
        return zzw.hashCode(Integer.valueOf(this.versionCode), this.packageName, Integer.valueOf(this.zzbdL), Integer.valueOf(this.zzbdM), this.zzbdQ, this.zzbdN, this.zzbdO, Boolean.valueOf(this.zzbdP), Boolean.valueOf(this.zzbdR), Integer.valueOf(this.zzbdS));
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PlayLoggerContext[");
        stringBuilder.append("versionCode=").append(this.versionCode).append(',');
        stringBuilder.append("package=").append(this.packageName).append(',');
        stringBuilder.append("packageVersionCode=").append(this.zzbdL).append(',');
        stringBuilder.append("logSource=").append(this.zzbdM).append(',');
        stringBuilder.append("logSourceName=").append(this.zzbdQ).append(',');
        stringBuilder.append("uploadAccount=").append(this.zzbdN).append(',');
        stringBuilder.append("loggingId=").append(this.zzbdO).append(',');
        stringBuilder.append("logAndroidId=").append(this.zzbdP).append(',');
        stringBuilder.append("isAnonymous=").append(this.zzbdR).append(',');
        stringBuilder.append("qosTier=").append(this.zzbdS);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        zze.zza(this, out, flags);
    }
}
