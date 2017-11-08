package com.google.android.gms.drive.query;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.drive.query.internal.LogicalFilter;

/* compiled from: Unknown */
public class Query implements SafeParcelable {
    public static final Creator<Query> CREATOR = new a();
    LogicalFilter EL;
    String EM;
    final int wj;

    Query(int versionCode, LogicalFilter clause, String pageToken) {
        this.wj = versionCode;
        this.EL = clause;
        this.EM = pageToken;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        a.a(this, out, flags);
    }
}
