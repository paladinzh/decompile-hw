package com.google.android.gms.drive.query.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.drive.metadata.CollectionMetadataField;
import com.google.android.gms.drive.metadata.internal.MetadataBundle;
import com.google.android.gms.drive.query.Filter;

/* compiled from: Unknown */
public class InFilter<T> implements SafeParcelable, Filter {
    public static final e CREATOR = new e();
    final MetadataBundle EP;
    private final CollectionMetadataField<T> EX;
    final int wj;

    InFilter(int versionCode, MetadataBundle value) {
        this.wj = versionCode;
        this.EP = value;
        this.EX = (CollectionMetadataField) d.b(value);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        e.a(this, out, flags);
    }
}
