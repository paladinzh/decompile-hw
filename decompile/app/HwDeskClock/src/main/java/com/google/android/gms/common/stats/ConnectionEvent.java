package com.google.android.gms.common.stats;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public final class ConnectionEvent implements SafeParcelable {
    public static final Creator<ConnectionEvent> CREATOR = new zza();
    final int mVersionCode;
    private final long zzafl;
    private int zzafm;
    private final String zzafn;
    private final String zzafo;
    private final String zzafp;
    private final String zzafq;
    private final String zzafr;
    private final String zzafs;
    private final long zzaft;
    private final long zzafu;
    private long zzafv;

    ConnectionEvent(int versionCode, long timeMillis, int eventType, String callingProcess, String callingService, String targetProcess, String targetService, String stackTrace, String connKey, long elapsedRealtime, long heapAlloc) {
        this.mVersionCode = versionCode;
        this.zzafl = timeMillis;
        this.zzafm = eventType;
        this.zzafn = callingProcess;
        this.zzafo = callingService;
        this.zzafp = targetProcess;
        this.zzafq = targetService;
        this.zzafv = -1;
        this.zzafr = stackTrace;
        this.zzafs = connKey;
        this.zzaft = elapsedRealtime;
        this.zzafu = heapAlloc;
    }

    public ConnectionEvent(long timeMillis, int eventType, String callingProcess, String callingService, String targetProcess, String targetService, String stackTrace, String connKey, long elapsedRealtime, long heapAlloc) {
        this(1, timeMillis, eventType, callingProcess, callingService, targetProcess, targetService, stackTrace, connKey, elapsedRealtime, heapAlloc);
    }

    public int describeContents() {
        return 0;
    }

    public int getEventType() {
        return this.zzafm;
    }

    public long getTimeMillis() {
        return this.zzafl;
    }

    public void writeToParcel(Parcel out, int flags) {
        zza.zza(this, out, flags);
    }

    public String zzpA() {
        return this.zzafq;
    }

    public String zzpB() {
        return this.zzafr;
    }

    public String zzpC() {
        return this.zzafs;
    }

    public long zzpD() {
        return this.zzafu;
    }

    public long zzpE() {
        return this.zzaft;
    }

    public String zzpx() {
        return this.zzafn;
    }

    public String zzpy() {
        return this.zzafo;
    }

    public String zzpz() {
        return this.zzafp;
    }
}
