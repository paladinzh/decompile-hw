package com.google.android.gms.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.location.LocationRequest;

/* compiled from: Unknown */
public final class hr implements SafeParcelable {
    public static final hs CREATOR = new hs();
    private final LocationRequest LF;
    private final hn LG;
    final int wj;

    public hr(int i, LocationRequest locationRequest, hn hnVar) {
        this.wj = i;
        this.LF = locationRequest;
        this.LG = hnVar;
    }

    public int describeContents() {
        hs hsVar = CREATOR;
        return 0;
    }

    public boolean equals(Object object) {
        boolean z = true;
        if (this == object) {
            return true;
        }
        if (!(object instanceof hr)) {
            return false;
        }
        hr hrVar = (hr) object;
        if (this.LF.equals(hrVar.LF)) {
            if (!this.LG.equals(hrVar.LG)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public LocationRequest gu() {
        return this.LF;
    }

    public hn gv() {
        return this.LG;
    }

    public int hashCode() {
        return ep.hashCode(this.LF, this.LG);
    }

    public String toString() {
        return ep.e(this).a("locationRequest", this.LF).a("filter", this.LG).toString();
    }

    public void writeToParcel(Parcel parcel, int flags) {
        hs hsVar = CREATOR;
        hs.a(this, parcel, flags);
    }
}
