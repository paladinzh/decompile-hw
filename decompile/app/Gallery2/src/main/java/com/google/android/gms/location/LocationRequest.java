package com.google.android.gms.location;

import android.os.Parcel;
import android.os.SystemClock;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.ep;

/* compiled from: Unknown */
public final class LocationRequest implements SafeParcelable {
    public static final LocationRequestCreator CREATOR = new LocationRequestCreator();
    long KV;
    long Lc;
    long Ld;
    boolean Le;
    int Lf;
    float Lg;
    int mPriority;
    private final int wj;

    public LocationRequest() {
        this.wj = 1;
        this.mPriority = 102;
        this.Lc = 3600000;
        this.Ld = 600000;
        this.Le = false;
        this.KV = Long.MAX_VALUE;
        this.Lf = Integer.MAX_VALUE;
        this.Lg = 0.0f;
    }

    LocationRequest(int versionCode, int priority, long interval, long fastestInterval, boolean explicitFastestInterval, long expireAt, int numUpdates, float smallestDisplacement) {
        this.wj = versionCode;
        this.mPriority = priority;
        this.Lc = interval;
        this.Ld = fastestInterval;
        this.Le = explicitFastestInterval;
        this.KV = expireAt;
        this.Lf = numUpdates;
        this.Lg = smallestDisplacement;
    }

    public static String bj(int i) {
        switch (i) {
            case 100:
                return "PRIORITY_HIGH_ACCURACY";
            case 102:
                return "PRIORITY_BALANCED_POWER_ACCURACY";
            case 104:
                return "PRIORITY_LOW_POWER";
            case 105:
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
        if (this.mPriority != locationRequest.mPriority || this.Lc != locationRequest.Lc || this.Ld != locationRequest.Ld || this.Le != locationRequest.Le || this.KV != locationRequest.KV || this.Lf != locationRequest.Lf || this.Lg != locationRequest.Lg) {
            z = false;
        }
        return z;
    }

    int getVersionCode() {
        return this.wj;
    }

    public int hashCode() {
        return ep.hashCode(Integer.valueOf(this.mPriority), Long.valueOf(this.Lc), Long.valueOf(this.Ld), Boolean.valueOf(this.Le), Long.valueOf(this.KV), Integer.valueOf(this.Lf), Float.valueOf(this.Lg));
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Request[").append(bj(this.mPriority));
        if (this.mPriority != 105) {
            stringBuilder.append(" requested=");
            stringBuilder.append(this.Lc + "ms");
        }
        stringBuilder.append(" fastest=");
        stringBuilder.append(this.Ld + "ms");
        if (this.KV != Long.MAX_VALUE) {
            long elapsedRealtime = this.KV - SystemClock.elapsedRealtime();
            stringBuilder.append(" expireIn=");
            stringBuilder.append(elapsedRealtime + "ms");
        }
        if (this.Lf != Integer.MAX_VALUE) {
            stringBuilder.append(" num=").append(this.Lf);
        }
        stringBuilder.append(']');
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel parcel, int flags) {
        LocationRequestCreator.a(this, parcel, flags);
    }
}
