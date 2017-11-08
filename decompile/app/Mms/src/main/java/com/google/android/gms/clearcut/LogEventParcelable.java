package com.google.android.gms.clearcut;

import android.os.Parcel;
import com.google.android.gms.clearcut.zzb.zzb;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzv;
import com.google.android.gms.common.internal.zzw;
import com.google.android.gms.internal.zzsz.zzd;
import com.google.android.gms.playlog.internal.PlayLoggerContext;
import java.util.Arrays;

/* compiled from: Unknown */
public class LogEventParcelable implements SafeParcelable {
    public static final zzd CREATOR = new zzd();
    public final int versionCode;
    public PlayLoggerContext zzafh;
    public byte[] zzafi;
    public int[] zzafj;
    public final zzd zzafk;
    public final zzb zzafl;
    public final zzb zzafm;

    LogEventParcelable(int versionCode, PlayLoggerContext playLoggerContext, byte[] logEventBytes, int[] testCodes) {
        this.versionCode = versionCode;
        this.zzafh = playLoggerContext;
        this.zzafi = logEventBytes;
        this.zzafj = testCodes;
        this.zzafk = null;
        this.zzafl = null;
        this.zzafm = null;
    }

    public LogEventParcelable(PlayLoggerContext playLoggerContext, zzd logEvent, zzb extensionProducer, zzb clientVisualElementsProducer, int[] testCodes) {
        this.versionCode = 1;
        this.zzafh = playLoggerContext;
        this.zzafk = logEvent;
        this.zzafl = extensionProducer;
        this.zzafm = clientVisualElementsProducer;
        this.zzafj = testCodes;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (this == other) {
            return true;
        }
        if (!(other instanceof LogEventParcelable)) {
            return false;
        }
        LogEventParcelable logEventParcelable = (LogEventParcelable) other;
        if (this.versionCode == logEventParcelable.versionCode && zzw.equal(this.zzafh, logEventParcelable.zzafh) && Arrays.equals(this.zzafi, logEventParcelable.zzafi) && Arrays.equals(this.zzafj, logEventParcelable.zzafj) && zzw.equal(this.zzafk, logEventParcelable.zzafk) && zzw.equal(this.zzafl, logEventParcelable.zzafl)) {
            if (!zzw.equal(this.zzafm, logEventParcelable.zzafm)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public int hashCode() {
        return zzw.hashCode(Integer.valueOf(this.versionCode), this.zzafh, this.zzafi, this.zzafj, this.zzafk, this.zzafl, this.zzafm);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("LogEventParcelable[");
        stringBuilder.append(this.versionCode);
        stringBuilder.append(", ");
        stringBuilder.append(this.zzafh);
        stringBuilder.append(", ");
        stringBuilder.append(this.zzafi != null ? new String(this.zzafi) : null);
        stringBuilder.append(", ");
        stringBuilder.append(this.zzafj != null ? zzv.zzcL(", ").zza(Arrays.asList(new int[][]{this.zzafj})) : (String) null);
        stringBuilder.append(", ");
        stringBuilder.append(this.zzafk);
        stringBuilder.append(", ");
        stringBuilder.append(this.zzafl);
        stringBuilder.append(", ");
        stringBuilder.append(this.zzafm);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        zzd.zza(this, out, flags);
    }
}
