package com.google.android.gms.drive.query.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.drive.query.Filter;

/* compiled from: Unknown */
public class FilterHolder implements SafeParcelable {
    public static final Creator<FilterHolder> CREATOR = new c();
    final ComparisonFilter<?> ER;
    final FieldOnlyFilter ES;
    final LogicalFilter ET;
    final NotFilter EU;
    final InFilter<?> EV;
    private final Filter EW;
    final int wj;

    FilterHolder(int versionCode, ComparisonFilter<?> comparisonField, FieldOnlyFilter fieldOnlyFilter, LogicalFilter logicalFilter, NotFilter notFilter, InFilter<?> containsFilter) {
        Filter filter;
        this.wj = versionCode;
        this.ER = comparisonField;
        this.ES = fieldOnlyFilter;
        this.ET = logicalFilter;
        this.EU = notFilter;
        this.EV = containsFilter;
        if (this.ER != null) {
            filter = this.ER;
        } else if (this.ES != null) {
            filter = this.ES;
        } else if (this.ET != null) {
            filter = this.ET;
        } else if (this.EU != null) {
            filter = this.EU;
        } else if (this.EV == null) {
            throw new IllegalArgumentException("At least one filter must be set.");
        } else {
            filter = this.EV;
        }
        this.EW = filter;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        c.a(this, out, flags);
    }
}
