package com.google.android.gms.common.stats;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public final class ConnectionEvent extends zzf implements SafeParcelable {
    public static final Creator<ConnectionEvent> CREATOR = new zza();
    final int mVersionCode;
    private final long zzane;
    private int zzanf;
    private final String zzang;
    private final String zzanh;
    private final String zzani;
    private final String zzanj;
    private final String zzank;
    private final String zzanl;
    private final long zzanm;
    private final long zzann;
    private long zzano;

    ConnectionEvent(int versionCode, long timeMillis, int eventType, String callingProcess, String callingService, String targetProcess, String targetService, String stackTrace, String connKey, long elapsedRealtime, long heapAlloc) {
        this.mVersionCode = versionCode;
        this.zzane = timeMillis;
        this.zzanf = eventType;
        this.zzang = callingProcess;
        this.zzanh = callingService;
        this.zzani = targetProcess;
        this.zzanj = targetService;
        this.zzano = -1;
        this.zzank = stackTrace;
        this.zzanl = connKey;
        this.zzanm = elapsedRealtime;
        this.zzann = heapAlloc;
    }

    public ConnectionEvent(long timeMillis, int eventType, String callingProcess, String callingService, String targetProcess, String targetService, String stackTrace, String connKey, long elapsedRealtime, long heapAlloc) {
        this(1, timeMillis, eventType, callingProcess, callingService, targetProcess, targetService, stackTrace, connKey, elapsedRealtime, heapAlloc);
    }

    public int describeContents() {
        return 0;
    }

    public int getEventType() {
        return this.zzanf;
    }

    public long getTimeMillis() {
        return this.zzane;
    }

    public void writeToParcel(Parcel out, int flags) {
        zza.zza(this, out, flags);
    }

    public String zzrF() {
        return this.zzang;
    }

    public String zzrG() {
        return this.zzanh;
    }

    public String zzrH() {
        return this.zzani;
    }

    public String zzrI() {
        return this.zzanj;
    }

    public String zzrJ() {
        return this.zzank;
    }

    public String zzrK() {
        return this.zzanl;
    }

    public long zzrL() {
        return this.zzano;
    }

    public long zzrM() {
        return this.zzann;
    }

    public long zzrN() {
        return this.zzanm;
    }

    public String zzrO() {
        return "\t" + zzrF() + "/" + zzrG() + "\t" + zzrH() + "/" + zzrI() + "\t" + (this.zzank != null ? this.zzank : "") + "\t" + zzrM();
    }
}
