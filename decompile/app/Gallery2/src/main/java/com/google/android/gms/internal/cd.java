package com.google.android.gms.internal;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public final class cd implements SafeParcelable {
    public static final ce CREATOR = new ce();
    public final String adUnitId;
    public final ApplicationInfo applicationInfo;
    public final db kN;
    public final ab kQ;
    public final Bundle ob;
    public final z oc;
    public final PackageInfo od;
    public final String oe;
    public final String of;
    public final String og;
    public final Bundle oh;
    public final int versionCode;

    cd(int i, Bundle bundle, z zVar, ab abVar, String str, ApplicationInfo applicationInfo, PackageInfo packageInfo, String str2, String str3, String str4, db dbVar, Bundle bundle2) {
        this.versionCode = i;
        this.ob = bundle;
        this.oc = zVar;
        this.kQ = abVar;
        this.adUnitId = str;
        this.applicationInfo = applicationInfo;
        this.od = packageInfo;
        this.oe = str2;
        this.of = str3;
        this.og = str4;
        this.kN = dbVar;
        this.oh = bundle2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        ce.a(this, out, flags);
    }
}
