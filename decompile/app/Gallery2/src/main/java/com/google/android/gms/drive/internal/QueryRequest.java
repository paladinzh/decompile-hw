package com.google.android.gms.drive.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.drive.query.Query;

/* compiled from: Unknown */
public class QueryRequest implements SafeParcelable {
    public static final Creator<QueryRequest> CREATOR = new ai();
    final Query Ef;
    final int wj;

    QueryRequest(int versionCode, Query query) {
        this.wj = versionCode;
        this.Ef = query;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        ai.a(this, dest, flags);
    }
}
