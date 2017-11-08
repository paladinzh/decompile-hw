package com.google.android.gms.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public final class ee$a implements SafeParcelable {
    public static final eq CREATOR = new eq();
    private final List<String> Bp = new ArrayList();
    private final String vi;
    private final int wj;
    private final int zo;
    private final String zq;

    ee$a(int i, String str, List<String> list, int i2, String str2) {
        this.wj = i;
        this.vi = str;
        this.Bp.addAll(list);
        this.zo = i2;
        this.zq = str2;
    }

    public int dS() {
        return this.zo;
    }

    public List<String> dT() {
        return new ArrayList(this.Bp);
    }

    public String dV() {
        return this.zq;
    }

    public int describeContents() {
        return 0;
    }

    public String getAccountName() {
        return this.vi;
    }

    public int getVersionCode() {
        return this.wj;
    }

    public void writeToParcel(Parcel out, int flags) {
        eq.a(this, out, flags);
    }
}
