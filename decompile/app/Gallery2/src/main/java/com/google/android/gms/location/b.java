package com.google.android.gms.location;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.ep;

/* compiled from: Unknown */
public class b implements SafeParcelable {
    public static final c CREATOR = new c();
    int Lh;
    int Li;
    long Lj;
    private final int wj;

    b(int i, int i2, int i3, long j) {
        this.wj = i;
        this.Lh = i2;
        this.Li = i3;
        this.Lj = j;
    }

    private String bk(int i) {
        switch (i) {
            case 0:
                return "STATUS_SUCCESSFUL";
            case 2:
                return "STATUS_TIMED_OUT_ON_SCAN";
            case 3:
                return "STATUS_NO_INFO_IN_DATABASE";
            case 4:
                return "STATUS_INVALID_SCAN";
            case 5:
                return "STATUS_UNABLE_TO_QUERY_DATABASE";
            case 6:
                return "STATUS_SCANS_DISABLED_IN_SETTINGS";
            case 7:
                return "STATUS_LOCATION_DISABLED_IN_SETTINGS";
            case 8:
                return "STATUS_IN_PROGRESS";
            default:
                return "STATUS_UNKNOWN";
        }
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!(other instanceof b)) {
            return false;
        }
        b bVar = (b) other;
        if (this.Lh == bVar.Lh && this.Li == bVar.Li && this.Lj == bVar.Lj) {
            z = true;
        }
        return z;
    }

    int getVersionCode() {
        return this.wj;
    }

    public int hashCode() {
        return ep.hashCode(Integer.valueOf(this.Lh), Integer.valueOf(this.Li), Long.valueOf(this.Lj));
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("LocationStatus[cell status: ").append(bk(this.Lh));
        stringBuilder.append(", wifi status: ").append(bk(this.Li));
        stringBuilder.append(", elapsed realtime ns: ").append(this.Lj);
        stringBuilder.append(']');
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel parcel, int flags) {
        c.a(this, parcel, flags);
    }
}
