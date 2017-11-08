package com.google.android.gms.drive.query.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.drive.metadata.MetadataField;
import com.google.android.gms.drive.metadata.internal.MetadataBundle;
import com.google.android.gms.drive.query.Filter;

/* compiled from: Unknown */
public class ComparisonFilter<T> implements SafeParcelable, Filter {
    public static final a CREATOR = new a();
    final Operator EO;
    final MetadataBundle EP;
    final MetadataField<T> EQ;
    final int wj;

    ComparisonFilter(int versionCode, Operator operator, MetadataBundle value) {
        this.wj = versionCode;
        this.EO = operator;
        this.EP = value;
        this.EQ = d.b(value);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        a.a(this, out, flags);
    }
}
