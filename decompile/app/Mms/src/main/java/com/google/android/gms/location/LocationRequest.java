package com.google.android.gms.location;

import android.os.Parcel;
import android.os.SystemClock;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;

/* compiled from: Unknown */
public final class LocationRequest implements SafeParcelable {
    public static final LocationRequestCreator CREATOR = new LocationRequestCreator();
    public static final int PRIORITY_BALANCED_POWER_ACCURACY = 102;
    public static final int PRIORITY_HIGH_ACCURACY = 100;
    public static final int PRIORITY_LOW_POWER = 104;
    public static final int PRIORITY_NO_POWER = 105;
    int mPriority;
    private final int mVersionCode;
    boolean zzaBr;
    long zzaND;
    long zzaNY;
    long zzaNZ;
    int zzaOa;
    float zzaOb;
    long zzaOc;

    public LocationRequest() {
        this.mVersionCode = 1;
        this.mPriority = 102;
        this.zzaNY = 3600000;
        this.zzaNZ = 600000;
        this.zzaBr = false;
        this.zzaND = Long.MAX_VALUE;
        this.zzaOa = Integer.MAX_VALUE;
        this.zzaOb = 0.0f;
        this.zzaOc = 0;
    }

    LocationRequest(int versionCode, int priority, long interval, long fastestInterval, boolean explicitFastestInterval, long expireAt, int numUpdates, float smallestDisplacement, long maxWaitTime) {
        this.mVersionCode = versionCode;
        this.mPriority = priority;
        this.zzaNY = interval;
        this.zzaNZ = fastestInterval;
        this.zzaBr = explicitFastestInterval;
        this.zzaND = expireAt;
        this.zzaOa = numUpdates;
        this.zzaOb = smallestDisplacement;
        this.zzaOc = maxWaitTime;
    }

    public static LocationRequest create() {
        return new LocationRequest();
    }

    private static void zzL(long j) {
        if ((j >= 0 ? 1 : null) == null) {
            throw new IllegalArgumentException("invalid interval: " + j);
        }
    }

    private static void zzd(float f) {
        if (f < 0.0f) {
            throw new IllegalArgumentException("invalid displacement: " + f);
        }
    }

    private static void zzhs(int i) {
        switch (i) {
            case 100:
            case 102:
            case PRIORITY_LOW_POWER /*104*/:
            case PRIORITY_NO_POWER /*105*/:
                return;
            default:
                throw new IllegalArgumentException("invalid quality: " + i);
        }
    }

    public static String zzht(int i) {
        switch (i) {
            case 100:
                return "PRIORITY_HIGH_ACCURACY";
            case 102:
                return "PRIORITY_BALANCED_POWER_ACCURACY";
            case PRIORITY_LOW_POWER /*104*/:
                return "PRIORITY_LOW_POWER";
            case PRIORITY_NO_POWER /*105*/:
                return "PRIORITY_NO_POWER";
            default:
                return "???";
        }
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object object) {
        boolean z = true;
        if (this == object) {
            return true;
        }
        if (!(object instanceof LocationRequest)) {
            return false;
        }
        LocationRequest locationRequest = (LocationRequest) object;
        if (this.mPriority != locationRequest.mPriority || this.zzaNY != locationRequest.zzaNY || this.zzaNZ != locationRequest.zzaNZ || this.zzaBr != locationRequest.zzaBr || this.zzaND != locationRequest.zzaND || this.zzaOa != locationRequest.zzaOa || this.zzaOb != locationRequest.zzaOb) {
            z = false;
        }
        return z;
    }

    public long getExpirationTime() {
        return this.zzaND;
    }

    public long getFastestInterval() {
        return this.zzaNZ;
    }

    public long getInterval() {
        return this.zzaNY;
    }

    public long getMaxWaitTime() {
        long j = this.zzaOc;
        return ((j > this.zzaNY ? 1 : (j == this.zzaNY ? 0 : -1)) >= 0 ? 1 : null) == null ? this.zzaNY : j;
    }

    public int getNumUpdates() {
        return this.zzaOa;
    }

    public int getPriority() {
        return this.mPriority;
    }

    public float getSmallestDisplacement() {
        return this.zzaOb;
    }

    int getVersionCode() {
        return this.mVersionCode;
    }

    public int hashCode() {
        return zzw.hashCode(Integer.valueOf(this.mPriority), Long.valueOf(this.zzaNY), Long.valueOf(this.zzaNZ), Boolean.valueOf(this.zzaBr), Long.valueOf(this.zzaND), Integer.valueOf(this.zzaOa), Float.valueOf(this.zzaOb));
    }

    public LocationRequest setExpirationDuration(long millis) {
        Object obj = 1;
        long elapsedRealtime = SystemClock.elapsedRealtime();
        if ((millis <= Long.MAX_VALUE - elapsedRealtime ? 1 : null) == null) {
            this.zzaND = Long.MAX_VALUE;
        } else {
            this.zzaND = millis + elapsedRealtime;
        }
        if (this.zzaND < 0) {
            obj = null;
        }
        if (obj == null) {
            this.zzaND = 0;
        }
        return this;
    }

    public LocationRequest setExpirationTime(long millis) {
        this.zzaND = millis;
        if ((this.zzaND >= 0 ? 1 : null) == null) {
            this.zzaND = 0;
        }
        return this;
    }

    public LocationRequest setFastestInterval(long millis) {
        zzL(millis);
        this.zzaBr = true;
        this.zzaNZ = millis;
        return this;
    }

    public LocationRequest setInterval(long millis) {
        zzL(millis);
        this.zzaNY = millis;
        if (!this.zzaBr) {
            this.zzaNZ = (long) (((double) this.zzaNY) / 6.0d);
        }
        return this;
    }

    public LocationRequest setMaxWaitTime(long millis) {
        zzL(millis);
        this.zzaOc = millis;
        return this;
    }

    public LocationRequest setNumUpdates(int numUpdates) {
        if (numUpdates > 0) {
            this.zzaOa = numUpdates;
            return this;
        }
        throw new IllegalArgumentException("invalid numUpdates: " + numUpdates);
    }

    public LocationRequest setPriority(int priority) {
        zzhs(priority);
        this.mPriority = priority;
        return this;
    }

    public LocationRequest setSmallestDisplacement(float smallestDisplacementMeters) {
        zzd(smallestDisplacementMeters);
        this.zzaOb = smallestDisplacementMeters;
        return this;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Request[").append(zzht(this.mPriority));
        if (this.mPriority != PRIORITY_NO_POWER) {
            stringBuilder.append(" requested=");
            stringBuilder.append(this.zzaNY).append("ms");
        }
        stringBuilder.append(" fastest=");
        stringBuilder.append(this.zzaNZ).append("ms");
        if ((this.zzaOc <= this.zzaNY ? 1 : null) == null) {
            stringBuilder.append(" maxWait=");
            stringBuilder.append(this.zzaOc).append("ms");
        }
        if (this.zzaND != Long.MAX_VALUE) {
            long elapsedRealtime = this.zzaND - SystemClock.elapsedRealtime();
            stringBuilder.append(" expireIn=");
            stringBuilder.append(elapsedRealtime).append("ms");
        }
        if (this.zzaOa != Integer.MAX_VALUE) {
            stringBuilder.append(" num=").append(this.zzaOa);
        }
        stringBuilder.append(']');
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel parcel, int flags) {
        LocationRequestCreator.zza(this, parcel, flags);
    }
}
