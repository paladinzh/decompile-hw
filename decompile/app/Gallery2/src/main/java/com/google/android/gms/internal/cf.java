package com.google.android.gms.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.util.Collections;
import java.util.List;

/* compiled from: Unknown */
public final class cf implements SafeParcelable {
    public static final cg CREATOR = new cg();
    public final int errorCode;
    public final List<String> mt;
    public final List<String> mu;
    public final long mx;
    public final String nw;
    public final String oi;
    public final long oj;
    public final boolean ok;
    public final long ol;
    public final List<String> om;
    public final String on;
    public final long oo;
    public final int orientation;
    public final int versionCode;

    cf(int i, String str, String str2, List<String> list, int i2, List<String> list2, long j, boolean z, long j2, List<String> list3, long j3, int i3, String str3, long j4) {
        this.versionCode = i;
        this.nw = str;
        this.oi = str2;
        this.mt = list == null ? null : Collections.unmodifiableList(list);
        this.errorCode = i2;
        this.mu = list2 == null ? null : Collections.unmodifiableList(list2);
        this.oj = j;
        this.ok = z;
        this.ol = j2;
        this.om = list3 == null ? null : Collections.unmodifiableList(list3);
        this.mx = j3;
        this.orientation = i3;
        this.on = str3;
        this.oo = j4;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        cg.a(this, out, flags);
    }
}
