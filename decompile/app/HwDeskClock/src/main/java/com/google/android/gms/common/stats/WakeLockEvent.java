package com.google.android.gms.common.stats;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.util.List;

/* compiled from: Unknown */
public final class WakeLockEvent implements SafeParcelable {
    public static final Creator<WakeLockEvent> CREATOR = new zzg();
    final int mVersionCode;
    private final String zzafU;
    private final int zzafV;
    private final List<String> zzafW;
    private final String zzafX;
    private int zzafY;
    private final String zzafZ;
    private final long zzafl;
    private int zzafm;
    private final long zzaft;
    private long zzafv = -1;
    private final String zzaga;
    private final float zzagb;

    WakeLockEvent(int versionCode, long timeMillis, int eventType, String wakelockName, int wakelockType, List<String> callingPackages, String eventKey, long elapsedRealtime, int deviceState, String secondaryWakeLockName, String hostPackageName, float beginPowerPercentage) {
        this.mVersionCode = versionCode;
        this.zzafl = timeMillis;
        this.zzafm = eventType;
        this.zzafU = wakelockName;
        this.zzafZ = secondaryWakeLockName;
        this.zzafV = wakelockType;
        this.zzafW = callingPackages;
        this.zzafX = eventKey;
        this.zzaft = elapsedRealtime;
        this.zzafY = deviceState;
        this.zzaga = hostPackageName;
        this.zzagb = beginPowerPercentage;
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
        zzg.zza(this, out, flags);
    }

    public String zzpC() {
        return this.zzafX;
    }

    public long zzpE() {
        return this.zzaft;
    }

    public String zzpG() {
        return this.zzafU;
    }

    public String zzpH() {
        return this.zzafZ;
    }

    public int zzpI() {
        return this.zzafV;
    }

    public List<String> zzpJ() {
        return this.zzafW;
    }

    public int zzpK() {
        return this.zzafY;
    }

    public String zzpL() {
        return this.zzaga;
    }

    public float zzpM() {
        return this.zzagb;
    }
}
