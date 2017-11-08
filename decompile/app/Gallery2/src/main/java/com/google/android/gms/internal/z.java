package com.google.android.gms.internal;

import android.location.Location;
import android.os.Bundle;
import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.util.List;

/* compiled from: Unknown */
public final class z implements SafeParcelable {
    public static final aa CREATOR = new aa();
    public final Bundle extras;
    public final long le;
    public final int lf;
    public final List<String> lg;
    public final boolean lh;
    public final boolean li;
    public final String lj;
    public final am lk;
    public final Location ll;
    public final String lm;
    public final int tagForChildDirectedTreatment;
    public final int versionCode;

    z(int i, long j, Bundle bundle, int i2, List<String> list, boolean z, int i3, boolean z2, String str, am amVar, Location location, String str2) {
        this.versionCode = i;
        this.le = j;
        this.extras = bundle;
        this.lf = i2;
        this.lg = list;
        this.lh = z;
        this.tagForChildDirectedTreatment = i3;
        this.li = z2;
        this.lj = str;
        this.lk = amVar;
        this.ll = location;
        this.lm = str2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        aa.a(this, out, flags);
    }
}
