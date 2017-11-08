package com.google.android.gms.common.stats;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.util.List;

/* compiled from: Unknown */
public final class WakeLockEvent extends zzf implements SafeParcelable {
    public static final Creator<WakeLockEvent> CREATOR = new zzh();
    private final long mTimeout;
    final int mVersionCode;
    private final String zzanQ;
    private final int zzanR;
    private final List<String> zzanS;
    private final String zzanT;
    private int zzanU;
    private final String zzanV;
    private final String zzanW;
    private final float zzanX;
    private final long zzane;
    private int zzanf;
    private final long zzanm;
    private long zzano;

    WakeLockEvent(int versionCode, long timeMillis, int eventType, String wakelockName, int wakelockType, List<String> callingPackages, String eventKey, long elapsedRealtime, int deviceState, String secondaryWakeLockName, String hostPackageName, float beginPowerPercentage, long timeout) {
        this.mVersionCode = versionCode;
        this.zzane = timeMillis;
        this.zzanf = eventType;
        this.zzanQ = wakelockName;
        this.zzanV = secondaryWakeLockName;
        this.zzanR = wakelockType;
        this.zzano = -1;
        this.zzanS = callingPackages;
        this.zzanT = eventKey;
        this.zzanm = elapsedRealtime;
        this.zzanU = deviceState;
        this.zzanW = hostPackageName;
        this.zzanX = beginPowerPercentage;
        this.mTimeout = timeout;
    }

    public WakeLockEvent(long timeMillis, int eventType, String wakelockName, int wakelockType, List<String> callingPackages, String eventKey, long elapsedRealtime, int deviceState, String secondaryWakeLockName, String hostPackageName, float beginPowerPercentage, long timeout) {
        this(1, timeMillis, eventType, wakelockName, wakelockType, callingPackages, eventKey, elapsedRealtime, deviceState, secondaryWakeLockName, hostPackageName, beginPowerPercentage, timeout);
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
        zzh.zza(this, out, flags);
    }

    public String zzrK() {
        return this.zzanT;
    }

    public long zzrL() {
        return this.zzano;
    }

    public long zzrN() {
        return this.zzanm;
    }

    public String zzrO() {
        return "\t" + zzrR() + "\t" + zzrT() + "\t" + (zzrU() != null ? TextUtils.join(",", zzrU()) : "") + "\t" + zzrV() + "\t" + (zzrS() != null ? zzrS() : "") + "\t" + (zzrW() != null ? zzrW() : "") + "\t" + zzrX();
    }

    public String zzrR() {
        return this.zzanQ;
    }

    public String zzrS() {
        return this.zzanV;
    }

    public int zzrT() {
        return this.zzanR;
    }

    public List<String> zzrU() {
        return this.zzanS;
    }

    public int zzrV() {
        return this.zzanU;
    }

    public String zzrW() {
        return this.zzanW;
    }

    public float zzrX() {
        return this.zzanX;
    }

    public long zzrY() {
        return this.mTimeout;
    }
}
